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
- 流式 AEAD 加密大文件（AES-GCM、ChaCha20-Poly1305）
- TOTP/HOTP 一次性密码（RFC 6238/4226）
- 加密算法策略管控（严格/标准/兼容）
- 版本化加密器，支持无缝算法迁移
- SecureBytes：支持 try-with-resources 的自动擦除安全字节容器
- 可选 Bouncy Castle 集成（SM2/SM3/SM4、BLAKE、PGP）

## Maven 依赖

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-crypto</artifactId>
    <version>1.0.3</version>
</dependency>
```

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
boolean sigValid = signer.verify(data, signature, keyPair.getPublic());

// HMAC-SHA256
var mac = OpenCrypto.hmacSha256(secretKey);
byte[] tag = mac.compute(data);

// X25519 密钥交换
var kex = OpenCrypto.x25519();
var kp = kex.generateKeyPair();
byte[] sharedSecret = kex.exchange(kp.getPrivate(), otherPublicKey);
```

## V1.0.3 新增功能

### TOTP/HOTP 一次性密码

符合 RFC 4226 (HOTP) 和 RFC 6238 (TOTP) 规范，兼容 Google Authenticator。

```java
import cloud.opencode.base.crypto.otp.Totp;
import cloud.opencode.base.crypto.otp.Hotp;
import cloud.opencode.base.crypto.otp.TotpSecret;

// 生成密钥
byte[] secret = TotpSecret.generate();              // 20 字节随机密钥
String base32 = TotpSecret.toBase32(secret);        // Base32 编码（用于二维码）
byte[] decoded = TotpSecret.fromBase32(base32);     // 解码

// TOTP — 生成和验证
Totp totp = Totp.sha1();                            // SHA-1, 30秒, 6位
String code = totp.generate(secret);                // 当前时间验证码
boolean valid = totp.verify(secret, code);          // 验证（窗口=1）
boolean ok = totp.verify(secret, code, 2);          // 自定义窗口

// TOTP — 自定义配置
Totp custom = Totp.builder()
    .algorithm("HmacSHA256")
    .period(60)
    .digits(8)
    .build();

// 生成 otpauth:// URI 用于二维码
String uri = Totp.generateUri("MyApp", "user@example.com", secret);

// HOTP — 基于计数器
Hotp hotp = Hotp.sha1();
String hotpCode = hotp.generate(secret, 42);        // 计数器 = 42
boolean hotpValid = hotp.verify(secret, 42, hotpCode, 5); // 前瞻窗口 = 5
```

| 类名 | 方法 | 说明 |
|------|------|------|
| `Totp` | `sha1()` / `sha256()` / `sha512()` | 使用预设算法创建 TOTP |
| `Totp` | `builder()` | 自定义算法、步长、位数 |
| `Totp` | `generate(secret)` | 生成当前时间的验证码 |
| `Totp` | `generate(secret, time)` | 生成指定时间的验证码 |
| `Totp` | `verify(secret, code)` | 验证（默认窗口 1） |
| `Totp` | `verify(secret, code, windowSize)` | 验证（自定义窗口） |
| `Totp` | `verify(secret, code, time, windowSize)` | 指定时间验证 |
| `Totp` | `generateUri(issuer, account, secret)` | 生成 otpauth:// URI |
| `Hotp` | `sha1()` / `sha256()` / `sha512()` | 使用预设算法创建 HOTP |
| `Hotp` | `generate(secret, counter)` | 生成 6 位 OTP |
| `Hotp` | `generate(secret, counter, digits)` | 生成指定位数 OTP (6-8) |
| `Hotp` | `verify(secret, counter, code, lookAhead)` | 带前瞻窗口验证 |
| `TotpSecret` | `generate()` / `generate(length)` | 生成随机密钥 |
| `TotpSecret` | `toBase32(data)` / `fromBase32(str)` | Base32 编解码 |

### 流式 AEAD 加密大文件

分段 AEAD 加密，每段独立认证，防重排和防截断保护。

```java
import cloud.opencode.base.crypto.streaming.StreamingAead;

byte[] key = new byte[32]; // AES-256 密钥
// ...填充密钥...

// AES-GCM 流式加密
try (StreamingAead aead = StreamingAead.aesGcm(key)
        .setSegmentSize(1024 * 1024)   // 1 MB 段大小（默认值）
        .setAad("context".getBytes())) {
    aead.encryptFile(Path.of("large.bin"), Path.of("large.enc"));
    aead.decryptFile(Path.of("large.enc"), Path.of("large.dec"));
}

// ChaCha20-Poly1305 流式加密
try (StreamingAead chacha = StreamingAead.chaCha20(key)) {
    chacha.encrypt(inputStream, outputStream);
}

// 流式 API
try (StreamingAead aead = StreamingAead.aesGcm(key)) {
    aead.encrypt(inputStream, encryptedOutputStream);
    aead.decrypt(encryptedInputStream, decryptedOutputStream);
}
```

| 类名 | 方法 | 说明 |
|------|------|------|
| `StreamingAead` | `aesGcm(key)` | 创建 AES-GCM 流式加密器（16/24/32 字节密钥） |
| `StreamingAead` | `chaCha20(key)` | 创建 ChaCha20-Poly1305 流式加密器（32 字节密钥） |
| `StreamingAead` | `setSegmentSize(bytes)` | 设置段大小（256B - 64MB，默认 1MB） |
| `StreamingAead` | `setAad(aad)` | 设置附加认证数据 |
| `StreamingAead` | `encrypt(in, out)` | 流式加密 |
| `StreamingAead` | `decrypt(in, out)` | 流式解密 |
| `StreamingAead` | `encryptFile(source, target)` | 加密文件 |
| `StreamingAead` | `decryptFile(source, target)` | 解密文件（原子写入，失败时不留部分明文） |
| `StreamingAead` | `close()` | 擦除内存中的密钥材料 |

