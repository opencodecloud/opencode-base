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
 * Priority 注解测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
@DisplayName("Priority 注解测试")
class PriorityTest {

    static class TestEvent extends Event {}

    @Nested
    @DisplayName("注解属性测试")
    class AnnotationAttributesTests {

        @Test
        @DisplayName("注解是运行时保留")
        void testRuntimeRetention() {
            Retention retention = Priority.class.getAnnotation(Retention.class);

            assertThat(retention).isNotNull();
            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("默认值为0")
        void testDefaultValue() throws NoSuchMethodException {
            class Subscriber {
                @Subscribe
                @Priority
                public void onEvent(TestEvent event) {}
            }

            Method method = Subscriber.class.getMethod("onEvent", TestEvent.class);
            Priority annotation = method.getAnnotation(Priority.class);

            assertThat(annotation.value()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("value属性测试")
    class ValueTests {

        @Test
        @DisplayName("可设置正值")
        void testPositiveValue() throws NoSuchMethodException {
            class Subscriber {
                @Subscribe
                @Priority(100)
                public void highPriority(TestEvent event) {}
            }

            Method method = Subscriber.class.getMethod("highPriority", TestEvent.class);
            Priority annotation = method.getAnnotation(Priority.class);

            assertThat(annotation.value()).isEqualTo(100);
        }

        @Test
        @DisplayName("可设置负值")
        void testNegativeValue() throws NoSuchMethodException {
            class Subscriber {
                @Subscribe
                @Priority(-50)
                public void lowPriority(TestEvent event) {}
            }

            Method method = Subscriber.class.getMethod("lowPriority", TestEvent.class);
            Priority annotation = method.getAnnotation(Priority.class);

            assertThat(annotation.value()).isEqualTo(-50);
        }

        @Test
        @DisplayName("可设置最大值")
        void testMaxValue() throws NoSuchMethodException {
            class Subscriber {
                @Subscribe
                @Priority(Integer.MAX_VALUE)
                public void maxPriority(TestEvent event) {}
            }

            Method method = Subscriber.class.getMethod("maxPriority", TestEvent.class);
            Priority annotation = method.getAnnotation(Priority.class);

            assertThat(annotation.value()).isEqualTo(Integer.MAX_VALUE);
        }
    }

    @Nested
    @DisplayName("与其他注解组合测试")
    class CombinationTests {

        @Test
        @DisplayName("可与Subscribe和Async组合")
        void testCombinedWithSubscribeAndAsync() throws NoSuchMethodException {
            class Subscriber {
                @Subscribe
                @Async
                @Priority(50)
                public void asyncHighPriority(TestEvent event) {}
            }

            Method method = Subscriber.class.getMethod("asyncHighPriority", TestEvent.class);

            assertThat(method.isAnnotationPresent(Subscribe.class)).isTrue();
            assertThat(method.isAnnotationPresent(Async.class)).isTrue();
            assertThat(method.getAnnotation(Priority.class).value()).isEqualTo(50);
        }
    }
}
