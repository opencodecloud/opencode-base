# OpenCode Base YML

**YAML processing library with SPI support for Java 25+**

`opencode-base-yml` provides a comprehensive YAML processing toolkit with parsing, writing, object binding, document merging, placeholder resolution, path queries, and security features. It uses an SPI-based provider architecture with optional SnakeYAML integration.

## Features

### Core Features
- **YAML Parsing**: Parse YAML strings, files, and streams to maps or documents
- **YAML Writing**: Dump objects and maps to YAML strings or files
- **Multi-Document**: Load and dump multi-document YAML
- **Validation**: Check YAML string validity

### Advanced Features
- **Object Binding**: Bind YAML documents to Java objects with annotation support
- **Document Merging**: Merge multiple YAML documents with configurable strategies
- **Placeholder Resolution**: Resolve `${...}` placeholders with system properties, environment variables, or custom properties
- **Path Queries**: Navigate YAML documents using dot-notation paths
- **Node Tree**: Parse YAML into a navigable node tree
- **SPI Architecture**: Pluggable YAML provider via `YmlProvider` SPI
- **SnakeYAML Integration**: Optional SnakeYAML backend for full YAML 1.1 support
- **Security**: Safe YAML loading with type restrictions, depth limits, and YAML bomb prevention

### V1.0.3 New Features
- **Unified Exception Hierarchy**: `OpenYmlException` now extends `OpenException` with error codes and component tracking
- **YAML Diff**: Compare two YAML documents and get a list of additions, removals, and modifications
- **Flatten/Unflatten**: Convert nested YAML to flat dot-notation maps and back
- **YAML-JSON**: Convert between YAML and JSON without external dependencies
- **Schema Validation**: Lightweight structural validation (required keys, type constraints, ranges, patterns)
- **!include Directives**: Resolve `!include` file references with path traversal protection and circular reference detection
- **Profile Loading**: Load base + profile-specific YAML files with deep merge (e.g., `application.yml` + `application-dev.yml`)
- **Strict Types Mode**: YAML 1.2 strict boolean handling to prevent the Norway problem (YES/NO -> boolean)
- **Thread-safe Provider**: SnakeYamlProvider creates per-call Yaml instances for thread safety

## Quick Start

### Maven Dependency
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-yml</artifactId>
    <version>1.0.3</version>
</dependency>
```

### Basic Usage

```java
import cloud.opencode.base.yml.*;
import java.nio.file.Path;
import java.util.Map;
import java.util.List;

// Parse YAML
Map<String, Object> data = OpenYml.load("server:\n  port: 8080");
YmlDocument doc = OpenYml.parse("server:\n  port: 8080");

// Load from file
YmlDocument doc = OpenYml.loadFile(Path.of("config.yml"));

// Access values via path
String host = doc.getString("server.host");
int port = doc.getInt("server.port", 8080);
boolean debug = doc.getBoolean("server.debug");
List<String> tags = doc.getList("tags");
Map<String, Object> db = doc.getMap("database");

// Write YAML
String yaml = OpenYml.dump(Map.of("name", "test", "version", "1.0"));
OpenYml.writeFile(data, Path.of("output.yml"));

// Object binding
record ServerConfig(int port, String host) {}
ServerConfig config = OpenYml.bind(doc, "server", ServerConfig.class);

// Merge documents
Map<String, Object> merged = OpenYml.merge(baseMap, overlayMap);

// Placeholder resolution
String resolved = OpenYml.resolvePlaceholders("server:\n  port: ${SERVER_PORT:8080}");

// Multi-document YAML
List<YmlDocument> docs = OpenYml.loadAll(multiDocYaml);
String multiDoc = OpenYml.dumpAll(List.of(map1, map2));

// Validation
boolean valid = OpenYml.isValid(yamlString);
```

### V1.0.3 Features

```java
import cloud.opencode.base.yml.diff.*;
import cloud.opencode.base.yml.transform.*;
import cloud.opencode.base.yml.schema.*;
import cloud.opencode.base.yml.profile.*;
import cloud.opencode.base.yml.include.*;

