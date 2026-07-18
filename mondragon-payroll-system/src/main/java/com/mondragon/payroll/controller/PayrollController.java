package com.mondragon.payroll.controller;

import com.mondragon.payroll.dto.*;
import com.mondragon.payroll.service.PayrollExcelExportService;
import com.mondragon.payroll.service.PayrollService;
import com.mondragon.payroll.service.PayslipPdfService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payrolls")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;
    private final PayslipPdfService payslipPdfService;
    private final PayrollExcelExportService payrollExcelExportService;

    @GetMapping
    public List<PayrollDto> findAll() {
        return payrollService.findAll();
    }

    @GetMapping("/archives")
    public List<PayrollArchiveDto> findArchives() {
        return payrollService.findArchives();
    }

    @GetMapping("/preview")
    public List<PayrollPreviewItemDto> preview(
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate periodStart,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate periodEnd) {
        return payrollService.preview(periodStart, periodEnd);
    }

    @GetMapping("/archives/{id}")
    public PayrollArchiveDto findArchive(@PathVariable Long id) {
        return payrollService.findArchiveById(id);
    }

    @GetMapping("/{id}")
    public PayrollDto findById(@PathVariable Long id) {
        return payrollService.findById(id);
    }

    @GetMapping("/{id}/print")
    public PayrollDto print(@PathVariable Long id) {
        return payrollService.getPrintData(id);
    }

    @GetMapping(value = "/{id}/payslips", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadPayslips(@PathVariable Long id) {
        PayrollDto payroll = payrollService.getPrintData(id);
        byte[] pdf = payslipPdfService.generatePayslips(payroll);
        String filename = "payslips-" + payroll.getPayrollNumber() + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping(value = "/{id}/export.xlsx")
    public ResponseEntity<byte[]> exportExcel(@PathVariable Long id) {
        PayrollDto payroll = payrollService.getPrintData(id);
        byte[] excel = payrollExcelExportService.export(id);
        String filename = "payroll-" + payroll.getPayrollNumber() + ".xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    @PostMapping("/generate")
    public PayrollDto generate(@Valid @RequestBody GeneratePayrollRequest request) {
        return payrollService.generate(request);
    }

    @PostMapping("/{id}/finalize")
    public PayrollDto finalizePayroll(@PathVariable Long id) {
        return payrollService.finalizePayroll(id);
    }

    @PostMapping("/{id}/archive")
    public PayrollArchiveDto archive(@PathVariable Long id) {
        return payrollService.archive(id);
    }

    @DeleteMapping("/{id}")
    public ApiMessage delete(@PathVariable Long id) {
        payrollService.deleteDraft(id);
        return new ApiMessage("Draft payroll deleted");
    }
}
