package com.vidyalaya.domain.repository;

import com.vidyalaya.domain.SchoolClass;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolClassRepository extends JpaRepository<SchoolClass, UUID> {}
