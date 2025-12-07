package com.tallyto.gestorfinanceiro.core.application.services;

import com.tallyto.gestorfinanceiro.api.dto.ComparativoMensalDTO;
import com.tallyto.gestorfinanceiro.api.dto.RelatorioMensalDTO;
import com.tallyto.gestorfinanceiro.core.domain.entities.*;
import com.tallyto.gestorfinanceiro.core.infra.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RelatorioMensalService {

    @Autowired
    private ProventoRepository proventoRepository;

    @Autowired
    private ContaFixaRepository contaFixaRepository;

    @Autowired
    private FaturaRepository faturaRepository;

    @Autowired
    private CompraDebitoRepository compraDebitoRepository;

    /**
     * Gera um relatório mensal completo baseado no mês/ano fornecidos
     * @param ano Ano do relatório
     * @param mes Mês do relatório (1-12)
     * @return RelatorioMensalDTO com todos os dados do mês
     */
    public RelatorioMensalDTO gerarRelatorioMensal(int ano, int mes) {
        YearMonth yearMonth = YearMonth.of(ano, mes);
        LocalDate inicioMes = yearMonth.atDay(1);
        LocalDate fimMes = yearMonth.atEndOfMonth();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault());
        String mesReferencia = yearMonth.format(formatter);

        // 1. Buscar proventos do mês
        List<Provento> proventos = proventoRepository.findByDataBetween(inicioMes, fimMes);
        List<RelatorioMensalDTO.ItemProventoDTO> proventosDTO = proventos.stream()
                .map(provento -> new RelatorioMensalDTO.ItemProventoDTO(
                        provento.getId(),
                        provento.getDescricao(),
                        provento.getValor(),
                        provento.getData(),
                        provento.getConta() != null ? provento.getConta().getTitular() : "Conta não informada"
                ))
                .collect(Collectors.toList());

        // 2. Buscar contas fixas que vencem no período especificado
        List<ContaFixa> contasFixas = contaFixaRepository.findByVencimentoBetween(inicioMes, fimMes);
        List<RelatorioMensalDTO.ItemGastoFixoDTO> gastosFixosDTO = contasFixas.stream()
                .map(conta -> new RelatorioMensalDTO.ItemGastoFixoDTO(
                        conta.getId(),
                        conta.getNome(),
                        conta.getValor(),
                        conta.getVencimento(),
                        conta.getCategoria() != null ? conta.getCategoria().getNome() : "Categoria não informada",
                        conta.isPago()
                ))
                .collect(Collectors.toList());

        // 3. Buscar faturas do mês (baseado na data de vencimento)
        List<Fatura> faturas = faturaRepository.findByDataVencimentoBetween(inicioMes, fimMes);
        
        // 4. Criar lista de cartões com suas faturas
        List<RelatorioMensalDTO.ItemCartaoDTO> cartoesDTO = faturas.stream()
                .map(fatura -> {
                    List<RelatorioMensalDTO.CompraCartaoDTO> comprasDTO = fatura.getCompras().stream()
                            .map(compra -> new RelatorioMensalDTO.CompraCartaoDTO(
                                    compra.getId(),
                                    compra.getDescricao(),
                                    compra.getValor(),
                                    compra.getData(),
                                    compra.getCategoria() != null ? compra.getCategoria().getNome() : "Categoria não informada"
                            ))
                            .collect(Collectors.toList());
                    
                    return new RelatorioMensalDTO.ItemCartaoDTO(
                            fatura.getCartaoCredito().getId(),
                            fatura.getCartaoCredito().getNome(),
                            fatura.getValorTotal(),
                            fatura.getDataVencimento(),
                            comprasDTO
                    );
                })
                .collect(Collectors.toList());

        // 5. Buscar compras em débito do mês
        List<CompraDebito> comprasDebito = compraDebitoRepository.findByDataCompraBetween(inicioMes, fimMes);
        List<RelatorioMensalDTO.ItemCompraDebitoDTO> comprasDebitoDTO = comprasDebito.stream()
                .map(compra -> new RelatorioMensalDTO.ItemCompraDebitoDTO(
                        compra.getId(),
                        compra.getNome(),
                        compra.getValor(),
                        compra.getDataCompra(),
                        compra.getCategoria() != null ? compra.getCategoria().getNome() : "Categoria não informada",
                        compra.getConta() != null ? compra.getConta().getTitular() : "Conta não informada"
                ))
                .collect(Collectors.toList());

        // 6. Calcular totais
        BigDecimal totalProventos = proventosDTO.stream()
                .map(RelatorioMensalDTO.ItemProventoDTO::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCartoes = cartoesDTO.stream()
                .map(RelatorioMensalDTO.ItemCartaoDTO::valorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalGastosFixos = gastosFixosDTO.stream()
                .map(RelatorioMensalDTO.ItemGastoFixoDTO::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalComprasDebito = comprasDebitoDTO.stream()
                .map(RelatorioMensalDTO.ItemCompraDebitoDTO::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 7. Calcular saldo final e dívidas (sem outras despesas)
        BigDecimal totalDespesas = totalCartoes.add(totalGastosFixos).add(totalComprasDebito);
        BigDecimal saldoFinal = totalProventos.subtract(totalDespesas);

        // 8. Criar resumo financeiro
        RelatorioMensalDTO.ResumoFinanceiroDTO resumoFinanceiro = new RelatorioMensalDTO.ResumoFinanceiroDTO(
                totalProventos,
                BigDecimal.ZERO, // Receitas pendentes - pode ser implementado futuramente
                totalCartoes,
                totalGastosFixos,
                totalComprasDebito,
                BigDecimal.ZERO, // Outras despesas removidas
                saldoFinal,
                totalDespesas
        );

        // 9. Criar lista vazia para receitas pendentes (funcionalidade futura)
        List<RelatorioMensalDTO.ItemReceitasPendentesDTO> receitasPendentes = List.of();

        // 10. Criar lista vazia para outras despesas (removida esta funcionalidade)
        List<RelatorioMensalDTO.ItemOutrasDescricaoDTO> outrasDespesasDTO = List.of();

        return new RelatorioMensalDTO(
                mesReferencia,
                inicioMes,
                resumoFinanceiro,
                proventosDTO,
                receitasPendentes,
                cartoesDTO,
                gastosFixosDTO,
                comprasDebitoDTO,
                outrasDespesasDTO,
                saldoFinal,
                totalDespesas
        );
    }

    /**
     * Gera relatório para o mês atual
     * @return RelatorioMensalDTO para o mês atual
     */
    public RelatorioMensalDTO gerarRelatorioMensalAtual() {
        YearMonth atual = YearMonth.now();
        return gerarRelatorioMensal(atual.getYear(), atual.getMonthValue());
    }

    /**
     * Calcula as contas fixas vencidas e não pagas
     * @param dataReferencia Data de referência para verificar vencimentos
     * @return Lista de contas fixas vencidas
     */
    public List<RelatorioMensalDTO.ItemGastoFixoDTO> obterContasFixasVencidas(LocalDate dataReferencia) {
        List<ContaFixa> contasVencidas = contaFixaRepository.findByVencimentoBeforeAndPagoIsFalse(dataReferencia);
        
        return contasVencidas.stream()
                .map(conta -> new RelatorioMensalDTO.ItemGastoFixoDTO(
                        conta.getId(),
                        conta.getNome(),
                        conta.getValor(),
                        conta.getVencimento(),
                        conta.getCategoria() != null ? conta.getCategoria().getNome() : "Categoria não informada",
                        conta.isPago()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Gera comparativo entre dois meses
     * @param anoAnterior Ano do mês anterior
     * @param mesAnterior Mês anterior (1-12)
     * @param anoAtual Ano do mês atual
     * @param mesAtual Mês atual (1-12)
     * @return ComparativoMensalDTO com análise comparativa
     */
    public ComparativoMensalDTO gerarComparativoMensal(int anoAnterior, int mesAnterior, int anoAtual, int mesAtual) {
        // Gerar relatórios dos dois meses
        RelatorioMensalDTO relatorioAnterior = gerarRelatorioMensal(anoAnterior, mesAnterior);
        RelatorioMensalDTO relatorioAtual = gerarRelatorioMensal(anoAtual, mesAtual);
        
        // Criar resumo comparativo
        ComparativoMensalDTO.ResumoComparativoDTO resumo = criarResumoComparativo(
            relatorioAnterior, 
            relatorioAtual
        );
        
        // Comparar categorias
        List<ComparativoMensalDTO.ComparativoCategoriaDTO> categorias = compararCategorias(
            relatorioAnterior, 
            relatorioAtual
        );
        
        // Identificar maiores variações
        List<ComparativoMensalDTO.DestaqueMudancaDTO> maioresVariacoes = identificarMaioresVariacoes(categorias);
        
        return new ComparativoMensalDTO(
            relatorioAnterior.mesReferencia(),
            relatorioAtual.mesReferencia(),
            resumo,
            categorias,
            maioresVariacoes
        );
    }

    private ComparativoMensalDTO.ResumoComparativoDTO criarResumoComparativo(
            RelatorioMensalDTO anterior, 
            RelatorioMensalDTO atual) {
        
        BigDecimal proventosAnterior = anterior.resumoFinanceiro().totalProventos();
        BigDecimal proventosAtual = atual.resumoFinanceiro().totalProventos();
        BigDecimal variacaoProventos = proventosAtual.subtract(proventosAnterior);
        BigDecimal percentualProventos = calcularPercentual(variacaoProventos, proventosAnterior);
        
        BigDecimal despesasAnterior = anterior.resumoFinanceiro().totalDividas();
        BigDecimal despesasAtual = atual.resumoFinanceiro().totalDividas();
        BigDecimal variacaoDespesas = despesasAtual.subtract(despesasAnterior);
        BigDecimal percentualDespesas = calcularPercentual(variacaoDespesas, despesasAnterior);
        
        BigDecimal saldoAnterior = anterior.saldoFinal();
        BigDecimal saldoAtual = atual.saldoFinal();
        BigDecimal variacaoSaldo = saldoAtual.subtract(saldoAnterior);
        BigDecimal percentualSaldo = calcularPercentual(variacaoSaldo, saldoAnterior);
        
        String statusGeral = determinarStatusGeral(variacaoSaldo, variacaoDespesas);
        
        return new ComparativoMensalDTO.ResumoComparativoDTO(
            proventosAnterior, proventosAtual, variacaoProventos, percentualProventos,
            despesasAnterior, despesasAtual, variacaoDespesas, percentualDespesas,
            saldoAnterior, saldoAtual, variacaoSaldo, percentualSaldo,
            statusGeral
        );
    }

    private List<ComparativoMensalDTO.ComparativoCategoriaDTO> compararCategorias(
            RelatorioMensalDTO anterior, 
            RelatorioMensalDTO atual) {
        
        List<ComparativoMensalDTO.ComparativoCategoriaDTO> comparativos = new ArrayList<>();
        
        // Mapear categorias de despesas do mês anterior
        Map<String, BigDecimal> categoriasAnterior = new HashMap<>();
        anterior.gastosFixos().forEach(g -> 
            categoriasAnterior.merge(g.categoria(), g.valor(), BigDecimal::add));
        anterior.comprasDebito().forEach(c -> 
            categoriasAnterior.merge(c.categoria(), c.valor(), BigDecimal::add));
        anterior.cartoes().forEach(cartao -> 
            cartao.compras().forEach(c -> 
                categoriasAnterior.merge(c.categoria(), c.valor(), BigDecimal::add)));
        
        // Mapear categorias de despesas do mês atual
        Map<String, BigDecimal> categoriasAtual = new HashMap<>();
        atual.gastosFixos().forEach(g -> 
            categoriasAtual.merge(g.categoria(), g.valor(), BigDecimal::add));
        atual.comprasDebito().forEach(c -> 
            categoriasAtual.merge(c.categoria(), c.valor(), BigDecimal::add));
        atual.cartoes().forEach(cartao -> 
            cartao.compras().forEach(c -> 
                categoriasAtual.merge(c.categoria(), c.valor(), BigDecimal::add)));
        
        // Combinar todas as categorias únicas
        Set<String> todasCategorias = new HashSet<>();
        todasCategorias.addAll(categoriasAnterior.keySet());
        todasCategorias.addAll(categoriasAtual.keySet());
        
        // Criar comparativos para cada categoria
        for (String categoria : todasCategorias) {
            BigDecimal valorAnterior = categoriasAnterior.getOrDefault(categoria, BigDecimal.ZERO);
            BigDecimal valorAtual = categoriasAtual.getOrDefault(categoria, BigDecimal.ZERO);
            BigDecimal variacao = valorAtual.subtract(valorAnterior);
            BigDecimal percentual = calcularPercentual(variacao, valorAnterior);
            String tendencia = determinarTendencia(variacao);
            
            comparativos.add(new ComparativoMensalDTO.ComparativoCategoriaDTO(
                categoria,
                "DESPESA",
                valorAnterior,
                valorAtual,
                variacao,
                percentual,
                tendencia
            ));
        }
        
        return comparativos;
    }

    private List<ComparativoMensalDTO.DestaqueMudancaDTO> identificarMaioresVariacoes(
            List<ComparativoMensalDTO.ComparativoCategoriaDTO> categorias) {
        
        return categorias.stream()
            .filter(c -> c.variacao().abs().compareTo(BigDecimal.TEN) > 0) // Apenas variações > R$ 10
            .sorted((c1, c2) -> c2.variacao().abs().compareTo(c1.variacao().abs())) // Ordenar por maior variação
            .limit(5) // Top 5
            .map(c -> new ComparativoMensalDTO.DestaqueMudancaDTO(
                c.categoria(),
                c.tipo(),
                c.variacao(),
                c.percentual(),
                determinarImpacto(c.variacao())
            ))
            .collect(Collectors.toList());
    }

    private BigDecimal calcularPercentual(BigDecimal variacao, BigDecimal valorBase) {
        if (valorBase.compareTo(BigDecimal.ZERO) == 0) {
            return variacao.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : new BigDecimal("100");
        }
        return variacao.multiply(new BigDecimal("100"))
            .divide(valorBase, 2, RoundingMode.HALF_UP);
    }

    private String determinarStatusGeral(BigDecimal variacaoSaldo, BigDecimal variacaoDespesas) {
        if (variacaoSaldo.compareTo(BigDecimal.ZERO) > 0) {
            return "MELHOROU";
        } else if (variacaoSaldo.compareTo(BigDecimal.ZERO) < 0 && 
                   variacaoDespesas.compareTo(BigDecimal.ZERO) > 0) {
            return "PIOROU";
        }
        return "ESTAVEL";
    }

    private String determinarTendencia(BigDecimal variacao) {
        if (variacao.compareTo(BigDecimal.ZERO) > 0) {
            return "AUMENTO";
        } else if (variacao.compareTo(BigDecimal.ZERO) < 0) {
            return "REDUCAO";
        }
        return "ESTAVEL";
    }

    private String determinarImpacto(BigDecimal variacao) {
        if (variacao.compareTo(BigDecimal.ZERO) < 0) {
            return "POSITIVO"; // Redução de despesa é positivo
        } else if (variacao.compareTo(BigDecimal.ZERO) > 0) {
            return "NEGATIVO"; // Aumento de despesa é negativo
        }
        return "NEUTRO";
    }
}
