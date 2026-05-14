package com.vidyalaya.config;

import com.vidyalaya.security.JwtAuthenticationFilter;
import com.vidyalaya.security.LoginRateLimitFilter;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final LoginRateLimitFilter loginRateLimitFilter;

    @Value(
            "${vidyalaya.cors.allowed-origins:http://localhost:5173,http://127.0.0.1:5173,http://[::1]:5173}")
    private String allowedOrigins;

    @Value("${spring.profiles.active:}")
    private String activeProfiles;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter, LoginRateLimitFilter loginRateLimitFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.loginRateLimitFilter = loginRateLimitFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(c -> c.configurationSource(corsConfigurationSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers(HttpMethod.OPTIONS, "/**")
                                        .permitAll()
                                        .requestMatchers(
                                                "/api/auth/login",
                                                "/api/auth/refresh",
                                                "/api/auth/forgot-password",
                                                "/swagger-ui/**",
                                                "/swagger-ui.html",
                                                "/api-docs/**",
                                                "/v3/api-docs/**",
                                                "/public/**")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.POST, "/api/auth/reset-password")
                                        .hasAuthority("ROLE_SUPER_ADMIN")
                                        .requestMatchers(HttpMethod.GET, "/api/faqs/active")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.POST, "/api/auth/register-tenant")
                                        .hasAuthority("ROLE_SUPER_ADMIN")
                                        .requestMatchers("/api/**")
                                        .authenticated()
                                        .anyRequest()
                                        .permitAll())
                .addFilterBefore(loginRateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        for (String o : allowedOrigins.split(",")) {
            String t = o.trim();
            if (!t.isEmpty()) {
                cfg.addAllowedOriginPattern(t);
            }
        }
        // Extra patterns for local UI ports (avoid "Invalid CORS request" when using preview or non-5173 ports).
        if (localDevCorsRelax()) {
            cfg.addAllowedOriginPattern("http://localhost:*");
            cfg.addAllowedOriginPattern("http://127.0.0.1:*");
        }
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setExposedHeaders(List.of("Authorization"));
        // JWT is in headers / localStorage — cookies not required; false avoids strict Origin+credential pairing issues.
        cfg.setAllowCredentials(false);
        cfg.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    /** True when `dev` or `api-debug` profile is active (comma-separated list safe). */
    private boolean localDevCorsRelax() {
        return profileActive(activeProfiles, "dev") || profileActive(activeProfiles, "api-debug");
    }

    private static boolean profileActive(String csv, String name) {
        if (csv == null || csv.isBlank()) {
            return false;
        }
        for (String p : csv.split(",")) {
            if (name.equalsIgnoreCase(p.trim())) {
                return true;
            }
        }
        return false;
    }
}
