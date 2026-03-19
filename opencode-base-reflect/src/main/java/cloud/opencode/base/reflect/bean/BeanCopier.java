package cloud.opencode.base.reflect.bean;

import cloud.opencode.base.reflect.OpenConstructor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Bean Copier
 * Bean复制器
 *
 * <p>High-performance bean property copier.</p>
 * <p>高性能bean属性复制器。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Property mapping and renaming - 属性映射和重命名</li>
 *   <li>Custom converters - 自定义转换器</li>
 *   <li>Property exclusion and null filtering - 属性排除和null过滤</li>
 *   <li>Batch list copying - 批量列表复制</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * BeanCopier<UserDTO, User> copier = BeanCopier.builder(UserDTO.class, User.class)
 *     .map("userName", "name")
 *     .exclude("password")
 *     .ignoreNulls(true)
 *     .build();
 * User user = copier.copy(dto);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: Configurable (ignoreNulls option) - 空值安全: 可配置（ignoreNulls选项）</li>
 * </ul>
 *
 * @param <S> the source type | 源类型
 * @param <T> the target type | 目标类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public class BeanCopier<S, T> {

    private final Class<S> sourceClass;
    private final Class<T> targetClass;
    private final Map<String, PropertyDescriptor> sourceDescriptors;
    private final Map<String, PropertyDescriptor> targetDescriptors;
    private final Map<String, String> propertyMappings;
    private final Map<String, Function<Object, Object>> converters;
    private final Set<String> excludedProperties;
    private final BiPredicate<String, Object> copyCondition;
    private final boolean ignoreNulls;

    private BeanCopier(Builder<S, T> builder) {
        this.sourceClass = builder.sourceClass;
        this.targetClass = builder.targetClass;
        this.sourceDescriptors = OpenBean.getPropertyDescriptors(sourceClass);
        this.targetDescriptors = OpenBean.getPropertyDescriptors(targetClass);
        this.propertyMappings = builder.propertyMappings;
        this.converters = builder.converters;
        this.excludedProperties = builder.excludedProperties;
        this.copyCondition = builder.copyCondition;
        this.ignoreNulls = builder.ignoreNulls;
    }

    /**
     * Creates a BeanCopier builder
     * 创建BeanCopier构建器
     *
     * @param sourceClass the source class | 源类
     * @param targetClass the target class | 目标类
     * @param <S>         the source type | 源类型
     * @param <T>         the target type | 目标类型
     * @return the builder | 构建器
     */
    public static <S, T> Builder<S, T> builder(Class<S> sourceClass, Class<T> targetClass) {
        return new Builder<>(sourceClass, targetClass);
    }

    /**
     * Creates a simple BeanCopier
     * 创建简单BeanCopier
     *
     * @param sourceClass the source class | 源类
     * @param targetClass the target class | 目标类
     * @param <S>         the source type | 源类型
     * @param <T>         the target type | 目标类型
     * @return the copier | 复制器
     */
    public static <S, T> BeanCopier<S, T> create(Class<S> sourceClass, Class<T> targetClass) {
        return builder(sourceClass, targetClass).build();
    }

    /**
     * Copies properties from source to target
     * 从源复制属性到目标
     *
     * @param source the source object | 源对象
     * @param target the target object | 目标对象
     */
    public void copy(S source, T target) {
        for (Map.Entry<String, PropertyDescriptor> entry : sourceDescriptors.entrySet()) {
            String sourceName = entry.getKey();
            PropertyDescriptor sourceDesc = entry.getValue();

            if (!sourceDesc.isReadable() || excludedProperties.contains(sourceName)) {
                continue;
            }

            String targetName = propertyMappings.getOrDefault(sourceName, sourceName);
            PropertyDescriptor targetDesc = targetDescriptors.get(targetName);

            if (targetDesc == null || !targetDesc.isWritable()) {
                continue;
            }

            Object value = sourceDesc.getValue(source);

            if (ignoreNulls && value == null) {
                continue;
            }

            if (copyCondition != null && !copyCondition.test(sourceName, value)) {
                continue;
            }

            // Apply converter if exists
            Function<Object, Object> converter = converters.get(sourceName);
            if (converter != null) {
                value = converter.apply(value);
            }

            // Type conversion if needed
            value = convertIfNeeded(value, targetDesc.getPropertyType());

            targetDesc.setValue(target, value);
        }
    }

    /**
     * Copies and creates a new target instance
     * 复制并创建新的目标实例
     *
     * @param source the source object | 源对象
     * @return the new target instance | 新的目标实例
     */
    public T copy(S source) {
        T target = OpenConstructor.newInstance(targetClass);
        copy(source, target);
        return target;
    }

    /**
     * Copies a list of objects
     * 复制对象列表
     *
     * @param sources the source list | 源列表
     * @return the target list | 目标列表
     */
    public List<T> copyList(List<S> sources) {
        List<T> result = new ArrayList<>(sources.size());
        for (S source : sources) {
            result.add(copy(source));
        }
        return result;
    }

    /**
     * Gets the source class
     * 获取源类
     *
     * @return the source class | 源类
     */
    public Class<S> getSourceClass() {
        return sourceClass;
    }

    /**
     * Gets the target class
     * 获取目标类
     *
     * @return the target class | 目标类
     */
    public Class<T> getTargetClass() {
        return targetClass;
    }

    /**
     * Cached type converters for Number source values, keyed by target type
     * 缓存的 Number 源值类型转换器，按目标类型索引
     */
    private static final Map<Class<?>, Function<Number, Object>> NUMBER_CONVERTERS = new ConcurrentHashMap<>();

    /**
     * Cached type converters for String source values, keyed by target type
     * 缓存的 String 源值类型转换器，按目标类型索引
     */
    private static final Map<Class<?>, Function<String, Object>> STRING_CONVERTERS = new ConcurrentHashMap<>();

    static {
        // Number converters
        NUMBER_CONVERTERS.put(Integer.class, Number::intValue);
        NUMBER_CONVERTERS.put(int.class, Number::intValue);
        NUMBER_CONVERTERS.put(Long.class, Number::longValue);
        NUMBER_CONVERTERS.put(long.class, Number::longValue);
        NUMBER_CONVERTERS.put(Double.class, Number::doubleValue);
        NUMBER_CONVERTERS.put(double.class, Number::doubleValue);
        NUMBER_CONVERTERS.put(Float.class, Number::floatValue);
        NUMBER_CONVERTERS.put(float.class, Number::floatValue);
        NUMBER_CONVERTERS.put(Short.class, Number::shortValue);
        NUMBER_CONVERTERS.put(short.class, Number::shortValue);
        NUMBER_CONVERTERS.put(Byte.class, Number::byteValue);
        NUMBER_CONVERTERS.put(byte.class, Number::byteValue);

        // String converters
        STRING_CONVERTERS.put(Integer.class, Integer::parseInt);
        STRING_CONVERTERS.put(int.class, Integer::parseInt);
        STRING_CONVERTERS.put(Long.class, Long::parseLong);
        STRING_CONVERTERS.put(long.class, Long::parseLong);
        STRING_CONVERTERS.put(Double.class, Double::parseDouble);
        STRING_CONVERTERS.put(double.class, Double::parseDouble);
        STRING_CONVERTERS.put(Boolean.class, Boolean::parseBoolean);
        STRING_CONVERTERS.put(boolean.class, Boolean::parseBoolean);
    }

    private Object convertIfNeeded(Object value, Class<?> targetType) {
        if (value == null || targetType.isInstance(value)) {
            return value;
        }

        // String target: always use String.valueOf
        if (targetType == String.class) {
            return String.valueOf(value);
        }

        // Number source: lookup cached converter
        if (value instanceof Number number) {
            Function<Number, Object> converter = NUMBER_CONVERTERS.get(targetType);
            if (converter != null) {
                return converter.apply(number);
            }
        }

        // String source: lookup cached converter
        if (value instanceof String str) {
            Function<String, Object> converter = STRING_CONVERTERS.get(targetType);
            if (converter != null) {
                return converter.apply(str);
            }
        }

        return value;
    }

    /**
     * Builder for BeanCopier
     * BeanCopier构建器
     *
     * @param <S> the source type | 源类型
     * @param <T> the target type | 目标类型
     */
    public static class Builder<S, T> {
        private final Class<S> sourceClass;
        private final Class<T> targetClass;
        private final Map<String, String> propertyMappings = new LinkedHashMap<>();
        private final Map<String, Function<Object, Object>> converters = new LinkedHashMap<>();
        private final Set<String> excludedProperties = new LinkedHashSet<>();
        private BiPredicate<String, Object> copyCondition;
        private boolean ignoreNulls = false;

        private Builder(Class<S> sourceClass, Class<T> targetClass) {
            this.sourceClass = sourceClass;
            this.targetClass = targetClass;
        }

        /**
         * Maps source property to target property
         * 映射源属性到目标属性
         *
         * @param sourceProperty the source property name | 源属性名
         * @param targetProperty the target property name | 目标属性名
         * @return this builder | 此构建器
         */
        public Builder<S, T> map(String sourceProperty, String targetProperty) {
            propertyMappings.put(sourceProperty, targetProperty);
            return this;
        }

        /**
         * Adds a converter for a property
         * 为属性添加转换器
         *
         * @param sourceProperty the source property name | 源属性名
         * @param converter      the converter | 转换器
         * @return this builder | 此构建器
         */
        public Builder<S, T> convert(String sourceProperty, Function<Object, Object> converter) {
            converters.put(sourceProperty, converter);
            return this;
        }

        /**
         * Excludes properties from copying
         * 从复制中排除属性
         *
         * @param properties the property names | 属性名
         * @return this builder | 此构建器
         */
        public Builder<S, T> exclude(String... properties) {
            Collections.addAll(excludedProperties, properties);
            return this;
        }

        /**
         * Sets whether to ignore null values
         * 设置是否忽略null值
         *
         * @param ignoreNulls whether to ignore | 是否忽略
         * @return this builder | 此构建器
         */
        public Builder<S, T> ignoreNulls(boolean ignoreNulls) {
            this.ignoreNulls = ignoreNulls;
            return this;
        }

        /**
         * Sets a condition for copying
         * 设置复制条件
         *
         * @param condition the condition (property name, value) -&gt; boolean | 条件
         * @return this builder | 此构建器
         */
        public Builder<S, T> when(BiPredicate<String, Object> condition) {
            this.copyCondition = condition;
            return this;
        }

        /**
         * Builds the BeanCopier
         * 构建BeanCopier
         *
         * @return the BeanCopier | BeanCopier
         */
        public BeanCopier<S, T> build() {
            return new BeanCopier<>(this);
        }
    }
}
