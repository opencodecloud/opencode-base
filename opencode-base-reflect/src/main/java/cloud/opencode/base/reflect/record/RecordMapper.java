package cloud.opencode.base.reflect.record;

import cloud.opencode.base.reflect.OpenConstructor;
import cloud.opencode.base.reflect.bean.OpenBean;
import cloud.opencode.base.reflect.bean.PropertyDescriptor;
import cloud.opencode.base.reflect.exception.OpenReflectException;

import java.lang.reflect.RecordComponent;
import java.util.*;
import java.util.function.Function;

/**
 * Record Mapper for advanced Record/Bean mapping
 * Record映射器 - 高级Record/Bean映射
 *
 * <p>Provides advanced mapping between Records and Beans with support for
 * field renaming, exclusion, type conversion, and batch operations.</p>
 * <p>提供Record和Bean之间的高级映射，支持字段重命名、排除、类型转换和批量操作。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Record to Record mapping - Record到Record映射</li>
 *   <li>Record to Bean mapping - Record到Bean映射</li>
 *   <li>Bean to Record mapping - Bean到Record映射</li>
 *   <li>Bean to Bean mapping - Bean到Bean映射</li>
 *   <li>Field renaming via map() - 通过map()重命名字段</li>
 *   <li>Field exclusion via exclude() - 通过exclude()排除字段</li>
 *   <li>Custom type converters - 自定义类型转换器</li>
 *   <li>Null value handling - 空值处理</li>
 *   <li>Batch mapping - 批量映射</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Simple Record to Record mapping
 * RecordMapper<UserDTO, User> mapper = RecordMapper.builder(UserDTO.class, User.class)
 *     .map("userName", "name")
 *     .exclude("password")
 *     .convert("age", v -> Integer.parseInt(v.toString()))
 *     .ignoreNulls(true)
 *     .build();
 *
 * User user = mapper.map(dto);
 * List<User> users = mapper.mapAll(dtos);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: No (source must not be null) - 空值安全: 否（源不能为null）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) per mapping where n is the number of fields - 时间复杂度: 每次映射O(n)，n为字段数</li>
 *   <li>Space complexity: O(n) for the field mapping configuration - 空间复杂度: O(n)用于字段映射配置</li>
 * </ul>
 *
 * @param <S> the source type | 源类型
 * @param <T> the target type | 目标类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.3
 */
public final class RecordMapper<S, T> {

    private final Class<S> sourceType;
    private final Class<T> targetType;
    private final boolean sourceIsRecord;
    private final boolean targetIsRecord;
    private final Map<String, String> fieldMappings;
    private final Set<String> excludedFields;
    private final Map<String, Function<Object, Object>> converters;
    private final boolean ignoreNulls;

    private RecordMapper(Builder<S, T> builder) {
        this.sourceType = builder.sourceType;
        this.targetType = builder.targetType;
        this.sourceIsRecord = sourceType.isRecord();
        this.targetIsRecord = targetType.isRecord();
        this.fieldMappings = Map.copyOf(builder.fieldMappings);
        this.excludedFields = Set.copyOf(builder.excludedFields);
        this.converters = Map.copyOf(builder.converters);
        this.ignoreNulls = builder.ignoreNulls;
    }

    // ==================== Factory | 工厂方法 ====================

    /**
     * Creates a new RecordMapper builder
     * 创建新的RecordMapper构建器
     *
     * @param sourceType the source type | 源类型
     * @param targetType the target type | 目标类型
     * @param <S>        the source type | 源类型
     * @param <T>        the target type | 目标类型
     * @return the builder | 构建器
     * @throws NullPointerException if sourceType or targetType is null | 如果sourceType或targetType为null
     */
    public static <S, T> Builder<S, T> builder(Class<S> sourceType, Class<T> targetType) {
        Objects.requireNonNull(sourceType, "sourceType must not be null");
        Objects.requireNonNull(targetType, "targetType must not be null");
        return new Builder<>(sourceType, targetType);
    }

    // ==================== Mapping | 映射 ====================

    /**
     * Maps a source object to a target object
     * 将源对象映射为目标对象
     *
     * @param source the source object | 源对象
     * @return the mapped target object | 映射后的目标对象
     * @throws NullPointerException  if source is null | 如果源对象为null
     * @throws OpenReflectException  if mapping fails | 如果映射失败
     */
    public T map(S source) {
        Objects.requireNonNull(source, "source must not be null");

        // Extract source field values
        Map<String, Object> sourceValues = extractSourceValues(source);

        // Build target values with mapping, exclusion, conversion
        Map<String, Object> targetValues = buildTargetValues(sourceValues);

        // Construct target
        return constructTarget(targetValues);
    }

