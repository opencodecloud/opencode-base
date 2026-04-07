package cloud.opencode.base.yml.include;

import cloud.opencode.base.yml.exception.OpenYmlException;
import cloud.opencode.base.yml.spi.YmlProvider;
import cloud.opencode.base.yml.spi.YmlProviderFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * YAML Include Resolver - Resolves {@code !include} directives in YAML files
 * YAML 包含解析器 - 解析 YAML 文件中的 {@code !include} 指令
 *
 * <p>This class processes YAML content and resolves {@code !include <path>} directives
 * by loading referenced files and substituting their content into the parent document.
 * It supports nested includes with configurable depth limits and security controls.</p>
 * <p>此类处理 YAML 内容并解析 {@code !include <path>} 指令，通过加载引用的文件并将其内容
 * 替换到父文档中。支持嵌套包含，提供可配置的深度限制和安全控制。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Resolve {@code !include} directives in YAML files - 解析 YAML 文件中的 !include 指令</li>
 *   <li>Nested include support with depth limit - 嵌套包含支持，带深度限制</li>
 *   <li>Circular reference detection - 循环引用检测</li>
 *   <li>Path traversal prevention - 路径遍历防护</li>
 *   <li>Configurable allowed file extensions - 可配置的允许文件扩展名</li>
 *   <li>Builder pattern for configuration - 构建器模式配置</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Simple usage with defaults
 * Map<String, Object> data = YmlIncludeResolver.load(Path.of("config.yml"));
 *
 * // Custom configuration
 * YmlIncludeResolver resolver = YmlIncludeResolver.builder()
 *     .maxDepth(5)
 *     .allowedExtensions(Set.of(".yml", ".yaml"))
 *     .basePath(Path.of("/etc/config"))
 *     .build();
 * Map<String, Object> data = resolver.resolve(Path.of("/etc/config/app.yml"));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构建后不可变）</li>
 *   <li>Path traversal prevention via basePath containment check - 通过 basePath 包含检查防止路径遍历</li>
 *   <li>Circular reference detection via visited file tracking - 通过已访问文件跟踪检测循环引用</li>
 *   <li>Extension whitelist to prevent loading non-YAML files - 扩展名白名单防止加载非 YAML 文件</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.3
 */
public final class YmlIncludeResolver {

    private static final int DEFAULT_MAX_DEPTH = 10;
    private static final Set<String> DEFAULT_EXTENSIONS = Set.of(".yml", ".yaml");

    /**
     * Pattern matching YAML values containing {@code !include <path>}.
     * 匹配包含 {@code !include <path>} 的 YAML 值的模式。
     */
    private static final Pattern INCLUDE_PATTERN = Pattern.compile("^!include\\s+(.+)$");

    private final int maxDepth;
    private final Set<String> allowedExtensions;
    private final Path basePath;

    private YmlIncludeResolver(int maxDepth, Set<String> allowedExtensions, Path basePath) {
        this.maxDepth = maxDepth;
        this.allowedExtensions = Set.copyOf(allowedExtensions);
        this.basePath = basePath;
    }

    /**
     * Creates a new builder for configuring the resolver.
     * 创建用于配置解析器的新构建器。
     *
     * @return a new builder | 新构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Convenience method to load a YAML file with include resolution using defaults.
     * 使用默认配置加载带包含解析的 YAML 文件的便捷方法。
     *
     * @param file the YAML file to load | 要加载的 YAML 文件
     * @return the resolved YAML data as a map | 解析后的 YAML 数据映射
     * @throws OpenYmlException if loading or resolution fails | 当加载或解析失败时
     */
    public static Map<String, Object> load(Path file) {
        Objects.requireNonNull(file, "file must not be null");
        Path parent = file.toAbsolutePath().normalize().getParent();
        if (parent == null) {
            throw new OpenYmlException("Cannot determine parent directory for file: " + file);
        }
        YmlIncludeResolver resolver = builder()
                .basePath(parent)
                .build();
        return resolver.resolve(file);
    }

