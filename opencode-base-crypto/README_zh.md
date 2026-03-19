# OpenCode Base Crypto

面向 JDK 25+ 的现代密码学工具库，全面支持对称/非对称加密、数字签名、密码哈希、密钥派生、密钥交换等。

## 功能特性

- 对称加密：AES-GCM、AES-CBC、ChaCha20-Poly1305、SM4-GCM/CBC
- 非对称加密：RSA-OAEP、RSA-PKCS1、SM2、ECC
- 数字签名：Ed25519、Ed448、ECDSA、RSA-SHA256、RSA-PSS、SM2
- 密码哈希：Argon2id、BCrypt、SCrypt、PBKDF2
- 消息摘要：SHA-256/384/512、SHA3-256/512、SM3、BLAKE2b、BLAKE3
- HMAC：HMAC-SHA256、HMAC-SHA512、Poly1305
- 密钥派生：HKDF、PBKDF2、Argon2、Scrypt
- 密钥交换：X25519、X448、ECDH
- 信封/混合加密（RSA + AES-GCM）
- JWT 创建和验证
- PGP 加密和密钥管理
- SSL/TLS 上下文构建器和证书固定
- 安全随机数、Nonce 生成和密钥轮换
- Sealed Box 和 Secret Box
- 编解码工具：Base64URL、Hex、PEM
- 常量时间比较和安全内存擦除
- 可选 Bouncy Castle 集成（SM2/SM3/SM4、BLAKE、PGP）

## Maven 依赖

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-crypto</artifactId>
    <version>1.0.0</version>
