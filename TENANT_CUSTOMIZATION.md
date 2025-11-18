# Customização Multi-Tenant para Venda SaaS

## Resumo das Implementações

Este documento descreve as melhorias implementadas na aplicação Salve Mais para torná-la pronta para venda a terceiros como solução SaaS (Software as a Service).

## 🎨 Funcionalidades Implementadas

### 1. Customização de Marca (Branding)
Cada tenant pode personalizar a aparência da aplicação:
- **Nome de exibição customizado** - Nome da empresa exibido na interface
- **Logo personalizado** - URL do logotipo da empresa
- **Favicon personalizado** - Ícone personalizado no navegador
- **Cores customizáveis**:
  - Cor primária
  - Cor secundária
  - Cor de destaque

### 2. Planos de Assinatura
Sistema de planos flexível com 4 níveis:
- **FREE** - Plano gratuito com recursos limitados
- **BASIC** - Plano básico
- **PREMIUM** - Plano premium com mais recursos
- **ENTERPRISE** - Plano empresarial completo

Controles por plano:
- Limite máximo de usuários
- Limite de armazenamento em GB
- Features habilitadas/desabilitadas por plano
- Período de trial com data de expiração
- Datas de início e fim da assinatura

### 3. Configurações de Notificação
Cada tenant pode configurar suas próprias notificações:
- **SMTP Customizado**:
  - Host e porta SMTP
  - Credenciais de autenticação
  - Email e nome de origem
- **SMS** - Habilitação de notificações por SMS
- **Webhooks** - URL para integração com sistemas externos

### 4. Configurações Regionais
Suporte para internacionalização:
- **Timezone** - Fuso horário da empresa
- **Locale** - Idioma e região (pt_BR, en_US, es_ES, etc.)
- **Moeda** - Código da moeda padrão (BRL, USD, EUR, etc.)
- **Formato de data** - Formato de exibição de datas

### 5. Metadados Customizados
Campo JSON flexível para armazenar configurações específicas de cada tenant.

## 📁 Arquivos Criados/Modificados

### Backend (Java/Spring Boot)

#### Entidade Atualizada:
- `Tenant.java` - Entidade expandida com novos campos

#### DTOs Criados:
- `TenantBrandingDTO.java` - DTO para customização de marca
- `TenantSubscriptionDTO.java` - DTO para configuração de planos
- `TenantSmtpConfigDTO.java` - DTO para configuração SMTP
- `TenantRegionalSettingsDTO.java` - DTO para configurações regionais

#### DTOs Atualizados:
- `TenantDTO.java` - Atualizado com novos campos
- `TenantResponseDTO.java` - Atualizado com novos campos de resposta

#### Serviços:
- `TenantService.java` - Novos métodos:
  - `updateBranding()` - Atualizar marca
  - `updateSubscription()` - Atualizar plano
  - `updateSmtpConfig()` - Atualizar SMTP
  - `updateRegionalSettings()` - Atualizar configurações regionais
  - `findByDomain()` - Buscar tenant por domínio

#### Controllers:
- `TenantController.java` - Novos endpoints:
  - `PUT /api/tenants/{id}/branding` - Atualizar marca
  - `PUT /api/tenants/{id}/subscription` - Atualizar plano
  - `PUT /api/tenants/{id}/smtp` - Atualizar SMTP
  - `PUT /api/tenants/{id}/regional-settings` - Atualizar configurações regionais
  - `GET /api/tenants/domain/{domain}` - Buscar por domínio

#### Migrations:
- `V19__add_tenant_customization_fields.sql` - Migration com novos campos

### Frontend (Angular)

#### Modelos:
- `tenant.model.ts` - Atualizado com novas interfaces:
  - `SubscriptionPlan` enum
  - `Tenant` interface expandida
  - `TenantBrandingDTO`
  - `TenantSubscriptionDTO`
  - `TenantSmtpConfigDTO`
  - `TenantRegionalSettingsDTO`

#### Serviços:
- `tenant.service.ts` - Novos métodos:
  - `getTenantById()`
  - `getTenantByDomain()`
  - `updateBranding()`
  - `updateSubscription()`
  - `updateSmtpConfig()`
  - `updateRegionalSettings()`

