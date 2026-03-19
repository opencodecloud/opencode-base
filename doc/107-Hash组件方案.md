# Hash 组件方案

## 1. 组件概述

`opencode-base-hash` 是一个全面、高性能、零依赖的哈希计算工具库。提供丰富的哈希算法（MurmurHash3、xxHash、FNV-1a、CRC32、SHA 系列）、一致性哈希环、布隆过滤器（标准 + 计数）、SimHash 文本指纹等数据结构，采用 Guava 风格的 `HashFunction -> Hasher -> HashCode` 流水线设计。

**适用场景：**
- 数据校验：文件完整性、消息摘要
- 分布式系统：一致性哈希负载均衡
- 海量数据去重：布隆过滤器判断存在性
- 文本相似度：SimHash 文本指纹比较
- 缓存键生成：高性能哈希键

**模块依赖：**
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-hash</artifactId>
    <version>${version}</version>
</dependency>
```

## 2. 包结构

```
cloud.opencode.base.hash
├── OpenHash.java                   // 统一入口工厂类
├── HashCode.java                   // 哈希结果封装
├── HashFunction.java               // 哈希函数接口
├── Hasher.java                     // 流式哈希计算器
├── Funnel.java                     // 对象序列化通道
│
├── function/                       // 哈希函数实现
│   ├── AbstractHashFunction.java   // 抽象基类
│   ├── Murmur3HashFunction.java    // MurmurHash3 (32/128位)
│   ├── XxHashFunction.java         // xxHash (64位)
│   ├── Fnv1aHashFunction.java      // FNV-1a (32/64位)
│   ├── Crc32HashFunction.java      // CRC32/CRC32C
│   └── MessageDigestHashFunction.java // 加密哈希适配 (MD5/SHA系列)
│
├── consistent/                     // 一致性哈希
│   ├── ConsistentHash.java         // 一致性哈希环
│   ├── ConsistentHashBuilder.java  // 构建器
│   ├── HashNode.java               // 节点定义 (record)
│   ├── VirtualNode.java            // 虚拟节点 (record)
│   └── NodeLocator.java            // 节点定位器接口
│
├── bloom/                          // 布隆过滤器
│   ├── BloomFilter.java            // 标准布隆过滤器
│   ├── BloomFilterBuilder.java     // 构建器
│   ├── CountingBloomFilter.java    // 计数布隆过滤器（支持删除）
│   └── BitArray.java               // 位数组（可选线程安全）
│
├── simhash/                        // SimHash 文本指纹
│   ├── SimHash.java                // SimHash 核心算法
│   ├── SimHashBuilder.java         // 构建器
│   ├── Fingerprint.java            // 指纹对象
│   └── Tokenizer.java              // 分词器接口
│
└── exception/
    └── OpenHashException.java      // 哈希异常
