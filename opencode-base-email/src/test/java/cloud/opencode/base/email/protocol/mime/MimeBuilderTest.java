package cloud.opencode.base.email.protocol.mime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for MimeBuilder
 *
 * @author Leon Soo
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.3
 */
@DisplayName("MimeBuilder")
class MimeBuilderTest {

    private static final String DOMAIN = "example.com";
    private static final String FROM = "sender@example.com";
    private static final List<String> TO = List.of("recipient@example.com");
    private static final String SUBJECT = "Test Subject";

    // ========== Helper ==========

    /**
     * Build a message with minimal defaults for simple cases.
     */
    private static String buildSimple(
            String textContent, String htmlContent, boolean htmlFlag, String content,
            List<MimeBuilder.AttachmentData> attachments) {
        return MimeBuilder.buildMessage(
                FROM, null, TO, null, null, null,
                SUBJECT, textContent, htmlContent, htmlFlag, content,
                attachments, null, 3, DOMAIN);
    }

    // ========== Case 1: Simple Text ==========

    @Nested
    @DisplayName("Simple text message (Case 1)")
    class SimpleTextMessageTests {

        @Test
        @DisplayName("should contain text/plain Content-Type")
        void shouldContainTextPlainContentType() {
            String msg = buildSimple("Hello World", null, false, null, null);

            assertThat(msg).contains("Content-Type: text/plain; charset=UTF-8");
        }

        @Test
        @DisplayName("should contain standard headers")
        void shouldContainStandardHeaders() {
            String msg = buildSimple("Hello World", null, false, null, null);

            assertThat(msg).contains("MIME-Version: 1.0");
            assertThat(msg).contains("Date: ");
            assertThat(msg).contains("Message-ID: ");
            assertThat(msg).contains("From: " + FROM);
            assertThat(msg).contains("To: recipient@example.com");
            assertThat(msg).contains("Subject: " + SUBJECT);
        }

        @Test
        @DisplayName("should contain quoted-printable encoded body")
        void shouldContainQuotedPrintableBody() {
            String msg = buildSimple("Hello World", null, false, null, null);

            assertThat(msg).contains("Content-Transfer-Encoding: quoted-printable");
            assertThat(msg).contains("Hello World");
        }

        @Test
        @DisplayName("should use content field with htmlFlag=false as text")
        void shouldUseContentFieldAsText() {
            String msg = buildSimple(null, null, false, "Plain text via content", null);

            assertThat(msg).contains("Content-Type: text/plain; charset=UTF-8");
            assertThat(msg).contains("Plain text via content");
        }

        @Test
        @DisplayName("should produce empty body when all content is null")
        void shouldProduceEmptyBodyWhenAllNull() {
            String msg = buildSimple(null, null, false, null, null);

            assertThat(msg).contains("Content-Type: text/plain; charset=UTF-8");
        }
    }

    // ========== Case 2: Simple HTML ==========

    @Nested
    @DisplayName("Simple HTML message (Case 2)")
    class SimpleHtmlMessageTests {

        @Test
        @DisplayName("should contain text/html Content-Type")
        void shouldContainTextHtmlContentType() {
            String msg = buildSimple(null, "<h1>Hello</h1>", false, null, null);

            assertThat(msg).contains("Content-Type: text/html; charset=UTF-8");
        }

        @Test
        @DisplayName("should contain HTML body content")
        void shouldContainHtmlBody() {
            String msg = buildSimple(null, "<h1>Hello</h1>", false, null, null);

            assertThat(msg).contains("<h1>Hello</h1>");
        }

        @Test
        @DisplayName("should use content field with htmlFlag=true as HTML")
        void shouldUseContentFieldAsHtml() {
            String msg = buildSimple(null, null, true, "<p>HTML via content</p>", null);

            assertThat(msg).contains("Content-Type: text/html; charset=UTF-8");
            assertThat(msg).contains("<p>HTML via content</p>");
        }
    }

    // ========== Case 3: Multipart/Alternative ==========

    @Nested
    @DisplayName("Multipart alternative message (Case 3)")
    class MultipartAlternativeTests {

        @Test
        @DisplayName("should contain multipart/alternative Content-Type")
        void shouldContainMultipartAlternative() {
            String msg = buildSimple("Plain text", "<h1>HTML</h1>", false, null, null);

            assertThat(msg).contains("Content-Type: multipart/alternative;");
            assertThat(msg).contains("boundary=");
        }

