# TimeSeries 组件方案

## 1. 组件概述

### 1.1 设计目标

`opencode-base-timeseries` 模块提供轻量级嵌入式时间序列数据处理能力，纯 JDK 实现、零外部依赖。

**核心特性：**
- 时间序列存储与查询（基于 `ConcurrentSkipListMap`，线程安全+时间有序）
- 聚合计算（SUM/AVG/MIN/MAX/COUNT/FIRST/LAST/百分位）
- 窗口计算（滚动窗口、滑动窗口、会话窗口）
- 采样与降采样（LTTB、M4、峰值保持、百分位、阈值等高级策略）
- 异常检测（Z-Score、IQR、移动平均偏差、尖峰检测、范围检测）
- 变点检测（CUSUM、二分分割、均值偏移、方差偏移）
- 时序预测（SMA、WMA、EMA、Holt双指数平滑、线性回归、朴素预测、漂移预测）
- 相关性分析（Pearson、Spearman、互相关、自相关ACF、偏自相关PACF、滚动相关）
- 季节性分解（经典加法/乘法分解、类STL分解、季节周期自动检测）
- 时序压缩（Gorilla XOR值压缩、Delta-delta时间戳压缩）
- 查询构建器（流式API、时间范围过滤、标签过滤、分组聚合）
- 安全防护（数据点验证、查询范围限制、结果大小限制、容量限制）

### 1.2 架构概览

```
┌─────────────────────────────────────────────────────────────────┐
│                        Application                               │
│                  (监控系统 / IoT / 金融分析)                       │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                         Facade Layer                             │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │                    OpenTimeSeries                            ││
│  │     (全局存储、快捷记录、统计查询、预测、降采样、异常检测)      ││
│  └─────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                        Core Layer                                │
│  ┌────────────┐ ┌────────────┐ ┌────────────┐ ┌──────────────┐ │
│  │ TimeSeries │ │ DataPoint  │ │   Query    │ │  TimeRange   │ │
│  │  时间序列  │ │   数据点   │ │  查询构建  │ │  时间范围    │ │
│  └────────────┘ └────────────┘ └────────────┘ └──────────────┘ │
│  ┌────────────────────┐  ┌──────────────────────────┐          │
│  │ BoundedTimeSeries  │  │    TimeSeriesStats       │          │
│  │   有界时间序列     │  │    统计信息(Record)      │          │
│  └────────────────────┘  └──────────────────────────┘          │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Processing Layer                            │
│ ┌──────────┐┌───────────┐┌──────────┐┌───────────┐┌──────────┐│
│ │aggregation││  window/  ││ sampling/││ detection/ ││ forecast/││
│ │  聚合计算 ││ 窗口计算  ││ 采样处理 ││ 异常/变点  ││   预测   ││
│ └──────────┘└───────────┘└──────────┘└───────────┘└──────────┘│
│ ┌──────────────┐┌──────────────────┐┌──────────────┐          │
│ │  analysis/   ││  decomposition/  ││ compression/ │          │
│ │  相关性分析  ││    季节性分解    ││  时序压缩   │          │
│ └──────────────┘└──────────────────┘└──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Infrastructure Layer                        │
│ ┌──────────────────────────────────────────┐┌────────────────┐ │
│ │   ConcurrentSkipListMap<Instant, DataPoint>│ │  validation/ │ │
│ │       (内存时序存储，按时间有序)            ││ exception/   │ │
│ └──────────────────────────────────────────┘└────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

**层次说明：**

| 层次 | 职责 | 核心类 |
|------|------|--------|
| Facade | 全局入口，统一简化API | `OpenTimeSeries` |
| Core | 核心数据模型 | `TimeSeries`, `DataPoint`, `BoundedTimeSeries`, `TimeSeriesStats`, `Query`, `TimeRange` |
| Processing | 数据处理算法 | `Aggregation`, `Aggregator`, `SamplerUtil`, `DownsamplingUtil`, `ForecastUtil`, `AnomalyDetectorUtil`, `ChangePointDetectionUtil`, `CorrelationUtil`, `SeasonalDecompositionUtil`, `CompressionUtil`, `Window` |
| Infrastructure | 底层存储与安全 | `ConcurrentSkipListMap`, `DataPointValidator`, `QueryLimiter`, `TimeSeriesException` |

### 1.3 模块依赖

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-timeseries</artifactId>
    <version>${version}</version>
</dependency>
```

---

## 2. 包结构

```
cloud.opencode.base.timeseries
├── DataPoint.java                          # 数据点 (Record，不可变)
├── TimeSeries.java                         # 时间序列核心类
├── BoundedTimeSeries.java                  # 带容量和时间限制的时间序列
├── OpenTimeSeries.java                     # 门面类（全局入口，静态工具方法）
├── TimeSeriesStats.java                    # 统计信息 (Record)
├── Aggregation.java                        # 聚合工具（降采样/移动平均/EMA/滚动统计）
├── aggregation/                            # 聚合器
│   ├── Aggregator.java                     # 聚合器接口
│   ├── SumAggregator.java                  # 求和聚合（单例）
│   ├── AvgAggregator.java                  # 平均值聚合（单例）
│   ├── MinAggregator.java                  # 最小值聚合（单例）
│   ├── MaxAggregator.java                  # 最大值聚合（单例）
│   ├── CountAggregator.java                # 计数聚合（单例）
│   └── AggregationResult.java              # 聚合结果 (sealed interface)
├── window/                                 # 窗口计算
│   ├── Window.java                         # 窗口接口
│   ├── TumblingWindow.java                 # 滚动窗口（不重叠）
│   ├── SlidingWindow.java                  # 滑动窗口（可重叠）
│   └── SessionWindow.java                  # 会话窗口（按活动间隔分组）
├── sampling/                               # 采样
│   ├── SamplerUtil.java                    # 基础采样工具（降采样/上采样/填充/重采样）
│   ├── DownsamplingUtil.java               # 高级降采样（LTTB/M4/峰值保持/方差保持/百分位/阈值）
│   ├── AggregationType.java                # 聚合类型枚举
│   └── FillStrategy.java                   # 填充策略枚举
├── detection/                              # 检测
│   ├── AnomalyDetectorUtil.java            # 异常检测工具
│   └── ChangePointDetectionUtil.java       # 变点检测工具
├── forecast/                               # 预测
│   └── ForecastUtil.java                   # 预测工具（SMA/WMA/EMA/Holt/线性/朴素/漂移）
├── analysis/                               # 分析
│   └── CorrelationUtil.java                # 相关性分析（Pearson/Spearman/互相关/ACF/PACF）
├── compression/                            # 压缩
│   └── CompressionUtil.java                # 时序压缩（Gorilla XOR/Delta-delta）
├── decomposition/                          # 分解
│   └── SeasonalDecompositionUtil.java      # 季节性分解（经典/STL/周期检测）
├── query/                                  # 查询
│   ├── Query.java                          # 流式查询构建器
│   ├── TimeRange.java                      # 时间范围 (Record)
│   └── QueryLimiter.java                   # 查询限制器
├── validation/                             # 验证
│   └── DataPointValidator.java             # 数据点验证器
└── exception/                              # 异常
    ├── TimeSeriesException.java            # 时间序列异常基类
    └── TimeSeriesErrorCode.java            # 错误码枚举 (TS-1xxx~4xxx)
```

---

## 3. 核心 API

