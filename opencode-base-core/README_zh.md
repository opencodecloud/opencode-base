# OpenCode Base Core

> OpenCode Base 库的核心工具和基础类型。

零依赖基础库，提供基本类型、类型转换、反射、线程、函数式错误处理、虚拟线程并发以及原始数组操作，适用于 JDK 25+。

## 环境要求

- JDK 25+
- 零外部依赖（JSpecify 仅为编译时依赖）

## 安装

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-core</artifactId>
    <version>1.0.3</version>
</dependency>
```

## 模块系统 (JPMS)

```java
requires cloud.opencode.base.core;
```

## v1.0.3 新特性

| 特性 | 说明 |
|------|------|
| `Result<T>` | 函数式错误处理的密封接口 -- Success 或 Failure，支持 Monad 操作 |
| `Either<L,R>` | 双值计算的密封接口 -- Left 或 Right，Right 倾向操作 |
| `Lazy<T>` | 虚拟线程安全的惰性求值，使用 VarHandle CAS（无 `synchronized`，不会固定线程） |
| `VirtualTasks` | 基于 JDK 25 虚拟线程的高级并发工具 |
| `OpenCollections` | 不可变集合构建器（ListBuilder、MapBuilder）和集合运算（并集、交集、差集） |
| `ObjectDiff` | Bean 差异比较引擎，支持深度比较、循环引用检测和字段过滤 |
| `Environment` | 运行时环境检测（JDK、OS、GraalVM、容器、虚拟线程） |
| `Page<T>` | **破坏性变更：**重构为不可变 record，新增工厂方法和 `map()` |
| `TriFunction` / `QuadFunction` | 三元和四元函数式接口 |
| JSpecify `@NullMarked` | 所有导出包均标注了空值安全注解 |
| `Retry` | 通用重试工具，支持可配置退避策略（固定、指数、斐波那契、抖动）、多异常类型重试、中止条件 |
| `OpenCollections` 增强 | `partition`、`groupBy`、`chunk`、`sliding`、`zip`、`zipWith`、`distinctBy`、`frequencies`、`flatten` |
| `Preconditions` 增强 | `checkPositive`、`checkNonNegative`、`checkBetween`、`checkNotBlank`、`checkNotEmpty` |
| `MoreObjects` | 注意：`allNull`/`anyNull`/`defaultIfNull`/`firstNonNull(变长参数)` 已在 `OpenObject` 中提供 |
| `ExceptionUtil` 增强 | `findCause`（Optional 风格）、`isOrCausedBy` |
| `VirtualTasks` 增强 | `supplyAsync`、`runAsync`（CompletableFuture 桥接）、带并发限制的 `parallelMap` |
| `Stopwatch` 增强 | `suspend`/`resume`（暂停/恢复）、`split`/`getLaps`（计次）、`time(Callable)`/`time(Runnable)`（一行代码计时） |
| `SpiLoader` 增强 | `loadSafe`（错误隔离加载）、`loadOrdered`（优先级排序加载） |
| `Range` 实现 `Predicate` | 支持 `stream().filter(range)` 和 Predicate 组合（`and`/`or`/`negate`） |
| `Environment` 增强 | `pid()`、`uptime()`、`javaHome()`、`userDir()`、`tempDir()` |
| `SystemInfo` | 全面的系统监控 — CPU 负载、物理/堆/交换内存、磁盘使用率、OS 信息、JVM 运行时详情 |
| `ProcessManager` | 操作系统进程管理 — 发现、带输出捕获的执行、终止/等待、进程树遍历 |

---

## API 参考

### 包：`cloud.opencode.base.core`

#### OpenArray

全面的数组操作工具，支持原始类型和对象数组。

| 方法 | 说明 |
|------|------|
| `newArray(Class<T>, int)` | 通过反射创建类型化数组 |
| `of(T...)` | 从可变参数创建数组 |
| `nullToEmpty(T[], Class<T[]>)` | 将 null 数组转换为空类型化数组 |
| `nullToEmpty(int[])` / `nullToEmpty(long[])` | 将 null 原始数组转换为空数组 |
| `add(T[], T)` | 在数组末尾追加元素 |
| `add(T[], int, T)` | 在指定索引插入元素 |
| `addAll(T[], T...)` | 连接两个数组 |
| `insert(int, T[], T...)` | 在索引处插入多个元素 |
| `insert(int, int[], int...)` | 在索引处插入 int 元素 |
| `remove(T[], int)` | 移除指定索引的元素 |
| `removeElement(T[], T)` | 移除元素的首次出现 |
| `removeAll(T[], int...)` | 移除多个索引处的元素 |
| `subarray(T[], int, int)` | 提取子数组（也支持 int[]、long[]、byte[] 重载） |
| `isEmpty(T[])` / `isNotEmpty(T[])` | 检查数组是否为空 |
| `contains(T[], T)` | 检查数组是否包含元素 |
| `indexOf(T[], T)` | 查找元素的首次索引 |
| `lastIndexOf(T[], T)` | 查找元素的最后索引 |
| `reverse(T[])` | 原地反转数组 |
| `swap(T[], int, int)` | 交换两个元素 |
| `shift(T[], int)` | 循环移位元素 |
| `isSorted(T[])` | 检查数组是否已排序 |
| `distinct(T[])` | 去除重复元素 |
| `toList(T[])` / `toSet(T[])` | 转换为集合 |
| `wrap(int[])` / `unwrap(Integer[])` | 装箱/拆箱原始数组 |

#### OpenBase64

Base64 编解码工具。

| 方法 | 说明 |
|------|------|
| `encode(byte[])` | 编码为 Base64 字符串 |
| `encodeUrlSafe(byte[])` | 使用 URL 安全的 Base64 编码 |
| `decode(String)` | 将 Base64 字符串解码为字节数组 |
| `decodeUrlSafe(String)` | 解码 URL 安全的 Base64 字符串 |
| `encodeToString(byte[])` | 使用标准字母表编码 |
| `isBase64(String)` | 检查字符串是否为有效的 Base64 |

#### OpenBit

位运算工具。

| 方法 | 说明 |
|------|------|
| `setBit(int, int)` / `clearBit(int, int)` | 设置/清除指定位 |
| `testBit(int, int)` | 测试指定位是否已设置 |
| `toggleBit(int, int)` | 切换指定位 |
| `highestOneBit(int)` / `lowestOneBit(int)` | 查找最高/最低已设置位 |
| `bitCount(int)` / `bitCount(long)` | 计算已设置位数 |
| `isPowerOfTwo(int)` / `isPowerOfTwo(long)` | 检查是否为 2 的幂 |
| `nextPowerOfTwo(int)` | 查找下一个 2 的幂 |
| `toBinaryString(int)` / `toBinaryString(long)` | 转换为二进制字符串 |

#### OpenBoolean

布尔值转换和评估工具。

| 方法 | 说明 |
|------|------|
| `toBoolean(String)` | 解析字符串为布尔值（支持 "yes"、"1"、"true" 等） |
| `toBoolean(int)` | 将 int 转换为布尔值（0 = false） |
| `toInt(boolean)` | 将布尔值转换为 int |
| `negate(Boolean)` | 空值安全的取反 |
| `isTrue(Boolean)` / `isFalse(Boolean)` | 空值安全的布尔检查 |
| `and(boolean...)` / `or(boolean...)` | 数组上的逻辑运算 |

#### OpenChar

字符类型检查和转换工具。

| 方法 | 说明 |
|------|------|
| `isAscii(char)` / `isAsciiPrintable(char)` | ASCII 检查 |
| `isLetter(char)` / `isDigit(char)` / `isLetterOrDigit(char)` | 类型检查 |
| `isUpperCase(char)` / `isLowerCase(char)` | 大小写检查 |
| `toUpperCase(char)` / `toLowerCase(char)` | 大小写转换 |
| `isBlank(char)` | 检查是否为空白字符 |
| `isChinese(char)` | 检查是否为中文字符 |
| `equals(char, char, boolean)` | 比较（可选忽略大小写） |

#### OpenCharset

字符集检测和转换工具。

| 方法 | 说明 |
|------|------|
| `defaultCharset()` | 获取系统默认字符集 |
| `forName(String)` | 按名称获取字符集（空值安全） |
| `convert(String, String, String)` | 在字符集之间转换字符串 |
| `isSupported(String)` | 检查字符集名称是否受支持 |

#### OpenClass

类元数据和类型检查工具。

| 方法 | 说明 |
|------|------|
| `getClass(String)` / `loadClass(String)` | 按名称加载类 |
| `getClassName(Object)` | 获取对象的类名 |
| `isAssignable(Class, Class)` | 检查可赋值性 |
| `isPrimitive(Class)` / `isPrimitiveWrapper(Class)` | 类型检查 |
| `getDefaultValue(Class)` | 获取类型的默认值 |
| `getInterfaces(Class)` | 获取所有实现的接口 |
| `getSuperClasses(Class)` | 获取父类链 |
| `isRecord(Class)` / `isSealed(Class)` | JDK 25 类型检查 |
| `isInnerClass(Class)` | 检查是否为内部/嵌套类 |

#### OpenEnum

枚举查找和转换工具。

| 方法 | 说明 |
|------|------|
| `valueOf(Class, String)` | 大小写敏感的枚举查找 |
| `valueOfIgnoreCase(Class, String)` | 大小写不敏感的枚举查找 |
| `getEnumMap(Class)` | 获取名称到枚举的映射 |
| `getEnumList(Class)` | 获取所有枚举常量列表 |
| `contains(Class, String)` | 检查枚举中是否存在指定名称 |

#### OpenHex

十六进制编解码工具。

| 方法 | 说明 |
|------|------|
| `encodeHex(byte[])` | 将字节编码为十六进制字符数组 |
| `encodeHexStr(byte[])` | 将字节编码为十六进制字符串 |
| `decodeHex(String)` / `decodeHex(char[])` | 将十六进制解码为字节 |
| `isHex(char)` | 检查字符是否为十六进制数字 |
| `toDigit(char)` | 将十六进制字符转换为 int 值 |

#### OpenMath

带溢出保护的数学运算。

| 方法 | 说明 |
|------|------|
| `add(int, int)` / `add(long, long)` | 带溢出检查的加法（Math.addExact） |
| `subtract(int, int)` / `subtract(long, long)` | 带溢出检查的减法 |
| `multiply(int, int)` / `multiply(long, long)` | 带溢出检查的乘法 |
| `divide(int, int)` / `divide(double, double)` | 安全除法 |
| `pow(double, double)` | 幂函数 |
| `sqrt(double)` | 平方根 |
| `abs(int)` / `abs(long)` / `abs(double)` | 绝对值 |
| `max(int...)` / `min(int...)` | 可变参数的最大/最小值 |
| `sum(int...)` / `sum(long...)` | 带溢出保护的求和 |
| `average(int...)` / `average(double...)` | 算术平均值 |
| `clamp(int, int, int)` | 将值钳制到范围内 |
| `gcd(int, int)` / `lcm(int, int)` | 最大公约数 / 最小公倍数 |
| `isPrime(long)` | 素数检测 |
| `factorial(int)` | 阶乘计算 |
| `fibonacci(int)` | 斐波那契数 |
| `round(double, int)` | 四舍五入到指定小数位 |
| `ceil(double)` / `floor(double)` | 向上/向下取整 |

#### OpenNumber

数字解析、比较和转换工具。

| 方法 | 说明 |
|------|------|
| `isNumber(String)` / `isInteger(String)` | 数字格式检查 |
| `parseInt(String, int)` | 带默认值的 int 解析 |
| `parseLong(String, long)` | 带默认值的 long 解析 |
| `parseDouble(String, double)` | 带默认值的 double 解析 |
| `compare(Number, Number)` | 通用数字比较 |
| `toBigDecimal(Number)` | 转换为 BigDecimal |
| `toInt(Number)` / `toLong(Number)` | 数字类型转换 |
| `isPositive(Number)` / `isNegative(Number)` / `isZero(Number)` | 符号检查 |

#### OpenObject

对象相等性、哈希和空值安全操作。

| 方法 | 说明 |
|------|------|
| `equal(Object, Object)` | 空值安全的相等比较 |
| `hashCode(Object...)` | 多值哈希码 |
| `toString(Object)` / `toString(Object, String)` | 空值安全的 toString |
| `defaultIfNull(T, T)` | 如果为 null 返回默认值 |
| `requireNonNull(T, String)` | 带消息的非空要求 |
| `clone(T)` | 通过序列化深度克隆 |
| `compare(T, T, Comparator)` | 空值安全的比较 |

#### OpenRadix

进制（基数）转换工具。

| 方法 | 说明 |
|------|------|
| `toBase(long, int)` | 转换为任意进制字符串 |
| `fromBase(String, int)` | 解析任意进制字符串 |
| `toBinary(long)` / `toOctal(long)` / `toHex(long)` | 常用进制转换 |
| `fromBinary(String)` / `fromOctal(String)` / `fromHex(String)` | 解析常用进制 |
| `encode62(long)` / `decode62(String)` | Base62 编解码 |

#### OpenStream

Stream 创建和转换工具。

| 方法 | 说明 |
|------|------|
| `of(T...)` | 从可变参数创建流 |
| `ofNullable(T)` | 从可空值创建流 |
| `concat(Stream...)` | 连接多个流 |
| `zip(Stream, Stream, BiFunction)` | 合并两个流 |
| `distinct(Stream, Function)` | 按键去重 |
| `batched(Stream, int)` | 将流分批为列表 |

#### OpenStringBase

基础字符串操作（空值安全、空白检查、裁剪）。

| 方法 | 说明 |
|------|------|
| `isEmpty(CharSequence)` / `isNotEmpty(CharSequence)` | 空值安全的空检查 |
| `isBlank(CharSequence)` / `isNotBlank(CharSequence)` | 空值安全的空白检查 |
| `trim(String)` / `strip(String)` | 空值安全的裁剪 |
| `defaultIfEmpty(String, String)` | 如果为空返回默认值 |
| `defaultIfBlank(String, String)` | 如果为空白返回默认值 |
| `nullToEmpty(String)` / `emptyToNull(String)` | null/空转换 |
| `truncate(String, int)` / `truncateMiddle(String, int)` | 带省略号的截断 |
| `repeat(String, int)` | 重复字符串 N 次 |
| `reverse(String)` | 反转字符串 |
| `capitalize(String)` / `uncapitalize(String)` | 首字母大小写 |
| `contains(String, String)` / `containsIgnoreCase(String, String)` | 子串搜索 |
| `startsWith(String, String)` / `endsWith(String, String)` | 前缀/后缀检查 |
| `removePrefix(String, String)` / `removeSuffix(String, String)` | 移除前缀/后缀 |
| `padLeft(String, int, char)` / `padRight(String, int, char)` | 字符串填充 |
| `countOccurrences(String, String)` | 计算子串出现次数 |
| `substringBefore(String, String)` / `substringAfter(String, String)` | 子串提取 |

#### Joiner

流式字符串连接器，支持分隔符、前缀、后缀。

| 方法 | 说明 |
|------|------|
| `on(String)` | 使用分隔符创建 Joiner |
| `on(char)` | 使用字符分隔符创建 Joiner |
| `skipNulls()` | 跳过 null 元素 |
| `useForNull(String)` | 用默认字符串替换 null |
| `withPrefix(String)` / `withSuffix(String)` | 添加前缀/后缀 |
| `join(Object...)` | 连接可变参数 |
| `join(Iterable)` | 连接可迭代对象 |
| `join(Iterator)` | 连接迭代器 |
| `appendTo(StringBuilder, Object...)` | 追加到现有构建器 |

#### Splitter

流式字符串分割器，支持正则和限制。

| 方法 | 说明 |
|------|------|
| `on(String)` | 使用分隔符创建 Splitter |
| `on(char)` | 使用字符分隔符创建 Splitter |
| `onPattern(String)` | 使用正则模式创建 Splitter |
| `trimResults()` | 裁剪每个结果 |
| `omitEmptyStrings()` | 移除空字符串 |
| `limit(int)` | 限制分割数量 |
| `split(String)` | 分割为可迭代对象 |
| `splitToList(String)` | 分割为列表 |
| `splitToStream(String)` | 分割为流 |

#### MoreObjects

ToStringHelper 和 firstNonNull 工具。

| 方法 | 说明 |
|------|------|
| `toStringHelper(Object)` / `toStringHelper(Class)` | 创建 ToStringHelper |
| `firstNonNull(T, T)` | 返回第一个非 null 参数 |
| `ToStringHelper.add(String, Object)` | 添加命名值 |
| `ToStringHelper.addValue(Object)` | 添加未命名值 |
| `ToStringHelper.omitNullValues()` | 跳过 null 值 |
| `ToStringHelper.toString()` | 构建字符串 |

#### Ordering

比较器构建器，支持链式调用和空值处理。

| 方法 | 说明 |
|------|------|
| `natural()` | 自然排序比较器 |
| `from(Comparator)` | 从现有比较器创建 |
| `reverse()` | 反转排序 |
| `nullsFirst()` / `nullsLast()` | 空值处理 |
| `onResultOf(Function)` | 转换后再比较 |
| `compound(Comparator)` | 次要比较器 |
| `min(T, T)` / `max(T, T)` | 两个值的最小/最大值 |
| `min(Iterable)` / `max(Iterable)` | 集合的最小/最大值 |
| `sortedCopy(Iterable)` | 返回排序副本 |
| `isOrdered(Iterable)` | 检查是否已排序 |

#### Preconditions

参数和状态校验，带描述性消息。

| 方法 | 说明 |
|------|------|
| `checkArgument(boolean, String, Object...)` | 验证参数条件 |
| `checkState(boolean, String, Object...)` | 验证状态条件 |
| `checkNotNull(T, String, Object...)` | 要求非空 |
| `checkElementIndex(int, int)` | 验证元素索引 |
| `checkPositionIndex(int, int)` | 验证位置索引 |
| `checkPositive(int/long, String)` | 验证值 > 0，返回值 |
| `checkNonNegative(int/long, String)` | 验证值 >= 0，返回值 |
| `checkBetween(int/long, min, max, String)` | 验证值在 [min, max]，返回值 |
| `checkNotBlank(String, String)` | 验证非空非空白字符串，返回值 |
| `checkNotEmpty(Collection, String)` | 验证非空非空集合，返回集合 |
| `checkNotEmpty(Map, String)` | 验证非空非空映射，返回映射 |

#### Range

不可变区间，支持开/闭/无界端点。

| 方法 | 说明 |
|------|------|
| `closed(C, C)` | 闭区间 [a, b] |
| `open(C, C)` | 开区间 (a, b) |
| `closedOpen(C, C)` | 半开区间 [a, b) |
| `openClosed(C, C)` | 半开区间 (a, b] |
| `atLeast(C)` / `atMost(C)` | 无界区间 |
| `greaterThan(C)` / `lessThan(C)` | 排他性无界 |
| `all()` | 全域区间 |
| `contains(C)` | 测试成员关系 |
| `test(C)` | Predicate 支持 — 委托给 `contains()` |
| `encloses(Range)` | 测试区间是否包含另一个 |
| `isConnected(Range)` | 测试区间是否相连 |
| `intersection(Range)` | 计算交集 |
| `span(Range)` | 计算最小包含区间 |
| `gap(Range)` | 计算断开区间之间的间隙 |

Range 实现了 `Predicate<C>`，支持：
```java
Range<Integer> range = Range.closed(1, 100);
List<Integer> filtered = numbers.stream().filter(range).toList();
Predicate<Integer> evenInRange = range.and(n -> n % 2 == 0);
```

#### Stopwatch

高精度计时器。

| 方法 | 说明 |
|------|------|
| `createStarted()` | 创建并启动计时器 |
| `createUnstarted()` | 创建不启动的计时器 |
| `start()` / `stop()` / `reset()` | 控制方法 |
| `suspend()` / `resume()` | 暂停/恢复（不重置累计时间） |
| `split()` | 记录计次时间，返回上次分段以来的 Duration |
| `getLaps()` | 获取所有计次记录 |
| `elapsed()` | 获取经过的 Duration |
| `elapsed(TimeUnit)` | 获取指定单位的经过时间 |
| `isRunning()` | 检查是否正在运行 |
| `time(Callable<T>)` | 一行代码计时：返回 Pair&lt;T, Duration&gt; |
| `time(Runnable)` | 一行代码计时：返回 Duration |

#### Suppliers

带缓存和过期功能的 Supplier 包装器。

| 方法 | 说明 |
|------|------|
| `memoize(Supplier)` | 永久缓存 Supplier 结果 |
| `memoizeWithExpiration(Supplier, long, TimeUnit)` | 带 TTL 的缓存 |
| `ofInstance(T)` | 返回固定值的 Supplier |
| `compose(Supplier, Function)` | 转换 Supplier 输出 |

#### Lazy **[v1.0.3 新增]**

虚拟线程安全的惰性求值容器，使用 VarHandle CAS。

| 方法 | 说明 |
|------|------|
| `of(Supplier<T>)` | 从 Supplier 创建 Lazy |
| `of(CheckedSupplier<T>)` | 从 CheckedSupplier 创建 Lazy（包装受检异常） |
| `value(T)` | 创建已求值的 Lazy |
| `get()` | 获取值（必要时计算，通过 CAS 保证线程安全） |
| `isEvaluated()` | 检查值是否已计算 |
| `map(Function)` | 惰性转换值 |
| `flatMap(Function)` | 惰性 flatMap 到另一个 Lazy |
| `filter(Predicate)` | 惰性过滤（不匹配时抛出 NoSuchElementException） |
| `getOrElse(T)` | 获取值或错误时返回默认值 |
| `getOrElse(Supplier)` | 获取值或错误时计算默认值 |
| `toOptional()` | 转换为 Optional |
| `reset()` | **[实验性]** 重置为未求值状态 |

#### Environment **[v1.0.3 新增]**

运行时环境检测（JDK、OS、GraalVM、容器、虚拟线程）。

| 方法 | 说明 |
|------|------|
| `javaVersion()` | 获取 Java 特性版本号（例如 25） |
| `javaVendor()` | 获取 Java 厂商字符串 |
| `isJavaVersionAtLeast(int)` | 检查最低 Java 版本 |
| `osName()` | 获取操作系统名称 |
| `isWindows()` / `isLinux()` / `isMacOS()` | 操作系统类型检查 |
| `isGraalVmNativeImage()` | 检查是否以 GraalVM 原生镜像运行 |
| `isContainer()` | 尽力检测容器环境（Docker、K8s、cgroup） |
| `isVirtualThread()` | 检查当前线程是否为虚拟线程 |
| `availableProcessors()` | 可用 CPU 数量（实时，不缓存） |
| `maxMemory()` / `totalMemory()` / `freeMemory()` | 内存指标（实时） |
| `pid()` | 当前 JVM 进程 ID |
| `uptime()` | JVM 运行时间（Duration） |
| `javaHome()` | Java 安装目录 |
| `userDir()` | 当前工作目录 |
| `tempDir()` | 系统临时目录 |

---

### 包：`cloud.opencode.base.core.result` **[v1.0.3 新增]**

#### Result\<T\>

函数式错误处理的密封接口 -- Success 或 Failure。

| 方法 | 说明 |
|------|------|
| `of(CheckedSupplier<T>)` | 通过执行 supplier 创建 Result，捕获异常为 Failure |
| `success(T)` | 创建 Success |
| `failure(Throwable)` | 创建 Failure |
| `successVoid()` | 创建 Success\<Void\>，用于仅副作用操作 |
| `isSuccess()` / `isFailure()` | 状态查询 |
| `map(Function)` | 转换成功值（自动捕获异常） |
| `flatMap(Function)` | 转换为另一个 Result |
| `recover(Function)` | 从 Failure 恢复为一个值 |
| `recoverWith(Function)` | 从 Failure 恢复为另一个 Result |
| `peek(Consumer)` | 对成功值执行副作用 |
| `peekFailure(Consumer)` | 对失败原因执行副作用 |
| `getOrElse(T)` | 获取值或默认值 |
| `getOrElseGet(Supplier)` | 获取值或计算默认值 |
| `getOrElseThrow(Function)` | 获取值或抛出映射异常 |
| `toOptional()` | 转换为 Optional |
| `stream()` | 转换为 Stream |

**内部类型：**
- `Result.Success<T>(T value)` -- 成功结果的 record
- `Result.Failure<T>(Throwable cause)` -- 失败结果的 record

```java
// 使用 switch 进行模式匹配（JDK 25）
Result<String> result = Result.of(() -> Files.readString(path));
switch (result) {
    case Result.Success(var v) -> process(v);
    case Result.Failure(var e) -> handleError(e);
}

