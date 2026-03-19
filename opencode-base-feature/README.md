# OpenCode Base Feature

**Feature toggle/flag management library for Java 25+**

`opencode-base-feature` provides comprehensive feature toggle support for grayscale release, A/B testing, and dynamic configuration. It offers pluggable storage backends, multiple rollout strategies, and an annotation-driven API.

## Features

### Core Features
- **Feature Registration**: Register, enable, disable, and delete feature flags
- **Strategy-Based Evaluation**: Pluggable strategies for feature activation decisions
- **Context-Aware**: User-based, tenant-based, and attribute-based evaluation
- **Listener Support**: Event-driven notifications on feature state changes
- **Pluggable Storage**: In-memory, file-based, cached, LRU, and Redis-backed stores

### Rollout Strategies
- **AlwaysOn / AlwaysOff**: Static on/off strategies
- **PercentageStrategy**: Random percentage-based rollout
- **ConsistentPercentageStrategy**: Deterministic hash-based percentage rollout
- **UserListStrategy**: Whitelist specific users
- **TenantAwareStrategy**: Tenant-specific feature management
- **DateRangeStrategy**: Time-based feature activation windows
- **ExpressionStrategy**: Expression language-based dynamic rules
- **CompositeStrategy**: Combine multiple strategies with AND/OR logic

### Advanced Features
- **Annotation Support**: `@FeatureToggle` and `@FeatureVariant` annotations
- **Proxy-Based Routing**: Dynamic variant routing via `FeatureProxy`
- **Audit Logging**: Full audit trail with `FileAuditLogger`
- **Security Management**: `SecureFeatureManager` with permission controls
- **Metrics**: `MetricsFeatureListener` for feature usage tracking

## Quick Start

### Maven Dependency
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-feature</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Basic Usage

```java
import cloud.opencode.base.feature.*;
import cloud.opencode.base.feature.strategy.*;

// Get singleton instance
OpenFeature features = OpenFeature.getInstance();

// Register a feature
Feature darkMode = Feature.builder("dark-mode")
    .name("Dark Mode")
    .description("Enable dark theme")
    .defaultEnabled(false)
    .strategy(new PercentageStrategy(30))
    .build();
features.register(darkMode);

// Check if enabled
if (features.isEnabled("dark-mode")) {
    // Use dark mode
}

// Check with user context
FeatureContext ctx = FeatureContext.builder()
    .userId("user123")
    .build();
if (features.isEnabled("dark-mode", ctx)) {
    // Use dark mode for this user
}
```

### Strategy Examples

```java
// Always on
Feature feature = Feature.builder("feature-a").alwaysOn().build();

// Percentage rollout
Feature feature = Feature.builder("feature-b").percentage(10).build();

// User whitelist
Feature feature = Feature.builder("beta")
    .forUsers("user1", "user2", "user3")
    .build();

// Date range
Feature feature = Feature.builder("promo")
    .strategy(new DateRangeStrategy(startDate, endDate))
    .build();

// Composite strategy
Feature feature = Feature.builder("complex")
    .strategy(CompositeStrategy.allOf(
        new PercentageStrategy(50),
        new UserListStrategy(Set.of("admin"))
    ))
    .build();
```

### Conditional Execution

```java
// Execute if enabled
features.ifEnabled("dark-mode", () -> applyDarkTheme());

// Get value based on feature state
String theme = features.ifEnabled("dark-mode",
    () -> "dark",
    () -> "light"
);
```

### Feature Stores

```java
// In-memory (default)
OpenFeature features = OpenFeature.getInstance();

// File-based persistence
OpenFeature features = OpenFeature.create(new FileFeatureStore(Path.of("features.json")));

// Cached store
OpenFeature features = OpenFeature.create(new CachedFeatureStore(backingStore));

// LRU store
OpenFeature features = OpenFeature.create(new LruFeatureStore(maxSize));
```

## Class Reference

### Root Package (`cloud.opencode.base.feature`)
| Class | Description |
|-------|-------------|
| `OpenFeature` | Main facade class for feature toggle management (singleton) |
| `Feature` | Immutable record representing a feature toggle definition |
| `FeatureContext` | Evaluation context carrying user, tenant, and custom attributes |

### Annotation Package (`cloud.opencode.base.feature.annotation`)
| Class | Description |
|-------|-------------|
| `FeatureToggle` | Annotation to mark methods guarded by a feature flag |
| `FeatureVariant` | Annotation for variant-specific method implementations |

### Audit Package (`cloud.opencode.base.feature.audit`)
| Class | Description |
|-------|-------------|
| `FeatureAuditEvent` | Record capturing feature state change audit data |
| `FileAuditLogger` | Audit logger that writes events to a file |
| `MetricsFeatureListener` | Listener that collects feature usage metrics |

### Exception Package (`cloud.opencode.base.feature.exception`)
| Class | Description |
|-------|-------------|
| `FeatureException` | Base exception for feature operations |
| `FeatureConfigException` | Exception for feature configuration errors |
| `FeatureNotFoundException` | Exception when a feature key is not found |
| `FeatureSecurityException` | Exception for feature security violations |
| `FeatureStoreException` | Exception for feature store I/O errors |
| `FeatureErrorCode` | Enumeration of feature error codes |

### Listener Package (`cloud.opencode.base.feature.listener`)
| Class | Description |
|-------|-------------|
| `FeatureListener` | Interface for receiving feature state change notifications |

### Proxy Package (`cloud.opencode.base.feature.proxy`)
| Class | Description |
|-------|-------------|
| `FeatureProxy` | Dynamic proxy for routing calls based on feature state |
| `VariantRouter` | Routes to different implementations based on feature variants |

### Security Package (`cloud.opencode.base.feature.security`)
| Class | Description |
|-------|-------------|
| `AuditLogger` | Interface for audit logging of feature operations |
| `SecureFeatureManager` | Feature manager with permission-based access control |

### Store Package (`cloud.opencode.base.feature.store`)
| Class | Description |
|-------|-------------|
| `FeatureStore` | Interface for feature persistence backends |
| `InMemoryFeatureStore` | Thread-safe in-memory store (default) |
| `FileFeatureStore` | File-based JSON persistent store |
| `CachedFeatureStore` | Caching decorator over any backing store |
| `LruFeatureStore` | LRU-eviction bounded in-memory store |
| `RedisFeatureStore` | Redis-backed distributed store |

### Strategy Package (`cloud.opencode.base.feature.strategy`)
| Class | Description |
|-------|-------------|
| `EnableStrategy` | Interface for feature activation strategies |
| `AlwaysOnStrategy` | Always returns enabled |
| `AlwaysOffStrategy` | Always returns disabled |
| `PercentageStrategy` | Random percentage-based rollout |
| `ConsistentPercentageStrategy` | Deterministic hash-based percentage rollout |
| `UserListStrategy` | Whitelist of specific user IDs |
| `TenantAwareStrategy` | Per-tenant feature management |
| `DateRangeStrategy` | Time-window-based activation |
| `ExpressionStrategy` | Dynamic rule evaluation using expression engine |
| `CompositeStrategy` | Combines multiple strategies with AND/OR logic |

## Requirements

- Java 25+ (uses records, sealed interfaces, virtual threads)
- No external dependencies for core functionality

## Optional Dependencies

- `opencode-base-cache` - For cached feature store
- `opencode-base-expression` - For expression-based strategies

## License

Apache License 2.0

## Author

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
