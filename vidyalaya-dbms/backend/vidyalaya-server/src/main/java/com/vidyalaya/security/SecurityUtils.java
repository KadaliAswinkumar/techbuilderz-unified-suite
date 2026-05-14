package com.vidyalaya.security;

import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static JwtAuthToken requireAuth() {
        var a = SecurityContextHolder.getContext().getAuthentication();
        if (!(a instanceof JwtAuthToken jwt)) {
            throw new org.springframework.security.access.AccessDeniedException("Not authenticated");
        }
        return jwt;
    }

    public static UUID currentUserId() {
        return requireAuth().getUserId();
    }

    public static String currentRole() {
        return requireAuth().getRole();
    }

    /** School-wide admin in a tenant, including dev super-admin acting via {@code X-Tenant-Slug}. */
    public static boolean isTenantAdminRole(String role) {
        return "ADMIN".equals(role) || "SUPER_ADMIN".equals(role);
    }

    public static void requireTenantAdmin() {
        if (!isTenantAdminRole(requireAuth().getRole())) {
            throw new org.springframework.security.access.AccessDeniedException("Admin only");
        }
    }
}
