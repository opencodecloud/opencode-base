# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.3] - 2026-04-02

### Added — opencode-base-captcha

- **CaptchaMetrics** — Lightweight metrics collection for CAPTCHA operations using LongAdder. Tracks generation counts by type, validation success/failure rates, average response times. Immutable `MetricsSnapshot` record for point-in-time captures.
- **CaptchaPool** — Pre-generates CAPTCHAs in a background virtual thread for O(1) retrieval under high load. Builder pattern with configurable pool size (1-10000) and refill threshold. Graceful fallback to real-time generation when pool is empty.
- **CompositeValidator** — Chains multiple `CaptchaValidator` instances with short-circuit semantics. First non-success result stops the chain. Builder and factory method construction.
- **TestCaptchaGenerator** — Generates CAPTCHAs with a deterministic fixed answer for unit testing. Added to sealed `CaptchaGenerator` permits (13 total).
- **HashedCaptchaStore** — Decorator that hashes answers with SHA-256 + random salt before storage. `verifyAnswer()`, `verifyAndRemove()`, `verifyAndRemoveResult()` for single-lookup verification. Defense in depth for storage compromise.
- **HashedCaptchaValidator** — Validator that works with `HashedCaptchaStore`. Auto-detected by `OpenCaptcha.builder()` when a `HashedCaptchaStore` is used.
- **CaptchaEventListener** — Interface for CAPTCHA lifecycle event callbacks (`onGenerated`, `onValidationSuccess`, `onValidationFailure`). Default empty implementations for selective override.
- **CaptchaEventDispatcher** — Thread-safe multi-listener event dispatcher with exception isolation (one listener failure does not affect others). Uses `CopyOnWriteArrayList`.

### Changed — opencode-base-captcha

- **OpenCaptcha** — Builder now supports `.metrics(CaptchaMetrics)`, `.validator(CaptchaValidator)`, `.eventListener(CaptchaEventListener)`. Auto-detects `HashedCaptchaStore` and uses `HashedCaptchaValidator`. `generate()` records metrics and fires events. `validate()` records metrics and fires success/failure events.
- **CaptchaGenerator** — Sealed interface permits updated: +`TestCaptchaGenerator` (12 → 13 implementations).
- **CaptchaStore** — Added `static HashedCaptchaStore hashed(CaptchaStore)` factory method.
- **CaptchaValidator** — Added `static CaptchaValidator composite(...)` and `static CaptchaValidator hashed(HashedCaptchaStore)` factory methods.

### Added — opencode-base-core

- **Result\<T\>** — Sealed `Result` monad with `Success`/`Failure` records. Type-safe error handling via `map`/`flatMap`/`recover`/`recoverWith`. Exceptions in `map` are auto-caught. Pattern matching via `switch` (JDK 25).
- **Either\<L,R\>** — Migrated from `opencode-base-functional`, upgraded `Left`/`Right` to records. Added `toResult()`, `toOptional()`, `stream()`, `bimap()`.
- **Lazy\<T\>** — Virtual-thread-safe lazy evaluation using VarHandle CAS (no `synchronized`). Supplier exception memoization. `@Experimental reset()` support.
- **VirtualTasks** — Virtual thread concurrency utilities: `invokeAll`, `invokeAny`, `invokeAllSettled`, `parallelMap`, `runAll` — all with timeout variants. nanoTime overflow-safe deadlines.
- **OpenCollections** — Immutable collection factory: `ListBuilder`, `MapBuilder`, `append`/`prepend`/`concat`/`without`/`withReplaced`, set ops (`union`/`intersection`/`difference`), collectors.
- **ObjectDiff** — Deep object diff engine with cycle detection (path-based), max depth/collection size guards, field include/exclude, collection element-level comparison.
- **Environment** — Lazy-cached runtime info: JDK version, OS detection, container detection, GraalVM native-image detection, virtual thread detection.
- **ScopedValueUtil** — Enhanced with `getIfBound()` (null-safe), `runWhere(Carrier)`, `callWhere(Carrier)` for multi-binding support.

### Changed — opencode-base-core

- **Page\<T\>** — **Breaking change**: rewritten as immutable `record`. Old mutable setters removed, replaced with `Page.of()` factory + `map()`. Overflow-safe `pages()` (Math.ceilDiv) and `offset()` (Math.multiplyExact).
- **Suppliers** — `memoize()` and `memoizeWithExpiration()` marked `@Deprecated(since = "1.0.3")`, replaced by `Lazy<T>`.
- **ThreadLocalUtil** — 7 methods marked `@Deprecated(since = "1.0.3")`, replaced by `ScopedValueUtil`. `keys()` now returns defensive copy.

