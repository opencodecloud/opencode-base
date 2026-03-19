package cloud.opencode.base.feature;

import cloud.opencode.base.feature.exception.FeatureNotFoundException;
import cloud.opencode.base.feature.listener.FeatureListener;
import cloud.opencode.base.feature.store.InMemoryFeatureStore;
import cloud.opencode.base.feature.strategy.AlwaysOffStrategy;
import cloud.opencode.base.feature.strategy.AlwaysOnStrategy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenFeature 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
@DisplayName("OpenFeature 测试")
class OpenFeatureTest {

    private OpenFeature features;

    @BeforeEach
    void setUp() {
        features = OpenFeature.create(new InMemoryFeatureStore());
    }

    @AfterEach
    void tearDown() {
        OpenFeature.resetInstance();
    }

    @Nested
    @DisplayName("getInstance() 测试")
    class GetInstanceTests {

        @Test
        @DisplayName("返回单例实例")
        void testGetInstance() {
            OpenFeature instance1 = OpenFeature.getInstance();
            OpenFeature instance2 = OpenFeature.getInstance();

            assertThat(instance1).isSameAs(instance2);
        }

        @Test
        @DisplayName("resetInstance后创建新实例")
        void testResetInstance() {
            OpenFeature instance1 = OpenFeature.getInstance();
            OpenFeature.resetInstance();
            OpenFeature instance2 = OpenFeature.getInstance();

            assertThat(instance1).isNotSameAs(instance2);
        }
    }

    @Nested
    @DisplayName("create() 测试")
    class CreateTests {

        @Test
        @DisplayName("使用自定义store创建")
        void testCreateWithStore() {
            InMemoryFeatureStore store = new InMemoryFeatureStore();
            OpenFeature custom = OpenFeature.create(store);

            assertThat(custom.getStore()).isSameAs(store);
        }
    }

    @Nested
    @DisplayName("register() 测试")
    class RegisterTests {

        @Test
        @DisplayName("注册功能")
        void testRegister() {
            Feature feature = Feature.builder("test").build();

            features.register(feature);

            assertThat(features.exists("test")).isTrue();
        }
    }

    @Nested
    @DisplayName("registerAll() 测试")
    class RegisterAllTests {

        @Test
        @DisplayName("注册多个功能")
        void testRegisterAll() {
            Feature f1 = Feature.builder("f1").build();
            Feature f2 = Feature.builder("f2").build();

            features.registerAll(f1, f2);

            assertThat(features.exists("f1")).isTrue();
            assertThat(features.exists("f2")).isTrue();
        }
    }

    @Nested
    @DisplayName("isEnabled() 测试")
    class IsEnabledTests {

        @Test
        @DisplayName("不存在的功能返回false")
        void testIsEnabledNonExistent() {
            assertThat(features.isEnabled("nonexistent")).isFalse();
        }

        @Test
        @DisplayName("AlwaysOn返回true")
        void testIsEnabledAlwaysOn() {
            features.register(Feature.builder("test").alwaysOn().build());

            assertThat(features.isEnabled("test")).isTrue();
        }

        @Test
        @DisplayName("AlwaysOff返回false")
        void testIsEnabledAlwaysOff() {
            features.register(Feature.builder("test").alwaysOff().build());

            assertThat(features.isEnabled("test")).isFalse();
        }

        @Test
        @DisplayName("无策略使用默认值")
        void testIsEnabledWithDefault() {
            features.register(Feature.builder("test").defaultEnabled(true).build());

            assertThat(features.isEnabled("test")).isTrue();
        }

        @Test
        @DisplayName("带上下文检查")
        void testIsEnabledWithContext() {
            features.register(Feature.builder("test").forUsers("user1").build());

            assertThat(features.isEnabled("test", FeatureContext.ofUser("user1"))).isTrue();
            assertThat(features.isEnabled("test", FeatureContext.ofUser("user2"))).isFalse();
        }
    }

