#!/bin/bash

# Script para rodar a aplicação Spring Boot localmente com variáveis de ambiente

# Carregar variáveis do arquivo .env
if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | xargs)
fi

# Sobrescrever configurações para desenvolvimento local
export DB_URL=jdbc:postgresql://localhost:5432/gestor
export DB_USERNAME=gestor
export DB_PASSWORD=gestor@admin
export EMAIL_HOST=localhost
export EMAIL_PORT=1025
export EMAIL_USERNAME=test@localhost
export EMAIL_PASSWORD=test
export EMAIL_SENDER=noreply@localhost
export SPRING_PROFILES_ACTIVE=dev

echo "🚀 Iniciando aplicação em modo de desenvolvimento..."
echo "📧 MailHog: http://localhost:8025"
echo "🗄️  PostgreSQL: localhost:5432"
echo ""

./mvnw spring-boot:run
