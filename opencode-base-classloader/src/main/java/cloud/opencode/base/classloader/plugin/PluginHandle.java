package cloud.opencode.base.classloader.plugin;

import cloud.opencode.base.classloader.loader.IsoClassLoader;

import java.util.Objects;

/**
 * Plugin handle - Provides access to a loaded plugin and its state
 * 插件句柄 - 提供对已加载插件及其状态的访问
 *
 * <p>Encapsulates a loaded plugin instance along with its descriptor,
 * ClassLoader, and current lifecycle state. State transitions are
 * managed by {@link PluginManager}.</p>
 *
 * <p>封装已加载的插件实例及其描述符、ClassLoader 和当前生命周期状态。
 * 状态转换由 {@link PluginManager} 管理。</p>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: State is volatile - 线程安全: 状态为 volatile</li>
 *   <li>Null-safe: Yes (constructor validates) - 空值安全: 是 (构造器校验)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
public class PluginHandle {

    private final String pluginId;
    private final PluginDescriptor descriptor;
    private final IsoClassLoader classLoader;
    private final Plugin plugin;
    private volatile PluginState state;

    /**
     * Create a new PluginHandle
     * 创建新的 PluginHandle
     *
     * @param pluginId    the unique plugin identifier | 唯一的插件标识符
     * @param descriptor  the plugin descriptor | 插件描述符
     * @param classLoader the isolated ClassLoader for this plugin | 此插件的隔离 ClassLoader
     * @param plugin      the plugin instance | 插件实例
     * @param state       the initial plugin state | 初始插件状态
     * @throws NullPointerException if any parameter is null | 当任何参数为 null 时
     */
    PluginHandle(String pluginId, PluginDescriptor descriptor, IsoClassLoader classLoader,
                 Plugin plugin, PluginState state) {
        this.pluginId = Objects.requireNonNull(pluginId, "pluginId must not be null");
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor must not be null");
        this.classLoader = Objects.requireNonNull(classLoader, "classLoader must not be null");
        this.plugin = Objects.requireNonNull(plugin, "plugin must not be null");
        this.state = Objects.requireNonNull(state, "state must not be null");
    }

    /**
     * Get the unique plugin identifier
     * 获取唯一的插件标识符
     *
     * @return the plugin id | 插件 ID
     */
    public String getPluginId() {
        return pluginId;
    }

    /**
     * Get the plugin descriptor
     * 获取插件描述符
     *
     * @return the descriptor | 描述符
     */
    public PluginDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * Get the isolated ClassLoader for this plugin
     * 获取此插件的隔离 ClassLoader
     *
     * @return the ClassLoader | 类加载器
     */
    public IsoClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Get the plugin instance
     * 获取插件实例
     *
     * @return the plugin instance | 插件实例
     */
    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * Get the current plugin state
     * 获取当前插件状态
     *
     * @return the current state | 当前状态
     */
    public PluginState getState() {
        return state;
    }

    /**
     * Set the plugin state (package-private, managed by PluginManager)
     * 设置插件状态（包级私有，由 PluginManager 管理）
     *
     * @param state the new state | 新状态
     */
    void setState(PluginState state) {
        this.state = Objects.requireNonNull(state, "state must not be null");
    }

    @Override
    public String toString() {
        return "PluginHandle{pluginId='" + pluginId + "', state=" + state + '}';
    }
}