// 链式操作
Result<Integer> length = result.map(String::trim).map(String::length);

// 恢复
String value = result.recover(ex -> "default").getOrElse("fallback");
```

#### Either\<L, R\>

双值计算的密封接口 -- Left 或 Right（Right 倾向）。

| 方法 | 说明 |
|------|------|
| `left(L)` | 创建 Left Either |
| `right(R)` | 创建 Right Either |
| `isLeft()` / `isRight()` | 状态查询 |
| `getLeft()` / `getRight()` | 获取值为 Optional |
| `map(Function)` | 转换 Right 值 |
| `flatMap(Function)` | 将 Right 转换为另一个 Either |
| `mapLeft(Function)` | 转换 Left 值 |
| `bimap(Function, Function)` | 转换两侧的值 |
| `getOrElse(R)` | 获取 Right 或默认值 |
| `orElse(Either)` | 如果是 Left 返回备选 Either |
| `fold(Function, Function)` | 将两种情况折叠为单一结果 |
| `swap()` | 交换 Left 和 Right |
| `peek(Consumer)` | 对 Right 值执行副作用 |
| `peekLeft(Consumer)` | 对 Left 值执行副作用 |
| `toOptional()` | 将 Right 转换为 Optional |
| `stream()` | 将 Right 转换为 Stream |
| `toResult()` | 转换为 Result（Right=Success，Left=Failure） |
| `toResult(Function)` | 使用自定义 Left-to-Throwable 映射转换为 Result |

**内部类型：**
- `Either.Left<L, R>(L value)` -- 左情况的 record
- `Either.Right<L, R>(R value)` -- 右情况的 record

```java
// 使用 switch 进行模式匹配（JDK 25）
Either<String, User> either = findUser(id);
switch (either) {
    case Either.Left(var err)  -> handleError(err);
    case Either.Right(var val) -> handleSuccess(val);
}

