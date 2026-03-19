# OpenCode Base ClassLoader

ClassLoader component for class loading, scanning, resource management, and metadata reading for JDK 25+.

## Features

- Unified classloader facade with default/context/isolated/hot-swap loaders
- Package and annotation scanning with configurable filters
- Resource abstraction (classpath, file, URL, JAR, byte array, input stream)
- Class and annotation metadata reading without loading classes
- Hot-swap classloader for dynamic class reloading
- Isolated classloader for plugin-style class isolation
- Resource loading with protocol-aware resolution

## Maven

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-classloader</artifactId>
    <version>1.0.0</version>
</dependency>
```

## API Overview

### Facade

| Class | Description |
|-------|-------------|
| `OpenClassLoader` | ClassLoader facade -- get default/context loader, create isolated/hot-swap loaders |
| `OpenClassPath` | Classpath scanning and resource discovery utilities |
| `OpenClassScanner` | High-level class scanning facade |
| `OpenMetadata` | Class metadata reading facade |
| `OpenResource` | Resource loading facade |

### Loaders

| Class | Description |
|-------|-------------|
| `HotSwapClassLoader` | Hot-swap classloader for dynamic class reloading (AutoCloseable) |
| `IsoClassLoader` | Isolated URLClassLoader for plugin-style class isolation (AutoCloseable) |
| `ResourceClassLoader` | ClassLoader that loads classes from byte array resources |

### Scanners

| Class | Description |
|-------|-------------|
| `ClassScanner` | Class scanner with filtering and callback support |
| `PackageScanner` | Package-level scanning for classes |
| `AnnotationScanner` | Annotation-based class scanning |
| `ScanFilter` | Filter interface for scan results |

### Resources

| Class | Description |
|-------|-------------|
| `Resource` | Resource interface (exists, readable, URL, input stream) |
| `AbstractResource` | Abstract base implementation of Resource |
| `ClassPathResource` | Classpath resource |
| `FileResource` | File system resource |
| `UrlResource` | URL-based resource |
| `JarResource` | JAR entry resource |
| `ByteArrayResource` | In-memory byte array resource |
| `InputStreamResource` | InputStream-backed resource |
| `ResourceLoader` | Protocol-aware resource loader (classpath:, file:, url:) |

### Metadata

| Class | Description |
|-------|-------------|
| `ClassMetadata` | Class-level metadata (name, modifiers, superclass, interfaces) |
| `AnnotationMetadata` | Annotation metadata (attributes, target type) |
| `FieldMetadata` | Field metadata (name, type, modifiers, annotations) |
| `MethodMetadata` | Method metadata (name, return type, parameters, annotations) |
| `MetadataReader` | Reads class metadata without loading the class |

### Exception

| Class | Description |
|-------|-------------|
| `OpenClassLoaderException` | ClassLoader operation exception |

## Quick Start

```java
import cloud.opencode.base.classloader.*;

// Get default classloader
ClassLoader cl = OpenClassLoader.getDefaultClassLoader();

// Create isolated classloader
var iso = OpenClassLoader.isolatedLoader()
    .addPath(Path.of("/plugins/my-plugin.jar"))
    .parent(ClassLoader.getSystemClassLoader())
    .build();

// Scan packages for classes
List<Class<?>> classes = OpenClassScanner.scan("com.example.model");

// Scan for annotated classes
List<Class<?>> entities = OpenClassScanner.scanWithAnnotation(
    "com.example", MyAnnotation.class);

// Load resources
Resource resource = OpenResource.classpath("config/app.properties");
InputStream is = resource.getInputStream();

// Read metadata without loading class
ClassMetadata meta = OpenMetadata.readClass("com.example.MyService");
String superClass = meta.superClassName();
```

## Requirements

- Java 25+

## License

Apache License 2.0
