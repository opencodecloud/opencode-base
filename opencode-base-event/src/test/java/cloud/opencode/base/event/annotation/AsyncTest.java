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
 * Async 注解测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
@DisplayName("Async 注解测试")
class AsyncTest {

    static class TestEvent extends Event {}

    @Nested
    @DisplayName("注解属性测试")
    class AnnotationAttributesTests {

        @Test
        @DisplayName("注解是运行时保留")
        void testRuntimeRetention() {
            Retention retention = Async.class.getAnnotation(Retention.class);

            assertThat(retention).isNotNull();
            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("可通过反射获取")
        void testReflectionAccess() throws NoSuchMethodException {
            class Subscriber {
                @Subscribe
                @Async
                public void onEvent(TestEvent event) {}
            }

            Method method = Subscriber.class.getMethod("onEvent", TestEvent.class);
            Async annotation = method.getAnnotation(Async.class);

            assertThat(annotation).isNotNull();
        }
    }

    @Nested
    @DisplayName("与Subscribe组合测试")
    class CombinationTests {

        @Test
        @DisplayName("可与Subscribe组合使用")
        void testCombinedWithSubscribe() throws NoSuchMethodException {
            class Subscriber {
                @Subscribe
                @Async
                public void asyncHandler(TestEvent event) {}
            }

            Method method = Subscriber.class.getMethod("asyncHandler", TestEvent.class);

            assertThat(method.isAnnotationPresent(Subscribe.class)).isTrue();
            assertThat(method.isAnnotationPresent(Async.class)).isTrue();
        }
    }
}
