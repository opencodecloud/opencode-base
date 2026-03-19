package cloud.opencode.base.test.assertion;

import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenAssertionsTest Tests
 * OpenAssertionsTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("OpenAssertions Tests")
class OpenAssertionsTest {

    @Nested
    @DisplayName("ObjectAssertion Tests")
    class ObjectAssertionTests {

        @Test
        @DisplayName("assertThat should create object assertion")
        void assertThatShouldCreateObjectAssertion() {
            var assertion = OpenAssertions.assertThat(new Object());
            assertThat(assertion).isNotNull();
        }

        @Test
        @DisplayName("isNull should pass for null")
        void isNullShouldPassForNull() {
            assertThatNoException().isThrownBy(() ->
                OpenAssertions.assertThat((Object) null).isNull());
        }

        @Test
        @DisplayName("isNotNull should pass for non-null")
        void isNotNullShouldPassForNonNull() {
            assertThatNoException().isThrownBy(() ->
                OpenAssertions.assertThat(new Object()).isNotNull());
        }

        @Test
        @DisplayName("isEqualTo should pass for equal objects")
        void isEqualToShouldPassForEqualObjects() {
            assertThatNoException().isThrownBy(() ->
                OpenAssertions.assertThat("test").isEqualTo("test"));
        }

        @Test
        @DisplayName("isNotEqualTo should pass for different objects")
        void isNotEqualToShouldPassForDifferentObjects() {
            assertThatNoException().isThrownBy(() ->
                OpenAssertions.assertThat("test").isNotEqualTo("other"));
        }

        @Test
        @DisplayName("isSameAs should pass for same instance")
        void isSameAsShouldPassForSameInstance() {
            Object obj = new Object();
            assertThatNoException().isThrownBy(() ->
                OpenAssertions.assertThat(obj).isSameAs(obj));
        }

        @Test
        @DisplayName("isInstanceOf should pass for matching type")
        void isInstanceOfShouldPassForMatchingType() {
            assertThatNoException().isThrownBy(() ->
                OpenAssertions.assertThat("test").isInstanceOf(String.class));
        }

        @Test
        @DisplayName("matches should pass when predicate matches")
        void matchesShouldPassWhenPredicateMatches() {
            assertThatNoException().isThrownBy(() ->
                OpenAssertions.assertThat("hello").matches(s -> s.length() == 5));
        }

        @Test
        @DisplayName("satisfies should execute consumer")
        void satisfiesShouldExecuteConsumer() {
            assertThatNoException().isThrownBy(() ->
                OpenAssertions.assertThat("hello").satisfies(s -> {
                    assertThat(s).hasSize(5);
                }));
        }
    }

    @Nested
    @DisplayName("StringAssertion Tests")
    class StringAssertionTests {

        @Test
        @DisplayName("isEmpty should pass for empty string")
        void isEmptyShouldPassForEmptyString() {
            assertThatNoException().isThrownBy(() ->
                OpenAssertions.assertThat("").isEmpty());
        }

        @Test
        @DisplayName("isNotEmpty should pass for non-empty string")
        void isNotEmptyShouldPassForNonEmptyString() {
            assertThatNoException().isThrownBy(() ->
                OpenAssertions.assertThat("hello").isNotEmpty());
        }

        @Test
        @DisplayName("isBlank should pass for blank string")
        void isBlankShouldPassForBlankString() {
            assertThatNoException().isThrownBy(() ->
                OpenAssertions.assertThat("   ").isBlank());
        }

        @Test
        @DisplayName("isNotBlank should pass for non-blank string")
        void isNotBlankShouldPassForNonBlankString() {
            assertThatNoException().isThrownBy(() ->
                OpenAssertions.assertThat("hello").isNotBlank());
        }

        @Test
        @DisplayName("contains should pass when substring exists")
        void containsShouldPassWhenSubstringExists() {
            assertThatNoException().isThrownBy(() ->
                OpenAssertions.assertThat("hello world").contains("world"));
        }

        @Test
        @DisplayName("startsWith should pass")
        void startsWithShouldPass() {
            assertThatNoException().isThrownBy(() ->
                OpenAssertions.assertThat("hello world").startsWith("hello"));
        }

