import { useState, type ReactNode } from "react";
import { Link, useParams } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import {
  ChevronDown,
  ChevronUp,
  Facebook,
  Instagram,
  MapPin,
  Printer,
  Twitter,
} from "lucide-react";
import { Cell, Pie, PieChart, ResponsiveContainer, Tooltip } from "recharts";
import { api } from "@/lib/api";
import { cn } from "@/lib/utils";
import type { TeacherDto } from "@/types/people";

function shortId(id: string) {
  return id.replace(/-/g, "").slice(-4).toUpperCase();
}

function fmt(d?: string | null) {
  if (!d) return "—";
  const x = d.slice(0, 10);
  const [y, m, day] = x.split("-");
  if (!y || !m || !day) return d;
  const mon = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"][Number(m) - 1];
  return `${day}, ${mon} ${y}`;
}

type InvigRow = {
  id?: string;
  exam?: { name?: string; examType?: string; examDate?: string; schoolClass?: { name?: string } };
};

type AssignRow = {
  id?: string;
  schoolClass?: { name?: string };
  subject?: { name?: string };
};

type SlotRow = {
  id?: string;
  dayOfWeek?: number;
  startTime?: string;
  endTime?: string;
  title?: string;
  schoolClass?: { name?: string };
};

const DAYS = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];

