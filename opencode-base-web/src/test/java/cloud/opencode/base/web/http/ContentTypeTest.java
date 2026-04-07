package cloud.opencode.base.web.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ContentType")
class ContentTypeTest {

    @Nested
    @DisplayName("Constants")
    class Constants {

        @Test
        @DisplayName("should define standard MIME types")
        void shouldDefineStandardTypes() {
            assertThat(ContentType.APPLICATION_JSON).isEqualTo("application/json");
            assertThat(ContentType.APPLICATION_XML).isEqualTo("application/xml");
            assertThat(ContentType.TEXT_PLAIN).isEqualTo("text/plain");
            assertThat(ContentType.TEXT_HTML).isEqualTo("text/html");
            assertThat(ContentType.MULTIPART_FORM_DATA).isEqualTo("multipart/form-data");
            assertThat(ContentType.APPLICATION_OCTET_STREAM).isEqualTo("application/octet-stream");
            assertThat(ContentType.IMAGE_PNG).isEqualTo("image/png");
            assertThat(ContentType.IMAGE_JPEG).isEqualTo("image/jpeg");
            assertThat(ContentType.VIDEO_MP4).isEqualTo("video/mp4");
            assertThat(ContentType.AUDIO_MPEG).isEqualTo("audio/mpeg");
        }
    }

    @Nested
    @DisplayName("of(String)")
    class OfMimeType {

        @Test
        @DisplayName("should create with MIME type only")
        void shouldCreateWithMimeOnly() {
            ContentType ct = ContentType.of("text/plain");
            assertThat(ct.getMimeType()).isEqualTo("text/plain");
            assertThat(ct.getCharset()).isNull();
            assertThat(ct.getBoundary()).isNull();
        }
    }

    @Nested
    @DisplayName("of(String, Charset)")
    class OfMimeTypeCharset {

        @Test
        @DisplayName("should create with MIME type and charset")
        void shouldCreateWithCharset() {
            ContentType ct = ContentType.of("text/html", StandardCharsets.UTF_8);
            assertThat(ct.getMimeType()).isEqualTo("text/html");
            assertThat(ct.getCharset()).isEqualTo(StandardCharsets.UTF_8);
        }
    }

    @Nested
    @DisplayName("Factory methods")
    class FactoryMethods {

        @Test
        @DisplayName("json() should create JSON type with UTF-8")
        void jsonShouldCreateJson() {
            ContentType ct = ContentType.json();
            assertThat(ct.getMimeType()).isEqualTo("application/json");
            assertThat(ct.getCharset()).isEqualTo(StandardCharsets.UTF_8);
        }

        @Test
        @DisplayName("xml() should create XML type with UTF-8")
        void xmlShouldCreateXml() {
            ContentType ct = ContentType.xml();
            assertThat(ct.getMimeType()).isEqualTo("application/xml");
            assertThat(ct.getCharset()).isEqualTo(StandardCharsets.UTF_8);
        }

        @Test
        @DisplayName("form() should create form type with UTF-8")
        void formShouldCreateForm() {
            ContentType ct = ContentType.form();
            assertThat(ct.getMimeType()).isEqualTo("application/x-www-form-urlencoded");
            assertThat(ct.getCharset()).isEqualTo(StandardCharsets.UTF_8);
        }

        @Test
        @DisplayName("text() should create text type with UTF-8")
        void textShouldCreateText() {
            ContentType ct = ContentType.text();
            assertThat(ct.getMimeType()).isEqualTo("text/plain");
            assertThat(ct.getCharset()).isEqualTo(StandardCharsets.UTF_8);
        }

        @Test
        @DisplayName("binary() should create octet-stream without charset")
        void binaryShouldCreateBinary() {
            ContentType ct = ContentType.binary();
            assertThat(ct.getMimeType()).isEqualTo("application/octet-stream");
            assertThat(ct.getCharset()).isNull();
        }

        @Test
        @DisplayName("multipart() should create multipart type with boundary")
        void multipartShouldCreateMultipart() {
            ContentType ct = ContentType.multipart("----boundary123");
            assertThat(ct.getMimeType()).isEqualTo("multipart/form-data");
            assertThat(ct.getBoundary()).isEqualTo("----boundary123");
        }
    }

    @Nested
    @DisplayName("parse(String)")
    class Parse {

        @Test
        @DisplayName("should parse MIME type only")
        void shouldParseMimeOnly() {
            ContentType ct = ContentType.parse("application/json");
            assertThat(ct.getMimeType()).isEqualTo("application/json");
            assertThat(ct.getCharset()).isNull();
        }

        @Test
        @DisplayName("should parse MIME type with charset")
        void shouldParseWithCharset() {
            ContentType ct = ContentType.parse("text/html; charset=utf-8");
            assertThat(ct.getMimeType()).isEqualTo("text/html");
            assertThat(ct.getCharset()).isEqualTo(StandardCharsets.UTF_8);
        }

