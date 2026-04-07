# OpenCode Base IO

面向 JDK 25+ 的 IO 工具库，提供文件操作、资源加载和流处理功能。基于 NIO.2 API 构建，使用非受检异常实现更简洁的代码。

## Maven 依赖

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-io</artifactId>
    <version>1.0.3</version>
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
- 文件校验和计算（MD5、SHA-256、CRC32 等）
- 批量文件操作，支持虚拟线程并行执行
- 文件监视，监听变更通知
- 文件锁，支持跨进程同步
- 临时文件管理，自动清理
- MIME 类型检测，O(1) 反向查找
- 路径工具和支持 mmap 的文件比较器
- 大文件分块处理
- 上传和下载进度跟踪
- 序列化编组接口
- **Gzip 压缩/解压缩，含炸弹防护**（V1.0.3）
- **Zip 归档操作，含完整安全防护**（V1.0.3）
- **文件后备输出流，内存安全缓冲**（V1.0.3）
- **十六进制转储格式化**（V1.0.3）
- **Reader↔InputStream / Writer↔OutputStream 适配器**（V1.0.3）
- **尾部读取器，高效读取文件末尾 N 行**（V1.0.3）
- **函数式行处理管道**（V1.0.3）
- **原子文件写入（临时文件 + 原子移动）**（V1.0.3）

## 安全防护

本模块内置多项针对常见 IO 攻击的防护机制：

| 防护类型 | 所在类 | 说明 |
|---------|-------|------|
| **SSRF 防护** | `UrlResource` | 仅允许 `http`、`https`、`file`、`jar` 协议；`ftp`、`gopher` 等协议抛出 `OpenIOOperationException` |
| **Gzip 炸弹防护** | `GzipUtil` | 默认解压大小上限 256 MB；可通过 `decompress(data, maxSize)` 自定义 |
| **Zip 炸弹防护** | `ZipUtil` | 解压时强制校验单条目及总解压大小上限 |
| **Zip 路径穿越防护** | `ZipBuilder` | 条目名称校验：拒绝 `..` 段、绝对路径（`/`、`\`）和空字节 |
| **资源路径穿越防护** | `OpenResource` | 路径段校验：类路径查找前拒绝 `..` 组件 |
| **符号链接安全** | `OpenTempFile` | 使用 `toRealPath()` 解析符号链接后再操作临时文件 |

```java
// SSRF：被拒绝的协议抛出 OpenIOOperationException
new UrlResource("ftp://host/file");    // 抛出异常 — SSRF 防护
new UrlResource("http://host/file");   // 正常

// Gzip 炸弹：默认 256 MB 限制
byte[] safe = GzipUtil.decompress(compressed);                    // 256 MB 限制
byte[] safe = GzipUtil.decompress(compressed, 10 * 1024 * 1024); // 自定义 10 MB 限制

// Zip 路径穿越：添加时校验条目名称
ZipUtil.builder()
    .addString("../../etc/passwd", "data"); // 抛出异常 — 路径穿越已拒绝
