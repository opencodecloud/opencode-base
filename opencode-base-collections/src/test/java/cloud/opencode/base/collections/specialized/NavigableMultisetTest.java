package cloud.opencode.base.collections.specialized;

import cloud.opencode.base.collections.Multiset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;

/**
 * NavigableMultiset 接口测试
 * 通过 TreeMultiset 实现类测试接口的所有方法
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("NavigableMultiset 接口测试")
class NavigableMultisetTest {

    @Nested
    @DisplayName("first/last 方法测试")
    class FirstLastMethodTests {

        @Test
        @DisplayName("first - 获取最小元素")
        void testFirst() {
            NavigableMultiset<Integer> multiset = TreeMultiset.create();
            multiset.add(5);
            multiset.add(3);
            multiset.add(7);

            assertThat(multiset.first()).isEqualTo(3);
        }

        @Test
        @DisplayName("first - 空多重集合抛异常")
        void testFirstEmpty() {
            NavigableMultiset<Integer> multiset = TreeMultiset.create();

            assertThatThrownBy(multiset::first)
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("first - 多个相同最小元素")
        void testFirstWithDuplicates() {
            NavigableMultiset<Integer> multiset = TreeMultiset.create();
            multiset.add(3, 5);
            multiset.add(5);
            multiset.add(7);

            assertThat(multiset.first()).isEqualTo(3);
        }

        @Test
        @DisplayName("last - 获取最大元素")
        void testLast() {
            NavigableMultiset<Integer> multiset = TreeMultiset.create();
            multiset.add(5);
            multiset.add(3);
            multiset.add(7);

            assertThat(multiset.last()).isEqualTo(7);
        }

        @Test
        @DisplayName("last - 空多重集合抛异常")
        void testLastEmpty() {
            NavigableMultiset<Integer> multiset = TreeMultiset.create();

            assertThatThrownBy(multiset::last)
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("last - 多个相同最大元素")
        void testLastWithDuplicates() {
            NavigableMultiset<Integer> multiset = TreeMultiset.create();
            multiset.add(3);
            multiset.add(5);
            multiset.add(7, 5);

            assertThat(multiset.last()).isEqualTo(7);
        }
    }

    @Nested
    @DisplayName("lower/higher 方法测试")
    class LowerHigherMethodTests {

        @Test
        @DisplayName("lower - 获取严格小于的最大元素")
        void testLower() {
            NavigableMultiset<Integer> multiset = TreeMultiset.create();
            multiset.add(1);
            multiset.add(3);
            multiset.add(5);
            multiset.add(7);

            assertThat(multiset.lower(5)).isEqualTo(3);
        }

        @Test
        @DisplayName("lower - 无更小元素返回 null")
        void testLowerNoSmaller() {
            NavigableMultiset<Integer> multiset = TreeMultiset.create();
            multiset.add(5);
            multiset.add(7);

            assertThat(multiset.lower(5)).isNull();
        }

        @Test
        @DisplayName("lower - 最小元素")
        void testLowerAtMinimum() {
            NavigableMultiset<Integer> multiset = TreeMultiset.create();
            multiset.add(1);
            multiset.add(3);

            assertThat(multiset.lower(1)).isNull();
        }

        @Test
        @DisplayName("higher - 获取严格大于的最小元素")
        void testHigher() {
            NavigableMultiset<Integer> multiset = TreeMultiset.create();
            multiset.add(1);
            multiset.add(3);
            multiset.add(5);
            multiset.add(7);

            assertThat(multiset.higher(5)).isEqualTo(7);
        }

        @Test
        @DisplayName("higher - 无更大元素返回 null")
        void testHigherNoLarger() {
            NavigableMultiset<Integer> multiset = TreeMultiset.create();
            multiset.add(3);
            multiset.add(5);

            assertThat(multiset.higher(5)).isNull();
        }

        @Test
        @DisplayName("higher - 最大元素")
        void testHigherAtMaximum() {
            NavigableMultiset<Integer> multiset = TreeMultiset.create();
            multiset.add(5);
            multiset.add(7);

            assertThat(multiset.higher(7)).isNull();
        }
    }

    @Nested
    @DisplayName("floor/ceiling 方法测试")
    class FloorCeilingMethodTests {

        @Test
        @DisplayName("floor - 获取小于等于的最大元素")
        void testFloor() {
            NavigableMultiset<Integer> multiset = TreeMultiset.create();
            multiset.add(1);
            multiset.add(3);
            multiset.add(5);

            assertThat(multiset.floor(4)).isEqualTo(3);
            assertThat(multiset.floor(5)).isEqualTo(5);
        }

        @Test
        @DisplayName("floor - 无匹配返回 null")
        void testFloorNoMatch() {
            NavigableMultiset<Integer> multiset = TreeMultiset.create();
            multiset.add(5);
            multiset.add(7);

            assertThat(multiset.floor(3)).isNull();
        }

        @Test
        @DisplayName("ceiling - 获取大于等于的最小元素")
        void testCeiling() {
            NavigableMultiset<Integer> multiset = TreeMultiset.create();
            multiset.add(1);
            multiset.add(3);
            multiset.add(5);

            assertThat(multiset.ceiling(2)).isEqualTo(3);
            assertThat(multiset.ceiling(3)).isEqualTo(3);
        }

        @Test
        @DisplayName("ceiling - 无匹配返回 null")
        void testCeilingNoMatch() {
            NavigableMultiset<Integer> multiset = TreeMultiset.create();
            multiset.add(1);
            multiset.add(3);

            assertThat(multiset.ceiling(5)).isNull();
        }
    }

    @Nested
    @DisplayName("pollFirstEntry/pollLastEntry 方法测试")
    class PollMethodTests {

        @Test
        @DisplayName("pollFirstEntry - 移除并返回最小元素条目")
        void testPollFirstEntry() {
            NavigableMultiset<Integer> multiset = TreeMultiset.create();
            multiset.add(3, 2);
            multiset.add(5);
            multiset.add(7);

            Multiset.Entry<Integer> entry = multiset.pollFirstEntry();

            assertThat(entry.getElement()).isEqualTo(3);
            assertThat(entry.getCount()).isEqualTo(2);
            assertThat(multiset.contains(3)).isFalse();
        }

        @Test
        @DisplayName("pollFirstEntry - 空多重集合返回 null")
        void testPollFirstEntryEmpty() {
            NavigableMultiset<Integer> multiset = TreeMultiset.create();

            Multiset.Entry<Integer> entry = multiset.pollFirstEntry();

            assertThat(entry).isNull();
        }

        @Test
        @DisplayName("pollLastEntry - 移除并返回最大元素条目")
        void testPollLastEntry() {
            NavigableMultiset<Integer> multiset = TreeMultiset.create();
            multiset.add(3);
            multiset.add(5);
            multiset.add(7, 3);

            Multiset.Entry<Integer> entry = multiset.pollLastEntry();

            assertThat(entry.getElement()).isEqualTo(7);
            assertThat(entry.getCount()).isEqualTo(3);
            assertThat(multiset.contains(7)).isFalse();
        }

        @Test
        @DisplayName("pollLastEntry - 空多重集合返回 null")
        void testPollLastEntryEmpty() {
            NavigableMultiset<Integer> multiset = TreeMultiset.create();

            Multiset.Entry<Integer> entry = multiset.pollLastEntry();

            assertThat(entry).isNull();
        }
    }

    @Nested
    @DisplayName("comparator 方法测试")
    class ComparatorMethodTests {

        @Test
        @DisplayName("comparator - 自然排序返回 null")
        void testComparatorNatural() {
            NavigableMultiset<Integer> multiset = TreeMultiset.create();

            assertThat(multiset.comparator()).isNull();
        }

        @Test
        @DisplayName("comparator - 自定义比较器")
        void testComparatorCustom() {
            Comparator<Integer> reverseOrder = Comparator.reverseOrder();
            NavigableMultiset<Integer> multiset = TreeMultiset.create(reverseOrder);

            assertThat(multiset.comparator()).isEqualTo(reverseOrder);
        }

        @Test
        @DisplayName("使用逆序比较器")
        void testReverseOrderComparator() {
            NavigableMultiset<Integer> multiset = TreeMultiset.create(Comparator.reverseOrder());
            multiset.add(1);
            multiset.add(3);
            multiset.add(5);

            assertThat(multiset.first()).isEqualTo(5);
            assertThat(multiset.last()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Multiset 继承方法测试")
    class MultisetMethodTests {

        @Test
        @DisplayName("继承 Multiset 接口")
        void testExtendsMultiset() {
            NavigableMultiset<Integer> multiset = TreeMultiset.create();

            assertThat(multiset).isInstanceOf(Multiset.class);
        }

        @Test
        @DisplayName("add/count - 添加和计数")
        void testAddAndCount() {
            NavigableMultiset<Integer> multiset = TreeMultiset.create();
            multiset.add(5, 3);

            assertThat(multiset.count(5)).isEqualTo(3);
        }

        @Test
        @DisplayName("remove - 移除")
        void testRemove() {
            NavigableMultiset<Integer> multiset = TreeMultiset.create();
            multiset.add(5, 3);

            int oldCount = multiset.remove(5, 2);

            assertThat(oldCount).isEqualTo(3);
            assertThat(multiset.count(5)).isEqualTo(1);
        }

        @Test
        @DisplayName("size - 大小")
        void testSize() {
            NavigableMultiset<Integer> multiset = TreeMultiset.create();
            multiset.add(3, 2);
            multiset.add(5, 3);

            assertThat(multiset.size()).isEqualTo(5);
        }

        @Test
        @DisplayName("elementSet - 元素集合有序")
        void testElementSetSorted() {
            NavigableMultiset<Integer> multiset = TreeMultiset.create();
            multiset.add(5);
            multiset.add(1);
            multiset.add(3);

            assertThat(multiset.elementSet()).containsExactly(1, 3, 5);
        }
    }

    @Nested
    @DisplayName("字符串类型测试")
    class StringTypeTests {

        @Test
        @DisplayName("字符串自然排序")
        void testStringNaturalOrder() {
            NavigableMultiset<String> multiset = TreeMultiset.create();
            multiset.add("banana");
            multiset.add("apple");
            multiset.add("cherry");

            assertThat(multiset.first()).isEqualTo("apple");
            assertThat(multiset.last()).isEqualTo("cherry");
        }

        @Test
        @DisplayName("字符串导航操作")
        void testStringNavigation() {
            NavigableMultiset<String> multiset = TreeMultiset.create();
            multiset.add("a");
            multiset.add("c");
            multiset.add("e");

            assertThat(multiset.lower("c")).isEqualTo("a");
            assertThat(multiset.higher("c")).isEqualTo("e");
            assertThat(multiset.floor("b")).isEqualTo("a");
            assertThat(multiset.ceiling("d")).isEqualTo("e");
        }
    }

    @Nested
    @DisplayName("边界情况测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("单元素多重集合")
        void testSingleElement() {
            NavigableMultiset<Integer> multiset = TreeMultiset.create();
            multiset.add(5);

            assertThat(multiset.first()).isEqualTo(5);
            assertThat(multiset.last()).isEqualTo(5);
            assertThat(multiset.lower(5)).isNull();
            assertThat(multiset.higher(5)).isNull();
            assertThat(multiset.floor(5)).isEqualTo(5);
            assertThat(multiset.ceiling(5)).isEqualTo(5);
        }

        @Test
        @DisplayName("所有元素相同")
        void testAllSameElements() {
            NavigableMultiset<Integer> multiset = TreeMultiset.create();
            multiset.add(5, 100);

            assertThat(multiset.first()).isEqualTo(5);
            assertThat(multiset.last()).isEqualTo(5);
            assertThat(multiset.size()).isEqualTo(100);
        }
    }
}
