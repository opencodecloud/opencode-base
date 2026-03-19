# Crypto 组件方案

## 1. 组件概述

### 1.1 设计目标

Crypto 组件提供现代化的密码学基础能力，核心设计理念:

- **安全优先**: 默认选用 OWASP 推荐的安全算法，避免不安全的默认值
- **简洁流畅的 API**: 门面类屏蔽 JCE 复杂性，链式调用
- **全面的算法支持**: 对称加密、非对称加密、数字签名、哈希、密钥派生、密钥协商
- **密码哈希**: 支持 Argon2、BCrypt、SCrypt、PBKDF2 等密码哈希算法
- **安全的密钥管理**: 密钥生成、编码、解析、安全擦除、密钥轮换
- **国密算法支持**: SM2、SM3、SM4 国密算法
- **PGP 加密**: 支持 OpenPGP 标准加密解密
- **JWT 工具**: 支持 JWT 创建和验证，HMAC/RSA/ECDSA 多种算法
- **NaCl 风格加密**: SealedBox/SecretBox 简化的公钥/对称加密

### 1.2 架构概览

```
┌─────────────────────────────────────────────────────────────────┐
│                         应用层                                   │
│              (Security 组件 / 业务代码)                          │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                       门面层 (Facade)                            │
│  ┌──────────┐ ┌──────────┐ ┌────────────┐ ┌──────────────────┐  │
│  │OpenCrypto│ │OpenDigest│ │OpenSymmetric│ │  OpenAsymmetric  │  │
│  │ 总入口   │ │ 摘要计算 │ │ 对称加密   │ │   非对称加密     │  │
│  └──────────┘ └──────────┘ └────────────┘ └──────────────────┘  │
│  ┌──────────┐ ┌──────────────┐ ┌──────────┐ ┌──────────┐       │
│  │ OpenSign │ │OpenPasswordHash│ │ OpenPgp │ │ OpenJwt │       │
│  │ 签名验签 │ │  密码哈希     │ │PGP 加密 │ │JWT 工具 │       │
│  └──────────┘ └──────────────┘ └──────────┘ └──────────┘       │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                       算法层 (Algorithm)                         │
│  ┌────────────┐ ┌────────────┐ ┌──────────────────────────────┐ │
│  │ symmetric/ │ │ asymmetric/│ │       signature/             │ │
│  │AES-GCM    │ │RSA-OAEP    │ │EdDSA/ECDSA/RSA-PSS/SM2      │ │
│  │ChaCha20   │ │ECC/SM2     │ │                              │ │
│  │SM4        │ │            │ │                              │ │
│  └────────────┘ └────────────┘ └──────────────────────────────┘ │
│  ┌────────────┐ ┌────────────┐ ┌──────────────────────────────┐ │
│  │ password/  │ │   kdf/     │ │     keyexchange/             │ │
│  │Argon2     │ │HKDF/PBKDF2│ │X25519/X448/ECDH              │ │
│  │BCrypt     │ │Scrypt     │ │                              │ │
│  │SCrypt     │ │Argon2Kdf  │ │                              │ │
│  └────────────┘ └────────────┘ └──────────────────────────────┘ │
│  ┌────────────┐ ┌────────────┐ ┌──────────────────────────────┐ │
│  │ envelope/  │ │sealedbox/  │ │       rotation/              │ │
│  │EnvelopeCrypto│SecretBox  │ │KeyRotation                   │ │
│  │HybridCrypto│ │SealedBox  │ │                              │ │
│  └────────────┘ └────────────┘ └──────────────────────────────┘ │
│  ┌────────────┐ ┌────────────┐                                  │
│  │   pgp/     │ │   jwt/     │                                  │
│  │PgpCipher   │ │JwtUtil     │                                  │
│  │PgpKeyUtil  │ │JwtClaims   │                                  │
│  └────────────┘ └────────────┘                                  │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                       基础层 (Foundation)                        │
│  ┌─────────────┐ ┌───────────────┐ ┌────────────┐ ┌──────────┐ │
│  │    key/     │ │   codec/      │ │  random/   │ │exception/│ │
│  │KeyGenerator │ │HexCodec       │ │SecureRandoms││OpenCrypto│ │
│  │SecureKeyStore││PemCodec       │ │NonceGenerator││Exception│ │
│  │KeyUtil      │ │Base64UrlCodec │ │RandomBytes │ │          │ │
│  └─────────────┘ └───────────────┘ └────────────┘ └──────────┘ │
│  ┌─────────────┐ ┌───────────────┐ ┌────────────┐              │
│  │    mac/     │ │    hash/      │ │   util/    │              │
│  │HmacSha256  │ │Sha2Hash       │ │CryptoUtil  │              │
│  │HmacSha512  │ │Sha3Hash       │ │ByteUtil    │              │
│  │Poly1305    │ │Blake2/3Hash   │ │CryptoDetector│             │
│  └─────────────┘ └───────────────┘ └────────────┘              │
└─────────────────────────────────────────────────────────────────┘
```

### 1.3 算法安全建议（OWASP）

| 场景 | 推荐算法 | 最低要求 |
|------|----------|----------|
| 对称加密 | AES-256-GCM | AES-128-GCM |
| 非对称加密 | RSA-OAEP-SHA256 | RSA-2048 |
| 数字签名 | Ed25519 / ECDSA P-256 | RSA-2048-SHA256 |
| 消息摘要 | SHA-256 / SHA-3 | SHA-256 |
| 密码哈希 | Argon2id | BCrypt (cost>=10) |
| 密钥派生 | HKDF-SHA256 | PBKDF2-SHA256 (>=310000 iterations) |
| 密钥协商 | X25519 | ECDH P-256 |

### 1.4 模块依赖

```
crypto 模块依赖:
├── opencode-base-core (必需)
└── 外部依赖:
    └── org.bouncycastle (可选，SM2/SM3/SM4/Argon2/SCrypt/PGP)
```
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-crypto</artifactId>
    <version>${version}</version>
