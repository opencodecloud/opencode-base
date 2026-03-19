# OpenCode Base ID

Distributed ID generation library for JDK 25+. Provides a unified facade for generating various types of unique identifiers including Snowflake, UUID, ULID, TSID, KSUID, NanoID, and segment-based IDs.

## Maven Dependency

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-id</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Features

- Snowflake: 64-bit time-ordered distributed IDs with configurable worker/datacenter bits
- UUID v4/v7: Standard 128-bit universally unique identifiers (v7 is time-ordered)
- ULID: 128-bit lexicographically sortable unique identifiers
- TSID: 64-bit time-sorted IDs with Crockford's Base32 encoding
- KSUID: 160-bit K-sortable unique identifiers
- NanoID: Compact, URL-friendly IDs with customizable alphabet and length
- Segment mode: Database sequence-based ID generation with double buffering
- Simple generators: Atomic counter, timestamp-based, and random IDs
- ID parsing and validation for Snowflake, ULID, TSID, and KSUID
- Thread-safe, all generators are safe for concurrent use

## Class Reference

### Core

| Class | Description |
|-------|-------------|
| `OpenId` | Main facade class providing unified API for all ID generation operations |
| `IdGenerator<T>` | Functional interface for all ID generators, supporting single and batch generation |
| `IdConverter` | Utility for converting IDs between different representations |
| `IdParser` | Utility for parsing ID strings back to structured data |

### Snowflake

| Class | Description |
|-------|-------------|
| `SnowflakeGenerator` | 64-bit Snowflake ID generator with configurable epoch and bit allocation |
| `SnowflakeBuilder` | Builder for customized Snowflake generator configuration |
| `SnowflakeConfig` | Configuration record for Snowflake generator parameters |
| `SnowflakeIdParser` | Parses Snowflake IDs to extract timestamp, worker ID, and sequence |
| `ClockBackwardStrategy` | Strategy interface for handling clock backward events |
| `ThrowException` | Clock backward strategy that throws an exception |
| `Wait` | Clock backward strategy that waits for the clock to catch up |
| `Extend` | Clock backward strategy that extends the sequence bits |
| `WorkerIdAssigner` | Interface for assigning worker IDs |
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

### ULID

| Class | Description |
|-------|-------------|
| `UlidGenerator` | 128-bit ULID generator producing 26-character Crockford Base32 strings |
| `UlidConfig` | Configuration for ULID generator |
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
| `Alphabet` | Predefined alphabets for NanoID generation |

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
| `OpenIdGenerationException` | Thrown when ID generation fails |

## Quick Start

```java
// Snowflake ID
long id = OpenId.snowflakeId();
String idStr = OpenId.snowflakeIdStr();

// Parse Snowflake ID
var parsed = OpenId.parseSnowflakeId(id);
System.out.println("Timestamp: " + parsed.timestamp());

// Custom Snowflake generator
IdGenerator<Long> generator = OpenId.createSnowflake(1, 1);

// UUID
UUID uuid = OpenId.uuid();
UUID uuidV7 = OpenId.uuidV7();
String simpleUuid = OpenId.simpleUuid(); // no hyphens

// ULID
String ulid = OpenId.ulid();
var parsedUlid = OpenId.parseUlid(ulid);

// TSID
long tsid = OpenId.tsid();
String tsidStr = OpenId.tsidStr(); // Crockford Base32

// KSUID
String ksuid = OpenId.ksuid();
boolean valid = OpenId.isValidKsuid(ksuid);

// NanoID
String nanoId = OpenId.nanoId();        // 21 chars
String shortId = OpenId.nanoId(10);     // custom length
String custom = OpenId.nanoId(8, "0123456789"); // digits only

// Simple IDs
long simpleId = OpenId.simpleId();      // atomic counter
String timestampId = OpenId.timestampId();
String randomId = OpenId.randomId(16);

// Batch generation
List<Long> ids = OpenId.getSnowflake().generateBatch(100);
```

## Requirements

- Java 25+

## License

Apache License 2.0
