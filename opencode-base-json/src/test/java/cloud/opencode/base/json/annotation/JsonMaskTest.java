package cloud.opencode.base.json.annotation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.*;

/**
 * JsonMask 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
@DisplayName("JsonMask 注解测试")
class JsonMaskTest {

    @Nested
    @DisplayName("注解属性测试")
    class AnnotationAttributeTests {

        @Test
        @DisplayName("注解可用于字段")
        void testTargetField() throws NoSuchFieldException {
            Field field = TestClass.class.getDeclaredField("maskedField");
            JsonMask annotation = field.getAnnotation(JsonMask.class);

            assertThat(annotation).isNotNull();
        }

        @Test
        @DisplayName("获取type属性")
        void testTypeAttribute() throws NoSuchFieldException {
            Field field = TestClass.class.getDeclaredField("passwordField");
            JsonMask annotation = field.getAnnotation(JsonMask.class);

            assertThat(annotation.type()).isEqualTo(JsonMask.MaskType.PASSWORD);
        }

        @Test
        @DisplayName("获取pattern属性")
        void testPatternAttribute() throws NoSuchFieldException {
            Field field = TestClass.class.getDeclaredField("customField");
            JsonMask annotation = field.getAnnotation(JsonMask.class);

            assertThat(annotation.pattern()).isEqualTo("(?<=.{2}).(?=.{2})");
        }

        @Test
        @DisplayName("获取maskChar属性")
        void testMaskCharAttribute() throws NoSuchFieldException {
            Field field = TestClass.class.getDeclaredField("customCharField");
            JsonMask annotation = field.getAnnotation(JsonMask.class);

            assertThat(annotation.maskChar()).isEqualTo('#');
        }

        @Test
        @DisplayName("获取prefixLength属性")
        void testPrefixLengthAttribute() throws NoSuchFieldException {
            Field field = TestClass.class.getDeclaredField("prefixSuffixField");
            JsonMask annotation = field.getAnnotation(JsonMask.class);

            assertThat(annotation.prefixLength()).isEqualTo(3);
        }

        @Test
        @DisplayName("获取suffixLength属性")
        void testSuffixLengthAttribute() throws NoSuchFieldException {
            Field field = TestClass.class.getDeclaredField("prefixSuffixField");
            JsonMask annotation = field.getAnnotation(JsonMask.class);

            assertThat(annotation.suffixLength()).isEqualTo(4);
        }

        @Test
        @DisplayName("获取enabled属性")
        void testEnabledAttribute() throws NoSuchFieldException {
            Field field = TestClass.class.getDeclaredField("disabledField");
            JsonMask annotation = field.getAnnotation(JsonMask.class);

            assertThat(annotation.enabled()).isFalse();
        }

        @Test
        @DisplayName("默认值测试")
        void testDefaultValues() throws NoSuchFieldException {
            Field field = TestClass.class.getDeclaredField("maskedField");
            JsonMask annotation = field.getAnnotation(JsonMask.class);

            assertThat(annotation.type()).isEqualTo(JsonMask.MaskType.FULL);
            assertThat(annotation.pattern()).isEmpty();
            assertThat(annotation.maskChar()).isEqualTo('*');
            assertThat(annotation.prefixLength()).isEqualTo(-1);
            assertThat(annotation.suffixLength()).isEqualTo(-1);
            assertThat(annotation.enabled()).isTrue();
        }
    }

    @Nested
    @DisplayName("MaskType枚举测试")
    class MaskTypeEnumTests {

        @Test
        @DisplayName("所有MaskType值存在")
        void testAllMaskTypes() {
            assertThat(JsonMask.MaskType.values())
                .containsExactlyInAnyOrder(
                    JsonMask.MaskType.PASSWORD,
                    JsonMask.MaskType.PHONE,
                    JsonMask.MaskType.ID_CARD,
                    JsonMask.MaskType.EMAIL,
                    JsonMask.MaskType.BANK_CARD,
                    JsonMask.MaskType.NAME,
                    JsonMask.MaskType.ADDRESS,
                    JsonMask.MaskType.CUSTOM,
                    JsonMask.MaskType.FULL
                );
        }

        @Test
        @DisplayName("valueOf返回正确值")
        void testValueOf() {
            assertThat(JsonMask.MaskType.valueOf("PASSWORD")).isEqualTo(JsonMask.MaskType.PASSWORD);
            assertThat(JsonMask.MaskType.valueOf("PHONE")).isEqualTo(JsonMask.MaskType.PHONE);
            assertThat(JsonMask.MaskType.valueOf("ID_CARD")).isEqualTo(JsonMask.MaskType.ID_CARD);
            assertThat(JsonMask.MaskType.valueOf("EMAIL")).isEqualTo(JsonMask.MaskType.EMAIL);
            assertThat(JsonMask.MaskType.valueOf("BANK_CARD")).isEqualTo(JsonMask.MaskType.BANK_CARD);
            assertThat(JsonMask.MaskType.valueOf("NAME")).isEqualTo(JsonMask.MaskType.NAME);
            assertThat(JsonMask.MaskType.valueOf("ADDRESS")).isEqualTo(JsonMask.MaskType.ADDRESS);
            assertThat(JsonMask.MaskType.valueOf("CUSTOM")).isEqualTo(JsonMask.MaskType.CUSTOM);
            assertThat(JsonMask.MaskType.valueOf("FULL")).isEqualTo(JsonMask.MaskType.FULL);
        }

        @Test
        @DisplayName("无效MaskType名抛出异常")
        void testInvalidValueOf() {
            assertThatThrownBy(() -> JsonMask.MaskType.valueOf("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("MaskType名称正确")
        void testMaskTypeNames() {
            assertThat(JsonMask.MaskType.PASSWORD.name()).isEqualTo("PASSWORD");
            assertThat(JsonMask.MaskType.PHONE.name()).isEqualTo("PHONE");
            assertThat(JsonMask.MaskType.ID_CARD.name()).isEqualTo("ID_CARD");
        }

        @Test
        @DisplayName("MaskType序号正确")
        void testMaskTypeOrdinals() {
            assertThat(JsonMask.MaskType.PASSWORD.ordinal()).isEqualTo(0);
            assertThat(JsonMask.MaskType.PHONE.ordinal()).isEqualTo(1);
            assertThat(JsonMask.MaskType.ID_CARD.ordinal()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("注解元数据测试")
    class AnnotationMetadataTests {

        @Test
        @DisplayName("Target为FIELD")
        void testTarget() {
            java.lang.annotation.Target target =
                JsonMask.class.getAnnotation(java.lang.annotation.Target.class);

            assertThat(target.value()).containsExactly(ElementType.FIELD);
        }

        @Test
        @DisplayName("Retention为RUNTIME")
        void testRetention() {
            java.lang.annotation.Retention retention =
                JsonMask.class.getAnnotation(java.lang.annotation.Retention.class);

            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("Documented存在")
        void testDocumented() {
            java.lang.annotation.Documented documented =
                JsonMask.class.getAnnotation(java.lang.annotation.Documented.class);

            assertThat(documented).isNotNull();
        }
    }

    @Nested
    @DisplayName("使用场景测试")
    class UsageScenarioTests {

        @Test
        @DisplayName("密码字段脱敏")
        void testPasswordMask() throws NoSuchFieldException {
            Field field = UserClass.class.getDeclaredField("password");
            JsonMask annotation = field.getAnnotation(JsonMask.class);

            assertThat(annotation.type()).isEqualTo(JsonMask.MaskType.PASSWORD);
        }

        @Test
        @DisplayName("手机号字段脱敏")
        void testPhoneMask() throws NoSuchFieldException {
            Field field = UserClass.class.getDeclaredField("phone");
            JsonMask annotation = field.getAnnotation(JsonMask.class);

            assertThat(annotation.type()).isEqualTo(JsonMask.MaskType.PHONE);
        }

        @Test
        @DisplayName("身份证字段脱敏")
        void testIdCardMask() throws NoSuchFieldException {
            Field field = UserClass.class.getDeclaredField("idCard");
            JsonMask annotation = field.getAnnotation(JsonMask.class);

            assertThat(annotation.type()).isEqualTo(JsonMask.MaskType.ID_CARD);
        }

        @Test
        @DisplayName("邮箱字段脱敏")
        void testEmailMask() throws NoSuchFieldException {
            Field field = UserClass.class.getDeclaredField("email");
            JsonMask annotation = field.getAnnotation(JsonMask.class);

            assertThat(annotation.type()).isEqualTo(JsonMask.MaskType.EMAIL);
        }

        @Test
        @DisplayName("银行卡字段脱敏")
        void testBankCardMask() throws NoSuchFieldException {
            Field field = UserClass.class.getDeclaredField("bankCard");
            JsonMask annotation = field.getAnnotation(JsonMask.class);

            assertThat(annotation.type()).isEqualTo(JsonMask.MaskType.BANK_CARD);
        }

        @Test
        @DisplayName("姓名字段脱敏")
        void testNameMask() throws NoSuchFieldException {
            Field field = UserClass.class.getDeclaredField("realName");
            JsonMask annotation = field.getAnnotation(JsonMask.class);

            assertThat(annotation.type()).isEqualTo(JsonMask.MaskType.NAME);
        }

        @Test
        @DisplayName("地址字段脱敏")
        void testAddressMask() throws NoSuchFieldException {
            Field field = UserClass.class.getDeclaredField("address");
            JsonMask annotation = field.getAnnotation(JsonMask.class);

            assertThat(annotation.type()).isEqualTo(JsonMask.MaskType.ADDRESS);
        }
    }

    // Test helper classes
    static class TestClass {
        @JsonMask
        String maskedField;

        @JsonMask(type = JsonMask.MaskType.PASSWORD)
        String passwordField;

        @JsonMask(type = JsonMask.MaskType.CUSTOM, pattern = "(?<=.{2}).(?=.{2})")
        String customField;

        @JsonMask(maskChar = '#')
        String customCharField;

        @JsonMask(prefixLength = 3, suffixLength = 4)
        String prefixSuffixField;

        @JsonMask(enabled = false)
        String disabledField;
    }

    static class UserClass {
        @JsonMask(type = JsonMask.MaskType.PASSWORD)
        String password;

        @JsonMask(type = JsonMask.MaskType.PHONE)
        String phone;

        @JsonMask(type = JsonMask.MaskType.ID_CARD)
        String idCard;

        @JsonMask(type = JsonMask.MaskType.EMAIL)
        String email;

        @JsonMask(type = JsonMask.MaskType.BANK_CARD)
        String bankCard;

        @JsonMask(type = JsonMask.MaskType.NAME)
        String realName;

        @JsonMask(type = JsonMask.MaskType.ADDRESS)
        String address;
    }
}
