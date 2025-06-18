package nnu.wyz.systemMS.service.iml;

import lombok.extern.slf4j.Slf4j;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.dao.DscCode.GeoActScriptFileDAD;
import nnu.wyz.systemMS.dao.DscCode.GeoActScriptFolderDAO;
import nnu.wyz.systemMS.model.dto.DscCode.GeoActFileNode;
import nnu.wyz.systemMS.model.dto.DscCode.GeoActFolderDTO;
import nnu.wyz.systemMS.model.dto.DscCode.GeoActScriptDTO;
import nnu.wyz.systemMS.model.dto.StringPackage;
import nnu.wyz.systemMS.model.entity.codeModel.GeoActFolder;
import nnu.wyz.systemMS.model.entity.codeModel.GeoActScriptFile;
import nnu.wyz.systemMS.service.DscCode.GeoActService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

@Slf4j
@Service
public class GeoActServiceImpl implements GeoActService {

    @Value("${codeOutPutHub}")
    private String codeOutPutHub;

    @Autowired
    private GeoActScriptFileDAD geoActScriptFileDao;

    @Autowired
    private GeoActScriptFolderDAO geoActScriptFolderDao;


    @Override
    public CommonResult<?> initFileTree(String userId, String sceneId) {
        List<GeoActFolder> folders = geoActScriptFolderDao.findByExecutorAndSceneId(userId, sceneId);
        List<GeoActScriptFile> files = geoActScriptFileDao.findByExecutorAndSceneId(userId, sceneId);
        List<GeoActFileNode> tree = buildFileTree(folders, files);
        return CommonResult.success(tree);
    }

    @Override
    public CommonResult<?> createSingleFile(GeoActScriptDTO geoActScriptDTO) {
        try {
            // === 判断是否已存在同名同类型文件 ===
            Optional<GeoActScriptFile> existing = geoActScriptFileDao
                    .findByParentIdAndFileNameAndType(
                            geoActScriptDTO.getParentId(),
                            geoActScriptDTO.getFileName(),
                            geoActScriptDTO.getType()
                    );

            if (existing.isPresent()) {
                return CommonResult.failed("文件已存在，名称和类型重复，禁止创建");
            }

            // 1. 构造实体对象
            GeoActScriptFile scriptFile = new GeoActScriptFile(geoActScriptDTO);

            //更新父文件夹孩子列表
            if(!updateParentsChildren(scriptFile)){
                return CommonResult.failed("父文件夹不存在！请联系管理员！");
            }

            // 生成文件路径
            String fullPath = buildPath(
                    geoActScriptDTO.getExecutor(),
                    geoActScriptDTO.getSceneId(),
                    geoActScriptDTO.getParentId()
            );

            // 确保目录存在
            File dir = new File(fullPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 写入文件（目前只支持 .py）
            if (".py".equalsIgnoreCase(geoActScriptDTO.getType())) {
                File file = new File(dir, scriptFile.getId() + ".py");
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    writer.write(geoActScriptDTO.getScript() != null ? geoActScriptDTO.getScript() : "");
                }
            } else {
                return CommonResult.failed("暂不支持该文件类型: " + geoActScriptDTO.getType());
            }

            // 2. 保存到数据库
            geoActScriptFileDao.save(scriptFile);

            return CommonResult.success("文件创建成功");
        } catch (Exception e) {
            e.printStackTrace();
            return CommonResult.failed("创建文件失败：" + e.getMessage());
        }
    }

    /**
     * 构建文件的完整路径（基础路径 + GeoActProject/userId/sceneId/parent链）
     */
    private String buildPath(String userId, String sceneId, String parentId) {
        List<String> folderNames = new ArrayList<>();

        // 从子到父逐层遍历构建路径
        while (parentId != null) {
            Optional<GeoActFolder> folderOpt = geoActScriptFolderDao.findById(parentId);
            if (folderOpt.isPresent()) {
                GeoActFolder folder = folderOpt.get();
                folderNames.add(folder.getId());
                parentId = folder.getParentId();
            } else {
                break; // 若找不到父节点，退出防止死循环
            }
        }

        // 构建基础路径
        StringBuilder pathBuilder = new StringBuilder();
        pathBuilder.append(codeOutPutHub)
                .append(File.separator).append("GeoActProject")
                .append(File.separator).append(userId)
                .append(File.separator).append(sceneId);

        // 加入文件夹路径（注意逆序）
        for (int i = folderNames.size() - 1; i >= 0; i--) {
            pathBuilder.append(File.separator).append(folderNames.get(i));
        }

        return pathBuilder.toString();
    }


