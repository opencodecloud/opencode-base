package cloud.opencode.base.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * DataEvent 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
@DisplayName("DataEvent 测试")
class DataEventTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用数据创建事件")
        void testConstructorWithData() {
            String data = "test data";
            DataEvent<String> event = new DataEvent<>(data);

            assertThat(event.getData()).isEqualTo(data);
            assertThat(event.getSource()).isNull();
        }

        @Test
        @DisplayName("使用数据和source创建事件")
        void testConstructorWithDataAndSource() {
            String data = "test data";
            DataEvent<String> event = new DataEvent<>(data, "TestService");

            assertThat(event.getData()).isEqualTo(data);
            assertThat(event.getSource()).isEqualTo("TestService");
        }

        @Test
        @DisplayName("使用null数据创建事件")
        void testConstructorWithNullData() {
            DataEvent<String> event = new DataEvent<>(null);

            assertThat(event.getData()).isNull();
        }

        @Test
        @DisplayName("继承Event属性")
        void testInheritsEventProperties() {
            DataEvent<String> event = new DataEvent<>("data");

            assertThat(event.getId()).isNotNull();
            assertThat(event.getTimestamp()).isNotNull();
            assertThat(event.isCancelled()).isFalse();
        }
    }

    @Nested
    @DisplayName("getData() 测试")
    class GetDataTests {

        @Test
        @DisplayName("获取字符串数据")
        void testGetStringData() {
            DataEvent<String> event = new DataEvent<>("hello");

            assertThat(event.getData()).isEqualTo("hello");
        }

        @Test
        @DisplayName("获取对象数据")
        void testGetObjectData() {
            List<Integer> data = List.of(1, 2, 3);
            DataEvent<List<Integer>> event = new DataEvent<>(data);

            assertThat(event.getData()).isEqualTo(data);
        }

        @Test
        @DisplayName("获取null数据")
        void testGetNullData() {
            DataEvent<String> event = new DataEvent<>(null);

            assertThat(event.getData()).isNull();
        }
    }

    @Nested
    @DisplayName("getDataType() 测试")
    class GetDataTypeTests {

        @Test
        @DisplayName("返回字符串类型")
        void testStringDataType() {
            DataEvent<String> event = new DataEvent<>("hello");

            assertThat(event.getDataType()).isEqualTo(String.class);
        }

        @Test
        @DisplayName("返回Integer类型")
        void testIntegerDataType() {
            DataEvent<Integer> event = new DataEvent<>(42);

            assertThat(event.getDataType()).isEqualTo(Integer.class);
        }

        @Test
        @DisplayName("返回List类型")
        void testListDataType() {
            DataEvent<List<String>> event = new DataEvent<>(List.of("a", "b"));

            assertThat(List.class.isAssignableFrom(event.getDataType())).isTrue();
        }

        @Test
        @DisplayName("null数据返回null类型")
        void testNullDataType() {
            DataEvent<String> event = new DataEvent<>(null);

            assertThat(event.getDataType()).isNull();
        }
    }

    @Nested
    @DisplayName("Event继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("可以取消事件")
        void testCanCancelEvent() {
            DataEvent<String> event = new DataEvent<>("data");

            assertThat(event.isCancelled()).isFalse();
            event.cancel();
            assertThat(event.isCancelled()).isTrue();
        }

        @Test
        @DisplayName("具有唯一ID")
        void testHasUniqueId() {
            DataEvent<String> event1 = new DataEvent<>("data1");
            DataEvent<String> event2 = new DataEvent<>("data2");

            assertThat(event1.getId()).isNotEqualTo(event2.getId());
        }
    }

    @Nested
    @DisplayName("toString() 测试")
    class ToStringTests {

        @Test
        @DisplayName("包含DataEvent")
        void testToStringContainsDataEvent() {
            DataEvent<String> event = new DataEvent<>("test");

            assertThat(event.toString()).contains("DataEvent");
        }

        @Test
        @DisplayName("包含数据")
        void testToStringContainsData() {
            DataEvent<String> event = new DataEvent<>("mydata");

            assertThat(event.toString()).contains("mydata");
        }

        @Test
        @DisplayName("包含ID")
        void testToStringContainsId() {
            DataEvent<String> event = new DataEvent<>("test");

            assertThat(event.toString()).contains(event.getId());
        }

        @Test
        @DisplayName("包含source")
        void testToStringContainsSource() {
            DataEvent<String> event = new DataEvent<>("test", "MyService");

            assertThat(event.toString()).contains("MyService");
        }
    }

    @Nested
    @DisplayName("类型安全测试")
    class TypeSafetyTests {

        @Test
        @DisplayName("自定义对象数据")
        void testCustomObjectData() {
            record Person(String name, int age) {}
            Person person = new Person("John", 30);

            DataEvent<Person> event = new DataEvent<>(person);

            assertThat(event.getData()).isEqualTo(person);
            assertThat(event.getData().name()).isEqualTo("John");
            assertThat(event.getData().age()).isEqualTo(30);
        }
    }
}
