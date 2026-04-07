# OpenCode Base IO

IO utilities for file operations, resource loading, and stream processing for JDK 25+. Built on NIO.2 API with unchecked exceptions for cleaner code.

## Maven Dependency

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-io</artifactId>
    <version>1.0.3</version>
</dependency>
```

## Features

- File reading: bytes, strings, lines, streams with charset support
- File writing: bytes, strings, lines, append operations
- File operations: copy, move, delete (single and recursive)
- Directory traversal: list, walk, glob, regex, and custom matchers
- Resource loading: classpath, filesystem, and URL resources with unified interface
- Byte and character source/sink abstractions
- Stream utilities: bounded, counting, tee, and fast byte array output streams
- File checksum calculation (MD5, SHA-256, CRC32, etc.)
- Batch file operations with virtual-thread parallel execution
- File watching for change notifications
- File locking for cross-process synchronization
- Temporary file management with auto-cleanup
- MIME type detection with O(1) reverse lookup
- Path utilities and file comparator with mmap support
- Chunked file processing for large files
- Progress tracking for uploads and downloads
- Serialization marshalling interface
- **Gzip compression/decompression with bomb protection** (V1.0.3)
- **Zip archive operations with security protections** (V1.0.3)
- **File-backed output stream for memory-safe buffering** (V1.0.3)
- **Hex dump formatting for binary data** (V1.0.3)
- **Reader-to-InputStream / Writer-to-OutputStream adapters** (V1.0.3)
- **Tail reader for efficient last-N-lines reading** (V1.0.3)
- **Functional line processor pipeline** (V1.0.3)
- **Atomic file write via temp-file-then-move** (V1.0.3)

## Security

This module includes built-in protections against common IO-related attacks:

| Protection | Class | Detail |
|-----------|-------|--------|
| **SSRF prevention** | `UrlResource` | Only `http`, `https`, `file`, `jar` protocols allowed; `ftp`, `gopher`, and others throw `OpenIOOperationException` |
| **Gzip bomb protection** | `GzipUtil` | Default 256 MB decompressed size limit; configurable via `decompress(data, maxSize)` |
| **Zip bomb protection** | `ZipUtil` | Per-entry and total uncompressed size limits enforced during extraction |
| **Zip path traversal** | `ZipBuilder` | Entry names validated: rejects `..` segments, absolute paths, and null bytes |
| **Resource path traversal** | `OpenResource` | Path segments validated; `..` components rejected before classpath lookup |
| **Symlink safety** | `OpenTempFile` | Uses `toRealPath()` to resolve symlinks before operating on temp files |

```java
// SSRF: rejected protocols throw OpenIOOperationException
new UrlResource("ftp://host/file");    // throws — SSRF protection
new UrlResource("http://host/file");   // OK

// Gzip bomb: default 256 MB limit
byte[] safe = GzipUtil.decompress(compressed);                    // 256 MB limit
byte[] safe = GzipUtil.decompress(compressed, 10 * 1024 * 1024); // custom 10 MB limit

// Zip path traversal: entry name validated at add time
ZipUtil.builder()
    .addString("../../etc/passwd", "data"); // throws — path traversal rejected
