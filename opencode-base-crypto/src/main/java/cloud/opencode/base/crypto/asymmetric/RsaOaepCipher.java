package cloud.opencode.base.crypto.asymmetric;

import cloud.opencode.base.core.OpenBase64;
import cloud.opencode.base.crypto.codec.HexCodec;
import cloud.opencode.base.crypto.codec.PemCodec;
import cloud.opencode.base.crypto.enums.DigestAlgorithm;
import cloud.opencode.base.crypto.exception.OpenCryptoException;
import cloud.opencode.base.crypto.exception.OpenKeyException;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * RSA cipher implementation with OAEP padding - Recommended RSA encryption
 * RSA 密码实现（OAEP 填充）- 推荐的 RSA 加密方式
 * <p>
 * OAEP (Optimal Asymmetric Encryption Padding) provides better security than PKCS1.
 * This implementation is recommended for all new applications.
 * OAEP（最优非对称加密填充）比 PKCS1 提供更好的安全性。
 * 建议所有新应用使用此实现。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>RSA-OAEP with SHA-256/384/512 - RSA-OAEP（SHA-256/384/512）</li>
 *   <li>Recommended for new applications - 推荐用于新应用</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * RsaOaepCipher oaep = RsaOaepCipher.sha256();
 * oaep.setPublicKey(publicKey);
 * byte[] encrypted = oaep.encrypt(data);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(k^2) for encrypt/decrypt (k=key bits) - 时间复杂度: O(k^2)，k为密钥位数</li>
 *   <li>Space complexity: O(k) - 空间复杂度: O(k)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
public final class RsaOaepCipher implements AsymmetricCipher {

    private static final String ALGORITHM = "RSA";
    private static final String BASE_TRANSFORMATION = "RSA/ECB/OAEPPadding";
    private static final int DEFAULT_KEY_SIZE = 2048;

    private PublicKey publicKey;
    private PrivateKey privateKey;
    private int keySize;
    private final DigestAlgorithm digest;
    private final String mgf;
    private final byte[] label;

    /**
     * Private constructor
     */
    private RsaOaepCipher(DigestAlgorithm digest, String mgf, byte[] label) {
        this.keySize = DEFAULT_KEY_SIZE;
        this.digest = digest;
        this.mgf = mgf;
        this.label = label;
    }

    /**
     * Create RSA-OAEP cipher with SHA-256 digest (recommended)
     * 创建使用 SHA-256 摘要的 RSA-OAEP 密码（推荐）
     *
     * @return new RSA-OAEP cipher with SHA-256
     */
    public static RsaOaepCipher sha256() {
        return new RsaOaepCipher(DigestAlgorithm.SHA256, "MGF1", null);
    }

    /**
     * Create RSA-OAEP cipher with SHA-384 digest
     * 创建使用 SHA-384 摘要的 RSA-OAEP 密码
     *
     * @return new RSA-OAEP cipher with SHA-384
     */
    public static RsaOaepCipher sha384() {
        return new RsaOaepCipher(DigestAlgorithm.SHA384, "MGF1", null);
    }

    /**
     * Create RSA-OAEP cipher with SHA-512 digest
     * 创建使用 SHA-512 摘要的 RSA-OAEP 密码
     *
     * @return new RSA-OAEP cipher with SHA-512
     */
    public static RsaOaepCipher sha512() {
        return new RsaOaepCipher(DigestAlgorithm.SHA512, "MGF1", null);
    }

    /**
     * Create RSA-OAEP cipher with generated key pair
     * 创建带生成密钥对的 RSA-OAEP 密码
     *
     * @param keySize the key size in bits (minimum 2048 recommended)
     * @return RSA-OAEP cipher with generated keys
     * @throws IllegalArgumentException if keySize is invalid
     */
    public static RsaOaepCipher withGeneratedKeyPair(int keySize) {
        if (keySize < 2048) {
            throw new IllegalArgumentException("RSA key size should be at least 2048 bits");
        }
        if (keySize % 1024 != 0) {
            throw new IllegalArgumentException("RSA key size must be a multiple of 1024");
        }

        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGORITHM);
            generator.initialize(keySize);
            KeyPair keyPair = generator.generateKeyPair();

