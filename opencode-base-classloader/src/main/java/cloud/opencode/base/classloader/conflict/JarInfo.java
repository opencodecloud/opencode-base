package cloud.opencode.base.classloader.conflict;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Immutable information about a JAR file
 * JAR 文件的不可变信息
 *
 * <p>Captures the path, version, and name of a JAR file for use in conflict detection.
 * Version is extracted from {@code MANIFEST.MF} ({@code Implementation-Version} or
 * {@code Bundle-Version}); it may be {@code null} if not present.</p>
 * <p>捕获 JAR 文件的路径、版本和名称，用于冲突检测。版本从 {@code MANIFEST.MF}
 * ({@code Implementation-Version} 或 {@code Bundle-Version}) 提取；如果不存在则为 {@code null}。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable record - 不可变记录</li>
 *   <li>Version extraction from MANIFEST.MF - 从 MANIFEST.MF 提取版本</li>
 *   <li>Null-safe version field - 版本字段空值安全</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * JarInfo info = new JarInfo(Path.of("/libs/guava-31.jar"), "31.1-jre", "guava-31.jar");
 * System.out.println(info.name());    // "guava-31.jar"
 * System.out.println(info.version()); // "31.1-jre"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 * </ul>
 *
 * @param path    the absolute path to the JAR file | JAR 文件的绝对路径
 * @param version the version from MANIFEST.MF, or null if not present | MANIFEST.MF 中的版本，不存在则为 null
 * @param name    the file name of the JAR | JAR 的文件名
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
public record JarInfo(Path path, String version, String name) {

    /**
     * Creates a new JarInfo instance with validation
     * 创建新的 JarInfo 实例并进行验证
     *
     * @param path    the absolute path to the JAR file | JAR 文件的绝对路径
     * @param version the version from MANIFEST.MF, or null | MANIFEST.MF 中的版本，或 null
     * @param name    the file name of the JAR | JAR 的文件名
     */
    public JarInfo {
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(name, "name must not be null");
    }
}
