package com.vidyalaya.domain.repository;

import com.vidyalaya.domain.TeacherAssignment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherAssignmentRepository extends JpaRepository<TeacherAssignment, UUID> {
    List<TeacherAssignment> findByTeacherId(UUID teacherId);
}
