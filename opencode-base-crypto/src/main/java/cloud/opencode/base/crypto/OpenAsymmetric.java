package cloud.opencode.base.crypto;

import cloud.opencode.base.core.OpenBase64;
import cloud.opencode.base.crypto.codec.HexCodec;
import cloud.opencode.base.crypto.enums.AsymmetricAlgorithm;
import cloud.opencode.base.crypto.exception.OpenCryptoException;
import cloud.opencode.base.crypto.asymmetric.*;

import java.nio.charset.StandardCharsets;
import java.security.*;

/**
 * Asymmetric encryption facade for encrypt/decrypt operations - Provides convenient API for various asymmetric algorithms
 * 非对称加密门面类 - 为各种非对称加密算法提供便捷的 API
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>RSA-OAEP encryption (recommended) - RSA-OAEP 加密（推荐）</li>
 *   <li>RSA-PKCS1 encryption - RSA-PKCS1 加密</li>
 *   <li>SM2 encryption (Chinese national standard) - SM2 加密（中国国密标准）</li>
 *   <li>Hex and Base64 output encoding - 十六进制和 Base64 输出编码</li>
 *   <li>Key pair generation - 密钥对生成</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * OpenAsymmetric rsa = OpenAsymmetric.rsaOaep();
 * KeyPair keyPair = rsa.generateKeyPair();
 * rsa.setKeyPair(keyPair);
 * String encrypted = rsa.encryptBase64("secret");
 * String decrypted = rsa.decryptBase64ToString(encrypted);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
public final class OpenAsymmetric {

    private final AsymmetricCipher cipher;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    private OpenAsymmetric(AsymmetricCipher cipher) {
        this.cipher = cipher;
    }

    // ==================== Static Factory Methods ====================

    /**
     * Create RSA-OAEP cipher (recommended)
     * 创建 RSA-OAEP 加密（推荐）
     *
     * @return OpenAsymmetric instance
     */
    public static OpenAsymmetric rsaOaep() {
        return new OpenAsymmetric(RsaOaepCipher.sha256());
    }

    /**
     * Create RSA-PKCS1 cipher
     * 创建 RSA-PKCS1 加密
     *
     * @return OpenAsymmetric instance
     */
    public static OpenAsymmetric rsa() {
        return new OpenAsymmetric(RsaCipher.create());
    }

    /**
     * Create ECC cipher (ECIES)
     * 创建 ECC 加密（ECIES）
     *
     * @return OpenAsymmetric instance
     */
    public static OpenAsymmetric ecc() {
        return new OpenAsymmetric(EccCipher.p256());
    }

    /**
     * Create SM2 cipher (requires Bouncy Castle)
     * 创建 SM2 加密（需要 Bouncy Castle）
     *
     * @return OpenAsymmetric instance
     */
    public static OpenAsymmetric sm2() {
        return new OpenAsymmetric(Sm2Cipher.create());
    }

    /**
     * Create cipher by algorithm enum
     * 根据算法枚举创建加密器
     *
     * @param algorithm asymmetric algorithm
     * @return OpenAsymmetric instance
     */
    public static OpenAsymmetric of(AsymmetricAlgorithm algorithm) {
        if (algorithm == null) {
            throw new NullPointerException("Algorithm cannot be null");
        }
        return switch (algorithm) {
            case RSA_PKCS1 -> rsa();
            case RSA_OAEP_SHA256, RSA_OAEP_SHA384, RSA_OAEP_SHA512 -> rsaOaep();
            case SM2 -> sm2();
        };
    }

    // ==================== Key Configuration ====================

    /**
     * Set private key for decryption
     * 设置解密私钥
     *
     * @param privateKey private key
     * @return this instance for chaining
     */
    public OpenAsymmetric setPrivateKey(PrivateKey privateKey) {
        if (privateKey == null) {
            throw new NullPointerException("Private key cannot be null");
        }
        this.privateKey = privateKey;
        cipher.setPrivateKey(privateKey);
        return this;
    }

    /**
     * Set public key for encryption
     * 设置加密公钥
     *
     * @param publicKey public key
     * @return this instance for chaining
     */
    public OpenAsymmetric setPublicKey(PublicKey publicKey) {
        if (publicKey == null) {
            throw new NullPointerException("Public key cannot be null");
        }
        this.publicKey = publicKey;
        cipher.setPublicKey(publicKey);
        return this;
    }

    /**
     * Set key pair
     * 设置密钥对
     *
     * @param keyPair key pair
     * @return this instance for chaining
     */
    public OpenAsymmetric setKeyPair(KeyPair keyPair) {
        if (keyPair == null) {
            throw new NullPointerException("Key pair cannot be null");
        }
        setPrivateKey(keyPair.getPrivate());
        setPublicKey(keyPair.getPublic());
        return this;
    }

    // ==================== Encryption ====================

    /**
     * Encrypt data with public key
     * 使用公钥加密数据
     *
     * @param plaintext plaintext bytes
     * @return ciphertext bytes
     */
    public byte[] encrypt(byte[] plaintext) {
        if (plaintext == null) {
            throw new NullPointerException("Plaintext cannot be null");
        }
        if (publicKey == null) {
            throw new OpenCryptoException(cipher.getAlgorithm(), "encrypt", "Public key not set");
        }
        return cipher.encrypt(plaintext);
    }

    /**
     * Encrypt string (UTF-8)
     * 加密字符串（UTF-8）
     *
     * @param plaintext plaintext string
     * @return ciphertext bytes
     */
    public byte[] encrypt(String plaintext) {
        if (plaintext == null) {
            throw new NullPointerException("Plaintext cannot be null");
        }
        return encrypt(plaintext.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Encrypt and return as hex string
     * 加密并返回十六进制字符串
     *
     * @param plaintext plaintext bytes
     * @return hex ciphertext
     */
    public String encryptHex(byte[] plaintext) {
        return HexCodec.encode(encrypt(plaintext));
    }

    /**
     * Encrypt string and return as hex string
     * 加密字符串并返回十六进制字符串
     *
     * @param plaintext plaintext string
     * @return hex ciphertext
     */
    public String encryptHex(String plaintext) {
        return HexCodec.encode(encrypt(plaintext));
    }

    /**
     * Encrypt and return as Base64 string
     * 加密并返回 Base64 字符串
     *
     * @param plaintext plaintext bytes
     * @return Base64 ciphertext
     */
    public String encryptBase64(byte[] plaintext) {
        return OpenBase64.encode(encrypt(plaintext));
    }

    /**
     * Encrypt string and return as Base64 string
     * 加密字符串并返回 Base64 字符串
     *
     * @param plaintext plaintext string
     * @return Base64 ciphertext
     */
    public String encryptBase64(String plaintext) {
        return OpenBase64.encode(encrypt(plaintext));
    }

    // ==================== Decryption ====================

    /**
     * Decrypt data with private key
     * 使用私钥解密数据
     *
     * @param ciphertext ciphertext bytes
     * @return plaintext bytes
     */
    public byte[] decrypt(byte[] ciphertext) {
        if (ciphertext == null) {
            throw new NullPointerException("Ciphertext cannot be null");
        }
        if (privateKey == null) {
            throw new OpenCryptoException(cipher.getAlgorithm(), "decrypt", "Private key not set");
        }
        return cipher.decrypt(ciphertext);
    }

    /**
     * Decrypt and return as string (UTF-8)
     * 解密并返回字符串（UTF-8）
     *
     * @param ciphertext ciphertext bytes
     * @return plaintext string
     */
    public String decryptToString(byte[] ciphertext) {
        return new String(decrypt(ciphertext), StandardCharsets.UTF_8);
    }

    /**
     * Decrypt hex-encoded ciphertext
     * 解密十六进制编码的密文
     *
     * @param hexCiphertext hex-encoded ciphertext
     * @return plaintext bytes
     */
    public byte[] decryptHex(String hexCiphertext) {
        if (hexCiphertext == null) {
            throw new NullPointerException("Ciphertext cannot be null");
        }
        return decrypt(HexCodec.decode(hexCiphertext));
    }

    /**
     * Decrypt hex-encoded ciphertext to string
     * 解密十六进制编码的密文为字符串
     *
     * @param hexCiphertext hex-encoded ciphertext
     * @return plaintext string
     */
    public String decryptHexToString(String hexCiphertext) {
        return new String(decryptHex(hexCiphertext), StandardCharsets.UTF_8);
    }

    /**
     * Decrypt Base64-encoded ciphertext
     * 解密 Base64 编码的密文
     *
     * @param base64Ciphertext Base64-encoded ciphertext
     * @return plaintext bytes
     */
    public byte[] decryptBase64(String base64Ciphertext) {
        if (base64Ciphertext == null) {
            throw new NullPointerException("Ciphertext cannot be null");
        }
        return decrypt(OpenBase64.decode(base64Ciphertext));
    }

    /**
     * Decrypt Base64-encoded ciphertext to string
     * 解密 Base64 编码的密文为字符串
     *
     * @param base64Ciphertext Base64-encoded ciphertext
     * @return plaintext string
     */
    public String decryptBase64ToString(String base64Ciphertext) {
        return new String(decryptBase64(base64Ciphertext), StandardCharsets.UTF_8);
    }

    // ==================== Key Generation ====================

    /**
     * Generate key pair for this algorithm
     * 生成此算法的密钥对
     *
     * @return generated key pair
     */
    public KeyPair generateKeyPair() {
        return cipher.generateKeyPair();
    }

    /**
     * Generate key pair and set it
     * 生成密钥对并设置
     *
     * @return this instance for chaining
     */
    public OpenAsymmetric withGeneratedKeyPair() {
        KeyPair keyPair = generateKeyPair();
        setKeyPair(keyPair);
        return this;
    }

    // ==================== Info Methods ====================

    /**
     * Get algorithm name
     * 获取算法名称
     *
     * @return algorithm name
     */
    public String getAlgorithm() {
        return cipher.getAlgorithm();
    }

    /**
     * Get maximum encrypt size in bytes
     * 获取最大加密大小（字节）
     *
     * @return maximum plaintext size, or -1 if no limit
     */
    public int getMaxEncryptSize() {
        return cipher.getMaxEncryptSize();
    }
}
