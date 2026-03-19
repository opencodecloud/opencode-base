package cloud.opencode.base.collections.specialized;

import cloud.opencode.base.collections.Range;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * RangeMap 接口测试
 * 通过 TreeRangeMap 实现类测试接口的所有方法
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("RangeMap 接口测试")
class RangeMapTest {

    @Nested
    @DisplayName("查询方法测试")
    class QueryMethodTests {

        @Test
        @DisplayName("get - 获取键对应的值")
        void testGet() {
            RangeMap<Integer, String> rangeMap = TreeRangeMap.create();
            rangeMap.put(Range.closed(1, 10), "small");

            assertThat(rangeMap.get(5)).isEqualTo("small");
        }

        @Test
        @DisplayName("get - 边界值")
        void testGetBoundary() {
            RangeMap<Integer, String> rangeMap = TreeRangeMap.create();
            rangeMap.put(Range.closed(1, 10), "small");

            assertThat(rangeMap.get(1)).isEqualTo("small");
            assertThat(rangeMap.get(10)).isEqualTo("small");
        }

        @Test
        @DisplayName("get - 不在任何范围内返回 null")
        void testGetOutsideRange() {
            RangeMap<Integer, String> rangeMap = TreeRangeMap.create();
            rangeMap.put(Range.closed(1, 10), "small");

            assertThat(rangeMap.get(0)).isNull();
            assertThat(rangeMap.get(11)).isNull();
        }

        @Test
        @DisplayName("get - 多个范围")
        void testGetMultipleRanges() {
            RangeMap<Integer, String> rangeMap = TreeRangeMap.create();
            rangeMap.put(Range.closed(1, 10), "small");
            rangeMap.put(Range.closed(11, 100), "medium");
            rangeMap.put(Range.closed(101, 1000), "large");

            assertThat(rangeMap.get(5)).isEqualTo("small");
            assertThat(rangeMap.get(50)).isEqualTo("medium");
            assertThat(rangeMap.get(500)).isEqualTo("large");
        }

        @Test
        @DisplayName("getEntry - 获取包含键的条目")
        void testGetEntry() {
            RangeMap<Integer, String> rangeMap = TreeRangeMap.create();
            rangeMap.put(Range.closed(1, 10), "small");

            Map.Entry<Range<Integer>, String> entry = rangeMap.getEntry(5);

            assertThat(entry).isNotNull();
            assertThat(entry.getKey()).isEqualTo(Range.closed(1, 10));
            assertThat(entry.getValue()).isEqualTo("small");
        }

        @Test
        @DisplayName("getEntry - 不存在返回 null")
        void testGetEntryNotExists() {
            RangeMap<Integer, String> rangeMap = TreeRangeMap.create();
            rangeMap.put(Range.closed(1, 10), "small");

            Map.Entry<Range<Integer>, String> entry = rangeMap.getEntry(20);

            assertThat(entry).isNull();
        }

        @Test
        @DisplayName("span - 获取跨度")
        void testSpan() {
            RangeMap<Integer, String> rangeMap = TreeRangeMap.create();
            rangeMap.put(Range.closed(1, 10), "small");
            rangeMap.put(Range.closed(20, 30), "medium");

            Range<Integer> span = rangeMap.span();

            assertThat(span).isEqualTo(Range.closed(1, 30));
        }

        @Test
        @DisplayName("isEmpty - 空判断")
        void testIsEmpty() {
            RangeMap<Integer, String> rangeMap = TreeRangeMap.create();

            assertThat(rangeMap.isEmpty()).isTrue();

            rangeMap.put(Range.closed(1, 10), "small");

            assertThat(rangeMap.isEmpty()).isFalse();
        }
    }

    @Nested
    @DisplayName("视图方法测试")
    class ViewMethodTests {

        @Test
        @DisplayName("asMapOfRanges - 返回范围-值映射")
        void testAsMapOfRanges() {
            RangeMap<Integer, String> rangeMap = TreeRangeMap.create();
            rangeMap.put(Range.closed(1, 10), "small");
            rangeMap.put(Range.closed(20, 30), "medium");

            Map<Range<Integer>, String> map = rangeMap.asMapOfRanges();

            assertThat(map).hasSize(2);
            assertThat(map.get(Range.closed(1, 10))).isEqualTo("small");
            assertThat(map.get(Range.closed(20, 30))).isEqualTo("medium");
        }

        @Test
        @DisplayName("asDescendingMapOfRanges - 降序返回")
        void testAsDescendingMapOfRanges() {
            RangeMap<Integer, String> rangeMap = TreeRangeMap.create();
            rangeMap.put(Range.closed(1, 10), "small");
            rangeMap.put(Range.closed(20, 30), "medium");

            Map<Range<Integer>, String> map = rangeMap.asDescendingMapOfRanges();

            assertThat(map).hasSize(2);
        }

        @Test
        @DisplayName("subRangeMap - 子范围视图")
        void testSubRangeMap() {
            RangeMap<Integer, String> rangeMap = TreeRangeMap.create();
            rangeMap.put(Range.closed(1, 10), "small");
            rangeMap.put(Range.closed(20, 30), "medium");
            rangeMap.put(Range.closed(40, 50), "large");

            RangeMap<Integer, String> subMap = rangeMap.subRangeMap(Range.closed(5, 35));

            assertThat(subMap.get(7)).isEqualTo("small");
            assertThat(subMap.get(25)).isEqualTo("medium");
            assertThat(subMap.get(45)).isNull();
        }
    }

    @Nested
    @DisplayName("修改方法测试")
    class ModificationMethodTests {

        @Test
        @DisplayName("put - 添加范围映射")
        void testPut() {
            RangeMap<Integer, String> rangeMap = TreeRangeMap.create();

            rangeMap.put(Range.closed(1, 10), "value");

            assertThat(rangeMap.get(5)).isEqualTo("value");
        }

