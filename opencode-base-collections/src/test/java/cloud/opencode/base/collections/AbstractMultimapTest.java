package cloud.opencode.base.collections;

import org.junit.jupiter.api.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * AbstractMultimapTest Tests
 * AbstractMultimapTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("AbstractMultimap 测试")
class AbstractMultimapTest {

    @Nested
    @DisplayName("通过ArrayListMultimap测试AbstractMultimap功能")
    class AbstractMultimapFunctionalTests {

        @Test
        @DisplayName("putAll(key, iterable)添加多个值")
        void testPutAllWithIterable() {
            Multimap<String, Integer> multimap = ArrayListMultimap.create();

            boolean changed = multimap.putAll("nums", List.of(1, 2, 3));

            assertThat(changed).isTrue();
            assertThat(multimap.get("nums")).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("replaceValues替换键的所有值")
        void testReplaceValues() {
            Multimap<String, Integer> multimap = ArrayListMultimap.create();
            multimap.put("a", 1);
            multimap.put("a", 2);

            Collection<Integer> old = multimap.replaceValues("a", List.of(10, 20));

            assertThat(old).containsExactly(1, 2);
            assertThat(multimap.get("a")).containsExactly(10, 20);
        }

        @Test
        @DisplayName("entries返回所有键值对")
        void testEntries() {
            Multimap<String, Integer> multimap = ArrayListMultimap.create();
            multimap.put("a", 1);
            multimap.put("b", 2);

            Collection<Map.Entry<String, Integer>> entries = multimap.entries();

            assertThat(entries).hasSize(2);
        }

        @Test
        @DisplayName("values返回所有值")
        void testValues() {
            Multimap<String, Integer> multimap = ArrayListMultimap.create();
            multimap.put("a", 1);
            multimap.put("a", 2);
            multimap.put("b", 3);

            assertThat(multimap.values()).containsExactlyInAnyOrder(1, 2, 3);
        }

        @Test
        @DisplayName("asMap返回不可修改的映射视图")
        void testAsMap() {
            Multimap<String, Integer> multimap = ArrayListMultimap.create();
            multimap.put("a", 1);

            var map = multimap.asMap();

            assertThat(map).containsKey("a");
        }

        @Test
        @DisplayName("keys返回键的Multiset")
        void testKeys() {
            Multimap<String, Integer> multimap = ArrayListMultimap.create();
            multimap.put("a", 1);
            multimap.put("a", 2);
            multimap.put("b", 3);

            Multiset<String> keys = multimap.keys();

            assertThat(keys.count("a")).isEqualTo(2);
            assertThat(keys.count("b")).isEqualTo(1);
        }
    }
}
