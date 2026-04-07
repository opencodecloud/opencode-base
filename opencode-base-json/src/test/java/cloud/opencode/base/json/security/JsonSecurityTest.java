package cloud.opencode.base.json.security;

import cloud.opencode.base.json.JsonNode;
import cloud.opencode.base.json.annotation.JsonMask;
import cloud.opencode.base.json.exception.OpenJsonProcessingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * JsonSecurity 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
@DisplayName("JsonSecurity 测试")
class JsonSecurityTest {

    @Nested
    @DisplayName("常量测试")
    class ConstantsTests {

        @Test
        @DisplayName("默认最大深度")
        void testDefaultMaxDepth() {
            assertThat(JsonSecurity.DEFAULT_MAX_DEPTH).isEqualTo(1000);
        }

        @Test
        @DisplayName("默认最大字符串长度")
        void testDefaultMaxStringLength() {
            assertThat(JsonSecurity.DEFAULT_MAX_STRING_LENGTH).isEqualTo(20_000_000);
        }

        @Test
        @DisplayName("默认最大条目数")
        void testDefaultMaxEntries() {
            assertThat(JsonSecurity.DEFAULT_MAX_ENTRIES).isEqualTo(100_000);
        }
    }

    @Nested
    @DisplayName("mask方法测试-PASSWORD")
    class MaskPasswordTests {

        @Test
        @DisplayName("密码脱敏")
        void testMaskPassword() {
            String result = JsonSecurity.mask("password123", JsonMask.MaskType.PASSWORD);
            assertThat(result).isEqualTo("******");
        }

        @Test
        @DisplayName("空值保持不变")
        void testMaskPasswordNull() {
            assertThat(JsonSecurity.mask(null, JsonMask.MaskType.PASSWORD)).isNull();
        }

        @Test
        @DisplayName("空字符串保持不变")
        void testMaskPasswordEmpty() {
            assertThat(JsonSecurity.mask("", JsonMask.MaskType.PASSWORD)).isEmpty();
        }
    }

    @Nested
    @DisplayName("mask方法测试-PHONE")
    class MaskPhoneTests {

        @Test
        @DisplayName("手机号脱敏")
        void testMaskPhone() {
            String result = JsonSecurity.mask("13812345678", JsonMask.MaskType.PHONE);
            assertThat(result).isEqualTo("138****5678");
        }

        @Test
        @DisplayName("短手机号完全脱敏")
        void testMaskShortPhone() {
            String result = JsonSecurity.mask("12345", JsonMask.MaskType.PHONE);
            assertThat(result).isEqualTo("*****");
        }
    }

    @Nested
    @DisplayName("mask方法测试-ID_CARD")
    class MaskIdCardTests {

        @Test
        @DisplayName("身份证脱敏")
        void testMaskIdCard() {
            String result = JsonSecurity.mask("110101199001011234", JsonMask.MaskType.ID_CARD);
            assertThat(result).isEqualTo("110***********1234");
        }

        @Test
        @DisplayName("短身份证完全脱敏")
        void testMaskShortIdCard() {
            String result = JsonSecurity.mask("1234567", JsonMask.MaskType.ID_CARD);
            assertThat(result).isEqualTo("*******");
        }
    }

    @Nested
    @DisplayName("mask方法测试-EMAIL")
    class MaskEmailTests {

        @Test
        @DisplayName("邮箱脱敏")
        void testMaskEmail() {
            String result = JsonSecurity.mask("test@example.com", JsonMask.MaskType.EMAIL);
            assertThat(result).isEqualTo("t***@example.com");
        }

        @Test
        @DisplayName("短用户名邮箱全部脱敏")
        void testMaskShortEmail() {
            String result = JsonSecurity.mask("t@example.com", JsonMask.MaskType.EMAIL);
            assertThat(result).isEqualTo("*************");
        }
    }

    @Nested
    @DisplayName("mask方法测试-BANK_CARD")
    class MaskBankCardTests {

        @Test
        @DisplayName("银行卡脱敏")
        void testMaskBankCard() {
            String result = JsonSecurity.mask("6222021234567890", JsonMask.MaskType.BANK_CARD);
            assertThat(result).isEqualTo("6222****7890");
        }

