package com.vidyalaya.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminResetPasswordRequest(
        @NotBlank String tenantSlug, @NotBlank String username, @NotBlank String newPassword) {}
