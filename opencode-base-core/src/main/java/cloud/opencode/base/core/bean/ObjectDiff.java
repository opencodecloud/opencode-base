package cloud.opencode.base.core.bean;

import cloud.opencode.base.core.exception.OpenIllegalStateException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.time.*;
import java.util.*;

/**
 * Object Difference Comparison Engine - Compares two objects and reports property-level diffs
 * 对象差异比较引擎 - 比较两个对象并报告属性级别的差异
 *
 * <p>Provides both simple shallow comparison and advanced deep comparison with
 * cycle detection, depth limits, include/exclude field filtering, and collection diff support.</p>
 * <p>提供简单的浅比较和高级深度比较功能，支持循环引用检测、深度限制、
 * 字段包含/排除过滤以及集合差异比较。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Shallow property comparison using Objects.equals - 使用 Objects.equals 的浅属性比较</li>
 *   <li>Deep recursive comparison with cycle detection - 带循环引用检测的深度递归比较</li>
 *   <li>Configurable max depth with overflow protection - 可配置的最大深度及溢出保护</li>
 *   <li>Include/exclude field filtering - 字段包含/排除过滤</li>
 *   <li>Collection element-level diff - 集合元素级别差异比较</li>
 *   <li>Record type support - Record 类型支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Simple compare - 简单比较
 * DiffResult<User> result = ObjectDiff.compare(oldUser, newUser);
 * if (result.hasDiffs()) {
 *     result.getModified().forEach(d ->
 *         System.out.println(d.fieldName() + ": " + d.oldValue() + " -> " + d.newValue()));
 * }
 *
 * // Advanced compare with builder - 使用构建器进行高级比较
 * DiffResult<User> result = ObjectDiff.builder(oldUser, newUser)
 *     .deep(true)
 *     .maxDepth(5)
 *     .exclude("password", "internalId")
 *     .collectionDiff(true)
 *     .compare();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility, Builder is single-thread use then compare) -
 *       线程安全: 是 (无状态工具，Builder 为单线程使用后比较)</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.3
 */
public final class ObjectDiff {

    /**
     * Set of types considered "simple" (compared by value, not recursed into)
     * 被视为"简单"类型的集合（按值比较，不递归）
     */
    private static final Set<Class<?>> SIMPLE_TYPES = Set.of(
            Boolean.class, Byte.class, Short.class, Integer.class, Long.class,
            Float.class, Double.class, Character.class, String.class,
            BigDecimal.class, BigInteger.class,
            LocalDate.class, LocalTime.class, LocalDateTime.class,
            ZonedDateTime.class, OffsetDateTime.class, Instant.class,
            Duration.class, Period.class,
            UUID.class
    );

    private ObjectDiff() {
    }

    // ==================== Simple Compare | 简单比较 ====================

    /**
     * Compares two objects shallowly, reporting property-level diffs
     * 浅比较两个对象，报告属性级别的差异
     *
     * <p>Uses {@link Objects#equals(Object, Object)} for each property value.</p>
     * <p>对每个属性值使用 {@link Objects#equals(Object, Object)} 进行比较。</p>
     *
     * @param <T>    the object type | 对象类型
     * @param oldObj the old object (nullable) | 旧对象（可为 null）
     * @param newObj the new object (nullable) | 新对象（可为 null）
     * @return the diff result | 差异结果
     */
    @SuppressWarnings("unchecked")
    public static <T> DiffResult<T> compare(T oldObj, T newObj) {
        if (oldObj == null && newObj == null) {
            return new DiffResult<>((Class<T>) Object.class, List.of());
        }

        Class<T> type = (Class<T>) (oldObj != null ? oldObj.getClass() : newObj.getClass());
        List<PropertyDescriptor> props = OpenBean.getPropertyDescriptors(type);
        List<Diff<?>> diffs = new ArrayList<>();

        for (PropertyDescriptor pd : props) {
            if (!pd.isReadable() && !pd.hasField()) {
                continue;
            }
            String fieldName = pd.name();
            Object oldVal = oldObj != null ? pd.getValue(oldObj) : null;
            Object newVal = newObj != null ? pd.getValue(newObj) : null;
            diffs.add(createDiff(fieldName, oldVal, newVal));
        }

        return new DiffResult<>(type, diffs);
    }

