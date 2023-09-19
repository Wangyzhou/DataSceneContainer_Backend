package nnu.wyz.fileMS.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/9/11 21:28
 */
@Repository
@Slf4j
public class ShpProcessDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Boolean deletePgTable(String ptName) {
        try {
            String sql = "DROP TABLE " + ptName;
            jdbcTemplate.execute(sql);
            log.info("删除表：" + ptName + "成功！");
            return true;
        } catch (Exception e) {
            log.error("删除表：" + ptName + "失败！");
            log.error(e.getMessage());
            return false;
        }
    }

    public String getShpType(String tableName) {
        try {
            String sql = "SELECT GeometryType(geom) FROM " + tableName + " limit 1";
            Map<String, Object> m = jdbcTemplate.queryForMap(sql);
            return m.get("geometrytype").toString();
        } catch (Exception e) {
            log.error(e.getMessage());
            return "none";
        }
    }
    public List<Double> getShpBox2D(String shpTableName) {

        String sql = MessageFormat.format("SELECT ST_Extent(geom) " +
                "from {0};", shpTableName);

        String boxStr = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> rs.getString("st_extent"));  //BOX(1 0,10 20)
        String coordStr = boxStr.substring(4, boxStr.length() - 1); //1 0,10 20
        double xMin = Double.parseDouble(coordStr.split(",")[0].split(" ")[0]);
        double xMax = Double.parseDouble(coordStr.split(",")[1].split(" ")[0]);
        double yMin = Double.parseDouble(coordStr.split(",")[0].split(" ")[1]);
        double yMax = Double.parseDouble(coordStr.split(",")[1].split(" ")[1]);

        Double[] boundsArray = {xMin, yMin, xMax, yMax};  //"bounds": [-180,-85,180,85],
        List<Double> bounds = new ArrayList<>(Arrays.asList(boundsArray));

        return bounds;
    }

}
