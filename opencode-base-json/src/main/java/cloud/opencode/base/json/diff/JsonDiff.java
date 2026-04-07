
package cloud.opencode.base.json.diff;

import cloud.opencode.base.json.JsonNode;
import cloud.opencode.base.json.exception.OpenJsonProcessingException;
import cloud.opencode.base.json.patch.JsonPatch;

import java.util.*;

/**
 * JSON Diff - JSON Document Comparison Tool
 * JSON Diff - JSON 文档比较工具
 *
 * <p>This class compares two JSON documents and generates a detailed
 * difference report. It can also produce JSON Patch (RFC 6902) output.</p>
 * <p>此类比较两个 JSON 文档并生成详细的差异报告。
 * 它还可以生成 JSON Patch（RFC 6902）输出。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * JsonNode source = OpenJson.parse("{\"name\":\"John\",\"age\":30}");
 * JsonNode target = OpenJson.parse("{\"name\":\"Jane\",\"email\":\"jane@example.com\"}");
 *
 * DiffResult result = JsonDiff.diff(source, target);
 *
 * // Iterate differences
 * for (Difference diff : result.getDifferences()) {
 *     System.out.println(diff.getType() + " at " + diff.getPath());
 * }
 *
 * // Generate JSON Patch
 * JsonPatch patch = result.toPatch();
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Deep comparison of JSON documents - JSON文档的深度比较</li>
 *   <li>Categorized differences (added, removed, changed, type changed) - 分类差异</li>
 *   <li>Automatic JSON Patch (RFC 6902) generation from diff - 从差异自动生成JSON Patch</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: N/A - 空值安全: 不适用</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
public final class JsonDiff {

    /**
     * Type of difference
     * 差异类型
     */
    public enum DiffType {
        /** Value was added - 值被添加 */
        ADDED,
        /** Value was removed - 值被移除 */
        REMOVED,
        /** Value was changed - 值被更改 */
        CHANGED,
        /** Type was changed - 类型被更改 */
        TYPE_CHANGED
    }

    /**
     * Represents a single difference between documents.
     * 表示文档之间的单个差异。
     */
    public record Difference(
            DiffType type,
            String path,
            JsonNode sourceValue,
            JsonNode targetValue
    ) {
        @Override
        public String toString() {
            return switch (type) {
                case ADDED -> "ADDED " + path + ": " + truncateValue(targetValue);
                case REMOVED -> "REMOVED " + path + ": " + truncateValue(sourceValue);
                case CHANGED -> "CHANGED " + path + ": " + truncateValue(sourceValue) + " -> " + truncateValue(targetValue);
                case TYPE_CHANGED -> "TYPE_CHANGED " + path + ": " +
                        getTypeName(sourceValue) + " -> " + getTypeName(targetValue);
            };
        }

        private String truncateValue(JsonNode node) {
            if (node == null) return "null";
            if (node.isNull()) return "null";
            if (node.isObject()) return "{...}(" + node.size() + " keys)";
            if (node.isArray()) return "[...](" + node.size() + " items)";
            String s = node.asString();
            if (s != null && s.length() > 50) return s.substring(0, 50) + "...";
            return String.valueOf(node);
        }

        private String getTypeName(JsonNode node) {
            if (node == null) return "null";
            return switch (node) {
                case JsonNode.NullNode _ -> "null";
                case JsonNode.StringNode _ -> "string";
                case JsonNode.NumberNode _ -> "number";
                case JsonNode.BooleanNode _ -> "boolean";
                case JsonNode.ArrayNode _ -> "array";
                case JsonNode.ObjectNode _ -> "object";
            };
        }
    }

    /**
     * Result of comparing two JSON documents.
     * 比较两个 JSON 文档的结果。
     */
    public record DiffResult(
            List<Difference> differences,
            JsonNode source,
            JsonNode target
    ) {
        /**
         * Returns whether the documents are identical.
         * 返回文档是否相同。
         *
         * @return true if identical - 如果相同则返回 true
         */
        public boolean isIdentical() {
            return differences.isEmpty();
        }

        /**
         * Returns whether there are differences.
         * 返回是否有差异。
         *
         * @return true if different - 如果不同则返回 true
         */
        public boolean hasDifferences() {
            return !differences.isEmpty();
        }

        /**
         * Returns the number of differences.
         * 返回差异数量。
         *
         * @return the difference count - 差异数量
         */
        public int getDifferenceCount() {
            return differences.size();
        }

        /**
         * Returns differences of a specific type.
         * 返回特定类型的差异。
         *
         * @param type the difference type - 差异类型
         * @return matching differences - 匹配的差异
         */
        public List<Difference> getDifferencesByType(DiffType type) {
            return differences.stream()
                    .filter(d -> d.type() == type)
                    .toList();
        }

        /**
         * Converts this diff result to a JSON Patch.
         * 将此差异结果转换为 JSON Patch。
         *
         * @return the JSON Patch - JSON Patch
         */
        public JsonPatch toPatch() {
            JsonPatch.Builder builder = JsonPatch.builder();
            for (Difference diff : differences) {
                switch (diff.type()) {
                    case ADDED -> builder.add(diff.path(), diff.targetValue());
                    case REMOVED -> builder.remove(diff.path());
                    case CHANGED, TYPE_CHANGED -> builder.replace(diff.path(), diff.targetValue());
                }
            }
            return builder.build();
        }

        /**
         * Returns a human-readable summary.
         * 返回人类可读的摘要。
         *
         * @return the summary - 摘要
         */
        public String getSummary() {
            if (isIdentical()) {
                return "Documents are identical";
            }
            int added = 0, removed = 0, changed = 0, typeChanged = 0;
            for (Difference d : differences) {
                switch (d.type()) {
                    case ADDED -> added++;
                    case REMOVED -> removed++;
                    case CHANGED -> changed++;
                    case TYPE_CHANGED -> typeChanged++;
                }
            }

            StringBuilder sb = new StringBuilder("Differences: ");
            List<String> parts = new ArrayList<>();
            if (added > 0) parts.add(added + " added");
            if (removed > 0) parts.add(removed + " removed");
            if (changed > 0) parts.add(changed + " changed");
            if (typeChanged > 0) parts.add(typeChanged + " type changed");
            sb.append(String.join(", ", parts));
            return sb.toString();
        }
    }

    private JsonDiff() {
        // Utility class
    }

    /**
     * Compares two JSON documents.
     * 比较两个 JSON 文档。
     *
     * @param source the source document - 源文档
     * @param target the target document - 目标文档
     * @return the diff result - 差异结果
     */
    private static final int MAX_DEPTH = 512;

    public static DiffResult diff(JsonNode source, JsonNode target) {
        List<Difference> differences = new ArrayList<>();
        compareNodes(source, target, "", differences, 0);
        return new DiffResult(Collections.unmodifiableList(differences), source, target);
    }

    /**
     * Checks if two documents are equal.
     * 检查两个文档是否相等。
     *
     * @param source the source document - 源文档
     * @param target the target document - 目标文档
     * @return true if equal - 如果相等则返回 true
     */
    public static boolean equals(JsonNode source, JsonNode target) {
        return diff(source, target).isIdentical();
    }

    private static void compareNodes(JsonNode source, JsonNode target, String path, List<Difference> differences, int depth) {
        if (depth > MAX_DEPTH) {
            throw OpenJsonProcessingException.pathError(
                    "Diff nesting depth exceeds maximum of " + MAX_DEPTH);
        }

        // Handle nulls
        if (source == null || source.isNull()) {
            if (target != null && !target.isNull()) {
                differences.add(new Difference(DiffType.ADDED, path, source, target));
            }
            return;
        }
        if (target == null || target.isNull()) {
            differences.add(new Difference(DiffType.REMOVED, path, source, target));
            return;
        }

        // Check type changes
        if (!sameType(source, target)) {
            differences.add(new Difference(DiffType.TYPE_CHANGED, path, source, target));
            return;
        }

        // Compare by type
        if (source.isObject()) {
            compareObjects(source, target, path, differences, depth);
        } else if (source.isArray()) {
            compareArrays(source, target, path, differences, depth);
        } else {
            // Scalar comparison
            if (!nodesEqual(source, target)) {
                differences.add(new Difference(DiffType.CHANGED, path, source, target));
            }
        }
    }

    private static void compareObjects(JsonNode source, JsonNode target, String path, List<Difference> differences, int depth) {
        Set<String> allKeys = new LinkedHashSet<>(source.size() + target.size());
        allKeys.addAll(source.keys());
        allKeys.addAll(target.keys());

        for (String key : allKeys) {
            String childPath = path.isEmpty() ? "/" + escapeJsonPointer(key) : path + "/" + escapeJsonPointer(key);
            JsonNode sourceChild = source.get(key);
            JsonNode targetChild = target.get(key);

            if (sourceChild == null) {
                differences.add(new Difference(DiffType.ADDED, childPath, null, targetChild));
            } else if (targetChild == null) {
                differences.add(new Difference(DiffType.REMOVED, childPath, sourceChild, null));
            } else {
                compareNodes(sourceChild, targetChild, childPath, differences, depth + 1);
            }
        }
    }

    private static void compareArrays(JsonNode source, JsonNode target, String path, List<Difference> differences, int depth) {
        int maxLen = Math.max(source.size(), target.size());

        for (int i = 0; i < maxLen; i++) {
            String childPath = path + "/" + i;
            JsonNode sourceChild = i < source.size() ? source.get(i) : null;
            JsonNode targetChild = i < target.size() ? target.get(i) : null;

            if (sourceChild == null) {
                differences.add(new Difference(DiffType.ADDED, childPath, null, targetChild));
            } else if (targetChild == null) {
                differences.add(new Difference(DiffType.REMOVED, childPath, sourceChild, null));
            } else {
                compareNodes(sourceChild, targetChild, childPath, differences, depth + 1);
            }
        }
    }

    private static boolean sameType(JsonNode a, JsonNode b) {
        return switch (a) {
            case JsonNode.ObjectNode _ -> b instanceof JsonNode.ObjectNode;
            case JsonNode.ArrayNode _ -> b instanceof JsonNode.ArrayNode;
            case JsonNode.StringNode _ -> b instanceof JsonNode.StringNode;
            case JsonNode.NumberNode _ -> b instanceof JsonNode.NumberNode;
            case JsonNode.BooleanNode _ -> b instanceof JsonNode.BooleanNode;
            case JsonNode.NullNode _ -> b instanceof JsonNode.NullNode;
        };
    }

    private static boolean nodesEqual(JsonNode a, JsonNode b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    private static String escapeJsonPointer(String token) {
        return token.replace("~", "~0").replace("/", "~1");
    }
}