```

## 3. 核心 API

### 3.1 OpenHash

> 哈希工具门面入口类，提供所有哈希算法和高级数据结构的工厂方法。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static HashFunction murmur3_32()` | MurmurHash3 32位 |
| `static HashFunction murmur3_32(int seed)` | MurmurHash3 32位（带种子） |
| `static HashFunction murmur3_128()` | MurmurHash3 128位 |
| `static HashFunction murmur3_128(int seed)` | MurmurHash3 128位（带种子） |
| `static HashFunction xxHash64()` | xxHash 64位 |
| `static HashFunction xxHash64(long seed)` | xxHash 64位（带种子） |
| `static HashFunction fnv1a_32()` | FNV-1a 32位 |
| `static HashFunction fnv1a_64()` | FNV-1a 64位 |
| `static HashFunction crc32()` | CRC32 |
| `static HashFunction crc32c()` | CRC32C (Castagnoli) |
| `static HashFunction md5()` | MD5（仅校验，勿用于安全） |
| `static HashFunction sha1()` | SHA-1（仅校验，勿用于安全） |
| `static HashFunction sha256()` | SHA-256 |
| `static HashFunction sha512()` | SHA-512 |
| `static HashFunction sha3_256()` | SHA-3 256位 |
| `static HashFunction sha3_512()` | SHA-3 512位 |
| `static HashCode hash(CharSequence input, Charset charset, HashFunction function)` | 计算字符串哈希值 |
| `static HashCode hash(byte[] input, HashFunction function)` | 计算字节数组哈希值 |
| `static HashCode combineOrdered(HashCode... hashCodes)` | 有序组合多个哈希值 |
| `static HashCode combineUnordered(HashCode... hashCodes)` | 无序组合多个哈希值 |
| `static <T> ConsistentHashBuilder<T> consistentHash()` | 创建一致性哈希环构建器 |
| `static <T> BloomFilterBuilder<T> bloomFilter()` | 创建布隆过滤器构建器 |
| `static <T> BloomFilterBuilder<T> bloomFilter(Funnel<? super T> funnel)` | 创建布隆过滤器构建器（带 Funnel） |
| `static <T> CountingBloomFilter.Builder<T> countingBloomFilter()` | 创建计数布隆过滤器构建器 |
| `static <T> CountingBloomFilter.Builder<T> countingBloomFilter(Funnel<? super T> funnel)` | 创建计数布隆过滤器构建器（带 Funnel） |
| `static SimHashBuilder simHash()` | 创建 SimHash 构建器 |

**示例：**

```java
// 基本哈希计算
HashCode hash = OpenHash.murmur3_128().hashUtf8("Hello World");
System.out.println(hash.toHex());

// SHA-256
HashCode sha = OpenHash.sha256().hashBytes(data);

// 一致性哈希
ConsistentHash<String> ring = OpenHash.<String>consistentHash()
    .virtualNodeCount(150)
    .addNode("server1", "192.168.1.1")
    .build();

// 布隆过滤器
BloomFilter<String> filter = OpenHash.<String>bloomFilter(Funnel.STRING_FUNNEL)
    .expectedInsertions(1_000_000)
    .fpp(0.01)
    .build();

// SimHash
SimHash simHash = OpenHash.simHash().nGram(3).build();
```

### 3.2 HashFunction

> 哈希函数接口，定义哈希计算的核心契约。HashFunction 实例是无状态、线程安全的。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `Hasher newHasher()` | 创建新的流式 Hasher 实例 |
| `Hasher newHasher(int expectedInputSize)` | 创建指定容量的 Hasher |
| `HashCode hashBytes(byte[] input)` | 计算字节数组哈希 |
| `HashCode hashBytes(byte[] input, int offset, int length)` | 计算字节数组部分哈希 |
| `default HashCode hashString(CharSequence input, Charset charset)` | 计算字符串哈希 |
| `default HashCode hashUtf8(CharSequence input)` | 计算 UTF-8 字符串哈希 |
| `HashCode hashInt(int input)` | 计算 int 哈希 |
| `HashCode hashLong(long input)` | 计算 long 哈希 |
| `<T> HashCode hashObject(T instance, Funnel<? super T> funnel)` | 计算对象哈希 |
| `int bits()` | 获取哈希位数 |
| `String name()` | 获取算法名称 |

**示例：**

```java
HashFunction murmur = OpenHash.murmur3_128();
HashCode hash = murmur.hashUtf8("Hello");
System.out.println(hash.bits());  // 128
System.out.println(murmur.name()); // "murmur3_128"
```

### 3.3 Hasher

> 流式哈希计算器接口，支持分块输入数据后一次性计算哈希。有状态，非线程安全，`hash()` 方法仅可调用一次。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `Hasher putByte(byte b)` | 添加字节 |
| `Hasher putBytes(byte[] bytes)` | 添加字节数组 |
| `Hasher putBytes(byte[] bytes, int offset, int length)` | 添加字节数组部分 |
| `Hasher putBytes(ByteBuffer buffer)` | 添加 ByteBuffer |
| `Hasher putShort(short s)` | 添加 short |
| `Hasher putInt(int i)` | 添加 int |
| `Hasher putLong(long l)` | 添加 long |
| `Hasher putFloat(float f)` | 添加 float |
| `Hasher putDouble(double d)` | 添加 double |
| `Hasher putBoolean(boolean b)` | 添加 boolean |
| `Hasher putChar(char c)` | 添加 char |
| `Hasher putString(CharSequence charSequence, Charset charset)` | 添加字符串 |
| `<T> Hasher putObject(T instance, Funnel<? super T> funnel)` | 添加对象 |
| `HashCode hash()` | 计算最终哈希值（仅调用一次） |

