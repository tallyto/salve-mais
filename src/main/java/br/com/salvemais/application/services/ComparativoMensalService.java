package br.com.salvemais.application.services;

import br.com.salvemais.web.api.dto.ComparativoMensalDTO;
import br.com.salvemais.web.api.dto.RelatorioMensalDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ComparativoMensalService {

    public ComparativoMensalDTO gerarComparativoMensal(RelatorioMensalDTO relatorioAnterior,
                                                       RelatorioMensalDTO relatorioAtual) {
        ComparativoMensalDTO.ResumoComparativoDTO resumo = criarResumoComparativo(relatorioAnterior, relatorioAtual);
        List<ComparativoMensalDTO.ComparativoCategoriaDTO> categorias = compararCategorias(relatorioAnterior, relatorioAtual);
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

        Map<String, BigDecimal> categoriasAnterior = new HashMap<>();
        anterior.gastosFixos().forEach(g ->
                categoriasAnterior.merge(g.categoria(), g.valor(), BigDecimal::add));
        anterior.comprasDebito().forEach(c ->
                categoriasAnterior.merge(c.categoria(), c.valor(), BigDecimal::add));
        anterior.cartoes().forEach(cartao ->
                cartao.compras().forEach(c ->
                        categoriasAnterior.merge(c.categoria(), c.valor(), BigDecimal::add)));

        Map<String, BigDecimal> categoriasAtual = new HashMap<>();
        atual.gastosFixos().forEach(g ->
                categoriasAtual.merge(g.categoria(), g.valor(), BigDecimal::add));
        atual.comprasDebito().forEach(c ->
                categoriasAtual.merge(c.categoria(), c.valor(), BigDecimal::add));
        atual.cartoes().forEach(cartao ->
                cartao.compras().forEach(c ->
                        categoriasAtual.merge(c.categoria(), c.valor(), BigDecimal::add)));

        Set<String> todasCategorias = new HashSet<>();
        todasCategorias.addAll(categoriasAnterior.keySet());
        todasCategorias.addAll(categoriasAtual.keySet());

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
                .filter(c -> c.variacao().abs().compareTo(BigDecimal.TEN) > 0)
                .sorted((c1, c2) -> c2.variacao().abs().compareTo(c1.variacao().abs()))
                .limit(5)
                .map(c -> new ComparativoMensalDTO.DestaqueMudancaDTO(
                        c.categoria(),
                        c.tipo(),
                        c.variacao().abs(),
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
            return "POSITIVO";
        } else if (variacao.compareTo(BigDecimal.ZERO) > 0) {
            return "NEGATIVO";
        }
        return "NEUTRO";
    }
}
