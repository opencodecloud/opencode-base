# Config 组件方案

## 1. 组件概述

`opencode-base-config` 模块提供统一的配置管理能力，支持多源配置加载、类型安全访问、占位符解析、热更新和配置验证。零外部依赖，基于 JDK 25 特性（ScopedValue、Record 绑定、密封接口）。

**核心特性：**
- 多配置源：Properties、YAML、环境变量、系统属性、命令行参数、HTTP、内存
- 配置优先级合并（高优先级覆盖低优先级）
- 类型安全配置读取（30+ 内置类型转换器）
- 占位符替换（`${key}` / `${key:default}` 语法）
- 配置热更新与变更监听（虚拟线程文件监控）
- 配置验证（Required / Range / Pattern）
- Record/POJO 配置绑定
- JDK 25 增强：ScopedValue 上下文、密封配置源类型、响应式配置值
- 多租户配置管理
- 加密配置支持

**模块依赖：**
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-config</artifactId>
    <version>${version}</version>
</dependency>
```

---

## 2. 包结构

```
cloud.opencode.base.config
├── Config.java                      # 统一配置访问接口
├── OpenConfig.java                  # 全局配置门面
├── ConfigBuilder.java               # 配置构建器
├── ConfigListener.java              # 配置变更监听器接口
├── ConfigChangeEvent.java           # 配置变更事件（record）
├── ConfigChangeType.java            # 变更类型枚举
├── OpenConfigException.java         # 配置异常
│
├── source/                          # 配置源
│   ├── ConfigSource.java           # 配置源接口
│   ├── CompositeConfigSource.java  # 组合配置源
│   ├── PropertiesConfigSource.java # Properties 文件配置源
│   ├── YamlConfigSource.java       # YAML 配置源
│   ├── EnvironmentConfigSource.java # 环境变量配置源
│   ├── SystemPropertiesConfigSource.java # 系统属性配置源
│   ├── CommandLineConfigSource.java # 命令行配置源
│   └── InMemoryConfigSource.java   # 内存配置源
│
├── converter/                       # 类型转换
│   ├── ConfigConverter.java        # 转换器接口
│   ├── ConverterRegistry.java      # 转换器注册表
│   └── SpiConverterRegistry.java   # SPI 转换器注册表
│
├── bind/                            # 配置绑定
│   ├── ConfigBinder.java           # 配置绑定器
│   ├── RecordConfigBinder.java     # Record 配置绑定器
│   ├── ConfigProperties.java       # @ConfigProperties 注解
│   └── NestedConfig.java           # @NestedConfig 注解
│
├── validation/                      # 配置验证
│   ├── ConfigValidator.java        # 验证器接口
│   ├── ValidationResult.java       # 验证结果
│   ├── RequiredValidator.java      # 必填验证器
│   ├── RangeValidator.java         # 范围验证器
│   ├── PatternValidator.java       # 模式验证器
│   └── ValidationModuleAdapter.java # 验证模块适配器
│
├── placeholder/                     # 占位符解析
│   ├── PlaceholderResolver.java    # 占位符解析器
│   └── ExpressionEvaluator.java    # 表达式求值器
│
├── jdk25/                           # JDK 25 增强
│   ├── ConfigContext.java          # ScopedValue 配置上下文
│   ├── ContextAwareConfig.java     # 上下文感知配置
│   ├── ReactiveConfigValue.java    # 响应式配置值
│   ├── ConfigSourceType.java       # 密封配置源类型
│   ├── ConfigSourceProcessor.java  # 模式匹配配置源处理器
│   ├── VirtualThreadConfigWatcher.java # 虚拟线程配置监控器
│   ├── DefaultValue.java           # @DefaultValue 注解
│   └── Required.java               # @Required 注解
│
└── advanced/                        # 高级功能
    ├── ConfigSourceFactory.java    # 配置源工厂
    ├── ConfigSourceProvider.java   # 配置源提供者 SPI
    ├── ConfigConverterProvider.java # 转换器提供者 SPI
    ├── EncryptedConfigProcessor.java # 加密配置处理器
    ├── MultiProfileConfig.java     # 多环境配置加载器
    ├── TenantConfigManager.java    # 多租户配置管理器
    └── HttpConfigSourceProvider.java # HTTP 配置源提供者
