package com.vidyalaya.auth;

import com.vidyalaya.auth.dto.ChangePasswordRequest;
import com.vidyalaya.auth.dto.ForgotPasswordRequest;
import com.vidyalaya.auth.dto.LoginRequest;
import com.vidyalaya.auth.dto.MeResponse;
import com.vidyalaya.auth.dto.RefreshRequest;
import com.vidyalaya.auth.dto.RegisterTenantRequest;
import com.vidyalaya.auth.dto.TokenResponse;
import com.vidyalaya.auth.dto.AdminResetPasswordRequest;
import com.vidyalaya.tenant.TenantRecord;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest req) {
        return authService.refresh(req);
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest req) {
        authService.logout(req);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public MeResponse me() {
        return authService.me();
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        authService.changePassword(req);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgot(@Valid @RequestBody ForgotPasswordRequest body) {
        authService.forgotPassword(body);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(Map.of("message", "If the account exists, reset instructions will be sent."));
    }

    @PostMapping("/reset-password")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<Map<String, String>> reset(
            @Valid @RequestBody AdminResetPasswordRequest body) {
        authService.adminResetPassword(body);
        return ResponseEntity.ok(Map.of("message", "Password reset completed."));
    }

    @PostMapping("/register-tenant")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public TenantRecord registerTenant(@Valid @RequestBody RegisterTenantRequest req) {
        return authService.registerTenant(req);
    }
}
