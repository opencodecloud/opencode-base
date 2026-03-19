package cloud.opencode.base.reflect.lambda;

import cloud.opencode.base.reflect.exception.OpenReflectException;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Lambda Information Holder
 * Lambda信息持有者
 *
 * <p>Holds metadata extracted from a serialized lambda expression.</p>
 * <p>持有从序列化lambda表达式提取的元数据。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Lambda implementation class discovery - Lambda实现类发现</li>
 *   <li>Implementation method access - 实现方法访问</li>
 *   <li>SerializedLambda metadata extraction - SerializedLambda元数据提取</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * LambdaInfo info = LambdaInfo.from((SerializableFunction<User, String>) User::getName);
 * String methodName = info.getImplMethodName();
 * Class<?> implClass = info.getImplClass();
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
public class LambdaInfo {

    private final SerializedLambda serializedLambda;
    private final Class<?> implClass;
    private final Method implMethod;

    /**
     * Creates a LambdaInfo
     * 创建LambdaInfo
     *
     * @param serializedLambda the serialized lambda | 序列化的lambda
     * @param implClass        the implementation class | 实现类
     * @param implMethod       the implementation method | 实现方法
     */
    public LambdaInfo(SerializedLambda serializedLambda, Class<?> implClass, Method implMethod) {
        this.serializedLambda = Objects.requireNonNull(serializedLambda);
        this.implClass = implClass;
        this.implMethod = implMethod;
    }

    /**
     * Creates LambdaInfo from a serializable lambda
     * 从可序列化lambda创建LambdaInfo
     *
     * @param lambda the lambda | lambda
     * @return the LambdaInfo | LambdaInfo
     */
    public static LambdaInfo from(Serializable lambda) {
        try {
            Method method = lambda.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(true);
            SerializedLambda serialized = (SerializedLambda) method.invoke(lambda);

            String implClassName = serialized.getImplClass().replace('/', '.');
            Class<?> implClass = Class.forName(implClassName);

            Method implMethod = findImplMethod(implClass, serialized);

            return new LambdaInfo(serialized, implClass, implMethod);
        } catch (Exception e) {
            throw new OpenReflectException("Failed to extract lambda info", e);
        }
    }

    /**
     * Gets the functional interface class name
     * 获取函数式接口类名
     *
     * @return the interface class name | 接口类名
     */
    public String getFunctionalInterfaceClassName() {
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
     * Gets the implementation class name
     * 获取实现类名
     *
     * @return the class name | 类名
     */
    public String getImplClassName() {
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
     * Gets the implementation class
     * 获取实现类
     *
     * @return the implementation class | 实现类
     */
    public Class<?> getImplClass() {
        return implClass;
    }

    /**
     * Gets the implementation method
     * 获取实现方法
     *
     * @return the implementation method or null | 实现方法或null
     */
    public Method getImplMethod() {
        return implMethod;
    }

    /**
     * Gets the number of captured arguments
     * 获取捕获参数数量
     *
     * @return the count | 数量
     */
    public int getCapturedArgCount() {
        return serializedLambda.getCapturedArgCount();
    }

    /**
     * Gets a captured argument
     * 获取捕获的参数
     *
     * @param index the index | 索引
     * @return the captured argument | 捕获的参数
     */
    public Object getCapturedArg(int index) {
        return serializedLambda.getCapturedArg(index);
    }

    /**
     * Gets all captured arguments
     * 获取所有捕获的参数
     *
     * @return array of captured arguments | 捕获参数数组
     */
    public Object[] getCapturedArgs() {
        int count = getCapturedArgCount();
        Object[] args = new Object[count];
        for (int i = 0; i < count; i++) {
            args[i] = getCapturedArg(i);
        }
        return args;
    }

    /**
     * Gets the underlying SerializedLambda
     * 获取底层SerializedLambda
     *
     * @return the SerializedLambda | SerializedLambda
     */
    public SerializedLambda getSerializedLambda() {
        return serializedLambda;
    }

    /**
     * Checks if this is a method reference
     * 检查是否为方法引用
     *
     * @return true if method reference | 如果是方法引用返回true
     */
    public boolean isMethodReference() {
        return !serializedLambda.getImplMethodName().startsWith("lambda$");
    }

    /**
     * Gets the impl method kind
     * 获取实现方法类型
     *
     * @return the method kind | 方法类型
     */
    public int getImplMethodKind() {
        return serializedLambda.getImplMethodKind();
    }

    private static Method findImplMethod(Class<?> implClass, SerializedLambda serialized) {
        String methodName = serialized.getImplMethodName();
        for (Method method : implClass.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "LambdaInfo[" + getImplClassName() + "::" + getImplMethodName() + "]";
    }
}
