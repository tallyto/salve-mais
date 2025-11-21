#!/bin/bash
# Script de rollback para versão anterior
# Uso: ./rollback.sh [versão_backup]

set -e

APP_DIR="$HOME/projetos/gestor-financeiro"
COMPOSE_FILE="docker-compose.prod.yml"
BACKUP_DIR="$APP_DIR/backups"

cd "$APP_DIR" || exit 1

# Lista backups disponíveis
list_backups() {
    echo "=== Backups disponíveis ==="
    if [ ! -d "$BACKUP_DIR" ] || [ -z "$(ls -A $BACKUP_DIR/*.version 2>/dev/null)" ]; then
        echo "Nenhum backup disponível."
        exit 1
    fi
    
    ls -t "$BACKUP_DIR"/*.version 2>/dev/null | while read version_file; do
        backup_name=$(basename "$version_file" .version)
        version=$(cat "$version_file")
        echo "  - $backup_name (versão: $version)"
    done
}

# Se nenhum argumento foi passado, lista os backups
if [ $# -eq 0 ]; then
    list_backups
    echo ""
    echo "Uso: ./rollback.sh <nome_do_backup>"
    echo "Exemplo: ./rollback.sh backup_1.9.1_20251120_205500"
    exit 0
fi

BACKUP_NAME=$1
BACKUP_VERSION_FILE="$BACKUP_DIR/${BACKUP_NAME}.version"

# Verifica se o backup existe
if [ ! -f "$BACKUP_VERSION_FILE" ]; then
    echo "Erro: Backup '$BACKUP_NAME' não encontrado!"
    echo ""
    list_backups
    exit 1
fi

VERSION=$(cat "$BACKUP_VERSION_FILE")
BACKUP_IMAGE="gestor-financeiro:${BACKUP_NAME}"

# Verifica se a imagem do backup existe
if ! docker image inspect "$BACKUP_IMAGE" >/dev/null 2>&1; then
    echo "Erro: Imagem do backup '$BACKUP_IMAGE' não encontrada no Docker!"
    exit 1
fi

echo "=== Iniciando rollback ==="
echo "Voltando para versão: $VERSION"
echo "Backup: $BACKUP_NAME"
echo ""
read -p "Confirma o rollback? (s/N): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Ss]$ ]]; then
    echo "Rollback cancelado."
    exit 0
fi

# Cria backup da versão atual antes do rollback
echo "Criando backup de segurança da versão atual..."
CURRENT_IMAGE=$(docker compose -f "$COMPOSE_FILE" images -q app 2>/dev/null || echo "")
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
if [ -n "$CURRENT_IMAGE" ]; then
    CURRENT_VERSION=$(grep -oP '(?<=<version>)[^<]+' pom.xml | head -1)
    docker tag "$CURRENT_IMAGE" "gestor-financeiro:pre_rollback_${CURRENT_VERSION}_${TIMESTAMP}" || true
fi

echo "Parando containers atuais..."
docker compose -f "$COMPOSE_FILE" down

echo "Restaurando imagem do backup..."
docker tag "$BACKUP_IMAGE" "gestor-financeiro:latest"

# Cria um docker-compose temporário para subir a imagem do backup
cat > /tmp/docker-compose-rollback.yml <<EOF
version: '3.8'
services:
  app:
    image: gestor-financeiro:latest
    container_name: gestor-financeiro-app
    restart: unless-stopped
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
    networks:
      - gestor-network

networks:
  gestor-network:
    driver: bridge
EOF

echo "Subindo container com a versão do backup..."
docker compose -f /tmp/docker-compose-rollback.yml up -d

if [ $? -ne 0 ]; then
    echo "Erro: Falha ao subir container do backup!"
    exit 1
fi

rm -f /tmp/docker-compose-rollback.yml

echo ""
echo "=== Rollback concluído com sucesso! ==="
echo "Versão restaurada: $VERSION"
echo "Container rodando com imagem: $BACKUP_IMAGE"
echo ""
echo "Para voltar à versão mais recente, execute: ./deploy.sh"
