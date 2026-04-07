package cloud.opencode.base.i18n.handler;

import java.util.Locale;

/**
 * Functional interface for handling missing i18n message keys
 * 处理缺失 i18n 消息键的函数式接口
 *
 * <p>Called when a message key cannot be found in the configured message provider.
 * Useful for collecting untranslated keys in development/testing environments.</p>
 * <p>当消息键在配置的消息提供者中找不到时调用。在开发/测试环境中用于收集未翻译的键。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Functional interface - 函数式接口</li>
 *   <li>Built-in logging handler - 内置日志处理器</li>
 *   <li>Built-in collecting handler - 内置收集处理器</li>
 *   <li>Composable via {@link #andThen(MissingKeyHandler)} - 可通过 andThen 组合</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Log missing keys
 * OpenI18n.setMissingKeyHandler(MissingKeyHandler.logging());
 *
 * // Collect for later inspection
 * CollectingMissingKeyHandler collector = MissingKeyHandler.collecting();
 * OpenI18n.setMissingKeyHandler(collector);
 * // ... after running tests ...
 * Set<String> missing = collector.getMissingKeys();
 *
 * // Custom lambda
 * OpenI18n.setMissingKeyHandler((key, locale) -> metrics.increment("i18n.missing." + key));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.3
 */
@FunctionalInterface
public interface MissingKeyHandler {

    /**
     * Called when a message key is not found
     * 当消息键未找到时调用
     *
     * @param key    the missing message key | 缺失的消息键
     * @param locale the locale that was requested | 请求的区域
     */
    void onMissingKey(String key, Locale locale);

    /**
     * Returns a composed handler that calls this handler and then the next
     * 返回一个组合处理器，先调用此处理器，再调用下一个
     *
     * @param next the handler to call after this one | 此处理器之后调用的处理器
     * @return composed handler | 组合处理器
     */
    default MissingKeyHandler andThen(MissingKeyHandler next) {
        return (key, locale) -> {
            this.onMissingKey(key, locale);
            next.onMissingKey(key, locale);
        };
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Returns a handler that logs missing keys using {@code System.Logger}
     * 返回使用 {@code System.Logger} 记录缺失键的处理器
     *
     * @return logging handler | 日志处理器
     */
    static MissingKeyHandler logging() {
        System.Logger logger = System.getLogger("cloud.opencode.base.i18n");
        return (key, locale) -> logger.log(System.Logger.Level.WARNING,
                "Missing i18n key: ''{0}'' [locale={1}]", key, locale);
    }

    /**
     * Returns a handler that collects all missing keys for later inspection
     * 返回收集所有缺失键以供后续检查的处理器
     *
     * @return collecting handler | 收集处理器
     */
    static CollectingMissingKeyHandler collecting() {
        return new CollectingMissingKeyHandler();
    }

    /**
     * A no-op handler that does nothing when a key is missing
     * 当键缺失时不执行任何操作的空处理器
     *
     * @return no-op handler | 空处理器
     */
    static MissingKeyHandler noOp() {
        return (key, locale) -> {};
    }
}
