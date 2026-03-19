# ClassLoader 组件方案

## 1. 组件概述

### 1.1 设计目标

ClassLoader 组件提供类加载、类扫描、资源管理和类元数据读取等功能。核心设计理念:

- **隔离类加载**: 支持独立的类加载环境，避免类冲突
- **热替换**: 支持运行时类动态替换，无需重启
- **延迟加载**: 读取类元数据无需初始化类
- **资源抽象**: 统一的资源访问接口，支持 classpath/file/url/jar 等多种来源
- **高性能扫描**: 支持包扫描、注解扫描，支持虚拟线程并行

### 1.2 架构概览

```
┌─────────────────────────────────────────────────────────────────┐
│                         应用层                                   │
│            (插件系统 / 热部署 / 动态加载)                         │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                       门面层 (Facade)                            │
│  ┌─────────────┐  ┌───────────────┐  ┌───────────────────────┐  │
│  │OpenClassLoader│ │OpenClassScanner│ │    OpenResource      │  │
│  │  类加载门面   │  │   扫描门面     │  │     资源门面         │  │
│  └─────────────┘  └───────────────┘  └───────────────────────┘  │
│  ┌─────────────┐  ┌───────────────┐                             │
│  │OpenClassPath│  │ OpenMetadata  │                             │
│  │ classpath   │  │  元数据门面   │                             │
│  └─────────────┘  └───────────────┘                             │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                       实现层 (Implementation)                    │
│  ┌───────────────────┐  ┌──────────────────┐  ┌──────────────┐  │
│  │     loader/       │  │    scanner/      │  │  resource/   │  │
│  │ IsoClassLoader    │  │ ClassScanner     │  │ Resource     │  │
│  │ HotSwapClassLoader│  │ PackageScanner   │  │ FileResource │  │
│  │ResourceClassLoader│  │AnnotationScanner │  │ JarResource  │  │
│  └───────────────────┘  └──────────────────┘  └──────────────┘  │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                      metadata/                            │  │
│  │  ClassMetadata / MethodMetadata / FieldMetadata / Reader  │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### 1.3 模块依赖

```
classloader 模块
├── 依赖: core (基础工具)
└── 外部依赖: 无 (纯 JDK 25 实现)
```
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-classloader</artifactId>
    <version>${version}</version>
</dependency>
```

---

## 2. 包结构设计

```
cloud.opencode.base.classloader
├── OpenClassLoader.java          # 类加载器门面
├── OpenClassPath.java            # ClassPath 工具门面
├── OpenClassScanner.java         # 类扫描器门面
├── OpenResource.java             # 资源加载门面
├── OpenMetadata.java             # 元数据读取门面
├── loader/                       # 类加载器实现
│   ├── IsoClassLoader.java       # 隔离类加载器
│   ├── HotSwapClassLoader.java   # 热替换类加载器
│   └── ResourceClassLoader.java  # 资源类加载器
├── scanner/                      # 类扫描器
│   ├── ClassScanner.java         # 类扫描器实现
│   ├── PackageScanner.java       # 包扫描器实现
│   ├── AnnotationScanner.java    # 注解扫描器实现
│   └── ScanFilter.java           # 扫描过滤器接口
├── resource/                     # 资源抽象
│   ├── Resource.java             # 资源接口
│   ├── AbstractResource.java     # 资源抽象基类
│   ├── ClassPathResource.java    # classpath 资源
│   ├── FileResource.java         # 文件资源
│   ├── UrlResource.java          # URL 资源
│   ├── JarResource.java          # JAR 资源
│   ├── ByteArrayResource.java    # 字节数组资源
│   ├── InputStreamResource.java  # 输入流资源
│   └── ResourceLoader.java       # 资源加载器
├── metadata/                     # 类元数据
│   ├── ClassMetadata.java        # 类元数据
│   ├── MethodMetadata.java       # 方法元数据
│   ├── FieldMetadata.java        # 字段元数据
│   ├── AnnotationMetadata.java   # 注解元数据
│   └── MetadataReader.java       # 元数据读取器
└── exception/                    # 异常
    └── OpenClassLoaderException.java # 统一异常
```

---

## 3. 门面类 API

### 3.1 OpenClassLoader — 类加载器门面

统一的类加载器操作入口，提供类加载器获取、创建、类加载等功能。

