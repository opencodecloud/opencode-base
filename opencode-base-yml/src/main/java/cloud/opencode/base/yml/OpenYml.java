package cloud.opencode.base.yml;

import cloud.opencode.base.yml.bind.YmlBinder;
import cloud.opencode.base.yml.diff.DiffEntry;
import cloud.opencode.base.yml.diff.YmlDiff;
import cloud.opencode.base.yml.exception.OpenYmlException;
import cloud.opencode.base.yml.include.YmlIncludeResolver;
import cloud.opencode.base.yml.merge.MergeStrategy;
import cloud.opencode.base.yml.merge.YmlMerger;
import cloud.opencode.base.yml.placeholder.PlaceholderResolver;
import cloud.opencode.base.yml.profile.YmlProfile;
import cloud.opencode.base.yml.spi.YmlProvider;
import cloud.opencode.base.yml.spi.YmlProviderFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * OpenYml - Main entry point for YAML operations
 * OpenYml - YAML 操作的主入口点
 *
 * <p>This class provides a simple API for parsing, writing, and manipulating YAML.</p>
 * <p>此类提供解析、写入和操作 YAML 的简单 API。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Parse YAML from strings, files, and streams - 从字符串、文件和流解析 YAML</li>
 *   <li>Write/dump YAML to strings, files, and writers - 将 YAML 写入字符串、文件和写入器</li>
 *   <li>Object binding (YAML to Java objects and back) - 对象绑定（YAML 到 Java 对象的双向转换）</li>
 *   <li>Document merging with configurable strategies - 可配置策略的文档合并</li>
 *   <li>Placeholder resolution (${key:default}) - 占位符解析</li>
 *   <li>Multi-document YAML support - 多文档 YAML 支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Parse YAML
 * Map<String, Object> data = OpenYml.load("server:\n  port: 8080");
 * YmlDocument doc = OpenYml.parse("server:\n  port: 8080");
 *
 * // Load from file
 * YmlDocument doc = OpenYml.loadFile(Path.of("config.yml"));
 *
 * // Write YAML
 * String yaml = OpenYml.dump(data);
 *
 * // Bind to object
 * ServerConfig config = OpenYml.bind(doc, ServerConfig.class);
 *
 * // Merge documents
 * Map<String, Object> merged = OpenYml.merge(base, overlay);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: No (null input may throw exceptions) - 空值安全: 否（空输入可能抛出异常）</li>
 *   <li>Uses SafeConstructor to prevent arbitrary type deserialization - 使用 SafeConstructor 防止任意类型反序列化</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
public final class OpenYml {

    private static final PlaceholderResolver DEFAULT_RESOLVER = PlaceholderResolver.builder()
        .withSystemProperties()
        .withEnvironmentVariables()
        .build();

    private OpenYml() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    // ================================
    // Parsing Methods
    // ================================

    /**
     * Parses YAML string to a Map.
     * 将 YAML 字符串解析为 Map。
     *
     * @param yaml the YAML string | YAML 字符串
     * @return the parsed map | 解析后的映射
     */
    public static Map<String, Object> load(String yaml) {
        YmlProvider provider = YmlProviderFactory.getProvider();
        return provider.load(yaml);
    }

    /**
     * Parses YAML to a document.
     * 将 YAML 解析为文档。
     *
     * @param yaml the YAML string | YAML 字符串
     * @return the document | 文档
     */
    public static YmlDocument parse(String yaml) {
        return YmlDocument.of(load(yaml));
    }

    /**
     * Loads YAML from a file.
     * 从文件加载 YAML。
     *
     * @param path the file path | 文件路径
     * @return the document | 文档
     */
    public static YmlDocument loadFile(Path path) {
        return loadFile(path, StandardCharsets.UTF_8);
    }

    /**
     * Loads YAML from a file with charset.
     * 使用字符集从文件加载 YAML。
     *
     * @param path    the file path | 文件路径
     * @param charset the charset | 字符集
     * @return the document | 文档
     */
    public static YmlDocument loadFile(Path path, Charset charset) {
        try {
            String content = Files.readString(path, charset);
            return parse(content);
        } catch (IOException e) {
            throw new OpenYmlException("Failed to read YAML file: " + path, e);
        }
    }

