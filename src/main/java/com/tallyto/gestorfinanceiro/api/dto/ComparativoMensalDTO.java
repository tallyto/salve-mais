package com.tallyto.gestorfinanceiro.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record ComparativoMensalDTO(
        String mesAnterior,
        String mesAtual,
        ResumoComparativoDTO resumoComparativo,
        List<ComparativoCategoriaDTO> categorias,
        List<DestaqueMudancaDTO> maioresVariacoes
) {
    
    public record ResumoComparativoDTO(
            BigDecimal totalProventosAnterior,
            BigDecimal totalProventosAtual,
            BigDecimal variacaoProventos,
            BigDecimal percentualProventos,
            
            BigDecimal totalDespesasAnterior,
            BigDecimal totalDespesasAtual,
            BigDecimal variacaoDespesas,
            BigDecimal percentualDespesas,
            
            BigDecimal saldoAnterior,
            BigDecimal saldoAtual,
            BigDecimal variacaoSaldo,
            BigDecimal percentualSaldo,
            
            String statusGeral // "MELHOROU", "PIOROU", "ESTAVEL"
    ) {}
    
    public record ComparativoCategoriaDTO(
            String categoria,
            String tipo, // "DESPESA" ou "PROVENTO"
            BigDecimal valorAnterior,
            BigDecimal valorAtual,
            BigDecimal variacao,
            BigDecimal percentual,
            String tendencia // "AUMENTO", "REDUCAO", "ESTAVEL"
    ) {}
    
    public record DestaqueMudancaDTO(
            String categoria,
            String tipo,
            BigDecimal variacaoAbsoluta,
            BigDecimal percentual,
            String impacto // "POSITIVO", "NEGATIVO", "NEUTRO"
    ) {}
}
