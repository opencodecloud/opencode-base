package cloud.opencode.base.log.spi;

import cloud.opencode.base.log.Logger;
import cloud.opencode.base.log.LogLevel;
import cloud.opencode.base.log.marker.Markers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.*;

/**
 * DefaultLogProvider 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
@DisplayName("DefaultLogProvider 测试")
class DefaultLogProviderTest {

    private DefaultLogProvider provider;

    @BeforeEach
    void setUp() {
        provider = new DefaultLogProvider();
    }

    @Nested
    @DisplayName("LogProvider实现测试")
    class LogProviderImplementationTests {

        @Test
        @DisplayName("getName返回DEFAULT")
        void testGetName() {
            assertThat(provider.getName()).isEqualTo("DEFAULT");
        }

        @Test
        @DisplayName("getPriority返回Integer.MAX_VALUE")
        void testGetPriority() {
            assertThat(provider.getPriority()).isEqualTo(Integer.MAX_VALUE);
        }

        @Test
        @DisplayName("isAvailable总是返回true")
        void testIsAvailable() {
            assertThat(provider.isAvailable()).isTrue();
        }

        @Test
        @DisplayName("getLogger返回Logger实例")
        void testGetLogger() {
            Logger logger = provider.getLogger("test.logger");

            assertThat(logger).isNotNull();
            assertThat(logger.getName()).isEqualTo("test.logger");
        }

        @Test
        @DisplayName("getLogger缓存实例")
        void testGetLoggerCaches() {
            Logger logger1 = provider.getLogger("cached.logger");
            Logger logger2 = provider.getLogger("cached.logger");

            assertThat(logger1).isSameAs(logger2);
        }

        @Test
        @DisplayName("getMDCAdapter返回适配器")
        void testGetMDCAdapter() {
            MDCAdapter adapter = provider.getMDCAdapter();
            assertThat(adapter).isNotNull();
        }

        @Test
        @DisplayName("getNDCAdapter返回适配器")
        void testGetNDCAdapter() {
            NDCAdapter adapter = provider.getNDCAdapter();
            assertThat(adapter).isNotNull();
        }
    }

    @Nested
    @DisplayName("DefaultLogger测试")
    class DefaultLoggerTests {

        private Logger logger;
        private ByteArrayOutputStream outContent;
        private ByteArrayOutputStream errContent;
        private PrintStream originalOut;
        private PrintStream originalErr;

        @BeforeEach
        void setUp() {
            logger = provider.getLogger("test.DefaultLogger");
            outContent = new ByteArrayOutputStream();
            errContent = new ByteArrayOutputStream();
            originalOut = System.out;
            originalErr = System.err;
            System.setOut(new PrintStream(outContent));
            System.setErr(new PrintStream(errContent));
        }

        @AfterEach
        void tearDown() {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }

        @Test
        @DisplayName("getName返回logger名称")
        void testGetName() {
            assertThat(logger.getName()).isEqualTo("test.DefaultLogger");
        }

        @Test
        @DisplayName("isTraceEnabled")
        void testIsTraceEnabled() {
            assertThat(logger.isTraceEnabled()).isFalse();
        }

        @Test
        @DisplayName("isDebugEnabled")
        void testIsDebugEnabled() {
            assertThat(logger.isDebugEnabled()).isFalse();
        }

        @Test
        @DisplayName("isInfoEnabled")
        void testIsInfoEnabled() {
            assertThat(logger.isInfoEnabled()).isTrue();
        }

        @Test
        @DisplayName("isWarnEnabled")
        void testIsWarnEnabled() {
            assertThat(logger.isWarnEnabled()).isTrue();
        }

        @Test
        @DisplayName("isErrorEnabled")
        void testIsErrorEnabled() {
            assertThat(logger.isErrorEnabled()).isTrue();
        }

        @Test
        @DisplayName("info记录消息")
        void testInfo() {
            logger.info("Test message");
            assertThat(outContent.toString()).contains("Test message");
        }

        @Test
        @DisplayName("info格式化消息")
        void testInfoFormat() {
            logger.info("Hello {}", "World");
            assertThat(outContent.toString()).contains("Hello World");
        }

        @Test
        @DisplayName("warn记录到stderr")
        void testWarn() {
            logger.warn("Warning message");
            assertThat(errContent.toString()).contains("Warning message");
        }

        @Test
        @DisplayName("error记录到stderr")
        void testError() {
            logger.error("Error message");
            assertThat(errContent.toString()).contains("Error message");
        }

        @Test
        @DisplayName("error带异常")
        void testErrorWithThrowable() {
            logger.error("Error", new RuntimeException("Test exception"));
            String output = errContent.toString();
            assertThat(output).contains("Error");
            assertThat(output).contains("RuntimeException");
        }

        @Test
        @DisplayName("error(Throwable)")
        void testErrorThrowableOnly() {
            logger.error(new RuntimeException("Direct exception"));
            assertThat(errContent.toString()).contains("Direct exception");
        }

        @Test
        @DisplayName("trace不记录（级别不够）")
        void testTraceNotLogged() {
            logger.trace("Trace message");
            assertThat(outContent.toString()).isEmpty();
        }

        @Test
        @DisplayName("debug不记录（级别不够）")
        void testDebugNotLogged() {
            logger.debug("Debug message");
            assertThat(outContent.toString()).isEmpty();
        }

        @Test
        @DisplayName("isEnabled检查级别")
        void testIsEnabled() {
            assertThat(logger.isEnabled(LogLevel.TRACE)).isFalse();
            assertThat(logger.isEnabled(LogLevel.DEBUG)).isFalse();
            assertThat(logger.isEnabled(LogLevel.INFO)).isTrue();
            assertThat(logger.isEnabled(LogLevel.WARN)).isTrue();
            assertThat(logger.isEnabled(LogLevel.ERROR)).isTrue();
        }

        @Test
        @DisplayName("log通用方法")
        void testLog() {
            logger.log(LogLevel.INFO, "Log message");
            assertThat(outContent.toString()).contains("Log message");
        }

        @Test
        @DisplayName("log格式化")
        void testLogFormat() {
            logger.log(LogLevel.WARN, "Formatted {}", "message");
            assertThat(errContent.toString()).contains("Formatted message");
        }

        @Test
        @DisplayName("log带异常")
        void testLogWithThrowable() {
            logger.log(LogLevel.ERROR, "With exception", new RuntimeException("Test"));
            assertThat(errContent.toString()).contains("RuntimeException");
        }

        @Test
        @DisplayName("带Marker的方法")
        void testWithMarker() {
            logger.info(Markers.SECURITY, "Security event");
            assertThat(outContent.toString()).contains("Security event");
        }

        @Test
        @DisplayName("带Marker格式化的方法")
        void testWithMarkerFormat() {
            logger.info(Markers.AUDIT, "User {} logged in", "admin");
            assertThat(outContent.toString()).contains("User admin logged in");
        }

        @Test
        @DisplayName("Supplier方法")
        void testInfoSupplier() {
            logger.info(() -> "Lazy message");
            assertThat(outContent.toString()).contains("Lazy message");
        }

        @Test
        @DisplayName("Supplier不计算当级别不够")
        void testSupplierNotEvaluated() {
            final boolean[] evaluated = {false};
            logger.trace(() -> {
                evaluated[0] = true;
                return "Should not evaluate";
            });
            assertThat(evaluated[0]).isFalse();
        }

        @Test
        @DisplayName("error Supplier带异常")
        void testErrorSupplierWithThrowable() {
            logger.error(() -> "Supplier error", new RuntimeException("Test"));
            assertThat(errContent.toString()).contains("Supplier error");
        }

        @Test
        @DisplayName("格式化处理多余占位符")
        void testFormatExtraPlaceholders() {
            logger.info("One {} two {}", "arg");
            assertThat(outContent.toString()).contains("One arg two {}");
        }

        @Test
        @DisplayName("格式化无参数")
        void testFormatNoArgs() {
            logger.info("No args here");
            assertThat(outContent.toString()).contains("No args here");
        }

        @Test
        @DisplayName("isTraceEnabled(Marker)")
        void testIsTraceEnabledMarker() {
            assertThat(logger.isTraceEnabled(Markers.SECURITY)).isFalse();
        }

        @Test
        @DisplayName("isDebugEnabled(Marker)")
        void testIsDebugEnabledMarker() {
            assertThat(logger.isDebugEnabled(Markers.SECURITY)).isFalse();
        }

        @Test
        @DisplayName("isInfoEnabled(Marker)")
        void testIsInfoEnabledMarker() {
            assertThat(logger.isInfoEnabled(Markers.SECURITY)).isTrue();
        }

        @Test
        @DisplayName("isWarnEnabled(Marker)")
        void testIsWarnEnabledMarker() {
            assertThat(logger.isWarnEnabled(Markers.SECURITY)).isTrue();
        }

        @Test
        @DisplayName("isErrorEnabled(Marker)")
        void testIsErrorEnabledMarker() {
            assertThat(logger.isErrorEnabled(Markers.SECURITY)).isTrue();
        }

        @Test
        @DisplayName("trace带Marker")
        void testTraceMarker() {
            logger.trace(Markers.PERFORMANCE, "Trace perf");
            assertThat(outContent.toString()).isEmpty();
        }

        @Test
        @DisplayName("trace带Marker和格式")
        void testTraceMarkerFormat() {
            logger.trace(Markers.PERFORMANCE, "Trace {}", "perf");
            assertThat(outContent.toString()).isEmpty();
        }

        @Test
        @DisplayName("debug带Marker")
        void testDebugMarker() {
            logger.debug(Markers.DATABASE, "Debug db");
            assertThat(outContent.toString()).isEmpty();
        }

        @Test
        @DisplayName("debug带Marker和格式")
        void testDebugMarkerFormat() {
            logger.debug(Markers.DATABASE, "Debug {}", "db");
            assertThat(outContent.toString()).isEmpty();
        }

        @Test
        @DisplayName("warn带Marker")
        void testWarnMarker() {
            logger.warn(Markers.NETWORK, "Warn network");
            assertThat(errContent.toString()).contains("Warn network");
        }

        @Test
        @DisplayName("warn带Marker和格式")
        void testWarnMarkerFormat() {
            logger.warn(Markers.NETWORK, "Warn {} issue", "network");
            assertThat(errContent.toString()).contains("Warn network issue");
        }

        @Test
        @DisplayName("warn带Marker和异常")
        void testWarnMarkerThrowable() {
            logger.warn(Markers.NETWORK, "Warn with exception", new RuntimeException("Test"));
            assertThat(errContent.toString()).contains("RuntimeException");
        }

        @Test
        @DisplayName("error带Marker")
        void testErrorMarker() {
            logger.error(Markers.SYSTEM, "Error system");
            assertThat(errContent.toString()).contains("Error system");
        }

        @Test
        @DisplayName("error带Marker和格式")
        void testErrorMarkerFormat() {
            logger.error(Markers.SYSTEM, "Error {} event", "system");
            assertThat(errContent.toString()).contains("Error system event");
        }

        @Test
        @DisplayName("error带Marker和异常")
        void testErrorMarkerThrowable() {
            logger.error(Markers.SYSTEM, "Error with throwable", new RuntimeException("Test"));
            assertThat(errContent.toString()).contains("RuntimeException");
        }

        @Test
        @DisplayName("trace带异常")
        void testTraceThrowable() {
            logger.trace("Trace exception", new RuntimeException("Test"));
            assertThat(outContent.toString()).isEmpty();
        }

        @Test
        @DisplayName("debug带异常")
        void testDebugThrowable() {
            logger.debug("Debug exception", new RuntimeException("Test"));
            assertThat(outContent.toString()).isEmpty();
        }

        @Test
        @DisplayName("info带异常")
        void testInfoThrowable() {
            logger.info("Info exception", new RuntimeException("Test"));
            assertThat(outContent.toString()).contains("RuntimeException");
        }

        @Test
        @DisplayName("短名称提取")
        void testShortName() {
            Logger shortLogger = provider.getLogger("com.example.MyClass");
            shortLogger.info("Test");
            assertThat(outContent.toString()).contains("MyClass");
        }

        @Test
        @DisplayName("无包名的短名称")
        void testShortNameNoPackage() {
            Logger simpleLogger = provider.getLogger("SimpleClass");
            simpleLogger.info("Test");
            assertThat(outContent.toString()).contains("SimpleClass");
        }
    }

    @Nested
    @DisplayName("DefaultMDCAdapter测试")
    class DefaultMDCAdapterTests {

        private MDCAdapter adapter;

        @BeforeEach
        void setUp() {
            adapter = provider.getMDCAdapter();
            adapter.clear();
        }

        @AfterEach
        void tearDown() {
            adapter.clear();
        }

        @Test
        @DisplayName("put和get")
        void testPutGet() {
            adapter.put("key", "value");
            assertThat(adapter.get("key")).isEqualTo("value");
        }

        @Test
        @DisplayName("remove")
        void testRemove() {
            adapter.put("key", "value");
            adapter.remove("key");
            assertThat(adapter.get("key")).isNull();
        }

        @Test
        @DisplayName("clear")
        void testClear() {
            adapter.put("key1", "value1");
            adapter.put("key2", "value2");
            adapter.clear();

            assertThat(adapter.get("key1")).isNull();
            assertThat(adapter.get("key2")).isNull();
        }

        @Test
        @DisplayName("getCopyOfContextMap")
        void testGetCopyOfContextMap() {
            adapter.put("key", "value");
            var copy = adapter.getCopyOfContextMap();

            assertThat(copy).containsEntry("key", "value");
            copy.put("newKey", "newValue");
            assertThat(adapter.get("newKey")).isNull();
        }

        @Test
        @DisplayName("setContextMap")
        void testSetContextMap() {
            adapter.put("old", "value");
            adapter.setContextMap(java.util.Map.of("new", "value"));

            assertThat(adapter.get("old")).isNull();
            assertThat(adapter.get("new")).isEqualTo("value");
        }

        @Test
        @DisplayName("setContextMap(null)")
        void testSetContextMapNull() {
            adapter.put("key", "value");
            adapter.setContextMap(null);
            assertThat(adapter.get("key")).isNull();
        }
    }

    @Nested
    @DisplayName("DefaultNDCAdapter测试")
    class DefaultNDCAdapterTests {

        private NDCAdapter adapter;

        @BeforeEach
        void setUp() {
            adapter = provider.getNDCAdapter();
            adapter.clear();
        }

        @AfterEach
        void tearDown() {
            adapter.clear();
        }

        @Test
        @DisplayName("push和pop")
        void testPushPop() {
            adapter.push("msg1");
            adapter.push("msg2");

            assertThat(adapter.pop()).isEqualTo("msg2");
            assertThat(adapter.pop()).isEqualTo("msg1");
        }

        @Test
        @DisplayName("peek")
        void testPeek() {
            adapter.push("msg");
            assertThat(adapter.peek()).isEqualTo("msg");
            assertThat(adapter.peek()).isEqualTo("msg"); // Still there
        }

        @Test
        @DisplayName("clear")
        void testClear() {
            adapter.push("msg1");
            adapter.push("msg2");
            adapter.clear();

            assertThat(adapter.getDepth()).isZero();
        }

        @Test
        @DisplayName("getDepth")
        void testGetDepth() {
            assertThat(adapter.getDepth()).isZero();
            adapter.push("msg");
            assertThat(adapter.getDepth()).isEqualTo(1);
        }

        @Test
        @DisplayName("setMaxDepth限制深度")
        void testSetMaxDepth() {
            adapter.setMaxDepth(2);
            adapter.push("1");
            adapter.push("2");
            adapter.push("3"); // Should be ignored

            assertThat(adapter.getDepth()).isEqualTo(2);
        }

        @Test
        @DisplayName("getCopyOfStack")
        void testGetCopyOfStack() {
            adapter.push("msg1");
            adapter.push("msg2");

            var copy = adapter.getCopyOfStack();
            assertThat(copy).hasSize(2);

            copy.push("msg3");
            assertThat(adapter.getDepth()).isEqualTo(2);
        }

        @Test
        @DisplayName("setStack")
        void testSetStack() {
            adapter.push("old");
            var newStack = new java.util.ArrayDeque<String>();
            newStack.push("new1");
            newStack.push("new2");
            adapter.setStack(newStack);

            assertThat(adapter.getDepth()).isEqualTo(2);
        }

        @Test
        @DisplayName("setStack(null)")
        void testSetStackNull() {
            adapter.push("msg");
            adapter.setStack(null);
            assertThat(adapter.getDepth()).isZero();
        }

        @Test
        @DisplayName("pop空栈返回null")
        void testPopEmpty() {
            assertThat(adapter.pop()).isNull();
        }

        @Test
        @DisplayName("peek空栈返回null")
        void testPeekEmpty() {
            assertThat(adapter.peek()).isNull();
        }
    }
}
