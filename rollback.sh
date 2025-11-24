#!/bin/bash
# Script de rollback para versão anterior
# Uso: ./rollback.sh [versão_backup]

set -e

APP_DIR="$HOME/projetos/salve-mais"
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
        timestamp=$(echo "$backup_name" | grep -oP '\d{8}_\d{6}')
        echo "  - $backup_name (versão: $version, data: $timestamp)"
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
CURRENT_IMAGE=$(docker ps --format "table {{.Image}}" | grep "gestor-financeiro" | head -1 || echo "")
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
if [ -n "$CURRENT_IMAGE" ]; then
    CURRENT_VERSION=$(awk '/<artifactId>gestor-financeiro<\/artifactId>/{getline; print}' pom.xml | grep -oP '(?<=<version>)[^<]+')
    docker tag "$CURRENT_IMAGE" "gestor-financeiro:pre_rollback_${CURRENT_VERSION}_${TIMESTAMP}" || true
fi

echo "Parando containers atuais..."
docker stop $(docker ps -q --filter "name=gestor-financeiro") 2>/dev/null || true
docker rm $(docker ps -aq --filter "name=gestor-financeiro") 2>/dev/null || true

echo "Restaurando imagem do backup..."
docker tag "$BACKUP_IMAGE" "gestor-financeiro:latest"

echo "Subindo container com a versão do backup..."
docker run -d \
    --name gestor-financeiro-app \
    --restart unless-stopped \
    -p 3001:3001 \
    --env-file .env \
    -e SPRING_PROFILES_ACTIVE=prod \
    "gestor-financeiro:latest"

if [ $? -ne 0 ]; then
    echo "Erro: Falha ao subir container do backup!"
    exit 1
fi

echo ""
echo "=== Rollback concluído com sucesso! ==="
echo "Versão restaurada: $VERSION"
echo "Container rodando com imagem: $BACKUP_IMAGE"
echo "Aplicação disponível em: http://localhost:3001"
echo ""
echo "Para voltar à versão mais recente, execute: ./deploy.sh"
