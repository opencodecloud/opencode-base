# Json 组件方案

## 1. 组件概述

### 1.1 设计目标

`opencode-base-json` 是一个统一的 JSON 处理门面组件，通过 SPI 机制适配并屏蔽底层 JSON 库（Jackson、Gson、Fastjson2）的实现差异。组件不重新实现 JSON 解析，而是提供简洁统一的 API 和智能的默认配置。

### 1.2 核心特性

- **统一门面 API**：`OpenJson.toJson()`、`OpenJson.fromJson()` 一行代码完成转换
- **SPI 可插拔引擎**：支持 Jackson、Gson、Fastjson2 自由切换
- **智能默认配置**：日期处理、null 值、命名策略开箱即用
- **泛型类型安全**：`TypeReference` 完美支持 `List<User>` 等复杂类型
- **JSONPath 支持**：无需反序列化即可读取深层字段
- **流式处理**：大 JSON 文件流式解析，避免内存溢出
- **JDK 25 增强**：Record 原生支持、sealed interface、Pattern Matching、Virtual Thread 安全
- **JSON Schema 验证**：支持 JSON Schema Draft 2020-12 验证
- **JSON Patch/Merge Patch**：RFC 6902/7396 标准实现
- **JSON Pointer**：RFC 6901 标准实现
- **JSON Diff**：JSON 文档比较与差异报告
- **响应式支持**：基于 `java.util.concurrent.Flow` 的 Reactive Streams 流式处理
- **数据脱敏**：内置手机号、身份证、邮箱等多种脱敏策略
- **安全防护**：深度/大小限制、危险键检测、XSS 防护

### 1.3 架构概览

```
┌──────────────────────────────────────────────────────────────┐
│                          应用层                                │
│       (Web API / 配置文件 / 数据交换 / 日志序列化)             │
└──────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌──────────────────────────────────────────────────────────────┐
│                     OpenJson 门面（静态 + 实例方法）            │
│   toJson / fromJson / parse / toTree / at / diff / patch     │
│   validate / createReader / createWriter                      │
└──────────────────────────────────────────────────────────────┘
                                │
        ┌───────────────────────┼───────────────────────┐
        ▼                       ▼                       ▼
┌────────────────┐   ┌────────────────┐   ┌────────────────┐
│  JsonPath      │   │  JsonPatch     │   │  JsonSchema    │
│  JsonPointer   │   │  MergePatch    │   │  Validator     │
│  (路径查询)    │   │  JsonDiff      │   │  (验证)        │
└────────────────┘   └────────────────┘   └────────────────┘
                                │
                                ▼
┌──────────────────────────────────────────────────────────────┐
│                  SPI Provider 接口 (JsonProvider)              │
│                  JsonProviderFactory (ServiceLoader)           │
└──────────────────────────────────────────────────────────────┘
        ┌───────────────────────┼───────────────────────┐
        ▼                       ▼                       ▼
┌────────────────┐   ┌────────────────┐   ┌────────────────┐
│   Jackson      │   │     Gson       │   │   Fastjson2    │
│   Provider     │   │   Provider     │   │   Provider     │
└────────────────┘   └────────────────┘   └────────────────┘
```

---

## 2. 包结构

```
cloud.opencode.base.json
├── OpenJson.java                          # JSON 统一门面（核心入口）
├── JsonConfig.java                        # JSON 配置（Builder 模式）
├── TypeReference.java                     # 泛型类型引用（解决类型擦除）
├── JsonNode.java                          # JSON 节点树模型（sealed interface）
│
├── annotation/                            # 注解包
│   ├── JsonProperty.java                  # JSON 属性名映射注解
│   ├── JsonIgnore.java                    # JSON 忽略字段注解
│   ├── JsonFormat.java                    # JSON 格式化注解（日期/数字）
│   ├── JsonNaming.java                    # JSON 命名策略注解
│   └── JsonMask.java                      # JSON 数据脱敏注解
│
├── adapter/                               # 类型适配器包
│   ├── JsonTypeAdapter.java               # 自定义类型适配器接口
│   └── JsonAdapterRegistry.java           # 适配器注册表
│
├── diff/                                  # JSON 比较包
│   └── JsonDiff.java                      # JSON 文档差异比较
│
├── exception/                             # 异常包
│   ├── OpenJsonProcessingException.java   # JSON 处理基础异常
│   └── JsonSchemaException.java           # JSON Schema 验证异常
│
├── patch/                                 # JSON 补丁包
│   ├── JsonPatch.java                     # JSON Patch (RFC 6902)
│   └── JsonMergePatch.java               # JSON Merge Patch (RFC 7396)
│
├── path/                                  # JSON 路径包
│   ├── JsonPath.java                      # JSONPath 查询（类 XPath 语法）
│   └── JsonPointer.java                   # JSON Pointer (RFC 6901)
│
├── reactive/                              # 响应式流处理包
│   ├── ReactiveJsonReader.java            # 响应式 JSON 读取器接口
│   ├── ReactiveJsonWriter.java            # 响应式 JSON 写入器接口
│   ├── DefaultReactiveJsonReader.java     # 默认响应式读取器实现
│   └── DefaultReactiveJsonWriter.java     # 默认响应式写入器实现
│
├── schema/                                # JSON Schema 验证包
│   └── JsonSchemaValidator.java           # JSON Schema 验证器
│
├── security/                              # 安全包
│   └── JsonSecurity.java                  # JSON 安全工具（脱敏/XSS防护）
│
├── spi/                                   # SPI 服务提供者包
│   ├── JsonProvider.java                  # JSON 提供者接口（SPI 核心）
│   ├── JsonProviderFactory.java           # 提供者工厂（ServiceLoader 管理）
│   └── JsonFeature.java                   # JSON 特性枚举
│
└── stream/                                # 流式处理包
    ├── JsonReader.java                    # 流式 JSON 读取器接口
    ├── JsonWriter.java                    # 流式 JSON 写入器接口
    └── JsonToken.java                     # JSON 令牌类型枚举
```

---

## 3. 核心 API

### 3.1 OpenJson - JSON 统一门面

`cloud.opencode.base.json.OpenJson`

JSON 处理的核心门面类，提供静态方法和实例方法两套 API。静态方法使用默认 Provider 和配置，实例方法使用自定义配置。

#### 工厂方法

| 方法签名 | 说明 |
|---------|------|
| `static OpenJson withConfig(JsonConfig config)` | 创建使用指定配置的 OpenJson 实例 |
| `static OpenJson withProvider(String providerName)` | 创建使用指定 Provider 的 OpenJson 实例 |
| `static OpenJson withConfigAndProvider(JsonConfig config, String providerName)` | 创建使用指定配置和 Provider 的 OpenJson 实例 |

#### 静态序列化方法

| 方法签名 | 说明 |
|---------|------|
| `static String toJson(Object obj)` | 将对象序列化为 JSON 字符串 |
| `static byte[] toJsonBytes(Object obj)` | 将对象序列化为 JSON 字节数组 |
| `static void toJson(Object obj, OutputStream out)` | 将对象序列化写入输出流 |
| `static void toJson(Object obj, Writer writer)` | 将对象序列化写入 Writer |
| `static String toPrettyJson(Object obj)` | 将对象序列化为美化的 JSON 字符串 |

#### 静态反序列化方法

| 方法签名 | 说明 |
|---------|------|
| `static <T> T fromJson(String json, Class<T> type)` | 从 JSON 字符串反序列化为对象 |
| `static <T> T fromJson(String json, TypeReference<T> typeRef)` | 从 JSON 字符串反序列化（泛型类型） |
| `static <T> T fromJson(byte[] json, Class<T> type)` | 从 JSON 字节数组反序列化 |
| `static <T> T fromJson(InputStream input, Class<T> type)` | 从输入流反序列化 |
| `static <T> T fromJson(Reader reader, Class<T> type)` | 从 Reader 反序列化 |
| `static <T> List<T> fromJsonArray(String json, Class<T> elementType)` | 从 JSON 字符串反序列化为列表 |
| `static <K, V> Map<K, V> fromJsonMap(String json, Class<K> keyType, Class<V> valueType)` | 从 JSON 字符串反序列化为 Map |

#### 静态树操作方法

| 方法签名 | 说明 |
|---------|------|
| `static JsonNode parse(String json)` | 将 JSON 字符串解析为 JsonNode 树 |
| `static JsonNode parse(byte[] json)` | 将 JSON 字节数组解析为 JsonNode 树 |
| `static JsonNode toTree(Object obj)` | 将对象转换为 JsonNode 树 |
| `static <T> T treeToValue(JsonNode node, Class<T> type)` | 将 JsonNode 树转换为对象 |

#### 静态路径查询方法

| 方法签名 | 说明 |
|---------|------|
| `static JsonNode at(String json, String pointer)` | 通过 JSON Pointer 访问值 |
| `static List<JsonNode> select(String json, String path)` | 通过 JSONPath 查询多个值 |
| `static JsonNode selectFirst(String json, String path)` | 通过 JSONPath 查询第一个匹配值 |

