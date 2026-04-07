# OpenCode Base TimeSeries

**Time series data processing library for Java 25+**

`opencode-base-timeseries` provides a comprehensive set of time series data structures, aggregation, windowing, anomaly detection, forecasting, downsampling, and analysis utilities. Zero external dependencies.

## Features

### Core Features
- **Time Series Store**: Global named series store with concurrent access support
- **Data Points**: Immutable timestamped data points with optional tags
- **Bounded Series**: Size-limited and age-limited time series with auto-eviction
- **Statistics**: Min, max, average, standard deviation, percentiles

### Advanced Features
- **Aggregation**: Sum, count, min, max, avg aggregators with rolling statistics
- **Moving Averages**: Simple moving average (SMA) and exponential moving average (EMA)
- **Windowing**: Tumbling, sliding, and session windows for stream processing
- **Anomaly Detection**: Z-Score, IQR, moving average, spike detection
- **Change Point Detection**: CUSUM, binary segmentation, mean/variance shift
- **Forecasting**: SMA, EMA, WMA, linear regression, Holt's method with confidence bounds
- **Downsampling**: LTTB, M4, peak-preserving, percentile, and threshold algorithms
- **Seasonal Decomposition**: Classical and STL decomposition with period detection
- **Correlation Analysis**: Pearson, Spearman, cross-correlation, autocorrelation, PACF
- **Compression**: Gorilla delta-of-delta + XOR compression
- **Query**: Fluent query builder with time range, filter, aggregation, and rate limiting

### V1.0.3 New Features
- **Alignment**: Align and resample series to common time grids with configurable fill strategies
- **Interpolation**: Linear, step (LOCF), and natural cubic spline interpolation
- **Rate Calculation**: Counter rate/irate/increase with automatic reset handling (Prometheus-style)
- **Gap Detection**: Detect data gaps, measure data completeness percentage
- **Streaming Stats**: Online statistics via Welford's algorithm (O(1) per update, mergeable)
- **Math Transforms**: Normalize, z-score, log, exp, scale, Bollinger bands, rolling std dev
- **Time Bucketing**: Calendar-aware aggregation (HOUR/DAY/WEEK/MONTH/QUARTER/YEAR) with time zone support
- **Lag/Lead/Shift**: Time series shifting and percentage change operations
- **Exception Hierarchy**: `TimeSeriesException` now extends `OpenException` (unified exception base)

## Quick Start

### Maven Dependency
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-timeseries</artifactId>
    <version>1.0.3</version>
</dependency>
```

### Basic Usage

```java
import cloud.opencode.base.timeseries.*;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

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

