package cloud.opencode.base.collections.specialized;

import cloud.opencode.base.collections.Range;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * TreeRangeMap 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("TreeRangeMap 测试")
class TreeRangeMapTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("create - 创建空 RangeMap")
        void testCreate() {
            TreeRangeMap<Integer, String> rangeMap = TreeRangeMap.create();

            assertThat(rangeMap.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("查询方法测试")
    class QueryMethodTests {

        @Test
        @DisplayName("get - 获取值")
        void testGet() {
            TreeRangeMap<Integer, String> rangeMap = TreeRangeMap.create();
            rangeMap.put(Range.closed(1, 10), "small");

            assertThat(rangeMap.get(5)).isEqualTo("small");
            assertThat(rangeMap.get(15)).isNull();
        }

        @Test
        @DisplayName("getEntry - 获取条目")
        void testGetEntry() {
            TreeRangeMap<Integer, String> rangeMap = TreeRangeMap.create();
            rangeMap.put(Range.closed(1, 10), "small");

            Map.Entry<Range<Integer>, String> entry = rangeMap.getEntry(5);

            assertThat(entry).isNotNull();
            assertThat(entry.getKey().lowerEndpoint()).isEqualTo(1);
            assertThat(entry.getKey().upperEndpoint()).isEqualTo(10);
            assertThat(entry.getValue()).isEqualTo("small");
        }

        @Test
        @DisplayName("getEntry - 不存在返回 null")
        void testGetEntryNotFound() {
            TreeRangeMap<Integer, String> rangeMap = TreeRangeMap.create();
            rangeMap.put(Range.closed(1, 10), "small");

            Map.Entry<Range<Integer>, String> entry = rangeMap.getEntry(15);

            assertThat(entry).isNull();
        }

        @Test
        @DisplayName("span - 跨度")
        void testSpan() {
            TreeRangeMap<Integer, String> rangeMap = TreeRangeMap.create();
            rangeMap.put(Range.closed(1, 10), "small");
            rangeMap.put(Range.closed(20, 30), "large");

            Range<Integer> span = rangeMap.span();

            assertThat(span.lowerEndpoint()).isEqualTo(1);
            assertThat(span.upperEndpoint()).isEqualTo(30);
        }

        @Test
        @DisplayName("span - 空映射抛异常")
        void testSpanEmpty() {
            TreeRangeMap<Integer, String> rangeMap = TreeRangeMap.create();

            assertThatThrownBy(rangeMap::span)
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("asMapOfRanges - 获取范围映射")
        void testAsMapOfRanges() {
            TreeRangeMap<Integer, String> rangeMap = TreeRangeMap.create();
            rangeMap.put(Range.closed(1, 10), "small");
            rangeMap.put(Range.closed(20, 30), "large");

            Map<Range<Integer>, String> map = rangeMap.asMapOfRanges();

            assertThat(map).hasSize(2);
        }

        @Test
        @DisplayName("asDescendingMapOfRanges - 降序获取范围映射")
        void testAsDescendingMapOfRanges() {
            TreeRangeMap<Integer, String> rangeMap = TreeRangeMap.create();
            rangeMap.put(Range.closed(1, 10), "small");
            rangeMap.put(Range.closed(20, 30), "large");

            Map<Range<Integer>, String> map = rangeMap.asDescendingMapOfRanges();

            assertThat(map).hasSize(2);
        }
    }

    @Nested
    @DisplayName("修改方法测试")
    class ModificationMethodTests {

        @Test
        @DisplayName("put - 放置范围值")
        void testPut() {
            TreeRangeMap<Integer, String> rangeMap = TreeRangeMap.create();

            rangeMap.put(Range.closed(1, 10), "value");

            assertThat(rangeMap.get(5)).isEqualTo("value");
        }

        @Test
        @DisplayName("put - 空范围不放置")
        void testPutEmptyRange() {
            TreeRangeMap<Integer, String> rangeMap = TreeRangeMap.create();

            rangeMap.put(Range.closedOpen(5, 5), "value");

            assertThat(rangeMap.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("put - 覆盖重叠范围")
        void testPutOverlapping() {
            TreeRangeMap<Integer, String> rangeMap = TreeRangeMap.create();
            rangeMap.put(Range.closed(1, 20), "original");

            rangeMap.put(Range.closed(5, 15), "new");

            assertThat(rangeMap.get(3)).isEqualTo("original");
            assertThat(rangeMap.get(10)).isEqualTo("new");
            assertThat(rangeMap.get(18)).isEqualTo("original");
        }

        @Test
        @DisplayName("putCoalescing - 合并相邻同值范围")
        void testPutCoalescing() {
            TreeRangeMap<Integer, String> rangeMap = TreeRangeMap.create();
            rangeMap.put(Range.closed(1, 5), "value");

            rangeMap.putCoalescing(Range.closed(5, 10), "value");

            Map<Range<Integer>, String> map = rangeMap.asMapOfRanges();
            assertThat(map).hasSize(1);
        }

        @Test
        @DisplayName("putAll - 批量放置")
        void testPutAll() {
            TreeRangeMap<Integer, String> source = TreeRangeMap.create();
            source.put(Range.closed(1, 10), "small");

            TreeRangeMap<Integer, String> rangeMap = TreeRangeMap.create();
            rangeMap.putAll(source);

            assertThat(rangeMap.get(5)).isEqualTo("small");
        }

        @Test
        @DisplayName("remove - 移除范围")
        void testRemove() {
            TreeRangeMap<Integer, String> rangeMap = TreeRangeMap.create();
            rangeMap.put(Range.closed(1, 20), "value");

            rangeMap.remove(Range.closed(5, 15));

            assertThat(rangeMap.get(3)).isEqualTo("value");
            assertThat(rangeMap.get(10)).isNull();
            assertThat(rangeMap.get(18)).isEqualTo("value");
        }

        @Test
        @DisplayName("remove - 空范围不移除")
        void testRemoveEmptyRange() {
            TreeRangeMap<Integer, String> rangeMap = TreeRangeMap.create();
            rangeMap.put(Range.closed(1, 10), "value");

            rangeMap.remove(Range.closedOpen(5, 5));

            assertThat(rangeMap.asMapOfRanges()).hasSize(1);
        }

        @Test
        @DisplayName("clear - 清空")
        void testClear() {
            TreeRangeMap<Integer, String> rangeMap = TreeRangeMap.create();
            rangeMap.put(Range.closed(1, 10), "value");

            rangeMap.clear();

            assertThat(rangeMap.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("视图方法测试")
    class ViewMethodTests {

        @Test
        @DisplayName("subRangeMap - 子范围映射")
        void testSubRangeMap() {
            TreeRangeMap<Integer, String> rangeMap = TreeRangeMap.create();
            rangeMap.put(Range.closed(1, 20), "value");

            RangeMap<Integer, String> subMap = rangeMap.subRangeMap(Range.closed(5, 15));

            assertThat(subMap.get(10)).isEqualTo("value");
            assertThat(subMap.get(3)).isNull();
        }
    }

    @Nested
    @DisplayName("Object 方法测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals - 相等")
        void testEquals() {
            TreeRangeMap<Integer, String> rangeMap1 = TreeRangeMap.create();
            rangeMap1.put(Range.closed(1, 10), "value");

            TreeRangeMap<Integer, String> rangeMap2 = TreeRangeMap.create();
            rangeMap2.put(Range.closed(1, 10), "value");

            assertThat(rangeMap1).isEqualTo(rangeMap2);
        }

        @Test
        @DisplayName("hashCode - 哈希码")
        void testHashCode() {
            TreeRangeMap<Integer, String> rangeMap1 = TreeRangeMap.create();
            rangeMap1.put(Range.closed(1, 10), "value");

            TreeRangeMap<Integer, String> rangeMap2 = TreeRangeMap.create();
            rangeMap2.put(Range.closed(1, 10), "value");

            assertThat(rangeMap1.hashCode()).isEqualTo(rangeMap2.hashCode());
        }

        @Test
        @DisplayName("toString - 字符串表示")
        void testToString() {
            TreeRangeMap<Integer, String> rangeMap = TreeRangeMap.create();
            rangeMap.put(Range.closed(1, 10), "value");

            assertThat(rangeMap.toString()).contains("1").contains("10").contains("value");
        }
    }
}
