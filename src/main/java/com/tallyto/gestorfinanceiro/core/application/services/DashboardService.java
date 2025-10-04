package com.tallyto.gestorfinanceiro.core.application.services;

import com.tallyto.gestorfinanceiro.api.dto.BudgetRuleDTO;
import com.tallyto.gestorfinanceiro.api.dto.CategoryExpenseDTO;
import com.tallyto.gestorfinanceiro.api.dto.DashboardSummaryDTO;
import com.tallyto.gestorfinanceiro.api.dto.MonthlyExpenseDTO;
import com.tallyto.gestorfinanceiro.api.dto.VariationDataDTO;
import com.tallyto.gestorfinanceiro.core.domain.entities.*;
import com.tallyto.gestorfinanceiro.core.infra.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private static final BigDecimal PERCENTUAL_NECESSIDADES = new BigDecimal("0.5"); // 50%
    private static final BigDecimal PERCENTUAL_DESEJOS = new BigDecimal("0.3"); // 30%
    private static final BigDecimal PERCENTUAL_ECONOMIA = new BigDecimal("0.2"); // 20%

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private ProventoRepository proventoRepository;

    @Autowired
    private ContaFixaRepository contaFixaRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private FaturaRepository faturaRepository;
    
    @Autowired
    private ReservaEmergenciaRepository reservaEmergenciaRepository;

    @Autowired
    private ParcelaRepository parcelaRepository;

    /**
     * Obtém o resumo financeiro para o dashboard
     * @return DashboardSummaryDTO com os dados de resumo
     */
    public DashboardSummaryDTO getSummary() {
        // Obtém saldo total das contas
        BigDecimal saldoTotal = contaRepository.findAll().stream()
                .map(Conta::getSaldo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Obtém o mês atual
        YearMonth mesAtual = YearMonth.now();
        LocalDate inicioMesAtual = mesAtual.atDay(1);
        LocalDate fimMesAtual = mesAtual.atEndOfMonth();

        // Obtém o mês anterior
        YearMonth mesAnterior = mesAtual.minusMonths(1);
        LocalDate inicioMesAnterior = mesAnterior.atDay(1);
        LocalDate fimMesAnterior = mesAnterior.atEndOfMonth();

        // Obtém receitas do mês atual
        BigDecimal receitasMes = proventoRepository.findByDataBetween(inicioMesAtual, fimMesAtual).stream()
                .map(Provento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Obtém despesas do mês atual (contas fixas do período)
        BigDecimal despesasFixasMes = contaFixaRepository.findByVencimentoBetween(inicioMesAtual, fimMesAtual).stream()
                .map(ContaFixa::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Obtém despesas do mês atual (faturas)
        BigDecimal despesasFaturasMes = faturaRepository.findByDataVencimentoBetween(inicioMesAtual, fimMesAtual).stream()
                .map(Fatura::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Total de despesas (contas fixas + faturas)
        BigDecimal despesasMes = despesasFixasMes.add(despesasFaturasMes);

        // Calcula o saldo do mês anterior
        BigDecimal receitasMesAnterior = proventoRepository.findByDataBetween(inicioMesAnterior, fimMesAnterior).stream()
                .map(Provento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal despesasFixasMesAnterior = contaFixaRepository.findByVencimentoBetween(inicioMesAnterior, fimMesAnterior).stream()
                .map(ContaFixa::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal despesasFaturasMesAnterior = faturaRepository.findByDataVencimentoBetween(inicioMesAnterior, fimMesAnterior).stream()
                .map(Fatura::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal despesasMesAnterior = despesasFixasMesAnterior.add(despesasFaturasMesAnterior);
        BigDecimal saldoMesAnterior = receitasMesAnterior.subtract(despesasMesAnterior);

        // Conta o número de contas e categorias
        long totalContas = contaRepository.count();
        long totalCategorias = categoriaRepository.count();
        
        // Dados da reserva de emergência
        BigDecimal reservaEmergenciaAtual = BigDecimal.ZERO;
        BigDecimal reservaEmergenciaObjetivo = BigDecimal.ZERO;
        BigDecimal reservaEmergenciaPercentual = BigDecimal.ZERO;
        Integer tempoRestante = 0;
        Long reservaId = null;
        
        // Busca a primeira reserva de emergência (assumindo que há apenas uma)
        Optional<ReservaEmergencia> reservaOpt = reservaEmergenciaRepository.findAll().stream().findFirst();
        
        if (reservaOpt.isPresent()) {
            ReservaEmergencia reserva = reservaOpt.get();
            reservaId = reserva.getId();
            reservaEmergenciaAtual = reserva.getSaldoAtual();
            reservaEmergenciaObjetivo = reserva.getObjetivo();
            reservaEmergenciaPercentual = reserva.getPercentualConcluido();
            
            // Calcula o tempo restante em meses
            if (reserva.getDataPrevisaoCompletar() != null) {
                tempoRestante = (int) ChronoUnit.MONTHS.between(
                        LocalDate.now(),
                        reserva.getDataPrevisaoCompletar()
                );
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
        
        // Definir o ID da reserva se existir
        if (reservaOpt.isPresent() && dto.getReservaEmergencia() != null) {
            dto.getReservaEmergencia().setId(reservaId);
        }
        
        // Calcular informações das parcelas
        dto.setParcelasResumo(calcularResumoParcelasMes(inicioMesAtual, fimMesAtual));
        
        return dto;
    }

    /**
     * Calcula o resumo das parcelas do mês
     */
    private DashboardSummaryDTO.ParcelasResumoDTO calcularResumoParcelasMes(LocalDate inicioMes, LocalDate fimMes) {
        // Busca todas as parcelas do mês
        List<Parcela> parcelasMes = parcelaRepository.findByDataVencimentoBetween(inicioMes, fimMes);
        
        // Total de parcelas ativas (compras parceladas em andamento)
        long totalParcelasAtivas = parcelaRepository.findByPaga(false).size();
        
        // Parcelas pagas e não pagas do mês
        long parcelasPagasMes = parcelasMes.stream().filter(Parcela::isPaga).count();
        long parcelasNaoPagasMes = parcelasMes.stream().filter(p -> !p.isPaga()).count();
        
        // Valor total das parcelas do mês
        BigDecimal valorTotalParcelasMes = parcelasMes.stream()
                .map(Parcela::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Valor pago no mês
        BigDecimal valorPagoMes = parcelasMes.stream()
                .filter(Parcela::isPaga)
                .map(Parcela::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Valor restante no mês
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

    /**
     * Obtém os dados de despesas por categoria para o gráfico de pizza
     * @return Lista de CategoryExpenseDTO com os dados por categoria
     */
    public List<CategoryExpenseDTO> getExpensesByCategory() {
        // Obtém o mês atual
        YearMonth mesAtual = YearMonth.now();
        LocalDate inicioMesAtual = mesAtual.atDay(1);
        LocalDate fimMesAtual = mesAtual.atEndOfMonth();

        // Agrupa despesas por categoria
        Map<Categoria, BigDecimal> gastosPorCategoria = new HashMap<>();

        // Adiciona contas fixas do período
        List<ContaFixa> contasFixas = contaFixaRepository.findByVencimentoBetween(inicioMesAtual, fimMesAtual);
        for (ContaFixa conta : contasFixas) {
            Categoria categoria = conta.getCategoria();
            if (categoria != null) {
                gastosPorCategoria.put(
                        categoria,
                        gastosPorCategoria.getOrDefault(categoria, BigDecimal.ZERO).add(conta.getValor())
                );
            }
        }

        // Adiciona faturas do período
        List<Fatura> faturas = faturaRepository.findByDataVencimentoBetween(inicioMesAtual, fimMesAtual);
        for (Fatura fatura : faturas) {
            // Para faturas, vamos usar a categoria do cartão ou uma categoria padrão "Cartões"
            // Como não temos categoria direta na fatura, vamos criar uma lógica para isso
            String nomeCategoria = "Cartões de Crédito";
            
            // Busca se já existe uma categoria "Cartões de Crédito"
            Categoria categoriaCartao = categoriaRepository.findByNome(nomeCategoria);
            
            if (categoriaCartao != null) {
                gastosPorCategoria.put(
                        categoriaCartao,
                        gastosPorCategoria.getOrDefault(categoriaCartao, BigDecimal.ZERO).add(fatura.getValorTotal())
                );
            } else {
                // Se não existe a categoria, vamos distribuir as faturas pelas categorias das compras dentro da fatura
                for (Compra compra : fatura.getCompras()) {
                    Categoria categoria = compra.getCategoria();
                    if (categoria != null) {
                        gastosPorCategoria.put(
                                categoria,
                                gastosPorCategoria.getOrDefault(categoria, BigDecimal.ZERO).add(compra.getValor())
                        );
                    }
                }
            }
        }

        // Calcula o total de gastos
        BigDecimal totalGastos = gastosPorCategoria.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Converte para DTOs com percentuais
        return gastosPorCategoria.entrySet().stream()
                .map(entry -> {
                    double percentual = 0.0;
                    if (totalGastos.compareTo(BigDecimal.ZERO) > 0) {
                        percentual = entry.getValue()
                                .divide(totalGastos, 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100))
                                .doubleValue();
                    }
                    
                    return new CategoryExpenseDTO(
                            entry.getKey().getId(),
                            entry.getKey().getNome(),
                            entry.getValue(),
                            percentual
                    );
                })
                .sorted(Comparator.comparing(CategoryExpenseDTO::valorTotal).reversed())
                .collect(Collectors.toList());
    }
    
    /**
     * Calcula a regra 50/30/20 para o orçamento mensal
     * @return DTO com os dados da regra 50/30/20
     */
    public BudgetRuleDTO getBudgetRule() {
        // Obtém o mês atual
        YearMonth mesAtual = YearMonth.now();
        LocalDate inicioMesAtual = mesAtual.atDay(1);
        LocalDate fimMesAtual = mesAtual.atEndOfMonth();

        // Obtém receita total do mês (proventos)
        BigDecimal receitaTotal = proventoRepository.findByDataBetween(inicioMesAtual, fimMesAtual).stream()
                .map(Provento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calcula os valores ideais com base na receita
        BigDecimal necessidadesIdeal = receitaTotal.multiply(PERCENTUAL_NECESSIDADES).setScale(2, RoundingMode.HALF_UP);
        BigDecimal desejosIdeal = receitaTotal.multiply(PERCENTUAL_DESEJOS).setScale(2, RoundingMode.HALF_UP);
        BigDecimal economiaIdeal = receitaTotal.multiply(PERCENTUAL_ECONOMIA).setScale(2, RoundingMode.HALF_UP);

        // Agora vamos calcular os gastos reais por tipo de categoria
        Map<Categoria.TipoCategoria, BigDecimal> gastosPorTipo = new EnumMap<>(Categoria.TipoCategoria.class);
        gastosPorTipo.put(Categoria.TipoCategoria.NECESSIDADE, BigDecimal.ZERO);
        gastosPorTipo.put(Categoria.TipoCategoria.DESEJO, BigDecimal.ZERO);
        gastosPorTipo.put(Categoria.TipoCategoria.ECONOMIA, BigDecimal.ZERO);

        // Adiciona contas fixas do período
        List<ContaFixa> contasFixas = contaFixaRepository.findByVencimentoBetween(inicioMesAtual, fimMesAtual);
        for (ContaFixa conta : contasFixas) {
            Categoria categoria = conta.getCategoria();
            if (categoria != null) {
                Categoria.TipoCategoria tipo = categoria.getTipo();
                gastosPorTipo.put(
                        tipo,
                        gastosPorTipo.get(tipo).add(conta.getValor())
                );
            }
        }

        // Adiciona faturas do período
        List<Fatura> faturas = faturaRepository.findByDataVencimentoBetween(inicioMesAtual, fimMesAtual);
        for (Fatura fatura : faturas) {
            // Para cada compra dentro da fatura
            for (Compra compra : fatura.getCompras()) {
                Categoria categoria = compra.getCategoria();
                if (categoria != null) {
                    Categoria.TipoCategoria tipo = categoria.getTipo();
                    gastosPorTipo.put(
                            tipo,
                            gastosPorTipo.get(tipo).add(compra.getValor())
                    );
                }
            }
        }

        // Valores reais gastos
        BigDecimal necessidadesReal = gastosPorTipo.get(Categoria.TipoCategoria.NECESSIDADE);
        BigDecimal desejosReal = gastosPorTipo.get(Categoria.TipoCategoria.DESEJO);
        BigDecimal economiaReal = gastosPorTipo.get(Categoria.TipoCategoria.ECONOMIA);

        // Total de gastos
        BigDecimal totalGastos = necessidadesReal.add(desejosReal).add(economiaReal);

        // Calcula percentuais reais (com base no total gasto)
        double necessidadesPercentual = totalGastos.compareTo(BigDecimal.ZERO) > 0 
                ? necessidadesReal.divide(totalGastos, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue()
                : 0.0;
        
        double desejosPercentual = totalGastos.compareTo(BigDecimal.ZERO) > 0 
                ? desejosReal.divide(totalGastos, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue()
                : 0.0;
        
        double economiaPercentual = totalGastos.compareTo(BigDecimal.ZERO) > 0 
                ? economiaReal.divide(totalGastos, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue()
                : 0.0;

        // Calcula diferenças entre ideal e real
        BigDecimal necessidadesDiferenca = necessidadesIdeal.subtract(necessidadesReal);
        BigDecimal desejosDiferenca = desejosIdeal.subtract(desejosReal);
        BigDecimal economiaDiferenca = economiaIdeal.subtract(economiaReal);

        // Define status com base nas diferenças
        String necessidadesStatus = getStatus(necessidadesReal, necessidadesIdeal);
        String desejosStatus = getStatus(desejosReal, desejosIdeal);
        String economiaStatus = getStatusEconomia(economiaReal, economiaIdeal);

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
                necessidadesStatus,
                desejosStatus,
                economiaStatus
        );
    }

    /**
     * Determina o status de gastos com base na comparação entre real e ideal
     * @param valorReal Valor real gasto
     * @param valorIdeal Valor ideal a ser gasto
     * @return Status descritivo
     */
    private String getStatus(BigDecimal valorReal, BigDecimal valorIdeal) {
        if (valorReal.compareTo(valorIdeal) <= 0) {
            return "DENTRO_DO_LIMITE";  // Gasto menor ou igual ao limite ideal
        } else {
            BigDecimal excesso = valorReal.subtract(valorIdeal);
            BigDecimal percentualExcesso = excesso.divide(valorIdeal, 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            
            if (percentualExcesso.compareTo(new BigDecimal("10")) <= 0) {
                return "POUCO_ACIMA";  // Excedeu em até 10%
            } else if (percentualExcesso.compareTo(new BigDecimal("30")) <= 0) {
                return "ACIMA";  // Excedeu entre 10% e 30%
            } else {
                return "MUITO_ACIMA";  // Excedeu em mais de 30%
            }
        }
    }
    
    /**
     * Determina o status de economia com base na comparação entre real e ideal
     * @param valorReal Valor real economizado
     * @param valorIdeal Valor ideal a ser economizado
     * @return Status descritivo
     */
    private String getStatusEconomia(BigDecimal valorReal, BigDecimal valorIdeal) {
        if (valorReal.compareTo(valorIdeal) >= 0) {
            return "ACIMA_DO_OBJETIVO";  // Economizou mais que o objetivo
        } else {
            BigDecimal deficit = valorIdeal.subtract(valorReal);
            BigDecimal percentualDeficit = deficit.divide(valorIdeal, 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            
            if (percentualDeficit.compareTo(new BigDecimal("10")) <= 0) {
                return "QUASE_NO_OBJETIVO";  // Faltou até 10% para atingir
            } else if (percentualDeficit.compareTo(new BigDecimal("30")) <= 0) {
                return "ABAIXO";  // Faltou entre 10% e 30% para atingir
            } else {
                return "MUITO_ABAIXO";  // Faltou mais de 30% para atingir
            }
        }
    }

    /**
     * Obtém dados de tendência de gastos e receitas por mês
     * @param meses Número de meses para trás para buscar (padrão: 6)
     * @return Lista de MonthlyExpenseDTO com os dados mensais
     */
    public List<MonthlyExpenseDTO> getMonthlyExpenseTrend(int meses) {
        List<MonthlyExpenseDTO> result = new ArrayList<>();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM", Locale.getDefault());
        
        // Mês atual
        YearMonth mesAtual = YearMonth.now();
        
        // Busca dados para cada mês
        for (int i = meses - 1; i >= 0; i--) {
            YearMonth targetMonth = mesAtual.minusMonths(i);
            LocalDate inicioMes = targetMonth.atDay(1);
            LocalDate fimMes = targetMonth.atEndOfMonth();
            
            // Busca receitas do mês
            BigDecimal receitas = proventoRepository.findByDataBetween(inicioMes, fimMes).stream()
                    .map(Provento::getValor)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Busca despesas do mês (contas fixas do período)
            BigDecimal despesasFixas = contaFixaRepository.findByVencimentoBetween(inicioMes, fimMes).stream()
                    .map(ContaFixa::getValor)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Busca despesas do mês (faturas)
            BigDecimal despesasFaturas = faturaRepository.findByDataVencimentoBetween(inicioMes, fimMes).stream()
                    .map(Fatura::getValorTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal despesasTotal = despesasFixas.add(despesasFaturas);
            
            // Cria DTO para o mês
            result.add(new MonthlyExpenseDTO(
                    inicioMes.format(monthFormatter),
                    inicioMes,
                    despesasTotal,
                    receitas
            ));
        }
        
        return result;
    }

    /**
     * Obtém tendência mensal por ano específico
     * @param year Ano para buscar os dados
     * @return Lista de MonthlyExpenseDTO com dados mensais do ano
     */
    public List<MonthlyExpenseDTO> getMonthlyExpenseTrendByYear(int year) {
        List<MonthlyExpenseDTO> result = new ArrayList<>();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        
        // Busca dados para cada mês do ano especificado
        for (int month = 1; month <= 12; month++) {
            YearMonth targetMonth = YearMonth.of(year, month);
            LocalDate inicioMes = targetMonth.atDay(1);
            LocalDate fimMes = targetMonth.atEndOfMonth();
            
            // Busca receitas do mês
            BigDecimal receitas = proventoRepository.findByDataBetween(inicioMes, fimMes).stream()
                    .map(Provento::getValor)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Busca despesas do mês (contas fixas do período)
            BigDecimal despesasFixas = contaFixaRepository.findByVencimentoBetween(inicioMes, fimMes).stream()
                    .map(ContaFixa::getValor)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Busca despesas do mês (faturas)
            BigDecimal despesasFaturas = faturaRepository.findByDataVencimentoBetween(inicioMes, fimMes).stream()
                    .map(Fatura::getValorTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal despesasTotal = despesasFixas.add(despesasFaturas);
            
            // Cria DTO para o mês
            result.add(new MonthlyExpenseDTO(
                    inicioMes.format(monthFormatter),
                    inicioMes,
                    despesasTotal,
                    receitas
            ));
        }
        
        return result;
    }

    /**
     * Obtém dados de variação mensal comparando com o período anterior
     * @return Lista de VariationDataDTO com as variações
     */
    public List<VariationDataDTO> getVariationData() {
        List<VariationDataDTO> variations = new ArrayList<>();
        
        // Obtém o mês atual e anterior
        YearMonth mesAtual = YearMonth.now();
        YearMonth mesAnterior = mesAtual.minusMonths(1);
        
        LocalDate inicioMesAtual = mesAtual.atDay(1);
        LocalDate fimMesAtual = mesAtual.atEndOfMonth();
        LocalDate inicioMesAnterior = mesAnterior.atDay(1);
        LocalDate fimMesAnterior = mesAnterior.atEndOfMonth();
        
        // Calcula dados do mês atual
        BigDecimal saldoTotalAtual = contaRepository.findAll().stream()
                .map(Conta::getSaldo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
        BigDecimal receitasAtual = proventoRepository.findByDataBetween(inicioMesAtual, fimMesAtual).stream()
                .map(Provento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
        BigDecimal despesasFixasAtual = contaFixaRepository.findByVencimentoBetween(inicioMesAtual, fimMesAtual).stream()
                .map(ContaFixa::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
        BigDecimal despesasFaturasAtual = faturaRepository.findByDataVencimentoBetween(inicioMesAtual, fimMesAtual).stream()
                .map(Fatura::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
        BigDecimal despesasAtual = despesasFixasAtual.add(despesasFaturasAtual);
        
        // Calcula dados do mês anterior
        BigDecimal receitasAnterior = proventoRepository.findByDataBetween(inicioMesAnterior, fimMesAnterior).stream()
                .map(Provento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
        BigDecimal despesasFixasAnterior = contaFixaRepository.findByVencimentoBetween(inicioMesAnterior, fimMesAnterior).stream()
                .map(ContaFixa::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
        BigDecimal despesasFaturasAnterior = faturaRepository.findByDataVencimentoBetween(inicioMesAnterior, fimMesAnterior).stream()
                .map(Fatura::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
        BigDecimal despesasAnterior = despesasFixasAnterior.add(despesasFaturasAnterior);
        BigDecimal saldoAnterior = receitasAnterior.subtract(despesasAnterior);
        
        // Cria variações
        
        // 1. Saldo Total
        variations.add(createVariation(
                "Saldo Total",
                saldoTotalAtual,
                saldoAnterior,
                "account_balance_wallet"
        ));
        
        // 2. Receitas
        variations.add(createVariation(
                "Receitas",
                receitasAtual,
                receitasAnterior,
                "trending_up"
        ));
        
        // 3. Despesas
        variations.add(createVariation(
                "Despesas",
                despesasAtual,
                despesasAnterior,
                "trending_down"
        ));
        
        // 4. Resultado Mensal
        BigDecimal resultadoAtual = receitasAtual.subtract(despesasAtual);
        BigDecimal resultadoAnterior = receitasAnterior.subtract(despesasAnterior);
        variations.add(createVariation(
                "Resultado Mensal",
                resultadoAtual,
                resultadoAnterior,
                "assessment"
        ));
        
        // 5. Reserva de Emergência
        // Busca dados da reserva de emergência
        Optional<ReservaEmergencia> reservaOpt = reservaEmergenciaRepository.findAll().stream().findFirst();
        if (reservaOpt.isPresent()) {
            ReservaEmergencia reserva = reservaOpt.get();
            
            // Assumimos que temos um registro do mês anterior para comparar
            // Caso não tenhamos, usamos o valor atual como anterior também (variação zero)
            BigDecimal valorAnterior = reserva.getSaldoAtual();
            // Em uma implementação real, você teria que buscar o histórico da reserva
            
            variations.add(createVariation(
                    "Reserva de Emergência",
                    reserva.getSaldoAtual(),
                    valorAnterior,
                    "savings"
            ));
            
            // Progresso da Reserva
            variations.add(createVariation(
                    "Progresso da Reserva",
                    reserva.getPercentualConcluido(),
                    BigDecimal.ZERO, // Aqui também precisaria de um histórico
                    "trending_up"
            ));
        }
        
        return variations;
    }
    
    /**
     * Método auxiliar para criar dados de variação
     */
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
        
        // Para despesas, a lógica de tendência é inversa (menos despesa = boa tendência)
        if ("Despesas".equals(metric)) {
            if (variation.compareTo(BigDecimal.ZERO) > 0) {
                trend = "down"; // Mais despesa = tendência ruim
            } else if (variation.compareTo(BigDecimal.ZERO) < 0) {
                trend = "up"; // Menos despesa = tendência boa
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
