import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";

export function ClassPage() {
  const { data } = useQuery({
    queryKey: ["school-classes"],
    queryFn: async () => (await api.get("/school-classes")).data,
  });

  return (
    <div className="surface-card p-4">
      <h1 className="mb-4 text-lg font-semibold">Classes</h1>
      <ul className="space-y-2">
        {(data as { id: string; name: string }[] | undefined)?.map((c) => (
          <li key={c.id} className="rounded-xl border border-slate-100 px-3 py-2">
            {c.name}
          </li>
        )) ?? <li className="text-sm text-slate-500">No data</li>}
      </ul>
    </div>
  );
}