```

## Class Reference

### Core

| Class | Description |
|-------|-------------|
| `OpenIO` | Main facade for file and directory operations: read, write, copy, move, delete, traverse |
| `OpenStream` | Stream utilities: copy, read, write, buffer, compare, drain |
| `OpenPath` | Path utilities: extension, filename, normalization, relativization |
| `OpenResource` | Resource loading from classpath and filesystem with path traversal protection |
| `OpenMimeType` | MIME type detection and extension mapping with O(1) reverse lookup |
| `OpenFileVisitors` | File visitor implementations for recursive delete, copy, and size calculation |

### File Operations

| Class | Description |
|-------|-------------|
| `FileReader` | Enhanced file reader with encoding detection and line processing |
| `FileWriter` | Enhanced file writer with atomic write and backup support |
| `FileCopier` | File copy utility with progress tracking and copy options |
| `FileComparator` | Compares files by content (mmap), size, or modification time |
| `FileWatcher` | Watches directories for creation, modification, and deletion events |
| `ChunkedFileProcessor` | Processes large files in configurable chunks to limit memory usage |
| `MoreFiles` | Advanced file utilities: atomic write, touch, secure delete, file tree builder |
| `TailReader` | Efficient reading of last N lines/bytes via backward byte scanning |
| `LineProcessor` | Functional line-by-line processing pipeline with `grep` / `map` / `limit` / `collect` |

### Resource Loading

| Class | Description |
|-------|-------------|
| `Resource` | Interface for abstract resource access (input stream, URL, length, last-modified) |
| `ResourceLoader` | Interface for loading `Resource` by location string |
| `DefaultResourceLoader` | Default loader: auto-detects `classpath:`, `file:`, URL, or plain path |
| `ClassPathResource` | Resource loaded from the classpath via `ClassLoader` |
| `FileSystemResource` | Resource backed by a `java.nio.file.Path` |
| `UrlResource` | Resource loaded from a URL with SSRF protocol whitelist |

### Source / Sink

| Class | Description |
|-------|-------------|
| `ByteSource` | Readable source of bytes: wrap array, file, URL, classpath; supports slice, concat, hash |
| `ByteSink` | Writable sink for bytes: file, output stream |
| `CharSource` | Readable source of characters: wrap string, reader, file; supports copyTo |
| `CharSink` | Writable sink for characters: file, writer |

### Stream

| Class | Description |
|-------|-------------|
| `BoundedInputStream` | Limits bytes read; optional exception when limit exceeded |
| `CountingInputStream` | Counts bytes read with reset support |
| `TeeInputStream` | Duplicates all read data to a secondary `OutputStream` |
| `FastByteArrayOutputStream` | High-performance `ByteArrayOutputStream` with reduced array copying |
| `FileBackedOutputStream` | Buffers in memory up to a threshold, then spills to a temp file |
| `ReaderInputStream` | Adapts a `Reader` to `InputStream` using configurable charset |
| `WriterOutputStream` | Adapts a `Writer` to `OutputStream` using configurable charset |

### Batch

| Class | Description |
|-------|-------------|
| `OpenBatch` | Bulk copy/move/delete with glob/pattern support and virtual-thread parallel execution |
| `BatchResult` | Result record: success count, failure count, skipped count, per-failure details |

### Checksum

| Class | Description |
|-------|-------------|
| `OpenChecksum` | Checksums for file, stream, or byte array: CRC32, MD5, SHA-1, SHA-256, SHA-512 |
| `Checksum` | Result record with algorithm name, raw bytes, and `hex()` accessor |

### Lock

| Class | Description |
|-------|-------------|
| `OpenFileLock` | File-based locking via `FileChannel.lock()` for cross-process synchronization |

### Temp

| Class | Description |
|-------|-------------|
| `OpenTempFile` | Creates temporary files with configurable prefix, suffix, and directory |
| `AutoDeleteTempFile` | Auto-deleting temporary file (implements `AutoCloseable`) |

### Progress

| Class | Description |
|-------|-------------|
| `DownloadProgress` | Progress tracking for download operations (bytes transferred, total, percentage) |
| `UploadProgress` | Progress tracking for upload operations (bytes transferred, total, percentage) |

### Serialization

| Class | Description |
|-------|-------------|
| `Marshaller` | Interface for object serialization/deserialization to byte streams |

### Compression

| Class | Description |
|-------|-------------|
| `GzipUtil` | Gzip compress/decompress for byte arrays, files, and streams; bomb-protected |
| `ZipUtil` | Zip archive creation, extraction, and inspection; bomb and path-traversal protected |
| `ZipBuilder` | Fluent builder for creating zip archives with entry-name validation |
| `ZipEntryInfo` | Immutable record of zip entry metadata (name, size, compressed size, modified time) |

### Hex

| Class | Description |
|-------|-------------|
| `HexDump` | Formats binary data as hex dump (offset + hex columns + ASCII); encodes/decodes hex strings |

### Exceptions

| Class | Description |
|-------|-------------|
| `OpenIOOperationException` | Unchecked exception for IO operation failures with operation name and path context |

## Quick Start

```java
import cloud.opencode.base.io.*;
import cloud.opencode.base.io.compress.*;
import cloud.opencode.base.io.file.*;
import cloud.opencode.base.io.hex.*;
import cloud.opencode.base.io.resource.*;
import cloud.opencode.base.io.source.*;
import cloud.opencode.base.io.stream.*;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
```

### File Read / Write

```java
// Read
String content = OpenIO.readString(Path.of("config.txt"));
List<String> lines = OpenIO.readLines(Path.of("data.csv"));
byte[] bytes = OpenIO.readBytes(Path.of("binary.dat"));

