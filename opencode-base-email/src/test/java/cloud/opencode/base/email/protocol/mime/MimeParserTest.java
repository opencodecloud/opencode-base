package cloud.opencode.base.email.protocol.mime;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link MimeParser}.
 */
class MimeParserTest {

    // ========== Simple Text Messages ==========

    @Nested
    class SimpleTextMessageTests {

        private static final String SIMPLE_TEXT_MSG =
                "From: sender@example.com\r\n" +
                "To: recipient@example.com\r\n" +
                "Subject: Test Subject\r\n" +
                "Date: Mon, 01 Jan 2024 12:00:00 +0000\r\n" +
                "Message-ID: <test123@example.com>\r\n" +
                "MIME-Version: 1.0\r\n" +
                "Content-Type: text/plain; charset=UTF-8\r\n" +
                "\r\n" +
                "Hello World";

        @Test
        void shouldParseFrom() {
            ParsedMessage msg = MimeParser.parse(SIMPLE_TEXT_MSG);
            assertThat(msg.from()).isEqualTo("sender@example.com");
        }

        @Test
        void shouldParseTo() {
            ParsedMessage msg = MimeParser.parse(SIMPLE_TEXT_MSG);
            assertThat(msg.to()).containsExactly("recipient@example.com");
        }

        @Test
        void shouldParseSubject() {
            ParsedMessage msg = MimeParser.parse(SIMPLE_TEXT_MSG);
            assertThat(msg.subject()).isEqualTo("Test Subject");
        }

        @Test
        void shouldParseMessageId() {
            ParsedMessage msg = MimeParser.parse(SIMPLE_TEXT_MSG);
            assertThat(msg.messageId()).isEqualTo("test123@example.com");
        }

        @Test
        void shouldParseSentDate() {
            ParsedMessage msg = MimeParser.parse(SIMPLE_TEXT_MSG);
            assertThat(msg.sentDate()).isNotNull();
        }

        @Test
        void shouldParseTextContent() {
            ParsedMessage msg = MimeParser.parse(SIMPLE_TEXT_MSG);
            assertThat(msg.textContent()).isEqualTo("Hello World");
        }

        @Test
        void shouldHaveNullHtmlContent() {
            ParsedMessage msg = MimeParser.parse(SIMPLE_TEXT_MSG);
            assertThat(msg.htmlContent()).isNull();
        }

        @Test
        void shouldHaveNoAttachments() {
            ParsedMessage msg = MimeParser.parse(SIMPLE_TEXT_MSG);
            assertThat(msg.attachments()).isEmpty();
        }

        @Test
        void shouldRecordSize() {
            ParsedMessage msg = MimeParser.parse(SIMPLE_TEXT_MSG);
            assertThat(msg.size()).isEqualTo(SIMPLE_TEXT_MSG.length());
        }
    }

    // ========== Simple HTML Messages ==========

    @Nested
    class SimpleHtmlMessageTests {

        private static final String HTML_MSG =
                "From: sender@example.com\r\n" +
                "To: recipient@example.com\r\n" +
                "Subject: HTML Test\r\n" +
                "MIME-Version: 1.0\r\n" +
                "Content-Type: text/html; charset=UTF-8\r\n" +
                "\r\n" +
                "<html><body><h1>Hello</h1></body></html>";

        @Test
        void shouldParseHtmlContent() {
            ParsedMessage msg = MimeParser.parse(HTML_MSG);
            assertThat(msg.htmlContent()).isEqualTo("<html><body><h1>Hello</h1></body></html>");
        }

        @Test
        void shouldHaveNullTextContent() {
            ParsedMessage msg = MimeParser.parse(HTML_MSG);
            assertThat(msg.textContent()).isNull();
        }
    }

    // ========== Multipart Alternative ==========

    @Nested
    class MultipartAlternativeTests {

        private static final String MULTIPART_ALT_MSG =
                "From: sender@example.com\r\n" +
                "To: recipient@example.com\r\n" +
                "Subject: Multipart Alternative\r\n" +
                "MIME-Version: 1.0\r\n" +
                "Content-Type: multipart/alternative; boundary=\"boundary123\"\r\n" +
                "\r\n" +
                "--boundary123\r\n" +
                "Content-Type: text/plain; charset=UTF-8\r\n" +
                "\r\n" +
                "Plain text version\r\n" +
                "--boundary123\r\n" +
                "Content-Type: text/html; charset=UTF-8\r\n" +
                "\r\n" +
                "<p>HTML version</p>\r\n" +
                "--boundary123--\r\n";

        @Test
        void shouldExtractTextContent() {
            ParsedMessage msg = MimeParser.parse(MULTIPART_ALT_MSG);
            assertThat(msg.textContent()).isEqualTo("Plain text version");
        }