#### 静态 Diff/Patch 方法

| 方法签名 | 说明 |
|---------|------|
| `static JsonDiff.DiffResult diff(String source, String target)` | 比较两个 JSON 文档的差异 |
| `static JsonNode patch(String target, JsonPatch patch)` | 应用 JSON Patch (RFC 6902) |
| `static JsonNode mergePatch(String target, String patch)` | 应用 JSON Merge Patch (RFC 7396) |

#### 静态 Schema 验证方法

| 方法签名 | 说明 |
|---------|------|
| `static JsonSchemaValidator.ValidationResult validate(String json, String schema)` | 验证 JSON 是否符合 Schema |
| `static void validateOrThrow(String json, String schema)` | 验证 JSON，不通过则抛异常 |

#### 静态流式处理方法

| 方法签名 | 说明 |
|---------|------|
| `static JsonReader createReader(InputStream input)` | 创建流式 JSON 读取器 |
| `static JsonReader createReader(Reader reader)` | 创建流式 JSON 读取器 |
| `static JsonWriter createWriter(OutputStream output)` | 创建流式 JSON 写入器 |
| `static JsonWriter createWriter(Writer writer)` | 创建流式 JSON 写入器 |

#### 实例方法

| 方法签名 | 说明 |
|---------|------|
| `String serialize(Object obj)` | 序列化对象为 JSON 字符串 |
| `void serialize(Object obj, OutputStream out)` | 序列化对象写入输出流 |
| `void serialize(Object obj, Writer writer)` | 序列化对象写入 Writer |
| `byte[] serializeToBytes(Object obj)` | 序列化对象为 JSON 字节数组 |
| `<T> T deserialize(String json, Class<T> type)` | 反序列化 JSON 字符串 |
| `<T> T deserialize(String json, TypeReference<T> typeRef)` | 反序列化 JSON 字符串（泛型） |
| `<T> T deserialize(byte[] json, Class<T> type)` | 反序列化字节数组 |
| `<T> T deserialize(InputStream input, Class<T> type)` | 反序列化输入流 |
| `<T> List<T> deserializeArray(String json, Class<T> elementType)` | 反序列化为列表 |
| `<K, V> Map<K, V> deserializeMap(String json, Class<K> keyType, Class<V> valueType)` | 反序列化为 Map |
| `JsonNode parseTree(String json)` | 解析为 JsonNode 树 |
| `JsonNode parseTree(byte[] json)` | 解析字节数组为 JsonNode 树 |
| `JsonNode valueToTree(Object obj)` | 对象转 JsonNode 树 |
| `<T> T treeToObject(JsonNode node, Class<T> type)` | JsonNode 转对象 |
| `JsonConfig getConfig()` | 获取当前配置 |
| `JsonProvider getProvider()` | 获取当前 Provider |

#### 代码示例

```java
// 基本序列化/反序列化
String json = OpenJson.toJson(user);
User user = OpenJson.fromJson(json, User.class);

// 泛型反序列化
List<User> users = OpenJson.fromJson(json, new TypeReference<List<User>>() {});
List<User> users2 = OpenJson.fromJsonArray(jsonArray, User.class);
Map<String, Object> map = OpenJson.fromJsonMap(json, String.class, Object.class);

// 美化输出
String pretty = OpenJson.toPrettyJson(user);

// 自定义配置实例
OpenJson openJson = OpenJson.withConfig(
    JsonConfig.builder()
        .prettyPrint()
        .namingStrategy(JsonNaming.Strategy.SNAKE_CASE)
        .dateFormat("yyyy-MM-dd HH:mm:ss")
        .enable(JsonFeature.IGNORE_UNKNOWN_PROPERTIES)
        .build()
);
String result = openJson.serialize(user);

// JSONPath 查询
List<JsonNode> prices = OpenJson.select(json, "$.store.book[*].price");
JsonNode first = OpenJson.selectFirst(json, "$.store.book[0].title");

// JSON Pointer 访问
JsonNode value = OpenJson.at(json, "/store/book/0/title");

// JSON Diff
JsonDiff.DiffResult diff = OpenJson.diff(source, target);

// JSON Patch
JsonPatch patch = JsonPatch.builder()
    .replace("/name", JsonNode.of("Jane"))
    .add("/email", JsonNode.of("jane@example.com"))
    .build();
JsonNode patched = OpenJson.patch(targetJson, patch);

// Schema 验证
OpenJson.validateOrThrow(json, schemaJson);
```

---

### 3.2 JsonConfig - JSON 配置

`cloud.opencode.base.json.JsonConfig`

JSON 处理的配置类，使用 Builder 模式构建。

#### 静态方法

| 方法签名 | 说明 |
|---------|------|
| `static Builder builder()` | 创建配置构建器 |

#### Builder 方法

| 方法签名 | 说明 |
|---------|------|
| `Builder enable(JsonFeature feature)` | 启用指定特性 |
| `Builder disable(JsonFeature feature)` | 禁用指定特性 |
| `Builder namingStrategy(JsonNaming.Strategy strategy)` | 设置命名策略 |
| `Builder dateFormat(String pattern)` | 设置日期格式 |
| `Builder timezone(ZoneId zone)` | 设置时区 |
| `Builder timezone(String zoneId)` | 设置时区（字符串） |
| `Builder maxDepth(int maxDepth)` | 设置最大嵌套深度 |
| `Builder maxStringLength(int maxLength)` | 设置最大字符串长度 |
| `Builder maxSize(int maxSize)` | 设置最大条目数 |
| `Builder indent(String indent)` | 设置缩进字符串 |
| `Builder prettyPrint()` | 启用美化打印 |
| `JsonConfig build()` | 构建配置 |

#### 实例方法

| 方法签名 | 说明 |
|---------|------|
| `Set<JsonFeature> getEnabledFeatures()` | 获取启用的特性集合 |
| `Set<JsonFeature> getDisabledFeatures()` | 获取禁用的特性集合 |
| `JsonNaming.Strategy getNamingStrategy()` | 获取命名策略 |
| `String getDateFormat()` | 获取日期格式 |
| `ZoneId getTimezone()` | 获取时区 |
| `int getMaxDepth()` | 获取最大深度 |
| `int getMaxStringLength()` | 获取最大字符串长度 |
| `int getMaxSize()` | 获取最大条目数 |
| `String getIndent()` | 获取缩进字符串 |
| `Builder toBuilder()` | 转换为 Builder（可修改后重新构建） |

#### 代码示例

```java
JsonConfig config = JsonConfig.builder()
    .enable(JsonFeature.PRETTY_PRINT)
    .enable(JsonFeature.IGNORE_UNKNOWN_PROPERTIES)
    .disable(JsonFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
    .namingStrategy(JsonNaming.Strategy.SNAKE_CASE)
    .dateFormat("yyyy-MM-dd HH:mm:ss")
    .timezone(ZoneId.of("Asia/Shanghai"))
    .maxDepth(100)
    .maxStringLength(1_000_000)
    .build();

// 基于已有配置修改
JsonConfig modified = config.toBuilder()
    .disable(JsonFeature.PRETTY_PRINT)
    .build();
```

---

### 3.3 TypeReference - 泛型类型引用

`cloud.opencode.base.json.TypeReference<T>`

解决 Java 泛型类型擦除问题，用于反序列化复杂泛型类型。

#### 工厂方法

| 方法签名 | 说明 |
|---------|------|
| `static <T> TypeReference<T> of(Class<T> type)` | 从 Class 创建类型引用 |
| `static TypeReference<?> of(Type type)` | 从 Type 创建类型引用 |

#### 实例方法

| 方法签名 | 说明 |
|---------|------|
| `Type getType()` | 获取完整泛型类型 |
| `Class<?> getRawType()` | 获取原始类型 |
| `boolean isParameterized()` | 是否为参数化类型 |
| `Type[] getTypeArguments()` | 获取类型参数数组 |

#### 代码示例

```java
// 匿名类方式（推荐，保留完整泛型信息）
List<User> users = OpenJson.fromJson(json, new TypeReference<List<User>>() {});
Map<String, List<Order>> orders = OpenJson.fromJson(json, new TypeReference<Map<String, List<Order>>>() {});

// 简单类型
TypeReference<String> ref = TypeReference.of(String.class);
```

---

### 3.4 JsonNode - JSON 节点树模型

`cloud.opencode.base.json.JsonNode`

JSON 节点的 sealed interface，拥有 6 个 permitted 实现：`ObjectNode`、`ArrayNode`、`StringNode`、`NumberNode`、`BooleanNode`、`NullNode`。

#### 类型检查方法

| 方法签名 | 说明 |
|---------|------|
| `default boolean isObject()` | 是否为对象节点 |
| `default boolean isArray()` | 是否为数组节点 |
| `default boolean isString()` | 是否为字符串节点 |
| `default boolean isNumber()` | 是否为数字节点 |
| `default boolean isBoolean()` | 是否为布尔节点 |
| `default boolean isNull()` | 是否为 null 节点 |
| `default boolean isValue()` | 是否为值节点（字符串/数字/布尔/null） |
| `default boolean isContainer()` | 是否为容器节点（对象/数组） |

