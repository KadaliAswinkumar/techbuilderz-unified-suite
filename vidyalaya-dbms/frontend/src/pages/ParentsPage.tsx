import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import { Filter, Plus, Search, X } from "lucide-react";
import { api } from "@/lib/api";
import { cn } from "@/lib/utils";
import type { ParentDto } from "@/types/people";

function shortId(id: string) {
  return id.replace(/-/g, "").slice(-4).toUpperCase();
}

export function ParentsPage() {
  const qc = useQueryClient();
  const [q, setQ] = useState("");
  const [kids, setKids] = useState<"" | "any" | "none">("");
  const [modal, setModal] = useState(false);

  const { data, isLoading } = useQuery({
    queryKey: ["parents"],
    queryFn: async () => (await api.get("/parents")).data as ParentDto[],
  });

  const filtered = useMemo(() => {
    const list = data ?? [];
    const qq = q.trim().toLowerCase();
    return list.filter((p) => {
      const blob = `${p.fullName} ${p.email ?? ""} ${p.phone ?? ""} ${p.occupation ?? ""}`.toLowerCase();
      const hit = !qq || blob.includes(qq);
      const c = p.childrenCount ?? 0;
      const kidsOk =
        kids === "" ? true : kids === "any" ? c > 0 : kids === "none" ? c === 0 : true;
      return hit && kidsOk;
    });
  }, [data, q, kids]);

  const create = useMutation({
    mutationFn: async (body: Record<string, string | null | undefined>) =>
      (await api.post("/parents", body)).data as ParentDto,
    onSuccess: async (row) => {
      setModal(false);
      await qc.invalidateQueries({ queryKey: ["parents"] });
      window.location.assign(`/parents/${row.id}`);
    },
  });

  return (
    <div className="space-y-5">
      <div className="surface-card p-4 md:p-5">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <h1 className="text-lg font-semibold tracking-tight text-slate-900 dark:text-slate-100">Parents</h1>
            <p className="mt-1 max-w-2xl text-sm text-slate-500 dark:text-slate-400">
              Search by name or contact, filter by linked children, then open a profile that matches the dashboard layout.
            </p>
          </div>
          <button
            type="button"
            onClick={() => setModal(true)}
            className="inline-flex items-center justify-center gap-2 rounded-2xl bg-[hsl(var(--accent))] px-5 py-2.5 text-sm font-semibold text-white shadow-md shadow-orange-500/20"
          >
            <Plus className="h-4 w-4" />
            Add parent
          </button>
        </div>
        <div className="mt-5 grid gap-3 md:grid-cols-2">
          <label className="flex flex-col rounded-2xl border border-slate-200 bg-slate-50/80 p-3 dark:border-slate-700 dark:bg-slate-800/40">
            <span className="mb-1 flex items-center gap-1 text-xs font-medium text-slate-500 dark:text-slate-400">
              <Search className="h-3.5 w-3.5" /> Search
            </span>
            <input
              value={q}
              onChange={(e) => setQ(e.target.value)}
              placeholder="Name, email, phone, occupation…"
              className="rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm outline-none focus:border-[hsl(var(--brand))] dark:border-slate-600 dark:bg-slate-950 dark:text-slate-100"
            />
          </label>
          <label className="flex flex-col rounded-2xl border border-slate-200 bg-slate-50/80 p-3 dark:border-slate-700 dark:bg-slate-800/40">
            <span className="mb-1 flex items-center gap-1 text-xs font-medium text-slate-500 dark:text-slate-400">
              <Filter className="h-3.5 w-3.5" /> Children
            </span>
            <select
              value={kids}
              onChange={(e) => setKids(e.target.value as "" | "any" | "none")}
              className="rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm dark:border-slate-600 dark:bg-slate-950 dark:text-slate-100"
            >
              <option value="">All parents</option>
              <option value="any">Has linked students</option>
              <option value="none">No children linked</option>
            </select>
          </label>
        </div>
      </div>

      <div className="text-sm text-slate-500 dark:text-slate-400">
        Showing <span className="font-semibold text-slate-800 dark:text-slate-200">{filtered.length}</span> of{" "}
        <span className="font-semibold text-slate-800 dark:text-slate-200">{data?.length ?? 0}</span>
      </div>

      {isLoading ? (
        <div className="surface-card p-10 text-center text-sm text-slate-500">Loading parents…</div>
      ) : filtered.length === 0 ? (
        <div className="surface-card p-10 text-center text-sm text-slate-500">No parents match these filters.</div>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
          {filtered.map((p) => (
            <Link
              key={p.id}
              to={`/parents/${p.id}`}
              className="group surface-card overflow-hidden transition hover:shadow-md dark:hover:shadow-lg dark:hover:shadow-slate-950/40"
            >
              <div className="flex items-start gap-4 p-4">
                <div className="h-16 w-16 shrink-0 rounded-full bg-gradient-to-br from-sky-100 to-slate-100 p-[3px] dark:from-sky-900/40 dark:to-slate-800">
                  <div className="flex h-full w-full items-center justify-center rounded-full bg-white text-sm font-semibold text-sky-800 dark:bg-slate-900 dark:text-sky-200">
                    {(p.fullName?.[0] ?? "?").toUpperCase()}
                  </div>
                </div>
                <div className="min-w-0 flex-1">
                  <div className="flex items-baseline gap-2">
                    <span className="truncate font-semibold text-slate-900 dark:text-slate-100">{p.fullName}</span>
                    <span className="shrink-0 text-xs font-medium text-[#ea7c4d]">({shortId(p.id)})</span>
                  </div>
                  <p className="mt-0.5 truncate text-xs text-slate-500 dark:text-slate-400">{p.email}</p>
                  <p className="mt-1 text-[11px] text-slate-600 dark:text-slate-300">
                    {p.childrenCount ?? 0} linked student{(p.childrenCount ?? 0) === 1 ? "" : "s"}
                    {p.occupation ? ` · ${p.occupation}` : ""}
                  </p>
                </div>
              </div>
              <div className="border-t border-slate-100 bg-slate-50/60 px-4 py-2 text-[11px] text-slate-500 dark:border-slate-800 dark:bg-slate-800/40 dark:text-slate-400">
                Open parent dashboard (kids, fees, exams)
              </div>
            </Link>
          ))}
        </div>
      )}

      {modal ? (
        <ParentCreateModal
          busy={create.isPending}
          error={create.isError ? String(create.error) : null}
          onClose={() => setModal(false)}
          onSubmit={(b) => create.mutate(b)}
        />
      ) : null}
    </div>
  );
}

