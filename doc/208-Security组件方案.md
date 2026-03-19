# Security 组件方案

## 1. 组件概述

`opencode-base-security` 是企业级安全能力的策略与集成层，提供安全上下文管理、密钥全生命周期管理、云 KMS 集成、TLS/PKI 支持、密码安全、审计监控等能力。基于 JDK 25，使用 ScopedValue 实现安全上下文传播。

**核心设计理念：** Crypto 负责「算得对、算得快」，Security 负责「谁能算、算什么、怎么管、怎么审计」。

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-security</artifactId>
    <version>${version}</version>
</dependency>
```

## 2. 包结构

```
cloud.opencode.base.security
├── SecurityContext.java                 # 安全上下文（ScopedValue）
├── api/                                 # 统一安全 API
│   ├── OpenCrypto.java                 # 统一加密门面
│   ├── AlgorithmSelector.java          # 算法选择器
│   └── AlgorithmSuite.java            # 算法套件（Record）
├── key/                                 # 密钥生命周期管理
│   ├── KeyManager.java                 # 密钥管理主入口
│   ├── KeyGenerator.java              # 密钥生成
│   ├── KeyStore.java                   # 密钥存储接口
│   ├── KeyRotation.java               # 密钥轮换策略
│   ├── KeyVersion.java                 # 密钥版本
│   ├── KeyVersionManager.java          # 版本管理
│   ├── Keyset.java                     # 密钥集合
│   ├── KeyBackup.java                  # 备份恢复
│   ├── KeyWrapper.java                 # 密钥包装
│   ├── KeyProvider.java               # 密钥提供者接口
│   ├── LocalKeyProvider.java          # 本地密钥提供者
│   ├── AwsKmsKeyProvider.java         # AWS KMS 提供者
│   ├── AzureKeyVaultProvider.java     # Azure Key Vault 提供者
│   ├── GcpCloudKmsProvider.java       # GCP KMS 提供者
│   ├── HashiCorpVaultProvider.java    # HashiCorp Vault 提供者
│   └── Pkcs11HsmProvider.java         # PKCS#11 HSM 提供者
├── tls/                                 # TLS/PKI 支持
│   ├── OpenTls.java                    # TLS 门面
│   ├── TlsServerBuilder.java          # 服务端 TLS 构建器
│   ├── TlsClientBuilder.java          # 客户端 TLS 构建器
│   ├── TlsCertificates.java           # 证书工具
│   └── SelfSignedCert.java            # 自签名证书
├── certificate/                         # 证书管理
│   ├── CertificateManager.java         # 证书管理器
│   ├── CertificateValidator.java       # 证书验证器
│   └── CertificateChainValidator.java  # 证书链验证器
├── password/                            # 密码安全
│   ├── OpenPassword.java               # 密码工具
│   ├── StrengthEvaluator.java          # 强度评估器
│   ├── PasswordStrength.java           # 强度等级枚举
│   ├── SecureStorage.java              # 安全存储
│   ├── LeakDetection.java             # 泄露检测
│   └── MfaManager.java                # MFA 管理
├── kms/                                 # 云 KMS 适配
│   ├── KmsAdapter.java                 # KMS 统一接口
│   ├── AwsKmsAdapter.java             # AWS KMS 适配器
│   ├── AzureKeyVaultAdapter.java      # Azure 适配器
│   └── GcpKmsAdapter.java             # GCP 适配器
├── monitor/                             # 审计与监控
│   ├── CryptoAuditor.java             # 操作审计
│   ├── CryptoAuditLogger.java         # 审计日志
│   └── PerformanceMonitor.java        # 性能监控
├── config/                              # 安全配置
│   ├── SecurityConfig.java             # 全局安全配置
│   ├── AlgorithmConfig.java           # 算法配置
│   ├── KeyConfig.java                 # 密钥配置
│   └── PolicyConfig.java             # 策略配置
├── enums/                               # 安全枚举
│   ├── SecurityLevel.java             # 安全等级
│   ├── Scenario.java                  # 业务场景
│   └── Platform.java                  # 平台类型
└── exception/                           # 异常体系
    ├── OpenSecurityException.java      # 安全基础异常
    ├── OpenKeyManagementException.java # 密钥管理异常
    ├── OpenTlsException.java          # TLS 异常
    ├── OpenPasswordPolicyException.java # 密码策略异常
    └── OpenSecurityConfigException.java # 配置异常
