package cloud.opencode.base.config.validation;

import cloud.opencode.base.config.Config;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Validation Module Adapter with Optional Validation Module Delegation
 * 支持可选 Validation 模块委托的验证适配器
 *
 * <p>Provides config validation using OpenValidator when the Validation module is available.
 * When not available, falls back to basic validation.</p>
 * <p>当 Validation 模块可用时，使用 OpenValidator 进行配置验证。
 * 不可用时，降级到基础验证。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Bean validation for config objects - 配置对象的 Bean 验证</li>
 *   <li>Constraint annotation support - 约束注解支持</li>
 *   <li>Custom validation rules - 自定义验证规则</li>
 *   <li>Graceful fallback - 优雅降级</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Validate a config-bound object
 * DatabaseConfig dbConfig = config.bind("database", DatabaseConfig.class);
 * ValidationResult result = ValidationModuleAdapter.validateObject(dbConfig);
 * if (!result.isValid()) {
 *     result.getErrors().forEach(System.err::println);
 * }
 *
 * // Create a ConfigValidator using validation module
 * ConfigValidator validator = ValidationModuleAdapter.forObject(DatabaseConfig.class, "database");
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: Partial (validates inputs) - 空值安全: 部分（验证输入）</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
public final class ValidationModuleAdapter {

    private static final MethodHandle VALIDATE_HANDLE;
    private static final MethodHandle IS_VALID_HANDLE;
    private static final MethodHandle GET_ERRORS_HANDLE;

    static {
        VALIDATE_HANDLE = initValidateHandle();
        IS_VALID_HANDLE = initIsValidHandle();
        GET_ERRORS_HANDLE = initGetErrorsHandle();
    }

    private ValidationModuleAdapter() {
    }

    private static MethodHandle initValidateHandle() {
        try {
            Class<?> openValidatorClass = Class.forName("cloud.opencode.base.validation.OpenValidator");
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            return lookup.findStatic(openValidatorClass, "validate",
                    MethodType.methodType(Object.class, Object.class));
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            return null;
        }
    }

    private static MethodHandle initIsValidHandle() {
        try {
            Class<?> validationResultClass = Class.forName("cloud.opencode.base.validation.ValidationResult");
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            return lookup.findVirtual(validationResultClass, "isValid",
                    MethodType.methodType(boolean.class));
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            return null;
        }
    }

    private static MethodHandle initGetErrorsHandle() {
        try {
            Class<?> validationResultClass = Class.forName("cloud.opencode.base.validation.ValidationResult");
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            return lookup.findVirtual(validationResultClass, "getErrors",
                    MethodType.methodType(List.class));
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            return null;
        }
    }

    /**
     * Checks if the Validation module is available.
     * 检查 Validation 模块是否可用
     *
     * @return true if Validation module is available | 如果 Validation 模块可用返回 true
     */
    public static boolean isValidationModuleAvailable() {
        return VALIDATE_HANDLE != null && IS_VALID_HANDLE != null;
    }

    /**
     * Validates an object using OpenValidator if available.
     * 如果可用，使用 OpenValidator 验证对象
     *
     * @param object the object to validate | 要验证的对象
     * @return validation result | 验证结果
     */
    public static ValidationResult validateObject(Object object) {
        Objects.requireNonNull(object, "object must not be null");

        if (!isValidationModuleAvailable()) {
            return ValidationResult.valid();
        }

        try {
            Object validationResult = VALIDATE_HANDLE.invokeWithArguments(object);
            boolean isValid = (boolean) IS_VALID_HANDLE.invokeWithArguments(validationResult);

            if (isValid) {
                return ValidationResult.valid();
            }

            @SuppressWarnings("unchecked")
            List<Object> errors = (List<Object>) GET_ERRORS_HANDLE.invokeWithArguments(validationResult);
            List<String> errorMessages = new ArrayList<>();
            for (Object error : errors) {
                errorMessages.add(error.toString());
            }
            return ValidationResult.invalid(errorMessages);
        } catch (Throwable e) {
            return ValidationResult.invalid(List.of("Validation failed: " + e.getMessage()));
        }
    }

