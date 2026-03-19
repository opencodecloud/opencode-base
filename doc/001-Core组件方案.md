# Core 组件方案

## 1. 组件概述

`opencode-base-core` 是整个体系的**基础核心组件**，提供最基本、最通用的工具能力。它是其他所有组件的公共依赖，自身**不依赖任何其他组件**和第三方库。

**设计原则**：

1. **零依赖**：仅依赖 JDK 25 标准库
2. **高内聚**：只包含最基础、最通用的工具类
3. **稳定性**：API 设计稳定，避免频繁变更
4. **性能优先**：所有工具方法追求高性能实现
5. **线程安全**：所有公共 API 保证线程安全
6. **空值安全**：所有方法验证输入，提供 Optional 方法

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-core</artifactId>
    <version>${version}</version>
</dependency>
```

## 2. 包结构

```text
cloud.opencode.base.core
├── OpenObject.java                    # 对象工具类
├── OpenArray.java                     # 数组工具类
├── OpenChar.java                      # 字符工具类
├── OpenStringBase.java                # 字符串基础工具
├── OpenNumber.java                    # 数字工具类
├── OpenBoolean.java                   # 布尔工具类
├── OpenEnum.java                      # 枚举工具类
├── OpenBit.java                       # 位操作工具类
├── OpenClass.java                     # Class 工具类
├── OpenMath.java                      # 数学工具类
├── OpenRadix.java                     # 进制转换工具
├── OpenHex.java                       # 十六进制工具
├── OpenBase64.java                    # Base64 编解码工具
├── OpenCharset.java                   # 字符集工具
├── OpenStream.java                    # Stream 工具类
├── Preconditions.java                 # 前置条件校验（Guava 风格）
├── Stopwatch.java                     # 轻量级计时器（Guava 风格）
├── MoreObjects.java                   # 对象工具扩展（含 ToStringHelper）
├── Suppliers.java                     # Supplier 工具（含 memoize 惰性缓存）
├── Range.java                         # 范围类型（开闭区间/边界检查/范围运算）
├── Ordering.java                      # 排序工具（链式比较器/null处理/复合排序）
├── Splitter.java                      # 字符串分割器（Guava风格/正则/固定长度）
├── Joiner.java                        # 字符串连接器（Guava风格/null跳过/Map连接）
├── primitives/                        # 原始类型工具
│   ├── Ints.java                     # int 数组工具
│   ├── Longs.java                    # long 数组工具
│   ├── Doubles.java                  # double 数组工具
│   ├── Booleans.java                 # boolean 数组工具
│   ├── Bytes.java                    # byte 数组工具
│   ├── Chars.java                    # char 数组工具
│   ├── Shorts.java                   # short 数组工具
│   └── Floats.java                   # float 数组工具
├── convert/                           # 类型转换
│   ├── Convert.java                  # 统一转换入口
│   ├── Converter.java                # 转换器接口
│   ├── ConverterRegistry.java        # 转换器注册表
│   ├── TypeUtil.java                 # 类型工具类
│   └── TypeReference.java            # 类型引用（泛型捕获）
├── bean/                              # Bean 操作
│   ├── OpenBean.java                 # Bean 工具类
│   ├── PropertyDescriptor.java       # 属性描述符
│   ├── PropertyConverter.java        # 属性转换器接口
│   └── BeanPath.java                 # Bean 路径访问
├── reflect/                           # 反射工具
│   ├── ReflectUtil.java              # 反射工具类
│   ├── FieldUtil.java                # 字段工具类
│   ├── MethodUtil.java               # 方法工具类
│   ├── ConstructorUtil.java          # 构造器工具类
│   ├── ModifierUtil.java             # 修饰符工具类
│   ├── RecordUtil.java               # Record 工具类
│   └── UnsafeUtil.java               # Unsafe 操作工具类
├── func/                              # 函数式接口增强
│   ├── CheckedSupplier.java          # 可抛异常的 Supplier
│   ├── CheckedConsumer.java          # 可抛异常的 Consumer
│   ├── CheckedFunction.java          # 可抛异常的 Function
│   ├── CheckedPredicate.java         # 可抛异常的 Predicate
│   ├── CheckedRunnable.java          # 可抛异常的 Runnable
│   └── CheckedCallable.java          # 可抛异常的 Callable
├── tuple/                             # 元组
│   ├── TupleUtil.java                # 元组工厂工具类
│   ├── Pair.java                     # 二元组 (Record)
│   ├── Triple.java                   # 三元组 (Record)
│   └── Quadruple.java                # 四元组 (Record)
├── stream/                            # 流与 Optional 增强
│   ├── OptionalUtil.java             # Optional 增强工具
│   └── ParallelStreamUtil.java       # 并行流工具
├── thread/                            # 线程工具
│   ├── OpenThread.java               # 线程工具类
│   ├── ScopedValueUtil.java          # Scoped Values 工具类 (JDK 25 JEP 506)
│   ├── StructuredTaskUtil.java       # 结构化并发工具类 (JDK 25 JEP 505)
│   ├── ThreadLocalUtil.java          # ThreadLocal 工具 (兼容旧代码)
│   └── NamedThreadFactory.java       # 命名线程工厂
├── random/                            # 随机工具
│   ├── OpenRandom.java               # 随机工具类
│   ├── VerifyCodeUtil.java           # 验证码生成工具
│   └── IdGenerator.java              # 简单 ID 生成接口
├── assertion/                         # 断言工具
│   └── OpenAssert.java               # 基础断言工具类
├── exception/                         # 统一异常体系
│   ├── OpenException.java            # 所有组件异常基类
│   ├── OpenIllegalArgumentException.java  # 参数校验异常
│   ├── OpenIllegalStateException.java     # 状态异常
│   ├── OpenUnsupportedOperationException.java # 不支持的操作
│   ├── OpenTimeoutException.java     # 超时异常
│   ├── OpenIOException.java          # IO 异常包装
│   └── ExceptionUtil.java            # 异常工具类
├── singleton/                         # 单例
│   └── Singleton.java                # 单例容器
├── spi/                               # SPI 支持
│   └── SpiLoader.java                # SPI 加载器
├── builder/                           # 构建器工具
│   ├── Builder.java                  # 构建器接口
│   ├── OpenBuilder.java              # 构建器工具类（统一入口）
│   ├── BeanBuilder.java              # JavaBean 构建器
│   ├── RecordBuilder.java            # Record 构建器
│   └── MapBuilder.java               # Map 构建器
└── internal/                          # 内部工具（仅供内部使用）
    ├── InternalCache.java            # 内部缓存接口
    └── InternalLRUCache.java         # 内部 LRU 缓存实现
```

---

## 3. 核心 API

### 3.1 OpenObject
> 对象工具类，提供全面的对象操作，包括空值检查、默认值处理、比较、克隆和类型检查。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `boolean isNull(Object)` | 检查对象是否为 null |
| `boolean isNotNull(Object)` | 检查对象是否非 null |
| `boolean isEmpty(Object)` | 检查对象是否为空（null/空字符串/空集合等） |
| `boolean isNotEmpty(Object)` | 检查对象是否非空 |
| `boolean isAnyNull(Object...)` | 任意一个为 null |
| `boolean isAllNull(Object...)` | 全部为 null |
| `boolean isAnyEmpty(Object...)` | 任意一个为空 |
| `boolean isAllEmpty(Object...)` | 全部为空 |
| `T defaultIfNull(T, T)` | null 时返回默认值 |
| `T defaultIfNull(T, Supplier<T>)` | null 时通过 Supplier 获取默认值 |
| `T defaultIfEmpty(T, T)` | 空时返回默认值 |
| `T firstNonNull(T...)` | 返回第一个非空值 |
| `T firstNonNull(Supplier<T>, T...)` | 返回第一个非空值，全空时使用 Supplier |
| `T requireNonNullElseGet(T, Supplier)` | 非空则返回，否则使用 Supplier |
| `R nullSafeGet(T, Function<T,R>)` | null 安全的属性获取 |
| `R nullSafeGet(T, Function<T,R>, R)` | null 安全获取，带默认值 |
| `Optional<R> nullSafeGetOptional(T, Function<T,R>)` | null 安全获取，返回 Optional |
| `boolean equals(Object, Object)` | 比较两个对象是否相等 |
| `boolean notEquals(Object, Object)` | 比较两个对象是否不等 |
| `boolean deepEquals(Object, Object)` | 深度比较（含数组） |
| `int compare(T, T)` | 比较两个 Comparable 对象 |
| `int compare(T, T, boolean)` | 比较（含 null 大小控制） |
| `T max(T, T)` | 返回较大值 |
| `T min(T, T)` | 返回较小值 |
| `boolean isBasicType(Object)` | 是否为基本类型 |
| `boolean isArray(Object)` | 是否为数组 |
| `boolean isPrimitiveArray(Object)` | 是否为原始类型数组 |
| `boolean isInstance(Object, Class)` | 是否为指定类型实例 |
| `Class<?> getType(Object)` | 获取运行时类型 |
| `boolean isWrapperType(Class)` | 是否为包装类型 |
| `boolean isPrimitiveOrWrapper(Class)` | 是否为原始类型或包装类型 |
| `int hashCode(Object...)` | 计算多值哈希 |
| `int identityHashCode(Object)` | 获取身份哈希 |
| `T clone(T)` | 克隆对象 |
| `T cloneIfPossible(T)` | 尝试克隆，失败返回原对象 |
| `byte[] serialize(Serializable)` | 序列化 |
| `T deserialize(byte[])` | 反序列化 |
| `String toString(Object)` | 转为字符串 |
| `String toString(Object, String)` | 转为字符串（含 null 默认值） |
| `String toDebugString(Object)` | 转为调试字符串（含类型信息） |
| `Optional<T> toOptional(T)` | 转为 Optional |

**示例：**

```java
// 空值检查
String name = OpenObject.defaultIfNull(userName, "Guest");
String first = OpenObject.firstNonNull(a, b, c);

// null 安全属性获取
String city = OpenObject.nullSafeGet(user, u -> u.getAddress().getCity(), "Unknown");

