package cloud.opencode.base.log.spi;

import cloud.opencode.base.log.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * LogProvider 接口测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
@DisplayName("LogProvider 接口测试")
class LogProviderTest {

    @Nested
    @DisplayName("接口定义测试")
    class InterfaceDefinitionTests {

        @Test
        @DisplayName("LogProvider是接口")
        void testIsInterface() {
            assertThat(LogProvider.class.isInterface()).isTrue();
        }

        @Test
        @DisplayName("定义了getName方法")
        void testGetNameMethod() throws NoSuchMethodException {
            assertThat(LogProvider.class.getMethod("getName")).isNotNull();
        }

        @Test
        @DisplayName("定义了getPriority默认方法")
        void testGetPriorityMethod() throws NoSuchMethodException {
            var method = LogProvider.class.getMethod("getPriority");
            assertThat(method).isNotNull();
            assertThat(method.isDefault()).isTrue();
        }

        @Test
        @DisplayName("定义了isAvailable方法")
        void testIsAvailableMethod() throws NoSuchMethodException {
            assertThat(LogProvider.class.getMethod("isAvailable")).isNotNull();
        }

        @Test
        @DisplayName("定义了getLogger(String)方法")
        void testGetLoggerStringMethod() throws NoSuchMethodException {
            assertThat(LogProvider.class.getMethod("getLogger", String.class)).isNotNull();
        }

        @Test
        @DisplayName("定义了getLogger(Class)默认方法")
        void testGetLoggerClassMethod() throws NoSuchMethodException {
            var method = LogProvider.class.getMethod("getLogger", Class.class);
            assertThat(method).isNotNull();
            assertThat(method.isDefault()).isTrue();
        }

        @Test
        @DisplayName("定义了getMDCAdapter方法")
        void testGetMDCAdapterMethod() throws NoSuchMethodException {
            assertThat(LogProvider.class.getMethod("getMDCAdapter")).isNotNull();
        }

        @Test
        @DisplayName("定义了getNDCAdapter默认方法")
        void testGetNDCAdapterMethod() throws NoSuchMethodException {
            var method = LogProvider.class.getMethod("getNDCAdapter");
            assertThat(method).isNotNull();
            assertThat(method.isDefault()).isTrue();
        }

        @Test
        @DisplayName("定义了getLogAdapter默认方法")
        void testGetLogAdapterMethod() throws NoSuchMethodException {
            var method = LogProvider.class.getMethod("getLogAdapter");
            assertThat(method).isNotNull();
            assertThat(method.isDefault()).isTrue();
        }

        @Test
        @DisplayName("定义了initialize默认方法")
        void testInitializeMethod() throws NoSuchMethodException {
            var method = LogProvider.class.getMethod("initialize");
            assertThat(method).isNotNull();
            assertThat(method.isDefault()).isTrue();
        }

        @Test
        @DisplayName("定义了shutdown默认方法")
        void testShutdownMethod() throws NoSuchMethodException {
            var method = LogProvider.class.getMethod("shutdown");
            assertThat(method).isNotNull();
            assertThat(method.isDefault()).isTrue();
        }
    }

    @Nested
    @DisplayName("默认方法测试")
    class DefaultMethodTests {

        private LogProvider createMinimalProvider() {
            return new LogProvider() {
                @Override
                public String getName() {
                    return "TEST";
                }

                @Override
                public boolean isAvailable() {
                    return true;
                }

                @Override
                public Logger getLogger(String name) {
                    return null;
                }

                @Override
                public MDCAdapter getMDCAdapter() {
                    return null;
                }
            };
        }

        @Test
        @DisplayName("getPriority默认返回100")
        void testGetPriorityDefault() {
            LogProvider provider = createMinimalProvider();
            assertThat(provider.getPriority()).isEqualTo(100);
        }

        @Test
        @DisplayName("getLogger(Class)委托给getLogger(String)")
        void testGetLoggerClass() {
            LogProvider provider = new LogProvider() {
                @Override
                public String getName() {
                    return "TEST";
                }

                @Override
                public boolean isAvailable() {
                    return true;
                }

                @Override
                public Logger getLogger(String name) {
                    return new TestLogger(name);
                }

                @Override
                public MDCAdapter getMDCAdapter() {
                    return null;
                }
            };

            Logger logger = provider.getLogger(LogProviderTest.class);
            assertThat(logger.getName()).isEqualTo(LogProviderTest.class.getName());
        }

