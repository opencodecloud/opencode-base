package cloud.opencode.base.lunar.calendar;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Solar Term Information - Combines a solar term with its date
 * 节气信息 - 节气与日期的组合
 *
 * <p>An immutable record that pairs a {@link SolarTerm} with its calculated date for a specific year.
 * This avoids re-calculating the date when iterating over solar terms.</p>
 * <p>一个不可变记录，将 {@link SolarTerm} 与指定年份的计算日期配对，
 * 避免在遍历节气时重复计算日期。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Pairs solar term with its date - 节气与日期配对</li>
 *   <li>Convenience accessors for term properties - 节气属性便捷访问</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SolarTermInfo info = new SolarTermInfo(SolarTerm.LI_CHUN, LocalDate.of(2024, 2, 4));
 * String name = info.getName();           // 立春
 * boolean major = info.isMajor();         // false
 * LocalDate date = info.date();           // 2024-02-04
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: No (fields must not be null) - 空值安全: 否（字段不能为null）</li>
 * </ul>
 *
 * @param term the solar term | 节气
 * @param date the date of the solar term | 节气日期
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see SolarTerm
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.3
 */
public record SolarTermInfo(SolarTerm term, LocalDate date) {

    /**
     * Compact constructor with validation.
     * 带验证的紧凑构造器。
     */
    public SolarTermInfo {
        Objects.requireNonNull(term, "term must not be null");
        Objects.requireNonNull(date, "date must not be null");
    }

    /**
     * Get Chinese name of the solar term
     * 获取节气中文名称
     *
     * @return the Chinese name | 中文名称
     */
    public String getName() {
        return term.getName();
    }

    /**
     * Get English name of the solar term
     * 获取节气英文名称
     *
     * @return the English name | 英文名称
     */
    public String getEnglishName() {
        return term.getEnglishName();
    }

    /**
     * Check if this is a major solar term (中气)
     * 检查是否为中气
     *
     * @return true if major | 如果是中气返回true
     */
    public boolean isMajor() {
        return term.isMajor();
    }

    @Override
    public String toString() {
        return term.getName() + "(" + date + ")";
    }
}
