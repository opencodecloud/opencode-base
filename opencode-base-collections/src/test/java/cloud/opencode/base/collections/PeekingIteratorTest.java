package cloud.opencode.base.collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * PeekingIterator 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("PeekingIterator 测试")
class PeekingIteratorTest {

    @Nested
    @DisplayName("基本操作测试")
    class BasicOperationTests {

        @Test
        @DisplayName("peek - 查看下一个元素")
        void testPeek() {
            List<String> list = List.of("a", "b", "c");
            PeekingIterator<String> it = IteratorUtil.peekingIterator(list.iterator());

            assertThat(it.peek()).isEqualTo("a");
            assertThat(it.peek()).isEqualTo("a");  // peek 不移动指针
        }

        @Test
        @DisplayName("peek 后 next - 返回相同元素")
        void testPeekThenNext() {
            List<String> list = List.of("a", "b", "c");
            PeekingIterator<String> it = IteratorUtil.peekingIterator(list.iterator());

            String peeked = it.peek();
            String next = it.next();

            assertThat(peeked).isEqualTo(next);
            assertThat(peeked).isEqualTo("a");
        }

        @Test
        @DisplayName("hasNext - 检查是否有下一个元素")
        void testHasNext() {
            List<String> list = List.of("a");
            PeekingIterator<String> it = IteratorUtil.peekingIterator(list.iterator());

            assertThat(it.hasNext()).isTrue();
            it.next();
            assertThat(it.hasNext()).isFalse();
        }

        @Test
        @DisplayName("next - 获取下一个元素并前进")
        void testNext() {
            List<String> list = List.of("a", "b", "c");
            PeekingIterator<String> it = IteratorUtil.peekingIterator(list.iterator());

            assertThat(it.next()).isEqualTo("a");
            assertThat(it.next()).isEqualTo("b");
            assertThat(it.next()).isEqualTo("c");
        }

        @Test
        @DisplayName("peek 空迭代器抛异常")
        void testPeekEmpty() {
            List<String> list = List.of();
            PeekingIterator<String> it = IteratorUtil.peekingIterator(list.iterator());

            assertThatThrownBy(it::peek)
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("next 空迭代器抛异常")
        void testNextEmpty() {
            List<String> list = List.of();
            PeekingIterator<String> it = IteratorUtil.peekingIterator(list.iterator());

            assertThatThrownBy(it::next)
                    .isInstanceOf(NoSuchElementException.class);
        }
    }

    @Nested
    @DisplayName("遍历测试")
    class TraversalTests {

        @Test
        @DisplayName("完整遍历列表")
        void testFullTraversal() {
            List<String> list = List.of("a", "b", "c");
            PeekingIterator<String> it = IteratorUtil.peekingIterator(list.iterator());
            List<String> result = new ArrayList<>();

            while (it.hasNext()) {
                result.add(it.next());
            }

            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("使用 peek 进行条件遍历")
        void testConditionalTraversal() {
            List<Integer> list = List.of(1, 2, 3, 4, 5);
            PeekingIterator<Integer> it = IteratorUtil.peekingIterator(list.iterator());
            List<Integer> result = new ArrayList<>();

            while (it.hasNext()) {
                if (it.peek() % 2 == 0) {
                    result.add(it.next());
                } else {
                    it.next();  // 跳过奇数
                }
            }

            assertThat(result).containsExactly(2, 4);
        }

        @Test
        @DisplayName("peek 和 next 交替使用")
        void testAlternatePeekNext() {
            List<String> list = List.of("a", "b", "c");
            PeekingIterator<String> it = IteratorUtil.peekingIterator(list.iterator());

            assertThat(it.peek()).isEqualTo("a");
            assertThat(it.next()).isEqualTo("a");
            assertThat(it.peek()).isEqualTo("b");
            assertThat(it.peek()).isEqualTo("b");
            assertThat(it.next()).isEqualTo("b");
            assertThat(it.next()).isEqualTo("c");
        }
    }

    @Nested
    @DisplayName("null 元素测试")
    class NullElementTests {

        @Test
        @DisplayName("处理 null 元素")
        void testNullElements() {
            List<String> list = new ArrayList<>();
            list.add("a");
            list.add(null);
            list.add("c");

            PeekingIterator<String> it = IteratorUtil.peekingIterator(list.iterator());

            assertThat(it.next()).isEqualTo("a");
            assertThat(it.peek()).isNull();
            assertThat(it.next()).isNull();
            assertThat(it.next()).isEqualTo("c");
        }
    }

    @Nested
    @DisplayName("remove 操作测试")
    class RemoveTests {

        @Test
        @DisplayName("remove - 移除元素")
        void testRemove() {
            List<String> list = new ArrayList<>(List.of("a", "b", "c"));
            PeekingIterator<String> it = IteratorUtil.peekingIterator(list.iterator());

            it.next();
            it.remove();

            assertThat(list).containsExactly("b", "c");
        }

        @Test
        @DisplayName("peek 后 remove 抛异常")
        void testRemoveAfterPeek() {
            List<String> list = new ArrayList<>(List.of("a", "b", "c"));
            PeekingIterator<String> it = IteratorUtil.peekingIterator(list.iterator());

            it.next();
            it.peek();

            // 在 peek 后调用 remove 可能会抛异常，取决于实现
            // 这里我们测试它的行为
            assertThatThrownBy(it::remove)
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("单元素测试")
    class SingleElementTests {

        @Test
        @DisplayName("单元素列表")
        void testSingleElement() {
            List<String> list = List.of("only");
            PeekingIterator<String> it = IteratorUtil.peekingIterator(list.iterator());

            assertThat(it.hasNext()).isTrue();
            assertThat(it.peek()).isEqualTo("only");
            assertThat(it.peek()).isEqualTo("only");
            assertThat(it.next()).isEqualTo("only");
            assertThat(it.hasNext()).isFalse();
        }
    }

    @Nested
    @DisplayName("边界情况测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("在最后一个元素上多次 peek")
        void testPeekAtLast() {
            List<String> list = List.of("a", "b");
            PeekingIterator<String> it = IteratorUtil.peekingIterator(list.iterator());

            it.next();  // 跳过 "a"
            assertThat(it.peek()).isEqualTo("b");
            assertThat(it.peek()).isEqualTo("b");
            assertThat(it.peek()).isEqualTo("b");
            assertThat(it.next()).isEqualTo("b");
            assertThat(it.hasNext()).isFalse();
        }

        @Test
        @DisplayName("连续调用 peek 返回相同值")
        void testConsecutivePeeks() {
            List<Integer> list = List.of(1, 2, 3);
            PeekingIterator<Integer> it = IteratorUtil.peekingIterator(list.iterator());

            Integer first = it.peek();
            Integer second = it.peek();
            Integer third = it.peek();

            assertThat(first).isEqualTo(second).isEqualTo(third).isEqualTo(1);
        }
    }
}
