
package cloud.opencode.base.serialization;

import cloud.opencode.base.serialization.binary.JdkSerializer;
import cloud.opencode.base.serialization.exception.OpenSerializationException;
import cloud.opencode.base.serialization.spi.SerializerProvider;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OpenSerializer - Unified Serialization Facade
 * 统一序列化门面
 *
 * <p>This is the main entry point for all serialization operations. It provides a unified API
 * for serializing and deserializing objects using various formats (JSON, XML, Kryo, Protobuf, etc.).</p>
 * <p>这是所有序列化操作的主入口点。它提供统一的 API，
 * 用于使用各种格式（JSON、XML、Kryo、Protobuf 等）序列化和反序列化对象。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Unified API for all formats - 所有格式的统一 API</li>
 *   <li>SPI-based serializer discovery - 基于 SPI 的序列化器发现</li>
 *   <li>TypeReference support for generics - 泛型的 TypeReference 支持</li>
 *   <li>Deep copy and type conversion - 深拷贝和类型转换</li>
 *   <li>Thread-safe operation - 线程安全操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Basic serialization/deserialization
 * byte[] data = OpenSerializer.serialize(user);
 * User restored = OpenSerializer.deserialize(data, User.class);
 *
 * // Specify format
 * byte[] json = OpenSerializer.serialize(user, "json");
 * byte[] kryo = OpenSerializer.serialize(user, "kryo");
 *
 * // String serialization
 * String jsonStr = OpenSerializer.serializeToString(user);
 *
 * // Generic types
 * List<User> users = OpenSerializer.deserialize(data, new TypeReference<List<User>>() {});
 * List<User> users = OpenSerializer.deserializeList(data, User.class);
 *
 * // Deep copy
 * User copy = OpenSerializer.deepCopy(user);
 *
 * // Type conversion
 * UserDTO dto = OpenSerializer.convert(user, UserDTO.class);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) where n = object graph size - O(n), n为对象图大小</li>
 *   <li>Space complexity: O(n) for serialized bytes - 序列化字节 O(n)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-serialization V1.0.0
 */
public final class OpenSerializer {

    private static final System.Logger LOG = System.getLogger(OpenSerializer.class.getName());

    /**
     * Registered serializers by format name
     * 按格式名称注册的序列化器
     */
    private static final Map<String, Serializer> SERIALIZERS = new ConcurrentHashMap<>();

    /**
     * Default serializer
     * 默认序列化器
     */
    private static volatile Serializer defaultSerializer;

    /**
     * Global configuration
     * 全局配置
     */
    private static volatile SerializerConfig config = SerializerConfig.defaults();

    /**
     * MethodHandle for OpenClone.clone() - null if deepclone module not available
     * OpenClone.clone() 的 MethodHandle - 如果 deepclone 模块不可用则为 null
     */
    private static final MethodHandle DEEP_CLONE_HANDLE;

    // Static initialization - load serializers via SPI and detect DeepClone
    static {
        loadSerializers();
        DEEP_CLONE_HANDLE = initDeepCloneHandle();
    }

    /**
     * Initializes the MethodHandle for OpenClone.clone() if available.
     * 如果可用，初始化 OpenClone.clone() 的 MethodHandle。
     */
    private static MethodHandle initDeepCloneHandle() {
        try {
            Class<?> openCloneClass = Class.forName("cloud.opencode.base.deepclone.OpenClone");
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            return lookup.findStatic(
                    openCloneClass,
                    "clone",
                    MethodType.methodType(Object.class, Object.class)
            );
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            // DeepClone module not available, will use serialization fallback
            return null;
        }
    }

    /**
     * Checks if DeepClone module is available.
     * 检查 DeepClone 模块是否可用。
     *
     * @return true if available - 如果可用返回 true
     */
    public static boolean isDeepCloneAvailable() {
        return DEEP_CLONE_HANDLE != null;
    }

    private OpenSerializer() {
        // Utility class, no instantiation
    }

    /**
     * Loads serializers via SPI mechanism.
     * 通过 SPI 机制加载序列化器。
     */
    private static void loadSerializers() {
        List<SerializerProvider> providers = new ArrayList<>();

        // Load all available providers
        ServiceLoader.load(SerializerProvider.class).forEach(provider -> {
            if (provider.isAvailable()) {
                providers.add(provider);
            }
        });

        // Sort by priority (lower number = higher priority)
        providers.sort(Comparator.comparingInt(SerializerProvider::getPriority));

        // Register serializers
        for (SerializerProvider provider : providers) {
            Serializer serializer = provider.create();
            SERIALIZERS.put(serializer.getFormat(), serializer);

            // First available serializer becomes default
            if (defaultSerializer == null) {
                defaultSerializer = serializer;
            }
        }

        // Fallback to JDK serializer if no providers found
        if (defaultSerializer == null) {
            JdkSerializer jdk = new JdkSerializer();
            SERIALIZERS.put(jdk.getFormat(), jdk);
            defaultSerializer = jdk;
        }
    }

