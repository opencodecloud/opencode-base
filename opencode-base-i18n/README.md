# OpenCode Base I18n

Internationalization message solution for JDK 25+. Provides a unified facade for message retrieval, formatting, and locale management with pluggable SPI providers.

## Maven Dependency

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-i18n</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Features

- Simple message retrieval with positional and named parameters
- ThreadLocal-based locale management with scoped execution
- Multiple message providers: ResourceBundle, caching, chaining, reloadable
- Pluggable SPI for locale resolvers, message providers, and formatters
- Accept-Header based locale resolution for web applications
- Composite locale resolver for flexible fallback strategies
- Locale context with timezone support (immutable record)
- ServiceLoader-based auto-discovery of message bundles
- Thread-safe global configuration

## Class Reference

### Core

| Class | Description |
|-------|-------------|
| `OpenI18n` | Main facade class for all i18n operations: message retrieval, locale management, and global configuration |
| `MessageSource` | Interface for internationalized message retrieval with locale support |
| `MessageBundle` | Interface for a locale-specific collection of messages, with key lookup and parent bundle support |
| `LocaleContext` | Immutable record encapsulating Locale and TimeZone for context management |

### Annotations

| Class | Description |
|-------|-------------|
| `@I18nMessage` | Annotation to mark message keys for internationalization |
| `@LocaleParam` | Annotation to mark a method parameter as the locale source |

### Formatters

| Class | Description |
|-------|-------------|
| `DefaultMessageFormatter` | Default message formatter using `java.text.MessageFormat` for positional parameters |
| `NamedParameterFormatter` | Formatter supporting named parameters like `${name}` in message templates |
| `TemplateFormatter` | Template-based message formatter with expression evaluation |

### Providers

| Class | Description |
|-------|-------------|
| `AbstractMessageProvider` | Base class for message provider implementations |
| `CachingMessageProvider` | Decorator that adds caching to any message provider |
| `ChainMessageProvider` | Chains multiple providers with fallback support |
| `ReloadableMessageProvider` | Provider that supports hot-reloading of message files |
| `ResourceBundleProvider` | Provider backed by Java ResourceBundle property files |

### Resolvers

| Class | Description |
|-------|-------------|
| `AcceptHeaderLocaleResolver` | Resolves locale from HTTP Accept-Language headers |
| `CompositeLocaleResolver` | Combines multiple resolvers with priority-based fallback |
| `FixedLocaleResolver` | Always returns a fixed, pre-configured locale |
| `ThreadLocalLocaleResolver` | Resolves locale from ThreadLocal for per-thread locale management |

### SPI

| Class | Description |
|-------|-------------|
| `LocaleResolver` | SPI interface for locale resolution strategies |
| `MessageFormatter` | SPI interface for message formatting with positional and named parameters |
| `MessageProvider` | SPI interface for loading messages from any data source |
| `MessageBundleProvider` | SPI interface for ServiceLoader-based message bundle discovery |

### Support

| Class | Description |
|-------|-------------|
| `LocaleContextHolder` | Holds the current LocaleContext in a ThreadLocal |
| `MessageSourceAccessor` | Convenience accessor for MessageSource with default locale support |
| `ReloadableResourceBundle` | ResourceBundle implementation supporting hot-reload |

### Exceptions

| Class | Description |
|-------|-------------|
| `OpenNoSuchMessageException` | Thrown when a requested message key is not found |

## Quick Start

```java
// Basic message retrieval
String msg = OpenI18n.get("user.welcome", "John");

// Message with specific locale
String chineseMsg = OpenI18n.get("user.welcome", Locale.CHINESE, "Zhang San");

// Message with named parameters
String msg = OpenI18n.get("order.confirmation", Map.of(
    "orderId", "12345",
    "total", 99.99
));

// Default value fallback
String msg = OpenI18n.getOrDefault("unknown.key", "Default Message");

// Scoped locale execution
OpenI18n.withLocale(Locale.JAPANESE, () -> {
    System.out.println(OpenI18n.get("greeting"));
});

// Scoped locale with return value
String result = OpenI18n.withLocale(Locale.FRENCH, () -> {
    return OpenI18n.get("product.name");
});

// Check message existence
if (OpenI18n.contains("error.notFound")) {
    // handle
}

// Configure custom provider
OpenI18n.setMessageProvider(new ResourceBundleProvider("i18n/messages"));
OpenI18n.setDefaultLocale(Locale.ENGLISH);
OpenI18n.setThrowOnMissingMessage(true);
```

## Requirements

- Java 25+

## License

Apache License 2.0
