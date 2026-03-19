package cloud.opencode.base.collections.concurrent;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ConcurrentCollectionFactory 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("ConcurrentCollectionFactory 测试")
class ConcurrentCollectionFactoryTest {

    @Nested
    @DisplayName("Map 工厂方法测试")
    class MapFactoryTests {

        @Test
        @DisplayName("newConcurrentMap - 创建并发 Map")
        void testNewConcurrentMap() {
            ConcurrentMap<String, Integer> map = ConcurrentCollectionFactory.newConcurrentMap();

            assertThat(map).isNotNull();
            assertThat(map).isEmpty();
            assertThat(map).isInstanceOf(ConcurrentHashMap.class);

            map.put("a", 1);
            assertThat(map.get("a")).isEqualTo(1);
        }

        @Test
        @DisplayName("newConcurrentMap - 指定容量")
        void testNewConcurrentMapWithCapacity() {
            ConcurrentMap<String, Integer> map = ConcurrentCollectionFactory.newConcurrentMap(100);

            assertThat(map).isNotNull();
            assertThat(map).isEmpty();
        }

        @Test
        @DisplayName("newConcurrentSortedMap - 创建并发排序 Map")
        void testNewConcurrentSortedMap() {
            ConcurrentNavigableMap<String, Integer> map = ConcurrentCollectionFactory.newConcurrentSortedMap();

            assertThat(map).isNotNull();
            assertThat(map).isInstanceOf(ConcurrentSkipListMap.class);

            map.put("c", 3);
            map.put("a", 1);
            map.put("b", 2);

            assertThat(new ArrayList<>(map.keySet())).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("newConcurrentSortedMap - 自定义比较器")
        void testNewConcurrentSortedMapWithComparator() {
            ConcurrentNavigableMap<String, Integer> map =
                    ConcurrentCollectionFactory.newConcurrentSortedMap(Comparator.reverseOrder());

            map.put("a", 1);
            map.put("c", 3);
            map.put("b", 2);

            assertThat(new ArrayList<>(map.keySet())).containsExactly("c", "b", "a");
        }
    }

    @Nested
    @DisplayName("Set 工厂方法测试")
    class SetFactoryTests {

        @Test
        @DisplayName("newConcurrentSet - 创建并发 Set")
        void testNewConcurrentSet() {
            Set<String> set = ConcurrentCollectionFactory.newConcurrentSet();

            assertThat(set).isNotNull();
            assertThat(set).isEmpty();

            set.add("a");
            set.add("b");

            assertThat(set).containsExactlyInAnyOrder("a", "b");
        }

        @Test
        @DisplayName("newConcurrentSet - 指定容量")
        void testNewConcurrentSetWithCapacity() {
            Set<String> set = ConcurrentCollectionFactory.newConcurrentSet(100);

            assertThat(set).isNotNull();
            assertThat(set).isEmpty();
        }

        @Test
        @DisplayName("newConcurrentSortedSet - 创建并发排序 Set")
        void testNewConcurrentSortedSet() {
            NavigableSet<String> set = ConcurrentCollectionFactory.newConcurrentSortedSet();

            assertThat(set).isNotNull();
            assertThat(set).isInstanceOf(ConcurrentSkipListSet.class);

            set.add("c");
            set.add("a");
            set.add("b");

            assertThat(new ArrayList<>(set)).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("newConcurrentSortedSet - 自定义比较器")
        void testNewConcurrentSortedSetWithComparator() {
            NavigableSet<String> set =
                    ConcurrentCollectionFactory.newConcurrentSortedSet(Comparator.reverseOrder());

            set.add("a");
            set.add("c");
            set.add("b");

            assertThat(new ArrayList<>(set)).containsExactly("c", "b", "a");
        }
    }

    @Nested
    @DisplayName("Queue 工厂方法测试")
    class QueueFactoryTests {

        @Test
        @DisplayName("newConcurrentQueue - 创建并发队列")
        void testNewConcurrentQueue() {
            Queue<String> queue = ConcurrentCollectionFactory.newConcurrentQueue();

            assertThat(queue).isNotNull();
            assertThat(queue).isInstanceOf(ConcurrentLinkedQueue.class);

            queue.offer("a");
            queue.offer("b");

            assertThat(queue.poll()).isEqualTo("a");
        }

        @Test
        @DisplayName("newConcurrentDeque - 创建并发双端队列")
        void testNewConcurrentDeque() {
            Deque<String> deque = ConcurrentCollectionFactory.newConcurrentDeque();

            assertThat(deque).isNotNull();
            assertThat(deque).isInstanceOf(ConcurrentLinkedDeque.class);

            deque.offerFirst("a");
            deque.offerLast("b");

            assertThat(deque.pollFirst()).isEqualTo("a");
            assertThat(deque.pollLast()).isEqualTo("b");
        }

        @Test
        @DisplayName("newBlockingQueue - 创建阻塞队列")
        void testNewBlockingQueue() {
            BlockingQueue<String> queue = ConcurrentCollectionFactory.newBlockingQueue(10);

            assertThat(queue).isNotNull();
            assertThat(queue).isInstanceOf(ArrayBlockingQueue.class);

            queue.offer("a");
            assertThat(queue.poll()).isEqualTo("a");
        }

