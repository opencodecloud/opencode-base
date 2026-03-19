/**
 * OpenCode Base Cron Module
 * Cron 表达式模块
 *
 * <p>Provides cron expression parsing, fluent builder, validation,
 * scheduling (next/previous execution), and human-readable description.</p>
 * <p>提供 Cron 表达式解析、流式构建器、校验、
 * 调度（下次/上次执行）和人类可读描述。</p>
 *
 * <p><strong>Supported syntax | 支持的语法:</strong></p>
 * <ul>
 *   <li>5/6-field cron expressions</li>
 *   <li>Name aliases: MON-FRI, JAN-DEC</li>
 *   <li>Special characters: L, W, #, ?</li>
 *   <li>Macros: @yearly, @monthly, @weekly, @daily, @hourly</li>
 *   <li>Range wrap-around: 22-2 (hours)</li>
 *   <li>OR semantics for day-of-month/day-of-week</li>
 * </ul>
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-cron V1.0.0
 */
module cloud.opencode.base.cron {
    exports cloud.opencode.base.cron;
    exports cloud.opencode.base.cron.exception;
}
