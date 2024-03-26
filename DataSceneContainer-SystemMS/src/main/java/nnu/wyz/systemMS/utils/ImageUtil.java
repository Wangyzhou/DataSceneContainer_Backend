package nnu.wyz.systemMS.utils;

import cn.hutool.core.util.IdUtil;
import lombok.SneakyThrows;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.text.MessageFormat;

/**
 * @description:
 * @author: yzwang
 * @time: 2024/3/21 9:43
 */

@Component
public class ImageUtil {

    private static String imageCompressPath;

    @Value("${fileTempPath}")
    private String imageTempPath;

    @PostConstruct
    public void init() {
        imageCompressPath = imageTempPath;
    }

    @SneakyThrows
    public static MultipartFile compressImageFile(File file) {
        if (file == null || !file.exists()) {
            return null;
        }
        File imageCompressTempDir = new File(imageCompressPath);
        if(!imageCompressTempDir.exists()) {
            imageCompressTempDir.mkdirs();
        }
        String suffix = file.getName().substring(file.getName().lastIndexOf(".") + 1);
        File imageFile = new File(imageCompressPath + File.separator + IdUtil.fastSimpleUUID() + "." + suffix);
//        System.out.println(imageCompressPath + File.separator + IdUtil.fastSimpleUUID() + "." + suffix);
        if (file.length() >= 1024 * 1024 * 0.1) {
            if (file.length() >= 1024 * 1024 * 0.1 && file.length() < 1024 * 1024) {
                Thumbnails.of(file).scale(0.5f).outputQuality(1f).toFile(imageFile);
            } else if(file.length() >= 1024 * 1024 && file.length() < 1024 * 1024 * 2) {
                Thumbnails.of(file).scale(0.5f).outputQuality(0.5f).toFile(imageFile);
            } else {
                Thumbnails.of(file).scale(0.5f).outputQuality(0.1f).toFile(imageFile);
            }
        } else {
            FileUtils.copyFile(file, imageFile);
        }
        return new MockMultipartFile(imageFile.getName(), imageFile.getName(), MimeTypesUtil.getMimeType(suffix), FileUtils.readFileToByteArray(imageFile));
    }

    /**
     * 判断文件是否为图片
     */
    public static boolean isPicture(String imgName) {
        boolean flag = false;
        if (StringUtils.isBlank(imgName)) {
            return false;
        }
        String[] arr = {"bmp", "dib", "gif", "jfif", "jpe", "jpeg", "jpg", "png", "tif", "tiff", "ico"};
        for (String item : arr) {
            if (item.equals(imgName)) {
                flag = true;
                break;
            }
        }
        return flag;
    }

}