// Normalization
TimeSeries normalized = OpenTimeSeries.normalize(series);
```

### V1.0.3 Features

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

// --- Alignment & Resampling ---
TimeSeries[] aligned = AlignmentUtil.align(seriesA, seriesB,
    Duration.ofMinutes(1), FillStrategy.LINEAR);
TimeSeries regular = AlignmentUtil.resample(series,
    Duration.ofSeconds(30), FillStrategy.PREVIOUS);
TimeSeries gridded = AlignmentUtil.alignToGrid(series,
    Duration.ofMinutes(1), AggregationType.AVG);

// --- Interpolation ---
TimeSeries linear = InterpolationUtil.linear(series, Duration.ofSeconds(10));
TimeSeries stepped = InterpolationUtil.step(series, Duration.ofSeconds(10));
TimeSeries smooth = InterpolationUtil.spline(series, Duration.ofSeconds(10));
TimeSeries atPoints = InterpolationUtil.interpolateAt(series,
    Instant.parse("2026-01-01T00:05:00Z"),
    Instant.parse("2026-01-01T00:15:00Z"));

// --- Counter Rate (Prometheus-style) ---
TimeSeries perSecRate = RateUtil.rate(counter, Duration.ofMinutes(5));
TimeSeries instantRate = RateUtil.irate(counter);
TimeSeries increase = RateUtil.increase(counter, Duration.ofMinutes(5));
int resetCount = RateUtil.resets(counter);
TimeSeries nnDeriv = RateUtil.nonNegativeDerivative(counter);

// --- Gap Detection & Data Quality ---
List<Gap> gaps = GapDetector.detectGaps(series, Duration.ofMinutes(1));
List<Gap> gapsCustom = GapDetector.detectGaps(series, Duration.ofMinutes(1), 2.0);
double completeness = GapDetector.dataCompleteness(series,
    Duration.ofMinutes(1), from, to);
Gap longest = GapDetector.longestGap(series).orElse(null);

// --- Streaming Stats (O(1) per update) ---
StreamingStats ss = new StreamingStats();
ss.add(42.0);
ss.add(43.5);
double mean = ss.mean();
TimeSeriesStats snapshot = ss.snapshot();
StreamingStats other = new StreamingStats();
ss.merge(other);  // parallel merge

// --- Math Transforms ---
TimeSeries norm = MathUtil.normalize(series);
TimeSeries zs = MathUtil.zScore(series);
TimeSeries logged = MathUtil.log(series);
TimeSeries scaled = MathUtil.scale(series, 2.0);
TimeSeries offset = MathUtil.offset(series, -10.0);
TimeSeries rollStd = MathUtil.rollingStdDev(series, 20);
MathUtil.BollingerBands bb = MathUtil.bollingerBands(series, 20, 2.0);

// --- Calendar-Aware Bucketing ---
TimeSeries daily = TimeBucketUtil.bucket(series, TimeBucket.DAY,
    ZoneId.of("Asia/Shanghai"), AggregationType.AVG);
TimeSeries monthly = TimeBucketUtil.bucket(series, TimeBucket.MONTH,
    ZoneId.of("UTC"), AggregationType.SUM);
TimeSeries fiveMin = TimeBucketUtil.bucket(series,
    Duration.ofMinutes(5), Instant.EPOCH, AggregationType.MAX);

// --- Lag / Lead / Shift / PctChange ---
TimeSeries lagged = series.lag(1);
TimeSeries led = series.lead(1);
TimeSeries shifted = series.shift(Duration.ofHours(-1));
TimeSeries returns = series.pctChange(1);
```

## API Method Reference

### AlignmentUtil (`timeseries.alignment`) — V1.0.3

| Method | Description |
|--------|-------------|
| `align(TimeSeries a, TimeSeries b, Duration interval, FillStrategy fill)` | Align two series to a common regular time grid |
| `resample(TimeSeries ts, Duration interval, FillStrategy fill)` | Resample a single series to regular intervals |
| `alignToGrid(TimeSeries ts, Duration interval, AggregationType agg)` | Snap to grid boundaries and aggregate within each bucket |

### InterpolationUtil (`timeseries.interpolation`) — V1.0.3

| Method | Description |
|--------|-------------|
| `linear(TimeSeries ts, Duration interval)` | Linear interpolation at regular intervals (requires 2+ points) |
| `step(TimeSeries ts, Duration interval)` | Step interpolation / LOCF at regular intervals |
| `spline(TimeSeries ts, Duration interval)` | Natural cubic spline interpolation (requires 3+ points) |
| `interpolateAt(TimeSeries ts, Instant... targets)` | Linear interpolation/extrapolation at specific timestamps |

### RateUtil (`timeseries.rate`) — V1.0.3

| Method | Description |
|--------|-------------|
| `rate(TimeSeries counter, Duration window)` | Per-second rate between consecutive points, handling counter resets |
| `irate(TimeSeries counter)` | Instantaneous rate from the last two points |
| `increase(TimeSeries counter, Duration window)` | Total increase between consecutive points, handling resets |
| `resets(TimeSeries counter)` | Count the number of counter resets |
| `nonNegativeDerivative(TimeSeries ts)` | Derivative treating negative deltas as counter resets |

### GapDetector (`timeseries.quality`) — V1.0.3

| Method | Description |
|--------|-------------|
| `detectGaps(TimeSeries ts, Duration expectedInterval)` | Detect gaps exceeding 1.5x expected interval |
| `detectGaps(TimeSeries ts, Duration expectedInterval, double toleranceFactor)` | Detect gaps with custom tolerance factor |
| `dataCompleteness(TimeSeries ts, Duration expectedInterval, Instant from, Instant to)` | Calculate data completeness ratio [0.0, 1.0] |
| `longestGap(TimeSeries ts)` | Find the longest gap between consecutive points |
| `gapCount(TimeSeries ts, Duration expectedInterval)` | Count the number of detected gaps |

