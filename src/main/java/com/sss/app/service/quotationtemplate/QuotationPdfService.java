package com.sss.app.service.quotationtemplate;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.Margin;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Converts already Mustache-rendered quotation HTML (from
 * QuotationRenderingService — the ONE rendering path, shared with the
 * on-screen preview) into a PDF, with a diagonal tiled watermark applied on
 * top. Uses a real headless browser (the machine's installed Chrome/Edge, via
 * Playwright) rather than a pure-Java PDF renderer, so the PDF renders the
 * exact same flexbox/gradient CSS the on-screen preview does — no separate
 * print-safe template variant to keep in sync.
 */
@Service
@Slf4j
public class QuotationPdfService {

    private Playwright playwright;
    private Browser browser;

    private synchronized Browser browser() {
        if (browser == null) {
            playwright = Playwright.create();
            // Launch the OS's own installed Chrome/Edge instead of
            // Playwright's bundled Chromium — avoids a ~300MB browser
            // download as a deploy step, since every target machine already
            // has one of these.
            browser = launchInstalledChromiumBrowser();
        }
        return browser;
    }

    private Browser launchInstalledChromiumBrowser() {
        IllegalStateException lastError = null;
        for (String channel : new String[]{"chrome", "msedge"}) {
            try {
                return playwright.chromium().launch(new BrowserType.LaunchOptions().setChannel(channel));
            } catch (RuntimeException e) {
                lastError = new IllegalStateException("Failed to launch browser channel '" + channel + "'", e);
            }
        }
        throw lastError;
    }

    @PreDestroy
    public void shutdown() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    /**
     * @param html          fully Mustache-rendered quotation HTML
     * @param watermarkText e.g. "TRP-000007 · Wanderlust Escapes" — null/blank skips the watermark
     */
    public byte[] render(String html, String watermarkText) {
        String withWatermark = injectWatermark(html, watermarkText);
        try (BrowserContext context = browser().newContext()) {
            Page page = context.newPage();
            page.setContent(withWatermark);
            return page.pdf(new Page.PdfOptions()
                    .setFormat("A4")
                    .setPrintBackground(true)
                    .setMargin(new Margin().setTop("14mm").setBottom("14mm").setLeft("10mm").setRight("10mm")));
        }
    }

    // A tiled, rotated, low-opacity SVG background repeated via plain CSS —
    // Chromium's print engine paints element backgrounds per-page, so this
    // tiles correctly across every page of the PDF without any per-page
    // logic. Applied on `body`, not `html`: every template sets an opaque
    // `background: #fff` on body itself, which would otherwise fully cover
    // a watermark painted behind it on `html`. Appending this rule AFTER the
    // template's own <style> block lets it win on the background-image
    // sub-property alone (same selector specificity, later in the cascade)
    // without disturbing the template's own background-color.
    private String injectWatermark(String html, String watermarkText) {
        if (watermarkText == null || watermarkText.isBlank()) {
            return html;
        }
        String dataUri = "data:image/svg+xml;base64,"
                + Base64.getEncoder().encodeToString(buildWatermarkSvg(watermarkText).getBytes(StandardCharsets.UTF_8));
        String style = "<style>body{background-image:url('" + dataUri + "') !important;background-repeat:repeat !important;"
                + "-webkit-print-color-adjust:exact;print-color-adjust:exact;}</style>";
        int headEnd = html.indexOf("</head>");
        if (headEnd < 0) {
            return style + html;
        }
        return html.substring(0, headEnd) + style + html.substring(headEnd);
    }

    private String buildWatermarkSvg(String text) {
        String escaped = escapeXml(text);
        return "<svg xmlns='http://www.w3.org/2000/svg' width='380' height='230'>"
                + "<text x='-20' y='130' transform='rotate(-30 190 115)' font-size='15' "
                + "font-family='Arial, Helvetica, sans-serif' fill='#000000' fill-opacity='0.06'>"
                + escaped + "</text></svg>";
    }

    private String escapeXml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;");
    }
}
