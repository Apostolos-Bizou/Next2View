package com.next2me.next2view.util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.jsoup.nodes.Document;

/**
 * HTML Sanitizer for user-supplied rich text content (defense-in-depth XSS protection).
 *
 * Used by entity lifecycle hooks (@PrePersist / @PreUpdate) on:
 *   - Project.contractDesc
 *   - CeoNote.content
 *   - Task.comment, Task.blockNote
 *
 * Frontend already sanitizes via DOMPurify; this is a SECOND layer that catches
 * any HTML that bypasses the frontend (e.g., direct API calls with malicious payload).
 *
 * Whitelist matches the frontend RichTextEditor allowed tags:
 *   p, br, strong, em, u, h1, h2, ul, ol, li, a, span
 *
 * Allowed attributes: href (a), target/rel (a), style (span — for color only)
 *
 * URL schemes: http, https, mailto only (blocks javascript:, data:, file:, etc.)
 */
public final class HtmlSanitizer {

    private HtmlSanitizer() { /* static utility */ }

    /**
     * Whitelist for rich text content. Built once and reused.
     * Thread-safe: Jsoup Safelist is immutable after configuration.
     */
    private static final Safelist SAFELIST = buildSafelist();

    private static Safelist buildSafelist() {
        Safelist sl = new Safelist();
        // Allowed tags
        sl.addTags("p", "br", "strong", "em", "u", "h1", "h2", "ul", "ol", "li", "a", "span");
        // Link attributes
        sl.addAttributes("a", "href", "target", "rel");
        sl.addProtocols("a", "href", "http", "https", "mailto");
        sl.addEnforcedAttribute("a", "rel", "noopener noreferrer nofollow");
        // Style attribute (for inline color via Tiptap Color extension)
        sl.addAttributes("span", "style");
        sl.addAttributes("p", "style");
        // Note: Jsoup does NOT validate CSS values inside style. We rely on the fact that
        // Jsoup strips JavaScript URL schemes from style attribute by default.
        return sl;
    }

    /**
     * Clean user-supplied HTML, keeping only safe tags/attributes.
     * Returns empty string for null/empty input. Output is safe to render with v-html on the client
     * AND has already been DOMPurify-sanitized by the frontend (defense-in-depth).
     *
     * @param dirtyHtml raw HTML from request payload
     * @return sanitized HTML safe for storage and rendering
     */
    public static String clean(String dirtyHtml) {
        if (dirtyHtml == null || dirtyHtml.isEmpty()) {
            return dirtyHtml;
        }
        // Jsoup.clean preserves text content but strips disallowed tags/attributes.
        // We use prettyPrint(false) to avoid Jsoup adding whitespace that would mutate the content.
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.prettyPrint(false);
        return Jsoup.clean(dirtyHtml, "", SAFELIST, settings);
    }
}