#### Componentes Criados:
- `tenant-settings/` - Componente completo para gerenciar configurações:
  - `tenant-settings.component.ts`
  - `tenant-settings.component.html`
  - `tenant-settings.component.css`

## 🚀 Como Utilizar

### 1. Executar Migration
```bash
# No backend, execute a aplicação para rodar a migration automática
# Ou execute manualmente:
flyway migrate
```

### 2. Atualizar Marca do Tenant
```typescript
// No frontend
const branding: TenantBrandingDTO = {
  displayName: 'Minha Empresa',
  logoUrl: 'https://exemplo.com/logo.png',
  faviconUrl: 'https://exemplo.com/favicon.ico',
  primaryColor: '#007bff',
  secondaryColor: '#6c757d',
  accentColor: '#28a745'
};

tenantService.updateBranding(tenantId, branding).subscribe(...);
```

### 3. Configurar Plano de Assinatura
```typescript
const subscription: TenantSubscriptionDTO = {
  subscriptionPlan: SubscriptionPlan.PREMIUM,
  maxUsers: 50,
  maxStorageGb: 100,
  enabledFeatures: {
    'relatorios_avancados': true,
    'exportacao_csv': true,
    'api_access': true
  }
};

tenantService.updateSubscription(tenantId, subscription).subscribe(...);
```

### 4. Acessar Configurações no Frontend
Adicione a rota no `app-routing.module.ts`:
```typescript
{
  path: 'configuracoes-tenant',
  component: TenantSettingsComponent,
  canActivate: [AuthGuard]
}
```

## 🔐 Controle de Acesso

Recomenda-se implementar guards para garantir que apenas administradores do tenant possam acessar as configurações:

```typescript
// Exemplo de verificação
if (user.role !== 'ADMIN') {
  return redirect('/dashboard');
}
```

## 📊 Modelo de Monetização

Com estas implementações, você pode:

1. **Cobrar por plano** - FREE, BASIC, PREMIUM, ENTERPRISE
2. **Limitar recursos** - Usuários, armazenamento, features
3. **Período trial** - Configurar data de expiração
4. **White-label** - Cada cliente com sua própria marca
5. **Multi-região** - Suporte a diferentes idiomas e moedas

## 🎯 Próximos Passos Recomendados

1. **Sistema de Pagamentos** - Integrar com Stripe/PayPal
2. **Analytics por Tenant** - Métricas de uso
3. **Upload de Logo** - Endpoint para upload direto
4. **Temas Dinâmicos** - Aplicar cores em tempo real
5. **Limite de API** - Rate limiting por plano
6. **Backup por Tenant** - Sistema de backup automático
7. **Auditoria** - Log de todas as mudanças de configuração

## 📝 Notas Técnicas

- Todos os campos de customização são opcionais
- Valores padrão são definidos na entidade Tenant
- Campos JSON (enabledFeatures, customMetadata) permitem extensibilidade
- Índices criados para otimizar consultas por plano e datas
- Validações de formato de cor (#RRGGBB) no frontend e backend
- Senha SMTP não é retornada no DTO de resposta por segurança

## 🔒 Segurança

- Senhas SMTP são armazenadas (considere encriptação)
- Tokens de confirmação são invalidados após uso
- Validação de domínio único por tenant
- Validação de email único por tenant

## 📚 Documentação da API

A documentação completa dos endpoints está disponível via Swagger em:
```
http://localhost:8080/swagger-ui.html
```

Endpoints principais:
- `GET /api/tenants` - Listar todos os tenants
- `GET /api/tenants/{id}` - Buscar tenant por ID
- `GET /api/tenants/domain/{domain}` - Buscar por domínio
- `PUT /api/tenants/{id}/branding` - Atualizar marca
- `PUT /api/tenants/{id}/subscription` - Atualizar plano
- `PUT /api/tenants/{id}/smtp` - Configurar SMTP
- `PUT /api/tenants/{id}/regional-settings` - Configurações regionais
