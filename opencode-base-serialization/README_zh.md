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
    <version>1.0.0</version>
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

## 类参考

### 根包 (`cloud.opencode.base.serialization`)
| 类 | 说明 |
|----|------|
| `OpenSerializer` | 所有序列化操作的主门面（序列化、反序列化、深拷贝、转换） |
| `Serializer` | 所有序列化器的核心接口（序列化、反序列化、格式、MIME 类型） |
| `SerializerConfig` | 全局序列化配置 |
| `TypeReference<T>` | 捕获泛型类型信息用于反序列化 |

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
