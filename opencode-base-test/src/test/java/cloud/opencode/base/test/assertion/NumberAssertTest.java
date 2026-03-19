package cloud.opencode.base.test.assertion;

import cloud.opencode.base.test.exception.AssertionException;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * NumberAssertTest Tests
 * NumberAssertTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("NumberAssert Tests")
class NumberAssertTest {

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("assertThat with int should create assertion")
        void assertThatWithIntShouldCreateAssertion() {
            assertThat(NumberAssert.assertThat(42)).isNotNull();
        }

        @Test
        @DisplayName("assertThat with long should create assertion")
        void assertThatWithLongShouldCreateAssertion() {
            assertThat(NumberAssert.assertThat(42L)).isNotNull();
        }

        @Test
        @DisplayName("assertThat with double should create assertion")
        void assertThatWithDoubleShouldCreateAssertion() {
            assertThat(NumberAssert.assertThat(42.0)).isNotNull();
        }

        @Test
        @DisplayName("assertThat with Integer should create assertion")
        void assertThatWithIntegerShouldCreateAssertion() {
            assertThat(NumberAssert.assertThat(Integer.valueOf(42))).isNotNull();
        }
    }

    @Nested
    @DisplayName("Null/NotNull Tests")
    class NullNotNullTests {

        @Test
        @DisplayName("isNull should pass for null")
        void isNullShouldPassForNull() {
            assertThatNoException().isThrownBy(() ->
                NumberAssert.assertThat((Integer) null).isNull());
        }

        @Test
        @DisplayName("isNull should fail for non-null")
        void isNullShouldFailForNonNull() {
            assertThatThrownBy(() -> NumberAssert.assertThat(42).isNull())
                .isInstanceOf(AssertionException.class);
        }

        @Test
        @DisplayName("isNotNull should pass for non-null")
        void isNotNullShouldPassForNonNull() {
            assertThatNoException().isThrownBy(() ->
                NumberAssert.assertThat(42).isNotNull());
        }

        @Test
        @DisplayName("isNotNull should fail for null")
        void isNotNullShouldFailForNull() {
            assertThatThrownBy(() -> NumberAssert.assertThat((Integer) null).isNotNull())
                .isInstanceOf(AssertionException.class);
        }
    }

    @Nested
    @DisplayName("Equality Tests")
    class EqualityTests {

        @Test
        @DisplayName("isEqualTo should pass for equal values")
        void isEqualToShouldPassForEqualValues() {
            assertThatNoException().isThrownBy(() ->
                NumberAssert.assertThat(42).isEqualTo(42));
        }

        @Test
        @DisplayName("isEqualTo should fail for different values")
        void isEqualToShouldFailForDifferentValues() {
            assertThatThrownBy(() -> NumberAssert.assertThat(42).isEqualTo(100))
                .isInstanceOf(AssertionException.class);
        }
    }

    @Nested
    @DisplayName("Zero Tests")
    class ZeroTests {

        @Test
        @DisplayName("isZero should pass for zero")
        void isZeroShouldPassForZero() {
            assertThatNoException().isThrownBy(() ->
                NumberAssert.assertThat(0).isZero());
        }

        @Test
        @DisplayName("isZero should fail for non-zero")
        void isZeroShouldFailForNonZero() {
            assertThatThrownBy(() -> NumberAssert.assertThat(42).isZero())
                .isInstanceOf(AssertionException.class);
        }

        @Test
        @DisplayName("isNotZero should pass for non-zero")
        void isNotZeroShouldPassForNonZero() {
            assertThatNoException().isThrownBy(() ->
                NumberAssert.assertThat(42).isNotZero());
        }

        @Test
        @DisplayName("isNotZero should fail for zero")
        void isNotZeroShouldFailForZero() {
            assertThatThrownBy(() -> NumberAssert.assertThat(0).isNotZero())
                .isInstanceOf(AssertionException.class);
        }
    }

    @Nested
    @DisplayName("Sign Tests")
    class SignTests {

        @Test
        @DisplayName("isPositive should pass for positive")
        void isPositiveShouldPassForPositive() {
            assertThatNoException().isThrownBy(() ->
                NumberAssert.assertThat(42).isPositive());
        }

        @Test
        @DisplayName("isPositive should fail for negative")
        void isPositiveShouldFailForNegative() {
            assertThatThrownBy(() -> NumberAssert.assertThat(-42).isPositive())
                .isInstanceOf(AssertionException.class);
        }

        @Test
        @DisplayName("isNegative should pass for negative")
        void isNegativeShouldPassForNegative() {
            assertThatNoException().isThrownBy(() ->
                NumberAssert.assertThat(-42).isNegative());
        }

        @Test
        @DisplayName("isNegative should fail for positive")
        void isNegativeShouldFailForPositive() {
            assertThatThrownBy(() -> NumberAssert.assertThat(42).isNegative())
                .isInstanceOf(AssertionException.class);
        }

        @Test
        @DisplayName("isNotNegative should pass for positive")
        void isNotNegativeShouldPassForPositive() {
            assertThatNoException().isThrownBy(() ->
                NumberAssert.assertThat(42).isNotNegative());
        }

        @Test
        @DisplayName("isNotNegative should pass for zero")
        void isNotNegativeShouldPassForZero() {
            assertThatNoException().isThrownBy(() ->
                NumberAssert.assertThat(0).isNotNegative());
        }

        @Test
        @DisplayName("isNotPositive should pass for negative")
        void isNotPositiveShouldPassForNegative() {
            assertThatNoException().isThrownBy(() ->
                NumberAssert.assertThat(-42).isNotPositive());
        }

        @Test
        @DisplayName("isNotPositive should pass for zero")
        void isNotPositiveShouldPassForZero() {
            assertThatNoException().isThrownBy(() ->
                NumberAssert.assertThat(0).isNotPositive());
        }
    }

    @Nested
    @DisplayName("Comparison Tests")
    class ComparisonTests {

        @Test
        @DisplayName("isGreaterThan should pass")
        void isGreaterThanShouldPass() {
            assertThatNoException().isThrownBy(() ->
                NumberAssert.assertThat(42).isGreaterThan(10));
        }

        @Test
        @DisplayName("isGreaterThan should fail")
        void isGreaterThanShouldFail() {
            assertThatThrownBy(() -> NumberAssert.assertThat(5).isGreaterThan(10))
                .isInstanceOf(AssertionException.class);
        }

        @Test
        @DisplayName("isGreaterThanOrEqualTo should pass")
        void isGreaterThanOrEqualToShouldPass() {
            assertThatNoException().isThrownBy(() ->
                NumberAssert.assertThat(42).isGreaterThanOrEqualTo(42));
        }

        @Test
        @DisplayName("isLessThan should pass")
        void isLessThanShouldPass() {
            assertThatNoException().isThrownBy(() ->
                NumberAssert.assertThat(5).isLessThan(10));
        }

        @Test
        @DisplayName("isLessThan should fail")
        void isLessThanShouldFail() {
            assertThatThrownBy(() -> NumberAssert.assertThat(42).isLessThan(10))
                .isInstanceOf(AssertionException.class);
        }

        @Test
        @DisplayName("isLessThanOrEqualTo should pass")
        void isLessThanOrEqualToShouldPass() {
            assertThatNoException().isThrownBy(() ->
                NumberAssert.assertThat(42).isLessThanOrEqualTo(42));
        }
    }

    @Nested
    @DisplayName("Range Tests")
    class RangeTests {

        @Test
        @DisplayName("isBetween should pass for value in range")
        void isBetweenShouldPassForValueInRange() {
            assertThatNoException().isThrownBy(() ->
                NumberAssert.assertThat(50).isBetween(10, 100));
        }

        @Test
        @DisplayName("isBetween should pass for boundary values")
        void isBetweenShouldPassForBoundaryValues() {
            assertThatNoException().isThrownBy(() ->
                NumberAssert.assertThat(10).isBetween(10, 100));
        }

        @Test
        @DisplayName("isBetween should fail for value out of range")
        void isBetweenShouldFailForValueOutOfRange() {
            assertThatThrownBy(() -> NumberAssert.assertThat(5).isBetween(10, 100))
                .isInstanceOf(AssertionException.class);
        }

        @Test
        @DisplayName("isStrictlyBetween should pass")
        void isStrictlyBetweenShouldPass() {
            assertThatNoException().isThrownBy(() ->
                NumberAssert.assertThat(50).isStrictlyBetween(10, 100));
        }

        @Test
        @DisplayName("isStrictlyBetween should fail for boundary")
        void isStrictlyBetweenShouldFailForBoundary() {
            assertThatThrownBy(() -> NumberAssert.assertThat(10).isStrictlyBetween(10, 100))
                .isInstanceOf(AssertionException.class);
        }
    }

    @Nested
    @DisplayName("Closeness Tests")
    class ClosenessTests {

        @Test
        @DisplayName("isCloseTo should pass")
        void isCloseToShouldPass() {
            assertThatNoException().isThrownBy(() ->
                NumberAssert.assertThat(50).isCloseTo(51, 5));
        }

        @Test
        @DisplayName("isCloseTo should fail")
        void isCloseToShouldFail() {
            assertThatThrownBy(() -> NumberAssert.assertThat(50).isCloseTo(100, 5))
                .isInstanceOf(AssertionException.class);
        }
    }

    @Nested
    @DisplayName("Parity Tests")
    class ParityTests {

        @Test
        @DisplayName("isEven should pass for even")
        void isEvenShouldPassForEven() {
            assertThatNoException().isThrownBy(() ->
                NumberAssert.assertThat(42).isEven());
        }

        @Test
        @DisplayName("isEven should fail for odd")
        void isEvenShouldFailForOdd() {
            assertThatThrownBy(() -> NumberAssert.assertThat(43).isEven())
                .isInstanceOf(AssertionException.class);
        }

        @Test
        @DisplayName("isOdd should pass for odd")
        void isOddShouldPassForOdd() {
            assertThatNoException().isThrownBy(() ->
                NumberAssert.assertThat(43).isOdd());
        }

        @Test
        @DisplayName("isOdd should fail for even")
        void isOddShouldFailForEven() {
            assertThatThrownBy(() -> NumberAssert.assertThat(42).isOdd())
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
                NumberAssert.assertThat(42)
                    .isNotNull()
                    .isPositive()
                    .isNotZero()
                    .isEven()
                    .isGreaterThan(10)
                    .isLessThan(100)
                    .isBetween(0, 50));
        }
    }
}
