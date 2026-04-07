package cloud.opencode.base.io.file;

import cloud.opencode.base.io.exception.OpenIOOperationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * TailReader 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.3
 */
@DisplayName("TailReader 测试")
class TailReaderTest {

    @TempDir
    Path tempDir;

    // ==================== lastLines 测试 ====================

    @Nested
    @DisplayName("lastLines 测试")
    class LastLinesTests {

        @Test
        @DisplayName("读取精确行数")
        void testExactLineCount() throws IOException {
            Path file = tempDir.resolve("exact.txt");
            Files.writeString(file, "line1\nline2\nline3\nline4\nline5\n");

            List<String> result = TailReader.lastLines(file, 3);

            assertThat(result).containsExactly("line3", "line4", "line5");
        }

        @Test
        @DisplayName("请求超过文件行数 — 返回全部行")
        void testRequestMoreThanAvailable() throws IOException {
            Path file = tempDir.resolve("few.txt");
            Files.writeString(file, "line1\nline2\nline3\n");

            List<String> result = TailReader.lastLines(file, 10);

            assertThat(result).containsExactly("line1", "line2", "line3");
        }

        @Test
        @DisplayName("空文件返回空列表")
        void testEmptyFile() throws IOException {
            Path file = tempDir.resolve("empty.txt");
            Files.writeString(file, "");

            List<String> result = TailReader.lastLines(file, 5);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("单行无末尾换行")
        void testSingleLineNoTrailingNewline() throws IOException {
            Path file = tempDir.resolve("single.txt");
            Files.writeString(file, "only line");

            List<String> result = TailReader.lastLines(file, 1);

            assertThat(result).containsExactly("only line");
        }

        @Test
        @DisplayName("单行有末尾换行")
        void testSingleLineWithTrailingNewline() throws IOException {
            Path file = tempDir.resolve("single_nl.txt");
            Files.writeString(file, "only line\n");

            List<String> result = TailReader.lastLines(file, 1);

            assertThat(result).containsExactly("only line");
        }

        @Test
        @DisplayName("有末尾换行符的文件")
        void testTrailingNewline() throws IOException {
            Path file = tempDir.resolve("trailing.txt");
            Files.writeString(file, "a\nb\nc\n");

            List<String> result = TailReader.lastLines(file, 2);

            assertThat(result).containsExactly("b", "c");
        }

        @Test
        @DisplayName("无末尾换行符的文件")
        void testNoTrailingNewline() throws IOException {
            Path file = tempDir.resolve("no_trailing.txt");
            Files.writeString(file, "a\nb\nc");

            List<String> result = TailReader.lastLines(file, 2);

            assertThat(result).containsExactly("b", "c");
        }

        @Test
        @DisplayName("请求所有行")
        void testRequestAllLines() throws IOException {
            Path file = tempDir.resolve("all.txt");
            Files.writeString(file, "line1\nline2\nline3\n");

            List<String> result = TailReader.lastLines(file, 3);

            assertThat(result).containsExactly("line1", "line2", "line3");
        }

        @Test
        @DisplayName("请求1行")
        void testRequestOneLine() throws IOException {
            Path file = tempDir.resolve("one.txt");
            Files.writeString(file, "first\nsecond\nthird\n");

            List<String> result = TailReader.lastLines(file, 1);

            assertThat(result).containsExactly("third");
        }
    }

    @Nested
    @DisplayName("大文件测试")
    class LargeFileTests {

        @Test
        @DisplayName("大文件（超过块大小）")
        void testLargeFile() throws IOException {
            Path file = tempDir.resolve("large.txt");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 2000; i++) {
                sb.append("line").append(i).append("\n");
            }
            Files.writeString(file, sb.toString());

            List<String> result = TailReader.lastLines(file, 5);

            assertThat(result).containsExactly(
                    "line1995", "line1996", "line1997", "line1998", "line1999"
            );
        }

        @Test
        @DisplayName("大文件请求全部行")
        void testLargeFileAllLines() throws IOException {
            Path file = tempDir.resolve("large_all.txt");
            StringBuilder sb = new StringBuilder();
            int lineCount = 100;
            for (int i = 0; i < lineCount; i++) {
                sb.append("line").append(i).append("\n");
            }
            Files.writeString(file, sb.toString());

            List<String> result = TailReader.lastLines(file, 200);

            assertThat(result).hasSize(lineCount);
            assertThat(result.getFirst()).isEqualTo("line0");
            assertThat(result.getLast()).isEqualTo("line99");
        }
    }