        @Test
        @DisplayName("should contain both text/plain and text/html parts")
        void shouldContainBothParts() {
            String msg = buildSimple("Plain text", "<h1>HTML</h1>", false, null, null);

            assertThat(msg).contains("Content-Type: text/plain; charset=UTF-8");
            assertThat(msg).contains("Content-Type: text/html; charset=UTF-8");
            assertThat(msg).contains("Plain text");
            assertThat(msg).contains("<h1>HTML</h1>");
        }

        @Test
        @DisplayName("should have boundary delimiters and closing boundary")
        void shouldHaveBoundaryDelimiters() {
            String msg = buildSimple("Text", "<b>Bold</b>", false, null, null);

            // Extract boundary from Content-Type header
            String boundary = extractBoundary(msg);
            assertThat(boundary).isNotNull();
            assertThat(msg).contains("--" + boundary);
            assertThat(msg).contains("--" + boundary + "--");
        }

        @Test
        @DisplayName("should use content as text fallback when htmlContent is set and content differs")
        void shouldUseContentAsTextFallback() {
            // htmlContent set, content provided (not html), textContent null
            String msg = MimeBuilder.buildMessage(
                    FROM, null, TO, null, null, null,
                    SUBJECT, null, "<h1>HTML</h1>", false, "Plain fallback",
                    null, null, 3, DOMAIN);

            assertThat(msg).contains("multipart/alternative");
            assertThat(msg).contains("Plain fallback");
            assertThat(msg).contains("<h1>HTML</h1>");
        }
    }

    // ========== Case 4: With Regular Attachments ==========

    @Nested
    @DisplayName("Message with regular attachments (Case 4)")
    class WithAttachmentsTests {

        private final byte[] pdfData = "fake pdf content".getBytes(StandardCharsets.UTF_8);
        private final MimeBuilder.AttachmentData pdfAttachment =
                new MimeBuilder.AttachmentData("test.pdf", "application/pdf", pdfData, false, null);

        @Test
        @DisplayName("should contain multipart/mixed Content-Type")
        void shouldContainMultipartMixed() {
            String msg = buildSimple("Hello", null, false, null, List.of(pdfAttachment));

            assertThat(msg).contains("Content-Type: multipart/mixed;");
        }

        @Test
        @DisplayName("should contain attachment with base64 encoding")
        void shouldContainBase64Attachment() {
            String msg = buildSimple("Hello", null, false, null, List.of(pdfAttachment));

            assertThat(msg).contains("Content-Transfer-Encoding: base64");
            String expectedBase64 = Base64.getMimeEncoder().encodeToString(pdfData);
            assertThat(msg).contains(expectedBase64);
        }

        @Test
        @DisplayName("should contain correct filename and Content-Disposition: attachment")
        void shouldContainCorrectFilenameAndDisposition() {
            String msg = buildSimple("Hello", null, false, null, List.of(pdfAttachment));

            assertThat(msg).contains("Content-Disposition: attachment; filename=\"test.pdf\"");
            assertThat(msg).contains("Content-Type: application/pdf; name=\"test.pdf\"");
        }

        @Test
        @DisplayName("should contain multiple attachments")
        void shouldContainMultipleAttachments() {
            byte[] txtData = "text data".getBytes(StandardCharsets.UTF_8);
            MimeBuilder.AttachmentData txtAttachment =
                    new MimeBuilder.AttachmentData("readme.txt", "text/plain", txtData, false, null);

            String msg = buildSimple("Body", null, false, null, List.of(pdfAttachment, txtAttachment));

            assertThat(msg).contains("filename=\"test.pdf\"");
            assertThat(msg).contains("filename=\"readme.txt\"");
        }

        @Test
        @DisplayName("should wrap alternative content inside mixed when both text and html present")
        void shouldWrapAlternativeInMixed() {
            String msg = buildSimple("Text", "<b>HTML</b>", false, null, List.of(pdfAttachment));

            assertThat(msg).contains("Content-Type: multipart/mixed;");
            assertThat(msg).contains("Content-Type: multipart/alternative;");
            assertThat(msg).contains("Content-Type: text/plain; charset=UTF-8");
            assertThat(msg).contains("Content-Type: text/html; charset=UTF-8");
        }
    }

