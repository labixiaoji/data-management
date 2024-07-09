//package cn.ist.management.controller;
//
//import cn.ist.management.util.FtpUtil;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.List;
//
//// ftp工具类测试代码，后续可能有用
//@RestController
//@RequestMapping("/ftp")
//@RequiredArgsConstructor
//public class FtpController {
//
//    private final FtpUtil ftpUtil;
//
//    private static final String ROOT_DIR = "./";
//
////    @GetMapping("/getFileNameList")
////    public List<String> getFileNameList() {
////        return ftpUtil.getFileNameList(ROOT_DIR);
////    }
////
////    @PostMapping("/upload")
////    public boolean uploadToFtpServer(MultipartFile file) throws IOException {
////        // 读取文件信息
////        String fileName = file.getOriginalFilename();
////        InputStream fileInputStream = file.getInputStream();
////        // 上传文件到 Ftp 服务
////        return ftpUtil.upload(ROOT_DIR, fileName, fileInputStream);
////    }
//
//    @GetMapping("/download")
//    public boolean download(){
//        String localPath = "C:/Users/A/Desktop/243.jpg", ftpFileName = "330.png";
//        return ftpUtil.download(ROOT_DIR, ftpFileName, localPath);
//    }
//
//    @GetMapping("/downloadAll")
//    public boolean downloadAll(){
//        String localPath = "C:\\Downloads\\testDownload";
//        return ftpUtil.downloadDirectory(ROOT_DIR, localPath);
//    }
//
//    @GetMapping("/delete")
//    public boolean delete(String fileName){
//        return ftpUtil.delete(ROOT_DIR, fileName);
//    }
//}
