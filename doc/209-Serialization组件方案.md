# Serialization 组件方案

## 1. 组件概述

`opencode-base-serialization` 提供统一的序列化/反序列化框架，通过 SPI 机制支持多种格式（JSON、XML、JDK、Kryo、Protobuf），并提供压缩装饰器、深拷贝、类型转换等便捷功能。JSON 委托 `opencode-base-json`，XML 委托 `opencode-base-xml`，避免重复实现。

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-serialization</artifactId>
    <version>${version}</version>
</dependency>
```

## 2. 包结构

```
cloud.opencode.base.serialization
├── OpenSerializer.java              # 序列化统一门面
├── Serializer.java                  # 序列化器核心接口
├── SerializerConfig.java            # 序列化配置
├── TypeReference.java               # 泛型类型引用
├── json/                            # JSON 序列化
│   ├── JsonSerializer.java          # JSON 序列化器（委托 opencode-json）
│   └── JsonSerializerProvider.java  # JSON SPI 提供者
├── xml/                             # XML 序列化
│   ├── XmlSerializer.java           # XML 序列化器（委托 opencode-xml）
│   └── XmlSerializerProvider.java   # XML SPI 提供者
├── binary/                          # 二进制序列化
│   ├── JdkSerializer.java           # JDK 原生序列化器
│   ├── JdkSerializerProvider.java   # JDK SPI 提供者
│   ├── KryoSerializer.java          # Kryo 高性能序列化器
│   ├── KryoSerializerProvider.java  # Kryo SPI 提供者
│   ├── ProtobufSerializer.java      # Protobuf 序列化器
│   └── ProtobufSerializerProvider.java # Protobuf SPI 提供者
├── compress/                        # 压缩支持
│   ├── CompressedSerializer.java    # 压缩装饰器
│   └── CompressionAlgorithm.java    # 压缩算法枚举
├── spi/                             # SPI 扩展
│   └── SerializerProvider.java      # 序列化器提供者接口
└── exception/
    └── OpenSerializationException.java  # 序列化异常
```

## 3. 核心 API

### 3.1 Serializer

> 序列化器核心接口，定义所有序列化器的契约。实现类需提供序列化为字节数组、反序列化为对象的能力，以及格式标识和 MIME 类型。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `byte[] serialize(Object obj)` | 将对象序列化为字节数组 |
| `<T> T deserialize(byte[] data, Class<T> type)` | 反序列化为指定类型 |
| `<T> T deserialize(byte[] data, TypeReference<T> typeRef)` | 反序列化为泛型类型 |
| `<T> T deserialize(byte[] data, Type type)` | 反序列化为 Type |
| `String getFormat()` | 获取格式名称（如 "json", "kryo"） |
| `default String getMimeType()` | 获取 MIME 类型（默认 application/octet-stream） |
| `default boolean supports(Class<?> type)` | 是否支持该类型 |
| `default boolean isTextBased()` | 是否为文本格式 |

**示例：**

```java
Serializer serializer = new JsonSerializer();
byte[] data = serializer.serialize(user);
User restored = serializer.deserialize(data, User.class);
List<User> users = serializer.deserialize(data, new TypeReference<List<User>>() {});
```

### 3.2 TypeReference

> 泛型类型引用，解决 Java 泛型擦除问题。通过匿名子类保留完整的泛型信息，支持 List、Set、Map、Collection、Optional 等常用类型的工厂方法。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static <T> TypeReference<T> of(Class<T> clazz)` | 从 Class 创建 |
| `static <T> TypeReference<T> of(Type type)` | 从 Type 创建 |
| `static <T> TypeReference<List<T>> listOf(Class<T> elementType)` | 创建 List 类型引用 |
| `static <T> TypeReference<Set<T>> setOf(Class<T> elementType)` | 创建 Set 类型引用 |
| `static <K, V> TypeReference<Map<K, V>> mapOf(Class<K> keyType, Class<V> valueType)` | 创建 Map 类型引用 |
| `static <T> TypeReference<Collection<T>> collectionOf(Class<T> elementType)` | 创建 Collection 类型引用 |
| `static <T> TypeReference<Optional<T>> optionalOf(Class<T> elementType)` | 创建 Optional 类型引用 |
| `Type getType()` | 获取完整泛型类型 |
| `Class<?> getRawType()` | 获取原始类型 |
| `boolean isParameterized()` | 是否为参数化类型 |
| `Type[] getTypeArguments()` | 获取类型参数 |

**示例：**

