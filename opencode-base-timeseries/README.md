# OpenCode Base TimeSeries

**Time series data processing library for Java 25+**

`opencode-base-timeseries` provides a comprehensive set of time series data structures, aggregation, windowing, anomaly detection, forecasting, downsampling, and analysis utilities.

## Features

### Core Features
- **Time Series Store**: Global named series store with concurrent access support
- **Data Points**: Timestamped data point creation and manipulation
- **Bounded Series**: Size-limited and age-limited time series
- **Statistics**: Min, max, average, standard deviation, and comprehensive stats

### Advanced Features
- **Aggregation**: Sum, count, min, max, avg aggregators with rolling statistics
- **Moving Averages**: Simple moving average (SMA) and exponential moving average (EMA)
- **Windowing**: Tumbling, sliding, and session windows for stream processing
- **Anomaly Detection**: Z-Score based anomaly detection
- **Change Point Detection**: Detect significant changes in time series trends
- **Forecasting**: SMA, EMA, linear regression, Holt's double exponential smoothing with confidence bounds
- **Downsampling**: LTTB, M4, peak-preserving, percentile, and threshold algorithms
- **Seasonal Decomposition**: Trend, seasonal, and residual component extraction
- **Correlation Analysis**: Cross-series correlation computation
- **Compression**: Time series data compression utilities
- **Query**: Time range queries with rate limiting

## Quick Start

### Maven Dependency
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-timeseries</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Basic Usage

```java
import cloud.opencode.base.timeseries.*;
import java.time.Duration;
import java.time.Instant;

// Record data points to a named series
OpenTimeSeries.record("cpu.usage", 75.5);
OpenTimeSeries.record("cpu.usage", Instant.now(), 82.3);

// Query recent data
List<DataPoint> points = OpenTimeSeries.query("cpu.usage", Duration.ofHours(1));

// Get statistics
TimeSeriesStats stats = OpenTimeSeries.stats("cpu.usage");

// Create standalone series
TimeSeries series = OpenTimeSeries.create("temperature");
series.addNow(22.5);
series.addNow(23.1);

// Downsampling
TimeSeries downsampled = OpenTimeSeries.downsample(series, Duration.ofMinutes(5));

// Moving average
TimeSeries smoothed = OpenTimeSeries.movingAverage(series, 10);

// Anomaly detection
List<DataPoint> anomalies = OpenTimeSeries.detectAnomalies(series, 2.0);

// Forecasting
TimeSeries forecast = OpenTimeSeries.linearForecast(series, 10);
TimeSeries holtForecast = OpenTimeSeries.holtForecast(series, 0.3, 0.1, 10);

// Normalization
TimeSeries normalized = OpenTimeSeries.normalize(series);
```

## Class Reference

### Root Package (`cloud.opencode.base.timeseries`)
| Class | Description |
|-------|-------------|
| `OpenTimeSeries` | Main facade: global store, recording, querying, aggregation, forecasting, downsampling |
| `TimeSeries` | Core time series data structure with add, query, map, filter, stats operations |
| `BoundedTimeSeries` | Size-limited and age-limited time series |
| `DataPoint` | Immutable timestamped data point (timestamp + value) |
| `Aggregation` | Moving average, EMA, rolling statistics utilities |
| `TimeSeriesStats` | Statistics record: count, min, max, avg, stddev, percentiles |

### Aggregation (`timeseries.aggregation`)
| Class | Description |
|-------|-------------|
| `AggregationResult` | Result of an aggregation operation |
| `Aggregator` | Base aggregator interface |
| `AvgAggregator` | Average value aggregator |
| `CountAggregator` | Count aggregator |
| `MaxAggregator` | Maximum value aggregator |
| `MinAggregator` | Minimum value aggregator |
| `SumAggregator` | Sum aggregator |

### Analysis (`timeseries.analysis`)
| Class | Description |
|-------|-------------|
| `CorrelationUtil` | Cross-series correlation analysis |

### Compression (`timeseries.compression`)
| Class | Description |
|-------|-------------|
| `CompressionUtil` | Time series data compression utilities |

### Decomposition (`timeseries.decomposition`)
| Class | Description |
|-------|-------------|
| `SeasonalDecompositionUtil` | Seasonal decomposition into trend, seasonal, and residual components |

### Detection (`timeseries.detection`)
| Class | Description |
|-------|-------------|
| `AnomalyDetectorUtil` | Z-Score and statistical anomaly detection |
| `ChangePointDetectionUtil` | Change point detection in time series data |

### Exception (`timeseries.exception`)
| Class | Description |
|-------|-------------|
| `TimeSeriesErrorCode` | Error codes for time series exceptions |
| `TimeSeriesException` | Base exception for time series operations |

### Forecast (`timeseries.forecast`)
| Class | Description |
|-------|-------------|
| `ForecastUtil` | SMA, EMA, linear regression, Holt's method forecasting with confidence bounds |

### Query (`timeseries.query`)
| Class | Description |
|-------|-------------|
| `Query` | Time series query builder |
| `QueryLimiter` | Query rate limiting |
| `TimeRange` | Time range definition for queries |

### Sampling (`timeseries.sampling`)
| Class | Description |
|-------|-------------|
| `AggregationType` | Enum of aggregation types (SUM, AVG, MIN, MAX, COUNT) |
| `DownsamplingUtil` | LTTB, M4, peak-preserving, percentile, threshold downsampling algorithms |
| `FillStrategy` | Missing data fill strategies (zero, forward, linear interpolation) |
| `SamplerUtil` | Time-based downsampling with aggregation |

### Validation (`timeseries.validation`)
| Class | Description |
|-------|-------------|
| `DataPointValidator` | Data point validation (NaN, null, timestamp range) |

### Window (`timeseries.window`)
| Class | Description |
|-------|-------------|
| `Window` | Base window interface |
| `TumblingWindow` | Fixed-size non-overlapping window |
| `SlidingWindow` | Overlapping window with configurable slide |
| `SessionWindow` | Activity-based window with gap timeout |

## Requirements

- Java 25+
- No external dependencies

## License

Apache License 2.0

## Author

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
