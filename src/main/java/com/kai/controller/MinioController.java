package com.kai.controller;

import com.kai.exception.ServiceException;
import com.kai.service.FileRecordService;
import io.minio.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.kai.common.R;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;

@RestController
@RequestMapping("/minio")
public class MinioController {

    @Autowired
    private MinioClient minioClient;
    @Autowired
    private FileRecordService fileRecordService;


    // 创建存储桶
    @PostMapping("/createBucket/{bucketName}")
    public String createBucket(@PathVariable String bucketName) {
        try {
            boolean isExist = minioClient.bucketExists( BucketExistsArgs.builder().bucket(bucketName).build());
            if (isExist) {
                return "Bucket already exists!";
            }

            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            return "Bucket created successfully!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    // 上传文件
    @PostMapping("/upload/{bucketName}")
    public String uploadFile(@RequestParam("file") MultipartFile file,@PathVariable String bucketName) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(file.getOriginalFilename())
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            return "File uploaded successfully: " + file.getOriginalFilename();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    // 下载文件
    @GetMapping("/download/{fileName}/{bucketName}")
    public void downloadFile(@PathVariable String fileName, @PathVariable String bucketName, HttpServletResponse response) {
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .build())) {
            byte[] bytes = stream.readAllBytes();

            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.getOutputStream().write(bytes);

        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException("Error: " + e.getMessage());
        }
    }

    //删除文件
    @DeleteMapping("/delete/{fileId}")
    public R<?> deleteFile(@PathVariable Long id) {
        try {
            fileRecordService.removeFileRecordById(id);
        } catch (Exception e) {
            e.printStackTrace();
            return R.ok("Error: " + e.getMessage());
        }
        return R.ok("File deleted successfully");
    }
}
