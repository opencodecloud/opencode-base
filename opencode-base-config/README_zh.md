# OpenCode Base Config

灵活的配置管理库，支持多配置源、类型转换、热重载、验证和 JDK 25+ 特性，适用于 Java 应用。

## 功能特性

- 多种配置源（properties、YAML、环境变量、系统属性、命令行、HTTP、内存）
- 支持优先级的复合配置源
- 自动类型转换，可扩展的转换器注册表
- 基于文件监听的热重载（虚拟线程驱动）
- 配置变更事件和监听器
- 占位符解析和表达式求值
- Bean 绑定（Java Bean 和 Record）
- 配置验证（必填、范围、正则模式）
- 多环境配置支持（dev、test、prod）
- 多租户配置管理
- 加密配置处理
- 基于 SPI 的可扩展配置源和转换器
- JDK 25 特性：密封类型、Record、ScopedValue、虚拟线程

## Maven 依赖

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-config</artifactId>
    <version>1.0.3</version>
</dependency>
```

## v1.0.3 新特性

- **松绑定（Relaxed Binding）** — `database.max-pool-size` 自动匹配 `DATABASE_MAX_POOL_SIZE`、`database.maxPoolSize` 等。通过 `ConfigBuilder.enableRelaxedBinding()` 启用。
- **ConfigDump** — 导出全部生效配置，敏感值自动掩码（`***`）。默认模式：password、secret、token、key、credential、auth、bearer。
- **ConfigDiff** — 对比两个配置快照，返回 `List<ConfigChangeEvent>`，支持格式化输出。
- **POJO @DefaultValue** — `@DefaultValue` 从 Record 扩展到 POJO 字段，`ConfigBinder` 支持。
- **验证汇总** — `ValidationResult.merge()` 将所有验证错误收集到一份报告中。
- **安全加固** — SSRF DNS 重绑定防护（IP 固定）、敏感值从 toString/异常中脱敏、PlaceholderResolver 深度限制。

## API 概览

### 核心

| 类名 | 说明 |
|------|------|
| `Config` | 核心配置接口（get、getAs、getOrDefault、subscribe） |
| `OpenConfig` | 全局配置管理器门面 |
| `ConfigBuilder` | 流式构建器，用于创建 Config 实例 |
| `RelaxedKeyResolver` **[v1.0.3]** | 松绑定解析：归一化、变体生成、跨命名约定解析 |
| `ConfigDump` **[v1.0.3]** | 配置导出与敏感值掩码 |
| `ConfigDiff` **[v1.0.3]** | 配置快照差异对比 |
| `ConfigChangeEvent` | 配置变更事件记录（key、changeType、timestamp） |
| `ConfigChangeType` | 变更类型枚举（ADDED、MODIFIED、REMOVED） |
| `ConfigListener` | 配置变更监听器接口 |
| `OpenConfigException` | 配置异常（值已脱敏） |

### 配置源

| 类名 | 说明 |
|------|------|
| `ConfigSource` | 配置源接口（load、reload、priority） |
| `PropertiesConfigSource` | Java .properties 文件配置源 |
| `YamlConfigSource` | YAML 文件配置源（需要 opencode-base-yml） |
| `EnvironmentConfigSource` | 操作系统环境变量配置源 |
| `SystemPropertiesConfigSource` | JVM 系统属性配置源 |
| `CommandLineConfigSource` | 命令行参数配置源 |
| `InMemoryConfigSource` | 内存键值对配置源 |
| `CompositeConfigSource` | 按优先级排序的复合配置源 |

### 类型转换器

| 类名 | 说明 |
|------|------|
| `ConfigConverter` | 类型转换器接口 |
| `ConverterRegistry` | 转换器注册表，内置类型支持 |
| `SpiConverterRegistry` | 基于 SPI 的自动发现转换器注册表 |
| `StringConverter` | 字符串恒等转换器 |
| `BooleanConverter` | 布尔转换器（true/false/yes/no/1/0） |
| `NumberConverters` | Integer、Long、Double、Float、BigDecimal 转换器 |
| `DurationConverter` | Duration 转换器（如 "30s"、"5m"、"1h"） |
| `DateTimeConverters` | LocalDate、LocalDateTime、Instant 转换器 |
| `EnumConverter` | 通用枚举转换器 |
| `CollectionConverters` | 从逗号分隔字符串转换为 List、Set、Map |

### Bean 绑定

| 类名 | 说明 |
|------|------|
| `ConfigBinder` | 将配置绑定到 JavaBean 实例（支持 @DefaultValue） |
| `RecordConfigBinder` | 将配置绑定到 Record 实例（支持 @DefaultValue） |
| `ConfigProperties` | 配置前缀绑定注解 |
| `NestedConfig` | 嵌套配置对象注解 |
| `DefaultValue` **[v1.0.3]** | Record 组件和 POJO 字段的默认值注解 |

### 验证

| 类名 | 说明 |
|------|------|
| `ConfigValidator` | 验证器接口 |
| `RequiredValidator` | 验证必填键是否存在 |
| `RangeValidator` | 验证数值是否在范围内 |
| `PatternValidator` | 验证字符串值是否匹配正则模式 |
| `ValidationResult` | 包含错误信息的验证结果 |
| `ValidationModuleAdapter` | 外部验证模块适配器 |

### 占位符与表达式

| 类名 | 说明 |
|------|------|
| `PlaceholderResolver` | 解析 `${key}` 和 `${key:default}` 占位符 |
| `ExpressionEvaluator` | 配置值的简单表达式求值器 |

### 高级功能

| 类名 | 说明 |
|------|------|
| `MultiProfileConfig` | 多环境配置（dev/test/prod） |
| `TenantConfigManager` | 多租户配置管理器 |
| `EncryptedConfigProcessor` | 加密配置值处理器 |
| `ConfigSourceProvider` | 自定义配置源提供者的 SPI 接口 |
| `HttpConfigSourceProvider` | 基于 HTTP 的远程配置源 |
| `ConfigConverterProvider` | 自定义转换器提供者的 SPI 接口 |
| `ConfigSourceFactory` | 配置源创建工厂 |

### JDK 25 特性

| 类名 | 说明 |
|------|------|
| `ConfigSourceType` | 密封接口，用于类型安全的配置源分类 |
| `ConfigContext` | 作用域配置上下文 |
| `ContextAwareConfig` | 上下文感知的 Config 装饰器 |
| `ConfigSourceProcessor` | 使用模式匹配的配置源处理器 |
| `ReactiveConfigValue` | 响应式配置值包装器 |
| `VirtualThreadConfigWatcher` | 基于虚拟线程的配置文件监听器 |
| `DefaultValue` | 默认配置值注解 |
| `Required` | 必填配置值注解 |

### 内部实现

| 类名 | 说明 |
|------|------|
| `DefaultConfig` | 默认 Config 实现（AutoCloseable） |
| `ConfigWatcher` | 文件系统配置监听器（AutoCloseable） |

## 快速开始

```java
import cloud.opencode.base.config.*;

