# OpenCode Base Serialization

**统一序列化门面，支持 SPI 可插拔机制，适用于 Java 25+**

`opencode-base-serialization` 提供统一的 API，跨多种格式（JSON、XML、Kryo、Protobuf、JDK）进行对象序列化和反序列化，支持基于 SPI 的提供者发现、压缩支持、深拷贝和类型转换。

## 功能特性

### 核心功能
- **统一 API**：所有序列化格式的单一入口点（`OpenSerializer`）
- **SPI 发现**：通过 `ServiceLoader` 自动发现序列化器
- **多种格式**：JSON、XML、Kryo、Protobuf 和 JDK 序列化
- **TypeReference**：反序列化时保留泛型类型信息
- **便捷方法**：`deserializeList`、`deserializeSet`、`deserializeMap`

### 高级功能
- **深拷贝**：通过序列化或 OpenClone 委托实现对象深拷贝
- **类型转换**：通过序列化/反序列化往返实现类型转换
- **压缩**：GZIP、LZ4、Snappy 和 ZSTD 压缩算法
- **压缩序列化器**：在任意序列化器上透明包装压缩
- **字符串序列化**：基于文本格式的直接字符串转换

### V1.0.3 新增功能
- **流式 API**：`Serializer` 接口新增 `serialize(Object, OutputStream)` / `deserialize(InputStream, Class)` 默认方法
- **ClassFilter**：反序列化类过滤器，支持白名单/黑名单、包规则、正则匹配
- **SerializationResult**：带元数据的序列化结果包装（计时、格式、压缩信息），含零拷贝 `dataUnsafe()` 方法
- **FormatDetector**：自动检测数据格式（JSON/XML/二进制）
- **SerializerInfo**：序列化器能力描述 record
- **DefaultClassFilter**：预置安全过滤器（`secure()` 和 `strict()`）
- **全局 ClassFilter 应用**：`SerializerConfig.classFilter` 在 JdkSerializer 和 KryoSerializer 中均生效
- **解压安全**：所有压缩算法（GZIP/LZ4/Snappy/ZSTD）均有解压大小限制（256MB），防止解压炸弹攻击

### 支持的格式
| 格式 | 序列化器 | 基于文本 | 可选依赖 |
|------|---------|---------|---------|
| JDK | `JdkSerializer` | 否 | 无（内置） |
| JSON | `JsonSerializer` | 是 | `opencode-base-json` |
| XML | `XmlSerializer` | 是 | Jakarta XML Bind + JAXB Runtime |
| Kryo | `KryoSerializer` | 否 | `com.esotericsoftware:kryo` |
| Protobuf | `ProtobufSerializer` | 否 | `com.google.protobuf:protobuf-java` |

## 快速开始

### Maven 依赖
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-serialization</artifactId>
    <version>1.0.3</version>
</dependency>
```

### 基本序列化
```java
import cloud.opencode.base.serialization.OpenSerializer;

// 使用默认序列化器序列化/反序列化
byte[] data = OpenSerializer.serialize(user);
User restored = OpenSerializer.deserialize(data, User.class);

// 指定格式
byte[] json = OpenSerializer.serialize(user, "json");
User fromJson = OpenSerializer.deserialize(json, User.class, "json");

// 字符串序列化
String jsonStr = OpenSerializer.serializeToString(user);
User fromStr = OpenSerializer.deserialize(jsonStr, User.class);
```

### 泛型类型
```java
import cloud.opencode.base.serialization.TypeReference;

// 泛型类型的 TypeReference
List<User> users = OpenSerializer.deserialize(data, new TypeReference<List<User>>() {});

// 便捷方法
List<User> users = OpenSerializer.deserializeList(data, User.class);
Set<String> tags = OpenSerializer.deserializeSet(data, String.class);
Map<String, Integer> scores = OpenSerializer.deserializeMap(data, String.class, Integer.class);
```

### 深拷贝和类型转换
```java
// 深拷贝
User copy = OpenSerializer.deepCopy(user);

// 类型转换
UserDTO dto = OpenSerializer.convert(user, UserDTO.class);
```

### 自定义序列化器注册
```java
// 注册自定义序列化器
OpenSerializer.register(myCustomSerializer);

// 设置默认格式
OpenSerializer.setDefault("json");

// 检查可用格式
Set<String> formats = OpenSerializer.getFormats();
boolean hasKryo = OpenSerializer.hasFormat("kryo");
```

### 压缩序列化
```java
import cloud.opencode.base.serialization.compress.*;

// 用压缩包装任意序列化器
CompressedSerializer compressed = new CompressedSerializer(
    OpenSerializer.get("json"),
    CompressionAlgorithm.GZIP
);

byte[] compressedData = compressed.serialize(largeObject);
```

### 流式 API (V1.0.3)
```java
// 序列化到 OutputStream
try (var out = new FileOutputStream("data.bin")) {
    OpenSerializer.serialize(user, out);
}

// 从 InputStream 反序列化
try (var in = new FileInputStream("data.bin")) {
    User restored = OpenSerializer.deserialize(in, User.class);
}
```

### 格式检测 (V1.0.3)
```java
import cloud.opencode.base.serialization.FormatDetector;