```

## 类参考

### 核心类

| 类 | 说明 |
|---|------|
| `OpenIO` | 文件和目录操作主门面：读取、写入、复制、移动、删除、遍历 |
| `OpenStream` | 流工具：复制、读取、写入、缓冲、比较、排空 |
| `OpenPath` | 路径工具：扩展名、文件名、规范化、相对化 |
| `OpenResource` | 含路径穿越防护的类路径和文件系统资源加载 |
| `OpenMimeType` | MIME 类型检测和扩展名映射，O(1) 反向查找 |
| `OpenFileVisitors` | 文件访问器实现：递归删除、复制和大小计算 |

### 文件操作

| 类 | 说明 |
|---|------|
| `FileReader` | 增强的文件读取器，支持编码检测和行处理 |
| `FileWriter` | 增强的文件写入器，支持原子写入和备份 |
| `FileCopier` | 文件复制工具，支持进度跟踪和选项 |
| `FileComparator` | 按内容（mmap）、大小或修改时间比较文件 |
| `FileWatcher` | 监视目录中的文件创建、修改和删除事件 |
| `ChunkedFileProcessor` | 按可配置的块大小处理大文件，限制内存使用 |
| `MoreFiles` | 高级文件工具：原子写入、touch、安全删除、文件树构建器 |
| `TailReader` | 通过反向字节扫描高效读取文件末尾 N 行/N 字节 |
| `LineProcessor` | 函数式逐行处理管道，支持 `grep` / `map` / `limit` / `collect` |

### 资源加载

| 类 | 说明 |
|---|------|
| `Resource` | 抽象资源访问接口（输入流、URL、大小、最后修改时间） |
| `ResourceLoader` | 按位置字符串加载 `Resource` 的接口 |
| `DefaultResourceLoader` | 默认加载器：自动识别 `classpath:`、`file:`、URL 或普通路径 |
| `ClassPathResource` | 通过 `ClassLoader` 从类路径加载的资源 |
| `FileSystemResource` | 由 `java.nio.file.Path` 支持的文件系统资源 |
| `UrlResource` | 含 SSRF 协议白名单的 URL 资源 |

### Source / Sink

| 类 | 说明 |
|---|------|
| `ByteSource` | 可读字节源：包装数组、文件、URL、类路径；支持切片、拼接、哈希 |
| `ByteSink` | 可写字节接收器：文件、输出流 |
| `CharSource` | 可读字符源：包装字符串、Reader、文件；支持 copyTo |
| `CharSink` | 可写字符接收器：文件、Writer |

### 流

| 类 | 说明 |
|---|------|
| `BoundedInputStream` | 限制读取字节数；超出时可选抛出异常 |
| `CountingInputStream` | 统计已读取字节数，支持重置 |
| `TeeInputStream` | 将读取数据同时写入辅助 `OutputStream` |
| `FastByteArrayOutputStream` | 高性能字节数组输出流，减少数组复制 |
| `FileBackedOutputStream` | 内存缓冲超过阈值后溢出到临时文件 |
| `ReaderInputStream` | 使用可配置字符集将 `Reader` 适配为 `InputStream` |
| `WriterOutputStream` | 使用可配置字符集将 `Writer` 适配为 `OutputStream` |

### 批量操作

| 类 | 说明 |
|---|------|
| `OpenBatch` | 批量复制/移动/删除，支持 glob 模式和虚拟线程并行执行 |
| `BatchResult` | 结果记录：成功数、失败数、跳过数及逐条失败详情 |

### 校验和

| 类 | 说明 |
|---|------|
| `OpenChecksum` | 文件、流或字节数组的校验和：CRC32、MD5、SHA-1、SHA-256、SHA-512 |
| `Checksum` | 结果记录，包含算法名称、原始字节和 `hex()` 访问器 |

### 锁

| 类 | 说明 |
|---|------|
| `OpenFileLock` | 基于 `FileChannel.lock()` 的文件锁，用于跨进程同步 |

### 临时文件

| 类 | 说明 |
|---|------|
| `OpenTempFile` | 临时文件创建，支持可配置的前缀、后缀和目录 |
| `AutoDeleteTempFile` | 关闭时自动删除的临时文件（实现 `AutoCloseable`） |

### 进度

| 类 | 说明 |
|---|------|
| `DownloadProgress` | 下载操作的进度跟踪（已传输字节、总字节、百分比） |
| `UploadProgress` | 上传操作的进度跟踪（已传输字节、总字节、百分比） |

### 序列化

| 类 | 说明 |
|---|------|
| `Marshaller` | 对象序列化/反序列化到字节流的接口 |

### 压缩

| 类 | 说明 |
|---|------|
| `GzipUtil` | Gzip 压缩/解压缩：字节、文件、流；含炸弹防护 |
| `ZipUtil` | Zip 归档创建、提取和检查；含炸弹和路径穿越防护 |
| `ZipBuilder` | 含条目名称校验的 Zip 归档流式构建器 |
| `ZipEntryInfo` | Zip 条目元数据的不可变记录（名称、大小、压缩大小、修改时间） |

### 十六进制

| 类 | 说明 |
|---|------|
| `HexDump` | 将二进制数据格式化为十六进制转储（偏移量 + 十六进制列 + ASCII 列）；支持十六进制字符串编解码 |

### 异常

| 类 | 说明 |
|---|------|
| `OpenIOOperationException` | IO 操作失败的非受检异常，包含操作名称和路径上下文 |

## 快速开始

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

### 文件读写

```java
// 读取
String content = OpenIO.readString(Path.of("config.txt"));
List<String> lines = OpenIO.readLines(Path.of("data.csv"));
byte[] bytes = OpenIO.readBytes(Path.of("binary.dat"));

