package cloud.opencode.base.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * MoreObjects - Extended Object utilities, including toString helper
 * 扩展对象工具类 - 包含 toString 辅助工具
 *
 * <p>This class provides additional utilities for working with objects, most notably
 * a fluent API for building toString() implementations.</p>
 * <p>该类提供了处理对象的额外工具，最重要的是用于构建 toString() 实现的流式 API。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>ToStringHelper for building readable toString() output - ToStringHelper 用于构建可读的 toString() 输出</li>
 *   <li>Null-safe property inclusion - 空值安全的属性包含</li>
 *   <li>Omit null values option - 可选忽略 null 值</li>
 *   <li>Array-aware formatting - 数组感知格式化</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Simple usage | 简单用法
 * public String toString() {
 *     return MoreObjects.toStringHelper(this)
 *         .add("name", name)
 *         .add("age", age)
 *         .toString();
 * }
 * // Output: User{name=John, age=30}
 *
 * // With omitNullValues | 忽略空值
 * return MoreObjects.toStringHelper("Person")
 *     .omitNullValues()
 *     .add("name", name)
 *     .add("nickname", null)  // will be omitted
 *     .add("age", age)
 *     .toString();
 * // Output: Person{name=John, age=30}
 *
 * // Using addValue for unnamed values | 使用 addValue 添加无名值
 * return MoreObjects.toStringHelper("Point")
 *     .addValue(x)
 *     .addValue(y)
 *     .toString();
 * // Output: Point{10, 20}
 * }</pre>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <p>ToStringHelper is NOT thread-safe. It should be used locally within a method.</p>
 * <p>ToStringHelper 非线程安全，应在方法内局部使用。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class MoreObjects {

    private MoreObjects() {
        // Utility class, not instantiable
    }

    // ==================== First Non-Null | 首个非空 ====================

    /**
     * Returns the first of two given parameters that is not null.
     * If both are null, returns null.
     * 返回两个参数中第一个非空的。如果都为空，返回 null。
     *
     * @param first  the first reference
     * @param second the second reference
     * @param <T>    the type of the references
     * @return first if it is non-null; otherwise second
     */
    public static <T> T firstNonNull(T first, T second) {
        return first != null ? first : second;
    }

    // ==================== ToStringHelper Factory | ToStringHelper 工厂 ====================

    /**
     * Creates a ToStringHelper for the given instance.
     * The class name will be used as the prefix.
     * 为给定实例创建 ToStringHelper。类名将用作前缀。
     *
     * @param self the object instance (typically "this")
     * @return a new ToStringHelper instance
     */
    public static ToStringHelper toStringHelper(Object self) {
        return new ToStringHelper(self.getClass().getSimpleName());
    }

    /**
     * Creates a ToStringHelper with the given class name.
     * 使用给定类创建 ToStringHelper。
     *
     * @param clazz the class whose simple name will be used
     * @return a new ToStringHelper instance
     */
    public static ToStringHelper toStringHelper(Class<?> clazz) {
        return new ToStringHelper(clazz.getSimpleName());
    }

    /**
     * Creates a ToStringHelper with the given string as the class name.
     * 使用给定字符串作为类名创建 ToStringHelper。
     *
     * @param className the class name to use in the output
     * @return a new ToStringHelper instance
     */
    public static ToStringHelper toStringHelper(String className) {
        return new ToStringHelper(className);
    }

    // ==================== ToStringHelper Inner Class | ToStringHelper 内部类 ====================

    /**
     * A helper class for building toString() implementations.
     * Provides a fluent API for adding named and unnamed values.
     * toString() 构建辅助类，提供流式 API 添加命名和未命名值。
     */
    public static final class ToStringHelper {

        private final String className;
        private final List<ValueHolder> values = new ArrayList<>();
        private boolean omitNullValues = false;

        private ToStringHelper(String className) {
            this.className = className;
        }

        /**
         * When called, null values will be omitted from the output.
         * 调用后，null 值将从输出中省略。
         *
         * @return this ToStringHelper for chaining
         */
        public ToStringHelper omitNullValues() {
            this.omitNullValues = true;
            return this;
        }

        // ==================== Add Named Values | 添加命名值 ====================

        /**
         * Adds a name-value pair.
         * 添加名称-值对。
         *
         * @param name  the property name
         * @param value the property value
         * @return this ToStringHelper for chaining
         */
        public ToStringHelper add(String name, Object value) {
            values.add(new ValueHolder(name, value));
            return this;
        }

        /**
         * Adds a name-value pair for a boolean value.
         * 添加布尔值的名称-值对。
         *
         * @param name  the property name
         * @param value the property value
         * @return this ToStringHelper for chaining
         */
        public ToStringHelper add(String name, boolean value) {
            return add(name, String.valueOf(value));
        }

        /**
         * Adds a name-value pair for a char value.
         * 添加字符值的名称-值对。
         *
         * @param name  the property name
         * @param value the property value
         * @return this ToStringHelper for chaining
         */
        public ToStringHelper add(String name, char value) {
            return add(name, String.valueOf(value));
        }

        /**
         * Adds a name-value pair for an int value.
         * 添加整数值的名称-值对。
         *
         * @param name  the property name
         * @param value the property value
         * @return this ToStringHelper for chaining
         */
        public ToStringHelper add(String name, int value) {
            return add(name, String.valueOf(value));
        }

        /**
         * Adds a name-value pair for a long value.
         * 添加长整数值的名称-值对。
         *
         * @param name  the property name
         * @param value the property value
         * @return this ToStringHelper for chaining
         */
        public ToStringHelper add(String name, long value) {
            return add(name, String.valueOf(value));
        }

        /**
         * Adds a name-value pair for a float value.
         * 添加浮点数值的名称-值对。
         *
         * @param name  the property name
         * @param value the property value
         * @return this ToStringHelper for chaining
         */
        public ToStringHelper add(String name, float value) {
            return add(name, String.valueOf(value));
        }

        /**
         * Adds a name-value pair for a double value.
         * 添加双精度值的名称-值对。
         *
         * @param name  the property name
         * @param value the property value
         * @return this ToStringHelper for chaining
         */
        public ToStringHelper add(String name, double value) {
            return add(name, String.valueOf(value));
        }

        // ==================== Add Unnamed Values | 添加未命名值 ====================

        /**
         * Adds an unnamed value.
         * 添加未命名值。
         *
         * @param value the value
         * @return this ToStringHelper for chaining
         */
        public ToStringHelper addValue(Object value) {
            values.add(new ValueHolder(null, value));
            return this;
        }

        /**
         * Adds an unnamed boolean value.
         * 添加未命名布尔值。
         *
         * @param value the value
         * @return this ToStringHelper for chaining
         */
        public ToStringHelper addValue(boolean value) {
            return addValue(String.valueOf(value));
        }

        /**
         * Adds an unnamed char value.
         * 添加未命名字符值。
         *
         * @param value the value
         * @return this ToStringHelper for chaining
         */
        public ToStringHelper addValue(char value) {
            return addValue(String.valueOf(value));
        }

        /**
         * Adds an unnamed int value.
         * 添加未命名整数值。
         *
         * @param value the value
         * @return this ToStringHelper for chaining
         */
        public ToStringHelper addValue(int value) {
            return addValue(String.valueOf(value));
        }

        /**
         * Adds an unnamed long value.
         * 添加未命名长整数值。
         *
         * @param value the value
         * @return this ToStringHelper for chaining
         */
        public ToStringHelper addValue(long value) {
            return addValue(String.valueOf(value));
        }

        /**
         * Adds an unnamed float value.
         * 添加未命名浮点数值。
         *
         * @param value the value
         * @return this ToStringHelper for chaining
         */
        public ToStringHelper addValue(float value) {
            return addValue(String.valueOf(value));
        }

        /**
         * Adds an unnamed double value.
         * 添加未命名双精度值。
         *
         * @param value the value
         * @return this ToStringHelper for chaining
         */
        public ToStringHelper addValue(double value) {
            return addValue(String.valueOf(value));
        }

        // ==================== Build String | 构建字符串 ====================

        /**
         * Returns the formatted string representation.
         * 返回格式化的字符串表示。
         *
         * @return the formatted string
         */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(32);
            sb.append(className).append('{');

            boolean first = true;
            for (ValueHolder holder : values) {
                // Skip null values if omitNullValues is enabled
                if (omitNullValues && holder.value == null) {
                    continue;
                }

                if (!first) {
                    sb.append(", ");
                }
                first = false;

                if (holder.name != null) {
                    sb.append(holder.name).append('=');
                }
                sb.append(formatValue(holder.value));
            }

            sb.append('}');
            return sb.toString();
        }

        /**
         * Formats a value for output, handling arrays specially.
         */
        private static String formatValue(Object value) {
            if (value == null) {
                return "null";
            }

            // Handle arrays
            if (value.getClass().isArray()) {
                if (value instanceof Object[] arr) {
                    return Arrays.deepToString(arr);
                }
                if (value instanceof int[] arr) {
                    return Arrays.toString(arr);
                }
                if (value instanceof long[] arr) {
                    return Arrays.toString(arr);
                }
                if (value instanceof double[] arr) {
                    return Arrays.toString(arr);
                }
                if (value instanceof float[] arr) {
                    return Arrays.toString(arr);
                }
                if (value instanceof boolean[] arr) {
                    return Arrays.toString(arr);
                }
                if (value instanceof byte[] arr) {
                    return Arrays.toString(arr);
                }
                if (value instanceof char[] arr) {
                    return Arrays.toString(arr);
                }
                if (value instanceof short[] arr) {
                    return Arrays.toString(arr);
                }
            }

            return value.toString();
        }

        /**
         * Internal holder for name-value pairs.
         */
        private record ValueHolder(String name, Object value) {
        }
    }
}
