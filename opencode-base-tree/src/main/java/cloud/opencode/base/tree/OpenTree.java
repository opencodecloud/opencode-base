package cloud.opencode.base.tree;

import cloud.opencode.base.tree.serialization.TreeSerializer;
import cloud.opencode.base.tree.virtual.LazyChildLoader;
import cloud.opencode.base.tree.virtual.VirtualTree;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Open Tree
 * 开放树
 *
 * <p>Main facade for tree operations.</p>
 * <p>树操作的主要门面。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Build trees from flat lists - 从扁平列表构建树</li>
 *   <li>Multiple traversal strategies (pre-order, post-order, BFS) - 多种遍历策略（前序、后序、广度优先）</li>
 *   <li>Search and filter operations - 搜索和过滤操作</li>
 *   <li>Tree statistics (depth, size, leaves) - 树统计（深度、大小、叶子节点）</li>
 *   <li>Virtual tree with lazy loading - 支持懒加载的虚拟化树</li>
 *   <li>Serialization to JSON, XML, Map - 序列化为JSON、XML、Map</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Build tree from flat list - 从扁平列表构建树
 * List<MyNode> tree = OpenTree.buildTree(flatList);
 *
 * // Find node by ID - 通过ID查找节点
 * MyNode node = OpenTree.find(tree, nodeId);
 *
 * // Serialize to JSON - 序列化为JSON
 * String json = OpenTree.toJson(tree);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Partial (null checks on input lists) - 空值安全: 部分（对输入列表有空值检查）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
public final class OpenTree {

    private OpenTree() {
        // Utility class
    }

    // ==================== TreeNode<T> based methods ====================

    public static <T> TreeNode<T> node(T data) {
        return new TreeNode<>(data);
    }

    public static <T, ID> List<TreeNode<T>> build(
        Collection<T> items,
        Function<T, ID> idExtractor,
        Function<T, ID> parentIdExtractor
    ) {
        return TreeBuilder.build(items, idExtractor, parentIdExtractor);
    }

    public static <T, ID> TreeNode<T> buildSingle(
        Collection<T> items,
        Function<T, ID> idExtractor,
        Function<T, ID> parentIdExtractor
    ) {
        return TreeBuilder.buildSingle(items, idExtractor, parentIdExtractor);
    }

    public static TreeNode<Map<String, Object>> fromMap(Map<String, Object> data, String childrenKey) {
        return TreeBuilder.buildFromMap(data, childrenKey);
    }

    public static <T> List<T> flatten(TreeNode<T> root) {
        return TreeBuilder.flatten(root);
    }

    public static <T> List<TreeBuilder.NodeWithDepth<T>> flattenWithDepth(TreeNode<T> root) {
        return TreeBuilder.flattenWithDepth(root);
    }

    public static <T> String print(TreeNode<T> root) {
        StringBuilder sb = new StringBuilder();
        print(root, "", true, sb);
        return sb.toString();
    }

    private static <T> void print(TreeNode<T> node, String prefix, boolean isLast, StringBuilder sb) {
        sb.append(prefix);
        sb.append(isLast ? "└── " : "├── ");
        sb.append(node.getData());
        sb.append("\n");

        List<TreeNode<T>> children = node.getChildren();
        for (int i = 0; i < children.size(); i++) {
            print(children.get(i), prefix + (isLast ? "    " : "│   "), i == children.size() - 1, sb);
        }
    }

    public static <T> void printToConsole(TreeNode<T> root) {
        System.out.println(print(root));
    }

    // ==================== Treeable<T, ID> based methods ====================

    /**
     * Build tree from flat list (default root detection)
     * 从扁平列表构建树（默认根节点检测）
     *
     * @param nodes the flat list | 扁平列表
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the root nodes | 根节点列表
     */
    public static <T extends Treeable<T, ID>, ID> List<T> buildTree(List<T> nodes) {
        return buildTree(nodes, null);
    }

