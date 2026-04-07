package cloud.opencode.base.test.data;

import cloud.opencode.base.test.exception.DataGenerationException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * AutoFill - Auto-populates Record and POJO instances via reflection
 * 自动填充 - 通过反射自动填充Record和POJO实例
 *
 * <p>Generates fully populated test objects without manually setting every field.
 * Supports both Java Records (via canonical constructor) and traditional POJOs
 * (via no-arg constructor + setter methods).</p>
 * <p>生成完全填充的测试对象，无需手动设置每个字段。
 * 支持Java Record（通过规范构造函数）和传统POJO（通过无参构造函数 + setter方法）。</p>
 *
 * <p><strong>Value generation notes | 值生成说明:</strong></p>
 * <ul>
 *   <li>Generated int and long values are always positive (1..bound) - 生成的int和long值始终为正数</li>
 *   <li>Collection fields (List, Set, Map) are filled with 1-2 String elements due to type erasure -
 *       由于类型擦除，集合字段使用1-2个String元素填充</li>
 * </ul>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Record support via canonical constructor - 通过规范构造函数支持Record</li>
 *   <li>POJO support via no-arg constructor + setters - 通过无参构造函数和setter支持POJO</li>
 *   <li>Deterministic generation with seed - 使用种子进行确定性生成</li>
 *   <li>Field override for specific values - 字段覆盖以设置特定值</li>
 *   <li>Recursive filling with max depth control - 带最大深度控制的递归填充</li>
 *   <li>Batch generation via list() - 通过list()批量生成</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Simple record filling
 * var user = AutoFill.of(UserRecord.class).build();
 *
 * // Deterministic with seed
 * var user = AutoFill.of(UserRecord.class).seed(42L).build();
 *
 * // Override specific fields
 * var user = AutoFill.of(UserRecord.class)
 *     .with("name", "Alice")
 *     .with("age", 30)
 *     .build();
 *
 * // Generate a list of 10 instances
 * List<UserRecord> users = AutoFill.of(UserRecord.class).list(10);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (Builder is not thread-safe) - 线程安全: 否（Builder非线程安全）</li>
 *   <li>Null-safe: Yes (rejects null type) - 空值安全: 是（拒绝空类型）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.3
 */
public final class AutoFill {

    private static final int DEFAULT_MAX_DEPTH = 3;
    private static final int STRING_LENGTH = 8;
    private static final int BYTE_ARRAY_LENGTH = 16;
    private static final int INT_BOUND = 9999;
    private static final long LONG_BOUND = 999_999L;
    private static final double DOUBLE_BOUND = 1000.0;
    private static final float FLOAT_BOUND = 100.0f;
    private static final int MAX_DURATION_SECONDS = 3600;
    private static final char[] ALPHA_CHARS = "abcdefghijklmnopqrstuvwxyz".toCharArray();

    private AutoFill() {
        // utility class
    }

    /**
     * Creates a builder for the given type.
     * 为指定类型创建构建器。
     *
     * @param <T>  the target type | 目标类型
     * @param type the class to auto-fill | 要自动填充的类
     * @return a new builder | 新的构建器
     * @throws NullPointerException if type is null | 当类型为空时抛出
     */
    public static <T> Builder<T> of(Class<T> type) {
        Objects.requireNonNull(type, "type must not be null");
        return new Builder<>(type);
    }

    /**
     * Builder for configuring and creating auto-filled instances.
     * 用于配置和创建自动填充实例的构建器。
     *
     * @param <T> the target type | 目标类型
     *
     * @author Leon Soo
     * <a href="https://leonsoo.com">www.LeonSoo.com</a>
     * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
     * @since JDK 25, opencode-base-test V1.0.3
     */
    public static final class Builder<T> {

        private final Class<T> type;
        private final Map<String, Object> overrides = new HashMap<>();
        private Long seed;
        private int maxDepth = DEFAULT_MAX_DEPTH;

        private Builder(Class<T> type) {
            this.type = type;
        }

        /**
         * Sets the random seed for deterministic generation.
         * 设置随机种子以进行确定性生成。
         *
         * @param seed the random seed | 随机种子
         * @return this builder | 此构建器
         */
        public Builder<T> seed(long seed) {
            this.seed = seed;
            return this;
        }

