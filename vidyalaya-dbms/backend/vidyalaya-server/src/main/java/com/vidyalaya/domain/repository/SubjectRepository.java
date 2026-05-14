package com.vidyalaya.domain.repository;

import com.vidyalaya.domain.Subject;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubjectRepository extends JpaRepository<Subject, UUID> {}
