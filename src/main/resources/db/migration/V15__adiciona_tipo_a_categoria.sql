-- Adiciona coluna tipo à tabela categoria
ALTER TABLE categoria
ADD COLUMN tipo VARCHAR(20) DEFAULT 'NECESSIDADE' NOT NULL;
