package cloud.opencode.base.neural.model;

import cloud.opencode.base.neural.exception.ModelFormatException;
import cloud.opencode.base.neural.tensor.Tensor;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * OCM File Writer
 * OCM 文件写入器
 *
 * <p>Serializes an {@link OcmModel} to the OpenCode Model (.ocm) binary format.
 * The format consists of a fixed-size header followed by metadata strings,
 * graph structure, and raw weight data.</p>
 * <p>将 {@link OcmModel} 序列化为 OpenCode 模型（.ocm）二进制格式。
 * 该格式由固定大小的头部、元数据字符串、计算图结构和原始权重数据组成。</p>
 *
 * <p><strong>Serialization Layout | 序列化布局:</strong></p>
 * <ol>
 *   <li>Header (64 bytes): magic, version, reserved - 头部</li>
 *   <li>Metadata: length-prefixed UTF-8 strings (name, author, description) + createdAt -
 *       元数据</li>
 *   <li>Graph inputs: count, then per-input (name, type ordinal, shape dims) -
 *       计算图输入</li>
 *   <li>Graph outputs: count, then per-output (name, nodeIndex, outputSlot) -
 *       计算图输出</li>
 *   <li>Graph nodes: count, then per-node (name, opType, attrs, inputSources, weights) -
 *       计算图节点</li>
 * </ol>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see OcmLoader
 * @see OcmFormat
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public final class OcmWriter {

    private OcmWriter() {
        throw new AssertionError("No OcmWriter instances");
    }

    /**
     * Write a model to a file path
     * 将模型写入文件路径
     *
     * @param model the model to write | 要写入的模型
     * @param path  target file path | 目标文件路径
     * @throws ModelFormatException if writing fails | 写入失败时抛出
     */
    public static void write(OcmModel model, Path path) {
        Objects.requireNonNull(model, "model must not be null");
        Objects.requireNonNull(path, "path must not be null");
        try (OutputStream os = Files.newOutputStream(path)) {
            os.write(write(model));
        } catch (IOException e) {
            throw new ModelFormatException("Failed to write model to " + path, e);
        }
    }

    /**
     * Write a model to a byte array
     * 将模型写入字节数组
     *
     * @param model the model to write | 要写入的模型
     * @return serialized model bytes | 序列化的模型字节
     * @throws ModelFormatException if writing fails | 写入失败时抛出
     */
    public static byte[] write(OcmModel model) {
        Objects.requireNonNull(model, "model must not be null");
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            // 1. Write header (64 bytes)
            writeHeader(dos);

            // 2. Write metadata
            ModelMetadata meta = model.metadata();
            writeString(dos, meta.name());
            writeString(dos, meta.author());
            writeString(dos, meta.description());
            dos.writeLong(meta.createdAt());

            Graph graph = model.graph();

            // 3. Write graph inputs
            List<GraphInput> inputs = graph.inputs();
            dos.writeInt(inputs.size());
            for (GraphInput input : inputs) {
                writeString(dos, input.name());
                dos.writeInt(input.type().ordinal());
                writeShape(dos, input.shape());
            }

            // 4. Write graph outputs
            List<GraphOutput> outputs = graph.outputs();
            dos.writeInt(outputs.size());
            for (GraphOutput output : outputs) {
                writeString(dos, output.name());
                dos.writeInt(output.nodeIndex());
                dos.writeInt(output.outputSlot());
            }

            // 5. Write graph nodes
            List<GraphNode> nodes = graph.nodes();
            dos.writeInt(nodes.size());
            for (GraphNode node : nodes) {
                writeString(dos, node.name());
                writeString(dos, node.opType());

                // Write input sources
                int[][] sources = node.inputSources();
                dos.writeInt(sources.length);
                for (int[] source : sources) {
                    dos.writeInt(source[0]);
                    dos.writeInt(source[1]);
                }

                // Write weights
                Tensor[] weights = node.weights();
                dos.writeInt(weights.length);
                for (Tensor w : weights) {
                    // Write shape
                    writeShape(dos, w.shape());
                    // Write data
                    float[] data = w.toFloatArray();
                    dos.writeInt(data.length);
                    for (float f : data) {
                        dos.writeFloat(f);
                    }
                }
            }

            dos.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new ModelFormatException("Failed to serialize model", e);
        }
    }

    /**
     * Write the 64-byte header
     */
    private static void writeHeader(DataOutputStream dos) throws IOException {
        dos.writeInt(OcmFormat.MAGIC);
        dos.writeShort(OcmFormat.VERSION_MAJOR);
        dos.writeShort(OcmFormat.VERSION_MINOR);
        // Pad remaining header bytes (64 - 4 - 2 - 2 = 56 bytes)
        byte[] padding = new byte[OcmFormat.HEADER_SIZE - 8];
        dos.write(padding);
    }

    /**
     * Write a length-prefixed UTF-8 string
     */
    private static void writeString(DataOutputStream dos, String s) throws IOException {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        dos.writeInt(bytes.length);
        dos.write(bytes);
    }

    /**
     * Write a shape (rank + dims)
     */
    private static void writeShape(DataOutputStream dos, cloud.opencode.base.neural.tensor.Shape shape)
            throws IOException {
        int[] dims = shape.dims();
        dos.writeInt(dims.length);
        for (int dim : dims) {
            dos.writeInt(dim);
        }
    }
}