function pf() {
  return "mt-1 w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm outline-none focus:border-[hsl(var(--brand))] dark:border-slate-600 dark:bg-slate-950 dark:text-slate-100";
}

function ParentCreateModal({
  onClose,
  onSubmit,
  busy,
  error,
}: {
  onClose: () => void;
  onSubmit: (b: Record<string, string | null | undefined>) => void;
  busy: boolean;
  error: string | null;
}) {
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [address, setAddress] = useState("");
  const [occupation, setOccupation] = useState("");
  const [employer, setEmployer] = useState("");
  const [educationSummary, setEducationSummary] = useState("");

  return (
    <div className="fixed inset-0 z-[70] flex items-end justify-center bg-black/45 p-4 sm:items-center">
      <div className="max-h-[92vh] w-full max-w-2xl overflow-y-auto rounded-3xl border border-slate-200 bg-white shadow-2xl dark:border-slate-700 dark:bg-slate-900">
        <div className="sticky top-0 flex items-center justify-between border-b border-slate-100 bg-white px-5 py-4 dark:border-slate-800 dark:bg-slate-900">
          <div>
            <h2 className="text-base font-semibold text-slate-900 dark:text-slate-100">New parent</h2>
            <p className="text-xs text-slate-500 dark:text-slate-400">Work, employer, and education context for the household.</p>
          </div>
          <button type="button" onClick={onClose} className="rounded-xl p-2 hover:bg-slate-100 dark:hover:bg-slate-800">
            <X className="h-5 w-5" />
          </button>
        </div>
        <form
          className="space-y-4 p-5"
          onSubmit={(e) => {
            e.preventDefault();
            if (!fullName.trim()) return;
            onSubmit({
              fullName: fullName.trim(),
              email: email || null,
              phone: phone || null,
              address: address || null,
              occupation: occupation || null,
              employer: employer || null,
              educationSummary: educationSummary || null,
              photoUrl: null,
              socialLinks: null,
            });
          }}
        >
          {error ? <p className="rounded-xl bg-red-50 px-3 py-2 text-xs text-red-700 dark:bg-red-950/40 dark:text-red-300">{error}</p> : null}
          <div className="grid gap-4 md:grid-cols-2">
            <label className="text-xs font-medium text-slate-600 dark:text-slate-300 md:col-span-2">
              Full name *
              <input required className={pf()} value={fullName} onChange={(e) => setFullName(e.target.value)} />
            </label>
            <label className="text-xs font-medium text-slate-600 dark:text-slate-300">
              Email
              <input type="email" className={pf()} value={email} onChange={(e) => setEmail(e.target.value)} />
            </label>
            <label className="text-xs font-medium text-slate-600 dark:text-slate-300">
              Phone
              <input className={pf()} value={phone} onChange={(e) => setPhone(e.target.value)} />
            </label>
            <label className="text-xs font-medium text-slate-600 dark:text-slate-300 md:col-span-2">
              Address
              <textarea rows={2} className={pf()} value={address} onChange={(e) => setAddress(e.target.value)} />
            </label>
            <label className="text-xs font-medium text-slate-600 dark:text-slate-300">
              Occupation
              <input className={pf()} value={occupation} onChange={(e) => setOccupation(e.target.value)} />
            </label>
            <label className="text-xs font-medium text-slate-600 dark:text-slate-300">
              Employer
              <input className={pf()} value={employer} onChange={(e) => setEmployer(e.target.value)} />
            </label>
            <label className="text-xs font-medium text-slate-600 dark:text-slate-300 md:col-span-2">
              Education &amp; background
              <textarea
                rows={4}
                className={pf()}
                placeholder="Degrees, certifications, languages spoken, volunteer roles…"
                value={educationSummary}
                onChange={(e) => setEducationSummary(e.target.value)}
              />
            </label>
          </div>
          <div className="flex justify-end gap-2 border-t border-slate-100 pt-4 dark:border-slate-800">
            <button type="button" onClick={onClose} className="rounded-2xl border border-slate-200 px-4 py-2 text-sm font-medium dark:border-slate-600">
              Cancel
            </button>
            <button type="submit" disabled={busy} className={cn("rounded-2xl bg-[hsl(var(--accent))] px-5 py-2 text-sm font-semibold text-white", busy && "opacity-60")}>
              {busy ? "Saving…" : "Save parent"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
