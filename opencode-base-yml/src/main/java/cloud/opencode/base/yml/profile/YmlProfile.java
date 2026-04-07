package cloud.opencode.base.yml.profile;

import cloud.opencode.base.yml.YmlDocument;
import cloud.opencode.base.yml.exception.OpenYmlException;
import cloud.opencode.base.yml.merge.MergeStrategy;
import cloud.opencode.base.yml.merge.YmlMerger;
import cloud.opencode.base.yml.spi.YmlProvider;
import cloud.opencode.base.yml.spi.YmlProviderFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * YAML Profile Loader - Profile-based configuration loading with deep merge
 * YAML Profile 加载器 - 基于 Profile 的配置加载与深度合并
 *
 * <p>This utility class supports loading a base YAML configuration file and overlaying
 * profile-specific files (e.g. {@code application-dev.yml}, {@code application-prod.yml})
 * using deep merge. Missing profile files are silently skipped.</p>
 * <p>此工具类支持加载基础 YAML 配置文件并使用深度合并覆盖 Profile 特定文件
 * （如 {@code application-dev.yml}、{@code application-prod.yml}）。
 * 缺失的 Profile 文件会被静默跳过。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Load base + profile overlay YAML files - 加载基础 + Profile 覆盖 YAML 文件</li>
 *   <li>Deep merge of profile-specific overrides - Profile 特定覆盖的深度合并</li>
 *   <li>Multiple profile support with ordered application - 多 Profile 支持，按顺序应用</li>
 *   <li>Auto-detect active profiles from system properties or environment - 从系统属性或环境变量自动检测活跃 Profile</li>
 *   <li>Custom merge strategy support - 自定义合并策略支持</li>
 *   <li>Missing profile files silently skipped - 缺失的 Profile 文件静默跳过</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Load application.yml + application-dev.yml
 * YmlDocument doc = YmlProfile.load(Path.of("config"), "application", "dev");
 *
 * // Load with multiple profiles
 * YmlDocument doc = YmlProfile.load(Path.of("config"), "application", "dev", "local");
 *
 * // Load with default name "application"
 * YmlDocument doc = YmlProfile.load(Path.of("config"), "dev", "local");
 *
 * // Get active profiles from system property or env
 * List<String> profiles = YmlProfile.getActiveProfiles();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: No (null inputs throw exceptions) - 空值安全: 否（空输入抛出异常）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.3
 */
public final class YmlProfile {

    /**
     * System property name for active profiles.
     * 活跃 Profile 的系统属性名称。
     */
    private static final String SYSTEM_PROPERTY_KEY = "yml.profiles.active";

    /**
     * Environment variable name for active profiles.
     * 活跃 Profile 的环境变量名称。
     */
    private static final String ENV_VARIABLE_KEY = "YAML_PROFILES_ACTIVE";

    /**
     * Default configuration file base name.
     * 默认配置文件基础名称。
     */
    private static final String DEFAULT_NAME = "application";

    private YmlProfile() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    /**
     * Loads base configuration and profile overlays with deep merge.
     * 加载基础配置和 Profile 覆盖，使用深度合并。
     *
     * <p>Loads {@code {basePath}/{name}.yml} as the base, then for each profile,
     * loads {@code {basePath}/{name}-{profile}.yml} if it exists and merges it.</p>
     * <p>加载 {@code {basePath}/{name}.yml} 作为基础，然后对每个 Profile，
     * 如果存在则加载 {@code {basePath}/{name}-{profile}.yml} 并合并。</p>
     *
     * @param basePath the directory containing config files | 包含配置文件的目录
     * @param name     the base config file name (without extension) | 基础配置文件名（无扩展名）
     * @param profiles the profile names to overlay | 要覆盖的 Profile 名称
     * @return the merged document | 合并后的文档
     * @throws OpenYmlException if the base file cannot be read | 当基础文件无法读取时
     */
    public static YmlDocument load(Path basePath, String name, String... profiles) {
        return load(basePath, name, MergeStrategy.DEEP_MERGE,
                profiles == null ? List.of() : Arrays.stream(profiles).filter(Objects::nonNull).toList());
    }

