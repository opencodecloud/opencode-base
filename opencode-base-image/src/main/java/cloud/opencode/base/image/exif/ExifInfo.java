package cloud.opencode.base.image.exif;

import java.time.Instant;

/**
 * EXIF Information Record
 * EXIF 信息记录
 *
 * <p>Immutable record holding EXIF metadata extracted from a JPEG image.
 * All fields except orientation are nullable, representing absent metadata.</p>
 * <p>不可变记录，保存从 JPEG 图片中提取的 EXIF 元数据。
 * 除 orientation 外所有字段均可为 null，表示元数据缺失。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Hold parsed EXIF metadata - 保存解析后的 EXIF 元数据</li>
 *   <li>GPS presence check - GPS 存在性检查</li>
 *   <li>Rotation necessity check - 旋转必要性检查</li>
 *   <li>Factory method for empty/absent EXIF - 空/缺失 EXIF 的工厂方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ExifInfo info = ExifOp.read(path);
 * if (info.hasGps()) {
 *     System.out.println("Location: " + info.latitude() + ", " + info.longitude());
 * }
 * if (info.needsRotation()) {
 *     BufferedImage corrected = ExifOp.autoOrient(image, info.orientation());
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 * </ul>
 *
 * @param orientation  EXIF orientation value (1-8), 0 if unknown | EXIF 方向值 (1-8)，未知为 0
 * @param cameraMake   camera manufacturer, nullable | 相机制造商，可为 null
 * @param cameraModel  camera model, nullable | 相机型号，可为 null
 * @param dateTime     date/time the photo was taken, nullable | 拍摄日期时间，可为 null
 * @param latitude     GPS latitude in decimal degrees, nullable | GPS 纬度（十进制度），可为 null
 * @param longitude    GPS longitude in decimal degrees, nullable | GPS 经度（十进制度），可为 null
 * @param imageWidth   image width in pixels, nullable | 图片宽度（像素），可为 null
 * @param imageHeight  image height in pixels, nullable | 图片高度（像素），可为 null
 * @param software     software used to create the image, nullable | 创建图片的软件，可为 null
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
public record ExifInfo(
        int orientation,
        String cameraMake,
        String cameraModel,
        Instant dateTime,
        Double latitude,
        Double longitude,
        Integer imageWidth,
        Integer imageHeight,
        String software
) {

    /**
     * Check if GPS coordinates are present
     * 检查是否存在 GPS 坐标
     *
     * @return true if both latitude and longitude are present | 当纬度和经度都存在时返回 true
     */
    public boolean hasGps() {
        return latitude != null && longitude != null;
    }

    /**
     * Check if the image needs rotation based on EXIF orientation
     * 根据 EXIF 方向检查图片是否需要旋转
     *
     * @return true if orientation is not normal (not 1) | 当方向不正常（非 1）时返回 true
     */
    public boolean needsRotation() {
        return orientation > 1;
    }

    /**
     * Create an empty ExifInfo representing absent or unparseable EXIF data
     * 创建一个空的 ExifInfo，表示缺失或无法解析的 EXIF 数据
     *
     * @return an ExifInfo with orientation=0 and all nullable fields null | orientation=0 且所有可空字段为 null 的 ExifInfo
     */
    public static ExifInfo empty() {
        return new ExifInfo(0, null, null, null, null, null, null, null, null);
    }
}
