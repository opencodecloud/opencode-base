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
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

/**
 * Abstract Character Source - A readable source of characters
 * 抽象字符源 - 可读的字符来源
 *
 * <p>An abstraction over different sources of character data. Each instance
 * represents an immutable source that can be read multiple times.</p>
 * <p>对不同字符数据源的抽象。每个实例代表一个可以多次读取的不可变源。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Unified character reading - 统一的字符读取</li>
 *   <li>Read lines as stream - 以流方式读取行</li>
 *   <li>Copy to Writer - 复制到Writer</li>
 *   <li>Content equality checks - 内容相等性检查</li>
 *   <li>Line counting - 行数统计</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // From file
 * CharSource source = CharSource.fromPath(Paths.get("data.txt"));
 * String content = source.read();
 *
 * // Read lines
 * List<String> lines = source.readLines();
 *
 * // Stream lines
 * try (Stream<String> lineStream = source.lines()) {
 *     lineStream.filter(s -> !s.isEmpty()).forEach(System.out::println);
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable source, each read opens new reader) - 线程安全: 是（不可变源，每次读取打开新Reader）</li>
 *   <li>Null-safe: No, arguments must not be null - 空值安全: 否，参数不可为null</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see ByteSource
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.2.0
 */
public abstract class CharSource {

    private static final int DEFAULT_BUFFER_SIZE = 8192;

    /**
     * Opens a new Reader for reading.
     * 打开新的Reader进行读取。
     *
     * <p>The caller is responsible for closing the reader.</p>
     * <p>调用者负责关闭Reader。</p>
     *
     * @return a new Reader | 新的Reader
     * @throws OpenIOOperationException if the reader cannot be opened | 如果Reader无法打开
     */
    public abstract Reader openStream();

    /**
     * Opens a new BufferedReader for reading.
     * 打开新的BufferedReader进行读取。
     *
     * @return a new BufferedReader | 新的BufferedReader
     * @throws OpenIOOperationException if the reader cannot be opened | 如果Reader无法打开
     */
    public BufferedReader openBufferedStream() {
        Reader reader = openStream();
        return (reader instanceof BufferedReader br) ? br : new BufferedReader(reader);
    }

    /**
     * Returns the length of this source in characters, if known.
     * 返回此源的字符长度（如果已知）。
     *
     * @return an Optional containing the length, or empty if unknown | 包含长度的Optional，如果未知则为空
     */
    public Optional<Long> lengthIfKnown() {
        return Optional.empty();
    }

    /**
     * Returns the length of this source in characters.
     * 返回此源的字符长度。
     *
     * <p>If the length is not known, this method reads the entire source.</p>
     * <p>如果长度未知，此方法会读取整个源。</p>
     *
     * @return the length in characters | 字符长度
     * @throws OpenIOOperationException if reading fails | 如果读取失败
     */
    public long length() {
        Optional<Long> known = lengthIfKnown();
        if (known.isPresent()) {
            return known.get();
        }

        try (Reader reader = openStream()) {
            long count = 0;
            char[] buf = new char[DEFAULT_BUFFER_SIZE];
            int read;
            while ((read = reader.read(buf)) != -1) {
                count += read;
            }
            return count;
        } catch (IOException e) {
            throw new OpenIOOperationException("Failed to determine length of CharSource", e);
        }
    }

    /**
     * Reads the entire contents as a string.
     * 将整个内容读取为字符串。
     *
     * @return the content string | 内容字符串
     * @throws OpenIOOperationException if reading fails | 如果读取失败
     */
    public String read() {
        try (Reader reader = openStream()) {
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[DEFAULT_BUFFER_SIZE];
            int read;
            while ((read = reader.read(buf)) != -1) {
                sb.append(buf, 0, read);
            }
            return sb.toString();
        } catch (IOException e) {
            throw new OpenIOOperationException("Failed to read CharSource", e);
        }
    }

    /**
     * Reads the first line.
     * 读取第一行。
     *
     * @return an Optional containing the first line, or empty if the source is empty |
     *         包含第一行的Optional，如果源为空则为空
     * @throws OpenIOOperationException if reading fails | 如果读取失败
     */
    public Optional<String> readFirstLine() {
        try (BufferedReader reader = openBufferedStream()) {
            return Optional.ofNullable(reader.readLine());
        } catch (IOException e) {
            throw new OpenIOOperationException("Failed to read first line from CharSource", e);
        }
    }

    /**
     * Reads all lines as a list.
     * 将所有行读取为列表。
     *
     * @return the list of lines | 行列表
     * @throws OpenIOOperationException if reading fails | 如果读取失败
     */
    public List<String> readLines() {
        try (BufferedReader reader = openBufferedStream()) {
            return reader.lines().toList();
        } catch (IOException e) {
            throw new OpenIOOperationException("Failed to read lines from CharSource", e);
        }
    }

