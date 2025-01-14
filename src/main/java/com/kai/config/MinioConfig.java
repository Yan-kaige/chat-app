package com.kai.config;
import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint("http://localhost:9000") // MinIO服务地址
                .credentials("admin", "admin123") // MinIO用户名和密码
                .build();
    }
}