**示例：**

```java
Hasher hasher = OpenHash.murmur3_128().newHasher();
HashCode hash = hasher
    .putString("Hello", StandardCharsets.UTF_8)
    .putInt(42)
    .putLong(System.currentTimeMillis())
    .hash();
```

### 3.4 HashCode

> 哈希计算结果封装，提供多种格式的结果访问方式。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `abstract int bits()` | 获取位数 |
| `abstract int asInt()` | 转换为 int（取低32位） |
| `abstract long asLong()` | 转换为 long（取低64位） |
| `abstract long padToLong()` | 转换为 long（32位哈希填充） |
| `abstract byte[] asBytes()` | 转换为字节数组 |
| `abstract int writeBytesTo(byte[] dest, int offset)` | 写入字节数组 |
| `String toHex()` | 转换为十六进制字符串 |
| `static HashCode fromInt(int hash)` | 从 int 创建 |
| `static HashCode fromLong(long hash)` | 从 long 创建 |
| `static HashCode fromBytes(byte[] bytes)` | 从字节数组创建 |
| `static HashCode fromHex(String hex)` | 从十六进制字符串创建 |

**示例：**

```java
HashCode hash = OpenHash.murmur3_128().hashUtf8("Hello World");
String hex = hash.toHex();         // 十六进制
byte[] bytes = hash.asBytes();     // 字节数组
long longVal = hash.asLong();      // long 值

// 从已有值创建
HashCode restored = HashCode.fromHex(hex);
```

### 3.5 Funnel\<T\>

> 对象序列化通道接口，定义如何将对象转换为字节流送入 Hasher。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `void funnel(T from, Hasher into)` | 将对象数据送入 Hasher |

**内置 Funnel 常量：**

| 常量 | 描述 |
|------|------|
| `Funnel.STRING_FUNNEL` | 字符串 Funnel（UTF-8） |
| `Funnel.BYTE_ARRAY_FUNNEL` | 字节数组 Funnel |
| `Funnel.INTEGER_FUNNEL` | Integer Funnel |
| `Funnel.LONG_FUNNEL` | Long Funnel |

**示例：**

```java
// 自定义 Funnel
Funnel<User> userFunnel = (user, into) -> {
    into.putString(user.getName(), StandardCharsets.UTF_8)
        .putInt(user.getAge())
        .putString(user.getEmail(), StandardCharsets.UTF_8);
};

HashCode hash = OpenHash.murmur3_128().hashObject(user, userFunnel);
```

### 3.6 AbstractHashFunction

> 哈希函数抽象基类，为各具体哈希函数实现提供通用逻辑。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `int bits()` | 获取哈希位数 |
| `String name()` | 获取算法名称 |
| `Hasher newHasher(int expectedInputSize)` | 创建指定容量的 Hasher |
| `HashCode hashBytes(byte[] input)` | 计算完整字节数组哈希 |
| `HashCode hashInt(int input)` | 计算 int 哈希 |
| `HashCode hashLong(long input)` | 计算 long 哈希 |
| `<T> HashCode hashObject(T instance, Funnel<? super T> funnel)` | 计算对象哈希 |

### 3.7 Murmur3HashFunction

> MurmurHash3 哈希函数实现，支持 32 位和 128 位两种模式。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static Murmur3HashFunction murmur3_32()` | 创建 32 位 MurmurHash3（默认种子） |
| `static Murmur3HashFunction murmur3_32(int seed)` | 创建 32 位 MurmurHash3（指定种子） |
| `static Murmur3HashFunction murmur3_128()` | 创建 128 位 MurmurHash3（默认种子） |
| `static Murmur3HashFunction murmur3_128(int seed)` | 创建 128 位 MurmurHash3（指定种子） |

### 3.8 XxHashFunction

> xxHash64 哈希函数实现，极高性能的非加密哈希。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static XxHashFunction xxHash64()` | 创建 xxHash64（默认种子） |
| `static XxHashFunction xxHash64(long seed)` | 创建 xxHash64（指定种子） |

