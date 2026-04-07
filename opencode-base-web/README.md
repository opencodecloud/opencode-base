# OpenCode Base Web

**Web utilities for Java 25+**

`opencode-base-web` provides a comprehensive set of web development utilities including unified result wrappers, pagination, request context management, URL/cookie/SSE/HTTP helpers, response encryption with signature verification, and common web validation.

## Features

### Core Features
- **Unified Result**: Generic `Result<T>` wrapper with success/failure, error codes, and messages
- **Pagination**: `PageRequest`, `PageResult`, and `Sort` for consistent pagination handling
- **Request Context**: Thread-local request/user context management with trace ID support
- **URL Utilities**: URL encoding/decoding, query string parsing/building

### Advanced Features
- **Result Encryption**: AES-256-GCM response encryption with HMAC-SHA256 signature for tamper detection (powered by `opencode-base-crypto`)
- **Auto Encryption Interceptor**: `@EncryptResult` / `@DecryptResult` annotations + `ResultEncryptionHandler` for framework integration
- **JSON Serialization**: Full object serialization/deserialization with generic type support (powered by `opencode-base-json`)
- **HTTP Constants**: Enums for HTTP methods, status codes, content types, and standard headers
- **Cookie Management**: `CookieJar` for cookie parsing and building
- **SSE Support**: Server-Sent Events data model
- **Request Body Builders**: Form, JSON, and file body builders
- **URL Builder**: Fluent URL construction with query parameters
- **Base64 Encoding**: Standard and URL-safe Base64 encoding/decoding
- **Validation**: IP, email, URL validation and private IP detection
- **Exception Handling**: Business exception converter with result code mapping
- **SPI Extension**: `ResultCodeProvider` and `ResultCustomizer` for customization

### V1.0.3 New Features
- **Multipart Body**: `MultipartBody` builder for multipart/form-data file uploads with text fields, byte[], Path, and InputStream support
- **CORS Configuration**: `CorsConfig` immutable record with builder, origin/method/header allowlists, presets (`allowAll()`, `restrictive()`)
- **Security Headers**: `SecurityHeaders` builder for CSP, HSTS, X-Frame-Options, X-Content-Type-Options, Referrer-Policy, Permissions-Policy, COEP, COOP
- **Rate Limit Info**: `RateLimitInfo` record for parsing/building X-RateLimit-* and Retry-After headers
- **Problem Details (RFC 9457)**: `ProblemDetail` record for standard error responses with extensions support
- **Media Type**: `MediaType` record with Accept header parsing, quality-factor sorting, and content negotiation (`bestMatch`)
- **ETag**: `ETag` record for SHA-256 based ETag generation, strong/weak comparison, and If-None-Match matching per RFC 7232
- **Client IP Resolution**: `ClientIp` utility for resolving real client IP from X-Forwarded-For, X-Real-IP, CF-Connecting-IP, True-Client-IP with trusted proxy filtering and IPv4/IPv6 support
- **Exception Hierarchy**: `OpenWebException` now extends `OpenException` (core unified base class)

## Quick Start

### Maven Dependency
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-web</artifactId>
    <version>1.0.3</version>
</dependency>
```

### Basic Usage

```java
import cloud.opencode.base.web.*;

// Success result
Result<User> result = OpenWeb.ok(user);
Result<Void> success = OpenWeb.ok();

// Failure result
Result<Void> error = OpenWeb.fail("Operation failed");
Result<Void> coded = OpenWeb.fail("BIZ_001", "Insufficient balance");
Result<Void> fromEx = OpenWeb.fail(exception);

// Pagination
PageRequest request = OpenWeb.pageRequest(1, 20, "createTime", "desc");
PageResult<User> page = OpenWeb.page(users, 1, 20, totalCount);

// Request context
OpenWeb.setContext(RequestContext.builder()
    .traceId("trace-123")
    .userId("user-456")
    .build());
String traceId = OpenWeb.getTraceId();
String userId = OpenWeb.getUserId();

