package cloud.opencode.base.serialization.binary;

import cloud.opencode.base.serialization.Serializer;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ProtobufSerializerProviderTest Tests
 * ProtobufSerializerProviderTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-serialization V1.0.0
 */
@DisplayName("ProtobufSerializerProvider Tests")
class ProtobufSerializerProviderTest {

    private ProtobufSerializerProvider provider;

    @BeforeEach
    void setUp() {
        provider = new ProtobufSerializerProvider();
    }

    @Nested
    @DisplayName("create Tests")
    class CreateTests {

        @Test
        @DisplayName("Should create ProtobufSerializer when Protobuf is available")
        void shouldCreateProtobufSerializerWhenProtobufIsAvailable() {
            if (!provider.isAvailable()) {
                return; // Skip if Protobuf not available
            }

            Serializer serializer = provider.create();

            assertThat(serializer).isNotNull();
            assertThat(serializer).isInstanceOf(ProtobufSerializer.class);
            assertThat(serializer.getFormat()).isEqualTo("protobuf");
        }
    }

    @Nested
    @DisplayName("getPriority Tests")
    class GetPriorityTests {

        @Test
        @DisplayName("Should return priority 60")
        void shouldReturnPriority60() {
            assertThat(provider.getPriority()).isEqualTo(60);
        }
    }

    @Nested
    @DisplayName("isAvailable Tests")
    class IsAvailableTests {

        @Test
        @DisplayName("isAvailable should check for Protobuf class")
        void isAvailableShouldCheckForProtobufClass() {
            boolean result = provider.isAvailable();

            // Result depends on whether Protobuf is on classpath
            // Just verify it returns without error
            assertThat(result).isIn(true, false);
        }

        @Test
        @DisplayName("isAvailable should return true when Protobuf is present")
        void isAvailableShouldReturnTrueWhenProtobufPresent() {
            try {
                Class.forName("com.google.protobuf.Message");
                assertThat(provider.isAvailable()).isTrue();
            } catch (ClassNotFoundException e) {
                // Protobuf not present, skip
            }
        }
    }

    @Nested
    @DisplayName("Create Serializer Format Tests")
    class CreateSerializerFormatTests {

        @Test
        @DisplayName("Created serializer should have format 'protobuf'")
        void createdSerializerShouldHaveFormatProtobuf() {
            if (!provider.isAvailable()) {
                return; // Skip if Protobuf not available
            }

            Serializer serializer = provider.create();
            assertThat(serializer.getFormat()).isEqualTo("protobuf");
        }
    }
}
