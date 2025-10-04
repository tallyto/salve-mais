package com.tallyto.gestorfinanceiro.api.dto;

import com.tallyto.gestorfinanceiro.core.domain.entities.CompraParcelada;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public record CompraParceladaResponseDTO(
        Long id,
        String descricao,
        BigDecimal valorTotal,
        LocalDate dataCompra,
        Integer parcelaInicial,
        Integer totalParcelas,
        Long categoriaId,
        String categoriaNome,
        Long cartaoId,
        String cartaoNome,
        List<ParcelaDTO> parcelas,
        Integer parcelasRestantes,
        BigDecimal valorParcela
) {
    public static CompraParceladaResponseDTO fromEntity(CompraParcelada compraParcelada) {
        List<ParcelaDTO> parcelasDTO = compraParcelada.getParcelas() != null
                ? compraParcelada.getParcelas().stream()
                    .map(ParcelaDTO::fromEntity)
                    .collect(Collectors.toList())
                : List.of();

        int parcelasRestantes = compraParcelada.getTotalParcelas() - compraParcelada.getParcelaInicial() + 1;
        BigDecimal valorParcela = !parcelasDTO.isEmpty() ? parcelasDTO.get(0).valor() : BigDecimal.ZERO;

        return new CompraParceladaResponseDTO(
                compraParcelada.getId(),
                compraParcelada.getDescricao(),
                compraParcelada.getValorTotal(),
                compraParcelada.getDataCompra(),
                compraParcelada.getParcelaInicial(),
                compraParcelada.getTotalParcelas(),
                compraParcelada.getCategoria() != null ? compraParcelada.getCategoria().getId() : null,
                compraParcelada.getCategoria() != null ? compraParcelada.getCategoria().getNome() : null,
                compraParcelada.getCartaoCredito().getId(),
                compraParcelada.getCartaoCredito().getNome(),
                parcelasDTO,
                parcelasRestantes,
                valorParcela
        );
    }
}
