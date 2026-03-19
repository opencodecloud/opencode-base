# OAuth2 组件方案

## 1. 组件概述

### 1.1 定位

`opencode-base-oauth2` 是一个轻量级的 OAuth 2.0 / OpenID Connect 客户端库，专为 JDK 25+ 设计。作为基础认证组件，供其他需要 OAuth2 认证的组件（如 Email、HTTP 客户端、云服务等）统一依赖。

### 1.2 核心特性

- **统一门面 API**：`OpenOAuth2` 提供 Google、Microsoft、GitHub、Apple、Facebook 等主流 Provider 的快捷创建
- **多授权模式**：支持 Authorization Code、Client Credentials、Device Code、Refresh Token 等模式
- **PKCE 支持**：内置 PKCE (Proof Key for Code Exchange) 支持，默认 S256 方法
- **Token 管理**：自动刷新、持久化（内存/文件）、过期检测
- **OpenID Connect**：完整的 OIDC 扩展，包括 ID Token 解析验证、UserInfo 获取、Nonce 验证
- **Provider 注册表**：内置主流 Provider 预设配置，支持自定义 Provider
- **JWT 解析**：无依赖的 JWT Claims 解析（不验证签名）
- **零外部依赖**：仅依赖 opencode-base-core 和 JDK 标准库

### 1.3 架构概览

```
+---------------------------------------------------------------------------+
|                            业务组件层                                      |
| +---------------+ +---------------+ +---------------+ +-----------------+ |
| | opencode-     | | opencode-     | | opencode-     | | opencode-       | |
| | base-email    | | base-net      | | base-cloud    | | base-xxx        | |
| | (Gmail OAuth) | | (API Client)  | | (AWS/GCP/...) | | (其他需OAuth2的)| |
| +-------+-------+ +-------+-------+ +-------+-------+ +--------+--------+ |
+---------+------------------+------------------+-----------------+---------+
          |                  |                  |                 |
          +------------------+------------------+-----------------+
                             |
                             v
          +--------------------------------------+
          |       opencode-base-oauth2           |
          | +----------------------------------+ |
          | |         OpenOAuth2 (门面)         | |
          | +----------------------------------+ |
          | +------------+ +---+ +------------+ |
          | | OAuth2     | |Oid| | Token      | |
          | | Client     | |c  | | Management | |
          | +------------+ +---+ +------------+ |
          | +------------+ +---+ +------------+ |
          | | Provider   | |PKC| | HTTP       | |
          | | Registry   | |E  | | Client     | |
          | +------------+ +---+ +------------+ |
          +--------------------------------------+
                             |
                             v
          +--------------------------------------+
          |        opencode-base-core            |
          +--------------------------------------+
```

### 1.4 支持的授权模式

| 授权模式 | 枚举值 | 适用场景 |
|---------|--------|---------|
| Authorization Code | `AUTHORIZATION_CODE` | Web 应用、移动应用（推荐配合 PKCE） |
| Client Credentials | `CLIENT_CREDENTIALS` | 服务器间通信、后台任务 |
| Device Code | `DEVICE_CODE` | CLI 工具、IoT 设备、智能电视 |
| Refresh Token | `REFRESH_TOKEN` | 刷新已有 Token |
| Password (Legacy) | `PASSWORD` | 遗留系统（不推荐） |
| Implicit (Legacy) | `IMPLICIT` | 遗留单页应用（不推荐） |

### 1.5 依赖关系

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-oauth2</artifactId>
    <version>${version}</version>
</dependency>
```

依赖层级：
```
opencode-base-oauth2
+-- opencode-base-core (基础工具)
+-- JDK HttpClient（无额外依赖）
```

---

## 2. 包结构

```
cloud.opencode.base.oauth2
+-- OpenOAuth2.java                    // 统一门面入口
+-- OAuth2Client.java                  // OAuth2 客户端
+-- OAuth2Config.java                  // 配置 record
+-- OAuth2Token.java                   // Token record
+-- OAuth2Scope.java                   // 权限范围常量与工具
|
+-- grant/                             // 授权模式
|   +-- GrantType.java                 // 授权类型枚举
|   +-- DeviceCodeResponse.java        // 设备码响应 record
|
+-- token/                             // Token 管理
|   +-- TokenStore.java                // Token 存储接口
|   +-- InMemoryTokenStore.java        // 内存 Token 存储
|   +-- FileTokenStore.java            // 文件 Token 存储
|   +-- TokenManager.java             // Token 管理接口
|   +-- TokenRefresher.java            // Token 刷新器
|
+-- provider/                          // Provider 支持
|   +-- OAuth2Provider.java            // Provider 接口
|   +-- Providers.java                 // 内置 Provider 常量 (GOOGLE, MICROSOFT, GITHUB, APPLE, FACEBOOK)
|   +-- CustomProvider.java            // 自定义 Provider
|   +-- ProviderRegistry.java          // Provider 注册表
|
+-- oidc/                              // OpenID Connect 扩展
|   +-- OidcClient.java                // OIDC 客户端
|   +-- OidcConfig.java                // OIDC 配置 record
|   +-- OidcToken.java                 // OIDC Token（包含 ID Token Claims）
|   +-- JwtClaims.java                 // JWT 声明 record
|   +-- UserInfo.java                  // 用户信息 record
|
+-- pkce/                              // PKCE 支持
|   +-- PkceChallenge.java             // PKCE 挑战 record
|
+-- http/                              // HTTP 传输
|   +-- OAuth2HttpClient.java          // OAuth2 HTTP 客户端
|   +-- HttpClientFactory.java         // HTTP 客户端工厂
|
+-- exception/                         // 异常
    +-- OAuth2Exception.java           // OAuth2 异常
    +-- OAuth2ErrorCode.java           // 错误码枚举
