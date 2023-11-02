package nnu.wyz.systemMS.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import nnu.wyz.domain.CommonResult;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/10/17 16:37
 */

@SuppressWarnings("ALL")
public class GeoJSONUtil {

    private static Map<String, Object> root;

    public static CommonResult initUtil(String geojsonUrl) {
        ObjectMapper objectMapper = new ObjectMapper();
        File geojsonFile = new File(geojsonUrl);
        try {
            root = objectMapper.readValue(geojsonFile, Map.class);
        } catch (IOException e) {
            e.printStackTrace();
            return CommonResult.failed("未知的错误，请重试！");
        }
        if (!root.containsKey("features") || !root.containsKey("type") || !"FeatureCollection".equals(root.get("type"))) {
            return CommonResult.failed("不支持的GeoJSON结构!");
        }
        return CommonResult.success("初始化成功!");
    }

    public static String getGeoJSONType() {
        ArrayList<Map<String, Object>> features = (ArrayList<Map<String, Object>>) root.get("features");
        List<String> types = features.stream().map(feature -> (Map<String, Object>) feature.get("geometry")).map(geometry -> (String) geometry.get("type")).distinct().collect(Collectors.toList());
        if (types.size() == 1) {
            return types.get(0);
        } else if (types.size() == 2) {
            if (types.contains("Point") && types.contains("MultiPoint")) {
                return "MultiPoint";
            } else if (types.contains("LineString") && types.contains("MultiLineString")) {
                return "MultiLineString";
            } else if (types.contains("Polygon") && types.contains("MultiPolygon")) {
                return "MultiPolygon";
            } else {
                return "Unknown";
            }
        } else {
            return "Unknown";
        }
    }

    public static List<Double> getGeoJSONBBOX() {
        ArrayList<Map<String, Object>> features = (ArrayList<Map<String, Object>>) root.get("features");
        ArrayList<Double> bbox = new ArrayList<>();
        double minLng = 180.0, maxLng = -180.0, minLat = 90.0, maxLat = -90.0;
        int flag = 0;
        for (Map<String, Object> feature : features) {
            Map<String, Object> geometry = (Map<String, Object>) feature.get("geometry");
            String geoType = (String) geometry.get("type");
            List<Object> coordinate = (List<Object>) geometry.get("coordinates");
            double featureMinLng = 0.0, featureMaxLng = 0.0, featureMinLat = 0.0, featureMaxLat = 0.0;
            ArrayList<Double> featureBbox = new ArrayList<>();
            switch (geoType) {
                case "Point":
                    featureBbox = (ArrayList<Double>) getBBOXFromTypeEqualsPoint(coordinate);
                    break;
                case "MultiPoint":
                    featureBbox = (ArrayList<Double>) getBBOXFromTypeEqualsMultiPoint(coordinate);
                    break;
                case "LineString":
                    featureBbox = (ArrayList<Double>) getBBOXFromTypeEqualsLineString(coordinate);
                    break;
                case "MultiLineString":
                    featureBbox = (ArrayList<Double>) getBBOXFromTypeEqualsMultiLineString(coordinate);
                    break;
                case "Polygon":
                    featureBbox = (ArrayList<Double>) getBBOXFromTypeEqualsPolygon(coordinate);
                    break;
                case "MultiPolygon":
                    featureBbox = (ArrayList<Double>) getBBOXFromTypeEqualsMultiPolygon(coordinate);
                    break;
                default:
                    featureBbox = new ArrayList<>(Arrays.asList(-180.0, -90.0, 180.0, 90.0));
                    break;
            }
            minLng = Math.min(minLng, featureBbox.get(0));
            maxLng = Math.max(maxLng, featureBbox.get(2));
            minLat = Math.min(minLat, featureBbox.get(1));
            maxLat = Math.max(maxLat, featureBbox.get(3));
        }
        bbox.add(minLng);
        bbox.add(minLat);
        bbox.add(maxLng);
        bbox.add(maxLat);
        return bbox;
    }

