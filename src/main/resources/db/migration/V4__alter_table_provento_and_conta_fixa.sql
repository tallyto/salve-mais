ALTER TABLE provento
    ADD COLUMN conta_id BIGINT,
    ADD CONSTRAINT fk_provento_conta FOREIGN KEY (conta_id) REFERENCES conta (id);

ALTER TABLE conta_fixa
    ADD COLUMN conta_id BIGINT,
    ADD CONSTRAINT fk_conta_fixa_conta FOREIGN KEY (conta_id) REFERENCES conta (id);
