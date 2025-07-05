CREATE TABLE IF NOT EXISTS public.tenants
(
    id           UUID PRIMARY KEY                    NOT NULL,
    domain       VARCHAR(255) UNIQUE                 NOT NULL,
    name         VARCHAR(255)                        NOT NULL,
    email        VARCHAR(255) UNIQUE                 NOT NULL,
    phone_number VARCHAR(15),
    address      TEXT,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by   VARCHAR(100)                        NOT NULL,
    updated_by   VARCHAR(100)                        NOT NULL
);

DO $$
    BEGIN
        -- Verifica se o tenant já existe
        IF NOT EXISTS (
            SELECT 1
            FROM public.tenants
            WHERE domain = 'lyto.com.br'
        ) THEN
            -- Insere o tenant se ele não existir
            INSERT INTO public.tenants (id, domain, name, email, phone_number, address, created_at, updated_at, created_by, updated_by)
            VALUES (gen_random_uuid(), 'lyto.com.br', 'Gerenciador Financeiro', 'contato@lyto.com.br', '+5595981243461',
                    'Nilo Cairo 36, Apto 1605, Centro, Curitiba - PR', NOW(), NOW(), 'system', 'system');
        END IF;
    END $$;