        /**
         * Overrides a specific field with the given value.
         * 使用给定值覆盖特定字段。
         *
         * <p>Note: overrides only apply to the top-level object and are NOT propagated
         * to nested Records or POJOs.</p>
         * <p>注意: 覆盖仅适用于顶层对象，不会传递到嵌套的Record或POJO。</p>
         *
         * @param fieldName the field/component name | 字段/组件名称
         * @param value     the value to set | 要设置的值
         * @return this builder | 此构建器
         * @throws NullPointerException if fieldName is null | 当字段名为空时抛出
         */
        public Builder<T> with(String fieldName, Object value) {
            Objects.requireNonNull(fieldName, "fieldName must not be null");
            overrides.put(fieldName, value);
            return this;
        }

        /**
         * Sets the maximum recursion depth for nested objects.
         * 设置嵌套对象的最大递归深度。
         *
         * @param depth the maximum depth (must be positive) | 最大深度（必须为正数）
         * @return this builder | 此构建器
         * @throws IllegalArgumentException if depth is not positive | 当深度不是正数时抛出
         */
        public Builder<T> maxDepth(int depth) {
            if (depth <= 0) {
                throw new IllegalArgumentException("maxDepth must be positive, got: " + depth);
            }
            this.maxDepth = depth;
            return this;
        }

        /**
         * Builds a single auto-filled instance.
         * 构建单个自动填充的实例。
         *
         * @return the auto-filled instance | 自动填充的实例
         * @throws DataGenerationException if instance creation fails | 当实例创建失败时抛出
         */
        public T build() {
            Random random = (seed != null) ? new Random(seed) : new Random();
            try {
                return createInstance(type, random, overrides, 0, maxDepth);
            } catch (Exception e) {
                throw new DataGenerationException(
                    "Failed to auto-fill " + type.getName(), e);
            }
        }

