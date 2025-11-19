-- Remove funcionalidade de personalização de cores do tenant

-- Remove as colunas de cores da tabela tenants
ALTER TABLE public.tenants DROP COLUMN IF EXISTS primary_color;
ALTER TABLE public.tenants DROP COLUMN IF EXISTS secondary_color; 
ALTER TABLE public.tenants DROP COLUMN IF EXISTS accent_color;