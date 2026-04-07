package cloud.opencode.base.csv;

import cloud.opencode.base.csv.bind.CsvBinder;
import cloud.opencode.base.csv.diff.CsvChange;
import cloud.opencode.base.csv.diff.CsvDiff;
import cloud.opencode.base.csv.exception.OpenCsvException;
import cloud.opencode.base.csv.internal.CsvFormatter;
import cloud.opencode.base.csv.internal.CsvParser;
import cloud.opencode.base.csv.merge.CsvMerge;
import cloud.opencode.base.csv.query.CsvQuery;
import cloud.opencode.base.csv.split.CsvSplit;
import cloud.opencode.base.csv.stats.CsvColumnStats;
import cloud.opencode.base.csv.stats.CsvStats;
import cloud.opencode.base.csv.stream.CsvReader;
import cloud.opencode.base.csv.stream.CsvWriter;
import cloud.opencode.base.csv.transform.CsvTransform;
import cloud.opencode.base.csv.validator.CsvValidationResult;
import cloud.opencode.base.csv.validator.CsvValidator;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * OpenCsv - Unified CSV Processing Facade
 * OpenCsv - 统一CSV处理门面
 *
 * <p>This is the primary entry point for all CSV operations in the OpenCode CSV library.
 * Provides a comprehensive static API for parsing, writing, binding, streaming, diffing,
 * and validating CSV data. All methods delegate to specialized internal components.</p>
 * <p>这是OpenCode CSV库中所有CSV操作的主要入口点。提供全面的静态API用于解析、写入、绑定、
 * 流处理、差异比较和验证CSV数据。所有方法委托给专门的内部组件。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>RFC 4180 compliant parsing and writing - 符合RFC 4180的解析和写入</li>
 *   <li>Multiple input/output sources (String, Path, Stream, Reader/Writer) - 多种输入/输出源</li>
 *   <li>Object binding for Records and POJOs - Record和POJO的对象绑定</li>
 *   <li>Streaming read/write for large files - 大文件的流式读写</li>
 *   <li>CSV document diff/change detection - CSV文档差异/变更检测</li>
 *   <li>Validation and utility methods - 验证和工具方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Parse CSV string
 * CsvDocument doc = OpenCsv.parse("name,age\nAlice,30\nBob,25");
 *
 * // Parse CSV file
 * CsvDocument doc = OpenCsv.parseFile(Path.of("data.csv"));
 *
 * // Bind to objects
 * List<Person> people = OpenCsv.bind("name,age\nAlice,30", Person.class);
 *
 * // Write CSV
 * String csv = OpenCsv.dump(doc);
 * OpenCsv.writeFile(doc, Path.of("output.csv"));
 *
 * // Compare documents
 * List<CsvChange> changes = OpenCsv.diff(original, modified);
 *
 * // Streaming
 * try (CsvReader reader = OpenCsv.reader(path)) {
 *     reader.stream().forEach(row -> process(row));
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless facade) - 线程安全: 是（无状态门面）</li>
 *   <li>Null-safe: Yes (validates all inputs) - 空值安全: 是（验证所有输入）</li>
 *   <li>DoS protection: Configurable row/column/field limits - DoS保护: 可配置的行/列/字段限制</li>
 *   <li>Formula injection protection via CsvConfig - 通过CsvConfig的公式注入保护</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see CsvDocument
 * @see CsvConfig
 * @see CsvBinder
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-csv V1.0.3
 */
public final class OpenCsv {

    private OpenCsv() {
        // utility class - facade pattern
    }

    // ==================== Parse Methods | 解析方法 ====================

    /**
     * Parses a CSV string into a document using default configuration
     * 使用默认配置将CSV字符串解析为文档
     *
     * @param csv the CSV string | CSV字符串
     * @return the parsed document | 解析后的文档
     * @throws OpenCsvException if parsing fails | 如果解析失败
     */
    public static CsvDocument parse(String csv) {
        return parse(csv, CsvConfig.DEFAULT);
    }

    /**
     * Parses a CSV string into a document with the specified configuration
     * 使用指定配置将CSV字符串解析为文档
     *
     * @param csv    the CSV string | CSV字符串
     * @param config the CSV configuration | CSV配置
     * @return the parsed document | 解析后的文档
     * @throws OpenCsvException if parsing fails | 如果解析失败
     */
    public static CsvDocument parse(String csv, CsvConfig config) {
        Objects.requireNonNull(csv, "csv must not be null");
        Objects.requireNonNull(config, "config must not be null");
        return CsvParser.parse(csv, config);
    }

