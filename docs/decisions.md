# Architecture Decisions

## ADR-001: Use a Single Repository

The project uses one repository named `ai-lab` with separate `backend` and `frontend` applications. This keeps local setup, documentation, and versioning simple while preserving a clear boundary between the primary backend and the supporting UI.

## ADR-002: Keep the Frontend Minimal

The frontend uses React, TypeScript, Vite, and Tailwind CSS. It provides only a chat interface, loading state, and error display. Agent orchestration, security rules, tools, RAG, memory, and approvals belong in the backend.

## ADR-003: Use Java 25 LTS

Java 25 is the latest LTS baseline selected for this new project. Java 26 was not selected because it is a short-lived non-LTS release.

## ADR-004: Use Spring Boot 4.1.0 and Spring AI 2.0.0

These are the selected stable Spring baselines for the project. Spring AI provides the common abstractions needed for chat, tools, structured output, embeddings, vector stores, RAG, and observability.

## ADR-005: Use Ollama as the Default Provider

The initial environment uses Ollama because it requires no external API key, keeps prompts local, supports local chat and embeddings, and makes development reproducible. The provider layer must remain replaceable.

## ADR-006: Separate Chat and Embedding Providers

Chat models and embedding models have different capabilities and operational requirements. The initial chat model is `llama3.2:3b`, while `nomic-embed-text` is used for embeddings. The GPT 5.6 Luna model is not required for the local setup and must not be assumed to provide embeddings.

## ADR-007: Run Infrastructure with Docker Compose

PostgreSQL, pgvector, Ollama, the backend, and the frontend are runnable through Docker Compose. Persistent volumes are used for database data and downloaded models.

The Ollama host port defaults to `11435` because port `11434` may already be occupied by a host Ollama installation. Container-to-container traffic still uses `http://ollama:11434`.

## ADR-008: Preserve Provider Agnosticism

OpenCode Go was considered as a possible initial provider. Its API key and endpoint can be used by an OpenAI-compatible client, but the model catalog and protocol may vary. Therefore, OpenCode Go is optional and is not embedded into the core design. Provider selection, protocol differences, capability detection, retries, and fallbacks belong behind the provider boundary.

## ADR-009: Keep Tests Independent from External LLMs

Unit and regression tests must use mocks or deterministic fixtures by default. Real-model tests are optional integration checks and must not be required for a normal build.

## ADR-010: Use English for Technical Artifacts

Source code, identifiers, logs, error messages, comments, test data, and technical documentation are written in English. End-user chat content may use any language supported by the selected model.
