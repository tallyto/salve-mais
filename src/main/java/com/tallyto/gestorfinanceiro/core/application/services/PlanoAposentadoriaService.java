package com.tallyto.gestorfinanceiro.core.application.services;

import com.tallyto.gestorfinanceiro.api.dto.PlanoAposentadoriaDTO;
import com.tallyto.gestorfinanceiro.core.domain.PlanoAposentadoria;
import com.tallyto.gestorfinanceiro.core.domain.repositories.PlanoAposentadoriaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlanoAposentadoriaService {

    private final PlanoAposentadoriaRepository planoAposentadoriaRepository;

    @Transactional
    public PlanoAposentadoriaDTO criar(PlanoAposentadoriaDTO dto) {
        if (planoAposentadoriaRepository.existsByAtivoTrue()) {
            throw new RuntimeException("Já existe um plano de aposentadoria ativo");
        }

        if (dto.getIdadeAposentadoria() <= dto.getIdadeAtual()) {
            throw new RuntimeException("Idade de aposentadoria deve ser maior que a idade atual");
        }

        PlanoAposentadoria plano = PlanoAposentadoria.builder()
                .idadeAtual(dto.getIdadeAtual())
                .idadeAposentadoria(dto.getIdadeAposentadoria())
                .rendaDesejada(dto.getRendaDesejada())
                .patrimonioAtual(dto.getPatrimonioAtual() != null ? dto.getPatrimonioAtual() : BigDecimal.ZERO)
                .contribuicaoMensal(dto.getContribuicaoMensal())
                .taxaRetornoAnual(dto.getTaxaRetornoAnual())
                .expectativaVida(dto.getExpectativaVida() != null ? dto.getExpectativaVida() : 85)
                .ativo(true)
                .build();

        plano.calcularPatrimonioNecessario();
        plano.calcularPatrimonioProjetado();
        plano = planoAposentadoriaRepository.save(plano);

        log.info("Plano de aposentadoria criado");
        return converterParaDTO(plano);
    }

    @Transactional
    public PlanoAposentadoriaDTO atualizar(PlanoAposentadoriaDTO dto) {
        PlanoAposentadoria plano = planoAposentadoriaRepository.findFirstByAtivoTrue()
                .orElseThrow(() -> new RuntimeException("Plano de aposentadoria não encontrado"));

        if (dto.getIdadeAposentadoria() <= dto.getIdadeAtual()) {
            throw new RuntimeException("Idade de aposentadoria deve ser maior que a idade atual");
        }

        plano.setIdadeAtual(dto.getIdadeAtual());
        plano.setIdadeAposentadoria(dto.getIdadeAposentadoria());
        plano.setRendaDesejada(dto.getRendaDesejada());
        plano.setPatrimonioAtual(dto.getPatrimonioAtual());
        plano.setContribuicaoMensal(dto.getContribuicaoMensal());
        plano.setTaxaRetornoAnual(dto.getTaxaRetornoAnual());
        plano.setExpectativaVida(dto.getExpectativaVida());

        plano.calcularPatrimonioNecessario();
        plano.calcularPatrimonioProjetado();
        plano = planoAposentadoriaRepository.save(plano);

        log.info("Plano de aposentadoria atualizado: {}", plano.getId());
        return converterParaDTO(plano);
    }

    @Transactional(readOnly = true)
    public PlanoAposentadoriaDTO buscar() {
        PlanoAposentadoria plano = planoAposentadoriaRepository.findFirstByAtivoTrue()
                .orElseThrow(() -> new RuntimeException("Plano de aposentadoria não encontrado"));
        return converterParaDTO(plano);
    }

    @Transactional
    public void deletar() {
        PlanoAposentadoria plano = planoAposentadoriaRepository.findFirstByAtivoTrue()
                .orElseThrow(() -> new RuntimeException("Plano de aposentadoria não encontrado"));
        planoAposentadoriaRepository.delete(plano);
        log.info("Plano de aposentadoria deletado: {}", plano.getId());
    }

    private PlanoAposentadoriaDTO converterParaDTO(PlanoAposentadoria plano) {
        return PlanoAposentadoriaDTO.builder()
                .id(plano.getId())
                .idadeAtual(plano.getIdadeAtual())
                .idadeAposentadoria(plano.getIdadeAposentadoria())
                .rendaDesejada(plano.getRendaDesejada())
                .patrimonioAtual(plano.getPatrimonioAtual())
                .contribuicaoMensal(plano.getContribuicaoMensal())
                .taxaRetornoAnual(plano.getTaxaRetornoAnual())
                .expectativaVida(plano.getExpectativaVida())
                .patrimonioNecessario(plano.getPatrimonioNecessario())
                .patrimonioProjetado(plano.getPatrimonioProjetado())
                .ativo(plano.getAtivo())
                .deficitOuSuperavit(plano.calcularDeficitOuSuperavit())
                .contribuicaoMensalNecessaria(plano.calcularContribuicaoMensalNecessaria())
                .status(plano.avaliarStatus())
                .anosAteAposentadoria(plano.calcularAnosAteAposentadoria())
                .anosAposAposentadoria(plano.calcularAnosAposAposentadoria())
                .build();
    }
}