// URL operations
String encoded = OpenWeb.urlEncode("hello world");
Map<String, String> params = OpenWeb.parseQuery("name=test&age=20");
String query = OpenWeb.buildQuery(Map.of("name", "test"));

// Validation
boolean validIp = OpenWeb.isValidIp("192.168.1.1");
boolean validEmail = OpenWeb.isValidEmail("test@example.com");
boolean privateIp = OpenWeb.isPrivateIp("10.0.0.1");

// Base64
String b64 = OpenWeb.base64Encode("secret");
String decoded = OpenWeb.base64Decode(b64);
```

### Result Encryption & Signature

Encrypt API responses with AES-256-GCM and protect against tampering with HMAC-SHA256 signature.

```java
import cloud.opencode.base.web.crypto.*;
import cloud.opencode.base.json.TypeReference;

// Create encryptor (both sides share the same key)
AesResultEncryptor encryptor = new AesResultEncryptor("shared-secret-key");

// --- Encrypt (server side) ---
Result<List<User>> result = Result.ok(userList);
EncryptedResult encrypted = encryptor.encrypt(result);
// Send encrypted JSON to client

// --- Decrypt (client side) ---
// decrypt() internally: verify signature → decrypt → deserialize
Result<List<User>> decrypted = encryptor.decrypt(encrypted, new TypeReference<List<User>>() {});
List<User> users = decrypted.data();

// If data has been tampered with → throws OpenCryptoException, data discarded
```

**Original Result JSON:**
```json
{
  "code": "00000",
  "message": "操作成功",
  "data": [
    { "id": 2, "email": "bob@gmail.com", "nickname": "Bob" }
  ],
  "success": true,
  "timestamp": "2026-03-25T08:30:00.123Z",
  "traceId": "13243244565464"
}
```

**Encrypted + Signed JSON:**
```json
{
  "code": "00000",
  "message": "操作成功",
  "encryptedData": "K7AxTDm5Fx4aCjJn9SfZ60yLKD1Vbt...",
  "algorithm": "AES-GCM",
  "timestamp": "2026-03-25T07:47:22.289849Z",
  "traceId": "13243244565464",
  "sign": "c7qBfZP7mDJ8fiZgkNsUAV8Fp9MqKp7Sr0nZr7Yy8bg="
}
```

**Custom Encryption Algorithm:**
```java
public class MyEncryptor extends AbstractResultEncryptor {
    @Override
    protected byte[] doEncrypt(byte[] data) throws Exception { /* ... */ }
    @Override
    protected byte[] doDecrypt(byte[] data) throws Exception { /* ... */ }
    @Override
    protected byte[] doSign(byte[] data) throws Exception { /* ... */ }
    @Override
    public String getAlgorithm() { return "MY-ALG"; }
}
```

### Auto Encryption Interceptor

Use `@EncryptResult` / `@DecryptResult` annotations for declarative encryption. The framework-agnostic `ResultEncryptionHandler` does the heavy lifting — framework interceptors just delegate to it.

```java
// 1. Implement key resolver (framework layer)
public class MyKeyResolver implements EncryptionKeyResolver {
    public byte[] resolveKey(String keyAlias) {
        return "partner".equals(keyAlias) ? partnerKey : defaultKey;
    }
}

// 2. Create handler
ResultEncryptionHandler handler = new ResultEncryptionHandler(new MyKeyResolver());

// 3. Annotate business methods
@EncryptResult                                          // encrypt with default key
public Result<User> getUser(Long id) { ... }

@EncryptResult(keyAlias = "partner")                    // encrypt with partner key
public Result<Order> getPartnerOrder(Long id) { ... }

@EncryptResult(enabled = false)                         // skip encryption
public Result<Config> getPublicConfig() { ... }

// 4. Decrypt incoming encrypted data
public Result<Void> callback(@DecryptResult EncryptedResult encrypted) { ... }

// 5. Framework interceptor delegates to handler
if (handler.shouldEncrypt(annotation)) {
    return handler.encrypt(result, annotation);
}
Result<T> decrypted = handler.decrypt(encrypted, dataType, annotation);
```

### URL Builder

```java
import cloud.opencode.base.web.url.*;