#### 值访问方法

| 方法签名 | 说明 |
|---------|------|
| `default String asString()` | 获取字符串值 |
| `default String asString(String defaultValue)` | 获取字符串值（带默认值） |
| `default int asInt()` | 获取 int 值 |
| `default int asInt(int defaultValue)` | 获取 int 值（带默认值） |
| `default long asLong()` | 获取 long 值 |
| `default long asLong(long defaultValue)` | 获取 long 值（带默认值） |
| `default double asDouble()` | 获取 double 值 |
| `default double asDouble(double defaultValue)` | 获取 double 值（带默认值） |
| `default boolean asBoolean()` | 获取 boolean 值 |
| `default boolean asBoolean(boolean defaultValue)` | 获取 boolean 值（带默认值） |
| `default BigDecimal asBigDecimal()` | 获取 BigDecimal 值 |
| `default BigInteger asBigInteger()` | 获取 BigInteger 值 |

#### 对象/数组访问方法

| 方法签名 | 说明 |
|---------|------|
| `default JsonNode get(String key)` | 获取对象属性（按键名） |
| `default JsonNode get(int index)` | 获取数组元素（按索引） |
| `default boolean has(String key)` | 对象是否包含指定键 |
| `default Set<String> keys()` | 获取对象的所有键 |
| `default int size()` | 获取元素数量 |
| `default boolean isEmpty()` | 是否为空 |

#### 路径导航方法

| 方法签名 | 说明 |
|---------|------|
| `default JsonNode at(String pointer)` | 通过 JSON Pointer 字符串导航 |
| `default JsonNode at(JsonPointer pointer)` | 通过 JsonPointer 对象导航 |
| `default List<JsonNode> select(String jsonPath)` | 通过 JSONPath 查询 |

#### 工厂方法

| 方法签名 | 说明 |
|---------|------|
| `static NullNode nullNode()` | 创建 null 节点 |
| `static StringNode of(String value)` | 创建字符串节点 |
| `static NumberNode of(Number value)` | 创建数字节点 |
| `static BooleanNode of(boolean value)` | 创建布尔节点 |
| `static JsonNode of(Object value)` | 创建节点（自动推断类型） |
| `static ObjectNode object()` | 创建空对象节点 |
| `static ArrayNode array()` | 创建空数组节点 |

#### ObjectNode 方法

| 方法签名 | 说明 |
|---------|------|
| `ObjectNode put(String key, JsonNode value)` | 设置属性 |
| `ObjectNode put(String key, String value)` | 设置字符串属性 |
| `ObjectNode put(String key, Number value)` | 设置数字属性 |
| `ObjectNode put(String key, boolean value)` | 设置布尔属性 |
| `ObjectNode putNull(String key)` | 设置 null 属性 |
| `ObjectNode putObject(String key)` | 设置空对象属性 |
| `ArrayNode putArray(String key)` | 设置空数组属性 |
| `ObjectNode remove(String key)` | 移除属性 |
| `Map<String, JsonNode> toMap()` | 转换为 Map |

#### ArrayNode 方法

| 方法签名 | 说明 |
|---------|------|
| `ArrayNode add(JsonNode value)` | 添加元素 |
| `ArrayNode add(String value)` | 添加字符串元素 |
| `ArrayNode add(Number value)` | 添加数字元素 |
| `ArrayNode add(boolean value)` | 添加布尔元素 |
| `ArrayNode addNull()` | 添加 null 元素 |
| `ObjectNode addObject()` | 添加空对象元素 |
| `ArrayNode addArray()` | 添加空数组元素 |
| `ArrayNode set(int index, JsonNode value)` | 设置指定索引元素 |
| `ArrayNode remove(int index)` | 移除指定索引元素 |
| `List<JsonNode> toList()` | 转换为 List |
| `Iterator<JsonNode> iterator()` | 获取迭代器 |

#### 代码示例

```java
// 构建 JsonNode 树
JsonNode.ObjectNode root = JsonNode.object()
    .put("name", "John")
    .put("age", 30)
    .put("active", true)
    .putNull("middleName");

root.putArray("tags")
    .add("java")
    .add("json");

root.putObject("address")
    .put("city", "Beijing")
    .put("zip", "100000");

// 解析与访问
JsonNode node = OpenJson.parse("{\"users\":[{\"name\":\"Alice\"},{\"name\":\"Bob\"}]}");
String name = node.get("users").get(0).get("name").asString();   // "Alice"
int size = node.get("users").size();                               // 2

// JSON Pointer 导航
JsonNode alice = node.at("/users/0/name");  // StringNode("Alice")

// JSONPath 查询
List<JsonNode> names = node.select("$.users[*].name");

// sealed interface 模式匹配
switch (node) {
    case JsonNode.ObjectNode obj -> System.out.println("Object with keys: " + obj.keys());
    case JsonNode.ArrayNode arr -> System.out.println("Array with " + arr.size() + " elements");
    case JsonNode.StringNode str -> System.out.println("String: " + str.asString());
    case JsonNode.NumberNode num -> System.out.println("Number: " + num.asDouble());
    case JsonNode.BooleanNode bool -> System.out.println("Boolean: " + bool.asBoolean());
    case JsonNode.NullNode _ -> System.out.println("Null");
}
```

---

### 3.5 JsonPath - JSONPath 查询

`cloud.opencode.base.json.path.JsonPath`

JSONPath 查询引擎，支持类 XPath 的语法来查询 JSON 文档。

#### 静态方法

| 方法签名 | 说明 |
|---------|------|
| `static JsonPath compile(String expression)` | 编译 JSONPath 表达式 |
| `static List<JsonNode> read(JsonNode root, String expression)` | 查询所有匹配节点 |
| `static JsonNode readFirst(JsonNode root, String expression)` | 查询第一个匹配节点 |
| `static boolean exists(JsonNode root, String expression)` | 检查路径是否存在 |

#### 实例方法

| 方法签名 | 说明 |
|---------|------|
| `List<JsonNode> evaluate(JsonNode root)` | 对根节点求值 |
| `JsonNode evaluateFirst(JsonNode root)` | 返回第一个匹配节点 |
| `String getExpression()` | 获取表达式字符串 |

#### 内部类型 - PathSegment（sealed interface）

| 实现类 | 说明 |
|-------|------|
| `PropertySegment(String property)` | 属性访问段 |
| `ArrayIndexSegment(int index)` | 数组索引段 |
| `ArraySliceSegment(Integer start, Integer end, int step)` | 数组切片段 |
| `WildcardSegment` | 通配符段（*） |
| `ArrayWildcardSegment` | 数组通配符段 |
| `RecursiveDescentSegment(String property)` | 递归下降段（..） |
| `FilterSegment(String expression)` | 过滤器段 |

#### 代码示例

```java
JsonNode root = OpenJson.parse("""
    {
        "store": {
            "book": [
                {"title": "Java", "price": 29.99},
                {"title": "Kotlin", "price": 35.99}
            ]
        }
    }
    """);

// 查询所有书的标题
List<JsonNode> titles = JsonPath.read(root, "$.store.book[*].title");

// 查询第一本书的价格
JsonNode price = JsonPath.readFirst(root, "$.store.book[0].price");

// 编译复用
JsonPath path = JsonPath.compile("$.store.book[*].price");
List<JsonNode> prices = path.evaluate(root);

// 检查路径存在
boolean exists = JsonPath.exists(root, "$.store.book[0].isbn");
```

---

### 3.6 JsonPointer - JSON Pointer (RFC 6901)

`cloud.opencode.base.json.path.JsonPointer`

RFC 6901 标准的 JSON Pointer 实现，用于精确定位 JSON 文档中的特定值。

#### 常量

| 常量 | 说明 |
|-----|------|
| `static final JsonPointer ROOT` | 根指针（空指针） |

#### 静态方法

| 方法签名 | 说明 |
|---------|------|
| `static JsonPointer parse(String pointer)` | 解析 JSON Pointer 字符串 |
| `static JsonPointer of(String... tokens)` | 从引用令牌创建 |
| `static JsonPointer of(List<String> tokens)` | 从令牌列表创建 |

#### 实例方法

| 方法签名 | 说明 |
|---------|------|
| `JsonNode evaluate(JsonNode root)` | 求值（路径不存在则抛异常） |
| `JsonNode evaluateOrNull(JsonNode root)` | 求值（路径不存在返回 null） |
| `boolean exists(JsonNode root)` | 检查路径是否存在 |
| `JsonPointer parent()` | 获取父指针 |
| `JsonPointer append(String property)` | 追加属性 |
| `JsonPointer append(int index)` | 追加数组索引 |
| `String getLastToken()` | 获取最后一个令牌 |
| `List<String> getTokens()` | 获取所有令牌 |
| `int depth()` | 获取深度 |
| `boolean isRoot()` | 是否为根指针 |

#### 代码示例

