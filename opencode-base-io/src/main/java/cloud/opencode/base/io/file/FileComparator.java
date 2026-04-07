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

package cloud.opencode.base.io.file;

import cloud.opencode.base.io.exception.OpenIOOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * File Content Comparator - Compares files by content
 * 文件内容比较器 - 按内容比较文件
 *
 * <p>Provides various methods for comparing files by content, including
 * byte-by-byte comparison, hash-based comparison, and line-by-line comparison.</p>
 * <p>提供多种按内容比较文件的方法，包括逐字节比较、基于哈希的比较和逐行比较。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Byte-by-byte content comparison - 逐字节内容比较</li>
 *   <li>Hash-based comparison for large files - 大文件的基于哈希比较</li>
 *   <li>Line-by-line text comparison - 逐行文本比较</li>
 *   <li>Diff generation - 差异生成</li>
 *   <li>Directory comparison - 目录比较</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Simple content comparison
 * boolean same = FileComparator.contentEquals(path1, path2);
 *
 * // Compare using hash (faster for large files)
 * boolean same = FileComparator.hashEquals(path1, path2, "SHA-256");
 *
 * // Compare directories
 * DirectoryDiff diff = FileComparator.compareDirectories(dir1, dir2);
 *
 * // Get line differences
 * LineDiff diff = FileComparator.diffLines(file1, file2);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: No, paths must not be null - 空值安全: 否，路径不可为null</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see MoreFiles
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.2.0
 */
public final class FileComparator {

    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private static final char[] HEX_LOWER = "0123456789abcdef".toCharArray();

    private FileComparator() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    // ==================== Content Comparison ====================

    /**
     * Compares two files byte-by-byte.
     * 逐字节比较两个文件。
     *
     * @param file1 the first file | 第一个文件
     * @param file2 the second file | 第二个文件
     * @return true if files have identical content | 如果文件内容相同返回true
     * @throws OpenIOOperationException if comparison fails | 如果比较失败
     */
    public static boolean contentEquals(Path file1, Path file2) {
        Objects.requireNonNull(file1, "file1 must not be null");
        Objects.requireNonNull(file2, "file2 must not be null");

        // Quick checks
        if (Files.notExists(file1) || Files.notExists(file2)) {
            return Files.notExists(file1) && Files.notExists(file2);
        }

        if (isSameFile(file1, file2)) {
            return true;
        }

        try {
            // Size check first
            long size1 = Files.size(file1);
            long size2 = Files.size(file2);
            if (size1 != size2) {
                return false;
            }

            if (size1 == 0) {
                return true;
            }

            // Use memory-mapped comparison for large files
            if (size1 > DEFAULT_BUFFER_SIZE * 10) {
                return contentEqualsMapped(file1, file2);
            }

            // Byte-by-byte comparison for smaller files
            return contentEqualsStreamed(file1, file2);

        } catch (IOException e) {
            throw new OpenIOOperationException("Failed to compare files: " + file1 + " and " + file2, e);
        }
    }

    /**
     * Compares two files using hash.
     * 使用哈希比较两个文件。
     *
     * @param file1     the first file | 第一个文件
     * @param file2     the second file | 第二个文件
     * @param algorithm the hash algorithm (e.g., "SHA-256", "MD5") | 哈希算法
     * @return true if files have the same hash | 如果文件哈希相同返回true
     * @throws OpenIOOperationException if comparison fails | 如果比较失败
     */
    public static boolean hashEquals(Path file1, Path file2, String algorithm) {
        Objects.requireNonNull(file1, "file1 must not be null");
        Objects.requireNonNull(file2, "file2 must not be null");
        Objects.requireNonNull(algorithm, "algorithm must not be null");

        byte[] hash1 = computeHash(file1, algorithm);
        byte[] hash2 = computeHash(file2, algorithm);
        return Arrays.equals(hash1, hash2);
    }

    /**
     * Computes the hash of a file.
     * 计算文件的哈希值。
     *
     * @param file      the file | 文件
     * @param algorithm the hash algorithm | 哈希算法
     * @return the hash bytes | 哈希字节
     * @throws OpenIOOperationException if hashing fails | 如果哈希失败
     */
    public static byte[] computeHash(Path file, String algorithm) {
        Objects.requireNonNull(file, "file must not be null");
        Objects.requireNonNull(algorithm, "algorithm must not be null");

        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            try (InputStream in = Files.newInputStream(file)) {
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
            throw new OpenIOOperationException("Failed to hash file: " + file, e);
        }
    }