// 使用 properties 文件快速开始
Config config = OpenConfig.builder()
    .addSource(new PropertiesConfigSource("app.properties"))
    .addSource(new EnvironmentConfigSource())
    .build();

// 获取值并自动类型转换
String name = config.get("app.name");
int port = config.getAs("server.port", Integer.class);
Duration timeout = config.getAs("server.timeout", Duration.class);

// 带默认值
String env = config.getOrDefault("app.env", "development");

// 绑定到 Bean
@ConfigProperties(prefix = "database")
public class DatabaseConfig {
    private String url;
    private String username;
    private int maxPoolSize;
    // getters/setters
}

DatabaseConfig dbConfig = ConfigBinder.bind(config, DatabaseConfig.class);

// 绑定到 Record
public record ServerConfig(String host, int port, Duration timeout) {}
ServerConfig server = RecordConfigBinder.bind(config, "server", ServerConfig.class);

// 监听变更
config.subscribe("server.*", event -> {
    System.out.println("配置已变更: " + event.key() +
        " = " + event.newValue());
});

// 多环境配置
Config profileConfig = MultiProfileConfig.builder()
    .baseSource(new PropertiesConfigSource("app.properties"))
    .profileSource("dev", new PropertiesConfigSource("app-dev.properties"))
    .profileSource("prod", new PropertiesConfigSource("app-prod.properties"))
    .activeProfile("dev")
    .build();
```

### v1.0.3 新功能示例

```java
import cloud.opencode.base.config.*;
import cloud.opencode.base.config.bind.DefaultValue;

// ---- 松绑定 ----
Config config = OpenConfig.builder()
    .addEnvironmentVariables()             // DATABASE_MAX_POOL_SIZE=10
    .enableRelaxedBinding()
    .build();
int poolSize = config.getInt("database.max-pool-size"); // 匹配环境变量 → 10

// ---- 配置导出（调试用，敏感值掩码） ----
Map<String, String> dump = ConfigDump.dump(config);
// db.password → "***", app.name → "myapp"
System.out.println(ConfigDump.dumpToString(config));

// ---- 配置差异对比 ----
Config before = OpenConfig.builder().addProperties(Map.of("a", "1", "b", "2")).build();
Config after  = OpenConfig.builder().addProperties(Map.of("a", "1", "b", "3", "c", "4")).build();
List<ConfigChangeEvent> changes = ConfigDiff.diff(before, after);
System.out.println(ConfigDiff.format(changes));
// ~ b: 2 -> 3
// + c = 4

// ---- POJO @DefaultValue ----
public class ServerConfig {
    @DefaultValue("8080") int port;
    @DefaultValue("localhost") String host;
}
ServerConfig server = config.bind("server", ServerConfig.class);
// 未配置 server.port 时 port=8080

// ---- 验证汇总（一次性报告所有缺失） ----
Config validated = OpenConfig.builder()
    .addProperties(Map.of("app.name", "myapp"))
    .required("db.url", "db.user", "app.port")
    .build(); // 抛出: "Missing required keys: [db.url, db.user, app.port]"
```

## 环境要求

- Java 25+

## 许可证

Apache License 2.0