```

---

## 3. 核心 API

### 3.1 OpenConfig

> 全局配置管理器门面，提供静态方法读取配置、创建构建器、加载配置文件。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static Config getGlobal()` | 获取全局配置实例 |
| `static void setGlobal(Config config)` | 设置全局配置实例 |
| `static ConfigBuilder builder()` | 创建配置构建器 |
| `static String getString(String key)` | 获取字符串配置 |
| `static String getString(String key, String defaultValue)` | 获取字符串配置（带默认值） |
| `static int getInt(String key)` | 获取整数配置 |
| `static int getInt(String key, int defaultValue)` | 获取整数配置（带默认值） |
| `static long getLong(String key)` | 获取长整数配置 |
| `static long getLong(String key, long defaultValue)` | 获取长整数配置（带默认值） |
| `static double getDouble(String key)` | 获取浮点数配置 |
| `static double getDouble(String key, double defaultValue)` | 获取浮点数配置（带默认值） |
| `static boolean getBoolean(String key)` | 获取布尔配置 |
| `static boolean getBoolean(String key, boolean defaultValue)` | 获取布尔配置（带默认值） |
| `static Duration getDuration(String key)` | 获取 Duration 配置 |
| `static Duration getDuration(String key, Duration defaultValue)` | 获取 Duration（带默认值） |
| `static <T> T get(String key, Class<T> type)` | 获取指定类型配置 |
| `static <T> T get(String key, Class<T> type, T defaultValue)` | 获取指定类型（带默认值） |
| `static <T> List<T> getList(String key, Class<T> elementType)` | 获取列表配置 |
| `static <K, V> Map<K, V> getMap(String key, Class<K> keyType, Class<V> valueType)` | 获取 Map 配置 |
| `static <T> Optional<T> getOptional(String key, Class<T> type)` | 获取可选配置 |
| `static Optional<String> getOptional(String key)` | 获取可选字符串 |
| `static Config getSubConfig(String prefix)` | 获取子配置 |
| `static Map<String, String> getByPrefix(String prefix)` | 按前缀获取所有配置 |
| `static boolean hasKey(String key)` | 检查键是否存在 |
| `static Set<String> getKeys()` | 获取所有键 |
| `static <T> T bind(String prefix, Class<T> type)` | 绑定配置到对象 |
| `static <T> void bindTo(String prefix, T target)` | 绑定配置到已有对象 |
| `static Config loadFromClasspath(String... resources)` | 从 classpath 加载 |
| `static Config loadFromFile(Path... files)` | 从文件加载 |
| `static Config loadFromProperties(Map<String, String> properties)` | 从 Map 加载 |

**示例：**
```java
// 快速读取
String dbUrl = OpenConfig.getString("database.url");
int port = OpenConfig.getInt("server.port", 8080);
Duration timeout = OpenConfig.getDuration("http.timeout");

// 构建并设置全局配置
Config config = OpenConfig.builder()
    .addClasspathResource("application.properties")
    .addEnvironmentVariables("APP_")
    .addSystemProperties()
    .required("database.url", "database.username")
    .enableHotReload()
    .build();
OpenConfig.setGlobal(config);

// 绑定到 Record
record DatabaseConfig(String url, String username, int maxPoolSize) {}
DatabaseConfig db = OpenConfig.bind("database", DatabaseConfig.class);
```

---

### 3.2 Config

