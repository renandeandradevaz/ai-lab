# AI Lab

AI Lab is a local-first laboratory for building and evaluating an AI Operations Copilot for a fictional e-commerce company. The project focuses on AI Engineering, including LLM applications, RAG, agent workflows, tools, security, human approval, evaluation, and observability.

The backend is the primary focus. The frontend is intentionally small and exists only to provide a browser-based chat interface.

## Current Status

### Completed

- [x] Monorepo layout with `backend` and `frontend` applications.
- [x] Java 25 and Spring Boot 4.1.0 backend baseline.
- [x] Spring AI 2.0.0 dependency management.
- [x] Ollama as the default local LLM provider.
- [x] Configurable local chat model.
- [x] Synchronous `POST /api/chat` endpoint.
- [x] Streaming `POST /api/chat/stream` endpoint.
- [x] Actuator health endpoint.
- [x] CORS configuration for local frontend development.
- [x] React, TypeScript, Vite, and Tailwind frontend baseline.
- [x] Minimal browser chat interface.
- [x] Docker Compose services for the backend, frontend, PostgreSQL with pgvector, and Ollama.
- [x] Persistent Docker volumes for PostgreSQL and Ollama models.
- [x] Automatic Ollama model initialization.
- [x] Makefile commands for common local operations.
- [x] Environment variable template and secret exclusion.
- [x] Basic Spring context test.
- [x] Backend Docker image build verified with Java 25.0.4.
- [x] Frontend Docker image build verified with Vite and Tailwind.
- [x] Full local stack verified with PostgreSQL, Ollama, backend, and frontend containers.
- [x] Real chat request verified through the backend and local Ollama model.

The current implementation is a bootstrap chat application only. It does not yet have conversation memory, so each request is independent even though previous messages remain visible in the browser. It also does not yet have tools, RAG, an agent loop, or access to fictional order data.

### Pending

- [ ] Add the database datasource and migration strategy.
- [ ] Implement the provider-agnostic model adapter layer.
- [ ] Add structured output schemas and validation.
- [ ] Implement operational tools and deterministic fictional business services.
- [ ] Add tool argument validation, timeout, retry, idempotency, and error policies.
- [ ] Implement the document ingestion and RAG pipeline.
- [ ] Add the fictional policy and support knowledge base.
- [ ] Add pgvector storage, metadata filtering, citations, and source tracking.
- [ ] Implement the full agent loop and planning state.
- [ ] Add short-term and long-term memory.
- [ ] Implement the human-in-the-loop approval workflow and audit trail.
- [ ] Add prompt injection defenses, sensitive data protection, allowlists, and least privilege.
- [ ] Add the MCP server and MCP resources.
- [ ] Create evaluation datasets, regression tests, and LLM-as-a-Judge evaluation.
- [ ] Add OpenTelemetry traces, metrics, token usage, and cost tracking.
- [ ] Implement caching, context reduction, model routing, and fallback policies.
- [ ] Expand integration, security, agent behavior, and end-to-end tests.
- [x] Document the current implementation and pending work in the README and versioned project docs.

## Technology Baseline

- Java 25 LTS
- Spring Boot 4.1.0
- Spring AI 2.0.0
- Maven 3.9.x
- React with TypeScript
- Vite
- Tailwind CSS
- Ollama
- PostgreSQL with pgvector
- Docker Compose
- OpenTelemetry (planned)

The application is provider-agnostic by design. Ollama is the initial local provider, but future adapters may support OpenCode Go, OpenAI, Anthropic, other OpenAI-compatible endpoints, and a mock provider for deterministic tests.

## Repository Layout

```text
ai-lab/
├── backend/       Spring Boot and Spring AI application
├── frontend/      Minimal React chat application
├── docs/           Versioned architecture, decisions, plan, and configuration
├── ollama-init.sh  Local model initialization script
├── docker-compose.yml
├── Makefile
└── .env.example
```

## Project Context

The durable project context is kept in versioned documentation:

- [Architecture](docs/architecture.md): runtime topology, boundaries, data flow, and safety model.
- [Architecture Decisions](docs/decisions.md): important technical choices and their rationale.
- [Implementation Plan](docs/implementation-plan.md): completed work, pending workstreams, and definition of done.
- [Configuration Reference](docs/configuration.md): all environment variables, defaults, and ownership.

## Local Requirements

- Docker Engine with Docker Compose.
- Java 25 for running the backend directly.
- Maven 3.9.x for running the backend directly.
- Node.js 22 or newer for running the frontend directly.

Docker execution does not require Java or Node.js installed on the host because the backend and frontend use container images.

## Configuration

Copy `.env.example` to `.env` and adjust the model names if necessary. Do not commit `.env` or any API key.

Important variables:

```text
LLM_CHAT_MODEL=llama3.2:3b
LLM_EMBEDDING_MODEL=nomic-embed-text
OLLAMA_BASE_URL=http://ollama:11434
OLLAMA_HOST_PORT=11435
```

The chat and embedding models are intentionally separate. Embeddings will be used by the future RAG implementation.

See [Configuration Reference](docs/configuration.md) for the complete variable list. The current chat model is `llama3.2:3b` and the current embedding model is `nomic-embed-text`.

## Running Locally

Start the complete Docker environment:

```bash
make up
```

The first startup downloads the configured Ollama models and can take some time. Models are stored in a persistent Docker volume.

Check backend health:

```bash
make health
```

Open the frontend at `http://localhost:5173`.

The backend is available at `http://localhost:8080` and its health endpoint is `http://localhost:8080/actuator/health`. The Ollama container is exposed on `http://localhost:11435` by default to avoid conflicts with a host Ollama installation; change `OLLAMA_HOST_PORT` if needed.

Stop the environment:

```bash
make down
```

## Session Handoff

To resume work in a new development session, read these files in order:

1. `README.md` for the current status and local commands.
2. `docs/implementation-plan.md` for completed and pending work.
3. `docs/architecture.md` for system boundaries and planned data flow.
4. `docs/decisions.md` for the rationale behind the selected stack and provider strategy.
5. `docs/configuration.md` for environment variables and defaults.

The source code and Docker files are the source of truth for the current implementation. Docker volumes are local runtime state and are not versioned by Git.

## Chat API

Send a synchronous chat request:

```bash
curl -X POST http://localhost:8080/api/chat \
  -H 'Content-Type: application/json' \
  -d '{"message":"What can you help me with?"}'
```

Stream a chat response:

```bash
curl -N -X POST http://localhost:8080/api/chat/stream \
  -H 'Content-Type: application/json' \
  -d '{"message":"Explain the role of an operations copilot."}'
```

## Development Commands

```text
make up            Start and build the complete environment
make down          Stop the environment
make logs          Follow container logs
make build         Build Docker images
make pull-models   Download configured Ollama models
make test          Run backend tests in a Java 25 container
make health        Check the backend health endpoint
```

## Language Policy

All source code, identifiers, logs, errors, comments, test data, and technical documentation are written in English. User-facing conversation content can be supplied in any language supported by the selected model.
