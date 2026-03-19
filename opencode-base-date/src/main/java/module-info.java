/**
 * OpenCode Base Date Module
 * OpenCode 基础日期模块
 *
 * <p>Provides comprehensive date and time utilities based on JDK 25 java.time API,
 * including cron expressions, timezone handling, holiday calendars, and lunar calendar support.</p>
 * <p>提供基于 JDK 25 java.time API 的全面日期时间工具，包括 Cron 表达式、时区处理、节假日日历和农历支持。</p>
 *
 * <p><strong>Key Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Cron Expression Parsing - Cron 表达式解析</li>
 *   <li>Date Range &amp; Interval - 日期范围与区间</li>
 *   <li>Holiday Calendar - 节假日日历</li>
 *   <li>Lunar Calendar - 农历日历</li>
 *   <li>Timezone Utilities - 时区工具</li>
 *   <li>Date Adjusters - 日期调整器</li>
 *   <li>Flexible Formatters - 灵活格式化</li>
 * </ul>
 *
 * @author Leon Soo
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
module cloud.opencode.base.date {

    // Export public API packages
    exports cloud.opencode.base.date;
    exports cloud.opencode.base.date.adjuster;
    exports cloud.opencode.base.date.between;
    exports cloud.opencode.base.date.cron;
    exports cloud.opencode.base.date.exception;
    exports cloud.opencode.base.date.extra;
    exports cloud.opencode.base.date.formatter;
    exports cloud.opencode.base.date.holiday;
    exports cloud.opencode.base.date.lunar;
    exports cloud.opencode.base.date.range;
    exports cloud.opencode.base.date.timezone;
}
