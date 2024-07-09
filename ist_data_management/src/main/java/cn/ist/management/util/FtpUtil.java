package cn.ist.management.util;

import cn.ist.management.config.FtpConfig;
import cn.ist.management.po.model.UnstructuredDataModel;
import cn.ist.management.util.thread.FileChangeEvent;
import cn.ist.management.util.thread.ListenerFileChangeThreadRunnable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class FtpUtil {

    private final FtpConfig ftpConfig;

    public static final String DIR_SPLIT = "/";

    /**
     * 获取 FTPClient
     * @return FTPClient
     */
    private FTPClient getFTPClient(String url) {
        FTPClient ftpClient = ftpConfig.connect(url);
        if (ftpClient == null || !FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
            throw new RuntimeException("ftp客户端异常");
        }
        return ftpClient;
    }

//    /**
//     * 获取FTP某一特定目录下的所有文件名称
//     *
//     * @param ftpDirPath FTP上的目标文件路径
//     */
//    public List<String> getFileNameList(String ftpDirPath) {
//        FTPClient ftpClient = getFTPClient();
//        try {
//            // 通过提供的文件路径获取 FTPFile 文件列表
//            // FTPFile[] ftpFiles = ftpClient.listFiles(ftpDirPath, FTPFile::isFile); // 只获取文件
//            // FTPFile[] ftpFiles = ftpClient.listFiles(ftpDirPath, FTPFile::isDirectory); // 只获取目录
//            FTPFile[] ftpFiles = ftpClient.listFiles(ftpDirPath);
//            if (ftpFiles != null && ftpFiles.length > 0) {
//                return Arrays.stream(ftpFiles).map(FTPFile::getName).collect(Collectors.toList());
//            }
//            log.error(String.format("路径有误，或目录【%s】为空", ftpDirPath));
//        } catch (IOException e) {
//            log.error("文件获取异常:", e);
//        } finally {
//            try {
//                if (ftpClient.isConnected()) {
//                    ftpClient.logout();
//                    ftpClient.disconnect();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return null;
//    }
//
//    /**
//     * 上传文件
//     *
//     * @param uploadPath 上传路径
//     * @param fileName   文件名
//     * @param input      文件输入流
//     * @return 上传结果
//     */
//    public boolean upload(String uploadPath, String fileName, InputStream input) {
//        FTPClient ftpClient = getFTPClient(url);
//        try {
//            // 切换到工作目录
//            if (!ftpClient.changeWorkingDirectory(uploadPath)) {
//                ftpClient.makeDirectory(uploadPath);
//                ftpClient.changeWorkingDirectory(uploadPath);
//            }
//            // 文件写入
//            boolean storeFile = ftpClient.storeFile(fileName, input);
//            if (storeFile) {
//                log.info("文件:{}上传成功", fileName);
//            } else {
//                throw new RuntimeException("ftp文件写入异常");
//            }
//            ftpClient.logout();
//            return true;
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (input != null) {
//                    input.close();
//                }
//                if (ftpClient.isConnected()) {
//                    ftpClient.disconnect();
//                }
//            } catch (IOException ioe) {
//                ioe.printStackTrace();
//            }
//        }
//        return false;
//    }

    public Map<String, Object> getDirectory(String url, String ftpDirPath, String localDirPath) {
        FTPClient ftpClient = getFTPClient(url);
        Map<String, Object> list = new HashMap<>();

        try {
            // 切换到FTP服务器文件夹
//            log.info(ftpClient.printWorkingDirectory());
            if (!ftpClient.changeWorkingDirectory(ftpDirPath)) {
                log.error("FTP文件夹路径不存在: {}", ftpDirPath);
//                return null;
            }

            // 获取FTP文件夹下的所有文件
            FTPFile[] ftpFiles = ftpClient.listFiles();
            if (ftpFiles != null && ftpFiles.length > 0) {
                for (FTPFile ftpFile : ftpFiles) {
                    if (ftpFile.isFile()) {
                        // 获取文件
                        Map<String, String> tmp = new HashMap<>();
                        tmp.put("fileName", ftpFile.getName());
                        tmp.put("ftpDirPath", ftpDirPath);
                        tmp.put("localDirPath", localDirPath);
                        long timestamp = System.currentTimeMillis();
                        Random random = new Random();
                        int randomInt = random.nextInt(1000);
                        String uniqueId = timestamp + "_" + randomInt;
                        list.put(uniqueId, tmp);
//                        list.put(ftpFile.getName(), ftpDirPath);
                        log.info(ftpFile.getName());
                    } else if (ftpFile.isDirectory()) {
                        // 递归下载子文件夹
                        Map<String, Object> dirMap = getDirectory(url, ftpDirPath + "/" + ftpFile.getName(), localDirPath + "/" + ftpFile.getName());
                        list.putAll(dirMap);
                    }
                }
                log.info("整个FTP文件夹获取成功");
                return list;
            } else {
                log.info("FTP文件夹为空: {}", ftpDirPath);
            }
        } catch (IOException e) {
            log.error("下载FTP文件夹失败", e);
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return list;
    }

//    public boolean downloadDirectory(String ftpDirPath, String localDirPath) {
//        FTPClient ftpClient = getFTPClient();
//
//        try {
//            // 切换到FTP服务器文件夹
//            if (!ftpClient.changeWorkingDirectory(ftpDirPath)) {
//                log.error("FTP文件夹路径不存在: {}", ftpDirPath);
//                return false;
//            }
//
//            // 获取FTP文件夹下的所有文件
//            FTPFile[] ftpFiles = ftpClient.listFiles();
//            if (ftpFiles != null && ftpFiles.length > 0) {
//                for (FTPFile ftpFile : ftpFiles) {
//                    if (ftpFile.isFile()) {
//                        // 下载文件
//                        download(ftpDirPath, ftpFile.getName(), localDirPath);
//                        log.info(ftpFile.getName());
//                    } else if (ftpFile.isDirectory()) {
//                        // 递归下载子文件夹
//                        downloadDirectory(ftpDirPath + "/" + ftpFile.getName(), localDirPath + "/" + ftpFile.getName());
//                    }
//                }
//                log.info("整个FTP文件夹下载成功");
//                return true;
//            } else {
//                log.info("FTP文件夹为空: {}", ftpDirPath);
//            }
//        } catch (IOException e) {
//            log.error("下载FTP文件夹失败", e);
//        } finally {
//            try {
//                if (ftpClient.isConnected()) {
//                    ftpClient.logout();
//                    ftpClient.disconnect();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        return false;
//    }


    /**
     * 下载文件 *
     *
     * @param ftpPath     FTP服务器文件目录 *
     * @param ftpFileName 文件名称 *
     * @param localPath   下载后的文件路径 *
     * @return
     */
    public boolean download(String url, String ftpPath, String ftpFileName, String localPath) {
        FTPClient ftpClient = getFTPClient(url);
        ftpClient.enterLocalPassiveMode();

        try {
            ftpClient.setControlEncoding("GBK");
            ftpClient.enterLocalPassiveMode();
            FTPFile[] ftpFiles = ftpClient.listFiles(ftpPath, file -> file.isFile() && file.getName().equals(ftpFileName));
            if (ftpFiles != null && ftpFiles.length > 0) {
                FTPFile ftpFile = ftpFiles[0];
                File localFile = new File(localPath + DIR_SPLIT + ftpFile.getName());
                // 检查本地文件是否已经存在
                if (localFile.exists()) {
                    log.warn("文件 {} 已存在于本地，跳过下载", ftpFileName);
                    return true;
                }
                // 判断本地路径目录是否存在，不存在则创建
                if (!localFile.getParentFile().exists()) {
                    localFile.getParentFile().mkdirs();
                }

                ftpClient.enterLocalPassiveMode();
                ftpClient.changeWorkingDirectory(ftpPath);

                OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(String.valueOf(localFile.toPath())));
//                outputStream = Files.newOutputStream(localFile.toPath());
                // ftpClient.retrieveFile(new String(ftpFile.getName().getBytes("GB2312"),"ISO-8859-1"), outputStream);
                ftpClient.retrieveFile(ftpFile.getName(), outputStream);

                outputStream.close();

                log.info("fileName:{},size:{}", ftpFile.getName(), ftpFile.getSize());
                log.info("下载文件成功...");
                return true;
            } else {
                log.info("文件不存在，filePathname:{},", ftpPath + DIR_SPLIT + ftpFileName);
            }
        } catch (Exception e) {
            log.error("下载文件失败...");
            e.printStackTrace();
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

//    /**
//     * 从FTP服务器删除文件或目录
//     * 存在文件的目录无法删除
//     *
//     * @param ftpPath  服务器文件存储路径
//     * @param fileName 服务器文件存储名称
//     * @return 删除结果
//     */
//    public boolean delete(String ftpPath, String fileName) {
//        FTPClient ftpClient = getFTPClient();
//        try {
//            // 在 ftp 目录下获取文件名与 fileName 匹配的文件信息
//            FTPFile[] ftpFiles = ftpClient.listFiles(ftpPath, file -> file.getName().equals(fileName));
//            // 删除文件
//            if (ftpFiles != null && ftpFiles.length > 0) {
//                boolean del;
//                String deleteFilePath = ftpPath + DIR_SPLIT + fileName;
//                FTPFile ftpFile = ftpFiles[0];
//                if (ftpFile.isDirectory()) {
//                    del = ftpClient.removeDirectory(deleteFilePath);
//                } else {
//                    del = ftpClient.deleteFile(deleteFilePath);
//                }
//                log.info(del ? "文件:{}删除成功" : "文件:{}删除失败", fileName);
//                return del;
//            } else {
//                log.warn("文件:{}未找到", fileName);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (ftpClient.isConnected()) {
//                    ftpClient.logout();
//                    ftpClient.disconnect();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return false;
//    }

    /**
     * 从本地删除文件或目录
     * 存在文件的目录无法删除
     *
     * @param localPath  服务器文件存储路径
     * @param fileName 服务器文件存储名称
     * @return 删除结果
     */
    public boolean deleteLocal(String url, String localPath, String fileName) {
        FTPClient ftpClient = getFTPClient(url);
        boolean del;
        try {
            String deleteFilePath = localPath + DIR_SPLIT + fileName;
            File localFile = new File(deleteFilePath);
            if (localFile.isDirectory()) {
                del = ftpClient.removeDirectory(deleteFilePath);
            } else {
                del = ftpClient.deleteFile(deleteFilePath);
            }
            log.info(del ? "文件:{}删除成功" : "文件:{}删除失败", fileName);
            return del;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public void addListenerFileChange(String url, UnstructuredDataModel model) {
        FTPClient client = getFTPClient(url);
        ListenerFileChangeThreadRunnable thread = new ListenerFileChangeThreadRunnable(client, (data) -> {
            Map<String, Object> res = new HashMap<>();
            res.put("fileName", extractDirectoryName(data.getFileName()));
            res.put("ftpDirPath", data.getFileName());
            res.put("localDirPath", "./ftpfiles" + data.getFileName());
            switch (data.getEventType()) {
                case FILE_ADD:
                    model.insertOne(res);
                    break;
                case FILE_UPDATE:
                    model.updateOne(res, res);
                    break;
                case FILE_DELETED:
                    model.deleteOne(res);
                    break;
                default:
                    break;
            }
        });
        new Thread(thread).start();
    }

    private static String extractDirectoryName(String filePath) {
        // 使用正则表达式匹配斜杠后的内容
        Pattern pattern = Pattern.compile("/(.+)");
        Matcher matcher = pattern.matcher(filePath);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return ""; // 如果没有匹配到，返回空字符串或其他适当的值
        }
    }
}


