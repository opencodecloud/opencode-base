package cloud.opencode.base.io.stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

/**
 * WriterOutputStream 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.3
 */
@DisplayName("WriterOutputStream 测试")
class WriterOutputStreamTest {

    @Nested
    @DisplayName("基本写入测试")
    class BasicWriteTests {

        @Test
        @DisplayName("写入ASCII文本")
        void testWriteAsciiText() throws IOException {
            StringWriter sw = new StringWriter();
            try (OutputStream os = new WriterOutputStream(sw)) {
                os.write("Hello World".getBytes(StandardCharsets.UTF_8));
            }
            assertThat(sw.toString()).isEqualTo("Hello World");
        }

        @Test
        @DisplayName("写入UTF-8多字节字符 — 中文")
        void testWriteUtf8Chinese() throws IOException {
            StringWriter sw = new StringWriter();
            try (OutputStream os = new WriterOutputStream(sw, StandardCharsets.UTF_8)) {
                os.write("你好世界".getBytes(StandardCharsets.UTF_8));
            }
            assertThat(sw.toString()).isEqualTo("你好世界");
        }

        @Test
        @DisplayName("写入UTF-8多字节字符 — 混合")
        void testWriteUtf8Mixed() throws IOException {
            String text = "Hello 你好 World 世界";
            StringWriter sw = new StringWriter();
            try (OutputStream os = new WriterOutputStream(sw)) {
                os.write(text.getBytes(StandardCharsets.UTF_8));
            }
            assertThat(sw.toString()).isEqualTo(text);
        }
    }

    @Nested
    @DisplayName("逐字节写入测试")
    class ByteByByteTests {

        @Test
        @DisplayName("逐字节写入ASCII")
        void testWriteByteByByte() throws IOException {
            StringWriter sw = new StringWriter();
            try (OutputStream os = new WriterOutputStream(sw)) {
                os.write('H');
                os.write('i');
            }
            assertThat(sw.toString()).isEqualTo("Hi");
        }

        @Test
        @DisplayName("逐字节写入UTF-8多字节字符")
        void testWriteByteByByteMultibyte() throws IOException {
            String text = "你好";
            byte[] bytes = text.getBytes(StandardCharsets.UTF_8);

            StringWriter sw = new StringWriter();
            try (OutputStream os = new WriterOutputStream(sw)) {
                for (byte b : bytes) {
                    os.write(b & 0xFF);
                }
            }
            assertThat(sw.toString()).isEqualTo(text);
        }
    }

    @Nested
    @DisplayName("刷新测试")
    class FlushTests {

        @Test
        @DisplayName("flush将缓冲数据写入Writer")
        void testFlushWritesBufferedData() throws IOException {
            StringWriter sw = new StringWriter();
            OutputStream os = new WriterOutputStream(sw);
            os.write("Hello".getBytes(StandardCharsets.UTF_8));
            os.flush();

            assertThat(sw.toString()).isEqualTo("Hello");
            os.close();
        }
    }

    @Nested
    @DisplayName("关闭传播测试")
    class CloseTests {

        @Test
        @DisplayName("关闭传播到底层Writer")
        void testClosePropagation() throws IOException {
            boolean[] closed = {false};
            StringWriter writer = new StringWriter() {
                @Override
                public void close() {
                    closed[0] = true;
                    // StringWriter.close() does nothing, but we track it
                }
            };

            OutputStream os = new WriterOutputStream(writer);
            os.write("test".getBytes(StandardCharsets.UTF_8));
            os.close();

            assertThat(closed[0]).isTrue();
        }

        @Test
        @DisplayName("关闭时刷新剩余数据")
        void testCloseFlushesRemainingData() throws IOException {
            StringWriter sw = new StringWriter();
            OutputStream os = new WriterOutputStream(sw);
            os.write("Hello".getBytes(StandardCharsets.UTF_8));
            // Don't flush, just close — should still get data
            os.close();

            assertThat(sw.toString()).isEqualTo("Hello");
        }
    }

    @Nested
    @DisplayName("字符集测试")
    class CharsetTests {

        @Test
        @DisplayName("默认字符集为UTF-8")
        void testDefaultCharsetIsUtf8() throws IOException {
            String text = "你好";
            StringWriter sw = new StringWriter();
            try (OutputStream os = new WriterOutputStream(sw)) {
                os.write(text.getBytes(StandardCharsets.UTF_8));
            }
            assertThat(sw.toString()).isEqualTo(text);
        }

        @Test
        @DisplayName("使用ISO-8859-1字符集")
        void testIso88591Charset() throws IOException {
            String text = "caf\u00e9";
            Charset charset = StandardCharsets.ISO_8859_1;
            StringWriter sw = new StringWriter();
            try (OutputStream os = new WriterOutputStream(sw, charset)) {
                os.write(text.getBytes(charset));
            }
            assertThat(sw.toString()).isEqualTo(text);
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("写入空数组")
        void testWriteEmptyArray() throws IOException {
            StringWriter sw = new StringWriter();
            try (OutputStream os = new WriterOutputStream(sw)) {
                os.write(new byte[0], 0, 0);
            }
            assertThat(sw.toString()).isEmpty();
        }

        @Test
        @DisplayName("构造函数空参数校验 — writer")
        void testNullWriter() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new WriterOutputStream(null));
        }

        @Test
        @DisplayName("构造函数空参数校验 — charset")
        void testNullCharset() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new WriterOutputStream(new StringWriter(), (Charset) null));
        }

        @Test
        @DisplayName("大数据写入")
        void testLargeWrite() throws IOException {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 5000; i++) {
                sb.append("Hello World 你好世界 ");
            }
            String text = sb.toString();

            StringWriter sw = new StringWriter();
            try (OutputStream os = new WriterOutputStream(sw)) {
                os.write(text.getBytes(StandardCharsets.UTF_8));
            }
            assertThat(sw.toString()).isEqualTo(text);
        }
    }
}
