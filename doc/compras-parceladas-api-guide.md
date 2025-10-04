# API de Compras Parceladas - Guia Rápido

## 🎯 Casos de Uso Práticos

### Cenário 1: Comprar notebook em 10x
```bash
curl -X POST http://localhost:8080/api/compras-parceladas \
  -H "Content-Type: application/json" \
  -d '{
    "descricao": "Notebook Dell Inspiron",
    "valorTotal": 3000.00,
    "dataCompra": "2025-10-03",
    "parcelaInicial": 1,
    "totalParcelas": 10,
    "categoriaId": 5,
    "cartaoId": 2
  }'
```

**Resultado**: Cria 10 parcelas de R$ 300,00 cada
- Parcela 1/10: Vence em 03/10/2025
- Parcela 2/10: Vence em 03/11/2025
- ...
- Parcela 10/10: Vence em 03/07/2026

### Cenário 2: Já paguei 1 parcela, quero cadastrar as 4 restantes
```bash
curl -X POST http://localhost:8080/api/compras-parceladas \
  -H "Content-Type: application/json" \
  -d '{
    "descricao": "Celular Samsung",
    "valorTotal": 500.00,
    "dataCompra": "2025-10-03",
    "parcelaInicial": 2,
    "totalParcelas": 5,
    "categoriaId": 3,
    "cartaoId": 2
  }'
```

**Resultado**: Cria 4 parcelas de R$ 125,00 cada
- Parcela 2/5: Vence em 03/10/2025
- Parcela 3/5: Vence em 03/11/2025
- Parcela 4/5: Vence em 03/12/2025
- Parcela 5/5: Vence em 03/01/2026

### Cenário 3: Ver todas as parcelas que vencem este mês
```bash
curl -X GET "http://localhost:8080/api/compras-parceladas/parcelas/cartao/2?inicio=2025-10-01&fim=2025-10-31"
```

### Cenário 4: Marcar uma parcela como paga
```bash
curl -X PATCH http://localhost:8080/api/compras-parceladas/parcelas/15/pagar
```

### Cenário 5: Ver todas as parcelas não pagas do meu cartão
```bash
curl -X GET http://localhost:8080/api/compras-parceladas/parcelas/nao-pagas/cartao/2
```

### Cenário 6: Ver todas as parcelas vencidas
```bash
curl -X GET http://localhost:8080/api/compras-parceladas/parcelas/vencidas
```

## 📝 Validações

### O que pode dar errado:

1. **Parcela inicial maior que total**
```json
{
  "parcelaInicial": 11,
  "totalParcelas": 10
}
```
❌ Erro: "Parcela inicial (11) não pode ser maior que o total de parcelas (10)"

2. **Parcela inicial menor que 1**
```json
{
  "parcelaInicial": 0,
  "totalParcelas": 10
}
```
❌ Erro: "Parcela inicial deve ser no mínimo 1"

3. **Valor total zero ou negativo**
```json
{
  "valorTotal": 0
}
```
❌ Erro: "Valor total deve ser maior que zero"

## 💡 Dicas de Integração

### Angular Service
```typescript
criarCompraParcelada(compra: CompraParceladaRequest): Observable<CompraParcelada> {
  return this.http.post<CompraParcelada>('/api/compras-parceladas', compra)
    .pipe(
      catchError(error => {
        // error.error.message contém a mensagem de erro detalhada
        console.error('Erro ao criar compra:', error.error.message);
        return throwError(error);
      })
    );
}
```

### React/Fetch
```javascript
async function criarCompraParcelada(compra) {
  try {
    const response = await fetch('/api/compras-parceladas', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(compra)
    });
    
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message);
    }
    
    return await response.json();
  } catch (error) {
    console.error('Erro:', error.message);
    throw error;
  }
}
```

## 🔍 Consultas Úteis

### Listar compras parceladas com paginação
```bash
curl "http://localhost:8080/api/compras-parceladas?page=0&size=10"
```

### Buscar detalhes de uma compra específica
```bash
curl http://localhost:8080/api/compras-parceladas/1
```

### Ver todas as parcelas de uma compra
```bash
curl http://localhost:8080/api/compras-parceladas/1/parcelas
```

### Listar compras de um cartão específico
```bash
curl "http://localhost:8080/api/compras-parceladas/cartao/2?page=0&size=10"
```

## 🎨 Resposta Completa - Exemplo

```json
{
  "id": 1,
  "descricao": "Notebook Dell Inspiron",
  "valorTotal": 3000.00,
  "dataCompra": "2025-10-03",
  "parcelaInicial": 1,
  "totalParcelas": 10,
  "categoriaId": 5,
  "categoriaNome": "Eletrônicos",
  "cartaoId": 2,
  "cartaoNome": "Nubank",
  "parcelasRestantes": 10,
  "valorParcela": 300.00,
  "parcelas": [
    {
      "id": 1,
      "numeroParcela": 1,
      "totalParcelas": 10,
      "valor": 300.00,
      "dataVencimento": "2025-10-03",
      "paga": false,
      "compraParceladaId": 1,
      "descricaoCompra": "Notebook Dell Inspiron"
    },
    {
      "id": 2,
      "numeroParcela": 2,
      "totalParcelas": 10,
      "valor": 300.00,
      "dataVencimento": "2025-11-03",
      "paga": false,
      "compraParceladaId": 1,
      "descricaoCompra": "Notebook Dell Inspiron"
    }
    // ... 8 parcelas restantes
  ]
}
```

## 🗑️ Exclusão

### Excluir uma compra parcelada
```bash
curl -X DELETE http://localhost:8080/api/compras-parceladas/1
```

**Atenção**: Ao excluir uma compra parcelada, todas as suas parcelas são excluídas automaticamente (CASCADE).

## 📊 Status HTTP

| Código | Significado |
|--------|-------------|
| 201 | Compra criada com sucesso |
| 200 | Operação bem-sucedida |
| 204 | Exclusão bem-sucedida (sem conteúdo) |
| 400 | Erro de validação (parâmetros inválidos) |
| 404 | Recurso não encontrado |
| 500 | Erro interno do servidor |
