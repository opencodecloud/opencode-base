package cloud.opencode.base.tree;

import java.io.PrintStream;
import java.util.List;
import java.util.function.Function;

/**
 * Tree Printer
 * 树打印器
 *
 * <p>Prints tree structure in ASCII format.</p>
 * <p>以ASCII格式打印树结构。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>ASCII tree visualization with box-drawing characters - 使用制表符的ASCII树可视化</li>
 *   <li>Custom node formatter support - 自定义节点格式化器支持</li>
 *   <li>Simple indented format option - 简单缩进格式选项</li>
 *   <li>Tree statistics output - 树统计输出</li>
 *   <li>Console and stream output - 控制台和流输出</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Print tree to string - 打印树为字符串
 * String output = TreePrinter.print(roots);
 *
 * // Print with custom formatter - 使用自定义格式化器打印
 * String output = TreePrinter.print(roots, node -> node.getName());
 *
 * // Print to console - 打印到控制台
 * TreePrinter.printToConsole(roots);
 *
 * // Get tree statistics - 获取树统计
 * String stats = TreePrinter.getStats(roots);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: No (null nodes or children may cause NullPointerException) - 空值安全: 否（null节点或子节点可能导致空指针异常）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
public final class TreePrinter {

    private static final String BRANCH = "├── ";
    private static final String LAST_BRANCH = "└── ";
    private static final String VERTICAL = "│   ";
    private static final String SPACE = "    ";

    private TreePrinter() {
        // Utility class
    }

    /**
     * Print tree to string
     * 打印树为字符串
     *
     * @param roots the root nodes | 根节点列表
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the string representation | 字符串表示
     */
    public static <T extends Treeable<T, ID>, ID> String print(List<T> roots) {
        return print(roots, Object::toString);
    }

    /**
     * Print tree to string with custom formatter
     * 使用自定义格式化器打印树为字符串
     *
     * @param roots the root nodes | 根节点列表
     * @param formatter the node formatter | 节点格式化器
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the string representation | 字符串表示
     */
    public static <T extends Treeable<T, ID>, ID> String print(
            List<T> roots, Function<T, String> formatter) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < roots.size(); i++) {
            printNode(roots.get(i), "", i == roots.size() - 1, sb, formatter);
        }
        return sb.toString();
    }

    private static <T extends Treeable<T, ID>, ID> void printNode(
            T node, String prefix, boolean isLast,
            StringBuilder sb, Function<T, String> formatter) {

        sb.append(prefix);
        sb.append(isLast ? LAST_BRANCH : BRANCH);
        sb.append(formatter.apply(node));
        sb.append("\n");

        List<T> children = node.getChildren();
        if (children != null && !children.isEmpty()) {
            String childPrefix = prefix + (isLast ? SPACE : VERTICAL);
            for (int i = 0; i < children.size(); i++) {
                printNode(children.get(i), childPrefix, i == children.size() - 1, sb, formatter);
            }
        }
    }

    /**
     * Print tree to console
     * 打印树到控制台
     *
     * @param roots the root nodes | 根节点列表
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     */
    public static <T extends Treeable<T, ID>, ID> void printToConsole(List<T> roots) {
        printToStream(roots, System.out, Object::toString);
    }

    /**
     * Print tree to console with formatter
     * 使用格式化器打印树到控制台
     *
     * @param roots the root nodes | 根节点列表
     * @param formatter the node formatter | 节点格式化器
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     */
    public static <T extends Treeable<T, ID>, ID> void printToConsole(
            List<T> roots, Function<T, String> formatter) {
        printToStream(roots, System.out, formatter);
    }

    /**
     * Print tree to stream
     * 打印树到流
     *
     * @param roots the root nodes | 根节点列表
     * @param out the output stream | 输出流
     * @param formatter the node formatter | 节点格式化器
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     */
    public static <T extends Treeable<T, ID>, ID> void printToStream(
            List<T> roots, PrintStream out, Function<T, String> formatter) {
        out.print(print(roots, formatter));
    }

    /**
     * Print single node tree
     * 打印单节点树
     *
     * @param root the root node | 根节点
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the string representation | 字符串表示
     */
    public static <T extends Treeable<T, ID>, ID> String printSingle(T root) {
        return print(List.of(root));
    }

    /**
     * Print simple tree (indented format)
     * 打印简单树（缩进格式）
     *
     * @param roots the root nodes | 根节点列表
     * @param indent the indent string | 缩进字符串
     * @param formatter the node formatter | 节点格式化器
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the string representation | 字符串表示
     */
    public static <T extends Treeable<T, ID>, ID> String printSimple(
            List<T> roots, String indent, Function<T, String> formatter) {
        StringBuilder sb = new StringBuilder();
        for (T root : roots) {
            printSimpleNode(root, 0, indent, sb, formatter);
        }
        return sb.toString();
    }

    private static <T extends Treeable<T, ID>, ID> void printSimpleNode(
            T node, int depth, String indent,
            StringBuilder sb, Function<T, String> formatter) {

        sb.append(indent.repeat(depth));
        sb.append(formatter.apply(node));
        sb.append("\n");

        List<T> children = node.getChildren();
        if (children != null) {
            for (T child : children) {
                printSimpleNode(child, depth + 1, indent, sb, formatter);
            }
        }
    }

    /**
     * Get tree statistics string
     * 获取树统计字符串
     *
     * @param roots the root nodes | 根节点列表
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the statistics string | 统计字符串
     */
    public static <T extends Treeable<T, ID>, ID> String getStats(List<T> roots) {
        int[] stats = {0, 0, 0}; // total, leaves, maxDepth
        collectStats(roots, 0, stats);
        return String.format("Nodes: %d, Leaves: %d, MaxDepth: %d",
            stats[0], stats[1], stats[2]);
    }

    private static <T extends Treeable<T, ID>, ID> void collectStats(
            List<T> nodes, int depth, int[] stats) {
        for (T node : nodes) {
            stats[0]++; // total
            stats[2] = Math.max(stats[2], depth); // maxDepth

            List<T> children = node.getChildren();
            if (children == null || children.isEmpty()) {
                stats[1]++; // leaves
            } else {
                collectStats(children, depth + 1, stats);
            }
        }
    }
}
