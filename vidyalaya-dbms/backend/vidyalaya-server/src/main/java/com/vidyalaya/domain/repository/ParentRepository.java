package com.vidyalaya.domain.repository;

import com.vidyalaya.domain.Parent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParentRepository extends JpaRepository<Parent, UUID> {
    long count();
}
