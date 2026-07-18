package com.mondragon.payroll.service;

import com.mondragon.payroll.dto.PayrollDto;
import com.mondragon.payroll.dto.PayrollItemDto;
import com.mondragon.payroll.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PayrollExcelExportService {

    private final PayrollService payrollService;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public byte[] export(Long payrollId) {
        PayrollDto payroll = payrollService.getPrintData(payrollId);
        if (payroll.getItems() == null || payroll.getItems().isEmpty()) {
            throw new BusinessException("Payroll has no items to export");
        }

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Payroll");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            String[] headers = {
                    "Employee Name",
                    "Basic Pay / Day",
                    "OT Pay / Day",
                    "Total Pay",
                    "CA Running Balance",
                    "CA Deduction",
                    "CA Balance After Deduction",
                    "SSS Deduction",
                    "PhilHealth Deduction",
                    "Savings Deduction",
                    "Total Salary"
            };

            Row meta = sheet.createRow(0);
            meta.createCell(0).setCellValue("Payroll Number");
            meta.createCell(1).setCellValue(payroll.getPayrollNumber());
            Row meta2 = sheet.createRow(1);
            meta2.createCell(0).setCellValue("Period");
            meta2.createCell(1).setCellValue(
                    format(payroll.getPeriodStart()) + " to " + format(payroll.getPeriodEnd()));
            Row meta3 = sheet.createRow(2);
            meta3.createCell(0).setCellValue("Generated At");
            meta3.createCell(1).setCellValue(
                    payroll.getGeneratedAt() != null ? payroll.getGeneratedAt().toString() : "");

            Row header = sheet.createRow(4);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 5;
            for (PayrollItemDto item : payroll.getItems()) {
                Row row = sheet.createRow(rowIdx++);
                BigDecimal caBal = nz(item.getCashAdvanceBalance());
                BigDecimal caDed = nz(item.getCashAdvanceDeduction());
                BigDecimal caAfter = item.getCashAdvanceBalanceAfter() != null
                        ? item.getCashAdvanceBalanceAfter()
                        : caBal.subtract(caDed).max(BigDecimal.ZERO);

                int c = 0;
                row.createCell(c++).setCellValue(nullToEmpty(item.getEmployeeName()));
                setMoney(row.createCell(c++), item.getDailyRate());
                setMoney(row.createCell(c++), item.getOvertimeRate());
                setMoney(row.createCell(c++), item.getGrossPay());
                setMoney(row.createCell(c++), caBal);
                setMoney(row.createCell(c++), caDed);
                setMoney(row.createCell(c++), caAfter);
                setMoney(row.createCell(c++), item.getSssDeduction());
                setMoney(row.createCell(c++), item.getPhilhealthDeduction());
                setMoney(row.createCell(c++), item.getSavingsDeduction());
                setMoney(row.createCell(c), item.getNetPay());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Failed to export Excel: " + e.getMessage());
        }
    }

    private static void setMoney(Cell cell, BigDecimal value) {
        cell.setCellValue(nz(value).doubleValue());
    }

    private static BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String format(java.time.LocalDate date) {
        return date == null ? "" : DATE_FMT.format(date);
    }
}
