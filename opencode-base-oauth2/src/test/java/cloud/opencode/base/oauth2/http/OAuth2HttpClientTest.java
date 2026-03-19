package cloud.opencode.base.oauth2.http;

import cloud.opencode.base.oauth2.OAuth2Config;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * OAuth2HttpClientTest Tests
 * OAuth2HttpClientTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
@DisplayName("OAuth2HttpClient 测试")
class OAuth2HttpClientTest {

    private OAuth2HttpClient client;

    @AfterEach
    void tearDown() {
        if (client != null) {
            client.close();
        }
    }

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("默认构造函数")
        void testDefaultConstructor() {
            client = new OAuth2HttpClient();
            assertThat(client).isNotNull();
        }

        @Test
        @DisplayName("自定义超时构造函数")
        void testCustomTimeoutsConstructor() {
            client = new OAuth2HttpClient(Duration.ofSeconds(5), Duration.ofSeconds(10));
            assertThat(client).isNotNull();
        }

        @Test
        @DisplayName("从config创建")
        void testConfigConstructor() {
            OAuth2Config config = OAuth2Config.builder()
                    .clientId("test-client")
                    .connectTimeout(Duration.ofSeconds(5))
                    .readTimeout(Duration.ofSeconds(10))
                    .build();

            client = new OAuth2HttpClient(config);
            assertThat(client).isNotNull();
        }
    }

    @Nested
    @DisplayName("AutoCloseable测试")
    class AutoCloseableTests {

        @Test
        @DisplayName("close不抛异常")
        void testClose() {
            client = new OAuth2HttpClient();
            assertThatCode(() -> client.close()).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("多次close不抛异常")
        void testMultipleClose() {
            client = new OAuth2HttpClient();
            assertThatCode(() -> {
                client.close();
                client.close();
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("方法签名测试")
    class MethodSignatureTests {

        @Test
        @DisplayName("postForm方法存在")
        void testPostFormMethodExists() throws NoSuchMethodException {
            assertThat(OAuth2HttpClient.class.getMethod("postForm", String.class, java.util.Map.class))
                    .isNotNull();
        }

        @Test
        @DisplayName("postForm带headers方法存在")
        void testPostFormWithHeadersMethodExists() throws NoSuchMethodException {
            assertThat(OAuth2HttpClient.class.getMethod("postForm", String.class, java.util.Map.class, java.util.Map.class))
                    .isNotNull();
        }

        @Test
        @DisplayName("get方法存在")
        void testGetMethodExists() throws NoSuchMethodException {
            assertThat(OAuth2HttpClient.class.getMethod("get", String.class, java.util.Map.class))
                    .isNotNull();
        }
    }
}
