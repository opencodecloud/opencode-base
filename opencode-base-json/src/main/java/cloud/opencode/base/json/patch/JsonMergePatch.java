
package cloud.opencode.base.json.patch;

import cloud.opencode.base.json.JsonNode;
import cloud.opencode.base.json.exception.OpenJsonProcessingException;

import java.util.Objects;

/**
 * JSON Merge Patch - RFC 7396 Implementation
 * JSON Merge Patch - RFC 7396 实现
 *
 * <p>This class implements JSON Merge Patch (RFC 7396), which provides
 * a simpler way to apply partial modifications to a JSON document.</p>
 * <p>此类实现 JSON Merge Patch（RFC 7396），提供一种更简单的方式
 * 对 JSON 文档进行部分修改。</p>
 *
 * <p><strong>Rules | 规则:</strong></p>
 * <ul>
 *   <li>If patch is not an object, replace the target - 如果补丁不是对象，替换目标</li>
 *   <li>For each property in the patch object: - 对于补丁对象中的每个属性：
 *     <ul>
 *       <li>If value is null, remove the property - 如果值为 null，移除属性</li>
 *       <li>Otherwise, recursively merge - 否则，递归合并</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * JsonNode target = OpenJson.parse("{\"name\":\"John\",\"age\":30,\"city\":\"NYC\"}");
 * JsonNode patch = OpenJson.parse("{\"age\":31,\"city\":null,\"email\":\"john@example.com\"}");
 *
 * JsonNode result = JsonMergePatch.apply(target, patch);
 * // Result: {"name":"John","age":31,"email":"john@example.com"}
 *
 * // Using builder
 * JsonNode result2 = JsonMergePatch.builder()
 *     .set("name", "Jane")
 *     .remove("age")
 *     .set("email", "jane@example.com")
 *     .apply(target);
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>RFC 7396 JSON Merge Patch implementation - RFC 7396 JSON Merge Patch实现</li>
 *   <li>Recursive merge of JSON object hierarchies - JSON对象层次结构的递归合并</li>
 *   <li>Diff generation between two JSON documents - 两个JSON文档之间的差异生成</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: Partial (validates inputs) - 空值安全: 部分（验证输入）</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://tools.ietf.org/html/rfc7396">RFC 7396 - JSON Merge Patch</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
public final class JsonMergePatch {

    /**
     * The patch content
     * 补丁内容
     */
    private final JsonNode patch;

    private JsonMergePatch(JsonNode patch) {
        this.patch = Objects.requireNonNull(patch, "Patch must not be null");
    }

    /**
     * Creates a merge patch from a JsonNode.
     * 从 JsonNode 创建合并补丁。
     *
     * @param patch the patch node - 补丁节点
     * @return the merge patch - 合并补丁
     */
    public static JsonMergePatch of(JsonNode patch) {
        return new JsonMergePatch(patch);
    }

    /**
     * Creates a new builder.
     * 创建新的构建器。
     *
     * @return a new builder - 新构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Applies a merge patch to a target.
     * 将合并补丁应用于目标。
     *
     * @param target the target document - 目标文档
     * @param patch  the patch document - 补丁文档
     * @return the merged result - 合并结果
     */
    public static JsonNode apply(JsonNode target, JsonNode patch) {
        return new JsonMergePatch(patch).apply(target);
    }

    /**
     * Applies this patch to a target.
     * 将此补丁应用于目标。
     *
     * @param target the target document - 目标文档
     * @return the merged result - 合并结果
     */
    public JsonNode apply(JsonNode target) {
        return merge(target, patch);
    }

    /**
     * Returns the patch content.
     * 返回补丁内容。
     *
     * @return the patch - 补丁
     */
    public JsonNode getPatch() {
        return patch;
    }

    /**
     * Creates a diff patch between two documents.
     * 创建两个文档之间的差异补丁。
     *
     * @param source the source document - 源文档
     * @param target the target document - 目标文档
     * @return the merge patch that transforms source to target - 将源转换为目标的合并补丁
     */
    public static JsonMergePatch diff(JsonNode source, JsonNode target) {
        return new JsonMergePatch(createDiff(source, target));
    }

    private JsonNode merge(JsonNode target, JsonNode patch) {
        // If patch is not an object, it replaces the target completely
        if (!patch.isObject()) {
            return deepCopy(patch);
        }

        // If target is not an object, start with empty object
        JsonNode.ObjectNode result;
        if (target.isObject()) {
            result = (JsonNode.ObjectNode) deepCopy(target);
        } else {
            result = JsonNode.object();
        }

        // Merge patch properties
        for (String key : patch.keys()) {
            JsonNode patchValue = patch.get(key);

            if (patchValue.isNull()) {
                // Null in patch means remove property
                result.remove(key);
            } else {
                // Recursively merge
                JsonNode targetValue = result.get(key);
                if (targetValue == null) {
                    targetValue = JsonNode.nullNode();
                }
                result.put(key, merge(targetValue, patchValue));
            }
        }

        return result;
    }

    private static JsonNode createDiff(JsonNode source, JsonNode target) {
        // If target is not an object, return it directly
        if (!target.isObject()) {
            return deepCopyStatic(target);
        }

        // If source is not an object, return entire target
        if (!source.isObject()) {
            return deepCopyStatic(target);
        }

        JsonNode.ObjectNode diff = JsonNode.object();

        // Find properties to remove (in source but not in target)
        for (String key : source.keys()) {
            if (!target.has(key)) {
                diff.putNull(key);
            }
        }

        // Find properties to add/update
        for (String key : target.keys()) {
            JsonNode sourceValue = source.get(key);
            JsonNode targetValue = target.get(key);

            if (sourceValue == null) {
                // Property added
                diff.put(key, deepCopyStatic(targetValue));
            } else if (!nodesEqual(sourceValue, targetValue)) {
                // Property changed
                if (sourceValue.isObject() && targetValue.isObject()) {
                    // Recursively diff objects
                    JsonNode nested = createDiff(sourceValue, targetValue);
                    if (!nested.isEmpty()) {
                        diff.put(key, nested);
                    }
                } else {
                    diff.put(key, deepCopyStatic(targetValue));
                }
            }
        }

        return diff;
    }

    private JsonNode deepCopy(JsonNode node) {
        return deepCopyStatic(node);
    }

    private static final int MAX_COPY_DEPTH = 512;

    private static JsonNode deepCopyStatic(JsonNode node) {
        return deepCopyStatic(node, 0);
    }

    private static JsonNode deepCopyStatic(JsonNode node, int depth) {
        if (depth > MAX_COPY_DEPTH) {
            throw OpenJsonProcessingException.pathError(
                    "Deep copy nesting depth exceeds maximum of " + MAX_COPY_DEPTH);
        }
        if (node.isObject()) {
            JsonNode.ObjectNode copy = JsonNode.object();
            for (String key : node.keys()) {
                copy.put(key, deepCopyStatic(node.get(key), depth + 1));
            }
            return copy;
        } else if (node.isArray()) {
            JsonNode.ArrayNode copy = JsonNode.array();
            for (int i = 0; i < node.size(); i++) {
                copy.add(deepCopyStatic(node.get(i), depth + 1));
            }
            return copy;
        }
        return node;
    }

    private static boolean nodesEqual(JsonNode a, JsonNode b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    /**
     * Builder for JsonMergePatch.
     * JsonMergePatch 的构建器。
     */
    public static final class Builder {
        private final JsonNode.ObjectNode patch = JsonNode.object();

        private Builder() {}

        /**
         * Sets a property value.
         * 设置属性值。
         *
         * @param key   the property name - 属性名
         * @param value the value - 值
         * @return this builder - 此构建器
         */
        public Builder set(String key, JsonNode value) {
            patch.put(key, value);
            return this;
        }

        /**
         * Sets a string property value.
         * 设置字符串属性值。
         *
         * @param key   the property name - 属性名
         * @param value the value - 值
         * @return this builder - 此构建器
         */
        public Builder set(String key, String value) {
            patch.put(key, value);
            return this;
        }

        /**
         * Sets a number property value.
         * 设置数字属性值。
         *
         * @param key   the property name - 属性名
         * @param value the value - 值
         * @return this builder - 此构建器
         */
        public Builder set(String key, Number value) {
            patch.put(key, value);
            return this;
        }

        /**
         * Sets a boolean property value.
         * 设置布尔属性值。
         *
         * @param key   the property name - 属性名
         * @param value the value - 值
         * @return this builder - 此构建器
         */
        public Builder set(String key, boolean value) {
            patch.put(key, value);
            return this;
        }

        /**
         * Removes a property (sets it to null in patch).
         * 移除属性（在补丁中设置为 null）。
         *
         * @param key the property name - 属性名
         * @return this builder - 此构建器
         */
        public Builder remove(String key) {
            patch.putNull(key);
            return this;
        }

        /**
         * Builds the merge patch.
         * 构建合并补丁。
         *
         * @return the merge patch - 合并补丁
         */
        public JsonMergePatch build() {
            return new JsonMergePatch(patch);
        }

        /**
         * Builds and applies the patch to a target.
         * 构建并将补丁应用于目标。
         *
         * @param target the target document - 目标文档
         * @return the merged result - 合并结果
         */
        public JsonNode apply(JsonNode target) {
            return build().apply(target);
        }
    }
}
