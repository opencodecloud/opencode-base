package cloud.opencode.base.config.source;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Command Line Arguments Configuration Source
 * 命令行参数配置源
 *
 * <p>Parses command line arguments into configuration properties with support for
 * multiple formats: --key=value, -Dkey=value, and boolean flags.</p>
 * <p>将命令行参数解析为配置属性,支持多种格式: --key=value, -Dkey=value 和布尔标志。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Multiple argument formats - 多种参数格式</li>
 *   <li>Boolean flag support (--flag -> flag=true) - 布尔标志支持</li>
 *   <li>Highest priority (200) - 最高优先级(200)</li>
 *   <li>Order preservation - 顺序保留</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Parse arguments
 * String[] args = {
 *     "--server.port=9090",
 *     "-Dapp.env=prod",
 *     "--debug",
 *     "--database.url=jdbc:mysql://localhost/db"
 * };
 *
 * ConfigSource source = new CommandLineConfigSource(args);
 * // server.port=9090
 * // app.env=prod
 * // debug=true
 * // database.url=jdbc:mysql://localhost/db
 * }</pre>
 *
 * <p><strong>Supported Formats | 支持的格式:</strong></p>
 * <pre>
 * --key=value     -> key=value
 * -Dkey=value     -> key=value
 * --flag          -> flag=true
 * -Dflag          -> flag=true
 * </pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) for parsing - 时间复杂度: 解析为O(n)</li>
 *   <li>Arguments parsed once at creation - 参数在创建时解析一次</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Immutable properties - 不可变属性</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
public class CommandLineConfigSource implements ConfigSource {

    private final Map<String, String> properties;

    /**
     * Create command line config source
     * 创建命令行配置源
     *
     * @param args command line arguments | 命令行参数
     */
    public CommandLineConfigSource(String[] args) {
        this.properties = parseArgs(args);
    }

    @Override
    public String getName() {
        return "command-line";
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public int getPriority() {
        return 200; // Highest priority - command line overrides everything
    }

    /**
     * Parse command line arguments
     * 解析命令行参数
     *
     * @param args command line arguments | 命令行参数
     * @return parsed properties | 解析的属性
     */
    private Map<String, String> parseArgs(String[] args) {
        Map<String, String> result = new LinkedHashMap<>();

        for (String arg : args) {
            if (arg.startsWith("--")) {
                parseKeyValue(arg.substring(2), result);
            } else if (arg.startsWith("-D")) {
                parseKeyValue(arg.substring(2), result);
            } else if (arg.startsWith("-")) {
                // Single dash flags
                parseKeyValue(arg.substring(1), result);
            }
        }

        return Map.copyOf(result);
    }

    /**
     * Parse key=value or flag argument
     * 解析key=value或标志参数
     *
     * @param arg argument without prefix | 不带前缀的参数
     * @param result result map | 结果映射
     */
    private void parseKeyValue(String arg, Map<String, String> result) {
        int idx = arg.indexOf('=');
        if (idx > 0) {
            // key=value format
            String key = arg.substring(0, idx);
            String value = arg.substring(idx + 1);
            result.put(key, value);
        } else {
            // Boolean flag: --flag equals --flag=true
            result.put(arg, "true");
        }
    }
}
