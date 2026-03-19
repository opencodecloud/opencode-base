/**
 * OpenCode Base Log Module
 * OpenCode 基础日志模块
 *
 * <p>Provides structured logging utilities based on JDK 25, including MDC context management,
 * audit logging, performance logging, log markers, and SPI-based log provider integration.</p>
 * <p>提供基于 JDK 25 的结构化日志工具，包括 MDC 上下文管理、审计日志、性能日志、
 * 日志标记和基于 SPI 的日志提供者集成。</p>
 *
 * <p><strong>Key Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Structured Logging - 结构化日志</li>
 *   <li>MDC Context Propagation - MDC 上下文传播</li>
 *   <li>Audit Log - 审计日志</li>
 *   <li>Performance Logging - 性能日志</li>
 *   <li>Log Enhancement (Correlation ID, Tracing) - 日志增强</li>
 *   <li>Log Markers - 日志标记</li>
 *   <li>SPI Log Provider - SPI 日志提供者</li>
 * </ul>
 *
 * @author Leon Soo
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
module cloud.opencode.base.log {
    // Required modules
    requires transitive cloud.opencode.base.core;

    // Export public API packages
    exports cloud.opencode.base.log;
    exports cloud.opencode.base.log.audit;
    exports cloud.opencode.base.log.context;
    exports cloud.opencode.base.log.enhance;
    exports cloud.opencode.base.log.exception;
    exports cloud.opencode.base.log.marker;
    exports cloud.opencode.base.log.perf;
    exports cloud.opencode.base.log.spi;

    // SPI: Log provider extension point
    uses cloud.opencode.base.log.spi.LogProvider;
}
