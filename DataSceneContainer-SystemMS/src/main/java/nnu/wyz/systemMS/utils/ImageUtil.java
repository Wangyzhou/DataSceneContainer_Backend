package nnu.wyz.systemMS.utils;

import cn.hutool.core.util.IdUtil;
import lombok.SneakyThrows;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
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
    public static File compressImageFile(MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            return null;
        }
        String fileName = multipartFile.getOriginalFilename();
        String suffix = StringUtils.substringAfterLast(fileName, ".");
        File imageFile = new File(imageCompressPath + File.separator + IdUtil.fastSimpleUUID() + "." + suffix);
        multipartFile.transferTo(imageFile);
        if (multipartFile.getSize() >= 1024 * 1024 * 0.1) {
            if (multipartFile.getSize() >= 1024 * 1024 * 0.1 && multipartFile.getSize() < 1024 * 1024) {
                Thumbnails.of(multipartFile.getInputStream()).scale(0.5f).outputQuality(1f).toFile(imageFile);
            } else if(multipartFile.getSize() >= 1024 * 1024 && multipartFile.getSize() < 1024 * 1024 * 2) {
                Thumbnails.of(multipartFile.getInputStream()).scale(0.5f).outputQuality(0.5f).toFile(imageFile);
            } else {
                Thumbnails.of(multipartFile.getInputStream()).scale(0.5f).outputQuality(0.1f).toFile(imageFile);
            }
        }
        return imageFile;
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