    /**
     * Maps a collection of source objects to a list of target objects
     * 将源对象集合映射为目标对象列表
     *
     * @param sources the source collection | 源集合
     * @return the mapped target list | 映射后的目标列表
     * @throws NullPointerException if sources is null | 如果源集合为null
     */
    public List<T> mapAll(Collection<? extends S> sources) {
        Objects.requireNonNull(sources, "sources must not be null");
        List<T> result = new ArrayList<>(sources.size());
        for (S source : sources) {
            result.add(map(source));
        }
        return result;
    }

    // ==================== Internal | 内部方法 ====================

    /**
     * Extracts field values from the source object
     * 从源对象提取字段值
     */
    private Map<String, Object> extractSourceValues(S source) {
        Map<String, Object> values = new LinkedHashMap<>();

        if (sourceIsRecord) {
            RecordComponent[] components = RecordUtil.getRecordComponents(sourceType);
            for (RecordComponent comp : components) {
                String name = comp.getName();
                try {
                    Object value = comp.getAccessor().invoke(source);
                    values.put(name, value);
                } catch (Exception e) {
                    throw new OpenReflectException(
                            "Failed to read record component '" + name + "' from " + sourceType.getName(), e);
                }
            }
        } else {
            Map<String, PropertyDescriptor> descriptors = OpenBean.getPropertyDescriptors(sourceType);
            for (Map.Entry<String, PropertyDescriptor> entry : descriptors.entrySet()) {
                PropertyDescriptor desc = entry.getValue();
                if (desc.isReadable()) {
                    Object value = desc.getValue(source);
                    values.put(entry.getKey(), value);
                }
            }
        }

        return values;
    }

    /**
     * Builds target values by applying mappings, exclusions, and converters
     * 通过应用映射、排除和转换器构建目标值
     */
    private Map<String, Object> buildTargetValues(Map<String, Object> sourceValues) {
        // Build reverse mapping: targetField -> sourceField
        Map<String, String> reverseMapping = new HashMap<>();
        for (Map.Entry<String, String> entry : fieldMappings.entrySet()) {
            reverseMapping.put(entry.getValue(), entry.getKey());
        }

        // Determine target field names
        Set<String> targetFieldNames = getTargetFieldNames();

        Map<String, Object> targetValues = new LinkedHashMap<>();
        for (String targetField : targetFieldNames) {
            // Find corresponding source field name
            String sourceField;
            if (reverseMapping.containsKey(targetField)) {
                sourceField = reverseMapping.get(targetField);
            } else {
                sourceField = targetField;
            }

            // Check exclusion (on both source and target field names)
            if (excludedFields.contains(sourceField) || excludedFields.contains(targetField)) {
                continue;
            }

            // Get value from source
            if (!sourceValues.containsKey(sourceField)) {
                // Source does not have this field; skip gracefully
                continue;
            }

            Object value = sourceValues.get(sourceField);

            // Apply ignoreNulls
            if (ignoreNulls && value == null) {
                continue;
            }

            // Apply converter (keyed on target field name)
            Function<Object, Object> converter = converters.get(targetField);
            if (converter != null) {
                try {
                    value = converter.apply(value);
                } catch (Exception e) {
                    throw new OpenReflectException(
                            "Converter failed for target field '" + targetField + "'", e);
                }
            }

            targetValues.put(targetField, value);
        }

        return targetValues;
    }

    /**
     * Gets the set of target field names
     * 获取目标字段名集合
     */
    private Set<String> getTargetFieldNames() {
        if (targetIsRecord) {
            RecordComponent[] components = RecordUtil.getRecordComponents(targetType);
            Set<String> names = new LinkedHashSet<>(components.length);
            for (RecordComponent comp : components) {
                names.add(comp.getName());
            }
            return names;
        } else {
            return OpenBean.getPropertyNames(targetType);
        }
    }

    /**
     * Constructs a target instance from the mapped values
     * 从映射值构造目标实例
     */
    @SuppressWarnings("unchecked")
    private T constructTarget(Map<String, Object> targetValues) {
        if (targetIsRecord) {
            return constructRecord(targetValues);
        } else {
            return constructBean(targetValues);
        }
    }

    /**
     * Constructs a Record target
     * 构造Record目标
     */
    @SuppressWarnings("unchecked")
    private T constructRecord(Map<String, Object> targetValues) {
        RecordComponent[] components = RecordUtil.getRecordComponents(targetType);
        Object[] args = new Object[components.length];
        for (int i = 0; i < components.length; i++) {
            String name = components[i].getName();
            if (targetValues.containsKey(name)) {
                args[i] = targetValues.get(name);
            } else {
                args[i] = defaultValue(components[i].getType());
            }
        }
        return castAndCreate(targetType, args);
    }

