package cloud.opencode.base.core.tuple;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Quadruple 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("Quadruple 测试")
class QuadrupleTest {

    @Nested
    @DisplayName("构造和工厂方法测试")
    class FactoryTests {

        @Test
        @DisplayName("of 创建四元组")
        void testOf() {
            Quadruple<String, Integer, Boolean, Double> quad = Quadruple.of("name", 25, true, 3.14);

            assertThat(quad.first()).isEqualTo("name");
            assertThat(quad.second()).isEqualTo(25);
            assertThat(quad.third()).isTrue();
            assertThat(quad.fourth()).isEqualTo(3.14);
        }

        @Test
        @DisplayName("of 创建包含 null 的四元组")
        void testOfWithNull() {
            Quadruple<String, Integer, Boolean, Double> quad = Quadruple.of(null, null, null, null);

            assertThat(quad.first()).isNull();
            assertThat(quad.second()).isNull();
            assertThat(quad.third()).isNull();
            assertThat(quad.fourth()).isNull();
        }

        @Test
        @DisplayName("empty 创建空四元组")
        void testEmpty() {
            Quadruple<String, Integer, Boolean, Double> quad = Quadruple.empty();

            assertThat(quad.first()).isNull();
            assertThat(quad.second()).isNull();
            assertThat(quad.third()).isNull();
            assertThat(quad.fourth()).isNull();
        }
    }

    @Nested
    @DisplayName("map 测试")
    class MapTests {

        @Test
        @DisplayName("mapFirst 映射第一个元素")
        void testMapFirst() {
            Quadruple<String, Integer, Boolean, Double> quad = Quadruple.of("hello", 42, true, 3.14);
            Quadruple<Integer, Integer, Boolean, Double> mapped = quad.mapFirst(String::length);

            assertThat(mapped.first()).isEqualTo(5);
            assertThat(mapped.second()).isEqualTo(42);
            assertThat(mapped.third()).isTrue();
            assertThat(mapped.fourth()).isEqualTo(3.14);
        }

        @Test
        @DisplayName("mapSecond 映射第二个元素")
        void testMapSecond() {
            Quadruple<String, Integer, Boolean, Double> quad = Quadruple.of("hello", 42, true, 3.14);
            Quadruple<String, String, Boolean, Double> mapped = quad.mapSecond(i -> "value:" + i);

            assertThat(mapped.first()).isEqualTo("hello");
            assertThat(mapped.second()).isEqualTo("value:42");
        }

        @Test
        @DisplayName("mapThird 映射第三个元素")
        void testMapThird() {
            Quadruple<String, Integer, Boolean, Double> quad = Quadruple.of("hello", 42, true, 3.14);
            Quadruple<String, Integer, String, Double> mapped = quad.mapThird(b -> b ? "yes" : "no");

            assertThat(mapped.third()).isEqualTo("yes");
        }

        @Test
        @DisplayName("mapFourth 映射第四个元素")
        void testMapFourth() {
            Quadruple<String, Integer, Boolean, Double> quad = Quadruple.of("hello", 42, true, 3.14);
            Quadruple<String, Integer, Boolean, String> mapped = quad.mapFourth(d -> String.format("%.1f", d));

            assertThat(mapped.fourth()).isEqualTo("3.1");
        }
    }

    @Nested
    @DisplayName("Pair 提取测试")
    class PairExtractionTests {

        @Test
        @DisplayName("toFirstPair 提取前两个元素")
        void testToFirstPair() {
            Quadruple<String, Integer, Boolean, Double> quad = Quadruple.of("a", 1, true, 3.14);
            Pair<String, Integer> pair = quad.toFirstPair();

            assertThat(pair.left()).isEqualTo("a");
            assertThat(pair.right()).isEqualTo(1);
        }

        @Test
        @DisplayName("toLastPair 提取后两个元素")
        void testToLastPair() {
            Quadruple<String, Integer, Boolean, Double> quad = Quadruple.of("a", 1, true, 3.14);
            Pair<Boolean, Double> pair = quad.toLastPair();

            assertThat(pair.left()).isTrue();
            assertThat(pair.right()).isEqualTo(3.14);
        }
    }

    @Nested
    @DisplayName("Triple 提取测试")
    class TripleExtractionTests {

        @Test
        @DisplayName("toFirstTriple 提取前三个元素")
        void testToFirstTriple() {
            Quadruple<String, Integer, Boolean, Double> quad = Quadruple.of("a", 1, true, 3.14);
            Triple<String, Integer, Boolean> triple = quad.toFirstTriple();

            assertThat(triple.first()).isEqualTo("a");
            assertThat(triple.second()).isEqualTo(1);
            assertThat(triple.third()).isTrue();
        }

