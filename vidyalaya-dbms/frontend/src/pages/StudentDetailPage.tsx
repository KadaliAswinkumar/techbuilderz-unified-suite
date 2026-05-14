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
import type { StudentDto } from "@/types/people";

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

type ExamRow = { id?: string; grade?: string; percentage?: number; status?: string; exam?: { name?: string; examType?: string; examDate?: string }; subject?: { name?: string } };

export function StudentDetailPage() {
  const { id } = useParams();
  const [open, setOpen] = useState(true);

  const { data: s } = useQuery({
    queryKey: ["student", id],
    queryFn: async () => (await api.get(`/students/${id}`)).data as StudentDto,
    enabled: !!id,
  });

  const { data: academic } = useQuery({
    queryKey: ["student-academic", id],
    queryFn: async () => (await api.get(`/students/${id}/academic-record`)).data as { examResults?: ExamRow[] },
    enabled: !!id,
  });

  const rows = (academic?.examResults ?? []).slice(0, 12);

  const att = [
    { name: "Present", value: 60 },
    { name: "Half day", value: 10 },
    { name: "Late", value: 8 },
    { name: "Absent", value: 22 },
  ];

  return (
    <div className="mx-auto max-w-6xl space-y-5">
      <div className="flex items-center justify-between">
        <Link to="/students" className="text-sm font-medium text-[hsl(var(--brand))] hover:underline">
          ← Students directory
        </Link>
      </div>

      <div className="grid gap-5 xl:grid-cols-3">
        <div className="space-y-5 xl:col-span-2">
          <div className="surface-card overflow-hidden">
            <div className="flex flex-wrap items-center justify-between gap-2 border-b border-slate-100 px-4 py-3 dark:border-slate-800">
              <h1 className="text-base font-semibold text-slate-900 dark:text-slate-100">Bio</h1>
              <div className="flex flex-wrap gap-2">
                <button
                  type="button"
                  onClick={() => window.print()}
                  className="inline-flex items-center rounded-xl border border-slate-200 bg-white px-3 py-1.5 text-xs font-semibold text-slate-700 shadow-sm hover:border-orange-200 hover:text-[#ea7c4d] dark:border-slate-600 dark:bg-slate-900 dark:text-slate-200"
                >
                  Print
                </button>
                <Link
                  to="/students"
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
                  background: "conic-gradient(from 180deg, #fb923c, #6366f1, #22d3ee, #fb923c)",
                }}
              >
                <div className="flex h-full w-full items-center justify-center rounded-full bg-white text-2xl font-semibold text-slate-600 dark:bg-slate-900 dark:text-slate-200">
                  {(s?.firstName?.[0] ?? s?.fullName?.[0] ?? "?").toUpperCase()}
                </div>
              </div>
              <div className="mt-4 flex flex-wrap items-center justify-center gap-2">
                <span className="text-xl font-semibold text-slate-900 dark:text-slate-100">{s?.fullName ?? "…"}</span>
                <span className="text-sm font-medium text-[#ea7c4d]">({shortId(id ?? "")})</span>
              </div>
              <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">{s?.email}</p>
              <p className="text-sm text-slate-500 dark:text-slate-400">{s?.phone}</p>

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
                Personal details
                {open ? <ChevronUp className="h-4 w-4" /> : <ChevronDown className="h-4 w-4" />}
              </button>
              {open ? (
                <div className="divide-y divide-slate-100 dark:divide-slate-800">
                  <Row label="Gender" value={s?.gender} />
                  <Row label="First name" value={s?.firstName} />
                  <Row label="Middle name" value={s?.middleName} />
                  <Row label="Last name" value={s?.lastName} />
                  <Row label="Father's name" value={s?.fatherName} />
                  <Row label="Mother's name" value={s?.motherName} />
                  <Row label="Father's occupation" value={s?.fatherOccupation} />
                  <Row label="Mother's occupation" value={s?.motherOccupation} />
                  <Row label="Date of birth" value={fmt(s?.dateOfBirth)} />
                  <Row label="Religion" value={s?.religion} />
                  <Row label="Caste" value={s?.caste} />
                  <Row label="Class" value={s?.className} />
                  <Row label="Section" value={s?.section} />
                  <Row label="Admission date" value={fmt(s?.admissionDate)} />
                  <Row label="Address" value={s?.address} />
                </div>
              ) : null}
            </div>

            {s?.aboutStudent ? (
              <div className="border-t border-slate-100 px-4 py-4 dark:border-slate-800">
                <h3 className="text-sm font-semibold text-slate-900 dark:text-slate-100">About student</h3>
                <p className="mt-2 text-sm leading-relaxed text-slate-600 dark:text-slate-300">{s.aboutStudent}</p>
              </div>
            ) : null}
          </div>

          <div className="surface-card overflow-hidden">
            <div className="flex items-center justify-between border-b border-slate-100 px-4 py-3 dark:border-slate-800">
              <h2 className="text-sm font-semibold text-slate-900 dark:text-slate-100">All exam results</h2>
            </div>
            <div className="overflow-x-auto">
              <table className="w-full min-w-[640px] text-left text-sm">
                <thead>
                  <tr className="border-b border-slate-100 bg-slate-50/80 text-xs font-medium text-slate-500 dark:border-slate-800 dark:bg-slate-800/50 dark:text-slate-400">
                    <th className="px-4 py-3">Exam</th>
                    <th className="px-4 py-3">Type</th>
                    <th className="px-4 py-3">Subject</th>
                    <th className="px-4 py-3">Grade</th>
                    <th className="px-4 py-3">%</th>
                    <th className="px-4 py-3">Date</th>
                  </tr>
                </thead>
                <tbody>
                  {rows.length === 0 ? (
                    <tr>
                      <td colSpan={6} className="px-4 py-6 text-center text-sm text-slate-500">
                        No exam results on file yet.
                      </td>
                    </tr>
                  ) : (
                    rows.map((r, i) => (
                      <tr key={r.id ?? i} className="border-b border-slate-50 last:border-0 dark:border-slate-800/80">
                        <td className="px-4 py-3 font-medium text-slate-900 dark:text-slate-100">{r.exam?.name ?? "—"}</td>
                        <td className="px-4 py-3 text-slate-600 dark:text-slate-300">{r.exam?.examType ?? "—"}</td>
                        <td className="px-4 py-3 text-slate-600 dark:text-slate-300">{r.subject?.name ?? "—"}</td>
                        <td className="px-4 py-3">{r.grade ?? "—"}</td>
                        <td className="px-4 py-3">{r.percentage ?? "—"}</td>
                        <td className="px-4 py-3 text-slate-500">{fmt(r.exam?.examDate)}</td>
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
            <div className="text-xs font-medium text-slate-500 dark:text-slate-400">Growth</div>
            <div className="mt-1 text-3xl font-bold text-emerald-600">72%</div>
            <div className="text-xs text-slate-500 dark:text-slate-400">6 events</div>
          </div>
          <div className="surface-card p-4">
            <div className="text-sm font-semibold text-slate-900 dark:text-slate-100">Attendance</div>
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
                  className={cn(
                    "rounded-lg py-1",
                    i === 15 ? "bg-[#ea7c4d] font-semibold text-white" : "text-slate-300"
                  )}
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
