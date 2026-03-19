package cloud.opencode.base.log.enhance;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * LogMasking 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
@DisplayName("LogMasking 测试")
class LogMaskingTest {

    @BeforeEach
    void setUp() {
        LogMasking.clearRules();
    }

    @AfterEach
    void tearDown() {
        LogMasking.clearRules();
    }

    @Nested
    @DisplayName("类定义测试")
    class ClassDefinitionTests {

        @Test
        @DisplayName("类是final的")
        void testIsFinal() {
            assertThat(java.lang.reflect.Modifier.isFinal(LogMasking.class.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws NoSuchMethodException {
            var constructor = LogMasking.class.getDeclaredConstructor();
            assertThat(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("registerRule方法测试")
    class RegisterRuleTests {

        @Test
        @DisplayName("注册脱敏规则")
        void testRegisterRule() {
            LogMasking.registerRule("password", LogMasking.MaskingStrategy.PASSWORD);

            assertThat(LogMasking.shouldMask("password")).isTrue();
        }

        @Test
        @DisplayName("字段名不区分大小写")
        void testRegisterRuleCaseInsensitive() {
            LogMasking.registerRule("PASSWORD", LogMasking.MaskingStrategy.PASSWORD);

            assertThat(LogMasking.shouldMask("password")).isTrue();
            assertThat(LogMasking.shouldMask("Password")).isTrue();
        }
    }

    @Nested
    @DisplayName("registerPatternRule方法测试")
    class RegisterPatternRuleTests {

        @Test
        @DisplayName("注册模式规则")
        void testRegisterPatternRule() {
            assertThatCode(() ->
                LogMasking.registerPatternRule("\\d{11}", s -> "***")
            ).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("clearRules方法测试")
    class ClearRulesTests {

        @Test
        @DisplayName("清除所有规则")
        void testClearRules() {
            LogMasking.registerRule("test", LogMasking.MaskingStrategy.FULL);
            LogMasking.clearRules();

            assertThat(LogMasking.shouldMask("test")).isFalse();
        }
    }

    @Nested
    @DisplayName("mask方法测试")
    class MaskTests {

        @Test
        @DisplayName("FULL策略")
        void testMaskFull() {
            String masked = LogMasking.mask("sensitive", LogMasking.MaskingStrategy.FULL);
            assertThat(masked).isEqualTo("******");
        }

        @Test
        @DisplayName("PHONE策略")
        void testMaskPhone() {
            String masked = LogMasking.mask("13812345678", LogMasking.MaskingStrategy.PHONE);
            assertThat(masked).isEqualTo("138****5678");
        }

        @Test
        @DisplayName("PHONE策略短号码")
        void testMaskPhoneShort() {
            String masked = LogMasking.mask("123456", LogMasking.MaskingStrategy.PHONE);
            assertThat(masked).isEqualTo("******");
        }

        @Test
        @DisplayName("EMAIL策略")
        void testMaskEmail() {
            String masked = LogMasking.mask("user@example.com", LogMasking.MaskingStrategy.EMAIL);
            assertThat(masked).isEqualTo("u***@example.com");
        }

        @Test
        @DisplayName("EMAIL策略短名称")
        void testMaskEmailShort() {
            String masked = LogMasking.mask("a@b.com", LogMasking.MaskingStrategy.EMAIL);
            assertThat(masked).isEqualTo("******");
        }

        @Test
        @DisplayName("ID_CARD策略")
        void testMaskIdCard() {
            String masked = LogMasking.mask("110101199001011234", LogMasking.MaskingStrategy.ID_CARD);
            assertThat(masked).isEqualTo("110***********1234");
        }

        @Test
        @DisplayName("ID_CARD策略短号")
        void testMaskIdCardShort() {
            String masked = LogMasking.mask("1234567", LogMasking.MaskingStrategy.ID_CARD);
            assertThat(masked).isEqualTo("******");
        }

        @Test
        @DisplayName("BANK_CARD策略")
        void testMaskBankCard() {
            String masked = LogMasking.mask("6222021234567890123", LogMasking.MaskingStrategy.BANK_CARD);
            assertThat(masked).isEqualTo("************0123");
        }

        @Test
        @DisplayName("BANK_CARD策略短号")
        void testMaskBankCardShort() {
            String masked = LogMasking.mask("123", LogMasking.MaskingStrategy.BANK_CARD);
            assertThat(masked).isEqualTo("******");
        }

        @Test
        @DisplayName("PASSWORD策略")
        void testMaskPassword() {
            String masked = LogMasking.mask("mypassword", LogMasking.MaskingStrategy.PASSWORD);
            assertThat(masked).isEqualTo("[PROTECTED]");
        }

        @Test
        @DisplayName("NAME策略两字名")
        void testMaskNameTwoChars() {
            String masked = LogMasking.mask("张三", LogMasking.MaskingStrategy.NAME);
            assertThat(masked).isEqualTo("张*");
        }

        @Test
        @DisplayName("NAME策略三字名")
        void testMaskNameThreeChars() {
            String masked = LogMasking.mask("张小三", LogMasking.MaskingStrategy.NAME);
            assertThat(masked).isEqualTo("张*三");
        }

        @Test
        @DisplayName("NAME策略单字名")
        void testMaskNameSingleChar() {
            String masked = LogMasking.mask("张", LogMasking.MaskingStrategy.NAME);
            assertThat(masked).isEqualTo("*");
        }

        @Test
        @DisplayName("ADDRESS策略")
        void testMaskAddress() {
            String masked = LogMasking.mask("北京市朝阳区某某路123号", LogMasking.MaskingStrategy.ADDRESS);
            assertThat(masked).isEqualTo("北京市朝阳区****");
        }

        @Test
        @DisplayName("ADDRESS策略短地址")
        void testMaskAddressShort() {
            String masked = LogMasking.mask("北京", LogMasking.MaskingStrategy.ADDRESS);
            assertThat(masked).isEqualTo("******");
        }

        @Test
        @DisplayName("CUSTOM策略不做修改")
        void testMaskCustom() {
            String masked = LogMasking.mask("value", LogMasking.MaskingStrategy.CUSTOM);
            assertThat(masked).isEqualTo("value");
        }

        @Test
        @DisplayName("null值返回null")
        void testMaskNull() {
            String masked = LogMasking.mask(null, LogMasking.MaskingStrategy.FULL);
            assertThat(masked).isNull();
        }

        @Test
        @DisplayName("空字符串返回空字符串")
        void testMaskEmpty() {
            String masked = LogMasking.mask("", LogMasking.MaskingStrategy.FULL);
            assertThat(masked).isEmpty();
        }
    }

    @Nested
    @DisplayName("maskByField方法测试")
    class MaskByFieldTests {

        @Test
        @DisplayName("根据字段名脱敏")
        void testMaskByField() {
            LogMasking.registerRule("phone", LogMasking.MaskingStrategy.PHONE);

            String masked = LogMasking.maskByField("phone", "13812345678");

            assertThat(masked).isEqualTo("138****5678");
        }

        @Test
        @DisplayName("未注册字段返回原值")
        void testMaskByFieldNotRegistered() {
            String masked = LogMasking.maskByField("unknown", "value");
            assertThat(masked).isEqualTo("value");
        }

        @Test
        @DisplayName("null值返回null")
        void testMaskByFieldNullValue() {
            LogMasking.registerRule("phone", LogMasking.MaskingStrategy.PHONE);

            String masked = LogMasking.maskByField("phone", null);

            assertThat(masked).isNull();
        }

        @Test
        @DisplayName("空值返回空值")
        void testMaskByFieldEmptyValue() {
            LogMasking.registerRule("phone", LogMasking.MaskingStrategy.PHONE);

            String masked = LogMasking.maskByField("phone", "");

            assertThat(masked).isEmpty();
        }
    }

    @Nested
    @DisplayName("shouldMask方法测试")
    class ShouldMaskTests {

        @Test
        @DisplayName("已注册字段返回true")
        void testShouldMaskRegistered() {
            LogMasking.registerRule("secret", LogMasking.MaskingStrategy.FULL);

            assertThat(LogMasking.shouldMask("secret")).isTrue();
        }

        @Test
        @DisplayName("未注册字段返回false")
        void testShouldMaskNotRegistered() {
            assertThat(LogMasking.shouldMask("unknown")).isFalse();
        }

        @Test
        @DisplayName("不区分大小写")
        void testShouldMaskCaseInsensitive() {
            LogMasking.registerRule("Secret", LogMasking.MaskingStrategy.FULL);

            assertThat(LogMasking.shouldMask("SECRET")).isTrue();
            assertThat(LogMasking.shouldMask("secret")).isTrue();
        }
    }

    @Nested
    @DisplayName("MaskingStrategy枚举测试")
    class MaskingStrategyEnumTests {

        @Test
        @DisplayName("所有策略都存在")
        void testAllStrategiesExist() {
            assertThat(LogMasking.MaskingStrategy.values()).containsExactly(
                LogMasking.MaskingStrategy.FULL,
                LogMasking.MaskingStrategy.PHONE,
                LogMasking.MaskingStrategy.EMAIL,
                LogMasking.MaskingStrategy.ID_CARD,
                LogMasking.MaskingStrategy.BANK_CARD,
                LogMasking.MaskingStrategy.PASSWORD,
                LogMasking.MaskingStrategy.NAME,
                LogMasking.MaskingStrategy.ADDRESS,
                LogMasking.MaskingStrategy.CUSTOM
            );
        }

        @Test
        @DisplayName("valueOf方法正常工作")
        void testValueOf() {
            assertThat(LogMasking.MaskingStrategy.valueOf("FULL"))
                .isEqualTo(LogMasking.MaskingStrategy.FULL);
        }
    }

    @Nested
    @DisplayName("Mask注解测试")
    class MaskAnnotationTests {

        @Test
        @DisplayName("注解存在")
        void testAnnotationExists() {
            assertThat(LogMasking.Mask.class).isNotNull();
            assertThat(LogMasking.Mask.class.isAnnotation()).isTrue();
        }

        @Test
        @DisplayName("注解有默认值")
        void testAnnotationDefaultValue() throws NoSuchMethodException {
            var valueMethod = LogMasking.Mask.class.getMethod("value");
            var defaultValue = valueMethod.getDefaultValue();
            assertThat(defaultValue).isEqualTo(LogMasking.MaskingStrategy.FULL);
        }
    }
}
