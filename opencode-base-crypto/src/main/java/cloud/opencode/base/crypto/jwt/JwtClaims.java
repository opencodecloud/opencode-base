/*
 * Copyright 2025 OpenCode Cloud Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.opencode.base.crypto.jwt;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * JWT Claims - Container for JWT claims (payload)
 * JWT 声明 - JWT 声明（载荷）容器
 *
 * <p>Provides a fluent builder API for constructing JWT claims with
 * both standard registered claims and custom claims.</p>
 * <p>提供流式构建器 API 用于构造 JWT 声明，支持标准注册声明和自定义声明。</p>
 *
 * <p><strong>Standard Claims (RFC 7519) | 标准声明:</strong></p>
 * <ul>
 *   <li>iss (issuer) - 签发者</li>
 *   <li>sub (subject) - 主题</li>
 *   <li>aud (audience) - 受众</li>
 *   <li>exp (expiration time) - 过期时间</li>
 *   <li>nbf (not before) - 生效时间</li>
 *   <li>iat (issued at) - 签发时间</li>
 *   <li>jti (JWT ID) - JWT 唯一标识</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * JwtClaims claims = JwtClaims.builder()
 *     .issuer("auth-service")
 *     .subject("user123")
 *     .audience("api-service")
 *     .expiresIn(Duration.ofHours(1))
 *     .claim("role", "admin")
 *     .claim("permissions", List.of("read", "write"))
 *     .build();
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>See class description for details - 详见类描述</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.2.0
 */
public final class JwtClaims {

    // Registered claim names
    public static final String ISSUER = "iss";
    public static final String SUBJECT = "sub";
    public static final String AUDIENCE = "aud";
    public static final String EXPIRATION = "exp";
    public static final String NOT_BEFORE = "nbf";
    public static final String ISSUED_AT = "iat";
    public static final String JWT_ID = "jti";

    private final Map<String, Object> claims;

    private JwtClaims(Map<String, Object> claims) {
        this.claims = Collections.unmodifiableMap(new LinkedHashMap<>(claims));
    }

    /**
     * Creates a new claims builder.
     * 创建新的声明构建器。
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates claims from a map.
     * 从映射创建声明。
     *
     * @param claims the claims map
     * @return JwtClaims instance
     */
    public static JwtClaims of(Map<String, Object> claims) {
        return new JwtClaims(claims);
    }

    /**
     * Creates empty claims.
     * 创建空声明。
     *
     * @return empty JwtClaims instance
     */
    public static JwtClaims empty() {
        return new JwtClaims(Map.of());
    }

    // ==================== Standard Claims Getters ====================

    /**
     * Returns the issuer claim.
     * 返回签发者声明。
     *
     * @return the issuer, or null if not present
     */
    public String issuer() {
        return getString(ISSUER);
    }

    /**
     * Returns the subject claim.
     * 返回主题声明。
     *
     * @return the subject, or null if not present
     */
    public String subject() {
        return getString(SUBJECT);
    }

    /**
     * Returns the audience claim.
     * 返回受众声明。
     *
     * @return the audience, or null if not present
     */
    @SuppressWarnings("unchecked")
    public List<String> audience() {
        Object aud = claims.get(AUDIENCE);
        if (aud == null) {
            return null;
        }
        if (aud instanceof String s) {
            return List.of(s);
        }
        if (aud instanceof List<?>) {
            return (List<String>) aud;
        }
        return List.of(aud.toString());
    }

    /**
     * Returns the expiration time.
     * 返回过期时间。
     *
     * @return the expiration instant, or null if not present
     */
    public Instant expiration() {
        return getInstant(EXPIRATION);
    }

    /**
     * Returns the not-before time.
     * 返回生效时间。
     *
     * @return the not-before instant, or null if not present
     */
    public Instant notBefore() {
        return getInstant(NOT_BEFORE);
    }