> 统一配置访问接口，提供类型安全的配置读取、子配置、配置绑定和变更监听。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `String getString(String key)` | 获取字符串值 |
| `String getString(String key, String defaultValue)` | 获取字符串值（带默认值） |
| `int getInt(String key)` | 获取整数值 |
| `int getInt(String key, int defaultValue)` | 获取整数值（带默认值） |
| `long getLong(String key)` | 获取长整数值 |
| `double getDouble(String key)` | 获取浮点数值 |
| `boolean getBoolean(String key)` | 获取布尔值 |
| `Duration getDuration(String key)` | 获取 Duration 值 |
| `<T> T get(String key, Class<T> type)` | 获取指定类型值 |
| `<T> T get(String key, Class<T> type, T defaultValue)` | 获取指定类型值（带默认值） |
| `<T> List<T> getList(String key, Class<T> elementType)` | 获取列表值 |
| `<K, V> Map<K, V> getMap(String key, Class<K> keyType, Class<V> valueType)` | 获取 Map 值 |
| `<T> Optional<T> getOptional(String key, Class<T> type)` | 可选获取 |
| `Optional<String> getOptional(String key)` | 可选获取字符串 |
| `Config getSubConfig(String prefix)` | 获取子配置 |
| `Map<String, String> getByPrefix(String prefix)` | 按前缀获取 |
| `boolean hasKey(String key)` | 检查键是否存在 |
| `Set<String> getKeys()` | 获取所有键 |
| `<T> T bind(String prefix, Class<T> type)` | 绑定配置到对象 |
| `<T> void bindTo(String prefix, T target)` | 绑定配置到已有对象 |
| `void addListener(String key, ConfigListener listener)` | 添加变更监听 |
| `void addListener(ConfigListener listener)` | 添加全局监听 |
| `void removeListener(ConfigListener listener)` | 移除监听 |

**示例：**
```java
Config config = OpenConfig.builder()
    .addClasspathResource("application.properties")
    .build();

String url = config.getString("database.url");
Duration timeout = config.getDuration("http.timeout");
List<String> hosts = config.getList("redis.hosts", String.class);

Config dbConfig = config.getSubConfig("database");
String dbUrl = dbConfig.getString("url"); // 读取 database.url
```

---

### 3.3 ConfigBuilder

> 配置构建器，支持添加多种配置源、注册转换器、配置验证和热更新。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `ConfigBuilder addClasspathResource(String resource)` | 添加 classpath 资源 |
| `ConfigBuilder addClasspathResources(String... resources)` | 批量添加 classpath 资源 |
| `ConfigBuilder addFile(Path file)` | 添加文件配置源 |
| `ConfigBuilder addFiles(Path... files)` | 批量添加文件 |
| `ConfigBuilder addSystemProperties()` | 添加系统属性 |
| `ConfigBuilder addEnvironmentVariables()` | 添加环境变量 |
| `ConfigBuilder addEnvironmentVariables(String prefix)` | 添加指定前缀环境变量 |
| `ConfigBuilder addCommandLineArgs(String[] args)` | 添加命令行参数 |
| `ConfigBuilder addProperties(Map<String, String> properties)` | 添加内存属性 |
| `ConfigBuilder addSource(ConfigSource source)` | 添加自定义配置源 |
| `<T> ConfigBuilder registerConverter(Class<T> type, ConfigConverter<T> converter)` | 注册类型转换器 |
| `ConfigBuilder disablePlaceholders()` | 禁用占位符解析 |
| `ConfigBuilder enableHotReload()` | 启用热更新 |
| `ConfigBuilder hotReloadInterval(Duration interval)` | 设置热更新间隔 |
| `ConfigBuilder addValidator(ConfigValidator validator)` | 添加验证器 |
| `ConfigBuilder required(String... keys)` | 添加必填项 |
| `Config build()` | 构建配置 |

**示例：**
```java
Config config = OpenConfig.builder()
    .addClasspathResource("application.properties")
    .addClasspathResource("application-dev.properties")
    .addEnvironmentVariables("APP_")
    .addSystemProperties()
    .addCommandLineArgs(args)
    .required("database.url", "api.key")
    .enableHotReload()
    .hotReloadInterval(Duration.ofSeconds(30))
    .build();
```

---

### 3.4 ConfigSource

> 配置源接口，所有配置来源的抽象。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `String getName()` | 获取配置源名称 |
| `Map<String, String> getProperties()` | 获取所有属性 |
| `int getPriority()` | 获取优先级（数值越大越高） |
| `default boolean supportsReload()` | 是否支持重载 |
| `default void reload()` | 重载配置 |

**优先级（从高到低）：**
- CommandLine: 200
- Environment: 100
- SystemProperties: 50
- Properties/YAML: 50
- InMemory: 10

---

### 3.5 PropertiesConfigSource

> Properties 文件配置源，支持 classpath 和文件系统加载。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `PropertiesConfigSource(String resource, boolean classpath)` | classpath 构造 |
| `PropertiesConfigSource(Path file)` | 文件路径构造 |
| `boolean supportsReload()` | 返回 true |
| `void reload()` | 重载配置文件 |