```

## 3. 核心 API

### 3.1 SecurityContext

> 安全上下文，基于 JDK 25 ScopedValue 实现线程安全的安全配置传播。提供安全级别、主体身份、密钥管理器、加密器、审计器等安全组件的绑定与获取。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static Optional<SecurityContext> current()` | 获取当前绑定的安全上下文 |
| `static SecurityContext require()` | 获取当前上下文，未绑定则抛异常 |
| `static boolean isBound()` | 检查是否已绑定安全上下文 |
| `static Builder builder()` | 创建 Builder |
| `static SecurityContext createDefault()` | 创建默认安全上下文 |
| `static SecurityContext withLevel(SecurityLevel level)` | 以指定安全级别创建上下文 |
| `<T, X extends Throwable> T call(ScopedValue.CallableOp<? extends T, X> callable)` | 在此上下文中执行有返回值的操作 |
| `void run(Runnable runnable)` | 在此上下文中执行操作 |
| `SecurityConfig config()` | 获取安全配置 |
| `Optional<KeyManager> keyManager()` | 获取密钥管理器 |
| `Optional<OpenCrypto> crypto()` | 获取加密门面 |
| `Optional<AlgorithmSelector> algorithmSelector()` | 获取算法选择器 |
| `Optional<CryptoAuditor> auditor()` | 获取审计器 |
| `Optional<PerformanceMonitor> performanceMonitor()` | 获取性能监控器 |
| `Optional<String> principal()` | 获取当前主体身份 |
| `SecurityLevel securityLevel()` | 获取安全级别 |
| `AlgorithmSuite algorithmSuite()` | 获取算法套件 |
| `SecurityContext withPrincipal(String principal)` | 创建指定主体的新上下文 |
| `SecurityContext withSecurityLevel(SecurityLevel level)` | 创建指定安全级别的新上下文 |

**示例：**

```java
// 创建安全上下文
SecurityContext ctx = SecurityContext.builder()
    .config(SecurityConfig.defaults())
    .principal("admin")
    .build();

// 在上下文中执行加密操作
String result = ctx.call(() -> {
    SecurityContext current = SecurityContext.require();
    return "Security level: " + current.securityLevel();
});

// 快速创建指定级别的上下文
SecurityContext highCtx = SecurityContext.withLevel(SecurityLevel.HIGH);
highCtx.run(() -> {
    // 在 HIGH 安全级别下执行操作
});
```

### 3.2 OpenCrypto

> 统一加密操作门面，提供对称加密、非对称加密、数字签名、哈希等操作的静态方法。通过密钥别名（alias）从 KeyManager 自动获取密钥，自动审计操作记录。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static byte[] encrypt(String keyAlias, byte[] plaintext)` | 对称加密（返回字节数组） |
| `static String encryptToBase64(String keyAlias, String plaintext)` | 对称加密（返回 Base64） |
| `static String encryptToHex(String keyAlias, byte[] plaintext)` | 对称加密（返回 Hex） |
| `static byte[] decrypt(String keyAlias, byte[] ciphertext)` | 对称解密 |
| `static String decryptFromBase64(String keyAlias, String ciphertext)` | 从 Base64 解密 |
| `static byte[] decryptFromHex(String keyAlias, String ciphertext)` | 从 Hex 解密 |
| `static byte[] encryptWithPublicKey(String keyAlias, byte[] plaintext)` | 使用公钥加密 |
| `static byte[] decryptWithPrivateKey(String keyAlias, byte[] ciphertext)` | 使用私钥解密 |
| `static byte[] sign(String keyAlias, byte[] data)` | 数字签名 |
| `static String signToBase64(String keyAlias, byte[] data)` | 签名（返回 Base64） |
| `static boolean verify(String keyAlias, byte[] data, byte[] signatureBytes)` | 验签 |
| `static boolean verifyFromBase64(String keyAlias, byte[] data, String signature)` | 从 Base64 验签 |
| `static byte[] hash(byte[] data)` | 计算哈希 |
| `static String hashToHex(byte[] data)` | 计算哈希（返回 Hex） |
| `static KeyManager getKeyManager()` | 获取当前密钥管理器 |
| `static void setKeyManager(KeyManager keyManager)` | 设置密钥管理器 |

**示例：**

```java
// 加密/解密
String encrypted = OpenCrypto.encryptToBase64("master-key", "Hello World");
String decrypted = OpenCrypto.decryptFromBase64("master-key", encrypted);

// 签名/验签
String signature = OpenCrypto.signToBase64("signingKey", data);
boolean valid = OpenCrypto.verifyFromBase64("signingKey", data, signature);

// 哈希
String hex = OpenCrypto.hashToHex("sensitive data".getBytes());
```

### 3.3 AlgorithmSelector

> 算法选择器，根据安全上下文、安全级别或业务场景自动选择合适的算法套件。支持注册自定义选择策略。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static AlgorithmSuite select(SecurityContext context)` | 根据上下文选择算法套件 |
| `static AlgorithmSuite select(SecurityLevel level)` | 根据安全级别选择 |
| `static AlgorithmSuite selectForScenario(Scenario scenario)` | 根据场景选择 |
| `static void registerStrategy(SelectionStrategy strategy)` | 注册自定义选择策略 |
| `static void clearStrategies()` | 清除所有自定义策略 |
| `static int strategyCount()` | 获取已注册策略数量 |

**示例：**

```java
// 根据安全级别选择
AlgorithmSuite suite = AlgorithmSelector.select(SecurityLevel.HIGH);

// 根据业务场景选择
AlgorithmSuite mobileSuite = AlgorithmSelector.selectForScenario(Scenario.MOBILE);

// 注册自定义策略
AlgorithmSelector.registerStrategy(ctx -> {
    if (ctx.securityLevel() == SecurityLevel.MAXIMUM) {
        return Optional.of(AlgorithmSuite.POST_QUANTUM);
    }
    return Optional.empty();
});
```

