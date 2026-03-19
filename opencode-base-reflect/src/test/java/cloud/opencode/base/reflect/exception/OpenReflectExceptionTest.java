package cloud.opencode.base.reflect.exception;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenReflectExceptionTest Tests
 * OpenReflectExceptionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("OpenReflectException 测试")
class OpenReflectExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用消息创建异常")
        void testConstructorWithMessage() {
            OpenReflectException exception = new OpenReflectException("test error");

            assertThat(exception.getMessage()).isEqualTo("[reflect] test error");
            assertThat(exception.targetType()).isNull();
            assertThat(exception.memberName()).isNull();
            assertThat(exception.operation()).isNull();
        }

        @Test
        @DisplayName("使用消息和原因创建异常")
        void testConstructorWithMessageAndCause() {
            RuntimeException cause = new RuntimeException("cause");
            OpenReflectException exception = new OpenReflectException("test error", cause);

            assertThat(exception.getMessage()).isEqualTo("[reflect] test error");
            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.targetType()).isNull();
        }

        @Test
        @DisplayName("使用目标、成员、操作和消息创建异常")
        void testConstructorWithTargetMemberOperationMessage() {
            OpenReflectException exception = new OpenReflectException(
                    String.class, "length", "invoke", "Method error");

            assertThat(exception.getMessage()).isEqualTo("[reflect] Method error");
            assertThat(exception.targetType()).isEqualTo(String.class);
            assertThat(exception.memberName()).isEqualTo("length");
            assertThat(exception.operation()).isEqualTo("invoke");
        }

        @Test
        @DisplayName("使用所有参数创建异常")
        void testConstructorWithAllParams() {
            RuntimeException cause = new RuntimeException("cause");
            OpenReflectException exception = new OpenReflectException(
                    String.class, "length", "invoke", "Method error", cause);

            assertThat(exception.getMessage()).isEqualTo("[reflect] Method error");
            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.targetType()).isEqualTo(String.class);
            assertThat(exception.memberName()).isEqualTo("length");
            assertThat(exception.operation()).isEqualTo("invoke");
        }
    }

    @Nested
    @DisplayName("fieldNotFound工厂方法测试")
    class FieldNotFoundTests {

        @Test
        @DisplayName("创建字段未找到异常")
        void testFieldNotFound() {
            OpenReflectException exception = OpenReflectException.fieldNotFound(String.class, "name");

            assertThat(exception.targetType()).isEqualTo(String.class);
            assertThat(exception.memberName()).isEqualTo("name");
            assertThat(exception.operation()).isEqualTo("getField");
            assertThat(exception.getMessage()).contains("Field 'name' not found");
            assertThat(exception.getMessage()).contains("java.lang.String");
        }
    }

    @Nested
    @DisplayName("fieldAccessFailed工厂方法测试")
    class FieldAccessFailedTests {

        @Test
        @DisplayName("创建字段访问失败异常")
        void testFieldAccessFailed() {
            IllegalAccessException cause = new IllegalAccessException("access denied");
            OpenReflectException exception = OpenReflectException.fieldAccessFailed(String.class, "value", cause);

            assertThat(exception.targetType()).isEqualTo(String.class);
            assertThat(exception.memberName()).isEqualTo("value");
            assertThat(exception.operation()).isEqualTo("accessField");
            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.getMessage()).contains("Failed to access field 'value'");
        }
    }

    @Nested
    @DisplayName("methodNotFound工厂方法测试")
    class MethodNotFoundTests {

        @Test
        @DisplayName("创建方法未找到异常（无参数）")
        void testMethodNotFoundNoParams() {
            OpenReflectException exception = OpenReflectException.methodNotFound(
                    String.class, "getValue", new Class<?>[0]);

            assertThat(exception.targetType()).isEqualTo(String.class);
            assertThat(exception.memberName()).isEqualTo("getValue");
            assertThat(exception.operation()).isEqualTo("getMethod");
            assertThat(exception.getMessage()).contains("Method 'getValue()'");
        }

        @Test
        @DisplayName("创建方法未找到异常（有参数）")
        void testMethodNotFoundWithParams() {
            OpenReflectException exception = OpenReflectException.methodNotFound(
                    String.class, "setValue", new Class<?>[]{String.class, int.class});

            assertThat(exception.getMessage()).contains("Method 'setValue(String, int)'");
        }

        @Test
        @DisplayName("创建方法未找到异常（null参数）")
        void testMethodNotFoundNullParams() {
            OpenReflectException exception = OpenReflectException.methodNotFound(
                    String.class, "getValue", null);

            assertThat(exception.getMessage()).contains("Method 'getValue()'");
        }
    }

    @Nested
    @DisplayName("methodInvokeFailed工厂方法测试")
    class MethodInvokeFailedTests {

        @Test
        @DisplayName("创建方法调用失败异常")
        void testMethodInvokeFailed() {
            RuntimeException cause = new RuntimeException("invoke error");
            OpenReflectException exception = OpenReflectException.methodInvokeFailed(
                    String.class, "length", cause);

            assertThat(exception.targetType()).isEqualTo(String.class);
            assertThat(exception.memberName()).isEqualTo("length");
            assertThat(exception.operation()).isEqualTo("invokeMethod");
            assertThat(exception.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("constructorNotFound工厂方法测试")
    class ConstructorNotFoundTests {

        @Test
        @DisplayName("创建构造器未找到异常（无参数）")
        void testConstructorNotFoundNoParams() {
            OpenReflectException exception = OpenReflectException.constructorNotFound(
                    String.class, new Class<?>[0]);

            assertThat(exception.targetType()).isEqualTo(String.class);
            assertThat(exception.memberName()).isEqualTo("<init>");
            assertThat(exception.operation()).isEqualTo("getConstructor");
            assertThat(exception.getMessage()).contains("Constructor()");
        }

        @Test
        @DisplayName("创建构造器未找到异常（有参数）")
        void testConstructorNotFoundWithParams() {
            OpenReflectException exception = OpenReflectException.constructorNotFound(
                    String.class, new Class<?>[]{char[].class});

            assertThat(exception.getMessage()).contains("Constructor(char[])");
        }
    }

    @Nested
    @DisplayName("instantiationFailed工厂方法测试")
    class InstantiationFailedTests {

        @Test
        @DisplayName("创建实例化失败异常")
        void testInstantiationFailed() {
            InstantiationException cause = new InstantiationException("cannot create");
            OpenReflectException exception = OpenReflectException.instantiationFailed(String.class, cause);

            assertThat(exception.targetType()).isEqualTo(String.class);
            assertThat(exception.memberName()).isEqualTo("<init>");
            assertThat(exception.operation()).isEqualTo("newInstance");
            assertThat(exception.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("classNotFound工厂方法测试")
    class ClassNotFoundTests {

        @Test
        @DisplayName("创建类未找到异常")
        void testClassNotFound() {
            OpenReflectException exception = OpenReflectException.classNotFound("com.example.NonExistent");

            assertThat(exception.targetType()).isNull();
            assertThat(exception.operation()).isEqualTo("forName");
            assertThat(exception.getMessage()).contains("Class not found: com.example.NonExistent");
        }
    }

    @Nested
    @DisplayName("classLoadFailed工厂方法测试")
    class ClassLoadFailedTests {

        @Test
        @DisplayName("创建类加载失败异常")
        void testClassLoadFailed() {
            ClassNotFoundException cause = new ClassNotFoundException("not found");
            OpenReflectException exception = OpenReflectException.classLoadFailed("com.example.Failed", cause);

            assertThat(exception.operation()).isEqualTo("forName");
            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.getMessage()).contains("Failed to load class: com.example.Failed");
        }
    }

    @Nested
    @DisplayName("typeCastFailed工厂方法测试")
    class TypeCastFailedTests {

        @Test
        @DisplayName("创建类型转换失败异常")
        void testTypeCastFailed() {
            OpenReflectException exception = OpenReflectException.typeCastFailed(Integer.class, "string");

            assertThat(exception.targetType()).isEqualTo(Integer.class);
            assertThat(exception.operation()).isEqualTo("cast");
            assertThat(exception.getMessage()).contains("Cannot cast java.lang.String to java.lang.Integer");
        }

        @Test
        @DisplayName("创建类型转换失败异常（null值）")
        void testTypeCastFailedNullValue() {
            OpenReflectException exception = OpenReflectException.typeCastFailed(Integer.class, null);

            assertThat(exception.getMessage()).contains("Cannot cast null to java.lang.Integer");
        }
    }

    @Nested
    @DisplayName("copyFailed工厂方法测试")
    class CopyFailedTests {

        @Test
        @DisplayName("创建复制失败异常")
        void testCopyFailed() {
            RuntimeException cause = new RuntimeException("copy error");
            OpenReflectException exception = OpenReflectException.copyFailed(String.class, Integer.class, cause);

            assertThat(exception.targetType()).isEqualTo(Integer.class);
            assertThat(exception.operation()).isEqualTo("copy");
            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.getMessage()).contains("Failed to copy from java.lang.String to java.lang.Integer");
        }
    }

    @Nested
    @DisplayName("proxyCreationFailed工厂方法测试")
    class ProxyCreationFailedTests {

        @Test
        @DisplayName("创建代理创建失败异常")
        void testProxyCreationFailed() {
            RuntimeException cause = new RuntimeException("proxy error");
            OpenReflectException exception = OpenReflectException.proxyCreationFailed(Runnable.class, cause);

            assertThat(exception.targetType()).isEqualTo(Runnable.class);
            assertThat(exception.operation()).isEqualTo("createProxy");
            assertThat(exception.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("annotationNotFound工厂方法测试")
    class AnnotationNotFoundTests {

        @Test
        @DisplayName("创建注解未找到异常")
        void testAnnotationNotFound() {
            OpenReflectException exception = OpenReflectException.annotationNotFound(
                    String.class, Deprecated.class);

            assertThat(exception.targetType()).isEqualTo(String.class);
            assertThat(exception.memberName()).isEqualTo("Deprecated");
            assertThat(exception.operation()).isEqualTo("getAnnotation");
            assertThat(exception.getMessage()).contains("Annotation @Deprecated not found on java.lang.String");
        }
    }

    @Nested
    @DisplayName("lambdaParseFailed工厂方法测试")
    class LambdaParseFailedTests {

        @Test
        @DisplayName("创建Lambda解析失败异常")
        void testLambdaParseFailed() {
            OpenReflectException exception = OpenReflectException.lambdaParseFailed("not serializable");

            assertThat(exception.operation()).isEqualTo("parseLambda");
            assertThat(exception.getMessage()).contains("Failed to parse lambda: not serializable");
        }
    }

    @Nested
    @DisplayName("recordOperationFailed工厂方法测试")
    class RecordOperationFailedTests {

        @Test
        @DisplayName("创建Record操作失败异常")
        void testRecordOperationFailed() {
            RuntimeException cause = new RuntimeException("record error");
            OpenReflectException exception = OpenReflectException.recordOperationFailed(
                    String.class, "getComponents", cause);

            assertThat(exception.targetType()).isEqualTo(String.class);
            assertThat(exception.operation()).isEqualTo("getComponents");
            assertThat(exception.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("illegalAccess工厂方法测试")
    class IllegalAccessTests {

        @Test
        @DisplayName("创建非法访问异常")
        void testIllegalAccess() {
            IllegalAccessException cause = new IllegalAccessException("private access");
            OpenReflectException exception = OpenReflectException.illegalAccess(
                    String.class, "value", cause);

            assertThat(exception.targetType()).isEqualTo(String.class);
            assertThat(exception.memberName()).isEqualTo("value");
            assertThat(exception.operation()).isEqualTo("access");
            assertThat(exception.getCause()).isEqualTo(cause);
        }
    }
}
