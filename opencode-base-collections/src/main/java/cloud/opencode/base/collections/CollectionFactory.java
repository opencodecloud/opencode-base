package cloud.opencode.base.collections;

import java.util.*;
import java.util.concurrent.*;

/**
 * CollectionFactory - Factory for Creating Collections
 * CollectionFactory - 创建集合的工厂
 *
 * <p>Provides factory methods for creating various collection types
 * with consistent API across different implementations.</p>
 * <p>提供创建各种集合类型的工厂方法，跨不同实现提供一致的 API。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>List factories - 列表工厂</li>
 *   <li>Set factories - 集合工厂</li>
 *   <li>Map factories - 映射工厂</li>
 *   <li>Queue and Deque factories - 队列和双端队列工厂</li>
 *   <li>Concurrent collection factories - 并发集合工厂</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create lists - 创建列表
 * List<String> arrayList = CollectionFactory.newArrayList();
 * List<String> linkedList = CollectionFactory.newLinkedList();
 *
 * // Create sets - 创建集合
 * Set<String> hashSet = CollectionFactory.newHashSet();
 * Set<String> linkedHashSet = CollectionFactory.newLinkedHashSet();
 * Set<String> treeSet = CollectionFactory.newTreeSet();
 *
 * // Create maps - 创建映射
 * Map<String, Integer> hashMap = CollectionFactory.newHashMap();
 * Map<String, Integer> linkedHashMap = CollectionFactory.newLinkedHashMap();
 * Map<String, Integer> treeMap = CollectionFactory.newTreeMap();
 *
 * // Create concurrent collections - 创建并发集合
 * Map<String, Integer> concurrentMap = CollectionFactory.newConcurrentHashMap();
 * Set<String> concurrentSet = CollectionFactory.newConcurrentHashSet();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless factory) - 线程安全: 是（无状态工厂）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public final class CollectionFactory {

    private CollectionFactory() {
    }

    // ==================== List 工厂 | List Factories ====================

    /**
     * Create an ArrayList.
     * 创建 ArrayList。
     *
     * @param <E> element type | 元素类型
     * @return new ArrayList | 新的 ArrayList
     */
    public static <E> ArrayList<E> newArrayList() {
        return new ArrayList<>();
    }

    /**
     * Create an ArrayList with initial elements.
     * 创建带初始元素的 ArrayList。
     *
     * @param <E>      element type | 元素类型
     * @param elements initial elements | 初始元素
     * @return new ArrayList | 新的 ArrayList
     */
    @SafeVarargs
    public static <E> ArrayList<E> newArrayList(E... elements) {
        ArrayList<E> list = new ArrayList<>(elements.length);
        Collections.addAll(list, elements);
        return list;
    }

    /**
     * Create an ArrayList from an iterable.
     * 从可迭代对象创建 ArrayList。
     *
     * @param <E>      element type | 元素类型
     * @param elements elements | 元素
     * @return new ArrayList | 新的 ArrayList
     */
    public static <E> ArrayList<E> newArrayList(Iterable<? extends E> elements) {
        if (elements instanceof Collection<? extends E> collection) {
            return new ArrayList<>(collection);
        }
        ArrayList<E> list = new ArrayList<>();
        for (E e : elements) {
            list.add(e);
        }
        return list;
    }

    /**
     * Create an ArrayList with initial capacity.
     * 创建指定容量的 ArrayList。
     *
     * @param <E>             element type | 元素类型
     * @param initialCapacity initial capacity | 初始容量
     * @return new ArrayList | 新的 ArrayList
     */
    public static <E> ArrayList<E> newArrayListWithCapacity(int initialCapacity) {
        return new ArrayList<>(initialCapacity);
    }

    /**
     * Create a LinkedList.
     * 创建 LinkedList。
     *
     * @param <E> element type | 元素类型
     * @return new LinkedList | 新的 LinkedList
     */
    public static <E> LinkedList<E> newLinkedList() {
        return new LinkedList<>();
    }

    /**
     * Create a LinkedList from an iterable.
     * 从可迭代对象创建 LinkedList。
     *
     * @param <E>      element type | 元素类型
     * @param elements elements | 元素
     * @return new LinkedList | 新的 LinkedList
     */
    public static <E> LinkedList<E> newLinkedList(Iterable<? extends E> elements) {
        LinkedList<E> list = new LinkedList<>();
        for (E e : elements) {
            list.add(e);
        }
        return list;
    }

    /**
     * Create a CopyOnWriteArrayList.
     * 创建 CopyOnWriteArrayList。
     *
     * @param <E> element type | 元素类型
     * @return new CopyOnWriteArrayList | 新的 CopyOnWriteArrayList
     */
    public static <E> CopyOnWriteArrayList<E> newCopyOnWriteArrayList() {
        return new CopyOnWriteArrayList<>();
    }

    // ==================== Set 工厂 | Set Factories ====================

    /**
     * Create a HashSet.
     * 创建 HashSet。
     *
     * @param <E> element type | 元素类型
     * @return new HashSet | 新的 HashSet
     */
    public static <E> HashSet<E> newHashSet() {
        return new HashSet<>();
    }

    /**
     * Create a HashSet with initial elements.
     * 创建带初始元素的 HashSet。
     *
     * @param <E>      element type | 元素类型
     * @param elements initial elements | 初始元素
     * @return new HashSet | 新的 HashSet
     */
    @SafeVarargs
    public static <E> HashSet<E> newHashSet(E... elements) {
        HashSet<E> set = new HashSet<>(elements.length);
        Collections.addAll(set, elements);
        return set;
    }

    /**
     * Create a HashSet from an iterable.
     * 从可迭代对象创建 HashSet。
     *
     * @param <E>      element type | 元素类型
     * @param elements elements | 元素
     * @return new HashSet | 新的 HashSet
     */
    public static <E> HashSet<E> newHashSet(Iterable<? extends E> elements) {
        HashSet<E> set = new HashSet<>();
        for (E e : elements) {
            set.add(e);
        }
        return set;
    }

    /**
     * Create a LinkedHashSet.
     * 创建 LinkedHashSet。
     *
     * @param <E> element type | 元素类型
     * @return new LinkedHashSet | 新的 LinkedHashSet
     */
    public static <E> LinkedHashSet<E> newLinkedHashSet() {
        return new LinkedHashSet<>();
    }

    /**
     * Create a LinkedHashSet with initial elements.
     * 创建带初始元素的 LinkedHashSet。
     *
     * @param <E>      element type | 元素类型
     * @param elements initial elements | 初始元素
     * @return new LinkedHashSet | 新的 LinkedHashSet
     */
    @SafeVarargs
    public static <E> LinkedHashSet<E> newLinkedHashSet(E... elements) {
        LinkedHashSet<E> set = new LinkedHashSet<>(elements.length);
        Collections.addAll(set, elements);
        return set;
    }

    /**
     * Create a TreeSet.
     * 创建 TreeSet。
     *
     * @param <E> element type | 元素类型
     * @return new TreeSet | 新的 TreeSet
     */
    public static <E extends Comparable<? super E>> TreeSet<E> newTreeSet() {
        return new TreeSet<>();
    }

    /**
     * Create a TreeSet with a comparator.
     * 创建带比较器的 TreeSet。
     *
     * @param <E>        element type | 元素类型
     * @param comparator the comparator | 比较器
     * @return new TreeSet | 新的 TreeSet
     */
    public static <E> TreeSet<E> newTreeSet(Comparator<? super E> comparator) {
        return new TreeSet<>(comparator);
    }

    /**
     * Create an EnumSet from elements.
     * 从元素创建 EnumSet。
     *
     * @param <E>      enum type | 枚举类型
     * @param first    first element | 第一个元素
     * @param elements additional elements | 附加元素
     * @return new EnumSet | 新的 EnumSet
     */
    @SafeVarargs
    public static <E extends Enum<E>> EnumSet<E> newEnumSet(E first, E... elements) {
        return EnumSet.of(first, elements);
    }

    /**
     * Create a concurrent hash set.
     * 创建并发哈希集合。
     *
     * @param <E> element type | 元素类型
     * @return new concurrent set | 新的并发集合
     */
    public static <E> Set<E> newConcurrentHashSet() {
        return ConcurrentHashMap.newKeySet();
    }

    /**
     * Create a CopyOnWriteArraySet.
     * 创建 CopyOnWriteArraySet。
     *
     * @param <E> element type | 元素类型
     * @return new CopyOnWriteArraySet | 新的 CopyOnWriteArraySet
     */
    public static <E> CopyOnWriteArraySet<E> newCopyOnWriteArraySet() {
        return new CopyOnWriteArraySet<>();
    }

    // ==================== Map 工厂 | Map Factories ====================

    /**
     * Create a HashMap.
     * 创建 HashMap。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return new HashMap | 新的 HashMap
     */
    public static <K, V> HashMap<K, V> newHashMap() {
        return new HashMap<>();
    }

    /**
     * Create a HashMap with initial capacity.
     * 创建指定容量的 HashMap。
     *
     * @param <K>             key type | 键类型
     * @param <V>             value type | 值类型
     * @param initialCapacity initial capacity | 初始容量
     * @return new HashMap | 新的 HashMap
     */
    public static <K, V> HashMap<K, V> newHashMapWithCapacity(int initialCapacity) {
        return new HashMap<>(initialCapacity);
    }

    /**
     * Create a LinkedHashMap.
     * 创建 LinkedHashMap。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return new LinkedHashMap | 新的 LinkedHashMap
     */
    public static <K, V> LinkedHashMap<K, V> newLinkedHashMap() {
        return new LinkedHashMap<>();
    }

    /**
     * Create a TreeMap.
     * 创建 TreeMap。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return new TreeMap | 新的 TreeMap
     */
    public static <K extends Comparable<? super K>, V> TreeMap<K, V> newTreeMap() {
        return new TreeMap<>();
    }

    /**
     * Create a TreeMap with a comparator.
     * 创建带比较器的 TreeMap。
     *
     * @param <K>        key type | 键类型
     * @param <V>        value type | 值类型
     * @param comparator the comparator | 比较器
     * @return new TreeMap | 新的 TreeMap
     */
    public static <K, V> TreeMap<K, V> newTreeMap(Comparator<? super K> comparator) {
        return new TreeMap<>(comparator);
    }

    /**
     * Create an EnumMap.
     * 创建 EnumMap。
     *
     * @param <K>     key type | 键类型
     * @param <V>     value type | 值类型
     * @param keyType key enum type | 键枚举类型
     * @return new EnumMap | 新的 EnumMap
     */
    public static <K extends Enum<K>, V> EnumMap<K, V> newEnumMap(Class<K> keyType) {
        return new EnumMap<>(keyType);
    }

    /**
     * Create a ConcurrentHashMap.
     * 创建 ConcurrentHashMap。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return new ConcurrentHashMap | 新的 ConcurrentHashMap
     */
    public static <K, V> ConcurrentHashMap<K, V> newConcurrentHashMap() {
        return new ConcurrentHashMap<>();
    }

    /**
     * Create a ConcurrentSkipListMap.
     * 创建 ConcurrentSkipListMap。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return new ConcurrentSkipListMap | 新的 ConcurrentSkipListMap
     */
    public static <K extends Comparable<? super K>, V> ConcurrentSkipListMap<K, V> newConcurrentSkipListMap() {
        return new ConcurrentSkipListMap<>();
    }

    /**
     * Create an IdentityHashMap.
     * 创建 IdentityHashMap。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return new IdentityHashMap | 新的 IdentityHashMap
     */
    public static <K, V> IdentityHashMap<K, V> newIdentityHashMap() {
        return new IdentityHashMap<>();
    }

    /**
     * Create a WeakHashMap.
     * 创建 WeakHashMap。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return new WeakHashMap | 新的 WeakHashMap
     */
    public static <K, V> WeakHashMap<K, V> newWeakHashMap() {
        return new WeakHashMap<>();
    }

    // ==================== Queue 工厂 | Queue Factories ====================

    /**
     * Create an ArrayDeque.
     * 创建 ArrayDeque。
     *
     * @param <E> element type | 元素类型
     * @return new ArrayDeque | 新的 ArrayDeque
     */
    public static <E> ArrayDeque<E> newArrayDeque() {
        return new ArrayDeque<>();
    }

    /**
     * Create a PriorityQueue.
     * 创建 PriorityQueue。
     *
     * @param <E> element type | 元素类型
     * @return new PriorityQueue | 新的 PriorityQueue
     */
    public static <E extends Comparable<? super E>> PriorityQueue<E> newPriorityQueue() {
        return new PriorityQueue<>();
    }

    /**
     * Create a PriorityQueue with a comparator.
     * 创建带比较器的 PriorityQueue。
     *
     * @param <E>        element type | 元素类型
     * @param comparator the comparator | 比较器
     * @return new PriorityQueue | 新的 PriorityQueue
     */
    public static <E> PriorityQueue<E> newPriorityQueue(Comparator<? super E> comparator) {
        return new PriorityQueue<>(comparator);
    }

    /**
     * Create a LinkedBlockingQueue.
     * 创建 LinkedBlockingQueue。
     *
     * @param <E> element type | 元素类型
     * @return new LinkedBlockingQueue | 新的 LinkedBlockingQueue
     */
    public static <E> LinkedBlockingQueue<E> newLinkedBlockingQueue() {
        return new LinkedBlockingQueue<>();
    }

    /**
     * Create an ArrayBlockingQueue.
     * 创建 ArrayBlockingQueue。
     *
     * @param <E>      element type | 元素类型
     * @param capacity the capacity | 容量
     * @return new ArrayBlockingQueue | 新的 ArrayBlockingQueue
     */
    public static <E> ArrayBlockingQueue<E> newArrayBlockingQueue(int capacity) {
        return new ArrayBlockingQueue<>(capacity);
    }

    /**
     * Create a PriorityBlockingQueue.
     * 创建 PriorityBlockingQueue。
     *
     * @param <E> element type | 元素类型
     * @return new PriorityBlockingQueue | 新的 PriorityBlockingQueue
     */
    public static <E extends Comparable<? super E>> PriorityBlockingQueue<E> newPriorityBlockingQueue() {
        return new PriorityBlockingQueue<>();
    }

    /**
     * Create a ConcurrentLinkedQueue.
     * 创建 ConcurrentLinkedQueue。
     *
     * @param <E> element type | 元素类型
     * @return new ConcurrentLinkedQueue | 新的 ConcurrentLinkedQueue
     */
    public static <E> ConcurrentLinkedQueue<E> newConcurrentLinkedQueue() {
        return new ConcurrentLinkedQueue<>();
    }

    /**
     * Create a ConcurrentLinkedDeque.
     * 创建 ConcurrentLinkedDeque。
     *
     * @param <E> element type | 元素类型
     * @return new ConcurrentLinkedDeque | 新的 ConcurrentLinkedDeque
     */
    public static <E> ConcurrentLinkedDeque<E> newConcurrentLinkedDeque() {
        return new ConcurrentLinkedDeque<>();
    }
}
