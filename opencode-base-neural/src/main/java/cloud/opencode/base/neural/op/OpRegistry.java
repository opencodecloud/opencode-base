package cloud.opencode.base.neural.op;

import cloud.opencode.base.neural.exception.NeuralErrorCode;
import cloud.opencode.base.neural.exception.OpExecutionException;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Operator Registry
 * 算子注册表
 *
 * <p>Central registry for neural network operators. Built-in operators are registered
 * at startup; custom operators can be registered at runtime via {@link #register}.</p>
 * <p>神经网络算子的中央注册表。内置算子在启动时注册；
 * 自定义算子可通过 {@link #register} 在运行时注册。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Thread-safe concurrent registration - 线程安全的并发注册</li>
 *   <li>Factory pattern: operators instantiated on demand - 工厂模式：算子按需实例化</li>
 *   <li>Runtime extensibility for custom operators - 运行时可扩展自定义算子</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Register custom operator
 * OpRegistry.register("HardSwish", HardSwishOp::new);
 *
 * // Create operator instance
 * Op op = OpRegistry.create("ReLU");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (ConcurrentHashMap) - 线程安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public final class OpRegistry {

    private static final Map<String, Supplier<Op>> REGISTRY = new ConcurrentHashMap<>();

    private OpRegistry() {
        throw new AssertionError("No OpRegistry instances");
    }

    static {
        // CNN operators
        register("Conv1D", Conv1DOp::new);
        register("Conv2D", Conv2DOp::new);
        register("DepthwiseConv2D", DepthwiseConv2DOp::new);
        register("MaxPool2D", MaxPool2DOp::new);
        register("AvgPool2D", AvgPool2DOp::new);
        register("BatchNorm", BatchNormOp::new);
        register("Linear", LinearOp::new);

        // Activation operators
        register("ReLU", ReluOp::new);
        register("Sigmoid", SigmoidOp::new);
        register("Tanh", TanhOp::new);
        register("Softmax", SoftmaxOp::new);
        register("HardSigmoid", HardSigmoidOp::new);
        register("HardSwish", HardSwishOp::new);
        register("LeakyReLU", LeakyReluOp::new);
        register("ELU", EluOp::new);
        register("SELU", SeluOp::new);
        register("GELU", GeluOp::new);
        register("Swish", SwishOp::new);
        register("Mish", MishOp::new);
        register("Softplus", SoftplusOp::new);

        // Sequence operators
        register("LSTM", LstmOp::new);
        register("BiLSTM", BiLstmOp::new);
        register("CTCDecode", CtcDecodeOp::new);
        register("CTCBeamSearch", CtcBeamSearchOp::new);

        // Structure operators
        register("Flatten", FlattenOp::new);
        register("Reshape", ReshapeOp::new);
        register("Dropout", DropoutOp::new);
        register("Add", AddOp::new);
        register("Concat", ConcatOp::new);
        register("GlobalAvgPool", GlobalAvgPoolOp::new);
    }

    /**
     * Register an operator factory
     * 注册算子工厂
     *
     * @param opType  the operator type name | 算子类型名称
     * @param factory the operator factory | 算子工厂
     * @throws IllegalStateException if opType is already registered | 当算子类型已注册时抛出
     */
    public static void register(String opType, Supplier<Op> factory) {
        Objects.requireNonNull(opType, "opType must not be null");
        Objects.requireNonNull(factory, "factory must not be null");
        if (REGISTRY.putIfAbsent(opType, factory) != null) {
            throw new IllegalStateException("Operator already registered: " + opType);
        }
    }

    /**
     * Create an operator instance by type
     * 按类型创建算子实例
     *
     * @param opType the operator type name | 算子类型名称
     * @return a new operator instance | 新算子实例
     * @throws OpExecutionException if opType is not registered | 当算子类型未注册时抛出
     */
    public static Op create(String opType) {
        Objects.requireNonNull(opType, "opType must not be null");
        Supplier<Op> factory = REGISTRY.get(opType);
        if (factory == null) {
            throw new OpExecutionException(
                    "Unsupported operator: " + opType, NeuralErrorCode.UNSUPPORTED_OP);
        }
        return factory.get();
    }

    /**
     * Check if an operator type is supported
     * 检查算子类型是否已支持
     *
     * @param opType the operator type name | 算子类型名称
     * @return true if registered | 已注册返回 true
     */
    public static boolean isSupported(String opType) {
        return opType != null && REGISTRY.containsKey(opType);
    }

    /**
     * Get all registered operator type names
     * 获取所有已注册的算子类型名称
     *
     * @return unmodifiable set of type names | 类型名称的不可修改集合
     */
    public static Set<String> registeredTypes() {
        return Set.copyOf(REGISTRY.keySet());
    }

    /**
     * Unregister an operator (for testing only)
     * 取消注册算子（仅用于测试）
     *
     * @param opType the operator type name | 算子类型名称
     */
    static void unregister(String opType) {
        REGISTRY.remove(opType);
    }
}
