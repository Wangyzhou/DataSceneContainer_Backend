package nnu.wyz.systemMS.service.iml;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.config.MinioConfig;
import nnu.wyz.systemMS.dao.DscCode.DscCodeModelDAO;
import nnu.wyz.systemMS.dao.DscCode.GeoActModelDAO;
import nnu.wyz.systemMS.dao.DscFileDAO;
import nnu.wyz.systemMS.dao.DscGeoAnalysisDAO;
import nnu.wyz.systemMS.model.DscGeoAnalysis.DscGeoAnalysisTool;
import nnu.wyz.systemMS.model.dto.ConvertSgrd2GeoTIFFDTO;
import nnu.wyz.systemMS.model.dto.TaskInfoDTO;
import nnu.wyz.systemMS.model.dto.UploadFileDTO;
import nnu.wyz.systemMS.model.entity.DscFileInfo;
import nnu.wyz.systemMS.model.entity.codeModel.DscCodeModel;
import nnu.wyz.systemMS.model.entity.codeModel.GeoActModel;
import nnu.wyz.systemMS.model.param.InitTaskParam;
import nnu.wyz.systemMS.service.DscCatalogService;
import nnu.wyz.systemMS.service.DscFileService;
import nnu.wyz.systemMS.service.DscGeoAnalysisToolService;
import nnu.wyz.systemMS.service.SysUploadTaskService;
import nnu.wyz.systemMS.utils.SagaOtherToolUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: yzwang
 * @time: 2024/1/11 16:49
 */
@Service
@Slf4j
public class DscGeoAnalysisToolServiceIml implements DscGeoAnalysisToolService {

    @Autowired
    private DscGeoAnalysisDAO dscGeoAnalysisDAO;

    @Autowired
    private DscFileDAO dscFileDAO;

    @Autowired
    private MinioConfig minioConfig;

    @Autowired
    private SysUploadTaskService sysUploadTaskService;

    @Autowired
    private DscFileService dscFileService;

    @Autowired
    private SagaOtherToolUtil sagaOtherToolUtil;

    @Autowired
    private DscCodeModelDAO dscCodeModelDAO;

    @Autowired
    private GeoActModelDAO geoActModelDAO;

    @Value("${fileSavePath}")
    private String root;

    @Override
    public CommonResult<?> getGeoAnalysisTool(String toolId) {
        Optional<?> byId = dscGeoAnalysisDAO.findById(toolId);
        if(!byId.isPresent()){
            byId = geoActModelDAO.findById(toolId);
        }
        return byId.map(dscGeoAnalysisTool -> CommonResult.success(dscGeoAnalysisTool, "获取工具成功")).orElseGet(() -> CommonResult.failed("未找到该工具"));
    }

