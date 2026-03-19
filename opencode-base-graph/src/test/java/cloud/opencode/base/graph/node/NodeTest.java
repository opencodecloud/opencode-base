package cloud.opencode.base.graph.node;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Node 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
@DisplayName("Node 测试")
class NodeTest {

    @Nested
    @DisplayName("Node接口测试")
    class NodeInterfaceTests {

        @Test
        @DisplayName("Node.of()工厂方法")
        void testOfFactoryMethod() {
            Node<String> node = Node.of("A");

            assertThat(node).isNotNull();
            assertThat(node.getValue()).isEqualTo("A");
        }

        @Test
        @DisplayName("Node.of()支持不同类型")
        void testOfWithDifferentTypes() {
            Node<Integer> intNode = Node.of(42);
            Node<Double> doubleNode = Node.of(3.14);

            assertThat(intNode.getValue()).isEqualTo(42);
            assertThat(doubleNode.getValue()).isEqualTo(3.14);
        }

        @Test
        @DisplayName("Node.of()支持null值")
        void testOfWithNull() {
            Node<String> node = Node.of(null);

            assertThat(node.getValue()).isNull();
        }
    }

    @Nested
    @DisplayName("SimpleNode测试")
    class SimpleNodeTests {

        @Test
        @DisplayName("实现Node接口")
        void testImplementsNode() {
            Node<String> node = Node.of("test");

            assertThat(node).isInstanceOf(Node.class);
        }

        @Test
        @DisplayName("getValue()返回正确值")
        void testGetValue() {
            Node<String> node = Node.of("value");

            assertThat(node.getValue()).isEqualTo("value");
        }

        @Test
        @DisplayName("相等性测试")
        void testEquality() {
            Node<String> node1 = Node.of("A");
            Node<String> node2 = Node.of("A");
            Node<String> node3 = Node.of("B");

            assertThat(node1).isEqualTo(node2);
            assertThat(node1).isNotEqualTo(node3);
        }

        @Test
        @DisplayName("hashCode一致性")
        void testHashCode() {
            Node<String> node1 = Node.of("A");
            Node<String> node2 = Node.of("A");

            assertThat(node1.hashCode()).isEqualTo(node2.hashCode());
        }
    }

    @Nested
    @DisplayName("泛型测试")
    class GenericTests {

        @Test
        @DisplayName("Integer类型节点")
        void testIntegerNode() {
            Node<Integer> node = Node.of(100);

            assertThat(node.getValue()).isEqualTo(100);
        }

        @Test
        @DisplayName("自定义类型节点")
        void testCustomTypeNode() {
            record Person(String name, int age) {}
            Node<Person> node = Node.of(new Person("Alice", 30));

            assertThat(node.getValue().name()).isEqualTo("Alice");
            assertThat(node.getValue().age()).isEqualTo(30);
        }

        @Test
        @DisplayName("List类型节点")
        void testListNode() {
            Node<java.util.List<String>> node = Node.of(java.util.List.of("a", "b", "c"));

            assertThat(node.getValue()).hasSize(3);
            assertThat(node.getValue()).containsExactly("a", "b", "c");
        }
    }
}
