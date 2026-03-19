package cloud.opencode.base.oauth2.token;

import cloud.opencode.base.oauth2.OAuth2Token;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Token Manager Interface
 * Token 管理接口
 *
 * <p>Manages OAuth2 tokens with automatic refresh capabilities.</p>
 * <p>管理 OAuth2 令牌，具有自动刷新功能。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Token storage and retrieval - Token 存储和检索</li>
 *   <li>Automatic token refresh - 自动 Token 刷新</li>
 *   <li>Token expiration handling - Token 过期处理</li>
 *   <li>Multi-user token management - 多用户 Token 管理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create token manager
 * TokenManager manager = new DefaultTokenManager(tokenStore, refresher);
 *
 * // Store token
 * manager.store("user-1", token);
 *
 * // Get valid token (auto-refresh if needed)
 * OAuth2Token validToken = manager.getValidToken("user-1");
 *
 * // Remove token
 * manager.remove("user-1");
 * }</pre>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <p>Implementations should be thread-safe.</p>
 * <p>实现应该是线程安全的。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
public interface TokenManager extends AutoCloseable {

    /**
     * Store a token
     * 存储 Token
     *
     * @param key   the storage key (e.g., user ID) | 存储键（例如用户 ID）
     * @param token the token to store | 要存储的 Token
     */
    void store(String key, OAuth2Token token);

    /**
     * Get a stored token
     * 获取存储的 Token
     *
     * @param key the storage key | 存储键
     * @return the token if found | 找到的 Token
     */
    Optional<OAuth2Token> get(String key);

    /**
     * Get a valid token, refreshing if necessary
     * 获取有效的 Token，必要时刷新
     *
     * <p>This method will:</p>
     * <ul>
     *   <li>Return the stored token if it's still valid</li>
     *   <li>Refresh the token if it's expiring soon and has a refresh token</li>
     *   <li>Throw an exception if the token is expired and cannot be refreshed</li>
     * </ul>
     *
     * @param key the storage key | 存储键
     * @return the valid token | 有效的 Token
     * @throws cloud.opencode.base.oauth2.exception.OAuth2Exception if no token found or refresh fails |
     *         如果未找到 Token 或刷新失败
     */
    OAuth2Token getValidToken(String key);

    /**
     * Get a valid token or obtain a new one
     * 获取有效的 Token 或获取新的
     *
     * <p>If no token exists for the key, the supplier is called to obtain a new token.</p>
     *
     * @param key           the storage key | 存储键
     * @param tokenSupplier supplier to obtain new token if needed | 需要时获取新 Token 的提供者
     * @return the valid token | 有效的 Token
     */
    OAuth2Token getOrObtain(String key, Supplier<OAuth2Token> tokenSupplier);

    /**
     * Check if a token exists for the key
     * 检查是否存在该键的 Token
     *
     * @param key the storage key | 存储键
     * @return true if token exists | 如果存在 Token 返回 true
     */
    boolean exists(String key);

    /**
     * Check if a valid token exists for the key
     * 检查是否存在该键的有效 Token
     *
     * @param key the storage key | 存储键
     * @return true if valid token exists | 如果存在有效 Token 返回 true
     */
    boolean hasValidToken(String key);

    /**
     * Remove a token
     * 移除 Token
     *
     * @param key the storage key | 存储键
     */
    void remove(String key);

    /**
     * Clear all tokens
     * 清除所有 Token
     */
    void clear();

    /**
     * Get the number of stored tokens
     * 获取存储的 Token 数量
     *
     * @return the token count | Token 数量
     */
    int size();

    /**
     * Close the token manager and release resources
     * 关闭 Token 管理器并释放资源
     */
    @Override
    void close();
}
