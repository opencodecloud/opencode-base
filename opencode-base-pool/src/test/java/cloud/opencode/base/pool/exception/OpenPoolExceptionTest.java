package cloud.opencode.base.pool.exception;

import org.junit.jupiter.api.*;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenPoolExceptionTest Tests
 * OpenPoolExceptionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
@DisplayName("OpenPoolException 测试")
class OpenPoolExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用消息构造")
        void testMessageConstructor() {
            OpenPoolException ex = new OpenPoolException("test error");

            assertThat(ex.getMessage()).contains("test error");
            assertThat(ex.poolName()).isNull();
            assertThat(ex.errorType()).isEqualTo(OpenPoolException.PoolErrorType.GENERAL);
        }

        @Test
        @DisplayName("使用消息和原因构造")
        void testMessageAndCauseConstructor() {
            RuntimeException cause = new RuntimeException("root cause");
            OpenPoolException ex = new OpenPoolException("test error", cause);

            assertThat(ex.getMessage()).contains("test error");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.errorType()).isEqualTo(OpenPoolException.PoolErrorType.GENERAL);
        }

        @Test
        @DisplayName("使用完整详情构造")
        void testFullConstructor() {
            OpenPoolException ex = new OpenPoolException(
                    "test error", "my-pool", OpenPoolException.PoolErrorType.EXHAUSTED);

            assertThat(ex.getMessage()).contains("test error");
            assertThat(ex.poolName()).isEqualTo("my-pool");
            assertThat(ex.errorType()).isEqualTo(OpenPoolException.PoolErrorType.EXHAUSTED);
        }

        @Test
        @DisplayName("使用完整详情和原因构造")
        void testFullConstructorWithCause() {
            RuntimeException cause = new RuntimeException("root");
            OpenPoolException ex = new OpenPoolException(
                    "test error", "my-pool", OpenPoolException.PoolErrorType.TIMEOUT, cause);

            assertThat(ex.getMessage()).contains("test error");
            assertThat(ex.poolName()).isEqualTo("my-pool");
            assertThat(ex.errorType()).isEqualTo(OpenPoolException.PoolErrorType.TIMEOUT);
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("exhausted创建池耗尽异常")
        void testExhausted() {
            OpenPoolException ex = OpenPoolException.exhausted("connection-pool");

            assertThat(ex.getMessage()).contains("exhausted");
            assertThat(ex.poolName()).isEqualTo("connection-pool");
            assertThat(ex.errorType()).isEqualTo(OpenPoolException.PoolErrorType.EXHAUSTED);
        }

        @Test
        @DisplayName("timeout创建超时异常")
        void testTimeout() {
            OpenPoolException ex = OpenPoolException.timeout("db-pool", Duration.ofSeconds(5));

            assertThat(ex.getMessage()).contains("5000ms");
            assertThat(ex.poolName()).isEqualTo("db-pool");
            assertThat(ex.errorType()).isEqualTo(OpenPoolException.PoolErrorType.TIMEOUT);
        }

        @Test
        @DisplayName("validationFailed创建验证失败异常")
        void testValidationFailed() {
            OpenPoolException ex = OpenPoolException.validationFailed("my-pool");

            assertThat(ex.getMessage()).contains("validation");
            assertThat(ex.poolName()).isEqualTo("my-pool");
            assertThat(ex.errorType()).isEqualTo(OpenPoolException.PoolErrorType.VALIDATION);
        }

        @Test
        @DisplayName("closed创建池已关闭异常")
        void testClosed() {
            OpenPoolException ex = OpenPoolException.closed("my-pool");

            assertThat(ex.getMessage()).contains("closed");
            assertThat(ex.poolName()).isEqualTo("my-pool");
            assertThat(ex.errorType()).isEqualTo(OpenPoolException.PoolErrorType.CLOSED);
        }

        @Test
        @DisplayName("createFailed创建对象创建失败异常")
        void testCreateFailed() {
            RuntimeException cause = new RuntimeException("create error");
            OpenPoolException ex = OpenPoolException.createFailed("my-pool", cause);

            assertThat(ex.getMessage()).contains("create");
            assertThat(ex.poolName()).isEqualTo("my-pool");
            assertThat(ex.errorType()).isEqualTo(OpenPoolException.PoolErrorType.CREATE);
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("destroyFailed创建对象销毁失败异常")
        void testDestroyFailed() {
            RuntimeException cause = new RuntimeException("destroy error");
            OpenPoolException ex = OpenPoolException.destroyFailed("my-pool", cause);

            assertThat(ex.getMessage()).contains("destroy");
            assertThat(ex.poolName()).isEqualTo("my-pool");
            assertThat(ex.errorType()).isEqualTo(OpenPoolException.PoolErrorType.DESTROY);
        }

        @Test
        @DisplayName("activateFailed创建对象激活失败异常")
        void testActivateFailed() {
            RuntimeException cause = new RuntimeException("activate error");
            OpenPoolException ex = OpenPoolException.activateFailed("my-pool", cause);

            assertThat(ex.getMessage()).contains("activate");
            assertThat(ex.poolName()).isEqualTo("my-pool");
            assertThat(ex.errorType()).isEqualTo(OpenPoolException.PoolErrorType.ACTIVATE);
        }

        @Test
        @DisplayName("passivateFailed创建对象钝化失败异常")
        void testPassivateFailed() {
            RuntimeException cause = new RuntimeException("passivate error");
            OpenPoolException ex = OpenPoolException.passivateFailed("my-pool", cause);

            assertThat(ex.getMessage()).contains("passivate");
            assertThat(ex.poolName()).isEqualTo("my-pool");
            assertThat(ex.errorType()).isEqualTo(OpenPoolException.PoolErrorType.PASSIVATE);
        }

        @Test
        @DisplayName("invalidState创建非法状态异常")
        void testInvalidState() {
            OpenPoolException ex = OpenPoolException.invalidState("Invalid state message");

            assertThat(ex.getMessage()).contains("Invalid state message");
            assertThat(ex.poolName()).isNull();
            assertThat(ex.errorType()).isEqualTo(OpenPoolException.PoolErrorType.INVALID_STATE);
        }
    }

    @Nested
    @DisplayName("PoolErrorType枚举测试")
    class PoolErrorTypeTests {

        @Test
        @DisplayName("包含所有错误类型")
        void testAllErrorTypes() {
            OpenPoolException.PoolErrorType[] types = OpenPoolException.PoolErrorType.values();

            assertThat(types).hasSize(10);
            assertThat(types).contains(
                    OpenPoolException.PoolErrorType.GENERAL,
                    OpenPoolException.PoolErrorType.EXHAUSTED,
                    OpenPoolException.PoolErrorType.TIMEOUT,
                    OpenPoolException.PoolErrorType.VALIDATION,
                    OpenPoolException.PoolErrorType.CLOSED,
                    OpenPoolException.PoolErrorType.CREATE,
                    OpenPoolException.PoolErrorType.DESTROY,
                    OpenPoolException.PoolErrorType.ACTIVATE,
                    OpenPoolException.PoolErrorType.PASSIVATE,
                    OpenPoolException.PoolErrorType.INVALID_STATE
            );
        }

        @Test
        @DisplayName("valueOf返回正确的枚举值")
        void testValueOf() {
            assertThat(OpenPoolException.PoolErrorType.valueOf("EXHAUSTED"))
                    .isEqualTo(OpenPoolException.PoolErrorType.EXHAUSTED);
        }
    }
}
