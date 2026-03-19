package cloud.opencode.base.log;

import cloud.opencode.base.log.marker.Marker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.*;

/**
 * Logger 接口测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
@DisplayName("Logger 接口测试")
class LoggerTest {

    @Nested
    @DisplayName("接口定义测试")
    class InterfaceDefinitionTests {

        @Test
        @DisplayName("Logger是接口")
        void testIsInterface() {
            assertThat(Logger.class.isInterface()).isTrue();
        }

        @Test
        @DisplayName("定义了getName方法")
        void testGetNameMethod() throws NoSuchMethodException {
            assertThat(Logger.class.getMethod("getName")).isNotNull();
        }

        @Test
        @DisplayName("定义了TRACE级别方法")
        void testTraceMethods() throws NoSuchMethodException {
            assertThat(Logger.class.getMethod("isTraceEnabled")).isNotNull();
            assertThat(Logger.class.getMethod("isTraceEnabled", Marker.class)).isNotNull();
            assertThat(Logger.class.getMethod("trace", String.class)).isNotNull();
            assertThat(Logger.class.getMethod("trace", String.class, Object[].class)).isNotNull();
            assertThat(Logger.class.getMethod("trace", String.class, Throwable.class)).isNotNull();
            assertThat(Logger.class.getMethod("trace", Supplier.class)).isNotNull();
            assertThat(Logger.class.getMethod("trace", Marker.class, String.class)).isNotNull();
            assertThat(Logger.class.getMethod("trace", Marker.class, String.class, Object[].class)).isNotNull();
        }

        @Test
        @DisplayName("定义了DEBUG级别方法")
        void testDebugMethods() throws NoSuchMethodException {
            assertThat(Logger.class.getMethod("isDebugEnabled")).isNotNull();
            assertThat(Logger.class.getMethod("isDebugEnabled", Marker.class)).isNotNull();
            assertThat(Logger.class.getMethod("debug", String.class)).isNotNull();
            assertThat(Logger.class.getMethod("debug", String.class, Object[].class)).isNotNull();
            assertThat(Logger.class.getMethod("debug", String.class, Throwable.class)).isNotNull();
            assertThat(Logger.class.getMethod("debug", Supplier.class)).isNotNull();
        }

        @Test
        @DisplayName("定义了INFO级别方法")
        void testInfoMethods() throws NoSuchMethodException {
            assertThat(Logger.class.getMethod("isInfoEnabled")).isNotNull();
            assertThat(Logger.class.getMethod("isInfoEnabled", Marker.class)).isNotNull();
            assertThat(Logger.class.getMethod("info", String.class)).isNotNull();
            assertThat(Logger.class.getMethod("info", String.class, Object[].class)).isNotNull();
            assertThat(Logger.class.getMethod("info", String.class, Throwable.class)).isNotNull();
            assertThat(Logger.class.getMethod("info", Supplier.class)).isNotNull();
        }

        @Test
        @DisplayName("定义了WARN级别方法")
        void testWarnMethods() throws NoSuchMethodException {
            assertThat(Logger.class.getMethod("isWarnEnabled")).isNotNull();
            assertThat(Logger.class.getMethod("isWarnEnabled", Marker.class)).isNotNull();
            assertThat(Logger.class.getMethod("warn", String.class)).isNotNull();
            assertThat(Logger.class.getMethod("warn", String.class, Object[].class)).isNotNull();
            assertThat(Logger.class.getMethod("warn", String.class, Throwable.class)).isNotNull();
            assertThat(Logger.class.getMethod("warn", Supplier.class)).isNotNull();
            assertThat(Logger.class.getMethod("warn", Marker.class, String.class, Throwable.class)).isNotNull();
        }

        @Test
        @DisplayName("定义了ERROR级别方法")
        void testErrorMethods() throws NoSuchMethodException {
            assertThat(Logger.class.getMethod("isErrorEnabled")).isNotNull();
            assertThat(Logger.class.getMethod("isErrorEnabled", Marker.class)).isNotNull();
            assertThat(Logger.class.getMethod("error", String.class)).isNotNull();
            assertThat(Logger.class.getMethod("error", String.class, Object[].class)).isNotNull();
            assertThat(Logger.class.getMethod("error", String.class, Throwable.class)).isNotNull();
            assertThat(Logger.class.getMethod("error", Throwable.class)).isNotNull();
            assertThat(Logger.class.getMethod("error", Supplier.class)).isNotNull();
            assertThat(Logger.class.getMethod("error", Supplier.class, Throwable.class)).isNotNull();
            assertThat(Logger.class.getMethod("error", Marker.class, String.class, Throwable.class)).isNotNull();
        }

        @Test
        @DisplayName("定义了通用方法")
        void testGenericMethods() throws NoSuchMethodException {
            assertThat(Logger.class.getMethod("isEnabled", LogLevel.class)).isNotNull();
            assertThat(Logger.class.getMethod("log", LogLevel.class, String.class)).isNotNull();
            assertThat(Logger.class.getMethod("log", LogLevel.class, String.class, Object[].class)).isNotNull();
            assertThat(Logger.class.getMethod("log", LogLevel.class, String.class, Throwable.class)).isNotNull();
        }
    }

    @Nested
    @DisplayName("Mock实现测试")
    class MockImplementationTests {

        private final TestLogger testLogger = new TestLogger("test");

        @Test
        @DisplayName("getName返回名称")
        void testGetName() {
            assertThat(testLogger.getName()).isEqualTo("test");
        }

        @Test
        @DisplayName("trace方法可调用")
        void testTraceMethods() {
            testLogger.trace("message");
            testLogger.trace("format {}", "arg");
            testLogger.trace("error", new RuntimeException());
            testLogger.trace(() -> "lazy message");

            assertThat(testLogger.getLogCount()).isEqualTo(4);
        }

        @Test
        @DisplayName("debug方法可调用")
        void testDebugMethods() {
            testLogger.debug("message");
            testLogger.debug("format {} {}", "arg1", "arg2");

            assertThat(testLogger.getLogCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("info方法可调用")
        void testInfoMethods() {
            testLogger.info("message");
            testLogger.info("format {}", "arg");

            assertThat(testLogger.getLogCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("warn方法可调用")
        void testWarnMethods() {
            testLogger.warn("message");
            testLogger.warn("message", new RuntimeException());

            assertThat(testLogger.getLogCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("error方法可调用")
        void testErrorMethods() {
            testLogger.error("message");
            testLogger.error("message", new RuntimeException());
            testLogger.error(new RuntimeException());
            testLogger.error(() -> "lazy", new RuntimeException());

            assertThat(testLogger.getLogCount()).isEqualTo(4);
        }
    }

    /**
     * 测试用Logger实现
     */
    private static class TestLogger implements Logger {
        private final String name;
        private int logCount = 0;

        TestLogger(String name) {
            this.name = name;
        }

        int getLogCount() {
            return logCount;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isTraceEnabled() {
            return true;
        }

        @Override
        public boolean isTraceEnabled(Marker marker) {
            return true;
        }

        @Override
        public void trace(String message) {
            logCount++;
        }

        @Override
        public void trace(String format, Object... args) {
            logCount++;
        }

        @Override
        public void trace(String message, Throwable throwable) {
            logCount++;
        }

        @Override
        public void trace(Supplier<String> messageSupplier) {
            logCount++;
        }

        @Override
        public void trace(Marker marker, String message) {
            logCount++;
        }

        @Override
        public void trace(Marker marker, String format, Object... args) {
            logCount++;
        }

        @Override
        public boolean isDebugEnabled() {
            return true;
        }

        @Override
        public boolean isDebugEnabled(Marker marker) {
            return true;
        }

        @Override
        public void debug(String message) {
            logCount++;
        }

        @Override
        public void debug(String format, Object... args) {
            logCount++;
        }

        @Override
        public void debug(String message, Throwable throwable) {
            logCount++;
        }

        @Override
        public void debug(Supplier<String> messageSupplier) {
            logCount++;
        }

        @Override
        public void debug(Marker marker, String message) {
            logCount++;
        }

        @Override
        public void debug(Marker marker, String format, Object... args) {
            logCount++;
        }

        @Override
        public boolean isInfoEnabled() {
            return true;
        }

        @Override
        public boolean isInfoEnabled(Marker marker) {
            return true;
        }

        @Override
        public void info(String message) {
            logCount++;
        }

        @Override
        public void info(String format, Object... args) {
            logCount++;
        }

        @Override
        public void info(String message, Throwable throwable) {
            logCount++;
        }

        @Override
        public void info(Supplier<String> messageSupplier) {
            logCount++;
        }

        @Override
        public void info(Marker marker, String message) {
            logCount++;
        }

        @Override
        public void info(Marker marker, String format, Object... args) {
            logCount++;
        }

        @Override
        public boolean isWarnEnabled() {
            return true;
        }

        @Override
        public boolean isWarnEnabled(Marker marker) {
            return true;
        }

        @Override
        public void warn(String message) {
            logCount++;
        }

        @Override
        public void warn(String format, Object... args) {
            logCount++;
        }

        @Override
        public void warn(String message, Throwable throwable) {
            logCount++;
        }

        @Override
        public void warn(Supplier<String> messageSupplier) {
            logCount++;
        }

        @Override
        public void warn(Marker marker, String message) {
            logCount++;
        }

        @Override
        public void warn(Marker marker, String format, Object... args) {
            logCount++;
        }

        @Override
        public void warn(Marker marker, String message, Throwable throwable) {
            logCount++;
        }

        @Override
        public boolean isErrorEnabled() {
            return true;
        }

        @Override
        public boolean isErrorEnabled(Marker marker) {
            return true;
        }

        @Override
        public void error(String message) {
            logCount++;
        }

        @Override
        public void error(String format, Object... args) {
            logCount++;
        }

        @Override
        public void error(String message, Throwable throwable) {
            logCount++;
        }

        @Override
        public void error(Throwable throwable) {
            logCount++;
        }

        @Override
        public void error(Supplier<String> messageSupplier) {
            logCount++;
        }

        @Override
        public void error(Supplier<String> messageSupplier, Throwable throwable) {
            logCount++;
        }

        @Override
        public void error(Marker marker, String message) {
            logCount++;
        }

        @Override
        public void error(Marker marker, String format, Object... args) {
            logCount++;
        }

        @Override
        public void error(Marker marker, String message, Throwable throwable) {
            logCount++;
        }

        @Override
        public boolean isEnabled(LogLevel level) {
            return true;
        }

        @Override
        public void log(LogLevel level, String message) {
            logCount++;
        }

        @Override
        public void log(LogLevel level, String format, Object... args) {
            logCount++;
        }

        @Override
        public void log(LogLevel level, String message, Throwable throwable) {
            logCount++;
        }
    }
}
