package br.com.salvemais.application.services;

import br.com.salvemais.web.api.dto.RelatorioMensalDTO;
import lombok.RequiredArgsConstructor;
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
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class ExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final DashboardExcelExportService dashboardExcelExportService;
    private final RelatorioMensalService relatorioMensalService;

    public byte[] generateDashboardExcel(Integer mes, Integer ano) throws IOException {
        return dashboardExcelExportService.generateDashboardExcel(mes, ano);
    }

    public byte[] generateRelatorioMensalExcel(Integer mes, Integer ano) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            RelatorioMensalDTO relatorio = relatorioMensalService.gerarRelatorioMensal(ano, mes);
            createRelatorioSheet(workbook, relatorio, ano, mes, headerStyle, titleStyle, currencyStyle, dateStyle);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private void createRelatorioSheet(Workbook workbook, RelatorioMensalDTO relatorio, Integer ano, Integer mes,
                                      CellStyle headerStyle, CellStyle titleStyle,
                                      CellStyle currencyStyle, CellStyle dateStyle) {
        Sheet sheet = workbook.createSheet("Relatório Mensal");
        int rowNum = 0;

        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("RELATÓRIO MENSAL");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));

        rowNum++;

        Row dateRow = sheet.createRow(rowNum++);
        dateRow.createCell(0).setCellValue("Data de Exportação:");
        dateRow.createCell(1).setCellValue(LocalDate.now().format(DATE_FORMATTER));

        rowNum++;

        if (relatorio.proventos() != null && !relatorio.proventos().isEmpty()) {
            Row provTitleRow = sheet.createRow(rowNum++);
            Cell provTitleCell = provTitleRow.createCell(0);
            provTitleCell.setCellValue("PROVENTOS");
            provTitleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 2));

            sheet.createRow(rowNum++);
            Row provHeaderRow = sheet.createRow(rowNum++);
            createHeaderCell(provHeaderRow, 0, "Descrição", headerStyle);
            createHeaderCell(provHeaderRow, 1, "Valor (R$)", headerStyle);

            BigDecimal totalProventos = BigDecimal.ZERO;
            for (var provento : relatorio.proventos()) {
                createDataRow(sheet, rowNum++, provento.descricao(), provento.valor(), currencyStyle);
                totalProventos = totalProventos.add(provento.valor());
            }

            Row totalProvRow = sheet.createRow(rowNum++);
            totalProvRow.createCell(0).setCellValue("Total Proventos");
            Cell totalProvCell = totalProvRow.createCell(1);
            totalProvCell.setCellValue(totalProventos.doubleValue());
            totalProvCell.setCellStyle(currencyStyle);

            rowNum++;
        }

        if (relatorio.gastosFixos() != null && !relatorio.gastosFixos().isEmpty()) {
            Row fixTitleRow = sheet.createRow(rowNum++);
            Cell fixTitleCell = fixTitleRow.createCell(0);
            fixTitleCell.setCellValue("GASTOS FIXOS");
            fixTitleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 2));

            sheet.createRow(rowNum++);
            Row fixHeaderRow = sheet.createRow(rowNum++);
            createHeaderCell(fixHeaderRow, 0, "Descrição", headerStyle);
            createHeaderCell(fixHeaderRow, 1, "Valor (R$)", headerStyle);

            BigDecimal totalFixos = BigDecimal.ZERO;
            for (var gasto : relatorio.gastosFixos()) {
                createDataRow(sheet, rowNum++, gasto.nome(), gasto.valor(), currencyStyle);
                totalFixos = totalFixos.add(gasto.valor());
            }

            Row totalFixRow = sheet.createRow(rowNum++);
            totalFixRow.createCell(0).setCellValue("Total Gastos Fixos");
            Cell totalFixCell = totalFixRow.createCell(1);
            totalFixCell.setCellValue(totalFixos.doubleValue());
            totalFixCell.setCellStyle(currencyStyle);

            rowNum++;
        }

        if (relatorio.comprasDebito() != null && !relatorio.comprasDebito().isEmpty()) {
            Row debitoTitleRow = sheet.createRow(rowNum++);
            Cell debitoTitleCell = debitoTitleRow.createCell(0);
            debitoTitleCell.setCellValue("COMPRAS EM DÉBITO");
            debitoTitleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 2));

            sheet.createRow(rowNum++);
            Row debitoHeaderRow = sheet.createRow(rowNum++);
            createHeaderCell(debitoHeaderRow, 0, "Descrição", headerStyle);
            createHeaderCell(debitoHeaderRow, 1, "Valor (R$)", headerStyle);

            BigDecimal totalDebito = BigDecimal.ZERO;
            for (var compra : relatorio.comprasDebito()) {
                createDataRow(sheet, rowNum++, compra.descricao(), compra.valor(), currencyStyle);
                totalDebito = totalDebito.add(compra.valor());
            }

            Row totalDebitoRow = sheet.createRow(rowNum++);
            totalDebitoRow.createCell(0).setCellValue("Total Compras em Débito");
            Cell totalDebitoCell = totalDebitoRow.createCell(1);
            totalDebitoCell.setCellValue(totalDebito.doubleValue());
            totalDebitoCell.setCellStyle(currencyStyle);

            rowNum++;
        }

        LocalDate inicioMes = LocalDate.of(ano, mes, 1);
        LocalDate fimMes = inicioMes.withDayOfMonth(inicioMes.lengthOfMonth());
        sheet.createRow(rowNum++); // linha vazia antes de usar faturas
        // Os dados de fatura já vêm no relatório mensal; esta seção mantém o resumo consolidado.
        if (relatorio.resumoFinanceiro() != null) {
            Row cartaoTitleRow = sheet.createRow(rowNum++);
            Cell cartaoTitleCell = cartaoTitleRow.createCell(0);
            cartaoTitleCell.setCellValue("FATURAS DE CARTÃO");
            cartaoTitleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 2));

            Row cartaoHeaderRow = sheet.createRow(rowNum++);
            createHeaderCell(cartaoHeaderRow, 0, "Métrica", headerStyle);
            createHeaderCell(cartaoHeaderRow, 1, "Valor (R$)", headerStyle);

            createDataRow(sheet, rowNum++, "Total Cartões", relatorio.resumoFinanceiro().totalCartoes(), currencyStyle);
        }

        if (relatorio.outrasDespesas() != null && !relatorio.outrasDespesas().isEmpty()) {
            Row otherTitleRow = sheet.createRow(rowNum++);
            Cell otherTitleCell = otherTitleRow.createCell(0);
            otherTitleCell.setCellValue("OUTRAS DESPESAS");
            otherTitleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 2));

            sheet.createRow(rowNum++);
            Row otherHeaderRow = sheet.createRow(rowNum++);
            createHeaderCell(otherHeaderRow, 0, "Descrição", headerStyle);
            createHeaderCell(otherHeaderRow, 1, "Valor (R$)", headerStyle);

            BigDecimal totalOtras = BigDecimal.ZERO;
            for (var despesa : relatorio.outrasDespesas()) {
                createDataRow(sheet, rowNum++, despesa.descricao(), despesa.valor(), currencyStyle);
                totalOtras = totalOtras.add(despesa.valor());
            }

            Row totalOtherRow = sheet.createRow(rowNum++);
            totalOtherRow.createCell(0).setCellValue("Total Outras Despesas");
            Cell totalOtherCell = totalOtherRow.createCell(1);
            totalOtherCell.setCellValue(totalOtras.doubleValue());
            totalOtherCell.setCellStyle(currencyStyle);

            rowNum++;
        }

        Row summaryTitleRow = sheet.createRow(rowNum++);
        Cell summaryTitleCell = summaryTitleRow.createCell(0);
        summaryTitleCell.setCellValue("RESUMO");
        summaryTitleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 2));

        sheet.createRow(rowNum++);
        Row summaryHeaderRow = sheet.createRow(rowNum++);
        createHeaderCell(summaryHeaderRow, 0, "Descrição", headerStyle);
        createHeaderCell(summaryHeaderRow, 1, "Valor (R$)", headerStyle);

        createDataRow(sheet, rowNum++, "Total Receitas", relatorio.resumoFinanceiro().totalProventos(), currencyStyle);
        createDataRow(sheet, rowNum++, "Total Despesas",
                relatorio.resumoFinanceiro().totalGastosFixos()
                        .add(relatorio.resumoFinanceiro().totalComprasDebito())
                        .add(relatorio.resumoFinanceiro().totalOutrasDespesas())
                        .add(relatorio.resumoFinanceiro().totalCartoes()),
                currencyStyle);
        createDataRow(sheet, rowNum++, "Saldo Líquido", relatorio.resumoFinanceiro().saldoFinal(), currencyStyle);

        sheet.setColumnWidth(0, 30 * 256);
        sheet.setColumnWidth(1, 20 * 256);
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
}