    /**
     * Loads YAML from an input stream.
     * 从输入流加载 YAML。
     *
     * @param input the input stream | 输入流
     * @return the document | 文档
     */
    public static YmlDocument loadStream(InputStream input) {
        YmlProvider provider = YmlProviderFactory.getProvider();
        return YmlDocument.of(provider.load(input));
    }

    /**
     * Loads multi-document YAML.
     * 加载多文档 YAML。
     *
     * @param yaml the YAML string | YAML 字符串
     * @return list of documents | 文档列表
     */
    public static List<YmlDocument> loadAll(String yaml) {
        YmlProvider provider = YmlProviderFactory.getProvider();
        List<Map<String, Object>> maps = provider.loadAll(yaml);
        return maps.stream()
            .map(YmlDocument::of)
            .toList();
    }

    // ================================
    // Writing Methods
    // ================================

    /**
     * Dumps data to YAML string.
     * 将数据转储为 YAML 字符串。
     *
     * @param data the data | 数据
     * @return the YAML string | YAML 字符串
     */
    public static String dump(Object data) {
        YmlProvider provider = YmlProviderFactory.getProvider();
        return provider.dump(data);
    }

    /**
     * Dumps data to YAML string with config.
     * 使用配置将数据转储为 YAML 字符串。
     *
     * @param data   the data | 数据
     * @param config the configuration | 配置
     * @return the YAML string | YAML 字符串
     */
    public static String dump(Object data, YmlConfig config) {
        YmlProvider provider = YmlProviderFactory.getProvider();
        return provider.dump(data, config);
    }

    /**
     * Dumps document to YAML string.
     * 将文档转储为 YAML 字符串。
     *
     * @param document the document | 文档
     * @return the YAML string | YAML 字符串
     */
    public static String dump(YmlDocument document) {
        return dump(document.asMap());
    }

    /**
     * Dumps object to YAML string.
     * 将对象转储为 YAML 字符串。
     *
     * @param object the object | 对象
     * @return the YAML string | YAML 字符串
     */
    public static String dumpObject(Object object) {
        Map<String, Object> map = YmlBinder.toMap(object);
        return dump(map);
    }

    /**
     * Writes YAML to a file.
     * 将 YAML 写入文件。
     *
     * @param data the data | 数据
     * @param path the file path | 文件路径
     */
    public static void writeFile(Object data, Path path) {
        writeFile(data, path, StandardCharsets.UTF_8);
    }

    /**
     * Writes YAML to a file with charset.
     * 使用字符集将 YAML 写入文件。
     *
     * @param data    the data | 数据
     * @param path    the file path | 文件路径
     * @param charset the charset | 字符集
     */
    public static void writeFile(Object data, Path path, Charset charset) {
        try {
            String yaml = dump(data);
            Files.writeString(path, yaml, charset);
        } catch (IOException e) {
            throw new OpenYmlException("Failed to write YAML file: " + path, e);
        }
    }

    /**
     * Writes YAML to a writer.
     * 将 YAML 写入写入器。
     *
     * @param data   the data | 数据
     * @param writer the writer | 写入器
     */
    public static void write(Object data, Writer writer) {
        YmlProvider provider = YmlProviderFactory.getProvider();
        provider.dump(data, writer);
    }

    /**
     * Dumps multiple documents.
     * 转储多个文档。
     *
     * @param documents the documents | 文档列表
     * @return the YAML string | YAML 字符串
     */
    public static String dumpAll(Iterable<?> documents) {
        YmlProvider provider = YmlProviderFactory.getProvider();
        return provider.dumpAll(documents);
    }

    // ================================
    // Binding Methods
    // ================================

    /**
     * Binds document to a Java object.
     * 将文档绑定到 Java 对象。
     *
     * @param document the document | 文档
     * @param type     the target type | 目标类型
     * @param <T>      the type parameter | 类型参数
     * @return the bound object | 绑定的对象
     */
    public static <T> T bind(YmlDocument document, Class<T> type) {
        return YmlBinder.bind(document, type);
    }