    /**
     * Helper to bridge generic type for RecordUtil.newInstance
     * 泛型类型桥接辅助方法
     */
    @SuppressWarnings("unchecked")
    private static <R extends Record, T> T castAndCreate(Class<T> targetType, Object[] args) {
        Class<R> recordClass = (Class<R>) targetType;
        return (T) RecordUtil.newInstance(recordClass, args);
    }

    /**
     * Constructs a Bean target
     * 构造Bean目标
     */
    private T constructBean(Map<String, Object> targetValues) {
        T target = OpenConstructor.newInstance(targetType);
        Map<String, PropertyDescriptor> descriptors = OpenBean.getPropertyDescriptors(targetType);
        for (Map.Entry<String, Object> entry : targetValues.entrySet()) {
            PropertyDescriptor desc = descriptors.get(entry.getKey());
            if (desc != null && desc.isWritable()) {
                desc.setValue(target, entry.getValue());
            }
        }
        return target;
    }

    /**
     * Returns the default value for a primitive type
     * 返回基本类型的默认值
     */
    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == boolean.class) return false;
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0.0f;
        if (type == double.class) return 0.0;
        if (type == char.class) return '\0';
        return null; // non-primitive type
    }

    // ==================== Builder | 构建器 ====================

    /**
     * Builder for RecordMapper
     * RecordMapper构建器
     *
     * <p>Configures field mappings, exclusions, converters, and null handling.</p>
     * <p>配置字段映射、排除、转换器和空值处理。</p>
     *
     * @param <S> the source type | 源类型
     * @param <T> the target type | 目标类型
     * @author Leon Soo
     * <a href="https://leonsoo.com">www.LeonSoo.com</a>
     * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
     * @since JDK 25, opencode-base-reflect V1.0.3
     */
    public static final class Builder<S, T> {

        private final Class<S> sourceType;
        private final Class<T> targetType;
        private final Map<String, String> fieldMappings = new LinkedHashMap<>();
        private final Set<String> excludedFields = new LinkedHashSet<>();
        private final Map<String, Function<Object, Object>> converters = new LinkedHashMap<>();
        private boolean ignoreNulls = false;

        private Builder(Class<S> sourceType, Class<T> targetType) {
            this.sourceType = sourceType;
            this.targetType = targetType;
        }

        /**
         * Maps a source field name to a target field name
         * 将源字段名映射到目标字段名
         *
         * @param sourceField the source field name | 源字段名
         * @param targetField the target field name | 目标字段名
         * @return this builder | 此构建器
         * @throws NullPointerException if sourceField or targetField is null | 如果字段名为null
         */
        public Builder<S, T> map(String sourceField, String targetField) {
            Objects.requireNonNull(sourceField, "sourceField must not be null");
            Objects.requireNonNull(targetField, "targetField must not be null");
            fieldMappings.put(sourceField, targetField);
            return this;
        }

        /**
         * Excludes a field from mapping
         * 从映射中排除字段
         *
         * @param fieldName the field name to exclude | 要排除的字段名
         * @return this builder | 此构建器
         * @throws NullPointerException if fieldName is null | 如果字段名为null
         */
        public Builder<S, T> exclude(String fieldName) {
            Objects.requireNonNull(fieldName, "fieldName must not be null");
            excludedFields.add(fieldName);
            return this;
        }

        /**
         * Adds a custom converter for a target field
         * 为目标字段添加自定义转换器
         *
         * @param targetField the target field name | 目标字段名
         * @param converter   the converter function | 转换器函数
         * @return this builder | 此构建器
         * @throws NullPointerException if targetField or converter is null | 如果参数为null
         */
        public Builder<S, T> convert(String targetField, Function<Object, Object> converter) {
            Objects.requireNonNull(targetField, "targetField must not be null");
            Objects.requireNonNull(converter, "converter must not be null");
            converters.put(targetField, converter);
            return this;
        }

        /**
         * Sets whether to ignore null values during mapping
         * 设置映射时是否忽略空值
         *
         * <p>When true, source fields with null values will not be mapped to the target.
         * For Record targets, unmapped fields will receive their type's default value.</p>
         * <p>当为true时，源字段中的null值不会映射到目标。
         * 对于Record目标，未映射的字段将使用其类型的默认值。</p>
         *
         * @param ignoreNulls whether to ignore nulls | 是否忽略空值
         * @return this builder | 此构建器
         */
        public Builder<S, T> ignoreNulls(boolean ignoreNulls) {
            this.ignoreNulls = ignoreNulls;
            return this;
        }

        /**
         * Builds the RecordMapper
         * 构建RecordMapper
         *
         * @return the RecordMapper | RecordMapper
         */
        public RecordMapper<S, T> build() {
            return new RecordMapper<>(this);
        }
    }
}
