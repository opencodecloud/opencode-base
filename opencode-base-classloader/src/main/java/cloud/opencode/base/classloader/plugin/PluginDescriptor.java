package cloud.opencode.base.classloader.plugin;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Immutable plugin descriptor loaded from META-INF/opencode/plugin.properties
 * 从 META-INF/opencode/plugin.properties 加载的不可变插件描述符
 *
 * <p>Contains all metadata needed to identify and load a plugin:
 * id, name, version, main class, and JAR path.</p>
 *
 * <p>包含标识和加载插件所需的所有元数据：
 * ID、名称、版本、主类和 JAR 路径。</p>
 *
 * <p><strong>Properties format | 属性文件格式:</strong></p>
 * <pre>
 * plugin.id=auth-plugin
 * plugin.name=Authentication Plugin
 * plugin.version=1.0.0
 * plugin.mainClass=com.example.AuthPlugin
 * </pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是 (不可变 record)</li>
 *   <li>Null-safe: Yes (constructor validates) - 空值安全: 是 (构造器校验)</li>
 * </ul>
 *
 * @param id        the unique plugin identifier | 唯一的插件标识符
 * @param name      the human-readable plugin name | 人类可读的插件名称
 * @param version   the plugin version string | 插件版本字符串
 * @param mainClass the fully qualified name of the {@link Plugin} implementation |
 *                  {@link Plugin} 实现的完全限定类名
 * @param jarPath   the path to the plugin JAR file | 插件 JAR 文件的路径
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
public record PluginDescriptor(
        String id,
        String name,
        String version,
        String mainClass,
        Path jarPath
) {

    /**
     * Create a new PluginDescriptor with null validation
     * 创建带空值校验的新 PluginDescriptor
     *
     * @param id        the unique plugin identifier, must not be null |
     *                  唯一的插件标识符，不能为 null
     * @param name      the human-readable plugin name, must not be null |
     *                  人类可读的插件名称，不能为 null
     * @param version   the plugin version string, must not be null |
     *                  插件版本字符串，不能为 null
     * @param mainClass the fully qualified plugin main class, must not be null |
     *                  完全限定的插件主类名，不能为 null
     * @param jarPath   the path to the plugin JAR, must not be null |
     *                  插件 JAR 的路径，不能为 null
     * @throws NullPointerException if any parameter is null | 当任何参数为 null 时
     */
    public PluginDescriptor {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(version, "version must not be null");
        Objects.requireNonNull(mainClass, "mainClass must not be null");
        Objects.requireNonNull(jarPath, "jarPath must not be null");
    }
}
