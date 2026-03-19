# Captcha 组件方案

## 1. 组件概述

### 1.1 设计目标

`opencode-base-captcha` 模块提供安全、灵活的验证码生成与验证能力。

**核心特性：**
- 多种验证码类型：数字、字母、字母数字、算术、中文、GIF 动图、特效
- 交互式验证码：滑块、点选、旋转、图片选择
- 可配置的安全策略和难度控制
- 基于 SecureRandom 的安全随机数
- 可插拔的存储（Memory/Redis）、渲染、验证策略
- 内置频率限制和行为分析
- 防机器识别：扭曲、干扰线、干扰点、动态效果

### 1.2 设计原则

| 原则 | 说明 |
|------|------|
| 安全性 | SecureRandom、防暴力破解、恒定时间比较、行为校验 |
| 灵活性 | 可配置字符集、长度、过期时间、图片尺寸、干扰强度 |
| 可扩展 | 密封接口 + 插件化架构，支持自定义生成器和验证器 |
| 零依赖 | 仅使用 JDK 原生 AWT/ImageIO API |
| 易用性 | OpenCaptcha 统一入口，静态方法和 Builder 模式并存 |

### 1.3 架构概览

```
+-----------------------------------------------------------------------+
|                         Application Layer                              |
|  +-------------------------------------------------------------------+|
|  |                     OpenCaptcha (门面入口)                          ||
|  |  create() | numeric() | gif() | slider() | generate() | validate()||
|  +-------------------------------------------------------------------+|
+-----------------------------------------------------------------------+
|                          Generator Layer                               |
|  +----------------+ +------------------+ +---------------------------+|
|  | ImageCaptcha   | | Arithmetic       | | Interactive               ||
|  | Generator      | | Generator        | | (Slider/Click/Rotate/     ||
|  | (Numeric/Alpha)| | ChineseCaptcha   | |  ImageSelect)             ||
|  +----------------+ | GifCaptcha       | +---------------------------+|
|                      | SpecCaptcha      |                              |
|                      +------------------+                              |
+-----------------------------------------------------------------------+
|                          Renderer Layer                                |
|  +------------------+ +------------------+ +-------------------------+|
|  | ImageCaptcha     | | GifCaptcha       | | Base64Captcha           ||
|  | Renderer (PNG)   | | Renderer (GIF)   | | Renderer (编码输出)      ||
|  +------------------+ +------------------+ +-------------------------+|
+-----------------------------------------------------------------------+
|                         Security Layer                                 |
|  +------------------+ +------------------+ +-------------------------+|
|  | CaptchaStore     | | Validator        | | RateLimiter             ||
|  | (Memory/Redis)   | | (Simple/TimeBased| | BehaviorAnalyzer        ||
|  +------------------+ |  /Behavior)      | | AntiBotStrategy         ||
|                       +------------------+ +-------------------------+|
+-----------------------------------------------------------------------+
|                          Support Layer                                 |
|  +------------------+ +------------------+ +-------------------------+|
|  | CaptchaFontUtil  | | CaptchaNoiseUtil | | GifEncoder/LZW/NeuQuant||
|  | CaptchaChars     | | CaptchaStrength  | | CaptchaDifficultyAdapter||
|  +------------------+ +------------------+ +-------------------------+|
+-----------------------------------------------------------------------+
```

### 1.4 模块依赖

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-captcha</artifactId>
    <version>${version}</version>
