# OpenCode Base String

**Comprehensive string processing library for Java 25+**

`opencode-base-string` is a full-featured string manipulation library providing naming conversion, template engine, similarity algorithms, fuzzy matching, data desensitization, regex utilities, and more.

## Features

### Core Features
- **String Operations**: Padding, truncation, case conversion, reverse, shuffle, wrap/unwrap
- **Null-safe Checks**: `isBlank()`, `isEmpty()`, `isNotBlank()`, `isNotEmpty()` (V1.0.3)
- **Batch Matching**: `containsAny()`, `startsWithAny()`, `endsWithAny()`, ignore-case variants (V1.0.3)
- **Single-pass Multi-replace**: `replaceEach()` — one scan, no recursive replacement (V1.0.3)
- **SLF4J-style Format**: `format("{} has {} items", name, count)` (V1.0.3)
- **Naming Conversion**: camelCase, PascalCase, snake_case, kebab-case, SCREAMING_SNAKE_CASE
- **Template Engine**: Variable interpolation, if/for/include nodes, custom filters
- **Similarity Algorithms**: Levenshtein distance (+ bounded), Jaccard, Cosine, Jaro-Winkler
- **Fuzzy Matching**: Aho-Corasick multi-pattern matching, fuzzy search with scoring

### Advanced Features
- **Grapheme Cluster Operations**: Emoji-safe length, substring, reverse, display width (V1.0.3)
- **URL Slug Generation**: NFD normalization, accent stripping, configurable separator (V1.0.3)
- **Data Desensitization**: Annotation-driven masking for phone, email, ID card, bank card, etc.
- **Jackson Integration**: Custom serializer for transparent JSON desensitization
- **Regex Utilities**: Precompiled patterns, common validation (email, phone, IP, URL)
- **String Diff**: Line-by-line diff comparison with unified diff output
- **Escape Utilities**: HTML, Java, SQL escape/unescape
- **Unicode Support**: Chinese detection, full-width/half-width conversion, Chinese segmentation
- **Format Utilities**: Duration, file size, number formatting
- **CSV/Tokenizer**: CSV parsing and configurable string tokenization
- **Codec**: Key-value string encoding/decoding

## Quick Start

### Maven Dependency
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-string</artifactId>
    <version>1.0.3</version>
</dependency>
```

### Basic Usage

```java
import cloud.opencode.base.string.*;
import cloud.opencode.base.string.similarity.LevenshteinDistance;
import cloud.opencode.base.string.unicode.OpenGrapheme;

// Padding and truncation
String padded = OpenString.padLeft("42", 5, '0');     // "00042"
String truncated = OpenString.truncate("Hello World", 8); // "Hello..."

// Case conversion
String title = OpenString.toTitleCase("hello world");  // "Hello World"
String swapped = OpenString.swapCase("Hello");         // "hELLO"

// Naming conversion
String camel = OpenNaming.toCamelCase("user_name");    // "userName"
String snake = OpenNaming.toSnakeCase("userName");     // "user_name"

// Template engine
String result = OpenTemplate.render("Hello, ${name}!", Map.of("name", "World"));

// SLF4J-style format (V1.0.3)
String msg = OpenString.format("{} has {} items", "Alice", 3); // "Alice has 3 items"

// Null-safe checks (V1.0.3)
boolean blank = OpenString.isBlank(null);    // true
boolean has = OpenString.containsAny("abc", "x", "b"); // true

// Single-pass multi-replace (V1.0.3)
String replaced = OpenString.replaceEach("aabbcc",
    new String[]{"aa", "bb"}, new String[]{"11", "22"}); // "1122cc"

// Grapheme-aware operations (V1.0.3)
int len = OpenGrapheme.length("a👨‍👩‍👧‍👦b");    // 3 (not 11)
String rev = OpenGrapheme.reverse("a👨‍👩‍👧‍👦b"); // "b👨‍👩‍👧‍👦a"
int width = OpenGrapheme.displayWidth("Hi你好");  // 6