// Right 倾向的链式调用
findUser(1L)
    .map(User::getName)
    .fold(err -> log.error(err), name -> log.info("Found: " + name));
```

---

### 包：`cloud.opencode.base.core.concurrent` **[v1.0.3 新增]**

#### VirtualTasks

基于 JDK 25 虚拟线程的高级并发原语。

| 方法 | 说明 |
|------|------|
| `invokeAll(List<Callable<T>>)` | 全部成功或在首次失败时抛出异常 |
| `invokeAll(List<Callable<T>>, Duration)` | 带超时的 invokeAll |
| `invokeAny(List<Callable<T>>)` | 首个成功结果胜出，取消其余 |
| `invokeAny(List<Callable<T>>, Duration)` | 带超时的 invokeAny |
| `invokeAllSettled(List<Callable<T>>)` | 收集所有结果为 Result（不因任务失败而抛出异常） |
| `invokeAllSettled(List<Callable<T>>, Duration)` | 带超时的 invokeAllSettled |
| `parallelMap(List<T>, Function<T,R>)` | 对列表进行并行映射 |
| `parallelMap(List<T>, Function<T,R>, Duration)` | 带超时的 parallelMap |
| `runAll(List<Runnable>)` | 运行所有任务直至完成 |
| `runAll(List<Runnable>, Duration)` | 带超时的 runAll |

```java
// 全部成功或失败
List<String> results = VirtualTasks.invokeAll(List.of(
    () -> fetchFromServiceA(),
    () -> fetchFromServiceB()
));

