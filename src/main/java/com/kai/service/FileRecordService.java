package com.kai.service;


import com.kai.model.FileRecord;
import com.kai.repository.FileRecordRepository;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.errors.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class FileRecordService {


    private FileRecordRepository  fileRecordRepository;


    private MinioClient minioClient;


    public void saveFileRecord(FileRecord fileRecord){
        fileRecordRepository.save(fileRecord);
    }

    public List<FileRecord> getFilesByRoomId(Long roomId) {
        return fileRecordRepository.findAllByRoomId(roomId);
    }

    public void removeFileRecordById(Long id) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        Optional<FileRecord> byId = fileRecordRepository.findById(id);

        if (byId.isEmpty()) {
            return;
        }

        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(byId.get().getBucketName())
                        .object(byId.get().getFileName())
                        .build()
        );

        fileRecordRepository.deleteById(id);
    }
}
