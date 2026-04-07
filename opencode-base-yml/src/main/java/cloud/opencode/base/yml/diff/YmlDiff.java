package cloud.opencode.base.yml.diff;

import cloud.opencode.base.yml.YmlDocument;
import cloud.opencode.base.yml.exception.OpenYmlException;

import java.util.*;

/**
 * YAML Diff - Compares two YAML documents and produces a list of differences
 * YAML 差异比较器 - 比较两个 YAML 文档并生成差异列表
 *
 * <p>This utility class recursively compares two YAML data structures (maps, lists,
 * and scalar values) and returns a list of {@link DiffEntry} describing every
 * addition, removal, and modification.</p>
 * <p>此工具类递归比较两个 YAML 数据结构（映射、列表和标量值），并返回描述每个
 * 新增、移除和修改的 {@link DiffEntry} 列表。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Deep recursive comparison of nested maps - 嵌套映射的深度递归比较</li>
 *   <li>Index-based list comparison - 基于索引的列表比较</li>
 *   <li>Dot-notation paths with array index support (e.g. items[0].name) - 点号路径表示法，支持数组索引</li>
 *   <li>Null-safe: null base = all ADDED, null other = all REMOVED - 空值安全</li>
 *   <li>Max depth limit of 50 to prevent stack overflow - 最大深度限制 50 防止栈溢出</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Map<String, Object> base = Map.of("server", Map.of("port", 8080));
 * Map<String, Object> other = Map.of("server", Map.of("port", 9090));
 *
 * List<DiffEntry> diffs = YmlDiff.diff(base, other);
 * // [DiffEntry[type=MODIFIED, path=server.port, oldValue=8080, newValue=9090]]
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Depth-limited: Yes (max 50 levels) - 深度限制: 是（最多 50 层）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.3
 */
public final class YmlDiff {

    /**
     * Maximum recursion depth to prevent stack overflow on cyclic or extremely deep structures.
     * 最大递归深度，防止循环或极深结构导致栈溢出。
     */
    private static final int MAX_DEPTH = 50;

    private YmlDiff() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    /**
     * Compares two maps and returns a list of differences.
     * 比较两个映射并返回差异列表。
     *
     * @param base  the base map (may be null, treated as empty) | 基础映射（可为 null，视为空）
     * @param other the other map (may be null, treated as empty) | 另一个映射（可为 null，视为空）
     * @return unmodifiable list of diff entries | 不可修改的差异条目列表
     * @throws OpenYmlException if nesting depth exceeds limit | 当嵌套深度超过限制时
     */
    public static List<DiffEntry> diff(Map<String, Object> base, Map<String, Object> other) {
        Map<String, Object> safeBase = base != null ? base : Collections.emptyMap();
        Map<String, Object> safeOther = other != null ? other : Collections.emptyMap();
        List<DiffEntry> entries = new ArrayList<>();
        diffMaps(safeBase, safeOther, "", entries, 0);
        return Collections.unmodifiableList(entries);
    }

    /**
     * Compares two YAML documents and returns a list of differences.
     * 比较两个 YAML 文档并返回差异列表。
     *
     * @param base  the base document (may be null, treated as empty) | 基础文档（可为 null，视为空）
     * @param other the other document (may be null, treated as empty) | 另一个文档（可为 null，视为空）
     * @return unmodifiable list of diff entries | 不可修改的差异条目列表
     * @throws OpenYmlException if nesting depth exceeds limit | 当嵌套深度超过限制时
     */
    public static List<DiffEntry> diff(YmlDocument base, YmlDocument other) {
        Map<String, Object> baseMap = base != null ? base.asMap() : Collections.emptyMap();
        Map<String, Object> otherMap = other != null ? other.asMap() : Collections.emptyMap();
        return diff(baseMap, otherMap);
    }

    /**
     * Recursively compares two maps.
     * 递归比较两个映射。
     */
    @SuppressWarnings("unchecked")
    private static void diffMaps(Map<String, Object> base, Map<String, Object> other,
                                 String prefix, List<DiffEntry> entries, int depth) {
        checkDepth(depth);

        // Keys only in base → REMOVED
        for (Map.Entry<String, Object> entry : base.entrySet()) {
            String key = entry.getKey();
            String path = buildPath(prefix, key);
            if (!other.containsKey(key)) {
                entries.add(DiffEntry.removed(path, entry.getValue()));
            }
        }

        // Keys only in other → ADDED; keys in both → compare
        for (Map.Entry<String, Object> entry : other.entrySet()) {
            String key = entry.getKey();
            String path = buildPath(prefix, key);
            if (!base.containsKey(key)) {
                entries.add(DiffEntry.added(path, entry.getValue()));
            } else {
                Object baseVal = base.get(key);
                Object otherVal = entry.getValue();
                diffValues(baseVal, otherVal, path, entries, depth + 1);
            }
        }
    }

    /**
     * Compares two values at the given path, recursing into maps and lists.
     * 在给定路径比较两个值，递归进入映射和列表。
     */
    @SuppressWarnings("unchecked")
    private static void diffValues(Object baseVal, Object otherVal,
                                   String path, List<DiffEntry> entries, int depth) {
        checkDepth(depth);

        // Both null → equal
        if (baseVal == null && otherVal == null) {
            return;
        }

        // One null → modified
        if (baseVal == null || otherVal == null) {
            entries.add(DiffEntry.modified(path, baseVal, otherVal));
            return;
        }

        // Both maps → recurse
        if (baseVal instanceof Map<?, ?> baseMap && otherVal instanceof Map<?, ?> otherMap) {
            diffMaps((Map<String, Object>) baseMap, (Map<String, Object>) otherMap,
                    path, entries, depth);
            return;
        }

        // Both lists → index-based comparison
        if (baseVal instanceof List<?> baseList && otherVal instanceof List<?> otherList) {
            diffLists(baseList, otherList, path, entries, depth);
            return;
        }

        // Scalar comparison (or type change)
        if (!baseVal.equals(otherVal)) {
            entries.add(DiffEntry.modified(path, baseVal, otherVal));
        }
    }

    /**
     * Compares two lists element by element using index-based comparison.
     * 使用基于索引的比较逐元素比较两个列表。
     */
    private static void diffLists(List<?> baseList, List<?> otherList,
                                  String path, List<DiffEntry> entries, int depth) {
        int maxSize = Math.max(baseList.size(), otherList.size());
        for (int i = 0; i < maxSize; i++) {
            String indexPath = path + "[" + i + "]";
            if (i >= baseList.size()) {
                entries.add(DiffEntry.added(indexPath, otherList.get(i)));
            } else if (i >= otherList.size()) {
                entries.add(DiffEntry.removed(indexPath, baseList.get(i)));
            } else {
                diffValues(baseList.get(i), otherList.get(i), indexPath, entries, depth + 1);
            }
        }
    }

    /**
     * Builds a dot-notation path.
     * 构建点号表示法路径。
     */
    private static String buildPath(String prefix, String key) {
        return prefix.isEmpty() ? key : prefix + "." + key;
    }

    /**
     * Checks recursion depth and throws if exceeded.
     * 检查递归深度，超过时抛出异常。
     */
    private static void checkDepth(int depth) {
        if (depth > MAX_DEPTH) {
            throw new OpenYmlException(
                    "YAML diff exceeded maximum depth of " + MAX_DEPTH
                            + "; possible cyclic structure");
        }
    }
}
