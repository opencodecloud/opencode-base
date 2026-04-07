package cloud.opencode.base.neural.model;

import cloud.opencode.base.neural.exception.ModelFormatException;
import cloud.opencode.base.neural.exception.ModelLoadException;
import cloud.opencode.base.neural.exception.NeuralErrorCode;
import cloud.opencode.base.neural.op.Op;
import cloud.opencode.base.neural.op.OpAttribute;
import cloud.opencode.base.neural.op.OpRegistry;
import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;
import cloud.opencode.base.neural.tensor.TensorType;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * OCM File Loader
 * OCM 文件加载器
 *
 * <p>Deserializes an {@link OcmModel} from the OpenCode Model (.ocm) binary format.
 * Validates the magic number and version before parsing the model data.
 * Operators are resolved from the {@link OpRegistry} during loading.</p>
 * <p>从 OpenCode 模型（.ocm）二进制格式反序列化 {@link OcmModel}。
 * 在解析模型数据之前验证魔数和版本。
 * 加载期间从 {@link OpRegistry} 解析算子。</p>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see OcmWriter
 * @see OcmFormat
 * @see OpRegistry
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public final class OcmLoader {

    /** Maximum allowed string length to prevent OOM from corrupt data (16 MB) */
    private static final int MAX_STRING_LENGTH = 16 * 1024 * 1024;

    /** Maximum allowed tensor element count to prevent OOM (256M elements = ~1GB float data) */
    private static final int MAX_TENSOR_ELEMENTS = 256 * 1024 * 1024;

    /** Maximum allowed node count */
    private static final int MAX_NODE_COUNT = 100_000;

    /** Maximum total tensor elements across all weights to prevent OOM (~4 GB float data) */
    private static final long MAX_TOTAL_TENSOR_ELEMENTS = 1024L * 1024 * 1024;

    private OcmLoader() {
        throw new AssertionError("No OcmLoader instances");
    }

    /**
     * Load a model from a file path
     * 从文件路径加载模型
     *
     * @param path source file path | 源文件路径
     * @return loaded model | 加载的模型
     * @throws ModelLoadException   if loading fails | 加载失败时抛出
     * @throws ModelFormatException if format is invalid | 格式无效时抛出
     */
    public static OcmModel load(Path path) {
        Objects.requireNonNull(path, "path must not be null");
        try (InputStream is = Files.newInputStream(path)) {
            return load(is);
        } catch (ModelLoadException | ModelFormatException e) {
            throw e;
        } catch (IOException e) {
            throw new ModelLoadException("Failed to load model from " + path, e);
        }
    }

    /**
     * Load a model from a byte array
     * 从字节数组加载模型
     *
     * @param data serialized model bytes | 序列化的模型字节
     * @return loaded model | 加载的模型
     * @throws ModelLoadException   if loading fails | 加载失败时抛出
     * @throws ModelFormatException if format is invalid | 格式无效时抛出
     */
    public static OcmModel load(byte[] data) {
        Objects.requireNonNull(data, "data must not be null");
        return load(new ByteArrayInputStream(data));
    }

    /**
     * Load a model from an input stream
     * 从输入流加载模型
     *
     * @param stream source input stream | 源输入流
     * @return loaded model | 加载的模型
     * @throws ModelLoadException   if loading fails | 加载失败时抛出
     * @throws ModelFormatException if format is invalid | 格式无效时抛出
     */
    public static OcmModel load(InputStream stream) {
        Objects.requireNonNull(stream, "stream must not be null");
        try {
            DataInputStream dis = new DataInputStream(stream);

            // 1. Read and validate header
            readHeader(dis);

            // 2. Read metadata
            String name = readString(dis);
            String author = readString(dis);
            String description = readString(dis);
            long createdAt = dis.readLong();
            ModelMetadata metadata = new ModelMetadata(name, author, description, createdAt);

            // 3. Read graph inputs
            int inputCount = dis.readInt();
            validateCount(inputCount, MAX_NODE_COUNT, "graph input count");
            List<GraphInput> graphInputs = new ArrayList<>(inputCount);
            for (int i = 0; i < inputCount; i++) {
                String inputName = readString(dis);
                int typeOrdinal = dis.readInt();
                TensorType type = tensorTypeFromOrdinal(typeOrdinal);
                Shape shape = readShape(dis);
                graphInputs.add(new GraphInput(inputName, type, shape));
            }

            // 4. Read graph outputs
            int outputCount = dis.readInt();
            validateCount(outputCount, MAX_NODE_COUNT, "graph output count");
            List<GraphOutput> graphOutputs = new ArrayList<>(outputCount);
            for (int i = 0; i < outputCount; i++) {
                String outputName = readString(dis);
                int nodeIndex = dis.readInt();
                int outputSlot = dis.readInt();
                graphOutputs.add(new GraphOutput(outputName, nodeIndex, outputSlot));
            }

            // 5. Read graph nodes
            int nodeCount = dis.readInt();
            validateCount(nodeCount, MAX_NODE_COUNT, "node count");
            List<GraphNode> nodes = new ArrayList<>(nodeCount);
            long totalTensorElements = 0;
            for (int i = 0; i < nodeCount; i++) {
                String nodeName = readString(dis);
                String opType = readString(dis);

                // Resolve operator from registry
                Op op = OpRegistry.create(opType);

                // Read input sources
                int sourceCount = dis.readInt();
                validateCount(sourceCount, MAX_NODE_COUNT, "input source count");
                int[][] inputSources = new int[sourceCount][2];
                for (int s = 0; s < sourceCount; s++) {
                    inputSources[s][0] = dis.readInt();
                    inputSources[s][1] = dis.readInt();
                }

                // Read weights
                int weightCount = dis.readInt();
                validateCount(weightCount, MAX_NODE_COUNT, "weight count");
                Tensor[] weights = new Tensor[weightCount];
                for (int w = 0; w < weightCount; w++) {
                    Shape shape = readShape(dis);
                    int dataLength = dis.readInt();
                    if (dataLength < 0 || dataLength > MAX_TENSOR_ELEMENTS) {
                        throw new ModelFormatException(
                                "Invalid tensor data length: " + dataLength);
                    }
                    if (dataLength != shape.size()) {
                        throw new ModelFormatException(
                                "Tensor data length " + dataLength
                                        + " does not match shape size " + shape.size());
                    }
                    totalTensorElements += dataLength;
                    if (totalTensorElements > MAX_TOTAL_TENSOR_ELEMENTS) {
                        throw new ModelFormatException(
                                "Total tensor elements " + totalTensorElements
                                        + " exceeds maximum " + MAX_TOTAL_TENSOR_ELEMENTS);
                    }
                    float[] data = new float[dataLength];
                    for (int d = 0; d < dataLength; d++) {
                        data[d] = dis.readFloat();
                    }
                    weights[w] = Tensor.fromFloat(data, shape);
                }

                nodes.add(new GraphNode(nodeName, opType, op, OpAttribute.empty(),
                        inputSources, weights));
            }

            // Validate graphOutput nodeIndex references
            for (GraphOutput go : graphOutputs) {
                if (go.nodeIndex() < 0 || go.nodeIndex() >= nodes.size()) {
                    throw new ModelFormatException(
                            "GraphOutput '" + go.name() + "' references node index "
                                    + go.nodeIndex() + " but only " + nodes.size() + " nodes exist");
                }
            }

            // Build graph and model
            Graph graph = new Graph(nodes, graphInputs, graphOutputs, metadata);

            // Build input/output TensorInfos
            List<TensorInfo> inputInfos = graphInputs.stream()
                    .map(gi -> new TensorInfo(gi.name(), gi.type(), gi.shape()))
                    .toList();
            // TODO: Output shape/type are not stored in .ocm format v1.0; these are placeholders.
            // Actual output shapes are determined at inference time via Graph.execute().
            List<TensorInfo> outputInfos = graphOutputs.stream()
                    .map(go -> new TensorInfo(go.name(), TensorType.FLOAT32, Shape.of(1)))
                    .toList();

            return new OcmModel(graph, metadata, inputInfos, outputInfos);
        } catch (ModelLoadException | ModelFormatException e) {
            throw e;
        } catch (IOException e) {
            throw new ModelLoadException("Failed to read model data", e);
        } catch (Exception e) {
            throw new ModelLoadException("Failed to load model: " + e.getMessage(), e);
        }
    }

    /**
     * Read and validate the 64-byte header
     */
    private static void readHeader(DataInputStream dis) throws IOException {
        int magic = dis.readInt();
        if (magic != OcmFormat.MAGIC) {
            throw new ModelFormatException(
                    String.format("Invalid magic number: 0x%08X (expected 0x%08X)",
                            magic, OcmFormat.MAGIC));
        }

        short majorVersion = dis.readShort();
        short minorVersion = dis.readShort();
        if (majorVersion != OcmFormat.VERSION_MAJOR) {
            throw new ModelLoadException(
                    "Unsupported model version: " + majorVersion + "." + minorVersion
                            + " (expected " + OcmFormat.VERSION_MAJOR + ".x)",
                    NeuralErrorCode.MODEL_VERSION_UNSUPPORTED);
        }

        // Skip remaining header padding (56 bytes)
        int remaining = OcmFormat.HEADER_SIZE - 8;
        int skipped = 0;
        while (skipped < remaining) {
            int n = (int) dis.skip(remaining - skipped);
            if (n <= 0) {
                // fallback: read bytes
                dis.readByte();
                n = 1;
            }
            skipped += n;
        }
    }

    /**
     * Read a length-prefixed UTF-8 string
     */
    private static String readString(DataInputStream dis) throws IOException {
        int length = dis.readInt();
        if (length < 0 || length > MAX_STRING_LENGTH) {
            throw new ModelFormatException("Invalid string length: " + length);
        }
        byte[] bytes = new byte[length];
        dis.readFully(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * Read a shape (rank + dims)
     */
    private static Shape readShape(DataInputStream dis) throws IOException {
        int rank = dis.readInt();
        if (rank < 0 || rank > 32) {
            throw new ModelFormatException("Invalid shape rank: " + rank);
        }
        if (rank == 0) {
            return Shape.scalar();
        }
        int[] dims = new int[rank];
        for (int i = 0; i < rank; i++) {
            dims[i] = dis.readInt();
        }
        return Shape.of(dims);
    }

    /**
     * Convert ordinal to TensorType
     */
    private static TensorType tensorTypeFromOrdinal(int ordinal) {
        TensorType[] types = TensorType.values();
        if (ordinal < 0 || ordinal >= types.length) {
            throw new ModelFormatException("Invalid tensor type ordinal: " + ordinal);
        }
        return types[ordinal];
    }

    /**
     * Validate a count value is within bounds
     */
    private static void validateCount(int count, int max, String fieldName) {
        if (count < 0 || count > max) {
            throw new ModelFormatException(
                    "Invalid " + fieldName + ": " + count + " (max " + max + ")");
        }
    }
}
