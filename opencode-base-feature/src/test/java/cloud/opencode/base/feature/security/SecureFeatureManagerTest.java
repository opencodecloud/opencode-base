package cloud.opencode.base.feature.security;

import cloud.opencode.base.feature.Feature;
import cloud.opencode.base.feature.FeatureContext;
import cloud.opencode.base.feature.OpenFeature;
import cloud.opencode.base.feature.audit.FeatureAuditEvent;
import cloud.opencode.base.feature.exception.FeatureSecurityException;
import cloud.opencode.base.feature.store.InMemoryFeatureStore;
import cloud.opencode.base.feature.strategy.AlwaysOffStrategy;
import cloud.opencode.base.feature.strategy.AlwaysOnStrategy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * SecureFeatureManager 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
@DisplayName("SecureFeatureManager 测试")
class SecureFeatureManagerTest {

    private OpenFeature features;
    private SecureFeatureManager manager;
    private List<FeatureAuditEvent> loggedEvents;

    @BeforeEach
    void setUp() {
        features = OpenFeature.create(new InMemoryFeatureStore());
        loggedEvents = new ArrayList<>();
        AuditLogger logger = loggedEvents::add;
        manager = new SecureFeatureManager(features, Set.of("admin1", "admin2"), logger);
    }

    @AfterEach
    void tearDown() {
        OpenFeature.resetInstance();
    }

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用默认OpenFeature实例")
        void testDefaultOpenFeatureConstructor() {
            SecureFeatureManager m = new SecureFeatureManager(Set.of("admin"), null);

            assertThat(m).isNotNull();
        }

