package com.sss.app.service.quotetemplate;

import java.util.UUID;

/**
 * One entry in the curated set (Section 9 — "if a fully custom per-org
 * template builder is too heavy for v1, ship a curated set of ~10 designs
 * instead"). Deliberately not 10 fully bespoke hand-built layouts: three
 * layout families (classic/modern/minimal) each rendered with different
 * accent colors, giving a real, distinct-looking set of 10 without hand
 * building ten complete designs — flagged as a v1 simplification. Real
 * per-org custom template building is later-phase work per the spec.
 */
public record QuoteTemplateDefinition(UUID id, String name, String description, String layoutFamily, String accentColor) {

    public static final java.util.List<QuoteTemplateDefinition> ALL = java.util.List.of(
            new QuoteTemplateDefinition(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Classic Teal", "Traditional layout with a clean header and teal accents", "classic", "#0f766e"),
            new QuoteTemplateDefinition(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Classic Navy", "Traditional layout with a formal navy palette", "classic", "#1e3a8a"),
            new QuoteTemplateDefinition(UUID.fromString("00000000-0000-0000-0000-000000000003"), "Classic Burgundy", "Traditional layout with a warm burgundy accent", "classic", "#9f1239"),
            new QuoteTemplateDefinition(UUID.fromString("00000000-0000-0000-0000-000000000004"), "Modern Coral", "Image-forward layout with bold coral highlights", "modern", "#e11d48"),
            new QuoteTemplateDefinition(UUID.fromString("00000000-0000-0000-0000-000000000005"), "Modern Ocean", "Image-forward layout with an ocean-blue palette", "modern", "#0369a1"),
            new QuoteTemplateDefinition(UUID.fromString("00000000-0000-0000-0000-000000000006"), "Modern Sunset", "Image-forward layout with warm sunset tones", "modern", "#c2410c"),
            new QuoteTemplateDefinition(UUID.fromString("00000000-0000-0000-0000-000000000007"), "Modern Forest", "Image-forward layout with an earthy green accent", "modern", "#15803d"),
            new QuoteTemplateDefinition(UUID.fromString("00000000-0000-0000-0000-000000000008"), "Minimal Slate", "Understated typography-first layout in slate grey", "minimal", "#334155"),
            new QuoteTemplateDefinition(UUID.fromString("00000000-0000-0000-0000-000000000009"), "Minimal Charcoal", "Understated typography-first layout in near-black", "minimal", "#18181b"),
            new QuoteTemplateDefinition(UUID.fromString("00000000-0000-0000-0000-00000000000a"), "Minimal Sand", "Understated typography-first layout in warm sand tones", "minimal", "#92400e")
    );

    public static QuoteTemplateDefinition byId(UUID id) {
        return ALL.stream().filter(t -> t.id().equals(id)).findFirst().orElse(null);
    }
}
