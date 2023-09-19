package nnu.wyz.dao;

import nnu.wyz.entity.DscUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DscUserDAO extends MongoRepository<DscUser, String> {

    DscUser findDscUserByEmail(String email);
}
