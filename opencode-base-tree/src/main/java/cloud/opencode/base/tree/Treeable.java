package cloud.opencode.base.tree;

import java.util.List;

/**
 * Treeable Interface
 * 可树化接口
 *
 * <p>Interface for objects that can be organized into a tree structure.</p>
 * <p>可组织成树形结构的对象接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Define tree node contract with ID and parent ID - 定义带ID和父ID的树节点契约</li>
 *   <li>Children list management - 子节点列表管理</li>
 *   <li>Compatible with OpenTree operations - 兼容OpenTree操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Implement Treeable - 实现Treeable
 * public class Department implements Treeable<Department, Long> {
 *     private Long id;
 *     private Long parentId;
 *     private List<Department> children;
 *     // implement getId(), getParentId(), getChildren(), setChildren()
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Depends on implementation - 空值安全: 取决于实现</li>
 * </ul>
 *
 * @param <T> the node type | 节点类型
 * @param <ID> the ID type | ID类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
public interface Treeable<T, ID> {

    /**
     * Get the node ID
     * 获取节点ID
     *
     * @return the ID | ID
     */
    ID getId();

    /**
     * Get the parent node ID
     * 获取父节点ID
     *
     * @return the parent ID | 父节点ID
     */
    ID getParentId();

    /**
     * Get the children list
     * 获取子节点列表
     *
     * @return the children | 子节点列表
     */
    List<T> getChildren();

    /**
     * Set the children list
     * 设置子节点列表
     *
     * @param children the children | 子节点列表
     */
    void setChildren(List<T> children);
}
