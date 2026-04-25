package com.next2me.next2view.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.next2me.next2view.dto.ReportDataDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PdfReportGenerator {

    // ── Brand Colors ──
    private static final Color NAVY = new Color(0x0f, 0x17, 0x2a);
    private static final Color NAVY_LIGHT = new Color(0x1e, 0x29, 0x3b);
    private static final Color ACCENT = new Color(0x3b, 0x82, 0xf6);
    private static final Color GREEN = new Color(0x05, 0x96, 0x69);
    private static final Color RED = new Color(0xdc, 0x26, 0x26);
    private static final Color YELLOW = new Color(0xd9, 0x77, 0x06);
    private static final Color LIGHT_BG = new Color(0xf7, 0xf8, 0xfb);
    private static final Color BORDER_COLOR = new Color(0xdf, 0xe1, 0xe6);
    private static final Color TEXT_PRIMARY = new Color(0x1a, 0x20, 0x2c);
    private static final Color TEXT_DIM = new Color(0x71, 0x80, 0x96);
    private static final Color WHITE = Color.WHITE;

    // ── Fonts ──
    private Font titleFont;
    private Font headerFont;
    private Font subHeaderFont;
    private Font bodyFont;
    private Font bodyBoldFont;
    private Font smallFont;
    private Font smallBoldFont;
    private Font footerFont;
    private Font whiteFont;
    private Font whiteBoldFont;
    private Font accentFont;

    private void initFonts() {
        titleFont = new Font(Font.HELVETICA, 22, Font.BOLD, NAVY);
        headerFont = new Font(Font.HELVETICA, 14, Font.BOLD, NAVY);
        subHeaderFont = new Font(Font.HELVETICA, 11, Font.BOLD, TEXT_PRIMARY);
        bodyFont = new Font(Font.HELVETICA, 9, Font.NORMAL, TEXT_PRIMARY);
        bodyBoldFont = new Font(Font.HELVETICA, 9, Font.BOLD, TEXT_PRIMARY);
        smallFont = new Font(Font.HELVETICA, 7.5f, Font.NORMAL, TEXT_DIM);
        smallBoldFont = new Font(Font.HELVETICA, 7.5f, Font.BOLD, TEXT_DIM);
        footerFont = new Font(Font.HELVETICA, 7, Font.NORMAL, TEXT_DIM);
        whiteFont = new Font(Font.HELVETICA, 9, Font.NORMAL, WHITE);
        whiteBoldFont = new Font(Font.HELVETICA, 12, Font.BOLD, WHITE);
        accentFont = new Font(Font.HELVETICA, 9, Font.BOLD, ACCENT);
    }

    // ══════════════════════════════════════════
    // PUBLIC: Generate branded PDF
    // ══════════════════════════════════════════
    public byte[] generatePdf(ReportDataDTO data) {
        initFonts();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 40, 40, 40, 60);
            PdfWriter writer = PdfWriter.getInstance(doc, baos);
            writer.setPageEvent(new BrandedFooter());
            doc.open();

            // Header band
            addHeaderBand(writer, doc, data);

            // Summary cards
            if (data.getSummary() != null) {
                addSummaryCards(doc, data);
            }

            // Sections
            if (data.getSections() != null) {
                for (Map<String, Object> section : data.getSections()) {
                    addSection(doc, section);
                }
            }

            // Signature block
            addSignatureBlock(doc);

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("PDF generation failed", e);
            throw new RuntimeException("PDF generation failed: " + e.getMessage(), e);
        }
    }

    // ══════════════════════════════════════════
    // HEADER BAND — Navy gradient with logo
    // ══════════════════════════════════════════
    private void addHeaderBand(PdfWriter writer, Document doc, ReportDataDTO data) throws DocumentException {
        PdfContentByte cb = writer.getDirectContent();
        float pageWidth = doc.getPageSize().getWidth();
        float top = doc.getPageSize().getHeight() - 15;

        // Navy band
        cb.setColorFill(NAVY);
        cb.rectangle(0, top - 80, pageWidth, 95);
        cb.fill();

        // Accent stripe at bottom of band
        cb.setColorFill(ACCENT);
        cb.rectangle(0, top - 83, pageWidth, 3);
        cb.fill();

        // Logo text
        cb.beginText();
        cb.setFontAndSize(BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, false), 10);
        cb.setColorFill(new Color(0xa8, 0xb0, 0xc4));
        cb.showTextAligned(Element.ALIGN_LEFT, "NEXT2ME GROUP", 45, top - 22, 0);
        cb.endText();

        cb.beginText();
        cb.setFontAndSize(BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, false), 20);
        cb.setColorFill(WHITE);
        cb.showTextAligned(Element.ALIGN_LEFT, "Next2View", 45, top - 45, 0);
        cb.endText();

        // Report title (right aligned)
        cb.beginText();
        cb.setFontAndSize(BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, false), 14);
        cb.setColorFill(WHITE);
        cb.showTextAligned(Element.ALIGN_RIGHT, data.getTemplateName(), pageWidth - 45, top - 30, 0);
        cb.endText();

        // Date
        String dateStr = data.getGeneratedAt() != null
            ? data.getGeneratedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            : LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        cb.beginText();
        cb.setFontAndSize(BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, false), 9);
        cb.setColorFill(new Color(0xa8, 0xb0, 0xc4));
        cb.showTextAligned(Element.ALIGN_RIGHT, "Generated: " + dateStr, pageWidth - 45, top - 50, 0);
        cb.endText();

        // Confidential badge
        cb.beginText();
        cb.setFontAndSize(BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, false), 7);
        cb.setColorFill(YELLOW);
        cb.showTextAligned(Element.ALIGN_RIGHT, "PRIVATE & CONFIDENTIAL", pageWidth - 45, top - 66, 0);
        cb.endText();

        // Add spacing after header
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph(" "));
    }

    // ══════════════════════════════════════════
    // SUMMARY CARDS — KPI grid
    // ══════════════════════════════════════════
    private void addSummaryCards(Document doc, ReportDataDTO data) throws DocumentException {
        Map<String, Object> summary = data.getSummary();

        // Select key metrics for the card grid
        String[][] cardDefs = selectCardDefinitions(data.getTemplateId(), summary);

        if (cardDefs.length == 0) return;

        int cols = Math.min(cardDefs.length, 3);
        PdfPTable grid = new PdfPTable(cols);
        grid.setWidthPercentage(100);
        grid.setSpacingBefore(10);
        grid.setSpacingAfter(15);

        for (String[] card : cardDefs) {
            PdfPCell cell = createKpiCard(card[0], card[1], card[2]);
            grid.addCell(cell);
        }

        // Fill remaining cells if needed
        int remaining = cols - (cardDefs.length % cols);
        if (remaining < cols) {
            for (int i = 0; i < remaining; i++) {
                PdfPCell empty = new PdfPCell();
                empty.setBorder(0);
                grid.addCell(empty);
            }
        }

        doc.add(grid);
    }

    private String[][] selectCardDefinitions(String templateId, Map<String, Object> summary) {
        if ("security-snapshot".equals(templateId)) {
            return new String[][]{
                {"Total Users", String.valueOf(summary.getOrDefault("totalUsers", "—")), "neutral"},
                {"MFA Enabled", String.valueOf(summary.getOrDefault("mfaEnabled", "—")), "green"},
                {"MFA Pending", String.valueOf(summary.getOrDefault("mfaPending", "—")), "red"},
                {"MFA Coverage", summary.getOrDefault("mfaCoveragePercent", "—") + "%", getPercentColor(summary.get("mfaCoveragePercent"))},
                {"Encryption", String.valueOf(summary.getOrDefault("encryptionAlgorithm", "—")), "accent"},
                {"Key Management", String.valueOf(summary.getOrDefault("keyManagement", "—")), "accent"},
            };
        } else if ("mfa-status".equals(templateId)) {
            return new String[][]{
                {"Total Users", String.valueOf(summary.getOrDefault("totalUsers", "—")), "neutral"},
                {"MFA Enabled", String.valueOf(summary.getOrDefault("mfaEnabled", "—")), "green"},
                {"MFA Pending", String.valueOf(summary.getOrDefault("mfaPending", "—")), "red"},
                {"Compliance", summary.getOrDefault("compliancePercent", "—") + "%", getPercentColor(summary.get("compliancePercent"))},
            };
        }
        return new String[0][];
    }

    private String getPercentColor(Object val) {
        if (val instanceof Number) {
            double v = ((Number) val).doubleValue();
            if (v >= 100) return "green";
            if (v >= 50) return "yellow";
            return "red";
        }
        return "neutral";
    }

    private PdfPCell createKpiCard(String label, String value, String colorType) {
        Color bgColor = LIGHT_BG;
        Color valueColor = TEXT_PRIMARY;
        if ("green".equals(colorType)) valueColor = GREEN;
        else if ("red".equals(colorType)) valueColor = RED;
        else if ("yellow".equals(colorType)) valueColor = YELLOW;
        else if ("accent".equals(colorType)) valueColor = ACCENT;

        PdfPCell cell = new PdfPCell();
        cell.setBorder(0);
        cell.setPadding(12);
        cell.setPaddingBottom(14);

        // Inner table for card layout
        PdfPTable inner = new PdfPTable(1);
        inner.setWidthPercentage(100);

        // Label
        PdfPCell labelCell = new PdfPCell(new Phrase(label.toUpperCase(), smallBoldFont));
        labelCell.setBorder(0);
        labelCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        labelCell.setPaddingBottom(4);
        inner.addCell(labelCell);

        // Value
        Font valFont = new Font(Font.HELVETICA, 16, Font.BOLD, valueColor);
        PdfPCell valCell = new PdfPCell(new Phrase(value, valFont));
        valCell.setBorder(0);
        valCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        inner.addCell(valCell);

        cell.addElement(inner);
        cell.setBackgroundColor(bgColor);
        cell.setBorderWidth(0.5f);
        cell.setBorderColor(BORDER_COLOR);
        cell.setUseAscender(true);
        cell.setUseDescender(true);
        return cell;
    }

    // ══════════════════════════════════════════
    // SECTIONS — Checklists, tables, action items
    // ══════════════════════════════════════════
    private void addSection(Document doc, Map<String, Object> section) throws DocumentException {
        String title = (String) section.getOrDefault("title", "Section");
        String status = (String) section.get("status");

        // Section header with status badge
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{70, 30});
        headerTable.setSpacingBefore(12);

        PdfPCell titleCell = new PdfPCell(new Phrase(title, headerFont));
        titleCell.setBorder(0);
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        titleCell.setPaddingBottom(8);
        headerTable.addCell(titleCell);

        if (status != null) {
            Font statusFont = new Font(Font.HELVETICA, 8, Font.BOLD,
                "COMPLIANT".equals(status) ? GREEN : YELLOW);
            String statusText = "COMPLIANT".equals(status) ? "✓ Compliant" : "⚠ Action Required";
            PdfPCell statusCell = new PdfPCell(new Phrase(statusText, statusFont));
            statusCell.setBorder(0);
            statusCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            statusCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            statusCell.setPaddingBottom(8);
            headerTable.addCell(statusCell);
        } else {
            PdfPCell empty = new PdfPCell();
            empty.setBorder(0);
            headerTable.addCell(empty);
        }

        doc.add(headerTable);

        // Divider line
        PdfPTable divider = new PdfPTable(1);
        divider.setWidthPercentage(100);
        PdfPCell divCell = new PdfPCell();
        divCell.setBorderWidthTop(1.5f);
        divCell.setBorderColorTop(ACCENT);
        divCell.setBorderWidthBottom(0);
        divCell.setBorderWidthLeft(0);
        divCell.setBorderWidthRight(0);
        divCell.setFixedHeight(2);
        divider.addCell(divCell);
        doc.add(divider);

        // Checklist items
        Object items = section.get("items");
        if (items instanceof List<?> itemList && !itemList.isEmpty()) {
            Object first = itemList.get(0);
            if (first instanceof Map<?, ?> firstMap) {
                if (firstMap.containsKey("check")) {
                    addChecklist(doc, itemList);
                } else if (firstMap.containsKey("action")) {
                    addActionItems(doc, itemList);
                }
            }
        }

        // User table
        Object users = section.get("users");
        if (users instanceof List<?> userList && !userList.isEmpty()) {
            addUserTable(doc, userList);
        }
    }

    // ── Checklist with color icons ──
    private void addChecklist(Document doc, List<?> items) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{4, 96});
        table.setSpacingBefore(6);

        for (Object item : items) {
            if (item instanceof Map<?, ?> map) {
                boolean ok = Boolean.TRUE.equals(map.get("status"));
                String check = String.valueOf(map.getOrDefault("check", ""));

                // Icon
                Font iconFont = new Font(Font.HELVETICA, 10, Font.BOLD, ok ? GREEN : RED);
                PdfPCell iconCell = new PdfPCell(new Phrase(ok ? "✓" : "✗", iconFont));
                iconCell.setBorder(0);
                iconCell.setPadding(5);
                iconCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.addCell(iconCell);

                // Text
                PdfPCell textCell = new PdfPCell(new Phrase(check, bodyFont));
                textCell.setBorder(0);
                textCell.setPadding(5);
                textCell.setBorderWidthBottom(0.5f);
                textCell.setBorderColorBottom(BORDER_COLOR);
                table.addCell(textCell);
            }
        }

        doc.add(table);
    }

    // ── Action Items with priority badges ──
    private void addActionItems(Document doc, List<?> items) throws DocumentException {
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{15, 25, 60});
        table.setSpacingBefore(6);

        for (Object item : items) {
            if (item instanceof Map<?, ?> map) {
                String priority = String.valueOf(map.getOrDefault("priority", ""));
                String user = String.valueOf(map.getOrDefault("user", ""));
                String action = String.valueOf(map.getOrDefault("action", ""));

                // Priority badge
                Color badgeColor = "CRITICAL".equals(priority) ? RED : "HIGH".equals(priority) ? YELLOW : ACCENT;
                Font badgeFont = new Font(Font.HELVETICA, 7, Font.BOLD, WHITE);
                PdfPCell badgeCell = new PdfPCell(new Phrase(priority, badgeFont));
                badgeCell.setBackgroundColor(badgeColor);
                badgeCell.setBorder(0);
                badgeCell.setPadding(4);
                badgeCell.setPaddingLeft(8);
                badgeCell.setPaddingRight(8);
                badgeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                badgeCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.addCell(badgeCell);

                // User
                PdfPCell userCell = new PdfPCell(new Phrase(user, bodyBoldFont));
                userCell.setBorder(0);
                userCell.setPadding(4);
                userCell.setBorderWidthBottom(0.5f);
                userCell.setBorderColorBottom(BORDER_COLOR);
                table.addCell(userCell);

                // Action
                PdfPCell actionCell = new PdfPCell(new Phrase(action, bodyFont));
                actionCell.setBorder(0);
                actionCell.setPadding(4);
                actionCell.setBorderWidthBottom(0.5f);
                actionCell.setBorderColorBottom(BORDER_COLOR);
                table.addCell(actionCell);
            }
        }

        doc.add(table);
    }

    // ── User MFA Table — colored header, role badges ──
    private void addUserTable(Document doc, List<?> users) throws DocumentException {
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{22, 14, 30, 14, 20});
        table.setSpacingBefore(8);

        // Header row
        String[] headers = {"USER", "ROLE", "COMPANY", "MFA", "ACTION"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, new Font(Font.HELVETICA, 7, Font.BOLD, WHITE)));
            cell.setBackgroundColor(NAVY);
            cell.setPadding(7);
            cell.setBorder(0);
            table.addCell(cell);
        }

        // Data rows
        boolean alternate = false;
        for (Object user : users) {
            if (user instanceof Map<?, ?> map) {
                Color rowBg = alternate ? LIGHT_BG : WHITE;
                String fullName = String.valueOf(map.getOrDefault("fullName", map.getOrDefault("username", "—")));
                String role = String.valueOf(map.getOrDefault("role", "—"));
                String company = String.valueOf(map.getOrDefault("company", "—"));
                boolean mfaEnabled = Boolean.TRUE.equals(map.get("mfaEnabled"));
                boolean actionRequired = Boolean.TRUE.equals(map.get("actionRequired"));

                // Name
                addTableCell(table, fullName, bodyBoldFont, rowBg, Element.ALIGN_LEFT);

                // Role badge
                Color roleBg = "CEO".equals(role) ? ACCENT : "DEPT_HEAD".equals(role) ? GREEN : TEXT_DIM;
                Font roleFont = new Font(Font.HELVETICA, 7, Font.BOLD, WHITE);
                PdfPCell roleCell = new PdfPCell();
                roleCell.setBackgroundColor(rowBg);
                roleCell.setBorder(0);
                roleCell.setPadding(5);
                roleCell.setBorderWidthBottom(0.5f);
                roleCell.setBorderColorBottom(BORDER_COLOR);

                PdfPTable roleBadge = new PdfPTable(1);
                PdfPCell badgeInner = new PdfPCell(new Phrase(role, roleFont));
                badgeInner.setBackgroundColor(roleBg);
                badgeInner.setBorder(0);
                badgeInner.setPadding(2);
                badgeInner.setPaddingLeft(4);
                badgeInner.setPaddingRight(4);
                badgeInner.setHorizontalAlignment(Element.ALIGN_CENTER);
                roleBadge.addCell(badgeInner);
                roleCell.addElement(roleBadge);
                table.addCell(roleCell);

                // Company
                addTableCell(table, company, bodyFont, rowBg, Element.ALIGN_LEFT);

                // MFA status
                Font mfaFont = new Font(Font.HELVETICA, 8, Font.BOLD, mfaEnabled ? GREEN : RED);
                addTableCell(table, mfaEnabled ? "✓ Active" : "✗ Inactive", mfaFont, rowBg, Element.ALIGN_CENTER);

                // Action
                if (actionRequired) {
                    Font actFont = new Font(Font.HELVETICA, 7, Font.BOLD, YELLOW);
                    addTableCell(table, "Needs activation", actFont, rowBg, Element.ALIGN_CENTER);
                } else {
                    addTableCell(table, "—", bodyFont, rowBg, Element.ALIGN_CENTER);
                }

                alternate = !alternate;
            }
        }

        doc.add(table);
    }

    private void addTableCell(PdfPTable table, String text, Font font, Color bg, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setBorder(0);
        cell.setPadding(6);
        cell.setBorderWidthBottom(0.5f);
        cell.setBorderColorBottom(BORDER_COLOR);
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    // ══════════════════════════════════════════
    // SIGNATURE BLOCK
    // ══════════════════════════════════════════
    private void addSignatureBlock(Document doc) throws DocumentException {
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph(" "));

        PdfPTable sigTable = new PdfPTable(2);
        sigTable.setWidthPercentage(100);
        sigTable.setWidths(new float[]{50, 50});

        // Left: Prepared by
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(0);
        leftCell.setPadding(10);
        Paragraph prep = new Paragraph("Prepared by:", smallBoldFont);
        prep.setSpacingAfter(30);
        leftCell.addElement(prep);
        Paragraph line1 = new Paragraph("_______________________________", smallFont);
        leftCell.addElement(line1);
        Paragraph name1 = new Paragraph("Next2View Platform (Automated)", smallFont);
        leftCell.addElement(name1);
        sigTable.addCell(leftCell);

        // Right: Approved by
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(0);
        rightCell.setPadding(10);
        Paragraph appr = new Paragraph("Approved by:", smallBoldFont);
        appr.setSpacingAfter(30);
        rightCell.addElement(appr);
        Paragraph line2 = new Paragraph("_______________________________", smallFont);
        rightCell.addElement(line2);
        Paragraph name2 = new Paragraph("CEO, Next2me Group", smallFont);
        rightCell.addElement(name2);
        sigTable.addCell(rightCell);

        doc.add(sigTable);
    }

    // ══════════════════════════════════════════
    // FOOTER — Page numbers + branding
    // ══════════════════════════════════════════
    private class BrandedFooter extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            float pageWidth = document.getPageSize().getWidth();

            // Footer line
            cb.setColorStroke(BORDER_COLOR);
            cb.setLineWidth(0.5f);
            cb.moveTo(40, 40);
            cb.lineTo(pageWidth - 40, 40);
            cb.stroke();

            // Left: branding
            try {
                cb.beginText();
                cb.setFontAndSize(BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, false), 7);
                cb.setColorFill(TEXT_DIM);
                cb.showTextAligned(Element.ALIGN_LEFT, "Generated by Next2View \u00b7 Next2me Group \u00b7 Private & Confidential", 40, 28, 0);
                cb.endText();
            } catch (Exception ignored) {}

            // Right: page number
            try {
                cb.beginText();
                cb.setFontAndSize(BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, false), 7);
                cb.setColorFill(TEXT_DIM);
                cb.showTextAligned(Element.ALIGN_RIGHT, "Page " + writer.getPageNumber(), pageWidth - 40, 28, 0);
                cb.endText();
            } catch (Exception ignored) {}
        }
    }
}