    /**
     * Resolves a YAML file with {@code !include} directives.
     * 解析带有 {@code !include} 指令的 YAML 文件。
     *
     * @param file the YAML file to resolve | 要解析的 YAML 文件
     * @return the resolved YAML data as a map | 解析后的 YAML 数据映射
     * @throws OpenYmlException if loading or resolution fails | 当加载或解析失败时
     */
    public Map<String, Object> resolve(Path file) {
        Objects.requireNonNull(file, "file must not be null");
        Path absoluteFile = file.toAbsolutePath().normalize();
        validateExtension(absoluteFile);
        String content = readFile(absoluteFile);
        Path resolveBase = basePath != null ? basePath : absoluteFile.getParent();
        if (resolveBase == null) {
            throw new OpenYmlException("Cannot determine base path for file: " + file);
        }
        Path anchor = resolveBase; // fixed anchor for path traversal checks
        Set<Path> visited = new LinkedHashSet<>();
        visited.add(absoluteFile);
        return resolveInternal(content, resolveBase, anchor, visited, 0);
    }

    /**
     * Resolves a YAML string with {@code !include} directives relative to the given base path.
     * 解析相对于给定基础路径的带有 {@code !include} 指令的 YAML 字符串。
     *
     * @param yaml     the YAML string to resolve | 要解析的 YAML 字符串
     * @param basePath the base path for resolving includes | 解析包含的基础路径
     * @return the resolved YAML data as a map | 解析后的 YAML 数据映射
     * @throws OpenYmlException if resolution fails | 当解析失败时
     */
    public Map<String, Object> resolve(String yaml, Path resolveBasePath) {
        Objects.requireNonNull(yaml, "yaml must not be null");
        Objects.requireNonNull(resolveBasePath, "basePath must not be null");
        Path resolveBase = resolveBasePath.toAbsolutePath().normalize();
        // Anchor to the provided basePath or instance basePath for path traversal checks
        Path anchor = this.basePath != null ? this.basePath : resolveBase;
        Set<Path> visited = new LinkedHashSet<>();
        return resolveInternal(yaml, resolveBase, anchor, visited, 0);
    }