    // ========== Case 5: With Inline Attachments ==========

    @Nested
    @DisplayName("Message with inline attachments (Case 5)")
    class WithInlineAttachmentsTests {

        private final byte[] imageBytes = "fake png data".getBytes(StandardCharsets.UTF_8);
        private final MimeBuilder.AttachmentData inlineImage =
                new MimeBuilder.AttachmentData("image.png", "image/png", imageBytes, true, "img001");

        @Test
        @DisplayName("should contain multipart/related Content-Type")
        void shouldContainMultipartRelated() {
            String msg = buildSimple(null, "<img src='cid:img001'>", false, null, List.of(inlineImage));

            assertThat(msg).contains("Content-Type: multipart/related;");
        }

        @Test
        @DisplayName("should contain Content-ID header on inline part")
        void shouldContainContentId() {
            String msg = buildSimple(null, "<img src='cid:img001'>", false, null, List.of(inlineImage));

            assertThat(msg).contains("Content-ID: <img001>");
        }

        @Test
        @DisplayName("should contain Content-Disposition: inline")
        void shouldContainInlineDisposition() {
            String msg = buildSimple(null, "<img src='cid:img001'>", false, null, List.of(inlineImage));

            assertThat(msg).contains("Content-Disposition: inline; filename=\"image.png\"");
        }

        @Test
        @DisplayName("should wrap alternative in related when both text and html present")
        void shouldWrapAlternativeInRelated() {
            String msg = buildSimple("Text", "<img src='cid:img001'>", false, null, List.of(inlineImage));

            assertThat(msg).contains("Content-Type: multipart/related;");
            assertThat(msg).contains("Content-Type: multipart/alternative;");
        }

        @Test
        @DisplayName("should not add angle brackets if contentId already has them")
        void shouldNotDoubleWrapContentId() {
            MimeBuilder.AttachmentData withBrackets =
                    new MimeBuilder.AttachmentData("img.png", "image/png", imageBytes, true, "<already@wrapped>");

            String msg = buildSimple(null, "<html>", false, null, List.of(withBrackets));

            assertThat(msg).contains("Content-ID: <already@wrapped>");
            // Should NOT contain double angle brackets
            assertThat(msg).doesNotContain("Content-ID: <<already@wrapped>>");
        }

        @Test
        @DisplayName("should omit Content-ID when contentId is null")
        void shouldOmitContentIdWhenNull() {
            MimeBuilder.AttachmentData noContentId =
                    new MimeBuilder.AttachmentData("img.png", "image/png", imageBytes, true, null);

            String msg = buildSimple(null, "<html>", false, null, List.of(noContentId));

            // The inline part should exist but without Content-ID
            assertThat(msg).contains("Content-Disposition: inline");
            // Count occurrences of Content-ID - should be 0
            assertThat(msg).doesNotContain("Content-ID:");
        }

        @Test
        @DisplayName("should omit Content-ID when contentId is empty")
        void shouldOmitContentIdWhenEmpty() {
            MimeBuilder.AttachmentData emptyContentId =
                    new MimeBuilder.AttachmentData("img.png", "image/png", imageBytes, true, "");

            String msg = buildSimple(null, "<html>", false, null, List.of(emptyContentId));

            assertThat(msg).doesNotContain("Content-ID:");
        }
    }

    // ========== Case 6: Full Combined ==========

    @Nested
    @DisplayName("Full combined message (Case 6)")
    class FullCombinedTests {

        private final byte[] imageBytes = "fake png".getBytes(StandardCharsets.UTF_8);
        private final byte[] pdfBytes = "fake pdf".getBytes(StandardCharsets.UTF_8);
        private final MimeBuilder.AttachmentData inlineImage =
                new MimeBuilder.AttachmentData("logo.png", "image/png", imageBytes, true, "logo1");
        private final MimeBuilder.AttachmentData regularPdf =
                new MimeBuilder.AttachmentData("report.pdf", "application/pdf", pdfBytes, false, null);

        @Test
        @DisplayName("should contain multipart/mixed at top level")
        void shouldContainMultipartMixedTopLevel() {
            String msg = buildSimple("Text body", "<h1>HTML body</h1>", false, null,
                    List.of(inlineImage, regularPdf));

            assertThat(msg).contains("Content-Type: multipart/mixed;");
        }

