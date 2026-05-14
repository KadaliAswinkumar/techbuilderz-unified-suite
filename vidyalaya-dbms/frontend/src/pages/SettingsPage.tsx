import { useEffect, useState } from "react";
import { api } from "@/lib/api";

export function SettingsPage() {
  const [primaryColor, setPrimaryColor] = useState("#4F46E5");
  const [logoUrl, setLogoUrl] = useState("");
  const [role, setRole] = useState<string>("");
  const [tenantSlug, setTenantSlug] = useState("");
  const [resetUsername, setResetUsername] = useState("");
  const [resetPassword, setResetPassword] = useState("");
  const [message, setMessage] = useState<string | null>(null);

  useEffect(() => {
    api
      .get("/tenant/branding")
      .then((r) => {
        const pc = r.data.primaryColor ?? "#4F46E5";
        setPrimaryColor(pc);
        setLogoUrl(r.data.logoUrl ?? "");
        document.documentElement.style.setProperty("--brand", hexToHsl(pc));
      })
      .catch(() => {});
    api
      .get("/auth/me")
      .then((r) => {
        setRole(String(r.data.role ?? ""));
        setTenantSlug(String(r.data.tenantSlug ?? ""));
      })
      .catch(() => {});
  }, []);

  async function save() {
    await api.put("/tenant/branding", { primaryColor, logoUrl });
    document.documentElement.style.setProperty("--brand", hexToHsl(primaryColor));
    setMessage("Branding updated.");
  }

  async function resetUserPassword() {
    setMessage(null);
    if (!tenantSlug.trim() || !resetUsername.trim() || !resetPassword.trim()) {
      setMessage("Fill tenant slug, username, and new password.");
      return;
    }
    await api.post("/auth/reset-password", {
      tenantSlug: tenantSlug.trim(),
      username: resetUsername.trim(),
      newPassword: resetPassword,
    });
    setResetPassword("");
    setMessage("Password reset completed.");
  }

  return (
    <div className="max-w-3xl space-y-6">
      <div className="surface-card space-y-4 p-6">
        <h1 className="text-lg font-semibold">Branding</h1>
        <div>
          <label className="text-xs font-medium text-slate-600">Primary color</label>
          <input
            type="color"
            className="mt-1 h-10 w-full"
            value={primaryColor}
            onChange={(e) => setPrimaryColor(e.target.value)}
          />
        </div>
        <div>
          <label className="text-xs font-medium text-slate-600">Logo URL</label>
          <input
            className="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm"
            value={logoUrl}
            onChange={(e) => setLogoUrl(e.target.value)}
          />
        </div>
        <button
          type="button"
          onClick={() => void save()}
          className="rounded-xl bg-[hsl(var(--brand))] px-4 py-2 text-sm font-semibold text-white"
        >
          Save
        </button>
      </div>

      {role === "SUPER_ADMIN" && (
        <div className="surface-card space-y-3 p-6">
          <h2 className="text-base font-semibold">Admin Password Reset</h2>
          <p className="text-xs text-slate-500">
            Use this only for controlled support cases. The change applies immediately.
          </p>
          <div>
            <label className="text-xs font-medium text-slate-600">Tenant slug</label>
            <input
              className="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm"
              value={tenantSlug}
              onChange={(e) => setTenantSlug(e.target.value)}
              placeholder="e.g. stmarys"
            />
          </div>
          <div>
            <label className="text-xs font-medium text-slate-600">Username</label>
            <input
              className="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm"
              value={resetUsername}
              onChange={(e) => setResetUsername(e.target.value)}
              placeholder="user to reset"
            />
          </div>
          <div>
            <label className="text-xs font-medium text-slate-600">New password</label>
            <input
              type="password"
              className="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm"
              value={resetPassword}
              onChange={(e) => setResetPassword(e.target.value)}
            />
          </div>
          <button
            type="button"
            onClick={() => void resetUserPassword()}
            className="rounded-xl bg-slate-900 px-4 py-2 text-sm font-semibold text-white"
          >
            Reset password
          </button>
        </div>
      )}

      {message && (
        <div className="rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700">
          {message}
        </div>
      )}
      <div className="rounded-xl border border-slate-200 bg-white px-4 py-3 text-xs text-slate-500">
        Signed in role: <strong>{role || "Unknown"}</strong>
      </div>
    </div>
  );
}

function hexToHsl(hex: string): string {
  const h = hex.replace("#", "");
  const r = parseInt(h.slice(0, 2), 16) / 255;
  const g = parseInt(h.slice(2, 4), 16) / 255;
  const b = parseInt(h.slice(4, 6), 16) / 255;
  const max = Math.max(r, g, b);
  const min = Math.min(r, g, b);
  let hDeg = 0;
  const l = (max + min) / 2;
  let s = 0;
  if (max !== min) {
    const d = max - min;
    s = l > 0.5 ? d / (2 - max - min) : d / (max + min);
    switch (max) {
      case r:
        hDeg = ((g - b) / d + (g < b ? 6 : 0)) / 6;
        break;
      case g:
        hDeg = ((b - r) / d + 2) / 6;
        break;
      default:
        hDeg = ((r - g) / d + 4) / 6;
    }
  }
  return `${Math.round(hDeg * 360)} ${Math.round(s * 100)}% ${Math.round(l * 100)}%`;
}
