package nnu.wyz.systemMS.service.iml;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.dao.DscCatalogDAO;
import nnu.wyz.systemMS.dao.DscCode.GeoActModelDAO;
import nnu.wyz.systemMS.dao.DscCode.GeoActScriptFileDAD;
import nnu.wyz.systemMS.dao.DscCode.GeoActScriptFolderDAO;
import nnu.wyz.systemMS.dao.DscCode.GeoActTaskDAO;
import nnu.wyz.systemMS.dao.DscFileDAO;
import nnu.wyz.systemMS.model.dto.CatalogChildrenDTO;
import nnu.wyz.systemMS.model.dto.DscCode.*;
import nnu.wyz.systemMS.model.dto.FundamentalPackage.StringPackage;
import nnu.wyz.systemMS.model.entity.DscCatalog;
import nnu.wyz.systemMS.model.entity.DscFileInfo;
import nnu.wyz.systemMS.model.entity.codeModel.GeoActFolder;
import nnu.wyz.systemMS.model.entity.codeModel.GeoActModel;
import nnu.wyz.systemMS.model.entity.codeModel.GeoActScriptFile;
import nnu.wyz.systemMS.model.entity.codeModel.GeoActTask;
import nnu.wyz.systemMS.model.entity.codeModel.ModelRecommend.DifyKnowledgePiece;
import nnu.wyz.systemMS.model.entity.codeModel.ModelRecommend.Scenario;
import nnu.wyz.systemMS.service.DscCode.GeoActService;
import nnu.wyz.systemMS.service.GeoActAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import org.apache.commons.io.FileUtils;

@Slf4j
@Service
public class GeoActServiceImpl implements GeoActService {

    @Value("${codeOutPutHub}")
    private String codeOutPutHub;

    @Value("${JupyterInnerOutPutHub}")
    private String JupyterInnerOutPutHub;

    @Value("${PythonDocker}")
    private String pythonDocker;

    @Autowired
    private GeoActScriptFileDAD geoActScriptFileDao;

    @Autowired
    private GeoActScriptFolderDAO geoActScriptFolderDao;

    @Autowired
    private GeoActTaskDAO geoActTaskDao;

    @Autowired
    private GeoActModelDAO geoActModelDao;

    @Autowired
    private GeoActAiService geoActAiService;

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private DscFileDAO dscFileDAO;

