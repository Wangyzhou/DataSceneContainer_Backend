package nnu.wyz.fileMS.utils;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.ClassUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;


/**
 * @Description 文件处理工具类
 * @Author bin
 * @Date 2021/10/20
 */
@Slf4j
public class FileUtils {

    private static final int BUFFER_SIZE = 2 * 1024;

    /**
     * 上传单文件
     *
     * @param file     上传的文件
     * @param inputDir 上传的文件目录(不包括文件名)
     * @return boolean
     * @Author bin
     **/
    public static boolean uploadSingleFile(MultipartFile file, String inputDir, String name) {

        // 如果文件大小为0 返回false
        if (!(file.getSize() > 0)) {
            return false;
        }
        File localFile = new File(inputDir);
        //先创建目录
        if (!localFile.exists()) {
            localFile.mkdirs();
            log.info("create dir, path : {}", inputDir);
        }
        String path = inputDir + "/" + name;

        localFile = new File(path);
        if(localFile.exists()) {
            return true;
        }
        boolean b;
        try {

            //创建文件
            boolean isMake = mkFile(localFile);
            if (!isMake) {
                return false;
            }
            //写入内容到文件里
            b = uploadFileByBufferStream(file.getInputStream(), localFile);
        } catch (IOException e) {
            e.printStackTrace();
            b = false;
        }
        return b;
    }

    /**
     * 创建文件，如果存在的话就先删除再创建
     *
     * @param file
     * @return boolean
     * @Author bin
     **/
    public static boolean mkFile(File file) {
        try {
            if (file.exists()) {
                // 如果文件存在
                // 判断该文件的md5值
                // 如果一样的话就不用再重复上传了
                // 不一样的话就删除文件再上传
                // 这个策略不知道有没有必要，因为上传的速度本来就很快了
                // if (md5.equals(getFileMd5(localFile)))
                //     return true
                boolean delete = file.delete();
                if (!delete) {
                    log.error("Delete exist file \"{}\" failed!!!", file.getPath(), new Exception("Delete exist file \"" + file.getPath() + "\" failed!!!"));
                    return false;
                }
            }
            // 如果文件不存在，则创建新的文件
            // 保证这个文件的父文件夹必须要存在
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            file.createNewFile();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 通过缓冲流上传文件
     *
     * @param in
     * @param localFile
     * @return boolean
     * @Author bin
     **/
    public static boolean uploadFileByBufferStream(InputStream in, File localFile) {

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        // InputStream in = null;
        try {
            //如果文件不存在，则创建新的文件
            if (!localFile.exists()) {
                localFile.createNewFile();
            }

            // 1.造节点流
            fos = new FileOutputStream(localFile);
            // in = file.getInputStream();

            // 2.造缓冲流
            bis = new BufferedInputStream(in);
            bos = new BufferedOutputStream(fos);

            // 3.复制的细节：读取、写入
            byte[] bytes = new byte[1024];
            int len;
            while ((len = bis.read(bytes)) != -1) {
                bos.write(bytes, 0, len);
            }
            bos.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (bos != null) {
                    bos.close();
                }
                if (bis != null) {
                    bis.close();
                }
            } catch (IOException e) {
                log.error("InputStream or OutputStream close error");
                return false;
            }
        }
        return true;
    }

    /**
     * 得到文件的md5
     *
     * @param file
     * @return java.lang.String 返回md5
     * @Author bin
     **/
    public static String getFileMd5(File file) {
        FileInputStream fis = null;
        String md5;
        try {
            fis = new FileInputStream(file);
            md5 = DigestUtils.md5DigestAsHex(fis);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                log.error("InputStream or OutputStream close error");
                return "";
            }
        }
        return md5;
    }