        @Test
        @DisplayName("put - 覆盖部分范围")
        void testPutOverlap() {
            RangeMap<Integer, String> rangeMap = TreeRangeMap.create();
            rangeMap.put(Range.closed(1, 20), "old");

            rangeMap.put(Range.closed(5, 15), "new");

            assertThat(rangeMap.get(3)).isEqualTo("old");
            assertThat(rangeMap.get(10)).isEqualTo("new");
            assertThat(rangeMap.get(18)).isEqualTo("old");
        }

        @Test
        @DisplayName("putCoalescing - 合并相邻相同值范围")
        void testPutCoalescing() {
            RangeMap<Integer, String> rangeMap = TreeRangeMap.create();
            rangeMap.put(Range.closed(1, 10), "value");

            // [1,10] 和 [10,20] 相交，应合并为 [1,20]
            rangeMap.putCoalescing(Range.closed(10, 20), "value");

            assertThat(rangeMap.asMapOfRanges()).hasSize(1);
        }

        @Test
        @DisplayName("putAll - 从另一个 RangeMap 复制")
        void testPutAll() {
            RangeMap<Integer, String> source = TreeRangeMap.create();
            source.put(Range.closed(1, 10), "small");
            source.put(Range.closed(20, 30), "medium");

            RangeMap<Integer, String> rangeMap = TreeRangeMap.create();
            rangeMap.putAll(source);

            assertThat(rangeMap.get(5)).isEqualTo("small");
            assertThat(rangeMap.get(25)).isEqualTo("medium");
        }

        @Test
        @DisplayName("remove - 移除范围")
        void testRemove() {
            RangeMap<Integer, String> rangeMap = TreeRangeMap.create();
            rangeMap.put(Range.closed(1, 20), "value");

            rangeMap.remove(Range.closed(5, 15));

            assertThat(rangeMap.get(3)).isEqualTo("value");
            assertThat(rangeMap.get(10)).isNull();
            assertThat(rangeMap.get(18)).isEqualTo("value");
        }

        @Test
        @DisplayName("remove - 完全移除范围")
        void testRemoveComplete() {
            RangeMap<Integer, String> rangeMap = TreeRangeMap.create();
            rangeMap.put(Range.closed(1, 10), "value");

            rangeMap.remove(Range.closed(1, 10));

            assertThat(rangeMap.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("clear - 清空所有映射")
        void testClear() {
            RangeMap<Integer, String> rangeMap = TreeRangeMap.create();
            rangeMap.put(Range.closed(1, 10), "small");
            rangeMap.put(Range.closed(20, 30), "medium");

            rangeMap.clear();

            assertThat(rangeMap.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("开闭区间测试")
    class BoundTypeTests {

        @Test
        @DisplayName("closed - 闭区间")
        void testClosed() {
            RangeMap<Integer, String> rangeMap = TreeRangeMap.create();
            rangeMap.put(Range.closed(1, 10), "value");

            assertThat(rangeMap.get(1)).isEqualTo("value");
            assertThat(rangeMap.get(10)).isEqualTo("value");
        }

        @Test
        @DisplayName("open - 开区间")
        void testOpen() {
            RangeMap<Integer, String> rangeMap = TreeRangeMap.create();
            rangeMap.put(Range.open(1, 10), "value");

            assertThat(rangeMap.get(1)).isNull();
            assertThat(rangeMap.get(2)).isEqualTo("value");
            assertThat(rangeMap.get(9)).isEqualTo("value");
            assertThat(rangeMap.get(10)).isNull();
        }

        @Test
        @DisplayName("closedOpen - 左闭右开")
        void testClosedOpen() {
            RangeMap<Integer, String> rangeMap = TreeRangeMap.create();
            rangeMap.put(Range.closedOpen(1, 10), "value");

            assertThat(rangeMap.get(1)).isEqualTo("value");
            assertThat(rangeMap.get(10)).isNull();
        }

        @Test
        @DisplayName("openClosed - 左开右闭")
        void testOpenClosed() {
            RangeMap<Integer, String> rangeMap = TreeRangeMap.create();
            rangeMap.put(Range.openClosed(1, 10), "value");

            assertThat(rangeMap.get(1)).isNull();
            assertThat(rangeMap.get(10)).isEqualTo("value");
        }
    }

    @Nested
    @DisplayName("边界情况测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("单点范围")
        void testSingletonRange() {
            RangeMap<Integer, String> rangeMap = TreeRangeMap.create();
            rangeMap.put(Range.closed(5, 5), "single");

            assertThat(rangeMap.get(5)).isEqualTo("single");
            assertThat(rangeMap.get(4)).isNull();
            assertThat(rangeMap.get(6)).isNull();
        }

        @Test
        @DisplayName("多个不相交范围")
        void testDisjointRanges() {
            RangeMap<Integer, String> rangeMap = TreeRangeMap.create();
            rangeMap.put(Range.closed(1, 10), "a");
            rangeMap.put(Range.closed(20, 30), "b");
            rangeMap.put(Range.closed(40, 50), "c");

            assertThat(rangeMap.get(15)).isNull();
            assertThat(rangeMap.get(35)).isNull();
        }
    }

    @Nested
    @DisplayName("字符串键测试")
    class StringKeyTests {

        @Test
        @DisplayName("字符串范围")
        void testStringRange() {
            RangeMap<String, String> rangeMap = TreeRangeMap.create();
            rangeMap.put(Range.closed("a", "m"), "first half");
            rangeMap.put(Range.closed("n", "z"), "second half");

            assertThat(rangeMap.get("c")).isEqualTo("first half");
            assertThat(rangeMap.get("p")).isEqualTo("second half");
        }
    }
}
