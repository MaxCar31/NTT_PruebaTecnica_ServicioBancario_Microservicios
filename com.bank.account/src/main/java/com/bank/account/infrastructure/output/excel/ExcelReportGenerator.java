package com.bank.account.infrastructure.output.excel;

import com.bank.account.domain.model.AccountStatement;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ENHANCED: Excel report generator with better formatting and additional info.
 *
 * New features:
 * - Color-coded transactions (green for credits, red for debits)
 * - Summary section with totals
 * - Better formatting and headers
 * - Account-level summaries
 */
@Component
public class ExcelReportGenerator {

    // Color constants
    private static final short COLOR_HEADER = IndexedColors.GREY_25_PERCENT.getIndex();
    private static final short COLOR_CREDIT = IndexedColors.LIGHT_GREEN.getIndex();
    private static final short COLOR_DEBIT = IndexedColors.LIGHT_ORANGE.getIndex();

    public byte[] generateStatement(AccountStatement statement) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle timeStyle = createTimeStyle(workbook);
            CellStyle creditStyle = createCreditStyle(workbook);
            CellStyle debitStyle = createDebitStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle summaryHeaderStyle = createSummaryHeaderStyle(workbook);

            Sheet sheet = workbook.createSheet("Account Statement");

            int rowNum = 0;

            // Add title and customer info
            rowNum = addReportHeader(sheet, statement, rowNum, workbook);

            // Add summary section (NEW)
            rowNum = addSummarySection(sheet, statement, rowNum, summaryHeaderStyle, currencyStyle);

            // Add transaction headers
            String[] headers = {
                    "Date", "Time", "Client", "Account Number", "Account Type",
                    "Movement Type", "Amount", "Available Balance"
            };

            Row headerRow = sheet.createRow(rowNum++);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Add transaction data
            if (statement.getAccounts() != null) {
                for (AccountStatement.AccountReportDetail account : statement.getAccounts()) {
                    if (account.getMovements() != null) {
                        for (AccountStatement.MovementReportDetail movement : account.getMovements()) {
                            Row dataRow = sheet.createRow(rowNum++);

                            // Determine if this is a credit or debit for styling
                            boolean isCredit = movement.getAmount().compareTo(BigDecimal.ZERO) > 0;
                            CellStyle amountStyle = isCredit ? creditStyle : debitStyle;

                            // Date
                            Cell dateCell = dataRow.createCell(0);
                            dateCell.setCellValue(movement.getDate().toLocalDate());
                            dateCell.setCellStyle(dateStyle);

                            // Time
                            Cell timeCell = dataRow.createCell(1);
                            timeCell.setCellValue(movement.getDate());
                            timeCell.setCellStyle(timeStyle);

                            // Client
                            dataRow.createCell(2).setCellValue(statement.getClientName());

                            // Account Number
                            dataRow.createCell(3).setCellValue(account.getAccountNumber());

                            // Account Type
                            dataRow.createCell(4).setCellValue(account.getAccountType());

                            // Movement Type
                            Cell typeCell = dataRow.createCell(5);
                            typeCell.setCellValue(movement.getMovementType());
                            typeCell.setCellStyle(amountStyle);

                            // Amount (with color coding)
                            Cell amountCell = dataRow.createCell(6);
                            amountCell.setCellValue(movement.getAmount().doubleValue());
                            amountCell.setCellStyle(amountStyle);

                            // Balance
                            Cell balanceCell = dataRow.createCell(7);
                            balanceCell.setCellValue(movement.getBalanceAfterMovement().doubleValue());
                            balanceCell.setCellStyle(currencyStyle);
                        }
                    }
                }
            }

