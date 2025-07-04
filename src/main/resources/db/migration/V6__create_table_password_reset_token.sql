-- Migration: Criação da tabela password_reset_token
CREATE TABLE password_reset_token (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    expiry TIMESTAMP NOT NULL
);
