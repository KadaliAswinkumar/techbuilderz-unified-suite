import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { setTokens } from "@/lib/auth";
import { publicHttp } from "@/lib/http";

export function LoginPage() {
  const nav = useNavigate();
  const [tenantSlug, setTenantSlug] = useState("");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [err, setErr] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);
  const [forgotLoading, setForgotLoading] = useState(false);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setErr(null);
    setNotice(null);
    const slug = tenantSlug.trim() || null;
    const user = username.trim();
    const pass = password;
    try {
      const { data } = await publicHttp.post("/api/auth/login", {
        tenantSlug: slug,
        username: user,
        password: pass,
      });
      const tenantStored =
        (data.tenantSlug ?? "").trim() ||
        (slug ?? "").trim() ||
        (import.meta.env.VITE_OPEN_API_DEV === "true" ? "demo" : "");
      setTokens(data.token, data.refreshToken, tenantStored);
      nav("/");
    } catch (ex: unknown) {
      const ax = ex as {
        code?: string;
        message?: string;
        response?: { data?: { error?: { message?: string }; message?: string }; status?: number };
      };
      if (ax.code === "ERR_NETWORK" || ax.message === "Network Error") {
        setErr(
          "Cannot reach the API. Use `npm run dev` (Vite proxies /api to port 8080) or start the backend with `./scripts/run-backend.sh`."
        );
        return;
      }
      const msg =
        ax.response?.data?.error?.message ??
        ax.response?.data?.message ??
        (typeof ax.response?.data === "string" ? ax.response.data : null);
      setErr(
        msg ??
          (ax.response?.status === 403
            ? "Request blocked (often CORS). Open the app at http://localhost:5173 or http://127.0.0.1:5173 and restart the API after config changes."
            : "Sign-in failed. Is the API running on port 8080?")
      );
    }
  }

  async function requestReset() {
    const tenant = tenantSlug.trim();
    const user = username.trim();
    if (!tenant || !user) {
      setErr("Enter tenant slug and username/email before requesting password reset.");
      return;
    }
    setErr(null);
    setNotice(null);
    setForgotLoading(true);
    try {
      await publicHttp.post("/api/auth/forgot-password", {
        tenantSlug: tenant,
        usernameOrEmail: user,
      });
      setNotice("Reset request submitted. Contact your administrator for the next step.");
    } catch {
      setErr("Unable to submit reset request right now. Please try again.");
    } finally {
      setForgotLoading(false);
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-100 px-4">
      <form
        onSubmit={submit}
        className="w-full max-w-md space-y-4 surface-card p-8"
      >
        <div className="text-center text-2xl font-semibold">Vidyalaya</div>
        <p className="text-center text-sm text-slate-500">School management sign in</p>
        <p className="text-center text-xs text-slate-400">
          Super admin login uses an empty tenant slug with configured secure credentials.
        </p>
        {err && <p className="rounded-lg bg-red-50 p-2 text-center text-sm text-red-600">{err}</p>}
        {notice && <p className="rounded-lg bg-emerald-50 p-2 text-center text-sm text-emerald-700">{notice}</p>}
        <div>
          <label className="text-xs font-medium text-slate-600">Tenant slug</label>
          <input
            className="mt-1 w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm text-slate-900 placeholder:text-slate-400"
            value={tenantSlug}
            onChange={(e) => setTenantSlug(e.target.value)}
            placeholder="e.g. stmarys (omit for super admin)"
          />
        </div>
        <div>
          <label className="text-xs font-medium text-slate-600">Username</label>
          <input
            className="mt-1 w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm text-slate-900 placeholder:text-slate-400"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
          />
        </div>
        <div>
          <label className="text-xs font-medium text-slate-600">Password</label>
          <input
            type="password"
            className="mt-1 w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm text-slate-900 placeholder:text-slate-400"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>
        <button
          type="submit"
          className="w-full rounded-xl bg-[hsl(var(--accent))] py-2.5 text-sm font-semibold text-white shadow"
        >
          Sign in
        </button>
        <button
          type="button"
          onClick={() => void requestReset()}
          disabled={forgotLoading}
          className="w-full rounded-xl border border-slate-200 py-2.5 text-sm font-medium text-slate-700 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-60"
        >
          {forgotLoading ? "Submitting reset request..." : "Forgot password"}
        </button>
        {import.meta.env.VITE_OPEN_API_DEV === "true" && (
          <div className="rounded-xl border border-amber-200 bg-amber-50 px-3 py-2 text-xs text-amber-900">
            <p className="font-semibold">Temporary demo credentials</p>
            <p>Tenant slug: <strong>demo</strong></p>
            <p>Admin: <strong>demoadmin / DemoAdmin123!</strong></p>
            <p>User: <strong>demouser / DemoUser123!</strong></p>
            <p>Super admin: tenant empty, <strong>superadmin / ChangeMe123!</strong></p>
          </div>
        )}
      </form>
    </div>
  );
}
