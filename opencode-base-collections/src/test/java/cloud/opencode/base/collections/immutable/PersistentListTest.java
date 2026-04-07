package cloud.opencode.base.collections.immutable;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

/**
 * PersistentList 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.3
 */
@DisplayName("PersistentList 测试")
class PersistentListTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("empty - 创建空列表")
        void testEmpty() {
            PersistentList<String> list = PersistentList.empty();

            assertThat(list.isEmpty()).isTrue();
            assertThat(list.size()).isZero();
        }

        @Test
        @DisplayName("of - 创建包含元素的列表")
        void testOf() {
            PersistentList<String> list = PersistentList.of("a", "b", "c");

            assertThat(list.size()).isEqualTo(3);
            assertThat(list.toList()).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("of - 无参调用创建空列表")
        void testOfEmpty() {
            PersistentList<String> list = PersistentList.of();

            assertThat(list.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("of - 单个元素")
        void testOfSingle() {
            PersistentList<Integer> list = PersistentList.of(42);

            assertThat(list.size()).isEqualTo(1);
            assertThat(list.head()).isEqualTo(42);
        }

        @Test
        @DisplayName("from - 从 Iterable 创建")
        void testFrom() {
            List<String> source = List.of("x", "y", "z");
            PersistentList<String> list = PersistentList.from(source);

            assertThat(list.size()).isEqualTo(3);
            assertThat(list.toList()).containsExactly("x", "y", "z");
        }

        @Test
        @DisplayName("from - 从空 Iterable 创建")
        void testFromEmpty() {
            PersistentList<String> list = PersistentList.from(List.of());

            assertThat(list.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("prepend 测试")
    class PrependTests {

        @Test
        @DisplayName("prepend - 在空列表前插")
        void testPrependToEmpty() {
            PersistentList<String> list = PersistentList.<String>empty().prepend("a");

            assertThat(list.size()).isEqualTo(1);
            assertThat(list.head()).isEqualTo("a");
        }

        @Test
        @DisplayName("prepend - 在非空列表前插")
        void testPrependToNonEmpty() {
            PersistentList<String> list = PersistentList.of("b", "c");
            PersistentList<String> list2 = list.prepend("a");

            assertThat(list2.size()).isEqualTo(3);
            assertThat(list2.toList()).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("prepend - 原列表不变（结构共享验证）")
        void testPrependStructuralSharing() {
            PersistentList<String> original = PersistentList.of("a", "b", "c");
            PersistentList<String> prepended = original.prepend("z");

            // Original is unchanged
            assertThat(original.size()).isEqualTo(3);
            assertThat(original.toList()).containsExactly("a", "b", "c");

            // Prepended has new element
            assertThat(prepended.size()).isEqualTo(4);
            assertThat(prepended.toList()).containsExactly("z", "a", "b", "c");

            // Structural sharing: prepended.tail() should be the same object as original
            assertThat(prepended.tail()).isSameAs(original);
        }

        @Test
        @DisplayName("prepend - null 元素")
        void testPrependNull() {
            PersistentList<String> list = PersistentList.<String>empty().prepend(null);

            assertThat(list.size()).isEqualTo(1);
            assertThat(list.head()).isNull();
        }
    }

    @Nested
    @DisplayName("append 测试")
    class AppendTests {

        @Test
        @DisplayName("append - 在空列表末尾追加")
        void testAppendToEmpty() {
            PersistentList<String> list = PersistentList.<String>empty().append("a");

            assertThat(list.size()).isEqualTo(1);
            assertThat(list.head()).isEqualTo("a");
        }

        @Test
        @DisplayName("append - 在非空列表末尾追加")
        void testAppendToNonEmpty() {
            PersistentList<String> list = PersistentList.of("a", "b");
            PersistentList<String> appended = list.append("c");

            assertThat(appended.size()).isEqualTo(3);
            assertThat(appended.toList()).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("append - 原列表不变")
        void testAppendOriginalUnchanged() {
            PersistentList<String> original = PersistentList.of("a", "b");
            original.append("c");

            assertThat(original.size()).isEqualTo(2);
            assertThat(original.toList()).containsExactly("a", "b");
        }
    }

    @Nested
    @DisplayName("head / tail 测试")
    class HeadTailTests {

        @Test
        @DisplayName("head - 返回第一个元素")
        void testHead() {
            PersistentList<String> list = PersistentList.of("a", "b", "c");

            assertThat(list.head()).isEqualTo("a");
        }

        @Test
        @DisplayName("tail - 返回尾部列表")
        void testTail() {
            PersistentList<String> list = PersistentList.of("a", "b", "c");
            PersistentList<String> tail = list.tail();

            assertThat(tail.size()).isEqualTo(2);
            assertThat(tail.toList()).containsExactly("b", "c");
        }

        @Test
        @DisplayName("tail - 单元素列表的尾部为空")
        void testTailOfSingle() {
            PersistentList<String> list = PersistentList.of("a");

            assertThat(list.tail().isEmpty()).isTrue();
        }

        @Test
        @DisplayName("head - 空列表抛出 NoSuchElementException")
        void testHeadOnEmpty() {
            PersistentList<String> list = PersistentList.empty();

            assertThatThrownBy(list::head)
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("tail - 空列表抛出 NoSuchElementException")
        void testTailOnEmpty() {
            PersistentList<String> list = PersistentList.empty();

            assertThatThrownBy(list::tail)
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("tail - 原列表不变")
        void testTailOriginalUnchanged() {
            PersistentList<String> original = PersistentList.of("a", "b", "c");
            original.tail();

            assertThat(original.size()).isEqualTo(3);
            assertThat(original.toList()).containsExactly("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("size / isEmpty / contains 测试")
    class BasicQueryTests {

        @Test
        @DisplayName("size - 正确计算大小")
        void testSize() {
            assertThat(PersistentList.empty().size()).isZero();
            assertThat(PersistentList.of(1).size()).isEqualTo(1);
            assertThat(PersistentList.of(1, 2, 3).size()).isEqualTo(3);
        }

        @Test
        @DisplayName("isEmpty - 空列表返回 true")
        void testIsEmpty() {
            assertThat(PersistentList.empty().isEmpty()).isTrue();
            assertThat(PersistentList.of(1).isEmpty()).isFalse();
        }

        @Test
        @DisplayName("contains - 包含存在的元素")
        void testContainsExisting() {
            PersistentList<String> list = PersistentList.of("a", "b", "c");

            assertThat(list.contains("a")).isTrue();
            assertThat(list.contains("b")).isTrue();
            assertThat(list.contains("c")).isTrue();
        }

        @Test
        @DisplayName("contains - 不包含不存在的元素")
        void testContainsNonExisting() {
            PersistentList<String> list = PersistentList.of("a", "b");

            assertThat(list.contains("z")).isFalse();
        }

        @Test
        @DisplayName("contains - 空列表不包含任何元素")
        void testContainsOnEmpty() {
            assertThat(PersistentList.empty().contains("x")).isFalse();
        }

        @Test
        @DisplayName("contains - null 元素查找")
        void testContainsNull() {
            PersistentList<String> list = PersistentList.of("a", null, "b");

            assertThat(list.contains(null)).isTrue();
        }
    }

    @Nested
    @DisplayName("reversed 测试")
    class ReversedTests {

        @Test
        @DisplayName("reversed - 反转列表")
        void testReversed() {
            PersistentList<String> list = PersistentList.of("a", "b", "c");
            PersistentList<String> reversed = list.reversed();

            assertThat(reversed.toList()).containsExactly("c", "b", "a");
        }

        @Test
        @DisplayName("reversed - 空列表反转")
        void testReversedEmpty() {
            PersistentList<String> list = PersistentList.empty();
            PersistentList<String> reversed = list.reversed();

            assertThat(reversed.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("reversed - 单元素列表反转")
        void testReversedSingle() {
            PersistentList<String> list = PersistentList.of("a");
            PersistentList<String> reversed = list.reversed();

            assertThat(reversed.toList()).containsExactly("a");
        }

        @Test
        @DisplayName("reversed - 原列表不变")
        void testReversedOriginalUnchanged() {
            PersistentList<String> original = PersistentList.of("a", "b", "c");
            original.reversed();

            assertThat(original.toList()).containsExactly("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("map 测试")
    class MapTests {

        @Test
        @DisplayName("map - 映射元素")
        void testMap() {
            PersistentList<String> list = PersistentList.of("a", "bb", "ccc");
            PersistentList<Integer> lengths = list.map(String::length);

            assertThat(lengths.toList()).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("map - 空列表映射")
        void testMapEmpty() {
            PersistentList<String> list = PersistentList.empty();
            PersistentList<Integer> mapped = list.map(String::length);

            assertThat(mapped.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("map - null 函数抛出异常")
        void testMapNullFunction() {
            PersistentList<String> list = PersistentList.of("a");

            assertThatThrownBy(() -> list.map(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("filter 测试")
    class FilterTests {

        @Test
        @DisplayName("filter - 过滤元素")
        void testFilter() {
            PersistentList<Integer> list = PersistentList.of(1, 2, 3, 4, 5);
            PersistentList<Integer> even = list.filter(n -> n % 2 == 0);

            assertThat(even.toList()).containsExactly(2, 4);
        }

        @Test
        @DisplayName("filter - 全部匹配")
        void testFilterAllMatch() {
            PersistentList<Integer> list = PersistentList.of(2, 4, 6);
            PersistentList<Integer> even = list.filter(n -> n % 2 == 0);

            assertThat(even.toList()).containsExactly(2, 4, 6);
        }

        @Test
        @DisplayName("filter - 无匹配")
        void testFilterNoneMatch() {
            PersistentList<Integer> list = PersistentList.of(1, 3, 5);
            PersistentList<Integer> even = list.filter(n -> n % 2 == 0);

            assertThat(even.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("filter - 空列表过滤")
        void testFilterEmpty() {
            PersistentList<Integer> list = PersistentList.empty();
            PersistentList<Integer> filtered = list.filter(n -> n > 0);

            assertThat(filtered.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("filter - null 谓词抛出异常")
        void testFilterNullPredicate() {
            PersistentList<Integer> list = PersistentList.of(1);

            assertThatThrownBy(() -> list.filter(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("转换操作测试")
    class ConversionTests {

        @Test
        @DisplayName("toList - 转为 JDK List")
        void testToList() {
            PersistentList<String> list = PersistentList.of("a", "b", "c");
            List<String> jdkList = list.toList();

            assertThat(jdkList).containsExactly("a", "b", "c");
            assertThatThrownBy(() -> jdkList.add("d"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("stream - 流操作")
        void testStream() {
            PersistentList<Integer> list = PersistentList.of(1, 2, 3, 4, 5);
            int sum = list.stream().mapToInt(Integer::intValue).sum();

            assertThat(sum).isEqualTo(15);
        }

        @Test
        @DisplayName("stream - 空列表流")
        void testStreamEmpty() {
            PersistentList<String> list = PersistentList.empty();
            long count = list.stream().count();

            assertThat(count).isZero();
        }

        @Test
        @DisplayName("iterator - 迭代器遍历")
        void testIterator() {
            PersistentList<String> list = PersistentList.of("a", "b", "c");
            List<String> collected = new ArrayList<>();
            Iterator<String> it = list.iterator();
            while (it.hasNext()) {
                collected.add(it.next());
            }

            assertThat(collected).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("iterator - 空列表迭代器")
        void testIteratorEmpty() {
            PersistentList<String> list = PersistentList.empty();
            Iterator<String> it = list.iterator();

            assertThat(it.hasNext()).isFalse();
            assertThatThrownBy(it::next)
                    .isInstanceOf(NoSuchElementException.class);
        }
    }

    @Nested
    @DisplayName("toString 测试")
    class ToStringTests {

        @Test
        @DisplayName("toString - 空列表")
        void testToStringEmpty() {
            assertThat(PersistentList.empty().toString()).isEqualTo("PersistentList[]");
        }

        @Test
        @DisplayName("toString - 非空列表")
        void testToStringNonEmpty() {
            PersistentList<String> list = PersistentList.of("a", "b", "c");

            assertThat(list.toString()).isEqualTo("PersistentList[a, b, c]");
        }
    }

    @Nested
    @DisplayName("结构共享验证")
    class StructuralSharingTests {

        @Test
        @DisplayName("多次 prepend 后原列表都不变")
        void testMultiplePrepends() {
            PersistentList<Integer> list0 = PersistentList.of(3, 4, 5);
            PersistentList<Integer> list1 = list0.prepend(2);
            PersistentList<Integer> list2 = list0.prepend(1);

            assertThat(list0.toList()).containsExactly(3, 4, 5);
            assertThat(list1.toList()).containsExactly(2, 3, 4, 5);
            assertThat(list2.toList()).containsExactly(1, 3, 4, 5);

            // Both list1 and list2 share list0 as their tail
            assertThat(list1.tail()).isSameAs(list0);
            assertThat(list2.tail()).isSameAs(list0);
        }
    }
}
