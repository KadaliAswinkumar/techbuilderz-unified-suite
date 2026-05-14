import { useMemo, useState, type ReactNode } from "react";
import { Link, useParams } from "react-router-dom";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  ChevronDown,
  ChevronUp,
  Facebook,
  Instagram,
  MapPin,
  Printer,
  Trash2,
  Twitter,
} from "lucide-react";
import { api } from "@/lib/api";
import { cn } from "@/lib/utils";
import type { ParentDto } from "@/types/people";

function shortId(id: string) {
  return id.replace(/-/g, "").slice(-4).toUpperCase();
}

function fmt(d?: string | null) {
  if (!d) return "—";
  const x = String(d).slice(0, 10);
  const [y, m, day] = x.split("-");
  if (!y || !m || !day) return String(d);
  const mon = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"][Number(m) - 1];
  return `${day}, ${mon} ${y}`;
}

type ExamRow = {
  id?: string;
  grade?: string;
  percentage?: number | string;
  status?: string;
  student?: { fullName?: string };
  exam?: { name?: string; examType?: string; examDate?: string };
  subject?: { name?: string };
};

type FeeRow = {
  id?: string;
  amount?: number | string;
  status?: string;
  createdAt?: string;
  feeStructure?: { name?: string };
};

type KidRow = { studentId: string; fullName: string; className?: string; section?: string };

type CommunityRow = { id: string; name: string };

function statusPill(status?: string) {
  const s = (status ?? "").toUpperCase();
  if (s === "ACTIVE") return "bg-emerald-100 text-emerald-800 dark:bg-emerald-950/50 dark:text-emerald-200";
  if (s === "OPENED" || s === "OPEN")
    return "bg-orange-100 text-orange-900 dark:bg-orange-950/40 dark:text-orange-200";
  if (s === "COMPLETED" || s === "CLOSED") return "bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-200";
  return "bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-200";
}