</dependency>
```

---

## 2. 包结构

```
cloud.opencode.base.captcha
├── OpenCaptcha.java              # 验证码主入口（门面类，含静态方法和实例方法）
├── Captcha.java                  # 验证码数据容器（Record）
├── CaptchaType.java              # 验证码类型枚举（10种）
├── CaptchaConfig.java            # 验证码配置（Builder 模式）
├── ValidationResult.java         # 验证结果（Record）
│
├── generator/                    # 生成器（密封接口体系）
│   ├── CaptchaGenerator.java    # 生成器密封接口
│   ├── AbstractCaptchaGenerator.java    # 生成器抽象基类
│   ├── ImageCaptchaGenerator.java       # 图形文本验证码（数字/字母/字母数字）
│   ├── ArithmeticCaptchaGenerator.java  # 算术验证码
│   ├── ChineseCaptchaGenerator.java     # 中文验证码
│   ├── GifCaptchaGenerator.java         # GIF 动态验证码
│   └── SpecCaptchaGenerator.java        # 特效验证码
│
├── interactive/                  # 交互式验证码
│   ├── SliderCaptchaGenerator.java      # 滑块验证码
│   ├── ClickCaptchaGenerator.java       # 点选验证码
│   ├── RotateCaptchaGenerator.java      # 旋转验证码
│   └── ImageSelectCaptchaGenerator.java # 图片选择验证码
│
├── renderer/                     # 渲染器
│   ├── CaptchaRenderer.java     # 渲染器接口
│   ├── ImageCaptchaRenderer.java       # PNG 静态图片渲染器
│   ├── GifCaptchaRenderer.java         # GIF 渲染器
│   └── Base64CaptchaRenderer.java      # Base64 输出渲染器
│
├── store/                        # 存储
│   ├── CaptchaStore.java        # 存储接口
│   ├── MemoryCaptchaStore.java  # 内存存储（ConcurrentHashMap + 自动清理）
│   └── RedisCaptchaStore.java   # Redis 存储（Builder 模式，框架无关）
│
├── validator/                    # 验证器
│   ├── CaptchaValidator.java    # 验证器接口
│   ├── SimpleCaptchaValidator.java      # 简单验证器
│   ├── TimeBasedCaptchaValidator.java   # 时间敏感验证器
│   ├── BehaviorCaptchaValidator.java    # 行为验证器
│   └── CaptchaRateLimiter.java          # 频率限制器
│
├── codec/                        # GIF 编解码
│   ├── GifEncoder.java          # GIF 编码器
│   ├── LZWEncoder.java          # LZW 压缩编码器
│   └── NeuQuantEncoder.java     # 颜色量化编码器
│
├── security/                     # 安全组件
│   ├── CaptchaSecurity.java     # 安全工具（ID生成/哈希/恒定时间比较）
│   ├── BehaviorAnalyzer.java    # 行为分析器（机器人检测）
│   └── AntiBotStrategy.java     # 反机器人策略（自适应难度）
│
├── exception/                    # 异常体系
│   ├── CaptchaException.java           # 异常基类
│   ├── CaptchaGenerationException.java # 生成异常
│   ├── CaptchaNotFoundException.java   # 未找到异常
│   ├── CaptchaExpiredException.java    # 过期异常
│   ├── CaptchaVerifyException.java     # 验证异常
│   └── CaptchaRateLimitException.java  # 频率限制异常
│
└── support/                      # 支持工具
    ├── CaptchaStrength.java     # 强度枚举（EASY/NORMAL/HARD/EXTREME）
    ├── CaptchaDifficultyAdapter.java   # 难度自适应器
    ├── CaptchaChars.java        # 字符集工具
    ├── CaptchaFontUtil.java     # 字体工具
    └── CaptchaNoiseUtil.java    # 干扰生成工具
```

---

## 3. 核心 API

### 3.1 Captcha（验证码数据容器）

`Captcha` 是一个不可变的 `record`，保存生成的验证码数据。

```java
public record Captcha(
    String id,                    // 唯一标识符
    CaptchaType type,             // 验证码类型
    byte[] imageData,             // 图像字节数据
    String answer,                // 正确答案
    Map<String, Object> metadata, // 附加元数据（交互式验证码使用）
    Instant createdAt,            // 创建时间
    Instant expiresAt             // 过期时间
) { ... }
```

**主要方法：**

| 方法 | 返回值 | 说明 |
|------|--------|------|
| `toBase64()` | `String` | 将图像数据转换为 Base64 字符串 |
| `toBase64DataUrl()` | `String` | 生成完整的 `data:image/...;base64,...` URI，可直接用于 `<img src>` |
| `getMimeType()` | `String` | 根据类型返回 MIME（GIF 返回 `image/gif`，其他返回 `image/png`） |
| `isExpired()` | `boolean` | 检查验证码是否已过期 |
| `getMetadata(String key)` | `<T> T` | 获取元数据值（泛型，可获取交互式验证码的额外数据） |
| `getWidth()` | `int` | 从元数据获取图像宽度 |
| `getHeight()` | `int` | 从元数据获取图像高度 |

**使用示例：**

```java
Captcha captcha = OpenCaptcha.create();

// 获取基本信息
String id = captcha.id();
String answer = captcha.answer();
CaptchaType type = captcha.type();

// 获取图像
String base64 = captcha.toBase64();
String dataUrl = captcha.toBase64DataUrl(); // data:image/png;base64,...

// 检查状态
boolean expired = captcha.isExpired();

