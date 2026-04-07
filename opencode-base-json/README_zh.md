# OpenCode Base Json

面向 JDK 25+ 的统一 JSON 处理门面，支持 SPI 可插拔引擎。提供序列化、反序列化、树模型、流式处理、JSONPath、JSON Patch、Schema 校验等统一 API。

## Maven 依赖

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-json</artifactId>
    <version>1.0.3</version>
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
- 多态类型处理：`@JsonTypeInfo`、`@JsonSubTypes`、`@JsonTypeName`
- 构造函数/工厂反序列化：`@JsonCreator`、`@JsonValue`
- 属性可见性控制：`@JsonAutoDetect`、`@JsonPropertyOrder`、`@JsonInclude`
- 循环引用处理：`@JsonManagedReference`、`@JsonBackReference`、`@JsonIdentityInfo`
- 自定义序列化器/反序列化器：`@JsonSerialize`、`@JsonDeserialize`
- 属性展开与视图过滤：`@JsonUnwrapped`、`@JsonView`
- 动态属性与别名：`@JsonAnySetter`、`@JsonAnyGetter`、`@JsonAlias`
- 原始 JSON 嵌入、根节点包装、过滤器、注入：`@JsonRawValue`、`@JsonRootName`、`@JsonFilter`、`@JsonInject`
- 枚举未知值默认处理：`@JsonEnumDefaultValue`
- 模块系统：可插拔的 `JsonModule`，含 `SimpleModule` 便捷类
- Mixin 注解：`MixinSource` 非侵入式注解覆盖
- 动态属性过滤：`PropertyFilter` 内置多种过滤策略
- 对象标识：`ObjectIdGenerator` 和 `ObjectIdResolver` 解决循环引用
- **[V1.0.3]** JSON 扁平化/反扁平化：嵌套 JSON 与点分隔键值对互转
- **[V1.0.3]** JSON 规范化（RFC 8785 JCS）：确定性输出，用于哈希和数字签名
- **[V1.0.3]** JSON 日志截断：安全截断大型 JSON，支持结构摘要
- **[V1.0.3]** JSON 结构相等比较：深度比较，忽略对象键顺序
- **[V1.0.3]** JSON 字符串工具：转义、反转义、isValid、minify、prettyPrint
- **[V1.0.3]** 异常体系：所有 JSON 异常统一继承 `OpenException`，支持统一 catch

## 类参考

### 核心类

| 类 | 说明 |
|---|------|
| `OpenJson` | 所有 JSON 操作的主门面类：序列化、反序列化、解析、查询、补丁、校验 |
| `JsonNode` | 树模型节点，表示 JSON 值（对象、数组、字符串、数字、布尔、null） |
| `JsonConfig` | JSON 处理配置：美化打印、null 处理、日期格式、特性 |
| `TypeReference<T>` | 泛型类型反序列化的类型令牌（如 `List<User>`） |

### 注解 — 基础

| 类 | 说明 |
|---|------|
| `@JsonProperty` | 将字段映射到 JSON 属性名 |
| `@JsonIgnore` | 从序列化/反序列化中排除字段 |
| `@JsonFormat` | 指定序列化的日期/时间格式 |
| `@JsonMask` | 在 JSON 输出中掩码敏感字段值 |
| `@JsonNaming` | 指定 JSON 属性名的命名策略 |

### 注解 — 多态类型

| 类 | 说明 |
|---|------|
| `@JsonTypeInfo` | 配置多态类型处理（Id 策略 + 包含方式） |
| `@JsonSubTypes` | 声明多态反序列化的已知子类型 |
| `@JsonTypeName` | 指定类的逻辑类型名 |

### 注解 — 构造函数与值

| 类 | 说明 |
|---|------|
| `@JsonCreator` | 标记用于反序列化的构造函数或工厂方法 |
| `@JsonValue` | 标记方法/字段，其返回值作为 JSON 表示 |

### 注解 — 类级元数据

