# OpenCode Base YML

**适用于 Java 25+ 的 YAML 处理库，支持 SPI 架构**

`opencode-base-yml` 提供了一套完整的 YAML 处理工具包，包括解析、写入、对象绑定、文档合并、占位符解析、路径查询和安全特性。采用基于 SPI 的提供者架构，可选集成 SnakeYAML。

## 功能特性

### 核心功能
- **YAML 解析**：将 YAML 字符串、文件和流解析为 Map 或文档
- **YAML 写入**：将对象和 Map 转储为 YAML 字符串或文件
- **多文档**：加载和转储多文档 YAML
- **验证**：检查 YAML 字符串有效性

### 高级功能
- **对象绑定**：将 YAML 文档绑定到 Java 对象，支持注解
- **文档合并**：使用可配置策略合并多个 YAML 文档
- **占位符解析**：解析 `${...}` 占位符，支持系统属性、环境变量或自定义属性
- **路径查询**：使用点号分隔路径导航 YAML 文档
- **节点树**：将 YAML 解析为可导航的节点树
- **SPI 架构**：通过 `YmlProvider` SPI 实现可插拔的 YAML 提供者
- **SnakeYAML 集成**：可选的 SnakeYAML 后端，完整支持 YAML 1.1
- **安全**：安全的 YAML 加载，支持类型限制、深度限制和 YAML 炸弹防护

### V1.0.3 新增功能
- **统一异常体系**：`OpenYmlException` 继承 `OpenException`，支持错误码和组件追踪
- **YAML 差异比较**：比较两个 YAML 文档，获取新增、删除和修改的差异列表
- **扁平化/反扁平化**：嵌套 YAML 与点号分隔的扁平 Map 互转
- **YAML-JSON 互转**：无需外部依赖的 YAML 与 JSON 互转
- **结构校验**：轻量级结构校验（必填键、类型约束、范围、正则匹配）
- **!include 指令**：解析 `!include` 文件引用，带路径穿越防护和循环引用检测
- **Profile 加载**：加载基础 + Profile 特定的 YAML 文件并深度合并（如 `application.yml` + `application-dev.yml`）
- **严格类型模式**：YAML 1.2 严格布尔值处理，防止 Norway 问题（YES/NO -> boolean）
- **线程安全提供者**：SnakeYamlProvider 每次调用创建新实例，保证线程安全

## 快速开始

### Maven 依赖
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-yml</artifactId>
    <version>1.0.3</version>
</dependency>
```

### 基本用法

```java
import cloud.opencode.base.yml.*;
import java.nio.file.Path;
import java.util.Map;
import java.util.List;

// 解析 YAML
Map<String, Object> data = OpenYml.load("server:\n  port: 8080");
YmlDocument doc = OpenYml.parse("server:\n  port: 8080");

// 从文件加载
YmlDocument doc = OpenYml.loadFile(Path.of("config.yml"));

// 通过路径访问值
String host = doc.getString("server.host");
int port = doc.getInt("server.port", 8080);
boolean debug = doc.getBoolean("server.debug");
List<String> tags = doc.getList("tags");
Map<String, Object> db = doc.getMap("database");

// 写入 YAML
String yaml = OpenYml.dump(Map.of("name", "test", "version", "1.0"));
OpenYml.writeFile(data, Path.of("output.yml"));

// 对象绑定
record ServerConfig(int port, String host) {}
ServerConfig config = OpenYml.bind(doc, "server", ServerConfig.class);

// 合并文档
Map<String, Object> merged = OpenYml.merge(baseMap, overlayMap);

// 占位符解析
String resolved = OpenYml.resolvePlaceholders("server:\n  port: ${SERVER_PORT:8080}");

// 多文档 YAML
List<YmlDocument> docs = OpenYml.loadAll(multiDocYaml);
String multiDoc = OpenYml.dumpAll(List.of(map1, map2));

// 验证
boolean valid = OpenYml.isValid(yamlString);
```

### V1.0.3 新功能

```java
import cloud.opencode.base.yml.diff.*;
import cloud.opencode.base.yml.transform.*;
import cloud.opencode.base.yml.schema.*;
import cloud.opencode.base.yml.profile.*;
import cloud.opencode.base.yml.include.*;

// YAML 差异比较
List<DiffEntry> diffs = OpenYml.diff(baseMap, otherMap);
diffs.forEach(d -> System.out.println(d.type() + " " + d.path()));