            // Add auto-filter
            if (rowNum > 1) {
                sheet.setAutoFilter(new CellRangeAddress(rowNum - (countTotalMovements(statement) + 1),
                        rowNum - 1, 0, headers.length - 1));
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * NEW: Add report header with title and date range
     */
    private int addReportHeader(Sheet sheet, AccountStatement statement, int rowNum, Workbook workbook) {
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleStyle.setFont(titleFont);

        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("ACCOUNT STATEMENT");
        titleCell.setCellStyle(titleStyle);

        Row clientRow = sheet.createRow(rowNum++);
        clientRow.createCell(0).setCellValue("Client:");
        clientRow.createCell(1).setCellValue(statement.getClientName());

        Row periodRow = sheet.createRow(rowNum++);
        periodRow.createCell(0).setCellValue("Period:");
        periodRow.createCell(1).setCellValue(
                statement.getStartDate().toLocalDate() + " to " + statement.getEndDate().toLocalDate()
        );

        rowNum++; // Empty row for spacing

        return rowNum;
    }

    /**
     * NEW: Add summary section with account totals
     */
    private int addSummarySection(Sheet sheet, AccountStatement statement, int rowNum,
                                  CellStyle headerStyle, CellStyle currencyStyle) {
        if (statement.getAccounts() == null || statement.getAccounts().isEmpty()) {
            return rowNum;
        }

        Row summaryHeaderRow = sheet.createRow(rowNum++);
        Cell summaryHeaderCell = summaryHeaderRow.createCell(0);
        summaryHeaderCell.setCellValue("ACCOUNT SUMMARY");
        summaryHeaderCell.setCellStyle(headerStyle);

        Row summaryLabelsRow = sheet.createRow(rowNum++);
        summaryLabelsRow.createCell(0).setCellValue("Account");
        summaryLabelsRow.createCell(1).setCellValue("Type");
        summaryLabelsRow.createCell(2).setCellValue("Initial Balance");
        summaryLabelsRow.createCell(3).setCellValue("Final Balance");
        summaryLabelsRow.createCell(4).setCellValue("Net Change");

        for (Cell cell : summaryLabelsRow) {
            cell.setCellStyle(headerStyle);
        }

        BigDecimal totalInitial = BigDecimal.ZERO;
        BigDecimal totalFinal = BigDecimal.ZERO;

        for (AccountStatement.AccountReportDetail account : statement.getAccounts()) {
            Row summaryRow = sheet.createRow(rowNum++);
            summaryRow.createCell(0).setCellValue(account.getAccountNumber());
            summaryRow.createCell(1).setCellValue(account.getAccountType());

            Cell initialCell = summaryRow.createCell(2);
            initialCell.setCellValue(account.getInitialBalance().doubleValue());
            initialCell.setCellStyle(currencyStyle);

            Cell finalCell = summaryRow.createCell(3);
            finalCell.setCellValue(account.getFinalBalance().doubleValue());
            finalCell.setCellStyle(currencyStyle);

            BigDecimal netChange = account.getFinalBalance().subtract(account.getInitialBalance());
            Cell changeCell = summaryRow.createCell(4);
            changeCell.setCellValue(netChange.doubleValue());
            changeCell.setCellStyle(netChange.compareTo(BigDecimal.ZERO) >= 0
                    ? createCreditStyle(sheet.getWorkbook())
                    : createDebitStyle(sheet.getWorkbook()));

            totalInitial = totalInitial.add(account.getInitialBalance());
            totalFinal = totalFinal.add(account.getFinalBalance());
        }

        // Add totals row
        Row totalsRow = sheet.createRow(rowNum++);
        Cell totalsLabel = totalsRow.createCell(0);
        totalsLabel.setCellValue("TOTAL");
        totalsLabel.setCellStyle(headerStyle);

        Cell totalInitialCell = totalsRow.createCell(2);
        totalInitialCell.setCellValue(totalInitial.doubleValue());
        totalInitialCell.setCellStyle(currencyStyle);

        Cell totalFinalCell = totalsRow.createCell(3);
        totalFinalCell.setCellValue(totalFinal.doubleValue());
        totalFinalCell.setCellStyle(currencyStyle);

        BigDecimal totalChange = totalFinal.subtract(totalInitial);
        Cell totalChangeCell = totalsRow.createCell(4);
        totalChangeCell.setCellValue(totalChange.doubleValue());
        totalChangeCell.setCellStyle(totalChange.compareTo(BigDecimal.ZERO) >= 0
                ? createCreditStyle(sheet.getWorkbook())
                : createDebitStyle(sheet.getWorkbook()));

        rowNum++; // Empty row for spacing

        return rowNum;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(COLOR_HEADER);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }

    private CellStyle createSummaryHeaderStyle(Workbook workbook) {
        CellStyle style = createHeaderStyle(workbook);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper creationHelper = workbook.getCreationHelper();
        style.setDataFormat(creationHelper.createDataFormat().getFormat("yyyy-mm-dd"));
        return style;
    }

    private CellStyle createTimeStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper creationHelper = workbook.getCreationHelper();
        style.setDataFormat(creationHelper.createDataFormat().getFormat("h:mm:ss AM/PM"));
        return style;
    }

    private CellStyle createCreditStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(COLOR_CREDIT);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createDebitStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(COLOR_DEBIT);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper creationHelper = workbook.getCreationHelper();
        style.setDataFormat(creationHelper.createDataFormat().getFormat("$#,##0.00"));
        return style;
    }

    private int countTotalMovements(AccountStatement statement) {
        if (statement.getAccounts() == null) return 0;
        return statement.getAccounts().stream()
                .mapToInt(account -> account.getMovements() != null ? account.getMovements().size() : 0)
                .sum();
    }
}