### 3.1 DataPoint -- 数据点

不可变 Record 类型，表示时间序列中的一个数据点。按时间戳排序，支持标签。

```java
public record DataPoint(
    Instant timestamp,    // 时间戳（不可为null）
    double value,         // 值
    Map<String, String> tags  // 标签（不可变Map）
) implements Comparable<DataPoint>
```

**工厂方法：**

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `of(Instant, double)` | 时间戳、值 | `DataPoint` | 创建不带标签的数据点 |
| `of(long, double)` | 纪元毫秒、值 | `DataPoint` | 从毫秒时间戳创建 |
| `of(Instant, double, Map)` | 时间戳、值、标签 | `DataPoint` | 创建带标签的数据点 |
| `now(double)` | 值 | `DataPoint` | 使用当前时间创建 |

**实例方法：**

| 方法 | 返回值 | 说明 |
|------|--------|------|
| `epochMillis()` | `long` | 获取纪元毫秒 |
| `isNaN()` | `boolean` | 检查值是否为NaN |
| `getTag(String)` | `String` | 获取指定标签的值 |
| `withTag(String, String)` | `DataPoint` | 创建带有新增标签的副本 |
| `compareTo(DataPoint)` | `int` | 按时间戳比较 |

```java
// 基本创建
DataPoint p1 = DataPoint.of(Instant.now(), 42.5);
DataPoint p2 = DataPoint.now(100.0);
DataPoint p3 = DataPoint.of(1700000000000L, 55.3);

// 带标签
DataPoint tagged = DataPoint.of(Instant.now(), 99.9, Map.of("host", "server1"));
DataPoint withExtra = tagged.withTag("region", "us-east");
String host = withExtra.getTag("host"); // "server1"
```

### 3.2 TimeSeries -- 时间序列

核心类，使用 `ConcurrentSkipListMap` 存储数据点，天然支持并发读写和按时间有序。支持链式调用。

```java
public class TimeSeries {
    public TimeSeries(String name)
    public TimeSeries(String name, Collection<DataPoint> points)
}
```

**数据操作方法（链式调用）：**

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `add(DataPoint)` | 数据点 | `TimeSeries` | 添加数据点 |
| `add(Instant, double)` | 时间戳、值 | `TimeSeries` | 快捷添加 |
| `addNow(double)` | 值 | `TimeSeries` | 添加当前时间的数据点 |
| `addAll(Collection<DataPoint>)` | 数据点集合 | `TimeSeries` | 批量添加 |
| `remove(Instant)` | 时间戳 | `void` | 移除指定时间戳的数据点 |
| `clear()` | - | `void` | 清除所有数据 |
| `retain(Duration)` | 保留时长 | `void` | 清除超过保留时长的旧数据 |

**查询方法：**

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `getName()` | - | `String` | 获取序列名称 |
| `size()` | - | `int` | 获取数据点数量 |
| `isEmpty()` | - | `boolean` | 是否为空 |
| `count()` | - | `double` | 获取计数（double） |
| `count(Instant, Instant)` | 起止时间 | `double` | 范围内计数 |
| `get(Instant)` | 时间戳 | `Optional<DataPoint>` | 精确查找 |
| `getFirst()` | - | `Optional<DataPoint>` | 获取第一个 |
| `getLast()` | - | `Optional<DataPoint>` | 获取最后一个 |
| `getLatest()` | - | `DataPoint` | 获取最新（可能为null） |
| `getPoints()` | - | `List<DataPoint>` | 获取所有点（不可变副本） |
| `all()` | - | `List<DataPoint>` | `getPoints()` 的别名 |
| `getValues()` | - | `double[]` | 获取所有值数组 |
| `getTimestamps()` | - | `Instant[]` | 获取所有时间戳数组 |

**范围查询方法（返回新的 TimeSeries）：**

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `range(Instant, Instant)` | 起止时间 | `TimeSeries` | 时间范围子集 |
| `last(Duration)` | 时长 | `TimeSeries` | 最近一段时间的数据 |
| `head(int)` | 数量 | `TimeSeries` | 前 n 个点 |
| `tail(int)` | 数量 | `TimeSeries` | 后 n 个点 |

**聚合方法：**

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `sum()` | - | `double` | 值的总和 |
| `sum(Instant, Instant)` | 起止时间 | `double` | 范围内总和 |
| `average()` | - | `OptionalDouble` | 平均值 |
| `average(Instant, Instant)` | 起止时间 | `double` | 范围内平均值 |
| `min()` | - | `OptionalDouble` | 最小值 |
| `max()` | - | `OptionalDouble` | 最大值 |
| `variance()` | - | `double` | 方差（样本方差） |
| `stdDev()` | - | `double` | 标准差 |
| `standardDeviation()` | - | `double` | `stdDev()` 的别名 |
| `percentile(int)` | 百分位(0-100) | `double` | 百分位值 |
| `stats()` | - | `TimeSeriesStats` | 完整统计信息 |

**变换方法（返回新的 TimeSeries）：**

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `map(DoubleUnaryOperator)` | 映射函数 | `TimeSeries` | 对每个值应用函数 |
| `diff()` | - | `TimeSeries` | 差分（相邻值之差） |
| `cumSum()` | - | `TimeSeries` | 累积和 |
| `combine(TimeSeries, DoubleBinaryOperator)` | 另一序列、运算符 | `TimeSeries` | 合并两个序列（按时间对齐） |
| `derivative()` | - | `TimeSeries` | 导数（变化率，单位/秒） |
| `movingAverage(int)` | 窗口大小 | `TimeSeries` | 简单移动平均 |
| `exponentialMovingAverage(double)` | alpha(0-1] | `TimeSeries` | 指数移动平均 |

**元数据方法：**

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `setMetadata(String, String)` | 键、值 | `void` | 设置元数据 |
| `getMetadata(String)` | 键 | `String` | 获取元数据 |
| `getAllMetadata()` | - | `Map<String,String>` | 获取所有元数据（不可变副本） |

```java
// 创建并添加数据
TimeSeries cpu = new TimeSeries("cpu_usage");
cpu.add(Instant.now(), 45.5)
   .add(Instant.now().plusSeconds(1), 52.3)
   .addNow(48.1);

// 聚合计算
double total = cpu.sum();
OptionalDouble avg = cpu.average();
double p99 = cpu.percentile(99);
TimeSeriesStats stats = cpu.stats();

// 变换
TimeSeries diff = cpu.diff();          // 差分
TimeSeries ma5 = cpu.movingAverage(5); // 5点移动平均
TimeSeries ema = cpu.exponentialMovingAverage(0.3);
TimeSeries deriv = cpu.derivative();   // 导数

// 范围查询
TimeSeries lastHour = cpu.last(Duration.ofHours(1));
TimeSeries morning = cpu.range(
    Instant.parse("2025-01-01T08:00:00Z"),
    Instant.parse("2025-01-01T12:00:00Z")
);

// 函数映射
TimeSeries normalized = cpu.map(v -> v / 100.0);

// 两序列合并
TimeSeries ratio = series1.combine(series2, (a, b) -> a / b);
```

### 3.3 BoundedTimeSeries -- 有界时间序列

继承 `TimeSeries`，在添加数据点时自动进行容量检查和过期驱逐。

```java
public class BoundedTimeSeries extends TimeSeries {
    public BoundedTimeSeries(String name, int maxSize, Duration maxAge)
    public BoundedTimeSeries(String name, int maxSize)  // 默认maxAge=365天
}
```

