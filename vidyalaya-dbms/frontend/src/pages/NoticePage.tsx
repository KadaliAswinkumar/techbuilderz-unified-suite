import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";

export function NoticePage() {
  const { data } = useQuery({
    queryKey: ["notices"],
    queryFn: async () => (await api.get("/notices")).data,
  });

  return (
    <div className="surface-card p-4">
      <h1 className="mb-4 text-lg font-semibold">Notices</h1>
      <div className="space-y-3">
        {(data as { id: string; title: string; body?: string }[] | undefined)?.map((n) => (
          <div key={n.id} className="rounded-xl border border-slate-100 p-3">
            <div className="font-semibold">{n.title}</div>
            <p className="mt-1 text-sm text-slate-600">{n.body}</p>
          </div>
        )) ?? null}
      </div>
    </div>
  );
}