// 比较
int cmp = OpenObject.compare(a, b);
```

---

### 3.2 OpenArray
> 数组工具类，提供全面的数组操作，包括创建、增删、搜索、转换和集合转换，支持原始类型和对象数组。

**空数组常量：**

| 常量 | 描述 |
|------|------|
| `EMPTY_INT_ARRAY` | 空 int 数组 |
| `EMPTY_LONG_ARRAY` | 空 long 数组 |
| `EMPTY_DOUBLE_ARRAY` | 空 double 数组 |
| `EMPTY_FLOAT_ARRAY` | 空 float 数组 |
| `EMPTY_SHORT_ARRAY` | 空 short 数组 |
| `EMPTY_BYTE_ARRAY` | 空 byte 数组 |
| `EMPTY_CHAR_ARRAY` | 空 char 数组 |
| `EMPTY_BOOLEAN_ARRAY` | 空 boolean 数组 |
| `EMPTY_STRING_ARRAY` | 空 String 数组 |
| `EMPTY_OBJECT_ARRAY` | 空 Object 数组 |

**主要方法：**

| 方法 | 描述 |
|------|------|
| `T[] newArray(Class<T>, int)` | 创建指定类型和长度的数组 |
| `T[] of(T...)` | 从元素创建数组 |
| `T[] nullToEmpty(T[], Class<T[]>)` | null 转为空数组 |
| `int[] nullToEmpty(int[])` | null 转为空 int 数组 |
| `long[] nullToEmpty(long[])` | null 转为空 long 数组 |
| `T[] add(T[], T)` | 在末尾添加元素 |
| `T[] add(T[], int, T)` | 在指定位置添加元素 |
| `T[] addAll(T[], T...)` | 合并两个数组 |
| `T[] insert(int, T[], T...)` | 在指定索引处插入元素 |
| `int[] insert(int, int[], int...)` | 在指定索引处插入 int 元素 |
| `T[] remove(T[], int)` | 按索引删除元素 |
| `T[] removeElement(T[], T)` | 按值删除元素 |
| `T[] removeAll(T[], int...)` | 删除多个索引位置的元素 |
| `int[] removeAll(int[], int...)` | 删除多个索引位置的 int 元素 |
| `T[] subarray(T[], int, int)` | 获取子数组 |
| `int[] subarray(int[], int, int)` | 获取 int 子数组 |
| `long[] subarray(long[], int, int)` | 获取 long 子数组 |
| `byte[] subarray(byte[], int, int)` | 获取 byte 子数组 |
| `void swap(Object[], int, int)` | 交换两个位置的元素 |
| `void swap(int[], int, int)` | 交换两个位置的 int 元素 |
| `void swap(Object[], int, int, int)` | 交换指定长度的元素 |
| `void reverse(T[])` | 反转数组 |
| `void reverse(int[])` | 反转 int 数组 |
| `void shuffle(T[])` | 随机打乱数组 |
| `void rotate(Object[], int)` | 旋转数组 |
| `boolean contains(T[], T)` | 是否包含元素 |
| `boolean contains(int[], int)` | 是否包含 int 元素 |
| `int indexOf(T[], T)` | 查找元素首次出现的索引 |
| `int indexOf(T[], T, int)` | 从指定位置查找 |
| `int lastIndexOf(T[], T)` | 查找元素最后出现的索引 |
| `Optional<T> getFirst(T[])` | 获取第一个元素 |
| `Optional<T> getLast(T[])` | 获取最后一个元素 |
| `boolean isEmpty(Object[])` | 是否为空 |
| `boolean isEmpty(int[])` | int 数组是否为空 |
| `boolean isNotEmpty(Object[])` | 是否非空 |
| `boolean isSameLength(Object[], Object[])` | 长度是否相同 |
| `boolean isSorted(int[])` | 是否已排序 |
| `boolean isSorted(T[])` | Comparable 数组是否已排序 |
| `int[] toPrimitive(Integer[])` | 包装类数组转原始类型 |
| `int[] toPrimitive(Integer[], int)` | 转原始类型（null 替换值） |
| `long[] toPrimitive(Long[])` | Long 转 long |
| `double[] toPrimitive(Double[])` | Double 转 double |
| `boolean[] toPrimitive(Boolean[])` | Boolean 转 boolean |
| `byte[] toPrimitive(Byte[])` | Byte 转 byte |
| `char[] toPrimitive(Character[])` | Character 转 char |
| `Integer[] toObject(int[])` | int 转 Integer |
| `Long[] toObject(long[])` | long 转 Long |
| `Double[] toObject(double[])` | double 转 Double |
| `Boolean[] toObject(boolean[])` | boolean 转 Boolean |
| `Byte[] toObject(byte[])` | byte 转 Byte |
| `Character[] toObject(char[])` | char 转 Character |
| `List<T> toList(T...)` | 转为 List |
| `Set<T> toSet(T...)` | 转为 Set |
| `Map<K,V> toMap(Object[][])` | 二维数组转 Map |
| `T[] filter(T[], Predicate<T>)` | 过滤数组 |
| `R[] map(T[], Function<T,R>, Class<R>)` | 映射转换数组 |

**示例：**

```java
String[] arr = OpenArray.of("a", "b", "c");
String[] newArr = OpenArray.add(arr, "d");          // ["a", "b", "c", "d"]
String[] sub = OpenArray.subarray(arr, 0, 2);       // ["a", "b"]
List<String> list = OpenArray.toList(arr);
boolean sorted = OpenArray.isSorted(new int[]{1, 2, 3}); // true
```

---

### 3.3 OpenNumber
> 数值工具类，提供数值验证、解析、转换、格式化和范围控制。

**主要方法 - 验证：**

| 方法 | 描述 |
|------|------|
| `boolean isNumber(String)` | 是否为数字 |
| `boolean isInteger(String)` | 是否为整数 |
| `boolean isLong(String)` | 是否为 long |
| `boolean isDouble(String)` | 是否为 double |
| `boolean isCreatable(String)` | 是否可创建为 Number |
| `boolean isParsable(String)` | 是否可解析 |

**主要方法 - 解析：**

| 方法 | 描述 |
|------|------|
| `int toInt(String, int)` | 解析 int（带默认值） |
| `long toLong(String, long)` | 解析 long（带默认值） |
| `float toFloat(String, float)` | 解析 float（带默认值） |
| `double toDouble(String, double)` | 解析 double（带默认值） |
| `BigDecimal toBigDecimal(String)` | 解析 BigDecimal |
| `BigDecimal toBigDecimal(String, BigDecimal)` | 解析 BigDecimal（带默认值） |
| `BigInteger toBigInteger(String)` | 解析 BigInteger |
| `OptionalInt tryParseInt(String)` | 安全解析 int |
| `OptionalLong tryParseLong(String)` | 安全解析 long |
| `OptionalDouble tryParseDouble(String)` | 安全解析 double |

**主要方法 - 转换/比较：**

| 方法 | 描述 |
|------|------|
| `int saturatedCast(long)` | long 转 int（溢出截断） |
| `int checkedCast(long)` | long 转 int（溢出抛异常） |
| `int saturatedCast(BigDecimal)` | BigDecimal 转 int（溢出截断） |
| `int compare(int, int)` | 比较两个 int |
| `int compare(long, long)` | 比较两个 long |
| `int compare(double, double)` | 比较两个 double |
| `T max(T, T)` | 返回较大值 |
| `T min(T, T)` | 返回较小值 |
| `int max(int...)` | 数组中最大值 |
| `int min(int...)` | 数组中最小值 |
| `long max(long...)` | long 数组最大值 |
| `long min(long...)` | long 数组最小值 |
| `int clamp(int, int, int)` | 将值限制在范围内 |
| `long clamp(long, long, long)` | 将 long 值限制在范围内 |
| `double clamp(double, double, double)` | 将 double 值限制在范围内 |
| `boolean inRange(int, int, int)` | 值是否在范围内 |
| `boolean inRange(long, long, long)` | long 值是否在范围内 |

**主要方法 - 高精度运算：**

| 方法 | 描述 |
|------|------|
| `BigDecimal add(Number...)` | 多值求和 |
| `BigDecimal subtract(BigDecimal, BigDecimal)` | 减法 |
| `BigDecimal multiply(BigDecimal, BigDecimal)` | 乘法 |
| `BigDecimal divide(BigDecimal, BigDecimal, int)` | 除法（指定精度） |
| `BigDecimal divide(BigDecimal, BigDecimal, int, RoundingMode)` | 除法（指定模式） |
| `BigDecimal round(BigDecimal, int)` | 四舍五入 |
| `BigDecimal round(BigDecimal, int, RoundingMode)` | 按模式舍入 |
| `double round(double, int)` | double 四舍五入 |
| `BigDecimal roundHalfEven(BigDecimal, int)` | 银行家舍入 |
| `String format(double, String)` | 格式化 |
| `String format(BigDecimal, String)` | BigDecimal 格式化 |
| `String formatPercent(double, int)` | 百分比格式化 |
| `String formatMoney(BigDecimal)` | 货币格式化 |

**示例：**

```java
OptionalInt opt = OpenNumber.tryParseInt("123");     // OptionalInt[123]
int value = OpenNumber.toInt("abc", 0);              // 0
int clamped = OpenNumber.clamp(150, 0, 100);         // 100
BigDecimal result = OpenNumber.add(new BigDecimal("1.1"), new BigDecimal("2.2"));
```

---

### 3.4 OpenMath
> 数学工具类，提供高精度算术、统计和数论函数。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `BigDecimal add(BigDecimal, BigDecimal)` | 加法 |
| `BigDecimal subtract(BigDecimal, BigDecimal)` | 减法 |
| `BigDecimal multiply(BigDecimal, BigDecimal)` | 乘法 |
| `BigDecimal divide(BigDecimal, BigDecimal, int)` | 除法 |
| `BigDecimal divide(BigDecimal, BigDecimal, int, RoundingMode)` | 指定模式除法 |
| `double round(double, int)` | 四舍五入 |
| `double ceil(double, int)` | 向上取整 |
| `double floor(double, int)` | 向下取整 |
| `double mean(double...)` | 平均值 |
| `double median(double...)` | 中位数 |
| `double variance(double...)` | 方差 |
| `double stdDev(double...)` | 标准差 |
| `double sum(double...)` | 求和 |
| `long sum(long...)` | long 求和（溢出安全） |
| `int sum(int...)` | int 求和（溢出安全） |
| `int gcd(int, int)` | 最大公约数 |
| `long gcd(long, long)` | long 最大公约数 |
| `int lcm(int, int)` | 最小公倍数 |
| `long lcm(long, long)` | long 最小公倍数 |
| `long factorial(int)` | 阶乘 |
| `BigInteger factorialBig(int)` | 大数阶乘 |
| `boolean isPrime(long)` | 是否为素数 |
| `long fibonacci(int)` | 斐波那契数 |
| `long pow(long, int)` | 幂运算 |
| `long modPow(long, long, long)` | 模幂运算 |
| `int abs(int)` | 绝对值 |
| `long abs(long)` | long 绝对值 |
| `int signum(int)` | 符号函数 |
| `boolean isEven(int)` | 是否为偶数 |
| `boolean isOdd(int)` | 是否为奇数 |
| `boolean isNegative(int)` | 是否为负数 |
| `boolean isPositive(int)` | 是否为正数 |

**示例：**

```java
BigDecimal result = OpenMath.divide(new BigDecimal("10"), new BigDecimal("3"), 2);  // 3.33
double avg = OpenMath.mean(1.0, 2.0, 3.0, 4.0);  // 2.5
boolean prime = OpenMath.isPrime(17);               // true
int gcd = OpenMath.gcd(12, 8);                     // 4
```

---

### 3.5 OpenBoolean
> 布尔工具类，提供布尔值转换、逻辑运算和验证。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `boolean toBoolean(Boolean)` | Boolean 转 boolean（null 为 false） |
| `boolean toBoolean(String)` | 字符串转 boolean |
| `boolean toBoolean(int)` | int 转 boolean |
| `Boolean toBooleanObject(boolean)` | boolean 转 Boolean |
| `Boolean toBooleanObject(String)` | 字符串转 Boolean |
| `String toString(Boolean)` | 转为 "true"/"false" |
| `String toStringYesNo(Boolean)` | 转为 "yes"/"no" |
| `String toStringOnOff(Boolean)` | 转为 "on"/"off" |
| `String toStringYN(Boolean)` | 转为 "Y"/"N" |
| `int toInteger(boolean)` | 转为 1/0 |
| `int toInteger(Boolean)` | Boolean 转为 1/0 |
| `boolean isTrue(Boolean)` | 是否为 true |
| `boolean isFalse(Boolean)` | 是否为 false |
| `boolean isNotTrue(Boolean)` | 是否非 true |
| `boolean isNotFalse(Boolean)` | 是否非 false |
| `boolean negate(boolean)` | 取反 |
| `Boolean negate(Boolean)` | Boolean 取反 |
| `boolean and(boolean...)` | 逻辑与 |
| `boolean or(boolean...)` | 逻辑或 |
| `boolean xor(boolean...)` | 逻辑异或 |
| `int compare(boolean, boolean)` | 比较 |

**示例：**

```java
String s = OpenBoolean.toStringYesNo(true);  // "yes"
boolean b = OpenBoolean.toBoolean("yes");    // true
boolean result = OpenBoolean.and(true, true, false);  // false
```

---

### 3.6 OpenChar
> 字符工具类，提供字符类型检查、大小写转换、ASCII 和 Unicode 操作。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `boolean isLetter(char)` | 是否为字母 |
| `boolean isDigit(char)` | 是否为数字 |
| `boolean isAlphanumeric(char)` | 是否为字母或数字 |
| `boolean isWhitespace(char)` | 是否为空白字符 |
| `boolean isAscii(char)` | 是否为 ASCII 字符 |
| `boolean isPrintableAscii(char)` | 是否为可打印 ASCII |
| `boolean isControl(char)` | 是否为控制字符 |
| `boolean isUpperCase(char)` | 是否为大写 |
| `boolean isLowerCase(char)` | 是否为小写 |
| `boolean isHexDigit(char)` | 是否为十六进制数字 |
| `boolean isOctalDigit(char)` | 是否为八进制数字 |
| `char toUpperCase(char)` | 转为大写 |
| `char toLowerCase(char)` | 转为小写 |
| `char toggleCase(char)` | 切换大小写 |
| `String toString(char)` | 字符转字符串 |
| `int toCodePoint(char)` | 转为 Unicode 码点 |
| `char fromCodePoint(int)` | 从码点转字符 |
| `String toHexString(char)` | 转十六进制字符串 |
| `String toUnicode(char)` | 转为 Unicode 表示 |
| `int toDigit(char)` | 字符转数字值 |
| `int toDigit(char, int)` | 指定基数转数字 |
| `String repeat(char, int)` | 重复字符 n 次 |
| `boolean equalsIgnoreCase(char, char)` | 忽略大小写比较 |
| `int getNumericValue(char)` | 获取数值 |
| `boolean inRange(char, char, char)` | 是否在范围内 |

**示例：**

```java
String unicode = OpenChar.toUnicode('中');          // "\\u4e2d"
char upper = OpenChar.toUpperCase('a');              // 'A'
String repeated = OpenChar.repeat('*', 5);           // "*****"
boolean hex = OpenChar.isHexDigit('F');              // true
```

---

### 3.7 OpenEnum
> 枚举工具类，提供枚举验证、检索、映射、过滤和转换。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `E getEnumByName(Class<E>, String)` | 按名称获取枚举 |
| `E getEnumByNameSafely(Class<E>, String, E)` | 按名称获取（带默认值） |
| `Optional<E> getEnumByNameOptional(Class<E>, String)` | 按名称获取（返回 Optional） |
| `E getEnumByNameIgnoreCase(Class<E>, String)` | 忽略大小写按名称获取 |
| `E getEnumByValue(Class<E>, V, Function)` | 按属性值获取枚举 |
| `E getEnumByValue(Class<E>, V, Function, E)` | 按属性值获取（带默认值） |
| `List<E> getEnumList(Class<E>)` | 获取枚举列表 |
| `EnumSet<E> getEnumSet(Class<E>)` | 获取 EnumSet |
| `Map<K,E> getEnumValueMap(Class<E>, Function)` | 枚举转 Map |
| `List<String> getEnumNames(Class<E>)` | 获取所有枚举名称 |
| `boolean isValidEnum(Class<E>, String)` | 名称是否有效 |
| `boolean isValidEnumIgnoreCase(Class<E>, String)` | 忽略大小写判断是否有效 |
| `int ordinal(Enum)` | 获取序号 |
| `String name(Enum)` | 获取名称 |
| `E getByOrdinal(Class<E>, int)` | 按序号获取 |

**示例：**

```java
Status status = OpenEnum.getEnumByName(Status.class, "ACTIVE");
Status safe = OpenEnum.getEnumByNameSafely(Status.class, "UNKNOWN", Status.DEFAULT);
Status byCode = OpenEnum.getEnumByValue(Status.class, 1, Status::getCode);
List<String> names = OpenEnum.getEnumNames(Status.class);
```

---

### 3.8 OpenBit
> 位操作工具类，提供位设置、清除、翻转、测试、计数、旋转和掩码操作。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `int setBit(int, int)` | 设置指定位 |
| `long setBit(long, int)` | 设置 long 指定位 |
| `int clearBit(int, int)` | 清除指定位 |
| `long clearBit(long, int)` | 清除 long 指定位 |
| `int flipBit(int, int)` | 翻转指定位 |
| `long flipBit(long, int)` | 翻转 long 指定位 |
| `boolean testBit(int, int)` | 测试指定位 |
| `boolean testBit(long, int)` | 测试 long 指定位 |
| `int countBits(int)` | 统计置位数 |
| `int countBits(long)` | 统计 long 置位数 |
| `int countLeadingZeros(int)` | 前导零数量 |
| `int countTrailingZeros(int)` | 尾部零数量 |
| `int rotateLeft(int, int)` | 循环左移 |
| `int rotateRight(int, int)` | 循环右移 |
| `int reverse(int)` | 位反转 |
| `int reverseBytes(int)` | 字节反转 |
| `int extractField(int, int, int)` | 提取位域 |
| `int insertField(int, int, int, int)` | 插入位域 |
| `int createMask(int)` | 创建位掩码 |
| `long createMaskLong(int)` | 创建 long 位掩码 |
| `boolean isPowerOfTwo(int)` | 是否为 2 的幂 |
| `int nextPowerOfTwo(int)` | 下一个 2 的幂 |
| `int highestOneBitPosition(int)` | 最高置位位置 |
| `int lowestOneBitPosition(int)` | 最低置位位置 |

**示例：**

```java
int value = OpenBit.setBit(0, 3);         // 8 (二进制 1000)
boolean set = OpenBit.testBit(value, 3);   // true
int count = OpenBit.countBits(0xFF);       // 8
boolean pow2 = OpenBit.isPowerOfTwo(16);   // true
```

---

### 3.9 OpenClass
> Class 工具类，提供类加载、类型判断、泛型处理和类路径操作。

**主要方法 - 类加载：**

| 方法 | 描述 |
|------|------|
| `ClassLoader getClassLoader()` | 获取当前类加载器 |
| `ClassLoader getClassLoader(Class)` | 获取指定类的类加载器 |
| `Class<?> loadClass(String)` | 加载类 |
| `Class<?> loadClass(String, boolean)` | 加载类（是否初始化） |
| `Class<?> loadClass(String, ClassLoader)` | 使用指定 ClassLoader 加载 |
| `Class<?> loadClassSafely(String)` | 安全加载（不抛异常） |
| `Optional<Class<?>> loadClassOptional(String)` | 加载类（返回 Optional） |
| `boolean isPresent(String)` | 类是否存在 |
| `boolean isPresent(String, ClassLoader)` | 指定 ClassLoader 中类是否存在 |

**主要方法 - 类型判断：**

| 方法 | 描述 |
|------|------|
| `boolean isPrimitive(Class)` | 是否为原始类型 |
| `boolean isPrimitiveWrapper(Class)` | 是否为包装类型 |
| `boolean isPrimitiveOrWrapper(Class)` | 是否为原始或包装类型 |
| `boolean isArray(Class)` | 是否为数组 |
| `boolean isCollection(Class)` | 是否为集合 |
| `boolean isInterface(Class)` | 是否为接口 |
| `boolean isAbstract(Class)` | 是否为抽象类 |
| `boolean isEnum(Class)` | 是否为枚举 |
| `boolean isRecord(Class)` | 是否为 Record |
| `boolean isSealed(Class)` | 是否为密封类 |
| `boolean isInnerClass(Class)` | 是否为内部类 |
| `boolean isAnonymousClass(Class)` | 是否为匿名类 |
| `boolean isLocalClass(Class)` | 是否为本地类 |
| `boolean isLambdaClass(Class)` | 是否为 Lambda 类 |
| `boolean isAssignable(Class, Class)` | 是否可赋值 |

**主要方法 - 类型转换/泛型：**

| 方法 | 描述 |
|------|------|
| `Class<?> getWrapperClass(Class)` | 获取包装类 |
| `Class<?> getPrimitiveClass(Class)` | 获取原始类型 |
| `Class<?> getComponentType(Class)` | 获取数组组件类型 |
| `Class<?> getArrayClass(Class)` | 获取数组类 |
| `Type[] getTypeArguments(Class)` | 获取泛型参数 |
| `Type[] getSuperclassTypeArguments(Class)` | 获取父类泛型参数 |
| `Type[] getInterfaceTypeArguments(Class, Class)` | 获取接口泛型参数 |
| `Class<?> resolveTypeArgument(Class, Class)` | 解析泛型参数 |
| `T getDefaultValue(Class<T>)` | 获取类型默认值 |
| `T newInstance(Class<T>)` | 创建实例 |

**主要方法 - 名称/路径：**

| 方法 | 描述 |
|------|------|
| `String getSimpleName(Class)` | 获取简单类名 |
| `String getShortName(Class)` | 获取短名称 |
| `String getFullName(Class)` | 获取全限定名 |
| `String getPackageName(Class)` | 获取包名 |
| `String classNameToPath(String)` | 类名转路径 |
| `String pathToClassName(String)` | 路径转类名 |
| `List<Class<?>> getSuperClasses(Class)` | 获取所有父类 |
| `Set<Class<?>> getAllInterfaces(Class)` | 获取所有接口 |
| `Set<Class<?>> getAllSuperTypes(Class)` | 获取所有超类型 |
| `Class<?> getCommonSuperClass(Class, Class)` | 获取公共父类 |

**示例：**

```java
Class<?> clazz = OpenClass.loadClass("com.example.MyClass");
Class<?> wrapped = OpenClass.getWrapperClass(int.class);  // Integer.class
Object defaultVal = OpenClass.getDefaultValue(int.class);  // 0
Type[] args = OpenClass.getTypeArguments(MyClass.class);
```

---

### 3.10 OpenBase64
> Base64 编解码工具类，支持标准、URL 安全和 MIME 编码。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `String encode(byte[])` | 标准 Base64 编码 |
| `String encode(String)` | 字符串编码 |
| `String encode(String, Charset)` | 指定字符集编码 |
| `byte[] encodeToBytes(byte[])` | 编码为字节数组 |
| `byte[] decode(String)` | 解码 |
| `String decodeToString(String)` | 解码为字符串 |
| `String decodeToString(String, Charset)` | 指定字符集解码 |
| `byte[] decode(byte[])` | 字节数组解码 |
| `String encodeUrlSafe(byte[])` | URL 安全编码 |
| `String encodeUrlSafe(String)` | 字符串 URL 安全编码 |
| `byte[] decodeUrlSafe(String)` | URL 安全解码 |
| `String decodeUrlSafeToString(String)` | URL 安全解码为字符串 |
| `String encodeMime(byte[])` | MIME 编码 |
| `byte[] decodeMime(String)` | MIME 解码 |
| `String encodeNoPadding(byte[])` | 无填充编码 |
| `String encodeUrlSafeNoPadding(byte[])` | URL 安全无填充编码 |
| `boolean isBase64(String)` | 是否为有效 Base64 |
| `boolean isBase64UrlSafe(String)` | 是否为有效 URL 安全 Base64 |
| `OutputStream encodingWrap(OutputStream)` | 包装为编码输出流 |
| `InputStream decodingWrap(InputStream)` | 包装为解码输入流 |
| `OutputStream encodingWrapUrlSafe(OutputStream)` | URL 安全编码输出流 |
| `InputStream decodingWrapUrlSafe(InputStream)` | URL 安全解码输入流 |

**示例：**

```java
String encoded = OpenBase64.encode("Hello");
String decoded = OpenBase64.decodeToString(encoded);
String urlSafe = OpenBase64.encodeUrlSafe(bytes);
```

---

### 3.11 OpenHex
> 十六进制工具类，提供 Hex 编码、解码和验证。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `String encodeHex(byte[])` | 编码为小写十六进制 |
| `String encodeHexUpper(byte[])` | 编码为大写十六进制 |
| `char[] encodeHexChars(byte[])` | 编码为字符数组 |
| `char[] encodeHexChars(byte[], boolean)` | 编码（指定大小写） |
| `String byteToHex(byte)` | 单字节转十六进制 |
| `byte[] decodeHex(String)` | 十六进制解码 |
| `byte[] decodeHex(char[])` | 字符数组解码 |
| `boolean isHexNumber(String)` | 是否为十六进制数 |
| `boolean isHexString(String)` | 是否为十六进制字符串 |
| `String format(String)` | 格式化（空格分隔） |
| `String normalize(String)` | 标准化（去除分隔符） |
| `String toHex(int)` | int 转十六进制 |
| `String toHex(long)` | long 转十六进制 |
| `int toInt(String)` | 十六进制转 int |
| `long toLong(String)` | 十六进制转 long |

**示例：**

```java
String hex = OpenHex.encodeHex(bytes);           // "48656c6c6f"
String upper = OpenHex.encodeHexUpper(bytes);    // "48656C6C6F"
byte[] data = OpenHex.decodeHex("48656c6c6f");
String formatted = OpenHex.format("48656c6c6f"); // "48 65 6c 6c 6f"
```

---

### 3.12 OpenCharset
> 字符集工具类，提供字符集转换、检测和常用字符集常量。

**常量：**

| 常量 | 描述 |
|------|------|
| `UTF_8` | UTF-8 字符集 |
| `UTF_16` | UTF-16 字符集 |
| `UTF_16BE` | UTF-16 大端字符集 |
| `UTF_16LE` | UTF-16 小端字符集 |
| `ISO_8859_1` | ISO-8859-1 字符集 |
| `US_ASCII` | ASCII 字符集 |

**主要方法：**

| 方法 | 描述 |
|------|------|
| `Charset GBK()` | 获取 GBK 字符集 |
| `Charset GB2312()` | 获取 GB2312 字符集 |
| `Charset GB18030()` | 获取 GB18030 字符集 |
| `Charset charset(String)` | 按名称获取字符集 |
| `Charset charset(String, Charset)` | 按名称获取（带默认值） |
| `Optional<Charset> charsetOptional(String)` | 按名称获取（返回 Optional） |
| `Charset defaultCharset()` | 获取默认字符集 |
| `byte[] toBytes(String)` | 字符串转字节（UTF-8） |
| `byte[] toBytes(String, Charset)` | 字符串转字节 |
| `String toString(byte[])` | 字节转字符串（UTF-8） |
| `String toString(byte[], Charset)` | 字节转字符串 |
| `String convert(String, Charset, Charset)` | 字符集转换 |
| `boolean isSupported(String)` | 字符集是否支持 |
| `boolean canEncode(String, Charset)` | 是否可编码 |
| `Charset detect(byte[])` | 检测字符集 |
| `boolean hasNonAscii(String)` | 是否包含非 ASCII |
| `boolean isAscii(String)` | 是否全为 ASCII |
| `String gbkToUtf8(String)` | GBK 转 UTF-8 |
| `String utf8ToGbk(String)` | UTF-8 转 GBK |
| `String iso8859ToUtf8(String)` | ISO-8859-1 转 UTF-8 |
| `byte[] removeBom(byte[])` | 移除 BOM |
| `boolean hasBom(byte[])` | 是否有 BOM |
| `byte[] addBom(byte[])` | 添加 BOM |
| `Reader newReader(InputStream)` | 创建 UTF-8 Reader |
| `Reader newReader(InputStream, Charset)` | 创建 Reader |
| `Writer newWriter(OutputStream)` | 创建 UTF-8 Writer |
| `Writer newWriter(OutputStream, Charset)` | 创建 Writer |

**示例：**

```java
Charset utf8 = OpenCharset.UTF_8;
Charset gbk = OpenCharset.GBK();
String str = OpenCharset.gbkToUtf8(gbkString);
Charset detected = OpenCharset.detect(bytes);
```

---

### 3.13 OpenRadix
> 进制转换工具类，支持二进制、八进制、十进制、十六进制及自定义进制转换。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `String decimalToBinary(long)` | 十进制转二进制 |
| `String decimalToOctal(long)` | 十进制转八进制 |
| `String decimalToHexadecimal(long)` | 十进制转十六进制（大写） |
| `String decimalToHexadecimalLower(long)` | 十进制转十六进制（小写） |
| `String toBase(long, int)` | 转为任意进制 |
| `String toBaseExtended(long, int)` | 转为扩展进制（最大62进制） |
| `long binaryToDecimal(String)` | 二进制转十进制 |
| `long octalToDecimal(String)` | 八进制转十进制 |
| `long hexadecimalToDecimal(String)` | 十六进制转十进制 |
| `long fromBase(String, int)` | 任意进制转十进制 |
| `long fromBaseExtended(String, int)` | 扩展进制转十进制 |
| `String binaryToHex(String)` | 二进制转十六进制 |
| `String hexToBinary(String)` | 十六进制转二进制 |
| `String convert(String, int, int)` | 进制间转换 |
| `String formatBinary(long)` | 格式化二进制 |
| `String formatHex(long)` | 格式化十六进制 |
| `boolean isBinary(String)` | 是否为二进制字符串 |
| `boolean isOctal(String)` | 是否为八进制字符串 |
| `boolean isHexadecimal(String)` | 是否为十六进制字符串 |

**示例：**

```java
String binary = OpenRadix.decimalToBinary(255);    // "11111111"
String hex = OpenRadix.binaryToHex("11111111");    // "FF"
String base36 = OpenRadix.toBase(1000, 36);        // "RS"
```

---

### 3.14 OpenStringBase
> 字符串基础工具类，提供最小化的字符串操作（完整版见 String 组件）。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `boolean isEmpty(CharSequence)` | 是否为空 |
| `boolean isNotEmpty(CharSequence)` | 是否非空 |
| `boolean isBlank(CharSequence)` | 是否为空白 |
| `boolean isNotBlank(CharSequence)` | 是否非空白 |
| `boolean hasLength(CharSequence)` | 是否有长度 |
| `boolean hasText(CharSequence)` | 是否有文本 |
| `int length(CharSequence)` | 获取长度 |
| `String defaultIfEmpty(String, String)` | 空时返回默认值 |
| `String defaultIfBlank(String, String)` | 空白时返回默认值 |
| `String nullToEmpty(String)` | null 转为空字符串 |
| `String emptyToNull(String)` | 空字符串转为 null |
| `String trimToNull(String)` | trim 后为空则返回 null |
| `String trimToEmpty(String)` | trim 后为空则返回 "" |
| `boolean equals(CharSequence, CharSequence)` | 比较 |
| `boolean equalsIgnoreCase(String, String)` | 忽略大小写比较 |
| `String trim(String)` | 去除空白 |
| `String toLowerCase(String)` | 转小写 |
| `String toUpperCase(String)` | 转大写 |
| `boolean startsWith(String, String)` | 是否以前缀开始 |
| `boolean endsWith(String, String)` | 是否以后缀结束 |
| `boolean contains(CharSequence, CharSequence)` | 是否包含子串 |

**示例：**

```java
String value = OpenStringBase.defaultIfBlank(str, "default");
String trimmed = OpenStringBase.trimToEmpty(str);
boolean blank = OpenStringBase.isBlank("  ");  // true
```

---

### 3.15 OpenStream
> Stream 工具类，提供增强的 Stream 操作，支持 JDK 25 Gatherers。

**主要方法 - 创建：**

| 方法 | 描述 |
|------|------|
| `Stream<T> of(T...)` | 从元素创建 Stream |
| `Stream<T> from(Iterable<T>)` | 从 Iterable 创建 |
| `Stream<T> from(Iterator<T>)` | 从 Iterator 创建 |
| `Stream<T> from(Optional<T>)` | 从 Optional 创建 |
| `IntStream range(int, int)` | 创建 int 范围流 |
| `IntStream rangeClosed(int, int)` | 创建闭区间 int 范围流 |
| `LongStream range(long, long)` | 创建 long 范围流 |
| `Stream<T> generate(Supplier<T>)` | 生成流 |
| `Stream<T> iterate(T, UnaryOperator<T>)` | 迭代流 |
| `Stream<T> iterate(T, Predicate<T>, UnaryOperator<T>)` | 带终止条件的迭代流 |

**主要方法 - 窗口/批量：**

| 方法 | 描述 |
|------|------|
| `List<List<T>> batch(Stream<T>, int)` | 按批次分组 |
| `Stream<List<T>> batchStream(Collection<T>, int)` | 批次流 |
| `Stream<List<T>> slidingWindow(Collection<T>, int)` | 滑动窗口 |
| `Stream<List<T>> slidingWindow(Collection<T>, int, int)` | 滑动窗口（指定步长） |
| `Stream<List<T>> tumblingWindow(Collection<T>, int)` | 翻转窗口 |

**主要方法 - 组合：**

| 方法 | 描述 |
|------|------|
| `Stream<R> zip(Stream<A>, Stream<B>, BiFunction)` | 合并两个流 |
| `Stream<IndexedValue<T>> zipWithIndex(Stream<T>)` | 带索引的流 |
| `Stream<T> merge(Stream<T>...)` | 合并多个流 |
| `Stream<T> interleave(Stream<T>, Stream<T>)` | 交错合并 |

**主要方法 - 过滤/转换：**

| 方法 | 描述 |
|------|------|
| `Stream<T> filterNulls(Stream<T>)` | 过滤 null |
| `Stream<T> distinctBy(Stream<T>, Function)` | 按键去重 |
| `Stream<T> takeWhile(Stream<T>, Predicate)` | 取满足条件的前缀 |
| `Stream<T> dropWhile(Stream<T>, Predicate)` | 跳过满足条件的前缀 |
| `Optional<T> findFirst(Stream<T>, Predicate)` | 查找第一个匹配 |

**主要方法 - 收集：**

| 方法 | 描述 |
|------|------|
| `List<T> toUnmodifiableList(Stream<T>)` | 转为不可变 List |
| `Set<T> toUnmodifiableSet(Stream<T>)` | 转为不可变 Set |
| `Map<K,T> toMap(Stream<T>, Function)` | 转为 Map |
| `Map<K,List<T>> groupBy(Stream<T>, Function)` | 分组 |
| `Map<Boolean,List<T>> partitionBy(Stream<T>, Predicate)` | 分区 |
| `String joining(Stream<T>, CharSequence)` | 连接为字符串 |
| `List<R> parallelMap(Collection<T>, Function, Executor)` | 并行映射 |

**示例：**

```java
List<List<User>> batches = OpenStream.batch(users.stream(), 100);
Stream<List<Integer>> windows = OpenStream.slidingWindow(numbers, 3);
Stream<Pair<A, B>> zipped = OpenStream.zip(streamA, streamB, Pair::of);
Map<String, List<User>> grouped = OpenStream.groupBy(users.stream(), User::getDept);
```

---

### 3.16 Preconditions
> 前置条件校验，Guava 风格的参数验证和状态检查。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `T checkNotNull(T)` | 非 null 检查 |
| `T checkNotNull(T, String)` | 非 null 检查（含消息） |
| `T checkNotNull(T, String, Object...)` | 非 null 检查（含格式化消息） |
| `void checkArgument(boolean)` | 参数条件检查 |
| `void checkArgument(boolean, String)` | 参数条件检查（含消息） |
| `void checkArgument(boolean, String, Object...)` | 参数条件检查（含格式化消息） |
| `void checkState(boolean)` | 状态条件检查 |
| `void checkState(boolean, String)` | 状态条件检查（含消息） |
| `void checkState(boolean, String, Object...)` | 状态条件检查（含格式化消息） |
| `int checkElementIndex(int, int)` | 元素索引检查 |
| `int checkElementIndex(int, int, String)` | 元素索引检查（含描述） |
| `int checkPositionIndex(int, int)` | 位置索引检查 |
| `int checkPositionIndex(int, int, String)` | 位置索引检查（含描述） |
| `void checkPositionIndexes(int, int, int)` | 位置索引范围检查 |

**示例：**

```java
Preconditions.checkArgument(age > 0, "age must be positive, got: %s", age);
Preconditions.checkState(isInitialized, "service not initialized");
Preconditions.checkNotNull(user, "user must not be null");
```

---

### 3.17 Stopwatch
> 轻量级计时器，用于测量代码执行时间。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `Stopwatch createUnstarted()` | 创建未启动的计时器 |
| `Stopwatch createStarted()` | 创建并启动计时器 |
| `Stopwatch start()` | 启动 |
| `Stopwatch stop()` | 停止 |
| `Stopwatch reset()` | 重置 |
| `boolean isRunning()` | 是否运行中 |
| `Duration elapsed()` | 获取已用时间 |
| `long elapsed(TimeUnit)` | 获取指定单位的时间 |
| `long elapsedNanos()` | 获取纳秒 |
| `long elapsedMillis()` | 获取毫秒 |
| `long elapsedSeconds()` | 获取秒 |
| `String toString()` | 格式化输出（如 "123.4 ms"） |

**示例：**

```java
Stopwatch sw = Stopwatch.createStarted();
doSomething();
sw.stop();
System.out.println("Elapsed: " + sw);  // "Elapsed: 123.4 ms"
```

---

### 3.18 MoreObjects
> 对象工具扩展，包含 ToStringHelper。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `T firstNonNull(T, T)` | 返回第一个非 null 值 |
| `ToStringHelper toStringHelper(Object)` | 创建 ToString 辅助器 |
| `ToStringHelper toStringHelper(Class)` | 创建 ToString 辅助器 |
| `ToStringHelper toStringHelper(String)` | 创建 ToString 辅助器 |

**ToStringHelper 方法：**

| 方法 | 描述 |
|------|------|
| `ToStringHelper omitNullValues()` | 省略 null 值 |
| `ToStringHelper add(String, Object)` | 添加名值对 |
| `ToStringHelper add(String, int/long/double/...)` | 添加原始类型名值对 |
| `ToStringHelper addValue(Object)` | 添加匿名值 |
| `String toString()` | 生成字符串 |

**示例：**

```java
String str = MoreObjects.toStringHelper(this)
    .add("name", name)
    .add("age", age)
    .omitNullValues()
    .toString();  // "User{name=Leon, age=25}"
