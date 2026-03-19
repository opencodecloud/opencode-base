package cloud.opencode.base.serialization;

import org.junit.jupiter.api.*;

import java.lang.reflect.Type;

import static org.assertj.core.api.Assertions.*;

/**
 * SerializerTest Tests
 * SerializerTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-serialization V1.0.0
 */
@DisplayName("Serializer Interface Tests")
class SerializerTest {

    @Nested
    @DisplayName("Default Method Tests")
    class DefaultMethodTests {

        @Test
        @DisplayName("getMimeType should return application/octet-stream by default")
        void getMimeTypeShouldReturnOctetStreamByDefault() {
            Serializer serializer = new TestMinimalSerializer();

            assertThat(serializer.getMimeType()).isEqualTo("application/octet-stream");
        }

        @Test
        @DisplayName("supports should return true by default")
        void supportsShouldReturnTrueByDefault() {
            Serializer serializer = new TestMinimalSerializer();

            assertThat(serializer.supports(String.class)).isTrue();
            assertThat(serializer.supports(Integer.class)).isTrue();
            assertThat(serializer.supports(Object.class)).isTrue();
        }

        @Test
        @DisplayName("isTextBased should return false by default")
        void isTextBasedShouldReturnFalseByDefault() {
            Serializer serializer = new TestMinimalSerializer();

            assertThat(serializer.isTextBased()).isFalse();
        }
    }

    @Nested
    @DisplayName("Override Default Methods Tests")
    class OverrideDefaultMethodsTests {

        @Test
        @DisplayName("getMimeType can be overridden")
        void getMimeTypeCanBeOverridden() {
            Serializer serializer = new TestCustomSerializer();

            assertThat(serializer.getMimeType()).isEqualTo("application/custom");
        }

        @Test
        @DisplayName("supports can be overridden")
        void supportsCanBeOverridden() {
            Serializer serializer = new TestCustomSerializer();

            assertThat(serializer.supports(String.class)).isTrue();
            assertThat(serializer.supports(Integer.class)).isFalse();
        }

        @Test
        @DisplayName("isTextBased can be overridden")
        void isTextBasedCanBeOverridden() {
            Serializer serializer = new TestCustomSerializer();

            assertThat(serializer.isTextBased()).isTrue();
        }
    }

    @Nested
    @DisplayName("Required Methods Tests")
    class RequiredMethodsTests {

        @Test
        @DisplayName("serialize is required")
        void serializeIsRequired() {
            Serializer serializer = new TestMinimalSerializer();

            byte[] result = serializer.serialize("test");

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("deserialize with Class is required")
        void deserializeWithClassIsRequired() {
            Serializer serializer = new TestMinimalSerializer();

            String result = serializer.deserialize(new byte[0], String.class);

            // TestMinimalSerializer returns null
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("deserialize with TypeReference is required")
        void deserializeWithTypeReferenceIsRequired() {
            Serializer serializer = new TestMinimalSerializer();
            TypeReference<String> typeRef = new TypeReference<>() {};

            String result = serializer.deserialize(new byte[0], typeRef);

            // TestMinimalSerializer returns null
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("deserialize with Type is required")
        void deserializeWithTypeIsRequired() {
            Serializer serializer = new TestMinimalSerializer();

            String result = serializer.deserialize(new byte[0], (Type) String.class);

            // TestMinimalSerializer returns null
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("getFormat is required")
        void getFormatIsRequired() {
            Serializer serializer = new TestMinimalSerializer();

            assertThat(serializer.getFormat()).isEqualTo("test");
        }
    }

    // Minimal implementation with only required methods
    static class TestMinimalSerializer implements Serializer {
        @Override
        public byte[] serialize(Object obj) {
            return new byte[0];
        }

        @Override
        public <T> T deserialize(byte[] data, Class<T> type) {
            return null;
        }

        @Override
        public <T> T deserialize(byte[] data, TypeReference<T> typeRef) {
            return null;
        }

        @Override
        public <T> T deserialize(byte[] data, Type type) {
            return null;
        }

        @Override
        public String getFormat() {
            return "test";
        }
    }

    // Custom implementation that overrides default methods
    static class TestCustomSerializer implements Serializer {
        @Override
        public byte[] serialize(Object obj) {
            return new byte[0];
        }

        @Override
        public <T> T deserialize(byte[] data, Class<T> type) {
            return null;
        }

        @Override
        public <T> T deserialize(byte[] data, TypeReference<T> typeRef) {
            return null;
        }

        @Override
        public <T> T deserialize(byte[] data, Type type) {
            return null;
        }

        @Override
        public String getFormat() {
            return "custom";
        }

        @Override
        public String getMimeType() {
            return "application/custom";
        }

        @Override
        public boolean supports(Class<?> type) {
            return type == String.class;
        }

        @Override
        public boolean isTextBased() {
            return true;
        }
    }
}
