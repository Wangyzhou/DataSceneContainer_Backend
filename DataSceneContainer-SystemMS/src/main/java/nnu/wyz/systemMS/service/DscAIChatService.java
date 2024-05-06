package nnu.wyz.systemMS.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.ChatDTO;

public interface DscAIChatService {

    CommonResult<JSONArray> chat(ChatDTO chatDTO);

}
