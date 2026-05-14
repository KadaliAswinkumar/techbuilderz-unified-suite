package com.vidyalaya.domain.repository;

import com.vidyalaya.domain.FeeStructure;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeeStructureRepository extends JpaRepository<FeeStructure, UUID> {}