    /**
     * 更新父文件夹孩子列表
     */
    private boolean updateParentsChildren(GeoActScriptFile geoActScriptFile) {
        String parentId = geoActScriptFile.getParentId();
        if (parentId == null) {
            // 没有父文件夹，不需要更新
            return true;
        }

        Optional<GeoActFolder> folderOpt = geoActScriptFolderDao.findById(parentId);
        if (folderOpt.isPresent()) {
            GeoActFolder parentFolder = folderOpt.get();
            List<String> children = parentFolder.getChildren();
            if (children == null) {
                children = new ArrayList<>();
            }
            if (!children.contains(geoActScriptFile.getId())) {
                children.add(geoActScriptFile.getId());
                parentFolder.setChildren(children);
                geoActScriptFolderDao.save(parentFolder);
            }else {
                children.remove(geoActScriptFile.getId());
                parentFolder.setChildren(children);
                geoActScriptFolderDao.save(parentFolder);
            }
            return true;
        } else {
            // 有父文件夹但数据库不存在，记录异常或处理错误
            return false;
        }
    }

    /**
     *递归删除
     */
    private void deleteFolderRecursivelyFromDB(String folderId) {
        // 1. 删除文件夹下所有文件
        List<GeoActScriptFile> files = geoActScriptFileDao.findByParentId(folderId);
        for (GeoActScriptFile file : files) {
            geoActScriptFileDao.deleteById(file.getId());
        }

        // 2. 查找所有子文件夹
        List<GeoActFolder> subFolders = geoActScriptFolderDao.findByParentId(folderId);
        for (GeoActFolder subFolder : subFolders) {
            deleteFolderRecursivelyFromDB(subFolder.getId()); // 递归删除
        }

        // 3. 删除当前文件夹
        geoActScriptFolderDao.deleteById(folderId);
    }
    
    private void deleteRecursive(File file) {
        if (file.isDirectory()) {
            for (File sub : Objects.requireNonNull(file.listFiles())) {
                deleteRecursive(sub);
            }
        }
        file.delete();
    }



    @Override
    public CommonResult<?> deleteSingleFile(String id) {
        try {
            // 1. 从数据库中查找文件是否存在
            Optional<GeoActScriptFile> fileOptional = geoActScriptFileDao.findById(id);
            if (!fileOptional.isPresent()) {
                return CommonResult.failed("文件不存在");
            }

            GeoActScriptFile file = fileOptional.get();

            // 2. 删除磁盘文件
            String fullPath = buildPath(file.getExecutor(), file.getSceneId(), file.getParentId());
            File target = new File(fullPath, file.getId() + file.getType());
            if (target.exists()) {
                if(!target.delete()){
                    return CommonResult.failed("删除失败！");
                }
            }else {
                return CommonResult.failed("删除失败！文件不存在！");
            }

            //更新父文件夹孩子列表
            if(!updateParentsChildren(file)){
                return CommonResult.failed("父文件夹不存在！请联系管理员！");
            }

            // 3. 删除数据库记录
            geoActScriptFileDao.deleteById(file.getId());

            return CommonResult.success("文件删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return CommonResult.failed("文件删除失败：" + e.getMessage());
        }
    }

    @Override
    public CommonResult<?> updateSingleFile(String fileId, StringPackage updateScript) {
        Optional<GeoActScriptFile> fileOptional = geoActScriptFileDao.findById(fileId);
        if (!fileOptional.isPresent()) {
            return CommonResult.failed("文件不存在！");
        }else {
            GeoActScriptFile file = fileOptional.get();
            String fullPath = buildPath(file.getExecutor(), file.getSceneId(), file.getParentId());
            File target = new File(fullPath, file.getId() + file.getType());
            if (!target.exists()) {
                return CommonResult.failed("文件不存在！");
            }else {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(target))) {
                    writer.write(updateScript.getContent() != null ? updateScript.getContent() : "");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return CommonResult.success("文件修改成功！");
            }
        }
    }



