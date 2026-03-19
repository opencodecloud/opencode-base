# OpenCode Base Hash

**哈希工具库，适用于 Java 25+**

`opencode-base-hash` 提供统一的哈希函数和基于哈希的数据结构 API，包括非加密哈希（MurmurHash3、xxHash、FNV-1a、CRC32）、加密哈希（MD5、SHA 系列、SHA-3）、布隆过滤器、一致性哈希环和用于文本相似度的 SimHash。

## 功能特性

### 哈希函数
- **MurmurHash3**：32 位和 128 位变体，支持种子
- **xxHash**：高性能 64 位哈希，支持种子
- **FNV-1a**：32 位和 64 位 Fowler-Noll-Vo 哈希
- **CRC32**：CRC32 和 CRC32C (Castagnoli) 校验
- **MD5**：仅用于校验（非加密安全）
- **SHA-1**：仅用于校验（非加密安全）
- **SHA-256 / SHA-512**：加密哈希函数
- **SHA3-256 / SHA3-512**：最新 SHA-3 标准

### 数据结构
- **BloomFilter**：概率性集合成员检测，可配置误判率
- **CountingBloomFilter**：支持删除元素的布隆过滤器
- **ConsistentHash**：用于分布式负载均衡的一致性哈希环
- **SimHash**：用于文本相似度检测的局部敏感哈希

### 工具
- **HashCode**：统一的哈希结果，支持 int、long、字节数组和十六进制字符串访问
- **Funnel**：类型安全的哈希输入序列化接口
- **Hasher**：流式哈希构建器，支持增量哈希
- **有序/无序组合**：组合多个哈希码

## 快速开始

### Maven 依赖
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-hash</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 基本用法

```java
import cloud.opencode.base.hash.*;

// 使用 MurmurHash3 哈希字符串
HashCode hash = OpenHash.murmur3_128().hashUtf8("Hello World");
System.out.println(hash.toString());  // 十六进制字符串

// 使用不同算法哈希
HashCode murmur = OpenHash.murmur3_32().hashUtf8("data");
HashCode xx = OpenHash.xxHash64().hashUtf8("data");
HashCode sha = OpenHash.sha256().hashUtf8("data");

// 哈希字节数组
byte[] data = "Hello".getBytes();
HashCode hash = OpenHash.hash(data, OpenHash.murmur3_128());

// 组合哈希码
HashCode combined = OpenHash.combineOrdered(hash1, hash2, hash3);
HashCode xored = OpenHash.combineUnordered(hash1, hash2, hash3);
```

### 布隆过滤器

```java
// 创建布隆过滤器
BloomFilter<String> filter = OpenHash.<String>bloomFilter(Funnel.STRING_FUNNEL)
    .expectedInsertions(1_000_000)
    .falsePositiveRate(0.01)
    .build();

// 添加元素
filter.put("apple");
filter.put("banana");

// 测试成员资格
filter.mightContain("apple");   // true
filter.mightContain("cherry");  // false（大概率）

// 计数布隆过滤器（支持删除）
CountingBloomFilter<String> countingFilter = OpenHash.<String>countingBloomFilter(Funnel.STRING_FUNNEL)
    .expectedInsertions(100_000)
    .build();
countingFilter.put("item");
countingFilter.remove("item");
```

### 一致性哈希环

```java
// 创建一致性哈希环
ConsistentHash<String> ring = OpenHash.<String>consistentHash()
    .addNode("server1", "192.168.1.1")
    .addNode("server2", "192.168.1.2")
    .addNode("server3", "192.168.1.3")
    .virtualNodes(150)
    .build();

// 将键路由到节点
String server = ring.getNode("user:12345").getData();  // "192.168.1.x"

// 动态添加/移除节点
ring.addNode("server4", "192.168.1.4");
ring.removeNode("server2");
```

### SimHash（文本相似度）

```java
// 创建 SimHash
SimHash simHash = OpenHash.simHash()
    .nGram(3)
    .build();

// 计算指纹
Fingerprint fp1 = simHash.hash("The quick brown fox");
Fingerprint fp2 = simHash.hash("The quick brown dog");

// 比较相似度（汉明距离）
int distance = fp1.hammingDistance(fp2);
boolean similar = fp1.isSimilar(fp2, 3);  // 3 位以内
```

### 流式哈希（Hasher）

```java
// 增量哈希
Hasher hasher = OpenHash.murmur3_128().newHasher();
hasher.putString("name", StandardCharsets.UTF_8);
hasher.putInt(42);
hasher.putLong(System.currentTimeMillis());
HashCode hash = hasher.hash();
```

## 类参考

### 根包 (`cloud.opencode.base.hash`)
| 类 | 说明 |
|---|------|
| `OpenHash` | 主门面类，提供所有哈希函数和数据结构的工厂方法 |
| `HashFunction` | 哈希函数实现接口 |
| `HashCode` | 不可变哈希结果，支持 int、long、字节数组和十六进制字符串访问 |
| `Hasher` | 流式哈希构建器，支持增量输入 |
| `Funnel` | 类型安全的序列化接口，将对象转换为哈希输入 |

### 函数包 (`cloud.opencode.base.hash.function`)
| 类 | 说明 |
|---|------|
| `AbstractHashFunction` | 哈希函数实现的基类 |
| `Murmur3HashFunction` | MurmurHash3 32 位和 128 位实现 |
| `XxHashFunction` | xxHash 64 位高性能哈希 |
| `Fnv1aHashFunction` | FNV-1a 32 位和 64 位哈希 |
| `Crc32HashFunction` | CRC32 和 CRC32C (Castagnoli) 实现 |
| `MessageDigestHashFunction` | 基于 JDK MessageDigest 的哈希（MD5、SHA-1、SHA-256、SHA-512、SHA3） |

### 布隆过滤器包 (`cloud.opencode.base.hash.bloom`)
| 类 | 说明 |
|---|------|
| `BloomFilter` | 概率性集合成员数据结构 |
| `BloomFilterBuilder` | 配置布隆过滤器参数的构建器 |
| `CountingBloomFilter` | 支持删除元素的布隆过滤器变体 |
| `BitArray` | 布隆过滤器底层的紧凑位数组 |

### 一致性哈希包 (`cloud.opencode.base.hash.consistent`)
| 类 | 说明 |
|---|------|
| `ConsistentHash` | 用于分布式负载均衡的一致性哈希环 |
| `ConsistentHashBuilder` | 配置一致性哈希环的构建器 |
| `HashNode` | 一致性哈希环中的物理节点 |
| `VirtualNode` | 映射到物理节点的虚拟节点 |
| `NodeLocator` | 按键定位节点的接口 |

### SimHash 包 (`cloud.opencode.base.hash.simhash`)
| 类 | 说明 |
|---|------|
| `SimHash` | 用于文本相似度的局部敏感哈希 |
| `SimHashBuilder` | 配置 SimHash 参数的构建器 |
| `Fingerprint` | SimHash 指纹，支持汉明距离比较 |
| `Tokenizer` | SimHash 输入的文本分词器（n-gram、词语） |

### 异常包 (`cloud.opencode.base.hash.exception`)
| 类 | 说明 |
|---|------|
| `OpenHashException` | 哈希操作的基础异常 |

## 环境要求

- Java 25+（使用 record、密封接口、模式匹配）
- 核心功能无外部依赖

## 许可证

Apache License 2.0

## 作者

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
