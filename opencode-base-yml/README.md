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
- **Security**: Safe YAML loading with type restrictions and depth limits

## Quick Start

### Maven Dependency
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-yml</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Basic Usage

```java
import cloud.opencode.base.yml.*;

// Parse YAML
Map<String, Object> data = OpenYml.load("server:\n  port: 8080");
YmlDocument doc = OpenYml.parse("server:\n  port: 8080");

// Load from file
YmlDocument doc = OpenYml.loadFile(Path.of("config.yml"));

// Access values via path
String port = doc.getString("server.port");
int timeout = doc.getInt("server.timeout", 30);

// Write YAML
String yaml = OpenYml.dump(Map.of("name", "test", "version", "1.0"));
OpenYml.writeFile(data, Path.of("output.yml"));

// Object binding
@YmlProperty("server")
record ServerConfig(int port, String host) {}
ServerConfig config = OpenYml.bind(doc, "server", ServerConfig.class);

// Merge documents
Map<String, Object> merged = OpenYml.merge(baseMap, overlayMap);
YmlDocument mergedDoc = OpenYml.merge(baseDoc, overlayDoc);

// Placeholder resolution
String resolved = OpenYml.resolvePlaceholders(
    "server:\n  port: ${SERVER_PORT:8080}");
YmlDocument doc = OpenYml.parseWithPlaceholders(yaml);

// Multi-document YAML
List<YmlDocument> docs = OpenYml.loadAll(multiDocYaml);

// Validation
boolean valid = OpenYml.isValid(yamlString);

// Node tree
YmlNode tree = OpenYml.parseTree(yaml);
```

## Class Reference

### Root Package (`cloud.opencode.base.yml`)
| Class | Description |
|-------|-------------|
| `OpenYml` | Main facade: parse, load, dump, bind, merge, placeholders, validate |
| `YmlDocument` | Parsed YAML document with path-based access (getString, getInt, getList, etc.) |
| `YmlNode` | YAML node tree interface for hierarchical navigation |
| `DefaultYmlNode` | Default YmlNode implementation |
| `YmlConfig` | YAML output configuration (indent, flow style, etc.) |

### Bind (`yml.bind`)
| Class | Description |
|-------|-------------|
| `YmlBinder` | YAML-to-Java object binding engine |
| `ConfigBinder` | Configuration-specific binding with prefix support |
| `PropertySource` | Abstraction for property sources |
| `@YmlProperty` | Specify YAML property name for a field |
| `@YmlAlias` | Define alternative property names |
| `@YmlIgnore` | Exclude field from YAML binding |
| `@YmlNestedProperty` | Mark field as nested YAML property |
| `@YmlValue` | Map field to scalar YAML value |

### Exception (`yml.exception`)
| Class | Description |
|-------|-------------|
| `OpenYmlException` | Base exception for YAML operations |
| `YmlBindException` | Exception for YAML binding errors |
| `YmlParseException` | Exception for YAML parsing errors |
| `YmlPathException` | Exception for YAML path resolution errors |
| `YmlPlaceholderException` | Exception for placeholder resolution errors |
| `YmlSecurityException` | Exception for YAML security violations |

### Merge (`yml.merge`)
| Class | Description |
|-------|-------------|
| `YmlMerger` | Merge multiple YAML maps with configurable strategy |
| `MergeStrategy` | Merge strategy enum (OVERLAY, DEEP_MERGE, REPLACE) |

### Path (`yml.path`)
| Class | Description |
|-------|-------------|
| `YmlPath` | YAML path expression (dot-notation) parser |
| `PathResolver` | Resolve dot-notation paths against YAML maps |

### Placeholder (`yml.placeholder`)
| Class | Description |
|-------|-------------|
| `PlaceholderResolver` | Resolve `${...}` placeholders with default values |
| `PropertyPlaceholder` | Placeholder parsing and resolution |

### Security (`yml.security`)
| Class | Description |
|-------|-------------|
| `SafeConstructor` | Safe YAML constructor with type restrictions |
| `YmlSecurity` | YAML security configuration |
| `YmlSafeLoader` | Safe YAML loader with depth and size limits |

### SnakeYAML (`yml.snakeyaml`)
| Class | Description |
|-------|-------------|
| `SnakeYamlProvider` | SnakeYAML-based YmlProvider implementation |
| `SnakeYmlNode` | SnakeYAML-based YmlNode implementation |

### SPI (`yml.spi`)
| Class | Description |
|-------|-------------|
| `YmlProvider` | SPI interface for YAML processing providers |
| `YmlProviderFactory` | Factory for discovering and loading YmlProvider implementations |
| `YmlFeature` | Feature flags for YAML providers |

## Requirements

- Java 25+
- No external dependencies for core functionality
- Optional: `org.yaml:snakeyaml:2.2` for full YAML 1.1 support

## License

Apache License 2.0

## Author

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
