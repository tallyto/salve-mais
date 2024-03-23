-- Limpar a base de dados
DELETE FROM conta_fixa;
DELETE FROM provento;
DELETE FROM transacao;
DELETE FROM conta;
DELETE FROM fatura_compra;
DELETE FROM fatura;
DELETE FROM compra;
DELETE FROM categoria;
DELETE FROM cartao_credito;

-- Zerar auto_increment
ALTER TABLE provento AUTO_INCREMENT = 1;
ALTER TABLE conta_fixa AUTO_INCREMENT = 1;
ALTER TABLE compra AUTO_INCREMENT = 1;
ALTER TABLE categoria AUTO_INCREMENT = 1;
ALTER TABLE cartao_credito AUTO_INCREMENT = 1;
ALTER TABLE fatura AUTO_INCREMENT = 1;
ALTER TABLE conta AUTO_INCREMENT = 1;
ALTER TABLE transacao AUTO_INCREMENT = 1;

-- Inserir dados na tabela cartao_credito
INSERT INTO cartao_credito (nome, vencimento)
VALUES
    ('Visa Infinity', '2023-11-10'),
    ('Santander Elite', '2023-12-15'),
    ('Latam Gold', '2023-10-30'),
    ('C6 Bank', '2023-12-10'),
    ('Inter', '2023-11-25');

-- Inserir dados na tabela categoria
INSERT INTO categoria (nome)
VALUES
    ('Alimentação'),
    ('Transporte'),
    ('Moradia'),
    ('Lazer'),
    ('Saúde'),
    ('Educação'),
    ('Vestuário'),
    ('Supermercado'),
    ('Combustível'),
    ('Impostos'),
    ('Clube'),
    ('Material Escolar'),
    ('Cinema e Entretenimento'),
    ('Seguro'),
    ('Piscina'),
    ('Taxas Bancárias'),
    ('Auxílios'),
    ('Investimentos'),
    ('Outros');

-- Inserir dados na tabela compra
INSERT INTO compra (descricao, valor, data, categoria_id, cartao_credito_id)
VALUES
    ('Eletrônicos Incríveis', 120.00, '2023-10-02', 1, 1),
    ('Livros Fantásticos', 80.00, '2023-10-03', 2, 2),
    ('Acessórios Estilosos', 50.00, '2023-10-05', 3, 3),
    ('Gadgets Incríveis', 200.00, '2023-10-08', 4, 4),
    ('Roupas da Moda', 150.00, '2023-10-10', 5, 5),
    ('Jantar Gourmet', 70.00, '2023-10-12', 1, 1),
    ('Ingressos para Show', 100.00, '2023-10-15', 2, 2),
    ('Café Exclusivo', 30.00, '2023-10-18', 3, 1),
    ('Tecnologia Inovadora', 180.00, '2023-10-20', 4, 3),
    ('Experiência Gastronomica', 90.00, '2023-10-22', 5, 4),
    ('Artigos de Esporte', 120.00, '2023-10-24', 1, 4),
    ('Viagem Aventureira', 300.00, '2023-10-26', 2, 1),
    ('Concerto Exclusivo', 50.00, '2023-10-28', 3, 2),
    ('Ferramentas de DIY', 70.00, '2023-10-29', 4, 3),
    ('Decoração Criativa', 100.00, '2023-10-30', 5, 4),
    ('Assinatura de Revista', 40.00, '2023-10-17', 1, 5),
    ('Arte Exclusiva', 60.00, '2023-10-19', 2, 1),
    ('Equipamento de Fitness', 80.00, '2023-10-23', 3, 2),
    ('Produtos de Beleza', 25.00, '2023-10-25', 4, 3),
    ('Plantas para Casa', 35.00, '2023-10-27', 5, 4);



-- Inserir dados na tabela conta
INSERT INTO conta (id, saldo, titular) VALUES (1, 1000.00, 'João Silva');
INSERT INTO conta (id, saldo, titular) VALUES (2, 500.00, 'Maria Santos');
INSERT INTO conta (id, saldo, titular) VALUES (3, 750.00, 'Pedro Oliveira');
INSERT INTO conta (id, saldo, titular) VALUES (4, 2000.00, 'Ana Costa');
INSERT INTO conta (id, saldo, titular) VALUES (5, 300.00, 'Luiza Pereira');

-- Inserir dados na tabela conta_fixa
INSERT INTO conta_fixa (nome, categoria_id, vencimento, valor, pago, conta_id)
VALUES
    ('Gás', 4, '2023-12-10', 70.00, 1,1),
    ('Telefone', 4, '2023-12-08', 40.00, 0,2),
    ('IPTU', 3, '2023-12-20', 150.00, 0,3),
    ('Condomínio', 3, '2023-12-05', 200.00, 1,4),
    ('TV a cabo', 4, '2023-11-30', 80.00, 1,5),
    ('Água', 4, '2023-12-12', 60.00, 0,3),
    ('Manutenção do carro', 2, '2023-11-28', 100.00, 1,1),
    ('Escola dos filhos', 1, '2023-12-03', 300.00, 1,2),
    ('Plano de saúde', 6, '2023-11-22', 250.00, 1,3),
    ('Manutenção do computador', 7, '2023-12-18', 50.00, 0,4),
    ('Manutenção do ar condicionado', 7, '2023-11-20', 80.00, 1,5),
    ('Supermercado', 8, '2023-12-08', 200.00, 0,1),
    ('Combustível', 2, '2023-12-10', 60.00, 1,1),
    ('Imposto de renda', 9, '2023-12-30', 300.00, 0,2),
    ('Mensalidade do clube', 5, '2023-11-25', 50.00, 1,3),
    ('Material escolar', 1, '2023-12-05', 100.00, 0,4),
    ('Cinema e lazer', 10, '2023-12-15', 30.00, 1,5),
    ('Seguro residencial', 9, '2023-11-30', 120.00, 0,3),
    ('Manutenção da piscina', 11, '2023-12-18', 70.00, 1,2),
    ('Taxa bancária', 12, '2023-12-22', 10.00, 1,3);

-- Inserir dados na tabela provento associados às contas
INSERT INTO provento (descricao, valor, data, conta_id)
VALUES
    ('Gratificação', 300.00, '2023-12-10', 1),
    ('Incentivo de vendas', 150.00, '2023-12-05', 1),
    ('Consultoria', 120.00, '2023-11-25', 1),
    ('Rendimento de poupança', 20.00, '2023-12-08', 2),
    ('Comissão de vendas', 80.00, '2023-11-28', 2),
    ('Bônus de desempenho', 200.00, '2023-12-03', 2),
    ('Aumento salarial', 400.00, '2023-11-22', 3),
    ('Restituição de imposto', 50.00, '2023-12-15', 3),
    ('Prêmio por metas atingidas', 120.00, '2023-12-18', 3),
    ('Bônus de fidelidade', 70.00, '2023-12-01', 4),
    ('Auxílio alimentação', 150.00, '2023-12-05', 4),
    ('Dividendos de ações', 80.00, '2023-11-28', 4),
    ('Rendimento de previdência privada', 120.00, '2023-12-01', 5),
    ('Venda de produtos online', 50.00, '2023-12-10', 5),
    ('Prêmio de produtividade', 180.00, '2023-12-15', 5),
    ('Trabalho freelancer de design', 90.00, '2023-12-18', 5);