    /**
     * Returns a stream of lines.
     * 返回行的流。
     *
     * <p>The returned stream must be closed after use.</p>
     * <p>返回的流在使用后必须关闭。</p>
     *
     * @return a stream of lines | 行的流
     * @throws OpenIOOperationException if the stream cannot be created | 如果流无法创建
     */
    public Stream<String> lines() {
        BufferedReader reader = openBufferedStream();
        return reader.lines().onClose(() -> {
            try {
                reader.close();
            } catch (IOException e) {
                throw new OpenIOOperationException("Failed to close reader", e);
            }
        });
    }

    /**
     * Counts the number of lines.
     * 统计行数。
     *
     * @return the line count | 行数
     * @throws OpenIOOperationException if reading fails | 如果读取失败
     */
    public long countLines() {
        try (Stream<String> lineStream = lines()) {
            return lineStream.count();
        }
    }

    /**
     * Copies the contents to a Writer.
     * 将内容复制到Writer。
     *
     * @param writer the target Writer | 目标Writer
     * @return the number of characters copied | 复制的字符数
     * @throws OpenIOOperationException if copying fails | 如果复制失败
     */
    public long copyTo(Writer writer) {
        Objects.requireNonNull(writer, "writer must not be null");

        try (Reader reader = openStream()) {
            long count = 0;
            char[] buf = new char[DEFAULT_BUFFER_SIZE];
            int read;
            while ((read = reader.read(buf)) != -1) {
                writer.write(buf, 0, read);
                count += read;
            }
            return count;
        } catch (IOException e) {
            throw new OpenIOOperationException("Failed to copy CharSource to Writer", e);
        }
    }

    /**
     * Copies the contents to a CharSink.
     * 将内容复制到CharSink。
     *
     * @param sink the target CharSink | 目标CharSink
     * @return the number of characters copied | 复制的字符数
     * @throws OpenIOOperationException if copying fails | 如果复制失败
     */
    public long copyTo(CharSink sink) {
        Objects.requireNonNull(sink, "sink must not be null");

        try (Writer writer = sink.openStream()) {
            return copyTo(writer);
        } catch (IOException e) {
            throw new OpenIOOperationException("Failed to copy CharSource to CharSink", e);
        }
    }

    /**
     * Copies the contents to an Appendable.
     * 将内容复制到Appendable。
     *
     * @param appendable the target Appendable | 目标Appendable
     * @return the number of characters copied | 复制的字符数
     * @throws OpenIOOperationException if copying fails | 如果复制失败
     */
    public long copyTo(Appendable appendable) {
        Objects.requireNonNull(appendable, "appendable must not be null");

        if (appendable instanceof Writer writer) {
            return copyTo(writer);
        }

        try (Reader reader = openStream()) {
            long count = 0;
            char[] buf = new char[DEFAULT_BUFFER_SIZE];
            int read;
            while ((read = reader.read(buf)) != -1) {
                appendable.append(new String(buf, 0, read));
                count += read;
            }
            return count;
        } catch (IOException e) {
            throw new OpenIOOperationException("Failed to copy CharSource to Appendable", e);
        }
    }

    /**
     * Checks if this source has the same content as another.
     * 检查此源是否与另一个源具有相同的内容。
     *
     * @param other the other CharSource | 另一个CharSource
     * @return true if contents are equal | 如果内容相等返回true
     * @throws OpenIOOperationException if reading fails | 如果读取失败
     */
    public boolean contentEquals(CharSource other) {
        Objects.requireNonNull(other, "other must not be null");

        try (Reader r1 = this.openStream();
             Reader r2 = other.openStream()) {

            char[] buf1 = new char[DEFAULT_BUFFER_SIZE];
            char[] buf2 = new char[DEFAULT_BUFFER_SIZE];

            while (true) {
                int read1 = readFully(r1, buf1);
                int read2 = readFully(r2, buf2);

                if (read1 != read2) {
                    return false;
                }
                if (read1 == 0) {
                    return true;
                }
                if (!Arrays.equals(buf1, 0, read1, buf2, 0, read2)) {
                    return false;
                }
            }
        } catch (IOException e) {
            throw new OpenIOOperationException("Failed to compare CharSource contents", e);
        }
    }

    /**
     * Checks if the source is empty.
     * 检查源是否为空。
     *
     * @return true if empty | 如果为空返回true
     * @throws OpenIOOperationException if reading fails | 如果读取失败
     */
    public boolean isEmpty() {
        Optional<Long> len = lengthIfKnown();
        if (len.isPresent()) {
            return len.get() == 0;
        }

        try (Reader reader = openStream()) {
            return reader.read() == -1;
        } catch (IOException e) {
            throw new OpenIOOperationException("Failed to check if CharSource is empty", e);
        }
    }

