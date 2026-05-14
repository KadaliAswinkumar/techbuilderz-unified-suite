package com.vidyalaya.domain.repository;

import com.vidyalaya.domain.Extracurricular;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExtracurricularRepository extends JpaRepository<Extracurricular, UUID> {
    List<Extracurricular> findByStudentId(UUID studentId);
}
