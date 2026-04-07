package cloud.opencode.base.neural.op;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Operator Attribute Container
 * 算子属性容器
 *
 * <p>Type-safe attribute container for neural network operators.
 * Stores key-value pairs for operator configuration (kernel size, stride, etc.).</p>
 * <p>神经网络算子的类型安全属性容器。
 * 存储算子配置的键值对（卷积核大小、步幅等）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Type-safe attribute access with defaults - 带默认值的类型安全属性访问</li>
 *   <li>Immutable after build - 构建后不可变</li>
 *   <li>Builder pattern for construction - Builder 模式构造</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * OpAttribute attrs = OpAttribute.builder()
 *     .put("kernel_size", 3)
 *     .put("stride", 1)
 *     .put("padding", 0)
 *     .build();
 * int kernelSize = attrs.getInt("kernel_size", 1);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public final class OpAttribute {

    private static final OpAttribute EMPTY = new OpAttribute(Map.of());

    private final Map<String, Object> attrs;

    private OpAttribute(Map<String, Object> attrs) {
        this.attrs = Collections.unmodifiableMap(attrs);
    }

    /**
     * Get an empty attribute container
     * 获取空属性容器
     *
     * @return empty attributes | 空属性容器
     */
    public static OpAttribute empty() {
        return EMPTY;
    }

    /**
     * Get integer attribute
     * 获取整数属性
     *
     * @param key          attribute key | 属性键
     * @param defaultValue default if not found | 未找到时的默认值
     * @return the integer value | 整数值
     */
    public int getInt(String key, int defaultValue) {
        Object v = attrs.get(key);
        if (v instanceof Number n) {
            return n.intValue();
        }
        return defaultValue;
    }

    /**
     * Get float attribute
     * 获取浮点属性
     *
     * @param key          attribute key | 属性键
     * @param defaultValue default if not found | 未找到时的默认值
     * @return the float value | 浮点值
     */
    public float getFloat(String key, float defaultValue) {
        Object v = attrs.get(key);
        if (v instanceof Number n) {
            return n.floatValue();
        }
        return defaultValue;
    }

    /**
     * Get integer array attribute
     * 获取整数数组属性
     *
     * @param key attribute key | 属性键
     * @return the int array, or empty array if not found | 整数数组，未找到时返回空数组
     */
    public int[] getIntArray(String key) {
        Object v = attrs.get(key);
        if (v instanceof int[] arr) {
            return arr.clone();
        }
        return new int[0];
    }

    /**
     * Get string attribute
     * 获取字符串属性
     *
     * @param key          attribute key | 属性键
     * @param defaultValue default if not found | 未找到时的默认值
     * @return the string value | 字符串值
     */
    public String getString(String key, String defaultValue) {
        Object v = attrs.get(key);
        if (v instanceof String s) {
            return s;
        }
        return defaultValue;
    }

    /**
     * Get boolean attribute
     * 获取布尔属性
     *
     * @param key          attribute key | 属性键
     * @param defaultValue default if not found | 未找到时的默认值
     * @return the boolean value | 布尔值
     */
    public boolean getBool(String key, boolean defaultValue) {
        Object v = attrs.get(key);
        if (v instanceof Boolean b) {
            return b;
        }
        return defaultValue;
    }

    /**
     * Check if attribute exists
     * 检查属性是否存在
     *
     * @param key attribute key | 属性键
     * @return true if exists | 存在返回 true
     */
    public boolean has(String key) {
        return attrs.containsKey(key);
    }

    /**
     * Create a builder
     * 创建构建器
     *
     * @return new builder | 新构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for OpAttribute
     * OpAttribute 构建器
     */
    public static final class Builder {
        private final Map<String, Object> attrs = new HashMap<>();

        private Builder() {
        }

        /**
         * Put an attribute
         * 设置属性
         *
         * @param key   attribute key | 属性键
         * @param value attribute value | 属性值
         * @return this builder | 此构建器
         */
        public Builder put(String key, Object value) {
            Objects.requireNonNull(key, "key must not be null");
            Objects.requireNonNull(value, "value must not be null");
            attrs.put(key, value);
            return this;
        }

        /**
         * Build the OpAttribute
         * 构建 OpAttribute
         *
         * @return the built OpAttribute | 构建的 OpAttribute
         */
        public OpAttribute build() {
            if (attrs.isEmpty()) {
                return EMPTY;
            }
            return new OpAttribute(new HashMap<>(attrs));
        }
    }

    @Override
    public String toString() {
        return "OpAttribute" + attrs;
    }
}
