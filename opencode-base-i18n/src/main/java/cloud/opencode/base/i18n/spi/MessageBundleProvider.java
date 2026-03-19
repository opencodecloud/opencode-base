package cloud.opencode.base.i18n.spi;

/**
 * SPI interface for message bundle registration
 * 消息包注册的SPI接口
 *
 * <p>Each module implements this interface to register its message bundle.
 * Bundles are automatically discovered via {@link java.util.ServiceLoader}
 * when {@link cloud.opencode.base.i18n.OpenI18n} is first used.</p>
 * <p>每个模块实现此接口来注册其消息包。
 * 当 {@link cloud.opencode.base.i18n.OpenI18n} 首次使用时，
 * 通过 {@link java.util.ServiceLoader} 自动发现。</p>
 *
 * <p><strong>Usage | 使用方式:</strong></p>
 * <ol>
 *   <li>Implement this interface - 实现此接口</li>
 *   <li>Register via META-INF/services or module-info.java provides - 通过SPI注册</li>
 *   <li>Place .properties files on classpath - 将properties文件放在classpath中</li>
 * </ol>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * public class MyCoreMessages implements MessageBundleProvider {
 *     @Override
 *     public String baseName() {
 *         return "my-core-messages";
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>SPI for providing message bundles - 提供消息包的SPI接口</li>
 *   <li>Supports custom message source loading - 支持自定义消息源加载</li>
 *   <li>Integration point for third-party message stores - 第三方消息存储集成点</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Implement to provide custom message bundles
 * // 实现以提供自定义消息包
 * public class MyBundleProvider implements MessageBundleProvider {
 *     @Override
 *     public MessageBundle getBundle(Locale locale) {
 *         return loadFromDatabase(locale);
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.0
 */
public interface MessageBundleProvider {

    /**
     * Returns the base name of the resource bundle
     * 返回资源包的基础名称
     *
     * <p>The base name is used to locate .properties files on the classpath.
     * For example, a base name of "my-messages" will load
     * {@code my-messages.properties}, {@code my-messages_en.properties},
     * {@code my-messages_zh.properties}, etc.</p>
     * <p>基础名称用于在classpath中定位.properties文件。
     * 例如，基础名称 "my-messages" 将加载
     * {@code my-messages.properties}、{@code my-messages_en.properties}、
     * {@code my-messages_zh.properties} 等。</p>
     *
     * @return the resource bundle base name | 资源包基础名称
     */
    String baseName();

    /**
     * Returns the priority of this bundle (lower value = higher priority)
     * 返回此包的优先级（值越小优先级越高）
     *
     * <p>When multiple bundles contain the same key, the one with higher
     * priority (lower value) wins. Default is 0.</p>
     * <p>当多个包包含相同的键时，优先级更高（值更小）的包优先。默认为0。</p>
     *
     * @return the priority | 优先级
     */
    default int priority() {
        return 0;
    }
}
