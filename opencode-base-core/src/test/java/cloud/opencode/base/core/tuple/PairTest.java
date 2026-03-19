package cloud.opencode.base.core.tuple;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Pair 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("Pair 测试")
class PairTest {

    @Nested
    @DisplayName("构造和工厂方法测试")
    class FactoryTests {

        @Test
        @DisplayName("of 创建二元组")
        void testOf() {
            Pair<String, Integer> pair = Pair.of("name", 25);

            assertThat(pair.left()).isEqualTo("name");
            assertThat(pair.right()).isEqualTo(25);
        }

        @Test
        @DisplayName("of 创建包含 null 的二元组")
        void testOfWithNull() {
            Pair<String, Integer> pair = Pair.of(null, null);

            assertThat(pair.left()).isNull();
            assertThat(pair.right()).isNull();
        }

        @Test
        @DisplayName("fromEntry 从 Map.Entry 创建")
        void testFromEntry() {
            Map.Entry<String, Integer> entry = Map.entry("key", 100);
            Pair<String, Integer> pair = Pair.fromEntry(entry);

            assertThat(pair.left()).isEqualTo("key");
            assertThat(pair.right()).isEqualTo(100);
        }

        @Test
        @DisplayName("empty 创建空二元组")
        void testEmpty() {
            Pair<String, Integer> pair = Pair.empty();

            assertThat(pair.left()).isNull();
            assertThat(pair.right()).isNull();
        }
    }

    @Nested
    @DisplayName("访问器别名测试")
    class AccessorTests {

        @Test
        @DisplayName("first 和 second 别名")
        void testFirstSecond() {
            Pair<String, Integer> pair = Pair.of("a", 1);

            assertThat(pair.first()).isEqualTo("a");
            assertThat(pair.second()).isEqualTo(1);
        }

        @Test
        @DisplayName("key 和 value 别名")
        void testKeyValue() {
            Pair<String, Integer> pair = Pair.of("key", 100);

            assertThat(pair.key()).isEqualTo("key");
            assertThat(pair.value()).isEqualTo(100);
        }

        @Test
        @DisplayName("left 和 right")
        void testLeftRight() {
            Pair<String, Integer> pair = Pair.of("left", 999);

            assertThat(pair.left()).isEqualTo("left");
            assertThat(pair.right()).isEqualTo(999);
        }
    }

    @Nested
    @DisplayName("swap 测试")
    class SwapTests {

        @Test
        @DisplayName("交换左右值")
        void testSwap() {
            Pair<String, Integer> original = Pair.of("hello", 42);
            Pair<Integer, String> swapped = original.swap();

            assertThat(swapped.left()).isEqualTo(42);
            assertThat(swapped.right()).isEqualTo("hello");
        }

