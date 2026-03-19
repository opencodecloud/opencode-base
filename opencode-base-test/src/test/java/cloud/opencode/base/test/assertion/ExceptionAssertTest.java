package cloud.opencode.base.test.assertion;

import cloud.opencode.base.test.exception.AssertionException;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ExceptionAssertTest Tests
 * ExceptionAssertTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("ExceptionAssert Tests")
class ExceptionAssertTest {

    @Nested
    @DisplayName("assertThatThrownBy Tests")
    class AssertThatThrownByTests {

        @Test
        @DisplayName("Should capture thrown exception")
        void shouldCaptureThrownException() {
            ExceptionAssert assertion = ExceptionAssert.assertThatThrownBy(() -> {
                throw new IllegalArgumentException("test");
            });
            assertThat(assertion).isNotNull();
        }

        @Test
        @DisplayName("Should fail when no exception thrown")
        void shouldFailWhenNoExceptionThrown() {
            assertThatThrownBy(() ->
                ExceptionAssert.assertThatThrownBy(() -> {})
            ).isInstanceOf(AssertionException.class);
        }
    }

    @Nested
    @DisplayName("assertThat Tests")
    class AssertThatTests {

        @Test
        @DisplayName("Should create assertion for exception")
        void shouldCreateAssertionForException() {
            ExceptionAssert assertion = ExceptionAssert.assertThat(new RuntimeException("test"));
            assertThat(assertion).isNotNull();
        }
    }

    @Nested
    @DisplayName("assertThatCode Tests")
    class AssertThatCodeTests {

        @Test
        @DisplayName("Should create assertion for code block")
        void shouldCreateAssertionForCodeBlock() {
            ExceptionAssert assertion = ExceptionAssert.assertThatCode(() -> {});
            assertThat(assertion).isNotNull();
        }
    }

    @Nested
    @DisplayName("doesNotThrowAnyException Tests")
    class DoesNotThrowAnyExceptionTests {

        @Test
        @DisplayName("Should pass when no exception thrown")
        void shouldPassWhenNoExceptionThrown() {
            assertThatNoException().isThrownBy(() ->
                ExceptionAssert.assertThatCode(() -> {}).doesNotThrowAnyException());
        }

        @Test
        @DisplayName("Should fail when exception thrown")
        void shouldFailWhenExceptionThrown() {
            assertThatThrownBy(() ->
                ExceptionAssert.assertThatCode(() -> { throw new RuntimeException("error"); })
                    .doesNotThrowAnyException()
            ).isInstanceOf(AssertionException.class);
        }
    }

    @Nested
    @DisplayName("isInstanceOf Tests")
    class IsInstanceOfTests {

        @Test
        @DisplayName("Should pass for matching type")
        void shouldPassForMatchingType() {
            assertThatNoException().isThrownBy(() ->
                ExceptionAssert.assertThatThrownBy(() -> { throw new IllegalArgumentException("test"); })
                    .isInstanceOf(IllegalArgumentException.class));
        }

        @Test
        @DisplayName("Should pass for supertype")
        void shouldPassForSupertype() {
            assertThatNoException().isThrownBy(() ->
                ExceptionAssert.assertThatThrownBy(() -> { throw new IllegalArgumentException("test"); })
                    .isInstanceOf(RuntimeException.class));
        }

        @Test
        @DisplayName("Should fail for non-matching type")
        void shouldFailForNonMatchingType() {
            assertThatThrownBy(() ->
                ExceptionAssert.assertThatThrownBy(() -> { throw new IllegalArgumentException("test"); })
                    .isInstanceOf(IllegalStateException.class)
            ).isInstanceOf(AssertionException.class);
        }
    }

    @Nested
    @DisplayName("isExactlyInstanceOf Tests")
    class IsExactlyInstanceOfTests {

        @Test
        @DisplayName("Should pass for exact type")
        void shouldPassForExactType() {
            assertThatNoException().isThrownBy(() ->
                ExceptionAssert.assertThatThrownBy(() -> { throw new IllegalArgumentException("test"); })
                    .isExactlyInstanceOf(IllegalArgumentException.class));
        }

        @Test
        @DisplayName("Should fail for supertype")
        void shouldFailForSupertype() {
            assertThatThrownBy(() ->
                ExceptionAssert.assertThatThrownBy(() -> { throw new IllegalArgumentException("test"); })
                    .isExactlyInstanceOf(RuntimeException.class)
            ).isInstanceOf(AssertionException.class);
        }
    }

    @Nested
    @DisplayName("hasMessage Tests")
    class HasMessageTests {

        @Test
        @DisplayName("Should pass for matching message")
        void shouldPassForMatchingMessage() {
            assertThatNoException().isThrownBy(() ->
                ExceptionAssert.assertThatThrownBy(() -> { throw new RuntimeException("expected message"); })
                    .hasMessage("expected message"));
        }

        @Test
        @DisplayName("Should fail for non-matching message")
        void shouldFailForNonMatchingMessage() {
            assertThatThrownBy(() ->
                ExceptionAssert.assertThatThrownBy(() -> { throw new RuntimeException("actual"); })
                    .hasMessage("expected")
            ).isInstanceOf(AssertionException.class);
        }
    }

    @Nested
    @DisplayName("hasMessageContaining Tests")
    class HasMessageContainingTests {

        @Test
        @DisplayName("Should pass when message contains substring")
        void shouldPassWhenMessageContainsSubstring() {
            assertThatNoException().isThrownBy(() ->
                ExceptionAssert.assertThatThrownBy(() -> { throw new RuntimeException("the error message"); })
                    .hasMessageContaining("error"));
        }

