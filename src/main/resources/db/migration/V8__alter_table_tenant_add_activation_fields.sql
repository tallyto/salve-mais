-- Adiciona campos para ativação e token de confirmação
ALTER TABLE public.tenants ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE public.tenants ADD COLUMN IF NOT EXISTS confirmation_token VARCHAR(255);
