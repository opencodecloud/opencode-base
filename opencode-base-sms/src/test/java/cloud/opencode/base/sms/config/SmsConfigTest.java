package cloud.opencode.base.sms.config;

import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * SmsConfigTest Tests
 * SmsConfigTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
@DisplayName("SmsConfig 测试")
class SmsConfigTest {

    @Nested
    @DisplayName("Builder测试")
    class BuilderTests {

        @Test
        @DisplayName("构建完整配置")
        void testBuilderFull() {
            SmsConfig config = SmsConfig.builder()
                    .providerType(SmsProviderType.ALIYUN)
                    .accessKey("ak123")
                    .secretKey("sk123")
                    .signName("TestSign")
                    .region("cn-hangzhou")
                    .timeout(Duration.ofSeconds(30))
                    .build();

            assertThat(config.providerType()).isEqualTo(SmsProviderType.ALIYUN);
            assertThat(config.accessKey()).isEqualTo("ak123");
            assertThat(config.secretKey()).isEqualTo("sk123");
            assertThat(config.signName()).isEqualTo("TestSign");
            assertThat(config.region()).isEqualTo("cn-hangzhou");
            assertThat(config.timeout()).isEqualTo(Duration.ofSeconds(30));
        }

        @Test
        @DisplayName("extraProperties批量设置")
        void testBuilderExtraPropertiesMap() {
            Map<String, String> props = Map.of("key1", "value1", "key2", "value2");
            SmsConfig config = SmsConfig.builder()
                    .providerType(SmsProviderType.CONSOLE)
                    .accessKey("ak")
                    .secretKey("sk")
                    .signName("sign")
                    .extraProperties(props)
                    .build();

            assertThat(config.extraProperties()).hasSize(2);
            assertThat(config.extraProperties()).containsEntry("key1", "value1");
            assertThat(config.extraProperties()).containsEntry("key2", "value2");
        }

        @Test
        @DisplayName("默认超时30秒")
        void testBuilderDefaultTimeout() {
            SmsConfig config = SmsConfig.builder()
                    .providerType(SmsProviderType.CONSOLE)
                    .accessKey("ak")
                    .secretKey("sk")
                    .signName("sign")
                    .build();

            assertThat(config.timeout()).isEqualTo(Duration.ofSeconds(30));
        }

        @Test
        @DisplayName("默认extraProperties为空Map")
        void testBuilderDefaultExtraProperties() {
            SmsConfig config = SmsConfig.builder()
                    .providerType(SmsProviderType.CONSOLE)
                    .accessKey("ak")
                    .secretKey("sk")
                    .signName("sign")
                    .build();

            assertThat(config.extraProperties()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Record方法测试")
    class RecordTests {

        @Test
        @DisplayName("equals和hashCode")
        void testEqualsAndHashCode() {
            SmsConfig c1 = SmsConfig.builder()
                    .providerType(SmsProviderType.ALIYUN)
                    .accessKey("ak")
                    .secretKey("sk")
                    .signName("sign")
                    .build();
            SmsConfig c2 = SmsConfig.builder()
                    .providerType(SmsProviderType.ALIYUN)
                    .accessKey("ak")
                    .secretKey("sk")
                    .signName("sign")
                    .build();

            assertThat(c1).isEqualTo(c2);
            assertThat(c1.hashCode()).isEqualTo(c2.hashCode());
        }

        @Test
        @DisplayName("不同配置不相等")
        void testNotEquals() {
            SmsConfig c1 = SmsConfig.builder()
                    .providerType(SmsProviderType.ALIYUN)
                    .accessKey("ak1")
                    .secretKey("sk")
                    .signName("sign")
                    .build();
            SmsConfig c2 = SmsConfig.builder()
                    .providerType(SmsProviderType.ALIYUN)
                    .accessKey("ak2")
                    .secretKey("sk")
                    .signName("sign")
                    .build();

            assertThat(c1).isNotEqualTo(c2);
        }

        @Test
        @DisplayName("toString包含providerType")
        void testToString() {
            SmsConfig config = SmsConfig.builder()
                    .providerType(SmsProviderType.ALIYUN)
                    .accessKey("mysecretkey123")
                    .secretKey("topsecret456")
                    .signName("sign")
                    .build();
            String str = config.toString();

            assertThat(str).contains("ALIYUN");
        }
    }

    @Nested
    @DisplayName("Record紧凑构造函数测试")
    class CompactConstructorTests {

        @Test
        @DisplayName("extraProperties不可变")
        void testExtraPropertiesImmutable() {
            SmsConfig config = new SmsConfig(
                    SmsProviderType.CONSOLE,
                    "ak", "sk", "sign", "region",
                    Duration.ofSeconds(30),
                    Map.of("key", "value")
            );

            assertThatThrownBy(() -> config.extraProperties().put("new", "value"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("null extraProperties变为空Map")
        void testNullExtraPropertiesBecomesEmpty() {
            SmsConfig config = new SmsConfig(
                    SmsProviderType.CONSOLE,
                    "ak", "sk", "sign", "region",
                    Duration.ofSeconds(30),
                    null
            );

            assertThat(config.extraProperties()).isEmpty();
        }

        @Test
        @DisplayName("null timeout使用默认值")
        void testNullTimeoutUsesDefault() {
            SmsConfig config = new SmsConfig(
                    SmsProviderType.CONSOLE,
                    "ak", "sk", "sign", "region",
                    null,
                    Map.of()
            );

            assertThat(config.timeout()).isEqualTo(Duration.ofSeconds(30));
        }
    }
}