    /**
     * Computes the hash of a file as hexadecimal string.
     * 计算文件的哈希值并返回十六进制字符串。
     *
     * @param file      the file | 文件
     * @param algorithm the hash algorithm | 哈希算法
     * @return the hash as hex string | 十六进制哈希字符串
     * @throws OpenIOOperationException if hashing fails | 如果哈希失败
     */
    public static String computeHashHex(Path file, String algorithm) {
        byte[] hash = computeHash(file, algorithm);
        char[] chars = new char[hash.length * 2];
        for (int i = 0; i < hash.length; i++) {
            int v = hash[i] & 0xFF;
            chars[i * 2] = HEX_LOWER[v >>> 4];
            chars[i * 2 + 1] = HEX_LOWER[v & 0x0F];
        }
        return new String(chars);
    }

    // ==================== Line Comparison ====================

    /**
     * Compares two text files line by line.
     * 逐行比较两个文本文件。
     *
     * @param file1   the first file | 第一个文件
     * @param file2   the second file | 第二个文件
     * @param charset the charset | 字符集
     * @return true if files have identical lines | 如果文件行相同返回true
     * @throws OpenIOOperationException if comparison fails | 如果比较失败
     */
    public static boolean linesEqual(Path file1, Path file2, Charset charset) {
        Objects.requireNonNull(file1, "file1 must not be null");
        Objects.requireNonNull(file2, "file2 must not be null");
        Objects.requireNonNull(charset, "charset must not be null");

        try (Stream<String> lines1 = Files.lines(file1, charset);
             Stream<String> lines2 = Files.lines(file2, charset)) {

            Iterator<String> iter1 = lines1.iterator();
            Iterator<String> iter2 = lines2.iterator();

            while (iter1.hasNext() && iter2.hasNext()) {
                if (!iter1.next().equals(iter2.next())) {
                    return false;
                }
            }

            return !iter1.hasNext() && !iter2.hasNext();

        } catch (IOException e) {
            throw new OpenIOOperationException(
                    "Failed to compare files line by line: " + file1 + " and " + file2, e);
        }
    }

    /**
     * Compares two text files line by line using UTF-8.
     * 使用UTF-8逐行比较两个文本文件。
     *
     * @param file1 the first file | 第一个文件
     * @param file2 the second file | 第二个文件
     * @return true if files have identical lines | 如果文件行相同返回true
     * @throws OpenIOOperationException if comparison fails | 如果比较失败
     */
    public static boolean linesEqual(Path file1, Path file2) {
        return linesEqual(file1, file2, StandardCharsets.UTF_8);
    }

    /**
     * Generates a diff between two text files.
     * 生成两个文本文件之间的差异。
     *
     * @param file1   the first file | 第一个文件
     * @param file2   the second file | 第二个文件
     * @param charset the charset | 字符集
     * @return the line diff | 行差异
     * @throws OpenIOOperationException if diff fails | 如果差异生成失败
     */
    public static LineDiff diffLines(Path file1, Path file2, Charset charset) {
        Objects.requireNonNull(file1, "file1 must not be null");
        Objects.requireNonNull(file2, "file2 must not be null");
        Objects.requireNonNull(charset, "charset must not be null");

        try {
            List<String> lines1 = Files.readAllLines(file1, charset);
            List<String> lines2 = Files.readAllLines(file2, charset);
            return computeLineDiff(lines1, lines2);
        } catch (IOException e) {
            throw new OpenIOOperationException(
                    "Failed to diff files: " + file1 + " and " + file2, e);
        }
    }

    /**
     * Generates a diff between two text files using UTF-8.
     * 使用UTF-8生成两个文本文件之间的差异。
     *
     * @param file1 the first file | 第一个文件
     * @param file2 the second file | 第二个文件
     * @return the line diff | 行差异
     * @throws OpenIOOperationException if diff fails | 如果差异生成失败
     */
    public static LineDiff diffLines(Path file1, Path file2) {
        return diffLines(file1, file2, StandardCharsets.UTF_8);
    }

    // ==================== Directory Comparison ====================

