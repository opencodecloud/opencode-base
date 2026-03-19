package cloud.opencode.base.config.jdk25;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * ConfigContext 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("ConfigContext 测试")
class ConfigContextTest {

    @Nested
    @DisplayName("ScopedValue常量测试")
    class ScopedValueConstantsTests {

        @Test
        @DisplayName("OVERRIDES常量存在")
        void testOverridesConstant() {
            assertThat(ConfigContext.OVERRIDES).isNotNull();
        }

        @Test
        @DisplayName("PROFILE常量存在")
        void testProfileConstant() {
            assertThat(ConfigContext.PROFILE).isNotNull();
        }

        @Test
        @DisplayName("TENANT_ID常量存在")
        void testTenantIdConstant() {
            assertThat(ConfigContext.TENANT_ID).isNotNull();
        }
    }

    @Nested
    @DisplayName("withOverrides测试")
    class WithOverridesTests {

        @Test
        @DisplayName("在覆盖上下文中执行")
        void testWithOverrides() throws Exception {
            Map<String, String> overrides = Map.of("key", "value");

            String result = ConfigContext.withOverrides(overrides, () -> {
                Optional<Map<String, String>> current = ConfigContext.currentOverrides();
                assertThat(current).isPresent();
                return current.get().get("key");
            });

            assertThat(result).isEqualTo("value");
        }

        @Test
        @DisplayName("上下文外无覆盖")
        void testNoOverridesOutsideContext() {
            Optional<Map<String, String>> overrides = ConfigContext.currentOverrides();
            assertThat(overrides).isEmpty();
        }
    }

    @Nested
    @DisplayName("withProfile测试")
    class WithProfileTests {

        @Test
        @DisplayName("在环境上下文中执行")
        void testWithProfile() throws Exception {
            String result = ConfigContext.withProfile("production", () -> {
                Optional<String> profile = ConfigContext.currentProfile();
                assertThat(profile).isPresent();
                return profile.get();
            });

            assertThat(result).isEqualTo("production");
        }

        @Test
        @DisplayName("上下文外无环境")
        void testNoProfileOutsideContext() {
            Optional<String> profile = ConfigContext.currentProfile();
            assertThat(profile).isEmpty();
        }
    }

    @Nested
    @DisplayName("withTenant测试")
    class WithTenantTests {

        @Test
        @DisplayName("在租户上下文中执行")
        void testWithTenant() throws Exception {
            String result = ConfigContext.withTenant("tenant-123", () -> {
                Optional<String> tenant = ConfigContext.currentTenant();
                assertThat(tenant).isPresent();
                return tenant.get();
            });

            assertThat(result).isEqualTo("tenant-123");
        }

        @Test
        @DisplayName("上下文外无租户")
        void testNoTenantOutsideContext() {
            Optional<String> tenant = ConfigContext.currentTenant();
            assertThat(tenant).isEmpty();
        }
    }

    @Nested
    @DisplayName("runWith测试")
    class RunWithTests {

        @Test
        @DisplayName("runWithOverrides")
        void testRunWithOverrides() {
            Map<String, String> overrides = Map.of("key", "value");

            ConfigContext.runWithOverrides(overrides, () -> {
                Optional<Map<String, String>> current = ConfigContext.currentOverrides();
                assertThat(current).isPresent();
                assertThat(current.get()).containsEntry("key", "value");
            });
        }

        @Test
        @DisplayName("runWithProfile")
        void testRunWithProfile() {
            ConfigContext.runWithProfile("staging", () -> {
                Optional<String> profile = ConfigContext.currentProfile();
                assertThat(profile).isPresent();
                assertThat(profile.get()).isEqualTo("staging");
            });
        }

        @Test
        @DisplayName("runWithTenant")
        void testRunWithTenant() {
            ConfigContext.runWithTenant("tenant-456", () -> {
                Optional<String> tenant = ConfigContext.currentTenant();
                assertThat(tenant).isPresent();
                assertThat(tenant.get()).isEqualTo("tenant-456");
            });
        }
    }

    @Nested
    @DisplayName("上下文隔离测试")
    class ContextIsolationTests {

        @Test
        @DisplayName("不同上下文隔离")
        void testContextIsolation() throws Exception {
            ConfigContext.withTenant("tenant-A", () -> {
                String tenantA = ConfigContext.currentTenant().orElse("none");

                // 这里不能嵌套withTenant因为ScopedValue不支持
                assertThat(tenantA).isEqualTo("tenant-A");
                return null;
            });

            ConfigContext.withTenant("tenant-B", () -> {
                String tenantB = ConfigContext.currentTenant().orElse("none");
                assertThat(tenantB).isEqualTo("tenant-B");
                return null;
            });
        }
    }

    @Nested
    @DisplayName("current方法测试")
    class CurrentMethodTests {

        @Test
        @DisplayName("currentOverrides - 绑定状态")
        void testCurrentOverridesBound() throws Exception {
            ConfigContext.withOverrides(Map.of("k", "v"), () -> {
                Optional<Map<String, String>> current = ConfigContext.currentOverrides();
                assertThat(current).isPresent();
                return null;
            });
        }

        @Test
        @DisplayName("currentProfile - 绑定状态")
        void testCurrentProfileBound() throws Exception {
            ConfigContext.withProfile("dev", () -> {
                Optional<String> current = ConfigContext.currentProfile();
                assertThat(current).isPresent();
                assertThat(current.get()).isEqualTo("dev");
                return null;
            });
        }

        @Test
        @DisplayName("currentTenant - 绑定状态")
        void testCurrentTenantBound() throws Exception {
            ConfigContext.withTenant("tid", () -> {
                Optional<String> current = ConfigContext.currentTenant();
                assertThat(current).isPresent();
                assertThat(current.get()).isEqualTo("tid");
                return null;
            });
        }
    }
}
