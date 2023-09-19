package nnu.wyz.fileMS.dao;

import nnu.wyz.fileMS.model.entity.DscUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DscUserDAO extends MongoRepository<DscUser, String> {
    DscUser findDscUserByEmail(String email);

    DscUser findDscUserByActiveCode(String activeCode);
}
