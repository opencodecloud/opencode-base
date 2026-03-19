package cloud.opencode.base.collections.primitive;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.*;

/**
 * LongObjectMap 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("LongObjectMap 测试")
class LongObjectMapTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("create - 创建空映射")
        void testCreate() {
            LongObjectMap<String> map = LongObjectMap.create();

            assertThat(map.isEmpty()).isTrue();
            assertThat(map.size()).isZero();
        }

        @Test
        @DisplayName("create - 指定初始容量")
        void testCreateWithCapacity() {
            LongObjectMap<String> map = LongObjectMap.create(100);

            assertThat(map.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("放置操作测试")
    class PutTests {

        @Test
        @DisplayName("put - 放置键值")
        void testPut() {
            LongObjectMap<String> map = LongObjectMap.create();

            String old = map.put(1L, "one");

            assertThat(old).isNull();
            assertThat(map.get(1L)).isEqualTo("one");
        }

        @Test
        @DisplayName("put - 替换值")
        void testPutReplace() {
            LongObjectMap<String> map = LongObjectMap.create();
            map.put(1L, "one");

            String old = map.put(1L, "ONE");

            assertThat(old).isEqualTo("one");
            assertThat(map.get(1L)).isEqualTo("ONE");
        }

        @Test
        @DisplayName("put - 零键")
        void testPutZeroKey() {
            LongObjectMap<String> map = LongObjectMap.create();

            map.put(0L, "zero");

            assertThat(map.containsKey(0L)).isTrue();
            assertThat(map.get(0L)).isEqualTo("zero");
        }

        @Test
        @DisplayName("put - 负数键")
        void testPutNegativeKey() {
            LongObjectMap<String> map = LongObjectMap.create();

            map.put(-100L, "negative");

            assertThat(map.containsKey(-100L)).isTrue();
            assertThat(map.get(-100L)).isEqualTo("negative");
        }

        @Test
        @DisplayName("put - 大数值键")
        void testPutLargeKey() {
            LongObjectMap<String> map = LongObjectMap.create();

            map.put(Long.MAX_VALUE, "max");
            map.put(Long.MIN_VALUE, "min");

            assertThat(map.get(Long.MAX_VALUE)).isEqualTo("max");
            assertThat(map.get(Long.MIN_VALUE)).isEqualTo("min");
        }

    }

    @Nested
    @DisplayName("获取操作测试")
    class GetTests {

        @Test
        @DisplayName("get - 获取存在的值")
        void testGet() {
            LongObjectMap<String> map = LongObjectMap.create();
            map.put(1L, "one");

            assertThat(map.get(1L)).isEqualTo("one");
        }

        @Test
        @DisplayName("get - 获取不存在的值")
        void testGetNotExists() {
            LongObjectMap<String> map = LongObjectMap.create();

            assertThat(map.get(999L)).isNull();
        }

        @Test
        @DisplayName("getOrDefault - 获取或默认值")
        void testGetOrDefault() {
            LongObjectMap<String> map = LongObjectMap.create();
            map.put(1L, "one");

            assertThat(map.getOrDefault(1L, "default")).isEqualTo("one");
            assertThat(map.getOrDefault(999L, "default")).isEqualTo("default");
        }
    }

    @Nested
    @DisplayName("删除操作测试")
    class RemoveTests {

        @Test
        @DisplayName("remove - 删除存在的键")
        void testRemove() {
            LongObjectMap<String> map = LongObjectMap.create();
            map.put(1L, "one");

            String removed = map.remove(1L);

            assertThat(removed).isEqualTo("one");
            assertThat(map.containsKey(1L)).isFalse();
        }

        @Test
        @DisplayName("remove - 删除不存在的键")
        void testRemoveNotExists() {
            LongObjectMap<String> map = LongObjectMap.create();

            String removed = map.remove(999L);

            assertThat(removed).isNull();
        }

        @Test
        @DisplayName("clear - 清空")
        void testClear() {
            LongObjectMap<String> map = LongObjectMap.create();
            map.put(1L, "one");
            map.put(2L, "two");

            map.clear();

            assertThat(map.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("查询操作测试")
    class QueryTests {

        @Test
        @DisplayName("containsKey - 包含键")
        void testContainsKey() {
            LongObjectMap<String> map = LongObjectMap.create();
            map.put(1L, "one");

            assertThat(map.containsKey(1L)).isTrue();
            assertThat(map.containsKey(2L)).isFalse();
        }

        @Test
        @DisplayName("containsValue - 包含值")
        void testContainsValue() {
            LongObjectMap<String> map = LongObjectMap.create();
            map.put(1L, "one");

            assertThat(map.containsValue("one")).isTrue();
            assertThat(map.containsValue("two")).isFalse();
        }

        @Test
        @DisplayName("size - 大小")
        void testSize() {
            LongObjectMap<String> map = LongObjectMap.create();
            map.put(1L, "one");
            map.put(2L, "two");

            assertThat(map.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("isEmpty - 空判断")
        void testIsEmpty() {
            LongObjectMap<String> empty = LongObjectMap.create();
            LongObjectMap<String> nonEmpty = LongObjectMap.create();
            nonEmpty.put(1L, "one");

            assertThat(empty.isEmpty()).isTrue();
            assertThat(nonEmpty.isEmpty()).isFalse();
        }
    }

    @Nested
    @DisplayName("视图操作测试")
    class ViewTests {

        @Test
        @DisplayName("keySet - 键集")
        void testKeySet() {
            LongObjectMap<String> map = LongObjectMap.create();
            map.put(1L, "one");
            map.put(2L, "two");

            LongSet keySet = map.keySet();

            assertThat(keySet.size()).isEqualTo(2);
            assertThat(keySet.contains(1L)).isTrue();
            assertThat(keySet.contains(2L)).isTrue();
        }

        @Test
        @DisplayName("values - 值集合")
        void testValues() {
            LongObjectMap<String> map = LongObjectMap.create();
            map.put(1L, "one");
            map.put(2L, "two");

            Collection<String> values = map.values();

            assertThat(values).hasSize(2);
            assertThat(values).containsExactlyInAnyOrder("one", "two");
        }
    }

    @Nested
    @DisplayName("迭代器测试")
    class IteratorTests {

        @Test
        @DisplayName("forEach - 遍历")
        void testForEach() {
            LongObjectMap<String> map = LongObjectMap.create();
            map.put(1L, "one");
            map.put(2L, "two");

            AtomicLong keySum = new AtomicLong(0);
            List<String> values = new ArrayList<>();

            map.forEach((k, v) -> {
                keySum.addAndGet(k);
                values.add(v);
            });

            assertThat(keySum.get()).isEqualTo(3L);
            assertThat(values).containsExactlyInAnyOrder("one", "two");
        }
    }

    @Nested
    @DisplayName("扩容测试")
    class ResizeTests {

        @Test
        @DisplayName("大量元素扩容")
        void testResize() {
            LongObjectMap<String> map = LongObjectMap.create();

            for (long i = 0; i < 1000; i++) {
                map.put(i, "value-" + i);
            }

            assertThat(map.size()).isEqualTo(1000);
            for (long i = 0; i < 1000; i++) {
                assertThat(map.get(i)).isEqualTo("value-" + i);
            }
        }

        @Test
        @DisplayName("边界值测试")
        void testBoundaryValues() {
            LongObjectMap<String> map = LongObjectMap.create();

            map.put(Long.MIN_VALUE, "min");
            map.put(Long.MAX_VALUE, "max");
            map.put(0L, "zero");

            assertThat(map.get(Long.MIN_VALUE)).isEqualTo("min");
            assertThat(map.get(Long.MAX_VALUE)).isEqualTo("max");
            assertThat(map.get(0L)).isEqualTo("zero");
        }
    }

    @Nested
    @DisplayName("Object 方法测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals - 相等")
        void testEquals() {
            LongObjectMap<String> map1 = LongObjectMap.create();
            map1.put(1L, "one");

            LongObjectMap<String> map2 = LongObjectMap.create();
            map2.put(1L, "one");

            assertThat(map1).isEqualTo(map2);
        }

        @Test
        @DisplayName("hashCode - 哈希码")
        void testHashCode() {
            LongObjectMap<String> map1 = LongObjectMap.create();
            map1.put(1L, "one");

            LongObjectMap<String> map2 = LongObjectMap.create();
            map2.put(1L, "one");

            assertThat(map1.hashCode()).isEqualTo(map2.hashCode());
        }

        @Test
        @DisplayName("toString - 字符串表示")
        void testToString() {
            LongObjectMap<String> map = LongObjectMap.create();
            map.put(1L, "one");

            String str = map.toString();

            assertThat(str).contains("1");
            assertThat(str).contains("one");
        }
    }
}
