# OpenCode Base SMS

**SMS messaging utilities for Java 25+**

`opencode-base-sms` provides a unified API for sending SMS messages through multiple providers (Alibaba Cloud, Tencent Cloud, Huawei Cloud, custom HTTP), with template management, batch sending, rate limiting, phone number validation, and log sanitization.

## Features

### Core Features
- **Unified Facade**: Single `OpenSms` entry point for all SMS operations
- **Multi-Provider**: Support for Alibaba Cloud, Tencent Cloud, Huawei Cloud, custom HTTP, and console mock
- **Template Engine**: Register and render SMS templates with variable substitution
- **Batch Sending**: Send to multiple recipients in a single call
- **Provider Factory**: Create providers from configuration objects

### Security & Validation
- **Phone Validation**: Validate phone number format before sending
- **Rate Limiting**: Protect against SMS abuse with configurable rate limits
- **Log Sanitization**: Mask sensitive data (phone numbers, content) in logs
- **Secure Config**: Separate secure configuration for credentials

### Provider Support
| Provider | Class | Description |
|----------|-------|-------------|
| Console | `ConsoleSmsProvider` | Mock provider for development/testing |
| Alibaba Cloud | `AliSmsSender` | Alibaba Cloud SMS integration |
| Tencent Cloud | `TencentSmsSender` | Tencent Cloud SMS integration |
| Huawei Cloud | `HuaweiSmsSender` | Huawei Cloud SMS integration |
| HTTP | `HttpSmsProvider` | Generic HTTP-based SMS gateway |

## Quick Start

### Maven Dependency
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-sms</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Send a Simple SMS
```java
import cloud.opencode.base.sms.OpenSms;
import cloud.opencode.base.sms.message.SmsResult;

// Console mock for development
OpenSms sms = OpenSms.console();

SmsResult result = sms.send("13800138000", "Your verification code is 123456");
System.out.println("Success: " + result.isSuccess());
```

### Configure a Provider
```java
import cloud.opencode.base.sms.config.*;

SmsConfig config = SmsConfig.builder()
    .providerType(SmsProviderType.ALIBABA)
    .accessKeyId("your-key-id")
    .accessKeySecret("your-key-secret")
    .signName("YourApp")
    .build();

OpenSms sms = OpenSms.of(config);
sms.send("13800138000", "Hello!");
```

### Template-Based Sending
```java
OpenSms sms = OpenSms.console()
    .registerTemplate("verify", "Your verification code is ${code}, valid for ${minutes} minutes.");

SmsResult result = sms.sendTemplate("verify", "13800138000",
    Map.of("code", "123456", "minutes", "5"));
```

### Batch Sending
```java
List<SmsResult> results = sms.sendToAll(
    List.of("13800138000", "13900139000", "13700137000"),
    "System maintenance at 3:00 AM"
);
```

### Template to Multiple Recipients
```java
List<SmsResult> results = sms.sendTemplateToAll(
    "verify",
    List.of("13800138000", "13900139000"),
    Map.of("code", "123456", "minutes", "5")
);
```

### Custom Provider
```java
SmsProvider customProvider = message -> {
    // Your custom sending logic
    return SmsResult.success(message.getPhoneNumber(), "custom-msg-id");
};

OpenSms sms = OpenSms.of(customProvider);
```

## Class Reference

### Root Package (`cloud.opencode.base.sms`)
| Class | Description |
|-------|-------------|
| `OpenSms` | Main facade for SMS operations (send, batch, template) |

### Config (`cloud.opencode.base.sms.config`)
| Class | Description |
|-------|-------------|
| `SmsConfig` | SMS provider configuration (credentials, sign name, region) |
| `ProviderConfig` | Provider-specific configuration parameters |
| `HttpSmsConfig` | Configuration for HTTP-based SMS gateways |
| `SecureSmsConfig` | Secure credential configuration with encryption support |
| `SmsProviderType` | Enum of supported SMS providers (ALIBABA, TENCENT, HUAWEI, HTTP, CONSOLE) |

### Message (`cloud.opencode.base.sms.message`)
| Class | Description |
|-------|-------------|
| `SmsMessage` | SMS message model (phone number, content, template ID, parameters) |
| `SmsResult` | Send result with success/failure status, message ID, and error info |

### Provider (`cloud.opencode.base.sms.provider`)
| Class | Description |
|-------|-------------|
| `SmsProvider` | Core provider interface (send, sendBatch, getName, isAvailable) |
| `SmsProviderFactory` | Factory for creating providers from configuration |
| `AbstractSmsProvider` | Base class for provider implementations with common logic |
| `ConsoleSmsProvider` | Console-based mock provider for development and testing |
| `AliSmsSender` | Alibaba Cloud SMS provider implementation |
| `TencentSmsSender` | Tencent Cloud SMS provider implementation |
| `HuaweiSmsSender` | Huawei Cloud SMS provider implementation |
| `HttpSmsProvider` | Generic HTTP-based SMS provider |

### Template (`cloud.opencode.base.sms.template`)
| Class | Description |
|-------|-------------|
| `SmsTemplate` | SMS message template with variable placeholders |
| `SmsTemplateRegistry` | Registry for managing and looking up templates |
| `TemplateManager` | Advanced template management with caching |
| `TemplateParser` | Parses template strings and substitutes variables |

### Validation (`cloud.opencode.base.sms.validation`)
| Class | Description |
|-------|-------------|
| `PhoneValidator` | Phone number format validator |
| `SmsRateLimiter` | Rate limiter to prevent SMS abuse |
| `SmsLogSanitizer` | Sanitizes sensitive data in log output |

### Batch (`cloud.opencode.base.sms.batch`)
| Class | Description |
|-------|-------------|
| `BatchSender` | Batch SMS sending with concurrency control |
| `BatchResult` | Aggregated result of a batch send operation |

### Utility (`cloud.opencode.base.sms.util`)
| Class | Description |
|-------|-------------|
| `PhoneNumberUtil` | Phone number parsing and formatting utilities |

### Exception (`cloud.opencode.base.sms.exception`)
| Class | Description |
|-------|-------------|
| `SmsException` | Base runtime exception for SMS errors |
| `SmsSendException` | Exception during SMS send operation |
| `SmsNetworkException` | Network-related SMS exception |
| `SmsRateLimitException` | Rate limit exceeded exception |
| `SmsTemplateException` | Template parsing or rendering exception |
| `SmsTimeoutException` | SMS operation timeout exception |
| `SmsErrorCode` | Enum of standardized SMS error codes |

## Requirements

- Java 25+
- No external dependencies

## License

Apache License 2.0

## Author

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
