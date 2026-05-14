import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";

export function ExamPage() {
  const { data } = useQuery({
    queryKey: ["exams"],
    queryFn: async () => (await api.get("/exams")).data,
  });

  return (
    <div className="surface-card p-4">
      <h1 className="mb-4 text-lg font-semibold">Exams</h1>
      <div className="overflow-x-auto">
        <table className="w-full text-left text-sm">
          <thead>
            <tr className="border-b text-xs text-slate-500">
              <th className="py-2">Name</th>
              <th>Type</th>
              <th>Date</th>
            </tr>
          </thead>
          <tbody>
            {(data as { id: string; name: string; examType: string; examDate: string }[] | undefined)?.map((e) => (
              <tr key={e.id} className="border-b border-slate-50">
                <td className="py-2">{e.name}</td>
                <td>{e.examType}</td>
                <td>{e.examDate}</td>
              </tr>
            )) ?? null}
          </tbody>
        </table>
      </div>
    </div>
  );
}
