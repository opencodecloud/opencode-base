package cloud.opencode.base.reflect.lambda;

import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * SerializableConsumerTest Tests
 * SerializableConsumerTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("SerializableConsumer 测试")
class SerializableConsumerTest {

    @Nested
    @DisplayName("accept方法测试")
    class AcceptTests {

        @Test
        @DisplayName("执行消费")
        void testAccept() {
            List<String> list = new ArrayList<>();
            SerializableConsumer<String> consumer = list::add;
            consumer.accept("test");
            assertThat(list).containsExactly("test");
        }
    }

    @Nested
    @DisplayName("of工厂方法测试")
    class OfTests {

        @Test
        @DisplayName("创建SerializableConsumer")
        void testOf() {
            List<String> list = new ArrayList<>();
            SerializableConsumer<String> consumer = SerializableConsumer.of(list::add);
            consumer.accept("test");
            assertThat(list).containsExactly("test");
        }
    }

    @Nested
    @DisplayName("noOp工厂方法测试")
    class NoOpTests {

        @Test
        @DisplayName("创建空操作消费者")
        void testNoOp() {
            SerializableConsumer<String> consumer = SerializableConsumer.noOp();
            assertThatCode(() -> consumer.accept("test")).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("andThen方法测试")
    class AndThenTests {

        @Test
        @DisplayName("链接消费者")
        void testAndThen() {
            List<String> list1 = new ArrayList<>();
            List<String> list2 = new ArrayList<>();
            SerializableConsumer<String> c1 = list1::add;
            SerializableConsumer<String> c2 = list2::add;
            SerializableConsumer<String> chained = c1.andThen(c2);

            chained.accept("test");

            assertThat(list1).containsExactly("test");
            assertThat(list2).containsExactly("test");
        }
    }

    @Nested
    @DisplayName("Serializable测试")
    class SerializableTests {

        @Test
        @DisplayName("消费者是可序列化的")
        void testSerializable() {
            SerializableConsumer<String> consumer = s -> {};
            assertThat(consumer).isInstanceOf(java.io.Serializable.class);
        }
    }
}
