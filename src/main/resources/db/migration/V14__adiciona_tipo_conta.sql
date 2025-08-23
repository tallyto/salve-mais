-- Adiciona tipo, taxa de rendimento e descrição à tabela conta
ALTER TABLE conta
    ADD COLUMN tipo VARCHAR(50) NOT NULL DEFAULT 'CORRENTE',
    ADD COLUMN taxa_rendimento DECIMAL(5,2),
    ADD COLUMN descricao VARCHAR(255);

-- Atualiza as contas existentes para definir o tipo apropriado
-- Inicialmente todas são CORRENTE por padrão, mas você pode adicionar atualizações específicas se necessário
-- Exemplo:
-- UPDATE conta SET tipo = 'POUPANCA' WHERE id = X;