    @Nested
    @DisplayName("字符集测试")
    class CharsetTests {

        @Test
        @DisplayName("UTF-8中文内容")
        void testUtf8Chinese() throws IOException {
            Path file = tempDir.resolve("chinese.txt");
            Files.writeString(file, "第一行\n第二行\n第三行\n", StandardCharsets.UTF_8);

            List<String> result = TailReader.lastLines(file, 2, StandardCharsets.UTF_8);

            assertThat(result).containsExactly("第二行", "第三行");
        }

        @Test
        @DisplayName("ISO-8859-1字符集")
        void testIso88591() throws IOException {
            Path file = tempDir.resolve("iso.txt");
            Charset charset = StandardCharsets.ISO_8859_1;
            Files.write(file, List.of("caf\u00e9", "na\u00efve", "r\u00e9sum\u00e9"), charset);

            List<String> result = TailReader.lastLines(file, 2, charset);

            assertThat(result).containsExactly("na\u00efve", "r\u00e9sum\u00e9");
        }
    }

    // ==================== lastBytes 测试 ====================

    @Nested
    @DisplayName("lastBytes 测试")
    class LastBytesTests {

        @Test
        @DisplayName("读取最后N个字节")
        void testLastBytes() throws IOException {
            Path file = tempDir.resolve("bytes.txt");
            Files.writeString(file, "Hello World");

            byte[] result = TailReader.lastBytes(file, 5);

            assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo("World");
        }

        @Test
        @DisplayName("请求超过文件大小 — 返回全部")
        void testLastBytesMoreThanFileSize() throws IOException {
            Path file = tempDir.resolve("small.txt");
            Files.writeString(file, "Hi");

            byte[] result = TailReader.lastBytes(file, 100);

            assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo("Hi");
        }

        @Test
        @DisplayName("读取最后1个字节")
        void testLastOneByte() throws IOException {
            Path file = tempDir.resolve("onebyte.txt");
            Files.writeString(file, "ABCDE");

            byte[] result = TailReader.lastBytes(file, 1);

            assertThat(result).containsExactly((byte) 'E');
        }
    }

    // ==================== 参数校验测试 ====================

    @Nested
    @DisplayName("参数校验测试")
    class ValidationTests {

        @Test
        @DisplayName("lastLines — count为0抛出异常")
        void testLastLinesZeroCount() {
            Path file = tempDir.resolve("dummy.txt");
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> TailReader.lastLines(file, 0));
        }

        @Test
        @DisplayName("lastLines — count为负数抛出异常")
        void testLastLinesNegativeCount() {
            Path file = tempDir.resolve("dummy.txt");
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> TailReader.lastLines(file, -1));
        }

        @Test
        @DisplayName("lastLines — path为null抛出异常")
        void testLastLinesNullPath() {
            assertThatNullPointerException()
                    .isThrownBy(() -> TailReader.lastLines(null, 1));
        }

        @Test
        @DisplayName("lastBytes — count为0抛出异常")
        void testLastBytesZeroCount() {
            Path file = tempDir.resolve("dummy.txt");
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> TailReader.lastBytes(file, 0));
        }

        @Test
        @DisplayName("lastBytes — path为null抛出异常")
        void testLastBytesNullPath() {
            assertThatNullPointerException()
                    .isThrownBy(() -> TailReader.lastBytes(null, 1));
        }

        @Test
        @DisplayName("lastLines — 不存在的文件抛出OpenIOOperationException")
        void testLastLinesNonexistentFile() {
            Path file = tempDir.resolve("nonexistent.txt");
            assertThatThrownBy(() -> TailReader.lastLines(file, 1))
                    .isInstanceOf(OpenIOOperationException.class);
        }

        @Test
        @DisplayName("lastBytes — 不存在的文件抛出OpenIOOperationException")
        void testLastBytesNonexistentFile() {
            Path file = tempDir.resolve("nonexistent.txt");
            assertThatThrownBy(() -> TailReader.lastBytes(file, 1))
                    .isInstanceOf(OpenIOOperationException.class);
        }
    }
}
