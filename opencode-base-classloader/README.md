# OpenCode Base ClassLoader

ClassLoader component for class loading, scanning, resource management, and metadata reading for JDK 25+.

## Features

- Unified classloader facade with default/context/isolated/hot-swap loaders
- Package and annotation scanning with configurable filters
- Resource abstraction (classpath, file, URL, JAR, byte array, input stream)
- Class and annotation metadata reading without loading classes
- Hot-swap classloader with version rollback and change notification
- Isolated classloader with leak detection and security policy enforcement
- Resource loading with protocol-aware resolution
- v1.0.3 Build-time class index for fast startup scanning
- v1.0.3 GraalVM native-image configuration generation
- v1.0.3 ClassLoader leak detection (PhantomReference-based)
- v1.0.3 Security policy (package whitelist/blacklist, bytecode size limit, custom verifier)
- v1.0.3 Nested JAR support (Spring Boot fat JAR, WAR)
- v1.0.3 Resource file change monitoring (WatchService + debouncing)
- v1.0.3 Plugin lifecycle management (discover/load/start/stop/unload)
- v1.0.3 Scan result caching with classpath hash validation
- v1.0.3 ClassLoader diagnostics (duplicate classes, package splits, load tracing)
- v1.0.3 Leak auto-cleanup (JDBC drivers, ThreadLocals, shutdown hooks, timers)
- v1.0.3 Cross-ClassLoader ServiceLoader bridge with priority
- v1.0.3 Bytecode-level class dependency analysis with cycle detection
- v1.0.3 JAR version conflict detection

## Maven

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-classloader</artifactId>
    <version>1.0.3</version>
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
| `HotSwapClassLoader` | Hot-swap classloader for dynamic class reloading with version rollback (AutoCloseable) |
| `HotSwapListener` | Functional interface for hot-swap event notification |
| `IsoClassLoader` | Isolated URLClassLoader for plugin-style class isolation (AutoCloseable) |
| `ResourceClassLoader` | ClassLoader that loads classes from byte array resources |

### Scanners

| Class | Description |
|-------|-------------|
| `ClassScanner` | Class scanner with filtering, caching, and nested JAR support |
| `PackageScanner` | Package-level scanning for classes |
| `AnnotationScanner` | Annotation-based class scanning |
| `ScanFilter` | Filter interface with preTest optimization for scan results |
| `CachedScanResult` | Immutable cached scan result with classpath hash validation |

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
| `MethodMetadata` | Method metadata (name, return type, parameters, generic types, annotations) |
| `RecordComponentMetadata` | Record component metadata (name, type, generic type, annotations) |
| `MetadataReader` | Reads class metadata without loading the class (ClassValue cached) |

### Index (v1.0.3)

| Class | Description |
|-------|-------------|
| `ClassIndex` | Immutable build-time class index record |
| `ClassIndexEntry` | Single class entry in the index |
| `ClassIndexReader` | Loads and validates class index from classpath |
| `ClassIndexWriter` | Generates class index at build time |
| `IndexAwareScanner` | Index-based scanning (no filesystem I/O) |

### Leak Detection & Cleanup (v1.0.3)

| Class | Description |
|-------|-------------|
| `LeakDetection` | Detection level enum (DISABLED / SIMPLE / PARANOID) |
| `LeakDetector` | Global singleton detector using PhantomReference |
| `LeakReport` | Immutable leak report record |
| `LeakCleaner` | v1.0.3 Auto-cleanup utility (JDBC drivers, ThreadLocals, shutdown hooks, timers) |
| `CleanupReport` | v1.0.3 Cleanup result report record |

**LeakCleaner methods:**

| Method | Description |
|--------|-------------|
| `cleanAll(ClassLoader)` | Run all cleanups, return aggregated CleanupReport |
| `cleanJdbcDrivers(ClassLoader)` | Deregister JDBC drivers loaded by the ClassLoader |
| `cleanThreadLocals(ClassLoader)` | Clear ThreadLocal entries referencing the ClassLoader's classes |
| `cleanShutdownHooks(ClassLoader)` | Remove shutdown hooks loaded by the ClassLoader |
| `cleanTimers(ClassLoader)` | Interrupt timer threads loaded by the ClassLoader |

### Security (v1.0.3)

| Class | Description |
|-------|-------------|
| `ClassLoadingPolicy` | Immutable policy (whitelist/blacklist/limits/verifier) |
| `BytecodeVerifier` | Functional interface for bytecode verification |

