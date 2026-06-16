package br.com.salvemais.application.services;

import br.com.salvemais.web.api.dto.ComparativoMensalDTO;
import br.com.salvemais.web.api.dto.RelatorioMensalDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ComparativoMensalServiceTest {

    private final ComparativoMensalService comparativoMensalService = new ComparativoMensalService();

    @Test
    void deveGerarComparativoComResumoEVariacoes() {
        RelatorioMensalDTO anterior = relatorio(
                "2025-08",
                new BigDecimal("1000.00"),
                new BigDecimal("700.00")
        );
        RelatorioMensalDTO atual = relatorio(
                "2025-09",
                new BigDecimal("1200.00"),
                new BigDecimal("800.00")
        );

        ComparativoMensalDTO resultado = comparativoMensalService.gerarComparativoMensal(anterior, atual);

        assertEquals("2025-08", resultado.mesAnterior());
        assertEquals("2025-09", resultado.mesAtual());
        assertEquals(new BigDecimal("200.00"), resultado.resumoComparativo().variacaoProventos());
        assertEquals(new BigDecimal("100.00"), resultado.resumoComparativo().variacaoSaldo());
        assertEquals("MELHOROU", resultado.resumoComparativo().statusGeral());
        assertEquals(1, resultado.categorias().size());
        assertEquals(1, resultado.maioresVariacoes().size());
    }

    private RelatorioMensalDTO relatorio(String referencia, BigDecimal proventos, BigDecimal despesas) {
        RelatorioMensalDTO.ResumoFinanceiroDTO resumo = new RelatorioMensalDTO.ResumoFinanceiroDTO(
                proventos,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                despesas,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                proventos.subtract(despesas),
                despesas
        );

        RelatorioMensalDTO.ItemGastoFixoDTO gastoFixo = new RelatorioMensalDTO.ItemGastoFixoDTO(
                1L, "Conta", despesas, LocalDate.of(2025, 9, 1), "Casa", true
        );

        return new RelatorioMensalDTO(
                referencia,
                LocalDate.of(2025, 9, 1),
                resumo,
                List.of(new RelatorioMensalDTO.ItemProventoDTO(
                        1L, "Salário", proventos, LocalDate.of(2025, 9, 1), "Conta"
                )),
                List.of(),
                List.of(),
                List.of(gastoFixo),
                List.of(),
                List.of(),
                proventos.subtract(despesas),
                despesas
        );
    }
}
