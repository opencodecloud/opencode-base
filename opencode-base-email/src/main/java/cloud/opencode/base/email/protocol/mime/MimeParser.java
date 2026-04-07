package cloud.opencode.base.email.protocol.mime;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MIME Message Parser
 * MIME 消息解析器
 *
 * <p>Parses raw RFC 2822 MIME messages into structured {@link ParsedMessage}
 * records. Handles multipart messages (mixed, alternative, related),
 * Content-Transfer-Encoding (base64, quoted-printable, 7bit, 8bit),
 * RFC 2047 encoded headers, and various date formats.</p>
 * <p>将原始 RFC 2822 MIME 消息解析为结构化的 {@link ParsedMessage} 记录。
 * 处理多部分消息（mixed、alternative、related）、内容传输编码
 * （base64、quoted-printable、7bit、8bit）、RFC 2047 编码邮件头和各种日期格式。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Header parsing with continuation line unfolding - 邮件头解析及折叠行展开</li>
 *   <li>Multipart boundary extraction and recursive parsing - 多部分边界提取和递归解析</li>
 *   <li>Content-Transfer-Encoding decoding - 内容传输编码解码</li>
 *   <li>RFC 2047 encoded-word decoding for headers - RFC 2047 编码字解码</li>
 *   <li>Multiple date format support - 多种日期格式支持</li>
 *   <li>DoS protection with configurable max depth - 可配置最大深度的 DoS 防护</li>
 *   <li>Attachment extraction - 附件提取</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Recursion depth limited to {@value #MAX_DEPTH} - 递归深度限制为 {@value #MAX_DEPTH}</li>
 * </ul>
 *
 * @author Leon Soo
 * @see ParsedMessage
 * @see MimeEncoder
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.3
 */
public final class MimeParser {

    /** Maximum recursion depth for multipart parsing (DoS protection). */
    private static final int MAX_DEPTH = 50;

    /** Pattern to extract boundary from Content-Type header. */
    private static final Pattern BOUNDARY_PATTERN =
            Pattern.compile("boundary\\s*=\\s*\"?([^\";\\s]+)\"?", Pattern.CASE_INSENSITIVE);

    /** Pattern to extract charset from Content-Type header. */
    private static final Pattern CHARSET_PATTERN =
            Pattern.compile("charset\\s*=\\s*\"?([^\";\\s]+)\"?", Pattern.CASE_INSENSITIVE);

    /** Pattern to extract name from Content-Type or Content-Disposition header. */
    private static final Pattern NAME_PATTERN =
            Pattern.compile("(?:file)?name\\s*=\\s*\"?([^\";\\s]+)\"?", Pattern.CASE_INSENSITIVE);

    /** Pattern to parse "Name <email>" format. */
    private static final Pattern ADDRESS_PATTERN =
            Pattern.compile("^\\s*\"?(.+?)\"?\\s*<([^>]+)>\\s*$");

    /** RFC 2822 date formats to try when parsing Date headers. */
    private static final DateTimeFormatter[] DATE_FORMATTERS = {
            // Standard RFC 2822: "Tue, 15 Jan 2025 10:30:00 +0800"
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US),
            // Without day-of-week: "15 Jan 2025 10:30:00 +0800"
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss Z", Locale.US),
            // With timezone name: "Tue, 15 Jan 2025 10:30:00 +0800 (CST)"
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z '('z')'", Locale.US),
            // Single-digit day: "Tue, 5 Jan 2025 10:30:00 +0800"
            DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss Z", Locale.US),
            // Two-digit year: "Tue, 15 Jan 25 10:30:00 +0800"
            DateTimeFormatter.ofPattern("EEE, dd MMM yy HH:mm:ss Z", Locale.US),
            // Without seconds: "Tue, 15 Jan 2025 10:30 +0800"
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm Z", Locale.US),
    };

    private MimeParser() {
        // utility class
    }

    /**
     * Parse a complete raw MIME message into a {@link ParsedMessage}
     * 将完整的原始 MIME 消息解析为 {@link ParsedMessage}
     *
     * <p>Splits the message at the first blank line to separate headers from body,
     * then recursively parses multipart structures to extract text, HTML, and
     * attachments.</p>
     * <p>在第一个空行处分割消息以分离邮件头和消息体，然后递归解析多部分结构以
     * 提取文本、HTML和附件。</p>
     *
     * @param rawMessage the raw RFC 2822 message string | 原始 RFC 2822 消息字符串
     * @return the parsed message | 解析后的消息
     * @throws IllegalArgumentException if rawMessage is null or empty | 消息为空时抛出
     */
    public static ParsedMessage parse(String rawMessage) {
        if (rawMessage == null || rawMessage.isEmpty()) {
            throw new IllegalArgumentException("Raw message must not be null or empty");
        }

        int size = rawMessage.length(); // approximate char count, sufficient for informational size field

        // Split headers and body
        String[] parts = splitHeadersAndBody(rawMessage);
        String headerSection = parts[0];
        String bodySection = parts[1];

        // Parse headers (once, producing both lowercase and original-case maps)
        ParsedHeaders parsedHeaders = parseHeadersBoth(headerSection);
        Map<String, String> headers = parsedHeaders.lowercase();
        Map<String, String> headersOriginal = parsedHeaders.originalCase();

        // Extract structured header values
        String messageId = headers.get("message-id");
        String fromHeader = headers.getOrDefault("from", "");
        String[] fromParsed = parseAddress(fromHeader);
        String fromEmail = fromParsed[1];
        String fromName = fromParsed[0];

        List<String> to = parseAddressList(headers.get("to"));
        List<String> cc = parseAddressList(headers.get("cc"));
        List<String> bcc = parseAddressList(headers.get("bcc"));
        String replyTo = headers.get("reply-to");
        String subject = MimeEncoder.decodeWord(headers.get("subject"));

        Instant sentDate = parseDate(headers.get("date"));
        Instant receivedDate = parseReceivedDate(headers.get("received"));

        // Parse body
        String contentType = headers.getOrDefault("content-type", "text/plain");
        String transferEncoding = headers.getOrDefault("content-transfer-encoding", "7bit");

        List<ParsedMessage.ParsedAttachment> attachments = new ArrayList<>();
        String textContent = null;
        String htmlContent = null;

        String contentTypeLower = contentType.toLowerCase(Locale.ROOT);

        if (contentTypeLower.startsWith("multipart/")) {
            String boundary = extractBoundary(contentType);
            if (boundary != null) {
                BodyResult result = parseMultipart(bodySection, boundary, contentType, 0);
                textContent = result.textContent;
                htmlContent = result.htmlContent;
                attachments.addAll(result.attachments);
            }
        } else if (contentTypeLower.startsWith("text/html")) {
            Charset cs = extractCharset(contentType);
            htmlContent = decodeBody(bodySection, transferEncoding, cs);
        } else if (contentTypeLower.startsWith("text/")) {
            Charset cs = extractCharset(contentType);
            textContent = decodeBody(bodySection, transferEncoding, cs);
        } else {
            // Non-text, non-multipart top-level (e.g., image, application) → treat as attachment
            String disposition = headers.getOrDefault("content-disposition", "");
            String fileName = sanitizeFileName(extractFileName(contentType, disposition));
            byte[] data = decodeBodyBytes(bodySection, transferEncoding);
            String baseCt = contentTypeLower.contains(";")
                    ? contentTypeLower.substring(0, contentTypeLower.indexOf(';')).trim()
                    : contentTypeLower;
            boolean inline = disposition.toLowerCase(Locale.ROOT).startsWith("inline");
            String contentId = headers.get("content-id");
            attachments.add(new ParsedMessage.ParsedAttachment(
                    fileName, baseCt, data, inline, stripAngleBrackets(contentId)));
        }

        // Use the case-preserved header map already parsed above
        Map<String, String> publicHeaders = headersOriginal;

        return new ParsedMessage(
                stripAngleBrackets(messageId),
                fromEmail,
                fromName,
                List.copyOf(to),
                List.copyOf(cc),
                List.copyOf(bcc),
                replyTo,
                subject,
                textContent,
                htmlContent,
                sentDate,
                receivedDate,
                size,
                Map.copyOf(publicHeaders),
                List.copyOf(attachments)
        );
    }

    /**
     * Parse just the headers from a raw message
     * 仅从原始消息中解析邮件头
     *
     * <p>Returns a map of header names (original case) to their values.
     * Continuation lines are unfolded.</p>
     * <p>返回邮件头名称（原始大小写）到值的映射。折叠行会被展开。</p>
     *
     * @param rawMessage the raw RFC 2822 message string | 原始 RFC 2822 消息字符串
     * @return the headers map | 邮件头映射
     * @throws IllegalArgumentException if rawMessage is null or empty | 消息为空时抛出
     */
    public static Map<String, String> parseHeaders(String rawMessage) {
        if (rawMessage == null || rawMessage.isEmpty()) {
            throw new IllegalArgumentException("Raw message must not be null or empty");
        }
        String[] parts = splitHeadersAndBody(rawMessage);
        return unfoldAndParseHeadersPreserveCase(parts[0]);
    }

    // ========== Internal: Splitting ==========

    /**
     * Split a message at the first blank line into [headers, body].
     */
    private static String[] splitHeadersAndBody(String raw) {
        // Try CRLF first, then bare LF
        int idx = raw.indexOf("\r\n\r\n");
        if (idx >= 0) {
            return new String[]{raw.substring(0, idx), raw.substring(idx + 4)};
        }
        idx = raw.indexOf("\n\n");
        if (idx >= 0) {
            return new String[]{raw.substring(0, idx), raw.substring(idx + 2)};
        }
        // No body
        return new String[]{raw, ""};
    }

    // ========== Internal: Header Parsing ==========

    /**
     * Unfold continuation lines and parse into lowercase-keyed map.
     */
    private static Map<String, String> unfoldAndParseHeaders(String headerSection) {
        Map<String, String> headers = new LinkedHashMap<>();
        String unfolded = unfoldHeaders(headerSection);
        String[] lines = unfolded.split("\\r?\\n");
        for (String line : lines) {
            if (line.isEmpty()) {
                continue;
            }
            int colon = line.indexOf(':');
            if (colon > 0) {
                String name = line.substring(0, colon).trim().toLowerCase(Locale.ROOT);
                String value = line.substring(colon + 1).trim();
                // For duplicate headers (e.g., Received), append
                headers.merge(name, value, (old, v) -> old + "; " + v);
            }
        }
        return headers;
    }

    /**
     * Unfold continuation lines and parse into original-case-keyed map.
     */
    private static Map<String, String> unfoldAndParseHeadersPreserveCase(String headerSection) {
        Map<String, String> headers = new LinkedHashMap<>();
        String unfolded = unfoldHeaders(headerSection);
        String[] lines = unfolded.split("\\r?\\n");
        for (String line : lines) {
            if (line.isEmpty()) {
                continue;
            }
            int colon = line.indexOf(':');
            if (colon > 0) {
                String name = line.substring(0, colon).trim();
                String value = line.substring(colon + 1).trim();
                headers.merge(name, value, (old, v) -> old + "; " + v);
            }
        }
        return headers;
    }

    /**
     * Unfold RFC 2822 header continuation lines.
     * Lines starting with whitespace are appended to the previous header.
     */
    private static String unfoldHeaders(String headerSection) {
        // Replace CRLF + whitespace or LF + whitespace with a single space
        return headerSection
                .replaceAll("\\r?\\n[ \\t]+", " ");
    }

    /** Combined result of parsing headers into both lowercase and original-case maps. */
    private record ParsedHeaders(Map<String, String> lowercase, Map<String, String> originalCase) {}

    /**
     * Parse headers once, producing both a lowercase-keyed map (for internal lookups)
     * and an original-case-keyed map (for the public API).
     * 一次解析邮件头，同时生成小写键映射（内部查找用）和原始大小写键映射（公共 API 用）。
     */
    private static ParsedHeaders parseHeadersBoth(String headerSection) {
        String unfolded = unfoldHeaders(headerSection);
        String[] lines = unfolded.split("\\r?\\n");

        Map<String, String> lowercase = new LinkedHashMap<>();
        Map<String, String> originalCase = new LinkedHashMap<>();

        for (String line : lines) {
            if (line.isEmpty()) {
                continue;
            }
            int colonIdx = line.indexOf(':');
            if (colonIdx > 0) {
                String name = line.substring(0, colonIdx).trim();
                String value = line.substring(colonIdx + 1).trim();
                lowercase.merge(name.toLowerCase(Locale.ROOT), value, (old, v) -> old + "; " + v);
                originalCase.merge(name, value, (old, v) -> old + "; " + v);
            }
        }
        return new ParsedHeaders(lowercase, originalCase);
    }

    // ========== Internal: Address Parsing ==========

    /**
     * Parse "Display Name <email@domain>" or just "email@domain".
     * Returns [name, email]. Name may be null.
     */
    private static String[] parseAddress(String address) {
        if (address == null || address.isEmpty()) {
            return new String[]{null, ""};
        }
        String decoded = MimeEncoder.decodeWord(address.trim());
        Matcher m = ADDRESS_PATTERN.matcher(decoded);
        if (m.matches()) {
            String name = m.group(1).trim();
            String email = m.group(2).trim();
            return new String[]{name.isEmpty() ? null : name, email};
        }
        // Bare email
        return new String[]{null, decoded.trim()};
    }

    /**
     * Parse a comma-separated list of addresses.
     */
    private static List<String> parseAddressList(String header) {
        if (header == null || header.isEmpty()) {
            return List.of();
        }
        List<String> addresses = new ArrayList<>();
        // Split on commas, but respect angle brackets
        StringBuilder current = new StringBuilder();
        int depth = 0;
        for (int i = 0; i < header.length(); i++) {
            char c = header.charAt(i);
            if (c == '<') {
                depth++;
            } else if (c == '>') {
                depth = Math.max(0, depth - 1);
            } else if (c == ',' && depth == 0) {
                addParsedAddress(current.toString(), addresses);
                current.setLength(0);
                continue;
            }
            current.append(c);
        }
        addParsedAddress(current.toString(), addresses);
        return addresses;
    }

    private static void addParsedAddress(String raw, List<String> list) {
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return;
        }
        String decoded = MimeEncoder.decodeWord(trimmed);
        // Extract just the email from "Name <email>" if present
        Matcher m = ADDRESS_PATTERN.matcher(decoded);
        if (m.matches()) {
            list.add(m.group(2).trim());
        } else {
            list.add(decoded);
        }
    }

    // ========== Internal: Date Parsing ==========

    /**
     * Parse a Date header value into an Instant, trying multiple formats.
     */
    private static Instant parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        // Clean up: remove comments in parentheses at end, e.g., "(CST)"
        String cleaned = dateStr.replaceAll("\\s*\\([^)]*\\)\\s*$", "").trim();

        // Try each RFC 2822 formatter
        for (DateTimeFormatter fmt : DATE_FORMATTERS) {
            try {
                ZonedDateTime zdt = ZonedDateTime.parse(cleaned, fmt);
                return zdt.toInstant();
            } catch (DateTimeParseException ignored) {
                // try next
            }
        }

        // Try ISO 8601
        try {
            return Instant.parse(cleaned);
        } catch (DateTimeParseException ignored) {
            // try ZonedDateTime ISO
        }
        try {
            return ZonedDateTime.parse(cleaned).toInstant();
        } catch (DateTimeParseException ignored) {
            // give up
        }

        return null;
    }

    /**
     * Parse a Received header to extract the date portion (after the last semicolon).
     */
    private static Instant parseReceivedDate(String receivedHeader) {
        if (receivedHeader == null || receivedHeader.isEmpty()) {
            return null;
        }
        int semi = receivedHeader.lastIndexOf(';');
        if (semi >= 0 && semi + 1 < receivedHeader.length()) {
            return parseDate(receivedHeader.substring(semi + 1).trim());
        }
        return null;
    }

    // ========== Internal: Multipart Parsing ==========

    /** Intermediate result from parsing a multipart body. */
    private record BodyResult(
            String textContent,
            String htmlContent,
            List<ParsedMessage.ParsedAttachment> attachments
    ) {}

    /**
     * Parse a multipart body by splitting on the boundary and recursively
     * processing each part.
     */
    private static BodyResult parseMultipart(
            String body, String boundary, String parentContentType, int depth) {

        if (depth >= MAX_DEPTH) {
            return new BodyResult(null, null, List.of());
        }

        String delimiterLine = "--" + boundary;
        String closingLine = "--" + boundary + "--";

        // Split body by boundary
        List<String> parts = new ArrayList<>();
        int pos = body.indexOf(delimiterLine);
        if (pos < 0) {
            return new BodyResult(null, null, List.of());
        }

        while (pos >= 0) {
            // Skip past the boundary line
            int lineEnd = body.indexOf('\n', pos);
            if (lineEnd < 0) {
                break;
            }
            int partStart = lineEnd + 1;

            // Find next boundary
            int nextBoundary = body.indexOf(delimiterLine, partStart);
            if (nextBoundary < 0) {
                break;
            }

            // Check for closing boundary
            String partContent = body.substring(partStart, nextBoundary);
            // Remove trailing CRLF before boundary
            if (partContent.endsWith("\r\n")) {
                partContent = partContent.substring(0, partContent.length() - 2);
            } else if (partContent.endsWith("\n")) {
                partContent = partContent.substring(0, partContent.length() - 1);
            }

            parts.add(partContent);

            // Check if next boundary is closing
            int afterBoundary = nextBoundary + delimiterLine.length();
            if (afterBoundary + 1 < body.length()
                    && body.charAt(afterBoundary) == '-'
                    && body.charAt(afterBoundary + 1) == '-') {
                break;
            }

            pos = nextBoundary;
        }

        String text = null;
        String html = null;
        List<ParsedMessage.ParsedAttachment> attachments = new ArrayList<>();

        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }

            // Split this part's headers from its body
            String[] partSplit = splitHeadersAndBody(part);
            String partHeaders = partSplit[0];
            String partBody = partSplit[1];

            Map<String, String> hdrs = unfoldAndParseHeaders(partHeaders);
            String ct = hdrs.getOrDefault("content-type", "text/plain");
            String cte = hdrs.getOrDefault("content-transfer-encoding", "7bit");
            String ctLower = ct.toLowerCase(Locale.ROOT);
            String disposition = hdrs.getOrDefault("content-disposition", "");
            String dispLower = disposition.toLowerCase(Locale.ROOT);

            if (ctLower.startsWith("multipart/")) {
                // Recursive multipart
                String subBoundary = extractBoundary(ct);
                if (subBoundary != null) {
                    BodyResult sub = parseMultipart(partBody, subBoundary, ct, depth + 1);
                    if (sub.textContent != null && text == null) {
                        text = sub.textContent;
                    }
                    if (sub.htmlContent != null && html == null) {
                        html = sub.htmlContent;
                    }
                    attachments.addAll(sub.attachments);
                }
            } else if (dispLower.startsWith("attachment")
                    || (dispLower.startsWith("inline") && !ctLower.startsWith("text/"))) {
                // Attachment or inline non-text
                String fileName = sanitizeFileName(extractFileName(ct, disposition));
                byte[] data = decodeBodyBytes(partBody, cte);
                String baseCt = ctLower.contains(";")
                        ? ctLower.substring(0, ctLower.indexOf(';')).trim()
                        : ctLower;
                boolean inline = dispLower.startsWith("inline");
                String contentId = hdrs.get("content-id");
                attachments.add(new ParsedMessage.ParsedAttachment(
                        fileName, baseCt, data, inline, stripAngleBrackets(contentId)));
            } else if (ctLower.startsWith("text/html")) {
                Charset cs = extractCharset(ct);
                if (html == null) {
                    html = decodeBody(partBody, cte, cs);
                }
            } else if (ctLower.startsWith("text/")) {
                Charset cs = extractCharset(ct);
                if (text == null) {
                    text = decodeBody(partBody, cte, cs);
                }
            } else {
                // Unknown content type with no disposition → treat as attachment
                String fileName = sanitizeFileName(extractFileName(ct, disposition));
                byte[] data = decodeBodyBytes(partBody, cte);
                String baseCt = ctLower.contains(";")
                        ? ctLower.substring(0, ctLower.indexOf(';')).trim()
                        : ctLower;
                String contentId = hdrs.get("content-id");
                attachments.add(new ParsedMessage.ParsedAttachment(
                        fileName, baseCt, data, false, stripAngleBrackets(contentId)));
            }
        }

        return new BodyResult(text, html, attachments);
    }

    // ========== Internal: Content Decoding ==========

    /**
     * Decode body text using the specified transfer encoding and charset.
     */
    private static String decodeBody(String body, String transferEncoding, Charset charset) {
        String teLower = transferEncoding.toLowerCase(Locale.ROOT).trim();
        return switch (teLower) {
            case "base64" -> {
                byte[] decoded = MimeEncoder.decodeBase64(body.replaceAll("\\s+", ""));
                yield new String(decoded, charset);
            }
            case "quoted-printable" -> MimeEncoder.decodeQuotedPrintable(body, charset);
            default -> body; // 7bit, 8bit, binary → pass through
        };
    }

    /**
     * Decode body to raw bytes using the specified transfer encoding.
     */
    private static byte[] decodeBodyBytes(String body, String transferEncoding) {
        String teLower = transferEncoding.toLowerCase(Locale.ROOT).trim();
        return switch (teLower) {
            case "base64" -> MimeEncoder.decodeBase64(body.replaceAll("\\s+", ""));
            case "quoted-printable" ->
                    MimeEncoder.decodeQuotedPrintable(body).getBytes(StandardCharsets.UTF_8);
            default -> body.getBytes(StandardCharsets.UTF_8);
        };
    }

    // ========== Internal: Content-Type Helpers ==========

    /**
     * Extract the boundary parameter from a Content-Type header value.
     */
    private static String extractBoundary(String contentType) {
        Matcher m = BOUNDARY_PATTERN.matcher(contentType);
        return m.find() ? m.group(1) : null;
    }

    /**
     * Extract the charset parameter from a Content-Type header, defaulting to UTF-8.
     */
    private static Charset extractCharset(String contentType) {
        Matcher m = CHARSET_PATTERN.matcher(contentType);
        if (m.find()) {
            try {
                return Charset.forName(m.group(1));
            } catch (Exception ignored) {
                // Fall through to default
            }
        }
        return StandardCharsets.UTF_8;
    }

    /**
     * Extract the file name from Content-Type name= or Content-Disposition filename= parameter.
     */
    private static String extractFileName(String contentType, String disposition) {
        // Try Content-Disposition filename first (higher priority)
        if (disposition != null && !disposition.isEmpty()) {
            Matcher m = NAME_PATTERN.matcher(disposition);
            if (m.find()) {
                return MimeEncoder.decodeWord(m.group(1));
            }
        }
        // Try Content-Type name
        if (contentType != null && !contentType.isEmpty()) {
            Matcher m = NAME_PATTERN.matcher(contentType);
            if (m.find()) {
                return MimeEncoder.decodeWord(m.group(1));
            }
        }
        return null;
    }

    /**
     * Sanitize attachment filename to prevent path traversal.
     * Strips directory components and rejects dangerous characters.
     * 清理附件文件名以防止路径遍历攻击。
     * 剥离目录组件并拒绝危险字符。
     *
     * @param fileName the raw filename from MIME headers | MIME 头中的原始文件名
     * @return the sanitized filename, or null if input was null/blank | 清理后的文件名，输入为空时返回 null
     */
    private static String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return fileName;
        }
        // Strip any directory path components (both / and \)
        int lastSlash = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
        if (lastSlash >= 0) {
            fileName = fileName.substring(lastSlash + 1);
        }
        // Remove .. sequences
        fileName = fileName.replace("..", "");
        // Remove control characters
        StringBuilder sb = new StringBuilder(fileName.length());
        for (int i = 0; i < fileName.length(); i++) {
            char c = fileName.charAt(i);
            if (c >= 0x20 && c != 0x7F) { // printable chars only
                sb.append(c);
            }
        }
        String result = sb.toString().trim();
        return result.isEmpty() ? "attachment" : result;
    }

    /**
     * Strip angle brackets from a value like "<id@domain>".
     */
    private static String stripAngleBrackets(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.startsWith("<") && trimmed.endsWith(">")) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }
}