| 方法 | 返回值 | 说明 |
|------|--------|------|
| `of(String, int, Duration)` | `BoundedTimeSeries` | 静态工厂方法 |
| `of(String, int)` | `BoundedTimeSeries` | 仅容量限制 |
| `getMaxSize()` | `int` | 最大容量 |
| `getMaxAge()` | `Duration` | 最大保留时间 |
| `remainingCapacity()` | `int` | 剩余容量 |
| `isFull()` | `boolean` | 是否已满 |

```java
// 最多10万个点，保留7天
BoundedTimeSeries bounded = BoundedTimeSeries.of("metrics", 100_000, Duration.ofDays(7));
bounded.addNow(42.0); // 超出容量时自动驱逐最旧的数据点
```

### 3.4 TimeSeriesStats -- 统计信息

不可变 Record，包含时间序列的完整统计摘要。

```java
public record TimeSeriesStats(
    long count,       // 数据点数量
    double sum,       // 总和
    double average,   // 平均值
    double min,       // 最小值
    double max,       // 最大值
    double stdDev     // 标准差
)
```

| 方法 | 返回值 | 说明 |
|------|--------|------|
| `empty()` | `TimeSeriesStats` | 创建空统计（全部为0） |
| `isEmpty()` | `boolean` | 是否为空 |
| `range()` | `double` | 极差（max - min） |
| `variance()` | `double` | 方差（stdDev^2） |

```java
TimeSeriesStats stats = series.stats();
System.out.println("数据量: " + stats.count());
System.out.println("均值: " + stats.average());
System.out.println("极差: " + stats.range());
System.out.println("方差: " + stats.variance());
```

### 3.5 OpenTimeSeries -- 门面类

全局入口，提供所有核心功能的静态方法。内部使用 `ConcurrentHashMap` 管理多个命名时间序列。

**全局存储管理：**

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `get(String)` | 名称 | `TimeSeries` | 获取或创建命名序列 |
| `record(String, double)` | 名称、值 | `void` | 记录当前时间的数据点 |
| `record(String, Instant, double)` | 名称、时间、值 | `void` | 记录指定时间的数据点 |
| `query(String, Duration)` | 名称、时长 | `List<DataPoint>` | 查询最近一段时间的数据 |
| `stats(String)` | 名称 | `TimeSeriesStats` | 获取统计信息 |
| `remove(String)` | 名称 | `void` | 删除命名序列 |
| `exists(String)` | 名称 | `boolean` | 检查序列是否存在 |
| `cleanup(Duration)` | 保留时长 | `void` | 清理所有序列的过期数据 |
| `getSeriesNames()` | - | `List<String>` | 获取所有序列名称 |
| `clearAll()` | - | `void` | 清除所有序列 |

**创建方法（不存入全局存储）：**

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `create(String)` | 名称 | `TimeSeries` | 创建新序列 |
| `of(String, Collection)` | 名称、初始点 | `TimeSeries` | 用初始数据创建 |
| `bounded(String, int, Duration)` | 名称、容量、时间 | `BoundedTimeSeries` | 创建有界序列 |
| `point(Instant, double)` | 时间、值 | `DataPoint` | 创建数据点 |
| `point(double)` | 值 | `DataPoint` | 创建当前时间数据点 |

**聚合与变换快捷方法：**

| 方法 | 说明 |
|------|------|
| `downsample(series, interval)` | 降采样（默认AVG） |
| `downsample(series, interval, aggregation)` | 降采样（指定聚合） |
| `movingAverage(series, window)` | 移动平均 |
| `ema(series, alpha)` | 指数移动平均 |
| `rollingStats(series, window)` | 滚动统计（返回min/max/avg/std四个序列的Map） |
| `merge(series...)` | 合并多个序列 |
| `fill(series, fillValue)` | 填充NaN值 |
| `normalize(series)` | 归一化到0-1 |
| `standardize(series)` | Z分数标准化 |

**异常检测快捷方法：**

| 方法 | 说明 |
|------|------|
| `detectAnomalies(series, threshold)` | Z-Score异常检测 |

**预测快捷方法：**

| 方法 | 说明 |
|------|------|
| `smaForecast(series, windowSize, steps)` | SMA预测 |
| `emaForecast(series, alpha, steps)` | EMA预测 |
| `linearForecast(series, steps)` | 线性回归预测 |
| `holtForecast(series, alpha, beta, steps)` | Holt双指数平滑预测 |
| `forecastWithBounds(series, windowSize, steps, confidence)` | 带置信区间的SMA预测 |

**高级降采样快捷方法：**

| 方法 | 说明 |
|------|------|
| `lttb(series, targetSize)` | LTTB算法（保持视觉形状） |
| `m4(series, bucketDuration)` | M4算法（保持min/max） |
| `peakPreserving(series, targetSize)` | 峰值保持降采样 |
| `percentile(series, bucketDuration, percentile)` | 百分位降采样 |
| `thresholdDownsample(series, threshold)` | 阈值降采样 |

```java
// 快捷记录
OpenTimeSeries.record("requests", 150);
OpenTimeSeries.record("response_time", Instant.now(), 23.5);

// 查询
List<DataPoint> last5Min = OpenTimeSeries.query("requests", Duration.ofMinutes(5));
TimeSeriesStats reqStats = OpenTimeSeries.stats("requests");

// 创建与变换
TimeSeries series = OpenTimeSeries.create("test");
TimeSeries normalized = OpenTimeSeries.normalize(series);
TimeSeries forecast = OpenTimeSeries.linearForecast(series, 10);
TimeSeries downsampled = OpenTimeSeries.lttb(series, 100);

// 全局清理
OpenTimeSeries.cleanup(Duration.ofDays(7));
```

---

## 4. 聚合计算

### 4.1 Aggregation -- 聚合工具类

提供降采样、移动平均、指数移动平均和滚动统计的静态方法。

```java
public final class Aggregation {
    // 聚合函数枚举
    public enum Function { SUM, AVG, MIN, MAX, COUNT, FIRST, LAST }
}
```

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `downsample(series, interval, function)` | 序列、间隔、聚合函数 | `TimeSeries` | 按时间桶聚合降采样 |
| `movingAverage(series, window)` | 序列、窗口大小 | `TimeSeries` | 简单移动平均 |
| `exponentialMovingAverage(series, alpha)` | 序列、平滑因子 | `TimeSeries` | 指数移动平均 |
| `rollingStats(series, window)` | 序列、窗口大小 | `Map<String, TimeSeries>` | 滚动统计（min/max/avg/std） |

```java
// 按5分钟桶取平均
TimeSeries avg5min = Aggregation.downsample(series, Duration.ofMinutes(5), Aggregation.Function.AVG);

// 滚动统计
Map<String, TimeSeries> rolling = Aggregation.rollingStats(series, 20);
TimeSeries rollingMin = rolling.get("min");
TimeSeries rollingMax = rolling.get("max");
TimeSeries rollingAvg = rolling.get("avg");
TimeSeries rollingStd = rolling.get("std");
```

### 4.2 Aggregator 接口与内置实现

`Aggregator` 是聚合器接口，内置5个单例实现。

```java
public interface Aggregator {
    double aggregate(List<DataPoint> points);
    String name();
}
```

