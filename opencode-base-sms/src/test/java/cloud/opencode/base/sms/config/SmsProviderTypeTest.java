package cloud.opencode.base.sms.config;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * SmsProviderTypeTest Tests
 * SmsProviderTypeTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
@DisplayName("SmsProviderType 测试")
class SmsProviderTypeTest {

    @Nested
    @DisplayName("枚举值测试")
    class EnumValuesTests {

        @Test
        @DisplayName("包含所有提供商类型")
        void testAllProviderTypes() {
            SmsProviderType[] types = SmsProviderType.values();

            assertThat(types).contains(
                    SmsProviderType.ALIYUN,
                    SmsProviderType.TENCENT,
                    SmsProviderType.HUAWEI,
                    SmsProviderType.BAIDU,
                    SmsProviderType.TWILIO,
                    SmsProviderType.AWS_SNS,
                    SmsProviderType.CONSOLE,
                    SmsProviderType.CUSTOM
            );
        }

        @Test
        @DisplayName("valueOf返回正确的枚举")
        void testValueOf() {
            assertThat(SmsProviderType.valueOf("ALIYUN")).isEqualTo(SmsProviderType.ALIYUN);
            assertThat(SmsProviderType.valueOf("TENCENT")).isEqualTo(SmsProviderType.TENCENT);
            assertThat(SmsProviderType.valueOf("CONSOLE")).isEqualTo(SmsProviderType.CONSOLE);
        }
    }

    @Nested
    @DisplayName("getCode方法测试")
    class GetCodeTests {

        @Test
        @DisplayName("返回正确的代码")
        void testGetCode() {
            assertThat(SmsProviderType.ALIYUN.getCode()).isEqualTo("aliyun");
            assertThat(SmsProviderType.TENCENT.getCode()).isEqualTo("tencent");
            assertThat(SmsProviderType.HUAWEI.getCode()).isEqualTo("huawei");
            assertThat(SmsProviderType.CONSOLE.getCode()).isEqualTo("console");
        }
    }

    @Nested
    @DisplayName("fromCode方法测试")
    class FromCodeTests {

        @Test
        @DisplayName("小写代码返回正确枚举")
        void testFromCodeLowercase() {
            assertThat(SmsProviderType.fromCode("aliyun")).isEqualTo(SmsProviderType.ALIYUN);
            assertThat(SmsProviderType.fromCode("tencent")).isEqualTo(SmsProviderType.TENCENT);
        }

        @Test
        @DisplayName("大写代码返回正确枚举")
        void testFromCodeUppercase() {
            assertThat(SmsProviderType.fromCode("ALIYUN")).isEqualTo(SmsProviderType.ALIYUN);
        }

        @Test
        @DisplayName("混合大小写返回正确枚举")
        void testFromCodeMixedCase() {
            assertThat(SmsProviderType.fromCode("Aliyun")).isEqualTo(SmsProviderType.ALIYUN);
        }

        @Test
        @DisplayName("无效代码抛出异常")
        void testFromCodeInvalid() {
            assertThatThrownBy(() -> SmsProviderType.fromCode("invalid"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null代码抛出异常")
        void testFromCodeNull() {
            assertThatThrownBy(() -> SmsProviderType.fromCode(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