        @Test
        void shouldExtractHtmlContent() {
            ParsedMessage msg = MimeParser.parse(MULTIPART_ALT_MSG);
            assertThat(msg.htmlContent()).isEqualTo("<p>HTML version</p>");
        }

        @Test
        void shouldHaveNoAttachments() {
            ParsedMessage msg = MimeParser.parse(MULTIPART_ALT_MSG);
            assertThat(msg.attachments()).isEmpty();
        }
    }

    // ========== Multipart Mixed ==========

    @Nested
    class MultipartMixedTests {

        private static final String ATTACHMENT_DATA = "Hello from attachment";
        private static final String ATTACHMENT_BASE64 =
                Base64.getEncoder().encodeToString(ATTACHMENT_DATA.getBytes(StandardCharsets.UTF_8));

        private static final String MULTIPART_MIXED_MSG =
                "From: sender@example.com\r\n" +
                "To: recipient@example.com\r\n" +
                "Subject: With Attachment\r\n" +
                "MIME-Version: 1.0\r\n" +
                "Content-Type: multipart/mixed; boundary=\"mixboundary\"\r\n" +
                "\r\n" +
                "--mixboundary\r\n" +
                "Content-Type: text/plain; charset=UTF-8\r\n" +
                "\r\n" +
                "Message body\r\n" +
                "--mixboundary\r\n" +
                "Content-Type: application/octet-stream; name=\"test.txt\"\r\n" +
                "Content-Disposition: attachment; filename=\"test.txt\"\r\n" +
                "Content-Transfer-Encoding: base64\r\n" +
                "\r\n" +
                ATTACHMENT_BASE64 + "\r\n" +
                "--mixboundary--\r\n";

        @Test
        void shouldExtractBodyText() {
            ParsedMessage msg = MimeParser.parse(MULTIPART_MIXED_MSG);
            assertThat(msg.textContent()).isEqualTo("Message body");
        }

        @Test
        void shouldExtractOneAttachment() {
            ParsedMessage msg = MimeParser.parse(MULTIPART_MIXED_MSG);
            assertThat(msg.attachments()).hasSize(1);
        }

        @Test
        void shouldExtractAttachmentFileName() {
            ParsedMessage msg = MimeParser.parse(MULTIPART_MIXED_MSG);
            assertThat(msg.attachments().getFirst().fileName()).isEqualTo("test.txt");
        }

        @Test
        void shouldExtractAttachmentContentType() {
            ParsedMessage msg = MimeParser.parse(MULTIPART_MIXED_MSG);
            assertThat(msg.attachments().getFirst().contentType()).isEqualTo("application/octet-stream");
        }

        @Test
        void shouldDecodeAttachmentData() {
            ParsedMessage msg = MimeParser.parse(MULTIPART_MIXED_MSG);
            String decoded = new String(msg.attachments().getFirst().data(), StandardCharsets.UTF_8);
            assertThat(decoded).isEqualTo(ATTACHMENT_DATA);
        }

        @Test
        void shouldMarkAttachmentAsNotInline() {
            ParsedMessage msg = MimeParser.parse(MULTIPART_MIXED_MSG);
            assertThat(msg.attachments().getFirst().inline()).isFalse();
        }
    }

    // ========== Multipart Related ==========

    @Nested
    class MultipartRelatedTests {

        private static final byte[] IMAGE_DATA = {(byte) 0x89, 'P', 'N', 'G'};
        private static final String IMAGE_BASE64 = Base64.getEncoder().encodeToString(IMAGE_DATA);

        private static final String MULTIPART_RELATED_MSG =
                "From: sender@example.com\r\n" +
                "To: recipient@example.com\r\n" +
                "Subject: Inline Image\r\n" +
                "MIME-Version: 1.0\r\n" +
                "Content-Type: multipart/related; boundary=\"relbound\"\r\n" +
                "\r\n" +
                "--relbound\r\n" +
                "Content-Type: text/html; charset=UTF-8\r\n" +
                "\r\n" +
                "<html><body><img src=\"cid:img1\"/></body></html>\r\n" +
                "--relbound\r\n" +
                "Content-Type: image/png; name=\"logo.png\"\r\n" +
                "Content-Disposition: inline; filename=\"logo.png\"\r\n" +
                "Content-Transfer-Encoding: base64\r\n" +
                "Content-ID: <img1>\r\n" +
                "\r\n" +
                IMAGE_BASE64 + "\r\n" +
                "--relbound--\r\n";

        @Test
        void shouldExtractHtmlContent() {
            ParsedMessage msg = MimeParser.parse(MULTIPART_RELATED_MSG);
            assertThat(msg.htmlContent()).contains("<img src=\"cid:img1\"/>");
        }

