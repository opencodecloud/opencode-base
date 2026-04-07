# OpenCode Base ID

面向 JDK 25+ 的分布式 ID 生成库。提供统一门面，支持生成 Snowflake、UUID、ULID、TSID、KSUID、NanoID、带前缀类型化ID 和号段模式等多种唯一标识符。

## Maven 依赖

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-id</artifactId>
    <version>1.0.3</version>
</dependency>
```

## 功能特性

- **Snowflake**：64 位时间有序分布式 ID，支持可配置的工作节点/数据中心位数
- **UUID v4/v7**：标准 128 位通用唯一标识符（v7 为时间有序）
- **ULID**：128 位字典序可排序唯一标识符，支持单调/非单调模式（通过 `UlidConfig`）
- **TSID**：64 位时间排序 ID，采用 Crockford Base32 编码
- **KSUID**：160 位 K 可排序唯一标识符
- **NanoID**：紧凑 URL 友好型 ID，支持自定义字母表和长度
- **SafeJsSnowflake**：保证值 ≤ 2^53−1（JavaScript `Number.MAX_SAFE_INTEGER`）的雪花 ID
- **PrefixedId / TypedIdGenerator**：Stripe 风格的类型前缀 ID（如 `usr_01ARZ3NDEK`、`order_XYZ999`）
- **SnowflakeFriendlyId**：雪花 ID 的人类可读格式，便于调试（如 `2024-01-15T10:30:00.123Z#3-7-42`）
- **号段模式**：基于数据库序列的 ID 生成，双缓冲高吞吐量
- **简单生成器**：原子计数器、时间戳、随机 ID
- **ID 解析**：Snowflake、ULID、TSID、KSUID、UUID 结构化解析
- **ID 转换**：Base62、Base36、Base58 编解码，UUID↔ULID、UUID↔Base62 互转
- 所有生成器均线程安全

## 类参考

### 核心

| 类 | 说明 |
|-------|-------------|
| `OpenId` | 主门面类，提供所有 ID 生成操作的统一 API |
| `IdGenerator<T>` | 所有生成器的函数式接口，支持单个和批量生成 |
| `IdConverter` | ID 格式转换：Base62、Base36、Base58、UUID↔ULID |
| `IdParser<T,R>` | ID 解析接口，返回结构化结果对象 |

### Snowflake

| 类 | 说明 |
|-------|-------------|
| `SnowflakeGenerator` | 64 位雪花 ID 生成器，支持可配置的起始时间和位分配 |
| `SnowflakeBuilder` | 自定义雪花生成器配置的构建器 |
| `SnowflakeConfig` | 雪花生成器配置记录 |
| `SnowflakeIdParser` | 解析雪花 ID 以提取时间戳、工作节点ID、序列号 |
| `SnowflakeFriendlyId` | 将雪花 long 转换为人类可读格式，便于调试日志 |
| `SafeJsSnowflakeGenerator` | 输出始终 ≤ JavaScript Number.MAX_SAFE_INTEGER（2^53−1）的雪花 ID |
| `ClockBackwardStrategy` | 时钟回拨处理策略接口 |
| `ThrowException` | 时钟回拨时抛出异常 |
| `Wait` | 时钟回拨时等待时钟追上 |
| `Extend` | 时钟回拨时扩展序列位 |
| `WorkerIdAssigner` | 工作节点 ID 分配器接口 |
| `FixedWorkerIdAssigner` | 显式固定工作节点和数据中心 ID 分配 |
| `IpBasedAssigner` | 基于机器 IP 地址分配工作节点 ID |
| `MacBasedAssigner` | 基于 MAC 地址分配工作节点 ID |
| `RandomAssigner` | 随机分配工作节点 ID |

### UUID

| 类 | 说明 |
|-------|-------------|
| `OpenUuid` | UUID 工具类，支持格式转换（简化字符串、字节数组） |
| `UuidGenerator` | UUID 生成器基接口 |
| `UuidV4Generator` | UUID v4（随机）生成器 |
| `UuidV7Generator` | UUID v7（时间有序）生成器 |
| `UuidParser` | UUID 解析器：版本、变体检测，v1/v6/v7 时间戳提取 |

### ULID

| 类 | 说明 |
|-------|-------------|
| `UlidGenerator` | 128 位 ULID 生成器，支持单调（默认）和非单调模式 |
| `UlidConfig` | ULID 配置（`withMonotonic()` / `nonMonotonic()`），现已实际生效 |
| `UlidParser` | 解析 ULID 字符串，提取时间戳和随机组件 |

### TSID

| 类 | 说明 |
|-------|-------------|
| `TsidGenerator` | 64 位时间排序 ID 生成器，支持可配置节点位 |
| `TsidParser` | 解析 TSID 值，提取时间戳和节点信息 |

### KSUID

| 类 | 说明 |
|-------|-------------|
| `KsuidGenerator` | 160 位 K 可排序唯一 ID 生成器（27 字符字符串） |
| `KsuidParser` | 解析 KSUID 字符串，提取时间戳和载荷 |

