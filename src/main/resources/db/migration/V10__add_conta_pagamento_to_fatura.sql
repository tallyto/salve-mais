-- Adicionar coluna para conta de pagamento na fatura
ALTER TABLE fatura 
ADD COLUMN conta_pagamento_id BIGINT;

-- Adicionar foreign key constraint
ALTER TABLE fatura 
ADD CONSTRAINT fk_fatura_conta_pagamento 
FOREIGN KEY (conta_pagamento_id) REFERENCES conta(id);

-- Adicionar índices para melhorar performance nas consultas
CREATE INDEX idx_fatura_conta_pagamento ON fatura(conta_pagamento_id);
CREATE INDEX idx_fatura_pago ON fatura(pago);
CREATE INDEX idx_fatura_cartao_pago ON fatura(cartao_credito_id, pago);