        @Test
        @DisplayName("should contain multipart/related nested inside mixed")
        void shouldContainMultipartRelatedNested() {
            String msg = buildSimple("Text body", "<h1>HTML body</h1>", false, null,
                    List.of(inlineImage, regularPdf));

            assertThat(msg).contains("Content-Type: multipart/related;");
        }

        @Test
        @DisplayName("should contain multipart/alternative nested inside related")
        void shouldContainMultipartAlternativeNested() {
            String msg = buildSimple("Text body", "<h1>HTML body</h1>", false, null,
                    List.of(inlineImage, regularPdf));

            assertThat(msg).contains("Content-Type: multipart/alternative;");
        }

        @Test
        @DisplayName("should contain all content types and parts")
        void shouldContainAllParts() {
            String msg = buildSimple("Text body", "<h1>HTML body</h1>", false, null,
                    List.of(inlineImage, regularPdf));

            assertThat(msg).contains("Content-Type: text/plain; charset=UTF-8");
            assertThat(msg).contains("Content-Type: text/html; charset=UTF-8");
            assertThat(msg).contains("Content-Disposition: inline; filename=\"logo.png\"");
            assertThat(msg).contains("Content-ID: <logo1>");
            assertThat(msg).contains("Content-Disposition: attachment; filename=\"report.pdf\"");
        }

        @Test
        @DisplayName("should contain text and HTML content")
        void shouldContainTextAndHtmlContent() {
            String msg = buildSimple("Text body", "<h1>HTML body</h1>", false, null,
                    List.of(inlineImage, regularPdf));

            assertThat(msg).contains("Text body");
            assertThat(msg).contains("<h1>HTML body</h1>");
        }

        @Test
        @DisplayName("should have closing boundaries for all multipart levels")
        void shouldHaveClosingBoundaries() {
            String msg = buildSimple("Text", "<b>Bold</b>", false, null,
                    List.of(inlineImage, regularPdf));

            // Count closing boundaries (--boundary--)
            long closingCount = msg.lines()
                    .filter(line -> line.matches("^--.*--$"))
                    .count();
            // Should have at least 3: alternative, related, mixed
            assertThat(closingCount).isGreaterThanOrEqualTo(3);
        }

        @Test
        @DisplayName("should work with only HTML and both attachment types (no text)")
        void shouldWorkWithHtmlOnlyAndBothAttachmentTypes() {
            String msg = buildSimple(null, "<h1>HTML only</h1>", false, null,
                    List.of(inlineImage, regularPdf));

            assertThat(msg).contains("Content-Type: multipart/mixed;");
            assertThat(msg).contains("Content-Type: multipart/related;");
            assertThat(msg).contains("Content-Type: text/html; charset=UTF-8");
            assertThat(msg).contains("Content-Disposition: attachment; filename=\"report.pdf\"");
        }
    }

    // ========== Header Tests ==========

    @Nested
    @DisplayName("Header generation")
    class HeaderTests {

        @Test
        @DisplayName("should encode From with display name using RFC 2047")
        void shouldEncodeFromWithDisplayName() {
            String msg = MimeBuilder.buildMessage(
                    FROM, "John Doe", TO, null, null, null,
                    SUBJECT, "Body", null, false, null,
                    null, null, 3, DOMAIN);

            // ASCII name does not get encoded
            assertThat(msg).contains("From: John Doe <sender@example.com>");
        }

        @Test
        @DisplayName("should RFC 2047 encode non-ASCII display name")
        void shouldEncodeNonAsciiDisplayName() {
            String msg = MimeBuilder.buildMessage(
                    FROM, "\u5F20\u4E09", TO, null, null, null,
                    SUBJECT, "Body", null, false, null,
                    null, null, 3, DOMAIN);

            // Non-ASCII name should be encoded as =?UTF-8?B?...?=
            assertThat(msg).contains("From: =?UTF-8?B?");
            assertThat(msg).contains("<sender@example.com>");
        }

        @Test
        @DisplayName("should RFC 2047 encode non-ASCII subject")
        void shouldEncodeNonAsciiSubject() {
            String msg = MimeBuilder.buildMessage(
                    FROM, null, TO, null, null, null,
                    "\u6D4B\u8BD5\u4E3B\u9898", "Body", null, false, null,
                    null, null, 3, DOMAIN);

            assertThat(msg).contains("Subject: =?UTF-8?B?");
        }