// Write
OpenIO.writeString(Path.of("output.txt"), "Hello World");
OpenIO.writeLines(Path.of("data.csv"), List.of("a,b,c", "1,2,3"));
OpenIO.append(Path.of("log.txt"), "New entry\n");

// Atomic write — readers never see partial content
MoreFiles.writeAtomically(Path.of("config.json"), jsonContent);
```

### File Operations

```java
OpenIO.copy(source, target);
OpenIO.move(source, target);
OpenIO.deleteRecursively(Path.of("/tmp/workspace"));

// Directory traversal — glob
try (Stream<Path> files = OpenIO.glob(Path.of("src"), "*.java")) {
    files.forEach(System.out::println);
}

// Advanced file tree
try (Stream<Path> files = MoreFiles.fileTree(dir)
        .maxDepth(3)
        .filter(Files::isRegularFile)
        .stream()) {
    files.forEach(System.out::println);
}
```

### Resource Loading

```java
// Classpath resource
Resource tpl = new ClassPathResource("templates/email.html");
String html = tpl.readString();

// Auto-detect prefix: classpath:, file:, URL, or plain path
ResourceLoader loader = DefaultResourceLoader.INSTANCE;
Resource r = loader.getResource("classpath:schema.sql");

// URL resource — only allowed protocols: http, https, file, jar
Resource remote = new UrlResource("https://example.com/data.json");
```

### ByteSource / CharSource

```java
// ByteSource — immutable, re-readable, composable
ByteSource source = ByteSource.fromPath(Path.of("data.bin"));
long size = source.size();
byte[] data = source.read();
source.copyTo(outputStream);

// Slice and concat
ByteSource slice = source.slice(1024, 512);
ByteSource all = ByteSource.concat(ByteSource.wrap(header), source);

// Content equality (block comparison, no full load needed for early exit)
boolean equal = source.contentEquals(ByteSource.fromPath(Path.of("copy.bin")));

// CharSource
CharSource charSrc = ByteSource.fromPath(Path.of("readme.txt"))
        .asCharSource(StandardCharsets.UTF_8);
String text = charSrc.read();
charSrc.copyTo(writer);
```

### Checksum

```java
// Hash a file
String sha256 = OpenChecksum.sha256(Path.of("file.zip"));
long crc = OpenChecksum.crc32(Path.of("file.zip"));

// Verify integrity
boolean valid = OpenChecksum.verify(Path.of("file.zip"), expectedSha256, "SHA-256");

// Full result object
Checksum cs = OpenChecksum.calculate(Path.of("file.zip"), "SHA-256");
System.out.println(cs.algorithm() + ": " + cs.hex());
```

### Compression

```java
// Gzip — byte array (default 256 MB decompression limit)
byte[] compressed = GzipUtil.compress(data);
byte[] original   = GzipUtil.decompress(compressed);

// Custom decompression limit (10 MB)
byte[] safe = GzipUtil.decompress(compressed, 10 * 1024 * 1024);

// Gzip — file
GzipUtil.compress(Path.of("data.json"), Path.of("data.json.gz"));
GzipUtil.decompress(Path.of("data.json.gz"), Path.of("data.json"));

// Zip — directory
ZipUtil.zip(Path.of("project-dir"), Path.of("project.zip"));
ZipUtil.unzip(Path.of("project.zip"), Path.of("output-dir"));

// Zip — builder API (entry names validated against path traversal)
ZipUtil.builder()
    .addFile(Path.of("readme.md"))
    .addFile(Path.of("src/Main.java"), "sources/Main.java")
    .addString("notes.txt", "Build notes")
    .addBytes("data.bin", rawBytes)
    .writeTo(Path.of("archive.zip"));

