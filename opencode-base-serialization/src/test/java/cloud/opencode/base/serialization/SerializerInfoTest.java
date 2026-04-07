package cloud.opencode.base.serialization;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * SerializerInfoTest Tests
 * SerializerInfoTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-serialization V1.0.3
 */
@DisplayName("SerializerInfo Tests")
class SerializerInfoTest {

    @Nested
    @DisplayName("Constructor and Accessor Tests")
    class ConstructorAndAccessorTests {

        @Test
        @DisplayName("should create record with all fields")
        void shouldCreateRecordWithAllFields() {
            SerializerInfo info = new SerializerInfo(
                    "json", "application/json", true, true, false, "JSON serializer");

            assertThat(info.format()).isEqualTo("json");
            assertThat(info.mimeType()).isEqualTo("application/json");
            assertThat(info.textBased()).isTrue();
            assertThat(info.supportsStreaming()).isTrue();
            assertThat(info.supportsCompression()).isFalse();
            assertThat(info.description()).isEqualTo("JSON serializer");
        }

        @Test
        @DisplayName("should create binary serializer info")
        void shouldCreateBinarySerializerInfo() {
            SerializerInfo info = new SerializerInfo(
                    "jdk", "application/octet-stream", false, false, true, "JDK serializer");

            assertThat(info.format()).isEqualTo("jdk");
            assertThat(info.mimeType()).isEqualTo("application/octet-stream");
            assertThat(info.textBased()).isFalse();
            assertThat(info.supportsStreaming()).isFalse();
            assertThat(info.supportsCompression()).isTrue();
        }

        @Test
        @DisplayName("should create XML serializer info")
        void shouldCreateXmlSerializerInfo() {
            SerializerInfo info = new SerializerInfo(
                    "xml", "application/xml", true, true, true, "XML serializer");

            assertThat(info.format()).isEqualTo("xml");
            assertThat(info.mimeType()).isEqualTo("application/xml");
            assertThat(info.textBased()).isTrue();
            assertThat(info.supportsStreaming()).isTrue();
            assertThat(info.supportsCompression()).isTrue();
        }
    }

    @Nested
    @DisplayName("equals and hashCode Tests")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("should be equal to itself")
        void shouldBeEqualToItself() {
            SerializerInfo info = new SerializerInfo(
                    "json", "application/json", true, true, false, "JSON");

            assertThat(info).isEqualTo(info);
        }

        @Test
        @DisplayName("should be equal to another with same values")
        void shouldBeEqualToAnotherWithSameValues() {
            SerializerInfo info1 = new SerializerInfo(
                    "json", "application/json", true, true, false, "JSON");
            SerializerInfo info2 = new SerializerInfo(
                    "json", "application/json", true, true, false, "JSON");

            assertThat(info1).isEqualTo(info2);
            assertThat(info1.hashCode()).isEqualTo(info2.hashCode());
        }

        @Test
        @DisplayName("should not be equal when format differs")
        void shouldNotBeEqualWhenFormatDiffers() {
            SerializerInfo info1 = new SerializerInfo(
                    "json", "application/json", true, true, false, "JSON");
            SerializerInfo info2 = new SerializerInfo(
                    "xml", "application/json", true, true, false, "JSON");

            assertThat(info1).isNotEqualTo(info2);
        }

        @Test
        @DisplayName("should not be equal when mimeType differs")
        void shouldNotBeEqualWhenMimeTypeDiffers() {
            SerializerInfo info1 = new SerializerInfo(
                    "json", "application/json", true, true, false, "JSON");
            SerializerInfo info2 = new SerializerInfo(
                    "json", "text/json", true, true, false, "JSON");

            assertThat(info1).isNotEqualTo(info2);
        }

        @Test
        @DisplayName("should not be equal when textBased differs")
        void shouldNotBeEqualWhenTextBasedDiffers() {
            SerializerInfo info1 = new SerializerInfo(
                    "json", "application/json", true, true, false, "JSON");
            SerializerInfo info2 = new SerializerInfo(
                    "json", "application/json", false, true, false, "JSON");

            assertThat(info1).isNotEqualTo(info2);
        }

        @Test
        @DisplayName("should not be equal to null")
        void shouldNotBeEqualToNull() {
            SerializerInfo info = new SerializerInfo(
                    "json", "application/json", true, true, false, "JSON");

            assertThat(info).isNotEqualTo(null);
        }

        @Test
        @DisplayName("should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            SerializerInfo info = new SerializerInfo(
                    "json", "application/json", true, true, false, "JSON");

            assertThat(info).isNotEqualTo("json");
        }
    }

    @Nested
    @DisplayName("toString Tests")
    class ToStringTests {

        @Test
        @DisplayName("toString should contain all field values")
        void toStringShouldContainAllFieldValues() {
            SerializerInfo info = new SerializerInfo(
                    "json", "application/json", true, true, false, "JSON serializer");

            String str = info.toString();

            assertThat(str).contains("SerializerInfo");
            assertThat(str).contains("json");
            assertThat(str).contains("application/json");
            assertThat(str).contains("JSON serializer");
        }

        @Test
        @DisplayName("toString should contain boolean field values")
        void toStringShouldContainBooleanFieldValues() {
            SerializerInfo info = new SerializerInfo(
                    "jdk", "application/octet-stream", false, false, true, "JDK");

            String str = info.toString();

            assertThat(str).contains("false");
            assertThat(str).contains("true");
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle null description")
        void shouldHandleNullDescription() {
            SerializerInfo info = new SerializerInfo(
                    "json", "application/json", true, false, false, null);

            assertThat(info.description()).isEmpty();
        }

        @Test
        @DisplayName("should handle empty strings")
        void shouldHandleEmptyStrings() {
            SerializerInfo info = new SerializerInfo("", "", true, false, false, "");

            assertThat(info.format()).isEmpty();
            assertThat(info.mimeType()).isEmpty();
            assertThat(info.description()).isEmpty();
        }

        @Test
        @DisplayName("should handle all false boolean flags")
        void shouldHandleAllFalseBooleanFlags() {
            SerializerInfo info = new SerializerInfo(
                    "custom", "application/custom", false, false, false, "Custom");

            assertThat(info.textBased()).isFalse();
            assertThat(info.supportsStreaming()).isFalse();
            assertThat(info.supportsCompression()).isFalse();
        }

        @Test
        @DisplayName("should handle all true boolean flags")
        void shouldHandleAllTrueBooleanFlags() {
            SerializerInfo info = new SerializerInfo(
                    "full", "application/full", true, true, true, "Full");

            assertThat(info.textBased()).isTrue();
            assertThat(info.supportsStreaming()).isTrue();
            assertThat(info.supportsCompression()).isTrue();
        }
    }
}
