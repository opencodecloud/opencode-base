/*
 * Copyright 2025 OpenCode Cloud Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.opencode.base.io.source;

import cloud.opencode.base.io.exception.OpenIOOperationException;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/**
 * Abstract Byte Sink - A writable destination for bytes
 * 抽象字节接收器 - 可写的字节目标
 *
 * <p>An abstraction over different destinations for byte data.
 * Counterpart to {@link ByteSource}.</p>
 * <p>对不同字节数据目标的抽象。是{@link ByteSource}的对应物。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Unified byte writing - 统一的字节写入</li>
 *   <li>Write from InputStream - 从输入流写入</li>
 *   <li>Write byte arrays - 写入字节数组</li>
 *   <li>Support for append mode - 支持追加模式</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Write to file
 * ByteSink sink = ByteSink.toPath(Paths.get("output.bin"));
 * sink.write(data);
 *
 * // Copy from source to sink
 * ByteSource source = ByteSource.fromPath(Paths.get("input.bin"));
 * source.copyTo(sink);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No, not safe for concurrent writes - 线程安全: 否，不支持并发写入</li>
 *   <li>Null-safe: No, data must not be null - 空值安全: 否，数据不可为null</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see ByteSource
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.2.0
 */
public abstract class ByteSink {

    /**
     * Opens a new OutputStream for writing.
     * 打开新的输出流进行写入。
     *
     * <p>The caller is responsible for closing the stream.</p>
     * <p>调用者负责关闭流。</p>
     *
     * @return a new OutputStream | 新的输出流
     * @throws OpenIOOperationException if the stream cannot be opened | 如果流无法打开
     */
    public abstract OutputStream openStream();

    /**
     * Returns a view of this sink as a CharSink with the given charset.
     * 返回使用给定字符集作为CharSink的此接收器视图。
     *
     * @param charset the charset to use | 要使用的字符集
     * @return a CharSink view | CharSink视图
     */
    public CharSink asCharSink(Charset charset) {
        Objects.requireNonNull(charset, "charset must not be null");
        return new CharSink() {
            @Override
            public Writer openStream() {
                return new OutputStreamWriter(ByteSink.this.openStream(), charset);
            }

            @Override
            public String toString() {
                return ByteSink.this.toString() + ".asCharSink(" + charset + ")";
            }
        };
    }

    /**
     * Returns a view of this sink as a CharSink with UTF-8 charset.
     * 返回使用UTF-8字符集作为CharSink的此接收器视图。
     *
     * @return a CharSink view | CharSink视图
     */
    public CharSink asCharSink() {
        return asCharSink(StandardCharsets.UTF_8);
    }

    /**
     * Writes all bytes from a byte array.
     * 从字节数组写入所有字节。
     *
     * @param bytes the bytes to write | 要写入的字节
     * @throws OpenIOOperationException if writing fails | 如果写入失败
     */
    public void write(byte[] bytes) {
        Objects.requireNonNull(bytes, "bytes must not be null");

        try (OutputStream out = openStream()) {
            out.write(bytes);
        } catch (IOException e) {
            throw new OpenIOOperationException("Failed to write bytes to ByteSink", e);
        }
    }

    /**
     * Writes all bytes from an InputStream.
     * 从输入流写入所有字节。
     *
     * @param input the input stream | 输入流
     * @return the number of bytes written | 写入的字节数
     * @throws OpenIOOperationException if writing fails | 如果写入失败
     */
    public long writeFrom(InputStream input) {
        Objects.requireNonNull(input, "input must not be null");

        try (OutputStream out = openStream()) {
            return input.transferTo(out);
        } catch (IOException e) {
            throw new OpenIOOperationException("Failed to write from InputStream to ByteSink", e);
        }
    }

    // ==================== Factory Methods ====================

    /**
     * Creates a ByteSink for writing to a file path.
     * 创建用于写入文件路径的ByteSink。
     *
     * @param path the file path | 文件路径
     * @return a ByteSink | ByteSink
     */
    public static ByteSink toPath(Path path) {
        Objects.requireNonNull(path, "path must not be null");
        return new PathByteSink(path, false);
    }

    /**
     * Creates a ByteSink for appending to a file path.
     * 创建用于追加到文件路径的ByteSink。
     *
     * @param path the file path | 文件路径
     * @return a ByteSink | ByteSink
     */
    public static ByteSink toPathAppend(Path path) {
        Objects.requireNonNull(path, "path must not be null");
        return new PathByteSink(path, true);
    }

    /**
     * Creates a ByteSink that discards all written data.
     * 创建丢弃所有写入数据的ByteSink。
     *
     * @return a null ByteSink | 空ByteSink
     */
    public static ByteSink nullSink() {
        return NullByteSink.INSTANCE;
    }

    // ==================== Implementation Classes ====================

    private static final class PathByteSink extends ByteSink {
        private final Path path;
        private final boolean append;

        PathByteSink(Path path, boolean append) {
            this.path = path;
            this.append = append;
        }

        @Override
        public OutputStream openStream() {
            try {
                OpenOption[] options = append
                        ? new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.APPEND}
                        : new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING};
                return Files.newOutputStream(path, options);
            } catch (IOException e) {
                throw new OpenIOOperationException("Failed to open file for writing: " + path, e);
            }
        }

        @Override
        public String toString() {
            return append
                    ? "ByteSink.toPathAppend(" + path + ")"
                    : "ByteSink.toPath(" + path + ")";
        }
    }

    private static final class NullByteSink extends ByteSink {
        static final NullByteSink INSTANCE = new NullByteSink();

        @Override
        public OutputStream openStream() {
            return new OutputStream() {
                @Override
                public void write(int b) {
                    // Discard
                }

                @Override
                public void write(byte[] b, int off, int len) {
                    // Discard
                }

                @Override
                public String toString() {
                    return "NullOutputStream";
                }
            };
        }

        @Override
        public void write(byte[] bytes) {
            // Discard
        }

        @Override
        public long writeFrom(InputStream input) {
            Objects.requireNonNull(input, "input must not be null");
            try {
                long count = 0;
                byte[] buf = new byte[8192];
                int read;
                while ((read = input.read(buf)) != -1) {
                    count += read;
                }
                return count;
            } catch (IOException e) {
                throw new OpenIOOperationException("Failed to read from InputStream", e);
            }
        }

        @Override
        public String toString() {
            return "ByteSink.nullSink()";
        }
    }
}
