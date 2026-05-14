import type { ReactNode } from "react";
import type { UseQueryResult } from "@tanstack/react-query";
import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";
import { effectiveTenantSlug } from "@/lib/auth";
import { cn } from "@/lib/utils";

type FeeStructureRow = {
  id: string;
  name: string;
  amount: number | string;
  dueDate?: string;
};

type FeePaymentRow = {
  id: string;
  amount: number | string;
  status: string;
  paidAt?: string | null;
  student?: { fullName?: string; id?: string };
  feeStructure?: { name?: string };
};

type SalaryRow = {
  id: string;
  amount: number | string;
  monthYear?: string;
  status?: string;
  teacher?: { fullName?: string };
};

type ExpenseRow = {
  id: string;
  category: string;
  amount: number | string;
  description?: string;
  expenseDate?: string;
};

export function AccountsPage() {
  const tenant = effectiveTenantSlug();

  const fees = useQuery({
    queryKey: ["fee-structures", tenant],
    queryFn: async () => (await api.get<FeeStructureRow[]>("/fee-structures")).data,
  });
  const pays = useQuery({
    queryKey: ["fee-payments", tenant],
    queryFn: async () => (await api.get<FeePaymentRow[]>("/fee-payments")).data,
  });
  const sal = useQuery({
    queryKey: ["salary-payments", tenant],
    queryFn: async () => (await api.get<SalaryRow[]>("/salary-payments")).data,
  });
  const exp = useQuery({
    queryKey: ["expenses", tenant],
    queryFn: async () => (await api.get<ExpenseRow[]>("/expenses")).data,
  });

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-xl font-bold tracking-tight text-slate-900 dark:text-white">Finance overview</h2>
        <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
          Demo fee structures, collections, payroll, and expenses for tenant <strong>{tenant || "—"}</strong>. Use this
          view to validate how records appear in the UI.
        </p>
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <FinancePanel
          title="Fee structures"
          description="Recurring fee definitions (amount & due date)."
          query={fees}
          emptyHint="No fee structures yet — restart the API with profile dev or check SUPER_ADMIN access."
        >
          {(rows) => (
            <table className="w-full text-left text-sm">
              <thead>
                <tr className="border-b border-slate-200 text-xs uppercase tracking-wide text-slate-500 dark:border-slate-700">
                  <th className="py-2 pr-3">Name</th>
                  <th className="py-2 pr-3">Amount</th>
                  <th className="py-2">Due</th>
                </tr>
              </thead>
              <tbody>
                {rows.map((r) => (
                  <tr key={r.id} className="border-b border-slate-100 dark:border-slate-800">
                    <td className="py-2.5 pr-3 font-medium text-slate-900 dark:text-slate-100">{r.name}</td>
                    <td className="py-2.5 pr-3 tabular-nums text-slate-700 dark:text-slate-300">
                      ₹{Number(r.amount).toLocaleString()}
                    </td>
                    <td className="py-2.5 text-slate-600 dark:text-slate-400">{r.dueDate?.slice?.(0, 10) ?? "—"}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </FinancePanel>

        <FinancePanel
          title="Fee payments"
          description="Per-student fee rows (PAID / DUE)."
          query={pays}
          emptyHint="No payments — ensure demo seed ran and X-Tenant-Slug is demo."
        >
          {(rows) => (
            <div className="max-h-80 overflow-auto">
              <table className="w-full text-left text-sm">
                <thead className="sticky top-0 bg-white/95 dark:bg-slate-900/95">
                  <tr className="border-b border-slate-200 text-xs uppercase tracking-wide text-slate-500 dark:border-slate-700">
                    <th className="py-2 pr-2">Student</th>
                    <th className="py-2 pr-2">Fee</th>
                    <th className="py-2 pr-2">Amount</th>
                    <th className="py-2">Status</th>
                  </tr>
                </thead>
                <tbody>
                  {rows.slice(0, 40).map((r) => (
                    <tr key={r.id} className="border-b border-slate-100 dark:border-slate-800">
                      <td className="max-w-[140px] truncate py-2 pr-2 text-slate-900 dark:text-slate-100">
                        {r.student?.fullName ?? "—"}
                      </td>
                      <td className="max-w-[100px] truncate py-2 pr-2 text-slate-600 dark:text-slate-400">
                        {r.feeStructure?.name ?? "—"}
                      </td>
                      <td className="py-2 pr-2 tabular-nums">₹{Number(r.amount).toLocaleString()}</td>
                      <td className="py-2">
                        <span
                          className={cn(
                            "rounded-full px-2 py-0.5 text-xs font-medium",
                            String(r.status).toUpperCase() === "PAID"
                              ? "bg-emerald-100 text-emerald-800 dark:bg-emerald-950 dark:text-emerald-200"
                              : "bg-amber-100 text-amber-900 dark:bg-amber-950 dark:text-amber-200"
                          )}
                        >
                          {r.status}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
              {rows.length > 40 && (
                <p className="mt-2 text-xs text-slate-500">Showing first 40 of {rows.length} rows.</p>
              )}
            </div>
          )}
        </FinancePanel>

        <FinancePanel
          title="Salary payments"
          description="Teacher payroll entries (demo)."
          query={sal}
          emptyHint="No salary rows seeded for this tenant."
        >
          {(rows) => (
            <div className="max-h-80 overflow-auto">
              <table className="w-full text-left text-sm">
                <thead className="sticky top-0 bg-white/95 dark:bg-slate-900/95">
                  <tr className="border-b border-slate-200 text-xs uppercase tracking-wide text-slate-500 dark:border-slate-700">
                    <th className="py-2 pr-2">Teacher</th>
                    <th className="py-2 pr-2">Month</th>
                    <th className="py-2 pr-2">Amount</th>
                    <th className="py-2">Status</th>
                  </tr>
                </thead>
                <tbody>
                  {rows.slice(0, 30).map((r) => (
                    <tr key={r.id} className="border-b border-slate-100 dark:border-slate-800">
                      <td className="py-2 pr-2 font-medium text-slate-900 dark:text-slate-100">
                        {r.teacher?.fullName ?? "—"}
                      </td>
                      <td className="py-2 pr-2 text-slate-600 dark:text-slate-400">{r.monthYear ?? "—"}</td>
                      <td className="py-2 pr-2 tabular-nums">₹{Number(r.amount).toLocaleString()}</td>
                      <td className="py-2 text-xs">{r.status ?? "—"}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </FinancePanel>

        <FinancePanel
          title="Expenses"
          description="Operating expenses by category."
          query={exp}
          emptyHint="No expenses — run demo replenish or check tenant."
        >
          {(rows) => (
            <table className="w-full text-left text-sm">
              <thead>
                <tr className="border-b border-slate-200 text-xs uppercase tracking-wide text-slate-500 dark:border-slate-700">
                  <th className="py-2 pr-2">Category</th>
                  <th className="py-2 pr-2">Amount</th>
                  <th className="py-2">Date</th>
                </tr>
              </thead>
              <tbody>
                {rows.map((r) => (
                  <tr key={r.id} className="border-b border-slate-100 dark:border-slate-800">
                    <td className="py-2.5 pr-2 font-medium text-slate-900 dark:text-slate-100">{r.category}</td>
                    <td className="py-2.5 pr-2 tabular-nums">₹{Number(r.amount).toLocaleString()}</td>
                    <td className="py-2.5 text-slate-600 dark:text-slate-400">{r.expenseDate?.slice?.(0, 10) ?? "—"}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </FinancePanel>
      </div>
    </div>
  );
}

function FinancePanel<T>({
  title,
  description,
  query,
  emptyHint,
  children,
}: {
  title: string;
  description: string;
  query: UseQueryResult<T[]>;
  emptyHint: string;
  children: (rows: T[]) => ReactNode;
}) {
  return (
    <div className="surface-card flex flex-col p-5">
      <div className="mb-4">
        <h3 className="font-bold text-slate-900 dark:text-white">{title}</h3>
        <p className="text-xs text-slate-500 dark:text-slate-400">{description}</p>
      </div>
      {query.isLoading ? (
        <p className="text-sm text-slate-500">Loading…</p>
      ) : query.isError ? (
        <div className="rounded-xl border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-800 dark:border-red-900 dark:bg-red-950/40 dark:text-red-200">
          Could not load this section. Super-admin needs access to fee structures, or your session may be missing tenant{" "}
          <code className="rounded bg-red-100 px-1 dark:bg-red-900">demo</code>.{" "}
          {query.error instanceof Error ? query.error.message : ""}
        </div>
      ) : !query.data?.length ? (
        <div className="rounded-xl border border-dashed border-slate-200 bg-slate-50/80 px-3 py-4 text-sm text-slate-600 dark:border-slate-700 dark:bg-slate-900/50 dark:text-slate-400">
          <p className="font-medium text-slate-700 dark:text-slate-300">No rows yet</p>
          <p className="mt-1 text-xs">{emptyHint}</p>
        </div>
      ) : (
        children(query.data)
      )}
    </div>
  );
}