### NanoID

| 类 | 说明 |
|-------|-------------|
| `NanoIdGenerator` | 紧凑 URL 友好型 ID 生成器（默认 21 字符） |
| `NanoIdBuilder` | 自定义 NanoID 大小和字母表的构建器 |
| `Alphabet` | 预定义字母表枚举 |

### 带前缀 / 类型化 ID

| 类 | 说明 |
|-------|-------------|
| `PrefixedId` | 不可变记录，表示带类型前缀的 ID（如 `usr_01ARZ3NDEK`） |
| `TypedIdGenerator` | `IdGenerator<String>`，将任意生成器与验证过的实体类型前缀结合 |

### 号段

| 类 | 说明 |
|-------|-------------|
| `SegmentIdGenerator` | 基于数据库序列的双缓冲高吞吐 ID 生成器 |
| `SegmentAllocator` | 号段分配器接口 |
| `JdbcSegmentAllocator` | 基于 JDBC 的数据库号段分配器 |
| `MemorySegmentAllocator` | 内存号段分配器（用于测试） |
| `SegmentBuffer` | 双缓冲实现，平滑 ID 生成 |

### 简单生成器

| 类 | 说明 |
|-------|-------------|
| `AtomicIdGenerator` | 原子计数器 ID 生成器 |
| `RandomIdGenerator` | 可配置长度的随机字符串 ID 生成器 |
| `TimestampIdGenerator` | 带防碰撞时间戳 ID 生成器 |

### 异常

| 类 | 说明 |
|-------|-------------|
| `OpenIdGenerationException` | ID 生成失败时抛出；携带 `generatorType`、`bizTag` 和结构化错误码常量 |

## 快速开始

```java
// ── 雪花 ID ────────────────────────────────────────────────────────────────
long id = OpenId.snowflakeId();
String idStr = OpenId.snowflakeIdStr();
var parsed = OpenId.parseSnowflakeId(id); // 时间戳、workerId、datacenterId、序列号

// 自定义配置
IdGenerator<Long> gen = OpenId.snowflakeBuilder()
    .workerIdAssigner(FixedWorkerIdAssigner.of(3, 1))
    .clockBackwardStrategy(Wait.ofSeconds(5))
    .build();

// 人类可读格式（调试/日志）
String readable = OpenId.snowflakeFriendly().toFriendly(id);
// → "2024-01-15T10:30:00.123Z#1-3-42"

// JavaScript 安全雪花（≤ 2^53-1）
long jsId = OpenId.safeJsSnowflakeId();

// ── 带前缀 / 类型化 ID ─────────────────────────────────────────────────────
TypedIdGenerator userGen  = OpenId.typedIdGenerator("usr",   UlidGenerator.create());
TypedIdGenerator orderGen = OpenId.typedIdGenerator("order", NanoIdGenerator.create());

String userId  = userGen.generate();  // "usr_01ARZ3NDEKTSV4RRFFQ69G5FAV"
String orderId = orderGen.generate(); // "order_V1StGXR8_Z5jdHi6B-myT"

PrefixedId pid = OpenId.parsePrefixedId(userId);
// pid.prefix() → "usr",  pid.rawId() → "01ARZ3NDEKTSV4RRFFQ69G5FAV"

// ── UUID ───────────────────────────────────────────────────────────────────
UUID uuid   = OpenId.uuid();    // v4（随机）
UUID uuidV7 = OpenId.uuidV7(); // v7（时间有序）
String simpleUuid = OpenId.simpleUuid(); // 32字符，无连字符

// 解析 UUID
UuidParser.ParsedUuid up = OpenId.parseUuid(uuidV7);
System.out.println(up.version());    // 7
System.out.println(up.timestamp()); // Instant（毫秒精度）

// ── ULID ───────────────────────────────────────────────────────────────────
String ulid = OpenId.ulid(); // 单调模式（默认）
// 非单调模式
UlidGenerator nonMono = UlidGenerator.create(UlidConfig.nonMonotonic());

// ── TSID / KSUID / NanoID ─────────────────────────────────────────────────
long tsid    = OpenId.tsid();
String ksuid = OpenId.ksuid();
String nano  = OpenId.nanoId();
String nano8 = OpenId.nanoId(8, "0123456789");

// ── 简单 ID ──────────────────────────────────────────────────────────────
long simpleId = OpenId.simpleId();      // 原子计数器
String tsId   = OpenId.timestampId();
String randId = OpenId.randomId(16);

// ── ID 转换 ────────────────────────────────────────────────────────────────
String b62 = IdConverter.toBase62(id);
String b58 = IdConverter.toBase58(id); // 比特币风格，无 0/O/I/l 歧义字符
String b36 = IdConverter.toBase36(id);

long back62 = IdConverter.fromBase62(b62);
long back58 = IdConverter.fromBase58(b58);

// ── 批量生成 ───────────────────────────────────────────────────────────────
List<Long> ids = OpenId.getSnowflake().generateBatch(100);
```

## 环境要求

- Java 25+

## 许可证

Apache License 2.0
