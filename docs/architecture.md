# AI Lab Architecture

## Purpose

AI Lab is a local-first laboratory for an AI Operations Copilot for a fictional e-commerce company. The project prioritizes AI Engineering capabilities over traditional backend infrastructure.

The backend is the primary product. The frontend is a deliberately small React client used to exercise the chat API.

## Runtime Topology

```text
Browser
  -> React frontend
      -> Spring Boot backend
          -> Provider-neutral AI layer
              -> Ollama chat model
              -> Ollama embedding model
          -> Operational tools and business services
          -> PostgreSQL and pgvector
          -> OpenTelemetry (planned)
```

The local environment is managed by Docker Compose:

- `frontend`: React production bundle served by `serve`.
- `backend`: Spring Boot application running on Java 25.
- `ollama`: Local model server with persistent model storage.
- `ollama-init`: Downloads configured models into the shared Ollama volume.
- `postgres`: PostgreSQL with the pgvector extension image.

## Backend Boundaries

The backend will be organized around AI capabilities rather than a traditional domain-heavy architecture:

- Chat interaction.
- Provider abstraction.
- Agent orchestration.
- Reusable skill catalog and workflow instructions.
- Tool catalog and execution policies.
- Knowledge ingestion and retrieval.
- Conversation memory.
- Human approval workflow.
- Guardrails and security.
- Evaluation.
- Observability and cost control.

Fictional business services will remain deterministic and intentionally simple. They exist to provide safe tool targets for the agent.

## Model Providers

The application must not depend on a single LLM vendor. The planned abstraction supports:

- Ollama as the default local provider.
- OpenCode Go as an optional remote provider.
- OpenAI as an optional provider.
- Anthropic as an optional provider.
- A mock provider for deterministic tests.

The chat model and embedding model are separate capabilities. The initial chat model is `llama3.2:3b`; the initial embedding model is `nomic-embed-text`.

Future providers must be selectable through configuration and must not require changes to agent, RAG, tool, or approval logic.

## Current API

- `POST /api/chat`: synchronous chat response.
- `POST /api/chat/stream`: text event stream response.
- `GET /actuator/health`: application health.

The frontend uses the synchronous endpoint at this stage. Streaming integration remains a frontend enhancement.

## Data and Knowledge Flow

The planned RAG flow is:

```text
Knowledge document
  -> Chunking
  -> Embedding model
  -> PostgreSQL/pgvector
  -> Similarity search and metadata filtering
  -> Retrieved context and source references
  -> Agent response
```

Operational data will be provided by deterministic fictional services and will be kept distinct from retrieved policy content.

## Safety Boundaries

The agent will not be allowed to execute high-risk mutations directly. Cancellation, refunds, and address changes require a human approval workflow. Read operations may run automatically when authorized.

The final system will enforce tool allowlists, argument validation, least privilege, iteration limits, sensitive data protection, prompt injection defenses, and final response validation.

## Skills

Skills are reusable, bounded workflow definitions for common operational scenarios. A skill may combine intent recognition, approved tools, knowledge retrieval, memory, and human approval, but it does not bypass the underlying execution and security policies.

Initial skill candidates include order status lookup, delayed-order investigation, policy lookup with citations, cancellation requests, and refund requests. Each skill must define its activation conditions, required inputs, permitted tools, validation rules, approval requirements, error behavior, and output contract.