### 3.9 Fnv1aHashFunction

> FNV-1a 哈希函数实现，支持 32 位和 64 位。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static Fnv1aHashFunction fnv1a_32()` | 创建 FNV-1a 32位 |
| `static Fnv1aHashFunction fnv1a_64()` | 创建 FNV-1a 64位 |

### 3.10 Crc32HashFunction

> CRC32 哈希函数实现，支持标准 CRC32 和 CRC32C (Castagnoli)。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static Crc32HashFunction crc32()` | 创建 CRC32 |
| `static Crc32HashFunction crc32c()` | 创建 CRC32C |

### 3.11 MessageDigestHashFunction

> 基于 JDK MessageDigest 的加密哈希函数适配器，支持 MD5、SHA-1、SHA-256、SHA-512、SHA-3 等。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static MessageDigestHashFunction md5()` | MD5 (128位) |
| `static MessageDigestHashFunction sha1()` | SHA-1 (160位) |
| `static MessageDigestHashFunction sha256()` | SHA-256 (256位) |
| `static MessageDigestHashFunction sha512()` | SHA-512 (512位) |
| `static MessageDigestHashFunction sha3_256()` | SHA-3 256位 |
| `static MessageDigestHashFunction sha3_512()` | SHA-3 512位 |
| `static MessageDigestHashFunction create(String algorithm, int bits)` | 创建自定义算法 |

### 3.12 ConsistentHash\<T\>

> 一致性哈希环实现，支持虚拟节点、权重、动态节点添加/移除和分布统计。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `T get(Object key)` | 根据键获取节点 |
| `List<T> get(Object key, int replicas)` | 获取多个副本节点 |
| `T locate(long hashValue)` | 根据哈希值定位节点 |
| `List<T> locateAll(long hashValue, int count)` | 定位多个节点 |
| `VirtualNode<T> getVirtualNode(long hashValue)` | 获取虚拟节点 |
| `boolean isEmpty()` | 是否为空 |
| `int getVirtualNodeCount()` | 虚拟节点总数 |
| `void addNode(String nodeId, T nodeData)` | 添加节点 |
| `void addNode(String nodeId, T nodeData, int weight)` | 添加带权重节点 |
| `void removeNode(String nodeId)` | 移除节点 |
| `Set<HashNode<T>> getNodes()` | 获取所有物理节点 |
| `int getNodeCount()` | 物理节点数量 |
| `void clear()` | 清空所有节点 |
| `Map<String, Integer> getDistribution(Collection<?> keys)` | 获取键分布统计 |
| `int getMigrationCount(String nodeId, Collection<?> keys)` | 计算节点移除后的迁移量 |
| `static <T> ConsistentHashBuilder<T> builder()` | 创建构建器 |

**示例：**

```java
ConsistentHash<String> ring = OpenHash.<String>consistentHash()
    .hashFunction(OpenHash.murmur3_128())
    .virtualNodeCount(150)
    .addNode("server1", "192.168.1.1", 1)
    .addNode("server2", "192.168.1.2", 2) // 权重2，双倍虚拟节点
    .addNode("server3", "192.168.1.3", 1)
    .concurrent(true) // 启用读写锁
    .build();

String server = ring.get("user_123");
List<String> replicas = ring.get("data_key", 3);

// 动态增删节点
ring.addNode("server4", "192.168.1.4");
ring.removeNode("server1");

