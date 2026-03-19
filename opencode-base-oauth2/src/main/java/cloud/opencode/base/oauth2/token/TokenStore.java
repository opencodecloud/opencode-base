package cloud.opencode.base.oauth2.token;

import cloud.opencode.base.oauth2.OAuth2Token;

import java.util.Optional;
import java.util.Set;

/**
 * Token Store Interface
 * Token 存储接口
 *
 * <p>Defines the contract for storing and retrieving OAuth2 tokens.</p>
 * <p>定义存储和检索 OAuth2 令牌的契约。</p>
 *
 * <p><strong>Implementations | 实现:</strong></p>
 * <ul>
 *   <li>{@link InMemoryTokenStore} - Memory storage - 内存存储</li>
 *   <li>{@link FileTokenStore} - File-based storage - 文件存储</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Use in-memory store
 * TokenStore store = new InMemoryTokenStore();
 * store.save("user-1", token);
 *
 * // Retrieve token
 * Optional<OAuth2Token> token = store.load("user-1");
 *
 * // Custom implementation
 * TokenStore redisStore = new TokenStore() {
 *     @Override
 *     public void save(String key, OAuth2Token token) {
 *         redis.set("oauth2:" + key, serialize(token));
 *     }
 *     // ... other methods
 * };
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>SPI for OAuth2 token persistence - OAuth2令牌持久化的SPI接口</li>
 *   <li>Pluggable storage backend support - 可插拔的存储后端支持</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
public interface TokenStore {

    /**
     * Save a token with the given key
     * 使用给定的键保存令牌
     *
     * @param key   the storage key | 存储键
     * @param token the token to save | 要保存的令牌
     */
    void save(String key, OAuth2Token token);

    /**
     * Load a token by key
     * 通过键加载令牌
     *
     * @param key the storage key | 存储键
     * @return the token if found | 找到的令牌
     */
    Optional<OAuth2Token> load(String key);

    /**
     * Delete a token by key
     * 通过键删除令牌
     *
     * @param key the storage key | 存储键
     */
    void delete(String key);

    /**
     * Delete all stored tokens
     * 删除所有存储的令牌
     */
    void deleteAll();

    /**
     * Check if a token exists
     * 检查令牌是否存在
     *
     * @param key the storage key | 存储键
     * @return true if exists | 存在返回 true
     */
    default boolean exists(String key) {
        return load(key).isPresent();
    }

    /**
     * Get all stored keys
     * 获取所有存储的键
     *
     * @return the set of keys | 键集合
     */
    default Set<String> keys() {
        return Set.of();
    }

    /**
     * Get the number of stored tokens
     * 获取存储的令牌数量
     *
     * @return the count | 数量
     */
    default int size() {
        return keys().size();
    }
}
