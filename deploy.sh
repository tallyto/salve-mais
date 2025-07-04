#!/bin/bash
# Script de deploy usando Docker Compose para produção
# Ajuste os caminhos conforme necessário

APP_DIR="$HOME/projetos/gestor-financeiro"
COMPOSE_FILE="docker-compose.prod.yml"

cd "$APP_DIR" || exit 1

echo "Parando containers antigos..."
docker compose -f "$COMPOSE_FILE" down

echo "Subindo containers em modo detached..."
docker compose -f "$COMPOSE_FILE" up -d --build

echo "Removendo imagens antigas não utilizadas..."
docker image prune -f

echo "Deploy finalizado!"