        @Test
        void shouldExtractInlineAttachment() {
            ParsedMessage msg = MimeParser.parse(MULTIPART_RELATED_MSG);
            assertThat(msg.attachments()).hasSize(1);
            assertThat(msg.attachments().getFirst().inline()).isTrue();
        }

        @Test
        void shouldExtractContentId() {
            ParsedMessage msg = MimeParser.parse(MULTIPART_RELATED_MSG);
            assertThat(msg.attachments().getFirst().contentId()).isEqualTo("img1");
        }

        @Test
        void shouldExtractInlineFileName() {
            ParsedMessage msg = MimeParser.parse(MULTIPART_RELATED_MSG);
            assertThat(msg.attachments().getFirst().fileName()).isEqualTo("logo.png");
        }

        @Test
        void shouldDecodeInlineData() {
            ParsedMessage msg = MimeParser.parse(MULTIPART_RELATED_MSG);
            assertThat(msg.attachments().getFirst().data()).isEqualTo(IMAGE_DATA);
        }
    }

    // ========== Header Parsing ==========

    @Nested
    class HeaderParsingTests {

        @Test
        void shouldParseDisplayNameAndEmail() {
            String raw =
                    "From: \"John Doe\" <john@example.com>\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: Test\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "\r\n" +
                    "body";
            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.from()).isEqualTo("john@example.com");
            assertThat(msg.fromName()).isEqualTo("John Doe");
        }