    // ==================== Builder | 构建器 ====================

    /**
     * Creates an advanced diff builder for the two objects
     * 为两个对象创建高级差异比较构建器
     *
     * @param <T>    the object type | 对象类型
     * @param oldObj the old object (nullable) | 旧对象（可为 null）
     * @param newObj the new object (nullable) | 新对象（可为 null）
     * @return a new builder instance | 新的构建器实例
     */
    public static <T> ObjectDiffBuilder<T> builder(T oldObj, T newObj) {
        return new ObjectDiffBuilder<>(oldObj, newObj);
    }

    // ==================== ObjectDiffBuilder | 差异比较构建器 ====================

    /**
     * Builder for advanced object diff comparison
     * 高级对象差异比较构建器
     *
     * <p>Provides fine-grained control over the comparison process including
     * deep recursion, depth limits, field filtering, and collection diff.</p>
     * <p>提供对比较过程的精细控制，包括深度递归、深度限制、字段过滤和集合差异。</p>
     *
     * @param <T> the object type | 对象类型
     *
     * @author Leon Soo
     * <a href="https://leonsoo.com">www.LeonSoo.com</a>
     * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
     * @since JDK 25, opencode-base-core V1.0.3
     */
    public static final class ObjectDiffBuilder<T> {

        private final T oldObj;
        private final T newObj;
        private boolean deep = false;
        private int maxDepth = 10;
        private int maxCollectionSize = 10_000;
        private Set<String> includeFields;
        private Set<String> excludeFields;
        private boolean collectionDiff = false;

        private ObjectDiffBuilder(T oldObj, T newObj) {
            this.oldObj = oldObj;
            this.newObj = newObj;
        }

        /**
         * Enables or disables deep recursive comparison
         * 启用或禁用深度递归比较
         *
         * @param deep true to enable deep comparison | true 启用深度比较
         * @return this builder | 此构建器
         */
        public ObjectDiffBuilder<T> deep(boolean deep) {
            this.deep = deep;
            return this;
        }

        /**
         * Sets the maximum recursion depth for deep comparison
         * 设置深度比较的最大递归深度
         *
         * @param maxDepth the max depth (must be positive) | 最大深度（必须为正数）
         * @return this builder | 此构建器
         * @throws IllegalArgumentException if maxDepth is not positive | 如果 maxDepth 不为正数
         */
        public ObjectDiffBuilder<T> maxDepth(int maxDepth) {
            if (maxDepth <= 0) {
                throw new IllegalArgumentException("maxDepth must be positive, got: " + maxDepth);
            }
            this.maxDepth = maxDepth;
            return this;
        }

        /**
         * Sets the maximum collection size for comparison; larger collections are marked UNCHANGED
         * 设置比较的最大集合大小；超过限制的集合标记为 UNCHANGED
         *
         * @param max the max collection size (must be positive) | 最大集合大小（必须为正数）
         * @return this builder | 此构建器
         * @throws IllegalArgumentException if max is not positive | 如果 max 不为正数
         */
        public ObjectDiffBuilder<T> maxCollectionSize(int max) {
            if (max <= 0) {
                throw new IllegalArgumentException("maxCollectionSize must be positive, got: " + max);
            }
            this.maxCollectionSize = max;
            return this;
        }

        /**
         * Sets the fields to include in comparison (whitelist); others are excluded
         * 设置要包含在比较中的字段（白名单）；其他字段被排除
         *
         * @param fields the field names to include | 要包含的字段名称
         * @return this builder | 此构建器
         */
        public ObjectDiffBuilder<T> include(String... fields) {
            this.includeFields = Set.of(fields);
            return this;
        }

