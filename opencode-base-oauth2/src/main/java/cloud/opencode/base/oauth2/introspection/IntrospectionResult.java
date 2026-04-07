package cloud.opencode.base.oauth2.introspection;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Token Introspection Result (RFC 7662)
 * Token 内省结果（RFC 7662）
 *
 * <p>Immutable record representing the response from an OAuth2 token introspection endpoint
 * as defined in RFC 7662. Contains information about the active state of a token and its
 * associated metadata.</p>
 * <p>不可变记录，表示 RFC 7662 定义的 OAuth2 Token 内省端点的响应。包含 Token 的活跃状态及其
 * 关联的元数据信息。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>RFC 7662 compliant introspection result - 符合 RFC 7662 的内省结果</li>
 *   <li>Immutable with defensive copy of claims map - 不可变，对 claims map 进行防御性拷贝</li>
 *   <li>Convenience methods for expiration and scope checking - 便捷的过期和权限范围检查方法</li>
 *   <li>Builder pattern for flexible construction - 构建器模式支持灵活构建</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Check if token is active and has required scope
 * // 检查 Token 是否活跃且具有所需权限范围
 * IntrospectionResult result = tokenIntrospection.introspect(token);
 * if (result.active() && result.hasScope("read")) {
 *     // Token is valid and has read scope
 * }
 *
 * // Build result manually
 * // 手动构建结果
 * IntrospectionResult result = IntrospectionResult.builder()
 *     .active(true)
 *     .scope("read write")
 *     .clientId("my-client")
 *     .sub("user123")
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: Yes (claims defensively copied) - 空值安全: 是（claims 进行防御性拷贝）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see <a href="https://tools.ietf.org/html/rfc7662">RFC 7662 - OAuth 2.0 Token Introspection</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.3
 */
