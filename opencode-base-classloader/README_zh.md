# OpenCode Base ClassLoader

类加载器组件，提供类加载、扫描、资源管理和元数据读取功能，适用于 JDK 25+。

## 功能特性

- 统一的类加载器门面，支持默认/上下文/隔离/热替换加载器
- 包扫描和注解扫描，支持可配置过滤器
- 资源抽象（classpath、文件、URL、JAR、字节数组、输入流）
- 无需加载类即可读取类和注解元数据
- 热替换类加载器，支持版本回退和变更通知
- 隔离类加载器，支持泄漏检测和安全策略
- 支持协议感知的资源加载
- v1.0.3 构建时类索引，加速启动扫描
- v1.0.3 GraalVM Native Image 配置生成
- v1.0.3 ClassLoader 泄漏检测（基于 PhantomReference）
- v1.0.3 安全策略（包白名单/黑名单、字节码大小限制、自定义验证器）
- v1.0.3 嵌套 JAR 支持（Spring Boot fat JAR、WAR）
- v1.0.3 资源文件变更监听（WatchService + 去抖动）
- v1.0.3 插件生命周期管理（发现/加载/启动/停止/卸载）
- v1.0.3 扫描结果缓存（classpath 哈希校验）
- v1.0.3 ClassLoader 诊断（重复类检测、包分裂检测、加载链追踪）
- v1.0.3 泄漏自动清理（JDBC 驱动、ThreadLocal、关闭钩子、定时器）
- v1.0.3 跨 ClassLoader ServiceLoader 桥接（支持优先级）
- v1.0.3 字节码级类依赖分析与循环依赖检测
- v1.0.3 JAR 版本冲突检测

## Maven 依赖

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-classloader</artifactId>
    <version>1.0.3</version>
