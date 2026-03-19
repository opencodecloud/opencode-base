/**
 * OpenCode Base Crypto Module
 * OpenCode 基础加密模块
 *
 * <p>Provides modern cryptographic utilities based on JDK 25 and BouncyCastle,
 * including symmetric/asymmetric encryption, hashing, digital signatures, JWT,
 * key management, and PGP support.</p>
 * <p>提供基于 JDK 25 和 BouncyCastle 的现代密码学工具，包括对称/非对称加密、
 * 哈希、数字签名、JWT、密钥管理和 PGP 支持。</p>
 *
 * <p><strong>Key Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Symmetric Encryption (AES-GCM, ChaCha20) - 对称加密</li>
 *   <li>Asymmetric Encryption (RSA, EC) - 非对称加密</li>
 *   <li>Message Digest &amp; MAC - 消息摘要与消息认证码</li>
 *   <li>Digital Signatures - 数字签名</li>
 *   <li>JWT (JSON Web Token) - JWT 令牌</li>
 *   <li>Key Derivation (PBKDF2, Argon2, scrypt) - 密钥派生</li>
 *   <li>Key Exchange (ECDH, X25519) - 密钥交换</li>
 *   <li>Password Hashing (BCrypt, Argon2) - 密码哈希</li>
 *   <li>PGP Encryption - PGP 加密</li>
 *   <li>Envelope Encryption - 信封加密</li>
 *   <li>Key Rotation - 密钥轮换</li>
 *   <li>Sealed Box (X25519+XSalsa20) - 密封盒</li>
 * </ul>
 *
 * @author Leon Soo
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
module cloud.opencode.base.crypto {
    // Required modules
    requires transitive cloud.opencode.base.core;

    // Optional: BouncyCastle provider (bcprov-jdk18on 1.79+)
    requires static org.bouncycastle.provider;
    // Optional: BouncyCastle PKIX/CMS (bcpkix-jdk18on 1.79+)
    requires static org.bouncycastle.pkix;
    // Optional: BouncyCastle OpenPGP (bcpg-jdk18on 1.79+)
    requires static org.bouncycastle.pg;

    // Export public API packages
    exports cloud.opencode.base.crypto;
    exports cloud.opencode.base.crypto.asymmetric;
    exports cloud.opencode.base.crypto.codec;
    exports cloud.opencode.base.crypto.enums;
    exports cloud.opencode.base.crypto.envelope;
    exports cloud.opencode.base.crypto.exception;
    exports cloud.opencode.base.crypto.hash;
    exports cloud.opencode.base.crypto.jwt;
    exports cloud.opencode.base.crypto.kdf;
    exports cloud.opencode.base.crypto.key;
    exports cloud.opencode.base.crypto.keyexchange;
    exports cloud.opencode.base.crypto.mac;
    exports cloud.opencode.base.crypto.password;
    exports cloud.opencode.base.crypto.pgp;
    exports cloud.opencode.base.crypto.random;
    exports cloud.opencode.base.crypto.rotation;
    exports cloud.opencode.base.crypto.sealedbox;
    exports cloud.opencode.base.crypto.signature;
    exports cloud.opencode.base.crypto.symmetric;
    exports cloud.opencode.base.crypto.util;
    exports cloud.opencode.base.crypto.ssl;
}
