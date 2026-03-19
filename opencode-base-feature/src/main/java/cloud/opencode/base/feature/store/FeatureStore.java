package cloud.opencode.base.feature.store;

import cloud.opencode.base.feature.Feature;

import java.util.List;
import java.util.Optional;

/**
 * Feature Store Interface
 * 功能存储接口
 *
 * <p>Interface for storing and retrieving feature definitions.</p>
 * <p>用于存储和检索功能定义的接口。</p>
 *
 * <p><strong>Implementations | 实现:</strong></p>
 * <ul>
 *   <li>{@link InMemoryFeatureStore} - In-memory storage | 内存存储</li>
 *   <li>{@link FileFeatureStore} - File-based storage | 文件存储</li>
 *   <li>{@link LruFeatureStore} - LRU cache storage | LRU缓存存储</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * FeatureStore store = new InMemoryFeatureStore();
 * store.save(Feature.builder("feature-1").alwaysOn().build());
 *
 * Optional<Feature> feature = store.find("feature-1");
 * List<Feature> all = store.findAll();
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>CRUD operations for feature flag persistence - 功能标志持久化的CRUD操作</li>
 *   <li>Listener support for change notification - 变更通知的监听器支持</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
public interface FeatureStore {

    /**
     * Save a feature
     * 保存功能
     *
     * @param feature the feature to save | 要保存的功能
     */
    void save(Feature feature);

    /**
     * Find a feature by key
     * 根据键查找功能
     *
     * @param key the feature key | 功能键
     * @return optional containing feature if found | 如果找到则包含功能的Optional
     */
    Optional<Feature> find(String key);

    /**
     * Find all features
     * 查找所有功能
     *
     * @return list of all features | 所有功能的列表
     */
    List<Feature> findAll();

    /**
     * Delete a feature
     * 删除功能
     *
     * @param key the feature key | 功能键
     * @return true if deleted | 如果删除成功返回true
     */
    boolean delete(String key);

    /**
     * Check if a feature exists
     * 检查功能是否存在
     *
     * @param key the feature key | 功能键
     * @return true if exists | 如果存在返回true
     */
    default boolean exists(String key) {
        return find(key).isPresent();
    }

    /**
     * Get the count of features
     * 获取功能数量
     *
     * @return feature count | 功能数量
     */
    default int count() {
        return findAll().size();
    }

    /**
     * Clear all features
     * 清除所有功能
     */
    void clear();
}