</dependency>
```

## API 概览

| 类名 | 说明 |
|------|------|
| `OpenCrypto` | 门面类，提供对所有加密功能的简化访问 |
| `OpenSymmetric` | 对称加密操作（AES-CBC 等） |
| `OpenAsymmetric` | 非对称加密操作（RSA、SM2、ECC） |
| `OpenSign` | 数字签名操作（Ed25519、ECDSA、RSA、SM2） |
| `OpenDigest` | 消息摘要/哈希操作 |
| `OpenPasswordHash` | 密码哈希门面 |
| `OpenJwt` | JWT 创建和验证 |
| `OpenPgp` | PGP 加密和密钥管理 |
| `OpenSsl` | SSL/TLS 门面，用于上下文和证书操作 |
| **对称加密** | |
| `AesGcmCipher` | AES-GCM AEAD 加密（128/256 位） |
| `AesCipher` | AES 加密，可配置模式和填充 |
| `ChaChaCipher` | ChaCha20-Poly1305 AEAD 加密 |
| `Sm4Cipher` | SM4 加密（GCM/CBC 模式，需要 Bouncy Castle） |
| `AeadCipher` | AEAD 加密接口 |
| `SymmetricCipher` | 对称加密接口 |
| `AesKeyValidator` | AES 密钥大小验证 |
| `CipherMode` | 加密模式枚举 |
| `Padding` | 填充方案枚举 |
| **非对称加密** | |
| `RsaCipher` | RSA-PKCS1 加密 |
| `RsaOaepCipher` | RSA-OAEP 加密 |
| `EccCipher` | ECC 加密 |
| `Sm2Cipher` | SM2 加密（需要 Bouncy Castle） |
| `AsymmetricCipher` | 非对称加密接口 |
| **签名** | |
| `EddsaSignature` | Ed25519/Ed448 签名 |
| `EcdsaSignature` | ECDSA 签名 |
| `RsaSignature` | RSA-SHA256 签名 |
| `RsaPssSignature` | RSA-PSS 签名 |
| `Sm2Signature` | SM2 签名（需要 Bouncy Castle） |
| `SignatureEngine` | 签名引擎接口 |
| **哈希/摘要** | |
| `Sha2Hash` | SHA-2 系列哈希函数 |
| `Sha3Hash` | SHA-3 系列哈希函数 |
| `Sm3Hash` | SM3 哈希（需要 Bouncy Castle） |
| `Blake2Hash` | BLAKE2b 哈希（需要 Bouncy Castle） |
| `Blake3Hash` | BLAKE3 哈希（需要 Bouncy Castle） |
| `HashFunction` | 哈希函数接口 |
| `HmacFunction` | HMAC 函数接口 |
| **密码哈希** | |
| `Argon2Hash` | Argon2id/Argon2i/Argon2d 密码哈希 |
| `BCryptHash` | BCrypt 密码哈希 |
| `SCryptHash` | SCrypt 密码哈希 |
| `Pbkdf2Hash` | PBKDF2 密码哈希 |
| `PasswordHash` | 密码哈希接口 |
| `PasswordPolicy` | 密码强度策略 |
| **密钥派生** | |
| `Hkdf` | HKDF 密钥派生 |
| `Pbkdf2` | PBKDF2 密钥派生 |
| `Argon2Kdf` | Argon2 密钥派生（需要 Bouncy Castle） |
| `Scrypt` | Scrypt 密钥派生 |
| `KdfEngine` | 密钥派生引擎接口 |
| **密钥管理** | |
| `KeyGenerator` | 密码学密钥生成 |
| `KeyPairUtil` | 密钥对工具 |
| `KeyUtil` | 通用密钥工具 |
| `SecretKeyUtil` | 秘密密钥工具 |
| `SecureKeyStore` | 安全密钥库封装 |
| **密钥交换** | |
| `X25519Engine` | X25519 密钥交换 |
| `X448Engine` | X448 密钥交换 |
| `EcdhEngine` | ECDH 密钥交换 |
| `KeyExchangeEngine` | 密钥交换接口 |
| **MAC** | |
| `HmacSha256` | HMAC-SHA256 |
| `HmacSha512` | HMAC-SHA512 |
| `Poly1305` | Poly1305 MAC |
| `Mac` | MAC 接口 |
| **信封/混合加密** | |
| `EnvelopeCrypto` | 信封加密（非对称密钥包裹对称密钥） |
| `HybridCrypto` | 混合加密（RSA + AES） |
| `EncryptedEnvelope` | 加密信封记录 |
| **Sealed Box** | |
| `SealedBox` | Sealed Box 加密 |
| `SecretBox` | Secret Box 加密 |
| **JWT** | |
| `JwtUtil` | JWT 创建和验证工具 |
| `JwtClaims` | JWT 声明模型 |
| `JwtAlgorithm` | JWT 算法枚举 |
| **PGP** | |
| `PgpCipher` | PGP 加密/解密 |
| `PgpKeyUtil` | PGP 密钥生成和导入/导出 |
| `PgpKeyPair` | PGP 密钥对记录 |
| `PgpAlgorithm` | PGP 算法枚举 |
| **SSL/TLS** | |
| `SslContextBuilder` | 流式 SSL 上下文构建器 |
| `CertificatePinner` | 证书固定 |
| `TrustAllManager` | 信任所有管理器（仅测试用） |
| **密钥轮换** | |
| `KeyRotation` | 密钥轮换管理 |
| **随机数** | |
| `RandomBytes` | 安全随机字节生成 |
| `NonceGenerator` | Nonce 生成 |
| `SecureRandoms` | SecureRandom 实例管理 |
| **编解码** | |
| `Base64UrlCodec` | Base64URL 编解码 |
| `HexCodec` | Hex 编解码 |
| `PemCodec` | PEM 编解码 |
| **工具类** | |
| `ByteUtil` | 字节数组工具 |
| `ConstantTimeUtil` | 常量时间比较 |
| `CryptoDetector` | 加密提供者检测 |
| `CryptoUtil` | 通用加密工具 |
| `SecureEraser` | 安全内存擦除 |
| **枚举** | |
| `AsymmetricAlgorithm` | 非对称算法枚举 |
| `CurveType` | 椭圆曲线类型枚举 |
| `DigestAlgorithm` | 摘要算法枚举 |
| `PasswordHashAlgorithm` | 密码哈希算法枚举 |
| `SignatureAlgorithm` | 签名算法枚举 |
| `SymmetricAlgorithm` | 对称算法枚举 |
| **异常** | |
| `OpenCryptoException` | 通用加密异常 |
| `OpenKeyException` | 密钥相关异常 |
| `OpenSignatureException` | 签名相关异常 |

## 快速开始

```java
import cloud.opencode.base.crypto.OpenCrypto;

// AES-GCM 加密
var cipher = OpenCrypto.aesGcm();
byte[] key = cipher.generateKey();
byte[] encrypted = cipher.encrypt(plaintext, key);
byte[] decrypted = cipher.decrypt(encrypted, key);

// SHA-256 摘要
byte[] hash = OpenCrypto.sha256().digest("Hello".getBytes());

// Argon2id 密码哈希
var hasher = OpenCrypto.argon2();
String hashed = hasher.hash("myPassword");
boolean matches = hasher.verify("myPassword", hashed);

// Ed25519 签名
var signer = OpenCrypto.ed25519();
var keyPair = signer.generateKeyPair();
byte[] signature = signer.sign(data, keyPair.getPrivate());
boolean valid = signer.verify(data, signature, keyPair.getPublic());

// HMAC-SHA256
var mac = OpenCrypto.hmacSha256(secretKey);
byte[] tag = mac.compute(data);

// X25519 密钥交换
var kex = OpenCrypto.x25519();
var kp = kex.generateKeyPair();
byte[] sharedSecret = kex.exchange(kp.getPrivate(), otherPublicKey);
```

## 环境要求

- Java 25+
- Bouncy Castle（可选，用于 SM2/SM3/SM4、BLAKE、PGP）

## 开源许可

Apache License 2.0