    // ==================== Configuration | 配置 ====================

    /**
     * Registers a serializer.
     * 注册序列化器。
     *
     * @param serializer the serializer to register - 要注册的序列化器
     */
    public static void register(Serializer serializer) {
        Objects.requireNonNull(serializer, "Serializer must not be null");
        SERIALIZERS.put(serializer.getFormat(), serializer);
    }

    /**
     * Sets the default serializer by format name.
     * 按格式名称设置默认序列化器。
     *
     * @param format the format name - 格式名称
     */
    public static void setDefault(String format) {
        defaultSerializer = get(format);
    }

    /**
     * Sets the default serializer.
     * 设置默认序列化器。
     *
     * @param serializer the serializer - 序列化器
     */
    public static void setDefault(Serializer serializer) {
        Objects.requireNonNull(serializer, "Serializer must not be null");
        defaultSerializer = serializer;
    }

    /**
     * Sets the global configuration.
     * 设置全局配置。
     *
     * @param config the configuration - 配置
     */
    public static void setConfig(SerializerConfig config) {
        OpenSerializer.config = Objects.requireNonNull(config, "Config must not be null");
    }

    /**
     * Gets the global configuration.
     * 获取全局配置。
     *
     * @return the configuration - 配置
     */
    public static SerializerConfig getConfig() {
        return config;
    }

    /**
     * Gets a serializer by format name.
     * 按格式名称获取序列化器。
     *
     * @param format the format name - 格式名称
     * @return the serializer - 序列化器
     * @throws OpenSerializationException if serializer not found - 如果未找到序列化器
     */
    public static Serializer get(String format) {
        Serializer serializer = SERIALIZERS.get(format);
        if (serializer == null) {
            throw OpenSerializationException.serializerNotFound(format);
        }
        return serializer;
    }

    /**
     * Gets the default serializer.
     * 获取默认序列化器。
     *
     * @return the default serializer - 默认序列化器
     */
    public static Serializer getDefault() {
        return defaultSerializer;
    }

    /**
     * Gets all registered format names.
     * 获取所有已注册的格式名称。
     *
     * @return set of format names - 格式名称集合
     */
    public static Set<String> getFormats() {
        return Set.copyOf(SERIALIZERS.keySet());
    }

    /**
     * Checks if a format is available.
     * 检查格式是否可用。
     *
     * @param format the format name - 格式名称
     * @return true if available - 如果可用则返回 true
     */
    public static boolean hasFormat(String format) {
        return SERIALIZERS.containsKey(format);
    }

    // ==================== Serialization | 序列化 ====================

    /**
     * Serializes an object to byte array using the default serializer.
     * 使用默认序列化器将对象序列化为字节数组。
     *
     * @param obj the object to serialize - 要序列化的对象
     * @return the serialized bytes - 序列化后的字节数组
     */
    public static byte[] serialize(Object obj) {
        return defaultSerializer.serialize(obj);
    }

    /**
     * Serializes an object to byte array using the specified format.
     * 使用指定格式将对象序列化为字节数组。
     *
     * @param obj    the object to serialize - 要序列化的对象
     * @param format the format name - 格式名称
     * @return the serialized bytes - 序列化后的字节数组
     */
    public static byte[] serialize(Object obj, String format) {
        return get(format).serialize(obj);
    }

    /**
     * Serializes an object to string using the default serializer.
     * 使用默认序列化器将对象序列化为字符串。
     *
     * @param obj the object to serialize - 要序列化的对象
     * @return the serialized string - 序列化后的字符串
     */
    public static String serializeToString(Object obj) {
        return new String(serialize(obj), StandardCharsets.UTF_8);
    }

    /**
     * Serializes an object to string using the specified format.
     * 使用指定格式将对象序列化为字符串。
     *
     * @param obj    the object to serialize - 要序列化的对象
     * @param format the format name - 格式名称
     * @return the serialized string - 序列化后的字符串
     */
    public static String serializeToString(Object obj, String format) {
        return new String(serialize(obj, format), StandardCharsets.UTF_8);
    }

    // ==================== Deserialization | 反序列化 ====================

    /**
     * Deserializes byte array to an object using the default serializer.
     * 使用默认序列化器将字节数组反序列化为对象。
     *
     * @param data the serialized data - 序列化的数据
     * @param type the target class - 目标类
     * @param <T>  the target type - 目标类型
     * @return the deserialized object - 反序列化后的对象
     */
    public static <T> T deserialize(byte[] data, Class<T> type) {
        return defaultSerializer.deserialize(data, type);
    }

