package com.sss.app.service.quotetemplate;

import java.util.List;
import java.util.UUID;

/**
 * Section 9: "a smaller, cleaner set is fine for invoices since they're
 * less marketing-oriented than quotes, but they should still carry the
 * org's branding." Half the size of the quote template set, same
 * registry/renderer pattern as QuoteTemplateDefinition (Phase 5).
 */
public record InvoiceTemplateDefinition(UUID id, String name, String description, String accentColor) {

    public static final List<InvoiceTemplateDefinition> ALL = List.of(
            new InvoiceTemplateDefinition(UUID.fromString("00000000-0000-0000-0000-000000000101"), "Standard Slate", "Clean, formal invoice layout in slate grey", "#334155"),
            new InvoiceTemplateDefinition(UUID.fromString("00000000-0000-0000-0000-000000000102"), "Standard Navy", "Clean, formal invoice layout in navy", "#1e3a8a"),
            new InvoiceTemplateDefinition(UUID.fromString("00000000-0000-0000-0000-000000000103"), "Standard Teal", "Clean, formal invoice layout in teal", "#0f766e"),
            new InvoiceTemplateDefinition(UUID.fromString("00000000-0000-0000-0000-000000000104"), "Standard Charcoal", "Clean, formal invoice layout in near-black", "#18181b"),
            new InvoiceTemplateDefinition(UUID.fromString("00000000-0000-0000-0000-000000000105"), "Standard Burgundy", "Clean, formal invoice layout in burgundy", "#9f1239")
    );

    public static InvoiceTemplateDefinition byId(UUID id) {
        return ALL.stream().filter(t -> t.id().equals(id)).findFirst().orElse(null);
    }
}
