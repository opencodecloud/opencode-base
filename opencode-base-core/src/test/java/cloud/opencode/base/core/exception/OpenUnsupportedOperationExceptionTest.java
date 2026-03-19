package cloud.opencode.base.core.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenUnsupportedOperationException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("OpenUnsupportedOperationException 测试")
class OpenUnsupportedOperationExceptionTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("仅消息构造")
        void testMessageConstructor() {
            OpenUnsupportedOperationException ex = new OpenUnsupportedOperationException("Not supported");

            assertThat(ex.getMessage()).isEqualTo("[Core] (UNSUPPORTED_OPERATION) Not supported");
            assertThat(ex.getComponent()).isEqualTo("Core");
            assertThat(ex.getErrorCode()).isEqualTo("UNSUPPORTED_OPERATION");
        }

        @Test
        @DisplayName("消息和原因构造")
        void testMessageAndCauseConstructor() {
            Exception cause = new RuntimeException("Original");
            OpenUnsupportedOperationException ex = new OpenUnsupportedOperationException("Not supported", cause);

            assertThat(ex.getMessage()).isEqualTo("[Core] (UNSUPPORTED_OPERATION) Not supported");
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("静态工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("immutable 创建不可变异常")
        void testImmutable() {
            OpenUnsupportedOperationException ex = OpenUnsupportedOperationException.immutable();

            assertThat(ex.getMessage()).contains("immutable");
        }

        @Test
        @DisplayName("readOnly 创建只读异常")
        void testReadOnly() {
            OpenUnsupportedOperationException ex = OpenUnsupportedOperationException.readOnly();

            assertThat(ex.getMessage()).contains("read-only");
        }

        @Test
        @DisplayName("notImplemented 创建未实现异常")
        void testNotImplemented() {
            OpenUnsupportedOperationException ex = OpenUnsupportedOperationException.notImplemented("saveToDatabase");

            assertThat(ex.getMessage()).contains("Method not implemented");
            assertThat(ex.getMessage()).contains("saveToDatabase");
        }

        @Test
        @DisplayName("unsupportedType 创建不支持类型异常")
        void testUnsupportedType() {
            OpenUnsupportedOperationException ex = OpenUnsupportedOperationException.unsupportedType(String.class);

            assertThat(ex.getMessage()).contains("Unsupported type");
            assertThat(ex.getMessage()).contains("java.lang.String");
        }

        @Test
        @DisplayName("unsupported 创建不支持操作异常")
        void testUnsupported() {
            OpenUnsupportedOperationException ex = OpenUnsupportedOperationException.unsupported("delete");

            assertThat(ex.getMessage()).contains("Unsupported operation");
            assertThat(ex.getMessage()).contains("delete");
        }
    }

    @Nested
    @DisplayName("继承关系测试")
    class InheritanceTests {

        @Test
        @DisplayName("是 OpenException 的子类")
        void testExtendsOpenException() {
            OpenUnsupportedOperationException ex = new OpenUnsupportedOperationException("Test");
            assertThat(ex).isInstanceOf(OpenException.class);
        }
    }
}