| 类 | 说明 |
|---|------|
| `@JsonAutoDetect` | 控制属性自动发现可见性（字段、getter、setter） |
| `@JsonPropertyOrder` | 控制属性序列化顺序 |
| `@JsonInclude` | 控制属性包含策略（NON_NULL、NON_EMPTY 等） |

### 注解 — 引用处理

| 类 | 说明 |
|---|------|
| `@JsonManagedReference` | 标记双向关系的正向/父端 |
| `@JsonBackReference` | 标记反向/子端（序列化时忽略） |
| `@JsonIdentityInfo` | 基于 ID 的循环引用序列化 |

### 注解 — 高级

| 类 | 说明 |
|---|------|
| `@JsonSerialize` | 指定字段或类的自定义序列化器 |
| `@JsonDeserialize` | 指定字段或类的自定义反序列化器 |
| `@JsonUnwrapped` | 将嵌套对象属性展开到父级 |
| `@JsonView` | 基于视图的属性过滤，选择性序列化 |
| `@JsonAnySetter` | 捕获未知 JSON 属性到 Map |
| `@JsonAnyGetter` | 将 Map 条目序列化为常规 JSON 属性 |
| `@JsonRawValue` | 嵌入预格式化的原始 JSON 字符串 |
| `@JsonRootName` | 指定序列化的根节点包装名 |
| `@JsonAlias` | 定义反序列化时的替代名称 |
| `@JsonFilter` | 指定命名属性过滤器进行动态过滤 |
| `@JsonInject` | 反序列化时注入非 JSON 来源的值 |
| `@JsonEnumDefaultValue` | 标记未知枚举值的默认值 |

### 对象标识

| 类 | 说明 |
|---|------|
| `ObjectIdGenerator<T>` | 对象 ID 生成策略的抽象基类 |
| `ObjectIdGenerators` | 内置生成器：IntSequence、UUID、Property、StringId |
| `ObjectIdResolver` | 对象 ID 到实例的解析接口 |
| `SimpleObjectIdResolver` | 基于 HashMap 的默认 ID 解析器 |

### 适配器

| 类 | 说明 |
|---|------|
| `JsonTypeAdapter<T>` | 自定义类型序列化/反序列化接口 |
| `JsonTypeAdapterFactory` | 动态创建类型适配器的工厂接口 |
| `JsonAdapterRegistry` | 自定义类型适配器的管理注册表 |
| `MixinSource` | 线程安全的 Mixin 注解覆盖注册表 |
| `PropertyFilter` | 动态属性过滤，内置 include/exclude/nonNull 等策略 |

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
| `JsonModule` | 可插拔 JSON 模块接口，含 `SimpleModule` 便捷类 |

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

### 工具类（V1.0.3）

| 类 | 说明 |
|---|------|
| `JsonFlattener` | 嵌套 JSON 扁平化为点分隔键值对，支持反向还原 |
| `JsonCanonicalizer` | RFC 8785 JSON 规范化方案，生成确定性输出 |
| `JsonTruncator` | 大型 JSON 日志截断，支持结构感知的摘要 |
| `JsonEquals` | 结构相等比较，忽略对象键顺序 |
| `JsonStrings` | 字符串级工具：转义、反转义、isValid、minify、prettyPrint |

### 异常

| 类 | 说明 |
|---|------|
| `OpenJsonProcessingException` | JSON 解析和处理错误时抛出（继承 `OpenException`） |
| `JsonSchemaException` | JSON Schema 校验失败时抛出 |

## 工具类 API（V1.0.3）

### JsonFlattener

| 方法 | 说明 |
|------|------|
| `flatten(JsonNode)` | 扁平化为点分隔映射，数组使用方括号表示：`a.b`、`c[0]` |
| `flatten(JsonNode, String separator)` | 使用自定义分隔符扁平化 |
| `flatten(JsonNode, FlattenConfig)` | 完整配置扁平化（分隔符、方括号/点分数组、最大深度） |
| `unflatten(Map<String,JsonNode>)` | 从平面映射还原嵌套树（自动检测数组） |
| `unflatten(Map<String,JsonNode>, String separator)` | 使用自定义分隔符还原 |

