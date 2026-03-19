package cloud.opencode.base.collections.specialized;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * EvictingQueue 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("EvictingQueue 测试")
class EvictingQueueTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("create - 指定最大容量")
        void testCreateWithMaxSize() {
            EvictingQueue<String> queue = EvictingQueue.create(3);

            assertThat(queue).isEmpty();
            assertThat(queue.maxSize()).isEqualTo(3);
        }

        @Test
        @DisplayName("create - 无效容量抛异常")
        void testCreateInvalidMaxSize() {
            assertThatThrownBy(() -> EvictingQueue.create(0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> EvictingQueue.create(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("create - 从集合创建")
        void testCreateFromCollection() {
            List<String> source = List.of("a", "b", "c");
            EvictingQueue<String> queue = EvictingQueue.create(5, source);

            assertThat(queue).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("builder - 构建器创建")
        void testBuilder() {
            EvictingQueue<String> queue = EvictingQueue.<String>builder(3).create();

            assertThat(queue).isEmpty();
            assertThat(queue.maxSize()).isEqualTo(3);
        }

        @Test
        @DisplayName("builder - 无效容量抛异常")
        void testBuilderInvalidMaxSize() {
            assertThatThrownBy(() -> EvictingQueue.builder(0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("builder - 带初始元素")
        void testBuilderWithElements() {
            EvictingQueue<String> queue = EvictingQueue.<String>builder(5)
                    .create("a", "b", "c");

            assertThat(queue).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("builder - 从Iterable创建")
        void testBuilderFromIterable() {
            EvictingQueue<String> queue = EvictingQueue.<String>builder(5)
                    .create(List.of("a", "b", "c"));

            assertThat(queue).containsExactly("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("淘汰监听器测试")
    class EvictionListenerTests {

        @Test
        @DisplayName("onEviction - 淘汰时调用监听器")
        void testEvictionListener() {
            List<String> evicted = new ArrayList<>();
            EvictingQueue<String> queue = EvictingQueue.<String>builder(2)
                    .onEviction(evicted::add)
                    .create();

            queue.add("a");
            queue.add("b");
            queue.add("c"); // evicts "a"

            assertThat(evicted).containsExactly("a");
            assertThat(queue).containsExactly("b", "c");
        }

        @Test
        @DisplayName("多次淘汰")
        void testMultipleEvictions() {
            List<String> evicted = new ArrayList<>();
            EvictingQueue<String> queue = EvictingQueue.<String>builder(2)
                    .onEviction(evicted::add)
                    .create();

            queue.add("a");
            queue.add("b");
            queue.add("c"); // evicts "a"
            queue.add("d"); // evicts "b"

            assertThat(evicted).containsExactly("a", "b");
            assertThat(queue).containsExactly("c", "d");
        }
    }

    @Nested
    @DisplayName("队列操作测试")
    class QueueOperationsTests {

        @Test
        @DisplayName("offer - 添加元素")
        void testOffer() {
            EvictingQueue<String> queue = EvictingQueue.create(3);

            assertThat(queue.offer("a")).isTrue();
            assertThat(queue.offer("b")).isTrue();

            assertThat(queue).containsExactly("a", "b");
        }

        @Test
        @DisplayName("offer - null元素抛异常")
        void testOfferNull() {
            EvictingQueue<String> queue = EvictingQueue.create(3);

            assertThatThrownBy(() -> queue.offer(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("offer - 自动淘汰")
        void testOfferWithEviction() {
            EvictingQueue<String> queue = EvictingQueue.create(2);
            queue.offer("a");
            queue.offer("b");
            queue.offer("c");

            assertThat(queue).containsExactly("b", "c");
        }

        @Test
        @DisplayName("add - 添加元素")
        void testAdd() {
            EvictingQueue<String> queue = EvictingQueue.create(3);

            assertThat(queue.add("a")).isTrue();
            assertThat(queue).containsExactly("a");
        }

        @Test
        @DisplayName("poll - 移除头部元素")
        void testPoll() {
            EvictingQueue<String> queue = EvictingQueue.create(3);
            queue.add("a");
            queue.add("b");

            assertThat(queue.poll()).isEqualTo("a");
            assertThat(queue).containsExactly("b");
        }

        @Test
        @DisplayName("poll - 空队列返回null")
        void testPollEmpty() {
            EvictingQueue<String> queue = EvictingQueue.create(3);

            assertThat(queue.poll()).isNull();
        }

        @Test
        @DisplayName("peek - 查看头部元素")
        void testPeek() {
            EvictingQueue<String> queue = EvictingQueue.create(3);
            queue.add("a");
            queue.add("b");

            assertThat(queue.peek()).isEqualTo("a");
            assertThat(queue).hasSize(2); // peek不移除
        }

        @Test
        @DisplayName("peek - 空队列返回null")
        void testPeekEmpty() {
            EvictingQueue<String> queue = EvictingQueue.create(3);

            assertThat(queue.peek()).isNull();
        }
    }

    @Nested
    @DisplayName("额外方法测试")
    class AdditionalMethodsTests {

        @Test
        @DisplayName("maxSize - 返回最大容量")
        void testMaxSize() {
            EvictingQueue<String> queue = EvictingQueue.create(5);

            assertThat(queue.maxSize()).isEqualTo(5);
        }

        @Test
        @DisplayName("remainingCapacity - 返回剩余容量")
        void testRemainingCapacity() {
            EvictingQueue<String> queue = EvictingQueue.create(5);
            queue.add("a");
            queue.add("b");

            assertThat(queue.remainingCapacity()).isEqualTo(3);
        }

        @Test
        @DisplayName("isFull - 判断是否已满")
        void testIsFull() {
            EvictingQueue<String> queue = EvictingQueue.create(2);

            assertThat(queue.isFull()).isFalse();
            queue.add("a");
            assertThat(queue.isFull()).isFalse();
            queue.add("b");
            assertThat(queue.isFull()).isTrue();
        }

        @Test
        @DisplayName("peekLast - 查看尾部元素")
        void testPeekLast() {
            EvictingQueue<String> queue = EvictingQueue.create(3);
            queue.add("a");
            queue.add("b");
            queue.add("c");

            assertThat(queue.peekLast()).isEqualTo("c");
        }

        @Test
        @DisplayName("peekLast - 空队列返回null")
        void testPeekLastEmpty() {
            EvictingQueue<String> queue = EvictingQueue.create(3);

            assertThat(queue.peekLast()).isNull();
        }

        @Test
        @DisplayName("toList - 转换为列表")
        void testToList() {
            EvictingQueue<String> queue = EvictingQueue.create(5);
            queue.add("a");
            queue.add("b");
            queue.add("c");

            List<String> list = queue.toList();

            assertThat(list).containsExactly("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("Collection 操作测试")
    class CollectionOperationsTests {

        @Test
        @DisplayName("size - 返回大小")
        void testSize() {
            EvictingQueue<String> queue = EvictingQueue.create(5);
            queue.add("a");
            queue.add("b");

            assertThat(queue.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("isEmpty - 判断是否为空")
        void testIsEmpty() {
            EvictingQueue<String> queue = EvictingQueue.create(3);

            assertThat(queue.isEmpty()).isTrue();
            queue.add("a");
            assertThat(queue.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("clear - 清空队列")
        void testClear() {
            EvictingQueue<String> queue = EvictingQueue.create(3);
            queue.add("a");
            queue.add("b");
            queue.clear();

            assertThat(queue).isEmpty();
        }

        @Test
        @DisplayName("contains - 包含判断")
        void testContains() {
            EvictingQueue<String> queue = EvictingQueue.create(3);
            queue.add("a");
            queue.add("b");

            assertThat(queue.contains("a")).isTrue();
            assertThat(queue.contains("c")).isFalse();
        }

        @Test
        @DisplayName("remove - 移除元素")
        void testRemove() {
            EvictingQueue<String> queue = EvictingQueue.create(5);
            queue.add("a");
            queue.add("b");
            queue.add("c");

            assertThat(queue.remove("b")).isTrue();
            assertThat(queue).containsExactly("a", "c");
        }

        @Test
        @DisplayName("iterator - 迭代")
        void testIterator() {
            EvictingQueue<String> queue = EvictingQueue.create(5);
            queue.add("a");
            queue.add("b");
            queue.add("c");

            List<String> elements = new ArrayList<>();
            for (String s : queue) {
                elements.add(s);
            }

            assertThat(elements).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("toArray - 转换为数组")
        void testToArray() {
            EvictingQueue<String> queue = EvictingQueue.create(5);
            queue.add("a");
            queue.add("b");

            Object[] array = queue.toArray();
            assertThat(array).containsExactly("a", "b");
        }

        @Test
        @DisplayName("toArray(T[]) - 转换为类型数组")
        void testToArrayTyped() {
            EvictingQueue<String> queue = EvictingQueue.create(5);
            queue.add("a");
            queue.add("b");

            String[] array = queue.toArray(new String[0]);
            assertThat(array).containsExactly("a", "b");
        }
    }
}
