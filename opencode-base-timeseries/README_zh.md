# OpenCode Base TimeSeries

**适用于 Java 25+ 的时间序列数据处理库**

`opencode-base-timeseries` 提供完整的时间序列数据结构、聚合、窗口、异常检测、预测、降采样和分析工具。零外部依赖。

## 功能特性

### 核心功能
- **时间序列存储**：支持并发访问的全局命名序列存储
- **数据点**：不可变的时间戳数据点，支持可选标签
- **有界序列**：大小限制和时间限制的时间序列，自动淘汰
- **统计信息**：最小值、最大值、平均值、标准差、百分位

### 高级功能
- **聚合**：求和、计数、最小值、最大值、平均值聚合器及滚动统计
- **移动平均**：简单移动平均（SMA）和指数移动平均（EMA）
- **窗口操作**：翻滚窗口、滑动窗口和会话窗口
- **异常检测**：Z-Score、IQR、移动平均、尖峰检测
- **变点检测**：CUSUM、二分分割、均值/方差漂移检测
- **预测**：SMA、EMA、WMA、线性回归、Holt 双指数平滑，支持置信区间
- **降采样**：LTTB、M4、保峰值、百分位和阈值算法
- **季节性分解**：经典分解和 STL 分解，支持自动周期检测
- **相关性分析**：Pearson、Spearman、互相关、自相关、偏自相关（PACF）
- **压缩**：Gorilla delta-delta + XOR 压缩
- **查询**：流式查询构建器，支持时间范围、过滤、聚合和速率限制

### V1.0.3 新增功能
- **对齐与重采样**：将序列对齐到共同时间网格，支持多种填充策略
- **插值**：线性插值、阶梯插值（LOCF）、自然三次样条插值
- **速率计算**：计数器 rate/irate/increase，自动处理 counter reset（Prometheus 风格）
- **缺口检测**：检测数据缺口、计算数据完整性百分比
- **流式统计**：基于 Welford 算法的在线统计（每次更新 O(1)，可合并）
- **数学变换**：归一化、z-score、对数、指数、缩放、布林带、滚动标准差
- **时间分桶**：日历级聚合（时/天/周/月/季/年），支持时区
- **滞后/领先/偏移**：时间序列偏移和百分比变化计算
- **异常体系**：`TimeSeriesException` 现在继承 `OpenException`（统一异常基类）

## 快速开始

### Maven 依赖
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-timeseries</artifactId>
    <version>1.0.3</version>
</dependency>
```

### 基本用法

```java
import cloud.opencode.base.timeseries.*;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

// 向命名序列记录数据点
OpenTimeSeries.record("cpu.usage", 75.5);
OpenTimeSeries.record("cpu.usage", Instant.now(), 82.3);

// 查询最近数据
List<DataPoint> points = OpenTimeSeries.query("cpu.usage", Duration.ofHours(1));

// 获取统计信息
TimeSeriesStats stats = OpenTimeSeries.stats("cpu.usage");

// 创建独立序列
TimeSeries series = OpenTimeSeries.create("temperature");
series.addNow(22.5);
series.addNow(23.1);

// 降采样
TimeSeries downsampled = OpenTimeSeries.downsample(series, Duration.ofMinutes(5));

// 移动平均
TimeSeries smoothed = OpenTimeSeries.movingAverage(series, 10);

// 异常检测
List<DataPoint> anomalies = OpenTimeSeries.detectAnomalies(series, 2.0);

// 预测
TimeSeries forecast = OpenTimeSeries.linearForecast(series, 10);

// 归一化
TimeSeries normalized = OpenTimeSeries.normalize(series);
```

### V1.0.3 新功能用法

```java
import cloud.opencode.base.timeseries.*;
import cloud.opencode.base.timeseries.alignment.AlignmentUtil;
import cloud.opencode.base.timeseries.bucket.TimeBucket;
import cloud.opencode.base.timeseries.bucket.TimeBucketUtil;
import cloud.opencode.base.timeseries.interpolation.InterpolationUtil;
import cloud.opencode.base.timeseries.math.MathUtil;
import cloud.opencode.base.timeseries.quality.Gap;
import cloud.opencode.base.timeseries.quality.GapDetector;
import cloud.opencode.base.timeseries.rate.RateUtil;
import cloud.opencode.base.timeseries.sampling.AggregationType;
import cloud.opencode.base.timeseries.sampling.FillStrategy;
import cloud.opencode.base.timeseries.stats.StreamingStats;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

// --- 对齐与重采样 ---
TimeSeries[] aligned = AlignmentUtil.align(seriesA, seriesB,
    Duration.ofMinutes(1), FillStrategy.LINEAR);
