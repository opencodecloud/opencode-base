package cloud.opencode.base.config.advanced;

import cloud.opencode.base.config.Config;
import cloud.opencode.base.config.ConfigBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * TenantConfigManager 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("TenantConfigManager 测试")
class TenantConfigManagerTest {

    private Config baseConfig;
    private TenantConfigManager manager;

    @BeforeEach
    void setUp() {
        baseConfig = new ConfigBuilder()
                .addProperties(Map.of(
                        "app.name", "BaseApp",
                        "db.url", "jdbc:mysql://localhost"
                ))
                .disablePlaceholders()
                .build();
        manager = new TenantConfigManager(baseConfig);
    }

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用基础配置创建")
        void testConstructor() {
            TenantConfigManager mgr = new TenantConfigManager(baseConfig);
            assertThat(mgr).isNotNull();
        }
    }

    @Nested
    @DisplayName("getConfig测试")
    class GetConfigTests {

        @Test
        @DisplayName("获取租户配置")
        void testGetConfig() {
            Config tenantConfig = manager.getConfig("tenant-1");

            assertThat(tenantConfig).isNotNull();
        }

        @Test
        @DisplayName("租户配置继承基础配置")
        void testTenantInheritsBase() {
            Config tenantConfig = manager.getConfig("tenant-1");

            // 应该继承基础配置
            assertThat(tenantConfig.getString("app.name")).isEqualTo("BaseApp");
        }

        @Test
        @DisplayName("相同租户返回缓存配置")
        void testSameTenantReturnsCached() {
            Config config1 = manager.getConfig("tenant-1");
            Config config2 = manager.getConfig("tenant-1");

            assertThat(config1).isSameAs(config2);
        }

        @Test
        @DisplayName("不同租户返回不同配置")
        void testDifferentTenantsReturnDifferentConfigs() {
            Config config1 = manager.getConfig("tenant-1");
            Config config2 = manager.getConfig("tenant-2");

            assertThat(config1).isNotSameAs(config2);
        }
    }

    @Nested
    @DisplayName("get测试")
    class GetTests {

        @Test
        @DisplayName("获取租户类型化配置值")
        void testGetTypedValue() {
            String name = manager.get("tenant-1", "app.name", String.class);

            assertThat(name).isEqualTo("BaseApp");
        }
    }

    @Nested
    @DisplayName("clearCache测试")
    class ClearCacheTests {

        @Test
        @DisplayName("清除单个租户缓存")
        void testClearCache() {
            Config config1 = manager.getConfig("tenant-1");

            manager.clearCache("tenant-1");

            Config config2 = manager.getConfig("tenant-1");

            assertThat(config1).isNotSameAs(config2);
        }

        @Test
        @DisplayName("清除不存在的租户缓存 - 不抛异常")
        void testClearNonexistentCache() {
            manager.clearCache("nonexistent-tenant");
            // 不应该抛出异常
        }
    }

    @Nested
    @DisplayName("clearAllCaches测试")
    class ClearAllCachesTests {

        @Test
        @DisplayName("清除所有缓存")
        void testClearAllCaches() {
            Config config1 = manager.getConfig("tenant-1");
            Config config2 = manager.getConfig("tenant-2");

            manager.clearAllCaches();

            Config newConfig1 = manager.getConfig("tenant-1");
            Config newConfig2 = manager.getConfig("tenant-2");

            assertThat(config1).isNotSameAs(newConfig1);
            assertThat(config2).isNotSameAs(newConfig2);
        }
    }
}