String url = UrlBuilder.create()
    .scheme("https")
    .host("api.example.com")
    .path("/users")
    .queryParam("page", "1")
    .queryParam("size", "20")
    .build();
```

## Class Reference

### Root Package (`cloud.opencode.base.web`)
| Class | Description |
|-------|-------------|
| `OpenWeb` | Main facade: result shortcuts, pagination, context, URL encoding, validation |
| `Result<T>` | Generic API response wrapper with code, message, data, timestamp |
| `Results` | Static factory methods for creating Result instances |
| `ResultCode` | Interface for result code definitions |
| `CommonResultCode` | Standard result codes: SUCCESS, BAD_REQUEST, UNAUTHORIZED, etc. |

### Body (`web.body`)
| Class | Description |
|-------|-------------|
| `RequestBody` | Base interface for request body types |
| `FormBody` | URL-encoded form request body builder |
| `JsonBody` | JSON request body builder |
| `FileBody` | File upload request body builder |
| `MultipartBody` | Multipart/form-data request body builder with text fields and file uploads |

### Cache (`web.cache`)
| Class | Description |
|-------|-------------|
| `ETag` | HTTP ETag generation (SHA-256), parsing, and strong/weak matching per RFC 7232 |

### CORS (`web.cors`)
| Class | Description |
|-------|-------------|
| `CorsConfig` | Immutable CORS configuration with builder, origin/method/header allowlists, and response header generation |

### Context (`web.context`)
| Class | Description |
|-------|-------------|
| `RequestContext` | Immutable request context with traceId, userId, headers, attributes |
| `RequestContextHolder` | Thread-local holder for request context |
| `UserContext` | User authentication context with userId, roles, permissions |

### Cookie (`web.cookie`)
| Class | Description |
|-------|-------------|
| `CookieJar` | Cookie parsing, building, and management utilities |

### Crypto (`web.crypto`)
| Class | Description |
|-------|-------------|
| `@EncryptResult` | Annotation: marks methods/classes for automatic response encryption |
| `@DecryptResult` | Annotation: marks parameters/classes for automatic request decryption |
| `ResultEncryptionHandler` | Core handler: framework interceptors delegate encrypt/decrypt to this |
| `EncryptionKeyResolver` | SPI: resolves encryption keys by alias |
| `ResultEncryptor` | SPI interface for result encryption/decryption with signature verification |
| `AbstractResultEncryptor` | Base class: encrypt, sign, verify, decrypt with OpenJson/OpenCrypto |
| `AesResultEncryptor` | AES-256-GCM encryption + HMAC-SHA256 signature (via opencode-base-crypto) |
| `EncryptedResult` | Encrypted result record: code, message, encryptedData, algorithm, timestamp, traceId, sign |
| `OpenCryptoException` | Exception for encryption/decryption/signature failures |

### Exception (`web.exception`)
| Class | Description |
|-------|-------------|
| `ExceptionConverter` | Converts exceptions to Result with appropriate error codes |
| `OpenBizException` | Business logic exception with result code |
| `OpenWebException` | Web layer exception |

### HTTP (`web.http`)
| Class | Description |
|-------|-------------|
| `HttpMethod` | Enum of HTTP methods (GET, POST, PUT, DELETE, etc.) |
| `HttpStatus` | Enum of HTTP status codes with reason phrases |
| `ContentType` | Common content type constants |
| `HttpHeaders` | Standard HTTP header name constants |
| `MediaType` | Media type with quality factor, Accept header parsing, and content negotiation |

### Page (`web.page`)
| Class | Description |
|-------|-------------|
| `PageInfo` | Pagination metadata (total, pages, hasNext, hasPrevious) |
| `PageRequest` | Pagination request with page number, size, and sort |
| `PageResult<T>` | Paginated result with items, total count, and page info |
| `Sort` | Sort criteria with field name and direction |

### Problem (`web.problem`)
| Class | Description |
|-------|-------------|
| `ProblemDetail` | RFC 9457 Problem Details for HTTP APIs with builder and extensions |

### Rate Limit (`web.ratelimit`)
| Class | Description |
|-------|-------------|
| `RateLimitInfo` | Parse/build X-RateLimit-* and Retry-After headers |

### Security (`web.security`)
| Class | Description |
|-------|-------------|
| `SecurityHeaders` | Builder for CSP, HSTS, X-Frame-Options, X-Content-Type-Options, Referrer-Policy, etc. |

### SPI (`web.spi`)
| Class | Description |
|-------|-------------|
| `ResultCodeProvider` | SPI for custom result code registration |
| `ResultCustomizer` | SPI for customizing result creation |

### SSE (`web.sse`)
| Class | Description |
|-------|-------------|
| `SseEvent` | Server-Sent Events data model |

### URL (`web.url`)
| Class | Description |
|-------|-------------|
| `QueryString` | Query string parsing and building |
| `UrlBuilder` | Fluent URL builder with scheme, host, path, query parameters |
| `OpenUrl` | URL utilities facade |

### Util (`web.util`)
| Class | Description |
|-------|-------------|
| `WebUtil` | URL encoding/decoding, query string parsing, IP/email/URL validation |
| `ClientIp` | Real client IP resolution from proxy headers (X-Forwarded-For, X-Real-IP, CF-Connecting-IP, True-Client-IP) |

## Dependencies

- `opencode-base-core` — Core utilities
- `opencode-base-json` — JSON serialization (OpenJson, TypeReference)
- `opencode-base-crypto` — Cryptography (AesGcmCipher, HmacSha256, OpenDigest, Hkdf, KeyGenerator)

## Requirements

- Java 25+

## License

Apache License 2.0

## Author

Leon Soo - [OpenCode.cloud](https://opencode.cloud)

---

# OpenCode Base Web 中文文档

**Java 25+ Web 工具库**

`opencode-base-web` 提供全面的 Web 开发工具集，包括统一响应封装、分页、请求上下文管理、URL/Cookie/SSE/HTTP 辅助工具、响应加密与签名验证，以及常用 Web 校验功能。

## 功能特性

### 核心功能
- **统一响应**：泛型 `Result<T>` 封装，支持成功/失败、错误码、消息
- **分页支持**：`PageRequest`、`PageResult`、`Sort`，统一分页处理
- **请求上下文**：线程安全的请求/用户上下文管理，支持链路追踪 ID
- **URL 工具**：URL 编解码、查询字符串解析/构建

### 高级功能
- **响应加密**：AES-256-GCM 加密 + HMAC-SHA256 签名防篡改（基于 `opencode-base-crypto`）
- **自动加密拦截**：`@EncryptResult` / `@DecryptResult` 注解 + `ResultEncryptionHandler` 处理器，支持框架集成
- **JSON 序列化**：完整的对象序列化/反序列化，支持泛型类型（基于 `opencode-base-json`）
- **HTTP 常量**：HTTP 方法、状态码、Content-Type、标准请求头枚举
- **Cookie 管理**：`CookieJar` Cookie 解析与构建
- **SSE 支持**：Server-Sent Events 数据模型
- **请求体构建器**：Form、JSON、文件上传请求体构建
- **URL 构建器**：流式 URL 构建，支持查询参数
- **Base64 编码**：标准和 URL 安全的 Base64 编解码
- **校验工具**：IP、邮箱、URL 校验及内网 IP 检测
- **异常处理**：业务异常转换器，支持响应码映射
- **SPI 扩展**：`ResultCodeProvider` 和 `ResultCustomizer` 自定义扩展

### V1.0.3 新增功能
- **Multipart 请求体**：`MultipartBody` 构建器，支持 multipart/form-data 文件上传
- **CORS 配置**：`CorsConfig` 不可变记录 + Builder，来源/方法/头部白名单
- **安全头部**：`SecurityHeaders` 构建器，支持 CSP、HSTS、X-Frame-Options 等
- **限流信息**：`RateLimitInfo` 解析/构建 X-RateLimit-* 和 Retry-After 头部
- **问题详情 (RFC 9457)**：`ProblemDetail` 标准错误响应格式
- **媒体类型**：`MediaType` Accept 头部解析、内容协商
- **ETag**：基于 SHA-256 的 ETag 生成、强/弱比较（RFC 7232）
- **客户端 IP 解析**：`ClientIp` 从代理头部解析真实 IP，支持 IPv4/IPv6
- **异常体系**：`OpenWebException` 现继承 `OpenException`

## 快速开始

### Maven 依赖
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-web</artifactId>
    <version>1.0.3</version>
</dependency>
```

