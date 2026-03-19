package cloud.opencode.base.io;

import cloud.opencode.base.io.exception.OpenIOOperationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenStream 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
@DisplayName("OpenStream 测试")
class OpenStreamTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("常量测试")
    class ConstantsTests {

        @Test
        @DisplayName("DEFAULT_BUFFER_SIZE常量")
        void testDefaultBufferSize() {
            assertThat(OpenStream.DEFAULT_BUFFER_SIZE).isEqualTo(8192);
        }
    }

    @Nested
    @DisplayName("copy(InputStream, OutputStream)方法测试")
    class CopyStreamTests {

        @Test
        @DisplayName("复制流")
        void testCopy() {
            byte[] data = {1, 2, 3, 4, 5};
            ByteArrayInputStream input = new ByteArrayInputStream(data);
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            long copied = OpenStream.copy(input, output);

            assertThat(copied).isEqualTo(5);
            assertThat(output.toByteArray()).isEqualTo(data);
        }

        @Test
        @DisplayName("复制空流")
        void testCopyEmpty() {
            ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            long copied = OpenStream.copy(input, output);

            assertThat(copied).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("copy带缓冲区大小方法测试")
    class CopyWithBufferSizeTests {

        @Test
        @DisplayName("使用自定义缓冲区大小复制")
        void testCopyWithBufferSize() {
            byte[] data = new byte[100];
            for (int i = 0; i < 100; i++) {
                data[i] = (byte) i;
            }
            ByteArrayInputStream input = new ByteArrayInputStream(data);
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            long copied = OpenStream.copy(input, output, 10);

            assertThat(copied).isEqualTo(100);
            assertThat(output.toByteArray()).isEqualTo(data);
        }
    }

    @Nested
    @DisplayName("copy(Reader, Writer)方法测试")
    class CopyReaderWriterTests {

        @Test
        @DisplayName("复制Reader到Writer")
        void testCopyReaderWriter() {
            StringReader reader = new StringReader("Hello, World!");
            StringWriter writer = new StringWriter();

            long copied = OpenStream.copy(reader, writer);

            assertThat(copied).isEqualTo(13);
            assertThat(writer.toString()).isEqualTo("Hello, World!");
        }
    }

    @Nested
    @DisplayName("copyToFile方法测试")
    class CopyToFileTests {

        @Test
        @DisplayName("复制流到文件")
        void testCopyToFile() throws Exception {
            byte[] data = {10, 20, 30};
            ByteArrayInputStream input = new ByteArrayInputStream(data);
            Path file = tempDir.resolve("output.bin");

            long copied = OpenStream.copyToFile(input, file);

            assertThat(copied).isEqualTo(3);
            assertThat(Files.readAllBytes(file)).isEqualTo(data);
        }
    }

    @Nested
    @DisplayName("toByteArray方法测试")
    class ToByteArrayTests {

        @Test
        @DisplayName("读取为字节数组")
        void testToByteArray() {
            byte[] data = {1, 2, 3, 4, 5};
            ByteArrayInputStream input = new ByteArrayInputStream(data);

            byte[] result = OpenStream.toByteArray(input);

            assertThat(result).isEqualTo(data);
        }

        @Test
        @DisplayName("读取空流")
        void testToByteArrayEmpty() {
            ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);

            byte[] result = OpenStream.toByteArray(input);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("toByteArray带大小限制方法测试")
    class ToByteArrayWithLimitTests {

        @Test
        @DisplayName("读取限制大小")
        void testToByteArrayWithLimit() {
            byte[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
            ByteArrayInputStream input = new ByteArrayInputStream(data);

            byte[] result = OpenStream.toByteArray(input, 5);

            assertThat(result).hasSize(5);
            assertThat(result).containsExactly(1, 2, 3, 4, 5);
        }
    }

    @Nested
    @DisplayName("toString(InputStream)方法测试")
    class ToStringInputStreamTests {

        @Test
        @DisplayName("读取为字符串")
        void testToString() {
            String content = "Hello, World!";
            ByteArrayInputStream input = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

            String result = OpenStream.toString(input);

            assertThat(result).isEqualTo(content);
        }

        @Test
        @DisplayName("使用指定字符集读取")
        void testToStringWithCharset() {
            String content = "你好";
            ByteArrayInputStream input = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

            String result = OpenStream.toString(input, StandardCharsets.UTF_8);

            assertThat(result).isEqualTo(content);
        }
    }

    @Nested
    @DisplayName("toString(Reader)方法测试")
    class ToStringReaderTests {

        @Test
        @DisplayName("读取Reader为字符串")
        void testToStringReader() {
            StringReader reader = new StringReader("Content");

            String result = OpenStream.toString(reader);

            assertThat(result).isEqualTo("Content");
        }
    }

    @Nested
    @DisplayName("write(byte[], OutputStream)方法测试")
    class WriteBytesTests {

        @Test
        @DisplayName("写入字节数组")
        void testWriteBytes() {
            byte[] data = {1, 2, 3};
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            OpenStream.write(data, output);

            assertThat(output.toByteArray()).isEqualTo(data);
        }
    }

    @Nested
    @DisplayName("write(String, OutputStream)方法测试")
    class WriteStringToOutputStreamTests {

        @Test
        @DisplayName("写入字符串到流")
        void testWriteString() {
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            OpenStream.write("Hello", output, StandardCharsets.UTF_8);

            assertThat(output.toString(StandardCharsets.UTF_8)).isEqualTo("Hello");
        }
    }

    @Nested
    @DisplayName("write(String, Writer)方法测试")
    class WriteStringToWriterTests {

        @Test
        @DisplayName("写入字符串到Writer")
        void testWriteStringToWriter() {
            StringWriter writer = new StringWriter();

            OpenStream.write("Content", writer);

            assertThat(writer.toString()).isEqualTo("Content");
        }
    }

    @Nested
    @DisplayName("closeQuietly方法测试")
    class CloseQuietlyTests {

        @Test
        @DisplayName("安全关闭Closeable")
        void testCloseQuietly() {
            ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);

            assertThatCode(() -> OpenStream.closeQuietly(input)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("null不抛异常")
        void testCloseQuietlyNull() {
            assertThatCode(() -> OpenStream.closeQuietly((Closeable) null)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("关闭多个Closeable")
        void testCloseQuietlyMultiple() {
            ByteArrayInputStream input1 = new ByteArrayInputStream(new byte[0]);
            ByteArrayInputStream input2 = new ByteArrayInputStream(new byte[0]);

            assertThatCode(() -> OpenStream.closeQuietly(input1, input2)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("数组为null不抛异常")
        void testCloseQuietlyNullArray() {
            assertThatCode(() -> OpenStream.closeQuietly((Closeable[]) null)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("关闭时异常被忽略")
        void testCloseQuietlyExceptionIgnored() {
            Closeable failing = () -> {
                throw new IOException("Close failed");
            };

            assertThatCode(() -> OpenStream.closeQuietly(failing)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("buffer(InputStream)方法测试")
    class BufferInputStreamTests {

        @Test
        @DisplayName("包装为BufferedInputStream")
        void testBufferInputStream() {
            ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);

            BufferedInputStream buffered = OpenStream.buffer(input);

            assertThat(buffered).isNotNull();
        }

        @Test
        @DisplayName("已是BufferedInputStream不重复包装")
        void testBufferInputStreamAlreadyBuffered() {
            BufferedInputStream input = new BufferedInputStream(new ByteArrayInputStream(new byte[0]));

            BufferedInputStream result = OpenStream.buffer(input);

            assertThat(result).isSameAs(input);
        }
    }

    @Nested
    @DisplayName("buffer(OutputStream)方法测试")
    class BufferOutputStreamTests {

        @Test
        @DisplayName("包装为BufferedOutputStream")
        void testBufferOutputStream() {
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            BufferedOutputStream buffered = OpenStream.buffer(output);

            assertThat(buffered).isNotNull();
        }

        @Test
        @DisplayName("已是BufferedOutputStream不重复包装")
        void testBufferOutputStreamAlreadyBuffered() {
            BufferedOutputStream output = new BufferedOutputStream(new ByteArrayOutputStream());

            BufferedOutputStream result = OpenStream.buffer(output);

            assertThat(result).isSameAs(output);
        }
    }

    @Nested
    @DisplayName("toReader方法测试")
    class ToReaderTests {

        @Test
        @DisplayName("转换为Reader")
        void testToReader() throws Exception {
            String content = "Hello";
            ByteArrayInputStream input = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

            Reader reader = OpenStream.toReader(input, StandardCharsets.UTF_8);

            assertThat(reader).isNotNull();
            char[] buffer = new char[10];
            int read = reader.read(buffer);
            assertThat(new String(buffer, 0, read)).isEqualTo(content);
        }
    }

    @Nested
    @DisplayName("toWriter方法测试")
    class ToWriterTests {

        @Test
        @DisplayName("转换为Writer")
        void testToWriter() throws Exception {
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            Writer writer = OpenStream.toWriter(output, StandardCharsets.UTF_8);
            writer.write("Hello");
            writer.flush();

            assertThat(output.toString(StandardCharsets.UTF_8)).isEqualTo("Hello");
        }
    }

    @Nested
    @DisplayName("toInputStream(String)方法测试")
    class ToInputStreamStringTests {

        @Test
        @DisplayName("字符串转换为InputStream")
        void testToInputStreamString() throws Exception {
            InputStream input = OpenStream.toInputStream("Hello", StandardCharsets.UTF_8);

            String result = OpenStream.toString(input);
            assertThat(result).isEqualTo("Hello");
        }
    }

    @Nested
    @DisplayName("toInputStream(byte[])方法测试")
    class ToInputStreamBytesTests {

        @Test
        @DisplayName("字节数组转换为InputStream")
        void testToInputStreamBytes() {
            byte[] data = {1, 2, 3};

            InputStream input = OpenStream.toInputStream(data);

            assertThat(OpenStream.toByteArray(input)).isEqualTo(data);
        }
    }

    @Nested
    @DisplayName("skip方法测试")
    class SkipTests {

        @Test
        @DisplayName("跳过字节")
        void testSkip() {
            byte[] data = {1, 2, 3, 4, 5};
            ByteArrayInputStream input = new ByteArrayInputStream(data);

            long skipped = OpenStream.skip(input, 3);

            assertThat(skipped).isEqualTo(3);
            assertThat(OpenStream.toByteArray(input)).containsExactly(4, 5);
        }
    }

    @Nested
    @DisplayName("drain方法测试")
    class DrainTests {

        @Test
        @DisplayName("排空流")
        void testDrain() {
            byte[] data = {1, 2, 3, 4, 5};
            ByteArrayInputStream input = new ByteArrayInputStream(data);

            long drained = OpenStream.drain(input);

            assertThat(drained).isEqualTo(5);
        }

        @Test
        @DisplayName("排空空流")
        void testDrainEmpty() {
            ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);

            long drained = OpenStream.drain(input);

            assertThat(drained).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("contentEquals(InputStream, InputStream)方法测试")
    class ContentEqualsInputStreamTests {

        @Test
        @DisplayName("相同内容返回true")
        void testContentEqualsTrue() {
            byte[] data = {1, 2, 3, 4, 5};
            ByteArrayInputStream input1 = new ByteArrayInputStream(data);
            ByteArrayInputStream input2 = new ByteArrayInputStream(data);

            boolean equals = OpenStream.contentEquals(input1, input2);

            assertThat(equals).isTrue();
        }

        @Test
        @DisplayName("不同内容返回false")
        void testContentEqualsFalse() {
            ByteArrayInputStream input1 = new ByteArrayInputStream(new byte[]{1, 2, 3});
            ByteArrayInputStream input2 = new ByteArrayInputStream(new byte[]{1, 2, 4});

            boolean equals = OpenStream.contentEquals(input1, input2);

            assertThat(equals).isFalse();
        }

        @Test
        @DisplayName("不同长度返回false")
        void testContentEqualsDifferentLength() {
            ByteArrayInputStream input1 = new ByteArrayInputStream(new byte[]{1, 2, 3});
            ByteArrayInputStream input2 = new ByteArrayInputStream(new byte[]{1, 2, 3, 4});

            boolean equals = OpenStream.contentEquals(input1, input2);

            assertThat(equals).isFalse();
        }

        @Test
        @DisplayName("空流相等")
        void testContentEqualsEmpty() {
            ByteArrayInputStream input1 = new ByteArrayInputStream(new byte[0]);
            ByteArrayInputStream input2 = new ByteArrayInputStream(new byte[0]);

            boolean equals = OpenStream.contentEquals(input1, input2);

            assertThat(equals).isTrue();
        }
    }

    @Nested
    @DisplayName("contentEquals(Reader, Reader)方法测试")
    class ContentEqualsReaderTests {

        @Test
        @DisplayName("相同内容返回true")
        void testContentEqualsTrue() {
            StringReader reader1 = new StringReader("Hello");
            StringReader reader2 = new StringReader("Hello");

            boolean equals = OpenStream.contentEquals(reader1, reader2);

            assertThat(equals).isTrue();
        }

        @Test
        @DisplayName("不同内容返回false")
        void testContentEqualsFalse() {
            StringReader reader1 = new StringReader("Hello");
            StringReader reader2 = new StringReader("World");

            boolean equals = OpenStream.contentEquals(reader1, reader2);

            assertThat(equals).isFalse();
        }

        @Test
        @DisplayName("使用BufferedReader")
        void testContentEqualsBufferedReader() {
            BufferedReader reader1 = new BufferedReader(new StringReader("Test"));
            BufferedReader reader2 = new BufferedReader(new StringReader("Test"));

            boolean equals = OpenStream.contentEquals(reader1, reader2);

            assertThat(equals).isTrue();
        }
    }
}
