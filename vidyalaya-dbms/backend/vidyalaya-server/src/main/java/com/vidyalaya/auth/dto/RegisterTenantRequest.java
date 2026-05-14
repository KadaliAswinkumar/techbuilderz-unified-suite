package com.vidyalaya.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterTenantRequest(
        @NotBlank String slug,
        @NotBlank String schoolName,
        @NotBlank String adminUsername,
        @NotBlank String adminPassword) {}