### 基础用法

```java
import cloud.opencode.base.web.*;

// 成功响应
Result<User> result = OpenWeb.ok(user);
Result<Void> success = OpenWeb.ok();

// 失败响应
Result<Void> error = OpenWeb.fail("操作失败");
Result<Void> coded = OpenWeb.fail("BIZ_001", "余额不足");
Result<Void> fromEx = OpenWeb.fail(exception);

// 分页
PageRequest request = OpenWeb.pageRequest(1, 20, "createTime", "desc");
PageResult<User> page = OpenWeb.page(users, 1, 20, totalCount);

// 请求上下文
OpenWeb.setContext(RequestContext.builder()
    .traceId("trace-123")
    .userId("user-456")
    .build());
String traceId = OpenWeb.getTraceId();
String userId = OpenWeb.getUserId();

// URL 操作
String encoded = OpenWeb.urlEncode("hello world");
Map<String, String> params = OpenWeb.parseQuery("name=test&age=20");
String query = OpenWeb.buildQuery(Map.of("name", "test"));

// 校验
boolean validIp = OpenWeb.isValidIp("192.168.1.1");
boolean validEmail = OpenWeb.isValidEmail("test@example.com");
boolean privateIp = OpenWeb.isPrivateIp("10.0.0.1");

// Base64
String b64 = OpenWeb.base64Encode("secret");
String decoded = OpenWeb.base64Decode(b64);
```

