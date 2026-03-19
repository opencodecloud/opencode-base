/*
 * Copyright 2025 OpenCode Cloud Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.opencode.base.yml.bind;

import cloud.opencode.base.yml.OpenYml;
import cloud.opencode.base.yml.exception.YmlBindException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Config Binder - Binds YAML properties to Java objects
 * 配置绑定器 - 将 YAML 属性绑定到 Java 对象
 *
 * <p>Provides flexible binding of YAML configuration to Java beans and records.</p>
 * <p>提供将 YAML 配置灵活绑定到 Java Bean 和 Record 的功能。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Bind YAML to Java beans and records - 将 YAML 绑定到 Java Bean 和 Record</li>
 *   <li>Prefix-based binding for nested configs - 基于前缀的嵌套配置绑定</li>
 *   <li>Automatic camelCase/kebab-case/snake_case name conversion - 自动驼峰/短横线/下划线命名转换</li>
 *   <li>PropertySource abstraction for multiple config sources - 多配置源的 PropertySource 抽象</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Bind YAML to bean
 * DatabaseConfig config = ConfigBinder.bind(yamlContent, DatabaseConfig.class);
 *
 * // Bind with prefix
 * ServerConfig server = ConfigBinder.bind(yamlContent, "server", ServerConfig.class);
 *
 * // Bind from PropertySource
 * PropertySource source = PropertySource.fromYaml(yamlContent);
 * AppConfig app = ConfigBinder.bind(source, AppConfig.class);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes (returns null/default for null input) - 空值安全: 是（空输入返回 null/默认值）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see PropertySource
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
public final class ConfigBinder {

    private static final Pattern CAMEL_CASE_BOUNDARY_PATTERN = Pattern.compile("([a-z])([A-Z])");
    private static final Pattern DOT_PATTERN = Pattern.compile("\\.");

    private ConfigBinder() {
    }

    // ==================== Bind from YAML String | 从 YAML 字符串绑定 ====================

    /**
     * Binds YAML content to a class
     * 将 YAML 内容绑定到类
     *
     * @param yamlContent YAML content | YAML 内容
     * @param clazz       target class | 目标类
     * @param <T>         type parameter | 类型参数
     * @return bound object | 绑定的对象
     */
    public static <T> T bind(String yamlContent, Class<T> clazz) {
        Map<String, Object> properties = OpenYml.load(yamlContent);
        return bind(properties, clazz);
    }

    /**
     * Binds YAML content with prefix to a class
     * 将带前缀的 YAML 内容绑定到类
     *
     * @param yamlContent YAML content | YAML 内容
     * @param prefix      property prefix | 属性前缀
     * @param clazz       target class | 目标类
     * @param <T>         type parameter | 类型参数
     * @return bound object | 绑定的对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T bind(String yamlContent, String prefix, Class<T> clazz) {
        Map<String, Object> properties = OpenYml.load(yamlContent);
        Map<String, Object> prefixedProperties = getNestedMap(properties, prefix);
        return bind(prefixedProperties, clazz);
    }

    // ==================== Bind from PropertySource | 从 PropertySource 绑定 ====================

    /**
     * Binds PropertySource to a class
     * 将 PropertySource 绑定到类
     *
     * @param source PropertySource | 属性源
     * @param clazz  target class | 目标类
     * @param <T>    type parameter | 类型参数
     * @return bound object | 绑定的对象
     */
    public static <T> T bind(PropertySource source, Class<T> clazz) {
        return bind(source.getProperties(), clazz);
    }

    /**
     * Binds PropertySource with prefix to a class
     * 将带前缀的 PropertySource 绑定到类
     *
     * @param source PropertySource | 属性源
     * @param prefix property prefix | 属性前缀
     * @param clazz  target class | 目标类
     * @param <T>    type parameter | 类型参数
     * @return bound object | 绑定的对象
     */
    public static <T> T bind(PropertySource source, String prefix, Class<T> clazz) {
        return bind(source.getProperties(prefix), clazz);
    }

    // ==================== Bind from Map | 从 Map 绑定 ====================

    /**
     * Binds a map to a class
     * 将 Map 绑定到类
     *
     * @param properties property map | 属性映射
     * @param clazz      target class | 目标类
     * @param <T>        type parameter | 类型参数
     * @return bound object | 绑定的对象
     */
    public static <T> T bind(Map<String, Object> properties, Class<T> clazz) {
        if (properties == null || properties.isEmpty()) {
            return createDefault(clazz);
        }

        try {
            if (clazz.isRecord()) {
                return bindToRecord(properties, clazz);
            } else {
                return bindToBean(properties, clazz);
            }
        } catch (Exception e) {
            throw new YmlBindException("Failed to bind properties to " + clazz.getName(), e);
        }
    }

    // ==================== Record Binding | Record 绑定 ====================

    @SuppressWarnings("unchecked")
    private static <T> T bindToRecord(Map<String, Object> properties, Class<T> clazz) throws Exception {
        RecordComponent[] components = clazz.getRecordComponents();
        Object[] args = new Object[components.length];
        Class<?>[] types = new Class<?>[components.length];

        for (int i = 0; i < components.length; i++) {
            RecordComponent component = components[i];
            String name = component.getName();
            Class<?> type = component.getType();
            types[i] = type;

            Object value = getPropertyValue(properties, name, type);
            args[i] = value;
        }

        Constructor<T> constructor = clazz.getDeclaredConstructor(types);
        constructor.setAccessible(true);
        return constructor.newInstance(args);
    }

    // ==================== Bean Binding | Bean 绑定 ====================

    private static <T> T bindToBean(Map<String, Object> properties, Class<T> clazz) throws Exception {
        T instance = clazz.getDeclaredConstructor().newInstance();

        for (Field field : getAllFields(clazz)) {
            String name = field.getName();
            Class<?> type = field.getType();

            Object value = getPropertyValue(properties, name, type);
            if (value != null) {
                field.setAccessible(true);
                field.set(instance, value);
            }
        }

        return instance;
    }

    // ==================== Value Conversion | 值转换 ====================

    @SuppressWarnings("unchecked")
    private static Object getPropertyValue(Map<String, Object> properties, String name, Class<?> type) {
        // Try exact name first, then camelCase to kebab-case
        Object value = properties.get(name);
        if (value == null) {
            value = properties.get(toKebabCase(name));
        }
        if (value == null) {
            value = properties.get(toSnakeCase(name));
        }

        if (value == null) {
            return getDefaultValue(type);
        }

        // Nested object binding
        if (value instanceof Map && !Map.class.isAssignableFrom(type)) {
            return bind((Map<String, Object>) value, type);
        }

        // Type conversion
        return convertValue(value, type);
    }

    @SuppressWarnings("unchecked")
    private static Object convertValue(Object value, Class<?> type) {
        if (value == null) {
            return getDefaultValue(type);
        }

        if (type.isInstance(value)) {
            return value;
        }

        String str = value.toString();

        // Primitive types
        if (type == String.class) return str;
        if (type == int.class || type == Integer.class) return Integer.parseInt(str);
        if (type == long.class || type == Long.class) return Long.parseLong(str);
        if (type == double.class || type == Double.class) return Double.parseDouble(str);
        if (type == float.class || type == Float.class) return Float.parseFloat(str);
        if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(str);
        if (type == short.class || type == Short.class) return Short.parseShort(str);
        if (type == byte.class || type == Byte.class) return Byte.parseByte(str);
        if (type == char.class || type == Character.class) return str.isEmpty() ? '\0' : str.charAt(0);

        // Collections
        if (List.class.isAssignableFrom(type) && value instanceof List) {
            return new ArrayList<>((List<?>) value);
        }
        if (Set.class.isAssignableFrom(type) && value instanceof List) {
            return new HashSet<>((List<?>) value);
        }
        if (Map.class.isAssignableFrom(type) && value instanceof Map) {
            return new HashMap<>((Map<?, ?>) value);
        }

        // Enum
        if (type.isEnum()) {
            return Enum.valueOf((Class<Enum>) type, str.toUpperCase());
        }

        return value;
    }

    // ==================== Utility Methods | 工具方法 ====================

    private static <T> T createDefault(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    private static Object getDefaultValue(Class<?> type) {
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == double.class) return 0.0;
        if (type == float.class) return 0.0f;
        if (type == boolean.class) return false;
        if (type == short.class) return (short) 0;
        if (type == byte.class) return (byte) 0;
        if (type == char.class) return '\0';
        return null;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getNestedMap(Map<String, Object> map, String path) {
        if (path == null || path.isEmpty()) {
            return map;
        }

        String[] parts = DOT_PATTERN.split(path);
        Object current = map;

        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(part);
            } else {
                return Map.of();
            }
        }

        if (current instanceof Map) {
            return (Map<String, Object>) current;
        }
        return Map.of();
    }

    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    private static String toKebabCase(String name) {
        return CAMEL_CASE_BOUNDARY_PATTERN.matcher(name).replaceAll("$1-$2").toLowerCase();
    }

    private static String toSnakeCase(String name) {
        return CAMEL_CASE_BOUNDARY_PATTERN.matcher(name).replaceAll("$1_$2").toLowerCase();
    }
}
