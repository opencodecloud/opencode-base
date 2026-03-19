package cloud.opencode.base.feature.store;

import cloud.opencode.base.feature.Feature;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Redis Feature Store
 * Redis功能存储
 *
 * <p>Distributed storage using Redis (requires external Redis client).</p>
 * <p>使用Redis的分布式存储（需要外部Redis客户端）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Distributed storage - 分布式存储</li>
 *   <li>TTL support - TTL支持</li>
 *   <li>Cluster support - 集群支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // With custom Redis client
 * RedisFeatureStore store = new RedisFeatureStore(
 *     "feature:",
 *     Duration.ofHours(1),
 *     (key, feature) -> redisClient.set(key, serialize(feature)),
 *     key -> deserialize(redisClient.get(key)),
 *     () -> redisClient.keys("feature:*").stream()
 *         .map(k -> deserialize(redisClient.get(k)))
 *         .toList(),
 *     key -> redisClient.del(key),
 *     () -> redisClient.keys("feature:*").forEach(redisClient::del)
 * );
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
 * @since JDK 25, opencode-base-feature V1.0.0
 */
public class RedisFeatureStore implements FeatureStore {

    private final String keyPrefix;
    private final Duration ttl;
    private final BiConsumer<String, Feature> setter;
    private final Function<String, Feature> getter;
    private final java.util.function.Supplier<List<Feature>> listAll;
    private final Function<String, Boolean> deleter;
    private final Runnable clearer;

    /**
     * Create Redis store with custom operations
     * 使用自定义操作创建Redis存储
     *
     * @param keyPrefix the key prefix | 键前缀
     * @param ttl       the TTL for features | 功能的TTL
     * @param setter    function to set feature | 设置功能的函数
     * @param getter    function to get feature | 获取功能的函数
     * @param listAll   function to list all features | 列出所有功能的函数
     * @param deleter   function to delete feature | 删除功能的函数
     * @param clearer   function to clear all | 清除所有的函数
     */
    public RedisFeatureStore(
            String keyPrefix,
            Duration ttl,
            BiConsumer<String, Feature> setter,
            Function<String, Feature> getter,
            java.util.function.Supplier<List<Feature>> listAll,
            Function<String, Boolean> deleter,
            Runnable clearer) {
        this.keyPrefix = keyPrefix != null ? keyPrefix : "feature:";
        this.ttl = ttl != null ? ttl : Duration.ofHours(1);
        this.setter = setter;
        this.getter = getter;
        this.listAll = listAll;
        this.deleter = deleter;
        this.clearer = clearer;
    }

    @Override
    public void save(Feature feature) {
        if (feature == null || feature.key() == null) {
            throw new IllegalArgumentException("Feature and key cannot be null");
        }
        setter.accept(keyPrefix + feature.key(), feature);
    }

    @Override
    public Optional<Feature> find(String key) {
        if (key == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(getter.apply(keyPrefix + key));
    }

    @Override
    public List<Feature> findAll() {
        return listAll.get();
    }

    @Override
    public boolean delete(String key) {
        if (key == null) {
            return false;
        }
        return deleter.apply(keyPrefix + key);
    }

    @Override
    public void clear() {
        clearer.run();
    }

    /**
     * Get the key prefix
     * 获取键前缀
     *
     * @return key prefix | 键前缀
     */
    public String getKeyPrefix() {
        return keyPrefix;
    }

    /**
     * Get the TTL
     * 获取TTL
     *
     * @return TTL | TTL
     */
    public Duration getTtl() {
        return ttl;
    }
}
