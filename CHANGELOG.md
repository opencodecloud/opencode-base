# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
