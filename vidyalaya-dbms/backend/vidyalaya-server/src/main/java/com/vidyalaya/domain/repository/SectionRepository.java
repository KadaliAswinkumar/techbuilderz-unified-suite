package com.vidyalaya.domain.repository;

import com.vidyalaya.domain.Section;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SectionRepository extends JpaRepository<Section, UUID> {}
