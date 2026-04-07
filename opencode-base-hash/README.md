# OpenCode Base Hash

**Hash utilities library for Java 25+**

`opencode-base-hash` provides a unified API for hash functions and hash-based data structures, including non-cryptographic hashes (MurmurHash3, xxHash, FNV-1a, CRC32, SipHash, Adler32), cryptographic hashes (MD5, SHA family, SHA-3), HMAC authentication (HMAC-SHA256/384/512), Bloom filters, consistent hash rings, SimHash for text similarity, and zero-allocation hash code combining.

## Features

### Hash Functions
- **MurmurHash3**: 32-bit and 128-bit variants with seed support
- **xxHash**: High-performance 64-bit hash with seed support
- **FNV-1a**: 32-bit and 64-bit Fowler-Noll-Vo hash
- **CRC32**: CRC32 and CRC32C (Castagnoli) checksums
- **Adler32**: Fast checksum (lighter than CRC32)
- **SipHash-2-4**: Hash-flooding resistant keyed hash (64-bit)
- **MD5**: For checksums only (not cryptographically secure)
- **SHA-1**: For checksums only (not cryptographically secure)
- **SHA-256 / SHA-384 / SHA-512**: Cryptographic hash functions
- **SHA3-256 / SHA3-512**: Latest SHA-3 standard
- **HMAC**: HMAC-MD5, HMAC-SHA1, HMAC-SHA256, HMAC-SHA384, HMAC-SHA512

### Data Structures
- **BloomFilter**: Probabilistic set membership with configurable false positive rate
- **CountingBloomFilter**: Bloom filter with element removal support
- **ConsistentHash**: Consistent hash ring for distributed load balancing
- **SimHash**: Locality-sensitive hashing for text similarity detection

### Utilities
- **HashCode**: Unified hash result with int, long, byte array, hex string, and Base64 access
- **HashCodes**: Zero-allocation hash code combiner for `hashCode()` implementations
- **Funnel**: Type-safe serialization interface for hash input
- **Hasher**: Streaming hash builder for incremental hashing
- **File/Stream hashing**: Convenience methods for hashing files and InputStreams
- **Ordered/Unordered Combine**: Combine multiple hash codes

## Quick Start

### Maven Dependency
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-hash</artifactId>
    <version>1.0.3</version>
</dependency>
```

### Basic Usage

```java
import cloud.opencode.base.hash.*;

// Hash a string with MurmurHash3
HashCode hash = OpenHash.murmur3_128().hashUtf8("Hello World");
System.out.println(hash.toHex());     // hex string
System.out.println(hash.toBase64());  // URL-safe Base64

// Hash with different algorithms
HashCode murmur = OpenHash.murmur3_32().hashUtf8("data");
HashCode xx = OpenHash.xxHash64().hashUtf8("data");
HashCode sha = OpenHash.sha256().hashUtf8("data");
HashCode sip = OpenHash.sipHash24().hashUtf8("data");

// HMAC authentication
byte[] key = "secret".getBytes();
HashCode hmac = OpenHash.hmacSha256(key).hashUtf8("message");

// Hash a file
HashCode fileHash = OpenHash.sha256().hashFile(Path.of("data.bin"));

// Hash an InputStream
HashCode streamHash = OpenHash.sha256().hashInputStream(inputStream);

// Zero-allocation hashCode() combining
int hash = HashCodes.combine(name.hashCode(), age, active ? 1 : 0);
int hash2 = HashCodes.start().add(name).add(age).add(active).result();

// Combine HashCode objects
HashCode combined = OpenHash.combineOrdered(hash1, hash2, hash3);
```

### Bloom Filter

```java
// Create a Bloom filter
BloomFilter<String> filter = OpenHash.<String>bloomFilter(Funnel.STRING_FUNNEL)
    .expectedInsertions(1_000_000)
    .fpp(0.01)
    .build();

// Add elements
filter.put("apple");
filter.put("banana");

// Test membership
filter.mightContain("apple");   // true
filter.mightContain("cherry");  // false (probably)

// Counting Bloom filter (supports removal)
CountingBloomFilter<String> countingFilter = OpenHash.<String>countingBloomFilter(Funnel.STRING_FUNNEL)
    .expectedInsertions(100_000)
    .build();