    /**
     * 解压文件
     *
     * @param inputDir 输入的文件路径
     * @param destDir  输出的文件路径
     * @return java.util.List<com.alibaba.fastjson.JSONObject>
     * @Author bin
     **/
    public static List<JSONObject> zipUncompress(String inputDir, String destDir) throws Exception {
        // 压缩的文件的路径
        List<JSONObject> fileList = new ArrayList<>();
        File srcFile = new File(inputDir);
        if (!srcFile.exists()) {
            throw new Exception(srcFile.getPath() + "所指文件不存在");
        }
        // 第二个参数设置编码，防止处理文件名存在中文的zip包时，控制台报错
        long start = System.currentTimeMillis();
        ZipFile zipFile = new ZipFile(srcFile, Charset.forName("GBK"));
        Enumeration<?> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            // 如果是文件夹，则不管，默认zip不能有文件夹
            if (entry.isDirectory()) {
                // String dirPath = destDirPath + "/" + entry.getName();
                // srcFile.mkdirs();
            } else {
                // 如果是文件，就先创建一个文件，然后用io流把内容copy过去
                String uploadPath = destDir + "/" + entry.getName();
                File targetFile = new File(uploadPath);
                mkFile(targetFile);
                // 将压缩文件内容写入到这个文件中
                uploadFileByBufferStream(zipFile.getInputStream(entry), targetFile);

                //把文件绝对路径加到pathList里
                JSONObject o = new JSONObject();
                o.put("fileName", targetFile.getName());
                o.put("path", uploadPath);
                fileList.add(o);
            }
        }
        zipFile.close();
        long end = System.currentTimeMillis();
        log.info("zip uncompress success");
        return fileList;
    }


    /**
     * 下载文件
     *
     * @param path     文件存储的物理地址
     * @param response
     * @return void
     * @Author bin
     **/
    public static void downloadFile(String path, HttpServletResponse response) {

        try {
            // path是指欲下载的文件的路径。
            File file = new File(path);
            // 取得文件名。
            String filename = file.getName();
            // 取得文件的后缀名。
            String ext = filename.substring(filename.lastIndexOf(".") + 1).toUpperCase();

            // 以流的形式下载文件。
            InputStream fis = new BufferedInputStream(new FileInputStream(path));
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();
            // 清空response
            response.reset();
            // 设置response的Header
            response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(file.getName(), "utf-8"));
            response.addHeader("Content-Length", "" + file.length());
            OutputStream toClient = new BufferedOutputStream(response.getOutputStream());
            response.setContentType("application/octet-stream");
            toClient.write(buffer);
            toClient.flush();
            toClient.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // 再返回response会出错
        // 承载客户端和服务器进行Http交互的Socket连接在 `toClient.close()` 已经关闭了，还试图发送数据给客户端就会出错
        // return response;

    }

    /**
     * 下载文件 方式2
     *
     * @param path     文件存储的物理地址
     * @param response
     * @return void
     * @Author bin
     **/
    public static void downloadFile2(String path, HttpServletResponse response) {

        // path是指欲下载的文件的路径。
        File file = new File(path);
        // 取得文件名。
        String filename = file.getName();
        // 取得文件的后缀名。
        String ext = filename.substring(filename.lastIndexOf(".") + 1).toUpperCase();


        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            // 清空response
            response.reset();
            // 设置response的Header
            response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(file.getName(), "utf-8"));
            response.addHeader("Content-Length", "" + file.length());
            response.setContentType("application/octet-stream");

            // 以流的形式下载文件。
            bis = new BufferedInputStream(new FileInputStream(path));
            bos = new BufferedOutputStream(response.getOutputStream());
            // 读取、写入
            byte[] bytes = new byte[1024];
            int len;
            while ((len = bis.read(bytes)) != -1) {
                bos.write(bytes, 0, len);
            }
            bos.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (bos != null) {
                    bos.close();
                }
                if (bis != null) {
                    bis.close();
                }
            } catch (IOException e) {
                log.error("InputStream or OutputStream close error");
            }
        }

    }


    /**
     * 压缩成ZIP
     *
     * @param srcFiles 需要压缩的文件列表
     * @param tempPath 压缩文件输出路径
     * @return void
     * @Author bin
     **/
    public static void toZip(List<File> srcFiles, String tempPath) throws RuntimeException {
        long start = System.currentTimeMillis();
        FileOutputStream out;
        ZipOutputStream zos = null;

        try {
            //创建临时文件
            File outFile = new File(tempPath);
            if (!outFile.exists()) {
                mkFile(outFile);
            }
            log.info("Start compressing files: {}", new Date());
            out = new FileOutputStream(outFile);
            zos = new ZipOutputStream(out); //这个要放到循环外，不然要是文件名一样的话处理完第一个文件zos就被关了
            for (File srcFile : srcFiles) {
                byte[] buf = new byte[BUFFER_SIZE];
//                zos = new ZipOutputStream(out);
                zos.putNextEntry(new ZipEntry(srcFile.getName()));
                int len;
                FileInputStream in = new FileInputStream(srcFile);
                BufferedInputStream bis = new BufferedInputStream(in);
                while ((len = bis.read(buf)) != -1) {
                    zos.write(buf, 0, len);
                }
                bis.close();
            }
            zos.closeEntry();
            long end = System.currentTimeMillis();
            log.info("Compression is complete, cost: {}ms", end - start);
        } catch (Exception e) {
            throw new RuntimeException("zip error from ZipUtils", e);
        } finally {
            if (zos != null) {
                try {
                    zos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 删除文件
     *
     * @param filePath 文件物理地址
     * @return boolean
     * @Author bin
     **/
    public static boolean deleteFile(String filePath) {
        File localFile = new File(filePath);
        if (localFile.exists()) {
            // 如果文件存在
            // 判断该文件的md5值
            // 如果一样的话就不用再重复上传了
            // 不一样的话就删除文件再上传
            // 这个策略不知道有没有必要，因为上传的速度本来就很快了
            // if (md5.equals(getFileMd5(localFile)))
            //     return true;

            boolean delete = localFile.delete();
            if (!delete) {
                log.error("Delete exist file \"{}\" failed!!!", filePath, new Exception("Delete exist file \"" + filePath + "\" failed!!!"));
                return false;
            }
            return true;
        }
        return false;
    }


    /**
     * 获取服务器存放文件的目录路径
     *
     * @return 目录路径（String)
     */
    public static String getFileDir() {
        String path = ClassUtils.getDefaultClassLoader().getResource("").getPath().substring(1) + "static/file";
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return path;
    }


    /**
     * File 转 MultipartFile
     *
     * @param path 文件路径
     * @return org.springframework.web.multipart.MultipartFile
     * @Author bin
     **/
    public MultipartFile F2M(String path) throws Exception {
        MultipartFile multipartFile = null;
        File file = new File(path);
        if (file.exists()) {
            FileInputStream input = new FileInputStream(file);
            multipartFile = new MockMultipartFile("file", file.getName(), "text/plain", IOUtils.toByteArray(input));
        }
        return multipartFile;
    }


    /**
     * 得到文件的文件类型
     *
     * @param path 文件所在路径
     * @return java.lang.String
     * @Author bin
     **/
    public static String getFileType(String path) {
        String[] split = path.split("\\.");
        return split[split.length - 1];
    }


    /**
     * 复制文件
     *
     * @param source 源文件路径
     * @param dest   目标文件路径
     * @return void
     * @Author bin
     **/
    public static void copyFile(File source, File dest) {
        try {
            org.apache.commons.io.FileUtils.copyFile(source, dest);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除指定文件夹
     *
     * @param path 文件夹路径
     * @return void
     * @Author bin
     **/
    public static boolean deleteDirectory(String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            return false;
        }
        try {
            org.apache.commons.io.FileUtils.deleteDirectory(directory);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 根据提供的相对路径(relativePath)实现文件夹的多级目录复制
     *
     * @param index    index的生成方式为函数第一行注释的代码，
     *                 传入一个index是因为通常情况下这个函数要执行很多次，重复计算这个数值会很浪费时间
     *                 所以建议在调用这个方法的时候在外层先求出这个index
     * @param filePath 待处理的文件
     * @param args     需生成的新的文件路径
     * @return java.util.List<java.lang.String>
     * @Author bin
     **/
    public static String[] generateSamePath(int index, String filePath, String... args) {


        // relativePath = relativePath.replace("\\", "/");
        // int index = (relativePath.split("/")).length;

        String[] split = filePath.split("/");
        for (int i = index; i < split.length - 1; i++) {
            for (int j = 0; j < args.length; j++) {
                args[j] += ("/" + split[i]);
            }
        }

        return args;

    }

    /**
     * 根据提供的上传路径以及根路径对传入的参数进行处理，生成对应的多级目录
     *
     * @param filePath 文件路径
     * @param rootPath 根路径(构造该路径之后的文件及文件夹)
     * @param args     待生成的文件路径
     * @return java.lang.String[]
     * @Author bin
     **/
    public static String[] buildMultiDirPath(String filePath, String rootPath, String... args) {

        filePath = filePath.replace("\\", "/");
        rootPath = rootPath.replace("\\", "/");

        // 得到所指文件根据 / 分割后的数组长度，用于在savePath生成对应的文件(先把反斜杠全部替换成斜杠)
        int index = (rootPath.split("/")).length;

        return generateSamePath(index, filePath, args);


    }
}