    /**
     * Returns the issued-at time.
     * 返回签发时间。
     *
     * @return the issued-at instant, or null if not present
     */
    public Instant issuedAt() {
        return getInstant(ISSUED_AT);
    }

    /**
     * Returns the JWT ID.
     * 返回 JWT ID。
     *
     * @return the JWT ID, or null if not present
     */
    public String jwtId() {
        return getString(JWT_ID);
    }

    // ==================== Generic Claim Access ====================

    /**
     * Returns a claim value.
     * 返回声明值。
     *
     * @param name the claim name
     * @return the claim value, or null if not present
     */
    public Object get(String name) {
        return claims.get(name);
    }

    /**
     * Returns a claim value as String.
     * 返回字符串类型的声明值。
     *
     * @param name the claim name
     * @return the claim value as string, or null if not present
     */
    public String getString(String name) {
        Object value = claims.get(name);
        return value != null ? value.toString() : null;
    }

    /**
     * Returns a claim value as Integer.
     * 返回整数类型的声明值。
     *
     * @param name the claim name
     * @return the claim value as integer, or null if not present
     */
    public Integer getInt(String name) {
        Object value = claims.get(name);
        if (value == null) return null;
        if (value instanceof Number n) return n.intValue();
        return Integer.parseInt(value.toString());
    }

    /**
     * Returns a claim value as Long.
     * 返回长整数类型的声明值。
     *
     * @param name the claim name
     * @return the claim value as long, or null if not present
     */
    public Long getLong(String name) {
        Object value = claims.get(name);
        if (value == null) return null;
        if (value instanceof Number n) return n.longValue();
        return Long.parseLong(value.toString());
    }

    /**
     * Returns a claim value as Boolean.
     * 返回布尔类型的声明值。
     *
     * @param name the claim name
     * @return the claim value as boolean, or null if not present
     */
    public Boolean getBoolean(String name) {
        Object value = claims.get(name);
        if (value == null) return null;
        if (value instanceof Boolean b) return b;
        return Boolean.parseBoolean(value.toString());
    }

    /**
     * Returns a claim value as Instant.
     * 返回 Instant 类型的声明值。
     *
     * @param name the claim name
     * @return the claim value as instant, or null if not present
     */
    public Instant getInstant(String name) {
        Object value = claims.get(name);
        if (value == null) return null;
        if (value instanceof Instant i) return i;
        if (value instanceof Number n) {
            return Instant.ofEpochSecond(n.longValue());
        }
        return Instant.ofEpochSecond(Long.parseLong(value.toString()));
    }

    /**
     * Returns a claim value as List.
     * 返回列表类型的声明值。
     *
     * @param name the claim name
     * @param <T>  the element type
     * @return the claim value as list, or null if not present
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String name) {
        Object value = claims.get(name);
        if (value == null) return null;
        if (value instanceof List<?> list) return (List<T>) list;
        return List.of((T) value);
    }

    /**
     * Checks if a claim is present.
     * 检查声明是否存在。
     *
     * @param name the claim name
     * @return true if the claim exists
     */
    public boolean contains(String name) {
        return claims.containsKey(name);
    }

    /**
     * Returns all claim names.
     * 返回所有声明名称。
     *
     * @return the set of claim names
     */
    public Set<String> names() {
        return claims.keySet();
    }

    /**
     * Returns the claims as a map.
     * 返回声明映射。
     *
     * @return unmodifiable claims map
     */
    public Map<String, Object> asMap() {
        return claims;
    }

    /**
     * Checks if the token has expired.
     * 检查令牌是否已过期。
     *
     * @return true if expired
     */
    public boolean isExpired() {
        Instant exp = expiration();
        return exp != null && Instant.now().isAfter(exp);
    }

