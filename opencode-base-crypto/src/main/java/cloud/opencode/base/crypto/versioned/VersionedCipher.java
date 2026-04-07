package cloud.opencode.base.crypto.versioned;

import cloud.opencode.base.crypto.exception.OpenCryptoException;
import cloud.opencode.base.crypto.symmetric.AeadCipher;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Versioned cipher supporting multiple algorithm versions for seamless key/algorithm rotation.
 * 支持多个算法版本的版本化加密器，用于无缝密钥/算法轮换。
 *
 * <p>Encrypts with the current version and decrypts with any registered version,
 * enabling zero-downtime algorithm migration.</p>
 * <p>使用当前版本加密，使用任何已注册版本解密，实现零停机算法迁移。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Multi-version cipher management - 多版本加密管理</li>
 *   <li>Transparent version detection on decrypt - 解密时透明的版本检测</li>
 *   <li>Base64 convenience methods - Base64 便捷方法</li>
 *   <li>Builder pattern construction - Builder 模式构建</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * VersionedCipher vc = VersionedCipher.builder()
 *     .addVersion(1, cipherV1)
 *     .addVersion(2, cipherV2)
 *     .currentVersion(2)
 *     .build();
 *
 * byte[] encrypted = vc.encrypt(plaintext);
 * byte[] decrypted = vc.decrypt(encrypted); // auto-detects version
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable map) - 线程安全: 是（不可变 map）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.3
 */
public final class VersionedCipher {

    /**
     * Internal record holding cipher and its algorithm name.
     * 内部记录，持有加密器及其算法名称。
     */
    private record CipherEntry(String algorithm, AeadCipher cipher) {
    }

    private final Map<Integer, CipherEntry> versions;
    private final int currentVersion;

    private VersionedCipher(Map<Integer, CipherEntry> versions, int currentVersion) {
        this.versions = versions;
        this.currentVersion = currentVersion;
    }

    /**
     * Creates a new Builder for constructing a VersionedCipher.
     * 创建用于构建 VersionedCipher 的 Builder。
     *
     * @return a new Builder instance | 新的 Builder 实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Encrypts plaintext using the current version cipher.
     * 使用当前版本加密器加密明文。
     *
     * @param plaintext the plaintext bytes to encrypt | 要加密的明文字节
     * @return serialized VersionedPayload containing version, algorithm, and ciphertext | 序列化的 VersionedPayload
     */
    public byte[] encrypt(byte[] plaintext) {
        Objects.requireNonNull(plaintext, "plaintext must not be null");
        CipherEntry entry = versions.get(currentVersion);
        if (entry == null) {
            throw new OpenCryptoException("VersionedCipher", "encrypt",
                    "No cipher registered for current version " + currentVersion);
        }
        byte[] ciphertext = entry.cipher().encrypt(plaintext);
        VersionedPayload payload = new VersionedPayload(currentVersion, entry.algorithm(), ciphertext);
        return payload.serialize();
    }

    /**
     * Decrypts a serialized VersionedPayload, auto-detecting the version.
     * 解密序列化的 VersionedPayload，自动检测版本。
     *
     * @param payload the serialized payload | 序列化的负载
     * @return decrypted plaintext bytes | 解密的明文字节
     * @throws OpenCryptoException if the version is not registered | 当版本未注册时抛出
     */
    public byte[] decrypt(byte[] payload) {
        Objects.requireNonNull(payload, "payload must not be null");
        VersionedPayload vp = VersionedPayload.deserialize(payload);
        CipherEntry entry = versions.get(vp.version());
        if (entry == null) {
            throw new OpenCryptoException("VersionedCipher", "decrypt",
                    "Unknown cipher version: " + vp.version());
        }
        return entry.cipher().decrypt(vp.ciphertext());
    }

    /**
     * Encrypts plaintext and returns the result as a Base64 string.
     * 加密明文并返回 Base64 编码的结果。
     *
     * @param plaintext the plaintext bytes to encrypt | 要加密的明文字节
     * @return Base64 encoded encrypted payload | Base64 编码的加密负载
     */
    public String encryptBase64(byte[] plaintext) {
        return Base64.getEncoder().encodeToString(encrypt(plaintext));
    }

