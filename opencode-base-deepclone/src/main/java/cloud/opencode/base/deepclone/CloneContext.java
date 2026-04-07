package cloud.opencode.base.deepclone;

import java.util.*;

/**
 * Context for tracking clone state and handling circular references
 * 用于跟踪克隆状态和处理循环引用的上下文
 *
 * <p>Each clone operation should use a fresh CloneContext to track
 * already-cloned objects and prevent infinite loops from circular references.</p>
 * <p>每次克隆操作应使用新的CloneContext来跟踪已克隆的对象，防止循环引用导致的无限循环。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Circular reference detection via IdentityHashMap - 通过IdentityHashMap检测循环引用</li>
 *   <li>Clone depth tracking - 克隆深度跟踪</li>
 *   <li>Clone path tracking for debugging - 用于调试的克隆路径跟踪</li>
 *   <li>Statistics collection - 统计信息收集</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CloneContext context = CloneContext.create();
 *
 * // Check if object already cloned
 * if (context.isCloned(original)) {
 *     return context.getCloned(original);
 * }
 *
 * // Register cloned object
 * context.registerCloned(original, cloned);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (use separate context per thread) - 线程安全: 否（每线程使用单独上下文）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
public final class CloneContext {

    /**
     * Maps original objects to their clones (identity-based)
     * 原始对象到克隆对象的映射（基于身份）
     */
    private final IdentityHashMap<Object, Object> clonedObjects = new IdentityHashMap<>(64);

    /**
     * Current clone depth
     * 当前克隆深度
     */
    private int depth = 0;

    /**
     * Maximum allowed depth
     * 允许的最大深度
     */
    private int maxDepth = 100;

    /**
     * Clone path for debugging
     * 用于调试的克隆路径
     */
    private final Deque<String> path = new ArrayDeque<>();

    /**
     * Clone policy
     * 克隆策略
     */
    private ClonePolicy policy = ClonePolicy.STANDARD;

    /**
     * Warnings for LENIENT mode
     * 宽松模式下的警告
     */
    private final java.util.List<String> warnings = new java.util.ArrayList<>();

    /**
     * Statistics tracking
     * 统计跟踪
     */
    private int objectsCloned = 0;
    private int objectsSkipped = 0;
    private int maxDepthReached = 0;
    private long startTime = System.nanoTime();

    private CloneContext() {
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates a new CloneContext
     * 创建新的CloneContext
     *
     * @return the new context | 新的上下文
     */
    public static CloneContext create() {
        return new CloneContext();
    }

    /**
     * Creates a new CloneContext with a max depth
     * 创建指定最大深度的CloneContext
     *
     * @param maxDepth the maximum depth | 最大深度
     * @return the new context | 新的上下文
     */
    public static CloneContext create(int maxDepth) {
        CloneContext ctx = new CloneContext();
        ctx.maxDepth = maxDepth;
        return ctx;
    }

    /**
     * Creates a new CloneContext with max depth and policy
     * 创建指定最大深度和策略的CloneContext
     *
     * @param maxDepth the maximum depth | 最大深度
     * @param policy   the clone policy | 克隆策略
     * @return the new context | 新的上下文
     */
    public static CloneContext create(int maxDepth, ClonePolicy policy) {
        CloneContext ctx = new CloneContext();
        ctx.maxDepth = maxDepth;
        ctx.policy = policy != null ? policy : ClonePolicy.STANDARD;
        return ctx;
    }

    // ==================== Clone Tracking | 克隆跟踪 ====================

    /**
     * Gets the map of cloned objects
     * 获取已克隆对象的映射
     *
     * @return the original-to-clone mapping | 原始对象到克隆对象的映射
     */
    public Map<Object, Object> getClonedObjects() {
        return Collections.unmodifiableMap(clonedObjects);
    }

    /**
     * Checks if an object has already been cloned
     * 检查对象是否已被克隆
     *
     * @param original the original object | 原始对象
     * @return true if already cloned | 如果已克隆返回true
     */
    public boolean isCloned(Object original) {
        return clonedObjects.containsKey(original);
    }

    /**
     * Gets the cloned copy of an object
     * 获取对象的克隆副本
     *
     * @param original the original object | 原始对象
     * @param <T>      the object type | 对象类型
     * @return the cloned copy, or null if not cloned | 克隆副本，如果未克隆则为null
     */
    @SuppressWarnings("unchecked")
    public <T> T getCloned(Object original) {
        return (T) clonedObjects.get(original);
    }

    /**
     * Registers a cloned object mapping
     * 注册克隆对象映射
     *
     * @param original the original object | 原始对象
     * @param cloned   the cloned object | 克隆对象
     */
    public void registerCloned(Object original, Object cloned) {
        clonedObjects.put(original, cloned);
        objectsCloned++;
    }

    /**
     * Increments the skipped objects count
     * 增加跳过对象计数
     */
    public void incrementSkipped() {
        objectsSkipped++;
    }

    // ==================== Depth Tracking | 深度跟踪 ====================

    /**
     * Gets the current clone depth
     * 获取当前克隆深度
     *
     * @return the current depth | 当前深度
     */
    public int getDepth() {
        return depth;
    }

    /**
     * Gets the maximum allowed depth
     * 获取允许的最大深度
     *
     * @return the max depth | 最大深度
     */
    public int getMaxDepth() {
        return maxDepth;
    }

    /**
     * Increments the depth and returns the new value
     * 增加深度并返回新值
     *
     * @return the new depth | 新深度
     */
    public int incrementDepth() {
        depth++;
        if (depth > maxDepthReached) {
            maxDepthReached = depth;
        }
        return depth;
    }

    /**
     * Decrements the depth and returns the new value
     * 减少深度并返回新值
     *
     * @return the new depth | 新深度
     */
    public int decrementDepth() {
        return --depth;
    }

    /**
     * Checks if max depth has been exceeded
     * 检查是否超过最大深度
     *
     * @return true if max depth exceeded | 如果超过最大深度返回true
     */
    public boolean isMaxDepthExceeded() {
        return depth > maxDepth;
    }

    // ==================== Path Tracking | 路径跟踪 ====================

    /**
     * Gets the current clone path
     * 获取当前克隆路径
     *
     * @return the path as a list | 路径列表
     */
    public List<String> getPath() {
        return new ArrayList<>(path);
    }

    /**
     * Gets the current path as a string
     * 获取当前路径字符串
     *
     * @return the path string | 路径字符串
     */
    public String getPathString() {
        return String.join(".", path);
    }

    /**
     * Pushes an element onto the path
     * 将元素压入路径
     *
     * @param element the path element | 路径元素
     */
    public void pushPath(String element) {
        path.push(element);
    }

    /**
     * Pops an element from the path
     * 从路径弹出元素
     */
    public void popPath() {
        if (!path.isEmpty()) {
            path.pop();
        }
    }

    // ==================== Policy & Warnings | 策略与警告 ====================

    /**
     * Gets the clone policy
     * 获取克隆策略
     *
     * @return the policy | 策略
     */
    public ClonePolicy getPolicy() {
        return policy;
    }

    /**
     * Checks if the policy is lenient
     * 检查策略是否为宽松模式
     *
     * @return true if lenient | 如果是宽松模式返回true
     */
    public boolean isLenient() {
        return policy == ClonePolicy.LENIENT;
    }

    /**
     * Checks if the policy is strict
     * 检查策略是否为严格模式
     *
     * @return true if strict | 如果是严格模式返回true
     */
    public boolean isStrict() {
        return policy == ClonePolicy.STRICT;
    }

    /**
     * Adds a warning message (used in LENIENT mode)
     * 添加警告消息（用于宽松模式）
     *
     * @param warning the warning message | 警告消息
     */
    public void addWarning(String warning) {
        warnings.add(warning);
    }

    /**
     * Gets all warning messages
     * 获取所有警告消息
     *
     * @return unmodifiable list of warnings | 不可修改的警告列表
     */
    public java.util.List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    // ==================== Statistics | 统计 ====================

    /**
     * Gets clone statistics
     * 获取克隆统计信息
     *
     * @return the statistics | 统计信息
     */
    public CloneStatistics getStatistics() {
        return new CloneStatistics(
                objectsCloned,
                objectsSkipped,
                maxDepthReached,
                System.nanoTime() - startTime
        );
    }

    /**
     * Clone statistics record
     * 克隆统计信息记录
     *
     * @param objectsCloned    number of objects cloned | 克隆的对象数
     * @param objectsSkipped   number of objects skipped (immutable) | 跳过的对象数（不可变）
     * @param maxDepthReached  maximum depth reached | 达到的最大深度
     * @param elapsedNanos     elapsed time in nanoseconds | 经过的纳秒时间
     */
    public record CloneStatistics(
            int objectsCloned,
            int objectsSkipped,
            int maxDepthReached,
            long elapsedNanos
    ) {
        /**
         * Gets elapsed time in milliseconds
         * 获取经过的毫秒时间
         *
         * @return elapsed milliseconds | 经过的毫秒
         */
        public double elapsedMillis() {
            return elapsedNanos / 1_000_000.0;
        }
    }
}
