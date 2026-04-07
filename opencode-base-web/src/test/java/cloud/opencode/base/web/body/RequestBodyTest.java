package cloud.opencode.base.web.body;

import cloud.opencode.base.web.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RequestBody")
class RequestBodyTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("empty()")
    class EmptyBody {

        @Test
        @DisplayName("should create body with null content type")
        void shouldCreateEmptyBody() {
            RequestBody body = RequestBody.empty();
            assertThat(body.getContentType()).isNull();
        }

        @Test
        @DisplayName("should have content length -1")
        void shouldHaveNegativeContentLength() {
            RequestBody body = RequestBody.empty();
            assertThat(body.getContentLength()).isEqualTo(-1);
        }

        @Test
        @DisplayName("should have non-null publisher")
        void shouldHavePublisher() {
            RequestBody body = RequestBody.empty();
            assertThat(body.getBodyPublisher()).isNotNull();
        }
    }

    @Nested
    @DisplayName("of(String, String)")
    class OfStringContentType {

        @Test
        @DisplayName("should create body with content and content type")
        void shouldCreate() {
            RequestBody body = RequestBody.of("hello", "text/plain");
            assertThat(body.getContentType()).isEqualTo("text/plain");
            assertThat(body.getContentLength()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("of(String, String, Charset)")
    class OfStringContentTypeCharset {

        @Test
        @DisplayName("should use specified charset")
        void shouldUseCharset() {
            RequestBody body = RequestBody.of("hello", "text/plain", StandardCharsets.UTF_16);
            assertThat(body.getContentType()).isEqualTo("text/plain");
            assertThat(body.getContentLength()).isGreaterThan(5); // UTF-16 is larger
        }
    }

    @Nested
    @DisplayName("of(byte[], String)")
    class OfBytesContentType {

        @Test
        @DisplayName("should create body from bytes")
        void shouldCreateFromBytes() {
            byte[] data = {1, 2, 3, 4, 5};
            RequestBody body = RequestBody.of(data, "application/octet-stream");
            assertThat(body.getContentType()).isEqualTo("application/octet-stream");
            assertThat(body.getContentLength()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("json(String)")
    class Json {

        @Test
        @DisplayName("should create JSON body")
        void shouldCreateJsonBody() {
            RequestBody body = RequestBody.json("{\"a\":1}");
            assertThat(body.getContentType()).contains("application/json");
            assertThat(body.getContentLength()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("text(String)")
    class TextBody {

        @Test
        @DisplayName("should create text body")
        void shouldCreateTextBody() {
            RequestBody body = RequestBody.text("hello");
            assertThat(body.getContentType()).contains("text/plain");
            assertThat(body.getContentLength()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("xml(String)")
    class XmlBody {

        @Test
        @DisplayName("should create XML body")
        void shouldCreateXmlBody() {
            RequestBody body = RequestBody.xml("<root/>");
            assertThat(body.getContentType()).contains("application/xml");
        }
    }

    @Nested
    @DisplayName("file(Path)")
    class FileBodyFactory {

        @Test
        @DisplayName("should create file body with auto-detected content type")
        void shouldCreateFileBody() throws Exception {
            Path file = tempDir.resolve("test.json");
            Files.writeString(file, "{}");
            RequestBody body = RequestBody.file(file);
            assertThat(body.getContentType()).isEqualTo(ContentType.APPLICATION_JSON);
            assertThat(body).isInstanceOf(FileBody.class);
        }
    }

    @Nested
    @DisplayName("file(Path, String)")
    class FileBodyFactoryWithContentType {

        @Test
        @DisplayName("should create file body with explicit content type")
        void shouldCreateFileBodyWithType() throws Exception {
            Path file = tempDir.resolve("data.bin");
            Files.write(file, new byte[]{1});
            RequestBody body = RequestBody.file(file, "application/custom");
            assertThat(body.getContentType()).isEqualTo("application/custom");
        }
    }

    @Nested
    @DisplayName("SimpleBody record")
    class SimpleBodyTest {

        @Test
        @DisplayName("should implement RequestBody interface")
        void shouldImplementRequestBody() {
            RequestBody body = RequestBody.json("{}");
            assertThat(body.getContentType()).isNotNull();
            assertThat(body.getBodyPublisher()).isNotNull();
            assertThat(body.getContentLength()).isGreaterThanOrEqualTo(0);
        }
    }

    @Nested
    @DisplayName("default getContentLength()")
    class DefaultContentLength {

        @Test
        @DisplayName("should return -1 by default")
        void shouldReturnNegativeOne() {
            RequestBody body = new RequestBody() {
                @Override
                public String getContentType() { return "text/plain"; }
                @Override
                public java.net.http.HttpRequest.BodyPublisher getBodyPublisher() {
                    return java.net.http.HttpRequest.BodyPublishers.noBody();
                }
            };
            assertThat(body.getContentLength()).isEqualTo(-1);
        }
    }
}