```

---

## 3. 核心 API

### 3.1 OpenOAuth2 -- 门面入口

`OpenOAuth2` 是整个 OAuth2 模块的统一入口，提供主流 Provider 快捷创建、PKCE 生成、JWT 解析、Token 存储等功能。

```java
public final class OpenOAuth2 {

    // === 预置 Provider 快捷创建（返回 Builder） ===
    public static OAuth2Client.Builder google(String clientId, String clientSecret);
    public static OAuth2Client.Builder microsoft(String clientId, String clientSecret);
    public static OAuth2Client.Builder microsoft(String tenantId, String clientId, String clientSecret);
    public static OAuth2Client.Builder github(String clientId, String clientSecret);
    public static OAuth2Client.Builder apple(String clientId, String clientSecret);
    public static OAuth2Client.Builder facebook(String clientId, String clientSecret);

    // === 自定义 Provider ===
    public static OAuth2Client.Builder client();
    public static OAuth2Client.Builder client(OAuth2Provider provider);
    public static OAuth2Client fromConfig(OAuth2Config config);

    // === PKCE ===
    public static PkceChallenge generatePkce();

    // === JWT 工具 ===
    public static JwtClaims parseJwt(String token);

    // === Token 工具 ===
    public static boolean isExpired(OAuth2Token token);
    public static boolean isExpiringSoon(OAuth2Token token, Duration threshold);

    // === Token 存储 ===
    public static TokenStore inMemoryTokenStore();
    public static TokenStore fileTokenStore(Path directory);
    public static TokenStore fileTokenStore(String appName);

    // === 配置构建 ===
    public static OAuth2Config.Builder configBuilder();
}
```

**使用示例：**

```java
// Google OAuth2 客户端
OAuth2Client client = OpenOAuth2.google("client-id", "client-secret")
    .redirectUri("http://localhost:8080/callback")
    .scopes("openid", "email", "profile")
    .build();

// PKCE 授权码流程
PkceChallenge pkce = OpenOAuth2.generatePkce();
String authUrl = client.getAuthorizationUrl("random-state", pkce);
// ... 用户授权后获得 code ...
OAuth2Token token = client.exchangeCode(code, pkce.verifier());

// 解析 JWT
JwtClaims claims = OpenOAuth2.parseJwt(token.idToken());
System.out.println("Subject: " + claims.sub());
```

---

### 3.2 OAuth2Client -- OAuth2 客户端

核心客户端类，实现 `AutoCloseable`，支持所有授权模式的完整流程。

```java
public class OAuth2Client implements AutoCloseable {

    // === 配置 ===
    public OAuth2Config config();

    // === Authorization Code 流程 ===
    public String getAuthorizationUrl(String state);
    public String getAuthorizationUrl(String state, PkceChallenge pkce);
    public String getAuthorizationUrl(String state, PkceChallenge pkce,
                                       Map<String, String> additionalParams);
    public OAuth2Token exchangeCode(String code);
    public OAuth2Token exchangeCode(String code, String codeVerifier);

    // === Client Credentials 流程 ===
    public OAuth2Token getClientCredentialsToken();

    // === Device Code 流程 ===
    public DeviceCodeResponse requestDeviceCode();
    public Optional<OAuth2Token> pollDeviceToken(String deviceCode);

    // === Token 刷新 ===
    public OAuth2Token refreshToken(String refreshToken);
    public OAuth2Token refreshToken(OAuth2Token token);

    // === Token 撤销 ===
    public void revokeToken(String token);
    public void revokeToken(OAuth2Token token);

    // === 用户信息 ===
    public UserInfo getUserInfo(OAuth2Token token);

    // === Token 存储管理 ===
    public void storeToken(String key, OAuth2Token token);
    public Optional<OAuth2Token> getStoredToken(String key);
    public OAuth2Token getValidToken(String key);   // 自动刷新过期 Token
    public void removeToken(String key);

    // === 生命周期 ===
    public void close();

    // === 构建器 ===
    public static Builder builder();

