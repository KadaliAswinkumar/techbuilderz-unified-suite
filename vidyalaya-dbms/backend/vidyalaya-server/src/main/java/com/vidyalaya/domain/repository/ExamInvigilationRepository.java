package com.vidyalaya.domain.repository;

import com.vidyalaya.domain.ExamInvigilation;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamInvigilationRepository extends JpaRepository<ExamInvigilation, UUID> {
    List<ExamInvigilation> findByTeacherId(UUID teacherId);
}