</dependency>
```

## API 概览

### 门面

| 类名 | 说明 |
|------|------|
| `OpenClassLoader` | 类加载器门面 -- 获取默认/上下文加载器，创建隔离/热替换加载器 |
| `OpenClassPath` | 类路径扫描和资源发现工具 |
| `OpenClassScanner` | 高级类扫描门面 |
| `OpenMetadata` | 类元数据读取门面 |
| `OpenResource` | 资源加载门面 |

### 加载器

| 类名 | 说明 |
|------|------|
| `HotSwapClassLoader` | 热替换类加载器，支持动态类重载和版本回退（AutoCloseable） |
| `HotSwapListener` | 热替换事件通知函数式接口 |
| `IsoClassLoader` | 隔离 URLClassLoader，用于插件式类隔离（AutoCloseable） |
| `ResourceClassLoader` | 从字节数组资源加载类的 ClassLoader |

### 扫描器

| 类名 | 说明 |
|------|------|
| `ClassScanner` | 类扫描器，支持过滤、缓存和嵌套 JAR 扫描 |
| `PackageScanner` | 包级别的类扫描 |
| `AnnotationScanner` | 基于注解的类扫描 |
| `ScanFilter` | 扫描过滤器接口，支持 preTest 类名预过滤优化 |
| `CachedScanResult` | 不可变的缓存扫描结果（classpath 哈希校验） |

### 资源

| 类名 | 说明 |
|------|------|
| `Resource` | 资源接口（存在性、可读性、URL、输入流） |
| `AbstractResource` | Resource 的抽象基类实现 |
| `ClassPathResource` | 类路径资源 |
| `FileResource` | 文件系统资源 |
| `UrlResource` | 基于 URL 的资源 |
| `JarResource` | JAR 条目资源 |
| `ByteArrayResource` | 内存字节数组资源 |
| `InputStreamResource` | 基于 InputStream 的资源 |
| `ResourceLoader` | 协议感知的资源加载器（classpath:、file:、url:） |

### 元数据

| 类名 | 说明 |
|------|------|
| `ClassMetadata` | 类级别元数据（名称、修饰符、父类、接口） |
| `AnnotationMetadata` | 注解元数据（属性、目标类型） |
| `FieldMetadata` | 字段元数据（名称、类型、修饰符、注解） |
| `MethodMetadata` | 方法元数据（名称、返回类型、参数、泛型类型、注解） |
| `RecordComponentMetadata` | Record 组件元数据（名称、类型、泛型类型、注解） |
| `MetadataReader` | 无需加载类即可读取类元数据（ClassValue 缓存） |

### 构建时索引 (v1.0.3)

| 类名 | 说明 |
|------|------|
| `ClassIndex` | 不可变的构建时类索引记录 |
| `ClassIndexEntry` | 索引中的单条类记录 |
| `ClassIndexReader` | 从 classpath 加载和验证类索引 |
| `ClassIndexWriter` | 构建时生成类索引 |
| `IndexAwareScanner` | 基于索引的扫描（无文件系统 I/O） |

### 泄漏检测与清理 (v1.0.3)

| 类名 | 说明 |
|------|------|
| `LeakDetection` | 检测级别枚举（DISABLED / SIMPLE / PARANOID） |
| `LeakDetector` | 基于 PhantomReference 的全局单例检测器 |
| `LeakReport` | 不可变的泄漏报告记录 |
| `LeakCleaner` | v1.0.3 自动清理工具（JDBC 驱动、ThreadLocal、关闭钩子、定时器） |
| `CleanupReport` | v1.0.3 清理结果报告记录 |

**LeakCleaner 方法列表:**

| 方法 | 说明 |
|------|------|
| `cleanAll(ClassLoader)` | 运行所有清理操作，返回汇总的 CleanupReport |
| `cleanJdbcDrivers(ClassLoader)` | 注销由指定 ClassLoader 加载的 JDBC 驱动 |
| `cleanThreadLocals(ClassLoader)` | 清除引用指定 ClassLoader 类的 ThreadLocal 条目 |
| `cleanShutdownHooks(ClassLoader)` | 移除由指定 ClassLoader 加载的关闭钩子 |
| `cleanTimers(ClassLoader)` | 中断由指定 ClassLoader 加载的定时器线程 |

### 安全策略 (v1.0.3)

| 类名 | 说明 |
|------|------|
| `ClassLoadingPolicy` | 不可变的加载策略（白名单/黑名单/限额/验证器） |
| `BytecodeVerifier` | 字节码验证函数式接口 |

### GraalVM (v1.0.3)

| 类名 | 说明 |
|------|------|
| `NativeImageSupport` | GraalVM Native Image 环境检测 |
| `NativeImageConfigGenerator` | 生成 reflect-config.json 和 resource-config.json |
| `ReflectConfig` | reflect-config.json 条目记录 |
| `ResourceConfig` | resource-config.json 模型记录 |

### 插件管理 (v1.0.3)

| 类名 | 说明 |
|------|------|
| `Plugin` | 插件 SPI 接口（onStart/onStop） |
| `PluginManager` | 插件生命周期管理器（发现/加载/启动/停止/卸载/重载） |
| `PluginDescriptor` | 插件元数据记录（来自 plugin.properties） |
| `PluginHandle` | 插件句柄，用于状态查询 |
| `PluginState` | 生命周期状态枚举 |
| `PluginContext` | 不可变的插件上下文 |

### 资源监听 (v1.0.3)

| 类名 | 说明 |
|------|------|
| `ResourceWatcher` | 基于 WatchService 的文件变更监听器 |
| `ResourceEvent` | 变更事件记录（CREATED/MODIFIED/DELETED） |
| `ResourceWatchHandle` | 可关闭的监听句柄 |
| `NestedJarHandler` | Fat JAR 嵌套 JAR 解压管理（引用计数） |
| `NestedJarResource` | 嵌套 JAR 条目资源实现 |

### ClassLoader 诊断 (v1.0.3)

| 类名 | 说明 |
|------|------|
| `ClassLoaderDiagnostics` | v1.0.3 诊断工具（重复类检测、包分裂检测、加载追踪） |
| `DuplicateClassReport` | v1.0.3 重复类报告记录 |
| `PackageSplitReport` | v1.0.3 包分裂报告记录 |
| `ClassLoadTrace` | v1.0.3 类加载追踪记录 |

**ClassLoaderDiagnostics 方法列表:**

| 方法 | 说明 |
|------|------|
| `findDuplicateClasses(ClassLoader...)` | 查找在 2 个以上 ClassLoader 中存在的重复类 |
| `detectPackageSplits(ClassLoader...)` | 检测跨多个 ClassLoader 拆分的包 |
| `traceClassLoading(String, ClassLoader)` | 追踪类加载的委托链路 |
| `findClassLocations(String, ClassLoader...)` | 在多个 ClassLoader 中查找类的所有资源 URL |

### 服务桥接 (v1.0.3)

| 类名 | 说明 |
|------|------|
| `ServiceBridge` | v1.0.3 跨 ClassLoader ServiceLoader 桥接 |
| `ServiceEntry` | v1.0.3 带优先级的服务条目记录 |

**ServiceBridge 方法列表:**

| 方法 | 说明 |
|------|------|
| `load(Class<S>, ClassLoader...)` | 从多个 ClassLoader 加载服务，按 @Priority 排序 |
| `load(Class<S>, Collection<ClassLoader>)` | 同上，Collection 版本 |
| `loadFirst(Class<S>, ClassLoader...)` | 加载最高优先级的服务，或返回 Optional.empty() |

### 依赖分析 (v1.0.3)

| 类名 | 说明 |
|------|------|
| `ClassDependencyAnalyzer` | v1.0.3 字节码级依赖分析器（常量池解析） |
| `DependencyGraph` | v1.0.3 依赖图记录（支持循环检测） |
| `CyclicDependency` | v1.0.3 循环依赖路径记录 |

**ClassDependencyAnalyzer 方法列表:**

| 方法 | 说明 |
|------|------|
| `analyze(String, ClassLoader)` | 从字节码分析类依赖（排除 java.*/javax.*/jdk.*） |
| `analyze(byte[])` | 从原始字节码分析依赖 |
| `analyzePackage(String, ClassLoader)` | 为包中所有类构建依赖图 |
| `detectCycles(DependencyGraph)` | 使用迭代式 Tarjan SCC 算法检测循环依赖 |

**DependencyGraph 方法列表:**

| 方法 | 说明 |
|------|------|
| `dependenciesOf(String)` | 获取一个类的直接依赖 |
| `dependentsOf(String)` | 获取依赖于给定类的类（反向查找） |
| `classNames()` | 获取图中所有类名 |

### JAR 冲突检测 (v1.0.3)

| 类名 | 说明 |
|------|------|
| `JarConflictDetector` | v1.0.3 检测 JAR 文件间的重复类 |
| `ConflictReport` | v1.0.3 冲突报告记录（含摘要） |
| `JarInfo` | v1.0.3 JAR 元数据记录（路径、版本、名称） |

**JarConflictDetector 方法列表:**

| 方法 | 说明 |
|------|------|
| `detect(Path...)` | 检测指定 JAR 文件之间的类冲突 |
| `detect(Collection<Path>)` | 同上，Collection 版本 |
| `detectInDirectory(Path)` | 检测目录中所有 *.jar 文件的冲突 |
| `detectInDirectory(Path, String)` | 使用 glob 模式过滤检测 |

**ConflictReport 方法列表:**

| 方法 | 说明 |
|------|------|
| `hasConflicts()` | 检查是否检测到冲突 |
| `conflictingClasses()` | 获取所有冲突类名集合 |
| `getConflictsForJar(Path)` | 获取涉及特定 JAR 的冲突 |
| `summary()` | 生成人类可读的双语摘要 |

### 异常

| 类名 | 说明 |
|------|------|
| `OpenClassLoaderException` | 类加载器操作异常 |

## 快速开始

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

// 获取默认类加载器
ClassLoader cl = OpenClassLoader.getDefaultClassLoader();

// 创建隔离类加载器
var iso = OpenClassLoader.isolatedLoader()
    .addPath(Path.of("/plugins/my-plugin.jar"))
    .parent(ClassLoader.getSystemClassLoader())
    .build();

// 扫描包中的类
List<Class<?>> classes = OpenClassScanner.scan("com.example.model");

// 扫描带注解的类
List<Class<?>> entities = OpenClassScanner.scanWithAnnotation(
    "com.example", MyAnnotation.class);

// 加载资源
Resource resource = OpenResource.classpath("config/app.properties");
InputStream is = resource.getInputStream();

// 无需加载类即可读取元数据
ClassMetadata meta = OpenMetadata.readClass("com.example.MyService");
String superClass = meta.superClassName();

// v1.0.3: 热替换类加载器，支持版本回退和变更通知
try (var loader = OpenClassLoader.hotSwapLoader(5)) { // 保留 5 个历史版本
    loader.addListener((name, oldV, newV) ->
        System.out.println(name + ": v" + oldV + " -> v" + newV));
    Class<?> v1 = loader.loadClass("com.example.Service", bytecodeV1);
    Class<?> v2 = loader.loadClass("com.example.Service", bytecodeV2);
    Optional<Class<?>> rolledBack = loader.rollback("com.example.Service"); // 回退到 v1
}

// v1.0.3: 带泄漏检测和安全策略的隔离类加载器
var loader = OpenClassLoader.isolatedLoader()
    .addPath(Path.of("/plugins/untrusted.jar"))
    .leakDetection(LeakDetection.SIMPLE)
    .policy(ClassLoadingPolicy.builder()
        .addDeniedPackage("java.net")
        .maxBytecodeSize(1024 * 1024)
        .build())
    .build();

// v1.0.3: 插件生命周期管理
try (var manager = PluginManager.builder()
        .pluginDir(Path.of("/plugins"))
        .leakDetection(LeakDetection.SIMPLE)
        .build()) {
    manager.discoverPlugins();
    manager.load("my-plugin");
    manager.start("my-plugin");
}

// v1.0.3: 监听资源变更
try (var watcher = OpenResource.createWatcher()) {
    watcher.watch(Path.of("config.yml"), event ->
        System.out.println(event.type() + ": " + event.resource().getFilename()));
}

// v1.0.3: GraalVM Native Image 配置生成
NativeImageConfigGenerator.builder()
    .addPackage("com.example.model")
    .addResourcePattern("config/.*")
    .outputDir(Path.of("META-INF/native-image"))
    .generate();

// v1.0.3: ClassLoader 诊断
ClassLoadTrace trace = OpenClassLoader.traceClassLoading("com.example.Foo", myLoader);
System.out.println("加载者: " + trace.definingLoader());
List<DuplicateClassReport> duplicates = OpenClassLoader.findDuplicateClasses(loader1, loader2);

// v1.0.3: 卸载时清理泄漏源
CleanupReport report = LeakCleaner.cleanAll(pluginClassLoader);
System.out.println("已移除 JDBC 驱动: " + report.jdbcDriversRemoved());

// v1.0.3: 跨 ClassLoader 服务发现
List<ServiceEntry<MyService>> services = ServiceBridge.load(MyService.class, loader1, loader2);
services.forEach(e -> System.out.println(e.classLoaderName() + ": " + e.service()));

// v1.0.3: 依赖分析
Set<String> deps = ClassDependencyAnalyzer.analyze("com.example.Foo", classLoader);
DependencyGraph graph = ClassDependencyAnalyzer.analyzePackage("com.example", classLoader);
List<CyclicDependency> cycles = ClassDependencyAnalyzer.detectCycles(graph);

// v1.0.3: JAR 冲突检测
ConflictReport conflicts = JarConflictDetector.detectInDirectory(Path.of("/libs"));
if (conflicts.hasConflicts()) {
    System.out.println(conflicts.summary());
}
```

## 环境要求

- Java 25+

## 许可证

Apache License 2.0