| 实现类 | 获取实例 | 功能 |
|--------|----------|------|
| `SumAggregator` | `SumAggregator.getInstance()` | 求和 |
| `AvgAggregator` | `AvgAggregator.getInstance()` | 平均值 |
| `MinAggregator` | `MinAggregator.getInstance()` | 最小值 |
| `MaxAggregator` | `MaxAggregator.getInstance()` | 最大值 |
| `CountAggregator` | `CountAggregator.getInstance()` | 计数 |

### 4.3 AggregationResult -- 聚合结果

密封接口（sealed interface），三种结果类型。

```java
public sealed interface AggregationResult
    permits AggregationResult.Success,
            AggregationResult.Empty,
            AggregationResult.Error {

    boolean isSuccess();

    record Success(double value, long count, Instant from, Instant to) implements AggregationResult { ... }
    record Empty(String seriesName) implements AggregationResult { ... }
    record Error(TimeSeriesErrorCode code, String message) implements AggregationResult { ... }
}
```

---

## 5. 采样与降采样

### 5.1 SamplerUtil -- 基础采样工具

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `downsample(series, interval, aggregation)` | 序列、间隔、聚合类型 | `TimeSeries` | 按时间桶降采样 |
| `downsample(series, interval)` | 序列、间隔 | `TimeSeries` | 默认AVG降采样 |
| `fillGaps(series, interval, strategy)` | 序列、间隔、填充策略 | `TimeSeries` | 填充缺失点 |
| `upsample(series, interval, strategy)` | 序列、间隔、策略 | `TimeSeries` | 上采样 |
| `resample(series, interval, agg, fill)` | 序列、间隔、聚合、填充 | `TimeSeries` | 先降后填重采样 |
| `randomSample(series, sampleSize)` | 序列、样本量 | `TimeSeries` | 随机采样 |
| `systematicSample(series, n)` | 序列、间隔 | `TimeSeries` | 系统采样（每n个） |

**AggregationType 聚合类型枚举：**
`SUM`, `AVG`, `MIN`, `MAX`, `FIRST`, `LAST`, `COUNT`

**FillStrategy 填充策略枚举：**
`ZERO`, `PREVIOUS`, `NEXT`, `LINEAR`, `AVERAGE`, `NAN`

```java
// 降采样：每小时取平均
TimeSeries hourly = SamplerUtil.downsample(series, Duration.ofHours(1), AggregationType.AVG);

// 填充缺失：线性插值
TimeSeries filled = SamplerUtil.fillGaps(series, Duration.ofMinutes(1), FillStrategy.LINEAR);

// 重采样为规则5分钟间隔
TimeSeries resampled = SamplerUtil.resample(
    series, Duration.ofMinutes(5), AggregationType.AVG, FillStrategy.PREVIOUS);

// 随机采样100个点
TimeSeries sampled = SamplerUtil.randomSample(series, 100);
```

### 5.2 DownsamplingUtil -- 高级降采样工具

提供面向可视化和数据分析的高级降采样算法。

| 方法 | 参数 | 说明 |
|------|------|------|
| `lttb(series, targetSize)` | 序列、目标点数 | LTTB算法，保持视觉形状，O(n)复杂度 |
| `m4(series, Duration)` | 序列、桶持续时间 | M4算法，每桶保留first/min/max/last |
| `m4(series, int)` | 序列、目标桶数 | M4算法（按桶数指定） |
| `percentile(series, Duration, int)` | 序列、桶时间、百分位 | 百分位聚合降采样 |
| `median(series, Duration)` | 序列、桶时间 | 中位数降采样（第50百分位） |
| `variancePreserving(series, targetSize)` | 序列、目标点数 | 高方差区域分配更多点 |
| `peakPreserving(series, targetSize)` | 序列、目标点数 | 保持局部极值 |
| `mode(series, Duration, int)` | 序列、桶时间、分箱数 | 众数降采样 |
| `threshold(series, double)` | 序列、最小差值 | 仅保留变化超过阈值的点 |
| `percentageThreshold(series, double)` | 序列、百分比阈值 | 仅保留百分比变化超过阈值的点 |

```java
// LTTB：将10万点降到1000点，保持视觉形状
TimeSeries visual = DownsamplingUtil.lttb(series, 1000);

// M4：每5分钟桶保留first/min/max/last，适合图表渲染
TimeSeries chart = DownsamplingUtil.m4(series, Duration.ofMinutes(5));

// 保持峰值降采样
TimeSeries peaks = DownsamplingUtil.peakPreserving(series, 500);

// 阈值降采样：仅保留变化>=5.0的点
TimeSeries significant = DownsamplingUtil.threshold(series, 5.0);
```

---

## 6. 异常检测与变点检测

### 6.1 AnomalyDetectorUtil -- 异常检测

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `detectByZScore(series, threshold)` | 序列、Z阈值(通常2-3) | `List<DataPoint>` | Z-Score检测，适合正态分布 |
| `detectByIQR(series, multiplier)` | 序列、IQR乘数(通常1.5) | `List<DataPoint>` | IQR检测，不假设分布，更鲁棒 |
| `detectByMovingAverage(series, windowSize, threshold)` | 序列、窗口、偏差阈值 | `List<DataPoint>` | 移动平均偏差检测 |
| `detectByStdDev(series, sigmas)` | 序列、标准差倍数 | `List<DataPoint>` | 标准差检测（等同Z-Score） |
| `detectSpikes(series, changeThreshold)` | 序列、变化百分比(0-1) | `List<DataPoint>` | 突变/尖峰检测 |
| `detectOutOfRange(series, min, max)` | 序列、范围 | `List<DataPoint>` | 范围外检测 |
| `getSummary(anomalies, totalPoints)` | 异常列表、总点数 | `String` | 生成异常摘要 |

```java
// Z-Score 异常检测
List<DataPoint> anomalies = AnomalyDetectorUtil.detectByZScore(series, 2.0);

// IQR 异常检测（对异常值更鲁棒）
List<DataPoint> outliers = AnomalyDetectorUtil.detectByIQR(series, 1.5);

// 移动平均偏差检测
List<DataPoint> deviations = AnomalyDetectorUtil.detectByMovingAverage(series, 10, 5.0);

// 尖峰检测（变化超过50%）
List<DataPoint> spikes = AnomalyDetectorUtil.detectSpikes(series, 0.5);

// 范围检测
List<DataPoint> outOfRange = AnomalyDetectorUtil.detectOutOfRange(series, 0, 100);

// 摘要
String summary = AnomalyDetectorUtil.getSummary(anomalies, series.size());
```

### 6.2 ChangePointDetectionUtil -- 变点检测

检测时间序列中统计属性（均值、方差）发生突变的位置。

**结果类型：**

```java
// 变点记录
public record ChangePoint(
    int index,           // 时序中的索引
    Instant timestamp,   // 时间戳
    double score,        // 显著性分数
    ChangeType type,     // 变化类型
    double beforeMean,   // 变化前均值
    double afterMean     // 变化后均值
) implements Comparable<ChangePoint> {
    double magnitude();      // 变化幅度
    Direction direction();   // 变化方向（INCREASE/DECREASE/NONE）
}

// 变化类型
public enum ChangeType { MEAN_SHIFT, VARIANCE_SHIFT, TREND_CHANGE, STRUCTURAL }

// 变化方向
public enum Direction { INCREASE, DECREASE, NONE }
```

**检测方法：**