TimeSeries regular = AlignmentUtil.resample(series,
    Duration.ofSeconds(30), FillStrategy.PREVIOUS);
TimeSeries gridded = AlignmentUtil.alignToGrid(series,
    Duration.ofMinutes(1), AggregationType.AVG);

// --- 插值 ---
TimeSeries linear = InterpolationUtil.linear(series, Duration.ofSeconds(10));
TimeSeries stepped = InterpolationUtil.step(series, Duration.ofSeconds(10));
TimeSeries smooth = InterpolationUtil.spline(series, Duration.ofSeconds(10));
TimeSeries atPoints = InterpolationUtil.interpolateAt(series,
    Instant.parse("2026-01-01T00:05:00Z"),
    Instant.parse("2026-01-01T00:15:00Z"));

// --- 计数器速率（Prometheus 风格） ---
TimeSeries perSecRate = RateUtil.rate(counter, Duration.ofMinutes(5));
TimeSeries instantRate = RateUtil.irate(counter);
TimeSeries increase = RateUtil.increase(counter, Duration.ofMinutes(5));
int resetCount = RateUtil.resets(counter);
TimeSeries nnDeriv = RateUtil.nonNegativeDerivative(counter);

// --- 缺口检测与数据质量 ---
List<Gap> gaps = GapDetector.detectGaps(series, Duration.ofMinutes(1));
List<Gap> gapsCustom = GapDetector.detectGaps(series, Duration.ofMinutes(1), 2.0);
double completeness = GapDetector.dataCompleteness(series,
    Duration.ofMinutes(1), from, to);
Gap longest = GapDetector.longestGap(series).orElse(null);

// --- 流式统计（每次更新 O(1)） ---
StreamingStats ss = new StreamingStats();
ss.add(42.0);
ss.add(43.5);
double mean = ss.mean();
TimeSeriesStats snapshot = ss.snapshot();
StreamingStats other = new StreamingStats();
ss.merge(other);  // 并行合并

// --- 数学变换 ---
TimeSeries norm = MathUtil.normalize(series);
TimeSeries zs = MathUtil.zScore(series);
TimeSeries logged = MathUtil.log(series);
TimeSeries scaled = MathUtil.scale(series, 2.0);
TimeSeries offset = MathUtil.offset(series, -10.0);
TimeSeries rollStd = MathUtil.rollingStdDev(series, 20);
MathUtil.BollingerBands bb = MathUtil.bollingerBands(series, 20, 2.0);

// --- 日历级聚合（带时区） ---
TimeSeries daily = TimeBucketUtil.bucket(series, TimeBucket.DAY,
    ZoneId.of("Asia/Shanghai"), AggregationType.AVG);
TimeSeries monthly = TimeBucketUtil.bucket(series, TimeBucket.MONTH,
    ZoneId.of("UTC"), AggregationType.SUM);
TimeSeries fiveMin = TimeBucketUtil.bucket(series,
    Duration.ofMinutes(5), Instant.EPOCH, AggregationType.MAX);

