package cloud.opencode.base.reflect.invokable;

import cloud.opencode.base.reflect.exception.OpenReflectException;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Method Signature for matching and override detection
 * 方法签名 - 用于匹配和覆盖检测
 *
 * <p>Represents a method signature consisting of name, parameter types, and optionally
 * a return type. Supports signature matching, override compatibility detection, and
 * JVM descriptor generation.</p>
 * <p>表示由名称、参数类型和可选返回类型组成的方法签名。
 * 支持签名匹配、覆盖兼容性检测和JVM描述符生成。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Signature matching by name and parameter types - 按名称和参数类型匹配签名</li>
 *   <li>Override detection with covariant return type support - 支持协变返回类型的覆盖检测</li>
 *   <li>JVM method descriptor generation - JVM方法描述符生成</li>
 *   <li>Human-readable signature formatting - 人类可读的签名格式化</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // From Method
 * MethodSignature sig = MethodSignature.of(method);
 *
 * // From name and parameter types
 * MethodSignature sig = MethodSignature.of("foo", String.class, int.class);
 *
 * // Check override compatibility
 * boolean canOverride = sig.isOverrideOf(superMethod);
 *
 * // Generate JVM descriptor
 * String descriptor = sig.toDescriptor(); // e.g., "(Ljava/lang/String;I)V"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: No (name and parameter types must be non-null) - 空值安全: 否（名称和参数类型须非空）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.3
 */
public final class MethodSignature {

    /**
     * Method name
     * 方法名称
     */
    private final String name;

    /**
     * Parameter types (defensive copy, never null)
     * 参数类型（防御性复制，不为null）
     */
    private final Class<?>[] parameterTypes;

    /**
     * Return type (may be null if not specified)
     * 返回类型（如果未指定可以为null）
     */
    private final Class<?> returnType;

    // ==================== Constructor | 构造方法 ====================

