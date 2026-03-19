package cloud.opencode.base.collections;

import cloud.opencode.base.collections.exception.OpenCollectionException;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * OpenCollection - General Collection Utility Class
 * OpenCollection - 通用集合工具类
 *
 * <p>Provides comprehensive collection operations including null-safe checks, set operations,
 * filtering, transformations, and batch operations.</p>
 * <p>提供全面的集合操作，包括空值安全检查、集合运算、过滤、转换和批量操作。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Null-safe operations - 空值安全操作</li>
 *   <li>Set algebra (union, intersection, difference) - 集合代数（并集、交集、差集）</li>
 *   <li>Element statistics and counting - 元素统计和计数</li>
 *   <li>Filtering and transformation - 过滤和转换</li>
 *   <li>Batch operations - 批量操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Check if empty - 检查是否为空
 * boolean empty = OpenCollection.isEmpty(list);
 *
 * // Union of collections - 集合并集
 * Collection<String> union = OpenCollection.union(list1, list2);
 *
 * // Filter elements - 过滤元素
 * Collection<String> filtered = OpenCollection.select(list, s -> s.startsWith("A"));
 *
 * // Count matches - 统计匹配数
 * int count = OpenCollection.countMatches(list, s -> s.length() > 5);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Most operations: O(n) - 大多数操作: O(n)</li>
 *   <li>Set operations: O(n + m) - 集合运算: O(n + m)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (operations on mutable collections are not synchronized) - 线程安全: 否</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public final class OpenCollection {

    private OpenCollection() {
    }

    // ==================== 空值安全检查 | Null-safe Checks ====================

    /**
     * Check if collection is empty (null or empty)
     * 判断集合是否为空（null 或 empty）
     *
     * @param coll the collection to check | 要检查的集合
     * @return true if null or empty | 如果为 null 或空则返回 true
     */
    public static boolean isEmpty(Collection<?> coll) {
        return coll == null || coll.isEmpty();
    }

    /**
     * Check if collection is not empty
     * 判断集合是否非空
     *
     * @param coll the collection to check | 要检查的集合
     * @return true if not null and not empty | 如果非 null 且非空则返回 true
     */
    public static boolean isNotEmpty(Collection<?> coll) {
        return !isEmpty(coll);
    }

    /**
     * Check if map is empty (null or empty)
     * 判断 Map 是否为空
     *
     * @param map the map to check | 要检查的 Map
     * @return true if null or empty | 如果为 null 或空则返回 true
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * Check if map is not empty
     * 判断 Map 是否非空
     *
     * @param map the map to check | 要检查的 Map
     * @return true if not null and not empty | 如果非 null 且非空则返回 true
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    /**
     * Check if iterable is empty
     * 判断 Iterable 是否为空
     *
     * @param iterable the iterable to check | 要检查的 Iterable
     * @return true if null or empty | 如果为 null 或空则返回 true
     */
    public static boolean isEmpty(Iterable<?> iterable) {
        if (iterable == null) {
            return true;
        }
        if (iterable instanceof Collection<?> coll) {
            return coll.isEmpty();
        }
        return !iterable.iterator().hasNext();
    }

    // ==================== 集合运算 | Set Operations ====================

    /**
     * Returns the union of two iterables
     * 返回两个可迭代对象的并集
     *
     * @param <E> element type | 元素类型
     * @param a   first iterable | 第一个可迭代对象
     * @param b   second iterable | 第二个可迭代对象
     * @return union collection | 并集
     */
    public static <E> Collection<E> union(Iterable<? extends E> a, Iterable<? extends E> b) {
        List<E> result = new ArrayList<>();
        if (a != null) {
            for (E e : a) {
                result.add(e);
            }
        }
        if (b != null) {
            for (E e : b) {
                result.add(e);
            }
        }
        return result;
    }

    /**
     * Returns the intersection of two iterables
     * 返回两个可迭代对象的交集
     *
     * @param <E> element type | 元素类型
     * @param a   first iterable | 第一个可迭代对象
     * @param b   second iterable | 第二个可迭代对象
     * @return intersection collection | 交集
     */
    public static <E> Collection<E> intersection(Iterable<? extends E> a, Iterable<? extends E> b) {
        List<E> result = new ArrayList<>();
        if (a == null || b == null) {
            return result;
        }
        Map<E, Integer> cardB = getCardinalityMap(b);
        for (E e : a) {
            Integer count = cardB.get(e);
            if (count != null && count > 0) {
                result.add(e);
                cardB.put(e, count - 1);
            }
        }
        return result;
    }

    /**
     * Returns the difference of two iterables (a - b)
     * 返回两个可迭代对象的差集 (a - b)
     *
     * @param <E> element type | 元素类型
     * @param a   first iterable | 第一个可迭代对象
     * @param b   second iterable | 第二个可迭代对象
     * @return difference collection | 差集
     */
    public static <E> Collection<E> subtract(Iterable<? extends E> a, Iterable<? extends E> b) {
        List<E> result = new ArrayList<>();
        if (a == null) {
            return result;
        }
        if (b == null) {
            for (E e : a) {
                result.add(e);
            }
            return result;
        }
        Map<E, Integer> cardB = getCardinalityMap(b);
        for (E e : a) {
            Integer count = cardB.get(e);
            if (count == null || count == 0) {
                result.add(e);
            } else {
                cardB.put(e, count - 1);
            }
        }
        return result;
    }

    /**
     * Returns the symmetric difference of two iterables (a XOR b)
     * 返回两个可迭代对象的对称差集 (a XOR b)
     *
     * @param <E> element type | 元素类型
     * @param a   first iterable | 第一个可迭代对象
     * @param b   second iterable | 第二个可迭代对象
     * @return symmetric difference collection | 对称差集
     */
    public static <E> Collection<E> disjunction(Iterable<? extends E> a, Iterable<? extends E> b) {
        List<E> result = new ArrayList<>();
        if (a == null && b == null) {
            return result;
        }
        if (a == null) {
            for (E e : b) {
                result.add(e);
            }
            return result;
        }
        if (b == null) {
            for (E e : a) {
                result.add(e);
            }
            return result;
        }
        Map<E, Integer> cardA = getCardinalityMap(a);
        Map<E, Integer> cardB = getCardinalityMap(b);
        Set<E> allKeys = new HashSet<>(cardA.keySet());
        allKeys.addAll(cardB.keySet());

        for (E key : allKeys) {
            int countA = cardA.getOrDefault(key, 0);
            int countB = cardB.getOrDefault(key, 0);
            int diff = Math.abs(countA - countB);
            for (int i = 0; i < diff; i++) {
                result.add(key);
            }
        }
        return result;
    }

    // ==================== 元素检查 | Element Checks ====================

    /**
     * Check if collection contains any of the given elements
     * 检查是否包含任一元素
     *
     * @param coll     the collection to check | 要检查的集合
     * @param elements the elements to look for | 要查找的元素
     * @return true if any element is found | 如果找到任一元素则返回 true
     */
    @SafeVarargs
    public static boolean containsAny(Collection<?> coll, Object... elements) {
        if (isEmpty(coll) || elements == null || elements.length == 0) {
            return false;
        }
        for (Object element : elements) {
            if (coll.contains(element)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if collection contains all the given elements
     * 检查是否包含所有元素
     *
     * @param coll     the collection to check | 要检查的集合
     * @param elements the elements to look for | 要查找的元素集合
     * @return true if all elements are found | 如果找到所有元素则返回 true
     */
    public static boolean containsAll(Collection<?> coll, Collection<?> elements) {
        if (isEmpty(coll)) {
            return isEmpty(elements);
        }
        if (isEmpty(elements)) {
            return true;
        }
        return coll.containsAll(elements);
    }

    /**
     * Compare two collections for equality (ignoring order)
     * 比较两个集合是否相等（忽略顺序）
     *
     * @param a first collection | 第一个集合
     * @param b second collection | 第二个集合
     * @return true if equal | 如果相等则返回 true
     */
    public static boolean isEqualCollection(Collection<?> a, Collection<?> b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        if (a.size() != b.size()) {
            return false;
        }
        Map<Object, Integer> cardA = getCardinalityMap(a);
        Map<Object, Integer> cardB = getCardinalityMap(b);
        return cardA.equals(cardB);
    }

    /**
     * Check if sub is a subset of sup
     * 检查是否为子集
     *
     * @param sub potential subset | 潜在子集
     * @param sup potential superset | 潜在超集
     * @return true if sub is a subset of sup | 如果 sub 是 sup 的子集则返回 true
     */
    public static boolean isSubCollection(Collection<?> sub, Collection<?> sup) {
        if (isEmpty(sub)) {
            return true;
        }
        if (isEmpty(sup)) {
            return false;
        }
        Map<Object, Integer> cardSup = getCardinalityMap(sup);
        for (Object e : sub) {
            Integer count = cardSup.get(e);
            if (count == null || count == 0) {
                return false;
            }
            cardSup.put(e, count - 1);
        }
        return true;
    }

    /**
     * Check if sub is a proper subset of sup
     * 检查是否为真子集
     *
     * @param sub potential proper subset | 潜在真子集
     * @param sup potential superset | 潜在超集
     * @return true if sub is a proper subset of sup | 如果 sub 是 sup 的真子集则返回 true
     */
    public static boolean isProperSubCollection(Collection<?> sub, Collection<?> sup) {
        return isSubCollection(sub, sup) &&
                (isEmpty(sub) ? isNotEmpty(sup) : sub.size() < sup.size());
    }

    // ==================== 元素统计 | Element Statistics ====================

    /**
     * Get the cardinality map of an iterable
     * 获取元素基数映射
     *
     * @param <E>  element type | 元素类型
     * @param coll the iterable | 可迭代对象
     * @return cardinality map | 基数映射
     */
    @SuppressWarnings("unchecked")
    public static <E> Map<E, Integer> getCardinalityMap(Iterable<? extends E> coll) {
        Map<E, Integer> count = new HashMap<>();
        if (coll != null) {
            for (E e : coll) {
                count.merge(e, 1, Integer::sum);
            }
        }
        return count;
    }

    /**
     * Count the occurrences of an object in an iterable
     * 统计元素出现次数
     *
     * @param obj  the object to count | 要统计的对象
     * @param coll the iterable | 可迭代对象
     * @return occurrence count | 出现次数
     */
    public static int cardinality(Object obj, Iterable<?> coll) {
        if (coll == null) {
            return 0;
        }
        int count = 0;
        for (Object e : coll) {
            if (Objects.equals(obj, e)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Count elements matching the predicate
     * 统计满足条件的元素数量
     *
     * @param <E>       element type | 元素类型
     * @param input     the iterable | 可迭代对象
     * @param predicate the predicate | 谓词
     * @return match count | 匹配数量
     */
    public static <E> int countMatches(Iterable<E> input, Predicate<? super E> predicate) {
        if (input == null || predicate == null) {
            return 0;
        }
        int count = 0;
        for (E e : input) {
            if (predicate.test(e)) {
                count++;
            }
        }
        return count;
    }

    // ==================== 过滤与转换 | Filtering and Transformation ====================

    /**
     * Filter collection in-place (retain matching elements)
     * 原地过滤（保留匹配元素）
     *
     * @param <E>        element type | 元素类型
     * @param collection the collection | 集合
     * @param predicate  the predicate | 谓词
     * @return true if collection was modified | 如果集合被修改则返回 true
     */
    public static <E> boolean filter(Iterable<E> collection, Predicate<? super E> predicate) {
        if (collection == null || predicate == null) {
            return false;
        }
        boolean modified = false;
        Iterator<E> iterator = collection.iterator();
        while (iterator.hasNext()) {
            if (!predicate.test(iterator.next())) {
                iterator.remove();
                modified = true;
            }
        }
        return modified;
    }

    /**
     * Filter collection in-place (remove matching elements)
     * 原地反向过滤（移除匹配元素）
     *
     * @param <E>        element type | 元素类型
     * @param collection the collection | 集合
     * @param predicate  the predicate | 谓词
     * @return true if collection was modified | 如果集合被修改则返回 true
     */
    public static <E> boolean filterInverse(Iterable<E> collection, Predicate<? super E> predicate) {
        if (collection == null || predicate == null) {
            return false;
        }
        return filter(collection, predicate.negate());
    }

    /**
     * Select matching elements into a new collection
     * 选择匹配元素到新集合
     *
     * @param <E>       element type | 元素类型
     * @param input     the input iterable | 输入可迭代对象
     * @param predicate the predicate | 谓词
     * @return collection of matching elements | 匹配元素集合
     */
    public static <E> Collection<E> select(Iterable<? extends E> input, Predicate<? super E> predicate) {
        List<E> result = new ArrayList<>();
        if (input == null || predicate == null) {
            return result;
        }
        for (E e : input) {
            if (predicate.test(e)) {
                result.add(e);
            }
        }
        return result;
    }

    /**
     * Select non-matching elements into a new collection
     * 选择不匹配元素到新集合
     *
     * @param <E>       element type | 元素类型
     * @param input     the input iterable | 输入可迭代对象
     * @param predicate the predicate | 谓词
     * @return collection of non-matching elements | 不匹配元素集合
     */
    public static <E> Collection<E> selectRejected(Iterable<? extends E> input, Predicate<? super E> predicate) {
        if (predicate == null) {
            return new ArrayList<>();
        }
        return select(input, predicate.negate());
    }

    /**
     * Transform collection in-place
     * 原地转换
     *
     * @param <E>         element type | 元素类型
     * @param collection  the collection | 集合
     * @param transformer the transformer | 转换器
     */
    public static <E> void transform(Collection<E> collection, UnaryOperator<E> transformer) {
        if (collection == null || transformer == null) {
            return;
        }
        if (collection instanceof List<E> list) {
            ListIterator<E> it = list.listIterator();
            while (it.hasNext()) {
                it.set(transformer.apply(it.next()));
            }
        } else {
            List<E> transformed = new ArrayList<>();
            for (E e : collection) {
                transformed.add(transformer.apply(e));
            }
            collection.clear();
            collection.addAll(transformed);
        }
    }

    /**
     * Collect transformed elements into a new collection
     * 收集转换结果到新集合
     *
     * @param <I>         input element type | 输入元素类型
     * @param <O>         output element type | 输出元素类型
     * @param input       the input iterable | 输入可迭代对象
     * @param transformer the transformer | 转换器
     * @return collection of transformed elements | 转换后的元素集合
     */
    public static <I, O> Collection<O> collect(Iterable<I> input, Function<? super I, ? extends O> transformer) {
        List<O> result = new ArrayList<>();
        if (input == null || transformer == null) {
            return result;
        }
        for (I e : input) {
            result.add(transformer.apply(e));
        }
        return result;
    }

    // ==================== 查找 | Searching ====================

    /**
     * Find the first matching element
     * 查找第一个匹配元素
     *
     * @param <E>        element type | 元素类型
     * @param collection the collection | 集合
     * @param predicate  the predicate | 谓词
     * @return first matching element or null | 第一个匹配元素或 null
     */
    public static <E> E find(Iterable<E> collection, Predicate<? super E> predicate) {
        if (collection == null || predicate == null) {
            return null;
        }
        for (E e : collection) {
            if (predicate.test(e)) {
                return e;
            }
        }
        return null;
    }

    /**
     * Check if any element matches the predicate
     * 检查是否存在匹配元素
     *
     * @param <E>       element type | 元素类型
     * @param input     the iterable | 可迭代对象
     * @param predicate the predicate | 谓词
     * @return true if any element matches | 如果有任何元素匹配则返回 true
     */
    public static <E> boolean exists(Iterable<E> input, Predicate<? super E> predicate) {
        return find(input, predicate) != null;
    }

    /**
     * Check if all elements match the predicate
     * 检查所有元素是否匹配
     *
     * @param <E>       element type | 元素类型
     * @param input     the iterable | 可迭代对象
     * @param predicate the predicate | 谓词
     * @return true if all elements match | 如果所有元素都匹配则返回 true
     */
    public static <E> boolean matchesAll(Iterable<E> input, Predicate<? super E> predicate) {
        if (input == null) {
            return true;
        }
        if (predicate == null) {
            return false;
        }
        for (E e : input) {
            if (!predicate.test(e)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Extract the single element from a collection
     * 从单元素集合提取元素
     *
     * @param <E>        element type | 元素类型
     * @param collection the collection | 集合
     * @return the single element | 单个元素
     * @throws OpenCollectionException if collection size is not 1 | 如果集合大小不为 1
     */
    public static <E> E extractSingleton(Collection<E> collection) {
        if (collection == null || collection.size() != 1) {
            throw new OpenCollectionException("Collection must contain exactly one element, but contains: " +
                    (collection == null ? 0 : collection.size()));
        }
        return collection.iterator().next();
    }

    // ==================== 批量操作 | Batch Operations ====================

    /**
     * Add all array elements to collection
     * 批量添加数组元素
     *
     * @param <E>        element type | 元素类型
     * @param collection the collection | 集合
     * @param elements   the elements to add | 要添加的元素
     * @return true if collection was modified | 如果集合被修改则返回 true
     */
    @SafeVarargs
    public static <E> boolean addAll(Collection<E> collection, E... elements) {
        if (collection == null || elements == null || elements.length == 0) {
            return false;
        }
        boolean modified = false;
        for (E e : elements) {
            if (collection.add(e)) {
                modified = true;
            }
        }
        return modified;
    }

    /**
     * Add all iterable elements to collection
     * 批量添加 Iterable 元素
     *
     * @param <E>        element type | 元素类型
     * @param collection the collection | 集合
     * @param iterable   the elements to add | 要添加的元素
     * @return true if collection was modified | 如果集合被修改则返回 true
     */
    public static <E> boolean addAll(Collection<E> collection, Iterable<? extends E> iterable) {
        if (collection == null || iterable == null) {
            return false;
        }
        if (iterable instanceof Collection<? extends E> coll) {
            return collection.addAll(coll);
        }
        boolean modified = false;
        for (E e : iterable) {
            if (collection.add(e)) {
                modified = true;
            }
        }
        return modified;
    }

    /**
     * Add all iterator elements to collection
     * 批量添加 Iterator 元素
     *
     * @param <E>        element type | 元素类型
     * @param collection the collection | 集合
     * @param iterator   the elements to add | 要添加的元素
     * @return true if collection was modified | 如果集合被修改则返回 true
     */
    public static <E> boolean addAll(Collection<E> collection, Iterator<? extends E> iterator) {
        if (collection == null || iterator == null) {
            return false;
        }
        boolean modified = false;
        while (iterator.hasNext()) {
            if (collection.add(iterator.next())) {
                modified = true;
            }
        }
        return modified;
    }

    /**
     * Add element to collection if not null
     * 如果元素不为 null 则添加
     *
     * @param <E>        element type | 元素类型
     * @param collection the collection | 集合
     * @param object     the element to add | 要添加的元素
     * @return true if element was added | 如果元素被添加则返回 true
     */
    public static <E> boolean addIgnoreNull(Collection<E> collection, E object) {
        if (collection == null || object == null) {
            return false;
        }
        return collection.add(object);
    }

    /**
     * Remove all null elements from collection
     * 移除所有 null 元素
     *
     * @param <E>        element type | 元素类型
     * @param collection the collection | 集合
     * @return true if collection was modified | 如果集合被修改则返回 true
     */
    public static <E> boolean removeNulls(Collection<E> collection) {
        if (collection == null) {
            return false;
        }
        return collection.removeIf(Objects::isNull);
    }

    // ==================== 排列组合 | Permutations ====================

    /**
     * Generate all permutations of a collection
     * 生成所有排列
     *
     * @param <E>        element type | 元素类型
     * @param collection the collection | 集合
     * @return all permutations | 所有排列
     */
    public static <E> Collection<List<E>> permutations(Collection<E> collection) {
        if (isEmpty(collection)) {
            List<List<E>> result = new ArrayList<>();
            result.add(new ArrayList<>());
            return result;
        }
        List<E> list = new ArrayList<>(collection);
        List<List<E>> result = new ArrayList<>();
        permute(list, 0, result);
        return result;
    }

    private static <E> void permute(List<E> list, int start, List<List<E>> result) {
        if (start == list.size()) {
            result.add(new ArrayList<>(list));
            return;
        }
        for (int i = start; i < list.size(); i++) {
            Collections.swap(list, start, i);
            permute(list, start + 1, result);
            Collections.swap(list, start, i);
        }
    }

    /**
     * Merge two sorted iterables
     * 合并两个有序集合
     *
     * @param <E> element type | 元素类型
     * @param a   first iterable | 第一个可迭代对象
     * @param b   second iterable | 第二个可迭代对象
     * @param c   comparator | 比较器
     * @return merged sorted list | 合并后的有序列表
     */
    public static <E> List<E> collate(Iterable<? extends E> a, Iterable<? extends E> b,
                                       Comparator<? super E> c) {
        List<E> result = new ArrayList<>();
        if (a == null && b == null) {
            return result;
        }
        if (a == null) {
            for (E e : b) {
                result.add(e);
            }
            return result;
        }
        if (b == null) {
            for (E e : a) {
                result.add(e);
            }
            return result;
        }

        Iterator<? extends E> itA = a.iterator();
        Iterator<? extends E> itB = b.iterator();
        E nextA = itA.hasNext() ? itA.next() : null;
        E nextB = itB.hasNext() ? itB.next() : null;

        while (nextA != null && nextB != null) {
            if (c.compare(nextA, nextB) <= 0) {
                result.add(nextA);
                nextA = itA.hasNext() ? itA.next() : null;
            } else {
                result.add(nextB);
                nextB = itB.hasNext() ? itB.next() : null;
            }
        }
        while (nextA != null) {
            result.add(nextA);
            nextA = itA.hasNext() ? itA.next() : null;
        }
        while (nextB != null) {
            result.add(nextB);
            nextB = itB.hasNext() ? itB.next() : null;
        }
        return result;
    }

    /**
     * Reverse an array in-place
     * 反转数组
     *
     * @param array the array to reverse | 要反转的数组
     */
    public static void reverseArray(Object[] array) {
        if (array == null || array.length <= 1) {
            return;
        }
        int left = 0;
        int right = array.length - 1;
        while (left < right) {
            Object temp = array[left];
            array[left] = array[right];
            array[right] = temp;
            left++;
            right--;
        }
    }

    // ==================== 安全获取 | Safe Access ====================

    /**
     * Get the size of an object (collection, map, array, or iterable)
     * 安全获取集合大小
     *
     * @param object the object | 对象
     * @return size or 0 if null | 大小或 0
     */
    public static int size(Object object) {
        if (object == null) {
            return 0;
        }
        if (object instanceof Collection<?> coll) {
            return coll.size();
        }
        if (object instanceof Map<?, ?> map) {
            return map.size();
        }
        if (object.getClass().isArray()) {
            return java.lang.reflect.Array.getLength(object);
        }
        if (object instanceof Iterable<?> iterable) {
            int count = 0;
            for (Object ignored : iterable) {
                count++;
            }
            return count;
        }
        throw new IllegalArgumentException("Unsupported object type: " + object.getClass());
    }

    /**
     * Get the n-th element from an iterable
     * 安全获取第 N 个元素
     *
     * @param <E>      element type | 元素类型
     * @param iterable the iterable | 可迭代对象
     * @param index    the index | 索引
     * @return the element at index | 索引处的元素
     * @throws IndexOutOfBoundsException if index is out of bounds | 如果索引越界
     */
    public static <E> E get(Iterable<E> iterable, int index) {
        if (iterable == null) {
            throw new IndexOutOfBoundsException("Iterable is null");
        }
        if (index < 0) {
            throw new IndexOutOfBoundsException("Index cannot be negative: " + index);
        }
        if (iterable instanceof List<E> list) {
            return list.get(index);
        }
        int i = 0;
        for (E e : iterable) {
            if (i == index) {
                return e;
            }
            i++;
        }
        throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + i);
    }

    /**
     * Get the first element from an iterable
     * 获取第一个元素
     *
     * @param <E>      element type | 元素类型
     * @param iterable the iterable | 可迭代对象
     * @return the first element or null | 第一个元素或 null
     */
    public static <E> E getFirst(Iterable<E> iterable) {
        if (iterable == null) {
            return null;
        }
        if (iterable instanceof List<E> list) {
            return list.isEmpty() ? null : list.getFirst();
        }
        Iterator<E> iterator = iterable.iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }

    /**
     * Get the last element from an iterable
     * 获取最后一个元素
     *
     * @param <E>      element type | 元素类型
     * @param iterable the iterable | 可迭代对象
     * @return the last element or null | 最后一个元素或 null
     */
    public static <E> E getLast(Iterable<E> iterable) {
        if (iterable == null) {
            return null;
        }
        if (iterable instanceof List<E> list) {
            return list.isEmpty() ? null : list.getLast();
        }
        E last = null;
        for (E e : iterable) {
            last = e;
        }
        return last;
    }

    /**
     * Return an empty collection if the input is null
     * 空集合返回默认值
     *
     * @param <E>        element type | 元素类型
     * @param collection the collection | 集合
     * @return collection or empty list | 集合或空列表
     */
    public static <E> Collection<E> emptyIfNull(Collection<E> collection) {
        return collection == null ? Collections.emptyList() : collection;
    }
}
