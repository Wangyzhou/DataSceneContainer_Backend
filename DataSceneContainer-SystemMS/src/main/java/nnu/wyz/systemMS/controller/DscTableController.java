package nnu.wyz.systemMS.controller;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.CreateTableFileDTO;
import nnu.wyz.systemMS.model.entity.DscTable;
import nnu.wyz.systemMS.service.DscTableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("dsc-table")
public class DscTableController {
    private  final DscTableService dscTableService;
    @Autowired
    public DscTableController(DscTableService dscTableService) {
        this.dscTableService = dscTableService;
    }

    @GetMapping("/getDscTable/{id}")
    public ResponseEntity<CommonResult<DscTable>> getDscTable(@PathVariable String id) {
        CommonResult<DscTable>  dscTable = dscTableService.getDscTableById(id);
        if(dscTable == null) {
            return ResponseEntity.notFound().build();
        }
//        System.out.println("dscTable = " + dscTable);
        return ResponseEntity.ok(dscTable);
    }

    //此接口用于接受前端传递的JSON并将其保存为表格文件，目前支持的的格式为.txt和.csv以及xlsx
    @PostMapping(value = "/add")
    public CommonResult<String> addDscTable(@RequestBody CreateTableFileDTO tableFileDTO) {
        return dscTableService.addDscTable(tableFileDTO);
    }

    @PutMapping(value="/update/{id}")
    public CommonResult<String> updateDscTable(@PathVariable String id, @RequestBody CreateTableFileDTO tableFileDTO) {
        return dscTableService.updateDscTable(id,tableFileDTO);
    }

}
