package com.kai.repository;

import com.kai.model.FileRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileRecordRepository extends JpaRepository<FileRecord, Long> {
    List<FileRecord> findAllByRoomId(Long roomId);
}
