package cloud.opencode.base.web.security;

import cloud.opencode.base.web.http.HttpHeaders;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Security Headers - HTTP Security Response Headers Builder
 * 安全头部 - HTTP 安全响应头部构建器
 *
 * <p>Provides a builder-pattern API for constructing a set of HTTP security response
 * headers. Supports modern best-practice headers including Content-Security-Policy,
 * Strict-Transport-Security, X-Frame-Options, and Cross-Origin policies.</p>
 * <p>提供构建器模式 API 用于构建一组 HTTP 安全响应头部。支持现代最佳实践头部，
 * 包括 Content-Security-Policy、Strict-Transport-Security、X-Frame-Options 和跨域策略。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fluent builder API for all security headers - 所有安全头部的流式构建器 API</li>
 *   <li>Preset configurations (strict, standard) - 预设配置（严格、标准）</li>
 *   <li>Apply to HttpHeaders or export as Map - 应用到 HttpHeaders 或导出为 Map</li>
 *   <li>Type-safe enums for X-Frame-Options and Referrer-Policy - X-Frame-Options 和 Referrer-Policy 类型安全枚举</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Strict preset
 * SecurityHeaders headers = SecurityHeaders.strict();
 *
 * // Standard preset
 * SecurityHeaders headers = SecurityHeaders.standard();
 *
 * // Custom configuration
 * SecurityHeaders headers = SecurityHeaders.builder()
 *     .contentSecurityPolicy("default-src 'self'")
 *     .strictTransportSecurity(31536000, true)
 *     .xFrameOptions(SecurityHeaders.FrameOption.DENY)
 *     .xContentTypeOptions()
 *     .xXssProtection()
 *     .referrerPolicy(SecurityHeaders.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
 *     .build();
 *
 * // Apply to HttpHeaders
 * HttpHeaders httpHeaders = HttpHeaders.of();
 * headers.applyTo(httpHeaders);
 *
 * // Export as Map
 * Map<String, String> map = headers.toMap();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 是（不可变）</li>
 *   <li>Null-safe: Builder rejects null arguments - 构建器拒绝 null 参数</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.3
 */
public final class SecurityHeaders {

    // ==================== Header Name Constants ====================

    private static final String CONTENT_SECURITY_POLICY = "Content-Security-Policy";
    private static final String STRICT_TRANSPORT_SECURITY = "Strict-Transport-Security";
    private static final String X_FRAME_OPTIONS = "X-Frame-Options";
    private static final String X_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";
    private static final String X_XSS_PROTECTION = "X-XSS-Protection";
    private static final String REFERRER_POLICY = "Referrer-Policy";
    private static final String PERMISSIONS_POLICY = "Permissions-Policy";
    private static final String CROSS_ORIGIN_EMBEDDER_POLICY = "Cross-Origin-Embedder-Policy";
    private static final String CROSS_ORIGIN_OPENER_POLICY = "Cross-Origin-Opener-Policy";

    // ==================== Instance Fields ====================

    private final Map<String, String> headers;

    private SecurityHeaders(Map<String, String> headers) {
        this.headers = Collections.unmodifiableMap(new LinkedHashMap<>(headers));
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
     * Creates a strict security headers configuration with the most restrictive settings.
     * 创建具有最严格设置的安全头部配置。
     *
     * <p>Includes: DENY frame options, nosniff, no-referrer, strict CSP,
     * HSTS with 1 year max-age and includeSubDomains, XSS protection disabled (modern best practice),
     * and strict cross-origin policies.</p>
     * <p>包含：DENY 框架选项、nosniff、no-referrer、严格 CSP、
     * 1 年 max-age 和 includeSubDomains 的 HSTS、禁用 XSS 保护（现代最佳实践）、
     * 以及严格的跨域策略。</p>
     *
     * @return strict SecurityHeaders - 严格的 SecurityHeaders
     */
    public static SecurityHeaders strict() {
        return builder()
                .contentSecurityPolicy("default-src 'none'; script-src 'self'; style-src 'self'; img-src 'self'; font-src 'self'; connect-src 'self'; frame-ancestors 'none'")
                .strictTransportSecurity(31536000, true)
                .xFrameOptions(FrameOption.DENY)
                .xContentTypeOptions()
                .xXssProtection()
                .referrerPolicy(ReferrerPolicy.NO_REFERRER)
                .permissionsPolicy("geolocation=(), camera=(), microphone=()")
                .crossOriginEmbedderPolicy("require-corp")
                .crossOriginOpenerPolicy("same-origin")
                .build();
    }

    /**
     * Creates a standard security headers configuration suitable for most applications.
     * 创建适用于大多数应用程序的标准安全头部配置。
     *
     * <p>Includes: SAMEORIGIN frame options, nosniff, strict-origin-when-cross-origin referrer,
     * HSTS with 1 year max-age, and XSS protection disabled (modern best practice).</p>
     * <p>包含：SAMEORIGIN 框架选项、nosniff、strict-origin-when-cross-origin referrer、
     * 1 年 max-age 的 HSTS、以及禁用 XSS 保护（现代最佳实践）。</p>
     *
     * @return standard SecurityHeaders - 标准的 SecurityHeaders
     */
    public static SecurityHeaders standard() {
        return builder()
                .contentSecurityPolicy("default-src 'self'")
                .strictTransportSecurity(31536000, false)
                .xFrameOptions(FrameOption.SAMEORIGIN)
                .xContentTypeOptions()
                .xXssProtection()
                .referrerPolicy(ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                .build();
    }

    // ==================== Instance Methods ====================

    /**
     * Applies all security headers to the given HttpHeaders.
     * 将所有安全头部应用到给定的 HttpHeaders。
     *
     * @param httpHeaders the headers to apply to - 要应用的头部
     */
    public void applyTo(HttpHeaders httpHeaders) {
        Objects.requireNonNull(httpHeaders, "httpHeaders must not be null");
        headers.forEach(httpHeaders::set);
    }

    /**
     * Returns an unmodifiable map of all security headers.
     * 返回所有安全头部的不可修改 Map。
     *
     * @return unmodifiable headers map - 不可修改的头部 Map
     */
    public Map<String, String> toMap() {
        return headers;
    }

    @Override
    public String toString() {
        return "SecurityHeaders" + headers;
    }

    // ==================== Enums ====================

    /**
     * X-Frame-Options header values.
     * X-Frame-Options 头部值。
     *
     * @since JDK 25, opencode-base-web V1.0.3
     */
    public enum FrameOption {

        /** Page cannot be displayed in a frame - 页面不能在框架中显示 */
        DENY("DENY"),

        /** Page can only be displayed in a frame on the same origin - 页面只能在同源框架中显示 */
        SAMEORIGIN("SAMEORIGIN");

        private final String value;

        FrameOption(String value) {
            this.value = value;
        }

        /**
         * Returns the header value.
         * 返回头部值。
         *
         * @return the value - 头部值
         */
        public String value() {
            return value;
        }
    }

    /**
     * Referrer-Policy header values.
     * Referrer-Policy 头部值。
     *
     * @since JDK 25, opencode-base-web V1.0.3
     */
    public enum ReferrerPolicy {

        /** No referrer information is sent - 不发送来源信息 */
        NO_REFERRER("no-referrer"),

        /** Referrer sent for same-protocol navigations - 同协议导航时发送来源 */
        NO_REFERRER_WHEN_DOWNGRADE("no-referrer-when-downgrade"),

        /** Only the origin is sent as referrer - 仅发送来源域 */
        ORIGIN("origin"),

        /** Full URL for same-origin, origin for cross-origin - 同源发送完整 URL，跨域仅发送来源域 */
        ORIGIN_WHEN_CROSS_ORIGIN("origin-when-cross-origin"),

        /** Full URL for same-origin, no referrer for cross-origin - 同源发送完整 URL，跨域不发送 */
        SAME_ORIGIN("same-origin"),

        /** Origin for cross-origin HTTPS requests only - 仅跨域 HTTPS 请求发送来源域 */
        STRICT_ORIGIN("strict-origin"),

        /** Full URL for same-origin, origin for secure cross-origin - 同源发送完整 URL，安全跨域发送来源域 */
        STRICT_ORIGIN_WHEN_CROSS_ORIGIN("strict-origin-when-cross-origin"),

        /** Full URL always - 始终发送完整 URL */
        UNSAFE_URL("unsafe-url");

        private final String value;

        ReferrerPolicy(String value) {
            this.value = value;
        }

        /**
         * Returns the header value.
         * 返回头部值。
         *
         * @return the value - 头部值
         */
        public String value() {
            return value;
        }
    }

    // ==================== Builder ====================

    /**
     * Builder for SecurityHeaders - fluent API for constructing security header sets.
     * SecurityHeaders 构建器 - 用于构建安全头部集的流式 API。
     *
     * @since JDK 25, opencode-base-web V1.0.3
     */
    public static final class Builder {

        private final Map<String, String> headers = new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * Sets the Content-Security-Policy header.
         * 设置 Content-Security-Policy 头部。
         *
         * @param policy the CSP policy string - CSP 策略字符串
         * @return this builder - 此构建器
         */
        public Builder contentSecurityPolicy(String policy) {
            Objects.requireNonNull(policy, "policy must not be null");
            headers.put(CONTENT_SECURITY_POLICY, policy);
            return this;
        }

        /**
         * Sets the Strict-Transport-Security header.
         * 设置 Strict-Transport-Security 头部。
         *
         * @param maxAge            the max age in seconds - 最大时间（秒）
         * @param includeSubDomains whether to include subdomains - 是否包含子域名
         * @return this builder - 此构建器
         * @throws IllegalArgumentException if maxAge is negative - 如果 maxAge 为负数
         */
        public Builder strictTransportSecurity(long maxAge, boolean includeSubDomains) {
            if (maxAge < 0) {
                throw new IllegalArgumentException("maxAge must not be negative: " + maxAge);
            }
            String value = includeSubDomains
                    ? "max-age=" + maxAge + "; includeSubDomains"
                    : "max-age=" + maxAge;
            headers.put(STRICT_TRANSPORT_SECURITY, value);
            return this;
        }

        /**
         * Sets the X-Frame-Options header.
         * 设置 X-Frame-Options 头部。
         *
         * @param option the frame option - 框架选项
         * @return this builder - 此构建器
         */
        public Builder xFrameOptions(FrameOption option) {
            Objects.requireNonNull(option, "option must not be null");
            headers.put(X_FRAME_OPTIONS, option.value());
            return this;
        }

        /**
         * Sets the X-Content-Type-Options header to "nosniff".
         * 设置 X-Content-Type-Options 头部为 "nosniff"。
         *
         * @return this builder - 此构建器
         */
        public Builder xContentTypeOptions() {
            headers.put(X_CONTENT_TYPE_OPTIONS, "nosniff");
            return this;
        }

        /**
         * Sets the X-XSS-Protection header to "0" (modern best practice: disable browser XSS filter).
         * 设置 X-XSS-Protection 头部为 "0"（现代最佳实践：禁用浏览器 XSS 过滤器）。
         *
         * @return this builder - 此构建器
         */
        public Builder xXssProtection() {
            headers.put(X_XSS_PROTECTION, "0");
            return this;
        }

        /**
         * Sets the Referrer-Policy header.
         * 设置 Referrer-Policy 头部。
         *
         * @param policy the referrer policy - 来源策略
         * @return this builder - 此构建器
         */
        public Builder referrerPolicy(ReferrerPolicy policy) {
            Objects.requireNonNull(policy, "policy must not be null");
            headers.put(REFERRER_POLICY, policy.value());
            return this;
        }

        /**
         * Sets the Permissions-Policy header.
         * 设置 Permissions-Policy 头部。
         *
         * @param policy the permissions policy string - 权限策略字符串
         * @return this builder - 此构建器
         */
        public Builder permissionsPolicy(String policy) {
            Objects.requireNonNull(policy, "policy must not be null");
            headers.put(PERMISSIONS_POLICY, policy);
            return this;
        }

        /**
         * Sets the Cross-Origin-Embedder-Policy header.
         * 设置 Cross-Origin-Embedder-Policy 头部。
         *
         * @param policy the COEP value (e.g., "require-corp", "unsafe-none") - COEP 值
         * @return this builder - 此构建器
         */
        public Builder crossOriginEmbedderPolicy(String policy) {
            Objects.requireNonNull(policy, "policy must not be null");
            headers.put(CROSS_ORIGIN_EMBEDDER_POLICY, policy);
            return this;
        }

        /**
         * Sets the Cross-Origin-Opener-Policy header.
         * 设置 Cross-Origin-Opener-Policy 头部。
         *
         * @param policy the COOP value (e.g., "same-origin", "same-origin-allow-popups") - COOP 值
         * @return this builder - 此构建器
         */
        public Builder crossOriginOpenerPolicy(String policy) {
            Objects.requireNonNull(policy, "policy must not be null");
            headers.put(CROSS_ORIGIN_OPENER_POLICY, policy);
            return this;
        }

        /**
         * Builds the SecurityHeaders.
         * 构建 SecurityHeaders。
         *
         * @return the SecurityHeaders - SecurityHeaders 实例
         */
        public SecurityHeaders build() {
            return new SecurityHeaders(headers);
        }
    }
}
