package cloud.opencode.base.test.assertion;

import cloud.opencode.base.test.exception.AssertionException;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * CollectionAssertTest Tests
 * CollectionAssertTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("CollectionAssert Tests")
class CollectionAssertTest {

    @Nested
    @DisplayName("Null/NotNull Tests")
    class NullNotNullTests {

        @Test
        @DisplayName("isNull should pass for null")
        void isNullShouldPassForNull() {
            assertThatNoException().isThrownBy(() ->
                CollectionAssert.assertThat(null).isNull());
        }

        @Test
        @DisplayName("isNull should fail for non-null")
        void isNullShouldFailForNonNull() {
            assertThatThrownBy(() -> CollectionAssert.assertThat(List.of("a")).isNull())
                .isInstanceOf(AssertionException.class);
        }

        @Test
        @DisplayName("isNotNull should pass for non-null")
        void isNotNullShouldPassForNonNull() {
            assertThatNoException().isThrownBy(() ->
                CollectionAssert.assertThat(List.of("a")).isNotNull());
        }

        @Test
        @DisplayName("isNotNull should fail for null")
        void isNotNullShouldFailForNull() {
            assertThatThrownBy(() -> CollectionAssert.assertThat(null).isNotNull())
                .isInstanceOf(AssertionException.class);
        }
    }

    @Nested
    @DisplayName("Empty/NotEmpty Tests")
    class EmptyNotEmptyTests {

        @Test
        @DisplayName("isEmpty should pass for empty collection")
        void isEmptyShouldPassForEmptyCollection() {
            assertThatNoException().isThrownBy(() ->
                CollectionAssert.assertThat(List.of()).isEmpty());
        }

        @Test
        @DisplayName("isEmpty should fail for non-empty collection")
        void isEmptyShouldFailForNonEmptyCollection() {
            assertThatThrownBy(() -> CollectionAssert.assertThat(List.of("a")).isEmpty())
                .isInstanceOf(AssertionException.class);
        }

        @Test
        @DisplayName("isNotEmpty should pass for non-empty collection")
        void isNotEmptyShouldPassForNonEmptyCollection() {
            assertThatNoException().isThrownBy(() ->
                CollectionAssert.assertThat(List.of("a")).isNotEmpty());
        }

        @Test
        @DisplayName("isNotEmpty should fail for empty collection")
        void isNotEmptyShouldFailForEmptyCollection() {
            assertThatThrownBy(() -> CollectionAssert.assertThat(List.of()).isNotEmpty())
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
                CollectionAssert.assertThat(List.of("a", "b", "c")).hasSize(3));
        }

        @Test
        @DisplayName("hasSize should fail for wrong size")
        void hasSizeShouldFailForWrongSize() {
            assertThatThrownBy(() -> CollectionAssert.assertThat(List.of("a", "b")).hasSize(5))
                .isInstanceOf(AssertionException.class);
        }

        @Test
        @DisplayName("hasSizeGreaterThan should pass")
        void hasSizeGreaterThanShouldPass() {
            assertThatNoException().isThrownBy(() ->
                CollectionAssert.assertThat(List.of("a", "b", "c")).hasSizeGreaterThan(2));
        }

        @Test
        @DisplayName("hasSizeGreaterThan should fail")
        void hasSizeGreaterThanShouldFail() {
            assertThatThrownBy(() -> CollectionAssert.assertThat(List.of("a")).hasSizeGreaterThan(5))
                .isInstanceOf(AssertionException.class);
        }

        @Test
        @DisplayName("hasSizeLessThan should pass")
        void hasSizeLessThanShouldPass() {
            assertThatNoException().isThrownBy(() ->
                CollectionAssert.assertThat(List.of("a")).hasSizeLessThan(5));
        }

