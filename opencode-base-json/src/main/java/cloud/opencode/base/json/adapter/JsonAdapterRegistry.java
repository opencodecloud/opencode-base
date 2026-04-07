
package cloud.opencode.base.json.adapter;

import cloud.opencode.base.json.JsonNode;

import java.lang.reflect.Type;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JSON Adapter Registry - Registry for Custom Type Adapters
 * JSON 适配器注册表 - 自定义类型适配器的注册表
 *
 * <p>This class manages registration and lookup of custom type adapters
 * for JSON serialization and deserialization.</p>
 * <p>此类管理 JSON 序列化和反序列化的自定义类型适配器的注册和查找。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * // Register a custom adapter
 * JsonAdapterRegistry.register(new MoneyAdapter());
 *
 * // Get adapter for a type
 * JsonTypeAdapter<Money> adapter = JsonAdapterRegistry.getAdapter(Money.class);
 *
 * // Use adapter
 * JsonNode json = adapter.toJson(new Money(100, "USD"));
 * Money money = adapter.fromJson(json);
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Global and isolated adapter registries - 全局和隔离的适配器注册表</li>
 *   <li>Built-in adapters for common Java types - 常见Java类型的内置适配器</li>
 *   <li>Factory pattern for dynamic adapter creation - 动态适配器创建的工厂模式</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Partial (validates inputs) - 空值安全: 部分（验证输入）</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
public final class JsonAdapterRegistry {

    /**
     * Global adapter registry
     * 全局适配器注册表
     */
    private static final Map<Type, JsonTypeAdapter<?>> ADAPTERS = new ConcurrentHashMap<>();

    /**
     * Adapter factory registry for dynamic adapter creation
     * 动态适配器创建的适配器工厂注册表
     */
    private static final List<AdapterFactory> FACTORIES = new java.util.concurrent.CopyOnWriteArrayList<>();

    static {
        // Register built-in adapters for common types
        registerBuiltInAdapters();
    }

    private JsonAdapterRegistry() {
        // Utility class
    }

    /**
     * Registers a type adapter.
     * 注册类型适配器。
     *
     * @param adapter the adapter to register - 要注册的适配器
     * @param <T>     the type - 类型
     */
    public static <T> void register(JsonTypeAdapter<T> adapter) {
        Objects.requireNonNull(adapter, "Adapter must not be null");
        ADAPTERS.put(adapter.getType(), adapter);
        if (adapter.getGenericType() != null && !adapter.getGenericType().equals(adapter.getType())) {
            ADAPTERS.put(adapter.getGenericType(), adapter);
        }
    }

    /**
     * Registers an adapter for a specific type.
     * 为特定类型注册适配器。
     *
     * @param type    the type - 类型
     * @param adapter the adapter - 适配器
     * @param <T>     the type - 类型
     */
    public static <T> void register(Class<T> type, JsonTypeAdapter<T> adapter) {
        Objects.requireNonNull(type, "Type must not be null");
        Objects.requireNonNull(adapter, "Adapter must not be null");
        ADAPTERS.put(type, adapter);
    }

    /**
     * Registers an adapter factory.
     * 注册适配器工厂。
     *
     * @param factory the factory - 工厂
     */
    public static void registerFactory(AdapterFactory factory) {
        Objects.requireNonNull(factory, "Factory must not be null");
        FACTORIES.add(factory);
    }

    /**
     * Gets an adapter for a type.
     * 获取类型的适配器。
     *
     * @param type the type - 类型
     * @param <T>  the type - 类型
     * @return the adapter, or null if not found - 适配器，如果未找到则返回 null
     */
    @SuppressWarnings("unchecked")
    public static <T> JsonTypeAdapter<T> getAdapter(Class<T> type) {
        return (JsonTypeAdapter<T>) getAdapter((Type) type);
    }

    /**
     * Gets an adapter for a generic type.
     * 获取泛型类型的适配器。
     *
     * @param type the type - 类型
     * @return the adapter, or null if not found - 适配器，如果未找到则返回 null
     */
    public static JsonTypeAdapter<?> getAdapter(Type type) {
        // Check direct registration
        JsonTypeAdapter<?> adapter = ADAPTERS.get(type);
        if (adapter != null) {
            return adapter;
        }

        // Try factories
        for (AdapterFactory factory : FACTORIES) {
            adapter = factory.create(type);
            if (adapter != null) {
                // Cache the created adapter
                ADAPTERS.put(type, adapter);
                return adapter;
            }
        }

        return null;
    }

    /**
     * Checks if an adapter exists for a type.
     * 检查类型是否存在适配器。
     *
     * @param type the type - 类型
     * @return true if adapter exists - 如果存在适配器则返回 true
     */
    public static boolean hasAdapter(Class<?> type) {
        return getAdapter(type) != null;
    }

    /**
     * Unregisters an adapter for a type.
     * 注销类型的适配器。
     *
     * @param type the type - 类型
     * @return the removed adapter, or null - 移除的适配器，或 null
     */
    public static JsonTypeAdapter<?> unregister(Class<?> type) {
        return ADAPTERS.remove(type);
    }

    /**
     * Returns all registered types.
     * 返回所有注册的类型。
     *
     * @return set of registered types - 注册类型的集合
     */
    public static Set<Type> getRegisteredTypes() {
        return Collections.unmodifiableSet(ADAPTERS.keySet());
    }

    /**
     * Clears all custom adapters (keeps built-in).
     * 清除所有自定义适配器（保留内置）。
     */
    public static void clear() {
        ADAPTERS.clear();
        FACTORIES.clear();
        registerBuiltInAdapters();
    }