```java
public final class OpenClassLoader {

    // ==================== 获取类加载器 ====================

    /** 获取默认类加载器（优先线程上下文，其次当前类，最后系统类加载器） */
    public static ClassLoader getDefaultClassLoader();

    /** 获取当前线程上下文类加载器 */
    public static ClassLoader getContextClassLoader();

    /** 获取指定类的类加载器 */
    public static ClassLoader getClassLoader(Class<?> clazz);

    // ==================== 创建类加载器 ====================

    /** 创建隔离类加载器 Builder */
    public static IsoClassLoader.Builder isolatedLoader();

    /** 创建热替换类加载器 */
    public static HotSwapClassLoader hotSwapLoader();
    public static HotSwapClassLoader hotSwapLoader(ClassLoader parent);

    /** 创建资源类加载器 */
    public static ResourceClassLoader resourceLoader(Path... resourcePaths);

    // ==================== 类加载操作 ====================

    /** 使用指定类加载器执行操作（带返回值） */
    public static <T> T withClassLoader(ClassLoader classLoader, Supplier<T> supplier);

    /** 使用指定类加载器执行操作（无返回值） */
    public static void withClassLoader(ClassLoader classLoader, Runnable runnable);

    /** 检查类在指定加载器中是否可见 */
    public static boolean isVisible(Class<?> clazz, ClassLoader classLoader);

    /** 获取类加载器继承层次 */
    public static List<ClassLoader> getClassLoaderHierarchy(ClassLoader classLoader);

    /** 查找定义指定类的类加载器 */
    public static Optional<ClassLoader> findDefiningClassLoader(String className);

    /** 尝试加载类 */
    public static Optional<Class<?>> loadClass(String className);
    public static Optional<Class<?>> loadClass(String className, ClassLoader classLoader);
}
```

**使用示例:**

```java
// 获取默认类加载器
ClassLoader cl = OpenClassLoader.getDefaultClassLoader();

// 创建隔离类加载器
IsoClassLoader iso = OpenClassLoader.isolatedLoader()
    .fromJar("/path/to/plugin.jar")
    .addIsolatedPackage("com.plugin")
    .build();

// 临时切换类加载器执行代码
String result = OpenClassLoader.withClassLoader(customLoader, () -> {
    return someService.doSomething();
});

// 安全加载类
Optional<Class<?>> clazz = OpenClassLoader.loadClass("com.example.MyClass");
```

### 3.2 OpenClassPath — ClassPath 工具门面

提供 ClassPath 相关的工具方法，包括路径查询、转换、资源查找等。

```java
public final class OpenClassPath {

    /** 获取当前 classpath 的所有 URL */
    public static List<URL> getClassPathUrls();

    /** 获取当前 classpath 的所有路径条目 */
    public static List<Path> getClassPathEntries();

    /** 获取运行时 classpath 字符串 */
    public static String getClassPath();

    /** 类名转资源路径: com.example.MyClass -> com/example/MyClass.class */
    public static String classNameToResourcePath(String className);

    /** 资源路径转类名: com/example/MyClass.class -> com.example.MyClass */
    public static String resourcePathToClassName(String resourcePath);

    /** 包名转资源路径: com.example -> com/example */
    public static String packageNameToResourcePath(String packageName);

    /** 获取指定包的所有资源 URL */
    public static List<URL> getPackageResources(String packageName);

    /** 查找单个资源 */
    public static Optional<URL> findResource(String resourceName);

    /** 查找所有匹配资源 */
    public static List<URL> findResources(String resourceName);

    /** 检查资源是否存在 */
    public static boolean resourceExists(String resourceName);

    /** 检查类是否存在于 classpath */
    public static boolean classExists(String className);
}
```

**使用示例:**

```java
// 获取所有 classpath URL
List<URL> urls = OpenClassPath.getClassPathUrls();

// 类名/资源路径互转
String path = OpenClassPath.classNameToResourcePath("com.example.MyClass");
// 结果: "com/example/MyClass.class"

// 查找资源
Optional<URL> url = OpenClassPath.findResource("config.yml");

// 检查类是否存在
boolean exists = OpenClassPath.classExists("com.example.SomeClass");
```

### 3.3 OpenClassScanner — 类扫描器门面

提供类扫描的快捷入口，支持按注解、子类型、接口实现等方式扫描。

```java
public final class OpenClassScanner {

    /** 创建扫描器 */
    public static ClassScanner of(String basePackage);
    public static ClassScanner of(String... basePackages);
    public static ClassScanner of(ClassLoader classLoader, String basePackage);

    /** 快速扫描带指定注解的类 */
    public static Set<Class<?>> scanWithAnnotation(
        String basePackage, Class<? extends Annotation> annotation);

    /** 快速扫描指定类型的子类 */
    public static <T> Set<Class<? extends T>> scanSubTypes(
        String basePackage, Class<T> superType);

    /** 快速扫描实现指定接口的类 */
    public static <T> Set<Class<? extends T>> scanImplementations(
        String basePackage, Class<T> interfaceType);

    /** 创建注解扫描器 */
    public static AnnotationScanner annotationScanner(String basePackage);

    /** 创建包扫描器 */
    public static PackageScanner packageScanner(String basePackage);
}
```

**使用示例:**

