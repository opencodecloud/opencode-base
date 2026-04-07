# OpenCode Base Json

Unified JSON processing facade with SPI mechanism for JDK 25+. Provides a single API for serialization, deserialization, tree model, streaming, JSONPath, JSON Patch, schema validation, and more.

## Maven Dependency

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-json</artifactId>
    <version>1.0.3</version>
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
- Polymorphic type handling: `@JsonTypeInfo`, `@JsonSubTypes`, `@JsonTypeName`
- Constructor/factory deserialization: `@JsonCreator`, `@JsonValue`
- Property visibility control: `@JsonAutoDetect`, `@JsonPropertyOrder`, `@JsonInclude`
- Circular reference handling: `@JsonManagedReference`, `@JsonBackReference`, `@JsonIdentityInfo`
- Custom serializer/deserializer: `@JsonSerialize`, `@JsonDeserialize`
- Property unwrapping and view-based filtering: `@JsonUnwrapped`, `@JsonView`
- Dynamic properties and aliases: `@JsonAnySetter`, `@JsonAnyGetter`, `@JsonAlias`
- Raw JSON embedding, root wrapping, filters, injection: `@JsonRawValue`, `@JsonRootName`, `@JsonFilter`, `@JsonInject`
- Enum default value for unknown entries: `@JsonEnumDefaultValue`
- Module system: pluggable `JsonModule` with `SimpleModule` for extensibility
- Mixin annotations: `MixinSource` for non-invasive annotation overlay
- Dynamic property filtering: `PropertyFilter` with built-in strategies
- Object identity: `ObjectIdGenerator` and `ObjectIdResolver` for circular reference resolution
- **[V1.0.3]** JSON flatten/unflatten: convert nested JSON to dot-notation key-value maps and back
- **[V1.0.3]** JSON canonicalization (RFC 8785 JCS): deterministic output for hashing and digital signatures
- **[V1.0.3]** JSON truncation for logging: truncate large JSON safely with structure summary
- **[V1.0.3]** JSON structural equality: deep comparison ignoring object key order
- **[V1.0.3]** JSON string utilities: escape, unescape, isValid, minify, prettyPrint
- **[V1.0.3]** Exception hierarchy: all JSON exceptions now extend `OpenException` for unified catch

## Class Reference

### Core

| Class | Description |
|-------|-------------|
| `OpenJson` | Main facade class for all JSON operations: serialize, deserialize, parse, query, patch, validate |
| `JsonNode` | Tree model node representing a JSON value (object, array, string, number, boolean, null) |
| `JsonConfig` | Configuration for JSON processing: pretty print, null handling, date format, features |
| `TypeReference<T>` | Type token for generic type deserialization (e.g., `List<User>`) |

### Annotations — Basic

| Class | Description |
|-------|-------------|
| `@JsonProperty` | Maps a field to a JSON property name |
| `@JsonIgnore` | Excludes a field from serialization/deserialization |
| `@JsonFormat` | Specifies date/time format for serialization |
| `@JsonMask` | Masks sensitive field values in JSON output |
| `@JsonNaming` | Specifies naming strategy for JSON property names |

### Annotations — Polymorphism

| Class | Description |
|-------|-------------|
| `@JsonTypeInfo` | Configures polymorphic type handling (Id strategy + inclusion method) |
| `@JsonSubTypes` | Declares known subtypes for polymorphic deserialization |
| `@JsonTypeName` | Specifies logical type name for a class |

### Annotations — Constructor & Value

| Class | Description |
|-------|-------------|
| `@JsonCreator` | Marks constructor or factory method for deserialization |
| `@JsonValue` | Marks method/field whose value becomes the JSON representation |

### Annotations — Class-Level Metadata

| Class | Description |
|-------|-------------|
| `@JsonAutoDetect` | Controls property auto-detection visibility (field, getter, setter) |
| `@JsonPropertyOrder` | Controls property serialization order |
| `@JsonInclude` | Controls property inclusion (NON_NULL, NON_EMPTY, etc.) |

### Annotations — Reference Handling

| Class | Description |
|-------|-------------|
| `@JsonManagedReference` | Marks forward/parent side of bidirectional relationship |
| `@JsonBackReference` | Marks back/child side (omitted from serialization) |
| `@JsonIdentityInfo` | ID-based serialization for circular references |

### Annotations — Advanced

| Class | Description |
|-------|-------------|
| `@JsonSerialize` | Specifies custom serializer for a field or class |
| `@JsonDeserialize` | Specifies custom deserializer for a field or class |
| `@JsonUnwrapped` | Unwraps nested object properties into parent |
| `@JsonView` | View-based property filtering for selective serialization |
| `@JsonAnySetter` | Captures unknown JSON properties into a Map |
| `@JsonAnyGetter` | Serializes Map entries as regular JSON properties |
| `@JsonRawValue` | Embeds pre-formatted raw JSON string |
| `@JsonRootName` | Specifies root wrapping name for serialization |
| `@JsonAlias` | Defines alternative names for deserialization |
| `@JsonFilter` | Specifies named property filter for dynamic filtering |
| `@JsonInject` | Injects non-JSON values during deserialization |
| `@JsonEnumDefaultValue` | Marks default enum value for unknown entries |

### Identity

| Class | Description |
|-------|-------------|
| `ObjectIdGenerator<T>` | Abstract base for object ID generation strategies |
| `ObjectIdGenerators` | Built-in generators: IntSequence, UUID, Property, StringId |
| `ObjectIdResolver` | Interface for resolving object IDs to instances |
| `SimpleObjectIdResolver` | Default HashMap-based ID resolver |

### Adapter

