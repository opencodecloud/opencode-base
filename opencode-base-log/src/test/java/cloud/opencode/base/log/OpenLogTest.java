package cloud.opencode.base.log;

import cloud.opencode.base.log.marker.Marker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenLog 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
@DisplayName("OpenLog 测试")
class OpenLogTest {

    @Nested
    @DisplayName("类定义测试")
    class ClassDefinitionTests {

        @Test
        @DisplayName("类是final的")
        void testIsFinal() {
            assertThat(java.lang.reflect.Modifier.isFinal(OpenLog.class.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws NoSuchMethodException {
            var constructor = OpenLog.class.getDeclaredConstructor();
            assertThat(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("get方法测试")
    class GetMethodTests {

        @Test
        @DisplayName("get()返回Logger")
        void testGet() {
            Logger logger = OpenLog.get();
            assertThat(logger).isNotNull();
        }

        @Test
        @DisplayName("get(Class)返回Logger")
        void testGetByClass() {
            Logger logger = OpenLog.get(OpenLogTest.class);
            assertThat(logger).isNotNull();
            assertThat(logger.getName()).contains("OpenLogTest");
        }

        @Test
        @DisplayName("get(String)返回Logger")
        void testGetByName() {
            Logger logger = OpenLog.get("TestLogger");
            assertThat(logger).isNotNull();
            assertThat(logger.getName()).isEqualTo("TestLogger");
        }
    }

    @Nested
    @DisplayName("TRACE级别方法测试")
    class TraceMethodTests {

        @Test
        @DisplayName("trace(String)方法存在")
        void testTraceString() throws NoSuchMethodException {
            var method = OpenLog.class.getMethod("trace", String.class);
            assertThat(java.lang.reflect.Modifier.isStatic(method.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("trace(String, Object...)方法存在")
        void testTraceFormat() throws NoSuchMethodException {
            var method = OpenLog.class.getMethod("trace", String.class, Object[].class);
            assertThat(java.lang.reflect.Modifier.isStatic(method.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("trace(Supplier)方法存在")
        void testTraceSupplier() throws NoSuchMethodException {
            var method = OpenLog.class.getMethod("trace", Supplier.class);
            assertThat(java.lang.reflect.Modifier.isStatic(method.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("DEBUG级别方法测试")
    class DebugMethodTests {

        @Test
        @DisplayName("debug(String)方法存在")
        void testDebugString() throws NoSuchMethodException {
            var method = OpenLog.class.getMethod("debug", String.class);
            assertThat(java.lang.reflect.Modifier.isStatic(method.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("debug(String, Object...)方法存在")
        void testDebugFormat() throws NoSuchMethodException {
            var method = OpenLog.class.getMethod("debug", String.class, Object[].class);
            assertThat(java.lang.reflect.Modifier.isStatic(method.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("debug(Supplier)方法存在")
        void testDebugSupplier() throws NoSuchMethodException {
            var method = OpenLog.class.getMethod("debug", Supplier.class);
            assertThat(java.lang.reflect.Modifier.isStatic(method.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("INFO级别方法测试")
    class InfoMethodTests {

        @Test
        @DisplayName("info(String)方法存在")
        void testInfoString() throws NoSuchMethodException {
            var method = OpenLog.class.getMethod("info", String.class);
            assertThat(java.lang.reflect.Modifier.isStatic(method.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("info(String, Object...)方法存在")
        void testInfoFormat() throws NoSuchMethodException {
            var method = OpenLog.class.getMethod("info", String.class, Object[].class);
            assertThat(java.lang.reflect.Modifier.isStatic(method.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("info(Supplier)方法存在")
        void testInfoSupplier() throws NoSuchMethodException {
            var method = OpenLog.class.getMethod("info", Supplier.class);
            assertThat(java.lang.reflect.Modifier.isStatic(method.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("info(Marker, String)方法存在")
        void testInfoMarker() throws NoSuchMethodException {
            var method = OpenLog.class.getMethod("info", Marker.class, String.class);
            assertThat(java.lang.reflect.Modifier.isStatic(method.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("WARN级别方法测试")
    class WarnMethodTests {

        @Test
        @DisplayName("warn(String)方法存在")
        void testWarnString() throws NoSuchMethodException {
            var method = OpenLog.class.getMethod("warn", String.class);
            assertThat(java.lang.reflect.Modifier.isStatic(method.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("warn(String, Object...)方法存在")
        void testWarnFormat() throws NoSuchMethodException {
            var method = OpenLog.class.getMethod("warn", String.class, Object[].class);
            assertThat(java.lang.reflect.Modifier.isStatic(method.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("warn(String, Throwable)方法存在")
        void testWarnException() throws NoSuchMethodException {
            var method = OpenLog.class.getMethod("warn", String.class, Throwable.class);
            assertThat(java.lang.reflect.Modifier.isStatic(method.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("warn(Supplier)方法存在")
        void testWarnSupplier() throws NoSuchMethodException {
            var method = OpenLog.class.getMethod("warn", Supplier.class);
            assertThat(java.lang.reflect.Modifier.isStatic(method.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("ERROR级别方法测试")
    class ErrorMethodTests {

        @Test
        @DisplayName("error(String)方法存在")
        void testErrorString() throws NoSuchMethodException {
            var method = OpenLog.class.getMethod("error", String.class);
            assertThat(java.lang.reflect.Modifier.isStatic(method.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("error(String, Object...)方法存在")
        void testErrorFormat() throws NoSuchMethodException {
            var method = OpenLog.class.getMethod("error", String.class, Object[].class);
            assertThat(java.lang.reflect.Modifier.isStatic(method.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("error(String, Throwable)方法存在")
        void testErrorException() throws NoSuchMethodException {
            var method = OpenLog.class.getMethod("error", String.class, Throwable.class);
            assertThat(java.lang.reflect.Modifier.isStatic(method.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("error(Throwable)方法存在")
        void testErrorThrowable() throws NoSuchMethodException {
            var method = OpenLog.class.getMethod("error", Throwable.class);
            assertThat(java.lang.reflect.Modifier.isStatic(method.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("error(Supplier, Throwable)方法存在")
        void testErrorSupplierThrowable() throws NoSuchMethodException {
            var method = OpenLog.class.getMethod("error", Supplier.class, Throwable.class);
            assertThat(java.lang.reflect.Modifier.isStatic(method.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("error(Marker, String, Throwable)方法存在")
        void testErrorMarker() throws NoSuchMethodException {
            var method = OpenLog.class.getMethod("error", Marker.class, String.class, Throwable.class);
            assertThat(java.lang.reflect.Modifier.isStatic(method.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("级别检查方法测试")
    class LevelCheckMethodTests {

        @Test
        @DisplayName("isTraceEnabled方法存在")
        void testIsTraceEnabled() throws NoSuchMethodException {
            var method = OpenLog.class.getMethod("isTraceEnabled");
            assertThat(java.lang.reflect.Modifier.isStatic(method.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("isDebugEnabled方法存在")
        void testIsDebugEnabled() throws NoSuchMethodException {
            var method = OpenLog.class.getMethod("isDebugEnabled");
            assertThat(java.lang.reflect.Modifier.isStatic(method.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("isInfoEnabled方法存在")
        void testIsInfoEnabled() throws NoSuchMethodException {
            var method = OpenLog.class.getMethod("isInfoEnabled");
            assertThat(java.lang.reflect.Modifier.isStatic(method.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("isWarnEnabled方法存在")
        void testIsWarnEnabled() throws NoSuchMethodException {
            var method = OpenLog.class.getMethod("isWarnEnabled");
            assertThat(java.lang.reflect.Modifier.isStatic(method.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("isErrorEnabled方法存在")
        void testIsErrorEnabled() throws NoSuchMethodException {
            var method = OpenLog.class.getMethod("isErrorEnabled");
            assertThat(java.lang.reflect.Modifier.isStatic(method.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("实际调用测试")
    class ActualInvocationTests {

        @Test
        @DisplayName("可以调用trace方法")
        void testTraceInvocation() {
            assertThatCode(() -> OpenLog.trace("test message"))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("可以调用debug方法")
        void testDebugInvocation() {
            assertThatCode(() -> OpenLog.debug("test message"))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("可以调用info方法")
        void testInfoInvocation() {
            assertThatCode(() -> OpenLog.info("test message"))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("可以调用warn方法")
        void testWarnInvocation() {
            assertThatCode(() -> OpenLog.warn("test message"))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("可以调用error方法")
        void testErrorInvocation() {
            assertThatCode(() -> OpenLog.error("test message"))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("可以调用error带异常")
        void testErrorWithException() {
            assertThatCode(() -> OpenLog.error("error occurred", new RuntimeException("test")))
                .doesNotThrowAnyException();
        }
    }
}
