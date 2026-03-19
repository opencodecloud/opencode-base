package cloud.opencode.base.crypto;

import cloud.opencode.base.crypto.enums.DigestAlgorithm;
import cloud.opencode.base.crypto.enums.AsymmetricAlgorithm;
import cloud.opencode.base.crypto.enums.SignatureAlgorithm;
import cloud.opencode.base.crypto.enums.SymmetricAlgorithm;
import cloud.opencode.base.crypto.envelope.EnvelopeCrypto;
import cloud.opencode.base.crypto.envelope.HybridCrypto;
import cloud.opencode.base.crypto.hash.*;
import cloud.opencode.base.crypto.kdf.Argon2Kdf;
import cloud.opencode.base.crypto.kdf.Hkdf;
import cloud.opencode.base.crypto.kdf.Pbkdf2;
import cloud.opencode.base.crypto.keyexchange.EcdhEngine;
import cloud.opencode.base.crypto.keyexchange.KeyExchangeEngine;
import cloud.opencode.base.crypto.keyexchange.X25519Engine;
import cloud.opencode.base.crypto.mac.HmacSha256;
import cloud.opencode.base.crypto.mac.HmacSha512;
import cloud.opencode.base.crypto.mac.Mac;
import cloud.opencode.base.crypto.password.*;
import cloud.opencode.base.crypto.symmetric.AeadCipher;
import cloud.opencode.base.crypto.symmetric.AesGcmCipher;
import cloud.opencode.base.crypto.symmetric.ChaChaCipher;
import cloud.opencode.base.crypto.symmetric.Sm4Cipher;

