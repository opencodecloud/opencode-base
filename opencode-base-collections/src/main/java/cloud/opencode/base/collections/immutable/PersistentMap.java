package cloud.opencode.base.collections.immutable;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * PersistentMap - Persistent immutable map based on HAMT (Hash Array Mapped Trie)
 * PersistentMap - 基于 HAMT（哈希数组映射字典树）的持久化不可变映射
 *
 * <p>Uses structural sharing for efficient immutable updates.
 * {@code put}/{@code remove} operations return new maps in O(log32 n) time,
 * sharing structure with the original map.</p>
 * <p>使用结构共享实现高效的不可变更新。{@code put}/{@code remove} 操作在 O(log32 n)
 * 时间内返回新映射，与原映射共享结构。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Structural sharing via HAMT - 基于 HAMT 的结构共享</li>
 *   <li>Immutable - 不可变</li>
 *   <li>O(log32 n) put/get/remove - O(log32 n) 的插入/查找/删除</li>
 *   <li>Hash collision handling - 哈希冲突处理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create map - 创建映射
 * PersistentMap<String, Integer> map = PersistentMap.<String, Integer>empty()
 *     .put("a", 1)
 *     .put("b", 2);
 *
 * // Original is unchanged after put - put 后原映射不变
 * PersistentMap<String, Integer> map2 = map.put("c", 3);
 * // map still has size 2, map2 has size 3
 *
 * // Get value - 获取值
 * Optional<Integer> val = map.get("a"); // Optional[1]
 *
 * // Remove key - 删除键
 * PersistentMap<String, Integer> map3 = map.remove("a");
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>put: O(log32 n) - put: O(log32 n)</li>
 *   <li>get: O(log32 n) - get: O(log32 n)</li>
 *   <li>remove: O(log32 n) - remove: O(log32 n)</li>
 *   <li>containsKey: O(log32 n) - containsKey: O(log32 n)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 * </ul>
 *
 * @param <K> key type | 键类型
 * @param <V> value type | 值类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.3
 */
public final class PersistentMap<K, V> {

    /** Number of bits per trie level. | 每个字典树层级的位数。 */
    private static final int BITS = 5;

    /** Branching factor (2^5 = 32). | 分支因子 (2^5 = 32)。 */
    private static final int WIDTH = 1 << BITS;

    /** Bit mask for extracting index at each level. | 用于在每个层级提取索引的位掩码。 */
    private static final int MASK = WIDTH - 1;

    /** Maximum trie depth (32^7 > Integer.MAX_VALUE). | 最大字典树深度。 */
    private static final int MAX_DEPTH = 7;

    /** Maximum shift value (MAX_DEPTH * BITS). | 最大位移值。 */
    private static final int MAX_SHIFT = MAX_DEPTH * BITS;

    private final Node<K, V> root;
    private final int size;

    @SuppressWarnings("rawtypes")
    private static final PersistentMap EMPTY = new PersistentMap<>(EmptyNode.INSTANCE, 0);

