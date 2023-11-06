package nnu.wyz.systemMS.service;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.PageableDTO;
import nnu.wyz.systemMS.model.entity.Message;
import nnu.wyz.systemMS.model.entity.PageInfo;

import java.util.ArrayList;
import java.util.List;

public interface DscMessageService {

    CommonResult<List<Message>> getNotReadMsg(String userId);

    CommonResult<PageInfo<Message>> getAllMsg(PageableDTO pageableDTO);

    CommonResult<String> readAllMsgs(List<String> msgIds);

    CommonResult<String> readAMsg(String msgId);

    CommonResult<String> deleteMsgs(ArrayList<String> msgIds);
}
