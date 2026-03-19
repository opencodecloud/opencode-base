# OpenCode Base ID

面向 JDK 25+ 的分布式 ID 生成库。提供统一门面，支持生成 Snowflake、UUID、ULID、TSID、KSUID、NanoID 和号段模式等多种唯一标识符。

## Maven 依赖

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-id</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 功能特性

- Snowflake：64 位时间有序分布式 ID，支持可配置的工作节点/数据中心位数
- UUID v4/v7：标准 128 位通用唯一标识符（v7 为时间有序）
- ULID：128 位字典序可排序唯一标识符
- TSID：64 位时间排序 ID，采用 Crockford Base32 编码
- KSUID：160 位 K 可排序唯一标识符
- NanoID：紧凑的 URL 友好 ID，支持自定义字母表和长度
- 号段模式：基于数据库序列的 ID 生成，双缓冲高吞吐
- 简单生成器：原子计数器、时间戳和随机 ID
- 支持 Snowflake、ULID、TSID 和 KSUID 的 ID 解析和验证
- 线程安全，所有生成器均支持并发使用

## 类参考

### 核心类

| 类 | 说明 |
|---|------|
| `OpenId` | 所有 ID 生成操作的统一 API 门面类 |
| `IdGenerator<T>` | 所有 ID 生成器的函数式接口，支持单个和批量生成 |
| `IdConverter` | 不同表示形式之间的 ID 转换工具 |
| `IdParser` | 将 ID 字符串解析回结构化数据的工具 |

### Snowflake（雪花算法）

| 类 | 说明 |
|---|------|
| `SnowflakeGenerator` | 64 位雪花 ID 生成器，支持可配置的纪元和位分配 |
| `SnowflakeBuilder` | 自定义雪花生成器配置的构建器 |
| `SnowflakeConfig` | 雪花生成器参数的配置记录 |
| `SnowflakeIdParser` | 解析雪花 ID，提取时间戳、工作节点 ID 和序列号 |
| `ClockBackwardStrategy` | 处理时钟回拨事件的策略接口 |
| `ThrowException` | 抛出异常的时钟回拨策略 |
| `Wait` | 等待时钟追上的时钟回拨策略 |
| `Extend` | 扩展序列位的时钟回拨策略 |
| `WorkerIdAssigner` | 工作节点 ID 分配接口 |
| `IpBasedAssigner` | 基于机器 IP 地址分配工作节点 ID |
| `MacBasedAssigner` | 基于 MAC 地址分配工作节点 ID |
| `RandomAssigner` | 随机分配工作节点 ID |

### UUID

| 类 | 说明 |
|---|------|
| `OpenUuid` | UUID 工具类，支持格式转换（简化字符串、字节数组） |
| `UuidGenerator` | UUID 生成器基础接口 |
| `UuidV4Generator` | UUID v4（随机）生成器 |
| `UuidV7Generator` | UUID v7（时间有序）生成器，用于可排序的 UUID |

### ULID

| 类 | 说明 |
|---|------|
| `UlidGenerator` | 128 位 ULID 生成器，生成 26 字符的 Crockford Base32 字符串 |
| `UlidConfig` | ULID 生成器配置 |
| `UlidParser` | 解析 ULID 字符串，提取时间戳和随机分量 |

### TSID

| 类 | 说明 |
|---|------|
| `TsidGenerator` | 64 位时间排序 ID 生成器，支持可配置的节点位数 |
| `TsidParser` | 解析 TSID 值，提取时间戳和节点信息 |

### KSUID

| 类 | 说明 |
|---|------|
| `KsuidGenerator` | 160 位 K 可排序唯一 ID 生成器（27 字符字符串） |
| `KsuidParser` | 解析 KSUID 字符串，提取时间戳和负载 |

### NanoID

| 类 | 说明 |
|---|------|
| `NanoIdGenerator` | 紧凑的 URL 友好 ID 生成器（默认 21 字符） |
| `NanoIdBuilder` | 自定义 NanoID 大小和字母表的构建器 |
| `Alphabet` | NanoID 生成的预定义字母表 |

### 号段模式

| 类 | 说明 |
|---|------|
| `SegmentIdGenerator` | 基于数据库序列的 ID 生成器，双缓冲保障高吞吐 |
| `SegmentAllocator` | 从后端存储分配 ID 号段的接口 |
| `JdbcSegmentAllocator` | 基于 JDBC 的号段分配器，使用数据库序列 |
| `MemorySegmentAllocator` | 内存号段分配器，用于测试 |
| `SegmentBuffer` | 平滑 ID 生成的双缓冲实现 |

### 简单生成器

| 类 | 说明 |
|---|------|
| `AtomicIdGenerator` | 基于原子计数器的简单 ID 生成器 |
| `RandomIdGenerator` | 可配置长度的随机字符串 ID 生成器 |
| `TimestampIdGenerator` | 基于时间戳的 ID 生成器，带冲突避免 |

### 异常

| 类 | 说明 |
|---|------|
| `OpenIdGenerationException` | ID 生成失败时抛出 |

## 快速开始

```java
// 雪花 ID
long id = OpenId.snowflakeId();
String idStr = OpenId.snowflakeIdStr();

// 解析雪花 ID
var parsed = OpenId.parseSnowflakeId(id);
System.out.println("时间戳: " + parsed.timestamp());

// 自定义雪花生成器
IdGenerator<Long> generator = OpenId.createSnowflake(1, 1);

// UUID
UUID uuid = OpenId.uuid();
UUID uuidV7 = OpenId.uuidV7();
String simpleUuid = OpenId.simpleUuid(); // 无连字符

// ULID
String ulid = OpenId.ulid();
var parsedUlid = OpenId.parseUlid(ulid);

// TSID
long tsid = OpenId.tsid();
String tsidStr = OpenId.tsidStr(); // Crockford Base32

// KSUID
String ksuid = OpenId.ksuid();
boolean valid = OpenId.isValidKsuid(ksuid);

// NanoID
String nanoId = OpenId.nanoId();        // 21 字符
String shortId = OpenId.nanoId(10);     // 自定义长度
String custom = OpenId.nanoId(8, "0123456789"); // 仅数字

// 简单 ID
long simpleId = OpenId.simpleId();      // 原子计数器
String timestampId = OpenId.timestampId();
String randomId = OpenId.randomId(16);

// 批量生成
List<Long> ids = OpenId.getSnowflake().generateBatch(100);
```

## 环境要求

- Java 25+

## 开源协议

Apache License 2.0