### GraalVM (v1.0.3)

| Class | Description |
|-------|-------------|
| `NativeImageSupport` | GraalVM native-image environment detection |
| `NativeImageConfigGenerator` | Generates reflect-config.json and resource-config.json |
| `ReflectConfig` | reflect-config.json entry record |
| `ResourceConfig` | resource-config.json model record |

### Plugin (v1.0.3)

| Class | Description |
|-------|-------------|
| `Plugin` | Plugin SPI interface (onStart/onStop) |
| `PluginManager` | Plugin lifecycle manager (discover/load/start/stop/unload/reload) |
| `PluginDescriptor` | Plugin metadata record (from plugin.properties) |
| `PluginHandle` | Plugin handle for state queries |
| `PluginState` | Lifecycle state enum |
| `PluginContext` | Immutable plugin context |

### Resource Monitoring (v1.0.3)

| Class | Description |
|-------|-------------|
| `ResourceWatcher` | File change monitoring with WatchService |
| `ResourceEvent` | Change event record (CREATED/MODIFIED/DELETED) |
| `ResourceWatchHandle` | Closeable watch handle |
| `NestedJarHandler` | Fat JAR nested JAR extraction with reference counting |
| `NestedJarResource` | Resource implementation for nested JAR entries |

### ClassLoader Diagnostics (v1.0.3)

| Class | Description |
|-------|-------------|
| `ClassLoaderDiagnostics` | v1.0.3 Diagnostic utility (duplicate classes, package splits, load tracing) |
| `DuplicateClassReport` | v1.0.3 Duplicate class report record |
| `PackageSplitReport` | v1.0.3 Package split report record |
| `ClassLoadTrace` | v1.0.3 Class loading trace record |

**ClassLoaderDiagnostics methods:**

| Method | Description |
|--------|-------------|
| `findDuplicateClasses(ClassLoader...)` | Find classes that exist in 2+ ClassLoaders |
| `detectPackageSplits(ClassLoader...)` | Detect packages split across multiple ClassLoaders |
| `traceClassLoading(String, ClassLoader)` | Trace the delegation chain for a class |
| `findClassLocations(String, ClassLoader...)` | Find all resource URLs for a class across ClassLoaders |

### Service Bridge (v1.0.3)

| Class | Description |
|-------|-------------|
| `ServiceBridge` | v1.0.3 Cross-ClassLoader ServiceLoader bridge |
| `ServiceEntry` | v1.0.3 Service entry record with priority support |

**ServiceBridge methods:**

| Method | Description |
|--------|-------------|
| `load(Class<S>, ClassLoader...)` | Load services from multiple ClassLoaders, sorted by @Priority |
| `load(Class<S>, Collection<ClassLoader>)` | Same as above, Collection variant |
| `loadFirst(Class<S>, ClassLoader...)` | Load highest-priority service, or Optional.empty() |

### Dependency Analysis (v1.0.3)

| Class | Description |
|-------|-------------|
| `ClassDependencyAnalyzer` | v1.0.3 Bytecode-level dependency analyzer (constant pool parsing) |
| `DependencyGraph` | v1.0.3 Dependency graph record with cycle detection support |
| `CyclicDependency` | v1.0.3 Cyclic dependency path record |

**ClassDependencyAnalyzer methods:**

| Method | Description |
|--------|-------------|
| `analyze(String, ClassLoader)` | Analyze class dependencies from bytecode (excludes java.*/javax.*/jdk.*) |
| `analyze(byte[])` | Analyze dependencies from raw bytecode |
| `analyzePackage(String, ClassLoader)` | Build dependency graph for all classes in a package |
| `detectCycles(DependencyGraph)` | Detect cyclic dependencies using iterative Tarjan SCC |

**DependencyGraph methods:**

| Method | Description |
|--------|-------------|
| `dependenciesOf(String)` | Get direct dependencies of a class |
| `dependentsOf(String)` | Get classes that depend on the given class (reverse lookup) |
| `classNames()` | Get all class names in the graph |

### JAR Conflict Detection (v1.0.3)

| Class | Description |
|-------|-------------|
| `JarConflictDetector` | v1.0.3 Detect duplicate classes across JAR files |
| `ConflictReport` | v1.0.3 Conflict report record with summary |
| `JarInfo` | v1.0.3 JAR metadata record (path, version, name) |

**JarConflictDetector methods:**

