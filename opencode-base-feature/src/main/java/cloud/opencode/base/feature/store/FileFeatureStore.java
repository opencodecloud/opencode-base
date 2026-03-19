package cloud.opencode.base.feature.store;

import cloud.opencode.base.feature.Feature;
import cloud.opencode.base.feature.exception.FeatureStoreException;
import cloud.opencode.base.feature.strategy.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * File Feature Store
 * 文件功能存储
 *
 * <p>File-based storage using simple properties format.</p>
 * <p>使用简单属性格式的文件存储。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>File persistence - 文件持久化</li>
 *   <li>Memory cache - 内存缓存</li>
 *   <li>Auto-reload support - 自动重载支持</li>
 * </ul>
 *
 * <p><strong>File Format | 文件格式:</strong></p>
 * <pre>
 * # Feature configuration
 * feature.dark-mode.enabled=true
 * feature.dark-mode.strategy=always-on
 * feature.new-ui.enabled=true
 * feature.new-ui.strategy=percentage
 * feature.new-ui.percentage=50
 * </pre>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * FeatureStore store = new FileFeatureStore(Path.of("features.properties"));
 * store.save(Feature.builder("feature-1").alwaysOn().build());
 * store.reload(); // Reload from file
 * }</pre>
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
 * @since JDK 25, opencode-base-feature V1.0.0
 */
public class FileFeatureStore implements FeatureStore {

    private static final Logger LOGGER = Logger.getLogger(FileFeatureStore.class.getName());

    private final Path filePath;
    private final Map<String, Feature> cache;

    /**
     * Create file store
     * 创建文件存储
     *
     * @param filePath the file path | 文件路径
     */
    public FileFeatureStore(Path filePath) {
        this.filePath = filePath;
        this.cache = new ConcurrentHashMap<>();
        loadFromFile();
    }

    @Override
    public void save(Feature feature) {
        if (feature == null || feature.key() == null) {
            throw new IllegalArgumentException("Feature and key cannot be null");
        }
        cache.put(feature.key(), feature);
        saveToFile();
    }

