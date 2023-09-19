package nnu.wyz.userMS.dao;

import nnu.wyz.userMS.entity.DscUser;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DscUserDAO extends MongoRepository<DscUser, String> {
    DscUser findDscUserByEmail(String email);

    DscUser findDscUserByActiveCode(String activeCode);
}