### 版本化加密器

自描述加密载荷，支持零停机算法迁移。

```java
import cloud.opencode.base.crypto.versioned.VersionedCipher;

// 构建多版本加密器
VersionedCipher vc = VersionedCipher.builder()
    .addVersion(1, oldCipher)       // 旧版 AES-128-GCM
    .addVersion(2, newCipher)       // 当前 AES-256-GCM
    .currentVersion(2)              // 使用 v2 加密
    .build();

// 加密始终使用当前版本
byte[] encrypted = vc.encrypt(plaintext);

// 解密自动从载荷头部检测版本
byte[] decrypted = vc.decrypt(encrypted);

// Base64 便捷方法
String base64 = vc.encryptBase64("敏感数据");
String plain = vc.decryptBase64ToString(base64);
```

| 类名 | 方法 | 说明 |
|------|------|------|
| `VersionedCipher` | `builder()` | 创建构建器 |
| `VersionedCipher` | `encrypt(plaintext)` | 使用当前版本加密 |
| `VersionedCipher` | `decrypt(payload)` | 解密（自动检测版本） |
| `VersionedCipher` | `encryptBase64(plaintext)` / `encryptBase64(str)` | 加密为 Base64 |
| `VersionedCipher` | `decryptBase64(base64)` / `decryptBase64ToString(base64)` | 从 Base64 解密 |
| `VersionedPayload` | `serialize()` / `deserialize(data)` | 二进制序列化 |

### 加密策略管控

算法治理，内置预定义策略用于合规检查。

```java
import cloud.opencode.base.crypto.policy.CryptoPolicy;

// 预定义策略
CryptoPolicy strict = CryptoPolicy.strict();      // AES-256-GCM, Ed25519 等
CryptoPolicy standard = CryptoPolicy.standard();  // + AES-128-GCM, RSA-OAEP 等
CryptoPolicy legacy = CryptoPolicy.legacy();      // + SHA-1, MD5, 3DES

// 检查算法合规性
strict.check("AES-256-GCM", 256);                 // 通过
strict.isAllowed("MD5", 0);                        // false
// strict.check("MD5", 0);                         // 抛出 PolicyViolationException

// 自定义策略
CryptoPolicy custom = CryptoPolicy.builder()
    .allow("AES-256-GCM", "ChaCha20-Poly1305")
    .deny("DES", "RC4")
    .minKeyBits("RSA", 4096)
    .build();
```

| 类名 | 方法 | 说明 |
|------|------|------|
| `CryptoPolicy` | `strict()` / `standard()` / `legacy()` | 预定义策略 |
| `CryptoPolicy` | `builder()` | 自定义策略构建器 |
| `CryptoPolicy` | `check(algorithm, keyBits)` | 检查合规性（违规时抛异常） |
| `CryptoPolicy` | `isAllowed(algorithm, keyBits)` | 检查合规性（返回布尔值） |
| `CryptoPolicy` | `getAllowedAlgorithms()` | 获取允许的算法集合 |
| `CryptoPolicy` | `getDeniedAlgorithms()` | 获取拒绝的算法集合 |
| `CryptoPolicy` | `getMinKeyBits()` | 获取最小密钥长度要求 |

### SecureBytes 安全字节容器

用于敏感密钥材料的自动擦除安全字节容器。

```java
import cloud.opencode.base.crypto.util.SecureBytes;

// 防御性拷贝 — 调用者保留原始数组
try (SecureBytes key = SecureBytes.of(rawKeyBytes)) {
    byte[] copy = key.getBytes();       // 返回副本
    byte[] ref = key.getBytesUnsafe();  // 返回直接引用（热路径）
    int len = key.length();
} // 内部数据在此被清零

// 零拷贝 — 调用者转让所有权
try (SecureBytes key = SecureBytes.wrap(generateKey())) {
    doEncrypt(key.getBytesUnsafe());
} // 原始数组被清零
```

| 类名 | 方法 | 说明 |
|------|------|------|
| `SecureBytes` | `of(data)` | 创建（防御性拷贝） |
| `SecureBytes` | `wrap(data)` | 创建（零拷贝，转让所有权） |
| `SecureBytes` | `getBytes()` | 获取数据副本 |
| `SecureBytes` | `getBytesUnsafe()` | 获取直接引用（热路径，不要持有） |
| `SecureBytes` | `length()` | 获取字节长度 |
| `SecureBytes` | `isClosed()` | 检查是否已关闭 |
| `SecureBytes` | `close()` | 清零内部数据 |
| `SecureBytes` | `equals(other)` | 常量时间比较（已关闭时返回 false） |

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
| `SecureBytes` | 自动擦除的安全字节容器（AutoCloseable） |
| **流式 AEAD** | |
| `StreamingAead` | 大数据/文件分段 AEAD 加密 |
| **OTP（一次性密码）** | |
| `Totp` | RFC 6238 基于时间的一次性密码生成和验证 |
| `Hotp` | RFC 4226 基于 HMAC 的一次性密码生成和验证 |
| `TotpSecret` | OTP 密钥生成和 Base32 编解码 |
| **加密策略** | |
| `CryptoPolicy` | 算法白名单/黑名单策略管控 |
| `PolicyViolationException` | 策略违规异常 |
| **版本化加密** | |
| `VersionedCipher` | 多版本加密器，支持算法迁移 |
| `VersionedPayload` | 带版本元数据的加密载荷 |
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

## 环境要求

- Java 25+
- Bouncy Castle（可选，用于 SM2/SM3/SM4、BLAKE、PGP）

## 开源许可

Apache License 2.0
