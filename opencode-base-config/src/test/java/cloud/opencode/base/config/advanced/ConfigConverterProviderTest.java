package cloud.opencode.base.config.advanced;

import cloud.opencode.base.config.converter.ConfigConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for ConfigConverterProvider SPI.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("ConfigConverterProvider Tests")
class ConfigConverterProviderTest {

    @Nested
    @DisplayName("Interface Contract Tests")
    class InterfaceContractTests {

        @Test
        @DisplayName("implementation can provide supported type")
        void testSupportedType() {
            ConfigConverterProvider provider = new TestConverterProvider();
            assertThat(provider.supportedType()).isEqualTo(StringBuilder.class);
        }

        @Test
        @DisplayName("implementation can create converter")
        void testCreate() {
            ConfigConverterProvider provider = new TestConverterProvider();
            ConfigConverter<?> converter = provider.create();
            assertThat(converter).isNotNull();
        }

        @Test
        @DisplayName("created converter converts correctly")
        void testConverterWorks() {
            ConfigConverterProvider provider = new TestConverterProvider();
            @SuppressWarnings("unchecked")
            ConfigConverter<StringBuilder> converter = (ConfigConverter<StringBuilder>) provider.create();
            StringBuilder result = converter.convert("test");
            assertThat(result.toString()).isEqualTo("test");
        }
    }

    @Nested
    @DisplayName("Default Priority Tests")
    class DefaultPriorityTests {

        @Test
        @DisplayName("default priority is 0")
        void testDefaultPriority() {
            ConfigConverterProvider provider = new TestConverterProvider();
            assertThat(provider.priority()).isEqualTo(0);
        }

        @Test
        @DisplayName("custom priority overrides default")
        void testCustomPriority() {
            ConfigConverterProvider provider = new ConfigConverterProvider() {
                @Override
                public Class<?> supportedType() { return String.class; }
                @Override
                public ConfigConverter<?> create() { return s -> s; }
                @Override
                public int priority() { return 100; }
            };
            assertThat(provider.priority()).isEqualTo(100);
        }
    }

    // Test helper
    static class TestConverterProvider implements ConfigConverterProvider {
        @Override
        public Class<?> supportedType() {
            return StringBuilder.class;
        }

        @Override
        public ConfigConverter<?> create() {
            return (ConfigConverter<StringBuilder>) StringBuilder::new;
        }
    }
}
