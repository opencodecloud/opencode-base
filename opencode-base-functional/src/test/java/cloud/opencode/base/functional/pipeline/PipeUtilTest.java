package cloud.opencode.base.functional.pipeline;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

/**
 * PipeUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
@DisplayName("PipeUtil 测试")
class PipeUtilTest {

    @Nested
    @DisplayName("pipe() 测试")
    class PipeTests {

        @Test
        @DisplayName("pipe() 创建管道并使用 then()")
        void testPipeThen() {
            String result = PipeUtil.pipe("  hello  ")
                    .then(String::trim)
                    .then(String::toUpperCase)
                    .get();

            assertThat(result).isEqualTo("HELLO");
        }

        @Test
        @DisplayName("pipe() 链式 then() 调用")
        void testPipeChain() {
            Integer result = PipeUtil.pipe("hello")
                    .then(String::length)
                    .then(n -> n * 2)
                    .get();

            assertThat(result).isEqualTo(10);
        }

        @Test
        @DisplayName("thenIfPresent() 非 null 时应用")
        void testThenIfPresentNonNull() {
            String result = PipeUtil.pipe("hello")
                    .thenIfPresent(String::toUpperCase)
                    .get();

            assertThat(result).isEqualTo("HELLO");
        }

        @Test
        @DisplayName("thenIfPresent() null 时不应用")
        void testThenIfPresentNull() {
            String result = PipeUtil.pipe((String) null)
                    .thenIfPresent(String::toUpperCase)
                    .get();

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("thenIf() 条件为真时应用")
        void testThenIfTrue() {
            String result = PipeUtil.pipe("hello")
                    .thenIf(true, String::toUpperCase)
                    .get();

            assertThat(result).isEqualTo("HELLO");
        }

        @Test
        @DisplayName("thenIf() 条件为假时不应用")
        void testThenIfFalse() {
            String result = PipeUtil.pipe("hello")
                    .thenIf(false, String::toUpperCase)
                    .get();

            assertThat(result).isEqualTo("hello");
        }

        @Test
        @DisplayName("tap() 执行副作用")
        void testTap() {
            AtomicReference<String> captured = new AtomicReference<>();
            String result = PipeUtil.pipe("hello")
                    .tap(captured::set)
                    .then(String::toUpperCase)
                    .get();

            assertThat(result).isEqualTo("HELLO");
            assertThat(captured.get()).isEqualTo("hello");
        }

        @Test
        @DisplayName("tap() null 时不执行")
        void testTapNull() {
            AtomicReference<String> captured = new AtomicReference<>();
            String result = PipeUtil.pipe((String) null)
                    .tap(captured::set)
                    .get();

            assertThat(result).isNull();
            assertThat(captured.get()).isNull();
        }

        @Test
        @DisplayName("get() 获取值")
        void testGet() {
            String result = PipeUtil.pipe("hello").get();

            assertThat(result).isEqualTo("hello");
        }

        @Test
        @DisplayName("getOrElse() 有值时返回值")
        void testGetOrElseWithValue() {
            String result = PipeUtil.pipe("hello").getOrElse("default");

            assertThat(result).isEqualTo("hello");
        }

        @Test
        @DisplayName("getOrElse() null 时返回默认值")
        void testGetOrElseNull() {
            String result = PipeUtil.pipe((String) null).getOrElse("default");

            assertThat(result).isEqualTo("default");
        }
    }

    @Nested
    @DisplayName("when() 测试")
    class WhenTests {

        @Test
        @DisplayName("when() 条件为真时应用")
        void testWhenTrue() {
            String result = PipeUtil.when(true, "hello", String::toUpperCase);

            assertThat(result).isEqualTo("HELLO");
        }

        @Test
        @DisplayName("when() 条件为假时不应用")
        void testWhenFalse() {
            String result = PipeUtil.when(false, "hello", String::toUpperCase);

            assertThat(result).isEqualTo("hello");
        }
    }

    @Nested
    @DisplayName("whenNonNull() 测试")
    class WhenNonNullTests {

        @Test
        @DisplayName("whenNonNull() 非 null 时应用")
        void testWhenNonNullNonNull() {
            Integer result = PipeUtil.whenNonNull("hello", String::length);

            assertThat(result).isEqualTo(5);
        }

        @Test
        @DisplayName("whenNonNull() null 时返回 null")
        void testWhenNonNullNull() {
            Integer result = PipeUtil.whenNonNull(null, String::length);

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("whenNonNullOrElse() 测试")
    class WhenNonNullOrElseTests {

        @Test
        @DisplayName("whenNonNullOrElse() 非 null 时应用")
        void testWhenNonNullOrElseNonNull() {
            Integer result = PipeUtil.whenNonNullOrElse("hello", String::length, 0);

            assertThat(result).isEqualTo(5);
        }

        @Test
        @DisplayName("whenNonNullOrElse() null 时返回默认值")
        void testWhenNonNullOrElseNull() {
            Integer result = PipeUtil.whenNonNullOrElse(null, String::length, 0);

            assertThat(result).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("whenMatches() 测试")
    class WhenMatchesTests {

        @Test
        @DisplayName("whenMatches() 谓词匹配时应用")
        void testWhenMatchesTrue() {
            String result = PipeUtil.whenMatches("hello", s -> s.length() > 3, String::toUpperCase);

            assertThat(result).isEqualTo("HELLO");
        }

        @Test
        @DisplayName("whenMatches() 谓词不匹配时不应用")
        void testWhenMatchesFalse() {
            String result = PipeUtil.whenMatches("hi", s -> s.length() > 3, String::toUpperCase);

            assertThat(result).isEqualTo("hi");
        }
    }

    @Nested
    @DisplayName("tap() 测试")
    class TapTests {

        @Test
        @DisplayName("tap() 执行副作用并返回原值")
        void testTap() {
            AtomicReference<String> captured = new AtomicReference<>();
            String result = PipeUtil.tap("hello", captured::set);

            assertThat(result).isEqualTo("hello");
            assertThat(captured.get()).isEqualTo("hello");
        }

        @Test
        @DisplayName("tap() null 也执行副作用")
        void testTapNull() {
            AtomicReference<String> captured = new AtomicReference<>("not null");
            String result = PipeUtil.tap(null, captured::set);

            assertThat(result).isNull();
            assertThat(captured.get()).isNull();
        }
    }

    @Nested
    @DisplayName("tapIfPresent() 测试")
    class TapIfPresentTests {

        @Test
        @DisplayName("tapIfPresent() 非 null 时执行副作用")
        void testTapIfPresentNonNull() {
            AtomicReference<String> captured = new AtomicReference<>();
            String result = PipeUtil.tapIfPresent("hello", captured::set);

            assertThat(result).isEqualTo("hello");
            assertThat(captured.get()).isEqualTo("hello");
        }

        @Test
        @DisplayName("tapIfPresent() null 时不执行副作用")
        void testTapIfPresentNull() {
            AtomicReference<String> captured = new AtomicReference<>("original");
            String result = PipeUtil.tapIfPresent(null, captured::set);

            assertThat(result).isNull();
            assertThat(captured.get()).isEqualTo("original");
        }
    }

    @Nested
    @DisplayName("transform() 测试")
    class TransformTests {

        @Test
        @DisplayName("transform() 转换集合")
        void testTransform() {
            List<Integer> result = PipeUtil.transform(List.of("a", "bb", "ccc"), String::length);

            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("transform() 空集合返回空列表")
        void testTransformEmpty() {
            List<Integer> result = PipeUtil.transform(List.of(), String::length);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("filterAndTransform() 测试")
    class FilterAndTransformTests {

        @Test
        @DisplayName("filterAndTransform() 过滤并转换")
        void testFilterAndTransform() {
            List<Integer> result = PipeUtil.filterAndTransform(
                    List.of("a", "bb", "ccc", "dddd"),
                    s -> s.length() > 1,
                    String::length
            );

            assertThat(result).containsExactly(2, 3, 4);
        }
    }

    @Nested
    @DisplayName("filterNonNull() 测试")
    class FilterNonNullTests {

        @Test
        @DisplayName("filterNonNull() 移除 null 元素")
        void testFilterNonNull() {
            List<String> result = PipeUtil.filterNonNull(
                    java.util.Arrays.asList("a", null, "b", null, "c")
            );

            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("filterNonNull() 无 null 时保持不变")
        void testFilterNonNullNoNulls() {
            List<String> result = PipeUtil.filterNonNull(List.of("a", "b", "c"));

            assertThat(result).containsExactly("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("chain() 测试")
    class ChainTests {

        @Test
        @DisplayName("chain() 链接两个函数")
        void testChainTwo() {
            var chained = PipeUtil.chain(String::trim, String::toUpperCase);

            assertThat(chained.apply("  hello  ")).isEqualTo("HELLO");
        }

        @Test
        @DisplayName("chain() 链接三个函数")
        void testChainThree() {
            var chained = PipeUtil.chain(
                    String::trim,
                    String::toUpperCase,
                    s -> s + "!"
            );

            assertThat(chained.apply("  hello  ")).isEqualTo("HELLO!");
        }
    }

    @Nested
    @DisplayName("sequence() 测试")
    class SequenceTests {

        @Test
        @DisplayName("sequence() 按顺序应用多个转换")
        void testSequence() {
            var sequenced = PipeUtil.sequence(
                    String::trim,
                    String::toUpperCase,
                    s -> s + "!"
            );

            assertThat(sequenced.apply("  hello  ")).isEqualTo("HELLO!");
        }

        @Test
        @DisplayName("sequence() 空数组返回恒等")
        void testSequenceEmpty() {
            var sequenced = PipeUtil.<String>sequence();

            assertThat(sequenced.apply("hello")).isEqualTo("hello");
        }
    }

    @Nested
    @DisplayName("apply() 测试")
    class ApplyTests {

        @Test
        @DisplayName("apply() 将值应用于函数")
        void testApply() {
            Integer result = PipeUtil.apply("hello", String::length);

            assertThat(result).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("完整场景测试")
    class CompleteScenarioTests {

        @Test
        @DisplayName("管道操作符风格")
        void testPipeOperatorStyle() {
            String result = PipeUtil.pipe("  hello world  ")
                    .then(String::trim)
                    .then(String::toUpperCase)
                    .then(s -> s.replace(" ", "_"))
                    .tap(s -> System.out.println("Processing: " + s))
                    .then(s -> s + "!")
                    .get();

            assertThat(result).isEqualTo("HELLO_WORLD!");
        }

        @Test
        @DisplayName("条件转换")
        void testConditionalTransformation() {
            String input = "  HELLO  ";
            boolean shouldLowerCase = true;

            String result = PipeUtil.pipe(input)
                    .then(String::trim)
                    .thenIf(shouldLowerCase, String::toLowerCase)
                    .get();

            assertThat(result).isEqualTo("hello");
        }

        @Test
        @DisplayName("函数链接")
        void testFunctionChaining() {
            var parseAndDouble = PipeUtil.chain(
                    String::trim,
                    Integer::parseInt,
                    n -> n * 2
            );

            assertThat(parseAndDouble.apply("  21  ")).isEqualTo(42);
        }
    }
}
