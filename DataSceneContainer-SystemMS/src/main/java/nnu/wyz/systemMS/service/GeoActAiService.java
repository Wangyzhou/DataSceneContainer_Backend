package nnu.wyz.systemMS.service;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.DscCode.GeoActModelDTO;
import nnu.wyz.systemMS.model.dto.DscWorkflowModelDTO;
import nnu.wyz.systemMS.model.entity.codeModel.GeoActModel;
import nnu.wyz.systemMS.model.entity.codeModel.ModelRecommend.DifyKnowledgePiece;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

public interface GeoActAiService {
    CommonResult<JSONObject> generateDescription (String script);
    boolean uploadToKnowledgeBase(DifyKnowledgePiece piece);
    JSONObject queryDifyDescription(String script);
}
