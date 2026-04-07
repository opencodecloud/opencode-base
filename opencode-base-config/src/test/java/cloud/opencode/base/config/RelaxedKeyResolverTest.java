package cloud.opencode.base.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * RelaxedKeyResolver Test
 * RelaxedKeyResolver 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.3
 */
@DisplayName("RelaxedKeyResolver 测试")
class RelaxedKeyResolverTest {

    @Nested
    @DisplayName("normalize 测试")
    class NormalizeTests {

        @Test
        @DisplayName("kebab-case 归一化")
        void testNormalizeKebabCase() {
            assertThat(RelaxedKeyResolver.normalize("database.max-pool-size"))
                .isEqualTo("databasemaxpoolsize");
        }

        @Test
        @DisplayName("camelCase 归一化")
        void testNormalizeCamelCase() {
            assertThat(RelaxedKeyResolver.normalize("database.maxPoolSize"))
                .isEqualTo("databasemaxpoolsize");
        }

        @Test
        @DisplayName("UPPER_SNAKE 归一化")
        void testNormalizeUpperSnake() {
            assertThat(RelaxedKeyResolver.normalize("DATABASE_MAX_POOL_SIZE"))
                .isEqualTo("databasemaxpoolsize");
        }

        @Test
        @DisplayName("snake_case 归一化")
        void testNormalizeSnakeCase() {
            assertThat(RelaxedKeyResolver.normalize("database.max_pool_size"))
                .isEqualTo("databasemaxpoolsize");
        }

        @Test
        @DisplayName("混合格式归一化")
        void testNormalizeMixed() {
            assertThat(RelaxedKeyResolver.normalize("my-App.maxPool_Size"))
                .isEqualTo("myappmaxpoolsize");
        }

        @Test
        @DisplayName("所有变体归一化到相同结果")
        void testAllVariantsNormalizeToSame() {
            String expected = "databasemaxpoolsize";
            assertThat(RelaxedKeyResolver.normalize("database.max-pool-size")).isEqualTo(expected);
            assertThat(RelaxedKeyResolver.normalize("database.maxPoolSize")).isEqualTo(expected);
            assertThat(RelaxedKeyResolver.normalize("database.max_pool_size")).isEqualTo(expected);
            assertThat(RelaxedKeyResolver.normalize("DATABASE_MAX_POOL_SIZE")).isEqualTo(expected);
            assertThat(RelaxedKeyResolver.normalize("database.maxpoolsize")).isEqualTo(expected);
        }

        @Test
        @DisplayName("空字符串归一化")
        void testNormalizeEmpty() {
            assertThat(RelaxedKeyResolver.normalize("")).isEmpty();
        }

        @Test
        @DisplayName("null 键抛出 NullPointerException")
        void testNormalizeNull() {
            assertThatNullPointerException()
                .isThrownBy(() -> RelaxedKeyResolver.normalize(null));
        }

        @Test
        @DisplayName("简单键归一化")
        void testNormalizeSimpleKey() {
            assertThat(RelaxedKeyResolver.normalize("port")).isEqualTo("port");
        }

        @Test
        @DisplayName("纯数字段归一化")
        void testNormalizeWithDigits() {
            assertThat(RelaxedKeyResolver.normalize("server.http2.max-streams"))
                .isEqualTo("serverhttp2maxstreams");
        }
    }

    @Nested
    @DisplayName("variants 测试")
    class VariantsTests {

        @Test
        @DisplayName("生成 kebab-case 键的所有变体")
        void testVariantsFromKebab() {
            Set<String> variants = RelaxedKeyResolver.variants("database.max-pool-size");

            assertThat(variants).contains(
                "database.max-pool-size",    // kebab-case
                "database.maxPoolSize",      // camelCase
                "database.max_pool_size",    // snake_case
                "DATABASE_MAX_POOL_SIZE",    // UPPER_SNAKE
                "database.maxpoolsize"       // flat lowercase
            );
        }

        @Test
        @DisplayName("生成 camelCase 键的所有变体")
        void testVariantsFromCamelCase() {
            Set<String> variants = RelaxedKeyResolver.variants("database.maxPoolSize");

            assertThat(variants).contains(
                "database.max-pool-size",
                "database.maxPoolSize",
                "database.max_pool_size",
                "DATABASE_MAX_POOL_SIZE",
                "database.maxpoolsize"
            );
        }

        @Test
        @DisplayName("生成 UPPER_SNAKE 键的所有变体")
        void testVariantsFromUpperSnake() {
            // UPPER_SNAKE format treats each underscore as a boundary.
            // Since there's no way to know the original dot placement,
            // each word becomes its own segment.
            Set<String> variants = RelaxedKeyResolver.variants("DATABASE_MAX_POOL_SIZE");

            // Must contain the original form and the flat form
            assertThat(variants).contains("DATABASE_MAX_POOL_SIZE");
            // All variants normalize to the same canonical form
            for (String variant : variants) {
                assertThat(RelaxedKeyResolver.normalize(variant))
                    .isEqualTo("databasemaxpoolsize");
            }
        }

        @Test
        @DisplayName("空字符串变体")
        void testVariantsEmpty() {
            assertThat(RelaxedKeyResolver.variants("")).containsExactly("");
        }

        @Test
        @DisplayName("null 键抛出 NullPointerException")
        void testVariantsNull() {
            assertThatNullPointerException()
                .isThrownBy(() -> RelaxedKeyResolver.variants(null));
        }

        @Test
        @DisplayName("简单键变体")
        void testVariantsSimpleKey() {
            Set<String> variants = RelaxedKeyResolver.variants("port");
            assertThat(variants).contains("port", "PORT");
        }
    }