        @Test
        @DisplayName("短银行卡完全脱敏")
        void testMaskShortBankCard() {
            String result = JsonSecurity.mask("1234567", JsonMask.MaskType.BANK_CARD);
            assertThat(result).isEqualTo("*******");
        }
    }

    @Nested
    @DisplayName("mask方法测试-NAME")
    class MaskNameTests {

        @Test
        @DisplayName("姓名脱敏-中文")
        void testMaskChineseName() {
            String result = JsonSecurity.mask("张三丰", JsonMask.MaskType.NAME);
            assertThat(result).isEqualTo("张**");
        }

        @Test
        @DisplayName("姓名脱敏-英文")
        void testMaskEnglishName() {
            String result = JsonSecurity.mask("John", JsonMask.MaskType.NAME);
            assertThat(result).isEqualTo("J***");
        }

        @Test
        @DisplayName("单字符姓名保持不变")
        void testMaskSingleCharName() {
            String result = JsonSecurity.mask("张", JsonMask.MaskType.NAME);
            assertThat(result).isEqualTo("张");
        }
    }

    @Nested
    @DisplayName("mask方法测试-ADDRESS")
    class MaskAddressTests {

        @Test
        @DisplayName("地址脱敏")
        void testMaskAddress() {
            String result = JsonSecurity.mask("北京市朝阳区xxx街道123号", JsonMask.MaskType.ADDRESS);
            assertThat(result).isEqualTo("北京市朝阳区****");
        }

        @Test
        @DisplayName("短地址完全脱敏")
        void testMaskShortAddress() {
            String result = JsonSecurity.mask("北京", JsonMask.MaskType.ADDRESS);
            assertThat(result).isEqualTo("**");
        }
    }

    @Nested
    @DisplayName("mask方法测试-FULL")
    class MaskFullTests {

        @Test
        @DisplayName("完全脱敏")
        void testMaskFull() {
            String result = JsonSecurity.mask("anything", JsonMask.MaskType.FULL);
            assertThat(result).isEqualTo("******");
        }
    }

    @Nested
    @DisplayName("mask方法测试-自定义")
    class MaskCustomTests {

        @Test
        @DisplayName("自定义前后缀脱敏")
        void testMaskWithPrefixSuffix() {
            String result = JsonSecurity.mask("1234567890", 3, 4, '*');
            assertThat(result).isEqualTo("123***7890");
        }

        @Test
        @DisplayName("自定义脱敏字符")
        void testMaskWithCustomChar() {
            String result = JsonSecurity.mask("13812345678", JsonMask.MaskType.PHONE, '#');
            assertThat(result).isEqualTo("138####5678");
        }

        @Test
        @DisplayName("前后缀长度大于字符串长度时完全脱敏")
        void testMaskOverflowPrefixSuffix() {
            String result = JsonSecurity.mask("123", 5, 5, '*');
            assertThat(result).isEqualTo("***");
        }
    }

    @Nested
    @DisplayName("maskWithPattern方法测试")
    class MaskWithPatternTests {

        @Test
        @DisplayName("正则表达式脱敏")
        void testMaskWithPattern() {
            String result = JsonSecurity.maskWithPattern("abc123def", "\\d", "*");
            assertThat(result).isEqualTo("abc***def");
        }

        @Test
        @DisplayName("null值返回null")
        void testMaskWithPatternNull() {
            assertThat(JsonSecurity.maskWithPattern(null, ".*", "*")).isNull();
        }
    }

    @Nested
    @DisplayName("validateDepth方法测试")
    class ValidateDepthTests {

        @Test
        @DisplayName("深度在限制内不抛出异常")
        void testValidateDepthValid() {
            JsonNode node = JsonNode.object()
                .put("level1", JsonNode.object()
                    .put("level2", JsonNode.object()
                        .put("value", "test")));

            assertThatCode(() -> JsonSecurity.validateDepth(node, 10))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("深度超过限制抛出异常")
        void testValidateDepthExceeds() {
            JsonNode node = JsonNode.object()
                .put("level1", JsonNode.object()
                    .put("level2", JsonNode.object()
                        .put("value", "test")));

            assertThatThrownBy(() -> JsonSecurity.validateDepth(node, 2))
                .isInstanceOf(OpenJsonProcessingException.class)
                .hasMessageContaining("depth");
        }
    }

