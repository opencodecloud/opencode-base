package cloud.opencode.base.oauth2.discovery;

import cloud.opencode.base.oauth2.exception.OAuth2ErrorCode;
import cloud.opencode.base.oauth2.exception.OAuth2Exception;
import cloud.opencode.base.oauth2.http.OAuth2HttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * OidcDiscovery Tests
 * OidcDiscovery 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.3
 */
@DisplayName("OidcDiscovery 测试")
class OidcDiscoveryTest {

    private static final String ISSUER = "https://accounts.example.com";

    /**
     * Stub HTTP client that returns preconfigured responses for GET requests.
     */
    private static final class StubHttpClient extends OAuth2HttpClient {
        private String responseBody;
        private OAuth2Exception exceptionToThrow;
        private int getCallCount;

        StubHttpClient() {
            super();
        }

        void setResponseBody(String body) {
            this.responseBody = body;
        }

        void setExceptionToThrow(OAuth2Exception ex) {
            this.exceptionToThrow = ex;
        }

        int getGetCallCount() {
            return getCallCount;
        }

        @Override
        public String get(String url, Map<String, String> headers) {
            getCallCount++;
            if (exceptionToThrow != null) {
                throw exceptionToThrow;
            }
            return responseBody;
        }
    }

    @AfterEach
    void tearDown() {
        OidcDiscovery.clearCache();
    }

    private static String createDiscoveryJson(String issuer) {
        return """
                {
                    "issuer": "%s",
                    "authorization_endpoint": "%s/authorize",
                    "token_endpoint": "%s/token",
                    "userinfo_endpoint": "%s/userinfo",
                    "jwks_uri": "%s/jwks",
                    "registration_endpoint": "%s/register",
                    "revocation_endpoint": "%s/revoke",
                    "introspection_endpoint": "%s/introspect",
                    "device_authorization_endpoint": "%s/device",
                    "pushed_authorization_request_endpoint": "%s/par",
                    "scopes_supported": ["openid", "email", "profile"],
                    "response_types_supported": ["code", "token"],
                    "grant_types_supported": ["authorization_code", "refresh_token"],
                    "token_endpoint_auth_methods_supported": ["client_secret_basic"],
                    "code_challenge_methods_supported": ["S256", "plain"]
                }
                """.formatted(issuer, issuer, issuer, issuer, issuer,
                issuer, issuer, issuer, issuer, issuer);
    }

    @Nested
    @DisplayName("discover() 方法测试")
    class DiscoverTests {

        @Test
        @DisplayName("成功发现并解析完整文档")
        void testDiscoverSuccess() {
            StubHttpClient httpClient = new StubHttpClient();
            httpClient.setResponseBody(createDiscoveryJson(ISSUER));

            DiscoveryDocument doc = OidcDiscovery.discover(ISSUER, httpClient);

            assertThat(doc).isNotNull();
            assertThat(doc.issuer()).isEqualTo(ISSUER);
            assertThat(doc.authorizationEndpoint()).isEqualTo(ISSUER + "/authorize");
            assertThat(doc.tokenEndpoint()).isEqualTo(ISSUER + "/token");
            assertThat(doc.userinfoEndpoint()).isEqualTo(ISSUER + "/userinfo");
            assertThat(doc.jwksUri()).isEqualTo(ISSUER + "/jwks");
            assertThat(doc.registrationEndpoint()).isEqualTo(ISSUER + "/register");
            assertThat(doc.revocationEndpoint()).isEqualTo(ISSUER + "/revoke");
            assertThat(doc.introspectionEndpoint()).isEqualTo(ISSUER + "/introspect");
            assertThat(doc.deviceAuthorizationEndpoint()).isEqualTo(ISSUER + "/device");
            assertThat(doc.parEndpoint()).isEqualTo(ISSUER + "/par");
        }

        @Test
        @DisplayName("成功解析数组字段")
        void testDiscoverArrayFields() {
            StubHttpClient httpClient = new StubHttpClient();
            httpClient.setResponseBody(createDiscoveryJson(ISSUER));

            DiscoveryDocument doc = OidcDiscovery.discover(ISSUER, httpClient);

            assertThat(doc.scopesSupported()).containsExactly("openid", "email", "profile");
            assertThat(doc.responseTypesSupported()).containsExactly("code", "token");
            assertThat(doc.grantTypesSupported()).containsExactly("authorization_code", "refresh_token");
            assertThat(doc.tokenEndpointAuthMethodsSupported()).containsExactly("client_secret_basic");
            assertThat(doc.codeChallengeMethodsSupported()).containsExactly("S256", "plain");
        }