    /**
     * Processes each line with a consumer.
     * 使用消费者处理每一行。
     *
     * @param processor the line processor | 行处理器
     * @throws OpenIOOperationException if reading fails | 如果读取失败
     */
    public void forEachLine(java.util.function.Consumer<String> processor) {
        Objects.requireNonNull(processor, "processor must not be null");

        try (Stream<String> lineStream = lines()) {
            lineStream.forEach(processor);
        }
    }

    /**
     * Returns a concatenated CharSource.
     * 返回连接后的CharSource。
     *
     * @param sources the sources to concatenate | 要连接的源
     * @return a concatenated CharSource | 连接后的CharSource
     */
    public static CharSource concat(CharSource... sources) {
        Objects.requireNonNull(sources, "sources must not be null");
        return concat(Arrays.asList(sources));
    }

    /**
     * Returns a concatenated CharSource.
     * 返回连接后的CharSource。
     *
     * @param sources the sources to concatenate | 要连接的源
     * @return a concatenated CharSource | 连接后的CharSource
     */
    public static CharSource concat(Iterable<? extends CharSource> sources) {
        Objects.requireNonNull(sources, "sources must not be null");
        return new ConcatenatedCharSource(sources);
    }

    // ==================== Factory Methods ====================

    /**
     * Creates a CharSource from a string.
     * 从字符串创建CharSource。
     *
     * @param string the string content | 字符串内容
     * @return a CharSource | CharSource
     */
    public static CharSource wrap(CharSequence string) {
        Objects.requireNonNull(string, "string must not be null");
        return new StringCharSource(string);
    }

    /**
     * Creates an empty CharSource.
     * 创建空的CharSource。
     *
     * @return an empty CharSource | 空的CharSource
     */
    public static CharSource empty() {
        return wrap("");
    }

    /**
     * Creates a CharSource from a file path with UTF-8 encoding.
     * 从文件路径创建使用UTF-8编码的CharSource。
     *
     * @param path the file path | 文件路径
     * @return a CharSource | CharSource
     */
    public static CharSource fromPath(Path path) {
        return fromPath(path, StandardCharsets.UTF_8);
    }

    /**
     * Creates a CharSource from a file path.
     * 从文件路径创建CharSource。
     *
     * @param path    the file path | 文件路径
     * @param charset the charset | 字符集
     * @return a CharSource | CharSource
     */
    public static CharSource fromPath(Path path, Charset charset) {
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(charset, "charset must not be null");
        return new PathCharSource(path, charset);
    }

    /**
     * Creates a CharSource from a URL with UTF-8 encoding.
     * 从URL创建使用UTF-8编码的CharSource。
     *
     * @param url the URL | URL
     * @return a CharSource | CharSource
     */
    public static CharSource fromUrl(URL url) {
        return fromUrl(url, StandardCharsets.UTF_8);
    }

    /**
     * Creates a CharSource from a URL.
     * 从URL创建CharSource。
     *
     * @param url     the URL | URL
     * @param charset the charset | 字符集
     * @return a CharSource | CharSource
     */
    public static CharSource fromUrl(URL url, Charset charset) {
        Objects.requireNonNull(url, "url must not be null");
        Objects.requireNonNull(charset, "charset must not be null");
        return new UrlCharSource(url, charset);
    }

    /**
     * Creates a CharSource from a classpath resource with UTF-8 encoding.
     * 从类路径资源创建使用UTF-8编码的CharSource。
     *
     * @param resourcePath the resource path | 资源路径
     * @return a CharSource | CharSource
     */
    public static CharSource fromResource(String resourcePath) {
        return fromResource(resourcePath, StandardCharsets.UTF_8);
    }