---

### 3.6 YamlConfigSource

> YAML 配置源，通过反射检测 yml 模块是否可用，支持优雅降级。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static boolean isYamlSupported()` | 检查 YAML 支持是否可用 |
| `YamlConfigSource(String resource, boolean classpath)` | classpath 构造 |
| `YamlConfigSource(Path file)` | 文件路径构造 |
| `boolean supportsReload()` | 返回 true |
| `void reload()` | 重载配置文件 |

---

### 3.7 EnvironmentConfigSource

> 环境变量配置源，支持可选前缀过滤。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `EnvironmentConfigSource()` | 加载所有环境变量 |
| `EnvironmentConfigSource(String prefix)` | 加载指定前缀环境变量 |

---

### 3.8 CommandLineConfigSource

> 命令行参数配置源，支持 `--key=value` 格式。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `CommandLineConfigSource(String[] args)` | 解析命令行参数 |

**示例：**
```java
// --server.port=9090 --database.url=jdbc:mysql://localhost/db
String[] args = {"--server.port=9090", "--database.url=jdbc:mysql://localhost/db"};
ConfigSource source = new CommandLineConfigSource(args);
```

---

### 3.9 InMemoryConfigSource

> 内存配置源，支持动态修改。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `InMemoryConfigSource()` | 空配置源 |
| `InMemoryConfigSource(Map<String, String> properties)` | 从 Map 构造 |
| `InMemoryConfigSource(String name, Map<String, String> properties)` | 命名构造 |
| `void setProperty(String key, String value)` | 设置属性 |
| `String removeProperty(String key)` | 移除属性 |
| `void setProperties(Map<String, String> props)` | 批量设置 |
| `void clear()` | 清空所有属性 |

---

### 3.10 CompositeConfigSource

> 组合配置源，按优先级合并多个配置源。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `CompositeConfigSource(List<ConfigSource> sources)` | 构造方法 |
| `List<ConfigSource> getSources()` | 获取所有子配置源 |
| `boolean supportsReload()` | 任一子源支持则返回 true |
| `void reload()` | 重载所有支持的子源 |

---

### 3.11 ConfigConverter

> 配置类型转换器接口。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `T convert(String value)` | 将字符串值转换为目标类型 |

---

### 3.12 ConverterRegistry

> 类型转换器注册表，内置 30+ 常用类型转换器。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static ConverterRegistry defaults()` | 创建包含默认转换器的注册表 |
| `<T> void register(Class<T> type, ConfigConverter<T> converter)` | 注册自定义转换器 |
| `<T> T convert(String value, Class<T> type)` | 执行类型转换 |
| `boolean hasConverter(Class<?> type)` | 检查是否有对应转换器 |

**内置支持类型：** `Integer`, `Long`, `Double`, `Float`, `Boolean`, `Duration`, `LocalDate`, `LocalDateTime`, `Instant`, `Path`, `URI`, `URL`, `InetAddress`, `Enum` 等。

**示例：**
```java
ConverterRegistry registry = ConverterRegistry.defaults();
Integer port = registry.convert("8080", Integer.class);
Duration timeout = registry.convert("30s", Duration.class);
LocalDate date = registry.convert("2025-01-01", LocalDate.class);
```

---

### 3.13 ConfigBinder

> 配置绑定器，将配置绑定到 POJO 对象。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `ConfigBinder(Config config, ConverterRegistry converters)` | 构造方法 |
| `<T> T bind(String prefix, Class<T> type)` | 绑定配置到新对象 |
| `<T> void bindTo(String prefix, T target)` | 绑定配置到已有对象 |

---

### 3.14 RecordConfigBinder

> Record 配置绑定器，专为 JDK 25 Record 类型设计。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `RecordConfigBinder(Config config, ConverterRegistry converters)` | 构造方法 |
| `<T extends Record> T bind(String prefix, Class<T> recordType)` | 绑定配置到 Record |

**字段名映射规则：** `maxPoolSize` -> `max-pool-size`（kebab-case）

**示例：**
```java
record DatabaseConfig(String url, String username, int maxPoolSize) {}
DatabaseConfig db = OpenConfig.bind("database", DatabaseConfig.class);
// 读取 database.url, database.username, database.max-pool-size
```

