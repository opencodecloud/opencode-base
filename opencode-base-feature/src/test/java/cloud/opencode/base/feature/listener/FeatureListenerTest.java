package cloud.opencode.base.feature.listener;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

/**
 * FeatureListener 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
@DisplayName("FeatureListener 测试")
class FeatureListenerTest {

    @Nested
    @DisplayName("函数式接口测试")
    class FunctionalInterfaceTests {

        @Test
        @DisplayName("可用作Lambda")
        void testAsLambda() {
            AtomicBoolean called = new AtomicBoolean(false);

            FeatureListener listener = (key, oldValue, newValue) -> called.set(true);
            listener.onFeatureChanged("test", false, true);

            assertThat(called.get()).isTrue();
        }

        @Test
        @DisplayName("可接收方法引用")
        void testMethodReference() {
            FeatureListener listener = this::captureChange;
            listener.onFeatureChanged("my-feature", false, true);

            assertThat(capturedKey.get()).isEqualTo("my-feature");
        }

        private final AtomicReference<String> capturedKey = new AtomicReference<>();

        private void captureChange(String key, boolean oldValue, boolean newValue) {
            capturedKey.set(key);
        }
    }

    @Nested
    @DisplayName("onFeatureChanged() 测试")
    class OnFeatureChangedTests {

        @Test
        @DisplayName("接收所有参数")
        void testReceivesAllParameters() {
            AtomicReference<String> capturedKey = new AtomicReference<>();
            AtomicBoolean capturedOld = new AtomicBoolean();
            AtomicBoolean capturedNew = new AtomicBoolean();

            FeatureListener listener = (key, oldValue, newValue) -> {
                capturedKey.set(key);
                capturedOld.set(oldValue);
                capturedNew.set(newValue);
            };

            listener.onFeatureChanged("feature-key", true, false);

            assertThat(capturedKey.get()).isEqualTo("feature-key");
            assertThat(capturedOld.get()).isTrue();
            assertThat(capturedNew.get()).isFalse();
        }

        @Test
        @DisplayName("启用状态变化")
        void testEnableStateChange() {
            AtomicBoolean wasEnabled = new AtomicBoolean(false);

            FeatureListener listener = (key, oldValue, newValue) -> {
                if (!oldValue && newValue) {
                    wasEnabled.set(true);
                }
            };

            listener.onFeatureChanged("test", false, true);

            assertThat(wasEnabled.get()).isTrue();
        }

        @Test
        @DisplayName("禁用状态变化")
        void testDisableStateChange() {
            AtomicBoolean wasDisabled = new AtomicBoolean(false);

            FeatureListener listener = (key, oldValue, newValue) -> {
                if (oldValue && !newValue) {
                    wasDisabled.set(true);
                }
            };

            listener.onFeatureChanged("test", true, false);

            assertThat(wasDisabled.get()).isTrue();
        }
    }

    @Nested
    @DisplayName("实现类测试")
    class ImplementationTests {

        @Test
        @DisplayName("可创建匿名实现")
        void testAnonymousImplementation() {
            FeatureListener listener = new FeatureListener() {
                @Override
                public void onFeatureChanged(String key, boolean oldValue, boolean newValue) {
                    // Implementation
                }
            };

            assertThat(listener).isNotNull();
        }
    }
}
