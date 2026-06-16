package br.com.salvemais.web.api.dto;

import java.math.BigDecimal;

public record CategoryExpenseDTO(
    Long categoriaId,
    String categoriaNome,
    BigDecimal valorTotal,
    double percentual
) {}
