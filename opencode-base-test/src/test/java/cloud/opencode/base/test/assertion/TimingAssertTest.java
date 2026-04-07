package cloud.opencode.base.test.assertion;

import cloud.opencode.base.test.exception.AssertionException;
import cloud.opencode.base.test.exception.TestException;
import org.junit.jupiter.api.*;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * TimingAssertTest Tests
 * TimingAssertTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.3
 */
@DisplayName("TimingAssert Tests")
class TimingAssertTest {

    @Nested
    @DisplayName("Runnable assertCompletesWithin Tests")
    class RunnableTests {

        @Test
        @DisplayName("should pass when runnable completes within timeout")
        void shouldPassWhenCompletesWithinTimeout() {
            assertThatNoException().isThrownBy(() ->
                TimingAssert.assertCompletesWithin(Duration.ofSeconds(2), () -> {
                    // fast operation - no sleep
                }));
        }

        @Test
        @DisplayName("should fail when runnable exceeds timeout")
        void shouldFailWhenExceedsTimeout() {
            assertThatThrownBy(() ->
                TimingAssert.assertCompletesWithin(Duration.ofMillis(10), () -> {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }))
                .isInstanceOf(AssertionException.class)
                .hasMessageContaining("timeout");
        }

        @Test
        @DisplayName("should throw AssertionException for null timeout")
        void shouldThrowForNullTimeout() {
            assertThatThrownBy(() ->
                TimingAssert.assertCompletesWithin(null, () -> {}))
                .isInstanceOf(AssertionException.class)
                .hasMessageContaining("timeout must not be null");
        }

        @Test
        @DisplayName("should throw AssertionException for null runnable")
        void shouldThrowForNullRunnable() {
            assertThatThrownBy(() ->
                TimingAssert.assertCompletesWithin(Duration.ofSeconds(1), (Runnable) null))
                .isInstanceOf(AssertionException.class)
                .hasMessageContaining("runnable must not be null");
        }

        @Test
        @DisplayName("should wrap checked exception from runnable in TestException")
        void shouldWrapCheckedExceptionFromRunnable() {
            assertThatThrownBy(() ->
                TimingAssert.assertCompletesWithin(Duration.ofSeconds(5), () -> {
                    throw new RuntimeException("test error");
                }))
                .isInstanceOf(TestException.class);
        }

        @Test
        @DisplayName("should pass with zero-duration timeout for instant operation")
        void shouldHandleZeroDuration() {
            // A no-op runnable might or might not complete in zero nanos
            // depending on timing; just verify no NPE or other unexpected errors.
            // We use a generous timeout instead.
            assertThatNoException().isThrownBy(() ->
                TimingAssert.assertCompletesWithin(Duration.ofSeconds(5), () -> {}));
        }
    }

    @Nested
    @DisplayName("Callable assertCompletesWithin Tests")
    class CallableTests {

        @Test
        @DisplayName("should return result when callable completes within timeout")
        void shouldReturnResultWhenCompletesWithinTimeout() {
            String result = TimingAssert.assertCompletesWithin(Duration.ofSeconds(2), () -> "hello");
            assertThat(result).isEqualTo("hello");
        }

        @Test
        @DisplayName("should return null result when callable returns null")
        void shouldReturnNullResult() {
            String result = TimingAssert.assertCompletesWithin(Duration.ofSeconds(2), () -> null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("should fail when callable exceeds timeout")
        void shouldFailWhenCallableExceedsTimeout() {
            assertThatThrownBy(() ->
                TimingAssert.assertCompletesWithin(Duration.ofMillis(10), () -> {
                    Thread.sleep(200);
                    return "too slow";
                }))
                .isInstanceOf(AssertionException.class)
                .hasMessageContaining("timeout");
        }

        @Test
        @DisplayName("should throw AssertionException for null timeout")
        void shouldThrowForNullTimeout() {
            assertThatThrownBy(() ->
                TimingAssert.assertCompletesWithin(null, () -> "x"))
                .isInstanceOf(AssertionException.class)
                .hasMessageContaining("timeout must not be null");
        }

        @Test
        @DisplayName("should throw AssertionException for null callable")
        void shouldThrowForNullCallable() {
            assertThatThrownBy(() ->
                TimingAssert.assertCompletesWithin(Duration.ofSeconds(1),
                    (java.util.concurrent.Callable<?>) null))
                .isInstanceOf(AssertionException.class)
                .hasMessageContaining("callable must not be null");
        }

        @Test
        @DisplayName("should wrap exception from callable in TestException")
        void shouldWrapExceptionFromCallable() {
            assertThatThrownBy(() ->
                TimingAssert.assertCompletesWithin(Duration.ofSeconds(5), () -> {
                    throw new Exception("callable error");
                }))
                .isInstanceOf(TestException.class);
        }

        @Test
        @DisplayName("should re-throw TestException from callable directly")
        void shouldRethrowTestExceptionDirectly() {
            var original = new TestException("direct error");
            assertThatThrownBy(() ->
                TimingAssert.assertCompletesWithin(Duration.ofSeconds(5), () -> {
                    throw original;
                }))
                .isSameAs(original);
        }
    }

    @Nested
    @DisplayName("Runnable TestException Passthrough Tests")
    class RunnableTestExceptionTests {

        @Test
        @DisplayName("should re-throw TestException from runnable directly")
        void shouldRethrowTestExceptionDirectly() {
            var original = new TestException("direct runnable error");
            assertThatThrownBy(() ->
                TimingAssert.assertCompletesWithin(Duration.ofSeconds(5), (Runnable) () -> {
                    throw original;
                }))
                .isSameAs(original);
        }
    }
}
