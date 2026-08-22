.PHONY: up down logs build test pull-models health

up:
	docker compose up --build

down:
	docker compose down

logs:
	docker compose logs -f

build:
	docker compose build

test:
	docker run --rm -v "$(PWD)/backend:/workspace" -w /workspace maven:3.9-eclipse-temurin-25 mvn test

pull-models:
	docker compose run --rm ollama-init

health:
	curl --fail http://localhost:8080/actuator/health
