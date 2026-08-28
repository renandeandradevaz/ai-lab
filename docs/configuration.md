# Configuration Reference

The project uses environment variables for local configuration. Copy `.env.example` to `.env` before starting the stack. The `.env` file is ignored by Git and must never contain committed secrets.

## User-configurable variables

| Variable | Default | Used by | Description |
| --- | --- | --- | --- |
| `POSTGRES_DB` | `ai_lab` | Docker Compose | PostgreSQL database name. |
| `POSTGRES_USER` | `ai_lab` | Docker Compose | PostgreSQL username. |
| `POSTGRES_PASSWORD` | `ai_lab` | Docker Compose | PostgreSQL password for local development. |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/ai_lab` | Backend | JDBC URL used by the backend when running outside Docker. |
| `LLM_CHAT_MODEL` | `llama3.2:3b` | Ollama initializer, backend | Ollama model used for chat. |
| `LLM_EMBEDDING_MODEL` | `nomic-embed-text` | Ollama initializer | Ollama model reserved for embeddings. |
| `OLLAMA_HOST_PORT` | `11435` | Docker Compose | Host port mapped to the Ollama container port `11434`. |
| `LLM_TEMPERATURE` | `0.2` | Backend | Chat model temperature. |
| `SERVER_PORT` | `8080` | Backend | Backend HTTP port inside and outside the container. |
| `VITE_API_BASE_URL` | `http://localhost:8080` | Frontend build | Backend URL embedded in the frontend production bundle. |

## Internal service variables

| Variable | Value in Docker | Description |
| --- | --- | --- |
| `OLLAMA_BASE_URL` | `http://ollama:11434` | Backend-to-Ollama URL on the Compose network. |
| `OLLAMA_HOST` | `http://ollama:11434` | Ollama CLI URL used by the model initialization service. |

These values are internal Docker network addresses and normally should not be changed for the standard local stack. When running the backend directly on the host, set `OLLAMA_BASE_URL=http://localhost:11434` or use the host Ollama port that is configured locally. The Docker backend uses `jdbc:postgresql://postgres:5432/...`; the host default is `jdbc:postgresql://localhost:5432/ai_lab`.

## Provider configuration

The current runtime provider is Ollama. Provider abstraction is planned so future providers can be selected without changing agent logic:

```text
LLM_PROVIDER=ollama
LLM_CHAT_MODEL=llama3.2:3b
LLM_EMBEDDING_MODEL=nomic-embed-text
```

`LLM_PROVIDER` is documented as a future configuration value and is not consumed by the current bootstrap implementation yet.

## Secret handling

- Do not commit `.env`.
- Do not put API keys in source code, Dockerfiles, or documentation.
- Use provider-specific environment variables when remote providers are added.
- Use `.env.example` only for placeholders and non-sensitive local defaults.
