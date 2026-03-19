package cloud.opencode.base.serialization.spi;

import cloud.opencode.base.serialization.Serializer;
import cloud.opencode.base.serialization.TypeReference;
import org.junit.jupiter.api.*;

import java.lang.reflect.Type;

import static org.assertj.core.api.Assertions.*;

/**
 * SerializerProviderTest Tests
 * SerializerProviderTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-serialization V1.0.0
 */
@DisplayName("SerializerProvider Tests")
class SerializerProviderTest {

    @Nested
    @DisplayName("Default Method Tests")
    class DefaultMethodTests {

        @Test
        @DisplayName("getPriority should return 100 by default")
        void getPriorityShouldReturn100ByDefault() {
            SerializerProvider provider = new TestSerializerProvider();

            assertThat(provider.getPriority()).isEqualTo(100);
        }

        @Test
        @DisplayName("isAvailable should return true by default")
        void isAvailableShouldReturnTrueByDefault() {
            SerializerProvider provider = new TestSerializerProvider();

            assertThat(provider.isAvailable()).isTrue();
        }
    }

    @Nested
    @DisplayName("Custom Implementation Tests")
    class CustomImplementationTests {

        @Test
        @DisplayName("Custom provider can override getPriority")
        void customProviderCanOverrideGetPriority() {
            SerializerProvider provider = new SerializerProvider() {
                @Override
                public Serializer create() {
                    return new TestSerializer();
                }

                @Override
                public int getPriority() {
                    return 50;
                }
            };

            assertThat(provider.getPriority()).isEqualTo(50);
        }

        @Test
        @DisplayName("Custom provider can override isAvailable")
        void customProviderCanOverrideIsAvailable() {
            SerializerProvider provider = new SerializerProvider() {
                @Override
                public Serializer create() {
                    return new TestSerializer();
                }

                @Override
                public boolean isAvailable() {
                    return false;
                }
            };

            assertThat(provider.isAvailable()).isFalse();
        }
    }

    @Nested
    @DisplayName("create Method Tests")
    class CreateMethodTests {

        @Test
        @DisplayName("create should return a Serializer")
        void createShouldReturnASerializer() {
            SerializerProvider provider = new TestSerializerProvider();

            Serializer serializer = provider.create();

            assertThat(serializer).isNotNull();
            assertThat(serializer).isInstanceOf(TestSerializer.class);
        }
    }

    // Test implementations
    private static class TestSerializerProvider implements SerializerProvider {
        @Override
        public Serializer create() {
            return new TestSerializer();
        }
    }

    private static class TestSerializer implements Serializer {
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
}
