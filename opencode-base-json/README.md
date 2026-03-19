# OpenCode Base Json

Unified JSON processing facade with SPI mechanism for JDK 25+. Provides a single API for serialization, deserialization, tree model, streaming, JSONPath, JSON Patch, schema validation, and more.

## Maven Dependency

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-json</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Features

- Unified facade for JSON operations with pluggable SPI providers (Jackson, Gson, Fastjson2, etc.)
- Serialization/deserialization: objects, arrays, maps, generic types, streams
- Tree model: parse, navigate, and manipulate JSON as `JsonNode` trees
- Streaming API: `JsonReader` and `JsonWriter` for memory-efficient processing
- JSONPath queries for selecting nodes from JSON documents
- JSON Pointer (RFC 6901) for direct node access
- JSON Patch (RFC 6902) for applying operations to JSON documents
- JSON Merge Patch (RFC 7396) for merging JSON documents
- JSON Schema validation with detailed error reporting
- Custom type adapters for specialized serialization
- Reactive JSON reader/writer for non-blocking processing
- JSON diff for comparing documents
- Security features: depth limits, size limits, type filtering
- Configurable: pretty printing, null handling, date formats, naming strategies
- Annotation support: `@JsonProperty`, `@JsonIgnore`, `@JsonFormat`, `@JsonMask`, `@JsonNaming`

## Class Reference

### Core

| Class | Description |
|-------|-------------|
| `OpenJson` | Main facade class for all JSON operations: serialize, deserialize, parse, query, patch, validate |
| `JsonNode` | Tree model node representing a JSON value (object, array, string, number, boolean, null) |
| `JsonConfig` | Configuration for JSON processing: pretty print, null handling, date format, features |
| `TypeReference<T>` | Type token for generic type deserialization (e.g., `List<User>`) |

### Annotations

| Class | Description |
|-------|-------------|
| `@JsonProperty` | Maps a field to a JSON property name |
| `@JsonIgnore` | Excludes a field from serialization/deserialization |
| `@JsonFormat` | Specifies date/time format for serialization |
| `@JsonMask` | Masks sensitive field values in JSON output |
| `@JsonNaming` | Specifies naming strategy for JSON property names |

### Adapter

| Class | Description |
|-------|-------------|
| `JsonTypeAdapter<T>` | Interface for custom type serialization/deserialization |
| `JsonAdapterRegistry` | Registry for managing custom type adapters |

### Diff

| Class | Description |
|-------|-------------|
| `JsonDiff` | Computes differences between two JSON documents |

### Patch

| Class | Description |
|-------|-------------|
| `JsonPatch` | JSON Patch (RFC 6902) implementation for add, remove, replace, move, copy, test operations |
| `JsonMergePatch` | JSON Merge Patch (RFC 7396) implementation for merging JSON documents |

### Path

| Class | Description |
|-------|-------------|
| `JsonPath` | JSONPath query engine for selecting nodes from JSON trees |
| `JsonPointer` | JSON Pointer (RFC 6901) for direct access to nested values |

### Schema

| Class | Description |
|-------|-------------|
| `JsonSchemaValidator` | Validates JSON data against JSON Schema with detailed error results |

### Reactive

| Class | Description |
|-------|-------------|
| `ReactiveJsonReader` | Interface for non-blocking reactive JSON reading |
| `ReactiveJsonWriter` | Interface for non-blocking reactive JSON writing |
| `DefaultReactiveJsonReader` | Default implementation of reactive JSON reader |
| `DefaultReactiveJsonWriter` | Default implementation of reactive JSON writer |

### Security

| Class | Description |
|-------|-------------|
| `JsonSecurity` | Security utilities: depth limits, size limits, type filtering for safe JSON processing |

### SPI

| Class | Description |
|-------|-------------|
| `JsonProvider` | SPI interface for pluggable JSON engine implementations |
| `JsonProviderFactory` | Factory for discovering and creating JSON provider instances |
| `JsonFeature` | Enum of configurable JSON processing features |

### Streaming

| Class | Description |
|-------|-------------|
| `JsonReader` | Pull-based streaming JSON reader for memory-efficient parsing |
| `JsonWriter` | Streaming JSON writer for generating JSON output |
| `JsonToken` | Token types emitted by the streaming JSON reader |

### Internal

| Class | Description |
|-------|-------------|
| `BuiltinJsonProvider` | Built-in JSON provider implementation (zero dependencies) |
| `BuiltinJsonReader` | Built-in streaming JSON reader |
| `BuiltinJsonWriter` | Built-in streaming JSON writer |
| `JsonParser` | Internal JSON string parser |
| `JsonSerializer` | Internal JSON object serializer |
| `BeanMapper` | Internal bean-to-JSON and JSON-to-bean mapper |

### Exceptions

| Class | Description |
|-------|-------------|
| `OpenJsonProcessingException` | Thrown for JSON parsing and processing errors |
| `JsonSchemaException` | Thrown when JSON schema validation fails |

## Quick Start

```java
// Serialize to JSON
String json = OpenJson.toJson(user);
String pretty = OpenJson.toPrettyJson(user);
byte[] bytes = OpenJson.toJsonBytes(user);

// Deserialize from JSON
User user = OpenJson.fromJson(json, User.class);
List<User> users = OpenJson.fromJsonArray(jsonArray, User.class);
Map<String, Object> map = OpenJson.fromJsonMap(json, String.class, Object.class);

// Generic types
List<User> users = OpenJson.fromJson(json, new TypeReference<List<User>>() {});

// Tree model
JsonNode node = OpenJson.parse(json);
String name = node.get("name").asText();
JsonNode tree = OpenJson.toTree(user);
User user = OpenJson.treeToValue(node, User.class);

// JSONPath query
List<JsonNode> names = OpenJson.select(node, "$.users[*].name");
JsonNode first = OpenJson.selectFirst(node, "$.users[0]");

// JSON Pointer
JsonNode value = OpenJson.at(node, "/users/0/name");

// JSON Diff
JsonDiff.DiffResult diff = OpenJson.diff(source, target);

// JSON Patch
JsonNode patched = OpenJson.patch(document, jsonPatch);
JsonNode merged = OpenJson.mergePatch(target, patchNode);

// Schema validation
var result = OpenJson.validate(data, schema);
if (!result.isValid()) {
    result.getErrors().forEach(System.out::println);
}

// Streaming
try (JsonReader reader = OpenJson.createReader(inputStream)) {
    while (reader.hasNext()) {
        JsonToken token = reader.next();
        // process tokens
    }
}

// Custom configuration
OpenJson customJson = OpenJson.withConfig(
    JsonConfig.builder().prettyPrint().build()
);
String result = customJson.serialize(obj);
```

## Requirements

- Java 25+

## License

Apache License 2.0
