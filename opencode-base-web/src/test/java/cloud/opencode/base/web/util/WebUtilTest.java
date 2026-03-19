package cloud.opencode.base.web.util;

import org.junit.jupiter.api.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * WebUtilTest Tests
 * WebUtilTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
@DisplayName("WebUtil Tests")
class WebUtilTest {

    @Nested
    @DisplayName("URL Encoding Tests")
    class UrlEncodingTests {

        @Test
        @DisplayName("urlEncode should encode special characters")
        void urlEncodeShouldEncodeSpecialCharacters() {
            assertThat(WebUtil.urlEncode("hello world")).isEqualTo("hello+world");
            assertThat(WebUtil.urlEncode("a=b&c=d")).isEqualTo("a%3Db%26c%3Dd");
        }

        @Test
        @DisplayName("urlDecode should decode encoded string")
        void urlDecodeShouldDecodeEncodedString() {
            assertThat(WebUtil.urlDecode("hello+world")).isEqualTo("hello world");
            assertThat(WebUtil.urlDecode("a%3Db%26c%3Dd")).isEqualTo("a=b&c=d");
        }

        @Test
        @DisplayName("urlEncode and urlDecode should be reversible")
        void urlEncodeAndUrlDecodeShouldBeReversible() {
            String original = "hello world & more=stuff";
            String encoded = WebUtil.urlEncode(original);
            String decoded = WebUtil.urlDecode(encoded);

            assertThat(decoded).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("Query String Tests")
    class QueryStringTests {

        @Test
        @DisplayName("parseQueryString should parse query string")
        void parseQueryStringShouldParseQueryString() {
            Map<String, String> params = WebUtil.parseQueryString("name=test&value=123");

            assertThat(params).containsEntry("name", "test");
            assertThat(params).containsEntry("value", "123");
        }

        @Test
        @DisplayName("parseQueryString should handle query with question mark")
        void parseQueryStringShouldHandleQueryWithQuestionMark() {
            Map<String, String> params = WebUtil.parseQueryString("?name=test&value=123");

            assertThat(params).containsEntry("name", "test");
        }

        @Test
        @DisplayName("parseQueryString should decode values")
        void parseQueryStringShouldDecodeValues() {
            Map<String, String> params = WebUtil.parseQueryString("name=hello+world");

            assertThat(params).containsEntry("name", "hello world");
        }

        @Test
        @DisplayName("parseQueryString should return empty map for null")
        void parseQueryStringShouldReturnEmptyMapForNull() {
            Map<String, String> params = WebUtil.parseQueryString(null);

            assertThat(params).isEmpty();
        }

        @Test
        @DisplayName("parseQueryString should return empty map for blank string")
        void parseQueryStringShouldReturnEmptyMapForBlankString() {
            Map<String, String> params = WebUtil.parseQueryString("   ");

            assertThat(params).isEmpty();
        }

        @Test
        @DisplayName("buildQueryString should build query string")
        void buildQueryStringShouldBuildQueryString() {
            Map<String, String> params = Map.of("name", "test", "value", "123");

            String queryString = WebUtil.buildQueryString(params);

            assertThat(queryString).contains("name=test");
            assertThat(queryString).contains("value=123");
            assertThat(queryString).contains("&");
        }

        @Test
        @DisplayName("buildQueryString should encode values")
        void buildQueryStringShouldEncodeValues() {
            Map<String, String> params = Map.of("name", "hello world");

            String queryString = WebUtil.buildQueryString(params);

            assertThat(queryString).contains("hello+world");
        }

        @Test
        @DisplayName("buildQueryString should return empty string for null")
        void buildQueryStringShouldReturnEmptyStringForNull() {
            String queryString = WebUtil.buildQueryString(null);

            assertThat(queryString).isEmpty();
        }

        @Test
        @DisplayName("buildQueryString should return empty string for empty map")
        void buildQueryStringShouldReturnEmptyStringForEmptyMap() {
            String queryString = WebUtil.buildQueryString(Map.of());

            assertThat(queryString).isEmpty();
        }
    }

    @Nested
    @DisplayName("IP Validation Tests")
    class IpValidationTests {

        @Test
        @DisplayName("isValidIp should return true for valid IP")
        void isValidIpShouldReturnTrueForValidIp() {
            assertThat(WebUtil.isValidIp("192.168.1.1")).isTrue();
            assertThat(WebUtil.isValidIp("0.0.0.0")).isTrue();
            assertThat(WebUtil.isValidIp("255.255.255.255")).isTrue();
        }

        @Test
        @DisplayName("isValidIp should return false for invalid IP")
        void isValidIpShouldReturnFalseForInvalidIp() {
            assertThat(WebUtil.isValidIp("256.1.1.1")).isFalse();
            assertThat(WebUtil.isValidIp("1.2.3")).isFalse();
            assertThat(WebUtil.isValidIp("invalid")).isFalse();
            assertThat(WebUtil.isValidIp(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Email Validation Tests")
    class EmailValidationTests {

        @Test
        @DisplayName("isValidEmail should return true for valid email")
        void isValidEmailShouldReturnTrueForValidEmail() {
            assertThat(WebUtil.isValidEmail("test@example.com")).isTrue();
            assertThat(WebUtil.isValidEmail("user.name@domain.org")).isTrue();
            assertThat(WebUtil.isValidEmail("user+tag@domain.co.uk")).isTrue();
        }

        @Test
        @DisplayName("isValidEmail should return false for invalid email")
        void isValidEmailShouldReturnFalseForInvalidEmail() {
            assertThat(WebUtil.isValidEmail("invalid")).isFalse();
            assertThat(WebUtil.isValidEmail("@domain.com")).isFalse();
            assertThat(WebUtil.isValidEmail("user@")).isFalse();
            assertThat(WebUtil.isValidEmail(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("URL Validation Tests")
    class UrlValidationTests {

        @Test
        @DisplayName("isValidUrl should return true for valid URL")
        void isValidUrlShouldReturnTrueForValidUrl() {
            assertThat(WebUtil.isValidUrl("https://example.com")).isTrue();
            assertThat(WebUtil.isValidUrl("http://localhost:8080")).isTrue();
            assertThat(WebUtil.isValidUrl("https://example.com/path")).isTrue();
        }

        @Test
        @DisplayName("isValidUrl should return false for invalid URL")
        void isValidUrlShouldReturnFalseForInvalidUrl() {
            assertThat(WebUtil.isValidUrl("invalid")).isFalse();
            assertThat(WebUtil.isValidUrl("ftp://example.com")).isFalse();
            assertThat(WebUtil.isValidUrl(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Private IP Tests")
    class PrivateIpTests {

        @Test
        @DisplayName("isPrivateIp should return true for private IPs")
        void isPrivateIpShouldReturnTrueForPrivateIps() {
            assertThat(WebUtil.isPrivateIp("10.0.0.1")).isTrue();
            assertThat(WebUtil.isPrivateIp("10.255.255.255")).isTrue();
            assertThat(WebUtil.isPrivateIp("172.16.0.1")).isTrue();
            assertThat(WebUtil.isPrivateIp("172.31.255.255")).isTrue();
            assertThat(WebUtil.isPrivateIp("192.168.0.1")).isTrue();
            assertThat(WebUtil.isPrivateIp("192.168.255.255")).isTrue();
            assertThat(WebUtil.isPrivateIp("127.0.0.1")).isTrue();
        }

        @Test
        @DisplayName("isPrivateIp should return false for public IPs")
        void isPrivateIpShouldReturnFalseForPublicIps() {
            assertThat(WebUtil.isPrivateIp("8.8.8.8")).isFalse();
            assertThat(WebUtil.isPrivateIp("1.1.1.1")).isFalse();
            assertThat(WebUtil.isPrivateIp("172.15.0.1")).isFalse();
            assertThat(WebUtil.isPrivateIp("172.32.0.1")).isFalse();
        }

        @Test
        @DisplayName("isPrivateIp should return false for invalid IP")
        void isPrivateIpShouldReturnFalseForInvalidIp() {
            assertThat(WebUtil.isPrivateIp("invalid")).isFalse();
        }
    }

    @Nested
    @DisplayName("IP Conversion Tests")
    class IpConversionTests {

        @Test
        @DisplayName("ipToLong should convert IP to long")
        void ipToLongShouldConvertIpToLong() {
            assertThat(WebUtil.ipToLong("0.0.0.0")).isEqualTo(0L);
            assertThat(WebUtil.ipToLong("0.0.0.1")).isEqualTo(1L);
            assertThat(WebUtil.ipToLong("192.168.1.1")).isEqualTo(3232235777L);
        }

        @Test
        @DisplayName("longToIp should convert long to IP")
        void longToIpShouldConvertLongToIp() {
            assertThat(WebUtil.longToIp(0L)).isEqualTo("0.0.0.0");
            assertThat(WebUtil.longToIp(1L)).isEqualTo("0.0.0.1");
            assertThat(WebUtil.longToIp(3232235777L)).isEqualTo("192.168.1.1");
        }

        @Test
        @DisplayName("ipToLong and longToIp should be reversible")
        void ipToLongAndLongToIpShouldBeReversible() {
            String original = "192.168.1.100";
            long longIp = WebUtil.ipToLong(original);
            String converted = WebUtil.longToIp(longIp);

            assertThat(converted).isEqualTo(original);
        }
    }
}
