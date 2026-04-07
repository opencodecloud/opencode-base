package cloud.opencode.base.io.file;

import cloud.opencode.base.io.exception.OpenIOOperationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * LineProcessor 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.3
 */
@DisplayName("LineProcessor 测试")
class LineProcessorTest {

    @TempDir
    Path tempDir;

    private Path createFile(String name, String... lines) throws IOException {
        Path file = tempDir.resolve(name);
        Files.write(file, List.of(lines));
        return file;
    }

    // ==================== 中间操作测试 ====================

    @Nested
    @DisplayName("filter + collect 测试")
    class FilterTests {

        @Test
        @DisplayName("按谓词过滤行")
        void testFilterCollect() throws IOException {
            Path file = createFile("filter.txt", "apple", "banana", "avocado", "cherry");

            List<String> result = LineProcessor.of(file)
                    .filter(s -> s.startsWith("a"))
                    .collect();

            assertThat(result).containsExactly("apple", "avocado");
        }
    }

    @Nested
    @DisplayName("map + collect 测试")
    class MapTests {

        @Test
        @DisplayName("映射行为大写")
        void testMapCollect() throws IOException {
            Path file = createFile("map.txt", "hello", "world");

            List<String> result = LineProcessor.of(file)
                    .map(String::toUpperCase)
                    .collect();

            assertThat(result).containsExactly("HELLO", "WORLD");
        }
    }

    @Nested
    @DisplayName("skip + limit + collect 测试")
    class SkipLimitTests {

        @Test
        @DisplayName("跳过和限制行数")
        void testSkipLimitCollect() throws IOException {
            Path file = createFile("skip.txt", "a", "b", "c", "d", "e");

            List<String> result = LineProcessor.of(file)
                    .skip(1)
                    .limit(3)
                    .collect();

            assertThat(result).containsExactly("b", "c", "d");
        }
    }

    @Nested
    @DisplayName("trim + nonEmpty 测试")
    class TrimNonEmptyTests {

        @Test
        @DisplayName("去空白并过滤空行")
        void testTrimNonEmpty() throws IOException {
            Path file = createFile("trim.txt", "  hello  ", "", "  ", "world", "  ");

            List<String> result = LineProcessor.of(file)
                    .trim()
                    .nonEmpty()
                    .collect();

            assertThat(result).containsExactly("hello", "world");
        }
    }

    @Nested
    @DisplayName("grep 测试")
    class GrepTests {

        @Test
        @DisplayName("正则表达式过滤")
        void testGrep() throws IOException {
            Path file = createFile("grep.txt",
                    "ERROR: something failed",
                    "INFO: all good",
                    "ERROR: another failure",
                    "WARN: be careful");

            List<String> result = LineProcessor.of(file)
                    .grep("^ERROR")
                    .collect();

            assertThat(result).containsExactly(
                    "ERROR: something failed",
                    "ERROR: another failure"
            );
        }

        @Test
        @DisplayName("正则表达式部分匹配")
        void testGrepPartialMatch() throws IOException {
            Path file = createFile("grep2.txt", "foo bar", "baz", "foobar");

            List<String> result = LineProcessor.of(file)
                    .grep("bar")
                    .collect();

            assertThat(result).containsExactly("foo bar", "foobar");
        }
    }

    // ==================== 终端操作测试 ====================

    @Nested
    @DisplayName("forEach 测试")
    class ForEachTests {

