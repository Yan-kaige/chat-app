package com.kai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "file_record")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "file_real_name", nullable = false)
    private String fileRealName;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "bucket_name", nullable = false)
    private String bucketName;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt=LocalDateTime.now();
}