// 分布统计
Map<String, Integer> dist = ring.getDistribution(keys);
```

### 3.13 ConsistentHashBuilder\<T\>

> 一致性哈希环构建器。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `ConsistentHashBuilder<T> hashFunction(HashFunction hashFunction)` | 设置哈希函数 |
| `ConsistentHashBuilder<T> virtualNodeCount(int count)` | 设置每物理节点的虚拟节点数 |
| `ConsistentHashBuilder<T> addNode(String nodeId, T nodeData)` | 添加节点 |
| `ConsistentHashBuilder<T> addNode(String nodeId, T nodeData, int weight)` | 添加带权重节点 |
| `ConsistentHashBuilder<T> addNodes(Collection<HashNode<T>> nodes)` | 批量添加节点 |
| `ConsistentHashBuilder<T> concurrent(boolean concurrent)` | 是否启用并发安全（ReadWriteLock） |
| `ConsistentHash<T> build()` | 构建哈希环 |

### 3.14 HashNode\<T\>

> 哈希环物理节点表示（record）。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `String id()` | 节点唯一标识 |
| `T data()` | 节点数据 |
| `int weight()` | 节点权重 |
| `static <T> HashNode<T> of(String id, T data)` | 创建默认权重节点 |
| `static <T> HashNode<T> of(String id, T data, int weight)` | 创建带权重节点 |

### 3.15 VirtualNode\<T\>

> 一致性哈希环中的虚拟节点（record）。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `String physicalNodeId()` | 所属物理节点 ID |
| `T data()` | 节点数据 |
| `String getKey()` | 虚拟节点键 |
| `static <T> VirtualNode<T> of(HashNode<T> physicalNode, int replicaIndex, long hashValue)` | 创建虚拟节点 |

### 3.16 NodeLocator\<T\>

> 节点定位器接口，一致性哈希环的抽象接口。

### 3.17 BloomFilter\<T\>

> 标准布隆过滤器实现，用于海量数据的存在性概率判断。无假阴性，可能有假阳性。实现了 `Predicate<T>` 接口。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `boolean put(T element)` | 添加元素 |
| `int putAll(Iterable<? extends T> elements)` | 批量添加元素 |
| `boolean test(T element)` | 判断元素是否可能存在 (Predicate) |
| `boolean mightContain(T element)` | 判断元素是否可能存在 |
| `double expectedFpp()` | 获取当前预估误判率 |
| `long approximateElementCount()` | 获取已插入的大约元素数量 |
| `long bitSize()` | 获取位数组大小 |
| `int hashCount()` | 获取哈希函数数量 |
| `BloomFilter<T> merge(BloomFilter<T> other)` | 合并另一个布隆过滤器 |
| `byte[] toBytes()` | 序列化为字节数组 |
| `static <T> BloomFilter<T> fromBytes(byte[] bytes, Funnel<? super T> funnel)` | 从字节数组反序列化 |
| `static <T> BloomFilterBuilder<T> builder(Funnel<? super T> funnel)` | 创建构建器 |

**示例：**

```java
BloomFilter<String> filter = OpenHash.<String>bloomFilter(Funnel.STRING_FUNNEL)
    .expectedInsertions(1_000_000)
    .fpp(0.01) // 1% 误判率
    .build();

filter.put("user_001");
filter.put("user_002");

if (filter.mightContain("user_001")) {
    System.out.println("可能存在");
}
if (!filter.mightContain("user_999")) {
    System.out.println("肯定不存在");
}

// 序列化与反序列化
byte[] bytes = filter.toBytes();
BloomFilter<String> restored = BloomFilter.fromBytes(bytes, Funnel.STRING_FUNNEL);
```

### 3.18 BloomFilterBuilder\<T\>

> 布隆过滤器构建器。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `BloomFilterBuilder<T> expectedInsertions(long expectedInsertions)` | 设置预期插入量 |
| `BloomFilterBuilder<T> fpp(double fpp)` | 设置期望误判率 (0.0-1.0) |
| `BloomFilterBuilder<T> hashFunction(HashFunction hashFunction)` | 设置哈希函数 |
| `BloomFilterBuilder<T> threadSafe(boolean threadSafe)` | 是否启用线程安全 |
| `BloomFilter<T> build()` | 构建布隆过滤器 |

### 3.19 CountingBloomFilter\<T\>

> 计数布隆过滤器实现，在标准布隆过滤器基础上支持删除操作。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `boolean put(T element)` | 添加元素（计数+1） |
| `boolean remove(T element)` | 删除元素（计数-1） |
| `boolean mightContain(T element)` | 判断元素是否可能存在 |
| `int count(T element)` | 获取元素计数（近似值） |
| `void clear()` | 清空过滤器 |
| `static <T> Builder<T> builder(Funnel<? super T> funnel)` | 创建构建器 |

**示例：**

```java
CountingBloomFilter<String> filter = OpenHash.<String>countingBloomFilter(Funnel.STRING_FUNNEL)
    .expectedInsertions(100_000)
    .fpp(0.01)
    .counterBits(4) // 4位计数器
    .build();

