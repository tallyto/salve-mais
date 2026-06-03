-- V32: Adicionar status de assinatura no tenant e criar tabela de planos

-- ============================================================================
-- PARTE 1: Adicionar colunas de assinatura na tabela tenants
-- ============================================================================

ALTER TABLE public.tenants
    ADD COLUMN IF NOT EXISTS subscription_status VARCHAR(20) NOT NULL DEFAULT 'TRIAL',
    ADD COLUMN IF NOT EXISTS stripe_subscription_id VARCHAR(255),
    ADD COLUMN IF NOT EXISTS stripe_customer_id VARCHAR(255);

-- Atualizar tenants existentes: se active=true e sem trial_end_date, setar como ATIVO
UPDATE public.tenants
SET subscription_status = 'ATIVO'
WHERE active = true AND subscription_status = 'TRIAL';

-- ============================================================================
-- PARTE 2: Criar tabela de planos
-- ============================================================================

CREATE TABLE IF NOT EXISTS public.planos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(100) NOT NULL UNIQUE,
    descricao TEXT,
    tipo VARCHAR(20) NOT NULL UNIQUE,
    preco_mensal NUMERIC(10, 2) NOT NULL,
    max_usuarios INTEGER NOT NULL,
    max_transacoes_mes INTEGER,
    max_storage_gb NUMERIC(10, 2),
    stripe_price_id VARCHAR(255),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- PARTE 3: Inserir planos padrão
-- ============================================================================

INSERT INTO public.planos (nome, descricao, tipo, preco_mensal, max_usuarios, max_transacoes_mes, max_storage_gb, ativo)
VALUES
    ('Gratuito',
     'Plano gratuito com funcionalidades básicas. Ideal para uso pessoal.',
     'FREE',
     0.00,
     1,
     50,
     0.50,
     true),

    ('Básico',
     'Plano básico com suporte a múltiplos usuários e mais transações mensais.',
     'BASIC',
     29.90,
     3,
     200,
     2.00,
     true),

    ('Premium',
     'Plano completo com todos os recursos e suporte prioritário.',
     'PREMIUM',
     59.90,
     10,
     1000,
     10.00,
     true),

    ('Enterprise',
     'Plano empresarial sem limites. Ideal para grandes equipes.',
     'ENTERPRISE',
     149.90,
     999,
     NULL,
     100.00,
     true)

ON CONFLICT (tipo) DO NOTHING;

-- ============================================================================
-- PARTE 4: Índices
-- ============================================================================

CREATE INDEX IF NOT EXISTS idx_tenants_subscription_status ON public.tenants(subscription_status);
CREATE INDEX IF NOT EXISTS idx_tenants_trial_end_date ON public.tenants(trial_end_date);
CREATE INDEX IF NOT EXISTS idx_tenants_stripe_customer_id ON public.tenants(stripe_customer_id);
