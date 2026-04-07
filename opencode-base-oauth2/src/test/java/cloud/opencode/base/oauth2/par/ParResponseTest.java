package cloud.opencode.base.oauth2.par;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * ParResponse Tests
 * ParResponse 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.3
 */
@DisplayName("ParResponse 测试")
class ParResponseTest {

    @Nested
    @DisplayName("构造器测试")
    class ConstructorTests {

        @Test
        @DisplayName("正常构造")
        void testValidConstruction() {
            Instant now = Instant.now();
            ParResponse response = new ParResponse("urn:ietf:params:oauth:request_uri:abc123", 60, now);

            assertThat(response.requestUri()).isEqualTo("urn:ietf:params:oauth:request_uri:abc123");
            assertThat(response.expiresIn()).isEqualTo(60);
            assertThat(response.createdAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("null requestUri抛出异常")
        void testNullRequestUri() {
            assertThatThrownBy(() -> new ParResponse(null, 60, Instant.now()))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("requestUri");
        }

        @Test
        @DisplayName("null createdAt默认为当前时间")
        void testNullCreatedAtDefaultsToNow() {
            ParResponse response = new ParResponse("urn:test", 60, null);

            assertThat(response.createdAt()).isNotNull();
            assertThat(response.createdAt()).isBeforeOrEqualTo(Instant.now());
        }
    }

    @Nested
    @DisplayName("isExpired方法测试")
    class IsExpiredTests {

        @Test
        @DisplayName("未过期")
        void testNotExpired() {
            ParResponse response = new ParResponse("urn:test", 600, Instant.now());

            assertThat(response.isExpired()).isFalse();
        }

        @Test
        @DisplayName("已过期")
        void testExpired() {
            ParResponse response = new ParResponse("urn:test", 60, Instant.now().minusSeconds(120));

            assertThat(response.isExpired()).isTrue();
        }

        @Test
        @DisplayName("刚好过期边界")
        void testJustExpired() {
            ParResponse response = new ParResponse("urn:test", 1, Instant.now().minusSeconds(5));

            assertThat(response.isExpired()).isTrue();
        }
    }

    @Nested
    @DisplayName("expiresAt方法测试")
    class ExpiresAtTests {

        @Test
        @DisplayName("计算正确的过期时间")
        void testExpiresAt() {
            Instant now = Instant.now();
            ParResponse response = new ParResponse("urn:test", 300, now);

            assertThat(response.expiresAt()).isEqualTo(now.plusSeconds(300));
        }
    }

    @Nested
    @DisplayName("remainingSeconds方法测试")
    class RemainingSecondsTests {

        @Test
        @DisplayName("剩余时间正确")
        void testRemainingSeconds() {
            ParResponse response = new ParResponse("urn:test", 600, Instant.now());

            long remaining = response.remainingSeconds();
            assertThat(remaining).isGreaterThan(500);
            assertThat(remaining).isLessThanOrEqualTo(600);
        }

        @Test
        @DisplayName("已过期返回0")
        void testRemainingSecondsExpired() {
            ParResponse response = new ParResponse("urn:test", 60, Instant.now().minusSeconds(120));

            assertThat(response.remainingSeconds()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Record方法测试")
    class RecordMethodsTests {

        @Test
        @DisplayName("equals和hashCode")
        void testEqualsAndHashCode() {
            Instant now = Instant.now();
            ParResponse r1 = new ParResponse("urn:test:123", 60, now);
            ParResponse r2 = new ParResponse("urn:test:123", 60, now);

            assertThat(r1).isEqualTo(r2);
            assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
        }

        @Test
        @DisplayName("不同对象不相等")
        void testNotEqual() {
            Instant now = Instant.now();
            ParResponse r1 = new ParResponse("urn:test:123", 60, now);
            ParResponse r2 = new ParResponse("urn:test:456", 60, now);

            assertThat(r1).isNotEqualTo(r2);
        }

        @Test
        @DisplayName("toString包含关键信息")
        void testToString() {
            ParResponse response = new ParResponse("urn:test:abc", 120, Instant.now());

            assertThat(response.toString()).contains("urn:test:abc");
            assertThat(response.toString()).contains("120");
        }
    }
}
