package br.com.salvemais.application.services;

import br.com.salvemais.domain.entities.Compra;
import br.com.salvemais.domain.entities.CompraParcelada;
import br.com.salvemais.domain.entities.Conta;
import br.com.salvemais.infrastructure.repositories.CompraRepository;
import br.com.salvemais.infrastructure.repositories.CompraParceladaRepository;
import br.com.salvemais.web.api.dto.CategoryExpenseDTO;
import br.com.salvemais.web.api.dto.DashboardSummaryDTO;
import br.com.salvemais.web.api.dto.MonthlyExpenseDTO;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class DashboardExcelExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final DashboardOverviewService dashboardOverviewService;
    private final DashboardCategoryExpenseService dashboardCategoryExpenseService;
    private final DashboardTrendService dashboardTrendService;
    private final ContaService contaService;
    private final CompraRepository compraRepository;
    private final CompraParceladaRepository compraParceladaRepository;

    public DashboardExcelExportService(DashboardOverviewService dashboardOverviewService,
                                       DashboardCategoryExpenseService dashboardCategoryExpenseService,
                                       DashboardTrendService dashboardTrendService,
                                       ContaService contaService,
                                       CompraRepository compraRepository,
                                       CompraParceladaRepository compraParceladaRepository) {
        this.dashboardOverviewService = dashboardOverviewService;
        this.dashboardCategoryExpenseService = dashboardCategoryExpenseService;
        this.dashboardTrendService = dashboardTrendService;
        this.contaService = contaService;
        this.compraRepository = compraRepository;
        this.compraParceladaRepository = compraParceladaRepository;
    }

    public byte[] generateDashboardExcel(Integer mes, Integer ano) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle percentStyle = createPercentStyle(workbook);

            DashboardSummaryDTO summary = dashboardOverviewService.getSummary(mes, ano);
            List<CategoryExpenseDTO> categories = dashboardCategoryExpenseService.getExpensesByCategory(mes, ano);
            List<MonthlyExpenseDTO> monthlyTrend = dashboardTrendService.getMonthlyExpenseTrendByYear(
                    ano != null ? ano : LocalDate.now().getYear()
            );
            List<Conta> contas = contaService.findAllAccounts(PageRequest.of(0, 100)).getContent();
            List<Compra> compras = compraRepository.findByDataMesEAno(
                    PageRequest.of(0, 10, Sort.by("data").descending()),
                    mes != null ? mes : LocalDate.now().getMonthValue(),
                    ano != null ? ano : LocalDate.now().getYear()
            ).getContent();
            List<CompraParcelada> comprasParceladas = compraParceladaRepository.findAll()
                    .stream()
                    .filter(cp -> cp.getParcelas() != null && cp.getParcelas().stream().anyMatch(p -> !p.isPaga()))
                    .limit(10)
                    .toList();

            createSummarySheet(workbook, summary, headerStyle, titleStyle, currencyStyle);
            createCategoriesSheet(workbook, categories, headerStyle, titleStyle, currencyStyle, percentStyle);
            createAccountsSheet(workbook, contas, headerStyle, titleStyle, currencyStyle);
            createTransactionsSheet(workbook, compras, headerStyle, titleStyle, currencyStyle, dateStyle);
            createInstallmentsSheet(workbook, comprasParceladas, headerStyle, titleStyle, currencyStyle, dateStyle);
            createTrendSheet(workbook, monthlyTrend, headerStyle, titleStyle, currencyStyle);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private void createSummarySheet(Workbook workbook, DashboardSummaryDTO summary,
                                    CellStyle headerStyle, CellStyle titleStyle, CellStyle currencyStyle) {
        Sheet sheet = workbook.createSheet("Resumo Financeiro");
        int rowNum = 0;

        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("RESUMO FINANCEIRO");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));

        rowNum++;

        Row dateRow = sheet.createRow(rowNum++);
        dateRow.createCell(0).setCellValue("Data de Exportação:");
        dateRow.createCell(1).setCellValue(LocalDate.now().format(DATE_FORMATTER));

        rowNum++;

        Row headerRow = sheet.createRow(rowNum++);
        Cell headerCell1 = headerRow.createCell(0);
        headerCell1.setCellValue("Métrica");
        headerCell1.setCellStyle(headerStyle);

        Cell headerCell2 = headerRow.createCell(1);
        headerCell2.setCellValue("Valor (R$)");
        headerCell2.setCellStyle(headerStyle);

        createDataRow(sheet, rowNum++, "Saldo Total", summary.getSaldoTotal(), currencyStyle);
        createDataRow(sheet, rowNum++, "Receitas do Mês", summary.getReceitasMes(), currencyStyle);
        createDataRow(sheet, rowNum++, "Despesas do Mês", summary.getDespesasMes(), currencyStyle);
        createDataRow(sheet, rowNum++, "Resultado Mensal",
                summary.getReceitasMes().subtract(summary.getDespesasMes()), currencyStyle);

        if (summary.getSaldoMesAnterior() != null) {
            createDataRow(sheet, rowNum++, "Saldo Mês Anterior", summary.getSaldoMesAnterior(), currencyStyle);
        }

        if (summary.getReservaEmergencia() != null) {
            rowNum++;
            Row subTitleRow = sheet.createRow(rowNum++);
            Cell subTitleCell = subTitleRow.createCell(0);
            subTitleCell.setCellValue("INDICADORES DE SAÚDE FINANCEIRA");
            subTitleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 2));

            if (summary.getReservaEmergencia().getSaldoAtual() != null) {
                createDataRow(sheet, rowNum++, "Reserva de Emergência Atual",
                        summary.getReservaEmergencia().getSaldoAtual(), currencyStyle);
            }
            if (summary.getReservaEmergencia().getObjetivo() != null) {
                createDataRow(sheet, rowNum++, "Meta Reserva de Emergência",
                        summary.getReservaEmergencia().getObjetivo(), currencyStyle);
            }
            if (summary.getReservaEmergencia().getPercentualConcluido() != null) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue("Percentual Concluído");
                Cell valueCell = row.createCell(1);
                valueCell.setCellValue(summary.getReservaEmergencia().getPercentualConcluido().doubleValue() / 100);
                valueCell.setCellStyle(createPercentStyle(workbook));
            }
        }

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createCategoriesSheet(Workbook workbook, List<CategoryExpenseDTO> categories,
                                       CellStyle headerStyle, CellStyle titleStyle,
                                       CellStyle currencyStyle, CellStyle percentStyle) {
        Sheet sheet = workbook.createSheet("Despesas por Categoria");
        int rowNum = 0;

        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("DESPESAS POR CATEGORIA");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));

        rowNum++;

        Row headerRow = sheet.createRow(rowNum++);
        createHeaderCell(headerRow, 0, "Categoria", headerStyle);
        createHeaderCell(headerRow, 1, "Valor (R$)", headerStyle);
        createHeaderCell(headerRow, 2, "Percentual (%)", headerStyle);

        for (CategoryExpenseDTO category : categories) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(category.categoriaNome());

            Cell valueCell = row.createCell(1);
            valueCell.setCellValue(category.valorTotal().doubleValue());
            valueCell.setCellStyle(currencyStyle);

            Cell percentCell = row.createCell(2);
            percentCell.setCellValue(category.percentual() / 100);
            percentCell.setCellStyle(percentStyle);
        }

        for (int i = 0; i < 3; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createAccountsSheet(Workbook workbook, List<Conta> contas,
                                     CellStyle headerStyle, CellStyle titleStyle, CellStyle currencyStyle) {
        Sheet sheet = workbook.createSheet("Contas e Saldos");
        int rowNum = 0;

        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("CONTAS E SALDOS");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

        rowNum++;

        Row headerRow = sheet.createRow(rowNum++);
        createHeaderCell(headerRow, 0, "Titular", headerStyle);
        createHeaderCell(headerRow, 1, "Tipo", headerStyle);
        createHeaderCell(headerRow, 2, "Saldo (R$)", headerStyle);
        createHeaderCell(headerRow, 3, "Descrição", headerStyle);

        for (Conta conta : contas) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(conta.getTitular());
            row.createCell(1).setCellValue(conta.getTipo().name());

            Cell saldoCell = row.createCell(2);
            saldoCell.setCellValue(conta.getSaldo().doubleValue());
            saldoCell.setCellStyle(currencyStyle);

            row.createCell(3).setCellValue(conta.getDescricao() != null ? conta.getDescricao() : "");
        }

        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createTransactionsSheet(Workbook workbook, List<Compra> compras,
                                         CellStyle headerStyle, CellStyle titleStyle,
                                         CellStyle currencyStyle, CellStyle dateStyle) {
        Sheet sheet = workbook.createSheet("Transações Recentes");
        int rowNum = 0;

        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("TRANSAÇÕES RECENTES");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

        rowNum++;

        Row headerRow = sheet.createRow(rowNum++);
        createHeaderCell(headerRow, 0, "Data", headerStyle);
        createHeaderCell(headerRow, 1, "Descrição", headerStyle);
        createHeaderCell(headerRow, 2, "Valor (R$)", headerStyle);
        createHeaderCell(headerRow, 3, "Categoria", headerStyle);
        createHeaderCell(headerRow, 4, "Cartão", headerStyle);

        for (Compra compra : compras) {
            Row row = sheet.createRow(rowNum++);

            Cell dateCell = row.createCell(0);
            dateCell.setCellValue(compra.getData());
            dateCell.setCellStyle(dateStyle);

            row.createCell(1).setCellValue(compra.getDescricao());

            Cell valueCell = row.createCell(2);
            valueCell.setCellValue(compra.getValor().doubleValue());
            valueCell.setCellStyle(currencyStyle);

            row.createCell(3).setCellValue(
                    compra.getCategoria() != null ? compra.getCategoria().getNome() : ""
            );
            row.createCell(4).setCellValue(
                    compra.getCartaoCredito() != null ? compra.getCartaoCredito().getNome() : ""
            );
        }

        for (int i = 0; i < 5; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createInstallmentsSheet(Workbook workbook, List<CompraParcelada> compras,
                                         CellStyle headerStyle, CellStyle titleStyle,
                                         CellStyle currencyStyle, CellStyle dateStyle) {
        Sheet sheet = workbook.createSheet("Faturas");
        int rowNum = 0;

        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("FATURAS EM ABERTO");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

        rowNum++;

        Row headerRow = sheet.createRow(rowNum++);
        createHeaderCell(headerRow, 0, "Descrição", headerStyle);
        createHeaderCell(headerRow, 1, "Valor Total (R$)", headerStyle);
        createHeaderCell(headerRow, 2, "Total Parcelas", headerStyle);
        createHeaderCell(headerRow, 3, "Próximo Vencimento", headerStyle);
        createHeaderCell(headerRow, 4, "Status", headerStyle);

        for (CompraParcelada compra : compras) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(compra.getDescricao());

            Cell valueCell = row.createCell(1);
            valueCell.setCellValue(compra.getValorTotal().doubleValue());
            valueCell.setCellStyle(currencyStyle);

            row.createCell(2).setCellValue(compra.getParcelas() != null ? compra.getParcelas().size() : 0);
            row.createCell(3).setCellValue(getProximoVencimento(compra));
            row.createCell(4).setCellValue(temParcelasPendentes(compra) ? "Em aberto" : "Quitada");
        }

        for (int i = 0; i < 5; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createTrendSheet(Workbook workbook, List<MonthlyExpenseDTO> trends,
                                  CellStyle headerStyle, CellStyle titleStyle, CellStyle currencyStyle) {
        Sheet sheet = workbook.createSheet("Tendência Mensal");
        int rowNum = 0;

        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("TENDÊNCIA MENSAL");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

        rowNum++;

        Row headerRow = sheet.createRow(rowNum++);
        createHeaderCell(headerRow, 0, "Mês/Ano", headerStyle);
        createHeaderCell(headerRow, 1, "Receitas (R$)", headerStyle);
        createHeaderCell(headerRow, 2, "Despesas (R$)", headerStyle);
        createHeaderCell(headerRow, 3, "Resultado (R$)", headerStyle);

        for (MonthlyExpenseDTO trend : trends) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(trend.mes());

            Cell receitasCell = row.createCell(1);
            receitasCell.setCellValue(trend.valorReceitas().doubleValue());
            receitasCell.setCellStyle(currencyStyle);

            Cell despesasCell = row.createCell(2);
            despesasCell.setCellValue(trend.valorDespesas().doubleValue());
            despesasCell.setCellStyle(currencyStyle);

            Cell resultadoCell = row.createCell(3);
            BigDecimal resultado = trend.valorReceitas().subtract(trend.valorDespesas());
            resultadoCell.setCellValue(resultado.doubleValue());
            resultadoCell.setCellStyle(currencyStyle);
        }

        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("_-\"R$\"\\ * #,##0.00_-;\\-\"R$\"\\ * #,##0.00_-;_-\"R$\"\\ * \"-\"??_-;_-@_-"));
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("dd/mm/yyyy"));
        return style;
    }

    private CellStyle createPercentStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("0.00%"));
        return style;
    }

    private void createHeaderCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void createDataRow(Sheet sheet, int rowNum, String label, BigDecimal value, CellStyle currencyStyle) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);

        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value.doubleValue());
        valueCell.setCellStyle(currencyStyle);
    }

    private String getProximoVencimento(CompraParcelada compra) {
        if (compra.getParcelas() == null || compra.getParcelas().isEmpty()) {
            return "-";
        }
        return compra.getParcelas().stream()
                .filter(p -> !p.isPaga())
                .map(p -> p.getDataVencimento().format(DATE_FORMATTER))
                .findFirst()
                .orElse("-");
    }

    private boolean temParcelasPendentes(CompraParcelada compra) {
        if (compra.getParcelas() == null || compra.getParcelas().isEmpty()) {
            return false;
        }
        return compra.getParcelas().stream().anyMatch(p -> !p.isPaga());
    }
}
