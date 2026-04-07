package cloud.opencode.base.test.assertion;

import cloud.opencode.base.test.exception.AssertionException;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * MapAssertTest Tests
 * MapAssertTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.3
 */
@DisplayName("MapAssert Tests")
class MapAssertTest {

    private final Map<String, Integer> sampleMap = Map.of("a", 1, "b", 2, "c", 3);

    @Nested
    @DisplayName("Null/NotNull Tests")
    class NullNotNullTests {

        @Test
        @DisplayName("isNull should pass for null map")
        void isNullShouldPassForNull() {
            assertThatNoException().isThrownBy(() ->
                MapAssert.assertThat(null).isNull());
        }

        @Test
        @DisplayName("isNull should fail for non-null map")
        void isNullShouldFailForNonNull() {
            assertThatThrownBy(() -> MapAssert.assertThat(sampleMap).isNull())
                .isInstanceOf(AssertionException.class);
        }

        @Test
        @DisplayName("isNotNull should pass for non-null map")
        void isNotNullShouldPassForNonNull() {
            assertThatNoException().isThrownBy(() ->
                MapAssert.assertThat(sampleMap).isNotNull());
        }

        @Test
        @DisplayName("isNotNull should fail for null map")
        void isNotNullShouldFailForNull() {
            assertThatThrownBy(() -> MapAssert.assertThat(null).isNotNull())
                .isInstanceOf(AssertionException.class);
        }
    }

    @Nested
    @DisplayName("Empty/NotEmpty Tests")
    class EmptyNotEmptyTests {

        @Test
        @DisplayName("isEmpty should pass for empty map")
        void isEmptyShouldPassForEmpty() {
            assertThatNoException().isThrownBy(() ->
                MapAssert.assertThat(Map.of()).isEmpty());
        }

        @Test
        @DisplayName("isEmpty should fail for non-empty map")
        void isEmptyShouldFailForNonEmpty() {
            assertThatThrownBy(() -> MapAssert.assertThat(sampleMap).isEmpty())
                .isInstanceOf(AssertionException.class)
                .hasMessageContaining("Expected empty map");
        }

        @Test
        @DisplayName("isNotEmpty should pass for non-empty map")
        void isNotEmptyShouldPassForNonEmpty() {
            assertThatNoException().isThrownBy(() ->
                MapAssert.assertThat(sampleMap).isNotEmpty());
        }

        @Test
        @DisplayName("isNotEmpty should fail for empty map")
        void isNotEmptyShouldFailForEmpty() {
            assertThatThrownBy(() -> MapAssert.assertThat(Map.of()).isNotEmpty())
                .isInstanceOf(AssertionException.class)
                .hasMessageContaining("Expected non-empty map");
        }

        @Test
        @DisplayName("isEmpty should fail for null map")
        void isEmptyShouldFailForNull() {
            assertThatThrownBy(() -> MapAssert.assertThat(null).isEmpty())
                .isInstanceOf(AssertionException.class);
        }
    }

    @Nested
    @DisplayName("Size Tests")
    class SizeTests {

        @Test
        @DisplayName("hasSize should pass for correct size")
        void hasSizeShouldPassForCorrectSize() {
            assertThatNoException().isThrownBy(() ->
                MapAssert.assertThat(sampleMap).hasSize(3));
        }

        @Test
        @DisplayName("hasSize should fail for wrong size")
        void hasSizeShouldFailForWrongSize() {
            assertThatThrownBy(() -> MapAssert.assertThat(sampleMap).hasSize(5))
                .isInstanceOf(AssertionException.class)
                .hasMessageContaining("Expected size 5 but was 3");
        }

        @Test
        @DisplayName("hasSizeGreaterThan should pass when size is greater")
        void hasSizeGreaterThanShouldPass() {
            assertThatNoException().isThrownBy(() ->
                MapAssert.assertThat(sampleMap).hasSizeGreaterThan(2));
        }

        @Test
        @DisplayName("hasSizeGreaterThan should fail when size is equal")
        void hasSizeGreaterThanShouldFailWhenEqual() {
            assertThatThrownBy(() -> MapAssert.assertThat(sampleMap).hasSizeGreaterThan(3))
                .isInstanceOf(AssertionException.class)
                .hasMessageContaining("Expected size > 3");
        }

        @Test
        @DisplayName("hasSizeGreaterThan should fail when size is less")
        void hasSizeGreaterThanShouldFailWhenLess() {
            assertThatThrownBy(() -> MapAssert.assertThat(sampleMap).hasSizeGreaterThan(5))
                .isInstanceOf(AssertionException.class);
        }

        @Test
        @DisplayName("hasSizeLessThan should pass when size is less")
        void hasSizeLessThanShouldPass() {
            assertThatNoException().isThrownBy(() ->
                MapAssert.assertThat(sampleMap).hasSizeLessThan(5));
        }

        @Test
        @DisplayName("hasSizeLessThan should fail when size is equal")
        void hasSizeLessThanShouldFailWhenEqual() {
            assertThatThrownBy(() -> MapAssert.assertThat(sampleMap).hasSizeLessThan(3))
                .isInstanceOf(AssertionException.class)
                .hasMessageContaining("Expected size < 3");
        }