</dependency>
```

---

## 2. 包结构设计

```
cloud.opencode.base.crypto
├── OpenCrypto.java                    # 总入口门面
├── OpenDigest.java                    # 摘要计算门面
├── OpenSign.java                      # 签名验签门面
├── OpenSymmetric.java                 # 对称加密门面
├── OpenAsymmetric.java                # 非对称加密门面
├── OpenPasswordHash.java              # 密码哈希门面
├── OpenPgp.java                       # PGP 加密门面
├── OpenJwt.java                       # JWT 工具门面
├── hash/                              # 哈希算法
│   ├── HashFunction.java              # 哈希函数接口
│   ├── Sha2Hash.java                  # SHA-2 实现 (SHA-224/256/384/512)
│   ├── Sha3Hash.java                  # SHA-3 实现 (SHA3-224/256/384/512)
│   ├── Sm3Hash.java                   # SM3 实现
│   ├── Blake2Hash.java                # BLAKE2 实现 (BLAKE2b/BLAKE2s)
│   ├── Blake3Hash.java                # BLAKE3 实现
│   └── HmacFunction.java             # HMAC 实现
├── symmetric/                         # 对称加密
│   ├── SymmetricCipher.java           # 对称加密接口 (CBC/CTR)
│   ├── AeadCipher.java               # AEAD 加密接口 (GCM/Poly1305)
│   ├── AesCipher.java                # AES CBC/CTR 实现
│   ├── AesGcmCipher.java             # AES-GCM 实现
│   ├── ChaChaCipher.java             # ChaCha20-Poly1305 实现
│   ├── Sm4Cipher.java                # SM4 CBC/GCM 实现
│   ├── CipherMode.java               # 加密模式枚举
│   └── Padding.java                   # 填充模式枚举
├── asymmetric/                        # 非对称加密
│   ├── AsymmetricCipher.java          # 非对称加密接口
│   ├── RsaCipher.java                # RSA PKCS1 实现
│   ├── RsaOaepCipher.java            # RSA-OAEP 实现（推荐）
│   ├── EccCipher.java                # ECC/ECIES 实现
│   └── Sm2Cipher.java                # SM2 实现
├── signature/                         # 数字签名
│   ├── SignatureEngine.java           # 签名引擎接口
│   ├── EddsaSignature.java           # EdDSA 签名 (Ed25519/Ed448)
│   ├── EcdsaSignature.java           # ECDSA 签名 (P-256/P-384/P-521)
│   ├── RsaSignature.java             # RSA SHA 签名
│   ├── RsaPssSignature.java          # RSA-PSS 签名（推荐）
│   └── Sm2Signature.java             # SM2 签名
├── password/                          # 密码哈希
│   ├── PasswordHash.java             # 密码哈希接口
│   ├── Argon2Hash.java               # Argon2 实现（推荐）
│   ├── BCryptHash.java               # BCrypt 实现
│   ├── SCryptHash.java               # SCrypt 实现
│   ├── Pbkdf2Hash.java               # PBKDF2 实现
│   ├── PasswordPolicy.java           # 密码策略
│   └── Argon2Type.java               # Argon2 类型枚举
├── kdf/                               # 密钥派生
│   ├── KdfEngine.java                # KDF 引擎接口
│   ├── Hkdf.java                     # HKDF 实现
│   ├── Pbkdf2.java                   # PBKDF2 KDF 实现
│   ├── Scrypt.java                   # Scrypt KDF 实现
│   └── Argon2Kdf.java                # Argon2 KDF 实现
├── keyexchange/                       # 密钥协商
│   ├── KeyExchangeEngine.java        # 密钥协商接口
│   ├── X25519Engine.java             # X25519 实现（推荐）
│   ├── X448Engine.java               # X448 实现
│   └── EcdhEngine.java               # ECDH 实现
├── key/                               # 密钥工具
│   ├── KeyGenerator.java             # 密钥生成器
│   ├── KeyUtil.java                  # 密钥工具类
│   ├── KeyPairUtil.java              # 密钥对工具
│   ├── SecretKeyUtil.java            # 对称密钥工具
│   └── SecureKeyStore.java           # 安全密钥存储（PKCS12）
├── mac/                               # 消息认证码
│   ├── Mac.java                      # MAC 接口
│   ├── HmacSha256.java              # HMAC-SHA256
│   ├── HmacSha512.java              # HMAC-SHA512
│   └── Poly1305.java                # Poly1305
├── envelope/                          # 信封加密
│   ├── EnvelopeCrypto.java           # 信封加密
│   ├── HybridCrypto.java            # 混合加密
│   └── EncryptedEnvelope.java        # 加密信封数据结构
├── sealedbox/                         # NaCl 风格加密
│   ├── SealedBox.java                # 匿名公钥加密
│   └── SecretBox.java                # 简化对称加密
├── rotation/                          # 密钥轮换
│   └── KeyRotation.java             # 密钥版本管理
├── pgp/                               # PGP 加密
│   ├── PgpCipher.java               # PGP 加密器
│   ├── PgpKeyUtil.java              # PGP 密钥工具
│   ├── PgpKeyPair.java              # PGP 密钥对 (record)
│   └── PgpAlgorithm.java            # PGP 算法配置
├── jwt/                               # JWT 工具
│   ├── JwtUtil.java                  # JWT 工具类
│   ├── JwtClaims.java               # JWT 声明容器
│   └── JwtAlgorithm.java            # JWT 算法枚举
├── codec/                             # 编解码
│   ├── HexCodec.java                # 十六进制编解码
│   ├── PemCodec.java                # PEM 编解码
│   └── Base64UrlCodec.java          # Base64URL 编解码
├── random/                            # 随机数
│   ├── SecureRandoms.java           # 安全随机数工厂
│   ├── RandomBytes.java             # 随机字节生成
│   └── NonceGenerator.java          # Nonce 生成器
├── util/                              # 工具类
│   ├── CryptoUtil.java              # 安全工具
│   ├── ConstantTimeUtil.java        # 常量时间比较
│   ├── ByteUtil.java                # 字节操作工具
│   └── CryptoDetector.java          # 加密数据检测
├── enums/                             # 算法枚举
│   ├── DigestAlgorithm.java         # 摘要算法
│   ├── SymmetricAlgorithm.java      # 对称算法
│   ├── AsymmetricAlgorithm.java     # 非对称算法
│   ├── SignatureAlgorithm.java      # 签名算法
│   ├── PasswordHashAlgorithm.java   # 密码哈希算法
│   └── CurveType.java              # 椭圆曲线类型
└── exception/                         # 异常
    ├── OpenCryptoException.java     # 加密异常
    ├── OpenKeyException.java        # 密钥异常
    └── OpenSignatureException.java  # 签名异常
```

---

## 3. 门面类 API

### 3.1 OpenCrypto — 总入口门面

所有加密功能的统一入口，通过静态方法创建各类加密器。

```java
public final class OpenCrypto {

    // ==================== 摘要/哈希 ====================
    public static OpenDigest sha256();
    public static OpenDigest sha384();
    public static OpenDigest sha512();
    public static OpenDigest sha3_256();
    public static OpenDigest sha3_512();
    public static OpenDigest sm3();
    public static OpenDigest blake2b();
    public static OpenDigest blake3();
    public static OpenDigest digester(DigestAlgorithm algorithm);

    // ==================== 对称加密 ====================
    public static AeadCipher aesGcm();                // AES-GCM（推荐）
    public static AeadCipher aesGcm256();             // AES-256-GCM
    public static OpenSymmetric aesCbc();              // AES-CBC
    public static AeadCipher chacha20Poly1305();       // ChaCha20-Poly1305
    public static AeadCipher sm4Gcm();                 // SM4-GCM
    public static Sm4Cipher sm4Cbc();                  // SM4-CBC
    public static OpenSymmetric symmetric(SymmetricAlgorithm algorithm);

    // ==================== 非对称加密 ====================
    public static OpenAsymmetric rsaOaep();            // RSA-OAEP（推荐）
    public static OpenAsymmetric rsa();                // RSA-PKCS1
    public static OpenAsymmetric sm2();                // SM2
    public static OpenAsymmetric asymmetric(AsymmetricAlgorithm algorithm);

    // ==================== 数字签名 ====================
    public static OpenSign ed25519();                  // Ed25519（推荐）
    public static OpenSign ed448();                    // Ed448
    public static OpenSign ecdsaP256();                // ECDSA P-256
    public static OpenSign ecdsaP384();                // ECDSA P-384
    public static OpenSign sha256WithRsa();             // RSA-SHA256
    public static OpenSign rsaPss();                    // RSA-PSS（推荐）
    public static OpenSign sm2Sign();                   // SM2 签名
    public static OpenSign signer(SignatureAlgorithm algorithm);

    // ==================== 密码哈希 ====================
    public static PasswordHash argon2();               // Argon2id（推荐）
    public static PasswordHash bcrypt();               // BCrypt
    public static PasswordHash scrypt();               // SCrypt
    public static PasswordHash pbkdf2();               // PBKDF2

    // ==================== HMAC ====================
    public static Mac hmacSha256(byte[] key);
    public static Mac hmacSha512(byte[] key);

    // ==================== 密钥派生 ====================
    public static Hkdf hkdf();                         // HKDF-SHA256
    public static Pbkdf2 pbkdf2Kdf();                  // PBKDF2
    public static Argon2Kdf argon2Kdf();               // Argon2 KDF

    // ==================== 密钥协商 ====================
    public static KeyExchangeEngine x25519();           // X25519（推荐）
    public static KeyExchangeEngine ecdhP256();         // ECDH P-256

    // ==================== 信封/混合加密 ====================
    public static EnvelopeCrypto envelope();            // 信封加密
    public static HybridCrypto hybrid();                // 混合加密
}
```

**使用示例:**

```java
// 计算 SHA-256 摘要
String hash = OpenCrypto.sha256().digestHex("Hello, World!");

// AES-GCM 加密
AeadCipher cipher = OpenCrypto.aesGcm();
cipher.setKey(secretKey);
byte[] encrypted = cipher.encrypt("sensitive data");
String decrypted = cipher.decryptToString(encrypted);