```

---

### 3.19 Suppliers
> Supplier 工具类，提供缓存、过期、组合等功能。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `Supplier<T> memoize(Supplier<T>)` | 惰性缓存（线程安全） |
| `Supplier<T> memoizeWithExpiration(Supplier<T>, long, TimeUnit)` | 带过期的缓存 |
| `Supplier<T> memoizeWithExpiration(Supplier<T>, Duration)` | 带过期的缓存 |
| `Supplier<T> compose(Function, Supplier)` | 组合 Function 和 Supplier |
| `Supplier<T> synchronizedSupplier(Supplier<T>)` | 同步 Supplier |
| `Supplier<T> ofInstance(T)` | 常量 Supplier |

**示例：**

```java
Supplier<ExpensiveObject> supplier = Suppliers.memoize(() -> new ExpensiveObject());
ExpensiveObject obj1 = supplier.get(); // 计算
ExpensiveObject obj2 = supplier.get(); // 返回缓存

Supplier<Config> configSupplier = Suppliers.memoizeWithExpiration(
    () -> loadConfig(), Duration.ofMinutes(5));
```

---

### 3.20 Range
> 范围类型，支持开闭区间、边界检查和范围运算。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `Range<C> closed(C, C)` | 闭区间 [a, b] |
| `Range<C> open(C, C)` | 开区间 (a, b) |
| `Range<C> closedOpen(C, C)` | 左闭右开 [a, b) |
| `Range<C> openClosed(C, C)` | 左开右闭 (a, b] |
| `Range<C> atMost(C)` | (-inf, b] |
| `Range<C> lessThan(C)` | (-inf, b) |
| `Range<C> atLeast(C)` | [a, +inf) |
| `Range<C> greaterThan(C)` | (a, +inf) |
| `Range<C> all()` | (-inf, +inf) |
| `Range<C> singleton(C)` | 单值 [a, a] |
| `Range<C> encloseAll(C...)` | 包含所有值的最小范围 |
| `boolean contains(C)` | 是否包含值 |
| `boolean containsAll(Iterable<C>)` | 是否包含所有值 |
| `boolean encloses(Range<C>)` | 是否包含另一个范围 |
| `boolean isConnected(Range<C>)` | 是否连通 |
| `Range<C> intersection(Range<C>)` | 交集 |
| `Range<C> span(Range<C>)` | 并集跨度 |
| `Optional<Range<C>> gap(Range<C>)` | 间隙 |
| `boolean isEmpty()` | 是否为空 |

**示例：**

```java
Range<Integer> closed = Range.closed(1, 10);        // [1, 10]
Range<Integer> open = Range.open(1, 10);             // (1, 10)
boolean contains = closed.contains(5);                // true
Range<Integer> intersection = closed.intersection(Range.closed(5, 15));  // [5, 10]
```

---

### 3.21 Ordering
> 排序工具，链式比较器构建，支持 null 处理和复合排序。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `Ordering<T> natural()` | 自然排序 |
| `Ordering<T> from(Comparator<T>)` | 从 Comparator 创建 |
| `Ordering<T> from(Function)` | 按属性排序 |
| `Ordering<T> allEqual()` | 所有元素相等 |
| `Ordering<T> explicit(T...)` | 显式顺序 |
| `Ordering<T> reversed()` | 反转 |
| `Ordering<T> nullsFirst()` | null 排在前面 |
| `Ordering<T> nullsLast()` | null 排在后面 |
| `Ordering<T> thenComparing(Comparator)` | 复合排序 |
| `Ordering<F> onResultOf(Function)` | 转换后排序 |
| `T min(T, T)` | 最小值 |
| `T max(T, T)` | 最大值 |
| `T min(Iterable<T>)` | 集合最小值 |
| `T max(Iterable<T>)` | 集合最大值 |
| `List<T> leastOf(Iterable<T>, int)` | 最小的 k 个 |
| `List<T> greatestOf(Iterable<T>, int)` | 最大的 k 个 |
| `List<T> sortedCopy(Iterable<T>)` | 排序副本 |
| `List<T> immutableSortedCopy(Iterable<T>)` | 不可变排序副本 |
| `boolean isOrdered(Iterable<T>)` | 是否已排序 |
| `boolean isStrictlyOrdered(Iterable<T>)` | 是否严格排序 |

**示例：**

```java
Ordering<String> natural = Ordering.natural();
Ordering<String> nullsFirst = Ordering.<String>natural().nullsFirst();
Ordering<Person> byAge = Ordering.from(Person::getAge);
String min = Ordering.<String>natural().min("apple", "banana");  // "apple"
List<String> top3 = Ordering.<String>natural().leastOf(list, 3);
```

---

### 3.22 Splitter
> 字符串分割器，Guava 风格，支持正则和固定长度。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `Splitter on(char)` | 按字符分割 |
| `Splitter on(String)` | 按字符串分割 |
| `Splitter on(Pattern)` | 按正则分割 |
| `Splitter onPattern(String)` | 按正则字符串分割 |
| `Splitter fixedLength(int)` | 固定长度分割 |
| `Splitter omitEmptyStrings()` | 跳过空字符串 |
| `Splitter trimResults()` | 去除空白 |
| `Splitter trimResults(Function)` | 自定义 trim |
| `Splitter limit(int)` | 限制分割数量 |
| `Iterable<String> split(CharSequence)` | 分割（惰性） |
| `List<String> splitToList(CharSequence)` | 分割为 List |
| `Stream<String> splitToStream(CharSequence)` | 分割为 Stream |
| `MapSplitter withKeyValueSeparator(char)` | 创建 Map 分割器 |
| `MapSplitter withKeyValueSeparator(String)` | 创建 Map 分割器 |

**示例：**

```java
List<String> parts = Splitter.on(',').splitToList("a,b,c");  // ["a", "b", "c"]
List<String> trimmed = Splitter.on(',').trimResults().splitToList("a , b , c");
List<String> fixed = Splitter.fixedLength(3).splitToList("abcdefg");  // ["abc", "def", "g"]
Map<String, String> map = Splitter.on(',').withKeyValueSeparator('=')
    .split("a=1,b=2");  // {a=1, b=2}
