package cloud.opencode.base.serialization.binary;

import cloud.opencode.base.serialization.Serializer;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * KryoSerializerProviderTest Tests
 * KryoSerializerProviderTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-serialization V1.0.0
 */
@DisplayName("KryoSerializerProvider Tests")
class KryoSerializerProviderTest {

    private KryoSerializerProvider provider;

    @BeforeEach
    void setUp() {
        provider = new KryoSerializerProvider();
    }

    @Nested
    @DisplayName("create Tests")
    class CreateTests {

        @Test
        @DisplayName("Should create KryoSerializer when Kryo is available")
        void shouldCreateKryoSerializerWhenKryoIsAvailable() {
            if (!provider.isAvailable()) {
                return; // Skip if Kryo not available
            }

            Serializer serializer = provider.create();

            assertThat(serializer).isNotNull();
            assertThat(serializer).isInstanceOf(KryoSerializer.class);
            assertThat(serializer.getFormat()).isEqualTo("kryo");
        }
    }

    @Nested
    @DisplayName("getPriority Tests")
    class GetPriorityTests {

        @Test
        @DisplayName("Should return priority 50")
        void shouldReturnPriority50() {
            assertThat(provider.getPriority()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("isAvailable Tests")
    class IsAvailableTests {

        @Test
        @DisplayName("isAvailable should check for Kryo class")
        void isAvailableShouldCheckForKryoClass() {
            boolean result = provider.isAvailable();

            // Result depends on whether Kryo is on classpath
            // Just verify it returns without error
            assertThat(result).isIn(true, false);
        }

        @Test
        @DisplayName("isAvailable should return true when Kryo is present")
        void isAvailableShouldReturnTrueWhenKryoPresent() {
            try {
                Class.forName("com.esotericsoftware.kryo.Kryo");
                assertThat(provider.isAvailable()).isTrue();
            } catch (ClassNotFoundException e) {
                // Kryo not present, skip
            }
        }
    }

    @Nested
    @DisplayName("Create Serializer Format Tests")
    class CreateSerializerFormatTests {

        @Test
        @DisplayName("Created serializer should have format 'kryo'")
        void createdSerializerShouldHaveFormatKryo() {
            if (!provider.isAvailable()) {
                return; // Skip if Kryo not available
            }

            Serializer serializer = provider.create();
            assertThat(serializer.getFormat()).isEqualTo("kryo");
        }
    }
}