    /**
     * Parses a CSV file using default configuration
     * 使用默认配置解析CSV文件
     *
     * @param file the file path | 文件路径
     * @return the parsed document | 解析后的文档
     * @throws OpenCsvException if parsing or I/O fails | 如果解析或I/O失败
     */
    public static CsvDocument parseFile(Path file) {
        return parseFile(file, CsvConfig.DEFAULT);
    }

    /**
     * Parses a CSV file with the specified configuration
     * 使用指定配置解析CSV文件
     *
     * @param file   the file path | 文件路径
     * @param config the CSV configuration | CSV配置
     * @return the parsed document | 解析后的文档
     * @throws OpenCsvException if parsing or I/O fails | 如果解析或I/O失败
     */
    public static CsvDocument parseFile(Path file, CsvConfig config) {
        Objects.requireNonNull(file, "file must not be null");
        Objects.requireNonNull(config, "config must not be null");
        try (Reader reader = Files.newBufferedReader(file, config.charset())) {
            return CsvParser.parse(reader, config);
        } catch (OpenCsvException e) {
            throw e;
        } catch (IOException e) {
            throw OpenCsvException.ioError("Failed to read file: " + file, e);
        }
    }

    /**
     * Parses a CSV file with the specified charset and default configuration
     * 使用指定字符集和默认配置解析CSV文件
     *
     * @param file    the file path | 文件路径
     * @param charset the character set | 字符集
     * @return the parsed document | 解析后的文档
     * @throws OpenCsvException if parsing or I/O fails | 如果解析或I/O失败
     */
    public static CsvDocument parseFile(Path file, Charset charset) {
        Objects.requireNonNull(charset, "charset must not be null");
        CsvConfig config = CsvConfig.builder().charset(charset).build();
        return parseFile(file, config);
    }

    /**
     * Parses CSV data from an input stream using default configuration
     * 使用默认配置从输入流解析CSV数据
     *
     * @param input the input stream | 输入流
     * @return the parsed document | 解析后的文档
     * @throws OpenCsvException if parsing fails | 如果解析失败
     */
    public static CsvDocument parse(InputStream input) {
        return parse(input, CsvConfig.DEFAULT);
    }

    /**
     * Parses CSV data from an input stream with the specified configuration
     * 使用指定配置从输入流解析CSV数据
     *
     * @param input  the input stream | 输入流
     * @param config the CSV configuration | CSV配置
     * @return the parsed document | 解析后的文档
     * @throws OpenCsvException if parsing fails | 如果解析失败
     */
    public static CsvDocument parse(InputStream input, CsvConfig config) {
        Objects.requireNonNull(input, "input must not be null");
        Objects.requireNonNull(config, "config must not be null");
        Reader reader = new java.io.BufferedReader(new InputStreamReader(input, config.charset()));
        return CsvParser.parse(reader, config);
    }

    /**
     * Parses CSV data from a reader using default configuration
     * 使用默认配置从Reader解析CSV数据
     *
     * @param reader the reader | 读取器
     * @return the parsed document | 解析后的文档
     * @throws OpenCsvException if parsing fails | 如果解析失败
     */
    public static CsvDocument parse(Reader reader) {
        return parse(reader, CsvConfig.DEFAULT);
    }

    /**
     * Parses CSV data from a reader with the specified configuration
     * 使用指定配置从Reader解析CSV数据
     *
     * @param reader the reader | 读取器
     * @param config the CSV configuration | CSV配置
     * @return the parsed document | 解析后的文档
     * @throws OpenCsvException if parsing fails | 如果解析失败
     */
    public static CsvDocument parse(Reader reader, CsvConfig config) {
        Objects.requireNonNull(reader, "reader must not be null");
        Objects.requireNonNull(config, "config must not be null");
        return CsvParser.parse(reader, config);
    }

    // ==================== Write Methods | 写入方法 ====================

