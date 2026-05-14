package com.vidyalaya.auth.dto;

public record TokenResponse(
        String token,
        String refreshToken,
        String role,
        long expiresIn,
        String tenantSlug) {}
