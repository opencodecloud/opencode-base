# OpenCode Base IO

面向 JDK 25+ 的 IO 工具库，提供文件操作、资源加载和流处理功能。基于 NIO.2 API 构建，使用非受检异常实现更简洁的代码。

## Maven 依赖

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-io</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 功能特性

- 文件读取：字节、字符串、行、流，支持字符集
- 文件写入：字节、字符串、行、追加操作
- 文件操作：复制、移动、删除（单个和递归）
- 目录遍历：列表、遍历、glob、正则和自定义匹配
- 资源加载：类路径、文件系统和 URL 资源的统一接口
- 字节和字符的 Source/Sink 抽象
- 流工具：有界、计数、复制和快速字节数组输出流
- 文件校验和计算（MD5、SHA-256 等）
- 批量文件操作和结果跟踪
- 文件监视，监听变更通知
- 文件锁，支持并发访问
- 临时文件管理，自动清理
- MIME 类型检测
- 路径工具和文件比较器
- 大文件分块处理
- 上传和下载进度跟踪
- 序列化编组接口

## 类参考

### 核心类

| 类 | 说明 |
|---|------|
| `OpenIO` | 文件和目录操作主门面：读取、写入、复制、移动、删除、遍历 |
| `OpenStream` | 输入/输出流操作工具类 |
| `OpenPath` | 路径操作和规范化工具类 |
| `OpenResource` | 类路径和文件系统资源加载工具 |
| `OpenMimeType` | MIME 类型检测和映射工具 |
| `OpenFileVisitors` | 文件访问器实现：递归删除、复制和大小计算 |

### 文件操作

| 类 | 说明 |
|---|------|
| `FileReader` | 增强的文件读取器，支持编码检测和行处理 |
| `FileWriter` | 增强的文件写入器，支持原子写入和备份 |
| `FileCopier` | 文件复制工具，支持进度跟踪和选项 |
| `FileComparator` | 按内容、大小或修改时间比较文件 |
| `FileWatcher` | 监视目录中的文件创建、修改和删除事件 |
| `ChunkedFileProcessor` | 按可配置的块大小处理大文件，限制内存使用 |
| `MoreFiles` | 标准 NIO.2 之外的附加文件工具方法 |

### 资源加载

| 类 | 说明 |
|---|------|
| `Resource` | 抽象资源访问接口，提供输入流和元数据 |
| `ResourceLoader` | 按位置字符串加载资源的接口 |
| `DefaultResourceLoader` | 默认资源加载器，支持 classpath: 和 file: 前缀 |
| `ClassPathResource` | 从类路径加载的资源 |
| `FileSystemResource` | 从文件系统加载的资源 |
| `UrlResource` | 从 URL 加载的资源 |

### Source / Sink

| 类 | 说明 |
|---|------|
| `ByteSource` | 字节的抽象源，提供读取操作 |
| `ByteSink` | 字节的抽象接收器，提供写入操作 |
| `CharSource` | 字符的抽象源，提供读取操作 |
| `CharSink` | 字符的抽象接收器，提供写入操作 |

### 流

| 类 | 说明 |
|---|------|
| `BoundedInputStream` | 限制读取字节数的输入流包装器 |
| `CountingInputStream` | 统计已读取字节数的输入流 |
| `TeeInputStream` | 将读取数据复制到辅助输出流的输入流 |
| `FastByteArrayOutputStream` | 高性能字节数组输出流，减少复制 |

### 批量操作

| 类 | 说明 |
|---|------|
| `OpenBatch` | 批量文件操作：批量复制、移动、删除，支持并行 |
| `BatchResult` | 批量操作结果记录，含成功/失败跟踪 |

### 校验和

| 类 | 说明 |
|---|------|
| `OpenChecksum` | 文件校验和计算工具（MD5、SHA-256、CRC32 等） |
| `Checksum` | 校验和结果记录，含算法和十六进制/字节表示 |

### 锁

| 类 | 说明 |
|---|------|
| `OpenFileLock` | 基于文件的锁，用于跨进程同步 |

### 临时文件

| 类 | 说明 |
|---|------|
| `OpenTempFile` | 临时文件创建，支持可配置的前缀、后缀和目录 |
| `AutoDeleteTempFile` | 关闭时自动删除的临时文件（实现 AutoCloseable） |

### 进度

| 类 | 说明 |
|---|------|
| `DownloadProgress` | 文件下载操作的进度跟踪 |
| `UploadProgress` | 文件上传操作的进度跟踪 |

### 序列化

| 类 | 说明 |
|---|------|
| `Marshaller` | 对象序列化/反序列化到字节流的接口 |

### 异常

| 类 | 说明 |
|---|------|
| `OpenIOOperationException` | IO 操作失败的非受检异常，含操作上下文 |

## 快速开始

```java
// 读取文件
String content = OpenIO.readString(Path.of("config.txt"));
List<String> lines = OpenIO.readLines(Path.of("data.csv"));
byte[] bytes = OpenIO.readBytes(Path.of("binary.dat"));

// 写入文件
OpenIO.writeString(Path.of("output.txt"), "Hello World");
OpenIO.writeLines(Path.of("data.csv"), List.of("a,b,c", "1,2,3"));
OpenIO.append(Path.of("log.txt"), "新条目\n");

// 文件操作
OpenIO.copy(source, target);
OpenIO.move(source, target);
OpenIO.deleteRecursively(Path.of("/tmp/workspace"));

// 目录遍历
try (Stream<Path> files = OpenIO.glob(Path.of("src"), "*.java")) {
    files.forEach(System.out::println);
}

try (Stream<Path> files = OpenIO.walk(Path.of("project"))) {
    long totalSize = files.filter(OpenIO::isFile).mapToLong(OpenIO::size).sum();
}

// 资源加载
Resource resource = new ClassPathResource("templates/email.html");
try (InputStream in = resource.getInputStream()) {
    // 处理资源
}

// 自动清理的临时文件
try (var tmp = new AutoDeleteTempFile("prefix", ".tmp")) {
    OpenIO.writeString(tmp.path(), "临时数据");
    // 关闭时自动删除文件
}

// 文件校验和
Checksum checksum = OpenChecksum.sha256(Path.of("file.zip"));
System.out.println("SHA-256: " + checksum.hex());
```

## 环境要求

- Java 25+

## 开源协议

Apache License 2.0
