package com.tallyto.gestorfinanceiro.application.services;

import com.tallyto.gestorfinanceiro.web.api.dto.*;
import com.tallyto.gestorfinanceiro.domain.entities.Conta;
import com.tallyto.gestorfinanceiro.domain.entities.ContaFixa;
import com.tallyto.gestorfinanceiro.domain.entities.Fatura;
import com.tallyto.gestorfinanceiro.domain.entities.ReservaEmergencia;
import com.tallyto.gestorfinanceiro.domain.entities.Transacao;
import com.tallyto.gestorfinanceiro.domain.enums.TipoConta;
import com.tallyto.gestorfinanceiro.domain.enums.TipoTransacao;
import com.tallyto.gestorfinanceiro.infrastructure.repositories.ContaFixaRepository;
import com.tallyto.gestorfinanceiro.infrastructure.repositories.ContaRepository;
import com.tallyto.gestorfinanceiro.infrastructure.repositories.FaturaRepository;
import com.tallyto.gestorfinanceiro.infrastructure.repositories.ReservaEmergenciaRepository;
import com.tallyto.gestorfinanceiro.infrastructure.repositories.TransacaoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Service
public class ReservaEmergenciaService {

    @Autowired
    private ReservaEmergenciaRepository reservaEmergenciaRepository;

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private ContaFixaRepository contaFixaRepository;

    @Autowired
    private FaturaRepository faturaRepository;

    @Autowired
    private TransacaoRepository transacaoRepository;

