# OpenCode Base SMS

**Java 25+ 短信工具库**

`opencode-base-sms` 提供统一的 API，通过多个提供商（阿里云、腾讯云、华为云、自定义 HTTP）发送短信，支持模板管理、批量发送、频率限制、手机号验证和日志脱敏。

## 功能特性

### 核心功能
- **统一门面**：单一 `OpenSms` 入口点处理所有短信操作
- **多提供商**：支持阿里云、腾讯云、华为云、自定义 HTTP 和控制台模拟
- **模板引擎**：注册和渲染带变量替换的短信模板
- **批量发送**：一次调用发送给多个收件人
- **提供商工厂**：从配置对象创建提供商

### 安全与验证
- **手机号验证**：发送前验证手机号格式
- **频率限制**：可配置的频率限制，防止短信滥用
- **日志脱敏**：在日志中屏蔽敏感数据（手机号、内容）
- **安全配置**：凭证的独立安全配置，支持加密

### 提供商支持
| 提供商 | 类 | 说明 |
|--------|-----|------|
| 控制台 | `ConsoleSmsProvider` | 用于开发/测试的模拟提供商 |
| 阿里云 | `AliSmsSender` | 阿里云短信集成 |
| 腾讯云 | `TencentSmsSender` | 腾讯云短信集成 |
| 华为云 | `HuaweiSmsSender` | 华为云短信集成 |
| HTTP | `HttpSmsProvider` | 通用 HTTP 短信网关 |

## 快速开始

### Maven 依赖
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-sms</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 发送简单短信
```java
import cloud.opencode.base.sms.OpenSms;
import cloud.opencode.base.sms.message.SmsResult;

// 开发环境使用控制台模拟
OpenSms sms = OpenSms.console();

SmsResult result = sms.send("13800138000", "您的验证码是 123456");
System.out.println("成功: " + result.isSuccess());
```

### 配置提供商
```java
import cloud.opencode.base.sms.config.*;

SmsConfig config = SmsConfig.builder()
    .providerType(SmsProviderType.ALIBABA)
    .accessKeyId("your-key-id")
    .accessKeySecret("your-key-secret")
    .signName("YourApp")
    .build();

OpenSms sms = OpenSms.of(config);
sms.send("13800138000", "你好！");
```

### 基于模板发送
```java
OpenSms sms = OpenSms.console()
    .registerTemplate("verify", "您的验证码是 ${code}，${minutes} 分钟内有效。");

SmsResult result = sms.sendTemplate("verify", "13800138000",
    Map.of("code", "123456", "minutes", "5"));
```

### 批量发送
```java
List<SmsResult> results = sms.sendToAll(
    List.of("13800138000", "13900139000", "13700137000"),
    "系统将于凌晨 3:00 进行维护"
);
```

### 模板批量发送
```java
List<SmsResult> results = sms.sendTemplateToAll(
    "verify",
    List.of("13800138000", "13900139000"),
    Map.of("code", "123456", "minutes", "5")
);
```

### 自定义提供商
```java
SmsProvider customProvider = message -> {
    // 自定义发送逻辑
    return SmsResult.success(message.getPhoneNumber(), "custom-msg-id");
};

OpenSms sms = OpenSms.of(customProvider);
```

## 类参考

### 根包 (`cloud.opencode.base.sms`)
| 类 | 说明 |
|----|------|
| `OpenSms` | 短信操作的主门面（发送、批量、模板） |

### 配置 (`cloud.opencode.base.sms.config`)
| 类 | 说明 |
|----|------|
| `SmsConfig` | 短信提供商配置（凭证、签名、区域） |
| `ProviderConfig` | 提供商特定的配置参数 |
| `HttpSmsConfig` | HTTP 短信网关配置 |
| `SecureSmsConfig` | 支持加密的安全凭证配置 |
| `SmsProviderType` | 支持的短信提供商枚举（ALIBABA、TENCENT、HUAWEI、HTTP、CONSOLE） |

### 消息 (`cloud.opencode.base.sms.message`)
| 类 | 说明 |
|----|------|
| `SmsMessage` | 短信消息模型（手机号、内容、模板 ID、参数） |
| `SmsResult` | 发送结果，包含成功/失败状态、消息 ID 和错误信息 |

### 提供商 (`cloud.opencode.base.sms.provider`)
| 类 | 说明 |
|----|------|
| `SmsProvider` | 核心提供商接口（send、sendBatch、getName、isAvailable） |
| `SmsProviderFactory` | 从配置创建提供商的工厂 |
| `AbstractSmsProvider` | 提供商实现的基类，包含通用逻辑 |
| `ConsoleSmsProvider` | 用于开发和测试的控制台模拟提供商 |
| `AliSmsSender` | 阿里云短信提供商实现 |
| `TencentSmsSender` | 腾讯云短信提供商实现 |
| `HuaweiSmsSender` | 华为云短信提供商实现 |
| `HttpSmsProvider` | 通用 HTTP 短信提供商 |

### 模板 (`cloud.opencode.base.sms.template`)
| 类 | 说明 |
|----|------|
| `SmsTemplate` | 带变量占位符的短信模板 |
| `SmsTemplateRegistry` | 管理和查找模板的注册表 |
| `TemplateManager` | 带缓存的高级模板管理 |
| `TemplateParser` | 解析模板字符串并替换变量 |

### 验证 (`cloud.opencode.base.sms.validation`)
| 类 | 说明 |
|----|------|
| `PhoneValidator` | 手机号格式验证器 |
| `SmsRateLimiter` | 防止短信滥用的频率限制器 |
| `SmsLogSanitizer` | 在日志输出中脱敏敏感数据 |

### 批量 (`cloud.opencode.base.sms.batch`)
| 类 | 说明 |
|----|------|
| `BatchSender` | 带并发控制的批量短信发送 |
| `BatchResult` | 批量发送操作的汇总结果 |

### 工具 (`cloud.opencode.base.sms.util`)
| 类 | 说明 |
|----|------|
| `PhoneNumberUtil` | 手机号解析和格式化工具 |

### 异常 (`cloud.opencode.base.sms.exception`)
| 类 | 说明 |
|----|------|
| `SmsException` | 短信错误的基础运行时异常 |
| `SmsSendException` | 短信发送操作异常 |
| `SmsNetworkException` | 网络相关的短信异常 |
| `SmsRateLimitException` | 频率限制超出异常 |
| `SmsTemplateException` | 模板解析或渲染异常 |
| `SmsTimeoutException` | 短信操作超时异常 |
| `SmsErrorCode` | 标准化短信错误码枚举 |

## 环境要求

- Java 25+
- 无外部依赖

## 许可证

Apache License 2.0

## 作者

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
