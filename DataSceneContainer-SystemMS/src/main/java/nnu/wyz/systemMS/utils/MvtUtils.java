package nnu.wyz.systemMS.utils;

import com.alibaba.fastjson.JSONObject;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @Description
 * @Author wyjq
 * @Date 2022/3/10
 */
public class MvtUtils {

    public static Boolean tileIsValid(int zoom,int x,int y) {
        Double size =  Math.pow(2,zoom);
        if(x>=size ||y>=size){
            return false;
        }
        if(x<0 ||y<0){
            return false;
        }
        return true;
    }

    public static HashMap<String,Double> tileToEnvelope(int zoom, int x, int y){
        double worldMercMax = 20037508.3427892;
        double worldMercMin = -1 * worldMercMax;
        double worldMercSize = worldMercMax - worldMercMin;

        double worldTileSize=Math.pow(2,zoom);
        double tileMercSize = worldMercSize / worldTileSize;

        HashMap<String, Double> env = new HashMap<String, Double>();
        env.put("xmin", worldMercMin + tileMercSize * x);
        env.put("xmax", worldMercMin + tileMercSize * (x+ 1));
        env.put("ymin", worldMercMax - tileMercSize * (y + 1));
        env.put("ymax", worldMercMax - tileMercSize * y);
        return  env;
    }

    public static String envelopeToBoundsSQL(HashMap<String,Double> env){
        int DENSIFY_FACTOR = 4;
        env.put("segSize",(env.get("xmax")-env.get("xmin"))/DENSIFY_FACTOR);
        String sqlTemp=String.format("ST_Segmentize(ST_MakeEnvelope(%f, %f, %f, %f, 3857),%f)",env.get("xmin"),env.get("ymin"),env.get("xmax"),env.get("ymax"),env.get("segSize"));
        return sqlTemp;
    }

    public static String envelopeToSQL(HashMap<String,Double> env,String tableName){
// lines_pg   gis_osm_transport_free_1
        HashMap<String,String> table=new HashMap<String,String>();
        table.put("table",tableName);
        table.put("srid","4326");
        table.put("geomColumn","geom");
        table.put("attrColumns"," * ");
        table.put("env",envelopeToBoundsSQL(env));

        String mvtsql= MessageFormat.format("WITH" +
                                                    " bounds AS ( SELECT {0} AS geom, {0}::box2d AS b2d)," +
                                                    " mvtgeom AS (" +
                                                     " SELECT ST_AsMVTGeom(ST_Transform(t.{1}, 3857), bounds.b2d) AS geom, {2}" +
                                                    " FROM {3} t, bounds" +
                                                    " WHERE ST_Intersects(t.{1}, ST_Transform(bounds.geom, {4}))" +
                                                    " )" +
                                                    " SELECT ST_AsMVT(mvtgeom.* , ''{3}'' ) FROM mvtgeom" ,
                                                    table.get("env"),table.get("geomColumn"),table.get("attrColumns"),table.get("table"),table.get("srid"));

        return mvtsql;
    }


    public static String getSingleMvtSql(String shpTableName,String attrNames,String envSql,String srid){

        String singleMvtSql= MessageFormat.format("(WITH" +
                        " bounds AS ( SELECT {0} AS geom, {0}::box2d AS b2d)," +
                        " mvtgeom AS (" +
                        " SELECT ST_AsMVTGeom(ST_Transform(t.{1}, 3857), bounds.b2d) AS geom, {2} " +
                        " FROM {3} t, bounds" +
                        " WHERE ST_Intersects(t.{1}, ST_Transform(bounds.geom, {4}))" +
                        " )" +
                        " SELECT ST_AsMVT(mvtgeom.* , ''{3}'' ) FROM mvtgeom)" ,
                envSql,"geom",attrNames,shpTableName,srid);
        return singleMvtSql;
    }

    public static JSONObject getTileRange(int zoom, double xMinMerc,  double yMinMerc, double xMaxMerc, double yMaxMerc){
        double worldMercMax = 20037508.3427892;
        double worldMercMin = -1 * worldMercMax;
        double worldMercSize = worldMercMax - worldMercMin;

        double worldTileSize=Math.pow(2,zoom);
        double tileMercSize = worldMercSize / worldTileSize;

        int xTileMin= (int) Math.floor((xMinMerc-worldMercMin)/tileMercSize);
        int yTileMin= (int) Math.floor((worldMercMax-yMaxMerc)/tileMercSize);
        int xTileMax= (int) Math.floor((xMaxMerc-worldMercMin)/tileMercSize);
        int yTileMax= (int) Math.floor((worldMercMax-yMinMerc)/tileMercSize);

        JSONObject jsonObject=new JSONObject();
        jsonObject.put("xTileMin",xTileMin);
        jsonObject.put("yTileMin",yTileMin);
        jsonObject.put("xTileMax",xTileMax);
        jsonObject.put("yTileMax",yTileMax);

        return jsonObject;
    }

    public static List<Double> wgs84ToWebMerc(double lon,double lat){
        List<Double> lonlat=new ArrayList<>();
        double lonMerc=lon*20037508.34/180;
        double latMerc=20037508.34*Math.log(Math.tan((90+lat)*Math.PI/360))/Math.PI;
        lonlat.add(lonMerc);
        lonlat.add(latMerc);
        return lonlat;
    }


}
