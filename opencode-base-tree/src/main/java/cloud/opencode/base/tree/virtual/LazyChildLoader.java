package cloud.opencode.base.tree.virtual;

import java.util.List;

/**
 * Lazy Child Loader
 * 懒加载子节点加载器
 *
 * <p>Functional interface for loading children on demand.</p>
 * <p>用于按需加载子节点的函数式接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Functional interface for lazy child loading - 用于懒加载子节点的函数式接口</li>
 *   <li>Supports lambda and method reference - 支持lambda和方法引用</li>
 *   <li>Used by VirtualTree for on-demand loading - 被VirtualTree用于按需加载</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Lambda implementation - Lambda实现
 * LazyChildLoader<VirtualTree<String, Long>, Long> loader =
 *     parentId -> database.findChildrenByParentId(parentId);
 *
 * // Method reference - 方法引用
 * LazyChildLoader<VirtualTree<String, Long>, Long> loader = repository::findByParentId;
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
@FunctionalInterface
public interface LazyChildLoader<T, ID> {

    /**
     * Load children for the given parent ID
     * 加载指定父节点ID的子节点
     *
     * @param parentId the parent ID | 父节点ID
     * @return the children list | 子节点列表
     */
    List<T> loadChildren(ID parentId);
}