    /**
     * Internal recursive resolution method.
     * 内部递归解析方法。
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> resolveInternal(String yaml, Path resolveBase,
                                                 Path anchor, Set<Path> visited, int depth) {
        if (depth > maxDepth) {
            throw new OpenYmlException("Include depth exceeded maximum of " + maxDepth
                    + "; possible recursive includes");
        }

        YmlProvider provider = YmlProviderFactory.getProvider();
        Map<String, Object> data = provider.load(yaml);
        if (data == null) {
            return new LinkedHashMap<>();
        }

        return (Map<String, Object>) processNode(data, resolveBase, anchor, visited, depth);
    }

    /**
     * Recursively processes a YAML node, resolving any include directives.
     * 递归处理 YAML 节点，解析任何包含指令。
     */
    @SuppressWarnings("unchecked")
    private Object processNode(Object node, Path resolveBase, Path anchor,
                                Set<Path> visited, int depth) {
        if (node instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = String.valueOf(entry.getKey());
                Object value = entry.getValue();
                Object processed = processNode(value, resolveBase, anchor, visited, depth);
                if (processed instanceof Map<?, ?> processedMap) {
                    result.put(key, processedMap);
                } else {
                    result.put(key, processed);
                }
            }
            return result;
        } else if (node instanceof List<?> list) {
            List<Object> result = new ArrayList<>(list.size());
            for (Object item : list) {
                result.add(processNode(item, resolveBase, anchor, visited, depth));
            }
            return result;
        } else if (node instanceof String str) {
            Matcher matcher = INCLUDE_PATTERN.matcher(str.strip());
            if (matcher.matches()) {
                String includePath = matcher.group(1).strip();
                return resolveInclude(includePath, resolveBase, anchor, visited, depth);
            }
            return node;
        }
        return node;
    }

    /**
     * Resolves a single include directive.
     * 解析单个包含指令。
     */
    private Object resolveInclude(String includePath, Path resolveBase,
                                   Path anchor, Set<Path> visited, int depth) {
        Path resolved = resolveBase.resolve(includePath).toAbsolutePath().normalize();

        // Security: check path traversal against fixed anchor (never shifts)
        if (!resolved.startsWith(anchor)) {
            throw new OpenYmlException("Path traversal detected: included file '"
                    + includePath + "' resolves outside base directory '" + anchor + "'");
        }

        // Security: check extension
        validateExtension(resolved);

        // Security: check circular reference
        if (visited.contains(resolved)) {
            throw new OpenYmlException("Circular include detected: '"
                    + resolved + "' has already been included. Chain: " + visited);
        }

        if (!Files.exists(resolved)) {
            throw new OpenYmlException("Included file not found: " + resolved);
        }

        String content = readFile(resolved);
        Set<Path> newVisited = new LinkedHashSet<>(visited);
        newVisited.add(resolved);

        return resolveInternal(content, resolved.getParent(), anchor, newVisited, depth + 1);
    }

    /**
     * Validates that the file has an allowed extension.
     * 验证文件是否具有允许的扩展名。
     */
    private void validateExtension(Path file) {
        String fileName = file.getFileName().toString().toLowerCase(Locale.ROOT);
        boolean allowed = allowedExtensions.stream()
                .anyMatch(ext -> fileName.endsWith(ext.toLowerCase(Locale.ROOT)));
        if (!allowed) {
            throw new OpenYmlException("File extension not allowed: '" + fileName
                    + "'. Allowed extensions: " + allowedExtensions);
        }
    }

    /**
     * Reads a file as a UTF-8 string.
     * 以 UTF-8 编码读取文件为字符串。
     */
    private static String readFile(Path file) {
        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new OpenYmlException("Failed to read included file: " + file, e);
        }
    }

    /**
     * Builder for YmlIncludeResolver - Configures include resolution behavior
     * YmlIncludeResolver 构建器 - 配置包含解析行为
     *
     * @author Leon Soo
     * <a href="https://leonsoo.com">www.LeonSoo.com</a>
     * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
     * @since JDK 25, opencode-base-yml V1.0.3
     */
    public static final class Builder {

        private int maxDepth = DEFAULT_MAX_DEPTH;
        private Set<String> allowedExtensions = DEFAULT_EXTENSIONS;
        private Path basePath;

        private Builder() {
        }

        /**
         * Sets the maximum include depth.
         * 设置最大包含深度。
         *
         * @param maxDepth the maximum depth (must be positive) | 最大深度（必须为正数）
         * @return this builder | 此构建器
         * @throws IllegalArgumentException if maxDepth is not positive | 当 maxDepth 不为正数时
         */
        public Builder maxDepth(int maxDepth) {
            if (maxDepth <= 0) {
                throw new IllegalArgumentException("maxDepth must be positive, got: " + maxDepth);
            }
            this.maxDepth = maxDepth;
            return this;
        }

        /**
         * Sets the allowed file extensions for included files.
         * 设置包含文件允许的文件扩展名。
         *
         * @param extensions the allowed extensions (e.g. ".yml", ".yaml") | 允许的扩展名
         * @return this builder | 此构建器
         * @throws NullPointerException     if extensions is null | 当 extensions 为 null 时
         * @throws IllegalArgumentException if extensions is empty | 当 extensions 为空时
         */
        public Builder allowedExtensions(Set<String> extensions) {
            Objects.requireNonNull(extensions, "extensions must not be null");
            if (extensions.isEmpty()) {
                throw new IllegalArgumentException("extensions must not be empty");
            }
            this.allowedExtensions = Set.copyOf(extensions);
            return this;
        }

        /**
         * Sets the base path for resolving included files.
         * 设置解析包含文件的基础路径。
         *
         * <p>All included file paths must resolve within this directory.
         * If not set, the parent directory of the loaded file is used.</p>
         * <p>所有包含的文件路径必须在此目录内解析。
         * 如果未设置，则使用加载文件的父目录。</p>
         *
         * @param basePath the base directory | 基础目录
         * @return this builder | 此构建器
         */
        public Builder basePath(Path basePath) {
            this.basePath = basePath != null ? basePath.toAbsolutePath().normalize() : null;
            return this;
        }

        /**
         * Builds the YmlIncludeResolver.
         * 构建 YmlIncludeResolver。
         *
         * @return the configured resolver | 配置好的解析器
         */
        public YmlIncludeResolver build() {
            return new YmlIncludeResolver(maxDepth, allowedExtensions, basePath);
        }
    }
}