    public static class Builder {
        public Builder config(OAuth2Config config);
        public Builder provider(OAuth2Provider provider);
        public Builder clientId(String clientId);
        public Builder clientSecret(String clientSecret);
        public Builder redirectUri(String redirectUri);
        public Builder scopes(String... scopes);
        public Builder grantType(GrantType grantType);
        public Builder tokenStore(TokenStore tokenStore);
        public Builder httpClient(OAuth2HttpClient httpClient);
        public OAuth2Client build();
    }
}
```

**Authorization Code 完整示例：**

```java
try (OAuth2Client client = OpenOAuth2.google("client-id", "client-secret")
        .redirectUri("http://localhost:8080/callback")
        .scopes("openid", "email", "profile")
        .tokenStore(OpenOAuth2.fileTokenStore("my-app"))
        .build()) {

    // 1. 生成 PKCE
    PkceChallenge pkce = OpenOAuth2.generatePkce();

    // 2. 生成授权 URL，引导用户授权
    String authUrl = client.getAuthorizationUrl("state-123", pkce);
    System.out.println("请访问: " + authUrl);

    // 3. 用户授权后，使用授权码换取 Token
    String code = "authorization-code-from-callback";
    OAuth2Token token = client.exchangeCode(code, pkce.verifier());

    // 4. 使用 Token 访问 API
    String accessToken = token.accessToken();
    String bearerHeader = token.toBearerHeader(); // "Bearer xxx..."

    // 5. 存储 Token 供后续使用
    client.storeToken("user-1", token);

    // 6. 后续获取有效 Token（自动刷新）
    OAuth2Token validToken = client.getValidToken("user-1");

    // 7. 获取用户信息
    UserInfo userInfo = client.getUserInfo(validToken);
    System.out.println("用户: " + userInfo.name());
}
```

**Client Credentials 示例：**

```java
try (OAuth2Client client = OpenOAuth2.google("client-id", "client-secret")
        .grantType(GrantType.CLIENT_CREDENTIALS)
        .build()) {

    OAuth2Token token = client.getClientCredentialsToken();
    // 使用 token.accessToken() 调用 API
}
```

**Device Code 示例：**

```java
try (OAuth2Client client = OpenOAuth2.microsoft("client-id", "client-secret")
        .build()) {

    // 1. 请求设备码
    DeviceCodeResponse response = client.requestDeviceCode();
    System.out.println("请访问: " + response.verificationUri());
    System.out.println("输入代码: " + response.userCode());

    // 2. 轮询等待用户授权
    Optional<OAuth2Token> token;
    do {
        Thread.sleep(response.interval() * 1000L);
        token = client.pollDeviceToken(response.deviceCode());
    } while (token.isEmpty() && !response.isExpired());

    if (token.isPresent()) {
        System.out.println("授权成功: " + token.get().accessToken());
    }
}
```

---

### 3.3 OAuth2Config -- 配置记录

OAuth2 客户端的完整配置，使用 Java record 实现。

```java
public record OAuth2Config(
    String clientId,
    String clientSecret,
    String authorizationEndpoint,
    String tokenEndpoint,
    String userInfoEndpoint,
    String revocationEndpoint,
    String deviceAuthorizationEndpoint,
    String redirectUri,
    Set<String> scopes,
    GrantType grantType,
    boolean usePkce,
    Duration connectTimeout,
    Duration readTimeout,
    Duration refreshThreshold
) {
    // === 流程判断 ===
    public boolean isAuthorizationCodeFlow();
    public boolean isClientCredentialsFlow();
    public boolean isDeviceCodeFlow();
    public boolean hasUserInfoEndpoint();
    public boolean hasRevocationEndpoint();
    public boolean hasDeviceAuthorizationEndpoint();

    // === 构建器 ===
    public static Builder builder();

    public static class Builder {
        public Builder clientId(String clientId);
        public Builder clientSecret(String clientSecret);
        public Builder authorizationEndpoint(String authorizationEndpoint);
        public Builder tokenEndpoint(String tokenEndpoint);
        public Builder userInfoEndpoint(String userInfoEndpoint);
        public Builder revocationEndpoint(String revocationEndpoint);
        public Builder deviceAuthorizationEndpoint(String deviceAuthorizationEndpoint);
        public Builder redirectUri(String redirectUri);
        public Builder scopes(String... scopes);
        public Builder scopes(Set<String> scopes);
        public Builder scope(String scope);
        public Builder grantType(GrantType grantType);
        public Builder usePkce(boolean usePkce);
        public Builder connectTimeout(Duration connectTimeout);
        public Builder readTimeout(Duration readTimeout);
        public Builder refreshThreshold(Duration refreshThreshold);
        public OAuth2Config build();
    }
}
```

**使用示例：**

```java
OAuth2Config config = OAuth2Config.builder()
    .clientId("my-client-id")
    .clientSecret("my-client-secret")
    .authorizationEndpoint("https://auth.example.com/authorize")
    .tokenEndpoint("https://auth.example.com/token")
    .userInfoEndpoint("https://auth.example.com/userinfo")
    .revocationEndpoint("https://auth.example.com/revoke")
    .redirectUri("http://localhost:8080/callback")
    .scopes("openid", "email", "profile")
    .grantType(GrantType.AUTHORIZATION_CODE)
    .usePkce(true)
    .connectTimeout(Duration.ofSeconds(10))
    .readTimeout(Duration.ofSeconds(30))
    .refreshThreshold(Duration.ofMinutes(5))
    .build();

OAuth2Client client = OpenOAuth2.fromConfig(config);
```

---

### 3.4 OAuth2Token -- Token 记录

OAuth2 访问令牌，使用 Java record 实现，提供过期检测和 Bearer 头生成。

```java
public record OAuth2Token(
    String accessToken,
    String tokenType,
    String refreshToken,
    String idToken,
    Set<String> scopes,
    Instant issuedAt,
    Instant expiresAt
) {
    // === 状态检查 ===
    public boolean isExpired();
    public boolean isExpiringSoon(Duration threshold);
    public Duration remainingTime();
    public boolean hasRefreshToken();
    public boolean hasIdToken();

    // === HTTP 头 ===
    public String toBearerHeader();        // "Bearer xxx"
    public String toAuthorizationHeader(); // "Bearer xxx"

    // === 构建器 ===
    public static Builder builder();
    public static Builder builder(OAuth2Token token);  // 从现有 Token 复制

    public static class Builder {
        public Builder accessToken(String accessToken);
        public Builder tokenType(String tokenType);
        public Builder refreshToken(String refreshToken);
        public Builder idToken(String idToken);
        public Builder scopes(Set<String> scopes);
        public Builder scope(String scope);
        public Builder scopeString(String scopeString);
        public Builder expiresIn(long seconds);
        public Builder expiresAt(Instant expiresAt);
        public Builder issuedAt(Instant issuedAt);
        public OAuth2Token build();
    }
}
```

**使用示例：**

```java
OAuth2Token token = OAuth2Token.builder()
    .accessToken("eyJhbGciOiJSUzI1NiIs...")
    .tokenType("Bearer")
    .refreshToken("refresh-token-value")
    .expiresIn(3600)
    .scope("openid")
    .scope("email")
    .build();

// 检查过期
if (token.isExpiringSoon(Duration.ofMinutes(5))) {
    token = client.refreshToken(token);
}

// 获取 Authorization 头
String header = token.toBearerHeader(); // "Bearer eyJhbGciOiJSUzI1NiIs..."
```

---

### 3.5 OAuth2Scope -- 权限范围

提供标准 OIDC 权限范围常量和主流 Provider 特有权限范围常量。

```java
public final class OAuth2Scope {

