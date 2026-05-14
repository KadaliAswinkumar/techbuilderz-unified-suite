package com.vidyalaya.auth.dto;

import java.util.UUID;

public record MeResponse(UUID userId, String username, String role, String tenantSlug) {}
