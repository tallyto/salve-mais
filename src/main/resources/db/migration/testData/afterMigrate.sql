-- Limpar a base de dados
delete from fatura_compra;
delete from fatura;
delete from provento;
delete from conta_fixa;
delete from compra;
delete from categoria;
delete from cartao_credito;

-- Zerar auto_increment
ALTER TABLE provento AUTO_INCREMENT = 1;
ALTER TABLE conta_fixa AUTO_INCREMENT = 1;
ALTER TABLE compra AUTO_INCREMENT = 1;
ALTER TABLE categoria AUTO_INCREMENT = 1;
ALTER TABLE cartao_credito AUTO_INCREMENT = 1;
alter table fatura auto_increment = 1;

-- Inserir dados na tabela cartao_credito
INSERT INTO cartao_credito (nome, vencimento)
VALUES ('Visa Infinity', '2023-11-10'),
       ('Santander Elite', '2023-12-15'),
       ('Latam Gold', '2023-10-30'),
       ('C6 Bank', '2023-12-10'),
       ('Inter', '2023-11-25');

-- Inserir dados na tabela categoria
INSERT INTO categoria (nome)
VALUES ('Alimentação'),
       ('Transporte'),
       ('Moradia'),
       ('Lazer'),
       ('Saúde');

-- Inserir dados na tabela compra
INSERT INTO compra (descricao, valor, data, categoria_id, cartao_credito_id)
VALUES
    ('Eletrônicos Incríveis', RAND() * 100, '2023-10-02', 1, 1),
    ('Livros Fantásticos', RAND() * 100, '2023-10-03', 2, 2),
    ('Acessórios Estilosos', RAND() * 100, '2023-10-05', 3, 3),
    ('Gadgets Incríveis', RAND() * 100, '2023-10-08', 4, 4),
    ('Roupas da Moda', RAND() * 100, '2023-10-10', 5, 5),
    ('Jantar Gourmet', RAND() * 100, '2023-10-12', 1, 1),
    ('Ingressos para Show', RAND() * 100, '2023-10-15', 2, 2),
    ('Café Exclusivo', RAND() * 100, '2023-10-18', 3, 1),
    ('Tecnologia Inovadora', RAND() * 100, '2023-10-20', 4, 3),
    ('Experiência Gastronomica', RAND() * 100, '2023-10-22', 5, 4),
    ('Artigos de Esporte', RAND() * 100, '2023-10-24', 1, 4),
    ('Viagem Aventureira', RAND() * 100, '2023-10-26', 2, 1),
    ('Concerto Exclusivo', RAND() * 100, '2023-10-28', 3, 2),
    ('Ferramentas de DIY', RAND() * 100, '2023-10-29', 4, 3),
    ('Decoração Criativa', RAND() * 100, '2023-10-30', 5, 4),
    ('Assinatura de Revista', RAND() * 100, '2023-10-17', 1, 5),
    ('Arte Exclusiva', RAND() * 100, '2023-10-19', 2, 1),
    ('Equipamento de Fitness', RAND() * 100, '2023-10-23', 3, 2),
    ('Produtos de Beleza', RAND() * 100, '2023-10-25', 4, 3),
    ('Plantas para Casa', RAND() * 100, '2023-10-27', 5, 4);

-- Inserir dados na tabela conta_fixa
INSERT INTO conta_fixa (nome, categoria_id, vencimento, valor, pago)
VALUES ('Aluguel', 3, '2023-12-05', 1200.00, 1),
       ('Internet', 4, '2023-11-28', 50.00, 1),
       ('Energia', 4, '2023-12-15', 100.00, 0),
       ('Academia', 5, '2023-12-01', 80.00, 1),
       ('Seguro do carro', 2, '2023-11-25', 200.00, 1);

-- Inserir dados na tabela provento
INSERT INTO provento (descricao, valor, data)
VALUES ('Salário', 2500.00, '2023-12-01'),
       ('Bônus', 500.00, '2023-12-15'),
       ('Freelance', 200.00, '2023-11-30'),
       ('Rendimento de investimentos', 50.00, '2023-12-10'),
       ('Prêmio', 100.00, '2023-11-25');