    private static List<Double> getBBOXFromTypeEqualsPoint(List<Object> coordinates) {
        double minLng = 180.0, maxLng = -180.0, minLat = 90.0, maxLat = -90.0;
        double lng = Double.parseDouble(coordinates.get(0).toString());
        double lat = Double.parseDouble(coordinates.get(1).toString());
        minLng = Math.min(minLng, lng);
        maxLng = Math.max(maxLng, lng);
        minLat = Math.min(minLat, lat);
        maxLat = Math.max(maxLat, lat);
        ArrayList<Double> bbox = new ArrayList<>();
        bbox.add(minLng);
        bbox.add(minLat);
        bbox.add(maxLng);
        bbox.add(maxLat);
        return bbox;
    }

    private static List<Double> getBBOXFromTypeEqualsMultiPoint(List<Object> coordinates) {
        double minLng = 180.0, maxLng = -180.0, minLat = 90.0, maxLat = -90.0;
        for (Object lngLat : coordinates) {
            List<Object> lngLatDouble = (List<Object>) lngLat;
            List<Double> featureBbox = getBBOXFromTypeEqualsPoint(lngLatDouble);
            minLng = Math.min(minLng, featureBbox.get(0));
            maxLng = Math.max(maxLng, featureBbox.get(2));
            minLat = Math.min(minLat, featureBbox.get(1));
            maxLat = Math.max(maxLat, featureBbox.get(3));
        }
        ArrayList<Double> bbox = new ArrayList<>();
        bbox.add(minLng);
        bbox.add(minLat);
        bbox.add(maxLng);
        bbox.add(maxLat);
        return bbox;
    }

    private static List<Double> getBBOXFromTypeEqualsLineString(List<Object> coordinates) {
        return getBBOXFromTypeEqualsMultiPoint(coordinates);
    }

    private static List<Double> getBBOXFromTypeEqualsMultiLineString(List<Object> coordinates) {
        double minLng = 180.0, maxLng = -180.0, minLat = 90.0, maxLat = -90.0;
        for (Object lineString : coordinates) {
            List<Object> lineStringObj = (List<Object>) lineString;
            List<Double> featureBbox = getBBOXFromTypeEqualsLineString(lineStringObj);
            minLng = Math.min(minLng, featureBbox.get(0));
            maxLng = Math.max(maxLng, featureBbox.get(2));
            minLat = Math.min(minLat, featureBbox.get(1));
            maxLat = Math.max(maxLat, featureBbox.get(3));
        }
        ArrayList<Double> bbox = new ArrayList<>();
        bbox.add(minLng);
        bbox.add(minLat);
        bbox.add(maxLng);
        bbox.add(maxLat);
        return bbox;
    }

    private static List<Double> getBBOXFromTypeEqualsPolygon(List<Object> coordinates) {
        return getBBOXFromTypeEqualsMultiLineString(coordinates);
    }

    private static List<Double> getBBOXFromTypeEqualsMultiPolygon(List<Object> coordinates) {
        double minLng = 180.0, maxLng = -180.0, minLat = 90.0, maxLat = -90.0;
        for (Object polygonObj : coordinates) {
            List<Object> polygon = (List<Object>) polygonObj;
            List<Double> featureBbox = getBBOXFromTypeEqualsPolygon(polygon);
            minLng = Math.min(minLng, featureBbox.get(0));
            maxLng = Math.max(maxLng, featureBbox.get(2));
            minLat = Math.min(minLat, featureBbox.get(1));
            maxLat = Math.max(maxLat, featureBbox.get(3));
        }
        ArrayList<Double> bbox = new ArrayList<>();
        bbox.add(minLng);
        bbox.add(minLat);
        bbox.add(maxLng);
        bbox.add(maxLat);
        return bbox;
    }

    public static List<Double> getCenterFromBBOX(List<Double> bbox) {
        double centerLng = (bbox.get(0) + bbox.get(2)) / 2;
        double centerLat = (bbox.get(1) + bbox.get(3)) / 2;
        ArrayList<Double> center = new ArrayList<>();
        center.add(centerLng);
        center.add(centerLat);
        return center;
    }