// 获取交互式验证码的额外数据
Integer targetX = captcha.getMetadata("targetX");
```

### 3.2 CaptchaType（验证码类型枚举）

```java
public enum CaptchaType {
    NUMERIC,        // 纯数字
    ALPHA,          // 纯字母
    ALPHANUMERIC,   // 字母 + 数字
    ARITHMETIC,     // 算术表达式
    CHINESE,        // 中文字符
    GIF,            // GIF 动画
    SLIDER,         // 滑块拖动
    CLICK,          // 点选文字
    ROTATE,         // 旋转图片
    IMAGE_SELECT    // 图片选择
}
```

**判断方法：**

| 方法 | 返回值 | 说明 |
|------|--------|------|
| `isInteractive()` | `boolean` | 是否为交互式验证码（SLIDER/CLICK/ROTATE/IMAGE_SELECT） |
| `isTextBased()` | `boolean` | 是否为文本类验证码（NUMERIC/ALPHA/ALPHANUMERIC/ARITHMETIC/CHINESE） |

### 3.3 CaptchaConfig（验证码配置）

使用 Builder 模式构建不可变配置对象。

```java
public final class CaptchaConfig {
    // 默认值
    // width=160, height=60, length=4, type=ALPHANUMERIC
    // expireTime=5分钟, noiseLines=5, noiseDots=50
    // fontSize=32.0f, fontName="Arial", caseSensitive=false
    // gifFrameCount=10, gifDelay=100

    public static CaptchaConfig defaults();
    public static Builder builder();
    public Builder toBuilder();
    // ... getter 方法
}
```

**Builder 方法：**

| 方法 | 参数 | 说明 |
|------|------|------|
| `width(int)` | 图像宽度 | 默认 160 |
| `height(int)` | 图像高度 | 默认 60 |
| `length(int)` | 验证码字符长度 | 默认 4 |
| `type(CaptchaType)` | 验证码类型 | 默认 ALPHANUMERIC |
| `expireTime(Duration)` | 过期时间 | 默认 5 分钟 |
| `noiseLines(int)` | 干扰线数量 | 默认 5 |
| `noiseDots(int)` | 干扰点数量 | 默认 50 |
| `fontSize(float)` | 字体大小 | 默认 32.0f |
| `fontName(String)` | 字体名称 | 默认 "Arial" |
| `backgroundColor(Color)` | 背景颜色 | 默认白色 |
| `fontColors(Color...)` | 字体颜色数组 | 默认 5 种颜色 |
| `caseSensitive(boolean)` | 是否区分大小写 | 默认 false |
| `gifFrameCount(int)` | GIF 帧数 | 默认 10 |
| `gifDelay(int)` | GIF 帧延迟（毫秒） | 默认 100 |

**使用示例：**

```java
// 默认配置
CaptchaConfig config = CaptchaConfig.defaults();

// 自定义配置
CaptchaConfig config = CaptchaConfig.builder()
    .width(200)
    .height(80)
    .length(6)
    .type(CaptchaType.ALPHANUMERIC)
    .expireTime(Duration.ofMinutes(2))
    .noiseLines(8)
    .noiseDots(80)
    .fontSize(36.0f)
    .caseSensitive(true)
    .build();

// 基于现有配置修改
CaptchaConfig newConfig = config.toBuilder()
    .type(CaptchaType.ARITHMETIC)
    .build();
```

### 3.4 OpenCaptcha（主入口）

`OpenCaptcha` 同时提供静态工厂方法（快速使用）和 Builder 实例方法（高级使用）。

#### 静态工厂方法（无状态，直接生成）

| 方法 | 返回值 | 说明 |
|------|--------|------|
| `create()` | `Captcha` | 使用默认配置创建验证码 |
| `create(CaptchaConfig config)` | `Captcha` | 使用指定配置创建验证码 |
| `create(CaptchaType type)` | `Captcha` | 创建指定类型的验证码 |
| `numeric()` | `Captcha` | 创建纯数字验证码 |
| `alpha()` | `Captcha` | 创建纯字母验证码 |
| `alphanumeric()` | `Captcha` | 创建字母数字验证码 |
| `arithmetic()` | `Captcha` | 创建算术验证码 |
| `chinese()` | `Captcha` | 创建中文验证码 |
| `gif()` | `Captcha` | 创建 GIF 动态验证码 |
| `slider()` | `Captcha` | 创建滑块验证码 |
| `click()` | `Captcha` | 创建点选验证码 |
| `rotate()` | `Captcha` | 创建旋转验证码 |

#### 实例方法（Builder 构建后使用，带存储和验证）

| 方法 | 返回值 | 说明 |
|------|--------|------|
| `generate()` | `Captcha` | 生成验证码并存储到 Store |
| `generate(CaptchaConfig config)` | `Captcha` | 使用指定配置生成并存储 |
| `validate(String id, String answer)` | `ValidationResult` | 验证答案 |
| `render(Captcha, OutputStream)` | `void` | 将验证码渲染到输出流 |
| `getStore()` | `CaptchaStore` | 获取存储实例 |
| `getConfig()` | `CaptchaConfig` | 获取配置 |

#### Builder

| 方法 | 说明 |
|------|------|
| `store(CaptchaStore)` | 设置存储（默认 MemoryCaptchaStore） |
| `config(CaptchaConfig)` | 设置配置（默认 CaptchaConfig.defaults()） |
| `type(CaptchaType)` | 设置验证码类型 |
| `build()` | 构建 OpenCaptcha 实例 |

**使用示例：**

```java
// === 方式一：静态方法快速生成（不带存储） ===
Captcha captcha = OpenCaptcha.create();
String dataUrl = captcha.toBase64DataUrl();

