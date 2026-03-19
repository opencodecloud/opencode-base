# OpenCode Base Web

**适用于 Java 25+ 的 Web 工具库**

`opencode-base-web` 提供了一套完整的 Web 开发工具，包括统一结果封装、分页、请求上下文管理、URL/Cookie/SSE/HTTP 辅助工具、响应加密和常用 Web 验证。

## 功能特性

### 核心功能
- **统一结果封装**：泛型 `Result<T>` 包装器，支持成功/失败、错误码和消息
- **分页**：`PageRequest`、`PageResult` 和 `Sort`，提供一致的分页处理
- **请求上下文**：基于 ThreadLocal 的请求/用户上下文管理，支持链路追踪 ID
- **URL 工具**：URL 编码/解码、查询字符串解析/构建

### 高级功能
- **结果加密**：基于 AES 的响应加密，用于敏感数据
- **HTTP 常量**：HTTP 方法、状态码、Content-Type 和标准头部枚举
- **Cookie 管理**：`CookieJar` 用于 Cookie 解析和构建
- **SSE 支持**：Server-Sent Events 数据模型
- **请求体构建器**：表单、JSON 和文件请求体构建器
- **URL 构建器**：流式 URL 构建，支持查询参数
- **Base64 编码**：标准和 URL 安全的 Base64 编码/解码
- **验证**：IP、邮箱、URL 验证和私有 IP 检测
- **异常处理**：业务异常转换器，支持错误码映射
- **SPI 扩展**：`ResultCodeProvider` 和 `ResultCustomizer` 用于自定义

## 快速开始

### Maven 依赖
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-web</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 基本用法

```java
import cloud.opencode.base.web.*;

// 成功结果
Result<User> result = OpenWeb.ok(user);
Result<Void> success = OpenWeb.ok();

// 失败结果
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

// 验证
boolean validIp = OpenWeb.isValidIp("192.168.1.1");
boolean validEmail = OpenWeb.isValidEmail("test@example.com");
boolean privateIp = OpenWeb.isPrivateIp("10.0.0.1");

// Base64
String b64 = OpenWeb.base64Encode("secret");
String decoded = OpenWeb.base64Decode(b64);
```

### URL 构建器

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

## 类参考

### 根包 (`cloud.opencode.base.web`)
| 类 | 说明 |
|----|------|
| `OpenWeb` | 主门面：结果快捷方法、分页、上下文、URL 编码、验证 |
| `Result<T>` | 通用 API 响应封装，包含 code、message、data、timestamp |
| `Results` | 创建 Result 实例的静态工厂方法 |
| `ResultCode` | 结果码定义接口 |
| `CommonResultCode` | 标准结果码：SUCCESS、BAD_REQUEST、UNAUTHORIZED 等 |

### 请求体 (`web.body`)
| 类 | 说明 |
|----|------|
| `RequestBody` | 请求体类型基础接口 |
| `FormBody` | URL 编码的表单请求体构建器 |
| `JsonBody` | JSON 请求体构建器 |
| `FileBody` | 文件上传请求体构建器 |

### 上下文 (`web.context`)
| 类 | 说明 |
|----|------|
| `RequestContext` | 不可变请求上下文，包含 traceId、userId、headers、attributes |
| `RequestContextHolder` | 请求上下文的 ThreadLocal 持有者 |
| `UserContext` | 用户认证上下文，包含 userId、角色、权限 |

### Cookie (`web.cookie`)
| 类 | 说明 |
|----|------|
| `CookieJar` | Cookie 解析、构建和管理工具 |

### 加密 (`web.crypto`)
| 类 | 说明 |
|----|------|
| `AbstractResultEncryptor` | 结果加密实现的基类 |
| `AesResultEncryptor` | 基于 AES 的响应数据加密 |
| `EncryptedResult` | 加密结果封装，包含密文和元数据 |
| `OpenCryptoException` | Web 加密操作异常 |
| `ResultEncryptor` | 结果加密接口 |

### 异常 (`web.exception`)
| 类 | 说明 |
|----|------|
| `ExceptionConverter` | 将异常转换为带有错误码的 Result |
| `OpenBizException` | 业务逻辑异常，带结果码 |
| `OpenWebException` | Web 层异常 |

### HTTP (`web.http`)
| 类 | 说明 |
|----|------|
| `HttpMethod` | HTTP 方法枚举（GET、POST、PUT、DELETE 等） |
| `HttpStatus` | HTTP 状态码枚举，包含原因短语 |
| `ContentType` | 常用 Content-Type 常量 |
| `HttpHeaders` | 标准 HTTP 头部名称常量 |

### 内部 (`web.internal`)
| 类 | 说明 |
|----|------|
| `TraceIdResolver` | 链路追踪 ID 生成和解析 |

### 分页 (`web.page`)
| 类 | 说明 |
|----|------|
| `PageInfo` | 分页元数据（总数、总页数、hasNext、hasPrevious） |
| `PageRequest` | 分页请求，包含页码、页大小和排序 |
| `PageResult<T>` | 分页结果，包含数据列表、总数和分页信息 |
| `Sort` | 排序条件，包含字段名和方向 |

### SPI (`web.spi`)
| 类 | 说明 |
|----|------|
| `ResultCodeProvider` | 自定义结果码注册 SPI |
| `ResultCustomizer` | 结果创建自定义 SPI |

### SSE (`web.sse`)
| 类 | 说明 |
|----|------|
| `SseEvent` | Server-Sent Events 数据模型 |

### URL (`web.url`)
| 类 | 说明 |
|----|------|
| `QueryString` | 查询字符串解析和构建 |
| `UrlBuilder` | 流式 URL 构建器，支持 scheme、host、path、查询参数 |
| `OpenUrl` | URL 工具门面 |

### 工具 (`web.util`)
| 类 | 说明 |
|----|------|
| `WebUtil` | URL 编码/解码、查询字符串解析、IP/邮箱/URL 验证 |

## 环境要求

- Java 25+
- 无外部依赖

## 开源协议

Apache License 2.0

## 作者

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
