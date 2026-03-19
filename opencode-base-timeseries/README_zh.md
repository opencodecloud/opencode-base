# OpenCode Base TimeSeries

**适用于 Java 25+ 的时间序列数据处理库**

`opencode-base-timeseries` 提供了完整的时间序列数据结构、聚合、窗口、异常检测、预测、降采样和分析工具。

## 功能特性

### 核心功能
- **时间序列存储**：支持并发访问的全局命名序列存储
- **数据点**：时间戳数据点的创建和操作
- **有界序列**：大小限制和时间限制的时间序列
- **统计信息**：最小值、最大值、平均值、标准差和综合统计

### 高级功能
- **聚合**：求和、计数、最小值、最大值、平均值聚合器及滚动统计
- **移动平均**：简单移动平均（SMA）和指数移动平均（EMA）
- **窗口操作**：翻滚窗口、滑动窗口和会话窗口
- **异常检测**：基于 Z-Score 的异常检测
- **变点检测**：检测时间序列趋势中的显著变化
- **预测**：SMA、EMA、线性回归、Holt 双指数平滑，支持置信区间
- **降采样**：LTTB、M4、保峰值、百分位和阈值算法
- **季节性分解**：趋势、季节性和残差分量提取
- **相关性分析**：跨序列相关性计算
- **压缩**：时间序列数据压缩工具
- **查询**：时间范围查询，支持速率限制

## 快速开始

### Maven 依赖
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-timeseries</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 基本用法

```java
import cloud.opencode.base.timeseries.*;
import java.time.Duration;
import java.time.Instant;

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
TimeSeries holtForecast = OpenTimeSeries.holtForecast(series, 0.3, 0.1, 10);

// 归一化
TimeSeries normalized = OpenTimeSeries.normalize(series);
```

## 类参考

### 根包 (`cloud.opencode.base.timeseries`)
| 类 | 说明 |
|----|------|
| `OpenTimeSeries` | 主门面：全局存储、记录、查询、聚合、预测、降采样 |
| `TimeSeries` | 核心时间序列数据结构，支持添加、查询、映射、过滤、统计操作 |
| `BoundedTimeSeries` | 大小限制和时间限制的时间序列 |
| `DataPoint` | 不可变的时间戳数据点（时间戳 + 值） |
| `Aggregation` | 移动平均、EMA、滚动统计工具 |
| `TimeSeriesStats` | 统计记录：计数、最小值、最大值、平均值、标准差、百分位 |

### 聚合 (`timeseries.aggregation`)
| 类 | 说明 |
|----|------|
| `AggregationResult` | 聚合操作结果 |
| `Aggregator` | 聚合器基础接口 |
| `AvgAggregator` | 平均值聚合器 |
| `CountAggregator` | 计数聚合器 |
| `MaxAggregator` | 最大值聚合器 |
| `MinAggregator` | 最小值聚合器 |
| `SumAggregator` | 求和聚合器 |

### 分析 (`timeseries.analysis`)
| 类 | 说明 |
|----|------|
| `CorrelationUtil` | 跨序列相关性分析 |

### 压缩 (`timeseries.compression`)
| 类 | 说明 |
|----|------|
| `CompressionUtil` | 时间序列数据压缩工具 |

### 分解 (`timeseries.decomposition`)
| 类 | 说明 |
|----|------|
| `SeasonalDecompositionUtil` | 季节性分解：趋势、季节性和残差分量 |

### 检测 (`timeseries.detection`)
| 类 | 说明 |
|----|------|
| `AnomalyDetectorUtil` | Z-Score 和统计异常检测 |
| `ChangePointDetectionUtil` | 时间序列变点检测 |

### 异常 (`timeseries.exception`)
| 类 | 说明 |
|----|------|
| `TimeSeriesErrorCode` | 时间序列异常错误码 |
| `TimeSeriesException` | 时间序列操作基础异常 |

### 预测 (`timeseries.forecast`)
| 类 | 说明 |
|----|------|
| `ForecastUtil` | SMA、EMA、线性回归、Holt 方法预测，支持置信区间 |

### 查询 (`timeseries.query`)
| 类 | 说明 |
|----|------|
| `Query` | 时间序列查询构建器 |
| `QueryLimiter` | 查询速率限制 |
| `TimeRange` | 查询时间范围定义 |

### 采样 (`timeseries.sampling`)
| 类 | 说明 |
|----|------|
| `AggregationType` | 聚合类型枚举（SUM、AVG、MIN、MAX、COUNT） |
| `DownsamplingUtil` | LTTB、M4、保峰值、百分位、阈值降采样算法 |
| `FillStrategy` | 缺失数据填充策略（零值、前向填充、线性插值） |
| `SamplerUtil` | 基于时间的降采样聚合 |

### 验证 (`timeseries.validation`)
| 类 | 说明 |
|----|------|
| `DataPointValidator` | 数据点验证（NaN、null、时间戳范围） |

### 窗口 (`timeseries.window`)
| 类 | 说明 |
|----|------|
| `Window` | 窗口基础接口 |
| `TumblingWindow` | 固定大小非重叠窗口 |
| `SlidingWindow` | 可配置滑动步长的重叠窗口 |
| `SessionWindow` | 基于活动的窗口，带间隙超时 |

## 环境要求

- Java 25+
- 无外部依赖

## 开源协议

Apache License 2.0

## 作者

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
