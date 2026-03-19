# OpenCode Base Captcha

零依赖的验证码生成与验证库，支持多种验证码类型，适用于 JDK 25+。

## 功能特性

- 多种验证码类型（文本、算术、中文、GIF 动画）
- 交互式验证码类型（滑块、旋转、点选、图片选择）
- 可配置尺寸、字体、干扰线和难度
- 可插拔存储（内存、Redis）
- 频率限制和反机器人行为分析
- Base64 和图片输出渲染
- 基于时间和行为的验证
- 自适应难度调整
- 密封接口设计，保障类型安全

## Maven 依赖

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-captcha</artifactId>
    <version>1.0.0</version>
</dependency>
```

## API 概览

### 核心

| 类名 | 说明 |
|------|------|
| `OpenCaptcha` | 验证码操作的主入口 / 门面类 |
| `Captcha` | 不可变验证码数据记录（id、code、图片字节、类型） |
| `CaptchaConfig` | 验证码配置（类型、宽度、高度、长度、过期时间） |
| `CaptchaType` | 验证码类型枚举（TEXT、ARITHMETIC、CHINESE、GIF、SLIDER 等） |
| `ValidationResult` | 验证结果记录（成功、消息、元数据） |

### 生成器

| 类名 | 说明 |
|------|------|
| `CaptchaGenerator` | 验证码生成器密封接口 |
| `AbstractCaptchaGenerator` | 生成器抽象基类 |
| `SpecCaptchaGenerator` | 标准 PNG 文本验证码生成器 |
| `ImageCaptchaGenerator` | 基于图片的验证码生成器 |
| `GifCaptchaGenerator` | GIF 动画验证码生成器 |
| `ArithmeticCaptchaGenerator` | 数学表达式验证码生成器 |
| `ChineseCaptchaGenerator` | 中文字符验证码生成器 |

### 交互式生成器

| 类名 | 说明 |
|------|------|
| `SliderCaptchaGenerator` | 滑块拼图验证码生成器 |
| `RotateCaptchaGenerator` | 图片旋转验证码生成器 |
| `ClickCaptchaGenerator` | 点选文字验证码生成器 |
| `ImageSelectCaptchaGenerator` | 图片选择验证码生成器 |

### 渲染器

| 类名 | 说明 |
|------|------|
| `CaptchaRenderer` | 验证码输出渲染器接口 |
| `ImageCaptchaRenderer` | PNG 图片渲染器 |
| `GifCaptchaRenderer` | GIF 图片渲染器 |
| `Base64CaptchaRenderer` | Base64 Data URL 渲染器 |

### 验证器

| 类名 | 说明 |
|------|------|
| `CaptchaValidator` | 验证器接口 |
| `SimpleCaptchaValidator` | 基本文本匹配验证器 |
| `TimeBasedCaptchaValidator` | 带时间过期检查的验证器 |
| `BehaviorCaptchaValidator` | 基于行为分析的验证器 |
| `CaptchaRateLimiter` | 验证尝试频率限制器 |

### 存储

| 类名 | 说明 |
|------|------|
| `CaptchaStore` | 验证码数据存储接口 |
| `MemoryCaptchaStore` | 内存存储，带自动淘汰 |
| `RedisCaptchaStore` | 基于 Redis 的存储 |

### 安全

| 类名 | 说明 |
|------|------|
| `CaptchaSecurity` | 安全配置与执行 |
| `AntiBotStrategy` | 反机器人检测策略 |
| `BehaviorAnalyzer` | 用户行为分析，用于机器人检测 |

### 辅助

| 类名 | 说明 |
|------|------|
| `CaptchaChars` | 验证码生成字符集 |
| `CaptchaFontUtil` | 字体加载和管理 |
| `CaptchaNoiseUtil` | 干扰线和扭曲绘制工具 |
| `CaptchaDifficultyAdapter` | 自适应难度调整 |
| `CaptchaStrength` | 验证码难度强度枚举 |

### 编解码（内部）

| 类名 | 说明 |
|------|------|
| `GifEncoder` | GIF 图片编码器 |
| `LZWEncoder` | GIF 用 LZW 压缩编码器 |
| `NeuQuantEncoder` | NeuQuant 神经网络颜色量化器 |

### 异常

| 类名 | 说明 |
|------|------|
| `CaptchaException` | 验证码基础异常 |
| `CaptchaGenerationException` | 验证码生成失败异常 |
| `CaptchaVerifyException` | 验证码验证失败异常 |
| `CaptchaExpiredException` | 验证码已过期异常 |
| `CaptchaNotFoundException` | 验证码未找到异常 |
| `CaptchaRateLimitException` | 频率限制超出异常 |

## 快速开始

```java
import cloud.opencode.base.captcha.*;

// 简单文本验证码
Captcha captcha = OpenCaptcha.create();
String base64 = captcha.toBase64DataUrl();  // 用于 <img> 标签
String code = captcha.code();               // 用于校验的答案

// 算术验证码
Captcha mathCaptcha = OpenCaptcha.create(CaptchaConfig.builder()
    .type(CaptchaType.ARITHMETIC)
    .width(200)
    .height(80)
    .build());

// GIF 动画验证码
Captcha gifCaptcha = OpenCaptcha.create(CaptchaConfig.builder()
    .type(CaptchaType.GIF)
    .length(5)
    .build());

// 验证
boolean valid = OpenCaptcha.verify(captchaId, userInput);
```

## 环境要求

- Java 25+

## 许可证

Apache License 2.0
