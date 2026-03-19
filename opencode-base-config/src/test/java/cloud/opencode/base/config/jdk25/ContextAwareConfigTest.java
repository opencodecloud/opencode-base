package cloud.opencode.base.config.jdk25;

import cloud.opencode.base.config.Config;
import cloud.opencode.base.config.ConfigBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * ContextAwareConfig 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("ContextAwareConfig 测试")
class ContextAwareConfigTest {

    private Config baseConfig;
    private ContextAwareConfig contextConfig;

    @BeforeEach
    void setUp() {
        baseConfig = new ConfigBuilder()
                .addProperties(Map.of(
                        "app.name", "BaseApp",
                        "app.port", "8080",
                        "db.url", "jdbc:mysql://localhost",
                        "tenants.tenant-1.db.url", "jdbc:mysql://tenant1-db",
                        "tenants.tenant-2.db.url", "jdbc:mysql://tenant2-db"
                ))
                .disablePlaceholders()
                .build();
        contextConfig = new ContextAwareConfig(baseConfig);
    }

    @Nested
    @DisplayName("基本委托测试")
    class DelegationTests {

        @Test
        @DisplayName("getString - 委托到基础配置")
        void testGetStringDelegation() {
            assertThat(contextConfig.getString("app.name")).isEqualTo("BaseApp");
        }

        @Test
        @DisplayName("getInt - 委托到基础配置")
        void testGetIntDelegation() {
            assertThat(contextConfig.getInt("app.port")).isEqualTo(8080);
        }

        @Test
        @DisplayName("getString带默认值 - 委托到基础配置")
        void testGetStringWithDefault() {
            assertThat(contextConfig.getString("missing", "default")).isEqualTo("default");
        }

        @Test
        @DisplayName("hasKey - 委托到基础配置")
        void testHasKey() {
            assertThat(contextConfig.hasKey("app.name")).isTrue();
            assertThat(contextConfig.hasKey("missing")).isFalse();
        }

        @Test
        @DisplayName("getKeys - 委托到基础配置")
        void testGetKeys() {
            assertThat(contextConfig.getKeys()).contains("app.name", "app.port");
        }
    }

    @Nested
    @DisplayName("覆盖上下文测试")
    class OverrideContextTests {

        @Test
        @DisplayName("使用覆盖上下文")
        void testWithOverrides() throws Exception {
            Map<String, String> overrides = Map.of("app.name", "OverriddenApp");

            String result = ConfigContext.withOverrides(overrides, () -> {
                return contextConfig.getString("app.name");
            });

            assertThat(result).isEqualTo("OverriddenApp");
        }

        @Test
        @DisplayName("覆盖不存在的键")
        void testOverrideNewKey() throws Exception {
            Map<String, String> overrides = Map.of("new.key", "new-value");

            String result = ConfigContext.withOverrides(overrides, () -> {
                return contextConfig.getString("new.key");
            });

            assertThat(result).isEqualTo("new-value");
        }

        @Test
        @DisplayName("覆盖上下文外 - 返回基础值")
        void testNoOverridesOutsideContext() {
            assertThat(contextConfig.getString("app.name")).isEqualTo("BaseApp");
        }
    }

    @Nested
    @DisplayName("租户上下文测试")
    class TenantContextTests {

        @Test
        @DisplayName("租户特定配置")
        void testTenantSpecificConfig() throws Exception {
            String result = ConfigContext.withTenant("tenant-1", () -> {
                return contextConfig.getString("db.url");
            });

            assertThat(result).isEqualTo("jdbc:mysql://tenant1-db");
        }

        @Test
        @DisplayName("不同租户不同配置")
        void testDifferentTenantsDifferentConfig() throws Exception {
            String tenant1Url = ConfigContext.withTenant("tenant-1", () -> {
                return contextConfig.getString("db.url");
            });

            String tenant2Url = ConfigContext.withTenant("tenant-2", () -> {
                return contextConfig.getString("db.url");
            });

            assertThat(tenant1Url).isEqualTo("jdbc:mysql://tenant1-db");
            assertThat(tenant2Url).isEqualTo("jdbc:mysql://tenant2-db");
        }

        @Test
        @DisplayName("租户配置不存在 - 回退到基础配置")
        void testTenantFallbackToBase() throws Exception {
            String result = ConfigContext.withTenant("tenant-1", () -> {
                return contextConfig.getString("app.name"); // 租户没有覆盖
            });

            assertThat(result).isEqualTo("BaseApp");
        }
    }

    @Nested
    @DisplayName("其他委托方法测试")
    class OtherDelegationTests {

        @Test
        @DisplayName("getLong")
        void testGetLong() {
            assertThat(contextConfig.getLong("app.port")).isEqualTo(8080L);
        }

        @Test
        @DisplayName("getLong带默认值")
        void testGetLongWithDefault() {
            assertThat(contextConfig.getLong("missing", 9999L)).isEqualTo(9999L);
        }

        @Test
        @DisplayName("getDouble")
        void testGetDouble() {
            Config doubleConfig = new ConfigBuilder()
                    .addProperties(Map.of("rate", "3.14"))
                    .disablePlaceholders()
                    .build();
            ContextAwareConfig cfg = new ContextAwareConfig(doubleConfig);

            assertThat(cfg.getDouble("rate")).isEqualTo(3.14);
        }

        @Test
        @DisplayName("getBoolean")
        void testGetBoolean() {
            Config boolConfig = new ConfigBuilder()
                    .addProperties(Map.of("enabled", "true"))
                    .disablePlaceholders()
                    .build();
            ContextAwareConfig cfg = new ContextAwareConfig(boolConfig);

            assertThat(cfg.getBoolean("enabled")).isTrue();
        }

        @Test
        @DisplayName("getDuration")
        void testGetDuration() {
            Config durationConfig = new ConfigBuilder()
                    .addProperties(Map.of("timeout", "30s"))
                    .disablePlaceholders()
                    .build();
            ContextAwareConfig cfg = new ContextAwareConfig(durationConfig);

            assertThat(cfg.getDuration("timeout")).isEqualTo(Duration.ofSeconds(30));
        }

        @Test
        @DisplayName("getOptional")
        void testGetOptional() {
            assertThat(contextConfig.getOptional("app.name")).isPresent();
            assertThat(contextConfig.getOptional("missing")).isEmpty();
        }

        @Test
        @DisplayName("getByPrefix")
        void testGetByPrefix() {
            Map<String, String> appProps = contextConfig.getByPrefix("app.");
            assertThat(appProps).containsKey("app.name");
        }

        @Test
        @DisplayName("getSubConfig")
        void testGetSubConfig() {
            Config subConfig = contextConfig.getSubConfig("app");
            assertThat(subConfig.getString("name")).isEqualTo("BaseApp");
        }
    }
}