---

### 3.15 ConfigValidator

> 配置验证器接口。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `ValidationResult validate(Config config)` | 验证配置 |

---

### 3.16 RequiredValidator

> 必填验证器，检查必填配置项是否存在。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `RequiredValidator(String... requiredKeys)` | 指定必填键 |
| `ValidationResult validate(Config config)` | 执行验证 |

---

### 3.17 RangeValidator

> 范围验证器，检查数值配置是否在指定范围内。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `RangeValidator(String key, Number min, Number max)` | 闭区间构造 |
| `RangeValidator(String key, Number min, Number max, boolean minInclusive, boolean maxInclusive)` | 自定义边界 |
| `ValidationResult validate(Config config)` | 执行验证 |

**示例：**
```java
RangeValidator portValidator = new RangeValidator("server.port", 1, 65535);
ValidationResult result = portValidator.validate(config);
```

---

### 3.18 PatternValidator

> 模式验证器，用正则表达式验证配置值格式。

**预定义常量：**
- `EMAIL_PATTERN` — 邮箱格式
- `URL_PATTERN` — URL 格式
- `IPV4_PATTERN` — IPv4 格式
- `PHONE_PATTERN` — 电话号码格式

**主要方法：**

| 方法 | 描述 |
|------|------|
| `PatternValidator(String key, String regex)` | 正则构造 |
| `PatternValidator(String key, String regex, String errorMessage)` | 带错误消息 |
| `static PatternValidator email(String key)` | 邮箱验证器 |
| `static PatternValidator url(String key)` | URL 验证器 |
| `static PatternValidator ipv4(String key)` | IPv4 验证器 |
| `static PatternValidator phone(String key)` | 电话验证器 |

---

### 3.19 ValidationResult

> 配置验证结果。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `boolean isValid()` | 是否验证通过 |
| `List<String> getErrors()` | 获取错误列表 |
| `static ValidationResult valid()` | 创建通过结果 |
| `static ValidationResult invalid(String error)` | 创建单错误结果 |
| `static ValidationResult invalid(List<String> errors)` | 创建多错误结果 |

---

### 3.20 ValidationModuleAdapter

> 验证模块适配器，可选集成 validation 模块进行注解驱动验证。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static boolean isValidationModuleAvailable()` | 检查 validation 模块是否可用 |
| `static ValidationResult validateObject(Object object)` | 验证对象 |
| `static <T> ConfigValidator forObject(Class<T> configClass, String prefix)` | 创建对象验证器 |
| `static ConfigValidator forRequiredKeys(String... requiredKeys)` | 创建必填验证器 |
| `static ConfigValidator forPatterns(Map<String, String> patterns)` | 创建模式验证器 |
| `static ConfigValidator combine(ConfigValidator... validators)` | 组合多个验证器 |

---

### 3.21 PlaceholderResolver

> 占位符解析器，支持 `${key}` 和 `${key:default}` 语法。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `PlaceholderResolver(Function<String, String> propertyResolver)` | 构造方法 |
| `PlaceholderResolver(Function<String, String> propertyResolver, String prefix, String suffix)` | 自定义前后缀 |
| `String resolve(String value)` | 解析占位符 |

**示例：**
```java
Map<String, String> props = Map.of(
    "host", "localhost", "port", "3306",
    "database.url", "jdbc:mysql://${host}:${port}/mydb"
);
PlaceholderResolver resolver = new PlaceholderResolver(props::get);
String url = resolver.resolve(props.get("database.url"));
// jdbc:mysql://localhost:3306/mydb
```

---

### 3.22 ConfigChangeEvent

> 配置变更事件（record），描述配置项的新增、修改和删除。

**Record 组件：** `String key`, `ChangeType changeType`, `String oldValue`, `String newValue`

**主要方法：**

| 方法 | 描述 |
|------|------|
| `boolean isAdded()` | 是否新增 |
| `boolean isModified()` | 是否修改 |
| `boolean isRemoved()` | 是否删除 |
| `static ConfigChangeEvent added(String key, String newValue)` | 创建新增事件 |
| `static ConfigChangeEvent modified(String key, String oldValue, String newValue)` | 创建修改事件 |
| `static ConfigChangeEvent removed(String key, String oldValue)` | 创建删除事件 |

---

### 3.23 ConfigContext (JDK 25)

> 基于 ScopedValue 的配置上下文，支持租户隔离和请求级配置覆盖。

**ScopedValue 常量：**
- `OVERRIDES` — 配置覆盖
- `PROFILE` — 当前环境 Profile
- `TENANT_ID` — 当前租户 ID

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static <T, X extends Throwable> T withOverrides(Map<String, String> overrides, Callable<T> task)` | 带覆盖配置执行 |
| `static <T, X extends Throwable> T withProfile(String profile, Callable<T> task)` | 带 Profile 执行 |
| `static <T, X extends Throwable> T withTenant(String tenantId, Callable<T> task)` | 带租户执行 |
| `static void runWithOverrides(Map<String, String> overrides, Runnable task)` | 运行带覆盖配置 |
| `static void runWithProfile(String profile, Runnable task)` | 运行带 Profile |
| `static void runWithTenant(String tenantId, Runnable task)` | 运行带租户 |
| `static Optional<Map<String, String>> currentOverrides()` | 获取当前覆盖 |
| `static Optional<String> currentProfile()` | 获取当前 Profile |
| `static Optional<String> currentTenant()` | 获取当前租户 |

