package cloud.opencode.base.collections;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * MultisetTest Tests
 * MultisetTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("Multiset 接口测试")
class MultisetTest {

    @Nested
    @DisplayName("通过HashMultiset测试Multiset接口")
    class HashMultisetTests {

        @Test
        @DisplayName("add添加元素并计数")
        void testAddAndCount() {
            Multiset<String> multiset = HashMultiset.create();

            multiset.add("apple");
            multiset.add("apple", 3);

            assertThat(multiset.count("apple")).isEqualTo(4);
        }

        @Test
        @DisplayName("size返回元素总数")
        void testSize() {
            Multiset<String> multiset = HashMultiset.create();
            multiset.add("a", 3);
            multiset.add("b", 2);

            assertThat(multiset.size()).isEqualTo(5);
        }

        @Test
        @DisplayName("setCount设置元素计数")
        void testSetCount() {
            Multiset<String> multiset = HashMultiset.create();

            multiset.setCount("apple", 5);

            assertThat(multiset.count("apple")).isEqualTo(5);
        }

        @Test
        @DisplayName("remove减少元素计数")
        void testRemove() {
            Multiset<String> multiset = HashMultiset.create();
            multiset.add("apple", 5);

            multiset.remove("apple", 2);

            assertThat(multiset.count("apple")).isEqualTo(3);
        }

        @Test
        @DisplayName("elementSet返回去重元素")
        void testElementSet() {
            Multiset<String> multiset = HashMultiset.create();
            multiset.add("a", 3);
            multiset.add("b", 2);

            assertThat(multiset.elementSet()).containsExactlyInAnyOrder("a", "b");
        }

        @Test
        @DisplayName("entrySet返回带计数的条目")
        void testEntrySet() {
            Multiset<String> multiset = HashMultiset.create();
            multiset.add("apple", 3);

            assertThat(multiset.entrySet()).hasSize(1);
            Multiset.Entry<String> entry = multiset.entrySet().iterator().next();
            assertThat(entry.getElement()).isEqualTo("apple");
            assertThat(entry.getCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("contains检查元素是否存在")
        void testContains() {
            Multiset<String> multiset = HashMultiset.create();
            multiset.add("apple");

            assertThat(multiset.contains("apple")).isTrue();
            assertThat(multiset.contains("banana")).isFalse();
        }
    }
}
