package cloud.opencode.base.io.source;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * CharSource 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
@DisplayName("CharSource 测试")
class CharSourceTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("wrap方法测试")
    class WrapTests {

        @Test
        @DisplayName("包装字符串")
        void testWrap() {
            String content = "Hello, World!";
            CharSource source = CharSource.wrap(content);

            assertThat(source.read()).isEqualTo(content);
        }

        @Test
        @DisplayName("包装空字符串")
        void testWrapEmpty() {
            CharSource source = CharSource.wrap("");

            assertThat(source.isEmpty()).isTrue();
            assertThat(source.read()).isEmpty();
        }

        @Test
        @DisplayName("lengthIfKnown返回正确长度")
        void testLengthIfKnown() {
            CharSource source = CharSource.wrap("Hello");

            assertThat(source.lengthIfKnown()).contains(5L);
        }

        @Test
        @DisplayName("toString返回描述")
        void testToString() {
            CharSource source = CharSource.wrap("test");

            assertThat(source.toString()).contains("CharSource.wrap");
        }

        @Test
        @DisplayName("长字符串toString截断")
        void testToStringLong() {
            String longString = "This is a very long string that exceeds 20 characters";
            CharSource source = CharSource.wrap(longString);

            assertThat(source.toString()).contains("...");
        }
    }

    @Nested
    @DisplayName("empty方法测试")
    class EmptyTests {

        @Test
        @DisplayName("创建空CharSource")
        void testEmpty() {
            CharSource source = CharSource.empty();

            assertThat(source.isEmpty()).isTrue();
            assertThat(source.read()).isEmpty();
        }
    }

    @Nested
    @DisplayName("fromPath方法测试")
    class FromPathTests {

        @Test
        @DisplayName("从文件路径创建(UTF-8)")
        void testFromPath() throws IOException {
            Path file = tempDir.resolve("test.txt");
            String content = "Hello, File!";
            Files.writeString(file, content);

            CharSource source = CharSource.fromPath(file);

            assertThat(source.read()).isEqualTo(content);
        }

        @Test
        @DisplayName("从文件路径创建(指定字符集)")
        void testFromPathWithCharset() throws IOException {
            Path file = tempDir.resolve("test.txt");
            String content = "你好";
            Files.writeString(file, content, StandardCharsets.UTF_8);

            CharSource source = CharSource.fromPath(file, StandardCharsets.UTF_8);

            assertThat(source.read()).isEqualTo(content);
        }

        @Test
        @DisplayName("toString返回路径")
        void testFromPathToString() throws IOException {
            Path file = tempDir.resolve("test.txt");
            Files.createFile(file);

            CharSource source = CharSource.fromPath(file);

            assertThat(source.toString()).contains("CharSource.fromPath");
        }
    }

    @Nested
    @DisplayName("openStream方法测试")
    class OpenStreamTests {

        @Test
        @DisplayName("打开Reader")
        void testOpenStream() throws IOException {
            CharSource source = CharSource.wrap("test content");

            try (Reader reader = source.openStream()) {
                char[] buf = new char[100];
                int read = reader.read(buf);
                assertThat(new String(buf, 0, read)).isEqualTo("test content");
            }
        }
    }

    @Nested
    @DisplayName("openBufferedStream方法测试")
    class OpenBufferedStreamTests {

        @Test
        @DisplayName("打开BufferedReader")
        void testOpenBufferedStream() throws IOException {
            CharSource source = CharSource.wrap("line1\nline2");

            try (BufferedReader reader = source.openBufferedStream()) {
                assertThat(reader.readLine()).isEqualTo("line1");
                assertThat(reader.readLine()).isEqualTo("line2");
            }
        }
    }

    @Nested
    @DisplayName("length方法测试")
    class LengthTests {

        @Test
        @DisplayName("返回正确长度")
        void testLength() {
            CharSource source = CharSource.wrap("Hello");

            assertThat(source.length()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("readFirstLine方法测试")
    class ReadFirstLineTests {

        @Test
        @DisplayName("读取首行")
        void testReadFirstLine() {
            CharSource source = CharSource.wrap("first\nsecond\nthird");

            assertThat(source.readFirstLine()).contains("first");
        }

        @Test
        @DisplayName("空源返回empty")
        void testReadFirstLineEmpty() {
            CharSource source = CharSource.empty();

            assertThat(source.readFirstLine()).isEmpty();
        }
    }

    @Nested
    @DisplayName("readLines方法测试")
    class ReadLinesTests {

        @Test
        @DisplayName("读取所有行")
        void testReadLines() {
            CharSource source = CharSource.wrap("line1\nline2\nline3");

            List<String> lines = source.readLines();

            assertThat(lines).containsExactly("line1", "line2", "line3");
        }

        @Test
        @DisplayName("空源返回空列表")
        void testReadLinesEmpty() {
            CharSource source = CharSource.empty();

            assertThat(source.readLines()).isEmpty();
        }
    }

    @Nested
    @DisplayName("lines方法测试")
    class LinesTests {

        @Test
        @DisplayName("返回行流")
        void testLines() {
            CharSource source = CharSource.wrap("a\nb\nc");

            try (Stream<String> lines = source.lines()) {
                assertThat(lines.toList()).containsExactly("a", "b", "c");
            }
        }
    }

    @Nested
    @DisplayName("countLines方法测试")
    class CountLinesTests {

        @Test
        @DisplayName("统计行数")
        void testCountLines() {
            CharSource source = CharSource.wrap("1\n2\n3\n4\n5");

            assertThat(source.countLines()).isEqualTo(5);
        }

        @Test
        @DisplayName("空源行数为0")
        void testCountLinesEmpty() {
            CharSource source = CharSource.empty();

            assertThat(source.countLines()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("copyTo方法测试")
    class CopyToTests {

        @Test
        @DisplayName("复制到Writer")
        void testCopyToWriter() throws IOException {
            CharSource source = CharSource.wrap("Hello, World!");
            StringWriter writer = new StringWriter();

            long copied = source.copyTo(writer);

            assertThat(copied).isEqualTo(13);
            assertThat(writer.toString()).isEqualTo("Hello, World!");
        }

        @Test
        @DisplayName("复制到CharSink")
        void testCopyToCharSink() throws IOException {
            CharSource source = CharSource.wrap("Test Content");
            Path targetFile = tempDir.resolve("target.txt");
            CharSink sink = CharSink.toPath(targetFile);

            long copied = source.copyTo(sink);

            assertThat(copied).isEqualTo(12);
            assertThat(Files.readString(targetFile)).isEqualTo("Test Content");
        }

        @Test
        @DisplayName("复制到Appendable")
        void testCopyToAppendable() {
            CharSource source = CharSource.wrap("Appendable");
            StringBuilder sb = new StringBuilder();

            long copied = source.copyTo(sb);

            assertThat(copied).isEqualTo(10);
            assertThat(sb.toString()).isEqualTo("Appendable");
        }
    }

    @Nested
    @DisplayName("contentEquals方法测试")
    class ContentEqualsTests {

        @Test
        @DisplayName("相同内容返回true")
        void testContentEqualsTrue() {
            CharSource source1 = CharSource.wrap("Hello");
            CharSource source2 = CharSource.wrap("Hello");

            assertThat(source1.contentEquals(source2)).isTrue();
        }

        @Test
        @DisplayName("不同内容返回false")
        void testContentEqualsFalse() {
            CharSource source1 = CharSource.wrap("Hello");
            CharSource source2 = CharSource.wrap("World");

            assertThat(source1.contentEquals(source2)).isFalse();
        }
    }

    @Nested
    @DisplayName("isEmpty方法测试")
    class IsEmptyTests {

        @Test
        @DisplayName("空源返回true")
        void testIsEmptyTrue() {
            CharSource source = CharSource.empty();

            assertThat(source.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("非空源返回false")
        void testIsEmptyFalse() {
            CharSource source = CharSource.wrap("x");

            assertThat(source.isEmpty()).isFalse();
        }
    }

    @Nested
    @DisplayName("forEachLine方法测试")
    class ForEachLineTests {

        @Test
        @DisplayName("处理每一行")
        void testForEachLine() {
            CharSource source = CharSource.wrap("a\nb\nc");
            List<String> collected = new ArrayList<>();

            source.forEachLine(collected::add);

            assertThat(collected).containsExactly("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("concat方法测试")
    class ConcatTests {

        @Test
        @DisplayName("连接多个CharSource(数组)")
        void testConcatVarargs() {
            CharSource s1 = CharSource.wrap("Hello");
            CharSource s2 = CharSource.wrap(", ");
            CharSource s3 = CharSource.wrap("World");

            CharSource concatenated = CharSource.concat(s1, s2, s3);

            assertThat(concatenated.read()).isEqualTo("Hello, World");
        }

        @Test
        @DisplayName("连接多个CharSource(Iterable)")
        void testConcatIterable() {
            List<CharSource> sources = List.of(
                CharSource.wrap("A"),
                CharSource.wrap("B"),
                CharSource.wrap("C")
            );

            CharSource concatenated = CharSource.concat(sources);

            assertThat(concatenated.read()).isEqualTo("ABC");
        }

        @Test
        @DisplayName("连接的lengthIfKnown正确")
        void testConcatLengthIfKnown() {
            CharSource s1 = CharSource.wrap("Hello");
            CharSource s2 = CharSource.wrap("World");

            CharSource concatenated = CharSource.concat(s1, s2);

            assertThat(concatenated.lengthIfKnown()).contains(10L);
        }

        @Test
        @DisplayName("toString返回连接描述")
        void testConcatToString() {
            CharSource concatenated = CharSource.concat(
                CharSource.empty(),
                CharSource.empty()
            );

            assertThat(concatenated.toString()).contains("concat");
        }
    }
}