    /**
     * Loads base configuration and profile overlays with a custom merge strategy.
     * 使用自定义合并策略加载基础配置和 Profile 覆盖。
     *
     * @param basePath the directory containing config files | 包含配置文件的目录
     * @param name     the base config file name (without extension) | 基础配置文件名（无扩展名）
     * @param strategy the merge strategy | 合并策略
     * @param profiles the profile names to overlay | 要覆盖的 Profile 名称
     * @return the merged document | 合并后的文档
     * @throws OpenYmlException if the base file cannot be read | 当基础文件无法读取时
     */
    public static YmlDocument load(Path basePath, String name, MergeStrategy strategy,
                                   List<String> profiles) {
        Objects.requireNonNull(basePath, "basePath must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(strategy, "strategy must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }

        // Load base file
        Path baseFile = basePath.resolve(name + ".yml");
        Map<String, Object> result = loadFileIfExists(baseFile);
        if (result == null) {
            // Try .yaml extension
            baseFile = basePath.resolve(name + ".yaml");
            result = loadFileIfExists(baseFile);
        }
        if (result == null) {
            throw new OpenYmlException("Base configuration file not found: "
                    + basePath.resolve(name + ".yml") + " or "
                    + basePath.resolve(name + ".yaml"));
        }

        // Overlay profiles
        if (profiles != null) {
            for (String profile : profiles) {
                if (profile == null || profile.isBlank()) {
                    continue;
                }
                Map<String, Object> profileData = loadProfileFile(basePath, name, profile);
                if (profileData != null) {
                    result = YmlMerger.merge(result, profileData, strategy);
                }
            }
        }

        return YmlDocument.of(result);
    }

    /**
     * Loads configuration from a directory using the default base name "application".
     * 使用默认基础名称 "application" 从目录加载配置。
     *
     * <p>Equivalent to {@code load(directory, "application", profiles)}.</p>
     * <p>等价于 {@code load(directory, "application", profiles)}。</p>
     *
     * @param directory the directory containing config files | 包含配置文件的目录
     * @param profiles  the profile names to overlay | 要覆盖的 Profile 名称
     * @return the merged document | 合并后的文档
     * @throws OpenYmlException if the base file cannot be found | 当基础文件找不到时
     */
    public static YmlDocument loadDefault(Path directory, String... profiles) {
        return load(directory, DEFAULT_NAME, profiles);
    }

    /**
     * Gets the active profiles from system property or environment variable.
     * 从系统属性或环境变量获取活跃的 Profile。
     *
     * <p>Checks in order:</p>
     * <ol>
     *   <li>System property {@code yml.profiles.active}</li>
     *   <li>Environment variable {@code YAML_PROFILES_ACTIVE}</li>
     * </ol>
     * <p>The value is split by comma, and each token is trimmed.</p>
     *
     * @return list of active profile names, or empty list if none configured | 活跃 Profile 名称列表，如果未配置则返回空列表
     */
    public static List<String> getActiveProfiles() {
        String value = System.getProperty(SYSTEM_PROPERTY_KEY);
        if (value == null || value.isBlank()) {
            value = System.getenv(ENV_VARIABLE_KEY);
        }
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::strip)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    /**
     * Loads a profile-specific file if it exists.
     * 加载 Profile 特定文件（如果存在）。
     */
    private static Map<String, Object> loadProfileFile(Path basePath, String name, String profile) {
        // Security: validate profile name to prevent path traversal
        if (profile.contains("/") || profile.contains("\\") || profile.contains("..")) {
            throw new OpenYmlException("Invalid profile name: '" + profile
                    + "' (must not contain path separators or '..')");
        }
        // Try .yml first, then .yaml
        Path profileFile = basePath.resolve(name + "-" + profile + ".yml");
        Map<String, Object> data = loadFileIfExists(profileFile);
        if (data != null) {
            return data;
        }
        profileFile = basePath.resolve(name + "-" + profile + ".yaml");
        return loadFileIfExists(profileFile);
    }

    /**
     * Loads a YAML file if it exists, returning null if not found.
     * 加载 YAML 文件（如果存在），如果找不到则返回 null。
     */
    private static Map<String, Object> loadFileIfExists(Path file) {
        if (!Files.exists(file) || !Files.isRegularFile(file)) {
            return null;
        }
        try {
            String content = Files.readString(file, StandardCharsets.UTF_8);
            YmlProvider provider = YmlProviderFactory.getProvider();
            Map<String, Object> data = provider.load(content);
            return data != null ? data : new LinkedHashMap<>();
        } catch (IOException e) {
            throw new OpenYmlException("Failed to read configuration file: " + file, e);
        }
    }
}