```java
// 匿名内部类方式
TypeReference<List<User>> listType = new TypeReference<List<User>>() {};

// 工厂方法
TypeReference<List<String>> stringList = TypeReference.listOf(String.class);
TypeReference<Map<String, Integer>> map = TypeReference.mapOf(String.class, Integer.class);
TypeReference<Set<Long>> longSet = TypeReference.setOf(Long.class);
```

### 3.3 SerializerConfig

> 序列化配置，控制类型信息包含、压缩开关、压缩算法、压缩阈值、未知属性处理等。支持 Builder 模式和 toBuilder() 方法。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static SerializerConfig defaults()` | 默认配置 |
| `static Builder builder()` | 创建 Builder |
| `Builder toBuilder()` | 转为 Builder（用于修改） |
| `boolean isIncludeTypeInfo()` | 是否包含类型信息 |
| `boolean isCompressionEnabled()` | 是否启用压缩 |
| `CompressionAlgorithm getCompressionAlgorithm()` | 获取压缩算法 |
| `int getCompressionThreshold()` | 获取压缩阈值（字节） |
| `boolean isFailOnUnknownProperties()` | 遇到未知属性是否失败 |

**示例：**

```java
SerializerConfig config = SerializerConfig.builder()
    .enableCompression(true)
    .compressionAlgorithm(CompressionAlgorithm.GZIP)
    .compressionThreshold(1024)
    .includeTypeInfo(false)
    .failOnUnknownProperties(false)
    .build();

OpenSerializer.setConfig(config);
```

### 3.4 OpenSerializer

> 序列化统一门面，提供序列化/反序列化、深拷贝、类型转换的静态方法。通过 SPI 自动发现序列化器，默认使用 JSON。deepCopy 优先委托 DeepClone 组件（如果可用），否则降级为序列化方案。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static boolean isDeepCloneAvailable()` | DeepClone 组件是否可用 |
| `static void register(Serializer serializer)` | 注册序列化器 |
| `static void setDefault(String format)` | 按格式名设置默认序列化器 |
| `static void setDefault(Serializer serializer)` | 直接设置默认序列化器 |
| `static void setConfig(SerializerConfig config)` | 设置全局配置 |
| `static SerializerConfig getConfig()` | 获取全局配置 |
| `static Serializer get(String format)` | 获取指定格式的序列化器 |
| `static Serializer getDefault()` | 获取默认序列化器 |
| `static Set<String> getFormats()` | 获取所有已注册格式 |
| `static boolean hasFormat(String format)` | 是否已注册指定格式 |
| `static byte[] serialize(Object obj)` | 序列化为字节数组 |
| `static byte[] serialize(Object obj, String format)` | 指定格式序列化 |
| `static String serializeToString(Object obj)` | 序列化为字符串 |
| `static String serializeToString(Object obj, String format)` | 指定格式序列化为字符串 |
| `static <T> T deserialize(byte[] data, Class<T> type)` | 反序列化 |
| `static <T> T deserialize(byte[] data, Class<T> type, String format)` | 指定格式反序列化 |
| `static <T> T deserialize(String data, Class<T> type)` | 从字符串反序列化 |
| `static <T> T deserialize(String data, Class<T> type, String format)` | 从字符串按格式反序列化 |
| `static <T> T deserialize(byte[] data, TypeReference<T> typeRef)` | 反序列化为泛型类型 |
| `static <T> T deserialize(String data, TypeReference<T> typeRef)` | 从字符串反序列化为泛型 |
| `static <T> T deserialize(byte[] data, TypeReference<T> typeRef, String format)` | 指定格式反序列化为泛型 |
| `static <T> List<T> deserializeList(byte[] data, Class<T> elementType)` | 反序列化为 List |
| `static <T> List<T> deserializeList(String data, Class<T> elementType)` | 从字符串反序列化为 List |
| `static <T> Set<T> deserializeSet(byte[] data, Class<T> elementType)` | 反序列化为 Set |
| `static <K, V> Map<K, V> deserializeMap(byte[] data, Class<K> keyType, Class<V> valueType)` | 反序列化为 Map |
| `static <K, V> Map<K, V> deserializeMap(String data, Class<K> keyType, Class<V> valueType)` | 从字符串反序列化为 Map |
| `static <T> T deepCopy(T obj)` | 深拷贝（优先 DeepClone） |
| `static <T> T deepCopy(T obj, String format)` | 指定格式深拷贝 |
| `static <T> T convert(Object source, Class<T> targetType)` | 类型转换 |
| `static <T> T convert(Object source, TypeReference<T> typeRef)` | 泛型类型转换 |
| `static <T> T convert(Object source, Class<T> targetType, String format)` | 指定格式类型转换 |

**示例：**