### 响应加密与签名

使用 AES-256-GCM 加密 API 响应数据，并通过 HMAC-SHA256 签名防止篡改。

```java
import cloud.opencode.base.web.crypto.*;
import cloud.opencode.base.json.TypeReference;

// 创建加密器（双方共享同一密钥）
AesResultEncryptor encryptor = new AesResultEncryptor("shared-secret-key");

// --- 加密（服务端） ---
Result<List<User>> result = Result.ok(userList);
EncryptedResult encrypted = encryptor.encrypt(result);
// 将加密后的 JSON 发送给客户端

// --- 解密（客户端） ---
// decrypt() 内部流程：验签 → 解密 → 反序列化
Result<List<User>> decrypted = encryptor.decrypt(encrypted, new TypeReference<List<User>>() {});
List<User> users = decrypted.data();

// 数据被篡改 → 抛出 OpenCryptoException，数据丢弃
```

**原始 Result JSON：**
```json
{
  "code": "00000",
  "message": "操作成功",
  "data": [
    { "id": 2, "email": "bob@gmail.com", "nickname": "Bob" }
  ],
  "success": true,
  "timestamp": "2026-03-25T08:30:00.123Z",
  "traceId": "13243244565464"
}
```

**加密+签名后 JSON：**
```json
{
  "code": "00000",
  "message": "操作成功",
  "encryptedData": "K7AxTDm5Fx4aCjJn9SfZ60yLKD1Vbt...",
  "algorithm": "AES-GCM",
  "timestamp": "2026-03-25T07:47:22.289849Z",
  "traceId": "13243244565464",
  "sign": "c7qBfZP7mDJ8fiZgkNsUAV8Fp9MqKp7Sr0nZr7Yy8bg="
}
```

**字段说明：**

| 字段 | 说明 |
|------|------|
| `code` | 响应码（明文保留），如 `"00000"` 表示成功 |
| `message` | 响应消息（明文保留） |
| `encryptedData` | `data` 字段经 AES-256-GCM 加密后的 Base64 编码 |
| `algorithm` | 加密算法标识 |
| `timestamp` | 加密时间戳（ISO-8601） |
| `traceId` | 请求追踪 ID（明文保留） |
| `sign` | HMAC-SHA256 签名，覆盖除 sign 外的所有字段，防篡改 |

