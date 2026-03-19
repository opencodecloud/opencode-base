package cloud.opencode.base.oauth2.token;

import cloud.opencode.base.oauth2.OAuth2Config;
import cloud.opencode.base.oauth2.OAuth2Token;
import cloud.opencode.base.oauth2.exception.OAuth2ErrorCode;
import cloud.opencode.base.oauth2.exception.OAuth2Exception;
import cloud.opencode.base.oauth2.http.OAuth2HttpClient;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Token Refresher
 * Token 刷新器
 *
 * <p>Handles automatic token refresh with de-duplication.</p>
 * <p>处理自动 Token 刷新，带有去重功能。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Automatic refresh before expiration - 过期前自动刷新</li>
 *   <li>Concurrent refresh de-duplication - 并发刷新去重</li>
 *   <li>Background refresh scheduling - 后台刷新调度</li>
 *   <li>Refresh failure handling - 刷新失败处理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create refresher
 * TokenRefresher refresher = new TokenRefresher(config, httpClient);
 *
 * // Refresh a token
 * OAuth2Token newToken = refresher.refresh(oldToken);
 *
 * // Check if refresh is needed
 * if (refresher.needsRefresh(token)) {
 *     token = refresher.refresh(token);
 * }
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
public class TokenRefresher implements AutoCloseable {

    private final OAuth2Config config;
    private final OAuth2HttpClient httpClient;
    private final Duration refreshThreshold;
    private final ConcurrentHashMap<String, CompletableFuture<OAuth2Token>> pendingRefreshes;
    private final ReentrantLock refreshLock;
    private final ScheduledExecutorService scheduler;
    private volatile boolean closed;

    /**
     * Create a new token refresher
     * 创建新的 Token 刷新器
     *
     * @param config     the OAuth2 configuration | OAuth2 配置
     * @param httpClient the HTTP client | HTTP 客户端
     */
    public TokenRefresher(OAuth2Config config, OAuth2HttpClient httpClient) {
        this(config, httpClient, config.refreshThreshold());
    }

