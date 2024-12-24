package nnu.wyz.systemMS.service.iml;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.config.MinioConfig;
import nnu.wyz.systemMS.dao.DscCatalogDAO;
import nnu.wyz.systemMS.dao.DscFileDAO;
import nnu.wyz.systemMS.model.dto.*;
import nnu.wyz.systemMS.model.entity.DscCatalog;
import nnu.wyz.systemMS.model.entity.DscTable;
import nnu.wyz.systemMS.model.entity.DscFileInfo;
import nnu.wyz.systemMS.model.param.InitTaskParam;
import nnu.wyz.systemMS.service.DscFileService;
import nnu.wyz.systemMS.service.DscTableService;
import org.bson.json.JsonReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import springfox.documentation.spring.web.json.Json;

import javax.validation.constraints.Null;
import java.io.*;
import java.util.*;

/**
 * @author tjk
 * @date 2024/12/19
 * @Description
 */

@Service
public class DscTableServiceIml implements DscTableService {

    @Autowired
    private DscFileDAO dscFileDAO;

    @Autowired
    private DscFileService dscFileService;

    @Value("${fileSavePath}")
    private String root;
    @Autowired
    private MinioConfig minioConfig;
    @Autowired
    private SysUploadTaskServiceImpl sysUploadTaskService;
    @Autowired
    private DscFileServiceIml dscFileServiceIml;

    @Autowired
    private DscCatalogDAO dscCatalogDAO;


    // 根据 ID 获取文件路径并读取数据
    @Override
    public CommonResult<DscTable> getDscTableById(String id) {
        Optional<DscFileInfo> byId = dscFileDAO.findById(id);
        System.out.println(byId.isPresent());
        DscFileInfo dscFileInfo = byId.get();
        System.out.println("dscFileInfo=" + dscFileInfo);
        String fileSuffix = dscFileInfo.getFileSuffix();
        System.out.println("fileSuffix=" + fileSuffix);
        String filePath = root + dscFileInfo.getBucketName() + File.separator + dscFileInfo.getObjectKey();
        System.out.println(filePath);
        File tableFile = new File(filePath);

        if (!tableFile.exists()) {
            return CommonResult.failed("未找到相应表格文件！");
        }

        DscTable dscTable = new DscTable();
        List<String> columns = new ArrayList<>();
        List<Map<String, String>> rows = new ArrayList<>();

        // 根据文件后缀判断处理逻辑
        try (BufferedReader reader = new BufferedReader(new FileReader(tableFile))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return CommonResult.failed("该表格文件不符合格式要求！");
            }

            String[] header;
            if (fileSuffix.equalsIgnoreCase("csv")) {
                // CSV文件的处理逻辑
                header = headerLine.split(",");
            } else if (fileSuffix.equalsIgnoreCase("txt")) {
                // TXT文件的处理逻辑，使用TAB分隔
                header = headerLine.split("\t");
            } else {
                return CommonResult.failed("不支持的文件格式！");
            }
            // 将列名添加到 columns 列表中
            columns.addAll(Arrays.asList(header));

            // 读取每一行数据
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values;
                if (fileSuffix.equalsIgnoreCase("csv")) {
                    // CSV格式的数据
                    values = line.split(",");
                } else if (fileSuffix.equalsIgnoreCase("txt")) {
                    // TXT格式的数据，使用TAB分隔
                    values = line.split("\t");
                } else {
                    continue;  // 不支持的格式
                }

                if (values.length == columns.size()) {
                    Map<String, String> rowData = new HashMap<>();
                    for (int i = 0; i < columns.size(); i++) {
                        rowData.put(columns.get(i), values[i]);
                    }
                    rows.add(rowData);
                }
            }

