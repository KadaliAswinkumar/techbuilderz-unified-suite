package com.vidyalaya.domain.repository;

import com.vidyalaya.domain.Teacher;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherRepository extends JpaRepository<Teacher, UUID> {
    long count();
}
