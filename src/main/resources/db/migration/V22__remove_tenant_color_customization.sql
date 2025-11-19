-- Remove funcionalidade de personalização de cores do tenant

-- Remove as colunas de cores da tabela tenant
ALTER TABLE tenant DROP COLUMN IF EXISTS primary_color;
ALTER TABLE tenant DROP COLUMN IF EXISTS secondary_color; 
ALTER TABLE tenant DROP COLUMN IF EXISTS accent_color;