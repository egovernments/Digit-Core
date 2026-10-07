package org.egov.web.requestvalidation;

import org.egov.requestvalidation.core.ContentDetector;
import org.egov.requestvalidation.core.ContentPolicy;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.Range;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Parser;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Accepts an HTML e-mail template only when it is plain formatting markup: allow-listed tags and attributes,
 * http(s), mailto or {placeholder} links, simple inline styles, and nothing the request-validation URL-scheme,
 * event-handler or control-character rules flag. Scripts, frames, forms, images, comments and style blocks fail.
 * Every tag in the text must be one the parser kept, so an unterminated, misplaced or stray tag (which a parser
 * drops here but which could take effect once the template is combined with other content) fails too, and text
 * and attribute values must carry no markup of their own, encoded or not. Nothing is modified.
 */
public final class SafeHtmlTemplate {
    private static final ContentDetector ACTIVE_CONTENT =
            new ContentDetector(ContentPolicy.builder().markupStart(false).build());
    private static final ContentDetector ANY_CONTENT = new ContentDetector(ContentPolicy.builder().build());
    // Templates nest a few levels; deeper documents fail instead of costing work.
    private static final int MAX_DEPTH = 64;

    private static final Set<String> TAGS = Set.of("html", "head", "body", "p", "br", "b", "strong", "i", "em", "u",
            "h1", "h2", "h3", "h4", "h5", "h6", "ul", "ol", "li", "table", "thead", "tbody", "tfoot", "tr", "td", "th",
            "span", "div", "a", "hr", "blockquote", "small", "sub", "sup");

    private static final Pattern SIZE = Pattern.compile("[0-9]{1,4}(%|px)?");
    private static final Pattern COLOR = Pattern.compile("#[0-9a-fA-F]{3,6}|[a-zA-Z]{1,20}");
    private static final Map<String, Pattern> ATTRIBUTES = Map.of(
            "align", Pattern.compile("(?i)left|right|center|justify"),
            "valign", Pattern.compile("(?i)top|middle|bottom|baseline"),
            "dir", Pattern.compile("(?i)ltr|rtl"),
            "width", SIZE, "height", SIZE, "colspan", SIZE, "rowspan", SIZE,
            "border", SIZE, "cellpadding", SIZE, "cellspacing", SIZE);

