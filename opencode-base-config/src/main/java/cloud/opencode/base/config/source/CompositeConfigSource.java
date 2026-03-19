package cloud.opencode.base.config.source;

import java.util.*;

/**
 * Composite Configuration Source
 * 组合配置源
 *
 * <p>Combines multiple configuration sources with priority-based merging. Higher priority
 * sources override lower priority ones.</p>
 * <p>组合多个配置源并基于优先级合并。高优先级源覆盖低优先级源。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Priority-based merging - 基于优先级的合并</li>
 *   <li>Multiple source aggregation - 多源聚合</li>
 *   <li>Automatic source ordering - 自动源排序</li>
 *   <li>Hot reload support - 热重载支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * List<ConfigSource> sources = List.of(
 *     new PropertiesConfigSource("defaults.properties"),  // Priority: 50
 *     new EnvironmentConfigSource(),                      // Priority: 100
 *     new CommandLineConfigSource(args)                   // Priority: 200
 * );
 *
 * CompositeConfigSource composite = new CompositeConfigSource(sources);
 * // Command line > Environment > Properties
 * }</pre>
 *
 * <p><strong>Priority Order | 优先级顺序:</strong></p>
 * <pre>
 * Higher number = Higher priority
 * 200: Command Line
 * 100: Environment Variables
 * 50:  Properties/YAML files
 * 10:  In-Memory defaults
 * </pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n*m) for merging - 时间复杂度: 合并为O(n*m)</li>
 *   <li>Merged map cached - 合并的映射被缓存</li>
 *   <li>Reload triggers re-merge - 重载触发重新合并</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Immutable merged properties - 不可变的合并属性</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
public class CompositeConfigSource implements ConfigSource {

    private static final System.Logger logger = System.getLogger(CompositeConfigSource.class.getName());

    private final List<ConfigSource> sources;
    private volatile Map<String, String> mergedProperties;

    /**
     * Create composite config source
     * 创建组合配置源
     *
     * @param sources configuration sources | 配置源列表
     */
    public CompositeConfigSource(List<ConfigSource> sources) {
        // Sort by priority (ascending, so we can override in order)
        this.sources = sources.stream()
            .sorted(Comparator.comparingInt(ConfigSource::getPriority))
            .toList();

        this.mergedProperties = mergeProperties();
    }

    @Override
    public String getName() {
        return "composite[" + sources.size() + " sources]";
    }

    @Override
    public Map<String, String> getProperties() {
        return mergedProperties;
    }

    @Override
    public int getPriority() {
        // Return highest priority among sources
        return sources.stream()
            .mapToInt(ConfigSource::getPriority)
            .max()
            .orElse(0);
    }

    @Override
    public boolean supportsReload() {
        return sources.stream().anyMatch(ConfigSource::supportsReload);
    }

    @Override
    public void reload() {
        sources.stream()
            .filter(ConfigSource::supportsReload)
            .forEach(source -> {
                try {
                    source.reload();
                } catch (Exception e) {
                    logger.log(System.Logger.Level.ERROR, "Failed to reload config source: " + source.getName(), e);
                }
            });

        this.mergedProperties = mergeProperties();
    }

    /**
     * Get all underlying sources
     * 获取所有底层源
     *
     * @return list of sources | 源列表
     */
    public List<ConfigSource> getSources() {
        return Collections.unmodifiableList(sources);
    }

    /**
     * Merge properties from all sources
     * 从所有源合并属性
     *
     * <p>Sources are processed in priority order (low to high), so higher priority
     * sources override lower priority ones.</p>
     * <p>源按优先级顺序处理(从低到高),因此高优先级源覆盖低优先级源。</p>
     *
     * @return merged properties map | 合并的属性映射
     */
    private Map<String, String> mergeProperties() {
        Map<String, String> merged = new LinkedHashMap<>();

        // Process in priority order (low to high)
        for (ConfigSource source : sources) {
            Map<String, String> props = source.getProperties();
            if (props != null) {
                merged.putAll(props);
            }
        }

        return Map.copyOf(merged);
    }
}