```java
JsonNode root = OpenJson.parse("{\"foo\":{\"bar\":[1,2,3]}}");

// 解析并求值
JsonPointer pointer = JsonPointer.parse("/foo/bar/1");
JsonNode value = pointer.evaluate(root);  // NumberNode(2)

// 安全求值
JsonNode maybe = JsonPointer.parse("/foo/baz").evaluateOrNull(root);  // null

// 构建指针
JsonPointer p = JsonPointer.of("foo", "bar", "0");
boolean exists = p.exists(root);  // true

// 指针操作
JsonPointer parent = pointer.parent();        // /foo/bar
JsonPointer child = parent.append("2");       // /foo/bar/2
```

---

### 3.7 JsonDiff - JSON 文档比较

`cloud.opencode.base.json.diff.JsonDiff`

比较两个 JSON 文档并生成差异报告，可转换为 JSON Patch。

#### 静态方法

| 方法签名 | 说明 |
|---------|------|
| `static DiffResult diff(JsonNode source, JsonNode target)` | 比较两个文档 |
| `static boolean equals(JsonNode source, JsonNode target)` | 检查两个文档是否相等 |

#### DiffResult 记录（record）

| 方法签名 | 说明 |
|---------|------|
| `boolean isIdentical()` | 文档是否相同 |
| `boolean hasDifferences()` | 是否有差异 |
| `int getDifferenceCount()` | 获取差异数量 |
| `List<Difference> getDifferencesByType(DiffType type)` | 按类型筛选差异 |
| `JsonPatch toPatch()` | 转换为 JSON Patch |
| `String getSummary()` | 获取可读摘要 |

#### DiffType 枚举

| 枚举值 | 说明 |
|-------|------|
| `ADDED` | 值被添加 |
| `REMOVED` | 值被移除 |
| `CHANGED` | 值被更改 |
| `TYPE_CHANGED` | 类型被更改 |

#### Difference 记录（record）

| 字段 | 类型 | 说明 |
|-----|------|------|
| `type` | `DiffType` | 差异类型 |
| `path` | `String` | JSON Pointer 路径 |
| `sourceValue` | `JsonNode` | 源文档值 |
| `targetValue` | `JsonNode` | 目标文档值 |

#### 代码示例

```java
JsonNode source = OpenJson.parse("{\"name\":\"John\",\"age\":30}");
JsonNode target = OpenJson.parse("{\"name\":\"Jane\",\"email\":\"jane@example.com\"}");

JsonDiff.DiffResult result = JsonDiff.diff(source, target);

// 检查差异
System.out.println(result.getSummary());
// "Differences: 1 removed, 1 changed, 1 added"

// 遍历差异
for (JsonDiff.Difference diff : result.differences()) {
    System.out.println(diff);
}

// 转换为 JSON Patch 并应用
JsonPatch patch = result.toPatch();
JsonNode patched = patch.apply(source);  // 等于 target
```

---

### 3.8 JsonPatch - JSON Patch (RFC 6902)

`cloud.opencode.base.json.patch.JsonPatch`

RFC 6902 标准实现，定义一系列操作来修改 JSON 文档。

#### 静态方法

| 方法签名 | 说明 |
|---------|------|
| `static Builder builder()` | 创建构建器 |
| `static JsonPatch of(List<PatchOperation> operations)` | 从操作列表创建 |

#### 实例方法

| 方法签名 | 说明 |
|---------|------|
| `JsonNode apply(JsonNode target)` | 应用补丁 |
| `boolean validate(JsonNode target)` | 验证补丁是否可应用 |
| `List<PatchOperation> getOperations()` | 获取操作列表 |
| `int size()` | 获取操作数量 |

#### Builder 方法

| 方法签名 | 说明 |
|---------|------|
| `Builder add(String path, JsonNode value)` | 添加 add 操作 |
| `Builder remove(String path)` | 添加 remove 操作 |
| `Builder replace(String path, JsonNode value)` | 添加 replace 操作 |
| `Builder move(String from, String path)` | 添加 move 操作 |
| `Builder copy(String from, String path)` | 添加 copy 操作 |
| `Builder test(String path, JsonNode expected)` | 添加 test 操作 |
| `JsonPatch build()` | 构建补丁 |

#### Operation 枚举

| 枚举值 | 说明 |
|-------|------|
| `ADD` | 添加值 |
| `REMOVE` | 移除值 |
| `REPLACE` | 替换值 |
| `MOVE` | 移动值 |
| `COPY` | 复制值 |
| `TEST` | 测试值 |

#### PatchOperation 记录（record）

| 字段 | 类型 | 说明 |
|-----|------|------|
| `op` | `Operation` | 操作类型 |
| `path` | `String` | 目标路径 |
| `from` | `String` | 源路径（move/copy） |
| `value` | `JsonNode` | 操作值 |

#### 代码示例

```java
JsonNode target = OpenJson.parse("{\"name\":\"John\",\"age\":30}");

JsonPatch patch = JsonPatch.builder()
    .replace("/name", JsonNode.of("Jane"))
    .add("/email", JsonNode.of("jane@example.com"))
    .remove("/age")
    .build();

JsonNode result = patch.apply(target);
// {"name":"Jane","email":"jane@example.com"}

// 验证补丁
boolean valid = patch.validate(target);  // true
```

---

### 3.9 JsonMergePatch - JSON Merge Patch (RFC 7396)

`cloud.opencode.base.json.patch.JsonMergePatch`

RFC 7396 标准实现，提供更简单的部分修改方式。

#### 静态方法

| 方法签名 | 说明 |
|---------|------|
| `static JsonMergePatch of(JsonNode patch)` | 从 JsonNode 创建 |
| `static Builder builder()` | 创建构建器 |
| `static JsonNode apply(JsonNode target, JsonNode patch)` | 应用合并补丁（静态） |
| `static JsonMergePatch diff(JsonNode source, JsonNode target)` | 生成差异补丁 |

#### 实例方法

| 方法签名 | 说明 |
|---------|------|
| `JsonNode apply(JsonNode target)` | 应用合并补丁 |
| `JsonNode getPatch()` | 获取补丁内容 |

#### Builder 方法

| 方法签名 | 说明 |
|---------|------|
| `Builder set(String key, JsonNode value)` | 设置属性 |
| `Builder set(String key, String value)` | 设置字符串属性 |
| `Builder set(String key, Number value)` | 设置数字属性 |
| `Builder set(String key, boolean value)` | 设置布尔属性 |
| `Builder remove(String key)` | 移除属性（设为 null） |
| `JsonMergePatch build()` | 构建合并补丁 |
| `JsonNode apply(JsonNode target)` | 构建并应用 |

#### 代码示例

```java
JsonNode target = OpenJson.parse("{\"name\":\"John\",\"age\":30,\"city\":\"NYC\"}");

// 静态方式
JsonNode patch = OpenJson.parse("{\"age\":31,\"city\":null,\"email\":\"john@example.com\"}");
JsonNode result = JsonMergePatch.apply(target, patch);
// {"name":"John","age":31,"email":"john@example.com"}

// Builder 方式
JsonNode result2 = JsonMergePatch.builder()
    .set("name", "Jane")
    .remove("age")
    .set("email", "jane@example.com")
    .apply(target);

// 生成差异补丁
JsonMergePatch diffPatch = JsonMergePatch.diff(source, target);
```

---

### 3.10 JsonSchemaValidator - JSON Schema 验证器

`cloud.opencode.base.json.schema.JsonSchemaValidator`

JSON Schema Draft 2020-12 的子集实现。

#### 支持的关键字

- **类型**: type, enum, const
- **字符串**: minLength, maxLength, pattern, format
- **数字**: minimum, maximum, exclusiveMinimum, exclusiveMaximum, multipleOf
- **数组**: minItems, maxItems, uniqueItems, items
- **对象**: properties, required, additionalProperties, minProperties, maxProperties
- **组合**: allOf, anyOf, oneOf, not

#### 静态方法

| 方法签名 | 说明 |
|---------|------|
| `static JsonSchemaValidator of(JsonNode schema)` | 创建验证器实例 |
| `static ValidationResult validate(JsonNode data, JsonNode schema)` | 验证数据 |
| `static void validateOrThrow(JsonNode data, JsonNode schema)` | 验证数据（失败抛异常） |

#### 实例方法

| 方法签名 | 说明 |
|---------|------|
| `ValidationResult validate(JsonNode data)` | 验证数据 |
| `void validateOrThrow(JsonNode data)` | 验证数据（失败抛异常） |

#### ValidationResult 记录（record）

| 方法签名 | 说明 |
|---------|------|
| `boolean isValid()` | 验证是否通过 |
| `List<ValidationError> getErrors()` | 获取错误列表 |
| `static ValidationResult success()` | 创建成功结果 |
| `static ValidationResult failure(List<ValidationError> errors)` | 创建失败结果 |
| `static ValidationResult failure(ValidationError error)` | 创建单错误失败结果 |

#### 代码示例