    private static final Pattern LINK = Pattern.compile("(?i)(https?://|mailto:)\\S+");
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{?[A-Za-z0-9_ .'-]{1,64}}?}");
    private static final Pattern TARGET = Pattern.compile("(?i)_blank|_self");

    private static final Set<String> CSS_PROPERTIES = Set.of("font-family", "font-size", "font-weight", "font-style",
            "color", "background-color", "text-align", "text-decoration", "line-height", "letter-spacing",
            "vertical-align", "white-space", "width", "height", "border", "border-top", "border-bottom", "border-left",
            "border-right", "border-collapse", "margin", "margin-top", "margin-bottom", "margin-left", "margin-right",
            "padding", "padding-top", "padding-bottom", "padding-left", "padding-right");
    // No parentheses, slashes, backslashes, colons or angle brackets: no url(), expression(), escapes or comments.
    private static final Pattern CSS_VALUE = Pattern.compile("[A-Za-z0-9#%.,\\s'\"-]{0,200}");

    private SafeHtmlTemplate() {
    }

    public static boolean isSafe(String html) {
        if (html == null || ACTIVE_CONTENT.detect(html).isPresent()) {
            return false;
        }
        Document document = Jsoup.parse(html, "", Parser.htmlParser().setTrackPosition(true));
        Set<Integer> kept = new HashSet<>();
        return allowed(document, html, kept) && everyTagKept(html, kept);
    }

    /** Walks the document with an explicit stack, so nesting depth cannot exhaust the thread stack. */
    private static boolean allowed(Document document, String html, Set<Integer> kept) {
        Deque<Node> nodes = new ArrayDeque<>();
        Deque<Integer> depths = new ArrayDeque<>();
        for (Node child : document.childNodes()) {
            nodes.push(child);
            depths.push(1);
        }
        while (!nodes.isEmpty()) {
            Node node = nodes.pop();
            int depth = depths.pop();
            if (node instanceof TextNode) {
                if (ANY_CONTENT.detect(((TextNode) node).getWholeText()).isPresent()) {
                    return false;
                }
            } else if (node instanceof DocumentType) {
                // Only the plain HTML5 doctype, written out in full.
                Range range = node.sourceRange();
                if (!range.isTracked()
                        || !"<!DOCTYPE html>".equalsIgnoreCase(html.substring(range.start().pos(), range.end().pos()))) {
                    return false;
                }
                keep(range, kept);
            } else if (node instanceof Element && depth <= MAX_DEPTH && allowedElement((Element) node)) {
                keep(node.sourceRange(), kept);
                keep(((Element) node).endSourceRange(), kept);
                for (Node child : node.childNodes()) {
                    nodes.push(child);
                    depths.push(depth + 1);
                }
            } else {
                // Comments (including conditional comments), data nodes, anything else and very deep nesting fail.
                return false;
            }
        }
        return true;
    }

    private static void keep(Range range, Set<Integer> kept) {
        // Implied elements (html, head, body, tbody) have no source text: an empty or untracked range.
        if (range.isTracked() && range.end().pos() > range.start().pos()) {
            kept.add(range.start().pos());
        }
    }

    /**
     * Each "<" that starts a tag, end tag, comment or declaration in the source must begin one the parser kept, and
     * the value must not end in "<", which starts a tag as soon as other text is appended.
     */
    private static boolean everyTagKept(String html, Set<Integer> kept) {
        if (html.endsWith("<")) {
            return false;
        }
        for (int index = html.indexOf('<'); index >= 0 && index + 1 < html.length(); index = html.indexOf('<', index + 1)) {
            char next = html.charAt(index + 1);
            boolean tag = (next >= 'a' && next <= 'z') || (next >= 'A' && next <= 'Z') || next == '/' || next == '!' || next == '?';
            if (tag && !kept.contains(index)) {
                return false;
            }
        }
        return true;
    }

    private static boolean allowedElement(Element element) {
        String tag = element.normalName();
        if (!TAGS.contains(tag)) {
            return false;
        }
        for (Attribute attribute : element.attributes()) {
            String name = attribute.getKey().toLowerCase(Locale.ROOT);
            String value = attribute.getValue().trim();
            if (ANY_CONTENT.detect(value).isPresent()) {
                return false;
            }
            boolean ok;
            switch (name) {
                case "style":
                    ok = safeStyle(value);
                    break;
                case "href":
                    ok = "a".equals(tag) && (LINK.matcher(value).matches() || PLACEHOLDER.matcher(value).matches());
                    break;
                case "target":
                    ok = "a".equals(tag) && TARGET.matcher(value).matches();
                    break;
                case "bgcolor":
                    ok = COLOR.matcher(value).matches();
                    break;
                default:
                    Pattern pattern = ATTRIBUTES.get(name);
                    ok = pattern != null && pattern.matcher(value).matches();
            }
            if (!ok) {
                return false;
            }
        }
        return true;
    }

    private static boolean safeStyle(String style) {
        for (String declaration : style.split(";")) {
            if (declaration.isBlank()) {
                continue;
            }
            int colon = declaration.indexOf(':');
            if (colon < 0) {
                return false;
            }
            String property = declaration.substring(0, colon).trim().toLowerCase(Locale.ROOT);
            String value = declaration.substring(colon + 1).trim();
            if (!CSS_PROPERTIES.contains(property) || !CSS_VALUE.matcher(value).matches()) {
                return false;
            }
        }
        return true;
    }
}