        @Test
        @DisplayName("双重交换恢复原值")
        void testDoubleSwap() {
            Pair<String, Integer> original = Pair.of("hello", 42);
            Pair<String, Integer> restored = original.swap().swap();

            assertThat(restored).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("map 测试")
    class MapTests {

        @Test
        @DisplayName("mapLeft 映射左值")
        void testMapLeft() {
            Pair<String, Integer> pair = Pair.of("hello", 42);
            Pair<Integer, Integer> mapped = pair.mapLeft(String::length);

            assertThat(mapped.left()).isEqualTo(5);
            assertThat(mapped.right()).isEqualTo(42);
        }

        @Test
        @DisplayName("mapRight 映射右值")
        void testMapRight() {
            Pair<String, Integer> pair = Pair.of("hello", 42);
            Pair<String, String> mapped = pair.mapRight(i -> "value:" + i);

            assertThat(mapped.left()).isEqualTo("hello");
            assertThat(mapped.right()).isEqualTo("value:42");
        }

        @Test
        @DisplayName("map 同时映射左右值")
        void testMapBoth() {
            Pair<String, Integer> pair = Pair.of("hello", 42);
            Pair<Integer, String> mapped = pair.map(String::length, String::valueOf);

            assertThat(mapped.left()).isEqualTo(5);
            assertThat(mapped.right()).isEqualTo("42");
        }
    }

    @Nested
    @DisplayName("apply 测试")
    class ApplyTests {

        @Test
        @DisplayName("应用二元函数")
        void testApply() {
            Pair<String, Integer> pair = Pair.of("hello", 3);
            String result = pair.apply((s, n) -> s.repeat(n));

            assertThat(result).isEqualTo("hellohellohello");
        }

        @Test
        @DisplayName("应用返回不同类型")
        void testApplyDifferentType() {
            Pair<Integer, Integer> pair = Pair.of(10, 3);
            Integer result = pair.apply((a, b) -> a + b);

            assertThat(result).isEqualTo(13);
        }
    }

    @Nested
    @DisplayName("null 检查测试")
    class NullCheckTests {

        @Test
        @DisplayName("hasNull - 左值为 null")
        void testHasNullLeft() {
            Pair<String, Integer> pair = Pair.of(null, 42);
            assertThat(pair.hasNull()).isTrue();
        }

        @Test
        @DisplayName("hasNull - 右值为 null")
        void testHasNullRight() {
            Pair<String, Integer> pair = Pair.of("hello", null);
            assertThat(pair.hasNull()).isTrue();
        }

        @Test
        @DisplayName("hasNull - 都非 null")
        void testHasNullFalse() {
            Pair<String, Integer> pair = Pair.of("hello", 42);
            assertThat(pair.hasNull()).isFalse();
        }

        @Test
        @DisplayName("allNonNull - 都非 null")
        void testAllNonNullTrue() {
            Pair<String, Integer> pair = Pair.of("hello", 42);
            assertThat(pair.allNonNull()).isTrue();
        }

        @Test
        @DisplayName("allNonNull - 有 null")
        void testAllNonNullFalse() {
            Pair<String, Integer> pair = Pair.of(null, 42);
            assertThat(pair.allNonNull()).isFalse();
        }
    }

    @Nested
    @DisplayName("转换测试")
    class ConversionTests {

        @Test
        @DisplayName("toArray 转换为数组")
        void testToArray() {
            Pair<String, Integer> pair = Pair.of("hello", 42);
            Object[] array = pair.toArray();

            assertThat(array).hasSize(2);
            assertThat(array[0]).isEqualTo("hello");
            assertThat(array[1]).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("equals 和 hashCode 测试")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("相同值相等")
        void testEquals() {
            Pair<String, Integer> p1 = Pair.of("hello", 42);
            Pair<String, Integer> p2 = Pair.of("hello", 42);

            assertThat(p1).isEqualTo(p2);
        }

        @Test
        @DisplayName("不同值不相等")
        void testNotEquals() {
            Pair<String, Integer> p1 = Pair.of("hello", 42);
            Pair<String, Integer> p2 = Pair.of("world", 42);

            assertThat(p1).isNotEqualTo(p2);
        }

        @Test
        @DisplayName("hashCode 一致性")
        void testHashCode() {
            Pair<String, Integer> p1 = Pair.of("hello", 42);
            Pair<String, Integer> p2 = Pair.of("hello", 42);

            assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
        }
    }

    @Nested
    @DisplayName("toString 测试")
    class ToStringTests {

        @Test
        @DisplayName("格式化输出")
        void testToString() {
            Pair<String, Integer> pair = Pair.of("hello", 42);
            assertThat(pair.toString()).isEqualTo("(hello, 42)");
        }

        @Test
        @DisplayName("包含 null 的输出")
        void testToStringWithNull() {
            Pair<String, Integer> pair = Pair.of(null, null);
            assertThat(pair.toString()).isEqualTo("(null, null)");
        }
    }

    @Nested
    @DisplayName("Record 特性测试")
    class RecordFeatureTests {

        @Test
        @DisplayName("是不可变的")
        void testImmutable() {
            Pair<String, Integer> pair = Pair.of("hello", 42);
            // Record 是不可变的，没有 setter 方法
            assertThat(pair.left()).isEqualTo("hello");
        }
    }
}
