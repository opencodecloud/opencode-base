package cloud.opencode.base.crypto.policy;

import java.util.*;

/**
 * Immutable cryptographic algorithm policy for enforcing allowed/denied algorithms and minimum key sizes.
 * 不可变的加密算法策略，用于强制允许/拒绝的算法和最小密钥长度。
 *
 * <p>Provides predefined policies ({@link #strict()}, {@link #standard()}, {@link #legacy()})
 * and a {@link Builder} for custom policies. Algorithm name matching is case-insensitive.</p>
 * <p>提供预定义策略（{@link #strict()}、{@link #standard()}、{@link #legacy()}）
 * 以及用于自定义策略的 {@link Builder}。算法名称匹配不区分大小写。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Predefined strict/standard/legacy policies - 预定义严格/标准/兼容策略</li>
 *   <li>Custom policy via Builder pattern - 通过 Builder 模式自定义策略</li>
 *   <li>Minimum key size enforcement - 最小密钥长度强制</li>
 *   <li>Case-insensitive algorithm matching - 大小写不敏感的算法匹配</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Use predefined strict policy
 * CryptoPolicy policy = CryptoPolicy.strict();
 * policy.check("AES-256-GCM", 256); // OK
 * policy.check("MD5", 0);           // throws PolicyViolationException
 *
 * // Custom policy
 * CryptoPolicy custom = CryptoPolicy.builder()
 *     .allow("AES-256-GCM", "SHA-256")
 *     .deny("MD5", "SHA-1")
 *     .minKeyBits("RSA", 2048)
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.3
 */
public final class CryptoPolicy {

    private final Set<String> allowedAlgorithms;
    private final Set<String> deniedAlgorithms;
    private final Map<String, Integer> minKeyBits;

    private CryptoPolicy(Set<String> allowedAlgorithms, Set<String> deniedAlgorithms,
                          Map<String, Integer> minKeyBits) {
        this.allowedAlgorithms = allowedAlgorithms;
        this.deniedAlgorithms = deniedAlgorithms;
        this.minKeyBits = minKeyBits;
    }

    /**
     * Returns a strict policy allowing only the strongest modern algorithms.
     * 返回仅允许最强现代算法的严格策略。
     *
     * <p>Allowed: AES-256-GCM, ChaCha20-Poly1305, Ed25519, X25519, SHA-256, SHA-384,
     * SHA-512, SHA3-256, SHA3-512, Argon2id, ECDSA-P256, ECDSA-P384, RSA-PSS(>=4096)</p>
     *
     * @return strict policy instance | 严格策略实例
     */
    public static CryptoPolicy strict() {
        return builder()
                .allow("AES-256-GCM", "ChaCha20-Poly1305", "Ed25519", "X25519",
                        "SHA-256", "SHA-384", "SHA-512", "SHA3-256", "SHA3-512",
                        "Argon2id", "ECDSA-P256", "ECDSA-P384", "RSA-PSS")
                .minKeyBits("RSA-PSS", 4096)
                .build();
    }

    /**
     * Returns a standard policy allowing commonly used secure algorithms.
     * 返回允许常用安全算法的标准策略。
     *
     * <p>Includes all strict algorithms plus: AES-128-GCM, AES-256-CBC,
     * RSA-OAEP(>=2048), ECDSA-P521, PBKDF2, BCrypt, SCrypt, Ed448, X448, RSA(>=2048)</p>
     *
     * @return standard policy instance | 标准策略实例
     */
    public static CryptoPolicy standard() {
        return builder()
                .allow("AES-256-GCM", "ChaCha20-Poly1305", "Ed25519", "X25519",
                        "SHA-256", "SHA-384", "SHA-512", "SHA3-256", "SHA3-512",
                        "Argon2id", "ECDSA-P256", "ECDSA-P384", "RSA-PSS",
                        "AES-128-GCM", "AES-256-CBC", "RSA-OAEP", "ECDSA-P521",
                        "PBKDF2", "BCrypt", "SCrypt", "Ed448", "X448", "RSA")
                .minKeyBits("RSA-PSS", 4096)
                .minKeyBits("RSA-OAEP", 2048)
                .minKeyBits("RSA", 2048)
                .build();
    }

    /**
     * Returns a legacy policy allowing older algorithms for backward compatibility.
     * 返回允许旧算法以实现向后兼容的兼容策略。
     *
     * <p>Includes all standard algorithms plus: AES-128-CBC, RSA(>=1024), 3DES,
     * SHA-1 (non-signature), MD5 (non-security)</p>
     *
     * @return legacy policy instance | 兼容策略实例
     */
    public static CryptoPolicy legacy() {
        return builder()
                .allow("AES-256-GCM", "ChaCha20-Poly1305", "Ed25519", "X25519",
                        "SHA-256", "SHA-384", "SHA-512", "SHA3-256", "SHA3-512",
                        "Argon2id", "ECDSA-P256", "ECDSA-P384", "RSA-PSS",
                        "AES-128-GCM", "AES-256-CBC", "RSA-OAEP", "ECDSA-P521",
                        "PBKDF2", "BCrypt", "SCrypt", "Ed448", "X448", "RSA",
                        "AES-128-CBC", "3DES", "SHA-1", "MD5")
                .minKeyBits("RSA-PSS", 4096)
                .minKeyBits("RSA-OAEP", 2048)
                .minKeyBits("RSA", 1024)
                .build();
    }

