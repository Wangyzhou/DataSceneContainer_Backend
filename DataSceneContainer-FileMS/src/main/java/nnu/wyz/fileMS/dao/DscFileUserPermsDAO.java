package nnu.wyz.fileMS.dao;

import nnu.wyz.fileMS.model.entity.DscFileUserPerms;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/8/29 21:24
 */
@Repository
public interface DscFileUserPermsDAO extends MongoRepository<DscFileUserPerms, String> {
    /**
     * 判断用户是否已拥有该文件
     * @param fileId
     * @param userId
     * @return
     */
    DscFileUserPerms findDscFileUserPermsByFileIdAndUserId(String fileId, String userId);

    List<DscFileUserPerms> findAllByFileId(String fileId);
}
