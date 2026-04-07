# OpenCode Base I18n

Internationalization message solution for JDK 25+. Provides a unified facade for message retrieval, ICU-like formatting (plural/select/number/date), type-safe message keys, locale fallback chains, and bundle validation — with no external dependencies and no Spring required.

## Maven Dependency

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-i18n</artifactId>
    <version>1.0.3</version>
</dependency>
```

## Features

- **ICU-like message formatting** — `{name}`, `{count, plural, one{# item} other{# items}}`, `{gender, select, male{He} other{They}}`, `{amount, number, #,##0.00}`, `{date, date, yyyy-MM-dd}` — no single-quote escaping quirks
- **CLDR plural rules** — 50+ languages without ICU4J dependency (East Asian, Slavic, Arabic, Welsh, Irish, …)
- **Type-safe message keys** — `I18nKey` interface and `I18nEnum` for compile-time key safety with auto-derived dot-case keys
- **Custom locale fallback chains** — `ChainedLocaleFallback` with `Builder`: pt-BR → pt-PT → es → en
- **Bundle validation** — `BundleValidator` detects missing/extra keys and reports coverage percentage
- **Missing key hooks** — pluggable `MissingKeyHandler` (no-op / logging / collecting / composed)
- **Multiple message providers** — ResourceBundle, caching decorator, chaining, reloadable hot-reload
- **ThreadLocal locale management** with scoped execution (`withLocale`)
- **Pluggable SPI** — `LocaleResolver`, `MessageFormatter`, `MessageProvider`, `MessageBundleProvider`, `LocaleFallbackStrategy`
- **Thread-safe global configuration**
- No Spring, no ICU4J, no external runtime dependencies

## Class Reference

### Core

| Class | Description |
|-------|-------------|
| `OpenI18n` | Main facade: message retrieval, locale management, global configuration, missing-key handler |
| `MessageSource` | Interface for internationalized message retrieval with locale support |
| `MessageBundle` | Interface for a locale-specific collection of messages |
| `LocaleContext` | Immutable record encapsulating Locale and TimeZone |

### Formatting

| Class | Description |
|-------|-------------|
| `IcuLikeFormatter` | ICU-style formatter: named/positional params, plural, select, number, date — no single-quote escaping |
| `DefaultMessageFormatter` | Default formatter using `java.text.MessageFormat` for positional parameters |
| `NamedParameterFormatter` | Formatter supporting `${name}` named parameters |

### Plural Support

| Class | Description |
|-------|-------------|
| `PluralRules` | CLDR plural rules for 50+ languages; no ICU4J dependency |
| `PluralCategory` | Enum: ZERO / ONE / TWO / FEW / MANY / OTHER |
| `PluralFormatter` | Parses `{count, plural, one{…} other{…}}` branches and substitutes `#` |
| `SelectFormatter` | Parses `{gender, select, male{…} other{…}}` branches |

### Type-Safe Keys

| Class | Description |
|-------|-------------|
| `I18nKey` | Interface with `key()` + default `get(…)` / `getOrDefault(…)` helpers |
| `I18nEnum` | Extends `I18nKey`; auto-derives dot-case key from class and enum name |
| `I18nMessage` | Immutable record: resolved key + locale + formatted text + params |

### Locale Fallback

| Class | Description |
|-------|-------------|
| `LocaleFallbackStrategy` | Functional interface: given a locale, return ordered fallback chain |
| `ChainedLocaleFallback` | Builder-based strategy: exact → language → default locale |

### Bundle Validation

| Class | Description |
|-------|-------------|
| `BundleValidator` | Compares key sets across locales: missing keys, extra keys, coverage% |
| `BundleValidationResult` | Immutable record: missingKeys, extraKeys, coverage(), isComplete(), summary() |

### Missing Key Handling

| Class | Description |
|-------|-------------|
| `MissingKeyHandler` | `@FunctionalInterface`; factories: `noOp()`, `logging()`, `collecting()`, `andThen()` |
| `CollectingMissingKeyHandler` | Thread-safe collector; useful in tests and development |

### Providers

| Class | Description |
|-------|-------------|
| `ResourceBundleProvider` | Provider backed by Java ResourceBundle `.properties` files |
| `CachingMessageProvider` | Caching decorator for any `MessageProvider` |
| `ChainMessageProvider` | Chains multiple providers with fallback |
| `ReloadableMessageProvider` | Hot-reload capable provider |

### Resolvers

| Class | Description |
|-------|-------------|
| `AcceptHeaderLocaleResolver` | Resolves locale from HTTP `Accept-Language` headers |
| `CompositeLocaleResolver` | Combines multiple resolvers with priority-based fallback |
| `FixedLocaleResolver` | Always returns a fixed, pre-configured locale |
| `ThreadLocalLocaleResolver` | Per-thread locale management via ThreadLocal |

### SPI

| Interface | Description |
|-----------|-------------|
| `LocaleResolver` | SPI for locale resolution strategies |
| `MessageFormatter` | SPI for message formatting |
| `MessageProvider` | SPI for loading messages from any data source |
| `MessageBundleProvider` | SPI for ServiceLoader-based message bundle discovery |
| `LocaleFallbackStrategy` | SPI for custom locale fallback chains |

### Exceptions

| Class | Description |
|-------|-------------|
| `OpenI18nException` | General i18n exception; factory methods `formatError()`, `parseError()` |
| `OpenNoSuchMessageException` | Thrown when a requested message key is not found |

## Quick Start

### Basic Usage

```java
// Set up a ResourceBundle provider
OpenI18n.setMessageProvider(new ResourceBundleProvider("i18n/messages"));
OpenI18n.setDefaultLocale(Locale.ENGLISH);

// Simple message retrieval
String msg = OpenI18n.get("user.welcome", "John");

// With specific locale
String frMsg = OpenI18n.get("user.welcome", Locale.FRENCH, "Jean");

// Named parameters
String msg = OpenI18n.get("order.confirmation", Map.of(
    "orderId", "12345",
    "total",   99.99
));

// Default value fallback
String msg = OpenI18n.getOrDefault("unknown.key", "Default Message");

// Scoped locale execution
OpenI18n.withLocale(Locale.JAPANESE, () -> {
    System.out.println(OpenI18n.get("greeting"));
});

// Check message existence
if (OpenI18n.contains("error.notFound")) {
    // handle
}
```

### ICU-like Formatting (No Single-Quote Escaping)

```java
// Named parameters — message template: "Hello, {name}! You have {count} messages."
String msg = OpenI18n.get("inbox.summary", Map.of("name", "Alice", "count", 3));

// Plural — template: "{count, plural, one{# message} other{# messages}}"
// count=1 → "1 message",  count=5 → "5 messages"
String msg = OpenI18n.get("inbox.count", Map.of("count", 1));

// Exact match — template: "{count, plural, =0{No messages} one{# message} other{# messages}}"
String msg = OpenI18n.get("inbox.count", Map.of("count", 0));

// Select — template: "{gender, select, male{He liked} female{She liked} other{They liked}} your post."
String msg = OpenI18n.get("reaction", Map.of("gender", "female"));

// Number format — template: "Total: {amount, number, #,##0.00}"
String msg = OpenI18n.get("order.total", Map.of("amount", 1234567.89));

// Date format — template: "Expires: {date, date, yyyy-MM-dd}"
String msg = OpenI18n.get("license.expiry", Map.of("date", LocalDate.now()));
```

### Type-Safe Keys with I18nEnum

```java
// Define type-safe keys
public enum Messages implements I18nEnum {
    USER_WELCOME,      // → key: "messages.user.welcome"
    ORDER_CONFIRMATION // → key: "messages.order.confirmation"
}

// Use the enum directly
String msg = Messages.USER_WELCOME.get("John");
String msg = Messages.ORDER_CONFIRMATION.get(Locale.FRENCH, Map.of("orderId", "123"));
String safe = Messages.USER_WELCOME.getOrDefault("Guest");
```

### Locale Fallback Chain

```java
// pt-BR → pt-PT → es → en
LocaleFallbackStrategy strategy = ChainedLocaleFallback.builder()
    .chain(Locale.forLanguageTag("pt-BR"),
        Locale.forLanguageTag("pt-PT"),
        Locale.forLanguageTag("es"),
        Locale.ENGLISH)
    .ultimateFallback(Locale.ENGLISH)
    .build();

OpenI18n.setFallbackStrategy(strategy);
```

### Bundle Validation

```java
BundleValidator validator = new BundleValidator(provider);

// Validate French against English
BundleValidationResult result = validator.validate(Locale.ENGLISH, Locale.FRENCH);
System.out.println(result.summary());
// → "[en → fr] coverage=85.0% missing=3 extra=0"

if (!result.isComplete()) {
    result.missingKeys().forEach(k -> System.out.println("Missing: " + k));
}

// Validate all supported locales at once
Map<Locale, BundleValidationResult> all = validator.validateAll(Locale.ENGLISH);
all.forEach((locale, r) -> System.out.printf("%s: %.0f%%\n", locale, r.coverage() * 100));
```

### Missing Key Handler

```java
// Collect missing keys during tests / development
CollectingMissingKeyHandler collector = MissingKeyHandler.collecting();
OpenI18n.setMissingKeyHandler(collector);

// ... run your application or tests ...

if (!collector.isEmpty()) {
    System.out.println("Missing keys: " + collector.getMissingKeys());
}

// Log missing keys in production
OpenI18n.setMissingKeyHandler(MissingKeyHandler.logging());

// Compose handlers
MissingKeyHandler combined = MissingKeyHandler.logging()
    .andThen(collector);
OpenI18n.setMissingKeyHandler(combined);
```

### CLDR Plural Rules

```java
// Programmatic plural selection (without full message pipeline)
PluralRules rules = PluralRules.forLocale(Locale.forLanguageTag("ru"));
PluralCategory cat = rules.select(21);  // → ONE (21 day → "день")
PluralCategory cat = rules.select(12);  // → MANY (12 days → "дней")
```

## Requirements

- Java 25+

## License

Apache License 2.0
