package cloud.opencode.base.test;

import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenAssertTest Tests
 * OpenAssertTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("OpenAssert Tests")
class OpenAssertTest {

    @Nested
    @DisplayName("assertTrue Tests")
    class AssertTrueTests {

        @Test
        @DisplayName("Should pass when condition is true")
        void shouldPassWhenConditionIsTrue() {
            assertThatNoException().isThrownBy(() -> OpenAssert.assertTrue(true));
        }

        @Test
        @DisplayName("Should fail when condition is false")
        void shouldFailWhenConditionIsFalse() {
            assertThatThrownBy(() -> OpenAssert.assertTrue(false))
                .isInstanceOf(AssertionError.class);
        }

        @Test
        @DisplayName("Should include message when fails")
        void shouldIncludeMessageWhenFails() {
            assertThatThrownBy(() -> OpenAssert.assertTrue(false, "custom message"))
                .isInstanceOf(AssertionError.class)
                .hasMessage("custom message");
        }

        @Test
        @DisplayName("Should support lazy message")
        void shouldSupportLazyMessage() {
            assertThatThrownBy(() -> OpenAssert.assertTrue(false, () -> "lazy message"))
                .isInstanceOf(AssertionError.class)
                .hasMessage("lazy message");
        }
    }

    @Nested
    @DisplayName("assertFalse Tests")
    class AssertFalseTests {

        @Test
        @DisplayName("Should pass when condition is false")
        void shouldPassWhenConditionIsFalse() {
            assertThatNoException().isThrownBy(() -> OpenAssert.assertFalse(false));
        }

        @Test
        @DisplayName("Should fail when condition is true")
        void shouldFailWhenConditionIsTrue() {
            assertThatThrownBy(() -> OpenAssert.assertFalse(true))
                .isInstanceOf(AssertionError.class);
        }
    }

    @Nested
    @DisplayName("assertNull Tests")
    class AssertNullTests {

        @Test
        @DisplayName("Should pass when object is null")
        void shouldPassWhenObjectIsNull() {
            assertThatNoException().isThrownBy(() -> OpenAssert.assertNull(null));
        }

        @Test
        @DisplayName("Should fail when object is not null")
        void shouldFailWhenObjectIsNotNull() {
            assertThatThrownBy(() -> OpenAssert.assertNull("not null"))
                .isInstanceOf(AssertionError.class);
        }
    }

    @Nested
    @DisplayName("assertNotNull Tests")
    class AssertNotNullTests {

        @Test
        @DisplayName("Should pass when object is not null")
        void shouldPassWhenObjectIsNotNull() {
            assertThatNoException().isThrownBy(() -> OpenAssert.assertNotNull("not null"));
        }

        @Test
        @DisplayName("Should fail when object is null")
        void shouldFailWhenObjectIsNull() {
            assertThatThrownBy(() -> OpenAssert.assertNotNull(null))
                .isInstanceOf(AssertionError.class);
        }
    }

    @Nested
    @DisplayName("assertEquals Tests")
    class AssertEqualsTests {

        @Test
        @DisplayName("Should pass when objects are equal")
        void shouldPassWhenObjectsAreEqual() {
            assertThatNoException().isThrownBy(() -> OpenAssert.assertEquals("hello", "hello"));
        }

        @Test
        @DisplayName("Should pass when both are null")
        void shouldPassWhenBothAreNull() {
            assertThatNoException().isThrownBy(() -> OpenAssert.assertEquals(null, null));
        }

        @Test
        @DisplayName("Should fail when objects are not equal")
        void shouldFailWhenObjectsAreNotEqual() {
            assertThatThrownBy(() -> OpenAssert.assertEquals("hello", "world"))
                .isInstanceOf(AssertionError.class);
        }

        @Test
        @DisplayName("Should pass for doubles within delta")
        void shouldPassForDoublesWithinDelta() {
            assertThatNoException().isThrownBy(() -> OpenAssert.assertEquals(1.0, 1.0001, 0.001));
        }

        @Test
        @DisplayName("Should fail for doubles outside delta")
        void shouldFailForDoublesOutsideDelta() {
            assertThatThrownBy(() -> OpenAssert.assertEquals(1.0, 1.01, 0.001))
                .isInstanceOf(AssertionError.class);
        }
    }

    @Nested
    @DisplayName("assertNotEquals Tests")
    class AssertNotEqualsTests {

        @Test
        @DisplayName("Should pass when objects are not equal")
        void shouldPassWhenObjectsAreNotEqual() {
            assertThatNoException().isThrownBy(() -> OpenAssert.assertNotEquals("hello", "world"));
        }

