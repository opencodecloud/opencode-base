package cloud.opencode.base.log.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenLogException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
@DisplayName("OpenLogException 测试")
class OpenLogExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("消息构造函数")
        void testMessageConstructor() {
            OpenLogException ex = new OpenLogException("Test message");

            assertThat(ex.getMessage()).contains("Test message");
        }

        @Test
        @DisplayName("消息和原因构造函数")
        void testMessageAndCauseConstructor() {
            RuntimeException cause = new RuntimeException("cause");
            OpenLogException ex = new OpenLogException("Test message", cause);

            assertThat(ex.getMessage()).contains("Test message");
            assertThat(ex.getCause()).isSameAs(cause);
        }
    }

    @Nested
    @DisplayName("providerNotFound工厂方法测试")
    class ProviderNotFoundTests {

        @Test
        @DisplayName("创建提供者未找到异常")
        void testProviderNotFound() {
            OpenLogException ex = OpenLogException.providerNotFound();

            assertThat(ex).isNotNull();
            assertThat(ex.getMessage()).contains("No log provider found");
        }
    }

    @Nested
    @DisplayName("initializationFailed工厂方法测试")
    class InitializationFailedTests {

        @Test
        @DisplayName("创建初始化失败异常")
        void testInitializationFailed() {
            RuntimeException cause = new RuntimeException("init error");
            OpenLogException ex = OpenLogException.initializationFailed(cause);

            assertThat(ex).isNotNull();
            assertThat(ex.getMessage()).contains("Failed to initialize");
            assertThat(ex.getCause()).isSameAs(cause);
        }
    }

    @Nested
    @DisplayName("invalidConfig工厂方法测试")
    class InvalidConfigTests {

        @Test
        @DisplayName("创建无效配置异常")
        void testInvalidConfig() {
            OpenLogException ex = OpenLogException.invalidConfig("log.level", "must be valid level");

            assertThat(ex).isNotNull();
            assertThat(ex.getMessage()).contains("Invalid log configuration");
            assertThat(ex.getMessage()).contains("log.level");
            assertThat(ex.getMessage()).contains("must be valid level");
        }
    }

    @Nested
    @DisplayName("adapterNotFound工厂方法测试")
    class AdapterNotFoundTests {

        @Test
        @DisplayName("创建适配器未找到异常")
        void testAdapterNotFound() {
            OpenLogException ex = OpenLogException.adapterNotFound("SLF4J");

            assertThat(ex).isNotNull();
            assertThat(ex.getMessage()).contains("Log adapter not found");
            assertThat(ex.getMessage()).contains("SLF4J");
        }
    }

    @Nested
    @DisplayName("unsupportedOperation工厂方法测试")
    class UnsupportedOperationTests {

        @Test
        @DisplayName("创建不支持操作异常")
        void testUnsupportedOperation() {
            OpenLogException ex = OpenLogException.unsupportedOperation("NDC");

            assertThat(ex).isNotNull();
            assertThat(ex.getMessage()).contains("Unsupported log operation");
            assertThat(ex.getMessage()).contains("NDC");
        }
    }

    @Nested
    @DisplayName("继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("是RuntimeException的子类")
        void testIsRuntimeException() {
            OpenLogException ex = new OpenLogException("test");
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("可以被捕获为Exception")
        void testCanBeCaughtAsException() {
            assertThatCode(() -> {
                try {
                    throw new OpenLogException("test");
                } catch (Exception e) {
                    // Expected
                }
            }).doesNotThrowAnyException();
        }
    }
}
