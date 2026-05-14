import { useEffect, useState } from "react";
import { NavLink, Outlet, useLocation } from "react-router-dom";
import {
  Bell,
  Bus,
  CalendarDays,
  GraduationCap,
  LayoutDashboard,
  LogOut,
  MessageCircle,
  Moon,
  Search,
  Settings,
  Sun,
  Users,
  Wallet,
} from "lucide-react";
import { clearTokens, effectiveTenantSlug, getTenantSlug, setTokens } from "@/lib/auth";
import { api } from "@/lib/api";
import { cn } from "@/lib/utils";
import { ChatWidget } from "@/components/ChatWidget";
import { applyTheme, getStoredTheme } from "@/lib/theme";

type Role = "SUPER_ADMIN" | "ROLE_ADMIN" | "ROLE_TEACHER" | "ROLE_PARENT" | "ROLE_STUDENT";

type NavItem = {
  to: string;
  label: string;
  icon: typeof LayoutDashboard;
  end?: boolean;
  roles?: Role[];
};

const nav: NavItem[] = [
  { to: "/", label: "Dashboard", icon: LayoutDashboard, end: true },
  { to: "/students", label: "Students", icon: Users, roles: ["SUPER_ADMIN", "ROLE_ADMIN", "ROLE_TEACHER"] },
  { to: "/teachers", label: "Teachers", icon: GraduationCap, roles: ["SUPER_ADMIN", "ROLE_ADMIN"] },
  { to: "/parents", label: "Parents", icon: Users, roles: ["SUPER_ADMIN", "ROLE_ADMIN"] },
  { to: "/accounts", label: "Account", icon: Wallet, roles: ["SUPER_ADMIN", "ROLE_ADMIN"] },
  { to: "/class", label: "Class", icon: GraduationCap, roles: ["SUPER_ADMIN", "ROLE_ADMIN", "ROLE_TEACHER"] },
  { to: "/exam", label: "Exam", icon: CalendarDays, roles: ["SUPER_ADMIN", "ROLE_ADMIN", "ROLE_TEACHER"] },
  { to: "/transport", label: "Transport", icon: Bus, roles: ["SUPER_ADMIN", "ROLE_ADMIN"] },
  { to: "/notice", label: "Notice", icon: MessageCircle },
];

