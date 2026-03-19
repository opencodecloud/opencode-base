package cloud.opencode.base.config.internal;

import cloud.opencode.base.config.*;
import cloud.opencode.base.config.bind.ConfigBinder;
import cloud.opencode.base.config.converter.ConverterRegistry;
import cloud.opencode.base.config.placeholder.PlaceholderResolver;
import cloud.opencode.base.config.source.CompositeConfigSource;
import cloud.opencode.base.config.source.ConfigSource;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Default Configuration Implementation
 * 默认配置实现
 *
 * <p>Thread-safe implementation of Config interface with support for hot reload,
 * placeholder resolution, and type conversion.</p>
 * <p>Config接口的线程安全实现,支持热重载、占位符解析和类型转换。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Thread-safe configuration access - 线程安全的配置访问</li>
 *   <li>Placeholder resolution - 占位符解析</li>
 *   <li>Type conversion - 类型转换</li>
 *   <li>Hot reload support - 热重载支持</li>
 *   <li>Change listeners - 变更监听器</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Immutable snapshots - 不可变快照</li>
 * </ul>
 *
 *
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // See class-level documentation for usage
 * // 参见类级文档了解用法
 * }</pre>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
public class DefaultConfig implements Config, AutoCloseable {

    private static final System.Logger LOG = System.getLogger(DefaultConfig.class.getName());

    private final CompositeConfigSource source;
    private final ConverterRegistry converters;
    private final List<ConfigListener> globalListeners = new CopyOnWriteArrayList<>();
    private final Map<String, List<ConfigListener>> keyListeners = new ConcurrentHashMap<>();
    private volatile PlaceholderResolver placeholderResolver;
    private ConfigWatcher watcher;

    public DefaultConfig(CompositeConfigSource source, ConverterRegistry converters) {
        this.source = source;
        this.converters = converters;
    }

    // ============ Basic Retrieval ============

    @Override
    public String getString(String key) {
        String value = source.getProperty(key);
        if (value == null) {
            throw OpenConfigException.keyNotFound(key);
        }
        return resolvePlaceholders(value);
    }

    @Override
    public String getString(String key, String defaultValue) {
        String value = source.getProperty(key);
        return value != null ? resolvePlaceholders(value) : defaultValue;
    }

    @Override
    public int getInt(String key) {
        return get(key, int.class);
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return get(key, int.class, defaultValue);
    }

    @Override
    public long getLong(String key) {
        return get(key, long.class);
    }

    @Override
    public long getLong(String key, long defaultValue) {
        return get(key, long.class, defaultValue);
    }

    @Override
    public double getDouble(String key) {
        return get(key, double.class);
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        return get(key, double.class, defaultValue);
    }

    @Override
    public boolean getBoolean(String key) {
        return get(key, boolean.class);
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return get(key, boolean.class, defaultValue);
    }

    @Override
    public Duration getDuration(String key) {
        return get(key, Duration.class);
    }

    @Override
    public Duration getDuration(String key, Duration defaultValue) {
        return get(key, Duration.class, defaultValue);
    }

    // ============ Generic Retrieval ============

    @Override
    public <T> T get(String key, Class<T> type) {
        String value = getString(key);
        return converters.convert(value, type);
    }

    @Override
    public <T> T get(String key, Class<T> type, T defaultValue) {
        String value = source.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        value = resolvePlaceholders(value);
        return converters.convert(value, type);
    }

    @Override
    public <T> List<T> getList(String key, Class<T> elementType) {
        String value = getString(key);
        return Arrays.stream(value.split(","))
            .map(String::trim)
            .map(v -> converters.convert(v, elementType))
            .toList();
    }

    @Override
    public <K, V> Map<K, V> getMap(String key, Class<K> keyType, Class<V> valueType) {
        Map<K, V> result = new HashMap<>();
        Map<String, String> subProps = getByPrefix(key + ".");

        for (Map.Entry<String, String> entry : subProps.entrySet()) {
            K k = converters.convert(entry.getKey(), keyType);
            V v = converters.convert(entry.getValue(), valueType);
            result.put(k, v);
        }

        return result;
    }

    // ============ Optional Retrieval ============

    @Override
    public Optional<String> getOptional(String key) {
        String value = source.getProperty(key);
        return Optional.ofNullable(value).map(this::resolvePlaceholders);
    }

    @Override
    public <T> Optional<T> getOptional(String key, Class<T> type) {
        return getOptional(key).map(v -> converters.convert(v, type));
    }

    // ============ Sub-configuration ============

    @Override
    public Config getSubConfig(String prefix) {
        Map<String, String> subProps = getByPrefix(prefix + ".");
        Map<String, String> withoutPrefix = subProps.entrySet().stream()
            .collect(Collectors.toMap(
                e -> e.getKey().substring(prefix.length() + 1),
                Map.Entry::getValue
            ));

        return new SubConfig(this, prefix, withoutPrefix);
    }

    @Override
    public Map<String, String> getByPrefix(String prefix) {
        return source.getProperties().entrySet().stream()
            .filter(e -> e.getKey().startsWith(prefix))
            .collect(Collectors.toUnmodifiableMap(
                Map.Entry::getKey,
                e -> resolvePlaceholders(e.getValue())
            ));
    }

    // ============ Configuration Checks ============

    @Override
    public boolean hasKey(String key) {
        return source.getProperty(key) != null;
    }

    @Override
    public Set<String> getKeys() {
        return source.getProperties().keySet();
    }

    // ============ Listeners ============

    @Override
    public void addListener(ConfigListener listener) {
        globalListeners.add(listener);
    }

