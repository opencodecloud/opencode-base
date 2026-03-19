package cloud.opencode.base.feature.store;

import cloud.opencode.base.feature.Feature;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-Memory Feature Store
 * 内存功能存储
 *
 * <p>Thread-safe in-memory implementation of FeatureStore.</p>
 * <p>FeatureStore的线程安全内存实现。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Thread-safe operations - 线程安全操作</li>
 *   <li>Fast lookups - 快速查找</li>
 *   <li>No persistence - 无持久化</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * FeatureStore store = new InMemoryFeatureStore();
 * store.save(Feature.builder("feature-1").alwaysOn().build());
 * store.save(Feature.builder("feature-2").percentage(50).build());
 *
 * boolean exists = store.exists("feature-1"); // true
 * int count = store.count(); // 2
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
public class InMemoryFeatureStore implements FeatureStore {

    private final Map<String, Feature> features;

    /**
     * Create in-memory store
     * 创建内存存储
     */
    public InMemoryFeatureStore() {
        this.features = new ConcurrentHashMap<>();
    }

    @Override
    public void save(Feature feature) {
        if (feature == null || feature.key() == null) {
            throw new IllegalArgumentException("Feature and key cannot be null");
        }
        features.put(feature.key(), feature);
    }

    @Override
    public Optional<Feature> find(String key) {
        if (key == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(features.get(key));
    }

    @Override
    public List<Feature> findAll() {
        return List.copyOf(features.values());
    }

    @Override
    public boolean delete(String key) {
        if (key == null) {
            return false;
        }
        return features.remove(key) != null;
    }

    @Override
    public boolean exists(String key) {
        return key != null && features.containsKey(key);
    }

    @Override
    public int count() {
        return features.size();
    }

    @Override
    public void clear() {
        features.clear();
    }
}