    public static List<String> getFields() {
        ArrayList<Map<String, Object>> features = (ArrayList<Map<String, Object>>) root.get("features");
        Map<String, Object> feature = features.get(0);
        Map<String, Object> properties = (Map<String, Object>) feature.get("properties");
        ArrayList<String> fields = new ArrayList<>();
        if (properties == null) {
            return fields;
        }
        for (Map.Entry<String, Object> property : properties.entrySet()) {
            fields.add(property.getKey());
        }
        return fields;
    }

    public static int getFeatureCount() {
        ArrayList<Map<String, Object>> features = (ArrayList<Map<String, Object>>) root.get("features");
        return features.size();
    }

    public static List<Map<String, Object>> getAttrs() {
        ArrayList<Map<String, Object>> features = (ArrayList<Map<String, Object>>) root.get("features");
        List<Map<String, Object>> properties = features.stream().map(feature -> (Map<String, Object>) feature.get("properties")).filter(e -> !Objects.isNull(e)).collect(Collectors.toList());
        return properties;
    }

    public static List getUniqueValues(String field, String method) {
        ArrayList<Map<String, Object>> features = (ArrayList<Map<String, Object>>) root.get("features");
        List<Object> properties = features.stream().map(feature -> ((Map<String, Object>) feature.get("properties")).get(field)).distinct().collect(Collectors.toList());
        Class<?> T = properties.get(0).getClass();
        switch (T.getName()) {
            case "java.lang.Double":
                List<Double> doubleValues = properties.stream().map(property -> Double.parseDouble(property.toString())).collect(Collectors.toList());
                doubleValues.sort(new Comparator<Double>() {
                    @Override
                    public int compare(Double o1, Double o2) {
                        if (method.equals("asc")) {
                            return o1.compareTo(o2);
                        } else {
                            return o2.compareTo(o1);
                        }
                    }
                });
                return doubleValues;
            case "java.lang.Integer":
                List<Integer> integerValues = properties.stream().map(property -> Integer.parseInt(property.toString())).collect(Collectors.toList());
                integerValues.sort(new Comparator<Integer>() {
                    @Override
                    public int compare(Integer o1, Integer o2) {
                        if (method.equals("asc")) {
                            return o1.compareTo(o2);
                        } else {
                            return o2.compareTo(o1);
                        }
                    }
                });
                return integerValues;
            case "java.lang.Float":
                List<Float> floatValues = properties.stream().map(property -> Float.parseFloat(property.toString())).collect(Collectors.toList());
                floatValues.sort(new Comparator<Float>() {
                    @Override
                    public int compare(Float o1, Float o2) {
                        if (method.equals("asc")) {
                            return o1.compareTo(o2);
                        } else {
                            return o2.compareTo(o1);
                        }
                    }
                });
                return floatValues;
            case "java.lang.Long":
                List<Long> longValues = properties.stream().map(property -> Long.parseLong(property.toString())).collect(Collectors.toList());
                longValues.sort(new Comparator<Long>() {
                    @Override
                    public int compare(Long o1, Long o2) {
                        if (method.equals("asc")) {
                            return o1.compareTo(o2);
                        } else {
                            return o2.compareTo(o1);
                        }
                    }
                });
                return longValues;
            case "java.lang.Short":
                List<Short> shortValues = properties.stream().map(property -> Short.parseShort(property.toString())).collect(Collectors.toList());
                shortValues.sort(new Comparator<Short>() {
                    @Override
                    public int compare(Short o1, Short o2) {
                        if (method.equals("asc")) {
                            return o1.compareTo(o2);
                        } else {
                            return o2.compareTo(o1);
                        }
                    }
                });
                return shortValues;
            case "java.lang.String":
                List<String> stringValues = properties.stream().map(property -> property.toString()).collect(Collectors.toList());
                Collections.sort(stringValues);
                if (!method.equals("asc")) {
                    Collections.reverse(stringValues);
                }
                return stringValues;
            default:
                return new ArrayList<>();
        }
    }
}
