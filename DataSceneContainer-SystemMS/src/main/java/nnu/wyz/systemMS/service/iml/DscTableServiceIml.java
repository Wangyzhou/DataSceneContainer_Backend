package nnu.wyz.systemMS.service.iml;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.dao.DscFileDAO;
import nnu.wyz.systemMS.model.entity.DscTable;
import nnu.wyz.systemMS.model.entity.DscFileInfo;
import nnu.wyz.systemMS.service.DscTableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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

    @Value("${fileSavePath}")
    private String root;



    // 根据 ID 获取文件路径并读取数据
    @Override
    public CommonResult<DscTable> getDscTableById(String id) {
        Optional<DscFileInfo> byId = dscFileDAO.findById(id);
        System.out.println(byId.isPresent());
        DscFileInfo dscFileInfo = byId.get();
        System.out.println("dscFileInfo="+dscFileInfo);
        String filePath = root + dscFileInfo.getBucketName()+ File.separator+dscFileInfo.getObjectKey();
        System.out.println(filePath);
        File tableFile = new File(filePath);

        if(!tableFile.exists()){
            return CommonResult.failed("未找到相应表格文件！");
        }

        DscTable dscTable = new DscTable();
        List<String> columns = new ArrayList<>();
        List<Map<String, String>> rows = new ArrayList<>();

        //使用OpenCSV读取CSV文件
        try(BufferedReader reader = new BufferedReader(new FileReader(tableFile))){
            //读取列名(表头)
            String headerLine = reader.readLine();
            if(headerLine == null){
                return CommonResult.failed("该表格文件不符合格式要求！");
            }
            String[] header = headerLine.split(",");
            columns.addAll(Arrays.asList(header));  // 将列名添加到 columns 列表中

            //读取每一行数据
            String line;
            while((line = reader.readLine()) != null){
                String[] values = line.split(",");
                if(values.length == columns.size()){
                    Map<String, String > rowData = new HashMap<>();
                    for(int i=0; i<columns.size(); i++){
                        rowData.put(columns.get(i), values[i]);
                    }
                    rows.add(rowData);
                }
            }
            System.out.println("line"+line);

            dscTable.setColumns(columns);
            dscTable.setRows(rows);
        }catch(IOException e){
            return CommonResult.failed(e.getMessage());
        }
        return CommonResult.success(dscTable,"获取表格数据成功");
    }
}
