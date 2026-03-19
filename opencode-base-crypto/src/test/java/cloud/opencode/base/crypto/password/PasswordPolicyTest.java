package cloud.opencode.base.crypto.password;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link PasswordPolicy}.
 * 密码策略单元测试。
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("PasswordPolicy Tests / 密码策略测试")
class PasswordPolicyTest {

    @Nested
    @DisplayName("Factory Method Tests / 工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("defaultPolicy()创建默认策略")
        void testDefaultPolicy() {
            PasswordPolicy policy = PasswordPolicy.defaultPolicy();
            assertThat(policy).isNotNull();
            assertThat(policy.getMinLength()).isEqualTo(12);
            assertThat(policy.getMaxLength()).isEqualTo(128);
        }

        @Test
        @DisplayName("strong()创建强密码策略")
        void testStrongPolicy() {
            PasswordPolicy policy = PasswordPolicy.strong();
            assertThat(policy).isNotNull();
            assertThat(policy.getMinLength()).isEqualTo(16);
            assertThat(policy.getMaxLength()).isEqualTo(128);
        }

        @Test
        @DisplayName("basic()创建基本策略")
        void testBasicPolicy() {
            PasswordPolicy policy = PasswordPolicy.basic();
            assertThat(policy).isNotNull();
            assertThat(policy.getMinLength()).isEqualTo(8);
            assertThat(policy.getMaxLength()).isEqualTo(128);
        }

        @Test
        @DisplayName("builder()创建构建器")
        void testBuilder() {
            PasswordPolicy.Builder builder = PasswordPolicy.builder();
            assertThat(builder).isNotNull();
        }
    }

    @Nested
    @DisplayName("Builder Tests / 构建器测试")
    class BuilderTests {

        @Test
        @DisplayName("builder构建默认实例")
        void testBuilderDefault() {
            PasswordPolicy policy = PasswordPolicy.builder().build();
            assertThat(policy).isNotNull();
            assertThat(policy.getMinLength()).isEqualTo(8);
            assertThat(policy.getMaxLength()).isEqualTo(128);
        }

        @Test
        @DisplayName("builder设置最小长度")
        void testBuilderMinLength() {
            PasswordPolicy policy = PasswordPolicy.builder()
                    .minLength(10)
                    .build();
            assertThat(policy.getMinLength()).isEqualTo(10);
        }

        @Test
        @DisplayName("builder设置最大长度")
        void testBuilderMaxLength() {
            PasswordPolicy policy = PasswordPolicy.builder()
                    .maxLength(64)
                    .build();
            assertThat(policy.getMaxLength()).isEqualTo(64);
        }

        @Test
        @DisplayName("builder设置要求大写字母")
        void testBuilderRequireUppercase() {
            PasswordPolicy policy = PasswordPolicy.builder()
                    .requireUppercase(true)
                    .build();
            assertThat(policy).isNotNull();
        }

        @Test
        @DisplayName("builder设置要求小写字母")
        void testBuilderRequireLowercase() {
            PasswordPolicy policy = PasswordPolicy.builder()
                    .requireLowercase(true)
                    .build();
            assertThat(policy).isNotNull();
        }

        @Test
        @DisplayName("builder设置要求数字")
        void testBuilderRequireDigit() {
            PasswordPolicy policy = PasswordPolicy.builder()
                    .requireDigit(true)
                    .build();
            assertThat(policy).isNotNull();
        }

        @Test
        @DisplayName("builder设置要求特殊字符")
        void testBuilderRequireSpecial() {
            PasswordPolicy policy = PasswordPolicy.builder()
                    .requireSpecial(true)
                    .build();
            assertThat(policy).isNotNull();
        }

        @Test
        @DisplayName("builder设置最小字符类型数")
        void testBuilderMinCharacterTypes() {
            PasswordPolicy policy = PasswordPolicy.builder()
                    .minCharacterTypes(3)
                    .build();
            assertThat(policy).isNotNull();
        }

