import { FormEvent, useState } from "react";

type Message = { role: "user" | "assistant"; content: string };
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "";

export function App() {
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);

  async function sendMessage(event: FormEvent) {
    event.preventDefault();
    const message = input.trim();
    if (!message || loading) return;

    setMessages((current) => [...current, { role: "user", content: message }]);
    setInput("");
    setLoading(true);

    try {
      const response = await fetch(`${API_BASE_URL}/api/chat`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ message })
      });
      if (!response.ok) throw new Error("The chat service is unavailable.");
      const data = (await response.json()) as { message: string };
      setMessages((current) => [...current, { role: "assistant", content: data.message }]);
    } catch (error) {
      const content = error instanceof Error ? error.message : "Unexpected error.";
      setMessages((current) => [...current, { role: "assistant", content }]);
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="mx-auto flex min-h-screen max-w-4xl flex-col px-4 py-8 sm:px-8">
      <header className="mb-8">
        <p className="text-sm font-semibold uppercase tracking-[0.25em] text-indigo-600">AI Lab</p>
        <h1 className="mt-2 text-3xl font-bold tracking-tight text-slate-900">Operations Copilot</h1>
        <p className="mt-2 text-slate-600">A local chat interface powered by Ollama.</p>
      </header>

      <section className="flex flex-1 flex-col rounded-2xl border border-slate-200 bg-white shadow-sm">
        <div className="flex-1 space-y-4 p-4 sm:p-6">
          {messages.length === 0 && <p className="py-16 text-center text-slate-400">Ask the copilot a question to begin.</p>}
          {messages.map((item, index) => (
            <div key={`${item.role}-${index}`} className={`flex ${item.role === "user" ? "justify-end" : "justify-start"}`}>
              <p className={`max-w-[85%] rounded-2xl px-4 py-3 text-sm leading-6 ${item.role === "user" ? "bg-indigo-600 text-white" : "bg-slate-100 text-slate-800"}`}>
                {item.content}
              </p>
            </div>
          ))}
          {loading && <p className="text-sm text-slate-400">Thinking...</p>}
        </div>
        <form onSubmit={sendMessage} className="flex gap-2 border-t border-slate-200 p-4">
          <input className="min-w-0 flex-1 rounded-xl border border-slate-300 px-4 py-3 outline-none focus:border-indigo-500" value={input} onChange={(event) => setInput(event.target.value)} placeholder="Ask about an order..." />
          <button className="rounded-xl bg-indigo-600 px-5 py-3 font-semibold text-white transition hover:bg-indigo-700 disabled:cursor-not-allowed disabled:opacity-50" disabled={loading || !input.trim()}>Send</button>
        </form>
      </section>
    </main>
  );
}