// YAML Diff
List<DiffEntry> diffs = OpenYml.diff(baseMap, otherMap);
diffs.forEach(d -> System.out.println(d.type() + " " + d.path()));

// Flatten / Unflatten
Map<String, Object> flat = OpenYml.flatten(nestedMap);
// {"server.port": 8080, "server.host": "localhost"}
Map<String, Object> nested = OpenYml.unflatten(flat);

// YAML to JSON
String json = OpenYml.toJson("server:\n  port: 8080");
String prettyJson = OpenYml.toJson("key: value", true);
Map<String, Object> fromJson = OpenYml.fromJson("{\"key\": \"value\"}");
String yamlFromJson = YmlJson.fromJsonToYaml("{\"port\": 8080}");

// Schema Validation
YmlSchema schema = YmlSchema.builder()
    .required("server.port", "server.host")
    .type("server.port", Integer.class)
    .range("server.port", 1, 65535)
    .pattern("server.host", "^[a-zA-Z0-9.-]+$")
    .nested("database", YmlSchema.builder().required("url").build())
    .rule("name", v -> ((String) v).length() <= 100, "Name too long")
    .build();
ValidationResult result = schema.validate(data);
if (!result.isValid()) {
    result.getErrors().forEach(e -> System.err.println(e.path() + ": " + e.message()));
}

// Profile Loading
YmlDocument doc = YmlProfile.load(Path.of("config"), "application", "dev", "local");
// Loads application.yml -> application-dev.yml -> application-local.yml (deep merge)
List<String> activeProfiles = YmlProfile.getActiveProfiles();
// Reads from system property "yml.profiles.active" or env "YAML_PROFILES_ACTIVE"

// !include Resolution
Map<String, Object> data = YmlIncludeResolver.load(Path.of("config.yml"));
// Or with custom settings:
YmlIncludeResolver resolver = YmlIncludeResolver.builder()
    .basePath(Path.of("/etc/config"))
    .maxDepth(5)
    .allowedExtensions(Set.of(".yml", ".yaml"))
    .build();
Map<String, Object> result = resolver.resolve(Path.of("/etc/config/app.yml"));

// Placeholder Resolution (advanced)
PlaceholderResolver resolver = PlaceholderResolver.builder()
    .withSystemProperties()
    .withEnvironmentVariables()
    .addPropertySource("custom", Map.of("APP_PORT", "9090"))
    .strict(true)
    .build();
String resolved = resolver.resolve("port: ${APP_PORT}");

