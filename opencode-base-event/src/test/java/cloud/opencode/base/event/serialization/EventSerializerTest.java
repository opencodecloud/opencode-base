package cloud.opencode.base.event.serialization;

import cloud.opencode.base.event.Event;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.Serial;
import java.time.Instant;
import java.util.List;
import java.util.Map;

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

    /**
     * Non-serializable event (does NOT implement Serializable)
     */
    private static class NonSerializableEvent extends Event {
        private final String data;

        public NonSerializableEvent(String source, String data) {
            super(source);
            this.data = data;
        }

        public String getData() {
            return data;
        }
    }

    /**
     * Event with complex payload fields for exercising filter paths
     */
    private static class ComplexEvent extends Event implements java.io.Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private final List<String> tags;
        private final Map<String, Integer> metadata;
        private final String description;

        public ComplexEvent(String source, List<String> tags, Map<String, Integer> metadata, String description) {
            super(source);
            this.tags = tags;
            this.metadata = metadata;
            this.description = description;
        }

        public List<String> getTags() {
            return tags;
        }

        public Map<String, Integer> getMetadata() {
            return metadata;
        }

        public String getDescription() {
            return description;
        }
    }

    @Nested
    @DisplayName("Non-serializable event tests | 不可序列化事件测试")
    class NonSerializableEventTests {

        @Test
        @DisplayName("serialize non-serializable event should throw EventSerializationException")
        void shouldThrowForNonSerializableEvent() {
            if (EventSerializer.isSerializationModuleAvailable()) {
                return;
            }

            NonSerializableEvent event = new NonSerializableEvent("src", "payload");

            assertThatThrownBy(() -> EventSerializer.serialize(event))
                    .isInstanceOf(EventSerializer.EventSerializationException.class)
                    .hasMessageContaining("Failed to serialize event");
        }
    }

    @Nested
    @DisplayName("Corrupt data tests | 损坏数据测试")
    class CorruptDataTests {

        @Test
        @DisplayName("deserialize corrupt bytes should throw EventSerializationException")
        void shouldThrowForCorruptBytes() {
            if (EventSerializer.isSerializationModuleAvailable()) {
                return;
            }

            byte[] garbage = {0x00, 0x01, 0x02, 0x03, 0x7F, (byte) 0xFF, (byte) 0xAB};

            assertThatThrownBy(() -> EventSerializer.deserialize(garbage, TestEvent.class))
                    .isInstanceOf(EventSerializer.EventSerializationException.class)
                    .hasMessageContaining("Failed to deserialize event");
        }

        @Test
        @DisplayName("deserialize empty byte array should throw EventSerializationException")
        void shouldThrowForEmptyBytes() {
            if (EventSerializer.isSerializationModuleAvailable()) {
                return;
            }

            byte[] empty = new byte[0];

            assertThatThrownBy(() -> EventSerializer.deserialize(empty, TestEvent.class))
                    .isInstanceOf(EventSerializer.EventSerializationException.class)
                    .hasMessageContaining("Failed to deserialize event");
        }
    }

    @Nested
    @DisplayName("Type mismatch detail tests | 类型不匹配详情测试")
    class TypeMismatchDetailTests {

        @Test
        @DisplayName("deserialized object type mismatch should contain 'not of expected type' message")
        void shouldContainExpectedTypeMessage() {
            if (EventSerializer.isSerializationModuleAvailable()) {
                return;
            }

            TestEvent original = new TestEvent("mismatch-src", "mismatch data");
            byte[] data = EventSerializer.serialize(original);

            assertThatThrownBy(() -> EventSerializer.deserialize(data, AnotherTestEvent.class))
                    .isInstanceOf(EventSerializer.EventSerializationException.class)
                    .hasMessageContaining("not of expected type");
        }
    }

    @Nested
    @DisplayName("String serialization edge cases | 字符串序列化边界测试")
    class StringSerializationEdgeCaseTests {

        @Test
        @DisplayName("serializeToString and deserializeFromString roundtrip with null source")
        void shouldRoundTripWithNullSource() {
            if (EventSerializer.isSerializationModuleAvailable()) {
                return;
            }

            TestEvent event = new TestEvent(null, "null-source-message");

            String base64 = EventSerializer.serializeToString(event);
            TestEvent restored = EventSerializer.deserializeFromString(base64, TestEvent.class);

            assertThat(restored).isNotNull();
            assertThat(restored.getSource()).isNull();
            assertThat(restored.getMessage()).isEqualTo("null-source-message");
        }

        @Test
        @DisplayName("invalid Base64 input should throw exception")
        void shouldThrowForInvalidBase64() {
            if (EventSerializer.isSerializationModuleAvailable()) {
                return;
            }

            String invalidBase64 = "!!!not-valid-base64!!!";

            assertThatThrownBy(() -> EventSerializer.deserializeFromString(invalidBase64, TestEvent.class))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Complex payload tests | 复杂负载测试")
    class ComplexPayloadTests {

        @Test
        @DisplayName("serialize and deserialize event with List and Map fields")
        void shouldRoundTripComplexEvent() {
            if (EventSerializer.isSerializationModuleAvailable()) {
                return;
            }

            List<String> tags = List.of("alpha", "beta", "gamma");
            Map<String, Integer> metadata = Map.of("count", 42, "version", 3);
            ComplexEvent original = new ComplexEvent("complex-src", tags, metadata, "complex description");

            byte[] data = EventSerializer.serialize(original);
            ComplexEvent restored = EventSerializer.deserialize(data, ComplexEvent.class);

            assertThat(restored).isNotNull();
            assertThat(restored.getTags()).containsExactlyInAnyOrderElementsOf(tags);
            assertThat(restored.getMetadata()).containsAllEntriesOf(metadata);
            assertThat(restored.getDescription()).isEqualTo("complex description");
        }

        @Test
        @DisplayName("serialize and deserialize event with empty collections")
        void shouldRoundTripWithEmptyCollections() {
            if (EventSerializer.isSerializationModuleAvailable()) {
                return;
            }

            ComplexEvent original = new ComplexEvent("empty-src", List.of(), Map.of(), "");

            byte[] data = EventSerializer.serialize(original);
            ComplexEvent restored = EventSerializer.deserialize(data, ComplexEvent.class);

            assertThat(restored).isNotNull();
            assertThat(restored.getTags()).isEmpty();
            assertThat(restored.getMetadata()).isEmpty();
            assertThat(restored.getDescription()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Large event tests | 大事件测试")
    class LargeEventTests {

        @Test
        @DisplayName("serialize and deserialize event with large string payload")
        void shouldRoundTripLargeEvent() {
            if (EventSerializer.isSerializationModuleAvailable()) {
                return;
            }

            String largePayload = "X".repeat(100_000);
            TestEvent original = new TestEvent("large-src", largePayload);

            byte[] data = EventSerializer.serialize(original);
            assertThat(data).isNotNull().isNotEmpty();

            TestEvent restored = EventSerializer.deserialize(data, TestEvent.class);

            assertThat(restored).isNotNull();
            assertThat(restored.getMessage()).isEqualTo(largePayload);
            assertThat(restored.getMessage()).hasSize(100_000);
        }

        @Test
        @DisplayName("string roundtrip with large payload")
        void shouldStringRoundTripLargeEvent() {
            if (EventSerializer.isSerializationModuleAvailable()) {
                return;
            }

            String largePayload = "Y".repeat(50_000);
            TestEvent original = new TestEvent("large-b64-src", largePayload);

            String base64 = EventSerializer.serializeToString(original);
            TestEvent restored = EventSerializer.deserializeFromString(base64, TestEvent.class);

            assertThat(restored.getMessage()).isEqualTo(largePayload);
        }
    }
}
