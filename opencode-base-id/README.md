# OpenCode Base ID

Distributed ID generation library for JDK 25+. Provides a unified facade for generating various types of unique identifiers including Snowflake, UUID, ULID, TSID, KSUID, NanoID, prefixed/typed IDs, and segment-based IDs.

## Maven Dependency

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-id</artifactId>
    <version>1.0.3</version>
</dependency>
```

## Features

- **Snowflake**: 64-bit time-ordered distributed IDs with configurable worker/datacenter bits
- **UUID v4/v7**: Standard 128-bit universally unique identifiers (v7 is time-ordered)
- **ULID**: 128-bit lexicographically sortable unique identifiers (monotonic/non-monotonic via `UlidConfig`)
- **TSID**: 64-bit time-sorted IDs with Crockford's Base32 encoding
- **KSUID**: 160-bit K-sortable unique identifiers
- **NanoID**: Compact, URL-friendly IDs with customizable alphabet and length
- **SafeJsSnowflake**: Snowflake IDs guaranteed ≤ 2^53−1 (JavaScript `Number.MAX_SAFE_INTEGER`)
- **PrefixedId / TypedIdGenerator**: Stripe-style type-prefixed IDs (e.g., `usr_01ARZ3NDEK`, `order_XYZ999`)
- **SnowflakeFriendlyId**: Human-readable Snowflake format for debugging (e.g., `2024-01-15T10:30:00.123Z#3-7-42`)
- **Segment mode**: Database sequence-based ID generation with double buffering
- **Simple generators**: Atomic counter, timestamp-based, and random IDs
- **ID parsing**: Snowflake, ULID, TSID, KSUID, UUID parsing with structured results
- **ID conversion**: Base62, Base36, Base58 encoding/decoding, UUID↔ULID, UUID↔Base62
- Thread-safe: all generators are safe for concurrent use

## Class Reference

### Core

| Class | Description |
|-------|-------------|
| `OpenId` | Main facade providing unified API for all ID generation operations |
| `IdGenerator<T>` | Functional interface for all ID generators, supporting single and batch generation |
| `IdConverter` | Converts IDs between representations: Base62, Base36, Base58, UUID↔ULID |
| `IdParser<T,R>` | Interface for parsing ID values into structured result objects |

### Snowflake

| Class | Description |
|-------|-------------|
| `SnowflakeGenerator` | 64-bit Snowflake ID generator with configurable epoch and bit allocation |
| `SnowflakeBuilder` | Builder for customized Snowflake generator configuration |
| `SnowflakeConfig` | Configuration record for Snowflake generator parameters |
| `SnowflakeIdParser` | Parses Snowflake IDs to extract timestamp, worker ID, and sequence |
| `SnowflakeFriendlyId` | Converts Snowflake longs to human-readable format for debugging |
| `SafeJsSnowflakeGenerator` | Snowflake IDs guaranteed ≤ JavaScript's Number.MAX_SAFE_INTEGER (2^53−1) |
| `ClockBackwardStrategy` | Strategy interface for handling clock backward events |
| `ThrowException` | Clock backward strategy that throws an exception |
| `Wait` | Clock backward strategy that waits for the clock to catch up |
| `Extend` | Clock backward strategy that extends the sequence bits |
| `WorkerIdAssigner` | Interface for assigning worker IDs |
| `FixedWorkerIdAssigner` | Explicit fixed worker and datacenter ID assignment |
| `IpBasedAssigner` | Assigns worker ID based on machine IP address |
| `MacBasedAssigner` | Assigns worker ID based on MAC address |
| `RandomAssigner` | Assigns a random worker ID |

### UUID

| Class | Description |
|-------|-------------|
| `OpenUuid` | UUID utility class for format conversion (simple string, byte array) |
| `UuidGenerator` | Base UUID generator interface |
| `UuidV4Generator` | UUID v4 (random) generator |
| `UuidV7Generator` | UUID v7 (time-ordered) generator for sortable UUIDs |
| `UuidParser` | Parses UUID objects: version, variant, timestamp extraction for v1/v6/v7 |

### ULID

| Class | Description |
|-------|-------------|
| `UlidGenerator` | 128-bit ULID generator; supports monotonic (default) and non-monotonic modes |
| `UlidConfig` | Configuration for ULID generator (`withMonotonic()` / `nonMonotonic()`) |
| `UlidParser` | Parses ULID strings to extract timestamp and random components |

### TSID

| Class | Description |
|-------|-------------|
| `TsidGenerator` | 64-bit time-sorted ID generator with configurable node bits |
| `TsidParser` | Parses TSID values to extract timestamp and node information |

### KSUID

| Class | Description |
|-------|-------------|
| `KsuidGenerator` | 160-bit K-sortable unique ID generator (27-character strings) |
| `KsuidParser` | Parses KSUID strings to extract timestamp and payload |

### NanoID