### 3.4 AlgorithmSuite

> 算法套件 Record，定义一组相关的加密算法配置。提供预定义套件和自定义 Builder。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static final AlgorithmSuite DEFAULT` | 默认套件：AES-256-GCM, RSA-2048, SHA-256 |
| `static final AlgorithmSuite HIGH_SECURITY` | 高安全套件：AES-256-GCM, RSA-4096, SHA-384 |
| `static final AlgorithmSuite COMPATIBLE` | 兼容套件：AES-128-CBC, RSA-2048, SHA-256 |
| `static final AlgorithmSuite POST_QUANTUM` | 后量子套件（实验性） |
| `static Builder builder()` | 创建自定义套件 Builder |
| `static AlgorithmSuite byName(String name)` | 根据名称查找套件 |
| `String symmetricTransformation()` | 获取对称加密变换（如 AES/GCM/NoPadding） |
| `boolean isAuthenticated()` | 是否为认证加密（GCM） |

**示例：**

```java
// 使用预定义套件
AlgorithmSuite suite = AlgorithmSuite.HIGH_SECURITY;

// 自定义套件
AlgorithmSuite custom = AlgorithmSuite.builder()
    .name("custom")
    .symmetricAlgorithm("AES")
    .symmetricKeySize(256)
    .asymmetricAlgorithm("RSA")
    .asymmetricKeySize(4096)
    .signatureAlgorithm("SHA384withRSA")
    .hashAlgorithm("SHA-384")
    .build();
```

### 3.5 KeyManager

> 密钥管理器，提供密钥的创建、轮换、版本管理、备份、导入导出等完整生命周期管理。实现 AutoCloseable 接口。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static KeyManager create(KeyStore keyStore)` | 创建密钥管理器 |
| `static KeyManager create(KeyStore keyStore, KeyConfig config)` | 使用指定配置创建 |
| `Keyset createKeyset(String alias, AlgorithmSuite suite, KeyConfig config)` | 创建密钥集 |
| `Optional<Keyset> getKeyset(String alias)` | 获取密钥集 |
| `void deleteKeyset(String alias)` | 删除密钥集 |
| `List<String> listKeysets()` | 列出所有密钥集 |
| `KeyVersion rotateKey(String alias)` | 轮换密钥（创建新版本） |
| `KeyVersion getActiveVersion(String alias)` | 获取当前活跃版本 |
| `Optional<KeyVersion> getVersion(String alias, int version)` | 获取指定版本 |
| `void disableVersion(String alias, int version)` | 禁用版本 |
| `void destroyVersion(String alias, int version)` | 销毁版本（不可恢复） |
| `void scheduleRotation(String alias, Duration interval)` | 配置自动轮换 |
| `void cancelRotation(String alias)` | 取消自动轮换 |
| `Optional<Instant> getNextRotationTime(String alias)` | 获取下次轮换时间 |
| `Optional<SecretKey> getSecretKey(String alias)` | 获取对称密钥 |
| `Optional<PublicKey> getPublicKey(String alias)` | 获取公钥 |
| `Optional<PrivateKey> getPrivateKey(String alias)` | 获取私钥 |
| `KeyPair generateKeyPair(String alias, AlgorithmSuite suite)` | 生成密钥对 |
| `byte[] exportKey(String alias, String wrappingKeyAlias)` | 导出密钥（加密） |
| `void importKey(String alias, byte[] wrappedKey, String wrappingKeyAlias)` | 导入密钥 |
| `SecretKey deriveKey(String masterAlias, String context, int keyLength)` | 派生子密钥 |
| `void shutdown()` | 关闭调度器 |
| `void close()` | 关闭（AutoCloseable） |

**示例：**

```java
// 创建密钥管理器
KeyManager keyManager = KeyManager.create(KeyStore.memory());

// 创建密钥集
Keyset keyset = keyManager.createKeyset("app-key",
    AlgorithmSuite.DEFAULT, KeyConfig.defaults());

// 轮换密钥
KeyVersion newVersion = keyManager.rotateKey("app-key");

// 配置自动轮换
keyManager.scheduleRotation("app-key", Duration.ofDays(90));

// 密钥派生
SecretKey derived = keyManager.deriveKey("master", "user-context", 256);
```

### 3.6 Keyset

> 密钥集合，包含多个版本的密钥，支持活跃版本查询、轮换检测等。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `String alias()` | 密钥别名 |
| `AlgorithmSuite algorithmSuite()` | 算法套件 |
| `List<KeyVersion> versions()` | 所有版本（不可变列表） |
| `int activeVersion()` | 当前活跃版本号 |
| `Instant createdAt()` | 创建时间 |
| `Instant lastRotatedAt()` | 最后轮换时间 |
| `KeyConfig config()` | 密钥配置 |
| `KeyVersion getActive()` | 获取当前活跃密钥版本 |
| `List<KeyVersion> getEnabledVersions()` | 获取所有启用的版本 |
| `boolean needsRotation()` | 是否需要轮换 |
| `int versionCount()` | 版本数量 |