    /**
     * Formats a CSV document to a string using default configuration
     * 使用默认配置将CSV文档格式化为字符串
     *
     * @param doc the CSV document | CSV文档
     * @return the CSV string | CSV字符串
     * @throws OpenCsvException if formatting fails | 如果格式化失败
     */
    public static String dump(CsvDocument doc) {
        return dump(doc, CsvConfig.DEFAULT);
    }

    /**
     * Formats a CSV document to a string with the specified configuration
     * 使用指定配置将CSV文档格式化为字符串
     *
     * @param doc    the CSV document | CSV文档
     * @param config the CSV configuration | CSV配置
     * @return the CSV string | CSV字符串
     * @throws OpenCsvException if formatting fails | 如果格式化失败
     */
    public static String dump(CsvDocument doc, CsvConfig config) {
        Objects.requireNonNull(doc, "doc must not be null");
        Objects.requireNonNull(config, "config must not be null");
        return CsvFormatter.format(doc, config);
    }

    /**
     * Writes a CSV document to a file using default configuration
     * 使用默认配置将CSV文档写入文件
     *
     * @param doc  the CSV document | CSV文档
     * @param file the file path | 文件路径
     * @throws OpenCsvException if writing or I/O fails | 如果写入或I/O失败
     */
    public static void writeFile(CsvDocument doc, Path file) {
        writeFile(doc, file, CsvConfig.DEFAULT);
    }

    /**
     * Writes a CSV document to a file with the specified configuration
     * 使用指定配置将CSV文档写入文件
     *
     * @param doc    the CSV document | CSV文档
     * @param file   the file path | 文件路径
     * @param config the CSV configuration | CSV配置
     * @throws OpenCsvException if writing or I/O fails | 如果写入或I/O失败
     */
    public static void writeFile(CsvDocument doc, Path file, CsvConfig config) {
        Objects.requireNonNull(doc, "doc must not be null");
        Objects.requireNonNull(file, "file must not be null");
        Objects.requireNonNull(config, "config must not be null");
        try (Writer writer = Files.newBufferedWriter(file, config.charset())) {
            CsvFormatter.format(doc, writer, config);
        } catch (OpenCsvException e) {
            throw e;
        } catch (IOException e) {
            throw OpenCsvException.ioError("Failed to write file: " + file, e);
        }
    }

    /**
     * Writes a CSV document to an output stream using default configuration
     * 使用默认配置将CSV文档写入输出流
     *
     * @param doc    the CSV document | CSV文档
     * @param output the output stream | 输出流
     * @throws OpenCsvException if writing fails | 如果写入失败
     */
    public static void write(CsvDocument doc, OutputStream output) {
        write(doc, output, CsvConfig.DEFAULT);
    }

    /**
     * Writes a CSV document to an output stream with the specified configuration
     * 使用指定配置将CSV文档写入输出流
     *
     * @param doc    the CSV document | CSV文档
     * @param output the output stream | 输出流
     * @param config the CSV configuration | CSV配置
     * @throws OpenCsvException if writing fails | 如果写入失败
     */
    public static void write(CsvDocument doc, OutputStream output, CsvConfig config) {
        Objects.requireNonNull(doc, "doc must not be null");
        Objects.requireNonNull(output, "output must not be null");
        Objects.requireNonNull(config, "config must not be null");
        try {
            java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(output, config.charset());
            CsvFormatter.format(doc, writer, config);
            writer.flush();
        } catch (OpenCsvException e) {
            throw e;
        } catch (IOException e) {
            throw OpenCsvException.writeError("Failed to write to output stream", e);
        }
    }

    /**
     * Writes a CSV document to a writer using default configuration
     * 使用默认配置将CSV文档写入Writer
     *
     * @param doc    the CSV document | CSV文档
     * @param writer the writer | 写入器
     * @throws OpenCsvException if writing fails | 如果写入失败
     */
    public static void write(CsvDocument doc, Writer writer) {
        write(doc, writer, CsvConfig.DEFAULT);
    }

    /**
     * Writes a CSV document to a writer with the specified configuration
     * 使用指定配置将CSV文档写入Writer
     *
     * @param doc    the CSV document | CSV文档
     * @param writer the writer | 写入器
     * @param config the CSV configuration | CSV配置
     * @throws OpenCsvException if writing fails | 如果写入失败
     */
    public static void write(CsvDocument doc, Writer writer, CsvConfig config) {
        Objects.requireNonNull(doc, "doc must not be null");
        Objects.requireNonNull(writer, "writer must not be null");
        Objects.requireNonNull(config, "config must not be null");
        CsvFormatter.format(doc, writer, config);
    }