**解密流程：**
1. **验签** — 对 `code + message + encryptedData + algorithm + timestamp + traceId` 重新计算 HMAC-SHA256，与 `sign` 比对
2. **验签失败** — 抛出 `OpenCryptoException`，数据丢弃，不执行解密
3. **验签成功** — Base64 解码 → AES-GCM 解密 → JSON 反序列化 → 返回 `Result<T>`

**自定义加密算法：**
```java
public class MyEncryptor extends AbstractResultEncryptor {
    @Override
    protected byte[] doEncrypt(byte[] data) throws Exception { /* 加密逻辑 */ }
    @Override
    protected byte[] doDecrypt(byte[] data) throws Exception { /* 解密逻辑 */ }
    @Override
    protected byte[] doSign(byte[] data) throws Exception { /* 签名逻辑 */ }
    @Override
    public String getAlgorithm() { return "MY-ALG"; }
}
```

### 自动加密拦截

使用 `@EncryptResult` / `@DecryptResult` 注解实现声明式加密。框架无关的 `ResultEncryptionHandler` 负责核心逻辑，框架拦截器只需委托给它。

```java
// 1. 实现密钥解析器（框架层）
public class MyKeyResolver implements EncryptionKeyResolver {
    public byte[] resolveKey(String keyAlias) {
        return "partner".equals(keyAlias) ? partnerKey : defaultKey;
    }
}

// 2. 创建处理器
ResultEncryptionHandler handler = new ResultEncryptionHandler(new MyKeyResolver());

// 3. 在业务方法上添加注解
@EncryptResult                                          // 使用默认密钥加密
public Result<User> getUser(Long id) { ... }

@EncryptResult(keyAlias = "partner")                    // 使用合作方密钥加密
public Result<Order> getPartnerOrder(Long id) { ... }

@EncryptResult(enabled = false)                         // 跳过加密
public Result<Config> getPublicConfig() { ... }

// 4. 解密传入的加密数据
public Result<Void> callback(@DecryptResult EncryptedResult encrypted) { ... }

// 5. 框架拦截器委托给 handler
if (handler.shouldEncrypt(annotation)) {
    return handler.encrypt(result, annotation);         // 自动加密+签名
}
Result<T> decrypted = handler.decrypt(encrypted, dataType, annotation);  // 自动验签+解密
```

**注解说明：**

| 注解 | 目标 | 说明 |
|------|------|------|
| `@EncryptResult` | 方法/类 | 标记响应需要自动加密 |
| `@EncryptResult(keyAlias="x")` | 方法/类 | 指定密钥别名 |
| `@EncryptResult(enabled=false)` | 方法 | 在加密类中跳过特定方法 |
| `@DecryptResult` | 参数/类 | 标记传入数据需要自动验签解密 |
| `@DecryptResult(keyAlias="x")` | 参数/类 | 指定解密密钥别名 |

## 类参考

### 根包 (`cloud.opencode.base.web`)
| 类 | 说明 |
|----|------|
| `OpenWeb` | 主门面：响应快捷方法、分页、上下文、URL 编码、校验 |
| `Result<T>` | 泛型 API 响应封装，含 code、message、data、timestamp |
| `Results` | Result 实例创建的静态工厂方法 |
| `ResultCode` | 响应码定义接口 |
| `CommonResultCode` | 标准响应码枚举：SUCCESS、BAD_REQUEST、UNAUTHORIZED 等 |

### 请求体 (`web.body`)
| 类 | 说明 |
|----|------|
| `RequestBody` | 请求体基础接口 |
| `FormBody` | URL 编码表单请求体构建器 |
| `JsonBody` | JSON 请求体构建器 |
| `FileBody` | 文件上传请求体构建器 |
| `MultipartBody` | multipart/form-data 请求体构建器 |

### 缓存 (`web.cache`)
| 类 | 说明 |
|----|------|
| `ETag` | HTTP ETag 生成、解析和匹配（RFC 7232） |

