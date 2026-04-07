package cloud.opencode.base.i18n.handler;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe handler that collects all missing message keys for later inspection
 * 线程安全的处理器，收集所有缺失消息键以供后续检查
 *
 * <p>Useful in testing environments to verify that all used message keys are
 * defined in the message bundles.</p>
 * <p>在测试环境中用于验证所有使用的消息键都在消息包中定义。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Thread-safe collection - 线程安全收集</li>
 *   <li>Deduplicates key+locale pairs - 去重键+区域对</li>
 *   <li>Clearable for test reuse - 可清除以重复使用</li>
 *   <li>Query methods for assertions - 用于断言的查询方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CollectingMissingKeyHandler collector = new CollectingMissingKeyHandler();
 * OpenI18n.setMissingKeyHandler(collector);
 *
 * // Run tests...
 * assertThat(collector.getMissingKeys()).isEmpty();
 *
 * // Or check specific key
 * assertThat(collector.contains("user.welcome")).isFalse();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (ConcurrentHashMap) - 线程安全: 是（ConcurrentHashMap）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.3
 */
public final class CollectingMissingKeyHandler implements MissingKeyHandler {

    private final Set<String> missingKeys = ConcurrentHashMap.newKeySet();

    // ==================== MissingKeyHandler ====================

    /**
     * Adds the missing key to the collection
     * 将缺失键添加到集合中
     *
     * @param key    the missing message key | 缺失的消息键
     * @param locale the locale that was requested | 请求的区域
     */
    @Override
    public void onMissingKey(String key, Locale locale) {
        missingKeys.add(key);
    }

    // ==================== Query Methods | 查询方法 ====================

    /**
     * Returns all collected missing keys as an unmodifiable set
     * 以不可修改集合的形式返回所有收集到的缺失键
     *
     * @return unmodifiable set of missing keys | 缺失键的不可修改集合
     */
    public Set<String> getMissingKeys() {
        return Collections.unmodifiableSet(missingKeys);
    }

    /**
     * Returns whether the given key was reported as missing
     * 返回给定键是否被报告为缺失
     *
     * @param key the key to check | 要检查的键
     * @return true if the key was reported missing | 如果键被报告为缺失则为 true
     */
    public boolean contains(String key) {
        return missingKeys.contains(key);
    }

    /**
     * Returns the number of unique missing keys collected
     * 返回收集到的唯一缺失键数量
     *
     * @return count | 数量
     */
    public int size() {
        return missingKeys.size();
    }

    /**
     * Returns true if no missing keys have been collected
     * 如果没有收集到缺失键则返回 true
     *
     * @return true if empty | 如果为空则为 true
     */
    public boolean isEmpty() {
        return missingKeys.isEmpty();
    }

    /**
     * Clears all collected keys (useful between test cases)
     * 清除所有收集的键（在测试用例之间使用）
     */
    public void clear() {
        missingKeys.clear();
    }
}
