package cloud.opencode.base.id.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenIdGenerationException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
@DisplayName("OpenIdGenerationException 测试")
class OpenIdGenerationExceptionTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("带消息构造")
        void testMessageConstructor() {
            OpenIdGenerationException ex = new OpenIdGenerationException("Test message");

            // 消息会带有组件前缀 "[id]"
            assertThat(ex.getMessage()).contains("Test message");
        }

        @Test
        @DisplayName("带消息和原因构造")
        void testMessageAndCauseConstructor() {
            Throwable cause = new RuntimeException("Cause");
            OpenIdGenerationException ex = new OpenIdGenerationException("Test message", cause);

            // 消息会带有组件前缀 "[id]"
            assertThat(ex.getMessage()).contains("Test message");
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("clockBackward创建异常")
        void testClockBackward() {
            OpenIdGenerationException ex = OpenIdGenerationException.clockBackward(1000L, 900L);

            assertThat(ex).isNotNull();
            assertThat(ex.getMessage()).contains("Clock moved backward");
        }

        @Test
        @DisplayName("invalidIdFormat创建异常")
        void testInvalidIdFormat() {
            OpenIdGenerationException ex = OpenIdGenerationException.invalidIdFormat("ULID", "invalid");

            assertThat(ex).isNotNull();
            assertThat(ex.getMessage()).contains("Invalid").contains("ULID");
        }

        @Test
        @DisplayName("segmentExhausted创建异常")
        void testSegmentExhausted() {
            OpenIdGenerationException ex = OpenIdGenerationException.segmentExhausted("order");

            assertThat(ex).isNotNull();
            assertThat(ex.getMessage()).contains("exhausted").contains("order");
        }

        @Test
        @DisplayName("segmentAllocationFailed创建异常")
        void testSegmentAllocationFailed() {
            Throwable cause = new RuntimeException("DB error");
            OpenIdGenerationException ex = OpenIdGenerationException.segmentAllocationFailed("order", cause);

            assertThat(ex).isNotNull();
            assertThat(ex.getMessage()).contains("Failed").contains("order");
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("segmentAllocationFailed无cause")
        void testSegmentAllocationFailedNoCause() {
            OpenIdGenerationException ex = OpenIdGenerationException.segmentAllocationFailed("order", null);

            assertThat(ex).isNotNull();
            assertThat(ex.getMessage()).contains("Failed").contains("order");
        }
    }

    @Nested
    @DisplayName("异常层级测试")
    class HierarchyTests {

        @Test
        @DisplayName("继承RuntimeException")
        void testExtendsRuntimeException() {
            OpenIdGenerationException ex = new OpenIdGenerationException("test");

            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }
}
