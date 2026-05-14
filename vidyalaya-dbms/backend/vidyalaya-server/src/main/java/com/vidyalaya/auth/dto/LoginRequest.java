package com.vidyalaya.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(String tenantSlug, @NotBlank String username, @NotBlank String password) {}
