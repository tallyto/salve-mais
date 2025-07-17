package com.tallyto.gestorfinanceiro.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RelatorioMensalDTO(
        String mesReferencia,
        LocalDate dataReferencia,
        ResumoFinanceiroDTO resumoFinanceiro,
        List<ItemProventoDTO> proventos,
        List<ItemReceitasPendentesDTO> receitasPendentes,
        List<ItemCartaoDTO> cartoes,
        List<ItemGastoFixoDTO> gastosFixos,
        List<ItemOutrasDescricaoDTO> outrasDespesas,
        BigDecimal saldoFinal,
        BigDecimal totalDividas
) {
    
    public record ResumoFinanceiroDTO(
            BigDecimal totalProventos,
            BigDecimal totalReceitasPendentes,
            BigDecimal totalCartoes,
            BigDecimal totalGastosFixos,
            BigDecimal totalOutrasDespesas,
            BigDecimal saldoFinal,
            BigDecimal totalDividas
    ) {}
    
    public record ItemProventoDTO(
            Long id,
            String descricao,
            BigDecimal valor,
            LocalDate data,
            String contaTitular
    ) {}
    
    public record ItemReceitasPendentesDTO(
            Long id,
            String descricao,
            BigDecimal valor,
            LocalDate dataVencimento,
            String categoria,
            boolean recebido
    ) {}
    
    public record ItemCartaoDTO(
            Long cartaoId,
            String nomeCartao,
            BigDecimal valorTotal,
            LocalDate dataVencimento,
            List<CompraCartaoDTO> compras
    ) {}
    
    public record CompraCartaoDTO(
            Long id,
            String descricao,
            BigDecimal valor,
            LocalDate data,
            String categoria
    ) {}
    
    public record ItemGastoFixoDTO(
            Long id,
            String nome,
            BigDecimal valor,
            LocalDate vencimento,
            String categoria,
            boolean pago
    ) {}
    
    public record ItemOutrasDescricaoDTO(
            Long id,
            String descricao,
            BigDecimal valor,
            LocalDate data,
            String categoria,
            String tipo // "COMPRA", "DESPESA", "OUTROS"
    ) {}
}