public record IntrospectionResult(
        boolean active,
        String scope,
        String clientId,
        String username,
        String tokenType,
        Instant exp,
        Instant iat,
        Instant nbf,
        String sub,
        String aud,
        String iss,
        String jti,
        Map<String, Object> claims
) {

    /**
     * Compact constructor with defensive copy of claims.
     * 带有 claims 防御性拷贝的紧凑构造器。
     */
    public IntrospectionResult {
        claims = claims != null ? Map.copyOf(claims) : Map.of();
    }

    /**
     * Check if the token has expired based on the exp claim.
     * 根据 exp 声明检查 Token 是否已过期。
     *
     * @return true if the token has expired, false if exp is null or not yet expired
     *         | 如果 Token 已过期返回 true，如果 exp 为 null 或尚未过期返回 false
     */
    public boolean isExpired() {
        return exp != null && Instant.now().isAfter(exp);
    }

    /**
     * Check if the token has a specific scope.
     * 检查 Token 是否具有特定的权限范围。
     *
     * @param requiredScope the scope to check | 要检查的权限范围
     * @return true if the scope is present | 如果存在该权限范围返回 true
     * @throws NullPointerException if requiredScope is null | 如果 requiredScope 为 null 则抛出
     */
    public boolean hasScope(String requiredScope) {
        Objects.requireNonNull(requiredScope, "requiredScope cannot be null");
        if (scope == null || scope.isBlank()) {
            return false;
        }
        return scopes().contains(requiredScope);
    }

    /**
     * Parse the scope string into a set of individual scopes.
     * 将权限范围字符串解析为单独权限范围的集合。
     *
     * @return unmodifiable set of scopes, empty set if scope is null or blank
     *         | 不可修改的权限范围集合，如果 scope 为 null 或空白则返回空集合
     */
    public Set<String> scopes() {
        if (scope == null || scope.isBlank()) {
            return Set.of();
        }
        return Collections.unmodifiableSet(
                Arrays.stream(scope.split("\\s+"))
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toSet())
        );
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
     * IntrospectionResult Builder
     * IntrospectionResult 构建器
     *
     * <p>Provides a fluent API for constructing {@link IntrospectionResult} instances.</p>
     * <p>提供用于构建 {@link IntrospectionResult} 实例的流式 API。</p>
     *
     * @author Leon Soo
     * <a href="https://leonsoo.com">www.LeonSoo.com</a>
     * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
     * @since JDK 25, opencode-base-oauth2 V1.0.3
     */
    public static class Builder {
        private boolean active;
        private String scope;
        private String clientId;
        private String username;
        private String tokenType;
        private Instant exp;
        private Instant iat;
        private Instant nbf;
        private String sub;
        private String aud;
        private String iss;
        private String jti;
        private Map<String, Object> claims;

        /**
         * Set the active status.
         * 设置活跃状态。
         *
         * @param active whether the token is active | Token 是否活跃
         * @return this builder | 此构建器
         */
        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        /**
         * Set the scope.
         * 设置权限范围。
         *
         * @param scope the scope string (space-delimited) | 权限范围字符串（空格分隔）
         * @return this builder | 此构建器
         */
        public Builder scope(String scope) {
            this.scope = scope;
            return this;
        }

        /**
         * Set the client ID.
         * 设置客户端 ID。
         *
         * @param clientId the client ID | 客户端 ID
         * @return this builder | 此构建器
         */
        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        /**
         * Set the username.
         * 设置用户名。
         *
         * @param username the username | 用户名
         * @return this builder | 此构建器
         */
        public Builder username(String username) {
            this.username = username;
            return this;
        }

        /**
         * Set the token type.
         * 设置 Token 类型。
         *
         * @param tokenType the token type | Token 类型
         * @return this builder | 此构建器
         */
        public Builder tokenType(String tokenType) {
            this.tokenType = tokenType;
            return this;
        }

        /**
         * Set the expiration time.
         * 设置过期时间。
         *
         * @param exp the expiration instant | 过期时间
         * @return this builder | 此构建器
         */
        public Builder exp(Instant exp) {
            this.exp = exp;
            return this;
        }

        /**
         * Set the issued-at time.
         * 设置签发时间。
         *
         * @param iat the issued-at instant | 签发时间
         * @return this builder | 此构建器
         */
        public Builder iat(Instant iat) {
            this.iat = iat;
            return this;
        }

        /**
         * Set the not-before time.
         * 设置生效时间。
         *
         * @param nbf the not-before instant | 生效时间
         * @return this builder | 此构建器
         */
        public Builder nbf(Instant nbf) {
            this.nbf = nbf;
            return this;
        }

        /**
         * Set the subject.
         * 设置主体。
         *
         * @param sub the subject identifier | 主体标识符
         * @return this builder | 此构建器
         */
        public Builder sub(String sub) {
            this.sub = sub;
            return this;
        }

        /**
         * Set the audience.
         * 设置受众。
         *
         * @param aud the audience | 受众
         * @return this builder | 此构建器
         */
        public Builder aud(String aud) {
            this.aud = aud;
            return this;
        }

        /**
         * Set the issuer.
         * 设置颁发者。
         *
         * @param iss the issuer | 颁发者
         * @return this builder | 此构建器
         */
        public Builder iss(String iss) {
            this.iss = iss;
            return this;
        }

        /**
         * Set the JWT ID.
         * 设置 JWT ID。
         *
         * @param jti the JWT ID | JWT ID
         * @return this builder | 此构建器
         */
        public Builder jti(String jti) {
            this.jti = jti;
            return this;
        }

        /**
         * Set additional claims.
         * 设置附加声明。
         *
         * @param claims the claims map | 声明映射
         * @return this builder | 此构建器
         */
        public Builder claims(Map<String, Object> claims) {
            this.claims = claims;
            return this;
        }

        /**
         * Build the IntrospectionResult.
         * 构建 IntrospectionResult。
         *
         * @return the introspection result | 内省结果
         */
        public IntrospectionResult build() {
            return new IntrospectionResult(
                    active, scope, clientId, username, tokenType,
                    exp, iat, nbf, sub, aud, iss, jti, claims
            );
        }
    }
}
