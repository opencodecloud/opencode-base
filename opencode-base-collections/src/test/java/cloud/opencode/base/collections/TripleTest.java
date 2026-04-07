package cloud.opencode.base.collections;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * TripleTest Tests
 * TripleTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.3
 */
@DisplayName("Triple 三元组测试")
class TripleTest {

    @Nested
    @DisplayName("of() 工厂方法")
    class OfTests {

        @Test
        @DisplayName("创建正常的Triple")
        void testOfNormal() {
            Triple<String, Integer, Boolean> triple = Triple.of("hello", 42, true);

            assertThat(triple.first()).isEqualTo("hello");
            assertThat(triple.second()).isEqualTo(42);
            assertThat(triple.third()).isTrue();
        }

        @Test
        @DisplayName("允许null值")
        void testOfWithNulls() {
            Triple<String, Integer, Boolean> triple = Triple.of(null, null, null);

            assertThat(triple.first()).isNull();
            assertThat(triple.second()).isNull();
            assertThat(triple.third()).isNull();
        }
    }

    @Nested
    @DisplayName("map 变换方法")
    class MapTests {

        @Test
        @DisplayName("mapFirst 变换第一个元素")
        void testMapFirst() {
            Triple<String, Integer, Boolean> triple = Triple.of("hello", 42, true);
            Triple<Integer, Integer, Boolean> mapped = triple.mapFirst(String::length);

            assertThat(mapped.first()).isEqualTo(5);
            assertThat(mapped.second()).isEqualTo(42);
            assertThat(mapped.third()).isTrue();
        }

        @Test
        @DisplayName("mapSecond 变换第二个元素")
        void testMapSecond() {
            Triple<String, Integer, Boolean> triple = Triple.of("hello", 42, true);
            Triple<String, String, Boolean> mapped = triple.mapSecond(String::valueOf);

            assertThat(mapped.first()).isEqualTo("hello");
            assertThat(mapped.second()).isEqualTo("42");
            assertThat(mapped.third()).isTrue();
        }

        @Test
        @DisplayName("mapThird 变换第三个元素")
        void testMapThird() {
            Triple<String, Integer, Boolean> triple = Triple.of("hello", 42, true);
            Triple<String, Integer, String> mapped = triple.mapThird(String::valueOf);

            assertThat(mapped.first()).isEqualTo("hello");
            assertThat(mapped.second()).isEqualTo(42);
            assertThat(mapped.third()).isEqualTo("true");
        }

        @Test
        @DisplayName("mapFirst null函数抛出NPE")
        void testMapFirstNullFn() {
            Triple<String, Integer, Boolean> triple = Triple.of("hello", 42, true);

            assertThatNullPointerException()
                    .isThrownBy(() -> triple.mapFirst(null));
        }

        @Test
        @DisplayName("mapSecond null函数抛出NPE")
        void testMapSecondNullFn() {
            Triple<String, Integer, Boolean> triple = Triple.of("hello", 42, true);

            assertThatNullPointerException()
                    .isThrownBy(() -> triple.mapSecond(null));
        }

        @Test
        @DisplayName("mapThird null函数抛出NPE")
        void testMapThirdNullFn() {
            Triple<String, Integer, Boolean> triple = Triple.of("hello", 42, true);

            assertThatNullPointerException()
                    .isThrownBy(() -> triple.mapThird(null));
        }
    }

    @Nested
    @DisplayName("drop 投影方法")
    class DropTests {

        @Test
        @DisplayName("dropThird 返回 (first, second)")
        void testDropThird() {
            Triple<String, Integer, Boolean> triple = Triple.of("a", 1, true);
            Pair<String, Integer> pair = triple.dropThird();

            assertThat(pair.first()).isEqualTo("a");
            assertThat(pair.second()).isEqualTo(1);
        }

        @Test
        @DisplayName("dropFirst 返回 (second, third)")
        void testDropFirst() {
            Triple<String, Integer, Boolean> triple = Triple.of("a", 1, true);
            Pair<Integer, Boolean> pair = triple.dropFirst();

            assertThat(pair.first()).isEqualTo(1);
            assertThat(pair.second()).isTrue();
        }

        @Test
        @DisplayName("dropSecond 返回 (first, third)")
        void testDropSecond() {
            Triple<String, Integer, Boolean> triple = Triple.of("a", 1, true);
            Pair<String, Boolean> pair = triple.dropSecond();

            assertThat(pair.first()).isEqualTo("a");
            assertThat(pair.second()).isTrue();
        }
    }

    @Nested
    @DisplayName("equals / hashCode")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("相同值的Triple相等")
        void testEquals() {
            Triple<String, Integer, Boolean> a = Triple.of("x", 1, true);
            Triple<String, Integer, Boolean> b = Triple.of("x", 1, true);

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("不同值的Triple不相等")
        void testNotEquals() {
            Triple<String, Integer, Boolean> a = Triple.of("x", 1, true);
            Triple<String, Integer, Boolean> b = Triple.of("x", 1, false);

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("null值Triple的equals")
        void testEqualsWithNulls() {
            Triple<String, Integer, Boolean> a = Triple.of(null, null, null);
            Triple<String, Integer, Boolean> b = Triple.of(null, null, null);

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("toString包含三个元素")
        void testToString() {
            Triple<String, Integer, Boolean> triple = Triple.of("hello", 42, true);
            String str = triple.toString();

            assertThat(str).contains("hello");
            assertThat(str).contains("42");
            assertThat(str).contains("true");
        }
    }
}