// Ed25519 签名
OpenSign signer = OpenCrypto.ed25519().withGeneratedKeyPair();
String sig = signer.signBase64("important message");
boolean valid = signer.verifyBase64("important message", sig);

// Argon2id 密码哈希
PasswordHash hasher = OpenCrypto.argon2();
String hashed = hasher.hash("myPassword");
boolean match = hasher.verify("myPassword", hashed);
```

---

## 4. 摘要/哈希

### 4.1 OpenDigest — 消息摘要门面

提供多种哈希算法的一次性计算和流式计算。

```java
public final class OpenDigest {

    // ==================== 静态工厂 ====================
    public static OpenDigest sha256();
    public static OpenDigest sha384();
    public static OpenDigest sha512();
    public static OpenDigest sha3_256();
    public static OpenDigest sha3_512();
    public static OpenDigest sm3();
    public static OpenDigest blake2b(int digestLength);
    public static OpenDigest blake3();
    public static OpenDigest of(DigestAlgorithm algorithm);

    // ==================== 一次性计算 ====================
    public byte[] digest(byte[] data);
    public byte[] digest(String data);
    public String digestHex(byte[] data);
    public String digestHex(String data);
    public String digestBase64(byte[] data);
    public String digestBase64(String data);

    // ==================== 文件/流计算 ====================
    public byte[] digestFile(Path file);
    public String digestFileHex(Path file);
    public byte[] digest(InputStream input);
    public String digestHex(InputStream input);

    // ==================== 流式计算 ====================
    public OpenDigest update(byte[] data);
    public OpenDigest update(byte[] data, int offset, int length);
    public OpenDigest update(String data);
    public OpenDigest update(ByteBuffer buffer);
    public byte[] doFinal();
    public String doFinalHex();
    public String doFinalBase64();
    public OpenDigest reset();

    // ==================== 信息获取 ====================
    public String getAlgorithm();
    public int getDigestLength();
}
```

**使用示例:**

```java
// 一次性计算
String hex = OpenDigest.sha256().digestHex("Hello");

// 流式计算
String hash = OpenDigest.sha3_256()
    .update("part1")
    .update("part2")
    .doFinalHex();

// 文件摘要
String fileHash = OpenDigest.sha256().digestFileHex(Path.of("/path/to/file"));
```

### 4.2 哈希函数实现

| 类名 | 算法 | 摘要长度 | 说明 |
|------|------|----------|------|
| `Sha2Hash` | SHA-224/256/384/512 | 28/32/48/64 字节 | JDK 内置 |
| `Sha3Hash` | SHA3-224/256/384/512 | 28/32/48/64 字节 | JDK 内置 |
| `Sm3Hash` | SM3 | 32 字节 | 国密标准，需 Bouncy Castle |
| `Blake2Hash` | BLAKE2b/BLAKE2s | 可配置 | 高性能，需 Bouncy Castle |
| `Blake3Hash` | BLAKE3 | 可配置（默认 32） | 最新一代 |

### 4.3 HmacFunction — HMAC

```java
public final class HmacFunction {
    public static HmacFunction hmacSha256(byte[] key);
    public static HmacFunction hmacSha384(byte[] key);
    public static HmacFunction hmacSha512(byte[] key);
    public static HmacFunction of(String algorithm, byte[] key);

    public byte[] mac(byte[] data);
    public byte[] mac(String data);
    public String macHex(byte[] data);
    public String macBase64(byte[] data);
    public boolean verify(byte[] data, byte[] mac);  // 常量时间比较
    public String getAlgorithm();
}
```

---

## 5. 对称加密

### 5.1 AeadCipher — AEAD 加密接口

认证加密（Authenticated Encryption with Associated Data），推荐用于所有新项目。

```java
public interface AeadCipher {
    AeadCipher setKey(SecretKey key);
    AeadCipher setKey(byte[] key);
    AeadCipher setIv(byte[] iv);
    AeadCipher setNonce(byte[] nonce);
    AeadCipher setAad(byte[] aad);
    AeadCipher setTagLength(int tagBits);

    byte[] encrypt(byte[] plaintext);
    byte[] encrypt(String plaintext);
    String encryptBase64(byte[] plaintext);
    String encryptBase64(String plaintext);
    String encryptHex(byte[] plaintext);
    void encryptFile(Path source, Path target);
    OutputStream encryptStream(OutputStream output);

    byte[] decrypt(byte[] ciphertext);
    String decryptToString(byte[] ciphertext);
    byte[] decryptBase64(String base64Ciphertext);
    String decryptBase64ToString(String base64Ciphertext);
    byte[] decryptHex(String hexCiphertext);
    void decryptFile(Path source, Path target);
    InputStream decryptStream(InputStream input);

    byte[] generateIv();
    byte[] generateNonce();
    int getIvLength();
    String getAlgorithm();
}
```

### 5.2 AesGcmCipher — AES-GCM 实现

```java
public final class AesGcmCipher implements AeadCipher {
    public static AesGcmCipher aes128Gcm();
    public static AesGcmCipher aes256Gcm();    // 推荐
    public static AesGcmCipher create();
    public static Builder builder();

    // 额外方法
    public byte[] getIv();
    public SecretKey getKey();
    public int getTagLength();

    public static class Builder {
        public Builder keySize(int bits);       // 128 或 256
        public Builder tagLength(int bits);     // 96-128
        public AesGcmCipher build();
    }
}
```

### 5.3 ChaChaCipher — ChaCha20-Poly1305 实现

```java
public final class ChaChaCipher implements AeadCipher {
    public static ChaChaCipher create();
    public static Builder builder();

    public byte[] getNonce();
    public SecretKey getKey();
    public int getNonceLength();
    public int getTagLength();
}
```

### 5.4 OpenSymmetric — 非 AEAD 对称加密门面

用于 CBC/CTR 等传统模式。

```java
public final class OpenSymmetric {
    public static OpenSymmetric aesCbc();
    public static OpenSymmetric aesCtr();
    public static OpenSymmetric of(SymmetricAlgorithm algorithm);

    public OpenSymmetric setKey(SecretKey key);
    public OpenSymmetric setKey(byte[] key);
    public OpenSymmetric setIv(byte[] iv);

    public byte[] encrypt(byte[] plaintext);
    public byte[] encrypt(String plaintext);
    public String encryptHex(byte[] plaintext);
    public String encryptHex(String plaintext);
    public String encryptBase64(byte[] plaintext);
    public String encryptBase64(String plaintext);

    public byte[] decrypt(byte[] ciphertext);
    public String decryptToString(byte[] ciphertext);
    public byte[] decryptHex(String hexCiphertext);
    public String decryptHexToString(String hexCiphertext);
    public byte[] decryptBase64(String base64Ciphertext);
    public String decryptBase64ToString(String base64Ciphertext);

    public byte[] generateIv();
    public SecretKey generateKey(int keySize);
    public String getAlgorithm();
    public int getIvLength();
}
```

### 5.5 Sm4Cipher — SM4 国密实现

同时实现 `SymmetricCipher` 和 `AeadCipher`，支持 CBC 和 GCM 模式。

```java
public final class Sm4Cipher implements SymmetricCipher, AeadCipher {
    public static Sm4Cipher cbc();
    public static Sm4Cipher gcm();
    public static boolean isBouncyCastleAvailable();
}
```

**使用示例:**

```java
// AES-GCM 加解密
AeadCipher aes = AesGcmCipher.aes256Gcm();
SecretKey key = KeyGenerator.generateAes256Key();
aes.setKey(key);

byte[] encrypted = aes.encrypt("sensitive data");
String decrypted = aes.decryptToString(encrypted);

// ChaCha20-Poly1305 加密（适合移动设备）
AeadCipher chacha = ChaChaCipher.create();
chacha.setKey(KeyGenerator.generateChacha20Key());
String base64 = chacha.encryptBase64("data");

