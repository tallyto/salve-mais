-- Adiciona coluna valor_economizado na tabela planos_compra
ALTER TABLE planos_compra 
ADD COLUMN valor_economizado DECIMAL(15, 2) DEFAULT 0 NOT NULL;
