package cloud.opencode.base.collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.ConcurrentMap;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenMap 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("OpenMap 测试")
class OpenMapTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("newHashMap - 创建空 HashMap")
        void testNewHashMap() {
            HashMap<String, Integer> map = OpenMap.newHashMap();

            assertThat(map).isEmpty();
            assertThat(map).isInstanceOf(HashMap.class);
        }

        @Test
        @DisplayName("of - 创建包含一个条目的 HashMap")
        void testOfOne() {
            HashMap<String, Integer> map = OpenMap.of("a", 1);

            assertThat(map).hasSize(1);
            assertThat(map.get("a")).isEqualTo(1);
        }

        @Test
        @DisplayName("of - 创建包含两个条目的 HashMap")
        void testOfTwo() {
            HashMap<String, Integer> map = OpenMap.of("a", 1, "b", 2);

            assertThat(map).hasSize(2);
            assertThat(map.get("a")).isEqualTo(1);
            assertThat(map.get("b")).isEqualTo(2);
        }

        @Test
        @DisplayName("of - 创建包含三个条目的 HashMap")
        void testOfThree() {
            HashMap<String, Integer> map = OpenMap.of("a", 1, "b", 2, "c", 3);

            assertThat(map).hasSize(3);
            assertThat(map.get("a")).isEqualTo(1);
            assertThat(map.get("b")).isEqualTo(2);
            assertThat(map.get("c")).isEqualTo(3);
        }

        @Test
        @DisplayName("from - 从另一个 Map 创建")
        void testFrom() {
            Map<String, Integer> source = Map.of("a", 1, "b", 2);
            HashMap<String, Integer> map = OpenMap.from(source);

            assertThat(map).containsEntry("a", 1);
            assertThat(map).containsEntry("b", 2);
        }

        @Test
        @DisplayName("from - null Map")
        void testFromNull() {
            HashMap<String, Integer> map = OpenMap.from(null);

            assertThat(map).isEmpty();
        }

        @Test
        @DisplayName("withExpectedSize - 指定预期大小")
        void testWithExpectedSize() {
            HashMap<String, Integer> map = OpenMap.withExpectedSize(100);

            assertThat(map).isEmpty();
        }

        @Test
        @DisplayName("newLinkedHashMap - 创建空 LinkedHashMap")
        void testNewLinkedHashMap() {
            LinkedHashMap<String, Integer> map = OpenMap.newLinkedHashMap();

            assertThat(map).isEmpty();
            assertThat(map).isInstanceOf(LinkedHashMap.class);
        }

        @Test
        @DisplayName("newTreeMap - 创建空 TreeMap")
        void testNewTreeMap() {
            TreeMap<String, Integer> map = OpenMap.newTreeMap();

            assertThat(map).isEmpty();
            assertThat(map).isInstanceOf(TreeMap.class);
        }

        @Test
        @DisplayName("newTreeMap - 带比较器")
        void testNewTreeMapWithComparator() {
            TreeMap<String, Integer> map = OpenMap.newTreeMap(Comparator.reverseOrder());
            map.put("a", 1);
            map.put("b", 2);
            map.put("c", 3);

            assertThat(new ArrayList<>(map.keySet())).containsExactly("c", "b", "a");
        }

        @Test
        @DisplayName("newConcurrentMap - 创建 ConcurrentHashMap")
        void testNewConcurrentMap() {
            ConcurrentMap<String, Integer> map = OpenMap.newConcurrentMap();

            assertThat(map).isEmpty();
        }

        @Test
        @DisplayName("newIdentityHashMap - 创建 IdentityHashMap")
        void testNewIdentityHashMap() {
            IdentityHashMap<String, Integer> map = OpenMap.newIdentityHashMap();

            assertThat(map).isEmpty();
            assertThat(map).isInstanceOf(IdentityHashMap.class);
        }
    }

    @Nested
    @DisplayName("转换操作测试")
    class TransformationTests {

        @Test
        @DisplayName("transformValues - 转换值")
        void testTransformValues() {
            Map<String, Integer> map = Map.of("a", 1, "b", 2, "c", 3);

            Map<String, String> result = OpenMap.transformValues(map, Object::toString);

            assertThat(result.get("a")).isEqualTo("1");
            assertThat(result.get("b")).isEqualTo("2");
            assertThat(result.get("c")).isEqualTo("3");
        }

        @Test
        @DisplayName("transformValues - null Map")
        void testTransformValuesNull() {
            Map<String, String> result = OpenMap.transformValues(null, Object::toString);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("transformValues - 空 Map")
        void testTransformValuesEmpty() {
            Map<String, String> result = OpenMap.transformValues(Map.of(), Object::toString);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("transformEntries - 转换条目")
        void testTransformEntries() {
            Map<String, Integer> map = new LinkedHashMap<>();
            map.put("a", 1);
            map.put("b", 2);

            Map<String, String> result = OpenMap.transformEntries(map, (k, v) -> k + "=" + v);

            assertThat(result.get("a")).isEqualTo("a=1");
            assertThat(result.get("b")).isEqualTo("b=2");
        }

        @Test
        @DisplayName("transformEntries - null Map")
        void testTransformEntriesNull() {
            Map<String, String> result = OpenMap.transformEntries(null, (k, v) -> k + "=" + v);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("过滤操作测试")
    class FilterTests {

        @Test
        @DisplayName("filterKeys - 按键过滤")
        void testFilterKeys() {
            Map<String, Integer> map = Map.of("a1", 1, "b2", 2, "a3", 3);

            Map<String, Integer> result = OpenMap.filterKeys(map, k -> k.startsWith("a"));

            assertThat(result).hasSize(2);
            assertThat(result).containsKeys("a1", "a3");
        }

        @Test
        @DisplayName("filterKeys - null Map")
        void testFilterKeysNull() {
            Map<String, Integer> result = OpenMap.filterKeys(null, k -> true);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("filterValues - 按值过滤")
        void testFilterValues() {
            Map<String, Integer> map = Map.of("a", 1, "b", 2, "c", 3, "d", 4);

            Map<String, Integer> result = OpenMap.filterValues(map, v -> v > 2);

            assertThat(result).hasSize(2);
            assertThat(result).containsValues(3, 4);
        }

        @Test
        @DisplayName("filterValues - null Map")
        void testFilterValuesNull() {
            Map<String, Integer> result = OpenMap.filterValues(null, v -> true);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("filterEntries - 按条目过滤")
        void testFilterEntries() {
            Map<String, Integer> map = Map.of("a", 1, "b", 2, "c", 3);

            Map<String, Integer> result = OpenMap.filterEntries(map,
                    e -> e.getKey().equals("a") || e.getValue() > 2);

            assertThat(result).hasSize(2);
            assertThat(result).containsKeys("a", "c");
        }

        @Test
        @DisplayName("filterEntries - null Map")
        void testFilterEntriesNull() {
            Map<String, Integer> result = OpenMap.filterEntries(null, e -> true);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("索引操作测试")
    class IndexTests {

        @Test
        @DisplayName("uniqueIndex - 按唯一键创建索引")
        void testUniqueIndex() {
            List<String> values = List.of("a", "bb", "ccc");

            Map<Integer, String> result = OpenMap.uniqueIndex(values, String::length);

            assertThat(result.get(1)).isEqualTo("a");
            assertThat(result.get(2)).isEqualTo("bb");
            assertThat(result.get(3)).isEqualTo("ccc");
        }

        @Test
        @DisplayName("uniqueIndex - 重复键抛异常")
        void testUniqueIndexDuplicate() {
            List<String> values = List.of("a", "b");

            assertThatThrownBy(() -> OpenMap.uniqueIndex(values, String::length))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Duplicate key");
        }
    }

    @Nested
    @DisplayName("查询操作测试")
    class QueryTests {

        @Test
        @DisplayName("getOrDefault - 获取值或默认值")
        void testGetOrDefault() {
            Map<String, Integer> map = Map.of("a", 1);

            assertThat(OpenMap.getOrDefault(map, "a", 0)).isEqualTo(1);
            assertThat(OpenMap.getOrDefault(map, "b", 0)).isZero();
        }

        @Test
        @DisplayName("getOrDefault - null Map")
        void testGetOrDefaultNull() {
            Integer result = OpenMap.getOrDefault(null, "a", 42);

            assertThat(result).isEqualTo(42);
        }

        @Test
        @DisplayName("containsAllKeys - 包含所有键")
        void testContainsAllKeys() {
            Map<String, Integer> map = Map.of("a", 1, "b", 2, "c", 3);

            assertThat(OpenMap.containsAllKeys(map, "a", "b")).isTrue();
            assertThat(OpenMap.containsAllKeys(map, "a", "d")).isFalse();
        }

        @Test
        @DisplayName("containsAllKeys - null Map")
        void testContainsAllKeysNull() {
            assertThat(OpenMap.containsAllKeys(null, "a")).isFalse();
        }

        @Test
        @DisplayName("immutableEntry - 创建不可变条目")
        void testImmutableEntry() {
            Map.Entry<String, Integer> entry = OpenMap.immutableEntry("a", 1);

            assertThat(entry.getKey()).isEqualTo("a");
            assertThat(entry.getValue()).isEqualTo(1);
        }
    }
}