    @Override
    public CommonResult<?> createFolder(GeoActFolderDTO geoActFolderDTO) {

        GeoActFolder geoActFolder = new GeoActFolder(geoActFolderDTO);

        // 判断是否有重名文件夹
        boolean exists = geoActScriptFolderDao.existsByFolderNameAndSceneIdAndParentId(
                geoActFolder.getFolderName(), geoActFolder.getSceneId(), geoActFolder.getParentId()
        );
        if (exists) {
            return CommonResult.failed("已存在相同名称的文件夹！");
        }

        // 生成路径
        String fullPath = buildPath(geoActFolder.getExecutor(), geoActFolder.getSceneId(), geoActFolder.getParentId());
        File dir = new File(fullPath, geoActFolder.getId());
        if (!dir.exists()) {
            boolean success = dir.mkdirs();
            if (!success) {
                return CommonResult.failed("磁盘创建文件夹失败");
            }
        }

        // 保存数据库记录
        geoActScriptFolderDao.save(geoActFolder);

        // 更新父节点 children
        if (geoActFolder.getParentId() != null) {
            geoActScriptFolderDao.findById(geoActFolder.getParentId()).ifPresent(parent -> {
                List<String> children = parent.getChildren() == null ? new ArrayList<>() : parent.getChildren();
                if (!children.contains(geoActFolder.getId())) {
                    children.add(geoActFolder.getId());
                    parent.setChildren(children);
                    geoActScriptFolderDao.save(parent);
                }
            });
        }

        return CommonResult.success("文件夹创建成功");
    }


    @Override
    public CommonResult<?> deleteFolder(String id) {
        Optional<GeoActFolder> folderOpt = geoActScriptFolderDao.findById(id);
        if (!folderOpt.isPresent()) {
            return CommonResult.failed("文件夹不存在");
        }

        GeoActFolder geoActFolder = folderOpt.get();

        // 删除磁盘文件夹（递归删除）
        String fullPath = buildPath(geoActFolder.getExecutor(), geoActFolder.getSceneId(), geoActFolder.getParentId());
        File dir = new File(fullPath, geoActFolder.getId());
        if (dir.exists()) {
            deleteRecursive(dir);
        }

        // 删除数据库中该文件夹及其所有子文件夹和文件
        deleteFolderRecursivelyFromDB(geoActFolder.getId());

        // 从父节点中移除自己
        if (geoActFolder.getParentId() != null) {
            geoActScriptFolderDao.findById(geoActFolder.getParentId()).ifPresent(parent -> {
                List<String> children = parent.getChildren();
                if (children != null && children.contains(geoActFolder.getId())) {
                    children.remove(geoActFolder.getId());
                    parent.setChildren(children);
                    geoActScriptFolderDao.save(parent);
                }
            });
        }

        // 删除数据库记录
        geoActScriptFolderDao.deleteById(geoActFolder.getId());
        return CommonResult.success("文件夹删除成功");
    }

    @Override
    public CommonResult<?> getScriptContent(String scriptId) {
        Optional<GeoActScriptFile> fileOptional = geoActScriptFileDao.findById(scriptId);
        if (!fileOptional.isPresent()) {
            return CommonResult.failed("文件不存在！");
        }else {
            GeoActScriptFile file = fileOptional.get();
            String fullParentPath = buildPath(file.getExecutor(), file.getSceneId(), file.getParentId());
            String fullFileName = file.getId() + file.getType();
            File fullPath = new File(fullParentPath, fullFileName);
            if (!fullPath.exists()) {
                return CommonResult.failed("文件存储损坏，请联系管理员！");
            }else {
                try {
                    // 使用 UTF-8 编码读取文件内容
                    String content = new String(Files.readAllBytes(fullPath.toPath()), StandardCharsets.UTF_8);
                    StringPackage stringPackage = new StringPackage(content);
                    return CommonResult.success(stringPackage);
                } catch (IOException e) {
                    e.printStackTrace();
                    return CommonResult.failed("读取文件内容失败：" + e.getMessage());
                }
            }
        }
    }