    @Override
    public CommonResult<String> convertSgrd2Geotiff(ConvertSgrd2GeoTIFFDTO convertSgrd2GeoTIFFDTO) {
        Optional<DscFileInfo> byId = dscFileDAO.findById(convertSgrd2GeoTIFFDTO.getSgrdFile());
        if (!byId.isPresent()) {
            return CommonResult.failed("未找到该文件");
        }
        DscFileInfo dscFileInfo = byId.get();
        String sgrdFilePath = root + dscFileInfo.getBucketName() + "/" + dscFileInfo.getObjectKey();
        String geoTiffId = IdUtil.randomUUID();
        // 物理存储到sgrd文件同目录下
        String catalogPath = dscFileInfo.getObjectKey().substring(0, dscFileInfo.getObjectKey().lastIndexOf("/"));
        String geoTiffFilePath = root + dscFileInfo.getBucketName() + "/" + catalogPath + "/" + geoTiffId + ".tif";
        boolean isConvert = sagaOtherToolUtil.ConvertSgrd2GeoTIFF(sgrdFilePath, geoTiffFilePath);
        if (!isConvert) {
            return CommonResult.failed("转换失败");
        }
        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(geoTiffFilePath);
            File file = new File(geoTiffFilePath);
            String md5 = DigestUtils.md5DigestAsHex(fileInputStream);
            String suffix = file.getName().substring(file.getName().lastIndexOf(".") + 1);
            String fileName = dscFileInfo.getFileName().substring(0, dscFileInfo.getFileName().lastIndexOf(".")) + ".tif";
            String fileId = IdUtil.objectId();
            DscFileInfo geoTiffInfo = new DscFileInfo(fileId, md5, fileName, suffix, false, convertSgrd2GeoTIFFDTO.getUserId(), DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"), DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"), file.length(), 0L, 0L, 0L, 0L, minioConfig.getGaOutputBucket(), catalogPath + "/" + file.getName(), 32);
            dscFileDAO.insert(geoTiffInfo);
            InitTaskParam initTaskParam = new InitTaskParam();
            initTaskParam.setIdentifier(md5);
            initTaskParam.setFileName(file.getName());
            initTaskParam.setFileId(fileId);
            initTaskParam.setUserId(convertSgrd2GeoTIFFDTO.getUserId());
            initTaskParam.setTotalSize(file.length());
            initTaskParam.setChunkSize(file.length());
            initTaskParam.setObjectName(file.getName().substring(0, file.getName().lastIndexOf(".")));
            TaskInfoDTO taskInfoDTO = sysUploadTaskService.initTask(initTaskParam);
            UploadFileDTO uploadFileDTO = new UploadFileDTO(convertSgrd2GeoTIFFDTO.getUserId(), taskInfoDTO.getTaskRecord().getId(), convertSgrd2GeoTIFFDTO.getOutputDir());
            dscFileService.create(uploadFileDTO, false,false);
            return CommonResult.success(fileId,"转换成功");
        } catch (IOException e) {
            log.error(e.getMessage());
            return CommonResult.failed("转换失败");
        }
    }

    @Override
    public CommonResult<List<JSONObject>> getGeoAnalysisToolList() {
        HashMap<String, List<DscGeoAnalysisTool>> map = new HashMap<>();
        HashMap<String, List<DscCodeModel>> mapCode = new HashMap<>();
        HashMap<String, List<GeoActModel>> mapGeoAct = new HashMap<>();
        dscGeoAnalysisDAO.findAll().forEach(dscGeoAnalysisTool -> {
            List<DscGeoAnalysisTool> orDefault = map.getOrDefault(dscGeoAnalysisTool.getCategory(), new ArrayList<>());
            orDefault.add(dscGeoAnalysisTool);
            map.put(dscGeoAnalysisTool.getCategory(), orDefault);
        });
        dscCodeModelDAO.findAll().forEach(dscCodeModel -> {
            List<DscCodeModel> orDefault = mapCode.getOrDefault(dscCodeModel.getCategory(), new ArrayList<>());
            orDefault.add(dscCodeModel);
            mapCode.put(dscCodeModel.getCategory(), orDefault);
        });
        geoActModelDAO.findAll().forEach(geoActModel -> {
            List<GeoActModel> orDefault = mapGeoAct.getOrDefault(geoActModel.getCategory(), new ArrayList<>());
            orDefault.add(geoActModel);
            mapGeoAct.put(geoActModel.getCategory(), orDefault);
        });
        ArrayList<JSONObject> treeData = new ArrayList<>();
        for (Map.Entry<String, List<DscGeoAnalysisTool>> entry : map.entrySet()) {
            JSONObject treeNode = new JSONObject();
            treeNode.put("id", IdUtil.randomUUID());
            treeNode.put("label", entry.getKey());
            treeNode.put("isLeaf", false);
            ArrayList<JSONObject> children = new ArrayList<>();
            for (DscGeoAnalysisTool dscGeoAnalysisTool : entry.getValue()) {
                JSONObject child = new JSONObject();
                child.put("id", dscGeoAnalysisTool.getId());
                child.put("label", dscGeoAnalysisTool.getName());
                child.put("isLeaf", true);
                child.put("isEnabled", dscGeoAnalysisTool.getIsEnabled());
                child.put("category", dscGeoAnalysisTool.getInvokeCmd().get(1));
                children.add(child);
            }
            treeNode.put("children", children);
            treeData.add(treeNode);
        }
        for (Map.Entry<String, List<DscCodeModel>> entry : mapCode.entrySet()) {
            JSONObject treeNode = new JSONObject();
            treeNode.put("id", IdUtil.randomUUID());
            treeNode.put("label", entry.getKey());
            treeNode.put("isLeaf", false);
            ArrayList<JSONObject> children = new ArrayList<>();
            for (DscCodeModel dscCodeModel : entry.getValue()) {
                JSONObject child = new JSONObject();
                child.put("id", dscCodeModel.getId());
                child.put("label", dscCodeModel.getName());
                child.put("isLeaf", true);
                child.put("isEnabled", dscCodeModel.isEnabled());
                child.put("category", dscCodeModel.getSubCategory());
                children.add(child);
            }
            treeNode.put("children", children);
            treeData.add(treeNode);
        }
        for (Map.Entry<String, List<GeoActModel>> entry : mapGeoAct.entrySet()) {
            JSONObject treeNode = new JSONObject();
            treeNode.put("id", IdUtil.randomUUID());
            treeNode.put("label", entry.getKey());
            treeNode.put("isLeaf", false);
            ArrayList<JSONObject> children = new ArrayList<>();
            for (GeoActModel geoActModel : entry.getValue()) {
                JSONObject child = new JSONObject();
                child.put("id", geoActModel.getId());
                child.put("label", geoActModel.getName());
                child.put("isLeaf", true);
                child.put("isEnabled", geoActModel.getIsEnabled());
                child.put("category", geoActModel.getSubCategory());
                children.add(child);
            }
            treeNode.put("children", children);
            treeData.add(treeNode);
        }
        return CommonResult.success(treeData, "获取工具列表成功");
    }

    @Override
    public CommonResult<?> getToolCategory() {
        // 一级目录 -> 二级目录 -> 三级目录列表
        Map<String, Map<String, List<String>>> categoryMap = new LinkedHashMap<>();
        Map<String, Boolean> hasDirectItemMap = new HashMap<>(); // 记录一级目录是否直接有条目

        geoActModelDAO.findAll().forEach(geoActModel -> {
            String category = geoActModel.getCategory();
            String subCategory = geoActModel.getSubCategory();

            if (category == null || category.trim().isEmpty()) {
                return; // 跳过空category
            }

            // 初始化一级目录
            Map<String, List<String>> level2Map = categoryMap.computeIfAbsent(category, k -> new LinkedHashMap<>());

            if (subCategory == null || subCategory.trim().isEmpty()) {
                // 一级目录直接存在条目
                hasDirectItemMap.put(category, true);
                return;
            }

            String[] parts = subCategory.split("_");
            if (parts.length == 1) {
                // 二级目录，三级为空
                String level2 = parts[0].trim();
                level2Map.computeIfAbsent(level2, k -> new ArrayList<>());
            } else if (parts.length == 2) {
                // 三级目录
                String level2 = parts[0].trim();
                String level3 = parts[1].trim();
                List<String> level3List = level2Map.computeIfAbsent(level2, k -> new ArrayList<>());
                if (!level3List.contains(level3)) {
                    level3List.add(level3);
                }
            }
        });

        // 构造树形结构
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Map<String, List<String>>> catEntry : categoryMap.entrySet()) {
            String category = catEntry.getKey();
            Map<String, List<String>> level2Map = catEntry.getValue();

            List<Map<String, Object>> level2List = new ArrayList<>();
            for (Map.Entry<String, List<String>> level2Entry : level2Map.entrySet()) {
                String level2 = level2Entry.getKey();
                List<String> level3List = level2Entry.getValue().stream().distinct().collect(Collectors.toList());

                List<Map<String, Object>> level3Nodes = new ArrayList<>();
                for (String level3 : level3List) {
                    if (!level3.isEmpty()) {
                        Map<String, Object> level3Node = new LinkedHashMap<>();
                        level3Node.put("label", level3);
                        level3Node.put("value", level3);
                        level3Nodes.add(level3Node);
                    }
                }

                Map<String, Object> level2Node = new LinkedHashMap<>();
                level2Node.put("label", level2);
                level2Node.put("value", level2);
                level2Node.put("children", level3Nodes.isEmpty() ? null : level3Nodes);
                level2List.add(level2Node);
            }

            Map<String, Object> categoryNode = new LinkedHashMap<>();
            categoryNode.put("label", category);
            categoryNode.put("value", category);

            // 如果一级目录下既没有二级目录，也有直接条目，则 children = null
            if (level2List.isEmpty() && hasDirectItemMap.getOrDefault(category, false)) {
                categoryNode.put("children", null);
            } else {
                categoryNode.put("children", level2List.isEmpty() ? null : level2List);
            }

            result.add(categoryNode);
        }

        return CommonResult.success(result, "获取目录成功");
    }


}
