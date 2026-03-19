# OpenCode Base OAuth2

**OAuth 2.0 / OIDC 客户端库 - 授权码、客户端凭证、设备码、PKCE 支持，适用于 JDK 25+**

`opencode-base-oauth2` 是一个轻量级、零依赖的 OAuth 2.0 和 OpenID Connect 客户端库，内置主流提供者（Google、Microsoft、GitHub、Apple、Facebook）支持，以及 PKCE、JWT 解析和可插拔令牌存储。

## 功能特性

- **多种授权类型**：授权码、客户端凭证、设备码、刷新令牌
- **PKCE 支持**：公开客户端的代码交换证明密钥（S256）
- **预配置提供者**：Google、Microsoft（含租户）、GitHub、Apple、Facebook
- **自定义提供者**：完整支持任何 OAuth2 兼容服务器
- **OIDC 支持**：OpenID Connect，含 UserInfo 端点和 JWT 声明解析
- **令牌管理**：内存和文件令牌存储，自动刷新
- **JWT 解析**：轻量级 JWT 声明提取，无外部依赖
- **HTTPS 强制**：自动 HTTPS 验证（仅 localhost 允许 HTTP）
- **线程安全**：所有组件都是线程安全的
- **建造者模式**：流式 API 构建客户端和配置

## 快速开始

### Maven 依赖
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-oauth2</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 基本用法 - Google OAuth2

```java
import cloud.opencode.base.oauth2.OpenOAuth2;
import cloud.opencode.base.oauth2.OAuth2Client;
import cloud.opencode.base.oauth2.OAuth2Token;
import cloud.opencode.base.oauth2.pkce.PkceChallenge;

// 创建 Google 客户端
OAuth2Client client = OpenOAuth2.google("client-id", "client-secret")
    .redirectUri("https://yourapp.com/callback")
    .scopes("openid", "email", "profile")
    .build();

// 生成 PKCE 挑战
PkceChallenge pkce = OpenOAuth2.generatePkce();

// 获取授权 URL
String authUrl = client.getAuthorizationUrl("random-state", pkce);

// 用授权码交换令牌（用户授权后）
OAuth2Token token = client.exchangeCode(code, pkce.verifier());

// 解析 JWT 声明
JwtClaims claims = OpenOAuth2.parseJwt(token.idToken());
String email = claims.email();
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

### 令牌存储和自动刷新

```java
// 基于文件的令牌存储
OAuth2Client client = OpenOAuth2.github("id", "secret")
    .redirectUri("https://yourapp.com/callback")
    .tokenStore(OpenOAuth2.fileTokenStore("myapp"))
    .build();

// 存储和获取令牌
client.storeToken("user-123", token);
OAuth2Token valid = client.getValidToken("user-123"); // 即将过期时自动刷新
```

## 类参考

| 类名 | 说明 |
|------|------|
| `OpenOAuth2` | 主门面 - 提供者、PKCE、JWT 和令牌存储的工厂方法 |
| `OAuth2Client` | 核心 OAuth2 客户端，支持所有授权类型和令牌管理 |
| `OAuth2Config` | 不可变 OAuth2 配置（端点、凭证、作用域） |
| `OAuth2Scope` | 标准 OAuth2 作用域常量 |
| `OAuth2Token` | 不可变令牌记录（访问令牌、刷新令牌、ID 令牌、过期时间） |
| `OAuth2ErrorCode` | OAuth2 异常的错误码枚举 |
| `OAuth2Exception` | OAuth2 操作失败的异常类型 |
| `DeviceCodeResponse` | 设备授权响应，包含代码和 URI |
| `GrantType` | 授权类型枚举（授权码、客户端凭证等） |
| `HttpClientFactory` | OAuth2 请求的 HTTP 客户端工厂 |
| `OAuth2HttpClient` | OAuth2 端点通信的 HTTP 客户端 |
| `JwtClaims` | JWT 令牌声明解析器和访问器 |
| `OidcClient` | OpenID Connect 客户端，含发现和 UserInfo |
| `OidcConfig` | OIDC 特定配置 |
| `OidcToken` | 含 ID 令牌的 OIDC 令牌 |
| `UserInfo` | OIDC UserInfo 端点响应 |
| `PkceChallenge` | PKCE 挑战/验证器生成器（S256） |
| `CustomProvider` | 自定义 OAuth2 提供者配置构建器 |
| `OAuth2Provider` | 定义端点 URL 的提供者接口 |
| `ProviderRegistry` | OAuth2 提供者管理注册表 |
| `Providers` | 预配置提供者（Google、Microsoft、GitHub、Apple、Facebook） |
| `FileTokenStore` | 基于文件的持久化令牌存储 |
| `InMemoryTokenStore` | 内存令牌存储 |
| `TokenManager` | 令牌生命周期管理，含自动刷新 |
| `TokenRefresher` | 后台令牌刷新调度器 |
| `TokenStore` | 令牌持久化的 SPI 接口 |

## 环境要求

- Java 25+（使用记录类、密封类、java.net.http.HttpClient）
- 无外部依赖

## 许可证

Apache License 2.0

## 作者

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
