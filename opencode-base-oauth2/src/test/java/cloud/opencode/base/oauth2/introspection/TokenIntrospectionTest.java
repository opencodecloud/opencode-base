package cloud.opencode.base.oauth2.introspection;

import cloud.opencode.base.oauth2.exception.OAuth2ErrorCode;
import cloud.opencode.base.oauth2.exception.OAuth2Exception;
import cloud.opencode.base.oauth2.http.OAuth2HttpClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

/**
 * TokenIntrospection Tests
 * TokenIntrospection 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.3
 */
@DisplayName("TokenIntrospection 测试")
class TokenIntrospectionTest {

    private static final String ENDPOINT = "https://auth.example.com/introspect";
    private static final String CLIENT_ID = "test-client";
    private static final String CLIENT_SECRET = "test-secret";

    /**
     * Stub HTTP client that returns a configurable response or throws a configurable exception.
     */
    private static class StubHttpClient extends OAuth2HttpClient {
        private String response;
        private RuntimeException exception;
        final AtomicReference<Map<String, String>> lastParams = new AtomicReference<>();
        final AtomicReference<String> lastUrl = new AtomicReference<>();

        StubHttpClient() {
            super(Duration.ofSeconds(1), Duration.ofSeconds(1));
        }

        void setResponse(String response) {
            this.response = response;
            this.exception = null;
        }

        void setException(RuntimeException exception) {
            this.exception = exception;
            this.response = null;
        }

        @Override
        public String postForm(String url, Map<String, String> params) {
            lastUrl.set(url);
            lastParams.set(params);
            if (exception != null) {
                throw exception;
            }
            return response;
        }
    }

    @Nested
    @DisplayName("构造器测试")
    class ConstructorTests {

        @Test
        @DisplayName("null endpoint抛出异常")
        void testNullEndpoint() {
            var client = new StubHttpClient();
            assertThatThrownBy(() -> new TokenIntrospection(null, CLIENT_ID, CLIENT_SECRET, client))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("introspectionEndpoint");
        }

        @Test
        @DisplayName("null clientId抛出异常")
        void testNullClientId() {
            var client = new StubHttpClient();
            assertThatThrownBy(() -> new TokenIntrospection(ENDPOINT, null, CLIENT_SECRET, client))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("clientId");
        }

        @Test
        @DisplayName("null clientSecret抛出异常")
        void testNullClientSecret() {
            var client = new StubHttpClient();
            assertThatThrownBy(() -> new TokenIntrospection(ENDPOINT, CLIENT_ID, null, client))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("clientSecret");
        }

