/*
 * Copyright 2025 OpenCode Cloud Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Performance Logging - Timing and Slow Operation Detection
 * 性能日志 - 计时和慢操作检测
 *
 * <p>This package provides utilities for measuring and logging operation
 * durations, detecting slow operations, and performance analysis.</p>
 * <p>本包提供用于测量和记录操作持续时间、检测慢操作和性能分析的工具。</p>
 *
 * <h2>Key Classes | 核心类</h2>
 * <ul>
 *   <li>{@link cloud.opencode.base.log.perf.StopWatch} - High-precision timer</li>
 *   <li>{@link cloud.opencode.base.log.perf.PerfLog} - Performance logging utility</li>
 *   <li>{@link cloud.opencode.base.log.perf.SlowOperationConfig} - Slow operation configuration</li>
 * </ul>
 *
 * <h2>StopWatch Usage | StopWatch 使用</h2>
 * <pre>{@code
 * // Basic timing
 * StopWatch watch = StopWatch.start("database-query");
 * executeQuery();
 * watch.stop();
 * log.info("Query took {}ms", watch.getElapsedMillis());
 *
 * // AutoCloseable with automatic logging
 * try (StopWatch watch = StopWatch.start("processOrder")) {
 *     processOrder();
 * } // Automatically logs duration on close
 *
 * // Threshold-based logging
 * StopWatch watch = StopWatch.start("api-call");
 * callExternalApi();
 * watch.stopAndLog(500); // Only warn if > 500ms
 * }</pre>
 *
 * <h2>PerfLog Convenience Methods | PerfLog 便捷方法</h2>
 * <pre>{@code
 * // Timed execution
 * PerfLog.timed("operation", () -> {
 *     performOperation();
 * });
 *
 * // Timed with return value
 * Result result = PerfLog.timed("computation", () -> {
 *     return computeResult();
 * });
 *
 * // With threshold
 * PerfLog.timedWithThreshold("slowOp", 1000, () -> {
 *     performSlowOperation();
 * });
 * }</pre>
 *
 * <h2>Slow Operation Detection | 慢操作检测</h2>
 * <pre>{@code
 * SlowOperationConfig config = SlowOperationConfig.builder()
 *     .thresholdMillis(500)
 *     .logLevel(LogLevel.WARN)
 *     .includeStackTrace(true)
 *     .build();
 *
 * long elapsed = 750;
 * if (config.isSlow(elapsed)) {
 *     log.warn("Slow operation detected: {}ms", elapsed);
 * }
 * }</pre>
 *
 * @author OpenCode Cloud Group
 * @see cloud.opencode.base.log.perf.StopWatch
 * @see cloud.opencode.base.log.perf.PerfLog
 * @see cloud.opencode.base.log.perf.SlowOperationConfig
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
package cloud.opencode.base.log.perf;
