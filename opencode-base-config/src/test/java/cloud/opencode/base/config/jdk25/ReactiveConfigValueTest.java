package cloud.opencode.base.config.jdk25;

import cloud.opencode.base.config.Config;
import cloud.opencode.base.config.ConfigBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.*;

/**
 * ReactiveConfigValue 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("ReactiveConfigValue 测试")
class ReactiveConfigValueTest {

    private Config config;

    @BeforeEach
    void setUp() {
        config = new ConfigBuilder()
                .addProperties(Map.of(
                        "app.name", "TestApp",
                        "log.level", "INFO",
                        "max.count", "100"
                ))
                .disablePlaceholders()
                .build();
    }

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用of工厂方法创建")
        void testOfFactoryMethod() {
            ReactiveConfigValue<String> value = ReactiveConfigValue.of(
                    config, "app.name", String.class, "default");

            assertThat(value).isNotNull();
        }

        @Test
        @DisplayName("使用构造函数创建")
        void testConstructor() {
            ReactiveConfigValue<String> value = new ReactiveConfigValue<>(
                    config, "app.name", String.class, "default");

            assertThat(value).isNotNull();
        }
    }

    @Nested
    @DisplayName("get测试")
    class GetTests {

        @Test
        @DisplayName("获取当前值")
        void testGet() {
            ReactiveConfigValue<String> value = ReactiveConfigValue.of(
                    config, "app.name", String.class, "default");

            assertThat(value.get()).isEqualTo("TestApp");
        }

        @Test
        @DisplayName("获取不存在的键 - 返回默认值")
        void testGetWithDefault() {
            ReactiveConfigValue<String> value = ReactiveConfigValue.of(
                    config, "missing.key", String.class, "default-value");

            assertThat(value.get()).isEqualTo("default-value");
        }

        @Test
        @DisplayName("获取整数值")
        void testGetIntegerValue() {
            ReactiveConfigValue<Integer> value = ReactiveConfigValue.of(
                    config, "max.count", Integer.class, 0);

            assertThat(value.get()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("subscribe测试")
    class SubscribeTests {

        @Test
        @DisplayName("订阅后立即收到当前值")
        void testSubscribeReceivesCurrentValue() {
            List<String> received = new CopyOnWriteArrayList<>();

            ReactiveConfigValue<String> value = ReactiveConfigValue.of(
                    config, "app.name", String.class, "default");
            value.subscribe(received::add);

            assertThat(received).contains("TestApp");
        }

        @Test
        @DisplayName("链式调用subscribe")
        void testSubscribeChaining() {
            List<String> received = new CopyOnWriteArrayList<>();

            ReactiveConfigValue<String> value = ReactiveConfigValue.of(
                            config, "app.name", String.class, "default")
                    .subscribe(received::add);

            assertThat(value).isNotNull();
        }
    }

    @Nested
    @DisplayName("unsubscribe测试")
    class UnsubscribeTests {

        @Test
        @DisplayName("取消订阅")
        void testUnsubscribe() {
            List<String> received = new ArrayList<>();
            java.util.function.Consumer<String> subscriber = received::add;

            ReactiveConfigValue<String> value = ReactiveConfigValue.of(
                    config, "app.name", String.class, "default");
            value.subscribe(subscriber);
            value.unsubscribe(subscriber);

            // 取消订阅后不应该再收到通知
            // 由于是初始订阅,仍会有一条初始值
            assertThat(received).hasSize(1);
        }
    }
}
