import axios from "axios";
import {
  clearTokens,
  effectiveTenantSlug,
  getAccessToken,
  getRefreshToken,
  getTenantSlug,
  setTokens,
} from "./auth";
import { publicHttp } from "./http";

export const api = axios.create({
  baseURL: "/api",
  timeout: 30_000,
});

api.interceptors.request.use((config) => {
  const t = getAccessToken();
  if (t) {
    config.headers.Authorization = `Bearer ${t}`;
  }
  const slug = effectiveTenantSlug();
  if (slug) {
    config.headers["X-Tenant-Slug"] = slug;
  }
  return config;
});

api.interceptors.response.use(
  (r) => r,
  async (err) => {
    const original = err.config;
    if (err.response?.status === 401 && !original._retry && getRefreshToken()) {
      original._retry = true;
      try {
        const { data } = await publicHttp.post(
          "/api/auth/refresh",
          {
            refreshToken: getRefreshToken(),
          },
          { headers: { "X-Tenant-Slug": effectiveTenantSlug() } }
        );
        const nextSlug = (data.tenantSlug ?? getTenantSlug() ?? "").trim() || effectiveTenantSlug();
        setTokens(data.token, data.refreshToken, nextSlug);
        original.headers.Authorization = `Bearer ${data.token}`;
        return api(original);
      } catch {
        clearTokens();
      }
    }
    return Promise.reject(err);
  }
);
