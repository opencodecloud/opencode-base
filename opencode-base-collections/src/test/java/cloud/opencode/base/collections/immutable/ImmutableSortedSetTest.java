package cloud.opencode.base.collections.immutable;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ImmutableSortedSet 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("ImmutableSortedSet 测试")
class ImmutableSortedSetTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of - 创建空 SortedSet")
        void testOfEmpty() {
            ImmutableSortedSet<String> set = ImmutableSortedSet.of();

            assertThat(set).isEmpty();
        }

        @Test
        @DisplayName("of - 创建包含一个元素的 SortedSet")
        void testOfOne() {
            ImmutableSortedSet<String> set = ImmutableSortedSet.of("a");

            assertThat(set).hasSize(1);
            assertThat(set).contains("a");
        }

        @Test
        @DisplayName("of - 创建包含多个元素的 SortedSet (已排序)")
        void testOfMultiple() {
            ImmutableSortedSet<String> set = ImmutableSortedSet.of("c", "a", "b");

            assertThat(set).hasSize(3);
            assertThat(new ArrayList<>(set)).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("copyOf - 从 Collection 复制")
        void testCopyOf() {
            List<String> source = List.of("c", "a", "b");
            ImmutableSortedSet<String> set = ImmutableSortedSet.copyOf(source);

            assertThat(set).hasSize(3);
            assertThat(new ArrayList<>(set)).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("copyOf - 从 Collection 复制并去重")
        void testCopyOfDedupe() {
            List<String> source = List.of("a", "b", "a", "c");
            ImmutableSortedSet<String> set = ImmutableSortedSet.copyOf(source);

            assertThat(set).hasSize(3);
        }

        @Test
        @DisplayName("copyOf - 使用比较器")
        void testCopyOfWithComparator() {
            List<String> source = List.of("a", "b", "c");
            ImmutableSortedSet<String> set = ImmutableSortedSet.copyOf(source, Comparator.reverseOrder());

            assertThat(new ArrayList<>(set)).containsExactly("c", "b", "a");
        }
    }

    @Nested
    @DisplayName("Builder 测试")
    class BuilderTests {

        @Test
        @DisplayName("naturalOrder - 自然顺序构建")
        void testNaturalOrder() {
            ImmutableSortedSet<Integer> set = ImmutableSortedSet.<Integer>naturalOrder()
                    .add(3, 1, 2)
                    .build();

            assertThat(new ArrayList<>(set)).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("orderedBy - 自定义顺序构建")
        void testOrderedBy() {
            ImmutableSortedSet<String> set = ImmutableSortedSet.<String>orderedBy(Comparator.reverseOrder())
                    .add("a", "b", "c")
                    .build();

            assertThat(new ArrayList<>(set)).containsExactly("c", "b", "a");
        }

        @Test
        @DisplayName("builder - 空构建")
        void testBuilderEmpty() {
            ImmutableSortedSet<String> set = ImmutableSortedSet.<String>naturalOrder().build();

            assertThat(set).isEmpty();
        }

        @Test
        @DisplayName("builder - addAll")
        void testBuilderAddAll() {
            ImmutableSortedSet<Integer> set = ImmutableSortedSet.<Integer>naturalOrder()
                    .addAll(List.of(3, 1, 2))
                    .build();

            assertThat(new ArrayList<>(set)).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("builder - null 元素抛异常")
        void testBuilderNullElement() {
            assertThatThrownBy(() -> ImmutableSortedSet.<String>naturalOrder()
                    .add((String) null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Set 方法测试")
    class SetMethodTests {

        @Test
        @DisplayName("size - 大小")
        void testSize() {
            ImmutableSortedSet<String> set = ImmutableSortedSet.of("a", "b", "c");

            assertThat(set.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("isEmpty - 空判断")
        void testIsEmpty() {
            assertThat(ImmutableSortedSet.of().isEmpty()).isTrue();
            assertThat(ImmutableSortedSet.of("a").isEmpty()).isFalse();
        }

        @Test
        @DisplayName("contains - 包含")
        void testContains() {
            ImmutableSortedSet<String> set = ImmutableSortedSet.of("a", "b", "c");

            assertThat(set.contains("a")).isTrue();
            assertThat(set.contains("d")).isFalse();
            assertThat(set.contains(null)).isFalse();
        }

        @Test
        @DisplayName("iterator - 迭代")
        void testIterator() {
            ImmutableSortedSet<String> set = ImmutableSortedSet.of("c", "a", "b");
            List<String> list = new ArrayList<>();

            for (String s : set) {
                list.add(s);
            }

            assertThat(list).containsExactly("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("NavigableSet 方法测试")
    class NavigableSetMethodTests {

        @Test
        @DisplayName("comparator - 获取比较器")
        void testComparator() {
            ImmutableSortedSet<String> natural = ImmutableSortedSet.of("a");
            ImmutableSortedSet<String> custom = ImmutableSortedSet.copyOf(List.of("a"), Comparator.reverseOrder());

            assertThat(natural.comparator()).isNull();
            assertThat(custom.comparator()).isNotNull();
        }

        @Test
        @DisplayName("first - 第一个元素")
        void testFirst() {
            ImmutableSortedSet<String> set = ImmutableSortedSet.of("c", "a", "b");

            assertThat(set.first()).isEqualTo("a");
        }

        @Test
        @DisplayName("first - 空集抛异常")
        void testFirstEmpty() {
            ImmutableSortedSet<String> set = ImmutableSortedSet.of();

            assertThatThrownBy(set::first)
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("last - 最后一个元素")
        void testLast() {
            ImmutableSortedSet<String> set = ImmutableSortedSet.of("c", "a", "b");

            assertThat(set.last()).isEqualTo("c");
        }

        @Test
        @DisplayName("last - 空集抛异常")
        void testLastEmpty() {
            ImmutableSortedSet<String> set = ImmutableSortedSet.of();

            assertThatThrownBy(set::last)
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("lower - 小于给定元素的最大元素")
        void testLower() {
            ImmutableSortedSet<Integer> set = ImmutableSortedSet.of(1, 3, 5);

            assertThat(set.lower(3)).isEqualTo(1);
            assertThat(set.lower(4)).isEqualTo(3);
            assertThat(set.lower(1)).isNull();
        }

        @Test
        @DisplayName("floor - 小于等于给定元素的最大元素")
        void testFloor() {
            ImmutableSortedSet<Integer> set = ImmutableSortedSet.of(1, 3, 5);

            assertThat(set.floor(3)).isEqualTo(3);
            assertThat(set.floor(4)).isEqualTo(3);
            assertThat(set.floor(0)).isNull();
        }

        @Test
        @DisplayName("ceiling - 大于等于给定元素的最小元素")
        void testCeiling() {
            ImmutableSortedSet<Integer> set = ImmutableSortedSet.of(1, 3, 5);

            assertThat(set.ceiling(3)).isEqualTo(3);
            assertThat(set.ceiling(2)).isEqualTo(3);
            assertThat(set.ceiling(6)).isNull();
        }

        @Test
        @DisplayName("higher - 大于给定元素的最小元素")
        void testHigher() {
            ImmutableSortedSet<Integer> set = ImmutableSortedSet.of(1, 3, 5);

            assertThat(set.higher(3)).isEqualTo(5);
            assertThat(set.higher(2)).isEqualTo(3);
            assertThat(set.higher(5)).isNull();
        }

        @Test
        @DisplayName("pollFirst - 抛异常")
        void testPollFirst() {
            ImmutableSortedSet<String> set = ImmutableSortedSet.of("a");

            assertThatThrownBy(set::pollFirst)
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("pollLast - 抛异常")
        void testPollLast() {
            ImmutableSortedSet<String> set = ImmutableSortedSet.of("a");

            assertThatThrownBy(set::pollLast)
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("descendingSet - 降序集")
        void testDescendingSet() {
            ImmutableSortedSet<String> set = ImmutableSortedSet.of("a", "b", "c");

            NavigableSet<String> descending = set.descendingSet();

            assertThat(descending.first()).isEqualTo("c");
            assertThat(descending.last()).isEqualTo("a");
        }

        @Test
        @DisplayName("descendingIterator - 降序迭代器")
        void testDescendingIterator() {
            ImmutableSortedSet<String> set = ImmutableSortedSet.of("a", "b", "c");
            List<String> list = new ArrayList<>();

            set.descendingIterator().forEachRemaining(list::add);

            assertThat(list).containsExactly("c", "b", "a");
        }

        @Test
        @DisplayName("subSet - 子集")
        void testSubSet() {
            ImmutableSortedSet<Integer> set = ImmutableSortedSet.of(1, 2, 3, 4, 5);

            NavigableSet<Integer> subSet = set.subSet(2, true, 4, true);

            assertThat(subSet).containsExactly(2, 3, 4);
        }

        @Test
        @DisplayName("headSet - 头集")
        void testHeadSet() {
            ImmutableSortedSet<Integer> set = ImmutableSortedSet.of(1, 2, 3, 4, 5);

            SortedSet<Integer> headSet = set.headSet(3);

            assertThat(headSet).containsExactly(1, 2);
        }

        @Test
        @DisplayName("tailSet - 尾集")
        void testTailSet() {
            ImmutableSortedSet<Integer> set = ImmutableSortedSet.of(1, 2, 3, 4, 5);

            SortedSet<Integer> tailSet = set.tailSet(3);

            assertThat(tailSet).containsExactly(3, 4, 5);
        }
    }
}