```java
JsonNode schema = OpenJson.parse("""
    {
        "type": "object",
        "properties": {
            "name": {"type": "string", "minLength": 1},
            "age": {"type": "integer", "minimum": 0, "maximum": 200},
            "email": {"type": "string", "pattern": "^.+@.+$"}
        },
        "required": ["name", "age"]
    }
    """);

JsonNode data = OpenJson.parse("{\"name\":\"John\",\"age\":30}");

// 验证
JsonSchemaValidator.ValidationResult result = JsonSchemaValidator.validate(data, schema);
if (!result.isValid()) {
    result.getErrors().forEach(error -> System.err.println(error));
}

// 复用验证器
JsonSchemaValidator validator = JsonSchemaValidator.of(schema);
validator.validateOrThrow(data);
```

---

### 3.11 JsonSecurity - JSON 安全工具

`cloud.opencode.base.json.security.JsonSecurity`

提供数据脱敏、深度/大小验证、危险键检测和 XSS 防护。

#### 常量

| 常量 | 类型 | 值 | 说明 |
|-----|------|---|------|
| `DEFAULT_MAX_DEPTH` | `int` | `1000` | 默认最大深度 |
| `DEFAULT_MAX_STRING_LENGTH` | `int` | `20_000_000` | 默认最大字符串长度 |
| `DEFAULT_MAX_ENTRIES` | `int` | `100_000` | 默认最大条目数 |

#### 数据脱敏方法

| 方法签名 | 说明 |
|---------|------|
| `static String mask(String value, JsonMask.MaskType type)` | 按脱敏类型脱敏 |
| `static String mask(String value, JsonMask.MaskType type, char maskChar)` | 自定义脱敏字符 |
| `static String mask(String value, int prefixLength, int suffixLength, char maskChar)` | 自定义前后缀长度 |
| `static String maskWithPattern(String value, String pattern, String replacement)` | 正则脱敏 |

#### 验证方法

| 方法签名 | 说明 |
|---------|------|
| `static void validateDepth(JsonNode node, int maxDepth)` | 验证 JSON 深度 |
| `static void validateSize(JsonNode node, int maxSize)` | 验证 JSON 大小 |
| `static int calculateDepth(JsonNode node)` | 计算 JSON 深度 |
| `static int calculateSize(JsonNode node)` | 计算 JSON 大小 |

#### 安全检查方法

| 方法签名 | 说明 |
|---------|------|
| `static List<String> findDangerousKeys(JsonNode node)` | 查找危险键 |
| `static boolean hasDangerousKeys(JsonNode node)` | 是否包含危险键 |

#### XSS 防护方法

| 方法签名 | 说明 |
|---------|------|
| `static String sanitizeForHtml(String value)` | 净化字符串 |
| `static JsonNode sanitizeForHtml(JsonNode node)` | 净化 JSON 节点 |

#### 安全验证方法

| 方法签名 | 说明 |
|---------|------|
| `static void validate(JsonNode node, SecurityOptions options)` | 根据安全选项验证 |

#### SecurityOptions 记录（record）

| 字段 | 类型 | 说明 |
|-----|------|------|
| `maxDepth` | `int` | 最大深度 |
| `maxStringLength` | `int` | 最大字符串长度 |
| `maxEntries` | `int` | 最大条目数 |
| `rejectDangerousKeys` | `boolean` | 是否拒绝危险键 |
| `sanitizeStrings` | `boolean` | 是否净化字符串 |

| 方法签名 | 说明 |
|---------|------|
| `static SecurityOptions defaults()` | 获取默认选项 |
| `static Builder builder()` | 创建构建器 |

#### 代码示例

```java
// 数据脱敏
String maskedPhone = JsonSecurity.mask("13812345678", JsonMask.MaskType.PHONE);
// "138****5678"

String maskedEmail = JsonSecurity.mask("test@example.com", JsonMask.MaskType.EMAIL);
// "t***@example.com"

String maskedId = JsonSecurity.mask("110101199001011234", JsonMask.MaskType.ID_CARD);
// "110***********1234"

// 自定义脱敏
String custom = JsonSecurity.mask("1234567890", 2, 3, '*');
// "12*****890"

// 安全验证
JsonSecurity.validateDepth(jsonNode, 50);
JsonSecurity.validateSize(jsonNode, 10000);

// XSS 防护
String safe = JsonSecurity.sanitizeForHtml("<script>alert('xss')</script>");
JsonNode safeNode = JsonSecurity.sanitizeForHtml(untrustedNode);

// 综合安全验证
JsonSecurity.SecurityOptions options = JsonSecurity.SecurityOptions.builder()
    .maxDepth(100)
    .maxEntries(50000)
    .rejectDangerousKeys(true)
    .build();
JsonSecurity.validate(node, options);
```

---

### 3.12 JsonAdapterRegistry - 适配器注册表

`cloud.opencode.base.json.adapter.JsonAdapterRegistry`

管理自定义类型适配器的全局注册表。内置 Java 时间类型、UUID、Currency、Locale、TimeZone、Optional 等适配器。

#### 静态方法

| 方法签名 | 说明 |
|---------|------|
| `static <T> void register(JsonTypeAdapter<T> adapter)` | 注册适配器 |
| `static <T> void register(Class<T> type, JsonTypeAdapter<T> adapter)` | 为特定类型注册适配器 |
| `static void registerFactory(AdapterFactory factory)` | 注册适配器工厂 |
| `static <T> JsonTypeAdapter<T> getAdapter(Class<T> type)` | 获取类型适配器 |
| `static JsonTypeAdapter<?> getAdapter(Type type)` | 获取泛型类型适配器 |
| `static boolean hasAdapter(Class<?> type)` | 检查适配器是否存在 |
| `static JsonTypeAdapter<?> unregister(Class<?> type)` | 注销适配器 |
| `static Set<Type> getRegisteredTypes()` | 获取所有注册类型 |
| `static void clear()` | 清除所有自定义适配器 |
| `static Registry createRegistry()` | 创建隔离注册表 |

#### AdapterFactory 接口

| 方法签名 | 说明 |
|---------|------|
| `JsonTypeAdapter<?> create(Type type)` | 为给定类型创建适配器 |

#### Registry 内部类

| 方法签名 | 说明 |
|---------|------|
| `<T> void register(JsonTypeAdapter<T> adapter)` | 注册适配器 |
| `<T> void register(Class<T> type, JsonTypeAdapter<T> adapter)` | 为类型注册适配器 |
| `void registerFactory(AdapterFactory factory)` | 注册工厂 |
| `<T> JsonTypeAdapter<T> getAdapter(Class<T> type)` | 获取适配器（含回退全局） |

#### 内置适配器

- `LocalDate`, `LocalTime`, `LocalDateTime`, `Instant`, `ZonedDateTime`, `OffsetDateTime`
- `Duration`, `Period`
- `UUID`, `Currency`, `Locale`, `TimeZone`, `ZoneId`
- `Optional<T>`（通过工厂动态创建）

---

### 3.13 JsonTypeAdapter - 类型适配器接口

`cloud.opencode.base.json.adapter.JsonTypeAdapter<T>`

自定义类型序列化/反序列化的适配器接口。

#### 抽象方法

| 方法签名 | 说明 |
|---------|------|
| `Class<T> getType()` | 获取处理的类型 |
| `JsonNode toJson(T value)` | 序列化为 JsonNode |
| `T fromJson(JsonNode node)` | 从 JsonNode 反序列化 |

#### 默认方法

| 方法签名 | 说明 |
|---------|------|
| `default Type getGenericType()` | 获取泛型类型 |
| `default void write(JsonWriter writer, T value)` | 流式写入 |
| `default T read(JsonReader reader)` | 流式读取 |
| `default boolean supportsStreaming()` | 是否支持流式处理 |
| `default boolean handlesNull()` | 是否处理 null |

#### 静态工厂方法

| 方法签名 | 说明 |
|---------|------|
| `static <T> JsonTypeAdapter<T> of(Class<T> type, Function<T, JsonNode> serializer, Function<JsonNode, T> deserializer)` | 从 lambda 创建适配器 |
| `static <T> JsonTypeAdapter<T> ofString(Class<T> type, Function<T, String> toString, Function<String, T> fromString)` | 创建字符串转换适配器 |

#### 代码示例

```java
// 自定义适配器
public class MoneyAdapter implements JsonTypeAdapter<Money> {
    @Override
    public Class<Money> getType() { return Money.class; }

    @Override
    public JsonNode toJson(Money value) {
        return JsonNode.object()
            .put("amount", value.getAmount())
            .put("currency", value.getCurrency());
    }

    @Override
    public Money fromJson(JsonNode node) {
        return new Money(
            node.get("amount").asBigDecimal(),
            node.get("currency").asString()
        );
    }
}
JsonAdapterRegistry.register(new MoneyAdapter());

// 简单 lambda 适配器
JsonAdapterRegistry.register(JsonTypeAdapter.ofString(
    Color.class,
    color -> "#" + Integer.toHexString(color.getRGB()),
    hex -> Color.decode(hex)
));
```

---

### 3.14 JsonFeature - JSON 特性枚举

`cloud.opencode.base.json.spi.JsonFeature`

控制 JSON 序列化、反序列化和安全行为的特性。

#### 序列化特性（SERIALIZATION）

