package cloud.opencode.base.collections.primitive;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * IntObjectMap 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("IntObjectMap 测试")
class IntObjectMapTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("create - 创建空映射")
        void testCreate() {
            IntObjectMap<String> map = IntObjectMap.create();

            assertThat(map.isEmpty()).isTrue();
            assertThat(map.size()).isZero();
        }

        @Test
        @DisplayName("create - 指定初始容量")
        void testCreateWithCapacity() {
            IntObjectMap<String> map = IntObjectMap.create(100);

            assertThat(map.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("放置操作测试")
    class PutTests {

        @Test
        @DisplayName("put - 放置键值")
        void testPut() {
            IntObjectMap<String> map = IntObjectMap.create();

            String old = map.put(1, "one");

            assertThat(old).isNull();
            assertThat(map.get(1)).isEqualTo("one");
        }

        @Test
        @DisplayName("put - 替换值")
        void testPutReplace() {
            IntObjectMap<String> map = IntObjectMap.create();
            map.put(1, "one");

            String old = map.put(1, "ONE");

            assertThat(old).isEqualTo("one");
            assertThat(map.get(1)).isEqualTo("ONE");
        }

        @Test
        @DisplayName("put - 零键")
        void testPutZeroKey() {
            IntObjectMap<String> map = IntObjectMap.create();

            map.put(0, "zero");

            assertThat(map.containsKey(0)).isTrue();
            assertThat(map.get(0)).isEqualTo("zero");
        }

        @Test
        @DisplayName("put - 负数键")
        void testPutNegativeKey() {
            IntObjectMap<String> map = IntObjectMap.create();

            map.put(-100, "negative");

            assertThat(map.containsKey(-100)).isTrue();
            assertThat(map.get(-100)).isEqualTo("negative");
        }

        @Test
        @DisplayName("put - null 值抛异常")
        void testPutNullValue() {
            IntObjectMap<String> map = IntObjectMap.create();

            assertThatThrownBy(() -> map.put(1, null))
                    .isInstanceOf(NullPointerException.class);
        }

    }

    @Nested
    @DisplayName("获取操作测试")
    class GetTests {

        @Test
        @DisplayName("get - 获取存在的值")
        void testGet() {
            IntObjectMap<String> map = IntObjectMap.create();
            map.put(1, "one");

            assertThat(map.get(1)).isEqualTo("one");
        }

        @Test
        @DisplayName("get - 获取不存在的值")
        void testGetNotExists() {
            IntObjectMap<String> map = IntObjectMap.create();

            assertThat(map.get(999)).isNull();
        }

        @Test
        @DisplayName("getOrDefault - 获取或默认值")
        void testGetOrDefault() {
            IntObjectMap<String> map = IntObjectMap.create();
            map.put(1, "one");

            assertThat(map.getOrDefault(1, "default")).isEqualTo("one");
            assertThat(map.getOrDefault(999, "default")).isEqualTo("default");
        }
    }

    @Nested
    @DisplayName("删除操作测试")
    class RemoveTests {

        @Test
        @DisplayName("remove - 删除存在的键")
        void testRemove() {
            IntObjectMap<String> map = IntObjectMap.create();
            map.put(1, "one");

            String removed = map.remove(1);

            assertThat(removed).isEqualTo("one");
            assertThat(map.containsKey(1)).isFalse();
        }

        @Test
        @DisplayName("remove - 删除不存在的键")
        void testRemoveNotExists() {
            IntObjectMap<String> map = IntObjectMap.create();

            String removed = map.remove(999);

            assertThat(removed).isNull();
        }

        @Test
        @DisplayName("clear - 清空")
        void testClear() {
            IntObjectMap<String> map = IntObjectMap.create();
            map.put(1, "one");
            map.put(2, "two");

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
            IntObjectMap<String> map = IntObjectMap.create();
            map.put(1, "one");

            assertThat(map.containsKey(1)).isTrue();
            assertThat(map.containsKey(2)).isFalse();
        }

        @Test
        @DisplayName("containsValue - 包含值")
        void testContainsValue() {
            IntObjectMap<String> map = IntObjectMap.create();
            map.put(1, "one");

            assertThat(map.containsValue("one")).isTrue();
            assertThat(map.containsValue("two")).isFalse();
        }

        @Test
        @DisplayName("size - 大小")
        void testSize() {
            IntObjectMap<String> map = IntObjectMap.create();
            map.put(1, "one");
            map.put(2, "two");

            assertThat(map.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("isEmpty - 空判断")
        void testIsEmpty() {
            IntObjectMap<String> empty = IntObjectMap.create();
            IntObjectMap<String> nonEmpty = IntObjectMap.create();
            nonEmpty.put(1, "one");

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
            IntObjectMap<String> map = IntObjectMap.create();
            map.put(1, "one");
            map.put(2, "two");

            IntSet keySet = map.keySet();

            assertThat(keySet.size()).isEqualTo(2);
            assertThat(keySet.contains(1)).isTrue();
            assertThat(keySet.contains(2)).isTrue();
        }

        @Test
        @DisplayName("values - 值集合")
        void testValues() {
            IntObjectMap<String> map = IntObjectMap.create();
            map.put(1, "one");
            map.put(2, "two");

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
            IntObjectMap<String> map = IntObjectMap.create();
            map.put(1, "one");
            map.put(2, "two");

            AtomicInteger keySum = new AtomicInteger(0);
            List<String> values = new ArrayList<>();

            map.forEach((k, v) -> {
                keySum.addAndGet(k);
                values.add(v);
            });

            assertThat(keySum.get()).isEqualTo(3);
            assertThat(values).containsExactlyInAnyOrder("one", "two");
        }
    }

    @Nested
    @DisplayName("扩容测试")
    class ResizeTests {

        @Test
        @DisplayName("大量元素扩容")
        void testResize() {
            IntObjectMap<String> map = IntObjectMap.create();

            for (int i = 0; i < 1000; i++) {
                map.put(i, "value-" + i);
            }

            assertThat(map.size()).isEqualTo(1000);
            for (int i = 0; i < 1000; i++) {
                assertThat(map.get(i)).isEqualTo("value-" + i);
            }
        }

        @Test
        @DisplayName("边界值测试")
        void testBoundaryValues() {
            IntObjectMap<String> map = IntObjectMap.create();

            map.put(Integer.MIN_VALUE, "min");
            map.put(Integer.MAX_VALUE, "max");
            map.put(0, "zero");

            assertThat(map.get(Integer.MIN_VALUE)).isEqualTo("min");
            assertThat(map.get(Integer.MAX_VALUE)).isEqualTo("max");
            assertThat(map.get(0)).isEqualTo("zero");
        }
    }

    @Nested
    @DisplayName("Object 方法测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals - 相等")
        void testEquals() {
            IntObjectMap<String> map1 = IntObjectMap.create();
            map1.put(1, "one");

            IntObjectMap<String> map2 = IntObjectMap.create();
            map2.put(1, "one");

            assertThat(map1).isEqualTo(map2);
        }

        @Test
        @DisplayName("hashCode - 哈希码")
        void testHashCode() {
            IntObjectMap<String> map1 = IntObjectMap.create();
            map1.put(1, "one");

            IntObjectMap<String> map2 = IntObjectMap.create();
            map2.put(1, "one");

            assertThat(map1.hashCode()).isEqualTo(map2.hashCode());
        }

        @Test
        @DisplayName("toString - 字符串表示")
        void testToString() {
            IntObjectMap<String> map = IntObjectMap.create();
            map.put(1, "one");

            String str = map.toString();

            assertThat(str).contains("1");
            assertThat(str).contains("one");
        }
    }
}
