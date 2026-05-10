#!/bin/bash
# Script de deploy para produção usando docker-compose.prod.yml
# Uso: ./deploy.sh

set -e

# Configurações
APP_NAME="gestor-financeiro"
COMPOSE_FILE="docker-compose.prod.yml"
IMAGE_NAME="gestor-financeiro:latest"

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Função para erro e saída
error_exit() {
    echo -e "${RED}ERRO: $1${NC}"
    exit 1
}

# Função para sucesso
success() {
    echo -e "${GREEN}✓ $1${NC}"
}

# Função para informação
info() {
    echo -e "${BLUE}ℹ $1${NC}"
}

# Função para aviso
warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

# Verificar se Docker e Docker Compose estão instalados
check_dependencies() {
    info "Verificando dependências..."
    
    if ! command -v docker &> /dev/null; then
        error_exit "Docker não está instalado no Runner!"
    fi
    
    if ! docker compose version &> /dev/null; then
        error_exit "Docker Compose plugin não está instalado!"
    fi
    
    success "Dependências verificadas"
}

# Verificar se arquivo .env existe (Ajustado para GitHub Actions)
check_env_file() {
    info "Verificando configurações de ambiente..."
    
    if [ ! -f ".env" ]; then
        # Se não houver .env, avisamos mas não travamos o deploy
        # pois as variáveis podem estar vindo do shell do GitHub Runner
        warning "Arquivo .env não encontrado. Usando variáveis de ambiente do Shell."
    else
        success "Arquivo .env local encontrado"
    fi
}

# Parar serviços atuais
stop_services() {
    info "Lidando com serviços atuais..."
    
    # Verifica se há containers rodando para este arquivo de compose
    if docker compose -f "$COMPOSE_FILE" ps --quiet | grep -q .; then
        docker compose -f "$COMPOSE_FILE" down
        success "Serviços anteriores parados"
    else
        info "Nenhum serviço rodando anteriormente"
    fi
}

# Construir nova imagem
build_image() {
    info "Construindo nova imagem na LXC remota..."
    
    # O contexto de build é enviado via rede pelo Docker Client
    docker compose -f "$COMPOSE_FILE" build
    
    success "Imagem construída com sucesso"
}

# Verificar estrutura do projeto
check_project_structure() {
    info "Verificando estrutura do projeto..."
    
    if [ ! -f "pom.xml" ]; then
        error_exit "Arquivo pom.xml não encontrado!"
    fi
    
    if [ ! -f "Dockerfile" ]; then
        error_exit "Dockerfile não encontrado!"
    fi
    
    success "Estrutura do projeto verificada"
}

# Iniciar serviços
start_services() {
    info "Iniciando novos containers..."
    
    docker compose -f "$COMPOSE_FILE" up -d
    
    success "Serviços iniciados com sucesso"
}

# Limpeza de imagens antigas (Ajustado para evitar erros de shell)
cleanup() {
    info "Limpando imagens órfãs..."
    
    # Remove apenas imagens 'dangling' (sem tag) para poupar espaço no Proxmox
    DANGLING_IMAGES=$(docker images -f "dangling=true" -q)
    if [ -n "$DANGLING_IMAGES" ]; then
        docker rmi $DANGLING_IMAGES 2>/dev/null || true
        success "Limpeza concluída"
    else
        info "Nenhuma imagem órfã para limpar"
    fi
}

# Mostrar status final
show_status() {
    info "Status final do deploy na LXC 192.168.0.105:"
    echo
    docker compose -f "$COMPOSE_FILE" ps
    echo
    success "Deploy concluído com sucesso!"
    info "Logs: docker compose -f $COMPOSE_FILE logs -f"
}

# Função principal
main() {
    echo -e "${BLUE}=== Iniciando deploy em $(date) ===${NC}"
    
    # Verificar se o arquivo de compose existe
    if [ ! -f "$COMPOSE_FILE" ]; then
        error_exit "Arquivo $COMPOSE_FILE não encontrado na raiz!"
    fi
    
    info "Usando arquivo de compose: $COMPOSE_FILE"
    
    check_dependencies
    check_env_file
    check_project_structure
    stop_services
    build_image
    start_services
    cleanup
    show_status
    
    echo -e "${GREEN}=== Deploy finalizado em $(date) ===${NC}"
}

trap 'error_exit "Deploy interrompido pelo usuário"' INT TERM

main "$@"
