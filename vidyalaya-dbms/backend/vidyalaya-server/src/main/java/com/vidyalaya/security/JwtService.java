package com.vidyalaya.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey key;
    private final long accessTokenMinutes;

    public JwtService(
            @Value("${vidyalaya.jwt.secret}") String secret,
            @Value("${vidyalaya.jwt.access-token-minutes:15}") long accessTokenMinutes) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 bytes");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.accessTokenMinutes = accessTokenMinutes;
    }

    public String createAccessToken(UUID userId, String tenantSlug, String role) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessTokenMinutes * 60);
        var builder =
                Jwts.builder()
                        .id(UUID.randomUUID().toString())
                        .issuedAt(Date.from(now))
                        .expiration(Date.from(exp))
                        .claim("role", role);
        if (userId != null) {
            builder.subject(userId.toString());
        } else {
            builder.subject("SUPER_ADMIN");
        }
        if (tenantSlug != null && !tenantSlug.isBlank()) {
            builder.claim("tid", tenantSlug);
        }
        return builder.signWith(key).compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public String role(Claims c) {
        return c.get("role", String.class);
    }

    public String tenantSlug(Claims c) {
        return c.get("tid", String.class);
    }

    public UUID userId(Claims c) {
        String sub = c.getSubject();
        if (sub == null || "SUPER_ADMIN".equals(sub)) {
            return null;
        }
        return UUID.fromString(sub);
    }
}