```

---

### 3.23 Joiner
> 字符串连接器，Guava 风格，支持 null 跳过和 Map 连接。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `Joiner on(char)` | 按字符连接 |
| `Joiner on(String)` | 按字符串连接 |
| `Joiner skipNulls()` | 跳过 null |
| `Joiner useForNull(String)` | null 替换文本 |
| `Joiner withFormatter(Function)` | 自定义格式化 |
| `String join(Object...)` | 连接多个对象 |
| `String join(Iterable)` | 连接 Iterable |
| `String join(Iterator)` | 连接 Iterator |
| `A appendTo(A, Object...)` | 追加到 Appendable |
| `StringBuilder appendTo(StringBuilder, Object...)` | 追加到 StringBuilder |
| `MapJoiner withKeyValueSeparator(char)` | 创建 Map 连接器 |
| `MapJoiner withKeyValueSeparator(String)` | 创建 Map 连接器 |

**示例：**

```java
String result = Joiner.on(',').join("a", "b", "c");  // "a,b,c"
String safe = Joiner.on(',').skipNulls().join("a", null, "b");  // "a,b"
Map<String, Integer> map = Map.of("a", 1, "b", 2);
String mapStr = Joiner.on(',').withKeyValueSeparator('=').join(map);  // "a=1,b=2"
```

---

### 3.24 原始类型工具（primitives 包）

#### Ints
> int 数组工具类，Guava 风格。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `byte[] toByteArray(int)` | 转为字节数组 |
| `int fromByteArray(byte[])` | 从字节数组转换 |
| `int fromBytes(byte, byte, byte, byte)` | 从 4 字节构造 |
| `int[] concat(int[]...)` | 合并多个数组 |
| `boolean contains(int[], int)` | 是否包含 |
| `int indexOf(int[], int)` | 查找索引 |
| `int indexOf(int[], int, int, int)` | 范围内查找 |
| `int indexOf(int[], int[])` | 子数组查找 |
| `int lastIndexOf(int[], int)` | 最后出现索引 |
| `int min(int...)` | 最小值 |
| `int max(int...)` | 最大值 |
| `int constrainToRange(int, int, int)` | 限制范围 |
| `int saturatedCast(long)` | 饱和转换 |
| `int checkedCast(long)` | 检查转换 |
| `int compare(int, int)` | 比较 |
| `Comparator<int[]> lexicographicalComparator()` | 字典序比较器 |
| `List<Integer> asList(int...)` | 转为 List |
| `int[] toArray(Collection)` | 从 Collection 转换 |
| `void reverse(int[])` | 反转 |
| `void rotate(int[], int)` | 旋转 |
| `void sortDescending(int[])` | 降序排序 |
| `boolean isSorted(int[])` | 是否已排序 |
| `String join(String, int...)` | 连接 |
| `Integer tryParse(String)` | 安全解析 |
| `Integer tryParse(String, int)` | 指定基数解析 |
| `int[] ensureCapacity(int[], int, int)` | 确保容量 |

> 类似的 API 也适用于 **Longs**, **Doubles**, **Floats**, **Shorts**, **Bytes**, **Chars**, **Booleans**，各自针对对应原始类型提供 concat/contains/indexOf/min/max/reverse/join/asList/toArray 等方法。

**示例：**

```java
int[] merged = Ints.concat(new int[]{1, 2}, new int[]{3, 4});  // [1, 2, 3, 4]
int index = Ints.indexOf(new int[]{1, 2, 3}, 2);  // 1
List<Integer> list = Ints.asList(1, 2, 3);
String joined = Ints.join(",", 1, 2, 3);  // "1,2,3"
```

---

### 3.25 类型转换（convert 包）

#### Convert
> 统一类型转换入口。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `Integer toInt(Object)` | 转为 Integer |
| `Integer toInt(Object, Integer)` | 转为 Integer（带默认值） |
| `Long toLong(Object)` | 转为 Long |
| `Long toLong(Object, Long)` | 转为 Long（带默认值） |
| `Double toDouble(Object)` | 转为 Double |
| `Float toFloat(Object)` | 转为 Float |
| `Short toShort(Object)` | 转为 Short |
| `Byte toByte(Object)` | 转为 Byte |
| `Boolean toBool(Object)` | 转为 Boolean |
| `Character toChar(Object)` | 转为 Character |
| `String toStr(Object)` | 转为 String |
| `int[] toIntArray(Object)` | 转为 int 数组 |
| `long[] toLongArray(Object)` | 转为 long 数组 |
| `String[] toStrArray(Object)` | 转为 String 数组 |
| `List<T> toList(Object, Class<T>)` | 转为 List |
| `Set<T> toSet(Object, Class<T>)` | 转为 Set |
| `T convert(Object, Class<T>)` | 通用转换 |
| `T convert(Object, TypeReference<T>)` | 泛型转换 |

#### TypeReference
> 类型引用，运行时捕获泛型类型信息。

| 方法 | 描述 |
|------|------|
| `Type getType()` | 获取泛型类型 |
| `Class<T> getRawType()` | 获取原始类型 |

#### TypeUtil
> 类型工具类。

| 方法 | 描述 |
|------|------|
| `boolean isPrimitive(Class)` | 是否为原始类型 |
| `boolean isWrapper(Class)` | 是否为包装类型 |
| `boolean isNumber(Class)` | 是否为数字类型 |
| `boolean isCollection(Class)` | 是否为集合类型 |
| `boolean isMap(Class)` | 是否为 Map 类型 |
| `boolean isArray(Class)` | 是否为数组类型 |
| `boolean isString(Class)` | 是否为 String |
| `boolean isDateTime(Class)` | 是否为日期时间类型 |
| `Class<?> getWrapperClass(Class)` | 获取包装类 |
| `Class<?> getPrimitiveClass(Class)` | 获取原始类型 |
| `T getDefaultValue(Class<T>)` | 获取类型默认值 |
| `T convert(Object, Class<T>)` | 类型转换 |
| `Type[] getGenericTypes(Type)` | 获取泛型参数 |
| `Class<?> getRawType(Type)` | 获取原始类型 |
| `boolean isAssignable(Class, Class)` | 是否可赋值 |

#### ConverterRegistry
> 转换器注册表。

| 方法 | 描述 |
|------|------|
| `void register(Type, Converter)` | 注册转换器 |
| `Converter<T> getConverter(Type)` | 获取转换器 |
| `boolean hasConverter(Type)` | 是否有转换器 |
| `void unregister(Type)` | 注销转换器 |
| `int size()` | 已注册数量 |

**示例：**

```java
Integer num = Convert.toInt("123", 0);
Boolean b = Convert.toBool("true");
List<String> list = Convert.toList("[1,2,3]", String.class);
TypeReference<List<String>> ref = new TypeReference<List<String>>() {};
```

---

### 3.26 Bean 操作（bean 包）

#### OpenBean
> Bean 工具类，提供 JavaBean 属性操作和转换。

**主要方法 - 复制：**

| 方法 | 描述 |
|------|------|
| `void copyProperties(Object, Object)` | 属性复制 |
| `void copyProperties(Object, Object, String...)` | 属性复制（忽略指定属性） |
| `void copyProperties(Object, Object, Map<String,String>)` | 属性复制（属性映射） |
| `void copyProperties(Object, Object, PropertyConverter)` | 属性复制（自定义转换） |
| `T copyToNew(Object, Class<T>)` | 复制到新实例 |
| `T copyToNew(Object, Class<T>, String...)` | 复制到新实例（忽略属性） |
| `void deepCopyProperties(Object, Object)` | 深拷贝属性 |

**主要方法 - Bean/Map 互转：**

| 方法 | 描述 |
|------|------|
| `Map<String,Object> toMap(Object)` | Bean 转 Map |
| `Map<String,Object> toMap(Object, String...)` | 转 Map（忽略属性） |
| `Map<String,Object> toMapNonNull(Object)` | 转 Map（排除 null） |
| `Map<String,Object> toUnderlineKeyMap(Object)` | 转下划线 key 的 Map |
| `T toBean(Map, Class<T>)` | Map 转 Bean |
| `T toBean(Map, Class<T>, Map<String,String>)` | Map 转 Bean（属性映射） |
| `T toBeanFromUnderlineKey(Map, Class<T>)` | 下划线 key Map 转 Bean |

**主要方法 - 属性操作：**

| 方法 | 描述 |
|------|------|
| `Object getProperty(Object, String)` | 获取属性值 |
| `T getProperty(Object, String, Class<T>)` | 获取属性值（指定类型） |
| `void setProperty(Object, String, Object)` | 设置属性值 |
| `void setProperties(Object, Map)` | 批量设置属性 |
| `List<PropertyDescriptor> getPropertyDescriptors(Class)` | 获取属性描述符 |
| `List<String> getPropertyNames(Class)` | 获取属性名列表 |
| `boolean hasProperty(Class, String)` | 是否有指定属性 |
| `Class<?> getPropertyType(Class, String)` | 获取属性类型 |

**主要方法 - 比较/Record：**

| 方法 | 描述 |
|------|------|
| `boolean equals(Object, Object)` | Bean 属性比较 |
| `Map<String,Object[]> diff(Object, Object)` | 属性差异 |
| `boolean isEmpty(Object)` | 所有属性是否为空 |
| `T fromRecord(Record, Class<T>)` | Record 转 Bean |
| `T toRecord(Object, Class<T>)` | Bean 转 Record |

#### BeanPath
> Bean 路径访问工具，支持嵌套属性、数组索引和 Map key。

| 方法 | 描述 |
|------|------|
| `Object get(Object, String)` | 按路径获取值 |
| `T get(Object, String, Class<T>)` | 按路径获取值（指定类型） |
| `Optional<T> getOptional(Object, String, Class<T>)` | 按路径获取（返回 Optional） |
| `void set(Object, String, Object)` | 按路径设置值 |
| `void setWithCreate(Object, String, Object)` | 按路径设置值（自动创建中间对象） |
| `boolean exists(Object, String)` | 路径是否存在 |
| `boolean isNull(Object, String)` | 路径值是否为 null |
| `List<PathSegment> parsePath(String)` | 解析路径 |

**示例：**

```java
OpenBean.copyProperties(source, target);
Map<String, Object> map = OpenBean.toMap(user);
User user = OpenBean.toBean(map, User.class);

