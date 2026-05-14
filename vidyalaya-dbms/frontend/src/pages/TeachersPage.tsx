import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import { Filter, Plus, Search, X } from "lucide-react";
import { api } from "@/lib/api";
import { cn } from "@/lib/utils";
import type { TeacherDto } from "@/types/people";

function shortId(id: string) {
  return id.replace(/-/g, "").slice(-4).toUpperCase();
}

export function TeachersPage() {
  const qc = useQueryClient();
  const [q, setQ] = useState("");
  const [qualFilter, setQualFilter] = useState("");
  const [modal, setModal] = useState(false);

  const { data, isLoading } = useQuery({
    queryKey: ["teachers"],
    queryFn: async () => (await api.get("/teachers")).data as TeacherDto[],
  });

  const quals = useMemo(() => {
    const set = new Set<string>();
    (data ?? []).forEach((t) => {
      if (t.qualification) set.add(t.qualification);
    });
    return Array.from(set).sort();
  }, [data]);

  const filtered = useMemo(() => {
    const list = data ?? [];
    const qq = q.trim().toLowerCase();
    return list.filter((t) => {
      const blob = `${t.fullName} ${t.email ?? ""} ${t.phone ?? ""} ${t.qualification ?? ""}`.toLowerCase();
      const hit = !qq || blob.includes(qq);
      const qOk = !qualFilter || (t.qualification ?? "").includes(qualFilter);
      return hit && qOk;
    });
  }, [data, q, qualFilter]);

  const create = useMutation({
    mutationFn: async (body: Record<string, string | number | null | undefined>) =>
      (await api.post("/teachers", body)).data as TeacherDto,
    onSuccess: async (row) => {
      setModal(false);
      await qc.invalidateQueries({ queryKey: ["teachers"] });
      window.location.assign(`/teachers/${row.id}`);
    },
  });

  return (
    <div className="space-y-5">
      <div className="surface-card p-4 md:p-5">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <h1 className="text-lg font-semibold tracking-tight text-slate-900 dark:text-slate-100">Teachers</h1>
            <p className="mt-1 max-w-2xl text-sm text-slate-500 dark:text-slate-400">
              Directory with search and education filter. Detail pages follow the ACERO bio layout.
            </p>
          </div>
          <button
            type="button"
            onClick={() => setModal(true)}
            className="inline-flex items-center justify-center gap-2 rounded-2xl bg-[hsl(var(--accent))] px-5 py-2.5 text-sm font-semibold text-white shadow-md shadow-orange-500/20"
          >
            <Plus className="h-4 w-4" />
            Add teacher
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
              placeholder="Name, email, qualification…"
              className="rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm outline-none focus:border-[hsl(var(--brand))] dark:border-slate-600 dark:bg-slate-950 dark:text-slate-100"
            />
          </label>
          <label className="flex flex-col rounded-2xl border border-slate-200 bg-slate-50/80 p-3 dark:border-slate-700 dark:bg-slate-800/40">
            <span className="mb-1 flex items-center gap-1 text-xs font-medium text-slate-500 dark:text-slate-400">
              <Filter className="h-3.5 w-3.5" /> Education / qualification
            </span>
            <select
              value={qualFilter}
              onChange={(e) => setQualFilter(e.target.value)}
              className="rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm dark:border-slate-600 dark:bg-slate-950 dark:text-slate-100"
            >
              <option value="">All</option>
              {quals.map((c) => (
                <option key={c} value={c}>
                  {c}
                </option>
              ))}
            </select>
          </label>
        </div>
      </div>

      <div className="text-sm text-slate-500 dark:text-slate-400">
        Showing <span className="font-semibold text-slate-800 dark:text-slate-200">{filtered.length}</span> of{" "}
        <span className="font-semibold text-slate-800 dark:text-slate-200">{data?.length ?? 0}</span>
      </div>

      {isLoading ? (
        <div className="surface-card p-10 text-center text-sm text-slate-500">Loading teachers…</div>
      ) : filtered.length === 0 ? (
        <div className="surface-card p-10 text-center text-sm text-slate-500">No teachers match these filters.</div>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
          {filtered.map((t) => (
            <Link
              key={t.id}
              to={`/teachers/${t.id}`}
              className="group surface-card overflow-hidden transition hover:shadow-md dark:hover:shadow-lg dark:hover:shadow-slate-950/40"
            >
              <div className="flex items-start gap-4 p-4">
                <div className="h-16 w-16 shrink-0 rounded-full bg-gradient-to-br from-indigo-100 to-slate-100 p-[3px] dark:from-indigo-900/40 dark:to-slate-800">
                  <div className="flex h-full w-full items-center justify-center rounded-full bg-white text-sm font-semibold text-indigo-700 dark:bg-slate-900 dark:text-indigo-200">
                    {(t.fullName?.[0] ?? "?").toUpperCase()}
                  </div>
                </div>
                <div className="min-w-0 flex-1">
                  <div className="flex items-baseline gap-2">
                    <span className="truncate font-semibold text-slate-900 dark:text-slate-100">{t.fullName}</span>
                    <span className="shrink-0 text-xs font-medium text-[#ea7c4d]">({shortId(t.id)})</span>
                  </div>
                  <p className="mt-0.5 truncate text-xs text-slate-500 dark:text-slate-400">{t.email}</p>
                  <p className="mt-1 line-clamp-2 text-[11px] text-slate-600 dark:text-slate-300">{t.qualification ?? "—"}</p>
                </div>
              </div>
              <div className="border-t border-slate-100 bg-slate-50/60 px-4 py-2 text-[11px] text-slate-500 dark:border-slate-800 dark:bg-slate-800/40 dark:text-slate-400">
                View bio, groups &amp; exam duties
              </div>
            </Link>
          ))}
        </div>
      )}

      {modal ? (
        <TeacherCreateModal
          busy={create.isPending}
          error={create.isError ? String(create.error) : null}
          onClose={() => setModal(false)}
          onSubmit={(b) => create.mutate(b)}
        />
      ) : null}
    </div>
  );
}