    /**
     * Create a new token refresher with custom threshold
     * 使用自定义阈值创建新的 Token 刷新器
     *
     * @param config           the OAuth2 configuration | OAuth2 配置
     * @param httpClient       the HTTP client | HTTP 客户端
     * @param refreshThreshold the refresh threshold | 刷新阈值
     */
    public TokenRefresher(OAuth2Config config, OAuth2HttpClient httpClient, Duration refreshThreshold) {
        this.config = Objects.requireNonNull(config, "config cannot be null");
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient cannot be null");
        this.refreshThreshold = refreshThreshold != null ? refreshThreshold : Duration.ofMinutes(5);
        this.pendingRefreshes = new ConcurrentHashMap<>();
        this.refreshLock = new ReentrantLock();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = Thread.ofVirtual().name("token-refresher").unstarted(r);
            return t;
        });
        this.closed = false;
    }

    /**
     * Check if a token needs refresh
     * 检查 Token 是否需要刷新
     *
     * @param token the token | Token
     * @return true if refresh needed | 如果需要刷新返回 true
     */
    public boolean needsRefresh(OAuth2Token token) {
        if (token == null) {
            return false;
        }
        return token.isExpiringSoon(refreshThreshold) && token.hasRefreshToken();
    }

    /**
     * Refresh a token
     * 刷新 Token
     *
     * @param token the token to refresh | 要刷新的 Token
     * @return the new token | 新 Token
     * @throws OAuth2Exception if refresh fails | 如果刷新失败
     */
    public OAuth2Token refresh(OAuth2Token token) {
        Objects.requireNonNull(token, "token cannot be null");

        if (!token.hasRefreshToken()) {
            throw new OAuth2Exception(OAuth2ErrorCode.TOKEN_REFRESH_FAILED,
                    "Token has no refresh token");
        }

        return refreshWithDeduplication(token.refreshToken());
    }

    /**
     * Refresh a token asynchronously
     * 异步刷新 Token
     *
     * @param token the token to refresh | 要刷新的 Token
     * @return future with the new token | 包含新 Token 的 Future
     */
    public CompletableFuture<OAuth2Token> refreshAsync(OAuth2Token token) {
        Objects.requireNonNull(token, "token cannot be null");

        if (!token.hasRefreshToken()) {
            return CompletableFuture.failedFuture(
                    new OAuth2Exception(OAuth2ErrorCode.TOKEN_REFRESH_FAILED,
                            "Token has no refresh token"));
        }

        return refreshAsyncWithDeduplication(token.refreshToken());
    }

    /**
     * Refresh with de-duplication to prevent multiple concurrent refreshes for the same token
     * 带去重的刷新，防止同一 Token 的多个并发刷新
     *
     * @param refreshToken the refresh token | 刷新令牌
     * @return the new token | 新 Token
     */
    private OAuth2Token refreshWithDeduplication(String refreshToken) {
        try {
            return refreshAsyncWithDeduplication(refreshToken).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OAuth2Exception(OAuth2ErrorCode.TOKEN_REFRESH_FAILED, "Refresh interrupted", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof OAuth2Exception oae) {
                throw oae;
            }
            throw new OAuth2Exception(OAuth2ErrorCode.TOKEN_REFRESH_FAILED, cause);
        }
    }

    /**
     * Async refresh with de-duplication
     * 带去重的异步刷新
     *
     * @param refreshToken the refresh token | 刷新令牌
     * @return future with the new token | 包含新 Token 的 Future
     */
    private CompletableFuture<OAuth2Token> refreshAsyncWithDeduplication(String refreshToken) {
        return pendingRefreshes.computeIfAbsent(refreshToken, rt -> {
            CompletableFuture<OAuth2Token> future = CompletableFuture.supplyAsync(() -> doRefresh(rt), scheduler);
            // Remove from pending map only after the future completes, so concurrent callers
            // always find the in-flight future and join it instead of starting a duplicate refresh.
            // 仅在 future 完成后从 pending map 移除，以便并发调用者始终找到进行中的 future 并加入，而不是启动重复刷新。
            future.whenComplete((result, error) -> pendingRefreshes.remove(rt, future));
            return future;
        });
    }

    /**
     * Perform the actual token refresh
     * 执行实际的 Token 刷新
     *
     * @param refreshToken the refresh token | 刷新令牌
     * @return the new token | 新 Token
     */
    private OAuth2Token doRefresh(String refreshToken) {
        if (closed) {
            throw new OAuth2Exception(OAuth2ErrorCode.TOKEN_REFRESH_FAILED, "Refresher is closed");
        }

        if (config.tokenEndpoint() == null) {
            throw new OAuth2Exception(OAuth2ErrorCode.MISSING_TOKEN_ENDPOINT);
        }

        Map<String, String> params = new LinkedHashMap<>();
        params.put("grant_type", "refresh_token");
        params.put("refresh_token", refreshToken);
        params.put("client_id", config.clientId());

        if (config.clientSecret() != null) {
            params.put("client_secret", config.clientSecret());
        }

        String response = httpClient.postForm(config.tokenEndpoint(), params);
        return parseTokenResponse(response);
    }

    /**
     * Parse token response
     * 解析 Token 响应
     *
     * @param json the JSON response | JSON 响应
     * @return the token | Token
     */
    private OAuth2Token parseTokenResponse(String json) {
        OAuth2Token.Builder builder = OAuth2Token.builder();

        String accessToken = extractJsonString(json, "access_token");
        if (accessToken == null) {
            throw new OAuth2Exception(OAuth2ErrorCode.TOKEN_PARSE_ERROR, "Missing access_token");
        }
        builder.accessToken(accessToken);

        String tokenType = extractJsonString(json, "token_type");
        if (tokenType != null) {
            builder.tokenType(tokenType);
        }

        String refreshToken = extractJsonString(json, "refresh_token");
        if (refreshToken != null) {
            builder.refreshToken(refreshToken);
        }

        String idToken = extractJsonString(json, "id_token");
        if (idToken != null) {
            builder.idToken(idToken);
        }

        String scope = extractJsonString(json, "scope");
        if (scope != null) {
            builder.scopeString(scope);
        }

        Long expiresIn = extractJsonLong(json, "expires_in");
        if (expiresIn != null) {
            builder.expiresIn(expiresIn);
        }

        return builder.build();
    }

    /**
     * Extract string field from JSON
     * 从 JSON 中提取字符串字段
     */
    private String extractJsonString(String json, String field) {
        String pattern = "\"" + field + "\"";
        int idx = json.indexOf(pattern);
        if (idx < 0) return null;

        int colonIdx = json.indexOf(':', idx + pattern.length());
        if (colonIdx < 0) return null;

        int start = colonIdx + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
            start++;
        }

        if (start >= json.length()) return null;

        if (json.charAt(start) == '"') {
            int end = json.indexOf('"', start + 1);
            if (end > start) {
                return json.substring(start + 1, end);
            }
        }

        return null;
    }

    /**
     * Extract long field from JSON
     * 从 JSON 中提取长整型字段
     */
    private Long extractJsonLong(String json, String field) {
        String pattern = "\"" + field + "\"";
        int idx = json.indexOf(pattern);
        if (idx < 0) return null;

        int colonIdx = json.indexOf(':', idx + pattern.length());
        if (colonIdx < 0) return null;

        int start = colonIdx + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
            start++;
        }

        if (start >= json.length()) return null;

        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) {
            end++;
        }

        if (end > start) {
            try {
                return Long.parseLong(json.substring(start, end));
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }

    /**
     * Get the refresh threshold
     * 获取刷新阈值
     *
     * @return the refresh threshold | 刷新阈值
     */
    public Duration refreshThreshold() {
        return refreshThreshold;
    }

    /**
     * Check if the refresher is closed
     * 检查刷新器是否已关闭
     *
     * @return true if closed | 如果已关闭返回 true
     */
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            pendingRefreshes.clear();
        }
    }
}