// 写入
OpenIO.writeString(Path.of("output.txt"), "Hello World");
OpenIO.writeLines(Path.of("data.csv"), List.of("a,b,c", "1,2,3"));
OpenIO.append(Path.of("log.txt"), "新条目\n");

// 原子写入 — 读取方永远不会看到部分写入的内容
MoreFiles.writeAtomically(Path.of("config.json"), jsonContent);
```

### 文件操作

```java
OpenIO.copy(source, target);
OpenIO.move(source, target);
OpenIO.deleteRecursively(Path.of("/tmp/workspace"));

// 目录遍历 — glob 模式
try (Stream<Path> files = OpenIO.glob(Path.of("src"), "*.java")) {
    files.forEach(System.out::println);
}

// 高级文件树遍历
try (Stream<Path> files = MoreFiles.fileTree(dir)
        .maxDepth(3)
        .filter(Files::isRegularFile)
        .stream()) {
    files.forEach(System.out::println);
}
```

### 资源加载

```java
// 类路径资源
Resource tpl = new ClassPathResource("templates/email.html");
String html = tpl.readString();

// 自动识别前缀：classpath:、file:、URL 或普通路径
ResourceLoader loader = DefaultResourceLoader.INSTANCE;
Resource r = loader.getResource("classpath:schema.sql");

// URL 资源 — 仅允许协议：http、https、file、jar
Resource remote = new UrlResource("https://example.com/data.json");
```

### ByteSource / CharSource

```java
// ByteSource — 不可变、可重复读取、可组合
ByteSource source = ByteSource.fromPath(Path.of("data.bin"));
long size = source.size();
byte[] data = source.read();
source.copyTo(outputStream);

// 切片与拼接
ByteSource slice = source.slice(1024, 512);
ByteSource all = ByteSource.concat(ByteSource.wrap(header), source);

// 内容相等性（块比较，可提前退出，不需全量加载）
boolean equal = source.contentEquals(ByteSource.fromPath(Path.of("copy.bin")));

// CharSource
CharSource charSrc = ByteSource.fromPath(Path.of("readme.txt"))
        .asCharSource(StandardCharsets.UTF_8);
String text = charSrc.read();
charSrc.copyTo(writer);
```

### 校验和

```java
// 文件哈希
String sha256 = OpenChecksum.sha256(Path.of("file.zip"));
long crc = OpenChecksum.crc32(Path.of("file.zip"));

// 完整性验证
boolean valid = OpenChecksum.verify(Path.of("file.zip"), expectedSha256, "SHA-256");

// 完整结果对象
Checksum cs = OpenChecksum.calculate(Path.of("file.zip"), "SHA-256");
System.out.println(cs.algorithm() + ": " + cs.hex());
```

### 压缩

```java
// Gzip — 字节数组（默认解压限制 256 MB）
byte[] compressed = GzipUtil.compress(data);
byte[] original   = GzipUtil.decompress(compressed);

// 自定义解压限制（10 MB）
byte[] safe = GzipUtil.decompress(compressed, 10 * 1024 * 1024);

// Gzip — 文件
GzipUtil.compress(Path.of("data.json"), Path.of("data.json.gz"));
GzipUtil.decompress(Path.of("data.json.gz"), Path.of("data.json"));

// Zip — 目录
ZipUtil.zip(Path.of("项目目录"), Path.of("project.zip"));
ZipUtil.unzip(Path.of("project.zip"), Path.of("输出目录"));

// Zip — 构建器 API（条目名称已校验路径穿越）
ZipUtil.builder()
    .addFile(Path.of("readme.md"))
    .addFile(Path.of("src/Main.java"), "sources/Main.java")
    .addString("notes.txt", "构建说明")
    .addBytes("data.bin", rawBytes)
    .writeTo(Path.of("archive.zip"));