        @Test
        @DisplayName("newLinkedBlockingQueue - 创建链接阻塞队列")
        void testNewLinkedBlockingQueue() {
            BlockingQueue<String> queue = ConcurrentCollectionFactory.newLinkedBlockingQueue();

            assertThat(queue).isNotNull();
            assertThat(queue).isInstanceOf(LinkedBlockingQueue.class);
        }

        @Test
        @DisplayName("newLinkedBlockingQueue - 指定容量")
        void testNewLinkedBlockingQueueWithCapacity() {
            BlockingQueue<String> queue = ConcurrentCollectionFactory.newLinkedBlockingQueue(100);

            assertThat(queue).isNotNull();
        }

        @Test
        @DisplayName("newPriorityBlockingQueue - 创建优先级阻塞队列")
        void testNewPriorityBlockingQueue() {
            BlockingQueue<Integer> queue = ConcurrentCollectionFactory.newPriorityBlockingQueue();

            assertThat(queue).isNotNull();
            assertThat(queue).isInstanceOf(PriorityBlockingQueue.class);

            queue.offer(3);
            queue.offer(1);
            queue.offer(2);

            assertThat(queue.poll()).isEqualTo(1);
        }

        @Test
        @DisplayName("newDelayQueue - 创建延迟队列")
        void testNewDelayQueue() {
            BlockingQueue<Delayed> queue = ConcurrentCollectionFactory.newDelayQueue();

            assertThat(queue).isNotNull();
            assertThat(queue).isInstanceOf(DelayQueue.class);
        }

        @Test
        @DisplayName("newSynchronousQueue - 创建同步队列")
        void testNewSynchronousQueue() {
            BlockingQueue<String> queue = ConcurrentCollectionFactory.newSynchronousQueue();

            assertThat(queue).isNotNull();
            assertThat(queue).isInstanceOf(SynchronousQueue.class);
        }

        @Test
        @DisplayName("newBlockingDeque - 创建阻塞双端队列")
        void testNewBlockingDeque() {
            BlockingDeque<String> deque = ConcurrentCollectionFactory.newBlockingDeque();

            assertThat(deque).isNotNull();
            assertThat(deque).isInstanceOf(LinkedBlockingDeque.class);
        }

        @Test
        @DisplayName("newBlockingDeque - 指定容量")
        void testNewBlockingDequeWithCapacity() {
            BlockingDeque<String> deque = ConcurrentCollectionFactory.newBlockingDeque(100);

            assertThat(deque).isNotNull();
        }
    }

    @Nested
    @DisplayName("List 工厂方法测试")
    class ListFactoryTests {

        @Test
        @DisplayName("newCopyOnWriteList - 创建写时复制列表")
        void testNewCopyOnWriteList() {
            List<String> list = ConcurrentCollectionFactory.newCopyOnWriteList();

            assertThat(list).isNotNull();
            assertThat(list).isInstanceOf(CopyOnWriteArrayList.class);

            list.add("a");
            list.add("b");

            assertThat(list).containsExactly("a", "b");
        }

        @Test
        @DisplayName("newCopyOnWriteList - 从集合创建")
        void testNewCopyOnWriteListFromCollection() {
            List<String> list = ConcurrentCollectionFactory.newCopyOnWriteList(List.of("a", "b", "c"));

            assertThat(list).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("newCopyOnWriteSet - 创建写时复制集合")
        void testNewCopyOnWriteSet() {
            Set<String> set = ConcurrentCollectionFactory.newCopyOnWriteSet();

            assertThat(set).isNotNull();
            assertThat(set).isInstanceOf(CopyOnWriteArraySet.class);

            set.add("a");
            set.add("b");

            assertThat(set).containsExactlyInAnyOrder("a", "b");
        }
    }

    @Nested
    @DisplayName("包装方法测试")
    class WrapperTests {

        @Test
        @DisplayName("synchronizedMap - 同步 Map")
        void testSynchronizedMap() {
            Map<String, Integer> map = ConcurrentCollectionFactory.synchronizedMap(new HashMap<>());

            assertThat(map).isNotNull();

            map.put("a", 1);
            assertThat(map.get("a")).isEqualTo(1);
        }

        @Test
        @DisplayName("synchronizedSet - 同步 Set")
        void testSynchronizedSet() {
            Set<String> set = ConcurrentCollectionFactory.synchronizedSet(new HashSet<>());

            assertThat(set).isNotNull();

            set.add("a");
            assertThat(set.contains("a")).isTrue();
        }

        @Test
        @DisplayName("synchronizedList - 同步 List")
        void testSynchronizedList() {
            List<String> list = ConcurrentCollectionFactory.synchronizedList(new ArrayList<>());

            assertThat(list).isNotNull();

            list.add("a");
            assertThat(list.get(0)).isEqualTo("a");
        }
    }
}
