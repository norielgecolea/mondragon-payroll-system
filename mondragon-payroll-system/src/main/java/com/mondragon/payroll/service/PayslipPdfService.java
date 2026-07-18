package com.mondragon.payroll.service;

import com.mondragon.payroll.dto.PayrollDto;
import com.mondragon.payroll.dto.PayrollItemDto;
import com.mondragon.payroll.exception.BusinessException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class PayslipPdfService {

    private static final String TEMPLATE_PATH = "templates/mondragon-payslip.pdf";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);

    /** Template uses top-left style layout; PDFBox y is from bottom. */
    private static final float PAGE_HEIGHT = 842.25f;

    private final byte[] templateBytes;
    private final PDType1Font fontRegular;
    private final PDType1Font fontBold;
    private final DecimalFormat moneyFormat;

    public PayslipPdfService() throws IOException {
        try (InputStream in = new ClassPathResource(TEMPLATE_PATH).getInputStream()) {
            this.templateBytes = in.readAllBytes();
        }
        this.fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        this.fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        this.moneyFormat = new DecimalFormat("#,##0.00", symbols);
    }

    public byte[] generatePayslips(PayrollDto payroll) {
        if (payroll.getItems() == null || payroll.getItems().isEmpty()) {
            throw new BusinessException("Payroll has no employee items to print");
        }
        try {
            List<byte[]> pagePdfs = new ArrayList<>();
            for (PayrollItemDto item : payroll.getItems()) {
                pagePdfs.add(fillOne(payroll, item));
            }
            ByteArrayOutputStream merged = new ByteArrayOutputStream();
            PDFMergerUtility merger = new PDFMergerUtility();
            for (byte[] pagePdf : pagePdfs) {
                merger.addSource(new RandomAccessReadBuffer(pagePdf));
            }
            merger.setDestinationStream(merged);
            merger.mergeDocuments(null);
            return merged.toByteArray();
        } catch (IOException e) {
            throw new BusinessException("Failed to generate payslip PDF: " + e.getMessage());
        }
    }

    private byte[] fillOne(PayrollDto payroll, PayrollItemDto item) throws IOException {
        try (PDDocument doc = Loader.loadPDF(new RandomAccessReadBuffer(templateBytes));
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = doc.getPage(0);
            PDRectangle box = page.getMediaBox();
            float pageH = box.getHeight() > 0 ? box.getHeight() : PAGE_HEIGHT;

            try (PDPageContentStream cs = new PDPageContentStream(doc, page, AppendMode.APPEND, true, true)) {
                writeText(cs, fontRegular, 11, 175, y(pageH, 185), nullSafe(item.getEmployeeName()));
                writeText(cs, fontRegular, 11, 175, y(pageH, 207), nullSafe(item.getEmployeeCode()));
                writeText(cs, fontRegular, 11, 175, y(pageH, 230), nullSafe(item.getPositionTitle()));

                String period = formatDate(payroll.getPeriodStart()) + " – " + formatDate(payroll.getPeriodEnd());
                writeText(cs, fontRegular, 10, 458, y(pageH, 185), period);
                LocalDate payDate = payroll.getPeriodEnd() != null ? payroll.getPeriodEnd() : LocalDate.now();
                writeText(cs, fontRegular, 11, 458, y(pageH, 207), formatDate(payDate));

                List<String[]> earnings = new ArrayList<>();
                earnings.add(new String[]{"Basic Pay", money(item.getBasicPay())});
                addLine(earnings, "Overtime Pay", item.getOvertimePay());
                addLine(earnings, "Bonus", item.getBonus());
                float ey = 330;
                for (String[] row : earnings) {
                    writeDetailRow(cs, fontRegular, 11, 70, 520, y(pageH, ey), row[0], row[1]);
                    ey += 22;
                }
                writeRight(cs, fontBold, 12, 520, y(pageH, 458), money(item.getGrossPay()));

                List<String[]> deductions = new ArrayList<>();
                addLine(deductions, "Cash Advance", item.getCashAdvanceDeduction());
                addLine(deductions, "Savings", item.getSavingsDeduction());
                addLine(deductions, "SSS", item.getSssDeduction());
                addLine(deductions, "PhilHealth", item.getPhilhealthDeduction());
                float dy = 560;
                if (deductions.isEmpty()) {
                    writeText(cs, fontRegular, 11, 70, y(pageH, dy), "None");
                } else {
                    for (String[] row : deductions) {
                        writeDetailRow(cs, fontRegular, 11, 70, 520, y(pageH, dy), row[0], row[1]);
                        dy += 22;
                    }
                }
                writeText(cs, fontBold, 13, 300, y(pageH, 668), money(item.getNetPay()));
            }

            doc.save(out);
            return out.toByteArray();
        }
    }

    private static float y(float pageH, float topY) {
        return pageH - topY;
    }

    private void addLine(List<String[]> rows, String label, BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            rows.add(new String[]{label, money(amount)});
        }
    }

    private String money(BigDecimal amount) {
        return moneyFormat.format(amount != null ? amount : BigDecimal.ZERO);
    }

    private static String formatDate(LocalDate date) {
        return date == null ? "" : DATE_FMT.format(date);
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }

    /** Label, dashed spacer, then right-aligned amount. */
    private void writeDetailRow(PDPageContentStream cs, PDType1Font font, float size,
                                float leftX, float rightX, float y,
                                String label, String amount) throws IOException {
        String safeLabel = sanitize(label);
        String safeAmount = sanitize(amount);
        float labelWidth = font.getStringWidth(safeLabel) / 1000f * size;
        float amountWidth = font.getStringWidth(safeAmount) / 1000f * size;
        float amountX = rightX - amountWidth;

        writeText(cs, font, size, leftX, y, safeLabel);

        float gapStart = leftX + labelWidth + 6;
        float gapEnd = amountX - 6;
        if (gapEnd > gapStart) {
            float dashWidth = font.getStringWidth("-") / 1000f * size;
            if (dashWidth > 0) {
                int count = Math.max(0, (int) ((gapEnd - gapStart) / dashWidth));
                if (count > 0) {
                    writeText(cs, font, size, gapStart, y, "-".repeat(count));
                }
            }
        }

        writeText(cs, font, size, amountX, y, safeAmount);
    }

    private void writeText(PDPageContentStream cs, PDType1Font font, float size,
                           float x, float y, String text) throws IOException {
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(sanitize(text));
        cs.endText();
    }

    private void writeRight(PDPageContentStream cs, PDType1Font font, float size,
                            float rightX, float y, String text) throws IOException {
        String safe = sanitize(text);
        float width = font.getStringWidth(safe) / 1000f * size;
        writeText(cs, font, size, rightX - width, y, safe);
    }

    /** WinAnsi-safe text for Standard 14 fonts. */
    private static String sanitize(String text) {
        if (text == null) return "";
        StringBuilder sb = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            if (c == '–' || c == '—' || c == '−') {
                sb.append('-');
            } else if (c == '₱') {
                sb.append("PHP ");
            } else if (c <= 255) {
                sb.append(c);
            } else {
                sb.append('?');
            }
        }
        return sb.toString();
    }
}
