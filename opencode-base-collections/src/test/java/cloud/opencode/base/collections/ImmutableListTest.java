package cloud.opencode.base.collections;

import cloud.opencode.base.collections.exception.OpenCollectionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ImmutableList 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("ImmutableList 测试")
class ImmutableListTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of - 创建空列表")
        void testOfEmpty() {
            ImmutableList<String> list = ImmutableList.of();

            assertThat(list).isEmpty();
            assertThat(list.size()).isZero();
        }

        @Test
        @DisplayName("of - 单个元素")
        void testOfSingle() {
            ImmutableList<String> list = ImmutableList.of("a");

            assertThat(list).containsExactly("a");
        }

        @Test
        @DisplayName("of - 两个元素")
        void testOfTwo() {
            ImmutableList<String> list = ImmutableList.of("a", "b");

            assertThat(list).containsExactly("a", "b");
        }

        @Test
        @DisplayName("of - 多个元素")
        void testOfMultiple() {
            ImmutableList<String> list = ImmutableList.of("a", "b", "c", "d", "e");

            assertThat(list).containsExactly("a", "b", "c", "d", "e");
        }

        @Test
        @DisplayName("of - null 元素抛异常")
        void testOfNullElement() {
            assertThatThrownBy(() -> ImmutableList.of((String) null))
                    .isInstanceOf(NullPointerException.class);

            assertThatThrownBy(() -> ImmutableList.of("a", null, "c"))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("of - null 数组返回空列表")
        void testOfNullArray() {
            ImmutableList<String> list = ImmutableList.of((String[]) null);

            assertThat(list).isEmpty();
        }

        @Test
        @DisplayName("copyOf - 从 Collection 复制")
        void testCopyOfCollection() {
            List<String> source = List.of("a", "b", "c");

            ImmutableList<String> list = ImmutableList.copyOf(source);

            assertThat(list).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("copyOf - 从 ImmutableList 返回相同实例")
        void testCopyOfImmutableList() {
            ImmutableList<String> source = ImmutableList.of("a", "b");

            ImmutableList<String> copy = ImmutableList.copyOf(source);

            assertThat(copy).isSameAs(source);
        }

        @Test
        @DisplayName("copyOf - null 或空集合返回空列表")
        void testCopyOfNullOrEmpty() {
            assertThat(ImmutableList.copyOf((Collection<String>) null)).isEmpty();
            assertThat(ImmutableList.copyOf(Collections.emptyList())).isEmpty();
        }

        @Test
        @DisplayName("copyOf - 从 Iterable 复制")
        void testCopyOfIterable() {
            Iterable<String> source = () -> List.of("a", "b", "c").iterator();

            ImmutableList<String> list = ImmutableList.copyOf(source);

            assertThat(list).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("copyOf - null Iterable 返回空列表")
        void testCopyOfNullIterable() {
            assertThat(ImmutableList.copyOf((Iterable<String>) null)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Builder 测试")
    class BuilderTests {

        @Test
        @DisplayName("builder - 添加单个元素")
        void testBuilderAddSingle() {
            ImmutableList<String> list = ImmutableList.<String>builder()
                    .add("a")
                    .add("b")
                    .build();

            assertThat(list).containsExactly("a", "b");
        }

        @Test
        @DisplayName("builder - 添加多个元素")
        void testBuilderAddMultiple() {
            ImmutableList<String> list = ImmutableList.<String>builder()
                    .add("a", "b", "c")
                    .build();

            assertThat(list).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("builder - addAll")
        void testBuilderAddAll() {
            ImmutableList<String> list = ImmutableList.<String>builder()
                    .add("a")
                    .addAll(List.of("b", "c"))
                    .build();

            assertThat(list).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("builder - 空构建返回空列表")
        void testBuilderEmpty() {
            ImmutableList<String> list = ImmutableList.<String>builder().build();

            assertThat(list).isEmpty();
        }

        @Test
        @DisplayName("builder - null 元素抛异常")
        void testBuilderNullElement() {
            assertThatThrownBy(() -> ImmutableList.<String>builder().add((String) null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("List 操作测试")
    class ListOperationTests {

        @Test
        @DisplayName("get - 按索引获取")
        void testGet() {
            ImmutableList<String> list = ImmutableList.of("a", "b", "c");

            assertThat(list.get(0)).isEqualTo("a");
            assertThat(list.get(1)).isEqualTo("b");
            assertThat(list.get(2)).isEqualTo("c");
        }

        @Test
        @DisplayName("get - 索引越界")
        void testGetOutOfBounds() {
            ImmutableList<String> list = ImmutableList.of("a");

            assertThatThrownBy(() -> list.get(-1))
                    .isInstanceOf(IndexOutOfBoundsException.class);

            assertThatThrownBy(() -> list.get(1))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }

        @Test
        @DisplayName("size - 列表大小")
        void testSize() {
            assertThat(ImmutableList.of().size()).isZero();
            assertThat(ImmutableList.of("a").size()).isEqualTo(1);
            assertThat(ImmutableList.of("a", "b", "c").size()).isEqualTo(3);
        }

        @Test
        @DisplayName("contains - 包含检查")
        void testContains() {
            ImmutableList<String> list = ImmutableList.of("a", "b", "c");

            assertThat(list.contains("a")).isTrue();
            assertThat(list.contains("d")).isFalse();
            assertThat(list.contains(null)).isFalse();
        }

        @Test
        @DisplayName("indexOf - 首次出现索引")
        void testIndexOf() {
            ImmutableList<String> list = ImmutableList.of("a", "b", "a", "c");

            assertThat(list.indexOf("a")).isEqualTo(0);
            assertThat(list.indexOf("b")).isEqualTo(1);
            assertThat(list.indexOf("d")).isEqualTo(-1);
            assertThat(list.indexOf(null)).isEqualTo(-1);
        }

        @Test
        @DisplayName("lastIndexOf - 最后出现索引")
        void testLastIndexOf() {
            ImmutableList<String> list = ImmutableList.of("a", "b", "a", "c");

            assertThat(list.lastIndexOf("a")).isEqualTo(2);
            assertThat(list.lastIndexOf("b")).isEqualTo(1);
            assertThat(list.lastIndexOf("d")).isEqualTo(-1);
            assertThat(list.lastIndexOf(null)).isEqualTo(-1);
        }

        @Test
        @DisplayName("toArray - 转换为数组")
        void testToArray() {
            ImmutableList<String> list = ImmutableList.of("a", "b", "c");

            Object[] array = list.toArray();

            assertThat(array).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("toArray(T[]) - 转换为类型数组")
        void testToArrayWithType() {
            ImmutableList<String> list = ImmutableList.of("a", "b", "c");

            String[] array = list.toArray(new String[0]);
            assertThat(array).containsExactly("a", "b", "c");

            String[] largerArray = list.toArray(new String[5]);
            assertThat(largerArray[0]).isEqualTo("a");
            assertThat(largerArray[3]).isNull();
        }
    }

    @Nested
    @DisplayName("不可变保护测试")
    class ImmutabilityTests {

        @Test
        @DisplayName("set - 抛异常")
        void testSet() {
            ImmutableList<String> list = ImmutableList.of("a", "b");

            assertThatThrownBy(() -> list.set(0, "x"))
                    .isInstanceOf(OpenCollectionException.class);
        }

        @Test
        @DisplayName("add - 抛异常")
        void testAdd() {
            ImmutableList<String> list = ImmutableList.of("a");

            assertThatThrownBy(() -> list.add("b"))
                    .isInstanceOf(OpenCollectionException.class);

            assertThatThrownBy(() -> list.add(0, "x"))
                    .isInstanceOf(OpenCollectionException.class);
        }

        @Test
        @DisplayName("remove - 抛异常")
        void testRemove() {
            ImmutableList<String> list = ImmutableList.of("a", "b");

            assertThatThrownBy(() -> list.remove(0))
                    .isInstanceOf(OpenCollectionException.class);

            assertThatThrownBy(() -> list.remove("a"))
                    .isInstanceOf(OpenCollectionException.class);
        }

        @Test
        @DisplayName("addAll - 抛异常")
        void testAddAll() {
            ImmutableList<String> list = ImmutableList.of("a");

            assertThatThrownBy(() -> list.addAll(List.of("b")))
                    .isInstanceOf(OpenCollectionException.class);

            assertThatThrownBy(() -> list.addAll(0, List.of("x")))
                    .isInstanceOf(OpenCollectionException.class);
        }

        @Test
        @DisplayName("removeAll - 抛异常")
        void testRemoveAll() {
            ImmutableList<String> list = ImmutableList.of("a", "b");

            assertThatThrownBy(() -> list.removeAll(List.of("a")))
                    .isInstanceOf(OpenCollectionException.class);
        }

        @Test
        @DisplayName("retainAll - 抛异常")
        void testRetainAll() {
            ImmutableList<String> list = ImmutableList.of("a", "b");

            assertThatThrownBy(() -> list.retainAll(List.of("a")))
                    .isInstanceOf(OpenCollectionException.class);
        }

        @Test
        @DisplayName("clear - 抛异常")
        void testClear() {
            ImmutableList<String> list = ImmutableList.of("a");

            assertThatThrownBy(list::clear)
                    .isInstanceOf(OpenCollectionException.class);
        }

        @Test
        @DisplayName("replaceAll - 抛异常")
        void testReplaceAll() {
            ImmutableList<String> list = ImmutableList.of("a");

            assertThatThrownBy(() -> list.replaceAll(String::toUpperCase))
                    .isInstanceOf(OpenCollectionException.class);
        }

        @Test
        @DisplayName("sort - 抛异常")
        void testSort() {
            ImmutableList<String> list = ImmutableList.of("b", "a");

            assertThatThrownBy(() -> list.sort(Comparator.naturalOrder()))
                    .isInstanceOf(OpenCollectionException.class);
        }
    }

    @Nested
    @DisplayName("迭代测试")
    class IterationTests {

        @Test
        @DisplayName("forEach - 遍历")
        void testForEach() {
            ImmutableList<String> list = ImmutableList.of("a", "b", "c");
            List<String> result = new ArrayList<>();

            list.forEach(result::add);

            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("iterator - 迭代器")
        void testIterator() {
            ImmutableList<String> list = ImmutableList.of("a", "b");
            List<String> result = new ArrayList<>();

            for (String s : list) {
                result.add(s);
            }

            assertThat(result).containsExactly("a", "b");
        }
    }

    @Nested
    @DisplayName("Object 方法测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals - 相等比较")
        void testEquals() {
            ImmutableList<String> list1 = ImmutableList.of("a", "b");
            ImmutableList<String> list2 = ImmutableList.of("a", "b");
            List<String> list3 = List.of("a", "b");

            assertThat(list1).isEqualTo(list2);
            assertThat(list1).isEqualTo(list3);
        }

        @Test
        @DisplayName("hashCode - 哈希码一致性")
        void testHashCode() {
            ImmutableList<String> list1 = ImmutableList.of("a", "b");
            ImmutableList<String> list2 = ImmutableList.of("a", "b");

            assertThat(list1.hashCode()).isEqualTo(list2.hashCode());
        }

        @Test
        @DisplayName("toString - 字符串表示")
        void testToString() {
            ImmutableList<String> list = ImmutableList.of("a", "b");

            assertThat(list.toString()).isEqualTo("[a, b]");
        }
    }

    @Nested
    @DisplayName("RandomAccess 测试")
    class RandomAccessTests {

        @Test
        @DisplayName("实现 RandomAccess")
        void testImplementsRandomAccess() {
            ImmutableList<String> list = ImmutableList.of("a", "b", "c");

            assertThat(list).isInstanceOf(RandomAccess.class);
        }
    }
}