    // === 标准 OIDC 范围 ===
    public static final String OPENID = "openid";
    public static final String PROFILE = "profile";
    public static final String EMAIL = "email";
    public static final String ADDRESS = "address";
    public static final String PHONE = "phone";
    public static final String OFFLINE_ACCESS = "offline_access";

    // === 工具方法 ===
    public static Set<String> combine(String... scopes);
    public static Set<String> combine(Set<String>... scopeSets);
    public static String toString(Set<String> scopes);
    public static String toString(String... scopes);
    public static Set<String> parse(String scopeString);
    public static Set<String> defaultOidc();  // openid + profile + email

    // === Google 特有范围 ===
    public static final class Google {
        public static final String GMAIL_READONLY;
        public static final String GMAIL_SEND;
        public static final String GMAIL_FULL;
        public static final String GMAIL_MODIFY;
        public static final String CALENDAR_READONLY;
        public static final String CALENDAR;
        public static final String DRIVE_READONLY;
        public static final String DRIVE;
    }

    // === Microsoft 特有范围 ===
    public static final class Microsoft {
        public static final String USER_READ;
        public static final String USER_READ_ALL;
        public static final String MAIL_READ;
        public static final String MAIL_SEND;
        public static final String MAIL_READWRITE;
        public static final String CALENDARS_READ;
        public static final String CALENDARS_READWRITE;
        public static final String FILES_READ;
        public static final String FILES_READWRITE;
        public static final String DEFAULT;
    }

    // === GitHub 特有范围 ===
    public static final class GitHub {
        public static final String READ_USER;
        public static final String USER_EMAIL;
        public static final String USER;
        public static final String PUBLIC_REPO;
        public static final String REPO;
        public static final String READ_ORG;
        public static final String GIST;
    }
}
```

**使用示例：**

```java
// 组合多个范围
Set<String> scopes = OAuth2Scope.combine(
    OAuth2Scope.OPENID,
    OAuth2Scope.EMAIL,
    OAuth2Scope.Google.GMAIL_READONLY
);

// 解析 scope 字符串
Set<String> parsed = OAuth2Scope.parse("openid email profile");

// 转为空格分隔字符串
String scopeStr = OAuth2Scope.toString(scopes);
```

---

### 3.6 GrantType -- 授权类型枚举

```java
public enum GrantType {
    AUTHORIZATION_CODE,
    CLIENT_CREDENTIALS,
    DEVICE_CODE,
    REFRESH_TOKEN,
    PASSWORD,
    IMPLICIT;

    public String value();                        // 返回 OAuth2 标准值，如 "authorization_code"
    public static GrantType fromValue(String value);
}
```

---

### 3.7 DeviceCodeResponse -- 设备码响应

```java
public record DeviceCodeResponse(
    String deviceCode,
    String userCode,
    String verificationUri,
    String verificationUriComplete,
    int expiresIn,
    int interval,
    Instant createdAt
) {
    public static final int DEFAULT_INTERVAL = 5;

    public boolean isExpired();
    public Instant expiresAt();
    public long remainingSeconds();
    public boolean hasVerificationUriComplete();
    public String getBestVerificationUri();

    public static Builder builder();

    public static class Builder {
        public Builder deviceCode(String deviceCode);
        public Builder userCode(String userCode);
        public Builder verificationUri(String verificationUri);
        public Builder verificationUriComplete(String verificationUriComplete);
        public Builder expiresIn(int expiresIn);
        public Builder interval(int interval);
        public Builder createdAt(Instant createdAt);
        public DeviceCodeResponse build();
    }
}
```

---

### 3.8 PkceChallenge -- PKCE 挑战

PKCE (Proof Key for Code Exchange) 用于增强 Authorization Code 流程的安全性。

```java
public record PkceChallenge(String verifier, String challenge, String method) {

    public static final String METHOD_S256 = "S256";
    public static final String METHOD_PLAIN = "plain";

    // === 生成 ===
    public static PkceChallenge generate();                   // 默认 S256，32 字节
    public static PkceChallenge generate(int verifierBytes);  // 自定义长度
    public static PkceChallenge plain(String verifier);       // plain 方法
    public static String calculateS256Challenge(String verifier);

    // === 验证 ===
    public static boolean verify(String verifier, String challenge, String method);

    // === 方法检查 ===
    public boolean isS256();
    public boolean isPlain();
}
```

**使用示例：**

```java
// 生成 PKCE
PkceChallenge pkce = PkceChallenge.generate();

// 构建授权 URL 时使用
String authUrl = authEndpoint
    + "?code_challenge=" + pkce.challenge()
    + "&code_challenge_method=" + pkce.method();

// 换取 Token 时使用 verifier
OAuth2Token token = client.exchangeCode(code, pkce.verifier());

// 验证
boolean valid = PkceChallenge.verify(pkce.verifier(), pkce.challenge(), pkce.method());
```

---

### 3.9 Provider 体系

#### OAuth2Provider 接口

```java
public interface OAuth2Provider {
    String name();
    String authorizationEndpoint();
    String tokenEndpoint();
    String userInfoEndpoint();        // 可返回 null
    String revocationEndpoint();      // 可返回 null
    String deviceAuthorizationEndpoint(); // 可返回 null
    Set<String> defaultScopes();
    default boolean requiresPkce() { return false; }
    default String jwksUri() { return null; }
    default String issuer() { return null; }
    default OAuth2Config toConfig(String clientId, String clientSecret,
                                   String redirectUri, Set<String> scopes,
                                   GrantType grantType) { ... }
}
```

#### Providers -- 内置 Provider 常量

```java
public final class Providers {
    public static final OAuth2Provider GOOGLE;
    public static final OAuth2Provider MICROSOFT;
    public static final OAuth2Provider GITHUB;
    public static final OAuth2Provider APPLE;
    public static final OAuth2Provider FACEBOOK;