    /**
     * Binds document at path to a Java object.
     * 将指定路径的文档绑定到 Java 对象。
     *
     * @param document the document | 文档
     * @param path     the path prefix | 路径前缀
     * @param type     the target type | 目标类型
     * @param <T>      the type parameter | 类型参数
     * @return the bound object | 绑定的对象
     */
    public static <T> T bind(YmlDocument document, String path, Class<T> type) {
        return YmlBinder.bind(document, path, type);
    }

    /**
     * Binds YAML string to a Java object.
     * 将 YAML 字符串绑定到 Java 对象。
     *
     * @param yaml the YAML string | YAML 字符串
     * @param type the target type | 目标类型
     * @param <T>  the type parameter | 类型参数
     * @return the bound object | 绑定的对象
     */
    public static <T> T bind(String yaml, Class<T> type) {
        return bind(parse(yaml), type);
    }

    /**
     * Converts object to a Map.
     * 将对象转换为 Map。
     *
     * @param object the object | 对象
     * @return the map | 映射
     */
    public static Map<String, Object> toMap(Object object) {
        return YmlBinder.toMap(object);
    }

    // ================================
    // Merge Methods
    // ================================

    /**
     * Merges two YAML maps.
     * 合并两个 YAML 映射。
     *
     * @param base    the base map | 基础映射
     * @param overlay the overlay map | 覆盖映射
     * @return the merged map | 合并后的映射
     */
    public static Map<String, Object> merge(Map<String, Object> base, Map<String, Object> overlay) {
        return YmlMerger.merge(base, overlay);
    }

    /**
     * Merges two YAML maps with strategy.
     * 使用策略合并两个 YAML 映射。
     *
     * @param base     the base map | 基础映射
     * @param overlay  the overlay map | 覆盖映射
     * @param strategy the merge strategy | 合并策略
     * @return the merged map | 合并后的映射
     */
    public static Map<String, Object> merge(Map<String, Object> base, Map<String, Object> overlay, MergeStrategy strategy) {
        return YmlMerger.merge(base, overlay, strategy);
    }

    /**
     * Merges multiple YAML maps.
     * 合并多个 YAML 映射。
     *
     * @param maps the maps to merge | 要合并的映射
     * @return the merged map | 合并后的映射
     */
    @SafeVarargs
    public static Map<String, Object> mergeAll(Map<String, Object>... maps) {
        return YmlMerger.mergeAll(maps);
    }

    /**
     * Merges two documents.
     * 合并两个文档。
     *
     * @param base    the base document | 基础文档
     * @param overlay the overlay document | 覆盖文档
     * @return the merged document | 合并后的文档
     */
    public static YmlDocument merge(YmlDocument base, YmlDocument overlay) {
        return YmlDocument.of(merge(base.asMap(), overlay.asMap()));
    }

    // ================================
    // Placeholder Resolution
    // ================================

    /**
     * Resolves placeholders in YAML string.
     * 解析 YAML 字符串中的占位符。
     *
     * @param yaml the YAML string | YAML 字符串
     * @return the resolved YAML | 解析后的 YAML
     */
    public static String resolvePlaceholders(String yaml) {
        return DEFAULT_RESOLVER.resolve(yaml);
    }

    /**
     * Resolves placeholders in YAML string with resolver.
     * 使用解析器解析 YAML 字符串中的占位符。
     *
     * @param yaml     the YAML string | YAML 字符串
     * @param resolver the placeholder resolver | 占位符解析器
     * @return the resolved YAML | 解析后的 YAML
     */
    public static String resolvePlaceholders(String yaml, PlaceholderResolver resolver) {
        return resolver.resolve(yaml);
    }

    /**
     * Resolves placeholders in map values.
     * 解析映射值中的占位符。
     *
     * @param data the data map | 数据映射
     * @return the resolved map | 解析后的映射
     */
    public static Map<String, Object> resolvePlaceholders(Map<String, Object> data) {
        return DEFAULT_RESOLVER.resolveMap(data);
    }

    /**
     * Parses YAML with placeholder resolution.
     * 解析 YAML 并解析占位符。
     *
     * @param yaml the YAML string | YAML 字符串
     * @return the document with resolved placeholders | 解析了占位符的文档
     */
    public static YmlDocument parseWithPlaceholders(String yaml) {
        String resolved = resolvePlaceholders(yaml);
        return parse(resolved);
    }

