package cloud.opencode.base.feature.audit;

import cloud.opencode.base.feature.exception.FeatureException;
import cloud.opencode.base.feature.exception.FeatureErrorCode;
import cloud.opencode.base.feature.security.AuditLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * File Audit Logger
 * 文件审计日志记录器
 *
 * <p>Audit logger that writes events to a file.</p>
 * <p>将事件写入文件的审计日志记录器。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>File-based logging - 文件日志记录</li>
 *   <li>Append mode - 追加模式</li>
 *   <li>Simple text format - 简单文本格式</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * AuditLogger logger = new FileAuditLogger(Path.of("audit.log"));
 * logger.log(new FeatureAuditEvent(...));
 *
 * // Output:
 * // [2024-01-15T10:30:00Z] ENABLE: feature=dark-mode, operator=admin, false -> true
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Partial (validates inputs) - 空值安全: 部分（验证输入）</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
public class FileAuditLogger implements AuditLogger {

    private final Path logFile;

    /**
     * Create file audit logger
     * 创建文件审计日志记录器
     *
     * @param logFile the log file path | 日志文件路径
     */
    public FileAuditLogger(Path logFile) {
        this.logFile = logFile;
    }

    /**
     * Log audit event to file
     * 将审计事件记录到文件
     *
     * @param event the audit event | 审计事件
     */
    @Override
    public synchronized void log(FeatureAuditEvent event) {
        try {
            if (logFile.getParent() != null) {
                Files.createDirectories(logFile.getParent());
            }

            String logLine = event.toLogString() + System.lineSeparator();
            Files.writeString(logFile, logLine,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new FeatureException("Failed to write audit log: " + e.getMessage(),
                e, event.featureKey(), FeatureErrorCode.AUDIT_FAILED);
        }
    }

    /**
     * Get the log file path
     * 获取日志文件路径
     *
     * @return log file path | 日志文件路径
     */
    public Path getLogFile() {
        return logFile;
    }
}