| 方法 | 说明 |
|------|------|
| `detectCusum(series, threshold)` | CUSUM算法，检测均值偏移 |
| `detectCusum(series)` | CUSUM（自动阈值=2.0） |
| `detectBinarySegmentation(series, minSegmentSize)` | 二分分割，使用BIC惩罚 |
| `detectBinarySegmentation(series)` | 二分分割（默认最小段=5） |
| `detectMeanShift(series, windowSize, threshold)` | 滚动均值偏移检测 |
| `detectMeanShift(series)` | 均值偏移（自动参数） |
| `detectVarianceShift(series, windowSize, threshold)` | 滚动方差偏移检测 |

**工具方法：**

| 方法 | 说明 |
|------|------|
| `getSegments(series, changePoints)` | 根据变点将序列切分为多个段 |
| `mergeNearbyChangePoints(changePoints, minDistance)` | 合并距离过近的变点 |
| `filterByScore(changePoints, minScore)` | 按显著性分数过滤 |

```java
// CUSUM检测
List<ChangePoint> cusumChanges = ChangePointDetectionUtil.detectCusum(series, 3.0);

// 二分分割
List<ChangePoint> segChanges = ChangePointDetectionUtil.detectBinarySegmentation(series, 10);

// 均值偏移
List<ChangePoint> meanShifts = ChangePointDetectionUtil.detectMeanShift(series, 20, 2.5);

// 获取分段
List<TimeSeries> segments = ChangePointDetectionUtil.getSegments(series, cusumChanges);

// 合并并过滤
List<ChangePoint> merged = ChangePointDetectionUtil.mergeNearbyChangePoints(cusumChanges, 10);
List<ChangePoint> significant = ChangePointDetectionUtil.filterByScore(merged, 3.0);
```

---

## 7. 预测

### 7.1 ForecastUtil -- 预测工具类

提供多种时序预测算法，所有方法均自动计算预测时间间隔。

| 方法 | 参数 | 说明 |
|------|------|------|
| `smaForecast(series, windowSize, steps)` | 窗口大小、步数 | 简单移动平均预测 |
| `smaForecastWithBounds(series, windowSize, steps, confidence)` | 窗口、步数、置信水平 | SMA预测+置信区间 |
| `wmaForecast(series, windowSize, steps)` | 窗口大小、步数 | 加权移动平均预测 |
| `emaForecast(series, alpha, steps)` | alpha(0-1]、步数 | 指数移动平均预测 |
| `holtForecast(series, alpha, beta, steps)` | alpha、beta、步数 | Holt双指数平滑（趋势感知） |
| `linearForecast(series, steps)` | 步数 | 线性回归趋势外推 |
| `linearForecastWithBounds(series, steps, confidence)` | 步数、置信水平 | 线性回归+置信区间 |
| `naiveForecast(series, steps)` | 步数 | 朴素预测（重复最后值） |
| `seasonalNaiveForecast(series, seasonLength, steps)` | 季节长度、步数 | 季节性朴素预测 |
| `driftForecast(series, steps)` | 步数 | 漂移预测（平均变化外推） |

**结果类型：**

```java
// 带置信区间的预测结果
public record ForecastResult(
    TimeSeries forecast,       // 预测值序列
    TimeSeries lower,          // 下界序列
    TimeSeries upper,          // 上界序列
    double confidenceLevel     // 置信水平
) {
    static ForecastResult empty(String name);
    boolean isEmpty();
    int size();
}

// 线性回归系数
public record LinearCoefficients(double intercept, double slope) {
    double predict(double x);
}
```

```java
// SMA 预测未来5步
TimeSeries forecast = ForecastUtil.smaForecast(series, 7, 5);

// 线性回归预测
TimeSeries linear = ForecastUtil.linearForecast(series, 10);

// Holt 双指数平滑（考虑趋势）
TimeSeries holt = ForecastUtil.holtForecast(series, 0.8, 0.2, 10);

// 带95%置信区间的预测
ForecastResult result = ForecastUtil.smaForecastWithBounds(series, 7, 5, 0.95);
TimeSeries predicted = result.forecast();
TimeSeries lowerBound = result.lower();
TimeSeries upperBound = result.upper();

// 漂移预测
TimeSeries drift = ForecastUtil.driftForecast(series, 10);
```

---

## 8. 相关性分析

### 8.1 CorrelationUtil

提供时间序列间的相关性分析工具，支持两序列按时间戳自动对齐。

**相关系数：**

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `pearson(series1, series2)` | 两个序列 | `double` | 皮尔逊相关系数（-1到1） |
| `pearson(double[], double[])` | 两个数组 | `double` | 数组版本 |
| `spearman(series1, series2)` | 两个序列 | `double` | 斯皮尔曼等级相关 |
| `spearman(double[], double[])` | 两个数组 | `double` | 数组版本 |

**互相关：**

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `crossCorrelation(s1, s2, maxLag)` | 两序列、最大滞后 | `CrossCorrelationResult` | 计算不同滞后下的互相关 |
| `crossCorrelation(x, y, maxLag)` | 两数组、最大滞后 | `CrossCorrelationResult` | 数组版本 |

**自相关：**

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `autocorrelation(series, maxLag)` | 序列、最大滞后 | `double[]` | 自相关函数ACF |
| `autocorrelation(values, maxLag)` | 数组、最大滞后 | `double[]` | 数组版本 |
| `partialAutocorrelation(series, maxLag)` | 序列、最大滞后 | `double[]` | 偏自相关函数PACF（Durbin-Levinson） |
| `partialAutocorrelation(values, maxLag)` | 数组、最大滞后 | `double[]` | 数组版本 |

**滞后分析与滚动相关：**

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `findOptimalLag(s1, s2, maxLag)` | 两序列、最大滞后 | `int` | 找到最佳滞后 |
| `correlationAtLag(s1, s2, lag)` | 两序列、滞后 | `double` | 指定滞后的相关性 |
| `rollingCorrelation(s1, s2, windowSize)` | 两序列、窗口大小 | `double[]` | 滚动相关 |
| `rollingCorrelation(x, y, windowSize)` | 两数组、窗口大小 | `double[]` | 数组版本 |

**CrossCorrelationResult：**

```java
public record CrossCorrelationResult(
    int[] lags,               // 滞后数组
    double[] correlations,    // 对应的相关系数
    int bestLag,              // 最佳滞后
    double bestCorrelation    // 最佳相关系数
) {
    double correlationAt(int lag);          // 查询指定滞后的相关性
    boolean isSignificant(int sampleSize);  // 95%置信度显著性检验
}
```

```java
// Pearson 相关
double corr = CorrelationUtil.pearson(cpuSeries, memorySeries);

// 互相关分析
CrossCorrelationResult ccf = CorrelationUtil.crossCorrelation(series1, series2, 20);
int bestLag = ccf.bestLag();
boolean significant = ccf.isSignificant(series1.size());

// 自相关
double[] acf = CorrelationUtil.autocorrelation(series, 30);

// 偏自相关
double[] pacf = CorrelationUtil.partialAutocorrelation(series, 20);

// 滚动相关
double[] rolling = CorrelationUtil.rollingCorrelation(series1, series2, 50);
```

---

## 9. 季节性分解

### 9.1 SeasonalDecompositionUtil

将时间序列分解为趋势(Trend)、季节性(Seasonal)和残差(Residual)三个组件。

**分解模型：**

