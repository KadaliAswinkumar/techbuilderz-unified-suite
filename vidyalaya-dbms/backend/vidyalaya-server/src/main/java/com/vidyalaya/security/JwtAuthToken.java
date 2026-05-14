package com.vidyalaya.security;

import java.util.Collection;
import java.util.UUID;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class JwtAuthToken extends AbstractAuthenticationToken {

    private final UUID userId;
    private final String tenantSlug;
    private final String role;

    public JwtAuthToken(
            UUID userId,
            String tenantSlug,
            String role,
            Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.userId = userId;
        this.tenantSlug = tenantSlug;
        this.role = role;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return userId != null ? userId.toString() : "SUPER_ADMIN";
    }

    public UUID getUserId() {
        return userId;
    }

    public String getTenantSlug() {
        return tenantSlug;
    }

    public String getRole() {
        return role;
    }
}