// AES-GCM 文件加密
AesGcmCipher fileEncryptor = AesGcmCipher.aes256Gcm();
fileEncryptor.setKey(key);
fileEncryptor.encryptFile(Path.of("plain.txt"), Path.of("encrypted.bin"));
fileEncryptor.decryptFile(Path.of("encrypted.bin"), Path.of("decrypted.txt"));
```

---

## 6. 非对称加密

### 6.1 OpenAsymmetric — 非对称加密门面

```java
public final class OpenAsymmetric {
    public static OpenAsymmetric rsaOaep();    // RSA-OAEP（推荐）
    public static OpenAsymmetric rsa();         // RSA PKCS1
    public static OpenAsymmetric ecc();         // ECC/ECIES
    public static OpenAsymmetric sm2();         // SM2
    public static OpenAsymmetric of(AsymmetricAlgorithm algorithm);

    public OpenAsymmetric setPrivateKey(PrivateKey privateKey);
    public OpenAsymmetric setPublicKey(PublicKey publicKey);
    public OpenAsymmetric setKeyPair(KeyPair keyPair);

    public byte[] encrypt(byte[] plaintext);
    public byte[] encrypt(String plaintext);
    public String encryptHex(byte[] plaintext);
    public String encryptHex(String plaintext);
    public String encryptBase64(byte[] plaintext);
    public String encryptBase64(String plaintext);

    public byte[] decrypt(byte[] ciphertext);
    public String decryptToString(byte[] ciphertext);
    public byte[] decryptHex(String hexCiphertext);
    public String decryptHexToString(String hexCiphertext);
    public byte[] decryptBase64(String base64Ciphertext);
    public String decryptBase64ToString(String base64Ciphertext);

    public KeyPair generateKeyPair();
    public OpenAsymmetric withGeneratedKeyPair();
    public String getAlgorithm();
    public int getMaxEncryptSize();
}
```

### 6.2 具体实现

| 类名 | 算法 | 说明 |
|------|------|------|
| `RsaOaepCipher` | RSA-OAEP-SHA256/384/512 | 推荐。支持 Builder 配置 digest/mgf/label |
| `RsaCipher` | RSA-PKCS1 | 传统方案，不推荐新项目使用 |
| `EccCipher` | ECIES (ECDH+AES-GCM) | 使用 P-256/P-384/P-521 曲线 |
| `Sm2Cipher` | SM2 | 国密标准，需 Bouncy Castle |

**使用示例:**

```java
// RSA-OAEP 加解密
RsaOaepCipher rsa = RsaOaepCipher.withGeneratedKeyPair(2048);
String encrypted = rsa.encryptBase64("secret message".getBytes());
byte[] decrypted = rsa.decryptBase64(encrypted);

// ECC 加解密
EccCipher ecc = EccCipher.withGeneratedKeyPair(CurveType.P_256);
byte[] ciphertext = ecc.encrypt("data");
String plaintext = ecc.decryptToString(ciphertext);

// 通过门面使用
OpenAsymmetric asym = OpenAsymmetric.rsaOaep().withGeneratedKeyPair();
String cipher = asym.encryptBase64("hello");
String plain = asym.decryptBase64ToString(cipher);
```

---

## 7. 数字签名

### 7.1 OpenSign — 签名验签门面

```java
public final class OpenSign {
    // 静态工厂
    public static OpenSign ed25519();        // Ed25519（推荐）
    public static OpenSign ed448();          // Ed448
    public static OpenSign ecdsaP256();      // ECDSA P-256
    public static OpenSign ecdsaP384();      // ECDSA P-384
    public static OpenSign ecdsaP521();      // ECDSA P-521
    public static OpenSign sha256WithRsa();   // RSA-SHA256
    public static OpenSign sha384WithRsa();   // RSA-SHA384
    public static OpenSign sha512WithRsa();   // RSA-SHA512
    public static OpenSign rsaPss();          // RSA-PSS-SHA256（推荐）
    public static OpenSign rsaPssSha384();    // RSA-PSS-SHA384
    public static OpenSign rsaPssSha512();    // RSA-PSS-SHA512
    public static OpenSign sm2();             // SM2 签名
    public static OpenSign of(SignatureAlgorithm algorithm);

    // 配置
    public OpenSign setPrivateKey(PrivateKey privateKey);
    public OpenSign setPublicKey(PublicKey publicKey);
    public OpenSign setKeyPair(KeyPair keyPair);

    // 签名
    public byte[] sign(byte[] data);
    public byte[] sign(String data);
    public String signHex(byte[] data);
    public String signHex(String data);
    public String signBase64(byte[] data);
    public String signBase64(String data);

    // 验签
    public boolean verify(byte[] data, byte[] signature);
    public boolean verify(String data, byte[] signature);
    public boolean verifyHex(byte[] data, String signatureHex);
    public boolean verifyHex(String data, String signatureHex);
    public boolean verifyBase64(byte[] data, String signatureBase64);
    public boolean verifyBase64(String data, String signatureBase64);

    // 密钥管理
    public KeyPair generateKeyPair();
    public OpenSign withGeneratedKeyPair();
    public String getAlgorithm();
}
```

### 7.2 签名引擎实现

| 类名 | 算法 | 说明 |
|------|------|------|
| `EddsaSignature` | Ed25519 / Ed448 | 推荐。确定性签名，抗侧信道攻击 |
| `EcdsaSignature` | ECDSA P-256/P-384/P-521 | 推荐。密钥短，性能好 |
| `RsaPssSignature` | RSA-PSS SHA256/384/512 | 推荐。可证明安全的 RSA 签名 |
| `RsaSignature` | SHA256/384/512 with RSA | PKCS#1 v1.5，传统方案 |
| `Sm2Signature` | SM3 with SM2 | 国密标准 |

所有签名引擎都实现 `SignatureEngine` 接口，额外支持:
- 文件签名: `signFile(Path)`、`verifyFile(Path, byte[])`
- 流签名: `sign(InputStream)`
- 流式签名: `update(byte[])` -> `doSign()` / `doVerify(byte[])`

**使用示例:**

```java
// Ed25519 签名
EddsaSignature ed = EddsaSignature.ed25519WithGeneratedKeyPair();
String signature = ed.signBase64("important data");
boolean valid = ed.verifyBase64("important data", signature);

// RSA-PSS 签名
RsaPssSignature rsa = RsaPssSignature.sha256WithKeyPair();
byte[] sig = rsa.sign("contract content");
boolean ok = rsa.verify("contract content", sig);

// 通过门面使用
OpenSign signer = OpenSign.ecdsaP256().withGeneratedKeyPair();
String hex = signer.signHex("message");
boolean verified = signer.verifyHex("message", hex);
```

---

## 8. 密码哈希

### 8.1 OpenPasswordHash — 密码哈希门面

```java
public final class OpenPasswordHash {
    public static OpenPasswordHash argon2();            // Argon2id（推荐）
    public static OpenPasswordHash bcrypt();             // BCrypt（默认 cost=12）
    public static OpenPasswordHash bcrypt(int cost);     // BCrypt 指定 cost
    public static OpenPasswordHash scrypt();             // SCrypt
    public static OpenPasswordHash pbkdf2();             // PBKDF2
    public static OpenPasswordHash pbkdf2(int iterations);
    public static OpenPasswordHash of(PasswordHashAlgorithm algorithm);

    public String hash(String password);
    public String hash(char[] password);
    public boolean verify(String password, String hash);
    public boolean verify(char[] password, String hash);
    public boolean needsRehash(String hash);

    // 密码策略
    public static PasswordPolicy defaultPolicy();
    public static PasswordPolicy strongPolicy();

    public String getAlgorithm();
}
```

### 8.2 PasswordHash 接口及实现

```java
public interface PasswordHash {
    String hash(char[] password);
    String hash(String password);
    boolean verify(char[] password, String hash);
    boolean verify(String password, String hash);
    boolean needsRehash(String hash);
    String getAlgorithm();
}
```

| 类名 | 算法 | 说明 | 需要 BC |
|------|------|------|---------|
| `Argon2Hash` | Argon2id/Argon2d/Argon2i | 推荐。抗 GPU/ASIC 攻击 | 是 |
| `BCryptHash` | BCrypt | 推荐。广泛使用，cost=4-31 | 否 |
| `SCryptHash` | SCrypt | 内存困难 | 是 |
| `Pbkdf2Hash` | PBKDF2-SHA256/SHA512 | JDK 内置，OWASP 推荐 310000 迭代 | 否 |

### 8.3 PasswordPolicy — 密码策略

```java
public final class PasswordPolicy {
    public static PasswordPolicy defaultPolicy();   // 默认策略
    public static PasswordPolicy strong();           // 强密码策略
    public static PasswordPolicy basic();            // 基本策略
    public static Builder builder();

