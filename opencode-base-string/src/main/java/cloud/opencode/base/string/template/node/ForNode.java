package cloud.opencode.base.string.template.node;

import java.util.*;

/**
 * For Node - Template node for loop iteration.
 * 循环节点 - 用于循环迭代的模板节点。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Iterable collection loop rendering - 可迭代集合循环渲染</li>
 *   <li>Nested body node support - 嵌套体节点支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ForNode node = new ForNode("item", "items", bodyNodes);
 * String result = node.render(Map.of("items", List.of("a", "b")));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public final class ForNode implements TemplateNode {
    private final String itemName;
    private final String collectionName;
    private final List<TemplateNode> bodyNodes;

    public ForNode(String itemName, String collectionName, List<TemplateNode> bodyNodes) {
        this.itemName = itemName;
        this.collectionName = collectionName;
        this.bodyNodes = bodyNodes;
    }

    @Override
    public String render(java.util.Map<String, Object> context) {
        Object collection = context.get(collectionName);
        if (!(collection instanceof Iterable)) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        int index = 0;
        for (Object item : (Iterable<?>) collection) {
            Map<String, Object> itemContext = new HashMap<>(context);
            itemContext.put(itemName, item);
            itemContext.put(itemName + "_index", index);
            itemContext.put(itemName + "_first", index == 0);
            
            for (TemplateNode node : bodyNodes) {
                sb.append(node.render(itemContext));
            }
            index++;
        }
        
        return sb.toString();
    }
}