        @Test
        @DisplayName("getNDCAdapter默认返回null")
        void testGetNDCAdapterDefault() {
            LogProvider provider = createMinimalProvider();
            assertThat(provider.getNDCAdapter()).isNull();
        }

        @Test
        @DisplayName("getLogAdapter默认返回null")
        void testGetLogAdapterDefault() {
            LogProvider provider = createMinimalProvider();
            assertThat(provider.getLogAdapter()).isNull();
        }

        @Test
        @DisplayName("initialize默认为空操作")
        void testInitializeDefault() {
            LogProvider provider = createMinimalProvider();
            assertThatCode(provider::initialize).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("shutdown默认为空操作")
        void testShutdownDefault() {
            LogProvider provider = createMinimalProvider();
            assertThatCode(provider::shutdown).doesNotThrowAnyException();
        }
    }

    // Test Logger implementation
    private static class TestLogger implements Logger {
        private final String name;

        TestLogger(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override public boolean isTraceEnabled() { return true; }
        @Override public boolean isTraceEnabled(cloud.opencode.base.log.marker.Marker marker) { return true; }
        @Override public void trace(String message) {}
        @Override public void trace(String format, Object... args) {}
        @Override public void trace(String message, Throwable t) {}
        @Override public void trace(java.util.function.Supplier<String> s) {}
        @Override public void trace(cloud.opencode.base.log.marker.Marker m, String message) {}
        @Override public void trace(cloud.opencode.base.log.marker.Marker m, String format, Object... args) {}

        @Override public boolean isDebugEnabled() { return true; }
        @Override public boolean isDebugEnabled(cloud.opencode.base.log.marker.Marker marker) { return true; }
        @Override public void debug(String message) {}
        @Override public void debug(String format, Object... args) {}
        @Override public void debug(String message, Throwable t) {}
        @Override public void debug(java.util.function.Supplier<String> s) {}
        @Override public void debug(cloud.opencode.base.log.marker.Marker m, String message) {}
        @Override public void debug(cloud.opencode.base.log.marker.Marker m, String format, Object... args) {}

        @Override public boolean isInfoEnabled() { return true; }
        @Override public boolean isInfoEnabled(cloud.opencode.base.log.marker.Marker marker) { return true; }
        @Override public void info(String message) {}
        @Override public void info(String format, Object... args) {}
        @Override public void info(String message, Throwable t) {}
        @Override public void info(java.util.function.Supplier<String> s) {}
        @Override public void info(cloud.opencode.base.log.marker.Marker m, String message) {}
        @Override public void info(cloud.opencode.base.log.marker.Marker m, String format, Object... args) {}

        @Override public boolean isWarnEnabled() { return true; }
        @Override public boolean isWarnEnabled(cloud.opencode.base.log.marker.Marker marker) { return true; }
        @Override public void warn(String message) {}
        @Override public void warn(String format, Object... args) {}
        @Override public void warn(String message, Throwable t) {}
        @Override public void warn(java.util.function.Supplier<String> s) {}
        @Override public void warn(cloud.opencode.base.log.marker.Marker m, String message) {}
        @Override public void warn(cloud.opencode.base.log.marker.Marker m, String format, Object... args) {}
        @Override public void warn(cloud.opencode.base.log.marker.Marker m, String message, Throwable t) {}

        @Override public boolean isErrorEnabled() { return true; }
        @Override public boolean isErrorEnabled(cloud.opencode.base.log.marker.Marker marker) { return true; }
        @Override public void error(String message) {}
        @Override public void error(String format, Object... args) {}
        @Override public void error(String message, Throwable t) {}
        @Override public void error(Throwable t) {}
        @Override public void error(java.util.function.Supplier<String> s) {}
        @Override public void error(java.util.function.Supplier<String> s, Throwable t) {}
        @Override public void error(cloud.opencode.base.log.marker.Marker m, String message) {}
        @Override public void error(cloud.opencode.base.log.marker.Marker m, String format, Object... args) {}
        @Override public void error(cloud.opencode.base.log.marker.Marker m, String message, Throwable t) {}

        @Override public boolean isEnabled(cloud.opencode.base.log.LogLevel level) { return true; }
        @Override public void log(cloud.opencode.base.log.LogLevel level, String message) {}
        @Override public void log(cloud.opencode.base.log.LogLevel level, String format, Object... args) {}
        @Override public void log(cloud.opencode.base.log.LogLevel level, String message, Throwable t) {}
    }
}
