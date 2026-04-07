package cloud.opencode.base.feature;

import cloud.opencode.base.feature.exception.FeatureNotFoundException;
import cloud.opencode.base.feature.listener.FeatureListener;
import cloud.opencode.base.feature.store.FeatureStore;
import cloud.opencode.base.feature.store.InMemoryFeatureStore;
import cloud.opencode.base.feature.strategy.EnableStrategy;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Open Feature
 * 开放功能
 *
 * <p>Main facade class for feature toggle management.</p>
 * <p>功能开关管理的主外观类。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Feature registration - 功能注册</li>
 *   <li>Feature evaluation - 功能评估</li>
 *   <li>Strategy management - 策略管理</li>
 *   <li>Listener support - 监听器支持</li>
 *   <li>Pluggable storage - 可插拔存储</li>
 *   <li>Group operations - 分组操作</li>
 *   <li>Snapshot/restore - 快照/恢复</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Get singleton instance
 * OpenFeature features = OpenFeature.getInstance();
 *
 * // Register a feature
 * Feature darkMode = Feature.builder("dark-mode")
 *     .name("Dark Mode")
 *     .defaultEnabled(false)
 *     .strategy(new PercentageStrategy(30))
 *     .build();
 * features.register(darkMode);
 *
 * // Check if enabled
 * if (features.isEnabled("dark-mode")) {
 *     // Use dark mode
 * }
 *
 * // Check with context
 * FeatureContext ctx = FeatureContext.builder()
 *     .userId("user123")
 *     .build();
 * if (features.isEnabled("dark-mode", ctx)) {
 *     // Use dark mode for this user
 * }
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Partial (validates inputs) - 空值安全: 部分（验证输入）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.3
 */
public class OpenFeature {

    private static final Logger LOGGER = Logger.getLogger(OpenFeature.class.getName());
    private static volatile OpenFeature instance;
    private static final Object LOCK = new Object();

    private volatile FeatureStore store;
    private final List<FeatureListener> listeners;

    /**
     * Private constructor for singleton
     * 单例的私有构造函数
     */
    private OpenFeature() {
        this(new InMemoryFeatureStore());
    }

    /**
     * Constructor with custom store
     * 使用自定义存储的构造函数
     *
     * @param store the feature store | 功能存储
     */
    private OpenFeature(FeatureStore store) {
        this.store = store;
        this.listeners = new CopyOnWriteArrayList<>();
    }