String city = BeanPath.get(user, "address.city", String.class);
Item item = BeanPath.get(order, "items[0]", Item.class);
BeanPath.setWithCreate(user, "address.city", "Beijing");
```

---

### 3.27 反射工具（reflect 包）

#### ReflectUtil
> 反射工具类，带缓存的核心反射操作。

| 方法 | 描述 |
|------|------|
| `T newInstance(Class<T>)` | 创建实例 |
| `T newInstance(Class<T>, Object...)` | 带参数创建实例 |
| `T invoke(Object, String, Object...)` | 调用方法 |
| `T invokeStatic(Class, String, Object...)` | 调用静态方法 |
| `T getFieldValue(Object, String)` | 获取字段值 |
| `void setFieldValue(Object, String, Object)` | 设置字段值 |
| `T getStaticFieldValue(Class, String)` | 获取静态字段值 |
| `void setStaticFieldValue(Class, String, Object)` | 设置静态字段值 |
| `Field[] getFields(Class)` | 获取所有字段 |
| `Field getField(Class, String)` | 获取指定字段 |
| `Method[] getMethods(Class)` | 获取所有方法 |
| `Method getMethod(Class, String, Class...)` | 获取指定方法 |
| `Constructor<?>[] getConstructors(Class)` | 获取所有构造器 |
| `Constructor<T> getDefaultConstructor(Class<T>)` | 获取默认构造器 |

#### FieldUtil
> 字段工具类。

| 方法 | 描述 |
|------|------|
| `List<Field> getAllFields(Class)` | 获取所有字段（含父类） |
| `List<Field> getDeclaredFields(Class)` | 获取本类声明字段 |
| `Optional<Field> getFieldByName(Class, String)` | 按名称查找 |
| `List<Field> getFieldsWithAnnotation(Class, Class)` | 按注解查找 |
| `List<Field> getFieldsByType(Class, Class)` | 按类型查找 |
| `List<Field> getStaticFields(Class)` | 获取静态字段 |
| `List<Field> getInstanceFields(Class)` | 获取实例字段 |
| `T getValue(Object, Field)` | 获取值 |
| `void setValue(Object, Field, Object)` | 设置值 |
| `Map<String,Field> getFieldMap(Class)` | 字段名到字段的映射 |

#### MethodUtil
> 方法工具类。

| 方法 | 描述 |
|------|------|
| `List<Method> getAllMethods(Class)` | 获取所有方法（含父类） |
| `List<Method> getMethodsByName(Class, String)` | 按名称查找 |
| `Optional<Method> getMethod(Class, String, Class...)` | 查找方法 |
| `List<Method> getMethodsWithAnnotation(Class, Class)` | 按注解查找 |
| `List<Method> getGetterMethods(Class)` | 获取 getter 方法 |
| `List<Method> getSetterMethods(Class)` | 获取 setter 方法 |
| `boolean isGetter(Method)` | 是否为 getter |
| `boolean isSetter(Method)` | 是否为 setter |
| `String getPropertyNameFromGetter(Method)` | 从 getter 获取属性名 |
| `T invoke(Object, Method, Object...)` | 调用方法 |

#### ConstructorUtil
> 构造器工具类。

| 方法 | 描述 |
|------|------|
| `List<Constructor<T>> getAllConstructors(Class<T>)` | 获取所有构造器 |
| `Optional<Constructor<T>> getDefaultConstructor(Class<T>)` | 获取默认构造器 |
| `Optional<Constructor<T>> getConstructor(Class<T>, Class...)` | 查找构造器 |
| `boolean hasDefaultConstructor(Class)` | 是否有默认构造器 |
| `T newInstance(Constructor<T>, Object...)` | 创建实例 |
| `T newInstance(Class<T>)` | 使用默认构造器创建实例 |
| `String[] getParameterNames(Constructor)` | 获取参数名 |

#### RecordUtil
> Record 工具类。

| 方法 | 描述 |
|------|------|
| `boolean isRecord(Class)` | 是否为 Record |
| `RecordComponent[] getComponents(Class)` | 获取组件 |
| `List<String> getComponentNames(Class)` | 获取组件名列表 |
| `Map<String,Class<?>> getComponentTypes(Class)` | 获取组件类型 |
| `T getComponentValue(Object, String)` | 获取组件值 |
| `Map<String,Object> toMap(Object)` | Record 转 Map |
| `T fromMap(Map, Class<T>)` | Map 转 Record |
| `T copyWith(T, String, Object)` | 复制并修改 |
| `T copyWith(T, Map<String,Object>)` | 复制并批量修改 |
| `boolean equals(Object, Object)` | 比较两个 Record |

**示例：**

```java
User user = ReflectUtil.newInstance(User.class);
String name = ReflectUtil.invoke(user, "getName");
ReflectUtil.setFieldValue(user, "name", "Leon");

