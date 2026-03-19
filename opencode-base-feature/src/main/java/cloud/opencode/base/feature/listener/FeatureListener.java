package cloud.opencode.base.feature.listener;

/**
 * Feature Listener Interface
 * 功能监听器接口
 *
 * <p>Listener for feature change events.</p>
 * <p>功能变更事件的监听器。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Change notification - 变更通知</li>
 *   <li>Audit logging - 审计日志</li>
 *   <li>Cache invalidation - 缓存失效</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * OpenFeature.getInstance().addListener((key, oldValue, newValue) -> {
 *     log.info("Feature {} changed: {} -> {}", key, oldValue, newValue);
 * });
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
@FunctionalInterface
public interface FeatureListener {

    /**
     * Called when a feature's enabled state changes
     * 当功能的启用状态改变时调用
     *
     * @param key      the feature key | 功能键
     * @param oldValue the old enabled state | 旧的启用状态
     * @param newValue the new enabled state | 新的启用状态
     */
    void onFeatureChanged(String key, boolean oldValue, boolean newValue);
}
