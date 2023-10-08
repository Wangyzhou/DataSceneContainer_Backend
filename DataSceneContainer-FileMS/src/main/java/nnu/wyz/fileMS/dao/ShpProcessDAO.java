package nnu.wyz.fileMS.dao;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import nnu.wyz.domain.CommonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.text.MessageFormat;
import java.util.*;

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
            String sql = "DROP TABLE " + ptName.toLowerCase();
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
            String sql = "SELECT GeometryType(geom) FROM " + tableName.toLowerCase() + " limit 1";
            Map<String, Object> m = jdbcTemplate.queryForMap(sql);
            return m.get("geometrytype").toString();
        } catch (Exception e) {
            log.error(e.getMessage());
            return "none";
        }
    }

    public List<Double> getShpBox2D(String shpTableName) {

        String sql = MessageFormat.format("SELECT ST_Extent(geom) FROM public.\"{0}\";", shpTableName.toLowerCase());

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

    public List<String> getFields(String tableName) {
        try {
            String sql = "SELECT column_name FROM information_schema.columns WHERE table_name   = '" + tableName.toLowerCase() + "'";
            List<Map<String, Object>> re = jdbcTemplate.queryForList(sql);
            ArrayList<String> fields = new ArrayList<>();
            re.forEach(field -> {
                if (!field.get("column_name").toString().equals("geom")) {
                    fields.add(field.get("column_name").toString());
                }
            });
            return fields;
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public List<Object> getUniqueValues(String ptName, String field, String method) {
        String sql = MessageFormat.format("SELECT DISTINCT ( \"{0}\") FROM {1} ORDER BY \"{2}\" {3}", field, ptName.toLowerCase(), field, method);
        List<Map<String, Object>> re = jdbcTemplate.queryForList(sql);
        ArrayList<Object> uniqueValues = new ArrayList<>();
        re.forEach(uniqueValueMap -> {
            uniqueValues.add(uniqueValueMap.get(field));
        });
        return uniqueValues;
    }

    public Long getFeatureCount(String ptName) {
        String sql = "SELECT COUNT(*) FROM " + ptName.toLowerCase();
        Map<String, Object> re = jdbcTemplate.queryForMap(sql);
        return (Long) re.get("count");
    }

    public List<Map<String, Object>> getShpAttrInfoFromPG(String tableName) {
        try {
            String sql = "SELECT * FROM " + tableName;
            List<Map<String, Object>> re = jdbcTemplate.queryForList(sql);
            List<Map<String, Object>> attrArray = new ArrayList<>();    //去掉geom的新数组
            for (Map<String, Object> stringObjectMap : re) {    //遍历每一个feature
                JSONObject attr = new JSONObject();
                for (Map.Entry<String, Object> entry : stringObjectMap.entrySet()) {
                    if (!entry.getKey().equals("geom")) {
                        attr.put(entry.getKey(), entry.getValue());
                    }
                }
                attrArray.add(attr);
            }
            return attrArray;
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }
}
