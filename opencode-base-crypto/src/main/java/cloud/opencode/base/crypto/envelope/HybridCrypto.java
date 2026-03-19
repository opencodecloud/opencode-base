package cloud.opencode.base.crypto.envelope;

import cloud.opencode.base.core.OpenBase64;
import cloud.opencode.base.crypto.enums.AsymmetricAlgorithm;
import cloud.opencode.base.crypto.enums.SymmetricAlgorithm;
import cloud.opencode.base.crypto.exception.OpenCryptoException;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Objects;

/**
 * Hybrid encryption implementation - Simplified envelope encryption with transparent format
 * 混合加密实现 - 简化的信封加密，格式透明
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Hybrid encryption combining asymmetric and symmetric - 结合非对称和对称的混合加密</li>
 *   <li>Automatic key negotiation - 自动密钥协商</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * HybridCrypto hybrid = HybridCrypto.rsaAes();
 * byte[] encrypted = hybrid.encrypt(data, publicKey);
 * byte[] decrypted = hybrid.decrypt(encrypted, privateKey);
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
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
public final class HybridCrypto {

    private final EnvelopeCrypto envelopeCrypto;

    /**
     * Private constructor
     *
     * @param envelopeCrypto underlying envelope crypto implementation
     */
    private HybridCrypto(EnvelopeCrypto envelopeCrypto) {
        this.envelopeCrypto = envelopeCrypto;
    }

    /**
     * Create hybrid crypto with RSA and AES (Recommended)
     * 创建使用 RSA 和 AES 的混合加密（推荐）
     *
     * @return new HybridCrypto instance
     */
    public static HybridCrypto rsaAes() {
        return new HybridCrypto(EnvelopeCrypto.rsaAesGcm());
    }

    /**
     * Create hybrid crypto with ECDH and AES
     * 创建使用 ECDH 和 AES 的混合加密
     *
     * @return new HybridCrypto instance
     */
    public static HybridCrypto ecdhAes() {
        return new HybridCrypto(EnvelopeCrypto.ecdhAesGcm());
    }

    /**
     * Create hybrid crypto with X25519 and ChaCha20
     * 创建使用 X25519 和 ChaCha20 的混合加密
     *
     * @return new HybridCrypto instance
     */
    public static HybridCrypto x25519ChaCha20() {
        return new HybridCrypto(EnvelopeCrypto.x25519ChaCha20());
    }

    /**
     * Create a new builder for custom configuration
     * 创建用于自定义配置的构建器
     *
     * @return new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for creating custom HybridCrypto instances
     * 用于创建自定义 HybridCrypto 实例的构建器
     */
    public static final class Builder {
        private AsymmetricAlgorithm asymmetricAlgorithm = AsymmetricAlgorithm.RSA_OAEP_SHA256;
        private SymmetricAlgorithm symmetricAlgorithm = SymmetricAlgorithm.AES_GCM_256;

        private Builder() {
        }

        /**
         * Set asymmetric algorithm for key encryption
         * 设置用于密钥加密的非对称算法
         *
         * @param algorithm asymmetric algorithm
         * @return this builder for method chaining
         * @throws NullPointerException if algorithm is null
         */
        public Builder asymmetricAlgorithm(AsymmetricAlgorithm algorithm) {
            this.asymmetricAlgorithm = Objects.requireNonNull(algorithm, "Asymmetric algorithm cannot be null");
            return this;
        }

        /**
         * Set symmetric algorithm for data encryption
         * 设置用于数据加密的对称算法
         *
         * @param algorithm symmetric algorithm
         * @return this builder for method chaining
         * @throws NullPointerException if algorithm is null
         */
        public Builder symmetricAlgorithm(SymmetricAlgorithm algorithm) {
            this.symmetricAlgorithm = Objects.requireNonNull(algorithm, "Symmetric algorithm cannot be null");
            return this;
        }

        /**
         * Build HybridCrypto instance
         * 构建 HybridCrypto 实例
         *
         * @return new HybridCrypto instance
         */
        public HybridCrypto build() {
            EnvelopeCrypto envelope = new EnvelopeCrypto(asymmetricAlgorithm, symmetricAlgorithm);
            return new HybridCrypto(envelope);
        }
    }

    /**
     * Set recipient public key for encryption
     * 设置接收者公钥用于加密
     *
     * @param publicKey recipient's public key
     * @return this instance for method chaining
     * @throws NullPointerException if publicKey is null
     */
    public HybridCrypto setRecipientPublicKey(PublicKey publicKey) {
        envelopeCrypto.setRecipientPublicKey(publicKey);
        return this;
    }

    /**
     * Set recipient private key for decryption
     * 设置接收者私钥用于解密
     *
     * @param privateKey recipient's private key
     * @return this instance for method chaining
     * @throws NullPointerException if privateKey is null
     */
    public HybridCrypto setRecipientPrivateKey(PrivateKey privateKey) {
        envelopeCrypto.setRecipientPrivateKey(privateKey);
        return this;
    }

    /**
     * Encrypt plaintext using hybrid encryption
     * 使用混合加密加密明文
     * <p>
     * The result is serialized in a compact binary format
     *
     * @param plaintext data to encrypt
     * @return encrypted bytes (serialized envelope)
     * @throws NullPointerException     if plaintext is null
     * @throws IllegalStateException    if public key is not set
     * @throws OpenCryptoException      if encryption fails
     */
    public byte[] encrypt(byte[] plaintext) {
        Objects.requireNonNull(plaintext, "Plaintext cannot be null");
        EncryptedEnvelope envelope = envelopeCrypto.encrypt(plaintext);
        return envelope.toBytes();
    }

    /**
     * Encrypt plaintext and return Base64 encoded result
     * 加密明文并返回 Base64 编码结果
     *
     * @param plaintext data to encrypt
     * @return Base64 encoded encrypted data
     * @throws NullPointerException     if plaintext is null
     * @throws IllegalStateException    if public key is not set
     * @throws OpenCryptoException      if encryption fails
     */
    public String encryptBase64(byte[] plaintext) {
        return OpenBase64.encode(encrypt(plaintext));
    }

    /**
     * Decrypt ciphertext using hybrid encryption
     * 使用混合加密解密密文
     *
     * @param ciphertext encrypted bytes (serialized envelope)
     * @return decrypted plaintext
     * @throws NullPointerException     if ciphertext is null
     * @throws IllegalStateException    if private key is not set
     * @throws OpenCryptoException      if decryption fails
     */
    public byte[] decrypt(byte[] ciphertext) {
        Objects.requireNonNull(ciphertext, "Ciphertext cannot be null");
        EncryptedEnvelope envelope = EncryptedEnvelope.fromBytes(ciphertext);
        return envelopeCrypto.decrypt(envelope);
    }

    /**
     * Decrypt Base64 encoded ciphertext
     * 解密 Base64 编码的密文
     *
     * @param base64Ciphertext Base64 encoded encrypted data
     * @return decrypted plaintext
     * @throws NullPointerException     if base64Ciphertext is null
     * @throws IllegalStateException    if private key is not set
     * @throws OpenCryptoException      if decryption fails
     */
    public byte[] decryptBase64(String base64Ciphertext) {
        Objects.requireNonNull(base64Ciphertext, "Base64 ciphertext cannot be null");
        byte[] ciphertext = OpenBase64.decode(base64Ciphertext);
        return decrypt(ciphertext);
    }

    /**
     * Get the asymmetric algorithm
     * 获取非对称算法
     *
     * @return asymmetric algorithm
     */
    public AsymmetricAlgorithm getAsymmetricAlgorithm() {
        return envelopeCrypto.getAsymmetricAlgorithm();
    }

    /**
     * Get the symmetric algorithm
     * 获取对称算法
     *
     * @return symmetric algorithm
     */
    public SymmetricAlgorithm getSymmetricAlgorithm() {
        return envelopeCrypto.getSymmetricAlgorithm();
    }
}
