package com.sss.app.service.quotationtemplate;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * The ONE place that combines a quotation template's HTML with quotation
 * data — used identically by the Settings sample preview and the real
 * Escape/Quote preview (and, later, PDF generation) so rendering logic never
 * forks between them.
 */
@Service
@Slf4j
@lombok.RequiredArgsConstructor
public class QuotationRenderingService {

    private final com.sss.app.service.exchangerate.CurrencyDisplayService currencyDisplayService;

    private static final MustacheFactory MUSTACHE_FACTORY = new DefaultMustacheFactory();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // Adds the vendor's base-currency symbol/code to every template's data
    // (unless the caller already set them), so templates print `{{currencySymbol}}`
    // instead of a hard-coded rupee sign. Async callers with no logged-in user
    // set these themselves from the record's org.
    private Map<String, Object> withCurrency(Map<String, Object> data) {
        if (data != null && data.containsKey("currencySymbol")) return data;
        Long orgId = null;
        try {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof com.sss.app.entity.users.User user) orgId = user.getOrgId();
        } catch (Exception ignored) {
            // no security context (async/scheduled) - fall through to the default currency
        }
        Map<String, Object> copy = new java.util.HashMap<>(data == null ? Map.of() : data);
        String code = currencyDisplayService.baseCurrencyCode(orgId);
        copy.put("currencyCode", copy.getOrDefault("currencyCode", code));
        copy.put("currencySymbol", currencyDisplayService.symbol(code));
        return copy;
    }

    // A rupee sign typed straight into an uploaded template would print on every
    // vendor/currency. Swap it for the placeholder that fits what follows: the quote's
    // currency before a pricing amount, the vendor's base currency anywhere else.
    private static final java.util.regex.Pattern RUPEE_BEFORE_PRICING =
            java.util.regex.Pattern.compile("(?:₹|&#8377;|&#[xX]20[bB]9;)(?=(?:\\s|<[^>]*>)*\\{\\{\\{?\\s*pricing\\.)");
    private static final java.util.regex.Pattern RUPEE_ANY =
            java.util.regex.Pattern.compile("₹|&#8377;|&#[xX]20[bB]9;");

    static String neutralizeHardcodedRupee(String html) {
        String withPricing = RUPEE_BEFORE_PRICING.matcher(html).replaceAll(java.util.regex.Matcher.quoteReplacement("{{pricing.currencySymbol}}"));
        return RUPEE_ANY.matcher(withPricing).replaceAll(java.util.regex.Matcher.quoteReplacement("{{currencySymbol}}"));
    }

    public String render(String cloudinaryUrl, Map<String, Object> data) {
        String templateHtml = neutralizeHardcodedRupee(fetchTemplateHtml(cloudinaryUrl));
        Mustache mustache = MUSTACHE_FACTORY.compile(new StringReader(templateHtml), "quotation-template");
        StringWriter writer = new StringWriter();
        mustache.execute(writer, withCurrency(data));
        return writer.toString();
    }

    // Reads the template straight from the classpath (DefaultMustacheFactory
    // resolves a bare resource name against the classpath root) — used for
    // the Quotation/Invoice email bodies, which ship with the app rather than
    // living in Cloudinary like the customer-facing PDF templates.
    public String renderClasspathTemplate(String classpathResource, Map<String, Object> data) {
        Mustache mustache = MUSTACHE_FACTORY.compile(classpathResource);
        StringWriter writer = new StringWriter();
        mustache.execute(writer, withCurrency(data));
        return writer.toString();
    }

    // For short dynamic strings (e.g. an email subject line) that aren't
    // worth their own template file.
    public String renderInline(String template, Map<String, Object> data) {
        Mustache mustache = MUSTACHE_FACTORY.compile(new StringReader(template), "inline-template");
        StringWriter writer = new StringWriter();
        mustache.execute(writer, withCurrency(data));
        return writer.toString();
    }

    private String fetchTemplateHtml(String cloudinaryUrl) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(cloudinaryUrl))
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("Cloudinary returned status " + response.statusCode() + " for template " + cloudinaryUrl);
            }
            return response.body();
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.error("Failed to fetch quotation template HTML from {}", cloudinaryUrl, e);
            throw new IllegalStateException("Failed to fetch quotation template from Cloudinary: " + e.getMessage(), e);
        }
    }
}
