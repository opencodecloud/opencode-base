package cloud.opencode.base.log;

import cloud.opencode.base.log.marker.Markers;
import cloud.opencode.base.log.marker.Marker;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * LogEvent 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.3
 */
@DisplayName("LogEvent 测试")
class LogEventTest {

    @Nested
    @DisplayName("Builder 默认值测试")
    class BuilderDefaultsTests {

        @Test
        @DisplayName("Builder 使用正确的默认值")
        void testBuilderDefaults() {
            long before = System.currentTimeMillis();
            LogEvent event = LogEvent.builder(LogLevel.INFO, "hello").build();
            long after = System.currentTimeMillis();

            assertThat(event.level()).isEqualTo(LogLevel.INFO);
            assertThat(event.message()).isEqualTo("hello");
            assertThat(event.loggerName()).isNull();
            assertThat(event.throwable()).isNull();
            assertThat(event.marker()).isNull();
            assertThat(event.mdc()).isEmpty();
            assertThat(event.timestampMillis()).isBetween(before, after);
            assertThat(event.threadName()).isEqualTo(Thread.currentThread().getName());
            assertThat(event.callerInfo()).isSameAs(CallerInfo.UNKNOWN);
        }
    }

    @Nested
    @DisplayName("Builder 完整字段测试")
    class BuilderAllFieldsTests {

        @Test
        @DisplayName("Builder 设置所有字段")
        void testBuilderWithAllFields() {
            RuntimeException ex = new RuntimeException("test");
            Marker marker = Markers.getMarker("TEST_EVENT");
            Map<String, String> mdc = Map.of("key", "value");
            CallerInfo caller = new CallerInfo("com.Test", "run", "Test.java", 10);

            LogEvent event = LogEvent.builder(LogLevel.ERROR, "error msg")
                    .loggerName("com.example.Logger")
                    .throwable(ex)
                    .marker(marker)
                    .mdc(mdc)
                    .timestamp(1234567890L)
                    .threadName("worker-1")
                    .callerInfo(caller)
                    .build();

            assertThat(event.level()).isEqualTo(LogLevel.ERROR);
            assertThat(event.message()).isEqualTo("error msg");
            assertThat(event.loggerName()).isEqualTo("com.example.Logger");
            assertThat(event.throwable()).isSameAs(ex);
            assertThat(event.marker()).isSameAs(marker);
            assertThat(event.mdc()).containsEntry("key", "value");
            assertThat(event.timestampMillis()).isEqualTo(1234567890L);
            assertThat(event.threadName()).isEqualTo("worker-1");
            assertThat(event.callerInfo()).isSameAs(caller);
        }
    }

    @Nested
    @DisplayName("MDC 防御性拷贝测试")
    class MdcDefensiveCopyTests {

        @Test
        @DisplayName("修改原始 Map 不影响事件")
        void testDefensiveCopyOfMdc() {
            Map<String, String> original = new HashMap<>();
            original.put("k1", "v1");

            LogEvent event = LogEvent.builder(LogLevel.INFO, "msg")
                    .mdc(original)
                    .build();

            original.put("k2", "v2");

            assertThat(event.mdc()).containsOnlyKeys("k1");
            assertThat(event.mdc()).doesNotContainKey("k2");
        }

        @Test
        @DisplayName("事件 MDC 不可修改")
        void testMdcIsUnmodifiable() {
            LogEvent event = LogEvent.builder(LogLevel.INFO, "msg")
                    .mdc(Map.of("k", "v"))
                    .build();

            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> event.mdc().put("new", "val"));
        }

        @Test
        @DisplayName("null MDC 处理为空 Map")
        void testNullMdcHandledAsEmpty() {
            LogEvent event = LogEvent.builder(LogLevel.INFO, "msg")
                    .mdc(null)
                    .build();

            assertThat(event.mdc()).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("直接构造时 null MDC 处理为空 Map")
        void testRecordConstructorNullMdc() {
            LogEvent event = new LogEvent(
                    LogLevel.INFO, "logger", "msg", null, null,
                    null, 0L, "main", CallerInfo.UNKNOWN);

            assertThat(event.mdc()).isNotNull().isEmpty();
        }
    }

    @Nested
    @DisplayName("便捷方法测试")
    class ConvenienceMethodTests {

        @Test
        @DisplayName("hasThrowable 返回 true（有异常时）")
        void testHasThrowableTrue() {
            LogEvent event = LogEvent.builder(LogLevel.ERROR, "err")
                    .throwable(new RuntimeException())
                    .build();
            assertThat(event.hasThrowable()).isTrue();
        }

        @Test
        @DisplayName("hasThrowable 返回 false（无异常时）")
        void testHasThrowableFalse() {
            LogEvent event = LogEvent.builder(LogLevel.INFO, "ok").build();
            assertThat(event.hasThrowable()).isFalse();
        }

        @Test
        @DisplayName("hasMarker 返回 true（有标记时）")
        void testHasMarkerTrue() {
            LogEvent event = LogEvent.builder(LogLevel.INFO, "msg")
                    .marker(Markers.getMarker("HAS_MARKER_TEST"))
                    .build();
            assertThat(event.hasMarker()).isTrue();
        }

        @Test
        @DisplayName("hasMarker 返回 false（无标记时）")
        void testHasMarkerFalse() {
            LogEvent event = LogEvent.builder(LogLevel.INFO, "msg").build();
            assertThat(event.hasMarker()).isFalse();
        }
    }

    @Nested
    @DisplayName("toFormattedString 测试")
    class FormattedStringTests {

        @Test
        @DisplayName("格式化字符串包含所有必要部分")
        void testToFormattedStringFormat() {
            LogEvent event = LogEvent.builder(LogLevel.WARN, "something happened")
                    .loggerName("com.example.Service")
                    .threadName("worker-5")
                    .timestamp(1700000000000L)
                    .build();

            String formatted = event.toFormattedString();

            assertThat(formatted).contains("[worker-5]");
            assertThat(formatted).contains("WARN");
            assertThat(formatted).contains("com.example.Service");
            assertThat(formatted).contains("something happened");
            assertThat(formatted).matches("\\[.*\\] \\[worker-5\\] WARN com\\.example\\.Service - something happened");
        }

        @Test
        @DisplayName("loggerName 为 null 时不报错")
        void testToFormattedStringNullLoggerName() {
            LogEvent event = LogEvent.builder(LogLevel.INFO, "msg")
                    .threadName("t1")
                    .build();

            String formatted = event.toFormattedString();
            assertThat(formatted).contains("INFO");
            assertThat(formatted).contains("msg");
        }
    }

    @Nested
    @DisplayName("构造验证测试")
    class ConstructorValidationTests {

        @Test
        @DisplayName("level 为 null 抛出异常")
        void testNullLevelThrows() {
            assertThatNullPointerException()
                    .isThrownBy(() -> LogEvent.builder(null, "msg").build());
        }

        @Test
        @DisplayName("message 为 null 抛出异常")
        void testNullMessageThrows() {
            assertThatNullPointerException()
                    .isThrownBy(() -> LogEvent.builder(LogLevel.INFO, null).build());
        }
    }
}
