package cloud.opencode.base.oauth2.token;

import cloud.opencode.base.oauth2.OAuth2Token;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-Memory Token Store
 * 内存 Token 存储
 *
 * <p>Thread-safe in-memory implementation of TokenStore.</p>
 * <p>TokenStore 的线程安全内存实现。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Thread-safe with ConcurrentHashMap - 使用 ConcurrentHashMap 线程安全</li>
 *   <li>Fast access - 快速访问</li>
 *   <li>No persistence - 无持久化</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TokenStore store = new InMemoryTokenStore();
 *
 * // Save token
 * store.save("user-1", token);
 *
 * // Load token
 * Optional<OAuth2Token> loaded = store.load("user-1");
 *
 * // Delete token
 * store.delete("user-1");
 * }</pre>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <p>This class is thread-safe.</p>
 * <p>此类是线程安全的。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
public class InMemoryTokenStore implements TokenStore {

    private final ConcurrentHashMap<String, OAuth2Token> tokens = new ConcurrentHashMap<>();

    /**
     * Create a new in-memory token store
     * 创建新的内存令牌存储
     */
    public InMemoryTokenStore() {
        // Default constructor
    }

    @Override
    public void save(String key, OAuth2Token token) {
        if (key == null || token == null) {
            throw new IllegalArgumentException("key and token cannot be null");
        }
        tokens.put(key, token);
    }

    @Override
    public Optional<OAuth2Token> load(String key) {
        if (key == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(tokens.get(key));
    }

    @Override
    public void delete(String key) {
        if (key != null) {
            tokens.remove(key);
        }
    }

    @Override
    public void deleteAll() {
        tokens.clear();
    }

    @Override
    public boolean exists(String key) {
        return key != null && tokens.containsKey(key);
    }

    @Override
    public Set<String> keys() {
        return Set.copyOf(tokens.keySet());
    }

    @Override
    public int size() {
        return tokens.size();
    }

    /**
     * Remove expired tokens
     * 移除过期的令牌
     *
     * @return the number of tokens removed | 移除的令牌数量
     */
    public int removeExpired() {
        int removed = 0;
        var iterator = tokens.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (entry.getValue().isExpired()) {
                iterator.remove();
                removed++;
            }
        }
        return removed;
    }
}