```java
// 快速扫描带 @Service 注解的类
Set<Class<?>> services = OpenClassScanner.scanWithAnnotation("com.example", Service.class);

// 扫描 Plugin 接口的所有实现类
Set<Class<? extends Plugin>> plugins =
    OpenClassScanner.scanImplementations("com.example", Plugin.class);

// 使用扫描器详细配置
Set<Class<?>> classes = OpenClassScanner.of("com.example")
    .includeJars(true)
    .includeInnerClasses(false)
    .parallel(true)
    .excludePackage("com.example.test")
    .scanWithAnnotation(Component.class);
```

### 3.4 OpenResource — 资源加载门面

统一的资源加载入口，支持 classpath/file/url 等多种协议。

```java
public final class OpenResource {

    /** 通过位置字符串获取资源（支持 classpath:、file:、url: 协议） */
    public static Resource get(String location);

    /** 加载 classpath 资源 */
    public static Resource classpath(String path);

    /** 加载文件系统资源 */
    public static Resource file(String path);

    /** 加载 URL 资源 */
    public static Resource url(String url);

    /** 从字节数组创建资源 */
    public static Resource bytes(byte[] content);
    public static Resource bytes(byte[] content, String description);

    /** 获取多个资源（支持 classpath*: 通配符） */
    public static List<Resource> getAll(String locationPattern);

    /** 检查资源是否存在 */
    public static boolean exists(String location);

    /** 快速读取资源为字符串（UTF-8） */
    public static String readString(String location) throws IOException;

    /** 快速读取资源为字节数组 */
    public static byte[] readBytes(String location) throws IOException;

    /** 快速读取资源的所有行 */
    public static List<String> readLines(String location) throws IOException;

    /** 创建资源加载器 */
    public static ResourceLoader loader();
    public static ResourceLoader loader(ClassLoader classLoader);
}
```

**使用示例:**

```java
// 加载 classpath 资源
Resource config = OpenResource.get("classpath:config.yml");
String content = config.getString();

// 快速读取
String text = OpenResource.readString("classpath:config.yml");
byte[] data = OpenResource.readBytes("file:/etc/app/config.bin");

// 加载多个资源（通配符）
List<Resource> xmlFiles = OpenResource.getAll("classpath*:META-INF/*.xml");

// 从字节数组创建内存资源
Resource memResource = OpenResource.bytes(myData, "in-memory config");
```

### 3.5 OpenMetadata — 元数据读取门面

不加载类即可读取类的元数据（类名、方法、字段、注解等）。

```java
public final class OpenMetadata {

    /** 通过类名读取类元数据 */
    public static ClassMetadata read(String className);

    /** 通过 Class 对象读取类元数据 */
    public static ClassMetadata read(Class<?> clazz);

    /** 通过 Resource 读取类元数据 */
    public static ClassMetadata read(Resource resource);

    /** 通过字节码读取类元数据 */
    public static ClassMetadata read(byte[] bytecode);

    /** 批量读取指定包下所有类元数据 */
    public static List<ClassMetadata> readPackage(String packageName);

    /** 批量读取（带类名过滤器） */
    public static List<ClassMetadata> readPackage(
        String packageName, Predicate<String> classNameFilter);

    /** 获取 MetadataReader 类（用于高级场景） */
    public static Class<MetadataReader> reader();
}
```

**使用示例:**

```java
// 读取类元数据
ClassMetadata metadata = OpenMetadata.read("com.example.MyClass");
ClassMetadata metadata2 = OpenMetadata.read(MyClass.class);

// 检查注解
if (metadata.hasAnnotation("org.springframework.stereotype.Service")) {
    System.out.println("This is a service class");
}

// 获取方法信息
for (MethodMetadata method : metadata.methods()) {
    if (method.hasAnnotation("org.springframework.web.bind.annotation.GetMapping")) {
        System.out.println("GET endpoint: " + method.methodName());
    }
}

// 批量读取包下所有类
List<ClassMetadata> allClasses = OpenMetadata.readPackage("com.example");

// 带过滤器的批量读取
List<ClassMetadata> filtered = OpenMetadata.readPackage("com.example",
    name -> !name.endsWith("Test"));
```

---

## 4. 类加载器

### 4.1 IsoClassLoader — 隔离类加载器

支持独立的类加载环境，适用于插件系统、模块隔离等场景。继承 `URLClassLoader`，实现 `AutoCloseable`。

