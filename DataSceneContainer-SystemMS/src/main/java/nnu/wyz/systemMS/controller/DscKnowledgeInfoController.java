package nnu.wyz.systemMS.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.FileTagDTO;
import nnu.wyz.systemMS.model.dto.UserKnowledgeInfoDTO;
import nnu.wyz.systemMS.service.DscFileTagService;
import nnu.wyz.systemMS.service.DscKnowledgeInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Desription：
 * @Author：mfz
 * @Date：2024/11/20 16:10
 */

@RestController
@RequestMapping(value = "/dsc-knowledge-info")
@Api(value = "DscFileTagController", tags = "用户个人空间知识库信息相关接口")
public class DscKnowledgeInfoController {

    @Autowired
    private DscKnowledgeInfoService dscKnowledgeInfoService;

    @ApiOperation(value = "查询用户知识库和文档id")
    @GetMapping(value = "/{userId}")
    public CommonResult<UserKnowledgeInfoDTO> getKnowledgeInfo(@PathVariable("userId") String userId) {
        return dscKnowledgeInfoService.getKnowledgeInfo(userId);
    }

    @ApiOperation(value = "更新用户知识库和文档id")
    @PostMapping(value = "/{userId}")
    public CommonResult<UserKnowledgeInfoDTO> updateKnowledgeInfo(@PathVariable("userId") String userId, @RequestBody UserKnowledgeInfoDTO userKnowledgeInfoDTO) {
        return dscKnowledgeInfoService.updateKnowledgeInfo(userId, userKnowledgeInfoDTO);
    }

}


