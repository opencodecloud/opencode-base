package cloud.opencode.base.web.cors;

import cloud.opencode.base.web.http.HttpHeaders;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * CORS Configuration - Cross-Origin Resource Sharing configuration builder and applier
 * CORS 配置 - 跨域资源共享配置构建器和应用器
 *
 * <p>An immutable record representing a complete CORS policy. Use the {@link Builder}
 * to construct instances with a fluent API. Provides preset configurations for common
 * scenarios and methods to generate CORS response headers.</p>
 * <p>一个不可变记录，表示完整的 CORS 策略。使用 {@link Builder} 通过流式 API 构建实例。
 * 提供常见场景的预设配置和生成 CORS 响应头部的方法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Builder pattern with fluent API - 流式 API 构建器模式</li>
 *   <li>Origin/method/header allowlist checking - 来源/方法/头部白名单检查</li>
 *   <li>Automatic CORS response header generation - 自动 CORS 响应头部生成</li>
 *   <li>Preset configurations (allowAll, restrictive) - 预设配置</li>
 *   <li>Security: credentials + wildcard origin prevention - 安全：凭证+通配符来源防护</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Allow all origins
 * CorsConfig cors = CorsConfig.allowAll();
 *
 * // Restrictive: only specific origins
 * CorsConfig cors = CorsConfig.restrictive("https://example.com");
 *
 * // Custom configuration
 * CorsConfig cors = CorsConfig.builder()
 *     .allowOrigin("https://example.com", "https://api.example.com")
 *     .allowMethod("GET", "POST")
 *     .allowHeader("Authorization", "Content-Type")
 *     .allowCredentials(true)
 *     .maxAge(3600)
 *     .build();
 *
 * // Generate response headers
 * Map<String, String> headers = cors.toHeaders("https://example.com");
 *
 * // Apply to HttpHeaders
 * HttpHeaders httpHeaders = HttpHeaders.of();
 * cors.applyTo(httpHeaders, "https://example.com");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 是（不可变记录）</li>
 *   <li>Null-safe: Builder rejects null arguments - 构建器拒绝 null 参数</li>
 *   <li>Credentials + wildcard origin is rejected at build time - 凭证+通配符来源在构建时被拒绝</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.3
 */
