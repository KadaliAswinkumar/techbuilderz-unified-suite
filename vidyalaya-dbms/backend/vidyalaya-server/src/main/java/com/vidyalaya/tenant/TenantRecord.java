package com.vidyalaya.tenant;

import java.time.Instant;
import java.util.UUID;

public record TenantRecord(
        UUID id,
        String slug,
        String name,
        String dbName,
        String dbHost,
        int dbPort,
        String dbUsername,
        String dbPasswordEnc,
        String primaryColor,
        String logoUrl,
        String openaiApiKeyEnc,
        Instant createdAt) {}