    // ==================== Binding Methods | 绑定方法 ====================

    /**
     * Parses a CSV string and binds rows to objects using default configuration
     * 使用默认配置解析CSV字符串并将行绑定到对象
     *
     * @param csv  the CSV string | CSV字符串
     * @param type the target type | 目标类型
     * @param <T>  the target type | 目标类型
     * @return list of bound objects | 绑定的对象列表
     * @throws OpenCsvException if parsing or binding fails | 如果解析或绑定失败
     */
    public static <T> List<T> bind(String csv, Class<T> type) {
        return bind(csv, type, CsvConfig.DEFAULT);
    }

    /**
     * Parses a CSV string and binds rows to objects with the specified configuration
     * 使用指定配置解析CSV字符串并将行绑定到对象
     *
     * @param csv    the CSV string | CSV字符串
     * @param type   the target type | 目标类型
     * @param config the CSV configuration | CSV配置
     * @param <T>    the target type | 目标类型
     * @return list of bound objects | 绑定的对象列表
     * @throws OpenCsvException if parsing or binding fails | 如果解析或绑定失败
     */
    public static <T> List<T> bind(String csv, Class<T> type, CsvConfig config) {
        CsvDocument doc = parse(csv, config);
        return CsvBinder.bind(doc, type);
    }

    /**
     * Binds CSV document rows to objects
     * 将CSV文档行绑定到对象
     *
     * @param doc  the CSV document | CSV文档
     * @param type the target type | 目标类型
     * @param <T>  the target type | 目标类型
     * @return list of bound objects | 绑定的对象列表
     * @throws OpenCsvException if binding fails | 如果绑定失败
     */
    public static <T> List<T> bind(CsvDocument doc, Class<T> type) {
        return CsvBinder.bind(doc, type);
    }

    /**
     * Parses a CSV file and binds rows to objects using default configuration
     * 使用默认配置解析CSV文件并将行绑定到对象
     *
     * @param file the file path | 文件路径
     * @param type the target type | 目标类型
     * @param <T>  the target type | 目标类型
     * @return list of bound objects | 绑定的对象列表
     * @throws OpenCsvException if parsing, I/O, or binding fails | 如果解析、I/O或绑定失败
     */
    public static <T> List<T> bindFile(Path file, Class<T> type) {
        return bindFile(file, type, CsvConfig.DEFAULT);
    }

    /**
     * Parses a CSV file and binds rows to objects with the specified configuration
     * 使用指定配置解析CSV文件并将行绑定到对象
     *
     * @param file   the file path | 文件路径
     * @param type   the target type | 目标类型
     * @param config the CSV configuration | CSV配置
     * @param <T>    the target type | 目标类型
     * @return list of bound objects | 绑定的对象列表
     * @throws OpenCsvException if parsing, I/O, or binding fails | 如果解析、I/O或绑定失败
     */
    public static <T> List<T> bindFile(Path file, Class<T> type, CsvConfig config) {
        CsvDocument doc = parseFile(file, config);
        return CsvBinder.bind(doc, type);
    }

    /**
     * Converts a collection of objects to a CSV document
     * 将对象集合转换为CSV文档
     *
     * @param objects the objects | 对象集合
     * @param type    the object type | 对象类型
     * @param <T>     the object type | 对象类型
     * @return the CSV document | CSV文档
     * @throws OpenCsvException if conversion fails | 如果转换失败
     */
    public static <T> CsvDocument fromObjects(Collection<T> objects, Class<T> type) {
        return CsvBinder.fromObjects(objects, type);
    }

    /**
     * Converts objects to a CSV string using default configuration
     * 使用默认配置将对象转换为CSV字符串
     *
     * @param objects the objects | 对象集合
     * @param type    the object type | 对象类型
     * @param <T>     the object type | 对象类型
     * @return the CSV string | CSV字符串
     * @throws OpenCsvException if conversion fails | 如果转换失败
     */
    public static <T> String dumpObjects(Collection<T> objects, Class<T> type) {
        return dumpObjects(objects, type, CsvConfig.DEFAULT);
    }