**示例：**

```java
Keyset keyset = keyManager.getKeyset("app-key").orElseThrow();
KeyVersion active = keyset.getActive();
boolean rotate = keyset.needsRotation();
```

### 3.7 KeyVersion

> 密钥版本，表示密钥集中的单个密钥版本，包含版本号、状态、有效期等信息。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `int version()` | 版本号 |
| `KeyState state()` | 密钥状态（ENABLED/DISABLED/DESTROYED） |
| `Instant createdAt()` | 创建时间 |
| `Instant expiresAt()` | 过期时间 |
| `String algorithm()` | 算法名称 |
| `int keySize()` | 密钥长度 |
| `boolean canEncrypt()` | 是否可用于加密 |
| `boolean canDecrypt()` | 是否可用于解密 |
| `boolean isExpired()` | 是否已过期 |

### 3.8 KeyStore

> 密钥存储抽象接口，定义密钥的存储、加载、删除操作。提供内存、文件、JKS 三种内置实现。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `void store(String alias, SecretKey key, KeyVersion version)` | 存储密钥 |
| `Optional<SecretKey> load(String alias, int version)` | 加载密钥 |
| `void delete(String alias, int version)` | 删除密钥 |
| `boolean exists(String alias, int version)` | 检查是否存在 |
| `Set<String> listAliases()` | 列出所有别名 |

### 3.9 KeyGenerator

> 密钥生成工具，提供对称密钥、非对称密钥对、随机字节的生成。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static SecretKey generateSymmetricKey()` | 生成默认对称密钥 |
| `static SecretKey generateSymmetricKey(AlgorithmSuite suite)` | 按套件生成对称密钥 |
| `static KeyPair generateKeyPair()` | 生成默认密钥对 |
| `static KeyPair generateKeyPair(AlgorithmSuite suite)` | 按套件生成密钥对 |
| `static byte[] generateRandomBytes(int length)` | 生成随机字节 |

### 3.10 KeyRotation

> 密钥轮换策略 Record，定义轮换间隔、策略类型、自动轮换开关、宽限期等。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static KeyRotation defaults()` | 默认轮换策略 |
| `static KeyRotation timeBased(Duration interval)` | 基于时间的轮换策略 |
| `boolean needsRotation(Instant lastRotation)` | 是否需要轮换 |
| `Instant nextRotationTime(Instant lastRotation)` | 下次轮换时间 |
| `static Builder builder()` | 创建 Builder |

### 3.11 KeyVersionManager

> 密钥版本管理器，提供多版本密钥的查询和清理。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static Optional<KeyVersion> getActiveVersion(List<KeyVersion> versions, int activeVersionNum)` | 获取活跃版本 |
| `static List<KeyVersion> getEnabledVersions(List<KeyVersion> versions)` | 获取所有启用版本 |
| `static List<KeyVersion> getDecryptableVersions(List<KeyVersion> versions)` | 获取所有可解密版本 |
| `static int getLatestVersionNumber(List<KeyVersion> versions)` | 获取最新版本号 |
| `static long countExpiredVersions(List<KeyVersion> versions)` | 统计已过期版本数 |
| `static List<KeyVersion> findVersionsToCleanup(List<KeyVersion> versions, int maxVersions)` | 查找需要清理的版本 |

### 3.12 KeyBackup

> 密钥备份恢复工具，支持密码保护的备份和恢复。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static byte[] backup(KeyManager keyManager, char[] password)` | 创建备份 |
| `static void restore(KeyManager keyManager, byte[] backup, char[] password)` | 恢复备份 |
| `static byte[] exportKeyset(Keyset keyset, char[] password)` | 导出单个密钥集 |
| `static Keyset importKeyset(byte[] data, char[] password)` | 导入密钥集 |

### 3.13 KeyWrapper

