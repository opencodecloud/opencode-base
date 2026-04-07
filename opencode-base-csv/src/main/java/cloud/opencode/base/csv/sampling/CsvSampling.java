package cloud.opencode.base.csv.sampling;

import cloud.opencode.base.csv.CsvDocument;
import cloud.opencode.base.csv.CsvRow;
import cloud.opencode.base.csv.exception.OpenCsvException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * CSV Sampling - Sampling utilities for CSV documents
 * CSV采样 - CSV文档的采样工具
 *
 * <p>Provides static methods for random sampling, systematic sampling,
 * and stratified sampling from a {@link CsvDocument}. All methods preserve
 * the original document's headers.</p>
 * <p>提供从 {@link CsvDocument} 进行随机采样、系统采样和分层采样的静态方法。
 * 所有方法保留原始文档的标题。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Random sampling (Fisher-Yates, with optional seed) - 随机采样</li>
 *   <li>Systematic sampling (every Nth row) - 系统采样（每N行）</li>
 *   <li>Stratified sampling (proportional by group) - 分层采样（按组比例）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CsvDocument sample = CsvSampling.random(doc, 10);
 * CsvDocument sample = CsvSampling.random(doc, 10, 42L);
 * CsvDocument sample = CsvSampling.systematic(doc, 5);
 * CsvDocument sample = CsvSampling.stratified(doc, "category", 20);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具）</li>
 *   <li>Null-safe: Validates all inputs - 空值安全: 验证所有输入</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-csv V1.0.3
 */
public final class CsvSampling {

    private CsvSampling() {
        // utility class
    }

    // ==================== Random Sampling | 随机采样 ====================

    /**
     * Randomly samples rows without replacement
     * 无放回随机采样行
     *
     * <p>Uses Fisher-Yates shuffle on row indices.
     * If sampleSize >= rowCount, the entire document is returned.</p>
     * <p>对行索引使用Fisher-Yates洗牌。
     * 如果sampleSize >= rowCount，返回整个文档。</p>
     *
     * @param doc        the source document | 源文档
     * @param sampleSize the number of rows to sample | 要采样的行数
     * @return a new document containing the sampled rows | 包含采样行的新文档
     * @throws NullPointerException if doc is null | 如果doc为null
     * @throws OpenCsvException     if sampleSize is not positive | 如果sampleSize不为正数
     */
    public static CsvDocument random(CsvDocument doc, int sampleSize) {
        Objects.requireNonNull(doc, "doc must not be null");
        validateSampleSize(sampleSize);

        if (doc.isEmpty()) {
            return buildDoc(doc.headers(), List.of());
        }
        if (sampleSize >= doc.rowCount()) {
            return doc;
        }

        return randomSample(doc, sampleSize, new Random());
    }

    /**
     * Randomly samples rows without replacement using a seed for reproducibility
     * 使用种子无放回随机采样行以实现可重现性
     *
     * @param doc        the source document | 源文档
     * @param sampleSize the number of rows to sample | 要采样的行数
     * @param seed       the random seed | 随机种子
     * @return a new document containing the sampled rows | 包含采样行的新文档
     * @throws NullPointerException if doc is null | 如果doc为null
     * @throws OpenCsvException     if sampleSize is not positive | 如果sampleSize不为正数
     */
    public static CsvDocument random(CsvDocument doc, int sampleSize, long seed) {
        Objects.requireNonNull(doc, "doc must not be null");
        validateSampleSize(sampleSize);

        if (doc.isEmpty()) {
            return buildDoc(doc.headers(), List.of());
        }
        if (sampleSize >= doc.rowCount()) {
            return doc;
        }

        return randomSample(doc, sampleSize, new Random(seed));
    }

    // ==================== Systematic Sampling | 系统采样 ====================

    /**
     * Performs systematic sampling, selecting every Nth row starting from a random offset
     * 执行系统采样，从随机偏移开始每隔N行选取一行
     *
     * @param doc      the source document | 源文档
     * @param interval the sampling interval (every Nth row) | 采样间隔（每N行）
     * @return a new document containing the sampled rows | 包含采样行的新文档
     * @throws NullPointerException if doc is null | 如果doc为null
     * @throws OpenCsvException     if interval is not positive | 如果interval不为正数
     */
    public static CsvDocument systematic(CsvDocument doc, int interval) {
        Objects.requireNonNull(doc, "doc must not be null");
        validateInterval(interval);

        if (doc.isEmpty()) {
            return buildDoc(doc.headers(), List.of());
        }

        return systematicSample(doc, interval, 0);
    }

    /**
     * Performs systematic sampling with a specified start offset
     * 使用指定起始偏移执行系统采样
     *
     * @param doc         the source document | 源文档
     * @param interval    the sampling interval | 采样间隔
     * @param startOffset the 0-based starting row offset | 0起始的起始行偏移
     * @return a new document containing the sampled rows | 包含采样行的新文档
     * @throws NullPointerException if doc is null | 如果doc为null
     * @throws OpenCsvException     if interval is not positive or startOffset is invalid | 参数无效时
     */
    public static CsvDocument systematic(CsvDocument doc, int interval, int startOffset) {
        Objects.requireNonNull(doc, "doc must not be null");
        validateInterval(interval);
        if (startOffset < 0 || startOffset >= interval) {
            throw new OpenCsvException(
                    "startOffset must be >= 0 and < interval (" + interval + "), but was: " + startOffset);
        }

        if (doc.isEmpty()) {
            return buildDoc(doc.headers(), List.of());
        }

        return systematicSample(doc, interval, startOffset);
    }

    // ==================== Stratified Sampling | 分层采样 ====================

