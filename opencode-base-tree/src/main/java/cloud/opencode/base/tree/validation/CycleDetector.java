package cloud.opencode.base.tree.validation;

import cloud.opencode.base.tree.Treeable;
import cloud.opencode.base.tree.exception.CycleDetectedException;

import java.util.*;

/**
 * Cycle Detector
 * 循环检测器
 *
 * <p>Detects cycles in tree or graph structures.</p>
 * <p>检测树或图结构中的循环。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Cycle detection in tree structures - 树结构中的循环检测</li>
 *   <li>Cycle path extraction - 循环路径提取</li>
 *   <li>Flat list cycle detection - 扁平列表循环检测</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * boolean hasCycle = CycleDetector.hasCycle(roots);
 * Optional<List<ID>> path = CycleDetector.findCyclePath(roots);
 * CycleDetector.checkNoCycle(roots); // throws CycleDetectedException
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
public final class CycleDetector {

    private CycleDetector() {
        // Utility class
    }

    /**
     * Check if tree has cycles
     * 检查树是否有循环
     *
     * @param roots the root nodes | 根节点
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return true if has cycle | 如果有循环返回true
     */
    public static <T extends Treeable<T, ID>, ID> boolean hasCycle(List<T> roots) {
        Set<ID> visiting = new HashSet<>();
        Set<ID> visited = new HashSet<>();

        for (T root : roots) {
            if (hasCycleInNode(root, visiting, visited)) {
                return true;
            }
        }
        return false;
    }

    private static <T extends Treeable<T, ID>, ID> boolean hasCycleInNode(
            T node, Set<ID> visiting, Set<ID> visited) {
        ID id = node.getId();
        if (id == null) return false;

        if (visiting.contains(id)) {
            return true; // Cycle detected
        }

        if (visited.contains(id)) {
            return false; // Already processed
        }

        visiting.add(id);

        List<T> children = node.getChildren();
        if (children != null) {
            for (T child : children) {
                if (hasCycleInNode(child, visiting, visited)) {
                    return true;
                }
            }
        }

        visiting.remove(id);
        visited.add(id);
        return false;
    }

    /**
     * Detect cycle and return path
     * 检测循环并返回路径
     *
     * @param roots the root nodes | 根节点
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the cycle path if found | 如果找到返回循环路径
     */
    public static <T extends Treeable<T, ID>, ID> Optional<List<ID>> findCyclePath(List<T> roots) {
        Set<ID> visiting = new HashSet<>();
        Set<ID> visited = new HashSet<>();
        List<ID> path = new ArrayList<>();

        for (T root : roots) {
            List<ID> cyclePath = findCycleInNode(root, visiting, visited, path);
            if (cyclePath != null) {
                return Optional.of(cyclePath);
            }
        }
        return Optional.empty();
    }

    private static <T extends Treeable<T, ID>, ID> List<ID> findCycleInNode(
            T node, Set<ID> visiting, Set<ID> visited, List<ID> path) {
        ID id = node.getId();
        if (id == null) return null;

        if (visiting.contains(id)) {
            // Found cycle, extract cycle path
            List<ID> cyclePath = new ArrayList<>();
            int idx = path.indexOf(id);
            for (int i = idx; i < path.size(); i++) {
                cyclePath.add(path.get(i));
            }
            cyclePath.add(id); // Complete the cycle
            return cyclePath;
        }

        if (visited.contains(id)) {
            return null;
        }

        visiting.add(id);
        path.add(id);

        List<T> children = node.getChildren();
        if (children != null) {
            for (T child : children) {
                List<ID> cyclePath = findCycleInNode(child, visiting, visited, path);
                if (cyclePath != null) {
                    return cyclePath;
                }
            }
        }

        visiting.remove(id);
        path.removeLast();
        visited.add(id);
        return null;
    }

    /**
     * Check for cycle and throw if found
     * 检查循环，如果发现则抛出异常
     *
     * @param roots the root nodes | 根节点
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @throws CycleDetectedException if cycle is found | 如果发现循环抛出异常
     */
    public static <T extends Treeable<T, ID>, ID> void checkNoCycle(List<T> roots) {
        Optional<List<ID>> cyclePath = findCyclePath(roots);
        if (cyclePath.isPresent()) {
            throw new CycleDetectedException(cyclePath.get());
        }
    }

    /**
     * Check flat list for potential cycles
     * 检查扁平列表中的潜在循环
     *
     * @param nodes the nodes to check | 要检查的节点
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return true if potential cycle exists | 如果存在潜在循环返回true
     */
    public static <T extends Treeable<T, ID>, ID> boolean hasPotentialCycle(List<T> nodes) {
        Map<ID, ID> parentMap = new HashMap<>();
        for (T node : nodes) {
            if (node.getId() != null && node.getParentId() != null) {
                parentMap.put(node.getId(), node.getParentId());
            }
        }

        for (ID id : parentMap.keySet()) {
            Set<ID> seen = new HashSet<>();
            ID current = id;
            while (current != null) {
                if (!seen.add(current)) {
                    return true; // Cycle
                }
                current = parentMap.get(current);
            }
        }
        return false;
    }
}