    private MethodSignature(String name, Class<?> returnType, Class<?>[] parameterTypes) {
        if (name == null || name.isBlank()) {
            throw new OpenReflectException("Method signature name must not be null, empty, or blank");
        }
        Objects.requireNonNull(parameterTypes, "Parameter types must not be null");
        for (int i = 0; i < parameterTypes.length; i++) {
            if (parameterTypes[i] == null) {
                throw new OpenReflectException("Parameter type at index " + i + " must not be null");
            }
        }
        this.name = name;
        this.parameterTypes = parameterTypes.clone();
        this.returnType = returnType;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates a MethodSignature from a Method, extracting name, parameter types, and return type
     * 从Method创建MethodSignature，提取名称、参数类型和返回类型
     *
     * @param method the method | 方法
     * @return MethodSignature instance | MethodSignature实例
     * @throws OpenReflectException if method is null | 如果方法为null
     */
    public static MethodSignature of(Method method) {
        Objects.requireNonNull(method, "Method must not be null");
        return new MethodSignature(method.getName(), method.getReturnType(), method.getParameterTypes());
    }

    /**
     * Creates a MethodSignature from name and parameter types (no return type)
     * 从名称和参数类型创建MethodSignature（不包含返回类型）
     *
     * @param name           the method name | 方法名称
     * @param parameterTypes the parameter types | 参数类型
     * @return MethodSignature instance | MethodSignature实例
     * @throws OpenReflectException if name is null or empty | 如果名称为null或空
     */
    public static MethodSignature of(String name, Class<?>... parameterTypes) {
        return new MethodSignature(name, null, parameterTypes != null ? parameterTypes : new Class<?>[0]);
    }

    /**
     * Creates a MethodSignature from name, return type, and parameter types
     * 从名称、返回类型和参数类型创建MethodSignature
     *
     * @param name           the method name | 方法名称
     * @param returnType     the return type | 返回类型
     * @param parameterTypes the parameter types | 参数类型
     * @return MethodSignature instance | MethodSignature实例
     * @throws OpenReflectException if name is null or empty | 如果名称为null或空
     */
    public static MethodSignature withReturnType(String name, Class<?> returnType, Class<?>... parameterTypes) {
        return new MethodSignature(name, returnType, parameterTypes == null ? new Class<?>[0] : parameterTypes);
    }

    // ==================== Accessors | 访问方法 ====================

    /**
     * Gets the method name
     * 获取方法名称
     *
     * @return the method name | 方法名称
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the parameter types (defensive copy)
     * 获取参数类型（防御性复制）
     *
     * @return the parameter types | 参数类型
     */
    public Class<?>[] getParameterTypes() {
        return parameterTypes.clone();
    }

    /**
     * Gets the return type, may be null if not specified
     * 获取返回类型，如果未指定可以为null
     *
     * @return the return type or null | 返回类型或null
     */
    public Class<?> getReturnType() {
        return returnType;
    }

    /**
     * Gets the number of parameters
     * 获取参数数量
     *
     * @return the parameter count | 参数数量
     */
    public int getParameterCount() {
        return parameterTypes.length;
    }

    // ==================== Matching | 匹配 ====================

    /**
     * Checks if this signature matches the given method (name + parameter types)
     * 检查此签名是否匹配给定方法（名称 + 参数类型）
     *
     * @param method the method to match against | 要匹配的方法
     * @return true if name and parameter types match | 如果名称和参数类型匹配返回true
     */
    public boolean matches(Method method) {
        if (method == null) {
            return false;
        }
        return name.equals(method.getName())
                && Arrays.equals(parameterTypes, method.getParameterTypes());
    }

    /**
     * Checks if this signature matches the given signature (name + parameter types)
     * 检查此签名是否匹配给定签名（名称 + 参数类型）
     *
     * @param other the other signature to match against | 要匹配的其他签名
     * @return true if name and parameter types match | 如果名称和参数类型匹配返回true
     */
    public boolean matches(MethodSignature other) {
        if (other == null) {
            return false;
        }
        return name.equals(other.name)
                && Arrays.equals(parameterTypes, other.parameterTypes);
    }

    // ==================== Override Detection | 覆盖检测 ====================

    /**
     * Checks if a method with this signature can override the given super method.
     * Requires name and parameter types to match, and the return type to be covariant
     * (this return type must be assignable to the super method's return type).
     * 检查具有此签名的方法是否可以覆盖给定的父方法。
     * 要求名称和参数类型匹配，且返回类型协变（此返回类型必须可赋值给父方法的返回类型）。
     *
     * @param superMethod the super method to check against | 要检查的父方法
     * @return true if this can override the super method | 如果可以覆盖返回true
     */
    public boolean isOverrideOf(Method superMethod) {
        if (superMethod == null) {
            return false;
        }
        if (!name.equals(superMethod.getName())) {
            return false;
        }
        if (!Arrays.equals(parameterTypes, superMethod.getParameterTypes())) {
            return false;
        }
        // If this signature has no return type specified, only check name + params
        if (returnType == null) {
            return true;
        }
        // Covariant return: this return type must be a subtype of super's return type
        return superMethod.getReturnType().isAssignableFrom(returnType);
    }

    /**
     * Checks if this signature is override-compatible with the given super signature.
     * Requires name and parameter types to match, and if both have return types,
     * the return type must be covariant.
     * 检查此签名是否与给定的父签名覆盖兼容。
     * 要求名称和参数类型匹配，如果两者都有返回类型，则返回类型必须协变。
     *
     * @param superSignature the super signature to check against | 要检查的父签名
     * @return true if override-compatible | 如果覆盖兼容返回true
     */
    public boolean isOverrideCompatible(MethodSignature superSignature) {
        if (superSignature == null) {
            return false;
        }
        if (!name.equals(superSignature.name)) {
            return false;
        }
        if (!Arrays.equals(parameterTypes, superSignature.parameterTypes)) {
            return false;
        }
        // If either has no return type, only check name + params
        if (returnType == null || superSignature.returnType == null) {
            return true;
        }
        // Covariant return: this return type must be a subtype of super's return type
        return superSignature.returnType.isAssignableFrom(returnType);
    }

    // ==================== Descriptor | 描述符 ====================

    /**
     * Generates the JVM method descriptor string.
     * Uses void as return type if not specified.
     * 生成JVM方法描述符字符串。
     * 如果未指定返回类型则使用void。
     *
     * <p>Examples | 示例:</p>
     * <ul>
     *   <li>{@code "(Ljava/lang/String;I)V"} for {@code foo(String, int): void}</li>
     *   <li>{@code "()Ljava/lang/String;"} for {@code bar(): String}</li>
     * </ul>
     *
     * @return the JVM method descriptor | JVM方法描述符
     */
    public String toDescriptor() {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        for (Class<?> paramType : parameterTypes) {
            appendTypeDescriptor(sb, paramType);
        }
        sb.append(')');
        appendTypeDescriptor(sb, returnType == null ? void.class : returnType);
        return sb.toString();
    }

    /**
     * Generates a human-readable string representation of the method signature.
     * 生成方法签名的人类可读字符串表示。
     *
     * <p>Format: {@code name(Type1, Type2): ReturnType}</p>
     *
     * @return the readable string | 可读字符串
     */
    public String toReadableString() {
        StringJoiner paramJoiner = new StringJoiner(", ");
        for (Class<?> paramType : parameterTypes) {
            paramJoiner.add(paramType.getSimpleName());
        }
        String returnName = returnType == null ? "void" : returnType.getSimpleName();
        return name + "(" + paramJoiner + "): " + returnName;
    }

    // ==================== Object Methods | Object方法 ====================

    /**
     * Checks equality based on name and parameter types (NOT return type, per Java overloading rules)
     * 基于名称和参数类型检查相等性（不包含返回类型，符合Java重载规则）
     *
     * @param o the other object | 其他对象
     * @return true if equal | 如果相等返回true
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MethodSignature other)) {
            return false;
        }
        return name.equals(other.name) && Arrays.equals(parameterTypes, other.parameterTypes);
    }

    /**
     * Hash code based on name and parameter types (NOT return type)
     * 基于名称和参数类型的哈希码（不包含返回类型）
     *
     * @return the hash code | 哈希码
     */
    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + Arrays.hashCode(parameterTypes);
        return result;
    }

    /**
     * Returns the readable string representation
     * 返回可读字符串表示
     *
     * @return string representation | 字符串表示
     */
    @Override
    public String toString() {
        return toReadableString();
    }

    // ==================== Internal | 内部方法 ====================

    /**
     * Appends the JVM type descriptor for the given class to the StringBuilder.
     * 将给定类的JVM类型描述符追加到StringBuilder。
     *
     * @param sb   the StringBuilder | StringBuilder
     * @param type the type | 类型
     */
    private static void appendTypeDescriptor(StringBuilder sb, Class<?> type) {
        if (type == void.class) {
            sb.append('V');
        } else if (type == boolean.class) {
            sb.append('Z');
        } else if (type == byte.class) {
            sb.append('B');
        } else if (type == char.class) {
            sb.append('C');
        } else if (type == short.class) {
            sb.append('S');
        } else if (type == int.class) {
            sb.append('I');
        } else if (type == long.class) {
            sb.append('J');
        } else if (type == float.class) {
            sb.append('F');
        } else if (type == double.class) {
            sb.append('D');
        } else if (type.isArray()) {
            sb.append('[');
            appendTypeDescriptor(sb, type.getComponentType());
        } else {
            sb.append('L');
            sb.append(type.getName().replace('.', '/'));
            sb.append(';');
        }
    }
}
