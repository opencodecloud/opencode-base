# OpenCode Base Json

面向 JDK 25+ 的统一 JSON 处理门面，支持 SPI 可插拔引擎。提供序列化、反序列化、树模型、流式处理、JSONPath、JSON Patch、Schema 校验等统一 API。

## Maven 依赖

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-json</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 功能特性

- 统一的 JSON 操作门面，支持可插拔的 SPI 提供者（Jackson、Gson、Fastjson2 等）
- 序列化/反序列化：对象、数组、Map、泛型类型、流
- 树模型：解析、导航和操作 JSON 为 `JsonNode` 树
- 流式 API：`JsonReader` 和 `JsonWriter` 用于内存高效的处理
- JSONPath 查询，从 JSON 文档中选择节点
- JSON Pointer（RFC 6901）直接访问节点
- JSON Patch（RFC 6902）对 JSON 文档应用操作
- JSON Merge Patch（RFC 7396）合并 JSON 文档
- JSON Schema 校验，含详细错误报告
- 自定义类型适配器，用于特殊序列化
- 响应式 JSON 读写器，用于非阻塞处理
- JSON diff 用于比较文档
- 安全特性：深度限制、大小限制、类型过滤
- 可配置：美化打印、null 处理、日期格式、命名策略
- 注解支持：`@JsonProperty`、`@JsonIgnore`、`@JsonFormat`、`@JsonMask`、`@JsonNaming`

## 类参考

### 核心类

| 类 | 说明 |
|---|------|
| `OpenJson` | 所有 JSON 操作的主门面类：序列化、反序列化、解析、查询、补丁、校验 |
| `JsonNode` | 树模型节点，表示 JSON 值（对象、数组、字符串、数字、布尔、null） |
| `JsonConfig` | JSON 处理配置：美化打印、null 处理、日期格式、特性 |
| `TypeReference<T>` | 泛型类型反序列化的类型令牌（如 `List<User>`） |

### 注解

| 类 | 说明 |
|---|------|
| `@JsonProperty` | 将字段映射到 JSON 属性名 |
| `@JsonIgnore` | 从序列化/反序列化中排除字段 |
| `@JsonFormat` | 指定序列化的日期/时间格式 |
| `@JsonMask` | 在 JSON 输出中掩码敏感字段值 |
| `@JsonNaming` | 指定 JSON 属性名的命名策略 |

### 适配器

| 类 | 说明 |
|---|------|
| `JsonTypeAdapter<T>` | 自定义类型序列化/反序列化接口 |
| `JsonAdapterRegistry` | 自定义类型适配器的管理注册表 |

### Diff

| 类 | 说明 |
|---|------|
| `JsonDiff` | 计算两个 JSON 文档之间的差异 |

### Patch

| 类 | 说明 |
|---|------|
| `JsonPatch` | JSON Patch（RFC 6902）实现：add、remove、replace、move、copy、test 操作 |
| `JsonMergePatch` | JSON Merge Patch（RFC 7396）实现，用于合并 JSON 文档 |

### Path

| 类 | 说明 |
|---|------|
| `JsonPath` | JSONPath 查询引擎，从 JSON 树中选择节点 |
| `JsonPointer` | JSON Pointer（RFC 6901），直接访问嵌套值 |

### Schema

| 类 | 说明 |
|---|------|
| `JsonSchemaValidator` | 根据 JSON Schema 校验 JSON 数据，含详细错误结果 |

### 响应式

| 类 | 说明 |
|---|------|
| `ReactiveJsonReader` | 非阻塞响应式 JSON 读取接口 |
| `ReactiveJsonWriter` | 非阻塞响应式 JSON 写入接口 |
| `DefaultReactiveJsonReader` | 响应式 JSON 读取器的默认实现 |
| `DefaultReactiveJsonWriter` | 响应式 JSON 写入器的默认实现 |

### 安全

| 类 | 说明 |
|---|------|
| `JsonSecurity` | 安全工具：深度限制、大小限制、类型过滤，用于安全的 JSON 处理 |

### SPI

| 类 | 说明 |
|---|------|
| `JsonProvider` | 可插拔 JSON 引擎实现的 SPI 接口 |
| `JsonProviderFactory` | 发现和创建 JSON 提供者实例的工厂 |
| `JsonFeature` | 可配置 JSON 处理特性的枚举 |

### 流式处理

| 类 | 说明 |
|---|------|
| `JsonReader` | 基于拉取的流式 JSON 读取器，内存高效解析 |
| `JsonWriter` | 流式 JSON 写入器，用于生成 JSON 输出 |
| `JsonToken` | 流式 JSON 读取器发出的令牌类型 |

### 内部实现

| 类 | 说明 |
|---|------|
| `BuiltinJsonProvider` | 内置 JSON 提供者实现（零依赖） |
| `BuiltinJsonReader` | 内置流式 JSON 读取器 |
| `BuiltinJsonWriter` | 内置流式 JSON 写入器 |
| `JsonParser` | 内部 JSON 字符串解析器 |
| `JsonSerializer` | 内部 JSON 对象序列化器 |
| `BeanMapper` | 内部 Bean 到 JSON 和 JSON 到 Bean 的映射器 |

### 异常

| 类 | 说明 |
|---|------|
| `OpenJsonProcessingException` | JSON 解析和处理错误时抛出 |
| `JsonSchemaException` | JSON Schema 校验失败时抛出 |

## 快速开始

```java
// 序列化为 JSON
String json = OpenJson.toJson(user);
String pretty = OpenJson.toPrettyJson(user);
byte[] bytes = OpenJson.toJsonBytes(user);

// 从 JSON 反序列化
User user = OpenJson.fromJson(json, User.class);
List<User> users = OpenJson.fromJsonArray(jsonArray, User.class);
Map<String, Object> map = OpenJson.fromJsonMap(json, String.class, Object.class);

// 泛型类型
List<User> users = OpenJson.fromJson(json, new TypeReference<List<User>>() {});

// 树模型
JsonNode node = OpenJson.parse(json);
String name = node.get("name").asText();
JsonNode tree = OpenJson.toTree(user);
User user = OpenJson.treeToValue(node, User.class);

// JSONPath 查询
List<JsonNode> names = OpenJson.select(node, "$.users[*].name");
JsonNode first = OpenJson.selectFirst(node, "$.users[0]");

// JSON Pointer
JsonNode value = OpenJson.at(node, "/users/0/name");

// JSON Diff
JsonDiff.DiffResult diff = OpenJson.diff(source, target);

// JSON Patch
JsonNode patched = OpenJson.patch(document, jsonPatch);
JsonNode merged = OpenJson.mergePatch(target, patchNode);

// Schema 校验
var result = OpenJson.validate(data, schema);
if (!result.isValid()) {
    result.getErrors().forEach(System.out::println);
}

// 流式处理
try (JsonReader reader = OpenJson.createReader(inputStream)) {
    while (reader.hasNext()) {
        JsonToken token = reader.next();
        // 处理令牌
    }
}

// 自定义配置
OpenJson customJson = OpenJson.withConfig(
    JsonConfig.builder().prettyPrint().build()
);
String result = customJson.serialize(obj);
```

## 环境要求

- Java 25+

## 开源协议

Apache License 2.0