        @Test
        @DisplayName("hasSizeLessThan should fail")
        void hasSizeLessThanShouldFail() {
            assertThatThrownBy(() -> CollectionAssert.assertThat(List.of("a", "b", "c")).hasSizeLessThan(2))
                .isInstanceOf(AssertionException.class);
        }
    }

    @Nested
    @DisplayName("Contains Tests")
    class ContainsTests {

        @Test
        @DisplayName("contains should pass when element exists")
        void containsShouldPassWhenElementExists() {
            assertThatNoException().isThrownBy(() ->
                CollectionAssert.assertThat(List.of("a", "b", "c")).contains("b"));
        }

        @Test
        @DisplayName("contains should fail when element not found")
        void containsShouldFailWhenElementNotFound() {
            assertThatThrownBy(() -> CollectionAssert.assertThat(List.of("a", "b")).contains("x"))
                .isInstanceOf(AssertionException.class);
        }

        @Test
        @DisplayName("doesNotContain should pass when element not found")
        void doesNotContainShouldPassWhenElementNotFound() {
            assertThatNoException().isThrownBy(() ->
                CollectionAssert.assertThat(List.of("a", "b")).doesNotContain("x"));
        }

        @Test
        @DisplayName("doesNotContain should fail when element exists")
        void doesNotContainShouldFailWhenElementExists() {
            assertThatThrownBy(() -> CollectionAssert.assertThat(List.of("a", "b")).doesNotContain("a"))
                .isInstanceOf(AssertionException.class);
        }

        @Test
        @DisplayName("containsAll should pass when all elements exist")
        void containsAllShouldPassWhenAllElementsExist() {
            assertThatNoException().isThrownBy(() ->
                CollectionAssert.assertThat(List.of("a", "b", "c")).containsAll("a", "b"));
        }

        @Test
        @DisplayName("containsAll should fail when some elements missing")
        void containsAllShouldFailWhenSomeElementsMissing() {
            assertThatThrownBy(() -> CollectionAssert.assertThat(List.of("a", "b")).containsAll("a", "x"))
                .isInstanceOf(AssertionException.class);
        }
    }

    @Nested
    @DisplayName("ContainsExactly Tests")
    class ContainsExactlyTests {

        @Test
        @DisplayName("containsExactly should pass for exact match")
        void containsExactlyShouldPassForExactMatch() {
            assertThatNoException().isThrownBy(() ->
                CollectionAssert.assertThat(List.of("a", "b", "c")).containsExactly("a", "b", "c"));
        }

        @Test
        @DisplayName("containsExactly should fail for wrong order")
        void containsExactlyShouldFailForWrongOrder() {
            assertThatThrownBy(() -> CollectionAssert.assertThat(List.of("a", "b")).containsExactly("b", "a"))
                .isInstanceOf(AssertionException.class);
        }

        @Test
        @DisplayName("containsExactly should fail for different size")
        void containsExactlyShouldFailForDifferentSize() {
            assertThatThrownBy(() -> CollectionAssert.assertThat(List.of("a", "b")).containsExactly("a"))
                .isInstanceOf(AssertionException.class);
        }

        @Test
        @DisplayName("containsExactlyInAnyOrder should pass")
        void containsExactlyInAnyOrderShouldPass() {
            assertThatNoException().isThrownBy(() ->
                CollectionAssert.assertThat(List.of("a", "b", "c")).containsExactlyInAnyOrder("c", "a", "b"));
        }

        @Test
        @DisplayName("containsExactlyInAnyOrder should fail for different elements")
        void containsExactlyInAnyOrderShouldFailForDifferentElements() {
            assertThatThrownBy(() -> CollectionAssert.assertThat(List.of("a", "b")).containsExactlyInAnyOrder("a", "x"))
                .isInstanceOf(AssertionException.class);
        }
    }

    @Nested
    @DisplayName("Predicate Tests")
    class PredicateTests {

        @Test
        @DisplayName("allMatch should pass when all match")
        void allMatchShouldPassWhenAllMatch() {
            assertThatNoException().isThrownBy(() ->
                CollectionAssert.assertThat(List.of(2, 4, 6)).allMatch(n -> n % 2 == 0));
        }

        @Test
        @DisplayName("allMatch should fail when some don't match")
        void allMatchShouldFailWhenSomeDontMatch() {
            assertThatThrownBy(() -> CollectionAssert.assertThat(List.of(1, 2, 3)).allMatch(n -> n % 2 == 0))
                .isInstanceOf(AssertionException.class);
        }

        @Test
        @DisplayName("anyMatch should pass when one matches")
        void anyMatchShouldPassWhenOneMatches() {
            assertThatNoException().isThrownBy(() ->
                CollectionAssert.assertThat(List.of(1, 2, 3)).anyMatch(n -> n % 2 == 0));
        }

        @Test
        @DisplayName("anyMatch should fail when none match")
        void anyMatchShouldFailWhenNoneMatch() {
            assertThatThrownBy(() -> CollectionAssert.assertThat(List.of(1, 3, 5)).anyMatch(n -> n % 2 == 0))
                .isInstanceOf(AssertionException.class);
        }

        @Test
        @DisplayName("noneMatch should pass when none match")
        void noneMatchShouldPassWhenNoneMatch() {
            assertThatNoException().isThrownBy(() ->
                CollectionAssert.assertThat(List.of(1, 3, 5)).noneMatch(n -> n % 2 == 0));
        }

        @Test
        @DisplayName("noneMatch should fail when some match")
        void noneMatchShouldFailWhenSomeMatch() {
            assertThatThrownBy(() -> CollectionAssert.assertThat(List.of(1, 2, 3)).noneMatch(n -> n % 2 == 0))
                .isInstanceOf(AssertionException.class);
        }
    }

    @Nested
    @DisplayName("Duplicates Tests")
    class DuplicatesTests {

        @Test
        @DisplayName("hasNoDuplicates should pass for unique elements")
        void hasNoDuplicatesShouldPassForUniqueElements() {
            assertThatNoException().isThrownBy(() ->
                CollectionAssert.assertThat(List.of("a", "b", "c")).hasNoDuplicates());
        }

        @Test
        @DisplayName("hasNoDuplicates should fail for duplicates")
        void hasNoDuplicatesShouldFailForDuplicates() {
            assertThatThrownBy(() -> CollectionAssert.assertThat(List.of("a", "b", "a")).hasNoDuplicates())
                .isInstanceOf(AssertionException.class);
        }
    }

    @Nested
    @DisplayName("Sorted Tests")
    class SortedTests {

        @Test
        @DisplayName("isSorted should pass for sorted collection")
        void isSortedShouldPassForSortedCollection() {
            assertThatNoException().isThrownBy(() ->
                CollectionAssert.assertThat(List.of(1, 2, 3, 4)).isSorted(Comparator.naturalOrder()));
        }

        @Test
        @DisplayName("isSorted should fail for unsorted collection")
        void isSortedShouldFailForUnsortedCollection() {
            assertThatThrownBy(() -> CollectionAssert.assertThat(List.of(3, 1, 2)).isSorted(Comparator.naturalOrder()))
                .isInstanceOf(AssertionException.class);
        }
    }

    @Nested
    @DisplayName("Equality Tests")
    class EqualityTests {

        @Test
        @DisplayName("isEqualTo should pass for equal collections")
        void isEqualToShouldPassForEqualCollections() {
            assertThatNoException().isThrownBy(() ->
                CollectionAssert.assertThat(List.of("a", "b")).isEqualTo(List.of("a", "b")));
        }

        @Test
        @DisplayName("isEqualTo should fail for different collections")
        void isEqualToShouldFailForDifferentCollections() {
            assertThatThrownBy(() -> CollectionAssert.assertThat(List.of("a", "b")).isEqualTo(List.of("x", "y")))
                .isInstanceOf(AssertionException.class);
        }
    }

    @Nested
    @DisplayName("Chaining Tests")
    class ChainingTests {

        @Test
        @DisplayName("Should support fluent chaining")
        void shouldSupportFluentChaining() {
            assertThatNoException().isThrownBy(() ->
                CollectionAssert.assertThat(List.of("a", "b", "c"))
                    .isNotNull()
                    .isNotEmpty()
                    .hasSize(3)
                    .contains("b")
                    .doesNotContain("x")
                    .hasNoDuplicates());
        }
    }
}
