package cloud.opencode.base.feature.audit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * FeatureAuditEvent 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
@DisplayName("FeatureAuditEvent 测试")
class FeatureAuditEventTest {

    @Nested
    @DisplayName("Record组件测试")
    class RecordComponentTests {

        @Test
        @DisplayName("所有record组件可访问")
        void testRecordComponents() {
            Instant now = Instant.now();
            FeatureAuditEvent event = new FeatureAuditEvent(
                    "feature-key", "admin", "ENABLE", false, true, now
            );

            assertThat(event.featureKey()).isEqualTo("feature-key");
            assertThat(event.operatorId()).isEqualTo("admin");
            assertThat(event.action()).isEqualTo("ENABLE");
            assertThat(event.oldValue()).isFalse();
            assertThat(event.newValue()).isTrue();
            assertThat(event.timestamp()).isEqualTo(now);
        }
    }

    @Nested
    @DisplayName("isStateChanged() 测试")
    class IsStateChangedTests {

        @Test
        @DisplayName("状态变化返回true")
        void testStateChanged() {
            FeatureAuditEvent event = new FeatureAuditEvent(
                    "key", "op", "ENABLE", false, true, Instant.now()
            );

            assertThat(event.isStateChanged()).isTrue();
        }

        @Test
        @DisplayName("状态未变返回false")
        void testStateNotChanged() {
            FeatureAuditEvent event = new FeatureAuditEvent(
                    "key", "op", "UPDATE", true, true, Instant.now()
            );

            assertThat(event.isStateChanged()).isFalse();
        }
    }

    @Nested
    @DisplayName("toLogString() 测试")
    class ToLogStringTests {

        @Test
        @DisplayName("返回格式化日志字符串")
        void testToLogString() {
            Instant now = Instant.parse("2024-01-15T10:30:00Z");
            FeatureAuditEvent event = new FeatureAuditEvent(
                    "dark-mode", "admin", "ENABLE", false, true, now
            );

            String log = event.toLogString();

            assertThat(log).contains("2024-01-15T10:30:00Z");
            assertThat(log).contains("ENABLE");
            assertThat(log).contains("dark-mode");
            assertThat(log).contains("admin");
            assertThat(log).contains("false");
            assertThat(log).contains("true");
        }
    }
}