filter.put("item1");
filter.put("item1"); // 计数 +1
filter.remove("item1"); // 计数 -1
```

### 3.20 CountingBloomFilter.Builder\<T\>

> 计数布隆过滤器构建器。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `Builder<T> expectedInsertions(long expectedInsertions)` | 设置预期插入量 |
| `Builder<T> fpp(double fpp)` | 设置期望误判率 |
| `Builder<T> counterBits(int bits)` | 设置计数器位数 |
| `Builder<T> hashFunction(HashFunction hashFunction)` | 设置哈希函数 |
| `CountingBloomFilter<T> build()` | 构建计数布隆过滤器 |

### 3.21 BitArray

> 布隆过滤器底层位数组实现，支持线程安全模式。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `BitArray(long bitSize)` | 创建指定大小的位数组 |
| `BitArray(long bitSize, boolean threadSafe)` | 创建位数组（可选线程安全） |
| `BitArray(long bitSize, long[] data)` | 从已有数据创建 |
| `boolean set(long index)` | 设置位 |
| `boolean get(long index)` | 获取位 |
| `boolean clear(long index)` | 清除位 |
| `long bitSize()` | 位数组大小 |
| `long bitCount()` | 已设置的位数 |
| `void clearAll()` | 清空所有位 |
| `BitArray or(BitArray other)` | 与另一位数组做或运算 |
| `byte[] toBytes()` | 序列化为字节数组 |
| `static BitArray fromBytes(byte[] bytes)` | 从字节数组反序列化 |

### 3.22 SimHash

> SimHash 算法实现，用于文本指纹生成和相似度计算。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `long hash(String text)` | 计算文本的 64 位指纹 |
| `Fingerprint fingerprint(String text)` | 计算指纹对象 |
| `int bits()` | 获取指纹位数 |
| `static int hammingDistance(long hash1, long hash2)` | 计算两个指纹的海明距离 |
| `static double similarity(long hash1, long hash2)` | 计算相似度 (0.0-1.0) |
| `static double similarity(long hash1, long hash2, int bits)` | 计算指定位数的相似度 |
| `static boolean isSimilar(long hash1, long hash2, int threshold)` | 判断是否相似 |
| `static SimHashBuilder builder()` | 创建构建器 |
| `static SimHash create()` | 创建默认配置的 SimHash |

**示例：**

```java
SimHash simHash = OpenHash.simHash()
    .nGram(3)
    .hashFunction(OpenHash.murmur3_128())
    .build();

long hash1 = simHash.hash("今天天气很好，适合出门散步");
long hash2 = simHash.hash("今天天气不错，适合外出运动");
long hash3 = simHash.hash("明天会下雨，记得带伞");

int distance12 = SimHash.hammingDistance(hash1, hash2); // 小
int distance13 = SimHash.hammingDistance(hash1, hash3); // 大
double similarity = SimHash.similarity(hash1, hash2);

