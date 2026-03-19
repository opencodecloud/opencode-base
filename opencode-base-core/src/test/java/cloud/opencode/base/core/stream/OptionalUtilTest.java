package cloud.opencode.base.core.stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * OptionalUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("OptionalUtil 测试")
class OptionalUtilTest {

    @Nested
    @DisplayName("firstPresent 测试")
    class FirstPresentTests {

        @Test
        @DisplayName("firstPresent 第一个存在")
        void testFirstPresentFirstExists() {
            Optional<String> result = OptionalUtil.firstPresent(
                    Optional.of("first"),
                    Optional.of("second"),
                    Optional.empty()
            );
            assertThat(result).isPresent().contains("first");
        }

        @Test
        @DisplayName("firstPresent 中间存在")
        void testFirstPresentMiddleExists() {
            Optional<String> result = OptionalUtil.firstPresent(
                    Optional.empty(),
                    Optional.of("second"),
                    Optional.of("third")
            );
            assertThat(result).isPresent().contains("second");
        }

        @Test
        @DisplayName("firstPresent 全部为空")
        void testFirstPresentAllEmpty() {
            Optional<String> result = OptionalUtil.firstPresent(
                    Optional.empty(),
                    Optional.empty()
            );
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("firstPresent 包含 null Optional")
        void testFirstPresentWithNullOptional() {
            Optional<String> result = OptionalUtil.firstPresent(
                    null,
                    Optional.of("value"),
                    Optional.empty()
            );
            assertThat(result).isPresent().contains("value");
        }
    }

    @Nested
    @DisplayName("firstPresentLazy 测试")
    class FirstPresentLazyTests {

        @Test
        @DisplayName("firstPresentLazy 延迟计算")
        void testFirstPresentLazyDelayed() {
            AtomicInteger counter = new AtomicInteger(0);

            Optional<String> result = OptionalUtil.firstPresentLazy(
                    () -> {
                        counter.incrementAndGet();
                        return Optional.of("first");
                    },
                    () -> {
                        counter.incrementAndGet();
                        return Optional.of("second");
                    }
            );

            assertThat(result).isPresent().contains("first");
            assertThat(counter.get()).isEqualTo(1); // 只调用了第一个
        }

