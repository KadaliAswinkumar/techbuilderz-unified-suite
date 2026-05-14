import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import { Filter, GraduationCap, Plus, Search, X } from "lucide-react";
import { api } from "@/lib/api";
import { cn } from "@/lib/utils";
import type { StudentDto } from "@/types/people";

function shortId(id: string) {
  return id.replace(/-/g, "").slice(-4).toUpperCase();
}

function admissionYear(s: StudentDto) {
  if (!s.admissionDate) return "";
  return String(s.admissionDate).slice(0, 4);
}

export function StudentsPage() {
  const qc = useQueryClient();
  const [q, setQ] = useState("");
  const [cls, setCls] = useState("");
  const [sec, setSec] = useState("");
  const [yr, setYr] = useState("");
  const [modal, setModal] = useState(false);

  const { data, isLoading } = useQuery({
    queryKey: ["students"],
    queryFn: async () => (await api.get("/students")).data as StudentDto[],
  });

  const classes = useMemo(() => {
    const set = new Set<string>();
    (data ?? []).forEach((s) => {
      if (s.className) set.add(s.className);
    });
    return Array.from(set).sort();
  }, [data]);

  const sections = useMemo(() => {
    const set = new Set<string>();
    (data ?? []).forEach((s) => {
      if (s.section) set.add(s.section);
    });
    return Array.from(set).sort();
  }, [data]);

  const years = useMemo(() => {
    const set = new Set<string>();
    (data ?? []).forEach((s) => {
      const y = admissionYear(s);
      if (y) set.add(y);
    });
    return Array.from(set).sort().reverse();
  }, [data]);

  const filtered = useMemo(() => {
    const list = data ?? [];
    const qq = q.trim().toLowerCase();
    return list.filter((s) => {
      const name = (s.fullName ?? "").toLowerCase();
      const hit =
        !qq ||
        name.includes(qq) ||
        (s.email ?? "").toLowerCase().includes(qq) ||
        (s.phone ?? "").toLowerCase().includes(qq);
      const cOk = !cls || s.className === cls;
      const sOk = !sec || s.section === sec;
      const yOk = !yr || admissionYear(s) === yr;
      return hit && cOk && sOk && yOk;
    });
  }, [data, q, cls, sec, yr]);

  const create = useMutation({
    mutationFn: async (body: Record<string, string | null | undefined>) => (await api.post("/students", body)).data as StudentDto,
    onSuccess: async (row) => {
      setModal(false);
      await qc.invalidateQueries({ queryKey: ["students"] });
      window.location.assign(`/students/${row.id}`);
    },
  });

  return (
    <div className="space-y-5">
      <div className="surface-card p-4 md:p-5">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <h1 className="text-lg font-semibold tracking-tight text-slate-900 dark:text-slate-100">Students</h1>
            <p className="mt-1 max-w-2xl text-sm text-slate-500 dark:text-slate-400">
              Search by name or contact, filter by class, section, and admission year. Cards match the school directory layout.
            </p>
          </div>
          <button
            type="button"
            onClick={() => setModal(true)}
            className="inline-flex items-center justify-center gap-2 rounded-2xl bg-[hsl(var(--accent))] px-5 py-2.5 text-sm font-semibold text-white shadow-md shadow-orange-500/20 transition hover:opacity-95"
          >
            <Plus className="h-4 w-4" />
            Add student
          </button>
        </div>

        <div className="mt-5 grid gap-3 md:grid-cols-2 xl:grid-cols-4">
          <label className="flex flex-col rounded-2xl border border-slate-200 bg-slate-50/80 p-3 dark:border-slate-700 dark:bg-slate-800/40">
            <span className="mb-1 flex items-center gap-1 text-xs font-medium text-slate-500 dark:text-slate-400">
              <Search className="h-3.5 w-3.5" /> Search
            </span>
            <input
              value={q}
              onChange={(e) => setQ(e.target.value)}
              placeholder="Name, email, phone…"
              className="rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm outline-none focus:border-[hsl(var(--brand))] dark:border-slate-600 dark:bg-slate-950 dark:text-slate-100"
            />
          </label>
          <label className="flex flex-col rounded-2xl border border-slate-200 bg-slate-50/80 p-3 dark:border-slate-700 dark:bg-slate-800/40">
            <span className="mb-1 flex items-center gap-1 text-xs font-medium text-slate-500 dark:text-slate-400">
              <GraduationCap className="h-3.5 w-3.5" /> Class
            </span>
            <select
              value={cls}
              onChange={(e) => setCls(e.target.value)}
              className="rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm dark:border-slate-600 dark:bg-slate-950 dark:text-slate-100"
            >
              <option value="">All classes</option>
              {classes.map((c) => (
                <option key={c} value={c}>
                  {c}
                </option>
              ))}
            </select>
          </label>
          <label className="flex flex-col rounded-2xl border border-slate-200 bg-slate-50/80 p-3 dark:border-slate-700 dark:bg-slate-800/40">
            <span className="mb-1 flex items-center gap-1 text-xs font-medium text-slate-500 dark:text-slate-400">
              <Filter className="h-3.5 w-3.5" /> Section
            </span>
            <select
              value={sec}
              onChange={(e) => setSec(e.target.value)}
              className="rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm dark:border-slate-600 dark:bg-slate-950 dark:text-slate-100"
            >
              <option value="">All sections</option>
              {sections.map((s) => (
                <option key={s} value={s}>
                  {s}
                </option>
              ))}
            </select>
          </label>
          <label className="flex flex-col rounded-2xl border border-slate-200 bg-slate-50/80 p-3 dark:border-slate-700 dark:bg-slate-800/40">
            <span className="mb-1 text-xs font-medium text-slate-500 dark:text-slate-400">Admission year</span>
            <select
              value={yr}
              onChange={(e) => setYr(e.target.value)}
              className="rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm dark:border-slate-600 dark:bg-slate-950 dark:text-slate-100"
            >
              <option value="">Any year</option>
              {years.map((y) => (
                <option key={y} value={y}>
                  {y}
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
        <div className="surface-card p-10 text-center text-sm text-slate-500">Loading students…</div>
      ) : filtered.length === 0 ? (
        <div className="surface-card p-10 text-center text-sm text-slate-500">
          No students match these filters. Try clearing filters or add a student.
        </div>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
          {filtered.map((s) => (
            <Link
              key={s.id}
              to={`/students/${s.id}`}
              className="group surface-card overflow-hidden transition hover:shadow-md dark:hover:shadow-lg dark:hover:shadow-slate-950/40"
            >
              <div className="flex items-start gap-4 p-4">
                <div className="relative shrink-0">
                  <div className="h-16 w-16 rounded-full bg-gradient-to-br from-orange-200 via-white to-indigo-100 p-[3px] dark:from-orange-900/40 dark:via-slate-800 dark:to-indigo-900/40">
                    <div className="flex h-full w-full items-center justify-center rounded-full bg-slate-100 text-sm font-semibold text-slate-600 dark:bg-slate-800 dark:text-slate-300">
                      {(s.firstName?.[0] ?? s.fullName?.[0] ?? "?").toUpperCase()}
                    </div>
                  </div>
                </div>
                <div className="min-w-0 flex-1">
                  <div className="flex items-baseline gap-2">
                    <span className="truncate font-semibold text-slate-900 dark:text-slate-100">{s.fullName}</span>
                    <span className="shrink-0 text-xs font-medium text-[#ea7c4d]">({shortId(s.id)})</span>
                  </div>
                  <p className="mt-0.5 truncate text-xs text-slate-500 dark:text-slate-400">{s.email}</p>
                  <div className="mt-2 flex flex-wrap gap-2">
                    <span className="rounded-full bg-orange-50 px-2.5 py-0.5 text-[11px] font-medium text-orange-800 dark:bg-orange-950/50 dark:text-orange-200">
                      {s.className ?? "—"}
                    </span>
                    <span className="rounded-full bg-slate-100 px-2.5 py-0.5 text-[11px] font-medium text-slate-700 dark:bg-slate-800 dark:text-slate-300">
                      Sec {s.section ?? "—"}
                    </span>
                  </div>
                </div>
              </div>
              <div className="border-t border-slate-100 bg-slate-50/60 px-4 py-2 text-[11px] text-slate-500 dark:border-slate-800 dark:bg-slate-800/40 dark:text-slate-400">
                Tap to open full bio &amp; records
              </div>
            </Link>
          ))}
        </div>
      )}

      {modal ? (
        <StudentCreateModal
          busy={create.isPending}
          error={create.isError ? String(create.error) : null}
          onClose={() => setModal(false)}
          onSubmit={(payload) => create.mutate(payload)}
        />
      ) : null}
    </div>
  );
}

function fieldClass() {
  return "mt-1 w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm outline-none focus:border-[hsl(var(--brand))] dark:border-slate-600 dark:bg-slate-950 dark:text-slate-100";
}

function StudentCreateModal({
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
  const [firstName, setFirstName] = useState("");
  const [middleName, setMiddleName] = useState("");
  const [lastName, setLastName] = useState("");
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [gender, setGender] = useState("Male");
  const [fatherName, setFatherName] = useState("");
  const [motherName, setMotherName] = useState("");
  const [fatherOccupation, setFatherOccupation] = useState("");
  const [motherOccupation, setMotherOccupation] = useState("");
  const [dateOfBirth, setDateOfBirth] = useState("");
  const [religion, setReligion] = useState("");
  const [caste, setCaste] = useState("");
  const [address, setAddress] = useState("");
  const [className, setClassName] = useState("Grade VI");
  const [section, setSection] = useState("A");
  const [admissionDate, setAdmissionDate] = useState("");
  const [aboutStudent, setAboutStudent] = useState("");
  const [photoUrl, setPhotoUrl] = useState("");
  const [socialLinks, setSocialLinks] = useState("");

  return (
    <div className="fixed inset-0 z-[70] flex items-end justify-center bg-black/45 p-4 sm:items-center">
      <div className="max-h-[92vh] w-full max-w-3xl overflow-y-auto rounded-3xl border border-slate-200 bg-white shadow-2xl dark:border-slate-700 dark:bg-slate-900">
        <div className="sticky top-0 flex items-center justify-between border-b border-slate-100 bg-white px-5 py-4 dark:border-slate-800 dark:bg-slate-900">
          <div>
            <h2 className="text-base font-semibold text-slate-900 dark:text-slate-100">New student</h2>
            <p className="text-xs text-slate-500 dark:text-slate-400">Capture full profile — matches admin records.</p>
          </div>
          <button type="button" onClick={onClose} className="rounded-xl p-2 hover:bg-slate-100 dark:hover:bg-slate-800">
            <X className="h-5 w-5" />
          </button>
        </div>
        <form
          className="space-y-4 p-5"
          onSubmit={(e) => {
            e.preventDefault();
            const fn =
              fullName.trim() ||
              [firstName, middleName, lastName]
                .map((x) => x.trim())
                .filter(Boolean)
                .join(" ")
                .trim();
            if (!fn) return;
            onSubmit({
              fullName: fn,
              firstName: firstName || null,
              middleName: middleName || null,
              lastName: lastName || null,
              email: email || null,
              phone: phone || null,
              gender,
              fatherName: fatherName || null,
              motherName: motherName || null,
              fatherOccupation: fatherOccupation || null,
              motherOccupation: motherOccupation || null,
              dateOfBirth: dateOfBirth || null,
              religion: religion || null,
              caste: caste || null,
              address: address || null,
              className: className || null,
              section: section || null,
              admissionDate: admissionDate || null,
              aboutStudent: aboutStudent || null,
              photoUrl: photoUrl || null,
              socialLinks: socialLinks || null,
            });
          }}
        >
          {error ? <p className="rounded-xl bg-red-50 px-3 py-2 text-xs text-red-700 dark:bg-red-950/40 dark:text-red-300">{error}</p> : null}
          <div className="grid gap-4 md:grid-cols-2">
            <label className="text-xs font-medium text-slate-600 dark:text-slate-300">
              Display full name *
              <input required className={fieldClass()} value={fullName} onChange={(e) => setFullName(e.target.value)} />
            </label>
            <label className="text-xs font-medium text-slate-600 dark:text-slate-300">
              First name
              <input className={fieldClass()} value={firstName} onChange={(e) => setFirstName(e.target.value)} />
            </label>
            <label className="text-xs font-medium text-slate-600 dark:text-slate-300">
              Middle name
              <input className={fieldClass()} value={middleName} onChange={(e) => setMiddleName(e.target.value)} />
            </label>
            <label className="text-xs font-medium text-slate-600 dark:text-slate-300">
              Last name
              <input className={fieldClass()} value={lastName} onChange={(e) => setLastName(e.target.value)} />
            </label>
            <label className="text-xs font-medium text-slate-600 dark:text-slate-300">
              Email
              <input type="email" className={fieldClass()} value={email} onChange={(e) => setEmail(e.target.value)} />
            </label>
            <label className="text-xs font-medium text-slate-600 dark:text-slate-300">
              Phone
              <input className={fieldClass()} value={phone} onChange={(e) => setPhone(e.target.value)} />
            </label>
            <label className="text-xs font-medium text-slate-600 dark:text-slate-300">
              Gender
              <select className={fieldClass()} value={gender} onChange={(e) => setGender(e.target.value)}>
                <option>Male</option>
                <option>Female</option>
                <option>Other</option>
              </select>
            </label>
            <label className="text-xs font-medium text-slate-600 dark:text-slate-300">
              Date of birth
              <input type="date" className={fieldClass()} value={dateOfBirth} onChange={(e) => setDateOfBirth(e.target.value)} />
            </label>
            <label className="text-xs font-medium text-slate-600 dark:text-slate-300">
              Father&apos;s name
              <input className={fieldClass()} value={fatherName} onChange={(e) => setFatherName(e.target.value)} />
            </label>
            <label className="text-xs font-medium text-slate-600 dark:text-slate-300">
              Mother&apos;s name
              <input className={fieldClass()} value={motherName} onChange={(e) => setMotherName(e.target.value)} />
            </label>
            <label className="text-xs font-medium text-slate-600 dark:text-slate-300">
              Father&apos;s occupation
              <input className={fieldClass()} value={fatherOccupation} onChange={(e) => setFatherOccupation(e.target.value)} />
            </label>
            <label className="text-xs font-medium text-slate-600 dark:text-slate-300">
              Mother&apos;s occupation
              <input className={fieldClass()} value={motherOccupation} onChange={(e) => setMotherOccupation(e.target.value)} />
            </label>
            <label className="text-xs font-medium text-slate-600 dark:text-slate-300">
              Religion
              <input className={fieldClass()} value={religion} onChange={(e) => setReligion(e.target.value)} />
            </label>
            <label className="text-xs font-medium text-slate-600 dark:text-slate-300">
              Caste / community
              <input className={fieldClass()} value={caste} onChange={(e) => setCaste(e.target.value)} />
            </label>
            <label className="text-xs font-medium text-slate-600 dark:text-slate-300 md:col-span-2">
              Address
              <textarea rows={2} className={fieldClass()} value={address} onChange={(e) => setAddress(e.target.value)} />
            </label>
            <label className="text-xs font-medium text-slate-600 dark:text-slate-300">
              Class
              <input className={fieldClass()} value={className} onChange={(e) => setClassName(e.target.value)} />
            </label>
            <label className="text-xs font-medium text-slate-600 dark:text-slate-300">
              Section
              <input className={fieldClass()} value={section} onChange={(e) => setSection(e.target.value)} />
            </label>
            <label className="text-xs font-medium text-slate-600 dark:text-slate-300">
              Admission date
              <input type="date" className={fieldClass()} value={admissionDate} onChange={(e) => setAdmissionDate(e.target.value)} />
            </label>
            <label className="text-xs font-medium text-slate-600 dark:text-slate-300 md:col-span-2">
              Photo URL
              <input className={fieldClass()} value={photoUrl} onChange={(e) => setPhotoUrl(e.target.value)} />
            </label>
            <label className="text-xs font-medium text-slate-600 dark:text-slate-300 md:col-span-2">
              Social links (JSON or URLs)
              <input className={fieldClass()} value={socialLinks} onChange={(e) => setSocialLinks(e.target.value)} />
            </label>
            <label className="text-xs font-medium text-slate-600 dark:text-slate-300 md:col-span-2">
              About student
              <textarea rows={3} className={fieldClass()} value={aboutStudent} onChange={(e) => setAboutStudent(e.target.value)} />
            </label>
          </div>
          <div className="flex justify-end gap-2 border-t border-slate-100 pt-4 dark:border-slate-800">
            <button
              type="button"
              onClick={onClose}
              className="rounded-2xl border border-slate-200 px-4 py-2 text-sm font-medium text-slate-700 dark:border-slate-600 dark:text-slate-300"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={busy}
              className={cn(
                "rounded-2xl bg-[hsl(var(--accent))] px-5 py-2 text-sm font-semibold text-white",
                busy && "opacity-60"
              )}
            >
              {busy ? "Saving…" : "Save student"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
