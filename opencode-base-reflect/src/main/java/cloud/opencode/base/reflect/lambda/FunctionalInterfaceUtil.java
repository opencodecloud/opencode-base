package cloud.opencode.base.reflect.lambda;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Functional Interface Utility
 * 函数式接口工具
 *
 * <p>Utilities for working with functional interfaces.</p>
 * <p>处理函数式接口的工具。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Functional interface detection - 函数式接口检测</li>
 *   <li>Abstract method extraction - 抽象方法提取</li>
 *   <li>SAM (Single Abstract Method) analysis - SAM分析</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * boolean isFunctional = FunctionalInterfaceUtil.isFunctionalInterface(Runnable.class);
 * Method sam = FunctionalInterfaceUtil.getSingleAbstractMethod(Function.class);
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
 *   <li>Time complexity: O(m) where m is the number of methods in the interface - 时间复杂度: O(m)，m为接口中的方法数量</li>
 *   <li>Space complexity: O(1) - 空间复杂度: O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public final class FunctionalInterfaceUtil {

    private FunctionalInterfaceUtil() {
    }

    /**
     * Checks if a class is a functional interface
     * 检查类是否为函数式接口
     *
     * @param clazz the class | 类
     * @return true if functional interface | 如果是函数式接口返回true
     */
    public static boolean isFunctionalInterface(Class<?> clazz) {
        if (!clazz.isInterface()) {
            return false;
        }
        // Has @FunctionalInterface annotation
        if (clazz.isAnnotationPresent(FunctionalInterface.class)) {
            return true;
        }
        // Or has exactly one abstract method
        return getSingleAbstractMethod(clazz).isPresent();
    }

    /**
     * Gets the single abstract method (SAM) of a functional interface
     * 获取函数式接口的单一抽象方法（SAM）
     *
     * @param clazz the class | 类
     * @return Optional of the SAM | SAM的Optional
     */
    public static Optional<Method> getSingleAbstractMethod(Class<?> clazz) {
        if (!clazz.isInterface()) {
            return Optional.empty();
        }

        List<Method> abstractMethods = getAbstractMethods(clazz);
        return abstractMethods.size() == 1 ? Optional.of(abstractMethods.get(0)) : Optional.empty();
    }

    /**
     * Gets all abstract methods of an interface
     * 获取接口的所有抽象方法
     *
     * @param clazz the class | 类
     * @return list of abstract methods | 抽象方法列表
     */
    public static List<Method> getAbstractMethods(Class<?> clazz) {
        if (!clazz.isInterface()) {
            return Collections.emptyList();
        }

        Set<MethodSignature> objectMethods = getObjectMethodSignatures();
        List<Method> abstractMethods = new ArrayList<>();

        for (Method method : clazz.getMethods()) {
            if (Modifier.isAbstract(method.getModifiers()) && !method.isDefault()) {
                MethodSignature sig = new MethodSignature(method);
                if (!objectMethods.contains(sig)) {
                    abstractMethods.add(method);
                }
            }
        }

        return abstractMethods;
    }

    /**
     * Gets default methods of an interface
     * 获取接口的默认方法
     *
     * @param clazz the class | 类
     * @return list of default methods | 默认方法列表
     */
    public static List<Method> getDefaultMethods(Class<?> clazz) {
        if (!clazz.isInterface()) {
            return Collections.emptyList();
        }

        List<Method> defaults = new ArrayList<>();
        for (Method method : clazz.getMethods()) {
            if (method.isDefault()) {
                defaults.add(method);
            }
        }
        return defaults;
    }

    /**
     * Gets static methods of an interface
     * 获取接口的静态方法
     *
     * @param clazz the class | 类
     * @return list of static methods | 静态方法列表
     */
    public static List<Method> getStaticMethods(Class<?> clazz) {
        if (!clazz.isInterface()) {
            return Collections.emptyList();
        }

        List<Method> statics = new ArrayList<>();
        for (Method method : clazz.getDeclaredMethods()) {
            if (Modifier.isStatic(method.getModifiers())) {
                statics.add(method);
            }
        }
        return statics;
    }

    /**
     * Gets the return type of the functional method
     * 获取函数式方法的返回类型
     *
     * @param clazz the functional interface | 函数式接口
     * @return the return type or null | 返回类型或null
     */
    public static Class<?> getFunctionalMethodReturnType(Class<?> clazz) {
        return getSingleAbstractMethod(clazz)
                .map(Method::getReturnType)
                .orElse(null);
    }

    /**
     * Gets the parameter types of the functional method
     * 获取函数式方法的参数类型
     *
     * @param clazz the functional interface | 函数式接口
     * @return array of parameter types | 参数类型数组
     */
    public static Class<?>[] getFunctionalMethodParameterTypes(Class<?> clazz) {
        return getSingleAbstractMethod(clazz)
                .map(Method::getParameterTypes)
                .orElse(new Class<?>[0]);
    }

    /**
     * Gets the arity (parameter count) of the functional method
     * 获取函数式方法的元数（参数数量）
     *
     * @param clazz the functional interface | 函数式接口
     * @return the arity | 元数
     */
    public static int getFunctionalMethodArity(Class<?> clazz) {
        return getSingleAbstractMethod(clazz)
                .map(Method::getParameterCount)
                .orElse(-1);
    }

    /**
     * Checks if functional interface returns void
     * 检查函数式接口是否返回void
     *
     * @param clazz the functional interface | 函数式接口
     * @return true if returns void | 如果返回void返回true
     */
    public static boolean returnsVoid(Class<?> clazz) {
        return getSingleAbstractMethod(clazz)
                .map(m -> m.getReturnType() == void.class)
                .orElse(false);
    }

    /**
     * Checks if functional interface has no parameters
     * 检查函数式接口是否没有参数
     *
     * @param clazz the functional interface | 函数式接口
     * @return true if no parameters | 如果没有参数返回true
     */
    public static boolean hasNoParameters(Class<?> clazz) {
        return getFunctionalMethodArity(clazz) == 0;
    }

    /**
     * Classifies the functional interface type
     * 分类函数式接口类型
     *
     * @param clazz the functional interface | 函数式接口
     * @return the category | 类别
     */
    public static FunctionalCategory classify(Class<?> clazz) {
        if (!isFunctionalInterface(clazz)) {
            return FunctionalCategory.NOT_FUNCTIONAL;
        }

        boolean returnsVoid = returnsVoid(clazz);
        int arity = getFunctionalMethodArity(clazz);

        if (returnsVoid) {
            return arity == 0 ? FunctionalCategory.RUNNABLE : FunctionalCategory.CONSUMER;
        } else {
            if (arity == 0) {
                return FunctionalCategory.SUPPLIER;
            }
            Class<?> returnType = getFunctionalMethodReturnType(clazz);
            if (returnType == boolean.class || returnType == Boolean.class) {
                return FunctionalCategory.PREDICATE;
            }
            return FunctionalCategory.FUNCTION;
        }
    }

    private static Set<MethodSignature> getObjectMethodSignatures() {
        Set<MethodSignature> sigs = new HashSet<>();
        for (Method method : Object.class.getMethods()) {
            sigs.add(new MethodSignature(method));
        }
        return sigs;
    }

    /**
     * Functional interface category
     * 函数式接口类别
     */
    public enum FunctionalCategory {
        /**
         * Not a functional interface
         * 不是函数式接口
         */
        NOT_FUNCTIONAL,
        /**
         * Runnable-like (no params, void return)
         * 类似Runnable（无参数，void返回）
         */
        RUNNABLE,
        /**
         * Consumer-like (has params, void return)
         * 类似Consumer（有参数，void返回）
         */
        CONSUMER,
        /**
         * Supplier-like (no params, has return)
         * 类似Supplier（无参数，有返回）
         */
        SUPPLIER,
        /**
         * Predicate-like (has params, boolean return)
         * 类似Predicate（有参数，boolean返回）
         */
        PREDICATE,
        /**
         * Function-like (has params, has return)
         * 类似Function（有参数，有返回）
         */
        FUNCTION
    }

    private record MethodSignature(String name, Class<?>[] parameterTypes) {
        MethodSignature(Method method) {
            this(method.getName(), method.getParameterTypes());
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof MethodSignature that)) return false;
            return name.equals(that.name) && Arrays.equals(parameterTypes, that.parameterTypes);
        }

        @Override
        public int hashCode() {
            return name.hashCode() * 31 + Arrays.hashCode(parameterTypes);
        }
    }
}