    private PersistentMap(Node<K, V> root, int size) {
        this.root = root;
        this.size = size;
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Return an empty persistent map.
     * 返回一个空的持久化映射。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return empty persistent map | 空的持久化映射
     */
    @SuppressWarnings("unchecked")
    public static <K, V> PersistentMap<K, V> empty() {
        return (PersistentMap<K, V>) EMPTY;
    }

    /**
     * Create a persistent map with one entry.
     * 创建包含一个条目的持久化映射。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @param k1  the key | 键
     * @param v1  the value | 值
     * @return persistent map with one entry | 包含一个条目的持久化映射
     */
    public static <K, V> PersistentMap<K, V> of(K k1, V v1) {
        return PersistentMap.<K, V>empty().put(k1, v1);
    }

    /**
     * Create a persistent map with two entries.
     * 创建包含两个条目的持久化映射。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @param k1  the first key | 第一个键
     * @param v1  the first value | 第一个值
     * @param k2  the second key | 第二个键
     * @param v2  the second value | 第二个值
     * @return persistent map with two entries | 包含两个条目的持久化映射
     */
    public static <K, V> PersistentMap<K, V> of(K k1, V v1, K k2, V v2) {
        return PersistentMap.<K, V>empty().put(k1, v1).put(k2, v2);
    }

    // ==================== 核心操作 | Core Operations ====================

    /**
     * Return a new map with the given key-value pair added or updated.
     * 返回添加或更新给定键值对后的新映射。
     *
     * @param key   the key | 键
     * @param value the value | 值
     * @return a new map with the entry | 包含该条目的新映射
     */
    public PersistentMap<K, V> put(K key, V value) {
        Objects.requireNonNull(key, "Key must not be null");
        Objects.requireNonNull(value, "Value must not be null");
        int hash = hash(key);
        PutResult<K, V> result = root.put(key, value, hash, 0);
        int newSize = result.replaced() ? size : size + 1;
        return new PersistentMap<>(result.node(), newSize);
    }

    /**
     * Return a new map with the given key removed.
     * 返回删除给定键后的新映射。
     *
     * @param key the key to remove | 要删除的键
     * @return a new map without the key | 不包含该键的新映射
     */
    public PersistentMap<K, V> remove(K key) {
        Objects.requireNonNull(key, "Key must not be null");
        int hash = hash(key);
        RemoveResult<K, V> result = root.remove(key, hash, 0);
        if (!result.found()) {
            return this;
        }
        return new PersistentMap<>(result.node(), size - 1);
    }

    /**
     * Return the value associated with the given key, if present.
     * 返回与给定键关联的值（如果存在）。
     *
     * @param key the key | 键
     * @return optional containing the value, or empty | 包含值的 Optional，或空
     */
    public Optional<V> get(K key) {
        Objects.requireNonNull(key, "Key must not be null");
        int hash = hash(key);
        return root.get(key, hash, 0);
    }

    /**
     * Check if this map contains the given key.
     * 检查此映射是否包含给定键。
     *
     * @param key the key | 键
     * @return true if the key is present | 如果键存在则返回 true
     */
    public boolean containsKey(K key) {
        return get(key).isPresent();
    }

    /**
     * Return the number of entries in this map.
     * 返回此映射中的条目数量。
     *
     * @return the size | 大小
     */
    public int size() {
        return size;
    }

    /**
     * Check if this map is empty.
     * 检查此映射是否为空。
     *
     * @return true if the map is empty | 如果映射为空则返回 true
     */
    public boolean isEmpty() {
        return size == 0;
    }

    // ==================== 转换操作 | Conversion Operations ====================

    /**
     * Convert this persistent map to a JDK {@link Map}.
     * 将此持久化映射转换为 JDK {@link Map}。
     *
     * @return an unmodifiable map containing all entries | 包含所有条目的不可修改映射
     */
    public Map<K, V> toMap() {
        Map<K, V> result = new HashMap<>(size);
        root.collectEntries(result);
        return Collections.unmodifiableMap(result);
    }

    /**
     * Return the set of keys.
     * 返回键的集合。
     *
     * @return unmodifiable set of keys | 不可修改的键集合
     */
    public Set<K> keySet() {
        Set<K> keys = new HashSet<>(size);
        Map<K, V> entries = new HashMap<>(size);
        root.collectEntries(entries);
        keys.addAll(entries.keySet());
        return Collections.unmodifiableSet(keys);
    }

    /**
     * Return the collection of values.
     * 返回值的集合。
     *
     * @return unmodifiable collection of values | 不可修改的值集合
     */
    public Collection<V> values() {
        Map<K, V> entries = new HashMap<>(size);
        root.collectEntries(entries);
        return Collections.unmodifiableCollection(entries.values());
    }

    /**
     * Return the set of entries.
     * 返回条目集合。
     *
     * @return unmodifiable set of entries | 不可修改的条目集合
     */
    public Set<Map.Entry<K, V>> entrySet() {
        Map<K, V> entries = new HashMap<>(size);
        root.collectEntries(entries);
        return Collections.unmodifiableSet(entries.entrySet());
    }

    /**
     * Return a sequential stream over the entries.
     * 返回条目上的顺序流。
     *
     * @return a stream of entries | 条目的流
     */
    public Stream<Map.Entry<K, V>> stream() {
        return entrySet().stream();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PersistentMap<?, ?> other)) {
            return false;
        }
        if (this.size != other.size) {
            return false;
        }
        return this.toMap().equals(other.toMap());
    }

    @Override
    public int hashCode() {
        return toMap().hashCode();
    }

    @Override
    public String toString() {
        return "PersistentMap" + toMap();
    }

    // ==================== 内部实现 | Internal Implementation ====================

    /**
     * Spread hash bits for better distribution.
     * 扩散哈希位以获得更好的分布。
     */
    private static int hash(Object key) {
        int h = key.hashCode();
        return h ^ (h >>> 16);
    }

    /**
     * Extract the index (0-31) from hash at the given shift level.
     * 在给定的 shift 层级从 hash 中提取索引 (0-31)。
     */
    private static int index(int hash, int shift) {
        return (hash >>> shift) & MASK;
    }

    // ==================== 结果类型 | Result Types ====================

    /**
     * Result of a put operation.
     * put 操作的结果。
     */
    private record PutResult<K, V>(Node<K, V> node, boolean replaced) {
    }

    /**
     * Result of a remove operation.
     * remove 操作的结果。
     */
    private record RemoveResult<K, V>(Node<K, V> node, boolean found) {
    }

    // ==================== 节点接口 | Node Interface ====================

    /**
     * Node in the HAMT trie.
     * HAMT 字典树中的节点。
     */
    private sealed interface Node<K, V>
            permits EmptyNode, LeafNode, BitmapNode, CollisionNode {

        /**
         * Get value for key.
         * 获取键对应的值。
         */
        Optional<V> get(K key, int hash, int shift);

        /**
         * Put a key-value pair.
         * 放入一个键值对。
         */
        PutResult<K, V> put(K key, V value, int hash, int shift);

        /**
         * Remove a key.
         * 删除一个键。
         */
        RemoveResult<K, V> remove(K key, int hash, int shift);

        /**
         * Collect all entries into the given map.
         * 将所有条目收集到给定的映射中。
         */
        void collectEntries(Map<K, V> target);
    }

    // ==================== EmptyNode | 空节点 ====================

    /**
     * Empty node singleton.
     * 空节点单例。
     */
    private record EmptyNode<K, V>() implements Node<K, V> {

        @SuppressWarnings("rawtypes")
        static final EmptyNode INSTANCE = new EmptyNode();

        @Override
        public Optional<V> get(K key, int hash, int shift) {
            return Optional.empty();
        }

        @Override
        public PutResult<K, V> put(K key, V value, int hash, int shift) {
            return new PutResult<>(new LeafNode<>(key, value, hash), false);
        }

        @Override
        public RemoveResult<K, V> remove(K key, int hash, int shift) {
            return new RemoveResult<>(this, false);
        }

        @Override
        public void collectEntries(Map<K, V> target) {
            // empty — nothing to collect
        }
    }

    // ==================== LeafNode | 叶节点 ====================

    /**
     * Leaf node holding a single key-value pair.
     * 持有单个键值对的叶节点。
     */
    private record LeafNode<K, V>(K key, V value, int hash) implements Node<K, V> {

        @Override
        public Optional<V> get(K key, int hash, int shift) {
            if (this.hash == hash && Objects.equals(this.key, key)) {
                return Optional.of(value);
            }
            return Optional.empty();
        }

        @Override
        public PutResult<K, V> put(K key, V value, int hash, int shift) {
            if (this.hash == hash && Objects.equals(this.key, key)) {
                // Same key — replace value
                if (Objects.equals(this.value, value)) {
                    return new PutResult<>(this, true);
                }
                return new PutResult<>(new LeafNode<>(key, value, hash), true);
            }
            if (this.hash == hash) {
                // Hash collision — create CollisionNode
                return new PutResult<>(
                        new CollisionNode<>(hash, List.of(
                                Map.entry(this.key, this.value),
                                Map.entry(key, value)
                        )),
                        false
                );
            }
            // Different hash — create a BitmapNode and insert both
            return mergeTwoLeaves(this, new LeafNode<>(key, value, hash), shift);
        }

        @Override
        public RemoveResult<K, V> remove(K key, int hash, int shift) {
            if (this.hash == hash && Objects.equals(this.key, key)) {
                return new RemoveResult<>(emptyNode(), true);
            }
            return new RemoveResult<>(this, false);
        }

        @Override
        public void collectEntries(Map<K, V> target) {
            target.put(key, value);
        }
    }

    // ==================== BitmapNode | 位图节点 ====================

    /**
     * Bitmap-indexed node with compressed child array.
     * 使用位图索引的压缩子数组节点。
     */
    private record BitmapNode<K, V>(int bitmap, Node<K, V>[] children) implements Node<K, V> {

        @Override
        public Optional<V> get(K key, int hash, int shift) {
            int bit = 1 << index(hash, shift);
            if ((bitmap & bit) == 0) {
                return Optional.empty();
            }
            int idx = Integer.bitCount(bitmap & (bit - 1));
            return children[idx].get(key, hash, shift + BITS);
        }

        @Override
        public PutResult<K, V> put(K key, V value, int hash, int shift) {
            int bit = 1 << index(hash, shift);
            int idx = Integer.bitCount(bitmap & (bit - 1));

            if ((bitmap & bit) != 0) {
                // Slot exists — recurse into child
                PutResult<K, V> result = children[idx].put(key, value, hash, shift + BITS);
                if (result.node() == children[idx]) {
                    return new PutResult<>(this, result.replaced());
                }
                Node<K, V>[] newChildren = children.clone();
                newChildren[idx] = result.node();
                return new PutResult<>(new BitmapNode<>(bitmap, newChildren), result.replaced());
            } else {
                // Empty slot — insert new leaf
                Node<K, V> newLeaf = new LeafNode<>(key, value, hash);
                int len = children.length;
                @SuppressWarnings("unchecked")
                Node<K, V>[] newChildren = (Node<K, V>[]) new Node<?, ?>[len + 1];
                System.arraycopy(children, 0, newChildren, 0, idx);
                newChildren[idx] = newLeaf;
                System.arraycopy(children, idx, newChildren, idx + 1, len - idx);
                return new PutResult<>(new BitmapNode<>(bitmap | bit, newChildren), false);
            }
        }

        @Override
        public RemoveResult<K, V> remove(K key, int hash, int shift) {
            int bit = 1 << index(hash, shift);
            if ((bitmap & bit) == 0) {
                return new RemoveResult<>(this, false);
            }
            int idx = Integer.bitCount(bitmap & (bit - 1));
            RemoveResult<K, V> result = children[idx].remove(key, hash, shift + BITS);
            if (!result.found()) {
                return new RemoveResult<>(this, false);
            }
            Node<K, V> updatedChild = result.node();
            if (updatedChild instanceof EmptyNode<K, V>) {
                // Child became empty — remove slot
                int newBitmap = bitmap & ~bit;
                if (newBitmap == 0) {
                    return new RemoveResult<>(emptyNode(), true);
                }
                int len = children.length;
                @SuppressWarnings("unchecked")
                Node<K, V>[] newChildren = (Node<K, V>[]) new Node<?, ?>[len - 1];
                System.arraycopy(children, 0, newChildren, 0, idx);
                System.arraycopy(children, idx + 1, newChildren, idx, len - idx - 1);
                // Collapse single-child bitmap to its child if it's a leaf
                if (newChildren.length == 1 && newChildren[0] instanceof LeafNode<K, V>) {
                    return new RemoveResult<>(newChildren[0], true);
                }
                return new RemoveResult<>(new BitmapNode<>(newBitmap, newChildren), true);
            }
            // Child was updated but not removed
            Node<K, V>[] newChildren = children.clone();
            newChildren[idx] = updatedChild;
            return new RemoveResult<>(new BitmapNode<>(bitmap, newChildren), true);
        }

        @Override
        public void collectEntries(Map<K, V> target) {
            for (Node<K, V> child : children) {
                child.collectEntries(target);
            }
        }
    }

    // ==================== CollisionNode | 冲突节点 ====================

    /**
     * Node handling hash collisions (same hash, different keys).
     * 处理哈希冲突的节点（相同哈希，不同键）。
     */
    private record CollisionNode<K, V>(int hash, List<Map.Entry<K, V>> entries) implements Node<K, V> {

        @Override
        public Optional<V> get(K key, int hash, int shift) {
            // Linear scan — handles both true hash collisions and mixed-hash entries
            // at the trie depth limit where different hashes are merged into one node.
            // 线性扫描 — 处理真实哈希冲突和深度上限处不同哈希合并到同一节点的情况。
            for (Map.Entry<K, V> entry : entries) {
                if (Objects.equals(entry.getKey(), key)) {
                    return Optional.of(entry.getValue());
                }
            }
            return Optional.empty();
        }

        @Override
        public PutResult<K, V> put(K key, V value, int hash, int shift) {
            if (this.hash == hash) {
                // Same hash bucket — look for existing key
                for (int i = 0; i < entries.size(); i++) {
                    if (Objects.equals(entries.get(i).getKey(), key)) {
                        // Replace value
                        List<Map.Entry<K, V>> newEntries = new ArrayList<>(entries);
                        newEntries.set(i, Map.entry(key, value));
                        return new PutResult<>(new CollisionNode<>(hash, List.copyOf(newEntries)), true);
                    }
                }
                // New key with same hash
                List<Map.Entry<K, V>> newEntries = new ArrayList<>(entries);
                newEntries.add(Map.entry(key, value));
                return new PutResult<>(new CollisionNode<>(hash, List.copyOf(newEntries)), false);
            }
            // Different hash — need to split into a BitmapNode
            // Wrap this collision node and the new leaf into a bitmap node
            Node<K, V> newLeaf = new LeafNode<>(key, value, hash);
            return mergeCollisionAndLeaf(this, newLeaf, shift);
        }

        @Override
        public RemoveResult<K, V> remove(K key, int hash, int shift) {
            // No hash short-circuit — handles mixed-hash entries at depth limit.
            // 不做哈希短路 — 处理深度上限处不同哈希合并到同一节点的情况。
            for (int i = 0; i < entries.size(); i++) {
                if (Objects.equals(entries.get(i).getKey(), key)) {
                    if (entries.size() == 2) {
                        Map.Entry<K, V> remaining = entries.get(1 - i);
                        if (PersistentMap.hash(remaining.getKey()) == this.hash) {
                            // Same hash — safe to collapse to LeafNode
                            // 相同哈希 — 可以安全折叠为 LeafNode
                            return new RemoveResult<>(
                                    new LeafNode<>(remaining.getKey(), remaining.getValue(), this.hash),
                                    true
                            );
                        }
                        // Mixed hashes (depth-limit merge) — keep as 1-entry CollisionNode
                        // to preserve the remaining entry's correct hash lookup path.
                        // 混合哈希（深度上限合并）— 保留为 1-entry CollisionNode
                        // 以保持剩余 entry 的正确哈希查找路径。
                    }
                    List<Map.Entry<K, V>> newEntries = new ArrayList<>(entries);
                    newEntries.remove(i);
                    return new RemoveResult<>(
                            new CollisionNode<>(this.hash, List.copyOf(newEntries)),
                            true
                    );
                }
            }
            return new RemoveResult<>(this, false);
        }

        @Override
        public void collectEntries(Map<K, V> target) {
            for (Map.Entry<K, V> entry : entries) {
                target.put(entry.getKey(), entry.getValue());
            }
        }
    }

    // ==================== 辅助方法 | Helper Methods ====================

    @SuppressWarnings("unchecked")
    private static <K, V> Node<K, V> emptyNode() {
        return (Node<K, V>) EmptyNode.INSTANCE;
    }

    /**
     * Merge two leaf nodes with different hashes into a BitmapNode tree.
     * 将两个具有不同哈希值的叶节点合并为 BitmapNode 树。
     */
    private static <K, V> PutResult<K, V> mergeTwoLeaves(
            LeafNode<K, V> leaf1, LeafNode<K, V> leaf2, int shift) {
        if (shift >= MAX_DEPTH * BITS) {
            // Extremely unlikely — treat as collision
            return new PutResult<>(
                    new CollisionNode<>(leaf1.hash(), List.of(
                            Map.entry(leaf1.key(), leaf1.value()),
                            Map.entry(leaf2.key(), leaf2.value())
                    )),
                    false
            );
        }
        int idx1 = index(leaf1.hash(), shift);
        int idx2 = index(leaf2.hash(), shift);
        if (idx1 == idx2) {
            // Same index at this level — recurse deeper
            PutResult<K, V> deeper = mergeTwoLeaves(leaf1, leaf2, shift + BITS);
            int bit = 1 << idx1;
            @SuppressWarnings("unchecked")
            Node<K, V>[] children = (Node<K, V>[]) new Node<?, ?>[]{deeper.node()};
            return new PutResult<>(new BitmapNode<>(bit, children), false);
        }
        // Different indices — place both in a BitmapNode
        int bit1 = 1 << idx1;
        int bit2 = 1 << idx2;
        int bitmap = bit1 | bit2;
        @SuppressWarnings("unchecked")
        Node<K, V>[] children;
        if (idx1 < idx2) {
            children = (Node<K, V>[]) new Node<?, ?>[]{leaf1, leaf2};
        } else {
            children = (Node<K, V>[]) new Node<?, ?>[]{leaf2, leaf1};
        }
        return new PutResult<>(new BitmapNode<>(bitmap, children), false);
    }

    /**
     * Merge a collision node with a leaf node at a given trie level.
     * 在给定的字典树层级合并冲突节点和叶节点。
     */
    private static <K, V> PutResult<K, V> mergeCollisionAndLeaf(
            CollisionNode<K, V> collision, Node<K, V> leaf, int shift) {
        int idxC = index(collision.hash(), shift);
        int hashL;
        if (leaf instanceof LeafNode<K, V> l) {
            hashL = l.hash();
        } else {
            throw new IllegalStateException("Expected LeafNode");
        }
        int idxL = index(hashL, shift);

        if (shift >= MAX_SHIFT) {
            // Depth limit reached — merge leaf entry into collision node
            LeafNode<K, V> leafNode = (LeafNode<K, V>) leaf;
            List<Map.Entry<K, V>> merged = new ArrayList<>(collision.entries());
            merged.add(Map.entry(leafNode.key(), leafNode.value()));
            return new PutResult<>(new CollisionNode<>(collision.hash(), List.copyOf(merged)), false);
        }

        if (idxC == idxL) {
            // Same slot — recurse deeper
            PutResult<K, V> deeper = mergeCollisionAndLeaf(collision, leaf, shift + BITS);
            int bit = 1 << idxC;
            @SuppressWarnings("unchecked")
            Node<K, V>[] children = (Node<K, V>[]) new Node<?, ?>[]{deeper.node()};
            return new PutResult<>(new BitmapNode<>(bit, children), false);
        }

        int bitC = 1 << idxC;
        int bitL = 1 << idxL;
        int bitmap = bitC | bitL;
        @SuppressWarnings("unchecked")
        Node<K, V>[] children;
        if (idxC < idxL) {
            children = (Node<K, V>[]) new Node<?, ?>[]{collision, leaf};
        } else {
            children = (Node<K, V>[]) new Node<?, ?>[]{leaf, collision};
        }
        return new PutResult<>(new BitmapNode<>(bitmap, children), false);
    }
}
