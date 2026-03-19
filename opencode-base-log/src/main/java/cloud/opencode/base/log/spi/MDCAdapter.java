package cloud.opencode.base.log.spi;

import java.util.Map;

/**
 * MDC Adapter Interface - Mapped Diagnostic Context Adapter
 * MDC 适配器接口 - 映射诊断上下文适配器
 *
 * <p>This interface provides an abstraction layer for MDC operations,
 * allowing different logging frameworks to provide their own implementations.</p>
 * <p>此接口为 MDC 操作提供抽象层，允许不同的日志框架提供自己的实现。</p>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Key-value context storage abstraction - 键值上下文存储抽象</li>
 *   <li>Context map copy and restore operations - 上下文映射拷贝和恢复操作</li>
 *   <li>Thread-isolated diagnostic context - 线程隔离的诊断上下文</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Implement custom MDC adapter
 * public class MyMDCAdapter implements MDCAdapter {
 *     @Override
 *     public void put(String key, String value) { ... }
 *     @Override
 *     public String get(String key) { ... }
 *     // ... other methods
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Yes (returns null for missing keys) - 空值安全: 是（缺少的键返回 null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
public interface MDCAdapter {

    /**
     * Puts a key-value pair into the MDC.
     * 将键值对放入 MDC。
     *
     * @param key   the key - 键
     * @param value the value - 值
     */
    void put(String key, String value);

    /**
     * Gets a value from the MDC.
     * 从 MDC 获取值。
     *
     * @param key the key - 键
     * @return the value, or null if not found - 值，如果未找到则返回 null
     */
    String get(String key);

    /**
     * Removes a key from the MDC.
     * 从 MDC 移除键。
     *
     * @param key the key to remove - 要移除的键
     */
    void remove(String key);

    /**
     * Clears all entries from the MDC.
     * 清空 MDC 中的所有条目。
     */
    void clear();

    /**
     * Returns a copy of the current context map.
     * 返回当前上下文映射的副本。
     *
     * @return a copy of the context map - 上下文映射的副本
     */
    Map<String, String> getCopyOfContextMap();

    /**
     * Sets the context map.
     * 设置上下文映射。
     *
     * @param contextMap the context map - 上下文映射
     */
    void setContextMap(Map<String, String> contextMap);
}