    @Nested
    @DisplayName("resolve 测试")
    class ResolveTests {

        @Test
        @DisplayName("精确匹配")
        void testResolveExactMatch() {
            Set<String> available = Set.of("database.max-pool-size", "server.port");
            Optional<String> result = RelaxedKeyResolver.resolve("database.max-pool-size", available);
            assertThat(result).hasValue("database.max-pool-size");
        }

        @Test
        @DisplayName("kebab 匹配 camelCase")
        void testResolveKebabToCamelCase() {
            Set<String> available = Set.of("database.maxPoolSize", "server.port");
            Optional<String> result = RelaxedKeyResolver.resolve("database.max-pool-size", available);
            assertThat(result).hasValue("database.maxPoolSize");
        }

        @Test
        @DisplayName("kebab 匹配 UPPER_SNAKE")
        void testResolveKebabToUpperSnake() {
            Set<String> available = Set.of("DATABASE_MAX_POOL_SIZE", "SERVER_PORT");
            Optional<String> result = RelaxedKeyResolver.resolve("database.max-pool-size", available);
            assertThat(result).hasValue("DATABASE_MAX_POOL_SIZE");
        }

        @Test
        @DisplayName("camelCase 匹配 kebab")
        void testResolveCamelToKebab() {
            Set<String> available = Set.of("database.max-pool-size");
            Optional<String> result = RelaxedKeyResolver.resolve("database.maxPoolSize", available);
            assertThat(result).hasValue("database.max-pool-size");
        }

        @Test
        @DisplayName("UPPER_SNAKE 匹配 kebab")
        void testResolveUpperSnakeToKebab() {
            Set<String> available = Set.of("database.max-pool-size");
            Optional<String> result = RelaxedKeyResolver.resolve("DATABASE_MAX_POOL_SIZE", available);
            assertThat(result).hasValue("database.max-pool-size");
        }

        @Test
        @DisplayName("无匹配返回空")
        void testResolveNoMatch() {
            Set<String> available = Set.of("server.port", "app.name");
            Optional<String> result = RelaxedKeyResolver.resolve("database.max-pool-size", available);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("空可用键集合返回空")
        void testResolveEmptyAvailable() {
            Optional<String> result = RelaxedKeyResolver.resolve("app.name", Set.of());
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("null 键抛出 NullPointerException")
        void testResolveNullKey() {
            assertThatNullPointerException()
                .isThrownBy(() -> RelaxedKeyResolver.resolve(null, Set.of()));
        }

        @Test
        @DisplayName("null 可用键集合抛出 NullPointerException")
        void testResolveNullAvailableKeys() {
            assertThatNullPointerException()
                .isThrownBy(() -> RelaxedKeyResolver.resolve("key", null));
        }

        @Test
        @DisplayName("精确匹配优先于宽松匹配")
        void testResolveExactMatchPriority() {
            Set<String> available = Set.of("app.name", "APP_NAME");
            Optional<String> result = RelaxedKeyResolver.resolve("app.name", available);
            assertThat(result).hasValue("app.name");
        }
    }

    @Nested
    @DisplayName("Integration 集成测试")
    class IntegrationTests {

        @Test
        @DisplayName("ConfigBuilder 启用宽松绑定后用 env 风格键访问")
        void testConfigBuilderRelaxedBinding() {
            Config config = OpenConfig.builder()
                .addProperties(Map.of(
                    "database.max-pool-size", "20",
                    "server.port", "8080"
                ))
                .enableRelaxedBinding()
                .build();

            // Access with UPPER_SNAKE style (env var)
            assertThat(config.getString("DATABASE_MAX_POOL_SIZE")).isEqualTo("20");
            // Access with camelCase
            assertThat(config.getString("database.maxPoolSize")).isEqualTo("20");
            // Access with exact key still works
            assertThat(config.getString("server.port")).isEqualTo("8080");
        }

        @Test
        @DisplayName("禁用宽松绑定时非精确键抛出异常")
        void testConfigBuilderWithoutRelaxedBinding() {
            Config config = OpenConfig.builder()
                .addProperties(Map.of("database.max-pool-size", "20"))
                .build();

            assertThatThrownBy(() -> config.getString("DATABASE_MAX_POOL_SIZE"))
                .isInstanceOf(OpenConfigException.class);
        }

        @Test
        @DisplayName("宽松绑定 getInt 测试")
        void testRelaxedBindingGetInt() {
            Config config = OpenConfig.builder()
                .addProperties(Map.of("server.port", "8080"))
                .enableRelaxedBinding()
                .build();

            assertThat(config.getInt("SERVER_PORT")).isEqualTo(8080);
        }

        @Test
        @DisplayName("宽松绑定 getOptional 测试")
        void testRelaxedBindingGetOptional() {
            Config config = OpenConfig.builder()
                .addProperties(Map.of("app.name", "TestApp"))
                .enableRelaxedBinding()
                .build();

            assertThat(config.getOptional("APP_NAME")).hasValue("TestApp");
            assertThat(config.getOptional("app.nonexistent")).isEmpty();
        }

        @Test
        @DisplayName("宽松绑定 getString 默认值测试")
        void testRelaxedBindingGetStringDefault() {
            Config config = OpenConfig.builder()
                .addProperties(Map.of("app.name", "TestApp"))
                .enableRelaxedBinding()
                .build();

            assertThat(config.getString("APP_NAME", "default")).isEqualTo("TestApp");
            assertThat(config.getString("APP_MISSING", "default")).isEqualTo("default");
        }
    }
}