> 密钥包装/解包工具，支持密钥加密保护和密码派生密钥包装。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static byte[] wrap(SecretKey keyToWrap, SecretKey wrappingKey)` | 使用密钥包装 |
| `static SecretKey unwrap(byte[] wrappedKey, SecretKey wrappingKey, String algorithm)` | 解包密钥 |
| `static byte[] wrapWithPassword(SecretKey key, char[] password)` | 使用密码包装（PBKDF2 派生） |
| `static SecretKey unwrapWithPassword(byte[] wrappedKeyWithSalt, char[] password, String algorithm)` | 使用密码解包 |

### 3.14 KeyProvider

> 外部密钥管理系统的抽象接口，支持 HSM 和云 KMS 集成。定义了密钥生成、加密、签名、管理等操作。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `String generateKey(String alias, KeySpec spec)` | 生成密钥 |
| `String generateKeyPair(String alias, KeySpec spec)` | 生成密钥对 |
| `String importKey(String alias, byte[] keyMaterial, KeySpec spec)` | 导入密钥 |
| `byte[] encrypt(String keyId, byte[] plaintext)` | 加密 |
| `byte[] decrypt(String keyId, byte[] ciphertext)` | 解密 |
| `byte[] wrapKey(String wrappingKeyId, SecretKey keyToWrap)` | 包装密钥 |
| `SecretKey unwrapKey(String wrappingKeyId, byte[] wrappedKey, String algorithm)` | 解包密钥 |
| `DataKeyPair generateDataKey(String keyId, KeySpec spec)` | 生成数据密钥对 |
| `byte[] sign(String keyId, byte[] message, SigningAlgorithm algorithm)` | 签名 |
| `boolean verify(String keyId, byte[] message, byte[] signature, SigningAlgorithm algorithm)` | 验签 |
| `Optional<KeyMetadata> getKeyMetadata(String keyId)` | 获取密钥元数据 |
| `Set<String> listKeys()` | 列出所有密钥 |
| `void enableKey(String keyId)` | 启用密钥 |
| `void disableKey(String keyId)` | 禁用密钥 |
| `void rotateKey(String keyId)` | 轮换密钥 |
| `String providerName()` | 提供者名称 |
| `boolean isAvailable()` | 是否可用 |

### 3.15 OpenTls

> TLS 配置和工具门面，提供服务端/客户端 TLS 构建器和自签名证书生成。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static TlsServerBuilder server()` | 创建服务端 TLS 构建器 |
| `static TlsClientBuilder client()` | 创建客户端 TLS 构建器 |
| `static SelfSignedCert generateSelfSigned(String cn, Duration validity)` | 生成自签名证书 |
| `static SSLContext getDefaultContext()` | 获取默认 SSLContext |
| `static String tlsV13()` | 返回 TLSv1.3 协议名 |
| `static String tlsV12()` | 返回 TLSv1.2 协议名 |

**示例：**

```java
// 服务端 TLS
SSLContext serverCtx = OpenTls.server()
    .withKeyStore(Path.of("server.p12"), "pass".toCharArray())
    .withMutualTls(true)
    .withTrustStore(Path.of("trust.p12"), "pass".toCharArray())
    .build();

// 客户端 TLS
SSLContext clientCtx = OpenTls.client()
    .withTrustStore(Path.of("trust.p12"), "pass".toCharArray())
    .withHostnameVerification(true)
    .build();
```

### 3.16 TlsServerBuilder

> TLS 服务端构建器，支持密钥库、信任库、mTLS、客户端认证模式、协议版本、密码套件、会话超时等配置。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static TlsServerBuilder create()` | 创建构建器 |
| `TlsServerBuilder withKeyStore(Path keyStore, char[] password)` | 设置密钥库 |
| `TlsServerBuilder withTrustStore(Path trustStore, char[] password)` | 设置信任库 |
| `TlsServerBuilder withMutualTls(boolean enabled)` | 启用双向 TLS |
| `TlsServerBuilder withClientAuth(ClientAuth clientAuth)` | 设置客户端认证模式 |
| `TlsServerBuilder withProtocols(String... protocols)` | 设置协议版本 |
| `TlsServerBuilder withCipherSuites(String... cipherSuites)` | 设置密码套件 |
| `TlsServerBuilder withSessionTimeout(Duration timeout)` | 设置会话超时 |
| `TlsServerBuilder withAlgorithmSuite(AlgorithmSuite suite)` | 设置算法套件 |
| `SSLContext build()` | 构建 SSLContext |
| `SSLServerSocketFactory buildServerSocketFactory()` | 构建 SSLServerSocketFactory |

### 3.17 TlsClientBuilder

> TLS 客户端构建器，支持信任库、客户端证书（mTLS）、主机名验证、SNI 等配置。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static TlsClientBuilder create()` | 创建构建器 |
| `TlsClientBuilder withTrustStore(Path trustStore, char[] password)` | 设置信任库 |
| `TlsClientBuilder trustAll()` | 信任所有证书（仅测试） |
| `TlsClientBuilder trustAllDangerouslyForTestingOnly()` | 信任所有（显式标记仅测试） |
| `TlsClientBuilder withClientCertificate(Path keyStore, char[] password)` | 设置客户端证书 |
| `TlsClientBuilder withHostnameVerification(boolean enabled)` | 启用主机名验证 |
| `TlsClientBuilder withProtocols(String... protocols)` | 设置协议版本 |
| `TlsClientBuilder withSni(String hostname)` | 设置 SNI |
| `SSLContext build()` | 构建 SSLContext |
| `SSLSocketFactory buildSocketFactory()` | 构建 SSLSocketFactory |

### 3.18 TlsCertificates

