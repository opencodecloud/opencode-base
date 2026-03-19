package cloud.opencode.base.oauth2.http;

import cloud.opencode.base.oauth2.OAuth2Config;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * HttpClientFactoryTest Tests
 * HttpClientFactoryTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
@DisplayName("HttpClientFactory 测试")
class HttpClientFactoryTest {

    @Nested
    @DisplayName("工具类测试")
    class UtilityClassTests {

        @Test
        @DisplayName("类是final的")
        void testFinalClass() {
            assertThat(java.lang.reflect.Modifier.isFinal(HttpClientFactory.class.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("构造函数私有")
        void testPrivateConstructor() throws NoSuchMethodException {
            var constructor = HttpClientFactory.class.getDeclaredConstructor();
            assertThat(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("常量测试")
    class ConstantsTests {

        @Test
        @DisplayName("DEFAULT_CONNECT_TIMEOUT")
        void testDefaultConnectTimeout() {
            assertThat(HttpClientFactory.DEFAULT_CONNECT_TIMEOUT).isEqualTo(Duration.ofSeconds(10));
        }

        @Test
        @DisplayName("DEFAULT_READ_TIMEOUT")
        void testDefaultReadTimeout() {
            assertThat(HttpClientFactory.DEFAULT_READ_TIMEOUT).isEqualTo(Duration.ofSeconds(30));
        }
    }

    @Nested
    @DisplayName("create方法测试")
    class CreateTests {

        @Test
        @DisplayName("create()使用默认设置")
        void testCreate() {
            OAuth2HttpClient client = HttpClientFactory.create();
            assertThat(client).isNotNull();
            client.close();
        }

        @Test
        @DisplayName("create(Duration, Duration)使用自定义超时")
        void testCreateWithTimeouts() {
            OAuth2HttpClient client = HttpClientFactory.create(
                    Duration.ofSeconds(5),
                    Duration.ofSeconds(15)
            );
            assertThat(client).isNotNull();
            client.close();
        }

        @Test
        @DisplayName("create(Duration, Duration) null使用默认值")
        void testCreateWithNullTimeouts() {
            OAuth2HttpClient client = HttpClientFactory.create(null, null);
            assertThat(client).isNotNull();
            client.close();
        }

        @Test
        @DisplayName("create(OAuth2Config)从配置创建")
        void testCreateFromConfig() {
            OAuth2Config config = OAuth2Config.builder()
                    .clientId("test-client")
                    .connectTimeout(Duration.ofSeconds(5))
                    .readTimeout(Duration.ofSeconds(15))
                    .build();

            OAuth2HttpClient client = HttpClientFactory.create(config);
            assertThat(client).isNotNull();
            client.close();
        }

        @Test
        @DisplayName("create(OAuth2Config) config为null抛出异常")
        void testCreateFromConfigNull() {
            assertThatThrownBy(() -> HttpClientFactory.create((OAuth2Config) null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("shared方法测试")
    class SharedTests {

        @Test
        @DisplayName("shared返回单例")
        void testSharedSingleton() {
            OAuth2HttpClient client1 = HttpClientFactory.shared();
            OAuth2HttpClient client2 = HttpClientFactory.shared();

            assertThat(client1).isSameAs(client2);
        }

        @Test
        @DisplayName("shared返回非null")
        void testSharedNotNull() {
            OAuth2HttpClient client = HttpClientFactory.shared();
            assertThat(client).isNotNull();
        }
    }

    @Nested
    @DisplayName("Builder测试")
    class BuilderTests {

        @Test
        @DisplayName("builder创建构建器")
        void testBuilder() {
            HttpClientFactory.Builder builder = HttpClientFactory.builder();
            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("builder设置connectTimeout")
        void testBuilderConnectTimeout() {
            OAuth2HttpClient client = HttpClientFactory.builder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();
            assertThat(client).isNotNull();
            client.close();
        }

        @Test
        @DisplayName("builder设置readTimeout")
        void testBuilderReadTimeout() {
            OAuth2HttpClient client = HttpClientFactory.builder()
                    .readTimeout(Duration.ofSeconds(15))
                    .build();
            assertThat(client).isNotNull();
            client.close();
        }

        @Test
        @DisplayName("builder使用秒设置超时")
        void testBuilderTimeoutSeconds() {
            OAuth2HttpClient client = HttpClientFactory.builder()
                    .connectTimeoutSeconds(5)
                    .readTimeoutSeconds(15)
                    .build();
            assertThat(client).isNotNull();
            client.close();
        }

        @Test
        @DisplayName("builder链式调用")
        void testBuilderChaining() {
            OAuth2HttpClient client = HttpClientFactory.builder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .readTimeout(Duration.ofSeconds(15))
                    .connectTimeoutSeconds(10)
                    .readTimeoutSeconds(30)
                    .build();
            assertThat(client).isNotNull();
            client.close();
        }
    }
}
