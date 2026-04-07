package cloud.opencode.base.test.assertion;

import cloud.opencode.base.test.exception.AssertionException;
import cloud.opencode.base.test.exception.TestErrorCode;
import cloud.opencode.base.test.exception.TestException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Snapshot Assert - JSON snapshot assertion utility
 * 快照断言 - JSON快照断言工具
 *
 * <p>Compares actual JSON strings against stored snapshot files. On first run
 * (when the snapshot file does not exist), the snapshot is created automatically.
 * On subsequent runs, the actual value is compared against the stored snapshot.</p>
 * <p>将实际JSON字符串与存储的快照文件进行比较。首次运行（快照文件不存在时），
 * 会自动创建快照。后续运行时，将实际值与存储的快照进行比较。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Automatic snapshot creation on first run - 首次运行自动创建快照</li>
 *   <li>Whitespace-normalized comparison - 空白规范化比较</li>
 *   <li>Custom snapshot directory support - 自定义快照目录支持</li>
 *   <li>Snapshot update via system property - 通过系统属性更新快照</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Compare against snapshot in default directory (src/test/resources/snapshots)
 * SnapshotAssert.assertMatchesSnapshot("user-response", actualJson);
 *
 * // Compare against snapshot in custom directory
 * SnapshotAssert.assertMatchesSnapshot(
 *     Path.of("src/test/resources/my-snapshots"),
 *     "user-response",
 *     actualJson
 * );
 *
 * // Force update snapshots by setting system property:
 * // -Dopencode.test.update-snapshots=true
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>File I/O: Uses UTF-8 encoding - 文件I/O: 使用UTF-8编码</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.3
 */
public final class SnapshotAssert {

    private static final String DEFAULT_SNAPSHOT_DIR = "src/test/resources/snapshots";
    private static final String SNAPSHOT_EXTENSION = ".json";
    private static final String UPDATE_PROPERTY = "opencode.test.update-snapshots";

    private SnapshotAssert() {
        // utility class
    }

    /**
     * Asserts that the actual JSON matches the stored snapshot.
     * Uses the default snapshot directory (src/test/resources/snapshots).
     * 断言实际JSON匹配存储的快照。使用默认快照目录。
     *
     * @param snapshotName the snapshot name (without extension) | 快照名称（不含扩展名）
     * @param actualJson   the actual JSON string | 实际JSON字符串
     * @throws AssertionException if the actual JSON does not match the snapshot | 如果实际JSON不匹配快照
     * @throws TestException      if an I/O error occurs | 如果发生I/O错误
     */
    public static void assertMatchesSnapshot(String snapshotName, String actualJson) {
        assertMatchesSnapshot(Path.of(DEFAULT_SNAPSHOT_DIR), snapshotName, actualJson);
    }

    /**
     * Asserts that the actual JSON matches the stored snapshot in the specified directory.
     * 断言实际JSON匹配指定目录中存储的快照。
     *
     * @param snapshotDir  the snapshot directory | 快照目录
     * @param snapshotName the snapshot name (without extension) | 快照名称（不含扩展名）
     * @param actualJson   the actual JSON string | 实际JSON字符串
     * @throws AssertionException if the actual JSON does not match the snapshot | 如果实际JSON不匹配快照
     * @throws TestException      if an I/O error occurs | 如果发生I/O错误
     */
    public static void assertMatchesSnapshot(Path snapshotDir, String snapshotName, String actualJson) {
        Objects.requireNonNull(snapshotDir, "snapshotDir must not be null");
        if (snapshotName == null || snapshotName.isBlank()) {
            throw new AssertionException("snapshotName must not be null or blank");
        }
        if (actualJson == null) {
            throw new AssertionException("actualJson must not be null");
        }

        Path snapshotFile = validateSnapshotPath(snapshotDir, snapshotName);
        boolean shouldUpdate = Boolean.getBoolean(UPDATE_PROPERTY);

        if (shouldUpdate || !Files.exists(snapshotFile)) {
            writeSnapshot(snapshotFile, actualJson);
            return;
        }

        String expectedJson = readSnapshot(snapshotFile);
        String normalizedActual = normalize(actualJson);
        String normalizedExpected = normalize(expectedJson);

        if (!normalizedActual.equals(normalizedExpected)) {
            throw new AssertionException(TestErrorCode.ASSERTION_EQUALS,
                    "Snapshot mismatch for '" + snapshotName + "'\n"
                            + "Expected (from snapshot):\n" + expectedJson + "\n"
                            + "Actual:\n" + actualJson + "\n"
                            + "To update snapshots, run with -D" + UPDATE_PROPERTY + "=true");
        }
    }