    /**
     * Build tree from flat list with specified root ID
     * 从扁平列表构建树（指定根节点ID）
     *
     * @param nodes the flat list | 扁平列表
     * @param rootId the root ID | 根节点ID
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the root nodes | 根节点列表
     */
    public static <T extends Treeable<T, ID>, ID> List<T> buildTree(List<T> nodes, ID rootId) {
        if (nodes == null || nodes.isEmpty()) {
            return new ArrayList<>();
        }

        Map<ID, T> nodeMap = nodes.stream()
            .collect(Collectors.toMap(Treeable::getId, n -> n, (a, b) -> a));

        List<T> roots = new ArrayList<>();

        for (T node : nodes) {
            ID parentId = node.getParentId();

            if (isRoot(parentId, rootId)) {
                roots.add(node);
            } else {
                T parent = nodeMap.get(parentId);
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(node);
                }
            }
        }

        return roots;
    }

    /**
     * Build sorted tree
     * 构建排序树
     *
     * @param nodes the flat list | 扁平列表
     * @param rootId the root ID | 根节点ID
     * @param comparator the comparator | 比较器
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the sorted root nodes | 排序后的根节点列表
     */
    public static <T extends Treeable<T, ID>, ID> List<T> buildTreeSorted(
            List<T> nodes, ID rootId, Comparator<T> comparator) {
        List<T> tree = buildTree(nodes, rootId);
        sortTree(tree, comparator);
        return tree;
    }

    /**
     * Build tree from maps
     * 从Map列表构建树
     *
     * @param maps the map list | Map列表
     * @param idKey the ID key | ID键
     * @param parentIdKey the parent ID key | 父ID键
     * @param nameKey the name key | 名称键
     * @return the root nodes | 根节点列表
     */
    public static List<DefaultTreeNode<Object>> buildTreeFromMaps(
            List<Map<String, Object>> maps,
            String idKey, String parentIdKey, String nameKey) {

        List<DefaultTreeNode<Object>> nodes = maps.stream()
            .map(m -> {
                DefaultTreeNode<Object> node = new DefaultTreeNode<>();
                node.setId(m.get(idKey));
                node.setParentId(m.get(parentIdKey));
                node.setName(String.valueOf(m.get(nameKey)));
                node.setExtra(new HashMap<>(m));
                return node;
            })
            .toList();

        return buildTree(new ArrayList<>(nodes), null);
    }

    // ==================== Traversal methods ====================

    /**
     * Pre-order traversal
     * 前序遍历
     *
     * @param roots the root nodes | 根节点列表
     * @param visitor the visitor | 访问者
     * @param <T> the node type | 节点类型
     */
    public static <T extends Treeable<T, ?>> void traversePreOrder(List<T> roots, Consumer<T> visitor) {
        for (T root : roots) {
            traversePreOrderNode(root, visitor);
        }
    }

    private static <T extends Treeable<T, ?>> void traversePreOrderNode(T node, Consumer<T> visitor) {
        visitor.accept(node);
        if (node.getChildren() != null) {
            for (T child : node.getChildren()) {
                traversePreOrderNode(child, visitor);
            }
        }
    }

    /**
     * Post-order traversal
     * 后序遍历
     *
     * @param roots the root nodes | 根节点列表
     * @param visitor the visitor | 访问者
     * @param <T> the node type | 节点类型
     */
    public static <T extends Treeable<T, ?>> void traversePostOrder(List<T> roots, Consumer<T> visitor) {
        for (T root : roots) {
            traversePostOrderNode(root, visitor);
        }
    }

    private static <T extends Treeable<T, ?>> void traversePostOrderNode(T node, Consumer<T> visitor) {
        if (node.getChildren() != null) {
            for (T child : node.getChildren()) {
                traversePostOrderNode(child, visitor);
            }
        }
        visitor.accept(node);
    }

    /**
     * Breadth-first (level order) traversal
     * 广度优先（层序）遍历
     *
     * @param roots the root nodes | 根节点列表
     * @param visitor the visitor | 访问者
     * @param <T> the node type | 节点类型
     */
    public static <T extends Treeable<T, ?>> void traverseBreadthFirst(List<T> roots, Consumer<T> visitor) {
        Deque<T> queue = new LinkedList<>(roots);
        while (!queue.isEmpty()) {
            T node = queue.poll();
            visitor.accept(node);
            if (node.getChildren() != null) {
                queue.addAll(node.getChildren());
            }
        }
    }

    /**
     * Traverse with depth information
     * 带深度信息的遍历
     *
     * @param roots the root nodes | 根节点列表
     * @param visitor the visitor | 访问者
     * @param <T> the node type | 节点类型
     */
    public static <T extends Treeable<T, ?>> void traverseWithDepth(List<T> roots, BiConsumer<T, Integer> visitor) {
        for (T root : roots) {
            traverseWithDepthNode(root, 0, visitor);
        }
    }

    private static <T extends Treeable<T, ?>> void traverseWithDepthNode(
            T node, int depth, BiConsumer<T, Integer> visitor) {
        visitor.accept(node, depth);
        if (node.getChildren() != null) {
            for (T child : node.getChildren()) {
                traverseWithDepthNode(child, depth + 1, visitor);
            }
        }
    }

    // ==================== Search methods ====================

    /**
     * Find node by ID
     * 按ID查找节点
     *
     * @param roots the root nodes | 根节点列表
     * @param id the ID to find | 要查找的ID
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the found node or null | 找到的节点或null
     */
    public static <T extends Treeable<T, ID>, ID> T find(List<T> roots, ID id) {
        for (T root : roots) {
            T found = findInNode(root, id);
            if (found != null) return found;
        }
        return null;
    }

    private static <T extends Treeable<T, ID>, ID> T findInNode(T node, ID id) {
        if (Objects.equals(node.getId(), id)) {
            return node;
        }
        if (node.getChildren() != null) {
            for (T child : node.getChildren()) {
                T found = findInNode(child, id);
                if (found != null) return found;
            }
        }
        return null;
    }

    /**
     * Find all nodes matching predicate
     * 查找所有满足条件的节点
     *
     * @param roots the root nodes | 根节点列表
     * @param predicate the predicate | 谓词
     * @param <T> the node type | 节点类型
     * @return the matching nodes | 匹配的节点列表
     */
    public static <T extends Treeable<T, ?>> List<T> findAll(List<T> roots, Predicate<T> predicate) {
        List<T> result = new ArrayList<>();
        traversePreOrder(roots, node -> {
            if (predicate.test(node)) {
                result.add(node);
            }
        });
        return result;
    }

    /**
     * Get path from root to specified node
     * 获取从根到指定节点的路径
     *
     * @param roots the root nodes | 根节点列表
     * @param id the target node ID | 目标节点ID
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the path from root to node | 从根到节点的路径
     */
    public static <T extends Treeable<T, ID>, ID> List<T> getPath(List<T> roots, ID id) {
        for (T root : roots) {
            List<T> path = new ArrayList<>();
            if (findPath(root, id, path)) {
                return path;
            }
        }
        return new ArrayList<>();
    }

    private static <T extends Treeable<T, ID>, ID> boolean findPath(T node, ID id, List<T> path) {
        path.add(node);
        if (Objects.equals(node.getId(), id)) {
            return true;
        }
        if (node.getChildren() != null) {
            for (T child : node.getChildren()) {
                if (findPath(child, id, path)) {
                    return true;
                }
            }
        }
        path.remove(path.size() - 1);
        return false;
    }

    /**
     * Get all leaf nodes
     * 获取所有叶子节点
     *
     * @param roots the root nodes | 根节点列表
     * @param <T> the node type | 节点类型
     * @return the leaf nodes | 叶子节点列表
     */
    public static <T extends Treeable<T, ?>> List<T> getLeaves(List<T> roots) {
        return findAll(roots, node -> node.getChildren() == null || node.getChildren().isEmpty());
    }

    // ==================== Operation methods ====================

    /**
     * Filter tree (keep matching nodes and their ancestors)
     * 过滤树（保留匹配节点及其祖先）
     *
     * @param roots the root nodes | 根节点列表
     * @param predicate the predicate | 谓词
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the filtered root nodes | 过滤后的根节点列表
     */
    public static <T extends Treeable<T, ID>, ID> List<T> filter(List<T> roots, Predicate<T> predicate) {
        List<T> result = new ArrayList<>();
        for (T root : roots) {
            T filtered = filterNode(root, predicate);
            if (filtered != null) {
                result.add(filtered);
            }
        }
        return result;
    }

    private static <T extends Treeable<T, ID>, ID> T filterNode(T node, Predicate<T> predicate) {
        List<T> filteredChildren = new ArrayList<>();
        if (node.getChildren() != null) {
            for (T child : node.getChildren()) {
                T filtered = filterNode(child, predicate);
                if (filtered != null) {
                    filteredChildren.add(filtered);
                }
            }
        }

        if (predicate.test(node) || !filteredChildren.isEmpty()) {
            node.setChildren(filteredChildren);
            return node;
        }
        return null;
    }

    /**
     * Flatten tree to list
     * 将树扁平化为列表
     *
     * @param roots the root nodes | 根节点列表
     * @param <T> the node type | 节点类型
     * @return the flattened list | 扁平化后的列表
     */
    public static <T extends Treeable<T, ?>> List<T> flattenTree(List<T> roots) {
        List<T> result = new ArrayList<>();
        traversePreOrder(roots, result::add);
        return result;
    }

    /**
     * Calculate tree depth
     * 计算树深度
     *
     * @param roots the root nodes | 根节点列表
     * @param <T> the node type | 节点类型
     * @return the maximum depth | 最大深度
     */
    public static <T extends Treeable<T, ?>> int depth(List<T> roots) {
        int maxDepth = 0;
        for (T root : roots) {
            maxDepth = Math.max(maxDepth, depthOfNode(root));
        }
        return maxDepth;
    }

    private static <T extends Treeable<T, ?>> int depthOfNode(T node) {
        if (node.getChildren() == null || node.getChildren().isEmpty()) {
            return 1;
        }
        int maxChildDepth = 0;
        for (T child : node.getChildren()) {
            maxChildDepth = Math.max(maxChildDepth, depthOfNode(child));
        }
        return maxChildDepth + 1;
    }

    /**
     * Calculate total node count
     * 计算节点总数
     *
     * @param roots the root nodes | 根节点列表
     * @param <T> the node type | 节点类型
     * @return the total count | 总数
     */
    public static <T extends Treeable<T, ?>> int size(List<T> roots) {
        int[] count = {0};
        traversePreOrder(roots, node -> count[0]++);
        return count[0];
    }

    /**
     * Sort tree recursively
     * 递归排序树
     *
     * @param nodes the nodes to sort | 要排序的节点
     * @param comparator the comparator | 比较器
     * @param <T> the node type | 节点类型
     */
    public static <T extends Treeable<T, ?>> void sortTree(List<T> nodes, Comparator<T> comparator) {
        nodes.sort(comparator);
        for (T node : nodes) {
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                sortTree(node.getChildren(), comparator);
            }
        }
    }

    // ==================== Helper methods ====================

    private static <ID> boolean isRoot(ID parentId, ID rootId) {
        if (rootId != null) {
            return Objects.equals(parentId, rootId);
        }
        return parentId == null ||
               (parentId instanceof Number n && n.longValue() == 0) ||
               "".equals(parentId) ||
               "0".equals(parentId);
    }

    // ==================== Virtual Tree methods | 虚拟化树方法 ====================

    /**
     * Create a virtual tree root with lazy loading
     * 创建支持懒加载的虚拟树根节点
     *
     * @param <T> the data type | 数据类型
     * @param <ID> the ID type | ID类型
     * @param id the root ID | 根节点ID
     * @param data the root data | 根节点数据
     * @param childLoader the lazy child loader | 懒加载器
     * @return the virtual tree root | 虚拟树根节点
     */
    public static <T, ID> VirtualTree<T, ID> virtualTree(
            ID id, T data, LazyChildLoader<VirtualTree<T, ID>, ID> childLoader) {
        return VirtualTree.root(id, data, childLoader);
    }

    /**
     * Create a virtual tree builder
     * 创建虚拟树构建器
     *
     * @param <T> the data type | 数据类型
     * @param <ID> the ID type | ID类型
     * @return the builder | 构建器
     */
    public static <T, ID> VirtualTree.Builder<T, ID> virtualTreeBuilder() {
        return VirtualTree.builder();
    }

    /**
     * Preload virtual tree to specified depth
     * 预加载虚拟树到指定深度
     *
     * @param <T> the data type | 数据类型
     * @param <ID> the ID type | ID类型
     * @param root the virtual tree root | 虚拟树根节点
     * @param depth the depth to preload | 预加载深度
     */
    public static <T, ID> void preloadVirtualTree(VirtualTree<T, ID> root, int depth) {
        root.preload(depth);
    }

    // ==================== Serialization methods | 序列化方法 ====================

    /**
     * Serialize tree to JSON
     * 将树序列化为JSON
     *
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @param roots the root nodes | 根节点列表
     * @return the JSON string | JSON字符串
     */
    public static <T extends Treeable<T, ID>, ID> String toJson(List<T> roots) {
        return TreeSerializer.toJson(roots);
    }

    /**
     * Serialize tree to JSON with custom field extractor
     * 使用自定义字段提取器将树序列化为JSON
     *
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @param roots the root nodes | 根节点列表
     * @param fieldExtractor the field extractor | 字段提取器
     * @return the JSON string | JSON字符串
     */
    public static <T extends Treeable<T, ID>, ID> String toJson(
            List<T> roots, Function<T, Map<String, Object>> fieldExtractor) {
        TreeSerializer.SerializerConfig config = TreeSerializer.SerializerConfig.builder()
                .fieldExtractor(fieldExtractor)
                .build();
        return TreeSerializer.toJson(roots, config);
    }

    /**
     * Serialize tree to XML
     * 将树序列化为XML
     *
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @param roots the root nodes | 根节点列表
     * @return the XML string | XML字符串
     */
    public static <T extends Treeable<T, ID>, ID> String toXml(List<T> roots) {
        return TreeSerializer.toXml(roots);
    }

    /**
     * Serialize tree to XML with custom field extractor
     * 使用自定义字段提取器将树序列化为XML
     *
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @param roots the root nodes | 根节点列表
     * @param fieldExtractor the field extractor | 字段提取器
     * @return the XML string | XML字符串
     */
    public static <T extends Treeable<T, ID>, ID> String toXml(
            List<T> roots, Function<T, Map<String, Object>> fieldExtractor) {
        TreeSerializer.SerializerConfig config = TreeSerializer.SerializerConfig.builder()
                .fieldExtractor(fieldExtractor)
                .build();
        return TreeSerializer.toXml(roots, config);
    }

    /**
     * Convert tree to list of maps
     * 将树转换为Map列表
     *
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @param roots the root nodes | 根节点列表
     * @return the map list | Map列表
     */
    public static <T extends Treeable<T, ID>, ID> List<Map<String, Object>> toMaps(List<T> roots) {
        return TreeSerializer.toMaps(roots);
    }

    /**
     * Convert tree to flat list of maps (without hierarchy)
     * 将树扁平化为Map列表（无层级）
     *
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @param roots the root nodes | 根节点列表
     * @return the flat map list | 扁平Map列表
     */
    public static <T extends Treeable<T, ID>, ID> List<Map<String, Object>> toFlatMaps(List<T> roots) {
        return TreeSerializer.toFlatMaps(roots);
    }

    /**
     * Serialize TreeNode to JSON
     * 将TreeNode序列化为JSON
     *
     * @param <T> the data type | 数据类型
     * @param root the root node | 根节点
     * @param dataSerializer function to serialize data | 数据序列化函数
     * @return the JSON string | JSON字符串
     */
    public static <T> String treeNodeToJson(TreeNode<T> root, Function<T, Map<String, Object>> dataSerializer) {
        return TreeSerializer.treeNodeToJson(root, dataSerializer);
    }

    /**
     * Serialize TreeNode to XML
     * 将TreeNode序列化为XML
     *
     * @param <T> the data type | 数据类型
     * @param root the root node | 根节点
     * @param dataSerializer function to serialize data | 数据序列化函数
     * @return the XML string | XML字符串
     */
    public static <T> String treeNodeToXml(TreeNode<T> root, Function<T, Map<String, Object>> dataSerializer) {
        return TreeSerializer.treeNodeToXml(root, dataSerializer);
    }
}
