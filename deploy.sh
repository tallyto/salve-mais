#!/bin/bash
# Script de deploy usando Docker Compose para produção
# Ajuste os caminhos conforme necessário

set -e  # Interrompe o script se qualquer comando falhar

APP_DIR="$HOME/projetos/gestor-financeiro"
COMPOSE_FILE="docker-compose.prod.yml"

cd "$APP_DIR" || exit 1

echo "Construindo nova imagem..."
docker compose -f "$COMPOSE_FILE" build

if [ $? -ne 0 ]; then
    echo "Erro: Build da imagem falhou! Containers antigos não foram afetados."
    exit 1
fi

echo "Build bem-sucedido! Parando containers antigos..."
docker compose -f "$COMPOSE_FILE" down

echo "Subindo novos containers em modo detached..."
docker compose -f "$COMPOSE_FILE" up -d

if [ $? -ne 0 ]; then
    echo "Erro: Subida dos containers falhou!"
    exit 1
fi

echo "Removendo imagens antigas não utilizadas..."
docker image prune -f

echo "Deploy finalizado!"