// Strict Types (solve Norway problem)
YmlConfig strictConfig = YmlConfig.builder().strictTypes(true).build();
// "YES" stays as String "true" instead of Boolean true
```

## API Reference

### OpenYml (Main Facade)

| Method | Description |
|--------|-------------|
| `load(String yaml)` | Parse YAML string to Map |
| `parse(String yaml)` | Parse YAML string to YmlDocument |
| `loadFile(Path)` | Load YAML from file |
| `loadFile(Path, Charset)` | Load YAML from file with charset |
| `loadStream(InputStream)` | Load YAML from stream |
| `loadAll(String yaml)` | Load multi-document YAML |
| `dump(Object)` | Dump object to YAML string |
| `dump(Object, YmlConfig)` | Dump with configuration |
| `dump(YmlDocument)` | Dump document to YAML string |
| `dumpObject(Object)` | Dump Java object via reflection to YAML |
| `writeFile(Object, Path)` | Write YAML to file |
| `writeFile(Object, Path, Charset)` | Write YAML to file with charset |
| `write(Object, Writer)` | Write YAML to Writer |
| `dumpAll(Iterable<?>)` | Dump multiple documents |
| `bind(YmlDocument, Class<T>)` | Bind document to Java object |
| `bind(YmlDocument, String, Class<T>)` | Bind document at path to Java object |
| `bind(String, Class<T>)` | Bind YAML string to Java object |
| `toMap(Object)` | Convert Java object to Map |
| `merge(Map, Map)` | Merge two maps (deep merge) |
| `merge(Map, Map, MergeStrategy)` | Merge with strategy |
| `mergeAll(Map...)` | Merge multiple maps |
| `merge(YmlDocument, YmlDocument)` | Merge two documents |
| `resolvePlaceholders(String)` | Resolve `${...}` placeholders |
| `resolvePlaceholders(String, PlaceholderResolver)` | Resolve with custom resolver |
| `resolvePlaceholders(Map)` | Resolve placeholders in Map values |
| `parseWithPlaceholders(String)` | Parse YAML with placeholder resolution |
| `parseWithPlaceholders(String, Map)` | Parse with custom properties |
| `isValid(String)` | Check YAML validity |
| `parseTree(String)` | Parse to YmlNode tree |
| `diff(Map, Map)` | Compare two maps |
| `diff(YmlDocument, YmlDocument)` | Compare two documents |
| `flatten(Map)` | Flatten nested map to dot-notation |
| `unflatten(Map)` | Unflatten dot-notation map |
| `toJson(String)` | Convert YAML to JSON |
| `toJson(String, boolean)` | Convert YAML to JSON (pretty) |
| `fromJson(String)` | Parse JSON to Map |
| `fromJsonToYaml(String)` | Convert JSON to YAML string |
| `validate(Map, YmlSchema)` | Validate map against schema |
| `validate(YmlDocument, YmlSchema)` | Validate document against schema |
| `loadProfile(Path, String, String...)` | Load with profile overlays |
| `loadDefaultProfile(Path, String...)` | Load with default name "application" |
| `loadWithIncludes(Path)` | Load with !include resolution |

### YmlDocument

| Method | Description |
|--------|-------------|
| `of(Map)` | Create from Map |
| `get(String path)` | Get value at path |
| `getString(String path)` | Get String value |
| `getString(String, String)` | Get String with default |
| `getInt(String path)` | Get Integer value |
| `getInt(String, int)` | Get int with default |
| `getLong(String path)` | Get Long value |
| `getBoolean(String path)` | Get Boolean value |
| `getBoolean(String, boolean)` | Get boolean with default |
| `getList(String path)` | Get List value |
| `getMap(String path)` | Get Map value |
| `getDocument(String path)` | Get sub-document at path |
| `getOptional(String path)` | Get Optional value |
| `has(String path)` | Check if path exists |
| `keys()` | Get root-level key set |
| `size()` | Get root-level entry count |
| `isEmpty()` | Check if document is empty |
| `asMap()` | Get as unmodifiable Map |
| `asList()` | Get as unmodifiable List |

### YmlNode (Tree Navigation)

| Method | Description |
|--------|-------------|
| `of(Object)` | Create node from raw data |
| `asText()` / `asText(String)` | Get text value (with default) |
| `asInt()` / `asInt(int)` | Get int value (with default) |
| `asLong()` / `asLong(long)` | Get long value (with default) |
| `asBoolean()` / `asBoolean(boolean)` | Get boolean value (with default) |
| `asDouble()` / `asDouble(double)` | Get double value (with default) |
| `get(String key)` | Get child by key |
| `get(int index)` | Get child by index |
| `at(String path)` | Navigate to path |
| `has(String key)` | Check child exists |
| `size()` | Get child count |
| `keys()` | Get child keys |
| `values()` | Get child values |
| `toObject()` / `toMap()` / `toList()` | Convert to Java types |
| `toYaml()` | Convert to YAML string |
| `getType()` | Get NodeType (SCALAR/SEQUENCE/MAPPING/NULL) |
| `isScalar()` / `isSequence()` / `isMapping()` / `isNull()` | Type checks |
| `getRawValue()` | Get underlying raw value |

### YmlConfig (Builder)

| Builder Method | Description |
|----------------|-------------|
| `defaults()` | Create default config |
| `builder()` | Create builder |
| `indent(int)` | Set indentation (default: 2) |
| `prettyPrint(boolean)` | Enable pretty printing |
| `safeMode(boolean)` | Enable safe mode |
| `strictTypes(boolean)` | Enable YAML 1.2 strict type mode |
| `allowDuplicateKeys(boolean)` | Allow duplicate keys |
| `maxAliasesForCollections(int)` | Max alias count (default: 50) |
| `maxNestingDepth(int)` | Max nesting depth |
| `maxDocumentSize(long)` | Max document size in bytes |
| `defaultFlowStyle(FlowStyle)` | Set flow style: FLOW / BLOCK / AUTO |
| `defaultScalarStyle(ScalarStyle)` | Set scalar style: PLAIN / SINGLE_QUOTED / DOUBLE_QUOTED / LITERAL / FOLDED |

### YmlDiff

| Method | Description |
|--------|-------------|
| `diff(Map, Map)` | Compare two maps, return List of DiffEntry |
| `diff(YmlDocument, YmlDocument)` | Compare two documents |

`DiffEntry` record: `type()` (ADDED/REMOVED/MODIFIED), `path()`, `oldValue()`, `newValue()`

Factory methods: `DiffEntry.added(path, value)`, `DiffEntry.removed(path, value)`, `DiffEntry.modified(path, old, new)`

### YmlFlattener

| Method | Description |
|--------|-------------|
| `flatten(Map)` | Flatten with "." separator |
| `flatten(Map, String)` | Flatten with custom separator |
| `unflatten(Map)` | Unflatten with "." separator |
| `unflatten(Map, String)` | Unflatten with custom separator |

### YmlJson

| Method | Description |
|--------|-------------|
| `toJson(String yaml)` | YAML string to compact JSON |
| `toJson(String yaml, boolean pretty)` | YAML string to JSON (pretty optional) |
| `toJson(Map)` | Map to compact JSON |
| `toJson(Map, boolean)` | Map to JSON (pretty optional) |
| `fromJson(String json)` | JSON string to Map |
| `fromJsonToYaml(String json)` | JSON string to YAML string |

### YmlSchema (Builder)

| Builder Method | Description |
|----------------|-------------|
| `required(String... keys)` | Add required keys |
| `type(String path, Class<?>)` | Add type constraint |
| `range(String path, Comparable min, Comparable max)` | Add value range |
| `pattern(String path, String regex)` | Add regex pattern |
| `nested(String path, YmlSchema)` | Add nested schema |
| `rule(String path, Predicate<Object>, String msg)` | Add custom rule |

`ValidationResult`: `isValid()`, `getErrors()`

`ValidationError` record: `path()`, `message()`, `type()` (MISSING_REQUIRED / TYPE_MISMATCH / OUT_OF_RANGE / PATTERN_MISMATCH / CUSTOM_RULE_FAILED)

### YmlIncludeResolver (Builder)

| Method | Description |
|--------|-------------|
| `load(Path file)` | Static convenience: load with defaults |
| `resolve(Path file)` | Resolve file with !include directives |
| `resolve(String yaml, Path basePath)` | Resolve YAML string |
| `builder().maxDepth(int)` | Max include depth (default: 10) |
| `builder().basePath(Path)` | Base directory for path containment |
| `builder().allowedExtensions(Set)` | Allowed file extensions (default: .yml, .yaml) |

### YmlProfile

| Method | Description |
|--------|-------------|
| `load(Path, String name, String... profiles)` | Load base + profiles with deep merge |
| `load(Path, String, MergeStrategy, List<String>)` | Load with custom strategy |
| `loadDefault(Path, String... profiles)` | Load using default name "application" |
| `getActiveProfiles()` | Get profiles from system property or env var |

### MergeStrategy

| Value | Description |
|-------|-------------|
| `OVERRIDE` | Later value wins completely |
| `KEEP_FIRST` | Earlier value wins |
| `DEEP_MERGE` | Recursive merge of nested maps |
| `APPEND_LISTS` | Concatenate list values |
| `MERGE_LISTS_UNIQUE` | Merge lists with deduplication |
| `FAIL_ON_CONFLICT` | Throw on conflicting values |

### PlaceholderResolver (Builder)

| Method | Description |
|--------|-------------|
| `create()` | Create empty resolver |
| `create(Map<String, String>)` | Create with properties |
| `builder()` | Create builder |
| `resolve(String text)` | Resolve placeholders in text |
| `resolveYaml(String yaml)` | Resolve placeholders in YAML |
| `resolveMap(Map)` | Resolve placeholders in Map values |
| `builder().withSystemProperties()` | Add system properties source |
| `builder().withEnvironmentVariables()` | Add environment variables source |
| `builder().addPropertySource(String, Map)` | Add custom property source |
| `builder().addResolver(Function)` | Add custom resolver function |
| `builder().prefix(String)` | Custom prefix (default: `${`) |
| `builder().suffix(String)` | Custom suffix (default: `}`) |
| `builder().defaultValueSeparator(String)` | Custom separator (default: `:`) |
| `builder().strict(boolean)` | Throw on unresolved placeholders |

### Bind Annotations

| Annotation | Description |
|------------|-------------|
| `@YmlProperty(value, defaultValue, required)` | Map field to YAML property path |
| `@YmlAlias(String[])` | Alternative property names |
| `@YmlIgnore` | Exclude field from binding |
| `@YmlNestedProperty(prefix)` | Nested object with path prefix |
| `@YmlValue(value, defaultValue)` | Map field to YAML value path |

### ConfigBinder

| Method | Description |
|--------|-------------|
| `bind(String yaml, Class<T>)` | Bind YAML string to config class |
| `bind(PropertySource, Class<T>)` | Bind property source to config class |
| `bind(Map, Class<T>)` | Bind Map to config class |

### PropertySource

| Method | Description |
|--------|-------------|
| `fromMap(String, Map)` | Create from Map |
| `fromYaml(String, String)` | Create from YAML string |
| `fromEnvironment()` | Create from environment variables |
| `fromSystemProperties()` | Create from system properties |
| `getProperty(String)` | Get property value |
| `getProperty(String, Class<T>)` | Get typed property value |
| `containsProperty(String)` | Check property exists |
| `getPropertyNames()` | Get all property names |
| `getProperties(String prefix)` | Get properties by prefix |

### YmlSecurity

| Method | Description |
|--------|-------------|
| `validate(String)` | Validate YAML for security issues |
| `validate(String, long, int)` | Validate with custom limits |
| `containsDangerousPatterns(String)` | Check for dangerous YAML tags |
| `countAliases(String)` | Count alias references |
| `countAnchors(String)` | Count anchor definitions |
| `createSafeConfig()` | Create safe YmlConfig |
| `createSafeConfig(long, int)` | Create safe config with custom limits |
| `sanitize(String)` | Remove dangerous patterns |
| `isSafeType(String)` | Check if type name is safe |

### YmlSafeLoader (Builder)

| Method | Description |
|--------|-------------|
| `create()` | Create with defaults |
| `builder()` | Create builder |
| `validate(Object)` | Validate data structure |
| `isAllowedType(Class<?>)` | Check type is allowed |
| `isDeniedTag(String)` | Check tag is denied |
| `builder().allowType(Class<?>)` | Add allowed type |
| `builder().denyTag(String)` | Add denied tag |
| `builder().maxDepth(int)` | Max nesting depth (default: 100) |
| `builder().maxSize(int)` | Max document size (default: 10MB) |
| `builder().maxAliases(int)` | Max alias count (default: 50) |

### PathResolver / YmlPath

| Method | Description |
|--------|-------------|
| `PathResolver.get(Object, String)` | Get value at dot-notation path |
| `PathResolver.get(Object, String, T)` | Get with default |
| `PathResolver.getString/Int/Long/Boolean(Object, String)` | Typed access |
| `PathResolver.getOptional(Object, String)` | Optional access |
| `PathResolver.has(Object, String)` | Check path exists |
| `PathResolver.getList/Map(Object, String)` | Collection access |
| `YmlPath.of(String)` | Parse path string (e.g. "a.b[0].c") |
| `YmlPath.of(String...)` | Create from segments |
| `YmlPath.root()` | Root path |
| `YmlPath.child(String)` | Append property segment |
| `YmlPath.index(int)` | Append array index segment |
| `YmlPath.parent()` | Navigate to parent |
| `YmlPath.depth()` | Get path depth |

## Requirements

- Java 25+
- No external dependencies for core functionality
- Optional: `org.yaml:snakeyaml:2.2` for full YAML 1.1 support

## License

Apache License 2.0

## Author

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