```java
public class IsoClassLoader extends URLClassLoader implements AutoCloseable {

    // ==================== 加载策略 ====================

    public enum LoadingStrategy {
        PARENT_FIRST,    // 父优先（默认 JVM 行为）
        CHILD_FIRST,     // 子优先（隔离模式）
        PARENT_ONLY,     // 仅父加载器
        CHILD_ONLY       // 仅本加载器
    }

    // ==================== 静态工厂 ====================

    /** 从 JAR 文件创建 Builder */
    public static Builder fromJar(String jarPath);
    public static Builder fromJar(Path jarPath);

    /** 从目录创建 Builder */
    public static Builder fromDirectory(Path directory);

    /** 从 URL 创建 Builder */
    public static Builder fromUrls(URL... urls);

    // ==================== 类加载 ====================

    /** 加载类（遵循配置的加载策略） */
    public Class<?> loadClass(String name) throws ClassNotFoundException;

    /** 强制从本加载器加载 */
    public Class<?> loadClassLocally(String name) throws ClassNotFoundException;

    /** 检查类是否可加载 */
    public boolean canLoad(String className);

    // ==================== 资源访问 ====================

    public URL getResource(String name);
    public Enumeration<URL> getResources(String name) throws IOException;
    public InputStream getResourceAsStream(String name);

    // ==================== 查询与生命周期 ====================

    /** 获取已加载的类名集合 */
    public Set<String> getLoadedClassNames();

    /** 关闭类加载器 */
    public void close();

    /** 是否已关闭 */
    public boolean isClosed();

    // ==================== Builder ====================

    public static class Builder {
        public Builder addUrl(URL url);
        public Builder addPath(Path path);
        public Builder addIsolatedPackage(String packageName);
        public Builder addIsolatedPackages(String... packageNames);
        public Builder addSharedPackage(String packageName);
        public Builder addSharedPackages(String... packageNames);
        public Builder parent(ClassLoader parent);
        public Builder loadingStrategy(LoadingStrategy strategy);
        public IsoClassLoader build();
    }
}
```

**使用示例:**

```java
// 创建隔离类加载器（子优先模式）
try (IsoClassLoader loader = IsoClassLoader.fromJar("/path/to/plugin.jar")
        .addIsolatedPackage("com.plugin")
        .addSharedPackage("com.shared.api")
        .loadingStrategy(IsoClassLoader.LoadingStrategy.CHILD_FIRST)
        .build()) {

    Class<?> pluginClass = loader.loadClass("com.plugin.MyPlugin");
    Object plugin = pluginClass.getDeclaredConstructor().newInstance();

    // 隔离验证：插件类的加载器是 IsoClassLoader
    assert pluginClass.getClassLoader() == loader;
}
```

### 4.2 HotSwapClassLoader — 热替换类加载器

支持运行时动态替换类字节码，适用于开发时热部署、插件热更新等场景。实现 `AutoCloseable`。

```java
public class HotSwapClassLoader extends ClassLoader implements AutoCloseable {

    // ==================== 构造方法 ====================

    public HotSwapClassLoader();
    public HotSwapClassLoader(ClassLoader parent);

    // ==================== 静态工厂 ====================

    public static HotSwapClassLoader create();
    public static HotSwapClassLoader create(ClassLoader parent);

    // ==================== 类操作 ====================

    /** 从字节码加载类 */
    public Class<?> loadClass(String name, byte[] bytecode);

    /** 从 class 文件重新加载类 */
    public Class<?> reloadClass(String name, Path classFile);

    /** 加载类（委派机制） */
    public Class<?> loadClass(String name) throws ClassNotFoundException;

    /** 获取类版本号 */
    public int getVersion(String className);

    /** 获取所有已加载类名 */
    public Set<String> getLoadedClassNames();

    /** 判断类是否已加载 */
    public boolean isLoaded(String className);

    /** 获取类字节码 */
    public Optional<byte[]> getBytecode(String className);

    /** 卸载指定类 */
    public void unloadClass(String className);

    /** 清理所有已加载类 */
    public void clear();

    /** 关闭 */
    public void close();

    /** 是否已关闭 */
    public boolean isClosed();
}
```

**使用示例:**

```java
HotSwapClassLoader loader = HotSwapClassLoader.create();

// 从字节码加载类
Class<?> v1 = loader.loadClass("com.example.Service", bytecodeV1);
Object instance = v1.getDeclaredConstructor().newInstance();

// 热替换：从 class 文件重新加载
Class<?> v2 = loader.reloadClass("com.example.Service", Path.of("Service.class"));

// 版本号自动递增
int version = loader.getVersion("com.example.Service"); // 2

// 使用完毕后关闭
loader.close();
```

### 4.3 ResourceClassLoader — 资源类加载器

专注于资源加载的类加载器，支持动态添加/移除资源路径。

```java
public class ResourceClassLoader extends ClassLoader {

    // ==================== 构造方法 ====================

    public ResourceClassLoader();
    public ResourceClassLoader(ClassLoader parent);

    // ==================== 静态工厂 ====================

    public static ResourceClassLoader create(Path... resourcePaths);
    public static ResourceClassLoader create(ClassLoader parent, Path... resourcePaths);

    // ==================== 资源路径管理 ====================

    /** 添加资源路径 */
    public void addResourcePath(Path path);

    /** 移除资源路径 */
    public void removeResourcePath(Path path);

    /** 获取所有资源路径 */
    public List<Path> getResourcePaths();

    /** 清空所有资源路径 */
    public void clearResourcePaths();

    // ==================== 资源操作 ====================

    public URL getResource(String name);
    public InputStream getResourceAsStream(String name);
    public Enumeration<URL> getResources(String name) throws IOException;

    /** 刷新资源缓存 */
    public void refresh();

    /** 检查资源是否存在 */
    public boolean resourceExists(String name);

    /** 列出指定路径下的资源名 */
    public List<String> listResources(String path);
}
```

