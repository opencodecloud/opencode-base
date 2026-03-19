# OpenCode Base Config

Flexible configuration management library with multiple sources, type conversion, hot reload, validation, and JDK 25+ features for Java applications.

## Features

- Multiple configuration sources (properties, YAML, environment, system properties, command line, HTTP, in-memory)
- Prioritized composite configuration source
- Automatic type conversion with extensible converter registry
- Hot reload with file watching (virtual thread powered)
- Configuration change events and listeners
- Placeholder resolution and expression evaluation
- Bean binding (Java beans and records)
- Configuration validation (required, range, pattern)
- Multi-profile support (dev, test, prod)
- Multi-tenant configuration management
- Encrypted configuration processing
- SPI-based extensibility for sources and converters
- JDK 25 features: sealed types, records, scoped values, virtual threads

## Maven

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-config</artifactId>
    <version>1.0.0</version>
</dependency>
```

## API Overview

### Core

| Class | Description |
|-------|-------------|
| `Config` | Core configuration interface (get, getAs, getOrDefault, subscribe) |
| `OpenConfig` | Global configuration manager facade |
| `ConfigBuilder` | Fluent builder for creating Config instances |
| `ConfigChangeEvent` | Configuration change event record (key, oldValue, newValue, type) |
| `ConfigChangeType` | Change type enum (ADDED, MODIFIED, REMOVED) |
| `ConfigListener` | Configuration change listener interface |
| `OpenConfigException` | Configuration exception |

### Configuration Sources

| Class | Description |
|-------|-------------|
| `ConfigSource` | Configuration source interface (load, reload, priority) |
| `PropertiesConfigSource` | Java .properties file source |
| `YamlConfigSource` | YAML file source (requires opencode-base-yml) |
| `EnvironmentConfigSource` | OS environment variables source |
| `SystemPropertiesConfigSource` | JVM system properties source |
| `CommandLineConfigSource` | Command line arguments source |
| `InMemoryConfigSource` | In-memory key-value source |
| `CompositeConfigSource` | Priority-ordered composite of multiple sources |

### Type Converters

| Class | Description |
|-------|-------------|
| `ConfigConverter` | Type converter interface |
| `ConverterRegistry` | Converter registry with built-in type support |
| `SpiConverterRegistry` | SPI-based auto-discovering converter registry |
| `StringConverter` | String identity converter |
| `BooleanConverter` | Boolean converter (true/false/yes/no/1/0) |
| `NumberConverters` | Integer, Long, Double, Float, BigDecimal converters |
| `DurationConverter` | Duration converter (e.g., "30s", "5m", "1h") |
| `DateTimeConverters` | LocalDate, LocalDateTime, Instant converters |
| `EnumConverter` | Generic enum converter |
| `CollectionConverters` | List, Set, Map converters from comma-separated strings |

### Bean Binding

| Class | Description |
|-------|-------------|
| `ConfigBinder` | Bind configuration to JavaBean instances |
| `RecordConfigBinder` | Bind configuration to record instances |
| `ConfigProperties` | Annotation for configuration prefix binding |
| `NestedConfig` | Annotation for nested configuration objects |

### Validation

| Class | Description |
|-------|-------------|
| `ConfigValidator` | Validator interface |
| `RequiredValidator` | Validates required keys exist |
| `RangeValidator` | Validates numeric values within range |
| `PatternValidator` | Validates string values match regex pattern |
| `ValidationResult` | Validation result with errors |
| `ValidationModuleAdapter` | Adapter for external validation modules |

### Placeholder & Expression

| Class | Description |
|-------|-------------|
| `PlaceholderResolver` | Resolves `${key}` and `${key:default}` placeholders |
| `ExpressionEvaluator` | Simple expression evaluator for config values |

### Advanced

| Class | Description |
|-------|-------------|
| `MultiProfileConfig` | Multi-profile configuration (dev/test/prod) |
| `TenantConfigManager` | Multi-tenant configuration manager |
| `EncryptedConfigProcessor` | Encrypted configuration value processor |
| `ConfigSourceProvider` | SPI interface for custom config source providers |
| `HttpConfigSourceProvider` | HTTP-based remote configuration source |
| `ConfigConverterProvider` | SPI interface for custom converter providers |
| `ConfigSourceFactory` | Factory for creating config sources |

### JDK 25 Features

| Class | Description |
|-------|-------------|
| `ConfigSourceType` | Sealed interface for type-safe config source classification |
| `ConfigContext` | Scoped configuration context |
| `ContextAwareConfig` | Context-aware Config decorator |
| `ConfigSourceProcessor` | Config source processor using pattern matching |
| `ReactiveConfigValue` | Reactive configuration value wrapper |
| `VirtualThreadConfigWatcher` | Virtual thread-based configuration file watcher |
| `DefaultValue` | Annotation for default configuration values |
| `Required` | Annotation for required configuration values |

### Internal

| Class | Description |
|-------|-------------|
| `DefaultConfig` | Default Config implementation (AutoCloseable) |
| `ConfigWatcher` | File system config watcher (AutoCloseable) |

## Quick Start

```java
import cloud.opencode.base.config.*;

// Quick start with properties file
Config config = OpenConfig.builder()
    .addSource(new PropertiesConfigSource("app.properties"))
    .addSource(new EnvironmentConfigSource())
    .build();

// Get values with type conversion
String name = config.get("app.name");
int port = config.getAs("server.port", Integer.class);
Duration timeout = config.getAs("server.timeout", Duration.class);

// With defaults
String env = config.getOrDefault("app.env", "development");

// Bind to a bean
@ConfigProperties(prefix = "database")
public class DatabaseConfig {
    private String url;
    private String username;
    private int maxPoolSize;
    // getters/setters
}

DatabaseConfig dbConfig = ConfigBinder.bind(config, DatabaseConfig.class);

// Bind to a record
public record ServerConfig(String host, int port, Duration timeout) {}
ServerConfig server = RecordConfigBinder.bind(config, "server", ServerConfig.class);

// Listen for changes
config.subscribe("server.*", event -> {
    System.out.println("Config changed: " + event.key() +
        " = " + event.newValue());
});

// Multi-profile
Config profileConfig = MultiProfileConfig.builder()
    .baseSource(new PropertiesConfigSource("app.properties"))
    .profileSource("dev", new PropertiesConfigSource("app-dev.properties"))
    .profileSource("prod", new PropertiesConfigSource("app-prod.properties"))
    .activeProfile("dev")
    .build();
```

## Requirements

- Java 25+

## License

Apache License 2.0
