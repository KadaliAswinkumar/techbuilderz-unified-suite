package com.vidyalaya.domain.repository;

import com.vidyalaya.domain.Faq;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FaqRepository extends JpaRepository<Faq, UUID> {
    List<Faq> findByActiveIsTrue();
}