    public static OAuth2Provider microsoftTenant(String tenantId);
}
```

**内置 Provider 配置：**

| Provider | 授权端点 | PKCE | 默认 Scope |
|----------|---------|------|-----------|
| Google | `accounts.google.com/o/oauth2/v2/auth` | 是 | openid, profile, email |
| Microsoft | `login.microsoftonline.com/common/oauth2/v2/authorize` | 是 | openid, profile, email |
| GitHub | `github.com/login/oauth/authorize` | 否 | read:user, user:email |
| Apple | `appleid.apple.com/auth/authorize` | 是 | openid, name, email |
| Facebook | `facebook.com/v18.0/dialog/oauth` | 否 | email, public_profile |

#### CustomProvider -- 自定义 Provider

```java
public final class CustomProvider implements OAuth2Provider {

    public static Builder builder();
    public static Builder from(OAuth2Provider provider); // 从现有 Provider 派生

    public static class Builder {
        public Builder name(String name);
        public Builder authorizationEndpoint(String authorizationEndpoint);
        public Builder tokenEndpoint(String tokenEndpoint);
        public Builder userInfoEndpoint(String userInfoEndpoint);
        public Builder revocationEndpoint(String revocationEndpoint);
        public Builder deviceAuthorizationEndpoint(String deviceAuthorizationEndpoint);
        public Builder jwksUri(String jwksUri);
        public Builder issuer(String issuer);
        public Builder defaultScopes(String... scopes);
        public Builder defaultScopes(Set<String> scopes);
        public Builder addScope(String scope);
        public CustomProvider build();
    }
}
```

**使用示例：**

```java
// 自定义 Provider
OAuth2Provider myProvider = CustomProvider.builder()
    .name("MyIdentityServer")
    .authorizationEndpoint("https://idp.example.com/authorize")
    .tokenEndpoint("https://idp.example.com/token")
    .userInfoEndpoint("https://idp.example.com/userinfo")
    .revocationEndpoint("https://idp.example.com/revoke")
    .jwksUri("https://idp.example.com/.well-known/jwks.json")
    .issuer("https://idp.example.com")
    .defaultScopes("openid", "profile", "email")
    .build();

OAuth2Client client = OAuth2Client.builder()
    .provider(myProvider)
    .clientId("my-client-id")
    .clientSecret("my-client-secret")
    .redirectUri("http://localhost:8080/callback")
    .build();

// 从现有 Provider 派生
OAuth2Provider customGoogle = CustomProvider.from(Providers.GOOGLE)
    .name("CustomGoogle")
    .build();
```

#### ProviderRegistry -- Provider 注册表

```java
public class ProviderRegistry {

    public ProviderRegistry();
    public static ProviderRegistry global();       // 全局单例
    public static ProviderRegistry withBuiltins(); // 包含内置 Provider

    public ProviderRegistry register(OAuth2Provider provider);
    public ProviderRegistry register(String name, OAuth2Provider provider);
    public OAuth2Provider get(String name);
    public Optional<OAuth2Provider> find(String name);
    public boolean contains(String name);
    public Optional<OAuth2Provider> remove(String name);
    public Set<String> names();
    public Collection<OAuth2Provider> all();
    public int size();
    public boolean isEmpty();
    public void clear();
}
```

**使用示例：**

```java
ProviderRegistry registry = ProviderRegistry.withBuiltins();

// 注册自定义 Provider
registry.register(myProvider);

// 按名称查找
OAuth2Provider google = registry.get("Google");
Optional<OAuth2Provider> provider = registry.find("MyIdentityServer");

// 获取所有已注册名称
Set<String> names = registry.names();
```

---

### 3.10 Token 管理

#### TokenStore 接口

```java
public interface TokenStore {
    void save(String key, OAuth2Token token);
    Optional<OAuth2Token> load(String key);
    void delete(String key);
    void deleteAll();
    default boolean exists(String key) { ... }
    default Set<String> keys() { ... }
    default int size() { ... }
    default int removeExpired() { ... }
}
```

#### InMemoryTokenStore

线程安全的内存 Token 存储实现。

```java
public class InMemoryTokenStore implements TokenStore {
    public InMemoryTokenStore();
    public void save(String key, OAuth2Token token);
    public Optional<OAuth2Token> load(String key);
    public void delete(String key);
    public void deleteAll();
    public boolean exists(String key);
    public Set<String> keys();
    public int size();
    public int removeExpired();
}
```

#### FileTokenStore

文件持久化 Token 存储实现，Token 以文件形式保存在指定目录。

```java
public class FileTokenStore implements TokenStore {
    public FileTokenStore(Path directory);
    public void save(String key, OAuth2Token token);
    public Optional<OAuth2Token> load(String key);
    public void delete(String key);
    public void deleteAll();
    public Set<String> keys();
}
```

**使用示例：**

```java
// 内存存储
TokenStore memoryStore = new InMemoryTokenStore();

// 文件存储
TokenStore fileStore = new FileTokenStore(Path.of(".tokens"));

// 通过门面创建
TokenStore store = OpenOAuth2.inMemoryTokenStore();
TokenStore fileStore = OpenOAuth2.fileTokenStore("my-app");

// 使用
store.save("user-1", token);
Optional<OAuth2Token> loaded = store.load("user-1");
store.delete("user-1");
```

#### TokenRefresher -- Token 刷新器

自动处理 Token 刷新，支持异步刷新。实现 `AutoCloseable`。

```java
public class TokenRefresher implements AutoCloseable {

    public TokenRefresher(OAuth2Config config, OAuth2HttpClient httpClient);
    public TokenRefresher(OAuth2Config config, OAuth2HttpClient httpClient,
                          Duration refreshThreshold);