countingFilter.put("item");
countingFilter.remove("item");
```

### Consistent Hash Ring

```java
// Create a consistent hash ring
ConsistentHash<String> ring = OpenHash.<String>consistentHash()
    .addNode("server1", "192.168.1.1")
    .addNode("server2", "192.168.1.2")
    .addNode("server3", "192.168.1.3")
    .virtualNodeCount(150)
    .build();

// Route a key to a node
String server = ring.get("user:12345");  // "192.168.1.x"

// Add/remove nodes dynamically
ring.addNode("server4", "192.168.1.4");
ring.removeNode("server2");
```

### SimHash (Text Similarity)

```java
// Create SimHash
SimHash simHash = OpenHash.simHash()
    .nGram(3)
    .build();

// Compute fingerprints
Fingerprint fp1 = simHash.fingerprint("The quick brown fox");
Fingerprint fp2 = simHash.fingerprint("The quick brown dog");

// Compare similarity (Hamming distance)
int distance = fp1.hammingDistance(fp2);
boolean similar = fp1.isSimilar(fp2, 3);  // within 3 bits
```

### Streaming Hash (Hasher)

```java
// Incremental hashing
Hasher hasher = OpenHash.murmur3_128().newHasher();
hasher.putString("name", StandardCharsets.UTF_8);
hasher.putInt(42);
hasher.putLong(System.currentTimeMillis());
HashCode hash = hasher.hash();
```

## Class Reference

### Root Package (`cloud.opencode.base.hash`)
| Class | Description |
|-------|-------------|
| `OpenHash` | Main facade with factory methods for all hash functions and data structures |
| `HashFunction` | Interface for hash function implementations (includes `hashFile`/`hashInputStream`) |
| `HashCode` | Immutable hash result with int, long, byte array, hex, and Base64 access |
| `HashCodes` | Zero-allocation hash code combiner for `hashCode()` implementations |
| `Hasher` | Streaming hash builder for incremental input |
| `Funnel` | Type-safe serialization interface for converting objects to hash input |

### Function Package (`cloud.opencode.base.hash.function`)
| Class | Description |
|-------|-------------|
| `AbstractHashFunction` | Base class for hash function implementations |
| `Murmur3HashFunction` | MurmurHash3 32-bit and 128-bit implementations |
| `XxHashFunction` | xxHash 64-bit high-performance hash |
| `Fnv1aHashFunction` | FNV-1a 32-bit and 64-bit hash |
| `Crc32HashFunction` | CRC32 and CRC32C (Castagnoli) implementations |
| `Adler32HashFunction` | Adler-32 checksum |
| `SipHashFunction` | SipHash-2-4 keyed hash (hash-flooding resistant) |
| `HmacHashFunction` | HMAC-MD5, HMAC-SHA1, HMAC-SHA256, HMAC-SHA384, HMAC-SHA512 |
| `MessageDigestHashFunction` | JDK MessageDigest-based hashes (MD5, SHA-1, SHA-256, SHA-384, SHA-512, SHA3) |

### Bloom Filter Package (`cloud.opencode.base.hash.bloom`)
| Class | Description |
|-------|-------------|
| `BloomFilter` | Probabilistic set membership data structure |
| `BloomFilterBuilder` | Builder for configuring Bloom filter parameters |
| `CountingBloomFilter` | Bloom filter variant supporting element removal |
| `BitArray` | Compact bit array backing the Bloom filter |

### Consistent Hash Package (`cloud.opencode.base.hash.consistent`)
| Class | Description |
|-------|-------------|
| `ConsistentHash` | Consistent hash ring for distributed load balancing |
| `ConsistentHashBuilder` | Builder for configuring consistent hash ring |
| `HashNode` | Physical node in the consistent hash ring |
| `VirtualNode` | Virtual node mapped to a physical node |
| `NodeLocator` | Interface for locating nodes by key |

### SimHash Package (`cloud.opencode.base.hash.simhash`)
| Class | Description |
|-------|-------------|
| `SimHash` | Locality-sensitive hashing for text similarity |
| `SimHashBuilder` | Builder for configuring SimHash parameters |
| `Fingerprint` | SimHash fingerprint with Hamming distance comparison |
| `Tokenizer` | Text tokenization for SimHash input (n-gram, word) |

### Exception Package (`cloud.opencode.base.hash.exception`)
| Class | Description |
|-------|-------------|
| `OpenHashException` | Base exception for hash operations |

## Requirements

- Java 25+ (uses records, sealed interfaces, pattern matching)
- No external dependencies for core functionality

## License

Apache License 2.0

## Author

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