Map<String, Object> map = RecordUtil.toMap(userRecord);
User updated = RecordUtil.copyWith(user, "name", "NewName");
```

---

### 3.28 函数式接口（func 包）

> 提供可抛受检异常的函数式接口，是 JDK 标准函数式接口的增强版。

| 接口 | 描述 |
|------|------|
| `CheckedSupplier<T>` | 可抛异常的 Supplier |
| `CheckedConsumer<T>` | 可抛异常的 Consumer |
| `CheckedFunction<T,R>` | 可抛异常的 Function |
| `CheckedPredicate<T>` | 可抛异常的 Predicate |
| `CheckedRunnable` | 可抛异常的 Runnable |
| `CheckedCallable<V>` | 可抛异常的 Callable |

**示例：**

```java
CheckedFunction<Path, String> reader = path -> Files.readString(path);
Function<Path, String> wrapped = reader.unchecked();
String result = reader.applyOrDefault(path, "fallback");
```

---

### 3.29 元组（tuple 包）

#### Pair
> 二元组（Record 实现）。

| 方法 | 描述 |
|------|------|
| `Pair<L,R> of(L, R)` | 创建 |
| `Pair<K,V> fromEntry(Map.Entry)` | 从 Entry 创建 |
| `Pair<L,R> empty()` | 空 Pair |
| `L left() / first() / key()` | 获取左值 |
| `R right() / second() / value()` | 获取右值 |
| `Pair<R,L> swap()` | 交换 |
| `Pair<T,R> mapLeft(Function)` | 映射左值 |
| `Pair<L,T> mapRight(Function)` | 映射右值 |
| `T apply(BiFunction)` | 应用函数 |
| `Map.Entry<L,R> toEntry()` | 转为 Entry |

#### Triple
> 三元组（Record 实现）。

| 方法 | 描述 |
|------|------|
| `Triple<A,B,C> of(A, B, C)` | 创建 |
| `A first() / left()` | 获取第一个值 |
| `B second() / middle()` | 获取第二个值 |
| `C third() / right()` | 获取第三个值 |
| `Triple<T,B,C> mapFirst(Function)` | 映射第一个值 |
| `Pair<A,B> toFirstPair()` | 转为前两个元素的 Pair |
| `Pair<B,C> toLastPair()` | 转为后两个元素的 Pair |

#### Quadruple
> 四元组（Record 实现）。

| 方法 | 描述 |
|------|------|
| `Quadruple<A,B,C,D> of(A, B, C, D)` | 创建 |
| `A first()` | 获取第一个值 |
| `B second()` | 获取第二个值 |
| `C third()` | 获取第三个值 |
| `D fourth()` | 获取第四个值 |
| `Triple<A,B,C> toFirstTriple()` | 转为前三个元素的 Triple |

**示例：**

```java
Pair<String, Integer> pair = Pair.of("name", 25);
Triple<String, Integer, Boolean> triple = Triple.of("name", 25, true);
Pair<String, String> mapped = pair.mapRight(String::valueOf);
```

---

### 3.30 线程工具（thread 包）

#### OpenThread
> 线程工具类，线程池创建和管理。

| 方法 | 描述 |
|------|------|
| `ExecutorService createFixedThreadPool(int, String)` | 创建固定线程池 |
| `ExecutorService createCachedThreadPool(String)` | 创建缓存线程池 |
| `ExecutorService createSingleThreadExecutor(String)` | 创建单线程执行器 |
| `ScheduledExecutorService createScheduledThreadPool(int, String)` | 创建调度线程池 |
| `ExecutorService createVirtualThreadExecutor()` | 创建虚拟线程执行器 |
| `ExecutorService createVirtualThreadExecutor(String)` | 创建命名虚拟线程执行器 |
| `CompletableFuture<Void> runAsync(Runnable)` | 异步运行 |
| `CompletableFuture<T> supplyAsync(Supplier<T>)` | 异步获取 |
| `CompletableFuture<T> executeAsync(Supplier<T>, Duration)` | 异步执行（超时） |
| `void sleep(Duration)` | 休眠 |
| `void sleepMillis(long)` | 休眠（毫秒） |
| `boolean isVirtualThread()` | 当前是否为虚拟线程 |
| `boolean isVirtualThread(Thread)` | 指定线程是否为虚拟线程 |
| `boolean shutdownGracefully(ExecutorService, Duration)` | 优雅关闭 |

#### ScopedValueUtil
> JDK 25 Scoped Values 工具类（JEP 506）。

| 方法 | 描述 |
|------|------|
| `ScopedValue<T> newScopedValue()` | 创建 ScopedValue |
| `void runWhere(ScopedValue<T>, T, Runnable)` | 在值上下文中运行 |
| `R callWhere(ScopedValue<T>, T, Callable<R>)` | 在值上下文中调用 |
| `void runWhere(ScopedValue<T1>, T1, ScopedValue<T2>, T2, Runnable)` | 多值上下文运行 |
| `boolean isBound(ScopedValue<T>)` | 是否已绑定 |
| `T getOrDefault(ScopedValue<T>, T)` | 获取或默认值 |
| `T get(ScopedValue<T>)` | 获取值 |

#### StructuredTaskUtil
> JDK 25 结构化并发工具类（JEP 505）。

| 方法 | 描述 |
|------|------|
| `List<T> invokeAll(List<Callable<T>>)` | 并行执行所有任务 |
| `List<T> invokeAll(List<Callable<T>>, Duration)` | 并行执行（超时） |
| `T invokeAny(List<Callable<T>>)` | 任一任务完成即返回 |
| `T invokeAny(List<Callable<T>>, Duration)` | 任一完成即返回（超时） |
| `R parallel(Callable<T>, Callable<U>, BiFunction<T,U,R>)` | 两任务并行合并 |
| `R parallel(Callable<T>, Callable<U>, BiFunction, Duration)` | 两任务并行（超时） |
| `R parallel(Callable<T1>, Callable<T2>, Callable<T3>, TriFunction)` | 三任务并行合并 |

#### ThreadLocalUtil
> ThreadLocal 工具类（兼容旧代码）。

| 方法 | 描述 |
|------|------|
| `T get(String)` | 获取值 |
| `T get(String, T)` | 获取值（带默认值） |
| `void set(String, T)` | 设置值 |
| `void remove(String)` | 移除值 |
| `void clear()` | 清除所有 |
| `void runWithContext(String, T, Runnable)` | 在上下文中运行 |
| `R callWithContext(String, T, Supplier<R>)` | 在上下文中调用 |

#### NamedThreadFactory
> 命名线程工厂。

| 方法 | 描述 |
|------|------|
| `NamedThreadFactory(String)` | 创建（指定前缀） |
| `NamedThreadFactory(String, boolean)` | 创建（指定前缀和守护） |
| `NamedThreadFactory daemon(String)` | 创建守护线程工厂 |
| `NamedThreadFactory nonDaemon(String)` | 创建非守护线程工厂 |
| `Builder builder()` | 创建构建器 |

**示例：**

```java
ExecutorService pool = OpenThread.createFixedThreadPool(4, "worker");
ExecutorService virtual = OpenThread.createVirtualThreadExecutor();
OpenThread.shutdownGracefully(pool, Duration.ofSeconds(30));

