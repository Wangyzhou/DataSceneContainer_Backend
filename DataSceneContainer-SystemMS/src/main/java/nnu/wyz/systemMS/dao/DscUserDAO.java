package nnu.wyz.systemMS.dao;

import nnu.wyz.systemMS.model.entity.DscUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DscUserDAO extends MongoRepository<DscUser, String> {
    DscUser findDscUserByEmail(String email);

    DscUser findDscUserByActiveCode(String activeCode);

    DscUser findDscUserById(String Id);

    List<DscUser> findAllByEnabled(int enabled);

}
