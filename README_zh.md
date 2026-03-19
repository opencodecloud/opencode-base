# OpenCode Base

[![Java](https://img.shields.io/badge/Java-25%2B-blue)](https://openjdk.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green)](https://www.apache.org/licenses/LICENSE-2.0)
[![Build](https://img.shields.io/badge/Build-Maven-orange)](https://maven.apache.org/)

**面向 JDK 25+ 的现代化零依赖 Java 工具库**

OpenCode Base 是一个包含 43 个模块化组件的综合工具套件，覆盖核心工具、数据处理、安全加密、并发处理和业务逻辑——全部基于现代 Java 特性构建，包括虚拟线程、Record、密封类和模式匹配。

## 核心亮点

- **零依赖** — 核心模块无需第三方库
- **JDK 25+ 原生** — 虚拟线程、Record、密封类、模式匹配
- **JPMS 模块化** — 完整的 Java 模块系统支持
- **生产就绪** — 14 轮深度安全审计，修复 338+ 问题
- **43 个模块** — 按需引入，每个模块独立可用
- **双语文档** — 英文 + 中文 Javadoc

> **[English README](README.md)**

## 快速开始

### Maven

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

按需添加更多模块：

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-json</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 使用示例

所有门面类以 `Open` 开头 — 在 IDE 中输入 `Open` 即可自动补全：

```java
// 核心工具
String name = OpenObject.defaultIfNull(user.getName(), "unknown");

// JSON（零依赖，内置 POJO 映射）
String json = OpenJson.toJson(user);
User parsed = OpenJson.fromJson(json, User.class);

// 缓存（LRU/LFU/W-TinyLFU）
Cache<String, User> cache = OpenCache.getOrCreate("users", c -> c
    .maximumSize(10000).expireAfterWrite(Duration.ofMinutes(30)));

// 加密（AES-GCM, Argon2, JWT, PGP）
byte[] encrypted = OpenCrypto.aesGcm().setKey(key).encrypt("机密数据");
String hash = OpenCrypto.argon2().hash("password123");

// Cron 表达式（L/W/#，OR 语义，人类可读描述）
CronExpression cron = OpenCron.parse("0 9 * * MON-FRI");
ZonedDateTime next = cron.nextExecution(ZonedDateTime.now());
String desc = cron.describe(); // "At 09:00, Monday through Friday"

// ID 生成（雪花算法, NanoID, UUID）
long id = OpenId.snowflake().nextId();

// 并行处理（虚拟线程）
List<Result> results = OpenParallel.parallelMap(items, this::process);
```

## 架构总览

```
opencode-base（43 个模块）
┌─────────────────────────────────────────────────────────────────┐
│                     业务组件 (3xx) — 16 个                       │
│  验证码  邮件  事件  功能开关  地理  图  图片  农历               │
│  货币  短信  测试  时序  树  Web  PDF  Cron                      │
├─────────────────────────────────────────────────────────────────┤
│                     领域组件 (2xx) — 10 个                       │
│  配置  函数式  JSON  日志  并行  对象池                          │
│  序列化  XML  YAML  OAuth2                                      │
├─────────────────────────────────────────────────────────────────┤
│                     基础组件 (1xx) — 16 个                       │
│  缓存  类加载  集合  加密  日期  深克隆  哈希                    │
│  国际化  ID  IO  反射  字符串  表达式  锁  规则  可观测          │
├─────────────────────────────────────────────────────────────────┤
│                     核心组件 (0xx) — 1 个                        │
│                          Core                                    │
└─────────────────────────────────────────────────────────────────┘
```

## 模块详情

### 核心组件 (0xx)

#### core — 核心工具库
基础工具类和统一异常体系。对象/数组/数学/十六进制/Base64 工具，类型转换（`Convert`），元组（`Pair`、`Triple`），构建器模式（`OpenBuilder`），前置条件（`Preconditions`），原始类型包装器（`Ints`、`Longs`、`Bytes`），受检函数式接口，线程工具，SPI 加载器。

### 基础组件 (1xx)

#### cache — 高性能本地缓存
LRU/LFU/W-TinyLFU/FIFO 淘汰策略，TTL/TTI 过期，异步 API，布隆过滤器/SingleFlight 防护（`ProtectedCache`），预刷新（`RefreshAheadCache`），读时复制，缓存预热，响应式 API（JDK Flow），弹性加载（重试/熔断/隔舱/超时），采样统计，虚拟线程支持。

#### classloader — 类加载与扫描
隔离类加载器，热替换支持，包/注解扫描，类元数据读取，资源加载（`OpenResource`），字节码分析。

#### collections — 集合工具
`BiMap`、`Multiset`、`Multimap`、`Table`、`FluentIterable`、不可变集合、原始类型集合（`IntList`、`LongList`）、`SkipList`、`BitArray`、分区/分组/压缩/展平操作、`ListUtil`、`MapUtil`、`SetUtil`。

#### crypto — 加密解密
AES-CBC/GCM、ChaCha20-Poly1305、SM4、RSA、SM2、Ed25519、ECDSA、X25519/X448 密钥交换、Argon2/BCrypt/SCrypt 密码哈希、SHA/SM3/HMAC 摘要、JWT 创建/验证、PGP 加密/签名、证书链验证、安全随机工具。

#### date — 日期时间
格式化/解析、工作日计算、季度/周操作、时间区间、`StopWatch`、相对时间（"3小时前"）、`TemporalUtil`、日期验证器、带缓存的 `DateFormatter`。

#### deepclone — 深度克隆
反射克隆、序列化克隆、Unsafe 克隆三种策略。处理循环引用、不可变对象跳过、自定义克隆策略。

#### hash — 哈希工具
MurmurHash3（32/128位）、xxHash（32/64位）、CRC32、`BloomFilter`（含构建器）、一致性哈希（`ConsistentHash`）、`SimHash`（文本相似度）、`HashCode` 工具。

#### i18n — 国际化
多语言消息加载、`NamedParameterFormatter`、可重载消息提供者、组合语言解析器、消息源 SPI。

#### id — ID 生成
雪花算法（可配置纪元/位数）、UUID（v4/v7）、NanoID、号段模式（`SegmentIdGenerator` 含缓冲预加载）、时间戳 ID、组合 ID 策略。

#### io — IO 工具
文件读写、路径操作、文件监听（`FileWatcher`）、分块文件处理（含进度回调）、临时文件管理、`StreamUtil`、序列化辅助、资源加载。

#### reflect — 反射工具
类型安全的字段/方法访问、`TypeToken` 泛型类型令牌、Lambda 元信息提取、属性描述符、Record 工具、`ReflectCache` LRU 缓存、注解扫描、`BeanDiff` 对象比较。

#### string — 字符串处理
命名转换（`OpenNaming`：camelCase/snake_case/kebab-case/PascalCase）、模板引擎、相似度算法（Levenshtein、Jaro-Winkler、余弦）、数据脱敏（`OpenMask`：手机号/邮箱/身份证）、正则工具（`RegexPattern`、`OpenVerify`）、`AhoCorasick` 多模式匹配、`NamedParameterParser`、Base62/Base58 编解码。

#### expression — 表达式引擎
算术/比较/逻辑/三元运算符、属性访问、方法调用、集合操作（`in`、`contains`）、Lambda 表达式、沙箱执行（资源限制）、编译表达式缓存、自定义函数注册。

#### lock — 锁抽象
`LocalLock`（ReentrantLock 包装）、`SpinLock`（纳秒级操作的忙等待）、`ReadWriteLock`、命名锁工厂（`NamedLockFactory`）、锁组（`LockGroup` 含死锁预防）、Fencing Token、可配置超时/可重入行为。

#### rules — 规则引擎
条件-动作规则（含优先级）、规则组、决策表（CSV/内存）、DSL 构建器、热重载、规则监听器、组合条件（AND/OR/NOT）、`RuleEngine`（首次触发/全部触发模式）。

#### observability — 可观测性
`OpenTelemetryTracer` 追踪管理、`SlowLogCollector`（有界、线程安全、可配置阈值）、空操作追踪器（零开销禁用追踪）。

### 领域组件 (2xx)

#### config — 配置管理
多源（Properties/YAML/JSON/环境变量/系统属性）、热重载（含变更监听）、类型安全绑定、占位符解析、Profile 支持、层级配置合并、配置验证。

#### functional — 函数式编程
`Try`（异常安全计算）、`Either`（左/右）、`Option`（空安全）、`Lazy`（延迟计算）、`Pipeline`（数据转换链）、`Lens`（不可变更新）、`For`（单子推导）、`Future`（异步）、模式匹配工具。

#### json — JSON 处理
零依赖内置解析器/序列化器（含完整 POJO 映射——基于反射的 `BeanMapper`）、`JsonNode` 树模型（密封接口）、流式 `JsonReader`/`JsonWriter`、JSON Pointer（RFC 6901）、JSONPath、JSON Patch（RFC 6902）、Merge Patch（RFC 7396）、JSON Schema 验证、JSON Diff、`@JsonProperty`/`@JsonIgnore`/`@JsonFormat` 注解、响应式流、SPI 支持 Jackson/Gson/Fastjson2 集成。

#### log — 日志门面
`OpenLog` 含 MDC/NDC 上下文传播、采样日志（`SampledLog`）、条件日志、审计日志（`AuditLog`）、虚拟线程上下文支持、结构化 JSON 日志、敏感数据脱敏。

#### parallel — 并行计算
基于虚拟线程的 `BatchProcessor`、`parallelMap`/`parallelFilter`、`StructuredScope`（结构化并发）、`AsyncPipeline`、`ScheduledScope`、`RateLimitedExecutor`（令牌桶）、`CpuBound` 执行器、`HybridExecutor`、截止时间感知执行。

#### pool — 对象池
`GenericObjectPool` 含创建/验证/销毁生命周期、自动扩缩容（最小空闲/最大容量）、健康检查、FIFO/LIFO 借出、`ThreadLocalPool`、池统计、空闲对象驱逐。

#### serialization — 序列化
统一 `Serializer` SPI、JSON/二进制/Protobuf 支持、压缩序列化（GZIP/LZ4/ZSTD）、Schema 演进、类型注册、`SerializerFactory` 自动发现。

#### xml — XML 处理
DOM/SAX/StAX 解析、XPath 查询、Schema 验证、XSLT 转换、对象绑定（XML ↔ Bean）、`StaxWriter`/`StaxReader` 流式处理、XXE 防护（`XmlSecurity`）、命名空间支持、美化打印。

#### yml — YAML 处理
多文档解析、锚点/别名支持、合并键、类型安全值访问、`PlaceholderResolver`、安全加载（深度/大小/别名限制，`YmlSafeLoader`）、SPI 解析器。

#### oauth2 — OAuth2 认证
授权码模式、客户端凭证模式、设备码模式、PKCE（RFC 7636）、自动令牌刷新、OIDC 发现、平台预设（Google/GitHub/Microsoft）、`FileTokenStore`、恒定时间令牌比较。

### 业务组件 (3xx)

#### captcha — 验证码
数字、算术、中文、GIF 动画类型。可配置字体/大小/噪点/干扰线、`MemoryCaptchaStore`（含 TTL）、可插拔存储 SPI、Base64 图片输出。

#### email — 邮件
SMTP/SMTPS 发送（HTML 正文、附件、内嵌图片）。模板渲染。IMAP/POP3 接收（文件夹管理）。IDLE 推送通知。重试和限速。

#### event — 事件总线
进程内事件总线（同步/异步，虚拟线程分发）、类型安全事件、事件过滤、优先级监听器、`Saga` 编排（补偿事务）、事件存储（`InMemoryEventStore`）、死信处理、事件监控。

#### feature — 功能开关
百分比放量、用户白名单/黑名单、日期范围激活、A/B 测试（含指标收集）、`FeatureProxy`（基于注解）、缓存功能存储、JSON/内存提供者。

#### geo — 地理位置
距离计算（Haversine、Vincenty）、地理围栏（多边形/圆/矩形）、GeoHash 编解码（含邻居搜索）、坐标验证、坐标系转换（WGS84/GCJ02/BD09）、边界框查询、区域管理。

#### graph — 图数据结构
有向/无向加权图、BFS/DFS 遍历、Dijkstra/Bellman-Ford/A* 最短路径、拓扑排序（Kahn 算法）、Prim/Kruskal 最小生成树、环检测、连通分量、图序列化。

#### image — 图片处理
缩放、裁剪、旋转、翻转、水印（文字/图片）、亮度/对比度调整、格式转换（JPEG/PNG/GIF/BMP/WebP）、缩略图生成、EXIF 读取、`GifEncoder`、图片验证。

#### lunar — 农历
阳历 ↔ 农历转换（1900-2100）、24 节气、生肖、天干地支、传统节日、月份/日期名称格式化。

#### money — 货币金额
精确 `Money` 类型（BigDecimal 驱动）、多币种（`Currency` 枚举）、分摊（`AllocationUtil`：比例/百分比/权重/轮询）、中文大写转换（壹佰贰拾叁元）、格式化、`MoneyCalcUtil`（求和/平均/最大/最小）。

#### sms — 短信
多平台支持（阿里云、腾讯云、华为云）、模板管理、批量发送、限速（`SmsRateLimiter`）、手机号验证、发送结果追踪、可插拔提供者 SPI。

#### test — 测试工具
流式断言（`OpenAssert`）、Mock 代理、数据生成（`Faker`：姓名/手机号/邮箱/地址/公司）、`SensitiveDataGenerator`、`BenchmarkRunner`（预热/迭代/百分位）、`ThreadSafetyChecker`、`Wait` 工具、HTTP Mock 服务器。

#### timeseries — 时序数据
数据采集（`TimeSeries`）、聚合（按时间窗口求和/平均/最小/最大/计数）、异常检测（Z-score、IQR、移动平均）、趋势分析、季节分解、数据压缩（差值/Gorilla 编码）、相关性分析、数据点验证。

#### tree — 树结构
列表转树（`ListToTreeConverter`）、树遍历（DFS/BFS）、树搜索/过滤/展平、AVL 树、红黑树、跳表、`TreeBuilder`（含深度控制）、`Treeable` 自定义节点接口。

#### web — Web 通用
统一返回 `Result<T>`（含结果码）、`PageResult` 分页、`RequestContextHolder`（线程局部请求上下文）、`ResultCode` 枚举、SSE（服务端推送事件）支持、Cookie 工具、URL 构建器、请求体读取。

#### pdf — PDF 处理
零依赖 PDF 创建（文本/表格/图片/页眉/页脚）、合并多个 PDF、按页范围拆分、表单字段填充、数字签名、水印、页码、字体嵌入、模板渲染。

#### cron — Cron 表达式
RFC 兼容解析器（5/6字段）、名称别名（MON-FRI, JAN-DEC）、特殊字符（L/W/#/?）、预定义宏（@daily/@yearly）、月中日/星期几 OR 语义、`nextExecution`/`previousExecution`、批量调度、人类可读 `describe()`、流式 `CronBuilder`、`CronValidator` 间隔检查。

## 相关项目 (opencode-pro)

| 项目 | 说明 |
|------|------|
| **OpenData** | 数据库 ORM 框架 |
| **OpenCache** | 分布式缓存框架（Redis/Memcached） |
| **OpenRes** | 弹性容错框架（熔断器、限流器、重试） |
| **OpenValidation** | 数据校验框架（50+ 约束注解） |
| **OpenSecurity** | 安全框架（HSM, Vault, ACME） |
| **OpenNet** | 网络框架（HTTP, WebSocket, SSE） |
| **OpenMetrics** | 可观测性框架（指标、追踪、健康检查） |
| **OpenTasker** | 任务调度框架（Cron, 分布式） |

## 环境要求

- **Java 25+**（虚拟线程、密封接口、Record、模式匹配）
- **Maven 3.9+**

## 构建

```bash
# 编译所有模块
mvn compile

# 运行所有测试
mvn test

# 完整验证（含覆盖率检查）
mvn verify

# 编译指定模块
mvn compile -pl opencode-base-json,opencode-base-cache -am
```

## 项目统计

| 指标 | 数值 |
|------|------|
| 模块数 | 43 |
| 源文件 | 1,900+ |
| 测试文件 | 1,700+ |
| 安全审计轮次 | 14 |
| 修复问题数 | 338+ |
| Java 版本 | 25 |
| 外部依赖 | 0（核心） |

## 许可证

[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)

## 作者

**Leon Soo** — [OpenCode.cloud](https://opencode.cloud) | [LeonSoo.com](https://leonsoo.com)
