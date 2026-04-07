package cloud.opencode.base.classloader.plugin;

/**
 * Plugin lifecycle state enumeration
 * 插件生命周期状态枚举
 *
 * <p>Represents the possible states of a plugin within the managed lifecycle:
 * DISCOVERED &rarr; LOADED &rarr; STARTED &rarr; STOPPED &rarr; UNLOADED.
 * A plugin may transition to FAILED from any active state.</p>
 *
 * <p>表示插件在托管生命周期中的可能状态：
 * DISCOVERED &rarr; LOADED &rarr; STARTED &rarr; STOPPED &rarr; UNLOADED。
 * 插件可以从任何活动状态转换为 FAILED。</p>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable enum) - 线程安全: 是 (不可变枚举)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
public enum PluginState {

    /**
     * Plugin JAR discovered on disk but not yet loaded
     * 插件 JAR 在磁盘上被发现但尚未加载
     */
    DISCOVERED,

    /**
     * Plugin class loaded and instantiated
     * 插件类已加载并实例化
     */
    LOADED,

    /**
     * Plugin started and running
     * 插件已启动并运行
     */
    STARTED,

    /**
     * Plugin stopped gracefully
     * 插件已正常停止
     */
    STOPPED,

    /**
     * Plugin unloaded, ClassLoader closed
     * 插件已卸载，ClassLoader 已关闭
     */
    UNLOADED,

    /**
     * Plugin encountered an error during lifecycle transition
     * 插件在生命周期转换过程中遇到错误
     */
    FAILED
}
