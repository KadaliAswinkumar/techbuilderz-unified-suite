package com.vidyalaya.domain.repository;

import com.vidyalaya.domain.ChatMessage;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {
    List<ChatMessage> findTop50ByUserIdOrderByCreatedAtDesc(UUID userId);

    List<ChatMessage> findTop100ByOrderByCreatedAtDesc();
}