    /**
     * Creates a CharSource from a classpath resource.
     * 从类路径资源创建CharSource。
     *
     * @param resourcePath the resource path | 资源路径
     * @param charset      the charset | 字符集
     * @return a CharSource | CharSource
     */
    public static CharSource fromResource(String resourcePath, Charset charset) {
        return fromResource(resourcePath, charset, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Creates a CharSource from a classpath resource.
     * 从类路径资源创建CharSource。
     *
     * @param resourcePath the resource path | 资源路径
     * @param charset      the charset | 字符集
     * @param classLoader  the class loader | 类加载器
     * @return a CharSource | CharSource
     */
    public static CharSource fromResource(String resourcePath, Charset charset, ClassLoader classLoader) {
        Objects.requireNonNull(resourcePath, "resourcePath must not be null");
        Objects.requireNonNull(charset, "charset must not be null");
        Objects.requireNonNull(classLoader, "classLoader must not be null");
        return new ResourceCharSource(resourcePath, charset, classLoader);
    }

    // ==================== Private Helpers ====================

    private static int readFully(Reader reader, char[] buf) throws IOException {
        int total = 0;
        while (total < buf.length) {
            int read = reader.read(buf, total, buf.length - total);
            if (read == -1) {
                break;
            }
            total += read;
        }
        return total;
    }

    // ==================== Implementation Classes ====================

    private static final class StringCharSource extends CharSource {
        private final CharSequence content;

        StringCharSource(CharSequence content) {
            this.content = content;
        }

        @Override
        public Reader openStream() {
            return new StringReader(content.toString());
        }

        @Override
        public Optional<Long> lengthIfKnown() {
            return Optional.of((long) content.length());
        }

        @Override
        public String read() {
            return content.toString();
        }

        @Override
        public boolean isEmpty() {
            return content.isEmpty();
        }

        @Override
        public String toString() {
            return "CharSource.wrap(\"" +
                    (content.length() > 20 ? content.subSequence(0, 20) + "..." : content) +
                    "\")";
        }
    }

    private static final class PathCharSource extends CharSource {
        private final Path path;
        private final Charset charset;

        PathCharSource(Path path, Charset charset) {
            this.path = path;
            this.charset = charset;
        }

        @Override
        public Reader openStream() {
            try {
                return Files.newBufferedReader(path, charset);
            } catch (IOException e) {
                throw new OpenIOOperationException("Failed to open file: " + path, e);
            }
        }

        @Override
        public String read() {
            try {
                return Files.readString(path, charset);
            } catch (IOException e) {
                throw new OpenIOOperationException("Failed to read file: " + path, e);
            }
        }

        @Override
        public List<String> readLines() {
            try {
                return Files.readAllLines(path, charset);
            } catch (IOException e) {
                throw new OpenIOOperationException("Failed to read lines from file: " + path, e);
            }
        }

        @Override
        public Stream<String> lines() {
            try {
                return Files.lines(path, charset);
            } catch (IOException e) {
                throw new OpenIOOperationException("Failed to create line stream for file: " + path, e);
            }
        }

        @Override
        public String toString() {
            return "CharSource.fromPath(" + path + ", " + charset + ")";
        }
    }

    private static final class UrlCharSource extends CharSource {
        private final URL url;
        private final Charset charset;

        UrlCharSource(URL url, Charset charset) {
            this.url = url;
            this.charset = charset;
        }

        @Override
        public Reader openStream() {
            try {
                return new InputStreamReader(url.openStream(), charset);
            } catch (IOException e) {
                throw new OpenIOOperationException("Failed to open URL: " + url, e);
            }
        }

        @Override
        public String toString() {
            return "CharSource.fromUrl(" + url + ", " + charset + ")";
        }
    }

    private static final class ResourceCharSource extends CharSource {
        private final String resourcePath;
        private final Charset charset;
        private final ClassLoader classLoader;

        ResourceCharSource(String resourcePath, Charset charset, ClassLoader classLoader) {
            this.resourcePath = resourcePath;
            this.charset = charset;
            this.classLoader = classLoader;
        }

        @Override
        public Reader openStream() {
            InputStream in = classLoader.getResourceAsStream(resourcePath);
            if (in == null) {
                throw new OpenIOOperationException("Resource not found: " + resourcePath);
            }
            return new InputStreamReader(in, charset);
        }

        @Override
        public String toString() {
            return "CharSource.fromResource(" + resourcePath + ", " + charset + ")";
        }
    }

    private static final class ConcatenatedCharSource extends CharSource {
        private final Iterable<? extends CharSource> sources;

        ConcatenatedCharSource(Iterable<? extends CharSource> sources) {
            this.sources = sources;
        }

        @Override
        public Reader openStream() {
            Iterator<? extends CharSource> iter = sources.iterator();
            return new Reader() {
                private Reader current = null;

                @Override
                public int read(char[] cbuf, int off, int len) throws IOException {
                    while (true) {
                        if (current == null) {
                            if (!iter.hasNext()) {
                                return -1;
                            }
                            current = iter.next().openStream();
                        }

                        int result = current.read(cbuf, off, len);
                        if (result != -1) {
                            return result;
                        }

                        current.close();
                        current = null;
                    }
                }

                @Override
                public void close() throws IOException {
                    if (current != null) {
                        current.close();
                    }
                }
            };
        }

        @Override
        public Optional<Long> lengthIfKnown() {
            long total = 0;
            for (CharSource source : sources) {
                Optional<Long> len = source.lengthIfKnown();
                if (len.isEmpty()) {
                    return Optional.empty();
                }
                total += len.get();
            }
            return Optional.of(total);
        }

        @Override
        public String toString() {
            return "CharSource.concat(...)";
        }
    }
}
