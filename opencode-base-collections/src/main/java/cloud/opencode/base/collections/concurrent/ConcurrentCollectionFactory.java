package cloud.opencode.base.collections.concurrent;

import java.util.*;
import java.util.concurrent.*;

/**
 * ConcurrentCollectionFactory - Factory for Thread-safe Collections
 * ConcurrentCollectionFactory - 线程安全集合工厂
 *
 * <p>Provides factory methods for creating various thread-safe collections.</p>
 * <p>提供创建各种线程安全集合的工厂方法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Thread-safe collection creation - 线程安全集合创建</li>
 *   <li>Various concurrency levels - 各种并发级别</li>
 *   <li>Optimized implementations - 优化的实现</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create concurrent map - 创建并发映射
 * Map<String, Integer> map = ConcurrentCollectionFactory.newConcurrentMap();
 *
 * // Create concurrent set - 创建并发集合
 * Set<String> set = ConcurrentCollectionFactory.newConcurrentSet();
 *
 * // Create blocking queue - 创建阻塞队列
 * Queue<String> queue = ConcurrentCollectionFactory.newBlockingQueue(100);
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public final class ConcurrentCollectionFactory {

    private ConcurrentCollectionFactory() {
    }

    // ==================== Map 工厂方法 | Map Factory Methods ====================

    /**
     * Create a new ConcurrentHashMap.
     * 创建新 ConcurrentHashMap。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return new ConcurrentHashMap | 新 ConcurrentHashMap
     */
    public static <K, V> ConcurrentMap<K, V> newConcurrentMap() {
        return new ConcurrentHashMap<>();
    }

    /**
     * Create a new ConcurrentHashMap with initial capacity.
     * 创建指定初始容量的新 ConcurrentHashMap。
     *
     * @param <K>             key type | 键类型
     * @param <V>             value type | 值类型
     * @param initialCapacity initial capacity | 初始容量
     * @return new ConcurrentHashMap | 新 ConcurrentHashMap
     */
    public static <K, V> ConcurrentMap<K, V> newConcurrentMap(int initialCapacity) {
        return new ConcurrentHashMap<>(initialCapacity);
    }

    /**
     * Create a new ConcurrentSkipListMap.
     * 创建新 ConcurrentSkipListMap。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return new ConcurrentSkipListMap | 新 ConcurrentSkipListMap
     */
    public static <K, V> ConcurrentNavigableMap<K, V> newConcurrentSortedMap() {
        return new ConcurrentSkipListMap<>();
    }

    /**
     * Create a new ConcurrentSkipListMap with comparator.
     * 创建具有比较器的新 ConcurrentSkipListMap。
     *
     * @param <K>        key type | 键类型
     * @param <V>        value type | 值类型
     * @param comparator the comparator | 比较器
     * @return new ConcurrentSkipListMap | 新 ConcurrentSkipListMap
     */
    public static <K, V> ConcurrentNavigableMap<K, V> newConcurrentSortedMap(Comparator<? super K> comparator) {
        return new ConcurrentSkipListMap<>(comparator);
    }

    // ==================== Set 工厂方法 | Set Factory Methods ====================

    /**
     * Create a new concurrent set backed by ConcurrentHashMap.
     * 创建由 ConcurrentHashMap 支持的新并发集合。
     *
     * @param <E> element type | 元素类型
     * @return new concurrent set | 新并发集合
     */
    public static <E> Set<E> newConcurrentSet() {
        return ConcurrentHashMap.newKeySet();
    }

    /**
     * Create a new concurrent set with initial capacity.
     * 创建指定初始容量的新并发集合。
     *
     * @param <E>             element type | 元素类型
     * @param initialCapacity initial capacity | 初始容量
     * @return new concurrent set | 新并发集合
     */
    public static <E> Set<E> newConcurrentSet(int initialCapacity) {
        return ConcurrentHashMap.newKeySet(initialCapacity);
    }

    /**
     * Create a new ConcurrentSkipListSet.
     * 创建新 ConcurrentSkipListSet。
     *
     * @param <E> element type | 元素类型
     * @return new ConcurrentSkipListSet | 新 ConcurrentSkipListSet
     */
    public static <E> NavigableSet<E> newConcurrentSortedSet() {
        return new ConcurrentSkipListSet<>();
    }

    /**
     * Create a new ConcurrentSkipListSet with comparator.
     * 创建具有比较器的新 ConcurrentSkipListSet。
     *
     * @param <E>        element type | 元素类型
     * @param comparator the comparator | 比较器
     * @return new ConcurrentSkipListSet | 新 ConcurrentSkipListSet
     */
    public static <E> NavigableSet<E> newConcurrentSortedSet(Comparator<? super E> comparator) {
        return new ConcurrentSkipListSet<>(comparator);
    }

    // ==================== Queue 工厂方法 | Queue Factory Methods ====================

    /**
     * Create a new ConcurrentLinkedQueue.
     * 创建新 ConcurrentLinkedQueue。
     *
     * @param <E> element type | 元素类型
     * @return new ConcurrentLinkedQueue | 新 ConcurrentLinkedQueue
     */
    public static <E> Queue<E> newConcurrentQueue() {
        return new ConcurrentLinkedQueue<>();
    }

    /**
     * Create a new ConcurrentLinkedDeque.
     * 创建新 ConcurrentLinkedDeque。
     *
     * @param <E> element type | 元素类型
     * @return new ConcurrentLinkedDeque | 新 ConcurrentLinkedDeque
     */
    public static <E> Deque<E> newConcurrentDeque() {
        return new ConcurrentLinkedDeque<>();
    }

    /**
     * Create a new ArrayBlockingQueue.
     * 创建新 ArrayBlockingQueue。
     *
     * @param <E>      element type | 元素类型
     * @param capacity the capacity | 容量
     * @return new ArrayBlockingQueue | 新 ArrayBlockingQueue
     */
    public static <E> BlockingQueue<E> newBlockingQueue(int capacity) {
        return new ArrayBlockingQueue<>(capacity);
    }

    /**
     * Create a new LinkedBlockingQueue.
     * 创建新 LinkedBlockingQueue。
     *
     * @param <E> element type | 元素类型
     * @return new LinkedBlockingQueue | 新 LinkedBlockingQueue
     */
    public static <E> BlockingQueue<E> newLinkedBlockingQueue() {
        return new LinkedBlockingQueue<>();
    }

    /**
     * Create a new LinkedBlockingQueue with capacity.
     * 创建指定容量的新 LinkedBlockingQueue。
     *
     * @param <E>      element type | 元素类型
     * @param capacity the capacity | 容量
     * @return new LinkedBlockingQueue | 新 LinkedBlockingQueue
     */
    public static <E> BlockingQueue<E> newLinkedBlockingQueue(int capacity) {
        return new LinkedBlockingQueue<>(capacity);
    }

    /**
     * Create a new PriorityBlockingQueue.
     * 创建新 PriorityBlockingQueue。
     *
     * @param <E> element type | 元素类型
     * @return new PriorityBlockingQueue | 新 PriorityBlockingQueue
     */
    public static <E> BlockingQueue<E> newPriorityBlockingQueue() {
        return new PriorityBlockingQueue<>();
    }

    /**
     * Create a new DelayQueue.
     * 创建新 DelayQueue。
     *
     * @param <E> element type | 元素类型
     * @return new DelayQueue | 新 DelayQueue
     */
    public static <E extends Delayed> BlockingQueue<E> newDelayQueue() {
        return new DelayQueue<>();
    }

    /**
     * Create a new SynchronousQueue.
     * 创建新 SynchronousQueue。
     *
     * @param <E> element type | 元素类型
     * @return new SynchronousQueue | 新 SynchronousQueue
     */
    public static <E> BlockingQueue<E> newSynchronousQueue() {
        return new SynchronousQueue<>();
    }

    /**
     * Create a new LinkedBlockingDeque.
     * 创建新 LinkedBlockingDeque。
     *
     * @param <E> element type | 元素类型
     * @return new LinkedBlockingDeque | 新 LinkedBlockingDeque
     */
    public static <E> BlockingDeque<E> newBlockingDeque() {
        return new LinkedBlockingDeque<>();
    }

    /**
     * Create a new LinkedBlockingDeque with capacity.
     * 创建指定容量的新 LinkedBlockingDeque。
     *
     * @param <E>      element type | 元素类型
     * @param capacity the capacity | 容量
     * @return new LinkedBlockingDeque | 新 LinkedBlockingDeque
     */
    public static <E> BlockingDeque<E> newBlockingDeque(int capacity) {
        return new LinkedBlockingDeque<>(capacity);
    }

    // ==================== Stack 工厂方法 | Stack Factory Methods ====================

    /**
     * Creates a new lock-free concurrent stack.
     * 创建新的无锁并发栈。
     *
     * @param <E> element type | 元素类型
     * @return new LockFreeStack | 新 LockFreeStack
     */
    public static <E> LockFreeStack<E> newLockFreeStack() {
        return new LockFreeStack<>();
    }

    // ==================== List 工厂方法 | List Factory Methods ====================

    /**
     * Create a new CopyOnWriteArrayList.
     * 创建新 CopyOnWriteArrayList。
     *
     * @param <E> element type | 元素类型
     * @return new CopyOnWriteArrayList | 新 CopyOnWriteArrayList
     */
    public static <E> List<E> newCopyOnWriteList() {
        return new CopyOnWriteArrayList<>();
    }

    /**
     * Create a new CopyOnWriteArrayList from collection.
     * 从集合创建新 CopyOnWriteArrayList。
     *
     * @param <E>        element type | 元素类型
     * @param collection the collection | 集合
     * @return new CopyOnWriteArrayList | 新 CopyOnWriteArrayList
     */
    public static <E> List<E> newCopyOnWriteList(Collection<? extends E> collection) {
        return new CopyOnWriteArrayList<>(collection);
    }

    /**
     * Create a new CopyOnWriteArraySet.
     * 创建新 CopyOnWriteArraySet。
     *
     * @param <E> element type | 元素类型
     * @return new CopyOnWriteArraySet | 新 CopyOnWriteArraySet
     */
    public static <E> Set<E> newCopyOnWriteSet() {
        return new CopyOnWriteArraySet<>();
    }

    // ==================== 包装方法 | Wrapper Methods ====================

    /**
     * Create a synchronized map from any map.
     * 从任何映射创建同步映射。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @param map the map | 映射
     * @return synchronized map | 同步映射
     */
    public static <K, V> Map<K, V> synchronizedMap(Map<K, V> map) {
        return Collections.synchronizedMap(map);
    }

    /**
     * Create a synchronized set from any set.
     * 从任何集合创建同步集合。
     *
     * @param <E> element type | 元素类型
     * @param set the set | 集合
     * @return synchronized set | 同步集合
     */
    public static <E> Set<E> synchronizedSet(Set<E> set) {
        return Collections.synchronizedSet(set);
    }

    /**
     * Create a synchronized list from any list.
     * 从任何列表创建同步列表。
     *
     * @param <E>  element type | 元素类型
     * @param list the list | 列表
     * @return synchronized list | 同步列表
     */
    public static <E> List<E> synchronizedList(List<E> list) {
        return Collections.synchronizedList(list);
    }
}