| Class | Description |
|-------|-------------|
| `JsonTypeAdapter<T>` | Interface for custom type serialization/deserialization |
| `JsonTypeAdapterFactory` | Factory interface for creating type adapters dynamically |
| `JsonAdapterRegistry` | Registry for managing custom type adapters |
| `MixinSource` | Thread-safe registry for mixin annotation overlays |
| `PropertyFilter` | Dynamic property filtering with built-in strategies (include/exclude/nonNull) |

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
| `JsonModule` | Interface for pluggable JSON modules with `SimpleModule` convenience class |

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

### Utility (V1.0.3)

| Class | Description |
|-------|-------------|
| `JsonFlattener` | Flatten nested JSON to dot-notation key-value maps and unflatten back |
| `JsonCanonicalizer` | RFC 8785 JSON Canonicalization Scheme for deterministic output |
| `JsonTruncator` | Truncate large JSON for logging with structure-aware summary |
| `JsonEquals` | Structural equality comparison ignoring object key order |
| `JsonStrings` | String-level utilities: escape, unescape, isValid, minify, prettyPrint |

### Exceptions

| Class | Description |
|-------|-------------|
| `OpenJsonProcessingException` | Thrown for JSON parsing and processing errors (extends `OpenException`) |
| `JsonSchemaException` | Thrown when JSON schema validation fails |

## Utility API (V1.0.3)

### JsonFlattener

| Method | Description |
|--------|-------------|
| `flatten(JsonNode)` | Flatten to dot-notation map with bracket arrays: `a.b`, `c[0]` |
| `flatten(JsonNode, String separator)` | Flatten with custom separator |
| `flatten(JsonNode, FlattenConfig)` | Flatten with full configuration (separator, bracket/dot arrays, max depth) |
| `unflatten(Map<String,JsonNode>)` | Restore nested tree from flat map (auto-detects arrays) |
| `unflatten(Map<String,JsonNode>, String separator)` | Restore with custom separator |

### JsonCanonicalizer

| Method | Description |
|--------|-------------|
| `canonicalize(JsonNode)` | RFC 8785 JCS: sorted keys, ES6 numbers, minimal escaping |
| `canonicalize(String json)` | Parse then canonicalize (convenience) |

### JsonTruncator

| Method | Description |
|--------|-------------|
| `truncate(String json, int maxLength)` | Fast string-level truncation with `...(truncated)` marker |
| `truncate(JsonNode, TruncateConfig)` | Tree-level truncation producing valid JSON with configurable limits |
| `summary(JsonNode)` | One-line summary: `Object{5 properties}`, `Array[10 elements]`, `String(27 chars)` |

### JsonEquals

| Method | Description |
|--------|-------------|
| `equals(JsonNode, JsonNode)` | Structural equality: objects ignore key order, numbers by value (`1 == 1.0`) |
| `equals(String, String)` | Parse then compare (convenience) |
| `equalsIgnoreArrayOrder(JsonNode, JsonNode)` | Ignore both object key order and array element order |

### JsonStrings

| Method | Description |
|--------|-------------|
| `escape(String)` | RFC 8259 escape: `"` `\` control chars. Returns original string if no escaping needed (zero-alloc) |
| `unescape(String)` | Reverse escape: handles `\uXXXX`, surrogate pairs. Fast path if no `\` |
| `isValid(String)` | State-machine validation — no tree construction, 3× faster than parse |
| `minify(String)` | Strip whitespace outside strings, single pass |
| `prettyPrint(String)` | Indent with 2 spaces (default) |
| `prettyPrint(String, String indent)` | Indent with custom string |

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

// Polymorphic type handling
@JsonTypeInfo(id = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Dog.class, name = "dog"),
    @JsonSubTypes.Type(value = Cat.class, name = "cat")
})
public abstract class Animal { }

// Constructor deserialization
public class Point {
    @JsonCreator
    public Point(@JsonProperty("x") int x, @JsonProperty("y") int y) { }
}

// View-based filtering
public interface Summary {}
public interface Detail extends Summary {}

public class User {
    @JsonView(Summary.class)
    private String name;

    @JsonView(Detail.class)
    private String email;
}

// Module system
JsonModule module = new JsonModule.SimpleModule("myModule", "1.0") {{
    addAdapter(myTypeAdapter);
    addMixin(TargetClass.class, MixinClass.class);
}};
OpenJson json = OpenJson.withConfig(config).registerModule(module);

// Property filter
PropertyFilter filter = PropertyFilter.exclude("password", "secret");
json.setPropertyFilter("securityFilter", filter);

// Mixin (non-invasive annotations)
json.addMixin(ThirdPartyClass.class, MyMixin.class);

// --- V1.0.3 Utility Methods ---

// Quick JSON validation
boolean valid = OpenJson.isValid("{\"key\":\"value\"}"); // true
boolean invalid = OpenJson.isValid("{bad json}");         // false

// Minify / Pretty-print
String minified = OpenJson.minify("{ \"a\" : 1 }");     // {"a":1}
String pretty = OpenJson.prettyPrint("{\"a\":1}");

// Structural equality (ignoring key order)
boolean eq = OpenJson.structuralEquals(nodeA, nodeB);

// Flatten / Unflatten
Map<String, JsonNode> flat = OpenJson.flatten(nestedNode);
// {"a.b": 1, "c[0]": 2, "c[1]": 3}
JsonNode restored = OpenJson.unflatten(flat);

// RFC 8785 Canonicalization
String canonical = OpenJson.canonicalize(node);

// Truncate for logging
String truncated = OpenJson.truncate(hugeJsonString, 200);

// String utilities
String escaped = JsonStrings.escape("hello\nworld");
String unescaped = JsonStrings.unescape("hello\\nworld");
```

## Requirements

- Java 25+

## License

Apache License 2.0