        @Test
        @DisplayName("null管理员集合使用空集合")
        void testNullAdminSet() {
            SecureFeatureManager m = new SecureFeatureManager(null, null);

            assertThat(m.isAdmin("anyone")).isFalse();
        }
    }

    @Nested
    @DisplayName("register() 测试")
    class RegisterTests {

        @Test
        @DisplayName("管理员可注册功能")
        void testAdminCanRegister() {
            Feature feature = Feature.builder("test").build();

            manager.register(feature, "admin1");

            assertThat(features.exists("test")).isTrue();
        }

        @Test
        @DisplayName("非管理员注册抛出异常")
        void testNonAdminCannotRegister() {
            Feature feature = Feature.builder("test").build();

            assertThatThrownBy(() -> manager.register(feature, "user"))
                    .isInstanceOf(FeatureSecurityException.class)
                    .hasMessageContaining("Unauthorized");
        }

        @Test
        @DisplayName("记录审计日志")
        void testLogsAuditEvent() {
            Feature feature = Feature.builder("test").alwaysOn().build();

            manager.register(feature, "admin1");

            assertThat(loggedEvents).hasSize(1);
            assertThat(loggedEvents.get(0).action()).isEqualTo("REGISTER");
        }
    }

    @Nested
    @DisplayName("enable() 测试")
    class EnableTests {

        @Test
        @DisplayName("管理员可启用功能")
        void testAdminCanEnable() {
            features.register(Feature.builder("test").defaultEnabled(false).build());

            manager.enable("test", "admin1");

            assertThat(features.isEnabled("test")).isTrue();
        }

        @Test
        @DisplayName("非管理员启用抛出异常")
        void testNonAdminCannotEnable() {
            features.register(Feature.builder("test").build());

            assertThatThrownBy(() -> manager.enable("test", "user"))
                    .isInstanceOf(FeatureSecurityException.class);
        }

        @Test
        @DisplayName("记录审计日志")
        void testLogsAuditEvent() {
            features.register(Feature.builder("test").defaultEnabled(false).build());

            manager.enable("test", "admin1");

            assertThat(loggedEvents).hasSize(1);
            assertThat(loggedEvents.get(0).action()).isEqualTo("ENABLE");
        }
    }

    @Nested
    @DisplayName("disable() 测试")
    class DisableTests {

        @Test
        @DisplayName("管理员可禁用功能")
        void testAdminCanDisable() {
            // 使用defaultEnabled而不是alwaysOn，因为disable只修改defaultEnabled，不修改策略
            features.register(Feature.builder("test").defaultEnabled(true).build());

            manager.disable("test", "admin2");

            assertThat(features.isEnabled("test")).isFalse();
        }

        @Test
        @DisplayName("非管理员禁用抛出异常")
        void testNonAdminCannotDisable() {
            features.register(Feature.builder("test").build());

            assertThatThrownBy(() -> manager.disable("test", "user"))
                    .isInstanceOf(FeatureSecurityException.class);
        }

        @Test
        @DisplayName("记录审计日志")
        void testLogsAuditEvent() {
            features.register(Feature.builder("test").alwaysOn().build());

            manager.disable("test", "admin1");

            assertThat(loggedEvents).hasSize(1);
            assertThat(loggedEvents.get(0).action()).isEqualTo("DISABLE");
        }
    }

    @Nested
    @DisplayName("updateStrategy() 测试")
    class UpdateStrategyTests {

        @Test
        @DisplayName("管理员可更新策略")
        void testAdminCanUpdateStrategy() {
            features.register(Feature.builder("test").alwaysOff().build());

            manager.updateStrategy("test", AlwaysOnStrategy.INSTANCE, "admin1");

            assertThat(features.isEnabled("test")).isTrue();
        }

        @Test
        @DisplayName("非管理员更新策略抛出异常")
        void testNonAdminCannotUpdateStrategy() {
            features.register(Feature.builder("test").build());

            assertThatThrownBy(() -> manager.updateStrategy("test", AlwaysOnStrategy.INSTANCE, "user"))
                    .isInstanceOf(FeatureSecurityException.class);
        }

        @Test
        @DisplayName("记录审计日志")
        void testLogsAuditEvent() {
            features.register(Feature.builder("test").alwaysOff().build());

            manager.updateStrategy("test", AlwaysOnStrategy.INSTANCE, "admin1");

            assertThat(loggedEvents).hasSize(1);
            assertThat(loggedEvents.get(0).action()).isEqualTo("UPDATE_STRATEGY");
        }
    }

    @Nested
    @DisplayName("isEnabled() 测试")
    class IsEnabledTests {

        @Test
        @DisplayName("无需权限检查状态")
        void testNoPermissionNeeded() {
            features.register(Feature.builder("test").alwaysOn().build());

            // 非管理员也可以检查
            assertThat(manager.isEnabled("test")).isTrue();
        }

        @Test
        @DisplayName("带上下文检查状态")
        void testWithContext() {
            features.register(Feature.builder("test").forUsers("user1").build());

            assertThat(manager.isEnabled("test", FeatureContext.ofUser("user1"))).isTrue();
            assertThat(manager.isEnabled("test", FeatureContext.ofUser("user2"))).isFalse();
        }
    }

    @Nested
    @DisplayName("get() 测试")
    class GetTests {

        @Test
        @DisplayName("无需权限获取功能")
        void testNoPermissionNeeded() {
            features.register(Feature.builder("test").build());

            Optional<Feature> result = manager.get("test");

            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("不存在的功能返回空")
        void testNonExistent() {
            Optional<Feature> result = manager.get("nonexistent");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("isAdmin() 测试")
    class IsAdminTests {

        @Test
        @DisplayName("管理员返回true")
        void testAdminReturnsTrue() {
            assertThat(manager.isAdmin("admin1")).isTrue();
            assertThat(manager.isAdmin("admin2")).isTrue();
        }

        @Test
        @DisplayName("非管理员返回false")
        void testNonAdminReturnsFalse() {
            assertThat(manager.isAdmin("user")).isFalse();
        }

        @Test
        @DisplayName("null返回false")
        void testNullReturnsFalse() {
            assertThat(manager.isAdmin(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("null AuditLogger测试")
    class NullAuditLoggerTests {

        @Test
        @DisplayName("null logger不抛异常")
        void testNullLoggerNoException() {
            SecureFeatureManager m = new SecureFeatureManager(features, Set.of("admin"), null);
            features.register(Feature.builder("test").build());

            assertThatNoException().isThrownBy(() -> m.enable("test", "admin"));
        }
    }
}