    /**
     * Parses YAML with placeholder resolution using custom properties.
     * 使用自定义属性解析 YAML 并解析占位符。
     *
     * @param yaml       the YAML string | YAML 字符串
     * @param properties the properties for placeholder resolution | 用于占位符解析的属性
     * @return the document with resolved placeholders | 解析了占位符的文档
     */
    public static YmlDocument parseWithPlaceholders(String yaml, Map<String, String> properties) {
        PlaceholderResolver resolver = PlaceholderResolver.create(properties);
        String resolved = resolver.resolve(yaml);
        return parse(resolved);
    }

    // ================================
    // Validation Methods
    // ================================

    /**
     * Checks if YAML string is valid.
     * 检查 YAML 字符串是否有效。
     *
     * @param yaml the YAML string | YAML 字符串
     * @return true if valid | 如果有效则返回 true
     */
    public static boolean isValid(String yaml) {
        YmlProvider provider = YmlProviderFactory.getProvider();
        return provider.isValid(yaml);
    }

    /**
     * Parses YAML to a node tree.
     * 将 YAML 解析为节点树。
     *
     * @param yaml the YAML string | YAML 字符串
     * @return the root node | 根节点
     */
    public static YmlNode parseTree(String yaml) {
        YmlProvider provider = YmlProviderFactory.getProvider();
        return provider.parseTree(yaml);
    }

    // ================================
    // Diff Methods
    // ================================

    /**
     * Compares two YAML maps and returns a list of differences.
     * 比较两个 YAML 映射并返回差异列表。
     *
     * @param base  the base map | 基础映射
     * @param other the other map | 另一个映射
     * @return list of diff entries | 差异条目列表
     */
    public static List<DiffEntry> diff(Map<String, Object> base, Map<String, Object> other) {
        return YmlDiff.diff(base, other);
    }

    /**
     * Compares two YAML documents and returns a list of differences.
     * 比较两个 YAML 文档并返回差异列表。
     *
     * @param base  the base document | 基础文档
     * @param other the other document | 另一个文档
     * @return list of diff entries | 差异条目列表
     */
    public static List<DiffEntry> diff(YmlDocument base, YmlDocument other) {
        return YmlDiff.diff(base, other);
    }

    // ================================
    // Profile Loading
    // ================================

    /**
     * Loads configuration with profile overlays.
     * 加载带有 Profile 覆盖的配置。
     *
     * <p>Loads {@code {basePath}/{name}.yml} as the base, then overlays
     * {@code {basePath}/{name}-{profile}.yml} for each profile.</p>
     * <p>加载 {@code {basePath}/{name}.yml} 作为基础，然后为每个 Profile
     * 覆盖 {@code {basePath}/{name}-{profile}.yml}。</p>
     *
     * @param basePath the directory containing config files | 包含配置文件的目录
     * @param name     the base config file name (without extension) | 基础配置文件名（无扩展名）
     * @param profiles the profile names to overlay | 要覆盖的 Profile 名称
     * @return the merged document | 合并后的文档
     */
    public static YmlDocument loadProfile(Path basePath, String name, String... profiles) {
        return YmlProfile.load(basePath, name, profiles);
    }

    /**
     * Loads configuration with profile overlays using default name "application".
     * 使用默认名称 "application" 加载带有 Profile 覆盖的配置。
     *
     * @param directory the directory containing config files | 包含配置文件的目录
     * @param profiles  the profile names to overlay | 要覆盖的 Profile 名称
     * @return the merged document | 合并后的文档
     */
    public static YmlDocument loadDefaultProfile(Path directory, String... profiles) {
        return YmlProfile.loadDefault(directory, profiles);
    }

    // ================================
    // Include Resolution
    // ================================

    /**
     * Loads a YAML file with {@code !include} directive resolution.
     * 加载带有 {@code !include} 指令解析的 YAML 文件。
     *
     * @param file the YAML file to load | 要加载的 YAML 文件
     * @return the resolved YAML data as a map | 解析后的 YAML 数据映射
     */
    public static Map<String, Object> loadWithIncludes(Path file) {
        return YmlIncludeResolver.load(file);
    }
}
