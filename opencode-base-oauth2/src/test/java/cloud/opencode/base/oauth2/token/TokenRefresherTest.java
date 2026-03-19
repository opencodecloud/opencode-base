package cloud.opencode.base.oauth2.token;

import cloud.opencode.base.oauth2.OAuth2Config;
import cloud.opencode.base.oauth2.OAuth2Token;
import cloud.opencode.base.oauth2.exception.OAuth2Exception;
import cloud.opencode.base.oauth2.http.OAuth2HttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * TokenRefresherTest Tests
 * TokenRefresherTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
@DisplayName("TokenRefresher 测试")
class TokenRefresherTest {

    private OAuth2Config config;
    private OAuth2HttpClient httpClient;
    private TokenRefresher refresher;

    @BeforeEach
    void setUp() {
        config = OAuth2Config.builder()
                .clientId("test-client")
                .clientSecret("test-secret")
                .tokenEndpoint("https://auth.example.com/token")
                .refreshThreshold(Duration.ofMinutes(5))
                .build();
        httpClient = new OAuth2HttpClient();
    }

    @AfterEach
    void tearDown() {
        if (refresher != null) {
            refresher.close();
        }
    }

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用config和httpClient创建")
        void testConstructor() {
            refresher = new TokenRefresher(config, httpClient);

            assertThat(refresher).isNotNull();
            assertThat(refresher.isClosed()).isFalse();
        }

        @Test
        @DisplayName("使用自定义阈值创建")
        void testConstructorWithCustomThreshold() {
            Duration threshold = Duration.ofMinutes(10);
            refresher = new TokenRefresher(config, httpClient, threshold);

            assertThat(refresher.refreshThreshold()).isEqualTo(threshold);
        }

        @Test
        @DisplayName("config为null抛出异常")
        void testConfigNull() {
            assertThatThrownBy(() -> new TokenRefresher(null, httpClient))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("httpClient为null抛出异常")
        void testHttpClientNull() {
            assertThatThrownBy(() -> new TokenRefresher(config, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("needsRefresh方法测试")
    class NeedsRefreshTests {

        @Test
        @DisplayName("null token返回false")
        void testNeedsRefreshNullToken() {
            refresher = new TokenRefresher(config, httpClient);
            assertThat(refresher.needsRefresh(null)).isFalse();
        }

        @Test
        @DisplayName("无refresh token返回false")
        void testNeedsRefreshNoRefreshToken() {
            refresher = new TokenRefresher(config, httpClient);
            OAuth2Token token = OAuth2Token.builder()
                    .accessToken("access")
                    .expiresAt(Instant.now().plusSeconds(60))
                    .build();

            assertThat(refresher.needsRefresh(token)).isFalse();
        }

        @Test
        @DisplayName("即将过期且有refresh token返回true")
        void testNeedsRefreshExpiringSoon() {
            refresher = new TokenRefresher(config, httpClient, Duration.ofMinutes(5));
            OAuth2Token token = OAuth2Token.builder()
                    .accessToken("access")
                    .refreshToken("refresh")
                    .expiresAt(Instant.now().plusSeconds(60)) // 1分钟后过期
                    .build();

            assertThat(refresher.needsRefresh(token)).isTrue();
        }

        @Test
        @DisplayName("未即将过期返回false")
        void testNeedsRefreshNotExpiringSoon() {
            refresher = new TokenRefresher(config, httpClient, Duration.ofMinutes(5));
            OAuth2Token token = OAuth2Token.builder()
                    .accessToken("access")
                    .refreshToken("refresh")
                    .expiresAt(Instant.now().plusSeconds(3600)) // 1小时后过期
                    .build();

            assertThat(refresher.needsRefresh(token)).isFalse();
        }
    }

    @Nested
    @DisplayName("refresh方法测试")
    class RefreshTests {

        @Test
        @DisplayName("token为null抛出异常")
        void testRefreshNullToken() {
            refresher = new TokenRefresher(config, httpClient);
            assertThatThrownBy(() -> refresher.refresh(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("无refresh token抛出异常")
        void testRefreshNoRefreshToken() {
            refresher = new TokenRefresher(config, httpClient);
            OAuth2Token token = OAuth2Token.builder()
                    .accessToken("access")
                    .build();

            assertThatThrownBy(() -> refresher.refresh(token))
                    .isInstanceOf(OAuth2Exception.class);
        }
    }

    @Nested
    @DisplayName("refreshAsync方法测试")
    class RefreshAsyncTests {

        @Test
        @DisplayName("token为null抛出异常")
        void testRefreshAsyncNullToken() {
            refresher = new TokenRefresher(config, httpClient);
            assertThatThrownBy(() -> refresher.refreshAsync(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("无refresh token返回失败的Future")
        void testRefreshAsyncNoRefreshToken() {
            refresher = new TokenRefresher(config, httpClient);
            OAuth2Token token = OAuth2Token.builder()
                    .accessToken("access")
                    .build();

            var future = refresher.refreshAsync(token);
            assertThat(future.isCompletedExceptionally()).isTrue();
        }
    }

    @Nested
    @DisplayName("生命周期测试")
    class LifecycleTests {

        @Test
        @DisplayName("close后isClosed返回true")
        void testClose() {
            refresher = new TokenRefresher(config, httpClient);
            assertThat(refresher.isClosed()).isFalse();

            refresher.close();
            assertThat(refresher.isClosed()).isTrue();
        }

        @Test
        @DisplayName("多次close不抛异常")
        void testMultipleClose() {
            refresher = new TokenRefresher(config, httpClient);

            assertThatCode(() -> {
                refresher.close();
                refresher.close();
                refresher.close();
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("refreshThreshold方法测试")
    class RefreshThresholdTests {

        @Test
        @DisplayName("返回配置的阈值")
        void testRefreshThreshold() {
            Duration threshold = Duration.ofMinutes(10);
            refresher = new TokenRefresher(config, httpClient, threshold);

            assertThat(refresher.refreshThreshold()).isEqualTo(threshold);
        }

        @Test
        @DisplayName("默认使用config的阈值")
        void testRefreshThresholdDefault() {
            refresher = new TokenRefresher(config, httpClient);

            assertThat(refresher.refreshThreshold()).isEqualTo(config.refreshThreshold());
        }
    }
}