        @Test
        @DisplayName("should include CC header")
        void shouldIncludeCcHeader() {
            String msg = MimeBuilder.buildMessage(
                    FROM, null, TO, List.of("cc1@example.com", "cc2@example.com"),
                    null, null, SUBJECT, "Body", null, false, null,
                    null, null, 3, DOMAIN);

            assertThat(msg).contains("Cc: cc1@example.com, cc2@example.com");
        }

        @Test
        @DisplayName("should include BCC header")
        void shouldIncludeBccHeader() {
            String msg = MimeBuilder.buildMessage(
                    FROM, null, TO, null,
                    List.of("secret@example.com"), null, SUBJECT, "Body", null, false, null,
                    null, null, 3, DOMAIN);

            assertThat(msg).contains("Bcc: secret@example.com");
        }

        @Test
        @DisplayName("should include Reply-To header")
        void shouldIncludeReplyToHeader() {
            String msg = MimeBuilder.buildMessage(
                    FROM, null, TO, null, null, "reply@example.com",
                    SUBJECT, "Body", null, false, null,
                    null, null, 3, DOMAIN);

            assertThat(msg).contains("Reply-To: reply@example.com");
        }

        @Test
        @DisplayName("should include X-Priority header for HIGH priority (1)")
        void shouldIncludeHighPriority() {
            String msg = MimeBuilder.buildMessage(
                    FROM, null, TO, null, null, null,
                    SUBJECT, "Body", null, false, null,
                    null, null, 1, DOMAIN);

            assertThat(msg).contains("X-Priority: 1");
        }

        @Test
        @DisplayName("should include X-Priority header for LOW priority (5)")
        void shouldIncludeLowPriority() {
            String msg = MimeBuilder.buildMessage(
                    FROM, null, TO, null, null, null,
                    SUBJECT, "Body", null, false, null,
                    null, null, 5, DOMAIN);

            assertThat(msg).contains("X-Priority: 5");
        }

        @Test
        @DisplayName("should omit X-Priority header for NORMAL priority (3)")
        void shouldOmitNormalPriority() {
            String msg = MimeBuilder.buildMessage(
                    FROM, null, TO, null, null, null,
                    SUBJECT, "Body", null, false, null,
                    null, null, 3, DOMAIN);

            assertThat(msg).doesNotContain("X-Priority:");
        }

        @Test
        @DisplayName("should include custom headers")
        void shouldIncludeCustomHeaders() {
            Map<String, String> headers = new LinkedHashMap<>();
            headers.put("X-Custom-Header", "CustomValue");
            headers.put("X-Mailer", "OpenCode");

            String msg = MimeBuilder.buildMessage(
                    FROM, null, TO, null, null, null,
                    SUBJECT, "Body", null, false, null,
                    null, headers, 3, DOMAIN);

            assertThat(msg).contains("X-Custom-Header: CustomValue");
            assertThat(msg).contains("X-Mailer: OpenCode");
        }

        @Test
        @DisplayName("should omit CC/BCC/Reply-To when null")
        void shouldOmitNullHeaders() {
            String msg = buildSimple("Body", null, false, null, null);

            assertThat(msg).doesNotContain("Cc:");
            assertThat(msg).doesNotContain("Bcc:");
            assertThat(msg).doesNotContain("Reply-To:");
        }

        @Test
        @DisplayName("should omit CC/BCC when empty list")
        void shouldOmitEmptyListHeaders() {
            String msg = MimeBuilder.buildMessage(
                    FROM, null, TO, List.of(), List.of(), null,
                    SUBJECT, "Body", null, false, null,
                    null, null, 3, DOMAIN);

            assertThat(msg).doesNotContain("Cc:");
            assertThat(msg).doesNotContain("Bcc:");
        }
    }

    // ========== GetMessageId Tests ==========

    @Nested
    @DisplayName("getMessageId()")
    class GetMessageIdTests {

        @Test
        @DisplayName("should extract Message-ID from built message")
        void shouldExtractMessageId() {
            String msg = buildSimple("Hello", null, false, null, null);

            String messageId = MimeBuilder.getMessageId(msg);

            assertThat(messageId).isNotNull();
            assertThat(messageId).startsWith("<");
            assertThat(messageId).endsWith("@" + DOMAIN + ">");
        }

