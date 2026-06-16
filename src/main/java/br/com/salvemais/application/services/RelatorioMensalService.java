package br.com.salvemais.application.services;

import br.com.salvemais.domain.entities.CompraDebito;
import br.com.salvemais.domain.entities.ContaFixa;
import br.com.salvemais.domain.entities.Fatura;
import br.com.salvemais.domain.entities.Provento;
import br.com.salvemais.infrastructure.repositories.CompraDebitoRepository;
import br.com.salvemais.infrastructure.repositories.ContaFixaRepository;
import br.com.salvemais.infrastructure.repositories.FaturaRepository;
import br.com.salvemais.infrastructure.repositories.ProventoRepository;
import br.com.salvemais.web.api.dto.ComparativoMensalDTO;
import br.com.salvemais.web.api.dto.RelatorioMensalDTO;
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

    @Autowired
    private CompraDebitoRepository compraDebitoRepository;

    @Autowired
    private ComparativoMensalService comparativoMensalService;

    public RelatorioMensalDTO gerarRelatorioMensal(int ano, int mes) {
        YearMonth yearMonth = YearMonth.of(ano, mes);
        LocalDate inicioMes = yearMonth.atDay(1);
        LocalDate fimMes = yearMonth.atEndOfMonth();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault());
        String mesReferencia = yearMonth.format(formatter);

        List<RelatorioMensalDTO.ItemProventoDTO> proventosDTO = proventoRepository.findByDataBetween(inicioMes, fimMes).stream()
                .map(provento -> new RelatorioMensalDTO.ItemProventoDTO(
                        provento.getId(),
                        provento.getDescricao(),
                        provento.getValor(),
                        provento.getData(),
                        provento.getConta() != null ? provento.getConta().getTitular() : "Conta não informada"
                ))
                .collect(Collectors.toList());

        List<RelatorioMensalDTO.ItemGastoFixoDTO> gastosFixosDTO = contaFixaRepository.findByVencimentoBetween(inicioMes, fimMes).stream()
                .map(conta -> new RelatorioMensalDTO.ItemGastoFixoDTO(
                        conta.getId(),
                        conta.getNome(),
                        conta.getValor(),
                        conta.getVencimento(),
                        conta.getCategoria() != null ? conta.getCategoria().getNome() : "Categoria não informada",
                        conta.isPago()
                ))
                .collect(Collectors.toList());

        List<RelatorioMensalDTO.ItemCartaoDTO> cartoesDTO = faturaRepository.findByDataVencimentoBetween(inicioMes, fimMes).stream()
                .map(fatura -> new RelatorioMensalDTO.ItemCartaoDTO(
                        fatura.getCartaoCredito().getId(),
                        fatura.getCartaoCredito().getNome(),
                        fatura.getValorTotal(),
                        fatura.getDataVencimento(),
                        fatura.getCompras().stream()
                                .map(compra -> new RelatorioMensalDTO.CompraCartaoDTO(
                                        compra.getId(),
                                        compra.getDescricao(),
                                        compra.getValor(),
                                        compra.getData(),
                                        compra.getCategoria() != null ? compra.getCategoria().getNome() : "Categoria não informada"
                                ))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());

        List<RelatorioMensalDTO.ItemCompraDebitoDTO> comprasDebitoDTO = compraDebitoRepository.findByDataCompraBetween(inicioMes, fimMes).stream()
                .map(compra -> new RelatorioMensalDTO.ItemCompraDebitoDTO(
                        compra.getId(),
                        compra.getNome(),
                        compra.getValor(),
                        compra.getDataCompra(),
                        compra.getCategoria() != null ? compra.getCategoria().getNome() : "Categoria não informada",
                        compra.getConta() != null ? compra.getConta().getTitular() : "Conta não informada"
                ))
                .collect(Collectors.toList());

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

        BigDecimal totalDespesas = totalCartoes.add(totalGastosFixos).add(totalComprasDebito);
        BigDecimal saldoFinal = totalProventos.subtract(totalDespesas);

        RelatorioMensalDTO.ResumoFinanceiroDTO resumoFinanceiro = new RelatorioMensalDTO.ResumoFinanceiroDTO(
                totalProventos,
                BigDecimal.ZERO,
                totalCartoes,
                totalGastosFixos,
                totalComprasDebito,
                BigDecimal.ZERO,
                saldoFinal,
                totalDespesas
        );

        return new RelatorioMensalDTO(
                mesReferencia,
                inicioMes,
                resumoFinanceiro,
                proventosDTO,
                List.of(),
                cartoesDTO,
                gastosFixosDTO,
                comprasDebitoDTO,
                List.of(),
                saldoFinal,
                totalDespesas
        );
    }

    public RelatorioMensalDTO gerarRelatorioMensalAtual() {
        YearMonth atual = YearMonth.now();
        return gerarRelatorioMensal(atual.getYear(), atual.getMonthValue());
    }

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

    public ComparativoMensalDTO gerarComparativoMensal(int anoAnterior, int mesAnterior, int anoAtual, int mesAtual) {
        RelatorioMensalDTO relatorioAnterior = gerarRelatorioMensal(anoAnterior, mesAnterior);
        RelatorioMensalDTO relatorioAtual = gerarRelatorioMensal(anoAtual, mesAtual);
        return comparativoMensalService.gerarComparativoMensal(relatorioAnterior, relatorioAtual);
    }
}
