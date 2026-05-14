package com.vidyalaya.domain.repository;

import com.vidyalaya.domain.Exam;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamRepository extends JpaRepository<Exam, UUID> {}
