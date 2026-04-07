/**
 * Log Filter Package - Provides Extensible Log Event Filtering
 * 日志过滤器包 - 提供可扩展的日志事件过滤
 *
 * <p>This package contains the log filter framework, including the core
 * {@link cloud.opencode.base.log.filter.LogFilter} interface, the
 * {@link cloud.opencode.base.log.filter.LogFilterChain} for composing filters,
 * and built-in filter implementations.</p>
 * <p>此包包含日志过滤器框架，包括核心
 * {@link cloud.opencode.base.log.filter.LogFilter} 接口、用于组合过滤器的
 * {@link cloud.opencode.base.log.filter.LogFilterChain}，以及内置过滤器实现。</p>
 *
 * <p><strong>Built-in Filters | 内置过滤器:</strong></p>
 * <ul>
 *   <li>{@link cloud.opencode.base.log.filter.LevelFilter} - Level threshold filter - 级别阈值过滤器</li>
 *   <li>{@link cloud.opencode.base.log.filter.MarkerFilter} - Marker-based filter - 基于标记的过滤器</li>
 *   <li>{@link cloud.opencode.base.log.filter.ThrottleFilter} - Rate-limiting filter - 速率限制过滤器</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.3
 */
package cloud.opencode.base.log.filter;