    /**
     * Compares two directories.
     * 比较两个目录。
     *
     * @param dir1 the first directory | 第一个目录
     * @param dir2 the second directory | 第二个目录
     * @return the directory diff | 目录差异
     * @throws OpenIOOperationException if comparison fails | 如果比较失败
     */
    public static DirectoryDiff compareDirectories(Path dir1, Path dir2) {
        return compareDirectories(dir1, dir2, _ -> true);
    }

    /**
     * Compares two directories with a filter.
     * 使用过滤器比较两个目录。
     *
     * @param dir1   the first directory | 第一个目录
     * @param dir2   the second directory | 第二个目录
     * @param filter the file filter | 文件过滤器
     * @return the directory diff | 目录差异
     * @throws OpenIOOperationException if comparison fails | 如果比较失败
     */
    public static DirectoryDiff compareDirectories(Path dir1, Path dir2, Predicate<Path> filter) {
        Objects.requireNonNull(dir1, "dir1 must not be null");
        Objects.requireNonNull(dir2, "dir2 must not be null");
        Objects.requireNonNull(filter, "filter must not be null");

        try {
            Set<Path> files1 = collectRelativePaths(dir1, filter);
            Set<Path> files2 = collectRelativePaths(dir2, filter);

            Set<Path> onlyInFirst = new TreeSet<>();
            Set<Path> onlyInSecond = new TreeSet<>();
            Set<Path> common = new TreeSet<>();
            Set<Path> different = new TreeSet<>();

            for (Path p : files1) {
                if (files2.contains(p)) {
                    common.add(p);
                } else {
                    onlyInFirst.add(p);
                }
            }

            for (Path p : files2) {
                if (!files1.contains(p)) {
                    onlyInSecond.add(p);
                }
            }

            // Compare common files
            for (Path p : common) {
                Path path1 = dir1.resolve(p);
                Path path2 = dir2.resolve(p);

                boolean isDir1 = Files.isDirectory(path1);
                boolean isDir2 = Files.isDirectory(path2);

                if (isDir1 != isDir2) {
                    different.add(p);
                } else if (!isDir1 && !contentEquals(path1, path2)) {
                    different.add(p);
                }
            }

            return new DirectoryDiff(
                    Collections.unmodifiableSet(onlyInFirst),
                    Collections.unmodifiableSet(onlyInSecond),
                    Collections.unmodifiableSet(common),
                    Collections.unmodifiableSet(different)
            );

        } catch (IOException e) {
            throw new OpenIOOperationException(
                    "Failed to compare directories: " + dir1 + " and " + dir2, e);
        }
    }

    // ==================== Result Records ====================

    /**
     * Result of comparing lines between two files.
     * 两个文件之间行比较的结果。
     *
     * @param added   lines only in file2 with line numbers | 仅在file2中的行及行号
     * @param removed lines only in file1 with line numbers | 仅在file1中的行及行号
     * @param changed lines that changed between files | 在文件间改变的行
     */
    public record LineDiff(
            List<LineEntry> added,
            List<LineEntry> removed,
            List<LineChange> changed
    ) {
        /**
         * Returns true if there are no differences.
         * 如果没有差异返回true。
         *
         * @return true if identical | 如果相同返回true
         */
        public boolean isEmpty() {
            return added.isEmpty() && removed.isEmpty() && changed.isEmpty();
        }

        /**
         * Returns a unified diff format string.
         * 返回统一差异格式字符串。
         *
         * @return unified diff | 统一差异
         */
        public String toUnifiedDiff() {
            StringBuilder sb = new StringBuilder();
            for (LineEntry entry : removed) {
                sb.append("- ").append(entry.lineNumber()).append(": ").append(entry.content()).append("\n");
            }
            for (LineEntry entry : added) {
                sb.append("+ ").append(entry.lineNumber()).append(": ").append(entry.content()).append("\n");
            }
            for (LineChange change : changed) {
                sb.append("~ ").append(change.lineNumber()).append(":\n");
                sb.append("  - ").append(change.oldContent()).append("\n");
                sb.append("  + ").append(change.newContent()).append("\n");
            }
            return sb.toString();
        }
    }

    /**
     * A line entry with line number.
     * 带有行号的行条目。
     *
     * @param lineNumber the line number (1-based) | 行号（从1开始）
     * @param content    the line content | 行内容
     */
    public record LineEntry(int lineNumber, String content) {}

