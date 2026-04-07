# OpenCode Base Serialization

**Unified serialization facade with SPI mechanism for Java 25+**

`opencode-base-serialization` provides a unified API for serializing and deserializing objects across multiple formats (JSON, XML, Kryo, Protobuf, JDK) with SPI-based provider discovery, compression support, deep copy, and type conversion.

## Features

### Core Features
- **Unified API**: Single entry point (`OpenSerializer`) for all serialization formats
- **SPI Discovery**: Automatic serializer discovery via `ServiceLoader`
- **Multiple Formats**: JSON, XML, Kryo, Protobuf, and JDK serialization
- **TypeReference**: Preserve generic type information for deserialization
- **Convenience Methods**: `deserializeList`, `deserializeSet`, `deserializeMap`

### Advanced Features
- **Deep Copy**: Object deep copy via serialization or OpenClone delegation
- **Type Conversion**: Convert between types via serialize/deserialize round-trip
- **Compression**: GZIP, LZ4, Snappy, and ZSTD compression algorithms
- **Compressed Serializer**: Transparent compression wrapper around any serializer
- **String Serialization**: Direct to/from String for text-based formats

### V1.0.3 New Features
- **Streaming API**: `serialize(Object, OutputStream)` / `deserialize(InputStream, Class)` on `Serializer` interface
- **ClassFilter**: Deserialization class filtering with allow/deny lists, package rules, and regex patterns
- **SerializationResult**: Metadata-rich result wrapper with timing, format, and compression info; includes zero-copy `dataUnsafe()` method
- **FormatDetector**: Auto-detect serialization format (JSON/XML/binary) from byte patterns
- **SerializerInfo**: Serializer capability introspection record
- **DefaultClassFilter**: Pre-built security filters (`secure()` and `strict()`)
- **Global ClassFilter Enforcement**: `SerializerConfig.classFilter` is enforced by both JdkSerializer and KryoSerializer
- **Decompression Safety**: All compression algorithms (GZIP/LZ4/Snappy/ZSTD) enforce a 256MB decompressed size limit to prevent decompression bomb attacks

### Supported Formats
| Format | Serializer | Text-Based | Optional Dependency |
|--------|-----------|------------|---------------------|
| JDK | `JdkSerializer` | No | None (built-in) |
| JSON | `JsonSerializer` | Yes | `opencode-base-json` |
| XML | `XmlSerializer` | Yes | Jakarta XML Bind + JAXB Runtime |
| Kryo | `KryoSerializer` | No | `com.esotericsoftware:kryo` |
| Protobuf | `ProtobufSerializer` | No | `com.google.protobuf:protobuf-java` |

## Quick Start

### Maven Dependency
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-serialization</artifactId>
    <version>1.0.3</version>
</dependency>
```

### Basic Serialization
```java
import cloud.opencode.base.serialization.OpenSerializer;

// Serialize / Deserialize with default serializer
byte[] data = OpenSerializer.serialize(user);
User restored = OpenSerializer.deserialize(data, User.class);

// Specify format
byte[] json = OpenSerializer.serialize(user, "json");
User fromJson = OpenSerializer.deserialize(json, User.class, "json");

// String serialization
String jsonStr = OpenSerializer.serializeToString(user);
User fromStr = OpenSerializer.deserialize(jsonStr, User.class);
```

### Generic Types
```java
import cloud.opencode.base.serialization.TypeReference;

// TypeReference for generic types
List<User> users = OpenSerializer.deserialize(data, new TypeReference<List<User>>() {});

// Convenience methods
List<User> users = OpenSerializer.deserializeList(data, User.class);
Set<String> tags = OpenSerializer.deserializeSet(data, String.class);
Map<String, Integer> scores = OpenSerializer.deserializeMap(data, String.class, Integer.class);
```

### Deep Copy and Type Conversion
```java
// Deep copy
User copy = OpenSerializer.deepCopy(user);

// Type conversion
UserDTO dto = OpenSerializer.convert(user, UserDTO.class);
```

### Custom Serializer Registration
```java
// Register a custom serializer
OpenSerializer.register(myCustomSerializer);

// Set default format
OpenSerializer.setDefault("json");

// Check available formats
Set<String> formats = OpenSerializer.getFormats();
boolean hasKryo = OpenSerializer.hasFormat("kryo");
```

### Compressed Serialization
```java
import cloud.opencode.base.serialization.compress.*;

// Wrap any serializer with compression
CompressedSerializer compressed = new CompressedSerializer(
    OpenSerializer.get("json"),
    CompressionAlgorithm.GZIP
);

byte[] compressedData = compressed.serialize(largeObject);
```

### Streaming API (V1.0.3)
```java
// Serialize to OutputStream
try (var out = new FileOutputStream("data.bin")) {
    OpenSerializer.serialize(user, out);
}

// Deserialize from InputStream
try (var in = new FileInputStream("data.bin")) {
    User restored = OpenSerializer.deserialize(in, User.class);
}
```

### Format Detection (V1.0.3)
```java
import cloud.opencode.base.serialization.FormatDetector;