    @Override
    public void addListener(String key, ConfigListener listener) {
        keyListeners.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>())
            .add(listener);
    }

    @Override
    public void removeListener(ConfigListener listener) {
        globalListeners.remove(listener);
        keyListeners.values().forEach(list -> list.remove(listener));
        // Clean up empty listener lists to prevent memory leak
        keyListeners.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    // ============ Configuration Binding ============

    @Override
    public <T> T bind(String prefix, Class<T> type) {
        ConfigBinder binder = new ConfigBinder(this, converters);
        return binder.bind(prefix, type);
    }

    @Override
    public <T> void bindTo(String prefix, T target) {
        ConfigBinder binder = new ConfigBinder(this, converters);
        binder.bindTo(prefix, target);
    }

    // ============ Features ============

    public void enablePlaceholderResolution() {
        this.placeholderResolver = new PlaceholderResolver(source::getProperty);
    }

    public void enableHotReload(Duration interval) {
        this.watcher = new ConfigWatcher(interval);
        this.watcher.addListener(this::notifyListeners);
        this.watcher.watch(source);
        this.watcher.start();
    }

    @Override
    public void close() {
        if (watcher != null) {
            watcher.close();
        }
    }

    private String resolvePlaceholders(String value) {
        return placeholderResolver != null ? placeholderResolver.resolve(value) : value;
    }

    private void notifyListeners(ConfigChangeEvent event) {
        // Global listeners
        globalListeners.forEach(listener -> {
            try {
                listener.onConfigChange(event);
            } catch (Exception e) {
                LOG.log(System.Logger.Level.WARNING, "Config listener threw exception for key '" + event.key() + "'", e);
            }
        });

        // Key-specific listeners
        List<ConfigListener> listeners = keyListeners.get(event.key());
        if (listeners != null) {
            listeners.forEach(listener -> {
                try {
                    listener.onConfigChange(event);
                } catch (Exception e) {
                    LOG.log(System.Logger.Level.WARNING, "Key-specific config listener threw exception for key '" + event.key() + "'", e);
                }
            });
        }
    }

    // ============ Sub-configuration Implementation ============

    private static class SubConfig implements Config {
        private final Config parent;
        private final String prefix;
        private final Map<String, String> properties;

        SubConfig(Config parent, String prefix, Map<String, String> properties) {
            this.parent = parent;
            this.prefix = prefix;
            this.properties = properties;
        }

        @Override
        public String getString(String key) {
            String fullKey = prefix + "." + key;
            return parent.getString(fullKey);
        }

        @Override
        public String getString(String key, String defaultValue) {
            String fullKey = prefix + "." + key;
            return parent.getString(fullKey, defaultValue);
        }

        @Override
        public int getInt(String key) {
            return parent.getInt(prefix + "." + key);
        }

        @Override
        public int getInt(String key, int defaultValue) {
            return parent.getInt(prefix + "." + key, defaultValue);
        }

        @Override
        public long getLong(String key) {
            return parent.getLong(prefix + "." + key);
        }

        @Override
        public long getLong(String key, long defaultValue) {
            return parent.getLong(prefix + "." + key, defaultValue);
        }

        @Override
        public double getDouble(String key) {
            return parent.getDouble(prefix + "." + key);
        }

        @Override
        public double getDouble(String key, double defaultValue) {
            return parent.getDouble(prefix + "." + key, defaultValue);
        }

        @Override
        public boolean getBoolean(String key) {
            return parent.getBoolean(prefix + "." + key);
        }

        @Override
        public boolean getBoolean(String key, boolean defaultValue) {
            return parent.getBoolean(prefix + "." + key, defaultValue);
        }

        @Override
        public Duration getDuration(String key) {
            return parent.getDuration(prefix + "." + key);
        }

        @Override
        public Duration getDuration(String key, Duration defaultValue) {
            return parent.getDuration(prefix + "." + key, defaultValue);
        }

        @Override
        public <T> T get(String key, Class<T> type) {
            return parent.get(prefix + "." + key, type);
        }

        @Override
        public <T> T get(String key, Class<T> type, T defaultValue) {
            return parent.get(prefix + "." + key, type, defaultValue);
        }

        @Override
        public <T> List<T> getList(String key, Class<T> elementType) {
            return parent.getList(prefix + "." + key, elementType);
        }

        @Override
        public <K, V> Map<K, V> getMap(String key, Class<K> keyType, Class<V> valueType) {
            return parent.getMap(prefix + "." + key, keyType, valueType);
        }

        @Override
        public Optional<String> getOptional(String key) {
            return parent.getOptional(prefix + "." + key);
        }

        @Override
        public <T> Optional<T> getOptional(String key, Class<T> type) {
            return parent.getOptional(prefix + "." + key, type);
        }

        @Override
        public Config getSubConfig(String subPrefix) {
            return parent.getSubConfig(prefix + "." + subPrefix);
        }

        @Override
        public Map<String, String> getByPrefix(String subPrefix) {
            return parent.getByPrefix(prefix + "." + subPrefix);
        }

        @Override
        public boolean hasKey(String key) {
            return parent.hasKey(prefix + "." + key);
        }

        @Override
        public Set<String> getKeys() {
            return properties.keySet();
        }

        @Override
        public void addListener(ConfigListener listener) {
            parent.addListener(listener);
        }

        @Override
        public void addListener(String key, ConfigListener listener) {
            parent.addListener(prefix + "." + key, listener);
        }

        @Override
        public void removeListener(ConfigListener listener) {
            parent.removeListener(listener);
        }

        @Override
        public <T> T bind(String subPrefix, Class<T> type) {
            return parent.bind(prefix + "." + subPrefix, type);
        }

        @Override
        public <T> void bindTo(String subPrefix, T target) {
            parent.bindTo(prefix + "." + subPrefix, target);
        }
    }
}
