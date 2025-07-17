# Sistema de Limites para Cartão de Crédito

## Funcionalidades Implementadas

### 1. Configuração de Limites
- Definir limite total do cartão
- Configurar percentual para alertas (padrão: 80%)
- Ativar/desativar cartões

### 2. Monitoramento em Tempo Real
- Cálculo automático do valor utilizado no mês
- Verificação de limite disponível
- Alertas quando atingir o percentual configurado
- Bloqueio de compras que excedem o limite

### 3. Relatórios e Status
- Status detalhado de cada cartão
- Lista de cartões em alerta
- Histórico de utilização

## Endpoints da API

### Configurar Limite do Cartão
```http
PUT /api/cartao-credito/{id}/limite
Content-Type: application/json

{
    "cartaoId": 1,
    "limiteTotal": 5000.00,
    "limiteAlertaPercentual": 80
}
```

### Verificar Status do Limite
```http
GET /api/cartao-credito/{id}/limite/status
```

**Resposta:**
```json
{
    "cartaoId": 1,
    "nomeCartao": "Cartão Principal",
    "limiteTotal": 5000.00,
    "valorUtilizado": 3200.00,
    "limiteDisponivel": 1800.00,
    "percentualUtilizado": 64.00,
    "limiteExcedido": false,
    "alertaAtivado": false,
    "limiteAlertaPercentual": 80
}
```

### Listar Status de Todos os Cartões
```http
GET /api/cartao-credito/limite/status
```

### Verificar Cartões em Alerta
```http
GET /api/cartao-credito/limite/alertas
```

### Calcular Limite Disponível
```http
GET /api/cartao-credito/{id}/limite/disponivel
```

### Verificar se Compra Pode ser Realizada
```http
POST /api/cartao-credito/{id}/limite/verificar-compra
Content-Type: application/json

{
    "valor": 250.00
}
```

**Resposta:**
```json
{
    "podeRealizar": true,
    "valorCompra": 250.00,
    "limiteDisponivel": 1800.00
}
```

## Validações Automáticas

### No Cadastro de Compras
- O sistema agora verifica automaticamente se a compra excede o limite
- Compras que excedem o limite são rejeitadas com erro 400

### Cálculo do Período
- O sistema considera o mês atual para cálculo de limite utilizado
- Baseado no ciclo mensal do cartão (1º ao último dia do mês)

## Banco de Dados

### Novos Campos na Tabela `cartao_credito`
- `limite_total`: Valor do limite do cartão
- `limite_alerta_percentual`: Percentual para disparo de alerta (padrão: 80%)
- `ativo`: Indica se o cartão está ativo

### Migration Aplicada
- V10__alter_cartao_credito_add_limite.sql

## Como Usar

1. **Configure o limite do cartão:**
   ```bash
   curl -X PUT http://localhost:8080/api/cartao-credito/1/limite \
   -H "Content-Type: application/json" \
   -d '{"cartaoId": 1, "limiteTotal": 5000.00, "limiteAlertaPercentual": 80}'
   ```

2. **Monitore o status:**
   ```bash
   curl http://localhost:8080/api/cartao-credito/1/limite/status
   ```

3. **Verifique alertas:**
   ```bash
   curl http://localhost:8080/api/cartao-credito/limite/alertas
   ```

## Próximos Passos Sugeridos

1. **Frontend:** Criar interfaces para configurar e monitorar limites
2. **Notificações:** Sistema de alertas por email/push
3. **Histórico:** Registrar histórico de alterações de limite
4. **Relatórios:** Gráficos de evolução de utilização de limite
