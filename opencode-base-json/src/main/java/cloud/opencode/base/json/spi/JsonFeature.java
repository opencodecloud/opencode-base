
package cloud.opencode.base.json.spi;

/**
 * JSON Feature - Configuration Features for JSON Processing
 * JSON 特性 - JSON 处理的配置特性
 *
 * <p>This enum defines configurable features that control JSON
 * serialization and deserialization behavior.</p>
 * <p>此枚举定义控制 JSON 序列化和反序列化行为的可配置特性。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * JsonConfig config = JsonConfig.builder()
 *     .enable(JsonFeature.PRETTY_PRINT)
 *     .enable(JsonFeature.IGNORE_UNKNOWN_PROPERTIES)
 *     .build();
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Categorized features: serialization, deserialization, security - 分类特性：序列化、反序列化、安全</li>
 *   <li>Default enabled/disabled state per feature - 每个特性的默认启用/禁用状态</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: N/A - 空值安全: 不适用</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
public enum JsonFeature {

    // ==================== Serialization Features ====================

    /**
     * Enable pretty printing with indentation.
     * 启用带缩进的美化打印。
     * <p>Default: false</p>
     */
    PRETTY_PRINT(Category.SERIALIZATION, false),

    /**
     * Write dates as ISO-8601 strings instead of timestamps.
     * 将日期写为 ISO-8601 字符串而非时间戳。
     * <p>Default: true</p>
     */
    WRITE_DATES_AS_ISO8601(Category.SERIALIZATION, true),

    /**
     * Write enums using their name() instead of toString().
     * 使用枚举的 name() 而非 toString() 写入。
     * <p>Default: true</p>
     */
    WRITE_ENUMS_USING_NAME(Category.SERIALIZATION, true),

    /**
     * Write null values for map entries.
     * 写入 Map 中的 null 值条目。
     * <p>Default: true</p>
     */
    WRITE_NULL_MAP_VALUES(Category.SERIALIZATION, true),

    /**
     * Write empty arrays for null array values.
     * 为 null 数组值写入空数组。
     * <p>Default: false</p>
     */
    WRITE_EMPTY_ARRAYS_FOR_NULL(Category.SERIALIZATION, false),

    /**
     * Sort map keys alphabetically.
     * 按字母顺序排序 Map 键。
     * <p>Default: false</p>
     */
    SORT_MAP_KEYS(Category.SERIALIZATION, false),

    /**
     * Escape non-ASCII characters.
     * 转义非 ASCII 字符。
     * <p>Default: false</p>
     */
    ESCAPE_NON_ASCII(Category.SERIALIZATION, false),

    /**
     * Include null properties in output.
     * 在输出中包含 null 属性。
     * <p>Default: true</p>
     */
    INCLUDE_NULL_PROPERTIES(Category.SERIALIZATION, true),

    /**
     * Include empty collections in output.
     * 在输出中包含空集合。
     * <p>Default: true</p>
     */
    INCLUDE_EMPTY_COLLECTIONS(Category.SERIALIZATION, true),

    // ==================== Deserialization Features ====================

    /**
     * Ignore unknown properties during deserialization.
     * 反序列化时忽略未知属性。
     * <p>Default: true</p>
     */
    IGNORE_UNKNOWN_PROPERTIES(Category.DESERIALIZATION, true),

    /**
     * Accept single values as arrays.
     * 接受单个值作为数组。
     * <p>Default: false</p>
     */
    ACCEPT_SINGLE_VALUE_AS_ARRAY(Category.DESERIALIZATION, false),

    /**
     * Accept empty string as null for object types.
     * 对对象类型接受空字符串作为 null。
     * <p>Default: false</p>
     */
    ACCEPT_EMPTY_STRING_AS_NULL(Category.DESERIALIZATION, false),

    /**
     * Fail on null for primitives.
     * 基本类型遇到 null 时失败。
     * <p>Default: false</p>
     */
    FAIL_ON_NULL_FOR_PRIMITIVES(Category.DESERIALIZATION, false),

    /**
     * Fail on numbers for enums.
     * 枚举遇到数字时失败。
     * <p>Default: false</p>
     */
    FAIL_ON_NUMBERS_FOR_ENUMS(Category.DESERIALIZATION, false),

    /**
     * Use BigDecimal for floating-point numbers.
     * 对浮点数使用 BigDecimal。
     * <p>Default: false</p>
     */
    USE_BIG_DECIMAL_FOR_FLOATS(Category.DESERIALIZATION, false),

    /**
     * Use BigInteger for large integers.
     * 对大整数使用 BigInteger。
     * <p>Default: false</p>
     */
    USE_BIG_INTEGER_FOR_INTS(Category.DESERIALIZATION, false),

    /**
     * Allow comments in JSON (non-standard).
     * 允许 JSON 中的注释（非标准）。
     * <p>Default: false</p>
     */
    ALLOW_COMMENTS(Category.DESERIALIZATION, false),

    /**
     * Allow trailing comma in arrays and objects (non-standard).
     * 允许数组和对象中的尾随逗号（非标准）。
     * <p>Default: false</p>
     */
    ALLOW_TRAILING_COMMA(Category.DESERIALIZATION, false),

    /**
     * Allow unquoted field names (non-standard).
     * 允许不带引号的字段名（非标准）。
     * <p>Default: false</p>
     */
    ALLOW_UNQUOTED_FIELD_NAMES(Category.DESERIALIZATION, false),

    /**
     * Allow single quotes instead of double quotes (non-standard).
     * 允许单引号代替双引号（非标准）。
     * <p>Default: false</p>
     */
    ALLOW_SINGLE_QUOTES(Category.DESERIALIZATION, false),

    // ==================== Security Features ====================

    /**
     * Limit maximum string length to prevent DoS attacks.
     * 限制最大字符串长度以防止 DoS 攻击。
     * <p>Default: true</p>
     */
    LIMIT_STRING_LENGTH(Category.SECURITY, true),

    /**
     * Limit maximum nesting depth to prevent stack overflow.
     * 限制最大嵌套深度以防止栈溢出。
     * <p>Default: true</p>
     */
    LIMIT_NESTING_DEPTH(Category.SECURITY, true),

    /**
     * Limit maximum number of entries in arrays/objects.
     * 限制数组/对象中的最大条目数。
     * <p>Default: true</p>
     */
    LIMIT_ENTRY_COUNT(Category.SECURITY, true);

    /**
     * Feature category enumeration
     * 特性类别枚举
     */
    public enum Category {
        /** Serialization features - 序列化特性 */
        SERIALIZATION,
        /** Deserialization features - 反序列化特性 */
        DESERIALIZATION,
        /** Security features - 安全特性 */
        SECURITY
    }

    private final Category category;
    private final boolean enabledByDefault;

    /**
     * Constructs a feature with category and default state.
     * 使用类别和默认状态构造特性。
     *
     * @param category         the feature category - 特性类别
     * @param enabledByDefault the default enabled state - 默认启用状态
     */
    JsonFeature(Category category, boolean enabledByDefault) {
        this.category = category;
        this.enabledByDefault = enabledByDefault;
    }

    /**
     * Returns the feature category.
     * 返回特性类别。
     *
     * @return the category - 类别
     */
    public Category getCategory() {
        return category;
    }

    /**
     * Returns whether this feature is enabled by default.
     * 返回此特性是否默认启用。
     *
     * @return true if enabled by default - 如果默认启用则返回 true
     */
    public boolean isEnabledByDefault() {
        return enabledByDefault;
    }

    /**
     * Returns whether this feature is a serialization feature.
     * 返回此特性是否为序列化特性。
     *
     * @return true if serialization feature - 如果是序列化特性则返回 true
     */
    public boolean isSerializationFeature() {
        return category == Category.SERIALIZATION;
    }

    /**
     * Returns whether this feature is a deserialization feature.
     * 返回此特性是否为反序列化特性。
     *
     * @return true if deserialization feature - 如果是反序列化特性则返回 true
     */
    public boolean isDeserializationFeature() {
        return category == Category.DESERIALIZATION;
    }

    /**
     * Returns whether this feature is a security feature.
     * 返回此特性是否为安全特性。
     *
     * @return true if security feature - 如果是安全特性则返回 true
     */
    public boolean isSecurityFeature() {
        return category == Category.SECURITY;
    }
}
