package com.vidyalaya.auth;

import com.vidyalaya.auth.dto.DevSwitchRoleRequest;
import com.vidyalaya.auth.dto.TokenResponse;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@ConditionalOnProperty(prefix = "vidyalaya.dev", name = "open-api", havingValue = "true")
public class DevOpenApiAuthController {

    private final AuthService authService;

    public DevOpenApiAuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/dev-token")
    public TokenResponse devToken() {
        return authService.devBootstrapSuperAdmin();
    }

    @PostMapping("/dev-switch-role")
    public TokenResponse devSwitchRole(@Valid @RequestBody DevSwitchRoleRequest req) {
        return authService.devSwitchRole(req.targetRole(), req.tenantSlug());
    }
}