        /**
         * Sets the fields to exclude from comparison (blacklist)
         * 设置要从比较中排除的字段（黑名单）
         *
         * @param fields the field names to exclude | 要排除的字段名称
         * @return this builder | 此构建器
         */
        public ObjectDiffBuilder<T> exclude(String... fields) {
            this.excludeFields = Set.of(fields);
            return this;
        }

        /**
         * Enables or disables element-level collection diff
         * 启用或禁用元素级别的集合差异比较
         *
         * @param diff true to enable collection diff | true 启用集合差异比较
         * @return this builder | 此构建器
         */
        public ObjectDiffBuilder<T> collectionDiff(boolean diff) {
            this.collectionDiff = diff;
            return this;
        }

        /**
         * Executes the comparison and returns the result
         * 执行比较并返回结果
         *
         * @return the diff result | 差异结果
         * @throws OpenIllegalStateException if max depth is exceeded during deep comparison |
         *         深度比较时超过最大深度
         */
        @SuppressWarnings("unchecked")
        public DiffResult<T> compare() {
            if (oldObj == null && newObj == null) {
                return new DiffResult<>((Class<T>) Object.class, List.of());
            }

            Class<T> type = (Class<T>) (oldObj != null ? oldObj.getClass() : newObj.getClass());
            List<PropertyDescriptor> props = OpenBean.getPropertyDescriptors(type);
            List<Diff<?>> diffs = new ArrayList<>();
            IdentityHashMap<Object, Boolean> visited = new IdentityHashMap<>();

            // Add root objects to visited set for cycle detection
            if (deep) {
                if (oldObj != null) {
                    visited.put(oldObj, Boolean.TRUE);
                }
                if (newObj != null) {
                    visited.put(newObj, Boolean.TRUE);
                }
            }

            for (PropertyDescriptor pd : props) {
                if (!pd.isReadable() && !pd.hasField()) {
                    continue;
                }
                String fieldName = pd.name();
                if (!shouldInclude(fieldName)) {
                    continue;
                }

                Object oldVal = oldObj != null ? pd.getValue(oldObj) : null;
                Object newVal = newObj != null ? pd.getValue(newObj) : null;

                if (deep) {
                    diffs.add(deepCompare(fieldName, oldVal, newVal, visited, 0));
                } else {
                    diffs.add(createDiff(fieldName, oldVal, newVal));
                }
            }

            return new DiffResult<>(type, diffs);
        }

        /**
         * Checks whether the given field should be included based on include/exclude filters
         * 检查给定字段是否应基于包含/排除过滤器被包含
         */
        private boolean shouldInclude(String fieldName) {
            if (includeFields != null && !includeFields.contains(fieldName)) {
                return false;
            }
            if (excludeFields != null && excludeFields.contains(fieldName)) {
                return false;
            }
            return true;
        }

