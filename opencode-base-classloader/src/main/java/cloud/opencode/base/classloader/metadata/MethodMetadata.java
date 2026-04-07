package cloud.opencode.base.classloader.metadata;

import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Method Metadata - Immutable method information
 * 方法元数据 - 不可变的方法信息
 *
 * <p>Represents method metadata read from class files without loading the class.</p>
 * <p>表示从类文件读取的方法元数据，无需加载类。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Method signature information - 方法签名信息</li>
 *   <li>Parameter types and names - 参数类型和名称</li>
 *   <li>Modifier information - 修饰符信息</li>
 *   <li>Annotation information - 注解信息</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * MethodMetadata method = classMetadata.methods().get(0);
 * String name = method.methodName();
 * String returnType = method.returnType();
 * boolean isGetter = method.isGetter();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是 (不可变)</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
public final class MethodMetadata {

    private final String methodName;
    private final String returnType;
    private final List<String> parameterTypes;
    private final List<String> parameterNames;
    private final List<String> exceptionTypes;
    private final int modifiers;
    private final boolean isSynthetic;
    private final boolean isBridge;
    private final boolean isDefault;
    private final List<AnnotationMetadata> annotations;
    private final List<List<AnnotationMetadata>> parameterAnnotations;
    private final String genericSignature;
    private final String genericReturnType;
    private final List<String> genericParameterTypes;

    /**
     * Create method metadata
     * 创建方法元数据
     *
     * @param methodName           method name | 方法名
     * @param returnType           return type | 返回类型
     * @param parameterTypes       parameter types | 参数类型
     * @param parameterNames       parameter names | 参数名称
     * @param exceptionTypes       exception types | 异常类型
     * @param modifiers            modifier flags | 修饰符标志
     * @param isSynthetic          is synthetic | 是否合成
     * @param isBridge             is bridge method | 是否桥接方法
     * @param isDefault            is default method | 是否默认方法
     * @param annotations          method annotations | 方法注解
     * @param parameterAnnotations parameter annotations | 参数注解
     */
    public MethodMetadata(String methodName, String returnType,
                          List<String> parameterTypes, List<String> parameterNames,
                          List<String> exceptionTypes, int modifiers,
                          boolean isSynthetic, boolean isBridge, boolean isDefault,
                          List<AnnotationMetadata> annotations,
                          List<List<AnnotationMetadata>> parameterAnnotations) {
        this(methodName, returnType, parameterTypes, parameterNames, exceptionTypes,
                modifiers, isSynthetic, isBridge, isDefault, annotations, parameterAnnotations,
                null, null, null);
    }

    /**
     * Create method metadata with generic type information
     * 创建带有泛型类型信息的方法元数据
     *
     * @param methodName           method name | 方法名
     * @param returnType           return type | 返回类型
     * @param parameterTypes       parameter types | 参数类型
     * @param parameterNames       parameter names | 参数名称
     * @param exceptionTypes       exception types | 异常类型
     * @param modifiers            modifier flags | 修饰符标志
     * @param isSynthetic          is synthetic | 是否合成
     * @param isBridge             is bridge method | 是否桥接方法
     * @param isDefault            is default method | 是否默认方法
     * @param annotations          method annotations | 方法注解
     * @param parameterAnnotations parameter annotations | 参数注解
     * @param genericSignature     generic signature | 泛型签名
     * @param genericReturnType    generic return type | 泛型返回类型
     * @param genericParameterTypes generic parameter types | 泛型参数类型列表
     */
    public MethodMetadata(String methodName, String returnType,
                          List<String> parameterTypes, List<String> parameterNames,
                          List<String> exceptionTypes, int modifiers,
                          boolean isSynthetic, boolean isBridge, boolean isDefault,
                          List<AnnotationMetadata> annotations,
                          List<List<AnnotationMetadata>> parameterAnnotations,
                          String genericSignature, String genericReturnType,
                          List<String> genericParameterTypes) {
        this.methodName = Objects.requireNonNull(methodName, "Method name must not be null");
        this.returnType = Objects.requireNonNull(returnType, "Return type must not be null");
        this.parameterTypes = parameterTypes != null ? List.copyOf(parameterTypes) : List.of();
        this.parameterNames = parameterNames != null ? List.copyOf(parameterNames) : List.of();
        this.exceptionTypes = exceptionTypes != null ? List.copyOf(exceptionTypes) : List.of();
        this.modifiers = modifiers;
        this.isSynthetic = isSynthetic;
        this.isBridge = isBridge;
        this.isDefault = isDefault;
        this.annotations = annotations != null ? List.copyOf(annotations) : List.of();
        this.parameterAnnotations = parameterAnnotations != null ?
                parameterAnnotations.stream().map(List::copyOf).toList() : List.of();
        this.genericSignature = genericSignature;
        this.genericReturnType = genericReturnType;
        this.genericParameterTypes = genericParameterTypes != null ?
                List.copyOf(genericParameterTypes) : List.of();
    }

