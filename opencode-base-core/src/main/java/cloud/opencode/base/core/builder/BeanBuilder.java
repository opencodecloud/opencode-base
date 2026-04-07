package cloud.opencode.base.core.builder;

import cloud.opencode.base.core.bean.OpenBean;
import cloud.opencode.base.core.bean.PropertyDescriptor;
import cloud.opencode.base.core.convert.Convert;
import cloud.opencode.base.core.exception.OpenIllegalArgumentException;
import cloud.opencode.base.core.reflect.ReflectUtil;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Bean Builder - Fluent builder for JavaBeans
 * JavaBean 构建器 - JavaBean 的流式构建器
 *
 * <p>Creates JavaBean instances with fluent API and automatic type conversion.</p>
 * <p>使用流式 API 和自动类型转换创建 JavaBean 实例。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Property setting (set, setIfNotNull, setIf) - 属性设置</li>
 *   <li>Batch setting (setAll) - 批量设置</li>
 *   <li>Copy from existing instance (from) - 从现有实例复制</li>
 *   <li>Automatic type conversion - 自动类型转换</li>
 *   <li>Build with validation (buildAndValidate) - 构建并验证</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * User user = BeanBuilder.of(User.class)
 *     .set("name", "John")
 *     .set("age", 25)
 *     .setIfNotNull("email", email)
 *     .build();
 *
 * User copy = BeanBuilder.from(existingUser)
 *     .set("name", "NewName")
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (builder instance not thread-safe) - 线程安全: 否</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) per property set, O(n) for build - 每次属性设置 O(1), 构建 O(n)</li>
 *   <li>Space complexity: O(n) where n = number of properties - O(n), n为属性数量</li>
 * </ul>
 *
 * @param <T> Bean type - Bean 类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public class BeanBuilder<T> implements Builder<T> {

    /**
     * Serializable Function - A function interface that supports serialization for method reference resolution
     * 可序列化函数 - 支持序列化的函数接口，用于方法引用解析
     *
     * <p>This interface combines {@link Function} and {@link Serializable} so that
     * getter method references can be resolved to property names via {@link SerializedLambda}.</p>
     * <p>此接口组合了 {@link Function} 和 {@link Serializable}，使得
     * getter 方法引用可以通过 {@link SerializedLambda} 解析为属性名。</p>
     *
     * @param <T> the input type | 输入类型
     * @param <R> the result type | 结果类型
     */
    @FunctionalInterface
    public interface SerializableFunction<T, R> extends Function<T, R>, Serializable {
    }

    private final Class<T> beanClass;
    private final Map<String, Object> properties = new LinkedHashMap<>();
    private T source;

    /**
     * Creates a BeanBuilder for the given class | 为指定类创建 BeanBuilder
     *
     * @param beanClass the target bean class | 目标 Bean 类
     */
    public BeanBuilder(Class<T> beanClass) {
        this.beanClass = beanClass;
    }

    /**
     * Creates a BeanBuilder for the given class | 为指定类创建 BeanBuilder
     *
     * @param <T> the bean type | Bean 类型
     * @param beanClass the target bean class | 目标 Bean 类
     * @return a new BeanBuilder | 新的 BeanBuilder
     */
    public static <T> BeanBuilder<T> of(Class<T> beanClass) {
        return new BeanBuilder<>(beanClass);
    }

    /**
     * Creates a builder from an existing instance | 从现有实例创建构建器
     *
     * @param <T> the bean type | Bean 类型
     * @param source the source instance | 源实例
     * @return a new BeanBuilder | 新的 BeanBuilder
     */
    public static <T> BeanBuilder<T> from(T source) {
        @SuppressWarnings("unchecked")
        Class<T> clazz = (Class<T>) source.getClass();
        BeanBuilder<T> builder = new BeanBuilder<>(clazz);
        builder.source = source;
        return builder;
    }

    /**
     * Sets a property value | 设置属性值
     *
     * @param propertyName the property name | 属性名
     * @param value the value | 值
     * @return this builder | 此构建器
     */
    public BeanBuilder<T> set(String propertyName, Object value) {
        properties.put(propertyName, value);
        return this;
    }

    /**
     * Sets a property value using a getter function reference (deprecated) |
     * 使用 getter 函数引用设置属性值（已废弃）
     *
     * <p>This method is retained for binary/source compatibility with 1.0.2.
     * Use {@link #set(SerializableFunction, Object)} instead, which provides
     * the same functionality with proper type-safe property name resolution.</p>
     * <p>此方法为兼容 1.0.2 版本而保留。
     * 请改用 {@link #set(SerializableFunction, Object)}，它提供相同的功能并支持类型安全的属性名解析。</p>
     *
     * <p>If the passed {@code getter} is actually a serializable method reference,
     * property name resolution works normally. Otherwise, an
     * {@link OpenIllegalArgumentException} is thrown with a clear migration hint.</p>
     * <p>如果传入的 {@code getter} 实际上是可序列化的方法引用，属性名解析正常工作。
     * 否则抛出 {@link OpenIllegalArgumentException} 并给出明确的迁移提示。</p>
     *
     * @param <V> the value type | 值类型
     * @param getter the getter function | getter 函数
     * @param value the value | 值
     * @return this builder | 此构建器
     * @throws OpenIllegalArgumentException if property name cannot be resolved
     *         如果无法解析属性名则抛出异常
     * @deprecated since 1.0.3, for removal. Use {@link #set(SerializableFunction, Object)} instead.
     *             自 1.0.3 起废弃，将被移除。请改用 {@link #set(SerializableFunction, Object)}。
     */
    @Deprecated(since = "1.0.3", forRemoval = true)
    public <V> BeanBuilder<T> set(Function<T, V> getter, V value) {
        // If the Function is actually a serializable method reference, resolve normally
        // 如果 Function 实际上是可序列化的方法引用，则正常解析
        if (getter instanceof Serializable) {
            try {
                Method writeReplace = getter.getClass().getDeclaredMethod("writeReplace");
                writeReplace.setAccessible(true);
                SerializedLambda lambda = (SerializedLambda) writeReplace.invoke(getter);
                String methodName = lambda.getImplMethodName();
                String propertyName;
                if (methodName.startsWith("get") && methodName.length() > 3) {
                    propertyName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
                } else if (methodName.startsWith("is") && methodName.length() > 2) {
                    propertyName = Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3);
                } else {
                    throw new OpenIllegalArgumentException(
                            "Method '" + methodName + "' does not follow getter naming convention (getXxx/isXxx) | "
                                    + "方法 '" + methodName + "' 不符合 getter 命名约定（getXxx/isXxx）");
                }
                properties.put(propertyName, value);
                return this;
            } catch (OpenIllegalArgumentException e) {
                throw e;
            } catch (Exception ignored) {
                // Fall through to error below
            }
        }
        throw new OpenIllegalArgumentException(
                "Cannot resolve property name from Function<T,V>. "
                        + "Please use set(SerializableFunction<T,V>, V) with a method reference (e.g. User::getName) instead. "
                        + "This overload is deprecated since 1.0.3. | "
                        + "无法从 Function<T,V> 解析属性名。"
                        + "请改用 set(SerializableFunction<T,V>, V) 并传入方法引用（如 User::getName）。"
                        + "此重载自 1.0.3 起已废弃。");
    }

    /**
     * Type-safe property setting (using getter method reference) | 类型安全设置属性（使用 getter 方法引用）
     *
     * <p>Usage example | 使用示例:</p>
     * <pre>{@code
     * BeanBuilder.of(User.class)
     *     .set(User::getName, "John")
     *     .set(User::getAge, 25)
     *     .build();
     * }</pre>
     *
     * @param <V> the value type | 值类型
     * @param getter the getter method reference (must be a method reference, not a lambda expression)
     *               getter 方法引用（必须是方法引用，不能是 lambda 表达式）
     * @param value the value | 值
     * @return this builder | 此构建器
     * @throws OpenIllegalArgumentException if the getter is not a valid method reference
     *         如果 getter 不是有效的方法引用则抛出异常
     */
    public <V> BeanBuilder<T> set(SerializableFunction<T, V> getter, V value) {
        String propertyName = resolvePropertyName(getter);
        properties.put(propertyName, value);
        return this;
    }

    /**
     * Conditionally sets a property (sets when non-null) | 条件设置属性（非 null 时设置）
     *
     * @param propertyName the property name | 属性名
     * @param value the value | 值
     * @return this builder | 此构建器
     */
    public BeanBuilder<T> setIfNotNull(String propertyName, Object value) {
        if (value != null) {
            properties.put(propertyName, value);
        }
        return this;
    }

    /**
     * Conditionally sets a property | 条件设置属性
     *
     * @param condition the condition | 条件
     * @param propertyName the property name | 属性名
     * @param value the value | 值
     * @return this builder | 此构建器
     */
    public BeanBuilder<T> setIf(boolean condition, String propertyName, Object value) {
        if (condition) {
            properties.put(propertyName, value);
        }
        return this;
    }

    /**
     * Sets multiple properties in batch | 批量设置属性
     *
     * @param props the property map | 属性映射
     * @return this builder | 此构建器
     */
    public BeanBuilder<T> setAll(Map<String, Object> props) {
        properties.putAll(props);
        return this;
    }

    /**
     * Configuration callback | 配置回调
     *
     * @param consumer the configuration consumer | 配置消费者
     * @return this builder | 此构建器
     */
    public BeanBuilder<T> configure(Consumer<BeanBuilder<T>> consumer) {
        consumer.accept(this);
        return this;
    }

    @Override
    public T build() {
        T bean;
        if (source != null) {
            bean = ReflectUtil.newInstance(beanClass);
            OpenBean.copyProperties(source, bean);
        } else {
            bean = ReflectUtil.newInstance(beanClass);
        }

        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            PropertyDescriptor pd = OpenBean.getPropertyDescriptor(beanClass, entry.getKey()).orElse(null);
            if (pd != null && pd.isWritable()) {
                Object value = Convert.convert(entry.getValue(), pd.type());
                pd.setValue(bean, value);
            }
        }

        return bean;
    }

    /**
     * Builds and validates the bean | 构建并验证 Bean
     *
     * @param validator the validation consumer | 验证消费者
     * @return the built bean | 构建的 Bean
     */
    public T buildAndValidate(Consumer<T> validator) {
        T bean = build();
        validator.accept(bean);
        return bean;
    }

    /**
     * Resolves property name from a getter method reference via SerializedLambda
     * 通过 SerializedLambda 从 getter 方法引用解析属性名
     *
     * <p>Extracts the property name by removing the "get"/"is" prefix from the
     * implementation method name and converting the first character to lowercase.</p>
     * <p>通过移除实现方法名的 "get"/"is" 前缀并将首字母转小写来提取属性名。</p>
     *
     * @param <V> the return type of the getter | getter 的返回类型
     * @param getter the serializable getter method reference | 可序列化的 getter 方法引用
     * @return the property name | 属性名
     * @throws OpenIllegalArgumentException if the method reference cannot be resolved or
     *         the method name does not follow getter naming conventions
     *         如果无法解析方法引用或方法名不符合 getter 命名约定则抛出异常
     */
    private <V> String resolvePropertyName(SerializableFunction<T, V> getter) {
        SerializedLambda lambda;
        try {
            Method writeReplace = getter.getClass().getDeclaredMethod("writeReplace");
            writeReplace.setAccessible(true);
            lambda = (SerializedLambda) writeReplace.invoke(getter);
        } catch (Exception e) {
            throw new OpenIllegalArgumentException(
                    "Failed to resolve property name from getter: the argument must be a method reference (e.g. User::getName), not a lambda expression | "
                            + "无法从 getter 解析属性名：参数必须是方法引用（如 User::getName），不能是 lambda 表达式", e);
        }

        String methodName = lambda.getImplMethodName();

        String propertyName;
        if (methodName.startsWith("get") && methodName.length() > 3) {
            propertyName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
        } else if (methodName.startsWith("is") && methodName.length() > 2) {
            propertyName = Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3);
        } else {
            throw new OpenIllegalArgumentException(
                    "Method '" + methodName + "' does not follow getter naming convention (getXxx/isXxx) | "
                            + "方法 '" + methodName + "' 不符合 getter 命名约定（getXxx/isXxx）");
        }

        return propertyName;
    }
}
