package cloud.opencode.base.core.tuple;

import cloud.opencode.base.core.func.TriFunction;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Triple 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("Triple 测试")
class TripleTest {

    @Nested
    @DisplayName("构造和工厂方法测试")
    class FactoryTests {

        @Test
        @DisplayName("of 创建三元组")
        void testOf() {
            Triple<String, Integer, Boolean> triple = Triple.of("name", 25, true);

            assertThat(triple.first()).isEqualTo("name");
            assertThat(triple.second()).isEqualTo(25);
            assertThat(triple.third()).isTrue();
        }

        @Test
        @DisplayName("of 创建包含 null 的三元组")
        void testOfWithNull() {
            Triple<String, Integer, Boolean> triple = Triple.of(null, null, null);

            assertThat(triple.first()).isNull();
            assertThat(triple.second()).isNull();
            assertThat(triple.third()).isNull();
        }

        @Test
        @DisplayName("empty 创建空三元组")
        void testEmpty() {
            Triple<String, Integer, Boolean> triple = Triple.empty();

            assertThat(triple.first()).isNull();
            assertThat(triple.second()).isNull();
            assertThat(triple.third()).isNull();
        }
    }

    @Nested
    @DisplayName("访问器别名测试")
    class AccessorTests {

        @Test
        @DisplayName("first, second, third")
        void testFirstSecondThird() {
            Triple<String, Integer, Boolean> triple = Triple.of("a", 1, true);

            assertThat(triple.first()).isEqualTo("a");
            assertThat(triple.second()).isEqualTo(1);
            assertThat(triple.third()).isTrue();
        }

        @Test
        @DisplayName("left, middle, right 别名")
        void testLeftMiddleRight() {
            Triple<String, Integer, Boolean> triple = Triple.of("left", 100, false);

            assertThat(triple.left()).isEqualTo("left");
            assertThat(triple.middle()).isEqualTo(100);
            assertThat(triple.right()).isFalse();
        }
    }

    @Nested
    @DisplayName("map 测试")
    class MapTests {

        @Test
        @DisplayName("mapFirst 映射第一个元素")
        void testMapFirst() {
            Triple<String, Integer, Boolean> triple = Triple.of("hello", 42, true);
            Triple<Integer, Integer, Boolean> mapped = triple.mapFirst(String::length);

            assertThat(mapped.first()).isEqualTo(5);
            assertThat(mapped.second()).isEqualTo(42);
            assertThat(mapped.third()).isTrue();
        }

        @Test
        @DisplayName("mapSecond 映射第二个元素")
        void testMapSecond() {
            Triple<String, Integer, Boolean> triple = Triple.of("hello", 42, true);
            Triple<String, String, Boolean> mapped = triple.mapSecond(i -> "value:" + i);

            assertThat(mapped.first()).isEqualTo("hello");
            assertThat(mapped.second()).isEqualTo("value:42");
            assertThat(mapped.third()).isTrue();
        }

        @Test
        @DisplayName("mapThird 映射第三个元素")
        void testMapThird() {
            Triple<String, Integer, Boolean> triple = Triple.of("hello", 42, true);
            Triple<String, Integer, String> mapped = triple.mapThird(b -> b ? "yes" : "no");

            assertThat(mapped.first()).isEqualTo("hello");
            assertThat(mapped.second()).isEqualTo(42);
            assertThat(mapped.third()).isEqualTo("yes");
        }

        @Test
        @DisplayName("map 同时映射所有元素")
        void testMapAll() {
            Triple<String, Integer, Boolean> triple = Triple.of("hello", 42, true);
            Triple<Integer, String, String> mapped = triple.map(
                    String::length,
                    String::valueOf,
                    b -> b ? "yes" : "no"
            );

            assertThat(mapped.first()).isEqualTo(5);
            assertThat(mapped.second()).isEqualTo("42");
            assertThat(mapped.third()).isEqualTo("yes");
        }
    }

    @Nested
    @DisplayName("apply 测试")
    class ApplyTests {

        @Test
        @DisplayName("应用三元函数")
        void testApply() {
            Triple<String, Integer, String> triple = Triple.of("Hello", 3, "!");
            String result = triple.apply((s, n, suffix) -> s.repeat(n) + suffix);

            assertThat(result).isEqualTo("HelloHelloHello!");
        }

        @Test
        @DisplayName("应用返回不同类型")
        void testApplyDifferentType() {
            Triple<Integer, Integer, Integer> triple = Triple.of(10, 20, 30);
            Integer result = triple.apply((a, b, c) -> a + b + c);

            assertThat(result).isEqualTo(60);
        }
    }

    @Nested
    @DisplayName("Pair 提取测试")
    class PairExtractionTests {