        @Test
        @DisplayName("should return format <uuid@domain>")
        void shouldReturnCorrectFormat() {
            String msg = buildSimple("Hello", null, false, null, null);

            String messageId = MimeBuilder.getMessageId(msg);

            assertThat(messageId).matches("<[a-f0-9]+@example\\.com>");
        }

        @Test
        @DisplayName("should return null for null input")
        void shouldReturnNullForNullInput() {
            assertThat(MimeBuilder.getMessageId(null)).isNull();
        }

        @Test
        @DisplayName("should return null when Message-ID header is absent")
        void shouldReturnNullWhenAbsent() {
            assertThat(MimeBuilder.getMessageId("From: test@example.com\r\nSubject: Hi\r\n")).isNull();
        }

        @Test
        @DisplayName("should extract Message-ID with different domains")
        void shouldWorkWithDifferentDomains() {
            String msg = MimeBuilder.buildMessage(
                    FROM, null, TO, null, null, null,
                    SUBJECT, "Body", null, false, null,
                    null, null, 3, "mail.opencode.cloud");

            String messageId = MimeBuilder.getMessageId(msg);

            assertThat(messageId).isNotNull();
            assertThat(messageId).endsWith("@mail.opencode.cloud>");
        }
    }

    // ========== Sanitize Tests ==========

    @Nested
    @DisplayName("Header value sanitization")
    class SanitizeTests {

        @Test
        @DisplayName("should strip \\r\\n from subject preventing header injection")
        void shouldStripCrLfFromSubject() {
            String msg = MimeBuilder.buildMessage(
                    FROM, null, TO, null, null, null,
                    "Subject\r\nBcc: injected@evil.com", "Body", null, false, null,
                    null, null, 3, DOMAIN);

            // CRLF removed, so the injected Bcc does NOT appear as a separate header line
            assertThat(msg).doesNotContain("\r\nBcc: injected@evil.com");
            // The sanitized subject concatenates "Subject" + "Bcc: injected@evil.com"
            assertThat(msg).contains("SubjectBcc:");
        }

        @Test
        @DisplayName("should strip \\r\\n from from name")
        void shouldStripCrLfFromFromName() {
            String msg = MimeBuilder.buildMessage(
                    FROM, "Name\r\nBcc: injected@evil.com", TO, null, null, null,
                    SUBJECT, "Body", null, false, null,
                    null, null, 3, DOMAIN);

            // The injected header should not appear on a new line
            assertThat(msg).doesNotContain("\r\nBcc: injected@evil.com");
        }

        @Test
        @DisplayName("should strip \\0 from header values")
        void shouldStripNulFromHeaderValues() {
            String msg = MimeBuilder.buildMessage(
                    FROM, null, TO, null, null, null,
                    "Test\0Subject", "Body", null, false, null,
                    null, null, 3, DOMAIN);

            // Subject should have NUL removed
            assertThat(msg).doesNotContain("\0");
        }

        @Test
        @DisplayName("should strip \\r\\n from custom header values")
        void shouldStripCrLfFromCustomHeaders() {
            Map<String, String> headers = Map.of("X-Custom", "Value\r\nEvil-Header: injected");

            String msg = MimeBuilder.buildMessage(
                    FROM, null, TO, null, null, null,
                    SUBJECT, "Body", null, false, null,
                    null, headers, 3, DOMAIN);

            assertThat(msg).doesNotContain("\r\nEvil-Header:");
        }

        @Test
        @DisplayName("should strip \\r\\n from To addresses")
        void shouldStripCrLfFromToAddresses() {
            String msg = MimeBuilder.buildMessage(
                    FROM, null, List.of("user@example.com\r\nBcc: evil@hacker.com"),
                    null, null, null,
                    SUBJECT, "Body", null, false, null,
                    null, null, 3, DOMAIN);

            assertThat(msg).doesNotContain("\r\nBcc: evil@hacker.com");
        }

        @Test
        @DisplayName("should strip \\r\\n from Reply-To")
        void shouldStripCrLfFromReplyTo() {
            String msg = MimeBuilder.buildMessage(
                    FROM, null, TO, null, null, "reply@example.com\r\nBcc: evil@hacker.com",
                    SUBJECT, "Body", null, false, null,
                    null, null, 3, DOMAIN);

            assertThat(msg).doesNotContain("\r\nBcc: evil@hacker.com");
        }