    /**
     * Adapter factory interface for dynamic adapter creation.
     * 动态适配器创建的适配器工厂接口。
     */
    @FunctionalInterface
    public interface AdapterFactory {
        /**
         * Creates an adapter for the given type.
         * 为给定类型创建适配器。
         *
         * @param type the type - 类型
         * @return the adapter, or null if not supported - 适配器，如果不支持则返回 null
         */
        JsonTypeAdapter<?> create(Type type);
    }

    /**
     * Creates a new isolated registry.
     * 创建新的隔离注册表。
     *
     * @return a new registry instance - 新注册表实例
     */
    public static Registry createRegistry() {
        return new Registry();
    }

    /**
     * Isolated adapter registry instance.
     * 隔离的适配器注册表实例。
     */
    public static final class Registry {
        private final Map<Type, JsonTypeAdapter<?>> adapters = new ConcurrentHashMap<>();
        private final List<AdapterFactory> factories = Collections.synchronizedList(new ArrayList<>());

        public <T> void register(JsonTypeAdapter<T> adapter) {
            Objects.requireNonNull(adapter, "Adapter must not be null");
            adapters.put(adapter.getType(), adapter);
        }

        public <T> void register(Class<T> type, JsonTypeAdapter<T> adapter) {
            Objects.requireNonNull(type, "Type must not be null");
            Objects.requireNonNull(adapter, "Adapter must not be null");
            adapters.put(type, adapter);
        }

        public void registerFactory(AdapterFactory factory) {
            Objects.requireNonNull(factory, "Factory must not be null");
            factories.add(factory);
        }

        @SuppressWarnings("unchecked")
        public <T> JsonTypeAdapter<T> getAdapter(Class<T> type) {
            JsonTypeAdapter<?> adapter = adapters.get(type);
            if (adapter != null) {
                return (JsonTypeAdapter<T>) adapter;
            }
            synchronized (factories) {
                for (AdapterFactory factory : factories) {
                    adapter = factory.create(type);
                    if (adapter != null) {
                        adapters.put(type, adapter);
                        return (JsonTypeAdapter<T>) adapter;
                    }
                }
            }
            // Fallback to global registry
            return JsonAdapterRegistry.getAdapter(type);
        }
    }

    // ==================== Built-in Adapters ====================

    private static void registerBuiltInAdapters() {
        // Date/Time adapters
        register(JsonTypeAdapter.ofString(LocalDate.class,
                LocalDate::toString,
                LocalDate::parse));

        register(JsonTypeAdapter.ofString(LocalTime.class,
                LocalTime::toString,
                LocalTime::parse));

        register(JsonTypeAdapter.ofString(LocalDateTime.class,
                LocalDateTime::toString,
                LocalDateTime::parse));

        register(JsonTypeAdapter.ofString(Instant.class,
                Instant::toString,
                Instant::parse));

        register(JsonTypeAdapter.ofString(ZonedDateTime.class,
                ZonedDateTime::toString,
                ZonedDateTime::parse));

        register(JsonTypeAdapter.ofString(OffsetDateTime.class,
                OffsetDateTime::toString,
                OffsetDateTime::parse));

        register(JsonTypeAdapter.ofString(Duration.class,
                Duration::toString,
                Duration::parse));

        register(JsonTypeAdapter.ofString(Period.class,
                Period::toString,
                Period::parse));

        // UUID adapter
        register(JsonTypeAdapter.ofString(UUID.class,
                UUID::toString,
                UUID::fromString));

        // Currency adapter
        register(JsonTypeAdapter.ofString(Currency.class,
                Currency::getCurrencyCode,
                Currency::getInstance));

        // Locale adapter
        register(JsonTypeAdapter.ofString(Locale.class,
                Locale::toLanguageTag,
                Locale::forLanguageTag));

        // TimeZone adapter
        register(JsonTypeAdapter.ofString(TimeZone.class,
                TimeZone::getID,
                TimeZone::getTimeZone));

        // ZoneId adapter
        register(JsonTypeAdapter.ofString(ZoneId.class,
                ZoneId::getId,
                ZoneId::of));

        // Optional adapter factory
        registerFactory(type -> {
            if (type instanceof java.lang.reflect.ParameterizedType pt) {
                if (pt.getRawType() == Optional.class) {
                    return createOptionalAdapter(pt.getActualTypeArguments()[0]);
                }
            }
            return null;
        });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static JsonTypeAdapter<?> createOptionalAdapter(Type elementType) {
        return new JsonTypeAdapter<Optional>() {
            @Override
            public Class<Optional> getType() {
                return Optional.class;
            }

            @Override
            public JsonNode toJson(Optional value) {
                if (value == null || value.isEmpty()) {
                    return JsonNode.nullNode();
                }
                Object element = value.get();
                JsonTypeAdapter adapter = getAdapter(element.getClass());
                if (adapter != null) {
                    return adapter.toJson(element);
                }
                return JsonNode.of(element);
            }

            @Override
            public Optional fromJson(JsonNode node) {
                if (node == null || node.isNull()) {
                    return Optional.empty();
                }
                if (elementType instanceof Class<?> clazz) {
                    JsonTypeAdapter adapter = getAdapter(clazz);
                    if (adapter != null) {
                        return Optional.ofNullable(adapter.fromJson(node));
                    }
                }
                return Optional.of(node);
            }

            @Override
            public boolean handlesNull() {
                return true;
            }
        };
    }
}
