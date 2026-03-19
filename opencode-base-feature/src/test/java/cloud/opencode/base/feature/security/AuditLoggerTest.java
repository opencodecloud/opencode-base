package cloud.opencode.base.feature.security;

import cloud.opencode.base.feature.audit.FeatureAuditEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.*;

/**
 * AuditLogger 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
@DisplayName("AuditLogger 测试")
class AuditLoggerTest {

    @Nested
    @DisplayName("接口测试")
    class InterfaceTests {

        @Test
        @DisplayName("可创建实现")
        void testCreateImplementation() {
            AtomicBoolean logged = new AtomicBoolean(false);

            AuditLogger logger = event -> logged.set(true);

            assertThat(logger).isNotNull();
        }

        @Test
        @DisplayName("log方法接收事件")
        void testLogReceivesEvent() {
            AtomicBoolean logged = new AtomicBoolean(false);
            AuditLogger logger = event -> {
                assertThat(event.featureKey()).isEqualTo("test-key");
                logged.set(true);
            };

            FeatureAuditEvent event = new FeatureAuditEvent(
                    "test-key", "op", "ENABLE", false, true, Instant.now()
            );
            logger.log(event);

            assertThat(logged.get()).isTrue();
        }
    }

    @Nested
    @DisplayName("Lambda实现测试")
    class LambdaTests {

        @Test
        @DisplayName("可用作Lambda")
        void testAsLambda() {
            AuditLogger logger = event -> System.out.println(event.toLogString());

            assertThat(logger).isNotNull();
        }
    }
}
