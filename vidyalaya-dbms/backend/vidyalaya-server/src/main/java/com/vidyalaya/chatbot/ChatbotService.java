package com.vidyalaya.chatbot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vidyalaya.domain.ChatMessage;
import com.vidyalaya.domain.repository.AppUserRepository;
import com.vidyalaya.domain.repository.ChatMessageRepository;
import com.vidyalaya.domain.repository.FaqRepository;
import com.vidyalaya.tenant.TenantRegistry;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatbotService {

    private final FaqRepository faqRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AppUserRepository appUserRepository;
    private final TenantRegistry tenantRegistry;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();

    @Value("${OPENAI_API_KEY:}")
    private String envOpenAiKey;

    public ChatbotService(
            FaqRepository faqRepository,
            ChatMessageRepository chatMessageRepository,
            AppUserRepository appUserRepository,
            TenantRegistry tenantRegistry,
            ObjectMapper objectMapper) {
        this.faqRepository = faqRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.appUserRepository = appUserRepository;
        this.tenantRegistry = tenantRegistry;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public String ask(String tenantSlug, UUID userId, String question) {
        String faqContext =
                faqRepository.findByActiveIsTrue().stream()
                        .map(f -> "Q: " + f.getQuestion() + "\nA: " + f.getAnswer())
                        .collect(Collectors.joining("\n\n"));
        String system =
                "You are Vidyalaya school assistant. Use only the FAQ context when possible. If unsure, say you will ask the school admin.\n\nFAQ:\n"
                        + faqContext;

        String apiKey =
                tenantRegistry
                        .findBySlug(tenantSlug)
                        .flatMap(tenantRegistry::decryptOpenAiKey)
                        .filter(k -> !k.isBlank())
                        .orElse(envOpenAiKey);
        if (apiKey == null || apiKey.isBlank()) {
            return "Chatbot is not configured (missing OpenAI API key).";
        }
        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", "gpt-4o-mini");
            ArrayNode messages = body.putArray("messages");
            messages.addObject().put("role", "system").put("content", system);
            messages.addObject().put("role", "user").put("content", question);
            String json = objectMapper.writeValueAsString(body);
            HttpRequest req =
                    HttpRequest.newBuilder(URI.create("https://api.openai.com/v1/chat/completions"))
                            .timeout(Duration.ofSeconds(60))
                            .header("Authorization", "Bearer " + apiKey)
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                            .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() / 100 != 2) {
                return "Assistant temporarily unavailable.";
            }
            JsonNode root = objectMapper.readTree(resp.body());
            String answer =
                    root.path("choices").path(0).path("message").path("content").asText("").trim();
            saveMsg(userId, "user", question);
            saveMsg(userId, "assistant", answer);
            return answer;
        } catch (Exception e) {
            return "Assistant temporarily unavailable.";
        }
    }

    private void saveMsg(UUID userId, String role, String content) {
        ChatMessage m = new ChatMessage();
        if (userId != null) {
            m.setUser(appUserRepository.getReferenceById(userId));
        }
        m.setRole(role);
        m.setContent(content);
        chatMessageRepository.save(m);
    }
}
