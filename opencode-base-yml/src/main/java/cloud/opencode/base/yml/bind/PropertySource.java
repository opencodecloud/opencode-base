/*
 * Copyright 2025 OpenCode Cloud Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.opencode.base.yml.bind;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Property Source Interface - Abstract Source of Configuration Properties
 * 属性源接口 - 配置属性的抽象来源
 *
 * <p>Provides a unified interface for accessing configuration properties
 * from various sources (YAML, Maps, environment variables, etc.).</p>
 * <p>提供统一接口用于从各种来源（YAML、Map、环境变量等）
 * 访问配置属性。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Unified access to YAML, Map, environment, and system properties - 统一访问 YAML、Map、环境变量和系统属性</li>
 *   <li>Typed property retrieval with defaults - 带默认值的类型化属性获取</li>
 *   <li>Prefix-based property subsetting - 基于前缀的属性子集</li>
 *   <li>Optional value support - 可选值支持</li>
 * </ul>
 *
 * <p><strong>Usage Example | 使用示例:</strong></p>
 * <pre>{@code
 * // Create from YAML
 * PropertySource source = PropertySource.fromYaml(yamlContent);
 *
 * // Access properties
 * String host = source.getProperty("database.host");
 * int port = source.getProperty("database.port", Integer.class, 5432);
 *
 * // Bind to object
 * DatabaseConfig config = YmlBinder.bind(source, DatabaseConfig.class);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation (MapPropertySource is immutable) - 线程安全: 取决于实现（MapPropertySource 不可变）</li>
 *   <li>Null-safe: Yes (returns null for missing properties) - 空值安全: 是（缺失属性返回 null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see YmlBinder
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
public interface PropertySource {

    /**
     * Gets the property source name
     * 获取属性源名称
     *
     * @return source name | 源名称
     */
    String getName();

    /**
     * Gets a property value by path
     * 通过路径获取属性值
     *
     * @param path property path | 属性路径
     * @return property value or null | 属性值或 null
     */
    String getProperty(String path);

    /**
     * Gets a property value with default
     * 获取属性值（带默认值）
     *
     * @param path         property path | 属性路径
     * @param defaultValue default value | 默认值
     * @return property value or default | 属性值或默认值
     */
    default String getProperty(String path, String defaultValue) {
        String value = getProperty(path);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets a typed property value
     * 获取类型化的属性值
     *
     * @param path property path | 属性路径
     * @param type target type | 目标类型
     * @param <T>  type parameter | 类型参数
     * @return typed value or null | 类型化值或 null
     */
    <T> T getProperty(String path, Class<T> type);

    /**
     * Gets a typed property value with default
     * 获取类型化的属性值（带默认值）
     *
     * @param path         property path | 属性路径
     * @param type         target type | 目标类型
     * @param defaultValue default value | 默认值
     * @param <T>          type parameter | 类型参数
     * @return typed value or default | 类型化值或默认值
     */
    default <T> T getProperty(String path, Class<T> type, T defaultValue) {
        T value = getProperty(path, type);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets a property as Optional
     * 获取属性作为 Optional
     *
     * @param path property path | 属性路径
     * @return Optional containing value | 包含值的 Optional
     */
    default Optional<String> getOptionalProperty(String path) {
        return Optional.ofNullable(getProperty(path));
    }

    /**
     * Checks if a property exists
     * 检查属性是否存在
     *
     * @param path property path | 属性路径
     * @return true if exists | 如果存在返回 true
     */
    boolean containsProperty(String path);

    /**
     * Gets all property names
     * 获取所有属性名
     *
     * @return set of property names | 属性名集合
     */
    Set<String> getPropertyNames();

    /**
     * Gets properties as Map
     * 获取属性作为 Map
     *
     * @return property map | 属性映射
     */
    Map<String, Object> getProperties();

    /**
     * Gets properties under a prefix
     * 获取前缀下的属性
     *
     * @param prefix the prefix | 前缀
     * @return property map under prefix | 前缀下的属性映射
     */
    Map<String, Object> getProperties(String prefix);

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates a PropertySource from a Map
     * 从 Map 创建 PropertySource
     *
     * @param name       source name | 源名称
     * @param properties property map | 属性映射
     * @return property source | 属性源
     */
    static PropertySource fromMap(String name, Map<String, Object> properties) {
        return new MapPropertySource(name, properties);
    }

    /**
     * Creates a PropertySource from YAML content
     * 从 YAML 内容创建 PropertySource
     *
     * @param yamlContent YAML content | YAML 内容
     * @return property source | 属性源
     */
    static PropertySource fromYaml(String yamlContent) {
        return fromYaml("yaml", yamlContent);
    }

    /**
     * Creates a PropertySource from YAML content with name
     * 从 YAML 内容创建命名的 PropertySource
     *
     * @param name        source name | 源名称
     * @param yamlContent YAML content | YAML 内容
     * @return property source | 属性源
     */
    static PropertySource fromYaml(String name, String yamlContent) {
        return new YamlPropertySource(name, yamlContent);
    }

    /**
     * Creates a PropertySource from environment variables
     * 从环境变量创建 PropertySource
     *
     * @return property source | 属性源
     */
    static PropertySource fromEnvironment() {
        return new EnvironmentPropertySource();
    }

    /**
     * Creates a PropertySource from system properties
     * 从系统属性创建 PropertySource
     *
     * @return property source | 属性源
     */
    static PropertySource fromSystemProperties() {
        return new SystemPropertySource();
    }

    // ==================== Default Implementations | 默认实现 ====================

    /**
     * Map-based PropertySource implementation
     * 基于 Map 的 PropertySource 实现
     */
    class MapPropertySource implements PropertySource {
        private final String name;
        private final Map<String, Object> properties;

        public MapPropertySource(String name, Map<String, Object> properties) {
            this.name = name;
            this.properties = properties != null ? Map.copyOf(properties) : Map.of();
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getProperty(String path) {
            Object value = getNestedValue(path);
            return value != null ? value.toString() : null;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getProperty(String path, Class<T> type) {
            Object value = getNestedValue(path);
            if (value == null) return null;
            if (type.isInstance(value)) return type.cast(value);
            // Basic type conversion
            String str = value.toString();
            if (type == String.class) return type.cast(str);
            if (type == Integer.class || type == int.class) return type.cast(Integer.parseInt(str));
            if (type == Long.class || type == long.class) return type.cast(Long.parseLong(str));
            if (type == Boolean.class || type == boolean.class) return type.cast(Boolean.parseBoolean(str));
            if (type == Double.class || type == double.class) return type.cast(Double.parseDouble(str));
            return null;
        }

        @Override
        public boolean containsProperty(String path) {
            return getNestedValue(path) != null;
        }

        @Override
        public Set<String> getPropertyNames() {
            return properties.keySet();
        }

        @Override
        public Map<String, Object> getProperties() {
            return properties;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Map<String, Object> getProperties(String prefix) {
            Object value = getNestedValue(prefix);
            if (value instanceof Map) {
                return (Map<String, Object>) value;
            }
            return Map.of();
        }

        @SuppressWarnings("unchecked")
        private Object getNestedValue(String path) {
            if (path == null || path.isEmpty()) return null;
            String[] parts = path.split("\\.");
            Object current = properties;
            for (String part : parts) {
                if (current instanceof Map) {
                    current = ((Map<String, Object>) current).get(part);
                } else {
                    return null;
                }
            }
            return current;
        }
    }

    /**
     * YAML-based PropertySource implementation
     * 基于 YAML 的 PropertySource 实现
     */
    class YamlPropertySource extends MapPropertySource {
        public YamlPropertySource(String name, String yamlContent) {
            super(name, parseYaml(yamlContent));
        }

        @SuppressWarnings("unchecked")
        private static Map<String, Object> parseYaml(String yamlContent) {
            if (yamlContent == null || yamlContent.isBlank()) {
                return Map.of();
            }
            try {
                Object parsed = cloud.opencode.base.yml.OpenYml.load(yamlContent);
                if (parsed instanceof Map) {
                    return (Map<String, Object>) parsed;
                }
                return Map.of();
            } catch (Exception e) {
                return Map.of();
            }
        }
    }

    /**
     * Environment variable PropertySource
     * 环境变量 PropertySource
     */
    class EnvironmentPropertySource implements PropertySource {
        @Override
        public String getName() {
            return "environment";
        }

        @Override
        public String getProperty(String path) {
            // Convert dot notation to env var style: database.host -> DATABASE_HOST
            String envKey = path.replace('.', '_').toUpperCase();
            return System.getenv(envKey);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getProperty(String path, Class<T> type) {
            String value = getProperty(path);
            if (value == null) return null;
            if (type == String.class) return type.cast(value);
            if (type == Integer.class || type == int.class) return type.cast(Integer.parseInt(value));
            if (type == Long.class || type == long.class) return type.cast(Long.parseLong(value));
            if (type == Boolean.class || type == boolean.class) return type.cast(Boolean.parseBoolean(value));
            if (type == Double.class || type == double.class) return type.cast(Double.parseDouble(value));
            return null;
        }

        @Override
        public boolean containsProperty(String path) {
            return getProperty(path) != null;
        }

        @Override
        public Set<String> getPropertyNames() {
            return System.getenv().keySet();
        }

        @Override
        public Map<String, Object> getProperties() {
            return Map.copyOf(System.getenv());
        }

        @Override
        public Map<String, Object> getProperties(String prefix) {
            String envPrefix = prefix.replace('.', '_').toUpperCase() + "_";
            Map<String, Object> result = new java.util.HashMap<>();
            System.getenv().forEach((key, value) -> {
                if (key.startsWith(envPrefix)) {
                    result.put(key.substring(envPrefix.length()).toLowerCase(), value);
                }
            });
            return result;
        }
    }

    /**
     * System properties PropertySource
     * 系统属性 PropertySource
     */
    class SystemPropertySource implements PropertySource {
        @Override
        public String getName() {
            return "system";
        }

        @Override
        public String getProperty(String path) {
            return System.getProperty(path);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getProperty(String path, Class<T> type) {
            String value = getProperty(path);
            if (value == null) return null;
            if (type == String.class) return type.cast(value);
            if (type == Integer.class || type == int.class) return type.cast(Integer.parseInt(value));
            if (type == Long.class || type == long.class) return type.cast(Long.parseLong(value));
            if (type == Boolean.class || type == boolean.class) return type.cast(Boolean.parseBoolean(value));
            if (type == Double.class || type == double.class) return type.cast(Double.parseDouble(value));
            return null;
        }

        @Override
        public boolean containsProperty(String path) {
            return System.getProperty(path) != null;
        }

        @Override
        public Set<String> getPropertyNames() {
            return System.getProperties().stringPropertyNames();
        }

        @Override
        @SuppressWarnings("unchecked")
        public Map<String, Object> getProperties() {
            Map<String, Object> props = new java.util.HashMap<>();
            System.getProperties().forEach((k, v) -> props.put(k.toString(), v));
            return props;
        }

        @Override
        public Map<String, Object> getProperties(String prefix) {
            String dotPrefix = prefix.endsWith(".") ? prefix : prefix + ".";
            Map<String, Object> result = new java.util.HashMap<>();
            System.getProperties().forEach((k, v) -> {
                String key = k.toString();
                if (key.startsWith(dotPrefix)) {
                    result.put(key.substring(dotPrefix.length()), v);
                }
            });
            return result;
        }
    }
}
