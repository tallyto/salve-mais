#!/bin/bash
# Script de deploy usando Docker Compose para produção
# Ajuste os caminhos conforme necessário

set -e  # Interrompe o script se qualquer comando falhar

APP_DIR="$HOME/projetos/gestor-financeiro"
COMPOSE_FILE="docker-compose.prod.yml"
BACKUP_DIR="$APP_DIR/backups"
VERSION=$(awk '/<artifactId>gestor-financeiro<\/artifactId>/{getline; print}' pom.xml | grep -oP '(?<=<version>)[^<]+')
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_NAME="backup_${VERSION}_${TIMESTAMP}"

cd "$APP_DIR" || exit 1

# Cria diretório de backups se não existir
mkdir -p "$BACKUP_DIR"

echo "=== Deploy da versão $VERSION ==="

# Verifica se já existe uma imagem com esta versão
if docker image inspect "gestor-financeiro:${VERSION}" >/dev/null 2>&1; then
    echo "Erro: Já existe uma imagem com a versão $VERSION!"
    echo "Por favor, atualize a versão no pom.xml antes de fazer o deploy."
    echo "Versão atual: $VERSION"
    exit 1
fi

# Salva imagem atual como backup
echo "Criando backup da imagem atual..."
CURRENT_IMAGE=$(docker compose -f "$COMPOSE_FILE" images -q app 2>/dev/null || echo "")
if [ -n "$CURRENT_IMAGE" ]; then
    docker tag "$CURRENT_IMAGE" "gestor-financeiro:${BACKUP_NAME}" || true
    echo "$VERSION" > "$BACKUP_DIR/${BACKUP_NAME}.version"
    echo "Backup criado: gestor-financeiro:${BACKUP_NAME}"
fi

echo "Construindo nova imagem versão $VERSION..."
docker compose -f "$COMPOSE_FILE" build --build-arg VERSION="$VERSION"

if [ $? -ne 0 ]; then
    echo "Erro: Build da imagem falhou! Containers antigos não foram afetados."
    exit 1
fi

# Tag da nova imagem com a versão
echo "Criando tag da versão $VERSION..."
NEW_IMAGE=$(docker compose -f "$COMPOSE_FILE" images -q app)
docker tag "$NEW_IMAGE" "gestor-financeiro:${VERSION}"
docker tag "$NEW_IMAGE" "gestor-financeiro:latest"

echo "Build bem-sucedido! Parando containers antigos..."
docker compose -f "$COMPOSE_FILE" down

echo "Subindo novos containers em modo detached..."
docker compose -f "$COMPOSE_FILE" up -d

if [ $? -ne 0 ]; then
    echo "Erro: Subida dos containers falhou!"
    exit 1
fi

# Mantém apenas os últimos 5 backups
echo "Limpando backups antigos (mantendo os 5 mais recentes)..."
cd "$BACKUP_DIR"
ls -t *.version 2>/dev/null | tail -n +6 | while read version_file; do
    backup_tag=$(basename "$version_file" .version)
    docker rmi "gestor-financeiro:${backup_tag}" 2>/dev/null || true
    rm -f "$version_file"
done
cd "$APP_DIR"

echo "Removendo imagens antigas não utilizadas..."
docker image prune -f

echo "=== Deploy finalizado com sucesso! ==="
echo "Versão deployada: $VERSION"
echo "Backup disponível: gestor-financeiro:${BACKUP_NAME}"