    @Override
    public Optional<Feature> find(String key) {
        if (key == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(cache.get(key));
    }

    @Override
    public List<Feature> findAll() {
        return List.copyOf(cache.values());
    }

    @Override
    public boolean delete(String key) {
        if (key == null) {
            return false;
        }
        boolean removed = cache.remove(key) != null;
        if (removed) {
            saveToFile();
        }
        return removed;
    }

    @Override
    public void clear() {
        cache.clear();
        saveToFile();
    }

    /**
     * Reload features from file
     * 从文件重新加载功能
     */
    public void reload() {
        cache.clear();
        loadFromFile();
    }

    /**
     * Load features from file
     * 从文件加载功能
     */
    private void loadFromFile() {
        if (!Files.exists(filePath)) {
            return;
        }

        try {
            Properties props = new Properties();
            try (var reader = Files.newBufferedReader(filePath)) {
                props.load(reader);
            }

            // Find all unique feature keys
            Set<String> featureKeys = new HashSet<>();
            for (String propKey : props.stringPropertyNames()) {
                if (propKey.startsWith("feature.")) {
                    String[] parts = propKey.split("\\.", 3);
                    if (parts.length >= 2) {
                        featureKeys.add(parts[1]);
                    }
                }
            }

            // Load each feature
            for (String key : featureKeys) {
                String prefix = "feature." + key + ".";
                String strategy = props.getProperty(prefix + "strategy", "default");
                String name = props.getProperty(prefix + "name", key);
                String description = props.getProperty(prefix + "description", "");
                boolean defaultEnabled = Boolean.parseBoolean(
                    props.getProperty(prefix + "defaultEnabled", "false"));

                EnableStrategy enableStrategy = parseStrategy(strategy, props, prefix, key);

                Feature feature = new Feature(
                    key, name, description, defaultEnabled, enableStrategy,
                    Map.of(), Instant.now(), Instant.now()
                );
                cache.put(key, feature);
            }
        } catch (IOException e) {
            throw new FeatureStoreException("Failed to load features from file: " + filePath, e);
        }
    }

    /**
     * Save features to file
     * 保存功能到文件
     */
    private void saveToFile() {
        try {
            Properties props = new Properties();

            for (Feature feature : cache.values()) {
                String prefix = "feature." + feature.key() + ".";
                props.setProperty(prefix + "name", feature.name() != null ? feature.name() : feature.key());
                if (feature.description() != null) {
                    props.setProperty(prefix + "description", feature.description());
                }
                props.setProperty(prefix + "defaultEnabled", String.valueOf(feature.defaultEnabled()));

                String strategyName = getStrategyName(feature.strategy(), feature.key());
                props.setProperty(prefix + "strategy", strategyName);

                // Save strategy-specific properties
                saveStrategyProperties(feature.strategy(), props, prefix, feature.key());
            }

            Files.createDirectories(filePath.getParent() != null ? filePath.getParent() : Path.of("."));
            try (var writer = Files.newBufferedWriter(filePath)) {
                props.store(writer, "Feature configuration");
            }
        } catch (IOException e) {
            throw new FeatureStoreException("Failed to save features to file: " + filePath, e);
        }
    }

    /**
     * Save strategy-specific properties
     * 保存策略特定属性
     */
    private void saveStrategyProperties(EnableStrategy strategy, Properties props, String prefix, String featureKey) {
        if (strategy == null) {
            return;
        }

        switch (strategy) {
            case PercentageStrategy ps ->
                props.setProperty(prefix + "percentage", String.valueOf(ps.getPercentage()));

            case ConsistentPercentageStrategy cps -> {
                props.setProperty(prefix + "percentage", String.valueOf(cps.getPercentage()));
                // Note: salt is not persisted for security reasons
            }

            case UserListStrategy uls ->
                props.setProperty(prefix + "users", String.join(",", uls.getAllowedUsers()));

            case DateRangeStrategy drs -> {
                if (drs.getStartTime() != null) {
                    props.setProperty(prefix + "startTime", drs.getStartTime().toString());
                }
                if (drs.getEndTime() != null) {
                    props.setProperty(prefix + "endTime", drs.getEndTime().toString());
                }
            }

            case AlwaysOnStrategy _, AlwaysOffStrategy _ -> {
                // No additional properties needed
            }

            default -> LOGGER.log(Level.WARNING,
                "Feature ''{0}'': Strategy type ''{1}'' cannot be fully serialized. " +
                "Will use defaultEnabled={2} on reload.",
                new Object[]{featureKey, strategy.getClass().getSimpleName(),
                    props.getProperty(prefix + "defaultEnabled")});
        }
    }

    private EnableStrategy parseStrategy(String strategy, Properties props, String prefix, String featureKey) {
        return switch (strategy.toLowerCase()) {
            case "always-on" -> AlwaysOnStrategy.INSTANCE;
            case "always-off" -> AlwaysOffStrategy.INSTANCE;
            case "percentage" -> {
                int percent = Integer.parseInt(props.getProperty(prefix + "percentage", "0"));
                yield new PercentageStrategy(percent);
            }
            case "consistent-percentage" -> {
                int percent = Integer.parseInt(props.getProperty(prefix + "percentage", "0"));
                yield new ConsistentPercentageStrategy(percent);
            }
            case "user-list" -> {
                String users = props.getProperty(prefix + "users", "");
                Set<String> userSet = users.isEmpty() ? Set.of() :
                    new HashSet<>(Arrays.asList(users.split(",")));
                yield new UserListStrategy(userSet);
            }
            case "date-range" -> {
                Instant start = parseInstant(props.getProperty(prefix + "startTime"));
                Instant end = parseInstant(props.getProperty(prefix + "endTime"));
                yield new DateRangeStrategy(start, end);
            }
            case "default" -> null; // Use defaultEnabled field
            default -> {
                LOGGER.log(Level.WARNING,
                    "Feature ''{0}'': Unknown strategy ''{1}'', will use defaultEnabled value.",
                    new Object[]{featureKey, strategy});
                yield null; // Use defaultEnabled field
            }
        };
    }

    private Instant parseInstant(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            LOGGER.log(Level.WARNING, "Failed to parse instant: {0}", value);
            return null;
        }
    }

    private String getStrategyName(EnableStrategy strategy, String featureKey) {
        if (strategy == null) return "default";
        if (strategy instanceof AlwaysOnStrategy) return "always-on";
        if (strategy instanceof AlwaysOffStrategy) return "always-off";
        if (strategy instanceof PercentageStrategy) return "percentage";
        if (strategy instanceof ConsistentPercentageStrategy) return "consistent-percentage";
        if (strategy instanceof UserListStrategy) return "user-list";
        if (strategy instanceof DateRangeStrategy) return "date-range";

        // Unsupported strategies: CompositeStrategy, TenantAwareStrategy, custom lambdas
        return "unsupported";
    }

    /**
     * Get the file path
     * 获取文件路径
     *
     * @return file path | 文件路径
     */
    public Path getFilePath() {
        return filePath;
    }
}
