package cloud.opencode.base.i18n.formatter;

import cloud.opencode.base.i18n.spi.MessageFormatter;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default message formatter using Java's MessageFormat
 * 使用Java MessageFormat的默认消息格式化器
 *
 * <p>Formats messages using Java's standard MessageFormat syntax.</p>
 * <p>使用Java标准MessageFormat语法格式化消息。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>MessageFormat syntax - MessageFormat语法</li>
 *   <li>Pattern caching - 模式缓存</li>
 *   <li>Thread-safe - 线程安全</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * DefaultMessageFormatter formatter = new DefaultMessageFormatter();
 * String result = formatter.format("Hello, {0}!", Locale.ENGLISH, "World");
 * // result: "Hello, World!"
 * }</pre>
 *
 * <p><strong>Supported patterns | 支持的模式:</strong></p>
 * <ul>
 *   <li>{0}, {1}, {2}... - Positional arguments | 位置参数</li>
 *   <li>{0,number} - Number formatting | 数字格式化</li>
 *   <li>{0,date} - Date formatting | 日期格式化</li>
 *   <li>{0,time} - Time formatting | 时间格式化</li>
 *   <li>{0,choice,...} - Choice formatting | 选择格式化</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) where n is template length; O(1) for cached patterns - 时间复杂度: O(n)，n 为模板长度；缓存命中为 O(1)</li>
 *   <li>Space complexity: O(c) for pattern cache where c is cache size (max 1000) - 空间复杂度: O(c) 模式缓存，最大 1000 条</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.0
 */
public class DefaultMessageFormatter implements MessageFormatter {

    private final Map<String, MessageFormat> cache = new ConcurrentHashMap<>();
    private final boolean cacheEnabled;
    private final int maxCacheSize;

    /**
     * Creates a formatter with caching enabled
     * 创建启用缓存的格式化器
     */
    public DefaultMessageFormatter() {
        this(true, 1000);
    }

    /**
     * Creates a formatter with specified caching option
     * 使用指定的缓存选项创建格式化器
     *
     * @param cacheEnabled whether to enable caching | 是否启用缓存
     */
    public DefaultMessageFormatter(boolean cacheEnabled) {
        this(cacheEnabled, 1000);
    }

    /**
     * Creates a formatter with caching options
     * 使用缓存选项创建格式化器
     *
     * @param cacheEnabled whether to enable caching | 是否启用缓存
     * @param maxCacheSize maximum cache size | 最大缓存大小
     */
    public DefaultMessageFormatter(boolean cacheEnabled, int maxCacheSize) {
        this.cacheEnabled = cacheEnabled;
        this.maxCacheSize = maxCacheSize;
    }

    @Override
    public String format(String template, Locale locale, Object... args) {
        if (template == null) {
            return null;
        }
        if (args == null || args.length == 0) {
            return template;
        }

        MessageFormat format = (MessageFormat) getMessageFormat(template, locale).clone();
        return format.format(args);
    }

    @Override
    public String format(String template, Locale locale, Map<String, Object> params) {
        if (template == null) {
            return null;
        }
        if (params == null || params.isEmpty()) {
            return template;
        }

        // Convert named parameters to indexed for MessageFormat
        // This is a simple implementation - for full named parameter support,
        // use NamedParameterFormatter
        Object[] args = params.values().toArray();
        return format(template, locale, args);
    }

    @Override
    public void clearCache() {
        cache.clear();
    }

    /**
     * Gets the current cache size
     * 获取当前缓存大小
     *
     * @return cache size | 缓存大小
     */
    public int getCacheSize() {
        return cache.size();
    }

    /**
     * Checks if caching is enabled
     * 检查是否启用缓存
     *
     * @return true if caching is enabled | 如果启用缓存返回true
     */
    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    private MessageFormat getMessageFormat(String template, Locale locale) {
        if (!cacheEnabled) {
            return new MessageFormat(template, locale);
        }

        String cacheKey = template + '\0' + locale.toLanguageTag();

        // Check if already cached
        MessageFormat cached = cache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // Evict before adding if cache is full (outside computeIfAbsent for atomicity)
        if (cache.size() >= maxCacheSize) {
            // Remove ~10% of entries to avoid frequent eviction
            int toRemove = Math.max(1, maxCacheSize / 10);
            var iterator = cache.keySet().iterator();
            while (iterator.hasNext() && toRemove > 0) {
                iterator.next();
                iterator.remove();
                toRemove--;
            }
        }

        return cache.computeIfAbsent(cacheKey, k -> new MessageFormat(template, locale));
    }
}
