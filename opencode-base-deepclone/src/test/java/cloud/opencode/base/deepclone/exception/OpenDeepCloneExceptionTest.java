package cloud.opencode.base.deepclone.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenDeepCloneException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
@DisplayName("OpenDeepCloneException 测试")
class OpenDeepCloneExceptionTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("创建带消息的异常")
        void testMessageConstructor() {
            OpenDeepCloneException exception = new OpenDeepCloneException("Test message");

            assertThat(exception.getMessage()).contains("Test message");
            assertThat(exception.getMessage()).contains("[deepclone]");
            assertThat(exception.getTargetType()).isNull();
            assertThat(exception.getPath()).isNull();
        }

        @Test
        @DisplayName("创建带消息和原因的异常")
        void testMessageCauseConstructor() {
            Throwable cause = new RuntimeException("Cause");
            OpenDeepCloneException exception = new OpenDeepCloneException("Test message", cause);

            assertThat(exception.getMessage()).contains("Test message");
            assertThat(exception.getMessage()).contains("[deepclone]");
            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.getTargetType()).isNull();
            assertThat(exception.getPath()).isNull();
        }

        @Test
        @DisplayName("创建带类型、路径和消息的异常")
        void testTypePathMessageConstructor() {
            OpenDeepCloneException exception = new OpenDeepCloneException(
                    String.class, "a.b.c", "Test message");

            assertThat(exception.getMessage()).contains("Test message");
            assertThat(exception.getMessage()).contains("[deepclone]");
            assertThat(exception.getTargetType()).isEqualTo(String.class);
            assertThat(exception.getPath()).isEqualTo("a.b.c");
        }

        @Test
        @DisplayName("创建带类型、路径、消息和原因的异常")
        void testFullConstructor() {
            Throwable cause = new RuntimeException("Cause");
            OpenDeepCloneException exception = new OpenDeepCloneException(
                    String.class, "a.b.c", "Test message", cause);

            assertThat(exception.getMessage()).contains("Test message");
            assertThat(exception.getMessage()).contains("[deepclone]");
            assertThat(exception.getTargetType()).isEqualTo(String.class);
            assertThat(exception.getPath()).isEqualTo("a.b.c");
            assertThat(exception.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("访问方法测试")
    class AccessorTests {

        @Test
        @DisplayName("getTargetType() 返回目标类型")
        void testGetTargetType() {
            OpenDeepCloneException exception = new OpenDeepCloneException(
                    Integer.class, null, "message");

            assertThat(exception.getTargetType()).isEqualTo(Integer.class);
        }

        @Test
        @DisplayName("getPath() 返回路径")
        void testGetPath() {
            OpenDeepCloneException exception = new OpenDeepCloneException(
                    null, "field1.field2", "message");

            assertThat(exception.getPath()).isEqualTo("field1.field2");
        }
    }

    @Nested
    @DisplayName("maxDepthExceeded() 工厂方法测试")
    class MaxDepthExceededTests {

        @Test
        @DisplayName("创建最大深度超出异常")
        void testMaxDepthExceeded() {
            OpenDeepCloneException exception = OpenDeepCloneException.maxDepthExceeded(100, "a.b.c");

            assertThat(exception.getMessage()).contains("Maximum clone depth exceeded");
            assertThat(exception.getMessage()).contains("100");
            assertThat(exception.getMessage()).contains("a.b.c");
            assertThat(exception.getPath()).isEqualTo("a.b.c");
        }
    }

    @Nested
    @DisplayName("unsupportedType() 工厂方法测试")
    class UnsupportedTypeTests {

        @Test
        @DisplayName("创建不支持类型异常")
        void testUnsupportedType() {
            OpenDeepCloneException exception = OpenDeepCloneException.unsupportedType(String.class);

            assertThat(exception.getMessage()).contains("Unsupported type");
            assertThat(exception.getMessage()).contains("java.lang.String");
            assertThat(exception.getTargetType()).isEqualTo(String.class);
        }
    }

    @Nested
    @DisplayName("instantiationFailed() 工厂方法测试")
    class InstantiationFailedTests {

        @Test
        @DisplayName("创建实例化失败异常")
        void testInstantiationFailed() {
            Throwable cause = new RuntimeException("Constructor error");
            OpenDeepCloneException exception = OpenDeepCloneException.instantiationFailed(
                    String.class, cause);

            assertThat(exception.getMessage()).contains("Failed to instantiate");
            assertThat(exception.getMessage()).contains("java.lang.String");
            assertThat(exception.getTargetType()).isEqualTo(String.class);
            assertThat(exception.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("fieldAccessFailed() 工厂方法测试")
    class FieldAccessFailedTests {

        @Test
        @DisplayName("创建字段访问失败异常")
        void testFieldAccessFailed() {
            Throwable cause = new IllegalAccessException("Access denied");
            OpenDeepCloneException exception = OpenDeepCloneException.fieldAccessFailed(
                    "myField", String.class, cause);

            assertThat(exception.getMessage()).contains("Failed to access field");
            assertThat(exception.getMessage()).contains("myField");
            assertThat(exception.getMessage()).contains("java.lang.String");
            assertThat(exception.getTargetType()).isEqualTo(String.class);
            assertThat(exception.getPath()).isEqualTo("myField");
            assertThat(exception.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("serializationFailed() 工厂方法测试")
    class SerializationFailedTests {

        @Test
        @DisplayName("创建序列化失败异常")
        void testSerializationFailed() {
            Throwable cause = new RuntimeException("IO error");
            OpenDeepCloneException exception = OpenDeepCloneException.serializationFailed(
                    String.class, cause);

            assertThat(exception.getMessage()).contains("Serialization failed");
            assertThat(exception.getMessage()).contains("java.lang.String");
            assertThat(exception.getTargetType()).isEqualTo(String.class);
            assertThat(exception.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("circularReference() 工厂方法测试")
    class CircularReferenceTests {

        @Test
        @DisplayName("创建循环引用异常")
        void testCircularReference() {
            OpenDeepCloneException exception = OpenDeepCloneException.circularReference(
                    String.class, "a.b.c");

            assertThat(exception.getMessage()).contains("Circular reference detected");
            assertThat(exception.getMessage()).contains("a.b.c");
            assertThat(exception.getMessage()).contains("java.lang.String");
            assertThat(exception.getTargetType()).isEqualTo(String.class);
            assertThat(exception.getPath()).isEqualTo("a.b.c");
        }
    }

    @Nested
    @DisplayName("继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("是RuntimeException子类")
        void testIsRuntimeException() {
            OpenDeepCloneException exception = new OpenDeepCloneException("test");

            assertThat(exception).isInstanceOf(RuntimeException.class);
        }
    }
}