| 枚举值 | 说明 |
|-------|------|
| `PRETTY_PRINT` | 美化打印 |
| `WRITE_DATES_AS_ISO8601` | 日期格式化为 ISO 8601 |
| `WRITE_ENUMS_USING_NAME` | 枚举使用名称 |
| `WRITE_NULL_MAP_VALUES` | 写入 Map 的 null 值 |
| `WRITE_EMPTY_ARRAYS_FOR_NULL` | null 写为空数组 |
| `SORT_MAP_KEYS` | Map 键排序 |
| `ESCAPE_NON_ASCII` | 转义非 ASCII 字符 |
| `INCLUDE_NULL_PROPERTIES` | 包含 null 属性 |
| `INCLUDE_EMPTY_COLLECTIONS` | 包含空集合 |

#### 反序列化特性（DESERIALIZATION）

| 枚举值 | 说明 |
|-------|------|
| `IGNORE_UNKNOWN_PROPERTIES` | 忽略未知属性 |
| `ACCEPT_SINGLE_VALUE_AS_ARRAY` | 单值作为数组 |
| `ACCEPT_EMPTY_STRING_AS_NULL` | 空字符串作为 null |
| `FAIL_ON_NULL_FOR_PRIMITIVES` | null 对应原始类型时失败 |
| `FAIL_ON_NUMBERS_FOR_ENUMS` | 数字对应枚举时失败 |
| `USE_BIG_DECIMAL_FOR_FLOATS` | 浮点数使用 BigDecimal |
| `USE_BIG_INTEGER_FOR_INTS` | 整数使用 BigInteger |
| `ALLOW_COMMENTS` | 允许注释 |
| `ALLOW_TRAILING_COMMA` | 允许尾逗号 |
| `ALLOW_UNQUOTED_FIELD_NAMES` | 允许无引号字段名 |
| `ALLOW_SINGLE_QUOTES` | 允许单引号 |

#### 安全特性（SECURITY）

| 枚举值 | 说明 |
|-------|------|
| `LIMIT_STRING_LENGTH` | 限制字符串长度 |
| `LIMIT_NESTING_DEPTH` | 限制嵌套深度 |
| `LIMIT_ENTRY_COUNT` | 限制条目数 |

#### 方法

| 方法签名 | 说明 |
|---------|------|
| `Category getCategory()` | 获取特性类别 |
| `boolean isEnabledByDefault()` | 是否默认启用 |
| `boolean isSerializationFeature()` | 是否为序列化特性 |
| `boolean isDeserializationFeature()` | 是否为反序列化特性 |
| `boolean isSecurityFeature()` | 是否为安全特性 |

---

### 3.15 JsonProvider - JSON 提供者接口

`cloud.opencode.base.json.spi.JsonProvider`

SPI 服务提供者接口，定义底层 JSON 库必须实现的方法。

#### 提供者信息方法

| 方法签名 | 说明 |
|---------|------|
| `String getName()` | 获取提供者名称 |
| `String getVersion()` | 获取提供者版本 |
| `int getPriority()` | 获取优先级（越高越优先） |
| `boolean isAvailable()` | 检查是否可用 |
| `void configure(JsonConfig config)` | 应用配置 |
| `boolean supportsFeature(JsonFeature feature)` | 是否支持指定特性 |

#### 序列化方法

| 方法签名 | 说明 |
|---------|------|
| `String toJson(Object obj)` | 序列化为字符串 |
| `byte[] toJsonBytes(Object obj)` | 序列化为字节数组 |
| `void toJson(Object obj, OutputStream output)` | 序列化写入输出流 |
| `void toJson(Object obj, Writer writer)` | 序列化写入 Writer |

#### 反序列化方法

| 方法签名 | 说明 |
|---------|------|
| `<T> T fromJson(String json, Class<T> type)` | 从字符串反序列化 |
| `<T> T fromJson(String json, TypeReference<T> typeRef)` | 从字符串反序列化（泛型） |
| `<T> T fromJson(byte[] json, Class<T> type)` | 从字节数组反序列化 |
| `<T> T fromJson(InputStream input, Class<T> type)` | 从输入流反序列化 |
| `<T> T fromJson(Reader reader, Class<T> type)` | 从 Reader 反序列化 |
| `<T> T fromJson(String json, Type type)` | 从字符串反序列化（Type） |
| `<T> List<T> fromJsonArray(String json, Class<T> elementType)` | 反序列化为列表 |
| `<K, V> Map<K, V> fromJsonMap(String json, Class<K> keyType, Class<V> valueType)` | 反序列化为 Map |

#### 树操作方法

| 方法签名 | 说明 |
|---------|------|
| `JsonNode parseTree(String json)` | 解析为 JsonNode |
| `JsonNode parseTree(byte[] json)` | 解析字节数组为 JsonNode |
| `<T> T treeToValue(JsonNode node, Class<T> type)` | JsonNode 转对象 |
| `JsonNode valueToTree(Object obj)` | 对象转 JsonNode |

#### 流式处理方法

| 方法签名 | 说明 |
|---------|------|
| `JsonReader createReader(InputStream input)` | 创建流式读取器 |
| `JsonReader createReader(Reader reader)` | 创建流式读取器 |
| `JsonWriter createWriter(OutputStream output)` | 创建流式写入器 |
| `JsonWriter createWriter(Writer writer)` | 创建流式写入器 |

#### 工具方法

| 方法签名 | 说明 |
|---------|------|
| `<T> T convertValue(Object obj, Class<T> type)` | 类型转换 |
| `<T> T convertValue(Object obj, TypeReference<T> typeRef)` | 类型转换（泛型） |
| `Object getUnderlyingProvider()` | 获取底层 Provider 实例 |
| `JsonProvider copy()` | 复制 Provider（独立配置） |

---

### 3.16 JsonProviderFactory - 提供者工厂

`cloud.opencode.base.json.spi.JsonProviderFactory`

管理 JSON Provider 的发现、注册和选择。通过 ServiceLoader 自动发现。

#### 获取 Provider 方法

| 方法签名 | 说明 |
|---------|------|
| `static JsonProvider getProvider()` | 获取默认 Provider |
| `static JsonProvider getProvider(String name)` | 按名称获取 |
| `static JsonProvider getProvider(JsonConfig config)` | 获取配置后的 Provider |
| `static JsonProvider getProvider(String name, JsonConfig config)` | 按名称获取并配置 |

#### 查询方法

| 方法签名 | 说明 |
|---------|------|
| `static List<String> getAvailableProviders()` | 获取可用 Provider 名称列表 |
| `static Collection<JsonProvider> getAllProviders()` | 获取所有注册 Provider |
| `static boolean hasProvider(String name)` | 检查指定名称是否可用 |
| `static boolean hasAnyProvider()` | 是否有任何可用 Provider |
| `static List<ProviderInfo> getProviderInfo()` | 获取所有 Provider 信息 |

#### 管理方法

| 方法签名 | 说明 |
|---------|------|
| `static void setDefaultProvider(JsonProvider provider)` | 设置默认 Provider |
| `static void setDefaultProvider(String name)` | 按名称设置默认 Provider |
| `static void registerProvider(JsonProvider provider)` | 注册 Provider |
| `static JsonProvider unregisterProvider(String name)` | 注销 Provider |
| `static void reload()` | 重新加载所有 Provider |

#### ProviderInfo 记录（record）

| 字段 | 类型 | 说明 |
|-----|------|------|
| `name` | `String` | 提供者名称 |
| `version` | `String` | 提供者版本 |
| `priority` | `int` | 优先级 |
| `isDefault` | `boolean` | 是否为默认 |

---

### 3.17 JsonReader - 流式 JSON 读取器

`cloud.opencode.base.json.stream.JsonReader`

流式（拉取式）JSON 解析器接口，适用于大文件的内存高效解析。实现 `Closeable`。

#### 结构导航方法

| 方法签名 | 说明 |
|---------|------|
| `void beginObject()` | 开始读取对象 |
| `void endObject()` | 结束读取对象 |
| `void beginArray()` | 开始读取数组 |
| `void endArray()` | 结束读取数组 |

#### 令牌检查方法

| 方法签名 | 说明 |
|---------|------|
| `boolean hasNext()` | 是否还有更多元素 |
| `JsonToken peek()` | 查看下一个令牌类型（不消费） |

#### 值读取方法

| 方法签名 | 说明 |
|---------|------|
| `String nextName()` | 读取属性名 |
| `String nextString()` | 读取字符串值 |
| `boolean nextBoolean()` | 读取布尔值 |
| `void nextNull()` | 消费 null 值 |
| `int nextInt()` | 读取 int 值 |
| `long nextLong()` | 读取 long 值 |
| `double nextDouble()` | 读取 double 值 |
| `BigInteger nextBigInteger()` | 读取 BigInteger 值 |
| `BigDecimal nextBigDecimal()` | 读取 BigDecimal 值 |
| `Number nextNumber()` | 读取 Number 值 |

#### 跳过方法