Captcha numeric = OpenCaptcha.numeric();
Captcha gif = OpenCaptcha.gif();
Captcha slider = OpenCaptcha.slider();

// 自定义配置
Captcha captcha = OpenCaptcha.create(CaptchaConfig.builder()
    .type(CaptchaType.ARITHMETIC)
    .width(200)
    .height(80)
    .build());

// === 方式二：Builder 构建实例（带存储和验证） ===
OpenCaptcha openCaptcha = OpenCaptcha.builder()
    .store(CaptchaStore.memory())
    .config(CaptchaConfig.builder()
        .type(CaptchaType.ALPHANUMERIC)
        .length(6)
        .expireTime(Duration.ofMinutes(2))
        .build())
    .build();

// 生成并自动存储
Captcha captcha = openCaptcha.generate();

// 验证答案
ValidationResult result = openCaptcha.validate(captcha.id(), userInput);
if (result.success()) {
    // 验证通过
} else {
    // result.message() 获取失败原因
    // result.code() 获取结果代码
}
```

### 3.5 ValidationResult（验证结果）

```java
public record ValidationResult(
    boolean success,   // 是否成功
    String message,    // 结果消息
    ResultCode code    // 结果代码
) { ... }
```

**ResultCode 枚举值：**

| 值 | 说明 |
|----|------|
| `SUCCESS` | 验证成功 |
| `NOT_FOUND` | 验证码不存在 |
| `EXPIRED` | 验证码已过期 |
| `MISMATCH` | 答案不匹配 |
| `RATE_LIMITED` | 超过频率限制 |
| `INVALID_INPUT` | 输入无效 |
| `SUSPICIOUS_BEHAVIOR` | 检测到可疑行为 |

**工厂方法：**

```java
ValidationResult.ok()                 // 成功
ValidationResult.notFound()           // 未找到
ValidationResult.expired()            // 已过期
ValidationResult.mismatch()           // 答案不匹配
ValidationResult.rateLimited()        // 频率限制
ValidationResult.invalidInput()       // 输入无效
ValidationResult.suspiciousBehavior() // 可疑行为

// 判断方法
result.success()  // true 表示成功
result.isFailed() // true 表示失败
```

### 3.6 CaptchaGenerator（生成器密封接口）

```java
public sealed interface CaptchaGenerator
    permits ImageCaptchaGenerator, ArithmeticCaptchaGenerator,
            ChineseCaptchaGenerator, GifCaptchaGenerator, SpecCaptchaGenerator,
            SliderCaptchaGenerator, ClickCaptchaGenerator, RotateCaptchaGenerator,
            ImageSelectCaptchaGenerator {

    Captcha generate(CaptchaConfig config);
    default Captcha generate();  // 使用默认配置
    CaptchaType getType();

    // 工厂方法
    static CaptchaGenerator forType(CaptchaType type);
    static CaptchaGenerator numeric();
    static CaptchaGenerator alpha();
    static CaptchaGenerator alphanumeric();
    static CaptchaGenerator arithmetic();
    static CaptchaGenerator chinese();
    static CaptchaGenerator gif();
}
```

**使用示例：**

```java
// 通过类型获取生成器
CaptchaGenerator generator = CaptchaGenerator.forType(CaptchaType.ALPHANUMERIC);
Captcha captcha = generator.generate(CaptchaConfig.defaults());

// 快捷工厂方法
CaptchaGenerator numericGen = CaptchaGenerator.numeric();
CaptchaGenerator arithmeticGen = CaptchaGenerator.arithmetic();
```

**各生成器实现：**

| 类 | 说明 | 支持类型 |
|----|------|----------|
| `ImageCaptchaGenerator` | 静态图片验证码 | NUMERIC, ALPHA, ALPHANUMERIC |
| `ArithmeticCaptchaGenerator` | 算术表达式验证码 | ARITHMETIC |
| `ChineseCaptchaGenerator` | 中文字符验证码 | CHINESE |
| `GifCaptchaGenerator` | GIF 动画验证码 | GIF |
| `SpecCaptchaGenerator` | 特效验证码 | ALPHANUMERIC（含特效） |
| `SliderCaptchaGenerator` | 滑块验证码 | SLIDER |
| `ClickCaptchaGenerator` | 点选文字验证码 | CLICK |
| `RotateCaptchaGenerator` | 旋转验证码 | ROTATE |
| `ImageSelectCaptchaGenerator` | 图片选择验证码 | IMAGE_SELECT |

---

## 4. 存储接口

### 4.1 CaptchaStore（存储接口）

```java
public interface CaptchaStore {
    void store(String id, String answer, Duration ttl);
    Optional<String> get(String id);
    Optional<String> getAndRemove(String id);  // 一次性获取并删除
    void remove(String id);
    boolean exists(String id);
    void clearExpired();
    void clearAll();
    int size();

