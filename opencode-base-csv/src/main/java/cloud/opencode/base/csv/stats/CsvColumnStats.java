package cloud.opencode.base.csv.stats;

import java.math.BigDecimal;

/**
 * CSV Column Statistics - Immutable summary of statistics for a single CSV column
 * CSV列统计 - 单个CSV列统计摘要的不可变记录
 *
 * <p>Contains count, distinct count, and numeric aggregate values for a column.
 * Numeric fields (sum, avg, min, max) are null when the column contains no
 * parseable numeric values.</p>
 * <p>包含列的计数、去重计数和数值聚合值。
 * 当列不包含可解析的数值时，数值字段（sum、avg、min、max）为null。</p>
 *
 * @param column        the column name | 列名
 * @param totalCount    total number of rows | 总行数
 * @param nonBlankCount number of non-blank values | 非空白值数量
 * @param distinctCount number of distinct non-null values | 去重非null值数量
 * @param sum           sum of numeric values, null if none | 数值之和，无数值则为null
 * @param avg           average of numeric values, null if none | 数值平均值，无数值则为null
 * @param min           minimum numeric value, null if none | 最小数值，无数值则为null
 * @param max           maximum numeric value, null if none | 最大数值，无数值则为null
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-csv V1.0.3
 */
public record CsvColumnStats(
        String column,
        long totalCount,
        long nonBlankCount,
        long distinctCount,
        BigDecimal sum,
        BigDecimal avg,
        BigDecimal min,
        BigDecimal max
) {
}
