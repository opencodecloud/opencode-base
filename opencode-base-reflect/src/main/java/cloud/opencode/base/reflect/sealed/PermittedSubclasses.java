package cloud.opencode.base.reflect.sealed;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Permitted Subclasses Collection
 * 许可子类集合
 *
 * <p>Represents the collection of permitted subclasses for a sealed class.</p>
 * <p>表示密封类的许可子类集合。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Iterable access to permitted subclasses - 可迭代访问许可子类</li>
 *   <li>Filtering and streaming support - 过滤和流支持</li>
 *   <li>Subclass type checking - 子类类型检查</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * PermittedSubclasses permitted = new PermittedSubclasses(Shape.class);
 * for (Class<?> subclass : permitted) {
 *     System.out.println(subclass.getSimpleName());
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: No (sealed class must be non-null) - 空值安全: 否（密封类须非空）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public class PermittedSubclasses implements Iterable<Class<?>> {

    private final Class<?> sealedClass;
    private final List<Class<?>> subclasses;

    /**
     * Creates a PermittedSubclasses collection
     * 创建PermittedSubclasses集合
     *
     * @param sealedClass the sealed class | 密封类
     */
    public PermittedSubclasses(Class<?> sealedClass) {
        if (!sealedClass.isSealed()) {
            throw new IllegalArgumentException("Class is not sealed: " + sealedClass.getName());
        }
        this.sealedClass = sealedClass;
        this.subclasses = Arrays.asList(sealedClass.getPermittedSubclasses());
    }

    /**
     * Gets the sealed class
     * 获取密封类
     *
     * @return the sealed class | 密封类
     */
    public Class<?> getSealedClass() {
        return sealedClass;
    }

    /**
     * Gets all permitted subclasses
     * 获取所有许可子类
     *
     * @return list of subclasses | 子类列表
     */
    public List<Class<?>> getAll() {
        return Collections.unmodifiableList(subclasses);
    }

    /**
     * Gets the number of permitted subclasses
     * 获取许可子类数量
     *
     * @return the count | 数量
     */
    public int size() {
        return subclasses.size();
    }

    /**
     * Checks if empty
     * 检查是否为空
     *
     * @return true if no permitted subclasses | 如果没有许可子类返回true
     */
    public boolean isEmpty() {
        return subclasses.isEmpty();
    }

    /**
     * Checks if a class is a permitted subclass
     * 检查类是否为许可子类
     *
     * @param clazz the class to check | 要检查的类
     * @return true if permitted | 如果是许可的返回true
     */
    public boolean isPermitted(Class<?> clazz) {
        return subclasses.contains(clazz);
    }

    /**
     * Gets a subclass by index
     * 按索引获取子类
     *
     * @param index the index | 索引
     * @return the subclass | 子类
     */
    public Class<?> get(int index) {
        return subclasses.get(index);
    }

    /**
     * Gets subclasses matching a predicate
     * 获取匹配谓词的子类
     *
     * @param predicate the predicate | 谓词
     * @return list of matching subclasses | 匹配的子类列表
     */
    public List<Class<?>> filter(Predicate<Class<?>> predicate) {
        return subclasses.stream().filter(predicate).toList();
    }

    /**
     * Gets only final subclasses
     * 仅获取final子类
     *
     * @return list of final subclasses | final子类列表
     */
    public List<Class<?>> getFinalSubclasses() {
        return filter(c -> java.lang.reflect.Modifier.isFinal(c.getModifiers()));
    }

    /**
     * Gets only non-final subclasses
     * 仅获取非final子类
     *
     * @return list of non-final subclasses | 非final子类列表
     */
    public List<Class<?>> getNonFinalSubclasses() {
        return filter(c -> !java.lang.reflect.Modifier.isFinal(c.getModifiers()));
    }

    /**
     * Gets only sealed subclasses
     * 仅获取密封子类
     *
     * @return list of sealed subclasses | 密封子类列表
     */
    public List<Class<?>> getSealedSubclasses() {
        return filter(Class::isSealed);
    }

    /**
     * Gets only record subclasses
     * 仅获取record子类
     *
     * @return list of record subclasses | record子类列表
     */
    public List<Class<?>> getRecordSubclasses() {
        return filter(Class::isRecord);
    }

    /**
     * Gets subclass names
     * 获取子类名称
     *
     * @return list of names | 名称列表
     */
    public List<String> getNames() {
        return subclasses.stream().map(Class::getName).toList();
    }

    /**
     * Gets subclass simple names
     * 获取子类简单名称
     *
     * @return list of simple names | 简单名称列表
     */
    public List<String> getSimpleNames() {
        return subclasses.stream().map(Class::getSimpleName).toList();
    }

    /**
     * Creates a stream of permitted subclasses
     * 创建许可子类的流
     *
     * @return stream of subclasses | 子类流
     */
    public Stream<Class<?>> stream() {
        return subclasses.stream();
    }

    /**
     * Gets recursively all permitted subclasses (including nested sealed classes)
     * 递归获取所有许可子类（包括嵌套的密封类）
     *
     * @return set of all subclasses | 所有子类集合
     */
    public Set<Class<?>> getAllRecursive() {
        Set<Class<?>> result = new LinkedHashSet<>();
        collectRecursive(sealedClass, result);
        return result;
    }

    /**
     * Gets the complete hierarchy as a tree structure
     * 获取完整层次结构作为树结构
     *
     * @return the hierarchy node | 层次结构节点
     */
    public HierarchyNode getHierarchy() {
        return new HierarchyNode(sealedClass);
    }

    private void collectRecursive(Class<?> clazz, Set<Class<?>> result) {
        if (clazz.isSealed()) {
            for (Class<?> sub : clazz.getPermittedSubclasses()) {
                result.add(sub);
                collectRecursive(sub, result);
            }
        }
    }

    @Override
    public Iterator<Class<?>> iterator() {
        return subclasses.iterator();
    }

    @Override
    public String toString() {
        return "PermittedSubclasses[" + sealedClass.getSimpleName() + " -> " + getSimpleNames() + "]";
    }

    /**
     * Hierarchy node for tree representation
     * 用于树表示的层次结构节点
     */
    public static class HierarchyNode {
        private final Class<?> clazz;
        private final List<HierarchyNode> children;

        /**
         * Creates a HierarchyNode
         * 创建HierarchyNode
         *
         * @param clazz the class | 类
         */
        public HierarchyNode(Class<?> clazz) {
            this.clazz = clazz;
            this.children = new ArrayList<>();

            if (clazz.isSealed()) {
                for (Class<?> sub : clazz.getPermittedSubclasses()) {
                    children.add(new HierarchyNode(sub));
                }
            }
        }

        /**
         * Gets the class
         * 获取类
         *
         * @return the class | 类
         */
        public Class<?> getClazz() {
            return clazz;
        }

        /**
         * Gets child nodes
         * 获取子节点
         *
         * @return list of children | 子节点列表
         */
        public List<HierarchyNode> getChildren() {
            return Collections.unmodifiableList(children);
        }

        /**
         * Checks if this is a leaf node
         * 检查是否为叶节点
         *
         * @return true if leaf | 如果是叶节点返回true
         */
        public boolean isLeaf() {
            return children.isEmpty();
        }

        /**
         * Gets the depth of this node
         * 获取此节点的深度
         *
         * @return the depth | 深度
         */
        public int getDepth() {
            if (children.isEmpty()) {
                return 0;
            }
            return 1 + children.stream().mapToInt(HierarchyNode::getDepth).max().orElse(0);
        }

        /**
         * Gets all leaf classes
         * 获取所有叶类
         *
         * @return list of leaf classes | 叶类列表
         */
        public List<Class<?>> getLeafClasses() {
            List<Class<?>> result = new ArrayList<>();
            collectLeaves(this, result);
            return result;
        }

        private void collectLeaves(HierarchyNode node, List<Class<?>> result) {
            if (node.isLeaf()) {
                result.add(node.clazz);
            } else {
                for (HierarchyNode child : node.children) {
                    collectLeaves(child, result);
                }
            }
        }

        @Override
        public String toString() {
            return toTreeString(0);
        }

        private String toTreeString(int indent) {
            StringBuilder sb = new StringBuilder();
            sb.append("  ".repeat(indent)).append(clazz.getSimpleName());
            if (clazz.isSealed()) sb.append(" (sealed)");
            else if (clazz.isRecord()) sb.append(" (record)");
            else if (java.lang.reflect.Modifier.isFinal(clazz.getModifiers())) sb.append(" (final)");
            sb.append("\n");
            for (HierarchyNode child : children) {
                sb.append(child.toTreeString(indent + 1));
            }
            return sb.toString();
        }
    }
}
