package cloud.opencode.base.cache.spi;

import cloud.opencode.base.cache.model.RemovalCause;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * RemovalListenerTest Tests
 * RemovalListenerTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("RemovalListener 接口测试")
class RemovalListenerTest {

    @Nested
    @DisplayName("函数式接口测试")
    class FunctionalInterfaceTests {

        @Test
        @DisplayName("Lambda表达式实现onRemoval")
        void testLambdaOnRemoval() {
            List<String> removals = new ArrayList<>();
            RemovalListener<String, String> listener = (key, value, cause) ->
                    removals.add(key + ":" + cause);

            listener.onRemoval("key1", "val1", RemovalCause.EXPLICIT);

            assertThat(removals).containsExactly("key1:EXPLICIT");
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("noOp创建空操作监听器")
        void testNoOp() {
            RemovalListener<String, String> listener = RemovalListener.noOp();

            assertThatNoException().isThrownBy(() ->
                    listener.onRemoval("key", "value", RemovalCause.EXPLICIT));
        }

        @Test
        @DisplayName("logging创建日志监听器")
        void testLogging() {
            RemovalListener<String, String> listener = RemovalListener.logging();

            assertThatNoException().isThrownBy(() ->
                    listener.onRemoval("key", "value", RemovalCause.EXPIRED));
        }

        @Test
        @DisplayName("combine组合多个监听器")
        void testCombine() {
            List<String> events = new ArrayList<>();
            RemovalListener<String, String> l1 = (k, v, c) -> events.add("l1:" + k);
            RemovalListener<String, String> l2 = (k, v, c) -> events.add("l2:" + k);

            RemovalListener<String, String> combined = RemovalListener.combine(l1, l2);
            combined.onRemoval("test", "value", RemovalCause.EXPLICIT);

            assertThat(events).containsExactly("l1:test", "l2:test");
        }
    }
}