    public ValidationResult validate(String password);
    public boolean isValid(String password);
    public int getMinLength();
    public int getMaxLength();

    public static final class ValidationResult {
        public boolean isValid();
        public List<String> getViolations();
        public String getFirstViolation();
    }

    public static final class Builder {
        public Builder minLength(int minLength);
        public Builder maxLength(int maxLength);
        public Builder requireUppercase(boolean require);
        public Builder requireLowercase(boolean require);
        public Builder requireDigit(boolean require);
        public Builder requireSpecial(boolean require);
        public Builder minCharacterTypes(int minTypes);
        public PasswordPolicy build();
    }
}
```

**使用示例:**

```java
// Argon2 密码哈希
Argon2Hash hasher = Argon2Hash.argon2id();
String hashed = hasher.hash("myPassword123");
boolean match = hasher.verify("myPassword123", hashed);
boolean needsUpdate = hasher.needsRehash(hashed);

// BCrypt 密码哈希
BCryptHash bcrypt = BCryptHash.withCost(12);
String hash = bcrypt.hash("password");

// 密码策略验证
PasswordPolicy policy = PasswordPolicy.strong();
PasswordPolicy.ValidationResult result = policy.validate("weak");
if (!result.isValid()) {
    System.out.println("Violations: " + result.getViolations());
}

// 自定义策略
PasswordPolicy custom = PasswordPolicy.builder()
    .minLength(12)
    .requireUppercase(true)
    .requireDigit(true)
    .requireSpecial(true)
    .minCharacterTypes(3)
    .build();
```

---

## 9. 密钥管理

### 9.1 KeyGenerator — 密钥生成器

```java
public final class KeyGenerator {

    // 对称密钥
    public static SecretKey generateAesKey(int keyBits);
    public static SecretKey generateAes128Key();
    public static SecretKey generateAes256Key();
    public static SecretKey generateChacha20Key();
    public static SecretKey generateSm4Key();
    public static SecretKey secretKey(byte[] keyBytes, String algorithm);

    // 非对称密钥对
    public static KeyPair generateRsaKeyPair(int keyBits);
    public static KeyPair generateRsa2048KeyPair();
    public static KeyPair generateRsa4096KeyPair();
    public static KeyPair generateEcKeyPair(CurveType curve);
    public static KeyPair generateP256KeyPair();
    public static KeyPair generateP384KeyPair();
    public static KeyPair generateEd25519KeyPair();
    public static KeyPair generateEd448KeyPair();
    public static KeyPair generateX25519KeyPair();
    public static KeyPair generateSm2KeyPair();

    // 密钥导出
    public static byte[] exportPublicKey(PublicKey publicKey);
    public static byte[] exportPrivateKey(PrivateKey privateKey);
    public static String exportPublicKeyPem(PublicKey publicKey);
    public static String exportPrivateKeyPem(PrivateKey privateKey);
    public static String exportKeyPairPem(KeyPair keyPair);

    // 密钥导入
    public static PublicKey importPublicKey(byte[] encoded, String algorithm);
    public static PrivateKey importPrivateKey(byte[] encoded, String algorithm);
    public static PublicKey importPublicKeyPem(String pem);
    public static PrivateKey importPrivateKeyPem(String pem);
    public static KeyPair importKeyPairPem(String publicKeyPem, String privateKeyPem);
}
```

### 9.2 SecureKeyStore — 安全密钥存储

基于 PKCS12 格式的密钥存储，支持对称密钥和非对称密钥对。

```java
public final class SecureKeyStore implements AutoCloseable {
    public static SecureKeyStore create();
    public static SecureKeyStore load(Path path, char[] password);

    public void store(String alias, SecretKey key, char[] password);
    public void store(String alias, KeyPair keyPair, char[] password);
    public void store(String alias, PrivateKey privateKey, char[] password, Certificate[] chain);

    public SecretKey getSecretKey(String alias, char[] password);
    public KeyPair getKeyPair(String alias, char[] password);
    public PrivateKey getPrivateKey(String alias, char[] password);
    public PublicKey getPublicKey(String alias);

    public boolean containsAlias(String alias);
    public void deleteEntry(String alias);
    public Set<String> aliases();

    public void save(Path path, char[] password);
    public void close();
}
```

### 9.3 KeyRotation — 密钥轮换

自动化密钥版本管理，支持优雅的密钥轮换。

```java
public final class KeyRotation<K> implements AutoCloseable {
    public VersionedKey<K> getCurrentKey();
    public VersionedKey<K> getKeyByVersion(long version);
    public long getCurrentVersion();
    public Set<Long> getAvailableVersions();
    public String getKeyId();

    public synchronized VersionedKey<K> rotate();
    public synchronized VersionedKey<K> addKey(K key);
    public synchronized void importKey(long version, K key, Instant createdAt);
    public synchronized int retireOldVersions();
    public synchronized int deleteRetiredKeys();
    public synchronized void startAutoRotation();
    public synchronized void stopAutoRotation();
    public boolean isRotationNeeded();
    public Duration getTimeUntilNextRotation();
    public void close();

    // 静态工厂
    public static <K> Builder<K> builder();
    public static KeyRotation<SecretKey> forAes(String keyId, int keyBits);
    public static KeyRotation<KeyPair> forRsa(String keyId, int keyBits);

    public static final class Builder<K> {
        public Builder<K> keyId(String keyId);
        public Builder<K> keyGenerator(Supplier<K> generator);
        public Builder<K> rotationInterval(Duration interval);
        public Builder<K> gracePeriod(Duration period);
        public Builder<K> maxVersions(int max);
        public Builder<K> initialKey(K key);
        public Builder<K> onRotation(Consumer<RotationEvent> listener);
        public KeyRotation<K> build();
    }

    public record VersionedKey<K>(long version, K key, Instant createdAt, KeyStatus status) {}
    public enum KeyStatus { ACTIVE, RETIRED, DESTROYED }
}
```

**使用示例:**

```java
// 创建 AES 密钥轮换器
KeyRotation<SecretKey> rotation = KeyRotation.<SecretKey>builder()
    .keyId("my-encryption-key")
    .keyGenerator(() -> KeyGenerator.generateAes256Key())
    .rotationInterval(Duration.ofDays(90))
    .maxVersions(5)
    .onRotation(event -> log.info("Key rotated: {}", event))
    .build();

// 获取当前密钥
VersionedKey<SecretKey> current = rotation.getCurrentKey();
SecretKey key = current.key();

// 手动轮换
rotation.rotate();

// 通过版本号获取旧密钥（用于解密旧数据）
VersionedKey<SecretKey> oldKey = rotation.getKeyByVersion(1);
```

---

## 10. 密钥派生

### 10.1 Hkdf — HMAC-based KDF

```java
public final class Hkdf implements KdfEngine {
    public static Hkdf sha256();
    public static Hkdf sha384();
    public static Hkdf sha512();

    public byte[] extract(byte[] salt, byte[] ikm);
    public byte[] expand(byte[] prk, byte[] info, int length);
    public byte[] extractAndExpand(byte[] salt, byte[] ikm, byte[] info, int length);
    public byte[] deriveKey(byte[] ikm, byte[] info, int length);
    public byte[] deriveKey(byte[] ikm, byte[] salt, byte[] info, int length);
    public byte[][] deriveKeys(byte[] salt, byte[] ikm, byte[][] infos, int[] lengths);

    public String getAlgorithm();
    public int getHashLength();
}
```

### 10.2 Pbkdf2 — 密码密钥派生

```java
public final class Pbkdf2 implements KdfEngine {
    public static Pbkdf2 hmacSha256(int iterations);
    public static Pbkdf2 hmacSha512(int iterations);
    public static Pbkdf2 owaspRecommended();

