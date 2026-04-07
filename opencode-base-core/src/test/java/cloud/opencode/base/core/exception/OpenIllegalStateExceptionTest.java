package cloud.opencode.base.core.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenIllegalStateException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("OpenIllegalStateException 测试")
class OpenIllegalStateExceptionTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("仅消息构造")
        void testMessageConstructor() {
            OpenIllegalStateException ex = new OpenIllegalStateException("Invalid state");

            assertThat(ex.getMessage()).isEqualTo("[Core] (ILLEGAL_STATE) Invalid state");
            assertThat(ex.getComponent()).isEqualTo("Core");
            assertThat(ex.getErrorCode()).isEqualTo("ILLEGAL_STATE");
        }

        @Test
        @DisplayName("消息和原因构造")
        void testMessageAndCauseConstructor() {
            Exception cause = new RuntimeException("Original");
            OpenIllegalStateException ex = new OpenIllegalStateException("Invalid state", cause);

            assertThat(ex.getMessage()).isEqualTo("[Core] (ILLEGAL_STATE) Invalid state");
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("静态工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("notInitialized 创建未初始化异常")
        void testNotInitialized() {
            OpenIllegalStateException ex = OpenIllegalStateException.notInitialized("CacheManager");

            assertThat(ex.getMessage()).contains("CacheManager");
            assertThat(ex.getMessage()).contains("has not been initialized");
        }

        @Test
        @DisplayName("alreadyClosed 创建已关闭异常")
        void testAlreadyClosed() {
            OpenIllegalStateException ex = OpenIllegalStateException.alreadyClosed("Connection");

            assertThat(ex.getMessage()).contains("Connection");
            assertThat(ex.getMessage()).contains("has already been closed");
        }

        @Test
        @DisplayName("alreadyExists 创建已存在异常")
        void testAlreadyExists() {
            OpenIllegalStateException ex = OpenIllegalStateException.alreadyExists("User", "user123");

            assertThat(ex.getMessage()).contains("User");
            assertThat(ex.getMessage()).contains("already exists");
            assertThat(ex.getMessage()).contains("user123");
        }

        @Test
        @DisplayName("notFound 创建未找到异常")
        void testNotFound() {
            OpenIllegalStateException ex = OpenIllegalStateException.notFound("Order", "ORD-001");

            assertThat(ex.getMessage()).contains("Order");
            assertThat(ex.getMessage()).contains("not found");
            assertThat(ex.getMessage()).contains("ORD-001");
        }

        @Test
        @DisplayName("invalidState 创建无效状态异常")
        void testInvalidState() {
            OpenIllegalStateException ex = OpenIllegalStateException.invalidState("RUNNING", "STOPPED");

            assertThat(ex.getMessage()).contains("Expected state: RUNNING");
            assertThat(ex.getMessage()).contains("but was: STOPPED");
        }
    }

    @Nested
    @DisplayName("继承关系测试")
    class InheritanceTests {

        @Test
        @DisplayName("是 IllegalStateException 的子类")
        void testExtendsIllegalStateException() {
            OpenIllegalStateException ex = new OpenIllegalStateException("Test");
            assertThat(ex).isInstanceOf(IllegalStateException.class);
        }
    }
}
