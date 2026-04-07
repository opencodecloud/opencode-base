package cloud.opencode.base.neural.exception;

/**
 * Neural Error Code
 * 神经网络错误码枚举
 *
 * <p>Error codes for neural network operations.</p>
 * <p>神经网络操作的错误码。</p>
 *
 * <p><strong>Error Code Ranges | 错误码范围:</strong></p>
 * <ul>
 *   <li>1xxx - Tensor errors | 张量错误</li>
 *   <li>2xxx - Operator errors | 算子错误</li>
 *   <li>3xxx - Model errors | 模型错误</li>
 *   <li>4xxx - Session errors | 会话错误</li>
 *   <li>5xxx - Internal errors | 内部错误</li>
 *   <li>6xxx - Loss function errors | 损失函数错误</li>
 *   <li>7xxx - Initialization errors | 初始化错误</li>
 *   <li>8xxx - Normalization errors | 归一化错误</li>
 *   <li>9xxx - Metric errors | 指标错误</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable enum) - 线程安全: 是（不可变枚举）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public enum NeuralErrorCode {

    UNKNOWN(0, "Unknown error", "未知错误"),

    // ==================== Tensor Errors (1xxx) | 张量错误 ====================
    SHAPE_MISMATCH(1001, "Shape mismatch", "形状不匹配"),
    INVALID_SHAPE(1002, "Invalid shape", "无效形状"),
    TYPE_MISMATCH(1003, "Tensor type mismatch", "张量类型不匹配"),
    TENSOR_CLOSED(1004, "Tensor is closed", "张量已关闭"),
    INDEX_OUT_OF_BOUNDS(1005, "Index out of bounds", "索引越界"),
    INVALID_PARAMETERS(1006, "Invalid parameters", "无效参数"),

    // ==================== Operator Errors (2xxx) | 算子错误 ====================
    OP_EXECUTION_FAILED(2001, "Operator execution failed", "算子执行失败"),
    UNSUPPORTED_OP(2002, "Unsupported operator", "不支持的算子"),
    INVALID_OP_ATTRIBUTE(2003, "Invalid operator attribute", "无效算子属性"),

    // ==================== Model Errors (3xxx) | 模型错误 ====================
    MODEL_LOAD_FAILED(3001, "Model load failed", "模型加载失败"),
    MODEL_FORMAT_ERROR(3002, "Model format error", "模型格式错误"),
    MODEL_CHECKSUM_MISMATCH(3003, "Model checksum mismatch", "模型校验和不匹配"),
    MODEL_VERSION_UNSUPPORTED(3004, "Model version unsupported", "不支持的模型版本"),
    GRAPH_EXECUTION_FAILED(3005, "Graph execution failed", "计算图执行失败"),

    // ==================== Session Errors (4xxx) | 会话错误 ====================
    SESSION_CLOSED(4001, "Session is closed", "会话已关闭"),
    SESSION_CONFIG_INVALID(4002, "Session configuration invalid", "会话配置无效"),
    INFERENCE_TIMEOUT(4003, "Inference timeout", "推理超时"),
    MEMORY_LIMIT_EXCEEDED(4004, "Memory limit exceeded", "超出内存限制"),

    // ==================== Internal Errors (5xxx) | 内部错误 ====================
    BLAS_ERROR(5001, "BLAS computation error", "BLAS计算错误"),
    PARALLEL_ERROR(5002, "Parallel execution error", "并行执行错误"),

    // ==================== Loss Errors (6xxx) | 损失函数错误 ====================
    INVALID_LOSS_INPUT(6001, "Invalid loss function input", "无效的损失函数输入"),
    LOSS_COMPUTATION_FAILED(6002, "Loss computation failed", "损失计算失败"),

    // ==================== Init Errors (7xxx) | 初始化错误 ====================
    INVALID_INIT_PARAMS(7001, "Invalid initialization parameters", "无效的初始化参数"),

    // ==================== Normalization Errors (8xxx) | 归一化错误 ====================
    NORMALIZATION_FAILED(8001, "Normalization failed", "归一化失败"),
    NORMALIZER_NOT_FITTED(8002, "Normalizer not fitted", "归一化器未拟合"),

    // ==================== Metric Errors (9xxx) | 指标错误 ====================
    INVALID_METRIC_INPUT(9001, "Invalid metric input", "无效的指标输入");

    private final int code;
    private final String message;
    private final String description;

    NeuralErrorCode(int code, String message, String description) {
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
