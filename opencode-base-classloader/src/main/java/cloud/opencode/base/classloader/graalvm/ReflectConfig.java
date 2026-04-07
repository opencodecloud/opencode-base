package cloud.opencode.base.classloader.graalvm;

import java.util.Objects;

/**
 * GraalVM reflect-config.json entry
 * GraalVM reflect-config.json 配置条目
 *
 * <p>Immutable record representing a single entry in the GraalVM
 * native image reflection configuration file.</p>
 * <p>不可变记录，表示 GraalVM Native Image 反射配置文件中的单条记录。</p>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 * </ul>
 *
 * @param name                    fully qualified class name | 完全限定类名
 * @param allDeclaredConstructors include all declared constructors | 包含所有已声明的构造器
 * @param allDeclaredMethods      include all declared methods | 包含所有已声明的方法
 * @param allDeclaredFields       include all declared fields | 包含所有已声明的字段
 * @param allPublicMethods        include all public methods | 包含所有公共方法
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
public record ReflectConfig(
        String name,
        boolean allDeclaredConstructors,
        boolean allDeclaredMethods,
        boolean allDeclaredFields,
        boolean allPublicMethods
) {

    /**
     * Compact constructor with validation
     * 带验证的紧凑构造器
     *
     * @throws NullPointerException if name is null | 如果 name 为 null 抛出异常
     */
    public ReflectConfig {
        Objects.requireNonNull(name, "Class name must not be null");
    }

    /**
     * Convert this entry to a JSON object string
     * 将此条目转换为 JSON 对象字符串
     *
     * @return JSON object string | JSON 对象字符串
     */
    public String toJson() {
        StringBuilder sb = new StringBuilder(256);
        sb.append('{');
        sb.append("\"name\":\"").append(escapeJson(name)).append('"');
        sb.append(",\"allDeclaredConstructors\":").append(allDeclaredConstructors);
        sb.append(",\"allDeclaredMethods\":").append(allDeclaredMethods);
        sb.append(",\"allDeclaredFields\":").append(allDeclaredFields);
        sb.append(",\"allPublicMethods\":").append(allPublicMethods);
        sb.append('}');
        return sb.toString();
    }

    private static String escapeJson(String value) {
        return cloud.opencode.base.classloader.index.ClassIndexWriter.escapeJson(value);
    }
}