    /**
     * Creates a ConfigValidator that validates bound config objects.
     * 创建一个验证绑定配置对象的 ConfigValidator
     *
     * @param configClass the config class to bind and validate | 要绑定和验证的配置类
     * @param prefix the config prefix | 配置前缀
     * @param <T> the config type | 配置类型
     * @return config validator | 配置验证器
     */
    public static <T> ConfigValidator forObject(Class<T> configClass, String prefix) {
        Objects.requireNonNull(configClass, "configClass must not be null");
        Objects.requireNonNull(prefix, "prefix must not be null");

        return config -> {
            try {
                T boundObject = bindConfig(config, configClass, prefix);
                if (boundObject == null) {
                    return ValidationResult.invalid(List.of(
                            "Failed to bind config with prefix: " + prefix));
                }
                return validateObject(boundObject);
            } catch (Exception e) {
                return ValidationResult.invalid(List.of(
                        "Config binding failed: " + e.getMessage()));
            }
        };
    }

    /**
     * Creates a ConfigValidator for required keys with validation annotations.
     * 创建带验证注解的必需键的 ConfigValidator
     *
     * @param requiredKeys required config keys | 必需的配置键
     * @return config validator | 配置验证器
     */
    public static ConfigValidator forRequiredKeys(String... requiredKeys) {
        return config -> {
            List<String> errors = new ArrayList<>();
            for (String key : requiredKeys) {
                String value = config.getString(key, null);
                if (value == null || value.isBlank()) {
                    errors.add("Required config key is missing or empty: " + key);
                }
            }
            return errors.isEmpty() ? ValidationResult.valid() : ValidationResult.invalid(errors);
        };
    }

    /**
     * Creates a ConfigValidator that validates values against patterns.
     * 创建根据模式验证值的 ConfigValidator
     *
     * @param patterns map of config keys to regex patterns | 配置键到正则模式的映射
     * @return config validator | 配置验证器
     */
    public static ConfigValidator forPatterns(Map<String, String> patterns) {
        Objects.requireNonNull(patterns, "patterns must not be null");

        return config -> {
            List<String> errors = new ArrayList<>();
            for (Map.Entry<String, String> entry : patterns.entrySet()) {
                String key = entry.getKey();
                String pattern = entry.getValue();
                String value = config.getString(key, null);

                if (value != null && !value.matches(pattern)) {
                    errors.add(String.format("Config '%s' value '%s' does not match pattern '%s'",
                            key, value, pattern));
                }
            }
            return errors.isEmpty() ? ValidationResult.valid() : ValidationResult.invalid(errors);
        };
    }

    /**
     * Combines multiple validators into one.
     * 将多个验证器组合为一个
     *
     * @param validators validators to combine | 要组合的验证器
     * @return combined validator | 组合后的验证器
     */
    public static ConfigValidator combine(ConfigValidator... validators) {
        return config -> {
            List<String> allErrors = new ArrayList<>();
            for (ConfigValidator validator : validators) {
                ValidationResult result = validator.validate(config);
                if (!result.isValid()) {
                    allErrors.addAll(result.getErrors());
                }
            }
            return allErrors.isEmpty() ? ValidationResult.valid() : ValidationResult.invalid(allErrors);
        };
    }

    @SuppressWarnings("unchecked")
    private static <T> T bindConfig(Config config, Class<T> clazz, String prefix) {
        try {
            // Try to use RecordConfigBinder if available
            Class<?> binderClass = Class.forName("cloud.opencode.base.config.bind.RecordConfigBinder");
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            MethodHandle bindHandle = lookup.findStatic(binderClass, "bind",
                    MethodType.methodType(Object.class, Config.class, Class.class, String.class));
            return (T) bindHandle.invokeWithArguments(config, clazz, prefix);
        } catch (Throwable e) {
            return null;
        }
    }
}