```java
public enum DecompositionModel {
    ADDITIVE,        // Y = T + S + R
    MULTIPLICATIVE   // Y = T * S * R
}
```

**分解方法：**

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `decompose(series, period)` | 序列、周期 | `DecompositionResult` | 加法分解（中心移动平均） |
| `decompose(series, period, model)` | 序列、周期、模型 | `DecompositionResult` | 指定模型分解 |
| `stlDecompose(series, period)` | 序列、周期 | `DecompositionResult` | 类STL分解（Loess平滑） |
| `stlDecompose(series, period, iterations)` | 序列、周期、迭代次数 | `DecompositionResult` | 指定迭代次数的STL分解 |
| `detectSeasonalPeriod(series)` | 序列 | `int` | 自动检测季节周期（-1表示未找到） |
| `detectSeasonalPeriod(series, min, max)` | 序列、最小/最大周期 | `int` | 范围内检测周期 |
| `calculateSeasonalIndices(series, period)` | 序列、周期 | `double[]` | 计算季节性指数 |

**DecompositionResult：**

```java
public record DecompositionResult(
    TimeSeries original,    // 原始序列
    TimeSeries trend,       // 趋势组件
    TimeSeries seasonal,    // 季节性组件
    TimeSeries residual,    // 残差组件
    DecompositionModel model, // 分解模型
    int period              // 季节周期
) {
    TimeSeries reconstruct();          // 从组件重建原始序列
    TimeSeries seasonallyAdjusted();   // 获取季节性调整后的序列
    double seasonalStrength();         // 季节性强度 (0-1)
    double trendStrength();            // 趋势强度 (0-1)
}
```

```java
// 月度数据的年度季节性分解（周期=12）
DecompositionResult result = SeasonalDecompositionUtil.decompose(series, 12);

TimeSeries trend = result.trend();
TimeSeries seasonal = result.seasonal();
TimeSeries residual = result.residual();
TimeSeries adjusted = result.seasonallyAdjusted();

double sStrength = result.seasonalStrength(); // 季节性强度
double tStrength = result.trendStrength();    // 趋势强度

// STL 分解
DecompositionResult stl = SeasonalDecompositionUtil.stlDecompose(series, 12, 3);

// 自动检测季节周期
int period = SeasonalDecompositionUtil.detectSeasonalPeriod(series);

// 乘法分解
DecompositionResult mult = SeasonalDecompositionUtil.decompose(
    series, 12, SeasonalDecompositionUtil.DecompositionModel.MULTIPLICATIVE);
```

---

## 10. 时序压缩

### 10.1 CompressionUtil

针对时间序列数据优化的压缩算法。时间戳采用 Delta-delta 编码，值采用 Gorilla XOR 压缩。

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `compressTimestamps(long[])` | 时间戳数组(毫秒) | `byte[]` | Delta-delta编码压缩时间戳 |
| `decompressTimestamps(byte[])` | 压缩字节 | `long[]` | 解压时间戳 |
| `compressValues(double[])` | 值数组 | `byte[]` | Gorilla XOR压缩值 |
| `decompressValues(byte[])` | 压缩字节 | `double[]` | 解压值 |
| `compress(TimeSeries)` | 时间序列 | `CompressedTimeSeries` | 压缩完整序列 |
| `decompress(CompressedTimeSeries)` | 压缩序列 | `TimeSeries` | 解压完整序列 |
| `deltaEncode(int[])` | 整数数组 | `int[]` | 简单Delta编码 |
| `deltaDecode(int[])` | 编码数组 | `int[]` | 简单Delta解码 |

**CompressedTimeSeries：**

```java
public record CompressedTimeSeries(
    byte[] compressedTimestamps,   // 压缩后的时间戳
    byte[] compressedValues,       // 压缩后的值
    int pointCount,                // 点数
    int originalSizeBytes,         // 原始大小
    int compressedSizeBytes        // 压缩后大小
) {
    double compressionRatio();       // 压缩比（原始/压缩）
    double spaceSavingsPercent();    // 空间节省百分比
}
```

```java
// 压缩整个时间序列
CompressedTimeSeries compressed = CompressionUtil.compress(series);
double ratio = compressed.compressionRatio();         // 例如 3.5x
double savings = compressed.spaceSavingsPercent();    // 例如 71.4%

// 解压
TimeSeries restored = CompressionUtil.decompress(compressed);

// 单独压缩时间戳
long[] timestamps = {1000, 1060, 1120, 1180};
byte[] compBytes = CompressionUtil.compressTimestamps(timestamps);
long[] decompressed = CompressionUtil.decompressTimestamps(compBytes);
```

---

## 11. 窗口计算

### 11.1 Window 接口

所有窗口的统一接口，将数据点分配到一个或多个窗口。

```java
public interface Window {
    List<Long> assignWindows(DataPoint point);     // 分配窗口
    Instant getWindowStart(long windowKey);         // 窗口起始时间
    Instant getWindowEnd(long windowKey);           // 窗口结束时间
    Duration getSize();                             // 窗口大小
    default boolean isInWindow(long key, Instant t); // 判断是否在窗口内
    default long getWindowKey(Instant timestamp);    // 获取窗口键
}
```

### 11.2 TumblingWindow -- 滚动窗口

不重叠的固定大小窗口。每个数据点恰好属于一个窗口。

```java
TumblingWindow hourly = TumblingWindow.hourly();
TumblingWindow daily = TumblingWindow.daily();
TumblingWindow fiveMin = TumblingWindow.minutes(5);
TumblingWindow custom = TumblingWindow.of(Duration.ofMinutes(15));

// 将数据点分配到窗口
List<Long> windows = hourly.assignWindows(dataPoint); // 恰好1个窗口
```

### 11.3 SlidingWindow -- 滑动窗口

可重叠的窗口，每个数据点可能属于多个窗口。

```java
// 10分钟窗口，每1分钟滑动一次
SlidingWindow sliding = SlidingWindow.of(Duration.ofMinutes(10), Duration.ofMinutes(1));
double overlap = sliding.getOverlapRatio(); // 0.9

List<Long> windows = sliding.assignWindows(dataPoint); // 可能属于多个窗口
```

### 11.4 SessionWindow -- 会话窗口

基于活动间隔动态创建窗口。数据点间隔超过 gap 则开启新会话。

```java
SessionWindow session = SessionWindow.of(Duration.ofMinutes(30));
Duration gap = session.getGap();

// 会话管理
Map<Instant, Instant> sessions = session.getSessions(); // 所有会话的起止时间
int count = session.getSessionCount();
session.clear(); // 清除会话状态
```

---

## 12. 查询构建器

### 12.1 Query -- 流式查询

```java
public final class Query {
    static Query from(TimeSeries series);         // 创建查询
    Query range(Instant from, Instant to);        // 时间范围
    Query range(TimeRange range);                 // 时间范围(Record)
    Query last(Duration duration);                // 最近时长
    Query limit(int limit);                       // 结果数量限制
    Query filter(Predicate<DataPoint> filter);    // 自定义过滤
    Query valueRange(double min, double max);     // 值范围过滤
    Query tag(String key, String value);          // 标签过滤
    Query aggregate(AggregationType aggregation); // 设置聚合
    Query groupBy(Duration interval);             // 分组间隔
    List<DataPoint> execute();                    // 执行查询
    OptionalDouble executeScalar();               // 执行并返回单值
}
```

