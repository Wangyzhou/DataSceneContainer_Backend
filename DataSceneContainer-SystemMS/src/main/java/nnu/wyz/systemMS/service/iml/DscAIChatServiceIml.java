package nnu.wyz.systemMS.service.iml;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.config.DockerClientConfig;
import nnu.wyz.systemMS.dao.DscDASceneConfigDAO;
import nnu.wyz.systemMS.model.dto.ChatDTO;
import nnu.wyz.systemMS.model.entity.DscDASceneConfig;
import nnu.wyz.systemMS.service.DscAIChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Objects;

/**
 * @description:
 * @author: yzwang
 * @time: 2024/4/17 14:56
 */
@Service
@Slf4j
public class DscAIChatServiceIml implements DscAIChatService {

    @Autowired
    private DscDASceneConfigDAO dscDASceneConfigDAO;

    @Autowired
    private DockerClientConfig dockerClientConfig;

    @Value("${ollama_container}")
    private String OLLAMA_CONTAINER;

    @SneakyThrows
    @Override
    public CommonResult<JSONArray> chat(ChatDTO chatDTO) {
        DscDASceneConfig bySceneId = dscDASceneConfigDAO.findBySceneId(chatDTO.getSceneId());
        if (Objects.isNull(bySceneId)) {
            return CommonResult.failed("场景不存在");
        }
        DockerClient dockerClient = dockerClientConfig.getDockerClient();
        ExecCreateCmdResponse exec = dockerClient.execCreateCmd(OLLAMA_CONTAINER)
                .withCmd("python", "main_for_one_chat.py", "-MODEL", "llama3:8b", "-IDENTIFIER", chatDTO.getSceneId(), "-PROMPT", chatDTO.getQuestion())
                .withAttachStdout(true)
                .withAttachStderr(true)
                .exec();
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream stderr = new PrintStream(baos);
        StringBuilder sb = new StringBuilder();
        dockerClient.execStartCmd(exec.getId())
                .exec(new ExecStartResultCallback(stdout, stderr) {
                    @Override
                    public void onNext(Frame frame) {
                        sb.append(frame.toString().replace("STDOUT: ", ""));
                        super.onNext(frame);
                    }
                }).awaitCompletion();
        JSONArray objects = JSON.parseArray(sb.toString());
        stderr.close();
        baos.close();
        return CommonResult.success(objects);
    }
}