    // 工厂方法
    static CaptchaStore memory();              // 创建内存存储
    static CaptchaStore memory(int maxSize);   // 指定最大容量
}
```

### 4.2 MemoryCaptchaStore（内存存储）

基于 `ConcurrentHashMap` 的线程安全内存存储，实现 `AutoCloseable`。

```java
public final class MemoryCaptchaStore implements CaptchaStore, AutoCloseable {
    public MemoryCaptchaStore();               // 默认容量
    public MemoryCaptchaStore(int maxSize);     // 指定最大容量
    public void shutdown();                     // 关闭清理调度器
    public void close();                        // 等同于 shutdown()
}
```

**使用示例：**

```java
CaptchaStore store = CaptchaStore.memory();
store.store("captcha-001", "abc123", Duration.ofMinutes(5));

Optional<String> answer = store.get("captcha-001");
Optional<String> removed = store.getAndRemove("captcha-001"); // 获取后删除
```

### 4.3 RedisCaptchaStore（Redis 存储）

框架无关的 Redis 存储，通过函数式接口注入 Redis 操作，兼容 Spring RedisTemplate、Jedis、Lettuce 等。

```java
public final class RedisCaptchaStore implements CaptchaStore {

    // Redis 写入接口（函数式）
    public interface RedisSetter {
        void set(String key, String value, Duration ttl);
    }

    // Builder
    public static Builder builder();

    public static class Builder {
        public Builder keyPrefix(String keyPrefix);                     // 键前缀
        public Builder setter(RedisSetter setter);                      // 写入函数
        public Builder getter(Function<String, String> getter);         // 读取函数
        public Builder deleter(Consumer<String> deleter);               // 删除函数
        public Builder existsChecker(Function<String, Boolean> checker); // 存在检查函数
        public RedisCaptchaStore build();
    }

    public String getKeyPrefix();
}
```

**使用示例：**

```java
// Spring RedisTemplate 集成
StringRedisTemplate redisTemplate = ...;
RedisCaptchaStore store = RedisCaptchaStore.builder()
    .keyPrefix("captcha:")
    .setter((key, value, ttl) ->
        redisTemplate.opsForValue().set(key, value, ttl))
    .getter(key ->
        redisTemplate.opsForValue().get(key))
    .deleter(key ->
        redisTemplate.delete(key))
    .existsChecker(key ->
        Boolean.TRUE.equals(redisTemplate.hasKey(key)))
    .build();

// Jedis 集成
JedisPool pool = ...;
RedisCaptchaStore store = RedisCaptchaStore.builder()
    .keyPrefix("captcha:")
    .setter((key, value, ttl) -> {
        try (Jedis jedis = pool.getResource()) {
            jedis.setex(key, ttl.toSeconds(), value);
        }
    })
    .getter(key -> {
        try (Jedis jedis = pool.getResource()) {
            return jedis.get(key);
        }
    })
    .deleter(key -> {
        try (Jedis jedis = pool.getResource()) {
            jedis.del(key);
        }
    })
    .build();

// 使用自定义存储
OpenCaptcha captcha = OpenCaptcha.builder()
    .store(store)
    .build();
```

---

## 5. 验证器

### 5.1 CaptchaValidator（验证器接口）

```java
public interface CaptchaValidator {
    ValidationResult validate(String id, String answer);
    ValidationResult validate(String id, String answer, boolean caseSensitive);

