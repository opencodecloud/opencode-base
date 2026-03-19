package cloud.opencode.base.collections.specialized;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * MinMaxPriorityQueue 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("MinMaxPriorityQueue 测试")
class MinMaxPriorityQueueTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("create - 创建空队列")
        void testCreate() {
            MinMaxPriorityQueue<Integer> queue = MinMaxPriorityQueue.create();

            assertThat(queue).isEmpty();
            assertThat(queue.isBounded()).isFalse();
        }

        @Test
        @DisplayName("create - 从集合创建")
        void testCreateFromCollection() {
            MinMaxPriorityQueue<Integer> queue = MinMaxPriorityQueue.create(List.of(5, 2, 8, 1, 9));

            assertThat(queue).hasSize(5);
            assertThat(queue.peekFirst()).isEqualTo(1);
        }

        @Test
        @DisplayName("builder - 构建器创建")
        void testBuilder() {
            MinMaxPriorityQueue<Integer> queue = MinMaxPriorityQueue.<Integer>builder().create();

            assertThat(queue).isEmpty();
        }

        @Test
        @DisplayName("orderedBy - 自定义比较器")
        void testOrderedBy() {
            MinMaxPriorityQueue<String> queue = MinMaxPriorityQueue
                    .orderedBy(Comparator.comparingInt(String::length))
                    .create();

            queue.addAll(List.of("a", "bbb", "cc"));

            assertThat(queue.peekFirst()).isEqualTo("a");
            assertThat(queue.peekLast()).isEqualTo("bbb");
        }

        @Test
        @DisplayName("maximumSize - 创建有界队列")
        void testMaximumSize() {
            MinMaxPriorityQueue<Integer> queue = MinMaxPriorityQueue
                    .<Integer>maximumSize(3)
                    .create();

            assertThat(queue.isBounded()).isTrue();
            assertThat(queue.maximumSize()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("双端操作测试")
    class DoubleEndedTests {

        @Test
        @DisplayName("peekFirst - 获取最小元素")
        void testPeekFirst() {
            MinMaxPriorityQueue<Integer> queue = MinMaxPriorityQueue.create(List.of(5, 2, 8, 1, 9));

            assertThat(queue.peekFirst()).isEqualTo(1);
        }

        @Test
        @DisplayName("peekFirst - 空队列返回null")
        void testPeekFirstEmpty() {
            MinMaxPriorityQueue<Integer> queue = MinMaxPriorityQueue.create();

            assertThat(queue.peekFirst()).isNull();
        }

        @Test
        @DisplayName("peekLast - 获取最大元素")
        void testPeekLast() {
            // 使用3个元素确保最大值在索引1或2处
            MinMaxPriorityQueue<Integer> queue = MinMaxPriorityQueue.create(List.of(5, 2, 8));

            assertThat(queue.peekLast()).isEqualTo(8);
        }

        @Test
        @DisplayName("peekLast - 空队列返回null")
        void testPeekLastEmpty() {
            MinMaxPriorityQueue<Integer> queue = MinMaxPriorityQueue.create();

            assertThat(queue.peekLast()).isNull();
        }

        @Test
        @DisplayName("peekLast - 单元素队列")
        void testPeekLastSingle() {
            MinMaxPriorityQueue<Integer> queue = MinMaxPriorityQueue.create();
            queue.add(5);

            assertThat(queue.peekLast()).isEqualTo(5);
        }

        @Test
        @DisplayName("peekLast - 双元素队列")
        void testPeekLastTwo() {
            MinMaxPriorityQueue<Integer> queue = MinMaxPriorityQueue.create();
            queue.add(3);
            queue.add(7);

            assertThat(queue.peekLast()).isEqualTo(7);
        }

        @Test
        @DisplayName("pollFirst - 移除最小元素")
        void testPollFirst() {
            MinMaxPriorityQueue<Integer> queue = MinMaxPriorityQueue.create(List.of(5, 2, 8, 1, 9));

            assertThat(queue.pollFirst()).isEqualTo(1);
            assertThat(queue.peekFirst()).isEqualTo(2);
        }

        @Test
        @DisplayName("pollFirst - 空队列返回null")
        void testPollFirstEmpty() {
            MinMaxPriorityQueue<Integer> queue = MinMaxPriorityQueue.create();

            assertThat(queue.pollFirst()).isNull();
        }

        @Test
        @DisplayName("pollLast - 移除最大元素")
        void testPollLast() {
            // 使用3个元素确保最大值在索引1或2处
            MinMaxPriorityQueue<Integer> queue = MinMaxPriorityQueue.create(List.of(5, 2, 8));

            assertThat(queue.pollLast()).isEqualTo(8);
            assertThat(queue.peekLast()).isEqualTo(5);
        }

        @Test
        @DisplayName("pollLast - 空队列返回null")
        void testPollLastEmpty() {
            MinMaxPriorityQueue<Integer> queue = MinMaxPriorityQueue.create();

            assertThat(queue.pollLast()).isNull();
        }

        @Test
        @DisplayName("pollLast - 单元素队列")
        void testPollLastSingle() {
            MinMaxPriorityQueue<Integer> queue = MinMaxPriorityQueue.create();
            queue.add(5);

            assertThat(queue.pollLast()).isEqualTo(5);
            assertThat(queue).isEmpty();
        }

        @Test
        @DisplayName("pollLast - 双元素队列")
        void testPollLastTwo() {
            MinMaxPriorityQueue<Integer> queue = MinMaxPriorityQueue.create();
            queue.add(3);
            queue.add(7);

            assertThat(queue.pollLast()).isEqualTo(7);
            assertThat(queue).containsExactly(3);
        }

        @Test
        @DisplayName("removeFirst - 移除最小元素")
        void testRemoveFirst() {
            MinMaxPriorityQueue<Integer> queue = MinMaxPriorityQueue.create(List.of(5, 2, 8));

            assertThat(queue.removeFirst()).isEqualTo(2);
        }

        @Test
        @DisplayName("removeFirst - 空队列抛异常")
        void testRemoveFirstEmpty() {
            MinMaxPriorityQueue<Integer> queue = MinMaxPriorityQueue.create();

            assertThatThrownBy(queue::removeFirst)
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("removeLast - 移除最大元素")
        void testRemoveLast() {
            MinMaxPriorityQueue<Integer> queue = MinMaxPriorityQueue.create(List.of(5, 2, 8));

            assertThat(queue.removeLast()).isEqualTo(8);
        }

        @Test
        @DisplayName("removeLast - 空队列抛异常")
        void testRemoveLastEmpty() {
            MinMaxPriorityQueue<Integer> queue = MinMaxPriorityQueue.create();

            assertThatThrownBy(queue::removeLast)
                    .isInstanceOf(NoSuchElementException.class);
        }
    }

    @Nested
    @DisplayName("有界队列测试")
    class BoundedQueueTests {

        @Test
        @DisplayName("offer - 有界队列自动淘汰最大值")
        void testBoundedQueueEviction() {
            MinMaxPriorityQueue<Integer> queue = MinMaxPriorityQueue
                    .<Integer>maximumSize(3)
                    .create();

            queue.offer(5);
            queue.offer(2);
            queue.offer(8);
            assertThat(queue.offer(1)).isTrue(); // 淘汰8

            assertThat(queue).hasSize(3);
            assertThat(queue.peekLast()).isEqualTo(5);
        }

        @Test
        @DisplayName("offer - 新元素>=最大值时拒绝")
        void testBoundedQueueReject() {
            MinMaxPriorityQueue<Integer> queue = MinMaxPriorityQueue
                    .<Integer>maximumSize(3)
                    .create();

            queue.offer(5);
            queue.offer(2);
            queue.offer(8);

            assertThat(queue.offer(10)).isFalse(); // 被拒绝
            assertThat(queue).hasSize(3);
        }

        @Test
        @DisplayName("isBounded - 判断是否有界")
        void testIsBounded() {
            MinMaxPriorityQueue<Integer> unbounded = MinMaxPriorityQueue.create();
            MinMaxPriorityQueue<Integer> bounded = MinMaxPriorityQueue.<Integer>maximumSize(5).create();

            assertThat(unbounded.isBounded()).isFalse();
            assertThat(bounded.isBounded()).isTrue();
        }

        @Test
        @DisplayName("maximumSize - 返回最大容量")
        void testMaximumSize() {
            MinMaxPriorityQueue<Integer> unbounded = MinMaxPriorityQueue.create();
            MinMaxPriorityQueue<Integer> bounded = MinMaxPriorityQueue.<Integer>maximumSize(5).create();

            assertThat(unbounded.maximumSize()).isEqualTo(Integer.MAX_VALUE);
            assertThat(bounded.maximumSize()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("Queue 操作测试")
    class QueueOperationsTests {

        @Test
        @DisplayName("offer - 添加元素")
        void testOffer() {
            MinMaxPriorityQueue<Integer> queue = MinMaxPriorityQueue.create();

            assertThat(queue.offer(5)).isTrue();
            assertThat(queue.offer(3)).isTrue();

            assertThat(queue).hasSize(2);
        }

        @Test
        @DisplayName("offer - null元素抛异常")
        void testOfferNull() {
            MinMaxPriorityQueue<Integer> queue = MinMaxPriorityQueue.create();

            assertThatThrownBy(() -> queue.offer(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("poll - 移除并返回最小元素")
        void testPoll() {
            MinMaxPriorityQueue<Integer> queue = MinMaxPriorityQueue.create(List.of(5, 2, 8));

            assertThat(queue.poll()).isEqualTo(2);
            assertThat(queue.poll()).isEqualTo(5);
            assertThat(queue.poll()).isEqualTo(8);
        }

        @Test
        @DisplayName("poll - 空队列返回null")
        void testPollEmpty() {
            MinMaxPriorityQueue<Integer> queue = MinMaxPriorityQueue.create();

            assertThat(queue.poll()).isNull();
        }

        @Test
        @DisplayName("peek - 查看最小元素")
        void testPeek() {
            MinMaxPriorityQueue<Integer> queue = MinMaxPriorityQueue.create(List.of(5, 2, 8));

            assertThat(queue.peek()).isEqualTo(2);
            assertThat(queue).hasSize(3); // peek不移除
        }

        @Test
        @DisplayName("peek - 空队列返回null")
        void testPeekEmpty() {
            MinMaxPriorityQueue<Integer> queue = MinMaxPriorityQueue.create();

            assertThat(queue.peek()).isNull();
        }
    }

    @Nested
    @DisplayName("Collection 操作测试")
    class CollectionOperationsTests {

        @Test
        @DisplayName("size - 返回大小")
        void testSize() {
            MinMaxPriorityQueue<Integer> queue = MinMaxPriorityQueue.create(List.of(1, 2, 3));

            assertThat(queue.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("isEmpty - 判断是否为空")
        void testIsEmpty() {
            MinMaxPriorityQueue<Integer> queue = MinMaxPriorityQueue.create();

            assertThat(queue.isEmpty()).isTrue();
            queue.add(1);
            assertThat(queue.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("clear - 清空队列")
        void testClear() {
            MinMaxPriorityQueue<Integer> queue = MinMaxPriorityQueue.create(List.of(1, 2, 3));
            queue.clear();

            assertThat(queue).isEmpty();
        }

        @Test
        @DisplayName("iterator - 迭代")
        void testIterator() {
            MinMaxPriorityQueue<Integer> queue = MinMaxPriorityQueue.create(List.of(5, 2, 8));

            List<Integer> elements = new ArrayList<>();
            for (Integer i : queue) {
                elements.add(i);
            }

            assertThat(elements).hasSize(3);
        }

        @Test
        @DisplayName("iterator - 空队列迭代")
        void testIteratorEmpty() {
            MinMaxPriorityQueue<Integer> queue = MinMaxPriorityQueue.create();
            Iterator<Integer> iter = queue.iterator();

            assertThat(iter.hasNext()).isFalse();
            assertThatThrownBy(iter::next)
                    .isInstanceOf(NoSuchElementException.class);
        }
    }

    @Nested
    @DisplayName("Builder 测试")
    class BuilderTests {

        @Test
        @DisplayName("comparator - 设置比较器")
        void testBuilderComparator() {
            MinMaxPriorityQueue<Integer> queue = MinMaxPriorityQueue.<Integer>builder()
                    .comparator(Comparator.reverseOrder())
                    .create();

            queue.addAll(List.of(1, 2, 3));

            assertThat(queue.peekFirst()).isEqualTo(3); // 反序，最大变最小
        }

        @Test
        @DisplayName("initialCapacity - 设置初始容量")
        void testBuilderInitialCapacity() {
            MinMaxPriorityQueue<Integer> queue = MinMaxPriorityQueue.<Integer>builder()
                    .initialCapacity(100)
                    .create();

            assertThat(queue).isEmpty();
        }

        @Test
        @DisplayName("initialCapacity - 负值抛异常")
        void testBuilderInitialCapacityInvalid() {
            assertThatThrownBy(() -> MinMaxPriorityQueue.builder().initialCapacity(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("maximumSize - 设置最大容量")
        void testBuilderMaximumSize() {
            MinMaxPriorityQueue<Integer> queue = MinMaxPriorityQueue.<Integer>builder()
                    .maximumSize(5)
                    .create();

            assertThat(queue.maximumSize()).isEqualTo(5);
        }

        @Test
        @DisplayName("maximumSize - 无效值抛异常")
        void testBuilderMaximumSizeInvalid() {
            assertThatThrownBy(() -> MinMaxPriorityQueue.builder().maximumSize(0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("create - 带初始元素")
        void testBuilderCreateWithElements() {
            MinMaxPriorityQueue<Integer> queue = MinMaxPriorityQueue.<Integer>builder()
                    .create(5, 2, 8);

            assertThat(queue).hasSize(3);
            assertThat(queue.peekFirst()).isEqualTo(2);
        }

        @Test
        @DisplayName("create - 从Iterable创建")
        void testBuilderCreateFromIterable() {
            MinMaxPriorityQueue<Integer> queue = MinMaxPriorityQueue.<Integer>builder()
                    .create(List.of(5, 2, 8));

            assertThat(queue).hasSize(3);
        }
    }
}