    // ==================== Getters ====================

    /**
     * Get method name
     * 获取方法名
     *
     * @return method name | 方法名
     */
    public String methodName() {
        return methodName;
    }

    /**
     * Get return type
     * 获取返回类型
     *
     * @return return type | 返回类型
     */
    public String returnType() {
        return returnType;
    }

    /**
     * Get parameter types
     * 获取参数类型
     *
     * @return list of parameter types | 参数类型列表
     */
    public List<String> parameterTypes() {
        return parameterTypes;
    }

    /**
     * Get parameter names
     * 获取参数名称
     *
     * @return list of parameter names | 参数名称列表
     */
    public List<String> parameterNames() {
        return parameterNames;
    }

    /**
     * Get exception types
     * 获取异常类型
     *
     * @return list of exception types | 异常类型列表
     */
    public List<String> exceptionTypes() {
        return exceptionTypes;
    }

    /**
     * Get modifiers
     * 获取修饰符
     *
     * @return modifier flags | 修饰符标志
     */
    public int modifiers() {
        return modifiers;
    }

    /**
     * Check if synthetic
     * 检查是否为合成方法
     *
     * @return true if synthetic | 是合成方法返回 true
     */
    public boolean isSynthetic() {
        return isSynthetic;
    }

    /**
     * Check if bridge method
     * 检查是否为桥接方法
     *
     * @return true if bridge | 是桥接方法返回 true
     */
    public boolean isBridge() {
        return isBridge;
    }

    /**
     * Check if default method
     * 检查是否为默认方法
     *
     * @return true if default | 是默认方法返回 true
     */
    public boolean isDefault() {
        return isDefault;
    }

    /**
     * Get annotations
     * 获取注解
     *
     * @return list of annotations | 注解列表
     */
    public List<AnnotationMetadata> annotations() {
        return annotations;
    }

    /**
     * Get parameter annotations
     * 获取参数注解
     *
     * @return list of parameter annotation lists | 参数注解列表的列表
     */
    public List<List<AnnotationMetadata>> parameterAnnotations() {
        return parameterAnnotations;
    }

    /**
     * Get generic signature of the method
     * 获取方法的泛型签名
     *
     * @return generic signature or null if not generic | 泛型签名，非泛型方法返回 null
     */
    public String getGenericSignature() {
        return genericSignature;
    }

    /**
     * Get generic return type (e.g. "List&lt;String&gt;" instead of "List")
     * 获取泛型返回类型（如 "List&lt;String&gt;" 而非 "List"）
     *
     * @return generic return type or null | 泛型返回类型或 null
     */
    public String getGenericReturnType() {
        return genericReturnType;
    }

    /**
     * Get generic parameter types
     * 获取泛型参数类型列表
     *
     * @return list of generic parameter type strings | 泛型参数类型字符串列表
     */
    public List<String> getGenericParameterTypes() {
        return genericParameterTypes;
    }

    // ==================== Modifier Checks | 修饰符检查 ====================

    /**
     * Check if abstract
     * 检查是否为抽象方法
     *
     * @return true if abstract | 是抽象方法返回 true
     */
    public boolean isAbstract() {
        return Modifier.isAbstract(modifiers);
    }

    /**
     * Check if static
     * 检查是否为静态方法
     *
     * @return true if static | 是静态方法返回 true
     */
    public boolean isStatic() {
        return Modifier.isStatic(modifiers);
    }