> TLS 证书工具，支持自签名证书生成、PEM 格式证书和私钥的加载/导出、PKCS12 密钥库创建。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static SelfSignedCert generateSelfSigned(String cn, Duration validity)` | 生成自签名证书 |
| `static X509Certificate loadFromPem(Path pemFile)` | 从 PEM 文件加载证书 |
| `static X509Certificate loadFromPem(String pemContent)` | 从 PEM 字符串加载证书 |
| `static PrivateKey loadPrivateKeyFromPem(Path pemFile)` | 从 PEM 加载私钥 |
| `static PrivateKey loadPrivateKeyFromPem(String pemContent, char[] password)` | 从 PEM 加载加密私钥 |
| `static String toPem(X509Certificate certificate)` | 证书导出为 PEM |
| `static String toPem(PrivateKey privateKey)` | 私钥导出为 PEM |
| `static byte[] createPkcs12(X509Certificate cert, PrivateKey key, char[] password)` | 创建 PKCS12 |

### 3.19 SelfSignedCert

> 自签名证书持有类，包含证书、私钥和公钥，支持导出为 KeyStore、PEM 文件或 PKCS12。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `X509Certificate certificate()` | 获取证书 |
| `PrivateKey privateKey()` | 获取私钥 |
| `PublicKey publicKey()` | 获取公钥 |
| `KeyStore toKeyStore(String alias, char[] password)` | 导出为 KeyStore |
| `void saveToPem(Path certFile, Path keyFile)` | 保存为 PEM 文件 |
| `void saveToPkcs12(Path p12File, char[] password)` | 保存为 PKCS12 |

### 3.20 CertificateManager

> 证书管理器，管理 X.509 证书的添加、查询、删除和过期检测。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static CertificateManager create(Path certificateStore)` | 创建证书管理器 |
| `void addCertificate(String alias, X509Certificate certificate)` | 添加证书 |
| `Optional<X509Certificate> getCertificate(String alias)` | 获取证书 |
| `void removeCertificate(String alias)` | 删除证书 |
| `Map<String, X509Certificate> listCertificates()` | 列出所有证书 |
| `List<X509Certificate> getExpiringCertificates(Duration within)` | 获取即将过期的证书 |
| `CertificateStatus checkStatus(X509Certificate certificate)` | 检查证书状态 |

### 3.21 CertificateValidator

> 证书验证器，支持 X.509 证书验证、证书链验证、主机名验证、OCSP 和 CRL 检查。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static CertificateValidator create()` | 创建验证器 |
| `static CertificateValidator create(KeyStore trustStore)` | 使用信任库创建 |
| `ValidationResult validate(X509Certificate certificate)` | 验证证书 |
| `ValidationResult validateChain(X509Certificate[] chain)` | 验证证书链 |
| `boolean verifyHostname(X509Certificate certificate, String hostname)` | 验证主机名 |
| `CertificateValidator addTrustAnchor(X509Certificate anchor)` | 添加信任锚 |
| `CertificateValidator enableOcsp(boolean enabled)` | 启用 OCSP |
| `CertificateValidator enableCrl(boolean enabled)` | 启用 CRL |

### 3.22 CertificateChainValidator

> 证书链验证工具，提供链验证、链构建、自签名检测等静态方法。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static ValidationResult validate(X509Certificate[] chain)` | 验证证书链 |
| `static ValidationResult validate(X509Certificate[] chain, Set<TrustAnchor> anchors)` | 使用信任锚验证 |
| `static List<X509Certificate> buildChain(X509Certificate endEntity, Set<X509Certificate> intermediates)` | 构建证书链 |
| `static boolean isSelfSigned(X509Certificate cert)` | 是否自签名 |
| `static int getChainDepth(X509Certificate[] chain)` | 获取链深度 |

### 3.23 OpenPassword

> 密码工具类，提供密码哈希、验证、生成、强度评估等功能。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static String hash(String password)` | 哈希密码（默认算法） |
| `static String hash(String password, int iterations)` | 指定迭代次数哈希 |
| `static boolean verify(String password, String hashedPassword)` | 验证密码 |
| `static String generate(int length)` | 生成随机密码 |
| `static String generate(int length, boolean lower, boolean upper, boolean digits, boolean special)` | 按规则生成密码 |
| `static String generatePassphrase(int wordCount, String separator)` | 生成助记词密码 |
| `static StrengthEvaluator.EvaluationResult evaluateStrength(String password)` | 评估密码强度 |
| `static boolean needsRehash(String hashedPassword, int currentIterations)` | 是否需要重新哈希 |

**示例：**

```java
// 哈希与验证
String hash = OpenPassword.hash("MyP@ssw0rd!");
boolean valid = OpenPassword.verify("MyP@ssw0rd!", hash);

// 生成密码
String password = OpenPassword.generate(16, true, true, true, true);

// 评估强度
var result = OpenPassword.evaluateStrength("weakpass");
```

### 3.24 StrengthEvaluator

> 密码强度评估器，支持最小/最大长度、字符类型要求、常见密码检测等规则配置。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static StrengthEvaluator create()` | 创建默认评估器 |
| `static StrengthEvaluator strict()` | 创建严格评估器 |
| `StrengthEvaluator minLength(int length)` | 设置最小长度 |
| `StrengthEvaluator maxLength(int length)` | 设置最大长度 |
| `StrengthEvaluator requireUppercase(boolean require)` | 要求大写字母 |
| `StrengthEvaluator requireLowercase(boolean require)` | 要求小写字母 |
| `StrengthEvaluator requireDigit(boolean require)` | 要求数字 |
| `StrengthEvaluator requireSpecial(boolean require)` | 要求特殊字符 |
| `StrengthEvaluator commonPasswords(List<String> passwords)` | 设置常见密码列表 |
| `EvaluationResult evaluate(String password)` | 评估密码强度 |
| `boolean isAcceptable(String password, PasswordStrength minimum)` | 是否达到最低要求 |

