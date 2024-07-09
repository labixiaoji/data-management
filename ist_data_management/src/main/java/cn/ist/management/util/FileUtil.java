package cn.ist.management.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Component
public class FileUtil {
    public List<Map<String, Object>> listFiles(String directoryPath) throws IOException {
        List<Map<String, Object>> fileList = new ArrayList<>();

        Path directory = Paths.get(directoryPath);
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Map<String, Object> fileInfo = new HashMap<>();
                fileInfo.put("path", file.toString());
                fileInfo.put("size", attrs.size());
                fileInfo.put("lastModified", attrs.lastModifiedTime().toString());
                fileList.add(fileInfo);
                return FileVisitResult.CONTINUE;
            }
        });

        return fileList;
    }


    /**
     * 获取路径下的所有文件
     *
     * @param directoryPath 需要遍历的文件夹路径
     * @return
     */
    public List<Map<String, Object>> getAllFiles(String directoryPath) {
        List<Map<String, Object>> list = new ArrayList<>();
        File baseFile = new File(directoryPath);
        if (!baseFile.exists()) {
            return list;
        }

        traverseDirectory(baseFile, list, directoryPath);

        return list;
    }

    public  boolean deleteDir(String path) {
        File file = new File(path);
        return deleteFile(file);
    }

    /**
     * 删除目录及其子文件夹
     *
     * @param rootFilePath 根目录
     * @return boolean
     */
    public boolean deleteFile(File rootFilePath) {
        if (rootFilePath.isDirectory()) {
            for (File file : rootFilePath.listFiles()) {
                deleteFile(file);
            }
        }
        return rootFilePath.delete();
    }


    private void traverseDirectory(File directory, List<Map<String, Object>> fileList, String basePath) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                Map<String, Object> map = new HashMap<>();
                map.put("fileName", file.getName());
                map.put("relativePath", basePath + "\\" + file.getName());  // 添加相对路径信息
                map.put("size", file.length());

                if (file.isDirectory()) {
                    basePath = directory + "\\" + file.getName();
                    // 递归处理子文件夹，传递相对路径信息
                    traverseDirectory(file, fileList, basePath);
                } else {
                    fileList.add(map);
                }
            }
        }
    }


    /**
     * 获取文件最后修改时间
     */
    public  String getFileTime(String filePath) {
        File file = new File(filePath);
        long time = file.lastModified();//返回文件最后修改时间，是以个long型毫秒数
        String ctime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(time));
        return ctime;
    }

    /**
     * 获取文件大小,整数
     */
    public  String getFileSize(String path) {
        File file = new File(path);
        long length = file.length();
        return FormetFileSize(length);
    }

    /**
     * 转换文件大小，带单位的字符串
     *
     * @param fileLength 文件大小值
     * @return String 文件大小
     * @Title: FormetFileSize
     * @author projectNo
     */
    public  String FormetFileSize(long fileLength) {
        DecimalFormat df = new DecimalFormat("#.00");
        DecimalFormat d = new DecimalFormat("#");
        String fileSizeString = "";
        if (fileLength < 1024) {
            fileSizeString = d.format((double) fileLength) + "B";
        } else if (fileLength < 1048576) {
            fileSizeString = df.format((double) fileLength / 1024) + "KB";
        } else if (fileLength < 1073741824) {
            fileSizeString = df.format((double) fileLength / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) fileLength / 1073741824) + "GB";
        }
        return fileSizeString;
    }


}
