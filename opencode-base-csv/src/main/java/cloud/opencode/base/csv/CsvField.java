package cloud.opencode.base.csv;

import cloud.opencode.base.csv.exception.OpenCsvException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

/**
 * CSV Field - Utility class for field value conversion
 * CSV字段 - 字段值转换工具类
 *
 * <p>Provides type-safe conversion methods for CSV field values. All conversion
 * failures throw {@link OpenCsvException} with clear diagnostic messages.</p>
 * <p>提供CSV字段值的类型安全转换方法。所有转换失败都会抛出带有清晰诊断消息的
 * {@link OpenCsvException}。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Type conversion: String, int, long, double, boolean, BigDecimal - 类型转换</li>
 *   <li>Date/time parsing with DateTimeFormatter - 使用DateTimeFormatter解析日期时间</li>
 *   <li>Null and blank detection - 空值和空白检测</li>
 *   <li>Clear error messages for conversion failures - 转换失败的清晰错误消息</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * int age = CsvField.asInt("30");
 * BigDecimal amount = CsvField.asBigDecimal("1234.56");
 * LocalDate date = CsvField.asLocalDate("2024-01-15", DateTimeFormatter.ISO_LOCAL_DATE);
 * boolean blank = CsvField.isBlank("");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-csv V1.0.3
 */
public final class CsvField {

    private CsvField() {
        // Utility class, no instances
    }

    /**
     * Returns the field value as a string (identity conversion)
     * 将字段值作为字符串返回（恒等转换）
     *
     * @param field the field value | 字段值
     * @return the string value | 字符串值
     */
    public static String asString(String field) {
        return field;
    }

    /**
     * Converts a field value to int
     * 将字段值转换为int
     *
     * @param field the field value | 字段值
     * @return the int value | int值
     * @throws OpenCsvException if the value cannot be converted | 如果值无法转换
     */
    public static int asInt(String field) {
        try {
            return Integer.parseInt(requireNonBlank(field, "int").trim());
        } catch (NumberFormatException e) {
            throw new OpenCsvException("Cannot convert field to int: '" + field + "'", e);
        }
    }

    /**
     * Converts a field value to long
     * 将字段值转换为long
     *
     * @param field the field value | 字段值
     * @return the long value | long值
     * @throws OpenCsvException if the value cannot be converted | 如果值无法转换
     */
    public static long asLong(String field) {
        try {
            return Long.parseLong(requireNonBlank(field, "long").trim());
        } catch (NumberFormatException e) {
            throw new OpenCsvException("Cannot convert field to long: '" + field + "'", e);
        }
    }

    /**
     * Converts a field value to double
     * 将字段值转换为double
     *
     * @param field the field value | 字段值
     * @return the double value | double值
     * @throws OpenCsvException if the value cannot be converted | 如果值无法转换
     */
    public static double asDouble(String field) {
        try {
            return Double.parseDouble(requireNonBlank(field, "double").trim());
        } catch (NumberFormatException e) {
            throw new OpenCsvException("Cannot convert field to double: '" + field + "'", e);
        }
    }

    /**
     * Converts a field value to boolean
     * 将字段值转换为boolean
     *
     * <p>Accepts "true"/"false" (case-insensitive), "1"/"0", "yes"/"no".</p>
     * <p>接受 "true"/"false"（不区分大小写）、"1"/"0"、"yes"/"no"。</p>
     *
     * @param field the field value | 字段值
     * @return the boolean value | boolean值
     * @throws OpenCsvException if the value cannot be converted | 如果值无法转换
     */
    public static boolean asBoolean(String field) {
        String trimmed = requireNonBlank(field, "boolean").trim().toLowerCase();
        return switch (trimmed) {
            case "true", "1", "yes" -> true;
            case "false", "0", "no" -> false;
            default -> throw new OpenCsvException("Cannot convert field to boolean: '" + field + "'");
        };
    }

    /**
     * Converts a field value to BigDecimal
     * 将字段值转换为BigDecimal
     *
     * @param field the field value | 字段值
     * @return the BigDecimal value | BigDecimal值
     * @throws OpenCsvException if the value cannot be converted | 如果值无法转换
     */
    public static BigDecimal asBigDecimal(String field) {
        try {
            return new BigDecimal(requireNonBlank(field, "BigDecimal").trim());
        } catch (NumberFormatException e) {
            throw new OpenCsvException("Cannot convert field to BigDecimal: '" + field + "'", e);
        }
    }

    /**
     * Converts a field value to LocalDate using the given formatter
     * 使用给定的格式化器将字段值转换为LocalDate
     *
     * @param field     the field value | 字段值
     * @param formatter the date formatter | 日期格式化器
     * @return the LocalDate value | LocalDate值
     * @throws OpenCsvException if the value cannot be parsed | 如果值无法解析
     */
    public static LocalDate asLocalDate(String field, DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter must not be null");
        try {
            return LocalDate.parse(requireNonBlank(field, "LocalDate").trim(), formatter);
        } catch (DateTimeParseException e) {
            throw new OpenCsvException("Cannot convert field to LocalDate: '" + field + "'", e);
        }
    }

    /**
     * Converts a field value to LocalDateTime using the given formatter
     * 使用给定的格式化器将字段值转换为LocalDateTime
     *
     * @param field     the field value | 字段值
     * @param formatter the date-time formatter | 日期时间格式化器
     * @return the LocalDateTime value | LocalDateTime值
     * @throws OpenCsvException if the value cannot be parsed | 如果值无法解析
     */
    public static LocalDateTime asLocalDateTime(String field, DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter must not be null");
        try {
            return LocalDateTime.parse(requireNonBlank(field, "LocalDateTime").trim(), formatter);
        } catch (DateTimeParseException e) {
            throw new OpenCsvException("Cannot convert field to LocalDateTime: '" + field + "'", e);
        }
    }

    /**
     * Checks if a field value is blank (null, empty, or whitespace only)
     * 检查字段值是否为空白（null、空或仅空格）
     *
     * @param field the field value | 字段值
     * @return true if blank | 如果为空白返回true
     */
    public static boolean isBlank(String field) {
        return field == null || field.isBlank();
    }

    private static String requireNonBlank(String field, String targetType) {
        if (field == null || field.isBlank()) {
            throw new OpenCsvException("Cannot convert blank field to " + targetType);
        }
        return field;
    }
}
