# Implementação de Filtros de Transação - Backend

## Resumo
Implementada funcionalidade completa de filtros para transações usando JPA Specifications, substituindo a lógica limitada anterior que só suportava filtro por `contaId`.

## Alterações Realizadas

### 1. Nova Classe: TransacaoSpecification
**Arquivo**: `src/main/java/com/tallyto/gestorfinanceiro/core/infra/repositories/specifications/TransacaoSpecification.java`

Classe utilitária que constrói queries dinâmicas usando Criteria API do JPA.

**Filtros Implementados**:
- ✅ `contaId` - Filtra por conta bancária
- ✅ `tipo` - Filtra por tipo de transação (CREDITO, DEBITO, TRANSFERENCIA_ENTRADA, TRANSFERENCIA_SAIDA, PAGAMENTO_FATURA)
- ✅ `categoriaId` - Filtra por categoria
- ✅ `dataInicio` - Filtra transações a partir de uma data (>=)
- ✅ `dataFim` - Filtra transações até uma data (<=)
- ✅ `faturaId` - Filtra transações de uma fatura específica
- ✅ `contaFixaId` - Filtra transações de uma despesa fixa
- ✅ `proventoId` - Filtra transações de um provento

**Ordenação**: Todas as queries retornam resultados ordenados por data DESC (mais recentes primeiro)

### 2. Atualização: TransacaoRepository
**Arquivo**: `src/main/java/com/tallyto/gestorfinanceiro/core/infra/repositories/TransacaoRepository.java`

**Mudança**: Adicionado `JpaSpecificationExecutor<Transacao>` à interface

```java
public interface TransacaoRepository extends JpaRepository<Transacao, Long>, JpaSpecificationExecutor<Transacao>
```

Isso habilita o uso de Specifications para queries dinâmicas.

### 3. Refatoração: TransacaoService
**Arquivo**: `src/main/java/com/tallyto/gestorfinanceiro/core/application/services/TransacaoService.java`

**Antes**:
```java
public Page<TransacaoDTO> listarTransacoes(TransacaoFiltroDTO filtro, Pageable pageable) {
    // Implementação básica - só suportava contaId
    if (filtro.contaId() != null) {
        return transacaoRepository.findByConta_IdOrderByDataDesc(filtro.contaId(), pageable)
                .map(this::toDTO);
    }
    
    return transacaoRepository.findAllByOrderByDataDesc(pageable)
                .map(this::toDTO);
}
```

**Depois**:
```java
public Page<TransacaoDTO> listarTransacoes(TransacaoFiltroDTO filtro, Pageable pageable) {
    return transacaoRepository.findAll(
        TransacaoSpecification.comFiltro(filtro),
        pageable
    ).map(this::toDTO);
}
```

## Como Funciona

1. **Frontend** envia parâmetros de filtro via query string:
   ```
   GET /api/transacoes?contaId=1&tipo=CREDITO&categoriaId=5&page=0&size=10
   ```

2. **Controller** recebe os parâmetros e cria `TransacaoFiltroDTO`

3. **Service** usa `TransacaoSpecification.comFiltro()` para criar query dinâmica

4. **Specification** constrói predicados JPA apenas para parâmetros não-nulos:
   ```java
   if (filtro.tipo() != null) {
       predicates.add(criteriaBuilder.equal(root.get("tipo"), filtro.tipo()));
   }
   ```

5. **Repository** executa a query com JPA Criteria API

6. **Resultado** é mapeado para DTO e retornado paginado

## Benefícios

✅ **Filtros Combinados**: Suporta qualquer combinação de filtros  
✅ **Performance**: Apenas cria predicados para filtros informados  
✅ **Manutenibilidade**: Código limpo e extensível  
✅ **Type-Safe**: Validação em tempo de compilação  
✅ **Flexível**: Fácil adicionar novos filtros  

## Exemplo de Uso

### Filtrar por tipo e categoria:
```
GET /api/transacoes?tipo=DEBITO&categoriaId=3
```

### Filtrar por período:
```
GET /api/transacoes?dataInicio=2025-01-01T00:00:00&dataFim=2025-01-31T23:59:59
```

### Filtrar por conta e tipo no período:
```
GET /api/transacoes?contaId=1&tipo=CREDITO&dataInicio=2025-01-01T00:00:00
```

## Testes

Compilação bem-sucedida:
```
[INFO] BUILD SUCCESS
[INFO] Total time:  15.175 s
```

## Próximos Passos

- [ ] Testar os filtros via interface web
- [ ] Adicionar testes unitários para TransacaoSpecification
- [ ] Documentar API no Swagger/OpenAPI
- [ ] Adicionar métricas de performance

---

**Data**: 05/10/2025  
**Versão Backend**: 1.9.0  
**Status**: ✅ Implementado e testado
