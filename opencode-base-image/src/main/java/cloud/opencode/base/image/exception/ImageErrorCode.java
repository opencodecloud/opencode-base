package cloud.opencode.base.image.exception;

/**
 * Image Error Code
 * 图片错误码枚举
 *
 * <p>Error codes for image processing operations.</p>
 * <p>图片处理操作的错误码。</p>
 *
 * <p><strong>Error Code Ranges | 错误码范围:</strong></p>
 * <ul>
 *   <li>1xxx - IO errors | IO错误</li>
 *   <li>2xxx - Format errors | 格式错误</li>
 *   <li>3xxx - Operation errors | 操作错误</li>
 *   <li>4xxx - Validation errors | 验证错误</li>
 *   <li>5xxx - Resource errors | 资源错误</li>
 * </ul>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Enumeration of image processing error codes - 图片处理错误码枚举</li>
 *   <li>Categorized by type: IO, format, operation, validation, resource - 按类型分类：IO、格式、操作、验证、资源</li>
 *   <li>Bilingual error messages (English + Chinese) - 双语错误消息（英文 + 中文）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Use error code in exception
 * throw new ImageException("Read failed", ImageErrorCode.READ_FAILED);
 * 
 * // Get error info
 * int code = ImageErrorCode.READ_FAILED.getCode();
 * String msg = ImageErrorCode.READ_FAILED.getMessage();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable enum) - 线程安全: 是（不可变枚举）</li>
 *   <li>Null-safe: N/A - 空值安全: 不适用</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.0
 */
public enum ImageErrorCode {

    /**
     * Unknown error | 未知错误
     */
    UNKNOWN(0, "Unknown error", "未知错误"),

    // ==================== IO Errors (1xxx) | IO错误 ====================

    /**
     * Image read failed | 图片读取失败
     */
    READ_FAILED(1001, "Image read failed", "图片读取失败"),

    /**
     * Image write failed | 图片写入失败
     */
    WRITE_FAILED(1002, "Image write failed", "图片写入失败"),

    /**
     * File not found | 文件不存在
     */
    FILE_NOT_FOUND(1003, "File not found", "文件不存在"),

    /**
     * IO error | IO错误
     */
    IO_ERROR(1004, "IO error", "IO错误"),

    // ==================== Format Errors (2xxx) | 格式错误 ====================

    /**
     * Unsupported format | 不支持的图片格式
     */
    UNSUPPORTED_FORMAT(2001, "Unsupported image format", "不支持的图片格式"),

    /**
     * Invalid image | 无效图片文件
     */
    INVALID_IMAGE(2002, "Invalid image file", "无效图片文件"),

    /**
     * Format mismatch | 格式不匹配
     */
    FORMAT_MISMATCH(2003, "Format mismatch", "格式不匹配"),

    /**
     * Magic number mismatch | 文件魔数不匹配
     */
    MAGIC_NUMBER_MISMATCH(2004, "File magic number mismatch", "文件魔数不匹配"),

    // ==================== Operation Errors (3xxx) | 操作错误 ====================

    /**
     * Resize failed | 缩放失败
     */
    RESIZE_FAILED(3001, "Resize failed", "缩放失败"),

    /**
     * Crop failed | 裁剪失败
     */
    CROP_FAILED(3002, "Crop failed", "裁剪失败"),

    /**
     * Rotate failed | 旋转失败
     */
    ROTATE_FAILED(3003, "Rotate failed", "旋转失败"),

    /**
     * Watermark failed | 水印失败
     */
    WATERMARK_FAILED(3004, "Watermark failed", "水印失败"),

    /**
     * Compress failed | 压缩失败
     */
    COMPRESS_FAILED(3005, "Compress failed", "压缩失败"),

    /**
     * Invalid parameters | 无效参数
     */
    INVALID_PARAMETERS(3006, "Invalid parameters", "无效参数"),

    /**
     * Convert failed | 转换失败
     */
    CONVERT_FAILED(3007, "Convert failed", "转换失败"),

    // ==================== Validation Errors (4xxx) | 验证错误 ====================

    /**
     * Image too large | 图片尺寸过大
     */
    IMAGE_TOO_LARGE(4001, "Image dimensions too large", "图片尺寸过大"),

    /**
     * File too large | 文件大小过大
     */
    FILE_TOO_LARGE(4002, "File size too large", "文件大小过大"),

    /**
     * Invalid dimensions | 无效尺寸
     */
    INVALID_DIMENSIONS(4003, "Invalid dimensions", "无效尺寸"),

    /**
     * Validation failed | 验证失败
     */
    VALIDATION_FAILED(4004, "Validation failed", "验证失败"),

    // ==================== Resource Errors (5xxx) | 资源错误 ====================

    /**
     * Processing timeout | 处理超时
     */
    TIMEOUT(5001, "Processing timeout", "处理超时"),

    /**
     * Out of memory | 内存不足
     */
    OUT_OF_MEMORY(5002, "Out of memory", "内存不足"),

    /**
     * Too many requests | 请求过多
     */
    TOO_MANY_REQUESTS(5003, "Too many concurrent requests", "请求过多"),

    /**
     * Resource unavailable | 资源不可用
     */
    RESOURCE_UNAVAILABLE(5004, "Resource unavailable", "资源不可用");

    private final int code;
    private final String message;
    private final String description;

    ImageErrorCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

    /**
     * Get error code
     * 获取错误码
     *
     * @return the error code | 错误码
     */
    public int getCode() {
        return code;
    }

    /**
     * Get error message
     * 获取错误消息
     *
     * @return the error message | 错误消息
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get error description
     * 获取错误描述
     *
     * @return the error description | 错误描述
     */
    public String getDescription() {
        return description;
    }
}
