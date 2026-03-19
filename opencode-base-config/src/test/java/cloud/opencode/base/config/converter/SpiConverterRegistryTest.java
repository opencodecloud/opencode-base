package cloud.opencode.base.config.converter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for SpiConverterRegistry.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("SpiConverterRegistry Tests")
class SpiConverterRegistryTest {

    @Nested
    @DisplayName("loadFromSpi Tests")
    class LoadFromSpiTests {

        @Test
        @DisplayName("loadFromSpi returns a non-null registry")
        void testLoadFromSpiReturnsRegistry() {
            ConverterRegistry registry = SpiConverterRegistry.loadFromSpi();
            assertThat(registry).isNotNull();
        }

        @Test
        @DisplayName("loadFromSpi registry includes default converters")
        void testIncludesDefaults() {
            ConverterRegistry registry = SpiConverterRegistry.loadFromSpi();
            // Default converters should be present
            assertThat(registry.hasConverter(String.class)).isTrue();
            assertThat(registry.hasConverter(Integer.class)).isTrue();
            assertThat(registry.hasConverter(Long.class)).isTrue();
            assertThat(registry.hasConverter(Boolean.class)).isTrue();
        }

        @Test
        @DisplayName("loadFromSpi registry can convert basic types")
        void testCanConvertBasicTypes() {
            ConverterRegistry registry = SpiConverterRegistry.loadFromSpi();
            assertThat(registry.convert("42", Integer.class)).isEqualTo(42);
            assertThat(registry.convert("hello", String.class)).isEqualTo("hello");
            assertThat(registry.convert("true", Boolean.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("Inheritance Tests")
    class InheritanceTests {

        @Test
        @DisplayName("SpiConverterRegistry extends ConverterRegistry")
        void testExtendsConverterRegistry() {
            assertThat(ConverterRegistry.class.isAssignableFrom(SpiConverterRegistry.class))
                    .isTrue();
        }
    }
}