| 方法签名 | 说明 |
|---------|------|
| `void skipValue()` | 跳过下一个值 |
| `default void skipObject()` | 跳过当前对象 |
| `default void skipArray()` | 跳过当前数组 |

#### 位置信息方法

| 方法签名 | 说明 |
|---------|------|
| `String getPath()` | 获取当前路径 |
| `int getLineNumber()` | 获取当前行号 |
| `int getColumnNumber()` | 获取当前列号 |

#### 配置方法

| 方法签名 | 说明 |
|---------|------|
| `void setLenient(boolean lenient)` | 设置宽松模式 |
| `boolean isLenient()` | 是否宽松模式 |

#### 代码示例

```java
try (JsonReader reader = OpenJson.createReader(inputStream)) {
    reader.beginObject();
    while (reader.hasNext()) {
        String name = reader.nextName();
        if ("id".equals(name)) {
            long id = reader.nextLong();
        } else if ("name".equals(name)) {
            String value = reader.nextString();
        } else {
            reader.skipValue();
        }
    }
    reader.endObject();
}
```

---

### 3.18 JsonWriter - 流式 JSON 写入器

`cloud.opencode.base.json.stream.JsonWriter`

流式（推送式）JSON 生成器接口，适用于大文件生成。实现 `Closeable`、`Flushable`。支持链式调用。

#### 结构写入方法

| 方法签名 | 说明 |
|---------|------|
| `JsonWriter beginObject()` | 开始写入对象 |
| `JsonWriter endObject()` | 结束写入对象 |
| `JsonWriter beginArray()` | 开始写入数组 |
| `JsonWriter endArray()` | 结束写入数组 |

#### 名称写入方法

| 方法签名 | 说明 |
|---------|------|
| `JsonWriter name(String name)` | 写入属性名 |

#### 值写入方法

| 方法签名 | 说明 |
|---------|------|
| `JsonWriter value(String value)` | 写入字符串值 |
| `JsonWriter value(boolean value)` | 写入布尔值 |
| `JsonWriter value(int value)` | 写入 int 值 |
| `JsonWriter value(long value)` | 写入 long 值 |
| `JsonWriter value(double value)` | 写入 double 值 |
| `default JsonWriter value(float value)` | 写入 float 值 |
| `JsonWriter value(Number value)` | 写入 Number 值 |
| `default JsonWriter value(BigInteger value)` | 写入 BigInteger 值 |
| `default JsonWriter value(BigDecimal value)` | 写入 BigDecimal 值 |
| `JsonWriter nullValue()` | 写入 null 值 |
| `JsonWriter jsonValue(String json)` | 写入原始 JSON（不转义） |

#### 便捷方法

| 方法签名 | 说明 |
|---------|------|
| `default JsonWriter property(String name, String value)` | 写入名称/字符串对 |
| `default JsonWriter property(String name, boolean value)` | 写入名称/布尔对 |
| `default JsonWriter property(String name, int value)` | 写入名称/int 对 |
| `default JsonWriter property(String name, long value)` | 写入名称/long 对 |
| `default JsonWriter property(String name, double value)` | 写入名称/double 对 |
| `default JsonWriter property(String name, Number value)` | 写入名称/Number 对 |
| `default JsonWriter propertyNull(String name)` | 写入名称/null 对 |

#### 配置方法

| 方法签名 | 说明 |
|---------|------|
| `JsonWriter setIndent(String indent)` | 设置缩进 |
| `JsonWriter setSerializeNulls(boolean serializeNulls)` | 设置是否序列化 null |
| `JsonWriter setLenient(boolean lenient)` | 设置宽松模式 |
| `boolean isLenient()` | 是否宽松模式 |
| `JsonWriter setHtmlSafe(boolean htmlSafe)` | 设置 HTML 安全模式 |
| `boolean isHtmlSafe()` | 是否 HTML 安全 |

#### 代码示例

```java
try (JsonWriter writer = OpenJson.createWriter(outputStream)) {
    writer.beginObject()
          .name("id").value(123)
          .name("name").value("John")
          .name("tags").beginArray()
              .value("java")
              .value("json")
          .endArray()
          .name("address").beginObject()
              .property("city", "Beijing")
              .property("zip", "100000")
          .endObject()
          .endObject();
}
```

---

### 3.19 JsonToken - JSON 令牌类型

`cloud.opencode.base.json.stream.JsonToken`

流式解析器的令牌类型枚举。

#### 枚举值

| 枚举值 | 说明 |
|-------|------|
| `START_OBJECT` | 对象开始 `{` |
| `END_OBJECT` | 对象结束 `}` |
| `START_ARRAY` | 数组开始 `[` |
| `END_ARRAY` | 数组结束 `]` |
| `NAME` | 属性名 |
| `STRING` | 字符串值 |
| `NUMBER` | 数字值 |
| `BOOLEAN` | 布尔值 |
| `NULL` | null 值 |
| `END_DOCUMENT` | 文档结束 |
| `NOT_AVAILABLE` | 不可用 |

#### 方法

| 方法签名 | 说明 |
|---------|------|
| `boolean isStructureStart()` | 是否为结构开始（START_OBJECT/START_ARRAY） |
| `boolean isStructureEnd()` | 是否为结构结束（END_OBJECT/END_ARRAY） |
| `boolean isScalarValue()` | 是否为标量值（STRING/NUMBER/BOOLEAN/NULL） |
| `boolean isValue()` | 是否为任意值（标量或结构开始） |
| `boolean isNumeric()` | 是否为数字 |

---

### 3.20 ReactiveJsonReader - 响应式 JSON 读取器

`cloud.opencode.base.json.reactive.ReactiveJsonReader`

基于 `java.util.concurrent.Flow` 的响应式 JSON 读取接口，支持背压。实现 `Closeable`。

#### 静态方法

| 方法签名 | 说明 |
|---------|------|
| `static ReactiveJsonReader create(InputStream input)` | 创建读取器 |
| `static ReactiveJsonReader create(InputStream input, int bufferSize)` | 创建读取器（自定义缓冲区） |

#### 实例方法

| 方法签名 | 说明 |
|---------|------|
| `<T> Flow.Publisher<T> readValues(Class<T> clazz)` | 读取对象流 |
| `<T> Flow.Publisher<T> readValues(Class<T> clazz, int batchSize)` | 读取对象流（指定批量） |
| `<T> Flow.Publisher<T> readArrayElements(Class<T> elementType)` | 读取数组元素流 |
| `<T> Flow.Publisher<T> readArrayElements(Class<T> elementType, int batchSize)` | 读取数组元素流（指定批量） |
| `boolean isOpen()` | 是否打开 |
| `long getElementsRead()` | 已读取元素数 |

---

### 3.21 ReactiveJsonWriter - 响应式 JSON 写入器

`cloud.opencode.base.json.reactive.ReactiveJsonWriter`

基于 `java.util.concurrent.Flow` 的响应式 JSON 写入接口。实现 `Closeable`。

#### 静态方法

| 方法签名 | 说明 |
|---------|------|
| `static ReactiveJsonWriter create(OutputStream output)` | 创建写入器 |
| `static ReactiveJsonWriter create(OutputStream output, int bufferSize)` | 创建写入器（自定义缓冲区） |
| `static ReactiveJsonWriter createPretty(OutputStream output)` | 创建美化写入器 |

#### 实例方法

| 方法签名 | 说明 |
|---------|------|
| `<T> CompletableFuture<Void> write(Flow.Publisher<T> source)` | 写入对象流（NDJSON） |
| `<T> CompletableFuture<Void> writeAsArray(Flow.Publisher<T> source)` | 写入为 JSON 数组 |
| `<T> CompletableFuture<Void> writeObject(T object)` | 写入单个对象 |
| `void flush()` | 刷新缓冲区 |
| `boolean isOpen()` | 是否打开 |
| `long getElementsWritten()` | 已写入元素数 |
| `long getBytesWritten()` | 已写入字节数 |

#### 代码示例

```java
// 响应式读取
try (ReactiveJsonReader reader = ReactiveJsonReader.create(
        new FileInputStream("large-data.json"))) {
    reader.readArrayElements(User.class)
        .subscribe(new Flow.Subscriber<>() {
            private Flow.Subscription subscription;

            @Override
            public void onSubscribe(Flow.Subscription s) {
                this.subscription = s;
                s.request(10);
            }

            @Override
            public void onNext(User user) {
                Thread.startVirtualThread(() -> processUser(user));
                subscription.request(1);
            }

            @Override
            public void onError(Throwable t) { t.printStackTrace(); }

            @Override
            public void onComplete() { System.out.println("Done"); }
        });
}

// 响应式写入
try (ReactiveJsonWriter writer = ReactiveJsonWriter.create(
        new FileOutputStream("output.json"))) {
    CompletableFuture<Void> future = writer.writeAsArray(userPublisher);
    future.join();
}
```

---

### 3.22 注解

#### 3.22.1 @JsonProperty - 属性映射

`cloud.opencode.base.json.annotation.JsonProperty`

作用于 `FIELD`、`METHOD`、`PARAMETER`。

