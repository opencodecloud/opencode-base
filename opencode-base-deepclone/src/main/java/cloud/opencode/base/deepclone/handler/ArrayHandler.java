package cloud.opencode.base.deepclone.handler;

import cloud.opencode.base.deepclone.CloneContext;
import cloud.opencode.base.deepclone.Cloner;

import java.lang.reflect.Array;

/**
 * Handler for cloning array types
 * 数组类型克隆处理器
 *
 * <p>Handles both primitive arrays (int[], double[], etc.) and object arrays.
 * Primitive arrays are copied directly, object arrays have their elements deep cloned.</p>
 * <p>处理基本类型数组（int[]、double[]等）和对象数组。
 * 基本类型数组直接复制，对象数组的元素会被深度克隆。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ArrayHandler handler = new ArrayHandler();
 * int[] intArray = {1, 2, 3};
 * int[] clonedInt = (int[]) handler.clone(intArray, cloner, context);
 *
 * String[] strArray = {"a", "b", "c"};
 * String[] clonedStr = (String[]) handler.clone(strArray, cloner, context);
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Primitive array direct copy - 基本类型数组直接复制</li>
 *   <li>Object array recursive deep clone - 对象数组递归深度克隆</li>
 *   <li>Preserves array component type - 保留数组组件类型</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
public final class ArrayHandler implements TypeHandler<Object> {

    @Override
    public Object clone(Object array, Cloner cloner, CloneContext context) {
        if (array == null) {
            return null;
        }

        Class<?> componentType = array.getClass().getComponentType();
        int length = Array.getLength(array);

        if (componentType.isPrimitive()) {
            return clonePrimitiveArray(array, componentType, length);
        }

        return cloneObjectArray(array, componentType, length, cloner, context);
    }

    /**
     * Clones a primitive type array
     * 克隆基本类型数组
     *
     * @param array         the original array | 原始数组
     * @param componentType the component type | 组件类型
     * @param length        the array length | 数组长度
     * @return the cloned array | 克隆的数组
     */
    public Object clonePrimitiveArray(Object array, Class<?> componentType, int length) {
        Object clone = Array.newInstance(componentType, length);
        System.arraycopy(array, 0, clone, 0, length);
        return clone;
    }

    /**
     * Clones a primitive array (convenience method)
     * 克隆基本类型数组（便捷方法）
     *
     * @param array the original array | 原始数组
     * @return the cloned array | 克隆的数组
     */
    public Object clonePrimitiveArray(Object array) {
        Class<?> componentType = array.getClass().getComponentType();
        int length = Array.getLength(array);
        return clonePrimitiveArray(array, componentType, length);
    }

    /**
     * Clones an object array with deep cloning of elements
     * 克隆对象数组并深度克隆元素
     *
     * @param array         the original array | 原始数组
     * @param componentType the component type | 组件类型
     * @param length        the array length | 数组长度
     * @param cloner        the cloner | 克隆器
     * @param context       the context | 上下文
     * @return the cloned array | 克隆的数组
     */
    private Object cloneObjectArray(Object array, Class<?> componentType, int length,
                                    Cloner cloner, CloneContext context) {
        Object clone = Array.newInstance(componentType, length);
        context.registerCloned(array, clone);

        for (int i = 0; i < length; i++) {
            Object element = Array.get(array, i);
            Object clonedElement = cloner.clone(element, context);
            Array.set(clone, i, clonedElement);
        }

        return clone;
    }

    /**
     * Clones an object array
     * 克隆对象数组
     *
     * @param array   the original array | 原始数组
     * @param cloner  the cloner | 克隆器
     * @param context the context | 上下文
     * @param <T>     the element type | 元素类型
     * @return the cloned array | 克隆的数组
     */
    @SuppressWarnings("unchecked")
    public <T> T[] cloneObjectArray(T[] array, Cloner cloner, CloneContext context) {
        return (T[]) clone(array, cloner, context);
    }

    @Override
    public boolean supports(Class<?> type) {
        return type != null && type.isArray();
    }

    @Override
    public int priority() {
        return 10;
    }
}