    @Autowired
    private DscCatalogDAO dscCatalogDAO;


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
                    geoActScriptDTO.getParentId(),
                    "host"
            );

            if(fullPath == null){
                return CommonResult.failed("文件索引平台出错！");
            }

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
                File exFile = new File(dir, scriptFile.getExScriptId() + ".py");
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(exFile))) {
                    writer.write(geoActScriptDTO.getExScript() != null ? geoActScriptDTO.getExScript() : "");
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
    public String buildPath(String userId, String sceneId, String parentId, String platform) {
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
        if(Objects.equals(platform, "host")){
            pathBuilder.append(codeOutPutHub)
                    .append(File.separator).append("GeoActProject")
                    .append(File.separator).append(userId)
                    .append(File.separator).append(sceneId);
        }else if(Objects.equals(platform, "jupyter")){
            pathBuilder.append(JupyterInnerOutPutHub)
                    .append(File.separator).append("GeoActProject")
                    .append(File.separator).append(userId)
                    .append(File.separator).append(sceneId);
        }else {
            return null;
        }


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
            String fullPath = buildPath(file.getExecutor(), file.getSceneId(), file.getParentId(), "host");
            if(fullPath == null){
                return CommonResult.failed("文件索引平台出错！");
            }
            File target = new File(fullPath, file.getId() + file.getType());
            File ex_target = new File(fullPath, file.getExScriptId() + file.getType());
            if (target.exists()) {
                if(!target.delete()){
                    return CommonResult.failed("删除失败！");
                }
                if(ex_target.exists()){
                    if(!ex_target.delete()){
                        return CommonResult.failed("执行副本删除失败！");
                    }
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
    public CommonResult<?> updateSingleFile(String fileId, String script) {
        Optional<GeoActScriptFile> fileOptional = geoActScriptFileDao.findById(fileId);
        if (!fileOptional.isPresent()) {
            return CommonResult.failed("文件不存在！");
        }else {
            GeoActScriptFile file = fileOptional.get();
            String fullPath = buildPath(file.getExecutor(), file.getSceneId(), file.getParentId(), "host");
            if(fullPath == null){
                return CommonResult.failed("文件索引平台出错！");
            }
            File target = new File(fullPath, file.getId() + file.getType());
            if (!target.exists()) {
                return CommonResult.failed("文件不存在！");
            }else {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(target))) {
                    writer.write(script != null ? script : "");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return CommonResult.success(true);
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
        String fullPath = buildPath(geoActFolder.getExecutor(), geoActFolder.getSceneId(), geoActFolder.getParentId(), "host");
        if(fullPath == null){
            return CommonResult.failed("文件索引平台出错！");
        }
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
        String fullPath = buildPath(geoActFolder.getExecutor(), geoActFolder.getSceneId(), geoActFolder.getParentId(), "host");
        if(fullPath == null){
            return CommonResult.failed("文件索引平台出错！");
        }
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
            String fullParentPath = buildPath(file.getExecutor(), file.getSceneId(), file.getParentId(), "host");
            if(fullParentPath == null){
                return CommonResult.failed("文件索引平台出错！");
            }
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

    @Override
    public CommonResult<?> submitGeoActTask(GeoActFileNode geoActFileNode) {
        Optional<GeoActScriptFile> fileOpt = geoActScriptFileDao.findById(geoActFileNode.getId());
        if (fileOpt.isPresent()) {
            GeoActScriptFile geoActScriptFile = fileOpt.get();
            String parentId = geoActScriptFile.getParentId();
            String executor = geoActScriptFile.getExecutor();
            String sceneId = geoActScriptFile.getSceneId();
            String fullParentPath = buildPath(executor, sceneId, parentId, "jupyter");
            if(fullParentPath == null){
                return CommonResult.failed("文件索引平台出错！");
            }
            GeoActTask geoActTask = new GeoActTask();
            geoActTask.setTaskId(geoActFileNode.getId());
            geoActTask.setParentPath(fullParentPath);
            geoActTask.setFileName(geoActFileNode.getName());
            geoActTask.setStartTime(new Date(System.currentTimeMillis()));
            geoActTask.setStatus("started");
            geoActTaskDao.save(geoActTask);
            return CommonResult.success(new StringPackage(geoActTask.getTaskId()));
        }else {
            return CommonResult.failed("文件不存在!");
        }
    }

    @Override
    public CommonResult<?> encapsulation(GeoActModelDTO geoActModelDTO) {
        GeoActModel geoActModel = new GeoActModel(geoActModelDTO);
        if(geoActModel.isInKnowledgeHubOrNot()){
            JSONObject des = geoActAiService.queryDifyDescription(geoActModel.getScript());
            DifyKnowledgePiece knowledgePiece = new DifyKnowledgePiece(geoActModel);
            String description = knowledgePiece.getDescription();
            description += des.getString("description");
            knowledgePiece.setDescription(description);
            Scenario scenario =  des.getObject("scenario", Scenario.class);
            knowledgePiece.setScenario(scenario);
            if(!geoActAiService.uploadToKnowledgeBase(knowledgePiece)){
                return CommonResult.failed("上传模型知识库失败");
            };
            geoActModel.setDescription(description);
        }
        Optional<GeoActScriptFile> fileOptional = geoActScriptFileDao.findById(geoActModel.getFileId());
        if (fileOptional.isPresent()) {
            GeoActScriptFile geoActScriptFile = fileOptional.get();
            String parentId = geoActScriptFile.getParentId();
            String executor = geoActScriptFile.getExecutor();
            String sceneId = geoActScriptFile.getSceneId();
            String parentPath = buildPath(executor, sceneId, parentId, "host");

            String fullPath = parentPath + File.separator + geoActScriptFile.getId() + geoActScriptFile.getType();

            File script = new File(fullPath);

            if (script.exists()) {
                String modelHub = codeOutPutHub + File.separator + "GeoActModel";
                File modelHubDir = new File(modelHub);
                if (!modelHubDir.exists()) {
                    modelHubDir.mkdirs();
                }
                String modelScriptName = geoActModelDTO.getAuthor()+ "-" + System.currentTimeMillis() + "-" +script.getName();
                geoActModel.setModelScriptName(modelScriptName);
                try {
                    // 拷贝原始文件
                    Files.copy(script.toPath(),
                            Paths.get(modelHub, modelScriptName),
                            StandardCopyOption.REPLACE_EXISTING);

                } catch (IOException e) {
                    return CommonResult.failed("模型封装失败: " + e.getMessage());
                }
            }else {
                return CommonResult.failed("模型文件已损坏！（模型源文件可能丢失）");
            }
        }
        geoActModelDao.save(geoActModel);
        return CommonResult.success(geoActModel.getId(),"模型封装成功！");
    }

    @Override
    public CommonResult<?> getModelScript(String id) {
        Optional<GeoActModel> geoActModel = geoActModelDao.findById(id);
        if (geoActModel.isPresent()) {
            GeoActModel model = geoActModel.get();
            String fileName = model.getModelScriptName();

                // 拼接文件路径
                String modelPath = codeOutPutHub
                        + File.separator + "GeoActModel"
                        + File.separator + fileName;

                File scriptFile = new File(modelPath);
                if (!scriptFile.exists()) {
                    return CommonResult.failed("脚本文件不存在: " + modelPath);
                }

                try {
                    // 读取文本内容（UTF-8 编码）
                    String content = new String(Files.readAllBytes(scriptFile.toPath()), StandardCharsets.UTF_8);
                    return CommonResult.success(content, "获取脚本成功！");
                } catch (IOException e) {
                    return CommonResult.failed("读取脚本失败: " + e.getMessage());
                }
        }
        return CommonResult.failed("未找到对应的模型或脚本文件");
    }


    @Override
    public CommonResult<?> deleteModelExScript(String fileName) {
        String dirPath = codeOutPutHub
                + File.separator + "GeoActModel";

        File targetFile = new File(dirPath, fileName);
        if (!targetFile.exists()) {
            return CommonResult.failed("文件不存在: " + targetFile.getAbsolutePath());
        }

        try {
            if (targetFile.delete()) {
                return CommonResult.success("文件删除成功: " + fileName);
            } else {
                return CommonResult.failed("文件删除失败: " + fileName);
            }
        } catch (SecurityException e) {
            return CommonResult.failed("文件删除失败（权限问题）: " + e.getMessage());
        }
    }

    @Override
    public CommonResult<?> deleteModel(String id) {
        Optional<GeoActModel> model = geoActModelDao.findById(id);
        if (model.isPresent()) {
            String fileName = model.get().getModelScriptName();
            String dirPath = codeOutPutHub + File.separator + "GeoActModel";
            File targetFile = new File(dirPath, fileName);
            if (targetFile.exists()) {
                targetFile.delete();
            }
            geoActModelDao.delete(model.get());
            return CommonResult.success("模型已删除！");
        }else{
            return CommonResult.failed("模型不存在！");
        }
    }

    @Override
    public CommonResult<?> preAstroid(String script){

        String url = "http://" + pythonDocker + "/analyze_code";

        // 请求体
        Map<String, String> request = new HashMap<>();
        request.put("code", script);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

        // 调用 FastAPI，直接用 Map 接收
        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Map.class
        );

        return CommonResult.success(response.getBody(), "解析成功");
    }

    @Override
    public CommonResult<?> getShxAndDbf(String id, String catalogId) {
        Optional<DscCatalog> catalogOpt = dscCatalogDAO.findById(catalogId);
        if (!catalogOpt.isPresent()) {
            return CommonResult.failed("未找到对应的目录");
        }

        Optional<DscFileInfo> dscFileInfo = dscFileDAO.findById(id);
        if (!dscFileInfo.isPresent()) {
            return CommonResult.failed("未找到对应的shp文件");
        }

        DscFileInfo shpInfo = dscFileInfo.get();
        String fileName = shpInfo.getFileName();
        if (!fileName.toLowerCase().endsWith(".shp")) {
            return CommonResult.failed("指定的文件不是shp文件");
        }

        // 去掉后缀，得到基名
        String baseName = fileName.substring(0, fileName.lastIndexOf("."));

        // 遍历目录下文件
        DscCatalog dscCatalog = catalogOpt.get();
        List<CatalogChildrenDTO> children = dscCatalog.getChildren();

        String shxName = baseName + ".shx";
        String dbfName = baseName + ".dbf";
        String prjName = baseName + ".prj";

        String shxId = null;
        String dbfId = null;
        String prjId = null;

        for (CatalogChildrenDTO child : children) {
            String name = child.getName();
            if (name.equalsIgnoreCase(shxName)) {
                Optional<DscFileInfo> shxOpt = dscFileDAO.findById(child.getId());
                if (shxOpt.isPresent()) {
                    shxId = shxOpt.get().getId();
                }
            } else if (name.equalsIgnoreCase(dbfName)) {
                Optional<DscFileInfo> dbfOpt = dscFileDAO.findById(child.getId());
                if (dbfOpt.isPresent()) {
                    dbfId = dbfOpt.get().getId();
                }
            } else if (name.equalsIgnoreCase(prjName)) {
                Optional<DscFileInfo> prjOpt = dscFileDAO.findById(child.getId());
                if (prjOpt.isPresent()) {
                    prjId = prjOpt.get().getId();
                }
            }
        }

        if (shxId == null || dbfId == null) {
            return CommonResult.failed("发布失败，组成Shapefile的.shp、.shx、.dbf必要文件不完整！");
        }

        Map<String, String> result = new HashMap<>();
        result.put("shxId", shxId);
        result.put("dbfId", dbfId);
        if (prjId != null) {
            result.put("prjId", prjId);
        }

        return CommonResult.success(result);
    }

    @Override
    public CommonResult<?> parseCode(GeoActParseParamsDTO geoActParseParamsDTO) {
        String id = geoActParseParamsDTO.getId();
        String fullPath;
        String parentPath;
        String fileName;
        GeoActScriptFile scriptFile;
        //判断编译的是模型代码还是普通脚本
        if(geoActParseParamsDTO.getOption().equals("model")){
            Optional<GeoActModel> modelOpt = geoActModelDao.findById(id);
            if(!modelOpt.isPresent()){
                return CommonResult.failed("源文件失效或不存在！");
            }
            GeoActModel geoActModel = modelOpt.get();
            parentPath = codeOutPutHub + File.separator + "GeoActModel";
            fullPath = parentPath + File.separator + modelOpt.get().getModelScriptName();
            fileName = geoActModel.getFileName();
        }else if(geoActParseParamsDTO.getOption().equals("script")){
            Optional<GeoActScriptFile> fileOpt = geoActScriptFileDao.findById(id);
            if (!fileOpt.isPresent()) {
                return CommonResult.failed("源文件失效或不存在！");
            }
            scriptFile = fileOpt.get();
            String parentId = scriptFile.getParentId();
            String executor = scriptFile.getExecutor();
            String sceneId = scriptFile.getSceneId();
            parentPath = buildPath(executor, sceneId, parentId, "host");

            fullPath = parentPath + File.separator + scriptFile.getId() + scriptFile.getType();
            fileName = scriptFile.getFileName() + scriptFile.getType();
        }else {
            return CommonResult.failed("未知的预编译类型");
        }

        try {
            // 读取fullPath文件的内容并将内容赋给codeToRun
            File scriptFileObj = new File(fullPath);
            String codeToRun = FileUtils.readFileToString(scriptFileObj, "UTF-8");
            String finalName = preCompilation(codeToRun, geoActParseParamsDTO, fileName, parentPath);
            return (finalName != null && !finalName.isEmpty())
                    ? CommonResult.success(finalName, "代码预编译成功！")
                    : CommonResult.failed("预编译失败");
        } catch (IOException e) {
            log.error("文件读写异常: {}", e.getMessage(), e);
            return CommonResult.failed("代码解析失败: " + e.getMessage());
        }
    }

    public String preCompilation(String code, GeoActParseParamsDTO geoActParseParamsDTO, String fileName, String parentPath) throws IOException {
        String codeToRun = handleComments(code);
        String newFilePath;
        String newFileName;
        // 在 config = { 后注入 executor 和 sceneCatalogId
        codeToRun = codeToRun.replaceFirst(
                "config\\s*=\\s*\\{",
                String.format("config = {\n  \"executor\": \"%s\",\n  \"sceneCatalog\": \"%s\",",
                        geoActParseParamsDTO.getExecutor(), geoActParseParamsDTO.getSceneCatalogId())
        );

        // 替换 4 种 ModelService 调用
        codeToRun = replaceParams(codeToRun, "runModel", geoActParseParamsDTO);
        codeToRun = replaceParams(codeToRun, "initInput", geoActParseParamsDTO);
        codeToRun = replaceParams(codeToRun, "setOutPut", geoActParseParamsDTO);
        codeToRun = replaceParams(codeToRun, "registerIO", geoActParseParamsDTO);

        // 将预编译过的codeToRun保存在原始文件同级目录下
        // 普通脚本文件名为fileName
        // 模型执行文件为executor-currentTime/fileName
        if(geoActParseParamsDTO.getOption().equals("model")){
            newFileName = geoActParseParamsDTO.getExecutor() + "-" + System.currentTimeMillis() + File.separator + fileName;
            newFilePath = parentPath + File.separator + newFileName;
        }else if(geoActParseParamsDTO.getOption().equals("script")){
            newFileName = fileName;
            newFilePath = parentPath + File.separator + newFileName;
        }else return null;
        File newFile = new File(newFilePath);
        FileUtils.writeStringToFile(newFile, codeToRun, "UTF-8");
        return newFileName;
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

    private static String replaceParams(String codeToRun, String method, GeoActParseParamsDTO geoActParseParamsDTO) {
        String accessToken = geoActParseParamsDTO.getAccessToken();
        String baseIP = geoActParseParamsDTO.getBaseIp();
        String executor = geoActParseParamsDTO.getExecutor();
        String sceneCatalogId = geoActParseParamsDTO.getSceneCatalogId();

        String insertParams;
        if ("setOutPut".equals(method)) {
            insertParams = String.format(", \"%s\", \"http://%s\", \"%s\", \"%s\"",
                    accessToken, baseIP, executor, sceneCatalogId);
        } else {
            insertParams = String.format(", \"%s\", \"%s\", \"http://%s\"",
                    accessToken, sceneCatalogId, baseIP);
        }

        Pattern pattern = Pattern.compile("((?:ModelService\\.)?" + method + ")\\(");
        Matcher matcher = pattern.matcher(codeToRun);
        StringBuilder result = new StringBuilder();

        int lastIndex = 0;
        while (matcher.find()) {
            int startIndex = matcher.end();
            int bracketCount = 1;
            int i = startIndex;
            while (i < codeToRun.length() && bracketCount > 0) {
                char c = codeToRun.charAt(i);
                if (c == '(') bracketCount++;
                else if (c == ')') bracketCount--;
                i++;
            }

            String insideParams = codeToRun.substring(startIndex, i - 1).trim();
            String originalCall = matcher.group(1);
            String newCall;

            if ("registerIO".equals(method) && insideParams.contains("], [")) {
                int firstArrayEnd = findMatchingBracket(insideParams, 0);
                if (firstArrayEnd == -1) {
                    newCall = originalCall + "(" + insideParams + insertParams + ");";
                } else {
                    String firstArray = insideParams.substring(0, firstArrayEnd + 1).trim();
                    int secondArrayStart = insideParams.indexOf('[', firstArrayEnd + 1);
                    if (secondArrayStart == -1) {
                        newCall = originalCall + "(" + insideParams + insertParams + ");";
                    } else {
                        int secondArrayEnd = findMatchingBracket(insideParams, secondArrayStart);
                        String secondArray = insideParams.substring(secondArrayStart,
                                secondArrayEnd == -1 ? insideParams.length() : secondArrayEnd + 1).trim();
                        String remaining = secondArrayEnd == -1 ? "" :
                                insideParams.substring(secondArrayEnd + 1).trim();

                        newCall = String.format("%s(%s, %s%s%s);",
                                originalCall,
                                firstArray, secondArray, insertParams,
                                remaining.isEmpty() ? "" : ", " + remaining);
                    }
                }
            } else {
                newCall = String.format("%s(%s%s)", originalCall, insideParams, insertParams);
            }

            result.append(codeToRun, lastIndex, matcher.start()).append(newCall);
            lastIndex = i;
        }

        result.append(codeToRun.substring(lastIndex));
        return result.toString();
    }

    private static int findMatchingBracket(String str, int start) {
        int count = 0;
        for (int i = start; i < str.length(); i++) {
            if (str.charAt(i) == '[') count++;
            else if (str.charAt(i) == ']') {
                count--;
                if (count == 0) return i;
            }
        }
        return -1;
    }

    private static String handleComments(String code) {
        // 去除每行的 # 注释
        return code.replaceAll("\\s*#.*$", "");
    }


}
