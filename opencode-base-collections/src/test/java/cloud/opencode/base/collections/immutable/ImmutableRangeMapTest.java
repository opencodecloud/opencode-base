package cloud.opencode.base.collections.immutable;

import cloud.opencode.base.collections.Range;
import cloud.opencode.base.collections.specialized.RangeMap;
import cloud.opencode.base.collections.specialized.TreeRangeMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;

/**
 * ImmutableRangeMap 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.3
 */
@DisplayName("ImmutableRangeMap 测试")
class ImmutableRangeMapTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of() - 创建空映射")
        void testOfEmpty() {
            ImmutableRangeMap<Integer, String> map = ImmutableRangeMap.of();

            assertThat(map.isEmpty()).isTrue();
            assertThat(map.asMapOfRanges()).isEmpty();
        }

        @Test
        @DisplayName("of(range, value) - 创建单条目映射")
        void testOfSingle() {
            ImmutableRangeMap<Integer, String> map =
                    ImmutableRangeMap.of(Range.closed(1, 10), "small");

            assertThat(map.isEmpty()).isFalse();
            assertThat(map.get(5)).isEqualTo("small");
        }

        @Test
        @DisplayName("of(emptyRange, value) - 空范围返回空映射")
        void testOfEmptyRange() {
            ImmutableRangeMap<Integer, String> map =
                    ImmutableRangeMap.of(Range.closedOpen(5, 5), "value");

            assertThat(map.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("copyOf(RangeMap) - 复制范围映射")
        void testCopyOfRangeMap() {
            TreeRangeMap<Integer, String> tree = TreeRangeMap.create();
            tree.put(Range.closed(1, 10), "small");
            tree.put(Range.closed(11, 100), "medium");

            ImmutableRangeMap<Integer, String> map = ImmutableRangeMap.copyOf(tree);

            assertThat(map.get(5)).isEqualTo("small");
            assertThat(map.get(50)).isEqualTo("medium");
        }

        @Test
        @DisplayName("copyOf(ImmutableRangeMap) - 返回自身")
        void testCopyOfImmutableReturnsSame() {
            ImmutableRangeMap<Integer, String> original =
                    ImmutableRangeMap.of(Range.closed(1, 10), "value");
            ImmutableRangeMap<Integer, String> copy = ImmutableRangeMap.copyOf(original);

            assertThat(copy).isSameAs(original);
        }
    }

    @Nested
    @DisplayName("Builder 测试")
    class BuilderTests {

        @Test
        @DisplayName("builder - 构建多条目映射")
        void testBuilder() {
            ImmutableRangeMap<Integer, String> map = ImmutableRangeMap.<Integer, String>builder()
                    .put(Range.closed(1, 10), "small")
                    .put(Range.closed(11, 100), "medium")
                    .put(Range.closed(101, 1000), "large")
                    .build();

            assertThat(map.get(5)).isEqualTo("small");
            assertThat(map.get(50)).isEqualTo("medium");
            assertThat(map.get(500)).isEqualTo("large");
        }

        @Test
        @DisplayName("builder - putAll 添加所有映射")
        void testBuilderPutAll() {
            TreeRangeMap<Integer, String> tree = TreeRangeMap.create();
            tree.put(Range.closed(1, 10), "a");
            tree.put(Range.closed(20, 30), "b");

            ImmutableRangeMap<Integer, String> map = ImmutableRangeMap.<Integer, String>builder()
                    .putAll(tree)
                    .build();

            assertThat(map.get(5)).isEqualTo("a");
            assertThat(map.get(25)).isEqualTo("b");
        }

        @Test
        @DisplayName("builder - 空构建")
        void testBuilderEmpty() {
            ImmutableRangeMap<Integer, String> map =
                    ImmutableRangeMap.<Integer, String>builder().build();

            assertThat(map.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("builder - 重叠范围后者覆盖")
        void testBuilderOverlapping() {
            ImmutableRangeMap<Integer, String> map = ImmutableRangeMap.<Integer, String>builder()
                    .put(Range.closed(1, 20), "first")
                    .put(Range.closed(5, 15), "second")
                    .build();

            assertThat(map.get(3)).isEqualTo("first");
            assertThat(map.get(10)).isEqualTo("second");
            assertThat(map.get(18)).isEqualTo("first");
        }
    }

    @Nested
    @DisplayName("查询方法测试")
    class QueryMethodTests {

        private final ImmutableRangeMap<Integer, String> map =
                ImmutableRangeMap.<Integer, String>builder()
                        .put(Range.closed(1, 10), "small")
                        .put(Range.closed(20, 30), "medium")
                        .put(Range.closed(40, 50), "large")
                        .build();

        @Test
        @DisplayName("get - 返回正确的值")
        void testGet() {
            assertThat(map.get(5)).isEqualTo("small");
            assertThat(map.get(25)).isEqualTo("medium");
            assertThat(map.get(45)).isEqualTo("large");
        }

        @Test
        @DisplayName("get - 值不在范围内返回 null")
        void testGetOutOfRange() {
            assertThat(map.get(0)).isNull();
            assertThat(map.get(15)).isNull();
            assertThat(map.get(35)).isNull();
            assertThat(map.get(55)).isNull();
        }

        @Test
        @DisplayName("getEntry - 返回范围和值")
        void testGetEntry() {
            Map.Entry<Range<Integer>, String> entry = map.getEntry(5);

            assertThat(entry).isNotNull();
            assertThat(entry.getKey()).isEqualTo(Range.closed(1, 10));
            assertThat(entry.getValue()).isEqualTo("small");
        }

        @Test
        @DisplayName("getEntry - 值不在范围内返回 null")
        void testGetEntryNull() {
            assertThat(map.getEntry(15)).isNull();
        }

        @Test
        @DisplayName("span - 返回包含所有范围的最小范围")
        void testSpan() {
            Range<Integer> span = map.span();

            assertThat(span).isEqualTo(Range.closed(1, 50));
        }

        @Test
        @DisplayName("span - 空映射抛出异常")
        void testSpanEmpty() {
            ImmutableRangeMap<Integer, String> empty = ImmutableRangeMap.of();

            assertThatThrownBy(empty::span)
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("isEmpty - 非空映射")
        void testIsNotEmpty() {
            assertThat(map.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("isEmpty - 空映射")
        void testIsEmpty() {
            assertThat(ImmutableRangeMap.of().isEmpty()).isTrue();
        }

        @Test
        @DisplayName("asMapOfRanges - 返回不可修改映射")
        void testAsMapOfRangesUnmodifiable() {
            Map<Range<Integer>, String> ranges = map.asMapOfRanges();

            assertThat(ranges).hasSize(3);
            assertThatThrownBy(() -> ranges.put(Range.closed(60, 70), "extra"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("asDescendingMapOfRanges - 返回降序映射")
        void testAsDescendingMapOfRanges() {
            Map<Range<Integer>, String> descending = map.asDescendingMapOfRanges();
            var keys = new java.util.ArrayList<>(descending.keySet());

            assertThat(keys).hasSize(3);
            assertThat(keys.get(0)).isEqualTo(Range.closed(40, 50));
            assertThat(keys.get(2)).isEqualTo(Range.closed(1, 10));
        }
    }

    @Nested
    @DisplayName("subRangeMap 测试")
    class SubRangeMapTests {

        @Test
        @DisplayName("subRangeMap - 返回视图范围内的子映射")
        void testSubRangeMap() {
            ImmutableRangeMap<Integer, String> map =
                    ImmutableRangeMap.<Integer, String>builder()
                            .put(Range.closed(1, 10), "small")
                            .put(Range.closed(20, 30), "medium")
                            .put(Range.closed(40, 50), "large")
                            .build();

            RangeMap<Integer, String> sub = map.subRangeMap(Range.closed(5, 25));

            assertThat(sub.get(5)).isEqualTo("small");
            assertThat(sub.get(10)).isEqualTo("small");
            assertThat(sub.get(20)).isEqualTo("medium");
            assertThat(sub.get(25)).isEqualTo("medium");
            assertThat(sub.get(1)).isNull();
            assertThat(sub.get(30)).isNull();
            assertThat(sub.get(45)).isNull();
        }

        @Test
        @DisplayName("subRangeMap - 空视图返回空")
        void testSubRangeMapEmpty() {
            ImmutableRangeMap<Integer, String> map =
                    ImmutableRangeMap.of(Range.closed(1, 10), "value");

            RangeMap<Integer, String> sub = map.subRangeMap(Range.closedOpen(5, 5));

            assertThat(sub.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("subRangeMap - 无交集返回空")
        void testSubRangeMapNoIntersection() {
            ImmutableRangeMap<Integer, String> map =
                    ImmutableRangeMap.of(Range.closed(1, 10), "value");

            RangeMap<Integer, String> sub = map.subRangeMap(Range.closed(20, 30));

            assertThat(sub.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("变更方法测试 - 抛出异常")
    class MutationMethodTests {

        private final ImmutableRangeMap<Integer, String> map =
                ImmutableRangeMap.of(Range.closed(1, 10), "value");

        @Test
        @DisplayName("put - 抛出 UnsupportedOperationException")
        void testPutThrows() {
            assertThatThrownBy(() -> map.put(Range.closed(20, 30), "new"))
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessage("ImmutableRangeMap does not support mutation");
        }

        @Test
        @DisplayName("putCoalescing - 抛出 UnsupportedOperationException")
        void testPutCoalescingThrows() {
            assertThatThrownBy(() -> map.putCoalescing(Range.closed(20, 30), "new"))
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessage("ImmutableRangeMap does not support mutation");
        }

        @Test
        @DisplayName("putAll - 抛出 UnsupportedOperationException")
        void testPutAllThrows() {
            TreeRangeMap<Integer, String> other = TreeRangeMap.create();
            other.put(Range.closed(20, 30), "new");

            assertThatThrownBy(() -> map.putAll(other))
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessage("ImmutableRangeMap does not support mutation");
        }

        @Test
        @DisplayName("remove - 抛出 UnsupportedOperationException")
        void testRemoveThrows() {
            assertThatThrownBy(() -> map.remove(Range.closed(1, 5)))
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessage("ImmutableRangeMap does not support mutation");
        }

        @Test
        @DisplayName("clear - 抛出 UnsupportedOperationException")
        void testClearThrows() {
            assertThatThrownBy(map::clear)
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessage("ImmutableRangeMap does not support mutation");
        }
    }

    @Nested
    @DisplayName("Object 方法测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals - 相同映射相等")
        void testEquals() {
            ImmutableRangeMap<Integer, String> a = ImmutableRangeMap.<Integer, String>builder()
                    .put(Range.closed(1, 10), "small")
                    .put(Range.closed(20, 30), "medium")
                    .build();
            ImmutableRangeMap<Integer, String> b = ImmutableRangeMap.<Integer, String>builder()
                    .put(Range.closed(1, 10), "small")
                    .put(Range.closed(20, 30), "medium")
                    .build();

            assertThat(a).isEqualTo(b);
        }

        @Test
        @DisplayName("equals - 与 TreeRangeMap 相等")
        void testEqualsWithTreeRangeMap() {
            ImmutableRangeMap<Integer, String> immutable =
                    ImmutableRangeMap.of(Range.closed(1, 10), "value");
            TreeRangeMap<Integer, String> tree = TreeRangeMap.create();
            tree.put(Range.closed(1, 10), "value");

            assertThat(immutable).isEqualTo(tree);
        }

        @Test
        @DisplayName("equals - 不同映射不相等")
        void testNotEquals() {
            ImmutableRangeMap<Integer, String> a =
                    ImmutableRangeMap.of(Range.closed(1, 10), "a");
            ImmutableRangeMap<Integer, String> b =
                    ImmutableRangeMap.of(Range.closed(1, 10), "b");

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("hashCode - 相同映射相同哈希")
        void testHashCode() {
            ImmutableRangeMap<Integer, String> a =
                    ImmutableRangeMap.of(Range.closed(1, 10), "value");
            ImmutableRangeMap<Integer, String> b =
                    ImmutableRangeMap.of(Range.closed(1, 10), "value");

            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("toString - 包含范围和值表示")
        void testToString() {
            ImmutableRangeMap<Integer, String> map =
                    ImmutableRangeMap.of(Range.closed(1, 10), "small");

            String str = map.toString();

            assertThat(str).contains("[1..10]");
            assertThat(str).contains("small");
        }
    }
}