// List zip entries
List<ZipEntryInfo> entries = ZipUtil.list(Path.of("archive.zip"));
entries.forEach(e -> System.out.printf("%s (%d bytes)%n", e.name(), e.size()));

// Read a single entry
byte[] content = ZipUtil.readEntry(Path.of("archive.zip"), "readme.md");
```

### Hex Dump

```java
// Format binary data as hex dump
byte[] data = "Hello, World!".getBytes();
String dump = HexDump.format(data);
// 00000000  48 65 6C 6C 6F 2C 20 57  6F 72 6C 64 21           |Hello, World!|

// Encode / decode hex string
String hex  = HexDump.toHex(data);           // "48656c6c6f2c20576f726c6421"
String up   = HexDump.toHexUpper(data);      // "48656C6C6F2C20576F726C6421"
byte[] back = HexDump.fromHex(hex);

// Hex dump a file (up to 1 MB by default)
String fileDump = HexDump.format(Path.of("binary.dat"));

// Hex dump a byte range of a file
String rangeDump = HexDump.format(Path.of("binary.dat"), 1024L, 256);
```

### Stream Utilities

```java
// Copy
long bytes = OpenStream.copy(inputStream, outputStream);

// Bounded stream — throw if data exceeds 5 MB
try (BoundedInputStream bounded = new BoundedInputStream(input, 5 * 1024 * 1024, true)) {
    byte[] data = bounded.readAllBytes();
}

// Tee — read and capture simultaneously
ByteArrayOutputStream copy = new ByteArrayOutputStream();
try (TeeInputStream tee = new TeeInputStream(input, copy)) {
    process(tee);
}
byte[] captured = copy.toByteArray();

// File-backed output stream — spills to disk when > threshold
try (FileBackedOutputStream fbos = new FileBackedOutputStream(1024 * 1024)) {
    fbos.write(possiblyLargeData);
    try (InputStream in = fbos.getInputStream()) {
        process(in);
    }
}

// Reader ↔ InputStream adapters
try (InputStream is = new ReaderInputStream(reader, StandardCharsets.UTF_8)) {
    process(is);
}
try (OutputStream os = new WriterOutputStream(writer, StandardCharsets.UTF_8)) {
    os.write(utf8Bytes);
}

// Stream content equality
boolean equal = OpenStream.contentEquals(stream1, stream2);
```

### Tail & Line Processing

```java
// Last 50 lines of a log file
List<String> lastLines = TailReader.lastLines(Path.of("app.log"), 50);

// Functional line pipeline
List<String> errors = LineProcessor.of(Path.of("app.log"))
    .grep("ERROR")
    .map(String::trim)
    .limit(100)
    .collect();
```

### Batch Operations

```java
List<Path> files = List.of(path1, path2, path3);

// Sequential batch copy
BatchResult result = OpenBatch.copyAll(files, targetDir);
System.out.printf("OK: %d  Failed: %d%n", result.successCount(), result.failureCount());

// Parallel with progress callback (virtual threads)
BatchResult result = OpenBatch.parallel()
    .parallelism(4)
    .onProgress((path, i, total) -> System.out.printf("[%d/%d] %s%n", i, total, path))
    .copyAll(files, targetDir);

// Glob-based batch delete
OpenBatch.deleteGlob(Path.of("build"), "*.class");
```

### Temporary Files

```java
// Auto-delete on close
try (AutoDeleteTempFile tmp = new AutoDeleteTempFile("prefix", ".tmp")) {
    OpenIO.writeString(tmp.path(), "temporary data");
    process(tmp.path());
} // deleted here

// Named temp file
Path tmp = OpenTempFile.create("report", ".pdf", Path.of("/tmp"));
```

### File Lock

```java
// Exclusive file lock for cross-process synchronization
OpenFileLock lock = OpenFileLock.of(Path.of("process.lock"));
lock.withLock(() -> {
    // only one process executes this block at a time
});
```

## Requirements

- Java 25+

## License

Apache License 2.0