        @Test
        @DisplayName("hasSizeLessThan should fail when size is greater")
        void hasSizeLessThanShouldFailWhenGreater() {
            assertThatThrownBy(() -> MapAssert.assertThat(sampleMap).hasSizeLessThan(1))
                .isInstanceOf(AssertionException.class);
        }
    }

    @Nested
    @DisplayName("ContainsKey Tests")
    class ContainsKeyTests {

        @Test
        @DisplayName("containsKey should pass for existing key")
        void containsKeyShouldPassForExisting() {
            assertThatNoException().isThrownBy(() ->
                MapAssert.assertThat(sampleMap).containsKey("a"));
        }

        @Test
        @DisplayName("containsKey should fail for missing key")
        void containsKeyShouldFailForMissing() {
            assertThatThrownBy(() -> MapAssert.assertThat(sampleMap).containsKey("z"))
                .isInstanceOf(AssertionException.class)
                .hasMessageContaining("Expected to contain key <z>");
        }

        @Test
        @DisplayName("doesNotContainKey should pass for missing key")
        void doesNotContainKeyShouldPassForMissing() {
            assertThatNoException().isThrownBy(() ->
                MapAssert.assertThat(sampleMap).doesNotContainKey("z"));
        }

        @Test
        @DisplayName("doesNotContainKey should fail for existing key")
        void doesNotContainKeyShouldFailForExisting() {
            assertThatThrownBy(() -> MapAssert.assertThat(sampleMap).doesNotContainKey("a"))
                .isInstanceOf(AssertionException.class)
                .hasMessageContaining("Expected not to contain key <a>");
        }
    }

    @Nested
    @DisplayName("ContainsValue Tests")
    class ContainsValueTests {

        @Test
        @DisplayName("containsValue should pass for existing value")
        void containsValueShouldPassForExisting() {
            assertThatNoException().isThrownBy(() ->
                MapAssert.assertThat(sampleMap).containsValue(1));
        }

        @Test
        @DisplayName("containsValue should fail for missing value")
        void containsValueShouldFailForMissing() {
            assertThatThrownBy(() -> MapAssert.assertThat(sampleMap).containsValue(99))
                .isInstanceOf(AssertionException.class)
                .hasMessageContaining("Expected to contain value <99>");
        }

        @Test
        @DisplayName("doesNotContainValue should pass for missing value")
        void doesNotContainValueShouldPassForMissing() {
            assertThatNoException().isThrownBy(() ->
                MapAssert.assertThat(sampleMap).doesNotContainValue(99));
        }

        @Test
        @DisplayName("doesNotContainValue should fail for existing value")
        void doesNotContainValueShouldFailForExisting() {
            assertThatThrownBy(() -> MapAssert.assertThat(sampleMap).doesNotContainValue(1))
                .isInstanceOf(AssertionException.class)
                .hasMessageContaining("Expected not to contain value <1>");
        }
    }

    @Nested
    @DisplayName("ContainsEntry Tests")
    class ContainsEntryTests {

        @Test
        @DisplayName("containsEntry should pass for matching entry")
        void containsEntryShouldPassForMatch() {
            assertThatNoException().isThrownBy(() ->
                MapAssert.assertThat(sampleMap).containsEntry("a", 1));
        }

        @Test
        @DisplayName("containsEntry should fail when key missing")
        void containsEntryShouldFailWhenKeyMissing() {
            assertThatThrownBy(() -> MapAssert.assertThat(sampleMap).containsEntry("z", 1))
                .isInstanceOf(AssertionException.class)
                .hasMessageContaining("key was not found");
        }

        @Test
        @DisplayName("containsEntry should fail when value mismatches")
        void containsEntryShouldFailWhenValueMismatches() {
            assertThatThrownBy(() -> MapAssert.assertThat(sampleMap).containsEntry("a", 99))
                .isInstanceOf(AssertionException.class)
                .hasMessageContaining("expected value <99>")
                .hasMessageContaining("but was <1>");
        }

        @Test
        @DisplayName("doesNotContainEntry should pass when key missing")
        void doesNotContainEntryShouldPassWhenKeyMissing() {
            assertThatNoException().isThrownBy(() ->
                MapAssert.assertThat(sampleMap).doesNotContainEntry("z", 1));
        }

        @Test
        @DisplayName("doesNotContainEntry should pass when value differs")
        void doesNotContainEntryShouldPassWhenValueDiffers() {
            assertThatNoException().isThrownBy(() ->
                MapAssert.assertThat(sampleMap).doesNotContainEntry("a", 99));
        }

        @Test
        @DisplayName("doesNotContainEntry should fail when entry matches")
        void doesNotContainEntryShouldFailWhenEntryMatches() {
            assertThatThrownBy(() -> MapAssert.assertThat(sampleMap).doesNotContainEntry("a", 1))
                .isInstanceOf(AssertionException.class)
                .hasMessageContaining("Expected not to contain entry");
        }