        @Test
        @DisplayName("Should fail when message does not contain substring")
        void shouldFailWhenMessageDoesNotContainSubstring() {
            assertThatThrownBy(() ->
                ExceptionAssert.assertThatThrownBy(() -> { throw new RuntimeException("message"); })
                    .hasMessageContaining("not found")
            ).isInstanceOf(AssertionException.class);
        }
    }

    @Nested
    @DisplayName("hasMessageStartingWith Tests")
    class HasMessageStartingWithTests {

        @Test
        @DisplayName("Should pass when message starts with prefix")
        void shouldPassWhenMessageStartsWithPrefix() {
            assertThatNoException().isThrownBy(() ->
                ExceptionAssert.assertThatThrownBy(() -> { throw new RuntimeException("Error: something"); })
                    .hasMessageStartingWith("Error:"));
        }

        @Test
        @DisplayName("Should fail when message does not start with prefix")
        void shouldFailWhenMessageDoesNotStartWithPrefix() {
            assertThatThrownBy(() ->
                ExceptionAssert.assertThatThrownBy(() -> { throw new RuntimeException("message"); })
                    .hasMessageStartingWith("Error")
            ).isInstanceOf(AssertionException.class);
        }
    }

    @Nested
    @DisplayName("hasMessageEndingWith Tests")
    class HasMessageEndingWithTests {

        @Test
        @DisplayName("Should pass when message ends with suffix")
        void shouldPassWhenMessageEndsWithSuffix() {
            assertThatNoException().isThrownBy(() ->
                ExceptionAssert.assertThatThrownBy(() -> { throw new RuntimeException("something failed"); })
                    .hasMessageEndingWith("failed"));
        }

        @Test
        @DisplayName("Should fail when message does not end with suffix")
        void shouldFailWhenMessageDoesNotEndWithSuffix() {
            assertThatThrownBy(() ->
                ExceptionAssert.assertThatThrownBy(() -> { throw new RuntimeException("message"); })
                    .hasMessageEndingWith("error")
            ).isInstanceOf(AssertionException.class);
        }
    }

    @Nested
    @DisplayName("Cause Tests")
    class CauseTests {

        @Test
        @DisplayName("hasCause should pass when cause exists")
        void hasCauseShouldPassWhenCauseExists() {
            assertThatNoException().isThrownBy(() ->
                ExceptionAssert.assertThatThrownBy(() -> {
                    throw new RuntimeException("outer", new IllegalArgumentException("inner"));
                }).hasCause());
        }

        @Test
        @DisplayName("hasCause should fail when no cause")
        void hasCauseShouldFailWhenNoCause() {
            assertThatThrownBy(() ->
                ExceptionAssert.assertThatThrownBy(() -> { throw new RuntimeException("no cause"); })
                    .hasCause()
            ).isInstanceOf(AssertionException.class);
        }

        @Test
        @DisplayName("hasNoCause should pass when no cause")
        void hasNoCauseShouldPassWhenNoCause() {
            assertThatNoException().isThrownBy(() ->
                ExceptionAssert.assertThatThrownBy(() -> { throw new RuntimeException("no cause"); })
                    .hasNoCause());
        }

        @Test
        @DisplayName("hasNoCause should fail when cause exists")
        void hasNoCauseShouldFailWhenCauseExists() {
            assertThatThrownBy(() ->
                ExceptionAssert.assertThatThrownBy(() -> {
                    throw new RuntimeException("outer", new IllegalArgumentException("inner"));
                }).hasNoCause()
            ).isInstanceOf(AssertionException.class);
        }

        @Test
        @DisplayName("hasCauseInstanceOf should pass for matching cause type")
        void hasCauseInstanceOfShouldPassForMatchingCauseType() {
            assertThatNoException().isThrownBy(() ->
                ExceptionAssert.assertThatThrownBy(() -> {
                    throw new RuntimeException("outer", new IllegalArgumentException("inner"));
                }).hasCauseInstanceOf(IllegalArgumentException.class));
        }

        @Test
        @DisplayName("hasRootCauseInstanceOf should pass for matching root cause type")
        void hasRootCauseInstanceOfShouldPassForMatchingRootCauseType() {
            assertThatNoException().isThrownBy(() ->
                ExceptionAssert.assertThatThrownBy(() -> {
                    throw new RuntimeException("level1",
                        new IllegalStateException("level2",
                            new IllegalArgumentException("root")));
                }).hasRootCauseInstanceOf(IllegalArgumentException.class));
        }
    }

    @Nested
    @DisplayName("getThrowable Tests")
    class GetThrowableTests {

        @Test
        @DisplayName("Should return the captured exception")
        void shouldReturnTheCapturedexception() {
            ExceptionAssert assertion = ExceptionAssert.assertThatThrownBy(() -> {
                throw new IllegalArgumentException("test");
            });
            Throwable throwable = assertion.getThrowable();
            assertThat(throwable).isInstanceOf(IllegalArgumentException.class);
            assertThat(throwable.getMessage()).isEqualTo("test");
        }
    }

    @Nested
    @DisplayName("Chaining Tests")
    class ChainingTests {

        @Test
        @DisplayName("Should support fluent chaining")
        void shouldSupportFluentChaining() {
            assertThatNoException().isThrownBy(() ->
                ExceptionAssert.assertThatThrownBy(() -> {
                    throw new IllegalArgumentException("Invalid input");
                })
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid input")
                    .hasMessageContaining("Invalid")
                    .hasNoCause());
        }
    }
}