            dscTable.setColumns(columns);
            dscTable.setRows(rows);
        } catch (IOException e) {
            return CommonResult.failed(e.getMessage());
        }

        return CommonResult.success(dscTable, "获取表格数据成功");
    }

    @Override
    public CommonResult<String > addDscTable(CreateTableFileDTO tableFileDTO) {
        if(!isValidDscTable(tableFileDTO.getData())){
            return CommonResult.failed("请输入正确的表格文件格式！");
        }
        //确认文件格式并确认分隔符
        String fileSuffix = ""+tableFileDTO.getTableType();
        String separator = "txt".equals(fileSuffix) ? "\t" : ",";

        //指定表格文件的物理路径并创建文件
        String filePath2UserId = root + minioConfig.getBucketName() + File.separator + tableFileDTO.getUserId();
        String filePhysicalName = IdUtil.randomUUID()+"."+fileSuffix;
        String filePath = filePath2UserId + File.separator + filePhysicalName;
        File tableFile = new File(filePath);

        //使用java的io尝试写入表格文件
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(tableFile))){
            //1.写入表头
            List<String> columns = tableFileDTO.getData().getColumns();
            //使用分隔符写入列名
            writer.write(String.join(separator, columns));
            writer.newLine(); //换行

            //2.写入每一行数据
            List<Map<String, String>> rows = tableFileDTO.getData().getRows();
            for(Map<String, String> rowData : rows){
                List<String> rowValues = new ArrayList<>();
                for(String column : columns){
                    rowValues.add(rowData.getOrDefault(column,""));
                }
                writer.write(String.join(separator, rowValues));
                writer.newLine();
            }
        }catch (IOException e){
            return CommonResult.failed("写入文件失败：" + e.getMessage());
        }

        //写入DscFileInfo所需数据
        try{
            FileInputStream fileInputStream  = new FileInputStream(tableFile);
            String md5 = DigestUtils.md5DigestAsHex(fileInputStream);
            String fileId = IdUtil.objectId();
            System.out.println("fileName=" + tableFile.getName()+"."+tableFileDTO.getTableType());
            String objectKey = tableFileDTO.getUserId()+File.separator+tableFile.getName();
            DscFileInfo dscFileInfo = new DscFileInfo(fileId,md5,tableFileDTO.getTableName(),fileSuffix,false,tableFileDTO.getUserId(),
                    DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"), DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"),
                    tableFile.length(),0L, 0L, 0L, 0L, minioConfig.getBucketName(),objectKey,32);
            dscFileDAO.insert(dscFileInfo);
            InitTaskParam initTaskParam = new InitTaskParam();
            initTaskParam.setIdentifier(md5);
            initTaskParam.setFileName(tableFileDTO.getTableName());
            initTaskParam.setFileId(fileId);
            initTaskParam.setUserId(tableFileDTO.getUserId());
            initTaskParam.setTotalSize(tableFile.length());
            initTaskParam.setChunkSize(tableFile.length());
            initTaskParam.setObjectName(tableFileDTO.getTableName().substring(0,tableFileDTO.getTableName().lastIndexOf(".")));
            TaskInfoDTO taskInfoDTO = sysUploadTaskService.initTask(initTaskParam);
            System.out.println("taskInfoDTO="+taskInfoDTO.getTaskRecord());
            UploadFileDTO uploadFileDTO = new UploadFileDTO(tableFileDTO.getUserId(),taskInfoDTO.getTaskRecord().getId(),tableFileDTO.getCatalog());
            CommonResult<String > result =  dscFileService.create(uploadFileDTO,false,false);
            System.out.println("result是："+result.getData());
            // 根据 result 判断操作是否成功
            if (result.getCode()==200) {
                return CommonResult.success(fileId, "表格文件创建成功");
            } else {
                return CommonResult.failed("文件创建失败: " + result.getMessage());
            }

        }catch (IOException e){
            return CommonResult.failed("写入文件失败"+e.getMessage());
        }


    }

    boolean isValidDscTable(DscTable dscTable) {
        if (dscTable == null || dscTable.getColumns() == null || dscTable.getRows() == null) {
            return false;
        }
        // 将columns转换为Set，以便快速查找
        Set<String> columnSet = new HashSet<>(dscTable.getColumns());

        // 检查rows中的每个Map是否包含columns中的所有字段
        for (Map<String, String> row : dscTable.getRows()) {
            if (row == null) {
                return false;
            }
            // 检查每一行是否包含所有列名
            for (String column : columnSet) {
                if (!row.containsKey(column)) {
                    return false;
                }
            }
        }

        // 如果所有检查都通过，则DscTable格式有效
        return true;
    }

    @Override
    public CommonResult<String > updateDscTable(String fileId, CreateTableFileDTO tableFileDTO) {
        Optional<DscFileInfo> existingFileInfo = dscFileDAO.findById(fileId);
        if (!existingFileInfo.isPresent()) {
            return CommonResult.failed("文件不存在！");
        }
        // 判断是否是改名字
        boolean isRename = !tableFileDTO.getTableName().equals(existingFileInfo.get().getFileName());
        //使用Id获取表格数据
        CommonResult<DscTable> tableData = getDscTableById(existingFileInfo.get().getId());
        // 判断是否有表格数据的变动
        boolean isDataChanged = tableData != null && !tableFileDTO.getData().equals(tableData.getData());


        // 确认文件格式并确认分隔符
        String fileSuffix = tableFileDTO.getTableType();
        String separator = "txt".equals(fileSuffix) ? "\t" : ",";

        // 指定文件路径并准备文件对象
        String filePath2UserId = root + minioConfig.getBucketName() + File.separator + tableFileDTO.getUserId();
        String filePhysicalName = IdUtil.randomUUID()+"."+fileSuffix;
        String filePath = filePath2UserId + File.separator + filePhysicalName;
        File updateTableFile = new File(filePath);

        try {
            if (isDataChanged) {
                // 更新表格数据：重新写入文件内容
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(updateTableFile))) {
                    List<String> columns = tableFileDTO.getData().getColumns();
                    writer.write(String.join(separator, columns));
                    writer.newLine(); // 换行

                    // 写入每一行数据
                    List<Map<String, String>> rows = tableFileDTO.getData().getRows();
                    for (Map<String, String> rowData : rows) {
                        List<String> rowValues = new ArrayList<>();
                        for (String column : columns) {
                            rowValues.add(rowData.getOrDefault(column, ""));
                        }
                        writer.write(String.join(separator, rowValues));
                        writer.newLine();
                    }
                } catch (IOException e) {
                    return CommonResult.failed("写入文件失败：" + e.getMessage());
                }
            }

            // 计算文件 MD5
            FileInputStream fileInputStream = new FileInputStream(updateTableFile);
            String md5 = DigestUtils.md5DigestAsHex(fileInputStream);

            // 更新 DscFileInfo 数据
            DscFileInfo dscFileInfo = new DscFileInfo(
                    fileId, md5, tableFileDTO.getTableName(), fileSuffix, false, tableFileDTO.getUserId(),
                    existingFileInfo.get().getCreatedTime(), DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"),
                    updateTableFile.length(), 0L, 0L, 0L, 0L, minioConfig.getBucketName(),
                    tableFileDTO.getUserId() + File.separator + updateTableFile.getName(), 32);
            // 删除MongoDB里原来的文件信息
            dscFileDAO.delete(existingFileInfo.get());
            //删除实际文件（dsc-file/userId下的文件）
            DeleteFileDTO deleteFileDTO = new DeleteFileDTO();
            deleteFileDTO.setUserId(existingFileInfo.get().getCreatedUser());
            deleteFileDTO.setFileId(existingFileInfo.get().getId());
            deleteFileDTO.setCatalogId(tableFileDTO.getCatalog());
            dscFileService.delete(deleteFileDTO);
            //插入新的MongoDB记录
            dscFileDAO.insert(dscFileInfo);
            //删除DscCatalog里记录的chidren的源文件记录
            Optional<DscCatalog> byId = dscCatalogDAO.findById(tableFileDTO.getCatalog());
            if (!byId.isPresent()) {
                return CommonResult.failed("未找到载体目录!");
            }
            DscCatalog dscCatalog = byId.get();
            List<CatalogChildrenDTO> children = dscCatalog.getChildren();
            Iterator<CatalogChildrenDTO> iterator = children.iterator();
            while (iterator.hasNext()) {
                CatalogChildrenDTO temp = iterator.next();
                if (temp.getId().equals(fileId)) {
                    iterator.remove();
                    break;
                }
            }
            dscCatalog.setTotal(dscCatalog.getTotal() - 1);
            dscCatalog.setUpdatedTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
            dscCatalogDAO.save(dscCatalog);
            // 更新任务信息
            InitTaskParam initTaskParam = new InitTaskParam();
            initTaskParam.setIdentifier(md5);
            initTaskParam.setFileName(tableFileDTO.getTableName());
            initTaskParam.setFileId(fileId);
            initTaskParam.setUserId(tableFileDTO.getUserId());
            initTaskParam.setTotalSize(updateTableFile.length());
            initTaskParam.setChunkSize(updateTableFile.length());
            initTaskParam.setObjectName(tableFileDTO.getTableName().substring(0, tableFileDTO.getTableName().lastIndexOf(".")));
            TaskInfoDTO taskInfoDTO = sysUploadTaskService.initTask(initTaskParam);

            // 创建文件上传任务
            UploadFileDTO uploadFileDTO = new UploadFileDTO(tableFileDTO.getUserId(), taskInfoDTO.getTaskRecord().getId(), tableFileDTO.getCatalog());
            CommonResult<String> result = dscFileService.create(uploadFileDTO, false, false);

            // 根据 result 判断操作是否成功
            if (result.getCode() == 200) {
                return CommonResult.success(fileId, "表格文件更新成功");
            } else {
                return CommonResult.failed("文件更新失败: " + result.getMessage());
            }

        } catch (IOException e) {
            return CommonResult.failed("处理文件失败：" + e.getMessage());
        }
    }
}
