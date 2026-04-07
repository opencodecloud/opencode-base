package cloud.opencode.base.io.stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

/**
 * ReaderInputStream 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.3
 */
@DisplayName("ReaderInputStream 测试")
class ReaderInputStreamTest {

    @Nested
    @DisplayName("基本读取测试")
    class BasicReadTests {

        @Test
        @DisplayName("读取ASCII文本")
        void testReadAsciiText() throws IOException {
            String text = "Hello World";
            try (InputStream is = new ReaderInputStream(new StringReader(text))) {
                byte[] result = is.readAllBytes();
                assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo(text);
            }
        }

        @Test
        @DisplayName("读取UTF-8多字节字符 — 中文")
        void testReadUtf8Chinese() throws IOException {
            String text = "你好世界";
            try (InputStream is = new ReaderInputStream(new StringReader(text), StandardCharsets.UTF_8)) {
                byte[] result = is.readAllBytes();
                assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo(text);
            }
        }

        @Test
        @DisplayName("读取UTF-8多字节字符 — emoji")
        void testReadUtf8Emoji() throws IOException {
            String text = "Hello \uD83D\uDE00 World";
            try (InputStream is = new ReaderInputStream(new StringReader(text), StandardCharsets.UTF_8)) {
                byte[] result = is.readAllBytes();
                assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo(text);
            }
        }

        @Test
        @DisplayName("读取空Reader")
        void testReadEmptyReader() throws IOException {
            try (InputStream is = new ReaderInputStream(new StringReader(""))) {
                byte[] result = is.readAllBytes();
                assertThat(result).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("逐字节读取测试")
    class ByteByByteTests {

        @Test
        @DisplayName("逐字节读取ASCII")
        void testReadByteByByte() throws IOException {
            String text = "ABC";
            try (InputStream is = new ReaderInputStream(new StringReader(text))) {
                assertThat(is.read()).isEqualTo('A');
                assertThat(is.read()).isEqualTo('B');
                assertThat(is.read()).isEqualTo('C');
                assertThat(is.read()).isEqualTo(-1);
            }
        }

        @Test
        @DisplayName("逐字节读取与批量读取结果一致")
        void testByteByByteMatchesBulk() throws IOException {
            String text = "Hello World 你好";

            byte[] bulkResult;
            try (InputStream is = new ReaderInputStream(new StringReader(text))) {
                bulkResult = is.readAllBytes();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (InputStream is = new ReaderInputStream(new StringReader(text))) {
                int b;
                while ((b = is.read()) != -1) {
                    baos.write(b);
                }
            }

            assertThat(baos.toByteArray()).isEqualTo(bulkResult);
        }
    }

    @Nested
    @DisplayName("字符集测试")
    class CharsetTests {

        @Test
        @DisplayName("默认字符集为UTF-8")
        void testDefaultCharsetIsUtf8() throws IOException {
            String text = "你好";
            byte[] expected = text.getBytes(StandardCharsets.UTF_8);

            try (InputStream is = new ReaderInputStream(new StringReader(text))) {
                byte[] result = is.readAllBytes();
                assertThat(result).isEqualTo(expected);
            }
        }

        @Test
        @DisplayName("使用ISO-8859-1字符集")
        void testIso88591Charset() throws IOException {
            String text = "caf\u00e9"; // cafe with accent
            Charset charset = StandardCharsets.ISO_8859_1;
            byte[] expected = text.getBytes(charset);

            try (InputStream is = new ReaderInputStream(new StringReader(text), charset)) {
                byte[] result = is.readAllBytes();
                assertThat(result).isEqualTo(expected);
            }
        }

        @Test
        @DisplayName("使用US-ASCII字符集")
        void testUsAsciiCharset() throws IOException {
            String text = "Hello";
            Charset charset = StandardCharsets.US_ASCII;
            byte[] expected = text.getBytes(charset);

            try (InputStream is = new ReaderInputStream(new StringReader(text), charset)) {
                byte[] result = is.readAllBytes();
                assertThat(result).isEqualTo(expected);
            }
        }
    }

    @Nested
    @DisplayName("关闭传播测试")
    class CloseTests {

        @Test
        @DisplayName("关闭传播到底层Reader")
        void testClosePropagation() throws IOException {
            boolean[] closed = {false};
            StringReader reader = new StringReader("test") {
                @Override
                public void close() {
                    closed[0] = true;
                    super.close();
                }
            };

            InputStream is = new ReaderInputStream(reader);
            is.close();

            assertThat(closed[0]).isTrue();
        }
    }

    @Nested
    @DisplayName("大数据测试")
    class LargeDataTests {

        @Test
        @DisplayName("读取超过缓冲区大小的文本")
        void testLargeText() throws IOException {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 5000; i++) {
                sb.append("Hello World 你好世界 ");
            }
            String text = sb.toString();

            try (InputStream is = new ReaderInputStream(new StringReader(text))) {
                byte[] result = is.readAllBytes();
                assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo(text);
            }
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("读取零字节不应报错")
        void testReadZeroBytes() throws IOException {
            try (InputStream is = new ReaderInputStream(new StringReader("test"))) {
                byte[] buf = new byte[10];
                int read = is.read(buf, 0, 0);
                assertThat(read).isZero();
            }
        }

        @Test
        @DisplayName("构造函数空参数校验")
        void testNullReader() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new ReaderInputStream(null));
        }

        @Test
        @DisplayName("构造函数空charset校验")
        void testNullCharset() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new ReaderInputStream(new StringReader("x"), (Charset) null));
        }
    }
}