    @Nested
    @DisplayName("isEnabledForUser() 测试")
    class IsEnabledForUserTests {

        @Test
        @DisplayName("为特定用户检查")
        void testIsEnabledForUser() {
            features.register(Feature.builder("test").forUsers("user1").build());

            assertThat(features.isEnabledForUser("test", "user1")).isTrue();
            assertThat(features.isEnabledForUser("test", "user2")).isFalse();
        }
    }

    @Nested
    @DisplayName("ifEnabled() 测试")
    class IfEnabledTests {

        @Test
        @DisplayName("启用时执行操作")
        void testIfEnabledExecutes() {
            features.register(Feature.builder("test").alwaysOn().build());
            AtomicBoolean executed = new AtomicBoolean(false);

            features.ifEnabled("test", () -> executed.set(true));

            assertThat(executed.get()).isTrue();
        }

        @Test
        @DisplayName("禁用时不执行操作")
        void testIfEnabledDoesNotExecute() {
            features.register(Feature.builder("test").alwaysOff().build());
            AtomicBoolean executed = new AtomicBoolean(false);

            features.ifEnabled("test", () -> executed.set(true));

            assertThat(executed.get()).isFalse();
        }

        @Test
        @DisplayName("带上下文的ifEnabled")
        void testIfEnabledWithContext() {
            features.register(Feature.builder("test").forUsers("user1").build());
            AtomicBoolean executed = new AtomicBoolean(false);

            features.ifEnabled("test", FeatureContext.ofUser("user1"), () -> executed.set(true));

            assertThat(executed.get()).isTrue();
        }

        @Test
        @DisplayName("带返回值的ifEnabled")
        void testIfEnabledWithSuppliers() {
            features.register(Feature.builder("test").alwaysOn().build());

            String result = features.ifEnabled("test", () -> "enabled", () -> "disabled");

            assertThat(result).isEqualTo("enabled");
        }

        @Test
        @DisplayName("禁用时使用disabled supplier")
        void testIfEnabledDisabledSupplier() {
            features.register(Feature.builder("test").alwaysOff().build());

            String result = features.ifEnabled("test", () -> "enabled", () -> "disabled");

            assertThat(result).isEqualTo("disabled");
        }

        @Test
        @DisplayName("带上下文和返回值的ifEnabled")
        void testIfEnabledWithContextAndSuppliers() {
            features.register(Feature.builder("test").forUsers("user1").build());

            String result = features.ifEnabled("test", FeatureContext.ofUser("user1"),
                    () -> "enabled", () -> "disabled");

            assertThat(result).isEqualTo("enabled");
        }
    }

    @Nested
    @DisplayName("get() 测试")
    class GetTests {

        @Test
        @DisplayName("获取存在的功能")
        void testGetExisting() {
            features.register(Feature.builder("test").build());

            Optional<Feature> result = features.get("test");

            assertThat(result).isPresent();
            assertThat(result.get().key()).isEqualTo("test");
        }