    public boolean needsRefresh(OAuth2Token token);
    public OAuth2Token refresh(OAuth2Token token);
    public CompletableFuture<OAuth2Token> refreshAsync(OAuth2Token token);

    public Duration refreshThreshold();
    public boolean isClosed();
    public void close();
}
```

#### TokenManager 接口

```java
public interface TokenManager extends AutoCloseable {
    // Token 管理的高级接口
}
```

---

### 3.11 OpenID Connect 扩展

#### OidcClient -- OIDC 客户端

在 `OAuth2Client` 基础上提供 OpenID Connect 扩展功能，包括 ID Token 验证、Nonce 验证、UserInfo 获取。实现 `AutoCloseable`。

```java
public class OidcClient implements AutoCloseable {

    // === 基础访问 ===
    public OAuth2Client oauth2Client();
    public OidcConfig oidcConfig();

    // === Nonce 生成 ===
    public String generateNonce();

    // === 授权 URL ===
    public String getAuthorizationUrl(String state);
    public String getAuthorizationUrl(String state, PkceChallenge pkce);
    public String getAuthorizationUrl(String state, PkceChallenge pkce, String nonce);

    // === Token 交换 ===
    public OidcToken exchangeCode(String code);
    public OidcToken exchangeCode(String code, String codeVerifier);
    public OidcToken exchangeCode(String code, String codeVerifier, String expectedNonce);

    // === ID Token 验证 ===
    public void validateIdToken(OidcToken token, String expectedNonce);

    // === Token 刷新 ===
    public OidcToken refreshToken(OidcToken token);
    public OidcToken getValidToken(String key);

    // === 用户信息 ===
    public UserInfo getUserInfo(OidcToken token);
    public UserInfo getUserInfo(OAuth2Token token);

    // === Token 存储 ===
    public void storeToken(String key, OidcToken token);
    public Optional<OidcToken> getStoredToken(String key);
    public void removeToken(String key);

    // === 生命周期 ===
    public void close();

    // === 构建器 ===
    public static Builder builder();

    public static class Builder {
        public Builder oauth2Client(OAuth2Client oauth2Client);
        public Builder ownedOAuth2Client(OAuth2Client oauth2Client);
        public Builder oidcConfig(OidcConfig oidcConfig);
        public OidcClient build();
    }
}
```

**使用示例：**

```java
// 创建 OIDC 客户端
OAuth2Client oauth2 = OpenOAuth2.google("client-id", "client-secret")
    .redirectUri("http://localhost:8080/callback")
    .build();

OidcClient oidc = OidcClient.builder()
    .ownedOAuth2Client(oauth2)
    .oidcConfig(OidcConfig.strict(
        "https://accounts.google.com",
        "https://www.googleapis.com/oauth2/v3/certs"
    ))
    .build();

// OIDC 授权流程
String nonce = oidc.generateNonce();
PkceChallenge pkce = PkceChallenge.generate();
String authUrl = oidc.getAuthorizationUrl("state", pkce, nonce);

// 换取 OIDC Token
OidcToken token = oidc.exchangeCode(code, pkce.verifier(), nonce);

// 获取用户信息
String email = token.email();
String name = token.name();
String subject = token.subject();

// 获取详细用户信息
UserInfo userInfo = oidc.getUserInfo(token);
```

#### OidcConfig -- OIDC 配置

```java
public record OidcConfig(
    String issuer,
    String jwksUri,
    String userInfoEndpoint,
    boolean validateIdToken,
    boolean validateNonce,
    boolean validateAudience,
    boolean validateExpiration,
    Duration clockSkew,
    Set<String> requiredClaims,
    Set<String> requestedClaims
) {
    public boolean canValidateSignature();
    public boolean canValidateIssuer();

    public static Builder builder();
    public static OidcConfig defaults();
    public static OidcConfig strict(String issuer, String jwksUri);

    public static class Builder {
        public Builder issuer(String issuer);
        public Builder jwksUri(String jwksUri);
        public Builder userInfoEndpoint(String userInfoEndpoint);
        public Builder validateIdToken(boolean validateIdToken);
        public Builder validateNonce(boolean validateNonce);
        public Builder validateAudience(boolean validateAudience);
        public Builder validateExpiration(boolean validateExpiration);
        public Builder clockSkew(Duration clockSkew);
        public Builder requiredClaims(String... claims);
        public Builder requestedClaims(String... claims);
        public OidcConfig build();
    }
}
```

#### OidcToken -- OIDC Token

封装 OAuth2Token 和解析后的 ID Token Claims，提供便捷的用户信息访问方法。

```java
public final class OidcToken {

    public OidcToken(OAuth2Token oauth2Token, JwtClaims idTokenClaims);
    public static OidcToken from(OAuth2Token oauth2Token);

    // === Token 访问 ===
    public OAuth2Token oauth2Token();
    public Optional<JwtClaims> idTokenClaims();
    public boolean hasIdToken();
    public String accessToken();
    public String refreshToken();
    public String idToken();

    // === 状态检查 ===
    public boolean isExpired();
    public boolean isExpiringSoon(Duration threshold);
    public boolean hasRefreshToken();
    public boolean isValid();
    public boolean isIdTokenExpired();
    public String toBearerHeader();

    // === 从 ID Token Claims 读取用户信息 ===
    public String subject();
    public String issuer();
    public String audience();
    public Instant expiration();
    public Instant issuedAt();
    public String nonce();
    public String email();
    public boolean isEmailVerified();
    public String name();
    public String picture();
    public Set<String> scopes();
}
```

#### JwtClaims -- JWT 声明

无依赖的 JWT Claims 解析（仅解析 Payload，不验证签名）。

```java
public record JwtClaims(
    String iss,       // 签发者
    String sub,       // 主题（用户ID）
    String aud,       // 受众
    Instant exp,      // 过期时间
    Instant iat,      // 签发时间
    Instant nbf,      // 生效时间
    String jti,       // JWT ID
    String nonce,     // OIDC Nonce
    Map<String, Object> claims  // 所有声明
) {
    public boolean isExpired();
    public boolean isNotYetValid();
    public boolean isValid();

    public Optional<Object> getClaim(String name);
    public Optional<String> getClaimAsString(String name);
    public String audience();                    // 返回第一个 audience
    public boolean hasAudience(String audience);

    public static JwtClaims parse(String token);
}
```

**使用示例：**

```java
JwtClaims claims = JwtClaims.parse(idToken);

