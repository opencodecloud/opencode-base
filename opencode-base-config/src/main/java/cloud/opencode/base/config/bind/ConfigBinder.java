package cloud.opencode.base.config.bind;

import cloud.opencode.base.config.Config;
import cloud.opencode.base.config.OpenConfigException;
import cloud.opencode.base.config.converter.ConverterRegistry;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.regex.Pattern;

/**
 * Configuration Binder
 * 配置绑定器
 *
 * <p>Binds configuration properties to POJOs using reflection with support for
 * nested configurations and type conversion.</p>
 * <p>使用反射将配置属性绑定到POJO,支持嵌套配置和类型转换。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>POJO binding with reflection - 使用反射的POJO绑定</li>
 *   <li>Nested configuration support - 嵌套配置支持</li>
 *   <li>Automatic type conversion - 自动类型转换</li>
 *   <li>Field name to key conversion - 字段名到键的转换</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * @ConfigProperties(prefix = "database")
 * public class DatabaseConfig {
 *     private String url;
 *     private String username;
 *     private int maxPoolSize;
 *
 *     @NestedConfig
 *     private PoolConfig pool;
 *
 *     // getters and setters
 * }
 *
 * ConfigBinder binder = new ConfigBinder(config, converters);
 * DatabaseConfig dbConfig = binder.bind("database", DatabaseConfig.class);
 * }</pre>
 *
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Partial (validates inputs) - 空值安全: 部分（验证输入）</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
public class ConfigBinder {

    private static final Pattern CAMEL_CASE_BOUNDARY_PATTERN = Pattern.compile("([a-z])([A-Z])");

    private final Config config;
    private final ConverterRegistry converters;

    public ConfigBinder(Config config, ConverterRegistry converters) {
        this.config = config;
        this.converters = converters;
    }

    /**
     * Bind configuration to new instance
     * 绑定配置到新实例
     *
     * @param <T> target type | 目标类型
     * @param prefix configuration prefix | 配置前缀
     * @param type target class | 目标类
     * @return bound instance | 绑定的实例
     */
    public <T> T bind(String prefix, Class<T> type) {
        try {
            T instance = type.getDeclaredConstructor().newInstance();
            bindTo(prefix, instance);
            return instance;
        } catch (Exception e) {
            throw OpenConfigException.bindFailed(prefix, type, e);
        }
    }

    /**
     * Bind configuration to existing instance
     * 绑定配置到现有实例
     *
     * @param <T> target type | 目标类型
     * @param prefix configuration prefix | 配置前缀
     * @param target target instance | 目标实例
     */
    public <T> void bindTo(String prefix, T target) {
        Class<?> type = target.getClass();

        for (Field field : type.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            field.setAccessible(true);
            String key = buildKey(prefix, field.getName());

            try {
                if (field.isAnnotationPresent(NestedConfig.class)) {
                    bindNestedField(prefix, field, target);
                } else {
                    bindSimpleField(key, field, target);
                }
            } catch (Exception e) {
                throw OpenConfigException.fieldBindFailed(field.getName(), e);
            }
        }
    }

    private void bindSimpleField(String key, Field field, Object target) throws Exception {
        if (!config.hasKey(key)) {
            // Apply @DefaultValue if present on the field
            DefaultValue defaultValue = field.getAnnotation(DefaultValue.class);
            if (defaultValue != null) {
                Object converted = converters.convert(defaultValue.value(), field.getType());
                field.set(target, converted);
            }
            return;
        }

        String value = config.getString(key);
        Object converted = converters.convert(value, field.getType());
        field.set(target, converted);
    }

    private void bindNestedField(String prefix, Field field, Object target) throws Exception {
        NestedConfig nested = field.getAnnotation(NestedConfig.class);
        String nestedPrefix = nested.prefix().isEmpty() ?
            buildKey(prefix, field.getName()) : nested.prefix();

        Object nestedInstance = bind(nestedPrefix, field.getType());
        field.set(target, nestedInstance);
    }

    private String buildKey(String prefix, String fieldName) {
        if (prefix == null || prefix.isEmpty()) {
            return toKebabCase(fieldName);
        }
        return prefix + "." + toKebabCase(fieldName);
    }

    private String toKebabCase(String camelCase) {
        return CAMEL_CASE_BOUNDARY_PATTERN.matcher(camelCase).replaceAll("$1-$2").toLowerCase();
    }
}