    /**
     * Deserializes byte array to an object using the specified format.
     * 使用指定格式将字节数组反序列化为对象。
     *
     * @param data   the serialized data - 序列化的数据
     * @param type   the target class - 目标类
     * @param format the format name - 格式名称
     * @param <T>    the target type - 目标类型
     * @return the deserialized object - 反序列化后的对象
     */
    public static <T> T deserialize(byte[] data, Class<T> type, String format) {
        return get(format).deserialize(data, type);
    }

    /**
     * Deserializes string to an object using the default serializer.
     * 使用默认序列化器将字符串反序列化为对象。
     *
     * @param data the serialized string - 序列化的字符串
     * @param type the target class - 目标类
     * @param <T>  the target type - 目标类型
     * @return the deserialized object - 反序列化后的对象
     */
    public static <T> T deserialize(String data, Class<T> type) {
        return deserialize(data.getBytes(StandardCharsets.UTF_8), type);
    }

    /**
     * Deserializes string to an object using the specified format.
     * 使用指定格式将字符串反序列化为对象。
     *
     * @param data   the serialized string - 序列化的字符串
     * @param type   the target class - 目标类
     * @param format the format name - 格式名称
     * @param <T>    the target type - 目标类型
     * @return the deserialized object - 反序列化后的对象
     */
    public static <T> T deserialize(String data, Class<T> type, String format) {
        return deserialize(data.getBytes(StandardCharsets.UTF_8), type, format);
    }

    /**
     * Deserializes byte array to a generic type using the default serializer.
     * 使用默认序列化器将字节数组反序列化为泛型类型。
     *
     * @param data    the serialized data - 序列化的数据
     * @param typeRef the type reference - 类型引用
     * @param <T>     the target type - 目标类型
     * @return the deserialized object - 反序列化后的对象
     */
    public static <T> T deserialize(byte[] data, TypeReference<T> typeRef) {
        return defaultSerializer.deserialize(data, typeRef);
    }

    /**
     * Deserializes string to a generic type using the default serializer.
     * 使用默认序列化器将字符串反序列化为泛型类型。
     *
     * @param data    the serialized string - 序列化的字符串
     * @param typeRef the type reference - 类型引用
     * @param <T>     the target type - 目标类型
     * @return the deserialized object - 反序列化后的对象
     */
    public static <T> T deserialize(String data, TypeReference<T> typeRef) {
        return deserialize(data.getBytes(StandardCharsets.UTF_8), typeRef);
    }

    /**
     * Deserializes byte array to a generic type using the specified format.
     * 使用指定格式将字节数组反序列化为泛型类型。
     *
     * @param data    the serialized data - 序列化的数据
     * @param typeRef the type reference - 类型引用
     * @param format  the format name - 格式名称
     * @param <T>     the target type - 目标类型
     * @return the deserialized object - 反序列化后的对象
     */
    public static <T> T deserialize(byte[] data, TypeReference<T> typeRef, String format) {
        return get(format).deserialize(data, typeRef);
    }

    // ==================== Convenience Deserialization | 便捷反序列化 ====================

    /**
     * Deserializes byte array to a List.
     * 将字节数组反序列化为 List。
     *
     * @param data        the serialized data - 序列化的数据
     * @param elementType the element type - 元素类型
     * @param <T>         the element type - 元素类型
     * @return the deserialized list - 反序列化后的列表
     */
    public static <T> List<T> deserializeList(byte[] data, Class<T> elementType) {
        return deserialize(data, TypeReference.listOf(elementType));
    }

    /**
     * Deserializes string to a List.
     * 将字符串反序列化为 List。
     *
     * @param data        the serialized string - 序列化的字符串
     * @param elementType the element type - 元素类型
     * @param <T>         the element type - 元素类型
     * @return the deserialized list - 反序列化后的列表
     */
    public static <T> List<T> deserializeList(String data, Class<T> elementType) {
        return deserializeList(data.getBytes(StandardCharsets.UTF_8), elementType);
    }

    /**
     * Deserializes byte array to a Set.
     * 将字节数组反序列化为 Set。
     *
     * @param data        the serialized data - 序列化的数据
     * @param elementType the element type - 元素类型
     * @param <T>         the element type - 元素类型
     * @return the deserialized set - 反序列化后的集合
     */
    public static <T> Set<T> deserializeSet(byte[] data, Class<T> elementType) {
        return deserialize(data, TypeReference.setOf(elementType));
    }

