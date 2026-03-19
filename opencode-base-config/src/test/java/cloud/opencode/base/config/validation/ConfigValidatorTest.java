package cloud.opencode.base.config.validation;

import cloud.opencode.base.config.Config;
import cloud.opencode.base.config.ConfigBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for ConfigValidator functional interface.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("ConfigValidator Tests")
class ConfigValidatorTest {

    @Nested
    @DisplayName("Functional Interface Tests")
    class FunctionalInterfaceTests {

        @Test
        @DisplayName("ConfigValidator is a functional interface")
        void testIsFunctionalInterface() {
            assertThat(ConfigValidator.class.isAnnotationPresent(FunctionalInterface.class))
                    .isTrue();
        }

        @Test
        @DisplayName("ConfigValidator can be implemented as lambda")
        void testLambdaImplementation() {
            ConfigValidator validator = config -> ValidationResult.valid();

            Config config = new ConfigBuilder()
                    .addProperties(Map.of("key", "value"))
                    .disablePlaceholders()
                    .build();

            ValidationResult result = validator.validate(config);
            assertThat(result.isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("Custom Validator Tests")
    class CustomValidatorTests {

        @Test
        @DisplayName("validator returns valid for valid config")
        void testValidConfig() {
            ConfigValidator portValidator = config -> {
                int port = config.getInt("server.port", 0);
                if (port >= 1024 && port <= 65535) {
                    return ValidationResult.valid();
                }
                return ValidationResult.invalid("Port must be between 1024-65535");
            };

            Config config = new ConfigBuilder()
                    .addProperties(Map.of("server.port", "8080"))
                    .disablePlaceholders()
                    .build();

            assertThat(portValidator.validate(config).isValid()).isTrue();
        }

        @Test
        @DisplayName("validator returns invalid for invalid config")
        void testInvalidConfig() {
            ConfigValidator portValidator = config -> {
                int port = config.getInt("server.port", 0);
                if (port >= 1024 && port <= 65535) {
                    return ValidationResult.valid();
                }
                return ValidationResult.invalid("Port must be between 1024-65535");
            };

            Config config = new ConfigBuilder()
                    .addProperties(Map.of("server.port", "80"))
                    .disablePlaceholders()
                    .build();

            ValidationResult result = portValidator.validate(config);
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).contains("Port must be between 1024-65535");
        }

        @Test
        @DisplayName("validator checks multiple conditions")
        void testMultipleConditions() {
            ConfigValidator multiValidator = config -> {
                var errors = new java.util.ArrayList<String>();
                if (!config.hasKey("db.url")) {
                    errors.add("Missing db.url");
                }
                if (!config.hasKey("db.user")) {
                    errors.add("Missing db.user");
                }
                return errors.isEmpty()
                        ? ValidationResult.valid()
                        : ValidationResult.invalid(errors);
            };

            Config config = new ConfigBuilder()
                    .addProperties(Map.of("db.url", "jdbc:mysql://localhost"))
                    .disablePlaceholders()
                    .build();

            ValidationResult result = multiValidator.validate(config);
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).containsExactly("Missing db.user");
        }
    }

    @Nested
    @DisplayName("Integration with ConfigBuilder Tests")
    class IntegrationTests {

        @Test
        @DisplayName("validator can be used in ConfigBuilder")
        void testWithConfigBuilder() {
            ConfigValidator alwaysValid = config -> ValidationResult.valid();

            Config config = new ConfigBuilder()
                    .addProperties(Map.of("key", "value"))
                    .addValidator(alwaysValid)
                    .build();

            assertThat(config).isNotNull();
        }
    }
}