        @Test
        @DisplayName("null httpClient抛出异常")
        void testNullHttpClient() {
            assertThatThrownBy(() -> new TokenIntrospection(ENDPOINT, CLIENT_ID, CLIENT_SECRET, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("httpClient");
        }
    }

    @Nested
    @DisplayName("introspect方法测试")
    class IntrospectTests {

        @Test
        @DisplayName("成功内省活跃Token")
        void testIntrospectActiveToken() {
            var client = new StubHttpClient();
            client.setResponse("""
                    {"active":true,"scope":"read write","client_id":"my-app","username":"john","token_type":"Bearer","sub":"user-123","aud":"https://api.example.com","iss":"https://auth.example.com","jti":"abc-123","exp":1700000000,"iat":1699996400,"nbf":1699996400}""");

            var introspection = new TokenIntrospection(ENDPOINT, CLIENT_ID, CLIENT_SECRET, client);
            IntrospectionResult result = introspection.introspect("test-token");

            assertThat(result.active()).isTrue();
            assertThat(result.scope()).isEqualTo("read write");
            assertThat(result.clientId()).isEqualTo("my-app");
            assertThat(result.username()).isEqualTo("john");
            assertThat(result.tokenType()).isEqualTo("Bearer");
            assertThat(result.sub()).isEqualTo("user-123");
            assertThat(result.aud()).isEqualTo("https://api.example.com");
            assertThat(result.iss()).isEqualTo("https://auth.example.com");
            assertThat(result.jti()).isEqualTo("abc-123");
            assertThat(result.exp()).isNotNull();
            assertThat(result.iat()).isNotNull();
            assertThat(result.nbf()).isNotNull();
        }

        @Test
        @DisplayName("成功内省非活跃Token")
        void testIntrospectInactiveToken() {
            var client = new StubHttpClient();
            client.setResponse("{\"active\":false}");

            var introspection = new TokenIntrospection(ENDPOINT, CLIENT_ID, CLIENT_SECRET, client);
            IntrospectionResult result = introspection.introspect("expired-token");

            assertThat(result.active()).isFalse();
        }

        @Test
        @DisplayName("发送正确的请求参数（无类型提示）")
        void testRequestParamsWithoutTypeHint() {
            var client = new StubHttpClient();
            client.setResponse("{\"active\":false}");

            var introspection = new TokenIntrospection(ENDPOINT, CLIENT_ID, CLIENT_SECRET, client);
            introspection.introspect("my-token");

            Map<String, String> params = client.lastParams.get();
            assertThat(params).containsEntry("token", "my-token");
            assertThat(params).containsEntry("client_id", CLIENT_ID);
            assertThat(params).containsEntry("client_secret", CLIENT_SECRET);
            assertThat(params).doesNotContainKey("token_type_hint");
            assertThat(client.lastUrl.get()).isEqualTo(ENDPOINT);
        }

        @Test
        @DisplayName("发送正确的请求参数（有类型提示）")
        void testRequestParamsWithTypeHint() {
            var client = new StubHttpClient();
            client.setResponse("{\"active\":false}");

            var introspection = new TokenIntrospection(ENDPOINT, CLIENT_ID, CLIENT_SECRET, client);
            introspection.introspect("my-token", "access_token");

            Map<String, String> params = client.lastParams.get();
            assertThat(params).containsEntry("token", "my-token");
            assertThat(params).containsEntry("token_type_hint", "access_token");
            assertThat(params).containsEntry("client_id", CLIENT_ID);
            assertThat(params).containsEntry("client_secret", CLIENT_SECRET);
        }

        @Test
        @DisplayName("空白类型提示不发送token_type_hint")
        void testBlankTypeHintNotSent() {
            var client = new StubHttpClient();
            client.setResponse("{\"active\":false}");

            var introspection = new TokenIntrospection(ENDPOINT, CLIENT_ID, CLIENT_SECRET, client);
            introspection.introspect("my-token", "  ");

            assertThat(client.lastParams.get()).doesNotContainKey("token_type_hint");
        }

        @Test
        @DisplayName("null token抛出NullPointerException")
        void testNullToken() {
            var client = new StubHttpClient();
            var introspection = new TokenIntrospection(ENDPOINT, CLIENT_ID, CLIENT_SECRET, client);

            assertThatThrownBy(() -> introspection.introspect(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("token");
        }

        @Test
        @DisplayName("refresh_token类型提示")
        void testRefreshTokenTypeHint() {
            var client = new StubHttpClient();
            client.setResponse("{\"active\":true}");

            var introspection = new TokenIntrospection(ENDPOINT, CLIENT_ID, CLIENT_SECRET, client);
            introspection.introspect("refresh-token-value", "refresh_token");

            assertThat(client.lastParams.get()).containsEntry("token_type_hint", "refresh_token");
        }
    }

    @Nested
    @DisplayName("错误处理测试")
    class ErrorHandlingTests {

        @Test
        @DisplayName("HTTP错误抛出INTROSPECTION_FAILED")
        void testHttpErrorThrowsIntrospectionFailed() {
            var client = new StubHttpClient();
            client.setException(new OAuth2Exception(OAuth2ErrorCode.SERVER_ERROR, "Internal Server Error"));

            var introspection = new TokenIntrospection(ENDPOINT, CLIENT_ID, CLIENT_SECRET, client);

            assertThatThrownBy(() -> introspection.introspect("bad-token"))
                    .isInstanceOf(OAuth2Exception.class)
                    .satisfies(ex -> {
                        OAuth2Exception oauthEx = (OAuth2Exception) ex;
                        assertThat(oauthEx.errorCode()).isEqualTo(OAuth2ErrorCode.INTROSPECTION_FAILED);
                    });
        }

        @Test
        @DisplayName("不支持错误抛出INTROSPECTION_NOT_SUPPORTED")
        void testNotSupportedError() {
            var client = new StubHttpClient();
            client.setException(new OAuth2Exception(OAuth2ErrorCode.PROVIDER_ERROR, "Introspection not supported"));

            var introspection = new TokenIntrospection(ENDPOINT, CLIENT_ID, CLIENT_SECRET, client);

            assertThatThrownBy(() -> introspection.introspect("token"))
                    .isInstanceOf(OAuth2Exception.class)
                    .satisfies(ex -> {
                        OAuth2Exception oauthEx = (OAuth2Exception) ex;
                        assertThat(oauthEx.errorCode()).isEqualTo(OAuth2ErrorCode.INTROSPECTION_NOT_SUPPORTED);
                    });
        }

        @Test
        @DisplayName("空响应抛出INTROSPECTION_FAILED")
        void testEmptyResponse() {
            var client = new StubHttpClient();
            client.setResponse("");

            var introspection = new TokenIntrospection(ENDPOINT, CLIENT_ID, CLIENT_SECRET, client);

            assertThatThrownBy(() -> introspection.introspect("token"))
                    .isInstanceOf(OAuth2Exception.class)
                    .satisfies(ex -> {
                        OAuth2Exception oauthEx = (OAuth2Exception) ex;
                        assertThat(oauthEx.errorCode()).isEqualTo(OAuth2ErrorCode.INTROSPECTION_FAILED);
                    });
        }

        @Test
        @DisplayName("null响应抛出INTROSPECTION_FAILED")
        void testNullResponse() {
            var client = new StubHttpClient();
            client.setResponse(null);

            var introspection = new TokenIntrospection(ENDPOINT, CLIENT_ID, CLIENT_SECRET, client);

            assertThatThrownBy(() -> introspection.introspect("token"))
                    .isInstanceOf(OAuth2Exception.class)
                    .satisfies(ex -> {
                        OAuth2Exception oauthEx = (OAuth2Exception) ex;
                        assertThat(oauthEx.errorCode()).isEqualTo(OAuth2ErrorCode.INTROSPECTION_FAILED);
                    });
        }

        @Test
        @DisplayName("非JSON响应抛出INTROSPECTION_FAILED（缺少active字段）")
        void testNonJsonResponse() {
            var client = new StubHttpClient();
            client.setResponse("not json at all");

            var introspection = new TokenIntrospection(ENDPOINT, CLIENT_ID, CLIENT_SECRET, client);

            assertThatThrownBy(() -> introspection.introspect("token"))
                    .isInstanceOf(OAuth2Exception.class)
                    .satisfies(ex -> {
                        OAuth2Exception oauthEx = (OAuth2Exception) ex;
                        assertThat(oauthEx.errorCode()).isEqualTo(OAuth2ErrorCode.INTROSPECTION_FAILED);
                        assertThat(oauthEx.getMessage()).contains("active");
                    });
        }

        @Test
        @DisplayName("RuntimeException包装为INTROSPECTION_FAILED")
        void testRuntimeExceptionWrapped() {
            var client = new StubHttpClient();
            client.setException(new RuntimeException("connection refused"));

            var introspection = new TokenIntrospection(ENDPOINT, CLIENT_ID, CLIENT_SECRET, client);

            assertThatThrownBy(() -> introspection.introspect("token"))
                    .isInstanceOf(OAuth2Exception.class)
                    .satisfies(ex -> {
                        OAuth2Exception oauthEx = (OAuth2Exception) ex;
                        assertThat(oauthEx.errorCode()).isEqualTo(OAuth2ErrorCode.INTROSPECTION_FAILED);
                    });
        }
    }

    @Nested
    @DisplayName("JSON解析测试")
    class JsonParsingTests {

        @Test
        @DisplayName("解析最小响应")
        void testParseMinimalResponse() {
            var client = new StubHttpClient();
            client.setResponse("{\"active\":true}");

            var introspection = new TokenIntrospection(ENDPOINT, CLIENT_ID, CLIENT_SECRET, client);
            IntrospectionResult result = introspection.introspect("token");

            assertThat(result.active()).isTrue();
            assertThat(result.scope()).isNull();
            assertThat(result.clientId()).isNull();
        }

        @Test
        @DisplayName("解析带有所有字段的响应")
        void testParseFullResponse() {
            var client = new StubHttpClient();
            client.setResponse("""
                    {
                      "active": true,
                      "scope": "openid profile email",
                      "client_id": "s6BhdRkqt3",
                      "username": "jdoe",
                      "token_type": "Bearer",
                      "exp": 1419356238,
                      "iat": 1419350238,
                      "nbf": 1419350238,
                      "sub": "Z5O3upPC88QrAjx00dis",
                      "aud": "https://protected.example.net/resource",
                      "iss": "https://server.example.com/",
                      "jti": "JlbmMeSs7EM"
                    }""");

            var introspection = new TokenIntrospection(ENDPOINT, CLIENT_ID, CLIENT_SECRET, client);
            IntrospectionResult result = introspection.introspect("token");

            assertThat(result.active()).isTrue();
            assertThat(result.scope()).isEqualTo("openid profile email");
            assertThat(result.clientId()).isEqualTo("s6BhdRkqt3");
            assertThat(result.username()).isEqualTo("jdoe");
            assertThat(result.tokenType()).isEqualTo("Bearer");
            assertThat(result.sub()).isEqualTo("Z5O3upPC88QrAjx00dis");
            assertThat(result.aud()).isEqualTo("https://protected.example.net/resource");
            assertThat(result.iss()).isEqualTo("https://server.example.com/");
            assertThat(result.jti()).isEqualTo("JlbmMeSs7EM");
            assertThat(result.exp().getEpochSecond()).isEqualTo(1419356238L);
            assertThat(result.iat().getEpochSecond()).isEqualTo(1419350238L);
            assertThat(result.nbf().getEpochSecond()).isEqualTo(1419350238L);
        }

        @Test
        @DisplayName("解析空JSON对象抛出INTROSPECTION_FAILED（缺少active字段）")
        void testParseEmptyObject() {
            var client = new StubHttpClient();
            client.setResponse("{}");

            var introspection = new TokenIntrospection(ENDPOINT, CLIENT_ID, CLIENT_SECRET, client);

            assertThatThrownBy(() -> introspection.introspect("token"))
                    .isInstanceOf(OAuth2Exception.class)
                    .satisfies(ex -> {
                        OAuth2Exception oauthEx = (OAuth2Exception) ex;
                        assertThat(oauthEx.errorCode()).isEqualTo(OAuth2ErrorCode.INTROSPECTION_FAILED);
                        assertThat(oauthEx.getMessage()).contains("active");
                    });
        }

        @Test
        @DisplayName("claims map只包含非标准字段")
        void testClaimsContainAllFields() {
            var client = new StubHttpClient();
            client.setResponse("""
                    {"active":true,"scope":"read","custom_field":"custom_value"}""");

            var introspection = new TokenIntrospection(ENDPOINT, CLIENT_ID, CLIENT_SECRET, client);
            IntrospectionResult result = introspection.introspect("token");

            // Standard fields (active, scope) should be excluded from claims
            assertThat(result.claims()).doesNotContainKey("active");
            assertThat(result.claims()).doesNotContainKey("scope");
            assertThat(result.claims()).containsKey("custom_field");
        }
    }
}
