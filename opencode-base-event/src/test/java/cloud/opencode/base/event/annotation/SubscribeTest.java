package cloud.opencode.base.event.annotation;

import cloud.opencode.base.event.Event;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.*;

/**
 * Subscribe 注解测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
@DisplayName("Subscribe 注解测试")
class SubscribeTest {

    static class TestEvent extends Event {}

    @Nested
    @DisplayName("注解属性测试")
    class AnnotationAttributesTests {

        @Test
        @DisplayName("注解是运行时保留")
        void testRuntimeRetention() {
            Retention retention = Subscribe.class.getAnnotation(Retention.class);

            assertThat(retention).isNotNull();
            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("可通过反射获取")
        void testReflectionAccess() throws NoSuchMethodException {
            class Subscriber {
                @Subscribe
                public void onEvent(TestEvent event) {}
            }

            Method method = Subscriber.class.getMethod("onEvent", TestEvent.class);
            Subscribe annotation = method.getAnnotation(Subscribe.class);

            assertThat(annotation).isNotNull();
        }
    }

    @Nested
    @DisplayName("应用位置测试")
    class TargetTests {

        @Test
        @DisplayName("可应用于方法")
        void testCanApplyToMethod() throws NoSuchMethodException {
            class Subscriber {
                @Subscribe
                public void handler(TestEvent event) {}
            }

            Method method = Subscriber.class.getMethod("handler", TestEvent.class);

            assertThat(method.isAnnotationPresent(Subscribe.class)).isTrue();
        }

        @Test
        @DisplayName("可应用于私有方法")
        void testCanApplyToPrivateMethod() throws NoSuchMethodException {
            class Subscriber {
                @Subscribe
                private void handler(TestEvent event) {}
            }

            Method method = Subscriber.class.getDeclaredMethod("handler", TestEvent.class);

            assertThat(method.isAnnotationPresent(Subscribe.class)).isTrue();
        }
    }
}