| Method | Description |
|--------|-------------|
| `detect(Path...)` | Detect class conflicts across specified JAR files |
| `detect(Collection<Path>)` | Same as above, Collection variant |
| `detectInDirectory(Path)` | Detect conflicts among all *.jar files in a directory |
| `detectInDirectory(Path, String)` | Detect conflicts with glob pattern filter |

**ConflictReport methods:**

| Method | Description |
|--------|-------------|
| `hasConflicts()` | Check if any conflicts were detected |
| `conflictingClasses()` | Get all conflicting class names |
| `getConflictsForJar(Path)` | Filter conflicts involving a specific JAR |
| `summary()` | Generate human-readable bilingual summary |

### Exception

| Class | Description |
|-------|-------------|
| `OpenClassLoaderException` | ClassLoader operation exception |

## Quick Start

```java
import cloud.opencode.base.classloader.*;
import cloud.opencode.base.classloader.conflict.*;
import cloud.opencode.base.classloader.dependency.*;
import cloud.opencode.base.classloader.diagnostic.*;
import cloud.opencode.base.classloader.leak.*;
import cloud.opencode.base.classloader.loader.*;
import cloud.opencode.base.classloader.metadata.*;
import cloud.opencode.base.classloader.plugin.*;
import cloud.opencode.base.classloader.resource.*;
import cloud.opencode.base.classloader.security.*;
import cloud.opencode.base.classloader.service.*;

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

// v1.0.3: Hot-swap with version rollback and change notification
try (var loader = OpenClassLoader.hotSwapLoader(5)) { // keep 5 history versions
    loader.addListener((name, oldV, newV) ->
        System.out.println(name + ": v" + oldV + " -> v" + newV));
    Class<?> v1 = loader.loadClass("com.example.Service", bytecodeV1);
    Class<?> v2 = loader.loadClass("com.example.Service", bytecodeV2);
    Optional<Class<?>> rolledBack = loader.rollback("com.example.Service"); // back to v1
}

// v1.0.3: Isolated classloader with leak detection and security policy
var loader = OpenClassLoader.isolatedLoader()
    .addPath(Path.of("/plugins/untrusted.jar"))
    .leakDetection(LeakDetection.SIMPLE)
    .policy(ClassLoadingPolicy.builder()
        .addDeniedPackage("java.net")
        .maxBytecodeSize(1024 * 1024)
        .build())
    .build();

// v1.0.3: Plugin lifecycle management
try (var manager = PluginManager.builder()
        .pluginDir(Path.of("/plugins"))
        .leakDetection(LeakDetection.SIMPLE)
        .build()) {
    manager.discoverPlugins();
    manager.load("my-plugin");
    manager.start("my-plugin");
}

// v1.0.3: Watch resource for changes
try (var watcher = OpenResource.createWatcher()) {
    watcher.watch(Path.of("config.yml"), event ->
        System.out.println(event.type() + ": " + event.resource().getFilename()));
}

// v1.0.3: GraalVM native-image config generation
NativeImageConfigGenerator.builder()
    .addPackage("com.example.model")
    .addResourcePattern("config/.*")
    .outputDir(Path.of("META-INF/native-image"))
    .generate();

// v1.0.3: ClassLoader diagnostics
ClassLoadTrace trace = OpenClassLoader.traceClassLoading("com.example.Foo", myLoader);
System.out.println("Loaded by: " + trace.definingLoader());
List<DuplicateClassReport> duplicates = OpenClassLoader.findDuplicateClasses(loader1, loader2);

// v1.0.3: Leak cleanup on unload
CleanupReport report = LeakCleaner.cleanAll(pluginClassLoader);
System.out.println("JDBC drivers removed: " + report.jdbcDriversRemoved());

// v1.0.3: Cross-ClassLoader service discovery
List<ServiceEntry<MyService>> services = ServiceBridge.load(MyService.class, loader1, loader2);
services.forEach(e -> System.out.println(e.classLoaderName() + ": " + e.service()));

// v1.0.3: Dependency analysis
Set<String> deps = ClassDependencyAnalyzer.analyze("com.example.Foo", classLoader);
DependencyGraph graph = ClassDependencyAnalyzer.analyzePackage("com.example", classLoader);
List<CyclicDependency> cycles = ClassDependencyAnalyzer.detectCycles(graph);

// v1.0.3: JAR conflict detection
ConflictReport conflicts = JarConflictDetector.detectInDirectory(Path.of("/libs"));
if (conflicts.hasConflicts()) {
    System.out.println(conflicts.summary());
}
```

## Requirements

- Java 25+

## License

Apache License 2.0
