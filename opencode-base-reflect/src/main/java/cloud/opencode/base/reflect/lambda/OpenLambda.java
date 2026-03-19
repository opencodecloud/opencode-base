package cloud.opencode.base.reflect.lambda;

import cloud.opencode.base.reflect.exception.OpenReflectException;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.*;

/**
 * Lambda Facade Entry Class
 * Lambda门面入口类
 *
 * <p>Provides common lambda operations API.</p>
 * <p>提供常用lambda操作API。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Lambda metadata extraction - Lambda元数据提取</li>
 *   <li>Method reference detection - 方法引用检测</li>
 *   <li>Functional interface utilities - 函数式接口工具</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Extract lambda info
 * LambdaInfo info = OpenLambda.getInfo((SerializableFunction<User, String>) User::getName);
 *
 * // Get implementation method
 * Method method = OpenLambda.getImplMethod((SerializableFunction<User, String>) User::getName);
 *
 * // Get field name from getter reference
 * String fieldName = OpenLambda.getFieldName((SerializableFunction<User, String>) User::getName);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: No (caller must ensure non-null lambda) - 空值安全: 否（调用方须确保非空lambda）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public final class OpenLambda {

    private OpenLambda() {
    }

    // ==================== Lambda Metadata | Lambda元数据 ====================

    /**
     * Extracts metadata from a serializable lambda
     * 从可序列化lambda提取元数据
     *
     * @param lambda the lambda | lambda
     * @return the LambdaInfo | LambdaInfo
     */
    public static LambdaInfo getInfo(Serializable lambda) {
        return LambdaInfo.from(lambda);
    }

    /**
     * Gets the implementation method of a lambda
     * 获取lambda的实现方法
     *
     * @param lambda the lambda | lambda
     * @return Optional of the method | 方法的Optional
     */
    public static Optional<Method> getImplMethod(Serializable lambda) {
        try {
            return Optional.ofNullable(getInfo(lambda).getImplMethod());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Gets the implementation method name
     * 获取实现方法名
     *
     * @param lambda the lambda | lambda
     * @return the method name | 方法名
     */
    public static String getImplMethodName(Serializable lambda) {
        return getInfo(lambda).getImplMethodName();
    }

    /**
     * Gets the implementation class
     * 获取实现类
     *
     * @param lambda the lambda | lambda
     * @return the implementation class | 实现类
     */
    public static Class<?> getImplClass(Serializable lambda) {
        return getInfo(lambda).getImplClass();
    }

    /**
     * Checks if lambda is a method reference
     * 检查lambda是否为方法引用
     *
     * @param lambda the lambda | lambda
     * @return true if method reference | 如果是方法引用返回true
     */
    public static boolean isMethodReference(Serializable lambda) {
        return getInfo(lambda).isMethodReference();
    }

    // ==================== Property Extraction | 属性提取 ====================

    /**
     * Extracts property name from a getter method reference
     * 从getter方法引用提取属性名
     *
     * @param getter the getter method reference | getter方法引用
     * @param <T>    the bean type | bean类型
     * @param <R>    the property type | 属性类型
     * @return the property name | 属性名
     */
    public static <T, R> String getPropertyName(SerializableFunction<T, R> getter) {
        LambdaInfo info = getInfo(getter);
        String methodName = info.getImplMethodName();
        return extractPropertyNameFromGetter(methodName);
    }

    /**
     * Extracts property name from a setter method reference
     * 从setter方法引用提取属性名
     *
     * @param setter the setter method reference | setter方法引用
     * @param <T>    the bean type | bean类型
     * @return the property name | 属性名
     */
    public static <T> String getPropertyNameFromSetter(SerializableConsumer<T> setter) {
        LambdaInfo info = getInfo(setter);
        String methodName = info.getImplMethodName();
        return extractPropertyNameFromSetter(methodName);
    }

    /**
     * Gets the property class from a getter
     * 从getter获取属性类
     *
     * @param getter the getter | getter
     * @param <T>    the bean type | bean类型
     * @param <R>    the property type | 属性类型
     * @return the property class | 属性类
     */
    public static <T, R> Class<?> getPropertyClass(SerializableFunction<T, R> getter) {
        LambdaInfo info = getInfo(getter);
        Method method = info.getImplMethod();
        return method != null ? method.getReturnType() : null;
    }

    // ==================== Functional Interface | 函数式接口 ====================

    /**
     * Checks if a class is a functional interface
     * 检查类是否为函数式接口
     *
     * @param clazz the class | 类
     * @return true if functional interface | 如果是函数式接口返回true
     */
    public static boolean isFunctionalInterface(Class<?> clazz) {
        return FunctionalInterfaceUtil.isFunctionalInterface(clazz);
    }

    /**
     * Gets the single abstract method of a functional interface
     * 获取函数式接口的单一抽象方法
     *
     * @param clazz the class | 类
     * @return Optional of the SAM | SAM的Optional
     */
    public static Optional<Method> getSingleAbstractMethod(Class<?> clazz) {
        return FunctionalInterfaceUtil.getSingleAbstractMethod(clazz);
    }

    /**
     * Classifies a functional interface
     * 分类函数式接口
     *
     * @param clazz the class | 类
     * @return the category | 类别
     */
    public static FunctionalInterfaceUtil.FunctionalCategory classify(Class<?> clazz) {
        return FunctionalInterfaceUtil.classify(clazz);
    }

    // ==================== Utility Methods | 工具方法 ====================

    /**
     * Creates a supplier from a value
     * 从值创建提供者
     *
     * @param value the value | 值
     * @param <T>   the type | 类型
     * @return the supplier | 提供者
     */
    public static <T> Supplier<T> constant(T value) {
        return () -> value;
    }

    /**
     * Creates a predicate that always returns true
     * 创建总是返回true的谓词
     *
     * @param <T> the type | 类型
     * @return the predicate | 谓词
     */
    public static <T> Predicate<T> alwaysTrue() {
        return t -> true;
    }

    /**
     * Creates a predicate that always returns false
     * 创建总是返回false的谓词
     *
     * @param <T> the type | 类型
     * @return the predicate | 谓词
     */
    public static <T> Predicate<T> alwaysFalse() {
        return t -> false;
    }

    /**
     * Creates an identity function
     * 创建恒等函数
     *
     * @param <T> the type | 类型
     * @return the identity function | 恒等函数
     */
    public static <T> Function<T, T> identity() {
        return t -> t;
    }

    /**
     * Creates a no-op consumer
     * 创建空操作消费者
     *
     * @param <T> the type | 类型
     * @return the no-op consumer | 空操作消费者
     */
    public static <T> Consumer<T> noOp() {
        return t -> {};
    }

    /**
     * Creates a runnable from a consumer and value
     * 从消费者和值创建可运行对象
     *
     * @param consumer the consumer | 消费者
     * @param value    the value | 值
     * @param <T>      the type | 类型
     * @return the runnable | 可运行对象
     */
    public static <T> Runnable bind(Consumer<T> consumer, T value) {
        return () -> consumer.accept(value);
    }

    /**
     * Creates a supplier from a function and input
     * 从函数和输入创建提供者
     *
     * @param function the function | 函数
     * @param input    the input | 输入
     * @param <T>      the input type | 输入类型
     * @param <R>      the result type | 结果类型
     * @return the supplier | 提供者
     */
    public static <T, R> Supplier<R> bind(Function<T, R> function, T input) {
        return () -> function.apply(input);
    }

    /**
     * Safely wraps a throwing function
     * 安全包装可抛出异常的函数
     *
     * @param function the throwing function | 可抛出异常的函数
     * @param <T>      the input type | 输入类型
     * @param <R>      the result type | 结果类型
     * @return the safe function | 安全函数
     */
    public static <T, R> Function<T, R> safe(ThrowingFunction<T, R> function) {
        return t -> {
            try {
                return function.apply(t);
            } catch (Exception e) {
                throw new OpenReflectException("Function failed", e);
            }
        };
    }

    /**
     * Safely wraps a throwing consumer
     * 安全包装可抛出异常的消费者
     *
     * @param consumer the throwing consumer | 可抛出异常的消费者
     * @param <T>      the input type | 输入类型
     * @return the safe consumer | 安全消费者
     */
    public static <T> Consumer<T> safe(ThrowingConsumer<T> consumer) {
        return t -> {
            try {
                consumer.accept(t);
            } catch (Exception e) {
                throw new OpenReflectException("Consumer failed", e);
            }
        };
    }

    /**
     * Safely wraps a throwing supplier
     * 安全包装可抛出异常的提供者
     *
     * @param supplier the throwing supplier | 可抛出异常的提供者
     * @param <T>      the result type | 结果类型
     * @return the safe supplier | 安全提供者
     */
    public static <T> Supplier<T> safe(ThrowingSupplier<T> supplier) {
        return () -> {
            try {
                return supplier.get();
            } catch (Exception e) {
                throw new OpenReflectException("Supplier failed", e);
            }
        };
    }

    private static String extractPropertyNameFromGetter(String methodName) {
        String name;
        if (methodName.startsWith("get") && methodName.length() > 3) {
            name = methodName.substring(3);
        } else if (methodName.startsWith("is") && methodName.length() > 2) {
            name = methodName.substring(2);
        } else {
            return methodName;
        }
        return decapitalize(name);
    }

    private static String extractPropertyNameFromSetter(String methodName) {
        if (methodName.startsWith("set") && methodName.length() > 3) {
            return decapitalize(methodName.substring(3));
        }
        return methodName;
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

    /**
     * A function that can throw checked exceptions
     * 可以抛出检查异常的函数
     *
     * @param <T> the input type | 输入类型
     * @param <R> the result type | 结果类型
     */
    @FunctionalInterface
    public interface ThrowingFunction<T, R> {
        /**
         * Applies this function
         * 应用此函数
         *
         * @param t the input | 输入
         * @return the result | 结果
         * @throws Exception if an error occurs | 如果发生错误
         */
        R apply(T t) throws Exception;
    }

    /**
     * A consumer that can throw checked exceptions
     * 可以抛出检查异常的消费者
     *
     * @param <T> the input type | 输入类型
     */
    @FunctionalInterface
    public interface ThrowingConsumer<T> {
        /**
         * Performs this operation
         * 执行此操作
         *
         * @param t the input | 输入
         * @throws Exception if an error occurs | 如果发生错误
         */
        void accept(T t) throws Exception;
    }

    /**
     * A supplier that can throw checked exceptions
     * 可以抛出检查异常的提供者
     *
     * @param <T> the result type | 结果类型
     */
    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        /**
         * Gets a result
         * 获取结果
         *
         * @return the result | 结果
         * @throws Exception if an error occurs | 如果发生错误
         */
        T get() throws Exception;
    }
}
