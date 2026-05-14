package com.vidyalaya.chatbot;

import com.vidyalaya.domain.ChatMessage;
import com.vidyalaya.domain.repository.ChatMessageRepository;
import com.vidyalaya.security.JwtAuthToken;
import com.vidyalaya.security.SecurityUtils;
import com.vidyalaya.tenant.TenantContext;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private final ChatbotService chatbotService;
    private final ChatMessageRepository chatMessageRepository;

    public ChatbotController(ChatbotService chatbotService, ChatMessageRepository chatMessageRepository) {
        this.chatbotService = chatbotService;
        this.chatMessageRepository = chatMessageRepository;
    }

    @PostMapping("/ask")
    @PreAuthorize("isAuthenticated()")
    public Map<String, String> ask(@RequestBody AskReq req) {
        JwtAuthToken jwt = SecurityUtils.requireAuth();
        String tenantSlug = jwt.getTenantSlug();
        if (tenantSlug == null || tenantSlug.isBlank()) {
            tenantSlug = TenantContext.get();
        }
        if (tenantSlug == null || tenantSlug.isBlank()) {
            throw new IllegalArgumentException(
                    "Tenant required (set X-Tenant-Slug, e.g. demo, when using a super-admin token)");
        }
        String answer = chatbotService.ask(tenantSlug, jwt.getUserId(), req.question());
        return Map.of("answer", answer);
    }

    @GetMapping("/history")
    @PreAuthorize("isAuthenticated()")
    public List<ChatMessage> history() {
        UUID uid = SecurityUtils.requireAuth().getUserId();
        if (uid == null) {
            return List.of();
        }
        return chatMessageRepository.findTop50ByUserIdOrderByCreatedAtDesc(uid);
    }

    public record AskReq(@NotBlank String question) {}
}
