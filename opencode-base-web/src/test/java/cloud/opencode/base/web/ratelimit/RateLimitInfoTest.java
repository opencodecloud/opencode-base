package cloud.opencode.base.web.ratelimit;

import cloud.opencode.base.web.http.HttpHeaders;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * Tests for {@link RateLimitInfo}.
 */
@DisplayName("RateLimitInfo")
class RateLimitInfoTest {

    // ==================== Factory Method Tests ====================

    @Nested
    @DisplayName("of")
    class OfTests {

        @Test
        @DisplayName("should create with valid parameters")
        void shouldCreateWithValidParameters() {
            RateLimitInfo info = RateLimitInfo.of(100, 42, 1700000000L);

            assertThat(info.limit()).isEqualTo(100);
            assertThat(info.remaining()).isEqualTo(42);
            assertThat(info.resetEpochSecond()).isEqualTo(1700000000L);
        }

        @Test
        @DisplayName("should create with zero remaining")
        void shouldCreateWithZeroRemaining() {
            RateLimitInfo info = RateLimitInfo.of(100, 0, 1700000000L);

            assertThat(info.remaining()).isZero();
        }

        @Test
        @DisplayName("should reject negative limit")
        void shouldRejectNegativeLimit() {
            assertThatThrownBy(() -> RateLimitInfo.of(-1, 0, 1700000000L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("limit");
        }

        @Test
        @DisplayName("should reject negative remaining")
        void shouldRejectNegativeRemaining() {
            assertThatThrownBy(() -> RateLimitInfo.of(100, -1, 1700000000L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("remaining");
        }

        @Test
        @DisplayName("should accept zero limit")
        void shouldAcceptZeroLimit() {
            RateLimitInfo info = RateLimitInfo.of(0, 0, 1700000000L);

            assertThat(info.limit()).isZero();
        }
    }

    // ==================== isExhausted Tests ====================

    @Nested
    @DisplayName("isExhausted")
    class IsExhaustedTests {

        @Test
        @DisplayName("should return true when remaining is zero")
        void shouldReturnTrueWhenZero() {
            RateLimitInfo info = RateLimitInfo.of(100, 0, 1700000000L);

            assertThat(info.isExhausted()).isTrue();
        }

        @Test
        @DisplayName("should return false when remaining is positive")
        void shouldReturnFalseWhenPositive() {
            RateLimitInfo info = RateLimitInfo.of(100, 50, 1700000000L);

            assertThat(info.isExhausted()).isFalse();
        }
    }

    // ==================== retryAfterSeconds Tests ====================

    @Nested
    @DisplayName("retryAfterSeconds")
    class RetryAfterSecondsTests {

        @Test
        @DisplayName("should return positive seconds for future reset")
        void shouldReturnPositiveForFuture() {
            long futureEpoch = Instant.now().getEpochSecond() + 3600;
            RateLimitInfo info = RateLimitInfo.of(100, 0, futureEpoch);

            long retryAfter = info.retryAfterSeconds();

            assertThat(retryAfter).isGreaterThan(0);
            assertThat(retryAfter).isLessThanOrEqualTo(3600);
        }

        @Test
        @DisplayName("should return zero for past reset")
        void shouldReturnZeroForPast() {
            long pastEpoch = Instant.now().getEpochSecond() - 3600;
            RateLimitInfo info = RateLimitInfo.of(100, 0, pastEpoch);

            assertThat(info.retryAfterSeconds()).isZero();
        }

        @Test
        @DisplayName("should return zero for current time reset")
        void shouldReturnZeroForCurrentTime() {
            long nowEpoch = Instant.now().getEpochSecond();
            RateLimitInfo info = RateLimitInfo.of(100, 0, nowEpoch);

            // May be 0 or 1 depending on timing
            assertThat(info.retryAfterSeconds()).isBetween(0L, 1L);
        }
    }

    // ==================== resetInstant Tests ====================

    @Nested
    @DisplayName("resetInstant")
    class ResetInstantTests {

        @Test
        @DisplayName("should return correct Instant")
        void shouldReturnCorrectInstant() {
            RateLimitInfo info = RateLimitInfo.of(100, 0, 1700000000L);

            Instant instant = info.resetInstant();

            assertThat(instant).isEqualTo(Instant.ofEpochSecond(1700000000L));
        }
    }

    // ==================== applyTo Tests ====================

    @Nested
    @DisplayName("applyTo")
    class ApplyToTests {

        @Test
        @DisplayName("should set rate limit headers")
        void shouldSetRateLimitHeaders() {
            RateLimitInfo info = RateLimitInfo.of(100, 42, 1700000000L);
            HttpHeaders headers = HttpHeaders.of();

            info.applyTo(headers);

            assertThat(headers.get("X-RateLimit-Limit")).isEqualTo("100");
            assertThat(headers.get("X-RateLimit-Remaining")).isEqualTo("42");
            assertThat(headers.get("X-RateLimit-Reset")).isEqualTo("1700000000");
        }

        @Test
        @DisplayName("should set Retry-After when exhausted and reset in future")
        void shouldSetRetryAfterWhenExhausted() {
            long futureEpoch = Instant.now().getEpochSecond() + 3600;
            RateLimitInfo info = RateLimitInfo.of(100, 0, futureEpoch);
            HttpHeaders headers = HttpHeaders.of();

            info.applyTo(headers);

            assertThat(headers.get("Retry-After")).isNotNull();
            long retryAfter = Long.parseLong(headers.get("Retry-After"));
            assertThat(retryAfter).isGreaterThan(0);
        }

        @Test
        @DisplayName("should not set Retry-After when not exhausted")
        void shouldNotSetRetryAfterWhenNotExhausted() {
            long futureEpoch = Instant.now().getEpochSecond() + 3600;
            RateLimitInfo info = RateLimitInfo.of(100, 50, futureEpoch);
            HttpHeaders headers = HttpHeaders.of();

            info.applyTo(headers);

            assertThat(headers.get("Retry-After")).isNull();
        }

        @Test
        @DisplayName("should set Retry-After to 0 when reset is in the past")
        void shouldSetRetryAfterZeroWhenPast() {
            long pastEpoch = Instant.now().getEpochSecond() - 3600;
            RateLimitInfo info = RateLimitInfo.of(100, 0, pastEpoch);
            HttpHeaders headers = HttpHeaders.of();

            info.applyTo(headers);

            assertThat(headers.get("Retry-After")).isEqualTo("0");
        }

        @Test
        @DisplayName("should reject null headers")
        void shouldRejectNullHeaders() {
            RateLimitInfo info = RateLimitInfo.of(100, 42, 1700000000L);

            assertThatThrownBy(() -> info.applyTo(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== fromHeaders Tests ====================

    @Nested
    @DisplayName("fromHeaders")
    class FromHeadersTests {

        @Test
        @DisplayName("should parse valid rate limit headers")
        void shouldParseValidHeaders() {
            HttpHeaders headers = HttpHeaders.of()
                    .set("X-RateLimit-Limit", "100")
                    .set("X-RateLimit-Remaining", "42")
                    .set("X-RateLimit-Reset", "1700000000");

            RateLimitInfo info = RateLimitInfo.fromHeaders(headers);

            assertThat(info).isNotNull();
            assertThat(info.limit()).isEqualTo(100);
            assertThat(info.remaining()).isEqualTo(42);
            assertThat(info.resetEpochSecond()).isEqualTo(1700000000L);
        }

        @Test
        @DisplayName("should return null when limit header is missing")
        void shouldReturnNullWhenLimitMissing() {
            HttpHeaders headers = HttpHeaders.of()
                    .set("X-RateLimit-Remaining", "42")
                    .set("X-RateLimit-Reset", "1700000000");

            assertThat(RateLimitInfo.fromHeaders(headers)).isNull();
        }

        @Test
        @DisplayName("should return null when remaining header is missing")
        void shouldReturnNullWhenRemainingMissing() {
            HttpHeaders headers = HttpHeaders.of()
                    .set("X-RateLimit-Limit", "100")
                    .set("X-RateLimit-Reset", "1700000000");

            assertThat(RateLimitInfo.fromHeaders(headers)).isNull();
        }

        @Test
        @DisplayName("should return null when reset header is missing")
        void shouldReturnNullWhenResetMissing() {
            HttpHeaders headers = HttpHeaders.of()
                    .set("X-RateLimit-Limit", "100")
                    .set("X-RateLimit-Remaining", "42");

            assertThat(RateLimitInfo.fromHeaders(headers)).isNull();
        }

        @Test
        @DisplayName("should return null for non-numeric header values")
        void shouldReturnNullForNonNumericValues() {
            HttpHeaders headers = HttpHeaders.of()
                    .set("X-RateLimit-Limit", "abc")
                    .set("X-RateLimit-Remaining", "42")
                    .set("X-RateLimit-Reset", "1700000000");

            assertThat(RateLimitInfo.fromHeaders(headers)).isNull();
        }

        @Test
        @DisplayName("should return null for negative limit in headers")
        void shouldReturnNullForNegativeLimitInHeaders() {
            HttpHeaders headers = HttpHeaders.of()
                    .set("X-RateLimit-Limit", "-1")
                    .set("X-RateLimit-Remaining", "42")
                    .set("X-RateLimit-Reset", "1700000000");

            assertThat(RateLimitInfo.fromHeaders(headers)).isNull();
        }

        @Test
        @DisplayName("should return null for negative remaining in headers")
        void shouldReturnNullForNegativeRemainingInHeaders() {
            HttpHeaders headers = HttpHeaders.of()
                    .set("X-RateLimit-Limit", "100")
                    .set("X-RateLimit-Remaining", "-5")
                    .set("X-RateLimit-Reset", "1700000000");

            assertThat(RateLimitInfo.fromHeaders(headers)).isNull();
        }

        @Test
        @DisplayName("should handle whitespace in header values")
        void shouldHandleWhitespace() {
            HttpHeaders headers = HttpHeaders.of()
                    .set("X-RateLimit-Limit", " 100 ")
                    .set("X-RateLimit-Remaining", " 42 ")
                    .set("X-RateLimit-Reset", " 1700000000 ");

            RateLimitInfo info = RateLimitInfo.fromHeaders(headers);

            assertThat(info).isNotNull();
            assertThat(info.limit()).isEqualTo(100);
        }

        @Test
        @DisplayName("should reject null headers argument")
        void shouldRejectNullHeaders() {
            assertThatThrownBy(() -> RateLimitInfo.fromHeaders(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should return null for empty headers")
        void shouldReturnNullForEmptyHeaders() {
            assertThat(RateLimitInfo.fromHeaders(HttpHeaders.of())).isNull();
        }
    }

    // ==================== Round-trip Tests ====================

    @Nested
    @DisplayName("Round-trip")
    class RoundTripTests {

        @Test
        @DisplayName("applyTo then fromHeaders should produce equivalent info")
        void shouldRoundTrip() {
            RateLimitInfo original = RateLimitInfo.of(1000, 500, 1700000000L);
            HttpHeaders headers = HttpHeaders.of();

            original.applyTo(headers);
            RateLimitInfo parsed = RateLimitInfo.fromHeaders(headers);

            assertThat(parsed).isNotNull();
            assertThat(parsed.limit()).isEqualTo(original.limit());
            assertThat(parsed.remaining()).isEqualTo(original.remaining());
            assertThat(parsed.resetEpochSecond()).isEqualTo(original.resetEpochSecond());
        }

        @Test
        @DisplayName("round-trip should preserve equality")
        void shouldPreserveEquality() {
            RateLimitInfo original = RateLimitInfo.of(100, 0, 1700000000L);
            HttpHeaders headers = HttpHeaders.of();

            original.applyTo(headers);
            RateLimitInfo parsed = RateLimitInfo.fromHeaders(headers);

            assertThat(parsed).isEqualTo(original);
        }
    }

    // ==================== Record Equality Tests ====================

    @Nested
    @DisplayName("Record equality")
    class RecordEqualityTests {

        @Test
        @DisplayName("equal info should be equal")
        void equalInfoShouldBeEqual() {
            RateLimitInfo a = RateLimitInfo.of(100, 42, 1700000000L);
            RateLimitInfo b = RateLimitInfo.of(100, 42, 1700000000L);

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("different info should not be equal")
        void differentInfoShouldNotBeEqual() {
            RateLimitInfo a = RateLimitInfo.of(100, 42, 1700000000L);
            RateLimitInfo b = RateLimitInfo.of(200, 42, 1700000000L);

            assertThat(a).isNotEqualTo(b);
        }
    }

    // ==================== Header Constants Tests ====================

    @Nested
    @DisplayName("Header constants")
    class HeaderConstantsTests {

        @Test
        @DisplayName("should have correct header names")
        void shouldHaveCorrectHeaderNames() {
            assertThat(RateLimitInfo.HEADER_LIMIT).isEqualTo("X-RateLimit-Limit");
            assertThat(RateLimitInfo.HEADER_REMAINING).isEqualTo("X-RateLimit-Remaining");
            assertThat(RateLimitInfo.HEADER_RESET).isEqualTo("X-RateLimit-Reset");
            assertThat(RateLimitInfo.HEADER_RETRY_AFTER).isEqualTo("Retry-After");
        }
    }
}
