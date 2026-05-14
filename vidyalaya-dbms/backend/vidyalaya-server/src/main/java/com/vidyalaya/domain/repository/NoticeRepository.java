package com.vidyalaya.domain.repository;

import com.vidyalaya.domain.Notice;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, UUID> {}