### Added — opencode-base-classloader

- **HotSwapClassLoader** — Version history with rollback, `HotSwapListener` notifications, `maxHistoryVersions` configuration, `SingleClassLoader` per-definition for safe reloading.
- **IsoClassLoader** — Loading strategies (`PARENT_FIRST`/`CHILD_FIRST`/`PARENT_ONLY`/`CHILD_ONLY`), `ClassLoadingPolicy` (package whitelist/blacklist, bytecode size limit, custom verifier), leak detection, per-name synchronization.
- **ClassScanner** — Scan result caching with classpath hash validation, parallel stream support, symlink cycle protection (depth limit + canonical path visited set), path traversal guard on cache files.
- **MetadataReader** — ASM-free metadata reading from class files (annotations, fields, methods, record components), `RecordComponentMetadata` support.
- **Resource monitoring** — `ResourceWatcher` (WatchService + debounce), `NestedJarHandler` (nested JAR extraction), `ResourceEvent`/`ResourceWatchHandle`.
- **Plugin lifecycle** — `PluginManager`/`PluginLoader`/`PluginDescriptor` for discover/load/start/stop/unload.
- **Build-time index** — `ClassIndex`/`ClassIndexBuilder` for fast startup scanning.
- **GraalVM** — `NativeImageConfigGenerator` for reflect/resource/native-image metadata.
- **Security** — `ClassLoadingPolicy`, `BytecodeVerifier`, package whitelist/blacklist.
- **ClassLoader Diagnostics** — `ClassLoaderDiagnostics` for duplicate class detection, package split detection, and class loading chain tracing. All reports are immutable records with no strong ClassLoader references.
- **Leak Auto-Cleanup** — `LeakCleaner` for cleaning JDBC drivers (public API), ThreadLocals, shutdown hooks, and timer threads on ClassLoader close. Graceful degradation under module system restrictions.
- **Cross-ClassLoader ServiceLoader** — `ServiceBridge` for discovering `ServiceLoader` services across isolated ClassLoaders with priority-based ordering.
- **Bytecode Dependency Analysis** — `ClassDependencyAnalyzer` parses constant pool to extract class dependencies without loading classes. Iterative Tarjan SCC algorithm for cycle detection (stack-overflow safe).
- **JAR Conflict Detection** — `JarConflictDetector` scans JAR files to find duplicate class definitions with version info from MANIFEST.MF. Directory scanning with glob patterns.

### Infrastructure

- **JSpecify 1.0.0** — `@NullMarked` on all 22 exported packages (provided scope, zero runtime cost).
- **JPMS** — `module-info.java` updated: 3 new exports (`result`, `concurrent`, `collect`), `requires static org.jspecify`.
- **GraalVM native-image** — `reflect-config.json`, `resource-config.json`, `native-image.properties` for core module.

### Added — opencode-base-config

- **RelaxedKeyResolver** — Spring Boot-style relaxed binding: `database.max-pool-size` matches `DATABASE_MAX_POOL_SIZE`, `database.maxPoolSize`, `database.max_pool_size`. Opt-in via `ConfigBuilder.enableRelaxedBinding()`. Cached after first resolution.
- **ConfigDump** — Export all effective config with sensitive value masking (`***`). Default patterns: password, secret, token, credential, api-key, passphrase. Custom patterns supported.
- **ConfigDiff** — Compare two config snapshots, returns `List<ConfigChangeEvent>` (ADDED/MODIFIED/REMOVED). Formatted output with `+`/`~`/`-` prefixes.
- **@DefaultValue on POJO fields** — Extended from record-only to POJO fields. `ConfigBinder` now reads `@DefaultValue` annotation on fields when config key is missing.
- **ValidationResult.merge()** — Collect all validation errors from multiple validators into a single report. `ConfigBuilder.build()` now reports ALL missing keys at once instead of failing on the first.

### Changed — opencode-base-config

- **jdk25.DefaultValue** — Deprecated in favor of `bind.DefaultValue` which supports both records and POJOs. `RecordConfigBinder` accepts both during transition.
- **ConfigDump/ConfigDiff** — Use `getString(key, null)` to avoid TOCTOU race during hot-reload.
- **DefaultConfig** — Relaxed binding negative lookups cached with sentinel to avoid O(n) re-scan.

