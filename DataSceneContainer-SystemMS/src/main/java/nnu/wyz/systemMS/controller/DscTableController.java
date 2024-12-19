package nnu.wyz.systemMS.controller;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.entity.DscTable;
import nnu.wyz.systemMS.service.DscTableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("dsc-table")
public class DscTableController {
    private  final DscTableService dscTableService;
    @Autowired
    public DscTableController(DscTableService dscTableService) {
        this.dscTableService = dscTableService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResult<DscTable>> getDscTable(@PathVariable String id) {
        CommonResult<DscTable>  dscTable = dscTableService.getDscTableById(id);
        if(dscTable == null) {
            return ResponseEntity.notFound().build();
        }
        System.out.println("dscTable = " + dscTable);
        return ResponseEntity.ok(dscTable);
    }
}
