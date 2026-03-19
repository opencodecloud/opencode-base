package cloud.opencode.base.serialization.binary;

import cloud.opencode.base.serialization.Serializer;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * JdkSerializerProviderTest Tests
 * JdkSerializerProviderTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-serialization V1.0.0
 */
@DisplayName("JdkSerializerProvider Tests")
class JdkSerializerProviderTest {

    private JdkSerializerProvider provider;

    @BeforeEach
    void setUp() {
        provider = new JdkSerializerProvider();
    }

    @Nested
    @DisplayName("create Tests")
    class CreateTests {

        @Test
        @DisplayName("create should return JdkSerializer instance")
        void createShouldReturnJdkSerializerInstance() {
            Serializer serializer = provider.create();

            assertThat(serializer).isNotNull();
            assertThat(serializer).isInstanceOf(JdkSerializer.class);
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

            assertThat(serializer.getFormat()).isEqualTo("jdk");
        }
    }

    @Nested
    @DisplayName("getPriority Tests")
    class GetPriorityTests {

        @Test
        @DisplayName("getPriority should return 1000")
        void getPriorityShouldReturn1000() {
            assertThat(provider.getPriority()).isEqualTo(1000);
        }

        @Test
        @DisplayName("getPriority should be lowest among serializers")
        void getPriorityShouldBeLowest() {
            // JDK serializer should have lowest priority (highest number)
            // as it's the fallback serializer
            assertThat(provider.getPriority()).isGreaterThan(100);
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
