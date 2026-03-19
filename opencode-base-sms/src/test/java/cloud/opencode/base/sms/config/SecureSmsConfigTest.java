package cloud.opencode.base.sms.config;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * SecureSmsConfigTest Tests
 * SecureSmsConfigTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
@DisplayName("SecureSmsConfig 测试")
class SecureSmsConfigTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("从HttpSmsConfig创建")
        void testCreateFromHttpConfig() {
            HttpSmsConfig httpConfig = HttpSmsConfig.builder()
                    .name("test")
                    .apiUrl("https://api.example.com")
                    .appId("app123")
                    .appKey("key456")
                    .signName("TestSign")
                    .build();

            SecureSmsConfig secureConfig = new SecureSmsConfig(httpConfig);

            assertThat(secureConfig.getName()).isEqualTo("test");
            assertThat(secureConfig.getApiUrl()).isEqualTo("https://api.example.com");
            assertThat(secureConfig.getAppId()).isEqualTo("app123");
            assertThat(secureConfig.getAppKey()).isEqualTo("key456");
            assertThat(secureConfig.getSignName()).isEqualTo("TestSign");
        }

        @Test
        @DisplayName("null appId处理")
        void testNullAppId() {
            HttpSmsConfig httpConfig = HttpSmsConfig.builder()
                    .name("test")
                    .apiUrl("https://api.example.com")
                    .build();

            SecureSmsConfig secureConfig = new SecureSmsConfig(httpConfig);

            assertThat(secureConfig.getAppId()).isNull();
            assertThat(secureConfig.getAppKey()).isNull();
        }
    }

    @Nested
    @DisplayName("clear方法测试")
    class ClearTests {

        @Test
        @DisplayName("清除后访问敏感数据抛出异常")
        void testClearThenAccessThrows() {
            HttpSmsConfig httpConfig = HttpSmsConfig.builder()
                    .name("test")
                    .apiUrl("https://api.example.com")
                    .appId("app123")
                    .appKey("key456")
                    .build();

            SecureSmsConfig secureConfig = new SecureSmsConfig(httpConfig);
            secureConfig.clear();

            assertThatThrownBy(secureConfig::getAppId)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("cleared");
            assertThatThrownBy(secureConfig::getAppKey)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("cleared");
        }

        @Test
        @DisplayName("清除后非敏感数据仍可访问")
        void testClearDoesNotAffectNonSensitive() {
            HttpSmsConfig httpConfig = HttpSmsConfig.builder()
                    .name("test")
                    .apiUrl("https://api.example.com")
                    .appId("app123")
                    .signName("TestSign")
                    .build();

            SecureSmsConfig secureConfig = new SecureSmsConfig(httpConfig);
            secureConfig.clear();

            assertThat(secureConfig.getName()).isEqualTo("test");
            assertThat(secureConfig.getApiUrl()).isEqualTo("https://api.example.com");
            assertThat(secureConfig.getSignName()).isEqualTo("TestSign");
        }

        @Test
        @DisplayName("多次clear不抛出异常")
        void testMultipleClearSafe() {
            HttpSmsConfig httpConfig = HttpSmsConfig.builder()
                    .name("test")
                    .apiUrl("https://api.example.com")
                    .appId("app123")
                    .build();

            SecureSmsConfig secureConfig = new SecureSmsConfig(httpConfig);
            secureConfig.clear();
            secureConfig.clear();

            assertThatNoException().isThrownBy(secureConfig::clear);
        }
    }

    @Nested
    @DisplayName("AutoCloseable测试")
    class AutoCloseableTests {

        @Test
        @DisplayName("close调用clear")
        void testCloseCallsClear() {
            HttpSmsConfig httpConfig = HttpSmsConfig.builder()
                    .name("test")
                    .apiUrl("https://api.example.com")
                    .appId("app123")
                    .build();

            SecureSmsConfig secureConfig = new SecureSmsConfig(httpConfig);
            secureConfig.close();

            assertThatThrownBy(secureConfig::getAppId)
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("try-with-resources自动清除")
        void testTryWithResources() {
            HttpSmsConfig httpConfig = HttpSmsConfig.builder()
                    .name("test")
                    .apiUrl("https://api.example.com")
                    .appId("app123")
                    .build();

            SecureSmsConfig secureConfig;
            try (SecureSmsConfig config = new SecureSmsConfig(httpConfig)) {
                secureConfig = config;
                assertThat(config.getAppId()).isEqualTo("app123");
            }

            assertThatThrownBy(secureConfig::getAppId)
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("不暴露敏感信息")
        void testToStringDoesNotExposeSensitive() {
            HttpSmsConfig httpConfig = HttpSmsConfig.builder()
                    .name("test")
                    .apiUrl("https://api.example.com")
                    .appId("supersecretappid")
                    .appKey("topsecretkey")
                    .signName("TestSign")
                    .build();

            SecureSmsConfig secureConfig = new SecureSmsConfig(httpConfig);
            String str = secureConfig.toString();

            assertThat(str).contains("test");
            assertThat(str).contains("api.example.com");
            assertThat(str).contains("TestSign");
            assertThat(str).doesNotContain("supersecretappid");
            assertThat(str).doesNotContain("topsecretkey");
        }
    }
}
