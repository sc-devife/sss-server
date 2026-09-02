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
public class QuotationRenderingService {

    private static final MustacheFactory MUSTACHE_FACTORY = new DefaultMustacheFactory();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public String render(String cloudinaryUrl, Map<String, Object> data) {
        String templateHtml = fetchTemplateHtml(cloudinaryUrl);
        Mustache mustache = MUSTACHE_FACTORY.compile(new StringReader(templateHtml), "quotation-template");
        StringWriter writer = new StringWriter();
        mustache.execute(writer, data);
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
