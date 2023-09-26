package nnu.wyz.systemMS.dao;

import nnu.wyz.systemMS.model.entity.Message;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DscMessageDAO extends MongoRepository<Message, String> {

    List<Message> findMessagesByTo(String userId);

    List<Message> findMessagesByToAndIsRead(String userId, Boolean isReader);
}