```java
// 序列化/反序列化
User user = new User("张三", 25);
byte[] data = OpenSerializer.serialize(user);
User restored = OpenSerializer.deserialize(data, User.class);

// 字符串序列化
String json = OpenSerializer.serializeToString(user);
User fromJson = OpenSerializer.deserialize(json, User.class);

// 泛型反序列化
List<User> users = OpenSerializer.deserialize(data, new TypeReference<List<User>>() {});
List<User> userList = OpenSerializer.deserializeList(data, User.class);
Map<String, User> userMap = OpenSerializer.deserializeMap(data, String.class, User.class);

// 深拷贝
User copy = OpenSerializer.deepCopy(user);

// 类型转换
UserDTO dto = OpenSerializer.convert(user, UserDTO.class);
```

### 3.5 JsonSerializer

> JSON 序列化器，委托 `opencode-base-json` 组件实现。格式名 "json"，MIME 类型 "application/json"，文本格式。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `byte[] serialize(Object obj)` | 序列化为 JSON 字节数组 |
| `<T> T deserialize(byte[] data, Class<T> type)` | 从 JSON 反序列化 |
| `<T> T deserialize(byte[] data, TypeReference<T> typeRef)` | 泛型反序列化 |
| `<T> T deserialize(byte[] data, Type type)` | Type 反序列化 |
| `String getFormat()` | 返回 "json" |
| `String getMimeType()` | 返回 "application/json" |
| `boolean isTextBased()` | 返回 true |

### 3.6 XmlSerializer

> XML 序列化器，委托 `opencode-base-xml` 组件实现。格式名 "xml"，MIME 类型 "application/xml"，文本格式。支持 JAXB 注解。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `byte[] serialize(Object obj)` | 序列化为 XML 字节数组 |
| `<T> T deserialize(byte[] data, Class<T> type)` | 从 XML 反序列化 |
| `String getFormat()` | 返回 "xml" |
| `String getMimeType()` | 返回 "application/xml" |
| `boolean isTextBased()` | 返回 true |
| `String serializeToString(Object obj)` | 序列化为 XML 字符串 |
| `<T> T deserialize(String xml, Class<T> type)` | 从 XML 字符串反序列化 |

### 3.7 JdkSerializer

> JDK 原生序列化器，要求对象实现 `Serializable` 接口。格式名 "jdk"。内置反序列化安全防护（ObjectInputFilter 黑名单过滤）。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `byte[] serialize(Object obj)` | JDK 序列化 |
| `<T> T deserialize(byte[] data, Class<T> type)` | JDK 反序列化（带安全过滤） |
| `String getFormat()` | 返回 "jdk" |
| `String getMimeType()` | 返回 "application/x-java-serialized-object" |
| `boolean supports(Class<?> type)` | 检查是否实现 Serializable |

### 3.8 KryoSerializer

> Kryo 高性能序列化器，使用对象池管理 Kryo 实例保证线程安全。格式名 "kryo"。支持安全模式、类注册优化。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `KryoSerializer()` | 创建默认实例 |
| `KryoSerializer(int poolSize)` | 指定池大小 |
| `KryoSerializer(int poolSize, boolean secureMode)` | 指定池大小和安全模式 |
| `static KryoSerializer secure()` | 创建安全模式实例 |
| `byte[] serialize(Object obj)` | Kryo 序列化 |
| `<T> T deserialize(byte[] data, Class<T> type)` | Kryo 反序列化 |
| `String getFormat()` | 返回 "kryo" |
| `String getMimeType()` | 返回 "application/x-kryo" |
| `KryoSerializer register(Class<?>... classes)` | 注册类（提升性能） |
| `KryoSerializer register(Class<?> clazz, int id)` | 注册类（指定 ID） |
| `KryoSerializer allow(Class<?>... classes)` | 安全模式下允许的类 |
| `boolean isSecureMode()` | 是否为安全模式 |

**示例：**

```java
// 基本用法
KryoSerializer kryo = new KryoSerializer();
byte[] data = kryo.serialize(user);
User restored = kryo.deserialize(data, User.class);

// 注册类提升性能
kryo.register(User.class, Order.class, Product.class);

// 安全模式
KryoSerializer secure = KryoSerializer.secure();
secure.allow(User.class, Order.class);
```

### 3.9 ProtobufSerializer

> Protobuf 序列化器，仅支持 Protobuf Message 类型。格式名 "protobuf"，MIME 类型 "application/x-protobuf"。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `byte[] serialize(Object obj)` | Protobuf 序列化（要求 Message 类型） |
| `<T> T deserialize(byte[] data, Class<T> type)` | Protobuf 反序列化 |
| `String getFormat()` | 返回 "protobuf" |
| `String getMimeType()` | 返回 "application/x-protobuf" |
| `boolean supports(Class<?> type)` | 检查是否为 Message 子类 |

