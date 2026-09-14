.PHONY: up down logs build test eval pull-models health

up:
	docker compose up --build

down:
	docker compose down

logs:
	docker compose logs -f

build:
	docker compose build

test:
	docker compose up -d --wait postgres
	docker run --rm --network host -e SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/ai_lab -e POSTGRES_USER=ai_lab -e POSTGRES_PASSWORD=ai_lab -e OLLAMA_BASE_URL=http://localhost:11434 -v "$(PWD)/backend:/workspace" -w /workspace maven:3.9-eclipse-temurin-25 mvn -DexcludedGroups=eval test

eval:
	docker compose up -d --wait postgres ollama
	docker compose run --rm ollama-init
	docker run --rm --network host -e SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/ai_lab -e POSTGRES_USER=ai_lab -e POSTGRES_PASSWORD=ai_lab -e OLLAMA_BASE_URL=http://localhost:11435 -e LLM_CHAT_MODEL=$${LLM_CHAT_MODEL:-llama3.2:3b} -e LLM_TEMPERATURE=$${LLM_TEMPERATURE:-0.2} -v "$(PWD)/backend:/workspace" -w /workspace maven:3.9-eclipse-temurin-25 mvn -Dtest=ChatEvalTests -Deval.runs=$${EVAL_RUNS:-1} test

pull-models:
	docker compose run --rm ollama-init

health:
	curl --fail http://localhost:8080/actuator/health
