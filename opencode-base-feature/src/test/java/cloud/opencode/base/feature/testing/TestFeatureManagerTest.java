package cloud.opencode.base.feature.testing;

import cloud.opencode.base.feature.Feature;
import cloud.opencode.base.feature.FeatureContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * TestFeatureManager 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.3
 */
@DisplayName("TestFeatureManager 测试")
class TestFeatureManagerTest {

    private TestFeatureManager manager;

    @BeforeEach
    void setUp() {
        manager = new TestFeatureManager();
    }

    @AfterEach
    void tearDown() {
        manager.close();
    }

    @Nested
    @DisplayName("withFeature() 测试")
    class WithFeatureTests {

        @Test
        @DisplayName("注册启用的功能")
        void testWithFeatureEnabled() {
            manager.withFeature("test", true);

            assertThat(manager.isEnabled("test")).isTrue();
        }

        @Test
        @DisplayName("注册禁用的功能")
        void testWithFeatureDisabled() {
            manager.withFeature("test", false);

            assertThat(manager.isEnabled("test")).isFalse();
        }

        @Test
        @DisplayName("链式调用")
        void testChaining() {
            manager.withFeature("f1", true)
                    .withFeature("f2", false);

            assertThat(manager.isEnabled("f1")).isTrue();
            assertThat(manager.isEnabled("f2")).isFalse();
        }

        @Test
        @DisplayName("注册完整Feature对象")
        void testWithFeatureObject() {
            Feature feature = Feature.builder("test").alwaysOn().build();
            manager.withFeature(feature);

            assertThat(manager.isEnabled("test")).isTrue();
        }
    }

    @Nested
    @DisplayName("enable()/disable() 测试")
    class EnableDisableTests {

        @Test
        @DisplayName("启用已存在的功能")
        void testEnableExisting() {
            manager.withFeature("test", false);
            manager.enable("test");

            assertThat(manager.getDelegate().getOrThrow("test").defaultEnabled()).isTrue();
        }

        @Test
        @DisplayName("启用不存在的功能自动注册")
        void testEnableNonExisting() {
            manager.enable("test");

            assertThat(manager.isEnabled("test")).isTrue();
        }

        @Test
        @DisplayName("禁用已存在的功能")
        void testDisableExisting() {
            manager.withFeature("test", true);
            manager.disable("test");

            assertThat(manager.getDelegate().getOrThrow("test").defaultEnabled()).isFalse();
        }

        @Test
        @DisplayName("禁用不存在的功能自动注册")
        void testDisableNonExisting() {
            manager.disable("test");

            assertThat(manager.isEnabled("test")).isFalse();
        }
    }

    @Nested
    @DisplayName("enableAll()/disableAll() 测试")
    class BulkOperationTests {

        @Test
        @DisplayName("启用所有功能")
        void testEnableAll() {
            manager.withFeature("f1", false)
                    .withFeature("f2", false);

            manager.enableAll();

            assertThat(manager.getDelegate().getOrThrow("f1").defaultEnabled()).isTrue();
            assertThat(manager.getDelegate().getOrThrow("f2").defaultEnabled()).isTrue();
        }

        @Test
        @DisplayName("禁用所有功能")
        void testDisableAll() {
            manager.withFeature("f1", true)
                    .withFeature("f2", true);

            manager.disableAll();

            assertThat(manager.getDelegate().getOrThrow("f1").defaultEnabled()).isFalse();
            assertThat(manager.getDelegate().getOrThrow("f2").defaultEnabled()).isFalse();
        }
    }

    @Nested
    @DisplayName("isEnabled() 带上下文测试")
    class ContextTests {

        @Test
        @DisplayName("使用上下文检查")
        void testIsEnabledWithContext() {
            Feature feature = Feature.builder("test")
                    .forUsers("user1")
                    .build();
            manager.withFeature(feature);

            assertThat(manager.isEnabled("test", FeatureContext.ofUser("user1"))).isTrue();
            assertThat(manager.isEnabled("test", FeatureContext.ofUser("user2"))).isFalse();
        }
    }

    @Nested
    @DisplayName("close() 测试")
    class CloseTests {

        @Test
        @DisplayName("关闭后清空所有功能")
        void testCloseClears() {
            manager.withFeature("test", true);
            manager.close();

            assertThat(manager.getDelegate().size()).isZero();
        }
    }

    @Nested
    @DisplayName("getDelegate() 测试")
    class DelegateTests {

        @Test
        @DisplayName("返回底层OpenFeature")
        void testGetDelegate() {
            assertThat(manager.getDelegate()).isNotNull();
        }
    }
}
