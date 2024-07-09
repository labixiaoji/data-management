package cn.ist.management.util.thread;

import lombok.Data;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

@Data
public class FileChangeData {

    /**
     * 文件信息
     */
    private FTPFile ftpFile;

    /**
     * 文件改变类型
     */
    private FileChangeType eventType;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * FTPClient
     */
    private FTPClient ftpClient;

    /**
     * 获取文件输入流
     *
     * @return InputStream
     */
    public InputStream getInputStream() {
        //如果是删除事件则不能够获取流
        if (Objects.equals(eventType, FileChangeType.FILE_DELETED)) {
            return null;
        }

        try {
            return ftpClient.retrieveFileStream(this.fileName);
        } catch (IOException e) {
            return null;
        }
    }

}

