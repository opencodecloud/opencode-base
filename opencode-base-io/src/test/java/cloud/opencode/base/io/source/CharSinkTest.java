package cloud.opencode.base.io.source;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * CharSink 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
@DisplayName("CharSink 测试")
class CharSinkTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("toPath方法测试")
    class ToPathTests {

        @Test
        @DisplayName("写入文件(UTF-8)")
        void testToPath() throws IOException {
            Path file = tempDir.resolve("output.txt");
            CharSink sink = CharSink.toPath(file);

            sink.write("Hello, World!");

            assertThat(Files.readString(file)).isEqualTo("Hello, World!");
        }

        @Test
        @DisplayName("写入文件(指定字符集)")
        void testToPathWithCharset() throws IOException {
            Path file = tempDir.resolve("output.txt");
            CharSink sink = CharSink.toPath(file, StandardCharsets.UTF_8);

            sink.write("你好，世界！");

            assertThat(Files.readString(file, StandardCharsets.UTF_8)).isEqualTo("你好，世界！");
        }

        @Test
        @DisplayName("覆盖已有文件")
        void testToPathOverwrite() throws IOException {
            Path file = tempDir.resolve("output.txt");
            Files.writeString(file, "old content");
            CharSink sink = CharSink.toPath(file);

            sink.write("new");

            assertThat(Files.readString(file)).isEqualTo("new");
        }

        @Test
        @DisplayName("toString返回描述")
        void testToPathToString() {
            Path file = tempDir.resolve("output.txt");
            CharSink sink = CharSink.toPath(file);

            assertThat(sink.toString()).contains("CharSink.toPath");
        }
    }

    @Nested
    @DisplayName("toPathAppend方法测试")
    class ToPathAppendTests {

        @Test
        @DisplayName("追加到文件(UTF-8)")
        void testToPathAppend() throws IOException {
            Path file = tempDir.resolve("output.txt");
            Files.writeString(file, "Hello");
            CharSink sink = CharSink.toPathAppend(file);

            sink.write(", World!");

            assertThat(Files.readString(file)).isEqualTo("Hello, World!");
        }

        @Test
        @DisplayName("追加到文件(指定字符集)")
        void testToPathAppendWithCharset() throws IOException {
            Path file = tempDir.resolve("output.txt");
            Files.writeString(file, "前缀", StandardCharsets.UTF_8);
            CharSink sink = CharSink.toPathAppend(file, StandardCharsets.UTF_8);

            sink.write("后缀");

            assertThat(Files.readString(file, StandardCharsets.UTF_8)).isEqualTo("前缀后缀");
        }

        @Test
        @DisplayName("追加到不存在的文件")
        void testToPathAppendNewFile() throws IOException {
            Path file = tempDir.resolve("newfile.txt");
            CharSink sink = CharSink.toPathAppend(file);

            sink.write("content");

            assertThat(Files.readString(file)).isEqualTo("content");
        }

        @Test
        @DisplayName("toString返回追加描述")
        void testToPathAppendToString() {
            Path file = tempDir.resolve("output.txt");
            CharSink sink = CharSink.toPathAppend(file);

            assertThat(sink.toString()).contains("toPathAppend");
        }
    }

    @Nested
    @DisplayName("nullSink方法测试")
    class NullSinkTests {

        @Test
        @DisplayName("丢弃所有数据")
        void testNullSink() {
            CharSink sink = CharSink.nullSink();

            // Should not throw
            sink.write("discarded content");
        }

        @Test
        @DisplayName("writeFrom返回字符数")
        void testNullSinkWriteFrom() {
            CharSink sink = CharSink.nullSink();
            StringReader reader = new StringReader("test content");

            long written = sink.writeFrom(reader);

            assertThat(written).isEqualTo(12);
        }

        @Test
        @DisplayName("writeLines不抛异常")
        void testNullSinkWriteLines() {
            CharSink sink = CharSink.nullSink();

            // Should not throw
            sink.writeLines(List.of("line1", "line2", "line3"));
        }

        @Test
        @DisplayName("writeLines(Stream)不抛异常")
        void testNullSinkWriteLinesStream() {
            CharSink sink = CharSink.nullSink();

            // Should not throw
            sink.writeLines(Stream.of("a", "b", "c"));
        }

        @Test
        @DisplayName("toString返回描述")
        void testNullSinkToString() {
            CharSink sink = CharSink.nullSink();

            assertThat(sink.toString()).contains("nullSink");
        }
    }

    @Nested
    @DisplayName("openStream方法测试")
    class OpenStreamTests {

        @Test
        @DisplayName("打开Writer")
        void testOpenStream() throws IOException {
            Path file = tempDir.resolve("stream.txt");
            CharSink sink = CharSink.toPath(file);

            try (Writer writer = sink.openStream()) {
                writer.write("direct write");
            }

            assertThat(Files.readString(file)).isEqualTo("direct write");
        }
    }

    @Nested
    @DisplayName("openBufferedStream方法测试")
    class OpenBufferedStreamTests {

        @Test
        @DisplayName("打开BufferedWriter")
        void testOpenBufferedStream() throws IOException {
            Path file = tempDir.resolve("buffered.txt");
            CharSink sink = CharSink.toPath(file);

            try (BufferedWriter writer = sink.openBufferedStream()) {
                writer.write("buffered");
                writer.newLine();
                writer.write("write");
            }

            List<String> lines = Files.readAllLines(file);
            assertThat(lines).containsExactly("buffered", "write");
        }
    }

    @Nested
    @DisplayName("write方法测试")
    class WriteTests {

        @Test
        @DisplayName("写入CharSequence")
        void testWrite() throws IOException {
            Path file = tempDir.resolve("data.txt");
            CharSink sink = CharSink.toPath(file);

            sink.write("Hello");

            assertThat(Files.readString(file)).isEqualTo("Hello");
        }

        @Test
        @DisplayName("写入StringBuilder")
        void testWriteStringBuilder() throws IOException {
            Path file = tempDir.resolve("data.txt");
            CharSink sink = CharSink.toPath(file);
            StringBuilder sb = new StringBuilder("StringBuilder content");

            sink.write(sb);

            assertThat(Files.readString(file)).isEqualTo("StringBuilder content");
        }

        @Test
        @DisplayName("写入空字符串")
        void testWriteEmpty() throws IOException {
            Path file = tempDir.resolve("empty.txt");
            CharSink sink = CharSink.toPath(file);

            sink.write("");

            assertThat(Files.readString(file)).isEmpty();
        }

        @Test
        @DisplayName("nullSink接受null CharSequence")
        void testWriteNull() {
            CharSink sink = CharSink.nullSink();

            // nullSink just discards everything including null
            assertThatCode(() -> sink.write(null)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("writeFrom方法测试")
    class WriteFromTests {

        @Test
        @DisplayName("从Reader写入")
        void testWriteFrom() throws IOException {
            Path file = tempDir.resolve("data.txt");
            CharSink sink = CharSink.toPath(file);
            StringReader reader = new StringReader("Reader content");

            long written = sink.writeFrom(reader);

            assertThat(written).isEqualTo(14);
            assertThat(Files.readString(file)).isEqualTo("Reader content");
        }

        @Test
        @DisplayName("从空Reader写入")
        void testWriteFromEmpty() throws IOException {
            Path file = tempDir.resolve("empty.txt");
            CharSink sink = CharSink.toPath(file);
            StringReader reader = new StringReader("");

            long written = sink.writeFrom(reader);

            assertThat(written).isEqualTo(0);
            assertThat(Files.readString(file)).isEmpty();
        }

        @Test
        @DisplayName("null Reader抛出异常")
        void testWriteFromNull() {
            CharSink sink = CharSink.nullSink();

            assertThatThrownBy(() -> sink.writeFrom(null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("writeLines方法测试")
    class WriteLinesTests {

        @Test
        @DisplayName("写入行(Iterable)")
        void testWriteLinesIterable() throws IOException {
            Path file = tempDir.resolve("lines.txt");
            CharSink sink = CharSink.toPath(file);

            sink.writeLines(List.of("line1", "line2", "line3"));

            List<String> lines = Files.readAllLines(file);
            assertThat(lines).containsExactly("line1", "line2", "line3");
        }

        @Test
        @DisplayName("写入行(Stream)")
        void testWriteLinesStream() throws IOException {
            Path file = tempDir.resolve("lines.txt");
            CharSink sink = CharSink.toPath(file);

            sink.writeLines(Stream.of("a", "b", "c"));

            List<String> lines = Files.readAllLines(file);
            assertThat(lines).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("写入行带自定义分隔符")
        void testWriteLinesCustomSeparator() throws IOException {
            Path file = tempDir.resolve("lines.txt");
            CharSink sink = CharSink.toPath(file);

            sink.writeLines(List.of("a", "b", "c"), "|");

            String content = Files.readString(file);
            assertThat(content).isEqualTo("a|b|c");
        }

        @Test
        @DisplayName("写入空行列表")
        void testWriteLinesEmpty() throws IOException {
            Path file = tempDir.resolve("empty.txt");
            CharSink sink = CharSink.toPath(file);

            sink.writeLines(List.of());

            assertThat(Files.readString(file)).isEmpty();
        }

        @Test
        @DisplayName("nullSink接受null行列表")
        void testWriteLinesNull() {
            CharSink sink = CharSink.nullSink();

            // nullSink just discards everything including null
            assertThatCode(() -> sink.writeLines((Iterable<String>) null)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("源到接收器复制测试")
    class SourceToSinkTests {

        @Test
        @DisplayName("CharSource复制到CharSink")
        void testSourceToSink() throws IOException {
            CharSource source = CharSource.wrap("Source content");
            Path targetFile = tempDir.resolve("target.txt");
            CharSink sink = CharSink.toPath(targetFile);

            source.copyTo(sink);

            assertThat(Files.readString(targetFile)).isEqualTo("Source content");
        }
    }
}
