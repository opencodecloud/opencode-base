package cloud.opencode.base.io.stream;

import cloud.opencode.base.io.exception.OpenIOOperationException;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Tee Input Stream
 * 分流输入流
 *
 * <p>Input stream that copies all data read to an output stream.
 * Similar to the Unix 'tee' command.</p>
 * <p>将读取的所有数据复制到输出流的输入流。
 * 类似于Unix的'tee'命令。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Duplicate stream data - 复制流数据</li>
 *   <li>Optional close branch - 可选关闭分支</li>
 *   <li>Logging and debugging - 日志和调试</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ByteArrayOutputStream copy = new ByteArrayOutputStream();
 * try (TeeInputStream tee = new TeeInputStream(input, copy)) {
 *     process(tee); // Read from tee
 * }
 * byte[] data = copy.toByteArray(); // Get copy of data
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
public class TeeInputStream extends FilterInputStream {

    private final OutputStream branch;
    private final boolean closeBranch;

    /**
     * Creates a tee input stream
     * 创建分流输入流
     *
     * @param in     the underlying input stream | 底层输入流
     * @param branch the output stream to tee to | 分流到的输出流
     */
    public TeeInputStream(InputStream in, OutputStream branch) {
        this(in, branch, true);
    }

    /**
     * Creates a tee input stream with close option
     * 创建带关闭选项的分流输入流
     *
     * @param in          the underlying input stream | 底层输入流
     * @param branch      the output stream to tee to | 分流到的输出流
     * @param closeBranch whether to close branch on close | 关闭时是否关闭分支
     */
    public TeeInputStream(InputStream in, OutputStream branch, boolean closeBranch) {
        super(in);
        this.branch = branch;
        this.closeBranch = closeBranch;
    }

    @Override
    public int read() throws IOException {
        int result = super.read();
        if (result != -1 && branch != null) {
            branch.write(result);
        }
        return result;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int result = super.read(b, off, len);
        if (result > 0 && branch != null) {
            branch.write(b, off, result);
        }
        return result;
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            if (closeBranch && branch != null) {
                try {
                    branch.close();
                } catch (IOException e) {
                    // Ignore close exception for branch
                }
            }
        }
    }

    /**
     * Gets the branch output stream
     * 获取分支输出流
     *
     * @return branch output stream | 分支输出流
     */
    public OutputStream getBranch() {
        return branch;
    }

    /**
     * Flushes the branch output stream
     * 刷新分支输出流
     */
    public void flushBranch() {
        if (branch != null) {
            try {
                branch.flush();
            } catch (IOException e) {
                throw OpenIOOperationException.writeFailed(e);
            }
        }
    }
}
