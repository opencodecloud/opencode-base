package cloud.opencode.base.feature;

import java.time.Instant;
import java.util.Map;

/**
 * Feature Snapshot
 * 功能快照
 *
 * <p>Immutable snapshot of all features at a point in time.</p>
 * <p>某一时刻所有功能的不可变快照。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Capture feature state - 捕获功能状态</li>
 *   <li>Restore feature state - 恢复功能状态</li>
 *   <li>Point-in-time snapshot - 时间点快照</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * FeatureSnapshot snapshot = openFeature.snapshot();
 * // ... modify features ...
 * openFeature.restore(snapshot); // restore to previous state
 * }</pre>
 *
 * @param features  the feature map | 功能映射
 * @param timestamp the snapshot timestamp | 快照时间戳
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: Partial (validates inputs) - 空值安全: 部分（验证输入）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.3
 */
public record FeatureSnapshot(Map<String, Feature> features, Instant timestamp) {

    /**
     * Compact constructor with validation
     * 带验证的紧凑构造函数
     */
    public FeatureSnapshot {
        features = features != null ? Map.copyOf(features) : Map.of();
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }

    /**
     * Get the number of features in this snapshot
     * 获取此快照中的功能数量
     *
     * @return the size | 大小
     */
    public int size() {
        return features.size();
    }

    /**
     * Check if this snapshot contains a feature with the given key
     * 检查此快照是否包含具有给定键的功能
     *
     * @param key the feature key | 功能键
     * @return true if contains | 如果包含返回true
     */
    public boolean contains(String key) {
        return key != null && features.containsKey(key);
    }
}