        /**
         * Builds a list of auto-filled instances.
         * 构建自动填充实例的列表。
         *
         * @param count the number of instances to create (must be positive) | 要创建的实例数量（必须为正数）
         * @return the list of auto-filled instances | 自动填充实例的列表
         * @throws IllegalArgumentException  if count is not positive | 当数量不是正数时抛出
         * @throws DataGenerationException   if instance creation fails | 当实例创建失败时抛出
         */
        public List<T> list(int count) {
            if (count <= 0) {
                throw new IllegalArgumentException("count must be positive, got: " + count);
            }
            Random random = (seed != null) ? new Random(seed) : new Random();
            try {
                // Resolve reflection metadata once, reuse for all iterations
                ReflectionMeta<T> meta = ReflectionMeta.resolve(type);
                List<T> result = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    result.add(createFromMeta(meta, random, overrides, 0, maxDepth));
                }
                return Collections.unmodifiableList(result);
            } catch (Exception e) {
                throw new DataGenerationException(
                    "Failed to auto-fill list of " + type.getName(), e);
            }
        }
    }

    // ==================== Internal | 内部实现 ====================

    /**
     * Cached reflection metadata for a single class to avoid repeated
     * reflection lookups when generating multiple instances.
     */
    private record ReflectionMeta<T>(
        Class<T> type,
        boolean isRecord,
        RecordComponent[] recordComponents,  // non-null only for records
        Constructor<T> constructor,
        Method[] setters                     // non-null only for POJOs
    ) {

        @SuppressWarnings("unchecked")
        static <T> ReflectionMeta<T> resolve(Class<T> type) {
            if (type.isRecord()) {
                RecordComponent[] components = type.getRecordComponents();
                Class<?>[] paramTypes = new Class<?>[components.length];
                for (int i = 0; i < components.length; i++) {
                    paramTypes[i] = components[i].getType();
                }
                try {
                    Constructor<T> ctor = type.getDeclaredConstructor(paramTypes);
                    return new ReflectionMeta<>(type, true, components, ctor, null);
                } catch (NoSuchMethodException e) {
                    throw new DataGenerationException(
                        "Failed to find canonical constructor for record " + type.getName(), e);
                }
            } else {
                Constructor<T> ctor;
                try {
                    ctor = type.getDeclaredConstructor();
                    ctor.setAccessible(true);
                } catch (NoSuchMethodException e) {
                    throw new DataGenerationException(
                        "Failed to create POJO instance of " + type.getName()
                            + " (no-arg constructor required)", e);
                }
                Method[] allMethods = type.getMethods();
                // Filter to only setter methods
                List<Method> setterList = new ArrayList<>();
                for (Method m : allMethods) {
                    String name = m.getName();
                    if (name.startsWith("set") && name.length() > 3
                            && m.getParameterTypes().length == 1) {
                        setterList.add(m);
                    }
                }
                return new ReflectionMeta<>(type, false, null, ctor,
                    setterList.toArray(new Method[0]));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T createInstance(Class<T> type, Random random,
                                        Map<String, Object> overrides,
                                        int currentDepth, int maxDepth) {
        if (currentDepth >= maxDepth) {
            return (T) primitiveDefault(type);
        }

        ReflectionMeta<T> meta = ReflectionMeta.resolve(type);
        return createFromMeta(meta, random, overrides, currentDepth, maxDepth);
    }

    @SuppressWarnings("unchecked")
    private static <T> T createFromMeta(ReflectionMeta<T> meta, Random random,
                                         Map<String, Object> overrides,
                                         int currentDepth, int maxDepth) {
        if (meta.isRecord()) {
            return createRecord(meta, random, overrides, currentDepth, maxDepth);
        } else {
            return createPojo(meta, random, overrides, currentDepth, maxDepth);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T createRecord(ReflectionMeta<T> meta, Random random,
                                       Map<String, Object> overrides,
                                       int currentDepth, int maxDepth) {
        RecordComponent[] components = meta.recordComponents();
        Object[] args = new Object[components.length];

        for (int i = 0; i < components.length; i++) {
            String name = components[i].getName();
            Class<?> componentType = components[i].getType();

            if (overrides.containsKey(name)) {
                args[i] = overrides.get(name);
            } else {
                args[i] = generateValue(componentType, random, currentDepth, maxDepth);
            }
        }

        try {
            return meta.constructor().newInstance(args);
        } catch (Exception e) {
            throw new DataGenerationException(
                "Failed to create record instance of " + meta.type().getName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T createPojo(ReflectionMeta<T> meta, Random random,
                                     Map<String, Object> overrides,
                                     int currentDepth, int maxDepth) {
        T instance;
        try {
            instance = meta.constructor().newInstance();
        } catch (Exception e) {
            throw new DataGenerationException(
                "Failed to create POJO instance of " + meta.type().getName()
                    + " (no-arg constructor required)", e);
        }

        for (Method method : meta.setters()) {
            String methodName = method.getName();
            String fieldName = Character.toLowerCase(methodName.charAt(3))
                + methodName.substring(4);
            Class<?> fieldType = method.getParameterTypes()[0];

            Object value;
            if (overrides.containsKey(fieldName)) {
                value = overrides.get(fieldName);
            } else {
                value = generateValue(fieldType, random, currentDepth, maxDepth);
            }

            try {
                method.invoke(instance, value);
            } catch (Exception e) {
                throw new DataGenerationException(
                    "Failed to invoke setter " + methodName + " on " + meta.type().getName(), e);
            }
        }

        return instance;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object generateValue(Class<?> type, Random random,
                                         int currentDepth, int maxDepth) {
        // Primitives and wrappers
        if (type == String.class) {
            return randomAlphabetic(random, STRING_LENGTH);
        }
        if (type == int.class || type == Integer.class) {
            return random.nextInt(INT_BOUND) + 1;
        }
        if (type == long.class || type == Long.class) {
            return random.nextLong(LONG_BOUND) + 1;
        }
        if (type == double.class || type == Double.class) {
            return random.nextDouble() * DOUBLE_BOUND;
        }
        if (type == float.class || type == Float.class) {
            return random.nextFloat() * FLOAT_BOUND;
        }
        if (type == boolean.class || type == Boolean.class) {
            return random.nextBoolean();
        }
        if (type == byte.class || type == Byte.class) {
            return (byte) random.nextInt(Byte.MAX_VALUE + 1);
        }
        if (type == short.class || type == Short.class) {
            return (short) random.nextInt(Short.MAX_VALUE + 1);
        }
        if (type == char.class || type == Character.class) {
            return ALPHA_CHARS[random.nextInt(ALPHA_CHARS.length)];
        }

        // byte[]
        if (type == byte[].class) {
            byte[] bytes = new byte[BYTE_ARRAY_LENGTH];
            random.nextBytes(bytes);
            return bytes;
        }

        // Date/Time types
        if (type == LocalDate.class) {
            long today = LocalDate.now().toEpochDay();
            long oneYearAgo = today - 365;
            return LocalDate.ofEpochDay(oneYearAgo + random.nextInt(366));
        }
        if (type == LocalDateTime.class) {
            long now = Instant.now().getEpochSecond();
            long oneYearSeconds = 365L * 24 * 60 * 60;
            long randomSecond = now - oneYearSeconds + (long) (random.nextDouble() * oneYearSeconds);
            return LocalDateTime.ofInstant(
                Instant.ofEpochSecond(randomSecond), ZoneOffset.UTC);
        }
        if (type == Instant.class) {
            long now = Instant.now().getEpochSecond();
            long oneYearSeconds = 365L * 24 * 60 * 60;
            long randomSecond = now - oneYearSeconds + (long) (random.nextDouble() * oneYearSeconds);
            return Instant.ofEpochSecond(randomSecond);
        }
        if (type == Duration.class) {
            return Duration.ofSeconds(random.nextInt(MAX_DURATION_SECONDS) + 1);
        }

        // UUID
        if (type == UUID.class) {
            byte[] uuidBytes = new byte[16];
            random.nextBytes(uuidBytes);
            // Set version 4 and variant bits
            uuidBytes[6] = (byte) ((uuidBytes[6] & 0x0f) | 0x40);
            uuidBytes[8] = (byte) ((uuidBytes[8] & 0x3f) | 0x80);
            long msb = 0;
            long lsb = 0;
            for (int i = 0; i < 8; i++) {
                msb = (msb << 8) | (uuidBytes[i] & 0xff);
            }
            for (int i = 8; i < 16; i++) {
                lsb = (lsb << 8) | (uuidBytes[i] & 0xff);
            }
            return new UUID(msb, lsb);
        }

        // Collections (generic type is unknown due to type erasure; fill with String elements)
        if (type == List.class || type == java.util.ArrayList.class) {
            List<String> list = new ArrayList<>(2);
            list.add(randomAlphabetic(random, STRING_LENGTH));
            list.add(randomAlphabetic(random, STRING_LENGTH));
            return Collections.unmodifiableList(list);
        }
        if (type == Set.class || type == java.util.HashSet.class) {
            Set<String> set = new HashSet<>(4);
            set.add(randomAlphabetic(random, STRING_LENGTH));
            set.add(randomAlphabetic(random, STRING_LENGTH));
            return Collections.unmodifiableSet(set);
        }
        if (type == Map.class || type == java.util.HashMap.class) {
            return Collections.singletonMap(
                randomAlphabetic(random, STRING_LENGTH),
                randomAlphabetic(random, STRING_LENGTH));
        }

        // Enum
        if (type.isEnum()) {
            Object[] constants = type.getEnumConstants();
            if (constants.length == 0) {
                return null;
            }
            return constants[random.nextInt(constants.length)];
        }

        // Nested Record or POJO - recurse
        if (type.isRecord() || hasNoArgConstructor(type)) {
            return createInstance(type, random, Map.of(), currentDepth + 1, maxDepth);
        }

        // Unknown type - return null
        return null;
    }

    private static boolean hasNoArgConstructor(Class<?> type) {
        try {
            type.getDeclaredConstructor();
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * Returns the default value for primitive types, or null for reference types.
     * Used when recursion depth is exceeded to avoid NPE on primitive Record components.
     */
    
    private static Object primitiveDefault(Class<?> type) {
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == double.class) return 0.0;
        if (type == float.class) return 0.0f;
        if (type == boolean.class) return false;
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == char.class) return '\0';
        return null;
    }

    private static String randomAlphabetic(Random random, int length) {
        char[] chars = new char[length];
        for (int i = 0; i < length; i++) {
            chars[i] = ALPHA_CHARS[random.nextInt(ALPHA_CHARS.length)];
        }
        return new String(chars);
    }
}
