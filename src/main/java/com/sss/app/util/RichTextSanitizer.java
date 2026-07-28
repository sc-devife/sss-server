package com.sss.app.util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

/**
 * Rich text (Terms/Inclusions/Exclusions content, Section 8) is authored via
 * the frontend's TipTap editor, but the API accepts raw HTML directly — a
 * caller bypassing the editor UI could otherwise store a script/iframe/on*
 * handler that later executes in every viewer's browser via
 * dangerouslySetInnerHTML (library screen, itinerary builder, and the
 * customer-facing quote preview). Strip down to exactly the tags the editor
 * can actually produce (bold/italic/underline, paragraphs, bullet/numbered
 * lists) before anything is persisted.
 */
public final class RichTextSanitizer {

    private static final Safelist ALLOWLIST = new Safelist()
            .addTags("p", "br", "strong", "b", "em", "i", "u", "ul", "ol", "li");

    private RichTextSanitizer() {}

    public static String sanitize(String html) {
        if (html == null) return null;
        return Jsoup.clean(html, ALLOWLIST);
    }
}
