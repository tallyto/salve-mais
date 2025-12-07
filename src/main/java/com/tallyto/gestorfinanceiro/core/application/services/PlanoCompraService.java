package com.tallyto.gestorfinanceiro.core.application.services;

import com.tallyto.gestorfinanceiro.api.dto.PlanoCompraDTO;
import com.tallyto.gestorfinanceiro.core.domain.PlanoCompra;
import com.tallyto.gestorfinanceiro.core.domain.repositories.PlanoCompraRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlanoCompraService {

    private final PlanoCompraRepository planoCompraRepository;

    @Transactional
    public PlanoCompraDTO criar(PlanoCompraDTO dto) {
        PlanoCompra plano = PlanoCompra.builder()
                .nome(dto.getNome())
                .descricao(dto.getDescricao())
                .valorTotal(dto.getValorTotal())
                .valorEconomizado(dto.getValorEconomizado() != null ? dto.getValorEconomizado() : java.math.BigDecimal.ZERO)
                .valorEntrada(dto.getValorEntrada())
                .numeroParcelas(dto.getNumeroParcelas())
                .taxaJuros(dto.getTaxaJuros())
                .tipoCompra(PlanoCompra.TipoCompra.valueOf(dto.getTipoCompra()))
                .dataPrevista(dto.getDataPrevista())
                .prioridade(dto.getPrioridade() != null ? dto.getPrioridade() : 3)
                .status(PlanoCompra.StatusPlano.PLANEJADO)
                .observacoes(dto.getObservacoes())
                .build();

        plano.calcularParcela();
        plano = planoCompraRepository.save(plano);

        log.info("Plano de compra criado: {}", plano.getNome());
        return converterParaDTO(plano);
    }

    @Transactional
    public PlanoCompraDTO atualizar(Long id, PlanoCompraDTO dto) {
        PlanoCompra plano = planoCompraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plano de compra não encontrado"));

        plano.setNome(dto.getNome());
        plano.setDescricao(dto.getDescricao());
        plano.setValorTotal(dto.getValorTotal());
        plano.setValorEconomizado(dto.getValorEconomizado() != null ? dto.getValorEconomizado() : java.math.BigDecimal.ZERO);
        plano.setValorEntrada(dto.getValorEntrada());
        plano.setNumeroParcelas(dto.getNumeroParcelas());
        plano.setTaxaJuros(dto.getTaxaJuros());
        plano.setTipoCompra(PlanoCompra.TipoCompra.valueOf(dto.getTipoCompra()));
        plano.setDataPrevista(dto.getDataPrevista());
        plano.setPrioridade(dto.getPrioridade());
        plano.setObservacoes(dto.getObservacoes());

        if (dto.getStatus() != null) {
            plano.setStatus(PlanoCompra.StatusPlano.valueOf(dto.getStatus()));
        }

        plano.calcularParcela();
        plano = planoCompraRepository.save(plano);

        log.info("Plano de compra atualizado: {}", plano.getId());
        return converterParaDTO(plano);
    }

    @Transactional(readOnly = true)
    public List<PlanoCompraDTO> listarTodos() {
        return planoCompraRepository.findAllByOrderByPrioridadeAscDataPrevistaAsc().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PlanoCompraDTO> listarPorStatus(String status) {
        PlanoCompra.StatusPlano statusPlano = PlanoCompra.StatusPlano.valueOf(status);
        return planoCompraRepository.findByStatusOrderByPrioridadeAscDataPrevistaAsc(statusPlano).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PlanoCompraDTO buscarPorId(Long id) {
        PlanoCompra plano = planoCompraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plano de compra não encontrado"));
        return converterParaDTO(plano);
    }

    @Transactional
    public void deletar(Long id) {
        PlanoCompra plano = planoCompraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plano de compra não encontrado"));
        planoCompraRepository.delete(plano);
        log.info("Plano de compra deletado: {}", id);
    }

    private PlanoCompraDTO converterParaDTO(PlanoCompra plano) {
        return PlanoCompraDTO.builder()
                .id(plano.getId())
                .nome(plano.getNome())
                .descricao(plano.getDescricao())
                .valorTotal(plano.getValorTotal())
                .valorEconomizado(plano.getValorEconomizado())
                .valorEntrada(plano.getValorEntrada())
                .numeroParcelas(plano.getNumeroParcelas())
                .taxaJuros(plano.getTaxaJuros())
                .valorParcela(plano.getValorParcela())
                .tipoCompra(plano.getTipoCompra().name())
                .dataPrevista(plano.getDataPrevista())
                .prioridade(plano.getPrioridade())
                .status(plano.getStatus().name())
                .observacoes(plano.getObservacoes())
                .valorFinal(plano.calcularValorFinal())
                .jurosTotal(plano.calcularJurosTotal())
                .percentualEconomizado(plano.calcularPercentualEconomizado())
                .build();
    }
}
