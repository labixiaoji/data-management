package cn.ist.management.util.thread;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ListenerFileChangeThreadRunnable implements Runnable {

    private final FTPClient ftpClient;

    private volatile boolean stop;

    private final Map<String, Long> fileMemory;

    private final FileChangeEvent fileChangeEvent;

    public ListenerFileChangeThreadRunnable(FTPClient ftpClient, FileChangeEvent fileChangeEvent) {
        this.ftpClient = ftpClient;
        this.fileChangeEvent = fileChangeEvent;
        //读取上次的缓存
        this.fileMemory = new HashMap<>();
    }

    @Override
    public void run() {
        while (!stop) {
            try {
                FTPFile[] ftpFiles = ftpClient.listFiles();

                //判断文件被删除
                hanleDel(ftpFiles);

                //判断文件是否有更改或新增
                for (FTPFile ftpFile : ftpFiles) {
                    //判断是否为文件夹
                    if (ftpFile.isDirectory()) {
//                        log.info("{}为文件不进行监听操作", ftpFile.getName());
                        continue;
                    }
                    FileChangeData fileChangeData = new FileChangeData();
                    fileChangeData.setFileName(ftpFile.getName());
                    fileChangeData.setFileSize(ftpFile.getSize());
                    fileChangeData.setFtpFile(ftpFile);
                    fileChangeData.setFtpClient(ftpClient);
                    //文件是否存在于缓存文件列表中
                    if (fileMemory.containsKey(ftpFile.getName())) {
//                        log.info("文件{}在内存中已经存在，进行大小判断", ftpFile.getName());
                        handleUpdate(ftpFile, fileChangeData);
                        continue;
                    }
                    handleAdd(ftpFile, fileChangeData);
                }
            } catch (Exception e) {
                log.error("FTP监控异常：{}", e);
                throw new RuntimeException(e);
            }
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                log.error("FTP监控异常：{}", e);
                throw new RuntimeException(e);
            }
        }
    }

    private void handleAdd(FTPFile ftpFile, FileChangeData fileChangeData) {
        //log.info("文件{}在内存中不存在进行缓存操作", ftpFile.getName());
        log.info("监听到FTP: {} 新增文件：{}", ftpClient.getPassiveHost(), ftpFile.getName());
        fileMemory.put(ftpFile.getName(), ftpFile.getSize());
        fileChangeData.setEventType(FileChangeType.FILE_ADD);
        try {
            fileChangeEvent.change(fileChangeData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleUpdate(FTPFile ftpFile, FileChangeData fileChangeData) {
        if (!Objects.equals(fileMemory.get(ftpFile.getName()), ftpFile.getSize())) {
//                            log.info("文件{}在内存中已经存在且大小不一致，进行更新缓存操作", ftpFile.getName());
            log.info("监听到FTP: {} 更新文件：{}", ftpClient.getPassiveHost(), ftpFile.getName());

            fileMemory.put(ftpFile.getName(), ftpFile.getSize());

            fileChangeData.setEventType(FileChangeType.FILE_UPDATE);
            try {
                fileChangeEvent.change(fileChangeData);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void hanleDel(FTPFile[] ftpFiles) {
        if (fileMemory.size() > 0) {
            Set<String> fileNames = new HashSet<>();
            for (FTPFile ftpFile : ftpFiles) {
                if (ftpFile.isDirectory()) {
//                            log.info("文件夹:{} 不做删除判断",ftpFile.getName());
                    continue;
                }
                fileNames.add(ftpFile.getName());
            }
            Set<Map.Entry<String, Long>> entries = fileMemory.entrySet();
            List<String> removeKeys = new ArrayList<>();
            for (Map.Entry<String, Long> entry : entries) {
                if (!fileNames.contains(entry.getKey())) {
                    log.info("监听到FTP: {} 删除文件：{}", ftpClient.getPassiveHost(), entry.getKey());
                    FileChangeData fileChangeData = new FileChangeData();
                    fileChangeData.setEventType(FileChangeType.FILE_DELETED);
                    fileChangeData.setFileName(entry.getKey());
                    fileChangeData.setFileSize(entry.getValue());
                    try {
                        fileChangeEvent.change(fileChangeData);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    removeKeys.add(entry.getKey());
                }
            }

            removeKeys.forEach(rmkey -> {
                    fileMemory.remove(rmkey);
                }
            );
        }
    }

    public boolean stopListener() {
        this.stop = Boolean.TRUE;
        this.fileMemory.clear();
        return this.stop;
    }
}