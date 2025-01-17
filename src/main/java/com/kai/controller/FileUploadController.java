package com.kai.controller;

import com.kai.context.UserContext;
import com.kai.exception.ServiceException;
import com.kai.model.FileRecord;
import com.kai.repository.FileRecordRepository;
import com.kai.service.FileRecordService;
import io.minio.*;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.kai.common.R;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@RestController
@RequestMapping("/api/file")
@AllArgsConstructor
public class FileUploadController {

    private MinioClient minioClient;


    private FileRecordService fileRecordService;
    private FileRecordRepository fileRecordRepository;

    @PostMapping("/upload/{roomId}")
    public R<?> uploadFile(
            @PathVariable Long roomId,@RequestParam(required = false) String bucketName,
            @RequestParam("file") MultipartFile file
    ) {

        bucketName = StringUtils.isEmpty(bucketName) ? "default" : bucketName;
        try {
            // 确保 bucket 存在
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            // 生成文件名
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            //再加上随机数6位
            timestamp += "_"+(int) ((Math.random() * 9 + 1) * 100000);
            String fileName = timestamp + "_" + file.getOriginalFilename();

            // 上传文件到 MinIO
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            // 保存上传记录到数据库
            FileRecord fileRecord = new FileRecord();
//            fileRecord.setFile_id();
            fileRecord.setRoomId(roomId);
            fileRecord.setFileRealName(file.getOriginalFilename());
            fileRecord.setFileName(fileName);
            fileRecord.setBucketName(bucketName);
            fileRecord.setFileSize(file.getSize());
            fileRecord.setUserId(UserContext.getUserId());
            fileRecord.setCreatedAt(LocalDateTime.now());
            fileRecordService.saveFileRecord(fileRecord);

            return R.ok("File uploaded successfully!");

        } catch (Exception e) {
            e.printStackTrace();
            return R.ok("File upload failed: " + e.getMessage());
        }
    }


    // 下载文件
    @GetMapping("/download/{fileId}")
    public void downloadFile(@PathVariable String fileId, HttpServletResponse response) {
        Optional<FileRecord> byId = fileRecordRepository.findById(Long.valueOf(fileId));

        if (byId.isEmpty()) {
            throw new ServiceException("File not found");
        }
        FileRecord fileRecord = byId.get();

        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(fileRecord.getBucketName())
                        .object(fileRecord.getFileName())
                        .build())) {
            byte[] bytes = stream.readAllBytes();


            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileRecord.getFileRealName() + "\"");
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.getOutputStream().write(bytes);


        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(e.getMessage());
        }
    }

    //查看指定聊天室的文件列表
    @GetMapping("/files/{roomId}")
    public R<?> getFilesByRoomId(@PathVariable Long roomId) {
        return R.ok(fileRecordService.getFilesByRoomId(roomId));
    }


    //删除文件
    @DeleteMapping("/delete/{fileId}")
    public R<?> deleteFile(@PathVariable Long fileId) {
        try {
            fileRecordService.removeFileRecordById(fileId);
        } catch (Exception e) {
            e.printStackTrace();
            return R.ok("Error: " + e.getMessage());
        }
        return R.ok("File deleted successfully");
    }
}