    /**
     * Converts objects to a CSV string with the specified configuration
     * 使用指定配置将对象转换为CSV字符串
     *
     * @param objects the objects | 对象集合
     * @param type    the object type | 对象类型
     * @param config  the CSV configuration | CSV配置
     * @param <T>     the object type | 对象类型
     * @return the CSV string | CSV字符串
     * @throws OpenCsvException if conversion fails | 如果转换失败
     */
    public static <T> String dumpObjects(Collection<T> objects, Class<T> type, CsvConfig config) {
        CsvDocument doc = CsvBinder.fromObjects(objects, type);
        return dump(doc, config);
    }

    // ==================== Streaming Methods | 流式方法 ====================

    /**
     * Creates a streaming CSV reader from an input stream using default configuration
     * 使用默认配置从输入流创建流式CSV读取器
     *
     * @param input the input stream | 输入流
     * @return the CSV reader | CSV读取器
     */
    public static CsvReader reader(InputStream input) {
        return reader(input, CsvConfig.DEFAULT);
    }

    /**
     * Creates a streaming CSV reader from an input stream with configuration
     * 使用配置从输入流创建流式CSV读取器
     *
     * @param input  the input stream | 输入流
     * @param config the CSV configuration | CSV配置
     * @return the CSV reader | CSV读取器
     */
    public static CsvReader reader(InputStream input, CsvConfig config) {
        Objects.requireNonNull(input, "input must not be null");
        Objects.requireNonNull(config, "config must not be null");
        return CsvReader.of(input, config);
    }

    /**
     * Creates a streaming CSV reader from a reader using default configuration
     * 使用默认配置从Reader创建流式CSV读取器
     *
     * @param reader the reader | 读取器
     * @return the CSV reader | CSV读取器
     */
    public static CsvReader reader(Reader reader) {
        return reader(reader, CsvConfig.DEFAULT);
    }

    /**
     * Creates a streaming CSV reader from a reader with configuration
     * 使用配置从Reader创建流式CSV读取器
     *
     * @param reader the reader | 读取器
     * @param config the CSV configuration | CSV配置
     * @return the CSV reader | CSV读取器
     */
    public static CsvReader reader(Reader reader, CsvConfig config) {
        Objects.requireNonNull(reader, "reader must not be null");
        Objects.requireNonNull(config, "config must not be null");
        return CsvReader.of(reader, config);
    }

    /**
     * Creates a streaming CSV reader from a file using default configuration
     * 使用默认配置从文件创建流式CSV读取器
     *
     * @param file the file path | 文件路径
     * @return the CSV reader | CSV读取器
     * @throws OpenCsvException if the file cannot be opened | 如果无法打开文件
     */
    public static CsvReader reader(Path file) {
        return reader(file, CsvConfig.DEFAULT);
    }

    /**
     * Creates a streaming CSV reader from a file with configuration
     * 使用配置从文件创建流式CSV读取器
     *
     * @param file   the file path | 文件路径
     * @param config the CSV configuration | CSV配置
     * @return the CSV reader | CSV读取器
     * @throws OpenCsvException if the file cannot be opened | 如果无法打开文件
     */
    public static CsvReader reader(Path file, CsvConfig config) {
        Objects.requireNonNull(file, "file must not be null");
        Objects.requireNonNull(config, "config must not be null");
        return CsvReader.of(file, config);
    }

    /**
     * Creates a streaming CSV writer to an output stream using default configuration
     * 使用默认配置创建到输出流的流式CSV写入器
     *
     * @param output the output stream | 输出流
     * @return the CSV writer | CSV写入器
     */
    public static CsvWriter writer(OutputStream output) {
        return writer(output, CsvConfig.DEFAULT);
    }

    /**
     * Creates a streaming CSV writer to an output stream with configuration
     * 使用配置创建到输出流的流式CSV写入器
     *
     * @param output the output stream | 输出流
     * @param config the CSV configuration | CSV配置
     * @return the CSV writer | CSV写入器
     */
    public static CsvWriter writer(OutputStream output, CsvConfig config) {
        Objects.requireNonNull(output, "output must not be null");
        Objects.requireNonNull(config, "config must not be null");
        return CsvWriter.of(output, config);
    }

