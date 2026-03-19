# OpenCode Base I18n

面向 JDK 25+ 的国际化消息解决方案。提供统一的消息获取、格式化和地区管理门面，支持可插拔的 SPI 提供者。

## Maven 依赖

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-i18n</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 功能特性

- 支持位置参数和命名参数的简洁消息获取
- 基于 ThreadLocal 的地区管理，支持作用域执行
- 多种消息提供者：ResourceBundle、缓存、链式、可重载
- 可插拔的 SPI，支持自定义地区解析器、消息提供者和格式化器
- 基于 Accept-Header 的 Web 应用地区解析
- 组合地区解析器，支持灵活的降级策略
- 带时区支持的地区上下文（不可变记录）
- 基于 ServiceLoader 的消息包自动发现
- 线程安全的全局配置

## 类参考

### 核心类

| 类 | 说明 |
|---|------|
| `OpenI18n` | 国际化操作主门面类：消息获取、地区管理和全局配置 |
| `MessageSource` | 支持地区的国际化消息获取接口 |
| `MessageBundle` | 特定地区的消息集合接口，支持键查找和父消息包 |
| `LocaleContext` | 封装 Locale 和 TimeZone 的不可变记录，用于上下文管理 |

### 注解

| 类 | 说明 |
|---|------|
| `@I18nMessage` | 标记消息键用于国际化的注解 |
| `@LocaleParam` | 标记方法参数作为地区来源的注解 |

### 格式化器

| 类 | 说明 |
|---|------|
| `DefaultMessageFormatter` | 使用 `java.text.MessageFormat` 的默认消息格式化器，支持位置参数 |
| `NamedParameterFormatter` | 支持 `${name}` 命名参数的消息格式化器 |
| `TemplateFormatter` | 基于模板的消息格式化器，支持表达式求值 |

### 提供者

| 类 | 说明 |
|---|------|
| `AbstractMessageProvider` | 消息提供者实现的基类 |
| `CachingMessageProvider` | 为任意消息提供者添加缓存的装饰器 |
| `ChainMessageProvider` | 支持降级回退的链式多提供者 |
| `ReloadableMessageProvider` | 支持消息文件热重载的提供者 |
| `ResourceBundleProvider` | 基于 Java ResourceBundle 属性文件的提供者 |

### 解析器

| 类 | 说明 |
|---|------|
| `AcceptHeaderLocaleResolver` | 从 HTTP Accept-Language 头解析地区 |
| `CompositeLocaleResolver` | 组合多个解析器，按优先级降级 |
| `FixedLocaleResolver` | 始终返回固定的预配置地区 |
| `ThreadLocalLocaleResolver` | 从 ThreadLocal 解析地区，用于线程级地区管理 |

### SPI 接口

| 类 | 说明 |
|---|------|
| `LocaleResolver` | 地区解析策略的 SPI 接口 |
| `MessageFormatter` | 支持位置参数和命名参数的消息格式化 SPI 接口 |
| `MessageProvider` | 从任意数据源加载消息的 SPI 接口 |
| `MessageBundleProvider` | 基于 ServiceLoader 的消息包发现 SPI 接口 |

### 支持类

| 类 | 说明 |
|---|------|
| `LocaleContextHolder` | 在 ThreadLocal 中持有当前 LocaleContext |
| `MessageSourceAccessor` | MessageSource 的便捷访问器，支持默认地区 |
| `ReloadableResourceBundle` | 支持热重载的 ResourceBundle 实现 |

### 异常

| 类 | 说明 |
|---|------|
| `OpenNoSuchMessageException` | 请求的消息键未找到时抛出 |

## 快速开始

```java
// 基本消息获取
String msg = OpenI18n.get("user.welcome", "张三");

// 指定地区获取消息
String englishMsg = OpenI18n.get("user.welcome", Locale.ENGLISH, "John");

// 使用命名参数
String msg = OpenI18n.get("order.confirmation", Map.of(
    "orderId", "12345",
    "total", 99.99
));

// 默认值回退
String msg = OpenI18n.getOrDefault("unknown.key", "默认消息");

// 作用域地区执行
OpenI18n.withLocale(Locale.JAPANESE, () -> {
    System.out.println(OpenI18n.get("greeting"));
});

// 带返回值的作用域地区执行
String result = OpenI18n.withLocale(Locale.FRENCH, () -> {
    return OpenI18n.get("product.name");
});

// 检查消息是否存在
if (OpenI18n.contains("error.notFound")) {
    // 处理
}

// 配置自定义提供者
OpenI18n.setMessageProvider(new ResourceBundleProvider("i18n/messages"));
OpenI18n.setDefaultLocale(Locale.CHINESE);
OpenI18n.setThrowOnMissingMessage(true);
```

## 环境要求

- Java 25+

## 开源协议

Apache License 2.0