    /**
     * Busca todas as reservas de emergência
     */
    public List<ReservaEmergenciaDTO> findAll() {
        return reservaEmergenciaRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca uma reserva de emergência específica por ID
     */
    public ReservaEmergenciaDetalheDTO findById(Long id) {
        ReservaEmergencia reserva = reservaEmergenciaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reserva de emergência não encontrada"));
        return toDetalheDTO(reserva);
    }

    /**
     * Cria uma nova reserva de emergência usando a conta RESERVA_EMERGENCIA
     * já selecionada pelo usuário. Nunca cria uma conta duplicada.
     */
    @Transactional
    public ReservaEmergenciaDTO create(ReservaEmergenciaInputDTO inputDTO) {
        Conta contaSelecionada = contaRepository.findById(inputDTO.contaId())
                .orElseThrow(() -> new EntityNotFoundException("Conta não encontrada"));

        if (!TipoConta.RESERVA_EMERGENCIA.equals(contaSelecionada.getTipo())) {
            throw new IllegalArgumentException("A conta selecionada deve ser do tipo RESERVA_EMERGENCIA");
        }

        // Atualiza a taxa de rendimento da conta selecionada, se fornecida
        BigDecimal taxa = inputDTO.taxaRendimento() != null
                ? inputDTO.taxaRendimento()
                : BigDecimal.valueOf(13.25);
        contaSelecionada.setTaxaRendimento(taxa);
        contaRepository.save(contaSelecionada);

        ReservaEmergencia reserva = new ReservaEmergencia();
        reserva.setConta(contaSelecionada);
        reserva.setObjetivo(inputDTO.objetivo());
        reserva.setMultiplicadorDespesas(inputDTO.multiplicadorDespesas());
        reserva.setValorContribuicaoMensal(inputDTO.valorContribuicaoMensal());
        reserva.setDataCriacao(LocalDate.now());
        reserva.setSaldoAtual(contaSelecionada.getSaldo());
        reserva.setPercentualConcluido(BigDecimal.ZERO);

        calcularPercentualConcluido(reserva);
        calcularDataPrevisaoCompletar(reserva);

        return toDTO(reservaEmergenciaRepository.save(reserva));
    }

    /**
     * Atualiza uma reserva de emergência existente
     */
    public ReservaEmergenciaDTO update(Long id, ReservaEmergenciaInputDTO inputDTO) {
        ReservaEmergencia reserva = reservaEmergenciaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reserva de emergência não encontrada"));
        
        // Se estiver tentando mudar a conta associada
        if (!reserva.getConta().getId().equals(inputDTO.contaId())) {
            Conta novaConta = contaRepository.findById(inputDTO.contaId())
                .orElseThrow(() -> new EntityNotFoundException("Conta não encontrada"));
            
            // Verifica se a nova conta é do tipo RESERVA_EMERGENCIA
            if (novaConta.getTipo() == null || !novaConta.getTipo().equals(TipoConta.RESERVA_EMERGENCIA)) {
                throw new IllegalArgumentException("Só é possível associar contas do tipo RESERVA_EMERGENCIA");
            }
            
            reserva.setConta(novaConta);
        }
        
        // Atualiza a taxa de rendimento se fornecida
        if (inputDTO.taxaRendimento() != null) {
            Conta conta = reserva.getConta();
            conta.setTaxaRendimento(inputDTO.taxaRendimento());
            contaRepository.save(conta);
        }
        
        reserva.setObjetivo(inputDTO.objetivo());
        reserva.setMultiplicadorDespesas(inputDTO.multiplicadorDespesas());
        reserva.setValorContribuicaoMensal(inputDTO.valorContribuicaoMensal());
        
        // Recalcula percentual concluído
        calcularPercentualConcluido(reserva);
        
        // Recalcula a data prevista para completar a reserva
        calcularDataPrevisaoCompletar(reserva);
        
        return toDTO(reservaEmergenciaRepository.save(reserva));
    }

    /**
     * Remove uma reserva de emergência
     */
    public void delete(Long id) {
        if (!reservaEmergenciaRepository.existsById(id)) {
            throw new EntityNotFoundException("Reserva de emergência não encontrada");
        }
        reservaEmergenciaRepository.deleteById(id);
    }
    
    /**
     * Atualiza o saldo da reserva de emergência
     */
    public ReservaEmergenciaDTO atualizarSaldo(Long id, BigDecimal valor) {
        ReservaEmergencia reserva = reservaEmergenciaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reserva de emergência não encontrada"));
        
        reserva.setSaldoAtual(reserva.getSaldoAtual().add(valor));
        
        // Recalcula percentual concluído
        calcularPercentualConcluido(reserva);
        
        // Recalcula a data prevista para completar a reserva
        calcularDataPrevisaoCompletar(reserva);
        
        return toDTO(reservaEmergenciaRepository.save(reserva));
    }
    
    /**
     * Retorna o histórico de contribuições (depósitos) da reserva,
     * ordenado do mais recente para o mais antigo.
     */
    public List<HistoricoContribuicaoDTO> historico(Long reservaId) {
        ReservaEmergencia reserva = reservaEmergenciaRepository.findById(reservaId)
                .orElseThrow(() -> new EntityNotFoundException("Reserva de emergência não encontrada"));

        Long contaReservaId = reserva.getConta().getId();

        return transacaoRepository.findByConta_IdAndTipo(
                contaReservaId,
                TipoTransacao.TRANSFERENCIA_ENTRADA,
                PageRequest.of(0, 100, Sort.by("data").descending())
        ).stream()
         .map(t -> new HistoricoContribuicaoDTO(
                 t.getId(),
                 t.getValor(),
                 t.getData(),
                 t.getContaDestino() != null ? t.getContaDestino().getTitular() : "—"
         ))
         .collect(Collectors.toList());
    }

    /**
     * Calcula o valor objetivo da reserva com base nas despesas mensais
     */
    public BigDecimal calcularObjetivoAutomatico(Integer multiplicadorDespesas) {
        BigDecimal despesasMensaisMedia = calcularMediaDespesasMensais();
        return despesasMensaisMedia.multiply(BigDecimal.valueOf(multiplicadorDespesas));
    }
    
    /**
     * Simula quanto tempo levará para completar uma reserva de emergência
     */
    public int simularTempoParaCompletar(BigDecimal objetivo, BigDecimal valorContribuicaoMensal) {
        if (valorContribuicaoMensal.compareTo(BigDecimal.ZERO) <= 0) {
            return -1; // Não é possível completar sem contribuição
        }
        
        // Calcula o número de meses necessários
        return objetivo.divide(valorContribuicaoMensal, 0, RoundingMode.CEILING).intValue();
    }
    
    /**
     * Realiza uma contribuição de uma conta para a reserva de emergência,
     * debitando a conta de origem, creditando a conta da reserva e
     * registrando as transações correspondentes.
     */
    @Transactional
    public ReservaEmergenciaDTO contribuirParaReserva(Long reservaId, ContribuicaoReservaDTO contribuicaoDTO) {
        ReservaEmergencia reserva = reservaEmergenciaRepository.findById(reservaId)
                .orElseThrow(() -> new EntityNotFoundException("Reserva de emergência não encontrada"));

        Conta contaOrigem = contaRepository.findById(contribuicaoDTO.contaOrigemId())
                .orElseThrow(() -> new EntityNotFoundException("Conta de origem não encontrada"));

        Conta contaReserva = reserva.getConta();

        if (contaOrigem.getSaldo().compareTo(contribuicaoDTO.valor()) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente na conta de origem");
        }

        // Atualiza saldos
        contaOrigem.setSaldo(contaOrigem.getSaldo().subtract(contribuicaoDTO.valor()));
        contaReserva.setSaldo(contaReserva.getSaldo().add(contribuicaoDTO.valor()));

        contaRepository.save(contaOrigem);
        contaRepository.save(contaReserva);

        // Registra transação de saída na conta de origem
        Transacao saida = new Transacao();
        saida.setTipo(TipoTransacao.TRANSFERENCIA_SAIDA);
        saida.setValor(contribuicaoDTO.valor());
        saida.setData(java.time.LocalDateTime.now());
        saida.setConta(contaOrigem);
        saida.setContaDestino(contaReserva);
        saida.setDescricao("Contribuição para reserva de emergência");
        saida.setSistema(true);
        transacaoRepository.save(saida);

        // Registra transação de entrada na conta da reserva
        Transacao entrada = new Transacao();
        entrada.setTipo(TipoTransacao.TRANSFERENCIA_ENTRADA);
        entrada.setValor(contribuicaoDTO.valor());
        entrada.setData(java.time.LocalDateTime.now());
        entrada.setConta(contaReserva);
        entrada.setContaDestino(contaOrigem);
        entrada.setDescricao("Contribuição para reserva de emergência");
        entrada.setSistema(true);
        transacaoRepository.save(entrada);

        // Atualiza reserva
        reserva.setSaldoAtual(contaReserva.getSaldo());
        calcularPercentualConcluido(reserva);
        calcularDataPrevisaoCompletar(reserva);

        return toDTO(reservaEmergenciaRepository.save(reserva));
    }
    
    /**
     * Calcula a média de despesas mensais dos últimos 6 meses
     */
    private BigDecimal calcularMediaDespesasMensais() {
        // Obtém o mês atual
        YearMonth mesAtual = YearMonth.now();
        
        BigDecimal totalDespesas = BigDecimal.ZERO;
        int mesesConsiderados = 6;
        
        // Calcula despesas dos últimos 6 meses
        for (int i = 0; i < mesesConsiderados; i++) {
            YearMonth mesPesquisa = mesAtual.minusMonths(i);
            LocalDate inicioPeriodo = mesPesquisa.atDay(1);
            LocalDate fimPeriodo = mesPesquisa.atEndOfMonth();
            
            // Soma despesas fixas do período
            BigDecimal despesasFixas = contaFixaRepository.findByVencimentoBetween(inicioPeriodo, fimPeriodo).stream()
                    .map(ContaFixa::getValor)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Soma faturas do período
            BigDecimal despesasFaturas = faturaRepository.findByDataVencimentoBetween(inicioPeriodo, fimPeriodo).stream()
                    .map(Fatura::getValorTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            totalDespesas = totalDespesas.add(despesasFixas).add(despesasFaturas);
        }
        
        // Calcula a média mensal
        if (mesesConsiderados > 0) {
            return totalDespesas.divide(BigDecimal.valueOf(mesesConsiderados), 2, RoundingMode.HALF_UP);
        } else {
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * Calcula o percentual concluído da reserva
     */
    private void calcularPercentualConcluido(ReservaEmergencia reserva) {
        if (reserva.getObjetivo().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal percentual = reserva.getSaldoAtual()
                    .divide(reserva.getObjetivo(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            
            // Limita o percentual a 100%
            if (percentual.compareTo(BigDecimal.valueOf(100)) > 0) {
                percentual = BigDecimal.valueOf(100);
            }
            
            reserva.setPercentualConcluido(percentual);
        } else {
            reserva.setPercentualConcluido(BigDecimal.ZERO);
        }
    }
    
    /**
     * Calcula a data prevista para completar a reserva
     */
    private void calcularDataPrevisaoCompletar(ReservaEmergencia reserva) {
        if (reserva.getValorContribuicaoMensal().compareTo(BigDecimal.ZERO) <= 0) {
            // Se não há contribuição mensal, não é possível estimar
            reserva.setDataPrevisaoCompletar(null);
            return;
        }
        
        BigDecimal valorRestante = reserva.getObjetivo().subtract(reserva.getSaldoAtual());
        
        if (valorRestante.compareTo(BigDecimal.ZERO) <= 0) {
            // Reserva já está completa
            reserva.setDataPrevisaoCompletar(LocalDate.now());
            return;
        }
        
        // Calcula o número de meses necessários
        int mesesNecessarios = valorRestante.divide(reserva.getValorContribuicaoMensal(), 0, RoundingMode.CEILING).intValue();
        
        // Define a data prevista para completar
        reserva.setDataPrevisaoCompletar(LocalDate.now().plusMonths(mesesNecessarios));
    }
    
    /**
     * Converte a entidade para DTO
     */
    private ReservaEmergenciaDTO toDTO(ReservaEmergencia reserva) {
        return new ReservaEmergenciaDTO(
                reserva.getId(),
                reserva.getObjetivo(),
                reserva.getMultiplicadorDespesas(),
                reserva.getSaldoAtual(),
                reserva.getPercentualConcluido(),
                reserva.getDataCriacao(),
                reserva.getDataPrevisaoCompletar(),
                reserva.getValorContribuicaoMensal(),
                reserva.getConta().getId()
        );
    }
    
    /**
     * Converte a entidade para DTO detalhado
     */
    private ReservaEmergenciaDetalheDTO toDetalheDTO(ReservaEmergencia reserva) {
        ContaDTO contaDTO = new ContaDTO(
                reserva.getConta().getId(),
                reserva.getConta().getTitular(),
                reserva.getConta().getSaldo(),
                reserva.getConta().getTipo(),
                reserva.getConta().getTaxaRendimento(),
                reserva.getConta().getDescricao()
        );
        
        int mesesRestantes = 0;
        if (reserva.getDataPrevisaoCompletar() != null) {
            mesesRestantes = (int) ChronoUnit.MONTHS.between(
                    LocalDate.now(),
                    reserva.getDataPrevisaoCompletar()
            );
            if (mesesRestantes < 0) {
                mesesRestantes = 0;
            }
        }
        
        BigDecimal despesasMensaisMedia = calcularMediaDespesasMensais();
        
        return new ReservaEmergenciaDetalheDTO(
                reserva.getId(),
                reserva.getObjetivo(),
                reserva.getMultiplicadorDespesas(),
                reserva.getSaldoAtual(),
                reserva.getPercentualConcluido(),
                reserva.getDataCriacao(),
                reserva.getDataPrevisaoCompletar(),
                reserva.getValorContribuicaoMensal(),
                contaDTO,
                mesesRestantes,
                despesasMensaisMedia
        );
    }
}