**示例：**
```java
ConfigContext.withTenant("tenant-123", () -> {
    String apiKey = config.getString("api.key"); // 租户专属配置
    return processRequest(apiKey);
});

ConfigContext.runWithOverrides(Map.of("database.url", "jdbc:h2:mem:test"), () -> {
    runTests(); // 使用覆盖配置
});
```

---

### 3.24 ReactiveConfigValue (JDK 25)

> 响应式配置值，配置变更时自动通知订阅者。使用虚拟线程进行通知。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `ReactiveConfigValue(Config config, String key, Class<T> type, T defaultValue)` | 构造方法 |
| `T get()` | 获取当前值 |
| `ReactiveConfigValue<T> subscribe(Consumer<T> subscriber)` | 订阅变更 |
| `void unsubscribe(Consumer<T> subscriber)` | 取消订阅 |
| `static <T> ReactiveConfigValue<T> of(Config config, String key, Class<T> type, T defaultValue)` | 静态工厂 |

**示例：**
```java
ReactiveConfigValue<String> logLevel = ReactiveConfigValue
    .of(config, "log.level", String.class, "INFO");

logLevel.subscribe(level -> System.out.println("日志级别变更: " + level));
String current = logLevel.get();
```

---

### 3.25 ConfigSourceType (JDK 25)

> 密封配置源类型，利用 JDK 25 sealed interface 和 pattern matching。

**许可实现：** `File`, `Classpath`, `Environment`, `System`, `CommandLine`

**示例：**
```java
ConfigSourceType source = new ConfigSourceType.File(Path.of("config.properties"));
ConfigSource configSource = source.toSource();

String desc = switch (source) {
    case ConfigSourceType.File f -> "文件: " + f.path();
    case ConfigSourceType.Classpath cp -> "类路径: " + cp.resource();
    case ConfigSourceType.Environment env -> "环境变量: " + env.prefix();
    case ConfigSourceType.System sys -> "系统属性";
    case ConfigSourceType.CommandLine cmd -> "命令行";
};
```

---

### 3.26 VirtualThreadConfigWatcher (JDK 25)

> 基于虚拟线程的配置文件监控器，实现 `AutoCloseable`。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `void start()` | 启动监控 |
| `void addListener(ConfigListener listener)` | 添加变更监听器 |
| `void close()` | 停止监控并释放资源 |

---

### 3.27 EncryptedConfigProcessor

> 加密配置处理器，自动解密 `ENC(...)` 格式的配置值。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static Config createEncryptedConfig(Config source, SecretKey key)` | 创建加密感知配置 |
| `static String encrypt(String plaintext, SecretKey key)` | 加密配置值 |

**示例：**
```java
SecretKey key = loadSecretKey();
Config config = EncryptedConfigProcessor.createEncryptedConfig(
    OpenConfig.getGlobal(), key);
