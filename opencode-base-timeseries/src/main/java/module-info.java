/**
 * OpenCode Base TimeSeries Module
 * OpenCode 基础时间序列模块
 *
 * <p>Provides time series data processing capabilities including
 * data storage, aggregation, windowing, sampling, and anomaly detection.</p>
 * <p>提供时间序列数据处理能力，包括数据存储、聚合、窗口、采样和异常检测。</p>
 *
 * <p><strong>Key Features | 主要功能:</strong></p>
 * <ul>
 *   <li>TimeSeries - ConcurrentSkipListMap-based storage with stats - 基于ConcurrentSkipListMap的存储</li>
 *   <li>BoundedTimeSeries - Size/age limited series - 有界时间序列</li>
 *   <li>Window Functions - Tumbling, Sliding, Session windows - 窗口函数</li>
 *   <li>Aggregation - Sum, Avg, Min, Max, Count aggregators - 聚合器</li>
 *   <li>Sampling - Downsample, Upsample, Gap filling - 采样</li>
 *   <li>Anomaly Detection - Z-Score, IQR, Moving Average, Spikes - 异常检测</li>
 *   <li>Query - Time range queries with limits - 查询</li>
 * </ul>
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
module cloud.opencode.base.timeseries {
    // Required modules
    requires transitive cloud.opencode.base.core;

    // Export public API packages
    exports cloud.opencode.base.timeseries;
    exports cloud.opencode.base.timeseries.aggregation;
    exports cloud.opencode.base.timeseries.detection;
    exports cloud.opencode.base.timeseries.exception;
    exports cloud.opencode.base.timeseries.forecast;
    exports cloud.opencode.base.timeseries.query;
    exports cloud.opencode.base.timeseries.sampling;
    exports cloud.opencode.base.timeseries.validation;
    exports cloud.opencode.base.timeseries.window;
}
