package cloud.opencode.base.serialization.json;

import cloud.opencode.base.serialization.Serializer;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * JsonSerializerProviderTest Tests
 * JsonSerializerProviderTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-serialization V1.0.0
 */
@DisplayName("JsonSerializerProvider Tests")
class JsonSerializerProviderTest {

    private JsonSerializerProvider provider;

    @BeforeEach
    void setUp() {
        provider = new JsonSerializerProvider();
    }

    @Nested
    @DisplayName("create Tests")
    class CreateTests {

        @Test
        @DisplayName("create should return JsonSerializer instance")
        void createShouldReturnJsonSerializerInstance() {
            Serializer serializer = provider.create();

            assertThat(serializer).isNotNull();
            assertThat(serializer).isInstanceOf(JsonSerializer.class);
        }

        @Test
        @DisplayName("create should return new instance each time")
        void createShouldReturnNewInstanceEachTime() {
            Serializer serializer1 = provider.create();
            Serializer serializer2 = provider.create();

            assertThat(serializer1).isNotSameAs(serializer2);
        }

        @Test
        @DisplayName("created serializer should have correct format")
        void createdSerializerShouldHaveCorrectFormat() {
            Serializer serializer = provider.create();

            assertThat(serializer.getFormat()).isEqualTo("json");
        }
    }

    @Nested
    @DisplayName("getPriority Tests")
    class GetPriorityTests {

        @Test
        @DisplayName("getPriority should return 10")
        void getPriorityShouldReturn10() {
            assertThat(provider.getPriority()).isEqualTo(10);
        }

        @Test
        @DisplayName("getPriority should be highest priority (lowest number)")
        void getPriorityShouldBeHighestPriority() {
            // JSON serializer should have highest priority (lowest number)
            // as it's the default serializer
            assertThat(provider.getPriority()).isLessThan(100);
        }
    }

    @Nested
    @DisplayName("isAvailable Tests")
    class IsAvailableTests {

        @Test
        @DisplayName("isAvailable should return true by default")
        void isAvailableShouldReturnTrueByDefault() {
            // Default implementation returns true
            assertThat(provider.isAvailable()).isTrue();
        }
    }

    @Nested
    @DisplayName("SerializerProvider Interface Tests")
    class SerializerProviderInterfaceTests {

        @Test
        @DisplayName("Should implement SerializerProvider interface")
        void shouldImplementSerializerProviderInterface() {
            assertThat(provider).isInstanceOf(cloud.opencode.base.serialization.spi.SerializerProvider.class);
        }
    }
}