    /**
     * Deserializes byte array to a Map.
     * 将字节数组反序列化为 Map。
     *
     * @param data      the serialized data - 序列化的数据
     * @param keyType   the key type - 键类型
     * @param valueType the value type - 值类型
     * @param <K>       the key type - 键类型
     * @param <V>       the value type - 值类型
     * @return the deserialized map - 反序列化后的 Map
     */
    public static <K, V> Map<K, V> deserializeMap(byte[] data, Class<K> keyType, Class<V> valueType) {
        return deserialize(data, TypeReference.mapOf(keyType, valueType));
    }

    /**
     * Deserializes string to a Map.
     * 将字符串反序列化为 Map。
     *
     * @param data      the serialized string - 序列化的字符串
     * @param keyType   the key type - 键类型
     * @param valueType the value type - 值类型
     * @param <K>       the key type - 键类型
     * @param <V>       the value type - 值类型
     * @return the deserialized map - 反序列化后的 Map
     */
    public static <K, V> Map<K, V> deserializeMap(String data, Class<K> keyType, Class<V> valueType) {
        return deserializeMap(data.getBytes(StandardCharsets.UTF_8), keyType, valueType);
    }

    // ==================== Utility Methods | 实用方法 ====================

    /**
     * Creates a deep copy of an object.
     * 创建对象的深拷贝。
     *
     * <p>This method delegates to OpenClone for optimal performance if the deepclone module
     * is available. Otherwise, it falls back to serialization-based deep copy.</p>
     * <p>如果 deepclone 模块可用，此方法会委托给 OpenClone 以获得最佳性能。
     * 否则，它会降级到基于序列化的深拷贝。</p>
     *
     * @param obj the object to copy - 要拷贝的对象
     * @param <T> the object type - 对象类型
     * @return the deep copy - 深拷贝
     */
    @SuppressWarnings("unchecked")
    public static <T> T deepCopy(T obj) {
        if (obj == null) {
            return null;
        }

        // Prefer OpenClone if available (higher performance, handles circular references)
        if (DEEP_CLONE_HANDLE != null) {
            try {
                return (T) DEEP_CLONE_HANDLE.invoke(obj);
            } catch (Throwable e) {
                // Fall back to serialization if OpenClone fails
                // (e.g., object not supported by reflective cloning)
                LOG.log(System.Logger.Level.WARNING, "OpenClone.clone() failed for " + obj.getClass().getSimpleName() + ", falling back to serialization", e);
            }
        }

        // Fallback: use serialization-based deep copy
        byte[] data = serialize(obj);
        return (T) deserialize(data, obj.getClass());
    }

    /**
     * Creates a deep copy of an object using the specified serializer.
     * 使用指定的序列化器创建对象的深拷贝。
     *
     * @param obj    the object to copy - 要拷贝的对象
     * @param format the format name - 格式名称
     * @param <T>    the object type - 对象类型
     * @return the deep copy - 深拷贝
     */
    @SuppressWarnings("unchecked")
    public static <T> T deepCopy(T obj, String format) {
        if (obj == null) {
            return null;
        }
        byte[] data = serialize(obj, format);
        return (T) deserialize(data, obj.getClass(), format);
    }

    /**
     * Converts an object to another type using serialization.
     * 使用序列化将对象转换为另一种类型。
     *
     * @param source     the source object - 源对象
     * @param targetType the target type - 目标类型
     * @param <T>        the target type - 目标类型
     * @return the converted object - 转换后的对象
     */
    public static <T> T convert(Object source, Class<T> targetType) {
        if (source == null) {
            return null;
        }
        byte[] data = serialize(source);
        return deserialize(data, targetType);
    }

    /**
     * Converts an object to a generic type using serialization.
     * 使用序列化将对象转换为泛型类型。
     *
     * @param source  the source object - 源对象
     * @param typeRef the target type reference - 目标类型引用
     * @param <T>     the target type - 目标类型
     * @return the converted object - 转换后的对象
     */
    public static <T> T convert(Object source, TypeReference<T> typeRef) {
        if (source == null) {
            return null;
        }
        byte[] data = serialize(source);
        return deserialize(data, typeRef);
    }

    /**
     * Converts an object to another type using the specified format.
     * 使用指定格式将对象转换为另一种类型。
     *
     * @param source     the source object - 源对象
     * @param targetType the target type - 目标类型
     * @param format     the format name - 格式名称
     * @param <T>        the target type - 目标类型
     * @return the converted object - 转换后的对象
     */
    public static <T> T convert(Object source, Class<T> targetType, String format) {
        if (source == null) {
            return null;
        }
        byte[] data = serialize(source, format);
        return deserialize(data, targetType, format);
    }
}
