package cloud.opencode.base.collections;

import org.junit.jupiter.api.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * MapDifferenceTest Tests
 * MapDifferenceTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("MapDifference 接口测试")
class MapDifferenceTest {

    @Nested
    @DisplayName("MapUtil.difference通过MapDifference接口测试")
    class DifferenceThroughMapUtilTests {

        @Test
        @DisplayName("相等的maps返回areEqual为true")
        void testEqualMaps() {
            Map<String, Integer> left = Map.of("a", 1, "b", 2);
            Map<String, Integer> right = Map.of("a", 1, "b", 2);

            MapDifference<String, Integer> diff = MapUtil.difference(left, right);

            assertThat(diff.areEqual()).isTrue();
        }

        @Test
        @DisplayName("不同的maps返回areEqual为false")
        void testDifferentMaps() {
            Map<String, Integer> left = Map.of("a", 1, "b", 2);
            Map<String, Integer> right = Map.of("a", 1, "c", 3);

            MapDifference<String, Integer> diff = MapUtil.difference(left, right);

            assertThat(diff.areEqual()).isFalse();
        }

        @Test
        @DisplayName("entriesOnlyOnLeft返回仅在左边的条目")
        void testEntriesOnlyOnLeft() {
            Map<String, Integer> left = Map.of("a", 1, "b", 2);
            Map<String, Integer> right = Map.of("a", 1);

            MapDifference<String, Integer> diff = MapUtil.difference(left, right);

            assertThat(diff.entriesOnlyOnLeft()).containsEntry("b", 2);
        }

        @Test
        @DisplayName("entriesOnlyOnRight返回仅在右边的条目")
        void testEntriesOnlyOnRight() {
            Map<String, Integer> left = Map.of("a", 1);
            Map<String, Integer> right = Map.of("a", 1, "c", 3);

            MapDifference<String, Integer> diff = MapUtil.difference(left, right);

            assertThat(diff.entriesOnlyOnRight()).containsEntry("c", 3);
        }

        @Test
        @DisplayName("entriesInCommon返回共同条目")
        void testEntriesInCommon() {
            Map<String, Integer> left = Map.of("a", 1, "b", 2);
            Map<String, Integer> right = Map.of("a", 1, "c", 3);

            MapDifference<String, Integer> diff = MapUtil.difference(left, right);

            assertThat(diff.entriesInCommon()).containsEntry("a", 1);
        }

        @Test
        @DisplayName("entriesDiffering返回值不同的条目")
        void testEntriesDiffering() {
            Map<String, Integer> left = Map.of("a", 1, "b", 2);
            Map<String, Integer> right = Map.of("a", 1, "b", 99);

            MapDifference<String, Integer> diff = MapUtil.difference(left, right);

            assertThat(diff.entriesDiffering()).containsKey("b");
            assertThat(diff.entriesDiffering().get("b").leftValue()).isEqualTo(2);
            assertThat(diff.entriesDiffering().get("b").rightValue()).isEqualTo(99);
        }
    }
}
