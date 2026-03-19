package cloud.opencode.base.yml.snakeyaml;

import cloud.opencode.base.yml.YmlConfig;
import cloud.opencode.base.yml.YmlNode;
import cloud.opencode.base.yml.exception.YmlParseException;
import cloud.opencode.base.yml.spi.YmlProvider;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.util.*;

/**
 * SnakeYAML Provider - YAML provider using SnakeYAML library
 * SnakeYAML 提供者 - 使用 SnakeYAML 库的 YAML 提供者
 *
 * <p>This class provides YAML parsing and writing using SnakeYAML.</p>
 * <p>此类使用 SnakeYAML 提供 YAML 解析和写入。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Full YmlProvider SPI implementation backed by SnakeYAML - 基于 SnakeYAML 的完整 YmlProvider SPI 实现</li>
 *   <li>Safe mode with SafeConstructor - 使用 SafeConstructor 的安全模式</li>
 *   <li>Configurable loader and dumper options - 可配置的加载器和输出器选项</li>
 *   <li>Multi-document and tree parsing support - 多文档和树解析支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SnakeYamlProvider provider = new SnakeYamlProvider();
 * Map<String, Object> data = provider.load("key: value");
 * String yaml = provider.dump(data);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (SnakeYAML Yaml instances are not thread-safe) - 线程安全: 否（SnakeYAML 实例非线程安全）</li>
 *   <li>Null-safe: No (null input may cause exceptions) - 空值安全: 否（空输入可能导致异常）</li>
 *   <li>Always uses SafeConstructor to prevent arbitrary deserialization - 始终使用 SafeConstructor 防止任意反序列化</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
public final class SnakeYamlProvider implements YmlProvider {

    private static final String NAME = "snakeyaml";

    private final YmlConfig config;
    private final Yaml yaml;

    /**
     * Creates a new SnakeYAML provider with default configuration.
     * 创建新的 SnakeYAML 提供者（使用默认配置）。
     */
    public SnakeYamlProvider() {
        this(YmlConfig.defaults());
    }

    /**
     * Creates a new SnakeYAML provider with the specified configuration.
     * 创建新的 SnakeYAML 提供者（使用指定配置）。
     *
     * @param config the configuration | 配置
     */
    public SnakeYamlProvider(YmlConfig config) {
        this.config = config;
        this.yaml = createYaml(config);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public boolean isAvailable() {
        try {
            Class.forName("org.yaml.snakeyaml.Yaml");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public Map<String, Object> load(String yamlStr) {
        try {
            Object result = yaml.load(yamlStr);
            return toMap(result);
        } catch (Exception e) {
            throw new YmlParseException("Failed to parse YAML", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T load(String yamlStr, Class<T> clazz) {
        try {
            return yaml.loadAs(yamlStr, clazz);
        } catch (Exception e) {
            throw new YmlParseException("Failed to parse YAML to " + clazz.getName(), e);
        }
    }

    @Override
    public Map<String, Object> load(InputStream input) {
        try {
            Object result = yaml.load(input);
            return toMap(result);
        } catch (Exception e) {
            throw new YmlParseException("Failed to parse YAML from input stream", e);
        }
    }

    @Override
    public <T> T load(InputStream input, Class<T> clazz) {
        try {
            return yaml.loadAs(input, clazz);
        } catch (Exception e) {
            throw new YmlParseException("Failed to parse YAML to " + clazz.getName(), e);
        }
    }

    @Override
    public List<Map<String, Object>> loadAll(String yamlStr) {
        try {
            List<Map<String, Object>> result = new ArrayList<>();
            for (Object doc : yaml.loadAll(yamlStr)) {
                result.add(toMap(doc));
            }
            return result;
        } catch (Exception e) {
            throw new YmlParseException("Failed to parse multi-document YAML", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> loadAll(String yamlStr, Class<T> clazz) {
        try {
            List<T> result = new ArrayList<>();
            for (Object doc : yaml.loadAll(yamlStr)) {
                if (clazz.isInstance(doc)) {
                    result.add((T) doc);
                }
            }
            return result;
        } catch (Exception e) {
            throw new YmlParseException("Failed to parse multi-document YAML to " + clazz.getName(), e);
        }
    }

    @Override
    public String dump(Object obj) {
        return yaml.dump(obj);
    }

    @Override
    public String dump(Object obj, YmlConfig dumpConfig) {
        Yaml configuredYaml = createYaml(dumpConfig);
        return configuredYaml.dump(obj);
    }

    @Override
    public void dump(Object obj, OutputStream output) {
        try (Writer writer = new OutputStreamWriter(output)) {
            yaml.dump(obj, writer);
        } catch (IOException e) {
            throw new YmlParseException("Failed to dump YAML to output stream", e);
        }
    }

    @Override
    public void dump(Object obj, Writer writer) {
        yaml.dump(obj, writer);
    }

    @Override
    public String dumpAll(Iterable<?> documents) {
        return yaml.dumpAll(documents.iterator());
    }

    @Override
    public YmlNode parseTree(String yamlStr) {
        Object data = yaml.load(yamlStr);
        return YmlNode.of(data);
    }

    @Override
    public YmlNode parseTree(InputStream input) {
        Object data = yaml.load(input);
        return YmlNode.of(data);
    }

    @Override
    public boolean isValid(String yamlStr) {
        try {
            yaml.load(yamlStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public YmlProvider configure(YmlConfig newConfig) {
        return new SnakeYamlProvider(newConfig);
    }

    @Override
    public YmlConfig getConfig() {
        return config;
    }

    private Yaml createYaml(YmlConfig cfg) {
        LoaderOptions loaderOptions = createLoaderOptions(cfg);
        DumperOptions dumperOptions = createDumperOptions(cfg);
        Representer representer = new Representer(dumperOptions);

        if (cfg.isSafeMode()) {
            return new Yaml(new SafeConstructor(loaderOptions), representer, dumperOptions, loaderOptions);
        }

        return new Yaml(new SafeConstructor(loaderOptions), representer, dumperOptions, loaderOptions);
    }

    private LoaderOptions createLoaderOptions(YmlConfig cfg) {
        LoaderOptions options = new LoaderOptions();

        // Set max aliases for security
        options.setMaxAliasesForCollections(cfg.getMaxAliasesForCollections());

        // Allow duplicate keys
        options.setAllowDuplicateKeys(cfg.isAllowDuplicateKeys());

        // Set code point limit for large documents
        options.setCodePointLimit((int) Math.min(cfg.getMaxDocumentSize(), Integer.MAX_VALUE));

        return options;
    }

    private DumperOptions createDumperOptions(YmlConfig cfg) {
        DumperOptions options = new DumperOptions();

        // Set default flow style based on config
        YmlConfig.FlowStyle flowStyle = cfg.getDefaultFlowStyle();
        options.setDefaultFlowStyle(switch (flowStyle) {
            case FLOW -> DumperOptions.FlowStyle.FLOW;
            case BLOCK -> DumperOptions.FlowStyle.BLOCK;
            case AUTO -> DumperOptions.FlowStyle.AUTO;
        });

        // Pretty print
        if (cfg.isPrettyPrint()) {
            options.setPrettyFlow(true);
        }

        // Indentation
        options.setIndent(cfg.getIndent());
        options.setIndicatorIndent(Math.max(0, cfg.getIndent() - 1));

        return options;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(Object obj) {
        if (obj == null) {
            return new LinkedHashMap<>();
        }
        if (obj instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                result.put(String.valueOf(entry.getKey()), entry.getValue());
            }
            return result;
        }
        // Single value - wrap in map
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("value", obj);
        return result;
    }
}
