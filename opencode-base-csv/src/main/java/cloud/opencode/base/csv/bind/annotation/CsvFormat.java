package cloud.opencode.base.csv.bind.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * CSV Format - Specifies formatting for a CSV field binding
 * CSV格式 - 指定CSV字段绑定的格式
 *
 * <p>Controls how field values are formatted and parsed during CSV binding.
 * Use {@link #pattern()} for date/number formats and {@link #nullValue()}
 * for handling null values.</p>
 * <p>控制CSV绑定期间字段值的格式化和解析。使用 {@link #pattern()} 指定日期/数字格式，
 * 使用 {@link #nullValue()} 处理null值。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * public record Order(
 *     @CsvColumn("date") @CsvFormat(pattern = "yyyy-MM-dd") LocalDate orderDate,
 *     @CsvColumn("amount") @CsvFormat(pattern = "#,##0.00") BigDecimal amount,
 *     @CsvColumn("note") @CsvFormat(nullValue = "N/A") String note
 * ) {}
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-csv V1.0.3
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CsvFormat {

    /**
     * The date/number format pattern (empty = use default)
     * 日期/数字格式模式（空 = 使用默认值）
     *
     * @return the format pattern | 格式模式
     */
    String pattern() default "";

    /**
     * The value to use when the field is null (empty = write empty string)
     * 字段为null时使用的值（空 = 写入空字符串）
     *
     * @return the null value representation | null值表示
     */
    String nullValue() default "";
}