        /**
         * Performs deep recursive comparison of two values
         * 执行两个值的深度递归比较
         */
        @SuppressWarnings("unchecked")
        private Diff<?> deepCompare(String fieldName, Object oldVal, Object newVal,
                                     IdentityHashMap<Object, Boolean> visited, int depth) {
            // Null cases
            if (oldVal == null && newVal == null) {
                return new Diff<>(fieldName, null, null, ChangeType.UNCHANGED);
            }
            if (oldVal == null) {
                return new Diff<>(fieldName, null, newVal, ChangeType.ADDED);
            }
            if (newVal == null) {
                return new Diff<>(fieldName, oldVal, null, ChangeType.REMOVED);
            }

            // Identity check
            if (oldVal == newVal) {
                return new Diff<>(fieldName, oldVal, newVal, ChangeType.UNCHANGED);
            }

            // Depth check
            if (depth >= maxDepth) {
                throw new OpenIllegalStateException(
                        "Max depth " + maxDepth + " exceeded at field: " + fieldName);
            }

            // Simple/primitive types - compare by value
            if (isSimpleType(oldVal.getClass())) {
                return createDiff(fieldName, oldVal, newVal);
            }

            // Cycle detection (for non-simple objects including collections/maps)
            if (visited.containsKey(oldVal) || visited.containsKey(newVal)) {
                return new Diff<>(fieldName, oldVal, newVal, ChangeType.CIRCULAR_REFERENCE);
            }

            // Collection comparison
            if (oldVal instanceof Collection<?> oldColl && newVal instanceof Collection<?> newColl) {
                visited.put(oldColl, Boolean.TRUE);
                visited.put(newColl, Boolean.TRUE);
                try {
                    return compareCollections(fieldName, oldColl, newColl, visited, depth);
                } finally {
                    visited.remove(oldColl);
                    visited.remove(newColl);
                }
            }

            // Map comparison
            if (oldVal instanceof Map<?, ?> oldMap && newVal instanceof Map<?, ?> newMap) {
                visited.put(oldMap, Boolean.TRUE);
                visited.put(newMap, Boolean.TRUE);
                try {
                    return compareMaps(fieldName, oldMap, newMap);
                } finally {
                    visited.remove(oldMap);
                    visited.remove(newMap);
                }
            }

            // Array comparison
            if (oldVal.getClass().isArray() && newVal.getClass().isArray()) {
                return compareArrays(fieldName, oldVal, newVal);
            }

            // Bean comparison - recurse into properties
            // Use path-based cycle detection: add on entry, remove on exit
            if (oldVal.getClass() == newVal.getClass()) {
                visited.put(oldVal, Boolean.TRUE);
                visited.put(newVal, Boolean.TRUE);
                try {

                List<PropertyDescriptor> nestedProps = OpenBean.getPropertyDescriptors(oldVal.getClass());
                boolean anyDiff = false;
                boolean hasCircular = false;

                for (PropertyDescriptor npd : nestedProps) {
                    if (!npd.isReadable() && !npd.hasField()) {
                        continue;
                    }
                    Object nestedOld = npd.getValue(oldVal);
                    Object nestedNew = npd.getValue(newVal);
                    Diff<?> nestedDiff = deepCompare(
                            fieldName + "." + npd.name(), nestedOld, nestedNew, visited, depth + 1);
                    if (nestedDiff.changeType() == ChangeType.CIRCULAR_REFERENCE) {
                        hasCircular = true;
                    } else if (nestedDiff.changeType() != ChangeType.UNCHANGED) {
                        anyDiff = true;
                    }
                }

                if (hasCircular) {
                    return new Diff<>(fieldName, oldVal, newVal, ChangeType.CIRCULAR_REFERENCE);
                }
                if (anyDiff) {
                    return new Diff<>(fieldName, oldVal, newVal, ChangeType.MODIFIED);
                }
                return new Diff<>(fieldName, oldVal, newVal, ChangeType.UNCHANGED);

                } finally {
                    // Remove from visited on exit to allow the same object
                    // to be compared in a sibling field path without false CIRCULAR_REFERENCE
                    visited.remove(oldVal);
                    visited.remove(newVal);
                }
            }

            // Different types
            return createDiff(fieldName, oldVal, newVal);
        }