        @Test
        @DisplayName("containsEntry should handle null values in map")
        void containsEntryShouldHandleNullValues() {
            Map<String, Integer> mapWithNull = new HashMap<>();
            mapWithNull.put("x", null);
            assertThatNoException().isThrownBy(() ->
                MapAssert.assertThat(mapWithNull).containsEntry("x", null));
        }
    }

    @Nested
    @DisplayName("ContainsKeys Tests")
    class ContainsKeysTests {

        @Test
        @DisplayName("containsKeys should pass when all keys present")
        void containsKeysShouldPassWhenAllPresent() {
            assertThatNoException().isThrownBy(() ->
                MapAssert.assertThat(sampleMap).containsKeys("a", "b", "c"));
        }

        @Test
        @DisplayName("containsKeys should fail when any key missing")
        void containsKeysShouldFailWhenAnyMissing() {
            assertThatThrownBy(() -> MapAssert.assertThat(sampleMap).containsKeys("a", "z"))
                .isInstanceOf(AssertionException.class)
                .hasMessageContaining("Expected to contain key <z>");
        }
    }

    @Nested
    @DisplayName("Predicate Tests")
    class PredicateTests {

        @Test
        @DisplayName("allKeysMatch should pass when all keys match")
        void allKeysMatchShouldPassWhenAllMatch() {
            assertThatNoException().isThrownBy(() ->
                MapAssert.assertThat(sampleMap).allKeysMatch(k -> k.length() == 1));
        }

        @Test
        @DisplayName("allKeysMatch should fail when any key does not match")
        void allKeysMatchShouldFailWhenAnyDoesNotMatch() {
            assertThatThrownBy(() ->
                MapAssert.assertThat(sampleMap).allKeysMatch(k -> k.equals("a")))
                .isInstanceOf(AssertionException.class)
                .hasMessageContaining("does not match predicate");
        }

        @Test
        @DisplayName("allKeysMatch should pass for empty map")
        void allKeysMatchShouldPassForEmptyMap() {
            assertThatNoException().isThrownBy(() ->
                MapAssert.<String, Integer>assertThat(Map.of()).allKeysMatch(k -> false));
        }

        @Test
        @DisplayName("allValuesMatch should pass when all values match")
        void allValuesMatchShouldPassWhenAllMatch() {
            assertThatNoException().isThrownBy(() ->
                MapAssert.assertThat(sampleMap).allValuesMatch(v -> v > 0));
        }

        @Test
        @DisplayName("allValuesMatch should fail when any value does not match")
        void allValuesMatchShouldFailWhenAnyDoesNotMatch() {
            assertThatThrownBy(() ->
                MapAssert.assertThat(sampleMap).allValuesMatch(v -> v > 2))
                .isInstanceOf(AssertionException.class)
                .hasMessageContaining("does not match predicate");
        }

        @Test
        @DisplayName("allValuesMatch should pass for empty map")
        void allValuesMatchShouldPassForEmptyMap() {
            assertThatNoException().isThrownBy(() ->
                MapAssert.<String, Integer>assertThat(Map.of()).allValuesMatch(v -> false));
        }
    }

    @Nested
    @DisplayName("IsEqualTo Tests")
    class IsEqualToTests {

        @Test
        @DisplayName("isEqualTo should pass for equal maps")
        void isEqualToShouldPassForEqualMaps() {
            Map<String, Integer> other = Map.of("a", 1, "b", 2, "c", 3);
            assertThatNoException().isThrownBy(() ->
                MapAssert.assertThat(sampleMap).isEqualTo(other));
        }

        @Test
        @DisplayName("isEqualTo should fail for different maps")
        void isEqualToShouldFailForDifferentMaps() {
            assertThatThrownBy(() -> MapAssert.assertThat(sampleMap).isEqualTo(Map.of("x", 9)))
                .isInstanceOf(AssertionException.class);
        }

        @Test
        @DisplayName("isEqualTo should pass for both null")
        void isEqualToShouldPassForBothNull() {
            assertThatNoException().isThrownBy(() ->
                MapAssert.assertThat(null).isEqualTo(null));
        }
    }

    @Nested
    @DisplayName("Fluent Chaining Tests")
    class FluentChainingTests {

        @Test
        @DisplayName("should support fluent chaining of multiple assertions")
        void shouldSupportFluentChaining() {
            assertThatNoException().isThrownBy(() ->
                MapAssert.assertThat(sampleMap)
                    .isNotNull()
                    .isNotEmpty()
                    .hasSize(3)
                    .hasSizeGreaterThan(1)
                    .hasSizeLessThan(10)
                    .containsKey("a")
                    .doesNotContainKey("z")
                    .containsValue(1)
                    .doesNotContainValue(99)
                    .containsEntry("b", 2)
                    .doesNotContainEntry("a", 99)
                    .containsKeys("a", "b")
                    .allKeysMatch(k -> k.length() == 1)
                    .allValuesMatch(v -> v > 0));
        }
    }
}
