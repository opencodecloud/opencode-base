# OpenCode Base OAuth2

**OAuth 2.0 / OIDC 客户端库 - 授权码、客户端凭证、设备码、PKCE、PAR、Token 内省，适用于 JDK 25+**

`opencode-base-oauth2` 是一个轻量级、零依赖的 OAuth 2.0 和 OpenID Connect 客户端库，内置主流提供者（Google、Microsoft、GitHub、Apple、Facebook）支持，以及 PKCE、JWT 解析、OIDC 自动发现、Token 内省（RFC 7662）、推送授权请求（RFC 9126）和可插拔令牌存储与自动生命周期管理。

## 功能特性

- **多种授权类型**：授权码、客户端凭证、设备码、刷新令牌
- **PKCE 支持**：公开客户端的代码交换证明密钥（S256）
- **预配置提供者**：Google、Microsoft（含租户）、GitHub、Apple、Facebook
- **自定义提供者**：完整支持任何 OAuth2 兼容服务器
- **OIDC 支持**：OpenID Connect，含 UserInfo 端点和 JWT 声明解析
- **OIDC 自动发现**：从 `/.well-known/openid-configuration` 自动配置端点（RFC 8414）
- **令牌管理**：内存和文件令牌存储，自动刷新，防雷鸣群保护
- **Token 内省**：从授权服务器查询令牌状态（RFC 7662）
- **推送授权请求（PAR）**：通过后端通道提交参数增强安全性（RFC 9126）
- **State 参数**：加密安全的 CSRF 防护，常量时间验证
- **颁发者验证**：授权服务器颁发者验证，防止混淆攻击（RFC 9207）
- **资源指示器**：指定目标资源以获取范围更窄的令牌（RFC 8707）
- **JWT 解析**：轻量级 JWT 声明提取，无外部依赖
- **HTTPS 强制**：自动 HTTPS 验证（仅 localhost 允许 HTTP）
- **OAuth2 错误解析**：从服务器响应中提取 `error`、`error_description`、`error_uri`
- **线程安全**：所有组件都是线程安全的
- **建造者模式**：流式 API 构建客户端和配置

## 快速开始

### Maven 依赖
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-oauth2</artifactId>
    <version>1.0.3</version>
</dependency>
```

### 基本用法 - Google OAuth2

```java
import cloud.opencode.base.oauth2.*;
import cloud.opencode.base.oauth2.pkce.PkceChallenge;

// 创建 Google 客户端
OAuth2Client client = OpenOAuth2.google("client-id", "client-secret")
    .redirectUri("https://yourapp.com/callback")
    .scopes("openid", "email", "profile")
    .build();

// 生成 PKCE 挑战和 state 参数
PkceChallenge pkce = OpenOAuth2.generatePkce();
String state = OpenOAuth2.generateState();

// 获取授权 URL
String authUrl = client.getAuthorizationUrl(state, pkce);

// 验证回调中的 state
if (!OpenOAuth2.validateState(state, callbackState)) {
    throw new SecurityException("检测到 CSRF 攻击");
}

// 用授权码交换令牌
OAuth2Token token = client.exchangeCode(code, pkce.verifier());

// 解析 JWT 声明
JwtClaims claims = OpenOAuth2.parseJwt(token.idToken());
```

### OIDC 自动发现

```java
import cloud.opencode.base.oauth2.discovery.*;

// 自动发现端点
DiscoveryDocument doc = OpenOAuth2.discover("https://accounts.google.com");
String tokenEndpoint = doc.tokenEndpoint();
boolean supportsPkce = doc.supportsPkce();

// 从发现文档构建配置
OAuth2Config config = doc.toConfig("client-id", "client-secret");
```

### 令牌生命周期管理

```java
import cloud.opencode.base.oauth2.token.*;

// 创建令牌管理器（防雷鸣群保护）
TokenManager manager = OpenOAuth2.tokenManager()
    .tokenStore(OpenOAuth2.inMemoryTokenStore())
    .build();

// 存储和获取令牌（即将过期时自动刷新）
manager.store("user-123", token);
OAuth2Token valid = manager.getValidToken("user-123");

// 不存在时自动获取
OAuth2Token t = manager.getOrObtain("user-123", () -> client.getClientCredentialsToken());
```

### Token 内省（RFC 7662）

```java
import cloud.opencode.base.oauth2.introspection.*;

TokenIntrospection introspection = OpenOAuth2.tokenIntrospection(
    "https://auth.example.com/introspect", "client-id", "client-secret");

IntrospectionResult result = introspection.introspect(accessToken);
if (result.active()) {
    String username = result.username();
    Set<String> scopes = result.scopes();
}
```

### 推送授权请求（RFC 9126）

```java
import cloud.opencode.base.oauth2.par.*;

PushedAuthorizationRequest par = OpenOAuth2.par(
    "https://auth.example.com/par", "client-id", "client-secret");

ParResponse response = par.push(Map.of(
    "response_type", "code",
    "redirect_uri", "https://yourapp.com/callback",
    "scope", "openid email"
));

String authUrl = PushedAuthorizationRequest.buildAuthorizationUrl(
    "https://auth.example.com/authorize", response, "client-id");
```

### 客户端凭证流程

```java
OAuth2Client client = OpenOAuth2.client()
    .clientId("service-client-id")
    .clientSecret("service-client-secret")
    .tokenEndpoint("https://auth.example.com/token")
    .scopes("api.read", "api.write")
    .build();