    public byte[] generateSalt();
    public byte[] generateSalt(int length);
    public byte[] deriveKey(char[] password, byte[] salt, int keyLength);
    public byte[] deriveKey(char[] password, byte[] salt, int keyLength, int iterations);

    public String getAlgorithm();
    public int getIterations();
}
```

### 10.3 Argon2Kdf — Argon2 KDF

```java
public final class Argon2Kdf implements KdfEngine {
    public static Argon2Kdf argon2id();
    public static Argon2Kdf argon2d();
    public static Argon2Kdf argon2i();
    public static Builder builder();

    public byte[] deriveKey(byte[] password, byte[] salt, int length);
    public byte[] deriveKey(byte[] password, byte[] salt, byte[] secret, byte[] ad, int length);
    public byte[] deriveKey(char[] password, byte[] salt, int length);
    public byte[] deriveKey(String password, byte[] salt, int length);
    public static byte[] generateSalt();
    public static byte[] generateSalt(int length);

    public String getAlgorithm();
    public Argon2Type getType();
    public int getIterations();
    public int getMemory();
    public int getParallelism();
}
```

---

## 11. 密钥协商

### 11.1 KeyExchangeEngine 接口

```java
public interface KeyExchangeEngine {
    KeyPair generateKeyPair();
    KeyExchangeEngine setPrivateKey(PrivateKey privateKey);
    KeyExchangeEngine setRemotePublicKey(PublicKey publicKey);
    byte[] computeSharedSecret();
    byte[] deriveKey(byte[] info, int length);
    String getAlgorithm();
}
```

### 11.2 实现

| 类名 | 算法 | 说明 |
|------|------|------|
| `X25519Engine` | X25519 | 推荐。高性能，固定 32 字节密钥 |
| `X448Engine` | X448 | 更高安全级别 |
| `EcdhEngine` | ECDH P-256/P-384/P-521 | NIST 标准曲线 |

所有实现都提供静态便捷方法:
- `agree(PrivateKey, PublicKey)` — 一步完成协商
- `agreeAndDerive(PrivateKey, PublicKey, byte[] info, int length)` — 协商并派生密钥

**使用示例:**

```java
// X25519 密钥协商
X25519Engine alice = X25519Engine.withGeneratedKeyPair();
X25519Engine bob = X25519Engine.withGeneratedKeyPair();

// Alice 使用 Bob 的公钥
alice.setRemotePublicKey(bob.getPublicKey());
byte[] aliceSecret = alice.computeSharedSecret();

// Bob 使用 Alice 的公钥
bob.setRemotePublicKey(alice.getPublicKey());
byte[] bobSecret = bob.computeSharedSecret();

// aliceSecret == bobSecret (共享密钥相同)

// 一步完成密钥协商
byte[] shared = X25519Engine.agree(alicePrivateKey, bobPublicKey);

// 协商并派生 AES 密钥
byte[] aesKey = X25519Engine.agreeAndDerive(
    alicePrivateKey, bobPublicKey, "AES-256-GCM".getBytes(), 32);
```

---

## 12. 信封加密与混合加密

### 12.1 EnvelopeCrypto — 信封加密

使用非对称加密保护对称密钥（DEK），对称密钥加密实际数据。

```java
public final class EnvelopeCrypto {
    public static EnvelopeCrypto rsaAesGcm();          // RSA + AES-GCM
    public static EnvelopeCrypto ecdhAesGcm();          // ECDH + AES-GCM
    public static EnvelopeCrypto x25519ChaCha20();      // X25519 + ChaCha20

    public EnvelopeCrypto setRecipientPublicKey(PublicKey publicKey);
    public EnvelopeCrypto setRecipientPrivateKey(PrivateKey privateKey);

    public EncryptedEnvelope encrypt(byte[] plaintext);
    public EncryptedEnvelope encrypt(byte[] plaintext, byte[] aad);
    public String encryptBase64(byte[] plaintext);

    public byte[] decrypt(EncryptedEnvelope envelope);
    public byte[] decrypt(EncryptedEnvelope envelope, byte[] aad);
    public byte[] decryptBase64(String base64Envelope);
}
```

### 12.2 EncryptedEnvelope — 加密信封数据结构

```java
public final class EncryptedEnvelope {
    public EncryptedEnvelope(byte[] encryptedKey, byte[] iv, byte[] ciphertext, byte[] tag);

    public byte[] encryptedKey();
    public byte[] iv();
    public byte[] ciphertext();
    public byte[] tag();

    public String toBase64();
    public static EncryptedEnvelope fromBase64(String base64);
    public byte[] toBytes();
    public static EncryptedEnvelope fromBytes(byte[] bytes);
}
```

### 12.3 HybridCrypto — 混合加密

简化的信封加密，透明格式。

```java
public final class HybridCrypto {
    public static HybridCrypto rsaAes();
    public static HybridCrypto ecdhAes();
    public static HybridCrypto x25519ChaCha20();
    public static Builder builder();

    public HybridCrypto setRecipientPublicKey(PublicKey publicKey);
    public HybridCrypto setRecipientPrivateKey(PrivateKey privateKey);

    public byte[] encrypt(byte[] plaintext);
    public String encryptBase64(byte[] plaintext);
    public byte[] decrypt(byte[] ciphertext);
    public byte[] decryptBase64(String base64Ciphertext);
}
```

---

## 13. NaCl 风格加密

### 13.1 SealedBox — 匿名公钥加密

类似 NaCl/Libsodium 的 SealedBox，发送者匿名。

```java
public final class SealedBox {
    // 便捷静态方法
    public static byte[] seal(byte[] plaintext, PublicKey recipientPublicKey);
    public static byte[] seal(String plaintext, PublicKey recipientPublicKey);
    public static byte[] open(byte[] sealed, KeyPair recipientKeyPair);
    public static String openAsString(byte[] sealed, KeyPair recipientKeyPair);
    public static KeyPair generateKeyPair();

    // Builder 模式
    public static Builder builder();

    public enum Algorithm { X25519_XSALSA20_POLY1305, X25519_CHACHA20_POLY1305 }
}
```

### 13.2 SecretBox — 简化对称加密

```java
public final class SecretBox {
    public static SecretKey generateKey();
    public static SecretKey keyFromBytes(byte[] keyBytes);

    public static byte[] encrypt(byte[] plaintext, SecretKey key);
    public static byte[] encrypt(String plaintext, SecretKey key);
    public static byte[] encryptWithAad(byte[] plaintext, SecretKey key, byte[] aad);

    public static byte[] decrypt(byte[] encrypted, SecretKey key);
    public static String decryptAsString(byte[] encrypted, SecretKey key);
    public static byte[] decryptWithAad(byte[] encrypted, SecretKey key, byte[] aad);
}
```

**使用示例:**

```java
// SealedBox: 匿名加密
KeyPair recipient = SealedBox.generateKeyPair();
byte[] sealed = SealedBox.seal("secret message", recipient.getPublic());
String message = SealedBox.openAsString(sealed, recipient);

// SecretBox: 简单对称加密
SecretKey key = SecretBox.generateKey();
String encrypted = Base64.getEncoder().encodeToString(
    SecretBox.encrypt("hello", key));
String decrypted = SecretBox.decryptAsString(
    Base64.getDecoder().decode(encrypted), key);
```

---

## 14. PGP 加密

### 14.1 OpenPgp — PGP 门面

```java
public final class OpenPgp {
    // 密钥管理
    public static PgpKeyPair generateKeyPair(String userId, String passphrase);
    public static PgpKeyPair generateKeyPair(String userId, String passphrase, int keySize);
    public static String exportPublicKey(PgpKeyPair keyPair);
    public static String exportPublicKey(PGPPublicKey publicKey);
    public static String exportSecretKey(PgpKeyPair keyPair);
    public static String exportSecretKey(PGPSecretKey secretKey);
    public static PGPPublicKey importPublicKey(String armoredKey);
    public static PgpKeyPair importKeyPair(String armoredSecretKey, String passphrase);