| Class | Description |
|-------|-------------|
| `NanoIdGenerator` | Compact, URL-friendly ID generator (default 21 characters) |
| `NanoIdBuilder` | Builder for customizing NanoID size and alphabet |
| `Alphabet` | Predefined alphabets: DEFAULT, ALPHANUMERIC, NUMERIC, HEX, NOLOOK_ALIKE, etc. |

### Prefixed / Typed ID

| Class | Description |
|-------|-------------|
| `PrefixedId` | Immutable record representing a type-prefixed ID (e.g., `usr_01ARZ3NDEK`) |
| `TypedIdGenerator` | `IdGenerator<String>` that wraps any generator with a validated entity-type prefix |

### Segment

| Class | Description |
|-------|-------------|
| `SegmentIdGenerator` | Database sequence-based ID generator with double buffering for high throughput |
| `SegmentAllocator` | Interface for allocating ID segments from a backing store |
| `JdbcSegmentAllocator` | JDBC-backed segment allocator using database sequences |
| `MemorySegmentAllocator` | In-memory segment allocator for testing |
| `SegmentBuffer` | Double-buffer implementation for smooth ID generation |

### Simple

| Class | Description |
|-------|-------------|
| `AtomicIdGenerator` | Simple atomic counter-based ID generator |
| `RandomIdGenerator` | Random string ID generator with configurable length |
| `TimestampIdGenerator` | Timestamp-based ID generator with collision avoidance |

### Exceptions

| Class | Description |
|-------|-------------|
| `OpenIdGenerationException` | Thrown when ID generation fails; carries `generatorType`, `bizTag`, and error code constants |

## Quick Start

```java
// ── Snowflake ──────────────────────────────────────────────────────────────
long id = OpenId.snowflakeId();
String idStr = OpenId.snowflakeIdStr();
var parsed = OpenId.parseSnowflakeId(id);  // timestamp, workerId, datacenterId, sequence

// Custom configuration
IdGenerator<Long> gen = OpenId.snowflakeBuilder()
    .workerIdAssigner(FixedWorkerIdAssigner.of(3, 1))
    .clockBackwardStrategy(Wait.ofSeconds(5))
    .build();

// Human-readable format for debugging/logging
String readable = OpenId.snowflakeFriendly().toFriendly(id);
// → "2024-01-15T10:30:00.123Z#1-3-42"

// JavaScript-safe Snowflake (≤ 2^53-1)
long jsId = OpenId.safeJsSnowflakeId();
assert SafeJsSnowflakeGenerator.isJsSafe(jsId); // always true

// ── Prefixed / Typed IDs ───────────────────────────────────────────────────
TypedIdGenerator userGen  = OpenId.typedIdGenerator("usr",   UlidGenerator.create());
TypedIdGenerator orderGen = OpenId.typedIdGenerator("order", NanoIdGenerator.create());

String userId  = userGen.generate();  // "usr_01ARZ3NDEKTSV4RRFFQ69G5FAV"
String orderId = orderGen.generate(); // "order_V1StGXR8_Z5jdHi6B-myT"

PrefixedId parsed2 = OpenId.parsePrefixedId(userId);
// parsed2.prefix() → "usr", parsed2.rawId() → "01ARZ3NDEKTSV4RRFFQ69G5FAV"

// ── UUID ───────────────────────────────────────────────────────────────────
UUID uuid  = OpenId.uuid();    // v4 (random)
UUID uuidV7 = OpenId.uuidV7(); // v7 (time-ordered)
String simpleUuid = OpenId.simpleUuid(); // 32-char, no hyphens

// Parse UUID
UuidParser.ParsedUuid up = OpenId.parseUuid(uuidV7);
System.out.println(up.version());    // 7
System.out.println(up.timestamp()); // Instant (ms precision)

// ── ULID ───────────────────────────────────────────────────────────────────
String ulid = OpenId.ulid(); // monotonic by default
// Non-monotonic mode
UlidGenerator nonMono = UlidGenerator.create(UlidConfig.nonMonotonic());

// ── TSID / KSUID / NanoID ─────────────────────────────────────────────────
long tsid    = OpenId.tsid();
String ksuid = OpenId.ksuid();
String nano  = OpenId.nanoId();
String nano8 = OpenId.nanoId(8, "0123456789");

// ── Simple IDs ─────────────────────────────────────────────────────────────
long simpleId = OpenId.simpleId();      // atomic counter
String tsId   = OpenId.timestampId();
String randId = OpenId.randomId(16);

// ── ID Conversion ─────────────────────────────────────────────────────────
String b62 = IdConverter.toBase62(id);
String b58 = IdConverter.toBase58(id); // Bitcoin-style, no 0/O/I/l
String b36 = IdConverter.toBase36(id);

long back62 = IdConverter.fromBase62(b62);
long back58 = IdConverter.fromBase58(b58);

// ── Batch generation ───────────────────────────────────────────────────────
List<Long> ids = OpenId.getSnowflake().generateBatch(100);
```

## Requirements

- Java 25+

## License

Apache License 2.0
