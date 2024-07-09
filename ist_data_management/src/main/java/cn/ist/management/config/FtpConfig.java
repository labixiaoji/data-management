package cn.ist.management.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.*;

@Data
@Slf4j
@Configuration
public class FtpConfig {


//    # 测试
//    ftp:
//    ip: ftp.dlptest.com
//    username: dlpuser
//    password: rNrKYTX9g7z3RgJRmxWuGHbeu
//    path: C:/filezillawork

//    /**
//     * FTP IP地址
//     */
//    private String ip = "ftp.dlptest.com";
//
//
//    /**
//     * FTP登录账号
//     */
//
//    private String username = "dlpuser";
//
//    /**
//     * FTP登录密码
//     */
//
//    private String password = "rNrKYTX9g7z3RgJRmxWuGHbeu";


    private FTPClient ftpClient;

    /**
     * 连接 FTP 服务
     */
    public FTPClient connect(String url) {
        ftpClient = new FTPClient();
//        String url = "ftp://dlpuser:rNrKYTX9g7z3RgJRmxWuGHbeu@ftp.dlptest.com/";

        try {

            URI uri = new URI(url);

            // 提取FTP服务器的地址和端口
            String ftpHost = uri.getHost();
            int port = 21; // 如果没有明确指定端口，则为-1

            // 设置编码
            ftpClient.setControlEncoding("UTF-8");
            // 设置连接超时时间(单位：毫秒)
            ftpClient.setConnectTimeout(60 * 1000);
            // 连接
            ftpClient.connect(ftpHost, port);

            ftpClient.login("anonymous", "");

            ftpClient.setControlKeepAliveTimeout(30);//用于设置传输控制命令的 Socket 的 alive 状态，注意单位为 s。
            ftpClient.setControlKeepAliveReplyTimeout(3000);

            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                log.error("未连接到FTP，用户名或密码错误");
                // 拒绝连接
                ftpClient.disconnect();
            } else {
                log.info("连接到FTP成功");
                // 设置二进制方式传输文件
//                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                // 设置被动工作模式
                ftpClient.enterLocalPassiveMode();
            }
            return ftpClient;
        } catch (SocketException e) {
            e.printStackTrace();
            log.error("FTP的IP地址错误");
        } catch (IOException e) {
            e.printStackTrace();
            log.error("FTP的端口错误");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return ftpClient;
    }

    /**
     * 断开 FTP 服务
     */
    public void closeConnect() {
        log.warn("关闭ftp服务器");
        try {
            if (ftpClient != null) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