// 列出 Zip 条目
List<ZipEntryInfo> entries = ZipUtil.list(Path.of("archive.zip"));
entries.forEach(e -> System.out.printf("%s (%d 字节)%n", e.name(), e.size()));

// 读取单个条目
byte[] content = ZipUtil.readEntry(Path.of("archive.zip"), "readme.md");
```

### 十六进制转储

```java
// 将二进制数据格式化为十六进制转储
byte[] data = "Hello, World!".getBytes();
String dump = HexDump.format(data);
// 00000000  48 65 6C 6C 6F 2C 20 57  6F 72 6C 64 21           |Hello, World!|

// 编码 / 解码十六进制字符串
String hex  = HexDump.toHex(data);           // "48656c6c6f2c20576f726c6421"
String up   = HexDump.toHexUpper(data);      // "48656C6C6F2C20576F726C6421"
byte[] back = HexDump.fromHex(hex);

// 对文件进行十六进制转储（默认最多 1 MB）
String fileDump = HexDump.format(Path.of("binary.dat"));

// 对文件的字节范围进行十六进制转储
String rangeDump = HexDump.format(Path.of("binary.dat"), 1024L, 256);
```

### 流工具

```java
// 复制
long bytes = OpenStream.copy(inputStream, outputStream);

// 有界流 — 超过 5 MB 抛出异常
try (BoundedInputStream bounded = new BoundedInputStream(input, 5 * 1024 * 1024, true)) {
    byte[] data = bounded.readAllBytes();
}

// 分流 — 同时读取和捕获
ByteArrayOutputStream copy = new ByteArrayOutputStream();
try (TeeInputStream tee = new TeeInputStream(input, copy)) {
    process(tee);
}
byte[] captured = copy.toByteArray();

// 文件后备输出流 — 超过阈值后溢出到磁盘
try (FileBackedOutputStream fbos = new FileBackedOutputStream(1024 * 1024)) {
    fbos.write(possiblyLargeData);
    try (InputStream in = fbos.getInputStream()) {
        process(in);
    }
}

// Reader ↔ InputStream 适配器
try (InputStream is = new ReaderInputStream(reader, StandardCharsets.UTF_8)) {
    process(is);
}
try (OutputStream os = new WriterOutputStream(writer, StandardCharsets.UTF_8)) {
    os.write(utf8Bytes);
}

// 流内容相等性
boolean equal = OpenStream.contentEquals(stream1, stream2);
```

### 尾部读取与行处理

```java
// 读取日志文件末尾 50 行
List<String> lastLines = TailReader.lastLines(Path.of("app.log"), 50);

// 函数式行处理管道
List<String> errors = LineProcessor.of(Path.of("app.log"))
    .grep("ERROR")
    .map(String::trim)
    .limit(100)
    .collect();
```

### 批量操作

```java
List<Path> files = List.of(path1, path2, path3);

// 顺序批量复制
BatchResult result = OpenBatch.copyAll(files, targetDir);
System.out.printf("成功: %d  失败: %d%n", result.successCount(), result.failureCount());

// 并行执行，带进度回调（虚拟线程）
BatchResult result = OpenBatch.parallel()
    .parallelism(4)
    .onProgress((path, i, total) -> System.out.printf("[%d/%d] %s%n", i, total, path))
    .copyAll(files, targetDir);

// 基于 glob 的批量删除
OpenBatch.deleteGlob(Path.of("build"), "*.class");
```

### 临时文件

```java
// 关闭时自动删除
try (AutoDeleteTempFile tmp = new AutoDeleteTempFile("prefix", ".tmp")) {
    OpenIO.writeString(tmp.path(), "临时数据");
    process(tmp.path());
} // 此处自动删除

// 命名临时文件
Path tmp = OpenTempFile.create("report", ".pdf", Path.of("/tmp"));
```

### 文件锁

```java
// 跨进程的排他文件锁
OpenFileLock lock = OpenFileLock.of(Path.of("process.lock"));
lock.withLock(() -> {
    // 同一时刻只有一个进程执行此块
});
```

## 环境要求

- Java 25+

## 开源协议

Apache License 2.0
