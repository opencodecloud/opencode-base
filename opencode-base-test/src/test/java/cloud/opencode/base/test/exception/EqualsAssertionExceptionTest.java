package cloud.opencode.base.test.exception;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * EqualsAssertionExceptionTest Tests
 * EqualsAssertionExceptionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("EqualsAssertionException Tests")
class EqualsAssertionExceptionTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create with expected and actual values")
        void shouldCreateWithExpectedAndActualValues() {
            EqualsAssertionException ex = new EqualsAssertionException("expected", "actual");

            assertThat(ex.getExpected()).isEqualTo("expected");
            assertThat(ex.getActual()).isEqualTo("actual");
            assertThat(ex.getMessage()).contains("expected").contains("actual");
        }

        @Test
        @DisplayName("Should create with expected, actual and message")
        void shouldCreateWithExpectedActualAndMessage() {
            EqualsAssertionException ex = new EqualsAssertionException("expected", "actual", "custom message");

            assertThat(ex.getExpected()).isEqualTo("expected");
            assertThat(ex.getActual()).isEqualTo("actual");
            assertThat(ex.getMessage()).contains("custom message");
        }

        @Test
        @DisplayName("Should create with message only")
        void shouldCreateWithMessageOnly() {
            EqualsAssertionException ex = new EqualsAssertionException("error message");

            assertThat(ex.getExpected()).isNull();
            assertThat(ex.getActual()).isNull();
            assertThat(ex.getMessage()).isEqualTo("error message");
        }

        @Test
        @DisplayName("Should create with message and cause")
        void shouldCreateWithMessageAndCause() {
            RuntimeException cause = new RuntimeException("cause");
            EqualsAssertionException ex = new EqualsAssertionException("error message", cause);

            assertThat(ex.getMessage()).isEqualTo("error message");
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("of() should create exception with expected and actual")
        void ofShouldCreateExceptionWithExpectedAndActual() {
            EqualsAssertionException ex = EqualsAssertionException.of("expected", "actual");

            assertThat(ex.getExpected()).isEqualTo("expected");
            assertThat(ex.getActual()).isEqualTo("actual");
        }

        @Test
        @DisplayName("of() should create exception with message")
        void ofShouldCreateExceptionWithMessage() {
            EqualsAssertionException ex = EqualsAssertionException.of("expected", "actual", "custom");

            assertThat(ex.getMessage()).contains("custom");
        }

        @Test
        @DisplayName("expectedNull() should create exception for null expected")
        void expectedNullShouldCreateExceptionForNullExpected() {
            EqualsAssertionException ex = EqualsAssertionException.expectedNull("actual");

            assertThat(ex.getExpected()).isNull();
            assertThat(ex.getActual()).isEqualTo("actual");
            assertThat(ex.getMessage()).contains("Expected null");
        }

        @Test
        @DisplayName("actualNull() should create exception for null actual")
        void actualNullShouldCreateExceptionForNullActual() {
            EqualsAssertionException ex = EqualsAssertionException.actualNull("expected");

            assertThat(ex.getExpected()).isEqualTo("expected");
            assertThat(ex.getActual()).isNull();
            assertThat(ex.getMessage()).contains("was null");
        }
    }

    @Nested
    @DisplayName("Assertion Method Tests")
    class AssertionMethodTests {

        @Test
        @DisplayName("assertEqualsOrThrow should pass when equal")
        void assertEqualsOrThrowShouldPassWhenEqual() {
            assertThatNoException().isThrownBy(() ->
                EqualsAssertionException.assertEqualsOrThrow("test", "test"));
        }

        @Test
        @DisplayName("assertEqualsOrThrow should throw when not equal")
        void assertEqualsOrThrowShouldThrowWhenNotEqual() {
            assertThatThrownBy(() ->
                EqualsAssertionException.assertEqualsOrThrow("expected", "actual"))
                .isInstanceOf(EqualsAssertionException.class);
        }

        @Test
        @DisplayName("assertEqualsOrThrow with message should include message")
        void assertEqualsOrThrowWithMessageShouldIncludeMessage() {
            assertThatThrownBy(() ->
                EqualsAssertionException.assertEqualsOrThrow("expected", "actual", "values must match"))
                .isInstanceOf(EqualsAssertionException.class)
                .hasMessageContaining("values must match");
        }

        @Test
        @DisplayName("assertEqualsOrThrow should handle null values")
        void assertEqualsOrThrowShouldHandleNullValues() {
            assertThatNoException().isThrownBy(() ->
                EqualsAssertionException.assertEqualsOrThrow(null, null));

            assertThatThrownBy(() ->
                EqualsAssertionException.assertEqualsOrThrow(null, "value"))
                .isInstanceOf(EqualsAssertionException.class);
        }
    }

    @Nested
    @DisplayName("Getter Tests")
    class GetterTests {

        @Test
        @DisplayName("getExpectedString should return string representation")
        void getExpectedStringShouldReturnStringRepresentation() {
            EqualsAssertionException ex = EqualsAssertionException.of(123, 456);
            assertThat(ex.getExpectedString()).isEqualTo("123");
        }

        @Test
        @DisplayName("getActualString should return string representation")
        void getActualStringShouldReturnStringRepresentation() {
            EqualsAssertionException ex = EqualsAssertionException.of(123, 456);
            assertThat(ex.getActualString()).isEqualTo("456");
        }

        @Test
        @DisplayName("getExpectedString should return 'null' for null value")
        void getExpectedStringShouldReturnNullForNullValue() {
            EqualsAssertionException ex = new EqualsAssertionException("message");
            assertThat(ex.getExpectedString()).isEqualTo("null");
        }
    }

    @Nested
    @DisplayName("Comparison Info Tests")
    class ComparisonInfoTests {

        @Test
        @DisplayName("getComparisonInfo should return detailed info")
        void getComparisonInfoShouldReturnDetailedInfo() {
            EqualsAssertionException ex = EqualsAssertionException.of("expected", "actual");
            String info = ex.getComparisonInfo();

            assertThat(info).contains("Equality Assertion Failed");
            assertThat(info).contains("Expected:");
            assertThat(info).contains("Actual:");
        }

        @Test
        @DisplayName("getComparisonInfo should show type info for non-null values")
        void getComparisonInfoShouldShowTypeInfoForNonNullValues() {
            EqualsAssertionException ex = EqualsAssertionException.of("expected", "actual");
            String info = ex.getComparisonInfo();

            assertThat(info).contains("Expected Type:");
            assertThat(info).contains("Actual Type:");
        }

        @Test
        @DisplayName("getComparisonInfo should show string length for string values")
        void getComparisonInfoShouldShowStringLengthForStringValues() {
            EqualsAssertionException ex = EqualsAssertionException.of("abc", "abcd");
            String info = ex.getComparisonInfo();

            assertThat(info).contains("Expected Length:");
            assertThat(info).contains("Actual Length:");
            assertThat(info).contains("First Difference at index:");
        }

        @Test
        @DisplayName("getComparisonInfo should handle null values")
        void getComparisonInfoShouldHandleNullValues() {
            EqualsAssertionException ex = EqualsAssertionException.actualNull("expected");
            String info = ex.getComparisonInfo();

            assertThat(info).contains("null");
        }

        @Test
        @DisplayName("getComparisonInfo should format character values")
        void getComparisonInfoShouldFormatCharacterValues() {
            EqualsAssertionException ex = EqualsAssertionException.of('a', 'b');
            String info = ex.getComparisonInfo();

            assertThat(info).contains("'a'");
            assertThat(info).contains("'b'");
        }
    }
}
