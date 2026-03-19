package cloud.opencode.base.tree.validation;

import cloud.opencode.base.tree.Treeable;
import cloud.opencode.base.tree.exception.TreeException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Tree Node Validator
 * 树节点验证器
 *
 * <p>Validates tree nodes and structure.</p>
 * <p>验证树节点和结构。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Node ID validation (null, duplicate) - 节点ID验证（null、重复）</li>
 *   <li>Tree structure validation - 树结构验证</li>
 *   <li>Max depth validation - 最大深度验证</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ValidationResult result = TreeNodeValidator.validate(nodes);
 * if (!result.valid()) {
 *     System.out.println(result.getErrorMessage());
 * }
 * TreeNodeValidator.validateOrThrow(nodes); // throws TreeException
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 否</li>
 *   <li>Null-safe: No (nodes must not be null) - 否（节点不能为null）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
public final class TreeNodeValidator {

    private TreeNodeValidator() {
        // Utility class
    }

    /**
     * Validate tree nodes
     * 验证树节点
     *
     * @param nodes the nodes to validate | 要验证的节点
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the validation result | 验证结果
     */
    public static <T extends Treeable<T, ID>, ID> ValidationResult validate(List<T> nodes) {
        List<String> errors = new ArrayList<>();
        Set<ID> ids = new HashSet<>();

        for (T node : nodes) {
            // Check null ID
            if (node.getId() == null) {
                errors.add("Node has null ID");
                continue;
            }

            // Check duplicate ID
            if (!ids.add(node.getId())) {
                errors.add("Duplicate ID: " + node.getId());
            }
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    /**
     * Validate tree structure
     * 验证树结构
     *
     * @param roots the root nodes | 根节点
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the validation result | 验证结果
     */
    public static <T extends Treeable<T, ID>, ID> ValidationResult validateStructure(List<T> roots) {
        List<String> errors = new ArrayList<>();
        Set<ID> visited = new HashSet<>();

        for (T root : roots) {
            validateNode(root, visited, errors, 0, 1000);
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    private static <T extends Treeable<T, ID>, ID> void validateNode(
            T node, Set<ID> visited, List<String> errors, int depth, int maxDepth) {
        if (depth > maxDepth) {
            errors.add("Max depth exceeded at node: " + node.getId());
            return;
        }

        ID id = node.getId();
        if (id == null) {
            errors.add("Node has null ID at depth " + depth);
            return;
        }

        if (!visited.add(id)) {
            errors.add("Circular reference detected at node: " + id);
            return;
        }

        List<T> children = node.getChildren();
        if (children != null) {
            for (T child : children) {
                validateNode(child, visited, errors, depth + 1, maxDepth);
            }
        }

        visited.remove(id); // Allow same ID in different branches
    }

    /**
     * Validate and throw if invalid
     * 验证并在无效时抛出异常
     *
     * @param nodes the nodes to validate | 要验证的节点
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @throws TreeException if validation fails | 如果验证失败抛出异常
     */
    public static <T extends Treeable<T, ID>, ID> void validateOrThrow(List<T> nodes) {
        ValidationResult result = validate(nodes);
        if (!result.valid()) {
            throw new TreeException("Validation failed: " + String.join(", ", result.errors()));
        }
    }

    /**
     * Validation Result
     * 验证结果
     *
     * @param valid whether valid | 是否有效
     * @param errors the error messages | 错误消息列表
     */
    public record ValidationResult(boolean valid, List<String> errors) {
        public ValidationResult {
            errors = errors != null ? List.copyOf(errors) : List.of();
        }

        public static ValidationResult success() {
            return new ValidationResult(true, List.of());
        }

        public static ValidationResult failure(List<String> errors) {
            return new ValidationResult(false, errors);
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public String getErrorMessage() {
            return String.join("; ", errors);
        }
    }
}
