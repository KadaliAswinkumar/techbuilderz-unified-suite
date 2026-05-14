import { useEffect, useRef, useState } from "react";
import { Bot, MessageCircle, Send, Sparkles, X } from "lucide-react";
import { api } from "@/lib/api";
import { getAccessToken } from "@/lib/auth";

type Msg = { role: "user" | "assistant"; text: string };

const SUGGESTIONS = [
  "When is the next fee due?",
  "How do I check exam results?",
  "Who do I contact for transport?",
];

export function ChatWidget() {
  const [open, setOpen] = useState(false);
  const [input, setInput] = useState("");
  const [messages, setMessages] = useState<Msg[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const bottomRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, open, loading]);

  async function send(text: string) {
    const q = text.trim();
    if (!q || loading) {
      return;
    }
    setError(null);
    setMessages((m) => [...m, { role: "user", text: q }]);
    setInput("");
    setLoading(true);
    try {
      const { data } = await api.post("/chatbot/ask", { question: q });
      const answer = (String((data as { answer?: string }).answer ?? "").trim() || "No reply received.");
      setMessages((m) => [...m, { role: "assistant", text: answer }]);
    } catch (e: unknown) {
      const msg = e instanceof Error ? e.message : "Something went wrong.";
      setError(msg);
      setMessages((m) => [...m, { role: "assistant", text: "Sorry — I could not reach the assistant. Try again in a moment." }]);
    } finally {
      setLoading(false);
    }
  }

  if (!getAccessToken()) {
    return null;
  }

  return (
    <div className="fixed bottom-4 right-4 z-50 md:bottom-6 md:right-6">
      {open ? (
        <div className="flex h-[min(32rem,calc(100vh-6rem))] w-[min(100vw-2rem,22rem)] flex-col overflow-hidden rounded-2xl border border-slate-200/80 bg-white shadow-2xl shadow-slate-900/10 dark:border-slate-700 dark:bg-slate-900 dark:shadow-black/40 sm:w-[26rem]">
          <div className="flex items-center justify-between bg-gradient-to-r from-[hsl(var(--brand))] to-indigo-600 px-4 py-3 text-white">
            <div className="flex items-center gap-2">
              <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-white/15">
                <Bot className="h-5 w-5" />
              </div>
              <div>
                <div className="text-sm font-semibold leading-tight">School assistant</div>
                <div className="text-[11px] text-white/80">Fees, exams, schedules and FAQs</div>
              </div>
            </div>
            <button
              type="button"
              onClick={() => setOpen(false)}
              className="rounded-lg p-1.5 hover:bg-white/10"
              aria-label="Close chat"
            >
              <X className="h-4 w-4" />
            </button>
          </div>

          <div className="flex flex-1 flex-col bg-slate-50/80 dark:bg-slate-950/50">
            <div className="flex-1 space-y-3 overflow-y-auto px-3 py-3">
              {messages.length === 0 && !loading ? (
                <div className="rounded-xl border border-dashed border-slate-200 bg-white/80 p-4 text-center dark:border-slate-700 dark:bg-slate-900/80">
                  <Sparkles className="mx-auto mb-2 h-6 w-6 text-amber-500" />
                  <p className="text-xs font-medium text-slate-800 dark:text-slate-100">Ask anything about the school</p>
                  <p className="mt-1 text-[11px] leading-relaxed text-slate-500 dark:text-slate-400">
                    Answers use your tenant FAQ and policy context when configured.
                  </p>
                  <div className="mt-3 flex flex-wrap justify-center gap-1.5">
                    {SUGGESTIONS.map((s) => (
                      <button
                        key={s}
                        type="button"
                        onClick={() => void send(s)}
                        className="rounded-full border border-slate-200 bg-white px-2.5 py-1 text-[10px] text-slate-600 transition hover:border-[hsl(var(--brand))] hover:text-[hsl(var(--brand))] dark:border-slate-600 dark:bg-slate-800 dark:text-slate-300"
                      >
                        {s}
                      </button>
                    ))}
                  </div>
                </div>
              ) : null}

              {messages.map((m, i) => (
                <div key={i} className={`flex ${m.role === "user" ? "justify-end" : "justify-start"}`}>
                  <div
                    className={
                      m.role === "user"
                        ? "max-w-[85%] rounded-2xl rounded-br-md bg-[hsl(var(--brand))] px-3 py-2 text-xs leading-relaxed text-white"
                        : "max-w-[90%] rounded-2xl rounded-bl-md border border-slate-200 bg-white px-3 py-2 text-xs leading-relaxed text-slate-800 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-100"
                    }
                  >
                    {m.text}
                  </div>
                </div>
              ))}
              {loading ? (
                <div className="flex justify-start">
                  <div className="flex items-center gap-2 rounded-2xl border border-slate-200 bg-white px-3 py-2 text-xs text-slate-500 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-400">
                    <span className="inline-flex h-1.5 w-1.5 animate-bounce rounded-full bg-slate-400 [animation-delay:-0.2s]" />
                    <span className="inline-flex h-1.5 w-1.5 animate-bounce rounded-full bg-slate-400" />
                    <span className="inline-flex h-1.5 w-1.5 animate-bounce rounded-full bg-slate-400 [animation-delay:0.2s]" />
                    Thinking…
                  </div>
                </div>
              ) : null}
              {error ? <p className="text-center text-[10px] text-red-600 dark:text-red-400">{error}</p> : null}
              <div ref={bottomRef} />
            </div>

            <div className="border-t border-slate-200 bg-white p-2 dark:border-slate-800 dark:bg-slate-900">
              <div className="flex gap-2">
                <input
                  className="min-w-0 flex-1 rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm outline-none focus:border-[hsl(var(--brand))] dark:border-slate-700 dark:bg-slate-950 dark:text-slate-100"
                  value={input}
                  onChange={(e) => setInput(e.target.value)}
                  placeholder="Type your question…"
                  onKeyDown={(e) => {
                    if (e.key === "Enter" && !e.shiftKey) {
                      e.preventDefault();
                      void send(input);
                    }
                  }}
                />
                <button
                  type="button"
                  onClick={() => void send(input)}
                  disabled={loading || !input.trim()}
                  className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-[hsl(var(--accent))] text-white shadow-sm disabled:opacity-40"
                  aria-label="Send"
                >
                  <Send className="h-4 w-4" />
                </button>
              </div>
            </div>
          </div>
        </div>
      ) : (
        <button
          type="button"
          onClick={() => setOpen(true)}
          className="group flex h-14 w-14 items-center justify-center rounded-full bg-gradient-to-br from-[hsl(var(--brand))] to-indigo-600 text-white shadow-lg shadow-indigo-900/25 ring-4 ring-white/30 transition hover:scale-105 dark:ring-slate-900/50"
          aria-label="Open assistant"
        >
          <MessageCircle className="h-6 w-6 transition group-hover:scale-110" />
        </button>
      )}
    </div>
  );
}
