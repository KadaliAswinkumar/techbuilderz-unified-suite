import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";

export function TransportPage() {
  const { data } = useQuery({
    queryKey: ["transport"],
    queryFn: async () => (await api.get("/transport/routes")).data,
  });

  return (
    <div className="surface-card p-4">
      <h1 className="mb-4 text-lg font-semibold">Transport routes</h1>
      <ul className="space-y-2">
        {(data as { id: string; name: string }[] | undefined)?.map((r) => (
          <li key={r.id} className="rounded-xl border border-slate-100 px-3 py-2">
            {r.name}
          </li>
        )) ?? <li className="text-sm text-slate-500">No routes</li>}
      </ul>
    </div>
  );
}