ScopedValueUtil.runWhere(USER_ID, "user123", () -> {
    String id = ScopedValueUtil.get(USER_ID);
});

List<String> results = StructuredTaskUtil.invokeAll(List.of(
    () -> fetchFromServiceA(),
    () -> fetchFromServiceB()
));
```

---

### 3.31 随机工具（random 包）

#### OpenRandom
> 随机工具类，全面的随机生成功能。

| 方法 | 描述 |
|------|------|
| `int randomInt(int)` | 随机 int [0, bound) |
| `int randomInt(int, int)` | 随机 int [origin, bound) |
| `long randomLong(long)` | 随机 long |
| `double randomDouble()` | 随机 double [0, 1) |
| `boolean randomBoolean()` | 随机 boolean |
| `byte[] randomBytes(int)` | 随机字节数组 |
| `byte[] secureBytes(int)` | 安全随机字节 |
| `int secureInt(int)` | 安全随机 int |
| `long secureLong()` | 安全随机 long |
| `String randomAlphanumeric(int)` | 随机字母数字字符串 |
| `String secureAlphanumeric(int)` | 安全随机字母数字 |
| `String randomNumeric(int)` | 随机数字字符串 |
| `String randomAlphabetic(int)` | 随机字母字符串 |
| `String randomUpperCase(int)` | 随机大写字母 |
| `String randomLowerCase(int)` | 随机小写字母 |
| `String randomString(int, String)` | 从指定字符集随机 |
| `String secureString(int, String)` | 安全随机字符串 |
| `String uuid()` | 生成 UUID |
| `String simpleUUID()` | 生成无连字符 UUID |
| `String secureUUID()` | 安全 UUID |
| `T randomElement(List<T>)` | 随机选取元素 |
| `T randomElement(T...)` | 随机选取元素 |
| `List<T> randomElements(List<T>, int)` | 随机选取多个 |
| `void shuffle(List<T>)` | 随机打乱 |
| `void shuffle(T[])` | 随机打乱数组 |
| `LocalDate randomDate(int, int)` | 随机日期 |

#### VerifyCodeUtil
> 验证码生成工具。

| 方法 | 描述 |
|------|------|
| `String numeric()` | 6 位数字验证码 |
| `String numeric(int)` | 指定长度数字验证码 |
| `String alphabetic(int)` | 字母验证码 |
| `String alphanumeric(int)` | 字母数字验证码 |
| `String noConfusing(int)` | 无混淆字符验证码 |
| `String numericRange(int, int)` | 数字范围验证码 |
| `String generate(int, String)` | 自定义字符集验证码 |
| `Builder builder()` | 创建构建器 |

**示例：**

```java
String code = OpenRandom.randomAlphanumeric(8);
String uuid = OpenRandom.simpleUUID();
String element = OpenRandom.randomElement(list);

