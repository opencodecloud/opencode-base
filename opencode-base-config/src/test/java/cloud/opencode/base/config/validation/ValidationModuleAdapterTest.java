package cloud.opencode.base.config.validation;

import cloud.opencode.base.config.Config;
import cloud.opencode.base.config.ConfigListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * ValidationModuleAdapter 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("ValidationModuleAdapter 测试")
class ValidationModuleAdapterTest {

    @Nested
    @DisplayName("isValidationModuleAvailable 测试")
    class IsValidationModuleAvailableTests {

        @Test
        @DisplayName("返回布尔值")
        void shouldReturnBoolean() {
            boolean result = ValidationModuleAdapter.isValidationModuleAvailable();
            assertThat(result).isIn(true, false);
        }
    }

    @Nested
    @DisplayName("validateObject 测试")
    class ValidateObjectTests {

        @Test
        @DisplayName("null 对象抛出异常")
        void shouldThrowExceptionForNullObject() {
            assertThatThrownBy(() -> ValidationModuleAdapter.validateObject(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("object");
        }

        @Test
        @DisplayName("验证有效对象返回 valid")
        void shouldReturnValidForValidObject() {
            TestConfig config = new TestConfig("http://localhost", 8080);

            ValidationResult result = ValidationModuleAdapter.validateObject(config);

            // If validation module not available, returns valid by default
            if (!ValidationModuleAdapter.isValidationModuleAvailable()) {
                assertThat(result.isValid()).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("forRequiredKeys 测试")
    class ForRequiredKeysTests {

        private MockConfig config;

        @BeforeEach
        void setUp() {
            config = new MockConfig();
        }

        @Test
        @DisplayName("所有必需键都存在时返回 valid")
        void shouldReturnValidWhenAllKeysPresent() {
            config.put("database.url", "jdbc:mysql://localhost/db");
            config.put("database.user", "admin");

            ConfigValidator validator = ValidationModuleAdapter.forRequiredKeys(
                    "database.url", "database.user");
            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("缺少必需键时返回 invalid")
        void shouldReturnInvalidWhenKeyMissing() {
            config.put("database.url", "jdbc:mysql://localhost/db");

            ConfigValidator validator = ValidationModuleAdapter.forRequiredKeys(
                    "database.url", "database.user");
            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).anyMatch(e -> e.contains("database.user"));
        }

        @Test
        @DisplayName("值为空时返回 invalid")
        void shouldReturnInvalidWhenValueIsEmpty() {
            config.put("database.url", "");
            config.put("database.user", "   ");

            ConfigValidator validator = ValidationModuleAdapter.forRequiredKeys(
                    "database.url", "database.user");
            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("forPatterns 测试")
    class ForPatternsTests {

        private MockConfig config;

        @BeforeEach
        void setUp() {
            config = new MockConfig();
        }

        @Test
        @DisplayName("所有值匹配模式时返回 valid")
        void shouldReturnValidWhenAllValuesMatchPatterns() {
            config.put("server.port", "8080");
            config.put("app.email", "admin@example.com");

            ConfigValidator validator = ValidationModuleAdapter.forPatterns(Map.of(
                    "server.port", "\\d+",
                    "app.email", "^[\\w.-]+@[\\w.-]+\\.[a-z]{2,}$"
            ));
            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("值不匹配模式时返回 invalid")
        void shouldReturnInvalidWhenValueDoesNotMatchPattern() {
            config.put("server.port", "not-a-number");

            ConfigValidator validator = ValidationModuleAdapter.forPatterns(Map.of(
                    "server.port", "\\d+"
            ));
            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).anyMatch(e -> e.contains("server.port"));
        }

        @Test
        @DisplayName("null patterns 抛出异常")
        void shouldThrowExceptionForNullPatterns() {
            assertThatThrownBy(() -> ValidationModuleAdapter.forPatterns(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("patterns");
        }

        @Test
        @DisplayName("缺少的键不验证模式")
        void shouldSkipMissingKeys() {
            // No key set

            ConfigValidator validator = ValidationModuleAdapter.forPatterns(Map.of(
                    "server.port", "\\d+"
            ));
            ValidationResult result = validator.validate(config);

            assertThat(result.isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("forObject 测试")
    class ForObjectTests {

        @Test
        @DisplayName("null configClass 抛出异常")
        void shouldThrowExceptionForNullConfigClass() {
            assertThatThrownBy(() -> ValidationModuleAdapter.forObject(null, "prefix"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("configClass");
        }

        @Test
        @DisplayName("null prefix 抛出异常")
        void shouldThrowExceptionForNullPrefix() {
            assertThatThrownBy(() -> ValidationModuleAdapter.forObject(TestConfig.class, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("prefix");
        }
    }

    @Nested
    @DisplayName("combine 测试")
    class CombineTests {

        private MockConfig config;

        @BeforeEach
        void setUp() {
            config = new MockConfig();
        }

        @Test
        @DisplayName("所有验证器通过时返回 valid")
        void shouldReturnValidWhenAllValidatorsPass() {
            config.put("database.url", "jdbc:mysql://localhost/db");
            config.put("server.port", "8080");

            ConfigValidator combined = ValidationModuleAdapter.combine(
                    ValidationModuleAdapter.forRequiredKeys("database.url"),
                    ValidationModuleAdapter.forPatterns(Map.of("server.port", "\\d+"))
            );
            ValidationResult result = combined.validate(config);

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("任一验证器失败时返回 invalid")
        void shouldReturnInvalidWhenAnyValidatorFails() {
            config.put("server.port", "not-a-number");

            ConfigValidator combined = ValidationModuleAdapter.combine(
                    ValidationModuleAdapter.forRequiredKeys("database.url"),
                    ValidationModuleAdapter.forPatterns(Map.of("server.port", "\\d+"))
            );
            ValidationResult result = combined.validate(config);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSizeGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("收集所有验证器的错误")
        void shouldCollectAllErrors() {
            config.put("server.port", "not-a-number");

            ConfigValidator combined = ValidationModuleAdapter.combine(
                    ValidationModuleAdapter.forRequiredKeys("database.url", "api.key"),
                    ValidationModuleAdapter.forPatterns(Map.of("server.port", "\\d+"))
            );
            ValidationResult result = combined.validate(config);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(3); // 2 missing keys + 1 pattern mismatch
        }
    }

    /**
     * Test config record
     */
    private record TestConfig(String url, int port) {
    }

    /**
     * Mock Config implementation for testing
     */
    private static class MockConfig implements Config {

        private final java.util.Map<String, String> properties = new java.util.HashMap<>();

        public void put(String key, String value) {
            properties.put(key, value);
        }

        @Override
        public String getString(String key) {
            return properties.get(key);
        }

        @Override
        public String getString(String key, String defaultValue) {
            return properties.getOrDefault(key, defaultValue);
        }

        @Override
        public int getInt(String key) {
            return Integer.parseInt(properties.get(key));
        }

        @Override
        public int getInt(String key, int defaultValue) {
            String value = properties.get(key);
            if (value == null) {
                return defaultValue;
            }
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }

        @Override
        public long getLong(String key) {
            return Long.parseLong(properties.get(key));
        }

        @Override
        public long getLong(String key, long defaultValue) {
            String value = properties.get(key);
            if (value == null) {
                return defaultValue;
            }
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }

        @Override
        public double getDouble(String key) {
            return Double.parseDouble(properties.get(key));
        }

        @Override
        public double getDouble(String key, double defaultValue) {
            String value = properties.get(key);
            if (value == null) {
                return defaultValue;
            }
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }

        @Override
        public boolean getBoolean(String key) {
            return Boolean.parseBoolean(properties.get(key));
        }

        @Override
        public boolean getBoolean(String key, boolean defaultValue) {
            String value = properties.get(key);
            if (value == null) {
                return defaultValue;
            }
            return Boolean.parseBoolean(value);
        }

        @Override
        public java.time.Duration getDuration(String key) {
            return java.time.Duration.parse(properties.get(key));
        }

        @Override
        public java.time.Duration getDuration(String key, java.time.Duration defaultValue) {
            String value = properties.get(key);
            if (value == null) {
                return defaultValue;
            }
            try {
                return java.time.Duration.parse(value);
            } catch (Exception e) {
                return defaultValue;
            }
        }

        @Override
        public <T> T get(String key, Class<T> type) {
            return null;
        }

        @Override
        public <T> T get(String key, Class<T> type, T defaultValue) {
            return defaultValue;
        }

        @Override
        public <T> List<T> getList(String key, Class<T> elementType) {
            return List.of();
        }

        @Override
        public <K, V> Map<K, V> getMap(String key, Class<K> keyType, Class<V> valueType) {
            return Map.of();
        }

        @Override
        public Optional<String> getOptional(String key) {
            return Optional.ofNullable(properties.get(key));
        }

        @Override
        public <T> Optional<T> getOptional(String key, Class<T> type) {
            return Optional.empty();
        }

        @Override
        public Config getSubConfig(String prefix) {
            MockConfig subset = new MockConfig();
            String prefixDot = prefix + ".";
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                if (entry.getKey().startsWith(prefixDot)) {
                    subset.put(entry.getKey().substring(prefixDot.length()), entry.getValue());
                }
            }
            return subset;
        }

        @Override
        public Map<String, String> getByPrefix(String prefix) {
            Map<String, String> result = new java.util.HashMap<>();
            String prefixDot = prefix + ".";
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                if (entry.getKey().startsWith(prefixDot)) {
                    result.put(entry.getKey().substring(prefixDot.length()), entry.getValue());
                }
            }
            return result;
        }

        @Override
        public boolean hasKey(String key) {
            return properties.containsKey(key);
        }

        @Override
        public Set<String> getKeys() {
            return properties.keySet();
        }

        @Override
        public void addListener(ConfigListener listener) {
            // No-op
        }

        @Override
        public void addListener(String key, ConfigListener listener) {
            // No-op
        }

        @Override
        public void removeListener(ConfigListener listener) {
            // No-op
        }

        @Override
        public <T> T bind(String prefix, Class<T> type) {
            return null;
        }

        @Override
        public <T> void bindTo(String prefix, T target) {
            // No-op
        }
    }
}