// 扁平化 / 反扁平化
Map<String, Object> flat = OpenYml.flatten(nestedMap);
// {"server.port": 8080, "server.host": "localhost"}
Map<String, Object> nested = OpenYml.unflatten(flat);

// YAML 转 JSON
String json = OpenYml.toJson("server:\n  port: 8080");
String prettyJson = OpenYml.toJson("key: value", true);
Map<String, Object> fromJson = OpenYml.fromJson("{\"key\": \"value\"}");
String yamlFromJson = YmlJson.fromJsonToYaml("{\"port\": 8080}");

// 结构校验
YmlSchema schema = YmlSchema.builder()
    .required("server.port", "server.host")
    .type("server.port", Integer.class)
    .range("server.port", 1, 65535)
    .pattern("server.host", "^[a-zA-Z0-9.-]+$")
    .nested("database", YmlSchema.builder().required("url").build())
    .rule("name", v -> ((String) v).length() <= 100, "名称过长")
    .build();
ValidationResult result = schema.validate(data);
if (!result.isValid()) {
    result.getErrors().forEach(e -> System.err.println(e.path() + ": " + e.message()));
}

// Profile 加载
YmlDocument doc = YmlProfile.load(Path.of("config"), "application", "dev", "local");
// 加载 application.yml -> application-dev.yml -> application-local.yml（深度合并）
List<String> activeProfiles = YmlProfile.getActiveProfiles();
// 从系统属性 "yml.profiles.active" 或环境变量 "YAML_PROFILES_ACTIVE" 读取

// !include 解析
Map<String, Object> data = YmlIncludeResolver.load(Path.of("config.yml"));
// 或使用自定义配置：
YmlIncludeResolver resolver = YmlIncludeResolver.builder()
    .basePath(Path.of("/etc/config"))
    .maxDepth(5)
    .allowedExtensions(Set.of(".yml", ".yaml"))
    .build();
Map<String, Object> result = resolver.resolve(Path.of("/etc/config/app.yml"));

// 占位符解析（高级用法）
PlaceholderResolver resolver = PlaceholderResolver.builder()
    .withSystemProperties()
    .withEnvironmentVariables()
    .addPropertySource("custom", Map.of("APP_PORT", "9090"))
    .strict(true)
    .build();
String resolved = resolver.resolve("port: ${APP_PORT}");

