package cloud.opencode.base.core.builder;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Builder Utility Class - Unified entry point for builders
 * 构建器工具类 - 构建器统一入口
 *
 * <p>Provides unified factory methods for creating various builder instances.</p>
 * <p>提供创建各种构建器实例的统一工厂方法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Bean builder (ofBean, from) - JavaBean 构建器</li>
 *   <li>Record builder (ofRecord, fromRecord) - Record 构建器</li>
 *   <li>Map builder (ofMap, ofHashMap, ofLinkedHashMap, ofTreeMap) - Map 构建器</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * User user = OpenBuilder.ofBean(User.class)
 *     .set("name", "John")
 *     .set("age", 25)
 *     .build();
 *
 * Map<String, Object> map = OpenBuilder.<String, Object>ofHashMap()
 *     .put("key1", "value1")
 *     .put("key2", "value2")
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是 (无状态)</li>
 * </ul>
 *
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) per step - 每步 O(1)</li>
 *   <li>Space complexity: O(1) - O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class OpenBuilder {

    private OpenBuilder() {
    }

    /**
     * Creates
     * 创建 JavaBean 构建器
     */
    public static <T> BeanBuilder<T> ofBean(Class<T> beanClass) {
        return BeanBuilder.of(beanClass);
    }

    /**
     * Creates a JavaBean builder from an existing instance
     * 从现有实例创建 JavaBean 构建器
     */
    public static <T> BeanBuilder<T> from(T source) {
        return BeanBuilder.from(source);
    }

    /**
     * Creates
     * 创建 Record 构建器
     */
    public static <T extends Record> RecordBuilder<T> ofRecord(Class<T> recordClass) {
        return RecordBuilder.of(recordClass);
    }

    /**
     * Creates a builder from an existing Record
     * 从现有 Record 创建构建器
     */
    public static <T extends Record> RecordBuilder<T> fromRecord(T record) {
        return RecordBuilder.from(record);
    }

    /**
     * Creates
     * 创建 Map 构建器
     */
    public static <K, V> MapBuilder<K, V> ofMap() {
        return MapBuilder.of();
    }

    /**
     * Creates
     * 创建 Map 构建器（指定实现）
     */
    public static <K, V> MapBuilder<K, V> ofMap(Supplier<Map<K, V>> mapSupplier) {
        return MapBuilder.of(mapSupplier);
    }

    /**
     * Creates
     * 创建 HashMap 构建器
     */
    public static <K, V> MapBuilder<K, V> ofHashMap() {
        return MapBuilder.hashMap();
    }

    /**
     * Creates
     * 创建 LinkedHashMap 构建器
     */
    public static <K, V> MapBuilder<K, V> ofLinkedHashMap() {
        return MapBuilder.linkedHashMap();
    }

    /**
     * Creates
     * 创建 TreeMap 构建器
     */
    public static <K extends Comparable<K>, V> MapBuilder<K, V> ofTreeMap() {
        return MapBuilder.treeMap();
    }
}
