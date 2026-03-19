package cloud.opencode.base.collections;

import cloud.opencode.base.collections.exception.OpenCollectionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ImmutableSet 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("ImmutableSet 测试")
class ImmutableSetTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of - 创建空集合")
        void testOfEmpty() {
            ImmutableSet<String> set = ImmutableSet.of();

            assertThat(set).isEmpty();
            assertThat(set.size()).isZero();
        }

        @Test
        @DisplayName("of - 单个元素")
        void testOfSingle() {
            ImmutableSet<String> set = ImmutableSet.of("a");

            assertThat(set).containsExactly("a");
        }

        @Test
        @DisplayName("of - 两个元素")
        void testOfTwo() {
            ImmutableSet<String> set = ImmutableSet.of("a", "b");

            assertThat(set).containsExactlyInAnyOrder("a", "b");
        }

        @Test
        @DisplayName("of - 多个元素")
        void testOfMultiple() {
            ImmutableSet<String> set = ImmutableSet.of("a", "b", "c", "d", "e");

            assertThat(set).containsExactlyInAnyOrder("a", "b", "c", "d", "e");
        }

        @Test
        @DisplayName("of - 去重")
        void testOfDeduplication() {
            ImmutableSet<String> set = ImmutableSet.of("a", "b", "a", "c", "b");

            assertThat(set).containsExactlyInAnyOrder("a", "b", "c");
        }

        @Test
        @DisplayName("of - null 元素抛异常")
        void testOfNullElement() {
            assertThatThrownBy(() -> ImmutableSet.of((String) null))
                    .isInstanceOf(NullPointerException.class);

            assertThatThrownBy(() -> ImmutableSet.of("a", null, "c"))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("of - null 数组返回空集合")
        void testOfNullArray() {
            ImmutableSet<String> set = ImmutableSet.of((String[]) null);

            assertThat(set).isEmpty();
        }

        @Test
        @DisplayName("copyOf - 从 Collection 复制")
        void testCopyOfCollection() {
            Set<String> source = Set.of("a", "b", "c");

            ImmutableSet<String> set = ImmutableSet.copyOf(source);

            assertThat(set).containsExactlyInAnyOrder("a", "b", "c");
        }

        @Test
        @DisplayName("copyOf - 从 ImmutableSet 返回相同实例")
        void testCopyOfImmutableSet() {
            ImmutableSet<String> source = ImmutableSet.of("a", "b");

            ImmutableSet<String> copy = ImmutableSet.copyOf(source);

            assertThat(copy).isSameAs(source);
        }

        @Test
        @DisplayName("copyOf - null 或空集合返回空集合")
        void testCopyOfNullOrEmpty() {
            assertThat(ImmutableSet.copyOf((Collection<String>) null)).isEmpty();
            assertThat(ImmutableSet.copyOf(Collections.emptySet())).isEmpty();
        }

        @Test
        @DisplayName("copyOf - 从 Iterable 复制")
        void testCopyOfIterable() {
            Iterable<String> source = () -> List.of("a", "b", "c").iterator();

            ImmutableSet<String> set = ImmutableSet.copyOf(source);

            assertThat(set).containsExactlyInAnyOrder("a", "b", "c");
        }

        @Test
        @DisplayName("copyOf - null Iterable 返回空集合")
        void testCopyOfNullIterable() {
            assertThat(ImmutableSet.copyOf((Iterable<String>) null)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Builder 测试")
    class BuilderTests {

        @Test
        @DisplayName("builder - 添加单个元素")
        void testBuilderAddSingle() {
            ImmutableSet<String> set = ImmutableSet.<String>builder()
                    .add("a")
                    .add("b")
                    .build();

            assertThat(set).containsExactlyInAnyOrder("a", "b");
        }

        @Test
        @DisplayName("builder - 添加多个元素")
        void testBuilderAddMultiple() {
            ImmutableSet<String> set = ImmutableSet.<String>builder()
                    .add("a", "b", "c")
                    .build();

            assertThat(set).containsExactlyInAnyOrder("a", "b", "c");
        }

        @Test
        @DisplayName("builder - addAll")
        void testBuilderAddAll() {
            ImmutableSet<String> set = ImmutableSet.<String>builder()
                    .add("a")
                    .addAll(List.of("b", "c"))
                    .build();

            assertThat(set).containsExactlyInAnyOrder("a", "b", "c");
        }

        @Test
        @DisplayName("builder - 去重")
        void testBuilderDeduplication() {
            ImmutableSet<String> set = ImmutableSet.<String>builder()
                    .add("a")
                    .add("a")
                    .add("b")
                    .build();

            assertThat(set).containsExactlyInAnyOrder("a", "b");
        }

        @Test
        @DisplayName("builder - 空构建返回空集合")
        void testBuilderEmpty() {
            ImmutableSet<String> set = ImmutableSet.<String>builder().build();

            assertThat(set).isEmpty();
        }

        @Test
        @DisplayName("builder - null 元素抛异常")
        void testBuilderNullElement() {
            assertThatThrownBy(() -> ImmutableSet.<String>builder().add((String) null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Set 操作测试")
    class SetOperationTests {

        @Test
        @DisplayName("size - 集合大小")
        void testSize() {
            assertThat(ImmutableSet.of().size()).isZero();
            assertThat(ImmutableSet.of("a").size()).isEqualTo(1);
            assertThat(ImmutableSet.of("a", "b", "c").size()).isEqualTo(3);
        }

        @Test
        @DisplayName("contains - 包含检查")
        void testContains() {
            ImmutableSet<String> set = ImmutableSet.of("a", "b", "c");

            assertThat(set.contains("a")).isTrue();
            assertThat(set.contains("d")).isFalse();
            assertThat(set.contains(null)).isFalse();
        }

        @Test
        @DisplayName("toArray - 转换为数组")
        void testToArray() {
            ImmutableSet<String> set = ImmutableSet.of("a", "b", "c");

            Object[] array = set.toArray();

            assertThat(array).containsExactlyInAnyOrder("a", "b", "c");
        }

        @Test
        @DisplayName("toArray(T[]) - 转换为类型数组")
        void testToArrayWithType() {
            ImmutableSet<String> set = ImmutableSet.of("a", "b", "c");

            String[] array = set.toArray(new String[0]);

            assertThat(array).containsExactlyInAnyOrder("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("不可变保护测试")
    class ImmutabilityTests {

        @Test
        @DisplayName("add - 抛异常")
        void testAdd() {
            ImmutableSet<String> set = ImmutableSet.of("a");

            assertThatThrownBy(() -> set.add("b"))
                    .isInstanceOf(OpenCollectionException.class);
        }

        @Test
        @DisplayName("remove - 抛异常")
        void testRemove() {
            ImmutableSet<String> set = ImmutableSet.of("a", "b");

            assertThatThrownBy(() -> set.remove("a"))
                    .isInstanceOf(OpenCollectionException.class);
        }

        @Test
        @DisplayName("addAll - 抛异常")
        void testAddAll() {
            ImmutableSet<String> set = ImmutableSet.of("a");

            assertThatThrownBy(() -> set.addAll(List.of("b")))
                    .isInstanceOf(OpenCollectionException.class);
        }

        @Test
        @DisplayName("removeAll - 抛异常")
        void testRemoveAll() {
            ImmutableSet<String> set = ImmutableSet.of("a", "b");

            assertThatThrownBy(() -> set.removeAll(List.of("a")))
                    .isInstanceOf(OpenCollectionException.class);
        }

        @Test
        @DisplayName("retainAll - 抛异常")
        void testRetainAll() {
            ImmutableSet<String> set = ImmutableSet.of("a", "b");

            assertThatThrownBy(() -> set.retainAll(List.of("a")))
                    .isInstanceOf(OpenCollectionException.class);
        }

        @Test
        @DisplayName("clear - 抛异常")
        void testClear() {
            ImmutableSet<String> set = ImmutableSet.of("a");

            assertThatThrownBy(set::clear)
                    .isInstanceOf(OpenCollectionException.class);
        }

        @Test
        @DisplayName("iterator.remove - 抛异常")
        void testIteratorRemove() {
            ImmutableSet<String> set = ImmutableSet.of("a", "b");
            Iterator<String> iterator = set.iterator();
            iterator.next();

            assertThatThrownBy(iterator::remove)
                    .isInstanceOf(OpenCollectionException.class);
        }
    }

    @Nested
    @DisplayName("迭代测试")
    class IterationTests {

        @Test
        @DisplayName("forEach - 遍历")
        void testForEach() {
            ImmutableSet<String> set = ImmutableSet.of("a", "b", "c");
            Set<String> result = new HashSet<>();

            set.forEach(result::add);

            assertThat(result).containsExactlyInAnyOrder("a", "b", "c");
        }

        @Test
        @DisplayName("iterator - 迭代器")
        void testIterator() {
            ImmutableSet<String> set = ImmutableSet.of("a", "b");
            Set<String> result = new HashSet<>();

            Iterator<String> iterator = set.iterator();
            while (iterator.hasNext()) {
                result.add(iterator.next());
            }

            assertThat(result).containsExactlyInAnyOrder("a", "b");
        }
    }

    @Nested
    @DisplayName("Object 方法测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals - 相等比较")
        void testEquals() {
            ImmutableSet<String> set1 = ImmutableSet.of("a", "b");
            ImmutableSet<String> set2 = ImmutableSet.of("b", "a");
            Set<String> set3 = Set.of("a", "b");

            assertThat(set1).isEqualTo(set2);
            assertThat(set1).isEqualTo(set3);
        }

        @Test
        @DisplayName("hashCode - 哈希码一致性")
        void testHashCode() {
            ImmutableSet<String> set1 = ImmutableSet.of("a", "b");
            ImmutableSet<String> set2 = ImmutableSet.of("a", "b");

            assertThat(set1.hashCode()).isEqualTo(set2.hashCode());
        }

        @Test
        @DisplayName("toString - 字符串表示")
        void testToString() {
            ImmutableSet<String> set = ImmutableSet.of("a");

            String str = set.toString();

            assertThat(str).contains("a");
        }
    }
}
