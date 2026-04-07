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
    <version>1.0.3</version>
</dependency>
```

## What's New in v1.0.3

- **Relaxed Binding** — `database.max-pool-size` matches `DATABASE_MAX_POOL_SIZE`, `database.maxPoolSize`, etc. Opt-in via `ConfigBuilder.enableRelaxedBinding()`.
- **ConfigDump** — Export all effective config with sensitive value masking (`***`). Default patterns: password, secret, token, key, credential, auth, bearer.
- **ConfigDiff** — Compare two config snapshots, returns `List<ConfigChangeEvent>` with formatted output.
- **@DefaultValue on POJO fields** — Extended from records to POJO fields for `ConfigBinder`.
- **Validation merge** — `ValidationResult.merge()` collects all validation errors into a single report.
- **Security hardening** — SSRF DNS rebinding prevention (IP pinning), sensitive values redacted from toString/exceptions, PlaceholderResolver depth capping.

## API Overview

### Core

| Class | Description |
|-------|-------------|
| `Config` | Core configuration interface (get, getAs, getOrDefault, subscribe) |
| `OpenConfig` | Global configuration manager facade |
| `ConfigBuilder` | Fluent builder for creating Config instances |
| `RelaxedKeyResolver` **[v1.0.3]** | Relaxed binding: normalize, variants, resolve across naming conventions |
| `ConfigDump` **[v1.0.3]** | Export config with sensitive value masking |
| `ConfigDiff` **[v1.0.3]** | Compare two config snapshots for changes |
| `ConfigChangeEvent` | Configuration change event record (key, changeType, timestamp) |
| `ConfigChangeType` | Change type enum (ADDED, MODIFIED, REMOVED) |
| `ConfigListener` | Configuration change listener interface |
| `OpenConfigException` | Configuration exception (values redacted for security) |

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
| `ConfigBinder` | Bind configuration to JavaBean instances (supports @DefaultValue) |
| `RecordConfigBinder` | Bind configuration to record instances (supports @DefaultValue) |
| `ConfigProperties` | Annotation for configuration prefix binding |
| `NestedConfig` | Annotation for nested configuration objects |
| `DefaultValue` **[v1.0.3]** | Default value annotation for record components and POJO fields |

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

### v1.0.3 Features

```java
import cloud.opencode.base.config.*;
import cloud.opencode.base.config.bind.DefaultValue;

// ---- Relaxed Binding ----
Config config = OpenConfig.builder()
    .addEnvironmentVariables()             // DATABASE_MAX_POOL_SIZE=10
    .enableRelaxedBinding()
    .build();
int poolSize = config.getInt("database.max-pool-size"); // matches env var → 10

// ---- ConfigDump (debug with sensitive masking) ----
Map<String, String> dump = ConfigDump.dump(config);
// db.password → "***", app.name → "myapp"
System.out.println(ConfigDump.dumpToString(config));

// ---- ConfigDiff (compare snapshots) ----
Config before = OpenConfig.builder().addProperties(Map.of("a", "1", "b", "2")).build();
Config after  = OpenConfig.builder().addProperties(Map.of("a", "1", "b", "3", "c", "4")).build();
List<ConfigChangeEvent> changes = ConfigDiff.diff(before, after);
System.out.println(ConfigDiff.format(changes));
// ~ b: 2 -> 3
// + c = 4

// ---- @DefaultValue on POJO fields ----
public class ServerConfig {
    @DefaultValue("8080") int port;
    @DefaultValue("localhost") String host;
}
ServerConfig server = config.bind("server", ServerConfig.class);
// port=8080 if server.port not configured

// ---- Validation merge (all errors at once) ----
Config validated = OpenConfig.builder()
    .addProperties(Map.of("app.name", "myapp"))
    .required("db.url", "db.user", "app.port")
    .build(); // throws: "Missing required keys: [db.url, db.user, app.port]"
```

## Requirements

- Java 25+

## License

Apache License 2.0
