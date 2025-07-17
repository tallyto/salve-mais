package com.tallyto.gestorfinanceiro.core.application.services;

import com.tallyto.gestorfinanceiro.api.dto.RelatorioMensalDTO;
import com.tallyto.gestorfinanceiro.core.domain.entities.*;
import com.tallyto.gestorfinanceiro.core.infra.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class RelatorioMensalService {

    @Autowired
    private ProventoRepository proventoRepository;

    @Autowired
    private ContaFixaRepository contaFixaRepository;

    @Autowired
    private FaturaRepository faturaRepository;

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

        // 5. Calcular totais
        BigDecimal totalProventos = proventosDTO.stream()
                .map(RelatorioMensalDTO.ItemProventoDTO::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCartoes = cartoesDTO.stream()
                .map(RelatorioMensalDTO.ItemCartaoDTO::valorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalGastosFixos = gastosFixosDTO.stream()
                .map(RelatorioMensalDTO.ItemGastoFixoDTO::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 6. Calcular saldo final e dívidas (sem outras despesas)
        BigDecimal totalDespesas = totalCartoes.add(totalGastosFixos);
        BigDecimal saldoFinal = totalProventos.subtract(totalDespesas);

        // 7. Criar resumo financeiro
        RelatorioMensalDTO.ResumoFinanceiroDTO resumoFinanceiro = new RelatorioMensalDTO.ResumoFinanceiroDTO(
                totalProventos,
                BigDecimal.ZERO, // Receitas pendentes - pode ser implementado futuramente
                totalCartoes,
                totalGastosFixos,
                BigDecimal.ZERO, // Outras despesas removidas
                saldoFinal,
                totalDespesas
        );

        // 8. Criar lista vazia para receitas pendentes (funcionalidade futura)
        List<RelatorioMensalDTO.ItemReceitasPendentesDTO> receitasPendentes = List.of();

        // 9. Criar lista vazia para outras despesas (removida esta funcionalidade)
        List<RelatorioMensalDTO.ItemOutrasDescricaoDTO> outrasDespesasDTO = List.of();

        return new RelatorioMensalDTO(
                mesReferencia,
                inicioMes,
                resumoFinanceiro,
                proventosDTO,
                receitasPendentes,
                cartoesDTO,
                gastosFixosDTO,
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
}