        @Test
        @DisplayName("尾部斜杠被正确处理")
        void testDiscoverTrailingSlash() {
            StubHttpClient httpClient = new StubHttpClient();
            httpClient.setResponseBody(createDiscoveryJson(ISSUER));

            DiscoveryDocument doc = OidcDiscovery.discover(ISSUER + "/", httpClient);

            assertThat(doc).isNotNull();
            assertThat(doc.issuer()).isEqualTo(ISSUER);
        }

        @Test
        @DisplayName("null issuerUrl 抛出异常")
        void testDiscoverNullIssuer() {
            StubHttpClient httpClient = new StubHttpClient();
            assertThatThrownBy(() -> OidcDiscovery.discover(null, httpClient))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("issuerUrl");
        }

        @Test
        @DisplayName("null httpClient 抛出异常")
        void testDiscoverNullHttpClient() {
            assertThatThrownBy(() -> OidcDiscovery.discover(ISSUER, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("httpClient");
        }
    }

    @Nested
    @DisplayName("错误处理测试")
    class ErrorHandlingTests {

        @Test
        @DisplayName("HTTP 请求失败抛出 DISCOVERY_FAILED")
        void testDiscoverNetworkError() {
            StubHttpClient httpClient = new StubHttpClient();
            httpClient.setExceptionToThrow(
                    new OAuth2Exception(OAuth2ErrorCode.NETWORK_ERROR, "Connection refused"));

            assertThatThrownBy(() -> OidcDiscovery.discover(ISSUER, httpClient))
                    .isInstanceOf(OAuth2Exception.class)
                    .satisfies(e -> {
                        OAuth2Exception oae = (OAuth2Exception) e;
                        assertThat(oae.errorCode()).isEqualTo(OAuth2ErrorCode.DISCOVERY_FAILED);
                    });
        }

        @Test
        @DisplayName("空响应抛出 DISCOVERY_INVALID_RESPONSE")
        void testDiscoverEmptyResponse() {
            StubHttpClient httpClient = new StubHttpClient();
            httpClient.setResponseBody("");

            assertThatThrownBy(() -> OidcDiscovery.discover(ISSUER, httpClient))
                    .isInstanceOf(OAuth2Exception.class)
                    .satisfies(e -> {
                        OAuth2Exception oae = (OAuth2Exception) e;
                        assertThat(oae.errorCode()).isEqualTo(OAuth2ErrorCode.DISCOVERY_INVALID_RESPONSE);
                    });
        }

        @Test
        @DisplayName("null 响应抛出 DISCOVERY_INVALID_RESPONSE")
        void testDiscoverNullResponse() {
            StubHttpClient httpClient = new StubHttpClient();
            httpClient.setResponseBody(null);

            assertThatThrownBy(() -> OidcDiscovery.discover(ISSUER, httpClient))
                    .isInstanceOf(OAuth2Exception.class)
                    .satisfies(e -> {
                        OAuth2Exception oae = (OAuth2Exception) e;
                        assertThat(oae.errorCode()).isEqualTo(OAuth2ErrorCode.DISCOVERY_INVALID_RESPONSE);
                    });
        }

        @Test
        @DisplayName("缺少 issuer 字段抛出 DISCOVERY_INVALID_RESPONSE")
        void testDiscoverMissingIssuer() {
            StubHttpClient httpClient = new StubHttpClient();
            httpClient.setResponseBody("""
                    {"authorization_endpoint": "https://example.com/auth"}
                    """);

            assertThatThrownBy(() -> OidcDiscovery.discover(ISSUER, httpClient))
                    .isInstanceOf(OAuth2Exception.class)
                    .satisfies(e -> {
                        OAuth2Exception oae = (OAuth2Exception) e;
                        assertThat(oae.errorCode()).isEqualTo(OAuth2ErrorCode.DISCOVERY_INVALID_RESPONSE);
                    });
        }

        @Test
        @DisplayName("HTTP issuer URL 被 SSRF 防护拒绝")
        void testDiscoverRejectsHttpUrl() {
            StubHttpClient httpClient = new StubHttpClient();
            assertThatThrownBy(() -> OidcDiscovery.discover("http://evil.example.com", httpClient))
                    .isInstanceOf(OAuth2Exception.class)
                    .satisfies(e -> {
                        OAuth2Exception oae = (OAuth2Exception) e;
                        assertThat(oae.errorCode()).isEqualTo(OAuth2ErrorCode.DISCOVERY_FAILED);
                        assertThat(oae.getMessage()).contains("HTTPS");
                    });
        }

        @Test
        @DisplayName("空 issuer URL 被拒绝")
        void testDiscoverRejectsEmptyUrl() {
            StubHttpClient httpClient = new StubHttpClient();
            assertThatThrownBy(() -> OidcDiscovery.discover("", httpClient))
                    .isInstanceOf(OAuth2Exception.class)
                    .satisfies(e -> {
                        OAuth2Exception oae = (OAuth2Exception) e;
                        assertThat(oae.errorCode()).isEqualTo(OAuth2ErrorCode.DISCOVERY_FAILED);
                    });
        }

        @Test
        @DisplayName("issuer 不匹配抛出 DISCOVERY_INVALID_RESPONSE")
        void testDiscoverIssuerMismatch() {
            StubHttpClient httpClient = new StubHttpClient();
            httpClient.setResponseBody(createDiscoveryJson("https://evil.example.com"));

            assertThatThrownBy(() -> OidcDiscovery.discover(ISSUER, httpClient))
                    .isInstanceOf(OAuth2Exception.class)
                    .satisfies(e -> {
                        OAuth2Exception oae = (OAuth2Exception) e;
                        assertThat(oae.errorCode()).isEqualTo(OAuth2ErrorCode.DISCOVERY_INVALID_RESPONSE);
                        assertThat(oae.getMessage()).contains("mismatch");
                    });
        }
    }

    @Nested
    @DisplayName("缓存测试")
    class CacheTests {

        @Test
        @DisplayName("第二次调用使用缓存")
        void testCaching() {
            StubHttpClient httpClient = new StubHttpClient();
            httpClient.setResponseBody(createDiscoveryJson(ISSUER));

            DiscoveryDocument doc1 = OidcDiscovery.discover(ISSUER, httpClient);
            DiscoveryDocument doc2 = OidcDiscovery.discover(ISSUER, httpClient);

            assertThat(doc1).isSameAs(doc2);
            // HTTP client should be called only once
            assertThat(httpClient.getGetCallCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("clearCache 清除缓存后重新获取")
        void testClearCache() {
            StubHttpClient httpClient = new StubHttpClient();
            httpClient.setResponseBody(createDiscoveryJson(ISSUER));

            OidcDiscovery.discover(ISSUER, httpClient);
            OidcDiscovery.clearCache();
            OidcDiscovery.discover(ISSUER, httpClient);

            assertThat(httpClient.getGetCallCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("不同 issuer 不共享缓存")
        void testCacheSeparation() {
            String issuer2 = "https://other.example.com";

            StubHttpClient httpClient1 = new StubHttpClient();
            httpClient1.setResponseBody(createDiscoveryJson(ISSUER));

            StubHttpClient httpClient2 = new StubHttpClient();
            httpClient2.setResponseBody(createDiscoveryJson(issuer2));

            DiscoveryDocument doc1 = OidcDiscovery.discover(ISSUER, httpClient1);
            DiscoveryDocument doc2 = OidcDiscovery.discover(issuer2, httpClient2);

            assertThat(doc1.issuer()).isEqualTo(ISSUER);
            assertThat(doc2.issuer()).isEqualTo(issuer2);
            assertThat(httpClient1.getGetCallCount()).isEqualTo(1);
            assertThat(httpClient2.getGetCallCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("最小文档解析测试")
    class MinimalDocumentTests {

        @Test
        @DisplayName("仅包含 issuer 的最小文档")
        void testMinimalDocument() {
            StubHttpClient httpClient = new StubHttpClient();
            httpClient.setResponseBody("""
                    {"issuer": "%s"}
                    """.formatted(ISSUER));

            DiscoveryDocument doc = OidcDiscovery.discover(ISSUER, httpClient);

            assertThat(doc.issuer()).isEqualTo(ISSUER);
            assertThat(doc.authorizationEndpoint()).isNull();
            assertThat(doc.tokenEndpoint()).isNull();
            assertThat(doc.scopesSupported()).isEmpty();
            assertThat(doc.grantTypesSupported()).isEmpty();
            assertThat(doc.codeChallengeMethodsSupported()).isEmpty();
        }
    }
}
