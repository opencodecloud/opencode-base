package cloud.opencode.base.parallel.exception;

import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenParallelExceptionTest Tests
 * OpenParallelExceptionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-parallel V1.0.0
 */
@DisplayName("OpenParallelException 测试")
class OpenParallelExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用消息构造")
        void testMessageConstructor() {
            OpenParallelException ex = new OpenParallelException("test error");

            assertThat(ex.getMessage()).contains("test error");
            assertThat(ex.getSuppressedExceptions()).isEmpty();
            assertThat(ex.getFailedCount()).isZero();
            assertThat(ex.getTotalCount()).isZero();
        }

        @Test
        @DisplayName("使用消息和原因构造")
        void testMessageAndCauseConstructor() {
            RuntimeException cause = new RuntimeException("root cause");
            OpenParallelException ex = new OpenParallelException("test error", cause);

            assertThat(ex.getMessage()).contains("test error");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getFailedCount()).isEqualTo(1);
            assertThat(ex.getTotalCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("使用完整详情构造")
        void testFullConstructor() {
            List<Throwable> suppressed = List.of(
                    new RuntimeException("error1"),
                    new RuntimeException("error2")
            );
            OpenParallelException ex = new OpenParallelException("test error", suppressed, 2, 5);

            assertThat(ex.getMessage()).contains("test error");
            assertThat(ex.getSuppressedExceptions()).hasSize(2);
            assertThat(ex.getFailedCount()).isEqualTo(2);
            assertThat(ex.getTotalCount()).isEqualTo(5);
        }

        @Test
        @DisplayName("null suppressed列表转为空列表")
        void testNullSuppressedList() {
            OpenParallelException ex = new OpenParallelException("test error", null, 1, 2);

            assertThat(ex.getSuppressedExceptions()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Getter方法测试")
    class GetterTests {

        @Test
        @DisplayName("getSuccessCount返回成功数")
        void testGetSuccessCount() {
            OpenParallelException ex = new OpenParallelException("error", List.of(), 3, 10);

            assertThat(ex.getSuccessCount()).isEqualTo(7);
        }

        @Test
        @DisplayName("getSuppressedExceptions返回不可变列表")
        void testSuppressedExceptionsImmutable() {
            List<Throwable> suppressed = List.of(new RuntimeException("error"));
            OpenParallelException ex = new OpenParallelException("error", suppressed, 1, 1);

            assertThatThrownBy(() -> ex.getSuppressedExceptions().clear())
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("timeout创建超时异常")
        void testTimeoutFactory() {
            Duration timeout = Duration.ofSeconds(30);
            OpenParallelException ex = OpenParallelException.timeout(timeout);

            assertThat(ex.getMessage()).contains("30000ms");
        }

        @Test
        @DisplayName("interrupted创建中断异常")
        void testInterruptedFactory() {
            InterruptedException cause = new InterruptedException("interrupted");
            OpenParallelException ex = OpenParallelException.interrupted(cause);

            assertThat(ex.getMessage()).contains("interrupted");
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("partialFailure创建部分失败异常")
        void testPartialFailureFactory() {
            List<Throwable> failures = List.of(
                    new RuntimeException("error1"),
                    new RuntimeException("error2")
            );
            OpenParallelException ex = OpenParallelException.partialFailure(failures, 10);

            assertThat(ex.getMessage()).contains("2/10");
            assertThat(ex.getFailedCount()).isEqualTo(2);
            assertThat(ex.getTotalCount()).isEqualTo(10);
        }

        @Test
        @DisplayName("allFailed创建全部失败异常")
        void testAllFailedFactory() {
            List<Throwable> failures = List.of(
                    new RuntimeException("error1"),
                    new RuntimeException("error2"),
                    new RuntimeException("error3")
            );
            OpenParallelException ex = OpenParallelException.allFailed(failures);

            assertThat(ex.getMessage()).contains("3");
            assertThat(ex.getFailedCount()).isEqualTo(3);
            assertThat(ex.getTotalCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("executionFailed创建执行失败异常")
        void testExecutionFailedFactory() {
            RuntimeException cause = new RuntimeException("root cause");
            OpenParallelException ex = OpenParallelException.executionFailed("custom message", cause);

            assertThat(ex.getMessage()).contains("custom message");
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }
}
