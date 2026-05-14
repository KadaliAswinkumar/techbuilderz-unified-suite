package com.vidyalaya.domain.repository;

import com.vidyalaya.domain.ParentCommunity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParentCommunityRepository extends JpaRepository<ParentCommunity, UUID> {
    List<ParentCommunity> findByParentId(UUID parentId);
}
