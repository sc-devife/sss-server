package com.sss.app.controller.quotetemplate;

import com.sss.app.service.quotetemplate.QuoteTemplateDefinition;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/quote-templates")
public class QuoteTemplateController {

    @GetMapping
    public ResponseEntity<List<QuoteTemplateDefinition>> list() {
        return ResponseEntity.ok(QuoteTemplateDefinition.ALL);
    }
}
