# Implementation Plan

This is one integrated project, not a sequence of public V1, V2, or V14 releases. The work is ordered internally so each capability can be implemented and verified without losing previous functionality.

## Completed

- [x] Create the `backend` and `frontend` monorepo layout.
- [x] Bootstrap Spring Boot 4.1.0 with Java 25 configuration.
- [x] Add Spring AI 2.0.0 dependency management.
- [x] Configure Ollama chat integration.
- [x] Add synchronous chat endpoint.
- [x] Add streaming chat endpoint.
- [x] Add Actuator health endpoint.
- [x] Add local CORS configuration.
- [x] Bootstrap React, TypeScript, Vite, and Tailwind.
- [x] Add a minimal browser chat interface.
- [x] Add Docker Compose for PostgreSQL, Ollama, backend, and frontend.
- [x] Add persistent PostgreSQL and Ollama volumes.
- [x] Add automatic Ollama model initialization.
- [x] Add Makefile and environment template.
- [x] Verify backend tests with Java 25 in Docker.
- [x] Verify backend and frontend Docker image builds.
- [x] Verify the full local stack and a real Ollama chat request.

## Workstream 1: Provider Layer

- Define provider-neutral chat, streaming, structured output, and embedding contracts.
- Implement Ollama adapters.
- Add capability detection.
- Add mock provider support.
- Add optional OpenCode Go, OpenAI, and Anthropic adapters.
- Keep provider configuration in environment variables.

## Workstream 2: Operational Domain and Tools

- Create deterministic fictional customers, orders, products, inventory, payments, deliveries, tickets, refunds, and address data.
- Implement read tools and mutation tools.
- Define tool schemas and validation.
- Add timeout, retry, idempotency, and error policies.
- Add tool allowlists and execution limits.

## Workstream 3: RAG

- Add refund, cancellation, shipping, warranty, FAQ, and support documents in English.
- Implement loading, chunking, embedding, and persistence.
- Enable pgvector and metadata filtering.
- Implement top-K retrieval.
- Add citations and source tracking.
- Handle insufficient evidence explicitly.

## Workstream 4: Agent and Memory

- Implement intent understanding and planning.
- Implement a bounded single-agent loop.
- Combine tools and RAG for the delayed-order use case.
- Add short-term conversation memory.
- Add long-term memory and entity references.
- Add context selection and summarization.

## Workstream 5: Human Approval and Security

- Classify tools by risk.
- Add pending approval actions.
- Add approve, reject, and expiration flows.
- Add audit trail.
- Block mutations before approval.
- Add input and output validation.
- Add direct and indirect prompt injection defenses.
- Add sensitive data masking and least privilege.
- Prevent excessive agency and unauthorized tool access.

## Workstream 6: MCP

- Expose operational tools through an MCP server.
- Expose policy and FAQ resources.
- Support discovery and schemas.
- Add access control and audit logging.
- Document direct function calling versus MCP.

## Workstream 7: Evaluation

- Version evaluation datasets.
- Add deterministic tool-selection assertions.
- Test RAG grounding and citations.
- Test HITL blocking.
- Test memory continuity.
- Test prompt injection defenses.
- Add optional LLM-as-a-Judge evaluation.
- Produce evaluation reports.

## Workstream 8: Observability and Cost

- Instrument HTTP, agent, LLM, tool, retrieval, memory, approval, and evaluation spans.
- Track latency, tokens, model, calls, sources, errors, retries, and estimated cost.
- Add correlation IDs.
- Add embedding and response caching where safe.
- Add context reduction and iteration limits.
- Add model routing and fallback policies.

## Definition of Done

The project is complete when the full agent can answer operational questions using tools and policies, cite its evidence, preserve relevant conversation context, request human approval for high-risk mutations, resist common prompt attacks, expose its lifecycle through telemetry, and pass the documented evaluation suite.