        @Test
        @DisplayName("Should fail when objects are equal")
        void shouldFailWhenObjectsAreEqual() {
            assertThatThrownBy(() -> OpenAssert.assertNotEquals("hello", "hello"))
                .isInstanceOf(AssertionError.class);
        }
    }

    @Nested
    @DisplayName("assertSame Tests")
    class AssertSameTests {

        @Test
        @DisplayName("Should pass when same instance")
        void shouldPassWhenSameInstance() {
            String obj = "test";
            assertThatNoException().isThrownBy(() -> OpenAssert.assertSame(obj, obj));
        }

        @Test
        @DisplayName("Should fail when different instances")
        void shouldFailWhenDifferentInstances() {
            assertThatThrownBy(() -> OpenAssert.assertSame(new String("test"), new String("test")))
                .isInstanceOf(AssertionError.class);
        }
    }

    @Nested
    @DisplayName("assertNotSame Tests")
    class AssertNotSameTests {

        @Test
        @DisplayName("Should pass when different instances")
        void shouldPassWhenDifferentInstances() {
            assertThatNoException().isThrownBy(() ->
                OpenAssert.assertNotSame(new String("test"), new String("test")));
        }

        @Test
        @DisplayName("Should fail when same instance")
        void shouldFailWhenSameInstance() {
            String obj = "test";
            assertThatThrownBy(() -> OpenAssert.assertNotSame(obj, obj))
                .isInstanceOf(AssertionError.class);
        }
    }

    @Nested
    @DisplayName("assertGreaterThan Tests")
    class AssertGreaterThanTests {

        @Test
        @DisplayName("Should pass when actual greater than expected")
        void shouldPassWhenActualGreaterThanExpected() {
            assertThatNoException().isThrownBy(() -> OpenAssert.assertGreaterThan(10, 5));
        }

        @Test
        @DisplayName("Should fail when actual not greater")
        void shouldFailWhenActualNotGreater() {
            assertThatThrownBy(() -> OpenAssert.assertGreaterThan(5, 10))
                .isInstanceOf(AssertionError.class);
        }
    }

    @Nested
    @DisplayName("assertLessThan Tests")
    class AssertLessThanTests {

        @Test
        @DisplayName("Should pass when actual less than expected")
        void shouldPassWhenActualLessThanExpected() {
            assertThatNoException().isThrownBy(() -> OpenAssert.assertLessThan(5, 10));
        }

        @Test
        @DisplayName("Should fail when actual not less")
        void shouldFailWhenActualNotLess() {
            assertThatThrownBy(() -> OpenAssert.assertLessThan(10, 5))
                .isInstanceOf(AssertionError.class);
        }
    }

    @Nested
    @DisplayName("assertBetween Tests")
    class AssertBetweenTests {

        @Test
        @DisplayName("Should pass when actual between min and max")
        void shouldPassWhenActualBetweenMinAndMax() {
            assertThatNoException().isThrownBy(() -> OpenAssert.assertBetween(5, 1, 10));
        }

        @Test
        @DisplayName("Should fail when actual below min")
        void shouldFailWhenActualBelowMin() {
            assertThatThrownBy(() -> OpenAssert.assertBetween(0, 1, 10))
                .isInstanceOf(AssertionError.class);
        }

        @Test
        @DisplayName("Should fail when actual above max")
        void shouldFailWhenActualAboveMax() {
            assertThatThrownBy(() -> OpenAssert.assertBetween(15, 1, 10))
                .isInstanceOf(AssertionError.class);
        }
    }

    @Nested
    @DisplayName("Collection Assertion Tests")
    class CollectionAssertionTests {

        @Test
        @DisplayName("assertEmpty should pass for empty collection")
        void assertEmptyShouldPassForEmptyCollection() {
            assertThatNoException().isThrownBy(() -> OpenAssert.assertEmpty(List.of()));
        }

        @Test
        @DisplayName("assertEmpty should fail for non-empty collection")
        void assertEmptyShouldFailForNonEmptyCollection() {
            assertThatThrownBy(() -> OpenAssert.assertEmpty(List.of("item")))
                .isInstanceOf(AssertionError.class);
        }

        @Test
        @DisplayName("assertNotEmpty should pass for non-empty collection")
        void assertNotEmptyShouldPassForNonEmptyCollection() {
            assertThatNoException().isThrownBy(() -> OpenAssert.assertNotEmpty(List.of("item")));
        }

        @Test
        @DisplayName("assertSize should pass for correct size")
        void assertSizeShouldPassForCorrectSize() {
            assertThatNoException().isThrownBy(() -> OpenAssert.assertSize(2, List.of("a", "b")));
        }

        @Test
        @DisplayName("assertContains should pass when element present")
        void assertContainsShouldPassWhenElementPresent() {
            assertThatNoException().isThrownBy(() -> OpenAssert.assertContains("a", List.of("a", "b")));
        }
    }