String subject = claims.sub();
String issuer = claims.iss();
boolean valid = claims.isValid();
Optional<String> email = claims.getClaimAsString("email");

if (claims.hasAudience("my-client-id")) {
    // 验证 audience
}
```

#### UserInfo -- 用户信息

OpenID Connect 标准用户信息，使用 Java record 实现。

```java
public record UserInfo(
    String sub,
    String name,
    String givenName,
    String familyName,
    String middleName,
    String nickname,
    String preferredUsername,
    String profile,
    String picture,
    String website,
    String email,
    Boolean emailVerified,
    String gender,
    String birthdate,
    String zoneinfo,
    String locale,
    String phoneNumber,
    Boolean phoneNumberVerified,
    Map<String, Object> claims
) {
    public Optional<Object> getClaim(String name);
    public Optional<String> getClaimAsString(String name);
    public boolean isEmailVerified();
    public boolean isPhoneNumberVerified();
    public String displayName();  // 返回 name 或拼接 givenName + familyName

    public static Builder builder();
    public static UserInfo fromJson(String json);

    public static class Builder {
        public Builder sub(String sub);
        public Builder name(String name);
        public Builder givenName(String givenName);
        public Builder familyName(String familyName);
        public Builder middleName(String middleName);
        public Builder nickname(String nickname);
        public Builder preferredUsername(String preferredUsername);
        public Builder profile(String profile);
        public Builder picture(String picture);
        public Builder website(String website);
        public Builder email(String email);
        public Builder emailVerified(Boolean emailVerified);
        public Builder gender(String gender);
        public Builder birthdate(String birthdate);
        public Builder zoneinfo(String zoneinfo);
        public Builder locale(String locale);
        public Builder phoneNumber(String phoneNumber);
        public Builder phoneNumberVerified(Boolean phoneNumberVerified);
        public Builder claims(Map<String, Object> claims);
        public Builder claim(String name, Object value);
        public UserInfo build();
    }
}
```

---

### 3.12 HTTP 传输

#### OAuth2HttpClient

基于 JDK HttpClient 的 OAuth2 HTTP 客户端，实现 `AutoCloseable`。

```java
public class OAuth2HttpClient implements AutoCloseable {

    public OAuth2HttpClient();
    public OAuth2HttpClient(Duration connectTimeout, Duration readTimeout);
    public OAuth2HttpClient(OAuth2Config config);

    public String postForm(String url, Map<String, String> params);
    public String postForm(String url, Map<String, String> params, Map<String, String> headers);
    public String get(String url, Map<String, String> headers);

    public void close();
}
```

#### HttpClientFactory

```java
public final class HttpClientFactory {

    public static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(10);
    public static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(30);

    public static OAuth2HttpClient create();
    public static OAuth2HttpClient create(Duration connectTimeout, Duration readTimeout);
    public static OAuth2HttpClient create(OAuth2Config config);
    public static OAuth2HttpClient shared();  // 共享实例

    public static Builder builder();

    public static class Builder {
        public Builder connectTimeout(Duration connectTimeout);
        public Builder readTimeout(Duration readTimeout);
        public Builder connectTimeoutSeconds(long seconds);
        public Builder readTimeoutSeconds(long seconds);
        public OAuth2HttpClient build();
    }
}
```

---

### 3.13 异常体系

```
RuntimeException
  +-- OAuth2Exception
        - errorCode: OAuth2ErrorCode
        - details: String
```

#### OAuth2Exception

```java
public class OAuth2Exception extends RuntimeException {

    public OAuth2Exception(OAuth2ErrorCode errorCode);
    public OAuth2Exception(OAuth2ErrorCode errorCode, String details);
    public OAuth2Exception(OAuth2ErrorCode errorCode, Throwable cause);
    public OAuth2Exception(OAuth2ErrorCode errorCode, String details, Throwable cause);

    public OAuth2ErrorCode errorCode();
    public String details();
    public int code();

    // 工厂方法
    public static OAuth2Exception tokenExpired();
    public static OAuth2Exception tokenInvalid(String details);
    public static OAuth2Exception authorizationFailed(String details);
    public static OAuth2Exception networkError(Throwable cause);
    public static OAuth2Exception invalidConfig(String details);
}
```

#### OAuth2ErrorCode 枚举

```java
public enum OAuth2ErrorCode {
    // Token 相关
    TOKEN_EXPIRED,
    TOKEN_INVALID,
    TOKEN_REVOKED,
    REFRESH_FAILED,

    // 授权相关
    AUTHORIZATION_FAILED,
    INVALID_GRANT,
    INVALID_SCOPE,
    ACCESS_DENIED,

    // 配置相关
    INVALID_CONFIG,
    PROVIDER_NOT_FOUND,

    // 网络相关
    NETWORK_ERROR,
    TIMEOUT,

    // 其他
    UNKNOWN_ERROR,
    ...;

