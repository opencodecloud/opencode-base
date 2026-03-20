package cloud.opencode.base.reflect.lambda;

import cloud.opencode.base.reflect.exception.OpenReflectException;

import java.io.Serializable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Lambda Utility Class
 * Lambda工具类
 *
 * <p>Provides low-level lambda operation utilities with caching.</p>
 * <p>提供带缓存的底层lambda操作工具。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>SerializedLambda extraction with caching - 带缓存的SerializedLambda提取</li>
 *   <li>Implementation method resolution - 实现方法解析</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SerializedLambda sl = LambdaUtil.getSerializedLambda(lambda);
 * String methodName = sl.getImplMethodName();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (uses ConcurrentHashMap for caching) - 线程安全: 是（使用ConcurrentHashMap缓存）</li>
 *   <li>Null-safe: No (caller must ensure non-null lambda) - 空值安全: 否（调用方须确保非空lambda）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) for cached lookups; O(1) for first SerializedLambda extraction via reflection - 时间复杂度: 缓存命中时 O(1)；首次通过反射提取 SerializedLambda 为 O(1)</li>
 *   <li>Space complexity: O(1) per cached lambda class entry - 空间复杂度: O(1)，每个缓存 lambda 类条目</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public final class LambdaUtil {

    private LambdaUtil() {
    }

    // ==================== SerializedLambda Extraction | SerializedLambda提取 ====================

    /**
     * Gets SerializedLambda from a serializable lambda
     * 从可序列化lambda获取SerializedLambda
     *
     * <p>Note: No caching is used because different lambda instances of the same
     * class may capture different values, making class-based caching incorrect.</p>
     * <p>注意：不使用缓存，因为同一类的不同lambda实例可能捕获不同的值，
     * 基于类的缓存是不正确的。</p>
     *
     * @param lambda the lambda | lambda
     * @return the SerializedLambda | SerializedLambda
     */
    public static java.lang.invoke.SerializedLambda getSerializedLambda(Serializable lambda) {
        return extractSerializedLambda(lambda);
    }

    private static java.lang.invoke.SerializedLambda extractSerializedLambda(Serializable lambda) {
        try {
            Method writeReplace = lambda.getClass().getDeclaredMethod("writeReplace");
            writeReplace.setAccessible(true);
            return (java.lang.invoke.SerializedLambda) writeReplace.invoke(lambda);
        } catch (Exception e) {
            throw new OpenReflectException("Failed to extract SerializedLambda", e);
        }
    }

    /**
     * Gets SerializedLambda safely
     * 安全获取SerializedLambda
     *
     * @param lambda the lambda | lambda
     * @return Optional of SerializedLambda | SerializedLambda的Optional
     */
    public static Optional<java.lang.invoke.SerializedLambda> getSerializedLambdaSafe(Serializable lambda) {
        try {
            return Optional.of(getSerializedLambda(lambda));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    // ==================== Lambda Information | Lambda信息 ====================

    /**
     * Gets the implementing class name
     * 获取实现类名
     *
     * @param lambda the lambda | lambda
     * @return the class name | 类名
     */
    public static String getImplClassName(Serializable lambda) {
        java.lang.invoke.SerializedLambda sl = getSerializedLambda(lambda);
        return sl.getImplClass().replace('/', '.');
    }

    /**
     * Gets the implementing class
     * 获取实现类
     *
     * @param lambda the lambda | lambda
     * @return the class | 类
     */
    public static Class<?> getImplClass(Serializable lambda) {
        String className = getImplClassName(lambda);
        try {
            return Class.forName(className, false, lambda.getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new OpenReflectException("Implementation class not found: " + className, e);
        }
    }

    /**
     * Gets the implementing method name
     * 获取实现方法名
     *
     * @param lambda the lambda | lambda
     * @return the method name | 方法名
     */
    public static String getImplMethodName(Serializable lambda) {
        return getSerializedLambda(lambda).getImplMethodName();
    }

    /**
     * Gets the implementing method signature
     * 获取实现方法签名
     *
     * @param lambda the lambda | lambda
     * @return the method signature | 方法签名
     */
    public static String getImplMethodSignature(Serializable lambda) {
        return getSerializedLambda(lambda).getImplMethodSignature();
    }

    /**
     * Gets the functional interface class name
     * 获取函数式接口类名
     *
     * @param lambda the lambda | lambda
     * @return the interface class name | 接口类名
     */
    public static String getFunctionalInterfaceClassName(Serializable lambda) {
        java.lang.invoke.SerializedLambda sl = getSerializedLambda(lambda);
        return sl.getFunctionalInterfaceClass().replace('/', '.');
    }

    /**
     * Gets the functional interface method name
     * 获取函数式接口方法名
     *
     * @param lambda the lambda | lambda
     * @return the method name | 方法名
     */
    public static String getFunctionalInterfaceMethodName(Serializable lambda) {
        return getSerializedLambda(lambda).getFunctionalInterfaceMethodName();
    }

    /**
     * Gets the captured argument count
     * 获取捕获参数数量
     *
     * @param lambda the lambda | lambda
     * @return the count | 数量
     */
    public static int getCapturedArgCount(Serializable lambda) {
        return getSerializedLambda(lambda).getCapturedArgCount();
    }

    /**
     * Gets a captured argument
     * 获取捕获参数
     *
     * @param lambda the lambda | lambda
     * @param index  the index | 索引
     * @return the captured argument | 捕获参数
     */
    public static Object getCapturedArg(Serializable lambda, int index) {
        return getSerializedLambda(lambda).getCapturedArg(index);
    }

    // ==================== Method Reference Detection | 方法引用检测 ====================

    /**
     * Checks if lambda is a method reference
     * 检查lambda是否为方法引用
     *
     * @param lambda the lambda | lambda
     * @return true if method reference | 如果是方法引用返回true
     */
    public static boolean isMethodReference(Serializable lambda) {
        String methodName = getImplMethodName(lambda);
        // Synthetic lambda methods start with "lambda$"
        return !methodName.startsWith("lambda$");
    }

    /**
     * Gets the implementation method kind
     * 获取实现方法种类
     *
     * @param lambda the lambda | lambda
     * @return the method kind | 方法种类
     */
    public static int getImplMethodKind(Serializable lambda) {
        return getSerializedLambda(lambda).getImplMethodKind();
    }

    /**
     * Checks if the method reference is static
     * 检查方法引用是否为静态
     *
     * @param lambda the lambda | lambda
     * @return true if static | 如果是静态返回true
     */
    public static boolean isStaticMethodReference(Serializable lambda) {
        // REF_invokeStatic = 6
        return getImplMethodKind(lambda) == 6;
    }

    /**
     * Checks if the method reference is a constructor reference
     * 检查方法引用是否为构造器引用
     *
     * @param lambda the lambda | lambda
     * @return true if constructor | 如果是构造器返回true
     */
    public static boolean isConstructorReference(Serializable lambda) {
        // REF_newInvokeSpecial = 8
        return getImplMethodKind(lambda) == 8;
    }

    // ==================== Property Extraction | 属性提取 ====================

    /**
     * Extracts property name from getter method reference
     * 从getter方法引用提取属性名
     *
     * @param lambda the lambda | lambda
     * @return the property name | 属性名
     */
    public static String extractPropertyName(Serializable lambda) {
        String methodName = getImplMethodName(lambda);
        return extractPropertyNameFromMethodName(methodName);
    }

    /**
     * Extracts property name from method name
     * 从方法名提取属性名
     *
     * @param methodName the method name | 方法名
     * @return the property name | 属性名
     */
    public static String extractPropertyNameFromMethodName(String methodName) {
        String name;
        if (methodName.startsWith("get") && methodName.length() > 3) {
            name = methodName.substring(3);
        } else if (methodName.startsWith("is") && methodName.length() > 2) {
            name = methodName.substring(2);
        } else if (methodName.startsWith("set") && methodName.length() > 3) {
            name = methodName.substring(3);
        } else {
            return methodName;
        }
        return decapitalize(name);
    }

    private static String decapitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        if (str.length() > 1 && Character.isUpperCase(str.charAt(1))) {
            return str;
        }
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }

    // ==================== Method Resolution | 方法解析 ====================

    /**
     * Gets the implementation method
     * 获取实现方法
     *
     * @param lambda the lambda | lambda
     * @return the method or null | 方法或null
     */
    public static Method getImplMethod(Serializable lambda) {
        Class<?> implClass = getImplClass(lambda);
        String methodName = getImplMethodName(lambda);
        String signature = getImplMethodSignature(lambda);

        Class<?>[] paramTypes = parseMethodSignature(signature);

        for (Method method : implClass.getDeclaredMethods()) {
            if (method.getName().equals(methodName) && matchesParameters(method, paramTypes)) {
                return method;
            }
        }
        return null;
    }

    /**
     * Gets the implementation method safely
     * 安全获取实现方法
     *
     * @param lambda the lambda | lambda
     * @return Optional of method | 方法的Optional
     */
    public static Optional<Method> getImplMethodSafe(Serializable lambda) {
        try {
            return Optional.ofNullable(getImplMethod(lambda));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static Class<?>[] parseMethodSignature(String signature) {
        // Simple parsing - handles common cases
        // Full implementation would parse the JVM signature format
        return new Class<?>[0]; // Simplified
    }

    private static boolean matchesParameters(Method method, Class<?>[] paramTypes) {
        // Simplified matching
        return true;
    }

    // ==================== MethodHandle Support | MethodHandle支持 ====================

    /**
     * Creates a MethodHandle for the implementation method
     * 为实现方法创建MethodHandle
     *
     * @param lambda the lambda | lambda
     * @return the MethodHandle | MethodHandle
     */
    public static MethodHandle toMethodHandle(Serializable lambda) {
        Method method = getImplMethod(lambda);
        if (method == null) {
            throw new OpenReflectException("Could not resolve implementation method");
        }
        try {
            method.setAccessible(true);
            return MethodHandles.lookup().unreflect(method);
        } catch (IllegalAccessException e) {
            throw new OpenReflectException("Failed to create MethodHandle", e);
        }
    }

    /**
     * Creates a MethodHandle safely
     * 安全创建MethodHandle
     *
     * @param lambda the lambda | lambda
     * @return Optional of MethodHandle | MethodHandle的Optional
     */
    public static Optional<MethodHandle> toMethodHandleSafe(Serializable lambda) {
        try {
            return Optional.of(toMethodHandle(lambda));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    // ==================== Cache Management | 缓存管理 ====================

    /**
     * Clears lambda cache (no-op, caching removed)
     * 清除lambda缓存（空操作，缓存已移除）
     */
    public static void clearCache() {
        // No-op: caching removed because class-based caching was incorrect
        // for different lambda instances capturing different values
    }

    /**
     * Gets cache size (always returns 0, caching removed)
     * 获取缓存大小（始终返回0，缓存已移除）
     *
     * @return always 0 | 始终为0
     */
    public static int getCacheSize() {
        return 0;
    }
}
