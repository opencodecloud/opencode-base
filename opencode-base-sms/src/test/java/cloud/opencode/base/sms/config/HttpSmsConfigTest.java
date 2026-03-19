package cloud.opencode.base.sms.config;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * HttpSmsConfigTest Tests
 * HttpSmsConfigTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
@DisplayName("HttpSmsConfig 测试")
class HttpSmsConfigTest {

    @Nested
    @DisplayName("Builder测试")
    class BuilderTests {

        @Test
        @DisplayName("构建完整配置")
        void testBuilderFull() {
            HttpSmsConfig config = HttpSmsConfig.builder()
                    .name("custom")
                    .apiUrl("https://api.example.com/sms")
                    .appId("app123")
                    .appKey("key456")
                    .signName("TestSign")
                    .extra("customKey", "customValue")
                    .build();

            assertThat(config.name()).isEqualTo("custom");
            assertThat(config.apiUrl()).isEqualTo("https://api.example.com/sms");
            assertThat(config.appId()).isEqualTo("app123");
            assertThat(config.appKey()).isEqualTo("key456");
            assertThat(config.signName()).isEqualTo("TestSign");
            assertThat(config.extra()).containsEntry("customKey", "customValue");
        }

        @Test
        @DisplayName("默认名称为http")
        void testBuilderDefaultName() {
            HttpSmsConfig config = HttpSmsConfig.builder()
                    .apiUrl("https://api.example.com")
                    .build();

            assertThat(config.name()).isEqualTo("http");
        }

        @Test
        @DisplayName("多个extra配置")
        void testBuilderMultipleExtra() {
            HttpSmsConfig config = HttpSmsConfig.builder()
                    .apiUrl("https://api.example.com")
                    .extra("key1", "value1")
                    .extra("key2", "value2")
                    .build();

            assertThat(config.extra())
                    .containsEntry("key1", "value1")
                    .containsEntry("key2", "value2");
        }
    }

    @Nested
    @DisplayName("isConfigured方法测试")
    class IsConfiguredTests {

        @Test
        @DisplayName("有apiUrl返回true")
        void testIsConfiguredWithApiUrl() {
            HttpSmsConfig config = HttpSmsConfig.builder()
                    .apiUrl("https://api.example.com")
                    .build();

            assertThat(config.isConfigured()).isTrue();
        }

        @Test
        @DisplayName("无apiUrl返回false")
        void testIsConfiguredWithoutApiUrl() {
            HttpSmsConfig config = HttpSmsConfig.builder()
                    .appId("app123")
                    .build();

            assertThat(config.isConfigured()).isFalse();
        }

        @Test
        @DisplayName("空apiUrl返回false")
        void testIsConfiguredWithEmptyApiUrl() {
            HttpSmsConfig config = HttpSmsConfig.builder()
                    .apiUrl("")
                    .build();

            assertThat(config.isConfigured()).isFalse();
        }
    }

    @Nested
    @DisplayName("Record方法测试")
    class RecordTests {

        @Test
        @DisplayName("equals和hashCode")
        void testEqualsAndHashCode() {
            HttpSmsConfig c1 = HttpSmsConfig.builder()
                    .name("test")
                    .apiUrl("https://api.example.com")
                    .build();
            HttpSmsConfig c2 = HttpSmsConfig.builder()
                    .name("test")
                    .apiUrl("https://api.example.com")
                    .build();

            assertThat(c1).isEqualTo(c2);
            assertThat(c1.hashCode()).isEqualTo(c2.hashCode());
        }

        @Test
        @DisplayName("toString包含关键信息")
        void testToString() {
            HttpSmsConfig config = HttpSmsConfig.builder()
                    .name("test")
                    .apiUrl("https://api.example.com")
                    .build();

            assertThat(config.toString()).contains("test");
            assertThat(config.toString()).contains("api.example.com");
        }
    }
}
