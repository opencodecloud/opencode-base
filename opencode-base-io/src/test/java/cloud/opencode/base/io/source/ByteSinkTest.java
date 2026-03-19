package cloud.opencode.base.io.source;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * ByteSink 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
@DisplayName("ByteSink 测试")
class ByteSinkTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("toPath方法测试")
    class ToPathTests {

        @Test
        @DisplayName("写入文件")
        void testToPath() throws IOException {
            Path file = tempDir.resolve("output.bin");
            ByteSink sink = ByteSink.toPath(file);
            byte[] data = {1, 2, 3, 4, 5};

            sink.write(data);

            assertThat(Files.readAllBytes(file)).isEqualTo(data);
        }

        @Test
        @DisplayName("覆盖已有文件")
        void testToPathOverwrite() throws IOException {
            Path file = tempDir.resolve("output.bin");
            Files.write(file, new byte[]{99, 99, 99});
            ByteSink sink = ByteSink.toPath(file);
            byte[] newData = {1, 2};

            sink.write(newData);

            assertThat(Files.readAllBytes(file)).isEqualTo(newData);
        }

        @Test
        @DisplayName("toString返回描述")
        void testToPathToString() {
            Path file = tempDir.resolve("output.bin");
            ByteSink sink = ByteSink.toPath(file);

            assertThat(sink.toString()).contains("ByteSink.toPath");
        }
    }

    @Nested
    @DisplayName("toPathAppend方法测试")
    class ToPathAppendTests {

        @Test
        @DisplayName("追加到文件")
        void testToPathAppend() throws IOException {
            Path file = tempDir.resolve("output.bin");
            Files.write(file, new byte[]{1, 2, 3});
            ByteSink sink = ByteSink.toPathAppend(file);
            byte[] appendData = {4, 5};

            sink.write(appendData);

            assertThat(Files.readAllBytes(file)).containsExactly(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("追加到不存在的文件")
        void testToPathAppendNewFile() throws IOException {
            Path file = tempDir.resolve("newfile.bin");
            ByteSink sink = ByteSink.toPathAppend(file);
            byte[] data = {1, 2, 3};

            sink.write(data);

            assertThat(Files.readAllBytes(file)).isEqualTo(data);
        }

        @Test
        @DisplayName("toString返回追加描述")
        void testToPathAppendToString() {
            Path file = tempDir.resolve("output.bin");
            ByteSink sink = ByteSink.toPathAppend(file);

            assertThat(sink.toString()).contains("toPathAppend");
        }
    }

    @Nested
    @DisplayName("nullSink方法测试")
    class NullSinkTests {

        @Test
        @DisplayName("丢弃所有数据")
        void testNullSink() {
            ByteSink sink = ByteSink.nullSink();

            // Should not throw
            sink.write(new byte[]{1, 2, 3, 4, 5});
        }

        @Test
        @DisplayName("writeFrom返回字节数")
        void testNullSinkWriteFrom() {
            ByteSink sink = ByteSink.nullSink();
            ByteArrayInputStream input = new ByteArrayInputStream(new byte[100]);

            long written = sink.writeFrom(input);

            assertThat(written).isEqualTo(100);
        }

        @Test
        @DisplayName("toString返回描述")
        void testNullSinkToString() {
            ByteSink sink = ByteSink.nullSink();

            assertThat(sink.toString()).contains("nullSink");
        }
    }

    @Nested
    @DisplayName("openStream方法测试")
    class OpenStreamTests {

        @Test
        @DisplayName("打开输出流")
        void testOpenStream() throws IOException {
            Path file = tempDir.resolve("stream.bin");
            ByteSink sink = ByteSink.toPath(file);

            try (OutputStream os = sink.openStream()) {
                os.write(new byte[]{1, 2, 3});
            }

            assertThat(Files.readAllBytes(file)).containsExactly(1, 2, 3);
        }
    }

    @Nested
    @DisplayName("asCharSink方法测试")
    class AsCharSinkTests {

        @Test
        @DisplayName("转换为CharSink(UTF-8)")
        void testAsCharSinkUtf8() throws IOException {
            Path file = tempDir.resolve("text.txt");
            ByteSink sink = ByteSink.toPath(file);
            CharSink charSink = sink.asCharSink();

            charSink.write("Hello");

            assertThat(Files.readString(file, StandardCharsets.UTF_8)).isEqualTo("Hello");
        }

        @Test
        @DisplayName("转换为CharSink(指定字符集)")
        void testAsCharSinkWithCharset() throws IOException {
            Path file = tempDir.resolve("text.txt");
            ByteSink sink = ByteSink.toPath(file);
            CharSink charSink = sink.asCharSink(StandardCharsets.UTF_8);

            charSink.write("你好");

            assertThat(Files.readString(file, StandardCharsets.UTF_8)).isEqualTo("你好");
        }

        @Test
        @DisplayName("toString包含字符集信息")
        void testAsCharSinkToString() {
            ByteSink sink = ByteSink.nullSink();
            CharSink charSink = sink.asCharSink(StandardCharsets.UTF_8);

            assertThat(charSink.toString()).contains("asCharSink");
        }
    }

    @Nested
    @DisplayName("write方法测试")
    class WriteTests {

        @Test
        @DisplayName("写入字节数组")
        void testWrite() throws IOException {
            Path file = tempDir.resolve("data.bin");
            ByteSink sink = ByteSink.toPath(file);
            byte[] data = {10, 20, 30, 40, 50};

            sink.write(data);

            assertThat(Files.readAllBytes(file)).isEqualTo(data);
        }

        @Test
        @DisplayName("写入空数组")
        void testWriteEmpty() throws IOException {
            Path file = tempDir.resolve("empty.bin");
            ByteSink sink = ByteSink.toPath(file);

            sink.write(new byte[0]);

            assertThat(Files.readAllBytes(file)).isEmpty();
        }

        @Test
        @DisplayName("nullSink接受null字节数组")
        void testWriteNull() {
            ByteSink sink = ByteSink.nullSink();

            // nullSink just discards everything including null
            assertThatCode(() -> sink.write(null)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("writeFrom方法测试")
    class WriteFromTests {

        @Test
        @DisplayName("从InputStream写入")
        void testWriteFrom() throws IOException {
            Path file = tempDir.resolve("data.bin");
            ByteSink sink = ByteSink.toPath(file);
            byte[] data = {1, 2, 3, 4, 5};
            ByteArrayInputStream input = new ByteArrayInputStream(data);

            long written = sink.writeFrom(input);

            assertThat(written).isEqualTo(5);
            assertThat(Files.readAllBytes(file)).isEqualTo(data);
        }

        @Test
        @DisplayName("从空InputStream写入")
        void testWriteFromEmpty() throws IOException {
            Path file = tempDir.resolve("empty.bin");
            ByteSink sink = ByteSink.toPath(file);
            ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);

            long written = sink.writeFrom(input);

            assertThat(written).isEqualTo(0);
            assertThat(Files.readAllBytes(file)).isEmpty();
        }

        @Test
        @DisplayName("null输入流抛出异常")
        void testWriteFromNull() {
            ByteSink sink = ByteSink.nullSink();

            assertThatThrownBy(() -> sink.writeFrom(null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("源到接收器复制测试")
    class SourceToSinkTests {

        @Test
        @DisplayName("ByteSource复制到ByteSink")
        void testSourceToSink() throws IOException {
            byte[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
            ByteSource source = ByteSource.wrap(data);
            Path targetFile = tempDir.resolve("target.bin");
            ByteSink sink = ByteSink.toPath(targetFile);

            source.copyTo(sink);

            assertThat(Files.readAllBytes(targetFile)).isEqualTo(data);
        }
    }
}
