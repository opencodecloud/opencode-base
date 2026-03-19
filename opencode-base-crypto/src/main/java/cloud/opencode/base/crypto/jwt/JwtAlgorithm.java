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

/**
 * JWT Algorithm - Supported algorithms for JWT signing
 * JWT 算法 - 支持的 JWT 签名算法
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>HMAC (HS256/384/512) algorithm definitions - HMAC（HS256/384/512）算法定义</li>
 *   <li>RSA (RS256/384/512, PS256/384/512) algorithm definitions - RSA（RS256/384/512、PS256/384/512）算法定义</li>
 *   <li>ECDSA (ES256/384/512) and EdDSA algorithm definitions - ECDSA（ES256/384/512）和 EdDSA 算法定义</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * JwtAlgorithm alg = JwtAlgorithm.HS256;
 * String jcaName = alg.jcaName();
 * boolean symmetric = alg.isSymmetric();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Partial - 空值安全: 部分</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) - 时间复杂度: O(1)</li>
 *   <li>Space complexity: O(1) - 空间复杂度: O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.2.0
 */
public enum JwtAlgorithm {

    /**
     * HMAC with SHA-256
     */
    HS256("HS256", "HmacSHA256", "symmetric"),

    /**
     * HMAC with SHA-384
     */
    HS384("HS384", "HmacSHA384", "symmetric"),

    /**
     * HMAC with SHA-512
     */
    HS512("HS512", "HmacSHA512", "symmetric"),

    /**
     * RSA PKCS#1 with SHA-256
     */
    RS256("RS256", "SHA256withRSA", "asymmetric"),

    /**
     * RSA PKCS#1 with SHA-384
     */
    RS384("RS384", "SHA384withRSA", "asymmetric"),

    /**
     * RSA PKCS#1 with SHA-512
     */
    RS512("RS512", "SHA512withRSA", "asymmetric"),

    /**
     * RSA-PSS with SHA-256
     */
    PS256("PS256", "SHA256withRSAandMGF1", "asymmetric"),

    /**
     * RSA-PSS with SHA-384
     */
    PS384("PS384", "SHA384withRSAandMGF1", "asymmetric"),

    /**
     * RSA-PSS with SHA-512
     */
    PS512("PS512", "SHA512withRSAandMGF1", "asymmetric"),

    /**
     * ECDSA with P-256 and SHA-256
     */
    ES256("ES256", "SHA256withECDSA", "asymmetric"),

    /**
     * ECDSA with P-384 and SHA-384
     */
    ES384("ES384", "SHA384withECDSA", "asymmetric"),

    /**
     * ECDSA with P-521 and SHA-512
     */
    ES512("ES512", "SHA512withECDSA", "asymmetric"),

    /**
     * EdDSA with Ed25519
     */
    EdDSA("EdDSA", "Ed25519", "asymmetric");

    private final String name;
    private final String jcaName;
    private final String type;

    JwtAlgorithm(String name, String jcaName, String type) {
        this.name = name;
        this.jcaName = jcaName;
        this.type = type;
    }

    /**
     * Returns the JWT algorithm name (e.g., "HS256", "RS256").
     * 返回 JWT 算法名称（例如："HS256"、"RS256"）。
     *
     * @return the algorithm name
     */
    public String algorithmName() {
        return name;
    }

    /**
     * Returns the JCA algorithm name.
     * 返回 JCA 算法名称。
     *
     * @return the JCA algorithm name
     */
    public String jcaName() {
        return jcaName;
    }

    /**
     * Returns the algorithm type ("symmetric" or "asymmetric").
     * 返回算法类型（"symmetric" 或 "asymmetric"）。
     *
     * @return the algorithm type
     */
    public String type() {
        return type;
    }

    /**
     * Checks if this is a symmetric algorithm.
     * 检查是否为对称算法。
     *
     * @return true if symmetric
     */
    public boolean isSymmetric() {
        return "symmetric".equals(type);
    }

    /**
     * Checks if this is an asymmetric algorithm.
     * 检查是否为非对称算法。
     *
     * @return true if asymmetric
     */
    public boolean isAsymmetric() {
        return "asymmetric".equals(type);
    }

    /**
     * Parses algorithm from string name.
     * 从字符串名称解析算法。
     *
     * @param name the algorithm name
     * @return the algorithm
     * @throws IllegalArgumentException if algorithm is not supported
     */
    public static JwtAlgorithm fromName(String name) {
        for (JwtAlgorithm alg : values()) {
            if (alg.name.equalsIgnoreCase(name)) {
                return alg;
            }
        }
        throw new IllegalArgumentException("Unsupported JWT algorithm: " + name);
    }
}
