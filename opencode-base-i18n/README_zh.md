# OpenCode Base I18n

面向 JDK 25+ 的国际化消息解决方案。提供统一的消息获取门面、ICU 风格格式化（复数/选择/数字/日期）、类型安全消息键、区域降级链和消息包验证 — 无需外部依赖，无需 Spring。

## Maven 依赖

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-i18n</artifactId>
    <version>1.0.3</version>
</dependency>
```

## 功能特性

- **ICU 风格消息格式化** — `{name}`、`{count, plural, one{# 条} other{# 条}}`、`{gender, select, male{他} other{他们}}`、`{amount, number, #,##0.00}`、`{date, date, yyyy-MM-dd}` — 无单引号转义烦恼
- **CLDR 复数规则** — 无需 ICU4J 即可支持 50+ 语言（东亚、斯拉夫、阿拉伯、威尔士、爱尔兰……）
- **类型安全消息键** — `I18nKey` 接口和 `I18nEnum`，编译期键安全，自动推导点分 key
- **自定义区域降级链** — `ChainedLocaleFallback` Builder：pt-BR → pt-PT → es → en
- **消息包验证** — `BundleValidator` 检测缺失/多余键并报告覆盖率百分比
- **缺失键钩子** — 可插拔 `MissingKeyHandler`（no-op / logging / collecting / 组合）
- **多消息提供者** — ResourceBundle、缓存装饰器、链式、热重载
- **基于 ThreadLocal 的区域管理**，支持 `withLocale` 作用域执行
- **可插拔 SPI** — `LocaleResolver`、`MessageFormatter`、`MessageProvider`、`MessageBundleProvider`、`LocaleFallbackStrategy`
- **线程安全的全局配置**
- 无 Spring、无 ICU4J、无外部运行时依赖

## 类参考

### 核心类

| 类 | 说明 |
|---|------|
| `OpenI18n` | 主门面：消息获取、区域管理、全局配置、缺失键处理器 |
| `MessageSource` | 支持区域的国际化消息获取接口 |
| `MessageBundle` | 特定区域的消息集合接口 |
| `LocaleContext` | 封装 Locale 和 TimeZone 的不可变记录 |

### 格式化器

| 类 | 说明 |
|---|------|
| `IcuLikeFormatter` | ICU 风格格式化器：命名/位置参数、plural、select、number、date — 无单引号转义 |
| `DefaultMessageFormatter` | 使用 `java.text.MessageFormat` 的默认格式化器，支持位置参数 |
| `NamedParameterFormatter` | 支持 `${name}` 命名参数的格式化器 |

### 复数支持

| 类 | 说明 |
|---|------|
| `PluralRules` | 50+ 语言的 CLDR 复数规则，无 ICU4J 依赖 |
| `PluralCategory` | 枚举：ZERO / ONE / TWO / FEW / MANY / OTHER |
| `PluralFormatter` | 解析 `{count, plural, one{…} other{…}}` 分支并替换 `#` |
| `SelectFormatter` | 解析 `{gender, select, male{…} other{…}}` 分支 |

### 类型安全键

| 类 | 说明 |
|---|------|
| `I18nKey` | 接口：`key()` + 默认方法 `get(…)` / `getOrDefault(…)` |
| `I18nEnum` | 继承 `I18nKey`，自动从类名和枚举名推导点分 key |
| `I18nMessage` | 不可变记录：已解析 key + 区域 + 格式化文本 + 参数 |

### 区域降级

| 类 | 说明 |
|---|------|
| `LocaleFallbackStrategy` | 函数式接口：给定区域，返回有序降级链 |
| `ChainedLocaleFallback` | Builder 风格策略：精确匹配 → 语言 → 默认区域 |

### 消息包验证

| 类 | 说明 |
|---|------|
| `BundleValidator` | 跨区域比较键集合：缺失键、多余键、覆盖率% |
| `BundleValidationResult` | 不可变记录：missingKeys、extraKeys、coverage()、isComplete()、summary() |

### 缺失键处理

| 类 | 说明 |
|---|------|
| `MissingKeyHandler` | `@FunctionalInterface`；工厂方法：`noOp()`、`logging()`、`collecting()`、`andThen()` |
| `CollectingMissingKeyHandler` | 线程安全收集器，适用于测试和开发阶段 |

### 消息提供者

| 类 | 说明 |
|---|------|
| `ResourceBundleProvider` | 基于 Java ResourceBundle `.properties` 文件的提供者 |
| `CachingMessageProvider` | 为任意 `MessageProvider` 添加缓存的装饰器 |
| `ChainMessageProvider` | 支持降级回退的链式多提供者 |
| `ReloadableMessageProvider` | 支持热重载的消息提供者 |

### 区域解析器

| 类 | 说明 |
|---|------|
| `AcceptHeaderLocaleResolver` | 从 HTTP `Accept-Language` 头解析区域 |
| `CompositeLocaleResolver` | 组合多个解析器，按优先级降级 |
| `FixedLocaleResolver` | 始终返回固定的预配置区域 |
| `ThreadLocalLocaleResolver` | 通过 ThreadLocal 实现线程级区域管理 |

### SPI 接口

| 接口 | 说明 |
|-----|------|
| `LocaleResolver` | 区域解析策略 SPI |
| `MessageFormatter` | 消息格式化 SPI |
| `MessageProvider` | 从任意数据源加载消息的 SPI |
| `MessageBundleProvider` | 基于 ServiceLoader 的消息包发现 SPI |
| `LocaleFallbackStrategy` | 自定义区域降级链 SPI |

### 异常

| 类 | 说明 |
|---|------|
| `OpenI18nException` | 通用 i18n 异常；工厂方法 `formatError()`、`parseError()` |
| `OpenNoSuchMessageException` | 请求的消息键未找到时抛出 |

## 快速开始

### 基本用法

```java
// 配置 ResourceBundle 提供者
OpenI18n.setMessageProvider(new ResourceBundleProvider("i18n/messages"));
OpenI18n.setDefaultLocale(Locale.CHINESE);

// 简单消息获取
String msg = OpenI18n.get("user.welcome", "张三");

// 指定区域获取
String engMsg = OpenI18n.get("user.welcome", Locale.ENGLISH, "John");

// 命名参数
String msg = OpenI18n.get("order.confirmation", Map.of(
    "orderId", "12345",
    "total",   99.99
));

// 默认值回退
String msg = OpenI18n.getOrDefault("unknown.key", "默认消息");

// 作用域区域执行
OpenI18n.withLocale(Locale.JAPANESE, () -> {
    System.out.println(OpenI18n.get("greeting"));
});

// 检查消息是否存在
if (OpenI18n.contains("error.notFound")) {
    // 处理
}
```

### ICU 风格格式化（无单引号转义）

```java
// 命名参数 — 消息模板："你好，{name}！你有 {count} 条消息。"
String msg = OpenI18n.get("inbox.summary", Map.of("name", "张三", "count", 3));

// 复数 — 模板："{count, plural, one{# message} other{# messages}}"
// count=1 → "1 message"，count=5 → "5 messages"
String msg = OpenI18n.get("inbox.count", Map.of("count", 1));

// 精确匹配 — 模板："{count, plural, =0{No messages} one{# message} other{# messages}}"
String msg = OpenI18n.get("inbox.count", Map.of("count", 0));

// 选择分支 — 模板："{gender, select, male{他} female{她} other{他们}} 喜欢了你的帖子。"
String msg = OpenI18n.get("reaction", Map.of("gender", "female"));

// 数字格式 — 模板："总计：{amount, number, #,##0.00}"
String msg = OpenI18n.get("order.total", Map.of("amount", 1234567.89));

// 日期格式 — 模板："到期日：{date, date, yyyy-MM-dd}"
String msg = OpenI18n.get("license.expiry", Map.of("date", LocalDate.now()));
```

### 类型安全键（I18nEnum）

```java
// 定义类型安全消息键
public enum Messages implements I18nEnum {
    USER_WELCOME,       // → key: "messages.user.welcome"
    ORDER_CONFIRMATION  // → key: "messages.order.confirmation"
}

// 直接使用枚举
String msg  = Messages.USER_WELCOME.get("张三");
String msg  = Messages.ORDER_CONFIRMATION.get(Locale.ENGLISH, Map.of("orderId", "123"));
String safe = Messages.USER_WELCOME.getOrDefault("访客");
```

### 区域降级链

```java
// pt-BR → pt-PT → es → en
LocaleFallbackStrategy strategy = ChainedLocaleFallback.builder()
    .chain(Locale.forLanguageTag("pt-BR"),
        Locale.forLanguageTag("pt-PT"),
        Locale.forLanguageTag("es"),
        Locale.ENGLISH)
    .ultimateFallback(Locale.ENGLISH)
    .build();

OpenI18n.setFallbackStrategy(strategy);
```

### 消息包验证

```java
BundleValidator validator = new BundleValidator(provider);

// 验证法语包相对于英语包的完整性
BundleValidationResult result = validator.validate(Locale.ENGLISH, Locale.FRENCH);
System.out.println(result.summary());
// → "[en → fr] coverage=85.0% missing=3 extra=0"

if (!result.isComplete()) {
    result.missingKeys().forEach(k -> System.out.println("缺失：" + k));
}

// 一次验证所有支持的区域
Map<Locale, BundleValidationResult> all = validator.validateAll(Locale.ENGLISH);
all.forEach((locale, r) -> System.out.printf("%s: %.0f%%\n", locale, r.coverage() * 100));
```

### 缺失键处理器

```java
// 测试/开发阶段收集缺失键
CollectingMissingKeyHandler collector = MissingKeyHandler.collecting();
OpenI18n.setMissingKeyHandler(collector);

// ... 运行应用或测试 ...

if (!collector.isEmpty()) {
    System.out.println("缺失的键：" + collector.getMissingKeys());
}

// 生产环境记录日志
OpenI18n.setMissingKeyHandler(MissingKeyHandler.logging());

// 组合多个处理器
MissingKeyHandler combined = MissingKeyHandler.logging()
    .andThen(collector);
OpenI18n.setMissingKeyHandler(combined);
```

### CLDR 复数规则

```java
// 直接使用复数规则（不经过完整消息处理流程）
PluralRules rules = PluralRules.forLocale(Locale.forLanguageTag("ru"));
PluralCategory cat = rules.select(21);  // → ONE（21 → "день"）
PluralCategory cat = rules.select(12);  // → MANY（12 → "дней"）
```

## 环境要求

- Java 25+

## 开源协议

Apache License 2.0
