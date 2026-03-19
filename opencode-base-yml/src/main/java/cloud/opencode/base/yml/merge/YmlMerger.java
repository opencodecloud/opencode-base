package cloud.opencode.base.yml.merge;

import java.util.*;

/**
 * YAML Merger - Merges multiple YAML documents
 * YAML 合并器 - 合并多个 YAML 文档
 *
 * <p>This class provides utilities for merging YAML data structures.</p>
 * <p>此类提供合并 YAML 数据结构的工具。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Deep merge of nested YAML maps - 嵌套 YAML 映射的深度合并</li>
 *   <li>Multiple merge strategies (override, keep-first, append, unique, fail-on-conflict) - 多种合并策略</li>
 *   <li>Merge varargs, arrays, or lists of maps - 合并可变参数、数组或映射列表</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Merge two documents
 * Map<String, Object> result = YmlMerger.merge(base, overlay);
 *
 * // Merge with strategy
 * Map<String, Object> result = YmlMerger.merge(base, overlay, MergeStrategy.DEEP_MERGE);
 *
 * // Merge multiple documents
 * Map<String, Object> result = YmlMerger.mergeAll(doc1, doc2, doc3);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes (null maps treated as empty) - 空值安全: 是（空映射视为空）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
public final class YmlMerger {

    private YmlMerger() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    /**
     * Merges two maps with default strategy (DEEP_MERGE).
     * 使用默认策略（DEEP_MERGE）合并两个映射。
     *
     * @param base    the base map | 基础映射
     * @param overlay the overlay map | 覆盖映射
     * @return the merged map | 合并后的映射
     */
    public static Map<String, Object> merge(Map<String, Object> base, Map<String, Object> overlay) {
        return merge(base, overlay, MergeStrategy.DEEP_MERGE);
    }

    /**
     * Merges two maps with the specified strategy.
     * 使用指定策略合并两个映射。
     *
     * @param base     the base map | 基础映射
     * @param overlay  the overlay map | 覆盖映射
     * @param strategy the merge strategy | 合并策略
     * @return the merged map | 合并后的映射
     */
    public static Map<String, Object> merge(Map<String, Object> base, Map<String, Object> overlay, MergeStrategy strategy) {
        if (base == null) {
            return overlay != null ? new LinkedHashMap<>(overlay) : new LinkedHashMap<>();
        }
        if (overlay == null) {
            return new LinkedHashMap<>(base);
        }

        Map<String, Object> result = new LinkedHashMap<>(base);

        for (Map.Entry<String, Object> entry : overlay.entrySet()) {
            String key = entry.getKey();
            Object overlayValue = entry.getValue();
            Object baseValue = result.get(key);

            result.put(key, mergeValues(baseValue, overlayValue, strategy));
        }

        return result;
    }

    /**
     * Merges multiple maps.
     * 合并多个映射。
     *
     * @param maps the maps to merge | 要合并的映射
     * @return the merged map | 合并后的映射
     */
    @SafeVarargs
    public static Map<String, Object> mergeAll(Map<String, Object>... maps) {
        return mergeAll(MergeStrategy.DEEP_MERGE, maps);
    }

    /**
     * Merges multiple maps with the specified strategy.
     * 使用指定策略合并多个映射。
     *
     * @param strategy the merge strategy | 合并策略
     * @param maps     the maps to merge | 要合并的映射
     * @return the merged map | 合并后的映射
     */
    @SafeVarargs
    public static Map<String, Object> mergeAll(MergeStrategy strategy, Map<String, Object>... maps) {
        if (maps == null || maps.length == 0) {
            return new LinkedHashMap<>();
        }

        Map<String, Object> result = new LinkedHashMap<>();
        for (Map<String, Object> map : maps) {
            result = merge(result, map, strategy);
        }
        return result;
    }

    /**
     * Merges a list of maps.
     * 合并映射列表。
     *
     * @param maps the maps to merge | 要合并的映射
     * @return the merged map | 合并后的映射
     */
    public static Map<String, Object> mergeAll(List<Map<String, Object>> maps) {
        return mergeAll(maps, MergeStrategy.DEEP_MERGE);
    }

    /**
     * Merges a list of maps with the specified strategy.
     * 使用指定策略合并映射列表。
     *
     * @param maps     the maps to merge | 要合并的映射
     * @param strategy the merge strategy | 合并策略
     * @return the merged map | 合并后的映射
     */
    public static Map<String, Object> mergeAll(List<Map<String, Object>> maps, MergeStrategy strategy) {
        if (maps == null || maps.isEmpty()) {
            return new LinkedHashMap<>();
        }

        Map<String, Object> result = new LinkedHashMap<>();
        for (Map<String, Object> map : maps) {
            result = merge(result, map, strategy);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Object mergeValues(Object baseValue, Object overlayValue, MergeStrategy strategy) {
        if (baseValue == null) {
            return overlayValue;
        }
        if (overlayValue == null) {
            return baseValue;
        }

        return switch (strategy) {
            case OVERRIDE -> overlayValue;
            case KEEP_FIRST -> baseValue;
            case DEEP_MERGE -> {
                if (baseValue instanceof Map<?, ?> baseMap && overlayValue instanceof Map<?, ?> overlayMap) {
                    yield merge((Map<String, Object>) baseMap, (Map<String, Object>) overlayMap, strategy);
                }
                yield overlayValue;
            }
            case APPEND_LISTS -> {
                if (baseValue instanceof List<?> baseList && overlayValue instanceof List<?> overlayList) {
                    List<Object> result = new ArrayList<>((List<Object>) baseList);
                    result.addAll((List<Object>) overlayList);
                    yield result;
                }
                if (baseValue instanceof Map<?, ?> baseMap && overlayValue instanceof Map<?, ?> overlayMap) {
                    yield merge((Map<String, Object>) baseMap, (Map<String, Object>) overlayMap, strategy);
                }
                yield overlayValue;
            }
            case MERGE_LISTS_UNIQUE -> {
                if (baseValue instanceof List<?> baseList && overlayValue instanceof List<?> overlayList) {
                    Set<Object> set = new LinkedHashSet<>((List<Object>) baseList);
                    set.addAll((List<Object>) overlayList);
                    yield new ArrayList<>(set);
                }
                if (baseValue instanceof Map<?, ?> baseMap && overlayValue instanceof Map<?, ?> overlayMap) {
                    yield merge((Map<String, Object>) baseMap, (Map<String, Object>) overlayMap, strategy);
                }
                yield overlayValue;
            }
            case FAIL_ON_CONFLICT -> {
                if (!Objects.equals(baseValue, overlayValue)) {
                    throw new IllegalStateException("Merge conflict: " + baseValue + " vs " + overlayValue);
                }
                yield baseValue;
            }
        };
    }
}
