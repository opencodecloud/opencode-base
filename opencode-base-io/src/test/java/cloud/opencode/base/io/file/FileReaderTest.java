package cloud.opencode.base.io.file;

import cloud.opencode.base.io.exception.OpenIOOperationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * FileReader 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
@DisplayName("FileReader 测试")
class FileReaderTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("of方法测试")
    class OfTests {

        @Test
        @DisplayName("从Path创建")
        void testOfPath() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.createFile(file);

            FileReader reader = FileReader.of(file);

            assertThat(reader.getPath()).isEqualTo(file);
        }

        @Test
        @DisplayName("从字符串创建")
        void testOfString() {
            String path = tempDir.resolve("test.txt").toString();

            FileReader reader = FileReader.of(path);

            assertThat(reader.getPath()).isNotNull();
        }
    }

    @Nested
    @DisplayName("charset方法测试")
    class CharsetTests {

        @Test
        @DisplayName("设置字符集")
        void testCharset() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "你好", StandardCharsets.UTF_8);

            String content = FileReader.of(file).charset(StandardCharsets.UTF_8).asString();

            assertThat(content).isEqualTo("你好");
        }
    }

    @Nested
    @DisplayName("asBytes方法测试")
    class AsBytesTests {

        @Test
        @DisplayName("读取为字节数组")
        void testAsBytes() throws Exception {
            Path file = tempDir.resolve("test.bin");
            byte[] data = {1, 2, 3, 4, 5};
            Files.write(file, data);

            byte[] result = FileReader.of(file).asBytes();

            assertThat(result).isEqualTo(data);
        }

        @Test
        @DisplayName("不存在的文件抛出异常")
        void testAsBytesNotExists() {
            Path file = tempDir.resolve("notexists.txt");

            assertThatThrownBy(() -> FileReader.of(file).asBytes())
                .isInstanceOf(OpenIOOperationException.class);
        }
    }

    @Nested
    @DisplayName("asString方法测试")
    class AsStringTests {

        @Test
        @DisplayName("读取为字符串")
        void testAsString() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "Hello, World!");

            String content = FileReader.of(file).asString();

            assertThat(content).isEqualTo("Hello, World!");
        }

        @Test
        @DisplayName("读取空文件")
        void testAsStringEmpty() throws Exception {
            Path file = tempDir.resolve("empty.txt");
            Files.createFile(file);

            String content = FileReader.of(file).asString();

            assertThat(content).isEmpty();
        }
    }

    @Nested
    @DisplayName("asLines方法测试")
    class AsLinesTests {

        @Test
        @DisplayName("读取为行列表")
        void testAsLines() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "line1\nline2\nline3");

            List<String> lines = FileReader.of(file).asLines();

            assertThat(lines).containsExactly("line1", "line2", "line3");
        }

        @Test
        @DisplayName("读取空文件")
        void testAsLinesEmpty() throws Exception {
            Path file = tempDir.resolve("empty.txt");
            Files.createFile(file);

            List<String> lines = FileReader.of(file).asLines();

            assertThat(lines).isEmpty();
        }
    }

    @Nested
    @DisplayName("asLineStream方法测试")
    class AsLineStreamTests {

        @Test
        @DisplayName("读取为行流")
        void testAsLineStream() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "a\nb\nc");

            try (Stream<String> lines = FileReader.of(file).asLineStream()) {
                assertThat(lines.toList()).containsExactly("a", "b", "c");
            }
        }
    }

    @Nested
    @DisplayName("asBufferedReader方法测试")
    class AsBufferedReaderTests {

        @Test
        @DisplayName("获取BufferedReader")
        void testAsBufferedReader() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "Hello");

            try (BufferedReader reader = FileReader.of(file).asBufferedReader()) {
                assertThat(reader.readLine()).isEqualTo("Hello");
            }
        }
    }

    @Nested
    @DisplayName("asInputStream方法测试")
    class AsInputStreamTests {

        @Test
        @DisplayName("获取InputStream")
        void testAsInputStream() throws Exception {
            Path file = tempDir.resolve("test.bin");
            byte[] data = {10, 20, 30};
            Files.write(file, data);

            try (InputStream is = FileReader.of(file).asInputStream()) {
                assertThat(is.readAllBytes()).isEqualTo(data);
            }
        }
    }

    @Nested
    @DisplayName("firstLine方法测试")
    class FirstLineTests {

        @Test
        @DisplayName("读取首行")
        void testFirstLine() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "first\nsecond\nthird");

            String firstLine = FileReader.of(file).firstLine();

            assertThat(firstLine).isEqualTo("first");
        }

        @Test
        @DisplayName("空文件返回null")
        void testFirstLineEmpty() throws Exception {
            Path file = tempDir.resolve("empty.txt");
            Files.createFile(file);

            String firstLine = FileReader.of(file).firstLine();

            assertThat(firstLine).isNull();
        }
    }

    @Nested
    @DisplayName("lastLine方法测试")
    class LastLineTests {

        @Test
        @DisplayName("读取末行")
        void testLastLine() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "first\nsecond\nlast");

            String lastLine = FileReader.of(file).lastLine();

            assertThat(lastLine).isEqualTo("last");
        }

        @Test
        @DisplayName("空文件返回null")
        void testLastLineEmpty() throws Exception {
            Path file = tempDir.resolve("empty.txt");
            Files.createFile(file);

            String lastLine = FileReader.of(file).lastLine();

            assertThat(lastLine).isNull();
        }

        @Test
        @DisplayName("单行文件")
        void testLastLineSingle() throws Exception {
            Path file = tempDir.resolve("single.txt");
            Files.writeString(file, "only line");

            String lastLine = FileReader.of(file).lastLine();

            assertThat(lastLine).isEqualTo("only line");
        }
    }

    @Nested
    @DisplayName("exists方法测试")
    class ExistsTests {

        @Test
        @DisplayName("存在的文件返回true")
        void testExistsTrue() throws Exception {
            Path file = tempDir.resolve("exists.txt");
            Files.createFile(file);

            assertThat(FileReader.of(file).exists()).isTrue();
        }

        @Test
        @DisplayName("不存在的文件返回false")
        void testExistsFalse() {
            Path file = tempDir.resolve("notexists.txt");

            assertThat(FileReader.of(file).exists()).isFalse();
        }
    }

    @Nested
    @DisplayName("size方法测试")
    class SizeTests {

        @Test
        @DisplayName("返回文件大小")
        void testSize() throws Exception {
            Path file = tempDir.resolve("test.txt");
            byte[] data = new byte[100];
            Files.write(file, data);

            assertThat(FileReader.of(file).size()).isEqualTo(100);
        }

        @Test
        @DisplayName("不存在的文件返回-1")
        void testSizeNotExists() {
            Path file = tempDir.resolve("notexists.txt");

            assertThat(FileReader.of(file).size()).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("getPath方法测试")
    class GetPathTests {

        @Test
        @DisplayName("返回路径")
        void testGetPath() {
            Path file = tempDir.resolve("test.txt");

            assertThat(FileReader.of(file).getPath()).isEqualTo(file);
        }
    }
}
