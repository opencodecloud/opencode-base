package cloud.opencode.base.geo.exception;

/**
 * Fence Not Found Exception
 * 围栏不存在异常
 *
 * <p>Exception thrown when a geo fence is not found.</p>
 * <p>当地理围栏不存在时抛出的异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fence not found error with fence ID - 围栏未找到错误（含围栏ID）</li>
 *   <li>Automatic error code assignment (FENCE_NOT_FOUND) - 自动分配错误码（FENCE_NOT_FOUND）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw new FenceNotFoundException("fence-001");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
public class FenceNotFoundException extends GeoException {

    private final String fenceId;

    /**
     * Create fence not found exception
     * 创建围栏不存在异常
     *
     * @param fenceId the fence ID | 围栏ID
     */
    public FenceNotFoundException(String fenceId) {
        super("Fence not found: " + fenceId, GeoErrorCode.FENCE_NOT_FOUND);
        this.fenceId = fenceId;
    }

    /**
     * Get the fence ID
     * 获取围栏ID
     *
     * @return the fence ID | 围栏ID
     */
    public String getFenceId() {
        return fenceId;
    }
}