        @Test
        @DisplayName("should parse charset with quotes")
        void shouldParseQuotedCharset() {
            ContentType ct = ContentType.parse("text/html; charset=\"utf-8\"");
            assertThat(ct.getCharset()).isEqualTo(StandardCharsets.UTF_8);
        }

        @Test
        @DisplayName("should parse boundary")
        void shouldParseBoundary() {
            ContentType ct = ContentType.parse("multipart/form-data; boundary=abc123");
            assertThat(ct.getMimeType()).isEqualTo("multipart/form-data");
            assertThat(ct.getBoundary()).isEqualTo("abc123");
        }

        @Test
        @DisplayName("should parse quoted boundary")
        void shouldParseQuotedBoundary() {
            ContentType ct = ContentType.parse("multipart/form-data; boundary=\"abc123\"");
            assertThat(ct.getBoundary()).isEqualTo("abc123");
        }

        @Test
        @DisplayName("should return null for null input")
        void shouldReturnNullForNull() {
            assertThat(ContentType.parse(null)).isNull();
        }

        @Test
        @DisplayName("should return null for blank input")
        void shouldReturnNullForBlank() {
            assertThat(ContentType.parse("  ")).isNull();
        }

        @Test
        @DisplayName("should handle invalid charset gracefully")
        void shouldHandleInvalidCharset() {
            ContentType ct = ContentType.parse("text/html; charset=not-a-charset");
            assertThat(ct.getMimeType()).isEqualTo("text/html");
            assertThat(ct.getCharset()).isNull();
        }
    }

    @Nested
    @DisplayName("getCharsetOrDefault()")
    class GetCharsetOrDefault {

        @Test
        @DisplayName("should return charset when present")
        void shouldReturnCharset() {
            ContentType ct = ContentType.of("text/plain", StandardCharsets.UTF_16);
            assertThat(ct.getCharsetOrDefault(StandardCharsets.UTF_8)).isEqualTo(StandardCharsets.UTF_16);
        }

        @Test
        @DisplayName("should return default when charset is null")
        void shouldReturnDefault() {
            ContentType ct = ContentType.of("text/plain");
            assertThat(ct.getCharsetOrDefault(StandardCharsets.UTF_8)).isEqualTo(StandardCharsets.UTF_8);
        }
    }

    @Nested
    @DisplayName("Type check methods")
    class TypeChecks {

        @Test
        @DisplayName("isJson() should detect JSON types")
        void isJsonShouldDetect() {
            assertThat(ContentType.of("application/json").isJson()).isTrue();
            assertThat(ContentType.of("application/vnd.api+json").isJson()).isTrue();
            assertThat(ContentType.of("text/plain").isJson()).isFalse();
        }

        @Test
        @DisplayName("isXml() should detect XML types")
        void isXmlShouldDetect() {
            assertThat(ContentType.of("application/xml").isXml()).isTrue();
            assertThat(ContentType.of("text/xml").isXml()).isTrue();
            assertThat(ContentType.of("application/soap+xml").isXml()).isTrue();
            assertThat(ContentType.of("text/plain").isXml()).isFalse();
        }

        @Test
        @DisplayName("isText() should detect text types")
        void isTextShouldDetect() {
            assertThat(ContentType.of("text/plain").isText()).isTrue();
            assertThat(ContentType.of("text/html").isText()).isTrue();
            assertThat(ContentType.of("application/json").isText()).isFalse();
        }

        @Test
        @DisplayName("isMultipart() should detect multipart types")
        void isMultipartShouldDetect() {
            assertThat(ContentType.of("multipart/form-data").isMultipart()).isTrue();
            assertThat(ContentType.of("multipart/mixed").isMultipart()).isTrue();
            assertThat(ContentType.of("text/plain").isMultipart()).isFalse();
        }

        @Test
        @DisplayName("isForm() should detect form type")
        void isFormShouldDetect() {
            assertThat(ContentType.of("application/x-www-form-urlencoded").isForm()).isTrue();
            assertThat(ContentType.of("text/plain").isForm()).isFalse();
        }
    }

    @Nested
    @DisplayName("toString()")
    class ToStringTest {

        @Test
        @DisplayName("should return MIME type only when no charset or boundary")
        void shouldReturnMimeOnly() {
            assertThat(ContentType.of("text/plain").toString()).isEqualTo("text/plain");
        }

        @Test
        @DisplayName("should include charset when present")
        void shouldIncludeCharset() {
            assertThat(ContentType.json().toString()).isEqualTo("application/json; charset=utf-8");
        }

        @Test
        @DisplayName("should include boundary when present")
        void shouldIncludeBoundary() {
            String result = ContentType.multipart("abc").toString();
            assertThat(result).isEqualTo("multipart/form-data; boundary=abc");
        }
    }
}