OAuth2Token token = client.getClientCredentialsToken();
```

### 设备码流程

```java
OAuth2Client client = OpenOAuth2.microsoft("client-id", "client-secret")
    .build();

DeviceCodeResponse device = client.requestDeviceCode();
System.out.println("访问: " + device.verificationUri());
System.out.println("输入代码: " + device.userCode());

// 轮询直到用户授权
Optional<OAuth2Token> token = client.pollDeviceToken(device.deviceCode());
```

## 类参考

| 类名 | 说明 |
|------|------|
| `OpenOAuth2` | 主门面 - 提供者、PKCE、JWT、发现、内省、PAR 的工厂方法 |
| `OAuth2Client` | 核心 OAuth2 客户端，支持所有授权类型和令牌管理 |
| `OAuth2Config` | 不可变配置（端点、凭证、作用域、PAR、内省、资源指示器） |
| `OAuth2Scope` | 标准 OAuth2 作用域常量 |
| `OAuth2Token` | 不可变令牌记录（访问令牌、刷新令牌、ID 令牌、过期时间） |
| `OAuth2ErrorCode` | OAuth2 异常的错误码枚举 |
| `OAuth2Exception` | 继承 OpenException 的异常，含服务器错误详情 |
| `DeviceCodeResponse` | 设备授权响应 |
| `GrantType` | 授权类型枚举 |
| `DiscoveryDocument` | OIDC 发现文档记录（RFC 8414） |
| `OidcDiscovery` | OIDC 端点自动发现（含缓存） |
| `IntrospectionResult` | Token 内省结果记录（RFC 7662） |
| `TokenIntrospection` | Token 内省客户端 |
| `ParResponse` | PAR 响应记录（RFC 9126） |
| `PushedAuthorizationRequest` | PAR 客户端 |
| `StateParameter` | 加密安全 state 参数生成器/验证器 |
| `IssuerValidator` | 授权服务器颁发者验证（RFC 9207） |
| `ResourceIndicator` | 资源指示器记录（RFC 8707） |
| `JwtClaims` | JWT 令牌声明解析器 |
| `OidcClient` | OpenID Connect 客户端 |
| `OidcConfig` | OIDC 特定配置 |
| `OidcToken` | 含 ID 令牌的 OIDC 令牌 |
| `UserInfo` | OIDC UserInfo 端点响应 |
| `PkceChallenge` | PKCE 挑战/验证器生成器（S256） |
| `CustomProvider` | 自定义 OAuth2 提供者配置构建器 |
| `OAuth2Provider` | 定义端点 URL 的提供者接口 |
| `ProviderRegistry` | OAuth2 提供者管理注册表 |
| `Providers` | 预配置提供者（Google、Microsoft、GitHub、Apple、Facebook） |
| `DefaultTokenManager` | 令牌生命周期管理器（防雷鸣群保护） |
| `FileTokenStore` | 基于文件的持久化令牌存储 |
| `InMemoryTokenStore` | 内存令牌存储 |
| `TokenManager` | 令牌生命周期管理接口 |
| `TokenRefresher` | 后台令牌刷新调度器 |
| `TokenStore` | 令牌持久化的 SPI 接口 |

## 安全性

本库经过 6 轮安全审计，共发现 46 项问题（36 项已修复，10 项接受风险）。

**加密安全：**
- 常量时间比较（`MessageDigest.isEqual`）用于 nonce、state 和 issuer 验证 — 防止时序侧信道攻击
- PKCE S256 使用 per-call `MessageDigest` — 对虚拟线程安全（无 `ThreadLocal` 泄漏）
- `SecureRandom` 用于所有随机数生成（state、nonce、PKCE 验证器）

**输入验证与防御：**
- 所有端点��制 HTTPS（仅 `localhost`/`127.0.0.1`/`::1` 允许 HTTP）
- JWT 大小限制（64KB）防止 OOM DoS
- JWT 要求 3 段结构（header.payload.signature）
- `iat`（签发时间）未来检查，防止令牌预签
- PKCE 验证器强制 43-128 字符范围（RFC 7636）
- Microsoft 租户 ID 正则验证 `[a-zA-Z0-9._-]+`，防止 URL 注入
- 拒绝负数 `expires_in`，防止令牌生命周期操纵

**数据保护：**
- `OAuth2Token.toString()` 和 `OAuth2Config.toString()` 脱敏（访问令牌、刷新令牌、客户端密钥）
- 异常消息已净化 — 不包含 issuer 值、不包含带查询参数的完整 URL
- `OidcToken.toString()` 排除 PII（主题、邮箱）
- `FileTokenStore` 使用 POSIX owner-only 权限（`rw-------`）+ 原子写入

**协议合规：**
- OIDC Discovery 缓存 1 小时 TTL（防止缓存投毒）
- SSRF 防护：���现 URL 必须使用 HTTPS scheme
- HTTP 重定向已禁用（`Redirect.NEVER`），防止凭证转发
- 错误消息中响应体截断至 256 字符，防止信息泄露

**JSON 解析：**
- 转义感知字符串解析，支持完整 JSON 反转义（`\"`、`\\`、`\/`、`\n`、`\r`、`\t`、`\b`、`\f`、`\uXXXX`）
- 快速路径优化：无转义字符串直接 `substring`（零分配）
- 统一 `JsonParser` 确保所有组件解析一致

## 环境要求

- Java 25+（使用记录类、密封类、java.net.http.HttpClient）
- 无外部依赖

## 许可证

Apache License 2.0

## 作者

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
