package cloud.opencode.base.collections.primitive;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * IntIntMap 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("IntIntMap 测试")
class IntIntMapTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("create - 创建空映射")
        void testCreate() {
            IntIntMap map = IntIntMap.create();

            assertThat(map.isEmpty()).isTrue();
            assertThat(map.size()).isZero();
        }

        @Test
        @DisplayName("create - 指定初始容量")
        void testCreateWithCapacity() {
            IntIntMap map = IntIntMap.create(100);

            assertThat(map.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("放置操作测试")
    class PutTests {

        @Test
        @DisplayName("put - 放置键值")
        void testPut() {
            IntIntMap map = IntIntMap.create();

            map.put(1, 100);

            assertThat(map.get(1)).isEqualTo(100);
        }

        @Test
        @DisplayName("put - 替换值")
        void testPutReplace() {
            IntIntMap map = IntIntMap.create();
            map.put(1, 100);

            int old = map.put(1, 200);

            assertThat(old).isEqualTo(100);
            assertThat(map.get(1)).isEqualTo(200);
        }

        @Test
        @DisplayName("put - 零键")
        void testPutZeroKey() {
            IntIntMap map = IntIntMap.create();

            map.put(0, 100);

            assertThat(map.containsKey(0)).isTrue();
            assertThat(map.get(0)).isEqualTo(100);
        }

        @Test
        @DisplayName("put - 负数键")
        void testPutNegativeKey() {
            IntIntMap map = IntIntMap.create();

            map.put(-100, 999);

            assertThat(map.containsKey(-100)).isTrue();
            assertThat(map.get(-100)).isEqualTo(999);
        }

        @Test
        @DisplayName("put - 零值")
        void testPutZeroValue() {
            IntIntMap map = IntIntMap.create();

            map.put(1, 0);

            assertThat(map.containsKey(1)).isTrue();
            assertThat(map.get(1)).isZero();
        }

    }

    @Nested
    @DisplayName("获取操作测试")
    class GetTests {

        @Test
        @DisplayName("get - 获取存在的值")
        void testGet() {
            IntIntMap map = IntIntMap.create();
            map.put(1, 100);

            assertThat(map.get(1)).isEqualTo(100);
        }

        @Test
        @DisplayName("getOrDefault - 获取或默认值")
        void testGetOrDefault() {
            IntIntMap map = IntIntMap.create();
            map.put(1, 100);

            assertThat(map.getOrDefault(1, -1)).isEqualTo(100);
            assertThat(map.getOrDefault(999, -1)).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("删除操作测试")
    class RemoveTests {

        @Test
        @DisplayName("remove - 删除存在的键")
        void testRemove() {
            IntIntMap map = IntIntMap.create();
            map.put(1, 100);

            int removed = map.remove(1);

            assertThat(removed).isEqualTo(100);
            assertThat(map.containsKey(1)).isFalse();
        }

        @Test
        @DisplayName("clear - 清空")
        void testClear() {
            IntIntMap map = IntIntMap.create();
            map.put(1, 100);
            map.put(2, 200);

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
            IntIntMap map = IntIntMap.create();
            map.put(1, 100);

            assertThat(map.containsKey(1)).isTrue();
            assertThat(map.containsKey(2)).isFalse();
        }

        @Test
        @DisplayName("containsValue - 包含值")
        void testContainsValue() {
            IntIntMap map = IntIntMap.create();
            map.put(1, 100);

            assertThat(map.containsValue(100)).isTrue();
            assertThat(map.containsValue(200)).isFalse();
        }

        @Test
        @DisplayName("size - 大小")
        void testSize() {
            IntIntMap map = IntIntMap.create();
            map.put(1, 100);
            map.put(2, 200);

            assertThat(map.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("isEmpty - 空判断")
        void testIsEmpty() {
            IntIntMap empty = IntIntMap.create();
            IntIntMap nonEmpty = IntIntMap.create();
            nonEmpty.put(1, 100);

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
            IntIntMap map = IntIntMap.create();
            map.put(1, 100);
            map.put(2, 200);

            IntSet keySet = map.keySet();

            assertThat(keySet.size()).isEqualTo(2);
            assertThat(keySet.contains(1)).isTrue();
            assertThat(keySet.contains(2)).isTrue();
        }

        @Test
        @DisplayName("valuesToArray - 值数组")
        void testValuesToArray() {
            IntIntMap map = IntIntMap.create();
            map.put(1, 100);
            map.put(2, 200);

            int[] values = map.valuesToArray();

            assertThat(values).hasSize(2);
            assertThat(values).containsExactlyInAnyOrder(100, 200);
        }
    }

    @Nested
    @DisplayName("迭代器测试")
    class IteratorTests {

        @Test
        @DisplayName("forEach - 遍历")
        void testForEach() {
            IntIntMap map = IntIntMap.create();
            map.put(1, 100);
            map.put(2, 200);

            AtomicInteger keySum = new AtomicInteger(0);
            AtomicInteger valueSum = new AtomicInteger(0);

            map.forEach((k, v) -> {
                keySum.addAndGet(k);
                valueSum.addAndGet(v);
            });

            assertThat(keySum.get()).isEqualTo(3);
            assertThat(valueSum.get()).isEqualTo(300);
        }
    }

    @Nested
    @DisplayName("扩容测试")
    class ResizeTests {

        @Test
        @DisplayName("大量元素扩容")
        void testResize() {
            IntIntMap map = IntIntMap.create();

            for (int i = 0; i < 1000; i++) {
                map.put(i, i * 10);
            }

            assertThat(map.size()).isEqualTo(1000);
            for (int i = 0; i < 1000; i++) {
                assertThat(map.get(i)).isEqualTo(i * 10);
            }
        }

        @Test
        @DisplayName("边界值测试")
        void testBoundaryValues() {
            IntIntMap map = IntIntMap.create();

            map.put(Integer.MIN_VALUE, 1);
            map.put(Integer.MAX_VALUE, 2);
            map.put(0, 3);

            assertThat(map.get(Integer.MIN_VALUE)).isEqualTo(1);
            assertThat(map.get(Integer.MAX_VALUE)).isEqualTo(2);
            assertThat(map.get(0)).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Object 方法测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals - 相等")
        void testEquals() {
            IntIntMap map1 = IntIntMap.create();
            map1.put(1, 100);

            IntIntMap map2 = IntIntMap.create();
            map2.put(1, 100);

            assertThat(map1).isEqualTo(map2);
        }

        @Test
        @DisplayName("hashCode - 哈希码")
        void testHashCode() {
            IntIntMap map1 = IntIntMap.create();
            map1.put(1, 100);

            IntIntMap map2 = IntIntMap.create();
            map2.put(1, 100);

            assertThat(map1.hashCode()).isEqualTo(map2.hashCode());
        }

        @Test
        @DisplayName("toString - 字符串表示")
        void testToString() {
            IntIntMap map = IntIntMap.create();
            map.put(1, 100);

            String str = map.toString();

            assertThat(str).contains("1");
            assertThat(str).contains("100");
        }
    }
}