    public int code();
    public String message();
}
```

---

## 4. 完整使用示例

### 4.1 Google OAuth2 完整流程

```java
// 1. 创建客户端
try (OAuth2Client client = OpenOAuth2.google("client-id", "client-secret")
        .redirectUri("http://localhost:8080/callback")
        .scopes(OAuth2Scope.OPENID, OAuth2Scope.EMAIL, OAuth2Scope.PROFILE,
                OAuth2Scope.Google.GMAIL_READONLY)
        .tokenStore(OpenOAuth2.fileTokenStore("my-gmail-app"))
        .build()) {

    // 2. 生成授权 URL（带 PKCE）
    PkceChallenge pkce = OpenOAuth2.generatePkce();
    String authUrl = client.getAuthorizationUrl("random-state", pkce);
    System.out.println("请在浏览器中打开: " + authUrl);

    // 3. 用户授权回调后获取 code
    String code = getCodeFromCallback(); // 从回调获取

    // 4. 换取 Token
    OAuth2Token token = client.exchangeCode(code, pkce.verifier());
    System.out.println("Access Token: " + token.accessToken());
    System.out.println("过期时间: " + token.expiresAt());

    // 5. 获取用户信息
    UserInfo userInfo = client.getUserInfo(token);
    System.out.println("用户: " + userInfo.name());
    System.out.println("邮箱: " + userInfo.email());

    // 6. 存储 Token
    client.storeToken("user-1", token);

    // 7. 后续使用：自动刷新
    OAuth2Token validToken = client.getValidToken("user-1");

    // 8. 解析 ID Token
    if (token.hasIdToken()) {
        JwtClaims claims = OpenOAuth2.parseJwt(token.idToken());
        System.out.println("Subject: " + claims.sub());
    }
}
```

### 4.2 Microsoft 多租户

```java
try (OAuth2Client client = OpenOAuth2.microsoft("tenant-id", "client-id", "client-secret")
        .redirectUri("http://localhost:8080/callback")
        .scopes(OAuth2Scope.Microsoft.USER_READ, OAuth2Scope.Microsoft.MAIL_READ)
        .build()) {

    OAuth2Token token = client.getClientCredentialsToken();
    // 使用 token 调用 Microsoft Graph API
}
```

### 4.3 GitHub OAuth2

```java
try (OAuth2Client client = OpenOAuth2.github("client-id", "client-secret")
        .redirectUri("http://localhost:8080/callback")
        .scopes(OAuth2Scope.GitHub.READ_USER, OAuth2Scope.GitHub.USER_EMAIL,
                OAuth2Scope.GitHub.REPO)
        .build()) {

    String authUrl = client.getAuthorizationUrl("state-123");
    // ... 授权流程 ...
    OAuth2Token token = client.exchangeCode(code);
    UserInfo userInfo = client.getUserInfo(token);
}
```

### 4.4 OIDC 完整流程

```java
OAuth2Client oauth2 = OpenOAuth2.google("client-id", "client-secret")
    .redirectUri("http://localhost:8080/callback")
    .scopes("openid", "email", "profile")
    .build();

try (OidcClient oidc = OidcClient.builder()
        .ownedOAuth2Client(oauth2)
        .oidcConfig(OidcConfig.strict(
            "https://accounts.google.com",
            "https://www.googleapis.com/oauth2/v3/certs"
        ))
        .build()) {

    // 生成 Nonce 和 PKCE
    String nonce = oidc.generateNonce();
    PkceChallenge pkce = PkceChallenge.generate();

    // 授权
    String authUrl = oidc.getAuthorizationUrl("state", pkce, nonce);

    // 换取 Token 并验证 ID Token
    OidcToken token = oidc.exchangeCode(code, pkce.verifier(), nonce);

    // 直接访问用户信息（从 ID Token）
    System.out.println("用户ID: " + token.subject());
    System.out.println("邮箱: " + token.email());
    System.out.println("姓名: " + token.name());
    System.out.println("头像: " + token.picture());
    System.out.println("邮箱已验证: " + token.isEmailVerified());

    // 获取完整 UserInfo
    UserInfo userInfo = oidc.getUserInfo(token);
    System.out.println("显示名: " + userInfo.displayName());
}
```

### 4.5 自定义 Provider

```java
OAuth2Provider myIdp = CustomProvider.builder()
    .name("EnterpriseIDP")
    .authorizationEndpoint("https://idp.corp.com/oauth/authorize")
    .tokenEndpoint("https://idp.corp.com/oauth/token")
    .userInfoEndpoint("https://idp.corp.com/oauth/userinfo")
    .revocationEndpoint("https://idp.corp.com/oauth/revoke")
    .jwksUri("https://idp.corp.com/.well-known/jwks.json")
    .issuer("https://idp.corp.com")
    .defaultScopes("openid", "profile", "email")
    .build();

try (OAuth2Client client = OpenOAuth2.client(myIdp)
        .clientId("my-client")
        .clientSecret("my-secret")
        .redirectUri("http://localhost:8080/callback")
        .build()) {

    // 使用与内置 Provider 完全相同的 API
    PkceChallenge pkce = OpenOAuth2.generatePkce();
    String authUrl = client.getAuthorizationUrl("state", pkce);
    // ...
}
```

### 4.6 Device Code 流程（CLI 工具）

```java
try (OAuth2Client client = OpenOAuth2.microsoft("client-id", "client-secret")
        .grantType(GrantType.DEVICE_CODE)
        .build()) {

    DeviceCodeResponse device = client.requestDeviceCode();

    System.out.println("=== 设备授权 ===");
    System.out.println("请在浏览器中访问: " + device.getBestVerificationUri());
    System.out.println("输入代码: " + device.userCode());
    System.out.println("此代码将在 " + device.remainingSeconds() + " 秒后过期");

    // 轮询等待
    while (!device.isExpired()) {
        Thread.sleep(device.interval() * 1000L);
        Optional<OAuth2Token> token = client.pollDeviceToken(device.deviceCode());
        if (token.isPresent()) {
            System.out.println("授权成功！");
            System.out.println("Access Token: " + token.get().accessToken());
            break;
        }
    }
}
```
