package cloud.opencode.base.sms.config;

import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * ProviderConfigTest Tests
 * ProviderConfigTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
@DisplayName("ProviderConfig 测试")
class ProviderConfigTest {

    @Nested
    @DisplayName("构造函数验证测试")
    class ConstructorValidationTests {

        @Test
        @DisplayName("null name抛出异常")
        void testNullNameThrows() {
            assertThatThrownBy(() -> ProviderConfig.builder().build())
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("name");
        }

        @Test
        @DisplayName("默认超时设置")
        void testDefaultTimeouts() {
            ProviderConfig config = ProviderConfig.builder()
                    .name("test")
                    .build();

            assertThat(config.timeout()).isEqualTo(Duration.ofSeconds(30));
            assertThat(config.connectTimeout()).isEqualTo(Duration.ofSeconds(10));
        }

        @Test
        @DisplayName("默认重试次数")
        void testDefaultMaxRetries() {
            ProviderConfig config = ProviderConfig.builder()
                    .name("test")
                    .build();

            assertThat(config.maxRetries()).isEqualTo(3);
        }

        @Test
        @DisplayName("负重试次数使用默认值")
        void testNegativeMaxRetriesUsesDefault() {
            ProviderConfig config = ProviderConfig.builder()
                    .name("test")
                    .maxRetries(-1)
                    .build();

            assertThat(config.maxRetries()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Builder测试")
    class BuilderTests {

        @Test
        @DisplayName("构建完整配置")
        void testBuilderFull() {
            ProviderConfig config = ProviderConfig.builder()
                    .name("aliyun")
                    .endpoint("dysmsapi.aliyuncs.com")
                    .apiVersion("2017-05-25")
                    .region("cn-hangzhou")
                    .timeout(Duration.ofSeconds(60))
                    .connectTimeout(Duration.ofSeconds(5))
                    .maxRetries(5)
                    .sdkAppId("1400000000")
                    .extraOptions(Map.of("key", "value"))
                    .build();

            assertThat(config.name()).isEqualTo("aliyun");
            assertThat(config.endpoint()).isEqualTo("dysmsapi.aliyuncs.com");
            assertThat(config.apiVersion()).isEqualTo("2017-05-25");
            assertThat(config.region()).isEqualTo("cn-hangzhou");
            assertThat(config.timeout()).isEqualTo(Duration.ofSeconds(60));
            assertThat(config.connectTimeout()).isEqualTo(Duration.ofSeconds(5));
            assertThat(config.maxRetries()).isEqualTo(5);
            assertThat(config.sdkAppId()).isEqualTo("1400000000");
        }

        @Test
        @DisplayName("option方法添加单个选项")
        void testBuilderOption() {
            ProviderConfig config = ProviderConfig.builder()
                    .name("test")
                    .option("key1", "value1")
                    .option("key2", "value2")
                    .build();

            assertThat(config.extraOptions())
                    .containsEntry("key1", "value1")
                    .containsEntry("key2", "value2");
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("aliyun工厂方法")
        void testAliyunFactory() {
            ProviderConfig config = ProviderConfig.aliyun("cn-hangzhou");

            assertThat(config.name()).isEqualTo("aliyun");
            assertThat(config.endpoint()).isEqualTo("dysmsapi.aliyuncs.com");
            assertThat(config.apiVersion()).isEqualTo("2017-05-25");
            assertThat(config.region()).isEqualTo("cn-hangzhou");
        }

        @Test
        @DisplayName("tencent工厂方法")
        void testTencentFactory() {
            ProviderConfig config = ProviderConfig.tencent("1400000000");

            assertThat(config.name()).isEqualTo("tencent");
            assertThat(config.endpoint()).isEqualTo("sms.tencentcloudapi.com");
            assertThat(config.sdkAppId()).isEqualTo("1400000000");
        }

        @Test
        @DisplayName("huawei工厂方法")
        void testHuaweiFactory() {
            ProviderConfig config = ProviderConfig.huawei("cn-north-4");

            assertThat(config.name()).isEqualTo("huawei");
            assertThat(config.endpoint()).contains("cn-north-4");
            assertThat(config.region()).isEqualTo("cn-north-4");
        }
    }

    @Nested
    @DisplayName("getOption方法测试")
    class GetOptionTests {

        @Test
        @DisplayName("返回存在的选项")
        void testGetOptionExists() {
            ProviderConfig config = ProviderConfig.builder()
                    .name("test")
                    .option("key", "value")
                    .build();

            assertThat(config.getOption("key")).isEqualTo("value");
        }

        @Test
        @DisplayName("不存在的选项返回null")
        void testGetOptionNotExists() {
            ProviderConfig config = ProviderConfig.builder()
                    .name("test")
                    .build();

            assertThat(config.getOption("nonexistent")).isNull();
        }

        @Test
        @DisplayName("带默认值获取选项")
        void testGetOptionWithDefault() {
            ProviderConfig config = ProviderConfig.builder()
                    .name("test")
                    .build();

            assertThat(config.getOption("nonexistent", "default")).isEqualTo("default");
        }
    }
}
