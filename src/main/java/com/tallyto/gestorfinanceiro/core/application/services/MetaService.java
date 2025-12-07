package com.tallyto.gestorfinanceiro.core.application.services;

import com.tallyto.gestorfinanceiro.api.dto.MetaAtualizarProgressoDTO;
import com.tallyto.gestorfinanceiro.api.dto.MetaDTO;
import com.tallyto.gestorfinanceiro.core.domain.Meta;
import com.tallyto.gestorfinanceiro.core.domain.repositories.MetaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetaService {

    private final MetaRepository metaRepository;

    @Transactional
    public MetaDTO criar(MetaDTO dto) {
        Meta meta = Meta.builder()
                .nome(dto.getNome())
                .descricao(dto.getDescricao())
                .valorAlvo(dto.getValorAlvo())
                .valorAtual(dto.getValorAtual() != null ? dto.getValorAtual() : BigDecimal.ZERO)
                .dataInicio(dto.getDataInicio())
                .dataAlvo(dto.getDataAlvo())
                .categoriaId(dto.getCategoriaId())
                .status(Meta.StatusMeta.EM_ANDAMENTO)
                .icone(dto.getIcone())
                .cor(dto.getCor())
                .notificarProgresso(dto.getNotificarProgresso() != null ? dto.getNotificarProgresso() : false)
                .build();

        calcularValorMensalSugerido(meta);
        meta = metaRepository.save(meta);

        log.info("Meta criada: {}", meta.getNome());
        return converterParaDTO(meta);
    }

    @Transactional
    public MetaDTO atualizar(Long id, MetaDTO dto) {
        Meta meta = metaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meta não encontrada"));

        meta.setNome(dto.getNome());
        meta.setDescricao(dto.getDescricao());
        meta.setValorAlvo(dto.getValorAlvo());
        meta.setDataAlvo(dto.getDataAlvo());
        meta.setCategoriaId(dto.getCategoriaId());
        meta.setIcone(dto.getIcone());
        meta.setCor(dto.getCor());
        meta.setNotificarProgresso(dto.getNotificarProgresso());

        if (dto.getStatus() != null) {
            meta.setStatus(Meta.StatusMeta.valueOf(dto.getStatus()));
        }

        calcularValorMensalSugerido(meta);
        meta = metaRepository.save(meta);

        log.info("Meta atualizada: {}", meta.getId());
        return converterParaDTO(meta);
    }

    @Transactional
    public MetaDTO atualizarProgresso(Long id, MetaAtualizarProgressoDTO dto) {
        Meta meta = metaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meta não encontrada"));

        meta.setValorAtual(meta.getValorAtual().add(dto.getValor()));

        // Se atingiu ou ultrapassou a meta, marcar como concluída
        if (meta.getValorAtual().compareTo(meta.getValorAlvo()) >= 0) {
            meta.setStatus(Meta.StatusMeta.CONCLUIDA);
        }

        meta = metaRepository.save(meta);

        log.info("Progresso atualizado para meta: {}. Valor adicionado: {}", meta.getId(), dto.getValor());
        return converterParaDTO(meta);
    }

    @Transactional(readOnly = true)
    public List<MetaDTO> listarTodas() {
        return metaRepository.findAllByOrderByDataAlvoAsc().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MetaDTO> listarPorStatus(String status) {
        Meta.StatusMeta statusMeta = Meta.StatusMeta.valueOf(status);
        return metaRepository.findByStatusOrderByDataAlvoAsc(statusMeta).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MetaDTO buscarPorId(Long id) {
        Meta meta = metaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meta não encontrada"));
        return converterParaDTO(meta);
    }

    @Transactional
    public void deletar(Long id) {
        Meta meta = metaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meta não encontrada"));
        metaRepository.delete(meta);
        log.info("Meta deletada: {}", id);
    }

    private void calcularValorMensalSugerido(Meta meta) {
        long mesesRestantes = ChronoUnit.MONTHS.between(
                LocalDate.now().withDayOfMonth(1),
                meta.getDataAlvo().withDayOfMonth(1)
        );

        if (mesesRestantes <= 0) {
            meta.setValorMensalSugerido(BigDecimal.ZERO);
            return;
        }

        BigDecimal valorRestante = meta.getValorAlvo().subtract(meta.getValorAtual());
        meta.setValorMensalSugerido(
                valorRestante.divide(BigDecimal.valueOf(mesesRestantes), 2, RoundingMode.HALF_UP)
        );
    }

    private MetaDTO converterParaDTO(Meta meta) {
        return MetaDTO.builder()
                .id(meta.getId())
                .nome(meta.getNome())
                .descricao(meta.getDescricao())
                .valorAlvo(meta.getValorAlvo())
                .valorAtual(meta.getValorAtual())
                .dataInicio(meta.getDataInicio())
                .dataAlvo(meta.getDataAlvo())
                .categoriaId(meta.getCategoriaId())
                .status(meta.getStatus().name())
                .valorMensalSugerido(meta.getValorMensalSugerido())
                .icone(meta.getIcone())
                .cor(meta.getCor())
                .notificarProgresso(meta.getNotificarProgresso())
                .percentualConcluido(meta.calcularPercentualConcluido())
                .valorRestante(meta.calcularValorRestante())
                .diasRestantes(meta.calcularDiasRestantes())
                .build();
    }
}
