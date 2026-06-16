package br.com.salvemais.application.services;

import br.com.salvemais.domain.entities.Categoria;
import br.com.salvemais.domain.entities.CompraDebito;
import br.com.salvemais.domain.entities.Conta;
import br.com.salvemais.domain.entities.ContaFixa;
import br.com.salvemais.domain.entities.Fatura;
import br.com.salvemais.domain.entities.Provento;
import br.com.salvemais.domain.entities.ReservaEmergencia;
import br.com.salvemais.domain.enums.TipoConta;
import br.com.salvemais.infrastructure.repositories.CompraDebitoRepository;
import br.com.salvemais.infrastructure.repositories.ContaFixaRepository;
import br.com.salvemais.infrastructure.repositories.ContaRepository;
import br.com.salvemais.infrastructure.repositories.FaturaRepository;
import br.com.salvemais.infrastructure.repositories.ProventoRepository;
import br.com.salvemais.infrastructure.repositories.ReservaEmergenciaRepository;
import br.com.salvemais.web.api.dto.MonthlyExpenseDTO;
import br.com.salvemais.web.api.dto.VariationDataDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class DashboardTrendService {

    private final ContaRepository contaRepository;
    private final ProventoRepository proventoRepository;
    private final ContaFixaRepository contaFixaRepository;
    private final FaturaRepository faturaRepository;
    private final ReservaEmergenciaRepository reservaEmergenciaRepository;
    private final CompraDebitoRepository compraDebitoRepository;

    public DashboardTrendService(ContaRepository contaRepository,
                                 ProventoRepository proventoRepository,
                                 ContaFixaRepository contaFixaRepository,
                                 FaturaRepository faturaRepository,
                                 ReservaEmergenciaRepository reservaEmergenciaRepository,
                                 CompraDebitoRepository compraDebitoRepository) {
        this.contaRepository = contaRepository;
        this.proventoRepository = proventoRepository;
        this.contaFixaRepository = contaFixaRepository;
        this.faturaRepository = faturaRepository;
        this.reservaEmergenciaRepository = reservaEmergenciaRepository;
        this.compraDebitoRepository = compraDebitoRepository;
    }

    public List<MonthlyExpenseDTO> getMonthlyExpenseTrend(int meses) {
        List<MonthlyExpenseDTO> result = new ArrayList<>();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM", Locale.getDefault());

        YearMonth mesAtual = YearMonth.now();

        for (int i = meses - 1; i >= 0; i--) {
            YearMonth targetMonth = mesAtual.minusMonths(i);
            LocalDate inicioMes = targetMonth.atDay(1);
            LocalDate fimMes = targetMonth.atEndOfMonth();

            BigDecimal receitas = sumProventos(inicioMes, fimMes);
            BigDecimal despesasTotal = sumDespesas(inicioMes, fimMes);

            result.add(new MonthlyExpenseDTO(
                    inicioMes.format(monthFormatter),
                    inicioMes,
                    despesasTotal,
                    receitas
            ));
        }

        return result;
    }

    public List<MonthlyExpenseDTO> getMonthlyExpenseTrendByYear(int year) {
        List<MonthlyExpenseDTO> result = new ArrayList<>();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");

        for (int month = 1; month <= 12; month++) {
            YearMonth targetMonth = YearMonth.of(year, month);
            LocalDate inicioMes = targetMonth.atDay(1);
            LocalDate fimMes = targetMonth.atEndOfMonth();

            BigDecimal receitas = sumProventos(inicioMes, fimMes);
            BigDecimal despesasTotal = sumDespesas(inicioMes, fimMes);

            result.add(new MonthlyExpenseDTO(
                    inicioMes.format(monthFormatter),
                    inicioMes,
                    despesasTotal,
                    receitas
            ));
        }

        return result;
    }

    public List<VariationDataDTO> getVariationData(Integer mes, Integer ano) {
        List<VariationDataDTO> variations = new ArrayList<>();

        YearMonth mesAtual;
        if (mes != null && ano != null) {
            mesAtual = YearMonth.of(ano, mes);
        } else {
            mesAtual = YearMonth.now();
        }
        YearMonth mesAnterior = mesAtual.minusMonths(1);

        LocalDate inicioMesAtual = mesAtual.atDay(1);
        LocalDate fimMesAtual = mesAtual.atEndOfMonth();
        LocalDate inicioMesAnterior = mesAnterior.atDay(1);
        LocalDate fimMesAnterior = mesAnterior.atEndOfMonth();

        BigDecimal saldoTotalAtual = contaRepository.findAll().stream()
                .filter(conta -> !TipoConta.RESERVA_EMERGENCIA.equals(conta.getTipo()))
                .map(Conta::getSaldo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal receitasAtual = sumProventos(inicioMesAtual, fimMesAtual);
        BigDecimal despesasAtual = sumDespesas(inicioMesAtual, fimMesAtual);

        BigDecimal receitasAnterior = sumProventos(inicioMesAnterior, fimMesAnterior);
        BigDecimal despesasAnterior = sumDespesas(inicioMesAnterior, fimMesAnterior);
        BigDecimal saldoAnterior = receitasAnterior.subtract(despesasAnterior);

        variations.add(createVariation("Saldo Total", saldoTotalAtual, saldoAnterior, "account_balance_wallet"));
        variations.add(createVariation("Receitas", receitasAtual, receitasAnterior, "trending_up"));
        variations.add(createVariation("Despesas", despesasAtual, despesasAnterior, "trending_down"));

        BigDecimal resultadoAtual = receitasAtual.subtract(despesasAtual);
        BigDecimal resultadoAnterior = receitasAnterior.subtract(despesasAnterior);
        variations.add(createVariation("Resultado Mensal", resultadoAtual, resultadoAnterior, "assessment"));

        Optional<ReservaEmergencia> reservaOpt = reservaEmergenciaRepository.findAll().stream().findFirst();
        if (reservaOpt.isPresent()) {
            ReservaEmergencia reserva = reservaOpt.get();
            BigDecimal valorAnterior = reserva.getSaldoAtual();

            variations.add(createVariation(
                    "Reserva de Emergência",
                    reserva.getSaldoAtual(),
                    valorAnterior,
                    "savings"
            ));

            variations.add(createVariation(
                    "Progresso da Reserva",
                    reserva.getPercentualConcluido(),
                    BigDecimal.ZERO,
                    "trending_up"
            ));
        }

        return variations;
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

    private VariationDataDTO createVariation(String metric, BigDecimal currentValue, BigDecimal previousValue, String icon) {
        BigDecimal variation = currentValue.subtract(previousValue);
        BigDecimal variationPercent = BigDecimal.ZERO;

        if (previousValue.compareTo(BigDecimal.ZERO) != 0) {
            variationPercent = variation.divide(previousValue.abs(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        String trend;
        if (variation.compareTo(BigDecimal.ZERO) > 0) {
            trend = "up";
        } else if (variation.compareTo(BigDecimal.ZERO) < 0) {
            trend = "down";
        } else {
            trend = "neutral";
        }

        if ("Despesas".equals(metric)) {
            if (variation.compareTo(BigDecimal.ZERO) > 0) {
                trend = "down";
            } else if (variation.compareTo(BigDecimal.ZERO) < 0) {
                trend = "up";
            }
        }

        return new VariationDataDTO(
                metric,
                currentValue,
                previousValue,
                variation,
                variationPercent,
                trend,
                icon
        );
    }
}
