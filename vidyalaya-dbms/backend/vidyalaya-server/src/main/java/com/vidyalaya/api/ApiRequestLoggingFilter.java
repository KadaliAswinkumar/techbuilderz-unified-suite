package com.vidyalaya.api;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * One line per request after completion: correlation id, method, path, HTTP status, duration.
 * Does not log bodies or Authorization (safe for login).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class ApiRequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiRequestLoggingFilter.class);

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri.startsWith("/error")) {
            return true;
        }
        return !(uri.startsWith("/api") || uri.startsWith("/public"));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String rid = Long.toHexString(ThreadLocalRandom.current().nextLong());
        request.setAttribute(ApiRequestLoggingFilter.class.getName() + ".rid", rid);
        MDC.put("rid", rid);
        long start = System.nanoTime();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long ms = (System.nanoTime() - start) / 1_000_000L;
            String uri = request.getRequestURI();
            String q = request.getQueryString();
            String path = q == null || q.isEmpty() ? uri : uri + "?" + q;
            int status = response.getStatus();
            try {
                if (status >= 500) {
                    log.error(
                            "API {} {} -> {} in {}ms (client={})",
                            request.getMethod(),
                            path,
                            status,
                            ms,
                            clientIp(request));
                } else if (status >= 400) {
                    log.warn(
                            "API {} {} -> {} in {}ms (client={})",
                            request.getMethod(),
                            path,
                            status,
                            ms,
                            clientIp(request));
                } else {
                    log.info(
                            "API {} {} -> {} in {}ms (client={})",
                            request.getMethod(),
                            path,
                            status,
                            ms,
                            clientIp(request));
                }
            } finally {
                MDC.remove("rid");
            }
        }
    }

    private static String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].strip();
        }
        return request.getRemoteAddr();
    }

    public static String requestId(HttpServletRequest request) {
        Object v = request.getAttribute(ApiRequestLoggingFilter.class.getName() + ".rid");
        return v == null ? "-" : v.toString();
    }
}
