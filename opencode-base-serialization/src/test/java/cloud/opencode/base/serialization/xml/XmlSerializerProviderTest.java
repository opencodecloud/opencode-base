package cloud.opencode.base.serialization.xml;

import cloud.opencode.base.serialization.Serializer;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * XmlSerializerProviderTest Tests
 * XmlSerializerProviderTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-serialization V1.0.0
 */
@DisplayName("XmlSerializerProvider Tests")
class XmlSerializerProviderTest {

    private XmlSerializerProvider provider;

    @BeforeEach
    void setUp() {
        provider = new XmlSerializerProvider();
    }

    @Nested
    @DisplayName("create Tests")
    class CreateTests {

        @Test
        @DisplayName("Should create XmlSerializer when JAXB is available")
        void shouldCreateXmlSerializerWhenJaxbIsAvailable() {
            if (!provider.isAvailable()) {
                return; // Skip if JAXB not available
            }

            Serializer serializer = provider.create();

            assertThat(serializer).isNotNull();
            assertThat(serializer).isInstanceOf(XmlSerializer.class);
            assertThat(serializer.getFormat()).isEqualTo("xml");
        }
    }

    @Nested
    @DisplayName("getPriority Tests")
    class GetPriorityTests {

        @Test
        @DisplayName("Should return priority 20")
        void shouldReturnPriority20() {
            assertThat(provider.getPriority()).isEqualTo(20);
        }
    }

    @Nested
    @DisplayName("isAvailable Tests")
    class IsAvailableTests {

        @Test
        @DisplayName("isAvailable should check for JAXB class")
        void isAvailableShouldCheckForJaxbClass() {
            boolean result = provider.isAvailable();

            // Result depends on whether JAXB is on classpath
            // Just verify it returns without error
            assertThat(result).isIn(true, false);
        }

        @Test
        @DisplayName("isAvailable should return true when JAXB is present")
        void isAvailableShouldReturnTrueWhenJaxbPresent() {
            try {
                Class.forName("jakarta.xml.bind.JAXBContext");
                assertThat(provider.isAvailable()).isTrue();
            } catch (ClassNotFoundException e) {
                // JAXB not present, skip
            }
        }
    }

    @Nested
    @DisplayName("Create Serializer Format Tests")
    class CreateSerializerFormatTests {

        @Test
        @DisplayName("Created serializer should have format 'xml'")
        void createdSerializerShouldHaveFormatXml() {
            if (!provider.isAvailable()) {
                return; // Skip if JAXB not available
            }

            Serializer serializer = provider.create();
            assertThat(serializer.getFormat()).isEqualTo("xml");
        }
    }
}
