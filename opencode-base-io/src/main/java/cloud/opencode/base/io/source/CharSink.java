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
import java.util.stream.Stream;

/**
 * Abstract Character Sink - A writable destination for characters
 * 抽象字符接收器 - 可写的字符目标
 *
 * <p>An abstraction over different destinations for character data.
 * Counterpart to {@link CharSource}.</p>
 * <p>对不同字符数据目标的抽象。是{@link CharSource}的对应物。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Unified character writing - 统一的字符写入</li>
 *   <li>Write from Reader - 从Reader写入</li>
 *   <li>Write strings and character sequences - 写入字符串和字符序列</li>
 *   <li>Write lines - 写入多行</li>
 *   <li>Support for append mode - 支持追加模式</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Write to file
 * CharSink sink = CharSink.toPath(Paths.get("output.txt"));
 * sink.write("Hello, World!");
 *
 * // Write lines
 * sink.writeLines(List.of("Line 1", "Line 2", "Line 3"));
 *
 * // Copy from source to sink
 * CharSource source = CharSource.fromPath(Paths.get("input.txt"));
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
 * @see CharSource
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.2.0
 */
public abstract class CharSink {

    private static final String LINE_SEPARATOR = System.lineSeparator();

    /**
     * Opens a new Writer for writing.
     * 打开新的Writer进行写入。
     *
     * <p>The caller is responsible for closing the writer.</p>
     * <p>调用者负责关闭Writer。</p>
     *
     * @return a new Writer | 新的Writer
     * @throws OpenIOOperationException if the writer cannot be opened | 如果Writer无法打开
     */
    public abstract Writer openStream();

    /**
     * Opens a new BufferedWriter for writing.
     * 打开新的BufferedWriter进行写入。
     *
     * @return a new BufferedWriter | 新的BufferedWriter
     * @throws OpenIOOperationException if the writer cannot be opened | 如果Writer无法打开
     */
    public BufferedWriter openBufferedStream() {
        Writer writer = openStream();
        return (writer instanceof BufferedWriter bw) ? bw : new BufferedWriter(writer);
    }

    /**
     * Writes a character sequence.
     * 写入字符序列。
     *
     * @param charSequence the character sequence to write | 要写入的字符序列
     * @throws OpenIOOperationException if writing fails | 如果写入失败
     */
    public void write(CharSequence charSequence) {
        Objects.requireNonNull(charSequence, "charSequence must not be null");

        try (Writer writer = openStream()) {
            writer.append(charSequence);
        } catch (IOException e) {
            throw new OpenIOOperationException("Failed to write character sequence to CharSink", e);
        }
    }

    /**
     * Writes all characters from a Reader.
     * 从Reader写入所有字符。
     *
     * @param reader the reader | Reader
     * @return the number of characters written | 写入的字符数
     * @throws OpenIOOperationException if writing fails | 如果写入失败
     */
    public long writeFrom(Reader reader) {
        Objects.requireNonNull(reader, "reader must not be null");

        try (Writer writer = openStream()) {
            long count = 0;
            char[] buf = new char[8192];
            int read;
            while ((read = reader.read(buf)) != -1) {
                writer.write(buf, 0, read);
                count += read;
            }
            return count;
        } catch (IOException e) {
            throw new OpenIOOperationException("Failed to write from Reader to CharSink", e);
        }
    }

    /**
     * Writes lines with the system line separator.
     * 使用系统行分隔符写入多行。
     *
     * @param lines the lines to write | 要写入的行
     * @throws OpenIOOperationException if writing fails | 如果写入失败
     */
    public void writeLines(Iterable<? extends CharSequence> lines) {
        writeLines(lines, LINE_SEPARATOR);
    }

    /**
     * Writes lines with a custom line separator.
     * 使用自定义行分隔符写入多行。
     *
     * @param lines         the lines to write | 要写入的行
     * @param lineSeparator the line separator | 行分隔符
     * @throws OpenIOOperationException if writing fails | 如果写入失败
     */
    public void writeLines(Iterable<? extends CharSequence> lines, String lineSeparator) {
        Objects.requireNonNull(lines, "lines must not be null");
        Objects.requireNonNull(lineSeparator, "lineSeparator must not be null");

        try (Writer writer = openBufferedStream()) {
            boolean first = true;
            for (CharSequence line : lines) {
                if (!first) {
                    writer.append(lineSeparator);
                }
                writer.append(line);
                first = false;
            }
        } catch (IOException e) {
            throw new OpenIOOperationException("Failed to write lines to CharSink", e);
        }
    }

    /**
     * Writes lines from a stream with the system line separator.
     * 使用系统行分隔符从流写入多行。
     *
     * @param lines the line stream | 行流
     * @throws OpenIOOperationException if writing fails | 如果写入失败
     */
    public void writeLines(Stream<? extends CharSequence> lines) {
        writeLines(lines, LINE_SEPARATOR);
    }

