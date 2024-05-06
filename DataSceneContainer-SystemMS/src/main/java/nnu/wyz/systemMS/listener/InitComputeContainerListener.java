package nnu.wyz.systemMS.listener;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.*;
import lombok.extern.slf4j.Slf4j;
import nnu.wyz.systemMS.config.DockerClientConfig;
import nnu.wyz.systemMS.config.DockerConfig;
import nnu.wyz.systemMS.dao.DscComputeContainerImageDAO;
import nnu.wyz.systemMS.dao.DscComputeContainerInstanceDAO;
import nnu.wyz.systemMS.model.entity.DscComputeContainerImage;
import nnu.wyz.systemMS.model.entity.DscComputeContainerInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: yzwang
 * @time: 2024/4/10 17:05
 */
@Component
@Slf4j
public class InitComputeContainerListener implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private DscComputeContainerImageDAO dscComputeContainerImageDAO;

    @Autowired
    private DscComputeContainerInstanceDAO dscComputeContainerInstanceDAO;

    @Autowired
    private DockerClientConfig dockerClientConfig;

    @Value("${project_base_path}")
    private String projectBasePath;

    @Value("${docker_local_host}")
    private String dockerLocalHost;

    @Value("${docker_local_port}")
    private String dockerLocalPort;

    @Autowired
    private DockerConfig dockerConfig;

    /**
     * 1、列出所有需部署的镜像，新建map记录
     * 2、遍历上述镜像，判断数据库是否有容器示例
     * 1)若有：则检查该容器是否可用
     * Ⅰ：可用 -> 添加容器实例，标记所用镜像为true
     * Ⅱ：不可用 -> 删除数据库容器实例, 标记所用镜像为false
     * 2)若无：标记所用镜像为false
     * 3、过滤false的镜像
     * 4、构建镜像（拉取或其他）
     * 5、创建并启动容器
     *
     * @param event
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 1、列出所有需部署的镜像，存入map
        List<DscComputeContainerImage> images = dscComputeContainerImageDAO.findAll();
        if (images.size() == 0) {
            log.error("库中无可用镜像");
            return;
        }
        Map<String, Boolean> computeContainerImageRec = new HashMap<>();
        List<Container> localAllContainer = dockerClientConfig.getDockerClient().listContainersCmd().withShowAll(true).exec();
        List<String> localAllContainerIds = localAllContainer.stream().map(Container::getId).collect(Collectors.toList());
        // 2、遍历上述镜像，判断数据库是否有容器示例
        for (DscComputeContainerImage image : images) {
            List<DscComputeContainerInstance> allByImageId = dscComputeContainerInstanceDAO.findAllByImageId(image.getId());
            // 2)若无：标记所用镜像为false
            if (allByImageId.size() == 0) {
                computeContainerImageRec.put(image.getId(), false);
                continue;
            }
            // 1)若有：则检查该容器是否可用
            DscComputeContainerInstance dscComputeContainerInstance = allByImageId.get(0);
            // Ⅰ：可用 -> 添加容器实例，标记所用镜像为true
            if (localAllContainerIds.contains(dscComputeContainerInstance.getContainerId())) {
                InspectContainerResponse inspectContainerResponse = dockerClientConfig.getDockerClient().inspectContainerCmd(dscComputeContainerInstance.getContainerId()).exec();
                if (Boolean.FALSE.equals(inspectContainerResponse.getState().getRunning())) {
                    dockerClientConfig.getDockerClient().startContainerCmd(dscComputeContainerInstance.getContainerId()).exec();
                }
                computeContainerImageRec.put(image.getId(), true);
                continue;
            }
            // Ⅱ：不可用 -> 删除数据库容器实例, 标记所用镜像为false
            computeContainerImageRec.put(image.getId(), false);
        }
        // 3、过滤false的镜像
        List<DscComputeContainerImage> imagesAfterFilter = images.stream().filter(image -> !computeContainerImageRec.get(image.getId())).collect(Collectors.toList());
        if (imagesAfterFilter.size() == 0) {
            log.info("所有容器均已部署");
            return;
        }
        List<Image> localImages = dockerClientConfig.getDockerClient().listImagesCmd().withShowAll(true).exec();
        ArrayList<String> localImageNames = new ArrayList<>();
        localImages.stream().map(Image::getRepoTags).filter(Objects::nonNull).flatMap(Arrays::stream).forEach(localImageNames::add);
        ExecutorService executorService = Executors.newFixedThreadPool(imagesAfterFilter.size());
        // 4、构建镜像（拉取或其他）
        List<Boolean> finished = imagesAfterFilter.stream().map(image -> CompletableFuture.supplyAsync(() -> {
                    // 若本地存在该镜像，则跳过
                    if (localImageNames.contains(image.getImageName())) {
                        log.info(image.getImageName() + "已存在");
                        return true;
                    }
                    try {
                        dockerClientConfig.getDockerClient().pullImageCmd(image.getImageName()).start().awaitCompletion();
                        return true;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        log.error(image.getImageName() + "镜像拉取失败！");
                        return false;
                    }
                }, executorService)).collect(Collectors.toList()).stream()
                .map(CompletableFuture::join).collect(Collectors.toList());
        //5、创建并启动持续性容器
        imagesAfterFilter.stream().map(image -> CompletableFuture.supplyAsync(() -> {
            if (!image.getRunType().equals("keep-alive")) {
                return image.getImageName() + "为一次性容器镜像，无需初始化！";
            }
            HostConfig hostConfig = new HostConfig();
            Bind bind = new Bind(projectBasePath + "/dsc-minio/data", new Volume("/home/minio-data"));
            hostConfig.setBinds(bind);
            String containerName = image.getImageName().replace("/", "_").replace(":", "_") + "-instance-" + IdUtil.fastSimpleUUID();
            CreateContainerResponse createContainerResponse = dockerClientConfig.getDockerClient().createContainerCmd(image.getImageName())
                    .withName(containerName)
                    .withHostConfig(hostConfig)
                    .exec();
            dockerClientConfig.getDockerClient().startContainerCmd(createContainerResponse.getId()).exec();
            DscComputeContainerInstance dscComputeContainerInstance = new DscComputeContainerInstance(IdUtil.randomUUID(), createContainerResponse.getId(), containerName, image.getId(), dockerLocalHost, dockerLocalPort, dockerConfig.getDocker_tls_verify().equals("yes"), DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
            dscComputeContainerInstanceDAO.insert(dscComputeContainerInstance);
            return image.getImageName() + "创建成功！";
        }, executorService)).collect(Collectors.toList()).stream().map(CompletableFuture::join).forEach(log::info);
        executorService.shutdown();
    }
}
