package cloud.opencode.base.classloader.plugin;

/**
 * Plugin SPI - Defines the lifecycle callbacks for a plugin
 * 插件 SPI - 定义插件的生命周期回调
 *
 * <p>Implement this interface to create a plugin that participates in the
 * managed lifecycle (discover &rarr; load &rarr; start &rarr; stop &rarr; unload).</p>
 *
 * <p>实现此接口以创建参与托管生命周期的插件
 * （发现 &rarr; 加载 &rarr; 启动 &rarr; 停止 &rarr; 卸载）。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * public class AuthPlugin implements Plugin {
 *     @Override
 *     public void onStart(PluginContext context) {
 *         System.out.println("Auth plugin started: " + context.pluginId());
 *     }
 *     @Override
 *     public void onStop() {
 *         System.out.println("Auth plugin stopped");
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
public interface Plugin {

    /**
     * Called when the plugin is started
     * 插件启动时调用
     *
     * @param context the plugin context providing identity and descriptor |
     *                提供身份和描述符的插件上下文
     */
    default void onStart(PluginContext context) {
    }

    /**
     * Called when the plugin is stopped
     * 插件停止时调用
     */
    default void onStop() {
    }
}
