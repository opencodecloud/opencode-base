package cloud.opencode.base.oauth2.token;

import cloud.opencode.base.oauth2.OAuth2Token;
import cloud.opencode.base.oauth2.exception.OAuth2ErrorCode;
import cloud.opencode.base.oauth2.exception.OAuth2Exception;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Default Token Manager Implementation
 * 默认 Token 管理器实现
 *
 * <p>Thread-safe implementation of {@link TokenManager} that delegates to
 * {@link TokenStore} for persistence and {@link TokenRefresher} for refresh operations.
 * Uses per-key locking to prevent thundering herd during token refresh.</p>
 * <p>线程安全的 {@link TokenManager} 实现，委托给 {@link TokenStore} 进行持久化，
 * 委托给 {@link TokenRefresher} 进行刷新操作。
 * 使用按键锁定防止 Token 刷新时的惊群效应。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Thread-safe token storage and retrieval - 线程安全的 Token 存储和检索</li>
 *   <li>Automatic token refresh with de-duplication - 带去重的自动 Token 刷新</li>
 *   <li>Per-key locking to prevent thundering herd - 按键锁定防止惊群</li>
 *   <li>Builder pattern for construction - 构建器模式用于构建</li>
 *   <li>Resource lifecycle management - 资源生命周期管理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Build a token manager
 * DefaultTokenManager manager = DefaultTokenManager.builder()
 *     .tokenStore(new InMemoryTokenStore())
 *     .refresher(refresher)
 *     .refreshThreshold(Duration.ofMinutes(5))
 *     .build();
 *
 * // Store and retrieve tokens
 * manager.store("user-1", token);
 * OAuth2Token validToken = manager.getValidToken("user-1");
 *
 * // Get or obtain
 * OAuth2Token token = manager.getOrObtain("user-1", () -> obtainNewToken());
 *
 * // Clean up
 * manager.close();
 * }</pre>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <p>This class is thread-safe. Concurrent requests for the same key will share
 * a single refresh operation rather than triggering multiple refreshes.</p>
 * <p>此类是线程安全的。对同一键的并发请求将共享单个刷新操作，
 * 而不是触发多次刷新。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.3
 */
public final class DefaultTokenManager implements TokenManager {

