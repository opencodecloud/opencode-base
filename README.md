# OpenCode Base

[![Java](https://img.shields.io/badge/Java-25%2B-blue)](https://openjdk.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green)](https://www.apache.org/licenses/LICENSE-2.0)
[![Build](https://img.shields.io/badge/Build-Maven-orange)](https://maven.apache.org/)

**A modern, zero-dependency Java utility library for JDK 25+**

OpenCode Base is a comprehensive toolkit of 45 modular components covering core utilities, data processing, security, concurrency, machine learning inference, and business logic — all built on modern Java features including virtual threads, records, sealed classes, and pattern matching.

## Highlights

- **Zero Dependencies** — Core modules require no third-party libraries
- **JDK 25+ Native** — Virtual threads, records, sealed classes, pattern matching
- **JPMS Modular** — Full Java module system support
- **Production Ready** — 14 rounds of deep security audits, 338+ issues fixed
- **45 Modules** — Pick only what you need, each module is independently usable
- **Bilingual Javadoc** — English + Chinese documentation

> **[中文文档 README_zh.md](README_zh.md)**

## Quick Start

### Maven

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-core</artifactId>
    <version>1.0.3</version>
</dependency>
```

Add more modules as needed:

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-json</artifactId>
    <version>1.0.3</version>
</dependency>
```

### Usage

All facade classes start with `Open` — type `Open` in your IDE for auto-completion:

```java
// Core utilities
String name = OpenObject.defaultIfNull(user.getName(), "unknown");

// JSON (zero-dependency, built-in POJO mapping)
String json = OpenJson.toJson(user);
User parsed = OpenJson.fromJson(json, User.class);

// Cache (LRU/LFU/W-TinyLFU)
Cache<String, User> cache = OpenCache.getOrCreate("users", c -> c
    .maximumSize(10000).expireAfterWrite(Duration.ofMinutes(30)));

// Crypto (AES-GCM, Argon2, JWT, PGP)
byte[] encrypted = OpenCrypto.aesGcm().setKey(key).encrypt("secret");
String hash = OpenCrypto.argon2().hash("password123");

// Cron (L/W/#, OR semantics, describe)
CronExpression cron = OpenCron.parse("0 9 * * MON-FRI");
ZonedDateTime next = cron.nextExecution(ZonedDateTime.now());
String desc = cron.describe(); // "At 09:00, Monday through Friday"

// ID generation (Snowflake, NanoID, UUID)
long id = OpenId.snowflake().nextId();

// Parallel (virtual threads)
List<Result> results = OpenParallel.parallelMap(items, this::process);
```

## Architecture

```
opencode-base (45 modules)
┌─────────────────────────────────────────────────────────────────┐
│                    Business Components (3xx) — 15               │
│  Captcha  Email  Event  Feature  Geo  Graph  Image  Lunar       │
│  Money  Test  TimeSeries  Tree  Web  Pdf  Cron                  │
├─────────────────────────────────────────────────────────────────┤
│                    Domain Components (2xx) — 12                  │
│  Config  Csv  Functional  Json  Log  Parallel  Pool              │
│  Serialization  Xml  Yml  OAuth2  Neural                         │
├─────────────────────────────────────────────────────────────────┤
│                    Foundation Components (1xx) — 17              │
│  Cache  ClassLoader  Collections  Crypto  Date  DeepClone  Hash │
│  I18n  ID  IO  Math  Reflect  String  Expression  Lock  Rules   │
│  Observability                                                   │
├─────────────────────────────────────────────────────────────────┤
│                    Core (0xx) — 1                                │
│                          Core                                    │
└─────────────────────────────────────────────────────────────────┘
```

## Module Details

### Core (0xx)

#### core
Base utilities and unified exception hierarchy. Object/Array/Math/Hex/Base64 utilities, type conversion (`Convert`), tuples (`Pair`, `Triple`), builder pattern (`OpenBuilder`), preconditions (`Preconditions`), primitive type wrappers (`Ints`, `Longs`, `Bytes`), checked functional interfaces, thread utilities, SPI loader.

### Foundation (1xx)

#### cache
High-performance local cache. LRU/LFU/W-TinyLFU/FIFO eviction policies, TTL/TTI expiration, async API, BloomFilter/SingleFlight protection (`ProtectedCache`), refresh-ahead (`RefreshAheadCache`), copy-on-read, cache warming, reactive API (JDK Flow), resilient loading (retry/circuit breaker/bulkhead/timeout), sampling statistics, virtual thread support.

#### classloader
Class loading and scanning. Isolated class loaders, hot-swap support, package/annotation scanning, class metadata reading, resource loading (`OpenResource`), bytecode analysis.

#### collections
Advanced collection utilities. `BiMap`, `Multiset`, `Multimap`, `Table`, `FluentIterable`, immutable collections, primitive collections (`IntList`, `LongList`), `SkipList`, `BitArray`, partition/group/zip/flatten operations, `ListUtil`, `MapUtil`, `SetUtil`.

#### crypto
Comprehensive cryptography. AES-CBC/GCM, ChaCha20-Poly1305, SM4, RSA, SM2, Ed25519, ECDSA, X25519/X448 key exchange, Argon2/BCrypt/SCrypt password hashing, SHA/SM3/HMAC digests, JWT creation/validation, PGP encryption/signing, certificate chain validation, secure random utilities.

#### date
Date and time utilities. Formatting/parsing, workday calculation, quarter/week operations, time ranges, `StopWatch`, relative time ("3 hours ago"), `TemporalUtil`, date validators, `DateFormatter` with cached patterns.

#### deepclone
Object deep cloning. Reflection-based, serialization-based, and Unsafe-based strategies. Handles circular references, immutable object skipping, custom clone strategies.

#### hash
Non-cryptographic hashing. MurmurHash3 (32/128-bit), xxHash (32/64-bit), CRC32, `BloomFilter` (with builder), consistent hashing (`ConsistentHash`), `SimHash` (text similarity), `HashCode` utilities.

#### i18n
Internationalization. Multi-locale message loading, `NamedParameterFormatter`, reloadable message providers, composite locale resolvers, message source SPI.

#### id
Distributed ID generation. Snowflake (configurable epoch/bits), UUID (v4/v7), NanoID, segment-based (`SegmentIdGenerator` with buffer preload), timestamp-based, composite ID strategies.

#### io
File and stream utilities. File read/write, path operations, file watching (`FileWatcher`), chunked file processing with progress callback, temporary file management, `StreamUtil`, serialization helpers, resource loading.

#### reflect
Reflection utilities. Type-safe field/method access, `TypeToken` for generic types, Lambda metadata extraction, property descriptors, Record utilities, `ReflectCache` with LRU caching, annotation scanning, `BeanDiff` for object comparison.

#### string
String processing. Naming conversion (`OpenNaming`: camelCase/snake_case/kebab-case/PascalCase), template engine, similarity algorithms (Levenshtein, Jaro-Winkler, cosine), data masking (`OpenMask`: phone/email/ID card), regex utilities (`RegexPattern`, `OpenVerify`), `AhoCorasick` multi-pattern matching, `NamedParameterParser`, Base62/Base58 codecs.

#### expression
Expression evaluation engine. Arithmetic/comparison/logical/ternary operators, property access, method calls, collection operations (`in`, `contains`), lambda expressions, sandbox execution with resource limits, compiled expression caching, custom function registry.

#### lock
Lock abstraction. `LocalLock` (ReentrantLock wrapper), `SpinLock` (busy-wait for nanosecond operations), `ReadWriteLock`, named lock factory (`NamedLockFactory`), lock groups (`LockGroup` with deadlock prevention), fencing tokens, configurable timeout/reentrant behavior.

#### rules
Lightweight rule engine. Condition-action rules with priority, rule groups, decision tables (CSV/in-memory), DSL builder, hot reload, rule listeners, composite conditions (AND/OR/NOT), `RuleEngine` with fire-first/fire-all modes.

#### math
Scientific computing library. Linear algebra (`Vector`, `Matrix`), probability distributions (Normal, T, Chi-Squared, F, Beta, Gamma, LogNormal, Binomial), numerical analysis (Brent's root-finding, Simpson's integration, differentiation), interpolation, combinatorics (binomial coefficients, permutations), special functions (gamma, beta, erf), descriptive statistics with streaming accumulator, hypothesis testing (T-test, Chi-Square, ANOVA), regression, Spearman/Kendall correlation.

#### observability
Tracing and diagnostics. `OpenTelemetryTracer` with span management, `SlowLogCollector` (bounded, thread-safe, configurable threshold), noop tracer for zero-overhead disabled tracing.

### Domain (2xx)

#### config
Configuration management. Multi-source (Properties/YAML/JSON/environment variables/system properties), hot reload with change listeners, type-safe binding, placeholder resolution, profile support, hierarchical config merging, config validation.

#### csv
CSV processing. RFC 4180 compliant parser/writer with DoS and formula-injection protection. Object binding (CSV ↔ Record/POJO via `@CsvColumn`/`@CsvFormat` annotations), streaming `CsvReader`/`CsvWriter` for low-memory processing, document diff, merge (concat/inner join/left join), fluent query/filter/projection, column statistics, row sampling, split into chunks, transformation pipeline, rule-based validation.

#### functional
Functional programming. `Try` (exception-safe computation), `Either` (left/right), `Option` (null-safe), `Lazy` (deferred evaluation), `Pipeline` (data transformation chain), `Lens` (immutable update), `For` (monadic comprehension), `Future` (async), pattern matching utilities.

#### json
JSON processing. Zero-dependency built-in parser/serializer with full POJO mapping (reflection-based `BeanMapper`), `JsonNode` tree model (sealed interface), streaming `JsonReader`/`JsonWriter`, JSON Pointer (RFC 6901), JSONPath, JSON Patch (RFC 6902), Merge Patch (RFC 7396), JSON Schema validation, JSON Diff, `@JsonProperty`/`@JsonIgnore`/`@JsonFormat` annotations, reactive streams, SPI for Jackson/Gson/Fastjson2 integration.

#### log
Logging facade. `OpenLog` with MDC/NDC context propagation, sampled logging (`SampledLog`), conditional logging, audit logging (`AuditLog`), virtual thread context support, structured JSON logging, sensitive data masking.

#### parallel
Parallel computing. Virtual-thread-based `BatchProcessor`, `parallelMap`/`parallelFilter`, `StructuredScope` (structured concurrency), `AsyncPipeline`, `ScheduledScope`, `RateLimitedExecutor` (token bucket), `CpuBound` executor, `HybridExecutor`, deadline-aware execution.

#### pool
Object pooling. `GenericObjectPool` with create/validate/destroy lifecycle, auto-scaling (min idle/max size), health check, FIFO/LIFO borrowing, `ThreadLocalPool`, pool statistics, idle object eviction.

#### serialization
Serialization abstraction. Unified `Serializer` SPI, JSON/binary/Protobuf support, compressed serialization (GZIP/LZ4/ZSTD), schema evolution, type registry, `SerializerFactory` with auto-discovery.

#### xml
XML processing. DOM/SAX/StAX parsing, XPath queries, Schema validation, XSLT transformation, object binding (XML ↔ Bean), `StaxWriter`/`StaxReader` streaming, XXE protection (`XmlSecurity`), namespace support, pretty printing.

#### yml
YAML processing. Multi-document parsing, anchor/alias support, merge keys, type-safe value access, `PlaceholderResolver`, safe loading with depth/size/alias limits (`YmlSafeLoader`), SPI-based parser.

#### neural
Neural network inference engine. Custom `.ocm` binary model format with computation graph. `Tensor` (float32, stride-based, zero-copy views), ~30 operators (Conv1D/2D, DepthwiseConv2D, MaxPool/AvgPool, LSTM, BiLSTM, CTC decode/beam-search, BatchNorm, Dropout, Linear, all standard activations), `InferenceSession` for model execution, loss functions (MSE, MAE, CrossEntropy, Huber, CosineSimilarity), evaluation metrics (Accuracy, Precision, Recall, F1, ConfusionMatrix), input normalization (MinMax, Z-Score, L2), weight initialization, quantization support.

#### oauth2
OAuth2/OIDC client. Authorization Code flow, Client Credentials, Device Code, PKCE (RFC 7636), auto token refresh, OIDC discovery, provider presets (Google, GitHub, Microsoft), `FileTokenStore`, constant-time token comparison.

### Business (3xx)

#### captcha
CAPTCHA generation. Numeric, arithmetic, Chinese character, GIF animation types. Configurable font/size/noise/interference lines, `MemoryCaptchaStore` with TTL, pluggable store SPI, Base64 image output.

#### email
Email utilities. SMTP/SMTPS sending with HTML body, attachments, inline images. Template rendering. IMAP/POP3 receiving with folder management. IDLE push notifications. Retry and rate limiting.

#### event
Event-driven architecture. In-process event bus with sync/async dispatch (virtual threads), type-safe events, event filtering, priority listeners, `Saga` orchestration (compensating transactions), event store (`InMemoryEventStore`), dead letter handling, event monitoring.

#### feature
Feature toggles. Percentage-based rollout, user whitelist/blacklist, date range activation, A/B testing with metric collection, `FeatureProxy` (annotation-based), cached feature store, JSON/in-memory providers.

#### geo
Geographic utilities. Distance calculation (Haversine, Vincenty), geofence (polygon/circle/rectangle), GeoHash encoding/decoding with neighbor search, coordinate validation, coordinate system conversion (WGS84/GCJ02/BD09), bounding box queries, area management.

#### graph
Graph data structures. Directed/undirected weighted graphs, BFS/DFS traversal, Dijkstra/Bellman-Ford/A* shortest path, topological sort (Kahn's algorithm), Prim/Kruskal MST, cycle detection, connected components, graph serialization.

#### image
Image processing. Resize, crop, rotate, flip, watermark (text/image), brightness/contrast adjustment, format conversion (JPEG/PNG/GIF/BMP/WebP), thumbnail generation, EXIF reading, `GifEncoder`, image validation.

#### lunar
Chinese lunar calendar. Solar ↔ lunar date conversion (1900-2100), 24 solar terms, Chinese zodiac (生肖), Heavenly Stems and Earthly Branches (天干地支/干支), traditional festivals, month/day name formatting.

#### money
Money and currency. Precise `Money` type (BigDecimal-based), multi-currency with `Currency` enum, allocation (`AllocationUtil`: ratio/percentage/weight/round-robin), Chinese uppercase conversion (壹佰贰拾叁元), formatting, `MoneyCalcUtil` (sum/average/max/min).

#### test
Testing utilities. Fluent assertions (`OpenAssert`), mock proxy, data generation (`Faker`: name/phone/email/address/company), `SensitiveDataGenerator`, `BenchmarkRunner` (warmup/iterations/percentile), `ThreadSafetyChecker`, `Wait` utility, HTTP mock server.

#### timeseries
Time series analysis. Data collection (`TimeSeries`), aggregation (sum/avg/min/max/count by time window), anomaly detection (Z-score, IQR, moving average), trend analysis, seasonal decomposition, data compression (delta/gorilla encoding), correlation analysis, data point validation.

#### tree
Tree data structures. List-to-tree conversion (`ListToTreeConverter`), tree traversal (DFS/BFS), tree search/filter/flatten, AVL tree, red-black tree, skip list, `TreeBuilder` with depth control, `Treeable` interface for custom nodes.

#### web
Web utilities. Unified response `Result<T>` with result codes, `PageResult` for pagination, `RequestContextHolder` (thread-local request context), `ResultCode` enum, SSE (Server-Sent Events) support, cookie utilities, URL builder, request body reading.

#### pdf
PDF processing. Zero-dependency PDF creation (text/tables/images/headers/footers), merge multiple PDFs, split by page range, form field filling, digital signatures, watermarks, page numbering, font embedding, template rendering.

#### cron
Cron expression library. RFC-compliant parser (5/6-field), name aliases (MON-FRI, JAN-DEC), special characters (L/W/#/?), predefined macros (@daily/@yearly), OR semantics for day-of-month/day-of-week, `nextExecution`/`previousExecution`, batch scheduling, human-readable `describe()`, fluent `CronBuilder`, `CronValidator` with interval check.

## Related Projects (opencode-pro)

| Project | Description |
|---------|-------------|
| **OpenData** | Database ORM framework |
| **OpenCache** | Distributed cache framework (Redis/Memcached) |
| **OpenRes** | Resilience framework (circuit breaker, rate limiter, retry) |
| **OpenValidation** | Validation framework (50+ constraint annotations) |
| **OpenSecurity** | Security framework (HSM, Vault, ACME) |
| **OpenNet** | Network framework (HTTP, WebSocket, SSE) |
| **OpenMetrics** | Observability framework (metrics, tracing, health) |
| **OpenTasker** | Task scheduling framework (Cron, distributed) |

## Requirements

- **Java 25+** (virtual threads, sealed interfaces, records, pattern matching)
- **Maven 3.9+** for building

## Building

```bash
# Compile all modules
mvn compile

# Run all tests
mvn test

# Full verify (with coverage check)
mvn verify

# Compile specific modules
mvn compile -pl opencode-base-json,opencode-base-cache -am
```

## Project Statistics

| Metric | Value |
|--------|-------|
| Modules | 45 |
| Source Files | 2,400+ |
| Test Files | 2,100+ |
| Security Audit Rounds | 14 |
| Issues Fixed | 338+ |
| Java Version | 25 |
| External Dependencies | 0 (core) |

## License

[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)

## Author

**Leon Soo** — [OpenCode.cloud](https://opencode.cloud) | [LeonSoo.com](https://leonsoo.com)