    /**
     * Get singleton instance
     * 获取单例实例
     *
     * @return the OpenFeature instance | OpenFeature实例
     */
    public static OpenFeature getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new OpenFeature();
                }
            }
        }
        return instance;
    }

    /**
     * Create new instance with custom store (for testing or custom usage)
     * 使用自定义存储创建新实例（用于测试或自定义用途）
     *
     * @param store the feature store | 功能存储
     * @return new OpenFeature instance | 新的OpenFeature实例
     */
    public static OpenFeature create(FeatureStore store) {
        return new OpenFeature(store);
    }

    /**
     * Reset singleton instance (for testing)
     * 重置单例实例（用于测试）
     */
    public static void resetInstance() {
        synchronized (LOCK) {
            instance = null;
        }
    }

    /**
     * Register a feature
     * 注册功能
     *
     * @param feature the feature to register | 要注册的功能
     */
    public void register(Feature feature) {
        store.save(feature);
    }

    /**
     * Register multiple features
     * 注册多个功能
     *
     * @param features the features to register | 要注册的功能
     */
    public void registerAll(Feature... features) {
        for (Feature feature : features) {
            store.save(feature);
        }
    }

    /**
     * Check if a feature is enabled
     * 检查功能是否启用
     *
     * @param key the feature key | 功能键
     * @return true if enabled | 如果启用返回true
     */
    public boolean isEnabled(String key) {
        return isEnabled(key, FeatureContext.empty());
    }

    /**
     * Check if a feature is enabled for a context
     * 检查功能对上下文是否启用
     *
     * @param key     the feature key | 功能键
     * @param context the evaluation context | 评估上下文
     * @return true if enabled | 如果启用返回true
     */
    public boolean isEnabled(String key, FeatureContext context) {
        Optional<Feature> featureOpt = store.find(key);
        if (featureOpt.isEmpty()) {
            return false;
        }

        Feature feature = featureOpt.get();
        return feature.isEnabled(context);
    }

    /**
     * Check if a feature is enabled for a specific user
     * 检查功能对特定用户是否启用
     *
     * @param key    the feature key | 功能键
     * @param userId the user id | 用户ID
     * @return true if enabled | 如果启用返回true
     */
    public boolean isEnabledForUser(String key, String userId) {
        return isEnabled(key, FeatureContext.builder().userId(userId).build());
    }

    /**
     * Execute action if feature is enabled
     * 如果功能启用则执行操作
     *
     * @param key    the feature key | 功能键
     * @param action the action to execute | 要执行的操作
     */
    public void ifEnabled(String key, Runnable action) {
        if (isEnabled(key)) {
            action.run();
        }
    }

    /**
     * Execute action if feature is enabled with context
     * 如果功能启用则执行操作（带上下文）
     *
     * @param key     the feature key | 功能键
     * @param context the evaluation context | 评估上下文
     * @param action  the action to execute | 要执行的操作
     */
    public void ifEnabled(String key, FeatureContext context, Runnable action) {
        if (isEnabled(key, context)) {
            action.run();
        }
    }

    /**
     * Get value based on feature state
     * 根据功能状态获取值
     *
     * @param key      the feature key | 功能键
     * @param enabled  supplier for enabled state | 启用状态的供应者
     * @param disabled supplier for disabled state | 禁用状态的供应者
     * @param <T>      the return type | 返回类型
     * @return the result | 结果
     */
    public <T> T ifEnabled(String key, Supplier<T> enabled, Supplier<T> disabled) {
        return isEnabled(key) ? enabled.get() : disabled.get();
    }

    /**
     * Get value based on feature state with context
     * 根据功能状态获取值（带上下文）
     *
     * @param key      the feature key | 功能键
     * @param context  the evaluation context | 评估上下文
     * @param enabled  supplier for enabled state | 启用状态的供应者
     * @param disabled supplier for disabled state | 禁用状态的供应者
     * @param <T>      the return type | 返回类型
     * @return the result | 结果
     */
    public <T> T ifEnabled(String key, FeatureContext context, Supplier<T> enabled, Supplier<T> disabled) {
        return isEnabled(key, context) ? enabled.get() : disabled.get();
    }

    /**
     * Get a feature by key
     * 根据键获取功能
     *
     * @param key the feature key | 功能键
     * @return optional containing feature | 包含功能的Optional
     */
    public Optional<Feature> get(String key) {
        return store.find(key);
    }

    /**
     * Get a feature or throw if not found
     * 获取功能，如果未找到则抛出异常
     *
     * @param key the feature key | 功能键
     * @return the feature | 功能
     * @throws FeatureNotFoundException if not found | 如果未找到
     */
    public Feature getOrThrow(String key) {
        return store.find(key)
            .orElseThrow(() -> new FeatureNotFoundException(key));
    }

    /**
     * Enable a feature
     * 启用功能
     *
     * @param key the feature key | 功能键
     */
    public void enable(String key) {
        Feature feature = getOrThrow(key);
        boolean oldValue = feature.isEnabled();

        Feature updated = new Feature(
            feature.key(),
            feature.name(),
            feature.description(),
            true,
            feature.strategy(),
            feature.metadata(),
            feature.group(),
            feature.expiresAt(),
            feature.lifecycle(),
            feature.createdAt(),
            Instant.now()
        );

        store.save(updated);
        notifyListeners(key, oldValue, true);
    }

    /**
     * Disable a feature
     * 禁用功能
     *
     * @param key the feature key | 功能键
     */
    public void disable(String key) {
        Feature feature = getOrThrow(key);
        boolean oldValue = feature.isEnabled();

        Feature updated = new Feature(
            feature.key(),
            feature.name(),
            feature.description(),
            false,
            feature.strategy(),
            feature.metadata(),
            feature.group(),
            feature.expiresAt(),
            feature.lifecycle(),
            feature.createdAt(),
            Instant.now()
        );

        store.save(updated);
        notifyListeners(key, oldValue, false);
    }

    /**
     * Update feature strategy
     * 更新功能策略
     *
     * @param key      the feature key | 功能键
     * @param strategy the new strategy | 新策略
     */
    public void updateStrategy(String key, EnableStrategy strategy) {
        Feature feature = getOrThrow(key);
        boolean oldValue = feature.isEnabled();

        Feature updated = new Feature(
            feature.key(),
            feature.name(),
            feature.description(),
            feature.defaultEnabled(),
            strategy,
            feature.metadata(),
            feature.group(),
            feature.expiresAt(),
            feature.lifecycle(),
            feature.createdAt(),
            Instant.now()
        );

        store.save(updated);

        boolean newValue = updated.isEnabled();
        if (oldValue != newValue) {
            notifyListeners(key, oldValue, newValue);
        }
    }

    /**
     * Delete a feature
     * 删除功能
     *
     * @param key the feature key | 功能键
     * @return true if deleted | 如果删除返回true
     */
    public boolean delete(String key) {
        Optional<Feature> featureOpt = store.find(key);
        if (featureOpt.isPresent()) {
            boolean oldValue = featureOpt.get().isEnabled();
            store.delete(key);
            notifyListeners(key, oldValue, false);
            return true;
        }
        return false;
    }

    /**
     * Get all feature keys
     * 获取所有功能键
     *
     * @return set of feature keys | 功能键集合
     */
    public Set<String> getAllKeys() {
        return store.findAll().stream()
            .map(Feature::key)
            .collect(java.util.stream.Collectors.toSet());
    }

    /**
     * Get all features
     * 获取所有功能
     *
     * @return map of key to feature | 键到功能的映射
     */
    public Map<String, Feature> getAll() {
        return store.findAll().stream()
            .collect(java.util.stream.Collectors.toMap(Feature::key, f -> f));
    }

    /**
     * Check if a feature exists
     * 检查功能是否存在
     *
     * @param key the feature key | 功能键
     * @return true if exists | 如果存在返回true
     */
    public boolean exists(String key) {
        return store.exists(key);
    }

    /**
     * Get the feature count
     * 获取功能数量
     *
     * @return number of features | 功能数量
     */
    public int size() {
        return store.count();
    }

    /**
     * Clear all features
     * 清空所有功能
     */
    public void clear() {
        store.clear();
    }

    /**
     * Add a listener
     * 添加监听器
     *
     * @param listener the listener | 监听器
     */
    public void addListener(FeatureListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    /**
     * Remove a listener
     * 移除监听器
     *
     * @param listener the listener | 监听器
     */
    public void removeListener(FeatureListener listener) {
        listeners.remove(listener);
    }

    /**
     * Get the feature store
     * 获取功能存储
     *
     * @return the feature store | 功能存储
     */
    public FeatureStore getStore() {
        return store;
    }

    /**
     * Set the feature store
     * 设置功能存储
     *
     * <p>Allows switching to a different feature store at runtime.
     * Existing features will not be migrated automatically.</p>
     * <p>允许在运行时切换到不同的功能存储。现有功能不会自动迁移。</p>
     *
     * @param store the new feature store | 新的功能存储
     * @throws NullPointerException if store is null | 如果store为null
     */
    public void setStore(FeatureStore store) {
        if (store == null) {
            throw new NullPointerException("store must not be null");
        }
        this.store = store;
    }

    // ==================== Group Operations | 分组操作 ====================

    /**
     * Get all features in a group
     * 获取组中的所有功能
     *
     * @param group the group name | 组名称
     * @return list of features in the group | 组中的功能列表
     */
    public List<Feature> getByGroup(String group) {
        if (group == null) {
            return List.of();
        }
        return store.findAll().stream()
            .filter(f -> group.equals(f.group()))
            .toList();
    }

    /**
     * Enable all features in a group
     * 启用组中的所有功能
     *
     * @param group the group name | 组名称
     */
    public void enableGroup(String group) {
        if (group == null) {
            throw new NullPointerException("group must not be null");
        }
        getByGroup(group).forEach(f -> enable(f.key()));
    }

    /**
     * Disable all features in a group
     * 禁用组中的所有功能
     *
     * @param group the group name | 组名称
     */
    public void disableGroup(String group) {
        if (group == null) {
            throw new NullPointerException("group must not be null");
        }
        getByGroup(group).forEach(f -> disable(f.key()));
    }

    // ==================== Snapshot Operations | 快照操作 ====================

    /**
     * Create a snapshot of current feature state
     * 创建当前功能状态的快照
     *
     * @return the snapshot | 快照
     */
    public FeatureSnapshot snapshot() {
        Map<String, Feature> features = getAll();
        return new FeatureSnapshot(features, Instant.now());
    }

    /**
     * Restore feature state from a snapshot
     * 从快照恢复功能状态
     *
     * @param snapshot the snapshot to restore | 要恢复的快照
     */
    public void restore(FeatureSnapshot snapshot) {
        if (snapshot == null) {
            throw new NullPointerException("snapshot must not be null");
        }
        // Atomic swap: build a new store, then assign it in one step
        InMemoryFeatureStore newStore = new InMemoryFeatureStore();
        snapshot.features().values().forEach(newStore::save);
        this.store = newStore;
    }

    /**
     * Notify listeners of feature change
     * 通知监听器功能变更
     */
    private void notifyListeners(String key, boolean oldValue, boolean newValue) {
        for (FeatureListener listener : listeners) {
            try {
                listener.onFeatureChanged(key, oldValue, newValue);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING,
                    "Feature listener {0} failed for key ''{1}'': {2}",
                    new Object[]{listener.getClass().getName(), key, e.getMessage()});
            }
        }
    }
}