        @Test
        @DisplayName("firstPresentLazy 全部为空")
        void testFirstPresentLazyAllEmpty() {
            Optional<String> result = OptionalUtil.firstPresentLazy(
                    () -> Optional.empty(),
                    () -> Optional.empty()
            );
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("mapIfPresent 测试")
    class MapIfPresentTests {

        @Test
        @DisplayName("mapIfPresent 存在值")
        void testMapIfPresentWithValue() {
            Optional<String> opt = Optional.of("hello");
            Optional<Integer> result = OptionalUtil.mapIfPresent(opt, String::length);
            assertThat(result).isPresent().contains(5);
        }

        @Test
        @DisplayName("mapIfPresent 空值")
        void testMapIfPresentEmpty() {
            Optional<String> opt = Optional.empty();
            Optional<Integer> result = OptionalUtil.mapIfPresent(opt, String::length);
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("stream 测试")
    class StreamTests {

        @Test
        @DisplayName("stream 存在值")
        void testStreamWithValue() {
            Optional<String> opt = Optional.of("value");
            Stream<String> stream = OptionalUtil.stream(opt);
            assertThat(stream.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("stream 空值")
        void testStreamEmpty() {
            Optional<String> opt = Optional.empty();
            Stream<String> stream = OptionalUtil.stream(opt);
            assertThat(stream.count()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("combine 测试")
    class CombineTests {

        @Test
        @DisplayName("combine 两个都存在")
        void testCombineBothPresent() {
            Optional<String> opt1 = Optional.of("Hello");
            Optional<String> opt2 = Optional.of("World");

            Optional<String> result = OptionalUtil.combine(opt1, opt2, (a, b) -> a + " " + b);
            assertThat(result).isPresent().contains("Hello World");
        }

        @Test
        @DisplayName("combine 第一个为空")
        void testCombineFirstEmpty() {
            Optional<String> opt1 = Optional.empty();
            Optional<String> opt2 = Optional.of("World");

            Optional<String> result = OptionalUtil.combine(opt1, opt2, (a, b) -> a + " " + b);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("combine 第二个为空")
        void testCombineSecondEmpty() {
            Optional<String> opt1 = Optional.of("Hello");
            Optional<String> opt2 = Optional.empty();

            Optional<String> result = OptionalUtil.combine(opt1, opt2, (a, b) -> a + " " + b);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("combine 组合器返回 null")
        void testCombineCombinerReturnsNull() {
            Optional<String> opt1 = Optional.of("Hello");
            Optional<String> opt2 = Optional.of("World");

            Optional<String> result = OptionalUtil.combine(opt1, opt2, (a, b) -> null);
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("combine3 测试")
    class Combine3Tests {

        @Test
        @DisplayName("combine3 三个都存在")
        void testCombine3AllPresent() {
            Optional<String> opt1 = Optional.of("A");
            Optional<String> opt2 = Optional.of("B");
            Optional<String> opt3 = Optional.of("C");

            Optional<String> result = OptionalUtil.combine3(opt1, opt2, opt3,
                    (a, b, c) -> a + b + c);
            assertThat(result).isPresent().contains("ABC");
        }

        @Test
        @DisplayName("combine3 有一个为空")
        void testCombine3OneEmpty() {
            Optional<String> opt1 = Optional.of("A");
            Optional<String> opt2 = Optional.empty();
            Optional<String> opt3 = Optional.of("C");

            Optional<String> result = OptionalUtil.combine3(opt1, opt2, opt3,
                    (a, b, c) -> a + b + c);
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("orElseGet 和 orNull 测试")
    class OrElseTests {

        @Test
        @DisplayName("orElseGet 存在值")
        void testOrElseGetWithValue() {
            Optional<String> opt = Optional.of("value");
            String result = OptionalUtil.orElseGet(opt, () -> "default");
            assertThat(result).isEqualTo("value");
        }

        @Test
        @DisplayName("orElseGet 空值")
        void testOrElseGetEmpty() {
            Optional<String> opt = Optional.empty();
            String result = OptionalUtil.orElseGet(opt, () -> "default");
            assertThat(result).isEqualTo("default");
        }

        @Test
        @DisplayName("orNull 存在值")
        void testOrNullWithValue() {
            Optional<String> opt = Optional.of("value");
            String result = OptionalUtil.orNull(opt);
            assertThat(result).isEqualTo("value");
        }

        @Test
        @DisplayName("orNull 空值")
        void testOrNullEmpty() {
            Optional<String> opt = Optional.empty();
            String result = OptionalUtil.orNull(opt);
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("orElseThrow 测试")
    class OrElseThrowTests {

        @Test
        @DisplayName("orElseThrow 存在值")
        void testOrElseThrowWithValue() {
            Optional<String> opt = Optional.of("value");
            String result = OptionalUtil.orElseThrow(opt, () -> new RuntimeException("Empty"));
            assertThat(result).isEqualTo("value");
        }

        @Test
        @DisplayName("orElseThrow 空值抛异常")
        void testOrElseThrowEmpty() {
            Optional<String> opt = Optional.empty();
            assertThatThrownBy(() -> OptionalUtil.orElseThrow(opt, () -> new RuntimeException("Empty")))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Empty");
        }
    }

    @Nested
    @DisplayName("flatMapNullable 测试")
    class FlatMapNullableTests {

        @Test
        @DisplayName("flatMapNullable 正常映射")
        void testFlatMapNullableNormal() {
            Optional<String> opt = Optional.of("hello");
            Optional<Integer> result = OptionalUtil.flatMapNullable(opt, String::length);
            assertThat(result).isPresent().contains(5);
        }

        @Test
        @DisplayName("flatMapNullable 映射返回 null")
        void testFlatMapNullableReturnsNull() {
            Optional<String> opt = Optional.of("hello");
            Optional<String> result = OptionalUtil.flatMapNullable(opt, s -> null);
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("filterAndMap 测试")
    class FilterAndMapTests {

        @Test
        @DisplayName("filterAndMap 通过过滤")
        void testFilterAndMapPasses() {
            Optional<String> opt = Optional.of("hello");
            Optional<Integer> result = OptionalUtil.filterAndMap(opt,
                    s -> s.length() > 3,
                    String::length);
            assertThat(result).isPresent().contains(5);
        }

        @Test
        @DisplayName("filterAndMap 未通过过滤")
        void testFilterAndMapFails() {
            Optional<String> opt = Optional.of("hi");
            Optional<Integer> result = OptionalUtil.filterAndMap(opt,
                    s -> s.length() > 3,
                    String::length);
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("ifPresentOrElse 测试")
    class IfPresentOrElseTests {

        @Test
        @DisplayName("ifPresentOrElse 存在值执行 action")
        void testIfPresentOrElsePresent() {
            Optional<String> opt = Optional.of("value");
            StringBuilder sb = new StringBuilder();

            OptionalUtil.ifPresentOrElse(opt,
                    v -> sb.append("present: ").append(v),
                    () -> sb.append("empty"));

            assertThat(sb.toString()).isEqualTo("present: value");
        }

        @Test
        @DisplayName("ifPresentOrElse 空值执行 emptyAction")
        void testIfPresentOrElseEmpty() {
            Optional<String> opt = Optional.empty();
            StringBuilder sb = new StringBuilder();

            OptionalUtil.ifPresentOrElse(opt,
                    v -> sb.append("present: ").append(v),
                    () -> sb.append("empty"));

            assertThat(sb.toString()).isEqualTo("empty");
        }
    }

    @Nested
    @DisplayName("flatten 测试")
    class FlattenTests {

        @Test
        @DisplayName("flatten 嵌套 Optional")
        void testFlattenNested() {
            Optional<Optional<String>> nested = Optional.of(Optional.of("value"));
            Optional<String> result = OptionalUtil.flatten(nested);
            assertThat(result).isPresent().contains("value");
        }

        @Test
        @DisplayName("flatten 外层为空")
        void testFlattenOuterEmpty() {
            Optional<Optional<String>> nested = Optional.empty();
            Optional<String> result = OptionalUtil.flatten(nested);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("flatten 内层为空")
        void testFlattenInnerEmpty() {
            Optional<Optional<String>> nested = Optional.of(Optional.empty());
            Optional<String> result = OptionalUtil.flatten(nested);
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("allPresent 测试")
    class AllPresentTests {

        @Test
        @DisplayName("allPresent 全部存在")
        void testAllPresentTrue() {
            boolean result = OptionalUtil.allPresent(
                    Optional.of("a"),
                    Optional.of("b"),
                    Optional.of("c")
            );
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("allPresent 有一个为空")
        void testAllPresentFalse() {
            boolean result = OptionalUtil.allPresent(
                    Optional.of("a"),
                    Optional.empty(),
                    Optional.of("c")
            );
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("allPresent 包含 null Optional")
        void testAllPresentWithNullOptional() {
            boolean result = OptionalUtil.allPresent(
                    Optional.of("a"),
                    null,
                    Optional.of("c")
            );
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("anyPresent 测试")
    class AnyPresentTests {

        @Test
        @DisplayName("anyPresent 有一个存在")
        void testAnyPresentTrue() {
            boolean result = OptionalUtil.anyPresent(
                    Optional.empty(),
                    Optional.of("value"),
                    Optional.empty()
            );
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("anyPresent 全部为空")
        void testAnyPresentFalse() {
            boolean result = OptionalUtil.anyPresent(
                    Optional.empty(),
                    Optional.empty()
            );
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("anyPresent 包含 null Optional")
        void testAnyPresentWithNullOptional() {
            boolean result = OptionalUtil.anyPresent(
                    null,
                    Optional.of("value")
            );
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("TriFunction 测试")
    class TriFunctionTests {

        @Test
        @DisplayName("TriFunction 应用")
        void testTriFunctionApply() {
            OptionalUtil.TriFunction<Integer, Integer, Integer, Integer> sum =
                    (a, b, c) -> a + b + c;

            Integer result = sum.apply(1, 2, 3);
            assertThat(result).isEqualTo(6);
        }
    }
}
