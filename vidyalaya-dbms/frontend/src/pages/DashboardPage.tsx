import { useMemo, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { api } from "@/lib/api";
import { effectiveTenantSlug } from "@/lib/auth";
import { cn } from "@/lib/utils";

function fmt(d: Date) {
  return d.toISOString().slice(0, 10);
}

function defaultRange() {
  const to = new Date();
  const from = new Date(to);
  from.setMonth(from.getMonth() - 11);
  from.setDate(1);
  return { from: fmt(from), to: fmt(to) };
}

export function DashboardPage() {
  const initial = useMemo(() => defaultRange(), []);
  const [from, setFrom] = useState(initial.from);
  const [to, setTo] = useState(initial.to);

  const q = useQuery({
    queryKey: ["dashboard-admin", effectiveTenantSlug(), from, to],
    queryFn: async () => {
      try {
        return (
          await api.get("/dashboard/admin", {
            params: { from, to },
          })
        ).data;
      } catch {
        return {};
      }
    },
  });

  const kpi = q.data ?? {};
  const gender = [
    { name: "Male", value: kpi.genderBreakdown?.malePercent ?? 55 },
    { name: "Female", value: kpi.genderBreakdown?.femalePercent ?? 45 },
  ];
  const barData =
    (kpi.monthlySeries as { month: string; earnings: number; expenses: number }[] | undefined) ?? [];

  return (
    <div className="space-y-6">
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <Kpi title="Students" value={kpi.students ?? "—"} />
        <Kpi title="Teachers" value={kpi.teachers ?? "—"} />
        <Kpi title="Parents" value={kpi.parents ?? "—"} />
        <Kpi title="Fee income" value={kpi.feeIncome != null ? `₹${kpi.feeIncome}` : "—"} />
      </div>

      <div className="grid gap-4 xl:grid-cols-3">
        <div className="surface-card p-4 xl:col-span-2">
          <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <h2 className="font-semibold text-slate-900 dark:text-slate-100">Earnings vs expenses</h2>
            <div className="flex flex-wrap items-center gap-2 text-xs">
              <label className="flex items-center gap-1 text-slate-600 dark:text-slate-400">
                From
                <input
                  type="date"
                  value={from}
                  onChange={(e) => setFrom(e.target.value)}
                  className="rounded-lg border border-slate-200 bg-white px-2 py-1 text-slate-900 dark:border-slate-600 dark:bg-slate-950 dark:text-slate-100"
                />
              </label>
              <label className="flex items-center gap-1 text-slate-600 dark:text-slate-400">
                To
                <input
                  type="date"
                  value={to}
                  onChange={(e) => setTo(e.target.value)}
                  className="rounded-lg border border-slate-200 bg-white px-2 py-1 text-slate-900 dark:border-slate-600 dark:bg-slate-950 dark:text-slate-100"
                />
              </label>
              <button
                type="button"
                onClick={() => {
                  const d = defaultRange();
                  setFrom(d.from);
                  setTo(d.to);
                }}
                className="rounded-lg border border-slate-200 px-2 py-1 font-medium text-slate-600 hover:bg-slate-50 dark:border-slate-600 dark:text-slate-300 dark:hover:bg-slate-800"
              >
                Last 12 months
              </button>
            </div>
          </div>
          <div className="h-72">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={barData}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} className="stroke-slate-200 dark:stroke-slate-700" />
                <XAxis dataKey="month" tick={{ fontSize: 11, fill: "currentColor" }} className="text-slate-600" />
                <YAxis tick={{ fontSize: 11, fill: "currentColor" }} className="text-slate-600" />
                <Tooltip
                  contentStyle={{
                    borderRadius: 12,
                    border: "1px solid rgb(226 232 240)",
                    fontSize: 12,
                  }}
                />
                <Bar dataKey="earnings" fill="#6366f1" radius={[6, 6, 0, 0]} name="Earnings" />
                <Bar dataKey="expenses" fill="#fb923c" radius={[6, 6, 0, 0]} name="Expenses" />
              </BarChart>
            </ResponsiveContainer>
          </div>
          {q.isFetching ? <p className="mt-2 text-center text-xs text-slate-500">Updating chart…</p> : null}
        </div>

        <div className="surface-card p-4">
          <h2 className="mb-2 font-semibold text-slate-900 dark:text-slate-100">Gender</h2>
          <div className="h-56">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={gender}
                  dataKey="value"
                  nameKey="name"
                  innerRadius={50}
                  outerRadius={70}
                  paddingAngle={4}
                >
                  <Cell fill="#6366f1" />
                  <Cell fill="#fb923c" />
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </div>
          <p className="text-center text-xs text-slate-500 dark:text-slate-400">
            {gender[0].value}% Male · {gender[1].value}% Female
          </p>
        </div>
      </div>

      <div className="grid gap-4 xl:grid-cols-3">
        <div className="surface-card p-4 xl:col-span-2">
          <h2 className="mb-3 font-semibold text-slate-900 dark:text-slate-100">Notice board</h2>
          <div className="space-y-3">
            {(kpi.notices as { id: string; title: string; createdAt: string }[] | undefined)
              ?.slice(0, 5)
              .map((n) => (
                <div key={n.id} className="surface-muted flex items-center gap-3 p-3">
                  <div className="h-12 w-12 shrink-0 rounded-lg bg-slate-200 dark:bg-slate-600" />
                  <div className="min-w-0 flex-1">
                    <div className="truncate font-medium text-slate-900 dark:text-slate-100">{n.title}</div>
                    <div className="text-xs text-slate-500 dark:text-slate-400">{n.createdAt?.slice?.(0, 10)}</div>
                  </div>
                </div>
              )) ?? <p className="text-sm text-slate-500 dark:text-slate-400">No notices yet.</p>}
          </div>
        </div>

        <div className="space-y-4">
          <div className="rounded-2xl border border-slate-800 bg-slate-900 p-4 text-white shadow-sm dark:border-slate-700">
            <div className="mb-2 flex items-center justify-between text-sm">
              <span className="font-semibold">April 2026</span>
              <span className="text-xs text-slate-400">Calendar</span>
            </div>
            <div className="grid grid-cols-7 gap-1 text-center text-[10px] text-slate-400">
              {["S", "M", "T", "W", "T", "F", "S"].map((d) => (
                <span key={d}>{d}</span>
              ))}
            </div>
            <div className="mt-2 grid grid-cols-7 gap-1 text-center text-xs">
              {Array.from({ length: 28 }, (_, i) => (
                <span
                  key={i}
                  className={cn(
                    "rounded-md py-1",
                    i === 12 ? "bg-[hsl(var(--accent))] font-semibold text-white" : "text-slate-300"
                  )}
                >
                  {i + 1}
                </span>
              ))}
            </div>
          </div>
          <div className="rounded-2xl bg-gradient-to-br from-orange-400 to-orange-500 p-4 text-white shadow-md">
            <div className="text-sm font-semibold">Join the community</div>
            <p className="mt-1 text-xs text-orange-50">Engage with parents and staff.</p>
            <button
              type="button"
              className="mt-3 rounded-full bg-white px-4 py-2 text-xs font-semibold text-orange-600"
            >
              Explore now
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

function Kpi({ title, value }: { title: string; value: string | number }) {
  return (
    <div className="surface-card p-4">
      <div className="text-xs font-medium text-slate-500 dark:text-slate-400">{title}</div>
      <div className="mt-1 text-2xl font-semibold text-slate-900 dark:text-slate-100">{value}</div>
    </div>
  );
}
