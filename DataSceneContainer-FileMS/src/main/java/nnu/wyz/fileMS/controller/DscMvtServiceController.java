package nnu.wyz.fileMS.controller;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.fileMS.model.entity.DscMvtServiceInfo;
import nnu.wyz.fileMS.model.entity.DscUserMvtS;
import nnu.wyz.fileMS.service.DscMvtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/9/7 21:28
 */
@RestController
@RequestMapping("/dsc-mvtS")
@Api(value = "DscShapefileController", tags = "矢量文件接口")
public class DscMvtServiceController {

    @Autowired
    private DscMvtService dscMvtService;

    @ApiOperation(value = "获取指定坐标的矢量瓦片")
    @GetMapping(value = "/mvt/{tableName}/{zoom}/{x}/{y}.pbf")
    public void getMvt(@PathVariable("tableName") String tableName,
                       @PathVariable("zoom") int zoom,
                       @PathVariable("x") int x,
                       @PathVariable("y") int y,
                       HttpServletResponse response) throws IOException {

        dscMvtService.getMvt(zoom, x, y, tableName, response);
    }

    @GetMapping(value = "/getList/{userId}")
    public CommonResult<List<DscMvtServiceInfo>> getServicesList(@PathVariable("userId") String userId) {
        return dscMvtService.getMvtServiceList(userId);
    }

    @DeleteMapping(value = "/delete/{userId}/{mvtId}")
    public CommonResult<String> deleteMvtService(@PathVariable("userId") String userId, @PathVariable("mvtId") String mvtSId) {
        return dscMvtService.deleteMvtService(userId, mvtSId);
    }

    @GetMapping(value = "/getMvtByFileId/{fileId}")
    public CommonResult<List<DscMvtServiceInfo>> getMvtByFileId(@PathVariable("fileId") String fileId) {
        return dscMvtService.getMvtByFileId(fileId);
    }
}