    static CaptchaValidator simple(CaptchaStore store);     // 简单验证器
    static CaptchaValidator timeBased(CaptchaStore store);   // 时间感知验证器
}
```

### 5.2 SimpleCaptchaValidator

基础验证器，从 Store 取出答案并比较。

```java
public final class SimpleCaptchaValidator implements CaptchaValidator {
    public SimpleCaptchaValidator(CaptchaStore store);
    public ValidationResult validate(String id, String answer);
    public ValidationResult validate(String id, String answer, boolean caseSensitive);
}
```

### 5.3 TimeBasedCaptchaValidator

在简单验证基础上增加创建时间检查，防止过快或过慢响应。

```java
public final class TimeBasedCaptchaValidator implements CaptchaValidator {
    public TimeBasedCaptchaValidator(CaptchaStore store);
    public void recordCreation(String id);     // 记录验证码创建时间
    public void clearOldRecords();             // 清理旧记录
}
```

### 5.4 BehaviorCaptchaValidator

带行为分析的高级验证器，检测机器人行为模式。

```java
public final class BehaviorCaptchaValidator implements CaptchaValidator {
    public BehaviorCaptchaValidator(CaptchaStore store);
    public BehaviorCaptchaValidator(CaptchaStore store, BehaviorAnalyzer analyzer);
    public void recordCreation(String captchaId, String clientId);
    public ValidationResult validate(String id, String answer, String clientId);
    public ValidationResult validate(String id, String answer, String clientId, boolean caseSensitive);
    public BehaviorAnalyzer getAnalyzer();
    public void clearOldRecords();
}
```

### 5.5 CaptchaRateLimiter（频率限制器）

```java
public final class CaptchaRateLimiter {
    public CaptchaRateLimiter();                          // 默认：10次/分钟
    public CaptchaRateLimiter(int maxRequests, Duration window);
    public boolean isAllowed(String clientId);             // 是否允许请求
    public int getRemainingRequests(String clientId);       // 剩余可用次数
    public Duration getTimeUntilReset(String clientId);    // 距离重置的时间
    public void clear(String clientId);                    // 清除指定客户端记录
    public void clearExpired();                            // 清除过期记录
}
```

**使用示例：**

```java
CaptchaRateLimiter limiter = new CaptchaRateLimiter(5, Duration.ofMinutes(1));
String clientIp = request.getRemoteAddr();

if (!limiter.isAllowed(clientIp)) {
    throw new CaptchaRateLimitException(clientIp,
        limiter.getTimeUntilReset(clientIp));
}

int remaining = limiter.getRemainingRequests(clientIp);
```

---

## 6. 安全组件

### 6.1 CaptchaSecurity（安全工具）

```java
public final class CaptchaSecurity {
    public static String generateSecureId();                    // 生成安全的随机 ID
    public static String generateSecureToken(int length);       // 生成指定长度的安全令牌
    public static String hashAnswer(String answer, String salt); // 哈希答案
    public static boolean verifyHashedAnswer(                    // 验证哈希答案
        String answer, String hashedAnswer, String salt);
    public static boolean constantTimeEquals(String a, String b); // 恒定时间字符串比较
    public static String generateSalt();                         // 生成盐值
}
```

### 6.2 BehaviorAnalyzer（行为分析器）

```java
public final class BehaviorAnalyzer {
    // 分析结果枚举
    public enum AnalysisResult {
        NORMAL, SUSPICIOUS, BOT
    }

    // 客户端行为记录
    public static final class ClientBehavior {
        public Instant getLastActivity();
        public int getTotalAttempts();
        public int getRecentFailures();
    }

    public AnalysisResult analyze(String clientId, Duration responseTime, boolean success);
    public ClientBehavior getBehavior(String clientId);
    public void clear(String clientId);
    public void clearOld();
}
```

### 6.3 AntiBotStrategy（反机器人策略）

根据客户端行为自适应调整验证码难度和类型。

```java
public final class AntiBotStrategy {
    public AntiBotStrategy(BehaviorAnalyzer analyzer);
    public static AntiBotStrategy create();

    public AntiBotStrategy withBaseStrength(CaptchaStrength strength);
    public AntiBotStrategy withBaseType(CaptchaType type);

    public CaptchaStrength recommendStrength(String clientId);  // 推荐难度
    public CaptchaType recommendType(String clientId);          // 推荐类型
    public boolean shouldBlock(String clientId);                 // 是否应拦截
    public BehaviorAnalyzer getAnalyzer();
}
```

**使用示例：**

```java
AntiBotStrategy strategy = AntiBotStrategy.create()
    .withBaseStrength(CaptchaStrength.NORMAL)
    .withBaseType(CaptchaType.ALPHANUMERIC);

// 根据客户端行为获取推荐
CaptchaStrength strength = strategy.recommendStrength(clientId);
CaptchaType type = strategy.recommendType(clientId);

if (strategy.shouldBlock(clientId)) {
    // 直接拦截
}

// 应用推荐到配置
CaptchaConfig config = strength.applyTo(CaptchaConfig.builder().type(type)).build();
Captcha captcha = OpenCaptcha.create(config);
```

---

## 7. 支持工具

### 7.1 CaptchaStrength（难度枚举）

```java
public enum CaptchaStrength {
    EASY, NORMAL, HARD, EXTREME;

