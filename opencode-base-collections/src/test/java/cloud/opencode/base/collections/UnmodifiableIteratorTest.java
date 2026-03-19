package cloud.opencode.base.collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * UnmodifiableIterator 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("UnmodifiableIterator 测试")
class UnmodifiableIteratorTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of - 包装普通迭代器")
        void testOf() {
            Iterator<String> original = List.of("a", "b", "c").iterator();
            UnmodifiableIterator<String> unmodifiable = UnmodifiableIterator.of(original);

            assertThat(unmodifiable).isNotNull();
            assertThat(unmodifiable.hasNext()).isTrue();
        }

        @Test
        @DisplayName("of - 已是UnmodifiableIterator则返回自身")
        void testOfAlreadyUnmodifiable() {
            Iterator<String> original = List.of("a", "b").iterator();
            UnmodifiableIterator<String> unmodifiable = UnmodifiableIterator.of(original);
            UnmodifiableIterator<String> wrapped = UnmodifiableIterator.of(unmodifiable);

            assertThat(wrapped).isSameAs(unmodifiable);
        }
    }

    @Nested
    @DisplayName("迭代操作测试")
    class IterationTests {

        @Test
        @DisplayName("hasNext - 有下一个元素")
        void testHasNext() {
            Iterator<String> original = List.of("a", "b").iterator();
            UnmodifiableIterator<String> unmodifiable = UnmodifiableIterator.of(original);

            assertThat(unmodifiable.hasNext()).isTrue();
            unmodifiable.next();
            assertThat(unmodifiable.hasNext()).isTrue();
            unmodifiable.next();
            assertThat(unmodifiable.hasNext()).isFalse();
        }

        @Test
        @DisplayName("next - 获取下一个元素")
        void testNext() {
            Iterator<String> original = List.of("a", "b", "c").iterator();
            UnmodifiableIterator<String> unmodifiable = UnmodifiableIterator.of(original);

            assertThat(unmodifiable.next()).isEqualTo("a");
            assertThat(unmodifiable.next()).isEqualTo("b");
            assertThat(unmodifiable.next()).isEqualTo("c");
        }

        @Test
        @DisplayName("next - 无更多元素时抛异常")
        void testNextNoMoreElements() {
            Iterator<String> original = List.of("a").iterator();
            UnmodifiableIterator<String> unmodifiable = UnmodifiableIterator.of(original);

            unmodifiable.next();
            assertThatThrownBy(unmodifiable::next)
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("完整迭代")
        void testFullIteration() {
            List<String> source = List.of("a", "b", "c");
            UnmodifiableIterator<String> unmodifiable = UnmodifiableIterator.of(source.iterator());

            List<String> result = new ArrayList<>();
            while (unmodifiable.hasNext()) {
                result.add(unmodifiable.next());
            }

            assertThat(result).containsExactly("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("remove 操作测试")
    class RemoveTests {

        @Test
        @DisplayName("remove - 抛出UnsupportedOperationException")
        void testRemoveThrows() {
            Iterator<String> original = new ArrayList<>(List.of("a", "b")).iterator();
            UnmodifiableIterator<String> unmodifiable = UnmodifiableIterator.of(original);

            unmodifiable.next();
            assertThatThrownBy(unmodifiable::remove)
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessageContaining("remove");
        }

        @Test
        @DisplayName("remove - 调用前未调用next")
        void testRemoveBeforeNext() {
            Iterator<String> original = List.of("a", "b").iterator();
            UnmodifiableIterator<String> unmodifiable = UnmodifiableIterator.of(original);

            assertThatThrownBy(unmodifiable::remove)
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("空迭代器测试")
    class EmptyIteratorTests {

        @Test
        @DisplayName("空迭代器 - hasNext返回false")
        void testEmptyIteratorHasNext() {
            Iterator<String> original = Collections.emptyIterator();
            UnmodifiableIterator<String> unmodifiable = UnmodifiableIterator.of(original);

            assertThat(unmodifiable.hasNext()).isFalse();
        }

        @Test
        @DisplayName("空迭代器 - next抛异常")
        void testEmptyIteratorNext() {
            Iterator<String> original = Collections.emptyIterator();
            UnmodifiableIterator<String> unmodifiable = UnmodifiableIterator.of(original);

            assertThatThrownBy(unmodifiable::next)
                    .isInstanceOf(NoSuchElementException.class);
        }
    }
}