    /**
     * Creates a streaming CSV writer to a writer using default configuration
     * 使用默认配置创建到Writer的流式CSV写入器
     *
     * @param writer the writer | 写入器
     * @return the CSV writer | CSV写入器
     */
    public static CsvWriter writer(Writer writer) {
        return writer(writer, CsvConfig.DEFAULT);
    }

    /**
     * Creates a streaming CSV writer to a writer with configuration
     * 使用配置创建到Writer的流式CSV写入器
     *
     * @param writer the writer | 写入器
     * @param config the CSV configuration | CSV配置
     * @return the CSV writer | CSV写入器
     */
    public static CsvWriter writer(Writer writer, CsvConfig config) {
        Objects.requireNonNull(writer, "writer must not be null");
        Objects.requireNonNull(config, "config must not be null");
        return CsvWriter.of(writer, config);
    }

    /**
     * Creates a streaming CSV writer to a file using default configuration
     * 使用默认配置创建到文件的流式CSV写入器
     *
     * @param file the file path | 文件路径
     * @return the CSV writer | CSV写入器
     * @throws OpenCsvException if the file cannot be opened | 如果无法打开文件
     */
    public static CsvWriter writer(Path file) {
        return writer(file, CsvConfig.DEFAULT);
    }

    /**
     * Creates a streaming CSV writer to a file with configuration
     * 使用配置创建到文件的流式CSV写入器
     *
     * @param file   the file path | 文件路径
     * @param config the CSV configuration | CSV配置
     * @return the CSV writer | CSV写入器
     * @throws OpenCsvException if the file cannot be opened | 如果无法打开文件
     */
    public static CsvWriter writer(Path file, CsvConfig config) {
        Objects.requireNonNull(file, "file must not be null");
        Objects.requireNonNull(config, "config must not be null");
        return CsvWriter.of(file, config);
    }

    // ==================== Utility Methods | 工具方法 ====================

