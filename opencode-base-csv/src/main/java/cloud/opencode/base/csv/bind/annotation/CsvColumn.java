package cloud.opencode.base.csv.bind.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * CSV Column - Maps a Java field to a CSV column
 * CSV列 - 将Java字段映射到CSV列
 *
 * <p>Annotate record components or class fields to specify which CSV column
 * they correspond to. If neither {@link #value()} nor {@link #index()} is set,
 * the Java field name is used as the header name.</p>
 * <p>标注记录组件或类字段以指定它们对应的CSV列。如果既未设置 {@link #value()}
 * 也未设置 {@link #index()}，则使用Java字段名作为标题名。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * public record Employee(
 *     @CsvColumn("Name") String name,
 *     @CsvColumn(index = 1) int age,
 *     @CsvColumn(value = "Role", required = true) String role
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
public @interface CsvColumn {

    /**
     * The CSV header name to bind to (empty = use field name)
     * 要绑定的CSV标题名（空 = 使用字段名）
     *
     * @return the header name | 标题名
     */
    String value() default "";

    /**
     * The 0-based column index (-1 = use name matching)
     * 0起始列索引（-1 = 使用名称匹配）
     *
     * @return the column index | 列索引
     */
    int index() default -1;

    /**
     * Whether this column is required (binding fails if missing)
     * 此列是否为必需（缺少时绑定失败）
     *
     * @return true if required | 如果必需返回true
     */
    boolean required() default false;
}