        @Test
        @DisplayName("builder(minLength < 1)抛出异常")
        void testBuilderInvalidMinLength() {
            assertThatThrownBy(() -> PasswordPolicy.builder().minLength(0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("1");
        }

        @Test
        @DisplayName("builder(maxLength < 1)抛出异常")
        void testBuilderInvalidMaxLength() {
            assertThatThrownBy(() -> PasswordPolicy.builder().maxLength(0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("1");
        }

        @Test
        @DisplayName("builder(minCharacterTypes < 1)抛出异常")
        void testBuilderInvalidMinCharacterTypesTooLow() {
            assertThatThrownBy(() -> PasswordPolicy.builder().minCharacterTypes(0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("1");
        }

        @Test
        @DisplayName("builder(minCharacterTypes > 4)抛出异常")
        void testBuilderInvalidMinCharacterTypesTooHigh() {
            assertThatThrownBy(() -> PasswordPolicy.builder().minCharacterTypes(5))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("4");
        }

        @Test
        @DisplayName("builder(minLength > maxLength)抛出异常")
        void testBuilderMinGreaterThanMax() {
            assertThatThrownBy(() -> PasswordPolicy.builder()
                    .minLength(20)
                    .maxLength(10)
                    .build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("exceed");
        }

        @Test
        @DisplayName("builder链式调用")
        void testBuilderChaining() {
            PasswordPolicy policy = PasswordPolicy.builder()
                    .minLength(12)
                    .maxLength(64)
                    .requireUppercase(true)
                    .requireLowercase(true)
                    .requireDigit(true)
                    .requireSpecial(true)
                    .minCharacterTypes(4)
                    .build();
            assertThat(policy).isNotNull();
        }
    }

    @Nested
    @DisplayName("Validate Tests / 验证测试")
    class ValidateTests {

        @Test
        @DisplayName("validate有效密码返回成功")
        void testValidateValidPassword() {
            PasswordPolicy policy = PasswordPolicy.defaultPolicy();
            PasswordPolicy.ValidationResult result = policy.validate("Password123!");
            assertThat(result.isValid()).isTrue();
            assertThat(result.getViolations()).isEmpty();
        }

        @Test
        @DisplayName("validate(null)返回失败")
        void testValidateNull() {
            PasswordPolicy policy = PasswordPolicy.defaultPolicy();
            PasswordPolicy.ValidationResult result = policy.validate(null);
            assertThat(result.isValid()).isFalse();
            assertThat(result.getViolations()).contains("Password cannot be null");
        }

        @Test
        @DisplayName("validate太短密码返回失败")
        void testValidateTooShort() {
            PasswordPolicy policy = PasswordPolicy.builder().minLength(10).build();
            PasswordPolicy.ValidationResult result = policy.validate("short");
            assertThat(result.isValid()).isFalse();
            assertThat(result.getViolations()).anyMatch(v -> v.contains("at least 10 characters"));
        }

        @Test
        @DisplayName("validate太长密码返回失败")
        void testValidateTooLong() {
            PasswordPolicy policy = PasswordPolicy.builder().maxLength(10).build();
            PasswordPolicy.ValidationResult result = policy.validate("verylongpassword");
            assertThat(result.isValid()).isFalse();
            assertThat(result.getViolations()).anyMatch(v -> v.contains("at most 10 characters"));
        }

        @Test
        @DisplayName("validate缺少大写字母返回失败")
        void testValidateMissingUppercase() {
            PasswordPolicy policy = PasswordPolicy.builder().requireUppercase(true).build();
            PasswordPolicy.ValidationResult result = policy.validate("password123!");
            assertThat(result.isValid()).isFalse();
            assertThat(result.getViolations()).anyMatch(v -> v.contains("uppercase"));
        }

        @Test
        @DisplayName("validate缺少小写字母返回失败")
        void testValidateMissingLowercase() {
            PasswordPolicy policy = PasswordPolicy.builder().requireLowercase(true).build();
            PasswordPolicy.ValidationResult result = policy.validate("PASSWORD123!");
            assertThat(result.isValid()).isFalse();
            assertThat(result.getViolations()).anyMatch(v -> v.contains("lowercase"));
        }

        @Test
        @DisplayName("validate缺少数字返回失败")
        void testValidateMissingDigit() {
            PasswordPolicy policy = PasswordPolicy.builder().requireDigit(true).build();
            PasswordPolicy.ValidationResult result = policy.validate("Password!");
            assertThat(result.isValid()).isFalse();
            assertThat(result.getViolations()).anyMatch(v -> v.contains("digit"));
        }

        @Test
        @DisplayName("validate缺少特殊字符返回失败")
        void testValidateMissingSpecial() {
            PasswordPolicy policy = PasswordPolicy.builder().requireSpecial(true).build();
            PasswordPolicy.ValidationResult result = policy.validate("Password123");
            assertThat(result.isValid()).isFalse();
            assertThat(result.getViolations()).anyMatch(v -> v.contains("special character"));
        }

        @Test
        @DisplayName("validate字符类型不足返回失败")
        void testValidateInsufficientCharacterTypes() {
            PasswordPolicy policy = PasswordPolicy.builder().minCharacterTypes(4).build();
            PasswordPolicy.ValidationResult result = policy.validate("onlylowercase");
            assertThat(result.isValid()).isFalse();
            assertThat(result.getViolations()).anyMatch(v -> v.contains("character types"));
        }

        @Test
        @DisplayName("validate多个违规全部返回")
        void testValidateMultipleViolations() {
            PasswordPolicy policy = PasswordPolicy.builder()
                    .minLength(10)
                    .requireUppercase(true)
                    .requireDigit(true)
                    .build();
            PasswordPolicy.ValidationResult result = policy.validate("short");
            assertThat(result.isValid()).isFalse();
            assertThat(result.getViolations().size()).isGreaterThanOrEqualTo(3);
        }
    }

    @Nested
    @DisplayName("IsValid Tests / isValid测试")
    class IsValidTests {

        @Test
        @DisplayName("isValid有效密码返回true")
        void testIsValidTrue() {
            PasswordPolicy policy = PasswordPolicy.defaultPolicy();
            boolean valid = policy.isValid("Password123!");
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("isValid无效密码返回false")
        void testIsValidFalse() {
            PasswordPolicy policy = PasswordPolicy.defaultPolicy();
            boolean valid = policy.isValid("weak");
            assertThat(valid).isFalse();
        }
    }

    @Nested
    @DisplayName("ValidationResult Tests / 验证结果测试")
    class ValidationResultTests {

        @Test
        @DisplayName("getViolations返回不可变列表")
        void testGetViolationsImmutable() {
            PasswordPolicy policy = PasswordPolicy.builder().minLength(100).build();
            PasswordPolicy.ValidationResult result = policy.validate("short");
            List<String> violations = result.getViolations();
            assertThatThrownBy(() -> violations.add("new violation"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("getFirstViolation返回第一个违规")
        void testGetFirstViolation() {
            PasswordPolicy policy = PasswordPolicy.builder().minLength(100).build();
            PasswordPolicy.ValidationResult result = policy.validate("short");
            String firstViolation = result.getFirstViolation();
            assertThat(firstViolation).isNotNull();
            assertThat(firstViolation).contains("at least 100 characters");
        }

        @Test
        @DisplayName("getFirstViolation有效密码返回null")
        void testGetFirstViolationNull() {
            PasswordPolicy policy = PasswordPolicy.builder().minLength(5).build();
            PasswordPolicy.ValidationResult result = policy.validate("validpassword");
            String firstViolation = result.getFirstViolation();
            assertThat(firstViolation).isNull();
        }
    }

    @Nested
    @DisplayName("Getter Tests / Getter测试")
    class GetterTests {

        @Test
        @DisplayName("getMinLength返回正确值")
        void testGetMinLength() {
            PasswordPolicy policy = PasswordPolicy.builder().minLength(15).build();
            assertThat(policy.getMinLength()).isEqualTo(15);
        }

        @Test
        @DisplayName("getMaxLength返回正确值")
        void testGetMaxLength() {
            PasswordPolicy policy = PasswordPolicy.builder().maxLength(50).build();
            assertThat(policy.getMaxLength()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("Policy Scenario Tests / 策略场景测试")
    class PolicyScenarioTests {

        @Test
        @DisplayName("默认策略验证强密码")
        void testDefaultPolicyStrongPassword() {
            PasswordPolicy policy = PasswordPolicy.defaultPolicy();
            // Strong password with all requirements
            assertThat(policy.isValid("MyStrongP@ss123")).isTrue();
        }

        @Test
        @DisplayName("默认策略拒绝弱密码")
        void testDefaultPolicyWeakPassword() {
            PasswordPolicy policy = PasswordPolicy.defaultPolicy();
            // Weak password - missing special char
            assertThat(policy.isValid("MyPassword123")).isFalse();
        }

        @Test
        @DisplayName("强策略要求更复杂密码")
        void testStrongPolicyRequiresComplexPassword() {
            PasswordPolicy policy = PasswordPolicy.strong();
            // Password that passes default but fails strong (too short)
            assertThat(policy.isValid("Password123!")).isFalse(); // < 16 chars
            assertThat(policy.isValid("VeryStrong@Password1")).isTrue();
        }

        @Test
        @DisplayName("基本策略接受简单密码")
        void testBasicPolicyAcceptsSimplePassword() {
            PasswordPolicy policy = PasswordPolicy.basic();
            // Simple password with 2 character types
            assertThat(policy.isValid("password1")).isTrue();
            assertThat(policy.isValid("PASSWORD1")).isTrue();
        }

        @Test
        @DisplayName("基本策略拒绝太简单密码")
        void testBasicPolicyRejectsTooSimple() {
            PasswordPolicy policy = PasswordPolicy.basic();
            // Only one character type
            assertThat(policy.isValid("password")).isFalse();
        }
    }

    @Nested
    @DisplayName("Special Characters Tests / 特殊字符测试")
    class SpecialCharactersTests {

        @Test
        @DisplayName("识别各种特殊字符")
        void testRecognizeSpecialCharacters() {
            PasswordPolicy policy = PasswordPolicy.builder()
                    .minLength(8)
                    .requireSpecial(true)
                    .build();

            // Test various special characters
            assertThat(policy.isValid("Password!")).isTrue();
            assertThat(policy.isValid("Password@")).isTrue();
            assertThat(policy.isValid("Password#")).isTrue();
            assertThat(policy.isValid("Password$")).isTrue();
            assertThat(policy.isValid("Password%")).isTrue();
            assertThat(policy.isValid("Password^")).isTrue();
            assertThat(policy.isValid("Password&")).isTrue();
            assertThat(policy.isValid("Password*")).isTrue();
            assertThat(policy.isValid("Password(")).isTrue();
            assertThat(policy.isValid("Password)")).isTrue();
            assertThat(policy.isValid("Password_")).isTrue();
            assertThat(policy.isValid("Password+")).isTrue();
            assertThat(policy.isValid("Password-")).isTrue();
            assertThat(policy.isValid("Password=")).isTrue();
        }
    }

    @Nested
    @DisplayName("Character Types Tests / 字符类型测试")
    class CharacterTypesTests {

        @Test
        @DisplayName("计算单一类型")
        void testSingleCharacterType() {
            PasswordPolicy policy = PasswordPolicy.builder()
                    .minLength(8)
                    .minCharacterTypes(1)
                    .build();
            assertThat(policy.isValid("password")).isTrue();
        }

        @Test
        @DisplayName("计算两种类型")
        void testTwoCharacterTypes() {
            PasswordPolicy policy = PasswordPolicy.builder()
                    .minLength(8)
                    .minCharacterTypes(2)
                    .build();
            assertThat(policy.isValid("Password")).isTrue(); // upper + lower
            assertThat(policy.isValid("password1")).isTrue(); // lower + digit
        }

        @Test
        @DisplayName("计算三种类型")
        void testThreeCharacterTypes() {
            PasswordPolicy policy = PasswordPolicy.builder()
                    .minLength(8)
                    .minCharacterTypes(3)
                    .build();
            assertThat(policy.isValid("Password1")).isTrue(); // upper + lower + digit
        }

        @Test
        @DisplayName("计算四种类型")
        void testFourCharacterTypes() {
            PasswordPolicy policy = PasswordPolicy.builder()
                    .minLength(8)
                    .minCharacterTypes(4)
                    .build();
            assertThat(policy.isValid("Password1!")).isTrue(); // all 4 types
        }
    }
}