        @Test
        @DisplayName("toLastTriple 提取后三个元素")
        void testToLastTriple() {
            Quadruple<String, Integer, Boolean, Double> quad = Quadruple.of("a", 1, true, 3.14);
            Triple<Integer, Boolean, Double> triple = quad.toLastTriple();

            assertThat(triple.first()).isEqualTo(1);
            assertThat(triple.second()).isTrue();
            assertThat(triple.third()).isEqualTo(3.14);
        }
    }

    @Nested
    @DisplayName("null 检查测试")
    class NullCheckTests {

        @Test
        @DisplayName("hasNull - 第一个为 null")
        void testHasNullFirst() {
            Quadruple<String, Integer, Boolean, Double> quad = Quadruple.of(null, 42, true, 3.14);
            assertThat(quad.hasNull()).isTrue();
        }

        @Test
        @DisplayName("hasNull - 第四个为 null")
        void testHasNullFourth() {
            Quadruple<String, Integer, Boolean, Double> quad = Quadruple.of("hello", 42, true, null);
            assertThat(quad.hasNull()).isTrue();
        }

        @Test
        @DisplayName("hasNull - 都非 null")
        void testHasNullFalse() {
            Quadruple<String, Integer, Boolean, Double> quad = Quadruple.of("hello", 42, true, 3.14);
            assertThat(quad.hasNull()).isFalse();
        }

        @Test
        @DisplayName("allNonNull - 都非 null")
        void testAllNonNullTrue() {
            Quadruple<String, Integer, Boolean, Double> quad = Quadruple.of("hello", 42, true, 3.14);
            assertThat(quad.allNonNull()).isTrue();
        }

        @Test
        @DisplayName("allNonNull - 有 null")
        void testAllNonNullFalse() {
            Quadruple<String, Integer, Boolean, Double> quad = Quadruple.of(null, 42, true, 3.14);
            assertThat(quad.allNonNull()).isFalse();
        }
    }

    @Nested
    @DisplayName("转换测试")
    class ConversionTests {

        @Test
        @DisplayName("toArray 转换为数组")
        void testToArray() {
            Quadruple<String, Integer, Boolean, Double> quad = Quadruple.of("hello", 42, true, 3.14);
            Object[] array = quad.toArray();

            assertThat(array).hasSize(4);
            assertThat(array[0]).isEqualTo("hello");
            assertThat(array[1]).isEqualTo(42);
            assertThat(array[2]).isEqualTo(true);
            assertThat(array[3]).isEqualTo(3.14);
        }
    }

    @Nested
    @DisplayName("equals 和 hashCode 测试")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("相同值相等")
        void testEquals() {
            Quadruple<String, Integer, Boolean, Double> q1 = Quadruple.of("hello", 42, true, 3.14);
            Quadruple<String, Integer, Boolean, Double> q2 = Quadruple.of("hello", 42, true, 3.14);

            assertThat(q1).isEqualTo(q2);
        }

        @Test
        @DisplayName("不同值不相等")
        void testNotEquals() {
            Quadruple<String, Integer, Boolean, Double> q1 = Quadruple.of("hello", 42, true, 3.14);
            Quadruple<String, Integer, Boolean, Double> q2 = Quadruple.of("world", 42, true, 3.14);

            assertThat(q1).isNotEqualTo(q2);
        }

        @Test
        @DisplayName("hashCode 一致性")
        void testHashCode() {
            Quadruple<String, Integer, Boolean, Double> q1 = Quadruple.of("hello", 42, true, 3.14);
            Quadruple<String, Integer, Boolean, Double> q2 = Quadruple.of("hello", 42, true, 3.14);

            assertThat(q1.hashCode()).isEqualTo(q2.hashCode());
        }
    }

    @Nested
    @DisplayName("toString 测试")
    class ToStringTests {

        @Test
        @DisplayName("格式化输出")
        void testToString() {
            Quadruple<String, Integer, Boolean, Double> quad = Quadruple.of("hello", 42, true, 3.14);
            assertThat(quad.toString()).isEqualTo("(hello, 42, true, 3.14)");
        }

        @Test
        @DisplayName("包含 null 的输出")
        void testToStringWithNull() {
            Quadruple<String, Integer, Boolean, Double> quad = Quadruple.of(null, null, null, null);
            assertThat(quad.toString()).isEqualTo("(null, null, null, null)");
        }
    }
}
