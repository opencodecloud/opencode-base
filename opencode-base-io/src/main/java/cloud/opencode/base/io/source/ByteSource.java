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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Abstract Byte Source - A readable source of bytes
 * 抽象字节源 - 可读的字节来源
 *
 * <p>An abstraction over different sources of byte data. Each instance
 * represents an immutable source that can be read multiple times.</p>
 * <p>对不同字节数据源的抽象。每个实例代表一个可以多次读取的不可变源。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Unified byte reading - 统一的字节读取</li>
 *   <li>Copy to OutputStream - 复制到输出流</li>
 *   <li>Content equality checks - 内容相等性检查</li>
 *   <li>Size estimation - 大小估算</li>
 *   <li>Hash computation - 哈希计算</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // From file
 * ByteSource source = ByteSource.fromPath(Paths.get("data.bin"));
 * byte[] data = source.read();
 *
 * // From URL
 * ByteSource urlSource = ByteSource.fromUrl(new URL("https://example.com/data"));
 * urlSource.copyTo(outputStream);
 *
 * // From byte array
 * ByteSource memSource = ByteSource.wrap(new byte[]{1, 2, 3});
 * long size = memSource.size();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable source, each read opens new stream) - 线程安全: 是（不可变源，每次读取打开新流）</li>
 *   <li>Null-safe: No, arguments must not be null - 空值安全: 否，参数不可为null</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see CharSource
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.2.0
 */
public abstract class ByteSource {

    private static final int DEFAULT_BUFFER_SIZE = 8192;

    /**
     * Opens a new InputStream for reading.
     * 打开新的输入流进行读取。
     *
     * <p>The caller is responsible for closing the stream.</p>
     * <p>调用者负责关闭流。</p>
     *
     * @return a new InputStream | 新的输入流
     * @throws OpenIOOperationException if the stream cannot be opened | 如果流无法打开
     */
    public abstract InputStream openStream();

    /**
     * Returns a view of the contents as a CharSource with the given charset.
     * 返回使用给定字符集作为CharSource的内容视图。
     *
     * @param charset the charset to use | 要使用的字符集
     * @return a CharSource view | CharSource视图
     */
    public CharSource asCharSource(Charset charset) {
        Objects.requireNonNull(charset, "charset must not be null");
        return new CharSource() {
            @Override
            public Reader openStream() {
                return new InputStreamReader(ByteSource.this.openStream(), charset);
            }

            @Override
            public String toString() {
                return ByteSource.this.toString() + ".asCharSource(" + charset + ")";
            }
        };
    }

    /**
     * Returns a view of the contents as a CharSource with UTF-8 charset.
     * 返回使用UTF-8字符集作为CharSource的内容视图。
     *
     * @return a CharSource view | CharSource视图
     */
    public CharSource asCharSource() {
        return asCharSource(StandardCharsets.UTF_8);
    }

    /**
     * Returns the size of this source in bytes, if known.
     * 返回此源的字节大小（如果已知）。
     *
     * @return an Optional containing the size, or empty if unknown | 包含大小的Optional，如果未知则为空
     */
    public Optional<Long> sizeIfKnown() {
        return Optional.empty();
    }

    /**
     * Returns the size of this source in bytes.
     * 返回此源的字节大小。
     *
     * <p>If the size is not known, this method reads the entire source.</p>
     * <p>如果大小未知，此方法会读取整个源。</p>
     *
     * @return the size in bytes | 字节大小
     * @throws OpenIOOperationException if reading fails | 如果读取失败
     */
    public long size() {
        Optional<Long> known = sizeIfKnown();
        if (known.isPresent()) {
            return known.get();
        }

        try (InputStream in = openStream()) {
            long count = 0;
            byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
            int read;
            while ((read = in.read(buf)) != -1) {
                count += read;
            }
            return count;
        } catch (IOException e) {
            throw new OpenIOOperationException("Failed to determine size of ByteSource", e);
        }
    }

    /**
     * Reads the entire contents as a byte array.
     * 将整个内容读取为字节数组。
     *
     * @return the byte array | 字节数组
     * @throws OpenIOOperationException if reading fails | 如果读取失败
     */
    public byte[] read() {
        try (InputStream in = openStream()) {
            return in.readAllBytes();
        } catch (IOException e) {
            throw new OpenIOOperationException("Failed to read ByteSource", e);
        }
    }

