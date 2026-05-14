package com.vidyalaya.security;

import com.vidyalaya.tenant.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TenantMvcInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthToken jwt && jwt.getTenantSlug() != null && !jwt.getTenantSlug().isBlank()) {
            TenantContext.set(jwt.getTenantSlug());
        } else {
            String slug = request.getHeader("X-Tenant-Slug");
            if (slug != null && !slug.isBlank()) {
                TenantContext.set(slug);
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TenantContext.clear();
    }
}
