package nnu.wyz.systemMS.utils;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.coverage.processing.Operations;
import org.geotools.geometry.Envelope2D;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.util.factory.Hints;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @description:
 * @author: yzwang
 * @time: 2024/4/2 19:56
 */

public class GeoToolsUtil {
    private static final ThreadLocal<File> file = new ThreadLocal<>();

    public static void init(String path) {
        File fileInstance = new File(path);
        if (!fileInstance.exists()) {
            throw new RuntimeException("文件不存在");
        }
        file.set(fileInstance);
    }

    public static String getTiffEpsgCode() throws IOException {
        File fileInstance = file.get();
        if (fileInstance == null) {
            throw new IllegalStateException("文件未初始化");
        }
        AbstractGridFormat format = GridFormatFinder.findFormat(fileInstance);
        GridCoverage2DReader reader = format.getReader(fileInstance);
        GridCoverage2D coverage;
        synchronized (reader) {
            coverage = reader.read(null);
        }
        CoordinateReferenceSystem crs = coverage.getCoordinateReferenceSystem2D();
        return extractEPSG(crs.toWKT()).get("EPSG");
    }

    public static void transformTiffCRS(String outputPath, String EPSGCode) throws IOException, FactoryException {
        File fileInstance = file.get();
        if (fileInstance == null) {
            throw new IllegalStateException("文件未初始化");
        }
        AbstractGridFormat format = GridFormatFinder.findFormat(fileInstance);
        GridCoverage2DReader reader = format.getReader(fileInstance);
        GridCoverage2D coverage;
        synchronized (reader) {
            coverage = reader.read(null);
        }
        CoordinateReferenceSystem crs = coverage.getCoordinateReferenceSystem2D();
        Hints hints = new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE);
        CRSAuthorityFactory factory = ReferencingFactoryFinder.getCRSAuthorityFactory("EPSG", hints);
        CoordinateReferenceSystem new_crs = factory.createCoordinateReferenceSystem("EPSG:" + EPSGCode);
        GridCoverage2D newCoverage2D = (GridCoverage2D) Operations.DEFAULT.resample(coverage, new_crs);
        final File writeFile = new File(outputPath);
        final GridCoverageWriter writer = format.getWriter(writeFile);
        try {
            writer.write(newCoverage2D, null);
        } catch (IOException ignored) {
        } finally {
            try {
                writer.dispose();
            } catch (Throwable ignored) {
            }
        }
    }

    public static List<Double> getTiffBbox() throws IOException {
        // 根据具体需求实现获取tiff文件的边界框的逻辑
        File fileInstance = file.get();
        if (fileInstance == null) {
            throw new IllegalStateException("文件未初始化");
        }
        AbstractGridFormat format = GridFormatFinder.findFormat(fileInstance);
        GridCoverage2DReader reader = format.getReader(fileInstance);
        GridCoverage2D coverage;
        synchronized (reader) {
            coverage = reader.read(null);
        }
        CoordinateReferenceSystem crs = coverage.getCoordinateReferenceSystem2D();
        Envelope2D envelope2D = coverage.getEnvelope2D();
        double maxX = envelope2D.getMaxX();
        double minX = envelope2D.getMinX();
        double maxY = envelope2D.getMaxY();
        double minY = envelope2D.getMinY();
        return Arrays.asList(minX, minY, maxX, maxY);
    }

    public static Map<String, String> extractEPSG(String crsWKTStr) {
        Map<String, String> map = new HashMap<>();
        String pattern = "AUTHORITY\\[\"(\\w+)\",\"(\\d+)\"\\]";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(crsWKTStr);
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2);
            map.put(key, value);
        }
        return map;
    }
}