### Gap (`timeseries.quality`) — V1.0.3

| Method | Description |
|--------|-------------|
| `Gap(Instant start, Instant end)` | Immutable record (validates start <= end) |
| `length()` | Duration between start and end |

### StreamingStats (`timeseries.stats`) — V1.0.3

| Method | Description |
|--------|-------------|
| `add(double value)` | Add value (O(1), rejects NaN/Infinity) |
| `add(DataPoint point)` | Add data point's value |
| `addAll(TimeSeries ts)` | Add all points from a series |
| `count()` / `sum()` / `mean()` / `min()` / `max()` | Running statistics (O(1) read) |
| `variance()` / `stdDev()` | Sample variance/stddev (Bessel's correction) |
| `snapshot()` | Create immutable `TimeSeriesStats` record |
| `merge(StreamingStats other)` | Merge two independently computed stats (parallel Welford) |
| `reset()` | Reset to empty state |

### MathUtil (`timeseries.math`) — V1.0.3

| Method | Description |
|--------|-------------|
| `normalize(TimeSeries ts)` | Min-max normalization to [0, 1] |
| `zScore(TimeSeries ts)` | Z-score standardization (Welford single-pass) |
| `log(TimeSeries ts)` / `log10(ts)` | Natural / base-10 logarithm |
| `exp(TimeSeries ts)` | Exponential transform |
| `abs(TimeSeries ts)` | Absolute value |
| `scale(TimeSeries ts, double factor)` | Multiply all values by factor |
| `offset(TimeSeries ts, double delta)` | Add delta to all values |
| `power(TimeSeries ts, double exponent)` | Power transform |
| `rollingStdDev(TimeSeries ts, int window)` | Rolling standard deviation (sliding Welford) |
| `bollingerBands(TimeSeries ts, int window, double numStdDev)` | Bollinger Bands (upper, middle SMA, lower) |

### TimeBucketUtil (`timeseries.bucket`) — V1.0.3

| Method | Description |
|--------|-------------|
| `bucket(TimeSeries ts, TimeBucket bucket, ZoneId zone, AggregationType agg)` | Calendar-aware bucketing (SECOND to YEAR) |
| `bucket(TimeSeries ts, Duration interval, Instant origin, AggregationType agg)` | Fixed-duration bucketing with custom origin |

### TimeSeries — V1.0.3 Additions

| Method | Description |
|--------|-------------|
| `lag(int periods)` | Create lagged series (values shifted backward by n periods) |
| `lead(int periods)` | Create lead series (values shifted forward by n periods) |
| `shift(Duration offset)` | Shift all timestamps by duration |
| `pctChange(int periods)` | Percentage change over n periods |

## Class Reference

### Root Package (`cloud.opencode.base.timeseries`)
| Class | Description |
|-------|-------------|
| `OpenTimeSeries` | Main facade: global store, recording, querying, aggregation, forecasting, downsampling |
| `TimeSeries` | Core time series: add, query, range, aggregate, transform, lag/lead/shift/pctChange |
| `BoundedTimeSeries` | Size-limited and age-limited time series with auto-eviction |
| `DataPoint` | Immutable record: timestamp + value + optional tags |
| `Aggregation` | Moving average, EMA, rolling statistics utilities |
| `TimeSeriesStats` | Immutable statistics record: count, sum, average, min, max, stdDev |

### Aggregation (`timeseries.aggregation`)
| Class | Description |
|-------|-------------|
| `AggregationResult` | Sealed result: Success / Empty / Error |
| `Aggregator` | Functional interface for custom aggregations |
| `AvgAggregator` / `CountAggregator` / `MaxAggregator` / `MinAggregator` / `SumAggregator` | Singleton aggregator implementations |

### Analysis (`timeseries.analysis`)
| Class | Description |
|-------|-------------|
| `CorrelationUtil` | Pearson, Spearman, cross-correlation, autocorrelation, PACF, rolling correlation |

### Compression (`timeseries.compression`)
| Class | Description |
|-------|-------------|
| `CompressionUtil` | Gorilla delta-delta + XOR compression/decompression |

### Decomposition (`timeseries.decomposition`)
| Class | Description |
|-------|-------------|
| `SeasonalDecompositionUtil` | Classical and STL decomposition with automatic period detection |

### Detection (`timeseries.detection`)
| Class | Description |
|-------|-------------|
| `AnomalyDetectorUtil` | Z-Score, IQR, moving average, spike, out-of-range detection |
| `ChangePointDetectionUtil` | CUSUM, binary segmentation, mean/variance shift detection |

### Exception (`timeseries.exception`)
| Class | Description |
|-------|-------------|
| `TimeSeriesErrorCode` | Categorized error codes: TS-1xxx (data) to TS-6xxx (rate/quality) |
| `TimeSeriesException` | Extends `OpenException` with structured error codes |

### Forecast (`timeseries.forecast`)
| Class | Description |
|-------|-------------|
| `ForecastUtil` | SMA, WMA, EMA, linear regression, Holt's method, drift, naive, seasonal naive |

### Query (`timeseries.query`)
| Class | Description |
|-------|-------------|
| `Query` | Fluent query builder with range, filter, aggregate, groupBy, limit |
| `QueryLimiter` | Thread-safe query range and result size limits (AtomicInteger) |
| `TimeRange` | Immutable time range record with factory methods |

### Sampling (`timeseries.sampling`)
| Class | Description |
|-------|-------------|
| `AggregationType` | Enum: SUM, AVG, MIN, MAX, FIRST, LAST, COUNT |
| `DownsamplingUtil` | LTTB, M4, peak-preserving, percentile, threshold algorithms |
| `FillStrategy` | Enum: ZERO, PREVIOUS, LINEAR, NAN, NEXT, AVERAGE |
| `SamplerUtil` | Time-based downsample, fill gaps, resample, random/systematic sample |

### Validation (`timeseries.validation`)
| Class | Description |
|-------|-------------|
| `DataPointValidator` | Validate timestamp range [2000-2100], finite values, NaN/Infinity rejection |

### Alignment (`timeseries.alignment`) — V1.0.3
| Class | Description |
|-------|-------------|
| `AlignmentUtil` | Align, resample, and snap-to-grid with fill strategies |

### Interpolation (`timeseries.interpolation`) — V1.0.3
| Class | Description |
|-------|-------------|
| `InterpolationUtil` | Linear, step (LOCF), natural cubic spline interpolation |

### Rate (`timeseries.rate`) — V1.0.3
| Class | Description |
|-------|-------------|
| `RateUtil` | Counter rate, irate, increase, resets, non-negative derivative |

### Quality (`timeseries.quality`) — V1.0.3
| Class | Description |
|-------|-------------|
| `Gap` | Immutable gap record with start/end validation |
| `GapDetector` | Gap detection, data completeness, longest gap |

### Stats (`timeseries.stats`) — V1.0.3
| Class | Description |
|-------|-------------|
| `StreamingStats` | Online Welford statistics with parallel merge support |

### Math (`timeseries.math`) — V1.0.3
| Class | Description |
|-------|-------------|
| `MathUtil` | Normalize, z-score, log/exp/abs/scale/offset/power, rolling std dev, Bollinger bands |

### Bucket (`timeseries.bucket`) — V1.0.3
| Class | Description |
|-------|-------------|
| `TimeBucket` | Enum: SECOND, MINUTE, HOUR, DAY, WEEK, MONTH, QUARTER, YEAR |
| `TimeBucketUtil` | Calendar-aware and fixed-duration bucketing with aggregation |

### Window (`timeseries.window`)
| Class | Description |
|-------|-------------|
| `Window` | Base window interface |
| `TumblingWindow` | Fixed-size non-overlapping windows |
| `SlidingWindow` | Overlapping windows with configurable slide |
| `SessionWindow` | Activity-based windows with gap timeout (thread-safe) |

## Requirements

- Java 25+
- No external dependencies

## License

Apache License 2.0

## Author

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