        /**
         * Compares two collections element by element when collectionDiff is enabled
         * 启用 collectionDiff 时逐元素比较两个集合
         */
        private Diff<?> compareCollections(String fieldName, Collection<?> oldColl, Collection<?> newColl,
                                            IdentityHashMap<Object, Boolean> visited, int depth) {
            // Size guard: skip deep element comparison for large collections,
            // but still compute MODIFIED vs UNCHANGED via equals
            if (oldColl.size() > maxCollectionSize || newColl.size() > maxCollectionSize) {
                return createDiff(fieldName, oldColl, newColl);
            }

            if (!collectionDiff) {
                return createDiff(fieldName, oldColl, newColl);
            }

            // Element-level comparison
            List<?> oldList = oldColl instanceof List<?> ol ? ol : new ArrayList<>(oldColl);
            List<?> newList = newColl instanceof List<?> nl ? nl : new ArrayList<>(newColl);

            int maxSize = Math.max(oldList.size(), newList.size());
            boolean anyDiff = false;

            for (int i = 0; i < maxSize; i++) {
                Object oldElem = i < oldList.size() ? oldList.get(i) : null;
                Object newElem = i < newList.size() ? newList.get(i) : null;

                Diff<?> elemDiff = deepCompare(fieldName + "[" + i + "]", oldElem, newElem, visited, depth + 1);
                if (elemDiff.changeType() != ChangeType.UNCHANGED) {
                    anyDiff = true;
                    break;
                }
            }

            if (anyDiff) {
                return new Diff<>(fieldName, oldColl, newColl, ChangeType.MODIFIED);
            }
            return new Diff<>(fieldName, oldColl, newColl, ChangeType.UNCHANGED);
        }

        /**
         * Compares two maps by their entries
         * 通过条目比较两个 Map
         */
        private Diff<?> compareMaps(String fieldName, Map<?, ?> oldMap, Map<?, ?> newMap) {
            if (Objects.equals(oldMap, newMap)) {
                return new Diff<>(fieldName, oldMap, newMap, ChangeType.UNCHANGED);
            }
            return new Diff<>(fieldName, oldMap, newMap, ChangeType.MODIFIED);
        }

        /**
         * Compares two arrays element by element
         * 逐元素比较两个数组
         */
        private Diff<?> compareArrays(String fieldName, Object oldArr, Object newArr) {
            int oldLen = java.lang.reflect.Array.getLength(oldArr);
            int newLen = java.lang.reflect.Array.getLength(newArr);

            if (oldLen != newLen) {
                return new Diff<>(fieldName, oldArr, newArr, ChangeType.MODIFIED);
            }

            for (int i = 0; i < oldLen; i++) {
                Object oldElem = java.lang.reflect.Array.get(oldArr, i);
                Object newElem = java.lang.reflect.Array.get(newArr, i);
                if (!Objects.equals(oldElem, newElem)) {
                    return new Diff<>(fieldName, oldArr, newArr, ChangeType.MODIFIED);
                }
            }

            return new Diff<>(fieldName, oldArr, newArr, ChangeType.UNCHANGED);
        }
    }

    // ==================== Private Helpers | 私有辅助方法 ====================

    /**
     * Creates a Diff by comparing old and new values with Objects.equals
     * 通过 Objects.equals 比较旧值和新值创建 Diff
     */
    private static Diff<Object> createDiff(String fieldName, Object oldVal, Object newVal) {
        if (oldVal == null && newVal == null) {
            return new Diff<>(fieldName, null, null, ChangeType.UNCHANGED);
        }
        if (oldVal == null) {
            return new Diff<>(fieldName, null, newVal, ChangeType.ADDED);
        }
        if (newVal == null) {
            return new Diff<>(fieldName, oldVal, null, ChangeType.REMOVED);
        }
        if (Objects.equals(oldVal, newVal)) {
            return new Diff<>(fieldName, oldVal, newVal, ChangeType.UNCHANGED);
        }
        return new Diff<>(fieldName, oldVal, newVal, ChangeType.MODIFIED);
    }

    /**
     * Checks whether the given type is a simple/value type
     * 检查给定类型是否为简单/值类型
     */
    private static boolean isSimpleType(Class<?> type) {
        return type.isPrimitive()
                || type.isEnum()
                || SIMPLE_TYPES.contains(type)
                || type == Class.class
                || URI.class.isAssignableFrom(type)
                || URL.class.isAssignableFrom(type);
    }
}
