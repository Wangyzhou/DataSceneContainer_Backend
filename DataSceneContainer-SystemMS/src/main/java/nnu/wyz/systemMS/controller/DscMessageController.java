package nnu.wyz.systemMS.controller;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.MessagePageDTO;
import nnu.wyz.systemMS.model.entity.Message;
import nnu.wyz.systemMS.model.entity.MsgPageInfo;
import nnu.wyz.systemMS.service.DscMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/9/20 19:32
 */
@RestController
@RequestMapping(value = "/dsc-message")
public class DscMessageController {
    @Autowired
    private DscMessageService dscMessageService;

    @GetMapping("/getNotReadMsg/{email}")
    public CommonResult<List<Message>> getNotReadMsg(@PathVariable("email") String email) {
        return dscMessageService.getNotReadMsg(email);
    }

    @GetMapping("/getAllMsg/{email}/{pageIndex}")
    public CommonResult<MsgPageInfo> getAllMsg(@PathVariable("email") String email,
                                               @PathVariable("pageIndex") Integer pageIndex) {
        MessagePageDTO messagePageDTO = new MessagePageDTO();
        messagePageDTO.setEmail(email);
        messagePageDTO.setPageIndex(pageIndex);
        messagePageDTO.setPageSize(10);
        return dscMessageService.getAllMsg(messagePageDTO);
    }

    @PutMapping("/readAllMsgs")
    public CommonResult<String> readAllMsgs(@RequestBody Map<String, Object> param) {
        ArrayList<String> msgIds =(ArrayList<String>) param.get("msgIds");
        return dscMessageService.readAllMsgs(msgIds);
    }

    @PutMapping(value = "/readAMsg/{msgId}")
    public CommonResult<String> readAMsg(@PathVariable("msgId") String msgId) {
        return dscMessageService.readAMsg(msgId);
    }

    @DeleteMapping(value = "/deleteMsgs")
    public CommonResult<String> deleteMsgs(@RequestBody Map<String, Object> param) {
        ArrayList<String> msgIds = (ArrayList<String>) param.get("msgIds");
        return dscMessageService.deleteMsgs(msgIds);
    }
}
