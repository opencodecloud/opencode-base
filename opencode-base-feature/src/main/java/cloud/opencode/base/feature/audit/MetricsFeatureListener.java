package cloud.opencode.base.feature.audit;

import cloud.opencode.base.feature.listener.FeatureListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Metrics Feature Listener
 * 指标功能监听器
 *
 * <p>Listener that collects metrics on feature changes.</p>
 * <p>收集功能变更指标的监听器。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Change counting - 变更计数</li>
 *   <li>Enable/Disable tracking - 启用/禁用跟踪</li>
 *   <li>Per-feature metrics - 按功能指标</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * MetricsFeatureListener metrics = new MetricsFeatureListener();
 * OpenFeature.getInstance().addListener(metrics);
 *
 * // Later, get metrics
 * long totalChanges = metrics.getTotalChanges();
 * long enableCount = metrics.getEnableCount("feature-key");
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Partial (validates inputs) - 空值安全: 部分（验证输入）</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
public class MetricsFeatureListener implements FeatureListener {

    private final AtomicLong totalChanges = new AtomicLong(0);
    private final Map<String, AtomicLong> enableCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> disableCounts = new ConcurrentHashMap<>();

    @Override
    public void onFeatureChanged(String key, boolean oldValue, boolean newValue) {
        totalChanges.incrementAndGet();

        if (newValue && !oldValue) {
            // Enabled
            enableCounts.computeIfAbsent(key, _ -> new AtomicLong(0)).incrementAndGet();
        } else if (!newValue && oldValue) {
            // Disabled
            disableCounts.computeIfAbsent(key, _ -> new AtomicLong(0)).incrementAndGet();
        }
    }

    /**
     * Get total number of changes
     * 获取总变更数
     *
     * @return total changes | 总变更数
     */
    public long getTotalChanges() {
        return totalChanges.get();
    }

    /**
     * Get enable count for a feature
     * 获取功能的启用次数
     *
     * @param key the feature key | 功能键
     * @return enable count | 启用次数
     */
    public long getEnableCount(String key) {
        AtomicLong count = enableCounts.get(key);
        return count != null ? count.get() : 0;
    }

    /**
     * Get disable count for a feature
     * 获取功能的禁用次数
     *
     * @param key the feature key | 功能键
     * @return disable count | 禁用次数
     */
    public long getDisableCount(String key) {
        AtomicLong count = disableCounts.get(key);
        return count != null ? count.get() : 0;
    }

    /**
     * Get all enable counts
     * 获取所有启用计数
     *
     * @return map of feature key to enable count | 功能键到启用计数的映射
     */
    public Map<String, Long> getAllEnableCounts() {
        Map<String, Long> result = new ConcurrentHashMap<>();
        enableCounts.forEach((k, v) -> result.put(k, v.get()));
        return result;
    }

    /**
     * Get all disable counts
     * 获取所有禁用计数
     *
     * @return map of feature key to disable count | 功能键到禁用计数的映射
     */
    public Map<String, Long> getAllDisableCounts() {
        Map<String, Long> result = new ConcurrentHashMap<>();
        disableCounts.forEach((k, v) -> result.put(k, v.get()));
        return result;
    }

    /**
     * Reset all metrics
     * 重置所有指标
     */
    public void reset() {
        totalChanges.set(0);
        enableCounts.clear();
        disableCounts.clear();
    }
}
