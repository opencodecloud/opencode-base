# OpenCode Base IO

IO utilities for file operations, resource loading, and stream processing for JDK 25+. Built on NIO.2 API with unchecked exceptions for cleaner code.

## Maven Dependency

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-io</artifactId>
    <version>1.0.0</version>
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
- File checksum calculation (MD5, SHA-256, etc.)
- Batch file operations with result tracking
- File watching for change notifications
- File locking for concurrent access
- Temporary file management with auto-cleanup
- MIME type detection
- Path utilities and file comparator
- Chunked file processing for large files
- Progress tracking for uploads and downloads
- Serialization marshalling interface

## Class Reference

### Core

| Class | Description |
|-------|-------------|
| `OpenIO` | Main facade for file and directory operations: read, write, copy, move, delete, traverse |
| `OpenStream` | Stream utility class for input/output stream operations |
| `OpenPath` | Path utility class for path manipulation and normalization |
| `OpenResource` | Resource loading utility for classpath and filesystem resources |
| `OpenMimeType` | MIME type detection and mapping utility |
| `OpenFileVisitors` | File visitor implementations for recursive delete, copy, and size calculation |

### File Operations

| Class | Description |
|-------|-------------|
| `FileReader` | Enhanced file reader with encoding detection and line processing |
| `FileWriter` | Enhanced file writer with atomic write and backup support |
| `FileCopier` | File copy utility with progress tracking and options |
| `FileComparator` | Compares files by content, size, or modification time |
| `FileWatcher` | Watches directories for file creation, modification, and deletion events |
| `ChunkedFileProcessor` | Processes large files in configurable chunks to limit memory usage |
| `MoreFiles` | Additional file utility methods beyond standard NIO.2 |

### Resource Loading

| Class | Description |
|-------|-------------|
| `Resource` | Interface for abstract resource access with input stream and metadata |
| `ResourceLoader` | Interface for loading resources by location string |
| `DefaultResourceLoader` | Default resource loader supporting classpath: and file: prefixes |
| `ClassPathResource` | Resource loaded from the classpath |
| `FileSystemResource` | Resource loaded from the filesystem |
| `UrlResource` | Resource loaded from a URL |

### Source / Sink

| Class | Description |
|-------|-------------|
| `ByteSource` | Abstract source of bytes with read operations |
| `ByteSink` | Abstract sink for bytes with write operations |
| `CharSource` | Abstract source of characters with read operations |
| `CharSink` | Abstract sink for characters with write operations |

### Stream

| Class | Description |
|-------|-------------|
| `BoundedInputStream` | Input stream wrapper that limits the number of bytes read |
| `CountingInputStream` | Input stream that counts the number of bytes read |
| `TeeInputStream` | Input stream that duplicates read data to a secondary output stream |
| `FastByteArrayOutputStream` | High-performance byte array output stream with reduced copying |

### Batch

| Class | Description |
|-------|-------------|
| `OpenBatch` | Batch file operations: bulk copy, move, delete with parallel support |
| `BatchResult` | Result record for batch operations with success/failure tracking |

### Checksum

| Class | Description |
|-------|-------------|
| `OpenChecksum` | File checksum calculation utility (MD5, SHA-256, CRC32, etc.) |
| `Checksum` | Checksum result record with algorithm and hex/byte representation |

### Lock

| Class | Description |
|-------|-------------|
| `OpenFileLock` | File-based locking for cross-process synchronization |

### Temp

| Class | Description |
|-------|-------------|
| `OpenTempFile` | Temporary file creation with configurable prefix, suffix, and directory |
| `AutoDeleteTempFile` | Temporary file that auto-deletes on close (implements AutoCloseable) |

### Progress

| Class | Description |
|-------|-------------|
| `DownloadProgress` | Progress tracking for file download operations |
| `UploadProgress` | Progress tracking for file upload operations |

### Serialization

| Class | Description |
|-------|-------------|
| `Marshaller` | Interface for object serialization/deserialization to byte streams |

### Exceptions

| Class | Description |
|-------|-------------|
| `OpenIOOperationException` | Unchecked exception for IO operation failures with operation context |

## Quick Start

```java
// Read file
String content = OpenIO.readString(Path.of("config.txt"));
List<String> lines = OpenIO.readLines(Path.of("data.csv"));
byte[] bytes = OpenIO.readBytes(Path.of("binary.dat"));

// Write file
OpenIO.writeString(Path.of("output.txt"), "Hello World");
OpenIO.writeLines(Path.of("data.csv"), List.of("a,b,c", "1,2,3"));
OpenIO.append(Path.of("log.txt"), "New entry\n");

// File operations
OpenIO.copy(source, target);
OpenIO.move(source, target);
OpenIO.deleteRecursively(Path.of("/tmp/workspace"));

// Directory traversal
try (Stream<Path> files = OpenIO.glob(Path.of("src"), "*.java")) {
    files.forEach(System.out::println);
}

try (Stream<Path> files = OpenIO.walk(Path.of("project"))) {
    long totalSize = files.filter(OpenIO::isFile).mapToLong(OpenIO::size).sum();
}

// Resource loading
Resource resource = new ClassPathResource("templates/email.html");
try (InputStream in = resource.getInputStream()) {
    // process resource
}

// Temporary file with auto-cleanup
try (var tmp = new AutoDeleteTempFile("prefix", ".tmp")) {
    OpenIO.writeString(tmp.path(), "temporary data");
    // file auto-deleted on close
}

// File checksum
Checksum checksum = OpenChecksum.sha256(Path.of("file.zip"));
System.out.println("SHA-256: " + checksum.hex());
```

## Requirements

- Java 25+

## License

Apache License 2.0
