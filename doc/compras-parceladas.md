# Funcionalidade de Compras Parceladas

## Visão Geral

Este documento descreve a funcionalidade de compras parceladas implementada no sistema Salve Mais. A funcionalidade permite que usuários cadastrem compras parceladas no cartão de crédito, podendo escolher a parcela inicial e o total de parcelas.

## Casos de Uso

### Exemplo 1: Compra completa parcelada
- **Cenário**: Compra de R$ 1.000,00 em 10x
- **Configuração**: `parcelaInicial=1`, `totalParcelas=10`
- **Resultado**: Gera 10 parcelas de R$ 100,00 cada

### Exemplo 2: Compra com parcelas já pagas
- **Cenário**: Compra de R$ 500,00 em 5x, já foram pagas 1 parcela
- **Configuração**: `parcelaInicial=2`, `totalParcelas=5`
- **Resultado**: Gera 4 parcelas restantes (2/5, 3/5, 4/5, 5/5) de R$ 125,00 cada

## Arquitetura

### Entidades

#### CompraParcelada
```java
- id: Long
- descricao: String
- valorTotal: BigDecimal
- dataCompra: LocalDate
- parcelaInicial: Integer (mínimo: 1)
- totalParcelas: Integer (mínimo: 1)
- categoria: Categoria
- cartaoCredito: CartaoCredito
- parcelas: List<Parcela>
```

#### Parcela
```java
- id: Long
- numeroParcela: Integer
- totalParcelas: Integer
- valor: BigDecimal
- dataVencimento: LocalDate
- paga: Boolean
- compraParcelada: CompraParcelada
```

### Banco de Dados

**Migration**: `V18__create_table_compra_parcelada_e_parcela.sql`

- Tabela `compra_parcelada` com constraints para validar parcelas
- Tabela `parcela` com relacionamento em cascata
- Índices para otimizar consultas por cartão, data e status de pagamento

### Regras de Negócio

1. **Validação de Parcelas**:
   - `parcelaInicial >= 1`
   - `parcelaInicial <= totalParcelas`
   - `totalParcelas > 0`

2. **Cálculo de Parcelas**:
   - Número de parcelas geradas: `totalParcelas - parcelaInicial + 1`
   - Valor por parcela: `valorTotal / número de parcelas geradas`
   - Ajuste de arredondamento na primeira parcela para garantir soma exata

3. **Data de Vencimento**:
   - Primeira parcela vence na `dataCompra`
   - Parcelas subsequentes vencem 1 mês após a anterior

4. **Integração com Faturas**:
   - Parcelas devem ser incluídas na fatura do cartão de acordo com a data de vencimento
   - Implementação futura: método no `FaturaService` para buscar parcelas do período

## API REST

### Endpoints

#### POST /api/compras-parceladas
Cria uma nova compra parcelada.

**Request Body**:
```json
{
  "descricao": "Notebook Dell",
  "valorTotal": 3000.00,
  "dataCompra": "2025-10-03",
  "parcelaInicial": 1,
  "totalParcelas": 10,
  "categoriaId": 5,
  "cartaoId": 2
}
```

**Response**: `CompraParceladaResponseDTO` com status 201 Created

#### GET /api/compras-parceladas
Lista todas as compras parceladas (paginado).

#### GET /api/compras-parceladas/{id}
Busca uma compra parcelada por ID.

#### GET /api/compras-parceladas/cartao/{cartaoId}
Lista compras parceladas de um cartão específico (paginado).

#### GET /api/compras-parceladas/{id}/parcelas
Lista parcelas de uma compra parcelada.

#### GET /api/compras-parceladas/parcelas/cartao/{cartaoId}
Busca parcelas por cartão e período.

**Query Params**: `inicio` (LocalDate), `fim` (LocalDate)

#### PATCH /api/compras-parceladas/parcelas/{parcelaId}/pagar
Marca uma parcela como paga.

#### PATCH /api/compras-parceladas/parcelas/{parcelaId}/despagar
Desmarca uma parcela como paga.

#### DELETE /api/compras-parceladas/{id}
Exclui uma compra parcelada e todas as suas parcelas.

#### GET /api/compras-parceladas/parcelas/nao-pagas/cartao/{cartaoId}
Lista parcelas não pagas de um cartão.

#### GET /api/compras-parceladas/parcelas/vencidas
Lista todas as parcelas vencidas (não pagas com data de vencimento anterior a hoje).

## Serviços

### CompraParceladaService

Principais métodos:
- `criarCompraParcelada()`: Cria compra e gera parcelas automaticamente
- `gerarParcelas()`: Gera as parcelas com cálculo de valores e datas
- `marcarParcelaComoPaga()`: Marca parcela individual como paga
- `listarParcelasPorCartaoEPeriodo()`: Busca parcelas para inclusão em fatura

## Testes

Testes unitários cobrem:
- ✅ Criação de compra com parcelas completas (1/10)
- ✅ Criação de compra com parcela inicial diferente de 1 (2/5)
- ✅ Validação de parcela inicial inválida
- ✅ Validação de parcela inicial maior que total
- ✅ Marcação de parcela como paga
- ✅ Busca de compra por ID
- ✅ Exclusão de compra parcelada

## Frontend (Próximos Passos)

### Modelos TypeScript Sugeridos

```typescript
export interface CompraParcelada {
  id: number;
  descricao: string;
  valorTotal: number;
  dataCompra: string;
  parcelaInicial: number;
  totalParcelas: number;
  categoriaId: number;
  categoriaNome: string;
  cartaoId: number;
  cartaoNome: string;
  parcelas: Parcela[];
  parcelasRestantes: number;
  valorParcela: number;
}

export interface Parcela {
  id: number;
  numeroParcela: number;
  totalParcelas: number;
  valor: number;
  dataVencimento: string;
  paga: boolean;
  compraParceladaId: number;
  descricaoCompra: string;
}

export interface CompraParceladaRequest {
  descricao: string;
  valorTotal: number;
  dataCompra: string;
  parcelaInicial: number;
  totalParcelas: number;
  categoriaId: number;
  cartaoId: number;
}
```

### Componentes Sugeridos

1. **compra-parcelada-form**: Formulário para cadastrar nova compra parcelada
2. **list-compras-parceladas**: Lista de compras parceladas com filtros
3. **parcelas-list**: Lista de parcelas com opção de marcar como paga
4. **compra-parcelada-detalhe**: Visualização detalhada de uma compra com suas parcelas

## Melhorias Futuras

1. **Integração completa com Faturas**:
   - Incluir parcelas automaticamente na geração de faturas
   - Criar DTO unificado para itens da fatura (compras + parcelas)

2. **Notificações**:
   - Notificar usuário sobre parcelas próximas ao vencimento
   - Alertar sobre parcelas vencidas não pagas

3. **Relatórios**:
   - Gráfico de parcelas pagas vs. não pagas
   - Projeção de gastos futuros baseado em parcelas pendentes

4. **Validação de Limite**:
   - Verificar se há limite disponível no cartão para todas as parcelas

5. **Edição de Parcelas**:
   - Permitir ajustar valores de parcelas específicas
   - Antecipar ou postergar vencimentos

## Conclusão

A funcionalidade de compras parceladas está completamente implementada no backend com:
- ✅ Entidades de domínio
- ✅ Migrations de banco de dados
- ✅ Repositórios com queries otimizadas
- ✅ DTOs para request/response
- ✅ Service com lógica de negócio
- ✅ Controller REST com endpoints completos
- ✅ Testes unitários
- ✅ Documentação no FaturaService para integração futura

Próximo passo: Implementar o frontend em Angular para consumir a API.
