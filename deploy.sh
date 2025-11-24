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
        error_exit "Docker não está instalado!"
    fi
    
    if ! command -v docker compose &> /dev/null; then
        error_exit "Docker Compose não está instalado!"
    fi
    
    success "Dependências verificadas"
}

# Verificar se arquivo .env existe
check_env_file() {
    info "Verificando arquivo de configuração..."
    
    if [ ! -f ".env" ]; then
        error_exit "Arquivo .env não encontrado! Certifique-se de que o arquivo .env com as configurações da VPS está presente."
    fi
    
    success "Arquivo .env encontrado"
}



# Parar serviços atuais
stop_services() {
    info "Parando serviços atuais..."
    
    if docker-compose -f "$COMPOSE_FILE" ps | grep -q "Up"; then
        docker compose -f "$COMPOSE_FILE" down
        success "Serviços parados"
    else
        info "Nenhum serviço rodando"
    fi
}

# Construir nova imagem
build_image() {
    info "Construindo nova imagem..."
    
    # Build da aplicação e imagem Docker (usando cache para otimização)
    docker compose -f "$COMPOSE_FILE" build
    
    success "Imagem construída com sucesso"
}

# Verificar estrutura do projeto
check_project_structure() {
    info "Verificando estrutura do projeto..."
    
    # Verificar se os arquivos essenciais existem
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
    info "Iniciando serviços..."
    
    docker compose -f "$COMPOSE_FILE" up -d
    
    success "Serviços iniciados"
}

# Verificar saúde da aplicação
health_check() {
    info "Verificando saúde da aplicação..."
    
    # Aguardar a aplicação inicializar
    sleep 30
    
    # Verificar se o container está rodando
    if ! docker compose -f "$COMPOSE_FILE" ps | grep -q "Up"; then
        error_exit "Container não está rodando após o deploy"
    fi
    
    # Tentar fazer uma requisição de health check (se disponível)
    if command -v curl &> /dev/null; then
        for i in {1..5}; do
            if curl -f -s http://localhost:3001/actuator/health >/dev/null 2>&1; then
                success "Aplicação está saudável"
                return 0
            fi
            warning "Tentativa $i/5 falhou, aguardando 10 segundos..."
            sleep 10
        done
        warning "Health check falhou, mas container está rodando"
    else
        warning "curl não disponível, pulando health check HTTP"
    fi
    
    success "Container verificado"
}

# Limpeza de imagens antigas
cleanup() {
    info "Limpando imagens antigas..."
    
    # Remover imagens dangling
    if docker images -f "dangling=true" -q | head -1 | grep -q .; then
        docker rmi $(docker images -f "dangling=true" -q) 2>/dev/null || true
        success "Imagens órfãs removidas"
    fi
}

# Mostrar status final
show_status() {
    info "Status final do deploy:"
    echo
    docker compose -f "$COMPOSE_FILE" ps
    echo
    success "Deploy concluído com sucesso!"
    info "Aplicação disponível em: http://localhost:3001"
    info "Logs podem ser visualizados com: docker compose -f $COMPOSE_FILE logs -f"
}

# Função principal
main() {
    echo -e "${BLUE}=== Iniciando deploy em $(date) ===${NC}"
    
    # Verificar se estamos no diretório correto
    if [ ! -f "$COMPOSE_FILE" ]; then
        error_exit "Arquivo $COMPOSE_FILE não encontrado! Execute o script no diretório raiz do projeto."
    fi
    
    check_dependencies
    check_env_file
    check_project_structure
    stop_services
    build_image
    start_services
    health_check
    cleanup
    show_status
    
    echo -e "${GREEN}=== Deploy finalizado em $(date) ===${NC}"
}

# Tratamento de sinais para limpeza em caso de interrupção
trap 'error_exit "Deploy interrompido pelo usuário"' INT TERM

# Executar função principal
main "$@"
