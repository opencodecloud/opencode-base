# OpenCode Base ClassLoader

类加载器组件，提供类加载、扫描、资源管理和元数据读取功能，适用于 JDK 25+。

## 功能特性

- 统一的类加载器门面，支持默认/上下文/隔离/热替换加载器
- 包扫描和注解扫描，支持可配置过滤器
- 资源抽象（classpath、文件、URL、JAR、字节数组、输入流）
- 无需加载类即可读取类和注解元数据
- 热替换类加载器，支持动态类重载
- 隔离类加载器，支持插件式类隔离
- 支持协议感知的资源加载

## Maven 依赖

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-classloader</artifactId>
    <version>1.0.0</version>
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
| `HotSwapClassLoader` | 热替换类加载器，支持动态类重载（AutoCloseable） |
| `IsoClassLoader` | 隔离 URLClassLoader，用于插件式类隔离（AutoCloseable） |
| `ResourceClassLoader` | 从字节数组资源加载类的 ClassLoader |

### 扫描器

| 类名 | 说明 |
|------|------|
| `ClassScanner` | 类扫描器，支持过滤和回调 |
| `PackageScanner` | 包级别的类扫描 |
| `AnnotationScanner` | 基于注解的类扫描 |
| `ScanFilter` | 扫描结果过滤器接口 |

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
| `MethodMetadata` | 方法元数据（名称、返回类型、参数、注解） |
| `MetadataReader` | 无需加载类即可读取类元数据 |

### 异常

| 类名 | 说明 |
|------|------|
| `OpenClassLoaderException` | 类加载器操作异常 |

## 快速开始

```java
import cloud.opencode.base.classloader.*;

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
```

## 环境要求

- Java 25+

## 许可证

Apache License 2.0