### 3.25 PasswordStrength

> 密码强度等级枚举，定义 VERY_WEAK 到 VERY_STRONG 五个级别。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `int minScore()` | 最低分数 |
| `int maxScore()` | 最高分数 |
| `String description()` | 描述信息 |
| `static PasswordStrength fromScore(int score)` | 根据分数获取等级 |
| `boolean meetsMinimum(PasswordStrength minimum)` | 是否满足最低要求 |

### 3.26 LeakDetection

> 密码泄露检测服务，支持基于 k-匿名的在线检测和离线黑名单。实现 AutoCloseable。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static LeakDetection create()` | 创建检测器（在线） |
| `static LeakDetection create(Duration timeout)` | 创建检测器（指定超时） |
| `static LeakDetection offline()` | 创建离线检测器 |
| `LeakResult check(String password)` | 检查密码是否泄露 |
| `CompletableFuture<LeakResult> checkAsync(String password)` | 异步检查 |
| `void addToBlacklist(String password)` | 添加到黑名单 |
| `void addHashToBlacklist(String sha1Hash)` | 添加哈希到黑名单 |
| `void clearBlacklist()` | 清除黑名单 |
| `int blacklistSize()` | 黑名单大小 |
| `void setOfflineMode(boolean offline)` | 设置离线模式 |

### 3.27 MfaManager

> 多因素认证管理器，支持 TOTP 密钥生成、验证码生成/验证、备份码生成。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static MfaManager create()` | 创建 MFA 管理器 |
| `MfaSecret generateSecret(String issuer, String accountName)` | 生成 TOTP 密钥 |
| `String generateCode(String secret)` | 生成当前验证码 |
| `String generateCode(String secret, Instant instant)` | 生成指定时间的验证码 |
| `VerificationResult verify(String secret, String code)` | 验证 TOTP 码 |
| `VerificationResult verify(String secret, String code, boolean preventReplay)` | 验证（防重放） |
| `int getRemainingSeconds()` | 获取当前码剩余有效秒数 |
| `String[] generateBackupCodes(int count)` | 生成备份码 |

**示例：**

```java
MfaManager mfa = MfaManager.create();
MfaManager.MfaSecret secret = mfa.generateSecret("MyApp", "user@example.com");
String code = mfa.generateCode(secret.secret());
MfaManager.VerificationResult result = mfa.verify(secret.secret(), userInputCode);
```

### 3.28 SecureStorage

> 加密凭据存储，支持基于密码或密钥的加密存储，条目可设置过期时间和元数据。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static SecureStorage create(char[] password)` | 使用密码创建存储 |
| `static SecureStorage create(SecretKey key)` | 使用密钥创建存储 |
| `void store(String key, String value)` | 存储凭据 |
| `void store(String key, String value, Instant expiresAt)` | 存储凭据（带过期时间） |
| `void store(String key, String value, Instant expiresAt, Map<String, String> metadata)` | 存储凭据（带元数据） |
| `Optional<String> retrieve(String key)` | 获取凭据 |
| `Optional<StoredEntry> retrieveEntry(String key)` | 获取完整条目 |
| `boolean remove(String key)` | 删除条目 |
| `boolean exists(String key)` | 检查是否存在 |
| `void clear()` | 清除所有 |
| `int cleanupExpired()` | 清理过期条目 |
| `int size()` | 条目数量 |

### 3.29 KmsAdapter

> KMS 统一适配器接口，提供云密钥管理服务的统一抽象，支持 AWS/Azure/GCP。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `Platform platform()` | 平台类型 |
| `KeyMetadata createKey(String keySpec, String description, Map<String, String> tags)` | 创建密钥 |
| `CompletableFuture<KeyMetadata> createKeyAsync(...)` | 异步创建密钥 |
| `Optional<KeyMetadata> getKey(String keyId)` | 获取密钥信息 |
| `List<KeyMetadata> listKeys()` | 列出所有密钥 |
| `EncryptResult encrypt(String keyId, byte[] plaintext)` | 加密 |
| `DecryptResult decrypt(String keyId, byte[] ciphertext)` | 解密 |
| `DataKeyResult generateDataKey(String keyId, String keySpec)` | 生成数据密钥 |
| `byte[] sign(String keyId, byte[] message, String algorithm)` | 签名 |
| `boolean verify(String keyId, byte[] message, byte[] signature, String algorithm)` | 验签 |
| `void rotateKey(String keyId)` | 轮换密钥 |
| `boolean isHealthy()` | 健康检查 |

### 3.30 CryptoAuditor

> 加密操作审计器，记录和查询所有安全操作事件，支持事件过滤和统计。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static CryptoAuditor create()` | 创建审计器 |
| `static CryptoAuditor create(CryptoAuditLogger logger)` | 使用指定日志器创建 |
| `static CryptoAuditor create(CryptoAuditLogger logger, int maxEvents, Duration retentionPeriod)` | 指定容量和保留期 |
| `void record(AuditEvent event)` | 记录事件 |
| `void recordKeyOperation(EventType type, String keyId, String principal, boolean success)` | 记录密钥操作 |
| `void recordCryptoOperation(EventType type, String operation, boolean success)` | 记录加密操作 |
| `List<AuditEvent> query(AuditQuery query)` | 查询审计事件 |
| `List<AuditEvent> getRecentEvents(int count)` | 获取最近事件 |
| `List<AuditEvent> getFailedEvents(int count)` | 获取失败事件 |
| `AuditStats getStatistics()` | 获取统计信息 |
| `void clear()` | 清空事件 |
| `int cleanupExpired()` | 清理过期事件 |