// 首个成功胜出
String fastest = VirtualTasks.invokeAny(List.of(
    () -> queryMirror1(),
    () -> queryMirror2()
));

// 收集所有结果（成功或失败）
List<Result<String>> settled = VirtualTasks.invokeAllSettled(List.of(
    () -> riskyOperation1(),
    () -> riskyOperation2()
));

// 带超时的并行映射
List<Integer> lengths = VirtualTasks.parallelMap(
    urls, url -> download(url).length(), Duration.ofSeconds(30)
);
```

---

### 包：`cloud.opencode.base.core.collect` **[v1.0.3 新增]**

#### OpenCollections

不可修改集合的工厂与工具方法。

| 方法 | 说明 |
|------|------|
| `listBuilder()` / `listBuilder(int)` | 创建 ListBuilder 用于增量构建列表 |
| `mapBuilder()` | 创建 MapBuilder 用于增量构建映射 |
| `append(List<T>, T)` | 在末尾追加元素的新列表 |
| `prepend(T, List<T>)` | 在开头插入元素的新列表 |
| `concat(List<T>, List<T>)` | 连接两个列表 |
| `without(List<T>, T)` | 移除首次出现的新列表 |
| `withReplaced(List<T>, int, T)` | 替换指定索引元素的新列表 |
| `union(Set<T>, Set<T>)` | 集合并集 |
| `intersection(Set<T>, Set<T>)` | 集合交集 |
| `difference(Set<T>, Set<T>)` | 集合差集 (a \\ b) |
| `toUnmodifiableList()` | 产生不可修改 List 的收集器 |
| `toUnmodifiableSet()` | 产生不可修改 Set 的收集器 |

**内部类型：**
- `OpenCollections.ListBuilder<T>` -- `add(T)`、`addAll(Iterable)`、`build()`
- `OpenCollections.MapBuilder<K,V>` -- `put(K,V)`、`putAll(Map)`、`build()`

---

### 包：`cloud.opencode.base.core.retry` **[v1.0.3 新增]**

#### Retry

通用重试工具，支持可配置的退避策略。

| 类 / 方法 | 说明 |
|-----------|------|
| `Retry.of(Callable<T>)` | 创建流式重试构建器 |
| `.maxAttempts(int)` | 设置最大重试次数（默认：3） |
| `.backoff(BackoffStrategy)` | 设置退避策略 |
| `.delay(Duration)` | 固定延迟快捷方式 |
| `.exponentialBackoff(Duration, double)` | 指数退避快捷方式 |
| `.maxDelay(Duration)` | 设置最大延迟上限 |
| `.retryOn(Predicate<Throwable>)` | 基于谓词的重试过滤 |
| `.retryOn(Class)` | 仅对指定异常类型重试 |
| `.retryOnAny(Class...)` | 对多个异常类型中的任意一个重试 |
| `.abortOn(Class)` | 遇到指定异常类型时立即中止重试（优先级高于 `retryOn`） |
| `.abortIf(Predicate<Throwable>)` | 谓词匹配时立即中止重试（优先级高于 `retryOn`） |
| `.onRetry(BiConsumer)` | 每次重试的回调 |
| `.execute()` | 执行重试逻辑 |
| `Retry.execute(Callable)` | 静态便捷方法（3次尝试，100ms） |
| `BackoffStrategy.fixed(Duration)` | 固定延迟 |
| `BackoffStrategy.exponential(Duration, double)` | 指数增长延迟 |
| `BackoffStrategy.exponentialWithJitter(...)` | 带抖动的指数退避 |
| `BackoffStrategy.fibonacci(Duration)` | 斐波那契数列延迟 |

```java
// 使用默认配置重试
String data = Retry.execute(() -> fetchData());