/**
 * Main facade class for cryptographic operations - Provides simplified access to all crypto features
 * 加密操作的主门面类 - 提供对所有加密功能的简化访问
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Unified access to all cryptographic operations - 统一访问所有加密操作</li>
 *   <li>Digest/hash computation (SHA-2, SHA-3, SM3, BLAKE) - 摘要/哈希计算（SHA-2、SHA-3、SM3、BLAKE）</li>
 *   <li>Symmetric encryption (AES-GCM, ChaCha20, SM4) - 对称加密（AES-GCM、ChaCha20、SM4）</li>
 *   <li>Asymmetric encryption (RSA-OAEP, SM2) - 非对称加密（RSA-OAEP、SM2）</li>
 *   <li>Digital signatures (Ed25519, ECDSA, RSA-PSS) - 数字签名（Ed25519、ECDSA、RSA-PSS）</li>
 *   <li>Password hashing (Argon2, BCrypt, SCrypt) - 密码哈希（Argon2、BCrypt、SCrypt）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // SHA-256 digest
 * String hash = OpenCrypto.sha256().digestHex("data");
 * 
 * // AES-GCM encryption
 * AeadCipher aes = OpenCrypto.aesGcm();
 * byte[] encrypted = aes.encrypt(data, key);
 * 
 * // Digital signature
 * OpenSign signer = OpenCrypto.ed25519().withGeneratedKeyPair();
 * byte[] sig = signer.sign("message");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Partial - 空值安全: 部分</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
public final class OpenCrypto {

    private OpenCrypto() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    // ==================== Digest/Hash ====================

    /**
     * SHA-256 digester
     * SHA-256 摘要器
     *
     * @return OpenDigest instance for SHA-256
     */
    public static OpenDigest sha256() {
        return OpenDigest.sha256();
    }

    /**
     * SHA-384 digester
     * SHA-384 摘要器
     *
     * @return OpenDigest instance for SHA-384
     */
    public static OpenDigest sha384() {
        return OpenDigest.sha384();
    }

    /**
     * SHA-512 digester
     * SHA-512 摘要器
     *
     * @return OpenDigest instance for SHA-512
     */
    public static OpenDigest sha512() {
        return OpenDigest.sha512();
    }

    /**
     * SHA3-256 digester
     * SHA3-256 摘要器
     *
     * @return OpenDigest instance for SHA3-256
     */
    public static OpenDigest sha3_256() {
        return OpenDigest.sha3_256();
    }

    /**
     * SHA3-512 digester
     * SHA3-512 摘要器
     *
     * @return OpenDigest instance for SHA3-512
     */
    public static OpenDigest sha3_512() {
        return OpenDigest.sha3_512();
    }

    /**
     * SM3 digester (requires Bouncy Castle)
     * SM3 摘要器（需要 Bouncy Castle）
     *
     * @return OpenDigest instance for SM3
     */
    public static OpenDigest sm3() {
        return OpenDigest.sm3();
    }

    /**
     * BLAKE2b digester (requires Bouncy Castle)
     * BLAKE2b 摘要器（需要 Bouncy Castle）
     *
     * @return OpenDigest instance for BLAKE2b-256
     */
    public static OpenDigest blake2b() {
        return OpenDigest.blake2b(32);
    }

    /**
     * BLAKE3 digester (requires Bouncy Castle)
     * BLAKE3 摘要器（需要 Bouncy Castle）
     *
     * @return OpenDigest instance for BLAKE3
     */
    public static OpenDigest blake3() {
        return OpenDigest.blake3();
    }

    /**
     * Custom digester by algorithm
     * 自定义摘要器
     *
     * @param algorithm digest algorithm
     * @return OpenDigest instance
     */
    public static OpenDigest digester(DigestAlgorithm algorithm) {
        return OpenDigest.of(algorithm);
    }

    // ==================== Symmetric Encryption ====================

    /**
     * AES-GCM cipher (recommended)
     * AES-GCM 加密（推荐）
     *
     * @return AES-GCM cipher instance
     */
    public static AeadCipher aesGcm() {
        return AesGcmCipher.aes128Gcm();
    }

    /**
     * AES-256-GCM cipher
     * AES-256-GCM 加密
     *
     * @return AES-256-GCM cipher instance
     */
    public static AeadCipher aesGcm256() {
        return AesGcmCipher.aes256Gcm();
    }

    /**
     * AES-CBC cipher
     * AES-CBC 加密
     *
     * @return OpenSymmetric instance for AES-CBC
     */
    public static OpenSymmetric aesCbc() {
        return OpenSymmetric.aesCbc();
    }

    /**
     * ChaCha20-Poly1305 cipher
     * ChaCha20-Poly1305 加密
     *
     * @return ChaCha20-Poly1305 cipher instance
     */
    public static AeadCipher chacha20Poly1305() {
        return ChaChaCipher.create();
    }

    /**
     * SM4-GCM cipher (requires Bouncy Castle)
     * SM4-GCM 加密（需要 Bouncy Castle）
     *
     * <p>SM4 is the Chinese national cryptographic standard (GB/T 32907-2016).
     * SM4 是中国国家密码标准（GB/T 32907-2016）。
     *
     * @return SM4-GCM cipher instance / SM4-GCM 加密实例
     */
    public static AeadCipher sm4Gcm() {
        return Sm4Cipher.gcm();
    }

    /**
     * SM4-CBC cipher (requires Bouncy Castle)
     * SM4-CBC 加密（需要 Bouncy Castle）
     *
     * @return SM4-CBC cipher instance / SM4-CBC 加密实例
     */
    public static Sm4Cipher sm4Cbc() {
        return Sm4Cipher.cbc();
    }

    /**
     * Custom symmetric cipher
     * 自定义对称加密
     *
     * @param algorithm symmetric algorithm
     * @return OpenSymmetric instance
     */
    public static OpenSymmetric symmetric(SymmetricAlgorithm algorithm) {
        return OpenSymmetric.of(algorithm);
    }

    // ==================== Asymmetric Encryption ====================

    /**
     * RSA-OAEP-SHA256 cipher (recommended)
     * RSA-OAEP-SHA256 加密（推荐）
     *
     * @return OpenAsymmetric instance for RSA-OAEP
     */
    public static OpenAsymmetric rsaOaep() {
        return OpenAsymmetric.rsaOaep();
    }

    /**
     * RSA-PKCS1 cipher
     * RSA-PKCS1 加密
     *
     * @return OpenAsymmetric instance for RSA
     */
    public static OpenAsymmetric rsa() {
        return OpenAsymmetric.rsa();
    }

    /**
     * SM2 cipher (requires Bouncy Castle)
     * SM2 加密（需要 Bouncy Castle）
     *
     * @return OpenAsymmetric instance for SM2
     */
    public static OpenAsymmetric sm2() {
        return OpenAsymmetric.sm2();
    }

    /**
     * Custom asymmetric cipher
     * 自定义非对称加密
     *
     * @param algorithm asymmetric algorithm
     * @return OpenAsymmetric instance
     */
    public static OpenAsymmetric asymmetric(AsymmetricAlgorithm algorithm) {
        return OpenAsymmetric.of(algorithm);
    }

    // ==================== Digital Signatures ====================

    /**
     * Ed25519 signer (recommended)
     * Ed25519 签名（推荐）
     *
     * @return OpenSign instance for Ed25519
     */
    public static OpenSign ed25519() {
        return OpenSign.ed25519();
    }

    /**
     * Ed448 signer
     * Ed448 签名
     *
     * @return OpenSign instance for Ed448
     */
    public static OpenSign ed448() {
        return OpenSign.ed448();
    }

    /**
     * ECDSA P-256 signer
     * ECDSA P-256 签名
     *
     * @return OpenSign instance for ECDSA P-256
     */
    public static OpenSign ecdsaP256() {
        return OpenSign.ecdsaP256();
    }

    /**
     * ECDSA P-384 signer
     * ECDSA P-384 签名
     *
     * @return OpenSign instance for ECDSA P-384
     */
    public static OpenSign ecdsaP384() {
        return OpenSign.ecdsaP384();
    }

    /**
     * RSA-SHA256 signer
     * RSA-SHA256 签名
     *
     * @return OpenSign instance for RSA-SHA256
     */
    public static OpenSign sha256WithRsa() {
        return OpenSign.sha256WithRsa();
    }

    /**
     * RSA-PSS-SHA256 signer
     * RSA-PSS-SHA256 签名
     *
     * @return OpenSign instance for RSA-PSS
     */
    public static OpenSign rsaPss() {
        return OpenSign.rsaPss();
    }

    /**
     * SM2 signer (requires Bouncy Castle)
     * SM2 签名（需要 Bouncy Castle）
     *
     * @return OpenSign instance for SM2
     */
    public static OpenSign sm2Sign() {
        return OpenSign.sm2();
    }

    /**
     * Custom signer
     * 自定义签名器
     *
     * @param algorithm signature algorithm
     * @return OpenSign instance
     */
    public static OpenSign signer(SignatureAlgorithm algorithm) {
        return OpenSign.of(algorithm);
    }

    // ==================== Password Hashing ====================

    /**
     * Argon2id password hash (recommended)
     * Argon2id 密码哈希（推荐）
     *
     * @return PasswordHash instance for Argon2id
     */
    public static PasswordHash argon2() {
        return Argon2Hash.argon2id();
    }

    /**
     * BCrypt password hash
     * BCrypt 密码哈希
     *
     * @return PasswordHash instance for BCrypt
     */
    public static PasswordHash bcrypt() {
        return BCryptHash.builder().build();
    }

    /**
     * SCrypt password hash
     * SCrypt 密码哈希
     *
     * @return PasswordHash instance for SCrypt
     */
    public static PasswordHash scrypt() {
        return SCryptHash.builder().build();
    }

    /**
     * PBKDF2 password hash
     * PBKDF2 密码哈希
     *
     * @return PasswordHash instance for PBKDF2
     */
    public static PasswordHash pbkdf2() {
        return Pbkdf2Hash.builder().build();
    }

    // ==================== HMAC ====================

    /**
     * HMAC-SHA256
     * HMAC-SHA256
     *
     * @param key secret key
     * @return Mac instance for HMAC-SHA256
     */
    public static Mac hmacSha256(byte[] key) {
        return HmacSha256.of(key);
    }

    /**
     * HMAC-SHA512
     * HMAC-SHA512
     *
     * @param key secret key
     * @return Mac instance for HMAC-SHA512
     */
    public static Mac hmacSha512(byte[] key) {
        return HmacSha512.of(key);
    }

    // ==================== Key Derivation ====================

    /**
     * HKDF-SHA256
     * HKDF-SHA256
     *
     * @return Hkdf instance
     */
    public static Hkdf hkdf() {
        return Hkdf.sha256();
    }

    /**
     * PBKDF2-SHA256 KDF
     * PBKDF2-SHA256 密钥派生
     *
     * @return Pbkdf2 instance
     */
    public static Pbkdf2 pbkdf2Kdf() {
        return Pbkdf2.owaspRecommended();
    }

    /**
     * Argon2 KDF (requires Bouncy Castle)
     * Argon2 密钥派生（需要 Bouncy Castle）
     *
     * @return Argon2Kdf instance
     */
    public static Argon2Kdf argon2Kdf() {
        return Argon2Kdf.argon2id();
    }

    // ==================== Key Exchange ====================

    /**
     * X25519 key exchange (recommended)
     * X25519 密钥协商（推荐）
     *
     * @return KeyExchangeEngine instance for X25519
     */
    public static KeyExchangeEngine x25519() {
        return X25519Engine.create();
    }

    /**
     * ECDH P-256 key exchange
     * ECDH P-256 密钥协商
     *
     * @return KeyExchangeEngine instance for ECDH P-256
     */
    public static KeyExchangeEngine ecdhP256() {
        return EcdhEngine.p256();
    }

    // ==================== Envelope Encryption ====================

    /**
     * Envelope encryption (RSA + AES-GCM)
     * 信封加密（RSA + AES-GCM）
     *
     * @return EnvelopeCrypto instance
     */
    public static EnvelopeCrypto envelope() {
        return EnvelopeCrypto.rsaAesGcm();
    }

    /**
     * Hybrid encryption (public key encrypts symmetric key)
     * 混合加密（公钥加密对称密钥）
     *
     * @return HybridCrypto instance
     */
    public static HybridCrypto hybrid() {
        return HybridCrypto.rsaAes();
    }
}
