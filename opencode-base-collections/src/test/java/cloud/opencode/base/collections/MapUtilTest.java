package cloud.opencode.base.collections;

import cloud.opencode.base.collections.exception.OpenCollectionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.ConcurrentMap;

import static org.assertj.core.api.Assertions.*;

/**
 * MapUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("MapUtil 测试")
class MapUtilTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("newHashMap - 空创建")
        void testNewHashMapEmpty() {
            HashMap<String, Integer> map = MapUtil.newHashMap();
            assertThat(map).isEmpty();
        }

        @Test
        @DisplayName("newHashMap - 从 Map 创建")
        void testNewHashMapFromMap() {
            Map<String, Integer> source = Map.of("a", 1, "b", 2);
            HashMap<String, Integer> map = MapUtil.newHashMap(source);
            assertThat(map).containsExactlyInAnyOrderEntriesOf(source);
        }

        @Test
        @DisplayName("newHashMap - 从 null Map 创建")
        void testNewHashMapFromNullMap() {
            HashMap<String, Integer> map = MapUtil.newHashMap(null);
            assertThat(map).isEmpty();
        }

        @Test
        @DisplayName("newHashMapWithExpectedSize - 预期大小")
        void testNewHashMapWithExpectedSize() {
            HashMap<String, Integer> map = MapUtil.newHashMapWithExpectedSize(10);
            assertThat(map).isEmpty();
        }

        @Test
        @DisplayName("newHashMapWithExpectedSize - 负大小")
        void testNewHashMapWithNegativeExpectedSize() {
            assertThatThrownBy(() -> MapUtil.newHashMapWithExpectedSize(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("newLinkedHashMap - 空创建")
        void testNewLinkedHashMapEmpty() {
            LinkedHashMap<String, Integer> map = MapUtil.newLinkedHashMap();
            assertThat(map).isEmpty();
        }

        @Test
        @DisplayName("newLinkedHashMapWithExpectedSize - 预期大小")
        void testNewLinkedHashMapWithExpectedSize() {
            LinkedHashMap<String, Integer> map = MapUtil.newLinkedHashMapWithExpectedSize(10);
            assertThat(map).isEmpty();
        }

        @Test
        @DisplayName("newLinkedHashMapWithExpectedSize - 负大小")
        void testNewLinkedHashMapWithNegativeExpectedSize() {
            assertThatThrownBy(() -> MapUtil.newLinkedHashMapWithExpectedSize(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("newTreeMap - 空创建")
        void testNewTreeMapEmpty() {
            TreeMap<String, Integer> map = MapUtil.newTreeMap();
            assertThat(map).isEmpty();
        }

        @Test
        @DisplayName("newTreeMap - 带比较器")
        void testNewTreeMapWithComparator() {
            TreeMap<String, Integer> map = MapUtil.newTreeMap(Comparator.reverseOrder());
            map.put("a", 1);
            map.put("b", 2);
            assertThat(map.firstKey()).isEqualTo("b");
        }

        @Test
        @DisplayName("newConcurrentMap - 空创建")
        void testNewConcurrentMapEmpty() {
            ConcurrentMap<String, Integer> map = MapUtil.newConcurrentMap();
            assertThat(map).isEmpty();
        }

        @Test
        @DisplayName("newIdentityHashMap - 空创建")
        void testNewIdentityHashMapEmpty() {
            IdentityHashMap<String, Integer> map = MapUtil.newIdentityHashMap();
            assertThat(map).isEmpty();
        }
    }

    @Nested
    @DisplayName("唯一索引测试")
    class UniqueIndexTests {

        @Test
        @DisplayName("uniqueIndex - Iterable")
        void testUniqueIndexIterable() {
            List<String> strings = List.of("a", "bb", "ccc");

            Map<Integer, String> result = MapUtil.uniqueIndex(strings, String::length);

            assertThat(result).containsEntry(1, "a");
            assertThat(result).containsEntry(2, "bb");
            assertThat(result).containsEntry(3, "ccc");
        }

        @Test
        @DisplayName("uniqueIndex - null 输入")
        void testUniqueIndexNullInput() {
            assertThat(MapUtil.uniqueIndex((Iterable<String>) null, String::length)).isEmpty();
            assertThat(MapUtil.uniqueIndex(List.of("a"), null)).isEmpty();
        }

        @Test
        @DisplayName("uniqueIndex - 重复键抛异常")
        void testUniqueIndexDuplicateKey() {
            List<String> strings = List.of("a", "b");

            assertThatThrownBy(() -> MapUtil.uniqueIndex(strings, String::length))
                    .isInstanceOf(OpenCollectionException.class)
                    .hasMessageContaining("Duplicate key");
        }

        @Test
        @DisplayName("uniqueIndex - Iterator")
        void testUniqueIndexIterator() {
            Iterator<String> iter = List.of("a", "bb").iterator();

            Map<Integer, String> result = MapUtil.uniqueIndex(iter, String::length);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("uniqueIndex - null Iterator")
        void testUniqueIndexNullIterator() {
            assertThat(MapUtil.uniqueIndex((Iterator<String>) null, String::length)).isEmpty();
            assertThat(MapUtil.uniqueIndex(List.of("a").iterator(), null)).isEmpty();
        }

        @Test
        @DisplayName("uniqueIndex - Iterator 重复键")
        void testUniqueIndexIteratorDuplicateKey() {
            Iterator<String> iter = List.of("a", "b").iterator();

            assertThatThrownBy(() -> MapUtil.uniqueIndex(iter, String::length))
                    .isInstanceOf(OpenCollectionException.class);
        }
    }

    @Nested
    @DisplayName("Map 差异测试")
    class MapDifferenceTests {

        @Test
        @DisplayName("difference - 计算差异")
        void testDifference() {
            Map<String, Integer> left = Map.of("a", 1, "b", 2, "c", 3);
            Map<String, Integer> right = Map.of("b", 2, "c", 30, "d", 4);

            MapDifference<String, Integer> diff = MapUtil.difference(left, right);

            assertThat(diff.entriesOnlyOnLeft()).containsEntry("a", 1);
            assertThat(diff.entriesOnlyOnRight()).containsEntry("d", 4);
            assertThat(diff.entriesInCommon()).containsEntry("b", 2);
            assertThat(diff.entriesDiffering()).containsKey("c");
            assertThat(diff.areEqual()).isFalse();
        }

        @Test
        @DisplayName("difference - 相等的 Map")
        void testDifferenceEqual() {
            Map<String, Integer> left = Map.of("a", 1, "b", 2);
            Map<String, Integer> right = Map.of("a", 1, "b", 2);

            MapDifference<String, Integer> diff = MapUtil.difference(left, right);

            assertThat(diff.areEqual()).isTrue();
            assertThat(diff.entriesOnlyOnLeft()).isEmpty();
            assertThat(diff.entriesOnlyOnRight()).isEmpty();
            assertThat(diff.entriesDiffering()).isEmpty();
        }

        @Test
        @DisplayName("difference - null 输入")
        void testDifferenceNull() {
            MapDifference<String, Integer> diff1 = MapUtil.difference(null, Map.of("a", 1));
            assertThat(diff1.entriesOnlyOnRight()).containsEntry("a", 1);

            MapDifference<String, Integer> diff2 = MapUtil.difference(Map.of("a", 1), null);
            assertThat(diff2.entriesOnlyOnLeft()).containsEntry("a", 1);
        }

        @Test
        @DisplayName("difference - ValueDifference")
        void testDifferenceValueDifference() {
            Map<String, Integer> left = Map.of("a", 1);
            Map<String, Integer> right = Map.of("a", 2);

            MapDifference<String, Integer> diff = MapUtil.difference(left, right);

            MapDifference.ValueDifference<Integer> valueDiff = diff.entriesDiffering().get("a");
            assertThat(valueDiff.leftValue()).isEqualTo(1);
            assertThat(valueDiff.rightValue()).isEqualTo(2);
        }

        @Test
        @DisplayName("difference - 带 Equivalence")
        void testDifferenceWithEquivalence() {
            Map<String, String> left = Map.of("a", "Hello");
            Map<String, String> right = Map.of("a", "HELLO");

            Equivalence<String> caseInsensitive = Equivalence.from(
                    (a, b) -> a.equalsIgnoreCase(b),
                    s -> s.toLowerCase().hashCode()
            );

            MapDifference<String, String> diff = MapUtil.difference(left, right, caseInsensitive);

            assertThat(diff.entriesInCommon()).containsEntry("a", "Hello");
            assertThat(diff.entriesDiffering()).isEmpty();
        }
    }

    @Nested
    @DisplayName("转换视图测试")
    class TransformViewTests {

        @Test
        @DisplayName("transformValues - 转换值")
        void testTransformValues() {
            Map<String, Integer> map = Map.of("a", 1, "b", 2);

            Map<String, String> result = MapUtil.transformValues(map, Object::toString);

            assertThat(result.get("a")).isEqualTo("1");
            assertThat(result.get("b")).isEqualTo("2");
            assertThat(result.size()).isEqualTo(2);
            assertThat(result.containsKey("a")).isTrue();
        }

        @Test
        @DisplayName("transformValues - null 输入")
        void testTransformValuesNull() {
            assertThat(MapUtil.transformValues(null, Object::toString)).isEmpty();
            assertThat(MapUtil.transformValues(Map.of("a", 1), null)).isEmpty();
        }

        @Test
        @DisplayName("transformValues - 不存在的键")
        void testTransformValuesNonExistentKey() {
            Map<String, Integer> map = Map.of("a", 1);
            Map<String, String> result = MapUtil.transformValues(map, Object::toString);
            assertThat(result.get("nonexistent")).isNull();
        }

        @Test
        @DisplayName("transformEntries - 转换条目")
        void testTransformEntries() {
            Map<String, Integer> map = Map.of("a", 1, "b", 2);

            Map<String, String> result = MapUtil.transformEntries(map,
                    (k, v) -> k + "=" + v);

            assertThat(result.get("a")).isEqualTo("a=1");
            assertThat(result.get("b")).isEqualTo("b=2");
        }

        @Test
        @DisplayName("transformEntries - null 输入")
        void testTransformEntriesNull() {
            assertThat(MapUtil.transformEntries(null, (k, v) -> v)).isEmpty();
            assertThat(MapUtil.transformEntries(Map.of("a", 1), null)).isEmpty();
        }

        @Test
        @DisplayName("transformEntries - 不存在的键")
        void testTransformEntriesNonExistentKey() {
            Map<String, Integer> map = Map.of("a", 1);
            Map<String, String> result = MapUtil.transformEntries(map, (k, v) -> k + v);
            assertThat(result.get("nonexistent")).isNull();
        }
    }

    @Nested
    @DisplayName("过滤视图测试")
    class FilterViewTests {

        @Test
        @DisplayName("filterKeys - 过滤键")
        void testFilterKeys() {
            Map<String, Integer> map = new HashMap<>();
            map.put("a", 1);
            map.put("ab", 2);
            map.put("abc", 3);

            Map<String, Integer> result = MapUtil.filterKeys(map, k -> k.length() > 1);

            assertThat(result).hasSize(2);
            assertThat(result).containsEntry("ab", 2);
            assertThat(result).containsEntry("abc", 3);
        }

        @Test
        @DisplayName("filterKeys - null 输入")
        void testFilterKeysNull() {
            assertThat(MapUtil.filterKeys(null, k -> true)).isEmpty();
            assertThat(MapUtil.filterKeys(Map.of("a", 1), null)).isEmpty();
        }

        @Test
        @DisplayName("filterValues - 过滤值")
        void testFilterValues() {
            Map<String, Integer> map = new HashMap<>();
            map.put("a", 1);
            map.put("b", 2);
            map.put("c", 3);

            Map<String, Integer> result = MapUtil.filterValues(map, v -> v > 1);

            assertThat(result).hasSize(2);
            assertThat(result).containsEntry("b", 2);
            assertThat(result).containsEntry("c", 3);
        }

        @Test
        @DisplayName("filterValues - null 输入")
        void testFilterValuesNull() {
            assertThat(MapUtil.filterValues(null, v -> true)).isEmpty();
            assertThat(MapUtil.filterValues(Map.of("a", 1), null)).isEmpty();
        }

        @Test
        @DisplayName("filterEntries - 过滤条目")
        void testFilterEntries() {
            Map<String, Integer> map = new HashMap<>();
            map.put("a", 1);
            map.put("b", 2);
            map.put("c", 3);

            Map<String, Integer> result = MapUtil.filterEntries(map,
                    e -> e.getKey().equals("a") || e.getValue() > 2);

            assertThat(result).hasSize(2);
            assertThat(result).containsEntry("a", 1);
            assertThat(result).containsEntry("c", 3);
        }

        @Test
        @DisplayName("filterEntries - null 输入")
        void testFilterEntriesNull() {
            assertThat(MapUtil.filterEntries(null, e -> true)).isEmpty();
            assertThat(MapUtil.filterEntries(Map.of("a", 1), null)).isEmpty();
        }

        @Test
        @DisplayName("filterEntries - get/containsKey")
        void testFilterEntriesGetContainsKey() {
            Map<String, Integer> map = new HashMap<>();
            map.put("a", 1);
            map.put("b", 2);

            Map<String, Integer> result = MapUtil.filterEntries(map, e -> e.getValue() > 1);

            assertThat(result.containsKey("a")).isFalse();
            assertThat(result.containsKey("b")).isTrue();
            assertThat(result.get("a")).isNull();
            assertThat(result.get("b")).isEqualTo(2);
            assertThat(result.get("nonexistent")).isNull();
        }
    }

    @Nested
    @DisplayName("辅助方法测试")
    class HelperMethodTests {

        @Test
        @DisplayName("fromProperties - 从 Properties 创建")
        void testFromProperties() {
            Properties props = new Properties();
            props.setProperty("key1", "value1");
            props.setProperty("key2", "value2");

            Map<String, String> result = MapUtil.fromProperties(props);

            assertThat(result).containsEntry("key1", "value1");
            assertThat(result).containsEntry("key2", "value2");
        }

        @Test
        @DisplayName("fromProperties - null")
        void testFromPropertiesNull() {
            assertThat(MapUtil.fromProperties(null)).isEmpty();
        }

        @Test
        @DisplayName("immutableEntry - 创建不可变条目")
        void testImmutableEntry() {
            Map.Entry<String, Integer> entry = MapUtil.immutableEntry("key", 42);

            assertThat(entry.getKey()).isEqualTo("key");
            assertThat(entry.getValue()).isEqualTo(42);
            assertThatThrownBy(() -> entry.setValue(100))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("capacity - 计算容量")
        void testCapacity() {
            assertThat(MapUtil.capacity(0)).isEqualTo(1);
            assertThat(MapUtil.capacity(1)).isEqualTo(2);
            assertThat(MapUtil.capacity(2)).isEqualTo(3);
            assertThat(MapUtil.capacity(3)).isEqualTo(4);
            assertThat(MapUtil.capacity(10)).isEqualTo(14);
        }
    }

    @Nested
    @DisplayName("BiMap 包装测试")
    class BiMapWrapperTests {

        @Test
        @DisplayName("synchronizedBiMap - 同步包装")
        void testSynchronizedBiMap() {
            BiMap<String, Integer> bimap = HashBiMap.create();
            bimap.put("a", 1);

            BiMap<String, Integer> syncBiMap = MapUtil.synchronizedBiMap(bimap);

            assertThat(syncBiMap.get("a")).isEqualTo(1);
            assertThat(syncBiMap.inverse().get(1)).isEqualTo("a");
            assertThat(syncBiMap.size()).isEqualTo(1);
            assertThat(syncBiMap.isEmpty()).isFalse();
            assertThat(syncBiMap.containsKey("a")).isTrue();
            assertThat(syncBiMap.containsValue(1)).isTrue();
        }

        @Test
        @DisplayName("synchronizedBiMap - 操作")
        void testSynchronizedBiMapOperations() {
            BiMap<String, Integer> bimap = HashBiMap.create();
            BiMap<String, Integer> syncBiMap = MapUtil.synchronizedBiMap(bimap);

            syncBiMap.put("a", 1);
            assertThat(syncBiMap.get("a")).isEqualTo(1);

            syncBiMap.forcePut("a", 2);
            assertThat(syncBiMap.get("a")).isEqualTo(2);

            syncBiMap.putAll(Map.of("b", 3));
            assertThat(syncBiMap.get("b")).isEqualTo(3);

            syncBiMap.remove("b");
            assertThat(syncBiMap.containsKey("b")).isFalse();

            assertThat(syncBiMap.keySet()).contains("a");
            assertThat(syncBiMap.values()).contains(2);
            assertThat(syncBiMap.entrySet()).isNotEmpty();

            syncBiMap.clear();
            assertThat(syncBiMap.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("unmodifiableBiMap - 不可修改包装")
        void testUnmodifiableBiMap() {
            BiMap<String, Integer> bimap = HashBiMap.create();
            bimap.put("a", 1);

            BiMap<String, Integer> unmodifiableBiMap = MapUtil.unmodifiableBiMap(bimap);

            assertThat(unmodifiableBiMap.get("a")).isEqualTo(1);
            assertThat(unmodifiableBiMap.size()).isEqualTo(1);
            assertThat(unmodifiableBiMap.isEmpty()).isFalse();
            assertThat(unmodifiableBiMap.containsKey("a")).isTrue();
            assertThat(unmodifiableBiMap.containsValue(1)).isTrue();
            assertThat(unmodifiableBiMap.keySet()).contains("a");
            assertThat(unmodifiableBiMap.values()).contains(1);
            assertThat(unmodifiableBiMap.entrySet()).isNotEmpty();
        }

        @Test
        @DisplayName("unmodifiableBiMap - 修改操作抛异常")
        void testUnmodifiableBiMapThrows() {
            BiMap<String, Integer> bimap = HashBiMap.create();
            BiMap<String, Integer> unmodifiableBiMap = MapUtil.unmodifiableBiMap(bimap);

            assertThatThrownBy(() -> unmodifiableBiMap.put("a", 1))
                    .isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> unmodifiableBiMap.forcePut("a", 1))
                    .isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> unmodifiableBiMap.remove("a"))
                    .isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> unmodifiableBiMap.putAll(Map.of("a", 1)))
                    .isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(unmodifiableBiMap::clear)
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("unmodifiableBiMap - inverse")
        void testUnmodifiableBiMapInverse() {
            BiMap<String, Integer> bimap = HashBiMap.create();
            bimap.put("a", 1);

            BiMap<String, Integer> unmodifiableBiMap = MapUtil.unmodifiableBiMap(bimap);
            BiMap<Integer, String> inverse = unmodifiableBiMap.inverse();

            assertThat(inverse.get(1)).isEqualTo("a");
            assertThatThrownBy(() -> inverse.put(2, "b"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
