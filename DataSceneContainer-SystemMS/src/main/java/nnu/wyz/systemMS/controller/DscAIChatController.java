package nnu.wyz.systemMS.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.ChatDTO;
import nnu.wyz.systemMS.service.DscAIChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @description:
 * @author: yzwang
 * @time: 2024/4/17 14:51
 */
@RestController
@RequestMapping(value = "/dsc-chat")
public class DscAIChatController {

    @Autowired
    DscAIChatService dscAIChatService;

    @PostMapping(value = "/chat")
    public CommonResult<JSONArray> chat(@RequestBody ChatDTO chatDTO) {
        return dscAIChatService.chat(chatDTO);
    }
}
