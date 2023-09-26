package nnu.wyz.systemMS.service.iml;

import lombok.extern.slf4j.Slf4j;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.dao.DscMessageDAO;
import nnu.wyz.systemMS.model.dto.MessagePageDTO;
import nnu.wyz.systemMS.model.entity.Message;
import nnu.wyz.systemMS.model.entity.MsgPageInfo;
import nnu.wyz.systemMS.service.DscMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/9/20 19:36
 */
@Service
@Slf4j
public class DscMessageServiceIml implements DscMessageService {

    @Autowired
    private DscMessageDAO dscMessageDAO;

    @Autowired
    private MongoTemplate mongoTemplate;

    private final static String COLLECTION_NAME = "message";

    @Override
    public CommonResult<List<Message>> getNotReadMsg(String email) {
        List<Message> messagesByTo = dscMessageDAO.findMessagesByToAndIsRead(email, false);
        return CommonResult.success(messagesByTo, "获取未读消息成功！");
    }

    @Override
    public CommonResult<MsgPageInfo> getAllMsg(MessagePageDTO messagePageDTO) {
        String email = messagePageDTO.getEmail();
        Integer pageIndex = messagePageDTO.getPageIndex();
        Integer pageSize = messagePageDTO.getPageSize();
        Query query = new Query(Criteria.where("to").is(email));
        int count = (int) mongoTemplate.count(query, Message.class);
        query.with(Sort.by(Sort.Order.desc("date")));
        query.skip((long) (pageIndex - 1) * pageSize);
        query.limit(pageSize);
        List<Message> allMsg = mongoTemplate.find(query, Message.class, COLLECTION_NAME);
        MsgPageInfo msgPageInfo = new MsgPageInfo();
        msgPageInfo.setMsgs(allMsg);
        msgPageInfo.setCount(count);
        msgPageInfo.setPageNum((count / pageSize) + 1);
        return CommonResult.success(msgPageInfo, "获取消息列表成功！");
    }

    @Override
    public CommonResult<String> readAllMsgs(List<String> msgIds) {
        msgIds.forEach(msgId -> {
            Optional<Message> byId = dscMessageDAO.findById(msgId);
            if(byId.isPresent()) {
                Message message = byId.get();
                message.setIsRead(true);
                dscMessageDAO.save(message);
            }
        });
        return CommonResult.success("全部已读！");
    }

    @Override
    public CommonResult<String> readAMsg(String msgId) {
        Optional<Message> byId = dscMessageDAO.findById(msgId);
        if(!byId.isPresent()) {
            return CommonResult.failed("不存在此条消息！");
        }
        Message message = byId.get();
        message.setIsRead(true);
        dscMessageDAO.save(message);
        return CommonResult.success("已读成功！");
    }

    @Override
    public CommonResult<String> deleteMsgs(ArrayList<String> msgIds) {
        msgIds.forEach(msgId -> dscMessageDAO.deleteById(msgId));
        return CommonResult.success("删除成功！");
    }
}
