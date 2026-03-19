package cloud.opencode.base.collections;

import org.junit.jupiter.api.*;

import java.util.Collection;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * MultimapTest Tests
 * MultimapTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("Multimap 接口测试")
class MultimapTest {

    @Nested
    @DisplayName("通过ArrayListMultimap测试Multimap接口")
    class ArrayListMultimapTests {

        @Test
        @DisplayName("put和get正确存储和获取多个值")
        void testPutAndGet() {
            Multimap<String, Integer> multimap = ArrayListMultimap.create();

            multimap.put("a", 1);
            multimap.put("a", 2);
            multimap.put("b", 3);

            assertThat(multimap.get("a")).containsExactly(1, 2);
            assertThat(multimap.get("b")).containsExactly(3);
        }

        @Test
        @DisplayName("size返回键值对总数")
        void testSize() {
            Multimap<String, Integer> multimap = ArrayListMultimap.create();

            multimap.put("a", 1);
            multimap.put("a", 2);
            multimap.put("b", 3);

            assertThat(multimap.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("isEmpty正确判断空状态")
        void testIsEmpty() {
            Multimap<String, Integer> multimap = ArrayListMultimap.create();

            assertThat(multimap.isEmpty()).isTrue();
            multimap.put("a", 1);
            assertThat(multimap.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("containsKey正确判断键是否存在")
        void testContainsKey() {
            Multimap<String, Integer> multimap = ArrayListMultimap.create();
            multimap.put("a", 1);

            assertThat(multimap.containsKey("a")).isTrue();
            assertThat(multimap.containsKey("b")).isFalse();
        }

        @Test
        @DisplayName("containsValue正确判断值是否存在")
        void testContainsValue() {
            Multimap<String, Integer> multimap = ArrayListMultimap.create();
            multimap.put("a", 1);

            assertThat(multimap.containsValue(1)).isTrue();
            assertThat(multimap.containsValue(99)).isFalse();
        }

        @Test
        @DisplayName("containsEntry正确判断键值对是否存在")
        void testContainsEntry() {
            Multimap<String, Integer> multimap = ArrayListMultimap.create();
            multimap.put("a", 1);

            assertThat(multimap.containsEntry("a", 1)).isTrue();
            assertThat(multimap.containsEntry("a", 99)).isFalse();
        }

        @Test
        @DisplayName("remove移除单个键值对")
        void testRemove() {
            Multimap<String, Integer> multimap = ArrayListMultimap.create();
            multimap.put("a", 1);
            multimap.put("a", 2);

            boolean removed = multimap.remove("a", 1);

            assertThat(removed).isTrue();
            assertThat(multimap.get("a")).containsExactly(2);
        }

        @Test
        @DisplayName("removeAll移除键的所有值")
        void testRemoveAll() {
            Multimap<String, Integer> multimap = ArrayListMultimap.create();
            multimap.put("a", 1);
            multimap.put("a", 2);

            Collection<Integer> removed = multimap.removeAll("a");

            assertThat(removed).containsExactly(1, 2);
            assertThat(multimap.containsKey("a")).isFalse();
        }

        @Test
        @DisplayName("keySet返回所有键")
        void testKeySet() {
            Multimap<String, Integer> multimap = ArrayListMultimap.create();
            multimap.put("a", 1);
            multimap.put("b", 2);

            assertThat(multimap.keySet()).containsExactlyInAnyOrder("a", "b");
        }

        @Test
        @DisplayName("clear清除所有条目")
        void testClear() {
            Multimap<String, Integer> multimap = ArrayListMultimap.create();
            multimap.put("a", 1);
            multimap.put("b", 2);

            multimap.clear();

            assertThat(multimap.isEmpty()).isTrue();
            assertThat(multimap.size()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("默认方法测试")
    class DefaultMethodTests {

        @Test
        @DisplayName("putAll从Map添加所有条目")
        void testPutAllFromMap() {
            Multimap<String, Integer> multimap = ArrayListMultimap.create();

            multimap.putAll(Map.of("a", 1, "b", 2));

            assertThat(multimap.size()).isEqualTo(2);
            assertThat(multimap.containsEntry("a", 1)).isTrue();
            assertThat(multimap.containsEntry("b", 2)).isTrue();
        }
    }
}
