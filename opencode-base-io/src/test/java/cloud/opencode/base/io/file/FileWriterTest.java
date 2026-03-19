package cloud.opencode.base.io.file;

import cloud.opencode.base.io.exception.OpenIOOperationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * FileWriter 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
@DisplayName("FileWriter 测试")
class FileWriterTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("of方法测试")
    class OfTests {

        @Test
        @DisplayName("从Path创建")
        void testOfPath() {
            Path file = tempDir.resolve("test.txt");

            FileWriter writer = FileWriter.of(file);

            assertThat(writer.getPath()).isEqualTo(file);
        }

        @Test
        @DisplayName("从字符串创建")
        void testOfString() {
            String path = tempDir.resolve("test.txt").toString();

            FileWriter writer = FileWriter.of(path);

            assertThat(writer.getPath()).isNotNull();
        }
    }

    @Nested
    @DisplayName("charset方法测试")
    class CharsetTests {

        @Test
        @DisplayName("设置字符集")
        void testCharset() throws Exception {
            Path file = tempDir.resolve("test.txt");

            FileWriter.of(file).charset(StandardCharsets.UTF_8).write("你好");

            assertThat(Files.readString(file, StandardCharsets.UTF_8)).isEqualTo("你好");
        }
    }

    @Nested
    @DisplayName("append方法测试")
    class AppendTests {

        @Test
        @DisplayName("启用追加模式")
        void testAppend() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "Hello");

            FileWriter.of(file).append().write(", World!");

            assertThat(Files.readString(file)).isEqualTo("Hello, World!");
        }

        @Test
        @DisplayName("设置追加模式(boolean)")
        void testAppendBoolean() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "First");

            FileWriter.of(file).append(true).write("Second");

            assertThat(Files.readString(file)).isEqualTo("FirstSecond");
        }

        @Test
        @DisplayName("不追加时覆盖")
        void testNoAppend() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "Old content");

            FileWriter.of(file).append(false).write("New");

            assertThat(Files.readString(file)).isEqualTo("New");
        }
    }

    @Nested
    @DisplayName("noCreateParents方法测试")
    class NoCreateParentsTests {

        @Test
        @DisplayName("禁用自动创建父目录")
        void testNoCreateParents() {
            Path file = tempDir.resolve("nonexistent/dir/test.txt");

            assertThatThrownBy(() -> FileWriter.of(file).noCreateParents().write("content"))
                .isInstanceOf(OpenIOOperationException.class);
        }
    }

    @Nested
    @DisplayName("write(byte[])方法测试")
    class WriteBytesTests {

        @Test
        @DisplayName("写入字节数组")
        void testWriteBytes() throws Exception {
            Path file = tempDir.resolve("test.bin");
            byte[] data = {1, 2, 3, 4, 5};

            FileWriter.of(file).write(data);

            assertThat(Files.readAllBytes(file)).isEqualTo(data);
        }

        @Test
        @DisplayName("创建父目录")
        void testWriteBytesCreatesParent() throws Exception {
            Path file = tempDir.resolve("sub/dir/test.bin");
            byte[] data = {1, 2, 3};

            FileWriter.of(file).write(data);

            assertThat(Files.exists(file.getParent())).isTrue();
            assertThat(Files.readAllBytes(file)).isEqualTo(data);
        }

        @Test
        @DisplayName("追加字节")
        void testWriteBytesAppend() throws Exception {
            Path file = tempDir.resolve("test.bin");
            Files.write(file, new byte[]{1, 2});

            FileWriter.of(file).append().write(new byte[]{3, 4});

            assertThat(Files.readAllBytes(file)).containsExactly(1, 2, 3, 4);
        }
    }

    @Nested
    @DisplayName("write(CharSequence)方法测试")
    class WriteStringTests {

        @Test
        @DisplayName("写入字符串")
        void testWriteString() throws Exception {
            Path file = tempDir.resolve("test.txt");

            FileWriter.of(file).write("Hello, World!");

            assertThat(Files.readString(file)).isEqualTo("Hello, World!");
        }

        @Test
        @DisplayName("写入StringBuilder")
        void testWriteStringBuilder() throws Exception {
            Path file = tempDir.resolve("test.txt");
            StringBuilder sb = new StringBuilder("Content");

            FileWriter.of(file).write(sb);

            assertThat(Files.readString(file)).isEqualTo("Content");
        }

        @Test
        @DisplayName("创建父目录")
        void testWriteStringCreatesParent() throws Exception {
            Path file = tempDir.resolve("a/b/c/test.txt");

            FileWriter.of(file).write("content");

            assertThat(Files.exists(file.getParent())).isTrue();
            assertThat(Files.readString(file)).isEqualTo("content");
        }
    }

    @Nested
    @DisplayName("writeLines方法测试")
    class WriteLinesTests {

        @Test
        @DisplayName("写入行")
        void testWriteLines() throws Exception {
            Path file = tempDir.resolve("test.txt");

            FileWriter.of(file).writeLines(List.of("line1", "line2", "line3"));

            List<String> lines = Files.readAllLines(file);
            assertThat(lines).containsExactly("line1", "line2", "line3");
        }

        @Test
        @DisplayName("追加行")
        void testWriteLinesAppend() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "existing\n");

            FileWriter.of(file).append().writeLines(List.of("new1", "new2"));

            List<String> lines = Files.readAllLines(file);
            assertThat(lines).containsExactly("existing", "new1", "new2");
        }
    }

    @Nested
    @DisplayName("asBufferedWriter方法测试")
    class AsBufferedWriterTests {

        @Test
        @DisplayName("获取BufferedWriter")
        void testAsBufferedWriter() throws Exception {
            Path file = tempDir.resolve("test.txt");

            try (BufferedWriter writer = FileWriter.of(file).asBufferedWriter()) {
                writer.write("Hello");
            }

            assertThat(Files.readString(file)).isEqualTo("Hello");
        }

        @Test
        @DisplayName("追加模式的BufferedWriter")
        void testAsBufferedWriterAppend() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, "First");

            try (BufferedWriter writer = FileWriter.of(file).append().asBufferedWriter()) {
                writer.write("Second");
            }

            assertThat(Files.readString(file)).isEqualTo("FirstSecond");
        }
    }

    @Nested
    @DisplayName("asOutputStream方法测试")
    class AsOutputStreamTests {

        @Test
        @DisplayName("获取OutputStream")
        void testAsOutputStream() throws Exception {
            Path file = tempDir.resolve("test.bin");
            byte[] data = {10, 20, 30};

            try (OutputStream os = FileWriter.of(file).asOutputStream()) {
                os.write(data);
            }

            assertThat(Files.readAllBytes(file)).isEqualTo(data);
        }

        @Test
        @DisplayName("追加模式的OutputStream")
        void testAsOutputStreamAppend() throws Exception {
            Path file = tempDir.resolve("test.bin");
            Files.write(file, new byte[]{1, 2});

            try (OutputStream os = FileWriter.of(file).append().asOutputStream()) {
                os.write(new byte[]{3, 4});
            }

            assertThat(Files.readAllBytes(file)).containsExactly(1, 2, 3, 4);
        }
    }

    @Nested
    @DisplayName("getPath方法测试")
    class GetPathTests {

        @Test
        @DisplayName("返回路径")
        void testGetPath() {
            Path file = tempDir.resolve("test.txt");

            assertThat(FileWriter.of(file).getPath()).isEqualTo(file);
        }
    }
}