String verifyCode = VerifyCodeUtil.numeric(4);
String safe = VerifyCodeUtil.noConfusing(6);
```

---

### 3.32 断言工具（assertion 包）

#### OpenAssert
> 基础断言工具类，提供全面的验证方法。

| 方法 | 描述 |
|------|------|
| `T notNull(T, String)` | 非 null 断言 |
| `T notNull(T, String, Object...)` | 非 null 断言（格式化消息） |
| `void isTrue(boolean, String)` | true 断言 |
| `void isFalse(boolean, String)` | false 断言 |
| `void state(boolean, String)` | 状态断言 |
| `T notEmpty(CharSequence, String)` | 非空字符串断言 |
| `T notBlank(CharSequence, String)` | 非空白断言 |
| `void matchesPattern(CharSequence, String, String)` | 正则匹配断言 |
| `T notEmpty(Collection, String)` | 非空集合断言 |
| `T notEmpty(Map, String)` | 非空 Map 断言 |
| `T[] notEmpty(T[], String)` | 非空数组断言 |
| `T[] noNullElements(T[], String)` | 无 null 元素断言 |
| `T inclusiveBetween(T, T, T)` | 闭区间范围断言 |
| `T exclusiveBetween(T, T, T)` | 开区间范围断言 |
| `void validIndex(int, int)` | 索引有效性断言 |
| `void isInstanceOf(Class, Object, String)` | 类型断言 |
| `void isAssignableFrom(Class, Class, String)` | 可赋值断言 |

**示例：**

```java
OpenAssert.notNull(user, "User must not be null");
OpenAssert.notBlank(name, "Name must not be blank");
OpenAssert.isTrue(age > 0, "Age must be positive");
OpenAssert.inclusiveBetween(1, 100, value);
```

---

### 3.33 异常体系（exception 包）

#### OpenException
> 统一异常基类，所有 OpenCode 组件异常的父类。

| 方法 | 描述 |
|------|------|
| `OpenException(String)` | 消息构造 |
| `OpenException(String, Throwable)` | 消息+原因构造 |
| `OpenException(String, String, String)` | 组件+错误码+消息构造 |
| `OpenException(String, String, String, Throwable)` | 完整构造 |
| `String getErrorCode()` | 获取错误码 |
| `String getComponent()` | 获取组件名 |
| `String getRawMessage()` | 获取原始消息 |

**子类：**

| 异常类 | 描述 |
|------|------|
| `OpenIllegalArgumentException` | 参数校验异常 |
| `OpenIllegalStateException` | 状态异常 |
| `OpenUnsupportedOperationException` | 不支持的操作 |
| `OpenTimeoutException` | 超时异常 |
| `OpenIOException` | IO 异常包装 |

#### ExceptionUtil
> 异常工具类。

| 方法 | 描述 |
|------|------|
| `Throwable getRootCause(Throwable)` | 获取根因 |
| `String getStackTrace(Throwable)` | 获取堆栈字符串 |
| `List<Throwable> getCausalChain(Throwable)` | 获取异常链 |
| `Throwable unwrap(Throwable)` | 解包装异常 |
| `void wrapAndThrow(CheckedRunnable)` | 包装并抛出 |
| `T wrapAndReturn(CheckedSupplier<T>)` | 包装并返回 |
| `RuntimeException sneakyThrow(Throwable)` | 静默抛出 |
| `boolean contains(Throwable, Class)` | 异常链是否包含类型 |
| `String getMessage(Throwable)` | 获取消息 |
| `String getRootCauseMessage(Throwable)` | 获取根因消息 |

---

### 3.34 其他工具

#### Singleton
> 全局单例容器。

| 方法 | 描述 |
|------|------|
| `T get(Class<T>)` | 按类型获取 |
| `T get(Class<T>, Supplier<T>)` | 获取或创建 |
| `void register(Class<T>, T)` | 注册 |
| `T registerIfAbsent(Class<T>, T)` | 不存在时注册 |
| `void remove(Class)` | 移除 |
| `T get(String)` | 按名称获取 |
| `void register(String, Object)` | 按名称注册 |
| `void clear()` | 清除所有 |

#### SpiLoader
> SPI 加载器。

| 方法 | 描述 |
|------|------|
| `List<T> load(Class<T>)` | 加载所有实现 |
| `List<T> load(Class<T>, ClassLoader)` | 指定 ClassLoader 加载 |
| `Optional<T> loadFirst(Class<T>)` | 加载第一个 |
| `T loadFirstOrDefault(Class<T>, T)` | 加载或默认值 |
| `List<T> reload(Class<T>)` | 重新加载 |
| `boolean hasService(Class<T>)` | 是否有服务 |
| `int count(Class<T>)` | 服务数量 |
| `Stream<T> loadStream(Class<T>)` | 加载为 Stream |
| `void clearCache()` | 清除缓存 |

#### 构建器工具（builder 包）

| 类 | 描述 |
|------|------|
| `Builder<T>` | 构建器函数式接口 |
| `OpenBuilder` | 构建器统一入口 |
| `BeanBuilder<T>` | JavaBean 流式构建器 |
| `RecordBuilder<T>` | Record 流式构建器 |
| `MapBuilder<K,V>` | Map 流式构建器 |

**示例：**

```java
User user = BeanBuilder.of(User.class)
    .set("name", "Leon")
    .set("age", 25)
    .build();

User record = RecordBuilder.of(User.class)
    .set("name", "Leon")
    .set("age", 25)
    .build();

Map<String, Object> map = MapBuilder.<String, Object>hashMap()
    .put("name", "Leon")
    .put("age", 25)
    .build();
```

#### OptionalUtil
> Optional 增强工具。

| 方法 | 描述 |
|------|------|
| `Optional<T> firstPresent(Optional<T>...)` | 第一个非空 Optional |
| `Optional<T> firstPresentLazy(Supplier<Optional<T>>...)` | 惰性第一个非空 |
| `Optional<R> combine(Optional<T>, Optional<U>, BiFunction)` | 组合两个 Optional |
| `T orNull(Optional<T>)` | 获取值或 null |
| `Optional<R> flatMapNullable(Optional<T>, Function)` | 平面映射（null 安全） |
| `boolean allPresent(Optional...)` | 是否全部有值 |
| `boolean anyPresent(Optional...)` | 是否任意有值 |

#### ParallelStreamUtil
> 并行流工具，智能选择并行/串行。

| 方法 | 描述 |
|------|------|
| `Stream<T> stream(Collection<T>)` | 自动选择并行/串行 |
| `Stream<T> stream(Collection<T>, int)` | 指定阈值 |
| `boolean isParallelRecommended(int)` | 是否推荐并行 |
| `int getAvailableProcessors()` | 可用处理器数 |
| `Stream<T> parallelStream(Collection<T>)` | 强制并行 |
| `Stream<T> sequentialStream(Collection<T>)` | 强制串行 |

#### UnsafeUtil
> 底层操作工具（VarHandle + FFM + sun.misc.Unsafe）。

| 方法 | 描述 |
|------|------|
| `boolean isUsingFFM()` | 是否使用 FFM API |
| `VarHandle findVarHandle(Class, String, Class)` | 查找 VarHandle |
| `boolean compareAndSetInt(VarHandle, Object, int, int)` | CAS 操作 |
| `long allocateMemory(long)` | 分配堆外内存 |
| `void freeMemory(long)` | 释放内存 |
| `void putInt(long, int)` | 写入堆外 int |
| `int getInt(long)` | 读取堆外 int |
| `T allocateInstance(Class<T>)` | 跳过构造器创建实例 |
| `void throwException(Throwable)` | 抛出受检异常 |
| `int pageSize()` | 获取页面大小 |