    /**
     * Performs stratified sampling, sampling proportionally from each group defined by a column
     * 执行分层采样，按列定义的每个组按比例采样
     *
     * <p>Groups rows by the specified column value, then samples proportionally
     * from each group. Each group gets at least 1 row if possible.
     * The total may differ slightly from sampleSize due to rounding.</p>
     * <p>按指定列值对行进行分组，然后从每个组按比例采样。
     * 如果可能，每个组至少获得1行。由于四舍五入，总数可能与sampleSize略有不同。</p>
     *
     * @param doc        the source document | 源文档
     * @param column     the column to group by | 用于分组的列
     * @param sampleSize the target total sample size | 目标总采样大小
     * @return a new document containing the sampled rows | 包含采样行的新文档
     * @throws NullPointerException if doc or column is null | 如果doc或column为null
     * @throws OpenCsvException     if sampleSize is not positive or column is not found | 参数无效时
     */
    public static CsvDocument stratified(CsvDocument doc, String column, int sampleSize) {
        Objects.requireNonNull(doc, "doc must not be null");
        Objects.requireNonNull(column, "column must not be null");
        validateSampleSize(sampleSize);

        if (doc.isEmpty()) {
            return buildDoc(doc.headers(), List.of());
        }

        return stratifiedSample(doc, column, sampleSize, new Random());
    }

    /**
     * Performs stratified sampling with a seed for reproducibility
     * 使用种子执行分层采样以实现可重现性
     *
     * @param doc        the source document | 源文档
     * @param column     the column to group by | 用于分组的列
     * @param sampleSize the target total sample size | 目标总采样大小
     * @param seed       the random seed | 随机种子
     * @return a new document containing the sampled rows | 包含采样行的新文档
     * @throws NullPointerException if doc or column is null | 如果doc或column为null
     * @throws OpenCsvException     if sampleSize is not positive or column is not found | 参数无效时
     */
    public static CsvDocument stratified(CsvDocument doc, String column, int sampleSize, long seed) {
        Objects.requireNonNull(doc, "doc must not be null");
        Objects.requireNonNull(column, "column must not be null");
        validateSampleSize(sampleSize);

        if (doc.isEmpty()) {
            return buildDoc(doc.headers(), List.of());
        }

        return stratifiedSample(doc, column, sampleSize, new Random(seed));
    }

    // ==================== Internal Helpers | 内部辅助方法 ====================

    private static void validateSampleSize(int sampleSize) {
        if (sampleSize <= 0) {
            throw new OpenCsvException("sampleSize must be positive, but was: " + sampleSize);
        }
    }

    private static void validateInterval(int interval) {
        if (interval <= 0) {
            throw new OpenCsvException("interval must be positive, but was: " + interval);
        }
    }

    private static CsvDocument randomSample(CsvDocument doc, int sampleSize, Random rng) {
        List<CsvRow> rows = doc.rows();
        int n = rows.size();

        // Fisher-Yates partial shuffle: shuffle first sampleSize elements
        int[] indices = new int[n];
        for (int i = 0; i < n; i++) {
            indices[i] = i;
        }
        for (int i = 0; i < sampleSize; i++) {
            int j = i + rng.nextInt(n - i);
            int tmp = indices[i];
            indices[i] = indices[j];
            indices[j] = tmp;
        }

        List<CsvRow> sampled = new ArrayList<>(sampleSize);
        for (int i = 0; i < sampleSize; i++) {
            sampled.add(rows.get(indices[i]));
        }
        return buildDoc(doc.headers(), sampled);
    }

    private static CsvDocument systematicSample(CsvDocument doc, int interval, int startOffset) {
        List<CsvRow> rows = doc.rows();
        List<CsvRow> sampled = new ArrayList<>();
        for (int i = startOffset; i < rows.size(); i += interval) {
            sampled.add(rows.get(i));
        }
        return buildDoc(doc.headers(), sampled);
    }

    private static CsvDocument stratifiedSample(CsvDocument doc, String column, int sampleSize, Random rng) {
        List<String> headers = doc.headers();
        int colIndex = headers.indexOf(column);
        if (colIndex < 0) {
            throw new OpenCsvException("Column not found: " + column);
        }

        // Group rows by column value, preserving first-seen order
        LinkedHashMap<String, List<CsvRow>> groups = new LinkedHashMap<>();
        for (CsvRow row : doc.rows()) {
            String key = colIndex < row.size() ? row.get(colIndex) : "";
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
        }

        int totalRows = doc.rowCount();
        int effectiveSampleSize = Math.min(sampleSize, totalRows);
        List<CsvRow> sampled = new ArrayList<>();

        for (Map.Entry<String, List<CsvRow>> entry : groups.entrySet()) {
            List<CsvRow> groupRows = entry.getValue();
            // Proportional allocation, at least 1 per group if possible
            int groupSample = (int) Math.round((double) groupRows.size() / totalRows * effectiveSampleSize);
            groupSample = Math.max(groupSample, Math.min(1, groupRows.size()));
            groupSample = Math.min(groupSample, groupRows.size());

            // Fisher-Yates partial shuffle on group
            int[] indices = new int[groupRows.size()];
            for (int i = 0; i < indices.length; i++) {
                indices[i] = i;
            }
            for (int i = 0; i < groupSample; i++) {
                int j = i + rng.nextInt(indices.length - i);
                int tmp = indices[i];
                indices[i] = indices[j];
                indices[j] = tmp;
            }
            for (int i = 0; i < groupSample; i++) {
                sampled.add(groupRows.get(indices[i]));
            }
        }

        return buildDoc(headers, sampled);
    }

    private static CsvDocument buildDoc(List<String> headers, List<CsvRow> rows) {
        CsvDocument.Builder builder = CsvDocument.builder().header(headers);
        for (CsvRow row : rows) {
            builder.addRow(row);
        }
        return builder.build();
    }
}