**使用示例:**

```java
ResourceClassLoader loader = ResourceClassLoader.create(
    Path.of("/app/resources"),
    Path.of("/app/config")
);

URL resource = loader.getResource("config.yml");
boolean exists = loader.resourceExists("application.properties");
List<String> files = loader.listResources("templates/");

// 动态添加路径
loader.addResourcePath(Path.of("/app/extra-resources"));
loader.refresh();
```

---

## 5. 类扫描器

### 5.1 ClassScanner — 类扫描器

扫描指定包下的类，支持链式配置和多种过滤方式。

```java
public class ClassScanner {

    // ==================== 静态工厂 ====================

    public static ClassScanner of(String basePackage);
    public static ClassScanner of(String... basePackages);
    public static ClassScanner of(ClassLoader classLoader, String basePackage);

    // ==================== 配置 ====================

    /** 是否扫描 JAR 文件 */
    public ClassScanner includeJars(boolean include);

    /** 是否包含内部类 */
    public ClassScanner includeInnerClasses(boolean include);

    /** 是否并行扫描 */
    public ClassScanner parallel(boolean parallel);

    /** 排除指定包 */
    public ClassScanner excludePackage(String packageName);

    /** 设置类加载器 */
    public ClassScanner classLoader(ClassLoader classLoader);

    // ==================== 扫描方法 ====================

    /** 扫描所有类 */
    public Set<Class<?>> scan();

    /** 使用过滤器扫描 */
    public Set<Class<?>> scan(ScanFilter filter);

    /** 扫描带指定注解的类 */
    public Set<Class<?>> scanWithAnnotation(Class<? extends Annotation> annotation);

    /** 扫描指定类型的子类 */
    public <T> Set<Class<? extends T>> scanSubTypes(Class<T> superType);

    /** 扫描实现指定接口的类 */
    public <T> Set<Class<? extends T>> scanImplementations(Class<T> interfaceType);

    // ==================== 流式 API ====================

    /** 返回类流（懒加载） */
    public Stream<Class<?>> stream();

    /** 返回类名流（不加载类） */
    public Stream<String> classNameStream();
}
```

### 5.2 AnnotationScanner — 注解扫描器

专注于注解扫描，可扫描类、方法、字段、构造器上的注解。

```java
public class AnnotationScanner {

    public static AnnotationScanner of(String basePackage);
    public AnnotationScanner classLoader(ClassLoader classLoader);
    public AnnotationScanner includeInnerClasses(boolean include);

    /** 扫描带指定注解的类 */
    public Set<Class<?>> scanClasses(Class<? extends Annotation> annotation);

    /** 扫描带指定注解的方法 */
    public Set<Method> scanMethods(Class<? extends Annotation> annotation);

    /** 扫描带指定注解的字段 */
    public Set<Field> scanFields(Class<? extends Annotation> annotation);

    /** 扫描带指定注解的构造器 */
    public Set<Constructor<?>> scanConstructors(Class<? extends Annotation> annotation);

    /** 扫描元注解（注解的注解） */
    public Set<Class<?>> scanMetaAnnotated(Class<? extends Annotation> metaAnnotation);

    /** 扫描方法参数上的注解，返回 Method -> 参数索引列表 */
    public Map<Method, List<Integer>> scanParameters(Class<? extends Annotation> annotation);

    /** 扫描包含被注解方法的类 */
    public Set<Class<?>> scanClassesWithAnnotatedMethods(Class<? extends Annotation> annotation);

    /** 扫描包含被注解字段的类 */
    public Set<Class<?>> scanClassesWithAnnotatedFields(Class<? extends Annotation> annotation);
}
```

**使用示例:**

```java
AnnotationScanner scanner = AnnotationScanner.of("com.example");

// 扫描 @Service 类
Set<Class<?>> services = scanner.scanClasses(Service.class);

// 扫描 @GetMapping 方法
Set<Method> getMethods = scanner.scanMethods(GetMapping.class);

// 扫描带 @Autowired 字段的类
Set<Class<?>> classes = scanner.scanClassesWithAnnotatedFields(Autowired.class);
```

### 5.3 PackageScanner — 包扫描器

扫描包结构，查找子包和类。