### 3.10 CompressedSerializer

> 压缩序列化装饰器，为任意序列化器添加压缩能力。数据超过阈值才压缩，通过头部字节标识压缩算法，反序列化时自动识别。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `CompressedSerializer(Serializer delegate, CompressionAlgorithm algorithm)` | 创建（默认阈值 1024B） |
| `CompressedSerializer(Serializer delegate, CompressionAlgorithm algorithm, int threshold)` | 创建（指定阈值） |
| `byte[] serialize(Object obj)` | 压缩序列化 |
| `<T> T deserialize(byte[] data, Class<T> type)` | 解压反序列化 |
| `<T> T deserialize(byte[] data, TypeReference<T> typeRef)` | 泛型解压反序列化 |
| `String getFormat()` | 返回 "格式+算法"（如 "json+gzip"） |
| `Serializer getDelegate()` | 获取委托的序列化器 |
| `CompressionAlgorithm getAlgorithm()` | 获取压缩算法 |
| `int getThreshold()` | 获取压缩阈值 |

**示例：**

```java
Serializer json = OpenSerializer.get("json");
CompressedSerializer compressed = new CompressedSerializer(
    json, CompressionAlgorithm.GZIP, 1024);

byte[] data = compressed.serialize(largeObject);
LargeObject restored = compressed.deserialize(data, LargeObject.class);

// 注册为全局可用
OpenSerializer.register(compressed);  // 格式名: "json+gzip"
```

### 3.11 CompressionAlgorithm

> 压缩算法枚举，定义支持的压缩算法及其标识 ID。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `NONE` | 无压缩（ID: 0） |
| `GZIP` | GZIP 压缩（ID: 1，内置） |
| `LZ4` | LZ4 压缩（ID: 2，高速） |
| `SNAPPY` | Snappy 压缩（ID: 3） |
| `ZSTD` | ZSTD 压缩（ID: 4，高压缩比） |
| `String getName()` | 获取算法名称 |
| `byte getId()` | 获取算法 ID |
| `boolean isBuiltIn()` | 是否为内置算法 |
| `boolean isAvailable()` | 运行时是否可用 |
| `static CompressionAlgorithm fromId(byte id)` | 根据 ID 查找 |
| `static CompressionAlgorithm fromName(String name)` | 根据名称查找 |

### 3.12 SerializerProvider

> SPI 接口，用于序列化器的自动发现和注册。实现类需在 `META-INF/services/` 下注册。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `Serializer create()` | 创建序列化器实例 |
| `default int getPriority()` | 获取优先级（数值越小越优先，默认 100） |
| `default boolean isAvailable()` | 运行时是否可用（默认 true） |

**SPI 注册示例：**

```
# META-INF/services/cloud.opencode.base.serialization.spi.SerializerProvider
cloud.opencode.base.serialization.json.JsonSerializerProvider
cloud.opencode.base.serialization.binary.JdkSerializerProvider
cloud.opencode.base.serialization.binary.KryoSerializerProvider
```

### 3.13 OpenSerializationException

> 序列化异常，继承 OpenException。包含格式名和目标类型信息。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `String getFormat()` | 获取格式名 |
| `Class<?> getTargetType()` | 获取目标类型 |
| `static OpenSerializationException serializeFailed(Object obj, Throwable cause)` | 序列化失败 |
| `static OpenSerializationException serializeFailed(Object obj, String format, Throwable cause)` | 指定格式序列化失败 |
| `static OpenSerializationException deserializeFailed(byte[] data, Class<?> type, Throwable cause)` | 反序列化失败 |
| `static OpenSerializationException deserializeFailed(byte[] data, Class<?> type, String format, Throwable cause)` | 指定格式反序列化失败 |
| `static OpenSerializationException serializerNotFound(String format)` | 序列化器未找到 |
| `static OpenSerializationException unsupportedType(Type type, String format)` | 不支持的类型 |
| `static OpenSerializationException compressionFailed(Throwable cause)` | 压缩失败 |
| `static OpenSerializationException compressionFailed(String algorithm, Throwable cause)` | 指定算法压缩失败 |
| `static OpenSerializationException decompressionFailed(Throwable cause)` | 解压失败 |
| `static OpenSerializationException decompressionFailed(String algorithm, Throwable cause)` | 指定算法解压失败 |
| `static OpenSerializationException missingDependency(String format, String dependency)` | 缺少依赖 |