export function TeacherDetailPage() {
  const { id } = useParams();
  const [open, setOpen] = useState(true);

  const { data: t } = useQuery({
    queryKey: ["teacher", id],
    queryFn: async () => (await api.get(`/teachers/${id}`)).data as TeacherDto,
    enabled: !!id,
  });

  const { data: assignments } = useQuery({
    queryKey: ["teacher-assignments", id],
    queryFn: async () => (await api.get(`/teachers/${id}/assignments`)).data as AssignRow[],
    enabled: !!id,
  });

  const { data: invigilations } = useQuery({
    queryKey: ["teacher-invigilations", id],
    queryFn: async () => (await api.get(`/teachers/${id}/invigilations`)).data as InvigRow[],
    enabled: !!id,
  });

  const { data: timetable } = useQuery({
    queryKey: ["teacher-timetable", id],
    queryFn: async () => (await api.get(`/teachers/${id}/timetable`)).data as SlotRow[],
    enabled: !!id,
  });

  const invRows = (invigilations ?? []).slice(0, 12);
  const att = [
    { name: "Present", value: 55 },
    { name: "Half day", value: 12 },
    { name: "Late", value: 10 },
    { name: "Absent", value: 23 },
  ];

  return (
    <div className="mx-auto max-w-6xl space-y-5">
      <div className="flex items-center justify-between">
        <Link to="/teachers" className="text-sm font-medium text-[hsl(var(--brand))] hover:underline">
          ← Teachers directory
        </Link>
      </div>

      <div className="grid gap-5 xl:grid-cols-3">
        <div className="space-y-5 xl:col-span-2">
          <div className="surface-card overflow-hidden">
            <div className="flex flex-wrap items-center justify-between gap-2 border-b border-slate-100 px-4 py-3 dark:border-slate-800">
              <h1 className="text-base font-semibold text-slate-900 dark:text-slate-100">Bio</h1>
              <div className="flex flex-wrap gap-2">
                <HeaderPill onClick={() => window.print()}>Print</HeaderPill>
                <Link
                  to="/teachers"
                  className="inline-flex items-center rounded-xl border border-slate-200 bg-white px-3 py-1.5 text-xs font-semibold text-slate-700 shadow-sm hover:border-orange-200 hover:text-[#ea7c4d] dark:border-slate-600 dark:bg-slate-900 dark:text-slate-200"
                >
                  Directory
                </Link>
              </div>
            </div>

            <div className="flex flex-col items-center px-5 pb-4 pt-6 text-center">
              <div
                className="h-28 w-28 rounded-full p-[3px]"
                style={{
                  background: "conic-gradient(from 90deg, #3b82f6, #6366f1, #22d3ee, #3b82f6)",
                }}
              >
                <div className="flex h-full w-full items-center justify-center rounded-full bg-white text-2xl font-semibold text-slate-600 dark:bg-slate-900 dark:text-slate-200">
                  {(t?.fullName?.[0] ?? "?").toUpperCase()}
                </div>
              </div>
              <div className="mt-4 flex flex-wrap items-center justify-center gap-2">
                <span className="text-xl font-semibold text-slate-900 dark:text-slate-100">{t?.fullName ?? "…"}</span>
                <span className="text-sm font-medium text-[#ea7c4d]">({shortId(id ?? "")})</span>
              </div>
              <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">{t?.email}</p>
              <p className="text-sm text-slate-500 dark:text-slate-400">{t?.phone}</p>

              <div className="mt-5 flex flex-wrap justify-center gap-2">
                <IconCircle label="Print">
                  <Printer className="h-4 w-4" />
                </IconCircle>
                <IconCircle label="Map">
                  <MapPin className="h-4 w-4" />
                </IconCircle>
                <IconCircle label="Facebook">
                  <Facebook className="h-4 w-4" />
                </IconCircle>
                <IconCircle label="Twitter">
                  <Twitter className="h-4 w-4" />
                </IconCircle>
                <IconCircle label="Instagram">
                  <Instagram className="h-4 w-4" />
                </IconCircle>
              </div>
            </div>

            <div className="border-t border-slate-100 dark:border-slate-800">
              <button
                type="button"
                onClick={() => setOpen((o) => !o)}
                className="flex w-full items-center justify-between bg-[#fff4ed] px-4 py-3 text-left text-sm font-semibold text-slate-900 dark:bg-orange-950/30 dark:text-slate-100"
              >
                Education &amp; experience
                {open ? <ChevronUp className="h-4 w-4" /> : <ChevronDown className="h-4 w-4" />}
              </button>
              {open ? (
                <div className="divide-y divide-slate-100 dark:divide-slate-800">
                  <Row label="Gender" value={t?.gender} />
                  <Row label="Qualification" value={t?.qualification} />
                  <Row label="Experience" value={t?.experienceSummary} />
                  <Row label="Date of birth" value={fmt(t?.dateOfBirth ?? null)} />
                  <Row label="Joining date" value={fmt(t?.joiningDate ?? null)} />
                  <Row label="Monthly salary" value={t?.salaryAmount != null ? String(t.salaryAmount) : null} />
                  <Row label="Address" value={t?.address} />
                </div>
              ) : null}
            </div>

            <div className="border-t border-slate-100 px-4 py-4 dark:border-slate-800">
              <h3 className="text-sm font-semibold text-slate-900 dark:text-slate-100">Groups</h3>
              <div className="mt-3 grid gap-3 sm:grid-cols-3">
                {(assignments ?? []).length === 0 ? (
                  <p className="text-sm text-slate-500 sm:col-span-3">No class assignments yet.</p>
                ) : (
                  (assignments ?? []).slice(0, 6).map((a, i) => (
                    <div
                      key={a.id ?? i}
                      className="rounded-2xl border border-slate-100 bg-slate-50/80 p-3 text-center dark:border-slate-800 dark:bg-slate-800/40"
                    >
                      <div className="text-xs font-semibold text-slate-900 dark:text-slate-100">{a.schoolClass?.name ?? "Class"}</div>
                      <div className="mt-1 text-[11px] text-slate-500 dark:text-slate-400">{a.subject?.name ?? "General"}</div>
                      <button
                        type="button"
                        className="mt-3 w-full rounded-full bg-[#ea7c4d]/15 py-1.5 text-[11px] font-semibold text-[#c45d38] dark:text-[#ffb49a]"
                      >
                        Join
                      </button>
                    </div>
                  ))
                )}
              </div>
              <button
                type="button"
                className="mt-4 inline-flex items-center gap-2 rounded-2xl bg-slate-900 px-4 py-2 text-xs font-semibold text-white dark:bg-slate-700"
              >
                + Create new group
              </button>
            </div>
          </div>

          <div className="surface-card overflow-hidden">
            <div className="flex items-center justify-between border-b border-slate-100 px-4 py-3 dark:border-slate-800">
              <h2 className="text-sm font-semibold text-slate-900 dark:text-slate-100">Exam helds</h2>
              <div className="flex gap-2 text-slate-400">
                <Printer className="h-4 w-4 cursor-pointer hover:text-slate-600" onClick={() => window.print()} />
              </div>
            </div>
            <div className="overflow-x-auto">
              <table className="w-full min-w-[640px] text-left text-sm">
                <thead>
                  <tr className="border-b border-slate-100 bg-slate-50/80 text-xs font-medium text-slate-500 dark:border-slate-800 dark:bg-slate-800/50 dark:text-slate-400">
                    <th className="px-4 py-3">Exam</th>
                    <th className="px-4 py-3">Type</th>
                    <th className="px-4 py-3">Class</th>
                    <th className="px-4 py-3">Date</th>
                    <th className="px-4 py-3">Status</th>
                  </tr>
                </thead>
                <tbody>
                  {invRows.length === 0 ? (
                    <tr>
                      <td colSpan={5} className="px-4 py-6 text-center text-sm text-slate-500">
                        No invigilation duties recorded.
                      </td>
                    </tr>
                  ) : (
                    invRows.map((r, i) => (
                      <tr key={r.id ?? i} className="border-b border-slate-50 last:border-0 dark:border-slate-800/80">
                        <td className="px-4 py-3 font-medium text-slate-900 dark:text-slate-100">{r.exam?.name ?? "—"}</td>
                        <td className="px-4 py-3 text-slate-600 dark:text-slate-300">{r.exam?.examType ?? "—"}</td>
                        <td className="px-4 py-3 text-slate-600 dark:text-slate-300">{r.exam?.schoolClass?.name ?? "—"}</td>
                        <td className="px-4 py-3 text-slate-500">{fmt(r.exam?.examDate)}</td>
                        <td className="px-4 py-3">
                          <span className="rounded-full bg-sky-100 px-2 py-0.5 text-xs font-medium text-sky-800 dark:bg-sky-950/50 dark:text-sky-200">
                            Scheduled
                          </span>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <div className="space-y-4">
          <div className="surface-card flex items-center justify-between p-4">
            <div>
              <div className="text-xs font-medium text-slate-500 dark:text-slate-400">Events</div>
              <div className="text-2xl font-bold text-slate-900 dark:text-slate-100">6</div>
            </div>
            <div className="flex h-10 w-10 items-center justify-center rounded-full bg-slate-900 text-white dark:bg-slate-700">
              <span className="text-lg">→</span>
            </div>
          </div>
          <div className="surface-card p-4">
            <div className="text-xs font-medium text-slate-500 dark:text-slate-400">Target achieved</div>
            <div className="mt-1 text-3xl font-bold text-indigo-600">84%</div>
          </div>
          <div className="surface-card p-4">
            <div className="text-sm font-semibold text-slate-900 dark:text-slate-100">Today&apos;s timeline</div>
            <ul className="mt-3 space-y-3 text-xs text-slate-600 dark:text-slate-300">
              {(timetable ?? []).slice(0, 4).map((slot, i) => (
                <li key={slot.id ?? i} className="flex gap-2 border-l-2 border-[#ea7c4d] pl-3">
                  <div>
                    <div className="font-semibold text-slate-800 dark:text-slate-100">
                      {slot.title ?? "Period"}
                      {slot.schoolClass?.name ? ` · ${slot.schoolClass.name}` : ""}
                    </div>
                    <div className="text-slate-500">
                      {DAYS[slot.dayOfWeek ?? 0] ?? "—"} {slot.startTime?.slice(0, 5) ?? ""}
                    </div>
                  </div>
                </li>
              ))}
              {(timetable ?? []).length === 0 ? <li className="text-slate-500">No timetable slots.</li> : null}
            </ul>
          </div>
          <div className="surface-card p-4">
            <div className="text-sm font-semibold text-slate-900 dark:text-slate-100">Attendance (sample)</div>
            <div className="mt-2 h-48">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie data={att} dataKey="value" nameKey="name" innerRadius={44} outerRadius={62} paddingAngle={3}>
                    {att.map((_, i) => (
                      <Cell key={i} fill={["#22c55e", "#eab308", "#f97316", "#ef4444"][i]} />
                    ))}
                  </Pie>
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
            </div>
          </div>
          <div className="rounded-3xl border border-slate-800 bg-gradient-to-b from-slate-900 to-slate-950 p-4 text-white shadow-lg">
            <div className="mb-3 flex rounded-full bg-slate-800/80 p-1 text-xs font-semibold">
              <span className="flex-1 rounded-full bg-[#ea7c4d] py-1.5 text-center text-white">Day to day</span>
              <span className="flex-1 py-1.5 text-center text-slate-400">Events</span>
            </div>
            <div className="text-center text-sm font-semibold">Feb 2026</div>
            <div className="mt-3 grid grid-cols-7 gap-1 text-center text-[10px] text-slate-400">
              {["S", "M", "T", "W", "T", "F", "S"].map((d) => (
                <span key={d}>{d}</span>
              ))}
            </div>
            <div className="mt-2 grid grid-cols-7 gap-1 text-center text-[11px]">
              {Array.from({ length: 28 }, (_, i) => (
                <span
                  key={i}
                  className={cn("rounded-lg py-1", i === 15 ? "bg-[#ea7c4d] font-semibold text-white" : "text-slate-300")}
                >
                  {i + 1}
                </span>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

function HeaderPill({ children, onClick }: { children: ReactNode; onClick: () => void }) {
  return (
    <button
      type="button"
      onClick={onClick}
      className="inline-flex items-center rounded-xl border border-slate-200 bg-white px-3 py-1.5 text-xs font-semibold text-slate-700 shadow-sm hover:border-orange-200 hover:text-[#ea7c4d] dark:border-slate-600 dark:bg-slate-900 dark:text-slate-200"
    >
      {children}
    </button>
  );
}

function Row({ label, value }: { label: string; value?: string | null }) {
  return (
    <div className="grid grid-cols-1 gap-1 px-4 py-2.5 sm:grid-cols-2 sm:gap-4">
      <div className="text-xs font-medium text-slate-500 dark:text-slate-400">{label}</div>
      <div className="text-sm font-medium text-slate-900 dark:text-slate-100">{value?.trim() ? value : "—"}</div>
    </div>
  );
}

function IconCircle({ children, label }: { children: ReactNode; label: string }) {
  return (
    <button
      type="button"
      title={label}
      className="flex h-10 w-10 items-center justify-center rounded-full border border-slate-200 bg-white text-slate-600 shadow-sm transition hover:border-orange-200 hover:text-[#ea7c4d] dark:border-slate-700 dark:bg-slate-900 dark:text-slate-300"
    >
      {children}
    </button>
  );
}
