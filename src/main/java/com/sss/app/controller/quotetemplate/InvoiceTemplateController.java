package com.sss.app.controller.quotetemplate;

import com.sss.app.service.quotetemplate.InvoiceTemplateDefinition;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/invoice-templates")
public class InvoiceTemplateController {

    @GetMapping
    public ResponseEntity<List<InvoiceTemplateDefinition>> list() {
        return ResponseEntity.ok(InvoiceTemplateDefinition.ALL);
    }
}
