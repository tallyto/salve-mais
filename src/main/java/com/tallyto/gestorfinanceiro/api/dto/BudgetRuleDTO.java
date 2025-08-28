package com.tallyto.gestorfinanceiro.api.dto;

import java.math.BigDecimal;

/**
 * DTO para representar a regra 50/30/20 nos gastos
 * - 50% Necessidades (despesas essenciais)
 * - 30% Desejos (gastos com lazer)
 * - 20% Economia (poupança e investimentos)
 */
public record BudgetRuleDTO(
    // Valores ideais
    BigDecimal necessidadesIdeal,    // 50% da receita
    BigDecimal desejosIdeal,         // 30% da receita
    BigDecimal economiaIdeal,        // 20% da receita
    
    // Valores reais
    BigDecimal necessidadesReal,     // Valor atual gasto com necessidades
    BigDecimal desejosReal,          // Valor atual gasto com desejos
    BigDecimal economiaReal,         // Valor atual economizado
    
    // Percentuais reais (baseados no total gasto, não na receita)
    double necessidadesPercentual,   // Percentual real de necessidades
    double desejosPercentual,        // Percentual real de desejos
    double economiaPercentual,       // Percentual real de economia
    
    // Diferenças entre ideal e real (valor)
    BigDecimal necessidadesDiferenca, // Diferença entre ideal e real (necessidades)
    BigDecimal desejosDiferenca,      // Diferença entre ideal e real (desejos)
    BigDecimal economiaDiferenca,     // Diferença entre ideal e real (economia)
    
    // Status (dentro do limite, ultrapassando, abaixo)
    String necessidadesStatus,
    String desejosStatus,
    String economiaStatus
) {}
