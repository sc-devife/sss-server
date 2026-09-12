package com.sss.app.service.escapedocs.impl;

import com.sss.app.service.escapedocs.DocSections;
import com.sss.app.service.escapedocs.EscapeDocsDataService;
import com.sss.app.service.escapedocs.EscapeDocsService;
import com.sss.app.service.escapedocs.EscapeDocumentResult;
import com.sss.app.service.quotationtemplate.QuotationPdfService;
import com.sss.app.service.quotationtemplate.QuotationRenderingService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EscapeDocsServiceImpl implements EscapeDocsService {

    private static final String TEMPLATE = "doc-templates/escape-docs.mustache";
    private static final DateTimeFormatter DAY_DATE = DateTimeFormatter.ofPattern("d MMM yyyy");

    private final EscapeDocsDataService escapeDocsDataService;
    private final QuotationRenderingService quotationRenderingService;
    private final QuotationPdfService quotationPdfService;

    @Override
    public String renderHtml(UUID escapeUid, DocSections sections) {
        Map<String, Object> data = escapeDocsDataService.buildData(escapeUid, sections);
        return quotationRenderingService.renderClasspathTemplate(TEMPLATE, data);
    }

    @Override
    public EscapeDocumentResult renderPdf(UUID escapeUid, DocSections sections) {
        Map<String, Object> data = escapeDocsDataService.buildData(escapeUid, sections);
        String html = quotationRenderingService.renderClasspathTemplate(TEMPLATE, data);
        byte[] pdf = quotationPdfService.render(html, watermarkText(data));
        return new EscapeDocumentResult(pdf, filename(data, "pdf"));
    }

    @Override
    public EscapeDocumentResult renderWord(UUID escapeUid, DocSections sections) {
        Map<String, Object> data = escapeDocsDataService.buildData(escapeUid, sections);
        byte[] docx = buildWordDocument(data, sections);
        return new EscapeDocumentResult(docx, filename(data, "docx"));
    }

    // Same "{tripCode} · {orgName}" convention as QuotationTemplateServiceImpl
    // so a watermark can never say something different from the document
    // it's stamped on.
    @SuppressWarnings("unchecked")
    private String watermarkText(Map<String, Object> data) {
        Object tripCode = data.get("tripCode");
        Object organization = data.get("organization");
        Object orgName = organization instanceof Map ? ((Map<String, Object>) organization).get("name") : null;
        if (tripCode == null && orgName == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        if (tripCode != null) sb.append(tripCode);
        if (tripCode != null && orgName != null) sb.append(" · ");
        if (orgName != null) sb.append(orgName);
        return sb.toString();
    }

    private String filename(Map<String, Object> data, String extension) {
        Object tripCode = data.get("tripCode");
        String base = (tripCode != null ? tripCode.toString() : "Escape") + " - Escape Document";
        String sanitized = base.replaceAll("[\\\\/:*?\"<>|]", "-").trim();
        return (sanitized.isEmpty() ? "escape-document" : sanitized) + "." + extension;
    }

    // ---- Word (.docx) building --------------------------------------------
    // No HTML->docx converter exists in POI, so each section is built
    // directly from the same data map buildData() assembles for the HTML/PDF
    // path — driven by the exact same DocSections flags, so both output
    // formats always agree on which sections are present.

    @SuppressWarnings("unchecked")
    private byte[] buildWordDocument(Map<String, Object> data, DocSections sections) {
        try (XWPFDocument doc = new XWPFDocument()) {
            appendHeading(doc, "Escape Document", 24, true);
            appendGreeting(doc, data);
            appendOverview(doc, data);
            appendHotelSection(doc, (List<Map<String, Object>>) data.get("hotels"));

            if (sections.transports()) {
                appendTransports(doc, (List<Map<String, Object>>) data.get("transportDays"));
            }

            // Always-on, placed immediately before Bank Account regardless of
            // whether Transports rendered above it.
            appendPaymentSchedule(doc, (Map<String, Object>) data.get("paymentSchedule"));

            if (sections.bankAccount() && Boolean.TRUE.equals(data.get("hasBankDetails"))) {
                appendBankAccount(doc, (Map<String, Object>) data.get("bank"));
            }
            if (sections.itinerary()) {
                appendItinerary(doc, (List<Map<String, Object>>) data.get("days"));
            }
            if (sections.inclusionsExclusions()) {
                appendContentSection(doc, "Inclusions", (List<Map<String, Object>>) data.get("inclusions"));
                appendContentSection(doc, "Exclusions", (List<Map<String, Object>>) data.get("exclusions"));
            }
            if (sections.termsAndConditions()) {
                appendContentSection(doc, "Terms & Conditions", (List<Map<String, Object>>) data.get("terms"));
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to build Escape Document (.docx)", e);
        }
    }

    // Always-on, mirrors the template's own greeting paragraph — never gated
    // by a checkbox, same as the Overview/Hotel Section/Payment Schedule
    // blocks below.
    @SuppressWarnings("unchecked")
    private void appendGreeting(XWPFDocument doc, Map<String, Object> data) {
        Map<String, Object> organization = (Map<String, Object>) data.get("organization");
        String orgName = organization != null ? (String) organization.get("name") : "us";
        appendParagraph(doc, "Dear " + asText(data.get("primaryTravellerName")) + ",", false);
        appendParagraph(doc, "Thank you for planning your trip to " + asText(data.get("escapePointNames"))
                + " with " + orgName + "! Please find your escape details below.", false);
    }

    // Hotel/Check-in/Check-out/Accommodation — a compact table distinct from
    // the full day-wise Itinerary section, always shown. The hotel name's
    // own star rating renders as a second line inside the same cell, right
    // below the (unchanged) hotel name.
    private void appendHotelSection(XWPFDocument doc, List<Map<String, Object>> hotels) {
        appendHeading(doc, "Hotel Section", 16, false);
        if (hotels == null || hotels.isEmpty()) {
            appendParagraph(doc, "No hotel bookings on this itinerary.", false);
            return;
        }
        XWPFTable table = doc.createTable(hotels.size() + 1, 4);
        setRow(table, 0, "Hotel", "Check In", "Check Out", "Accommodation");
        for (int i = 0; i < hotels.size(); i++) {
            Map<String, Object> hotel = hotels.get(i);
            XWPFTableRow row = table.getRow(i + 1);
            setHotelNameCell(row.getCell(0), asText(hotel.get("hotelName")), hotel.get("stars"));
            setCellText(row.getCell(1), asText(hotel.get("checkIn")));
            setCellText(row.getCell(2), asText(hotel.get("checkOut")));
            setCellText(row.getCell(3), asText(hotel.get("accommodation")));
        }
    }

    // Total Price/Per Person as a side-by-side pair, then Amount Received on
    // its own, then Due Amount/Due Date as a second pair — always shown,
    // placed immediately before Bank Account.
    private void appendPaymentSchedule(XWPFDocument doc, Map<String, Object> schedule) {
        appendHeading(doc, "Payment Schedule", 16, false);
        if (schedule == null) {
            return;
        }
        XWPFTable topRow = doc.createTable(1, 2);
        setStatCell(topRow.getRow(0).getCell(0), "Total Price (incl. tax)", "₹" + asText(schedule.get("totalFormatted")));
        setStatCell(topRow.getRow(0).getCell(1), "Per Person", "₹" + asText(schedule.get("perPaxFormatted")));

        appendLabelValue(doc, "Amount Received", "₹" + asText(schedule.get("amountReceivedFormatted")));

        XWPFTable bottomRow = doc.createTable(1, 2);
        setStatCell(bottomRow.getRow(0).getCell(0), "Due Amount", "₹" + asText(schedule.get("dueAmountFormatted")));
        String dueDate = Boolean.TRUE.equals(schedule.get("hasDueDate")) ? asText(schedule.get("dueDateFormatted")) : "—";
        setStatCell(bottomRow.getRow(0).getCell(1), "Due Date (Time Limit)", dueDate);
    }

    private void setRow(XWPFTable table, int rowIndex, String... values) {
        XWPFTableRow row = table.getRow(rowIndex);
        for (int i = 0; i < values.length; i++) {
            XWPFRun run = row.getCell(i).getParagraphs().get(0).createRun();
            run.setText(values[i]);
            run.setBold(rowIndex == 0);
            run.setFontSize(11);
        }
    }

    private void setCellText(XWPFTableCell cell, String text) {
        XWPFRun run = cell.getParagraphs().get(0).createRun();
        run.setText(text);
        run.setFontSize(11);
    }

    private void setHotelNameCell(XWPFTableCell cell, String name, Object stars) {
        XWPFRun nameRun = cell.getParagraphs().get(0).createRun();
        nameRun.setText(name);
        nameRun.setBold(true);
        nameRun.setFontSize(11);
        if (stars instanceof Integer starCount && starCount > 0) {
            XWPFParagraph starsParagraph = cell.addParagraph();
            XWPFRun starsRun = starsParagraph.createRun();
            starsRun.setText("★".repeat(starCount));
            starsRun.setFontSize(10);
            starsRun.setColor("B45309");
        }
    }

    // A small label-over-value tile, laid out two-per-row via a borderless
    // table cell — mirrors the HTML template's .payment-stat/.overview-grid
    // "label on top, value below" pattern.
    private void setStatCell(XWPFTableCell cell, String label, String value) {
        XWPFRun labelRun = cell.getParagraphs().get(0).createRun();
        labelRun.setText(label);
        labelRun.setBold(true);
        labelRun.setFontSize(9);
        labelRun.setColor("6B7280");
        XWPFParagraph valueParagraph = cell.addParagraph();
        XWPFRun valueRun = valueParagraph.createRun();
        valueRun.setText(value);
        valueRun.setBold(true);
        valueRun.setFontSize(13);
    }

    @SuppressWarnings("unchecked")
    private void appendOverview(XWPFDocument doc, Map<String, Object> data) {
        appendHeading(doc, "Package / Escape Overview", 16, false);

        List<Map<String, Object>> escapePoints = (List<Map<String, Object>>) data.get("escapePoints");
        String escapePointNames = escapePoints == null || escapePoints.isEmpty()
                ? "—"
                : escapePoints.stream().map(ep -> String.valueOf(ep.get("name"))).reduce((a, b) -> a + ", " + b).orElse("—");

        Map<String, Object> organization = (Map<String, Object>) data.get("organization");
        String orgName = organization != null ? (String) organization.get("name") : null;
        String orgEmail = organization != null ? (String) organization.get("contactEmail") : null;
        String orgPhone = organization != null ? (String) organization.get("contactPhone") : null;

        appendLabelValue(doc, "Escape ID", asText(data.get("tripCode")));
        appendLabelValue(doc, "Escape Point", escapePointNames);
        appendLabelValue(doc, "Start Date", formatDate(data.get("startDate")));
        appendLabelValue(doc, "End Date", formatDate(data.get("endDate")));
        appendLabelValue(doc, "Duration", data.get("numberOfDays") != null ? data.get("numberOfDays") + " days" : "—");
        appendLabelValue(doc, "Primary Traveller", asText(data.get("primaryTravellerName")));
        appendLabelValue(doc, "Number of Travellers", asText(data.get("travellersCount")));
        appendLabelValue(doc, "Organization", orgName != null ? orgName : "—");
        if (orgEmail != null || orgPhone != null) {
            String contact = String.join(" · ", java.util.stream.Stream.of(orgEmail, orgPhone).filter(java.util.Objects::nonNull).toList());
            appendLabelValue(doc, "Organization Contact", contact);
        }
    }

    @SuppressWarnings("unchecked")
    private void appendTransports(XWPFDocument doc, List<Map<String, Object>> transportDays) {
        appendHeading(doc, "Transports", 16, false);
        if (transportDays == null || transportDays.isEmpty()) {
            appendParagraph(doc, "No transport bookings on this itinerary.", false);
            return;
        }
        for (Map<String, Object> day : transportDays) {
            appendHeading(doc, "Day " + day.get("dayNumber") + (day.get("date") != null ? " — " + formatDate(day.get("date")) : ""), 13, false);
            List<Map<String, Object>> items = (List<Map<String, Object>>) day.get("items");
            for (Map<String, Object> item : items) {
                Map<String, Object> transport = (Map<String, Object>) item.get("transport");
                if (transport == null) continue;
                String title = asText(item.get("title"));
                appendBullet(doc, title + " (" + asText(transport.get("modeCode")) + ")");
                List<Map<String, Object>> legs = (List<Map<String, Object>>) transport.get("legs");
                if (legs != null) {
                    for (Map<String, Object> leg : legs) {
                        String legLine = asText(leg.get("departureAirport")) + " → " + asText(leg.get("arrivalAirport"))
                                + (leg.get("flightNumber") != null ? " (" + leg.get("flightNumber") + ")" : "");
                        appendBullet(doc, legLine, 1);
                    }
                }
            }
        }
    }

    // 2-column-pair layout: each row is label/value/label/value (e.g.
    // "Account Name … Account Number …" on one row).
    private void appendBankAccount(XWPFDocument doc, Map<String, Object> bank) {
        appendHeading(doc, "Bank Account", 16, false);
        if (bank == null) {
            appendParagraph(doc, "No bank account configured for this organization.", false);
            return;
        }
        XWPFTable table = doc.createTable(3, 4);
        setPairRow(table, 0, "Account Name", asText(bank.get("accountName")), "Account Number", asText(bank.get("accountNumber")));
        setPairRow(table, 1, "Bank Name", asText(bank.get("bankName")), "Branch", asText(bank.get("branchName")));
        setPairRow(table, 2, "IFSC", asText(bank.get("ifsc")), "SWIFT Code", asText(bank.get("swiftCode")));
    }

    private void setPairRow(XWPFTable table, int rowIndex, String label1, String value1, String label2, String value2) {
        XWPFTableRow row = table.getRow(rowIndex);
        setLabelCell(row.getCell(0), label1);
        setCellText(row.getCell(1), value1);
        setLabelCell(row.getCell(2), label2);
        setCellText(row.getCell(3), value2);
    }

    private void setLabelCell(XWPFTableCell cell, String label) {
        XWPFRun run = cell.getParagraphs().get(0).createRun();
        run.setText(label);
        run.setBold(true);
        run.setFontSize(10);
        run.setColor("6B7280");
    }

    @SuppressWarnings("unchecked")
    private void appendItinerary(XWPFDocument doc, List<Map<String, Object>> days) {
        appendHeading(doc, "Itinerary", 16, false);
        if (days == null || days.isEmpty()) {
            appendParagraph(doc, "No itinerary items yet.", false);
            return;
        }
        for (Map<String, Object> day : days) {
            appendHeading(doc, "Day " + day.get("dayNumber") + (day.get("date") != null ? " — " + formatDate(day.get("date")) : ""), 13, false);
            List<Map<String, Object>> items = (List<Map<String, Object>>) day.get("items");
            for (Map<String, Object> item : items) {
                StringBuilder line = new StringBuilder(asText(item.get("title")));
                Map<String, Object> hotel = (Map<String, Object>) item.get("hotel");
                Map<String, Object> transport = (Map<String, Object>) item.get("transport");
                Map<String, Object> activity = (Map<String, Object>) item.get("activity");
                if (hotel != null) {
                    line.append(" — ").append(hotel.get("nights")).append(" night(s)");
                    if (hotel.get("roomTypeName") != null) line.append(", ").append(hotel.get("roomTypeName"));
                } else if (transport != null) {
                    line.append(" — ").append(asText(transport.get("modeCode")));
                } else if (activity != null && activity.get("durationMinutes") != null) {
                    line.append(" — ").append(activity.get("durationMinutes")).append(" min");
                }
                appendBullet(doc, line.toString());
                if (item.get("notes") != null && !String.valueOf(item.get("notes")).isBlank()) {
                    appendBullet(doc, String.valueOf(item.get("notes")), 1);
                }
            }
        }
    }

    private void appendContentSection(XWPFDocument doc, String heading, List<Map<String, Object>> items) {
        appendHeading(doc, heading, 16, false);
        if (items == null || items.isEmpty()) {
            appendParagraph(doc, "None added.", false);
            return;
        }
        for (Map<String, Object> item : items) {
            appendHeading(doc, asText(item.get("name")), 12, false);
            appendHtmlContent(doc, (String) item.get("contentHtml"));
        }
    }

    // Walks the sanitized HTML (same allowlist as RichTextSanitizer: p, br,
    // strong/b, em/i, u, ul, ol, li) into paragraphs/runs — POI has no
    // HTML importer, so this is a deliberately small, purpose-built
    // converter rather than a general one.
    private void appendHtmlContent(XWPFDocument doc, String html) {
        if (html == null || html.isBlank()) {
            return;
        }
        Element body = Jsoup.parseBodyFragment(html).body();
        for (Element child : body.children()) {
            switch (child.tagName()) {
                case "p" -> appendRichParagraph(doc.createParagraph(), child);
                case "ul", "ol" -> {
                    int index = 1;
                    for (Element li : child.children()) {
                        XWPFParagraph p = doc.createParagraph();
                        p.setIndentationLeft(360);
                        XWPFRun marker = p.createRun();
                        marker.setText("ul".equals(child.tagName()) ? "• " : (index++) + ". ");
                        appendRichParagraph(p, li);
                    }
                }
                default -> appendRichParagraph(doc.createParagraph(), child);
            }
        }
    }

    // Renders one element's inline content (text/strong/b/em/i/u/br) as runs
    // on an already-created paragraph, preserving bold/italic/underline.
    private void appendRichParagraph(XWPFParagraph paragraph, Element element) {
        for (Node node : element.childNodes()) {
            appendInlineNode(paragraph, node, false, false, false);
        }
    }

    private void appendInlineNode(XWPFParagraph paragraph, Node node, boolean bold, boolean italic, boolean underline) {
        if (node instanceof TextNode text) {
            String value = text.text();
            if (value.isBlank()) return;
            XWPFRun run = paragraph.createRun();
            run.setText(value);
            run.setBold(bold);
            run.setItalic(italic);
            if (underline) run.setUnderline(UnderlinePatterns.SINGLE);
            return;
        }
        if (node instanceof Element el) {
            if ("br".equals(el.tagName())) {
                paragraph.createRun().addBreak();
                return;
            }
            boolean nextBold = bold || "strong".equals(el.tagName()) || "b".equals(el.tagName());
            boolean nextItalic = italic || "em".equals(el.tagName()) || "i".equals(el.tagName());
            boolean nextUnderline = underline || "u".equals(el.tagName());
            for (Node child : el.childNodes()) {
                appendInlineNode(paragraph, child, nextBold, nextItalic, nextUnderline);
            }
        }
    }

    private void appendHeading(XWPFDocument doc, String text, int fontSize, boolean center) {
        XWPFParagraph p = doc.createParagraph();
        if (center) p.setAlignment(ParagraphAlignment.CENTER);
        p.setSpacingBefore(200);
        XWPFRun run = p.createRun();
        run.setText(text);
        run.setBold(true);
        run.setFontSize(fontSize);
    }

    private void appendParagraph(XWPFDocument doc, String text, boolean bold) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun run = p.createRun();
        run.setText(text);
        run.setBold(bold);
        run.setFontSize(11);
    }

    private void appendBullet(XWPFDocument doc, String text) {
        appendBullet(doc, text, 0);
    }

    private void appendBullet(XWPFDocument doc, String text, int indentLevel) {
        XWPFParagraph p = doc.createParagraph();
        p.setIndentationLeft(360 * (indentLevel + 1));
        XWPFRun run = p.createRun();
        run.setText("• " + text);
        run.setFontSize(11);
    }

    private void appendLabelValue(XWPFDocument doc, String label, String value) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun labelRun = p.createRun();
        labelRun.setText(label + ": ");
        labelRun.setBold(true);
        labelRun.setFontSize(11);
        XWPFRun valueRun = p.createRun();
        valueRun.setText(value == null || value.isBlank() ? "—" : value);
        valueRun.setFontSize(11);
    }

    private String asText(Object value) {
        return value == null ? "—" : String.valueOf(value);
    }

    private String formatDate(Object value) {
        if (value instanceof LocalDate date) {
            return date.format(DAY_DATE);
        }
        return value == null ? "—" : String.valueOf(value);
    }
}