// Auto-detect format from data
String format = FormatDetector.detect(data);  // "json", "xml", "binary", "unknown"
boolean isJson = FormatDetector.isJson(data);
boolean isXml = FormatDetector.isXml(data);

// Also via OpenSerializer facade
String detected = OpenSerializer.detect(data);
```

### Class Filtering (V1.0.3)
```java
import cloud.opencode.base.serialization.filter.*;

// Use pre-built security filter
ClassFilter secure = DefaultClassFilter.secure();

// Use strict mode (allowlist only)
ClassFilter strict = DefaultClassFilter.strict();

// Custom filter
ClassFilter custom = new ClassFilterBuilder()
    .allowPackage("com.myapp.model")
    .denyPackage("javax.naming", "java.rmi")
    .deny("java.lang.Runtime", "java.lang.ProcessBuilder")
    .defaultDeny()
    .build();

// Apply globally (enforced by JdkSerializer and KryoSerializer)
OpenSerializer.setConfig(SerializerConfig.builder()
    .classFilter(secure)
    .build());
```

### Serialization Result (V1.0.3)
```java
// Serialize with metadata
SerializationResult result = OpenSerializer.serializeWithResult(user);
byte[] data = result.data();
String format = result.format();
long nanos = result.durationNanos();
int size = result.size();

// Auto-timed serialization
SerializationResult timed = SerializationResult.timed(
    () -> OpenSerializer.serialize(user), "json"
);
```

### Serializer Introspection (V1.0.3)
```java
// List all registered serializers with capabilities
List<SerializerInfo> infos = OpenSerializer.listSerializers();
for (SerializerInfo info : infos) {
    System.out.println(info.format() + " - " + info.description());
}
```

## Class Reference

### Root Package (`cloud.opencode.base.serialization`)
| Class | Description |
|-------|-------------|
| `OpenSerializer` | Main facade for all serialization operations (serialize, deserialize, deep copy, convert, stream, detect) |
| `Serializer` | Core interface for all serializers (serialize, deserialize, stream, format, MIME type) |
| `SerializerConfig` | Global serialization configuration (compression, class filter) |
| `TypeReference<T>` | Captures generic type information for deserialization |
| `SerializationResult` | Serialization result with metadata (size, format, timing) |
| `FormatDetector` | Auto-detect data format from byte patterns |
| `SerializerInfo` | Serializer capability description record |

### Binary (`cloud.opencode.base.serialization.binary`)
| Class | Description |
|-------|-------------|
| `JdkSerializer` | Standard JDK ObjectInputStream/ObjectOutputStream serializer |
| `JdkSerializerProvider` | SPI provider for JdkSerializer |
| `KryoSerializer` | High-performance Kryo-based binary serializer |
| `KryoSerializerProvider` | SPI provider for KryoSerializer |
| `ProtobufSerializer` | Google Protocol Buffers serializer |
| `ProtobufSerializerProvider` | SPI provider for ProtobufSerializer |

### JSON (`cloud.opencode.base.serialization.json`)
| Class | Description |
|-------|-------------|
| `JsonSerializer` | JSON format serializer using opencode-base-json |
| `JsonSerializerProvider` | SPI provider for JsonSerializer |

### XML (`cloud.opencode.base.serialization.xml`)
| Class | Description |
|-------|-------------|
| `XmlSerializer` | XML format serializer using JAXB |
| `XmlSerializerProvider` | SPI provider for XmlSerializer |

### Compress (`cloud.opencode.base.serialization.compress`)
| Class | Description |
|-------|-------------|
| `CompressedSerializer` | Decorator that adds compression to any serializer |
| `CompressionAlgorithm` | Enum of compression algorithms (GZIP, LZ4, SNAPPY, ZSTD) |

### SPI (`cloud.opencode.base.serialization.spi`)
| Class | Description |
|-------|-------------|
| `SerializerProvider` | SPI interface for registering serializers via ServiceLoader |

### Filter (`cloud.opencode.base.serialization.filter`)
| Class | Description |
|-------|-------------|
| `ClassFilter` | Functional interface for deserialization class filtering |
| `ClassFilterBuilder` | Builder for constructing class filters with allow/deny rules |
| `DefaultClassFilter` | Pre-built security filters (`secure()`, `strict()`) |

### Exception (`cloud.opencode.base.serialization.exception`)
| Class | Description |
|-------|-------------|
| `OpenSerializationException` | Runtime exception for serialization/deserialization errors |

## Requirements

- Java 25+
- Required: `opencode-base-json` (for JSON support)

## Optional Dependencies

- `jakarta.xml.bind:jakarta.xml.bind-api` + `org.glassfish.jaxb:jaxb-runtime` -- XML serialization
- `com.esotericsoftware:kryo` -- Kryo binary serialization
- `com.google.protobuf:protobuf-java` -- Protocol Buffers serialization
- `org.lz4:lz4-java` -- LZ4 compression
- `org.xerial.snappy:snappy-java` -- Snappy compression
- `com.github.luben:zstd-jni` -- ZSTD compression
- `opencode-base-deepclone` -- Optimized deep copy

## License

Apache License 2.0

## Author

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