    @Nested
    @DisplayName("Map Assertion Tests")
    class MapAssertionTests {

        @Test
        @DisplayName("assertEmpty should pass for empty map")
        void assertEmptyShouldPassForEmptyMap() {
            assertThatNoException().isThrownBy(() -> OpenAssert.assertEmpty(Map.of()));
        }

        @Test
        @DisplayName("assertContainsKey should pass when key present")
        void assertContainsKeyShouldPassWhenKeyPresent() {
            assertThatNoException().isThrownBy(() -> OpenAssert.assertContainsKey("key", Map.of("key", "value")));
        }
    }

    @Nested
    @DisplayName("String Assertion Tests")
    class StringAssertionTests {

        @Test
        @DisplayName("assertBlank should pass for blank string")
        void assertBlankShouldPassForBlankString() {
            assertThatNoException().isThrownBy(() -> OpenAssert.assertBlank("   "));
            assertThatNoException().isThrownBy(() -> OpenAssert.assertBlank(null));
        }

        @Test
        @DisplayName("assertNotBlank should pass for non-blank string")
        void assertNotBlankShouldPassForNonBlankString() {
            assertThatNoException().isThrownBy(() -> OpenAssert.assertNotBlank("hello"));
        }

        @Test
        @DisplayName("assertContains should pass when substring present")
        void assertContainsShouldPassWhenSubstringPresent() {
            assertThatNoException().isThrownBy(() -> OpenAssert.assertContains("ell", "hello"));
        }

        @Test
        @DisplayName("assertStartsWith should pass when prefix matches")
        void assertStartsWithShouldPassWhenPrefixMatches() {
            assertThatNoException().isThrownBy(() -> OpenAssert.assertStartsWith("hel", "hello"));
        }

        @Test
        @DisplayName("assertEndsWith should pass when suffix matches")
        void assertEndsWithShouldPassWhenSuffixMatches() {
            assertThatNoException().isThrownBy(() -> OpenAssert.assertEndsWith("llo", "hello"));
        }

        @Test
        @DisplayName("assertMatches should pass when regex matches")
        void assertMatchesShouldPassWhenRegexMatches() {
            assertThatNoException().isThrownBy(() -> OpenAssert.assertMatches("\\d+", "12345"));
        }
    }

    @Nested
    @DisplayName("Exception Assertion Tests")
    class ExceptionAssertionTests {

        @Test
        @DisplayName("assertThrows should return thrown exception")
        void assertThrowsShouldReturnThrownException() {
            RuntimeException thrown = OpenAssert.assertThrows(RuntimeException.class, () -> {
                throw new RuntimeException("test");
            });
            assertThat(thrown.getMessage()).isEqualTo("test");
        }

        @Test
        @DisplayName("assertThrows should fail when no exception")
        void assertThrowsShouldFailWhenNoException() {
            assertThatThrownBy(() -> OpenAssert.assertThrows(RuntimeException.class, () -> {}))
                .isInstanceOf(AssertionError.class);
        }

        @Test
        @DisplayName("assertDoesNotThrow should pass when no exception")
        void assertDoesNotThrowShouldPassWhenNoException() {
            assertThatNoException().isThrownBy(() -> OpenAssert.assertDoesNotThrow(() -> {}));
        }

        @Test
        @DisplayName("assertDoesNotThrow should fail when exception thrown")
        void assertDoesNotThrowShouldFailWhenExceptionThrown() {
            assertThatThrownBy(() -> OpenAssert.assertDoesNotThrow(() -> {
                throw new RuntimeException("test");
            })).isInstanceOf(AssertionError.class);
        }
    }

    @Nested
    @DisplayName("Timeout Assertion Tests")
    class TimeoutAssertionTests {

        @Test
        @DisplayName("assertTimeout should pass when completed in time")
        void assertTimeoutShouldPassWhenCompletedInTime() {
            assertThatNoException().isThrownBy(() ->
                OpenAssert.assertTimeout(Duration.ofSeconds(1), () -> {}));
        }
    }

    @Nested
    @DisplayName("fail Tests")
    class FailTests {

        @Test
        @DisplayName("fail() should throw AssertionError")
        void failShouldThrowAssertionError() {
            assertThatThrownBy(() -> OpenAssert.fail())
                .isInstanceOf(AssertionError.class);
        }

        @Test
        @DisplayName("fail(message) should throw AssertionError with message")
        void failWithMessageShouldThrowAssertionErrorWithMessage() {
            assertThatThrownBy(() -> OpenAssert.fail("custom"))
                .isInstanceOf(AssertionError.class)
                .hasMessage("custom");
        }
    }
}
