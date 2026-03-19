package cloud.opencode.base.tree.builder;

import cloud.opencode.base.tree.Treeable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * List To Tree Converter
 * 列表转树转换器
 *
 * <p>Converts flat list to tree structure.</p>
 * <p>将扁平列表转换为树结构。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Flat list to tree conversion - 扁平列表转树结构转换</li>
 *   <li>Root ID detection (null, 0, empty) - 根ID检测（null、0、空）</li>
 *   <li>Custom extractor support - 自定义提取器支持</li>
 *   <li>Sorted tree output - 排序树输出</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Convert flat list to tree
 * List<MyNode> roots = ListToTreeConverter.convert(nodeList);
 *
 * // With specific root ID
 * List<MyNode> roots = ListToTreeConverter.convert(nodeList, 0L);
 *
 * // With sorting
 * List<MyNode> roots = ListToTreeConverter.convertSorted(
 *     nodeList, null, Comparator.comparing(MyNode::getName));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (operates on input list) - 否（操作输入列表）</li>
 *   <li>Null-safe: Yes (returns empty list for null input) - 是（null输入返回空列表）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) - single pass to build map, single pass to link parents - 时间复杂度: O(n) - 一次遍历建立映射，一次遍历关联父节点</li>
 *   <li>Space complexity: O(n) - ID-to-node map proportional to list size - 空间复杂度: O(n) - ID到节点映射与列表大小成正比</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
public final class ListToTreeConverter {

    private ListToTreeConverter() {
        // Utility class
    }

    /**
     * Convert list to tree
     * 列表转树
     *
     * @param nodes the node list | 节点列表
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the root nodes | 根节点列表
     */
    public static <T extends Treeable<T, ID>, ID> List<T> convert(List<T> nodes) {
        return convert(nodes, null);
    }

    /**
     * Convert list to tree with root ID
     * 使用根ID将列表转树
     *
     * @param nodes the node list | 节点列表
     * @param rootId the root ID | 根ID
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the root nodes | 根节点列表
     */
    public static <T extends Treeable<T, ID>, ID> List<T> convert(List<T> nodes, ID rootId) {
        if (nodes == null || nodes.isEmpty()) {
            return new ArrayList<>();
        }

        // Build ID to node map
        Map<ID, T> nodeMap = nodes.stream()
            .collect(Collectors.toMap(Treeable::getId, n -> n,
                (a, b) -> { throw new IllegalArgumentException("Duplicate node ID: " + a.getId()); }));

        List<T> roots = new ArrayList<>();

        for (T node : nodes) {
            ID parentId = node.getParentId();

            if (isRoot(parentId, rootId)) {
                roots.add(node);
            } else {
                T parent = nodeMap.get(parentId);
                if (parent != null) {
                    List<T> children = parent.getChildren();
                    if (children == null) {
                        children = new ArrayList<>();
                        parent.setChildren(children);
                    }
                    children.add(node);
                }
            }
        }

        return roots;
    }

    /**
     * Convert list to tree with comparator
     * 使用比较器将列表转树
     *
     * @param nodes the node list | 节点列表
     * @param rootId the root ID | 根ID
     * @param comparator the comparator | 比较器
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the sorted root nodes | 排序后的根节点列表
     */
    public static <T extends Treeable<T, ID>, ID> List<T> convertSorted(
            List<T> nodes, ID rootId, Comparator<T> comparator) {
        List<T> roots = convert(nodes, rootId);
        sortTree(roots, comparator);
        return roots;
    }

    /**
     * Convert using extractors
     * 使用提取器转换
     *
     * @param items the item list | 项列表
     * @param idExtractor the ID extractor | ID提取器
     * @param parentIdExtractor the parent ID extractor | 父ID提取器
     * @param nodeFactory the node factory | 节点工厂
     * @param <T> the item type | 项类型
     * @param <N> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the root nodes | 根节点列表
     */
    public static <T, N extends Treeable<N, ID>, ID> List<N> convert(
            List<T> items,
            Function<T, ID> idExtractor,
            Function<T, ID> parentIdExtractor,
            Function<T, N> nodeFactory) {

        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }

        List<N> nodes = items.stream()
            .map(nodeFactory)
            .toList();

        return convert(nodes, null);
    }

    private static <ID> boolean isRoot(ID parentId, ID rootId) {
        if (rootId != null) {
            return Objects.equals(parentId, rootId);
        }
        return parentId == null ||
            (parentId instanceof Number n && n.longValue() == 0) ||
            "".equals(parentId) ||
            "0".equals(parentId);
    }

    private static <T extends Treeable<T, ?>> void sortTree(List<T> nodes, Comparator<T> comparator) {
        if (nodes == null || nodes.isEmpty()) return;
        nodes.sort(comparator);
        for (T node : nodes) {
            sortTree(node.getChildren(), comparator);
        }
    }
}
