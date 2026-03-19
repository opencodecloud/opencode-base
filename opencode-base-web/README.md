# OpenCode Base Web

**Web utilities for Java 25+**

`opencode-base-web` provides a comprehensive set of web development utilities including unified result wrappers, pagination, request context management, URL/cookie/SSE/HTTP helpers, response encryption, and common web validation.

## Features

### Core Features
- **Unified Result**: Generic `Result<T>` wrapper with success/failure, error codes, and messages
- **Pagination**: `PageRequest`, `PageResult`, and `Sort` for consistent pagination handling
- **Request Context**: Thread-local request/user context management with trace ID support
- **URL Utilities**: URL encoding/decoding, query string parsing/building

### Advanced Features
- **Result Encryption**: AES-based response encryption for sensitive data
- **HTTP Constants**: Enums for HTTP methods, status codes, content types, and standard headers
- **Cookie Management**: `CookieJar` for cookie parsing and building
- **SSE Support**: Server-Sent Events data model
- **Request Body Builders**: Form, JSON, and file body builders
- **URL Builder**: Fluent URL construction with query parameters
- **Base64 Encoding**: Standard and URL-safe Base64 encoding/decoding
- **Validation**: IP, email, URL validation and private IP detection
- **Exception Handling**: Business exception converter with result code mapping
- **SPI Extension**: `ResultCodeProvider` and `ResultCustomizer` for customization

## Quick Start

### Maven Dependency
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-web</artifactId>
    <version>1.0.0</version>
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
| `AbstractResultEncryptor` | Base class for result encryption implementations |
| `AesResultEncryptor` | AES-based response data encryption |
| `EncryptedResult` | Encrypted result wrapper with ciphertext and metadata |
| `OpenCryptoException` | Exception for web crypto operations |
| `ResultEncryptor` | Interface for result encryption |

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

### Internal (`web.internal`)
| Class | Description |
|-------|-------------|
| `TraceIdResolver` | Trace ID generation and resolution |

### Page (`web.page`)
| Class | Description |
|-------|-------------|
| `PageInfo` | Pagination metadata (total, pages, hasNext, hasPrevious) |
| `PageRequest` | Pagination request with page number, size, and sort |
| `PageResult<T>` | Paginated result with items, total count, and page info |
| `Sort` | Sort criteria with field name and direction |

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

## Requirements

- Java 25+
- No external dependencies

## License

Apache License 2.0

## Author

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
