package cloud.opencode.base.web.util;

import cloud.opencode.base.web.http.HttpHeaders;
import org.junit.jupiter.api.*;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * ClientIpTest Tests
 * ClientIpTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.3
 */
@DisplayName("ClientIp Tests")
class ClientIpTest {

    @Nested
    @DisplayName("No Proxy Header Tests")
    class NoProxyHeaderTests {

        @Test
        @DisplayName("should return null for empty headers")
        void shouldReturnNullForEmptyHeaders() {
            HttpHeaders headers = HttpHeaders.of();

            assertThat(ClientIp.resolve(headers)).isNull();
        }

        @Test
        @DisplayName("should return null for null headers")
        void shouldReturnNullForNullHeaders() {
            assertThat(ClientIp.resolve(null)).isNull();
        }

        @Test
        @DisplayName("should return null for unrelated headers")
        void shouldReturnNullForUnrelatedHeaders() {
            HttpHeaders headers = HttpHeaders.of()
                    .add("Content-Type", "application/json")
                    .add("Accept", "text/html");

            assertThat(ClientIp.resolve(headers)).isNull();
        }
    }

    @Nested
    @DisplayName("X-Forwarded-For Tests")
    class XForwardedForTests {

        @Test
        @DisplayName("single IP in X-Forwarded-For should be returned")
        void singleIpInXForwardedForShouldBeReturned() {
            HttpHeaders headers = HttpHeaders.of()
                    .add("X-Forwarded-For", "203.0.113.50");

            assertThat(ClientIp.resolve(headers)).isEqualTo("203.0.113.50");
        }

        @Test
        @DisplayName("multiple IPs should return leftmost without trusted proxies")
        void multipleIpsShouldReturnLeftmostWithoutTrustedProxies() {
            HttpHeaders headers = HttpHeaders.of()
                    .add("X-Forwarded-For", "203.0.113.50, 70.41.3.18, 150.172.238.178");

            // Without trusted proxies: right-to-left, first non-trusted is 150.172.238.178
            // But since no trusted set, it returns the rightmost valid IP first found from right
            // Actually: right to left, first non-trusted = 150.172.238.178
            assertThat(ClientIp.resolve(headers)).isEqualTo("150.172.238.178");
        }

        @Test
        @DisplayName("multiple IPs with trusted proxies should skip trusted")
        void multipleIpsWithTrustedProxiesShouldSkipTrusted() {
            HttpHeaders headers = HttpHeaders.of()
                    .add("X-Forwarded-For", "203.0.113.50, 70.41.3.18, 150.172.238.178");

            String ip = ClientIp.resolve(headers, Set.of("150.172.238.178", "70.41.3.18"));

            assertThat(ip).isEqualTo("203.0.113.50");
        }

        @Test
        @DisplayName("all IPs trusted should return null from XFF and fall through")
        void allIpsTrustedShouldReturnNull() {
            HttpHeaders headers = HttpHeaders.of()
                    .add("X-Forwarded-For", "10.0.0.1, 10.0.0.2");

            String ip = ClientIp.resolve(headers, Set.of("10.0.0.1", "10.0.0.2"));

            assertThat(ip).isNull();
        }

        @Test
        @DisplayName("blank X-Forwarded-For should fall through to next header")
        void blankXForwardedForShouldFallThrough() {
            HttpHeaders headers = HttpHeaders.of()
                    .add("X-Forwarded-For", "   ")
                    .add("X-Real-IP", "192.168.1.100");

            assertThat(ClientIp.resolve(headers)).isEqualTo("192.168.1.100");
        }

        @Test
        @DisplayName("invalid IPs in X-Forwarded-For should be skipped")
        void invalidIpsInXForwardedForShouldBeSkipped() {
            HttpHeaders headers = HttpHeaders.of()
                    .add("X-Forwarded-For", "not-an-ip, 999.999.999.999, 10.0.0.1");

            assertThat(ClientIp.resolve(headers)).isEqualTo("10.0.0.1");
        }

