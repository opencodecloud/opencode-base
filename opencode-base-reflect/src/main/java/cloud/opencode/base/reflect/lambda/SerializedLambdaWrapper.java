package cloud.opencode.base.reflect.lambda;

import cloud.opencode.base.reflect.exception.OpenReflectException;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * SerializedLambda Wrapper Class
 * SerializedLambda包装类
 *
 * <p>Provides a convenient wrapper around java.lang.invoke.SerializedLambda.</p>
 * <p>提供java.lang.invoke.SerializedLambda的便捷包装。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>SerializedLambda wrapping with convenience methods - SerializedLambda便捷方法包装</li>
 *   <li>Implementation class and method resolution - 实现类和方法解析</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SerializedLambdaWrapper wrapper = SerializedLambdaWrapper.of(lambda);
 * String methodName = wrapper.getImplMethodName();
 * Optional<Method> method = wrapper.getImplMethod();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: No (lambda must be non-null and serializable) - 空值安全: 否（lambda须非空且可序列化）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public final class SerializedLambdaWrapper {

    private final java.lang.invoke.SerializedLambda serializedLambda;
    private final Serializable lambda;

    private SerializedLambdaWrapper(Serializable lambda, java.lang.invoke.SerializedLambda serializedLambda) {
        this.lambda = lambda;
        this.serializedLambda = serializedLambda;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates a wrapper from a serializable lambda
     * 从可序列化lambda创建包装器
     *
     * @param lambda the lambda | lambda
     * @return the wrapper | 包装器
     */
    public static SerializedLambdaWrapper from(Serializable lambda) {
        java.lang.invoke.SerializedLambda sl = LambdaUtil.getSerializedLambda(lambda);
        return new SerializedLambdaWrapper(lambda, sl);
    }

    /**
     * Creates a wrapper safely
     * 安全创建包装器
     *
     * @param lambda the lambda | lambda
     * @return Optional of wrapper | 包装器的Optional
     */
    public static Optional<SerializedLambdaWrapper> fromSafe(Serializable lambda) {
        try {
            return Optional.of(from(lambda));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    // ==================== Delegate Methods | 委托方法 ====================

    /**
     * Gets the capturing class
     * 获取捕获类
     *
     * @return the capturing class name | 捕获类名
     */
    public String getCapturingClass() {
        return serializedLambda.getCapturingClass().replace('/', '.');
    }

    /**
     * Gets the functional interface class
     * 获取函数式接口类
     *
     * @return the functional interface class name | 函数式接口类名
     */
    public String getFunctionalInterfaceClass() {
        return serializedLambda.getFunctionalInterfaceClass().replace('/', '.');
    }

    /**
     * Gets the functional interface method name
     * 获取函数式接口方法名
     *
     * @return the method name | 方法名
     */
    public String getFunctionalInterfaceMethodName() {
        return serializedLambda.getFunctionalInterfaceMethodName();
    }

    /**
     * Gets the functional interface method signature
     * 获取函数式接口方法签名
     *
     * @return the method signature | 方法签名
     */
    public String getFunctionalInterfaceMethodSignature() {
        return serializedLambda.getFunctionalInterfaceMethodSignature();
    }

    /**
     * Gets the implementation class
     * 获取实现类
     *
     * @return the implementation class name | 实现类名
     */
    public String getImplClass() {
        return serializedLambda.getImplClass().replace('/', '.');
    }

    /**
     * Gets the implementation method name
     * 获取实现方法名
     *
     * @return the method name | 方法名
     */
    public String getImplMethodName() {
        return serializedLambda.getImplMethodName();
    }

    /**
     * Gets the implementation method signature
     * 获取实现方法签名
     *
     * @return the method signature | 方法签名
     */
    public String getImplMethodSignature() {
        return serializedLambda.getImplMethodSignature();
    }

    /**
     * Gets the implementation method kind
     * 获取实现方法种类
     *
     * @return the method kind | 方法种类
     */
    public int getImplMethodKind() {
        return serializedLambda.getImplMethodKind();
    }

    /**
     * Gets the instantiated method type
     * 获取实例化方法类型
     *
     * @return the method type | 方法类型
     */
    public String getInstantiatedMethodType() {
        return serializedLambda.getInstantiatedMethodType();
    }

    /**
     * Gets the captured argument count
     * 获取捕获参数数量
     *
     * @return the count | 数量
     */
    public int getCapturedArgCount() {
        return serializedLambda.getCapturedArgCount();
    }

    /**
     * Gets a captured argument
     * 获取捕获参数
     *
     * @param index the index | 索引
     * @return the captured argument | 捕获参数
     */
    public Object getCapturedArg(int index) {
        return serializedLambda.getCapturedArg(index);
    }

    // ==================== Convenience Methods | 便捷方法 ====================

    /**
     * Checks if this is a method reference
     * 检查是否为方法引用
     *
     * @return true if method reference | 如果是方法引用返回true
     */
    public boolean isMethodReference() {
        return !getImplMethodName().startsWith("lambda$");
    }

    /**
     * Checks if this is a static method reference
     * 检查是否为静态方法引用
     *
     * @return true if static | 如果是静态返回true
     */
    public boolean isStaticMethodReference() {
        // REF_invokeStatic = 6
        return getImplMethodKind() == 6;
    }

    /**
     * Checks if this is a constructor reference
     * 检查是否为构造器引用
     *
     * @return true if constructor | 如果是构造器返回true
     */
    public boolean isConstructorReference() {
        // REF_newInvokeSpecial = 8
        return getImplMethodKind() == 8;
    }

    /**
     * Checks if this is an instance method reference
     * 检查是否为实例方法引用
     *
     * @return true if instance method | 如果是实例方法返回true
     */
    public boolean isInstanceMethodReference() {
        // REF_invokeVirtual = 5, REF_invokeSpecial = 7, REF_invokeInterface = 9
        int kind = getImplMethodKind();
        return kind == 5 || kind == 7 || kind == 9;
    }

    /**
     * Gets the property name (for getter/setter method references)
     * 获取属性名（用于getter/setter方法引用）
     *
     * @return the property name | 属性名
     */
    public String getPropertyName() {
        return LambdaUtil.extractPropertyNameFromMethodName(getImplMethodName());
    }

    /**
     * Gets the implementation class as Class object
     * 获取实现类作为Class对象
     *
     * @return the class | 类
     */
    public Class<?> getImplClassAsClass() {
        try {
            return Class.forName(getImplClass(), false, lambda.getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new OpenReflectException("Implementation class not found: " + getImplClass(), e);
        }
    }

    /**
     * Gets the implementation method
     * 获取实现方法
     *
     * @return Optional of method | 方法的Optional
     */
    public Optional<Method> getImplMethod() {
        return LambdaUtil.getImplMethodSafe(lambda);
    }

    /**
     * Gets the underlying SerializedLambda
     * 获取底层SerializedLambda
     *
     * @return the SerializedLambda | SerializedLambda
     */
    public java.lang.invoke.SerializedLambda unwrap() {
        return serializedLambda;
    }

    @Override
    public String toString() {
        return "SerializedLambdaWrapper{" +
               "capturingClass=" + getCapturingClass() +
               ", implClass=" + getImplClass() +
               ", implMethodName=" + getImplMethodName() +
               ", methodKind=" + getImplMethodKind() +
               ", isMethodReference=" + isMethodReference() +
               '}';
    }
}
