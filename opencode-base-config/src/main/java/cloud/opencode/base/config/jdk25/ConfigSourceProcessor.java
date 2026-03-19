package cloud.opencode.base.config.jdk25;

/**
 * Configuration Source Processor with Pattern Matching
 * 配置源处理器(使用模式匹配)
 *
 * <p>Demonstrates JDK 25 pattern matching for sealed types. Processes different
 * configuration source types using exhaustive switch expressions.</p>
 * <p>演示JDK 25密封类型的模式匹配。使用完备的switch表达式处理不同的配置源类型。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Exhaustive pattern matching - 完备模式匹配</li>
 *   <li>Record pattern deconstruction - 记录模式解构</li>
 *   <li>Type-safe source handling - 类型安全的源处理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ConfigSourceProcessor processor = new ConfigSourceProcessor();
 *
 * // Process file source
 * processor.process(new ConfigSourceType.File(Path.of("app.properties")));
 *
 * // Process environment source
 * processor.process(new ConfigSourceType.Environment("APP"));
 *
 * // Process system properties
 * processor.process(new ConfigSourceType.System());
 * }</pre>
 *
 * <p><strong>JDK 25 Features Used | 使用的JDK 25特性:</strong></p>
 * <ul>
 *   <li>Pattern matching for switch - switch模式匹配</li>
 *   <li>Record patterns - 记录模式</li>
 *   <li>Sealed type exhaustiveness - 密封类型完备性</li>
 * </ul>
 *
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Partial (validates inputs) - 空值安全: 部分（验证输入）</li>
 * </ul>
  * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) per call - exhaustive switch dispatch - 时间复杂度: 每次调用 O(1)，完备 switch 分发</li>
 *   <li>Space complexity: O(1) - 空间复杂度: O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
public class ConfigSourceProcessor {

    public void process(ConfigSourceType sourceType) {
        switch (sourceType) {
            case ConfigSourceType.File(var path, var watchable) -> {
                System.out.println("Loading from file: " + path);
                if (watchable) {
                    System.out.println("Watching for changes");
                }
            }
            case ConfigSourceType.Classpath(var resource) ->
                System.out.println("Loading from classpath: " + resource);
            case ConfigSourceType.Environment(var prefix) ->
                System.out.println("Loading from environment" +
                    (prefix != null ? " with prefix: " + prefix : ""));
            case ConfigSourceType.System() ->
                System.out.println("Loading from system properties");
            case ConfigSourceType.InMemory(var props) ->
                System.out.println("Loading from memory: " + props.size() + " properties");
        }
    }
}