function tf() {
  return "mt-1 w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm outline-none focus:border-[hsl(var(--brand))] dark:border-slate-600 dark:bg-slate-950 dark:text-slate-100";
}

function TeacherCreateModal({
  onClose,
  onSubmit,
  busy,
  error,
}: {
  onClose: () => void;
  onSubmit: (b: Record<string, string | number | null | undefined>) => void;
  busy: boolean;
  error: string | null;
}) {
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [gender, setGender] = useState("Female");
  const [address, setAddress] = useState("");
  const [qualification, setQualification] = useState("M.Ed Mathematics");
  const [experienceSummary, setExperienceSummary] = useState("");
  const [joiningDate, setJoiningDate] = useState("");
  const [dateOfBirth, setDateOfBirth] = useState("");
  const [salaryAmount, setSalaryAmount] = useState("40000");

  return (
    <div className="fixed inset-0 z-[70] flex items-end justify-center bg-black/45 p-4 sm:items-center">
      <div className="max-h-[92vh] w-full max-w-2xl overflow-y-auto rounded-3xl border border-slate-200 bg-white shadow-2xl dark:border-slate-700 dark:bg-slate-900">
        <div className="sticky top-0 flex items-center justify-between border-b border-slate-100 bg-white px-5 py-4 dark:border-slate-800 dark:bg-slate-900">
          <div>
            <h2 className="text-base font-semibold text-slate-900 dark:text-slate-100">New teacher</h2>
            <p className="text-xs text-slate-500 dark:text-slate-400">Education, experience, and payroll basics.</p>
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
              gender,
              dateOfBirth: dateOfBirth || null,
              address: address || null,
              qualification: qualification || null,
              experienceSummary: experienceSummary || null,
              joiningDate: joiningDate || null,
              salaryAmount: Number(salaryAmount) || 0,
              photoUrl: null,
              socialLinks: null,
            });
          }}
        >
          {error ? <p className="rounded-xl bg-red-50 px-3 py-2 text-xs text-red-700 dark:bg-red-950/40 dark:text-red-300">{error}</p> : null}
          <div className="grid gap-4 md:grid-cols-2">
            <label className="text-xs font-medium text-slate-600 dark:text-slate-300 md:col-span-2">
              Full name *
              <input required className={tf()} value={fullName} onChange={(e) => setFullName(e.target.value)} />
            </label>
            <label className="text-xs font-medium text-slate-600 dark:text-slate-300">
              Email
              <input type="email" className={tf()} value={email} onChange={(e) => setEmail(e.target.value)} />
            </label>
            <label className="text-xs font-medium text-slate-600 dark:text-slate-300">
              Phone
              <input className={tf()} value={phone} onChange={(e) => setPhone(e.target.value)} />
            </label>
            <label className="text-xs font-medium text-slate-600 dark:text-slate-300">
              Gender
              <select className={tf()} value={gender} onChange={(e) => setGender(e.target.value)}>
                <option>Female</option>
                <option>Male</option>
                <option>Other</option>
              </select>
            </label>
            <label className="text-xs font-medium text-slate-600 dark:text-slate-300">
              Joining date
              <input type="date" className={tf()} value={joiningDate} onChange={(e) => setJoiningDate(e.target.value)} />
            </label>
            <label className="text-xs font-medium text-slate-600 dark:text-slate-300">
              Date of birth
              <input type="date" className={tf()} value={dateOfBirth} onChange={(e) => setDateOfBirth(e.target.value)} />
            </label>
            <label className="text-xs font-medium text-slate-600 dark:text-slate-300 md:col-span-2">
              Address
              <textarea rows={2} className={tf()} value={address} onChange={(e) => setAddress(e.target.value)} />
            </label>
            <label className="text-xs font-medium text-slate-600 dark:text-slate-300 md:col-span-2">
              Education (qualification)
              <input className={tf()} value={qualification} onChange={(e) => setQualification(e.target.value)} />
            </label>
            <label className="text-xs font-medium text-slate-600 dark:text-slate-300 md:col-span-2">
              Experience summary
              <textarea
                rows={4}
                className={tf()}
                placeholder="Years taught, subjects, certifications, workshops…"
                value={experienceSummary}
                onChange={(e) => setExperienceSummary(e.target.value)}
              />
            </label>
            <label className="text-xs font-medium text-slate-600 dark:text-slate-300">
              Monthly salary
              <input type="number" className={tf()} value={salaryAmount} onChange={(e) => setSalaryAmount(e.target.value)} />
            </label>
          </div>
          <div className="flex justify-end gap-2 border-t border-slate-100 pt-4 dark:border-slate-800">
            <button type="button" onClick={onClose} className="rounded-2xl border border-slate-200 px-4 py-2 text-sm font-medium dark:border-slate-600">
              Cancel
            </button>
            <button type="submit" disabled={busy} className={cn("rounded-2xl bg-[hsl(var(--accent))] px-5 py-2 text-sm font-semibold text-white", busy && "opacity-60")}>
              {busy ? "Saving…" : "Save teacher"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