    /**
     * Deletes the snapshot file if it exists.
     * 删除快照文件（如果存在）。
     *
     * @param snapshotName the snapshot name (without extension) | 快照名称（不含扩展名）
     * @return true if the file was deleted, false if it did not exist | 如果文件被删除返回true，如果不存在返回false
     */
    public static boolean deleteSnapshot(String snapshotName) {
        return deleteSnapshot(Path.of(DEFAULT_SNAPSHOT_DIR), snapshotName);
    }

    /**
     * Deletes the snapshot file if it exists.
     * 删除快照文件（如果存在）。
     *
     * @param snapshotDir  the snapshot directory | 快照目录
     * @param snapshotName the snapshot name (without extension) | 快照名称（不含扩展名）
     * @return true if the file was deleted, false if it did not exist | 如果文件被删除返回true，如果不存在返回false
     */
    public static boolean deleteSnapshot(Path snapshotDir, String snapshotName) {
        Path snapshotFile = validateSnapshotPath(snapshotDir, snapshotName);
        try {
            return Files.deleteIfExists(snapshotFile);
        } catch (IOException e) {
            throw new TestException(TestErrorCode.GENERAL_ERROR, e);
        }
    }

    // ==================== Helper Methods | 辅助方法 ====================

    private static void writeSnapshot(Path snapshotFile, String content) {
        try {
            Files.createDirectories(snapshotFile.getParent());
            Files.writeString(snapshotFile, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new TestException(TestErrorCode.GENERAL_ERROR, e);
        }
    }

    private static String readSnapshot(Path snapshotFile) {
        try {
            return Files.readString(snapshotFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new TestException(TestErrorCode.GENERAL_ERROR, e);
        }
    }

    /**
     * Validates that the resolved snapshot path does not escape the snapshot directory.
     * Prevents path traversal attacks via malicious snapshot names.
     * 验证解析后的快照路径不会逃逸出快照目录。防止通过恶意快照名称进行路径遍历攻击。
     *
     * @param snapshotDir  the snapshot directory | 快照目录
     * @param snapshotName the snapshot name | 快照名称
     * @return the validated path | 验证后的路径
     * @throws AssertionException if the path escapes the snapshot directory | 如果路径逃逸出快照目录
     */
    private static Path validateSnapshotPath(Path snapshotDir, String snapshotName) {
        Path resolved = snapshotDir.resolve(snapshotName + SNAPSHOT_EXTENSION).normalize();
        Path normalizedDir = snapshotDir.normalize();
        if (!resolved.startsWith(normalizedDir)) {
            throw new AssertionException("snapshot name must not escape the snapshot directory: " + snapshotName);
        }
        return resolved;
    }

    /**
     * Normalizes JSON by removing structural whitespace (between tokens) while
     * preserving whitespace inside quoted string values.
     * 规范化JSON，移除结构性空白（标记之间的），同时保留带引号字符串值内部的空白。
     *
     * <p>Uses a simple state machine to track whether the current position is
     * inside a quoted string, handling escaped quotes correctly.</p>
     * <p>使用简单状态机跟踪当前位置是否在引号字符串内部，正确处理转义引号。</p>
     *
     * @param json the JSON string to normalize | 要规范化的JSON字符串
     * @return the normalized JSON string | 规范化后的JSON字符串
     */
    private static String normalize(String json) {
        if (json == null) {
            return "";
        }
        String trimmed = json.trim();
        StringBuilder sb = new StringBuilder(trimmed.length());
        boolean inString = false;
        boolean escaped = false;

        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (escaped) {
                sb.append(c);
                escaped = false;
                continue;
            }
            if (c == '\\' && inString) {
                sb.append(c);
                escaped = true;
                continue;
            }
            if (c == '"') {
                inString = !inString;
                sb.append(c);
                continue;
            }
            if (!inString && Character.isWhitespace(c)) {
                continue;
            }
            sb.append(c);
        }
        return sb.toString();
    }
}
