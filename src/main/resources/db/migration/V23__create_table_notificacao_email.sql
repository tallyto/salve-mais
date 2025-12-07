CREATE TABLE IF NOT EXISTS public.notificacoes_email
(
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    domain       VARCHAR(255)                        NOT NULL,
    horario      TIME                                NOT NULL,
    ativo        BOOLEAN          DEFAULT TRUE       NOT NULL,
    created_at   TIMESTAMP        DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at   TIMESTAMP        DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by   VARCHAR(100)                        NOT NULL,
    updated_by   VARCHAR(100)                        NOT NULL,
    CONSTRAINT fk_notificacao_tenant FOREIGN KEY (domain) REFERENCES public.tenants (domain) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_notificacao_email_domain ON public.notificacoes_email (domain);
CREATE INDEX IF NOT EXISTS idx_notificacao_email_ativo ON public.notificacoes_email (ativo);