public record CorsConfig(
        Set<String> allowedOrigins,
        Set<String> allowedMethods,
        Set<String> allowedHeaders,
        Set<String> exposedHeaders,
        boolean allowCredentials,
        long maxAge
) {

    // ==================== Header Constants ====================

    private static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    private static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
    private static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
    private static final String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";
    private static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
    private static final String ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";
    private static final String WILDCARD = "*";

    // ==================== Compact Constructor ====================

    /**
     * Compact constructor that creates defensive copies of all sets.
     * Methods and headers use case-insensitive TreeSet for O(log n) lookup.
     * Origins are normalized to lowercase for consistent matching.
     * 紧凑构造函数，创建所有集合的防御性副本。
     * 方法和头部使用大小写不敏感的 TreeSet 以实现 O(log n) 查找。
     * 来源统一转为小写以保证匹配一致性。
     */
    public CorsConfig {
        // Origins: normalize to lowercase for consistent isOriginAllowed() matching
        Set<String> normalizedOrigins = new LinkedHashSet<>();
        for (String o : allowedOrigins) {
            normalizedOrigins.add(WILDCARD.equals(o) ? o : o.toLowerCase(Locale.ROOT));
        }
        allowedOrigins = Collections.unmodifiableSet(normalizedOrigins);
        // Methods/Headers: TreeSet with CASE_INSENSITIVE_ORDER → O(log n) contains()
        Set<String> methodSet = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        methodSet.addAll(allowedMethods);
        allowedMethods = Collections.unmodifiableSet(methodSet);
        Set<String> headerSet = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        headerSet.addAll(allowedHeaders);
        allowedHeaders = Collections.unmodifiableSet(headerSet);
        exposedHeaders = Collections.unmodifiableSet(new LinkedHashSet<>(exposedHeaders));
    }

    // ==================== Factory Methods ====================

    /**
     * Creates a new builder.
     * 创建新的构建器。
     *
     * @return a new Builder - 新的构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a permissive CORS configuration that allows all origins, methods, and headers.
     * 创建允许所有来源、方法和头部的宽松 CORS 配置。
     *
     * <p>Note: allowCredentials is false because wildcard origin and credentials
     * cannot be used together per the CORS specification.</p>
     * <p>注意：allowCredentials 为 false，因为根据 CORS 规范通配符来源和凭证不能同时使用。</p>
     *
     * @return a permissive CorsConfig - 宽松的 CorsConfig
     */
    public static CorsConfig allowAll() {
        return builder()
                .allowOrigin(WILDCARD)
                .allowMethod("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS")
                .allowHeader(WILDCARD)
                .allowCredentials(false)
                .maxAge(86400)
                .build();
    }

    /**
     * Creates a restrictive CORS configuration that only allows specified origins.
     * 创建仅允许指定来源的限制性 CORS 配置。
     *
     * <p>Allows common HTTP methods and standard headers with credentials enabled.</p>
     * <p>允许常见 HTTP 方法和标准头部，并启用凭证。</p>
     *
     * @param origins the allowed origins - 允许的来源
     * @return a restrictive CorsConfig - 限制性的 CorsConfig
     * @throws IllegalArgumentException if origins is null or empty - 如果 origins 为 null 或空
     */
    public static CorsConfig restrictive(String... origins) {
        Objects.requireNonNull(origins, "origins must not be null");
        if (origins.length == 0) {
            throw new IllegalArgumentException("At least one origin must be specified");
        }
        return builder()
                .allowOrigin(origins)
                .allowMethod("GET", "POST", "PUT", "DELETE")
                .allowHeader("Authorization", "Content-Type", "Accept")
                .allowCredentials(true)
                .maxAge(3600)
                .build();
    }

    // ==================== Query Methods ====================

    /**
     * Checks if the given origin is allowed by this CORS configuration.
     * 检查给定的来源是否被此 CORS 配置允许。
     *
     * @param origin the origin to check - 要检查的来源
     * @return true if allowed - 如果允许返回 true
     */
    public boolean isOriginAllowed(String origin) {
        if (origin == null) {
            return false;
        }
        if (allowedOrigins.contains(WILDCARD)) {
            return true;
        }
        // origins stored normalized to lowercase; compare likewise
        return allowedOrigins.contains(origin.toLowerCase(Locale.ROOT));
    }

    /**
     * Checks if the given HTTP method is allowed by this CORS configuration.
     * 检查给定的 HTTP 方法是否被此 CORS 配置允许。
     *
     * @param method the method to check - 要检查的方法
     * @return true if allowed - 如果允许返回 true
     */
    public boolean isMethodAllowed(String method) {
        if (method == null) {
            return false;
        }
        // TreeSet(CASE_INSENSITIVE_ORDER) — O(log n) case-insensitive lookup
        return allowedMethods.contains(WILDCARD) || allowedMethods.contains(method);
    }

    /**
     * Checks if the given header is allowed by this CORS configuration.
     * 检查给定的头部是否被此 CORS 配置允许。
     *
     * @param header the header to check - 要检查的头部
     * @return true if allowed - 如果允许返回 true
     */
    public boolean isHeaderAllowed(String header) {
        if (header == null) {
            return false;
        }
        // TreeSet(CASE_INSENSITIVE_ORDER) — O(log n) case-insensitive lookup
        return allowedHeaders.contains(WILDCARD) || allowedHeaders.contains(header);
    }

    /**
     * Checks if this configuration allows all origins (wildcard).
     * 检查此配置是否允许所有来源（通配符）。
     *
     * @return true if wildcard origin is allowed - 如果允许通配符来源返回 true
     */
    public boolean allowsAll() {
        return allowedOrigins.contains(WILDCARD);
    }

    // ==================== Header Generation ====================

    /**
     * Generates CORS response headers for the given request origin.
     * 为给定的请求来源生成 CORS 响应头部。
     *
     * <p>Returns an empty map if the origin is not allowed.</p>
     * <p>如果来源不被允许则返回空 Map。</p>
     *
     * @param requestOrigin the request origin - 请求来源
     * @return CORS response headers map - CORS 响应头部 Map
     */
    public Map<String, String> toHeaders(String requestOrigin) {
        if (!isOriginAllowed(requestOrigin)) {
            return Collections.emptyMap();
        }

        Map<String, String> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        // Access-Control-Allow-Origin
        if (allowsAll() && !allowCredentials) {
            result.put(ACCESS_CONTROL_ALLOW_ORIGIN, WILDCARD);
        } else {
            result.put(ACCESS_CONTROL_ALLOW_ORIGIN, requestOrigin);
        }

        // Access-Control-Allow-Methods
        if (!allowedMethods.isEmpty()) {
            result.put(ACCESS_CONTROL_ALLOW_METHODS, String.join(", ", allowedMethods));
        }

        // Access-Control-Allow-Headers
        if (!allowedHeaders.isEmpty()) {
            result.put(ACCESS_CONTROL_ALLOW_HEADERS, String.join(", ", allowedHeaders));
        }

        // Access-Control-Expose-Headers
        if (!exposedHeaders.isEmpty()) {
            result.put(ACCESS_CONTROL_EXPOSE_HEADERS, String.join(", ", exposedHeaders));
        }

        // Access-Control-Allow-Credentials
        if (allowCredentials) {
            result.put(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        }

        // Access-Control-Max-Age
        if (maxAge > 0) {
            result.put(ACCESS_CONTROL_MAX_AGE, String.valueOf(maxAge));
        }

        return Collections.unmodifiableMap(result);
    }

    /**
     * Applies CORS response headers to the given HttpHeaders for the specified request origin.
     * 将 CORS 响应头部应用到给定的 HttpHeaders 上。
     *
     * @param headers       the headers to apply to - 要应用的头部
     * @param requestOrigin the request origin - 请求来源
     */
    public void applyTo(HttpHeaders headers, String requestOrigin) {
        Objects.requireNonNull(headers, "headers must not be null");
        toHeaders(requestOrigin).forEach(headers::set);
    }

    // ==================== Builder ====================

    /**
     * Builder for CorsConfig - fluent API for constructing CORS configurations.
     * CorsConfig 构建器 - 用于构建 CORS 配置的流式 API。
     *
     * @since JDK 25, opencode-base-web V1.0.3
     */
    public static final class Builder {

        private final Set<String> allowedOrigins = new LinkedHashSet<>();
        private final Set<String> allowedMethods = new LinkedHashSet<>();
        private final Set<String> allowedHeaders = new LinkedHashSet<>();
        private final Set<String> exposedHeaders = new LinkedHashSet<>();
        private boolean allowCredentials;
        private long maxAge;

        private Builder() {
        }

        /**
         * Adds allowed origins.
         * 添加允许的来源。
         *
         * @param origins the origins to allow - 允许的来源
         * @return this builder - 此构建器
         */
        public Builder allowOrigin(String... origins) {
            Objects.requireNonNull(origins, "origins must not be null");
            for (String origin : origins) {
                Objects.requireNonNull(origin, "origin must not be null");
                allowedOrigins.add(origin);
            }
            return this;
        }

        /**
         * Adds allowed HTTP methods.
         * 添加允许的 HTTP 方法。
         *
         * @param methods the methods to allow - 允许的方法
         * @return this builder - 此构建器
         */
        public Builder allowMethod(String... methods) {
            Objects.requireNonNull(methods, "methods must not be null");
            for (String method : methods) {
                Objects.requireNonNull(method, "method must not be null");
                allowedMethods.add(method);
            }
            return this;
        }

        /**
         * Adds allowed request headers.
         * 添加允许的请求头部。
         *
         * @param headers the headers to allow - 允许的头部
         * @return this builder - 此构建器
         */
        public Builder allowHeader(String... headers) {
            Objects.requireNonNull(headers, "headers must not be null");
            for (String header : headers) {
                Objects.requireNonNull(header, "header must not be null");
                allowedHeaders.add(header);
            }
            return this;
        }

        /**
         * Adds exposed response headers.
         * 添加暴露的响应头部。
         *
         * @param headers the headers to expose - 要暴露的头部
         * @return this builder - 此构建器
         */
        public Builder exposeHeader(String... headers) {
            Objects.requireNonNull(headers, "headers must not be null");
            for (String header : headers) {
                Objects.requireNonNull(header, "header must not be null");
                exposedHeaders.add(header);
            }
            return this;
        }

        /**
         * Sets whether credentials (cookies, authorization) are allowed.
         * 设置是否允许凭证（cookies、authorization）。
         *
         * @param allowCredentials true to allow credentials - true 表示允许凭证
         * @return this builder - 此构建器
         */
        public Builder allowCredentials(boolean allowCredentials) {
            this.allowCredentials = allowCredentials;
            return this;
        }

        /**
         * Sets the max age for preflight request caching (in seconds).
         * 设置预检请求缓存的最大时间（秒）。
         *
         * @param seconds the max age in seconds (must be non-negative) - 最大时间（秒，必须为非负数）
         * @return this builder - 此构建器
         * @throws IllegalArgumentException if seconds is negative - 如果秒数为负数
         */
        public Builder maxAge(long seconds) {
            if (seconds < 0) {
                throw new IllegalArgumentException("maxAge must not be negative: " + seconds);
            }
            this.maxAge = seconds;
            return this;
        }

        /**
         * Builds the CorsConfig.
         * 构建 CorsConfig。
         *
         * @return the CorsConfig - CorsConfig 实例
         * @throws IllegalStateException if allowCredentials is true and wildcard origin is used
         *                               - 如果 allowCredentials 为 true 且使用了通配符来源
         */
        public CorsConfig build() {
            if (allowCredentials && allowedOrigins.contains(WILDCARD)) {
                throw new IllegalStateException(
                        "allowCredentials cannot be true when allowedOrigins contains '*'. "
                                + "The CORS specification forbids this combination."
                );
            }
            return new CorsConfig(
                    allowedOrigins,
                    allowedMethods,
                    allowedHeaders,
                    exposedHeaders,
                    allowCredentials,
                    maxAge
            );
        }
    }
}
