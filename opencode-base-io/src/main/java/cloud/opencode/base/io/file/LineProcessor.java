package cloud.opencode.base.io.file;

import cloud.opencode.base.io.exception.OpenIOOperationException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Fluent Line-by-Line File Processor
 * 流式逐行文件处理器
 *
 * <p>A fluent, functional line-by-line file processor that accumulates
 * operations and applies them lazily when a terminal operation is called.
 * Built on top of {@link Files#lines(Path, Charset)} for lazy evaluation.</p>
 * <p>流式的、函数式的逐行文件处理器，累积操作并在调用终端操作时惰性地应用它们。
 * 基于{@link Files#lines(Path, Charset)}实现惰性求值。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fluent API with immutable pipeline - 不可变管道的流式API</li>
 *   <li>Lazy evaluation - 惰性求值</li>
 *   <li>Filter, map, skip, limit operations - 过滤、映射、跳过、限制操作</li>
 *   <li>Convenience shortcuts (trim, nonEmpty, grep) - 便捷快捷方式</li>
 *   <li>Multiple terminal operations - 多种终端操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Read non-empty trimmed lines
 * List<String> lines = LineProcessor.of(path)
 *     .trim()
 *     .nonEmpty()
 *     .collect();
 *
 * // Grep and limit
 * List<String> errors = LineProcessor.of(path)
 *     .grep("ERROR")
 *     .limit(100)
 *     .collect();
 *
 * // Process and write to output
 * LineProcessor.of(input)
 *     .filter(line -> !line.startsWith("#"))
 *     .map(String::toUpperCase)
 *     .toFile(output);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.3
 */
public final class LineProcessor {

    private final Path path;
    private final Charset charset;
    private final List<PipelineOp> operations;

    /**
     * Sealed interface for pipeline operations.
     */
    private sealed interface PipelineOp {
    }

    private record FilterOp(Predicate<String> predicate) implements PipelineOp {
    }

    private record MapOp(UnaryOperator<String> mapper) implements PipelineOp {
    }

    private record SkipOp(long n) implements PipelineOp {
    }

    private record LimitOp(long maxLines) implements PipelineOp {
    }

    private LineProcessor(Path path, Charset charset, List<PipelineOp> operations) {
        this.path = path;
        this.charset = charset;
        this.operations = operations;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates a LineProcessor for the given path with UTF-8 charset
     * 使用UTF-8字符集为给定路径创建LineProcessor
     *
     * @param path the file path | 文件路径
     * @return new LineProcessor instance | 新的LineProcessor实例
     * @throws NullPointerException if path is null | 当path为null时抛出
     */
    public static LineProcessor of(Path path) {
        return of(path, StandardCharsets.UTF_8);
    }

    /**
     * Creates a LineProcessor for the given path with specified charset
     * 使用指定字符集为给定路径创建LineProcessor
     *
     * @param path    the file path | 文件路径
     * @param charset the charset for reading | 读取使用的字符集
     * @return new LineProcessor instance | 新的LineProcessor实例
     * @throws NullPointerException if path or charset is null | 当path或charset为null时抛出
     */
    public static LineProcessor of(Path path, Charset charset) {
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(charset, "charset must not be null");
        return new LineProcessor(path, charset, List.of());
    }

    /**
     * Creates a LineProcessor for the given path string with UTF-8 charset
     * 使用UTF-8字符集为给定路径字符串创建LineProcessor
     *
     * @param path the file path string | 文件路径字符串
     * @return new LineProcessor instance | 新的LineProcessor实例
     * @throws NullPointerException if path is null | 当path为null时抛出
     */
    public static LineProcessor of(String path) {
        Objects.requireNonNull(path, "path must not be null");
        return of(Path.of(path));
    }

    // ==================== Intermediate Operations | 中间操作 ====================

    /**
     * Filters lines matching the predicate
     * 过滤匹配谓词的行
     *
     * @param predicate the filter predicate | 过滤谓词
     * @return new LineProcessor with filter applied | 应用过滤器的新LineProcessor
     * @throws NullPointerException if predicate is null | 当predicate为null时抛出
     */
    public LineProcessor filter(Predicate<String> predicate) {
        Objects.requireNonNull(predicate, "predicate must not be null");
        return withOp(new FilterOp(predicate));
    }

    /**
     * Maps each line using the mapper function
     * 使用映射函数映射每一行
     *
     * @param mapper the mapping function | 映射函数
     * @return new LineProcessor with map applied | 应用映射的新LineProcessor
     * @throws NullPointerException if mapper is null | 当mapper为null时抛出
     */
    public LineProcessor map(UnaryOperator<String> mapper) {
        Objects.requireNonNull(mapper, "mapper must not be null");
        return withOp(new MapOp(mapper));
    }

    /**
     * Skips the first N lines
     * 跳过前N行
     *
     * @param n the number of lines to skip | 要跳过的行数
     * @return new LineProcessor with skip applied | 应用跳过的新LineProcessor
     * @throws IllegalArgumentException if n is negative | 当n为负数时抛出
     */
    public LineProcessor skip(long n) {
        if (n < 0) {
            throw new IllegalArgumentException("skip count must not be negative, got: " + n);
        }
        return withOp(new SkipOp(n));
    }

    /**
     * Limits the number of lines to process
     * 限制要处理的行数
     *
     * @param maxLines the maximum number of lines | 最大行数
     * @return new LineProcessor with limit applied | 应用限制的新LineProcessor
     * @throws IllegalArgumentException if maxLines is negative | 当maxLines为负数时抛出
     */
    public LineProcessor limit(long maxLines) {
        if (maxLines < 0) {
            throw new IllegalArgumentException("limit must not be negative, got: " + maxLines);
        }
        return withOp(new LimitOp(maxLines));
    }

    /**
     * Trims whitespace from each line (shortcut for {@code map(String::trim)})
     * 去除每行的空白字符（{@code map(String::trim)}的快捷方式）
     *
     * @return new LineProcessor with trim applied | 应用去空白的新LineProcessor
     */
    public LineProcessor trim() {
        return map(String::trim);
    }

    /**
     * Filters out empty lines (shortcut for {@code filter(s -> !s.isEmpty())})
     * 过滤掉空行（{@code filter(s -> !s.isEmpty())}的快捷方式）
     *
     * @return new LineProcessor with non-empty filter applied | 应用非空过滤的新LineProcessor
     */
    public LineProcessor nonEmpty() {
        return filter(s -> !s.isEmpty());
    }

    /**
     * Filters lines matching the given regex pattern
     * 过滤匹配给定正则表达式模式的行
     *
     * @param regex the regex pattern to match | 要匹配的正则表达式模式
     * @return new LineProcessor with grep filter applied | 应用grep过滤的新LineProcessor
     * @throws NullPointerException if regex is null | 当regex为null时抛出
     */
    public LineProcessor grep(String regex) {
        Objects.requireNonNull(regex, "regex must not be null");
        Pattern pattern = Pattern.compile(regex);
        return filter(line -> pattern.matcher(line).find());
    }

    // ==================== Terminal Operations | 终端操作 ====================

    /**
     * Applies the action to each processed line
     * 对每个处理后的行应用操作
     *
     * @param action the action to apply | 要应用的操作
     * @throws NullPointerException     if action is null | 当action为null时抛出
     * @throws OpenIOOperationException if an I/O error occurs | 当发生I/O错误时抛出
     */
    public void forEach(Consumer<String> action) {
        Objects.requireNonNull(action, "action must not be null");
        try (Stream<String> stream = buildStream()) {
            stream.forEach(action);
        }
    }

    /**
     * Collects all processed lines into a list
     * 将所有处理后的行收集到列表中
     *
     * @return list of processed lines | 处理后的行列表
     * @throws OpenIOOperationException if an I/O error occurs | 当发生I/O错误时抛出
     */
    public List<String> collect() {
        try (Stream<String> stream = buildStream()) {
            return stream.toList();
        }
    }

    /**
     * Counts the number of processed lines
     * 统计处理后的行数
     *
     * @return the line count | 行数
     * @throws OpenIOOperationException if an I/O error occurs | 当发生I/O错误时抛出
     */
    public long count() {
        try (Stream<String> stream = buildStream()) {
            return stream.count();
        }
    }

    /**
     * Reduces the processed lines using the given identity and accumulator
     * 使用给定的初始值和累加器归约处理后的行
     *
     * @param identity    the identity value | 初始值
     * @param accumulator the accumulator function | 累加器函数
     * @return the reduced result | 归约结果
     * @throws NullPointerException     if accumulator is null | 当accumulator为null时抛出
     * @throws OpenIOOperationException if an I/O error occurs | 当发生I/O错误时抛出
     */
    public String reduce(String identity, BinaryOperator<String> accumulator) {
        Objects.requireNonNull(accumulator, "accumulator must not be null");
        try (Stream<String> stream = buildStream()) {
            return stream.reduce(identity, accumulator);
        }
    }

    /**
     * Writes the processed lines to an output file using UTF-8
     * 使用UTF-8将处理后的行写入输出文件
     *
     * @param output the output file path | 输出文件路径
     * @throws NullPointerException     if output is null | 当output为null时抛出
     * @throws OpenIOOperationException if an I/O error occurs | 当发生I/O错误时抛出
     */
    public void toFile(Path output) {
        toFile(output, StandardCharsets.UTF_8);
    }

    /**
     * Writes the processed lines to an output file with specified charset
     * 使用指定字符集将处理后的行写入输出文件
     *
     * @param output  the output file path | 输出文件路径
     * @param charset the charset for writing | 写入使用的字符集
     * @throws NullPointerException     if output or charset is null | 当output或charset为null时抛出
     * @throws OpenIOOperationException if an I/O error occurs | 当发生I/O错误时抛出
     */
    public void toFile(Path output, Charset charset) {
        Objects.requireNonNull(output, "output must not be null");
        Objects.requireNonNull(charset, "charset must not be null");
        try (Stream<String> stream = buildStream()) {
            List<String> lines = stream.toList();
            Files.write(output, lines, charset);
        } catch (IOException e) {
            throw OpenIOOperationException.writeFailed(output, e);
        }
    }

    /**
     * Finds the first processed line
     * 查找第一个处理后的行
     *
     * @return the first line, or empty if none | 第一行，如果没有则为空
     * @throws OpenIOOperationException if an I/O error occurs | 当发生I/O错误时抛出
     */
    public Optional<String> findFirst() {
        try (Stream<String> stream = buildStream()) {
            return stream.findFirst();
        }
    }

    /**
     * Tests whether any processed line matches the predicate
     * 测试是否有任何处理后的行匹配谓词
     *
     * @param predicate the predicate to test | 要测试的谓词
     * @return true if any line matches | 如果有行匹配则返回true
     * @throws NullPointerException     if predicate is null | 当predicate为null时抛出
     * @throws OpenIOOperationException if an I/O error occurs | 当发生I/O错误时抛出
     */
    public boolean anyMatch(Predicate<String> predicate) {
        Objects.requireNonNull(predicate, "predicate must not be null");
        try (Stream<String> stream = buildStream()) {
            return stream.anyMatch(predicate);
        }
    }

    // ==================== Internal | 内部方法 ====================

    /**
     * Creates a new LineProcessor with an additional operation.
     */
    private LineProcessor withOp(PipelineOp op) {
        List<PipelineOp> newOps = new ArrayList<>(operations.size() + 1);
        newOps.addAll(operations);
        newOps.add(op);
        return new LineProcessor(path, charset, Collections.unmodifiableList(newOps));
    }

    /**
     * Builds the stream with all accumulated operations applied.
     */
    private Stream<String> buildStream() {
        Stream<String> stream;
        try {
            stream = Files.lines(path, charset);
        } catch (IOException e) {
            throw OpenIOOperationException.readFailed(path, e);
        }

        for (PipelineOp op : operations) {
            stream = switch (op) {
                case FilterOp f -> stream.filter(f.predicate());
                case MapOp m -> stream.map(m.mapper());
                case SkipOp s -> stream.skip(s.n());
                case LimitOp l -> stream.limit(l.maxLines());
            };
        }

        return stream;
    }
}