if (SimHash.isSimilar(hash1, hash2, 3)) {
    System.out.println("文本相似");
}
```

### 3.23 SimHashBuilder

> SimHash 配置构建器。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `SimHashBuilder tokenizer(Function<String, List<String>> tokenizer)` | 设置自定义分词函数 |
| `SimHashBuilder tokenizer(Tokenizer tokenizer)` | 设置分词器 |
| `SimHashBuilder nGram(int n)` | 使用 N-gram 分词 |
| `SimHashBuilder whitespaceTokenizer()` | 使用空格分词 |
| `SimHashBuilder wordTokenizer()` | 使用单词分词 |
| `SimHashBuilder characterTokenizer()` | 使用字符分词 |
| `SimHashBuilder hashFunction(HashFunction hashFunction)` | 设置词哈希函数 |
| `SimHashBuilder bits(int bits)` | 设置指纹位数 (32 或 64) |
| `SimHashBuilder weightFunction(Function<String, Integer> weightFunction)` | 设置词权重函数 |
| `SimHashBuilder lengthWeighted()` | 使用长度加权 |
| `SimHashBuilder uniformWeight()` | 使用均匀权重 |
| `SimHash build()` | 构建 SimHash 实例 |

### 3.24 Fingerprint

> SimHash 文本指纹对象，封装指纹值和位数，提供相似度比较功能。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `Fingerprint(long value, int bits)` | 构造指纹 |
| `long value()` | 指纹值 |
| `int bits()` | 位数 |
| `int hammingDistance(Fingerprint other)` | 与另一指纹的海明距离 |
| `double similarity(Fingerprint other)` | 与另一指纹的相似度 |
| `boolean isSimilar(Fingerprint other, int threshold)` | 是否相似 |
| `String toHex()` | 转十六进制字符串 |
| `String toBinary()` | 转二进制字符串 |
| `static Fingerprint of64(long value)` | 创建 64 位指纹 |
| `static Fingerprint of32(int value)` | 创建 32 位指纹 |
| `static Fingerprint of(long value, int bits)` | 创建指定位数指纹 |
| `static Fingerprint fromHex(String hex, int bits)` | 从十六进制创建 |

**示例：**

```java
Fingerprint fp1 = Fingerprint.of64(simHash.hash(text1));
Fingerprint fp2 = Fingerprint.of64(simHash.hash(text2));

int distance = fp1.hammingDistance(fp2);
double similarity = fp1.similarity(fp2);
boolean similar = fp1.isSimilar(fp2, 3);

String hex = fp1.toHex();
Fingerprint restored = Fingerprint.fromHex(hex, 64);
```

### 3.25 Tokenizer

> SimHash 文本处理的分词器接口，继承 `Function<String, List<String>>`。

**内置实现（通过静态方法获取）：**

| 方法 | 描述 |
|------|------|
| `Tokenizer.whitespace()` | 空格分词 |
| `Tokenizer.ngram(int n)` | N-gram 分词 |

**示例：**

```java
Tokenizer tokenizer = Tokenizer.whitespace();
List<String> tokens = tokenizer.tokenize("Hello World");

Tokenizer ngram = Tokenizer.ngram(3);
List<String> grams = ngram.tokenize("Hello"); // ["Hel", "ell", "llo"]
```

### 3.26 OpenHashException

> 哈希操作异常类，继承自 `OpenException`。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `OpenHashException(String message)` | 基本构造 |
| `OpenHashException(String message, Throwable cause)` | 带原因构造 |
| `OpenHashException(String algorithm, String operation, String message)` | 带算法和操作构造 |
| `OpenHashException(String algorithm, String operation, String message, Throwable cause)` | 完整构造 |
| `String algorithm()` | 获取算法名称 |
| `String operation()` | 获取操作类型 |
| `static OpenHashException algorithmNotSupported(String algorithm)` | 算法不支持异常 |
| `static OpenHashException invalidInput(String message)` | 无效输入异常 |
| `static OpenHashException illegalState(String message)` | 状态异常 |
| `static OpenHashException hashFailed(String algorithm, Throwable cause)` | 哈希计算失败异常 |
| `static OpenHashException invalidBloomFilterConfig(String reason)` | 布隆过滤器配置无效异常 |
| `static OpenHashException nodeNotFound(String nodeId)` | 节点不存在异常 |
| `static OpenHashException hasherAlreadyUsed()` | Hasher 已使用异常 |
