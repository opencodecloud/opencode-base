package cloud.opencode.base.classloader.index;

import java.util.List;
import java.util.Objects;

/**
 * Class Index - Immutable record representing a pre-built class index
 * 类索引 - 表示预构建类索引的不可变记录
 *
 * <p>Contains a versioned, timestamped snapshot of all scanned class metadata
 * along with a classpath hash for staleness detection.</p>
 * <p>包含所有已扫描类元数据的带版本号和时间戳的快照，
 * 以及用于陈旧检测的 classpath 哈希值。</p>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是 (不可变记录)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
public record ClassIndex(
        int version,
        String timestamp,
        String classpathHash,
        List<ClassIndexEntry> entries
) {

    /**
     * Current index format version
     * 当前索引格式版本
     */
    public static final int CURRENT_VERSION = 1;

    /**
     * Default classpath location for the index file
     * 索引文件在 classpath 上的默认位置
     */
    public static final String INDEX_LOCATION = "META-INF/opencode/class-index.json";

    /**
     * Create a class index with defensive copy of entries
     * 创建类索引，对条目列表进行防御性拷贝
     *
     * @param version       index format version | 索引格式版本
     * @param timestamp     creation timestamp in ISO-8601 format | ISO-8601 格式的创建时间戳
     * @param classpathHash SHA-256 hash of the classpath for staleness detection | 用于陈旧检测的 classpath SHA-256 哈希值
     * @param entries       list of class index entries | 类索引条目列表
     */
    public ClassIndex {
        Objects.requireNonNull(timestamp, "timestamp must not be null");
        Objects.requireNonNull(classpathHash, "classpathHash must not be null");
        entries = entries != null ? List.copyOf(entries) : List.of();
    }
}
