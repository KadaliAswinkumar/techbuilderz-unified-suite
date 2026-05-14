package com.vidyalaya.domain.repository;

import com.vidyalaya.domain.PublicPage;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicPageRepository extends JpaRepository<PublicPage, UUID> {
    Optional<PublicPage> findByPageKey(String pageKey);
}