    /**
     * Checks if a string is valid CSV
     * 检查字符串是否为有效的CSV
     *
     * @param csv the CSV string | CSV字符串
     * @return true if valid | 如果有效返回true
     */
    public static boolean isValid(String csv) {
        if (csv == null || csv.isEmpty()) {
            return false;
        }
        try {
            CsvParser.parse(csv, CsvConfig.DEFAULT);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Counts the number of data rows in a CSV string (excluding header)
     * 计算CSV字符串中的数据行数（不含标题行）
     *
     * @param csv the CSV string | CSV字符串
     * @return the row count | 行数
     * @throws OpenCsvException if parsing fails | 如果解析失败
     */
    public static int rowCount(String csv) {
        Objects.requireNonNull(csv, "csv must not be null");
        CsvDocument doc = CsvParser.parse(csv, CsvConfig.DEFAULT);
        return doc.rowCount();
    }

    /**
     * Extracts headers from a CSV string using default configuration
     * 使用默认配置从CSV字符串提取标题
     *
     * @param csv the CSV string | CSV字符串
     * @return the header list | 标题列表
     * @throws OpenCsvException if parsing fails | 如果解析失败
     */
    public static List<String> headers(String csv) {
        return headers(csv, CsvConfig.DEFAULT);
    }

    /**
     * Extracts headers from a CSV string with the specified configuration
     * 使用指定配置从CSV字符串提取标题
     *
     * @param csv    the CSV string | CSV字符串
     * @param config the CSV configuration | CSV配置
     * @return the header list | 标题列表
     * @throws OpenCsvException if parsing fails | 如果解析失败
     */
    public static List<String> headers(String csv, CsvConfig config) {
        Objects.requireNonNull(csv, "csv must not be null");
        Objects.requireNonNull(config, "config must not be null");
        CsvDocument doc = CsvParser.parse(csv, config);
        return doc.headers();
    }

    // ==================== Diff Methods | 差异方法 ====================

    /**
     * Computes differences between two CSV documents
     * 计算两个CSV文档之间的差异
     *
     * @param original the original document | 原始文档
     * @param modified the modified document | 修改后的文档
     * @return list of changes | 变更列表
     */
    public static List<CsvChange> diff(CsvDocument original, CsvDocument modified) {
        return CsvDiff.diff(original, modified);
    }

    /**
     * Computes differences using a key column for row matching
     * 使用键列进行行匹配来计算差异
     *
     * @param original  the original document | 原始文档
     * @param modified  the modified document | 修改后的文档
     * @param keyColumn the key column header name | 键列标题名称
     * @return list of changes | 变更列表
     */
    public static List<CsvChange> diffByKey(CsvDocument original, CsvDocument modified,
                                             String keyColumn) {
        return CsvDiff.diffByKey(original, modified, keyColumn);
    }

    // ==================== Query Methods | 查询方法 ====================

    /**
     * Creates a fluent query from a CSV document
     * 从CSV文档创建流式查询
     *
     * @param doc the CSV document | CSV文档
     * @return a new CsvQuery builder | 新的CsvQuery构建器
     */
    public static CsvQuery query(CsvDocument doc) {
        return CsvQuery.from(doc);
    }

    // ==================== Transform Methods | 转换方法 ====================

    /**
     * Creates a fluent transformation pipeline from a CSV document
     * 从CSV文档创建流式转换管道
     *
     * @param doc the CSV document | CSV文档
     * @return a new CsvTransform builder | 新的CsvTransform构建器
     */
    public static CsvTransform transform(CsvDocument doc) {
        return CsvTransform.from(doc);
    }

    // ==================== Merge Methods | 合并方法 ====================

    /**
     * Concatenates multiple CSV documents vertically (appends rows)
     * 纵向连接多个CSV文档（追加行）
     *
     * @param docs the documents to concatenate | 要连接的文档
     * @return the merged document | 合并后的文档
     */
    public static CsvDocument concat(CsvDocument... docs) {
        return CsvMerge.concat(docs);
    }

    /**
     * Joins two CSV documents using inner join on a key column
     * 使用内连接在键列上连接两个CSV文档
     *
     * @param left      the left document | 左文档
     * @param right     the right document | 右文档
     * @param keyColumn the key column name | 键列名
     * @return the joined document | 连接后的文档
     */
    public static CsvDocument innerJoin(CsvDocument left, CsvDocument right, String keyColumn) {
        return CsvMerge.innerJoin(left, right, keyColumn);
    }

    /**
     * Joins two CSV documents using left join on a key column
     * 使用左连接在键列上连接两个CSV文档
     *
     * @param left      the left document | 左文档
     * @param right     the right document | 右文档
     * @param keyColumn the key column name | 键列名
     * @return the joined document | 连接后的文档
     */
    public static CsvDocument leftJoin(CsvDocument left, CsvDocument right, String keyColumn) {
        return CsvMerge.leftJoin(left, right, keyColumn);
    }

    // ==================== Stats Methods | 统计方法 ====================

    /**
     * Computes summary statistics for a column
     * 计算列的摘要统计信息
     *
     * @param doc    the CSV document | CSV文档
     * @param column the column name | 列名
     * @return the column statistics | 列统计信息
     */
    public static CsvColumnStats stats(CsvDocument doc, String column) {
        return CsvStats.summary(doc, column);
    }

    // ==================== Split Methods | 分割方法 ====================

    /**
     * Splits a CSV document into chunks of the specified size
     * 将CSV文档按指定大小分割为块
     *
     * @param doc     the CSV document | CSV文档
     * @param maxRows the maximum rows per chunk | 每块最大行数
     * @return list of document chunks | 文档块列表
     */
    public static java.util.List<CsvDocument> split(CsvDocument doc, int maxRows) {
        return CsvSplit.bySize(doc, maxRows);
    }

    // ==================== Validator Methods | 校验方法 ====================

    /**
     * Creates a new CSV validator builder
     * 创建新的CSV校验器构建器
     *
     * @return a new validator builder | 新的校验器构建器
     */
    public static CsvValidator.Builder validator() {
        return CsvValidator.builder();
    }

    // ==================== Builder / Config Methods | 构建器/配置方法 ====================

    /**
     * Creates a new CSV document builder
     * 创建新的CSV文档构建器
     *
     * @return a new document builder | 新的文档构建器
     */
    public static CsvDocument.Builder builder() {
        return CsvDocument.builder();
    }

    /**
     * Creates a new CSV configuration builder
     * 创建新的CSV配置构建器
     *
     * @return a new config builder | 新的配置构建器
     */
    public static CsvConfig.Builder config() {
        return CsvConfig.builder();
    }
}
