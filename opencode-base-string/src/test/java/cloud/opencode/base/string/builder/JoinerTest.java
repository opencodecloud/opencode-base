package cloud.opencode.base.string.builder;

import org.junit.jupiter.api.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * JoinerTest Tests
 * JoinerTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("Joiner Tests")
class JoinerTest {

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("on(String) should create joiner with string separator")
        void onStringShouldCreateJoinerWithStringSeparator() {
            Joiner joiner = Joiner.on(", ");
            assertThat(joiner.join(List.of("a", "b", "c"))).isEqualTo("a, b, c");
        }

        @Test
        @DisplayName("on(char) should create joiner with char separator")
        void onCharShouldCreateJoinerWithCharSeparator() {
            Joiner joiner = Joiner.on(',');
            assertThat(joiner.join(List.of("a", "b", "c"))).isEqualTo("a,b,c");
        }
    }

    @Nested
    @DisplayName("join Iterable Tests")
    class JoinIterableTests {

        @Test
        @DisplayName("join(Iterable) should join elements")
        void joinIterableShouldJoinElements() {
            Joiner joiner = Joiner.on("-");
            assertThat(joiner.join(List.of("a", "b", "c"))).isEqualTo("a-b-c");
        }

        @Test
        @DisplayName("join(Iterable) should handle single element")
        void joinIterableShouldHandleSingleElement() {
            Joiner joiner = Joiner.on("-");
            assertThat(joiner.join(List.of("a"))).isEqualTo("a");
        }

        @Test
        @DisplayName("join(Iterable) should handle empty iterable")
        void joinIterableShouldHandleEmptyIterable() {
            Joiner joiner = Joiner.on("-");
            assertThat(joiner.join(List.of())).isEmpty();
        }
    }

    @Nested
    @DisplayName("join Array Tests")
    class JoinArrayTests {

        @Test
        @DisplayName("join(Object[]) should join array elements")
        void joinArrayShouldJoinArrayElements() {
            Joiner joiner = Joiner.on("-");
            assertThat(joiner.join(new Object[]{"a", "b", "c"})).isEqualTo("a-b-c");
        }
    }

    @Nested
    @DisplayName("join Varargs Tests")
    class JoinVarargsTests {

        @Test
        @DisplayName("join(first, second, rest...) should join varargs")
        void joinVarargsShouldJoinVarargs() {
            Joiner joiner = Joiner.on("-");
            assertThat(joiner.join("a", "b", "c", "d")).isEqualTo("a-b-c-d");
        }

        @Test
        @DisplayName("join(first, second) should join two elements")
        void joinTwoShouldJoinTwoElements() {
            Joiner joiner = Joiner.on("-");
            assertThat(joiner.join("a", "b")).isEqualTo("a-b");
        }
    }

    @Nested
    @DisplayName("skipNulls Tests")
    class SkipNullsTests {

        @Test
        @DisplayName("skipNulls should skip null elements")
        void skipNullsShouldSkipNullElements() {
            Joiner joiner = Joiner.on("-").skipNulls();
            assertThat(joiner.join(Arrays.asList("a", null, "c"))).isEqualTo("a-c");
        }

        @Test
        @DisplayName("skipNulls should return same instance if already skipping")
        void skipNullsShouldReturnSameInstanceIfAlreadySkipping() {
            Joiner joiner = Joiner.on("-").skipNulls();
            assertThat(joiner.skipNulls()).isSameAs(joiner);
        }

        @Test
        @DisplayName("skipNulls should throw if useForNull already set")
        void skipNullsShouldThrowIfUseForNullAlreadySet() {
            Joiner joiner = Joiner.on("-").useForNull("N/A");
            assertThatThrownBy(joiner::skipNulls)
                .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("useForNull Tests")
    class UseForNullTests {

        @Test
        @DisplayName("useForNull should replace null with specified string")
        void useForNullShouldReplaceNullWithSpecifiedString() {
            Joiner joiner = Joiner.on("-").useForNull("N/A");
            assertThat(joiner.join(Arrays.asList("a", null, "c"))).isEqualTo("a-N/A-c");
        }

        @Test
        @DisplayName("useForNull should return same instance if already set")
        void useForNullShouldReturnSameInstanceIfAlreadySet() {
            Joiner joiner = Joiner.on("-").useForNull("N/A");
            assertThat(joiner.useForNull("other")).isSameAs(joiner);
        }

        @Test
        @DisplayName("useForNull should throw if skipNulls already set")
        void useForNullShouldThrowIfSkipNullsAlreadySet() {
            Joiner joiner = Joiner.on("-").skipNulls();
            assertThatThrownBy(() -> joiner.useForNull("N/A"))
                .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Null Handling Tests")
    class NullHandlingTests {

        @Test
        @DisplayName("join should throw for null element by default")
        void joinShouldThrowForNullElementByDefault() {
            Joiner joiner = Joiner.on("-");
            assertThatThrownBy(() -> joiner.join(Arrays.asList("a", null, "c")))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("appendTo Tests")
    class AppendToTests {

        @Test
        @DisplayName("appendTo StringBuilder should append joined string")
        void appendToStringBuilderShouldAppendJoinedString() {
            Joiner joiner = Joiner.on("-");
            StringBuilder sb = new StringBuilder("prefix:");
            joiner.appendTo(sb, List.of("a", "b", "c"));
            assertThat(sb.toString()).isEqualTo("prefix:a-b-c");
        }

        @Test
        @DisplayName("appendTo Appendable should append joined string")
        void appendToAppendableShouldAppendJoinedString() {
            Joiner joiner = Joiner.on("-");
            StringBuilder sb = new StringBuilder();
            Appendable result = joiner.appendTo((Appendable) sb, List.of("a", "b"));
            assertThat(result.toString()).isEqualTo("a-b");
        }
    }

    @Nested
    @DisplayName("MapJoiner Tests")
    class MapJoinerTests {

        @Test
        @DisplayName("withKeyValueSeparator(String) should create MapJoiner")
        void withKeyValueSeparatorStringShouldCreateMapJoiner() {
            Joiner.MapJoiner mapJoiner = Joiner.on("&").withKeyValueSeparator("=");
            Map<String, String> map = new LinkedHashMap<>();
            map.put("a", "1");
            map.put("b", "2");
            assertThat(mapJoiner.join(map)).isEqualTo("a=1&b=2");
        }

        @Test
        @DisplayName("withKeyValueSeparator(char) should create MapJoiner")
        void withKeyValueSeparatorCharShouldCreateMapJoiner() {
            Joiner.MapJoiner mapJoiner = Joiner.on("&").withKeyValueSeparator('=');
            Map<String, String> map = new LinkedHashMap<>();
            map.put("key", "value");
            assertThat(mapJoiner.join(map)).isEqualTo("key=value");
        }

        @Test
        @DisplayName("MapJoiner appendTo should work")
        void mapJoinerAppendToShouldWork() {
            Joiner.MapJoiner mapJoiner = Joiner.on("&").withKeyValueSeparator("=");
            Map<String, String> map = new LinkedHashMap<>();
            map.put("a", "1");
            StringBuilder sb = new StringBuilder();
            mapJoiner.appendTo(sb, map);
            assertThat(sb.toString()).isEqualTo("a=1");
        }

        @Test
        @DisplayName("MapJoiner useForNull should work")
        void mapJoinerUseForNullShouldWork() {
            Joiner.MapJoiner mapJoiner = Joiner.on("&").withKeyValueSeparator("=").useForNull("N/A");
            Map<String, String> map = new LinkedHashMap<>();
            map.put("a", null);
            assertThat(mapJoiner.join(map)).isEqualTo("a=N/A");
        }
    }
}
