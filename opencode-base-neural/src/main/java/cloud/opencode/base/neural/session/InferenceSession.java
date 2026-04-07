package cloud.opencode.base.neural.session;

import cloud.opencode.base.neural.exception.NeuralErrorCode;
import cloud.opencode.base.neural.exception.NeuralException;
import cloud.opencode.base.neural.internal.TensorPool;
import cloud.opencode.base.neural.model.Graph;
import cloud.opencode.base.neural.model.ModelMetadata;
import cloud.opencode.base.neural.model.OcmLoader;
import cloud.opencode.base.neural.model.OcmModel;
import cloud.opencode.base.neural.model.TensorInfo;
import cloud.opencode.base.neural.tensor.Tensor;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Inference Session
 * 推理会话
 *
 * <p>High-level entry point for running neural network inference. Wraps an
 * {@link OcmModel} with session-level configuration (thread pool, memory limits,
 * profiling). Use the static {@code load} methods to create a session from a
 * model file, stream, or byte array.</p>
 * <p>运行神经网络推理的高层入口。将 {@link OcmModel} 与会话级配置
 * （线程池、内存限制、性能分析）结合。使用静态 {@code load} 方法
 * 从模型文件、流或字节数组创建会话。</p>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <ul>
 *   <li>Thread-safe: Conditionally. Concurrent {@link #run(Map)} calls are safe;
 *       do not call {@link #close()} while {@link #run(Map)} is in progress.
 *       条件线程安全：并发 {@link #run(Map)} 调用是安全的；
 *       不要在 {@link #run(Map)} 执行期间调用 {@link #close()}。</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see SessionConfig
 * @see ProfilingResult
 * @see OcmModel
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public final class InferenceSession implements AutoCloseable {

    private final OcmModel model;
    private final SessionConfig config;
    private final TensorPool tensorPool;
    private final ThreadLocal<TensorPool> threadLocalPool;
    private final ReentrantLock closeLock = new ReentrantLock();
    private volatile boolean closed;
    private volatile ProfilingResult lastProfilingResult;

    private InferenceSession(OcmModel model, SessionConfig config) {
        this.model = model;
        this.config = config;
        this.tensorPool = new TensorPool();
        int poolCapacity = config.tensorPoolCapacity();
        this.threadLocalPool = ThreadLocal.withInitial(() -> new TensorPool(poolCapacity));
        this.closed = false;
    }

    // ==================== Load Methods | 加载方法 ====================

    /**
     * Load a model from a file path with default configuration
     * 使用默认配置从文件路径加载模型
     *
     * @param modelPath path to the model file | 模型文件路径
     * @return a new inference session | 新的推理会话
     * @throws NeuralException if the model cannot be loaded | 如果无法加载模型
     */
    public static InferenceSession load(Path modelPath) {
        return load(modelPath, SessionConfig.defaults());
    }

    /**
     * Load a model from a file path with custom configuration
     * 使用自定义配置从文件路径加载模型
     *
     * @param modelPath path to the model file | 模型文件路径
     * @param config    session configuration | 会话配置
     * @return a new inference session | 新的推理会话
     * @throws NeuralException if the model cannot be loaded | 如果无法加载模型
     */
    public static InferenceSession load(Path modelPath, SessionConfig config) {
        if (modelPath == null) {
            throw new NeuralException("modelPath must not be null",
                    NeuralErrorCode.MODEL_LOAD_FAILED);
        }
        if (config == null) {
            throw new NeuralException("config must not be null",
                    NeuralErrorCode.SESSION_CONFIG_INVALID);
        }
        OcmModel model = OcmLoader.load(modelPath);
        return new InferenceSession(model, config);
    }

    /**
     * Load a model from an input stream with default configuration
     * 使用默认配置从输入流加载模型
     *
     * @param stream input stream containing the model data | 包含模型数据的输入流
     * @return a new inference session | 新的推理会话
     * @throws NeuralException if the model cannot be loaded | 如果无法加载模型
     */
    public static InferenceSession load(InputStream stream) {
        if (stream == null) {
            throw new NeuralException("stream must not be null",
                    NeuralErrorCode.MODEL_LOAD_FAILED);
        }
        OcmModel model = OcmLoader.load(stream);
        return new InferenceSession(model, SessionConfig.defaults());
    }

    /**
     * Load a model from a byte array with default configuration
     * 使用默认配置从字节数组加载模型
     *
     * @param modelData byte array containing the model data | 包含模型数据的字节数组
     * @return a new inference session | 新的推理会话
     * @throws NeuralException if the model cannot be loaded | 如果无法加载模型
     */
    public static InferenceSession load(byte[] modelData) {
        if (modelData == null) {
            throw new NeuralException("modelData must not be null",
                    NeuralErrorCode.MODEL_LOAD_FAILED);
        }
        OcmModel model = OcmLoader.load(modelData);
        return new InferenceSession(model, SessionConfig.defaults());
    }

    // ==================== Inference | 推理 ====================

    /**
     * Run inference on the given inputs
     * 对给定输入运行推理
     *
     * <p>Thread-safe: each call creates its own result map. Model weights are immutable
     * and graph execution is designed for concurrent access.</p>
     * <p>线程安全：每次调用创建自己的结果映射。模型权重不可变，
     * 计算图执行设计为支持并发访问。</p>
     *
     * @param inputs map of input name to tensor | 输入名称到张量的映射
     * @return map of output name to tensor | 输出名称到张量的映射
     * @throws NeuralException if the session is closed or inference fails |
     *                         如果会话已关闭或推理失败
     */
    public Map<String, Tensor> run(Map<String, Tensor> inputs) {
        ensureOpen();
        if (inputs == null) {
            throw new NeuralException("inputs must not be null",
                    NeuralErrorCode.INVALID_PARAMETERS);
        }

        Graph graph = model.graph();
        // Thread-local pool ensures thread safety for concurrent run() calls
        // while avoiding per-call allocation overhead.
        // Data leakage between requests is prevented by TensorPool.acquire() which
        // zeros arrays before returning them (Arrays.fill(0.0f)), so stale data from
        // a previous inference call is never visible to the next caller.
        TensorPool callPool = threadLocalPool.get();

        if (config.enableProfiling()) {
            long startNanos = System.nanoTime();
            Map<String, Tensor> outputs = graph.execute(inputs, callPool);
            long elapsed = System.nanoTime() - startNanos;

            var timing = new ProfilingResult.OpTiming("graph", "FullInference", elapsed);
            lastProfilingResult = new ProfilingResult(List.of(timing), elapsed);
            return outputs;
        }

        return graph.execute(inputs, callPool);
    }

    // ==================== Info | 信息 ====================

    /**
     * Get model metadata
     * 获取模型元数据
     *
     * @return model metadata | 模型元数据
     * @throws NeuralException if the session is closed | 如果会话已关闭
     */
    public ModelMetadata metadata() {
        ensureOpen();
        return model.metadata();
    }

    /**
     * Get input tensor info
     * 获取输入张量信息
     *
     * @return list of input tensor descriptors | 输入张量描述符列表
     * @throws NeuralException if the session is closed | 如果会话已关闭
     */
    public List<TensorInfo> inputInfo() {
        ensureOpen();
        return model.inputInfo();
    }

    /**
     * Get output tensor info
     * 获取输出张量信息
     *
     * @return list of output tensor descriptors | 输出张量描述符列表
     * @throws NeuralException if the session is closed | 如果会话已关闭
     */
    public List<TensorInfo> outputInfo() {
        ensureOpen();
        return model.outputInfo();
    }

    // ==================== Diagnostics | 诊断 ====================

    /**
     * Warm up the session by running inference with dummy inputs
     * 使用虚拟输入运行推理来预热会话
     *
     * <p>Creates zero-filled tensors matching the expected input shapes and runs
     * inference {@code iterations} times. Useful for JIT warmup and memory pre-allocation.</p>
     * <p>创建与预期输入形状匹配的全零张量，并运行推理 {@code iterations} 次。
     * 适用于 JIT 预热和内存预分配。</p>
     *
     * @param iterations number of warmup iterations (must be &gt; 0) | 预热迭代次数（必须大于0）
     * @throws NeuralException if the session is closed or warmup fails |
     *                         如果会话已关闭或预热失败
     */
    public void warmup(int iterations) {
        ensureOpen();
        if (iterations <= 0) {
            throw new NeuralException(
                    "warmup iterations must be > 0, got: " + iterations,
                    NeuralErrorCode.INVALID_PARAMETERS);
        }

        // Build dummy inputs from inputInfo
        List<TensorInfo> infos = model.inputInfo();
        Map<String, Tensor> dummyInputs = new HashMap<>(infos.size());
        for (TensorInfo info : infos) {
            dummyInputs.put(info.name(), Tensor.zeros(info.shape()));
        }

        try {
            for (int i = 0; i < iterations; i++) {
                Map<String, Tensor> outputs = run(dummyInputs);
                // Close output tensors to avoid resource leaks
                for (Tensor output : outputs.values()) {
                    output.close();
                }
            }
        } finally {
            // Close dummy input tensors
            for (Tensor input : dummyInputs.values()) {
                input.close();
            }
        }
    }

    /**
     * Get the last profiling result
     * 获取最后一次性能分析结果
     *
     * <p>Returns {@code null} if profiling is not enabled or no inference has been run yet.</p>
     * <p>如果未启用性能分析或尚未运行推理，则返回 {@code null}。</p>
     *
     * @return the last profiling result, or null | 最后一次性能分析结果，或 null
     */
    public ProfilingResult lastProfilingResult() {
        return lastProfilingResult;
    }

    // ==================== Lifecycle | 生命周期 ====================

    /**
     * Close this session, releasing the underlying model and resources
     * 关闭此会话，释放底层模型和资源
     *
     * <p>Idempotent: calling close multiple times has no additional effect.</p>
     * <p>幂等：多次调用 close 不会产生额外影响。</p>
     */
    @Override
    public void close() {
        if (closed) {
            return;
        }
        closeLock.lock();
        try {
            if (!closed) {
                closed = true;
                tensorPool.clear();
                model.close();
            }
        } finally {
            closeLock.unlock();
        }
    }

    /**
     * Check if this session has been closed
     * 检查此会话是否已关闭
     *
     * @return true if closed | 如果已关闭则返回 true
     */
    public boolean isClosed() {
        return closed;
    }

    // ==================== Internal | 内部方法 ====================

    private void ensureOpen() {
        if (closed) {
            throw new NeuralException("InferenceSession is closed",
                    NeuralErrorCode.SESSION_CLOSED);
        }
    }
}
