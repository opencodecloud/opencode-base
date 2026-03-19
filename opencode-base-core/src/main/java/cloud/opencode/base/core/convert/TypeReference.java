package cloud.opencode.base.core.convert;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Type Reference - Captures generic type information at runtime
 * 类型引用 - 运行时捕获泛型类型信息
 *
 * <p>Uses anonymous class pattern to preserve full generic type information.</p>
 * <p>使用匿名类方式创建，保留完整的泛型类型信息。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Captures parameterized types - 捕获参数化类型</li>
 *   <li>Provides raw type access - 提供原始类型访问</li>
 *   <li>Enables type-safe generic conversions - 支持类型安全的泛型转换</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Capture List<String> type - 捕获 List<String> 类型
 * TypeReference<List<String>> ref = new TypeReference<List<String>>() {};
 * Type type = ref.getType();  // java.util.List<java.lang.String>
 * Class<?> raw = ref.getRawType();  // java.util.List
 *
 * // Use with conversion - 用于转换
 * List<String> result = Convert.convert(value, new TypeReference<List<String>>() {});
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after creation) - 线程安全: 是（创建后不可变）</li>
 *   <li>Null-safe: No, type must not be null - 空值安全: 否，类型不可为null</li>
 * </ul>
 *
 * @param <T> the type to capture - 要捕获的类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public abstract class TypeReference<T> {

    private final Type type;
    private final Class<T> rawType;

    @SuppressWarnings("unchecked")
    protected TypeReference() {
        Type superClass = getClass().getGenericSuperclass();
        if (!(superClass instanceof ParameterizedType)) {
            throw new IllegalArgumentException("TypeReference must be parameterized");
        }
        this.type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
        if (this.type instanceof ParameterizedType pt) {
            this.rawType = (Class<T>) pt.getRawType();
        } else if (this.type instanceof Class<?> c) {
            this.rawType = (Class<T>) c;
        } else {
            this.rawType = null;
        }
    }

    /**
     * Gets the full generic type
     * 获取完整的泛型类型
     */
    public Type getType() {
        return type;
    }

    /**
     * Gets the raw type (erased generic type)
     * 获取原始类型
     */
    public Class<T> getRawType() {
        return rawType;
    }

    @Override
    public String toString() {
        return type.getTypeName();
    }
}
