package cloud.opencode.base.config;

import cloud.opencode.base.config.converter.ConverterRegistry;
import cloud.opencode.base.config.internal.DefaultConfig;
import cloud.opencode.base.config.source.*;
import cloud.opencode.base.config.validation.ConfigValidator;
import cloud.opencode.base.config.validation.RequiredValidator;
import cloud.opencode.base.config.validation.ValidationResult;
import cloud.opencode.base.config.converter.ConfigConverter;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Configuration Builder
 * 配置构建器
 *
 * <p>Fluent builder for constructing Config instances with multiple sources, validators,
 * converters, and options.</p>
 * <p>用于构建Config实例的流式构建器,支持多源、验证器、转换器和选项。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fluent builder API - 流式构建器API</li>
 *   <li>Multiple configuration sources - 多配置源</li>
 *   <li>Custom type converters - 自定义类型转换器</li>
 *   <li>Configuration validation - 配置验证</li>
 *   <li>Placeholder resolution control - 占位符解析控制</li>
 *   <li>Hot reload configuration - 热重载配置</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Config config = OpenConfig.builder()
 *     // Add sources (后添加的优先级更高)
 *     .addClasspathResource("default.properties")
 *     .addClasspathResource("application.properties")
 *     .addFile(Path.of("/etc/app/config.properties"))
 *     .addEnvironmentVariables("APP")
 *     .addSystemProperties()
 *     .addCommandLineArgs(args)
 *
 *     // Register custom converters
 *     .registerConverter(InetAddress.class, InetAddress::getByName)
 *
 *     // Validation
 *     .required("database.url", "database.password")
 *
 *     // Features
 *     .enableHotReload()
 *     .hotReloadInterval(Duration.ofSeconds(10))
 *
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 *   <li>Use in single thread during configuration - 在配置期间使用单线程</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(s) to build where s is the number of registered sources - 时间复杂度: O(s)，s 为注册的配置源数量</li>
 *   <li>Space complexity: O(s) for source and validator lists - 空间复杂度: O(s) 存储配置源和验证器列表</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
public class ConfigBuilder {

    private final List<ConfigSource> sources = new ArrayList<>();
    private final ConverterRegistry converters = ConverterRegistry.defaults();
    private final List<ConfigValidator> validators = new ArrayList<>();
    private boolean enablePlaceholders = true;
    private boolean enableHotReload = false;
    private boolean relaxedBinding = false;
    private Duration hotReloadInterval = Duration.ofSeconds(30);

    // ============ Add Configuration Sources | 添加配置源 ============

    /**
     * Add classpath resource
     * 添加类路径资源
     *
     * @param resource resource path | 资源路径
     * @return this builder | 构建器
     */
    public ConfigBuilder addClasspathResource(String resource) {
        sources.add(new PropertiesConfigSource(resource, true));
        return this;
    }

    /**
     * Add multiple classpath resources
     * 添加多个类路径资源
     *
     * @param resources resource paths | 资源路径
     * @return this builder | 构建器
     */
    public ConfigBuilder addClasspathResources(String... resources) {
        for (String resource : resources) {
            addClasspathResource(resource);
        }
        return this;
    }

    /**
     * Add file
     * 添加文件
     *
     * @param file file path | 文件路径
     * @return this builder | 构建器
     */
    public ConfigBuilder addFile(Path file) {
        sources.add(new PropertiesConfigSource(file));
        return this;
    }

    /**
     * Add multiple files
     * 添加多个文件
     *
     * @param files file paths | 文件路径
     * @return this builder | 构建器
     */
    public ConfigBuilder addFiles(Path... files) {
        for (Path file : files) {
            addFile(file);
        }
        return this;
    }

    /**
     * Add system properties
     * 添加系统属性
     *
     * @return this builder | 构建器
     */
    public ConfigBuilder addSystemProperties() {
        sources.add(new SystemPropertiesConfigSource());
        return this;
    }

    /**
     * Add environment variables
     * 添加环境变量
     *
     * @return this builder | 构建器
     */
    public ConfigBuilder addEnvironmentVariables() {
        sources.add(new EnvironmentConfigSource());
        return this;
    }

    /**
     * Add environment variables with prefix filter
     * 添加环境变量(带前缀过滤)
     *
     * @param prefix environment variable prefix | 环境变量前缀
     * @return this builder | 构建器
     */
    public ConfigBuilder addEnvironmentVariables(String prefix) {
        sources.add(new EnvironmentConfigSource(prefix));
        return this;
    }