    /**
     * Writes lines from a stream with a custom line separator.
     * 使用自定义行分隔符从流写入多行。
     *
     * @param lines         the line stream | 行流
     * @param lineSeparator the line separator | 行分隔符
     * @throws OpenIOOperationException if writing fails | 如果写入失败
     */
    public void writeLines(Stream<? extends CharSequence> lines, String lineSeparator) {
        Objects.requireNonNull(lines, "lines must not be null");
        Objects.requireNonNull(lineSeparator, "lineSeparator must not be null");

        try (Writer writer = openBufferedStream()) {
            boolean[] first = {true};
            lines.forEach(line -> {
                try {
                    if (!first[0]) {
                        writer.append(lineSeparator);
                    }
                    writer.append(line);
                    first[0] = false;
                } catch (IOException e) {
                    throw new OpenIOOperationException("Failed to write line to CharSink", e);
                }
            });
        } catch (IOException e) {
            throw new OpenIOOperationException("Failed to write lines to CharSink", e);
        }
    }

    // ==================== Factory Methods ====================

    /**
     * Creates a CharSink for writing to a file path with UTF-8 encoding.
     * 创建用于写入文件路径的使用UTF-8编码的CharSink。
     *
     * @param path the file path | 文件路径
     * @return a CharSink | CharSink
     */
    public static CharSink toPath(Path path) {
        return toPath(path, StandardCharsets.UTF_8);
    }

    /**
     * Creates a CharSink for writing to a file path.
     * 创建用于写入文件路径的CharSink。
     *
     * @param path    the file path | 文件路径
     * @param charset the charset | 字符集
     * @return a CharSink | CharSink
     */
    public static CharSink toPath(Path path, Charset charset) {
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(charset, "charset must not be null");
        return new PathCharSink(path, charset, false);
    }

    /**
     * Creates a CharSink for appending to a file path with UTF-8 encoding.
     * 创建用于追加到文件路径的使用UTF-8编码的CharSink。
     *
     * @param path the file path | 文件路径
     * @return a CharSink | CharSink
     */
    public static CharSink toPathAppend(Path path) {
        return toPathAppend(path, StandardCharsets.UTF_8);
    }

    /**
     * Creates a CharSink for appending to a file path.
     * 创建用于追加到文件路径的CharSink。
     *
     * @param path    the file path | 文件路径
     * @param charset the charset | 字符集
     * @return a CharSink | CharSink
     */
    public static CharSink toPathAppend(Path path, Charset charset) {
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(charset, "charset must not be null");
        return new PathCharSink(path, charset, true);
    }

    /**
     * Creates a CharSink that discards all written data.
     * 创建丢弃所有写入数据的CharSink。
     *
     * @return a null CharSink | 空CharSink
     */
    public static CharSink nullSink() {
        return NullCharSink.INSTANCE;
    }

    // ==================== Implementation Classes ====================

    private static final class PathCharSink extends CharSink {
        private final Path path;
        private final Charset charset;
        private final boolean append;

        PathCharSink(Path path, Charset charset, boolean append) {
            this.path = path;
            this.charset = charset;
            this.append = append;
        }

        @Override
        public Writer openStream() {
            try {
                OpenOption[] options = append
                        ? new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.APPEND}
                        : new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING};
                return Files.newBufferedWriter(path, charset, options);
            } catch (IOException e) {
                throw new OpenIOOperationException("Failed to open file for writing: " + path, e);
            }
        }

        @Override
        public void write(CharSequence charSequence) {
            try {
                if (append) {
                    // For append, use the standard approach
                    super.write(charSequence);
                } else {
                    // For non-append, use Files.writeString for efficiency
                    Files.writeString(path, charSequence, charset);
                }
            } catch (IOException e) {
                throw new OpenIOOperationException("Failed to write to file: " + path, e);
            }
        }

        @Override
        public String toString() {
            return append
                    ? "CharSink.toPathAppend(" + path + ", " + charset + ")"
                    : "CharSink.toPath(" + path + ", " + charset + ")";
        }
    }

    private static final class NullCharSink extends CharSink {
        static final NullCharSink INSTANCE = new NullCharSink();

        @Override
        public Writer openStream() {
            return new Writer() {
                @Override
                public void write(char[] cbuf, int off, int len) {
                    // Discard
                }

                @Override
                public void flush() {
                    // No-op
                }

                @Override
                public void close() {
                    // No-op
                }

                @Override
                public String toString() {
                    return "NullWriter";
                }
            };
        }

        @Override
        public void write(CharSequence charSequence) {
            // Discard
        }

        @Override
        public long writeFrom(Reader reader) {
            Objects.requireNonNull(reader, "reader must not be null");
            try {
                long count = 0;
                char[] buf = new char[8192];
                int read;
                while ((read = reader.read(buf)) != -1) {
                    count += read;
                }
                return count;
            } catch (IOException e) {
                throw new OpenIOOperationException("Failed to read from Reader", e);
            }
        }

        @Override
        public void writeLines(Iterable<? extends CharSequence> lines, String lineSeparator) {
            // Discard
        }

        @Override
        public void writeLines(Stream<? extends CharSequence> lines, String lineSeparator) {
            Objects.requireNonNull(lines, "lines must not be null");
            lines.forEach(_ -> {});
        }

        @Override
        public String toString() {
            return "CharSink.nullSink()";
        }
    }
}
