package cloud.opencode.base.date.holiday;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a holiday with date and metadata
 * 表示带有日期和元数据的假日
 *
 * <p>This class represents a single holiday with its date, name, type, and
 * additional information.</p>
 * <p>此类表示单个假日，包含其日期、名称、类型和附加信息。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Holiday date and name - 假日日期和名称</li>
 *   <li>Holiday type classification - 假日类型分类</li>
 *   <li>Observed date support - 调休日期支持</li>
 *   <li>Multi-language support - 多语言支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Holiday newYear = Holiday.of(
 *     LocalDate.of(2024, 1, 1),
 *     "New Year's Day",
 *     HolidayType.PUBLIC
 * );
 *
 * Holiday springFestival = Holiday.builder()
 *     .date(LocalDate.of(2024, 2, 10))
 *     .name("Spring Festival")
 *     .chineseName("春节")
 *     .type(HolidayType.PUBLIC)
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: Yes (with explicit null checks in builder) - 空值安全: 是（构建器中有明确的空值检查）</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
public final class Holiday implements Comparable<Holiday>, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The date of the holiday
     */
    private final LocalDate date;

    /**
     * The name of the holiday (English)
     */
    private final String name;

    /**
     * The Chinese name of the holiday
     */
    private final String chineseName;

    /**
     * The type of the holiday
     */
    private final HolidayType type;

    /**
     * The observed date (if different from actual date)
     */
    private final LocalDate observedDate;

    /**
     * Whether this is a day off
     */
    private final boolean dayOff;

    /**
     * Additional description
     */
    private final String description;

    // ==================== Holiday Type Enum | 假日类型枚举 ====================

    /**
     * Types of holidays
     * 假日类型
     */
    public enum HolidayType {
        /**
         * Public/national holiday
         * 公共/国家假日
         */
        PUBLIC,

        /**
         * Bank/financial holiday
         * 银行/金融假日
         */
        BANK,

        /**
         * Religious holiday
         * 宗教假日
         */
        RELIGIOUS,

        /**
         * Cultural/traditional holiday
         * 文化/传统假日
         */
        CULTURAL,

        /**
         * Observance (not necessarily a day off)
         * 纪念日（不一定放假）
         */
        OBSERVANCE,

        /**
         * Company-specific holiday
         * 公司特定假日
         */
        COMPANY
    }

    // ==================== Constructors | 构造函数 ====================

    /**
     * Private constructor for builder
     */
    private Holiday(Builder builder) {
        this.date = Objects.requireNonNull(builder.date, "date must not be null");
        this.name = Objects.requireNonNull(builder.name, "name must not be null");
        this.chineseName = builder.chineseName;
        this.type = builder.type != null ? builder.type : HolidayType.PUBLIC;
        this.observedDate = builder.observedDate;
        this.dayOff = builder.dayOff;
        this.description = builder.description;
    }

    // ==================== Static Factory Methods | 静态工厂方法 ====================

    /**
     * Creates a simple holiday
     * 创建简单假日
     *
     * @param date the date | 日期
     * @param name the name | 名称
     * @return the Holiday | 假日
     */
    public static Holiday of(LocalDate date, String name) {
        return builder().date(date).name(name).build();
    }

    /**
     * Creates a holiday with type
     * 创建带类型的假日
     *
     * @param date the date | 日期
     * @param name the name | 名称
     * @param type the type | 类型
     * @return the Holiday | 假日
     */
    public static Holiday of(LocalDate date, String name, HolidayType type) {
        return builder().date(date).name(name).type(type).build();
    }

    /**
     * Creates a holiday with bilingual names
     * 创建双语名称的假日
     *
     * @param date        the date | 日期
     * @param name        the English name | 英文名称
     * @param chineseName the Chinese name | 中文名称
     * @param type        the type | 类型
     * @return the Holiday | 假日
     */
    public static Holiday of(LocalDate date, String name, String chineseName, HolidayType type) {
        return builder().date(date).name(name).chineseName(chineseName).type(type).build();
    }

    /**
     * Creates a builder
     * 创建构建器
     *
     * @return the builder | 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    // ==================== Getters | 获取器 ====================

    /**
     * Gets the date
     * 获取日期
     *
     * @return the date | 日期
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Gets the name
     * 获取名称
     *
     * @return the name | 名称
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the Chinese name
     * 获取中文名称
     *
     * @return the Chinese name, or null if not set | 中文名称，如果未设置则为null
     */
    public String getChineseName() {
        return chineseName;
    }

    /**
     * Gets the localized name
     * 获取本地化名称
     *
     * @param preferChinese whether to prefer Chinese | 是否偏好中文
     * @return the localized name | 本地化名称
     */
    public String getLocalizedName(boolean preferChinese) {
        if (preferChinese && chineseName != null) {
            return chineseName;
        }
        return name;
    }

    /**
     * Gets the type
     * 获取类型
     *
     * @return the type | 类型
     */
    public HolidayType getType() {
        return type;
    }

    /**
     * Gets the observed date
     * 获取调休日期
     *
     * @return the observed date, or the actual date if not set | 调休日期，如果未设置则返回实际日期
     */
    public LocalDate getObservedDate() {
        return observedDate != null ? observedDate : date;
    }

    /**
     * Checks if this is a day off
     * 检查是否放假
     *
     * @return true if day off | 如果放假返回true
     */
    public boolean isDayOff() {
        return dayOff;
    }

    /**
     * Gets the description
     * 获取描述
     *
     * @return the description, or null if not set | 描述，如果未设置则为null
     */
    public String getDescription() {
        return description;
    }

    // ==================== Query Methods | 查询方法 ====================

    /**
     * Checks if the holiday falls on the specified date
     * 检查假日是否在指定日期
     *
     * @param date the date to check | 要检查的日期
     * @return true if on this date | 如果在此日期返回true
     */
    public boolean isOn(LocalDate date) {
        return this.date.equals(date);
    }

    /**
     * Checks if this is a public holiday
     * 检查是否为公共假日
     *
     * @return true if public holiday | 如果是公共假日返回true
     */
    public boolean isPublicHoliday() {
        return type == HolidayType.PUBLIC;
    }

    /**
     * Gets the year of the holiday
     * 获取假日的年份
     *
     * @return the year | 年份
     */
    public int getYear() {
        return date.getYear();
    }

    // ==================== Comparable Implementation | Comparable实现 ====================

    @Override
    public int compareTo(Holiday other) {
        return date.compareTo(other.date);
    }

    // ==================== Object Methods | Object方法 ====================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Holiday other)) return false;
        return Objects.equals(date, other.date) && Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, name);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(date).append(": ").append(name);
        if (chineseName != null) {
            sb.append(" (").append(chineseName).append(")");
        }
        sb.append(" [").append(type).append("]");
        if (dayOff) {
            sb.append(" *");
        }
        return sb.toString();
    }

    // ==================== Builder | 构建器 ====================

    /**
     * Builder for Holiday
     * Holiday构建器
     */
    public static class Builder {
        private LocalDate date;
        private String name;
        private String chineseName;
        private HolidayType type = HolidayType.PUBLIC;
        private LocalDate observedDate;
        private boolean dayOff = true;
        private String description;

        private Builder() {
        }

        /**
         * Sets the date
         * 设置日期
         *
         * @param date the date | 日期
         * @return this builder | 此构建器
         */
        public Builder date(LocalDate date) {
            this.date = date;
            return this;
        }

        /**
         * Sets the name
         * 设置名称
         *
         * @param name the name | 名称
         * @return this builder | 此构建器
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the Chinese name
         * 设置中文名称
         *
         * @param chineseName the Chinese name | 中文名称
         * @return this builder | 此构建器
         */
        public Builder chineseName(String chineseName) {
            this.chineseName = chineseName;
            return this;
        }

        /**
         * Sets the type
         * 设置类型
         *
         * @param type the type | 类型
         * @return this builder | 此构建器
         */
        public Builder type(HolidayType type) {
            this.type = type;
            return this;
        }

        /**
         * Sets the observed date
         * 设置调休日期
         *
         * @param observedDate the observed date | 调休日期
         * @return this builder | 此构建器
         */
        public Builder observedDate(LocalDate observedDate) {
            this.observedDate = observedDate;
            return this;
        }

        /**
         * Sets whether this is a day off
         * 设置是否放假
         *
         * @param dayOff whether it's a day off | 是否放假
         * @return this builder | 此构建器
         */
        public Builder dayOff(boolean dayOff) {
            this.dayOff = dayOff;
            return this;
        }

        /**
         * Sets the description
         * 设置描述
         *
         * @param description the description | 描述
         * @return this builder | 此构建器
         */
        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Builds the Holiday
         * 构建Holiday
         *
         * @return the Holiday | 假日
         */
        public Holiday build() {
            return new Holiday(this);
        }
    }
}
