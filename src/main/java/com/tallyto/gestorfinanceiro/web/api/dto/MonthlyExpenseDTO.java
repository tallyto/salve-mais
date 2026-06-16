package com.tallyto.gestorfinanceiro.web.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MonthlyExpenseDTO(
    String mes,
    LocalDate data,
    BigDecimal valorDespesas,
    BigDecimal valorReceitas
) {}