### 3.31 PerformanceMonitor

> 加密操作性能监控器，记录操作耗时、吞吐量、字节处理量等指标。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static PerformanceMonitor create()` | 创建监控器 |
| `Timer startTimer(String operation)` | 开始计时 |
| `<T> T measure(String operation, Supplier<T> action)` | 测量操作耗时 |
| `<T> T measureWithBytes(String operation, long bytes, Supplier<T> action)` | 测量操作（带字节数） |
| `void recordOperation(String operation, long durationNanos, long bytesProcessed, boolean success)` | 记录操作 |
| `Optional<OperationMetrics> getMetrics(String operation)` | 获取操作指标 |
| `Map<String, OperationMetrics> getAllMetrics()` | 获取所有指标 |
| `Duration getUptime()` | 获取运行时间 |
| `long getTotalOperations()` | 获取总操作数 |
| `long getTotalBytes()` | 获取总处理字节数 |
| `double getOperationsPerSecond()` | 获取每秒操作数 |
| `void reset()` | 重置统计 |
| `String getSummaryReport()` | 获取摘要报告 |

### 3.32 SecurityConfig

> 全局安全配置 Record，包含默认安全级别、算法套件、密钥轮换间隔、审计和监控开关。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static SecurityConfig defaults()` | 默认配置 |
| `static Builder builder()` | 创建 Builder |
| `Builder.defaultSecurityLevel(SecurityLevel level)` | 设置默认安全级别 |
| `Builder.defaultAlgorithmSuite(AlgorithmSuite suite)` | 设置默认算法套件 |
| `Builder.keyRotationInterval(Duration interval)` | 设置密钥轮换间隔 |
| `Builder.auditEnabled(boolean enabled)` | 启用审计 |
| `Builder.metricsEnabled(boolean enabled)` | 启用监控 |

### 3.33 SecurityLevel

> 安全等级枚举，定义 LOW、MEDIUM、HIGH、MAXIMUM 四个级别，每个级别对应不同的密钥长度。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `int symmetricKeySize()` | 对称密钥长度 |
| `int asymmetricKeySize()` | 非对称密钥长度 |
| `String description()` | 描述信息 |
| `boolean isAtLeast(SecurityLevel required)` | 是否达到指定级别 |
| `static SecurityLevel getDefault()` | 获取默认级别 |

### 3.34 Scenario

> 业务场景枚举，定义 SERVER、CLIENT、MOBILE、IOT、INTERNAL、EXTERNAL 等场景。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `String description()` | 描述信息 |
| `boolean supportsStrongCrypto()` | 是否支持强加密 |
| `boolean supportsKeyRotation()` | 是否支持密钥轮换 |
| `SecurityLevel recommendedSecurityLevel()` | 推荐安全级别 |

### 3.35 Platform

> 平台类型枚举，定义 AWS、AZURE、GCP、ALIBABA_CLOUD、ON_PREMISE 等平台。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `String displayName()` | 显示名称 |
| `String kmsService()` | KMS 服务名 |
| `boolean supportsCloudKms()` | 是否支持云 KMS |
| `boolean isCloud()` | 是否为云平台 |
| `static Platform fromString(String name)` | 根据名称查找 |

### 3.36 异常体系

> 安全组件异常层次结构，所有异常均继承自 `OpenSecurityException`（继承 OpenException）。

| 异常类 | 描述 |
|------|------|
| `OpenSecurityException` | 安全基础异常 |
| `OpenKeyManagementException` | 密钥管理异常，包含 keyAlias 和 KeyOperation |
| `OpenTlsException` | TLS 异常，包含 hostname 和 TlsErrorType |
| `OpenPasswordPolicyException` | 密码策略异常，包含 violations 列表 |
| `OpenSecurityConfigException` | 配置异常，包含 configKey |

**工厂方法示例：**

```java
// 密钥管理异常
throw OpenKeyManagementException.keyNotFound("master-key");
throw OpenKeyManagementException.rotationFailed("app-key", cause);

// TLS 异常
throw OpenTlsException.certificateExpired("example.com");
throw OpenTlsException.hostnameMismatch("expected.com", "actual.com");

// 密码策略异常
throw OpenPasswordPolicyException.tooWeak("密码长度不足");
throw OpenPasswordPolicyException.leaked();
```