        @Test
        @DisplayName("对每行执行操作")
        void testForEach() throws IOException {
            Path file = createFile("foreach.txt", "a", "b", "c");
            List<String> collected = new ArrayList<>();

            LineProcessor.of(file).forEach(collected::add);

            assertThat(collected).containsExactly("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("count 测试")
    class CountTests {

        @Test
        @DisplayName("统计行数")
        void testCount() throws IOException {
            Path file = createFile("count.txt", "a", "b", "c", "d");

            long count = LineProcessor.of(file).count();

            assertThat(count).isEqualTo(4);
        }

        @Test
        @DisplayName("统计过滤后的行数")
        void testCountWithFilter() throws IOException {
            Path file = createFile("count2.txt", "a", "bb", "ccc", "dd");

            long count = LineProcessor.of(file)
                    .filter(s -> s.length() > 1)
                    .count();

            assertThat(count).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("reduce 测试")
    class ReduceTests {

        @Test
        @DisplayName("归约连接所有行")
        void testReduce() throws IOException {
            Path file = createFile("reduce.txt", "a", "b", "c");

            String result = LineProcessor.of(file)
                    .reduce("", (a, b) -> a.isEmpty() ? b : a + "," + b);

            assertThat(result).isEqualTo("a,b,c");
        }
    }

    @Nested
    @DisplayName("toFile 测试")
    class ToFileTests {

        @Test
        @DisplayName("写入处理结果到文件")
        void testToFile() throws IOException {
            Path input = createFile("input.txt", "hello", "world", "test");
            Path output = tempDir.resolve("output.txt");

            LineProcessor.of(input)
                    .map(String::toUpperCase)
                    .toFile(output);

            List<String> outputLines = Files.readAllLines(output);
            assertThat(outputLines).containsExactly("HELLO", "WORLD", "TEST");
        }

        @Test
        @DisplayName("使用指定字符集写入文件")
        void testToFileWithCharset() throws IOException {
            Path input = createFile("input_cs.txt", "hello", "world");
            Path output = tempDir.resolve("output_cs.txt");

            LineProcessor.of(input)
                    .toFile(output, StandardCharsets.UTF_8);

            List<String> outputLines = Files.readAllLines(output, StandardCharsets.UTF_8);
            assertThat(outputLines).containsExactly("hello", "world");
        }
    }

    @Nested
    @DisplayName("findFirst 测试")
    class FindFirstTests {

        @Test
        @DisplayName("查找第一个匹配行")
        void testFindFirst() throws IOException {
            Path file = createFile("first.txt", "alpha", "beta", "gamma");

            Optional<String> result = LineProcessor.of(file)
                    .filter(s -> s.contains("eta"))
                    .findFirst();

            assertThat(result).isPresent().hasValue("beta");
        }

        @Test
        @DisplayName("无匹配行返回空")
        void testFindFirstEmpty() throws IOException {
            Path file = createFile("first2.txt", "alpha", "beta");

            Optional<String> result = LineProcessor.of(file)
                    .filter(s -> s.startsWith("z"))
                    .findFirst();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("anyMatch 测试")
    class AnyMatchTests {

        @Test
        @DisplayName("存在匹配行返回true")
        void testAnyMatchTrue() throws IOException {
            Path file = createFile("match.txt", "apple", "banana", "cherry");

            boolean result = LineProcessor.of(file)
                    .anyMatch(s -> s.equals("banana"));

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("无匹配行返回false")
        void testAnyMatchFalse() throws IOException {
            Path file = createFile("match2.txt", "apple", "banana");

            boolean result = LineProcessor.of(file)
                    .anyMatch(s -> s.equals("grape"));

            assertThat(result).isFalse();
        }
    }

    // ==================== 链式操作测试 ====================

    @Nested
    @DisplayName("链式操作测试")
    class ChainedOperationsTests {

        @Test
        @DisplayName("filter → map → limit → collect")
        void testChainedOperations() throws IOException {
            Path file = createFile("chain.txt",
                    "  apple  ", "  BANANA  ", "  cherry  ", "  DATE  ", "  elderberry  ");

            List<String> result = LineProcessor.of(file)
                    .trim()
                    .filter(s -> s.equals(s.toLowerCase()))
                    .map(String::toUpperCase)
                    .limit(2)
                    .collect();

            assertThat(result).containsExactly("APPLE", "CHERRY");
        }
    }

    // ==================== 边界条件测试 ====================

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("空文件返回空列表")
        void testEmptyFile() throws IOException {
            Path file = tempDir.resolve("empty.txt");
            Files.writeString(file, "");

            List<String> result = LineProcessor.of(file).collect();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("不存在的文件抛出OpenIOOperationException")
        void testNonexistentFile() {
            Path file = tempDir.resolve("nonexistent.txt");

            assertThatThrownBy(() -> LineProcessor.of(file).collect())
                    .isInstanceOf(OpenIOOperationException.class);
        }

        @Test
        @DisplayName("of(String) 工厂方法")
        void testOfString() throws IOException {
            Path file = createFile("ofstr.txt", "hello", "world");

            List<String> result = LineProcessor.of(file.toString()).collect();

            assertThat(result).containsExactly("hello", "world");
        }

        @Test
        @DisplayName("大文件处理")
        void testLargeFile() throws IOException {
            Path file = tempDir.resolve("large.txt");
            List<String> lines = new ArrayList<>();
            for (int i = 0; i < 10000; i++) {
                lines.add("line" + i);
            }
            Files.write(file, lines);

            long count = LineProcessor.of(file)
                    .filter(s -> s.startsWith("line9"))
                    .count();

            // line9, line90-line99, line900-line999, line9000-line9999
            assertThat(count).isEqualTo(1 + 10 + 100 + 1000);
        }

        @Test
        @DisplayName("null path 抛出 NullPointerException")
        void testNullPath() {
            assertThatNullPointerException()
                    .isThrownBy(() -> LineProcessor.of((Path) null));
        }

        @Test
        @DisplayName("skip负数抛出IllegalArgumentException")
        void testNegativeSkip() throws IOException {
            Path file = createFile("neg.txt", "a");
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> LineProcessor.of(file).skip(-1));
        }

        @Test
        @DisplayName("limit负数抛出IllegalArgumentException")
        void testNegativeLimit() throws IOException {
            Path file = createFile("neg2.txt", "a");
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> LineProcessor.of(file).limit(-1));
        }
    }

    // ==================== 不可变性测试 ====================

    @Nested
    @DisplayName("不可变性测试")
    class ImmutabilityTests {

        @Test
        @DisplayName("中间操作返回新实例")
        void testImmutability() throws IOException {
            Path file = createFile("immutable.txt", "a", "b", "c");

            LineProcessor base = LineProcessor.of(file);
            LineProcessor filtered = base.filter(s -> s.equals("a"));

            // base should still return all lines
            assertThat(base.collect()).containsExactly("a", "b", "c");
            // filtered should return only matching
            assertThat(filtered.collect()).containsExactly("a");
        }
    }
}