    public int getNoiseLines();      // 获取干扰线数量
    public int getNoiseDots();       // 获取干扰点数量
    public float getFontSize();      // 获取字体大小
    public CaptchaConfig.Builder applyTo(CaptchaConfig.Builder builder); // 应用到配置
    public CaptchaConfig toConfig(); // 转为配置
}
```

### 7.2 CaptchaDifficultyAdapter（自适应难度）

根据客户端历史记录自动调整难度。

```java
public final class CaptchaDifficultyAdapter {
    public CaptchaStrength getStrength(String clientId);
    public CaptchaConfig getConfig(String clientId);
    public CaptchaConfig getConfig(String clientId, CaptchaConfig baseConfig);
    public void recordAttempt(String clientId, boolean success);
    public void reset(String clientId);
    public double getGlobalFailureRate();
    public int getFailureCount(String clientId);
    public int getTrackedClientCount();
    public void clearAll();
}
```

### 7.3 CaptchaChars（字符集工具）

```java
public final class CaptchaChars {
    public static final char[] NUMERIC;        // "0123456789"
    public static final char[] ALPHA_LOWER;    // 排除易混淆字符
    public static final char[] ALPHA_UPPER;    // 排除易混淆字符
    public static final char[] ALPHA;          // 大小写字母
    public static final char[] ALPHANUMERIC;   // 字母数字混合

    public static String generate(CaptchaType type, int length);
    public static String generateFromChars(char[] chars, int length);
    public static String generateChinese(int length);
    public static String[] generateArithmetic();  // 返回 [表达式, 答案]
    public static Random getRandom();
    public static int randomInt(int bound);
    public static int randomInt(int min, int max);
}
```

### 7.4 CaptchaFontUtil（字体工具）

```java
public final class CaptchaFontUtil {
    public static Font getFont(CaptchaConfig config);
    public static Font getFont(String fontName, float fontSize);
    public static Font getRandomStyleFont(Font font);
    public static Font getRotatedFont(Font font, double angle);
    public static Font getChineseFont(float fontSize);
    public static Color getRandomColor(CaptchaConfig config);
    public static Color randomColor();
    public static Color randomLightColor();
    public static Color randomDarkColor();
}
```

### 7.5 CaptchaNoiseUtil（干扰工具）

```java
public final class CaptchaNoiseUtil {
    public static void drawNoiseLines(Graphics2D g, CaptchaConfig config);
    public static void drawCurveLines(Graphics2D g, CaptchaConfig config);
    public static void drawCubicCurveLines(Graphics2D g, CaptchaConfig config);
    public static void drawNoiseDots(Graphics2D g, CaptchaConfig config);
    public static void drawBackgroundNoise(Graphics2D g, CaptchaConfig config);
    public static void shear(Graphics2D g, CaptchaConfig config);
    public static void drawGradientBackground(Graphics2D g, CaptchaConfig config);
    public static void drawInterferencePattern(Graphics2D g, CaptchaConfig config);
}
```

---

## 8. 渲染器

### 8.1 CaptchaRenderer（渲染器接口）

```java
public interface CaptchaRenderer {
    void render(Captcha captcha, OutputStream out) throws IOException;
    byte[] renderToBytes(Captcha captcha);
    String renderToBase64(Captcha captcha);
    String getContentType();
}
```

### 8.2 渲染器实现

| 类 | Content-Type | 说明 |
|----|-------------|------|
| `ImageCaptchaRenderer` | `image/png` | PNG 静态图片渲染 |
| `GifCaptchaRenderer` | `image/gif` | GIF 动画渲染 |
| `Base64CaptchaRenderer` | 根据类型自动选择 | Base64 编码输出 |

```java
// 在 Servlet 中使用
CaptchaRenderer renderer = new ImageCaptchaRenderer();
response.setContentType(renderer.getContentType());
renderer.render(captcha, response.getOutputStream());

// 获取 Base64
Base64CaptchaRenderer base64Renderer = new Base64CaptchaRenderer();
String base64 = base64Renderer.renderToBase64(captcha);
```

---

## 9. 异常体系

```
RuntimeException
└── CaptchaException                    # 验证码异常基类
    ├── CaptchaGenerationException      # 生成失败（字体缺失、图片创建失败等）
    │   └── getType(): CaptchaType      # 获取失败的验证码类型
    ├── CaptchaNotFoundException        # 验证码不存在
    │   └── getCaptchaId(): String      # 获取验证码 ID
    ├── CaptchaExpiredException         # 验证码已过期
    │   └── getCaptchaId(): String      # 获取验证码 ID
    ├── CaptchaVerifyException          # 验证失败
    │   ├── getCaptchaId(): String      # 获取验证码 ID
    │   └── getProvidedAnswer(): String # 获取用户提供的答案
    └── CaptchaRateLimitException       # 频率限制
        ├── getClientId(): String       # 获取客户端标识
        └── getRetryAfter(): Duration   # 获取重试等待时间