// 构建器模式 + 指数退避
String result = Retry.of(() -> httpClient.get(url))
    .maxAttempts(5)
    .exponentialBackoff(Duration.ofMillis(200), 2.0)
    .retryOn(IOException.class)
    .maxDelay(Duration.ofSeconds(30))
    .execute();

// 多异常类型重试
String result = Retry.of(() -> httpClient.get(url))
    .retryOnAny(IOException.class, TimeoutException.class)
    .maxAttempts(3)
    .execute();

// 遇到不可重试的异常立即中止
String result = Retry.of(() -> httpClient.post(url, body))
    .maxAttempts(5)
    .retryOn(IOException.class)
    .abortOn(IllegalArgumentException.class)
    .execute();
```

---

### 包：`cloud.opencode.base.core.annotation`

| 类名 | 说明 |
|------|------|
| `Experimental` | 标记 API 为实验性的（可能不经通知即更改） |

---

### 包：`cloud.opencode.base.core.assertion`

#### OpenAssert

流式断言工具，用于参数校验。

| 方法 | 说明 |
|------|------|
| `notNull(T, String)` | 断言非空 |
| `isTrue(boolean, String)` / `isFalse(boolean, String)` | 断言布尔条件 |
| `state(boolean, String)` | 断言状态条件 |
| `notEmpty(CharSequence, String)` | 断言字符序列非空 |
| `notBlank(CharSequence, String)` | 断言字符序列非空白 |
| `matchesPattern(CharSequence, String, String)` | 断言正则匹配 |
| `notEmpty(Collection, String)` | 断言集合非空 |
| `notEmpty(Map, String)` | 断言映射非空 |
| `notEmpty(T[], String)` | 断言数组非空 |
| `noNullElements(T[], String)` | 断言数组无 null 元素 |
| `noNullElements(Iterable, String)` | 断言可迭代对象无 null 元素 |
| `inclusiveBetween(T, T, T)` | 断言值在 [start, end] 内 |
| `exclusiveBetween(T, T, T)` | 断言值在 (start, end) 内 |
| `validIndex(int, int)` | 断言有效的数组/列表索引 |

---

### 包：`cloud.opencode.base.core.bean`

| 类名 | 说明 |
|------|------|
| `OpenBean` | Bean 拷贝、属性访问和内省门面 |
| `BeanPath` | 嵌套属性路径访问（如 `user.address.city`） |
| `PropertyConverter` | Bean 属性类型转换接口 |
| `PropertyDescriptor` | Bean 属性元数据描述符 |
| `ObjectDiff` **[v1.0.3]** | 对象差异比较引擎 |
| `ObjectDiff.ObjectDiffBuilder` **[v1.0.3]** | 高级差异比较构建器 |
| `DiffResult<T>` **[v1.0.3]** | 比较结果记录 |
| `Diff<T>` **[v1.0.3]** | 单个属性差异记录 |
| `ChangeType` **[v1.0.3]** | 变更类型枚举：ADDED、REMOVED、MODIFIED、UNCHANGED、CIRCULAR_REFERENCE |

#### ObjectDiff **[v1.0.3 新增]**

| 方法 | 说明 |
|------|------|
| `compare(T, T)` | 简单浅属性比较 |
| `builder(T, T)` | 创建高级差异比较构建器 |

#### ObjectDiff.ObjectDiffBuilder **[v1.0.3 新增]**

| 方法 | 说明 |
|------|------|
| `deep(boolean)` | 启用/禁用深度递归比较 |
| `maxDepth(int)` | 设置最大递归深度（默认：10） |
| `maxCollectionSize(int)` | 设置比较的最大集合大小 |
| `include(String...)` | 要比较的字段白名单 |
| `exclude(String...)` | 从比较中排除的字段黑名单 |
| `collectionDiff(boolean)` | 启用元素级别的集合差异比较 |
| `compare()` | 执行比较并返回 DiffResult |

#### DiffResult\<T\> **[v1.0.3 新增]**

| 方法 | 说明 |
|------|------|
| `type()` | 比较对象的类 |
| `diffs()` | 所有属性差异 |
| `hasDiffs()` | 是否存在非 UNCHANGED 的差异 |
| `getModified()` | 仅 MODIFIED 差异 |
| `getAdded()` | 仅 ADDED 差异 |
| `getRemoved()` | 仅 REMOVED 差异 |

```java
// 简单比较
DiffResult<User> result = ObjectDiff.compare(oldUser, newUser);
if (result.hasDiffs()) {
    result.getModified().forEach(d ->
        System.out.println(d.fieldName() + ": " + d.oldValue() + " -> " + d.newValue()));
}