// --- 滞后/领先/偏移/百分比变化 ---
TimeSeries lagged = series.lag(1);
TimeSeries led = series.lead(1);
TimeSeries shifted = series.shift(Duration.ofHours(-1));
TimeSeries returns = series.pctChange(1);
```

## API 方法参考

### AlignmentUtil（`timeseries.alignment`）— V1.0.3

| 方法 | 说明 |
|------|------|
| `align(TimeSeries a, TimeSeries b, Duration interval, FillStrategy fill)` | 将两个序列对齐到共同规则时间网格 |
| `resample(TimeSeries ts, Duration interval, FillStrategy fill)` | 将单个序列重采样为规则间隔 |
| `alignToGrid(TimeSeries ts, Duration interval, AggregationType agg)` | 对齐到网格边界并聚合 |

### InterpolationUtil（`timeseries.interpolation`）— V1.0.3

| 方法 | 说明 |
|------|------|
| `linear(TimeSeries ts, Duration interval)` | 线性插值（需 2+ 个点） |
| `step(TimeSeries ts, Duration interval)` | 阶梯插值 / LOCF |
| `spline(TimeSeries ts, Duration interval)` | 自然三次样条插值（需 3+ 个点） |
| `interpolateAt(TimeSeries ts, Instant... targets)` | 在指定时间戳处线性插值/外推 |

### RateUtil（`timeseries.rate`）— V1.0.3

| 方法 | 说明 |
|------|------|
| `rate(TimeSeries counter, Duration window)` | 连续点间每秒速率，处理计数器重置 |
| `irate(TimeSeries counter)` | 最后两点的瞬时速率 |
| `increase(TimeSeries counter, Duration window)` | 连续点间总增量，处理重置 |
| `resets(TimeSeries counter)` | 计数器重置次数 |
| `nonNegativeDerivative(TimeSeries ts)` | 非负导数（负增量视为重置） |

### GapDetector（`timeseries.quality`）— V1.0.3

| 方法 | 说明 |
|------|------|
| `detectGaps(TimeSeries ts, Duration expectedInterval)` | 检测超过 1.5 倍预期间隔的缺口 |
| `detectGaps(TimeSeries ts, Duration expectedInterval, double toleranceFactor)` | 自定义容差因子检测缺口 |
| `dataCompleteness(TimeSeries ts, Duration expectedInterval, Instant from, Instant to)` | 计算数据完整性 [0.0, 1.0] |
| `longestGap(TimeSeries ts)` | 查找最长缺口 |
| `gapCount(TimeSeries ts, Duration expectedInterval)` | 缺口数量 |

### Gap（`timeseries.quality`）— V1.0.3

| 方法 | 说明 |
|------|------|
| `Gap(Instant start, Instant end)` | 不可变记录（验证 start <= end） |
| `length()` | 开始到结束的时长 |

### StreamingStats（`timeseries.stats`）— V1.0.3

| 方法 | 说明 |
|------|------|
| `add(double value)` | 添加值（O(1)，拒绝 NaN/Infinity） |
| `add(DataPoint point)` | 添加数据点的值 |
| `addAll(TimeSeries ts)` | 添加序列中的所有点 |
| `count()` / `sum()` / `mean()` / `min()` / `max()` | 运行统计量（O(1) 读取） |
| `variance()` / `stdDev()` | 样本方差/标准差（贝塞尔校正） |
| `snapshot()` | 创建不可变 `TimeSeriesStats` 快照 |
| `merge(StreamingStats other)` | 合并两个独立统计量（并行 Welford） |
| `reset()` | 重置为空状态 |

### MathUtil（`timeseries.math`）— V1.0.3

| 方法 | 说明 |
|------|------|
| `normalize(TimeSeries ts)` | 最小-最大归一化到 [0, 1] |
| `zScore(TimeSeries ts)` | Z 分数标准化（单遍 Welford） |
| `log(TimeSeries ts)` / `log10(ts)` | 自然对数 / 以 10 为底的对数 |
| `exp(TimeSeries ts)` | 指数变换 |
| `abs(TimeSeries ts)` | 绝对值 |
| `scale(TimeSeries ts, double factor)` | 按因子缩放 |
| `offset(TimeSeries ts, double delta)` | 偏移所有值 |
| `power(TimeSeries ts, double exponent)` | 幂变换 |
| `rollingStdDev(TimeSeries ts, int window)` | 滚动标准差（滑动 Welford） |
| `bollingerBands(TimeSeries ts, int window, double numStdDev)` | 布林带（上轨、中轨 SMA、下轨） |

### TimeBucketUtil（`timeseries.bucket`）— V1.0.3

| 方法 | 说明 |
|------|------|
| `bucket(TimeSeries ts, TimeBucket bucket, ZoneId zone, AggregationType agg)` | 日历级分桶（SECOND 到 YEAR） |
| `bucket(TimeSeries ts, Duration interval, Instant origin, AggregationType agg)` | 固定时长分桶（自定义起点） |

### TimeSeries — V1.0.3 新增方法

| 方法 | 说明 |
|------|------|
| `lag(int periods)` | 创建滞后序列（值向后移动 n 个周期） |
| `lead(int periods)` | 创建领先序列（值向前移动 n 个周期） |
| `shift(Duration offset)` | 将所有时间戳偏移指定时长 |
| `pctChange(int periods)` | 计算 n 个周期的百分比变化 |

## 类参考

### 根包（`cloud.opencode.base.timeseries`）
| 类 | 说明 |
|----|------|
| `OpenTimeSeries` | 主门面：全局存储、记录、查询、聚合、预测、降采样 |
| `TimeSeries` | 核心时间序列：添加、查询、范围、聚合、变换、lag/lead/shift/pctChange |
| `BoundedTimeSeries` | 大小限制和时间限制的时间序列，自动淘汰 |
| `DataPoint` | 不可变记录：时间戳 + 值 + 可选标签 |
| `Aggregation` | 移动平均、EMA、滚动统计工具 |
| `TimeSeriesStats` | 不可变统计记录：count、sum、average、min、max、stdDev |

### 聚合（`timeseries.aggregation`）
| 类 | 说明 |
|----|------|
| `AggregationResult` | 密封结果：Success / Empty / Error |
| `Aggregator` | 函数式聚合器接口 |
| `AvgAggregator` / `CountAggregator` / `MaxAggregator` / `MinAggregator` / `SumAggregator` | 单例聚合器实现 |

### 分析（`timeseries.analysis`）
| 类 | 说明 |
|----|------|
| `CorrelationUtil` | Pearson、Spearman、互相关、自相关、PACF、滚动相关 |

### 压缩（`timeseries.compression`）
| 类 | 说明 |
|----|------|
| `CompressionUtil` | Gorilla delta-delta + XOR 压缩/解压 |

### 分解（`timeseries.decomposition`）
| 类 | 说明 |
|----|------|
| `SeasonalDecompositionUtil` | 经典分解和 STL 分解，支持自动周期检测 |

### 检测（`timeseries.detection`）
| 类 | 说明 |
|----|------|
| `AnomalyDetectorUtil` | Z-Score、IQR、移动平均、尖峰、范围检测 |
| `ChangePointDetectionUtil` | CUSUM、二分分割、均值/方差漂移检测 |

### 异常（`timeseries.exception`）
| 类 | 说明 |
|----|------|
| `TimeSeriesErrorCode` | 分类错误码：TS-1xxx（数据）到 TS-6xxx（速率/质量） |
| `TimeSeriesException` | 继承 `OpenException`，带结构化错误码 |

### 预测（`timeseries.forecast`）
| 类 | 说明 |
|----|------|
| `ForecastUtil` | SMA、WMA、EMA、线性回归、Holt 方法、漂移、朴素、季节朴素 |

### 查询（`timeseries.query`）
| 类 | 说明 |
|----|------|
| `Query` | 流式查询构建器：range、filter、aggregate、groupBy、limit |
| `QueryLimiter` | 线程安全的查询范围和结果大小限制（AtomicInteger） |
| `TimeRange` | 不可变时间范围记录，含工厂方法 |

### 采样（`timeseries.sampling`）
| 类 | 说明 |
|----|------|
| `AggregationType` | 枚举：SUM、AVG、MIN、MAX、FIRST、LAST、COUNT |
| `DownsamplingUtil` | LTTB、M4、保峰值、百分位、阈值降采样算法 |
| `FillStrategy` | 枚举：ZERO、PREVIOUS、LINEAR、NAN、NEXT、AVERAGE |
| `SamplerUtil` | 基于时间的降采样、缺口填充、重采样、随机/系统采样 |

### 验证（`timeseries.validation`）
| 类 | 说明 |
|----|------|
| `DataPointValidator` | 验证时间戳范围 [2000-2100]、有限值、NaN/Infinity 拒绝 |

### 对齐（`timeseries.alignment`）— V1.0.3
| 类 | 说明 |
|----|------|
| `AlignmentUtil` | 对齐、重采样、网格对齐，支持填充策略 |

### 插值（`timeseries.interpolation`）— V1.0.3
| 类 | 说明 |
|----|------|
| `InterpolationUtil` | 线性、阶梯（LOCF）、自然三次样条插值 |

### 速率（`timeseries.rate`）— V1.0.3
| 类 | 说明 |
|----|------|
| `RateUtil` | 计数器 rate、irate、increase、resets、非负导数 |

### 数据质量（`timeseries.quality`）— V1.0.3
| 类 | 说明 |
|----|------|
| `Gap` | 不可变缺口记录，含 start/end 验证 |
| `GapDetector` | 缺口检测、数据完整性、最长缺口 |

### 统计（`timeseries.stats`）— V1.0.3
| 类 | 说明 |
|----|------|
| `StreamingStats` | 在线 Welford 统计，支持并行合并 |

### 数学（`timeseries.math`）— V1.0.3
| 类 | 说明 |
|----|------|
| `MathUtil` | 归一化、z-score、log/exp/abs/scale/offset/power、滚动标准差、布林带 |

### 分桶（`timeseries.bucket`）— V1.0.3
| 类 | 说明 |
|----|------|
| `TimeBucket` | 枚举：SECOND、MINUTE、HOUR、DAY、WEEK、MONTH、QUARTER、YEAR |
| `TimeBucketUtil` | 日历级和固定时长分桶聚合 |

### 窗口（`timeseries.window`）
| 类 | 说明 |
|----|------|
| `Window` | 窗口基础接口 |
| `TumblingWindow` | 固定大小非重叠窗口 |
| `SlidingWindow` | 可配置滑动步长的重叠窗口 |
| `SessionWindow` | 基于活动的窗口，带间隙超时（线程安全） |

## 环境要求

- Java 25+
- 无外部依赖

## 开源协议

Apache License 2.0

## 作者

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