| 属性 | 类型 | 默认值 | 说明 |
|-----|------|-------|------|
| `value` | `String` | `""` | JSON 属性名 |
| `required` | `boolean` | `false` | 反序列化时是否必需 |
| `defaultValue` | `String` | `""` | 缺失时的默认值（JSON 字面量） |
| `access` | `Access` | `AUTO` | 访问类型（AUTO/READ_ONLY/WRITE_ONLY） |
| `index` | `int` | `-1` | 序列化排序索引 |

#### 3.22.2 @JsonIgnore - 忽略字段

`cloud.opencode.base.json.annotation.JsonIgnore`

作用于 `FIELD`、`METHOD`。

| 属性 | 类型 | 默认值 | 说明 |
|-----|------|-------|------|
| `serialize` | `boolean` | `true` | 是否在序列化时忽略 |
| `deserialize` | `boolean` | `true` | 是否在反序列化时忽略 |

#### 3.22.3 @JsonFormat - 格式化

`cloud.opencode.base.json.annotation.JsonFormat`

作用于 `FIELD`、`METHOD`、`TYPE`。

| 属性 | 类型 | 默认值 | 说明 |
|-----|------|-------|------|
| `pattern` | `String` | `""` | 格式化模式 |
| `shape` | `Shape` | `ANY` | 值形状（ANY/SCALAR/ARRAY/OBJECT/NUMBER/NUMBER_INT/NUMBER_FLOAT/STRING/BOOLEAN） |
| `timezone` | `String` | `""` | 时区 ID |
| `locale` | `String` | `""` | 区域设置 |
| `lenient` | `boolean` | `false` | 宽松解析 |

#### 3.22.4 @JsonNaming - 命名策略

`cloud.opencode.base.json.annotation.JsonNaming`

作用于 `TYPE`。

| 属性 | 类型 | 默认值 | 说明 |
|-----|------|-------|------|
| `value` | `Strategy` | `IDENTITY` | 命名策略 |

#### Strategy 枚举

| 枚举值 | 示例 |
|-------|------|
| `IDENTITY` | userName -> userName |
| `SNAKE_CASE` | userName -> user_name |
| `UPPER_SNAKE_CASE` | userName -> USER_NAME |
| `KEBAB_CASE` | userName -> user-name |
| `PASCAL_CASE` | userName -> UserName |
| `LOWER_CASE` | userName -> username |
| `DOT_CASE` | userName -> user.name |

#### 3.22.5 @JsonMask - 数据脱敏

`cloud.opencode.base.json.annotation.JsonMask`

作用于 `FIELD`。

| 属性 | 类型 | 默认值 | 说明 |
|-----|------|-------|------|
| `type` | `MaskType` | `FULL` | 脱敏类型 |
| `pattern` | `String` | `""` | 自定义正则（CUSTOM 类型用） |
| `maskChar` | `char` | `'*'` | 脱敏字符 |
| `prefixLength` | `int` | `-1` | 可见前缀长度 |
| `suffixLength` | `int` | `-1` | 可见后缀长度 |
| `enabled` | `boolean` | `true` | 是否启用 |

#### MaskType 枚举

| 枚举值 | 效果示例 |
|-------|---------|
| `PASSWORD` | `password123` -> `******` |
| `PHONE` | `13812345678` -> `138****5678` |
| `ID_CARD` | `110101199001011234` -> `110***********1234` |
| `EMAIL` | `test@example.com` -> `t***@example.com` |
| `BANK_CARD` | `6222021234567890123` -> `6222****0123` |
| `NAME` | `张三丰` -> `张**` |
| `ADDRESS` | `北京市朝阳区xxx街道` -> `北京市朝阳区****` |
| `CUSTOM` | 自定义正则脱敏 |
| `FULL` | `anything` -> `******` |

#### 注解代码示例

```java
@JsonNaming(JsonNaming.Strategy.SNAKE_CASE)
public class User {
    @JsonProperty(value = "user_id", required = true, index = 0)
    private Long id;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String username;

    @JsonIgnore
    private String password;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createdAt;

    @JsonMask(type = JsonMask.MaskType.PHONE)
    private String phone;

    @JsonMask(type = JsonMask.MaskType.EMAIL)
    private String email;

    @JsonMask(type = JsonMask.MaskType.ID_CARD)
    private String idCard;
}
```

---

### 3.23 异常

#### 3.23.1 OpenJsonProcessingException - JSON 处理异常

`cloud.opencode.base.json.exception.OpenJsonProcessingException`

JSON 操作的基础运行时异常，继承 `RuntimeException`。

#### ErrorType 枚举

| 枚举值 | 说明 |
|-------|------|
| `PARSE_ERROR` | 解析错误 |
| `SERIALIZATION_ERROR` | 序列化错误 |
| `DESERIALIZATION_ERROR` | 反序列化错误 |
| `TYPE_CONVERSION_ERROR` | 类型转换错误 |
| `PATH_ERROR` | 路径求值错误 |
| `IO_ERROR` | IO 错误 |
| `CONFIG_ERROR` | 配置错误 |
| `UNKNOWN` | 未知错误 |

#### 构造器

| 签名 | 说明 |
|-----|------|
| `OpenJsonProcessingException(String message)` | 基本消息 |
| `OpenJsonProcessingException(String message, Throwable cause)` | 消息 + 原因 |
| `OpenJsonProcessingException(String message, ErrorType errorType)` | 消息 + 类型 |
| `OpenJsonProcessingException(String message, ErrorType errorType, Throwable cause)` | 消息 + 类型 + 原因 |
| `OpenJsonProcessingException(String message, ErrorType errorType, Throwable cause, int line, int column, String source)` | 完整位置信息 |

#### 实例方法

| 方法签名 | 说明 |
|---------|------|
| `ErrorType getErrorType()` | 获取错误类型 |
| `int getLine()` | 获取错误行号 |
| `int getColumn()` | 获取错误列号 |
| `String getSource()` | 获取源标识符 |
| `boolean hasLocation()` | 是否有位置信息 |
| `String getLocationString()` | 获取格式化位置字符串 |

#### 静态工厂方法

| 方法签名 | 说明 |
|---------|------|
| `static OpenJsonProcessingException parseError(String message)` | 创建解析错误 |
| `static OpenJsonProcessingException parseError(String message, int line, int column)` | 创建带位置的解析错误 |
| `static OpenJsonProcessingException serializationError(String message, Throwable cause)` | 创建序列化错误 |
| `static OpenJsonProcessingException deserializationError(String message, Throwable cause)` | 创建反序列化错误 |
| `static OpenJsonProcessingException typeConversionError(String message)` | 创建类型转换错误 |
| `static OpenJsonProcessingException pathError(String message)` | 创建路径错误 |
| `static OpenJsonProcessingException ioError(String message, Throwable cause)` | 创建 IO 错误 |
| `static OpenJsonProcessingException configError(String message)` | 创建配置错误 |

#### 3.23.2 JsonSchemaException - Schema 验证异常

`cloud.opencode.base.json.exception.JsonSchemaException`

继承 `OpenJsonProcessingException`，包含验证错误详情。

#### ValidationError 记录（record）

| 字段 | 类型 | 说明 |
|-----|------|------|
| `path` | `String` | JSON Pointer 路径 |
| `keyword` | `String` | Schema 关键字 |
| `message` | `String` | 错误消息 |
| `expected` | `Object` | 预期值 |
| `actual` | `Object` | 实际值 |

#### 构造器

| 签名 | 说明 |
|-----|------|
| `JsonSchemaException(String message)` | 单消息 |
| `JsonSchemaException(String message, List<ValidationError> errors)` | 消息 + 错误列表 |
| `JsonSchemaException(String message, List<ValidationError> errors, String schemaUri)` | 完整参数 |

#### 实例方法

| 方法签名 | 说明 |
|---------|------|
| `List<ValidationError> getErrors()` | 获取错误列表 |
| `int getErrorCount()` | 获取错误数量 |
| `String getSchemaUri()` | 获取 Schema URI |
| `boolean hasErrors()` | 是否有错误 |
| `String getErrorReport()` | 获取格式化错误报告 |

#### 静态工厂方法

| 方法签名 | 说明 |
|---------|------|
| `static JsonSchemaException typeMismatch(String path, String expected, String actual)` | 类型不匹配 |
| `static JsonSchemaException missingRequired(String path, String property)` | 缺少必需属性 |
| `static JsonSchemaException patternMismatch(String path, String pattern, String value)` | 模式不匹配 |
| `static JsonSchemaException constraintViolation(String path, String constraint, String message)` | 约束违反 |
| `static Builder builder()` | 创建构建器 |

#### Builder 方法

| 方法签名 | 说明 |
|---------|------|
| `Builder schemaUri(String schemaUri)` | 设置 Schema URI |
| `Builder addError(ValidationError error)` | 添加错误 |
| `Builder addError(String path, String keyword, String message)` | 添加错误 |
| `boolean hasErrors()` | 是否有错误 |
| `JsonSchemaException build()` | 构建异常 |
| `void throwIfErrors()` | 有错误则抛出 |
