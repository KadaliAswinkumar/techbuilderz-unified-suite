package com.vidyalaya.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record DevSwitchRoleRequest(@NotBlank String targetRole, String tenantSlug) {}