### Performance

- No P0 issues. All new components < 100ns/op.
- `Lazy.get()` post-init: 12.3 ns/op (3x vs Suppliers.memoize — acceptable trade-off for virtual-thread safety).
- `Result` chain (of→map→map→getOrElse): 33.5 ns/op (1.5x vs try-catch). Failure short-circuit: 13.4 ns/op (zero allocation).
- `OpenCollections.ListBuilder`: 36.1 ns/op (0.89x vs ArrayList — faster due to pre-allocation).
- `VirtualTasks.invokeAll`: 39.5 μs/call (1.48x vs raw executor — acceptable for I/O tasks).

---

## [1.0.2] - 2026-03-28

### Changed
- Version bump for dependency alignment across modules

---

## [1.0.0] - 2026-03-15

### Added
- **43 modules** covering core utilities, data processing, security, concurrency, and business logic
- **Core (001)**: Type conversion, math, tuples, builder pattern, exception hierarchy
- **Cache (101)**: LRU/LFU/W-TinyLFU eviction, async API, virtual threads, BloomFilter protection
- **ClassLoader (102)**: Class scanning, resource management, metadata reading
- **Collections (103)**: BiMap, Multiset, Multimap, Table, immutable & primitive collections
- **Crypto (104)**: AES-GCM, ChaCha20, RSA, SM2/SM4, Ed25519, Argon2, BCrypt, JWT, PGP
- **Date (105)**: Date formatting, workday calculation, time ranges
- **DeepClone (106)**: Reflection/serialization/unsafe deep clone strategies
- **Hash (107)**: MurmurHash3, xxHash, CRC32, Bloom filter, consistent hash, SimHash
- **I18n (108)**: Multi-locale message formatting
- **ID (109)**: Snowflake, UUID, NanoID, segment-based ID generators
- **IO (110)**: File I/O, path operations, file watching, chunked processing
- **Reflect (111)**: Type-safe reflection, Lambda metadata, Record support
- **String (112)**: Naming conversion, template engine, similarity, data masking
- **Expression (113)**: Safe expression evaluation with sandbox
- **Lock (117)**: Local, spin, read-write, named lock factory, fencing token
- **Rules (118)**: Lightweight rule engine with DSL, decision tables
- **Config (200)**: Multi-source config with hot reload
- **Functional (201)**: Try, Either, Option, Lazy, Pipeline, Lens
- **Json (202)**: Zero-dependency JSON parser with DOM, streaming, object binding
- **Log (203)**: Log facade with MDC/NDC, sampling, audit logging
- **Parallel (205)**: Virtual-thread-based batch processing, parallel map/filter
- **Pool (206)**: Generic object pool with lifecycle, auto-scaling
- **Serialization (209)**: Unified serialization: JSON, binary, Protobuf
- **Xml (211)**: DOM/SAX/StAX parsing, XPath, Schema validation, XSLT
- **Yml (212)**: YAML parsing with multi-document, anchors, merge keys
- **OAuth2 (213)**: OAuth2/OIDC client with PKCE
- **Captcha (300)**: Image CAPTCHA: numeric, arithmetic, Chinese, GIF
- **Email (302)**: SMTP/IMAP/POP3 with templates
- **Event (303)**: In-process event bus with Saga orchestration
- **Feature (304)**: Feature toggles with A/B testing
- **Geo (305)**: Haversine/Vincenty distance, geofence, GeoHash
- **Graph (306)**: Directed/undirected graphs, Dijkstra, topological sort
- **Image (307)**: Resize, crop, rotate, watermark, format conversion
- **Lunar (308)**: Chinese lunar calendar, solar terms, zodiac
- **Money (309)**: Precise money calculation, multi-currency, allocation
- **SMS (311)**: Multi-platform SMS (Aliyun, Tencent, Huawei)
- **Test (313)**: Fluent assertions, mock proxy, Faker, benchmarks
- **TimeSeries (314)**: Time series aggregation, anomaly detection
- **Tree (315)**: List-to-tree, AVL tree, red-black tree, skip list
- **Web (316)**: Unified Result<T>, PageResult, request context
- **Pdf (318)**: Zero-dependency PDF create, merge, split, digital signature
- **Cron**: Full-featured cron expression parser with L/W/#, OR semantics, describe()
- **Observability**: Tracing, spans, slow-log collection

### Security
- 14 rounds of deep security audits, 338+ issues identified and fixed
- Covers thread safety, resource leaks, integer overflow, injection, race conditions