export function ParentDetailPage() {
  const { id } = useParams();
  const qc = useQueryClient();
  const [open, setOpen] = useState(true);

  const { data: p } = useQuery({
    queryKey: ["parent", id],
    queryFn: async () => (await api.get(`/parents/${id}`)).data as ParentDto,
    enabled: !!id,
  });

  const { data: kids } = useQuery({
    queryKey: ["parent-children", id],
    queryFn: async () => (await api.get(`/parents/${id}/children`)).data as KidRow[],
    enabled: !!id,
  });

  const { data: communities } = useQuery({
    queryKey: ["parent-comm", id],
    queryFn: async () => (await api.get(`/parents/${id}/communities`)).data as CommunityRow[],
    enabled: !!id,
  });

  const { data: examList } = useQuery({
    queryKey: ["parent-exams", id],
    queryFn: async () => (await api.get(`/parents/${id}/exam-results`)).data as ExamRow[],
    enabled: !!id,
  });

  const { data: feeDues } = useQuery({
    queryKey: ["parent-fees", id],
    queryFn: async () => (await api.get(`/parents/${id}/fee-dues`)).data as FeeRow[],
    enabled: !!id,
  });

  const exams = (examList ?? []).slice(0, 15);
  const fees = feeDues ?? [];

  const dueTotal = useMemo(() => {
    return fees.reduce((acc, f) => acc + Number(f.amount ?? 0), 0);
  }, [fees]);

  const removeComm = useMutation({
    mutationFn: async (communityId: string) => api.delete(`/parents/communities/${communityId}`),
    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: ["parent-comm", id] });
    },
  });

  return (
    <div className="mx-auto max-w-6xl space-y-5">
      <div className="flex items-center justify-between">
        <Link to="/parents" className="text-sm font-medium text-[hsl(var(--brand))] hover:underline">
          ← Parents directory
        </Link>
      </div>

      <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
        <Stat title="Due fees" value={fees.length ? `$${dueTotal.toFixed(0)}` : "—"} />
        <Stat title="Results" value={String(examList?.length ?? 0)} />
        <Stat title="Complaints" value="—" />
        <Stat title="Linked kids" value={String(kids?.length ?? p?.childrenCount ?? 0)} />
      </div>

      <div className="grid gap-5 xl:grid-cols-3">
        <div className="space-y-5 xl:col-span-2">
          <div className="surface-card overflow-hidden">
            <div className="flex flex-wrap items-center justify-between gap-2 border-b border-slate-100 px-4 py-3 dark:border-slate-800">
              <h1 className="text-base font-semibold text-slate-900 dark:text-slate-100">Parents</h1>
              <div className="flex flex-wrap gap-2">
                <button
                  type="button"
                  onClick={() => window.print()}
                  className="inline-flex items-center rounded-xl border border-slate-200 bg-white px-3 py-1.5 text-xs font-semibold text-slate-700 shadow-sm hover:border-orange-200 hover:text-[#ea7c4d] dark:border-slate-600 dark:bg-slate-900 dark:text-slate-200"
                >
                  Print
                </button>
                <Link
                  to="/parents"
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
                  background: "conic-gradient(from 0deg, #3b82f6, #60a5fa, #93c5fd, #3b82f6)",
                }}
              >
                <div className="flex h-full w-full items-center justify-center rounded-full bg-white text-2xl font-semibold text-slate-600 dark:bg-slate-900 dark:text-slate-200">
                  {(p?.fullName?.[0] ?? "?").toUpperCase()}
                </div>
              </div>
              <div className="mt-4 flex flex-wrap items-center justify-center gap-2">
                <span className="text-xl font-semibold text-slate-900 dark:text-slate-100">{p?.fullName ?? "…"}</span>
                <span className="text-sm font-medium text-[#ea7c4d]">({shortId(id ?? "")})</span>
              </div>
              <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">{p?.email}</p>
              <p className="text-sm text-slate-500 dark:text-slate-400">{p?.phone}</p>

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
                Personal &amp; professional details
                {open ? <ChevronUp className="h-4 w-4" /> : <ChevronDown className="h-4 w-4" />}
              </button>
              {open ? (
                <div className="divide-y divide-slate-100 dark:divide-slate-800">
                  <Row label="Occupation" value={p?.occupation} />
                  <Row label="Employer" value={p?.employer} />
                  <Row label="Education &amp; background" value={p?.educationSummary} />
                  <Row label="Address" value={p?.address} />
                </div>
              ) : null}
            </div>
          </div>

          <div className="surface-card p-4 md:p-5">
            <h2 className="text-sm font-semibold text-slate-900 dark:text-slate-100">Kids</h2>
            <div className="mt-4 flex flex-wrap gap-3">
              {(kids ?? []).map((c) => (
                <Link
                  key={c.studentId}
                  to={`/students/${c.studentId}`}
                  className="flex min-w-[140px] flex-1 items-center gap-3 rounded-2xl border border-slate-100 bg-slate-50/80 p-3 transition hover:border-orange-200 dark:border-slate-800 dark:bg-slate-800/40"
                >
                  <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-white text-sm font-semibold text-sky-700 shadow-sm dark:bg-slate-900 dark:text-sky-200">
                    {(c.fullName?.[0] ?? "?").toUpperCase()}
                  </div>
                  <div className="min-w-0 text-left text-sm">
                    <div className="truncate font-semibold text-slate-900 dark:text-slate-100">{c.fullName}</div>
                    <div className="truncate text-xs text-slate-500">
                      {c.className ?? "—"}
                      {c.section ? ` · ${c.section}` : ""}
                    </div>
                  </div>
                </Link>
              ))}
              {(kids ?? []).length === 0 ? <span className="text-sm text-slate-500">No children linked yet.</span> : null}
            </div>
          </div>

          <div className="surface-card p-4 md:p-5">
            <h2 className="text-sm font-semibold text-slate-900 dark:text-slate-100">Joined community</h2>
            <div className="mt-4 grid gap-3 sm:grid-cols-3">
              {(communities ?? []).map((c) => (
                <div
                  key={c.id}
                  className="flex flex-col rounded-2xl border border-slate-100 bg-white p-3 shadow-sm dark:border-slate-800 dark:bg-slate-900/60"
                >
                  <div className="text-sm font-semibold text-slate-900 dark:text-slate-100">{c.name}</div>
                  <button
                    type="button"
                    disabled={removeComm.isPending}
                    onClick={() => removeComm.mutate(c.id)}
                    className="mt-3 inline-flex items-center justify-center gap-1 rounded-full bg-[#ea7c4d]/15 py-2 text-xs font-semibold text-[#c45d38] disabled:opacity-50 dark:text-[#ffb49a]"
                  >
                    <Trash2 className="h-3 w-3" />
                    Remove
                  </button>
                </div>
              ))}
              {(communities ?? []).length === 0 ? <p className="text-sm text-slate-500">No communities yet.</p> : null}
            </div>
          </div>

          <div className="surface-card overflow-hidden">
            <div className="flex flex-wrap items-center justify-between gap-2 border-b border-slate-100 px-4 py-3 dark:border-slate-800">
              <h2 className="text-sm font-semibold text-slate-900 dark:text-slate-100">Exam results (children)</h2>
              <div className="flex gap-2 text-slate-400">
                <Printer className="h-4 w-4 cursor-pointer hover:text-slate-600" onClick={() => window.print()} />
              </div>
            </div>
            <div className="overflow-x-auto">
              <table className="w-full min-w-[720px] text-left text-sm">
                <thead>
                  <tr className="border-b border-slate-100 bg-slate-50/80 text-xs font-medium text-slate-500 dark:border-slate-800 dark:bg-slate-800/50 dark:text-slate-400">
                    <th className="px-4 py-3">Exam</th>
                    <th className="px-4 py-3">Student</th>
                    <th className="px-4 py-3">Subject</th>
                    <th className="px-4 py-3">Grade</th>
                    <th className="px-4 py-3">%</th>
                    <th className="px-4 py-3">Status</th>
                    <th className="px-4 py-3">Date</th>
                  </tr>
                </thead>
                <tbody>
                  {exams.length === 0 ? (
                    <tr>
                      <td colSpan={7} className="px-4 py-6 text-center text-sm text-slate-500">
                        No exam results for linked students.
                      </td>
                    </tr>
                  ) : (
                    exams.map((r, i) => (
                      <tr key={r.id ?? i} className="border-b border-slate-50 last:border-0 dark:border-slate-800/80">
                        <td className="px-4 py-3 font-medium text-slate-900 dark:text-slate-100">{r.exam?.name ?? "—"}</td>
                        <td className="px-4 py-3 text-slate-600 dark:text-slate-300">{r.student?.fullName ?? "—"}</td>
                        <td className="px-4 py-3 text-slate-600 dark:text-slate-300">{r.subject?.name ?? "—"}</td>
                        <td className="px-4 py-3">{r.grade ?? "—"}</td>
                        <td className="px-4 py-3">{r.percentage ?? "—"}</td>
                        <td className="px-4 py-3">
                          <span className={cn("rounded-full px-2 py-0.5 text-xs font-medium", statusPill(r.status))}>
                            {r.status ?? "—"}
                          </span>
                        </td>
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

          <div className="surface-card p-4">
            <h3 className="text-sm font-semibold text-slate-900 dark:text-slate-100">Fee dues</h3>
            <ul className="mt-3 space-y-3">
              {fees.length === 0 ? (
                <li className="text-sm text-slate-500">No outstanding fee lines.</li>
              ) : (
                fees.map((f, i) => (
                  <li key={f.id ?? i} className="flex items-center justify-between gap-2 border-b border-slate-100 pb-3 last:border-0 dark:border-slate-800">
                    <div>
                      <div className="text-sm font-medium text-slate-900 dark:text-slate-100">{f.feeStructure?.name ?? "Fee"}</div>
                      <div className="text-xs text-slate-500">{fmt(f.createdAt)}</div>
                    </div>
                    <div className="text-right">
                      <div className="text-sm font-semibold text-slate-900 dark:text-slate-100">
                        ${Number(f.amount ?? 0).toFixed(2)}
                      </div>
                      <span className="mt-1 inline-block rounded-full bg-amber-100 px-2 py-0.5 text-[10px] font-semibold uppercase text-amber-900 dark:bg-amber-950/50 dark:text-amber-200">
                        Due
                      </span>
                    </div>
                  </li>
                ))
              )}
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
}

function Stat({ title, value }: { title: string; value: string }) {
  return (
    <div className="surface-card p-4">
      <div className="text-xs text-slate-500 dark:text-slate-400">{title}</div>
      <div className="mt-1 text-lg font-semibold text-slate-900 dark:text-slate-100">{value}</div>
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
