package cloud.opencode.base.config.bind;

import cloud.opencode.base.config.Config;
import cloud.opencode.base.config.OpenConfigException;
import cloud.opencode.base.config.converter.ConverterRegistry;
import cloud.opencode.base.config.bind.DefaultValue;
import cloud.opencode.base.config.jdk25.Required;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.regex.Pattern;

/**
 * Record Configuration Binder for JDK 25 Records
 * JDK 25 Record的配置绑定器
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * record DatabaseConfig(
 *     @Required String url,
 *     @Required String username,
 *     @DefaultValue("10") int maxPoolSize
 * ) {}
 *
 * RecordConfigBinder binder = new RecordConfigBinder(config, converters);
 * DatabaseConfig dbConfig = binder.bind("database", DatabaseConfig.class);
 * }</pre>
 *
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Core RecordConfigBinder functionality - RecordConfigBinder核心功能</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: Partial (validates inputs) - 空值安全: 部分（验证输入）</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
public class RecordConfigBinder {

    private static final Pattern CAMEL_CASE_BOUNDARY_PATTERN = Pattern.compile("([a-z])([A-Z])");

    private final Config config;
    private final ConverterRegistry converters;

    public RecordConfigBinder(Config config, ConverterRegistry converters) {
        this.config = config;
        this.converters = converters;
    }

    public <T extends Record> T bind(String prefix, Class<T> recordType) {
        RecordComponent[] components = recordType.getRecordComponents();
        Object[] args = new Object[components.length];
        Class<?>[] paramTypes = new Class<?>[components.length];

        for (int i = 0; i < components.length; i++) {
            RecordComponent component = components[i];
            String key = buildKey(prefix, component.getName());
            paramTypes[i] = component.getType();

            if (component.getType().isRecord()) {
                @SuppressWarnings("unchecked")
                Class<? extends Record> nestedType = (Class<? extends Record>) component.getType();
                args[i] = bind(key, nestedType);
            } else {
                args[i] = bindValue(key, component);
            }
        }

        try {
            Constructor<T> constructor = recordType.getDeclaredConstructor(paramTypes);
            return constructor.newInstance(args);
        } catch (Exception e) {
            throw OpenConfigException.bindFailed(prefix, recordType, e);
        }
    }

    private Object bindValue(String key, RecordComponent component) {
        String value = config.getString(key, null);

        if (value == null) {
            // Check new annotation (bind.DefaultValue) first, then legacy (jdk25.DefaultValue)
            DefaultValue defaultValue = component.getAnnotation(DefaultValue.class);
            if (defaultValue != null) {
                value = defaultValue.value();
            } else {
                @SuppressWarnings("deprecation")
                cloud.opencode.base.config.jdk25.DefaultValue legacyDefault =
                        component.getAnnotation(cloud.opencode.base.config.jdk25.DefaultValue.class);
                if (legacyDefault != null) {
                    value = legacyDefault.value();
                }
            }
        }

        if (value == null) {
            if (component.isAnnotationPresent(Required.class)) {
                throw OpenConfigException.requiredKeyMissing(key);
            }
            return getDefaultForType(component.getType());
        }

        return converters.convert(value, component.getType());
    }

    private String buildKey(String prefix, String name) {
        if (prefix == null || prefix.isEmpty()) {
            return toKebabCase(name);
        }
        return prefix + "." + toKebabCase(name);
    }

    private String toKebabCase(String camelCase) {
        return CAMEL_CASE_BOUNDARY_PATTERN.matcher(camelCase).replaceAll("$1-$2").toLowerCase();
    }

    private Object getDefaultForType(Class<?> type) {
        if (type == int.class || type == Integer.class) return 0;
        if (type == long.class || type == Long.class) return 0L;
        if (type == double.class || type == Double.class) return 0.0;
        if (type == float.class || type == Float.class) return 0.0f;
        if (type == boolean.class || type == Boolean.class) return false;
        if (type == short.class || type == Short.class) return (short) 0;
        if (type == byte.class || type == Byte.class) return (byte) 0;
        if (type == char.class || type == Character.class) return '\0';
        return null;
    }
}
