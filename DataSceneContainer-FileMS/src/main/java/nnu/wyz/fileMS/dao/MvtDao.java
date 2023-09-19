package nnu.wyz.fileMS.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;


/**
 * @Description
 * @Author wyjq
 * @Date 2022/3/10
 */

@Slf4j
@Repository
public class MvtDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;


    public byte[] getMvtFromDefaultPg(String sql) {
        try{
            byte[] reByte = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> rs.getBytes("st_asmvt"));
            return reByte;
        }catch (Exception e){
            log.error("默认数据库瓦片获取失败"+e.getMessage());
            return null;
        }
    }

}