            RsaOaepCipher cipher = sha256();
            cipher.publicKey = keyPair.getPublic();
            cipher.privateKey = keyPair.getPrivate();
            cipher.keySize = keySize;
            return cipher;
        } catch (NoSuchAlgorithmException e) {
            throw OpenCryptoException.algorithmNotAvailable(ALGORITHM);
        }
    }

    /**
     * Create a builder for customizing RSA-OAEP parameters
     * 创建用于自定义 RSA-OAEP 参数的构建器
     *
     * @return new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public AsymmetricCipher setPublicKey(PublicKey publicKey) {
        if (publicKey == null) {
            throw new NullPointerException("Public key cannot be null");
        }
        if (!ALGORITHM.equals(publicKey.getAlgorithm())) {
            throw new IllegalArgumentException("Key must be RSA key");
        }
        this.publicKey = publicKey;
        return this;
    }

    @Override
    public AsymmetricCipher setPublicKey(byte[] encodedKey) {
        if (encodedKey == null) {
            throw new NullPointerException("Encoded key cannot be null");
        }
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encodedKey);
            this.publicKey = keyFactory.generatePublic(keySpec);
            return this;
        } catch (Exception e) {
            throw new OpenKeyException("Failed to decode public key", e);
        }
    }

    @Override
    public AsymmetricCipher setPublicKeyPem(String pem) {
        if (pem == null) {
            throw new NullPointerException("PEM string cannot be null");
        }
        try {
            byte[] encoded = PemCodec.decodePublicKey(pem);
            return setPublicKey(encoded);
        } catch (Exception e) {
            throw new OpenKeyException("Failed to parse PEM public key", e);
        }
    }

    @Override
    public AsymmetricCipher setPrivateKey(PrivateKey privateKey) {
        if (privateKey == null) {
            throw new NullPointerException("Private key cannot be null");
        }
        if (!ALGORITHM.equals(privateKey.getAlgorithm())) {
            throw new IllegalArgumentException("Key must be RSA key");
        }
        this.privateKey = privateKey;
        return this;
    }

    @Override
    public AsymmetricCipher setPrivateKey(byte[] encodedKey) {
        if (encodedKey == null) {
            throw new NullPointerException("Encoded key cannot be null");
        }
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encodedKey);
            this.privateKey = keyFactory.generatePrivate(keySpec);
            return this;
        } catch (Exception e) {
            throw new OpenKeyException("Failed to decode private key", e);
        }
    }

    @Override
    public AsymmetricCipher setPrivateKeyPem(String pem) {
        if (pem == null) {
            throw new NullPointerException("PEM string cannot be null");
        }
        try {
            byte[] encoded = PemCodec.decodePrivateKey(pem);
            return setPrivateKey(encoded);
        } catch (Exception e) {
            throw new OpenKeyException("Failed to parse PEM private key", e);
        }
    }

    @Override
    public AsymmetricCipher setKeyPair(KeyPair keyPair) {
        if (keyPair == null) {
            throw new NullPointerException("KeyPair cannot be null");
        }
        setPublicKey(keyPair.getPublic());
        setPrivateKey(keyPair.getPrivate());
        return this;
    }

    @Override
    public byte[] encrypt(byte[] plaintext) {
        if (plaintext == null) {
            throw new NullPointerException("Plaintext cannot be null");
        }
        if (publicKey == null) {
            throw new IllegalStateException("Public key not set");
        }

        int maxSize = getMaxEncryptSize();
        if (maxSize > 0 && plaintext.length > maxSize) {
            throw OpenCryptoException.dataTooLong(getAlgorithm(), maxSize);
        }

        try {
            Cipher cipher = Cipher.getInstance(BASE_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey, createOaepParams());
            return cipher.doFinal(plaintext);
        } catch (Exception e) {
            throw OpenCryptoException.encryptionFailed(getAlgorithm(), e);
        }
    }

    @Override
    public byte[] encrypt(String plaintext) {
        if (plaintext == null) {
            throw new NullPointerException("Plaintext cannot be null");
        }
        return encrypt(plaintext.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String encryptBase64(byte[] plaintext) {
        return OpenBase64.encode(encrypt(plaintext));
    }

    @Override
    public String encryptHex(byte[] plaintext) {
        return HexCodec.encode(encrypt(plaintext));
    }

    @Override
    public byte[] decrypt(byte[] ciphertext) {
        if (ciphertext == null) {
            throw new NullPointerException("Ciphertext cannot be null");
        }
        if (privateKey == null) {
            throw new IllegalStateException("Private key not set");
        }

        try {
            Cipher cipher = Cipher.getInstance(BASE_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, privateKey, createOaepParams());
            return cipher.doFinal(ciphertext);
        } catch (Exception e) {
            throw OpenCryptoException.decryptionFailed(getAlgorithm(), e);
        }
    }

    @Override
    public String decryptToString(byte[] ciphertext) {
        return new String(decrypt(ciphertext), StandardCharsets.UTF_8);
    }

    @Override
    public byte[] decryptBase64(String base64Ciphertext) {
        if (base64Ciphertext == null) {
            throw new NullPointerException("Base64 ciphertext cannot be null");
        }
        byte[] ciphertext = OpenBase64.decode(base64Ciphertext);
        return decrypt(ciphertext);
    }

    @Override
    public byte[] decryptHex(String hexCiphertext) {
        if (hexCiphertext == null) {
            throw new NullPointerException("Hex ciphertext cannot be null");
        }
        byte[] ciphertext = HexCodec.decode(hexCiphertext);
        return decrypt(ciphertext);
    }

    @Override
    public String getAlgorithm() {
        return String.format("RSA/ECB/OAEPWith%sAndMGF1Padding", digest.getAlgorithmName());
    }

    @Override
    public int getMaxEncryptSize() {
        // For RSA with OAEP: max data size = (key size in bytes) - 2 * (hash length in bytes) - 2
        int hashLength = digest.getDigestLength() / 8;

        if (publicKey != null) {
            if (publicKey instanceof java.security.interfaces.RSAPublicKey rsaKey) {
                return rsaKey.getModulus().bitLength() / 8 - 2 * hashLength - 2;
            }
        }
        // Fallback to configured key size
        return (keySize / 8) - 2 * hashLength - 2;
    }

    @Override
    public KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGORITHM);
            generator.initialize(keySize);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw OpenCryptoException.algorithmNotAvailable(ALGORITHM);
        }
    }

    /**
     * Create OAEP parameter specification
     * 创建 OAEP 参数规范
     *
     * @return OAEP parameter specification
     */
    private OAEPParameterSpec createOaepParams() {
        MGF1ParameterSpec mgfSpec = new MGF1ParameterSpec(digest.getAlgorithmName());
        PSource.PSpecified pSource = label != null ?
            new PSource.PSpecified(label) : PSource.PSpecified.DEFAULT;

        return new OAEPParameterSpec(
            digest.getAlgorithmName(),
            mgf,
            mgfSpec,
            pSource
        );
    }

    /**
     * Get the public key
     * 获取公钥
     *
     * @return the public key, or null if not set
     */
    public PublicKey getPublicKey() {
        return publicKey;
    }

    /**
     * Get the private key
     * 获取私钥
     *
     * @return the private key, or null if not set
     */
    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    /**
     * Builder for RSA-OAEP cipher
     * RSA-OAEP 密码构建器
     */
    public static final class Builder {
        private DigestAlgorithm digest = DigestAlgorithm.SHA256;
        private String mgf = "MGF1";
        private byte[] label = null;

        private Builder() {
        }

        /**
         * Set the digest algorithm for OAEP
         * 设置 OAEP 的摘要算法
         *
         * @param digest the digest algorithm
         * @return this builder
         */
        public Builder digest(DigestAlgorithm digest) {
            if (digest == null) {
                throw new NullPointerException("Digest algorithm cannot be null");
            }
            this.digest = digest;
            return this;
        }

        /**
         * Set the mask generation function
         * 设置掩码生成函数
         *
         * @param mgf the MGF name (typically "MGF1")
         * @return this builder
         */
        public Builder mgf(String mgf) {
            if (mgf == null) {
                throw new NullPointerException("MGF cannot be null");
            }
            this.mgf = mgf;
            return this;
        }

        /**
         * Set the label for OAEP (optional)
         * 设置 OAEP 的标签（可选）
         *
         * @param label the label bytes
         * @return this builder
         */
        public Builder label(byte[] label) {
            this.label = label;
            return this;
        }

        /**
         * Build the RSA-OAEP cipher
         * 构建 RSA-OAEP 密码
         *
         * @return configured RSA-OAEP cipher
         */
        public RsaOaepCipher build() {
            return new RsaOaepCipher(digest, mgf, label);
        }
    }
}