    /**
     * Check if final
     * 检查是否为 final 方法
     *
     * @return true if final | 是 final 方法返回 true
     */
    public boolean isFinal() {
        return Modifier.isFinal(modifiers);
    }

    /**
     * Check if public
     * 检查是否为 public 方法
     *
     * @return true if public | 是 public 方法返回 true
     */
    public boolean isPublic() {
        return Modifier.isPublic(modifiers);
    }

    /**
     * Check if private
     * 检查是否为 private 方法
     *
     * @return true if private | 是 private 方法返回 true
     */
    public boolean isPrivate() {
        return Modifier.isPrivate(modifiers);
    }

    /**
     * Check if protected
     * 检查是否为 protected 方法
     *
     * @return true if protected | 是 protected 方法返回 true
     */
    public boolean isProtected() {
        return Modifier.isProtected(modifiers);
    }

    // ==================== Convenience Methods | 便捷方法 ====================

    /**
     * Check if has specified annotation
     * 检查是否有指定注解
     *
     * @param annotationClassName annotation class name | 注解类名
     * @return true if has annotation | 有注解返回 true
     */
    public boolean hasAnnotation(String annotationClassName) {
        return annotations.stream()
                .anyMatch(a -> a.annotationType().equals(annotationClassName));
    }

    /**
     * Get specified annotation
     * 获取指定注解
     *
     * @param annotationClassName annotation class name | 注解类名
     * @return optional annotation | 可选的注解
     */
    public Optional<AnnotationMetadata> getAnnotation(String annotationClassName) {
        return annotations.stream()
                .filter(a -> a.annotationType().equals(annotationClassName))
                .findFirst();
    }

    /**
     * Get method signature
     * 获取方法签名
     *
     * @return method signature | 方法签名
     */
    public String getSignature() {
        return methodName + "(" + String.join(", ", parameterTypes) + ")";
    }

    /**
     * Get parameter count
     * 获取参数数量
     *
     * @return parameter count | 参数数量
     */
    public int parameterCount() {
        return parameterTypes.size();
    }

    /**
     * Check if no parameters
     * 检查是否无参数
     *
     * @return true if no parameters | 无参数返回 true
     */
    public boolean hasNoParameters() {
        return parameterTypes.isEmpty();
    }

    /**
     * Check if is getter method
     * 检查是否为 getter 方法
     *
     * @return true if getter | 是 getter 返回 true
     */
    public boolean isGetter() {
        return (methodName.startsWith("get") || methodName.startsWith("is"))
                && parameterTypes.isEmpty()
                && !"void".equals(returnType);
    }

    /**
     * Check if is setter method
     * 检查是否为 setter 方法
     *
     * @return true if setter | 是 setter 返回 true
     */
    public boolean isSetter() {
        return methodName.startsWith("set")
                && parameterTypes.size() == 1
                && "void".equals(returnType);
    }

    /**
     * Check if is constructor
     * 检查是否为构造方法
     *
     * @return true if constructor | 是构造方法返回 true
     */
    public boolean isConstructor() {
        return "<init>".equals(methodName);
    }

    /**
     * Check if is static initializer
     * 检查是否为静态初始化块
     *
     * @return true if static initializer | 是静态初始化块返回 true
     */
    public boolean isStaticInitializer() {
        return "<clinit>".equals(methodName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MethodMetadata that)) return false;
        return methodName.equals(that.methodName) && parameterTypes.equals(that.parameterTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(methodName, parameterTypes);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String mods = Modifier.toString(modifiers);
        if (!mods.isEmpty()) sb.append(mods).append(" ");
        sb.append(getSimpleReturnType()).append(" ").append(methodName);
        sb.append("(");
        for (int i = 0; i < parameterTypes.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(getSimpleTypeName(parameterTypes.get(i)));
            if (i < parameterNames.size()) {
                sb.append(" ").append(parameterNames.get(i));
            }
        }
        sb.append(")");
        return sb.toString();
    }

    private String getSimpleReturnType() {
        return getSimpleTypeName(returnType);
    }

    private String getSimpleTypeName(String typeName) {
        int lastDot = typeName.lastIndexOf('.');
        return lastDot != -1 ? typeName.substring(lastDot + 1) : typeName;
    }
}
