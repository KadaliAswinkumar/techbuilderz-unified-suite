package com.vidyalaya.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(@NotBlank String tenantSlug, @NotBlank String usernameOrEmail) {}