// Split and join (V1.0.3)
List<String> parts = OpenString.split("a,b,c", ",");        // ["a", "b", "c"]
String joined = OpenString.joinSkipBlanks(", ", "a", "", "b"); // "a, b"

// Abbreviation (V1.0.3)
String abbr = OpenString.abbreviate("Hello World Test String", 15); // "Hello World..."

// URL slug generation (V1.0.3)
String slug = OpenSlug.toSlug("Hello World!");    // "hello-world"

// Similarity
double score = OpenSimilarity.levenshtein("kitten", "sitting");

// Bounded Levenshtein distance (V1.0.3) — early exit if distance > threshold
int dist = LevenshteinDistance.boundedDistance("kitten", "sitting", 5); // 3
int far  = LevenshteinDistance.boundedDistance("abc", "xyz", 1);       // -1 (exceeded)

// Desensitization
String masked = OpenMask.maskPhone("13812345678");     // "138****5678"
String email = OpenMask.maskEmail("test@example.com"); // "t***t@example.com"
```

## Class Reference

### Root Package (`cloud.opencode.base.string`)
| Class | Description |
|-------|-------------|
| `OpenString` | Core string operations facade: padding, truncation, case conversion, extraction, cleaning, validation, null-safe checks, batch matching, replaceEach, SLF4J format, split/join |
| `OpenNaming` | Naming convention conversion: camelCase, snake_case, kebab-case, PascalCase |
| `OpenSlug` | URL-friendly slug generation with accent stripping and configurable separator |
| `OpenTemplate` | Simple template rendering with variable substitution |

### Abbreviation (`string.abbr`)
| Class | Description |
|-------|-------------|
| `OpenAbbreviation` | String abbreviation and shortening utilities |

### Builder (`string.builder`)
| Class | Description |
|-------|-------------|
| `CharMatcher` | Character matching predicates for filtering and testing |
| `Joiner` | Configurable string joining with separator, prefix, suffix |
| `Splitter` | Configurable string splitting with trimming, empty filtering |

### Codec (`string.codec`)
| Class | Description |
|-------|-------------|
| `KeyValueCodec` | Encode/decode key-value pairs to/from string representation |

### Desensitization (`string.desensitize`)
| Class | Description |
|-------|-------------|
| `DesensitizeProcessor` | Core processor for applying desensitization rules |
| `OpenMask` | Facade for common masking operations (phone, email, ID card, etc.) |
| `@Desensitize` | Field-level annotation to specify desensitization type |
| `@DesensitizeBean` | Class-level annotation to enable bean desensitization |
| `DesensitizeException` | Exception for desensitization errors |
| `CollectionHandler` | Handler for desensitizing collection fields |
| `NumberHandler` | Handler for desensitizing numeric fields |
| `StringHandler` | Handler for desensitizing string fields |
| `DesensitizeModule` | Jackson module for transparent JSON desensitization |
| `DesensitizeSerializer` | Jackson serializer for desensitized fields |
| `DesensitizeStrategy` | Interface for custom desensitization strategies |
| `DesensitizeType` | Enum of built-in desensitization types |
| `MaskPattern` | Configurable mask pattern definition |
| `StrategyRegistry` | Registry for desensitization strategies |

### Diff (`string.diff`)
| Class | Description |
|-------|-------------|
| `DiffLine` | Represents a single diff line with change type |
| `DiffResult` | Result of a diff comparison containing all diff lines |
| `OpenDiff` | Facade for string diff operations |

### Escape (`string.escape`)
| Class | Description |
|-------|-------------|
| `HtmlUtil` | HTML escape and unescape utilities |
| `JavaUtil` | Java string escape and unescape utilities |
| `OpenEscape` | Unified escape facade for HTML, Java, SQL |
| `SqlUtil` | SQL escape utilities to prevent injection |

### Exception (`string.exception`)
| Class | Description |
|-------|-------------|
| `OpenStringException` | Base exception for string module errors |

### Format (`string.format`)
| Class | Description |
|-------|-------------|
| `OpenDuration` | Human-readable duration formatting |
| `OpenFileSize` | Human-readable file size formatting (KB, MB, GB) |
| `OpenFormat` | General-purpose string formatting utilities |
| `OpenNumberFormat` | Number formatting with locale support |

### Match (`string.match`)
| Class | Description |
|-------|-------------|
| `AhoCorasick` | Aho-Corasick algorithm for multi-pattern string matching |
| `FuzzyMatch` | Fuzzy match result record |
| `FuzzyMatcher` | Configurable fuzzy string matcher |
| `OpenFuzzyMatch` | Facade for fuzzy matching operations |
| `PatternMatch` | Pattern-based string matching utilities |

### Naming (`string.naming`)
| Class | Description |
|-------|-------------|
| `CaseUtil` | Low-level case conversion utilities |
| `NamingCase` | Enum of naming conventions (CAMEL, SNAKE, KEBAB, etc.) |
| `WordUtil` | Word-level string operations (split, join by convention) |

### Parse (`string.parse`)
| Class | Description |
|-------|-------------|
| `CsvUtil` | CSV parsing and generation utilities |
| `OpenParse` | Facade for string parsing operations |
| `TokenizerUtil` | Configurable string tokenizer |
| `NamedParameterParser` | Named parameter parsing for SQL-style templates |

### Regex (`string.regex`)
| Class | Description |
|-------|-------------|
| `MatcherUtil` | Regex matcher helper utilities |
| `OpenRegex` | Facade for regex operations |
| `OpenVerify` | Common validation patterns (email, phone, IP, URL, etc.) |
| `RegexPattern` | Precompiled common regex patterns |
| `RegexUtil` | Regex compilation and matching utilities |

### Similarity (`string.similarity`)
| Class | Description |
|-------|-------------|
| `CosineSimilarity` | Cosine similarity algorithm for string comparison |
| `JaccardSimilarity` | Jaccard similarity coefficient for string comparison |
| `LevenshteinDistance` | Levenshtein edit distance algorithm |
| `OpenSimilarity` | Facade for string similarity algorithms |

### Template (`string.template`)
| Class | Description |
|-------|-------------|
| `ContextBuilder` | Builder for template context variables |
| `PlaceholderTemplate` | Simple placeholder-based template |
| `StringTemplate` | Advanced string template with expressions |
| `Template` | Template interface |
| `TemplateContext` | Template execution context |
| `TemplateEngine` | Full-featured template engine with parsing and rendering |
| `TemplateFilter` | Template output filter interface |
| `TemplateUtil` | Template utility methods |
| `ForNode` | Template AST node for for-loops |
| `IfNode` | Template AST node for conditionals |
| `IncludeNode` | Template AST node for includes |
| `TemplateNode` | Base template AST node |
| `TextNode` | Template AST node for static text |
| `VariableNode` | Template AST node for variable interpolation |

### Text (`string.text`)
| Class | Description |
|-------|-------------|
| `OpenHighlight` | Text highlighting with search term marking |
| `OpenText` | Text processing utilities |
| `OpenTruncate` | Advanced text truncation strategies |
| `OpenWrap` | Text wrapping and line-breaking utilities |

### Unicode (`string.unicode`)
| Class | Description |
|-------|-------------|
| `ChineseSegmenter` | Basic Chinese text segmentation |
| `OpenChinese` | Chinese character detection and conversion |
| `OpenFullWidth` | Full-width/half-width character conversion |
| `OpenUnicode` | Unicode character utilities |
| `OpenGrapheme` | Grapheme cluster operations: emoji-safe length, substring, reverse, display width |

## Requirements

- Java 25+
- No external dependencies for core functionality
- Optional: `com.fasterxml.jackson.core:jackson-databind` for Jackson desensitization integration

## License

Apache License 2.0

## Author

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
