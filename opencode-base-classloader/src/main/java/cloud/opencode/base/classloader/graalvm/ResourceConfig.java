package cloud.opencode.base.classloader.graalvm;

import java.util.List;
import java.util.Objects;

/**
 * GraalVM resource-config.json configuration
 * GraalVM resource-config.json 资源配置
 *
 * <p>Immutable record representing the GraalVM native image resource
 * configuration file content.</p>
 * <p>不可变记录，表示 GraalVM Native Image 资源配置文件内容。</p>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record with defensive copy) - 线程安全: 是（不可变记录，防御性拷贝）</li>
 * </ul>
 *
 * @param includes list of resource patterns to include | 要包含的资源模式列表
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
public record ResourceConfig(List<Pattern> includes) {

    /**
     * Resource pattern entry
     * 资源模式条目
     *
     * @param pattern resource glob pattern | 资源匹配模式
     */
    public record Pattern(String pattern) {

        /**
         * Compact constructor with validation
         * 带验证的紧凑构造器
         *
         * @throws NullPointerException if pattern is null | 如果 pattern 为 null 抛出异常
         */
        public Pattern {
            Objects.requireNonNull(pattern, "Pattern must not be null");
        }

        /**
         * Convert to JSON object string
         * 转换为 JSON 对象字符串
         *
         * @return JSON object string | JSON 对象字符串
         */
        public String toJson() {
            return "{\"pattern\":\"" + escapeJson(pattern) + "\"}";
        }

        private static String escapeJson(String value) {
            return cloud.opencode.base.classloader.index.ClassIndexWriter.escapeJson(value);
        }
    }

    /**
     * Compact constructor with defensive copy and validation
     * 带防御性拷贝和验证的紧凑构造器
     *
     * @throws NullPointerException if includes is null | 如果 includes 为 null 抛出异常
     */
    public ResourceConfig {
        Objects.requireNonNull(includes, "Includes list must not be null");
        includes = List.copyOf(includes);
    }

    /**
     * Convert to JSON string matching GraalVM resource-config.json format
     * 转换为符合 GraalVM resource-config.json 格式的 JSON 字符串
     *
     * @return JSON string | JSON 字符串
     */
    public String toJson() {
        StringBuilder sb = new StringBuilder(256);
        sb.append("{\"resources\":{\"includes\":[");
        for (int i = 0; i < includes.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(includes.get(i).toJson());
        }
        sb.append("]}}");
        return sb.toString();
    }
}
