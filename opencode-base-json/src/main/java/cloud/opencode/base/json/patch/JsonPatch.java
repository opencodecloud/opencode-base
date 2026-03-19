
package cloud.opencode.base.json.patch;

import cloud.opencode.base.json.JsonNode;
import cloud.opencode.base.json.exception.OpenJsonProcessingException;
import cloud.opencode.base.json.path.JsonPointer;

import java.util.*;

/**
 * JSON Patch - RFC 6902 Implementation
 * JSON Patch - RFC 6902 实现
 *
 * <p>This class implements JSON Patch (RFC 6902), which defines a format
 * for expressing a sequence of operations to apply to a JSON document.</p>
 * <p>此类实现 JSON Patch（RFC 6902），定义了一种格式，
 * 用于表示应用于 JSON 文档的操作序列。</p>
 *
 * <p><strong>Operations | 操作:</strong></p>
 * <ul>
 *   <li>{@code add} - Add a value - 添加值</li>
 *   <li>{@code remove} - Remove a value - 移除值</li>
 *   <li>{@code replace} - Replace a value - 替换值</li>
 *   <li>{@code move} - Move a value - 移动值</li>
 *   <li>{@code copy} - Copy a value - 复制值</li>
 *   <li>{@code test} - Test a value - 测试值</li>
 * </ul>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * JsonNode target = OpenJson.parse("{\"name\":\"John\",\"age\":30}");
 *
 * JsonPatch patch = JsonPatch.builder()
 *     .replace("/name", JsonNode.of("Jane"))
 *     .add("/email", JsonNode.of("jane@example.com"))
 *     .remove("/age")
 *     .build();
 *
 * JsonNode result = patch.apply(target);
 * // Result: {"name":"Jane","email":"jane@example.com"}
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>RFC 6902 JSON Patch implementation - RFC 6902 JSON Patch实现</li>
 *   <li>Six patch operations: add, remove, replace, move, copy, test - 六种补丁操作</li>
 *   <li>Builder pattern for constructing patch sequences - 构建补丁序列的构建器模式</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: N/A - 空值安全: 不适用</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://tools.ietf.org/html/rfc6902">RFC 6902 - JSON Patch</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
public final class JsonPatch {

    /**
     * Patch operation type
     * 补丁操作类型
     */
    public enum Operation {
        ADD, REMOVE, REPLACE, MOVE, COPY, TEST
    }

    /**
     * Single patch operation
     * 单个补丁操作
     */
    public record PatchOperation(
            Operation op,
            String path,
            String from,
            JsonNode value
    ) {
        public PatchOperation(Operation op, String path, JsonNode value) {
            this(op, path, null, value);
        }

        public PatchOperation(Operation op, String path) {
            this(op, path, null, null);
        }

        public PatchOperation(Operation op, String path, String from) {
            this(op, path, from, null);
        }
    }

    /**
     * The list of operations
     * 操作列表
     */
    private final List<PatchOperation> operations;

    private JsonPatch(List<PatchOperation> operations) {
        this.operations = List.copyOf(operations);
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
     * Creates a patch from a list of operations.
     * 从操作列表创建补丁。
     *
     * @param operations the operations - 操作
     * @return the patch - 补丁
     */
    public static JsonPatch of(List<PatchOperation> operations) {
        return new JsonPatch(operations);
    }

    /**
     * Applies this patch to a JSON document.
     * 将此补丁应用于 JSON 文档。
     *
     * @param target the target document - 目标文档
     * @return the patched document - 打补丁后的文档
     * @throws OpenJsonProcessingException if patch fails - 如果补丁失败
     */
    public JsonNode apply(JsonNode target) {
        Objects.requireNonNull(target, "Target must not be null");

        JsonNode result = deepCopy(target);
        for (PatchOperation op : operations) {
            result = applyOperation(result, op);
        }
        return result;
    }

    /**
     * Validates this patch against a target without applying.
     * 验证此补丁对目标的有效性但不应用。
     *
     * @param target the target document - 目标文档
     * @return true if patch would succeed - 如果补丁会成功则返回 true
     */
    public boolean validate(JsonNode target) {
        try {
            apply(target);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns the operations in this patch.
     * 返回此补丁中的操作。
     *
     * @return the operations - 操作
     */
    public List<PatchOperation> getOperations() {
        return operations;
    }

    /**
     * Returns the number of operations.
     * 返回操作数量。
     *
     * @return the operation count - 操作数量
     */
    public int size() {
        return operations.size();
    }

    private JsonNode applyOperation(JsonNode target, PatchOperation op) {
        return switch (op.op()) {
            case ADD -> applyAdd(target, op.path(), op.value());
            case REMOVE -> applyRemove(target, op.path());
            case REPLACE -> applyReplace(target, op.path(), op.value());
            case MOVE -> applyMove(target, op.from(), op.path());
            case COPY -> applyCopy(target, op.from(), op.path());
            case TEST -> applyTest(target, op.path(), op.value());
        };
    }

    private JsonNode applyAdd(JsonNode target, String path, JsonNode value) {
        JsonPointer pointer = JsonPointer.parse(path);

        if (pointer.isRoot()) {
            return value;
        }

        JsonPointer parent = pointer.parent();
        String token = pointer.getLastToken();
        JsonNode parentNode = parent.evaluate(target);

        if (parentNode.isObject()) {
            ((JsonNode.ObjectNode) parentNode).put(token, value);
        } else if (parentNode.isArray()) {
            JsonNode.ArrayNode array = (JsonNode.ArrayNode) parentNode;
            if ("-".equals(token)) {
                array.add(value);
            } else {
                int index = parseArrayIndex(token, array.size() + 1);
                insertAt(array, index, value);
            }
        } else {
            throw OpenJsonProcessingException.pathError(
                    "Cannot add to non-container at: " + parent);
        }

        return target;
    }

    private JsonNode applyRemove(JsonNode target, String path) {
        JsonPointer pointer = JsonPointer.parse(path);

        if (pointer.isRoot()) {
            throw OpenJsonProcessingException.pathError("Cannot remove root element");
        }

        JsonPointer parent = pointer.parent();
        String token = pointer.getLastToken();
        JsonNode parentNode = parent.evaluate(target);

        if (parentNode.isObject()) {
            if (!parentNode.has(token)) {
                throw OpenJsonProcessingException.pathError(
                        "Property not found: " + path);
            }
            ((JsonNode.ObjectNode) parentNode).remove(token);
        } else if (parentNode.isArray()) {
            JsonNode.ArrayNode array = (JsonNode.ArrayNode) parentNode;
            int index = parseArrayIndex(token, array.size());
            array.remove(index);
        } else {
            throw OpenJsonProcessingException.pathError(
                    "Cannot remove from non-container at: " + parent);
        }

        return target;
    }

    private JsonNode applyReplace(JsonNode target, String path, JsonNode value) {
        JsonPointer pointer = JsonPointer.parse(path);

        if (pointer.isRoot()) {
            return value;
        }

        // Verify path exists
        if (!pointer.exists(target)) {
            throw OpenJsonProcessingException.pathError(
                    "Path not found for replace: " + path);
        }

        JsonPointer parent = pointer.parent();
        String token = pointer.getLastToken();
        JsonNode parentNode = parent.evaluate(target);

        if (parentNode.isObject()) {
            ((JsonNode.ObjectNode) parentNode).put(token, value);
        } else if (parentNode.isArray()) {
            int index = parseArrayIndex(token, parentNode.size());
            ((JsonNode.ArrayNode) parentNode).set(index, value);
        }

        return target;
    }

    private JsonNode applyMove(JsonNode target, String from, String path) {
        JsonPointer fromPointer = JsonPointer.parse(from);
        JsonNode value = fromPointer.evaluate(target);

        target = applyRemove(target, from);
        return applyAdd(target, path, deepCopy(value));
    }

    private JsonNode applyCopy(JsonNode target, String from, String path) {
        JsonPointer fromPointer = JsonPointer.parse(from);
        JsonNode value = fromPointer.evaluate(target);
        return applyAdd(target, path, deepCopy(value));
    }

    private JsonNode applyTest(JsonNode target, String path, JsonNode expected) {
        JsonPointer pointer = JsonPointer.parse(path);
        JsonNode actual = pointer.evaluate(target);

        if (!nodesEqual(actual, expected)) {
            throw OpenJsonProcessingException.pathError(
                    "Test failed at " + path + ": expected " + expected + ", got " + actual);
        }
        return target;
    }

    private int parseArrayIndex(String token, int size) {
        try {
            int index = Integer.parseInt(token);
            if (index < 0 || index >= size) {
                throw OpenJsonProcessingException.pathError(
                        "Array index out of bounds: " + index);
            }
            return index;
        } catch (NumberFormatException e) {
            throw OpenJsonProcessingException.pathError(
                    "Invalid array index: " + token);
        }
    }

    private void insertAt(JsonNode.ArrayNode array, int index, JsonNode value) {
        // Ensure the list is mutable before modification
        List<JsonNode> elements = new ArrayList<>(array.toList());
        elements.add(index, value);

        // Clear and rebuild
        while (array.size() > 0) {
            array.remove(array.size() - 1);
        }
        for (JsonNode element : elements) {
            array.add(element);
        }
    }

    private JsonNode deepCopy(JsonNode node) {
        if (node.isObject()) {
            JsonNode.ObjectNode copy = JsonNode.object();
            for (String key : node.keys()) {
                copy.put(key, deepCopy(node.get(key)));
            }
            return copy;
        } else if (node.isArray()) {
            JsonNode.ArrayNode copy = JsonNode.array();
            for (int i = 0; i < node.size(); i++) {
                copy.add(deepCopy(node.get(i)));
            }
            return copy;
        }
        return node;
    }

    private boolean nodesEqual(JsonNode a, JsonNode b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    /**
     * Builder for JsonPatch.
     * JsonPatch 的构建器。
     */
    public static final class Builder {
        private final List<PatchOperation> operations = new ArrayList<>();

        private Builder() {}

        /**
         * Adds an add operation.
         * 添加一个 add 操作。
         *
         * @param path  the JSON Pointer path - JSON Pointer 路径
         * @param value the value to add - 要添加的值
         * @return this builder - 此构建器
         */
        public Builder add(String path, JsonNode value) {
            operations.add(new PatchOperation(Operation.ADD, path, value));
            return this;
        }

        /**
         * Adds a remove operation.
         * 添加一个 remove 操作。
         *
         * @param path the JSON Pointer path - JSON Pointer 路径
         * @return this builder - 此构建器
         */
        public Builder remove(String path) {
            operations.add(new PatchOperation(Operation.REMOVE, path));
            return this;
        }

        /**
         * Adds a replace operation.
         * 添加一个 replace 操作。
         *
         * @param path  the JSON Pointer path - JSON Pointer 路径
         * @param value the new value - 新值
         * @return this builder - 此构建器
         */
        public Builder replace(String path, JsonNode value) {
            operations.add(new PatchOperation(Operation.REPLACE, path, value));
            return this;
        }

        /**
         * Adds a move operation.
         * 添加一个 move 操作。
         *
         * @param from the source path - 源路径
         * @param path the target path - 目标路径
         * @return this builder - 此构建器
         */
        public Builder move(String from, String path) {
            operations.add(new PatchOperation(Operation.MOVE, path, from));
            return this;
        }

        /**
         * Adds a copy operation.
         * 添加一个 copy 操作。
         *
         * @param from the source path - 源路径
         * @param path the target path - 目标路径
         * @return this builder - 此构建器
         */
        public Builder copy(String from, String path) {
            operations.add(new PatchOperation(Operation.COPY, path, from));
            return this;
        }

        /**
         * Adds a test operation.
         * 添加一个 test 操作。
         *
         * @param path     the JSON Pointer path - JSON Pointer 路径
         * @param expected the expected value - 预期值
         * @return this builder - 此构建器
         */
        public Builder test(String path, JsonNode expected) {
            operations.add(new PatchOperation(Operation.TEST, path, expected));
            return this;
        }

        /**
         * Builds the patch.
         * 构建补丁。
         *
         * @return the patch - 补丁
         */
        public JsonPatch build() {
            return new JsonPatch(operations);
        }
    }
}
