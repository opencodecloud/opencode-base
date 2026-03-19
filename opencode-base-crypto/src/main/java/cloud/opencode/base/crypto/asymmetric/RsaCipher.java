package cloud.opencode.base.crypto.asymmetric;

import cloud.opencode.base.core.OpenBase64;
import cloud.opencode.base.crypto.codec.HexCodec;
import cloud.opencode.base.crypto.codec.PemCodec;
import cloud.opencode.base.crypto.exception.OpenCryptoException;
import cloud.opencode.base.crypto.exception.OpenKeyException;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * RSA cipher implementation with PKCS1 padding - Legacy RSA encryption
 * RSA 密码实现（PKCS1 填充）- 传统 RSA 加密
 * <p>
 * Note: RSA with PKCS1 padding is considered legacy. For new applications,
 * use {@link RsaOaepCipher} which provides better security.
 * 注意：使用 PKCS1 填充的 RSA 被认为是传统方式。对于新应用，
 * 建议使用 {@link RsaOaepCipher}，它提供更好的安全性。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>RSA-PKCS1 encryption/decryption - RSA-PKCS1 加密/解密</li>
 *   <li>Key pair generation - 密钥对生成</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * RsaCipher rsa = RsaCipher.create();
 * rsa.setPublicKey(publicKey);
 * byte[] encrypted = rsa.encrypt(data);
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
public final class RsaCipher implements AsymmetricCipher {

    private static final String ALGORITHM = "RSA";
    private static final String TRANSFORMATION = "RSA/ECB/PKCS1Padding";
    private static final int DEFAULT_KEY_SIZE = 2048;
    private static final int PKCS1_PADDING_OVERHEAD = 11;

    private PublicKey publicKey;
    private PrivateKey privateKey;
    private int keySize;

    /**
     * Private constructor
     */
    private RsaCipher() {
        this.keySize = DEFAULT_KEY_SIZE;
    }

    /**
     * Create a new RSA cipher instance
     * 创建新的 RSA 密码实例
     *
     * @return new RSA cipher instance
     */
    public static RsaCipher create() {
        return new RsaCipher();
    }

    /**
     * Create RSA cipher with generated 2048-bit key pair
     * 创建带 2048 位密钥对的 RSA 密码
     *
     * @return RSA cipher with generated keys
     */
    public static RsaCipher rsa2048() {
        return withGeneratedKeyPair(2048);
    }

    /**
     * Create RSA cipher with generated 4096-bit key pair
     * 创建带 4096 位密钥对的 RSA 密码
     *
     * @return RSA cipher with generated keys
     */
    public static RsaCipher rsa4096() {
        return withGeneratedKeyPair(4096);
    }

    /**
     * Create RSA cipher with generated key pair of specified size
     * 创建带指定大小密钥对的 RSA 密码
     *
     * @param keySize the key size in bits (minimum 2048 recommended)
     * @return RSA cipher with generated keys
     * @throws IllegalArgumentException if keySize is invalid
     */
    public static RsaCipher withGeneratedKeyPair(int keySize) {
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

            RsaCipher cipher = new RsaCipher();
            cipher.publicKey = keyPair.getPublic();
            cipher.privateKey = keyPair.getPrivate();
            cipher.keySize = keySize;
            return cipher;
        } catch (NoSuchAlgorithmException e) {
            throw OpenCryptoException.algorithmNotAvailable(ALGORITHM);
        }
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
            throw OpenCryptoException.dataTooLong(TRANSFORMATION, maxSize);
        }

        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(plaintext);
        } catch (Exception e) {
            throw OpenCryptoException.encryptionFailed(TRANSFORMATION, e);
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
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher.doFinal(ciphertext);
        } catch (Exception e) {
            throw OpenCryptoException.decryptionFailed(TRANSFORMATION, e);
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
        return TRANSFORMATION;
    }

    @Override
    public int getMaxEncryptSize() {
        // For RSA with PKCS1 padding: max data size = (key size in bytes) - 11
        if (publicKey != null) {
            // Get actual key size from the key if available
            if (publicKey instanceof java.security.interfaces.RSAPublicKey rsaKey) {
                return rsaKey.getModulus().bitLength() / 8 - PKCS1_PADDING_OVERHEAD;
            }
        }
        // Fallback to configured key size
        return (keySize / 8) - PKCS1_PADDING_OVERHEAD;
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
}
