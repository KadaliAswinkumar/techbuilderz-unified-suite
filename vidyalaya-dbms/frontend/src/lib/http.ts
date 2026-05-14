import axios from "axios";

const TIMEOUT_MS = 30_000;

/**
 * Same-origin requests to `/api/...` (Vite proxies to Spring). No auth interceptors —
 * use for login, dev-token, and refresh so stale tokens are never attached by mistake.
 */
export const publicHttp = axios.create({
  baseURL: "",
  timeout: TIMEOUT_MS,
});