        @Test
        @DisplayName("should strip \\r\\n from From address")
        void shouldStripCrLfFromFromAddress() {
            String msg = MimeBuilder.buildMessage(
                    "sender@example.com\r\nBcc: evil@hacker.com", null, TO,
                    null, null, null,
                    SUBJECT, "Body", null, false, null,
                    null, null, 3, DOMAIN);

            assertThat(msg).doesNotContain("\r\nBcc: evil@hacker.com");
        }
    }

    // ========== Round Trip Tests ==========

    @Nested
    @DisplayName("Round trip (build + parse)")
    class RoundTripTests {

        @Test
        @DisplayName("should round-trip plain text message")
        void shouldRoundTripPlainText() {
            String raw = buildSimple("Hello World", null, false, null, null);
            ParsedMessage parsed = MimeParser.parse(raw);

            assertThat(parsed.from()).isEqualTo(FROM);
            assertThat(parsed.to()).containsExactly("recipient@example.com");
            assertThat(parsed.subject()).isEqualTo(SUBJECT);
            assertThat(parsed.textContent()).isNotNull();
            assertThat(parsed.textContent().trim()).isEqualTo("Hello World");
        }

        @Test
        @DisplayName("should round-trip HTML message")
        void shouldRoundTripHtml() {
            String raw = buildSimple(null, "<h1>Hello</h1>", false, null, null);
            ParsedMessage parsed = MimeParser.parse(raw);

            assertThat(parsed.htmlContent()).isNotNull();
            assertThat(parsed.htmlContent().trim()).isEqualTo("<h1>Hello</h1>");
        }

        @Test
        @DisplayName("should round-trip multipart/alternative message")
        void shouldRoundTripMultipartAlternative() {
            String raw = buildSimple("Plain text", "<h1>HTML</h1>", false, null, null);
            ParsedMessage parsed = MimeParser.parse(raw);

            assertThat(parsed.textContent()).isNotNull();
            assertThat(parsed.textContent().trim()).isEqualTo("Plain text");
            assertThat(parsed.htmlContent()).isNotNull();
            assertThat(parsed.htmlContent().trim()).isEqualTo("<h1>HTML</h1>");
        }

        @Test
        @DisplayName("should round-trip message with attachment")
        void shouldRoundTripWithAttachment() {
            byte[] data = "PDF content here".getBytes(StandardCharsets.UTF_8);
            MimeBuilder.AttachmentData att =
                    new MimeBuilder.AttachmentData("report.pdf", "application/pdf", data, false, null);

            String raw = buildSimple("Body text", null, false, null, List.of(att));
            ParsedMessage parsed = MimeParser.parse(raw);

            assertThat(parsed.textContent()).isNotNull();
            assertThat(parsed.textContent().trim()).isEqualTo("Body text");
            assertThat(parsed.attachments()).hasSize(1);

            ParsedMessage.ParsedAttachment parsedAtt = parsed.attachments().getFirst();
            assertThat(parsedAtt.fileName()).isEqualTo("report.pdf");
            assertThat(parsedAtt.contentType()).isEqualTo("application/pdf");
            assertThat(parsedAtt.data()).isEqualTo(data);
            assertThat(parsedAtt.inline()).isFalse();
        }

        @Test
        @DisplayName("should round-trip message with inline attachment")
        void shouldRoundTripWithInlineAttachment() {
            byte[] imageData = "PNG data".getBytes(StandardCharsets.UTF_8);
            MimeBuilder.AttachmentData inline =
                    new MimeBuilder.AttachmentData("logo.png", "image/png", imageData, true, "logo1");

            String raw = buildSimple(null, "<img src='cid:logo1'>", false, null, List.of(inline));
            ParsedMessage parsed = MimeParser.parse(raw);

            assertThat(parsed.attachments()).hasSize(1);

            ParsedMessage.ParsedAttachment parsedAtt = parsed.attachments().getFirst();
            assertThat(parsedAtt.fileName()).isEqualTo("logo.png");
            assertThat(parsedAtt.inline()).isTrue();
            assertThat(parsedAtt.contentId()).isEqualTo("logo1");
            assertThat(parsedAtt.data()).isEqualTo(imageData);
        }