        @Test
        @DisplayName("endsWith should pass")
        void endsWithShouldPass() {
            assertThatNoException().isThrownBy(() ->
                OpenAssertions.assertThat("hello world").endsWith("world"));
        }

        @Test
        @DisplayName("hasLength should pass for correct length")
        void hasLengthShouldPassForCorrectLength() {
            assertThatNoException().isThrownBy(() ->
                OpenAssertions.assertThat("hello").hasLength(5));
        }

        @Test
        @DisplayName("matchesRegex should pass")
        void matchesRegexShouldPass() {
            assertThatNoException().isThrownBy(() ->
                OpenAssertions.assertThat("abc123").matchesRegex("[a-z]+\\d+"));
        }
    }

    @Nested
    @DisplayName("CollectionAssertion Tests")
    class CollectionAssertionTests {

        @Test
        @DisplayName("isEmpty should pass for empty collection")
        void isEmptyShouldPassForEmptyCollection() {
            assertThatNoException().isThrownBy(() ->
                OpenAssertions.assertThat(List.of()).isEmpty());
        }

        @Test
        @DisplayName("isNotEmpty should pass for non-empty collection")
        void isNotEmptyShouldPassForNonEmptyCollection() {
            assertThatNoException().isThrownBy(() ->
                OpenAssertions.assertThat(List.of("a")).isNotEmpty());
        }

        @Test
        @DisplayName("hasSize should pass for correct size")
        void hasSizeShouldPassForCorrectSize() {
            assertThatNoException().isThrownBy(() ->
                OpenAssertions.assertThat(List.of("a", "b")).hasSize(2));
        }

        @Test
        @DisplayName("contains should pass when element exists")
        void containsShouldPassWhenElementExists() {
            assertThatNoException().isThrownBy(() ->
                OpenAssertions.assertThat(List.of("a", "b", "c")).contains("b"));
        }

        @Test
        @DisplayName("doesNotContain should pass when element not found")
        void doesNotContainShouldPassWhenElementNotFound() {
            assertThatNoException().isThrownBy(() ->
                OpenAssertions.assertThat(List.of("a", "b")).doesNotContain("x"));
        }

        @Test
        @DisplayName("containsAll should pass when all elements exist")
        void containsAllShouldPassWhenAllElementsExist() {
            assertThatNoException().isThrownBy(() ->
                OpenAssertions.assertThat(List.of("a", "b", "c")).containsAll("a", "b"));
        }
    }

    @Nested
    @DisplayName("MapAssertion Tests")
    class MapAssertionTests {

        @Test
        @DisplayName("isEmpty should pass for empty map")
        void isEmptyShouldPassForEmptyMap() {
            assertThatNoException().isThrownBy(() ->
                OpenAssertions.assertThat(Map.of()).isEmpty());
        }

        @Test
        @DisplayName("isNotEmpty should pass for non-empty map")
        void isNotEmptyShouldPassForNonEmptyMap() {
            assertThatNoException().isThrownBy(() ->
                OpenAssertions.assertThat(Map.of("key", "value")).isNotEmpty());
        }

        @Test
        @DisplayName("hasSize should pass for correct size")
        void hasSizeShouldPassForCorrectSize() {
            assertThatNoException().isThrownBy(() ->
                OpenAssertions.assertThat(Map.of("k1", "v1", "k2", "v2")).hasSize(2));
        }

        @Test
        @DisplayName("containsKey should pass when key exists")
        void containsKeyShouldPassWhenKeyExists() {
            assertThatNoException().isThrownBy(() ->
                OpenAssertions.assertThat(Map.of("key", "value")).containsKey("key"));
        }

        @Test
        @DisplayName("containsValue should pass when value exists")
        void containsValueShouldPassWhenValueExists() {
            assertThatNoException().isThrownBy(() ->
                OpenAssertions.assertThat(Map.of("key", "value")).containsValue("value"));
        }

        @Test
        @DisplayName("containsEntry should pass when entry exists")
        void containsEntryShouldPassWhenEntryExists() {
            assertThatNoException().isThrownBy(() ->
                OpenAssertions.assertThat(Map.of("key", "value")).containsEntry("key", "value"));
        }
    }

    @Nested
    @DisplayName("NumberAssertion Tests")
    class NumberAssertionTests {