    /**
     * Copies the contents to an OutputStream.
     * 将内容复制到输出流。
     *
     * @param output the target OutputStream | 目标输出流
     * @return the number of bytes copied | 复制的字节数
     * @throws OpenIOOperationException if copying fails | 如果复制失败
     */
    public long copyTo(OutputStream output) {
        Objects.requireNonNull(output, "output must not be null");

        try (InputStream in = openStream()) {
            return in.transferTo(output);
        } catch (IOException e) {
            throw new OpenIOOperationException("Failed to copy ByteSource to OutputStream", e);
        }
    }

    /**
     * Copies the contents to a ByteSink.
     * 将内容复制到ByteSink。
     *
     * @param sink the target ByteSink | 目标ByteSink
     * @return the number of bytes copied | 复制的字节数
     * @throws OpenIOOperationException if copying fails | 如果复制失败
     */
    public long copyTo(ByteSink sink) {
        Objects.requireNonNull(sink, "sink must not be null");

        try (OutputStream out = sink.openStream()) {
            return copyTo(out);
        } catch (IOException e) {
            throw new OpenIOOperationException("Failed to copy ByteSource to ByteSink", e);
        }
    }

    /**
     * Checks if this source has the same content as another.
     * 检查此源是否与另一个源具有相同的内容。
     *
     * @param other the other ByteSource | 另一个ByteSource
     * @return true if contents are equal | 如果内容相等返回true
     * @throws OpenIOOperationException if reading fails | 如果读取失败
     */
    public boolean contentEquals(ByteSource other) {
        Objects.requireNonNull(other, "other must not be null");

        // Quick check: if sizes are known and different, return false
        Optional<Long> thisSize = this.sizeIfKnown();
        Optional<Long> otherSize = other.sizeIfKnown();
        if (thisSize.isPresent() && otherSize.isPresent() &&
                !thisSize.get().equals(otherSize.get())) {
            return false;
        }

        try (InputStream in1 = this.openStream();
             InputStream in2 = other.openStream()) {

            byte[] buf1 = new byte[DEFAULT_BUFFER_SIZE];
            byte[] buf2 = new byte[DEFAULT_BUFFER_SIZE];

            while (true) {
                int read1 = readFully(in1, buf1);
                int read2 = readFully(in2, buf2);

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
            throw new OpenIOOperationException("Failed to compare ByteSource contents", e);
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
        Optional<Long> size = sizeIfKnown();
        if (size.isPresent()) {
            return size.get() == 0;
        }

        try (InputStream in = openStream()) {
            return in.read() == -1;
        } catch (IOException e) {
            throw new OpenIOOperationException("Failed to check if ByteSource is empty", e);
        }
    }

    /**
     * Computes the hash of the contents using the specified algorithm.
     * 使用指定算法计算内容的哈希值。
     *
     * @param algorithm the hash algorithm (e.g., "SHA-256", "MD5") | 哈希算法
     * @return the hash bytes | 哈希字节
     * @throws OpenIOOperationException if hashing fails | 如果哈希失败
     */
    public byte[] hash(String algorithm) {
        Objects.requireNonNull(algorithm, "algorithm must not be null");

        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            try (InputStream in = openStream()) {
                byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
                int read;
                while ((read = in.read(buf)) != -1) {
                    digest.update(buf, 0, read);
                }
            }
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new OpenIOOperationException("Hash algorithm not found: " + algorithm, e);
        } catch (IOException e) {
            throw new OpenIOOperationException("Failed to hash ByteSource", e);
        }
    }

    /**
     * Returns a ByteSource that returns a slice of this source.
     * 返回此源的一个切片的ByteSource。
     *
     * @param offset the starting offset | 起始偏移量
     * @param length the maximum length | 最大长度
     * @return a sliced ByteSource | 切片后的ByteSource
     */
    public ByteSource slice(long offset, long length) {
        if (offset < 0) {
            throw new IllegalArgumentException("offset must not be negative");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length must not be negative");
        }

        if (offset == 0 && length == Long.MAX_VALUE) {
            return this;
        }

        return new SlicedByteSource(this, offset, length);
    }

    /**
     * Returns a concatenated ByteSource.
     * 返回连接后的ByteSource。
     *
     * @param sources the sources to concatenate | 要连接的源
     * @return a concatenated ByteSource | 连接后的ByteSource
     */
    public static ByteSource concat(ByteSource... sources) {
        Objects.requireNonNull(sources, "sources must not be null");
        return concat(Arrays.asList(sources));
    }

    /**
     * Returns a concatenated ByteSource.
     * 返回连接后的ByteSource。
     *
     * @param sources the sources to concatenate | 要连接的源
     * @return a concatenated ByteSource | 连接后的ByteSource
     */
    public static ByteSource concat(Iterable<? extends ByteSource> sources) {
        Objects.requireNonNull(sources, "sources must not be null");
        return new ConcatenatedByteSource(sources);
    }

    // ==================== Factory Methods ====================

    /**
     * Creates a ByteSource from a byte array.
     * 从字节数组创建ByteSource。
     *
     * @param bytes the byte array | 字节数组
     * @return a ByteSource | ByteSource
     */
    public static ByteSource wrap(byte[] bytes) {
        Objects.requireNonNull(bytes, "bytes must not be null");
        return new ByteArrayByteSource(bytes);
    }

    /**
     * Creates an empty ByteSource.
     * 创建空的ByteSource。
     *
     * @return an empty ByteSource | 空的ByteSource
     */
    public static ByteSource empty() {
        return wrap(new byte[0]);
    }

    /**
     * Creates a ByteSource from a file path.
     * 从文件路径创建ByteSource。
     *
     * @param path the file path | 文件路径
     * @return a ByteSource | ByteSource
     */
    public static ByteSource fromPath(Path path) {
        Objects.requireNonNull(path, "path must not be null");
        return new PathByteSource(path);
    }

    /**
     * Creates a ByteSource from a URL.
     * 从URL创建ByteSource。
     *
     * @param url the URL | URL
     * @return a ByteSource | ByteSource
     */
    public static ByteSource fromUrl(URL url) {
        Objects.requireNonNull(url, "url must not be null");
        return new UrlByteSource(url);
    }

    /**
     * Creates a ByteSource from a classpath resource.
     * 从类路径资源创建ByteSource。
     *
     * @param resourcePath the resource path | 资源路径
     * @return a ByteSource | ByteSource
     */
    public static ByteSource fromResource(String resourcePath) {
        Objects.requireNonNull(resourcePath, "resourcePath must not be null");
        return fromResource(resourcePath, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Creates a ByteSource from a classpath resource.
     * 从类路径资源创建ByteSource。
     *
     * @param resourcePath the resource path | 资源路径
     * @param classLoader  the class loader | 类加载器
     * @return a ByteSource | ByteSource
     */
    public static ByteSource fromResource(String resourcePath, ClassLoader classLoader) {
        Objects.requireNonNull(resourcePath, "resourcePath must not be null");
        Objects.requireNonNull(classLoader, "classLoader must not be null");
        return new ResourceByteSource(resourcePath, classLoader);
    }

    // ==================== Private Helpers ====================

    private static int readFully(InputStream in, byte[] buf) throws IOException {
        int total = 0;
        while (total < buf.length) {
            int read = in.read(buf, total, buf.length - total);
            if (read == -1) {
                break;
            }
            total += read;
        }
        return total;
    }

    // ==================== Implementation Classes ====================

    private static final class ByteArrayByteSource extends ByteSource {
        private final byte[] bytes;

        ByteArrayByteSource(byte[] bytes) {
            this.bytes = bytes.clone();
        }

        @Override
        public InputStream openStream() {
            return new ByteArrayInputStream(bytes);
        }

        @Override
        public Optional<Long> sizeIfKnown() {
            return Optional.of((long) bytes.length);
        }

        @Override
        public byte[] read() {
            return bytes.clone();
        }

        @Override
        public String toString() {
            return "ByteSource.wrap(byte[" + bytes.length + "])";
        }
    }

    private static final class PathByteSource extends ByteSource {
        private final Path path;

        PathByteSource(Path path) {
            this.path = path;
        }

        @Override
        public InputStream openStream() {
            try {
                return Files.newInputStream(path);
            } catch (IOException e) {
                throw new OpenIOOperationException("Failed to open file: " + path, e);
            }
        }

        @Override
        public Optional<Long> sizeIfKnown() {
            try {
                return Optional.of(Files.size(path));
            } catch (IOException e) {
                return Optional.empty();
            }
        }

        @Override
        public byte[] read() {
            try {
                return Files.readAllBytes(path);
            } catch (IOException e) {
                throw new OpenIOOperationException("Failed to read file: " + path, e);
            }
        }

        @Override
        public String toString() {
            return "ByteSource.fromPath(" + path + ")";
        }
    }

    private static final class UrlByteSource extends ByteSource {
        private final URL url;

        UrlByteSource(URL url) {
            this.url = url;
        }

        @Override
        public InputStream openStream() {
            try {
                return url.openStream();
            } catch (IOException e) {
                throw new OpenIOOperationException("Failed to open URL: " + url, e);
            }
        }

        @Override
        public String toString() {
            return "ByteSource.fromUrl(" + url + ")";
        }
    }

    private static final class ResourceByteSource extends ByteSource {
        private final String resourcePath;
        private final ClassLoader classLoader;

        ResourceByteSource(String resourcePath, ClassLoader classLoader) {
            this.resourcePath = resourcePath;
            this.classLoader = classLoader;
        }

        @Override
        public InputStream openStream() {
            InputStream in = classLoader.getResourceAsStream(resourcePath);
            if (in == null) {
                throw new OpenIOOperationException("Resource not found: " + resourcePath);
            }
            return in;
        }

        @Override
        public String toString() {
            return "ByteSource.fromResource(" + resourcePath + ")";
        }
    }

    private static final class SlicedByteSource extends ByteSource {
        private final ByteSource source;
        private final long offset;
        private final long length;

        SlicedByteSource(ByteSource source, long offset, long length) {
            this.source = source;
            this.offset = offset;
            this.length = length;
        }

        @Override
        public InputStream openStream() {
            InputStream in = source.openStream();
            try {
                // Skip to offset
                long remaining = offset;
                while (remaining > 0) {
                    long skipped = in.skip(remaining);
                    if (skipped == 0) {
                        // Try reading one byte
                        if (in.read() == -1) {
                            break;
                        }
                        remaining--;
                    } else {
                        remaining -= skipped;
                    }
                }
                return new BoundedInputStream(in, length);
            } catch (Exception e) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
                if (e instanceof IOException ioe) {
                    throw new OpenIOOperationException("Failed to create sliced stream", ioe);
                }
                throw new OpenIOOperationException("Failed to create sliced stream", e);
            }
        }

        @Override
        public Optional<Long> sizeIfKnown() {
            Optional<Long> sourceSize = source.sizeIfKnown();
            if (sourceSize.isPresent()) {
                long actualSize = Math.max(0, sourceSize.get() - offset);
                return Optional.of(Math.min(actualSize, length));
            }
            return Optional.empty();
        }

        @Override
        public String toString() {
            return source + ".slice(" + offset + ", " + length + ")";
        }
    }

    private static final class ConcatenatedByteSource extends ByteSource {
        private final Iterable<? extends ByteSource> sources;

        ConcatenatedByteSource(Iterable<? extends ByteSource> sources) {
            this.sources = sources;
        }

        @Override
        public InputStream openStream() {
            return new SequenceInputStream(new java.util.Enumeration<>() {
                private final java.util.Iterator<? extends ByteSource> iter = sources.iterator();

                @Override
                public boolean hasMoreElements() {
                    return iter.hasNext();
                }

                @Override
                public InputStream nextElement() {
                    return iter.next().openStream();
                }
            });
        }

        @Override
        public Optional<Long> sizeIfKnown() {
            long total = 0;
            for (ByteSource source : sources) {
                Optional<Long> size = source.sizeIfKnown();
                if (size.isEmpty()) {
                    return Optional.empty();
                }
                total += size.get();
            }
            return Optional.of(total);
        }

        @Override
        public String toString() {
            return "ByteSource.concat(...)";
        }
    }

    /**
     * Bounded input stream that limits the number of bytes read.
     */
    private static final class BoundedInputStream extends FilterInputStream {
        private long remaining;

        BoundedInputStream(InputStream in, long limit) {
            super(in);
            this.remaining = limit;
        }

        @Override
        public int read() throws IOException {
            if (remaining <= 0) {
                return -1;
            }
            int result = super.read();
            if (result != -1) {
                remaining--;
            }
            return result;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (remaining <= 0) {
                return -1;
            }
            int toRead = (int) Math.min(len, remaining);
            int result = super.read(b, off, toRead);
            if (result > 0) {
                remaining -= result;
            }
            return result;
        }

        @Override
        public long skip(long n) throws IOException {
            long toSkip = Math.min(n, remaining);
            long skipped = super.skip(toSkip);
            remaining -= skipped;
            return skipped;
        }

        @Override
        public int available() throws IOException {
            return (int) Math.min(super.available(), remaining);
        }
    }
}
