package cloud.opencode.base.log.enhance;

import cloud.opencode.base.log.Logger;
import cloud.opencode.base.log.LoggerFactory;
import cloud.opencode.base.log.marker.Markers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * StructuredLog 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
@DisplayName("StructuredLog 测试")
class StructuredLogTest {

    @Nested
    @DisplayName("类定义测试")
    class ClassDefinitionTests {

        @Test
        @DisplayName("类是final的")
        void testIsFinal() {
            assertThat(java.lang.reflect.Modifier.isFinal(StructuredLog.class.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws NoSuchMethodException {
            var constructor = StructuredLog.class.getDeclaredConstructor();
            assertThat(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("trace方法测试")
    class TraceTests {

        @Test
        @DisplayName("创建TRACE级别Builder")
        void testTrace() {
            StructuredLog.Builder builder = StructuredLog.trace();
            assertThat(builder).isNotNull();
        }
    }

    @Nested
    @DisplayName("debug方法测试")
    class DebugTests {

        @Test
        @DisplayName("创建DEBUG级别Builder")
        void testDebug() {
            StructuredLog.Builder builder = StructuredLog.debug();
            assertThat(builder).isNotNull();
        }
    }

    @Nested
    @DisplayName("info方法测试")
    class InfoTests {

        @Test
        @DisplayName("创建INFO级别Builder")
        void testInfo() {
            StructuredLog.Builder builder = StructuredLog.info();
            assertThat(builder).isNotNull();
        }
    }

    @Nested
    @DisplayName("warn方法测试")
    class WarnTests {

        @Test
        @DisplayName("创建WARN级别Builder")
        void testWarn() {
            StructuredLog.Builder builder = StructuredLog.warn();
            assertThat(builder).isNotNull();
        }
    }

    @Nested
    @DisplayName("error方法测试")
    class ErrorTests {

        @Test
        @DisplayName("创建ERROR级别Builder")
        void testError() {
            StructuredLog.Builder builder = StructuredLog.error();
            assertThat(builder).isNotNull();
        }
    }

    @Nested
    @DisplayName("Builder类测试")
    class BuilderTests {

        @Test
        @DisplayName("Builder是final类")
        void testBuilderIsFinal() {
            assertThat(java.lang.reflect.Modifier.isFinal(StructuredLog.Builder.class.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("message方法设置消息")
        void testMessage() {
            StructuredLog.Builder builder = StructuredLog.info()
                .message("Test message");

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("field方法添加字段")
        void testField() {
            StructuredLog.Builder builder = StructuredLog.info()
                .field("key", "value");

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("fields方法添加多个字段")
        void testFields() {
            StructuredLog.Builder builder = StructuredLog.info()
                .fields(Map.of("key1", "value1", "key2", "value2"));

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("exception方法设置异常")
        void testException() {
            StructuredLog.Builder builder = StructuredLog.error()
                .exception(new RuntimeException("test"));

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("marker方法设置标记")
        void testMarker() {
            StructuredLog.Builder builder = StructuredLog.info()
                .marker(Markers.AUDIT);

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("traceId方法设置追踪ID")
        void testTraceId() {
            StructuredLog.Builder builder = StructuredLog.info()
                .traceId("trace-123");

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("spanId方法设置跨度ID")
        void testSpanId() {
            StructuredLog.Builder builder = StructuredLog.info()
                .spanId("span-456");

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("log方法记录日志")
        void testLog() {
            assertThatCode(() ->
                StructuredLog.info()
                    .message("Test message")
                    .field("key", "value")
                    .log()
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("log(Logger)方法记录到指定logger")
        void testLogWithLogger() {
            Logger logger = LoggerFactory.getLogger(StructuredLogTest.class);

            assertThatCode(() ->
                StructuredLog.info()
                    .message("Test message")
                    .log(logger)
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("链式调用")
        void testChaining() {
            assertThatCode(() ->
                StructuredLog.info()
                    .message("User login")
                    .field("userId", "user123")
                    .field("ip", "192.168.1.1")
                    .field("duration", 150)
                    .field("success", true)
                    .traceId("trace-123")
                    .spanId("span-456")
                    .log()
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("不同类型的字段值")
        void testDifferentFieldTypes() {
            assertThatCode(() ->
                StructuredLog.info()
                    .message("Test")
                    .field("string", "value")
                    .field("number", 123)
                    .field("boolean", true)
                    .field("object", new Object())
                    .log()
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("TRACE级别带异常日志")
        void testTraceWithException() {
            assertThatCode(() ->
                StructuredLog.trace()
                    .message("Trace message")
                    .exception(new RuntimeException("test"))
                    .log()
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("DEBUG级别带异常日志")
        void testDebugWithException() {
            assertThatCode(() ->
                StructuredLog.debug()
                    .message("Debug message")
                    .exception(new RuntimeException("test"))
                    .log()
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("INFO级别带marker日志")
        void testInfoWithMarker() {
            assertThatCode(() ->
                StructuredLog.info()
                    .message("Info message")
                    .marker(Markers.SECURITY)
                    .log()
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("INFO级别带异常日志")
        void testInfoWithException() {
            assertThatCode(() ->
                StructuredLog.info()
                    .message("Info message")
                    .exception(new RuntimeException("test"))
                    .log()
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("WARN级别带marker和异常日志")
        void testWarnWithMarkerAndException() {
            assertThatCode(() ->
                StructuredLog.warn()
                    .message("Warn message")
                    .marker(Markers.SECURITY)
                    .exception(new RuntimeException("test"))
                    .log()
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("ERROR级别带marker和异常日志")
        void testErrorWithMarkerAndException() {
            assertThatCode(() ->
                StructuredLog.error()
                    .message("Error message")
                    .marker(Markers.SECURITY)
                    .exception(new RuntimeException("test"))
                    .log()
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("消息中包含特殊字符")
        void testMessageWithSpecialCharacters() {
            assertThatCode(() ->
                StructuredLog.info()
                    .message("Message with \"quotes\" and \\ backslash")
                    .field("value", "With\nnewline\tand\ttabs")
                    .log()
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("无消息只有字段")
        void testNoMessageOnlyFields() {
            assertThatCode(() ->
                StructuredLog.info()
                    .field("key", "value")
                    .log()
            ).doesNotThrowAnyException();
        }
    }
}
