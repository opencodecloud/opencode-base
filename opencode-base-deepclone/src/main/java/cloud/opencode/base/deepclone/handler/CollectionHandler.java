package cloud.opencode.base.deepclone.handler;

import cloud.opencode.base.deepclone.CloneContext;
import cloud.opencode.base.deepclone.Cloner;
import cloud.opencode.base.deepclone.exception.OpenDeepCloneException;

import java.util.*;
import java.util.concurrent.*;

/**
 * Handler for cloning Collection types
 * 集合类型克隆处理器
 *
 * <p>Handles all Collection types including List, Set, Queue, and their implementations.
 * Elements are deep cloned recursively.</p>
 * <p>处理所有Collection类型，包括List、Set、Queue及其实现。元素会被递归深度克隆。</p>
 *
 * <p><strong>Supported Types | 支持的类型:</strong></p>
 * <ul>
 *   <li>ArrayList, LinkedList, Vector, Stack</li>
 *   <li>HashSet, LinkedHashSet, TreeSet</li>
 *   <li>ArrayDeque, PriorityQueue</li>
 *   <li>ConcurrentLinkedQueue, CopyOnWriteArrayList, etc.</li>
 * </ul>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Deep clone all Collection types - 深度克隆所有Collection类型</li>
 *   <li>Preserves original collection type - 保留原始集合类型</li>
 *   <li>Recursive element cloning - 递归元素克隆</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CollectionHandler handler = new CollectionHandler();
 * List<User> cloned = (List<User>) handler.clone(originalList, cloner, context);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
public final class CollectionHandler implements TypeHandler<Collection<?>> {

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Collection<?> clone(Collection<?> original, Cloner cloner, CloneContext context) {
        if (original == null) {
            return null;
        }

        Collection clone = createInstance(original.getClass(), original.size());
        context.registerCloned(original, clone);

        for (Object element : original) {
            clone.add(cloner.clone(element, context));
        }

        return clone;
    }

    /**
     * Clones a List
     * 克隆List
     *
     * @param list    the original list | 原始列表
     * @param cloner  the cloner | 克隆器
     * @param context the context | 上下文
     * @param <T>     the element type | 元素类型
     * @return the cloned list | 克隆的列表
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> cloneList(List<T> list, Cloner cloner, CloneContext context) {
        return (List<T>) clone(list, cloner, context);
    }

    /**
     * Clones a Set
     * 克隆Set
     *
     * @param set     the original set | 原始集合
     * @param cloner  the cloner | 克隆器
     * @param context the context | 上下文
     * @param <T>     the element type | 元素类型
     * @return the cloned set | 克隆的集合
     */
    @SuppressWarnings("unchecked")
    public <T> Set<T> cloneSet(Set<T> set, Cloner cloner, CloneContext context) {
        return (Set<T>) clone(set, cloner, context);
    }

    /**
     * Clones a Queue
     * 克隆Queue
     *
     * @param queue   the original queue | 原始队列
     * @param cloner  the cloner | 克隆器
     * @param context the context | 上下文
     * @param <T>     the element type | 元素类型
     * @return the cloned queue | 克隆的队列
     */
    @SuppressWarnings("unchecked")
    public <T> Queue<T> cloneQueue(Queue<T> queue, Cloner cloner, CloneContext context) {
        return (Queue<T>) clone(queue, cloner, context);
    }

    /**
     * Creates an instance of the specified collection type
     * 创建指定集合类型的实例
     *
     * @param type the collection type | 集合类型
     * @param size the expected size | 预期大小
     * @param <T>  the element type | 元素类型
     * @return the new collection instance | 新的集合实例
     */
    @SuppressWarnings("unchecked")
    public <T> Collection<T> createInstance(Class<?> type, int size) {
        // List implementations
        if (ArrayList.class.isAssignableFrom(type)) {
            return new ArrayList<>(size);
        }
        if (LinkedList.class.isAssignableFrom(type)) {
            return new LinkedList<>();
        }
        if (Vector.class.isAssignableFrom(type)) {
            return new Vector<>(size);
        }
        if (Stack.class.isAssignableFrom(type)) {
            return new Stack<>();
        }
        if (CopyOnWriteArrayList.class.isAssignableFrom(type)) {
            return new CopyOnWriteArrayList<>();
        }

        // Set implementations
        if (LinkedHashSet.class.isAssignableFrom(type)) {
            return new LinkedHashSet<>(size);
        }
        if (TreeSet.class.isAssignableFrom(type)) {
            return new TreeSet<>();
        }
        if (HashSet.class.isAssignableFrom(type)) {
            return new HashSet<>(size);
        }
        if (CopyOnWriteArraySet.class.isAssignableFrom(type)) {
            return new CopyOnWriteArraySet<>();
        }
        if (ConcurrentSkipListSet.class.isAssignableFrom(type)) {
            return new ConcurrentSkipListSet<>();
        }

        // Queue implementations
        if (ArrayDeque.class.isAssignableFrom(type)) {
            return new ArrayDeque<>(size);
        }
        if (PriorityQueue.class.isAssignableFrom(type)) {
            return new PriorityQueue<>(size > 0 ? size : 11);
        }
        if (ConcurrentLinkedQueue.class.isAssignableFrom(type)) {
            return new ConcurrentLinkedQueue<>();
        }
        if (LinkedBlockingQueue.class.isAssignableFrom(type)) {
            return new LinkedBlockingQueue<>();
        }
        if (ArrayBlockingQueue.class.isAssignableFrom(type)) {
            return new ArrayBlockingQueue<>(size > 0 ? size : 16);
        }
        if (PriorityBlockingQueue.class.isAssignableFrom(type)) {
            return new PriorityBlockingQueue<>();
        }

        // Default to ArrayList for List, HashSet for Set, ArrayDeque for Queue
        if (List.class.isAssignableFrom(type)) {
            return new ArrayList<>(size);
        }
        if (Set.class.isAssignableFrom(type)) {
            return new HashSet<>(size);
        }
        if (Queue.class.isAssignableFrom(type)) {
            return new ArrayDeque<>(size);
        }

        throw OpenDeepCloneException.unsupportedType(type);
    }

    @Override
    public boolean supports(Class<?> type) {
        return type != null && Collection.class.isAssignableFrom(type);
    }

    @Override
    public int priority() {
        return 20;
    }
}
