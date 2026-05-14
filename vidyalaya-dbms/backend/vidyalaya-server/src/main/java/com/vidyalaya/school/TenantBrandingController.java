package com.vidyalaya.school;

import com.vidyalaya.tenant.TenantContext;
import com.vidyalaya.tenant.TenantRecord;
import com.vidyalaya.tenant.TenantRegistry;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenant")
public class TenantBrandingController {

    private final TenantRegistry tenantRegistry;

    public TenantBrandingController(TenantRegistry tenantRegistry) {
        this.tenantRegistry = tenantRegistry;
    }

    @GetMapping("/branding")
    @PreAuthorize("isAuthenticated()")
    public BrandingResponse branding() {
        TenantRecord t =
                tenantRegistry.findBySlug(TenantContext.require()).orElseThrow();
        return new BrandingResponse(t.slug(), t.name(), t.primaryColor(), t.logoUrl());
    }

    @PutMapping("/branding")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public BrandingResponse update(@RequestBody BrandingUpdate req) {
        String slug = TenantContext.require();
        tenantRegistry.updateBranding(slug, req.primaryColor(), req.logoUrl());
        TenantRecord t = tenantRegistry.findBySlug(slug).orElseThrow();
        return new BrandingResponse(t.slug(), t.name(), t.primaryColor(), t.logoUrl());
    }

    @PutMapping("/openai-key")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public void setOpenAi(@RequestBody OpenAiKeyReq req) {
        tenantRegistry.updateOpenAiKey(TenantContext.require(), req.apiKey());
    }

    public record BrandingResponse(String slug, String name, String primaryColor, String logoUrl) {}

    public record BrandingUpdate(String primaryColor, String logoUrl) {}

    public record OpenAiKeyReq(@NotBlank String apiKey) {}
}
