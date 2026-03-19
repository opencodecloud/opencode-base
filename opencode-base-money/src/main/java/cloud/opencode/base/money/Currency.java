package cloud.opencode.base.money;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Currency
 * 货币类型
 *
 * <p>Enumeration of commonly used currencies with symbol, name, and decimal scale.</p>
 * <p>常用货币枚举，包含符号、名称和小数位数。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Currency cny = Currency.CNY;
 * System.out.println(cny.getSymbol());  // ¥
 * System.out.println(cny.getName());    // 人民币
 * System.out.println(cny.getScale());   // 2
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Enumeration of commonly used currencies (CNY, USD, EUR, etc.) - 常用货币枚举</li>
 *   <li>Currency symbol, name, code, and decimal scale - 货币符号、名称、代码和小数位数</li>
 *   <li>Lookup by currency code - 按货币代码查找</li>
 *   <li>Locale-based currency detection - 基于区域的货币检测</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable enum) - 线程安全: 是（不可变枚举）</li>
 *   <li>Null-safe: No, code must not be null for lookup - 空值安全: 否，查找时代码不可为null</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-money V1.0.0
 */
public enum Currency {

    /**
     * Chinese Yuan Renminbi | 人民币
     */
    CNY("¥", "人民币", "Chinese Yuan", "CNY", 2),

    /**
     * United States Dollar | 美元
     */
    USD("$", "美元", "US Dollar", "USD", 2),

    /**
     * Euro | 欧元
     */
    EUR("€", "欧元", "Euro", "EUR", 2),

    /**
     * British Pound Sterling | 英镑
     */
    GBP("£", "英镑", "British Pound", "GBP", 2),

    /**
     * Japanese Yen | 日元
     */
    JPY("¥", "日元", "Japanese Yen", "JPY", 0),

    /**
     * Hong Kong Dollar | 港币
     */
    HKD("HK$", "港币", "Hong Kong Dollar", "HKD", 2),

    /**
     * Australian Dollar | 澳元
     */
    AUD("A$", "澳元", "Australian Dollar", "AUD", 2),

    /**
     * Canadian Dollar | 加拿大元
     */
    CAD("C$", "加拿大元", "Canadian Dollar", "CAD", 2),

    /**
     * Swiss Franc | 瑞士法郎
     */
    CHF("CHF", "瑞士法郎", "Swiss Franc", "CHF", 2),

    /**
     * Singapore Dollar | 新加坡元
     */
    SGD("S$", "新加坡元", "Singapore Dollar", "SGD", 2),

    /**
     * South Korean Won | 韩元
     */
    KRW("₩", "韩元", "South Korean Won", "KRW", 0),

    /**
     * Taiwan Dollar | 台币
     */
    TWD("NT$", "台币", "Taiwan Dollar", "TWD", 2);

    private static final Map<String, Currency> LOOKUP;
    static {
        Map<String, Currency> map = new HashMap<>();
        for (Currency c : values()) {
            map.put(c.code.toUpperCase(Locale.ROOT), c);
            map.put(c.name().toUpperCase(Locale.ROOT), c);
        }
        LOOKUP = Map.copyOf(map);
    }

    private final String symbol;
    private final String nameZh;
    private final String nameEn;
    private final String code;
    private final int scale;

    Currency(String symbol, String nameZh, String nameEn, String code, int scale) {
        this.symbol = symbol;
        this.nameZh = nameZh;
        this.nameEn = nameEn;
        this.code = code;
        this.scale = scale;
    }

    /**
     * Get currency symbol
     * 获取货币符号
     *
     * @return the symbol | 符号
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Get Chinese name
     * 获取中文名称
     *
     * @return the Chinese name | 中文名称
     */
    public String getNameZh() {
        return nameZh;
    }

    /**
     * Get English name
     * 获取英文名称
     *
     * @return the English name | 英文名称
     */
    public String getNameEn() {
        return nameEn;
    }

    /**
     * Get name (Chinese)
     * 获取名称（中文）
     *
     * @return the name | 名称
     */
    public String getName() {
        return nameZh;
    }

    /**
     * Get ISO 4217 currency code
     * 获取 ISO 4217 货币代码
     *
     * @return the code | 代码
     */
    public String getCode() {
        return code;
    }

    /**
     * Get decimal scale
     * 获取小数位数
     *
     * @return the scale | 小数位数
     */
    public int getScale() {
        return scale;
    }

    /**
     * Get currency by code
     * 根据代码获取货币
     *
     * @param code the currency code | 货币代码
     * @return the currency | 货币
     * @throws IllegalArgumentException if currency not found | 如果未找到货币
     */
    public static Currency of(String code) {
        if (code == null) {
            throw new IllegalArgumentException("Currency code must not be null");
        }
        Currency c = LOOKUP.get(code.toUpperCase(Locale.ROOT));
        if (c == null) {
            throw new IllegalArgumentException("Unknown currency: " + code);
        }
        return c;
    }

    /**
     * Check if currency code is supported
     * 检查货币代码是否支持
     *
     * @param code the currency code | 货币代码
     * @return true if supported | 如果支持返回true
     */
    public static boolean isSupported(String code) {
        return code != null && LOOKUP.containsKey(code.toUpperCase(Locale.ROOT));
    }
}
