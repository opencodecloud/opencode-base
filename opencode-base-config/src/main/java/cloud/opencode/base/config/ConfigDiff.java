package cloud.opencode.base.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Configuration snapshot comparison utility
 * 配置快照差异对比工具
 *
 * <p>Compares two configuration snapshots and produces a list of change events
 * describing additions, removals, and modifications.</p>
 * <p>比较两个配置快照，生成描述添加、删除和修改的变更事件列表。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Compare two Config instances - 比较两个Config实例</li>
 *   <li>Compare two property maps - 比较两个属性映射</li>
 *   <li>Detect added, removed, and modified keys - 检测添加、删除和修改的键</li>
 *   <li>Human-readable change formatting - 可读的变更格式化</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Config before = OpenConfig.builder()
 *     .addProperties(Map.of("db.port", "3306", "old.key", "abc"))
 *     .build();
 * Config after = OpenConfig.builder()
 *     .addProperties(Map.of("db.port", "5432", "cache.ttl", "60"))
 *     .build();
 *
 * List<ConfigChangeEvent> changes = ConfigDiff.diff(before, after);
 * String formatted = ConfigDiff.format(changes);
 * // MODIFIED: db.port (3306 → 5432)
 * // ADDED: cache.ttl (60)
 * // REMOVED: old.key (was: abc)
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Returns unmodifiable lists - 返回不可修改列表</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.3
 */
public final class ConfigDiff {

    private ConfigDiff() {
        // Prevent instantiation
    }

    // ==================== Diff Methods | 差异方法 ====================

    /**
     * Compare two Config instances and produce a list of change events
     * 比较两个Config实例并生成变更事件列表
     *
     * <p>Detects added, removed, and modified keys. The result is sorted by key
     * and returned as an unmodifiable list.</p>
     * <p>检测添加、删除和修改的键。结果按键排序并返回为不可修改列表。</p>
     *
     * @param before the original configuration | 原始配置
     * @param after  the new configuration | 新配置
     * @return unmodifiable list of change events sorted by key | 按键排序的不可修改变更事件列表
     * @throws NullPointerException if before or after is null | 如果before或after为null
     */
    public static List<ConfigChangeEvent> diff(Config before, Config after) {
        Objects.requireNonNull(before, "before must not be null");
        Objects.requireNonNull(after, "after must not be null");

        Map<String, String> beforeMap = toMap(before);
        Map<String, String> afterMap = toMap(after);
        return diff(beforeMap, afterMap);
    }

    /**
     * Compare two property maps and produce a list of change events
     * 比较两个属性映射并生成变更事件列表
     *
     * <p>Detects added, removed, and modified keys. The result is sorted by key
     * and returned as an unmodifiable list.</p>
     * <p>检测添加、删除和修改的键。结果按键排序并返回为不可修改列表。</p>
     *
     * @param before the original properties | 原始属性
     * @param after  the new properties | 新属性
     * @return unmodifiable list of change events sorted by key | 按键排序的不可修改变更事件列表
     * @throws NullPointerException if before or after is null | 如果before或after为null
     */
    public static List<ConfigChangeEvent> diff(Map<String, String> before, Map<String, String> after) {
        Objects.requireNonNull(before, "before must not be null");
        Objects.requireNonNull(after, "after must not be null");

        List<ConfigChangeEvent> changes = new ArrayList<>();

        // Collect all keys sorted
        Set<String> allKeys = new TreeSet<>();
        allKeys.addAll(before.keySet());
        allKeys.addAll(after.keySet());

        for (String key : allKeys) {
            boolean inBefore = before.containsKey(key);
            boolean inAfter = after.containsKey(key);

            if (inAfter && !inBefore) {
                // ADDED
                changes.add(ConfigChangeEvent.added(key, after.get(key)));
            } else if (inBefore && !inAfter) {
                // REMOVED
                changes.add(ConfigChangeEvent.removed(key, before.get(key)));
            } else if (inBefore) {
                // Both present, check if modified
                String oldValue = before.get(key);
                String newValue = after.get(key);
                if (!Objects.equals(oldValue, newValue)) {
                    changes.add(ConfigChangeEvent.modified(key, oldValue, newValue));
                }
            }
        }

        return Collections.unmodifiableList(changes);
    }

    // ==================== Format Method | 格式化方法 ====================

    /**
     * Format a list of change events as a human-readable string
     * 将变更事件列表格式化为可读字符串
     *
     * <p>Output format examples:</p>
     * <ul>
     *   <li>{@code + app.new-key = value}</li>
     *   <li>{@code ~ db.port: 3306 -> 5432}</li>
     *   <li>{@code - old.removed-key = was-value}</li>
     * </ul>
     *
     * @param changes list of change events | 变更事件列表
     * @return formatted string representation | 格式化字符串表示
     * @throws NullPointerException if changes is null | 如果changes为null
     */
    public static String format(List<ConfigChangeEvent> changes) {
        Objects.requireNonNull(changes, "changes must not be null");

        StringBuilder sb = new StringBuilder();
        for (ConfigChangeEvent event : changes) {
            if (!sb.isEmpty()) {
                sb.append('\n');
            }
            switch (event.changeType()) {
                case ADDED -> sb.append("+ ").append(event.key())
                        .append(" = ").append(event.newValue());
                case MODIFIED -> sb.append("~ ").append(event.key())
                        .append(": ").append(event.oldValue())
                        .append(" -> ").append(event.newValue());
                case REMOVED -> sb.append("- ").append(event.key())
                        .append(" = ").append(event.oldValue());
            }
        }
        return sb.toString();
    }

    // ==================== Private Methods | 私有方法 ====================

    /**
     * Convert a Config instance to a flat key-value map
     * 将Config实例转换为扁平键值映射
     */
    private static Map<String, String> toMap(Config config) {
        Map<String, String> map = new TreeMap<>();
        for (String key : config.getKeys()) {
            // Use default value to avoid exception if key is removed during hot-reload
            map.put(key, config.getString(key, null));
        }
        return map;
    }
}