// 使用构建器进行高级比较
DiffResult<User> result = ObjectDiff.builder(oldUser, newUser)
    .deep(true)
    .maxDepth(5)
    .exclude("password", "internalId")
    .collectionDiff(true)
    .compare();
```

---

### 包：`cloud.opencode.base.core.builder`

| 类名 | 说明 |
|------|------|
| `Builder<T>` | 通用构建器接口，含 `build()` 方法 |
| `OpenBuilder` | 构建器工厂门面 |
| `BeanBuilder<T>` | JavaBean 实例的流式构建器 |
| `RecordBuilder<T>` | Record 实例的流式构建器 |
| `MapBuilder<K,V>` | Map 实例的流式构建器 |

---

### 包：`cloud.opencode.base.core.compare`

| 类名 | 说明 |
|------|------|
| `CompareUtil` | 通用比较运算符分派（EQ、NE、LT、LE、GT、GE） |

---

### 包：`cloud.opencode.base.core.container`

| 类名 | 说明 |
|------|------|
| `ContainerUtil` | 对 Collection、Map、Array、CharSequence、Optional 的通用 size/empty 操作 |

---

### 包：`cloud.opencode.base.core.convert`

| 类名 | 说明 |
|------|------|
| `Convert` | 类型转换门面 |
| `Converter<T>` | 转换器接口 |
| `ConverterRegistry` | 可扩展的转换器注册表 |
| `TypeReference<T>` | 泛型类型令牌，用于保留类型信息 |
| `TypeUtil` | 类型解析和检查工具 |
| `AttributeConverter<S,T>` | 双向属性转换 SPI |

---

### 包：`cloud.opencode.base.core.exception`

| 类名 | 说明 |
|------|------|
| `OpenException` | 所有 OpenCode 模块的基础运行时异常 |
| `OpenIOException` | I/O 相关异常 |
| `OpenIllegalArgumentException` | 非法参数异常 |
| `OpenIllegalStateException` | 非法状态异常 |
| `OpenTimeoutException` | 超时异常 |
| `OpenUnsupportedOperationException` | 不支持的操作异常 |
| `ExceptionUtil` | 异常包装、解包和堆栈跟踪工具 |

---

### 包：`cloud.opencode.base.core.func`

| 类名 | 说明 |
|------|------|
| `CheckedFunction<T,R>` | 可抛出受检异常的 Function |
| `CheckedConsumer<T>` | 可抛出受检异常的 Consumer |
| `CheckedSupplier<T>` | 可抛出受检异常的 Supplier |
| `CheckedPredicate<T>` | 可抛出受检异常的 Predicate |
| `CheckedRunnable` | 可抛出受检异常的 Runnable |
| `CheckedCallable<T>` | 可抛出受检异常的 Callable |
| `TriFunction<A,B,C,R>` | 三元函数接口 |
| `QuadFunction<A,B,C,D,R>` | 四元函数接口 |

---

### 包：`cloud.opencode.base.core.page`

#### Page\<T\> **[v1.0.3 破坏性变更 -- 重构为不可变 record]**

| 方法 | 说明 |
|------|------|
| `of(long, long, long, List<T>)` | 创建 Page（current、size、total、records） |
| `empty(long)` | 创建给定页大小的空 Page |
| `current()` / `size()` / `total()` / `records()` | Record 访问器 |
| `pages()` | 计算总页数 |
| `hasNext()` / `hasPrevious()` | 导航检查 |
| `offset()` | 零基偏移量：(current - 1) * size |
| `map(Function<T,U>)` | 映射记录并保留分页元数据 |

#### PageRequest

| 方法 | 说明 |
|------|------|
| `of(long, long)` / `of(long, long, Sort)` | 创建 PageRequest |
| `ofSize(long)` | 创建给定页大小的第 1 页请求 |
| `getOffset()` | 计算偏移量 |
| `isFirst()` | 检查是否为第一页 |
| `next()` / `previous()` / `first()` | 导航 |
| `withSort(Sort)` / `withPage(long)` | 修改副本 |
| `toPage()` **[v1.0.3]** | 创建与此请求匹配的空 Page |

#### Sort

| 方法 | 说明 |
|------|------|
| `by(String)` / `by(Direction, String)` | 创建 Sort |
| `by(Direction, String...)` / `by(Order...)` / `by(List<Order>)` | 创建多字段 Sort |
| `unsorted()` | 无排序 |
| `getOrders()` | 获取排序规范 |
| `isUnsorted()` | 检查是否无排序 |
| `and(Sort)` | 与另一个 Sort 组合 |
| `toSql()` | 渲染为 SQL 片段 |

---

### 包：`cloud.opencode.base.core.primitives`

原始类型数组工具。每个类提供：

| 方法 | 适用类型 |
|------|---------|
| `contains(arr, val)` | 所有类型 |
| `indexOf(arr, val)` / `lastIndexOf(arr, val)` | 所有类型 |
| `min(arr)` / `max(arr)` | 数值类型 |
| `sum(arr)` | int、long、double、float |
| `sort(arr)` / `reverse(arr)` | 所有类型 |
| `concat(arr, arr)` | 所有类型 |
| `toArray(Collection)` | 所有类型 |
| `asList(arr)` | 所有类型 |

| 类名 | 类型 |
|------|------|
| `Ints` | `int[]` |
| `Longs` | `long[]` |
| `Doubles` | `double[]` |
| `Floats` | `float[]` |
| `Shorts` | `short[]` |
| `Bytes` | `byte[]` |
| `Chars` | `char[]` |
| `Booleans` | `boolean[]` |

---

### 包：`cloud.opencode.base.core.random`

| 类名 | 说明 |
|------|------|
| `OpenRandom` | 安全随机数生成工具 |
| `IdGenerator` | 唯一 ID 生成器接口 |
| `VerifyCodeUtil` | 验证码生成工具 |

---

### 包：`cloud.opencode.base.core.reflect`

| 类名 | 说明 |
|------|------|
| `ReflectUtil` | 通用反射工具 |
| `FieldUtil` | 字段访问和操作 |
| `MethodUtil` | 方法查找和调用 |
| `ConstructorUtil` | 构造器查找和实例化 |
| `ModifierUtil` | 修饰符检查 |
| `RecordUtil` | Record 组件访问 |
| `UnsafeUtil` | `sun.misc.Unsafe` 包装器，用于底层操作 |

---

### 包：`cloud.opencode.base.core.singleton`

| 类名 | 说明 |
|------|------|
| `Singleton` | 线程安全的懒加载单例注册表 |

---

### 包：`cloud.opencode.base.core.spi`

#### SpiLoader

带缓存、错误隔离和优先级排序的 SPI 服务加载工具。

| 方法 | 说明 |
|------|------|
| `load(Class<T>)` | 加载所有实现（带缓存） |
| `loadFirst(Class<T>)` | 加载第一个实现（Optional） |
| `loadFirstOrDefault(Class<T>, T)` | 加载第一个或使用默认值 |
| `loadSafe(Class<T>)` | 错误隔离加载 — 跳过损坏的 provider |
| `loadOrdered(Class<T>)` | 按 `getPriority()`/`getOrder()` 排序加载 |
| `loadByType(Class<T>, Class<S>)` | 按子类型过滤 |
| `loadStream(Class<T>)` | 延迟加载为 Stream |
| `reload(Class<T>)` | 强制重新加载（刷新缓存） |
| `hasService(Class<T>)` / `count(Class<T>)` | 服务可用性检查 |
| `clearCache()` / `clearCache(Class)` | 缓存管理 |

---

### 包：`cloud.opencode.base.core.stream`

| 类名 | 说明 |
|------|------|
| `OptionalUtil` | Optional 扩展工具 |
| `ParallelStreamUtil` | 并行流执行工具 |

---

### 包：`cloud.opencode.base.core.thread`

| 类名 | 说明 |
|------|------|
| `OpenThread` | 线程工具（sleep、虚拟线程创建） |
| `NamedThreadFactory` | 支持自定义命名模式的 ThreadFactory |
| `ScopedValueUtil` | JDK 25 ScopedValue 工具 |
| `ThreadLocalUtil` | ThreadLocal 管理工具 |

---

### 包：`cloud.opencode.base.core.tuple`

| 类名 | 说明 |
|------|------|
| `Pair<A,B>` | 不可变的二元组 Record |
| `Triple<A,B,C>` | 不可变的三元组 Record |
| `Quadruple<A,B,C,D>` | 不可变的四元组 Record |
| `TupleUtil` | 元组创建和转换工具 |

### 包：`cloud.opencode.base.core.system` **[v1.0.3 新增]**

全面的系统信息门面 — CPU、内存、磁盘、操作系统和 JVM 运行时指标。

#### SystemInfo

| 方法 | 说明 |
|------|------|
| `cpu()` | CPU 信息快照（处理器数、架构、负载） |
| `cpuLoad()` | 系统级 CPU 负载 [0.0, 1.0]，不可用返回 -1 |
| `processCpuLoad()` | JVM 进程 CPU 负载 [0.0, 1.0]，不可用返回 -1 |
| `loadAverage()` | 1/5/15 分钟平均负载（Linux 读取 /proc/loadavg） |
| `memory()` | 物理内存信息快照 |
| `heapMemory()` | JVM 堆内存信息 |
| `nonHeapMemory()` | JVM 非堆内存信息 |
| `physicalMemoryTotal()` / `physicalMemoryFree()` | 物理内存字节数 |
| `swapTotal()` / `swapFree()` | 交换空间字节数 |
| `disks()` | 所有文件存储信息 |
| `disk(Path)` | 指定路径的磁盘信息 |
| `diskTotal(Path)` / `diskFree(Path)` / `diskUsable(Path)` | 磁盘空间字节数 |
| `os()` | 操作系统信息（名称、版本、架构、主机名） |
| `hostname()` | 主机名（优先环境变量，DNS 后备） |
| `uptime()` | JVM 运行时间（毫秒） |
| `environmentVariables()` | 系统环境变量（不可修改） |
| `runtime()` | JVM 运行时信息（版本、PID、启动参数） |

#### 记录类型

| 记录 | 字段 |
|------|------|
| `CpuInfo` | `availableProcessors`、`arch`、`systemCpuLoad`、`processCpuLoad`、`loadAverage` |
| `MemoryInfo` | `total`、`used`、`free`、`max` + `usagePercent()`、`totalDisplay()` |
| `DiskInfo` | `name`、`type`、`totalSpace`、`usableSpace`、`unallocatedSpace`、`readOnly` |
| `OsInfo` | `name`、`version`、`arch`、`hostname`、`availableProcessors`、`physicalMemoryTotal`、`swapTotal` |
| `RuntimeInfo` | `javaVersion`、`javaVendor`、`javaHome`、`vmName`、`vmVersion`、`uptime`、`startTime`、`pid`、`inputArguments` |

### 包：`cloud.opencode.base.core.process` **[v1.0.3 新增]**

进程管理工具 — 使用 JDK ProcessHandle 和 ProcessBuilder 实现进程发现、执行和控制。

#### ProcessManager

| 方法 | 说明 |
|------|------|
| `current()` | 当前 JVM 进程信息 |
| `find(long pid)` | 通过 PID 查找进程 |
| `listAll()` | 列出所有可见进程 |
| `findByName(String)` | 按命令名查找（不区分大小写，包含匹配） |
| `findByCommand(String)` | 按完整命令行查找 |
| `currentPid()` | 当前进程 PID |
| `parent()` | 父进程信息 |
| `children()` / `children(long pid)` | 直接子进程 |
| `descendants()` / `descendants(long pid)` | 所有后代进程 |
| `execute(String...)` | 执行命令，捕获 stdout/stderr |
| `execute(List<String>)` | 执行命令列表 |
| `execute(ProcessConfig)` | 使用完整配置执行 |
| `start(String...)` / `start(ProcessConfig)` | 启动但不等待 |
| `kill(long pid)` / `killForcibly(long pid)` | 终止进程 |
| `isAlive(long pid)` | 检查进程是否存活 |
| `waitFor(long pid, long timeout, TimeUnit)` | 等待进程退出 |

#### ProcessConfig（Builder 模式）

| 方法 | 说明 |
|------|------|
| `builder(String...)` / `builder(List<String>)` | 以命令创建构建器 |
| `workingDirectory(Path)` | 设置工作目录 |
| `environment(String, String)` | 添加环境变量 |
| `timeout(Duration)` | 设置执行超时 |
| `redirectErrorStream(boolean)` | 将 stderr 合并到 stdout |
| `stdoutFile(Path)` / `stderrFile(Path)` | 重定向到文件 |
| `inheritIO(boolean)` | 继承父进程 IO 流 |

#### 记录类型

| 记录 | 字段 |
|------|------|
| `ProcessInfo` | `pid`、`command`、`commandLine`、`user`、`startTime`、`cpuDuration`、`alive` |
| `ProcessResult` | `exitCode`、`stdout`、`stderr`、`duration`、`command` + `isSuccess()`、`orThrow()` |

---

## 快速开始

```java
import cloud.opencode.base.core.*;
import cloud.opencode.base.core.result.*;
import cloud.opencode.base.core.concurrent.*;
import cloud.opencode.base.core.collect.*;
import cloud.opencode.base.core.tuple.*;
import cloud.opencode.base.core.primitives.*;
import cloud.opencode.base.core.system.*;
import cloud.opencode.base.core.process.*;

