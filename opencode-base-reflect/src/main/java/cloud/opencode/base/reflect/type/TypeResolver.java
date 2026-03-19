package cloud.opencode.base.reflect.type;

import java.lang.reflect.*;
import java.util.*;

/**
 * Type Resolver
 * 类型解析器
 *
 * <p>Resolves type variables in the context of a specific type,
 * enabling proper generic type inference.</p>
 * <p>在特定类型上下文中解析类型变量，实现正确的泛型类型推断。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Type variable resolution - 类型变量解析</li>
 *   <li>Parameterized type resolution - 参数化类型解析</li>
 *   <li>Inheritance chain traversal - 继承链遍历</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Resolve type variable in context
 * Type resolved = TypeResolver.resolveType(StringList.class, listElementType);
 *
 * // Get type variable map
 * Map<TypeVariable<?>, Type> typeMap = TypeResolver.getTypeVariableMap(StringList.class);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: No (caller must ensure non-null type arguments) - 空值安全: 否（调用方须确保非空类型参数）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public final class TypeResolver {

    private TypeResolver() {
    }

    /**
     * Resolves a type in the context of another type
     * 在另一类型上下文中解析类型
     *
     * @param context  the context type | 上下文类型
     * @param toResolve the type to resolve | 要解析的类型
     * @return the resolved type | 解析后的类型
     */
    public static Type resolveType(Type context, Type toResolve) {
        Map<TypeVariable<?>, Type> typeMap = getTypeVariableMap(context);
        return resolve(toResolve, typeMap);
    }

    /**
     * Gets the type variable map for a type
     * 获取类型的类型变量映射
     *
     * @param type the type | 类型
     * @return map of type variables to their actual types | 类型变量到实际类型的映射
     */
    public static Map<TypeVariable<?>, Type> getTypeVariableMap(Type type) {
        Map<TypeVariable<?>, Type> result = new HashMap<>();
        populateTypeMap(type, result);
        return result;
    }

    private static void populateTypeMap(Type type, Map<TypeVariable<?>, Type> result) {
        if (type instanceof ParameterizedType pt) {
            Class<?> rawType = (Class<?>) pt.getRawType();
            TypeVariable<?>[] typeParams = rawType.getTypeParameters();
            Type[] actualArgs = pt.getActualTypeArguments();

            for (int i = 0; i < typeParams.length && i < actualArgs.length; i++) {
                result.put(typeParams[i], actualArgs[i]);
            }

            // Process superclass
            Type genericSuper = rawType.getGenericSuperclass();
            if (genericSuper != null) {
                populateTypeMap(resolvePartial(genericSuper, result), result);
            }

            // Process interfaces
            for (Type iface : rawType.getGenericInterfaces()) {
                populateTypeMap(resolvePartial(iface, result), result);
            }
        } else if (type instanceof Class<?> clazz) {
            Type genericSuper = clazz.getGenericSuperclass();
            if (genericSuper != null) {
                populateTypeMap(genericSuper, result);
            }
            for (Type iface : clazz.getGenericInterfaces()) {
                populateTypeMap(iface, result);
            }
        }
    }

    private static Type resolvePartial(Type type, Map<TypeVariable<?>, Type> typeMap) {
        if (type instanceof ParameterizedType pt) {
            Type[] args = pt.getActualTypeArguments();
            Type[] resolvedArgs = new Type[args.length];
            boolean changed = false;
            for (int i = 0; i < args.length; i++) {
                resolvedArgs[i] = resolve(args[i], typeMap);
                if (resolvedArgs[i] != args[i]) {
                    changed = true;
                }
            }
            if (changed) {
                return new ParameterizedTypeImpl((Class<?>) pt.getRawType(),
                        pt.getOwnerType(), resolvedArgs);
            }
        }
        return type;
    }

    private static Type resolve(Type type, Map<TypeVariable<?>, Type> typeMap) {
        if (type instanceof TypeVariable<?> tv) {
            Type resolved = typeMap.get(tv);
            if (resolved != null) {
                return resolve(resolved, typeMap);
            }
            return type;
        }

        if (type instanceof ParameterizedType pt) {
            Type[] args = pt.getActualTypeArguments();
            Type[] resolvedArgs = new Type[args.length];
            boolean changed = false;
            for (int i = 0; i < args.length; i++) {
                resolvedArgs[i] = resolve(args[i], typeMap);
                if (resolvedArgs[i] != args[i]) {
                    changed = true;
                }
            }
            if (changed) {
                return new ParameterizedTypeImpl((Class<?>) pt.getRawType(),
                        pt.getOwnerType(), resolvedArgs);
            }
            return type;
        }

        if (type instanceof GenericArrayType gat) {
            Type componentType = gat.getGenericComponentType();
            Type resolvedComponent = resolve(componentType, typeMap);
            if (resolvedComponent != componentType) {
                return new GenericArrayTypeImpl(resolvedComponent);
            }
            return type;
        }

        if (type instanceof WildcardType wt) {
            Type[] upperBounds = wt.getUpperBounds();
            Type[] lowerBounds = wt.getLowerBounds();
            Type[] resolvedUpper = new Type[upperBounds.length];
            Type[] resolvedLower = new Type[lowerBounds.length];
            boolean changed = false;

            for (int i = 0; i < upperBounds.length; i++) {
                resolvedUpper[i] = resolve(upperBounds[i], typeMap);
                if (resolvedUpper[i] != upperBounds[i]) {
                    changed = true;
                }
            }
            for (int i = 0; i < lowerBounds.length; i++) {
                resolvedLower[i] = resolve(lowerBounds[i], typeMap);
                if (resolvedLower[i] != lowerBounds[i]) {
                    changed = true;
                }
            }
            if (changed) {
                return new WildcardTypeImpl(resolvedUpper, resolvedLower);
            }
            return type;
        }

        return type;
    }

    /**
     * Resolves the return type of a method in a specific type context
     * 在特定类型上下文中解析方法返回类型
     *
     * @param context the context type | 上下文类型
     * @param method  the method | 方法
     * @return the resolved return type | 解析后的返回类型
     */
    public static Type resolveReturnType(Type context, Method method) {
        return resolveType(context, method.getGenericReturnType());
    }

    /**
     * Resolves the parameter types of a method in a specific type context
     * 在特定类型上下文中解析方法参数类型
     *
     * @param context the context type | 上下文类型
     * @param method  the method | 方法
     * @return array of resolved parameter types | 解析后的参数类型数组
     */
    public static Type[] resolveParameterTypes(Type context, Method method) {
        Type[] paramTypes = method.getGenericParameterTypes();
        Type[] result = new Type[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            result[i] = resolveType(context, paramTypes[i]);
        }
        return result;
    }

    /**
     * Resolves the type of a field in a specific type context
     * 在特定类型上下文中解析字段类型
     *
     * @param context the context type | 上下文类型
     * @param field   the field | 字段
     * @return the resolved field type | 解析后的字段类型
     */
    public static Type resolveFieldType(Type context, Field field) {
        return resolveType(context, field.getGenericType());
    }

    /**
     * Gets the type argument at a specific index from a parameterized type
     * 从参数化类型获取指定索引的类型参数
     *
     * @param type  the parameterized type | 参数化类型
     * @param index the index | 索引
     * @return the type argument or null | 类型参数或null
     */
    public static Type getTypeArgument(Type type, int index) {
        if (type instanceof ParameterizedType pt) {
            Type[] args = pt.getActualTypeArguments();
            if (index >= 0 && index < args.length) {
                return args[index];
            }
        }
        return null;
    }

    /**
     * Finds the type argument for a specific type variable in the inheritance chain
     * 在继承链中查找特定类型变量的类型参数
     *
     * @param type       the type to search | 要搜索的类型
     * @param targetType the target parameterized type | 目标参数化类型
     * @param index      the type argument index | 类型参数索引
     * @return the resolved type argument or null | 解析后的类型参数或null
     */
    public static Type findTypeArgument(Type type, Class<?> targetType, int index) {
        Map<TypeVariable<?>, Type> typeMap = getTypeVariableMap(type);

        TypeVariable<?>[] typeParams = targetType.getTypeParameters();
        if (index >= 0 && index < typeParams.length) {
            Type resolved = typeMap.get(typeParams[index]);
            if (resolved != null) {
                return resolved;
            }
        }

        return null;
    }
}
