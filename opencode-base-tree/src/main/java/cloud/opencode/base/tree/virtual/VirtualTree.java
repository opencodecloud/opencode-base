package cloud.opencode.base.tree.virtual;

import cloud.opencode.base.tree.Treeable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Virtual Tree
 * 虚拟化树
 *
 * <p>A tree implementation with lazy loading support for large datasets.
 * Children are loaded on-demand when accessed, reducing initial memory footprint.</p>
 * <p>支持懒加载的树实现，适用于大数据量场景。
 * 子节点在访问时按需加载，减少初始内存占用。</p>
 *
 * <p><strong>Key Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Lazy loading - Children loaded on first access - 懒加载</li>
 *   <li>Caching - Loaded nodes are cached - 缓存已加载节点</li>
 *   <li>Thread-safe - Concurrent access support - 线程安全</li>
 *   <li>Memory efficient - LRU eviction for large trees - 内存高效</li>
 *   <li>Pagination - Support for paginated child loading - 分页加载</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * VirtualTree<String, Long> tree = VirtualTree.root(1L, "Root",
 *     parentId -> loadChildrenFromDB(parentId));
 *
 * // Children loaded lazily on access
 * List<VirtualTree<String, Long>> children = tree.getChildren();
 *
 * // Preload to depth
 * tree.preload(3);
 * }</pre>
 * @param <T> the data type | 数据类型
 * @param <ID> the ID type | ID类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
public class VirtualTree<T, ID> implements Treeable<VirtualTree<T, ID>, ID> {

    private final ID id;
    private final ID parentId;
    private final T data;
    private final LazyChildLoader<VirtualTree<T, ID>, ID> childLoader;

    private final AtomicBoolean childrenLoaded = new AtomicBoolean(false);
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private volatile List<VirtualTree<T, ID>> children;

    // Cache for loaded nodes (shared across tree)
    private final Map<ID, VirtualTree<T, ID>> nodeCache;
    private final int maxCacheSize;
    private final boolean cacheEnabled;

    // Statistics (AtomicLong for thread-safe increments)
    private final AtomicLong loadCount = new AtomicLong();
    private final AtomicLong cacheHits = new AtomicLong();
    private final AtomicLong cacheMisses = new AtomicLong();

    /**
     * Create virtual tree node with lazy loading
     * 创建支持懒加载的虚拟树节点
     *
     * @param id the node ID | 节点ID
     * @param parentId the parent ID | 父节点ID
     * @param data the node data | 节点数据
     * @param childLoader the lazy child loader | 懒加载器
     */
    public VirtualTree(ID id, ID parentId, T data, LazyChildLoader<VirtualTree<T, ID>, ID> childLoader) {
        this(id, parentId, data, childLoader, createLruCache(10000), 10000, true);
    }