    /**
     * Checks if the token is not yet valid.
     * 检查令牌是否尚未生效。
     *
     * @return true if not yet valid
     */
    public boolean isNotYetValid() {
        Instant nbf = notBefore();
        return nbf != null && Instant.now().isBefore(nbf);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JwtClaims that)) return false;
        return claims.equals(that.claims);
    }

    @Override
    public int hashCode() {
        return claims.hashCode();
    }

    @Override
    public String toString() {
        return "JwtClaims" + claims;
    }

    // ==================== Builder ====================

    /**
     * Builder for JwtClaims.
     * JwtClaims 构建器。
     */
    public static final class Builder {
        private final Map<String, Object> claims = new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * Sets the issuer claim.
         * 设置签发者声明。
         *
         * @param issuer the issuer
         * @return this builder
         */
        public Builder issuer(String issuer) {
            return claim(ISSUER, issuer);
        }

        /**
         * Sets the subject claim.
         * 设置主题声明。
         *
         * @param subject the subject
         * @return this builder
         */
        public Builder subject(String subject) {
            return claim(SUBJECT, subject);
        }

        /**
         * Sets the audience claim.
         * 设置受众声明。
         *
         * @param audience the audience
         * @return this builder
         */
        public Builder audience(String audience) {
            return claim(AUDIENCE, audience);
        }

        /**
         * Sets the audience claim with multiple values.
         * 设置多值受众声明。
         *
         * @param audiences the audiences
         * @return this builder
         */
        public Builder audience(List<String> audiences) {
            return claim(AUDIENCE, audiences);
        }

        /**
         * Sets the expiration time.
         * 设置过期时间。
         *
         * @param expiration the expiration instant
         * @return this builder
         */
        public Builder expiration(Instant expiration) {
            return claim(EXPIRATION, expiration.getEpochSecond());
        }

        /**
         * Sets the expiration time relative to now.
         * 设置相对于当前时间的过期时间。
         *
         * @param duration the duration from now
         * @return this builder
         */
        public Builder expiresIn(Duration duration) {
            return expiration(Instant.now().plus(duration));
        }

        /**
         * Sets the not-before time.
         * 设置生效时间。
         *
         * @param notBefore the not-before instant
         * @return this builder
         */
        public Builder notBefore(Instant notBefore) {
            return claim(NOT_BEFORE, notBefore.getEpochSecond());
        }

        /**
         * Sets the issued-at time to now.
         * 设置签发时间为当前时间。
         *
         * @return this builder
         */
        public Builder issuedAtNow() {
            return issuedAt(Instant.now());
        }

        /**
         * Sets the issued-at time.
         * 设置签发时间。
         *
         * @param issuedAt the issued-at instant
         * @return this builder
         */
        public Builder issuedAt(Instant issuedAt) {
            return claim(ISSUED_AT, issuedAt.getEpochSecond());
        }

        /**
         * Sets the JWT ID.
         * 设置 JWT ID。
         *
         * @param jwtId the JWT ID
         * @return this builder
         */
        public Builder jwtId(String jwtId) {
            return claim(JWT_ID, jwtId);
        }

        /**
         * Generates a random JWT ID.
         * 生成随机 JWT ID。
         *
         * @return this builder
         */
        public Builder generateJwtId() {
            return jwtId(UUID.randomUUID().toString());
        }

        /**
         * Sets a custom claim.
         * 设置自定义声明。
         *
         * @param name  the claim name
         * @param value the claim value
         * @return this builder
         */
        public Builder claim(String name, Object value) {
            Objects.requireNonNull(name, "claim name must not be null");
            if (value != null) {
                claims.put(name, value);
            }
            return this;
        }

        /**
         * Sets multiple claims from a map.
         * 从映射设置多个声明。
         *
         * @param claims the claims map
         * @return this builder
         */
        public Builder claims(Map<String, Object> claims) {
            Objects.requireNonNull(claims, "claims must not be null");
            this.claims.putAll(claims);
            return this;
        }

        /**
         * Builds the JwtClaims instance.
         * 构建 JwtClaims 实例。
         *
         * @return the JwtClaims
         */
        public JwtClaims build() {
            return new JwtClaims(claims);
        }
    }
}
