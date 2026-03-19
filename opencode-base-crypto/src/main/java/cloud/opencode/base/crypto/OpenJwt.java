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

package cloud.opencode.base.crypto;

import cloud.opencode.base.crypto.jwt.JwtAlgorithm;
import cloud.opencode.base.crypto.jwt.JwtClaims;
import cloud.opencode.base.crypto.jwt.JwtUtil;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Duration;
import java.util.Objects;

/**
 * OpenJWT Facade - Simplified API for JWT operations
 * OpenJWT 门面类 - 简化的 JWT 操作 API
 *
 * <p>This facade provides easy-to-use static methods for common JWT operations,
 * including creation and verification with various algorithms.</p>
 * <p>此门面类提供易于使用的静态方法用于常见的 JWT 操作，包括使用各种算法创建和验证。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>HMAC symmetric signing (HS256/384/512) - HMAC 对称签名</li>
 *   <li>RSA asymmetric signing (RS256/384/512) - RSA 非对称签名</li>
 *   <li>ECDSA asymmetric signing (ES256/384/512) - ECDSA 非对称签名</li>
 *   <li>Claim validation - 声明验证</li>
 *   <li>Expiration checking - 过期检查</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Quick JWT creation with HMAC
 * String token = OpenJwt.sign("user123", "secret-key", Duration.ofHours(1));
 *
 * // Verify and get claims
 * JwtClaims claims = OpenJwt.verify(token, "secret-key");
 *
 * // Create JWT with custom claims
 * JwtClaims customClaims = JwtClaims.builder()
 *     .subject("user123")
 *     .issuer("auth-service")
 *     .expiresIn(Duration.ofHours(1))
 *     .claim("role", "admin")
 *     .build();
 * String customToken = OpenJwt.sign(customClaims, "secret-key");
 *
 * // Create JWT with RSA
 * KeyPair keyPair = OpenJwt.generateRsaKeyPair();
 * String rsaToken = OpenJwt.signRsa("user123", keyPair.getPrivate(), Duration.ofHours(1));
 * JwtClaims rsaClaims = OpenJwt.verify(rsaToken, keyPair.getPublic());
 * }</pre>
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
public final class OpenJwt {

    private OpenJwt() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    // ==================== Quick Sign Methods ====================

    /**
     * Creates a JWT with HMAC-SHA256.
     * 使用 HMAC-SHA256 创建 JWT。
     *
     * @param subject    the subject (e.g., user ID)
     * @param secret     the secret key
     * @param expiration the expiration duration
     * @return the JWT string
     */
    public static String sign(String subject, String secret, Duration expiration) {
        return JwtUtil.quickSign(subject, secret, expiration);
    }

    /**
     * Creates a JWT with HMAC-SHA256 using JwtClaims.
     * 使用 JwtClaims 和 HMAC-SHA256 创建 JWT。
     *
     * @param claims the JWT claims
     * @param secret the secret key
     * @return the JWT string
     */
    public static String sign(JwtClaims claims, String secret) {
        return JwtUtil.quickSign(claims, secret);
    }

    /**
     * Creates a JWT with specified HMAC algorithm.
     * 使用指定的 HMAC 算法创建 JWT。
     *
     * @param claims    the JWT claims
     * @param secret    the secret key
     * @param algorithm the HMAC algorithm (HS256, HS384, HS512)
     * @return the JWT string
     */
    public static String sign(JwtClaims claims, String secret, JwtAlgorithm algorithm) {
        Objects.requireNonNull(claims, "claims must not be null");
        Objects.requireNonNull(secret, "secret must not be null");
        Objects.requireNonNull(algorithm, "algorithm must not be null");

        return JwtUtil.builder()
                .algorithm(algorithm)
                .secret(secret)
                .claims(claims)
                .sign();
    }

    // ==================== RSA Sign Methods ====================

    /**
     * Creates a JWT with RSA-SHA256.
     * 使用 RSA-SHA256 创建 JWT。
     *
     * @param subject    the subject
     * @param privateKey the RSA private key
     * @param expiration the expiration duration
     * @return the JWT string
     */
    public static String signRsa(String subject, PrivateKey privateKey, Duration expiration) {
        return JwtUtil.builder()
                .algorithm(JwtAlgorithm.RS256)
                .privateKey(privateKey)
                .subject(subject)
                .issuedAtNow()
                .expiresIn(expiration)
                .sign();
    }

    /**
     * Creates a JWT with RSA-SHA256 using JwtClaims.
     * 使用 JwtClaims 和 RSA-SHA256 创建 JWT。
     *
     * @param claims     the JWT claims
     * @param privateKey the RSA private key
     * @return the JWT string
     */
    public static String signRsa(JwtClaims claims, PrivateKey privateKey) {
        return signRsa(claims, privateKey, JwtAlgorithm.RS256);
    }

    /**
     * Creates a JWT with specified RSA algorithm.
     * 使用指定的 RSA 算法创建 JWT。
     *
     * @param claims     the JWT claims
     * @param privateKey the RSA private key
     * @param algorithm  the RSA algorithm (RS256, RS384, RS512, PS256, PS384, PS512)
     * @return the JWT string
     */
    public static String signRsa(JwtClaims claims, PrivateKey privateKey, JwtAlgorithm algorithm) {
        Objects.requireNonNull(claims, "claims must not be null");
        Objects.requireNonNull(privateKey, "privateKey must not be null");
        Objects.requireNonNull(algorithm, "algorithm must not be null");

        return JwtUtil.builder()
                .algorithm(algorithm)
                .privateKey(privateKey)
                .claims(claims)
                .sign();
    }

    // ==================== ECDSA Sign Methods ====================

    /**
     * Creates a JWT with ECDSA-SHA256.
     * 使用 ECDSA-SHA256 创建 JWT。
     *
     * @param subject    the subject
     * @param privateKey the EC private key
     * @param expiration the expiration duration
     * @return the JWT string
     */
    public static String signEc(String subject, PrivateKey privateKey, Duration expiration) {
        return JwtUtil.builder()
                .algorithm(JwtAlgorithm.ES256)
                .privateKey(privateKey)
                .subject(subject)
                .issuedAtNow()
                .expiresIn(expiration)
                .sign();
    }

    /**
     * Creates a JWT with ECDSA-SHA256 using JwtClaims.
     * 使用 JwtClaims 和 ECDSA-SHA256 创建 JWT。
     *
     * @param claims     the JWT claims
     * @param privateKey the EC private key
     * @return the JWT string
     */
    public static String signEc(JwtClaims claims, PrivateKey privateKey) {
        return signEc(claims, privateKey, JwtAlgorithm.ES256);
    }

    /**
     * Creates a JWT with specified ECDSA algorithm.
     * 使用指定的 ECDSA 算法创建 JWT。
     *
     * @param claims     the JWT claims
     * @param privateKey the EC private key
     * @param algorithm  the ECDSA algorithm (ES256, ES384, ES512)
     * @return the JWT string
     */
    public static String signEc(JwtClaims claims, PrivateKey privateKey, JwtAlgorithm algorithm) {
        Objects.requireNonNull(claims, "claims must not be null");
        Objects.requireNonNull(privateKey, "privateKey must not be null");
        Objects.requireNonNull(algorithm, "algorithm must not be null");

        return JwtUtil.builder()
                .algorithm(algorithm)
                .privateKey(privateKey)
                .claims(claims)
                .sign();
    }

    // ==================== Verification ====================

    /**
     * Verifies a JWT with HMAC secret.
     * 使用 HMAC 密钥验证 JWT。
     *
     * @param token  the JWT string
     * @param secret the secret key
     * @return the verified claims
     */
    public static JwtClaims verify(String token, String secret) {
        return JwtUtil.verify(token, secret);
    }

    /**
     * Verifies a JWT with public key.
     * 使用公钥验证 JWT。
     *
     * @param token     the JWT string
     * @param publicKey the public key (RSA or EC)
     * @return the verified claims
     */
    public static JwtClaims verify(String token, PublicKey publicKey) {
        return JwtUtil.verify(token, publicKey);
    }

    /**
     * Parses a JWT without verification (unsafe).
     * 解析 JWT 但不验证（不安全）。
     *
     * @param token the JWT string
     * @return the claims (unverified)
     */
    public static JwtClaims parseUnsafe(String token) {
        return JwtUtil.parseUnsafe(token);
    }

    // ==================== Key Generation ====================

    /**
     * Generates an RSA key pair for JWT signing.
     * 生成用于 JWT 签名的 RSA 密钥对。
     *
     * @return the RSA key pair
     */
    public static KeyPair generateRsaKeyPair() {
        return generateRsaKeyPair(2048);
    }

    /**
     * Generates an RSA key pair with specified key size.
     * 生成指定密钥大小的 RSA 密钥对。
     *
     * @param keySize the key size in bits
     * @return the RSA key pair
     */
    public static KeyPair generateRsaKeyPair(int keySize) {
        try {
            java.security.KeyPairGenerator keyGen = java.security.KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(keySize);
            return keyGen.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate RSA key pair", e);
        }
    }

    /**
     * Generates an EC key pair for JWT signing.
     * 生成用于 JWT 签名的 EC 密钥对。
     *
     * @return the EC key pair (P-256)
     */
    public static KeyPair generateEcKeyPair() {
        return generateEcKeyPair("secp256r1");
    }

    /**
     * Generates an EC key pair with specified curve.
     * 生成指定曲线的 EC 密钥对。
     *
     * @param curveName the curve name (secp256r1, secp384r1, secp521r1)
     * @return the EC key pair
     */
    public static KeyPair generateEcKeyPair(String curveName) {
        try {
            java.security.KeyPairGenerator keyGen = java.security.KeyPairGenerator.getInstance("EC");
            keyGen.initialize(new java.security.spec.ECGenParameterSpec(curveName));
            return keyGen.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate EC key pair", e);
        }
    }

    // ==================== Builder Access ====================

    /**
     * Creates a new JWT builder for advanced configuration.
     * 创建新的 JWT 构建器用于高级配置。
     *
     * @return a new JwtUtil.Builder instance
     */
    public static JwtUtil.Builder builder() {
        return JwtUtil.builder();
    }

    /**
     * Creates a new claims builder.
     * 创建新的声明构建器。
     *
     * @return a new JwtClaims.Builder instance
     */
    public static JwtClaims.Builder claims() {
        return JwtClaims.builder();
    }
}
