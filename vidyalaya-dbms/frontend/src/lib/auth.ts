const ACCESS = "vidyalaya_access";
const REFRESH = "vidyalaya_refresh";
const TENANT = "vidyalaya_tenant";

/** Seeded demo tenant in DevDemoDataService / DemoDataRunner when API runs with profile `dev`. */
export const DEFAULT_DEV_TENANT_SLUG = "demo";

/**
 * Tenant slug sent on `X-Tenant-Slug`. Super-admin JWTs often omit tenant; in local dev we default to {@link DEFAULT_DEV_TENANT_SLUG}
 * so dashboard and finance APIs resolve the correct tenant database.
 */
export function effectiveTenantSlug(): string {
  const raw = (getTenantSlug() ?? "").trim();
  if (raw) {
    return raw;
  }
  if (import.meta.env.VITE_OPEN_API_DEV === "true") {
    return DEFAULT_DEV_TENANT_SLUG;
  }
  return "";
}

/**
 * Persist default demo tenant when user has a token but no slug (common after super-admin login with empty tenant field).
 */
export function ensureDevTenantSlug(): void {
  if (import.meta.env.VITE_OPEN_API_DEV !== "true") {
    return;
  }
  const access = getAccessToken();
  if (!access || (getTenantSlug() ?? "").trim()) {
    return;
  }
  setTokens(access, getRefreshToken() ?? "", DEFAULT_DEV_TENANT_SLUG);
}

export function setTokens(access: string, refresh: string, tenantSlug: string) {
  localStorage.setItem(ACCESS, access);
  localStorage.setItem(REFRESH, refresh);
  localStorage.setItem(TENANT, tenantSlug);
}

export function clearTokens() {
  localStorage.removeItem(ACCESS);
  localStorage.removeItem(REFRESH);
  localStorage.removeItem(TENANT);
}

export function getAccessToken() {
  return localStorage.getItem(ACCESS);
}

export function getRefreshToken() {
  return localStorage.getItem(REFRESH);
}

export function getTenantSlug() {
  return localStorage.getItem(TENANT) ?? "";
}