    // 加密
    public static String encrypt(String plaintext, PGPPublicKey publicKey);
    public static String encrypt(String plaintext, String armoredPublicKey);
    public static String encrypt(String plaintext, PgpKeyPair keyPair);
    public static byte[] encrypt(byte[] data, PGPPublicKey publicKey);
    public static String encryptArmored(byte[] data, PGPPublicKey publicKey);

    // 解密
    public static String decrypt(String armoredMessage, PGPSecretKey secretKey, String passphrase);
    public static String decrypt(String armoredMessage, String armoredSecretKey, String passphrase);
    public static String decrypt(String armoredMessage, PgpKeyPair keyPair, String passphrase);
    public static byte[] decrypt(byte[] encryptedData, PGPSecretKey secretKey, String passphrase);

    // 密钥信息
    public static String keyIdHex(PGPPublicKey publicKey);
    public static String fingerprintHex(PGPPublicKey publicKey);

    // 创建 PgpCipher
    public static PgpCipher cipher();
}
```

### 14.2 PgpCipher — PGP 加密器

链式 API 的 PGP 加密器，支持更细粒度的配置。

```java
public final class PgpCipher {
    public static PgpCipher create();

    public PgpCipher withPublicKey(PGPPublicKey publicKey);
    public PgpCipher withPublicKey(String armoredKey);
    public PgpCipher withSecretKey(PGPSecretKey secretKey, String passphrase);
    public PgpCipher withSecretKey(String armoredKey, String passphrase);
    public PgpCipher withKeyPair(PgpKeyPair keyPair, String passphrase);
    public PgpCipher withSymmetricAlgorithm(PgpAlgorithm.Symmetric algorithm);
    public PgpCipher withIntegrityCheck(boolean enabled);
    public PgpCipher withCompression(boolean enabled);

    public String encryptArmored(String plaintext);
    public String encryptArmored(byte[] data);
    public byte[] encrypt(byte[] data);
    public String encryptBase64(byte[] data);

    public String decryptArmored(String armoredData);
    public byte[] decryptArmoredToBytes(String armoredData);
    public byte[] decrypt(byte[] encryptedData);
    public byte[] decryptBase64(String base64Data);
    public String decryptBase64ToString(String base64Data);
}
```

**使用示例:**

```java
// 生成 PGP 密钥对
PgpKeyPair keyPair = OpenPgp.generateKeyPair("user@example.com", "passphrase");

// 导出公钥（分享给对方）
String publicKeyArmor = OpenPgp.exportPublicKey(keyPair);

// 加密
String encrypted = OpenPgp.encrypt("Hello, PGP!", keyPair.publicKey());

// 解密
String decrypted = OpenPgp.decrypt(encrypted, keyPair.secretKey(), "passphrase");

// 使用 PgpCipher
PgpCipher cipher = PgpCipher.create()
    .withPublicKey(keyPair.publicKey())
    .withSymmetricAlgorithm(PgpAlgorithm.Symmetric.AES_256)
    .withCompression(true);
String armored = cipher.encryptArmored("confidential data");
```

---

## 15. JWT 工具

### 15.1 OpenJwt — JWT 门面

```java
public final class OpenJwt {
    // HMAC 签名
    public static String sign(String subject, String secret, Duration expiration);
    public static String sign(JwtClaims claims, String secret);
    public static String sign(JwtClaims claims, String secret, JwtAlgorithm algorithm);

    // RSA 签名
    public static String signRsa(String subject, PrivateKey privateKey, Duration expiration);
    public static String signRsa(JwtClaims claims, PrivateKey privateKey);
    public static String signRsa(JwtClaims claims, PrivateKey privateKey, JwtAlgorithm algorithm);

    // ECDSA 签名
    public static String signEc(String subject, PrivateKey privateKey, Duration expiration);
    public static String signEc(JwtClaims claims, PrivateKey privateKey);
    public static String signEc(JwtClaims claims, PrivateKey privateKey, JwtAlgorithm algorithm);

    // 验证
    public static JwtClaims verify(String token, String secret);
    public static JwtClaims verify(String token, PublicKey publicKey);
    public static JwtClaims parseUnsafe(String token);   // 不验证签名

    // 密钥生成
    public static KeyPair generateRsaKeyPair();
    public static KeyPair generateRsaKeyPair(int keySize);
    public static KeyPair generateEcKeyPair();
    public static KeyPair generateEcKeyPair(String curveName);

    // Builder
    public static JwtUtil.Builder builder();
    public static JwtClaims.Builder claims();
}
```

### 15.2 JwtClaims — JWT 声明

```java
public final class JwtClaims {
    // 标准声明
    public String issuer();
    public String subject();
    public List<String> audience();
    public Instant expiration();
    public Instant notBefore();
    public Instant issuedAt();
    public String jwtId();

    // 自定义声明
    public Object get(String name);
    public String getString(String name);
    public Integer getInt(String name);
    public Long getLong(String name);
    public Boolean getBoolean(String name);
    public Instant getInstant(String name);
    public <T> List<T> getList(String name);

    // 查询
    public boolean contains(String name);
    public Set<String> names();
    public Map<String, Object> asMap();
    public boolean isExpired();
    public boolean isNotYetValid();

    // 工厂
    public static Builder builder();
    public static JwtClaims of(Map<String, Object> claims);
    public static JwtClaims empty();

    public static final class Builder {
        public Builder issuer(String issuer);
        public Builder subject(String subject);
        public Builder audience(String audience);
        public Builder audience(List<String> audiences);
        public Builder expiration(Instant expiration);
        public Builder expiresIn(Duration duration);
        public Builder notBefore(Instant notBefore);
        public Builder issuedAtNow();
        public Builder issuedAt(Instant issuedAt);
        public Builder jwtId(String jwtId);
        public Builder generateJwtId();
        public Builder claim(String name, Object value);
        public Builder claims(Map<String, Object> claims);
        public JwtClaims build();
    }
}
```

### 15.3 JwtAlgorithm — JWT 算法

支持的算法: HS256/HS384/HS512, RS256/RS384/RS512, ES256/ES384/ES512。

**使用示例:**

```java
// HMAC 签名
String token = OpenJwt.sign("user123", "secret-key", Duration.ofHours(1));
JwtClaims claims = OpenJwt.verify(token, "secret-key");
System.out.println(claims.subject()); // "user123"

// 自定义声明
JwtClaims customClaims = OpenJwt.claims()
    .subject("user123")
    .issuer("my-app")
    .expiresIn(Duration.ofHours(24))
    .issuedAtNow()
    .generateJwtId()
    .claim("role", "admin")
    .claim("permissions", List.of("read", "write"))
    .build();
String customToken = OpenJwt.sign(customClaims, "secret-key");

// RSA 签名
KeyPair keyPair = OpenJwt.generateRsaKeyPair();
String rsaToken = OpenJwt.signRsa("user123", keyPair.getPrivate(), Duration.ofHours(1));
JwtClaims rsaClaims = OpenJwt.verify(rsaToken, keyPair.getPublic());

// Builder 模式
String token = JwtUtil.builder()
    .algorithm(JwtAlgorithm.HS256)
    .secret("my-secret")
    .subject("user123")
    .expiresIn(Duration.ofHours(1))
    .claim("role", "admin")
    .sign();
