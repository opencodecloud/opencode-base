package cloud.opencode.base.classloader.plugin;

import java.util.Objects;

/**
 * Immutable plugin context passed to plugins during lifecycle callbacks
 * 在生命周期回调期间传递给插件的不可变插件上下文
 *
 * <p>Contains the plugin identity and descriptor information needed by a
 * {@link Plugin} during its {@link Plugin#onStart(PluginContext)} callback.</p>
 *
 * <p>包含 {@link Plugin} 在 {@link Plugin#onStart(PluginContext)} 回调期间
 * 所需的插件身份和描述符信息。</p>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是 (不可变 record)</li>
 *   <li>Null-safe: Yes (constructor validates) - 空值安全: 是 (构造器校验)</li>
 * </ul>
 *
 * @param pluginId   the unique plugin identifier | 唯一的插件标识符
 * @param descriptor the plugin descriptor | 插件描述符
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
public record PluginContext(
        String pluginId,
        PluginDescriptor descriptor
) {

    /**
     * Create a new PluginContext with null validation
     * 创建带空值校验的新 PluginContext
     *
     * @param pluginId   the unique plugin identifier, must not be null |
     *                   唯一的插件标识符，不能为 null
     * @param descriptor the plugin descriptor, must not be null |
     *                   插件描述符，不能为 null
     * @throws NullPointerException if any parameter is null | 当任何参数为 null 时
     */
    public PluginContext {
        Objects.requireNonNull(pluginId, "pluginId must not be null");
        Objects.requireNonNull(descriptor, "descriptor must not be null");
    }
}
