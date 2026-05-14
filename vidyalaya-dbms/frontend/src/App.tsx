import type { ReactElement, ReactNode } from "react";
import { useEffect, useState } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { publicHttp } from "@/lib/http";
import { AppShell } from "@/components/AppShell";
import { AccountsPage } from "@/pages/AccountsPage";
import { ClassPage } from "@/pages/ClassPage";
import { DashboardPage } from "@/pages/DashboardPage";
import { ExamPage } from "@/pages/ExamPage";
import { LoginPage } from "@/pages/LoginPage";
import { NoticePage } from "@/pages/NoticePage";
import { ParentDetailPage } from "@/pages/ParentDetailPage";
import { ParentsPage } from "@/pages/ParentsPage";
import { SettingsPage } from "@/pages/SettingsPage";
import { StudentDetailPage } from "@/pages/StudentDetailPage";
import { StudentsPage } from "@/pages/StudentsPage";
import { TeacherDetailPage } from "@/pages/TeacherDetailPage";
import { TeachersPage } from "@/pages/TeachersPage";
import { TransportPage } from "@/pages/TransportPage";
import { DEFAULT_DEV_TENANT_SLUG, getAccessToken, setTokens } from "@/lib/auth";

function RequireAuth({ children }: { children: ReactElement }) {
  if (!getAccessToken()) {
    return <Navigate to="/login" replace />;
  }
  return children;
}

/** When VITE_OPEN_API_DEV=true, wait for GET /api/auth/dev-token before routing (avoids login redirect race). */
function DevAuthGate({ children }: { children: ReactNode }) {
  const devSkip = import.meta.env.VITE_OPEN_API_DEV === "true";
  const [ready, setReady] = useState(() => !devSkip || !!getAccessToken());

  useEffect(() => {
    if (!devSkip || getAccessToken()) {
      return;
    }
    let cancelled = false;
    (async () => {
      try {
        const { data } = await publicHttp.get("/api/auth/dev-token");
        const slug = (data.tenantSlug ?? "").trim() || DEFAULT_DEV_TENANT_SLUG;
        setTokens(data.token, data.refreshToken, slug);
      } catch {
        /* API not in dev profile or DB down — use normal login */
      } finally {
        if (!cancelled) setReady(true);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [devSkip]);

  if (!ready) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-slate-100 text-sm text-slate-600">
        Dev mode: connecting to API…
      </div>
    );
  }
  return children;
}

export default function App() {
  return (
    <DevAuthGate>
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route
        path="/"
        element={
          <RequireAuth>
            <AppShell />
          </RequireAuth>
        }
      >
        <Route index element={<DashboardPage />} />
        <Route path="students" element={<StudentsPage />} />
        <Route path="students/:id" element={<StudentDetailPage />} />
        <Route path="teachers" element={<TeachersPage />} />
        <Route path="teachers/:id" element={<TeacherDetailPage />} />
        <Route path="parents" element={<ParentsPage />} />
        <Route path="parents/:id" element={<ParentDetailPage />} />
        <Route path="accounts" element={<AccountsPage />} />
        <Route path="class" element={<ClassPage />} />
        <Route path="exam" element={<ExamPage />} />
        <Route path="transport" element={<TransportPage />} />
        <Route path="notice" element={<NoticePage />} />
        <Route path="settings" element={<SettingsPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
    </DevAuthGate>
  );
}