```

---

## 16. 编解码工具

### 16.1 HexCodec — 十六进制编解码

```java
public final class HexCodec {
    public static String encode(byte[] data);
    public static String encodeUpperCase(byte[] data);
    public static byte[] decode(String hex);
    public static boolean isValidHex(String hex);
}
```

### 16.2 PemCodec — PEM 编解码

```java
public final class PemCodec {
    public static String encodePublicKey(byte[] key);
    public static byte[] decodePublicKey(String pem);
    public static String encodePrivateKey(byte[] key);
    public static byte[] decodePrivateKey(String pem);
    public static String encodeCertificate(byte[] cert);
    public static byte[] decodeCertificate(String pem);
    public static String encode(String type, byte[] data);
    public static byte[] decode(String pem);
    public static String getType(String pem);
}
```

### 16.3 Base64UrlCodec — Base64URL 编解码

```java
public final class Base64UrlCodec {
    public static String encode(byte[] data);
    public static byte[] decode(String data);
    public static String encodeNoPadding(byte[] data);
    public static byte[] decodeNoPadding(String data);
}
```

---

## 17. 随机数与 Nonce

### 17.1 SecureRandoms — 安全随机数工厂

```java
public final class SecureRandoms {
    public static SecureRandom getDefault();
    public static SecureRandom getStrong();
    public static SecureRandom getDrbg();
    public static SecureRandom getDrbg(int strength, ...);
    public static SecureRandom getInstance(String algorithm);
    public static SecureRandom getInstance(String algorithm, String provider);
    public static byte[] generateSeed(int numBytes);
    public static byte[] nextBytes(int numBytes);
}
```

### 17.2 RandomBytes — 随机字节生成

```java
public final class RandomBytes {
    public static byte[] generate(int length);
    public static byte[] generate(int length, SecureRandom random);
    public static String generateHex(int length);
    public static String generateBase64(int length);
    public static String generateBase64Url(int length);
}
```

### 17.3 NonceGenerator — Nonce 生成器

```java
public final class NonceGenerator {
    public static final int AES_GCM_NONCE_SIZE = 12;
    public static final int CHACHA20_NONCE_SIZE = 12;

    public static byte[] random(int length);
    public static byte[] counter(long counter, int length);
    public static byte[] timestamp(int randomLength);
    public static byte[] hybrid(int totalLength);
    public static byte[] forAesGcm();
    public static byte[] forChaCha20();
}
```

---

## 18. 安全工具

### 18.1 CryptoUtil

```java
public final class CryptoUtil {
    public static boolean constantTimeEquals(byte[] a, byte[] b);
    public static boolean constantTimeEquals(String a, String b);
    public static void secureErase(byte[] data);
    public static void secureErase(char[] data);
    public static byte[] randomBytes(int length);
}
```

### 18.2 ConstantTimeUtil

```java
public final class ConstantTimeUtil {
    public static boolean equals(byte[] a, byte[] b);
    public static boolean equals(String a, String b);
    public static int compare(byte[] a, byte[] b);
}
```

### 18.3 ByteUtil

```java
public final class ByteUtil {
    public static byte[] concat(byte[]... arrays);
    public static byte[][] split(byte[] array, int... lengths);
    public static byte[] xor(byte[] a, byte[] b);
    public static byte[] reverse(byte[] array);
    public static byte[] intToBytes(int value);
    public static int bytesToInt(byte[] bytes);
    public static byte[] longToBytes(long value);
    public static long bytesToLong(byte[] bytes);
    public static byte[] padPkcs7(byte[] data, int blockSize);
    public static byte[] unpadPkcs7(byte[] data);
}
```

### 18.4 CryptoDetector — 加密数据检测

```java
public final class CryptoDetector {
    public static double calculateEntropy(byte[] data);
    public static double calculateEntropy(String text);
    public static boolean looksEncrypted(byte[] data);
    public static boolean hasUniformByteDistribution(byte[] data, double tolerance);

    public static EncodingType detectEncoding(String text);
    public static KeyFormat detectKeyFormat(String keyData);
    public static HashFormat detectHashFormat(String hashString);
    public static CryptoAnalysis analyze(byte[] data);
    public static CryptoAnalysis analyze(String text);
    public static boolean looksLikeSecret(String text);
    public static int estimateSecurityStrength(byte[] data);

    public enum EncodingType { BASE64, BASE64_URL, HEX, ... }
    public enum KeyFormat { PEM_PUBLIC, PEM_PRIVATE, PEM_CERTIFICATE, ... }
    public enum HashFormat { BCRYPT, ARGON2, SCRYPT, PBKDF2, ... }
    public record CryptoAnalysis(double entropy, boolean looksEncrypted, ...) {}
}
```

---

## 19. 异常体系

### 19.1 OpenCryptoException — 加密异常

```java
public class OpenCryptoException extends RuntimeException {
    public OpenCryptoException(String message);
    public OpenCryptoException(String message, Throwable cause);
    public OpenCryptoException(String algorithm, String operation, String message);
    public OpenCryptoException(String algorithm, String operation, String message, Throwable cause);

    public String algorithm();
    public String operation();

    // 工厂方法
    public static OpenCryptoException algorithmNotAvailable(String algorithm);
    public static OpenCryptoException encryptionFailed(String algorithm, Throwable cause);
    public static OpenCryptoException decryptionFailed(String algorithm, Throwable cause);
    public static OpenCryptoException authenticationFailed(String algorithm);
    public static OpenCryptoException paddingError(String algorithm);
    public static OpenCryptoException invalidIv(String algorithm, int expected, int actual);
    public static OpenCryptoException dataTooLong(String algorithm, int maxSize);
}
```

### 19.2 OpenKeyException — 密钥异常

```java
public class OpenKeyException extends RuntimeException {
    public OpenKeyException(String message);
    public OpenKeyException(String message, Throwable cause);
    public OpenKeyException(String keyType, String message);
    public OpenKeyException(String keyType, String message, Throwable cause);

    public String keyType();

    public static OpenKeyException generationFailed(String keyType, Throwable cause);
    public static OpenKeyException parseFailed(String keyType, Throwable cause);
    public static OpenKeyException insufficientStrength(String keyType, int minBits, int actualBits);
    public static OpenKeyException invalidFormat(String keyType, String format);
    public static OpenKeyException typeMismatch(String expected, String actual);
    public static OpenKeyException keyNotSet(String operation);
}
```

注意: `OpenKeyException` 直接继承 `RuntimeException`，不继承 `OpenCryptoException`。

### 19.3 OpenSignatureException — 签名异常

```java
public class OpenSignatureException extends RuntimeException {
    public OpenSignatureException(String message);
    public OpenSignatureException(String message, Throwable cause);
    public OpenSignatureException(String algorithm, String message);
    public OpenSignatureException(String algorithm, String message, Throwable cause);

    public String algorithm();

    public static OpenSignatureException signFailed(String algorithm, Throwable cause);
    public static OpenSignatureException verifyFailed(String algorithm, Throwable cause);
    public static OpenSignatureException invalidSignature(String algorithm);
    public static OpenSignatureException invalidFormat(String algorithm, String format);
}
```

---

## 20. 算法枚举

### 20.1 DigestAlgorithm — 摘要算法

支持: SHA-256, SHA-384, SHA-512, SHA3-256, SHA3-512, SM3, BLAKE2B, BLAKE3 等。

每个枚举值提供:
- `getAlgorithmName()` — JCA 算法名
- `getDigestLength()` — 摘要长度（位）
- `isSecure()` — 是否安全（排除 MD5/SHA1）

### 20.2 SymmetricAlgorithm — 对称加密算法

支持: AES-GCM-128/256, AES-CBC-128/256, AES-CTR-128/256, ChaCha20-Poly1305, SM4-GCM, SM4-CBC。

每个枚举值提供:
- `getTransformation()` — JCA transformation
- `getKeySize()` — 密钥大小（位）
- `isAead()` — 是否为 AEAD 算法
- `isRecommended()` — 是否推荐

### 20.3 AsymmetricAlgorithm — 非对称加密算法

支持: RSA-OAEP-SHA256/384/512, RSA-PKCS1, SM2。

### 20.4 SignatureAlgorithm — 签名算法

支持: Ed25519, Ed448, ECDSA-P256/P384/P521, RSA-SHA256/384/512, RSA-PSS-SHA256/384/512, SM2。

### 20.5 CurveType — 椭圆曲线类型

支持: P-256, P-384, P-521, secp256k1, Curve25519, Curve448, Ed25519, Ed448, SM2。

---

## 21. 版本信息

| 属性 | 值 |
|------|-----|
| 模块名 | opencode-base-crypto |
| 编号 | 104 |
| 层级 | 基础组件 (1xx) |
| 最低 JDK | 25 |
| 核心依赖 | opencode-base-core |
| 可选依赖 | Bouncy Castle (SM2/SM3/SM4/Argon2/SCrypt/PGP) |