        @Test
        @DisplayName("should round-trip full combined message")
        void shouldRoundTripFullCombined() {
            byte[] imageData = "PNG".getBytes(StandardCharsets.UTF_8);
            byte[] pdfData = "PDF".getBytes(StandardCharsets.UTF_8);
            MimeBuilder.AttachmentData inline =
                    new MimeBuilder.AttachmentData("img.png", "image/png", imageData, true, "cid1");
            MimeBuilder.AttachmentData regular =
                    new MimeBuilder.AttachmentData("doc.pdf", "application/pdf", pdfData, false, null);

            String raw = buildSimple("Text", "<h1>HTML</h1>", false, null, List.of(inline, regular));
            ParsedMessage parsed = MimeParser.parse(raw);

            assertThat(parsed.textContent()).isNotNull();
            assertThat(parsed.textContent().trim()).isEqualTo("Text");
            assertThat(parsed.htmlContent()).isNotNull();
            assertThat(parsed.htmlContent().trim()).isEqualTo("<h1>HTML</h1>");
            assertThat(parsed.attachments()).hasSize(2);
        }

        @Test
        @DisplayName("should round-trip non-ASCII subject")
        void shouldRoundTripNonAsciiSubject() {
            String chineseSubject = "\u6D4B\u8BD5\u90AE\u4EF6";
            String raw = MimeBuilder.buildMessage(
                    FROM, null, TO, null, null, null,
                    chineseSubject, "Body", null, false, null,
                    null, null, 3, DOMAIN);
            ParsedMessage parsed = MimeParser.parse(raw);

            assertThat(parsed.subject()).isEqualTo(chineseSubject);
        }

        @Test
        @DisplayName("should round-trip CC and Reply-To")
        void shouldRoundTripCcAndReplyTo() {
            String raw = MimeBuilder.buildMessage(
                    FROM, null, TO,
                    List.of("cc@example.com"), null, "reply@example.com",
                    SUBJECT, "Body", null, false, null,
                    null, null, 3, DOMAIN);
            ParsedMessage parsed = MimeParser.parse(raw);

            assertThat(parsed.cc()).containsExactly("cc@example.com");
            assertThat(parsed.replyTo()).isEqualTo("reply@example.com");
        }

        @Test
        @DisplayName("should round-trip Message-ID")
        void shouldRoundTripMessageId() {
            String raw = buildSimple("Body", null, false, null, null);
            String builtId = MimeBuilder.getMessageId(raw);
            ParsedMessage parsed = MimeParser.parse(raw);

            // MimeParser strips angle brackets from message-id
            assertThat(builtId).isNotNull();
            String expectedId = builtId.startsWith("<") && builtId.endsWith(">")
                    ? builtId.substring(1, builtId.length() - 1) : builtId;
            assertThat(parsed.messageId()).isEqualTo(expectedId);
        }
    }

    // ========== AttachmentData Record Tests ==========

    @Nested
    @DisplayName("AttachmentData record")
    class AttachmentDataTests {

        @Test
        @DisplayName("should store all fields correctly")
        void shouldStoreAllFields() {
            byte[] data = "test".getBytes(StandardCharsets.UTF_8);
            MimeBuilder.AttachmentData att =
                    new MimeBuilder.AttachmentData("file.txt", "text/plain", data, true, "cid123");

            assertThat(att.fileName()).isEqualTo("file.txt");
            assertThat(att.contentType()).isEqualTo("text/plain");
            assertThat(att.data()).isEqualTo(data);
            assertThat(att.inline()).isTrue();
            assertThat(att.contentId()).isEqualTo("cid123");
        }

        @Test
        @DisplayName("should allow null contentId for regular attachments")
        void shouldAllowNullContentId() {
            byte[] data = "test".getBytes(StandardCharsets.UTF_8);
            MimeBuilder.AttachmentData att =
                    new MimeBuilder.AttachmentData("file.txt", "text/plain", data, false, null);

            assertThat(att.contentId()).isNull();
            assertThat(att.inline()).isFalse();
        }
    }

    // ========== Utility ==========

    /**
     * Extract boundary value from a raw MIME message string.
     */
    private static String extractBoundary(String msg) {
        int idx = msg.indexOf("boundary=\"");
        if (idx < 0) {
            return null;
        }
        int start = idx + "boundary=\"".length();
        int end = msg.indexOf('"', start);
        return end > start ? msg.substring(start, end) : null;
    }
}
