package com.vidyalaya.config;

import com.vidyalaya.security.TenantMvcInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final TenantMvcInterceptor tenantMvcInterceptor;

    public WebMvcConfig(TenantMvcInterceptor tenantMvcInterceptor) {
        this.tenantMvcInterceptor = tenantMvcInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tenantMvcInterceptor).addPathPatterns("/api/**");
    }
}
