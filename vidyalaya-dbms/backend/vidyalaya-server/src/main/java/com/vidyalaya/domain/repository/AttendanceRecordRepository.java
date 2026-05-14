package com.vidyalaya.domain.repository;

import com.vidyalaya.domain.AttendanceRecord;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, UUID> {
    List<AttendanceRecord> findByStudentIdOrderByRecordDateDesc(UUID studentId);
}
