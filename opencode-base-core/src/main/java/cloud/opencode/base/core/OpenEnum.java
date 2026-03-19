package cloud.opencode.base.core;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Enum Utility Class - Validation, retrieval, mapping, filtering and conversion for enums
 * 枚举工具类 - 枚举的验证、获取、映射、过滤和转换
 *
 * <p>Provides comprehensive enum operations with internal caching optimization.</p>
 * <p>提供全面的枚举操作，包括验证、获取、映射、过滤和转换，支持内部缓存优化。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Get by name (getEnumByName, getEnumByNameSafely, getEnumByNameIgnoreCase) - 按名称获取</li>
 *   <li>Get by value (getEnumByValue with custom extractor) - 按自定义值获取</li>
 *   <li>Collection operations (getEnumList, getEnumSet, getEnumValueMap) - 集合操作</li>
 *   <li>Validation (isValidEnum, isValidEnumIgnoreCase) - 验证</li>
 *   <li>Utilities (ordinal, name, getByOrdinal) - 工具方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Get by name - 按名称获取
 * Status status = OpenEnum.getEnumByName(Status.class, "ACTIVE");
 * Status safe = OpenEnum.getEnumByNameSafely(Status.class, "UNKNOWN", Status.DEFAULT);
 *
 * // Case-insensitive - 忽略大小写
 * Status s = OpenEnum.getEnumByNameIgnoreCase(Status.class, "active");
 *
 * // Validation - 验证
 * boolean valid = OpenEnum.isValidEnum(Status.class, "ACTIVE");
 *
 * // Get by custom value - 按自定义值获取
 * Status byCode = OpenEnum.getEnumByValue(Status.class, 1, Status::getCode);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (ConcurrentHashMap cache) - 线程安全: 是 (并发哈希表缓存)</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class OpenEnum {

    private OpenEnum() {
    }

    // 枚举值缓存
    private static final Map<Class<?>, Map<String, Enum<?>>> ENUM_NAME_CACHE = new ConcurrentHashMap<>();

    // ==================== 获取枚举值 ====================

    /**
     * Gets an enum value by name
     * 按名称获取枚举值
     *
     * @param enumClass the enum class | 枚举类
     * @param name      the enum name | 枚举名称
     * @param <E>       the enum type | 枚举类型
     * @return the enum value | 枚举值
     * @throws IllegalArgumentException if the name is invalid | 如果名称无效
     */
    public static <E extends Enum<E>> E getEnumByName(Class<E> enumClass, String name) {
        return Enum.valueOf(enumClass, name);
    }

    /**
     * Safely gets an enum value with a default
     * 安全获取枚举值（带默认值）
     *
     * @param enumClass    the enum class | 枚举类
     * @param name         the enum name | 枚举名称
     * @param defaultValue the default value | 默认值
     * @param <E>          the enum type | 枚举类型
     * @return the enum value or default | 枚举值或默认值
     */
    public static <E extends Enum<E>> E getEnumByNameSafely(Class<E> enumClass, String name, E defaultValue) {
        if (name == null || enumClass == null) {
            return defaultValue;
        }
        try {
            return Enum.valueOf(enumClass, name);
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }

    /**
     * Gets an Optional-wrapped enum value
     * 获取 Optional 包装的枚举值
     *
     * @param enumClass the enum class | 枚举类
     * @param name      the enum name | 枚举名称
     * @param <E>       the enum type | 枚举类型
     * @return the Optional-wrapped enum value | Optional 包装的枚举值
     */
    public static <E extends Enum<E>> Optional<E> getEnumByNameOptional(Class<E> enumClass, String name) {
        if (name == null || enumClass == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(Enum.valueOf(enumClass, name));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * Gets an enum value by name ignoring case
     * 忽略大小写获取枚举值
     *
     * @param enumClass the enum class | 枚举类
     * @param name      the enum name | 枚举名称
     * @param <E>       the enum type | 枚举类型
     * @return the enum value | 枚举值
     */
    @SuppressWarnings("unchecked")
    public static <E extends Enum<E>> E getEnumByNameIgnoreCase(Class<E> enumClass, String name) {
        if (name == null || enumClass == null) {
            return null;
        }
        Map<String, Enum<?>> cache = ENUM_NAME_CACHE.computeIfAbsent(enumClass, k -> {
            Map<String, Enum<?>> map = new HashMap<>();
            for (E e : enumClass.getEnumConstants()) {
                map.put(e.name().toLowerCase(), e);
            }
            return map;
        });
        return (E) cache.get(name.toLowerCase());
    }

    /**
     * Gets an enum by a custom value
     * 按自定义值获取枚举
     *
     * @param enumClass     the enum class | 枚举类
     * @param value         the target value | 目标值
     * @param valueExtractor the value extractor | 值提取器
     * @param <E>           the enum type | 枚举类型
     * @param <V>           the value type | 值类型
     * @return the enum value | 枚举值
     */
    public static <E extends Enum<E>, V> E getEnumByValue(Class<E> enumClass, V value,
                                                          Function<E, V> valueExtractor) {
        if (enumClass == null || valueExtractor == null) {
            return null;
        }
        for (E e : enumClass.getEnumConstants()) {
            V extractedValue = valueExtractor.apply(e);
            if (Objects.equals(extractedValue, value)) {
                return e;
            }
        }
        return null;
    }

    /**
     * Gets an enum by a custom value with a default
     * 按自定义值获取枚举（带默认值）
     */
    public static <E extends Enum<E>, V> E getEnumByValue(Class<E> enumClass, V value,
                                                          Function<E, V> valueExtractor, E defaultValue) {
        E result = getEnumByValue(enumClass, value, valueExtractor);
        return result != null ? result : defaultValue;
    }

    // ==================== 获取枚举集合 ====================

    /**
     * Gets a list of all enum values
     * 获取所有枚举值列表
     *
     * @param enumClass the enum class | 枚举类
     * @param <E>       the enum type | 枚举类型
     * @return the list of enum values | 枚举值列表
     */
    public static <E extends Enum<E>> List<E> getEnumList(Class<E> enumClass) {
        if (enumClass == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(enumClass.getEnumConstants());
    }

    /**
     * Gets an EnumSet of all enum values
     * 获取所有枚举值集合
     *
     * @param enumClass the enum class | 枚举类
     * @param <E>       the enum type | 枚举类型
     * @return the EnumSet | 枚举值 EnumSet
     */
    public static <E extends Enum<E>> EnumSet<E> getEnumSet(Class<E> enumClass) {
        if (enumClass == null) {
            throw new IllegalArgumentException("enumClass must not be null");
        }
        return EnumSet.allOf(enumClass);
    }

    /**
     * Creates an enum value map
     * 创建枚举值映射
     *
     * @param enumClass     the enum class | 枚举类
     * @param keyExtractor  the key extractor | 键提取器
     * @param <E>           the enum type | 枚举类型
     * @param <K>           the key type | 键类型
     * @return the enum map | 枚举映射
     */
    public static <E extends Enum<E>, K> Map<K, E> getEnumValueMap(Class<E> enumClass,
                                                                    Function<E, K> keyExtractor) {
        if (enumClass == null || keyExtractor == null) {
            return Collections.emptyMap();
        }
        Map<K, E> map = new HashMap<>();
        for (E e : enumClass.getEnumConstants()) {
            map.put(keyExtractor.apply(e), e);
        }
        return Collections.unmodifiableMap(map);
    }

    /**
     * Gets a list of enum names
     * 获取枚举名称列表
     *
     * @param enumClass the enum class | 枚举类
     * @param <E>       the enum type | 枚举类型
     * @return the list of names | 名称列表
     */
    public static <E extends Enum<E>> List<String> getEnumNames(Class<E> enumClass) {
        if (enumClass == null) {
            return Collections.emptyList();
        }
        E[] constants = enumClass.getEnumConstants();
        List<String> names = new ArrayList<>(constants.length);
        for (E e : constants) {
            names.add(e.name());
        }
        return names;
    }

    // ==================== 验证 ====================

    /**
     * Validates whether the enum name is valid
     * 验证枚举名称是否有效
     *
     * @param enumClass the enum class | 枚举类
     * @param name      the enum name | 枚举名称
     * @param <E>       the enum type | 枚举类型
     * @return true if valid | 如果有效返回 true
     */
    public static <E extends Enum<E>> boolean isValidEnum(Class<E> enumClass, String name) {
        return getEnumByNameOptional(enumClass, name).isPresent();
    }

    /**
     * Validates whether the enum name is valid (case-insensitive)
     * 验证枚举名称是否有效（忽略大小写）
     */
    public static <E extends Enum<E>> boolean isValidEnumIgnoreCase(Class<E> enumClass, String name) {
        return getEnumByNameIgnoreCase(enumClass, name) != null;
    }

    // ==================== 工具方法 ====================

    /**
     * Gets the ordinal of the enum value
     * 获取枚举的 ordinal
     */
    public static int ordinal(Enum<?> enumValue) {
        return enumValue == null ? -1 : enumValue.ordinal();
    }

    /**
     * Gets the name of the enum value
     * 获取枚举的名称
     */
    public static String name(Enum<?> enumValue) {
        return enumValue == null ? null : enumValue.name();
    }

    /**
     * Gets an enum value by its ordinal
     * 根据 ordinal 获取枚举值
     */
    public static <E extends Enum<E>> E getByOrdinal(Class<E> enumClass, int ordinal) {
        if (enumClass == null) {
            return null;
        }
        E[] constants = enumClass.getEnumConstants();
        if (ordinal < 0 || ordinal >= constants.length) {
            return null;
        }
        return constants[ordinal];
    }
}
