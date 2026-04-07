package cloud.opencode.base.collections;

import org.junit.jupiter.api.*;

import java.io.*;
import java.util.AbstractMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * PairTest Tests
 * PairTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.3
 */
@DisplayName("Pair 二元组测试")
class PairTest {

    @Nested
    @DisplayName("of() 工厂方法")
    class OfTests {

        @Test
        @DisplayName("创建正常的Pair")
        void testOfNormal() {
            Pair<String, Integer> pair = Pair.of("hello", 42);

            assertThat(pair.first()).isEqualTo("hello");
            assertThat(pair.second()).isEqualTo(42);
        }

        @Test
        @DisplayName("允许null值")
        void testOfWithNulls() {
            Pair<String, Integer> pair = Pair.of(null, null);

            assertThat(pair.first()).isNull();
            assertThat(pair.second()).isNull();
        }

        @Test
        @DisplayName("允许部分null值")
        void testOfWithPartialNull() {
            Pair<String, Integer> pair = Pair.of("hello", null);

            assertThat(pair.first()).isEqualTo("hello");
            assertThat(pair.second()).isNull();
        }
    }

    @Nested
    @DisplayName("swap() 交换")
    class SwapTests {

        @Test
        @DisplayName("交换元素位置")
        void testSwap() {
            Pair<String, Integer> pair = Pair.of("a", 1);
            Pair<Integer, String> swapped = pair.swap();

            assertThat(swapped.first()).isEqualTo(1);
            assertThat(swapped.second()).isEqualTo("a");
        }

        @Test
        @DisplayName("交换含null元素")
        void testSwapWithNull() {
            Pair<String, Integer> pair = Pair.of(null, 1);
            Pair<Integer, String> swapped = pair.swap();

            assertThat(swapped.first()).isEqualTo(1);
            assertThat(swapped.second()).isNull();
        }
    }

    @Nested
    @DisplayName("map 变换方法")
    class MapTests {

        @Test
        @DisplayName("mapFirst 变换第一个元素")
        void testMapFirst() {
            Pair<String, Integer> pair = Pair.of("hello", 42);
            Pair<Integer, Integer> mapped = pair.mapFirst(String::length);

            assertThat(mapped.first()).isEqualTo(5);
            assertThat(mapped.second()).isEqualTo(42);
        }

        @Test
        @DisplayName("mapSecond 变换第二个元素")
        void testMapSecond() {
            Pair<String, Integer> pair = Pair.of("hello", 42);
            Pair<String, String> mapped = pair.mapSecond(String::valueOf);

            assertThat(mapped.first()).isEqualTo("hello");
            assertThat(mapped.second()).isEqualTo("42");
        }

        @Test
        @DisplayName("map 双参数函数")
        void testMap() {
            Pair<String, Integer> pair = Pair.of("hello", 3);
            String result = pair.map((s, n) -> s.repeat(n));

            assertThat(result).isEqualTo("hellohellohello");
        }

        @Test
        @DisplayName("mapFirst null函数抛出NPE")
        void testMapFirstNullFn() {
            Pair<String, Integer> pair = Pair.of("hello", 42);

            assertThatNullPointerException()
                    .isThrownBy(() -> pair.mapFirst(null));
        }

        @Test
        @DisplayName("mapSecond null函数抛出NPE")
        void testMapSecondNullFn() {
            Pair<String, Integer> pair = Pair.of("hello", 42);

            assertThatNullPointerException()
                    .isThrownBy(() -> pair.mapSecond(null));
        }

        @Test
        @DisplayName("map null函数抛出NPE")
        void testMapNullFn() {
            Pair<String, Integer> pair = Pair.of("hello", 42);

            assertThatNullPointerException()
                    .isThrownBy(() -> pair.map(null));
        }
    }

    @Nested
    @DisplayName("Map.Entry 接口")
    class MapEntryTests {

        @Test
        @DisplayName("getKey 返回 first")
        void testGetKey() {
            Pair<String, Integer> pair = Pair.of("key", 1);

            assertThat(pair.getKey()).isEqualTo("key");
        }

        @Test
        @DisplayName("getValue 返回 second")
        void testGetValue() {
            Pair<String, Integer> pair = Pair.of("key", 1);

            assertThat(pair.getValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("setValue 抛出 UnsupportedOperationException")
        void testSetValueThrows() {
            Pair<String, Integer> pair = Pair.of("key", 1);

            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> pair.setValue(2))
                    .withMessageContaining("immutable");
        }
    }

    @Nested
    @DisplayName("fromEntry() 从Map.Entry创建")
    class FromEntryTests {

        @Test
        @DisplayName("从Map.Entry创建Pair")
        void testFromEntry() {
            Map.Entry<String, Integer> entry = new AbstractMap.SimpleEntry<>("key", 42);
            Pair<String, Integer> pair = Pair.fromEntry(entry);

            assertThat(pair.first()).isEqualTo("key");
            assertThat(pair.second()).isEqualTo(42);
        }

        @Test
        @DisplayName("null entry抛出NPE")
        void testFromEntryNull() {
            assertThatNullPointerException()
                    .isThrownBy(() -> Pair.fromEntry(null));
        }
    }

    @Nested
    @DisplayName("equals / hashCode")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("相同值的Pair相等")
        void testEquals() {
            Pair<String, Integer> a = Pair.of("x", 1);
            Pair<String, Integer> b = Pair.of("x", 1);

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("不同值的Pair不相等")
        void testNotEquals() {
            Pair<String, Integer> a = Pair.of("x", 1);
            Pair<String, Integer> b = Pair.of("x", 2);

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("null值Pair的equals")
        void testEqualsWithNulls() {
            Pair<String, Integer> a = Pair.of(null, null);
            Pair<String, Integer> b = Pair.of(null, null);

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("toString包含两个元素")
        void testToString() {
            Pair<String, Integer> pair = Pair.of("hello", 42);
            String str = pair.toString();

            assertThat(str).contains("hello");
            assertThat(str).contains("42");
        }
    }

    @Nested
    @DisplayName("Serializable")
    class SerializableTests {

        @Test
        @DisplayName("序列化和反序列化")
        @SuppressWarnings("unchecked")
        void testSerializable() throws Exception {
            Pair<String, Integer> original = Pair.of("hello", 42);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(original);
            }

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            try (ObjectInputStream ois = new ObjectInputStream(bais)) {
                Pair<String, Integer> deserialized = (Pair<String, Integer>) ois.readObject();
                assertThat(deserialized).isEqualTo(original);
            }
        }
    }
}
