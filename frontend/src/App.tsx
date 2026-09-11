import { FormEvent, useEffect, useState } from "react";

type Role = "USER" | "ASSISTANT";
type Message = { role: Role; content: string };
type Conversation = { id: string; subject: string; updatedAt: string };
type ConversationView = {
  id: string;
  subject: string;
  summary: string | null;
  messages: Message[];
};

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "";
const USERS = ["user_1", "user_2", "user_3"];
const MAX_MESSAGE_LENGTH = 500;

function newConversationId(): string {
  return crypto.randomUUID();
}

export function App() {
  const [userId, setUserId] = useState(USERS[0]);
  const [conversationId, setConversationId] = useState(newConversationId);
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [subject, setSubject] = useState("New conversation");
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [summary, setSummary] = useState<string | null>(null);

  useEffect(() => {
    void loadConversations(userId);
    startNewConversation();
  }, [userId]);

  async function loadConversations(selectedUser: string) {
    try {
      const response = await fetch(`${API_BASE_URL}/api/chat/conversations?userId=${selectedUser}`);
      if (!response.ok) throw new Error("Could not load conversations.");
      setConversations((await response.json()) as Conversation[]);
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : "Could not load conversations.");
    }
  }

  function startNewConversation() {
    setConversationId(newConversationId());
    setSubject("New conversation");
    setMessages([]);
    setSummary(null);
    setError(null);
  }

  async function selectConversation(id: string) {
    setError(null);
    try {
      const response = await fetch(
        `${API_BASE_URL}/api/chat/conversations/${id}?userId=${userId}`
      );
      if (!response.ok) throw new Error("Could not load the selected conversation.");
      const data = (await response.json()) as ConversationView;
      setConversationId(data.id);
      setSubject(data.subject);
      setSummary(data.summary);
      setMessages(data.messages);
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : "Could not load conversation.");
    }
  }

  async function sendMessage(event: FormEvent) {
    event.preventDefault();
    const message = input.trim();
    if (!message || message.length > MAX_MESSAGE_LENGTH || loading) return;

    setMessages((current) => [...current, { role: "USER", content: message }]);
    setInput("");
    setLoading(true);
    setError(null);

    try {
      const response = await fetch(`${API_BASE_URL}/api/chat`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ userId, conversationId, message })
      });
      if (!response.ok) {
        const body = (await response.json().catch(() => null)) as { detail?: string; message?: string } | null;
        throw new Error(body?.detail ?? body?.message ?? "The chat service is unavailable.");
      }

      const data = (await response.json()) as {
        conversationId: string;
        subject: string;
        summary: string | null;
        message: string;
      };
      setConversationId(data.conversationId);
      setSubject(data.subject);
      setSummary(data.summary);
      setMessages((current) => [...current, { role: "ASSISTANT", content: data.message }]);
      await loadConversations(userId);
    } catch (requestError) {
      const content = requestError instanceof Error ? requestError.message : "Unexpected error.";
      setError(content);
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="mx-auto flex min-h-screen max-w-6xl gap-4 px-4 py-4 sm:px-8 sm:py-8">
      <aside className="flex w-64 shrink-0 flex-col rounded-2xl border border-slate-200 bg-slate-900 p-4 text-white">
        <div className="mb-5">
          <p className="text-xs font-semibold uppercase tracking-[0.25em] text-indigo-300">AI Lab</p>
          <h1 className="mt-2 text-xl font-bold">Conversations</h1>
        </div>
        <label className="mb-2 text-xs font-semibold uppercase tracking-wide text-slate-400" htmlFor="user">
          Simulated user
        </label>
        <select
          id="user"
          className="mb-4 rounded-lg border border-slate-700 bg-slate-800 px-3 py-2 text-sm outline-none focus:border-indigo-400"
          value={userId}
          onChange={(event) => setUserId(event.target.value)}
        >
          {USERS.map((user) => <option key={user}>{user}</option>)}
        </select>
        <button
          className="mb-4 rounded-lg bg-indigo-500 px-3 py-2 text-sm font-semibold transition hover:bg-indigo-400"
          onClick={startNewConversation}
        >
          + New conversation
        </button>
        <div className="min-h-0 flex-1 space-y-1 overflow-y-auto">
          {conversations.map((conversation) => (
            <button
              key={conversation.id}
              className={`w-full rounded-lg px-3 py-2 text-left text-sm transition ${conversation.id === conversationId ? "bg-slate-700" : "hover:bg-slate-800"}`}
              onClick={() => void selectConversation(conversation.id)}
            >
              <span className="block truncate">{conversation.subject}</span>
              <span className="mt-1 block truncate text-xs text-slate-400">{conversation.id}</span>
            </button>
          ))}
          {conversations.length === 0 && <p className="px-2 text-sm text-slate-500">No saved conversations.</p>}
        </div>
      </aside>

      <section className="flex min-h-[calc(100vh-2rem)] min-w-0 flex-1 flex-col rounded-2xl border border-slate-200 bg-white shadow-sm sm:min-h-[calc(100vh-4rem)]">
        <header className="border-b border-slate-200 px-5 py-4 sm:px-6">
          <p className="text-sm font-semibold uppercase tracking-[0.2em] text-indigo-600">{userId}</p>
          <h2 className="mt-1 truncate text-xl font-bold tracking-tight text-slate-900">{subject}</h2>
          <p className="mt-1 truncate text-xs text-slate-400">{conversationId}</p>
        </header>

        <div className="flex-1 space-y-4 overflow-y-auto p-4 sm:p-6">
          {summary && (
            <div className="rounded-xl border border-amber-200 bg-amber-50 p-3 text-sm text-amber-900">
              <strong>Conversation summary:</strong> {summary}
            </div>
          )}
          {messages.length === 0 && !summary && (
            <p className="py-16 text-center text-slate-400">Ask the copilot a question to begin.</p>
          )}
          {messages.map((item, index) => (
            <div key={`${item.role}-${index}`} className={`flex ${item.role === "USER" ? "justify-end" : "justify-start"}`}>
              <p className={`max-w-[85%] rounded-2xl px-4 py-3 text-sm leading-6 ${item.role === "USER" ? "bg-indigo-600 text-white" : "bg-slate-100 text-slate-800"}`}>
                {item.content}
              </p>
            </div>
          ))}
          {loading && <p className="text-sm text-slate-400">Thinking...</p>}
          {error && <p className="rounded-lg bg-red-50 p-3 text-sm text-red-700">{error}</p>}
        </div>

        <form onSubmit={sendMessage} className="border-t border-slate-200 p-4">
          <div className="flex gap-2">
            <input
              className="min-w-0 flex-1 rounded-xl border border-slate-300 px-4 py-3 outline-none focus:border-indigo-500"
              value={input}
              maxLength={MAX_MESSAGE_LENGTH}
              onChange={(event) => setInput(event.target.value)}
              placeholder="Ask about an order..."
            />
            <button className="rounded-xl bg-indigo-600 px-5 py-3 font-semibold text-white transition hover:bg-indigo-700 disabled:cursor-not-allowed disabled:opacity-50" disabled={loading || !input.trim() || input.length > MAX_MESSAGE_LENGTH}>Send</button>
          </div>
          <div className="mt-2 flex justify-between text-xs text-slate-400">
            <span>Messages are saved by conversation.</span>
            <span>{input.length}/{MAX_MESSAGE_LENGTH}</span>
          </div>
        </form>
      </section>
    </main>
  );
}
