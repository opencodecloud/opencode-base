package cloud.opencode.base.yml.spi;

import cloud.opencode.base.yml.YmlConfig;
import cloud.opencode.base.yml.YmlNode;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.List;
import java.util.Map;

/**
 * YAML Provider SPI Interface - Service provider interface for YAML processing
 * YAML 提供者 SPI 接口 - YAML 处理的服务提供者接口
 *
 * <p>This interface defines the contract for YAML processing providers.
 * Implementations can use different underlying libraries like SnakeYAML.</p>
 * <p>此接口定义了 YAML 处理提供者的契约。
 * 实现可以使用不同的底层库，如 SnakeYAML。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Load YAML from strings, streams, and to typed objects - 从字符串、流加载 YAML 及到类型化对象</li>
 *   <li>Dump objects to YAML strings, streams, and writers - 将对象输出到 YAML 字符串、流和写入器</li>
 *   <li>Tree parsing and validation support - 树解析和验证支持</li>
 *   <li>Multi-document YAML support - 多文档 YAML 支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Register via SPI: META-INF/services/cloud.opencode.base.yml.spi.YmlProvider
 * YmlProvider provider = YmlProviderFactory.getProvider();
 * Map<String, Object> data = provider.load("key: value");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Depends on implementation - 空值安全: 取决于实现</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
public interface YmlProvider {

    /**
     * Gets the provider name.
     * 获取提供者名称。
     *
     * @return the name | 名称
     */
    String getName();

    /**
     * Gets the provider priority (higher value = higher priority).
     * 获取提供者优先级（值越大优先级越高）。
     *
     * @return the priority | 优先级
     */
    default int getPriority() {
        return 100;
    }

    /**
     * Checks if this provider is available.
     * 检查此提供者是否可用。
     *
     * @return true if available | 如果可用则返回 true
     */
    default boolean isAvailable() {
        return true;
    }

    // ==================== Loading | 加载 ====================

    /**
     * Loads YAML string to Map.
     * 将 YAML 字符串加载为 Map。
     *
     * @param yaml the YAML string | YAML 字符串
     * @return the Map | Map
     */
    Map<String, Object> load(String yaml);

    /**
     * Loads YAML string to object.
     * 将 YAML 字符串加载为对象。
     *
     * @param yaml  the YAML string | YAML 字符串
     * @param clazz the target type | 目标类型
     * @param <T>   the type parameter | 类型参数
     * @return the object | 对象
     */
    <T> T load(String yaml, Class<T> clazz);

    /**
     * Loads YAML from input stream.
     * 从输入流加载 YAML。
     *
     * @param input the input stream | 输入流
     * @return the Map | Map
     */
    Map<String, Object> load(InputStream input);

    /**
     * Loads YAML from input stream to object.
     * 从输入流加载 YAML 为对象。
     *
     * @param input the input stream | 输入流
     * @param clazz the target type | 目标类型
     * @param <T>   the type parameter | 类型参数
     * @return the object | 对象
     */
    <T> T load(InputStream input, Class<T> clazz);

    /**
     * Loads multi-document YAML.
     * 加载多文档 YAML。
     *
     * @param yaml the YAML string | YAML 字符串
     * @return the list of documents | 文档列表
     */
    List<Map<String, Object>> loadAll(String yaml);

    /**
     * Loads multi-document YAML to objects.
     * 加载多文档 YAML 为对象列表。
     *
     * @param yaml  the YAML string | YAML 字符串
     * @param clazz the target type | 目标类型
     * @param <T>   the type parameter | 类型参数
     * @return the list of objects | 对象列表
     */
    <T> List<T> loadAll(String yaml, Class<T> clazz);

    // ==================== Dumping | 输出 ====================

    /**
     * Dumps object to YAML string.
     * 将对象输出为 YAML 字符串。
     *
     * @param obj the object | 对象
     * @return the YAML string | YAML 字符串
     */
    String dump(Object obj);

    /**
     * Dumps object to YAML string with config.
     * 使用配置将对象输出为 YAML 字符串。
     *
     * @param obj    the object | 对象
     * @param config the configuration | 配置
     * @return the YAML string | YAML 字符串
     */
    String dump(Object obj, YmlConfig config);

    /**
     * Dumps object to output stream.
     * 将对象输出到输出流。
     *
     * @param obj    the object | 对象
     * @param output the output stream | 输出流
     */
    void dump(Object obj, OutputStream output);

    /**
     * Dumps object to writer.
     * 将对象输出到 Writer。
     *
     * @param obj    the object | 对象
     * @param writer the writer | Writer
     */
    void dump(Object obj, Writer writer);

    /**
     * Dumps multiple documents to YAML string.
     * 将多个文档输出为 YAML 字符串。
     *
     * @param documents the documents | 文档列表
     * @return the YAML string | YAML 字符串
     */
    String dumpAll(Iterable<?> documents);

    // ==================== Tree Parsing | 树解析 ====================

    /**
     * Parses YAML to node tree.
     * 将 YAML 解析为节点树。
     *
     * @param yaml the YAML string | YAML 字符串
     * @return the root node | 根节点
     */
    YmlNode parseTree(String yaml);

    /**
     * Parses YAML from input stream to node tree.
     * 从输入流将 YAML 解析为节点树。
     *
     * @param input the input stream | 输入流
     * @return the root node | 根节点
     */
    YmlNode parseTree(InputStream input);

    // ==================== Validation | 验证 ====================

    /**
     * Checks if YAML string is valid.
     * 检查 YAML 字符串是否有效。
     *
     * @param yaml the YAML string | YAML 字符串
     * @return true if valid | 如果有效则返回 true
     */
    boolean isValid(String yaml);

    // ==================== Configuration | 配置 ====================

    /**
     * Creates a configured provider instance.
     * 创建配置后的提供者实例。
     *
     * @param config the configuration | 配置
     * @return the configured provider | 配置后的提供者
     */
    YmlProvider configure(YmlConfig config);

    /**
     * Gets the current configuration.
     * 获取当前配置。
     *
     * @return the configuration | 配置
     */
    YmlConfig getConfig();
}
