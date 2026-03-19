package cloud.opencode.base.tree;

import cloud.opencode.base.tree.exception.TreeException;

import java.util.*;
import java.util.function.Function;

/**
 * Tree Builder
 * 树构建器
 *
 * <p>Utility for building trees from flat lists.</p>
 * <p>从扁平列表构建树的工具。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Build tree from flat list with ID extractors - 通过ID提取器从扁平列表构建树</li>
 *   <li>Build from nested map structure - 从嵌套Map结构构建</li>
 *   <li>Flatten tree to list with optional depth info - 将树展平为列表（可带深度信息）</li>
 *   <li>Depth limit protection against stack overflow - 深度限制防止栈溢出</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Build tree from flat list - 从扁平列表构建树
 * List<TreeNode<Item>> roots = TreeBuilder.build(items, Item::getId, Item::getParentId);
 *
 * // Build single root tree - 构建单根树
 * TreeNode<Item> root = TreeBuilder.buildSingle(items, Item::getId, Item::getParentId);
 *
 * // Flatten tree - 展平树
 * List<Item> flat = TreeBuilder.flatten(root);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Partial (null parent IDs treated as roots) - 空值安全: 部分（null父ID视为根节点）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) - build/flatten traverse all nodes once - 时间复杂度: O(n) - build/flatten 均单次遍历全部节点</li>
 *   <li>Space complexity: O(n) - node map and result list proportional to input size - 空间复杂度: O(n) - 节点映射和结果列表与输入规模成正比</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
public final class TreeBuilder {

    private TreeBuilder() {
        // Utility class
    }

    /**
     * Build tree from flat list
     * 从扁平列表构建树
     *
     * @param items the flat list | 扁平列表
     * @param idExtractor the ID extractor | ID提取器
     * @param parentIdExtractor the parent ID extractor | 父ID提取器
     * @param <T> the item type | 项类型
     * @param <ID> the ID type | ID类型
     * @return the root nodes | 根节点列表
     */
    public static <T, ID> List<TreeNode<T>> build(
        Collection<T> items,
        Function<T, ID> idExtractor,
        Function<T, ID> parentIdExtractor
    ) {
        Map<ID, TreeNode<T>> nodeMap = new HashMap<>();
        List<TreeNode<T>> roots = new ArrayList<>();

        // Create nodes
        for (T item : items) {
            nodeMap.put(idExtractor.apply(item), new TreeNode<>(item));
        }

        // Link nodes
        for (T item : items) {
            ID id = idExtractor.apply(item);
            ID parentId = parentIdExtractor.apply(item);
            TreeNode<T> node = nodeMap.get(id);

            if (parentId == null || !nodeMap.containsKey(parentId)) {
                roots.add(node);
            } else {
                TreeNode<T> parent = nodeMap.get(parentId);
                parent.addChild(node);
            }
        }

        return roots;
    }

    /**
     * Build single rooted tree
     * 构建单根树
     *
     * @param items the flat list | 扁平列表
     * @param idExtractor the ID extractor | ID提取器
     * @param parentIdExtractor the parent ID extractor | 父ID提取器
     * @param <T> the item type | 项类型
     * @param <ID> the ID type | ID类型
     * @return the root node or null | 根节点或null
     */
    public static <T, ID> TreeNode<T> buildSingle(
        Collection<T> items,
        Function<T, ID> idExtractor,
        Function<T, ID> parentIdExtractor
    ) {
        List<TreeNode<T>> roots = build(items, idExtractor, parentIdExtractor);
        return roots.isEmpty() ? null : roots.get(0);
    }

    /** Default maximum depth for tree construction from nested maps to prevent stack overflow */
    private static final int DEFAULT_MAX_DEPTH = 1000;

    /**
     * Build from nested map structure
     * 从嵌套映射结构构建
     *
     * @param data the root data | 根数据
     * @param childrenKey the children key | 子节点键
     * @return the root node | 根节点
     * @throws TreeException if max depth is exceeded | 如果超过最大深度
     */
    public static TreeNode<Map<String, Object>> buildFromMap(Map<String, Object> data, String childrenKey) {
        return buildFromMap(data, childrenKey, DEFAULT_MAX_DEPTH);
    }

    /**
     * Build from nested map structure with depth limit
     * 从嵌套映射结构构建（带深度限制）
     *
     * @param data the root data | 根数据
     * @param childrenKey the children key | 子节点键
     * @param maxDepth the maximum tree depth | 最大树深度
     * @return the root node | 根节点
     * @throws TreeException if max depth is exceeded | 如果超过最大深度
     */
    public static TreeNode<Map<String, Object>> buildFromMap(Map<String, Object> data, String childrenKey, int maxDepth) {
        if (maxDepth < 0) {
            throw new IllegalArgumentException("maxDepth must not be negative, got: " + maxDepth);
        }
        return buildFromMapInternal(data, childrenKey, 0, maxDepth);
    }

    @SuppressWarnings("unchecked")
    private static TreeNode<Map<String, Object>> buildFromMapInternal(
            Map<String, Object> data, String childrenKey, int currentDepth, int maxDepth) {
        if (currentDepth > maxDepth) {
            throw TreeException.maxDepthExceeded(maxDepth);
        }

        TreeNode<Map<String, Object>> node = new TreeNode<>(new HashMap<>(data));
        node.getData().remove(childrenKey);

        Object childrenObj = data.get(childrenKey);
        if (childrenObj instanceof List<?> children) {
            for (Object child : children) {
                if (child instanceof Map<?, ?>) {
                    node.addChild(buildFromMapInternal(
                        (Map<String, Object>) child, childrenKey, currentDepth + 1, maxDepth));
                }
            }
        }

        return node;
    }

    /**
     * Flatten tree to list
     * 将树展平为列表
     *
     * @param root the root node | 根节点
     * @param <T> the data type | 数据类型
     * @return the flattened list | 展平后的列表
     */
    public static <T> List<T> flatten(TreeNode<T> root) {
        List<T> result = new ArrayList<>();
        root.forEachPreOrder(node -> result.add(node.getData()));
        return result;
    }

    /**
     * Flatten tree to list with depth info
     * 将树展平为带深度信息的列表
     *
     * @param root the root node | 根节点
     * @param <T> the data type | 数据类型
     * @return list of node-depth pairs | 节点-深度对列表
     */
    public static <T> List<NodeWithDepth<T>> flattenWithDepth(TreeNode<T> root) {
        List<NodeWithDepth<T>> result = new ArrayList<>();
        flattenWithDepth(root, 0, result);
        return result;
    }

    private static <T> void flattenWithDepth(TreeNode<T> node, int depth, List<NodeWithDepth<T>> result) {
        if (depth > DEFAULT_MAX_DEPTH) {
            throw TreeException.maxDepthExceeded(DEFAULT_MAX_DEPTH);
        }
        result.add(new NodeWithDepth<>(node.getData(), depth));
        for (TreeNode<T> child : node.getChildren()) {
            flattenWithDepth(child, depth + 1, result);
        }
    }

    /**
     * Node with depth info
     * 带深度信息的节点
     *
     * @param data the data | 数据
     * @param depth the depth | 深度
     * @param <T> the data type | 数据类型
     */
    public record NodeWithDepth<T>(T data, int depth) {}
}
