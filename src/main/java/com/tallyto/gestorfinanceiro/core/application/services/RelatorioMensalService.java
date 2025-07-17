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
    private CompraRepository compraRepository;

    @Autowired
    private CartaoCreditoRepository cartaoCreditoRepository;

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

        // 2. Buscar contas fixas (gastos fixos)
        List<ContaFixa> contasFixas = contaFixaRepository.findAll();
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

        // 3. Buscar compras do mês
        List<Compra> compras = compraRepository.findByDataBetween(inicioMes, fimMes);
        
        // 4. Separar compras por cartão
        List<CartaoCredito> cartoes = cartaoCreditoRepository.findAll();
        List<RelatorioMensalDTO.ItemCartaoDTO> cartoesDTO = cartoes.stream()
                .map(cartao -> {
                    List<Compra> comprasCartao = compras.stream()
                            .filter(compra -> compra.getCartaoCredito() != null && 
                                    compra.getCartaoCredito().getId().equals(cartao.getId()))
                            .collect(Collectors.toList());
                    
                    List<RelatorioMensalDTO.CompraCartaoDTO> comprasDTO = comprasCartao.stream()
                            .map(compra -> new RelatorioMensalDTO.CompraCartaoDTO(
                                    compra.getId(),
                                    compra.getDescricao(),
                                    compra.getValor(),
                                    compra.getData(),
                                    compra.getCategoria() != null ? compra.getCategoria().getNome() : "Categoria não informada"
                            ))
                            .collect(Collectors.toList());
                    
                    BigDecimal totalCartao = comprasDTO.stream()
                            .map(RelatorioMensalDTO.CompraCartaoDTO::valor)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    return new RelatorioMensalDTO.ItemCartaoDTO(
                            cartao.getId(),
                            cartao.getNome(),
                            totalCartao,
                            cartao.getVencimento(),
                            comprasDTO
                    );
                })
                .filter(cartao -> cartao.valorTotal().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());

        // 5. Buscar outras despesas (compras sem cartão específico ou outras categorias)
        List<Compra> outrasCompras = compras.stream()
                .filter(compra -> compra.getCartaoCredito() == null)
                .collect(Collectors.toList());
        
        List<RelatorioMensalDTO.ItemOutrasDescricaoDTO> outrasDespesasDTO = outrasCompras.stream()
                .map(compra -> new RelatorioMensalDTO.ItemOutrasDescricaoDTO(
                        compra.getId(),
                        compra.getDescricao(),
                        compra.getValor(),
                        compra.getData(),
                        compra.getCategoria() != null ? compra.getCategoria().getNome() : "Categoria não informada",
                        "COMPRA"
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

        BigDecimal totalOutrasDespesas = outrasDespesasDTO.stream()
                .map(RelatorioMensalDTO.ItemOutrasDescricaoDTO::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 7. Calcular saldo final e dívidas
        BigDecimal totalDespesas = totalCartoes.add(totalGastosFixos).add(totalOutrasDespesas);
        BigDecimal saldoFinal = totalProventos.subtract(totalDespesas);

        // 8. Criar resumo financeiro
        RelatorioMensalDTO.ResumoFinanceiroDTO resumoFinanceiro = new RelatorioMensalDTO.ResumoFinanceiroDTO(
                totalProventos,
                BigDecimal.ZERO, // Receitas pendentes - pode ser implementado futuramente
                totalCartoes,
                totalGastosFixos,
                totalOutrasDespesas,
                saldoFinal,
                totalDespesas
        );

        // 9. Criar lista vazia para receitas pendentes (funcionalidade futura)
        List<RelatorioMensalDTO.ItemReceitasPendentesDTO> receitasPendentes = List.of();

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
