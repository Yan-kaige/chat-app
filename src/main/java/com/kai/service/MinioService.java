package com.kai.service;

import io.minio.*;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import io.minio.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.UUID;

@Service
public class MinioService {

    @Autowired
    private MinioClient minioClient;

    /**
     * 从 MultipartFile 上传文件
     */
    public String upload(MultipartFile file, String bucketName) throws Exception {
        ensureBucketExists(bucketName);

        String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );

        return generatePresignedUrl(bucketName, fileName);
    }

    /**
     * 从字节数组上传文件
     */
    public String upload(byte[] data, String bucketName, String fileName, String contentType) throws Exception {
        ensureBucketExists(bucketName);

        String uniqueFileName = UUID.randomUUID() + "-" + fileName;

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(uniqueFileName)
                        .stream(new ByteArrayInputStream(data), data.length, -1)
                        .contentType(contentType)
                        .build()
        );

        return generatePresignedUrl(bucketName, uniqueFileName);
    }

    /**
     * 确保 Bucket 存在
     */
    private void ensureBucketExists(String bucketName) throws Exception {
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }

    /**
     * 生成预签名 URL
     */
    private String generatePresignedUrl(String bucketName, String fileName) throws Exception {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .expiry(60 * 60) // 1 hour expiry
                        .method(Method.GET)
                        .build()
        );
    }
}