export function AppShell() {
  const loc = useLocation();
  const [dark, setDark] = useState(() => document.documentElement.classList.contains("dark"));
  const [role, setRole] = useState<Role | null>(null);
  const [switchRole, setSwitchRole] = useState("ROLE_ADMIN");
  const [switchTenant, setSwitchTenant] = useState("");
  const [switchingRole, setSwitchingRole] = useState(false);
  const [switchMsg, setSwitchMsg] = useState<string | null>(null);

  useEffect(() => {
    let mounted = true;
    api
      .get("/auth/me")
      .then(({ data }) => {
        if (mounted) {
          setRole(data.role as Role);
          setSwitchTenant(String(data.tenantSlug ?? getTenantSlug() ?? ""));
        }
      })
      .catch(() => {
        if (mounted) {
          setRole(null);
        }
      });
    return () => {
      mounted = false;
    };
  }, []);

  useEffect(() => {
    const stored = getStoredTheme();
    if (stored) {
      setDark(stored === "dark");
    }
  }, []);

  const title =
    nav.find((n) => (n.end ? loc.pathname === n.to : loc.pathname.startsWith(n.to)))?.label ??
    "Dashboard";
  const visibleNav = nav.filter((item) => !item.roles || (role !== null && item.roles.includes(role)));

  function toggleTheme() {
    const next = document.documentElement.classList.contains("dark") ? "light" : "dark";
    applyTheme(next);
    setDark(next === "dark");
  }

  async function handleRoleSwitch() {
    setSwitchMsg(null);
    if (import.meta.env.VITE_OPEN_API_DEV !== "true") {
      setSwitchMsg("Role switch is available only when VITE_OPEN_API_DEV=true.");
      return;
    }
    if (switchRole !== "SUPER_ADMIN" && !switchTenant.trim()) {
      setSwitchMsg("Tenant slug is required for tenant roles.");
      return;
    }
    setSwitchingRole(true);
    try {
      const { data } = await api.post("/auth/dev-switch-role", {
        targetRole: switchRole,
        tenantSlug: switchTenant.trim() || null,
      });
      setTokens(data.token, data.refreshToken, (data.tenantSlug ?? "").trim());
      setRole(data.role as Role);
      setSwitchMsg(`Switched to ${data.role}${data.tenantSlug ? ` (${data.tenantSlug})` : ""}.`);
      window.location.href = "/";
    } catch {
      setSwitchMsg("Unable to switch role. Ensure API runs with Spring profile dev.");
    } finally {
      setSwitchingRole(false);
    }
  }

  return (
    <div className="flex min-h-screen bg-slate-50 dark:bg-slate-950">
      <aside className="hidden w-56 shrink-0 border-r border-slate-200 bg-white dark:border-slate-800 dark:bg-slate-900 md:block">
        <div className="flex items-center gap-2 px-5 py-6 text-lg font-semibold tracking-tight text-slate-900 dark:text-slate-100">
          <div className="h-9 w-9 rounded-xl bg-[hsl(var(--brand))]" />
          <span>ACERO</span>
        </div>
        <nav className="space-y-1 px-3 pb-8">
          {visibleNav.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) =>
                cn(
                  "flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm text-slate-500 transition hover:bg-slate-50 dark:text-slate-400 dark:hover:bg-slate-800",
                  isActive &&
                    "border-l-4 border-[hsl(var(--accent))] bg-slate-50 font-medium text-slate-900 dark:bg-slate-800/80 dark:text-slate-100"
                )
              }
            >
              <item.icon className="h-4 w-4" />
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className="space-y-1 border-t border-slate-100 px-3 py-4 dark:border-slate-800">
          {import.meta.env.VITE_OPEN_API_DEV === "true" && (
            <div className="mb-3 rounded-xl border border-amber-200 bg-amber-50 p-3 dark:border-amber-900 dark:bg-amber-950/20">
              <div className="mb-2 text-[11px] font-semibold uppercase tracking-wide text-amber-800 dark:text-amber-300">
                Dev Role Switcher
              </div>
              <select
                value={switchRole}
                onChange={(e) => setSwitchRole(e.target.value)}
                className="mb-2 w-full rounded-lg border border-amber-200 bg-white px-2 py-1.5 text-xs dark:border-amber-900 dark:bg-slate-900"
              >
                <option value="ROLE_ADMIN">Admin</option>
                <option value="USER">User</option>
                <option value="ROLE_TEACHER">Teacher</option>
                <option value="ROLE_PARENT">Parent</option>
                <option value="ROLE_STUDENT">Student</option>
                <option value="SUPER_ADMIN">Super Admin</option>
              </select>
              <input
                value={switchTenant}
                onChange={(e) => setSwitchTenant(e.target.value)}
                placeholder="tenant slug (e.g. demo)"
                className="mb-2 w-full rounded-lg border border-amber-200 bg-white px-2 py-1.5 text-xs dark:border-amber-900 dark:bg-slate-900"
                disabled={switchRole === "SUPER_ADMIN"}
              />
              <button
                type="button"
                onClick={() => void handleRoleSwitch()}
                disabled={switchingRole}
                className="w-full rounded-lg bg-amber-600 px-2 py-1.5 text-xs font-semibold text-white disabled:opacity-60"
              >
                {switchingRole ? "Switching..." : "Switch Role"}
              </button>
              {switchMsg && <p className="mt-2 text-[11px] text-amber-800 dark:text-amber-300">{switchMsg}</p>}
            </div>
          )}
          <NavLink
            to="/settings"
            className={({ isActive }) =>
              cn(
                "flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm text-slate-500 dark:text-slate-400",
                isActive && "font-medium text-slate-900 dark:text-slate-100"
              )
            }
          >
            <Settings className="h-4 w-4" />
            Settings
          </NavLink>
          <button
            type="button"
            onClick={() => {
              clearTokens();
              window.location.href = "/login";
            }}
            className="flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-left text-sm text-slate-500 hover:bg-slate-50 dark:text-slate-400 dark:hover:bg-slate-800"
          >
            <LogOut className="h-4 w-4" />
            Log out
          </button>
        </div>
      </aside>

      <div className="flex min-w-0 flex-1 flex-col">
        <header className="sticky top-0 z-10 border-b border-slate-200 bg-white/90 px-4 py-3 backdrop-blur dark:border-slate-800 dark:bg-slate-900/90 md:px-8">
          <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
            <h1 className="text-xl font-semibold text-slate-900 dark:text-slate-100">{title}</h1>
            <div className="flex flex-1 flex-wrap items-center justify-end gap-3 md:max-w-3xl">
              <div className="relative hidden min-w-[200px] flex-1 md:block">
                <Search className="absolute left-3 top-2.5 h-4 w-4 text-slate-400" />
                <input
                  placeholder="Search here"
                  className="w-full rounded-full border border-slate-200 bg-slate-50 py-2 pl-9 pr-3 text-sm text-slate-900 outline-none focus:border-[hsl(var(--brand))] dark:border-slate-700 dark:bg-slate-950 dark:text-slate-100"
                />
              </div>
              <span className="rounded-full border border-slate-200 px-3 py-1 text-xs font-medium text-slate-600 dark:border-slate-700 dark:text-slate-300">
                EN
              </span>
              <button
                type="button"
                onClick={toggleTheme}
                className="rounded-full border border-slate-200 p-2 text-slate-600 hover:bg-slate-50 dark:border-slate-700 dark:text-slate-300 dark:hover:bg-slate-800"
                aria-label={dark ? "Switch to light theme" : "Switch to dark theme"}
              >
                {dark ? <Sun className="h-4 w-4" /> : <Moon className="h-4 w-4" />}
              </button>
              <button
                type="button"
                className="rounded-full border border-slate-200 p-2 text-slate-600 hover:bg-slate-50 dark:border-slate-700 dark:text-slate-300 dark:hover:bg-slate-800"
              >
                <Bell className="h-4 w-4" />
              </button>
              <button
                type="button"
                className="rounded-full border border-slate-200 p-2 text-slate-600 hover:bg-slate-50 dark:border-slate-700 dark:text-slate-300 dark:hover:bg-slate-800"
              >
                <MessageCircle className="h-4 w-4" />
              </button>
              <div className="flex items-center gap-2 rounded-2xl border border-slate-200 bg-white px-3 py-1.5 dark:border-slate-700 dark:bg-slate-900">
                <div className="h-9 w-9 rounded-full bg-slate-200 dark:bg-slate-600" />
                <div className="text-xs leading-tight">
                  <div className="font-semibold text-slate-900 dark:text-slate-100">{role?.replace("ROLE_", "") || "User"}</div>
                  <div className="truncate text-slate-500 dark:text-slate-400" title={effectiveTenantSlug()}>
                    {effectiveTenantSlug() || "—"}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </header>
        <main className="flex-1 p-4 pb-20 md:p-8 md:pb-8">
          <Outlet />
        </main>
      </div>
      <nav className="fixed bottom-0 left-0 right-0 z-20 flex border-t border-slate-200 bg-white px-2 py-2 dark:border-slate-800 dark:bg-slate-900 md:hidden">
        {visibleNav.slice(0, 4).map((item) => (
          <NavLink
            key={`mobile-${item.to}`}
            to={item.to}
            end={item.end}
            className="flex flex-1 flex-col items-center text-[10px] text-slate-600 dark:text-slate-400"
          >
            <item.icon className="h-4 w-4" />
            {item.label}
          </NavLink>
        ))}
      </nav>
      <ChatWidget />
    </div>
  );
}
