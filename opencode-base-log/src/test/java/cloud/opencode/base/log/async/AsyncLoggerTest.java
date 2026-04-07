package cloud.opencode.base.log.async;

import cloud.opencode.base.log.LogLevel;
import cloud.opencode.base.log.Logger;
import cloud.opencode.base.log.context.MDC;
import cloud.opencode.base.log.marker.Marker;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * AsyncLogger 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.3
 */
@DisplayName("AsyncLogger 测试")
class AsyncLoggerTest {

    private RecordingLogger recordingLogger;

    @BeforeEach
    void setUp() {
        recordingLogger = new RecordingLogger("test-logger");
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Nested
    @DisplayName("创建测试")
    class CreationTests {

        @Test
        @DisplayName("wrap 创建有效实例")
        void wrapCreatesValidInstance() {
            try (AsyncLogger asyncLogger = AsyncLogger.wrap(recordingLogger)) {
                assertThat(asyncLogger).isNotNull();
                assertThat(asyncLogger.getName()).isEqualTo("test-logger");
            }
        }

        @Test
        @DisplayName("wrap 使用自定义队列容量")
        void wrapWithCustomCapacity() {
            try (AsyncLogger asyncLogger = AsyncLogger.wrap(recordingLogger, 128)) {
                assertThat(asyncLogger).isNotNull();
            }
        }

        @Test
        @DisplayName("wrap null 委托抛出异常")
        void wrapNullDelegateThrows() {
            assertThatThrownBy(() -> AsyncLogger.wrap(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("wrap 非法队列容量抛出异常")
        void wrapInvalidCapacityThrows() {
            assertThatThrownBy(() -> AsyncLogger.wrap(recordingLogger, 0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> AsyncLogger.wrap(recordingLogger, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("异步日志投递测试")
    class AsyncDeliveryTests {

        @Test
        @DisplayName("异步记录消息成功投递")
        void asyncLoggingDeliversMessages() throws Exception {
            try (AsyncLogger asyncLogger = AsyncLogger.wrap(recordingLogger)) {
                asyncLogger.info("hello world");
                asyncLogger.flush();

                assertThat(recordingLogger.getEntries()).hasSize(1);
                LogEntry entry = recordingLogger.getEntries().getFirst();
                assertThat(entry.level).isEqualTo(LogLevel.INFO);
                assertThat(entry.message).isEqualTo("hello world");
            }
        }

        @Test
        @DisplayName("多种日志级别都能正确投递")
        void multipleLogLevelsWork() throws Exception {
            try (AsyncLogger asyncLogger = AsyncLogger.wrap(recordingLogger)) {
                asyncLogger.trace("trace msg");
                asyncLogger.debug("debug msg");
                asyncLogger.info("info msg");
                asyncLogger.warn("warn msg");
                asyncLogger.error("error msg");
                asyncLogger.flush();

                assertThat(recordingLogger.getEntries()).hasSize(5);
                assertThat(recordingLogger.getEntries().get(0).level).isEqualTo(LogLevel.TRACE);
                assertThat(recordingLogger.getEntries().get(1).level).isEqualTo(LogLevel.DEBUG);
                assertThat(recordingLogger.getEntries().get(2).level).isEqualTo(LogLevel.INFO);
                assertThat(recordingLogger.getEntries().get(3).level).isEqualTo(LogLevel.WARN);
                assertThat(recordingLogger.getEntries().get(4).level).isEqualTo(LogLevel.ERROR);
            }
        }

        @Test
        @DisplayName("带参数的日志记录")
        void loggingWithArgs() throws Exception {
            try (AsyncLogger asyncLogger = AsyncLogger.wrap(recordingLogger)) {
                asyncLogger.info("hello {}", "world");
                asyncLogger.flush();

                assertThat(recordingLogger.getEntries()).hasSize(1);
                LogEntry entry = recordingLogger.getEntries().getFirst();
                assertThat(entry.message).isEqualTo("hello {}");
                assertThat(entry.args).containsExactly("world");
            }
        }

        @Test
        @DisplayName("带异常的日志记录")
        void loggingWithThrowable() throws Exception {
            RuntimeException ex = new RuntimeException("test error");
            try (AsyncLogger asyncLogger = AsyncLogger.wrap(recordingLogger)) {
                asyncLogger.error("failed", ex);
                asyncLogger.flush();

                assertThat(recordingLogger.getEntries()).hasSize(1);
                LogEntry entry = recordingLogger.getEntries().getFirst();
                assertThat(entry.level).isEqualTo(LogLevel.ERROR);
                assertThat(entry.throwable).isSameAs(ex);
            }
        }

        @Test
        @DisplayName("error(Throwable) 方法正确投递")
        void errorThrowableOnly() throws Exception {
            RuntimeException ex = new RuntimeException("only throwable");
            try (AsyncLogger asyncLogger = AsyncLogger.wrap(recordingLogger)) {
                asyncLogger.error(ex);
                asyncLogger.flush();

                assertThat(recordingLogger.getEntries()).hasSize(1);
                LogEntry entry = recordingLogger.getEntries().getFirst();
                assertThat(entry.level).isEqualTo(LogLevel.ERROR);
                assertThat(entry.throwable).isSameAs(ex);
                assertThat(entry.message).isNull();
            }
        }

        @Test
        @DisplayName("Supplier 延迟求值日志")
        void supplierLazyEvaluation() throws Exception {
            try (AsyncLogger asyncLogger = AsyncLogger.wrap(recordingLogger)) {
                asyncLogger.info(() -> "lazy-" + "message");
                asyncLogger.flush();

                assertThat(recordingLogger.getEntries()).hasSize(1);
                assertThat(recordingLogger.getEntries().getFirst().message).isEqualTo("lazy-message");
            }
        }

        @Test
        @DisplayName("log 通用方法正确投递")
        void genericLogMethod() throws Exception {
            try (AsyncLogger asyncLogger = AsyncLogger.wrap(recordingLogger)) {
                asyncLogger.log(LogLevel.WARN, "generic warn");
                asyncLogger.log(LogLevel.ERROR, "error with {}", "param");
                asyncLogger.flush();

                assertThat(recordingLogger.getEntries()).hasSize(2);
                assertThat(recordingLogger.getEntries().get(0).level).isEqualTo(LogLevel.WARN);
                assertThat(recordingLogger.getEntries().get(1).args).containsExactly("param");
            }
        }
    }

    @Nested
    @DisplayName("同步委托测试")
    class SynchronousDelegationTests {

        @Test
        @DisplayName("isEnabled 方法同步委托")
        void isEnabledDelegatesSynchronously() {
            try (AsyncLogger asyncLogger = AsyncLogger.wrap(recordingLogger)) {
                assertThat(asyncLogger.isTraceEnabled()).isTrue();
                assertThat(asyncLogger.isDebugEnabled()).isTrue();
                assertThat(asyncLogger.isInfoEnabled()).isTrue();
                assertThat(asyncLogger.isWarnEnabled()).isTrue();
                assertThat(asyncLogger.isErrorEnabled()).isTrue();
                assertThat(asyncLogger.isEnabled(LogLevel.INFO)).isTrue();
            }
        }

        @Test
        @DisplayName("getName 同步委托")
        void getNameDelegates() {
            try (AsyncLogger asyncLogger = AsyncLogger.wrap(recordingLogger)) {
                assertThat(asyncLogger.getName()).isEqualTo("test-logger");
            }
        }

        @Test
        @DisplayName("isEnabled 使用 Marker 同步委托")
        void isEnabledWithMarkerDelegates() {
            SimpleMarker marker = new SimpleMarker("TEST");
            try (AsyncLogger asyncLogger = AsyncLogger.wrap(recordingLogger)) {
                assertThat(asyncLogger.isTraceEnabled(marker)).isTrue();
                assertThat(asyncLogger.isDebugEnabled(marker)).isTrue();
                assertThat(asyncLogger.isInfoEnabled(marker)).isTrue();
                assertThat(asyncLogger.isWarnEnabled(marker)).isTrue();
                assertThat(asyncLogger.isErrorEnabled(marker)).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("Flush 测试")
    class FlushTests {

        @Test
        @DisplayName("flush 排空队列")
        void flushDrainsQueue() throws Exception {
            try (AsyncLogger asyncLogger = AsyncLogger.wrap(recordingLogger)) {
                for (int i = 0; i < 100; i++) {
                    asyncLogger.info("message-" + i);
                }
                asyncLogger.flush();

                assertThat(recordingLogger.getEntries()).hasSize(100);
            }
        }
    }

    @Nested
    @DisplayName("关闭测试")
    class ShutdownTests {

        @Test
        @DisplayName("shutdown 正常关闭")
        void shutdownWorks() throws Exception {
            AsyncLogger asyncLogger = AsyncLogger.wrap(recordingLogger);
            asyncLogger.info("before shutdown");
            asyncLogger.flush();
            asyncLogger.shutdown();

            assertThat(recordingLogger.getEntries()).hasSize(1);
        }

        @Test
        @DisplayName("close 正常关闭")
        void closeWorks() throws Exception {
            AsyncLogger asyncLogger = AsyncLogger.wrap(recordingLogger);
            asyncLogger.info("before close");
            asyncLogger.flush();
            asyncLogger.close();

            assertThat(recordingLogger.getEntries()).hasSize(1);
        }

        @Test
        @DisplayName("重复关闭是安全的")
        void doubleCloseIsSafe() {
            AsyncLogger asyncLogger = AsyncLogger.wrap(recordingLogger);
            asyncLogger.close();
            asyncLogger.close(); // should not throw
        }
    }

    @Nested
    @DisplayName("背压测试")
    class BackpressureTests {

        @Test
        @DisplayName("队列满时同步回退不丢消息")
        void backpressureFallsBackToSync() throws Exception {
            // Use tiny queue capacity
            try (AsyncLogger asyncLogger = AsyncLogger.wrap(recordingLogger, 2)) {
                // Log more messages than queue capacity
                for (int i = 0; i < 20; i++) {
                    asyncLogger.info("msg-" + i);
                }
                asyncLogger.flush();

                // All messages should be delivered (some async, some sync fallback)
                assertThat(recordingLogger.getEntries()).hasSize(20);
            }
        }
    }

    @Nested
    @DisplayName("MDC 上下文传播测试")
    class MDCPropagationTests {

        @Test
        @DisplayName("MDC 上下文传播到异步线程")
        void mdcContextPropagated() throws Exception {
            try (AsyncLogger asyncLogger = AsyncLogger.wrap(recordingLogger)) {
                MDC.put("requestId", "req-12345");
                asyncLogger.info("with context");
                asyncLogger.flush();

                assertThat(recordingLogger.getEntries()).hasSize(1);
                LogEntry entry = recordingLogger.getEntries().getFirst();
                assertThat(entry.mdcSnapshot).containsEntry("requestId", "req-12345");
            }
        }
    }

    @Nested
    @DisplayName("Marker 日志测试")
    class MarkerTests {

        @Test
        @DisplayName("带 Marker 的日志正确投递")
        void markerLogging() throws Exception {
            SimpleMarker marker = new SimpleMarker("SECURITY");
            try (AsyncLogger asyncLogger = AsyncLogger.wrap(recordingLogger)) {
                asyncLogger.trace(marker, "trace with marker");
                asyncLogger.debug(marker, "debug with marker");
                asyncLogger.info(marker, "info with marker");
                asyncLogger.warn(marker, "warn with marker");
                asyncLogger.error(marker, "error with marker");
                asyncLogger.flush();

                assertThat(recordingLogger.getEntries()).hasSize(5);
                for (LogEntry entry : recordingLogger.getEntries()) {
                    assertThat(entry.marker).isNotNull();
                    assertThat(entry.marker.getName()).isEqualTo("SECURITY");
                }
            }
        }

        @Test
        @DisplayName("带 Marker 和异常的 warn/error 日志")
        void markerWithThrowable() throws Exception {
            SimpleMarker marker = new SimpleMarker("AUDIT");
            RuntimeException ex = new RuntimeException("marker-error");
            try (AsyncLogger asyncLogger = AsyncLogger.wrap(recordingLogger)) {
                asyncLogger.warn(marker, "warn msg", ex);
                asyncLogger.error(marker, "error msg", ex);
                asyncLogger.flush();

                assertThat(recordingLogger.getEntries()).hasSize(2);
                assertThat(recordingLogger.getEntries().get(0).level).isEqualTo(LogLevel.WARN);
                assertThat(recordingLogger.getEntries().get(0).throwable).isSameAs(ex);
                assertThat(recordingLogger.getEntries().get(1).level).isEqualTo(LogLevel.ERROR);
                assertThat(recordingLogger.getEntries().get(1).throwable).isSameAs(ex);
            }
        }

        @Test
        @DisplayName("带 Marker 和参数的日志")
        void markerWithArgs() throws Exception {
            SimpleMarker marker = new SimpleMarker("TEST");
            try (AsyncLogger asyncLogger = AsyncLogger.wrap(recordingLogger)) {
                asyncLogger.trace(marker, "trace {}", "a");
                asyncLogger.debug(marker, "debug {}", "b");
                asyncLogger.info(marker, "info {}", "c");
                asyncLogger.warn(marker, "warn {}", "d");
                asyncLogger.error(marker, "error {}", "e");
                asyncLogger.flush();

                assertThat(recordingLogger.getEntries()).hasSize(5);
            }
        }
    }

    // ==================== Test Helpers ====================

    /**
     * Simple Marker implementation for testing.
     */
    private static class SimpleMarker implements Marker {
        private final String name;

        SimpleMarker(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void add(Marker reference) {
        }

        @Override
        public boolean remove(Marker reference) {
            return false;
        }

        @Override
        public boolean hasReferences() {
            return false;
        }

        @Override
        public Iterator<Marker> iterator() {
            return Collections.emptyIterator();
        }

        @Override
        public boolean contains(Marker other) {
            return false;
        }

        @Override
        public boolean contains(String name) {
            return this.name.equals(name);
        }
    }

    /**
     * Log entry recorded by RecordingLogger.
     */
    static class LogEntry {
        final LogLevel level;
        final String message;
        final Object[] args;
        final Throwable throwable;
        final Marker marker;
        final Map<String, String> mdcSnapshot;

        LogEntry(LogLevel level, String message, Object[] args, Throwable throwable,
                 Marker marker, Map<String, String> mdcSnapshot) {
            this.level = level;
            this.message = message;
            this.args = args;
            this.throwable = throwable;
            this.marker = marker;
            this.mdcSnapshot = mdcSnapshot;
        }
    }

    /**
     * Recording Logger that captures all log calls for assertion.
     */
    private static class RecordingLogger implements Logger {
        private final String name;
        private final CopyOnWriteArrayList<LogEntry> entries = new CopyOnWriteArrayList<>();

        RecordingLogger(String name) {
            this.name = name;
        }

        List<LogEntry> getEntries() {
            return entries;
        }

        private void record(LogLevel level, String message, Object[] args,
                            Throwable throwable, Marker marker) {
            Map<String, String> mdc = MDC.getCopyOfContextMap();
            entries.add(new LogEntry(level, message, args, throwable, marker, mdc));
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
            record(LogLevel.TRACE, message, null, null, null);
        }

        @Override
        public void trace(String format, Object... args) {
            record(LogLevel.TRACE, format, args, null, null);
        }

        @Override
        public void trace(String message, Throwable throwable) {
            record(LogLevel.TRACE, message, null, throwable, null);
        }

        @Override
        public void trace(Supplier<String> messageSupplier) {
            record(LogLevel.TRACE, messageSupplier.get(), null, null, null);
        }

        @Override
        public void trace(Marker marker, String message) {
            record(LogLevel.TRACE, message, null, null, marker);
        }

        @Override
        public void trace(Marker marker, String format, Object... args) {
            record(LogLevel.TRACE, format, args, null, marker);
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
            record(LogLevel.DEBUG, message, null, null, null);
        }

        @Override
        public void debug(String format, Object... args) {
            record(LogLevel.DEBUG, format, args, null, null);
        }

        @Override
        public void debug(String message, Throwable throwable) {
            record(LogLevel.DEBUG, message, null, throwable, null);
        }

        @Override
        public void debug(Supplier<String> messageSupplier) {
            record(LogLevel.DEBUG, messageSupplier.get(), null, null, null);
        }

        @Override
        public void debug(Marker marker, String message) {
            record(LogLevel.DEBUG, message, null, null, marker);
        }

        @Override
        public void debug(Marker marker, String format, Object... args) {
            record(LogLevel.DEBUG, format, args, null, marker);
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
            record(LogLevel.INFO, message, null, null, null);
        }

        @Override
        public void info(String format, Object... args) {
            record(LogLevel.INFO, format, args, null, null);
        }

        @Override
        public void info(String message, Throwable throwable) {
            record(LogLevel.INFO, message, null, throwable, null);
        }

        @Override
        public void info(Supplier<String> messageSupplier) {
            record(LogLevel.INFO, messageSupplier.get(), null, null, null);
        }

        @Override
        public void info(Marker marker, String message) {
            record(LogLevel.INFO, message, null, null, marker);
        }

        @Override
        public void info(Marker marker, String format, Object... args) {
            record(LogLevel.INFO, format, args, null, marker);
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
            record(LogLevel.WARN, message, null, null, null);
        }

        @Override
        public void warn(String format, Object... args) {
            record(LogLevel.WARN, format, args, null, null);
        }

        @Override
        public void warn(String message, Throwable throwable) {
            record(LogLevel.WARN, message, null, throwable, null);
        }

        @Override
        public void warn(Supplier<String> messageSupplier) {
            record(LogLevel.WARN, messageSupplier.get(), null, null, null);
        }

        @Override
        public void warn(Marker marker, String message) {
            record(LogLevel.WARN, message, null, null, marker);
        }

        @Override
        public void warn(Marker marker, String format, Object... args) {
            record(LogLevel.WARN, format, args, null, marker);
        }

        @Override
        public void warn(Marker marker, String message, Throwable throwable) {
            record(LogLevel.WARN, message, null, throwable, marker);
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
            record(LogLevel.ERROR, message, null, null, null);
        }

        @Override
        public void error(String format, Object... args) {
            record(LogLevel.ERROR, format, args, null, null);
        }

        @Override
        public void error(String message, Throwable throwable) {
            record(LogLevel.ERROR, message, null, throwable, null);
        }

        @Override
        public void error(Throwable throwable) {
            record(LogLevel.ERROR, null, null, throwable, null);
        }

        @Override
        public void error(Supplier<String> messageSupplier) {
            record(LogLevel.ERROR, messageSupplier.get(), null, null, null);
        }

        @Override
        public void error(Supplier<String> messageSupplier, Throwable throwable) {
            record(LogLevel.ERROR, messageSupplier.get(), null, throwable, null);
        }

        @Override
        public void error(Marker marker, String message) {
            record(LogLevel.ERROR, message, null, null, marker);
        }

        @Override
        public void error(Marker marker, String format, Object... args) {
            record(LogLevel.ERROR, format, args, null, marker);
        }

        @Override
        public void error(Marker marker, String message, Throwable throwable) {
            record(LogLevel.ERROR, message, null, throwable, marker);
        }

        @Override
        public boolean isEnabled(LogLevel level) {
            return true;
        }

        @Override
        public void log(LogLevel level, String message) {
            record(level, message, null, null, null);
        }

        @Override
        public void log(LogLevel level, String format, Object... args) {
            record(level, format, args, null, null);
        }

        @Override
        public void log(LogLevel level, String message, Throwable throwable) {
            record(level, message, null, throwable, null);
        }
    }
}
