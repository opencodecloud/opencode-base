package cloud.opencode.base.tree.operation;

import cloud.opencode.base.tree.Treeable;
import cloud.opencode.base.tree.exception.TreeException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Tree Filter
 * 树过滤器
 *
 * <p>Filters tree nodes while preserving structure.</p>
 * <p>过滤树节点同时保持结构。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Filter with ancestor preservation - 保留祖先的过滤</li>
 *   <li>Flat filter without structure - 无结构的扁平过滤</li>
 *   <li>Depth-based filtering - 基于深度的过滤</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Filter keeping ancestors
 * List<MyNode> filtered = TreeFilter.filter(roots, n -> n.isActive());
 *
 * // Filter by depth
 * List<MyNode> shallow = TreeFilter.filterByDepth(roots, 2);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 否</li>
 *   <li>Null-safe: No (roots must not be null) - 否（根节点不能为null）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
public final class TreeFilter {

    private static final int MAX_DEPTH = 1000;

    private TreeFilter() {
        // Utility class
    }

    /**
     * Filter tree, keeping matching nodes and their ancestors
     * 过滤树，保留匹配节点及其祖先
     *
     * @param roots the root nodes | 根节点列表
     * @param predicate the filter predicate | 过滤谓词
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the filtered roots | 过滤后的根节点
     */
    public static <T extends Treeable<T, ID>, ID> List<T> filter(List<T> roots, Predicate<T> predicate) {
        return filterWithAncestors(roots, predicate);
    }

    /**
     * Filter tree keeping ancestors of matching nodes
     * 过滤树保留匹配节点的祖先
     *
     * @param roots the root nodes | 根节点列表
     * @param predicate the filter predicate | 过滤谓词
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the filtered roots | 过滤后的根节点
     */
    public static <T extends Treeable<T, ID>, ID> List<T> filterWithAncestors(
            List<T> roots, Predicate<T> predicate) {
        List<T> result = new ArrayList<>();
        for (T root : roots) {
            T filtered = filterNode(root, predicate, 0);
            if (filtered != null) {
                result.add(filtered);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Treeable<T, ID>, ID> T filterNode(T node, Predicate<T> predicate, int depth) {
        if (depth > MAX_DEPTH) {
            throw TreeException.maxDepthExceeded(MAX_DEPTH);
        }

        List<T> children = node.getChildren();
        List<T> filteredChildren = new ArrayList<>();

        if (children != null) {
            for (T child : children) {
                T filtered = filterNode(child, predicate, depth + 1);
                if (filtered != null) {
                    filteredChildren.add(filtered);
                }
            }
        }

        // Keep node if it matches or has matching descendants
        if (predicate.test(node) || !filteredChildren.isEmpty()) {
            try {
                T copy = (T) node.getClass().getDeclaredConstructor().newInstance();
                copyFields(node, copy);
                copy.setChildren(filteredChildren);
                return copy;
            } catch (Exception e) {
                // Fall back to modifying original
                node.setChildren(filteredChildren);
                return node;
            }
        }

        return null;
    }

    /**
     * Filter only matching nodes (no ancestor preservation)
     * 仅过滤匹配节点（不保留祖先）
     *
     * @param roots the root nodes | 根节点列表
     * @param predicate the filter predicate | 过滤谓词
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the matching nodes | 匹配的节点
     */
    public static <T extends Treeable<T, ID>, ID> List<T> filterFlat(
            List<T> roots, Predicate<T> predicate) {
        return TreeUtil.findAll(roots, predicate);
    }

    /**
     * Filter by depth
     * 按深度过滤
     *
     * @param roots the root nodes | 根节点列表
     * @param maxDepth the maximum depth | 最大深度
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the filtered roots | 过滤后的根节点
     */
    public static <T extends Treeable<T, ID>, ID> List<T> filterByDepth(
            List<T> roots, int maxDepth) {
        int clampedDepth = Math.min(maxDepth, MAX_DEPTH);
        List<T> result = new ArrayList<>();
        for (T root : roots) {
            result.add(filterByDepthNode(root, 0, clampedDepth));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Treeable<T, ID>, ID> T filterByDepthNode(
            T node, int currentDepth, int maxDepth) {
        if (currentDepth >= maxDepth) {
            try {
                T copy = (T) node.getClass().getDeclaredConstructor().newInstance();
                copyFields(node, copy);
                copy.setChildren(new ArrayList<>());
                return copy;
            } catch (Exception e) {
                node.setChildren(new ArrayList<>());
                return node;
            }
        }

        List<T> children = node.getChildren();
        if (children != null && !children.isEmpty()) {
            List<T> filteredChildren = new ArrayList<>();
            for (T child : children) {
                filteredChildren.add(filterByDepthNode(child, currentDepth + 1, maxDepth));
            }
            try {
                T copy = (T) node.getClass().getDeclaredConstructor().newInstance();
                copyFields(node, copy);
                copy.setChildren(filteredChildren);
                return copy;
            } catch (Exception e) {
                node.setChildren(filteredChildren);
                return node;
            }
        }

        return node;
    }

    private static <T extends Treeable<T, ID>, ID> void copyFields(T source, T target) {
        // Use reflection to copy basic fields
        try {
            var methods = source.getClass().getMethods();
            for (var method : methods) {
                String name = method.getName();
                if (name.startsWith("get") && method.getParameterCount() == 0
                    && !name.equals("getChildren") && !name.equals("getClass")) {
                    String setterName = "set" + name.substring(3);
                    try {
                        var setter = target.getClass().getMethod(setterName, method.getReturnType());
                        setter.invoke(target, method.invoke(source));
                    } catch (NoSuchMethodException ignored) {
                        // No setter, skip
                    }
                }
            }
        } catch (Exception ignored) {
            // Best effort
        }
    }
}
