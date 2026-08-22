#!/bin/sh
set -eu

ollama pull "${LLM_CHAT_MODEL}"
ollama pull "${LLM_EMBEDDING_MODEL}"