    @Nested
    @DisplayName("validateSize方法测试")
    class ValidateSizeTests {

        @Test
        @DisplayName("大小在限制内不抛出异常")
        void testValidateSizeValid() {
            JsonNode node = JsonNode.object()
                .put("a", 1)
                .put("b", 2);

            assertThatCode(() -> JsonSecurity.validateSize(node, 10))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("大小超过限制抛出异常")
        void testValidateSizeExceeds() {
            JsonNode node = JsonNode.object()
                .put("a", 1)
                .put("b", 2)
                .put("c", 3);

            assertThatThrownBy(() -> JsonSecurity.validateSize(node, 2))
                .isInstanceOf(OpenJsonProcessingException.class)
                .hasMessageContaining("size");
        }
    }

    @Nested
    @DisplayName("calculateDepth方法测试")
    class CalculateDepthTests {

        @Test
        @DisplayName("计算空对象深度")
        void testCalculateDepthEmpty() {
            assertThat(JsonSecurity.calculateDepth(JsonNode.object())).isEqualTo(0);
        }

        @Test
        @DisplayName("计算嵌套对象深度")
        void testCalculateDepthNested() {
            JsonNode node = JsonNode.object()
                .put("level1", JsonNode.object()
                    .put("level2", JsonNode.object()));

            assertThat(JsonSecurity.calculateDepth(node)).isEqualTo(2);
        }

        @Test
        @DisplayName("计算数组深度")
        void testCalculateDepthArray() {
            JsonNode node = JsonNode.array()
                .add(JsonNode.array().add(1));

            assertThat(JsonSecurity.calculateDepth(node)).isEqualTo(2);
        }

