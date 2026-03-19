package cloud.opencode.base.reflect.type;

import java.lang.reflect.*;
import java.util.*;

/**
 * Type Variable Utility Class
 * 类型变量工具类
 *
 * <p>Utility class for working with type variables in generics.</p>
 * <p>用于处理泛型中类型变量的工具类。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Type variable lookup by name - 按名称查找类型变量</li>
 *   <li>Type variable bounds extraction - 类型变量边界提取</li>
 *   <li>Type parameter analysis - 类型参数分析</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TypeVariable<?> tv = TypeVariableUtil.getTypeVariable(MyClass.class, "T");
 * Type[] bounds = TypeVariableUtil.getBounds(tv);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: No (caller must ensure non-null arguments) - 空值安全: 否（调用方须确保非空参数）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(p) where p is the number of type parameters on the class - 时间复杂度: O(p)，p为类上的类型参数数量</li>
 *   <li>Space complexity: O(1) - 空间复杂度: O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public final class TypeVariableUtil {

    private TypeVariableUtil() {
    }

    /**
     * Gets type variable by name from a class
     * 从类按名称获取类型变量
     *
     * @param clazz the class | 类
     * @param name  the type variable name | 类型变量名
     * @return the type variable or null | 类型变量或null
     */
    public static TypeVariable<?> getTypeVariable(Class<?> clazz, String name) {
        for (TypeVariable<?> tv : clazz.getTypeParameters()) {
            if (tv.getName().equals(name)) {
                return tv;
            }
        }
        return null;
    }

    /**
     * Gets all type variable names from a class
     * 从类获取所有类型变量名
     *
     * @param clazz the class | 类
     * @return list of type variable names | 类型变量名列表
     */
    public static List<String> getTypeVariableNames(Class<?> clazz) {
        TypeVariable<?>[] typeParams = clazz.getTypeParameters();
        List<String> names = new ArrayList<>(typeParams.length);
        for (TypeVariable<?> tv : typeParams) {
            names.add(tv.getName());
        }
        return names;
    }

    /**
     * Gets the bounds of a type variable
     * 获取类型变量的边界
     *
     * @param typeVariable the type variable | 类型变量
     * @return array of bounds | 边界数组
     */
    public static Type[] getBounds(TypeVariable<?> typeVariable) {
        return typeVariable.getBounds();
    }

    /**
     * Checks if a type variable has non-Object bounds
     * 检查类型变量是否有非Object边界
     *
     * @param typeVariable the type variable | 类型变量
     * @return true if has bounds | 如果有边界返回true
     */
    public static boolean hasBounds(TypeVariable<?> typeVariable) {
        Type[] bounds = typeVariable.getBounds();
        if (bounds.length == 0) {
            return false;
        }
        if (bounds.length == 1 && bounds[0].equals(Object.class)) {
            return false;
        }
        return true;
    }

    /**
     * Gets the upper bound of a type variable
     * 获取类型变量的上界
     *
     * @param typeVariable the type variable | 类型变量
     * @return the upper bound or Object.class | 上界或Object.class
     */
    public static Type getUpperBound(TypeVariable<?> typeVariable) {
        Type[] bounds = typeVariable.getBounds();
        return bounds.length > 0 ? bounds[0] : Object.class;
    }

    /**
     * Gets the raw upper bound class of a type variable
     * 获取类型变量的原始上界类
     *
     * @param typeVariable the type variable | 类型变量
     * @return the raw upper bound class | 原始上界类
     */
    public static Class<?> getRawUpperBound(TypeVariable<?> typeVariable) {
        return TypeUtil.getRawType(getUpperBound(typeVariable));
    }

    /**
     * Checks if a type contains type variables
     * 检查类型是否包含类型变量
     *
     * @param type the type | 类型
     * @return true if contains type variables | 如果包含类型变量返回true
     */
    public static boolean containsTypeVariable(Type type) {
        if (type instanceof TypeVariable) {
            return true;
        }
        if (type instanceof ParameterizedType pt) {
            for (Type arg : pt.getActualTypeArguments()) {
                if (containsTypeVariable(arg)) {
                    return true;
                }
            }
        }
        if (type instanceof GenericArrayType gat) {
            return containsTypeVariable(gat.getGenericComponentType());
        }
        if (type instanceof WildcardType wt) {
            for (Type bound : wt.getUpperBounds()) {
                if (containsTypeVariable(bound)) {
                    return true;
                }
            }
            for (Type bound : wt.getLowerBounds()) {
                if (containsTypeVariable(bound)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Collects all type variables from a type
     * 从类型收集所有类型变量
     *
     * @param type the type | 类型
     * @return set of type variables | 类型变量集合
     */
    public static Set<TypeVariable<?>> collectTypeVariables(Type type) {
        Set<TypeVariable<?>> result = new LinkedHashSet<>();
        collectTypeVariables(type, result);
        return result;
    }

    private static void collectTypeVariables(Type type, Set<TypeVariable<?>> result) {
        if (type instanceof TypeVariable<?> tv) {
            result.add(tv);
        } else if (type instanceof ParameterizedType pt) {
            for (Type arg : pt.getActualTypeArguments()) {
                collectTypeVariables(arg, result);
            }
        } else if (type instanceof GenericArrayType gat) {
            collectTypeVariables(gat.getGenericComponentType(), result);
        } else if (type instanceof WildcardType wt) {
            for (Type bound : wt.getUpperBounds()) {
                collectTypeVariables(bound, result);
            }
            for (Type bound : wt.getLowerBounds()) {
                collectTypeVariables(bound, result);
            }
        }
    }

    /**
     * Creates a map of type variable names to their Types from a parameterized type
     * 从参数化类型创建类型变量名到Type的映射
     *
     * @param type the parameterized type | 参数化类型
     * @return map of names to types | 名称到类型的映射
     */
    public static Map<String, Type> getTypeArgumentsByName(Type type) {
        if (type instanceof ParameterizedType pt) {
            Class<?> rawType = (Class<?>) pt.getRawType();
            TypeVariable<?>[] typeParams = rawType.getTypeParameters();
            Type[] actualArgs = pt.getActualTypeArguments();

            Map<String, Type> result = new LinkedHashMap<>();
            for (int i = 0; i < typeParams.length && i < actualArgs.length; i++) {
                result.put(typeParams[i].getName(), actualArgs[i]);
            }
            return result;
        }
        return Collections.emptyMap();
    }
}
