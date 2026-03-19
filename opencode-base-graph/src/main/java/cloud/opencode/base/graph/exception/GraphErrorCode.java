package cloud.opencode.base.graph.exception;

/**
 * Graph Error Code
 * 图错误码
 *
 * <p>Error codes for graph operations.</p>
 * <p>图操作的错误码。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Categorized error codes (1xxx structure, 2xxx algorithm, 3xxx validation, 4xxx resource) - 分类错误码</li>
 *   <li>Numeric code and description for each error - 每个错误的数字代码和描述</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * GraphErrorCode code = GraphErrorCode.CYCLE_DETECTED;
 * int numericCode = code.getCode();          // 2001
 * String desc = code.getDescription();       // "Cycle detected"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable enum) - 线程安全: 是（不可变枚举）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.0
 */
public enum GraphErrorCode {

    /** Unknown error | 未知错误 */
    UNKNOWN(0, "Unknown error"),

    // Structure errors 1xxx | 结构错误
    /** Vertex not found | 顶点不存在 */
    VERTEX_NOT_FOUND(1001, "Vertex not found"),
    /** Edge not found | 边不存在 */
    EDGE_NOT_FOUND(1002, "Edge not found"),
    /** Duplicate vertex | 重复顶点 */
    DUPLICATE_VERTEX(1003, "Duplicate vertex"),
    /** Duplicate edge | 重复边 */
    DUPLICATE_EDGE(1004, "Duplicate edge"),

    // Algorithm errors 2xxx | 算法错误
    /** Cycle detected | 检测到环 */
    CYCLE_DETECTED(2001, "Cycle detected"),
    /** No path exists | 无路径 */
    NO_PATH(2002, "No path exists"),
    /** Graph is disconnected | 图不连通 */
    DISCONNECTED(2003, "Graph is disconnected"),
    /** Negative weight edge | 负权边 */
    NEGATIVE_WEIGHT(2004, "Negative weight edge"),
    /** Invalid graph direction | 无效的图方向 */
    INVALID_DIRECTION(2005, "Invalid graph direction"),

    // Validation errors 3xxx | 验证错误
    /** Invalid vertex | 无效顶点 */
    INVALID_VERTEX(3001, "Invalid vertex"),
    /** Invalid edge | 无效边 */
    INVALID_EDGE(3002, "Invalid edge"),
    /** Invalid weight | 无效权重 */
    INVALID_WEIGHT(3003, "Invalid weight"),

    // Resource errors 4xxx | 资源错误
    /** Limit exceeded | 超出限制 */
    LIMIT_EXCEEDED(4001, "Limit exceeded"),
    /** Timeout | 计算超时 */
    TIMEOUT(4002, "Computation timeout"),
    /** Out of memory | 内存不足 */
    OUT_OF_MEMORY(4003, "Out of memory");

    private final int code;
    private final String description;

    GraphErrorCode(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Get the error code
     * 获取错误码
     *
     * @return error code | 错误码
     */
    public int getCode() {
        return code;
    }

    /**
     * Get the error description
     * 获取错误描述
     *
     * @return error description | 错误描述
     */
    public String getDescription() {
        return description;
    }
}
