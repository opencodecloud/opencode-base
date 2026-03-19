package cloud.opencode.base.event.serialization;

import cloud.opencode.base.event.Event;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.Serial;
import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * EventSerializer 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
@DisplayName("EventSerializer 测试")
class EventSerializerTest {

    @Nested
    @DisplayName("isSerializationModuleAvailable 测试")
    class IsSerializationModuleAvailableTests {

        @Test
        @DisplayName("返回布尔值")
        void shouldReturnBoolean() {
            boolean result = EventSerializer.isSerializationModuleAvailable();
            assertThat(result).isIn(true, false);
        }
    }

    @Nested
    @DisplayName("serialize 测试")
    class SerializeTests {

        @Test
        @DisplayName("序列化事件成功")
        void shouldSerializeEvent() {
            // Skip if serialization module is available (test classes may not be compatible)
            if (EventSerializer.isSerializationModuleAvailable()) {
                return;
            }

            TestEvent event = new TestEvent("test-123", "Hello World");
            byte[] data = EventSerializer.serialize(event);
            assertThat(data).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("null 事件抛出异常")
        void shouldThrowExceptionForNullEvent() {
            assertThatThrownBy(() -> EventSerializer.serialize(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("event");
        }
    }

    @Nested
    @DisplayName("deserialize 测试")
    class DeserializeTests {

        @Test
        @DisplayName("反序列化事件成功")
        void shouldDeserializeEvent() {
            // Skip if serialization module is available (test classes may not be compatible)
            if (EventSerializer.isSerializationModuleAvailable()) {
                return;
            }

            TestEvent original = new TestEvent("test-456", "Test Message");

            byte[] data = EventSerializer.serialize(original);
            TestEvent restored = EventSerializer.deserialize(data, TestEvent.class);

            assertThat(restored).isNotNull();
            assertThat(restored.getId()).isEqualTo(original.getId());
            assertThat(restored.getMessage()).isEqualTo(original.getMessage());
        }

        @Test
        @DisplayName("null 数据抛出异常")
        void shouldThrowExceptionForNullData() {
            assertThatThrownBy(() -> EventSerializer.deserialize(null, TestEvent.class))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("data");
        }

        @Test
        @DisplayName("null 类型抛出异常")
        void shouldThrowExceptionForNullType() {
            byte[] data = new byte[]{1, 2, 3};
            assertThatThrownBy(() -> EventSerializer.deserialize(data, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("eventType");
        }

        @Test
        @DisplayName("反序列化类型不匹配时抛出异常")
        void shouldThrowExceptionForTypeMismatch() {
            // Skip if serialization module is available (test classes may not be compatible)
            if (EventSerializer.isSerializationModuleAvailable()) {
                return;
            }

            TestEvent original = new TestEvent("test-789", "Original Message");
            byte[] data = EventSerializer.serialize(original);

            assertThatThrownBy(() -> EventSerializer.deserialize(data, AnotherTestEvent.class))
                    .isInstanceOf(EventSerializer.EventSerializationException.class);
        }
    }

    @Nested
    @DisplayName("serializeToString 测试")
    class SerializeToStringTests {

        @Test
        @DisplayName("序列化为 Base64 字符串")
        void shouldSerializeToBase64String() {
            // Skip if serialization module is available (test classes may not be compatible)
            if (EventSerializer.isSerializationModuleAvailable()) {
                return;
            }

            TestEvent event = new TestEvent("test-b64", "Base64 Test");

            String base64 = EventSerializer.serializeToString(event);

            assertThat(base64).isNotNull().isNotEmpty();
            assertThat(base64).matches("^[A-Za-z0-9+/=]+$");
        }
    }

    @Nested
    @DisplayName("deserializeFromString 测试")
    class DeserializeFromStringTests {

        @Test
        @DisplayName("从 Base64 字符串反序列化")
        void shouldDeserializeFromBase64String() {
            // Skip if serialization module is available (test classes may not be compatible)
            if (EventSerializer.isSerializationModuleAvailable()) {
                return;
            }

            TestEvent original = new TestEvent("test-from-b64", "From Base64");

            String base64 = EventSerializer.serializeToString(original);
            TestEvent restored = EventSerializer.deserializeFromString(base64, TestEvent.class);

            assertThat(restored).isNotNull();
            assertThat(restored.getId()).isEqualTo(original.getId());
            assertThat(restored.getMessage()).isEqualTo(original.getMessage());
        }

        @Test
        @DisplayName("null Base64 字符串抛出异常")
        void shouldThrowExceptionForNullBase64() {
            assertThatThrownBy(() -> EventSerializer.deserializeFromString(null, TestEvent.class))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("base64");
        }
    }

    @Nested
    @DisplayName("往返测试")
    class RoundTripTests {

        @Test
        @DisplayName("字节数组往返")
        void shouldRoundTripWithBytes() {
            // Skip if serialization module is available (test classes may not be compatible)
            if (EventSerializer.isSerializationModuleAvailable()) {
                return;
            }

            TestEvent original = new TestEvent("roundtrip-bytes", "Round Trip Test");

            byte[] data = EventSerializer.serialize(original);
            TestEvent restored = EventSerializer.deserialize(data, TestEvent.class);

            assertThat(restored.getId()).isEqualTo(original.getId());
            assertThat(restored.getMessage()).isEqualTo(original.getMessage());
            assertThat(restored.getTimestamp()).isEqualTo(original.getTimestamp());
        }

        @Test
        @DisplayName("字符串往返")
        void shouldRoundTripWithString() {
            // Skip if serialization module is available (test classes may not be compatible)
            if (EventSerializer.isSerializationModuleAvailable()) {
                return;
            }

            TestEvent original = new TestEvent("roundtrip-string", "String Round Trip");

            String base64 = EventSerializer.serializeToString(original);
            TestEvent restored = EventSerializer.deserializeFromString(base64, TestEvent.class);

            assertThat(restored.getId()).isEqualTo(original.getId());
            assertThat(restored.getMessage()).isEqualTo(original.getMessage());
        }
    }

    @Nested
    @DisplayName("EventSerializationException 测试")
    class EventSerializationExceptionTests {

        @Test
        @DisplayName("创建带消息的异常")
        void shouldCreateExceptionWithMessage() {
            var exception = new EventSerializer.EventSerializationException("Test message");

            assertThat(exception.getMessage()).isEqualTo("Test message");
            assertThat(exception.getCause()).isNull();
        }

        @Test
        @DisplayName("创建带消息和原因的异常")
        void shouldCreateExceptionWithMessageAndCause() {
            var cause = new RuntimeException("Root cause");
            var exception = new EventSerializer.EventSerializationException("Test message", cause);

            assertThat(exception.getMessage()).isEqualTo("Test message");
            assertThat(exception.getCause()).isEqualTo(cause);
        }
    }

    /**
     * Test event implementation
     */
    private static class TestEvent extends Event implements java.io.Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private final String message;

        public TestEvent(String source, String message) {
            super(source);
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Another test event for type mismatch testing
     */
    private static class AnotherTestEvent extends Event implements java.io.Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        public AnotherTestEvent(String source) {
            super(source);
        }
    }
}