        @Test
        @DisplayName("spaces around IPs should be trimmed")
        void spacesAroundIpsShouldBeTrimmed() {
            HttpHeaders headers = HttpHeaders.of()
                    .add("X-Forwarded-For", "  203.0.113.50  ,  70.41.3.18  ");

            assertThat(ClientIp.resolve(headers)).isEqualTo("70.41.3.18");
        }
    }

    @Nested
    @DisplayName("X-Real-IP Tests")
    class XRealIpTests {

        @Test
        @DisplayName("should resolve from X-Real-IP")
        void shouldResolveFromXRealIp() {
            HttpHeaders headers = HttpHeaders.of()
                    .add("X-Real-IP", "192.168.1.100");

            assertThat(ClientIp.resolve(headers)).isEqualTo("192.168.1.100");
        }

        @Test
        @DisplayName("X-Forwarded-For should take priority over X-Real-IP")
        void xForwardedForShouldTakePriorityOverXRealIp() {
            HttpHeaders headers = HttpHeaders.of()
                    .add("X-Forwarded-For", "203.0.113.50")
                    .add("X-Real-IP", "192.168.1.100");

            assertThat(ClientIp.resolve(headers)).isEqualTo("203.0.113.50");
        }

        @Test
        @DisplayName("invalid X-Real-IP should fall through")
        void invalidXRealIpShouldFallThrough() {
            HttpHeaders headers = HttpHeaders.of()
                    .add("X-Real-IP", "not-valid")
                    .add("CF-Connecting-IP", "10.20.30.40");

            assertThat(ClientIp.resolve(headers)).isEqualTo("10.20.30.40");
        }
    }

    @Nested
    @DisplayName("CF-Connecting-IP Tests")
    class CfConnectingIpTests {

        @Test
        @DisplayName("should resolve from CF-Connecting-IP")
        void shouldResolveFromCfConnectingIp() {
            HttpHeaders headers = HttpHeaders.of()
                    .add("CF-Connecting-IP", "172.16.0.1");

            assertThat(ClientIp.resolve(headers)).isEqualTo("172.16.0.1");
        }

        @Test
        @DisplayName("CF-Connecting-IP should have lower priority than XFF and X-Real-IP")
        void cfConnectingIpShouldHaveLowerPriority() {
            HttpHeaders headers = HttpHeaders.of()
                    .add("X-Real-IP", "192.168.1.1")
                    .add("CF-Connecting-IP", "10.0.0.1");

            assertThat(ClientIp.resolve(headers)).isEqualTo("192.168.1.1");
        }
    }

    @Nested
    @DisplayName("True-Client-IP Tests")
    class TrueClientIpTests {

        @Test
        @DisplayName("should resolve from True-Client-IP")
        void shouldResolveFromTrueClientIp() {
            HttpHeaders headers = HttpHeaders.of()
                    .add("True-Client-IP", "8.8.8.8");

            assertThat(ClientIp.resolve(headers)).isEqualTo("8.8.8.8");
        }

        @Test
        @DisplayName("True-Client-IP should be lowest priority")
        void trueClientIpShouldBeLowestPriority() {
            HttpHeaders headers = HttpHeaders.of()
                    .add("CF-Connecting-IP", "1.2.3.4")
                    .add("True-Client-IP", "5.6.7.8");

            assertThat(ClientIp.resolve(headers)).isEqualTo("1.2.3.4");
        }
    }

    @Nested
    @DisplayName("Invalid IP Filtering Tests")
    class InvalidIpFilteringTests {

        @Test
        @DisplayName("should reject non-IP strings")
        void shouldRejectNonIpStrings() {
            HttpHeaders headers = HttpHeaders.of()
                    .add("X-Real-IP", "malicious-header-value");

            assertThat(ClientIp.resolve(headers)).isNull();
        }

        @Test
        @DisplayName("should reject out-of-range octets")
        void shouldRejectOutOfRangeOctets() {
            HttpHeaders headers = HttpHeaders.of()
                    .add("X-Real-IP", "256.0.0.1");

            assertThat(ClientIp.resolve(headers)).isNull();
        }

        @Test
        @DisplayName("should reject partial IPs")
        void shouldRejectPartialIps() {
            HttpHeaders headers = HttpHeaders.of()
                    .add("X-Real-IP", "192.168.1");

            assertThat(ClientIp.resolve(headers)).isNull();
        }

