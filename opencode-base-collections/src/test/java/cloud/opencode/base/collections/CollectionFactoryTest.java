package cloud.opencode.base.collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;

/**
 * CollectionFactory 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("CollectionFactory 测试")
class CollectionFactoryTest {

    @Nested
    @DisplayName("List 工厂测试")
    class ListFactoryTests {

        @Test
        @DisplayName("newArrayList - 创建空 ArrayList")
        void testNewArrayList() {
            ArrayList<String> list = CollectionFactory.newArrayList();

            assertThat(list).isEmpty();
            assertThat(list).isInstanceOf(ArrayList.class);
        }

        @Test
        @DisplayName("newArrayList - 带初始元素")
        void testNewArrayListWithElements() {
            ArrayList<String> list = CollectionFactory.newArrayList("a", "b", "c");

            assertThat(list).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("newArrayList - 从 Iterable 创建")
        void testNewArrayListFromIterable() {
            Set<String> set = new LinkedHashSet<>(List.of("a", "b", "c"));
            ArrayList<String> list = CollectionFactory.newArrayList(set);

            assertThat(list).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("newArrayList - 从 Collection 创建")
        void testNewArrayListFromCollection() {
            List<String> source = List.of("a", "b", "c");
            ArrayList<String> list = CollectionFactory.newArrayList(source);

            assertThat(list).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("newArrayListWithCapacity - 指定容量")
        void testNewArrayListWithCapacity() {
            ArrayList<String> list = CollectionFactory.newArrayListWithCapacity(100);

            assertThat(list).isEmpty();
        }

        @Test
        @DisplayName("newLinkedList - 创建空 LinkedList")
        void testNewLinkedList() {
            LinkedList<String> list = CollectionFactory.newLinkedList();

            assertThat(list).isEmpty();
            assertThat(list).isInstanceOf(LinkedList.class);
        }

        @Test
        @DisplayName("newLinkedList - 从 Iterable 创建")
        void testNewLinkedListFromIterable() {
            LinkedList<String> list = CollectionFactory.newLinkedList(List.of("a", "b", "c"));

            assertThat(list).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("newCopyOnWriteArrayList - 创建")
        void testNewCopyOnWriteArrayList() {
            CopyOnWriteArrayList<String> list = CollectionFactory.newCopyOnWriteArrayList();

            assertThat(list).isEmpty();
            assertThat(list).isInstanceOf(CopyOnWriteArrayList.class);
        }
    }

    @Nested
    @DisplayName("Set 工厂测试")
    class SetFactoryTests {

        @Test
        @DisplayName("newHashSet - 创建空 HashSet")
        void testNewHashSet() {
            HashSet<String> set = CollectionFactory.newHashSet();

            assertThat(set).isEmpty();
            assertThat(set).isInstanceOf(HashSet.class);
        }

        @Test
        @DisplayName("newHashSet - 带初始元素")
        void testNewHashSetWithElements() {
            HashSet<String> set = CollectionFactory.newHashSet("a", "b", "c");

            assertThat(set).containsExactlyInAnyOrder("a", "b", "c");
        }

        @Test
        @DisplayName("newHashSet - 从 Iterable 创建")
        void testNewHashSetFromIterable() {
            HashSet<String> set = CollectionFactory.newHashSet(List.of("a", "b", "a"));

            assertThat(set).containsExactlyInAnyOrder("a", "b");
        }

        @Test
        @DisplayName("newLinkedHashSet - 创建空 LinkedHashSet")
        void testNewLinkedHashSet() {
            LinkedHashSet<String> set = CollectionFactory.newLinkedHashSet();

            assertThat(set).isEmpty();
            assertThat(set).isInstanceOf(LinkedHashSet.class);
        }

        @Test
        @DisplayName("newLinkedHashSet - 带初始元素")
        void testNewLinkedHashSetWithElements() {
            LinkedHashSet<String> set = CollectionFactory.newLinkedHashSet("c", "a", "b");

            assertThat(new ArrayList<>(set)).containsExactly("c", "a", "b");
        }

        @Test
        @DisplayName("newTreeSet - 创建空 TreeSet")
        void testNewTreeSet() {
            TreeSet<String> set = CollectionFactory.newTreeSet();

            assertThat(set).isEmpty();
            assertThat(set).isInstanceOf(TreeSet.class);
        }

        @Test
        @DisplayName("newTreeSet - 带比较器")
        void testNewTreeSetWithComparator() {
            TreeSet<String> set = CollectionFactory.newTreeSet(Comparator.reverseOrder());
            set.addAll(List.of("a", "b", "c"));

            assertThat(new ArrayList<>(set)).containsExactly("c", "b", "a");
        }

        @Test
        @DisplayName("newEnumSet - 创建 EnumSet")
        void testNewEnumSet() {
            EnumSet<Thread.State> set = CollectionFactory.newEnumSet(
                    Thread.State.NEW, Thread.State.RUNNABLE);

            assertThat(set).containsExactlyInAnyOrder(Thread.State.NEW, Thread.State.RUNNABLE);
        }

        @Test
        @DisplayName("newConcurrentHashSet - 创建并发 HashSet")
        void testNewConcurrentHashSet() {
            Set<String> set = CollectionFactory.newConcurrentHashSet();
            set.add("a");

            assertThat(set).contains("a");
        }

        @Test
        @DisplayName("newCopyOnWriteArraySet - 创建")
        void testNewCopyOnWriteArraySet() {
            CopyOnWriteArraySet<String> set = CollectionFactory.newCopyOnWriteArraySet();

            assertThat(set).isEmpty();
            assertThat(set).isInstanceOf(CopyOnWriteArraySet.class);
        }
    }

    @Nested
    @DisplayName("Map 工厂测试")
    class MapFactoryTests {

        @Test
        @DisplayName("newHashMap - 创建空 HashMap")
        void testNewHashMap() {
            HashMap<String, Integer> map = CollectionFactory.newHashMap();

            assertThat(map).isEmpty();
            assertThat(map).isInstanceOf(HashMap.class);
        }

        @Test
        @DisplayName("newHashMapWithCapacity - 指定容量")
        void testNewHashMapWithCapacity() {
            HashMap<String, Integer> map = CollectionFactory.newHashMapWithCapacity(100);

            assertThat(map).isEmpty();
        }

        @Test
        @DisplayName("newLinkedHashMap - 创建空 LinkedHashMap")
        void testNewLinkedHashMap() {
            LinkedHashMap<String, Integer> map = CollectionFactory.newLinkedHashMap();

            assertThat(map).isEmpty();
            assertThat(map).isInstanceOf(LinkedHashMap.class);
        }

        @Test
        @DisplayName("newTreeMap - 创建空 TreeMap")
        void testNewTreeMap() {
            TreeMap<String, Integer> map = CollectionFactory.newTreeMap();

            assertThat(map).isEmpty();
            assertThat(map).isInstanceOf(TreeMap.class);
        }

        @Test
        @DisplayName("newTreeMap - 带比较器")
        void testNewTreeMapWithComparator() {
            TreeMap<String, Integer> map = CollectionFactory.newTreeMap(Comparator.reverseOrder());
            map.put("a", 1);
            map.put("b", 2);
            map.put("c", 3);

            assertThat(new ArrayList<>(map.keySet())).containsExactly("c", "b", "a");
        }

        @Test
        @DisplayName("newEnumMap - 创建 EnumMap")
        void testNewEnumMap() {
            EnumMap<Thread.State, String> map = CollectionFactory.newEnumMap(Thread.State.class);
            map.put(Thread.State.NEW, "new");

            assertThat(map.get(Thread.State.NEW)).isEqualTo("new");
        }

        @Test
        @DisplayName("newConcurrentHashMap - 创建 ConcurrentHashMap")
        void testNewConcurrentHashMap() {
            ConcurrentHashMap<String, Integer> map = CollectionFactory.newConcurrentHashMap();

            assertThat(map).isEmpty();
            assertThat(map).isInstanceOf(ConcurrentHashMap.class);
        }

        @Test
        @DisplayName("newConcurrentSkipListMap - 创建")
        void testNewConcurrentSkipListMap() {
            ConcurrentSkipListMap<String, Integer> map = CollectionFactory.newConcurrentSkipListMap();

            assertThat(map).isEmpty();
            assertThat(map).isInstanceOf(ConcurrentSkipListMap.class);
        }

        @Test
        @DisplayName("newIdentityHashMap - 创建")
        void testNewIdentityHashMap() {
            IdentityHashMap<String, Integer> map = CollectionFactory.newIdentityHashMap();

            assertThat(map).isEmpty();
            assertThat(map).isInstanceOf(IdentityHashMap.class);
        }

        @Test
        @DisplayName("newWeakHashMap - 创建")
        void testNewWeakHashMap() {
            WeakHashMap<String, Integer> map = CollectionFactory.newWeakHashMap();

            assertThat(map).isEmpty();
            assertThat(map).isInstanceOf(WeakHashMap.class);
        }
    }

    @Nested
    @DisplayName("Queue 工厂测试")
    class QueueFactoryTests {

        @Test
        @DisplayName("newArrayDeque - 创建空 ArrayDeque")
        void testNewArrayDeque() {
            ArrayDeque<String> deque = CollectionFactory.newArrayDeque();

            assertThat(deque).isEmpty();
            assertThat(deque).isInstanceOf(ArrayDeque.class);
        }

        @Test
        @DisplayName("newPriorityQueue - 创建空 PriorityQueue")
        void testNewPriorityQueue() {
            PriorityQueue<Integer> queue = CollectionFactory.newPriorityQueue();

            assertThat(queue).isEmpty();
            assertThat(queue).isInstanceOf(PriorityQueue.class);
        }

        @Test
        @DisplayName("newPriorityQueue - 带比较器")
        void testNewPriorityQueueWithComparator() {
            PriorityQueue<Integer> queue = CollectionFactory.newPriorityQueue(Comparator.reverseOrder());
            queue.addAll(List.of(1, 3, 2));

            assertThat(queue.poll()).isEqualTo(3);
            assertThat(queue.poll()).isEqualTo(2);
            assertThat(queue.poll()).isEqualTo(1);
        }

        @Test
        @DisplayName("newLinkedBlockingQueue - 创建")
        void testNewLinkedBlockingQueue() {
            LinkedBlockingQueue<String> queue = CollectionFactory.newLinkedBlockingQueue();

            assertThat(queue).isEmpty();
            assertThat(queue).isInstanceOf(LinkedBlockingQueue.class);
        }

        @Test
        @DisplayName("newArrayBlockingQueue - 创建")
        void testNewArrayBlockingQueue() {
            ArrayBlockingQueue<String> queue = CollectionFactory.newArrayBlockingQueue(10);

            assertThat(queue).isEmpty();
            assertThat(queue).isInstanceOf(ArrayBlockingQueue.class);
            assertThat(queue.remainingCapacity()).isEqualTo(10);
        }

        @Test
        @DisplayName("newPriorityBlockingQueue - 创建")
        void testNewPriorityBlockingQueue() {
            PriorityBlockingQueue<Integer> queue = CollectionFactory.newPriorityBlockingQueue();

            assertThat(queue).isEmpty();
            assertThat(queue).isInstanceOf(PriorityBlockingQueue.class);
        }

        @Test
        @DisplayName("newConcurrentLinkedQueue - 创建")
        void testNewConcurrentLinkedQueue() {
            ConcurrentLinkedQueue<String> queue = CollectionFactory.newConcurrentLinkedQueue();

            assertThat(queue).isEmpty();
            assertThat(queue).isInstanceOf(ConcurrentLinkedQueue.class);
        }

        @Test
        @DisplayName("newConcurrentLinkedDeque - 创建")
        void testNewConcurrentLinkedDeque() {
            ConcurrentLinkedDeque<String> deque = CollectionFactory.newConcurrentLinkedDeque();

            assertThat(deque).isEmpty();
            assertThat(deque).isInstanceOf(ConcurrentLinkedDeque.class);
        }
    }
}