String password = config.getString("database.password"); // 自动解密
```

---

### 3.28 MultiProfileConfig

> 多环境配置加载器，根据命令行参数自动加载对应 Profile 的配置。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static Config load(String[] args)` | 根据参数加载多 Profile 配置 |

---

### 3.29 TenantConfigManager

> 多租户配置管理器，每个租户可覆盖基础配置。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `TenantConfigManager(Config baseConfig)` | 构造方法 |
| `Config getConfig(String tenantId)` | 获取租户配置 |
| `<T> T get(String tenantId, String key, Class<T> type)` | 获取租户配置值 |
| `void clearCache(String tenantId)` | 清除租户缓存 |
| `void clearAllCaches()` | 清除所有缓存 |

**示例：**
```java
TenantConfigManager manager = new TenantConfigManager(OpenConfig.getGlobal());
Config tenantConfig = manager.getConfig("tenant-123");
String apiKey = tenantConfig.getString("api.key");
```

---

### 3.30 ConfigSourceFactory

> 配置源工厂，根据 URI 前缀创建配置源。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static ConfigSource create(String uri)` | 根据 URI 创建配置源 |
| `static ConfigSource create(String uri, Map<String, Object> options)` | 带选项创建 |

**支持的 URI 格式：** `file:`, `classpath:`, `env:`, `http:`/`https:`

---

### 3.31 OpenConfigException

> 配置组件异常，继承自 `OpenException`。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `OpenConfigException(String message)` | 消息构造 |
| `OpenConfigException(String message, Throwable cause)` | 消息+原因 |
| `OpenConfigException(String configKey, String configSource, String message)` | 带键和源 |
| `String configKey()` | 获取配置键 |
| `String configSource()` | 获取配置源 |
| `static OpenConfigException keyNotFound(String key)` | 键不存在 |
| `static OpenConfigException requiredKeyMissing(String key)` | 必填键缺失 |
| `static OpenConfigException conversionFailed(String key, String value, Class<?> targetType)` | 类型转换失败 |
| `static OpenConfigException sourceLoadFailed(String source, Throwable cause)` | 配置源加载失败 |
| `static OpenConfigException bindFailed(String prefix, Class<?> targetType, Throwable cause)` | 绑定失败 |
| `static OpenConfigException validationFailed(String errors)` | 验证失败 |
| `static OpenConfigException placeholderResolveFailed(String placeholder)` | 占位符解析失败 |
| `static OpenConfigException converterNotFound(Class<?> type)` | 转换器未找到 |
| `static OpenConfigException decryptionFailed(Throwable cause)` | 解密失败 |

---

## 4. 使用示例

### 4.1 快速开始

```java
// 从 classpath 加载配置
Config config = OpenConfig.loadFromClasspath("application.properties");
OpenConfig.setGlobal(config);

// 读取配置
String url = OpenConfig.getString("database.url");
int port = OpenConfig.getInt("server.port", 8080);
boolean debug = OpenConfig.getBoolean("app.debug", false);
```

### 4.2 多配置源合并

```java
Config config = OpenConfig.builder()
    .addClasspathResource("application.properties")       // 基础配置
    .addClasspathResource("application-dev.properties")    // 环境配置
    .addEnvironmentVariables("APP_")                       // 环境变量
    .addSystemProperties()                                 // 系统属性
    .addCommandLineArgs(args)                              // 命令行参数（最高优先级）
    .required("database.url")
    .addValidator(new RangeValidator("server.port", 1, 65535))
    .addValidator(PatternValidator.email("admin.email"))
    .enableHotReload()
    .build();
```

### 4.3 Record 绑定

```java
record ServerConfig(
    @Required String host,
    @DefaultValue("8080") int port,
    @DefaultValue("PT30S") Duration timeout
) {}

ServerConfig server = OpenConfig.bind("server", ServerConfig.class);
```

### 4.4 配置热更新

```java
Config config = OpenConfig.builder()
    .addFile(Path.of("/etc/app/config.properties"))
    .enableHotReload()
    .hotReloadInterval(Duration.ofSeconds(10))
    .build();

config.addListener("log.level", event -> {
    if (event.isModified()) {
        System.out.println("日志级别变更: " + event.oldValue() + " -> " + event.newValue());
    }
});
```

---

*文档更新日期：2026-02-27*