    /**
     * Add command line arguments
     * 添加命令行参数
     *
     * @param args command line arguments | 命令行参数
     * @return this builder | 构建器
     */
    public ConfigBuilder addCommandLineArgs(String[] args) {
        sources.add(new CommandLineConfigSource(args));
        return this;
    }

    /**
     * Add properties map
     * 添加属性映射
     *
     * @param properties properties map | 属性映射
     * @return this builder | 构建器
     */
    public ConfigBuilder addProperties(Map<String, String> properties) {
        sources.add(new InMemoryConfigSource(properties));
        return this;
    }

    /**
     * Add custom configuration source
     * 添加自定义配置源
     *
     * @param source configuration source | 配置源
     * @return this builder | 构建器
     */
    public ConfigBuilder addSource(ConfigSource source) {
        sources.add(source);
        return this;
    }

    // ============ Type Converters | 类型转换器 ============

    /**
     * Register type converter
     * 注册类型转换器
     *
     * @param <T> target type | 目标类型
     * @param type target class | 目标类
     * @param converter converter function | 转换器函数
     * @return this builder | 构建器
     */
    public <T> ConfigBuilder registerConverter(Class<T> type, ConfigConverter<T> converter) {
        converters.register(type, converter);
        return this;
    }

    // ============ Placeholders | 占位符 ============

    /**
     * Disable placeholder resolution
     * 禁用占位符解析
     *
     * @return this builder | 构建器
     */
    public ConfigBuilder disablePlaceholders() {
        this.enablePlaceholders = false;
        return this;
    }

    // ============ Hot Reload | 热重载 ============

    /**
     * Enable hot reload
     * 启用热重载
     *
     * @return this builder | 构建器
     */
    public ConfigBuilder enableHotReload() {
        this.enableHotReload = true;
        return this;
    }

    /**
     * Set hot reload interval
     * 设置热重载间隔
     *
     * @param interval check interval | 检查间隔
     * @return this builder | 构建器
     */
    public ConfigBuilder hotReloadInterval(Duration interval) {
        this.hotReloadInterval = interval;
        return this;
    }

    // ============ Relaxed Binding | 宽松绑定 ============

    /**
     * Enable relaxed binding for configuration key resolution
     * 启用配置键解析的宽松绑定
     *
     * <p>When enabled, configuration keys are matched using relaxed rules, allowing
     * different naming conventions (kebab-case, camelCase, snake_case, UPPER_SNAKE)
     * to be used interchangeably.</p>
     * <p>启用后，配置键使用宽松规则匹配，允许不同命名约定（kebab-case、camelCase、
     * snake_case、UPPER_SNAKE）互换使用。</p>
     *
     * @return this builder | 构建器
     */
    public ConfigBuilder enableRelaxedBinding() {
        this.relaxedBinding = true;
        return this;
    }

    // ============ Validation | 验证 ============

    /**
     * Add validator
     * 添加验证器
     *
     * @param validator configuration validator | 配置验证器
     * @return this builder | 构建器
     */
    public ConfigBuilder addValidator(ConfigValidator validator) {
        validators.add(validator);
        return this;
    }

    /**
     * Add required keys validation
     * 添加必填键验证
     *
     * @param keys required keys | 必填键
     * @return this builder | 构建器
     */
    public ConfigBuilder required(String... keys) {
        validators.add(new RequiredValidator(keys));
        return this;
    }

    // ============ Build | 构建 ============

    /**
     * Build configuration
     * 构建配置
     *
     * @return configuration instance | 配置实例
     * @throws OpenConfigException if validation fails | 如果验证失败
     */
    public Config build() {
        // Create composite source
        CompositeConfigSource compositeSource = new CompositeConfigSource(sources);

        // Create config instance
        DefaultConfig config = new DefaultConfig(compositeSource, converters, relaxedBinding);

        // Enable placeholder resolution
        if (enablePlaceholders) {
            config.enablePlaceholderResolution();
        }

        // Enable hot reload
        if (enableHotReload) {
            config.enableHotReload(hotReloadInterval);
        }

        // Validate - collect all results and merge
        List<ValidationResult> results = validators.stream()
            .map(validator -> validator.validate(config))
            .toList();
        ValidationResult merged = ValidationResult.merge(results);
        if (!merged.isValid()) {
            throw OpenConfigException.validationFailed(
                String.join(", ", merged.getErrors()));
        }

        return config;
    }
}
