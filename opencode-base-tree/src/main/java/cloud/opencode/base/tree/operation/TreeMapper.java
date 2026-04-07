package cloud.opencode.base.tree.operation;

import cloud.opencode.base.tree.Treeable;
import cloud.opencode.base.tree.exception.TreeException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Tree Mapper - Maps tree nodes to different types
 * 树映射器 - 将树节点映射为不同类型
 *
 * <p>Maps tree nodes to different types while preserving tree structure.</p>
 * <p>将树节点映射为不同类型同时保持树结构。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Map tree nodes to different types - 将树节点映射为不同类型</li>
 *   <li>Custom children setter support - 自定义子节点设置器支持</li>
 *   <li>Value extraction from tree - 从树中提取值</li>
 *   <li>Max depth protection (1000) - 最大深度保护（1000）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Map nodes to DTOs
 * List<NodeDTO> dtos = TreeMapper.map(roots, node -> new NodeDTO(node));
 *
 * // Extract all names
 * List<String> names = TreeMapper.extractAll(roots, MyNode::getName);
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
public final class TreeMapper {

    private static final int MAX_DEPTH = 1000;

    private TreeMapper() {
        // Utility class
    }

    /**
     * Map tree nodes to different type
     * 将树节点映射为不同类型
     *
     * @param roots the root nodes | 根节点列表
     * @param mapper the mapper function | 映射函数
     * @param <S> the source type | 源类型
     * @param <T> the target type | 目标类型
     * @param <ID> the ID type | ID类型
     * @return the mapped roots | 映射后的根节点
     * @throws TreeException if tree depth exceeds 1000 | 如果树深度超过1000
     */
    public static <S extends Treeable<S, ID>, T extends Treeable<T, ID>, ID> List<T> map(
            List<S> roots, Function<S, T> mapper) {
        List<T> result = new ArrayList<>();
        for (S root : roots) {
            result.add(mapNode(root, mapper, 0));
        }
        return result;
    }

    private static <S extends Treeable<S, ID>, T extends Treeable<T, ID>, ID> T mapNode(
            S source, Function<S, T> mapper, int depth) {
        if (depth > MAX_DEPTH) {
            throw TreeException.maxDepthExceeded(MAX_DEPTH);
        }
        T target = mapper.apply(source);

        List<S> children = source.getChildren();
        if (children != null && !children.isEmpty()) {
            List<T> mappedChildren = new ArrayList<>();
            for (S child : children) {
                mappedChildren.add(mapNode(child, mapper, depth + 1));
            }
            target.setChildren(mappedChildren);
        }

        return target;
    }

    /**
     * Map tree to different structure with custom children mapper
     * 使用自定义子节点映射器将树映射为不同结构
     *
     * @param roots the root nodes | 根节点列表
     * @param nodeMapper the node mapper | 节点映射器
     * @param childrenSetter the children setter | 子节点设置器
     * @param <S> the source type | 源类型
     * @param <T> the target type | 目标类型
     * @param <ID> the ID type | ID类型
     * @return the mapped list | 映射后的列表
     * @throws TreeException if tree depth exceeds 1000 | 如果树深度超过1000
     */
    public static <S extends Treeable<S, ID>, T, ID> List<T> mapToAny(
            List<S> roots,
            Function<S, T> nodeMapper,
            java.util.function.BiConsumer<T, List<T>> childrenSetter) {
        List<T> result = new ArrayList<>();
        for (S root : roots) {
            result.add(mapToAnyNode(root, nodeMapper, childrenSetter, 0));
        }
        return result;
    }

    private static <S extends Treeable<S, ID>, T, ID> T mapToAnyNode(
            S source,
            Function<S, T> nodeMapper,
            java.util.function.BiConsumer<T, List<T>> childrenSetter,
            int depth) {
        if (depth > MAX_DEPTH) {
            throw TreeException.maxDepthExceeded(MAX_DEPTH);
        }
        T target = nodeMapper.apply(source);

        List<S> children = source.getChildren();
        if (children != null && !children.isEmpty()) {
            List<T> mappedChildren = new ArrayList<>();
            for (S child : children) {
                mappedChildren.add(mapToAnyNode(child, nodeMapper, childrenSetter, depth + 1));
            }
            childrenSetter.accept(target, mappedChildren);
        }

        return target;
    }

    /**
     * Extract values from tree
     * 从树中提取值
     *
     * @param roots the root nodes | 根节点列表
     * @param extractor the value extractor | 值提取器
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @param <R> the result type | 结果类型
     * @return the extracted values | 提取的值
     * @throws TreeException if tree depth exceeds 1000 | 如果树深度超过1000
     */
    public static <T extends Treeable<T, ID>, ID, R> List<R> extractAll(
            List<T> roots, Function<T, R> extractor) {
        List<R> result = new ArrayList<>();
        for (T root : roots) {
            extractFromNode(root, extractor, result, 0);
        }
        return result;
    }

    private static <T extends Treeable<T, ID>, ID, R> void extractFromNode(
            T node, Function<T, R> extractor, List<R> result, int depth) {
        if (depth > MAX_DEPTH) {
            throw TreeException.maxDepthExceeded(MAX_DEPTH);
        }
        result.add(extractor.apply(node));

        List<T> children = node.getChildren();
        if (children != null) {
            for (T child : children) {
                extractFromNode(child, extractor, result, depth + 1);
            }
        }
    }
}