```java
// 流式查询示例
List<DataPoint> result = Query.from(series)
    .last(Duration.ofHours(1))
    .valueRange(0, 100)
    .aggregate(AggregationType.AVG)
    .groupBy(Duration.ofMinutes(5))
    .limit(100)
    .execute();

// 标量查询
OptionalDouble avg = Query.from(series)
    .range(TimeRange.today())
    .aggregate(AggregationType.AVG)
    .executeScalar();

// 标签过滤
List<DataPoint> serverData = Query.from(series)
    .tag("host", "server1")
    .execute();
```

### 12.2 TimeRange -- 时间范围

不可变 Record，表示查询的时间范围。

```java
public record TimeRange(Instant from, Instant to) {
    static TimeRange last(Duration duration);               // 从现在回溯
    static TimeRange today();                               // 最近24小时
    static TimeRange thisHour();                            // 最近1小时
    static TimeRange of(Instant from, Instant to);          // 指定范围
    static TimeRange ofMillis(long fromMillis, long toMillis); // 毫秒范围

    Duration duration();              // 范围时长
    boolean isValid();                // 是否有效（from <= to）
    boolean isEmpty();                // 是否为空（from == to）
    boolean contains(Instant t);      // 是否包含时间戳
    boolean overlaps(TimeRange other); // 是否重叠
    TimeRange extend(Duration d);     // 双向扩展
    TimeRange shift(Duration d);      // 平移
}
```

### 12.3 QueryLimiter -- 查询限制器

防止资源耗尽的查询保护。默认最大范围365天，最大结果10万条。

| 方法 | 说明 |
|------|------|
| `validateRange(Instant, Instant)` | 验证查询范围 |
| `validateRange(TimeRange)` | 验证时间范围 |
| `limitResult(List)` | 限制结果大小 |
| `exceedsLimit(int)` | 检查是否超限 |
| `setMaxRangeDays(int)` | 设置最大范围天数 |
| `setMaxResultSize(int)` | 设置最大结果数 |
| `getMaxRangeDays()` | 获取最大范围天数 |
| `getMaxResultSize()` | 获取最大结果数 |
| `resetDefaults()` | 重置为默认值 |

---

## 13. 数据验证

### 13.1 DataPointValidator

| 方法 | 参数 | 说明 |
|------|------|------|
| `validate(DataPoint)` | 数据点 | 完整验证（时间戳+值），不合法抛出 `TimeSeriesException` |
| `validateTimestamp(Instant)` | 时间戳 | 验证时间戳在合法范围内（2000-2100） |
| `validateValue(double)` | 值 | 验证值不为NaN/Infinity |
| `isValid(DataPoint)` | 数据点 | 检查是否合法（不抛异常） |
| `isValidValue(double)` | 值 | 检查值是否合法 |
| `isValidTimestamp(Instant)` | 时间戳 | 检查时间戳是否合法 |
| `getMinTimestamp()` | - | 获取最小合法时间戳 |
| `getMaxTimestamp()` | - | 获取最大合法时间戳 |

```java
// 验证数据点
DataPointValidator.validate(dataPoint); // 不合法抛出异常

// 安全检查
if (DataPointValidator.isValid(dataPoint)) {
    series.add(dataPoint);
}
```

---

## 14. 异常体系

### 14.1 TimeSeriesErrorCode -- 错误码

```java
public enum TimeSeriesErrorCode {
    // 1xxx - 数据错误
    INVALID_TIMESTAMP("TS-1001", "Invalid timestamp"),
    INVALID_VALUE("TS-1002", "Invalid value"),
    DUPLICATE_TIMESTAMP("TS-1003", "Duplicate timestamp"),
    EMPTY_SERIES("TS-1004", "Time series is empty"),

    // 2xxx - 查询错误
    QUERY_RANGE_TOO_LARGE("TS-2001", "Query range too large"),
    INVALID_TIME_RANGE("TS-2002", "Invalid time range"),
    SERIES_NOT_FOUND("TS-2003", "Time series not found"),

    // 3xxx - 计算错误
    AGGREGATION_FAILED("TS-3001", "Aggregation failed"),
    WINDOW_SIZE_INVALID("TS-3002", "Invalid window size"),
    INSUFFICIENT_DATA("TS-3003", "Insufficient data"),

    // 4xxx - 容量错误
    CAPACITY_EXCEEDED("TS-4001", "Capacity exceeded"),
    MEMORY_LIMIT_EXCEEDED("TS-4002", "Memory limit exceeded");

    String code();     // 错误码
    String message();  // 错误消息
}
```

### 14.2 TimeSeriesException

```java
public class TimeSeriesException extends RuntimeException {
    public TimeSeriesException(TimeSeriesErrorCode errorCode)
    public TimeSeriesException(TimeSeriesErrorCode errorCode, String detail)
    public TimeSeriesException(TimeSeriesErrorCode errorCode, Throwable cause)
    public TimeSeriesException(TimeSeriesErrorCode errorCode, String detail, Throwable cause)

    TimeSeriesErrorCode errorCode();   // 获取错误码
    String getErrorCodeString();       // 获取错误码字符串
}
```

---

## 15. 使用示例

### 15.1 应用内指标收集

```java
// 记录系统指标
OpenTimeSeries.record("cpu_usage", 45.5);
OpenTimeSeries.record("memory_usage", 72.3);
OpenTimeSeries.record("request_count", 150);

// 查询最近5分钟
List<DataPoint> recentCpu = OpenTimeSeries.query("cpu_usage", Duration.ofMinutes(5));

// 统计
TimeSeriesStats stats = OpenTimeSeries.stats("cpu_usage");
System.out.println("CPU - 均值: " + stats.average() + ", 最大: " + stats.max());

// 定期清理
OpenTimeSeries.cleanup(Duration.ofDays(7));
```

### 15.2 异常检测与告警

```java
TimeSeries latency = OpenTimeSeries.get("api_latency");

// Z-Score 检测高延迟
List<DataPoint> anomalies = AnomalyDetectorUtil.detectByZScore(latency, 2.5);
if (!anomalies.isEmpty()) {
    String summary = AnomalyDetectorUtil.getSummary(anomalies, latency.size());
    System.out.println("延迟异常: " + summary);
}

// 变点检测：是否发生性能退化
List<ChangePointDetectionUtil.ChangePoint> changes =
    ChangePointDetectionUtil.detectMeanShift(latency, 50, 3.0);
for (var cp : changes) {
    System.out.printf("在 %s 检测到均值变化: %.2f -> %.2f (方向: %s)%n",
        cp.timestamp(), cp.beforeMean(), cp.afterMean(), cp.direction());
}
```

### 15.3 数据分析与预测

```java
// 相关性分析
double corr = CorrelationUtil.pearson(cpuSeries, memorySeries);
System.out.println("CPU/内存相关性: " + corr);

// 季节性分解
int period = SeasonalDecompositionUtil.detectSeasonalPeriod(salesSeries);
if (period > 0) {
    var result = SeasonalDecompositionUtil.decompose(salesSeries, period);
    System.out.printf("季节性强度: %.2f, 趋势强度: %.2f%n",
        result.seasonalStrength(), result.trendStrength());
}

// 预测
TimeSeries forecast = ForecastUtil.holtForecast(salesSeries, 0.8, 0.2, 30);
ForecastUtil.ForecastResult withBounds =
    ForecastUtil.linearForecastWithBounds(salesSeries, 30, 0.95);
```