// 严格类型（解决 Norway 问题）
YmlConfig strictConfig = YmlConfig.builder().strictTypes(true).build();
// "YES" 保持为字符串 "true"，而不是布尔值 true
```

## API 参考

### OpenYml（主门面）

| 方法 | 说明 |
|------|------|
| `load(String yaml)` | 解析 YAML 字符串为 Map |
| `parse(String yaml)` | 解析 YAML 字符串为 YmlDocument |
| `loadFile(Path)` | 从文件加载 YAML |
| `loadFile(Path, Charset)` | 使用字符集从文件加载 |
| `loadStream(InputStream)` | 从流加载 YAML |
| `loadAll(String yaml)` | 加载多文档 YAML |
| `dump(Object)` | 将对象转储为 YAML 字符串 |
| `dump(Object, YmlConfig)` | 使用配置转储 |
| `dump(YmlDocument)` | 将文档转储为 YAML 字符串 |
| `dumpObject(Object)` | 通过反射将 Java 对象转储为 YAML |
| `writeFile(Object, Path)` | 写入 YAML 到文件 |
| `writeFile(Object, Path, Charset)` | 使用字符集写入 |
| `write(Object, Writer)` | 写入 YAML 到 Writer |
| `dumpAll(Iterable<?>)` | 转储多个文档 |
| `bind(YmlDocument, Class<T>)` | 将文档绑定到 Java 对象 |
| `bind(YmlDocument, String, Class<T>)` | 将指定路径绑定到 Java 对象 |
| `bind(String, Class<T>)` | 将 YAML 字符串绑定到 Java 对象 |
| `toMap(Object)` | 将 Java 对象转换为 Map |
| `merge(Map, Map)` | 合并两个 Map（深度合并） |
| `merge(Map, Map, MergeStrategy)` | 使用策略合并 |
| `mergeAll(Map...)` | 合并多个 Map |
| `merge(YmlDocument, YmlDocument)` | 合并两个文档 |
| `resolvePlaceholders(String)` | 解析 `${...}` 占位符 |
| `resolvePlaceholders(String, PlaceholderResolver)` | 使用自定义解析器 |
| `resolvePlaceholders(Map)` | 解析 Map 值中的占位符 |
| `parseWithPlaceholders(String)` | 解析 YAML 并解析占位符 |
| `parseWithPlaceholders(String, Map)` | 使用自定义属性 |
| `isValid(String)` | 检查 YAML 有效性 |
| `parseTree(String)` | 解析为 YmlNode 树 |
| `diff(Map, Map)` | 比较两个 Map |
| `diff(YmlDocument, YmlDocument)` | 比较两个文档 |
| `flatten(Map)` | 扁平化嵌套 Map |
| `unflatten(Map)` | 反扁平化 |
| `toJson(String)` | YAML 转 JSON |
| `toJson(String, boolean)` | YAML 转 JSON（美化） |
| `fromJson(String)` | JSON 解析为 Map |
| `fromJsonToYaml(String)` | JSON 转 YAML 字符串 |
| `validate(Map, YmlSchema)` | 根据模式校验 Map |
| `validate(YmlDocument, YmlSchema)` | 根据模式校验文档 |
| `loadProfile(Path, String, String...)` | 加载 Profile 覆盖配置 |
| `loadDefaultProfile(Path, String...)` | 使用默认名 "application" 加载 |
| `loadWithIncludes(Path)` | 加载并解析 !include 指令 |

### YmlDocument（文档）

| 方法 | 说明 |
|------|------|
| `of(Map)` | 从 Map 创建 |
| `get(String path)` | 获取路径处的值 |
| `getString(String path)` | 获取字符串值 |
| `getString(String, String)` | 获取字符串（带默认值） |
| `getInt(String path)` | 获取整数值 |
| `getInt(String, int)` | 获取整数（带默认值） |
| `getLong(String path)` | 获取长整数值 |
| `getBoolean(String path)` | 获取布尔值 |
| `getBoolean(String, boolean)` | 获取布尔值（带默认值） |
| `getList(String path)` | 获取列表值 |
| `getMap(String path)` | 获取 Map 值 |
| `getDocument(String path)` | 获取子文档 |
| `getOptional(String path)` | 获取 Optional 值 |
| `has(String path)` | 检查路径是否存在 |
| `keys()` | 获取根级键集合 |
| `size()` | 获取根级条目数 |
| `isEmpty()` | 检查文档是否为空 |
| `asMap()` | 获取不可修改的 Map |
| `asList()` | 获取不可修改的 List |

### YmlNode（树导航）

| 方法 | 说明 |
|------|------|
| `of(Object)` | 从原始数据创建节点 |
| `asText()` / `asText(String)` | 获取文本值（带默认值） |
| `asInt()` / `asInt(int)` | 获取整数值（带默认值） |
| `asLong()` / `asLong(long)` | 获取长整数值（带默认值） |
| `asBoolean()` / `asBoolean(boolean)` | 获取布尔值（带默认值） |
| `asDouble()` / `asDouble(double)` | 获取双精度值（带默认值） |
| `get(String key)` | 按键获取子节点 |
| `get(int index)` | 按索引获取子节点 |
| `at(String path)` | 导航到路径 |
| `has(String key)` | 检查子节点是否存在 |
| `size()` | 获取子节点数量 |
| `keys()` | 获取子节点键名 |
| `values()` | 获取子节点值 |
| `toObject()` / `toMap()` / `toList()` | 转换为 Java 类型 |
| `toYaml()` | 转换为 YAML 字符串 |
| `getType()` | 获取 NodeType（SCALAR/SEQUENCE/MAPPING/NULL） |
| `isScalar()` / `isSequence()` / `isMapping()` / `isNull()` | 类型检查 |
| `getRawValue()` | 获取底层原始值 |

### YmlConfig（配置构建器）

| 构建器方法 | 说明 |
|-----------|------|
| `defaults()` | 创建默认配置 |
| `builder()` | 创建构建器 |
| `indent(int)` | 设置缩进（默认：2） |
| `prettyPrint(boolean)` | 启用美化打印 |
| `safeMode(boolean)` | 启用安全模式 |
| `strictTypes(boolean)` | 启用 YAML 1.2 严格类型模式 |
| `allowDuplicateKeys(boolean)` | 允许重复键 |
| `maxAliasesForCollections(int)` | 最大别名数（默认：50） |
| `maxNestingDepth(int)` | 最大嵌套深度 |
| `maxDocumentSize(long)` | 最大文档大小（字节） |
| `defaultFlowStyle(FlowStyle)` | 流式风格：FLOW / BLOCK / AUTO |
| `defaultScalarStyle(ScalarStyle)` | 标量风格：PLAIN / SINGLE_QUOTED / DOUBLE_QUOTED / LITERAL / FOLDED |

### YmlDiff（差异比较）

| 方法 | 说明 |
|------|------|
| `diff(Map, Map)` | 比较两个 Map，返回 DiffEntry 列表 |
| `diff(YmlDocument, YmlDocument)` | 比较两个文档 |

`DiffEntry` 记录：`type()`（ADDED/REMOVED/MODIFIED）、`path()`、`oldValue()`、`newValue()`

工厂方法：`DiffEntry.added(path, value)`、`DiffEntry.removed(path, value)`、`DiffEntry.modified(path, old, new)`

### YmlFlattener（扁平化）

| 方法 | 说明 |
|------|------|
| `flatten(Map)` | 使用 "." 分隔符扁平化 |
| `flatten(Map, String)` | 使用自定义分隔符扁平化 |
| `unflatten(Map)` | 使用 "." 分隔符反扁平化 |
| `unflatten(Map, String)` | 使用自定义分隔符反扁平化 |

### YmlJson（JSON 转换）

| 方法 | 说明 |
|------|------|
| `toJson(String yaml)` | YAML 字符串转紧凑 JSON |
| `toJson(String yaml, boolean pretty)` | YAML 字符串转 JSON（可选美化） |
| `toJson(Map)` | Map 转紧凑 JSON |
| `toJson(Map, boolean)` | Map 转 JSON（可选美化） |
| `fromJson(String json)` | JSON 字符串转 Map |
| `fromJsonToYaml(String json)` | JSON 字符串转 YAML 字符串 |

### YmlSchema（结构校验构建器）

| 构建器方法 | 说明 |
|-----------|------|
| `required(String... keys)` | 添加必填键 |
| `type(String path, Class<?>)` | 添加类型约束 |
| `range(String path, Comparable min, Comparable max)` | 添加值范围 |
| `pattern(String path, String regex)` | 添加正则匹配 |
| `nested(String path, YmlSchema)` | 添加嵌套模式 |
| `rule(String path, Predicate<Object>, String msg)` | 添加自定义规则 |

`ValidationResult`：`isValid()`、`getErrors()`

`ValidationError` 记录：`path()`、`message()`、`type()`（MISSING_REQUIRED / TYPE_MISMATCH / OUT_OF_RANGE / PATTERN_MISMATCH / CUSTOM_RULE_FAILED）

### YmlIncludeResolver（文件包含构建器）

| 方法 | 说明 |
|------|------|
| `load(Path file)` | 静态便捷方法：使用默认配置加载 |
| `resolve(Path file)` | 解析文件中的 !include 指令 |
| `resolve(String yaml, Path basePath)` | 解析 YAML 字符串 |
| `builder().maxDepth(int)` | 最大包含深度（默认：10） |
| `builder().basePath(Path)` | 路径包含检查的基准目录 |
| `builder().allowedExtensions(Set)` | 允许的文件扩展名（默认：.yml, .yaml） |

### YmlProfile（环境配置）

| 方法 | 说明 |
|------|------|
| `load(Path, String name, String... profiles)` | 加载基础 + Profile 深度合并 |
| `load(Path, String, MergeStrategy, List<String>)` | 使用自定义策略加载 |
| `loadDefault(Path, String... profiles)` | 使用默认名 "application" 加载 |
| `getActiveProfiles()` | 从系统属性或环境变量获取活跃 Profile |

### MergeStrategy（合并策略）

| 值 | 说明 |
|----|------|
| `OVERRIDE` | 后者完全覆盖前者 |
| `KEEP_FIRST` | 前者优先 |
| `DEEP_MERGE` | 递归深度合并嵌套 Map |
| `APPEND_LISTS` | 拼接列表值 |
| `MERGE_LISTS_UNIQUE` | 合并列表并去重 |
| `FAIL_ON_CONFLICT` | 冲突时抛出异常 |

### PlaceholderResolver（占位符解析构建器）

| 方法 | 说明 |
|------|------|
| `create()` | 创建空解析器 |
| `create(Map<String, String>)` | 使用属性创建 |
| `builder()` | 创建构建器 |
| `resolve(String text)` | 解析文本中的占位符 |
| `resolveYaml(String yaml)` | 解析 YAML 中的占位符 |
| `resolveMap(Map)` | 解析 Map 值中的占位符 |
| `builder().withSystemProperties()` | 添加系统属性源 |
| `builder().withEnvironmentVariables()` | 添加环境变量源 |
| `builder().addPropertySource(String, Map)` | 添加自定义属性源 |
| `builder().addResolver(Function)` | 添加自定义解析函数 |
| `builder().prefix(String)` | 自定义前缀（默认：`${`） |
| `builder().suffix(String)` | 自定义后缀（默认：`}`） |
| `builder().defaultValueSeparator(String)` | 自定义分隔符（默认：`:`） |
| `builder().strict(boolean)` | 未解析时抛出异常 |

### 绑定注解

| 注解 | 说明 |
|------|------|
| `@YmlProperty(value, defaultValue, required)` | 映射字段到 YAML 属性路径 |
| `@YmlAlias(String[])` | 备选属性名 |
| `@YmlIgnore` | 排除字段 |
| `@YmlNestedProperty(prefix)` | 带路径前缀的嵌套对象 |
| `@YmlValue(value, defaultValue)` | 映射字段到 YAML 值路径 |

### ConfigBinder（配置绑定）

| 方法 | 说明 |
|------|------|
| `bind(String yaml, Class<T>)` | 将 YAML 字符串绑定到配置类 |
| `bind(PropertySource, Class<T>)` | 将属性源绑定到配置类 |
| `bind(Map, Class<T>)` | 将 Map 绑定到配置类 |

### PropertySource（属性源）

| 方法 | 说明 |
|------|------|
| `fromMap(String, Map)` | 从 Map 创建 |
| `fromYaml(String, String)` | 从 YAML 字符串创建 |
| `fromEnvironment()` | 从环境变量创建 |
| `fromSystemProperties()` | 从系统属性创建 |
| `getProperty(String)` | 获取属性值 |
| `getProperty(String, Class<T>)` | 获取类型化属性值 |
| `containsProperty(String)` | 检查属性是否存在 |
| `getPropertyNames()` | 获取所有属性名 |
| `getProperties(String prefix)` | 按前缀获取属性 |

### YmlSecurity（安全工具）

| 方法 | 说明 |
|------|------|
| `validate(String)` | 验证 YAML 安全性 |
| `validate(String, long, int)` | 使用自定义限制验证 |
| `containsDangerousPatterns(String)` | 检查危险 YAML 标签 |
| `countAliases(String)` | 计算别名引用数 |
| `countAnchors(String)` | 计算锚点定义数 |
| `createSafeConfig()` | 创建安全 YmlConfig |
| `createSafeConfig(long, int)` | 使用自定义限制创建 |
| `sanitize(String)` | 移除危险模式 |
| `isSafeType(String)` | 检查类型名是否安全 |

### YmlSafeLoader（安全加载构建器）

| 方法 | 说明 |
|------|------|
| `create()` | 使用默认值创建 |
| `builder()` | 创建构建器 |
| `validate(Object)` | 验证数据结构 |
| `isAllowedType(Class<?>)` | 检查类型是否允许 |
| `isDeniedTag(String)` | 检查标签是否被拒绝 |
| `builder().allowType(Class<?>)` | 添加允许的类型 |
| `builder().denyTag(String)` | 添加拒绝的标签 |
| `builder().maxDepth(int)` | 最大嵌套深度（默认：100） |
| `builder().maxSize(int)` | 最大文档大小（默认：10MB） |
| `builder().maxAliases(int)` | 最大别名数（默认：50） |

### PathResolver / YmlPath（路径工具）

| 方法 | 说明 |
|------|------|
| `PathResolver.get(Object, String)` | 获取点号路径处的值 |
| `PathResolver.get(Object, String, T)` | 获取（带默认值） |
| `PathResolver.getString/Int/Long/Boolean(Object, String)` | 类型化访问 |
| `PathResolver.getOptional(Object, String)` | Optional 访问 |
| `PathResolver.has(Object, String)` | 检查路径是否存在 |
| `PathResolver.getList/Map(Object, String)` | 集合访问 |
| `YmlPath.of(String)` | 解析路径字符串（如 "a.b[0].c"） |
| `YmlPath.of(String...)` | 从段创建 |
| `YmlPath.root()` | 根路径 |
| `YmlPath.child(String)` | 追加属性段 |
| `YmlPath.index(int)` | 追加数组索引段 |
| `YmlPath.parent()` | 导航到父路径 |
| `YmlPath.depth()` | 获取路径深度 |

## 环境要求

- Java 25+
- 核心功能无外部依赖
- 可选：`org.yaml:snakeyaml:2.2`（用于完整 YAML 1.1 支持）

## 开源协议

Apache License 2.0

## 作者

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