### JsonCanonicalizer

| 方法 | 说明 |
|------|------|
| `canonicalize(JsonNode)` | RFC 8785 JCS：键排序、ES6 数字、最小化转义 |
| `canonicalize(String json)` | 解析后规范化（便捷方法） |

### JsonTruncator

| 方法 | 说明 |
|------|------|
| `truncate(String json, int maxLength)` | 快速字符串级截断，附加 `...(truncated)` 标记 |
| `truncate(JsonNode, TruncateConfig)` | 树级截断，生成有效 JSON，支持可配置的限制 |
| `summary(JsonNode)` | 单行摘要：`Object{5 properties}`、`Array[10 elements]`、`String(27 chars)` |

### JsonEquals

| 方法 | 说明 |
|------|------|
| `equals(JsonNode, JsonNode)` | 结构相等：对象忽略键顺序，数字按值比较（`1 == 1.0`） |
| `equals(String, String)` | 解析后比较（便捷方法） |
| `equalsIgnoreArrayOrder(JsonNode, JsonNode)` | 同时忽略对象键顺序和数组元素顺序 |

### JsonStrings

| 方法 | 说明 |
|------|------|
| `escape(String)` | RFC 8259 转义：`"` `\` 控制字符。无需转义时零分配直接返回原字符串 |
| `unescape(String)` | 反转义：处理 `\uXXXX`、代理对。无 `\` 时快速路径 |
| `isValid(String)` | 状态机验证 — 不构建树，比 parse 快 3 倍以上 |
| `minify(String)` | 去除字符串外空白，单遍处理 |
| `prettyPrint(String)` | 默认 2 空格缩进格式化 |
| `prettyPrint(String, String indent)` | 自定义缩进字符串格式化 |

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

// 多态类型处理
@JsonTypeInfo(id = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Dog.class, name = "dog"),
    @JsonSubTypes.Type(value = Cat.class, name = "cat")
})
public abstract class Animal { }

// 构造函数反序列化
public class Point {
    @JsonCreator
    public Point(@JsonProperty("x") int x, @JsonProperty("y") int y) { }
}

// 视图过滤
public interface Summary {}
public interface Detail extends Summary {}

public class User {
    @JsonView(Summary.class)
    private String name;

    @JsonView(Detail.class)
    private String email;
}

// 模块系统
JsonModule module = new JsonModule.SimpleModule("myModule", "1.0") {{
    addAdapter(myTypeAdapter);
    addMixin(TargetClass.class, MixinClass.class);
}};
OpenJson json = OpenJson.withConfig(config).registerModule(module);

// 属性过滤器
PropertyFilter filter = PropertyFilter.exclude("password", "secret");
json.setPropertyFilter("securityFilter", filter);

// Mixin（非侵入式注解）
json.addMixin(ThirdPartyClass.class, MyMixin.class);

// --- V1.0.3 工具方法 ---

// 快速 JSON 验证
boolean valid = OpenJson.isValid("{\"key\":\"value\"}"); // true
boolean invalid = OpenJson.isValid("{bad json}");         // false

// 压缩 / 美化
String minified = OpenJson.minify("{ \"a\" : 1 }");     // {"a":1}
String pretty = OpenJson.prettyPrint("{\"a\":1}");

// 结构相等（忽略键顺序）
boolean eq = OpenJson.structuralEquals(nodeA, nodeB);

// 扁平化 / 反扁平化
Map<String, JsonNode> flat = OpenJson.flatten(nestedNode);
// {"a.b": 1, "c[0]": 2, "c[1]": 3}
JsonNode restored = OpenJson.unflatten(flat);

// RFC 8785 规范化
String canonical = OpenJson.canonicalize(node);

// 日志截断
String truncated = OpenJson.truncate(hugeJsonString, 200);

// 字符串工具
String escaped = JsonStrings.escape("hello\nworld");
String unescaped = JsonStrings.unescape("hello\\nworld");
```

## 环境要求

- Java 25+

## 开源协议

Apache License 2.0
