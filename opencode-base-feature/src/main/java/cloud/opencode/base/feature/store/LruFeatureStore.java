package cloud.opencode.base.feature.store;

import cloud.opencode.base.feature.Feature;

import java.util.*;

/**
 * LRU Feature Store
 * LRU功能存储
 *
 * <p>In-memory store with LRU eviction policy.</p>
 * <p>带有LRU淘汰策略的内存存储。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>LRU eviction - LRU淘汰</li>
 *   <li>Capacity limit - 容量限制</li>
 *   <li>Thread-safe - 线程安全</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Store with max 100 features
 * FeatureStore store = new LruFeatureStore(100);
 *
 * // When capacity exceeded, least recently used features are evicted
 * for (int i = 0; i < 150; i++) {
 *     store.save(Feature.builder("feature-" + i).build());
 * }
 * // Only 100 most recent features remain
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
public class LruFeatureStore implements FeatureStore {

    private final Map<String, Feature> store;
    private final int maxSize;

    /**
     * Create LRU store with max size
     * 创建具有最大大小的LRU存储
     *
     * @param maxSize the maximum number of features | 最大功能数
     */
    public LruFeatureStore(int maxSize) {
        this.maxSize = maxSize;
        this.store = Collections.synchronizedMap(
            new LinkedHashMap<>(maxSize, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Feature> eldest) {
                    return size() > maxSize;
                }
            }
        );
    }

    @Override
    public void save(Feature feature) {
        if (feature == null || feature.key() == null) {
            throw new IllegalArgumentException("Feature and key cannot be null");
        }
        store.put(feature.key(), feature);
    }

    @Override
    public Optional<Feature> find(String key) {
        if (key == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(store.get(key));
    }

    @Override
    public List<Feature> findAll() {
        synchronized (store) {
            return List.copyOf(store.values());
        }
    }

    @Override
    public boolean delete(String key) {
        if (key == null) {
            return false;
        }
        return store.remove(key) != null;
    }

    @Override
    public boolean exists(String key) {
        return key != null && store.containsKey(key);
    }

    @Override
    public int count() {
        return store.size();
    }

    @Override
    public void clear() {
        store.clear();
    }

    /**
     * Get the maximum size
     * 获取最大大小
     *
     * @return max size | 最大大小
     */
    public int getMaxSize() {
        return maxSize;
    }
}
