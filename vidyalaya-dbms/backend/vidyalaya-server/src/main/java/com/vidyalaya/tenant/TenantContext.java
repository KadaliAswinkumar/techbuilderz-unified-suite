package com.vidyalaya.tenant;

public final class TenantContext {

    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    private TenantContext() {}

    public static void set(String tenantSlug) {
        CURRENT.set(tenantSlug);
    }

    public static String get() {
        return CURRENT.get();
    }

    public static String require() {
        String s = CURRENT.get();
        if (s == null || s.isBlank()) {
            throw new IllegalStateException("Tenant context not set");
        }
        return s;
    }

    public static void clear() {
        CURRENT.remove();
    }
}