        @Test
        @DisplayName("toFirstPair 提取前两个元素")
        void testToFirstPair() {
            Triple<String, Integer, Boolean> triple = Triple.of("a", 1, true);
            Pair<String, Integer> pair = triple.toFirstPair();

            assertThat(pair.left()).isEqualTo("a");
            assertThat(pair.right()).isEqualTo(1);
        }

        @Test
        @DisplayName("toLastPair 提取后两个元素")
        void testToLastPair() {
            Triple<String, Integer, Boolean> triple = Triple.of("a", 1, true);
            Pair<Integer, Boolean> pair = triple.toLastPair();

            assertThat(pair.left()).isEqualTo(1);
            assertThat(pair.right()).isTrue();
        }
    }

    @Nested
    @DisplayName("null 检查测试")
    class NullCheckTests {

        @Test
        @DisplayName("hasNull - 第一个为 null")
        void testHasNullFirst() {
            Triple<String, Integer, Boolean> triple = Triple.of(null, 42, true);
            assertThat(triple.hasNull()).isTrue();
        }

        @Test
        @DisplayName("hasNull - 第二个为 null")
        void testHasNullSecond() {
            Triple<String, Integer, Boolean> triple = Triple.of("hello", null, true);
            assertThat(triple.hasNull()).isTrue();
        }

        @Test
        @DisplayName("hasNull - 第三个为 null")
        void testHasNullThird() {
            Triple<String, Integer, Boolean> triple = Triple.of("hello", 42, null);
            assertThat(triple.hasNull()).isTrue();
        }

        @Test
        @DisplayName("hasNull - 都非 null")
        void testHasNullFalse() {
            Triple<String, Integer, Boolean> triple = Triple.of("hello", 42, true);
            assertThat(triple.hasNull()).isFalse();
        }

        @Test
        @DisplayName("allNonNull - 都非 null")
        void testAllNonNullTrue() {
            Triple<String, Integer, Boolean> triple = Triple.of("hello", 42, true);
            assertThat(triple.allNonNull()).isTrue();
        }

        @Test
        @DisplayName("allNonNull - 有 null")
        void testAllNonNullFalse() {
            Triple<String, Integer, Boolean> triple = Triple.of(null, 42, true);
            assertThat(triple.allNonNull()).isFalse();
        }
    }

    @Nested
    @DisplayName("转换测试")
    class ConversionTests {

        @Test
        @DisplayName("toArray 转换为数组")
        void testToArray() {
            Triple<String, Integer, Boolean> triple = Triple.of("hello", 42, true);
            Object[] array = triple.toArray();

            assertThat(array).hasSize(3);
            assertThat(array[0]).isEqualTo("hello");
            assertThat(array[1]).isEqualTo(42);
            assertThat(array[2]).isEqualTo(true);
        }
    }

    @Nested
    @DisplayName("equals 和 hashCode 测试")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("相同值相等")
        void testEquals() {
            Triple<String, Integer, Boolean> t1 = Triple.of("hello", 42, true);
            Triple<String, Integer, Boolean> t2 = Triple.of("hello", 42, true);

            assertThat(t1).isEqualTo(t2);
        }

        @Test
        @DisplayName("不同值不相等")
        void testNotEquals() {
            Triple<String, Integer, Boolean> t1 = Triple.of("hello", 42, true);
            Triple<String, Integer, Boolean> t2 = Triple.of("world", 42, true);

            assertThat(t1).isNotEqualTo(t2);
        }

        @Test
        @DisplayName("hashCode 一致性")
        void testHashCode() {
            Triple<String, Integer, Boolean> t1 = Triple.of("hello", 42, true);
            Triple<String, Integer, Boolean> t2 = Triple.of("hello", 42, true);

            assertThat(t1.hashCode()).isEqualTo(t2.hashCode());
        }
    }

    @Nested
    @DisplayName("toString 测试")
    class ToStringTests {

        @Test
        @DisplayName("格式化输出")
        void testToString() {
            Triple<String, Integer, Boolean> triple = Triple.of("hello", 42, true);
            assertThat(triple.toString()).isEqualTo("(hello, 42, true)");
        }

        @Test
        @DisplayName("包含 null 的输出")
        void testToStringWithNull() {
            Triple<String, Integer, Boolean> triple = Triple.of(null, null, null);
            assertThat(triple.toString()).isEqualTo("(null, null, null)");
        }
    }

    @Nested
    @DisplayName("TriFunction 测试")
    class TriFunctionTests {

        @Test
        @DisplayName("TriFunction 作为函数式接口")
        void testTriFunction() {
            TriFunction<Integer, Integer, Integer, Integer> sum = (a, b, c) -> a + b + c;
            assertThat(sum.apply(1, 2, 3)).isEqualTo(6);
        }
    }
}