    private final TokenStore tokenStore;
    private final TokenRefresher refresher;
    private final boolean ownsRefresher;
    private final Duration refreshThreshold;
    private final ConcurrentHashMap<String, ReentrantLock> refreshLocks;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * Create a DefaultTokenManager via the Builder.
     * 通过 Builder 创建 DefaultTokenManager。
     *
     * @param builder the builder | 构建器
     */
    private DefaultTokenManager(Builder builder) {
        this.tokenStore = Objects.requireNonNull(builder.tokenStore, "tokenStore cannot be null");
        this.refresher = builder.refresher;
        this.ownsRefresher = builder.ownsRefresher;
        this.refreshThreshold = builder.refreshThreshold != null
                ? builder.refreshThreshold : Duration.ofMinutes(5);
        this.refreshLocks = new ConcurrentHashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void store(String key, OAuth2Token token) {
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(token, "token cannot be null");
        ensureOpen();
        tokenStore.save(key, token);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<OAuth2Token> get(String key) {
        Objects.requireNonNull(key, "key cannot be null");
        ensureOpen();
        return tokenStore.load(key);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Ensures only one refresh happens per key. Concurrent requests for the same key
     * will wait for the in-progress refresh to complete rather than starting a new one.</p>
     * <p>确保每个键只发生一次刷新。对同一键的并发请求将等待正在进行的刷新完成，
     * 而不是启动新的刷新。</p>
     */
    @Override
    public OAuth2Token getValidToken(String key) {
        Objects.requireNonNull(key, "key cannot be null");
        ensureOpen();

        Optional<OAuth2Token> optToken = tokenStore.load(key);
        if (optToken.isEmpty()) {
            throw new OAuth2Exception(OAuth2ErrorCode.TOKEN_NOT_FOUND,
                    "No token found for key: " + key);
        }

        OAuth2Token token = optToken.get();

        // Token is still valid and not expiring soon
        if (!isRefreshNeeded(token)) {
            return token;
        }

        // Token is expired and cannot be refreshed
        if (token.isExpired() && !token.hasRefreshToken()) {
            throw new OAuth2Exception(OAuth2ErrorCode.TOKEN_EXPIRED,
                    "Token expired and no refresh token available for key: " + key);
        }

        // No refresher configured
        if (refresher == null) {
            if (token.isExpired()) {
                throw new OAuth2Exception(OAuth2ErrorCode.TOKEN_EXPIRED,
                        "Token expired and no refresher configured for key: " + key);
            }
            return token;
        }

        // Refresh with per-key lock to prevent thundering herd
        return refreshWithLock(key, token);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OAuth2Token getOrObtain(String key, Supplier<OAuth2Token> tokenSupplier) {
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(tokenSupplier, "tokenSupplier cannot be null");
        ensureOpen();

        Optional<OAuth2Token> optToken = tokenStore.load(key);
        if (optToken.isPresent()) {
            OAuth2Token token = optToken.get();
            if (!token.isExpired()) {
                if (!isRefreshNeeded(token) || refresher == null) {
                    return token;
                }
                return refreshWithLock(key, token);
            }
            // Token is expired, try refresh first if possible
            if (token.hasRefreshToken() && refresher != null) {
                try {
                    return refreshWithLock(key, token);
                } catch (OAuth2Exception e) {
                    // Refresh failed, fall through to obtain new token
                }
            }
        }

        // Obtain new token atomically with per-key lock
        ReentrantLock lock = refreshLocks.computeIfAbsent(key, k -> new ReentrantLock());
        lock.lock();
        try {
            // Double-check after acquiring lock
            Optional<OAuth2Token> recheck = tokenStore.load(key);
            if (recheck.isPresent() && !recheck.get().isExpired()) {
                return recheck.get();
            }

            OAuth2Token newToken = tokenSupplier.get();
            Objects.requireNonNull(newToken, "tokenSupplier returned null");
            tokenStore.save(key, newToken);
            return newToken;
        } finally {
            lock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists(String key) {
        Objects.requireNonNull(key, "key cannot be null");
        ensureOpen();
        return tokenStore.exists(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasValidToken(String key) {
        Objects.requireNonNull(key, "key cannot be null");
        ensureOpen();
        Optional<OAuth2Token> optToken = tokenStore.load(key);
        return optToken.isPresent() && !optToken.get().isExpired();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(String key) {
        Objects.requireNonNull(key, "key cannot be null");
        ensureOpen();
        tokenStore.delete(key);
        refreshLocks.remove(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        ensureOpen();
        tokenStore.deleteAll();
        refreshLocks.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        ensureOpen();
        return tokenStore.size();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Closes the TokenRefresher if it was created internally by this manager.</p>
     * <p>如果 TokenRefresher 是由此管理器内部创建的，则关闭它。</p>
     */
    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            if (ownsRefresher && refresher != null) {
                refresher.close();
            }
            refreshLocks.clear();
        }
    }

    /**
     * Check if a token needs refresh based on the configured threshold.
     * 根据配置的阈值检查 Token 是否需要刷新。
     *
     * @param token the token | Token
     * @return true if refresh is needed | 如果需要刷新返回 true
     */
    private boolean isRefreshNeeded(OAuth2Token token) {
        if (token.isExpired()) {
            return true;
        }
        return token.hasRefreshToken() && token.isExpiringSoon(refreshThreshold);
    }

    /**
     * Refresh a token with per-key lock to prevent thundering herd.
     * 使用按键锁定刷新 Token 以防止惊群效应。
     *
     * @param key   the storage key | 存储键
     * @param token the token to refresh | 要刷新的 Token
     * @return the refreshed token | 刷新后的 Token
     */
    private OAuth2Token refreshWithLock(String key, OAuth2Token token) {
        ReentrantLock lock = refreshLocks.computeIfAbsent(key, k -> new ReentrantLock());
        lock.lock();
        try {
            // Double-check: another thread may have already refreshed
            Optional<OAuth2Token> recheck = tokenStore.load(key);
            if (recheck.isPresent()) {
                OAuth2Token current = recheck.get();
                if (!isRefreshNeeded(current)) {
                    return current;
                }
                token = current;
            }

            // Perform refresh
            try {
                OAuth2Token refreshed = refresher.refresh(token);
                tokenStore.save(key, refreshed);
                return refreshed;
            } catch (OAuth2Exception e) {
                throw new OAuth2Exception(OAuth2ErrorCode.TOKEN_REFRESH_FAILED,
                        "Failed to refresh token for key: " + key, e);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Ensure the manager is still open.
     * 确保管理器仍然打开。
     *
     * @throws IllegalStateException if the manager is closed | 如果管理器已关闭
     */
    private void ensureOpen() {
        if (closed.get()) {
            throw new IllegalStateException("TokenManager is closed");
        }
    }

    /**
     * Create a new builder.
     * 创建新的构建器。
     *
     * @return the builder | 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * DefaultTokenManager Builder
     * DefaultTokenManager 构建器
     *
     * <p>Builder for constructing {@link DefaultTokenManager} instances.</p>
     * <p>用于构建 {@link DefaultTokenManager} 实例的构建器。</p>
     */
    public static final class Builder {
        private TokenStore tokenStore;
        private TokenRefresher refresher;
        private boolean ownsRefresher;
        private Duration refreshThreshold;

        Builder() {
        }

        /**
         * Set the token store.
         * 设置 Token 存储。
         *
         * @param tokenStore the token store | Token 存储
         * @return this builder | 此构建器
         */
        public Builder tokenStore(TokenStore tokenStore) {
            this.tokenStore = tokenStore;
            return this;
        }

        /**
         * Set the token refresher.
         * 设置 Token 刷新器。
         *
         * @param refresher the token refresher | Token 刷新器
         * @return this builder | 此构建器
         */
        public Builder refresher(TokenRefresher refresher) {
            this.refresher = refresher;
            this.ownsRefresher = false;
            return this;
        }

        /**
         * Set the token refresher, indicating this manager owns it and should close it.
         * 设置 Token 刷新器，表示此管理器拥有它并应关闭它。
         *
         * @param refresher the token refresher | Token 刷新器
         * @return this builder | 此构建器
         */
        public Builder ownedRefresher(TokenRefresher refresher) {
            this.refresher = refresher;
            this.ownsRefresher = true;
            return this;
        }

        /**
         * Set the refresh threshold duration.
         * 设置刷新阈值时长。
         *
         * <p>Tokens will be refreshed when their remaining validity is less than this threshold.</p>
         * <p>当 Token 的剩余有效期小于此阈值时将刷新 Token。</p>
         *
         * @param refreshThreshold the refresh threshold | 刷新阈值
         * @return this builder | 此构建器
         */
        public Builder refreshThreshold(Duration refreshThreshold) {
            this.refreshThreshold = refreshThreshold;
            return this;
        }

        /**
         * Build the DefaultTokenManager.
         * 构建 DefaultTokenManager。
         *
         * @return the default token manager | 默认 Token 管理器
         * @throws NullPointerException if tokenStore is null | 如果 tokenStore 为 null
         */
        public DefaultTokenManager build() {
            return new DefaultTokenManager(this);
        }
    }
}
