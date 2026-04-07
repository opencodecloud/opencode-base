package cloud.opencode.base.oauth2.par;

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
 * PushedAuthorizationRequest Tests
 * PushedAuthorizationRequest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.3
 */
@DisplayName("PushedAuthorizationRequest 测试")
class PushedAuthorizationRequestTest {

    private static final String PAR_ENDPOINT = "https://auth.example.com/par";
    private static final String CLIENT_ID = "test-client";
    private static final String CLIENT_SECRET = "test-secret";

    /**
     * Stub HTTP client for testing without Mockito (JPMS compatibility).
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
        @DisplayName("null parEndpoint抛出异常")
        void testNullParEndpoint() {
            var client = new StubHttpClient();
            assertThatThrownBy(() ->
                    new PushedAuthorizationRequest(null, CLIENT_ID, CLIENT_SECRET, client))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("parEndpoint");
        }

        @Test
        @DisplayName("null clientId抛出异常")
        void testNullClientId() {
            var client = new StubHttpClient();
            assertThatThrownBy(() ->
                    new PushedAuthorizationRequest(PAR_ENDPOINT, null, CLIENT_SECRET, client))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("clientId");
        }

        @Test
        @DisplayName("null clientSecret抛出异常")
        void testNullClientSecret() {
            var client = new StubHttpClient();
            assertThatThrownBy(() ->
                    new PushedAuthorizationRequest(PAR_ENDPOINT, CLIENT_ID, null, client))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("clientSecret");
        }

        @Test
        @DisplayName("null httpClient抛出异常")
        void testNullHttpClient() {
            assertThatThrownBy(() ->
                    new PushedAuthorizationRequest(PAR_ENDPOINT, CLIENT_ID, CLIENT_SECRET, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("httpClient");
        }
    }

    @Nested
    @DisplayName("push方法测试")
    class PushTests {

        @Test
        @DisplayName("成功推送授权请求")
        void testSuccessfulPush() {
            var client = new StubHttpClient();
            client.setResponse("""
                    {"request_uri":"urn:ietf:params:oauth:request_uri:tiW8ACQgHN","expires_in":60}""");

            var par = new PushedAuthorizationRequest(PAR_ENDPOINT, CLIENT_ID, CLIENT_SECRET, client);
            ParResponse result = par.push(Map.of(
                    "response_type", "code",
                    "redirect_uri", "https://app.example.com/callback",
                    "scope", "openid profile"
            ));

            assertThat(result.requestUri()).isEqualTo("urn:ietf:params:oauth:request_uri:tiW8ACQgHN");
            assertThat(result.expiresIn()).isEqualTo(60);
            assertThat(result.createdAt()).isNotNull();
        }

        @Test
        @DisplayName("发送正确的请求参数")
        void testRequestParams() {
            var client = new StubHttpClient();
            client.setResponse("{\"request_uri\":\"urn:test\",\"expires_in\":60}");

            var par = new PushedAuthorizationRequest(PAR_ENDPOINT, CLIENT_ID, CLIENT_SECRET, client);
            par.push(Map.of(
                    "response_type", "code",
                    "scope", "openid"
            ));

            Map<String, String> params = client.lastParams.get();
            assertThat(params).containsEntry("response_type", "code");
            assertThat(params).containsEntry("scope", "openid");
            assertThat(params).containsEntry("client_id", CLIENT_ID);
            assertThat(params).containsEntry("client_secret", CLIENT_SECRET);
            assertThat(client.lastUrl.get()).isEqualTo(PAR_ENDPOINT);
        }

        @Test
        @DisplayName("null参数抛出NullPointerException")
        void testNullParams() {
            var client = new StubHttpClient();
            var par = new PushedAuthorizationRequest(PAR_ENDPOINT, CLIENT_ID, CLIENT_SECRET, client);

            assertThatThrownBy(() -> par.push(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("authorizationParams");
        }

        @Test
        @DisplayName("空Map推送成功")
        void testEmptyParams() {
            var client = new StubHttpClient();
            client.setResponse("{\"request_uri\":\"urn:test\",\"expires_in\":30}");

            var par = new PushedAuthorizationRequest(PAR_ENDPOINT, CLIENT_ID, CLIENT_SECRET, client);
            ParResponse result = par.push(Map.of());

            assertThat(result.requestUri()).isEqualTo("urn:test");
        }
    }

    @Nested
    @DisplayName("错误处理测试")
    class ErrorHandlingTests {

        @Test
        @DisplayName("HTTP错误抛出PAR_FAILED")
        void testHttpErrorThrowsParFailed() {
            var client = new StubHttpClient();
            client.setException(new OAuth2Exception(OAuth2ErrorCode.SERVER_ERROR, "Internal Server Error"));

            var par = new PushedAuthorizationRequest(PAR_ENDPOINT, CLIENT_ID, CLIENT_SECRET, client);

            assertThatThrownBy(() -> par.push(Map.of("response_type", "code")))
                    .isInstanceOf(OAuth2Exception.class)
                    .satisfies(ex -> {
                        OAuth2Exception oauthEx = (OAuth2Exception) ex;
                        assertThat(oauthEx.errorCode()).isEqualTo(OAuth2ErrorCode.PAR_FAILED);
                    });
        }

        @Test
        @DisplayName("不支持错误抛出PAR_NOT_SUPPORTED")
        void testNotSupportedError() {
            var client = new StubHttpClient();
            client.setException(new OAuth2Exception(OAuth2ErrorCode.PROVIDER_ERROR, "PAR not supported"));

            var par = new PushedAuthorizationRequest(PAR_ENDPOINT, CLIENT_ID, CLIENT_SECRET, client);

            assertThatThrownBy(() -> par.push(Map.of("response_type", "code")))
                    .isInstanceOf(OAuth2Exception.class)
                    .satisfies(ex -> {
                        OAuth2Exception oauthEx = (OAuth2Exception) ex;
                        assertThat(oauthEx.errorCode()).isEqualTo(OAuth2ErrorCode.PAR_NOT_SUPPORTED);
                    });
        }

        @Test
        @DisplayName("空响应抛出PAR_FAILED")
        void testEmptyResponse() {
            var client = new StubHttpClient();
            client.setResponse("");

            var par = new PushedAuthorizationRequest(PAR_ENDPOINT, CLIENT_ID, CLIENT_SECRET, client);

            assertThatThrownBy(() -> par.push(Map.of("response_type", "code")))
                    .isInstanceOf(OAuth2Exception.class)
                    .satisfies(ex -> {
                        OAuth2Exception oauthEx = (OAuth2Exception) ex;
                        assertThat(oauthEx.errorCode()).isEqualTo(OAuth2ErrorCode.PAR_FAILED);
                    });
        }

        @Test
        @DisplayName("缺少request_uri抛出PAR_FAILED")
        void testMissingRequestUri() {
            var client = new StubHttpClient();
            client.setResponse("{\"expires_in\":60}");

            var par = new PushedAuthorizationRequest(PAR_ENDPOINT, CLIENT_ID, CLIENT_SECRET, client);

            assertThatThrownBy(() -> par.push(Map.of("response_type", "code")))
                    .isInstanceOf(OAuth2Exception.class)
                    .satisfies(ex -> {
                        OAuth2Exception oauthEx = (OAuth2Exception) ex;
                        assertThat(oauthEx.errorCode()).isEqualTo(OAuth2ErrorCode.PAR_FAILED);
                    });
        }

        @Test
        @DisplayName("RuntimeException包装为PAR_FAILED")
        void testRuntimeExceptionWrapped() {
            var client = new StubHttpClient();
            client.setException(new RuntimeException("connection refused"));

            var par = new PushedAuthorizationRequest(PAR_ENDPOINT, CLIENT_ID, CLIENT_SECRET, client);

            assertThatThrownBy(() -> par.push(Map.of("response_type", "code")))
                    .isInstanceOf(OAuth2Exception.class)
                    .satisfies(ex -> {
                        OAuth2Exception oauthEx = (OAuth2Exception) ex;
                        assertThat(oauthEx.errorCode()).isEqualTo(OAuth2ErrorCode.PAR_FAILED);
                    });
        }

        @Test
        @DisplayName("缺少expires_in时默认为0")
        void testMissingExpiresIn() {
            var client = new StubHttpClient();
            client.setResponse("{\"request_uri\":\"urn:test\"}");

            var par = new PushedAuthorizationRequest(PAR_ENDPOINT, CLIENT_ID, CLIENT_SECRET, client);
            ParResponse result = par.push(Map.of("response_type", "code"));

            assertThat(result.requestUri()).isEqualTo("urn:test");
            assertThat(result.expiresIn()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("buildAuthorizationUrl方法测试")
    class BuildAuthorizationUrlTests {

        @Test
        @DisplayName("构建正确的授权URL")
        void testBuildAuthorizationUrl() {
            ParResponse response = new ParResponse(
                    "urn:ietf:params:oauth:request_uri:tiW8ACQgHN", 60, null);

            String url = PushedAuthorizationRequest.buildAuthorizationUrl(
                    "https://auth.example.com/authorize", response, "my-client");

            assertThat(url).startsWith("https://auth.example.com/authorize?");
            assertThat(url).contains("client_id=my-client");
            assertThat(url).contains("request_uri=");
        }

        @Test
        @DisplayName("URL编码request_uri")
        void testUrlEncodesRequestUri() {
            ParResponse response = new ParResponse(
                    "urn:ietf:params:oauth:request_uri:abc 123", 60, null);

            String url = PushedAuthorizationRequest.buildAuthorizationUrl(
                    "https://auth.example.com/authorize", response, "my-client");

            // Space should be encoded
            assertThat(url).doesNotContain(" ");
            assertThat(url).contains("request_uri=urn");
        }

        @Test
        @DisplayName("null authorizationEndpoint抛出异常")
        void testNullAuthorizationEndpoint() {
            ParResponse response = new ParResponse("urn:test", 60, null);

            assertThatThrownBy(() ->
                    PushedAuthorizationRequest.buildAuthorizationUrl(null, response, "client"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("authorizationEndpoint");
        }

        @Test
        @DisplayName("null parResponse抛出异常")
        void testNullParResponse() {
            assertThatThrownBy(() ->
                    PushedAuthorizationRequest.buildAuthorizationUrl(
                            "https://auth.example.com/authorize", null, "client"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("parResponse");
        }

        @Test
        @DisplayName("null clientId抛出异常")
        void testNullClientIdInBuildUrl() {
            ParResponse response = new ParResponse("urn:test", 60, null);

            assertThatThrownBy(() ->
                    PushedAuthorizationRequest.buildAuthorizationUrl(
                            "https://auth.example.com/authorize", response, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("clientId");
        }
    }
}