        @Test
        @DisplayName("null节点返回0")
        void testCalculateDepthNull() {
            assertThat(JsonSecurity.calculateDepth(null)).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("calculateSize方法测试")
    class CalculateSizeTests {

        @Test
        @DisplayName("计算对象大小")
        void testCalculateSizeObject() {
            JsonNode node = JsonNode.object()
                .put("a", 1)
                .put("b", 2);

            assertThat(JsonSecurity.calculateSize(node)).isEqualTo(3); // 1 object + 2 values
        }

        @Test
        @DisplayName("计算数组大小")
        void testCalculateSizeArray() {
            JsonNode node = JsonNode.array().add(1).add(2).add(3);

            assertThat(JsonSecurity.calculateSize(node)).isEqualTo(4); // 1 array + 3 elements
        }
    }

    @Nested
    @DisplayName("findDangerousKeys方法测试")
    class FindDangerousKeysTests {

        @Test
        @DisplayName("检测__proto__键")
        void testFindProtoKey() {
            JsonNode node = JsonNode.object().put("__proto__", "value");

            List<String> dangerous = JsonSecurity.findDangerousKeys(node);

            assertThat(dangerous).contains("__proto__");
        }

        @Test
        @DisplayName("检测constructor键")
        void testFindConstructorKey() {
            JsonNode node = JsonNode.object().put("constructor", "value");

            List<String> dangerous = JsonSecurity.findDangerousKeys(node);

            assertThat(dangerous).contains("constructor");
        }

        @Test
        @DisplayName("检测嵌套危险键")
        void testFindNestedDangerousKey() {
            JsonNode node = JsonNode.object()
                .put("nested", JsonNode.object().put("$where", "value"));

            List<String> dangerous = JsonSecurity.findDangerousKeys(node);

            assertThat(dangerous).contains("nested.$where");
        }

        @Test
        @DisplayName("无危险键返回空列表")
        void testFindNoDangerousKeys() {
            JsonNode node = JsonNode.object().put("safe", "value");

            List<String> dangerous = JsonSecurity.findDangerousKeys(node);

            assertThat(dangerous).isEmpty();
        }
    }

    @Nested
    @DisplayName("hasDangerousKeys方法测试")
    class HasDangerousKeysTests {

        @Test
        @DisplayName("有危险键返回true")
        void testHasDangerousKeysTrue() {
            JsonNode node = JsonNode.object().put("eval", "value");

            assertThat(JsonSecurity.hasDangerousKeys(node)).isTrue();
        }

        @Test
        @DisplayName("无危险键返回false")
        void testHasDangerousKeysFalse() {
            JsonNode node = JsonNode.object().put("safe", "value");

            assertThat(JsonSecurity.hasDangerousKeys(node)).isFalse();
        }
    }

    @Nested
    @DisplayName("sanitizeForHtml方法测试")
    class SanitizeForHtmlTests {

        @Test
        @DisplayName("转义HTML特殊字符")
        void testSanitizeForHtml() {
            String result = JsonSecurity.sanitizeForHtml("<script>alert('xss')</script>");

            assertThat(result).doesNotContain("<", ">", "'");
            assertThat(result).contains("&lt;", "&gt;", "&#x27;");
        }

        @Test
        @DisplayName("转义双引号")
        void testSanitizeDoubleQuotes() {
            String result = JsonSecurity.sanitizeForHtml("\"quoted\"");

            assertThat(result).contains("&quot;");
        }

        @Test
        @DisplayName("转义&符号")
        void testSanitizeAmpersand() {
            String result = JsonSecurity.sanitizeForHtml("a & b");

            assertThat(result).isEqualTo("a &amp; b");
        }

        @Test
        @DisplayName("null返回null")
        void testSanitizeNull() {
            assertThat(JsonSecurity.sanitizeForHtml((String) null)).isNull();
        }

        @Test
        @DisplayName("净化JsonNode")
        void testSanitizeJsonNode() {
            JsonNode node = JsonNode.object()
                .put("name", "<script>alert('xss')</script>");

            JsonNode result = JsonSecurity.sanitizeForHtml(node);

            assertThat(result.get("name").asString()).doesNotContain("<", ">");
        }
    }

    @Nested
    @DisplayName("SecurityOptions测试")
    class SecurityOptionsTests {

        @Test
        @DisplayName("defaults返回默认选项")
        void testDefaults() {
            JsonSecurity.SecurityOptions options = JsonSecurity.SecurityOptions.defaults();

            assertThat(options.maxDepth()).isEqualTo(JsonSecurity.DEFAULT_MAX_DEPTH);
            assertThat(options.maxStringLength()).isEqualTo(JsonSecurity.DEFAULT_MAX_STRING_LENGTH);
            assertThat(options.maxEntries()).isEqualTo(JsonSecurity.DEFAULT_MAX_ENTRIES);
            assertThat(options.rejectDangerousKeys()).isTrue();
            assertThat(options.sanitizeStrings()).isFalse();
        }

        @Test
        @DisplayName("builder创建自定义选项")
        void testBuilder() {
            JsonSecurity.SecurityOptions options = JsonSecurity.SecurityOptions.builder()
                .maxDepth(50)
                .maxStringLength(1000)
                .maxEntries(100)
                .rejectDangerousKeys(false)
                .sanitizeStrings(true)
                .build();

            assertThat(options.maxDepth()).isEqualTo(50);
            assertThat(options.maxStringLength()).isEqualTo(1000);
            assertThat(options.maxEntries()).isEqualTo(100);
            assertThat(options.rejectDangerousKeys()).isFalse();
            assertThat(options.sanitizeStrings()).isTrue();
        }
    }

    @Nested
    @DisplayName("validate方法测试")
    class ValidateTests {

        @Test
        @DisplayName("验证通过的文档")
        void testValidateSuccess() {
            JsonNode node = JsonNode.object().put("name", "test");
            JsonSecurity.SecurityOptions options = JsonSecurity.SecurityOptions.defaults();

            assertThatCode(() -> JsonSecurity.validate(node, options))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("检测危险键时验证失败")
        void testValidateFailsDangerousKeys() {
            JsonNode node = JsonNode.object().put("__proto__", "value");
            JsonSecurity.SecurityOptions options = JsonSecurity.SecurityOptions.defaults();

            assertThatThrownBy(() -> JsonSecurity.validate(node, options))
                .isInstanceOf(OpenJsonProcessingException.class)
                .hasMessageContaining("dangerous keys");
        }
    }
}