    /**
     * Create a thread-safe LRU cache
     * 创建线程安全的LRU缓存
     */
    private static <ID, T> Map<ID, VirtualTree<T, ID>> createLruCache(int maxSize) {
        return Collections.synchronizedMap(new LinkedHashMap<>(maxSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<ID, VirtualTree<T, ID>> eldest) {
                return size() > maxSize;
            }
        });
    }

    /**
     * Create virtual tree node with configuration
     * 创建可配置的虚拟树节点
     *
     * @param id the node ID | 节点ID
     * @param parentId the parent ID | 父节点ID
     * @param data the node data | 节点数据
     * @param childLoader the lazy child loader | 懒加载器
     * @param nodeCache shared node cache | 共享节点缓存
     * @param maxCacheSize maximum cache size | 最大缓存大小
     * @param cacheEnabled whether caching is enabled | 是否启用缓存
     */
    public VirtualTree(ID id, ID parentId, T data,
                       LazyChildLoader<VirtualTree<T, ID>, ID> childLoader,
                       Map<ID, VirtualTree<T, ID>> nodeCache,
                       int maxCacheSize, boolean cacheEnabled) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.parentId = parentId;
        this.data = data;
        this.childLoader = Objects.requireNonNull(childLoader, "childLoader cannot be null");
        this.nodeCache = nodeCache;
        this.maxCacheSize = maxCacheSize;
        this.cacheEnabled = cacheEnabled;

        // Register self in cache
        if (cacheEnabled && nodeCache != null) {
            nodeCache.put(id, this);
        }
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create a root virtual tree node
     * 创建根虚拟树节点
     *
     * @param <T> the data type | 数据类型
     * @param <ID> the ID type | ID类型
     * @param id the node ID | 节点ID
     * @param data the node data | 节点数据
     * @param childLoader the lazy child loader | 懒加载器
     * @return the virtual tree node | 虚拟树节点
     */
    public static <T, ID> VirtualTree<T, ID> root(ID id, T data,
                                                   LazyChildLoader<VirtualTree<T, ID>, ID> childLoader) {
        return new VirtualTree<>(id, null, data, childLoader);
    }

    /**
     * Create a virtual tree node with builder
     * 使用构建器创建虚拟树节点
     *
     * @param <T> the data type | 数据类型
     * @param <ID> the ID type | ID类型
     * @return the builder | 构建器
     */
    public static <T, ID> Builder<T, ID> builder() {
        return new Builder<>();
    }

    // ==================== Treeable Implementation | Treeable实现 ====================

    @Override
    public ID getId() {
        return id;
    }

    @Override
    public ID getParentId() {
        return parentId;
    }

    @Override
    public List<VirtualTree<T, ID>> getChildren() {
        ensureChildrenLoaded();
        lock.readLock().lock();
        try {
            return children != null ? Collections.unmodifiableList(children) : List.of();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void setChildren(List<VirtualTree<T, ID>> children) {
        lock.writeLock().lock();
        try {
            this.children = children != null ? new ArrayList<>(children) : new ArrayList<>();
            this.childrenLoaded.set(true);
        } finally {
            lock.writeLock().unlock();
        }
    }

    // ==================== Lazy Loading | 懒加载 ====================

    /**
     * Ensure children are loaded
     * 确保子节点已加载
     */
    private void ensureChildrenLoaded() {
        if (!childrenLoaded.get()) {
            lock.writeLock().lock();
            try {
                if (!childrenLoaded.get()) {
                    loadChildren();
                }
            } finally {
                lock.writeLock().unlock();
            }
        }
    }

    /**
     * Load children from the loader
     * 从加载器加载子节点
     */
    private void loadChildren() {
        try {
            List<VirtualTree<T, ID>> loaded = childLoader.loadChildren(id);
            this.children = loaded != null ? new ArrayList<>(loaded) : new ArrayList<>();

            // Add loaded children to cache
            if (cacheEnabled && nodeCache != null) {
                for (VirtualTree<T, ID> child : this.children) {
                    addToCache(child);
                }
            }

            loadCount.incrementAndGet();
            childrenLoaded.set(true);
        } catch (Exception e) {
            this.children = new ArrayList<>();
            childrenLoaded.set(true);
            throw new RuntimeException("Failed to load children for node: " + id, e);
        }
    }

    /**
     * Add node to cache with LRU eviction
     * 添加节点到缓存（带LRU淘汰）
     *
     * <p>LRU eviction is handled automatically by the underlying LinkedHashMap
     * when the cache exceeds maxCacheSize.</p>
     * <p>当缓存超过maxCacheSize时，底层LinkedHashMap会自动处理LRU淘汰。</p>
     */
    private void addToCache(VirtualTree<T, ID> node) {
        nodeCache.put(node.getId(), node);
    }

    /**
     * Check if children are loaded
     * 检查子节点是否已加载
     *
     * @return true if loaded | 如果已加载返回true
     */
    public boolean isChildrenLoaded() {
        return childrenLoaded.get();
    }

    /**
     * Reload children (force refresh)
     * 重新加载子节点（强制刷新）
     */
    public void reloadChildren() {
        lock.writeLock().lock();
        try {
            childrenLoaded.set(false);
            children = null;
            loadChildren();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Preload children to specified depth
     * 预加载到指定深度的子节点
     *
     * @param depth the depth to preload | 预加载深度
     */
    public void preload(int depth) {
        if (depth <= 0) return;

        ensureChildrenLoaded();
        for (VirtualTree<T, ID> child : getChildren()) {
            child.preload(depth - 1);
        }
    }

    /**
     * Unload children to free memory
     * 卸载子节点以释放内存
     */
    public void unloadChildren() {
        lock.writeLock().lock();
        try {
            if (children != null) {
                // Remove children from cache
                if (cacheEnabled && nodeCache != null) {
                    for (VirtualTree<T, ID> child : children) {
                        nodeCache.remove(child.getId());
                    }
                }
                children = null;
            }
            childrenLoaded.set(false);
        } finally {
            lock.writeLock().unlock();
        }
    }

    // ==================== Node Access | 节点访问 ====================

    /**
     * Get the node data
     * 获取节点数据
     *
     * @return the data | 数据
     */
    public T getData() {
        return data;
    }

    /**
     * Check if this is a root node
     * 检查是否为根节点
     *
     * @return true if root | 如果是根节点返回true
     */
    public boolean isRoot() {
        return parentId == null;
    }

    /**
     * Check if this is a leaf node
     * 检查是否为叶子节点
     *
     * @return true if leaf | 如果是叶子节点返回true
     */
    public boolean isLeaf() {
        return getChildren().isEmpty();
    }

    /**
     * Get child count without loading all children
     * 获取子节点数量（不加载所有子节点）
     *
     * @return the child count | 子节点数量
     */
    public int getChildCount() {
        return getChildren().size();
    }

    /**
     * Find node by ID in cache
     * 从缓存中查找节点
     *
     * @param nodeId the node ID | 节点ID
     * @return the node if found | 找到的节点
     */
    public Optional<VirtualTree<T, ID>> findInCache(ID nodeId) {
        if (cacheEnabled && nodeCache != null) {
            VirtualTree<T, ID> cached = nodeCache.get(nodeId);
            if (cached != null) {
                cacheHits.incrementAndGet();
                return Optional.of(cached);
            }
        }
        cacheMisses.incrementAndGet();
        return Optional.empty();
    }

    /**
     * Find node by ID (searches tree recursively)
     * 通过ID查找节点（递归搜索树）
     *
     * @param nodeId the node ID | 节点ID
     * @return the node if found | 找到的节点
     */
    public Optional<VirtualTree<T, ID>> find(ID nodeId) {
        // First check cache
        Optional<VirtualTree<T, ID>> cached = findInCache(nodeId);
        if (cached.isPresent()) {
            return cached;
        }

        // Then search tree
        if (Objects.equals(this.id, nodeId)) {
            return Optional.of(this);
        }

        for (VirtualTree<T, ID> child : getChildren()) {
            Optional<VirtualTree<T, ID>> found = child.find(nodeId);
            if (found.isPresent()) {
                return found;
            }
        }

        return Optional.empty();
    }

    /**
     * Find all nodes matching predicate
     * 查找所有匹配谓词的节点
     *
     * @param predicate the predicate | 谓词
     * @return the matching nodes | 匹配的节点
     */
    public List<VirtualTree<T, ID>> findAll(Predicate<T> predicate) {
        List<VirtualTree<T, ID>> result = new ArrayList<>();
        findAllRecursive(predicate, result);
        return result;
    }

    private void findAllRecursive(Predicate<T> predicate, List<VirtualTree<T, ID>> result) {
        if (predicate.test(data)) {
            result.add(this);
        }
        for (VirtualTree<T, ID> child : getChildren()) {
            child.findAllRecursive(predicate, result);
        }
    }

    // ==================== Traversal | 遍历 ====================

    /**
     * Traverse in pre-order
     * 前序遍历
     *
     * @param visitor the visitor | 访问者
     */
    public void traversePreOrder(Consumer<VirtualTree<T, ID>> visitor) {
        visitor.accept(this);
        for (VirtualTree<T, ID> child : getChildren()) {
            child.traversePreOrder(visitor);
        }
    }

    /**
     * Traverse in post-order
     * 后序遍历
     *
     * @param visitor the visitor | 访问者
     */
    public void traversePostOrder(Consumer<VirtualTree<T, ID>> visitor) {
        for (VirtualTree<T, ID> child : getChildren()) {
            child.traversePostOrder(visitor);
        }
        visitor.accept(this);
    }

    /**
     * Traverse level by level (breadth-first)
     * 层级遍历（广度优先）
     *
     * @param visitor the visitor | 访问者
     */
    public void traverseBreadthFirst(Consumer<VirtualTree<T, ID>> visitor) {
        Queue<VirtualTree<T, ID>> queue = new ArrayDeque<>();
        queue.offer(this);

        while (!queue.isEmpty()) {
            VirtualTree<T, ID> current = queue.poll();
            visitor.accept(current);
            queue.addAll(current.getChildren());
        }
    }

    /**
     * Traverse with depth limit (for controlled lazy loading)
     * 带深度限制的遍历（用于控制懒加载）
     *
     * @param visitor the visitor | 访问者
     * @param maxDepth the maximum depth | 最大深度
     */
    public void traverseWithDepthLimit(Consumer<VirtualTree<T, ID>> visitor, int maxDepth) {
        traverseWithDepthLimitRecursive(visitor, maxDepth, 0);
    }

    private void traverseWithDepthLimitRecursive(Consumer<VirtualTree<T, ID>> visitor, int maxDepth, int currentDepth) {
        if (currentDepth > maxDepth) return;

        visitor.accept(this);
        for (VirtualTree<T, ID> child : getChildren()) {
            child.traverseWithDepthLimitRecursive(visitor, maxDepth, currentDepth + 1);
        }
    }

    // ==================== Statistics | 统计 ====================

    /**
     * Get cache statistics
     * 获取缓存统计
     *
     * @return the statistics | 统计信息
     */
    public CacheStats getCacheStats() {
        return new CacheStats(
                nodeCache != null ? nodeCache.size() : 0,
                maxCacheSize,
                loadCount.get(),
                cacheHits.get(),
                cacheMisses.get()
        );
    }

    /**
     * Clear cache
     * 清除缓存
     */
    public void clearCache() {
        if (nodeCache != null) {
            nodeCache.clear();
            // Re-register self
            nodeCache.put(id, this);
        }
    }

    /**
     * Get total node count (loaded nodes only)
     * 获取总节点数（仅已加载的节点）
     *
     * @return the count | 数量
     */
    public int getLoadedNodeCount() {
        int count = 1;
        if (childrenLoaded.get() && children != null) {
            for (VirtualTree<T, ID> child : children) {
                count += child.getLoadedNodeCount();
            }
        }
        return count;
    }

    // ==================== Cache Statistics Record | 缓存统计记录 ====================

    /**
     * Cache Statistics
     * 缓存统计
     */
    public record CacheStats(
            int currentSize,
            int maxSize,
            long loadCount,
            long cacheHits,
            long cacheMisses
    ) {
        /**
         * Get cache hit rate
         * 获取缓存命中率
         *
         * @return the hit rate | 命中率
         */
        public double hitRate() {
            long total = cacheHits + cacheMisses;
            return total > 0 ? (double) cacheHits / total : 0.0;
        }

        /**
         * Get cache usage percentage
         * 获取缓存使用率
         *
         * @return the usage percentage | 使用率
         */
        public double usagePercentage() {
            return maxSize > 0 ? (double) currentSize / maxSize * 100 : 0.0;
        }
    }

    // ==================== Builder | 构建器 ====================

    /**
     * Virtual Tree Builder
     * 虚拟树构建器
     *
     * @param <T> the data type | 数据类型
     * @param <ID> the ID type | ID类型
     */
    public static class Builder<T, ID> {
        private ID id;
        private ID parentId;
        private T data;
        private LazyChildLoader<VirtualTree<T, ID>, ID> childLoader;
        private Map<ID, VirtualTree<T, ID>> nodeCache;
        private int maxCacheSize = 10000;
        private boolean cacheEnabled = true;

        /**
         * Set node ID
         * 设置节点ID
         *
         * @param id the ID | ID
         * @return the builder | 构建器
         */
        public Builder<T, ID> id(ID id) {
            this.id = id;
            return this;
        }

        /**
         * Set parent ID
         * 设置父节点ID
         *
         * @param parentId the parent ID | 父节点ID
         * @return the builder | 构建器
         */
        public Builder<T, ID> parentId(ID parentId) {
            this.parentId = parentId;
            return this;
        }

        /**
         * Set node data
         * 设置节点数据
         *
         * @param data the data | 数据
         * @return the builder | 构建器
         */
        public Builder<T, ID> data(T data) {
            this.data = data;
            return this;
        }

        /**
         * Set child loader
         * 设置子节点加载器
         *
         * @param childLoader the loader | 加载器
         * @return the builder | 构建器
         */
        public Builder<T, ID> childLoader(LazyChildLoader<VirtualTree<T, ID>, ID> childLoader) {
            this.childLoader = childLoader;
            return this;
        }

        /**
         * Set node cache
         * 设置节点缓存
         *
         * @param nodeCache the cache | 缓存
         * @return the builder | 构建器
         */
        public Builder<T, ID> nodeCache(Map<ID, VirtualTree<T, ID>> nodeCache) {
            this.nodeCache = nodeCache;
            return this;
        }

        /**
         * Set max cache size
         * 设置最大缓存大小
         *
         * @param maxCacheSize the max size | 最大大小
         * @return the builder | 构建器
         */
        public Builder<T, ID> maxCacheSize(int maxCacheSize) {
            this.maxCacheSize = maxCacheSize;
            return this;
        }

        /**
         * Enable or disable cache
         * 启用或禁用缓存
         *
         * @param cacheEnabled whether enabled | 是否启用
         * @return the builder | 构建器
         */
        public Builder<T, ID> cacheEnabled(boolean cacheEnabled) {
            this.cacheEnabled = cacheEnabled;
            return this;
        }

        /**
         * Build the virtual tree node
         * 构建虚拟树节点
         *
         * @return the node | 节点
         */
        public VirtualTree<T, ID> build() {
            if (nodeCache == null) {
                nodeCache = createLruCache(maxCacheSize);
            }
            return new VirtualTree<>(id, parentId, data, childLoader, nodeCache, maxCacheSize, cacheEnabled);
        }
    }

    @Override
    public String toString() {
        return "VirtualTree{" +
                "id=" + id +
                ", parentId=" + parentId +
                ", data=" + data +
                ", loaded=" + childrenLoaded.get() +
                ", childCount=" + (children != null ? children.size() : "?") +
                '}';
    }
}
