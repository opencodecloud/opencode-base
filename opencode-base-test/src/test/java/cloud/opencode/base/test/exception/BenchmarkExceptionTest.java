package cloud.opencode.base.test.exception;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * BenchmarkExceptionTest Tests
 * BenchmarkExceptionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("BenchmarkException Tests")
class BenchmarkExceptionTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create with error code")
        void shouldCreateWithErrorCode() {
            BenchmarkException ex = new BenchmarkException(TestErrorCode.BENCHMARK_FAILED);

            assertThat(ex.getTestErrorCode()).isEqualTo(TestErrorCode.BENCHMARK_FAILED);
        }

        @Test
        @DisplayName("Should create with error code and detail")
        void shouldCreateWithErrorCodeAndDetail() {
            BenchmarkException ex = new BenchmarkException(TestErrorCode.BENCHMARK_TIMEOUT, "too slow");

            assertThat(ex.getTestErrorCode()).isEqualTo(TestErrorCode.BENCHMARK_TIMEOUT);
            assertThat(ex.getMessage()).contains("too slow");
        }

        @Test
        @DisplayName("Should create with message")
        void shouldCreateWithMessage() {
            BenchmarkException ex = new BenchmarkException("Custom message");
            assertThat(ex.getMessage()).contains("Custom message");
        }

        @Test
        @DisplayName("Should create with message and cause")
        void shouldCreateWithMessageAndCause() {
            Throwable cause = new RuntimeException("root");
            BenchmarkException ex = new BenchmarkException("Custom message", cause);

            assertThat(ex.getMessage()).contains("Custom message");
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("executionFailed() should create execution failed exception")
        void executionFailedShouldCreateExecutionFailedException() {
            Throwable cause = new RuntimeException("failed");
            BenchmarkException ex = BenchmarkException.executionFailed("myBenchmark", cause);

            assertThat(ex.getMessage()).contains("myBenchmark");
            assertThat(ex.getMessage()).contains("failed");
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("timeout() should create timeout exception")
        void timeoutShouldCreateTimeoutException() {
            BenchmarkException ex = BenchmarkException.timeout("myBenchmark", 5000);

            assertThat(ex.getTestErrorCode()).isEqualTo(TestErrorCode.BENCHMARK_TIMEOUT);
            assertThat(ex.getMessage()).contains("myBenchmark");
            assertThat(ex.getMessage()).contains("5000ms");
        }
    }

    @Nested
    @DisplayName("Inheritance Tests")
    class InheritanceTests {

        @Test
        @DisplayName("Should extend TestException")
        void shouldExtendTestException() {
            BenchmarkException ex = new BenchmarkException(TestErrorCode.BENCHMARK_FAILED);
            assertThat(ex).isInstanceOf(TestException.class);
        }
    }
}