```java
public class PackageScanner {

    public static PackageScanner of(String basePackage);
    public PackageScanner classLoader(ClassLoader classLoader);
    public PackageScanner useVirtualThreads(boolean useVirtualThreads);

    /** 查找所有子包 */
    public Set<String> findSubPackages();

    /** 查找所有类 */
    public List<Class<?>> findClasses();
    public List<Class<?>> findClasses(boolean recursive);

    /** 查找类名（不加载类） */
    public List<String> findClassNames(boolean recursive);

    /** 包是否存在 */
    public boolean exists();

    /** 获取基础包名 */
    public String getBasePackage();
}
```

### 5.4 ScanFilter — 扫描过滤器

函数式接口，提供丰富的预定义过滤器和组合操作。

```java
@FunctionalInterface
public interface ScanFilter {
    boolean test(Class<?> clazz);

    // 组合操作
    default ScanFilter and(ScanFilter other);
    default ScanFilter or(ScanFilter other);
    default ScanFilter negate();

    // 预定义过滤器
    static ScanFilter and(ScanFilter... filters);
    static ScanFilter or(ScanFilter... filters);
    static ScanFilter not(ScanFilter filter);
    static ScanFilter isConcrete();
    static ScanFilter isInterface();
    static ScanFilter isAbstract();
    static ScanFilter isEnum();
    static ScanFilter isRecord();
    static ScanFilter isAnnotation();
    static ScanFilter hasAnnotation(Class<? extends Annotation> annotation);
    static ScanFilter isSubTypeOf(Class<?> superType);
    static ScanFilter implementsInterface(Class<?> interfaceType);
    static ScanFilter nameStartsWith(String prefix);
    static ScanFilter nameEndsWith(String suffix);
    static ScanFilter nameMatches(String regex);
    static ScanFilter inPackage(String packageName);
    static ScanFilter hasModifier(int modifier);
    static ScanFilter isPublic();
}
```

**使用示例:**

```java
// 组合过滤器
Set<Class<?>> classes = ClassScanner.of("com.example")
    .scan(ScanFilter.and(
        ScanFilter.isConcrete(),
        ScanFilter.hasAnnotation(Component.class),
        ScanFilter.not(ScanFilter.nameEndsWith("Test"))
    ));

// 扫描所有 Record 类
Set<Class<?>> records = ClassScanner.of("com.example")
    .scan(ScanFilter.isRecord());
```

---

## 6. 资源抽象

### 6.1 Resource — 资源接口

统一的资源访问抽象，所有资源类型都实现该接口。

```java
public interface Resource {

    /** 资源是否存在 */
    boolean exists();

    /** 是否可读 */
    boolean isReadable();

    /** 是否为文件资源 */
    boolean isFile();

    /** 获取输入流 */
    InputStream getInputStream() throws IOException;

    /** 获取 URL */
    URL getUrl() throws IOException;

    /** 获取 URI */
    URI getUri() throws IOException;

    /** 获取文件对象（仅文件资源） */
    Optional<File> getFile();

    /** 获取路径（仅文件资源） */
    Optional<Path> getPath();

    /** 获取内容长度 */
    long contentLength() throws IOException;

    /** 获取最后修改时间 */
    long lastModified() throws IOException;

    /** 获取文件名 */
    String getFilename();

    /** 获取描述信息 */
    String getDescription();

    /** 创建相对资源 */
    Resource createRelative(String relativePath) throws IOException;

    // ==================== 便捷读取方法（default） ====================

    /** 读取为字节数组 */
    default byte[] getBytes() throws IOException;

    /** 读取为字符串（UTF-8） */
    default String getString() throws IOException;

    /** 读取为字符串（指定编码） */
    default String getString(Charset charset) throws IOException;

    /** 读取所有行 */
    default List<String> readLines() throws IOException;
}
```

### 6.2 具体资源实现

| 类名 | 描述 | 构造参数 |
|------|------|----------|
| `ClassPathResource` | 从 classpath 加载 | `(String path)`, `(String path, ClassLoader)` |
| `FileResource` | 从文件系统加载 | `(String path)`, `(File file)`, `(Path path)` |
| `UrlResource` | 从 URL 加载 | `(String url)`, `(URL url)`, `(URI uri)` |
| `JarResource` | 从 JAR 文件内部加载 | `(Path jarPath, String entryPath)`, `(URL jarUrl, String entryPath)` |
| `ByteArrayResource` | 从字节数组创建 | `(byte[] byteArray)`, `(byte[] byteArray, String description)` |
| `InputStreamResource` | 从输入流创建（一次性） | `(InputStream is)`, `(InputStream is, String description)` |

**使用示例:**

```java
// 统一接口访问不同来源的资源
Resource r1 = new ClassPathResource("config.yml");
Resource r2 = new FileResource("/etc/app/config.yml");
Resource r3 = new UrlResource("https://config.example.com/app.yml");
Resource r4 = new JarResource(Path.of("lib/app.jar"), "config/app.yml");
Resource r5 = new ByteArrayResource("{\"key\":\"value\"}".getBytes());

for (Resource resource : List.of(r1, r2, r3, r4, r5)) {
    if (resource.exists()) {
        String content = resource.getString();
        System.out.println(resource.getDescription() + ": " + content.length());
    }
}
```