        @Test
        @DisplayName("isZero should pass for zero")
        void isZeroShouldPassForZero() {
            assertThatNoException().isThrownBy(() ->
                OpenAssertions.assertThat(0).isZero());
        }

        @Test
        @DisplayName("isPositive should pass for positive")
        void isPositiveShouldPassForPositive() {
            assertThatNoException().isThrownBy(() ->
                OpenAssertions.assertThat(42).isPositive());
        }

        @Test
        @DisplayName("isNegative should pass for negative")
        void isNegativeShouldPassForNegative() {
            assertThatNoException().isThrownBy(() ->
                OpenAssertions.assertThat(-42).isNegative());
        }

        @Test
        @DisplayName("isGreaterThan should pass")
        void isGreaterThanShouldPass() {
            assertThatNoException().isThrownBy(() ->
                OpenAssertions.assertThat(42).isGreaterThan(10));
        }

        @Test
        @DisplayName("isLessThan should pass")
        void isLessThanShouldPass() {
            assertThatNoException().isThrownBy(() ->
                OpenAssertions.assertThat(10).isLessThan(42));
        }

        @Test
        @DisplayName("isBetween should pass")
        void isBetweenShouldPass() {
            assertThatNoException().isThrownBy(() ->
                OpenAssertions.assertThat(25).isBetween(10, 50));
        }
    }

    @Nested
    @DisplayName("BooleanAssertion Tests")
    class BooleanAssertionTests {

        @Test
        @DisplayName("isTrue should pass for true")
        void isTrueShouldPassForTrue() {
            assertThatNoException().isThrownBy(() ->
                OpenAssertions.assertThat(true).isTrue());
        }

        @Test
        @DisplayName("isFalse should pass for false")
        void isFalseShouldPassForFalse() {
            assertThatNoException().isThrownBy(() ->
                OpenAssertions.assertThat(false).isFalse());
        }
    }

    @Nested
    @DisplayName("ThrowableAssertion Tests")
    class ThrowableAssertionTests {

        @Test
        @DisplayName("assertThatThrownBy should capture exception")
        void assertThatThrownByShouldCaptureException() {
            var assertion = OpenAssertions.assertThatThrownBy(() -> {
                throw new IllegalArgumentException("test");
            });
            assertThat(assertion).isNotNull();
        }

        @Test
        @DisplayName("isInstanceOf should pass for matching type")
        void isInstanceOfShouldPassForMatchingType() {
            assertThatNoException().isThrownBy(() ->
                OpenAssertions.assertThatThrownBy(() -> { throw new IllegalArgumentException("test"); })
                    .isInstanceOf(IllegalArgumentException.class));
        }

        @Test
        @DisplayName("hasMessage should pass for matching message")
        void hasMessageShouldPassForMatchingMessage() {
            assertThatNoException().isThrownBy(() ->
                OpenAssertions.assertThatThrownBy(() -> { throw new RuntimeException("expected"); })
                    .hasMessage("expected"));
        }

        @Test
        @DisplayName("hasMessageContaining should pass")
        void hasMessageContainingShouldPass() {
            assertThatNoException().isThrownBy(() ->
                OpenAssertions.assertThatThrownBy(() -> { throw new RuntimeException("error message"); })
                    .hasMessageContaining("error"));
        }

        @Test
        @DisplayName("hasCauseInstanceOf should pass for matching cause type")
        void hasCauseInstanceOfShouldPassForMatchingCauseType() {
            assertThatNoException().isThrownBy(() ->
                OpenAssertions.assertThatThrownBy(() -> {
                    throw new RuntimeException("outer", new IllegalArgumentException("inner"));
                }).hasCauseInstanceOf(IllegalArgumentException.class));
        }
    }

    @Nested
    @DisplayName("assertThatCode Tests")
    class AssertThatCodeTests {

        @Test
        @DisplayName("Should pass when no exception thrown")
        void shouldPassWhenNoExceptionThrown() {
            assertThatNoException().isThrownBy(() ->
                OpenAssertions.assertThatCode(() -> {}));
        }

        @Test
        @DisplayName("Should fail when exception thrown")
        void shouldFailWhenExceptionThrown() {
            assertThatThrownBy(() ->
                OpenAssertions.assertThatCode(() -> { throw new RuntimeException("error"); })
            ).isInstanceOf(AssertionError.class);
        }
    }
}
