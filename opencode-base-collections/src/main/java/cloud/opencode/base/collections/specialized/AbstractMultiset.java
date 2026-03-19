package cloud.opencode.base.collections.specialized;

import cloud.opencode.base.collections.Multiset;

import java.util.*;

/**
 * AbstractMultiset - Abstract Multiset Base Class
 * AbstractMultiset - 抽象多重集合基类
 *
 * <p>Provides a skeletal implementation of the Multiset interface.</p>
 * <p>提供 Multiset 接口的骨架实现。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Skeletal multiset implementation - 骨架多重集合实现</li>
 *   <li>Count-based element tracking - 基于计数的元素跟踪</li>
 *   <li>Collection interface compliance - 集合接口兼容</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Subclasses provide backing store
 * Multiset<String> multiset = HashMultiset.create();
 * multiset.add("a", 3);
 * int count = multiset.count("a"); // 3
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (not synchronized) - 否（未同步）</li>
 *   <li>Null-safe: Implementation-dependent - 取决于实现</li>
 * </ul>
 * @param <E> element type | 元素类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public abstract class AbstractMultiset<E> extends AbstractCollection<E> implements Multiset<E> {

    // ==================== Collection 方法 | Collection Methods ====================

    @Override
    public boolean add(E element) {
        add(element, 1);
        return true;
    }

    @Override
    public boolean remove(Object element) {
        return remove(element, 1) > 0;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean changed = false;
        for (E element : c) {
            changed |= add(element);
        }
        return changed;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean changed = false;
        for (Object o : c) {
            changed |= remove(o);
        }
        return changed;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean changed = false;
        Iterator<E> iterator = iterator();
        while (iterator.hasNext()) {
            if (!c.contains(iterator.next())) {
                iterator.remove();
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public Object[] toArray() {
        Object[] array = new Object[size()];
        int index = 0;
        for (E element : this) {
            array[index++] = element;
        }
        return array;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        int size = size();
        if (a.length < size) {
            a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
        }
        int index = 0;
        for (E element : this) {
            a[index++] = (T) element;
        }
        if (a.length > size) {
            a[size] = null;
        }
        return a;
    }

    // ==================== Object 方法 | Object Methods ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Multiset<?> that)) return false;

        if (size() != that.size()) return false;
        for (Entry<?> entry : entrySet()) {
            if (that.count(entry.getElement()) != entry.getCount()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        for (Entry<E> entry : entrySet()) {
            hashCode += Objects.hashCode(entry.getElement()) ^ entry.getCount();
        }
        return hashCode;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Entry<E> entry : entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(entry.getElement()).append(" x ").append(entry.getCount());
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }
}
