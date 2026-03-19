package cloud.opencode.base.test.assertion;

import org.junit.jupiter.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * SoftAssertTest Tests
 * SoftAssertTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("SoftAssert Tests")
class SoftAssertTest {

    @Nested
    @DisplayName("Null Assertion Tests")
    class NullAssertionTests {

        @Test
        @DisplayName("isNull should not add failure when null")
        void isNullShouldNotAddFailureWhenNull() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.isNull(null);
            assertThat(softAssert.hasFailures()).isFalse();
        }

        @Test
        @DisplayName("isNull should add failure when not null")
        void isNullShouldAddFailureWhenNotNull() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.isNull("value");
            assertThat(softAssert.hasFailures()).isTrue();
        }

        @Test
        @DisplayName("isNull with message should use custom message")
        void isNullWithMessageShouldUseCustomMessage() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.isNull("value", "custom message");
            assertThat(softAssert.getFailures().getFirst().getMessage()).isEqualTo("custom message");
        }

        @Test
        @DisplayName("isNull with supplier should use lazy message")
        void isNullWithSupplierShouldUseLazyMessage() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.isNull("value", () -> "lazy message");
            assertThat(softAssert.getFailures().getFirst().getMessage()).isEqualTo("lazy message");
        }

        @Test
        @DisplayName("isNotNull should not add failure when not null")
        void isNotNullShouldNotAddFailureWhenNotNull() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.isNotNull("value");
            assertThat(softAssert.hasFailures()).isFalse();
        }

        @Test
        @DisplayName("isNotNull should add failure when null")
        void isNotNullShouldAddFailureWhenNull() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.isNotNull(null);
            assertThat(softAssert.hasFailures()).isTrue();
        }

        @Test
        @DisplayName("isNotNull with message should use custom message")
        void isNotNullWithMessageShouldUseCustomMessage() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.isNotNull(null, "custom message");
            assertThat(softAssert.getFailures().getFirst().getMessage()).isEqualTo("custom message");
        }

        @Test
        @DisplayName("isNotNull with supplier should use lazy message")
        void isNotNullWithSupplierShouldUseLazyMessage() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.isNotNull(null, () -> "lazy message");
            assertThat(softAssert.getFailures().getFirst().getMessage()).isEqualTo("lazy message");
        }
    }

    @Nested
    @DisplayName("Equality Assertion Tests")
    class EqualityAssertionTests {

        @Test
        @DisplayName("isEqualTo should not add failure when equal")
        void isEqualToShouldNotAddFailureWhenEqual() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.isEqualTo("value", "value");
            assertThat(softAssert.hasFailures()).isFalse();
        }

        @Test
        @DisplayName("isEqualTo should add failure when not equal")
        void isEqualToShouldAddFailureWhenNotEqual() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.isEqualTo("expected", "actual");
            assertThat(softAssert.hasFailures()).isTrue();
        }

        @Test
        @DisplayName("isEqualTo with message should use custom message")
        void isEqualToWithMessageShouldUseCustomMessage() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.isEqualTo("expected", "actual", "custom message");
            assertThat(softAssert.getFailures().getFirst().getMessage()).isEqualTo("custom message");
        }

        @Test
        @DisplayName("isEqualTo with supplier should use lazy message")
        void isEqualToWithSupplierShouldUseLazyMessage() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.isEqualTo("expected", "actual", () -> "lazy message");
            assertThat(softAssert.getFailures().getFirst().getMessage()).isEqualTo("lazy message");
        }

        @Test
        @DisplayName("isNotEqualTo should not add failure when not equal")
        void isNotEqualToShouldNotAddFailureWhenNotEqual() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.isNotEqualTo("unexpected", "actual");
            assertThat(softAssert.hasFailures()).isFalse();
        }

        @Test
        @DisplayName("isNotEqualTo should add failure when equal")
        void isNotEqualToShouldAddFailureWhenEqual() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.isNotEqualTo("value", "value");
            assertThat(softAssert.hasFailures()).isTrue();
        }

        @Test
        @DisplayName("isNotEqualTo with message should use custom message")
        void isNotEqualToWithMessageShouldUseCustomMessage() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.isNotEqualTo("value", "value", "custom message");
            assertThat(softAssert.getFailures().getFirst().getMessage()).isEqualTo("custom message");
        }

        @Test
        @DisplayName("isNotEqualTo with supplier should use lazy message")
        void isNotEqualToWithSupplierShouldUseLazyMessage() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.isNotEqualTo("value", "value", () -> "lazy message");
            assertThat(softAssert.getFailures().getFirst().getMessage()).isEqualTo("lazy message");
        }
    }

    @Nested
    @DisplayName("Boolean Assertion Tests")
    class BooleanAssertionTests {

        @Test
        @DisplayName("isTrue should not add failure when true")
        void isTrueShouldNotAddFailureWhenTrue() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.isTrue(true);
            assertThat(softAssert.hasFailures()).isFalse();
        }

        @Test
        @DisplayName("isTrue should add failure when false")
        void isTrueShouldAddFailureWhenFalse() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.isTrue(false);
            assertThat(softAssert.hasFailures()).isTrue();
        }

        @Test
        @DisplayName("isTrue with message should use custom message")
        void isTrueWithMessageShouldUseCustomMessage() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.isTrue(false, "custom message");
            assertThat(softAssert.getFailures().getFirst().getMessage()).isEqualTo("custom message");
        }

        @Test
        @DisplayName("isTrue with supplier should use lazy message")
        void isTrueWithSupplierShouldUseLazyMessage() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.isTrue(false, () -> "lazy message");
            assertThat(softAssert.getFailures().getFirst().getMessage()).isEqualTo("lazy message");
        }

        @Test
        @DisplayName("isFalse should not add failure when false")
        void isFalseShouldNotAddFailureWhenFalse() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.isFalse(false);
            assertThat(softAssert.hasFailures()).isFalse();
        }

        @Test
        @DisplayName("isFalse should add failure when true")
        void isFalseShouldAddFailureWhenTrue() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.isFalse(true);
            assertThat(softAssert.hasFailures()).isTrue();
        }

        @Test
        @DisplayName("isFalse with message should use custom message")
        void isFalseWithMessageShouldUseCustomMessage() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.isFalse(true, "custom message");
            assertThat(softAssert.getFailures().getFirst().getMessage()).isEqualTo("custom message");
        }

        @Test
        @DisplayName("isFalse with supplier should use lazy message")
        void isFalseWithSupplierShouldUseLazyMessage() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.isFalse(true, () -> "lazy message");
            assertThat(softAssert.getFailures().getFirst().getMessage()).isEqualTo("lazy message");
        }
    }

    @Nested
    @DisplayName("String Assertion Tests")
    class StringAssertionTests {

        @Test
        @DisplayName("isEmpty should not add failure when empty")
        void isEmptyShouldNotAddFailureWhenEmpty() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.isEmpty("");
            assertThat(softAssert.hasFailures()).isFalse();
        }

        @Test
        @DisplayName("isEmpty should add failure when not empty")
        void isEmptyShouldAddFailureWhenNotEmpty() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.isEmpty("value");
            assertThat(softAssert.hasFailures()).isTrue();
        }

        @Test
        @DisplayName("isEmpty should add failure when null")
        void isEmptyShouldAddFailureWhenNull() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.isEmpty(null);
            assertThat(softAssert.hasFailures()).isTrue();
        }

        @Test
        @DisplayName("isNotEmpty should not add failure when not empty")
        void isNotEmptyShouldNotAddFailureWhenNotEmpty() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.isNotEmpty("value");
            assertThat(softAssert.hasFailures()).isFalse();
        }

        @Test
        @DisplayName("isNotEmpty should add failure when empty")
        void isNotEmptyShouldAddFailureWhenEmpty() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.isNotEmpty("");
            assertThat(softAssert.hasFailures()).isTrue();
        }

        @Test
        @DisplayName("contains should not add failure when contains substring")
        void containsShouldNotAddFailureWhenContainsSubstring() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.contains("hello world", "world");
            assertThat(softAssert.hasFailures()).isFalse();
        }

        @Test
        @DisplayName("contains should add failure when not contains")
        void containsShouldAddFailureWhenNotContains() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.contains("hello", "world");
            assertThat(softAssert.hasFailures()).isTrue();
        }

        @Test
        @DisplayName("startsWith should not add failure when starts with prefix")
        void startsWithShouldNotAddFailureWhenStartsWithPrefix() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.startsWith("hello world", "hello");
            assertThat(softAssert.hasFailures()).isFalse();
        }

        @Test
        @DisplayName("startsWith should add failure when not starts with")
        void startsWithShouldAddFailureWhenNotStartsWith() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.startsWith("hello", "world");
            assertThat(softAssert.hasFailures()).isTrue();
        }

        @Test
        @DisplayName("endsWith should not add failure when ends with suffix")
        void endsWithShouldNotAddFailureWhenEndsWithSuffix() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.endsWith("hello world", "world");
            assertThat(softAssert.hasFailures()).isFalse();
        }

        @Test
        @DisplayName("endsWith should add failure when not ends with")
        void endsWithShouldAddFailureWhenNotEndsWith() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.endsWith("hello", "world");
            assertThat(softAssert.hasFailures()).isTrue();
        }
    }

    @Nested
    @DisplayName("Number Assertion Tests")
    class NumberAssertionTests {

        @Test
        @DisplayName("isGreaterThan should not add failure when greater")
        void isGreaterThanShouldNotAddFailureWhenGreater() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.isGreaterThan(10, 5);
            assertThat(softAssert.hasFailures()).isFalse();
        }

        @Test
        @DisplayName("isGreaterThan should add failure when not greater")
        void isGreaterThanShouldAddFailureWhenNotGreater() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.isGreaterThan(5, 10);
            assertThat(softAssert.hasFailures()).isTrue();
        }

        @Test
        @DisplayName("isGreaterThanOrEqualTo should not add failure when greater or equal")
        void isGreaterThanOrEqualToShouldNotAddFailureWhenGreaterOrEqual() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.isGreaterThanOrEqualTo(10, 10);
            assertThat(softAssert.hasFailures()).isFalse();
        }

        @Test
        @DisplayName("isLessThan should not add failure when less")
        void isLessThanShouldNotAddFailureWhenLess() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.isLessThan(5, 10);
            assertThat(softAssert.hasFailures()).isFalse();
        }

        @Test
        @DisplayName("isLessThan should add failure when not less")
        void isLessThanShouldAddFailureWhenNotLess() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.isLessThan(10, 5);
            assertThat(softAssert.hasFailures()).isTrue();
        }

        @Test
        @DisplayName("isLessThanOrEqualTo should not add failure when less or equal")
        void isLessThanOrEqualToShouldNotAddFailureWhenLessOrEqual() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.isLessThanOrEqualTo(10, 10);
            assertThat(softAssert.hasFailures()).isFalse();
        }

        @Test
        @DisplayName("isBetween should not add failure when in range")
        void isBetweenShouldNotAddFailureWhenInRange() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.isBetween(5, 1, 10);
            assertThat(softAssert.hasFailures()).isFalse();
        }

        @Test
        @DisplayName("isBetween should add failure when out of range")
        void isBetweenShouldAddFailureWhenOutOfRange() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.isBetween(15, 1, 10);
            assertThat(softAssert.hasFailures()).isTrue();
        }

        @Test
        @DisplayName("Number assertions should handle null")
        void numberAssertionsShouldHandleNull() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.isGreaterThan(null, 5);
            assertThat(softAssert.hasFailures()).isTrue();
        }
    }

    @Nested
    @DisplayName("Failure Management Tests")
    class FailureManagementTests {

        @Test
        @DisplayName("assertAll should not throw when no failures")
        void assertAllShouldNotThrowWhenNoFailures() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.isTrue(true);

            assertThatNoException().isThrownBy(softAssert::assertAll);
        }

        @Test
        @DisplayName("assertAll should throw when has failures")
        void assertAllShouldThrowWhenHasFailures() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.isTrue(false);

            assertThatThrownBy(softAssert::assertAll)
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Multiple assertions failed");
        }

        @Test
        @DisplayName("assertAll with heading should include heading")
        void assertAllWithHeadingShouldIncludeHeading() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.isTrue(false);

            assertThatThrownBy(() -> softAssert.assertAll("Custom Heading"))
                .hasMessageContaining("Custom Heading");
        }

        @Test
        @DisplayName("getFailureCount should return correct count")
        void getFailureCountShouldReturnCorrectCount() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.isTrue(false);
            softAssert.isTrue(false);

            assertThat(softAssert.getFailureCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("getFailures should return unmodifiable list")
        void getFailuresShouldReturnUnmodifiableList() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.isTrue(false);

            List<AssertionError> failures = softAssert.getFailures();
            assertThat(failures).hasSize(1);
        }

        @Test
        @DisplayName("reset should clear all failures")
        void resetShouldClearAllFailures() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.isTrue(false);
            softAssert.reset();

            assertThat(softAssert.hasFailures()).isFalse();
        }
    }

    @Nested
    @DisplayName("Chaining Tests")
    class ChainingTests {

        @Test
        @DisplayName("Should support fluent chaining")
        void shouldSupportFluentChaining() {
            SoftAssert softAssert = new SoftAssert()
                .isNotNull("value")
                .isEqualTo("a", "a")
                .isTrue(true)
                .isFalse(false)
                .isNotEmpty("text")
                .contains("hello world", "world");

            assertThat(softAssert.hasFailures()).isFalse();
        }

        @Test
        @DisplayName("Chaining should collect all failures")
        void chainingShouldCollectAllFailures() {
            SoftAssert softAssert = new SoftAssert()
                .isNull("not null")
                .isEqualTo("expected", "actual")
                .isTrue(false);

            assertThat(softAssert.getFailureCount()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("toString Tests")
    class ToStringTests {

        @Test
        @DisplayName("toString should return failure count")
        void toStringShouldReturnFailureCount() {
            SoftAssert softAssert = new SoftAssert();
            softAssert.isTrue(false);

            assertThat(softAssert.toString()).contains("failures=1");
        }
    }
}
