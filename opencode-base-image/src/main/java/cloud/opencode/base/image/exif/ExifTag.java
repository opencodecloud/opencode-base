package cloud.opencode.base.image.exif;

/**
 * EXIF Tag Category Enum
 * EXIF 标签类别枚举
 *
 * <p>Defines categories of EXIF metadata tags that can be read or stripped
 * from JPEG images.</p>
 * <p>定义可从 JPEG 图片中读取或清除的 EXIF 元数据标签类别。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Categorize EXIF tags for selective read/strip - 对 EXIF 标签进行分类以支持选择性读取/清除</li>
 *   <li>ALL represents the entire EXIF segment - ALL 代表整个 EXIF 段</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Strip all EXIF data
 * byte[] clean = ExifOp.strip(jpegBytes, ExifTag.ALL);
 *
 * // Strip only GPS data
 * byte[] noGps = ExifOp.strip(jpegBytes, ExifTag.GPS);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (enum is immutable) - 线程安全: 是（枚举不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
public enum ExifTag {

    /**
     * All EXIF tags - strip the entire APP1 segment
     * 所有 EXIF 标签 - 清除整个 APP1 段
     */
    ALL,

    /**
     * Orientation tag (0x0112)
     * 方向标签 (0x0112)
     */
    ORIENTATION,

    /**
     * GPS-related tags (latitude, longitude, etc.)
     * GPS 相关标签（纬度、经度等）
     */
    GPS,

    /**
     * DateTime tag (0x0132)
     * 日期时间标签 (0x0132)
     */
    DATETIME,

    /**
     * Camera make and model tags (0x010F, 0x0110)
     * 相机制造商和型号标签 (0x010F, 0x0110)
     */
    CAMERA,

    /**
     * Software tag (0x0131)
     * 软件标签 (0x0131)
     */
    SOFTWARE
}
