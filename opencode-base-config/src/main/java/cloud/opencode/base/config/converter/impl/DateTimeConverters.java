package cloud.opencode.base.config.converter.impl;

import cloud.opencode.base.config.converter.ConfigConverter;
import java.time.*;

/**
 * Date and Time Converters Collection
 * 日期和时间转换器集合
 *
 * <p>Provides converters for java.time types using ISO-8601 format.</p>
 * <p>提供使用ISO-8601格式的java.time类型转换器。</p>
 *
 * <p><strong>Included Converters | 包含的转换器:</strong></p>
 * <ul>
 *   <li>{@link LocalDateConverter} - "2024-01-15"</li>
 *   <li>{@link LocalTimeConverter} - "10:30:00"</li>
 *   <li>{@link LocalDateTimeConverter} - "2024-01-15T10:30:00"</li>
 *   <li>{@link InstantConverter} - "2024-01-15T10:30:00Z"</li>
 *   <li>{@link ZonedDateTimeConverter} - "2024-01-15T10:30:00+08:00[Asia/Shanghai]"</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * var dateConverter = new DateTimeConverters.LocalDateConverter();
 * LocalDate date = dateConverter.convert("2024-01-15");
 *
 * var instantConverter = new DateTimeConverters.InstantConverter();
 * Instant timestamp = instantConverter.convert("2024-01-15T10:30:00Z");
 * }</pre>
 *
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Core DateTimeConverters functionality - DateTimeConverters核心功能</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) per conversion - 每次转换 O(1)</li>
 *   <li>Space complexity: O(1) - O(1)</li>
 * </ul>
 *
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
public class DateTimeConverters {
    
    public static class LocalDateConverter implements ConfigConverter<LocalDate> {
        @Override
        public LocalDate convert(String value) {
            return LocalDate.parse(value);
        }
        
        @Override
        public Class<LocalDate> getType() {
            return LocalDate.class;
        }
    }
    
    public static class LocalTimeConverter implements ConfigConverter<LocalTime> {
        @Override
        public LocalTime convert(String value) {
            return LocalTime.parse(value);
        }
        
        @Override
        public Class<LocalTime> getType() {
            return LocalTime.class;
        }
    }
    
    public static class LocalDateTimeConverter implements ConfigConverter<LocalDateTime> {
        @Override
        public LocalDateTime convert(String value) {
            return LocalDateTime.parse(value);
        }
        
        @Override
        public Class<LocalDateTime> getType() {
            return LocalDateTime.class;
        }
    }
    
    public static class InstantConverter implements ConfigConverter<Instant> {
        @Override
        public Instant convert(String value) {
            return Instant.parse(value);
        }
        
        @Override
        public Class<Instant> getType() {
            return Instant.class;
        }
    }
    
    public static class ZonedDateTimeConverter implements ConfigConverter<ZonedDateTime> {
        @Override
        public ZonedDateTime convert(String value) {
            return ZonedDateTime.parse(value);
        }
        
        @Override
        public Class<ZonedDateTime> getType() {
            return ZonedDateTime.class;
        }
    }
}