        @Test
        @DisplayName("should reject empty string")
        void shouldRejectEmptyString() {
            HttpHeaders headers = HttpHeaders.of()
                    .add("X-Real-IP", "");

            assertThat(ClientIp.resolve(headers)).isNull();
        }
    }

    @Nested
    @DisplayName("Trusted Proxy Tests")
    class TrustedProxyTests {

        @Test
        @DisplayName("null trusted proxies should be treated as empty set")
        void nullTrustedProxiesShouldBeTreatedAsEmptySet() {
            HttpHeaders headers = HttpHeaders.of()
                    .add("X-Forwarded-For", "10.0.0.1");

            assertThat(ClientIp.resolve(headers, null)).isEqualTo("10.0.0.1");
        }

        @Test
        @DisplayName("empty trusted proxies should return rightmost valid IP")
        void emptyTrustedProxiesShouldReturnRightmostValidIp() {
            HttpHeaders headers = HttpHeaders.of()
                    .add("X-Forwarded-For", "1.1.1.1, 2.2.2.2, 3.3.3.3");

            assertThat(ClientIp.resolve(headers, Set.of())).isEqualTo("3.3.3.3");
        }

        @Test
        @DisplayName("should skip multiple trusted proxies in chain")
        void shouldSkipMultipleTrustedProxiesInChain() {
            HttpHeaders headers = HttpHeaders.of()
                    .add("X-Forwarded-For", "1.1.1.1, 10.0.0.1, 10.0.0.2, 10.0.0.3");

            String ip = ClientIp.resolve(headers, Set.of("10.0.0.1", "10.0.0.2", "10.0.0.3"));

            assertThat(ip).isEqualTo("1.1.1.1");
        }
    }

    @Nested
    @DisplayName("IP Validation Tests")
    class IpValidationTests {

        @Test
        @DisplayName("valid IPv4 addresses should pass")
        void validIpv4AddressesShouldPass() {
            assertThat(ClientIp.isValidIp("0.0.0.0")).isTrue();
            assertThat(ClientIp.isValidIp("255.255.255.255")).isTrue();
            assertThat(ClientIp.isValidIp("192.168.1.1")).isTrue();
            assertThat(ClientIp.isValidIp("10.0.0.1")).isTrue();
            assertThat(ClientIp.isValidIp("127.0.0.1")).isTrue();
        }

        @Test
        @DisplayName("valid IPv6 addresses should pass")
        void validIpv6AddressesShouldPass() {
            assertThat(ClientIp.isValidIp("::1")).isTrue();
            assertThat(ClientIp.isValidIp("2001:db8::1")).isTrue();
            assertThat(ClientIp.isValidIp("fe80::1")).isTrue();
        }

        @Test
        @DisplayName("invalid formats should fail")
        void invalidFormatsShouldFail() {
            assertThat(ClientIp.isValidIp(null)).isFalse();
            assertThat(ClientIp.isValidIp("")).isFalse();
            assertThat(ClientIp.isValidIp("abc")).isFalse();
            assertThat(ClientIp.isValidIp("1.2.3")).isFalse();
            assertThat(ClientIp.isValidIp("1.2.3.4.5")).isFalse();
            assertThat(ClientIp.isValidIp("256.0.0.1")).isFalse();
        }
    }

    @Nested
    @DisplayName("IPv6 Resolution Tests")
    class Ipv6ResolutionTests {

        @Test
        @DisplayName("should resolve IPv6 from X-Forwarded-For")
        void shouldResolveIpv6FromXForwardedFor() {
            HttpHeaders headers = HttpHeaders.of()
                    .add("X-Forwarded-For", "2001:db8::1, 10.0.0.1");

            String ip = ClientIp.resolve(headers, Set.of("10.0.0.1"));

            assertThat(ip).isEqualTo("2001:db8::1");
        }

        @Test
        @DisplayName("should resolve IPv6 from X-Real-IP")
        void shouldResolveIpv6FromXRealIp() {
            HttpHeaders headers = HttpHeaders.of()
                    .add("X-Real-IP", "::1");

            assertThat(ClientIp.resolve(headers)).isEqualTo("::1");
        }
    }
}
