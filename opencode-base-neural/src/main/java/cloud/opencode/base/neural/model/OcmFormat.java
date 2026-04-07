package cloud.opencode.base.neural.model;

/**
 * OCM Format Constants
 * OCM 格式常量
 *
 * <p>Defines the binary format constants for the OpenCode Model (.ocm) file format.
 * The format uses a fixed-size header followed by serialized model data including
 * metadata, graph structure, and weight tensors.</p>
 * <p>定义 OpenCode 模型（.ocm）文件格式的二进制格式常量。
 * 该格式使用固定大小的头部，后跟序列化的模型数据，
 * 包括元数据、计算图结构和权重张量。</p>
 *
 * <p><strong>Format Layout | 格式布局:</strong></p>
 * <ul>
 *   <li>Bytes 0-3: Magic number (0x4F434D00 = "OCM\0") - 魔数</li>
 *   <li>Bytes 4-5: Major version - 主版本号</li>
 *   <li>Bytes 6-7: Minor version - 次版本号</li>
 *   <li>Bytes 8-63: Reserved (zero-filled) - 保留（零填充）</li>
 *   <li>Bytes 64+: Model payload - 模型数据</li>
 * </ul>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable constants) - 线程安全: 是（不可变常量）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see OcmWriter
 * @see OcmLoader
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public final class OcmFormat {

    /**
     * Magic number identifying .ocm files ("OCM\0" in ASCII)
     * 标识 .ocm 文件的魔数（ASCII 中的 "OCM\0"）
     */
    public static final int MAGIC = 0x4F434D00;

    /**
     * Current major version of the format
     * 格式的当前主版本号
     */
    public static final short VERSION_MAJOR = 1;

    /**
     * Current minor version of the format
     * 格式的当前次版本号
     */
    public static final short VERSION_MINOR = 0;

    /**
     * Fixed header size in bytes
     * 固定头部大小（字节）
     */
    public static final int HEADER_SIZE = 64;

    private OcmFormat() {
        throw new AssertionError("No OcmFormat instances");
    }
}