// ---- v1.0.3 Result monad ----
Result<String> result = Result.of(() -> Files.readString(path));
String content = result
    .map(String::trim)
    .recover(ex -> "fallback")
    .getOrElse("");

// ---- v1.0.3 Either monad ----
Either<String, User> either = findUser(id);
String name = either.map(User::getName).getOrElse("unknown");

// ---- v1.0.3 Lazy（虚拟线程安全）----
Lazy<ExpensiveObject> lazy = Lazy.of(() -> createExpensiveObject());
ExpensiveObject obj = lazy.get();  // 仅计算一次，结果缓存

// ---- v1.0.3 VirtualTasks ----
List<String> results = VirtualTasks.invokeAll(List.of(
    () -> fetchFromServiceA(),
    () -> fetchFromServiceB()
));

// ---- v1.0.3 OpenCollections ----
List<String> items = OpenCollections.<String>listBuilder()
    .add("a").add("b").add("c").build();
List<String> appended = OpenCollections.append(items, "d");

// ---- v1.0.3 ObjectDiff ----
DiffResult<User> diff = ObjectDiff.builder(oldUser, newUser)
    .deep(true).exclude("password").compare();

// ---- v1.0.3 Environment ----
if (Environment.isContainer()) {
    int cpus = Environment.availableProcessors();
}