### CORS (`web.cors`)
| 类 | 说明 |
|----|------|
| `CorsConfig` | 不可变 CORS 配置，支持 Builder 和响应头部生成 |

### 上下文 (`web.context`)
| 类 | 说明 |
|----|------|
| `RequestContext` | 不可变请求上下文，含 traceId、userId、headers、attributes |
| `RequestContextHolder` | 请求上下文的线程安全持有器 |
| `UserContext` | 用户认证上下文，含 userId、角色、权限 |

### Cookie (`web.cookie`)
| 类 | 说明 |
|----|------|
| `CookieJar` | Cookie 解析、构建和管理工具 |

### 加密 (`web.crypto`)
| 类 | 说明 |
|----|------|
| `@EncryptResult` | 注解：标记方法/类的响应需要自动加密 |
| `@DecryptResult` | 注解：标记参数/类的请求需要自动验签解密 |
| `ResultEncryptionHandler` | 核心处理器：框架拦截器委托加解密的入口 |
| `EncryptionKeyResolver` | SPI：根据别名解析加密密钥 |
| `ResultEncryptor` | SPI 接口：响应加密/解密/验签 |
| `AbstractResultEncryptor` | 基类：加密、签名、验签、解密，集成 OpenJson 和 OpenCrypto |
| `AesResultEncryptor` | AES-256-GCM 加密 + HMAC-SHA256 签名（基于 opencode-base-crypto） |
| `EncryptedResult` | 加密响应记录：code、message、encryptedData、algorithm、timestamp、traceId、sign |
| `OpenCryptoException` | 加密/解密/签名失败异常 |

### 异常 (`web.exception`)
| 类 | 说明 |
|----|------|
| `ExceptionConverter` | 异常转 Result，映射对应错误码 |
| `OpenBizException` | 业务逻辑异常，含响应码 |
| `OpenWebException` | Web 层异常 |

### HTTP (`web.http`)
| 类 | 说明 |
|----|------|
| `HttpMethod` | HTTP 方法枚举（GET、POST、PUT、DELETE 等） |
| `HttpStatus` | HTTP 状态码枚举，含原因短语 |
| `ContentType` | 常用 Content-Type 常量 |
| `HttpHeaders` | 标准 HTTP 请求头名称常量 |
| `MediaType` | 媒体类型，支持质量因子、Accept 解析、内容协商 |

### 分页 (`web.page`)
| 类 | 说明 |
|----|------|
| `PageInfo` | 分页元数据（总数、总页数、hasNext、hasPrevious） |
| `PageRequest` | 分页请求，含页码、每页大小、排序 |
| `PageResult<T>` | 分页响应，含数据列表、总数、分页信息 |
| `Sort` | 排序条件，含字段名和方向 |

### SPI (`web.spi`)
| 类 | 说明 |
|----|------|
| `ResultCodeProvider` | 自定义响应码注册 SPI |
| `ResultCustomizer` | Result 创建自定义 SPI |

### SSE (`web.sse`)
| 类 | 说明 |
|----|------|
| `SseEvent` | Server-Sent Events 数据模型 |

### URL (`web.url`)
| 类 | 说明 |
|----|------|
| `QueryString` | 查询字符串解析与构建 |
| `UrlBuilder` | 流式 URL 构建器，支持 scheme、host、path、查询参数 |
| `OpenUrl` | URL 工具门面 |

### 工具 (`web.util`)
| 类 | 说明 |
|----|------|
| `WebUtil` | URL 编解码、查询字符串解析、IP/邮箱/URL 校验 |
| `ClientIp` | 从代理头部解析真实客户端 IP |

## 模块依赖

- `opencode-base-core` — 核心工具
- `opencode-base-json` — JSON 序列化（OpenJson、TypeReference）
- `opencode-base-crypto` — 加密组件（AesGcmCipher、HmacSha256、OpenDigest、Hkdf、KeyGenerator）

## 环境要求

- Java 25+

## 许可证

Apache License 2.0

## 作者

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
