package cloud.opencode.base.graph.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Union-Find (Disjoint Set) data structure with path compression and union by rank.
 * 并查集（不相交集合）数据结构，支持路径压缩和按秩合并。
 *
 * <p>Provides efficient operations for tracking connected components in a graph.
 * Uses path compression during find operations and union by rank to keep the tree flat,
 * achieving near-constant amortized time complexity for both find and union.</p>
 * <p>提供高效的操作来跟踪图中的连通分量。在查找操作中使用路径压缩，
 * 在合并操作中使用按秩合并来保持树的扁平化，从而实现近常数的均摊时间复杂度。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Path compression for near O(1) find - 路径压缩实现近O(1)查找</li>
 *   <li>Union by rank for balanced trees - 按秩合并保持树平衡</li>
 *   <li>Component count tracking - 连通分量计数跟踪</li>
 *   <li>Component enumeration - 连通分量枚举</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * UnionFind<String> uf = new UnionFind<>(List.of("A", "B", "C", "D"));
 * uf.union("A", "B");
 * uf.union("C", "D");
 * boolean same = uf.connected("A", "B"); // true
 * int count = uf.componentCount();        // 2
 * Set<String> comp = uf.componentOf("A"); // {"A", "B"}
 * }</pre>
 *
 * @param <V> the vertex type | 顶点类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.3
 */
public class UnionFind<V> {

    private final Map<V, V> parent;
    private final Map<V, Integer> rank;
    private int componentCount;

    /**
     * Create a UnionFind with the given elements, each in its own singleton set.
     * 使用给定元素创建并查集，每个元素初始为独立的单元素集合。
     *
     * @param elements the initial elements | 初始元素集合
     * @throws IllegalArgumentException if elements is null or contains null elements |
     *                                  当elements为null或包含null元素时抛出
     */
    public UnionFind(Collection<V> elements) {
        if (elements == null) {
            throw new IllegalArgumentException("Elements collection must not be null");
        }
        this.parent = new HashMap<>();
        this.rank = new HashMap<>();
        for (V element : elements) {
            if (element == null) {
                throw new IllegalArgumentException("Element must not be null");
            }
            parent.put(element, element);
            rank.put(element, 0);
        }
        this.componentCount = parent.size();
    }

    /**
     * Find the representative (root) of the set containing the given element, with path compression.
     * 查找包含给定元素的集合的代表元素（根），使用路径压缩。
     *
     * @param element the element to find | 要查找的元素
     * @return the representative of the set | 集合的代表元素
     * @throws IllegalArgumentException if element is null or not in the UnionFind |
     *                                  当元素为null或不在并查集中时抛出
     */
    public V find(V element) {
        validateElement(element);
        V root = element;
        while (!Objects.equals(root, parent.get(root))) {
            root = parent.get(root);
        }
        // Path compression
        V current = element;
        while (!Objects.equals(current, root)) {
            V next = parent.get(current);
            parent.put(current, root);
            current = next;
        }
        return root;
    }

    /**
     * Union the sets containing elements a and b using union by rank.
     * 使用按秩合并将包含元素a和b的集合合并。
     *
     * @param a the first element | 第一个元素
     * @param b the second element | 第二个元素
     * @return true if the two elements were in different sets and have been merged,
     *         false if they were already in the same set |
     *         如果两个元素在不同集合中并已合并则返回true，如果已在同一集合中则返回false
     * @throws IllegalArgumentException if a or b is null or not in the UnionFind |
     *                                  当a或b为null或不在并查集中时抛出
     */
    public boolean union(V a, V b) {
        V rootA = find(a);
        V rootB = find(b);
        if (Objects.equals(rootA, rootB)) {
            return false;
        }
        int rankA = rank.get(rootA);
        int rankB = rank.get(rootB);
        if (rankA < rankB) {
            parent.put(rootA, rootB);
        } else if (rankA > rankB) {
            parent.put(rootB, rootA);
        } else {
            parent.put(rootB, rootA);
            rank.put(rootA, rankA + 1);
        }
        componentCount--;
        return true;
    }

    /**
     * Check if two elements are in the same component.
     * 检查两个元素是否在同一个连通分量中。
     *
     * @param a the first element | 第一个元素
     * @param b the second element | 第二个元素
     * @return true if a and b are in the same component | 如果a和b在同一连通分量中则返回true
     * @throws IllegalArgumentException if a or b is null or not in the UnionFind |
     *                                  当a或b为null或不在并查集中时抛出
     */
    public boolean connected(V a, V b) {
        return Objects.equals(find(a), find(b));
    }

    /**
     * Get the number of distinct components.
     * 获取不同连通分量的数量。
     *
     * @return the number of components | 连通分量的数量
     */
    public int componentCount() {
        return componentCount;
    }

    /**
     * Get all elements in the same component as the given element.
     * 获取与给定元素在同一连通分量中的所有元素。
     *
     * @param element the element | 元素
     * @return an unmodifiable set of all elements in the same component |
     *         包含同一连通分量中所有元素的不可修改集合
     * @throws IllegalArgumentException if element is null or not in the UnionFind |
     *                                  当元素为null或不在并查集中时抛出
     */
    public Set<V> componentOf(V element) {
        V root = find(element);
        Set<V> component = new HashSet<>();
        for (V v : parent.keySet()) {
            if (Objects.equals(find(v), root)) {
                component.add(v);
            }
        }
        return Set.copyOf(component);
    }

    /**
     * Get all components as a list of sets.
     * 获取所有连通分量，以集合列表的形式返回。
     *
     * @return a list of all components, each represented as an unmodifiable set |
     *         所有连通分量的列表，每个分量为不可修改的集合
     */
    public List<Set<V>> components() {
        Map<V, Set<V>> groups = new HashMap<>();
        for (V v : parent.keySet()) {
            V root = find(v);
            groups.computeIfAbsent(root, k -> new HashSet<>()).add(v);
        }
        List<Set<V>> result = new ArrayList<>(groups.size());
        for (Set<V> group : groups.values()) {
            result.add(Set.copyOf(group));
        }
        return List.copyOf(result);
    }

    private void validateElement(V element) {
        if (element == null) {
            throw new IllegalArgumentException("Element must not be null");
        }
        if (!parent.containsKey(element)) {
            throw new IllegalArgumentException("Element not found in UnionFind: " + element);
        }
    }
}
