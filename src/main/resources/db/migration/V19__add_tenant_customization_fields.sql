-- Migration para adicionar campos de customização ao Tenant
-- Configurações de Marca
ALTER TABLE public.tenants ADD COLUMN IF NOT EXISTS display_name VARCHAR(255);
ALTER TABLE public.tenants ADD COLUMN IF NOT EXISTS logo_url VARCHAR(500);
ALTER TABLE public.tenants ADD COLUMN IF NOT EXISTS favicon_url VARCHAR(500);
ALTER TABLE public.tenants ADD COLUMN IF NOT EXISTS primary_color VARCHAR(7);
ALTER TABLE public.tenants ADD COLUMN IF NOT EXISTS secondary_color VARCHAR(7);
ALTER TABLE public.tenants ADD COLUMN IF NOT EXISTS accent_color VARCHAR(7);

-- Configurações de Plano e Recursos
ALTER TABLE public.tenants ADD COLUMN IF NOT EXISTS subscription_plan VARCHAR(50) NOT NULL DEFAULT 'FREE';
ALTER TABLE public.tenants ADD COLUMN IF NOT EXISTS max_users INTEGER;
ALTER TABLE public.tenants ADD COLUMN IF NOT EXISTS max_storage_gb DECIMAL(10,2);
ALTER TABLE public.tenants ADD COLUMN IF NOT EXISTS trial_end_date TIMESTAMP;
ALTER TABLE public.tenants ADD COLUMN IF NOT EXISTS subscription_start_date TIMESTAMP;
ALTER TABLE public.tenants ADD COLUMN IF NOT EXISTS subscription_end_date TIMESTAMP;
ALTER TABLE public.tenants ADD COLUMN IF NOT EXISTS enabled_features JSONB;

-- Configurações de Notificação
ALTER TABLE public.tenants ADD COLUMN IF NOT EXISTS custom_smtp_host VARCHAR(255);
ALTER TABLE public.tenants ADD COLUMN IF NOT EXISTS custom_smtp_port INTEGER;
ALTER TABLE public.tenants ADD COLUMN IF NOT EXISTS custom_smtp_user VARCHAR(255);
ALTER TABLE public.tenants ADD COLUMN IF NOT EXISTS custom_smtp_password VARCHAR(255);
ALTER TABLE public.tenants ADD COLUMN IF NOT EXISTS custom_smtp_from_email VARCHAR(255);
ALTER TABLE public.tenants ADD COLUMN IF NOT EXISTS custom_smtp_from_name VARCHAR(255);
ALTER TABLE public.tenants ADD COLUMN IF NOT EXISTS sms_enabled BOOLEAN DEFAULT FALSE;
ALTER TABLE public.tenants ADD COLUMN IF NOT EXISTS webhook_url VARCHAR(500);

-- Configurações Regionais e Localização
ALTER TABLE public.tenants ADD COLUMN IF NOT EXISTS timezone VARCHAR(50) DEFAULT 'America/Sao_Paulo';
ALTER TABLE public.tenants ADD COLUMN IF NOT EXISTS locale VARCHAR(10) DEFAULT 'pt_BR';
ALTER TABLE public.tenants ADD COLUMN IF NOT EXISTS currency_code VARCHAR(3) DEFAULT 'BRL';
ALTER TABLE public.tenants ADD COLUMN IF NOT EXISTS date_format VARCHAR(20) DEFAULT 'dd/MM/yyyy';

-- Metadados Customizados
ALTER TABLE public.tenants ADD COLUMN IF NOT EXISTS custom_metadata JSONB;

-- Criar índices para melhorar performance
CREATE INDEX IF NOT EXISTS idx_tenants_subscription_plan ON public.tenants(subscription_plan);
CREATE INDEX IF NOT EXISTS idx_tenants_trial_end_date ON public.tenants(trial_end_date);
CREATE INDEX IF NOT EXISTS idx_tenants_subscription_end_date ON public.tenants(subscription_end_date);

