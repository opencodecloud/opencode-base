package cloud.opencode.base.feature.audit;

import cloud.opencode.base.feature.exception.FeatureException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * FileAuditLogger 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
@DisplayName("FileAuditLogger 测试")
class FileAuditLoggerTest {

    @TempDir
    Path tempDir;

    private Path logFile;
    private FileAuditLogger logger;

    @BeforeEach
    void setUp() {
        logFile = tempDir.resolve("audit.log");
        logger = new FileAuditLogger(logFile);
    }

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("创建FileAuditLogger")
        void testConstructor() {
            FileAuditLogger l = new FileAuditLogger(logFile);

            assertThat(l.getLogFile()).isEqualTo(logFile);
        }
    }

    @Nested
    @DisplayName("log() 测试")
    class LogTests {

        @Test
        @DisplayName("写入审计事件")
        void testLogEvent() throws IOException {
            FeatureAuditEvent event = new FeatureAuditEvent(
                    "feature-key", "admin", "ENABLE", false, true, Instant.now()
            );

            logger.log(event);

            assertThat(Files.exists(logFile)).isTrue();
            List<String> lines = Files.readAllLines(logFile);
            assertThat(lines).hasSize(1);
            assertThat(lines.get(0)).contains("feature-key");
            assertThat(lines.get(0)).contains("admin");
            assertThat(lines.get(0)).contains("ENABLE");
        }

        @Test
        @DisplayName("追加多个事件")
        void testAppendEvents() throws IOException {
            FeatureAuditEvent event1 = new FeatureAuditEvent(
                    "f1", "op", "ENABLE", false, true, Instant.now()
            );
            FeatureAuditEvent event2 = new FeatureAuditEvent(
                    "f2", "op", "DISABLE", true, false, Instant.now()
            );

            logger.log(event1);
            logger.log(event2);

            List<String> lines = Files.readAllLines(logFile);
            assertThat(lines).hasSize(2);
        }

        @Test
        @DisplayName("创建父目录")
        void testCreatesParentDirectories() throws IOException {
            Path nestedPath = tempDir.resolve("subdir/nested/audit.log");
            FileAuditLogger nestedLogger = new FileAuditLogger(nestedPath);
            FeatureAuditEvent event = new FeatureAuditEvent(
                    "key", "op", "ENABLE", false, true, Instant.now()
            );

            nestedLogger.log(event);

            assertThat(Files.exists(nestedPath)).isTrue();
        }
    }

    @Nested
    @DisplayName("getLogFile() 测试")
    class GetLogFileTests {

        @Test
        @DisplayName("返回日志文件路径")
        void testGetLogFile() {
            assertThat(logger.getLogFile()).isEqualTo(logFile);
        }
    }
}