// 自动检测数据格式
String format = FormatDetector.detect(data);  // "json", "xml", "binary", "unknown"
boolean isJson = FormatDetector.isJson(data);

// 通过 OpenSerializer 门面
String detected = OpenSerializer.detect(data);
```

### 类过滤器 (V1.0.3)
```java
import cloud.opencode.base.serialization.filter.*;

// 使用预置安全过滤器
ClassFilter secure = DefaultClassFilter.secure();

// 严格模式（仅白名单）
ClassFilter strict = DefaultClassFilter.strict();

// 自定义过滤器
ClassFilter custom = new ClassFilterBuilder()
    .allowPackage("com.myapp.model")
    .denyPackage("javax.naming", "java.rmi")
    .defaultDeny()
    .build();

// 通过 SerializerConfig 全局应用（对 JdkSerializer 和 KryoSerializer 均生效）
OpenSerializer.setConfig(SerializerConfig.builder()
    .classFilter(DefaultClassFilter.strict())
    .build());
```

### 序列化结果 (V1.0.3)
```java
// 带元数据的序列化
SerializationResult result = OpenSerializer.serializeWithResult(user);
byte[] data = result.data();
long nanos = result.durationNanos();
int size = result.size();
```

## 类参考

### 根包 (`cloud.opencode.base.serialization`)
| 类 | 说明 |
|----|------|
| `OpenSerializer` | 所有序列化操作的主门面（序列化、反序列化、流式、深拷贝、转换、检测） |
| `Serializer` | 所有序列化器的核心接口（序列化、反序列化、流式、格式、MIME 类型） |
| `SerializerConfig` | 全局序列化配置（压缩、类过滤器） |
| `TypeReference<T>` | 捕获泛型类型信息用于反序列化 |
| `SerializationResult` | 带元数据的序列化结果（大小、格式、计时） |
| `FormatDetector` | 从字节模式自动检测数据格式 |
| `SerializerInfo` | 序列化器能力描述 record |

### 二进制 (`cloud.opencode.base.serialization.binary`)
| 类 | 说明 |
|----|------|
| `JdkSerializer` | 标准 JDK ObjectInputStream/ObjectOutputStream 序列化器 |
| `JdkSerializerProvider` | JdkSerializer 的 SPI 提供者 |
| `KryoSerializer` | 基于 Kryo 的高性能二进制序列化器 |
| `KryoSerializerProvider` | KryoSerializer 的 SPI 提供者 |
| `ProtobufSerializer` | Google Protocol Buffers 序列化器 |
| `ProtobufSerializerProvider` | ProtobufSerializer 的 SPI 提供者 |

### JSON (`cloud.opencode.base.serialization.json`)
| 类 | 说明 |
|----|------|
| `JsonSerializer` | 使用 opencode-base-json 的 JSON 格式序列化器 |
| `JsonSerializerProvider` | JsonSerializer 的 SPI 提供者 |

### XML (`cloud.opencode.base.serialization.xml`)
| 类 | 说明 |
|----|------|
| `XmlSerializer` | 使用 JAXB 的 XML 格式序列化器 |
| `XmlSerializerProvider` | XmlSerializer 的 SPI 提供者 |

### 压缩 (`cloud.opencode.base.serialization.compress`)
| 类 | 说明 |
|----|------|
| `CompressedSerializer` | 为任意序列化器添加压缩的装饰器 |
| `CompressionAlgorithm` | 压缩算法枚举（GZIP、LZ4、SNAPPY、ZSTD） |

### SPI (`cloud.opencode.base.serialization.spi`)
| 类 | 说明 |
|----|------|
| `SerializerProvider` | 通过 ServiceLoader 注册序列化器的 SPI 接口 |

### 过滤器 (`cloud.opencode.base.serialization.filter`)
| 类 | 说明 |
|----|------|
| `ClassFilter` | 反序列化类过滤函数式接口 |
| `ClassFilterBuilder` | 类过滤器构建器（白名单/黑名单规则） |
| `DefaultClassFilter` | 预置安全过滤器（`secure()`, `strict()`） |

### 异常 (`cloud.opencode.base.serialization.exception`)
| 类 | 说明 |
|----|------|
| `OpenSerializationException` | 序列化/反序列化错误的运行时异常 |

## 环境要求

- Java 25+
- 必需：`opencode-base-json`（用于 JSON 支持）

## 可选依赖

- `jakarta.xml.bind:jakarta.xml.bind-api` + `org.glassfish.jaxb:jaxb-runtime` -- XML 序列化
- `com.esotericsoftware:kryo` -- Kryo 二进制序列化
- `com.google.protobuf:protobuf-java` -- Protocol Buffers 序列化
- `org.lz4:lz4-java` -- LZ4 压缩
- `org.xerial.snappy:snappy-java` -- Snappy 压缩
- `com.github.luben:zstd-jni` -- ZSTD 压缩
- `opencode-base-deepclone` -- 优化的深拷贝

## 许可证

Apache License 2.0

## 作者

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