### 6.3 ResourceLoader — 资源加载器

根据位置字符串前缀自动选择合适的资源类型。

```java
public class ResourceLoader {

    public static ResourceLoader create();
    public static ResourceLoader create(ClassLoader classLoader);

    /** 加载单个资源 */
    public Resource load(String location);

    /** 加载多个资源（支持通配符） */
    public List<Resource> loadAll(String locationPattern);

    /** 设置类加载器 */
    public ResourceLoader classLoader(ClassLoader classLoader);
}
```

支持的协议前缀:
- `classpath:` — ClassPath 资源
- `classpath*:` — 所有匹配的 ClassPath 资源
- `file:` — 文件系统资源
- `url:` — URL 资源
- `jar:` — JAR 内资源

---

## 7. 类元数据

### 7.1 MetadataReader — 元数据读取器

不加载类即可读取类的完整元数据，基于字节码解析。

```java
public final class MetadataReader {

    /** 通过类名读取 */
    public static ClassMetadata read(String className);

    /** 通过 Class 对象读取 */
    public static ClassMetadata read(Class<?> clazz);

    /** 通过 Resource 读取 */
    public static ClassMetadata read(Resource resource);

    /** 通过输入流读取 */
    public static ClassMetadata read(InputStream inputStream);

    /** 通过字节码读取 */
    public static ClassMetadata read(byte[] bytecode);

    /** 批量读取包下所有类 */
    public static List<ClassMetadata> readAll(String packageName);

    /** 批量读取（带过滤器） */
    public static List<ClassMetadata> readAll(String packageName, Predicate<String> classNameFilter);
}
```

### 7.2 ClassMetadata — 类元数据

不可变的类信息容器，提供丰富的查询方法。

```java
public final class ClassMetadata {

    // ==================== 基本信息 ====================
    public String className();           // 全限定类名
    public String packageName();         // 包名
    public String simpleName();          // 简单类名
    public String superClassName();      // 父类名
    public List<String> interfaceNames();// 实现的接口列表
    public int modifiers();              // 修饰符
    public String sourceFile();          // 源文件名

    // ==================== 类型判断 ====================
    public boolean isInterface();
    public boolean isAbstract();
    public boolean isAnnotation();
    public boolean isEnum();
    public boolean isRecord();
    public boolean isSealed();
    public boolean isFinal();
    public boolean isConcrete();         // 非抽象非接口
    public boolean isInnerClass();

    // ==================== 成员访问 ====================
    public List<MethodMetadata> methods();
    public List<FieldMetadata> fields();
    public List<AnnotationMetadata> annotations();
    public List<String> permittedSubclasses();  // sealed 类的许可子类

    // ==================== 便捷查询 ====================
    public boolean hasAnnotation(String annotationClassName);
    public boolean hasAnnotation(Class<? extends Annotation> annotationClass);
    public Optional<AnnotationMetadata> getAnnotation(String annotationClassName);
    public boolean isSubTypeOf(String className);
    public List<String> getMethodNames();
    public List<String> getFieldNames();
    public List<MethodMetadata> getMethodsByName(String methodName);
    public Optional<FieldMetadata> getField(String fieldName);
    public Optional<String> getOuterClassName();

    // ==================== Builder ====================
    public static Builder builder();
}
```

### 7.3 MethodMetadata — 方法元数据

```java
public final class MethodMetadata {
    public String methodName();
    public String returnType();
    public List<String> parameterTypes();
    public List<String> parameterNames();
    public List<String> exceptionTypes();
    public int modifiers();
    public boolean isSynthetic();
    public boolean isBridge();
    public boolean isDefault();
    public List<AnnotationMetadata> annotations();
    public List<List<AnnotationMetadata>> parameterAnnotations();

    // 便捷方法
    public boolean isAbstract();
    public boolean isStatic();
    public boolean isFinal();
    public boolean isPublic();
    public boolean isPrivate();
    public boolean isProtected();
    public boolean hasAnnotation(String annotationClassName);
    public Optional<AnnotationMetadata> getAnnotation(String annotationClassName);
    public String getSignature();        // 如 "getName()"
    public int parameterCount();
    public boolean hasNoParameters();
    public boolean isGetter();
    public boolean isSetter();
    public boolean isConstructor();
    public boolean isStaticInitializer();
}
```

### 7.4 FieldMetadata — 字段元数据

```java
public final class FieldMetadata {
    public String fieldName();
    public String fieldType();
    public int modifiers();
    public Object constantValue();
    public List<AnnotationMetadata> annotations();

    // 便捷方法
    public boolean isStatic();
    public boolean isFinal();
    public boolean isTransient();
    public boolean isVolatile();
    public boolean isPublic();
    public boolean isPrivate();
    public boolean isProtected();
    public boolean hasConstantValue();
    public boolean hasAnnotation(String annotationClassName);
    public Optional<AnnotationMetadata> getAnnotation(String annotationClassName);
    public String getSimpleTypeName();
}
```