    /**
     * Decrypts a Base64 encoded payload.
     * 解密 Base64 编码的负载。
     *
     * @param base64 Base64 encoded payload | Base64 编码的负载
     * @return decrypted plaintext bytes | 解密的明文字节
     */
    public byte[] decryptBase64(String base64) {
        Objects.requireNonNull(base64, "base64 must not be null");
        return decrypt(Base64.getDecoder().decode(base64));
    }

    /**
     * Encrypts a plaintext string and returns the result as a Base64 string.
     * 加密明文字符串并返回 Base64 编码的结果。
     *
     * @param plaintext the plaintext string | 明文字符串
     * @return Base64 encoded encrypted payload | Base64 编码的加密负载
     */
    public String encryptBase64(String plaintext) {
        Objects.requireNonNull(plaintext, "plaintext must not be null");
        return encryptBase64(plaintext.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decrypts a Base64 encoded payload and returns the result as a string.
     * 解密 Base64 编码的负载并返回字符串结果。
     *
     * @param base64 Base64 encoded payload | Base64 编码的负载
     * @return decrypted plaintext string | 解密的明文字符串
     */
    public String decryptBase64ToString(String base64) {
        return new String(decryptBase64(base64), StandardCharsets.UTF_8);
    }

    /**
     * Builder for constructing {@link VersionedCipher} instances.
     * 用于构建 {@link VersionedCipher} 实例的构建器。
     */
    public static final class Builder {

        private final Map<Integer, CipherEntry> versions = new LinkedHashMap<>();
        private int currentVersion = -1;
        private boolean currentVersionSet = false;

        private Builder() {
        }

        /**
         * Registers a cipher for the given version number.
         * 为给定版本号注册加密器。
         *
         * @param version the version number (0-255) | 版本号（0-255）
         * @param cipher the AEAD cipher for this version | 此版本的 AEAD 加密器
         * @return this builder | 当前构建器
         * @throws IllegalArgumentException if the version is already registered or out of range | 当版本已注册或超出范围时抛出
         */
        public Builder addVersion(int version, AeadCipher cipher) {
            if (version < 0 || version > 255) {
                throw new IllegalArgumentException(
                        "Version must be in range [0, 255], got: " + version);
            }
            Objects.requireNonNull(cipher, "cipher must not be null");
            if (versions.containsKey(version)) {
                throw new IllegalArgumentException("Version " + version + " is already registered");
            }
            versions.put(version, new CipherEntry(cipher.getAlgorithm(), cipher));
            return this;
        }

        /**
         * Sets the current (active) version used for encryption.
         * 设置用于加密的当前（活动）版本。
         *
         * @param version the version to use for encryption (must be already registered) | 用于加密的版本（必须已注册）
         * @return this builder | 当前构建器
         * @throws IllegalArgumentException if the version has not been registered | 当版本未注册时抛出
         */
        public Builder currentVersion(int version) {
            if (!versions.containsKey(version)) {
                throw new IllegalArgumentException(
                        "Version " + version + " must be registered via addVersion() before setting as current");
            }
            this.currentVersion = version;
            this.currentVersionSet = true;
            return this;
        }

        /**
         * Builds an immutable VersionedCipher from the current builder state.
         * 从当前构建器状态构建不可变的 VersionedCipher。
         *
         * @return a new VersionedCipher instance | 新的 VersionedCipher 实例
         * @throws IllegalStateException if no versions registered or currentVersion not set | 当未注册版本或未设置当前版本时抛出
         */
        public VersionedCipher build() {
            if (versions.isEmpty()) {
                throw new IllegalStateException("At least one version must be registered");
            }
            if (!currentVersionSet) {
                throw new IllegalStateException("currentVersion must be set before build()");
            }
            return new VersionedCipher(
                    Collections.unmodifiableMap(new LinkedHashMap<>(versions)),
                    currentVersion
            );
        }
    }
}