### 15.4 可视化数据准备

```java
// 原始数据10万点，降到1000点用于前端图表
TimeSeries chartData = DownsamplingUtil.lttb(rawSeries, 1000);

// M4降采样保持极值，适合折线图
TimeSeries m4Data = DownsamplingUtil.m4(rawSeries, Duration.ofMinutes(5));

// 压缩传输
CompressionUtil.CompressedTimeSeries compressed = CompressionUtil.compress(rawSeries);
System.out.printf("压缩比: %.1fx, 节省: %.1f%%%n",
    compressed.compressionRatio(), compressed.spaceSavingsPercent());
```

### 15.5 流式查询

```java
// 复杂查询
List<DataPoint> result = Query.from(series)
    .range(TimeRange.last(Duration.ofHours(24)))
    .filter(p -> p.value() > 0)
    .aggregate(AggregationType.AVG)
    .groupBy(Duration.ofHours(1))
    .limit(24)
    .execute();

// 标量聚合
OptionalDouble maxToday = Query.from(series)
    .range(TimeRange.today())
    .aggregate(AggregationType.MAX)
    .executeScalar();
```

---

## 16. 线程安全与性能

### 16.1 线程安全设计

| 类 | 策略 | 说明 |
|-----|------|------|
| `DataPoint` | Record 不可变 | 天然线程安全 |
| `TimeSeriesStats` | Record 不可变 | 天然线程安全 |
| `TimeRange` | Record 不可变 | 天然线程安全 |
| `TimeSeries` | `ConcurrentSkipListMap` + `ConcurrentHashMap` | 并发读写安全，O(log n) |
| `OpenTimeSeries` | `ConcurrentHashMap` 管理全局存储 | 并发注册/查询安全 |
| `BoundedTimeSeries` | 继承 `TimeSeries` 的并发特性 | 驱逐操作安全 |

### 16.2 性能特征

- **数据点创建**：Record 构造，极低开销
- **数据插入**：`ConcurrentSkipListMap.put`，O(log n)，无锁并发读
- **范围查询**：`subMap` 返回视图，O(log n) 定位 + O(k) 遍历
- **聚合计算**：Stream 遍历，O(n)
- **移动平均**：滑动窗口优化，O(n)
- **LTTB降采样**：O(n) 线性时间
- **内存占用**：每个 DataPoint 约 40 字节（含 tags Map 开销），纯数值约 16 字节

---

## 17. FAQ

### Q1: 如何处理乱序数据？

`ConcurrentSkipListMap` 自动按时间戳排序，无需手动排序：

```java
TimeSeries series = new TimeSeries("test");
series.add(Instant.parse("2025-01-01T12:00:00Z"), 100);
series.add(Instant.parse("2025-01-01T10:00:00Z"), 80);  // 乱序
series.add(Instant.parse("2025-01-01T14:00:00Z"), 120);
// all() 自动有序: 10:00->80, 12:00->100, 14:00->120
```

### Q2: 如何选择异常检测方法？

| 方法 | 适用场景 | 参数建议 |
|------|----------|----------|
| Z-Score | 近似正态分布的数据 | threshold=2~3 |
| IQR | 分布未知/偏态数据，对异常值更鲁棒 | multiplier=1.5 |
| 移动平均 | 需要考虑局部趋势的检测 | 按采样率调节窗口 |
| 尖峰检测 | 突变/瞬时异常 | changeThreshold=0.3~0.5 |
| 范围检测 | 已知业务边界 | 设定合理范围 |

### Q3: 如何选择窗口类型？

| 窗口类型 | 适用场景 | 特点 |
|----------|----------|------|
| `TumblingWindow` | 固定周期统计（每小时/每天报表） | 不重叠，数据不重复 |
| `SlidingWindow` | 实时趋势分析（移动平均） | 可重叠，平滑过渡 |
| `SessionWindow` | 用户行为分析（会话统计） | 基于活动间隔动态分组 |

### Q4: 如何选择降采样算法？

| 算法 | 适用场景 | 特点 |
|------|----------|------|
| `lttb` | 图表可视化 | 保持视觉形状，O(n) |
| `m4` | 图表渲染 | 保持极值，每桶4点 |
| `peakPreserving` | 关注极值 | 保留局部峰/谷 |
| `variancePreserving` | 波动区域关注 | 高方差区域更多点 |
| `threshold` | 仅关注变化 | 去除平稳期 |
| `percentile` | 统计分析 | 桶内取百分位 |

### Q5: 如何优化大规模数据的内存？

```java
// 1. 使用 BoundedTimeSeries 限制容量
BoundedTimeSeries bounded = BoundedTimeSeries.of("metrics", 100_000, Duration.ofDays(1));

// 2. 定期清理过期数据
OpenTimeSeries.cleanup(Duration.ofDays(7));

// 3. 降采样减少数据量
TimeSeries hourly = SamplerUtil.downsample(raw, Duration.ofHours(1), AggregationType.AVG);

// 4. 压缩存储/传输
CompressedTimeSeries compressed = CompressionUtil.compress(series);
```

---

## 18. 版本信息

| 属性 | 值 |
|------|-----|
| 文档版本 | 3.0 |
| 更新日期 | 2026-02-27 |
| 模块名 | opencode-base-timeseries |
| 最低 JDK | 25 |
| 第三方依赖 | 无（纯 JDK 实现） |

### 功能清单

| 功能 | 状态 | 说明 |
|------|------|------|
| DataPoint 数据点 | 已完成 | Record 实现，支持 tags，不可变 |
| TimeSeries 时间序列 | 已完成 | ConcurrentSkipListMap 存储，链式API |
| BoundedTimeSeries 有界序列 | 已完成 | 自动容量/时间驱逐 |
| 聚合计算 | 已完成 | sum/avg/min/max/count/percentile/variance/stdDev |
| 移动平均/EMA | 已完成 | 滑动窗口优化 |
| 变换操作 | 已完成 | map/diff/cumSum/combine/derivative |
| 降采样 | 已完成 | 基础聚合+LTTB/M4/峰值保持/方差保持/百分位/阈值 |
| 上采样/填充 | 已完成 | zero/previous/next/linear/average/NaN 六种策略 |
| 异常检测 | 已完成 | Z-Score/IQR/移动平均/尖峰/范围 |
| 变点检测 | 已完成 | CUSUM/二分分割/均值偏移/方差偏移 |
| 预测 | 已完成 | SMA/WMA/EMA/Holt/线性/朴素/季节朴素/漂移+置信区间 |
| 相关性分析 | 已完成 | Pearson/Spearman/互相关/ACF/PACF/滚动相关 |
| 季节性分解 | 已完成 | 经典(加法/乘法)/STL/周期检测/季节指数 |
| 时序压缩 | 已完成 | Gorilla XOR值压缩/Delta-delta时间戳压缩 |
| 查询构建器 | 已完成 | 流式API/时间/值/标签过滤/分组聚合 |
| 窗口计算 | 已完成 | 滚动/滑动/会话窗口 |
| 容量限制 | 已完成 | BoundedTimeSeries |
| 查询保护 | 已完成 | QueryLimiter (范围/结果限制) |
| 数据验证 | 已完成 | DataPointValidator (时间戳/值验证) |
| 异常体系 | 已完成 | TimeSeriesException + 错误码 TS-1xxx~4xxx |
