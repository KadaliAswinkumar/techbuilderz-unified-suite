package com.vidyalaya.domain.repository;

import com.vidyalaya.domain.ExamResult;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamResultRepository extends JpaRepository<ExamResult, UUID> {
    List<ExamResult> findByStudentIdOrderByCreatedAtDesc(UUID studentId);

    List<ExamResult> findByExamId(UUID examId);
}