```

---

## 10. GIF 编解码

### 10.1 GifEncoder

```java
public final class GifEncoder {
    public void setDelay(int ms);              // 设置帧延迟（毫秒）
    public void setDispose(int code);          // 设置帧处理方式
    public void setRepeat(int iter);           // 设置循环次数（0=无限）
    public void setQuality(int quality);       // 设置颜色量化质量（1-30，默认10）
    public boolean start(OutputStream os);     // 开始编码
    public boolean addFrame(BufferedImage im); // 添加帧
    public boolean finish();                   // 完成编码
}
```

**使用示例：**

```java
GifEncoder encoder = new GifEncoder();
encoder.start(outputStream);
encoder.setDelay(100);
encoder.setRepeat(0); // 无限循环

for (BufferedImage frame : frames) {
    encoder.addFrame(frame);
}

encoder.finish();
```

---

## 11. 完整使用示例

### 11.1 基础使用

```java
// 1. 快速生成验证码
Captcha captcha = OpenCaptcha.create();
String id = captcha.id();
String dataUrl = captcha.toBase64DataUrl();

// 返回给前端
return Map.of(
    "captchaId", id,
    "image", dataUrl   // data:image/png;base64,...
);
```

### 11.2 各类型验证码

```java
// 数字验证码
Captcha numeric = OpenCaptcha.numeric();

// 算术验证码 (答案为计算结果)
Captcha arithmetic = OpenCaptcha.arithmetic();

// GIF 动态验证码
Captcha gif = OpenCaptcha.gif();

// 中文验证码
Captcha chinese = OpenCaptcha.chinese();

// 滑块验证码（交互式）
Captcha slider = OpenCaptcha.slider();

// 点选验证码（交互式）
Captcha click = OpenCaptcha.click();

// 旋转验证码（交互式）
Captcha rotate = OpenCaptcha.rotate();
```

### 11.3 带存储的完整流程

```java
// 构建实例
OpenCaptcha openCaptcha = OpenCaptcha.builder()
    .store(CaptchaStore.memory(1000))
    .config(CaptchaConfig.builder()
        .type(CaptchaType.ALPHANUMERIC)
        .length(6)
        .width(200)
        .height(80)
        .expireTime(Duration.ofMinutes(3))
        .noiseLines(8)
        .caseSensitive(false)
        .build())
    .build();

// 生成（自动存储）
Captcha captcha = openCaptcha.generate();

// 验证
ValidationResult result = openCaptcha.validate(captcha.id(), userInput);
if (result.success()) {
    // 验证通过
} else {
    switch (result.code()) {
        case NOT_FOUND -> handleNotFound();
        case EXPIRED -> handleExpired();
        case MISMATCH -> handleMismatch();
        case RATE_LIMITED -> handleRateLimited();
        default -> handleError();
    }
}
```

### 11.4 行为分析集成

```java
CaptchaStore store = CaptchaStore.memory();
BehaviorCaptchaValidator validator = new BehaviorCaptchaValidator(store);

// 生成验证码时记录
validator.recordCreation(captchaId, clientId);

// 验证时带上客户端标识
ValidationResult result = validator.validate(captchaId, answer, clientId);
```

### 11.5 频率限制

```java
CaptchaRateLimiter limiter = new CaptchaRateLimiter(5, Duration.ofMinutes(1));

// 检查是否允许
if (!limiter.isAllowed(clientIp)) {
    Duration retryAfter = limiter.getTimeUntilReset(clientIp);
    throw new CaptchaRateLimitException(clientIp, retryAfter);
}

// 生成验证码
Captcha captcha = OpenCaptcha.create();
```

### 11.6 自适应难度

```java
CaptchaDifficultyAdapter adapter = new CaptchaDifficultyAdapter();

// 获取推荐难度
CaptchaStrength strength = adapter.getStrength(clientId);
CaptchaConfig config = adapter.getConfig(clientId);

// 生成验证码
Captcha captcha = OpenCaptcha.create(config);

// 记录验证结果
adapter.recordAttempt(clientId, verificationSuccess);
```

---

## 12. 线程安全

| 组件 | 线程安全 | 说明 |
|------|---------|------|
| `OpenCaptcha`（静态方法） | 是 | 无状态静态方法 |
| `OpenCaptcha`（实例） | 是 | 内部状态不可变 |
| `CaptchaGenerator` 各实现 | 是 | 无状态或每次生成新实例 |
| `MemoryCaptchaStore` | 是 | 基于 ConcurrentHashMap |
| `RedisCaptchaStore` | 是 | 依赖 Redis 原子操作 |
| `CaptchaConfig` | 是 | 不可变对象 |
| `Captcha` | 是 | 不可变 Record |
| `CaptchaRateLimiter` | 是 | 基于 ConcurrentHashMap |
| `BehaviorAnalyzer` | 是 | 内部同步 |

---

## 13. 版本信息

| 属性 | 值 |
|------|-----|
| 模块名 | opencode-base-captcha |
| 最低 JDK | 25 |
| 核心依赖 | opencode-base-core |
| 第三方依赖 | 无（纯 JDK AWT/ImageIO） |