### 7.5 AnnotationMetadata — 注解元数据

```java
public final class AnnotationMetadata {
    public String annotationType();           // 注解全限定名
    public Map<String, Object> attributes();  // 所有属性
    public boolean isRuntimeVisible();        // 是否运行时可见

    // 便捷方法
    public <T> Optional<T> getAttribute(String name, Class<T> type);
    public <T> T getAttribute(String name, Class<T> type, T defaultValue);
    public Optional<Object> getValue();       // 获取 value 属性
    public Optional<String> getStringValue(); // 获取 value 属性为 String
    public boolean hasAttribute(String name);
    public Set<String> getAttributeNames();
    public String getSimpleName();            // 注解简单名
}
```

---

## 8. 异常体系

### 8.1 OpenClassLoaderException

继承 `OpenException`，是 ClassLoader 组件的统一异常。提供语义化的静态工厂方法。

```java
public class OpenClassLoaderException extends OpenException {

    // ==================== 构造方法 ====================
    public OpenClassLoaderException(String message);
    public OpenClassLoaderException(String message, Throwable cause);

    // ==================== 静态工厂方法 ====================
    public static OpenClassLoaderException classNotFound(String className);
    public static OpenClassLoaderException classNotFound(String className, Throwable cause);
    public static OpenClassLoaderException classLoadFailed(String className, Throwable cause);
    public static OpenClassLoaderException resourceNotFound(String resourceName);
    public static OpenClassLoaderException resourceReadFailed(String resourceName, Throwable cause);
    public static OpenClassLoaderException metadataParseFailed(String className, Throwable cause);
    public static OpenClassLoaderException scanFailed(String packageName, Throwable cause);
    public static OpenClassLoaderException classLoaderClosed();

    // ==================== 访问方法 ====================
    public Optional<String> getClassName();
    public Optional<String> getResourceName();
}
```

---

## 9. 使用示例

### 9.1 插件系统

```java
// 加载插件 JAR，隔离其依赖
try (IsoClassLoader loader = IsoClassLoader.fromJar("/plugins/my-plugin.jar")
        .addIsolatedPackage("com.myplugin")
        .addSharedPackage("com.api")
        .loadingStrategy(IsoClassLoader.LoadingStrategy.CHILD_FIRST)
        .build()) {

    Class<?> pluginClass = loader.loadClass("com.myplugin.PluginImpl");
    Object plugin = pluginClass.getDeclaredConstructor().newInstance();

    // 通过共享 API 接口调用
    PluginApi api = (PluginApi) plugin;
    api.execute();
}
```

### 9.2 框架启动时的组件扫描

```java
// 扫描所有 @Component 类，过滤掉测试类
Set<Class<?>> components = OpenClassScanner.of("com.example")
    .includeJars(false)
    .parallel(true)
    .scan(ScanFilter.and(
        ScanFilter.isConcrete(),
        ScanFilter.hasAnnotation(Component.class),
        ScanFilter.not(ScanFilter.nameEndsWith("Test"))
    ));

// 注册到容器
for (Class<?> clazz : components) {
    container.register(clazz);
}
```

### 9.3 不加载类检查注解

```java
// 批量检查包下所有 Entity 类
List<ClassMetadata> entities = OpenMetadata.readPackage("com.example.model",
    name -> !name.contains("$"))  // 排除内部类
    .stream()
    .filter(m -> m.hasAnnotation("javax.persistence.Entity"))
    .toList();

for (ClassMetadata entity : entities) {
    System.out.println("Entity: " + entity.simpleName());
    entity.fields().stream()
        .filter(f -> f.hasAnnotation("javax.persistence.Id"))
        .forEach(f -> System.out.println("  Primary Key: " + f.fieldName()));
}
```

### 9.4 动态资源加载

```java
// 根据环境加载不同配置
String env = System.getProperty("app.env", "dev");
Resource config = OpenResource.get("classpath:config-" + env + ".yml");

if (config.exists()) {
    String content = config.getString();
    // 解析配置...
} else {
    // 使用默认配置
    config = OpenResource.get("classpath:config-default.yml");
}

// 扫描所有 SQL 文件
List<Resource> sqlFiles = OpenResource.getAll("classpath*:sql/**/*.sql");
for (Resource sql : sqlFiles) {
    String ddl = sql.getString();
    System.out.println("Executing: " + sql.getFilename());
}
```

---

## 10. 版本信息

| 属性 | 值 |
|------|-----|
| 模块名 | opencode-base-classloader |
| 编号 | 102 |
| 层级 | 基础组件 (1xx) |
| 最低 JDK | 25 |
| 核心依赖 | opencode-base-core |
| 第三方依赖 | 无 |