    /**
     * Creates a new Builder for constructing a custom CryptoPolicy.
     * 创建用于构建自定义 CryptoPolicy 的 Builder。
     *
     * @return a new Builder instance | 新的 Builder 实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Checks whether the given algorithm with key size is allowed, throwing an exception if not.
     * 检查给定算法和密钥长度是否被允许，如果不允许则抛出异常。
     *
     * @param algorithm the algorithm name (case-insensitive) | 算法名称（不区分大小写）
     * @param keyBits the key size in bits | 密钥长度（比特）
     * @throws PolicyViolationException if the algorithm or key size violates the policy | 当算法或密钥长度违反策略时抛出
     */
    public void check(String algorithm, int keyBits) {
        Objects.requireNonNull(algorithm, "algorithm must not be null");
        String upper = algorithm.toUpperCase(Locale.ROOT);

        // Check denied list first
        if (deniedAlgorithms.contains(upper)) {
            throw new PolicyViolationException(algorithm, "check",
                    "Algorithm '" + algorithm + "' is explicitly denied by current policy");
        }

        // Check allowed list
        if (!allowedAlgorithms.contains(upper)) {
            throw new PolicyViolationException(algorithm, "check",
                    "Algorithm '" + algorithm + "' is not allowed by current policy");
        }

        // Check minimum key bits
        Integer minBits = minKeyBits.get(upper);
        if (minBits != null && keyBits < minBits) {
            throw new PolicyViolationException(algorithm, "check",
                    "Algorithm '" + algorithm + "' requires at least " + minBits
                            + " bits, but " + keyBits + " bits provided");
        }
    }

    /**
     * Returns whether the given algorithm with key size is allowed by this policy.
     * 返回给定算法和密钥长度是否被此策略允许。
     *
     * @param algorithm the algorithm name (case-insensitive) | 算法名称（不区分大小写）
     * @param keyBits the key size in bits | 密钥长度（比特）
     * @return true if allowed, false otherwise | 如果允许则返回 true，否则返回 false
     */
    public boolean isAllowed(String algorithm, int keyBits) {
        Objects.requireNonNull(algorithm, "algorithm must not be null");
        String upper = algorithm.toUpperCase(Locale.ROOT);

        if (deniedAlgorithms.contains(upper)) {
            return false;
        }
        if (!allowedAlgorithms.contains(upper)) {
            return false;
        }
        Integer minBits = minKeyBits.get(upper);
        return minBits == null || keyBits >= minBits;
    }

    /**
     * Returns an unmodifiable set of allowed algorithm names (uppercase).
     * 返回允许的算法名称的不可修改集合（大写）。
     *
     * @return unmodifiable set of allowed algorithms | 不可修改的允许算法集合
     */
    public Set<String> getAllowedAlgorithms() {
        return allowedAlgorithms;
    }

    /**
     * Returns an unmodifiable set of denied algorithm names (uppercase).
     * 返回拒绝的算法名称的不可修改集合（大写）。
     *
     * @return unmodifiable set of denied algorithms | 不可修改的拒绝算法集合
     */
    public Set<String> getDeniedAlgorithms() {
        return deniedAlgorithms;
    }

    /**
     * Returns an unmodifiable map of minimum key size requirements (uppercase algorithm name to bits).
     * 返回最小密钥长度要求的不可修改映射（大写算法名称到比特数）。
     *
     * @return unmodifiable map of minimum key bits | 不可修改的最小密钥长度映射
     */
    public Map<String, Integer> getMinKeyBits() {
        return minKeyBits;
    }

    /**
     * Builder for constructing custom {@link CryptoPolicy} instances.
     * 用于构建自定义 {@link CryptoPolicy} 实例的构建器。
     */
    public static final class Builder {

        private final Set<String> allowed = new LinkedHashSet<>();
        private final Set<String> denied = new LinkedHashSet<>();
        private final Map<String, Integer> minKeyBits = new HashMap<>();

        private Builder() {
        }

        /**
         * Adds algorithms to the allowed set.
         * 将算法添加到允许集合。
         *
         * @param algorithms algorithm names to allow | 要允许的算法名称
         * @return this builder | 当前构建器
         */
        public Builder allow(String... algorithms) {
            for (String alg : algorithms) {
                Objects.requireNonNull(alg, "algorithm must not be null");
                allowed.add(alg.toUpperCase(Locale.ROOT));
            }
            return this;
        }

        /**
         * Adds algorithms to the denied set.
         * 将算法添加到拒绝集合。
         *
         * @param algorithms algorithm names to deny | 要拒绝的算法名称
         * @return this builder | 当前构建器
         */
        public Builder deny(String... algorithms) {
            for (String alg : algorithms) {
                Objects.requireNonNull(alg, "algorithm must not be null");
                denied.add(alg.toUpperCase(Locale.ROOT));
            }
            return this;
        }

        /**
         * Sets the minimum key size in bits for an algorithm.
         * 设置算法的最小密钥长度（比特）。
         *
         * @param algorithm the algorithm name | 算法名称
         * @param bits minimum key size in bits | 最小密钥长度（比特）
         * @return this builder | 当前构建器
         */
        public Builder minKeyBits(String algorithm, int bits) {
            Objects.requireNonNull(algorithm, "algorithm must not be null");
            if (bits < 0) {
                throw new IllegalArgumentException("minKeyBits must be non-negative");
            }
            minKeyBits.put(algorithm.toUpperCase(Locale.ROOT), bits);
            return this;
        }

        /**
         * Builds an immutable CryptoPolicy from the current builder state.
         * 从当前构建器状态构建不可变的 CryptoPolicy。
         *
         * @return a new CryptoPolicy instance | 新的 CryptoPolicy 实例
         */
        public CryptoPolicy build() {
            return new CryptoPolicy(
                    Collections.unmodifiableSet(new LinkedHashSet<>(allowed)),
                    Collections.unmodifiableSet(new LinkedHashSet<>(denied)),
                    Collections.unmodifiableMap(new HashMap<>(minKeyBits))
            );
        }
    }
}
