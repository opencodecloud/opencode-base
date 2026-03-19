package cloud.opencode.base.xml.bind.adapter;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Date Adapter - Adapters for date/time types
 * 日期适配器 - 日期/时间类型的适配器
 *
 * <p>This class provides XML adapters for common date/time types.</p>
 * <p>此类为常见的日期/时间类型提供 XML 适配器。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>LocalDate, LocalDateTime, LocalTime adapters - LocalDate、LocalDateTime、LocalTime 适配器</li>
 *   <li>Instant and ZonedDateTime adapters - Instant 和 ZonedDateTime 适配器</li>
 *   <li>Legacy java.util.Date adapter - 传统 java.util.Date 适配器</li>
 *   <li>Duration and Period adapters - Duration 和 Period 适配器</li>
 *   <li>Customizable DateTimeFormatter support - 可自定义 DateTimeFormatter 支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Register a date adapter with the binder
 * XmlBinder binder = XmlBinder.create()
 *     .registerAdapter(new DateAdapter.LocalDateAdapter());
 *
 * // With custom format
 * XmlBinder binder = XmlBinder.create()
 *     .registerAdapter(new DateAdapter.LocalDateAdapter(
 *         DateTimeFormatter.ofPattern("dd/MM/yyyy")));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (adapters are stateless or use immutable formatters) - 线程安全: 是（适配器无状态或使用不可变格式化器）</li>
 *   <li>Null-safe: Yes (null inputs return null) - 空值安全: 是（空值输入返回 null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
public final class DateAdapter {

    private DateAdapter() {
        // Utility class
    }

    /**
     * Adapter for LocalDate.
     * LocalDate 适配器。
     */
    public static class LocalDateAdapter implements XmlAdapter<String, LocalDate> {
        private final DateTimeFormatter formatter;

        public LocalDateAdapter() {
            this(DateTimeFormatter.ISO_LOCAL_DATE);
        }

        public LocalDateAdapter(DateTimeFormatter formatter) {
            this.formatter = formatter;
        }

        @Override
        public LocalDate unmarshal(String value) {
            return value != null ? LocalDate.parse(value, formatter) : null;
        }

        @Override
        public String marshal(LocalDate value) {
            return value != null ? value.format(formatter) : null;
        }
    }

    /**
     * Adapter for LocalDateTime.
     * LocalDateTime 适配器。
     */
    public static class LocalDateTimeAdapter implements XmlAdapter<String, LocalDateTime> {
        private final DateTimeFormatter formatter;

        public LocalDateTimeAdapter() {
            this(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

        public LocalDateTimeAdapter(DateTimeFormatter formatter) {
            this.formatter = formatter;
        }

        @Override
        public LocalDateTime unmarshal(String value) {
            return value != null ? LocalDateTime.parse(value, formatter) : null;
        }

        @Override
        public String marshal(LocalDateTime value) {
            return value != null ? value.format(formatter) : null;
        }
    }

    /**
     * Adapter for LocalTime.
     * LocalTime 适配器。
     */
    public static class LocalTimeAdapter implements XmlAdapter<String, LocalTime> {
        private final DateTimeFormatter formatter;

        public LocalTimeAdapter() {
            this(DateTimeFormatter.ISO_LOCAL_TIME);
        }

        public LocalTimeAdapter(DateTimeFormatter formatter) {
            this.formatter = formatter;
        }

        @Override
        public LocalTime unmarshal(String value) {
            return value != null ? LocalTime.parse(value, formatter) : null;
        }

        @Override
        public String marshal(LocalTime value) {
            return value != null ? value.format(formatter) : null;
        }
    }

    /**
     * Adapter for Instant.
     * Instant 适配器。
     */
    public static class InstantAdapter implements XmlAdapter<String, Instant> {
        @Override
        public Instant unmarshal(String value) {
            return value != null ? Instant.parse(value) : null;
        }

        @Override
        public String marshal(Instant value) {
            return value != null ? value.toString() : null;
        }
    }

    /**
     * Adapter for ZonedDateTime.
     * ZonedDateTime 适配器。
     */
    public static class ZonedDateTimeAdapter implements XmlAdapter<String, ZonedDateTime> {
        private final DateTimeFormatter formatter;

        public ZonedDateTimeAdapter() {
            this(DateTimeFormatter.ISO_ZONED_DATE_TIME);
        }

        public ZonedDateTimeAdapter(DateTimeFormatter formatter) {
            this.formatter = formatter;
        }

        @Override
        public ZonedDateTime unmarshal(String value) {
            return value != null ? ZonedDateTime.parse(value, formatter) : null;
        }

        @Override
        public String marshal(ZonedDateTime value) {
            return value != null ? value.format(formatter) : null;
        }
    }

    /**
     * Adapter for legacy Date.
     * 传统 Date 适配器。
     */
    public static class LegacyDateAdapter implements XmlAdapter<String, Date> {
        @Override
        public Date unmarshal(String value) {
            if (value == null) return null;
            Instant instant = Instant.parse(value);
            return Date.from(instant);
        }

        @Override
        public String marshal(Date value) {
            return value != null ? value.toInstant().toString() : null;
        }
    }

    /**
     * Adapter for Duration.
     * Duration 适配器。
     */
    public static class DurationAdapter implements XmlAdapter<String, Duration> {
        @Override
        public Duration unmarshal(String value) {
            return value != null ? Duration.parse(value) : null;
        }

        @Override
        public String marshal(Duration value) {
            return value != null ? value.toString() : null;
        }
    }

    /**
     * Adapter for Period.
     * Period 适配器。
     */
    public static class PeriodAdapter implements XmlAdapter<String, Period> {
        @Override
        public Period unmarshal(String value) {
            return value != null ? Period.parse(value) : null;
        }

        @Override
        public String marshal(Period value) {
            return value != null ? value.toString() : null;
        }
    }
}