        @Test
        void shouldParseBareEmailFrom() {
            String raw =
                    "From: bare@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: Test\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "\r\n" +
                    "body";
            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.from()).isEqualTo("bare@example.com");
            assertThat(msg.fromName()).isNull();
        }

        @Test
        void shouldParseMultipleToAddresses() {
            String raw =
                    "From: sender@example.com\r\n" +
                    "To: a@example.com, \"Bob\" <b@example.com>, c@example.com\r\n" +
                    "Subject: Test\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "\r\n" +
                    "body";
            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.to()).containsExactly("a@example.com", "b@example.com", "c@example.com");
        }

        @Test
        void shouldParseCcAddresses() {
            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "CC: cc1@example.com, cc2@example.com\r\n" +
                    "Subject: Test\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "\r\n" +
                    "body";
            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.cc()).containsExactly("cc1@example.com", "cc2@example.com");
        }

        @Test
        void shouldParseBccAddresses() {
            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "BCC: secret@example.com\r\n" +
                    "Subject: Test\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "\r\n" +
                    "body";
            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.bcc()).containsExactly("secret@example.com");
        }

        @Test
        void shouldReturnEmptyListWhenNoCC() {
            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: Test\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "\r\n" +
                    "body";
            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.cc()).isEmpty();
            assertThat(msg.bcc()).isEmpty();
        }

        @Test
        void shouldParseEncodedSubject() {
            String encodedSubject = MimeEncoder.encodeWord("\u4F60\u597D", "UTF-8");
            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: " + encodedSubject + "\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "\r\n" +
                    "body";
            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.subject()).isEqualTo("\u4F60\u597D");
        }

        @Test
        void shouldParseReplyTo() {
            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Reply-To: reply@example.com\r\n" +
                    "Subject: Test\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "\r\n" +
                    "body";
            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.replyTo()).isEqualTo("reply@example.com");
        }

        @Test
        void shouldParseCustomHeaders() {
            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "X-Custom-Header: custom-value\r\n" +
                    "Subject: Test\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "\r\n" +
                    "body";
            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.headers()).containsEntry("X-Custom-Header", "custom-value");
        }

        @Test
        void shouldParseDateWithDayOfWeek() {
            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: Test\r\n" +
                    "Date: Wed, 15 Jan 2025 10:30:00 +0800\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "\r\n" +
                    "body";
            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.sentDate()).isNotNull();
        }

        @Test
        void shouldParseDateWithoutDayOfWeek() {
            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: Test\r\n" +
                    "Date: 15 Jan 2025 10:30:00 +0000\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "\r\n" +
                    "body";
            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.sentDate()).isNotNull();
        }

        @Test
        void shouldParseDateWithTimezoneComment() {
            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: Test\r\n" +
                    "Date: Wed, 15 Jan 2025 10:30:00 +0000 (UTC)\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "\r\n" +
                    "body";
            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.sentDate()).isNotNull();
        }

        @Test
        void shouldParseSingleDigitDay() {
            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: Test\r\n" +
                    "Date: Sun, 5 Jan 2025 08:00:00 +0000\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "\r\n" +
                    "body";
            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.sentDate()).isNotNull();
        }

        @Test
        void shouldReturnNullForMissingDate() {
            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: Test\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "\r\n" +
                    "body";
            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.sentDate()).isNull();
        }

        @Test
        void shouldReturnNullForUnparseableDate() {
            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: Test\r\n" +
                    "Date: not-a-date\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "\r\n" +
                    "body";
            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.sentDate()).isNull();
        }

        @Test
        void shouldParseReceivedHeader() {
            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: Test\r\n" +
                    "Received: from mail.example.com; Wed, 15 Jan 2025 10:30:00 +0000\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "\r\n" +
                    "body";
            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.receivedDate()).isNotNull();
        }

        @Test
        void shouldReturnNullReceivedDateWhenMissing() {
            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: Test\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "\r\n" +
                    "body";
            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.receivedDate()).isNull();
        }

        @Test
        void shouldParseMessageIdWithAngleBrackets() {
            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: Test\r\n" +
                    "Message-ID: <unique123@mail.example.com>\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "\r\n" +
                    "body";
            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.messageId()).isEqualTo("unique123@mail.example.com");
        }
    }

    // ========== parseHeaders (public method) ==========

    @Nested
    class ParseHeadersMethodTests {

        @Test
        void shouldThrowOnNull() {
            assertThatThrownBy(() -> MimeParser.parseHeaders(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldThrowOnEmpty() {
            assertThatThrownBy(() -> MimeParser.parseHeaders(""))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldPreserveHeaderCase() {
            String raw =
                    "From: sender@example.com\r\n" +
                    "X-Custom-Header: value\r\n" +
                    "\r\n" +
                    "body";
            Map<String, String> headers = MimeParser.parseHeaders(raw);
            assertThat(headers).containsKey("From");
            assertThat(headers).containsKey("X-Custom-Header");
        }

        @Test
        void shouldUnfoldContinuationLines() {
            String raw =
                    "Subject: This is a very long\r\n" +
                    " subject that spans multiple lines\r\n" +
                    "\r\n" +
                    "body";
            Map<String, String> headers = MimeParser.parseHeaders(raw);
            assertThat(headers.get("Subject"))
                    .isEqualTo("This is a very long subject that spans multiple lines");
        }

        @Test
        void shouldMergeDuplicateHeaders() {
            String raw =
                    "Received: from server1\r\n" +
                    "Received: from server2\r\n" +
                    "\r\n" +
                    "body";
            Map<String, String> headers = MimeParser.parseHeaders(raw);
            assertThat(headers.get("Received")).contains("server1").contains("server2");
        }
    }

    // ========== Attachments ==========

    @Nested
    class AttachmentTests {

        @Test
        void shouldParseBase64Attachment() {
            byte[] fileData = "File content here".getBytes(StandardCharsets.UTF_8);
            String base64 = Base64.getEncoder().encodeToString(fileData);

            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: Test\r\n" +
                    "MIME-Version: 1.0\r\n" +
                    "Content-Type: multipart/mixed; boundary=\"att\"\r\n" +
                    "\r\n" +
                    "--att\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "\r\n" +
                    "Body\r\n" +
                    "--att\r\n" +
                    "Content-Type: application/pdf; name=\"doc.pdf\"\r\n" +
                    "Content-Disposition: attachment; filename=\"doc.pdf\"\r\n" +
                    "Content-Transfer-Encoding: base64\r\n" +
                    "\r\n" +
                    base64 + "\r\n" +
                    "--att--\r\n";

            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.attachments()).hasSize(1);
            ParsedMessage.ParsedAttachment att = msg.attachments().getFirst();
            assertThat(att.fileName()).isEqualTo("doc.pdf");
            assertThat(att.contentType()).isEqualTo("application/pdf");
            assertThat(att.data()).isEqualTo(fileData);
            assertThat(att.inline()).isFalse();
        }

        @Test
        void shouldParseMultipleAttachments() {
            String b64a = Base64.getEncoder().encodeToString("AAA".getBytes(StandardCharsets.UTF_8));
            String b64b = Base64.getEncoder().encodeToString("BBB".getBytes(StandardCharsets.UTF_8));

            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: Test\r\n" +
                    "MIME-Version: 1.0\r\n" +
                    "Content-Type: multipart/mixed; boundary=\"multi\"\r\n" +
                    "\r\n" +
                    "--multi\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "\r\n" +
                    "Body\r\n" +
                    "--multi\r\n" +
                    "Content-Type: text/csv; name=\"a.csv\"\r\n" +
                    "Content-Disposition: attachment; filename=\"a.csv\"\r\n" +
                    "Content-Transfer-Encoding: base64\r\n" +
                    "\r\n" +
                    b64a + "\r\n" +
                    "--multi\r\n" +
                    "Content-Type: text/csv; name=\"b.csv\"\r\n" +
                    "Content-Disposition: attachment; filename=\"b.csv\"\r\n" +
                    "Content-Transfer-Encoding: base64\r\n" +
                    "\r\n" +
                    b64b + "\r\n" +
                    "--multi--\r\n";

            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.attachments()).hasSize(2);
            assertThat(msg.attachments().get(0).fileName()).isEqualTo("a.csv");
            assertThat(msg.attachments().get(1).fileName()).isEqualTo("b.csv");
        }

        @Test
        void shouldExtractFileNameFromContentTypeName() {
            // When Content-Disposition has no filename, fall back to Content-Type name
            String b64 = Base64.getEncoder().encodeToString("data".getBytes(StandardCharsets.UTF_8));

            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: Test\r\n" +
                    "MIME-Version: 1.0\r\n" +
                    "Content-Type: multipart/mixed; boundary=\"fb\"\r\n" +
                    "\r\n" +
                    "--fb\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "\r\n" +
                    "Body\r\n" +
                    "--fb\r\n" +
                    "Content-Type: application/zip; name=\"archive.zip\"\r\n" +
                    "Content-Disposition: attachment\r\n" +
                    "Content-Transfer-Encoding: base64\r\n" +
                    "\r\n" +
                    b64 + "\r\n" +
                    "--fb--\r\n";

            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.attachments().getFirst().fileName()).isEqualTo("archive.zip");
        }

        @Test
        void shouldParseQuotedPrintableAttachment() {
            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: Test\r\n" +
                    "MIME-Version: 1.0\r\n" +
                    "Content-Type: multipart/mixed; boundary=\"qp\"\r\n" +
                    "\r\n" +
                    "--qp\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "\r\n" +
                    "Body\r\n" +
                    "--qp\r\n" +
                    "Content-Type: text/plain; name=\"note.txt\"\r\n" +
                    "Content-Disposition: attachment; filename=\"note.txt\"\r\n" +
                    "Content-Transfer-Encoding: quoted-printable\r\n" +
                    "\r\n" +
                    "caf=C3=A9\r\n" +
                    "--qp--\r\n";

            ParsedMessage msg = MimeParser.parse(raw);
            String decoded = new String(msg.attachments().getFirst().data(), StandardCharsets.UTF_8);
            assertThat(decoded).isEqualTo("caf\u00E9");
        }
    }

    // ========== Content Transfer Encoding ==========

    @Nested
    class ContentTransferEncodingTests {

        @Test
        void shouldDecodeBase64Body() {
            String base64Body = Base64.getMimeEncoder().encodeToString(
                    "Decoded text".getBytes(StandardCharsets.UTF_8));
            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: Test\r\n" +
                    "Content-Type: text/plain; charset=UTF-8\r\n" +
                    "Content-Transfer-Encoding: base64\r\n" +
                    "\r\n" +
                    base64Body;
            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.textContent()).isEqualTo("Decoded text");
        }

        @Test
        void shouldDecodeQuotedPrintableBody() {
            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: Test\r\n" +
                    "Content-Type: text/plain; charset=UTF-8\r\n" +
                    "Content-Transfer-Encoding: quoted-printable\r\n" +
                    "\r\n" +
                    "caf=C3=A9";
            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.textContent()).isEqualTo("caf\u00E9");
        }

        @Test
        void shouldPassThrough7bitBody() {
            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: Test\r\n" +
                    "Content-Type: text/plain; charset=UTF-8\r\n" +
                    "Content-Transfer-Encoding: 7bit\r\n" +
                    "\r\n" +
                    "Plain 7bit text";
            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.textContent()).isEqualTo("Plain 7bit text");
        }

        @Test
        void shouldPassThrough8bitBody() {
            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: Test\r\n" +
                    "Content-Type: text/plain; charset=UTF-8\r\n" +
                    "Content-Transfer-Encoding: 8bit\r\n" +
                    "\r\n" +
                    "Plain 8bit text";
            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.textContent()).isEqualTo("Plain 8bit text");
        }

        @Test
        void shouldDefaultTo7bitWhenNoEncoding() {
            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: Test\r\n" +
                    "Content-Type: text/plain; charset=UTF-8\r\n" +
                    "\r\n" +
                    "Default encoding text";
            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.textContent()).isEqualTo("Default encoding text");
        }

        @Test
        void shouldDecodeBase64HtmlBody() {
            String base64Html = Base64.getMimeEncoder().encodeToString(
                    "<p>Hello</p>".getBytes(StandardCharsets.UTF_8));
            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: Test\r\n" +
                    "Content-Type: text/html; charset=UTF-8\r\n" +
                    "Content-Transfer-Encoding: base64\r\n" +
                    "\r\n" +
                    base64Html;
            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.htmlContent()).isEqualTo("<p>Hello</p>");
        }
    }

    // ========== Edge Cases ==========

    @Nested
    class EdgeCaseTests {

        @Test
        void shouldThrowOnNullMessage() {
            assertThatThrownBy(() -> MimeParser.parse(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null or empty");
        }

        @Test
        void shouldThrowOnEmptyMessage() {
            assertThatThrownBy(() -> MimeParser.parse(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null or empty");
        }

        @Test
        void shouldHandleHeadersOnlyNoBody() {
            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: No body";
            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.from()).isEqualTo("sender@example.com");
            // Body defaults to text/plain with empty string
            assertThat(msg.textContent()).isEmpty();
        }

        @Test
        void shouldHandleFoldedHeaders() {
            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: This is a very long subject\r\n" +
                    " that has been folded across\r\n" +
                    "\tmultiple lines\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "\r\n" +
                    "body";
            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.subject())
                    .isEqualTo("This is a very long subject that has been folded across multiple lines");
        }

        @Test
        void shouldHandleBareLfLineEndings() {
            String raw =
                    "From: sender@example.com\n" +
                    "To: recipient@example.com\n" +
                    "Subject: Bare LF\n" +
                    "Content-Type: text/plain\n" +
                    "\n" +
                    "Body with bare LF";
            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.subject()).isEqualTo("Bare LF");
            assertThat(msg.textContent()).isEqualTo("Body with bare LF");
        }

        @Test
        void shouldHandleNestedMultipart() {
            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: Nested\r\n" +
                    "MIME-Version: 1.0\r\n" +
                    "Content-Type: multipart/mixed; boundary=\"outer\"\r\n" +
                    "\r\n" +
                    "--outer\r\n" +
                    "Content-Type: multipart/alternative; boundary=\"inner\"\r\n" +
                    "\r\n" +
                    "--inner\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "\r\n" +
                    "Nested text\r\n" +
                    "--inner\r\n" +
                    "Content-Type: text/html\r\n" +
                    "\r\n" +
                    "<p>Nested HTML</p>\r\n" +
                    "--inner--\r\n" +
                    "\r\n" +
                    "--outer--\r\n";

            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.textContent()).isEqualTo("Nested text");
            assertThat(msg.htmlContent()).isEqualTo("<p>Nested HTML</p>");
        }

        @Test
        void shouldHandleUnknownContentTypeAsAttachment() {
            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: Binary\r\n" +
                    "MIME-Version: 1.0\r\n" +
                    "Content-Type: application/octet-stream\r\n" +
                    "\r\n" +
                    "binary data";
            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.attachments()).hasSize(1);
            assertThat(msg.attachments().getFirst().contentType()).isEqualTo("application/octet-stream");
        }

        @Test
        void shouldDefaultContentTypeToTextPlain() {
            // No Content-Type header at all
            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: No CT\r\n" +
                    "\r\n" +
                    "Default text";
            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.textContent()).isEqualTo("Default text");
        }

        @Test
        void shouldHandleMultipartWithNoBoundary() {
            // multipart but no boundary param => no parts extracted
            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: Test\r\n" +
                    "Content-Type: multipart/mixed\r\n" +
                    "\r\n" +
                    "some body";
            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.textContent()).isNull();
            assertThat(msg.htmlContent()).isNull();
        }

        @Test
        void shouldHandleMultipartWithNoBoundaryInBody() {
            // Boundary declared but not present in body
            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: Test\r\n" +
                    "Content-Type: multipart/mixed; boundary=\"missing\"\r\n" +
                    "\r\n" +
                    "no boundaries here";
            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.textContent()).isNull();
        }

        @Test
        void shouldHandleEmptyFrom() {
            String raw =
                    "To: recipient@example.com\r\n" +
                    "Subject: No from\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "\r\n" +
                    "body";
            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.from()).isEmpty();
            assertThat(msg.fromName()).isNull();
        }

        @Test
        void shouldHandlePartWithUnknownContentTypeInMultipart() {
            String b64 = Base64.getEncoder().encodeToString("data".getBytes(StandardCharsets.UTF_8));
            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: Test\r\n" +
                    "MIME-Version: 1.0\r\n" +
                    "Content-Type: multipart/mixed; boundary=\"unk\"\r\n" +
                    "\r\n" +
                    "--unk\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "\r\n" +
                    "Body\r\n" +
                    "--unk\r\n" +
                    "Content-Type: application/x-custom\r\n" +
                    "Content-Transfer-Encoding: base64\r\n" +
                    "\r\n" +
                    b64 + "\r\n" +
                    "--unk--\r\n";

            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.attachments()).hasSize(1);
            assertThat(msg.attachments().getFirst().contentType()).isEqualTo("application/x-custom");
        }

        @Test
        void shouldHandleInlineNonTextInMultipart() {
            // Inline disposition with non-text type should be treated as attachment
            byte[] data = {1, 2, 3};
            String b64 = Base64.getEncoder().encodeToString(data);

            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: Test\r\n" +
                    "MIME-Version: 1.0\r\n" +
                    "Content-Type: multipart/mixed; boundary=\"inl\"\r\n" +
                    "\r\n" +
                    "--inl\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "\r\n" +
                    "Body\r\n" +
                    "--inl\r\n" +
                    "Content-Type: image/jpeg; name=\"photo.jpg\"\r\n" +
                    "Content-Disposition: inline; filename=\"photo.jpg\"\r\n" +
                    "Content-Transfer-Encoding: base64\r\n" +
                    "Content-ID: <photo1>\r\n" +
                    "\r\n" +
                    b64 + "\r\n" +
                    "--inl--\r\n";

            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.attachments()).hasSize(1);
            assertThat(msg.attachments().getFirst().inline()).isTrue();
            assertThat(msg.attachments().getFirst().contentId()).isEqualTo("photo1");
        }

        @Test
        void shouldHandleTopLevelBinaryWithInlineDisposition() {
            byte[] data = {10, 20, 30};
            String b64 = Base64.getEncoder().encodeToString(data);

            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: Test\r\n" +
                    "Content-Type: image/png\r\n" +
                    "Content-Disposition: inline; filename=\"img.png\"\r\n" +
                    "Content-Transfer-Encoding: base64\r\n" +
                    "Content-ID: <cidimg>\r\n" +
                    "\r\n" +
                    b64;

            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.attachments()).hasSize(1);
            assertThat(msg.attachments().getFirst().inline()).isTrue();
            assertThat(msg.attachments().getFirst().contentId()).isEqualTo("cidimg");
            assertThat(msg.attachments().getFirst().fileName()).isEqualTo("img.png");
            assertThat(msg.attachments().getFirst().data()).isEqualTo(data);
        }

        @Test
        void shouldHandleCharsetInContentType() {
            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: Test\r\n" +
                    "Content-Type: text/plain; charset=ISO-8859-1\r\n" +
                    "Content-Transfer-Encoding: quoted-printable\r\n" +
                    "\r\n" +
                    "caf=E9";
            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.textContent()).isEqualTo("caf\u00E9");
        }

        @Test
        void shouldHandleInvalidCharsetGracefully() {
            // Invalid charset should default to UTF-8
            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: Test\r\n" +
                    "Content-Type: text/plain; charset=INVALID-CHARSET-XYZ\r\n" +
                    "\r\n" +
                    "Hello";
            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.textContent()).isEqualTo("Hello");
        }
    }

    // ========== Sanitize File Name ==========

    @Nested
    class SanitizeFileNameTests {

        @Test
        void shouldPreserveNormalFileName() {
            String raw = buildAttachmentMsg("report.pdf");
            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.attachments().getFirst().fileName()).isEqualTo("report.pdf");
        }

        @Test
        void shouldStripPathTraversalForwardSlash() {
            String raw = buildAttachmentMsg("../../etc/passwd");
            ParsedMessage msg = MimeParser.parse(raw);
            String name = msg.attachments().getFirst().fileName();
            assertThat(name).doesNotContain("/");
            assertThat(name).doesNotContain("..");
        }

        @Test
        void shouldStripBackslashPaths() {
            String raw = buildAttachmentMsg("dir\\file.txt");
            ParsedMessage msg = MimeParser.parse(raw);
            String name = msg.attachments().getFirst().fileName();
            assertThat(name).doesNotContain("\\");
            assertThat(name).isEqualTo("file.txt");
        }

        @Test
        void shouldRemoveControlCharacters() {
            String raw = buildAttachmentMsg("file\u0001name.txt");
            ParsedMessage msg = MimeParser.parse(raw);
            String name = msg.attachments().getFirst().fileName();
            assertThat(name).doesNotContain("\u0001");
            assertThat(name).isEqualTo("filename.txt");
        }

        @Test
        void shouldHandleNullFileName() {
            // No name or filename param => null filename passed to sanitize => null returned
            String b64 = Base64.getEncoder().encodeToString("x".getBytes(StandardCharsets.UTF_8));
            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: Test\r\n" +
                    "MIME-Version: 1.0\r\n" +
                    "Content-Type: multipart/mixed; boundary=\"nn\"\r\n" +
                    "\r\n" +
                    "--nn\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "\r\n" +
                    "Body\r\n" +
                    "--nn\r\n" +
                    "Content-Type: application/octet-stream\r\n" +
                    "Content-Disposition: attachment\r\n" +
                    "Content-Transfer-Encoding: base64\r\n" +
                    "\r\n" +
                    b64 + "\r\n" +
                    "--nn--\r\n";

            ParsedMessage msg = MimeParser.parse(raw);
            // null filename stays null after sanitize
            assertThat(msg.attachments().getFirst().fileName()).isNull();
        }

        @Test
        void shouldReturnAttachmentForDotsOnlyName() {
            // ".." after stripping path components and removing ".." becomes empty => "attachment"
            String raw = buildAttachmentMsg("..");
            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.attachments().getFirst().fileName()).isEqualTo("attachment");
        }

        private String buildAttachmentMsg(String fileName) {
            String b64 = Base64.getEncoder().encodeToString("data".getBytes(StandardCharsets.UTF_8));
            return "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: Test\r\n" +
                    "MIME-Version: 1.0\r\n" +
                    "Content-Type: multipart/mixed; boundary=\"san\"\r\n" +
                    "\r\n" +
                    "--san\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "\r\n" +
                    "Body\r\n" +
                    "--san\r\n" +
                    "Content-Type: application/octet-stream\r\n" +
                    "Content-Disposition: attachment; filename=\"" + fileName + "\"\r\n" +
                    "Content-Transfer-Encoding: base64\r\n" +
                    "\r\n" +
                    b64 + "\r\n" +
                    "--san--\r\n";
        }
    }

    // ========== Date without seconds format ==========

    @Nested
    class DateFormatTests {

        @Test
        void shouldParseDateWithoutSeconds() {
            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: Test\r\n" +
                    "Date: Wed, 15 Jan 2025 10:30 +0000\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "\r\n" +
                    "body";
            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.sentDate()).isNotNull();
        }

        @Test
        void shouldParseTwoDigitYear() {
            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: Test\r\n" +
                    "Date: Wed, 15 Jan 25 10:30:00 +0000\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "\r\n" +
                    "body";
            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.sentDate()).isNotNull();
        }

        @Test
        void shouldParseReceivedDateAfterSemicolon() {
            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: Test\r\n" +
                    "Received: from mx.example.com by mail.example.com; Wed, 15 Jan 2025 10:30:00 +0000\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "\r\n" +
                    "body";
            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.receivedDate()).isNotNull();
        }

        @Test
        void shouldReturnNullForReceivedWithNoSemicolon() {
            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: Test\r\n" +
                    "Received: from mx.example.com by mail.example.com\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "\r\n" +
                    "body";
            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.receivedDate()).isNull();
        }
    }

    // ========== Content-Type with semicolon in base type extraction ==========

    @Nested
    class ContentTypeExtractionTests {

        @Test
        void shouldExtractBaseContentTypeWithParams() {
            // top-level non-text type with params like "; name=..." should extract base type
            byte[] data = {1, 2, 3};
            String b64 = Base64.getEncoder().encodeToString(data);

            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: Test\r\n" +
                    "Content-Type: image/jpeg; name=\"photo.jpg\"\r\n" +
                    "Content-Transfer-Encoding: base64\r\n" +
                    "\r\n" +
                    b64;

            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.attachments().getFirst().contentType()).isEqualTo("image/jpeg");
        }

        @Test
        void shouldExtractBoundaryFromQuotedValue() {
            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: Test\r\n" +
                    "MIME-Version: 1.0\r\n" +
                    "Content-Type: multipart/alternative; boundary=\"my-boundary\"\r\n" +
                    "\r\n" +
                    "--my-boundary\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "\r\n" +
                    "Text\r\n" +
                    "--my-boundary--\r\n";

            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.textContent()).isEqualTo("Text");
        }

        @Test
        void shouldExtractBoundaryFromUnquotedValue() {
            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: Test\r\n" +
                    "MIME-Version: 1.0\r\n" +
                    "Content-Type: multipart/alternative; boundary=simple_boundary\r\n" +
                    "\r\n" +
                    "--simple_boundary\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "\r\n" +
                    "Unquoted\r\n" +
                    "--simple_boundary--\r\n";

            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.textContent()).isEqualTo("Unquoted");
        }
    }

    // ========== Strip Angle Brackets ==========

    @Nested
    class StripAngleBracketsTests {

        @Test
        void shouldStripBrackets() {
            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: Test\r\n" +
                    "Message-ID: <abc@def.com>\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "\r\n" +
                    "body";
            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.messageId()).isEqualTo("abc@def.com");
        }

        @Test
        void shouldHandleMessageIdWithoutBrackets() {
            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: Test\r\n" +
                    "Message-ID: abc@def.com\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "\r\n" +
                    "body";
            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.messageId()).isEqualTo("abc@def.com");
        }

        @Test
        void shouldHandleNullMessageId() {
            String raw =
                    "From: sender@example.com\r\n" +
                    "To: recipient@example.com\r\n" +
                    "Subject: Test\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "\r\n" +
                    "body";
            ParsedMessage msg = MimeParser.parse(raw);
            assertThat(msg.messageId()).isNull();
        }
    }
}