// ---- v1.0.3 Page（不可变 record）----
Page<User> page = Page.of(1, 10, 100, userList);
Page<UserDto> dtoPage = page.map(UserDto::from);

// ---- v1.0.3 Retry 重试 ----
String data = Retry.of(() -> httpClient.get(url))
    .maxAttempts(5)
    .exponentialBackoff(Duration.ofMillis(200), 2.0)
    .retryOnAny(IOException.class, TimeoutException.class)
    .abortOn(IllegalArgumentException.class)
    .execute();

// ---- v1.0.3 Stopwatch 计次 ----
Stopwatch sw = Stopwatch.createStarted();
// ... 阶段 1 ...
Duration lap1 = sw.split();
// ... 阶段 2 ...
Duration lap2 = sw.split();

// ---- v1.0.3 一行代码计时 ----
Duration elapsed = Stopwatch.time(() -> heavyComputation());

// ---- v1.0.3 Range 作为 Predicate ----
Range<Integer> range = Range.closed(1, 100);
List<Integer> inRange = numbers.stream().filter(range).toList();

// ---- v1.0.3 OpenCollections 增强 ----
List<List<Integer>> chunks = OpenCollections.chunk(List.of(1,2,3,4,5), 2);
Map<String, List<User>> byCity = OpenCollections.groupBy(users, User::city);

// ---- v1.0.3 Preconditions 增强 ----
this.size = Preconditions.checkPositive(size, "size");
this.name = Preconditions.checkNotBlank(name, "name");

// ---- v1.0.3 SystemInfo 系统信息 ----
CpuInfo cpu = SystemInfo.cpu();
System.out.println("处理器: " + cpu.availableProcessors());
System.out.println("CPU 负载: " + SystemInfo.cpuLoad());

MemoryInfo heap = SystemInfo.heapMemory();
System.out.println("堆内存: " + heap.usedDisplay() + " / " + heap.totalDisplay());

List<DiskInfo> disks = SystemInfo.disks();
disks.forEach(d -> System.out.println(d.name() + ": " + d.usagePercent() + "% 已用"));

RuntimeInfo rt = SystemInfo.runtime();
System.out.println("PID: " + rt.pid() + ", 运行时间: " + rt.uptime() + "ms");

// ---- v1.0.3 ProcessManager 进程管理 ----
ProcessResult result = ProcessManager.execute("echo", "hello");
System.out.println(result.stdout());  // "hello\n"
result.orThrow();                     // 退出码非 0 则抛出异常

ProcessConfig config = ProcessConfig.builder("git", "status")
    .workingDirectory(Path.of("/my/repo"))
    .timeout(Duration.ofSeconds(10))
    .build();
ProcessResult gitResult = ProcessManager.execute(config);

List<ProcessInfo> javaProcs = ProcessManager.findByName("java");
boolean alive = ProcessManager.isAlive(ProcessManager.currentPid());

// ---- 经典 API ----
int value = Convert.toInt("42");
Pair<String, Integer> pair = Pair.of("Alice", 30);
String joined = Joiner.on(", ").skipNulls().join("a", null, "b");
```

## 许可证

Apache License 2.0