        @Test
        @DisplayName("获取不存在的功能返回空")
        void testGetNonExisting() {
            Optional<Feature> result = features.get("nonexistent");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getOrThrow() 测试")
    class GetOrThrowTests {

        @Test
        @DisplayName("获取存在的功能")
        void testGetOrThrowExisting() {
            features.register(Feature.builder("test").build());

            Feature result = features.getOrThrow("test");

            assertThat(result.key()).isEqualTo("test");
        }

        @Test
        @DisplayName("获取不存在的功能抛出异常")
        void testGetOrThrowNonExisting() {
            assertThatThrownBy(() -> features.getOrThrow("nonexistent"))
                    .isInstanceOf(FeatureNotFoundException.class)
                    .hasMessageContaining("nonexistent");
        }
    }

    @Nested
    @DisplayName("enable() 测试")
    class EnableTests {

        @Test
        @DisplayName("启用功能")
        void testEnable() {
            features.register(Feature.builder("test").defaultEnabled(false).build());

            features.enable("test");

            Feature feature = features.getOrThrow("test");
            assertThat(feature.defaultEnabled()).isTrue();
        }

        @Test
        @DisplayName("启用不存在的功能抛出异常")
        void testEnableNonExisting() {
            assertThatThrownBy(() -> features.enable("nonexistent"))
                    .isInstanceOf(FeatureNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("disable() 测试")
    class DisableTests {

        @Test
        @DisplayName("禁用功能")
        void testDisable() {
            features.register(Feature.builder("test").defaultEnabled(true).build());

            features.disable("test");

            Feature feature = features.getOrThrow("test");
            assertThat(feature.defaultEnabled()).isFalse();
        }

        @Test
        @DisplayName("禁用不存在的功能抛出异常")
        void testDisableNonExisting() {
            assertThatThrownBy(() -> features.disable("nonexistent"))
                    .isInstanceOf(FeatureNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateStrategy() 测试")
    class UpdateStrategyTests {

        @Test
        @DisplayName("更新策略")
        void testUpdateStrategy() {
            features.register(Feature.builder("test").alwaysOff().build());

            features.updateStrategy("test", AlwaysOnStrategy.INSTANCE);

            assertThat(features.isEnabled("test")).isTrue();
        }

        @Test
        @DisplayName("更新不存在的功能策略抛出异常")
        void testUpdateStrategyNonExisting() {
            assertThatThrownBy(() -> features.updateStrategy("nonexistent", AlwaysOnStrategy.INSTANCE))
                    .isInstanceOf(FeatureNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("delete() 测试")
    class DeleteTests {

        @Test
        @DisplayName("删除存在的功能")
        void testDeleteExisting() {
            features.register(Feature.builder("test").build());

            boolean result = features.delete("test");

            assertThat(result).isTrue();
            assertThat(features.exists("test")).isFalse();
        }

        @Test
        @DisplayName("删除不存在的功能返回false")
        void testDeleteNonExisting() {
            boolean result = features.delete("nonexistent");

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("getAllKeys() 测试")
    class GetAllKeysTests {

        @Test
        @DisplayName("获取所有键")
        void testGetAllKeys() {
            features.register(Feature.builder("f1").build());
            features.register(Feature.builder("f2").build());

            Set<String> keys = features.getAllKeys();

            assertThat(keys).containsExactlyInAnyOrder("f1", "f2");
        }

        @Test
        @DisplayName("空store返回空集合")
        void testGetAllKeysEmpty() {
            Set<String> keys = features.getAllKeys();

            assertThat(keys).isEmpty();
        }
    }

    @Nested
    @DisplayName("getAll() 测试")
    class GetAllTests {

        @Test
        @DisplayName("获取所有功能")
        void testGetAll() {
            features.register(Feature.builder("f1").build());
            features.register(Feature.builder("f2").build());

            Map<String, Feature> all = features.getAll();

            assertThat(all).hasSize(2);
            assertThat(all).containsKeys("f1", "f2");
        }
    }

    @Nested
    @DisplayName("exists() 测试")
    class ExistsTests {

        @Test
        @DisplayName("存在返回true")
        void testExistsTrue() {
            features.register(Feature.builder("test").build());

            assertThat(features.exists("test")).isTrue();
        }

        @Test
        @DisplayName("不存在返回false")
        void testExistsFalse() {
            assertThat(features.exists("nonexistent")).isFalse();
        }
    }

    @Nested
    @DisplayName("size() 测试")
    class SizeTests {

        @Test
        @DisplayName("返回功能数量")
        void testSize() {
            features.register(Feature.builder("f1").build());
            features.register(Feature.builder("f2").build());

            assertThat(features.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("空store返回0")
        void testSizeEmpty() {
            assertThat(features.size()).isZero();
        }
    }

    @Nested
    @DisplayName("clear() 测试")
    class ClearTests {

        @Test
        @DisplayName("清空所有功能")
        void testClear() {
            features.register(Feature.builder("f1").build());
            features.register(Feature.builder("f2").build());

            features.clear();

            assertThat(features.size()).isZero();
        }
    }

    @Nested
    @DisplayName("监听器测试")
    class ListenerTests {

        @Test
        @DisplayName("添加监听器")
        void testAddListener() {
            AtomicInteger callCount = new AtomicInteger(0);
            FeatureListener listener = (key, oldValue, newValue) -> callCount.incrementAndGet();

            features.addListener(listener);
            features.register(Feature.builder("test").defaultEnabled(false).build());
            features.enable("test");

            assertThat(callCount.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("null监听器不添加")
        void testAddNullListener() {
            features.addListener(null);
            features.register(Feature.builder("test").build());
            features.enable("test");
            // 不抛异常即为成功
        }

        @Test
        @DisplayName("移除监听器")
        void testRemoveListener() {
            AtomicInteger callCount = new AtomicInteger(0);
            FeatureListener listener = (key, oldValue, newValue) -> callCount.incrementAndGet();

            features.addListener(listener);
            features.removeListener(listener);
            features.register(Feature.builder("test").defaultEnabled(false).build());
            features.enable("test");

            assertThat(callCount.get()).isZero();
        }

        @Test
        @DisplayName("监听器异常不影响其他监听器")
        void testListenerExceptionDoesNotAffectOthers() {
            AtomicInteger callCount = new AtomicInteger(0);

            features.addListener((key, oldValue, newValue) -> {
                throw new RuntimeException("Error");
            });
            features.addListener((key, oldValue, newValue) -> callCount.incrementAndGet());

            features.register(Feature.builder("test").defaultEnabled(false).build());
            features.enable("test");

            assertThat(callCount.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("enable通知监听器")
        void testEnableNotifiesListeners() {
            AtomicBoolean notified = new AtomicBoolean(false);
            features.addListener((key, oldValue, newValue) -> {
                if ("test".equals(key) && !oldValue && newValue) {
                    notified.set(true);
                }
            });

            features.register(Feature.builder("test").defaultEnabled(false).build());
            features.enable("test");

            assertThat(notified.get()).isTrue();
        }

        @Test
        @DisplayName("disable通知监听器")
        void testDisableNotifiesListeners() {
            AtomicBoolean notified = new AtomicBoolean(false);
            features.addListener((key, oldValue, newValue) -> {
                if ("test".equals(key) && oldValue && !newValue) {
                    notified.set(true);
                }
            });

            features.register(Feature.builder("test").defaultEnabled(true).build());
            features.disable("test");

            assertThat(notified.get()).isTrue();
        }

        @Test
        @DisplayName("delete通知监听器")
        void testDeleteNotifiesListeners() {
            AtomicBoolean notified = new AtomicBoolean(false);
            features.addListener((key, oldValue, newValue) -> {
                if ("test".equals(key)) {
                    notified.set(true);
                }
            });

            features.register(Feature.builder("test").alwaysOn().build());
            features.delete("test");

            assertThat(notified.get()).isTrue();
        }
    }

    @Nested
    @DisplayName("getStore()/setStore() 测试")
    class StoreTests {

        @Test
        @DisplayName("获取store")
        void testGetStore() {
            assertThat(features.getStore()).isNotNull();
        }

        @Test
        @DisplayName("设置store")
        void testSetStore() {
            InMemoryFeatureStore newStore = new InMemoryFeatureStore();
            features.setStore(newStore);

            assertThat(features.getStore()).isSameAs(newStore);
        }

        @Test
        @DisplayName("null store抛出异常")
        void testSetNullStore() {
            assertThatThrownBy(() -> features.setStore(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("null");
        }
    }
}
