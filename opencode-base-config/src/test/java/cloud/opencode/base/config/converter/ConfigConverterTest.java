package cloud.opencode.base.config.converter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for ConfigConverter functional interface.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("ConfigConverter Tests")
class ConfigConverterTest {

    @Nested
    @DisplayName("Functional Interface Tests")
    class FunctionalInterfaceTests {

        @Test
        @DisplayName("ConfigConverter is a functional interface")
        void testIsFunctionalInterface() {
            assertThat(ConfigConverter.class.isAnnotationPresent(FunctionalInterface.class))
                    .isTrue();
        }

        @Test
        @DisplayName("ConfigConverter can be implemented as lambda")
        void testLambdaImplementation() {
            ConfigConverter<Integer> converter = Integer::parseInt;
            assertThat(converter.convert("42")).isEqualTo(42);
        }

        @Test
        @DisplayName("ConfigConverter can be implemented as method reference")
        void testMethodReference() {
            ConfigConverter<String> converter = String::trim;
            assertThat(converter.convert("  hello  ")).isEqualTo("hello");
        }
    }

    @Nested
    @DisplayName("Default getType Method Tests")
    class GetTypeTests {

        @Test
        @DisplayName("default getType returns null")
        void testDefaultGetTypeReturnsNull() {
            ConfigConverter<String> converter = s -> s;
            assertThat(converter.getType()).isNull();
        }

        @Test
        @DisplayName("overridden getType returns correct type")
        void testOverriddenGetType() {
            ConfigConverter<Integer> converter = new ConfigConverter<>() {
                @Override
                public Integer convert(String value) {
                    return Integer.parseInt(value);
                }

                @Override
                public Class<Integer> getType() {
                    return Integer.class;
                }
            };
            assertThat(converter.getType()).isEqualTo(Integer.class);
        }
    }
}
