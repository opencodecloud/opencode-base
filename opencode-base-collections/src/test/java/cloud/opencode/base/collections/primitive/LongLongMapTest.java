package cloud.opencode.base.collections.primitive;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.*;

/**
 * LongLongMap 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.3
 */
@DisplayName("LongLongMap 测试")
class LongLongMapTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("create - 创建空映射")
        void testCreate() {
            LongLongMap map = LongLongMap.create();

            assertThat(map.isEmpty()).isTrue();
            assertThat(map.size()).isZero();
        }

        @Test
        @DisplayName("create - 指定初始容量")
        void testCreateWithCapacity() {
            LongLongMap map = LongLongMap.create(100);

            assertThat(map.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("of - 从键值对创建")
        void testOf() {
            LongLongMap map = LongLongMap.of(1L, 100L, 2L, 200L);

            assertThat(map.size()).isEqualTo(2);
            assertThat(map.get(1L)).isEqualTo(100L);
            assertThat(map.get(2L)).isEqualTo(200L);
        }

        @Test
        @DisplayName("of - 空参数")
        void testOfEmpty() {
            LongLongMap map = LongLongMap.of();

            assertThat(map.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("of - 奇数参数抛异常")
        void testOfOddArguments() {
            assertThatThrownBy(() -> LongLongMap.of(1L, 2L, 3L))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("放置操作测试")
    class PutTests {

        @Test
        @DisplayName("put - 放置键值")
        void testPut() {
            LongLongMap map = LongLongMap.create();

            map.put(1L, 100L);

            assertThat(map.get(1L)).isEqualTo(100L);
        }

        @Test
        @DisplayName("put - 替换值")
        void testPutReplace() {
            LongLongMap map = LongLongMap.create();
            map.put(1L, 100L);

            long old = map.put(1L, 200L);

            assertThat(old).isEqualTo(100L);
            assertThat(map.get(1L)).isEqualTo(200L);
        }

        @Test
        @DisplayName("put - 零键")
        void testPutZeroKey() {
            LongLongMap map = LongLongMap.create();

            map.put(0L, 100L);

            assertThat(map.containsKey(0L)).isTrue();
            assertThat(map.get(0L)).isEqualTo(100L);
        }

        @Test
        @DisplayName("put - 负数键")
        void testPutNegativeKey() {
            LongLongMap map = LongLongMap.create();

            map.put(-100L, 999L);

            assertThat(map.containsKey(-100L)).isTrue();
            assertThat(map.get(-100L)).isEqualTo(999L);
        }

        @Test
        @DisplayName("put - 零值")
        void testPutZeroValue() {
            LongLongMap map = LongLongMap.create();

            map.put(1L, 0L);

            assertThat(map.containsKey(1L)).isTrue();
            assertThat(map.get(1L)).isZero();
        }
    }

    @Nested
    @DisplayName("获取操作测试")
    class GetTests {

        @Test
        @DisplayName("get - 获取存在的值")
        void testGet() {
            LongLongMap map = LongLongMap.create();
            map.put(1L, 100L);

            assertThat(map.get(1L)).isEqualTo(100L);
        }

        @Test
        @DisplayName("get - 键不存在抛异常")
        void testGetNotFound() {
            LongLongMap map = LongLongMap.create();

            assertThatThrownBy(() -> map.get(999L))
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("getOrDefault - 获取或默认值")
        void testGetOrDefault() {
            LongLongMap map = LongLongMap.create();
            map.put(1L, 100L);

            assertThat(map.getOrDefault(1L, -1L)).isEqualTo(100L);
            assertThat(map.getOrDefault(999L, -1L)).isEqualTo(-1L);
        }
    }

    @Nested
    @DisplayName("删除操作测试")
    class RemoveTests {

        @Test
        @DisplayName("remove - 删除存在的键")
        void testRemove() {
            LongLongMap map = LongLongMap.create();
            map.put(1L, 100L);

            long removed = map.remove(1L);

            assertThat(removed).isEqualTo(100L);
            assertThat(map.containsKey(1L)).isFalse();
        }

        @Test
        @DisplayName("clear - 清空")
        void testClear() {
            LongLongMap map = LongLongMap.create();
            map.put(1L, 100L);
            map.put(2L, 200L);

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
            LongLongMap map = LongLongMap.create();
            map.put(1L, 100L);

            assertThat(map.containsKey(1L)).isTrue();
            assertThat(map.containsKey(2L)).isFalse();
        }

        @Test
        @DisplayName("containsValue - 包含值")
        void testContainsValue() {
            LongLongMap map = LongLongMap.create();
            map.put(1L, 100L);

            assertThat(map.containsValue(100L)).isTrue();
            assertThat(map.containsValue(200L)).isFalse();
        }

        @Test
        @DisplayName("size - 大小")
        void testSize() {
            LongLongMap map = LongLongMap.create();
            map.put(1L, 100L);
            map.put(2L, 200L);

            assertThat(map.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("isEmpty - 空判断")
        void testIsEmpty() {
            LongLongMap empty = LongLongMap.create();
            LongLongMap nonEmpty = LongLongMap.create();
            nonEmpty.put(1L, 100L);

            assertThat(empty.isEmpty()).isTrue();
            assertThat(nonEmpty.isEmpty()).isFalse();
        }
    }

    @Nested
    @DisplayName("视图操作测试")
    class ViewTests {

        @Test
        @DisplayName("keys - 键数组")
        void testKeys() {
            LongLongMap map = LongLongMap.create();
            map.put(1L, 100L);
            map.put(2L, 200L);

            long[] keys = map.keys();

            assertThat(keys).hasSize(2);
            assertThat(keys).containsExactlyInAnyOrder(1L, 2L);
        }

        @Test
        @DisplayName("values - 值数组")
        void testValues() {
            LongLongMap map = LongLongMap.create();
            map.put(1L, 100L);
            map.put(2L, 200L);

            long[] values = map.values();

            assertThat(values).hasSize(2);
            assertThat(values).containsExactlyInAnyOrder(100L, 200L);
        }
    }

    @Nested
    @DisplayName("迭代器测试")
    class IteratorTests {

        @Test
        @DisplayName("forEach - 遍历")
        void testForEach() {
            LongLongMap map = LongLongMap.create();
            map.put(1L, 100L);
            map.put(2L, 200L);

            AtomicLong keySum = new AtomicLong(0);
            AtomicLong valueSum = new AtomicLong(0);

            map.forEach((k, v) -> {
                keySum.addAndGet(k);
                valueSum.addAndGet(v);
            });

            assertThat(keySum.get()).isEqualTo(3L);
            assertThat(valueSum.get()).isEqualTo(300L);
        }
    }

    @Nested
    @DisplayName("扩容测试")
    class ResizeTests {

        @Test
        @DisplayName("大量元素扩容")
        void testResize() {
            LongLongMap map = LongLongMap.create();

            for (long i = 0; i < 1000; i++) {
                map.put(i, i * 10);
            }

            assertThat(map.size()).isEqualTo(1000);
            for (long i = 0; i < 1000; i++) {
                assertThat(map.get(i)).isEqualTo(i * 10);
            }
        }

        @Test
        @DisplayName("边界值测试")
        void testBoundaryValues() {
            LongLongMap map = LongLongMap.create();

            map.put(Long.MIN_VALUE, 1L);
            map.put(Long.MAX_VALUE, 2L);
            map.put(0L, 3L);

            assertThat(map.get(Long.MIN_VALUE)).isEqualTo(1L);
            assertThat(map.get(Long.MAX_VALUE)).isEqualTo(2L);
            assertThat(map.get(0L)).isEqualTo(3L);
        }
    }

    @Nested
    @DisplayName("Object 方法测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals - 相等")
        void testEquals() {
            LongLongMap map1 = LongLongMap.create();
            map1.put(1L, 100L);

            LongLongMap map2 = LongLongMap.create();
            map2.put(1L, 100L);

            assertThat(map1).isEqualTo(map2);
        }

        @Test
        @DisplayName("equals - 不相等")
        void testNotEquals() {
            LongLongMap map1 = LongLongMap.create();
            map1.put(1L, 100L);

            LongLongMap map2 = LongLongMap.create();
            map2.put(1L, 200L);

            assertThat(map1).isNotEqualTo(map2);
        }

        @Test
        @DisplayName("hashCode - 哈希码")
        void testHashCode() {
            LongLongMap map1 = LongLongMap.create();
            map1.put(1L, 100L);

            LongLongMap map2 = LongLongMap.create();
            map2.put(1L, 100L);

            assertThat(map1.hashCode()).isEqualTo(map2.hashCode());
        }

        @Test
        @DisplayName("toString - 字符串表示")
        void testToString() {
            LongLongMap map = LongLongMap.create();
            map.put(1L, 100L);

            String str = map.toString();

            assertThat(str).contains("1");
            assertThat(str).contains("100");
        }
    }
}