    /**
     * A line change between two files.
     * 两个文件之间的行变化。
     *
     * @param lineNumber the line number | 行号
     * @param oldContent the content in file1 | file1中的内容
     * @param newContent the content in file2 | file2中的内容
     */
    public record LineChange(int lineNumber, String oldContent, String newContent) {}

    /**
     * Result of comparing two directories.
     * 比较两个目录的结果。
     *
     * @param onlyInFirst  paths only in first directory | 仅在第一个目录中的路径
     * @param onlyInSecond paths only in second directory | 仅在第二个目录中的路径
     * @param common       paths in both directories | 两个目录中都有的路径
     * @param different    common paths with different content | 内容不同的共同路径
     */
    public record DirectoryDiff(
            Set<Path> onlyInFirst,
            Set<Path> onlyInSecond,
            Set<Path> common,
            Set<Path> different
    ) {
        /**
         * Returns true if directories are identical.
         * 如果目录相同返回true。
         *
         * @return true if identical | 如果相同返回true
         */
        public boolean isIdentical() {
            return onlyInFirst.isEmpty() && onlyInSecond.isEmpty() && different.isEmpty();
        }

        /**
         * Returns the total number of differences.
         * 返回差异总数。
         *
         * @return difference count | 差异数量
         */
        public int differenceCount() {
            return onlyInFirst.size() + onlyInSecond.size() + different.size();
        }
    }

    // ==================== Private Helpers ====================

    private static boolean isSameFile(Path file1, Path file2) {
        try {
            return Files.isSameFile(file1, file2);
        } catch (IOException e) {
            return false;
        }
    }

    private static boolean contentEqualsStreamed(Path file1, Path file2) throws IOException {
        try (InputStream in1 = Files.newInputStream(file1);
             InputStream in2 = Files.newInputStream(file2)) {

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
        }
    }

    private static boolean contentEqualsMapped(Path file1, Path file2) throws IOException {
        try (FileChannel ch1 = FileChannel.open(file1, StandardOpenOption.READ);
             FileChannel ch2 = FileChannel.open(file2, StandardOpenOption.READ)) {

            long size = ch1.size();
            if (ch2.size() != size) {
                return false;
            }

            // Compare in chunks (64MB for fewer syscalls)
            long position = 0;
            int chunkSize = 64 * 1024 * 1024;

            while (position < size) {
                long remaining = size - position;
                int mapSize = (int) Math.min(remaining, chunkSize);

                ByteBuffer buf1 = ch1.map(FileChannel.MapMode.READ_ONLY, position, mapSize);
                ByteBuffer buf2 = ch2.map(FileChannel.MapMode.READ_ONLY, position, mapSize);

                if (!buf1.equals(buf2)) {
                    return false;
                }

                position += mapSize;
            }

            return true;
        }
    }

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

    private static Set<Path> collectRelativePaths(Path dir, Predicate<Path> filter) throws IOException {
        Set<Path> paths = new TreeSet<>();
        try (Stream<Path> stream = Files.walk(dir)) {
            stream.filter(p -> !p.equals(dir))
                    .filter(filter)
                    .forEach(p -> paths.add(dir.relativize(p)));
        }
        return paths;
    }

    private static LineDiff computeLineDiff(List<String> lines1, List<String> lines2) {
        List<LineEntry> added = new ArrayList<>();
        List<LineEntry> removed = new ArrayList<>();
        List<LineChange> changed = new ArrayList<>();

        // Simple line-by-line comparison
        int maxLines = Math.max(lines1.size(), lines2.size());

        for (int i = 0; i < maxLines; i++) {
            String line1 = i < lines1.size() ? lines1.get(i) : null;
            String line2 = i < lines2.size() ? lines2.get(i) : null;

            int lineNum = i + 1;

            if (line1 == null) {
                added.add(new LineEntry(lineNum, line2));
            } else if (line2 == null) {
                removed.add(new LineEntry(lineNum, line1));
            } else if (!line1.equals(line2)) {
                changed.add(new LineChange(lineNum, line1, line2));
            }
        }

        return new LineDiff(
                Collections.unmodifiableList(added),
                Collections.unmodifiableList(removed),
                Collections.unmodifiableList(changed)
        );
    }
}
