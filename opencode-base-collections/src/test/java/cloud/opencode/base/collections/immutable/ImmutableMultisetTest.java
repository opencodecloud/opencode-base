package cloud.opencode.base.collections.immutable;

import cloud.opencode.base.collections.exception.OpenCollectionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ImmutableMultiset 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("ImmutableMultiset 测试")
class ImmutableMultisetTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of - 创建空 Multiset")
        void testOfEmpty() {
            ImmutableMultiset<String> multiset = ImmutableMultiset.of();

            assertThat(multiset).isEmpty();
            assertThat(multiset.size()).isZero();
        }

        @Test
        @DisplayName("of - 创建包含一个元素的 Multiset")
        void testOfOne() {
            ImmutableMultiset<String> multiset = ImmutableMultiset.of("a");

            assertThat(multiset.size()).isEqualTo(1);
            assertThat(multiset.count("a")).isEqualTo(1);
        }

        @Test
        @DisplayName("of - 创建包含两个相同元素的 Multiset")
        void testOfTwoSame() {
            ImmutableMultiset<String> multiset = ImmutableMultiset.of("a", "a");

            assertThat(multiset.size()).isEqualTo(2);
            assertThat(multiset.count("a")).isEqualTo(2);
        }

        @Test
        @DisplayName("of - 创建包含两个不同元素的 Multiset")
        void testOfTwoDifferent() {
            ImmutableMultiset<String> multiset = ImmutableMultiset.of("a", "b");

            assertThat(multiset.size()).isEqualTo(2);
            assertThat(multiset.count("a")).isEqualTo(1);
            assertThat(multiset.count("b")).isEqualTo(1);
        }

        @Test
        @DisplayName("of - 创建包含多个元素的 Multiset")
        void testOfVarargs() {
            ImmutableMultiset<String> multiset = ImmutableMultiset.of("a", "b", "a", "c", "b", "a");

            assertThat(multiset.size()).isEqualTo(6);
            assertThat(multiset.count("a")).isEqualTo(3);
            assertThat(multiset.count("b")).isEqualTo(2);
            assertThat(multiset.count("c")).isEqualTo(1);
        }

        @Test
        @DisplayName("copyOf - 从 Collection 复制")
        void testCopyOfCollection() {
            List<String> source = List.of("a", "b", "a", "c");
            ImmutableMultiset<String> multiset = ImmutableMultiset.copyOf(source);

            assertThat(multiset.size()).isEqualTo(4);
            assertThat(multiset.count("a")).isEqualTo(2);
            assertThat(multiset.count("b")).isEqualTo(1);
            assertThat(multiset.count("c")).isEqualTo(1);
        }

        @Test
        @DisplayName("copyOf - null Collection")
        void testCopyOfNull() {
            ImmutableMultiset<String> multiset = ImmutableMultiset.copyOf((Collection<String>) null);

            assertThat(multiset).isEmpty();
        }

        @Test
        @DisplayName("copyOf - 空 Collection")
        void testCopyOfEmpty() {
            ImmutableMultiset<String> multiset = ImmutableMultiset.copyOf(List.of());

            assertThat(multiset).isEmpty();
        }

        @Test
        @DisplayName("copyOf - 返回相同实例")
        void testCopyOfSameInstance() {
            ImmutableMultiset<String> original = ImmutableMultiset.of("a", "b");
            ImmutableMultiset<String> copy = ImmutableMultiset.copyOf(original);

            assertThat(copy).isSameAs(original);
        }

        @Test
        @DisplayName("copyOf - 从 Iterable 复制")
        void testCopyOfIterable() {
            Iterable<String> iterable = () -> List.of("a", "b", "a").iterator();
            ImmutableMultiset<String> multiset = ImmutableMultiset.copyOf(iterable);

            assertThat(multiset.count("a")).isEqualTo(2);
            assertThat(multiset.count("b")).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Builder 测试")
    class BuilderTests {

        @Test
        @DisplayName("builder - 构建 Multiset")
        void testBuilder() {
            ImmutableMultiset<String> multiset = ImmutableMultiset.<String>builder()
                    .add("a")
                    .add("b")
                    .add("a")
                    .build();

            assertThat(multiset.count("a")).isEqualTo(2);
            assertThat(multiset.count("b")).isEqualTo(1);
        }

        @Test
        @DisplayName("builder - 空构建")
        void testBuilderEmpty() {
            ImmutableMultiset<String> multiset = ImmutableMultiset.<String>builder().build();

            assertThat(multiset).isEmpty();
        }

        @Test
        @DisplayName("builder - add 带计数")
        void testBuilderAddWithCount() {
            ImmutableMultiset<String> multiset = ImmutableMultiset.<String>builder()
                    .add("a", 3)
                    .add("b", 2)
                    .build();

            assertThat(multiset.count("a")).isEqualTo(3);
            assertThat(multiset.count("b")).isEqualTo(2);
            assertThat(multiset.size()).isEqualTo(5);
        }

        @Test
        @DisplayName("builder - add 负数计数抛异常")
        void testBuilderAddNegativeCount() {
            assertThatThrownBy(() -> ImmutableMultiset.<String>builder()
                    .add("a", -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("builder - add 零计数")
        void testBuilderAddZeroCount() {
            ImmutableMultiset<String> multiset = ImmutableMultiset.<String>builder()
                    .add("a", 0)
                    .build();

            assertThat(multiset).isEmpty();
        }

        @Test
        @DisplayName("builder - add varargs")
        void testBuilderAddVarargs() {
            ImmutableMultiset<String> multiset = ImmutableMultiset.<String>builder()
                    .add("a", "b", "c")
                    .build();

            assertThat(multiset.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("builder - addAll")
        void testBuilderAddAll() {
            ImmutableMultiset<String> multiset = ImmutableMultiset.<String>builder()
                    .addAll(List.of("a", "b", "a"))
                    .build();

            assertThat(multiset.count("a")).isEqualTo(2);
            assertThat(multiset.count("b")).isEqualTo(1);
        }

        @Test
        @DisplayName("builder - setCount")
        void testBuilderSetCount() {
            ImmutableMultiset<String> multiset = ImmutableMultiset.<String>builder()
                    .add("a")
                    .setCount("a", 5)
                    .build();

            assertThat(multiset.count("a")).isEqualTo(5);
        }

        @Test
        @DisplayName("builder - setCount 为零移除元素")
        void testBuilderSetCountZero() {
            ImmutableMultiset<String> multiset = ImmutableMultiset.<String>builder()
                    .add("a")
                    .setCount("a", 0)
                    .build();

            assertThat(multiset).isEmpty();
        }

        @Test
        @DisplayName("builder - setCount 负数抛异常")
        void testBuilderSetCountNegative() {
            assertThatThrownBy(() -> ImmutableMultiset.<String>builder()
                    .setCount("a", -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("builder - null 元素抛异常")
        void testBuilderNullElement() {
            assertThatThrownBy(() -> ImmutableMultiset.<String>builder()
                    .add((String) null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Multiset 特有方法测试")
    class MultisetMethodTests {

        @Test
        @DisplayName("count - 计数")
        void testCount() {
            ImmutableMultiset<String> multiset = ImmutableMultiset.of("a", "b", "a", "a");

            assertThat(multiset.count("a")).isEqualTo(3);
            assertThat(multiset.count("b")).isEqualTo(1);
            assertThat(multiset.count("c")).isZero();
        }

        @Test
        @DisplayName("count - null 元素")
        void testCountNull() {
            ImmutableMultiset<String> multiset = ImmutableMultiset.of("a");

            assertThat(multiset.count(null)).isZero();
        }

        @Test
        @DisplayName("elementSet - 元素集")
        void testElementSet() {
            ImmutableMultiset<String> multiset = ImmutableMultiset.of("a", "b", "a", "c");

            assertThat(multiset.elementSet()).containsExactlyInAnyOrder("a", "b", "c");
        }

        @Test
        @DisplayName("entrySet - 条目集")
        void testEntrySet() {
            ImmutableMultiset<String> multiset = ImmutableMultiset.of("a", "b", "a");

            Set<ImmutableMultiset.Entry<String>> entries = multiset.entrySet();

            assertThat(entries).hasSize(2);

            for (ImmutableMultiset.Entry<String> entry : entries) {
                if (entry.getElement().equals("a")) {
                    assertThat(entry.getCount()).isEqualTo(2);
                } else if (entry.getElement().equals("b")) {
                    assertThat(entry.getCount()).isEqualTo(1);
                }
            }
        }
    }

    @Nested
    @DisplayName("Collection 实现测试")
    class CollectionImplementationTests {

        @Test
        @DisplayName("size - 总大小")
        void testSize() {
            ImmutableMultiset<String> multiset = ImmutableMultiset.of("a", "b", "a", "c", "a");

            assertThat(multiset.size()).isEqualTo(5);
        }

        @Test
        @DisplayName("isEmpty - 空判断")
        void testIsEmpty() {
            assertThat(ImmutableMultiset.of().isEmpty()).isTrue();
            assertThat(ImmutableMultiset.of("a").isEmpty()).isFalse();
        }

        @Test
        @DisplayName("contains - 包含")
        void testContains() {
            ImmutableMultiset<String> multiset = ImmutableMultiset.of("a", "b");

            assertThat(multiset.contains("a")).isTrue();
            assertThat(multiset.contains("c")).isFalse();
            assertThat(multiset.contains(null)).isFalse();
        }

        @Test
        @DisplayName("iterator - 迭代")
        void testIterator() {
            ImmutableMultiset<String> multiset = ImmutableMultiset.of("a", "b", "a");
            List<String> list = new ArrayList<>();

            for (String element : multiset) {
                list.add(element);
            }

            assertThat(list).hasSize(3);
            assertThat(list).containsExactlyInAnyOrder("a", "a", "b");
        }

        @Test
        @DisplayName("toArray - 转数组")
        void testToArray() {
            ImmutableMultiset<String> multiset = ImmutableMultiset.of("a", "b", "a");

            Object[] array = multiset.toArray();

            assertThat(array).hasSize(3);
        }

        @Test
        @DisplayName("toArray(T[]) - 转类型数组")
        void testToArrayTyped() {
            ImmutableMultiset<String> multiset = ImmutableMultiset.of("a", "b", "a");

            String[] array = multiset.toArray(new String[0]);

            assertThat(array).hasSize(3);
        }
    }

    @Nested
    @DisplayName("不可变保护测试")
    class ImmutabilityTests {

        @Test
        @DisplayName("add - 抛异常")
        void testAdd() {
            ImmutableMultiset<String> multiset = ImmutableMultiset.of("a");

            assertThatThrownBy(() -> multiset.add("b"))
                    .isInstanceOf(OpenCollectionException.class);
        }

        @Test
        @DisplayName("remove - 抛异常")
        void testRemove() {
            ImmutableMultiset<String> multiset = ImmutableMultiset.of("a");

            assertThatThrownBy(() -> multiset.remove("a"))
                    .isInstanceOf(OpenCollectionException.class);
        }

        @Test
        @DisplayName("addAll - 抛异常")
        void testAddAll() {
            ImmutableMultiset<String> multiset = ImmutableMultiset.of("a");

            assertThatThrownBy(() -> multiset.addAll(List.of("b")))
                    .isInstanceOf(OpenCollectionException.class);
        }

        @Test
        @DisplayName("removeAll - 抛异常")
        void testRemoveAll() {
            ImmutableMultiset<String> multiset = ImmutableMultiset.of("a");

            assertThatThrownBy(() -> multiset.removeAll(List.of("a")))
                    .isInstanceOf(OpenCollectionException.class);
        }

        @Test
        @DisplayName("retainAll - 抛异常")
        void testRetainAll() {
            ImmutableMultiset<String> multiset = ImmutableMultiset.of("a", "b");

            assertThatThrownBy(() -> multiset.retainAll(List.of("a")))
                    .isInstanceOf(OpenCollectionException.class);
        }

        @Test
        @DisplayName("clear - 抛异常")
        void testClear() {
            ImmutableMultiset<String> multiset = ImmutableMultiset.of("a");

            assertThatThrownBy(multiset::clear)
                    .isInstanceOf(OpenCollectionException.class);
        }

        @Test
        @DisplayName("iterator.remove - 抛异常")
        void testIteratorRemove() {
            ImmutableMultiset<String> multiset = ImmutableMultiset.of("a");
            Iterator<String> it = multiset.iterator();
            it.next();

            assertThatThrownBy(it::remove)
                    .isInstanceOf(OpenCollectionException.class);
        }
    }

    @Nested
    @DisplayName("Object 方法测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals - 相等")
        void testEquals() {
            ImmutableMultiset<String> multiset1 = ImmutableMultiset.of("a", "b", "a");
            ImmutableMultiset<String> multiset2 = ImmutableMultiset.of("a", "a", "b");

            assertThat(multiset1).isEqualTo(multiset2);
        }

        @Test
        @DisplayName("hashCode - 哈希码")
        void testHashCode() {
            ImmutableMultiset<String> multiset1 = ImmutableMultiset.of("a", "b", "a");
            ImmutableMultiset<String> multiset2 = ImmutableMultiset.of("a", "a", "b");

            assertThat(multiset1.hashCode()).isEqualTo(multiset2.hashCode());
        }

        @Test
        @DisplayName("toString - 字符串表示")
        void testToString() {
            ImmutableMultiset<String> multiset = ImmutableMultiset.of("a", "a");

            assertThat(multiset.toString()).contains("a").contains("2");
        }
    }
}
