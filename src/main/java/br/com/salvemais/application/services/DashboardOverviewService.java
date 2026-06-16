package br.com.salvemais.application.services;

import br.com.salvemais.domain.entities.Categoria;
import br.com.salvemais.domain.entities.Compra;
import br.com.salvemais.domain.entities.CompraDebito;
import br.com.salvemais.domain.entities.Conta;
import br.com.salvemais.domain.entities.ContaFixa;
import br.com.salvemais.domain.entities.Fatura;
import br.com.salvemais.domain.entities.Parcela;
import br.com.salvemais.domain.entities.Provento;
import br.com.salvemais.domain.entities.ReservaEmergencia;
import br.com.salvemais.domain.enums.TipoConta;
import br.com.salvemais.infrastructure.repositories.CompraDebitoRepository;
import br.com.salvemais.infrastructure.repositories.ContaFixaRepository;
import br.com.salvemais.infrastructure.repositories.ContaRepository;
import br.com.salvemais.infrastructure.repositories.CategoriaRepository;
import br.com.salvemais.infrastructure.repositories.FaturaRepository;
import br.com.salvemais.infrastructure.repositories.ParcelaRepository;
import br.com.salvemais.infrastructure.repositories.ProventoRepository;
import br.com.salvemais.infrastructure.repositories.ReservaEmergenciaRepository;
import br.com.salvemais.web.api.dto.BudgetRuleDTO;
import br.com.salvemais.web.api.dto.DashboardSummaryDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DashboardOverviewService {

    private static final BigDecimal PERCENTUAL_NECESSIDADES = new BigDecimal("0.5");
    private static final BigDecimal PERCENTUAL_DESEJOS = new BigDecimal("0.3");
    private static final BigDecimal PERCENTUAL_ECONOMIA = new BigDecimal("0.2");

    private final ContaRepository contaRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProventoRepository proventoRepository;
    private final ContaFixaRepository contaFixaRepository;
    private final FaturaRepository faturaRepository;
    private final ReservaEmergenciaRepository reservaEmergenciaRepository;
    private final ParcelaRepository parcelaRepository;
    private final CompraDebitoRepository compraDebitoRepository;

    public DashboardOverviewService(ContaRepository contaRepository,
                                    CategoriaRepository categoriaRepository,
                                    ProventoRepository proventoRepository,
                                    ContaFixaRepository contaFixaRepository,
                                    FaturaRepository faturaRepository,
                                    ReservaEmergenciaRepository reservaEmergenciaRepository,
                                    ParcelaRepository parcelaRepository,
                                    CompraDebitoRepository compraDebitoRepository) {
        this.contaRepository = contaRepository;
        this.categoriaRepository = categoriaRepository;
        this.proventoRepository = proventoRepository;
        this.contaFixaRepository = contaFixaRepository;
        this.faturaRepository = faturaRepository;
        this.reservaEmergenciaRepository = reservaEmergenciaRepository;
        this.parcelaRepository = parcelaRepository;
        this.compraDebitoRepository = compraDebitoRepository;
    }

    public DashboardSummaryDTO getSummary(Integer mes, Integer ano) {
        BigDecimal saldoTotal = contaRepository.findAll().stream()
                .filter(conta -> !TipoConta.RESERVA_EMERGENCIA.equals(conta.getTipo()))
                .map(Conta::getSaldo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        YearMonth mesAtual = mes != null && ano != null ? YearMonth.of(ano, mes) : YearMonth.now();
        LocalDate inicioMesAtual = mesAtual.atDay(1);
        LocalDate fimMesAtual = mesAtual.atEndOfMonth();

        YearMonth mesAnterior = mesAtual.minusMonths(1);
        LocalDate inicioMesAnterior = mesAnterior.atDay(1);
        LocalDate fimMesAnterior = mesAnterior.atEndOfMonth();

        BigDecimal receitasMes = sumProventos(inicioMesAtual, fimMesAtual);
        BigDecimal despesasMes = sumDespesas(inicioMesAtual, fimMesAtual);
        BigDecimal receitasMesAnterior = sumProventos(inicioMesAnterior, fimMesAnterior);
        BigDecimal despesasMesAnterior = sumDespesas(inicioMesAnterior, fimMesAnterior);
        BigDecimal saldoMesAnterior = receitasMesAnterior.subtract(despesasMesAnterior);

        long totalContas = contaRepository.count();
        long totalCategorias = categoriaRepository.count();

        BigDecimal reservaEmergenciaAtual = BigDecimal.ZERO;
        BigDecimal reservaEmergenciaObjetivo = BigDecimal.ZERO;
        BigDecimal reservaEmergenciaPercentual = BigDecimal.ZERO;
        Integer tempoRestante = 0;
        Long reservaId = null;

        Optional<ReservaEmergencia> reservaOpt = reservaEmergenciaRepository.findAll().stream().findFirst();
        if (reservaOpt.isPresent()) {
            ReservaEmergencia reserva = reservaOpt.get();
            reservaId = reserva.getId();
            reservaEmergenciaAtual = reserva.getSaldoAtual();
            reservaEmergenciaObjetivo = reserva.getObjetivo();
            reservaEmergenciaPercentual = reserva.getPercentualConcluido();
            if (reserva.getDataPrevisaoCompletar() != null) {
                tempoRestante = (int) ChronoUnit.MONTHS.between(LocalDate.now(), reserva.getDataPrevisaoCompletar());
                if (tempoRestante < 0) {
                    tempoRestante = 0;
                }
            }
        }

        DashboardSummaryDTO dto = new DashboardSummaryDTO(
                saldoTotal,
                receitasMes,
                despesasMes,
                totalContas,
                totalCategorias,
                saldoMesAnterior,
                receitasMesAnterior,
                despesasMesAnterior,
                reservaEmergenciaAtual,
                reservaEmergenciaObjetivo,
                reservaEmergenciaPercentual,
                tempoRestante
        );

        if (reservaOpt.isPresent() && dto.getReservaEmergencia() != null) {
            dto.getReservaEmergencia().setId(reservaId);
        }

        dto.setParcelasResumo(calcularResumoParcelasMes(inicioMesAtual, fimMesAtual));
        return dto;
    }

    public BudgetRuleDTO getBudgetRule() {
        YearMonth mesAtual = YearMonth.now();
        LocalDate inicioMesAtual = mesAtual.atDay(1);
        LocalDate fimMesAtual = mesAtual.atEndOfMonth();

        BigDecimal receitaTotal = sumProventos(inicioMesAtual, fimMesAtual);
        BigDecimal necessidadesIdeal = receitaTotal.multiply(PERCENTUAL_NECESSIDADES).setScale(2, RoundingMode.HALF_UP);
        BigDecimal desejosIdeal = receitaTotal.multiply(PERCENTUAL_DESEJOS).setScale(2, RoundingMode.HALF_UP);
        BigDecimal economiaIdeal = receitaTotal.multiply(PERCENTUAL_ECONOMIA).setScale(2, RoundingMode.HALF_UP);

        Map<Categoria.TipoCategoria, BigDecimal> gastosPorTipo = new EnumMap<>(Categoria.TipoCategoria.class);
        gastosPorTipo.put(Categoria.TipoCategoria.NECESSIDADE, BigDecimal.ZERO);
        gastosPorTipo.put(Categoria.TipoCategoria.DESEJO, BigDecimal.ZERO);
        gastosPorTipo.put(Categoria.TipoCategoria.ECONOMIA, BigDecimal.ZERO);

        for (ContaFixa conta : contaFixaRepository.findByVencimentoBetween(inicioMesAtual, fimMesAtual)) {
            if (conta.getCategoria() != null) {
                Categoria.TipoCategoria tipo = conta.getCategoria().getTipo();
                gastosPorTipo.put(tipo, gastosPorTipo.get(tipo).add(conta.getValor()));
            }
        }

        for (CompraDebito compra : compraDebitoRepository.findByDataCompraBetween(inicioMesAtual, fimMesAtual)) {
            if (compra.getCategoria() != null) {
                Categoria.TipoCategoria tipo = compra.getCategoria().getTipo();
                gastosPorTipo.put(tipo, gastosPorTipo.get(tipo).add(compra.getValor()));
            }
        }

        for (Fatura fatura : faturaRepository.findByDataVencimentoBetween(inicioMesAtual, fimMesAtual)) {
            for (Compra compra : fatura.getCompras()) {
                if (compra.getCategoria() != null) {
                    Categoria.TipoCategoria tipo = compra.getCategoria().getTipo();
                    gastosPorTipo.put(tipo, gastosPorTipo.get(tipo).add(compra.getValor()));
                }
            }
        }

        BigDecimal necessidadesReal = gastosPorTipo.get(Categoria.TipoCategoria.NECESSIDADE);
        BigDecimal desejosReal = gastosPorTipo.get(Categoria.TipoCategoria.DESEJO);
        BigDecimal economiaReal = gastosPorTipo.get(Categoria.TipoCategoria.ECONOMIA);
        BigDecimal totalGastos = necessidadesReal.add(desejosReal).add(economiaReal);

        double necessidadesPercentual = calcularPercentual(necessidadesReal, totalGastos);
        double desejosPercentual = calcularPercentual(desejosReal, totalGastos);
        double economiaPercentual = calcularPercentual(economiaReal, totalGastos);

        BigDecimal necessidadesDiferenca = necessidadesIdeal.subtract(necessidadesReal);
        BigDecimal desejosDiferenca = desejosIdeal.subtract(desejosReal);
        BigDecimal economiaDiferenca = economiaIdeal.subtract(economiaReal);

        return new BudgetRuleDTO(
                necessidadesIdeal,
                desejosIdeal,
                economiaIdeal,
                necessidadesReal,
                desejosReal,
                economiaReal,
                necessidadesPercentual,
                desejosPercentual,
                economiaPercentual,
                necessidadesDiferenca,
                desejosDiferenca,
                economiaDiferenca,
                getStatus(necessidadesReal, necessidadesIdeal),
                getStatus(desejosReal, desejosIdeal),
                getStatusEconomia(economiaReal, economiaIdeal)
        );
    }

    private DashboardSummaryDTO.ParcelasResumoDTO calcularResumoParcelasMes(LocalDate inicioMes, LocalDate fimMes) {
        List<Parcela> parcelasMes = parcelaRepository.findByDataVencimentoBetweenAtivas(inicioMes, fimMes);
        long totalParcelasAtivas = parcelaRepository.findParcelasNaoPagasAtivas().size();
        long parcelasPagasMes = parcelasMes.stream().filter(Parcela::isPaga).count();
        long parcelasNaoPagasMes = parcelasMes.stream().filter(p -> !p.isPaga()).count();
        BigDecimal valorTotalParcelasMes = parcelasMes.stream()
                .map(Parcela::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal valorPagoMes = parcelasMes.stream()
                .filter(Parcela::isPaga)
                .map(Parcela::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal valorRestanteMes = valorTotalParcelasMes.subtract(valorPagoMes);

        return new DashboardSummaryDTO.ParcelasResumoDTO(
                totalParcelasAtivas,
                parcelasPagasMes,
                parcelasNaoPagasMes,
                valorTotalParcelasMes,
                valorPagoMes,
                valorRestanteMes
        );
    }

    private BigDecimal sumProventos(LocalDate inicio, LocalDate fim) {
        return proventoRepository.findByDataBetween(inicio, fim).stream()
                .map(Provento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumDespesas(LocalDate inicio, LocalDate fim) {
        BigDecimal despesasFixas = contaFixaRepository.findByVencimentoBetween(inicio, fim).stream()
                .map(ContaFixa::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal despesasFaturas = faturaRepository.findByDataVencimentoBetween(inicio, fim).stream()
                .map(Fatura::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal despesasComprasDebito = compraDebitoRepository.findByDataCompraBetween(inicio, fim).stream()
                .map(CompraDebito::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return despesasFixas.add(despesasFaturas).add(despesasComprasDebito);
    }

    private double calcularPercentual(BigDecimal valor, BigDecimal total) {
        return total.compareTo(BigDecimal.ZERO) > 0
                ? valor.divide(total, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue()
                : 0.0;
    }

    private String getStatus(BigDecimal valorReal, BigDecimal valorIdeal) {
        if (valorReal.compareTo(valorIdeal) <= 0) {
            return "DENTRO_DO_LIMITE";
        }
        BigDecimal excesso = valorReal.subtract(valorIdeal);
        BigDecimal percentualExcesso = excesso.divide(valorIdeal, 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        if (percentualExcesso.compareTo(new BigDecimal("10")) <= 0) {
            return "POUCO_ACIMA";
        } else if (percentualExcesso.compareTo(new BigDecimal("30")) <= 0) {
            return "ACIMA";
        }
        return "MUITO_ACIMA";
    }

    private String getStatusEconomia(BigDecimal valorReal, BigDecimal valorIdeal) {
        if (valorReal.compareTo(valorIdeal) >= 0) {
            return "ACIMA_DO_OBJETIVO";
        }
        BigDecimal deficit = valorIdeal.subtract(valorReal);
        BigDecimal percentualDeficit = deficit.divide(valorIdeal, 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        if (percentualDeficit.compareTo(new BigDecimal("10")) <= 0) {
            return "QUASE_NO_OBJETIVO";
        } else if (percentualDeficit.compareTo(new BigDecimal("30")) <= 0) {
            return "ABAIXO";
        }
        return "MUITO_ABAIXO";
    }
}