    @Override
    public CommonResult<?> renameNode(GeoActFileNode geoActFileNode) {
        if(Objects.equals(geoActFileNode.getType(), "folder")) {
            Optional<GeoActFolder> folderOpt = geoActScriptFolderDao.findById(geoActFileNode.getId());
            if (folderOpt.isPresent()) {
                GeoActFolder geoActFolder = folderOpt.get();
                geoActFolder.setFolderName(geoActFileNode.getName());
                geoActScriptFolderDao.save(geoActFolder);
                return CommonResult.success("重命名文件夹成功！");
            }else {
                return CommonResult.failed("重命名文件夹失败，文件夹不存在！");
            }
        }else {
            Optional<GeoActScriptFile> fileOpt = geoActScriptFileDao.findById(geoActFileNode.getId());
            if (fileOpt.isPresent()) {
                GeoActScriptFile geoActScriptFile = fileOpt.get();
                geoActScriptFile.setFileName(geoActFileNode.getName());
                geoActScriptFileDao.save(geoActScriptFile);
                return CommonResult.success("重命名文件成功！");
            }else {
                return CommonResult.failed("重命名文件失败，文件不存在！");
            }
        }
    }


    public List<GeoActFileNode> buildFileTree(List<GeoActFolder> folders, List<GeoActScriptFile> files) {
        // 所有节点（包括文件夹和文件）
        Map<String, GeoActFileNode> nodeMap = new HashMap<>();

        // 文件夹节点
        folders.forEach(folder -> {
            GeoActFileNode node = new GeoActFileNode(
                    folder.getId(),
                    folder.getFolderName(),
                    "folder",
                    new ArrayList<>()
            );
            nodeMap.put(folder.getId(), node);
        });

        // 文件节点
        files.forEach(file -> {
            GeoActFileNode node = new GeoActFileNode(
                    file.getId(),
                    file.getFileName(),
                    file.getType(),
                    null // 文件无子节点
            );
            nodeMap.put(file.getId(), node);
        });

        // 构建树结构
        List<GeoActFileNode> rootNodes = new ArrayList<>();

        folders.forEach(folder -> {
            GeoActFileNode currentNode = nodeMap.get(folder.getId());

            // 如果没有父节点，作为根节点
            if (folder.getParentId() == null || folder.getParentId().isEmpty()) {
                rootNodes.add(currentNode);
            }
            // 根据 children 追加子节点（子文件或子文件夹）
            if (folder.getChildren() != null && !folder.getChildren().isEmpty()) {
                folder.getChildren().stream()
                        .map(nodeMap::get)
                        .filter(Objects::nonNull)
                        .forEach(childNode -> {
                            if (currentNode.getChildren() != null) {
                                currentNode.getChildren().add(childNode);
                            }
                        });
            }
        });

        files.forEach(file -> {
            GeoActFileNode node = nodeMap.get(file.getId());

            //没有父节点，作为根节点
            if(file.getParentId() == null || file.getParentId().isEmpty()) {
                rootNodes.add(node);
            }
        });

        // ✅ 排序所有节点（递归）
        sortFileTree(rootNodes);

        return rootNodes;
    }

    // 辅助方法：递归地按 name 排序每个节点的 children
    private void sortFileTree(List<GeoActFileNode> nodes) {
        if (nodes == null || nodes.isEmpty()) return;

        nodes.sort(Comparator.comparing(n -> n.getName().toLowerCase())); // 忽略大小写排序

        for (GeoActFileNode node : nodes) {
            sortFileTree(node.getChildren());
        }
    }


}
