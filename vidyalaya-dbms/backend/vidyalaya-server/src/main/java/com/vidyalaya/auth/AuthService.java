package com.vidyalaya.auth;

import com.vidyalaya.auth.dto.ChangePasswordRequest;
import com.vidyalaya.auth.dto.ForgotPasswordRequest;
import com.vidyalaya.auth.dto.LoginRequest;
import com.vidyalaya.auth.dto.MeResponse;
import com.vidyalaya.auth.dto.RefreshRequest;
import com.vidyalaya.auth.dto.RegisterTenantRequest;
import com.vidyalaya.auth.dto.TokenResponse;
import com.vidyalaya.auth.dto.AdminResetPasswordRequest;
import com.vidyalaya.domain.AppUser;
import com.vidyalaya.domain.RefreshToken;
import com.vidyalaya.domain.repository.AppUserRepository;
import com.vidyalaya.domain.repository.RefreshTokenRepository;
import com.vidyalaya.security.JwtAuthToken;
import com.vidyalaya.security.JwtService;
import com.vidyalaya.tenant.TenantContext;
import com.vidyalaya.tenant.TenantProvisioningService;
import com.vidyalaya.tenant.TenantRecord;
import com.vidyalaya.tenant.TenantRegistry;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final AppUserRepository appUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TenantRegistry tenantRegistry;
    private final TenantProvisioningService tenantProvisioningService;
    private final JdbcTemplate masterJdbc;
    private final String superUsername;
    private final String superPassword;
    private final long refreshDays;
    private final long accessTokenSeconds;
    private final boolean openApiDev;

    public AuthService(
            AppUserRepository appUserRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            TenantRegistry tenantRegistry,
            TenantProvisioningService tenantProvisioningService,
            @Qualifier("masterJdbcTemplate") JdbcTemplate masterJdbc,
            @Value("${vidyalaya.super-admin.username}") String superUsername,
            @Value("${vidyalaya.super-admin.password}") String superPassword,
            @Value("${vidyalaya.jwt.refresh-token-days:7}") long refreshDays,
            @Value("${vidyalaya.jwt.access-token-minutes:15}") long accessTokenMinutes,
            @Value("${vidyalaya.dev.open-api:false}") boolean openApiDev) {
        this.appUserRepository = appUserRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.tenantRegistry = tenantRegistry;
        this.tenantProvisioningService = tenantProvisioningService;
        this.masterJdbc = masterJdbc;
        this.superUsername = superUsername == null ? "" : superUsername.strip();
        String sp = superPassword == null ? "" : superPassword.strip();
        if (sp.isEmpty()) {
            throw new IllegalStateException(
                    "Super-admin password is blank. Set vidyalaya.super-admin.password or SUPER_ADMIN_PASSWORD.");
        }
        this.superPassword = sp;
        this.refreshDays = refreshDays;
        this.accessTokenSeconds = accessTokenMinutes * 60;
        this.openApiDev = openApiDev;
    }

    public TokenResponse login(LoginRequest req) {
        String tenantRaw = req.tenantSlug() == null ? "" : req.tenantSlug().trim();
        boolean blankTenant = tenantRaw.isEmpty();
        String user = req.username() == null ? "" : req.username().trim();
        String pass = req.password() == null ? "" : req.password();

        if (blankTenant && superUsername.equalsIgnoreCase(user)) {
            if (!superPassword.equals(pass.strip())) {
                throw new BadCredentialsException("Invalid super-admin password");
            }
            return issueSuperTokens();
        }

        String slug = blankTenant ? TenantContext.get() : tenantRaw;
        if (slug == null || slug.isBlank()) {
            throw new BadCredentialsException(
                    "School tenant slug is required (use the field above, e.g. stmarys)");
        }
        TenantContext.set(slug);
        try {
            if (tenantRegistry.findBySlug(slug).isEmpty()) {
                throw new BadCredentialsException("Unknown tenant");
            }
            AppUser appUser =
                    appUserRepository
                            .findByUsernameIgnoreCase(user)
                            .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
            if (!appUser.isActive() || !passwordEncoder.matches(pass, appUser.getPasswordHash())) {
                throw new BadCredentialsException("Invalid credentials");
            }
            return issueTenantTokens(appUser, slug);
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * Local-only when {@code vidyalaya.dev.open-api=true} (Spring profile {@code dev}); exposes {@code GET
     * /api/auth/dev-token}. Never enable in production.
     */
    public TokenResponse devBootstrapSuperAdmin() {
        if (!openApiDev) {
            throw new AccessDeniedException("Dev token endpoint is disabled.");
        }
        log.warn("DEV open-api: issuing super-admin tokens without password (vidyalaya.dev.open-api=true)");
        return issueSuperTokens();
    }

    public TokenResponse devSwitchRole(String targetRoleRaw, String tenantSlugRaw) {
        if (!openApiDev) {
            throw new AccessDeniedException("Role switch is enabled only in dev open-api mode.");
        }
        String targetRole = normalizeRole(targetRoleRaw);
        if ("SUPER_ADMIN".equals(targetRole)) {
            return issueSuperTokens();
        }
        String slug = tenantSlugRaw == null ? "" : tenantSlugRaw.trim();
        if (slug.isBlank()) {
            throw new IllegalArgumentException("Tenant slug is required for tenant roles");
        }
        if (tenantRegistry.findBySlug(slug).isEmpty()) {
            throw new IllegalArgumentException("Unknown tenant");
        }
        TenantContext.set(slug);
        try {
            java.util.Optional<AppUser> user = findDevSwitchUser(targetRole);
            if (user.isPresent()) {
                return issueTenantTokens(user.get(), slug);
            }
            log.warn(
                    "DEV role switch fallback: no active user found for role {} in tenant {}, issuing role token without user binding",
                    targetRole,
                    slug);
            return issueDevTenantRoleToken(targetRole, slug);
        } finally {
            TenantContext.clear();
        }
    }

    private java.util.Optional<AppUser> findDevSwitchUser(String role) {
        if ("USER".equals(role)) {
            java.util.Optional<AppUser> userLike =
                    appUserRepository.findFirstByRoleInAndActiveTrueOrderByCreatedAtAsc(
                            List.of("ROLE_TEACHER", "ROLE_PARENT", "ROLE_STUDENT"));
            if (userLike.isPresent()) {
                return userLike;
            }
            return appUserRepository.findFirstByRoleAndActiveTrueOrderByCreatedAtAsc("ROLE_ADMIN");
        }
        return appUserRepository.findFirstByRoleAndActiveTrueOrderByCreatedAtAsc(role);
    }

    private static String normalizeRole(String raw) {
        String role = raw == null ? "" : raw.trim().toUpperCase(Locale.ROOT);
        return switch (role) {
            case "SUPER_ADMIN" -> "SUPER_ADMIN";
            case "ADMIN", "ROLE_ADMIN" -> "ROLE_ADMIN";
            case "TEACHER", "ROLE_TEACHER" -> "ROLE_TEACHER";
            case "PARENT", "ROLE_PARENT" -> "ROLE_PARENT";
            case "STUDENT", "ROLE_STUDENT" -> "ROLE_STUDENT";
            case "USER" -> "USER";
            default -> throw new IllegalArgumentException("Unsupported role: " + raw);
        };
    }

    private TokenResponse issueDevTenantRoleToken(String role, String slug) {
        String access = jwtService.createAccessToken(null, slug, role);
        // Dev utility token only: no refresh persistence for synthetic role sessions.
        return new TokenResponse(access, "", role, accessTokenSeconds, slug);
    }

    private TokenResponse issueSuperTokens() {
        String access = jwtService.createAccessToken(null, null, "SUPER_ADMIN");
        String rawRefresh = UUID.randomUUID().toString();
        String hash = TokenHasher.sha256(rawRefresh);
        UUID id = UUID.randomUUID();
        UUID family = UUID.randomUUID();
        Instant exp = Instant.now().plus(refreshDays, ChronoUnit.DAYS);
        masterJdbc.update(
                """
                INSERT INTO super_refresh_tokens (id, token_hash, expires_at, family_id, revoked)
                VALUES (?,?,?,?,false)
                """,
                id,
                hash,
                Timestamp.from(exp),
                family);
        return new TokenResponse(access, rawRefresh, "SUPER_ADMIN", accessTokenSeconds, null);
    }

    private TokenResponse issueTenantTokens(AppUser user, String slug) {
        String access = jwtService.createAccessToken(user.getId(), slug, user.getRole());
        String rawRefresh = UUID.randomUUID().toString();
        String hash = TokenHasher.sha256(rawRefresh);
        RefreshToken rt = new RefreshToken();
        rt.setTokenHash(hash);
        rt.setUser(user);
        rt.setExpiresAt(Instant.now().plus(refreshDays, ChronoUnit.DAYS));
        rt.setFamilyId(UUID.randomUUID());
        rt.setRevoked(false);
        refreshTokenRepository.save(rt);
        return new TokenResponse(access, rawRefresh, user.getRole(), accessTokenSeconds, slug);
    }

    @Transactional
    public TokenResponse refresh(RefreshRequest req) {
        String hash = TokenHasher.sha256(req.refreshToken());
        String tenantSlug = TenantContext.get();
        if (tenantSlug == null || tenantSlug.isBlank()) {
            return refreshSuper(hash);
        }
        if (tenantRegistry.findBySlug(tenantSlug).isEmpty()) {
            throw new BadCredentialsException("Unknown tenant");
        }
        RefreshToken existing =
                refreshTokenRepository
                        .findByTokenHashAndRevokedIsFalse(hash)
                        .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));
        if (existing.getExpiresAt().isBefore(Instant.now())) {
            throw new BadCredentialsException("Expired refresh token");
        }
        existing.setRevoked(true);
        refreshTokenRepository.save(existing);
        AppUser user = existing.getUser();
        return issueTenantTokens(user, tenantSlug);
    }

    private TokenResponse refreshSuper(String hash) {
        List<UUID> ids =
                masterJdbc.query(
                        """
                        SELECT id FROM super_refresh_tokens
                        WHERE token_hash = ? AND revoked = false AND expires_at > now()
                        """,
                        (rs, i) -> rs.getObject("id", UUID.class),
                        hash);
        if (ids.isEmpty()) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        masterJdbc.update("UPDATE super_refresh_tokens SET revoked = true WHERE id = ?", ids.get(0));
        return issueSuperTokens();
    }

    @Transactional
    public void logout(RefreshRequest req) {
        String hash = TokenHasher.sha256(req.refreshToken());
        String tenantSlug = TenantContext.get();
        if (tenantSlug == null || tenantSlug.isBlank()) {
            masterJdbc.update("UPDATE super_refresh_tokens SET revoked = true WHERE token_hash = ?", hash);
            return;
        }
        refreshTokenRepository
                .findByTokenHashAndRevokedIsFalse(hash)
                .ifPresent(
                        rt -> {
                            rt.setRevoked(true);
                            refreshTokenRepository.save(rt);
                        });
    }

    public MeResponse me() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof JwtAuthToken jwt)) {
            throw new IllegalStateException("Not authenticated");
        }
        if ("SUPER_ADMIN".equals(jwt.getRole())) {
            return new MeResponse(null, superUsername, "SUPER_ADMIN", null);
        }
        if (jwt.getUserId() == null && openApiDev) {
            return new MeResponse(null, "dev-role-user", jwt.getRole(), jwt.getTenantSlug());
        }
        UUID userId =
                java.util.Optional.ofNullable(jwt.getUserId())
                        .orElseThrow(() -> new IllegalStateException("Invalid token"));
        String username =
                appUserRepository
                        .findById(userId)
                        .map(AppUser::getUsername)
                        .orElseThrow(() -> new IllegalStateException("User not found"));
        return new MeResponse(userId, username, jwt.getRole(), jwt.getTenantSlug());
    }

    @Transactional
    public void changePassword(ChangePasswordRequest req) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof JwtAuthToken jwt) || jwt.getUserId() == null) {
            throw new AccessDeniedException("Not allowed");
        }
        UUID userId =
                java.util.Optional.ofNullable(jwt.getUserId())
                        .orElseThrow(() -> new AccessDeniedException("Not allowed"));
        AppUser user =
                appUserRepository
                        .findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!passwordEncoder.matches(req.currentPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Current password incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        appUserRepository.save(user);
    }

    public TenantRecord registerTenant(RegisterTenantRequest req) {
        return tenantProvisioningService.provision(
                req.slug(), req.schoolName(), req.adminUsername(), req.adminPassword());
    }

    public void forgotPassword(ForgotPasswordRequest req) {
        String slug = req.tenantSlug().trim();
        if (tenantRegistry.findBySlug(slug).isEmpty()) {
            return;
        }
        TenantContext.set(slug);
        try {
            appUserRepository.findByUsernameIgnoreCase(req.usernameOrEmail().trim());
            log.info("Password reset requested for tenant={} identifier={}", slug, req.usernameOrEmail().trim());
        } finally {
            TenantContext.clear();
        }
    }

    @Transactional
    public void adminResetPassword(AdminResetPasswordRequest req) {
        String slug = req.tenantSlug().trim();
        if (tenantRegistry.findBySlug(slug).isEmpty()) {
            throw new IllegalArgumentException("Unknown tenant");
        }
        TenantContext.set(slug);
        try {
            AppUser user =
                    appUserRepository
                            .findByUsernameIgnoreCase(req.username().trim())
                            .orElseThrow(() -> new IllegalArgumentException("User not found"));
            user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
            appUserRepository.save(user);
        } finally {
            TenantContext.clear();
        }
    }
}
